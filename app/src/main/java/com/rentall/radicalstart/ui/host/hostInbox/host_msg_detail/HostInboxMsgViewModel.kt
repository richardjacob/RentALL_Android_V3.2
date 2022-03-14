package com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail

import android.content.Intent
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.apollographql.apollo.api.Response
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.ui.inbox.InboxNavigator
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Handler
import javax.inject.Inject

class HostInboxMsgViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<InboxNavigator>(dataManager,resourceProvider) {

    private val repoResult = MutableLiveData<Listing<GetThreadsQuery.ThreadItem>>()
    val inboxInitData = MutableLiveData<InboxMsgInitData>()
    val posts: LiveData<PagedList<GetThreadsQuery.ThreadItem>> = Transformations.switchMap(repoResult) {
        it.pagedList
    }
    val networkState = Transformations.switchMap(repoResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(repoResult) { it.refreshState }!!
    val msg = ObservableField("")
    val isNewMessage = MutableLiveData<Boolean>()
    val isRetry = ObservableField(-1)
    var retryCalled = ""

    var timerValue = ObservableField("")

    var preApprovalVisible = ObservableField(false)

    var preApproved = MutableLiveData<PreApproved>()

    private lateinit var disposable: Disposable

    var inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH)
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

    var isPreApproved = false

    var preApprovedTime : Long = 0

    var approved = false

    var apprvedTime: Long = 0


    fun notificationRefresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun notificationRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun setInitialData(intent: Intent) {
        try {
            val initData = intent.extras?.getParcelable<InboxMsgInitData>("inboxInitData")!! as InboxMsgInitData
            inboxInitData.value = initData
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun getInboxMsg() {
        val buildQuery = GetThreadsQuery
                .builder()
                .threadId(getThreadId())
                .threadType("host")
        repoResult.value = dataManager.listOfInboxMsg(buildQuery, 10)
    }

    fun getInboxMsg1(page: Int): Single<Response<GetThreadsQuery.Data>> {
        isRetry.set(-1)
        val buildQuery = GetThreadsQuery
                .builder()
                .threadId(getThreadId())
                .threadType("host")
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
                                it.data()?.let { response ->
                                    if (response.readMessage()?.status() == 200) {
                                    } else if (response.readMessage()?.status() == 500) {
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

    fun sendMsg(from: String = "") {
            isRetry.set(1)

            var mutate: SendMessageMutation
            if (from.equals("")) {
                mutate = SendMessageMutation
                        .builder()
                        .threadId(inboxInitData.value!!.threadId)
                        .content(msg.get()!!.trim())
                        .type("message")
                        .build()
            } else {
                mutate = SendMessageMutation
                        .builder()
                        .threadId(inboxInitData.value!!.threadId)
                        .content(null)
                        .startDate(preApproved.value!!.startDate)
                        .endDate(preApproved.value!!.endDate)
                        .personCapacity(preApproved.value!!.personCapacity)
                        .type("preApproved")
                        .build()
            }
            compositeDisposable.add(dataManager.sendMessage(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe(
                            {
                                try {
                                    it.data()?.let { response ->
                                        if (response.sendMessage()?.status() == 200) {
                                            msg.set("")
//                                        retryCalled = ""
                                            navigator.addMessage(response.sendMessage()!!.results()!!)
                                            // getInboxMsg()
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
                                if (from == "") {
                                    isRetry.set(1)
                                } else {
                                    isRetry.set(6)
                                }
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
                .switchMap { dataManager.getNewMessage(buildQuery).onErrorResumeNext { _: Throwable -> Observable.empty() } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    try {
                        val result = it.data()?.unReadThreadCount
                        if (result!!.status() == 200) {
                            //isNewMessage.value = result.results()?.isUnReadMessage
                            if (isNewMessage.value != result.results()?.isUnReadMessage) {
                                isNewMessage.value = result.results()?.isUnReadMessage!!
                            }
                        } else if (result.status() == 500) {
                            navigator.openSessionExpire()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun approveReservation(threadId : Int, content: String, type: String, startDate: String, endDate: String, personCapacity: Int, reservationId: Int, actionType: String,view : View?=null){
        isRetry.set(-1)
        val mutate = ReservationStatusMutation
                .builder()
                .threadId(threadId)
                .content(null)
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
                                    if(view!=null){
                                        android.os.Handler().postDelayed( {
                                            view.isClickable=true
                                        },3000)
                                    }
                                    if (response.ReservationStatus()?.status() == 200) {
                                        retryCalled = ""
                                        if (actionType == "approved"){
                                            isRetry.set(4)
                                            navigator.hideTopView(resourceProvider.getString(R.string.reservation_approved))
                                        }else if (actionType == "declined"){
                                            isRetry.set(5)
                                            navigator.hideTopView(resourceProvider.getString(R.string.reservation_declined))
                                        }
                                        //upcomingTripResult.value?.refresh?.invoke()
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
                        {
                            if(view!=null){
                                android.os.Handler().postDelayed( {
                                    view.isClickable=true
                                },3000)
                            }
                            if (actionType == "approved"){
                                isRetry.set(4)
                            }else if (actionType == "declined"){
                                isRetry.set(5)
                            }
                            handleException(it)
                        }
                ))
    }

    fun clearHttp() {
        dataManager.clearHttpCache()
    }
}