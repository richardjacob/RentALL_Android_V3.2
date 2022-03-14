package com.rentall.radicalstart.ui.listing

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.Carousel.setDefaultGlobalSnapHelperFactory
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.epoxy.VisibilityState
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.ActivityListingDetailsEpoxyBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.booking.BookingActivity
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.listing.amenities.AmenitiesFragment
import com.rentall.radicalstart.ui.listing.cancellation.CancellationFragment
import com.rentall.radicalstart.ui.listing.contact_host.ContactHostFragment
import com.rentall.radicalstart.ui.listing.desc.DescriptionFragment
import com.rentall.radicalstart.ui.listing.map.MapFragment
import com.rentall.radicalstart.ui.listing.photo_story.PhotoStoryFragment
import com.rentall.radicalstart.ui.listing.pricebreakdown.PriceBreakDownFragment
import com.rentall.radicalstart.ui.listing.review.ReviewFragment
import com.rentall.radicalstart.ui.listing.share.ShareActivity
import com.rentall.radicalstart.ui.saved.SavedBotomSheet
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.Utils.Companion.clickWithDebounce
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.vo.ListingInitData
import com.yongbeom.aircalendar.AirCalendarDatePickerActivity
import com.yongbeom.aircalendar.core.AirCalendarIntent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import org.json.JSONArray
import javax.inject.Inject
import kotlin.math.round

class ListingDetails: BaseActivity<ActivityListingDetailsEpoxyBinding, ListingDetailsViewModel>(), ListingNavigator {

    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_listing_details_epoxy
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: ActivityListingDetailsEpoxyBinding
    private lateinit var listingDetail: ViewListingDetailsQuery.Results
    private lateinit var initialListData: ListingInitData
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory
    private lateinit var epoxyVisibilityTracker: EpoxyVisibilityTracker
    private var mCurrentState = State.IDLE
    private var heartDrawableExpaned: Int = 0
    private var heartDrawableIdle: Int = 0
    private var photoPosition = 0
    private var isDescLineCount = 0
    private lateinit var similarListing : ArrayList<GetSimilarListingQuery.Result>
    val array = ArrayList<String?>()

    enum class State {
        EXPANDED,
        IDLE
    }

    data class PriceBreakDown(var startDate: String, var endDate: String, var guestCount: Int)

