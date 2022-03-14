package com.rentall.radicalstart.ui.host.hostInbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.rentall.radicalstart.GetAllThreadsQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class HostInboxViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val inboxResult = MutableLiveData<Listing<GetAllThreadsQuery.Result>>()
    lateinit var inboxList: LiveData<PagedList<GetAllThreadsQuery.Result>>
    val networkState = Transformations.switchMap(inboxResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(inboxResult) { it.refreshState }!!

    fun loadInbox() : LiveData<PagedList<GetAllThreadsQuery.Result>> {
        if (!::inboxList.isInitialized) {
            inboxList = MutableLiveData()
            val buildQuery = GetAllThreadsQuery
                    .builder()
                    .threadType("host")
            inboxResult.value = dataManager.listOfInbox(buildQuery, 10)
            inboxList = Transformations.switchMap(inboxResult) {
                it.pagedList
            }
        }
        return inboxList
    }

    fun inboxRefresh() {
        inboxResult.value?.refresh?.invoke()
    }

    fun inboxRetry() {
        inboxResult.value?.retry?.invoke()
    }

    fun getInboxList() {
        val buildQuery = GetAllThreadsQuery
                .builder()
                .threadType("host")
        inboxResult.value = dataManager.listOfInbox(buildQuery, 10)
    }
}