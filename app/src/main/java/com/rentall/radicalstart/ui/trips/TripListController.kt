package com.rentall.radicalstart.ui.trips

import android.content.Intent
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.GetAllReservationQuery
import com.rentall.radicalstart.ViewholderTripsListBindingModel_
import com.rentall.radicalstart.ui.cancellation.CancellationActivity
import com.rentall.radicalstart.ui.inbox.msg_detail.InboxMsgActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.reservation.ReservationActivity
import com.rentall.radicalstart.util.CurrencyUtil
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.viewholderLoader
import com.rentall.radicalstart.vo.InboxMsgInitData
import com.rentall.radicalstart.vo.ListingInitData
import timber.log.Timber

class TripsListController (
        val cancelClickListener: (id: Int) -> Unit,
        val messageClick: (item: InboxMsgInitData) -> Unit,
        val receiptClick: (item: Int) -> Unit,
        val itineraryClick: (item: Int) -> Unit,
        val listClick: (item: Int) -> Unit
        ): PagedListEpoxyController<GetAllReservationQuery.Result>() {

    var isLoading = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    override fun buildItemModel(currentPosition: Int, item: GetAllReservationQuery.Result?): EpoxyModel<*> {

        try {
            var cancelVisibility = false
            var itineraryVisibility = false
            var messageVisibility = false
            var supportVisibility = false
            var receiptVisibility = true

            if (item?.listData() != null && (item.reservationState()!! == "pending" || item.reservationState()!! == "approved")) {
                cancelVisibility = true
            }
            if (item?.reservationState()!! == "pending" || item.reservationState()!! == "approved") {
                itineraryVisibility = true
            }

            if (item.reservationState()!! == "declined" || item.reservationState()!! == "cancelled") {
                itineraryVisibility = false
                cancelVisibility = false
            }

            var title: String? = null
            var namePrice: String? = null
            var street: String? = null
            var address: String? = null

            /* if (item.listData() != null) {
                      title = item.listData()?.title()
                      namePrice = item.hostData()?.displayName() + " - " + viewModel.currencyConverter(item.currency()!!, item.total()!!)
                      street = item.listData()?.street()
                      address = item.listData()?.city() + " " +
                              item.listData()?.state() + " " +
                              item.listData()?.country() + " " +
                              item.listData()?.zipcode()
                      messageVisibility = true
                  } else {
                      supportVisibility = true
                      receiptVisibility = false
                      itineraryVisibility = false
                  }*/

            return ViewholderTripsListBindingModel_()
                    .id("viewholder- $currentPosition")
                    .status(item.reservationState()?.capitalize())
                    .title(title)
                    .date(Utils.epochToDate(item.checkIn()!!.toLong()) + " - " + Utils.epochToDate(item.checkOut()!!.toLong()))
                    .namePrice(namePrice)
                    .street(street)
                    .address(address)
                    .cancelVisibility(cancelVisibility)
                    .messageVisibility(messageVisibility)
                    .itineraryVisibility(itineraryVisibility)
                    .supportVisibility(supportVisibility)
                    .receiptVisibility(receiptVisibility)
                    .cancelClick(View.OnClickListener {
                        /*try {
                            CancellationActivity.openCancellationActivity(baseActivity!!, item.id()!!)
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                        }*/
                    })
                    .messageClick(View.OnClickListener {
                       /* try {
                            InboxMsgActivity.openInboxMsgDetailsActivity(baseActivity!!, InboxMsgInitData(
                                    threadId = item.messageData()?.id()!!,
                                    guestId = item.guestData()?.profileId().toString(),
                                    guestName = item.guestData()?.displayName()!!,
                                    guestPicture = item.guestData()?.picture()!!,
                                    hostId = item.hostData()?.profileId().toString(),
                                    hostName = item.hostData()?.displayName()!!,
                                    hostPicture = item.hostData()?.picture()!!)
                            )
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                        }*/
                    })
                    .receiptClick(View.OnClickListener {
                        /*val intent = Intent(context, ReservationActivity::class.java)
                        intent.putExtra("type", 2)
                        intent.putExtra("reservationId", item.id())
                        startActivity(intent)*/
                    })
                    .itineraryClick(View.OnClickListener {
                       /* val intent = Intent(context, ReservationActivity::class.java)
                        intent.putExtra("type", 1)
                        intent.putExtra("reservationId", item.id())
                        startActivity(intent)*/
                    })
                    .onClick(View.OnClickListener {
                        /*try {
                            val currency = BindingAdapters.getCurrencySymbol(viewModel.currencyUser.value!!) + CurrencyUtil.getRate(
                                    base = viewModel.getCurrencyBase()!!,
                                    to = viewModel.currencyUser.value!!,
                                    from = item.listData()!!.listingData()?.currency()!!,
                                    rateStr = viewModel.currencyRates.value!!,
                                    amount = item.listData()!!.listingData()?.basePrice()!!.toDouble()
                            ).toString()
                            val photo = ArrayList<String>()
                            photo.add(item.listData()!!.listPhotos()!![0].name()!!)
                            ListingDetails.openListDetailsActivity(context!!, ListingInitData(
                                    item.listData()!!.title()!!,
                                    photo,
                                    item.listData()!!.id()!!,
                                    item.listData()!!.roomType()!!,
                                    item.listData()!!.reviewsStarRating(),
                                    item.listData()!!.reviewsCount(),
                                    currency,
                                    0,
                                    selectedCurrency = viewModel.currencyUser.value!!,
                                    currencyBase = viewModel.currencyBase.value!!,
                                    currencyRate = viewModel.currencyRates.value!!,
                                    startDate = "0",
                                    endDate = "0",
                                    bookingType = item.listData()!!.bookingType()!!
                            ))
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                        }*/
                    })
        } catch (e: Exception) {
            Timber.e(e,"CRASH")
        }
        return ViewholderTripsListBindingModel_()
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        try {
            super.addModels(models)
            if (isLoading) {
                viewholderLoader {
                    id("loading")
                    isLoading(true)
                }
            }
        } catch (e: Exception) {
            Timber.e(e,"CRASH")
        }
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}
