package com.rentall.radicalstart.ui.host.step_three.listingPrice

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentListPriceBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.OptionsSubFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class ListingPriceFragment : BaseFragment<HostFragmentListPriceBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentListPriceBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_price
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding=viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.listPriceToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.listPriceToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.listPriceToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()){
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.listPriceToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.listPriceToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.listPriceToolbar.ivNavigateup.onClick {
                baseActivity!!.onBackPressed()
        }

        subscribeToLiveData()
        obserbeData()
    }

    fun obserbeData(){
        viewModel.listDetailsStep3.observe(viewLifecycleOwner, Observer {
            if(isAdded) {
                if (mBinding.rvListPrice.adapter != null) {
                    if(viewModel.isSnackbarShown){
                        hideSnackbar()
                        viewModel.isSnackbarShown = false
                    }
                    mBinding.rvListPrice.requestModelBuild()
                }
            }
        })

    }

    fun subscribeToLiveData(){
        mBinding.rvListPrice.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.base_price))
                paddingBottom(true)
                paddingTop(true)
            }

            viewholderUserNormalText {
                id("subHeader")
                text(getString(R.string.price_text))
                paddingTop(false)
                paddingBottom(true)
            }
            viewholderListNumEt {
                id("priceEt")
                text(viewModel.basePrice)
                hint(getString(R.string.base_price_hint))
                inputType(true)
                paddingBottom(true)
                title(getString(R.string.base_price))
            }

            viewholderListNumEt {
                id("cleanEt")
                paddingBottom(true)
                title(getString(R.string.cleaning_price))
                inputType(true)
                text(viewModel.cleaningPrice)
                hint(getString(R.string.cleaning_price_hint))
            }

            viewholderUserNormalText {
                id("currency")
                text(getString(R.string.currency))
                paddingTop(false)
                paddingBottom(false)
            }

            viewholderListTv {
                id("noticeOption")
                hint(viewModel.listDetailsStep3.value!!.currency)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    OptionsSubFragment.newInstance("price").show(childFragmentManager, "price")
                })
            }

            viewholderDivider {
                id("optiondiv")
            }


        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flSubFragment.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onRetry() {

    }
}