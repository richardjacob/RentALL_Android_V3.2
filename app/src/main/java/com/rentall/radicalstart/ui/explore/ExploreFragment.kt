package com.rentall.radicalstart.ui.explore

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentExplore1Binding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.explore.filter.FilterFragment
import com.rentall.radicalstart.ui.explore.guest.GuestFragment
import com.rentall.radicalstart.ui.explore.map.ListingMapFragment
import com.rentall.radicalstart.ui.explore.search.SearchLocationFragment
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.saved.SavedBotomSheet
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.Utils.Companion.getCurrentLocale
import com.rentall.radicalstart.util.Utils.Companion.getMonth
import com.rentall.radicalstart.vo.DefaultListing
import com.rentall.radicalstart.vo.ListingInitData
import com.rentall.radicalstart.vo.SearchListing
import com.yongbeom.aircalendar.AirCalendarDatePickerActivity
import com.yongbeom.aircalendar.core.AirCalendarIntent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class ExploreFragment : BaseFragment<FragmentExplore1Binding, ExploreViewModel>(), ExploreNavigator {

    private var isFromLocationSearch: Boolean = false
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_explore1
    override val viewModel: ExploreViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentExplore1Binding
    private var recommend = ArrayList<DefaultListing>()
    private var mostViewed = ArrayList<DefaultListing>()
    private lateinit var pagingController1: SearchListingController1
    private var toLoadInRecommend = false
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this

        subscribeToLiveData()
        initView()
        viewModel.getexploreLists()
    }

    private fun initView() {
        val  currentLocale : Locale = Utils.getCurrentLocale(requireContext())!!
        mBinding.locale = currentLocale
        snapHelperFactory = object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                return CustomSnapHelper()
            }
        }
        onClickListeners()
    }

    private fun onClickListeners() {
        mBinding.searchIconIv.onClick {
            Utils.clickWithDebounce(mBinding.searchIconIv){
                if (viewModel.searchResult.value == true) {
                    reset()
                } else {
                    (baseActivity as HomeActivity).hideBottomNavigation()
                    openFragment(SearchLocationFragment(), "search")
                }
            }
        }
        mBinding.tvDates.onClick {
            Utils.clickWithDebounce(mBinding.tvDates){
                openCalender()
            }
        }
        mBinding.tvGuestPlacholderTitle.onClick {
            (baseActivity as HomeActivity).hideBottomNavigation()
            openFragment(GuestFragment(), "guest")
        }
        mBinding.searchTv.onClick {
            Utils.clickWithDebounce(mBinding.searchTv){
                (baseActivity as HomeActivity).hideBottomNavigation()
                openFragment(SearchLocationFragment(), "search")
            }
        }
        mBinding.tvFilter.onClick {
            Utils.clickWithDebounce(mBinding.tvFilter){
                (baseActivity as HomeActivity).hideBottomNavigation()
                openFragment(FilterFragment(), "filter")
            }
        }
        mBinding.ibExploreLocation.onClick {
            viewModel.searchPageResult12.value?.let {
                if (it.isNotEmpty()) {
                    val scrollPosition = 0
                    openFragment(ListingMapFragment.newInstance(scrollPosition), "map")
                }
            }
        }
    }

    override fun disableIcons() {
        with(mBinding) {
            searchIconIv.disable()
            tvDates.disable()
            tvGuestPlacholderTitle.disable()
            tvFilter.disable()
            ibExploreLocation.disable()
            searchTv.disable()
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flExploreFragment.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }
    private fun checkLocationForSearch(location : String) {
             isFromLocationSearch = true
            viewModel.location.value = location
            // showToast("Getting address ${viewModel.location.value}")

    }
    @SuppressLint("WrongConstant")
    private fun setUp() {

        mBinding.rvExploreEpoxy.withModels {
            if (recommend.size > 0 || mostViewed.size > 0) {
                if (recommend.size > 0) {
                    if (viewModel.popularLocations?.isNotEmpty() == true) {

                        viewholderListingHeader {
                            id(23333)
                            text(getString(R.string.popular_location))
                            locale(getCurrentLocale(requireContext()))
                        }

                        listingSimilarCarousel {
                            id((0..999).random())
                            padding(Carousel.Padding.dp(10, 20, 17, 10, 15))
                            Carousel.setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                            //models(models)
                            models(mutableListOf<ViewholderPopularLocationItemBindingModel_>().apply {
                                viewModel.popularLocations?.forEach { poploc ->
                                    if (poploc.isEnable == "true") {
                                        add(ViewholderPopularLocationItemBindingModel_()
                                                .id(poploc.id())
                                                .imgURL(poploc.image())
                                                .title(poploc.location())
                                                .onLocationClick(View.OnClickListener {
                                                    poploc.locationAddress()?.let { it1 -> checkLocationForSearch(it1) }
                                                })
                                        )


                                    }


                                }
                            })
                        }
                    }
                    viewholderListingHeader {
                        id(23)
                        text(getString(R.string.recommed_viewed))
                        locale(getCurrentLocale(requireContext()))
                    }
                    exploreGridCarousel {
                        id("carousel")
                        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                        val currentLan =  preferences.getString("Locale.Helper.Selected.Language", "en").toString()
                        models(mutableListOf<ViewholderListingBindingModel_>().apply {
                            recommend.forEachIndexed { index, item ->
                                val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(item.currency, item.basePrice))
                                Log.d("listID","${item.id } review ${item.reviewsStarRating}")
                                add(ViewholderListingBindingModel_()
                                        .id("mostViewed - ${item.id}")
                                        .title(item.title.trim().replace("\\s+", " "))
                                        .roomType(item.roomType)
                                        .image(item.listPhotoName)
                                        .bookingType(item.bookingType)
                                        .locale(getCurrentLocale(requireContext()))
                                        .reviewsCount(item.reviewsCount)
                                        .reviewsStarRating(item.reviewsStarRating)
                                        .currency("$currency ${resources.getString(R.string.per_night)}")
                                        .wishListStatus(item.wishListStatus)
                                        .isOwnerList(item.isListOwner)
                                        .heartClickListener(View.OnClickListener {
                                            val bottomSheet = SavedBotomSheet.newInstance(item.id, item.listPhotoName!!, false, item.wishListGroupCount)
                                           // bottomSheet.isCancelable = false
                                            bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                                        })
                                        .clickListener(View.OnClickListener {
                                            val photo = ArrayList<String>()
                                            photo.add(item.listPhotoName!!)
                                            ListingDetails.openListDetailsActivity(requireContext(),ListingInitData(
                                                    item.title, photo , item.id, item.roomType!!,
                                                    item.reviewsStarRating, item.reviewsCount, currency,
                                                    0,
                                                    selectedCurrency = viewModel.getUserCurrency(),
                                                    currencyBase = viewModel.getCurrencyBase(),
                                                    currencyRate = viewModel.getCurrencyRates(),
                                                    startDate = viewModel.getStartDate(),
                                                    endDate = viewModel.getEndDate(),
                                                    bookingType = recommend.get(index).bookingType,
                                                    minGuestCount = viewModel.getMinGuestCount(),
                                                    maxGuestCount = viewModel.getMaxGuestCount(),
                                                    isWishList = item.wishListStatus!!
                                            ))
                                        }))
                            }
                        })
                    }
                    /*viewholderListingShowbtn {
                    id("showmore") c
                    flag(true)
                    clickListener(View.OnClickListener { viewModel.recommendListingExpand() })
                }*/
                }
                if (mostViewed.size > 0) {
                    viewholderListingHeader {
                        id(231)
                        text(getString(R.string.most_viewed))
                        locale(getCurrentLocale(requireContext()))
                    }
                    exploreGridCarousel {
                        id("carousel1")
                        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                        val currentLan =  preferences.getString("Locale.Helper.Selected.Language", "en").toString()
                        models(mutableListOf<ViewholderListingBindingModel_>().apply {
                            mostViewed.forEachIndexed { index, item ->
                                val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(item.currency, item.basePrice))
                                add(ViewholderListingBindingModel_()
                                        .id("mostViewed - ${item.id}")
                                        .title(item.title.trim().replace("\\s+", " "))
                                        .roomType(item.roomType)
                                        .image(item.listPhotoName)
                                        .bookingType(mostViewed.get(index).bookingType)
                                        .reviewsCount(item.reviewsCount)
                                        .locale(getCurrentLocale(requireContext()))
                                        .reviewsStarRating(item.reviewsStarRating)
                                        .currency("$currency ${resources.getString(R.string.per_night)}")
                                        .wishListStatus(item.wishListStatus)
                                        .isOwnerList(item.isListOwner)
                                        .heartClickListener(View.OnClickListener {
                                            val bottomSheet = SavedBotomSheet.newInstance(item.id, item.listPhotoName!!, false, item.wishListGroupCount)
                                            bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                                        })
                                        .clickListener(View.OnClickListener {
                                            val photo = ArrayList<String>()
                                            photo.add(item.listPhotoName!!)
                                            ListingDetails.openListDetailsActivity(requireContext(), ListingInitData(
                                                    item.title,
                                                    photo,
                                                    item.id,
                                                    item.roomType!!,
                                                    item.reviewsStarRating,
                                                    item.reviewsCount,
                                                    currency,
                                                    0,
                                                    selectedCurrency = viewModel.getUserCurrency(),
                                                    currencyBase = viewModel.getCurrencyBase(),
                                                    currencyRate = viewModel.getCurrencyRates(),
                                                    startDate = viewModel.getStartDate(),
                                                    endDate = viewModel.getEndDate(),
                                                    bookingType = item.bookingType,
                                                    minGuestCount = viewModel.getMinGuestCount(),
                                                    maxGuestCount = viewModel.getMaxGuestCount(),
                                                    isWishList = item.wishListStatus!!
                                            ))
                                        }))
                            }
                        })
                    }
                }
                /*  viewholderListingShowbtn {
                      id("showmore1")
                      flag(true)
                      clickListener(View.OnClickListener { viewModel.mostViewedListingExpand() })
                  }*/
            } else {
                viewholderExploreNoResult {
                    id(123)
                }
            }
        }


    }

    private fun subscribeToLiveData() {
        viewModel.personCapacity.observe(viewLifecycleOwner, Observer {
            it?.let { personCapacity ->
                if ( personCapacity.isNotEmpty() && personCapacity.toInt() > 0) {
                    val text: String = if (personCapacity.toInt() == 1) {
                        "$personCapacity ${resources.getString(R.string.guest_small)}"
                    } else {
                        "$personCapacity ${resources.getString(R.string.guests_small)}"
                    }
                    setBgnText(mBinding.tvGuestPlacholderTitle, text, false)
                    mBinding.tvFilter.visible()
                } else {
                    setBgnText(mBinding.tvGuestPlacholderTitle, resources.getString(R.string.guest), true)
                    when {
                        mBinding.tvDates.text != resources.getString(R.string.dates) -> mBinding.tvFilter.visible()
                        viewModel.getSearchResult() -> mBinding.tvFilter.visible()
                        else -> mBinding.tvFilter.gone()
                    }
                }
            }
        })

        viewModel.location.observe(viewLifecycleOwner, Observer {
            it?.let { currentLocation ->
                if (currentLocation.isNotEmpty()) {
                    with(mBinding.searchTv) {
                        text = it
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
                    }
                    mBinding.searchIconIv.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_black_18dp))
                    if (isFromLocationSearch.not()) {
                        baseActivity?.onBackPressed()

                    }
                    viewModel.startSearching()
                    isFromLocationSearch = false
                    mBinding.tvFilter.visible()
                } else {
                    with(mBinding.searchTv) {
                        text = resources.getString(R.string.search_box)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.search_text_color))
                    }
                    mBinding.searchIconIv.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_glass))
                }
            }
        })

        viewModel.filterCount.observe(viewLifecycleOwner, Observer {
            it?.let { filterCount ->
                if (filterCount > 0) {
                    val text: String = if (filterCount == 1) {
                        "$filterCount ${resources.getString(R.string.filter_small)}"
                    } else {
                        "$filterCount ${resources.getString(R.string.filters_small)}"
                    }
                    setBgnText(mBinding.tvFilter, text, false)
                } else {
                    setBgnText(mBinding.tvFilter, resources.getString(R.string.filter), true)
                }
            }
        })

        viewModel.defaultListingData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (viewModel.searchResult.value!!.not()) {
                    val recommend1 = it["recommend"]
                    if (recommend1!!.isNotEmpty()) {
                        recommend = recommend1
                    }
                    val mostViewed1 = it["mostViewed"]
                    if (mostViewed1!!.isNotEmpty()) {
                        mostViewed = mostViewed1
                    }
                    mBinding.rvExploreEpoxySearch.gone()
                    mBinding.rvExploreEpoxy.visible()
                    mBinding.flSearchLoading.gone()
                    mBinding.rvExploreEpoxySearch.adapter = null
                    if (viewModel.getSearchResult().not()) {
                        if (mBinding.rvExploreEpoxy.adapter != null) {
                            mBinding.rvExploreEpoxy.requestModelBuild()

                        } else {
                            setUp()


                        }
                    }
                    enableIcons()
                    mBinding.rvExploreEpoxySearch.clearOnScrollListeners()
                    if (::disposable.isInitialized) {
                        compositeDisposable.remove(disposable)
                    }
                }
            }
        })

        viewModel.searchPageResult12.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty() && viewModel.searchResult.value!!) {
                    mBinding.rvExploreEpoxySearch.visible()
                    pagingController1.list = it
                    pagingController1.requestModelBuild()
                    mBinding.flSearchLoading.gone()
                    mBinding.rvExploreEpoxy.gone()
                    mBinding.ibExploreLocation.visible()

                }
            }
        })
    }

    override fun searchForListing() {
        pageNumber = 1
        isLoadedAll = false
        lastVisibleItem = 0
        totalItemCount = 0
        viewModel.searchPageResult12.value = ArrayList()
        initController1()
        setUpLoadMoreListener()
        subscribeForData()
    }


    private fun openListingDetail(item: SearchListing, listingInitData: ListingInitData) {
        try {
            listingInitData.photo.clear()
            listingInitData.title = item.title
            listingInitData.photo.add(item.listPhotoName!!)
            listingInitData.id = item.id
            listingInitData.roomType = item.roomType!!
            listingInitData.ratingStarCount = item.reviewsStarRating
            listingInitData.reviewCount = item.reviewsCount
            listingInitData.bookingType = item.bookingType
            listingInitData.isWishList = item.wishListStatus!!
            ListingDetails.openListDetailsActivity(requireContext(), listingInitData)
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    override fun enableIcons() {
        with(mBinding) {
            tvDates.enable()
            tvGuestPlacholderTitle.enable()
            tvFilter.enable()
            ibExploreLocation.enable()
            searchTv.enable()
            searchIconIv.enable()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 4) {
            if (data != null) {
                val startDateFromResult = data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE).orEmpty()
                val endDateFromResult =  data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE).orEmpty()
                if (startDateFromResult.isNotEmpty() && endDateFromResult.isNotEmpty()) {
                    setDateInCalendar(data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE).orEmpty(),
                            data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE).orEmpty())
                } else {
                    resetDate()
                    viewModel.getSearchListing()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateInCalendar(selStartDate: String, selEndDate: String) {
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
            viewModel.startDate.value = selStartDate
            viewModel.endDate.value = selEndDate
            val startMonthName = getMonth(langType!!,selStartDate)
            val endMonthName = getMonth(langType,selEndDate)
            if (startMonthName == endMonthName) {
                mBinding.tvDates.text = "$startMonthName ${selStartDate.split("-")[2]} - ${selEndDate.split("-")[2]}"
            } else {
                mBinding.tvDates.text = "$startMonthName ${selStartDate.split("-")[2]} - $endMonthName ${selEndDate.split("-")[2]}"
            }
            mBinding.tvDates.background = ContextCompat.getDrawable(requireContext(), R.drawable.curve_button_blue)
            mBinding.tvDates.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            mBinding.tvFilter.visible()
            viewModel.startSearching()
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    private fun resetDate() {
        viewModel.startDate.value = "0"
        viewModel.endDate.value = "0"
        mBinding.tvDates.background = ContextCompat.getDrawable(requireContext(), R.drawable.date_bg)
        mBinding.tvDates.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        mBinding.tvDates.text = resources.getString(R.string.dates)
        when {
            viewModel.getPersonCapacity() > 0 -> mBinding.tvFilter.visible()
            viewModel.getSearchResult() -> mBinding.tvFilter.visible()
            else -> mBinding.tvFilter.gone()
        }
    }

    private fun openCalender() {
        val isSelect: Boolean = !(viewModel.getStartDate() == "0" || viewModel.getEndDate() == "0")
        val intent = AirCalendarIntent(activity)
        intent.isBooking(isSelect)
        intent.isSelect(isSelect)
        intent.setStartDate(viewModel.getStartDate())
        intent.setEndDate(viewModel.getEndDate())
        intent.isMonthLabels(false)
        intent.setType(false)
        startActivityForResult(intent, 4)
        activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_change)
    }

    fun onBackPressed() {
        mBinding.rvExploreEpoxySearch.clear()
        hideSnackbar()
        if (viewModel.getSearchResult()) {
            reset()
            if (toLoadInRecommend) {
                viewModel.getexploreLists()
                toLoadInRecommend = false
            }
        } else {
            baseActivity?.finish()
        }
    }

    private fun reset() {
        mBinding.rvExploreEpoxySearch.clearOnScrollListeners()
        mBinding.rvExploreEpoxySearch.clear()
        viewModel.clearSearchRequest()
        resetDate()
        mBinding.ibExploreLocation.gone()
        mBinding.llNoResult.gone()
        mBinding.rvExploreEpoxy.visible()
        mBinding.appBarLayout.setExpanded(true)
        viewModel.increaseCurrentPage(1)
        isLoadedAll = false
        viewModel.searchPageResult12.value = ArrayList()
        pageNumber = 1
    }

    private fun setBgnText(view: TextView, text: String, status: Boolean) {
        view.text = text
        if (status) {
            view.background = ContextCompat.getDrawable(view.context, R.drawable.date_bg)
            view.setTextColor(ContextCompat.getColor(view.context, R.color.black))
        } else {
            view.background = ContextCompat.getDrawable(view.context, R.drawable.curve_button_blue)
            view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
        }
    }

    override fun onDestroyView() {
        mBinding.rvExploreEpoxy.adapter = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        mBinding.appBarLayout.setExpanded(true)
    }

    override fun onRetry() {
        mBinding.appBarLayout.setExpanded(true)
        if (viewModel.getSearchResult()) {
            subscribeForData()
        } else {
            viewModel.getexploreLists()
        }
    }

    /*override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (::mViewModelFactory.isInitialized) {
                onRefresh()
        }
    }*/

    fun onRefresh() {
        if (viewModel.getSearchResult()) {
            mBinding.ibExploreLocation.gone()
            mBinding.rvExploreEpoxy.gone()
            mBinding.searchLoading.visible()
            viewModel.isRefreshing =true
            searchForListing()
           // viewModel.repoRefresh()
        } else {
            viewModel.getexploreLists()
        }
    }

    fun onRefreshOnWishList(value: Int?, flag: Boolean, count: Int) {
        if (viewModel.getSearchResult()) {
            viewModel.refreshOnWishList(true)
            viewModel.changeWishListStatusInSearch(value, flag, count)
        } else {
            viewModel.changeWishListStatus(value, flag, count)
        }
    }

    fun onRefreshOnError() {
        /*if (viewModel.exploreLists.value == null) {
            viewModel.getexploreLists()
        }*/
    }

    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private var progressBar: ProgressBar? = null
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem: Int = 0
    private var totalItemCount:Int = 0
    private var isLoadedAll = false
    private lateinit var disposable: Disposable

    private fun initController1() {
        pagingController1 = SearchListingController1(ListingInitData(
                selectedCurrency = viewModel.getUserCurrency(),
                currencyBase = viewModel.getCurrencyBase(),
                currencyRate = viewModel.getCurrencyRates(),
                guestCount = viewModel.getPersonCapacity(),
                minGuestCount = viewModel.getMinGuestCount(),
                maxGuestCount = viewModel.getMaxGuestCount()
        ), clickListener = { item, listingInitData ,view->
            Utils.clickWithDebounce(view){
                listingInitData.startDate = viewModel.getStartDate()
                listingInitData.endDate = viewModel.getEndDate()
                openListingDetail(item, listingInitData)
            }
        }, retryListener = {
            viewModel.repoRetry()
        }, onWishListClick = { item, _ ->
            try {
                val bottomSheet = SavedBotomSheet.newInstance(item.id, item.listPhotoName!!,false, item.wishListGroupCount)
                bottomSheet.show(childFragmentManager, "bottomSheetFragment")
            } catch (e: Exception) {
                e.printStackTrace()
                showError()
            }
        })
        val layoutManager = LinearLayoutManager(activity)
        mBinding.rvExploreEpoxySearch.layoutManager = layoutManager
        mBinding.rvExploreEpoxySearch.setController(pagingController1)

        pagingController1.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if(positionStart == 0) {
                    layoutManager.scrollToPosition(0)
                }
            }
        })
    }

    private fun setUpLoadMoreListener() {
        mBinding.rvExploreEpoxySearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView,
                                    dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = mBinding.rvExploreEpoxySearch.layoutManager!!.itemCount
                lastVisibleItem = (mBinding.rvExploreEpoxySearch.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()
                if (!isLoadedAll && !loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    pageNumber++
                    paginator.onNext(pageNumber)
                    loading = true
                }
            }
        })
    }

    private fun subscribeForData() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }
        disposable = paginator
                .onBackpressureDrop()
                .doOnNext { page ->
                    loading = true
                    if (page == 1) {
                        mBinding.flSearchLoading.visible()
                        mBinding.llNoResult.gone()
                    }
                    progressBar?.visibility = View.VISIBLE
                    viewModel.increaseCurrentPage(page)
                    pagingController1.isLoading = true
                }
                .concatMapSingle { page ->
                    viewModel.getSearchListing1()
                          //  .delay(5, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .doOnError { throwable ->
                               // showToast("Something went wrong. Please try again.${throwable.printStackTrace()}")
                            }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ items ->
                    if (viewModel.searchResult.value!!) {
                        if (items!!.data()!!.SearchListing()!!.status() == 200) {
                            if (items.data()!!.SearchListing()!!.results()!!.isNotEmpty()) {
                                if (items.data()!!.SearchListing()!!.results()!!.size < 10) {
                                    isLoadedAll = true
                                }
                                viewModel.setSearchData(items.data()!!.SearchListing()!!.results())
                            } else {
                                isLoadedAll = true
                            }
                            mBinding.ibExploreLocation.visible()
                        }else if(items!!.data()!!.SearchListing()!!.status() == 500) {
                            openSessionExpire()
                        }
                        else {
                            if (pageNumber == 1) {
                                mBinding.llNoResult.visible()
                                mBinding.flSearchLoading.gone()
                                mBinding.rvExploreEpoxy.gone()
                                mBinding.ibExploreLocation.gone()
                            }
                            isLoadedAll = true
//                            ib_explore_location.gone()
                        }
                        loading = false
//                        fl_search_loading.gone()
                        pagingController1.isLoading = false
                    }
                }, { t -> viewModel.handleException(t)  })
        compositeDisposable.add(disposable)
        paginator.onNext(pageNumber)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    fun onRefreshWish() {
        onRefresh()
        toLoadInRecommend = true
    }
}

class CustomSnapHelper : LinearSnapHelper() {
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager is LinearLayoutManager) {
            if (needToDoSnap(layoutManager) == false) {
                return null
            }
        }
        return super.findSnapView(layoutManager)
    }

    fun needToDoSnap(linearLayoutManager: LinearLayoutManager): Boolean {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition() != 0 && linearLayoutManager.findLastCompletelyVisibleItemPosition() != linearLayoutManager.itemCount - 1
    }
}
