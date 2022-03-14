package com.rentall.radicalstart.ui.host.step_three.guestBooking

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentGuestBookingBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class GuestBookingFragment : BaseFragment<HostFragmentGuestBookingBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentGuestBookingBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_guest_booking
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.guestBookToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.guestBookToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.guestBookToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.guestBookToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.guestBookToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.guestBookToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }


        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        viewModel.listSettingArray.observe(viewLifecycleOwner, Observer {
            it?.let {bookingList ->
                setUI(bookingList.reviewGuestBook()!!)
            }

        })
    }

    fun setUI(it : GetListingSettingQuery.ReviewGuestBook){
        val reviewArray = it.listSettings()
        mBinding.rvGuestBook.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.how_book))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.how_book_sub))
                paddingTop(false)
                paddingBottom(true)
            }

            reviewArray?.forEachIndexed { index, s ->
                viewholderGuestReq {
                    id("req"+index)
                    text(s.itemName())
                    isVerified(true)
                    direction(true)
                }
                viewholderDivider {
                    id("divider $index")
                }
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.meet_req))
                paddingTop(true)
                paddingBottom(true)
            }

        }
    }

    override fun onRetry() {

    }
}