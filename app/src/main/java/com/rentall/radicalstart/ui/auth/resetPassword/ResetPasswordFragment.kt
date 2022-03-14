package com.rentall.radicalstart.ui.auth.resetPassword

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAuthChangePasswordBinding
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.UiEvent
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

private const val TOKEN = "param1"
private const val EMAIL = "param2"

class ResetPasswordFragment : BaseFragment<FragmentAuthChangePasswordBinding, ResetPasswordViewModel>(), AuthNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_change_password
    override val viewModel: ResetPasswordViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(ResetPasswordViewModel::class.java)
    lateinit var mBinding: FragmentAuthChangePasswordBinding
    private var token: String? = null
    private var email: String? = null

    companion object {
        @JvmStatic
        fun newInstance(token: String, email: String) =
                ResetPasswordFragment().apply {
                    arguments = Bundle().apply {
                        putString(TOKEN, token)
                        putString(EMAIL, email)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(TOKEN)
            email = it.getString(EMAIL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        viewModel.token.value = token
        viewModel.email.value = email
    }

    private fun initView() {
        mBinding.actionBar.tvRightside.gone()
        mBinding.actionBar.rlToolbarNavigateup.onClick { activity?.onBackPressed() }
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {
        viewModel.validateData()
    }

}