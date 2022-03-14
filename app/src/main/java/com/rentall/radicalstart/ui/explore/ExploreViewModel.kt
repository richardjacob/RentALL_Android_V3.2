package com.rentall.radicalstart.ui.explore

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.PagedList
import com.apollographql.apollo.api.Response
import com.rentall.radicalstart.GetDefaultSettingQuery
import com.rentall.radicalstart.GetExploreListingsQuery
import com.rentall.radicalstart.SearchListingQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.DefaultListing
import com.rentall.radicalstart.vo.Outcome
import com.rentall.radicalstart.vo.Photo
import com.rentall.radicalstart.vo.SearchListing
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class ExploreViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<ExploreNavigator>(dataManager,resourceProvider) {

     var popularLocations: MutableList<GetExploreListingsQuery.PopularLocationListing>? = null
    val repoResult = MutableLiveData<Listing<SearchListingQuery.Result>>()

    val searchPageResult = MutableLiveData<Single<Response<SearchListingQuery.Data>>>()
    val searchPageResult12 = MutableLiveData<ArrayList<SearchListing>>().apply { value = ArrayList() }

    val posts: LiveData<PagedList<SearchListingQuery.Result>>? = switchMap(repoResult) {
        it.pagedList
    }
    val networkState: LiveData<NetworkState>? = switchMap(repoResult) { it.networkState }

    fun repoRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun repoRefresh() {
        repoResult.value?.refresh?.invoke()
    }

    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()
    val amenities = MutableLiveData<HashSet<Int>>()
    val roomType = MutableLiveData<HashSet<Int>>()
    val personCapacity = MutableLiveData<String>()
    val bed = MutableLiveData<String>()
    val bathrooms = MutableLiveData<String>()
    val bedrooms = MutableLiveData<String>()
    val spaces = MutableLiveData<HashSet<Int>>()
    val houseRule = MutableLiveData<HashSet<Int>>()
    val priceRange = MutableLiveData<Array<Int>>()
    val bookingType = MutableLiveData<String>()
    val location = MutableLiveData<String>()

    val filterCount = MutableLiveData<Int>()
    val personCapacity1 = ObservableField<String>()
    val bed1 = ObservableField<String>()
    val bathrooms1 = ObservableField<String>()
    val bedrooms1 = ObservableField<String>()
    val searchLocation = MutableLiveData<Outcome<List<String>>>()
    val searchResult = MutableLiveData<Boolean>().apply { value = false }
    val minRange = MutableLiveData<Int>()
    val maxRange = MutableLiveData<Int>()
    val minRangeSelected = MutableLiveData<Int>()
    val maxRangeSelected = MutableLiveData<Int>()
    val guestMinCount  = MutableLiveData<Int>()
    val guestMaxCount = MutableLiveData<Int>()
    lateinit var exploreLists : MutableLiveData<GetExploreListingsQuery.Data>
    lateinit var defaultSettings : MutableLiveData<GetDefaultSettingQuery.Data>
    val exploreLists1 = MutableLiveData<GetExploreListingsQuery.Data>()

    val map = Collections.synchronizedMap(HashMap<String, ArrayList<DefaultListing>>())

    var defaultListingData = MutableLiveData<Map<String, ArrayList<DefaultListing>>>().apply { value = null }

    var isRefreshing = false

    val refreshWishList = MutableLiveData<Boolean>().apply { value = false }

    val currentPage = MutableLiveData<Int>().apply { value = 1 }

    var isGoogleLoaded = false

    init {
        startDate.value = "0"
        endDate.value = "0"
        location.value = ""
        filterCount.value = 0
        bookingType.value = ""
        personCapacity.value = "0"
        bed.value = "0"
        bathrooms.value = "0"
        bedrooms.value = "0"
      //  searchResult.value = false
        amenities.value =  HashSet()
        spaces.value =  HashSet()
        houseRule.value =  HashSet()
        roomType.value = HashSet()
        priceRange.value = emptyArray()
    }

    fun increaseCurrentPage(page: Int) {
        currentPage.value = page
    }

    fun getRefreshWishList(): Boolean {
        return refreshWishList.value?.let { it } ?: false
    }

    fun getSearchResult(): Boolean {
        return searchResult.value?.let { it } ?: false
    }

    /*fun loadExploreLists() : MutableLiveData<GetExploreListingsQuery.Data> {
        if (!::exploreLists.isInitialized){
            exploreLists = MutableLiveData()
            getexploreLists()
        }
        return exploreLists
    }*/

    fun getexploreLists() {
        navigator.disableIcons()
        val request = GetExploreListingsQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.getExploreListing(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            refreshWishList.value = false
                            try {
                                val response = it.data()!!
                                if (response.mostViewedListing?.status() == 200 &&
                                        response.recommend?.status() == 200 &&
                                        response.listingSettingsCommon?.status() == 200 &&
                                        response.searchSettings?.status() == 200 &&
                                        response.Currency()?.status() == 200) {
                                    setInitialData(response)
                                    //navigator.enableIcons()
                                } /*else if(response.mostViewedListing?.status() == 500) {
                                    navigator.openSessionExpire()
                                }*/
                                else {
                                    navigator.showError()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navigator.showError()
                            }
                        },
                        {
                            handleException(it)
                        }
                ))
    }

    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.doGetDefaultSettingApiCall(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                val response = it.data()!!
                                if (response.listingSettingsCommon?.status() == 200 &&
                                      //  response.recommend?.status() == 200 &&
                                        response.searchSettings?.status() == 200 &&
                                        response.Currency()?.status() == 200 //&&
                                     //   response.mostViewedListing?.status() == 200
                                ) {
                                     //setInitialData(response)
                                } else {
                                    navigator.showError()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navigator.showError()
                            }
                        },
                        {
                            it.printStackTrace()
                            handleException(it)
                        }
                ))
    }

    private fun setInitialData(response: GetExploreListingsQuery.Data) {
        try {
            minRange.value  = getConvertedRate(
                    response.searchSettings!!.results()!!.priceRangeCurrency()!!,
                    response.searchSettings!!.results()!!.minPrice()!!.toDouble()
            ).toInt()
            maxRange.value  = getConvertedRate(
                    response.searchSettings!!.results()!!.priceRangeCurrency()!!,
                    response.searchSettings!!.results()!!.maxPrice()!!.toDouble()
            ).toInt()
            response.listingSettingsCommon?.results()?.let {
                for(i in 0 until it.size) {
                    if (it[i].id() == 2) {
                        guestMinCount.value = it[i].listSettings()?.get(0)?.startValue()
                        guestMaxCount.value = it[i].listSettings()?.get(0)?.endValue()
                        break
                    }
                }
            }
            minRangeSelected.value = minRange.value
            maxRangeSelected.value = maxRange.value

            val mostRecommend = parseData(response, false)
            val mostViewed = parseData(response, true)
            map.clear()
            map["recommend"] = mostRecommend
            map["mostViewed"] = mostViewed

            exploreLists1.value = response
            defaultListingData.value = map
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun parseData(response: GetExploreListingsQuery.Data, isMostViewed: Boolean) : ArrayList<DefaultListing>{
        val listingData = ArrayList<DefaultListing>()
        if (isMostViewed) {
            response.mostViewedListing?.results()?.forEach {
                popularLocations = it.popularLocationListing()
                listingData.add(DefaultListing(
                        id = it.id()!!,
                        basePrice = it.listingData()!!.basePrice()!!,
                        beds = it.beds()!!,
                        bookingType = it.bookingType()!!,
                        coverPhoto = 0,//it.listPhotoName()!!,
                        currency = it.listingData()!!.currency()!!,
                        isListOwner = it.isListOwner!!,
                        listPhotoName = it.listPhotoName()!!,
                        personCapacity = it.personCapacity()!!,
                        reviewsCount = it.reviewsCount(),
                        reviewsStarRating = it.reviewsStarRating(),
                        roomType = it.roomType()!!,
                        title = it.title()!!,
                        wishListStatus = it.wishListStatus(),
                        wishListGroupCount = it.wishListGroupCount()
                ))
            }
        } else {
            response.recommend?.results()?.forEach {
                listingData.add(DefaultListing(
                        id = it.id()!!,
                        basePrice = it.listingData()!!.basePrice()!!,
                        beds = it.beds()!!,
                        bookingType = it.bookingType()!!,
                        coverPhoto = it.coverPhoto(),
                        currency = it.listingData()!!.currency()!!,
                        isListOwner = it.isListOwner!!,
                        listPhotoName = it.listPhotoName()!!,
                        personCapacity = it.personCapacity()!!,
                        reviewsCount = it.reviewsCount(),
                        reviewsStarRating = it.reviewsStarRating(),
                        roomType = it.roomType()!!,
                        title = it.title()!!,
                        wishListStatus = it.wishListStatus(),
                        wishListGroupCount = it.wishListGroupCount()
                ))
            }
        }
         return listingData
    }

    fun getPersonCapacity(): Int {
        return personCapacity.value?.toInt() ?: 0
    }

    fun getStartDate(): String {
        return startDate.value.toString()
    }

    fun getEndDate(): String {
        return endDate.value.toString()
    }

    fun getMinGuestCount() : Int? {
        return guestMinCount.value
    }

    fun getMaxGuestCount() : Int? {
        return guestMaxCount.value
    }

    fun startSearching() {
        increaseCurrentPage(1)
        navigator.searchForListing()
    }

    fun getSearchListing1(): Single<Response<SearchListingQuery.Data>> {
        searchResult.value = true
        catchAll("ExploreSetPriceRange") { setPriceRange() }
        val query = SearchListingQuery
                .builder()
                .bookingType(bookingType.value)
                .personCapacity(personCapacity.value?.toInt())
                .dates(getDate())
                .amenities(amenities.value?.toList())
                .beds(bed.value?.toInt())
                .bathrooms(bathrooms.value?.toInt())
                .bedrooms(bedrooms.value?.toInt())
                .roomType(roomType.value?.toList())
                .spaces(spaces.value?.toList())
                .houseRules(houseRule.value?.toList())
                .priceRange(priceRange.value?.toList())
                .address(location.value)
                .currentPage(currentPage.value)
                .currency(getUserCurrency())
                .build()
       /* dataManager.getSearchListing(query)
        searchPageResult.value = dataManager.getSearchListing(query)
        if (searchResult.value!!) {
            searchPageResult.value = dataManager.getSearchListing(query)
        }*/
        return dataManager.getSearchListing(query)//searchPageResult.value!!
    }

    fun getSearchListing() {
        try {
            if (getUserCurrency().isEmpty() || defaultListingData.value == null ) {

            } else {
                searchResult.value = true
                catchAll("ExploreSetPriceRange") { setPriceRange() }
                val query = SearchListingQuery
                        .builder()
                        .bookingType(bookingType.value)
                        .personCapacity(personCapacity.value?.toInt())
                        .dates(getDate())
                        .amenities(amenities.value?.toList())
                        .beds(bed.value?.toInt())
                        .bathrooms(bathrooms.value?.toInt())
                        .bedrooms(bedrooms.value?.toInt())
                        .roomType(roomType.value?.toList())
                        .spaces(spaces.value?.toList())
                        .houseRules(houseRule.value?.toList())
                        .priceRange(priceRange.value?.toList())
                        .address(location.value)
                if (searchResult.value!!) {
                    repoResult.value = dataManager.listOfSearchListing(query, 5)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }

    }

    private fun getDate(): String? {
        return if (startDate.value == "0" || endDate.value == "0") {
            null
        } else {
            "'${startDate.value}'" +" AND "+ "'${endDate.value}'"
        }
    }

    private fun setPriceRange() {
        try {
            if (minRangeSelected.value != minRange.value || maxRangeSelected.value != maxRange.value) {
                priceRange.value = null
                val min = getConvertedRate(getUserCurrency(), minRangeSelected.value!!.toDouble()).toInt()
                val max = getConvertedRate(getUserCurrency(), maxRangeSelected.value!!.toDouble()).toInt()
                priceRange.value = arrayOf(min, max)
            } else {
                priceRange.value = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun searchLocation(location: String) {
        compositeDisposable.add(dataManager.getLocationAutoComplete(location)
                .performOnBackOutOnMain(scheduler)
                .subscribe ({ r ->
                    val list = ArrayList<String>()
                    r?.forEach {
                        list.add(it.getFullText(null).toString())
                    }
                    searchLocation.value = Outcome.success(list)
                }, {
                    t -> t.printStackTrace()
                    Log.d("Places Failed","Places Failed")
                }))
    }

    fun setLocation(location: String) {
        this.location.value = location
    }

    fun clearSearchRequest() {
        increaseCurrentPage(1)
        searchResult.value = false
        startDate.value = "0"
        endDate.value = "0"
        amenities.value = HashSet()
        roomType.value =  HashSet()
        personCapacity.value = "0"
        bed.value = "0"
        bathrooms.value = "0"
        bedrooms.value = "0"
        spaces.value =  HashSet()
        houseRule.value = HashSet()
        priceRange.value = emptyArray()
        location.value = ""
        bookingType.value = ""
        filterCount.value = 0
        minRangeSelected.value = minRange.value
        maxRangeSelected.value = maxRange.value
        defaultListingData.value = defaultListingData.value
    }

    fun refreshOnWishList(flag: Boolean) {
        refreshWishList.value = flag
    }

    fun changeWishListStatus(value: Int?, flag: Boolean, count: Int) {
        val list = defaultListingData.value
        val recommend = list?.get("recommend")
        val mostViewed = list?.get("mostViewed")
        recommend?.forEach {
            if (it.id == value) {
                it.wishListGroupCount = count
                it.wishListStatus = flag
            }
        }
        mostViewed?.forEach {
            if (it.id == value) {
                it.wishListGroupCount = count
                it.wishListStatus = flag
            }
        }
        val map = Collections.synchronizedMap(HashMap<String, ArrayList<DefaultListing>>())
        map["recommend"] = recommend
        map["mostViewed"] = mostViewed
        defaultListingData.value = map
    }

    fun changeWishListStatusInSearch(value: Int?, flag: Boolean, count: Int) {
        val list = searchPageResult12.value
        list?.forEach {
            if (it.id == value) {
                it.wishListGroupCount = count
                it.wishListStatus = flag//true//false//flag
            }
        }
        searchPageResult12.value = list
        changeWishListStatus(value, flag, count)
    }

    fun setSearchData(results: List<SearchListingQuery.Result>?) {
        val list = searchPageResult12.value
        list?.addAll(parseData(results))
        searchPageResult12.value = list
    }

    private fun parseData(results: List<SearchListingQuery.Result>?): List<SearchListing> {
        val list = ArrayList<SearchListing>()
        results?.forEach {
            val photoList = ArrayList<Photo>()
            it.listPhotos()?.forEach {
                photoList.add(Photo(it.id()!!, it.name()!!))
            }
            list.add(SearchListing(
                    id = it.id()!!,
                    wishListStatus = it.wishListStatus(),
                    title = it.title()!!,
                    roomType = it.roomType(),
                    reviewsStarRating = it.reviewsStarRating(),
                    reviewsCount = it.reviewsCount(),
                    personCapacity = it.personCapacity()!!,
                    listPhotoName = it.listPhotoName(),
                    isListOwner = it.isListOwner,
                    currency = it.listingData()?.currency()!!,
                    coverPhoto = it.coverPhoto(),
                    bookingType = it.bookingType()!!,
                    beds = it.beds()!!,
                    basePrice = it.listingData()?.basePrice()!!,
                    listPhotos = photoList,
                    lat = it.lat()!!,
                    lng = it.lng()!!
            ))
        }
        return list
    }
}