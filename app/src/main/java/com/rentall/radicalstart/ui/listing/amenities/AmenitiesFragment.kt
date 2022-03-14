package com.rentall.radicalstart.ui.listing.amenities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ViewListingDetailsQuery
import com.rentall.radicalstart.databinding.FragmentListingAmenitiesBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListingDetailsSectionHeader
import com.rentall.radicalstart.viewholderListingDetailsSublist
import javax.inject.Inject

class AmenitiesFragment: BaseFragment<FragmentListingAmenitiesBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: FragmentListingAmenitiesBinding
    private var type: String? = null
    val list = ArrayList<String>()
    private var convertedType: String = ""

    companion object {
        private const val LISTTYPE = "param1"
        @JvmStatic
        fun newInstance(type: String) =
                AmenitiesFragment().apply {
                    arguments = Bundle().apply {
                        putString(LISTTYPE, type)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        arguments?.let {
            type = it.getString(LISTTYPE)
        }
        mBinding.rlShowresult.gone()
        mBinding.filterToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { it?.let { details ->
            when (type) {
                "Amenities" -> {
                    convertedType = getString(R.string.amenities)
                    generateUserAmenitiesList(details)
                }
                "User Space" -> {
                    convertedType = getString(R.string.user_space)
                    generateUserSpaceList(details)
                }
                "User Safety" -> {
                    convertedType = getString(R.string.user_safety)
                    generateUserSafetyList(details)
                }
                "House rules" -> {
                    convertedType = getString(R.string.house_rules)
                    generateHouseRulesList(details)
                }
            }
            initEpoxy(list)
        } })
    }

    private fun generateUserAmenitiesList (details: ViewListingDetailsQuery.Results) {
        details.userAmenities()?.forEach { item ->
            item.let { amenity ->
                if (amenity.itemName().isNullOrEmpty().not()) {
                    list.add(amenity.itemName()!!)
                }
            }
        }
    }

    private fun generateUserSpaceList (details: ViewListingDetailsQuery.Results) {
        details.userSpaces()?.forEach { item ->
            item.let { amenity ->
                if (amenity.itemName().isNullOrEmpty().not()) {
                    list.add(amenity.itemName()!!)
                }
            }
        }
    }

    private fun generateUserSafetyList (details: ViewListingDetailsQuery.Results) {
        details.userSafetyAmenities()?.forEach { item ->
            item.let { amenity ->
                if (amenity.itemName().isNullOrEmpty().not()) {
                    list.add(amenity.itemName()!!)
                }
            }
        }
    }

    private fun generateHouseRulesList (details: ViewListingDetailsQuery.Results) {
        details.houseRules()?.forEach { item ->
            item.let { amenity ->
                if (amenity.itemName().isNullOrEmpty().not()) {
                    list.add(amenity.itemName()!!)
                }
            }
        }
    }

    private fun initEpoxy(it: List<String>) {
        mBinding.rlListingAmenities.withModels {
            viewholderListingDetailsSectionHeader {
                id("header")
                header(convertedType)
            }
            for (i in 0 until it.size) {
                viewholderListingDetailsSublist {
                    id("list")
                    list(it[i])
                    paddingTop(true)
                }
            }
        }
    }

    override fun onRetry() {

    }
}