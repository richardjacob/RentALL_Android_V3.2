package com.rentall.radicalstart.ui.user_profile

import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.rentall.radicalstart.CreateReportUserMutation
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ShowUserProfileQuery
import com.rentall.radicalstart.UserReviewsQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager, resourceProvider) {

    val profileID = MutableLiveData<Int>()
    val userProfile = MutableLiveData<ShowUserProfileQuery.Results>()
    val selectContent = ObservableField<String>()
    val repoResult = MutableLiveData<Listing<UserReviewsQuery.Result>>()
    val posts: LiveData<PagedList<UserReviewsQuery.Result>> = Transformations.switchMap(repoResult) {
        it.pagedList
    }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(repoResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(repoResult) { it.refreshState }

    init {
        selectContent.set("")
    }

    fun reviewRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun setValuesFromIntent(intent: Intent) {
        try {
            val profileId = intent.extras!!.getInt("profileId")
            profileID.value = profileId
            getUserProfile()
        } catch (e: KotlinNullPointerException) {
            navigator.showError()
        }
    }

    fun getUserProfile() {
        val request = ShowUserProfileQuery
                .builder()
                .profileId(profileID.value)
                .isUser(false)
                .build()

        compositeDisposable.add(dataManager.getUserProfile(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                if (it.data()?.showUserProfile()?.status() == 200) {
                                    userProfile.value = it.data()?.showUserProfile()?.results()
                                } else if (it.data()?.showUserProfile()?.status() == 500) {
                                    navigator.openSessionExpire()
                                } else {
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

    fun reportUser() {
        val request = CreateReportUserMutation
                .builder()
                .profileId(profileID.value)
                .reporterId(dataManager.currentUserId)
                .reportType(selectContent.get())
                .build()

        compositeDisposable.add(dataManager.createReportUser(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                if (it.data()?.createReportUser()?.status() == 200) {
                                    navigator.showToast(resourceProvider.getString(R.string.reported_successfully))
                                    (navigator as UserProfileNavigator).closeScreen()
                                } else if (it.data()?.createReportUser()?.status() == 500) {
                                    navigator.openSessionExpire()
                                } else {
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
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

    fun getReview() {
        val query = UserReviewsQuery
                .builder()
                .ownerType("others")
                .profileId(profileID.value)
        repoResult.value = dataManager.listOfUserReview(query, 10)
    }
}