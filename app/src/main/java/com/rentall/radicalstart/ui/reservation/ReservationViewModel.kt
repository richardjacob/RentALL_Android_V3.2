package com.rentall.radicalstart.ui.reservation

import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.GetReservationQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class ReservationViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<ReservationNavigator>(dataManager,resourceProvider) {

    var reservationComplete = MutableLiveData<GetReservationQuery.GetReservation>()
    var reservation = MutableLiveData<GetReservationQuery.Results>()
    val reservationId = MutableLiveData<Int>()

    fun getName(): String? {
       return dataManager.currentUserName
    }

    fun getSiteName(): String? {
        return dataManager.siteName
    }

    fun getReservationDetails() {
        val buildQuery = GetReservationQuery
                .builder()
                .reservationId(reservationId.value!!)
                .convertCurrency(getUserCurrency())
                .build()
        compositeDisposable.add(dataManager.getReservationDetails(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.reservation
                        if (data?.status() == 200) {
                            try {
                                if (data?.results()!!.listData() == null){
                                    navigator.show404Page()
                                }
                            } catch (e: Exception) {
                            }
                            reservationComplete.value = response.data()!!.reservation
                            reservation.value = data.results()

                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }

                }, {
                   handleException(it)
                } )
        )
    }

    fun currencyConverter(currency: String, total: Double): String {
        return getCurrencySymbol() + Utils.formatDecimal(getConvertedRate(currency, total))
    }
}