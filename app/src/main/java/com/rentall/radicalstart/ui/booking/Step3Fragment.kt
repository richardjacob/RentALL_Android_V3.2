package com.rentall.radicalstart.ui.booking

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentBookingStep1Binding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.invisible
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderBookingConfirmEmailBox
import com.rentall.radicalstart.viewholderBookingSteper
import javax.inject.Inject

class Step3Fragment : BaseFragment<FragmentBookingStep1Binding, BookingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: BookingViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(BookingViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        setUp()
    }

    private fun initView() {
        mBinding.rlListingPricedetails.invisible()
        mBinding.tvListingCheckAvailability.text = resources.getString(R.string.next).toLowerCase().capitalize()
        mBinding.tvListingCheckAvailability.onClick {
            (baseActivity as BookingActivity).navigateToScreen(4)
        }
        mBinding.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
    }

    private fun setUp() {
        mBinding.rlBooking.withModels {
            viewholderBookingSteper {
                id("sd123")
                step(resources.getString(R.string.step_3_of_5))
                title(resources.getString(R.string.check_your_email))
                info(resources.getString(R.string.tap_the_link_in_the_email_we_sent_you_Confirming_your_email_address_helps_us_send_you_trip_information))
                infoVisibility(true)
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderBookingConfirmEmailBox {
                id("dsgs")
            }
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    override fun onRetry() {

    }

}