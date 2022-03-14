package com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.GetThreadsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.SendMessageMutation
import com.rentall.radicalstart.databinding.HostActivityInboxMessagesBinding
import com.rentall.radicalstart.ui.auth.AuthActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.cancellation.CancellationActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.ui.inbox.InboxNavigator
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.vo.InboxMsgInitData
import com.rentall.radicalstart.vo.PreApproved
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class HostNewInboxMsgActivity : BaseActivity<HostActivityInboxMessagesBinding, HostInboxMsgViewModel>(), InboxNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostActivityInboxMessagesBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_inbox_messages
    override val viewModel: HostInboxMsgViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(HostInboxMsgViewModel::class.java)

    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private var paginationAdapter: PaginationAdapter? = null
    private var progressBar: ProgressBar? = null
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var layoutManager: LinearLayoutManager? = null
    private var isLoadedAll = false
    private lateinit var disposable: Disposable

    var guestName = ""
    var guestProfileID = 0


    var handler = Handler()
    var runnable: Runnable? = null

    var preAdded: Boolean = false
    var approved: Boolean = false

    var from = ""

    companion object {
        @JvmStatic
        fun openInboxMsgDetailsActivity(activity: Activity, inboxMsgData: InboxMsgInitData) {
            val intent = Intent(activity, HostNewInboxMsgActivity::class.java)
            intent.putExtra("inboxInitData", inboxMsgData)
            activity.startActivityForResult(intent, 53)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        topView = mBinding.rlInboxDetailSendMsg
        try {
            from = intent?.getStringExtra("from")!!
            from?.let {
                if (it == "fcm") {
                    if (viewModel.loginStatus == 0) {
                        startActivity(Intent(this, AuthActivity::class.java))
                        this.finish()
                    }
                }
            }
        }catch(e: Exception){
            from = ""
        }
        viewModel.setInitialData(intent)
        initView()
        subscribeToLiveData()
        setUpLoadMoreListener()
        subscribeForData()
        Timber.d("subscribe1")
        mBinding.tvNewMsgPill.invisible()
        mBinding.tvNewMsgPill.onClick {
            mBinding.tvNewMsgPill.slideDown()
            pageNumber = 1
            compositeDisposable.remove(disposable)
            subscribeForData()
            Timber.d("subscribe2")
            paginationAdapter?.removeItems()
        }
    }





    private fun initView() {

        mBinding.toolbarInbox.ivCameraToolbar.gone()
        mBinding.toolbarInbox.ivNavigateup.onClick {
            if(from != null && from == "fcm"){
                val intent = Intent(this, HostHomeActivity::class.java)
                intent.putExtra("from","fcm")
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_right)
                startActivity(intent)
                finish()
            }else {
                handler.removeCallbacks(runnable ?: Runnable { } )
                finish()
            }
        }
        mBinding.toolbarInbox.tvToolbarHeading.text = resources.getString(R.string.message)
        layoutManager = LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).orientation = RecyclerView.VERTICAL
        mBinding.rvInboxDetails.layoutManager = layoutManager
        (layoutManager as LinearLayoutManager).reverseLayout = true
        mBinding.tvInboxSend.onClick {
            checkNetwork {
                mBinding.tvInboxSend.disable()
            if (!viewModel.msg.get().equals("")) {
                viewModel.sendMsg()
            }
        } }
        mBinding.rvInboxDetails.adapter?.setHasStableIds(true)

    }

    private fun subscribeToLiveData() {
        viewModel.inboxInitData.observe(this, Observer { initData ->
            initData?.let { it ->
                paginationAdapter = PaginationAdapter(it.hostId,
                        it.guestPicture,
                        it.hostPicture,
                        it.receiverID,
                        it.senderID)
                mBinding.rvInboxDetails.adapter = paginationAdapter
            }
        })
        viewModel.isNewMessage.observe(this, Observer {
            it?.let { gotNewMsg ->
                if (gotNewMsg) mBinding.tvNewMsgPill.slideUp()
            }
        })
    }

    private fun readMessage() {
        try {
            /*if(viewModel.posts.value!!.size <= 10) {
                viewModel.readMessage()
                viewModel.newMsg()
            }*/
            viewModel.readMessage()
            viewModel.newMsg()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpLoadMoreListener() {
        mBinding.rvInboxDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView,
                                    dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = layoutManager!!.itemCount
                lastVisibleItem = layoutManager!!.findLastVisibleItemPosition()
                if (!isLoadedAll && !loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    pageNumber++
                    paginator.onNext(pageNumber)
                    loading = true
                }
            }
        })
    }

    private fun subscribeForData() {
        disposable = paginator
                .onBackpressureDrop()
                .doOnNext { page ->
                    loading = true
                    progressBar?.visibility = View.VISIBLE
                }
                .concatMapSingle { page ->
                    viewModel.getInboxMsg1(page)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe {  }
                            .doFinally {  }
                            .doOnSuccess {
                                if (page == 1) {
                                    readMessage()
                                }
                            }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    if (items!!.data()!!.threads!!.status() == 200) {
                        enableSendButton()
                        if (items.data()!!.threads!!.results()!!.threadItems()!!.isNotEmpty()) {
                            if (items.data()!!.threads!!.results()!!.threadItems()!!.size < 10) {
                                isLoadedAll = true
                            }
                            try {
                                guestName = items.data()!!.threads!!.results()!!.guestProfile()!!.displayName()!!
                                guestProfileID = items.data()!!.threads!!.results()!!.guestProfile()!!.profileId()!!
                                getPreApproved(items.data()!!.threads!!.results()!!.threadItems()!!)
                            } catch (e: Exception) {

                            }
                            paginationAdapter!!.addItems(items.data()!!.threads!!.results()!!.threadItems()!!)
                            paginationAdapter!!.notifyDataSetChanged()
                        } else {
                            isLoadedAll = true
                        }
                    } else if (items.data()!!.threads!!.status() == 400) {
                        mBinding.inlError.root.visible()
                    } else if (items.data()!!.threads!!.status() == 500) {
                        openSessionExpire()
                    }
                    loading = false
                }, { t ->
                    if (pageNumber == 1) {
                        viewModel.isRetry.set(2)
                        viewModel.handleException(t)
                    } else {
                        viewModel.handleException(t, true)
                    }
                })
        compositeDisposable.add(disposable)
        paginator.onNext(pageNumber)
    }

    private fun enableSendButton() {
        mBinding.tvInboxSend.isClickable = true
        mBinding.etInput.isClickable = true
        mBinding.tvInboxSend.enable()
        mBinding.etInput.enable()
    }

    fun getPreApproved(threadItems: MutableList<GetThreadsQuery.ThreadItem>) {
        try {
            var list: PreApproved

//            if(threadItems.size > 1) {
            for (i in 0 until threadItems.size) {
                if((threadItems[i].type()!! == "approved" || threadItems[i].type()!! == "preApproved" ||
                                threadItems[i].type()!! == "cancelledByHost" || threadItems[i].type()!! == "cancelledByGuest" ||
                                threadItems[i].type()!! == "completed"|| threadItems[i].type()!! == "intantBooking" || threadItems[i].type()!! == "declined" || threadItems[i].type()!! == "inquiry" || threadItems[i].type()!! == "requestToBook")  && (threadItems[i].type()!! == "message").not()&&pageNumber==1) {
                    var cont = ""
                    var reserId = 0
                    if (threadItems[i].reservationId() != null) {
                        reserId = threadItems[i].reservationId()!!
                    }
                    if (threadItems[i].content() != null) {
                        cont = threadItems[i].content()!!
                    }
                    val start = Utils.getGmtDate(threadItems[i].startDate()!!.toLong())
                    val end = Utils.getGmtDate(threadItems[i].endDate()!!.toLong())
                    viewModel.preApproved.value = PreApproved(
                            threadItems[i].threadId()!!,
                            threadItems[i].type()!!,
                            createdAt = threadItems[i].createdAt()!!.toLong(),
                            endDate = end,
                            startDate = start,
                            personCapacity = threadItems[i].personCapacity()!!.toInt(),
                            content = cont,
                            reservationID = reserId
                    )
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
            if (viewModel.preApproved.value?.id != 0) {
                viewModel.preApprovalVisible.set(true)
                if (viewModel.preApproved.value!!.type.equals("requestToBook")) {
                    val remaining = Utils.getMilliSec(viewModel.preApproved.value!!.createdAt)
                    if (remaining < 1440) {
                        setRequestBook()
                    } else {
                        viewModel.preApprovalVisible.set(false)
                    }
                } else if (viewModel.preApproved.value!!.type.equals("inquiry")) {
                    setUpPreApproval()
                } else if (viewModel.preApproved.value!!.type.equals("preApproved")) {
                    val remaining = Utils.getMilliSec(viewModel.preApprovedTime)
                    setRequestSent()
                } else if (viewModel.preApproved.value!!.type.equals("approved") || viewModel.preApproved.value!!.type.equals("intantBooking")) {
                    setCancelBooking()
                } else if (viewModel.preApproved.value!!.type.equals("cancelledByHost") || viewModel.preApproved.value!!.type.equals("cancelledByGuest")) {
                    bookingCancelled()
                } else if (viewModel.preApproved.value!!.type.equals("declined")) {
                    declinedBooking()
                } else if (viewModel.preApproved.value!!.type.equals("completed")) {
                    completedReserv()
                } else {
                    viewModel.preApprovalVisible.set(false)
                }
            } else {
                viewModel.preApprovalVisible.set(false)
                preAdded = true
            }


    }

    fun setUpPreApproval() {
        with(mBinding) {
            preHeaderText = getString(R.string.pre_approved_header, guestName)
            presubText = getString(R.string.pre_approved_sub_text, guestName)
            presubVisible = false // visible
            declineVisible = false
            prebuttonText = getString(R.string.pre_approval_text)
            preApproval = false // visible
            val remaining = Utils.getMilliSec(viewModel!!.preApproved.value!!.createdAt)
            if (remaining < 1440) {
                var remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)

                viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                runnable = Runnable {
                    kotlin.run {
                        remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)
                        viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                        handler.postDelayed(runnable ?: Runnable { } , 1000)
                    }
                }
                handler.postDelayed(runnable ?: Runnable { } , 1000)
            } else {
                viewModel!!.preApprovalVisible.set(false)
            }

            preApprovalClick = View.OnClickListener {
//                viewModel!!.retryCalled = "preapproval"
                    viewModel!!.sendMsg("preapproval")
            }
        }
    }

    fun setCancelBooking() {
        with(mBinding) {
            preHeaderText = getString(R.string.booking_confirmed_txt)
            prebuttonText = getString(R.string.cancel_reserve)
            declineVisible = false
            viewModel!!.timerValue.set(getString(R.string.host_cancel_text))
            preApprovalClick = View.OnClickListener {
                try {
//                    viewModel!!.retryCalled = "host"
                    CancellationActivity.openCancellationActivity(this@HostNewInboxMsgActivity, viewModel!!.preApproved.value!!.reservationID, guestProfileID, "host")
                    finish()
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setRequestBook() {
        Timber.d("hello prebutton reset")
        with(mBinding) {
            preHeaderText = getString(R.string.request_book_text, guestName)
            presubText = getString(R.string.pre_approved_sub_text, guestName)
            presubVisible = false
            prebuttonText = getString(R.string.approve)
            declineVisible = true
            val remaining = Utils.getMilliSec(viewModel!!.preApproved.value!!.createdAt)
            if (remaining < 1440) {
                var remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)

                viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                runnable = Runnable {
                    kotlin.run {
                        remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)
                        viewModel!!.timerValue.set(getString(R.string.timer_text_before, remain))
                        handler.postDelayed(runnable ?: Runnable { } , 1000)
                    }
                }
                handler.postDelayed(runnable ?: Runnable { } , 1000)
            } else {
                viewModel!!.preApprovalVisible.set(false)
            }

            preApprovalClick = View.OnClickListener {
//                viewModel!!.retryCalled = "approved"

                it.isClickable=false
                viewModel!!.approveReservation(viewModel!!.preApproved.value!!.id,
                        viewModel!!.preApproved.value!!.content, "approved", viewModel!!.preApproved.value!!.startDate,
                        viewModel!!.preApproved.value!!.endDate, viewModel!!.preApproved.value!!.personCapacity,
                        viewModel!!.preApproved.value!!.reservationID, "approved",it)

            }

            declineClick = View.OnClickListener {
//                viewModel!!.retryCalled = "declined"
                it.isClickable=false
                viewModel!!.approveReservation(viewModel!!.preApproved.value!!.id,
                        viewModel!!.preApproved.value!!.content, "declined", viewModel!!.preApproved.value!!.startDate,
                        viewModel!!.preApproved.value!!.endDate, viewModel!!.preApproved.value!!.personCapacity,
                        viewModel!!.preApproved.value!!.reservationID, "declined",it)
            }
        }
    }

    fun setRequestSent() {
        handler.removeCallbacks(runnable ?: Runnable { } )
        with(mBinding) {
            preHeaderText = getString(R.string.request_approved)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.approval_text))
        }
    }

    fun declinedBooking() {
        handler.removeCallbacks(runnable ?: Runnable { } )
        with(mBinding) {
            preHeaderText = getString(R.string.declined_booking)
            preApproval = true
            declineVisible = false
            viewModel!!.timerValue.set(getString(R.string.declined_content))
        }
    }

    fun completedReserv() {
        handler.removeCallbacks(runnable ?: Runnable { } )
        with(mBinding) {
            preHeaderText = getString(R.string.reserv_compelted)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.completed_content))
        }
    }

    fun bookingCancelled() {
        handler.removeCallbacks(runnable ?: Runnable { } )
        with(mBinding) {
            preHeaderText = getString(R.string.booking_request_cancelled)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.cancelled_content, guestName))
        }
    }

    override fun onRetry() {
        try {
            if (viewModel.isRetry.get() == 1) {
                if (!viewModel!!.msg.get().equals("")) {
                    viewModel.sendMsg()
                }
            } else if (viewModel.isRetry.get() == 2) {
                pageNumber = 1
                compositeDisposable.remove(disposable)
                subscribeForData()
                Timber.d("subscribe3")
                paginationAdapter?.removeItems()
            } else if (viewModel.isRetry.get() == 3) {
                CancellationActivity.openCancellationActivity(this, viewModel.preApproved.value!!.reservationID, guestProfileID, "guest")
                finish()
            } else if (viewModel.isRetry.get() == 4) {
                viewModel.approveReservation(viewModel.preApproved.value!!.id,
                        viewModel.preApproved.value!!.content, "approved", viewModel.preApproved.value!!.startDate,
                        viewModel.preApproved.value!!.endDate, viewModel.preApproved.value!!.personCapacity,
                        viewModel.preApproved.value!!.reservationID, "approved")
            } else if (viewModel.isRetry.get() == 5) {
                viewModel.approveReservation(viewModel.preApproved.value!!.id,
                        viewModel.preApproved.value!!.content, "declined", viewModel.preApproved.value!!.startDate,
                        viewModel.preApproved.value!!.endDate, viewModel.preApproved.value!!.personCapacity,
                        viewModel.preApproved.value!!.reservationID, "declined")
            } else if (viewModel.isRetry.get() == 6) {
                    viewModel.sendMsg("preapproval")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

       /* if (viewModel.isRetry.get() == 1) {
            if (viewModel.retryCalled == "") {
               // checkNetwork()
            } else if (viewModel.retryCalled == "preapproval") {
                viewModel.sendMsg("preapproval")
            } else if (viewModel.retryCalled == "host") {
                CancellationActivity.openCancellationActivity(this@HostNewInboxMsgActivity, viewModel.preApproved.value!!.reservationID, guestProfileID, "host")
                finish()
            } else if (viewModel.retryCalled == "approved") {
                viewModel.approveReservation(viewModel.preApproved.value!!.id,
                        viewModel.preApproved.value!!.content, "approved", viewModel.preApproved.value!!.startDate,
                        viewModel.preApproved.value!!.endDate, viewModel.preApproved.value!!.personCapacity,
                        viewModel.preApproved.value!!.reservationID, "approved")
            } else if (viewModel.retryCalled == "declined") {
                viewModel.approveReservation(viewModel.preApproved.value!!.id,
                        viewModel.preApproved.value!!.content, "declined", viewModel.preApproved.value!!.startDate,
                        viewModel.preApproved.value!!.endDate, viewModel.preApproved.value!!.personCapacity,
                        viewModel.preApproved.value!!.reservationID, "declined")
            }*//*else {
            viewModel.sendMsg()
        } else if(viewModel.isRetry.get() == 2) {
            pageNumber = 1
            compositeDisposable.remove(disposable)
            subscribeForData()
            paginationAdapter?.removeItems()
        }*//*
        }*/
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        handler.removeCallbacks(runnable ?: Runnable { } )
        super.onDestroy()
    }

    override fun moveToBackScreen() {

    }

    override fun openBillingActivity() {

    }

    override fun openListingDetails() {

    }

    override fun hideTopView(msg: String) {
        showToast(msg)

//     This code is for update the approved or declined status to the recyclerView pagination adapter
        pageNumber = 1
        compositeDisposable.remove(disposable)
        subscribeForData()
        Timber.d("subscribe2")
        paginationAdapter?.removeItems()


        handler.removeCallbacks(runnable ?: Runnable { } )
        if (msg.equals(getString(R.string.reservation_approved))) {
            setCancelBooking()
        } else {
            declinedBooking()
        }
    }

    override fun addMessage(text: SendMessageMutation.Results) {
        paginationAdapter?.addItem(GetThreadsQuery.ThreadItem(
                text.__typename(),
                text.id(),
                viewModel.inboxInitData.value!!.threadId,
                text.reservationId(),
                text.content(),
                text.sentBy(),
                text.type(),
                text.startDate(),
                text.endDate(),
                text.createdAt(),
                text.personCapacity()
        ))


        if (text.type().equals("preApproved")) {
                 setRequestSent()
       }

        paginationAdapter!!.notifyDataSetChanged()
        mBinding.rvInboxDetails.layoutManager?.scrollToPosition(0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("from on click ","of back button is : "+from)
        if(from != null && from.equals("fcm")){
            val intent = Intent(this, HostHomeActivity::class.java)
            intent.putExtra("from","fcm")
            startActivity(intent)
            finish()
        }
    }
}
