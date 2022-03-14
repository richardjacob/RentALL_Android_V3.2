package com.rentall.radicalstart.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityPaymentBinding
import com.rentall.radicalstart.ui.WebViewActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.reservation.ReservationActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.viewholderSelectPaymentCurrency
import com.rentall.radicalstart.viewholderSelectPaymentType
import com.rentall.radicalstart.vo.PaymentType
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.StripeIntent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import timber.log.Timber
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.Utils.Companion.getQueryMap
import javax.inject.Inject


class PaymentTypeActivity : BaseActivity<ActivityPaymentBinding, PaymentViewModel>(), PaymentNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityPaymentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_payment
    override val viewModel: PaymentViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(PaymentViewModel::class.java)
    private lateinit var stripe: Stripe
    lateinit var amount: String

    var paymentTypeArray = ArrayList<PaymentType>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        paymentTypeArray.add(PaymentType("PayPal", 1))
        paymentTypeArray.add(PaymentType("Stripe", 2))
        viewModel.initData(intent)
        viewModel.getCurrency()
        initView()
    }

    private fun initView() {
        mBinding.ivNavigateup.onClick {
            if (viewModel.isLoading.get().not()) {
                this.onBackPressed()
            }
        }

        mBinding.btnPay.onClick {
            Utils.clickWithDebounce(mBinding.btnPay){
                if (viewModel.selectedPaymentType == 0) {
                    viewModel.navigator.showSnackbar(getString(R.string.select_payment_type), getString(R.string.please_select_payment_type_to_continue))
                } else {
                    if (viewModel.selectedPaymentType == 1) {
                        if (viewModel.selectedCurrency.get() != getString(R.string.currency)) {
                            viewModel.createReservation("")
                        } else {
                            viewModel.navigator.showSnackbar(getString(R.string.currency), getString(R.string.please_select_currency))
                        }
                    }else if (viewModel.selectedPaymentType == 2) {
                        stripe = Stripe(applicationContext, Constants.stripePublishableKey)
                        addFragment(StripePaymentFragment(stripe), "STRIPE_FRAGMENT")
                    }
                }
            }
        }
        setUp()
    }


    private fun setUp() {
        mBinding.rvPaymentType.withModels {
            paymentTypeArray.forEachIndexed { index, paymentType ->
                viewholderSelectPaymentType {
                    id("paymentType--$index")
                    text(paymentType.typeText)
                    onClick(View.OnClickListener {
                        viewModel.selectedPaymentType = paymentType.typeInt
                        requestModelBuild()
                    })
                    isChecked(viewModel.selectedPaymentType == paymentType.typeInt)
                }
            }

            if (viewModel.selectedPaymentType == 1) {
                viewholderSelectPaymentCurrency {
                    id("selectCurrency")
                    viewModel(viewModel)
                    selectedCurrency(viewModel.selectedCurrency)
                    onCurrencyClick(View.OnClickListener {
                        PaymentDialogOptionsFragment.newInstance("paymentCurrency").show(supportFragmentManager, "paymentCurrency")
                    })
                }
            }
        }
    }


    private fun addFragment(fragment: Fragment, tag: String?) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.fragFrame.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun moveToReservation(id: Int) {
        val intent = Intent(this, ReservationActivity::class.java)
        intent.putExtra("type", 1)
        intent.putExtra("reservationId", id)
        intent.putExtra("userType", "Guest")
        setResult(32, intent)
        startActivity(intent)
        finish()
    }

    override fun finishScreen() {
        val intent = Intent()
        setResult(32, intent)
        finish()
    }

    override fun moveToPayPalWebView(redirectUrl: String) {
        if(redirectUrl.isNotEmpty()){
            WebViewActivity.openWebViewActivityForResult(104,this, redirectUrl, "PayPalPayment-104")
        }else{
            viewModel.navigator.showToast(getString(R.string.return_url_not_fount))
        }
    }

    override fun onBackPressed() {
        if (viewModel.isLoading.get().not()) {
            super.onBackPressed()
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.setIsLoading(false)
        if(resultCode!=0){
            if(resultCode==104){
                if(data?.getStringArrayExtra("url").toString().contains("/cancel".toRegex())){
                    viewModel.navigator.showToast(getString(R.string.payment_failed))
                }else{
                    data?.getStringExtra("url")?.let {
                        val map = getQueryMap(it)
                        val paymentId= map?.get("paymentId")
                        val payerId = map?.get("PayerID")
                        viewModel.confirmPayPalPayment(paymentId?:"",payerId?:"")
                    }
                }
            }else if(resultCode==107){
                viewModel.navigator.showToast(getString(R.string.payment_cancelled))
            }else{
                stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
                    override fun onSuccess(result: PaymentIntentResult) {
                        val paymentIntent = result.intent
                        when (paymentIntent.status) {
                            StripeIntent.Status.Succeeded -> viewModel.confirmReservation(paymentIntent.toString())
                            StripeIntent.Status.RequiresPaymentMethod ->
                                viewModel.navigator.showToast("Payment failed")
                            StripeIntent.Status.RequiresConfirmation -> {
                                print("Re-confirming PaymentIntent after handling a required action")
                                viewModel.paymentIntentLiveData.value = paymentIntent.id
                            }
                            else -> viewModel.navigator.showToast("Payment failed")
                        }
                    }

                    override fun onError(e: Exception) {
                        print("hello your payment failed")
                        viewModel.navigator.showToast(e.message!!)
                    }
                })
            }
        }
    }


    override fun onRetry() {
    }
}