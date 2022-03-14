package com.rentall.radicalstart.host.payout.addPayout

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentAddPayoutAccountDetailsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class PaymentTypeFragment : BaseFragment<FragmentAddPayoutAccountDetailsBinding, AddPayoutViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_add_payout_account_details
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(AddPayoutViewModel::class.java)
    lateinit var mBinding: FragmentAddPayoutAccountDetailsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.btnNext.gone()
    }

    private fun subscribeToLiveData() {
        viewModel.loadPayoutMethods().observe(viewLifecycleOwner, Observer {
            it?.let { result ->
               setup(result)
            }
        })
        hideKeyboard()
    }

    private fun setup(result: List<GetPaymentMethodsQuery.Result>) {
        mBinding.rlAddPayout.withModels {
            viewholderCenterText {
                id("12")
                text(resources.getString(R.string.choose_how_we_pay_you))
                paddingTop(true)
                large(true)
            }

            result.forEach {
                viewholderPayoutChooseHowWePay {
                    id(it.id())
                    info(it.details())
                    currency(resources.getString(R.string.currency)+" : ["+ (it.currency() ?: "USD") +"]")
                    fees(resources.getString(R.string.fees)+" : ${it.fees()}")
                    processingTime(resources.getString(R.string.processed_in)+" : "+ it.processedIn())
                    if(it.name()=="Paypal"){
                        paymentType(resources.getString(R.string.paypal))
                    }else{
                        paymentType(resources.getString(R.string.bank_account))
                    }
                    clickListener { _ ->
                        viewModel.currency.set(it.currency())
                       // showToast("setting currency ${it.currency()}")
                        if (it.paymentType() == 1) {
                            viewModel.navigator.moveToScreen(AddPayoutActivity.Screen.PAYPALDETAILS)
                        } else if(it.paymentType() == 2) {
                            if(viewModel.isDOB) {
                                viewModel.navigator.moveToScreen(AddPayoutActivity.Screen.PAYOUTDETAILS)
                            }else{
                                showSnackbar(resources.getString(R.string.dob),resources.getString(R.string.dob_msg))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRetry() {

    }
}