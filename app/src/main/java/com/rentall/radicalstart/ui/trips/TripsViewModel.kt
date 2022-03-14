package com.rentall.radicalstart.ui.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.rentall.radicalstart.GetAllReservationQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject


class TripsViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val currencyBase = MutableLiveData<String>()
    val currencyRates = MutableLiveData<String>()
    val currencyUser = MutableLiveData<String>()

    val tripResult = MutableLiveData<Listing<GetAllReservationQuery.Result>>()
    lateinit var tripList: LiveData<PagedList<GetAllReservationQuery.Result>>
    val networkState: LiveData<NetworkState> = Transformations.switchMap(tripResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(tripResult) { it.refreshState }

    val tripType = MutableLiveData<String>()

    var isRefreshing = false

    fun loadTrips(dateFilters: String) : LiveData<PagedList<GetAllReservationQuery.Result>> {
        if (!::tripList.isInitialized) {
            tripList = MutableLiveData()
            val buildQuery = GetAllReservationQuery
                    .builder()
                    .dateFilter("previous")
                    .userType("Guest")
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
                    .userType("Guest")
            upcomingTripResult.value = dataManager.listOfTripsList(buildQuery, 10)
            upcomingTripList = Transformations.switchMap(upcomingTripResult) {
                it.pagedList
            }
        }
        return upcomingTripList
    }

    fun upcomingTripRefresh() {
        upcomingTripResult.value?.refresh?.invoke()
    }

    fun upcomingTripRetry() {
        upcomingTripResult.value?.retry?.invoke()
    }
}
