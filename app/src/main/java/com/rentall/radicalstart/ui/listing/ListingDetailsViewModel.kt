package com.rentall.radicalstart.ui.listing

import android.content.Intent
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.PagedList
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.ListingInitData
import java.util.*
import javax.inject.Inject

class ListingDetailsViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<ListingNavigator>(dataManager,resourceProvider) {

    val isSimilarListingLoad = MutableLiveData<Boolean?>()
    val isListingDetailsLoad = MutableLiveData<Boolean>()
    val isReviewsLoad = MutableLiveData<Boolean>()
    val isBillingCalculationLoad = MutableLiveData<Boolean>()


    val similarListing = MutableLiveData<List<GetSimilarListingQuery.Result>>()
    val repoResult = MutableLiveData<Listing<GetPropertyReviewsQuery.Result>>()
    val posts: LiveData<PagedList<GetPropertyReviewsQuery.Result>> = switchMap(repoResult) {
        it.pagedList
    }
    val networkState: LiveData<NetworkState> = switchMap(repoResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = switchMap(repoResult) { it.refreshState }
    val reviewCount: LiveData<Int> = switchMap(repoResult) { it.count }
    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()
    val priceBreakDown = ObservableBoolean(false)
    val billingCalculation = MutableLiveData<GetBillingCalculationQuery.Result>()
    val bookingText = ObservableField(resourceProvider.getString(R.string.check_availability))
    val dateGuestCount = MutableLiveData<ListingDetails.PriceBreakDown>()

    val personCapacity1 = ObservableField<String>()
    val personCapacity = MutableLiveData<String>()

    lateinit var initialValue : MutableLiveData<ListingInitData>
    lateinit var listingDetails : MutableLiveData<ViewListingDetailsQuery.Results>
    var bathroomType="Private Room"
    var blockedDatesArray = ArrayList<String>()

    var isPreview = false
    var ispublish = ObservableBoolean(false)

    val msg = ObservableField<String>("")

    val loadedApis = MutableLiveData<ArrayList<Int>>()

    val isWishList = MutableLiveData<Boolean>()

    val carouselPosition = MutableLiveData<Int>()
    val carouselUrl = MutableLiveData<String>()

    val isWishListChanged = MutableLiveData<Boolean>()

    var retryCalled = ""

    init {
       // startDate.value = "0"
       // endDate.value = "0"
        /*isSimilarListingLoad.value = false
        isListingDetailsLoad.value = false
        isReviewsLoad.value = false
        isBillingCalculationLoad.value = false*/
        similarListing.value = emptyList()
        loadedApis.value = arrayListOf()
        carouselUrl.value = ""
    }

    fun getIsWishListChanged() : Boolean {
        return isWishListChanged.value?.let { it } ?: false
    }

    fun setCarouselCurrentPhoto(url: String) {
        carouselUrl.value?.let {
           if(it != url) {
               carouselUrl.value = url
           } else {
               return
           }
            if (::initialValue.isInitialized) {
                initialValue.value?.photo?.let { photoList ->
                    if (photoList.size > 0) {
                        for (i in 0 until photoList.size) {
                            if (url == photoList[i]) {
                                carouselPosition.value = i
                                break
                            }
                        }
                    }
                }
            }
       }
    }

    fun loadInitialValues(intent: Intent) : MutableLiveData<ListingInitData> {
        if (!::initialValue.isInitialized) {
            initialValue = MutableLiveData()
            setInitialValuesFromIntent(intent)
        }
        return initialValue
    }

    fun isListingDetailsInitialized() : Boolean {
        return isListingDetailsLoad.value?.let { it } ?: false
    }

    private fun setInitialValuesFromIntent (intent: Intent) {
        try {
            val initData = intent.extras!!.getParcelable <ListingInitData> ("listingInitData") as ListingInitData
            if(initData.isPreview){
                isPreview = true
            }else{
                isPreview = false
            }
            if(initData.guestCount==0){
                initData.guestCount=1
            }
            initialValue.value = initData


        } catch (e :KotlinNullPointerException) {
            navigator.showError()
        }
    }

    fun loadListingDetails() : MutableLiveData<ViewListingDetailsQuery.Results> {
        if (!::listingDetails.isInitialized) {
            listingDetails = MutableLiveData()
            getListingDetails()
        }
        return listingDetails
    }

    fun loadListingDetailsWishList() {
        isWishListChanged.value = true
        getListingDetails()
    }

    fun loadSimilarWishList() {
        isWishListChanged.value = true
        getSimilarListing()
    }

    fun getReview() {
        try {
            listingDetails.value?.id()?.let {
                repoResult.value = dataManager.listOfReview(it, listingDetails.value?.userId()!!, 10)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun getListingDetails() {
        initialValue.value?.let {
            isListingDetailsLoad.value = false
            var buildQuery : ViewListingDetailsQuery
            if(isPreview){
                buildQuery = ViewListingDetailsQuery
                        .builder()
                        .listId(initialValue.value!!.id)
                        .preview(true)
                        .build()
            }else {
                buildQuery = ViewListingDetailsQuery
                        .builder()
                        .listId(initialValue.value!!.id)
                        .build()
            }

            compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data()?.viewListing()
                            if (data!!.status() == 200) {
                                val dates = data.results()!!.blockedDates()
                                if(dates != null){
                                    if(dates.size > 0){
                                        dates.forEachIndexed { index, blockedDate ->
                                            if(blockedDate.calendarStatus().equals("available").not()) {
                                                val timestamp = blockedDate.blockedDates()!!
                                                blockedDatesArray.add(Utils.getBlockedDateFormat(timestamp))
                                            }
                                        }
                                    }
                                }

                                data.results()!!.settingsData()!!.forEachIndexed { _, settingsDatum ->
                                    if(settingsDatum.listsettings()!=null){
                                        if(settingsDatum.listsettings()!!.settingsType()!!.typeName()=="bathroomType"){
                                            bathroomType=settingsDatum.listsettings()!!.itemName()!!
                                        }
                                    }
                                }


                                if(initialValue.value?.isPreview!!) {
                                    ispublish.set(false)
                                }else{
                                    ispublish.set(data.results()!!.isPublished!!)
                                }
                                listingDetails.value = data.results()
                                isListingDetailsLoad.value = true
                            } else {
                                navigator.show404Screen()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, { handleException(it) }
                    )
            )
        }
    }

    fun getSimilarListing() {
        isSimilarListingLoad.value = true
        val buildQuery = GetSimilarListingQuery
                .builder()
                .lat(listingDetails.value?.lat())
                .lng(listingDetails.value?.lng())
                .listId(listingDetails.value?.id())
                .build()

        compositeDisposable.add(dataManager.doSimilarListingApiCall(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    isSimilarListingLoad.value = false
                    removeApiTolist(1)
                    try {
                        val data = response.data()!!.similarListing
                        if (data!!.status() == 200) {
                            if (data.results()?.size!! > 0) {
                                similarListing.value = data.results()
                            }
                        } else if(data.status() == 500) {
                            navigator.openSessionExpire()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, { isSimilarListingLoad.value = false
                    addApiTolist(1)
                    handleException(it) } ))
    }

    fun getBillingCalculation() {
        try {
            isBillingCalculationLoad.value = true
            if (initialValue.value!!.guestCount == 0) {
                initialValue.value!!.guestCount = initialValue.value!!.guestCount.plus(1)
            }
            if (startDate.value == null && endDate.value == null) {
                return
            }
            val buildQuery = GetBillingCalculationQuery
                    .builder()
                    .listId(initialValue.value!!.id)
                    .startDate(startDate.value!!)
                    .endDate(endDate.value!!)
                    .guests(initialValue.value!!.guestCount)
                    .convertCurrency(initialValue.value!!.selectedCurrency)
                    .build()

            compositeDisposable.add(dataManager.getBillingCalculation(buildQuery)
                    .doOnSubscribe { priceBreakDown.set(false); setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            removeApiTolist(2)
                            val data = response.data()?.billingCalculation
                            if (data?.status() == 200) {
                                billingCalculation.value = data.result()
                                if (initialValue.value!!.bookingType == "request") {
                                    bookingText.set(resourceProvider.getString(R.string.request_to_book))
                                } else {
                                    bookingText.set(resourceProvider.getString(R.string.book_txt))
                                }
                                priceBreakDown.set(true)
                                navigator.hideSnackbar()
                            } else if(data?.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                billingCalculation.value = null
                                priceBreakDown.set(false)
                                bookingText.set(resourceProvider.getString(R.string.check_availability))
                                startDate.value = "0"
                                endDate.value = "0"
                                data?.errorMessage()?.let {
                                    navigator.showSnackbar("Info  ", it)
                                } ?: navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                            }
                            isBillingCalculationLoad.value = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, { isBillingCalculationLoad.value = false
                        billingCalculation.value = null
                        addApiTolist(2)
                        handleException(it) } ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkVerification() {
        val buildQuery = GetProfileQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    //removeApiTolist(3)
                    try {
                        val data = response.data()!!.userAccount()
                        if (data?.status() == 200) {
                            val result = data.result()
                            if(result!!.verification()!!.isEmailConfirmed!!.not()) {
                                 navigator.showSnackbar(
                                    resourceProvider.getString(R.string.verification),
                                    resourceProvider.getString(R.string.email_not_verified), resourceProvider.getString(R.string.dismiss))
                            } else {
                                dataManager.currentUserProfilePicUrl = result.picture()
                                if (result.picture().isNullOrEmpty()) {
                                    navigator.openBillingActivity(false)
                                } else {
                                    navigator.openBillingActivity(true)
                                }
                            }
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            //navigator.showError()
                            navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                        }
                    } catch (e: Exception) {
                    //    navigator.showError()
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                    }
                }, {
                    addApiTolist(3)
                    handleException(it)
                    //navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                } )
        )
    }

    fun getStartDate(): String? {
        return startDate.value.toString()
    }

    fun getEndDate(): String? {
        return endDate.value.toString()
    }

    fun contactHost() {
        try {
            val buildQuery = ContactHostMutation
                    .builder()
                    .startDate(startDate.value!!)
                    .endDate(endDate.value!!)
                    .personCapacity(initialValue.value?.guestCount!!)
                    .content(msg.get()!!)
                    .hostId(listingDetails.value?.userId()!!)
                    .listId(listingDetails.value?.id()!!)
                    .userId(dataManager.currentUserId!!)
                    .type("inquiry")
                    .build()

            compositeDisposable.add(dataManager.contactHost(buildQuery)
                    .doOnSubscribe { setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        //removeApiTolist(4)
                        try {
                            val data = response.data()!!.CreateEnquiry()
                            if (data?.status() == 200) {
                                msg.set("")
                                navigator.showToast(resourceProvider.getString(R.string.your_message_sent_to_host))
                                navigator.removeSubScreen()
                            } else if(data?.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                                //navigator.showError()
                            }
                        } catch (e: Exception) {
                            navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                            //navigator.showError()
                        }
                    }, {
                        addApiTolist(4)
                        //navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                        handleException(it) } )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addApiTolist(id: Int) {
        val api = loadedApis.value
        api?.add(id)
        loadedApis.value = api
    }

    fun removeApiTolist(id: Int) {
        val api = loadedApis.value
        api?.remove(id)
        loadedApis.value = api
    }

    fun currencyConverter(currency: String, total: Double): String {
        return getCurrencySymbol() + getConvertedRate(currency, total).toString()
    }

    override fun onCleared() {
        super.onCleared()
    }
}