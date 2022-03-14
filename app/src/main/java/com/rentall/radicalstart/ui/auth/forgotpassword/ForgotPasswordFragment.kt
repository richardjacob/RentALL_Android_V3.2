package com.rentall.radicalstart.ui.auth.forgotpassword

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAuthForgotPasswordBinding
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.UiEvent
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class ForgotPasswordFragment : BaseFragment<FragmentAuthForgotPasswordBinding, ForgotPasswordViewModel>(), AuthNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_forgot_password
    override val viewModel: ForgotPasswordViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(ForgotPasswordViewModel::class.java)
    lateinit var mBinding: FragmentAuthForgotPasswordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
    }

    private fun initView() {
        mBinding.actionBar.tvRightside.gone()
        mBinding.actionBar.rlToolbarNavigateup.onClick { activity?.onBackPressed() }
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {
        viewModel.emailValidation()
    }

}