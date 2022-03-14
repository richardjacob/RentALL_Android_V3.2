package com.rentall.radicalstart.ui.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.apollographql.apollo.api.Response
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.Photo
import com.rentall.radicalstart.vo.SavedList
import com.rentall.radicalstart.vo.SearchListing
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SavedViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<SavedNavigator>(dataManager,resourceProvider) {

    var createdWishlistGroup = ArrayList<SavedList>()
    lateinit var wishListGroup : MutableLiveData<List<SavedList>>
    val wishListGroupCopy = MutableLiveData<List<SavedList>>()
    private  var wishList  =  MutableLiveData<List<GetWishListGroupQuery.WishList>>()
    var wishlistType : Int? = 0
    var listId = MutableLiveData<Int>()
    var listImage = MutableLiveData<String>()
    var listGroupCount = MutableLiveData<Int>()
    var screen = MutableLiveData<String>()
    var isSimilar = MutableLiveData<Boolean>()

    var isLoadingInProgess = MutableLiveData<Int>().apply { value = 0 }

    var retryArray = MutableLiveData<ArrayList<String>>()

    var isRefreshing = false

    val isWishListAdded = MutableLiveData<Boolean>()

    val firstSetValue = MutableLiveData<Boolean>().apply { value = false }

    var retryCalled = ""

    fun getIsWishListAdded(): Boolean {
        return isWishListAdded.value?.let { it } ?: false
    }

    fun setListDetails(id: Int, image: String, count: Int) {
        listId.value = id
        listImage.value = image
        listGroupCount.value = count
    }

    fun loadWishListGroup(): MutableLiveData<List<SavedList>> {
        if (!::wishListGroup.isInitialized) {
            wishListGroup = MutableLiveData()
            getAllWishListGroup()

        }
        return wishListGroup
    }

    fun getAllWishListGroup() {
        val buildMutation = GetAllWishListGroupQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.getAllWishListGroup(buildMutation)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.allWishListGroup?.status() == 200) {
                            wishListGroup.value = parseList(response.data()?.allWishListGroup?.results())
                            wishListGroupCopy.value = parseList(response.data()?.allWishListGroup?.results())//wishListGroup.value
                        } else if(response.data()?.allWishListGroup?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            wishListGroup.value = emptyList()
                            //navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it, true)
                } )
        )
    }

    fun getWishList(page: Int) {
        val buildMutation = GetWishListGroupQuery
                .builder()
                //.id(dataManager.wishlistId!!)
                .currentPage(1)
                .build()
        compositeDisposable.add(dataManager.getWishListGroup(buildMutation)
                .delay(1, TimeUnit.SECONDS)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.wishListGroup?.status() == 200) {
                            wishList.value = response.data()?.wishListGroup!!.results()!!.wishLists()!!
                        }else if(response.data()?.wishListGroup?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, { handleException(it) } )
        )
    }

    private fun parseList(results: List<GetAllWishListGroupQuery.Result>?): List<SavedList>? {
        val list = ArrayList<SavedList>()
        try {
            results?.forEachIndexed { index, result ->
                var isInGroup = false
                result.wishListIds()?.let {
                    if (it.contains(listId.value)) {
                        isInGroup = true
                    }
                }

                list.add(SavedList(
                        result.id()!!,
                        result.name()!!,
                        result.wishListCover()?.listData()?.listPhotoName(),
                        result.wishListCount(),
                        isInGroup,
                        result.id().toString()
                ))
            }
            list.reverse()
        } catch (e: Exception) {
           navigator.showError()
        }
        return list
    }

    fun createWishList(listId: Int, groupId: Int, eventKey: Boolean, flag: Boolean = false) {
        isWishListAdded.value=false
        isLoadingInProgess.value = isLoadingInProgess.value?.plus(1)
        val buildMutation = CreateWishListMutation
                .builder()
                .listId(listId)
                .wishListGroupId(groupId)
                .eventKey(eventKey)
                .build()

        compositeDisposable.add(dataManager.CreateWishList(buildMutation)
               // .delay(5000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.CreateWishList()?.status() == 200) {
                            retryCalled = ""
                            if (flag) {
                                navigator.reloadExplore()
                               // wishListRefresh()
                                 deleteList(listId)
                            } else {
                                isWishListAdded.value = true
                                changeWishListStatus(groupId, eventKey)
                                //getAllWishListGroup()
                            }
                        } else if(response.data()?.CreateWishList()?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                        navigator.showError()
                    }
                }, {
                    isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
                    if(!flag) {
                        changeRetry(groupId)
                    }
                    handleException(it, true)
                } )
        )
    }

    fun changeRetry(groupID: Int){
        val list = wishListGroup.value
        list?.forEachIndexed { index, savedList: SavedList ->
            if(savedList.id == groupID) {
                savedList.progress = AuthViewModel.LottieProgress.NORMAL
                savedList.isRetry = retryCalled
            }
        }
        wishListGroup.value = list
        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
    }

    private fun deleteList(listId: Int) {
        try {
            val list = searchPageResult12.value
            for (i in 0 until list!!.size) {
                if (listId == list[i].id) {
                    list.removeAt(i)
                    break
                }
            }
            searchPageResult12.value = list
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private fun changeWishListStatus(groupId: Int, eventKey: Boolean) {
        val list = wishListGroup.value
        list?.forEachIndexed { index, savedList: SavedList ->
            if(savedList.id == groupId) {
                if (eventKey) {
                    listGroupCount.value = listGroupCount.value?.plus(1)
                    savedList.img = listImage.value
                    savedList.wishListCount = savedList.wishListCount?.plus(1)
                } else {
                    listGroupCount.value = listGroupCount.value?.minus(1)
                    savedList.wishListCount = savedList.wishListCount?.minus(1)
                    if (savedList.wishListCount!! <= 0) {
                        savedList.img = ""
                    } else {
                        savedList.img = wishListGroupCopy.value?.get(index)?.img
                    }
                }
                savedList.isWishList = eventKey
                savedList.progress = AuthViewModel.LottieProgress.NORMAL
            }
        }
        wishListGroup.value = list
        isLoadingInProgess.value = isLoadingInProgess.value?.minus(1)
    }

    fun deleteWishListGroup(groupId: Int) {
        val buildMutation = DeleteWishListGroupMutation
                .builder()
                .id(groupId)
                .build()

        compositeDisposable.add(dataManager.deleteWishListGroup(buildMutation)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.DeleteWishListGroup()?.status() == 200) {
                            retryCalled = ""
                            navigator.reloadExplore()
                            navigator.moveUpScreen()
                        }else if(response.data()?.DeleteWishListGroup()?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, { handleException(it) } )
        )
    }

    fun updateWishListGroup(listId: Int) {
        val buildMutation = UpdateWishListGroupMutation
                .builder()
                .id(listId)
                .isPublic(wishlistType!!)
                .build()

        compositeDisposable.add(dataManager.updateWishListGroup(buildMutation)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.UpdateWishListGroup()?.status() == 200) {
                            //navigator.navigateScreen(PHScreen.MOVETOHOME)
                        }else if(response.data()?.UpdateWishListGroup()?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, { handleException(it) } )
        )
    }

    val wishListGroupResult = MutableLiveData<Listing<GetAllWishListGroupQuery.Result>>()
    lateinit var wishListGroupList: LiveData<PagedList<GetAllWishListGroupQuery.Result>>
    val wishListGroupNetworkState = Transformations.switchMap(wishListGroupResult) { it.networkState }!!
    val wishListGroupRefreshState = Transformations.switchMap(wishListGroupResult) { it.refreshState }!!

    fun loadwishListGroup() : LiveData<PagedList<GetAllWishListGroupQuery.Result>> {
        if (!::wishListGroupList.isInitialized) {
            wishListGroupList = MutableLiveData()
            val buildQuery = GetAllWishListGroupQuery
                    .builder()
            wishListGroupResult.value = dataManager.listOfWishListGroup(buildQuery, 10)
            wishListGroupList = Transformations.switchMap(wishListGroupResult) {
                it.pagedList
            }
        }
        return wishListGroupList
    }

    fun wishListGroupRefresh() {
        wishListGroupResult.value?.refresh?.invoke()
    }

    fun wishListGroupRetry() {
        wishListGroupResult.value?.retry?.invoke()
    }

    val wishListResult = MutableLiveData<Listing<GetWishListGroupQuery.WishList>>()
    lateinit var wishListList: LiveData<PagedList<GetWishListGroupQuery.WishList>>
    val wishListNetworkState = Transformations.switchMap(wishListResult) { it.networkState }!!
    val wishListRefreshState = Transformations.switchMap(wishListResult) { it.refreshState }!!

    fun loadwishList(id: Int) : LiveData<PagedList<GetWishListGroupQuery.WishList>> {
        if (!::wishListList.isInitialized) {
            wishListList = MutableLiveData()
            val buildQuery = GetWishListGroupQuery
                    .builder()
                    .id(id)

            wishListResult.value = dataManager.listOfWishList(buildQuery, 10)
            wishListList = Transformations.switchMap(wishListResult) {
                it.pagedList
            }
        }
        return wishListList
    }

    fun wishListRefresh() {
        wishListResult.value?.refresh?.invoke()
    }

    fun wishListRetry() {
        wishListResult.value?.retry?.invoke()
    }

    val currentPage = MutableLiveData<Int>().apply { value = 1 }
    val searchPageResult12 = MutableLiveData<ArrayList<SearchListing>>().apply { value = ArrayList() }

    fun increaseCurrentPage(page: Int) {
        currentPage.value = page
    }

    fun getSavedDetails() : Single<Response<GetWishListGroupQuery.Data>> {
        val buildQuery = GetWishListGroupQuery
                .builder()
                .id(listId.value!!)
                .currentPage(currentPage.value)
                .build()
        return dataManager.getWishList(buildQuery)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun setSavedData(results: List<GetWishListGroupQuery.WishList>) {
        val list = searchPageResult12.value
        list?.addAll(parseData(results))
        searchPageResult12.value = list
    }

    private fun parseData(results: List<GetWishListGroupQuery.WishList>): List<SearchListing> {
        val list = ArrayList<SearchListing>()
        results.forEach {
            it.listData()?.let {
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
                        isListOwner = false,
                        currency = it.listingData()?.currency()!!,
                        coverPhoto = it.coverPhoto(),
                        bookingType = it.bookingType()!!,
                        beds = it.beds()!!,
                        basePrice = it.listingData()?.basePrice()!!,
                        listPhotos = photoList,
                        lat = 0.0,
                        lng = 0.0
                ))
            }
        }
        return list
    }
}