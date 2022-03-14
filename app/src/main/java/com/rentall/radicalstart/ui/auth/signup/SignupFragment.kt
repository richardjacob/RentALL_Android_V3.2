package com.rentall.radicalstart.ui.auth.signup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAuthSignupBinding
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.UiEvent
import com.rentall.radicalstart.util.minimizeApp
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class SignupFragment : BaseFragment<FragmentAuthSignupBinding, SignupViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_signup
    override val viewModel: SignupViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(SignupViewModel::class.java)
    lateinit var mBinding: FragmentAuthSignupBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        mBinding.tvWelcome.text = getString(R.string.welcome_to_appname)// Utils.fromHtml( "Welcome to RentALL<sup><small>beta</small></sup>")
        mBinding.actionBar.tvRightside.text = resources.getString(R.string.logintext)
        mBinding.actionBar.ivNavigateup.setImageResource(R.drawable.ic_close_white_24dp)
        mBinding.actionBar.tvRightside.onClick { RxBus.publish(UiEvent.Navigate(AuthViewModel.Screen.LOGIN)) }
        mBinding.actionBar.rlToolbarNavigateup.onClick { baseActivity?.minimizeApp() }
        mBinding.btnFb.onClick { RxBus.publish(UiEvent.Navigate(AuthViewModel.Screen.FB)) }
        mBinding.btnGoogle.onClick { RxBus.publish(UiEvent.Navigate(AuthViewModel.Screen.GOOGLE)) }
        mBinding.btnCreateAccount.onClick { RxBus.publish(UiEvent.Navigate(AuthViewModel.Screen.NAME)) }
    }

    override fun onRetry() {

    }

}