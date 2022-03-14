package com.rentall.radicalstart.ui.listing.share

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.ActivityShareBinding
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels

private const val ARG_ID = "id"
private const val ARG_TITLE = "title"
private const val ARG_IMAGE = "image"

class ShareActivity: AppCompatActivity() {

    private lateinit var binding: ActivityShareBinding
    private lateinit var resolveInfoList: List<ResolveInfo>
    private var mCurrentState = State.COLLAPSED
    private var id: Int? = null
    private var title: String? = null
    private var img: String? = null

    companion object {
        @JvmStatic fun openShareIntent(
                context: Context,
                id: Int,
                title: String,
                img: String
        ) {
            val intent = Intent(context, ShareActivity::class.java)
            intent.putExtra(ARG_ID, id)
            intent.putExtra(ARG_TITLE, title)
            intent.putExtra(ARG_IMAGE, img)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent?.let {
            id = it.getIntExtra(ARG_ID, 0)
            title = it.getStringExtra(ARG_TITLE)
            img = it.getStringExtra(ARG_IMAGE)
        }
        binding.ivNavigateup.onClick { finish() }
        shareIntentSpecificApps()
    }

    enum class State {
        EXPANDED,
        COLLAPSED
    }

    private fun shareIntentSpecificApps() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0)
        initRecycler()
    }

    private fun sendTextToIntent(packageName: String, name: String) {
        val intent = Intent()
        intent.component = ComponentName(packageName, name)
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.check_out_this_listing_on_appname) + " $title ")
        intent.putExtra(Intent.EXTRA_TEXT, Constants.shareUrl + id)
        startActivity(intent)
    }

    private fun toggleAnimation(colorFrom: Int, colorTo: Int) {
        val colorFrom1 = ContextCompat.getColor(this@ShareActivity, colorFrom)
        val colorTo1 = ContextCompat.getColor(this@ShareActivity, colorTo)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom1, colorTo1)
        colorAnimation.duration = 200 // milliseconds
        colorAnimation.addUpdateListener {
            animator -> binding.toolbarListingDetails.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    private fun initRecycler() {
        EpoxyVisibilityTracker().attach(binding.rlShareApp)
        binding.rlShareApp.withModels {
            viewholderShareListingCard {
                id("ShareCard")
                img(img)
                title(title)
                onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                    if (percentVisibleHeight < 20) {
                        if (mCurrentState != State.EXPANDED) {
                            toggleAnimation(R.color.transparent, R.color.white)
                            binding.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_clear_black_24dp))
                        }
                        mCurrentState = State.EXPANDED
                    } else {
                        if (mCurrentState != State.COLLAPSED) {
                            toggleAnimation(R.color.white, R.color.transparent)
                            binding.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_close_white_24dp))
                        }
                        mCurrentState = State.COLLAPSED
                    }
                }
            }

            viewholderShareList {
                id("ShareList")
                name(resources.getString(R.string.copied_to_clipboard))
                icon(ContextCompat.getDrawable(this@ShareActivity, R.drawable.ic_content_copy_black_24dp))
                clickListener(View.OnClickListener { copyTextToClipboard() })
            }

            for (resInfo in resolveInfoList) {

                viewholderDivider { id(1) }

                val packageName = resInfo.activityInfo.packageName
                val className = resInfo.activityInfo.name
                val name = resInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString()
                val image = resInfo.activityInfo.applicationInfo.loadIcon(packageManager)

                viewholderShareList {
                    id(packageName)
                    name(name)
                    packageName(packageName)
                    icon(image)
                    clickListener(View.OnClickListener { sendTextToIntent(packageName, className)})
                }
            }
        }
    }

    private fun copyTextToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(resources.getString(R.string.copied_to_clipboard), Constants.shareUrl + id)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@ShareActivity, resources.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_change, R.anim.slide_down)
    }
}