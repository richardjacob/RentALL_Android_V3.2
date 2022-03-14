package com.rentall.radicalstart.ui.profile.about

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.AboutActivityBinding
import com.rentall.radicalstart.ui.WebViewActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.profile.about.why_Host.WhyHostActivity
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.withModels
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class AboutActivity : BaseActivity<AboutActivityBinding, AboutViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var mBinding: AboutActivityBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.about_activity
    override val viewModel: AboutViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(AboutViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!

        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.about)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }


        setUp()

    }

    fun setUp(){
        mBinding.rvSetting.withModels {
            val pinfo = packageManager.getPackageInfo(packageName, 0)
            val versionNumber = pinfo.versionCode
            val versionname = pinfo.versionName
            viewholderProfileLists {
                id("whyHost")
                name(getString(R.string.why_host))
                paddingbottam(true)
                paddingtop(true)
                iconVisible(true)
                onClick(View.OnClickListener {
                    val intent = Intent(this@AboutActivity, WhyHostActivity::class.java)
                    startActivity(intent)
                })
            }

            viewholderDivider {
                id("div1")
            }

            viewholderProfileLists {
                id("privateDetails")
                name(getString(R.string.terms_privacy))
                iconVisible(true)
                paddingbottam(true)
                paddingtop(true)
                onClick(View.OnClickListener {
                    val privateURL = Constants.WEBSITE+"/privacy/"
                    WebViewActivity.openWebViewActivity(this@AboutActivity, privateURL, getString(R.string.terms_privacy)) })
            }
            viewholderDivider {
                id("div2")
            }

            viewholderProfileLists {
                id("aboutUs")
                name(getString(R.string.about_us))
                iconVisible(true)
                paddingbottam(true)
                paddingtop(true)
                onClick(View.OnClickListener {
                    val aboutURL = Constants.WEBSITE+"/about/"
                    WebViewActivity.openWebViewActivity(this@AboutActivity, aboutURL, getString(R.string.about_us))
                })
            }
            viewholderDivider {
                id("div3")
            }

            viewholderProfileLists {
                id("trustnSafety")
                name(getString(R.string.trust_and_safty))
                iconVisible(true)
                paddingbottam(true)
                paddingtop(true)
                onClick(View.OnClickListener {
                    val trustURL = Constants.WEBSITE+"/safety/"
                    WebViewActivity.openWebViewActivity(this@AboutActivity, trustURL, getString(R.string.trust_and_safty))
                })
            }
            viewholderDivider {
                id("div4")
            }

            viewholderProfileLists {
                id("version")
                name(getString(R.string.version))
                iconVisible(true)
                paddingbottam(true)
                paddingtop(true)
                textVisible(true)
                currencyText(versionname.toString())

            }

        }
    }

    private fun getBitmapFromVectorDrawable(@DrawableRes drawableId: Int): Bitmap? {
        var drawable = AppCompatResources.getDrawable(this!!, drawableId) ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onRetry() {

    }

}