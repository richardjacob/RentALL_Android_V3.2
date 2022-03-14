package com.rentall.radicalstart.ui.listing.desc

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentListingAmenitiesBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListingDetailsSectionHeader
import com.rentall.radicalstart.viewholderListingDetailsSublist
import javax.inject.Inject

class DescriptionFragment: BaseFragment<FragmentListingAmenitiesBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentListingAmenitiesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.rlShowresult.gone()
        mBinding.filterToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { text -> text?.let { initEpoxy(it.description()) } })
    }

    private fun initEpoxy(it: String?) {
        mBinding.rlListingAmenities.withModels {
            viewholderListingDetailsSectionHeader {
                id("header")
                header(resources.getString(R.string.details))
            }
            if (it.isNullOrEmpty().not()) viewholderListingDetailsSublist {
                id("desc")
                list(it)
                paddingTop(true)
            }
        }
    }

    override fun onRetry() {

    }

}