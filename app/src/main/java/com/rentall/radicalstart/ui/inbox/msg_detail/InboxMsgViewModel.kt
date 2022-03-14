package com.rentall.radicalstart.ui.inbox.msg_detail

import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.apollographql.apollo.api.Response
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.ui.inbox.InboxNavigator
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.InboxMsgInitData
import com.rentall.radicalstart.vo.PreApproved
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InboxMsgViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<InboxNavigator>(dataManager,resourceProvider) {

    private val repoResult = MutableLiveData<Listing<GetThreadsQuery.ThreadItem>>()
    val inboxInitData = MutableLiveData<InboxMsgInitData>()
    val posts: LiveData<PagedList<GetThreadsQuery.ThreadItem>> = Transformations.switchMap(repoResult) {
        it.pagedList
    }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(repoResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(repoResult) { it.refreshState }
    val msg = ObservableField("")
    val isNewMessage = MutableLiveData<Boolean>()
    val isRetry = ObservableField(-1)

    var retryCalled = ""

    var timerValue = ObservableField("")

    var preApprovalVisible = ObservableField(false)

    var preApproved = MutableLiveData<PreApproved>()

    var isProfilePic = false
    var approved = false

    private lateinit var disposable: Disposable

    var listingDetails : ViewListingDetailsQuery.Results? = null

    var billingCalculation : GetBillingCalculationQuery.Result? = null
    val lottieProgress = ObservableField<InboxMsgViewModel.LottieProgress>(InboxMsgViewModel.LottieProgress.LOADING)
    val isBook = ObservableField<Boolean>(false)
    var loginStatus = 3

    init {
        loginStatus = dataManager.currentUserLoggedInMode
        preApproved.value = PreApproved(
                0,
                "",
                false,
                0,
                "",
                "",
                0
        )
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    fun notificationRefresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun notificationRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun setInitialData(intent: Intent) {
        try {
            val initData = intent.extras?.getParcelable<InboxMsgInitData>("inboxInitData") as InboxMsgInitData
            inboxInitData.value = initData
        } catch (e :KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun getInboxMsg() {
        val buildQuery = GetThreadsQuery
                .builder()
                .threadId(getThreadId())
                .threadType("Guest")
        repoResult.value = dataManager.listOfInboxMsg(buildQuery, 10)
    }

    fun getInboxMsg1(page: Int): Single<Response<GetThreadsQuery.Data>> {
        isRetry.set(-1)
        val buildQuery = GetThreadsQuery
                .builder()
                .threadId(getThreadId())
                .threadType("Guest")
                .currentPage(page)
                .build()
        return dataManager.listOfInboxMsg1(buildQuery)
    }

    private fun getThreadId(): Int {
        return inboxInitData.value?.threadId ?: 0
    }

    fun readMessage() {
        val mutate = ReadMessageMutation
                .builder()
                .threadId(getThreadId())
                .build()
        compositeDisposable.add(dataManager.setReadMessage(mutate)
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            try {
                                it.data()?.let { response->
                                    if (response.readMessage()?.status() == 200) { }
                                    else if(response.readMessage()?.status() == 500) {
                                        navigator.openSessionExpire()
                                    }
                                } //?: navigator.showError()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        {
                           // navigator.showError()
                        }
                ))
    }

    fun sendMsg() {
            isRetry.set(1)

            val mutate = SendMessageMutation
                    .builder()
                    .threadId(inboxInitData.value!!.threadId)
                    .content(msg.get()!!.trim())
                    .type("message")
                    .build()
            compositeDisposable.add(dataManager.sendMessage(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe(
                            {
                                try {
                                    it.data()?.let { response ->
                                        if (response.sendMessage()?.status() == 200) {
                                            msg.set("")
                                            navigator.addMessage(response.sendMessage()!!.results()!!)
                                        } else if (response.sendMessage()?.status() == 500) {
                                            navigator.openSessionExpire()
                                        }
                                    } ?: navigator.showError()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    navigator.showError()
                                }
                            },
                            {
                                isRetry.set(1)
                                handleException(it)
                            }
                    ))
    }

    fun newMsg() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }

        val buildQuery = GetUnReadThreadCountQuery
                .builder()
                .threadId(getThreadId())
                .build()
        disposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
                .switchMap { dataManager.getNewMessage(buildQuery).onErrorResumeNext { _: Throwable -> Observable.empty() }}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    try {
                        val result = it.data()?.unReadThreadCount
                        if (result!!.status() == 200) {
                            //isNewMessage.value = result.results()?.isUnReadMessage
                            if (isNewMessage.value != result.results()?.isUnReadMessage) {
                                isNewMessage.value = result.results()?.isUnReadMessage!!
                            }
                        } else if(result.status() == 500) {
                            navigator.openSessionExpire()
                        }
                    } catch (e:  Exception) {
                        e.printStackTrace()
                    }
                }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun checkVerification() {
        isRetry.set(-1)
        isBook.set(true)
        lottieProgress.set(InboxMsgViewModel.LottieProgress.LOADING)
        val buildQuery = GetProfileQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .doFinally{
                    isBook.set(false)
                    lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                }
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.userAccount()
                        if (data?.status() == 200) {
                            val result = data.result()
                            retryCalled = ""
                            if(result!!.verification()!!.isEmailConfirmed!!.not()) {
                                navigator.showSnackbar(
                                        resourceProvider.getString(R.string.verification),
                                        resourceProvider.getString(R.string.email_not_verified), resourceProvider.getString(R.string.dismiss))
                            } else {
                                dataManager.currentUserProfilePicUrl = result.picture()
                                isProfilePic = !result.picture().isNullOrEmpty()
                                getBillingCalculation()
                            }
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            navigator.showSnackbar("Info  ", data!!.errorMessage()!!)
                        }
                    } catch (e: Exception) {
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                    }
                }, {
                    isRetry.set(4)

                    handleException(it)
                } )
        )
    }

    fun getBillingCalculation() {
        try {
            val buildQuery = GetBillingCalculationQuery
                    .builder()
                    .listId(inboxInitData.value!!.listID!!)
                    .startDate(Utils.getBlockedDateFormat(preApproved.value!!.startDate))
                    .endDate(Utils.getBlockedDateFormat(preApproved.value!!.endDate))
                    .guests(preApproved.value!!.personCapacity)
                    .convertCurrency(getUserCurrency())
                    .build()

            compositeDisposable.add(dataManager.getBillingCalculation(buildQuery)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data()?.billingCalculation
                            when {
                                data?.status() == 200 -> {
                                    billingCalculation = data.result()
                                    getListingDetails("billing")
                                }
                                data?.status() == 500 -> {
                                    navigator.openSessionExpire()
                                }
                                else -> {
                                    isBook.set(false)
                                    lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                                    navigator.showSnackbar("Info  ", data!!.errorMessage()!!)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, { handleException(it) } ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getListingDetails(str: String) {
        val buildQuery = ViewListingDetailsQuery
                        .builder()
                        .listId(inboxInitData.value!!.listID!!)
                        .build()

            compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data()?.viewListing()
                            if (data!!.status() == 200) {
                                listingDetails = data.results()
                                if(str.equals("billing")) {
                                    isBook.set(false)
                                    lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                                    navigator.openBillingActivity()
                                }else{
                                    navigator.openListingDetails()
                                }
                            }else if(data?.status() == 500) {
                                navigator.openSessionExpire()
                            }  else {
                                isBook.set(false)
                                lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                                navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, { handleException(it) }
                    )
            )
        }

    fun clearHttp() {
        dataManager.clearHttpCache()
    }
}
