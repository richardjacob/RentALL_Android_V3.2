package com.rentall.radicalstart.host.payout.addPayout

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentAddPayoutAccountDetailsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderCenterText
import com.rentall.radicalstart.viewholderPayoutAccountInfo
import com.stripe.android.Stripe
import javax.inject.Inject

class PayoutAccountInfoFragment : BaseFragment<FragmentAddPayoutAccountDetailsBinding, AddPayoutViewModel>() {

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
        viewModel.stripe =  Stripe(requireContext(), Constants.stripePublishableKey)
        initView()

    }

    private fun initView() {
        mBinding.btnNext.text =  resources.getString(R.string.next)
        mBinding.btnNext.onClick {
            if (viewModel.checkAccountInfo()) {
                viewModel.navigator.moveToScreen(AddPayoutActivity.Screen.PAYOUTTYPE)
            }
        }
        mBinding.rlAddPayout.withModels {
            viewholderCenterText {
                id("12")
                text( resources.getString(R.string.address_of_payout))
                paddingTop(true)
            }
            viewholderPayoutAccountInfo {
                id(14)
                country(viewModel.country.get())
                city(viewModel.city)
                address1(viewModel.address1)
                address2(viewModel.address2)
                state(viewModel.state)
                zip(viewModel.zip)
            }
        }
    }



    override fun onRetry() {

    }
}