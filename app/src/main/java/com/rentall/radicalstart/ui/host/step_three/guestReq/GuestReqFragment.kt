package com.rentall.radicalstart.ui.host.step_three.guestReq

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentGuestReqBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class GuestReqFragment : BaseFragment<HostFragmentGuestReqBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentGuestReqBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_guest_req
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.guestReqToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.guestReqToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.guestReqToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.guestReqToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.guestReqToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.guestReqToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.rvGuestReq.gone()
        mBinding.tvNext.gone()

        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        viewModel.listSettingArray.observe(viewLifecycleOwner, Observer {
            it?.let {reqList ->
                mBinding.rvGuestReq.visible()
                mBinding.tvNext.visible()
                setUI(reqList.guestRequirements()!!)
            }

        })
    }

    fun setUI(it : GetListingSettingQuery.GuestRequirements){
        val roomTypeArray = it.listSettings()
        mBinding.rvGuestReq.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.guest_req))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.guest_req_sub))
                paddingTop(false)
                paddingBottom(true)
            }

            roomTypeArray?.forEachIndexed { index, s ->
                viewholderGuestReq {
                    id("req$index")
                    text(s.itemName())
                    isVerified(true)
                    direction(true)
                }

                if (index != it.listSettings()!!.lastIndex){
                    viewholderDivider {
                        id("Divider - $index")
                    }
                }
            }



        }
    }

    override fun onRetry() {

    }
}