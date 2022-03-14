package com.rentall.radicalstart.ui.listing.contact_host

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentListingPricebreakdownBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.ui.listing.guest.GuestFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.vo.ListingInitData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ContactHostFragment: BaseFragment<FragmentListingPricebreakdownBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_pricebreakdown
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentListingPricebreakdownBinding

    var startDate: String = ""
    var endDate: String = ""
    var guestCount: String = ""
    lateinit var dateGuest: ListingDetails.PriceBreakDown

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.viewModel = viewModel
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.btnBook.text = resources.getString(R.string.send_message)
        mBinding.breakDownToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.btnBook.onClick {
            if (viewModel.startDate.value.isNullOrEmpty() || viewModel.endDate.value.isNullOrEmpty()) {
                showToast(resources.getString(R.string.please_select_date))
            } else if (viewModel.initialValue.value?.guestCount == 0) {
                showToast(resources.getString(R.string.please_select_guest))
            } else if (viewModel.msg.get()!!.trim().isNullOrEmpty()) {
                showToast(resources.getString(R.string.please_enter_the_message))
            } else {
                hideKeyboard()
                viewModel.retryCalled = "contact"
                viewModel.contactHost()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.initialValue.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { initValue ->
                setup(initValue)
            }
        })

        viewModel.dateGuestCount.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { dateDiscount ->
                dateGuest = dateDiscount
                viewModel.getBillingCalculation()
                mBinding.rlListingPricebreakdown.requestModelBuild()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setup(it: ListingInitData) {
        mBinding.rlListingPricebreakdown.withModels {
            viewholderPricebreakListinfo {
                id("info")
                type(it.roomType)
                img(it.photo[0])
                price(it.price)
                imageClick(View.OnClickListener {
                    Utils.clickWithDebounce(it){ListingDetails.openListDetailsActivity(requireContext(),viewModel.initialValue.value!!)}
                })
                reviewsStarRating(it.ratingStarCount)
                reviewsCount(it.reviewCount)
            }
            viewholderDividerPadding {
                id("divider1")
            }
            viewholderListingPricebreakCheckinout {
                id("checkinout")
                checkIn(getCalenderMonth(viewModel.startDate.value))
                checkOut(getCalenderMonth(viewModel.endDate.value))
                guest(it.guestCount)
                checkInOnClick(View.OnClickListener {Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                checkOutOnClick(View.OnClickListener {Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                guestOnClick(View.OnClickListener {
                    Utils.clickWithDebounce(it){
                        hideKeyboard()
                        (baseActivity as ListingDetails).openFragment(GuestFragment())
                    }
                })
            }
            viewholderDividerPadding {
                id("divider2")
            }
            viewholderBookingMsgHost {
                id("dsfg")
                msg(viewModel.msg)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(false)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.setOnTouchListener(null)
                }
            }
        }
    }

    private fun getCalenderMonth(dateString: String?): String {
        try {
            return if (dateString != null && dateString != "0") {
                val startDateArray: List<String> = dateString.split("-")
                val year: Int = startDateArray[0].toInt()
                val month = startDateArray[1].toInt()
                val date = startDateArray[2].toInt()
                val monthPattern = SimpleDateFormat("MMM", Locale.ENGLISH)
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, date)
                monthPattern.format(cal.time) + " " + date
            } else {
                resources.getString(R.string.add_date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return resources.getString(R.string.add_date)
        }
    }

    override fun onRetry() {

    }

}