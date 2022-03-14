package com.rentall.radicalstart.ui.payment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentPaymentDialogBinding
import com.rentall.radicalstart.ui.base.BaseBottomSheet
import com.rentall.radicalstart.viewholderDivider
import com.rentall.radicalstart.viewholderHostBottomOptions
import javax.inject.Inject

class PaymentDialogOptionsFragment : BaseBottomSheet<FragmentPaymentDialogBinding,PaymentViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_payment_dialog
    override val viewModel: PaymentViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(PaymentViewModel::class.java)
    lateinit var mBinding: FragmentPaymentDialogBinding
    var type: String = ""

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
                PaymentDialogOptionsFragment().apply {
                    arguments = Bundle().apply {
                        putString("type", type)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding=viewDataBinding!!
        arguments?.let {
            type= it.getString("type")!!
        }
        setUp()
    }

    fun setUp(){
        mBinding.rvPaymentType.withModels {
                if(type=="paymentCurrency") {
                    val options = viewModel.currencies.value
                    options?.forEachIndexed { index, s ->
                            if (s.isPayment == true) {
                                viewholderHostBottomOptions {
                                    id("selected - $index")
                                    list(s.symbol())
                                    clickListener(View.OnClickListener {
                                        viewModel.selectedCurrency.set(s.symbol())
                                        dismiss()
                                    })
                                }
                                viewholderDivider {
                                    id("divider - $index")
                                }
                            }
                    }
                }
        }
    }


    override fun onRetry() {
    }
}