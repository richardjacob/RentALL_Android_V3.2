package com.rentall.radicalstart.ui.reservation

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentBookingStep1Binding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.inbox.msg_detail.NewInboxMsgActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.vo.InboxMsgInitData
import com.rentall.radicalstart.vo.ListingInitData
import javax.inject.Inject

class ItineraryFragment : BaseFragment<FragmentBookingStep1Binding, ReservationViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: ReservationViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ReservationViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.rlListingBottom.gone()
        mBinding.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        viewModel.reservation.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { reservationDetails ->
                setup(reservationDetails)
            }
        })
    }

    private fun setup(it: GetReservationQuery.Results) {
        mBinding.rlBooking.withModels {
            viewholderCenterText {
                id("text1")
                text(resources.getString(R.string.you_are_going_to) +" ${it.listData()?.city()}!")
                large(true)
                isRed(false)
                paddingTop(true)
            }

            viewholderCenterText {
                id("text2")
                text(resources.getString(R.string.reservation_code)+ " ${it.confirmationCode()}")
                large(false)
                isRed(false)
                paddingTop(false)
                clickListener(View.OnClickListener {  })
            }

            viewholderCenterText {
                id("text3")
                text(resources.getString(R.string.view_receipt))
                large(false)
                isRed(true)
                paddingTop(false)
                paddingBottom(true)
                clickListener(View.OnClickListener {
                    (baseActivity as ReservationActivity).navigateToScreen(9)
//                    viewModel.uiEventLiveData.value = UiEventWrapper(UiEvent.Navigate1(9))
                })
            }

            viewholderDivider {
                id("divider1")
            }

            viewholderItinenaryListinfo {
                id("listInfo")
                type(resources.getString(R.string.shared_room))
                if(it.listData()!=null&&it.listData()!!.title()!=null){
                    title(it.listData()?.title()!!.trim().replace("\\s+", " "))
                }
                reviewsCount(it.listData()?.reviewsCount())
                img(it.listData()?.listPhotoName())
                imageClick { _ ->
                    try {
                        val currency = viewModel.getCurrencySymbol() + viewModel.getConvertedRate(it.currency()!!, it.basePrice()!!.toDouble()).toString()
                        val photo = ArrayList<String>()
                        photo.add(it.listData()!!.listPhotoName()!!)
                        ListingDetails.openListDetailsActivity(requireContext(), ListingInitData(
                                it.listData()!!.title()!!,
                                photo,
                                it.listData()!!.id()!!,
                                it.listData()!!.roomType()!!,
                                it.listData()!!.reviewsStarRating(),
                                it.listData()!!.reviewsCount(),
                                currency,
                                0,
                                selectedCurrency = viewModel.getUserCurrency(),
                                currencyBase = viewModel.getCurrencyBase(),
                                currencyRate = viewModel.getCurrencyRates(),
                                startDate = "0",
                                endDate = "0",
                                bookingType = it.listData()!!.bookingType()!!,
                                isWishList = it.listData()!!.wishListStatus()!!
                        ))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError()
                    }
                }
                reviewsStarRating(it.listData()?.reviewsStarRating())
                location(it.listData()?.city() + ", " +
                        it.listData()?.state() + ", " +
                        it.listData()?.country())
            }

            viewholderDivider {
                id("divider2")
            }

            viewholderBookingDateInfo {
                id("booking_date")
                try {
                    if(resources.getBoolean(R.bool.is_left_to_right_layout).not()){
                        ltrDirection(false)
                    }else{
                        ltrDirection(true)
                    }
                    checkIn(Utils.epochToDate(it.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!))
                    checkOut(Utils.epochToDate(it.checkOut()!!.toLong(), Utils.getCurrentLocale(requireContext())!!))
                    if (it.checkInStart() == "Flexible" && it.checkInEnd() == "Flexible") {
                        startTime(getString(R.string.flexible_check_in_time))
                    } else if (it.checkInStart() != "Flexible" && it.checkInEnd() == "Flexible") {
                        val sTime = when {
                            BindingAdapters.timeConverter(it.checkInStart()!!) == "0AM" -> {
                                "12AM"
                            }
                            BindingAdapters.timeConverter(it.checkInStart()!!) == "0PM" -> {
                                "12PM"
                            }
                            else -> {
                                BindingAdapters.timeConverter(it.checkInStart()!!)
                            }
                        }
                        startTime("${getString(R.string.from)} $sTime")
                    } else if (it.checkInStart() == "Flexible" && it.checkInEnd() != "Flexible") {
                        val eTime = when {
                            BindingAdapters.timeConverter(it.checkInEnd()!!) == "0AM" -> {
                                "12AM"
                            }
                            BindingAdapters.timeConverter(it.checkInEnd()!!) == "0PM" -> {
                                "12PM"
                            }
                            else -> {
                                BindingAdapters.timeConverter(it.checkInEnd()!!)
                            }
                        }
                        startTime("${getString(R.string.upto)} $eTime")
                    } else if (it.checkInStart() != "Flexible" && it.checkInEnd() != "Flexible") {
                        val sTime = when {
                            BindingAdapters.timeConverter(it.checkInStart()!!) == "0AM" -> {
                                "12AM"
                            }
                            BindingAdapters.timeConverter(it.checkInStart()!!) == "0PM" -> {
                                "12PM"
                            }
                            else -> {
                                BindingAdapters.timeConverter(it.checkInStart()!!)
                            }
                        }

                        val eTime = when {
                            BindingAdapters.timeConverter(it.checkInEnd()!!) == "0AM" -> {
                                "12AM"
                            }
                            BindingAdapters.timeConverter(it.checkInEnd()!!) == "0PM" -> {
                                "12PM"
                            }
                            else -> {
                                BindingAdapters.timeConverter(it.checkInEnd()!!)
                            }
                        }
                        startTime("$sTime - $eTime")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                timeVisibility(true)
            }

            viewholderDivider {
                id("divider3")
            }

            viewholderItineraryTextBold {
                id("Address")
                text(resources.getString(R.string.address))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderListingDetailsSublist {
                id("addressText")
                list(it.listData()?.street() + ", " +
                        it.listData()?.city() + ", " +
                        it.listData()?.state() + ", " +
                        it.listData()?.country() + ", " +
                        it.listData()?.zipcode())
            }

            viewholderItineraryTextNormal {
                id("viewListing")
                text(resources.getString(R.string.view_Listing))
                isRed(true)
                large(false)
                paddingBottom(true)
                paddingTop(false)
                clickListener(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view){
                        try {
                            val currency = viewModel.getCurrencySymbol() + viewModel.getConvertedRate(it.currency()!!, it.basePrice()!!.toDouble()).toString()
                            val photo = ArrayList<String>()
                            photo.add(it.listData()!!.listPhotoName()!!)
                            ListingDetails.openListDetailsActivity(requireContext(), ListingInitData(
                                    it.listData()!!.title()!!,
                                    photo,
                                    it.listData()!!.id()!!,
                                    it.listData()!!.roomType()!!,
                                    it.listData()!!.reviewsStarRating(),
                                    it.listData()!!.reviewsCount(),
                                    currency,
                                    0,
                                    selectedCurrency = viewModel.getUserCurrency(),
                                    currencyBase = viewModel.getCurrencyBase(),
                                    currencyRate = viewModel.getCurrencyRates(),
                                    startDate = "0",
                                    endDate = "0",
                                    bookingType = it.listData()!!.bookingType()!!,
                                    isWishList = it.listData()!!.wishListStatus()!!
                            ))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showError()
                        }
                    }
                })
            }

            viewholderDivider {
                id("divider4")
            }

            viewholderItineraryTextBold {
                id("Host")
                text(resources.getString(R.string.host))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryAvatar {
                id("avatar")
                avatarImg(it.hostData()?.picture())
                avatarClick(View.OnClickListener {view ->
                    Utils.clickWithDebounce(view){
                        UserProfileActivity.openProfileActivity(requireContext(), it.hostData()?.profileId()!!)
                    }
                })
            }

            viewholderListingDetailsSublist {
                id("name")
                list(it.hostData()?.displayName())
            }

            viewholderItineraryTextNormal {
                id("send msg")
                text(resources.getString(R.string.message_host))
                isRed(true)
                large(false)
                paddingBottom(true)
                paddingTop(false)
                clickListener(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view){
                        try {
                            NewInboxMsgActivity.openInboxMsgDetailsActivity(baseActivity!!, InboxMsgInitData(
                                    threadId = it.messageData()?.id()!!,
                                    guestId = it.guestData()?.userId().toString(),
                                    guestName = it.guestData()?.displayName()!!,
                                    guestPicture = it.guestData()?.picture(),
                                    hostId = it.hostData()?.userId().toString(),
                                    hostName = it.hostData()?.displayName()!!,
                                    hostPicture = it.hostData()?.picture(),
                                    senderID = it.guestData()?.profileId()!!,
                                    receiverID = it.hostData()?.profileId()!!,
                                    listID = it.listId())
                            )
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                            showError()
                        }
                    }
                })
            }

            viewholderDivider {
                id("divider5")
            }

            viewholderItineraryTextBold {
                id("Billing")
                text(resources.getString(R.string.billing))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextLeftRight {
                id("Billing_text")
                try {
                    rightSide(it.nights().toString() + " "+ resources.getQuantityString(R.plurals.night_count, it.nights()!!))
                    leftSide(viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee()!!).toString())
//                    if (viewModel.reservationComplete.value!!.convertedGuestServicefee() != null) {
//                        leftSide(viewModel.getCurrencySymbol() + viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee().toString())
//                    } else {
//                        leftSide(viewModel.currencyConverter(it.currency()!!, Utils.formatDecimal(it.total()!!).toDouble()))
//                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                paddingBottom(true)
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