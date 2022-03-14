package com.rentall.radicalstart.ui.listing.cancellation

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentListingAmenitiesBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class CancellationFragment: BaseFragment<FragmentListingAmenitiesBinding, ListingDetailsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: FragmentListingAmenitiesBinding
    val list = ArrayList<CancellationState>()
    var cancellationDesc: String = ""

    data class CancellationState(val color: Int, val date: String, val day: String, val content: String)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.filterToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.rlShowresult.gone()
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { it?.let { details ->
         //   cancellationDesc = fromHtml("<b>"+details.listingData()?.cancellation()?.policyName()+"</b>" + " - "+details.listingData()?.cancellation()?.policyContent())
            cancellationDesc = details.listingData()?.cancellation()?.policyName() + " - "+details.listingData()?.cancellation()?.policyContent()
            when (details.listingData()?.cancellation()?.policyName()) {
                "Flexible" -> generateFlexibleItems()
                "Moderate" -> generateModerateItems()
                "Strict" -> generateStrictItems()
            }
            initEpoxy()
        } })
    }

    private fun generateModerateItems() {
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_green),
                resources.getString(R.string.moderate_day),
                resources.getString(R.string.moderate_date1),
                resources.getString(R.string.moderate_green)))
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_yellow),
                resources.getString(R.string.check_in),
                resources.getString(R.string.moderate_date2),
                resources.getString(R.string.moderate_yellow)))
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_red),
                resources.getString(R.string.check_out),
                resources.getString(R.string.moderate_date3),
                resources.getString(R.string.moderate_red)))
    }

    private fun generateStrictItems() {
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_green),
                resources.getString(R.string.strict_day),
                resources.getString(R.string.strict_date1),
                resources.getString(R.string.strict_yellow)))
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_yellow),
                resources.getString(R.string.check_in),
                resources.getString(R.string.strict_date2),
                resources.getString(R.string.strict_red)))
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_red),
                resources.getString(R.string.check_out),
                resources.getString(R.string.strict_date3),
                resources.getString(R.string.strict_red1)))
    }

    private fun generateFlexibleItems() {
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_green),
                resources.getString(R.string.flexible_day),
                resources.getString(R.string.flexible_date1),
                resources.getString(R.string.flexible_green)))
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_yellow),
                resources.getString(R.string.check_in),
                resources.getString(R.string.flexible_date2),
                resources.getString(R.string.flexible_yellow)))
        list.add(CancellationState(
                ContextCompat.getColor(requireContext(), R.color.cancellation_red),
                resources.getString(R.string.check_out),
                resources.getString(R.string.flexible_date3),
                resources.getString(R.string.flexible_red)))
    }

    private fun initEpoxy() {
        mBinding.rlListingAmenities.withModels {
            viewholderListingDetailsSectionHeader {
                id("header")
                header(resources.getString(R.string.cancellation))
            }
            viewholderListingDetailsHeader {
                id("subheader")
                header(cancellationDesc)
                isBlack(true)
                large(false)
                typeface(Typeface.DEFAULT_BOLD)
            }
            viewholderListingDetailsHeader {
                id("example")
                header(resources.getString(R.string.example))
                isBlack(true)
                large(false)
                typeface(Typeface.DEFAULT_BOLD)
            }
            list.forEachIndexed { index, item ->
                viewholderListingDetailsCancellation {
                    id(index)
                    color(item.color)
                    date(item.date)
                    day(item.day)
                    content(item.content)
                }
            }
            viewholderUserNormalText {
                id("cancellation policy")
                text(resources.getString(R.string.cancellation_policy_points))
                paddingTop(true)
                paddingBottom(true)
            }
        }
    }

    override fun onRetry() {

    }
}