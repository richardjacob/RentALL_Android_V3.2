package com.rentall.radicalstart.host.payout.addPayout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityAddPayoutBinding
import com.rentall.radicalstart.host.payout.editpayout.EditPayoutActivity
import com.rentall.radicalstart.ui.WebViewActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.util.addFragmentToActivity
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.replaceFragmentInActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class AddPayoutActivity: BaseActivity<ActivityAddPayoutBinding, AddPayoutViewModel>(), AddPayoutNavigator {
var connectUrl=""
    enum class Screen {
        INTRO,
        INFO,
        PAYOUTTYPE,
        PAYOUTDETAILS,
        PAYPALDETAILS,
        WEBVIEW,
        FINISH,
    }

    companion object {
        @JvmStatic fun openActivity(activity: Activity, countryName: String, countryCode: String) {
            val intent = Intent(activity, AddPayoutActivity::class.java)
            intent.putExtra("country", countryName)
            intent.putExtra("countryCode", countryCode)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }

        @JvmStatic fun openActivityFromWebView(activity: Activity, status: String,accountId: String) {
            val intent = Intent(activity, AddPayoutActivity::class.java)
            intent.putExtra("from", "webview")
            intent.putExtra("status", status)
            intent.putExtra("accountId",accountId)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }
    }

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityAddPayoutBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_add_payout
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(AddPayoutViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        if(intent?.hasExtra("accountId")!!){
            viewModel.accountID=intent.getStringExtra("accountId").orEmpty()
        }
        initView()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun initView() {
        if(intent?.hasExtra("from")!!){
            val status = intent?.getStringExtra("status").orEmpty()
            if(status.equals("success")){
                viewModel.setPayout()
            }
        }else {
            viewModel.country.set(intent?.getStringExtra("country"))
            viewModel.countryCode.set(intent?.getStringExtra("countryCode"))
        }
        mBinding.inlToolbar.textView.text = getString(R.string.payouts)
        mBinding.inlToolbar.textView.textSize = 20.0F
        mBinding.inlToolbar.rlToolbarDone.background = null
        with(mBinding.inlToolbar.ivNavigateup) {
            setImageResource(R.drawable.ic_arrow_back_black_24dp)
            onClick {
                onBackPressed()
            }
        }
        addFragment(PaymentIntroFragment(), "PaymentIntro")
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        addFragmentToActivity(mBinding.flAddPayout.id, fragment, tag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flAddPayout.id, fragment, tag)
    }

    override fun moveToScreen(screen: Screen) {
        when(screen.name){
            Screen.INTRO.name -> addFragment(PaymentIntroFragment(), "PaymentIntro")
            Screen.INFO.name -> replaceFragment(PayoutAccountInfoFragment(), "PaymentInfo")
            Screen.PAYOUTTYPE.name -> replaceFragment(PaymentTypeFragment(), "PaymentType")
            Screen.PAYOUTDETAILS.name -> replaceFragment(PayoutAccountDetailFragment(), "PaymentDetails")
            Screen.PAYPALDETAILS.name -> replaceFragment(PayoutPaypalDetailsFragment(), "PaypalDetails")
            Screen.WEBVIEW.name -> {
                WebViewActivity.openWebViewActivity(this, viewModel.connectingURL, "AddStripe Onboarding-${viewModel.accountID}")
                finish()
            }
            Screen.FINISH.name ->{ finish() }
            }
        }


    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flAddPayout.id)
        if (fragment is PaymentTypeFragment) {
            viewModel.getPayoutMethods()
        } else if (fragment is PayoutPaypalDetailsFragment) {
            fragment.checkDetails()
        }
    }

}