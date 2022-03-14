package com.rentall.radicalstart.ui.host.step_three.discount

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentDiscountPriceBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListNumEt
import com.rentall.radicalstart.viewholderListingDetailsHeader
import com.rentall.radicalstart.viewholderUserName
import javax.inject.Inject

class DiscountPriceFragment : BaseFragment< HostFragmentDiscountPriceBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentDiscountPriceBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_discount_price
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.discountPriceToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.discountPriceToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.discountPriceToolbar.tvRightside.onClick {
                if(viewModel.checkDiscount()  && viewModel.checkTripLength() && viewModel.checkPrice()){
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.discountPriceToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.discountPriceToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.discountPriceToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()

    }

    fun subscribeToLiveData(){
        mBinding.rvDiscountPrice.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.discounts))
                paddingTop(true)
                paddingBottom(false)
            }

            viewholderListingDetailsHeader {
                id("discountText")
                header(getString(R.string.discount_long))
                isBlack(true)
                large(true)
            }

            viewholderListNumEt {
                id("weeklyDis")
                text(viewModel.weekDiscount)
                title(getString(R.string.weekly_discount))
                inputType(false)
                paddingBottom(true)
                hint(getString(R.string.discount_hint))
            }

//            viewholderUserNormalText {
//                id("monthly")
//                text(getString(R.string.monthly_discount))
//                paddingBottom(false)
//                paddingTop(true)
//            }

            viewholderListNumEt {
                id("monthlyDis")
                hint(getString(R.string.discount_hint))
                title(getString(R.string.monthly_discount))
                inputType(false)
                paddingBottom(true)
                text(viewModel.monthDiscount)
            }
        }
    }

    override fun onRetry() {
    }
}