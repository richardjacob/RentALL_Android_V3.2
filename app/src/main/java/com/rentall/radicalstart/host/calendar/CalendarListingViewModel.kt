package com.rentall.radicalstart.host.calendar

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.GetListingSpecialPriceQuery
import com.rentall.radicalstart.ManageListingsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.UpdateSpecialPriceMutation
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import org.threeten.bp.LocalDate
import javax.inject.Inject


class CalendarListingViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<CalendarAvailabilityNavigator>(dataManager,resourceProvider) {

    data class list(val id: Int, val title: String = "", val room: String? ="", val img: String? ="" )

    val startDate = MutableLiveData<LocalDate>()
    val endDate = MutableLiveData<LocalDate>()
    val selectedListing = MutableLiveData<list>()
    val manageListing1 = MutableLiveData<ArrayList<list>>()
    val blockedDates1 = MutableLiveData<List<GetListingSpecialPriceQuery.Result>>()
    val calendarStatus = ObservableField<String>("available")
    val specialPrice = ObservableField<String>("")

    val isCalendarLoading = ObservableBoolean(false)
    val navigateBack = ObservableBoolean(false)

    init {
//        getManageListings()
    }

    fun getManageListings() {
        navigator.hideWholeView(true)

        val buildQuery = ManageListingsQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.getManageListings(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()?.ManageListings()
                        if (data!!.status() == 200) {
                            setData(data.results()!!)
                        } else if(data.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            data.errorMessage()?.let {
                                navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                            } ?: navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    Log.d("dsfs", "Sdf")
                    handleException(it)
                    navigator.hideWholeView(true)
                    it.printStackTrace()
                } )
        )
    }

    private fun setData(data: MutableList<ManageListingsQuery.Result>) {
        try {
            val list = ArrayList<list>()
            data.forEachIndexed { _, result ->
                var title = ""
                if(result.title().isNullOrEmpty()){
                    title = ""
                }else{
                    title = result.title()!!
                }
                if (result.isReady!! && (result.listApprovalStatus() == "approved" || result.isPublished == true)) {
                    list.add(list(result.id()!!,
                            title,
                            result.settingsData()!![0].listsettings()?.itemName(),
                            result.listPhotoName()))
                    if (list.size == 1) {
                        selectedList(list[0])
                    }
                }
            }
            manageListing1.value = list
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun selectedList(list: list) {
        selectedListing.value = list
        getListBlockedDates()
    }

    fun getListBlockedDates() {
        val buildQuery = GetListingSpecialPriceQuery
                .builder()
                .listId(selectedListing.value!!.id)
                .build()
        navigator.hideCalendar(true)
        isCalendarLoading.set(true)
        compositeDisposable.add(dataManager.getListSpecialBlockedDates(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()?.listingSpecialPrice
                        if (data!!.status() == 200) {
                            isCalendarLoading.set(false)
                            blockedDates1.value = data.results()!!
                        } else if(data.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            isCalendarLoading.set(true)
                            navigator.hideCalendar(true)
                            data.errorMessage()?.let {
                                navigator.showToast(it)
                            } ?: navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        isCalendarLoading.set(true)
                        navigator.hideCalendar(true)
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    isCalendarLoading.set(true)
                    it.printStackTrace()
                    navigator.hideCalendar(true)
                    handleException(it)
                } )
        )
    }

    fun updateBlockedDates() {
        navigator.hideKeyboard()
        isLoading.set(true)
        val buildQueryBuilder = UpdateSpecialPriceMutation.builder()
        buildQueryBuilder.listId(selectedListing.value!!.id)
        buildQueryBuilder.blockedDates(getSelectedDates())
        if (calendarStatus.get() == "available") {
            if(specialPrice.get()!!.isNotEmpty()){
                buildQueryBuilder.isSpecialPrice(specialPrice.get()?.toDouble())
            }else{
                buildQueryBuilder.isSpecialPrice(null)
            }
        }
        buildQueryBuilder.calendarStatus(calendarStatus.get())

        compositeDisposable.add(dataManager.getUpdateSpecialListBlockedDates(buildQueryBuilder.build())
                .performOnBackOutOnMain(scheduler)
                .doFinally { setIsLoading(false) }
                .subscribe( { response ->
                    try {
                        val data = response.data()?.UpdateSpecialPrice()
                        if (data!!.status() == 200) {
                            navigateBack.set(false)
                            navigator.showToast(resourceProvider.getString(R.string.your_dates_updated))
                            navigator.closeAvailability(true)
                        } else if(data.status() == 500) {
                            navigateBack.set(false)
                            navigator.openSessionExpire()
                        } else {
                            navigateBack.set(true)
                            navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    it.printStackTrace()
                    handleException(it, true)
                } )
        )
    }

    private fun getSelectedDates(): MutableList<String> {
        val selectedList = ArrayList<String>()
        if (startDate.value != null && endDate.value != null) {
            var date = startDate.value!!.minusDays(1)
            while (date!!.isBefore(endDate.value)) {
                date = date.plusDays(1)
                selectedList.add(date.toString())
            }
        } else {
            selectedList.add(startDate.value.toString())
        }
        return selectedList
    }
}