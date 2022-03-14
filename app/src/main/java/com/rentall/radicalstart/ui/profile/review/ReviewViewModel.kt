package com.rentall.radicalstart.ui.profile.review

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.ui.profile.edit_profile.EditProfileNavigator
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class ReviewViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<ReviewNavigator>(dataManager, resourceProvider) {


    var aboutYouResult = MutableLiveData<Listing<GetUserReviewsQuery.Result>>()
    var byYouResult = MutableLiveData<Listing<GetUserReviewsQuery.Result>>()
    var pendingResult= MutableLiveData<Listing<GetPendingUserReviewsQuery.Result>>()
    lateinit var aboutYouList: LiveData<PagedList<GetUserReviewsQuery.Result>>
    lateinit var byYouList: LiveData<PagedList<GetUserReviewsQuery.Result>>
    lateinit var pendingList: LiveData<PagedList<GetPendingUserReviewsQuery.Result>>
    val reviewDesc = ObservableField("")
    val networkStateAboutYou: LiveData<NetworkState> = Transformations.switchMap(aboutYouResult) { it.networkState }
    val netWorkStateByYou : LiveData<NetworkState> = Transformations.switchMap(byYouResult){it.networkState}
    val networkStatePending : LiveData<NetworkState> = Transformations.switchMap(pendingResult){it.networkState}
    var pendingReviewResult= MutableLiveData<GetPendingUserReviewQuery.Result>()
    var userRating = ObservableField(0.toFloat())
    val reloadData = MutableLiveData<String>()
    lateinit var retry: () -> Unit
    init {
        reloadData.value = ""
    }

    fun loadAboutYouList(): LiveData<PagedList<GetUserReviewsQuery.Result>> {
        if (!::aboutYouList.isInitialized) {
            aboutYouList = MutableLiveData()
            val buildQuery = GetUserReviewsQuery
                    .builder()
                    .ownerType("other")
            aboutYouResult.value = dataManager.getUserReviews(buildQuery, 10)
            aboutYouList = Transformations.switchMap(aboutYouResult) {
                it.pagedList
            }
        }
        return aboutYouList
    }

    fun loadByYouList(): LiveData<PagedList<GetUserReviewsQuery.Result>> {
       if(!::byYouList.isInitialized){
           byYouList = MutableLiveData()
           val buildQuery = GetUserReviewsQuery
                   .builder()
                   .ownerType("me")
           byYouResult.value = dataManager.getUserReviews(buildQuery,10)
           byYouList = Transformations.switchMap(byYouResult){
               it.pagedList
           }
       }
        return byYouList
    }

    fun loadByYouListPending(): LiveData<PagedList<GetPendingUserReviewsQuery.Result>>{
        if(!::pendingList.isInitialized){
            pendingList = MutableLiveData()
            val buildQuery= GetPendingUserReviewsQuery
                    .builder()
            pendingResult.value = dataManager.getPendingUserReviews(buildQuery,10)
            pendingList = Transformations.switchMap(pendingResult){
                it.pagedList
            }
        }
        return pendingList
    }


    fun getPendingUserReview(reservationId: Int){
        val buildQuery = GetPendingUserReviewQuery
                .builder()
                .reservationId(reservationId)
                .build()

        compositeDisposable.add(dataManager.getPendingUserReview(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe{setIsLoading(true)}
                .doFinally{setIsLoading(false)}
                .subscribe({response->
                    try{
                        if(response.data()?.pendingUserReview?.status()==200){
                            val result = response.data()?.pendingUserReview?.result()
                            if(result?.listData()!=null){
                                pendingReviewResult.value = result
                            }else{
                                navigator.show404Page()
                            }
                        }else{
                            response.data()?.pendingUserReview?.errorMessage().let {
                                navigator.showToast(it ?: resourceProvider.getString(R.string.something_went_wrong))
                            }
                            navigator.show404Page()
                        }
                    }catch (e: Exception){
                         navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                    }
                },
                {
                    handleException(it)
                }))

    }

    fun writeUserReview(listId: Int,receiverId: String,reservationId: Int){
        val buildQuery= WriteUserReviewMutation
                .builder()
                .listId(listId)
                .receiverId(receiverId)
                .rating(userRating.get()?.toDouble()!!)
                .reservationId(reservationId)
                .reviewContent(reviewDesc.get()?.trim()?.replace("\\s+".toRegex(), " ")!!)
                .build()
        compositeDisposable.add(dataManager.writeReview(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe{setIsLoading(true)}
                .doFinally{setIsLoading(false)}
                .subscribe({response->
                    try{
                        if(response.data()?.writeUserReview()?.status()==200){
                            navigator.moveToScreen(ViewScreen.GO_BACK_AND_REFRESH)
                        }else{
                            response.data()?.writeUserReview()?.errorMessage().let {
                                navigator.showToast(it ?: resourceProvider.getString(R.string.something_went_wrong))
                            }
                        }
                    }catch (e: Exception){
                        handleException(e)
                    }
                },{
                    handleException(it)
                }))
    }

    enum class ViewScreen {
        GO_BACK_TO,
        GO_BACK_AND_REFRESH
    }

    fun getByYouInitialized(): Boolean{
        return ::byYouList.isInitialized
    }

    fun getPendingInitialized(): Boolean{
        return ::pendingList.isInitialized
    }

    fun onRefreshByYou(){
        byYouResult.value?.refresh?.invoke()
    }

    fun onRefreshPending(){
        pendingResult.value?.refresh?.invoke()
    }

}