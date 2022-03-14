package com.rentall.radicalstart.ui.payment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentStripePaymentBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.vo.Outcome
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.exception.APIConnectionException
import com.stripe.android.model.PaymentMethod
import timber.log.Timber
import javax.inject.Inject

class StripePaymentFragment(val stripe: Stripe) : BaseFragment<FragmentStripePaymentBinding, PaymentViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentStripePaymentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_stripe_payment
    override val viewModel: PaymentViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(PaymentViewModel::class.java)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        subscribeToLiveData()
        mBinding.btnPay.onClick {
            creatingToken()
        }
    }


    private fun subscribeToLiveData() {
        viewModel.stripeResponse?.observe(viewLifecycleOwner, Observer {
            it?.let { outCome ->
                when (outCome) {
                    is Outcome.Error -> {
                        if (outCome.e is APIConnectionException) {
                            showOffline()
                        } else {
                            showToast(resources.getString(R.string.something_went_wrong))
                        }
                    }
                    is Outcome.Success -> {
                        viewModel.token.value = outCome.data.id
                        viewModel.validateToken()
                    }
                    is Outcome.Progress -> {
                        viewModel.isLoading.set(outCome.loading)
                    }
                }
            }
        })

        viewModel.stripeReqAdditionAction.observe(viewLifecycleOwner, Observer {
            it?.let { outCome ->
                if (outCome == "1") {
                    stripe.handleNextActionForPayment(requireActivity(), viewModel.paymentIntentSecret.value!!)
                }
            }
        })

        viewModel.paymentIntentLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { outCome ->
                creatingToken(null, outCome)
            }
        })

    }


    private fun creatingToken() {
        val randomNumber = Math.random()
        val card = mBinding.cardInputWidget.card
        if (card == null) {
            Toast.makeText(activity, resources.getString(R.string.enter_correct_card_details), Toast.LENGTH_LONG).show()
        } else {
            val params = card.toPaymentMethodsParams()
            viewModel.setIsLoading(true)

            stripe.createPaymentMethod(params, "Idempotency-Key: $randomNumber", null, object : ApiResultCallback<PaymentMethod> {
                override fun onError(e: Exception) {
                    viewModel.setIsLoading(false)
                    print("Payment failed")
                    viewModel.navigator.showToast("Payment failed ${e.message}")
                    Timber.e(e,"payment ")
                }

                override fun onSuccess(result: PaymentMethod) {
                    viewModel.setIsLoading(false)
                    print("Created PaymentMethod")
                    creatingToken(result.id, null)
                }
            })
        }
    }

    private fun creatingToken(paymentMethod: String?, paymentIntent: String?) {

        if (paymentMethod != null) {
            viewModel.createReservation(paymentMethod.toString())
        } else {
            viewModel.confirmReservation(paymentIntent.toString())
        }
    }

    override fun onRetry() {
        creatingToken()
    }
}