    companion object {
        @JvmStatic fun openListDetailsActivity(context: Context, listingInitData: ListingInitData) {
            val intent = Intent(context, ListingDetails::class.java)
            intent.putExtra("listingInitData", listingInitData)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.exit_to_left, R.anim.abc_fade_out)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        topView = mBinding.clListingDetails
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(mBinding.rlListingDetails)
        snapHelperFactory = object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                return PagerSnapHelper()
            }
        }
        mBinding.ivShareListingDetails.onClick {
            if(viewModel.isListingDetailsInitialized()) {
                openShareActivity()
            }
        }
        mBinding.ivNavigateup.onClick { onBackPressed() }

        mBinding.rlCheckAvailability.clickWithDebounce {
            if (listingDetail.userId() != viewModel.getUserId()) {
                if (viewModel.isListingDetailsInitialized()) {
                    openPrice()
                }
            }else{
                Toast.makeText(this, resources.getString(R.string.this_listing_is_not_available_to_book), Toast.LENGTH_LONG).show()
            }
        }

        mBinding.rlListingPricedetails.onClick {
        if (listingDetail.userId() != viewModel.getUserId()) {
            if (viewModel.isListingDetailsInitialized()) {
                openReview()
            }
        }
        }
        mBinding.btnExplore.onClick {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        mBinding.ivItemListingHeart.onClick {
            listingDetail.id()?.let {
                val bottomSheet = SavedBotomSheet.newInstance(it, listingDetail.listPhotoName()!!, false, 0)
                bottomSheet.show(this.supportFragmentManager!!, "bottomSheetFragment")
            }
        }
    }

    private fun openPrice() {
        if (viewModel.priceBreakDown.get().not()) {
            openAvailabilityActivity()
        } else {
            if (listingDetail.userId() != viewModel.getUserId()) {
                if (openAvailabilityStatus()) {
                    openFragment(PriceBreakDownFragment())
                }
            } else {
                Toast.makeText(this, resources.getString(R.string.this_listing_is_not_available_to_book), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openReview() {
        if (viewModel.priceBreakDown.get()) {
            if (openAvailabilityStatus()) {
                openFragment(PriceBreakDownFragment())
            }
        } else {
            initialListData.reviewCount?.let {
                if (it > 0) {
                    openFragment(ReviewFragment.newInstance(initialListData.reviewCount, initialListData.ratingStarCount,0))
                }
            }
        }
    }

    private fun openShareActivity() {
        try {
            ShareActivity.openShareIntent(this, initialListData.id, initialListData.title, viewModel.carouselUrl.value!!)
            overridePendingTransition(R.anim.slide_up, R.anim.no_change)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadInitialValues(intent).observe(this, Observer {
            it?.let { initValues ->
                initialListData = initValues
                setUp()
                if ((initialListData.startDate == "0").not() &&
                        (initialListData.endDate == "0").not()) {
                    viewModel.startDate.value = initialListData.startDate
                    viewModel.endDate.value = initialListData.endDate
                    viewModel.getBillingCalculation()
                }
            }
        })

        viewModel.loadListingDetails().observe(this, Observer { listingDetails ->
            listingDetails?.let {
                try {
                    initialListData = viewModel.initialValue.value!!
                    if(initialListData.photo.size > 0) {
                        it.listPhotos()?.forEachIndexed { _, listPhoto ->
                            if ( listPhoto.name()!! != initialListData.photo[0]) {
                                initialListData.photo.add(listPhoto.name()!!)
                            }
                        }
                    } else {
                        it.listPhotos()?.forEachIndexed { _, listPhoto ->
                            initialListData.photo.add(listPhoto.name()!!)
                        }
                    }

                    initialListData.hostName = it.user()?.profile()?.displayName()!!
                    initialListData.location =  it.city()+", "+ it.state()+", "+ it.country()
                    initialListData.mapImage = generateMapLocation(it.lat()!!, it.lng()!!)
                    initialListData.ownerPhoto = it.user()?.profile()?.picture()

                    if (it.isListOwner!!.not()) {
                        viewModel.isWishList.value = it.wishListStatus()
                    }

                    with(mBinding) {
                        val currency = viewModel?.getCurrencySymbol() + Utils.formatDecimal(viewModel?.getConvertedRate(it.listingData()?.currency()!!, it.listingData()?.basePrice()!!.toDouble())!!)//getCurrency(it.listingData()?.currency()!!, it.listingData()?.basePrice()!!.toDouble())
                        price = currency + " " + getString(R.string.per_night)
                        reviewsCount = it.reviewsCount()
                        reviewsStarRating = it.reviewsStarRating()
                        mBinding.wishListStatus = it.wishListStatus()
                        mBinding.isOwnerList = it.isListOwner
                        mBinding.tvListingPrice.visible()
                        mBinding.tvListingRatingCount.visible()
                        mBinding.rlListingBottom.visible()
                    }
                    listingDetail = it
                    if (it.reviewsCount()!! > 0) {
                        viewModel.getReview()
                    }
                    listingDetail.houseRules()?.forEachIndexed { _, t: ViewListingDetailsQuery.HouseRule? ->
                        array.add(t?.itemName()!!)
                    }
                    mBinding.rlListingDetails.requestModelBuild()
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    showError()
                }
            }
        })

        viewModel.isSimilarListingLoad.observe(this, Observer {
            mBinding.rlListingDetails.requestModelBuild()
        })

        viewModel.similarListing.observe(this, Observer { it -> it?.let {
            similarListing = ArrayList(it)
//            buildModel(it)
            mBinding.rlListingDetails.requestModelBuild()
        } })

        viewModel.posts.observe(this, Observer<PagedList<GetPropertyReviewsQuery.Result>> {
            viewModel.isReviewsLoad.value = true
        })

        viewModel.isWishList.observe(this, Observer {
            it?.let { isWishList ->
                mBinding.wishListStatus = isWishList
                if (isWishList) {
                    mBinding.ivItemListingHeart.visible()
                    heartDrawableExpaned = R.drawable.ic_heart_filled
                    heartDrawableIdle = R.drawable.ic_heart_filled
                    mBinding.ivItemListingHeart.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, R.drawable.ic_heart_filled))
                } else {
                    heartDrawableExpaned = R.drawable.ic_heart_black
                    heartDrawableIdle = R.drawable.ic_heart_white
                    if (mCurrentState == State.EXPANDED) {
                        mBinding.ivItemListingHeart.visible()
                        mBinding.ivItemListingHeart.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, heartDrawableExpaned))
                    } else {
                        mBinding.ivItemListingHeart.visible()
                        mBinding.ivItemListingHeart.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails,  heartDrawableIdle))
                    }
                }
            }
        })

        viewModel.reviewCount.observe(this, Observer {
            mBinding.reviewsCount = it
        })

        viewModel.networkState.observe(this, Observer {
            it?.let { networkState ->
                if (networkState.status == Status.FAILED) {
                    viewModel.isReviewsLoad.value = false
                    it.msg?.let { thr ->
                        viewModel.handleException(thr)
                    } ?: viewModel.handleException(Throwable())
                }
            }
        })
    }

    private fun movePosition(position: Int?) {
        position?.let {
            if (photoPosition != position) {
                photoPosition = position
                val view = ((mBinding.rlListingDetails as RecyclerView).layoutManager as LinearLayoutManager).findViewByPosition(0)
                if (view is RecyclerView) {
                    view.scrollToPosition(photoPosition)
                }
            }
        }
    }

    private fun generateMapLocation(latitude: Double, longitude: Double): String {
        val location = Location("location")
        location.latitude = latitude
        location.longitude = longitude
        return GoogleStaticMapsAPIServices.getStaticMapURL(location, 200)
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment) {
        hideSnackbar()
        topView = null
        mBinding.flListingRoot.fitsSystemWindows = true
        mBinding.appBarLayout.fitsSystemWindows = false
        mBinding.flListingRoot.requestApplyInsets()
        mBinding.appBarLayout.requestApplyInsets()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.decorView.systemUiVisibility
            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.decorView.systemUiVisibility = flags
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flListing.id, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun toggleAnimation(colorFrom: Int, colorTo: Int) {
        val colorFrom1 = ContextCompat.getColor(this@ListingDetails, colorFrom)
        val colorTo1 = ContextCompat.getColor(this@ListingDetails, colorTo)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom1, colorTo1)
        colorAnimation.duration = 200
        colorAnimation.addUpdateListener {
            animator -> mBinding.toolbarListingDetails.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    override fun openBillingActivity(isProfilePresent: Boolean) {
        try {
            viewModel.billingCalculation.value?.let {
                val spPrice = it.specialPricing()
                var priceArray = ArrayList<HashMap<String, String>>()
                spPrice?.forEachIndexed { index, specialPricing ->
                    var temp = HashMap<String,String>()
                    temp.put("blockedDates",specialPricing.blockedDates()!!)
                    temp.put("isSpecialPrice",specialPricing.isSpecialPrice().toString())
                    priceArray.add(temp)
                }
                val jsonVal = JSONArray(priceArray)
                Log.d("printing ", "jSon val == " + jsonVal.toString())
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("lisitingDetails",viewModel.initialValue.value!!)
                intent.putExtra("checkOut", it.checkOut())
                intent.putExtra("checkIn", it.checkIn())
                intent.putExtra("nights", it.nights())
                intent.putExtra("basePrice", it.basePrice())
                intent.putExtra("cleaningPrice", it.cleaningPrice())
                intent.putExtra("guestServiceFee", it.guestServiceFee())
                intent.putExtra("discount", it.discount())
                intent.putExtra("discountLabel", it.discountLabel())
                intent.putExtra("total", it.total())
                intent.putExtra("title", viewModel.initialValue.value?.title)
                intent.putExtra("image", viewModel.initialValue.value?.photo!![0])
                intent.putExtra("houseRules", array)
                intent.putExtra("guest", viewModel.initialValue.value?.guestCount)
                intent.putExtra("cancellation", viewModel.listingDetails.value!!.listingData()?.cancellation()?.policyName())
                intent.putExtra("cancellationContent", viewModel.listingDetails.value!!.listingData()?.cancellation()?.policyContent())
                intent.putExtra("hostServiceFee", it.hostServiceFee()!!)
                intent.putExtra("listId", viewModel.initialValue.value!!.id)
                intent.putExtra("currency", it.currency())
                intent.putExtra("bookingType", viewModel.listingDetails.value!!.bookingType())
                intent.putExtra("isProfilePresent", isProfilePresent)
                intent.putExtra("averagePrice",it.averagePrice()!!)
                intent.putExtra("priceForDays",it.priceForDays()!!)
                intent.putExtra("specialPricing",jsonVal.toString())
                intent.putExtra("isSpecialPriceAssigned",it.isSpecialPriceAssigned())
                startActivityForResult(intent, 39)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun removeSubScreen() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
        }
    }

    private fun setUp() {
        try {
            mBinding.rlListingDetails.withModels {
                if (::initialListData.isInitialized) {
                    // Listing Stories

                    if(initialListData.photo.isNotEmpty()) listingPhotosCarousel {
                        id("carousel")
                        paddingDp(0)
                        setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                                return PagerSnapHelper()
                            }
                        })

                        withModelsFrom(initialListData.photo) {
                            ViewholderListingDetailsCarouselBindingModel_()
                                    .id(it)
                                    .url(it)
                                    .clickListener { _ ->
                                        if (viewModel.isListingDetailsLoad.value!!) {
                                            openFragment(PhotoStoryFragment())
                                        }
                                    }
                                    .onVisibilityChanged { model, _, _, _, _, _ ->
                                        viewModel.setCarouselCurrentPhoto(model.url())
                                    }
                                    .onVisibilityStateChanged { _, _, _ ->

                                    }
                        }
                        onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                            mCurrentState = if (percentVisibleHeight < 35) {
                                if (mCurrentState != State.EXPANDED) {
                                    toggleAnimation(R.color.transparent, R.color.white)
                                    if(::listingDetail.isInitialized) {
                                        if (listingDetail.isListOwner!!.not()) {
                                            mBinding.ivItemListingHeart.visible()
                                            mBinding.ivItemListingHeart.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, heartDrawableExpaned))
                                        }else{
                                            mBinding.ivItemListingHeart.gone()
                                        }
                                    }
                                    mBinding.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, R.drawable.ic_arrow_back_black_24dp))
                                    mBinding.ivShareListingDetails.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, R.drawable.ic_share_black_24dp))
                                    ViewCompat.setElevation(mBinding.appBarLayout, 5F)
                                    mBinding.appBarLayout.setBackgroundColor(ContextCompat.getColor(this@ListingDetails, R.color.white))
                                }
                                State.EXPANDED
                            } else {
                                if (mCurrentState != State.IDLE) {
                                    toggleAnimation(R.color.white, R.color.transparent)
                                    if(::listingDetail.isInitialized) {
                                        if (listingDetail.isListOwner!!.not()) {
                                            mBinding.ivItemListingHeart.visible()
                                            mBinding.ivItemListingHeart.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, heartDrawableIdle))
                                        }else{
                                            mBinding.ivItemListingHeart.gone()
                                        }
                                    }
                                    mBinding.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, R.drawable.ic_arrow_back_white_24dp))
                                    mBinding.ivShareListingDetails.setImageDrawable(ContextCompat.getDrawable(this@ListingDetails, R.drawable.ic_share_white_24dp))
                                    ViewCompat.setElevation(mBinding.appBarLayout, 0F)
                                    mBinding.appBarLayout.setBackgroundColor(ContextCompat.getColor(this@ListingDetails, R.color.transparent))
                                }
                                State.IDLE
                            }
                        }
                        onBind { _, view, _ ->
                            (view as RecyclerView).scrollToPosition(photoPosition)
                        }
                    }

                    // Title / Host
                    if (initialListData.title.isNotEmpty()) viewholderListingDetailsTitle {
                        id("title")
                        type(initialListData.roomType)
                        title(initialListData.title.trim().replace("\\s+", " "))
                        location(initialListData.location)
                        owner(getString(R.string.hosted_by)+" " +initialListData.hostName)
                        url(initialListData.ownerPhoto)
                        bookingType(initialListData.bookingType)
                        onProfileClick(View.OnClickListener {
                            try {
                                Utils.clickWithDebounce(it){
                                    if(::listingDetail.isInitialized){
                                        UserProfileActivity.openProfileActivity(this@ListingDetails, listingDetail.user()?.profile()?.profileId()!!)
                                    }
                                }

                            } catch (e: KotlinNullPointerException) {
                                e.printStackTrace()
                            }
                        })
                    }

                    if (viewModel.isListingDetailsLoad.value!! && ::listingDetail.isInitialized) {

                        // Icons
                        viewholderListingDetailsIcons {
                            id("icon")
                            guestCount(listingDetail.personCapacity()!!.toString()+" "+ resources.getQuantityString(R.plurals.guest_count,listingDetail.personCapacity()!!))
                            val count = listingDetail.bathrooms()!!
                            val remain = Utils.formatDecimal(count)
                            if(count>1.0){
                                when (viewModel.bathroomType) {
                                    "Private Room" -> {
                                        bathCount("$remain " + resources.getQuantityString(R.plurals.private_bath_count,2))
                                    }
                                    "Shared Room" -> {
                                        bathCount("$remain " + resources.getQuantityString(R.plurals.shared_bath_count,2))
                                    }
                                    else -> {
                                        bathCount("$remain " + resources.getQuantityString(R.plurals.bathroom_count,2))
                                    }
                                }
                            }else{
                                when (viewModel.bathroomType) {
                                    "Private Room" -> {
                                        bathCount("$remain " + resources.getQuantityString(R.plurals.private_bath_count,1))
                                    }
                                    "Shared Room" -> {
                                        bathCount("$remain " + resources.getQuantityString(R.plurals.shared_bath_count,1))
                                    }
                                    else -> {
                                        bathCount("$remain " + resources.getQuantityString(R.plurals.bathroom_count,1))
                                    }
                                }
                            }

                            bedroomCount(listingDetail.bedrooms()?.toInt().toString()+" "+resources.getQuantityString(R.plurals.bedroom_count,listingDetail.bedrooms()!!.toFloatOrNull()!!.toInt()))
                            bedCount(listingDetail.beds()!!.toString()+" "+resources.getQuantityString(R.plurals.bed_count,listingDetail.beds()!!))
                        }
                        viewholderListingDetailsHeader {
                            id("max/min")
                            header(resources.getString(R.string.about_list))
                            isBlack(true)
                            large(true)
                        }
                        // Desc
                        viewholderListingDetailsDesc {
                            id("desc")
                            paddingBottom(true)
                            paddingTop(true)
                            desc(listingDetail.description())
                            size(16.toFloat())
                            clickListener(View.OnClickListener { openFragment(DescriptionFragment()) })
                            if (isDescLineCount == 1) {
                                paddingBottom(true)
                            }
                            onBind { _, view, _ ->
                                val textView = view.dataBinding.root.findViewById<TextView>(R.id.tv_descTemp)
                                textView.post {
                                    isDescLineCount = (textView as TextView).lineCount
                                    textView.visibility=View.GONE
                                    requestModelBuild()
                                }
                            }
                        }
                        if (isDescLineCount > 4) {
                            viewholderListingDetailsListShowmore {
                                id("readmoreDesc")
                                paddingTop(true)
                                text(resources.getString(R.string.read_more))
                                clickListener(View.OnClickListener { openFragment(DescriptionFragment()) })
                            }
                        }

                        viewholderDivider {
                            id(1)
                        }

                        // Min / Max nights
                        if(listingDetail.listingData()?.minNight() != 0 || listingDetail.listingData()?.maxNight() != 0) {
                            viewholderListingDetailsHeader {
                                id("max/min")
                                header(resources.getString(R.string.min_max_nights))
                                isBlack(true)
                                large(true)
                            }

                            if(listingDetail.listingData()?.minNight() != 0) viewholderListingDetailsDesc {
                                id("min")
                                paddingBottom(false)
                                paddingTop(true)
                                size(16.toFloat())
                                if (listingDetail.listingData()?.minNight() == 0) {
                                    desc( "1 " + resources.getString(R.string.min) + resources.getQuantityString(R.plurals.night_count, 1))
                                } else {
                                    desc(listingDetail.listingData()?.minNight().toString() + " "+resources.getString(R.string.min) +
                                            resources.getQuantityString(R.plurals.night_count, listingDetail.listingData()?.minNight()!!))
                                }
                            }

                            if(listingDetail.listingData()?.maxNight() != 0) {
                                viewholderListingDetailsDesc {
                                    id("max")
                                    paddingBottom(true)
                                    paddingTop(true)
                                    size(16.toFloat())
                                    if (listingDetail.listingData()?.maxNight() == 0) {
                                        desc( "1 " + resources.getString(R.string.max) + resources.getQuantityString(R.plurals.night_count, 1))
                                    } else {
                                        desc(listingDetail.listingData()?.maxNight().toString() + " "+ resources.getString(R.string.max) +
                                                resources.getQuantityString(R.plurals.night_count, listingDetail.listingData()?.maxNight()!!))
                                    }
                                }
                            }

                            viewholderDividerPadding {
                                id(2)
                            }
                        }

                        // Amenities
                        if (listingDetail.userAmenities()?.isNotEmpty()!!) {

                            viewholderListingDetailsHeader {
                                id("Amenities")
                                header(resources.getString(R.string.amenities))
                                isBlack(true)
                                large(true)
                            }

                            listingDetail.userAmenities()?.forEachIndexed { index, userAmenity ->
                                if (index < 3) {
                                    viewholderListingDetailsSublist {
                                        id(userAmenity.id())
                                        list(userAmenity.itemName())
                                        paddingTop(true)
                                    }
                                }
                            }

                            if (listingDetail.userAmenities()?.size!! > 3) {
                                viewholderListingDetailsListShowmore {
                                    id("readmore")
                                    text(resources.getString(R.string.read_more))
                                    clickListener(View.OnClickListener {
                                        openFragment(AmenitiesFragment.newInstance("Amenities"))
                                    })
                                }
                                viewholderDivider {
                                    id(5)
                                }
                            } else {
                                viewholderDividerPadding {
                                    id(23)
                                }
                            }
                        }

                        // User Space
                        if (listingDetail.userSpaces()?.isNotEmpty()!!) {
                            viewholderListingDetailsHeader {
                                id("User Space")
                                header(resources.getString(R.string.user_space))
                                isBlack(true)
                                large(true)
                            }

                            listingDetail.userSpaces()?.forEachIndexed { index, userSpace ->
                                if (index < 3) {
                                    viewholderListingDetailsSublist {
                                        id(userSpace.id())
                                        list(userSpace.itemName())
                                        paddingTop(true)
                                    }
                                }
                            }

                            if (listingDetail.userSpaces()?.size!! > 3) {
                                viewholderListingDetailsListShowmore {
                                    id("readmore")
                                    text(resources.getString(R.string.read_more))
                                    clickListener(View.OnClickListener {
                                        openFragment(AmenitiesFragment.newInstance("User Space")) })
                                }
                                viewholderDivider {
                                    id(459)
                                }
                            } else {
                                viewholderDividerPadding {
                                    id(2098)
                                }
                            }
                        }

                        // User Safety
                        if (listingDetail.userSafetyAmenities()?.isNotEmpty()!!) {

                            viewholderListingDetailsHeader {
                                id("User Safety")
                                header(resources.getString(R.string.user_safety))
                                isBlack(true)
                                large(true)
                            }

                            listingDetail.userSafetyAmenities()?.forEachIndexed { index, userSafety ->
                                if (index < 3) {
                                    viewholderListingDetailsSublist {
                                        id(userSafety.id())
                                        list(userSafety.itemName())
                                        paddingTop(true)
                                    }
                                }
                            }

                            if (listingDetail.userSafetyAmenities()?.size!! > 3) {
                                viewholderListingDetailsListShowmore {
                                    id("readmore")
                                    text(resources.getString(R.string.read_more))
                                    clickListener(View.OnClickListener {
                                        openFragment(AmenitiesFragment.newInstance("User Safety"))
                                    })
                                }
                            }else {
                                viewholderDividerPadding {
                                    id(2098)
                                }
                            }
                        }

                        // Map Location
                        viewholderListingDetailsMap {
                            id("staticMap")
                            img(initialListData.mapImage)
                            location(listingDetail.city() +", "+ listingDetail.state() +", " + listingDetail.country())
                            clickListener(View.OnClickListener { openFragment(MapFragment()) })
                            onVisibilityStateChanged { _, _, visibilityState ->
                                if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE && viewModel.isSimilarListingLoad.value == null) {
                                    viewModel.getSimilarListing()
                                }
                            }
                        }

                        // Check-in / out time
                        viewholderListingDetailsCheckin {
                            id("Check-in time")
                            isTime(true)
                            rightSide(resources.getString(R.string.check_in_time))
                            clickListener(View.OnClickListener {})
                            if(listingDetail.listingData()?.checkInStart()!! == "Flexible") {
                                leftSide(listingDetail.listingData()?.checkInStart()!!)
                            }else{
                                if (BindingAdapters.timeConverter(listingDetail.listingData()?.checkInStart()!!) == "0AM") {
                                    leftSide("12AM")
                                } else if (BindingAdapters.timeConverter(listingDetail.listingData()?.checkInStart()!!) == "0PM") {
                                    leftSide("12PM")
                                } else {
                                    leftSide(BindingAdapters.timeConverter(listingDetail.listingData()?.checkInStart()!!))
                                }
                            }
//                            leftSide(listingDetail.listingData()?.checkInStart())
                        }

                        viewholderDivider {
                            id(4)
                        }

                        viewholderListingDetailsCheckin {
                            id("Check-out time")
                            isTime(true)
                            rightSide(resources.getString(R.string.check_out_time))
                            clickListener(View.OnClickListener {  })
                            if(listingDetail.listingData()?.checkInEnd()!! == "Flexible"){
                                leftSide(listingDetail.listingData()?.checkInEnd())
                            }else {
                                if (BindingAdapters.timeConverter(listingDetail.listingData()?.checkInEnd()!!) == "0AM") {
                                    leftSide("12AM")
                                } else if (BindingAdapters.timeConverter(listingDetail.listingData()?.checkInEnd()!!) == "0PM") {
                                    leftSide("12PM")
                                } else {
                                    leftSide(BindingAdapters.timeConverter(listingDetail.listingData()?.checkInEnd()!!))
                                }
                            }
//                            leftSide(listingDetail.listingData()?.checkInEnd())
                        }

                        viewholderDivider {
                            id(5)
                        }

                        // Review
                        if ( viewModel.isReviewsLoad.value != null) {
                            if(viewModel.isReviewsLoad.value!! && viewModel.posts.value?.size!! > 0) {
                                /*viewholderListingDetailsHeader {
                                    id("reviewHeading")
                                    header(resources.getString(R.string.review))
                                    isBlack(true)
                                    large(true)
                                }*/
                                    viewholderReviewHeader {
                                        id("reviewHeader")
                                        if(listingDetail.reviewsCount() !=null && listingDetail.reviewsStarRating()!=null){
                                            val roundOff = round(listingDetail.reviewsStarRating()?.toDouble()!!/listingDetail.reviewsCount()?.toDouble()!!)
                                            displayCount(roundOff.toInt())
                                            reviewsCount(listingDetail.reviewsCount())
                                        }
                                        isBlack(true)
                                        large(true)
                                    }
                                    listingSimilarCarousel {
                                    id("SimilarCarousel11")
                                    padding(Carousel.Padding.dp(20, 20, 20, 20, 25))
                                    setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                                    models(mutableListOf<ViewholderListingDetailsReviewListBindingModel_>().apply {
                                        viewModel.posts.value?.forEachIndexed { index, result ->
                                            if(index <10){
                                                val name = if(result.isAdmin == true){
                                                    getString(R.string.verified_by) + " "+ getString(R.string.app_name)
                                                }else{
                                                    result.authorData()?.fragments()?.profileFields()?.firstName() ?: ""
                                                }
                                                add(ViewholderListingDetailsReviewListBindingModel_()
                                                        .id(index)
                                                        .date(result.createdAt())
                                                        .type("aboutYou")
                                                        .reviewsTotal(1)
                                                        .ratingTotal(result.rating())
                                                        .imgUrl(result.authorData()?.fragments()?.profileFields()?.picture() ?: "")
                                                        .profileId(result.authorData()?.fragments()?.profileFields()?.profileId() ?: 0)
                                                        .name(name)
                                                        .onAvatarClick(View.OnClickListener {
                                                            if(result.isAdmin?.not()!!){
                                                                if(result.authorData() !=null && result.authorData()?.fragments()?.profileFields()?.profileId() !=null){
                                                                    UserProfileActivity.openProfileActivity(this@ListingDetails, result.authorData()?.fragments()?.profileFields()?.profileId()!!)
                                                                }
                                                            }
                                                        })
                                                        .isAdmin(result.isAdmin)
                                                        .onBind{_,view,_ ->
                                                            try{
                                                                val textView= view.dataBinding.root.findViewById<TextView>(R.id.tv_reviewContent)
                                                                var spannableString=SpannableString("")
                                                                val clickableSpan = object: ClickableSpan(){
                                                                    override fun onClick(p0: View) {
                                                                        clickWithDebounce(p0) {
                                                                            openFragment(ReviewFragment.newInstance(listingDetail.reviewsCount()!!, listingDetail.reviewsStarRating()!!,index+1))
                                                                        }
                                                                    }
                                                                    override fun updateDrawState(ds: TextPaint) { // override updateDrawState
                                                                        ds.isUnderlineText = false // set to false to remove underline
                                                                        ds.color = ContextCompat.getColor(view.dataBinding.root.context, R.color.colorPrimary)
                                                                    }
                                                                }

                                                                val content = result.reviewContent()?.trim()?.replace("\\s+".toRegex(), " ") ?: ""
                                                                if(content.length<100){
                                                                    spannableString =  SpannableString(content)
                                                                }else{
                                                                    val subStringContent = content.substring(0,100)
                                                                    spannableString =  SpannableString(subStringContent +"..."+getString(R.string.read_more_with_out_dot))
                                                                    spannableString.setSpan(clickableSpan, subStringContent.length+3, subStringContent.length+3+getString(R.string.read_more_with_out_dot).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                                                }
                                                                textView.movementMethod= LinkMovementMethod.getInstance()
                                                                textView.text = spannableString
                                                            }catch (e: Exception){
                                                                e.printStackTrace()
                                                            }
                                                        })
                                            }
                                            }
                                    })
                                }
                                    if ( viewModel.posts.value?.size!! > 1 ) viewholderListingDetailsReadreview {
                                        id("readReview")
                                        reviewsCount(listingDetail.reviewsCount())
                                        clickListener(View.OnClickListener {
                                            clickWithDebounce(it){
                                                openFragment(ReviewFragment.newInstance(listingDetail.reviewsCount()!!, listingDetail.reviewsStarRating()!!,0))
                                            }
                                        })
                                    }


                                /*viewholderListingDetailsReviews {
                                    id("review")
                                    val name = if (viewModel.posts.value!![0]!!.isAdmin!!) {
                                        resources.getString(R.string.verified_by) + " "+  resources.getString(R.string.app_name) //viewModel.getSiteName()
                                    } else {
                                        viewModel.posts.value!![0]!!.authorData()?.firstName() + " " + viewModel.posts.value!![0]!!.authorData()?.lastName()
                                    }
                                    val image = if (viewModel.posts.value!![0]!!.isAdmin!!) {
                                        ""
                                    } else {
                                        viewModel.posts.value!![0]!!.authorData()?.picture()
                                    }
                                    comment(viewModel.posts.value!![0]!!.reviewContent())
                                    date(viewModel.posts.value!![0]!!.createdAt())
                                    imgUrl(image)
                                    isAdmin(viewModel.posts.value!![0]!!.isAdmin!!)
                                    name(name)
                                    padding(true)
                                    onAvatarClick(View.OnClickListener {
                                        clickWithDebounce(it){
                                            if(viewModel.posts.value!![0]!!.authorData()!=null){
                                                UserProfileActivity.openProfileActivity(this@ListingDetails, viewModel.posts.value!![0]!!.authorData()!!.profileId()!!)
                                            }
                                        }
                                    })
                                }*/
                            }
                            viewholderDivider {
                                id(6)
                            }
                        }

                        // House rules
                        if (listingDetail.houseRules()?.size!! > 0) {
                            viewholderListingDetailsCheckin {
                                id("House rules")
                                isTime(false)
                                rightSide(resources.getString(R.string.house_rules))
                                leftSide(resources.getString(R.string.read))
                                clickListener(View.OnClickListener {
                                    clickWithDebounce(it){
                                        openFragment(AmenitiesFragment.newInstance("House rules"))
                                    }
                                     })
                            }

                            viewholderDivider {
                                id(7)
                            }
                        }

                        // Cancellation policy
                        viewholderListingDetailsCheckin {
                            id("cancellation policy")
                            isTime(false)
                            rightSide(resources.getString(R.string.cancellation_policy))
                            leftSide(listingDetail.listingData()?.cancellation()?.policyName())
                            clickListener(View.OnClickListener {
                                clickWithDebounce(it){
                                    openFragment(CancellationFragment())
                                }
                                 })
                        }

                        viewholderDivider {
                            id(8)
                        }

                        // Availability
                        viewholderListingDetailsCheckin {
                            id("availability")
                            isTime(false)
                            rightSide(resources.getString(R.string.availability))
                            leftSide(resources.getString(R.string.check))
                            clickListener(View.OnClickListener {
                                Utils.clickWithDebounce(it){
                                    if (listingDetail.userId() != viewModel.getUserId()) {
                                        openAvailabilityActivity()
                                    }else{
                                        Toast.makeText(this@ListingDetails, resources.getString(R.string.this_listing_is_not_available_to_book), Toast.LENGTH_LONG).show()
                                    }
                                }
                            })
                        }

                        viewholderDivider {
                            id(9)
                        }

                        // Contact Host
                        if(viewModel.listingDetails.value?.userId() != viewModel.dataManager.currentUserId) {
                            viewholderListingDetailsCheckin {
                                id("Contact Host")
                                isTime(false)
                                rightSide(resources.getString(R.string.contact_host))
                                leftSide(resources.getString(R.string.message))
                                clickListener(View.OnClickListener {
                                    clickWithDebounce(it){
                                        openFragment(ContactHostFragment())
                                    }
                                })
                            }
                            viewholderDivider {
                                id(10)
                            }
                        }

                        //Similar Homes
                        if (viewModel.similarListing.value?.isNotEmpty()!!) {
                            viewholderListingDetailsSectionHeader {
                                id("Similar Listing")
                                header(resources.getString(R.string.similar_listing))
                            }
                            listingSimilarCarousel {
                                id("SimilarCarousel11")
                                padding(Carousel.Padding.dp(20, 20, 20, 20, 25))
                                setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                                //models(models)
                                models(mutableListOf<ViewholderListingDetailsSimilarCarouselBindingModel_>().apply {
                                    similarListing.forEachIndexed { index, item ->
                                        val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(item.listingData()!!.currency()!!, item.listingData()!!.basePrice()!!.toDouble()))
                                        add(ViewholderListingDetailsSimilarCarouselBindingModel_()
                                                .id(index)
                                                .title(item.title())
                                                .type(item.roomType())
                                                .price(currency)
                                                .reviewsCount(item.reviewsCount())
                                                .reviewsStarRating(item.reviewsStarRating())
                                                .url(item.listPhotoName())
                                                .bookingType(item.bookingType())
                                                .wishListStatus(item.wishListStatus())
                                                .isOwnerList(item.isListOwner)
                                                .heartClickListener(View.OnClickListener {
                                                    val bottomSheet = SavedBotomSheet.newInstance(item.id()!!, item.listPhotoName()!!, true, 0)
                                                    bottomSheet.show(this@ListingDetails.supportFragmentManager, "bottomSheetFragment")
                                                })
                                                .clickListener(View.OnClickListener {
                                                    Utils.clickWithDebounce(it) {
                                                        val image = ArrayList<String>()
                                                        image.add(item.listPhotoName()!!)
                                                        val listingInitData = ListingInitData(
                                                                item.title()!!,
                                                                image,
                                                                item.id()!!,
                                                                item.roomType()!!,
                                                                item.reviewsStarRating(),
                                                                item.reviewsCount(),
                                                                currency,
                                                                initialListData.guestCount,
                                                                initialListData.startDate,
                                                                initialListData.endDate,
                                                                viewModel.getUserCurrency(),
                                                                viewModel.getCurrencyBase(),
                                                                viewModel.getCurrencyRates(),
                                                                bookingType = item.bookingType()!!,
                                                                minGuestCount = initialListData.minGuestCount,
                                                                maxGuestCount = initialListData.maxGuestCount
                                                        )
                                                        val intent = Intent(this@ListingDetails, ListingDetails::class.java)
                                                        intent.putExtra("listingInitData", listingInitData)
                                                        startActivityForResult(intent, 50)
                                                    }
                                                })
                                        )
                                    }
                                })
                            }
                        } else {
                            if (viewModel.isSimilarListingLoad.value == null || viewModel.isSimilarListingLoad.value == true) {
                                viewholderLoader {
                                    id("viewListingLoading")
                                    isLoading(true)
                                    /*if(initialListData.title.isEmpty()) {
                                        isCenterView(true)
                                    }*/
                                }
                            }
                        }
                    }

                    // Loading
                    if(::listingDetail.isInitialized.not()) {
                        viewholderLoader {
                            id("viewListingLoading")
                            isLoading(true)
                            /*if(initialListData.title.isEmpty()) {
                                isCenterView(true)
                            }*/
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            setWishListIntent()
            movePosition(viewModel.carouselPosition.value)
            topView = null//mBinding.clListingDetails
            mBinding.flListingRoot.fitsSystemWindows = false
            mBinding.appBarLayout.fitsSystemWindows = true
            mBinding.flListingRoot.requestApplyInsets()
            mBinding.appBarLayout.requestApplyInsets()
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = 0
            super.onBackPressed()
        } else if (supportFragmentManager.backStackEntryCount ==  2) {
            setWishListIntent()
            super.onBackPressed()
        } else if (supportFragmentManager.backStackEntryCount ==  0) {
            setWishListIntent()
            topView = mBinding.clListingDetails
            super.onBackPressed()
        } else {
            setWishListIntent()
            finish()
            overridePendingTransition(R.anim.no_change, R.anim.exit_to_right)
        }
    }

    private fun setWishListIntent() {
        if (viewModel.getIsWishListChanged()) {
            val intent = Intent()
            setResult(89, intent)
        }
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    fun openAvailabilityActivity(): Boolean {
        if (::listingDetail.isInitialized) {
            try {
                listingDetail.listingData()?.maxDaysNotice()?.let {
                    return when(it) {
                        "3months" -> { openCalender(3); true }
                        "6months" -> { openCalender(6); true }
                        "9months" -> { openCalender(9); true }
                        "12months" -> { openCalender(12); true }
                        "available" -> { openCalender(-1); true }
                        "unavailable" -> { showToast(resources.getString(R.string.this_listing_is_not_available_to_book)); false  }
                        else -> { false }
                    }
                }
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    fun openAvailabilityStatus(): Boolean {
        if (::listingDetail.isInitialized) {
            try {
                listingDetail.listingData()?.maxDaysNotice()?.let {
                    return when(it) {
                        "3months" -> { true }
                        "6months" -> { true }
                        "9months" -> { true }
                        "12months" -> { true }
                        "available" -> { true }
                        "unavailable" -> { showToast(resources.getString(R.string.this_listing_is_not_available_to_book)); false }
                        else -> { false }
                    }
                }
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    fun openCalender(activeMonths: Int) {
        try {
            val isSelect: Boolean = !(viewModel.getStartDate() == null || viewModel.getEndDate() == null)
            val intent = AirCalendarIntent(this)
            intent.isBooking(isSelect)
            intent.isSelect(isSelect)
            intent.setBookingDateArray(viewModel.blockedDatesArray)
            intent.setStartDate(viewModel.startDate.value)
            intent.setEndDate(viewModel.endDate.value)
            intent.isMonthLabels(false)
            intent.setType(true)
            intent.setMaxBookingDate(listingDetail.listingData()?.maxNight()!!)
            intent.setMinBookingDate(listingDetail.listingData()?.minNight()!!)
            intent.setActiveMonth(activeMonths)
            startActivityForResult(intent, 4)
            overridePendingTransition(R.anim.slide_up, R.anim.no_change)
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 4) {
            if (data != null) {
                val startDateFromResult = data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE).orEmpty()
                val endDateFromResult = data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE).orEmpty()
                if (startDateFromResult.isNotEmpty() && endDateFromResult.isNotEmpty()) {
                    setDateInCalendar(data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE).orEmpty(),
                            data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE).orEmpty())
                } else {
                    mBinding.tvListingCheckAvailability.text = resources.getString(R.string.check_availability)
                }
            }
        } else if (resultCode == 35 ) {
            val intent = Intent()
            setResult(56, intent)
            finish()
        } else if (resultCode == 89 && requestCode == 50) {
            loadSimilarDetails()
        }
    }

    private fun setDateInCalendar(selStartDate: String, selEndDate: String) {
        viewModel.startDate.value = selStartDate
        viewModel.endDate.value = selEndDate
        initialListData.startDate=  viewModel.startDate.value!!
        initialListData.endDate= viewModel.endDate.value!!
        viewModel.dateGuestCount.value = PriceBreakDown(
                selStartDate,
                selEndDate,
                viewModel.initialValue.value!!.guestCount)
        viewModel.getBillingCalculation()
    }

    override fun onRetry() {
        try {
            if (viewModel.isListingDetailsLoad.value != null &&
                    viewModel.isListingDetailsLoad.value!!.not()) {
                viewModel.getListingDetails()
            } else {
                viewModel.loadedApis.value?.let {
                    if(it.contains(1)) {
                        viewModel.getSimilarListing()
                    }
                    if(it.contains(2)) {
                        viewModel.getBillingCalculation()
                    }
                    if(it.contains(3)) {
                        viewModel.checkVerification()
                    }
                    if(it.contains(4)) {
                        viewModel.contactHost()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        epoxyVisibilityTracker.detach(mBinding.rlListingDetails)
        super.onDestroy()
    }

    override fun show404Screen() {
        mBinding.rlRootListing.gone()
        mBinding.flListing.gone()
        mBinding.rlListingDetails.gone()
        mBinding.ll404Page.visible()
    }

    fun loadListingDetails() {
        viewModel.loadListingDetailsWishList()
    }

    fun loadSimilarDetails() {
        viewModel.loadSimilarWishList()
    }
}