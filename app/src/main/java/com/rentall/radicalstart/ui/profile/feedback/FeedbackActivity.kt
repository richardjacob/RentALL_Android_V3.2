package com.rentall.radicalstart.ui.profile.feedback

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.ActivityFeedbackBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class FeedbackActivity: BaseActivity<ActivityFeedbackBinding,FeedbackViewModel>(),FeedbackNavigator {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_feedback
    override val viewModel: FeedbackViewModel
        get() = ViewModelProviders.of(this,mViewModelFactory).get(FeedbackViewModel::class.java)
    private lateinit var mBinding : ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = this.viewDataBinding!!
        viewModel.navigator = this
        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.feedback)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }
        setUp()
    }

    fun setUp(){
        mBinding.rvFeedback.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.how_we_doing))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("content")
                text(getString(R.string.feedback_content))
                paddingTop(false)
                paddingBottom(true)
            }

            viewholderItineraryTextBold {
                id("liketodo")
                text(getString(R.string.like_to_do))
                isRed(false)
                large(false)
                paddingBottom(true)
                paddingTop(true)
            }

            viewholderFeedback {
                id("feedback")
                text(getString(R.string.give_product_feedback))
                image(R.drawable.feedback)
                onClick(View.OnClickListener {
                    openFeedBackDialog()
                })
            }

            viewholderFeedback {
                id("bug")
                text(getString(R.string.report_bug))
                image(R.drawable.bug)
                onClick(View.OnClickListener {
                    openReportBugDialog()
                })
            }

        }
    }

    private fun openFeedBackDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.feedback))
        val dialogLayout = inflater.inflate(R.layout.feedback_alert, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        builder.setView(dialogLayout)
        viewModel.feedbackType.set("Feed Back")
        viewModel.msg.set(editText.text.toString())
         builder.setPositiveButton(getString(R.string.send)) { dialogInterface, i ->
             if (editText.text.trim().isNotBlank() || editText.text.trim().isNotEmpty() || editText.text.equals("")){
                 viewModel.sendFeedback(viewModel.feedbackType.get().toString(), viewModel.msg.get().toString())
             }else{
                 showToast(getString(R.string.please_enter_feedback))
             }
              }
         builder.setNegativeButton(getString(R.string.cancel)) {
             dialogInterface, i -> dialogInterface.dismiss()
         }
         builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
             dialog.dismiss()
         }
        builder.show()
    }

    private fun openReportBugDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.bug))
        val dialogLayout = inflater.inflate(R.layout.feedback_alert, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        editText.setHint(getString(R.string.enter_bug_here))
        viewModel.feedbackType.set("Bug")
        viewModel.msg.set(editText.text.toString())
        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
            if (editText.text.trim().isNotBlank() || editText.text.trim().isNotEmpty() || editText.text.equals("")){
                viewModel.sendFeedback(viewModel.feedbackType.get().toString(), viewModel.msg.get().toString())
            }else{
                showToast(getString(R.string.pls_enter_bug))
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) {
            dialogInterface, i -> dialogInterface.dismiss()
        }
        builder.show()
    }

    override fun onRetry() {
       viewModel.sendFeedback(viewModel.feedbackType.get().toString(), viewModel.msg.get().toString())
    }
}