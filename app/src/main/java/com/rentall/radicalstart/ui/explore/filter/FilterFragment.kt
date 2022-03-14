package com.rentall.radicalstart.ui.explore.filter

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentListingAmenitiesBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.explore.ExploreFragment
import com.rentall.radicalstart.ui.explore.ExploreViewModel
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class FilterFragment : BaseFragment<FragmentListingAmenitiesBinding, ExploreViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ExploreViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentListingAmenitiesBinding

    private var initialAmenitiesSize = 2
    private var initialHouseRulesSize = 2
    private var initialFacilitiesSize = 2

    private var amenities = HashSet<Int>()
    private var roomType = HashSet<Int>()
    private var spaces = HashSet<Int>()
    private var houseRule = HashSet<Int>()
    private var bookingType = String()
    private var count = 0

    private var minRange = 0
    private var maxRange = 0

    private var currencySymbol = String()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.filterToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.filterToolbar.tvRightside.text = resources.getString(R.string.clear)
        mBinding.filterToolbar.tvRightside.onClick {
            clearSelectedFilters()
            mBinding.rlListingAmenities.requestModelBuild()
        }
        mBinding.filterToolbar.tvRightside.visible()
        mBinding.btnGuestSeeresult.onClick {
            try {
                setViewModelValue()
                var count = (((baseActivity as HomeActivity).pageAdapter.getCurrentFragment()) as ExploreFragment).childFragmentManager.backStackEntryCount
                while (count >= 0 ) {
                    (((baseActivity as HomeActivity).pageAdapter.getCurrentFragment()) as ExploreFragment).childFragmentManager.popBackStack()
                    count--
                }
                (baseActivity as HomeActivity).showBottomNavigation()
                viewModel.startSearching()
            } catch (e: Exception) { e.printStackTrace() }
        }
        getViewModelValue()
    }

    private fun clearSelectedFilters() {
        try {
            amenities = HashSet()
            roomType = HashSet()
            spaces = HashSet()
            houseRule = HashSet()
            bookingType = String()
            count = 0
            minRange = viewModel.minRange.value?.toInt()!!
            maxRange = viewModel.maxRange.value?.toInt()!!
            viewModel.bed1.set("0")
            viewModel.bedrooms1.set("0")
            viewModel.bathrooms1.set("0")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getViewModelValue() {
        try {
            bookingType = viewModel.bookingType.value!!
            roomType = viewModel.roomType.value!!
            amenities = viewModel.amenities.value!!
            spaces =  viewModel.spaces.value!!
            houseRule = viewModel.houseRule.value!!
            viewModel.bed1.set(viewModel.bed.value)
            viewModel.bedrooms1.set(viewModel.bedrooms.value)
            viewModel.bathrooms1.set(viewModel.bathrooms.value)
            minRange = viewModel.minRangeSelected.value?.toInt()!!
            maxRange = viewModel.maxRangeSelected.value?.toInt()!!
            currencySymbol = BindingAdapters.getCurrencySymbol(viewModel.getUserCurrency())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setViewModelValue() {
        try {
            if (bookingType == "instant") {
                count++
            } else {
                if (count != 0) {
                    count--
                }
            }
            count += roomType.size
            count += amenities.size
            count += spaces.size
            count += houseRule.size

            if (viewModel.bed1.get()!!.toInt() > 0) {
                count++
            }
            if (viewModel.bedrooms1.get()!!.toInt() > 0) {
                count++
            }
            if (viewModel.bathrooms1.get()!!.toInt() > 0) {
                count++
            }
            if (minRange != viewModel.minRange.value || maxRange != viewModel.maxRange.value) {
                count++
            }
            viewModel.bookingType.value = bookingType
            viewModel.roomType.value = roomType
            viewModel.amenities.value = amenities
            viewModel.spaces.value = spaces
            viewModel.houseRule.value = houseRule
            viewModel.bed.value = (viewModel.bed1.get())
            viewModel.bedrooms.value = (viewModel.bedrooms1.get())
            viewModel.bathrooms.value = (viewModel.bathrooms1.get())
            viewModel.minRangeSelected.value = minRange
            viewModel.maxRangeSelected.value = maxRange
            viewModel.filterCount.value = count
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.exploreLists1.observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                try {
                    initEpoxy(list.listingSettingsCommon?.results()!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun initEpoxy(it: List<GetExploreListingsQuery.Result3>) {
        try {
            mBinding.rlListingAmenities.withModels {
                viewholderFilterInstantbook {
                    id("instantBook")
                    isChecked(bookingType == "instant")
                    onClick(View.OnClickListener {
                        bookingType = if (bookingType == "instant") {
                            ""
                        } else {
                            "instant"
                        }
                        requestModelBuild()
                    })
                }

                viewholderDivider {
                    id("1")
                }

                viewholderListingDetailsHeader {
                    id("priceheader")
                    header(resources.getString(R.string.price_range))
                    large(false)
                    isBlack(true)
                    typeface(Typeface.DEFAULT_BOLD)
                }

                if (viewModel.maxRange.value!! > viewModel.minRange.value!!) {
                    viewholderFilterPricerange {
                        id("pricerange")
                        price(currencySymbol + minRange.toString() + " - " + currencySymbol + maxRange.toString())
                        onBind { _, view, _ ->
                            with(((view.dataBinding.root).findViewById<RangeSeekBar>(R.id.rangebar_filter_price))) {
                                setRange(viewModel.minRange.value!!.toFloat(), viewModel.maxRange.value!!.toFloat())
                                setValue(minRange.toFloat(), maxRange.toFloat())
                                setOnRangeChangedListener(object : OnRangeChangedListener {
                                    override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                                        // Log.d("onStartTrackingTouch",  isLeft.toString())
                                    }

                                    override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                                        //  Log.d("onRangeChanged", leftValue.toString() + "  " + rightValue.toString() + "  " + isFromUser)
                                        minRange = leftValue.toInt()
                                        maxRange = rightValue.toInt()
                                        mBinding.rlListingAmenities.requestModelBuild()
                                    }

                                    override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                                    }
                                })
                            }
                        }
                        onUnbind { _, view ->
                            with(((view.dataBinding.root).findViewById<RangeSeekBar>(R.id.rangebar_filter_price))) {
                                setOnRangeChangedListener(null)
                            }
                        }
                    }
                }

                viewholderDivider {
                    id("2")
                }

                for (i in 0 until it.size) {
                    if (it[i].id() == 1 && it[i].listSettings()!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("hometype")
                            header(resources.getString(R.string.home_type))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        it[i].listSettings()?.forEachIndexed { index, list ->
                            viewholderFilterCheckbox {
                                id("Home Type ${list.id()}")
                                text(list.itemName())
                                isChecked(roomType.contains(list.id()))
                                onClick(View.OnClickListener {
                                    if (roomType.contains(list.id()!!)) {
                                        roomType.remove(list.id()!!)
                                    } else {
                                        roomType.add(list.id()!!)
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                        viewholderDivider {
                            id("3")
                        }
                        break
                    }
                }

                viewholderListingDetailsHeader {
                    id("Roomsandbeds")
                    header(resources.getString(R.string.Rooms_and_beds))
                    large(false)
                    isBlack(true)
                    typeface(Typeface.DEFAULT_BOLD)
                }
                for (i in 0 until it.size) {
                    if (it[i].id() == 5 && it[i].listSettings()!!.size > 0) {
                        viewholderFilterPlusMinusBedroom {
                            id(it[i].id())
                            text(it[i].typeLabel())
                            plusLimit1(it[i].listSettings()!![0].endValue())
                            minusLimit1(1)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.bedrooms1.set(viewModel.bedrooms1.get()!!.toInt().plus(1).toString())
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.bedrooms1.set(viewModel.bedrooms1.get()!!.toInt().minus(1).toString())
                            })
                        }
                        break
                    }
                }
                for (i in 0 until it.size) {
                    if (it[i].id() == 6 && it[i].listSettings()!!.size > 0) {
                        viewholderFilterPlusMinusBathroom {
                            id(it[i].id())
                            text(it[i].typeLabel())
                            plusLimit1(it[i].listSettings()!![0].endValue())
                            minusLimit1(1)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.bathrooms1.set(viewModel.bathrooms1.get()!!.toInt().plus(1).toString())
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.bathrooms1.set(viewModel.bathrooms1.get()!!.toInt().minus(1).toString())
                            })
                        }
                        break
                    }
                }
                for (i in 0 until it.size) {
                    if (it[i].id() == 8 && it[i].listSettings()!!.size > 0) {
                        viewholderFilterPlusMinus {
                            id(it[i].id())
                            text(it[i].typeLabel())
                            plusLimit1(it[i].listSettings()!![0].endValue())
                            minusLimit1(1)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.bed1.set(viewModel.bed1.get()!!.toInt().plus(1).toString())
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.bed1.set(viewModel.bed1.get()!!.toInt().minus(1).toString())
                            })
                        }
                        break
                    }
                }
                viewholderDivider {
                    id("4")
                }
                it.forEachIndexed { _, item ->
                    if (item.id() == 10 && item.listSettings()!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("initialAmenitiesSize")
                            header(resources.getString(R.string.amenities))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        item.listSettings()!!.subList(0, initialAmenitiesSize).forEachIndexed { index, list ->
                            viewholderFilterCheckbox {
                                id("initialAmenitiesSize ${list.id()}")
                                text(list.itemName())
                                isChecked(amenities.contains(list.id()))
                                onClick(View.OnClickListener {
                                    if (amenities.contains(list.id()!!)) {
                                        amenities.remove(list.id()!!)
                                    } else {
                                        amenities.add(list.id()!!)
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                        if (item.listSettings()!!.size > 3) {
                            viewholderListingDetailsListShowmore {
                                id("readmore amenities")
                                if (initialAmenitiesSize == 2) {
                                    text(resources.getString(R.string.show_all_amenities))
                                } else {
                                    text(resources.getString(R.string.close_all_amenities))
                                }
                                clickListener(View.OnClickListener {
                                    initialAmenitiesSize = if (initialAmenitiesSize == 2) {
                                        item.listSettings()!!.size
                                    } else {
                                        2
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                        viewholderDivider {
                            id("5")
                        }
                    }
                    if (item.id() == 12 && item.listSettings()!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("Facilities")
                            header(resources.getString(R.string.facilities))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        item.listSettings()!!.subList(0, initialHouseRulesSize).forEachIndexed { index, list ->
                            viewholderFilterCheckbox {
                                id("Facilities ${list.id()}")
                                text(list.itemName())
                                isChecked(spaces.contains(list.id()))
                                onClick(View.OnClickListener {
                                    if (spaces.contains(list.id()!!)) {
                                        spaces.remove(list.id()!!)
                                    } else {
                                        spaces.add(list.id()!!)
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                        if (item.listSettings()!!.size > 3) {
                            viewholderListingDetailsListShowmore {
                                id("readmore facilities")
                                if (initialHouseRulesSize == 2) {
                                    text(resources.getString(R.string.show_all_facilities))
                                } else {
                                    text(resources.getString(R.string.close_all_facilities))
                                }
                                clickListener(View.OnClickListener {
                                    initialHouseRulesSize = if (initialHouseRulesSize == 2) {
                                        item.listSettings()!!.size
                                    } else {
                                        2
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                        viewholderDivider {
                            id("6")
                        }
                    }
                    if (item.id() == 14 && item.listSettings()!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("House rules")
                            header(resources.getString(R.string.house_rules))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        item.listSettings()!!.subList(0, initialFacilitiesSize).forEachIndexed { index, list ->
                            viewholderFilterCheckbox {
                                id("House rules ${list.id()}")
                                text(list.itemName())
                                isChecked(houseRule.contains(list.id()))
                                onClick(View.OnClickListener {
                                    if (houseRule.contains(list.id()!!)) {
                                        houseRule.remove(list.id()!!)
                                    } else {
                                        houseRule.add(list.id()!!)
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                        if (item.listSettings()!!.size > 3) {
                            viewholderListingDetailsListShowmore {
                                id("readmore house rules")
                                if (initialFacilitiesSize == 2) {
                                    text(resources.getString(R.string.show_all_house_rules))
                                } else {
                                    text(resources.getString(R.string.close_all_house_rules))
                                }
                                clickListener(View.OnClickListener {
                                    initialFacilitiesSize = if (initialFacilitiesSize == 2) {
                                        item.listSettings()!!.size
                                    } else {
                                        2
                                    }
                                    requestModelBuild()
                                })
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRetry() {

    }
}