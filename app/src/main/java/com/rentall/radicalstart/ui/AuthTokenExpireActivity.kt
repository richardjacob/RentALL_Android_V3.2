package com.rentall.radicalstart.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityAuthtokenexpireBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class AuthTokenExpireActivity: BaseActivity<ActivityAuthtokenexpireBinding, AuthTokenViewModel>() {

    companion object {
        @JvmStatic fun openActivity(activity: Activity) {
            val intent = Intent(activity, AuthTokenExpireActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityAuthtokenexpireBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_authtokenexpire
    override val viewModel: AuthTokenViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(AuthTokenViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        mBinding.btnLogIn.onClick {
            SplashActivity.openActivity(this)
            finish()
        }
    }

    override fun onRetry() {

    }

}