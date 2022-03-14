package com.rentall.radicalstart.host.payout.editpayout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.ActivityPayoutBinding
import com.rentall.radicalstart.ui.WebViewActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.vo.Payout
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class EditPayoutActivity : BaseActivity<ActivityPayoutBinding, PayoutViewModel>(), EditPayoutNavigator {

    enum class Screen {
        INTRO,
        INFO,
        PAYOUTTYPE,
        PAYOUTDETAILS,
        PAYPALDETAILS,
        WEBVIEW,
        FINISH
    }

    companion object {
        @JvmStatic
        fun openActivity(activity: Activity) {
            val intent = Intent(activity, EditPayoutActivity::class.java)
            activity.startActivity(intent)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityPayoutBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_payout
    override val viewModel: PayoutViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(PayoutViewModel::class.java)
    var payoutList = ArrayList<Payout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
        setUp()
    }

    private fun initView() {
        mBinding.rlToolbarNavigateup.onClick {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.payoutsList.value != null) {
            viewModel.getPayouts()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadPayouts().observe(this, Observer {
            it?.let { result ->
                payoutList = result
                mBinding.rlEditPayout.requestModelBuild()
            }
        })
    }

    private fun setUp() {
        mBinding.rlEditPayout.withModels {

            viewholderListingDetailsSectionHeader {
                id("sectionheader")
                header(resources.getString(R.string.edit_your_payout_methods))
            }
            if (viewModel.isLoading.get()) {
                viewholderLoader {
                    id("Loader")
                    isLoading(true)
                }
            } else {
                if (payoutList.isNotEmpty()) {
                    viewholderListingDetailsListShowmore {
                        id("readmore")
                        text(resources.getString(R.string.add_payout_method))
                        clickListener(View.OnClickListener {
                            openFragment(CountryFragment(), "Country")
                        })
                    }
                    payoutList.forEachIndexed { index, result ->
                        viewholderPayoutType {

                            id(result.id)
                            toolTipIcon(result.isVerified)

                            onBind { _, view, _ ->
                                val viewroot = view.dataBinding.root.findViewById<RelativeLayout>(R.id.linearPayout)
                                val viewpressed = view.dataBinding.root.findViewById<ImageView>(R.id.tooltipImage)
                                viewpressed.setOnClickListener {
                                    val builder = AlertDialog.Builder(this@EditPayoutActivity)
                                    builder.setMessage(R.string.stripe_tooltip_text)
                                    builder.setPositiveButton(getString(R.string.ok_text)) { _, _ -> }
                                    builder.show()
                                }
                            }

                            if (result.name == "Paypal") {
                                paymentType(resources.getString(R.string.paypal))
                            } else {
                                paymentType(getString(R.string.bank_account))
                            }

                            if (result.method == 1) {
                                details(resources.getString(R.string.details) + ": " + result.payEmail)
                            } else {
                                details(resources.getString(R.string.details) + ": " + "****" + result.pinDigit)
                            }
                            isVerified(result.isVerified)
                            isDefault(result.isDefault)
                            if (!result.isVerified) {
                                removeVisible(false)
                                buttonText(getString(R.string.verify))
                            } else {
                                if (result.isDefault) {
                                    removeVisible(true)
                                    buttonText(getString(R.string.default_caps))
                                } else {
                                    removeVisible(false)
                                    buttonText(getString(R.string.set_default_caps))
                                }
                            }
                            if (result.isVerified) {
                                if (result.isDefault) {
                                    isDefaultInverse(false)
                                } else {
                                    isDefaultInverse(true)
                                }
                            } else {
                                isDefaultInverse(true)
                            }
                            currency(resources.getString(R.string.currency) + ": [" + result.currency + "]")
                            if (result.isVerified) {
                                status(resources.getString(R.string.status) + ": " + resources.getString(R.string.ready))
                            } else {
                                status(resources.getString(R.string.status) + ": " + resources.getString(R.string.not_ready))
                            }

                            removeClick { _ ->
                                viewModel.setDefaultRemovePayputs("remove", result.id)
                            }
                            setDefault { _ ->
                                if (payoutList.getOrNull(index)?.isVerified == true)
                                    viewModel.setDefaultRemovePayputs("set", result.id)
                                else
                                    viewModel.verifyPayout(result.payEmail!!)
                            }
                        }
                    }
                } else {
                    viewholderListingDetailsListShowmore {
                        id("readmore")
                        text(resources.getString(R.string.add_payout_method))
                        clickListener(View.OnClickListener {
                            openFragment(CountryFragment(), "Country")
                        })
                    }
                }
            }
        }
    }

    fun openFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flEditPayout.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun disableCountrySearch(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flEditPayout.id)
        if (fragment is CountryFragment) {
            fragment.disableCountrySearch(flag)
        }
    }

    override fun moveToScreen(screen: Screen) {
        when (screen.name) {
            Screen.WEBVIEW.name -> {
                WebViewActivity.openWebViewActivity(this, viewModel.connectingURL, "EditStripe Onboarding-${viewModel.accountID}")
            }
            Screen.FINISH.name -> finish()
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onBackPressed() {
        viewModel.listSearch.value = null
        super.onBackPressed()
    }

    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flEditPayout.id)
        if (fragment is CountryFragment) {
            viewModel.getCountryCode()
        } else {
            viewModel.getPayouts()
        }
    }

}