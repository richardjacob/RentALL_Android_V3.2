package com.rentall.radicalstart.ui.listing.map

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ListingListingMapBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class MapFragment: BaseFragment<ListingListingMapBinding, ListingDetailsViewModel>(), OnMapReadyCallback {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.listing_listing_map
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: ListingListingMapBinding
    private lateinit var location: LatLng

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        mBinding.mapView?.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)
    }

    private fun initView() {
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer { list -> list?.let {
            try {
                if (it.lat()!!.isNaN().not() && it.lng()!!.isNaN().not()) {
                    location = LatLng(it.lat()!!, it.lng()!!)
                }
                mBinding.title = it.title() + " "+resources.getString(R.string.inn)+ " " + it.city() + ", " + it.state() + ", " + it.country()
            } catch (e: Exception) {
                showError()
            }
        } })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMaxZoomPreference(18f)
        googleMap.setMinZoomPreference(14f)
        if(::location.isInitialized) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            googleMap.addCircle(CircleOptions()
                    .center(location)
                    .strokeColor(Utils.getColor(requireContext(), R.color.map_stroke))
                    .strokeWidth(3f)
                    .fillColor(Utils.getColor(requireContext(), R.color.map_fill))
                    .radius(500.0))
        }
    }

    override fun onResume() {
        mBinding.mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        mBinding.mapView?.onPause()
        super.onPause()
    }

    override fun onStart() {
        mBinding.mapView?.onStart()
        super.onStart()
    }

    override fun onStop() {
        mBinding.mapView?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mBinding.mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mBinding.mapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mBinding.mapView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRetry() {

    }
}