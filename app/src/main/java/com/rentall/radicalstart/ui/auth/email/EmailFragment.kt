package com.rentall.radicalstart.ui.auth.email

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAuthEmailVerificationBinding
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.UiEvent
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"

class EmailFragment : BaseFragment<FragmentAuthEmailVerificationBinding, EmailVerificationViewModel>(), AuthNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_email_verification
    override val viewModel: EmailVerificationViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(EmailVerificationViewModel::class.java)
    lateinit var mBinding: FragmentAuthEmailVerificationBinding
    private var param1: String = ""

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
                EmailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        arguments?.let {
            param1 = it.getString(ARG_PARAM1, "")
        }
        initView()
    }

    private fun initView() {
        mBinding.ltLoadingView.setImageResource(R.drawable.ic_right_arrow_blue)
        mBinding.actionBar.tvRightside.gone()
        mBinding.actionBar.rlToolbarNavigateup.onClick { baseActivity?.onBackPressed() }
        viewModel.email.set(param1)
    }

    override fun onResume() {
        super.onResume()
        viewModel.lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {
        viewModel.checkEmail()
    }
}