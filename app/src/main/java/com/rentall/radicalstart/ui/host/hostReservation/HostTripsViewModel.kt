package com.rentall.radicalstart.ui.host.hostReservation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.rentall.radicalstart.GetAllReservationQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ReservationStatusMutation
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class HostTripsViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val currencyBase = MutableLiveData<String>()
    val currencyRates= MutableLiveData<String>()
    val currencyUser = MutableLiveData<String>()

    val tripResult = MutableLiveData<Listing<GetAllReservationQuery.Result>>()
    lateinit var tripList: LiveData<PagedList<GetAllReservationQuery.Result>>
    val networkState: LiveData<NetworkState> = Transformations.switchMap(tripResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(tripResult) { it.refreshState }

    val tripType = MutableLiveData<String>()

    var retryCalled = ""

    fun loadTrips(dateFilters: String) : LiveData<PagedList<GetAllReservationQuery.Result>> {
        if (!::tripList.isInitialized) {
            tripList = MutableLiveData()
            val buildQuery = GetAllReservationQuery
                    .builder()
                    .dateFilter("previous")
                    .userType("host")
            tripResult.value = dataManager.listOfTripsList(buildQuery, 10)
            tripList = Transformations.switchMap(tripResult) {
                it.pagedList
            }
        }
        return tripList
    }

    fun tripRefresh() {
        tripResult.value?.refresh?.invoke()
    }

    fun tripRetry() {
        tripResult.value?.retry?.invoke()
    }

    val upcomingTripResult = MutableLiveData<Listing<GetAllReservationQuery.Result>>()
    lateinit var upcomingTripList: LiveData<PagedList<GetAllReservationQuery.Result>>
    val upcomingNetworkState: LiveData<NetworkState> = Transformations.switchMap(upcomingTripResult) { it.networkState }
    val upcomingRefreshState: LiveData<NetworkState> = Transformations.switchMap(upcomingTripResult) { it.refreshState }

    fun loadUpcomingTrips(dateFilters: String) : LiveData<PagedList<GetAllReservationQuery.Result>> {
        if (!::upcomingTripList.isInitialized) {
            upcomingTripList = MutableLiveData()
            val buildQuery = GetAllReservationQuery
                    .builder()
                    .dateFilter("upcoming")
                    .userType("host")
            upcomingTripResult.value = dataManager.listOfTripsList(buildQuery, 10)
            upcomingTripList = Transformations.switchMap(upcomingTripResult) {
                it.pagedList
            }
        }
        return upcomingTripList
    }

    fun approveReservation(threadId : Int, content: String, type: String, startDate: String, endDate: String, personCapacity: Int, reservationId: Int, actionType: String){
        val mutate = ReservationStatusMutation
                .builder()
                .threadId(threadId)
                .content(content)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .personCapacity(personCapacity)
                .reservationId(reservationId)
                .actionType(actionType)
                .build()
        compositeDisposable.add(dataManager.getReseravtionStatus(mutate)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                it.data()?.let { response->
                                    if (response.ReservationStatus()?.status() == 200) {
                                        if (actionType == "approved"){
                                            navigator.showToast(resourceProvider.getString(R.string.reservation_approved))
                                        }else if (actionType == "declined"){
                                            navigator.showToast(resourceProvider.getString(R.string.reservation_declined))
                                        }
                                        upcomingTripResult.value?.refresh?.invoke()
                                    } else if(response.ReservationStatus()?.status() == 500) {
                                        navigator.openSessionExpire()
                                    }else{
                                        navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                                    }
                                } ?: navigator.showError()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        { handleException(it) }
                ))
    }

    fun upcomingTripRefresh() {
        upcomingTripResult.value?.refresh?.invoke()
    }

    fun upcomingTripRetry() {
        upcomingTripResult.value?.retry?.invoke()
    }
}
