package com.rentall.radicalstart.ui.user_profile.report_user

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentUserProfileBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.user_profile.UserProfileNavigator
import com.rentall.radicalstart.ui.user_profile.UserProfileViewModel
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class ReportUserFragment : BaseFragment<FragmentUserProfileBinding, UserProfileViewModel>(), UserProfileNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentUserProfileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_user_profile
    override val viewModel: UserProfileViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(UserProfileViewModel::class.java)
    private var selectArray = arrayOf(false, false, false)
    private var isSelected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        setUp()
    }

    private fun initView() {
        mBinding.text = viewModel.selectContent
        mBinding.actionBar.root.gone()
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.tvToolbarHeading.gone()
        mBinding.actionBar.ivNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.tvOk.onClick {
            if (isSelected) {
                viewModel.reportUser()
            } else {
                Toast.makeText(context, resources.getString(R.string.please_select_the_option), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setUp() {
        mBinding.rvUserProfile.withModels {
            viewholderUserName {
                id("header")
                name(resources.getString(R.string.do_you_want_to_report_this_user))
                paddingTop(true)
            }
            viewholderUserNormalText {
                id("headerInfo")
                text(resources.getString(R.string.if_so_please_choose_one_of_the_following_reasons))
                paddingBottom(true)
            }
            viewholderReportUserRadio {
                viewModel.resourceProvider
                id("1")
                text(resources.getString(R.string.this_profile_should_not_be_on_appname))
                radioVisibility(selectArray[0])
                onClick(View.OnClickListener { selector(0); viewModel.selectContent.set("Shouldn't available") })
            }
            viewholderDivider {
                id("Divider - 1")
            }
            viewholderReportUserRadio {
                id("2")
                text(resources.getString(R.string.attempt_to_share_contact_information))
                radioVisibility(selectArray[1])
                onClick(View.OnClickListener { selector(1); viewModel.selectContent.set("Direct contact") })
            }
            viewholderDivider {
                id("Divider - 2")
            }
            viewholderReportUserRadio {
                id("3")
                text(resources.getString(R.string.inappropriate_content_of_spam))
                radioVisibility(selectArray[2])
                onClick(View.OnClickListener { selector(2); viewModel.selectContent.set("Spam") })
            }
        }
    }

    private fun selector(index: Int) {
        selectArray.forEachIndexed { i: Int, _: Boolean ->
            selectArray[i] = index == i
            isSelected = true
        }
        mBinding.rvUserProfile.requestModelBuild()
    }

    override fun onDestroyView() {
        viewModel.selectContent.set("")
        super.onDestroyView()
    }

    override fun closeScreen() {
        baseActivity?.onBackPressed()
    }

    override fun onRetry() {

    }

}
