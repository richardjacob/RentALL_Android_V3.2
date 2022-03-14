package com.rentall.radicalstart.ui.inbox.msg_detail

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
import com.rentall.radicalstart.ViewListingDetailsQuery
import com.rentall.radicalstart.databinding.ActivityInboxMessagesBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.booking.BookingActivity
import com.rentall.radicalstart.ui.cancellation.CancellationActivity
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.inbox.InboxNavigator
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.vo.InboxMsgInitData
import com.rentall.radicalstart.vo.ListingInitData
import com.rentall.radicalstart.vo.PreApproved
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.Nullable
import org.json.JSONArray
import javax.inject.Inject


class NewInboxMsgActivity : BaseActivity<ActivityInboxMessagesBinding, InboxMsgViewModel>(), InboxNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityInboxMessagesBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_inbox_messages
    override val viewModel: InboxMsgViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(InboxMsgViewModel::class.java)

    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private var paginationAdapter: PaginationAdapter? = null
    private var progressBar: ProgressBar? = null
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem: Int = 0
    private var totalItemCount:Int = 0
    private var layoutManager: LinearLayoutManager? = null
    private var isLoadedAll = false
    private lateinit var disposable: Disposable

    var hostName = ""
    var hostProfileId = 0

    var handler = Handler()
    var runnable: Runnable? = null

    var preAdded : Boolean = false
    var approved: Boolean = false

    var from = ""

    companion object {
        @JvmStatic
        fun openInboxMsgDetailsActivity(activity: Activity, inboxMsgData: InboxMsgInitData) {
            val intent = Intent(activity, NewInboxMsgActivity::class.java)
            intent.putExtra("inboxInitData", inboxMsgData)
            activity.startActivityForResult(intent, 53)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            from = intent?.getStringExtra("from")!!
            from?.let {
                if (it == "fcm") {
                    if(viewModel.loginStatus == 0){
                        openSessionExpire()
                    }
                    viewModel.clearHttp()
                }
            }
        }catch (e: Exception){
            from = ""
        }
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        topView = mBinding.rlInboxDetailSendMsg
        viewModel.setInitialData(intent)
        initView()
        subscribeToLiveData()
        setUpLoadMoreListener()
        subscribeForData()
        mBinding.tvNewMsgPill.invisible()
        mBinding.tvNewMsgPill.onClick {
            mBinding.tvNewMsgPill.slideDown()
            pageNumber = 1
            compositeDisposable.remove(disposable)
            subscribeForData()
            paginationAdapter?.removeItems()
        }
    }

    private fun initView() {
        mBinding.toolbarInbox.ivCameraToolbar.gone()
        mBinding.toolbarInbox.ivNavigateup.onClick {
            if(from != null && from.equals("fcm")){
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("from","fcm")
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_right)
                startActivity(intent)
                finish()
            }else {
                finish()
            }
        }
        mBinding.toolbarInbox.tvToolbarHeading.text = resources.getString(R.string.message)
        layoutManager = LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).orientation = RecyclerView.VERTICAL
        mBinding.rvInboxDetails.layoutManager = layoutManager
        (layoutManager as LinearLayoutManager).reverseLayout = true
        mBinding.tvInboxSend.onClick { checkNetwork {
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
                        it.hostPicture,
                        it.guestPicture,
                        it.senderID,
                        it.receiverID)
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
                            .doOnSubscribe { viewModel.setIsLoading(true) }
                            .doFinally { viewModel.setIsLoading(false) }
                            .doOnSuccess {
                                if (page == 1) {
                                    readMessage()
                                }
                            }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ items ->
                    if (items!!.data()!!.threads!!.status() == 200) {
                        enableSendButton()
                        if (items.data()!!.threads!!.results()!!.threadItems()!!.isNotEmpty()) {
                            if (items.data()!!.threads!!.results()!!.threadItems()!!.size < 10) {
                                isLoadedAll = true
                            }
                            hostName = items.data()!!.threads!!.results()!!.hostProfile()!!.displayName()!!
                            hostProfileId = items.data()!!.threads!!.results()!!.hostProfile()!!.profileId()!!
                            getPreApproved(items.data()!!.threads!!.results()!!.threadItems()!!)
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
                    t.printStackTrace()
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

    fun getPreApproved(threadItems: @Nullable MutableList<GetThreadsQuery.ThreadItem>) {
        try {
            var list : PreApproved
            for(i in 0 until  threadItems.size){
                if((threadItems[i].type()!! == "approved" || threadItems[i].type()!! == "preApproved" ||
                                threadItems[i].type()!! == "cancelledByHost" || threadItems[i].type()!! == "cancelledByGuest" ||
                                threadItems[i].type()!! == "completed"|| threadItems[i].type()!! == "intantBooking" || threadItems[i].type()!! == "declined" || threadItems[i].type()!! == "inquiry" || threadItems[i].type()!! == "requestToBook")  && (threadItems[i].type()!! == "message").not()&&pageNumber==1) {
                    var reserId = 0
                    var cont = ""
                    if (threadItems[i].reservationId() != null) {
                        reserId = threadItems[i].reservationId()!!
                    }
                    if (threadItems[i].content() != null) {
                        cont = threadItems[i].content()!!
                    }
                    viewModel.preApproved.value = PreApproved(
                            threadItems[i].threadId()!!,
                            threadItems[i].type()!!,
                            createdAt = threadItems[i].createdAt()!!.toLong(),
                            startDate = threadItems[i].startDate()!!,
                            endDate = threadItems[i].endDate()!!,
                            personCapacity = threadItems[i].personCapacity()!!.toInt(),
                            reservationID = reserId,
                            content = cont
                    )
                    break
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        if(viewModel.preApproved.value?.id != 0){
            viewModel.preApprovalVisible.set(true)
            if(viewModel.preApproved.value!!.type.equals("approved") || viewModel.preApproved.value!!.type.equals("intantBooking")){
                setCancelBooking()
            }else if(viewModel.preApproved.value!!.type.equals("preApproved")){
                setUpPreApproval()
            }else if(viewModel.preApproved.value!!.type.equals("cancelledByHost") || viewModel.preApproved.value!!.type.equals("cancelledByGuest")){
                bookingCancelled()
                //viewModel.preApprovalVisible.set(false)
            }else if(viewModel.preApproved.value!!.type.equals("completed")){
                completedTrip()
            }else if(viewModel.preApproved.value!!.type.equals("declined")){
                declinedBooking()
            }else if(viewModel.preApproved.value!!.type.equals("inquiry")){
                setRequestBook()
            }else if(viewModel.preApproved.value!!.type.equals("requestToBook")){
                setReqToBook()
            }
            else{
                viewModel.preApprovalVisible.set(false)
            }
        }else{
            preAdded = true
            viewModel.preApprovalVisible.set(false)
        }
    }

    fun bookingCancelled(){
        //handler.removeCallbacks(runnable ?: Runnable { } )
        with(mBinding){
            preHeaderText = getString(R.string.booking_request_cancelled)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.booking_cancelled_by_guest))
        }
    }

    fun completedTrip(){
        with(mBinding){
            preHeaderText = getString(R.string.trip_is_completed)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.your_trip_is_completed))
        }
    }
    fun declinedBooking(){
        handler.removeCallbacks(runnable ?: Runnable { } )
        with(mBinding){
            preHeaderText = getString(R.string.declined_booking)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.declined_guest_content))
        }
    }
    fun setReqToBook(){
        with(mBinding){
            preHeaderText = getString(R.string.request_to_book_txt,hostName)
            preApproval = true
            viewModel!!.timerValue.set(getString(R.string.most_host_respond))
        }
    }
    fun setRequestBook(){
        with(mBinding){
            preHeaderText = getString(R.string.request_to_book_header,hostName)
            presubVisible = true
            prebuttonText = getString(R.string.request_to_book)
            presubText = getString(R.string.respond_text)
            viewModel!!.timerValue.set("")
            bookClick = View.OnClickListener {
                Utils.clickWithDebounce(it){
                    viewModel!!.getListingDetails("List")
                }
            }
        }
    }

    fun setCancelBooking(){
        with(mBinding){
            preHeaderText = getString(R.string.booking_confirmed_txt)
            presubVisible = true
            prebuttonText = getString(R.string.cancel_reserve)
            presubText = getString(R.string.guest_cancel_text)
            viewModel!!.timerValue.set("")
            bookClick = View.OnClickListener {
//                viewModel!!.retryCalled = "guest"
                Utils.clickWithDebounce(it){
                    CancellationActivity.openCancellationActivity(this@NewInboxMsgActivity, viewModel!!.preApproved.value!!.reservationID,hostProfileId, "guest")
                    finish()
                }
            }
        }
    }

    fun setUpPreApproval(){
        with(mBinding){
            preHeaderText = getString(R.string.book_header,hostName)
            presubText = getString(R.string.book_with_in,hostName)
            presubVisible = false
            prebuttonText = getString(R.string.book_button)
            val remaining = Utils.getMilliSec(viewModel!!.preApproved.value!!.createdAt)
            if(remaining < 1440) {
                var remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)

                viewModel!!.timerValue.set(getString(R.string.book_with_in,remain))
                runnable = Runnable {
                    kotlin.run {
                        remain = Utils.difference(viewModel!!.preApproved.value!!.createdAt)
                        viewModel!!.timerValue.set(getString(R.string.book_with_in,remain))
                        handler.postDelayed(runnable ?: Runnable { } , 1000)
                    }
                }
                handler.postDelayed(runnable ?: Runnable { } , 1000)
            }
            else{
                viewModel!!.preApprovalVisible.set(false)
            }

            bookClick = View.OnClickListener {
//                viewModel!!.retryCalled = "check"
                Utils.clickWithDebounce(it){
                    viewModel!!.checkVerification()
                }
            }
        }
    }

    override fun hideTopView(msg: String) {

    }

    override fun onRetry() {
        try {
            if (viewModel.isRetry.get() == 1) {
                if (!viewModel.msg.get().equals("")) {
                    viewModel.sendMsg()
                }
            } else if (viewModel.isRetry.get() == 2) {
                pageNumber = 1
                compositeDisposable.remove(disposable)
                subscribeForData()
                paginationAdapter?.removeItems()
            } else if (viewModel.isRetry.get() == 3) {
                CancellationActivity.openCancellationActivity(this@NewInboxMsgActivity, viewModel.preApproved.value!!.reservationID, hostProfileId, "guest")
                finish()
            } else if (viewModel.isRetry.get() == 4) {
                viewModel.checkVerification()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun moveToBackScreen() {

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
        paginationAdapter!!.notifyDataSetChanged()
        mBinding.rvInboxDetails.layoutManager?.scrollToPosition(0)
    }



    override fun openBillingActivity() {
        try {

            val array = ArrayList<String>()
            viewModel.listingDetails?.let {
                val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(it.listingData()?.currency()!!, it.listingData()?.basePrice()!!))
                val photo = ArrayList<String>()
                photo.add(it.listPhotoName()!!)
                it.houseRules()?.forEachIndexed { _, t: ViewListingDetailsQuery.HouseRule? ->
                    array.add(t?.itemName()!!)
                }
                var reviewStart = 0
                if(it.reviewsStarRating() != null){
                    reviewStart = it.reviewsStarRating()!!
                }
                val listInitData = ListingInitData(
                        it.title()!!, photo , it.id()!!, it.roomType()!!,
                        reviewStart, it.reviewsCount()!!, currency,
                        viewModel.preApproved.value!!.personCapacity,
                        selectedCurrency = viewModel.getUserCurrency(),
                        currencyBase = viewModel.getCurrencyBase(),
                        currencyRate = viewModel.getCurrencyRates(),
                        startDate = viewModel.preApproved.value!!.startDate,
                        endDate = viewModel.preApproved.value!!.endDate,
                        bookingType = it.bookingType()!!,
                        minGuestCount = 0,
                        maxGuestCount = viewModel.preApproved.value!!.personCapacity,
                        isWishList = it.wishListStatus()!!
                )
                val spPrice = viewModel.billingCalculation!!.specialPricing()
                var priceArray = ArrayList<HashMap<String, String>>()
                spPrice?.forEachIndexed { index, specialPricing ->
                    var temp = HashMap<String,String>()
                    temp.put("blockedDates",specialPricing.blockedDates()!!)
                    temp.put("isSpecialPrice",specialPricing.isSpecialPrice().toString())
                    priceArray.add(temp)
                }
                val jsonVal = JSONArray(priceArray)

                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("lisitingDetails",listInitData)
                intent.putExtra("checkOut", Utils.getBlockedDateFormat(viewModel.preApproved.value!!.endDate))
                intent.putExtra("checkIn", Utils.getBlockedDateFormat(viewModel.preApproved.value!!.startDate))
                intent.putExtra("nights", viewModel.billingCalculation!!.nights())
                intent.putExtra("basePrice", viewModel.billingCalculation?.basePrice())
                intent.putExtra("cleaningPrice",viewModel.billingCalculation?.cleaningPrice()!!)
                intent.putExtra("guestServiceFee", viewModel.billingCalculation!!.guestServiceFee())
                intent.putExtra("discount", viewModel.billingCalculation!!.discount())
                intent.putExtra("discountLabel", viewModel.billingCalculation!!.discountLabel())
                intent.putExtra("total", viewModel.billingCalculation!!.total())
                intent.putExtra("title", it.title()!!)
                intent.putExtra("image", photo[0])
                intent.putExtra("houseRules", array)
                intent.putExtra("guest", viewModel.preApproved.value!!.personCapacity)
                intent.putExtra("cancellation", it.listingData()!!.cancellation()!!.policyName())
                intent.putExtra("cancellationContent", it.listingData()!!.cancellation()!!.policyContent())
                intent.putExtra("hostServiceFee", viewModel.billingCalculation!!.hostServiceFee())
                intent.putExtra("listId",listInitData.id)
                intent.putExtra("currency", viewModel.billingCalculation!!.currency())
                intent.putExtra("bookingType", "instant")
                intent.putExtra("isProfilePresent", viewModel.isProfilePic)
                intent.putExtra("averagePrice",viewModel.billingCalculation!!.averagePrice()!!)
                intent.putExtra("priceForDays",viewModel.billingCalculation!!.priceForDays()!!)
                intent.putExtra("specialPricing",jsonVal.toString())
                intent.putExtra("isSpecialPriceAssigned",viewModel.billingCalculation!!.isSpecialPriceAssigned())
                startActivityForResult(intent, 39)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun openListingDetails() {
        val array = ArrayList<String>()
        viewModel.listingDetails?.let {
            val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(it.listingData()?.currency()!!, it.listingData()?.basePrice()!!))
            val photo = ArrayList<String>()
            photo.add(it.listPhotoName()!!)
            it.houseRules()?.forEachIndexed { _, t: ViewListingDetailsQuery.HouseRule? ->
                array.add(t?.itemName()!!)
            }
            var reviewStart = 0
            if (it.reviewsStarRating() != null) {
                reviewStart = it.reviewsStarRating()!!
            }
            val listInitData = ListingInitData(
                    it.title()!!, photo, it.id()!!, it.roomType()!!,
                    reviewStart, it.reviewsCount()!!, currency,
                    0,
                    selectedCurrency = viewModel.getUserCurrency(),
                    currencyBase = viewModel.getCurrencyBase(),
                    currencyRate = viewModel.getCurrencyRates(),
                    startDate = "0",
                    endDate = "0",
                    bookingType = it.bookingType()!!,
                    minGuestCount = 0,
                    maxGuestCount = viewModel.preApproved.value!!.personCapacity,
                    isWishList = it.wishListStatus()!!
            )

            ListingDetails.openListDetailsActivity(this@NewInboxMsgActivity,listInitData)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("from on click ","of back button is : "+from)
        if(from != null && from.equals("fcm")){
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("from","fcm")
            startActivity(intent)
            finish()
        }

    }
}
