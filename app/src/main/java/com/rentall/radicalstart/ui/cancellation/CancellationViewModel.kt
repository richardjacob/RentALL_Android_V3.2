package com.rentall.radicalstart.ui.cancellation

import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.CancelReservationMutation
import com.rentall.radicalstart.CancellationDataQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject


class CancellationViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<CancellationNavigator>(dataManager,resourceProvider) {

    private lateinit var cancellationDetails: MutableLiveData<CancellationDataQuery.Results>
    var retryCalled = ""

    private fun getThreadId(): Int {
        return try {
            cancellationDetails.value!!.threadId()!!
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
            0
        }
    }

    fun loadCancellationDetails(resverationId: Int, userType: String) : MutableLiveData<CancellationDataQuery.Results> {
        if (!::cancellationDetails.isInitialized) {
            cancellationDetails = MutableLiveData()
            getCancellationDetails(resverationId, userType)
        }
        return cancellationDetails
    }

    public fun getCancellationDetails(resverationId: Int, userType: String) {
        val buildQuery = CancellationDataQuery
                .builder()
                .userType(userType)
                .currency(getUserCurrency())
                .reservationId(resverationId)
                .build()

        compositeDisposable.add(dataManager.getCancellationDetails(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()?.cancelReservationData()
                        if (data!!.status() == 200) {
                            cancellationDetails.value = data.results()
                        } else if(data.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            data.errorMessage()?.let {
                                navigator.showToast(it)
                            } ?: navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    fun cancelReservation(text: String, reservationId: Int) {
        try {
            val buildMutation = CancelReservationMutation
                    .builder()
                    .reservationId(reservationId)
                    .threadId(getThreadId())
                    .message(text)
                    .cancellationPolicy(getCancellationPolicy())
                    .checkIn(getCheckIn())
                    .checkOut(getCheckOut())
                    .refundToGuest(getRefundToGuest())
                    .cancelledBy(getCancelledBy())
                    .guestServiceFee(getGuestServiceFee())
                    .guests(getGuest())
                    .total(getTotal())
                    .payoutToHost(getPayoutToHost())
                    .hostServiceFee(getHostServiceFee())
                    .currency(cancellationDetails.value!!.currency()!!)
                    .build()

            compositeDisposable.add(dataManager.cancelReservation(buildMutation)
                    .doOnSubscribe { setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data()?.cancelReservation()
                            if (data!!.status() == 200) {
                                retryCalled = ""
                                navigator.showToast(resourceProvider.getString(R.string.reservation_cancelled))
                                navigator.moveBackScreen()
                            } else if(data.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                data.errorMessage()?.let {
                                    navigator.showToast(it)
                                } ?: navigator.showError()
                            }
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        handleException(it)
                    } )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun getCancellationPolicy(): String {
        return try {
            cancellationDetails.value!!.cancellationPolicy()!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getCheckIn(): String {
        return try {
            cancellationDetails.value!!.checkIn()!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getCheckOut(): String {
        return try {
            cancellationDetails.value!!.checkOut()!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getRefundToGuest(): Double {
        return try {
            cancellationDetails.value!!.refundToGuest()!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getCancelledBy(): String {
        return try {
            cancellationDetails.value!!.cancelledBy()!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getGuest(): Int {
        return try {
            cancellationDetails.value!!.guests()!!
        } catch (E: KotlinNullPointerException) {
            0
        }
    }

    private fun getTotal(): Double {
        return try {
            cancellationDetails.value!!.total()!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getPayoutToHost(): Double {
        return try {
            cancellationDetails.value!!.payoutToHost()!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getHostServiceFee(): Double {
        return try {
            cancellationDetails.value!!.hostServiceFee()!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getGuestServiceFee(): Double {
        return try {
            cancellationDetails.value!!.guestServiceFee()!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    fun currencyConverter(currency: String, total: Double): String {
        return getCurrencySymbol() + Utils.formatDecimal(getConvertedRate(currency, total))
    }

}
