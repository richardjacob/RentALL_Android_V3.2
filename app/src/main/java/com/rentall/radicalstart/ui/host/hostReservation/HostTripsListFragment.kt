package com.rentall.radicalstart.ui.host.hostReservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.FragmentTripsListBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.cancellation.CancellationActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail.HostNewInboxMsgActivity
import com.rentall.radicalstart.ui.host.hostReservation.hostContactUs.HostContactSupport
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.reservation.ReservationActivity
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.vo.InboxMsgInitData
import com.rentall.radicalstart.vo.ListingInitData
import timber.log.Timber
import javax.inject.Inject


private const val ARG_PARAM = "param1"

class HostTripsListFragment : BaseFragment<FragmentTripsListBinding, HostTripsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentTripsListBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_trips_list
    override val viewModel: HostTripsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(HostTripsViewModel::class.java)
    private lateinit var pagingController: HostTripsListController
    private var param1: String? = null

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
                HostTripsListFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM, param1)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initController()
        viewModel.let {
            if (param1 == "upcoming") {
                subscribeToLiveData1()
            } else {
                subscribeToLiveData()
            }
        }
      /*  mBinding.srlTrip.setOnRefreshListener {
            if (param1 == "upcoming") {
                viewModel.upcomingTripRefresh()
            } else {
                viewModel.tripRefresh()
            }
        }*/
        mBinding.btnExplore.onClick {
            (baseActivity as HostHomeActivity).viewDataBinding?.let {
                it.vpHome.setCurrentItem(0, false)
                it.bnExplore.setCurrentItem(0, false)
            }
        }
    }

    private fun initController() {
        try {
            pagingController = HostTripsListController(
                    viewModel.getCurrencyBase(),
                    viewModel.getCurrencyRates(),
                    viewModel.getUserCurrency()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refresh() {
        if (::mViewModelFactory.isInitialized) {
            /*if (mBinding.srlTrip.isRefreshing.not()) {
                if (param1 == "upcoming") {
                    viewModel.upcomingTripRefresh()
                } else {
                    viewModel.tripRefresh()
                }
            }*/
            if(mBinding.rvTripsList.adapter !=null){
                mBinding.rvTripsList.gone()
                mBinding.ltLoadingView.visible()
            }
            if (param1 == "upcoming") {
                viewModel.upcomingTripRefresh()
            } else {
                viewModel.tripRefresh()
            }
        }
    }

    override fun onRetry() {
        if (::mViewModelFactory.isInitialized) {
            if (param1 == "upcoming") {
                viewModel.upcomingTripRetry()
            } else {
                viewModel.tripRetry()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadTrips(param1!!).observe(viewLifecycleOwner, Observer<PagedList<GetAllReservationQuery.Result>> { pagedList ->
            pagedList?.let {
                if (mBinding.rvTripsList.adapter == pagingController.adapter) {
                    pagingController.submitList(it)
                } else {
                    mBinding.rvTripsList.adapter = pagingController.adapter
                    pagingController.submitList(it)
                }
            }
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.ltLoadingView.visibility = View.GONE
                        mBinding.rvTripsList.gone()
                        mBinding.llNoResult.visible()
                        if (param1 == "upcoming") {
                            mBinding.tvStartPlanning.text = resources.getString(R.string.you_don_t_have_any_upcoming_reservations)
                        }else {
                            mBinding.tvStartPlanning.text = resources.getString(R.string.you_don_t_have_any_previous_reservations)
                            mBinding.btnExplore.gone()
                            mBinding.tvNoReservation.gone()
                        }
                    }
                    NetworkState.EXPIRED -> {
                        openSessionExpire()
                    }
                    NetworkState.LOADING -> {
                        mBinding.ltLoadingView.visibility = View.VISIBLE
                        mBinding.llNoResult.gone()
                        pagingController.isLoading = true
                    }
                    NetworkState.LOADED -> {
                        mBinding.ltLoadingView.visibility = View.GONE
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            viewModel.handleException(it.msg!!)
                        }
                    }
                }
            }
        })

        /*viewModel.refreshState.observe(this, Observer {
            mBinding.srlTrip.isRefreshing = it == NetworkState.LOADING
        })*/
    }

    private fun subscribeToLiveData1() {
        param1?.let {
            viewModel.loadUpcomingTrips(it).observe(viewLifecycleOwner, Observer<PagedList<GetAllReservationQuery.Result>> { pagedList ->
                pagedList?.let {
                    if (isDetached.not()) {
                        if (mBinding.rvTripsList.adapter == pagingController.adapter) {
                            pagingController.submitList(it)
                        } else {
                            mBinding.rvTripsList.adapter = pagingController.adapter
                            pagingController.submitList(it)
                        }
                    }
                }
            })
        }

        /*if (param1 == "upcoming") {
            mBinding.tvStartPlanning.setText("You don't have any upcoming reservations !")
        }else{
            mBinding.tvStartPlanning.setText("You don't have any previous reservations !")
        }*/

        viewModel.upcomingNetworkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.ltLoadingView.gone()
                        mBinding.rvTripsList.gone()
                        mBinding.llNoResult.visible()
                        if (param1 == "upcoming") {
                            mBinding.tvStartPlanning.text =  resources.getString(R.string.you_don_t_have_any_upcoming_reservations)
                        }else{
                            mBinding.tvStartPlanning.text = resources.getString(R.string.you_don_t_have_any_previous_reservations)
                        }
                        mBinding.btnExplore.gone()
                        mBinding.tvNoReservation.gone()
                    }
                    NetworkState.LOADING -> {
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                       // mBinding.rvTripsList.gone()
                        mBinding.ltLoadingView.visible()
                    }
                    NetworkState.LOADED -> {
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                        mBinding.rvTripsList.visible()
                        mBinding.ltLoadingView.gone()
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            it.msg?.let { thr ->
                                viewModel.handleException(thr)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })

      /*  viewModel.networkState.observe(this, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.ltLoadingView.visibility = View.GONE
                        mBinding.rvTripsList.gone()
                        mBinding.llNoResult.visible()
                        if (param1 == "upcoming") {
                            mBinding.tvStartPlanning.setText("You don't have any upcoming reservations!")
                        }else{
                            mBinding.tvStartPlanning.setText("You don't have any previous reservations!")
                        }
                        mBinding.btnExplore.gone()
                        mBinding.tvNoReservation.gone()
                    }
                    NetworkState.LOADING -> {
                        pagingController.isLoading = true
                        mBinding.ltLoadingView.visibility = View.VISIBLE
                        mBinding.llNoResult.gone()
                        mBinding.rvTripsList.gone()
                    }
                    NetworkState.LOADED -> {
                        mBinding.ltLoadingView.visibility = View.GONE
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                        mBinding.rvTripsList.visible()
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            mBinding.ltLoadingView.visibility = View.GONE
                            viewModel.handleException(it.msg!!)
                        }
                    }
                }
            }
        })
*/

        /* viewModel.upcomingRefreshState.observe(this, Observer {
             mBinding.srlTrip.isRefreshing = it == NetworkState.LOADING
         })*/
    }

    override fun onDestroyView() {
        mBinding.rvTripsList.adapter = null
        super.onDestroyView()
    }

    fun currencyConverter(base: String, rate: String, userCurrency: String, currency: String, total: Double): String {
        return BindingAdapters.getCurrencySymbol(userCurrency) + Utils.formatDecimal(CurrencyUtil.getRate(
                base = base,
                to = userCurrency,
                from = currency,
                rateStr = rate,
                amount = total
        ))
    }

    inner class HostTripsListController(val base: String, val rate: String, val userCurrency: String) : PagedListEpoxyController<GetAllReservationQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(currentPosition: Int, item: GetAllReservationQuery.Result?): EpoxyModel<*> {

            try {
                var cancelVisibility = false
                var approveVisibility = false
                var declineVisibility = false
                var messageVisibility = false
                var supportVisibility = false
                var receiptVisibility = true

                if (item?.reservationState() != null && item.listData() != null) {
                    if (item?.reservationState()!! == "pending" && item.listData()!!.bookingType() == "request") {
                        cancelVisibility = false
                        approveVisibility = true
                        declineVisibility = true
                        receiptVisibility = false
                    }
                    if (item?.listData() != null && (item.reservationState()!! == "approved")) {
                        cancelVisibility = true
                        approveVisibility = false
                        declineVisibility = false
                    }
                    if (item.reservationState()!! == "declined" || item.reservationState()!! == "cancelled") {
                        approveVisibility = false
                        declineVisibility = false
                        cancelVisibility = false
                        receiptVisibility = false
                    }
                }

                var title: String? = null
                var namePrice: String? = null
                var street: String? = null
                var address: String? = null

                if (item != null) {
                    if (item.listData() != null) {
                        if(item.listData()!=null&&item.listData()!!.title()!=null){
                            title = item.listData()?.title()!!.trim().replace("\\s+", " ")
                        }
                        try {
                            namePrice = item.guestData()?.displayName() + " - " + currencyConverter(base, rate, userCurrency, item.currency()!!, item.total()!!.toDouble() - item.hostServiceFee()!!.toDouble())
                        } catch (E: Exception) {
                            E.printStackTrace()
                        }
                        street = item.listData()?.street()
                        address = item.listData()?.city() + ", " +
                                item.listData()?.state() + ", " +
                                item.listData()?.country() + " " +
                                item.listData()?.zipcode()
                        messageVisibility = true
                    } else {
                        supportVisibility = true
                        receiptVisibility = false
                        approveVisibility = false
                        declineVisibility = false
                    }
                }

                var date = ""
                try {
                date = Utils.epochToDate(item!!.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!) + " - " + Utils.epochToDate(item.checkOut()!!.toLong(), Utils.getCurrentLocale(requireContext())!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                var status = ""
                try {
                    status = item!!.reservationState()!!.capitalize()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return ViewholderHostTripsListBindingModel_()
                        .id("viewholder- $currentPosition")
                        .status(status)
                        .title(title)
                        .date(date)
                        .namePrice(namePrice)
                        .street(street)
                        .address(address)
                        .cancelVisibility(cancelVisibility)
                        .messageVisibility(messageVisibility)
                        .approveVisibility(approveVisibility)
                        .declineVisibility(declineVisibility)
                        .supportVisibility(supportVisibility)
                        .receiptVisibility(receiptVisibility)
                        .cancelClick(View.OnClickListener {
                            try {
                            CancellationActivity.openCancellationActivity(baseActivity!!, item!!?.id()!! ,item.guestData()?.profileId()!!, "host")
                            } catch (e: KotlinNullPointerException) {
                                e.printStackTrace()
                            }
                        })
                        .messageClick(View.OnClickListener {
                            try {
                                HostNewInboxMsgActivity.openInboxMsgDetailsActivity(baseActivity!!, InboxMsgInitData(
                                        threadId = item!!.messageData()?.id()!!,
                                        guestId = item.guestData()?.userId()!!,
                                        guestName = item.guestData()?.displayName()!!,
                                        guestPicture = item.guestData()?.picture(),
                                        hostId = item.hostData()?.userId()!!,
                                        hostName = item.hostData()?.displayName()!!,
                                        hostPicture = item.hostData()?.picture(),
                                        senderID = item.guestData()?.profileId()!!,
                                        receiverID = item.hostData()?.profileId()!!,
                                        listID = item.listId()
                                        )
                                )
                            } catch (e: KotlinNullPointerException) {
                                e.printStackTrace()
                                showError()
                            }
                        })
                        .receiptClick(View.OnClickListener {
                            try {
                                val intent = Intent(context, ReservationActivity::class.java)
                                intent.putExtra("type", 2)
                                intent.putExtra("reservationId", item!!.id())
                                intent.putExtra("userType","host")
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        })
                        .approveClick(View.OnClickListener {
                        viewModel.retryCalled="update-${item!!.messageData()!!.id()}-${Utils.epochToDate(item!!.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!)}-${Utils.epochToDate(item!!.checkOut()!!.toLong(), Utils.getCurrentLocale(requireContext())!!)}-${item.guests()}-${item!!.id()}-approved"
                        viewModel.approveReservation(item!!.messageData()!!.id()!!, "", "approved", Utils.epochToDate(item!!.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!), Utils.epochToDate(item!!.checkOut()!!.toLong(), Utils.getCurrentLocale(requireContext())!!), item.guests()!!, item!!.id()!!.toInt(), "approved")
                            requestModelBuild()
                        })
                        .declineClick(View.OnClickListener {
                        viewModel.retryCalled="update-${item!!.messageData()!!.id()}-${Utils.epochToDate(item!!.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!)}-${Utils.epochToDate(item!!.checkOut()!!.toLong(), Utils.getCurrentLocale(requireContext())!!)}-${item.guests()}-${item!!.id()}-declined"
                        viewModel.approveReservation(item!!.messageData()!!.id()!!, "", "declined", Utils.epochToDate(item!!.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!), Utils.epochToDate(item!!.checkIn()!!.toLong(), Utils.getCurrentLocale(requireContext())!!), item.guests()!!, item!!.id()!!.toInt(), "declined")
                            requestModelBuild()
                        })
                        .supportClick(View.OnClickListener {
                            try {
                                HostContactSupport.newInstance(item!!.id()!!, item.listId()!!).show(childFragmentManager)
                            } catch (e: KotlinNullPointerException) {
                                e.printStackTrace()
                            }
                        })
                        .onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it){
                                if (supportVisibility.not()) {
                                    try {
                                        val currency = BindingAdapters.getCurrencySymbol(userCurrency) + CurrencyUtil.getRate(
                                                base = base,
                                                to = userCurrency,
                                                from = item!!.listData()!!.listingData()?.currency()!!,
                                                rateStr = rate,
                                                amount = item.listData()!!.listingData()?.basePrice()!!.toDouble()
                                        ).toString()
                                        val photo = ArrayList<String>()
                                        photo.add(item.listData()!!.listPhotoName()!!)
                                        val listingInitData = ListingInitData(
                                                item.listData()!!.title()!!,
                                                photo,
                                                item.listData()!!.id()!!,
                                                item.listData()!!.roomType()!!,
                                                item.listData()!!.reviewsStarRating(),
                                                item.listData()!!.reviewsCount(),
                                                currency,
                                                0,
                                                selectedCurrency = userCurrency,
                                                currencyBase = base,
                                                currencyRate = rate,
                                                startDate = "0",
                                                endDate = "0",
                                                bookingType = item.listData()!!.bookingType()!!
                                        )
                                        val intent = Intent(context, ListingDetails::class.java)
                                        intent.putExtra("listingInitData", listingInitData)
                                        baseActivity?.startActivityForResult(intent, 5)
                                    } catch (e: KotlinNullPointerException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        })
            } catch (e: Exception) {
                Timber.e(e,"CRASH")
            }
            return ViewholderHostTripsListBindingModel_()
        }

        override fun addModels(models: List<EpoxyModel<*>>) {
           /* if (mBinding.llNoResult.visibility == View.VISIBLE){
                viewholderListingDetailsSectionHeader {
                    id("header")
                    if (param1 == "upcoming"){
                        header("Upcoming Reservations")
                    }else{
                        header("Previous Reservations")
                    }
                }
            }*/
            try {
                super.addModels(models)
            } catch (e: Exception) {
                Timber.e(e,"CRASH")
            }
        }

        init {
            isDebugLoggingEnabled = true
        }

        override fun onExceptionSwallowed(exception: RuntimeException) {
            throw exception
        }

    }
}