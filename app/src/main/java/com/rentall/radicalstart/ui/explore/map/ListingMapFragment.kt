package com.rentall.radicalstart.ui.explore.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.adapter.ListingAdapter
import com.rentall.radicalstart.databinding.FragmentListingOnMapBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.explore.ExploreFragment
import com.rentall.radicalstart.ui.explore.ExploreViewModel
import com.rentall.radicalstart.ui.explore.filter.FilterFragment
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.saved.SavedBotomSheet
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.vo.Listing
import com.rentall.radicalstart.vo.ListingInitData
import com.rentall.radicalstart.vo.SearchListing
import com.stripe.android.model.BankAccount
import timber.log.Timber
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

class ListingMapFragment : BaseFragment<FragmentListingOnMapBinding, ExploreViewModel>(), OnMapReadyCallback {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_on_map
    override val viewModel: ExploreViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentListingOnMapBinding
    private lateinit var getMap: GoogleMap
    private var carouselHeight: Int = 0
    open var listingList = ArrayList<Listing>()
    var selectedListing: Listing? = null
    private var currentListingPosition: Int = 0
    private var previousMarker: Marker? = null
    private val markerList: ArrayList<Marker> = ArrayList()
    private var scrollPosition: Int? = null

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
                ListingMapFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM1, param1)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            scrollPosition = it.getInt(ARG_PARAM1)
        }
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mBinding = viewDataBinding!!
        mBinding.mapView.onCreate(mapViewBundle)
        mBinding.mapView.getMapAsync(this)
        (baseActivity as HomeActivity).hideBottomNavigation()
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.rlToolbarNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
    }

    private fun subscribeToLiveData() {
       /* viewModel.posts?.observe(this, Observer<PagedList<SearchListingQuery.Result>> { pagedList ->
            pagedList?.let {
                //initListing(it)
            }
        })
*/
        viewModel.searchPageResult12.observe(viewLifecycleOwner, Observer {
            it?.let {
                initListing(it)
            }
        })

        viewModel.filterCount.observe(viewLifecycleOwner, Observer {
            it?.let { filterCount ->
                if (filterCount > 0) {
                    mBinding.tvMapBadge.text = filterCount.toString()
                    mBinding.tvMapBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.curve_button_blue_map)
                    mBinding.tvMapBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    mBinding.ivFilterIcon.gone()
                    mBinding.tvMapBadge.visible()
                } else {
                    mBinding.ivFilterIcon.visible()
                    mBinding.tvMapBadge.gone()
                }
            }
        })
    }

    private fun initListing(it: ArrayList<SearchListing>) {
        try {
            listingList.clear()
/*            if (it.size > 10) {
                for (i in scrollPosition!!..scrollPosition!!.plus(9)) {
                    if (listingList.size == 0) {
                        addListing(it[i], true)
                    } else if ( i < it.size) {
                        addListing(it[i], false)
                    }
                }
                if (listingList.size < 10) {
                    while (listingList.size < 9) {
                        addListing(it[scrollPosition!!.minus(listingList.size)], false)
                    }
                }
                setSimilarListingAdapter()
            } */
       //     else {
                for (i in 0 until it.size) {
                    if (listingList.size == scrollPosition) {
                        addListing(it[i], true)
                    } else {
                        addListing(it[i], false)
                    }
                }

                setSimilarListingAdapter()
                mBinding.rvExploreListingMap.layoutManager?.scrollToPosition(scrollPosition!!)
          //  }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addListing(item: SearchListing, selected: Boolean) {
        try {
            var images = ""
            if (item.coverPhoto != null) {
                for (i in 0 until item.listPhotos.size) {
                    if (item.coverPhoto == item.listPhotos[i].id) {
                        images = (item.listPhotos[i].name)
                        break
                    }
                }
            } else {
                images = item.listPhotos[0].name
            }

            var currency = viewModel.getCurrencySymbol() + viewModel.getConvertedRate(item.currency, item.basePrice).toString()

            val text = currency.split(".")
            if(text[1] == "0"){
                currency = text[0]
            }

            var reviewStarCount: Int? = 0
            var reviewsCount = ""
            item.reviewsStarRating?.let {
                reviewStarCount = it
            }
            item.reviewsCount?.let {
                reviewsCount = it.toString()
            }

            listingList.add(Listing(images,
                    item.roomType!!,
                    item.title,
                    currency,
                    reviewStarCount!!,
                    reviewsCount,
                    "",
                    item.lat,
                    item.lng,
                    selected,
                    id = item.id,
                    isWishList = item.wishListStatus!!,
                    bookingType = item.bookingType,
                    isOwnerList =  item.isListOwner!!,
                    per_night = " " + resources.getString(R.string.per_night)
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val vto = mBinding.rvExploreListingMap.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mBinding.rvExploreListingMap?.let {
                    carouselHeight = mBinding.rvExploreListingMap.height
                    if (::getMap.isInitialized && listingList.size > 0) {
                        if (carouselHeight > 0) {
                            getMap.setPadding(0,0,0, mBinding.rvExploreListingMap.height)
                            getMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(listingList[0].lat, listingList[0].long), 15f))
                            val obs = mBinding.carouselAndCoordinatorContainer.viewTreeObserver
                            obs.removeOnGlobalLayoutListener(this)
                        }
                    }
                }
            }
        })
        mBinding.llMapFilter.onClick {
            ((baseActivity as HomeActivity).pageAdapter.getCurrentFragment() as ExploreFragment).openFragment(FilterFragment(), "filter")
        }
        mBinding.rvExploreListingMap.smoothScrollToPosition(0)
    }

    private fun setSimilarListingAdapter() {
        viewModel.isGoogleLoaded = true
        val snapHelper = PagerSnapHelper()
        if (mBinding.rvExploreListingMap.onFlingListener == null) {
            snapHelper.attachToRecyclerView(mBinding.rvExploreListingMap)
        }
        try{
            val mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            mLayoutManager.isSmoothScrollbarEnabled = true
            with(mBinding.rvExploreListingMap) {
                layoutManager = mLayoutManager
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                adapter = ListingAdapter(listingList,
                        ListingInitData(
                                startDate = viewModel.getStartDate(),
                                endDate = viewModel.getEndDate(),
                                guestCount = viewModel.getPersonCapacity(),
                                selectedCurrency = viewModel.getUserCurrency(),
                                currencyBase = viewModel.getCurrencyBase(),
                                currencyRate = viewModel.getCurrencyRates()

                        ),
                        clickListener = {it ->
                            val bottomSheet = SavedBotomSheet.newInstance(it.id, it.image, false, 0)
                            bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                        }
                )
            }

            ((mBinding.rvExploreListingMap.itemAnimator) as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false

            mBinding.rvExploreListingMap.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                var currentState: Int = 0
                override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE == newState) {
                        val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        if ( position != -1) {
                            if (currentState != position) {
                                changeMarkerOnScrolledListing(position)
                                for(i in 0 until listingList.size) {
                                    listingList[i].selected = i == position
                                }
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                            currentState = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        }
                    }
                }
            })
        }catch (e: Exception){
             e.printStackTrace()
        }

    }

    private fun changeMarkerOnScrolledListing(position: Int) {
        try {
            makePreviousMarkerWhite()
            currentListingPosition = position
            Timber.d("Old LatLng ${selectedListing!!.lat}, ${selectedListing!!.long}")
            selectedListing = listingList[currentListingPosition]
            makeCurrentMarkerGreen(markerList[position])
            previousMarker = markerList[position]
            revealListing()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        getMap = googleMap
        val iconFactory = IconGenerator(context)
        for (i in 0 until listingList.size) {
            if (i == 0) {
                iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                iconFactory.setTextAppearance(R.style.BaseText)
            } else {
                iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.white))
                iconFactory.setTextAppearance(R.style.BaseText1)
            }
            addIcon(iconFactory, listingList[i].price, LatLng(listingList[i].lat, listingList[i].long), i.toString())
        }
        getMap.setOnMapClickListener { hideListing() }
        getMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                hideListing()
            }
        }
        getMap.setOnMarkerClickListener { marker ->
            makePreviousMarkerWhite()
            currentListingPosition = marker.tag.toString().toInt()
            selectedListing = listingList[currentListingPosition]
            makeCurrentMarkerGreen(marker)
            previousMarker = marker
            revealListing()
            false
        }
        if (listingList.size > 0) {
            carouselHeight = mBinding.rvExploreListingMap.height
            if (::getMap.isInitialized && listingList.size > 0) {
                if (carouselHeight > 0) {
                    getMap.setPadding(0,0,0, mBinding.rvExploreListingMap.height)
                    getMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(listingList[0].lat, listingList[0].long), 15f))
                }
            }
        }
    }

    private fun makePreviousMarkerWhite() {
        val iconFactory = IconGenerator(context)
        iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.white))
        iconFactory.setTextAppearance(R.style.BaseText1)
        previousMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(selectedListing?.price)))
        previousMarker?.zIndex = 0.0f
    }

    private fun makeCurrentMarkerGreen(marker: Marker) {
        val iconFactory = IconGenerator(context)
        iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        iconFactory.setTextAppearance(R.style.BaseText)
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(selectedListing?.price)))
        marker.zIndex = 1.0f
    }

    private fun hideListing() {
        getMap.setPadding(0, mBinding.toolbarListingDetails.height, 0, 0)
        mBinding.carouselAndCoordinatorContainer.animate()
                .translationY(mBinding.rvExploreListingMap.height.toFloat()).duration = 300
    }

    private fun revealListing() {
        try {
            Timber.d("New LatLng ${selectedListing!!.lat}, ${selectedListing!!.long}")
            getMap.setPadding(0, mBinding.toolbarListingDetails.height, 0, carouselHeight)
            mBinding.rvExploreListingMap.smoothScrollToPosition(currentListingPosition)
            mBinding.carouselAndCoordinatorContainer.animate()
                    .translationY(0F)
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                        }
                    })
            val cameraUpdate  = CameraUpdateFactory.newLatLngZoom(LatLng(selectedListing!!.lat, selectedListing!!.long), 15f)
            getMap.animateCamera(cameraUpdate)
            Timber.d("New LatLng ${selectedListing!!.lat}, ${selectedListing!!.long}")
        } catch (e: Exception) {
            Timber.d("New LatLng error")
            e.printStackTrace() }
    }

    private fun addIcon(iconFactory: IconGenerator, text: CharSequence, position: LatLng, tag: String) {
        try {
            val markerOptions = MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)))
                    .position(position)
                    .anchor(iconFactory.anchorU, iconFactory.anchorV)
            if (tag == "0") {
                previousMarker = getMap.addMarker(markerOptions)
                previousMarker!!.tag = tag
                selectedListing = listingList[0]
                makeCurrentMarkerGreen(previousMarker!!)
                markerList.add(previousMarker!!)
            } else {
                val marker = getMap.addMarker(markerOptions)
                marker.tag = tag
                markerList.add(marker)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mBinding.mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mBinding.mapView.onResume()
        changeMarkerOnScrolledListing(0)
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView.onStop()
    }

    override fun onPause() {
        mBinding.mapView.onPause()
        super.onPause()
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if(listingList != null) {
//            listingList.clear()
//        }
//    }

    override fun onDestroy() {
        mBinding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.mapView.onLowMemory()
    }

    override fun onRetry() {

    }

}