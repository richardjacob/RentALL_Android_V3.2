package com.rentall.radicalstart.ui.host.step_one

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentMapLocationBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import timber.log.Timber
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MaplocationFragment : BaseFragment<HostFragmentMapLocationBinding, StepOneViewModel>(), OnMapReadyCallback {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_map_location
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentMapLocationBinding
    private lateinit var p1: LatLng
    private lateinit var mMap: GoogleMap
    var strUser: String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if (baseActivity!!.getIntent().hasExtra("from")){
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
            viewModel.isEdit = strUser.isNotEmpty() && strUser.equals("steps")
        }
        if (viewModel.isEdit){
            if (viewModel.lat.get() != null && viewModel.lng.get() != null) {
                p1 = LatLng(viewModel.lat.get()!!.toDouble(), viewModel.lng.get()!!.toDouble())
                viewModel.latLng.set(p1?.toString())
            }
            else {
                viewModel.latLng.set("")
                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.you_have_entered_an_incorrect_location))
                baseActivity?.onBackPressed()
            }
            viewModel.address.set("")
            viewModel.location.set("")
            viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
        }else{
            viewModel.address.set("")
            viewModel.location.set("")
            viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.zipcode.get())
        }

        if (viewModel.isListAdded) {
            mBinding.actionBar.tvRightside.text = getText(R.string.save_and_exit)
            mBinding.actionBar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_bar_color))
            mBinding.actionBar.tvRightside.onClick {
                viewModel.retryCalled = "update"
                if (viewModel.countryCode.get().isNullOrEmpty() || viewModel.street.get().isNullOrEmpty() || viewModel.city.get().isNullOrEmpty() || viewModel.state.get().isNullOrEmpty() || viewModel.zipcode.get().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page), resources.getString(R.string.please_fill_them))
                } else if (viewModel.isEdit && viewModel.location.get().isNullOrEmpty()) {
                    if (isNetworkConnected){
                        Timber.d("MapLocationFragment")
                        baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
                    }else{
                        baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                    }
                }else {
                    viewModel.updateHostStepOne()
                }
            }
        } else {
            mBinding.actionBar.tvRightside.visibility = View.GONE
        }
        mBinding.actionBar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.actionBar.ivNavigateup.onClick {
            if (viewModel.isEdit){
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.ADDRESS)
            }else{
                baseActivity?.onBackPressed() }
        }
        mBinding.text = getString(R.string.is_the_pin_in_the_right_place)
        mBinding.title = getString(R.string.entire_place) + getString(R.string.inn) + getString(R.string.madurai) + ", " + getString(R.string.tn) + ", " + getString(R.string.entire_place)

        mBinding.mapView?.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)
        mBinding.tvNext.setOnClickListener {

            if (viewModel.isAddressResolved) {
                Utils.clickWithDebounce(mBinding.tvNext){
                    Log.d("location ", "is ::: " + viewModel.address.get().toString())
                    viewModel.onContinueClick(StepOneViewModel.NextScreen.AMENITIES)
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setMaxZoomPreference(30f)
        googleMap.setMinZoomPreference(14f)
        googleMap.isMyLocationEnabled
        getLocationFromAddress(viewModel.address.get().toString())
    }
    fun mapIconAndLocation(){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(p1))
            val bitmap = activity?.getBitmapFromVectorDrawable(R.drawable.ic_marker)
            val a = MarkerOptions().position(p1).icon(BitmapDescriptorFactory.fromBitmap(bitmap))
            val drgMarkers = mMap.addMarker(a)
            val drgCircle = mMap.addCircle(CircleOptions()
                    .center(p1)
                    .strokeColor(Utils.getColor(requireContext(), R.color.map_stroke))
                    .strokeWidth(3f)
                    .fillColor(Utils.getColor(requireContext(), R.color.map_fill))
                    .radius(500.0))

            mMap.setOnCameraMoveListener {
                drgMarkers.position = mMap.cameraPosition.target
                drgCircle.isVisible = false
            }
            mMap.setOnCameraIdleListener {
                drgMarkers.position = mMap.cameraPosition.target
                drgCircle.isVisible = true
                drgCircle.center = mMap.cameraPosition.target
                p1 = drgMarkers!!.position
                viewModel.address.set(p1.toString())
                viewModel.lat.set(p1.latitude.toString())
                viewModel.lng.set(p1.longitude.toString())
                Log.d("current ", " lat lng is == " + viewModel.address.get().toString())
            }
    }


    fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(this, drawableId) ?: return null

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888) ?: return null
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    fun getLocationFromAddress(strAddress: String) {
        Log.d("   "," strAddress ::  "+strAddress)

        var url= "https://maps.googleapis.com/maps/api/geocode/json?address=${URLEncoder.encode(strAddress,"UTF-8")}&key=${Constants.googleMapKey}"
        val request= JsonObjectRequest(Request.Method.GET,url,null,Response.Listener { response ->
            try{
                var lat=0.0
                var lng=0.0
                Timber.d("MapLocationError1231 $lat, $lng")
                val jsonArray= response.getJSONArray("results")
                if(jsonArray.length()>0){
                    viewModel.isAddressResolved = true
                    val jsonObject= jsonArray.getJSONObject(0)
                    val geometry= jsonObject.getJSONObject("geometry")
                    val locations= geometry.getJSONObject("location")
                    lat = locations.getDouble("lat")
                    lng = locations.getDouble("lng")
                    p1 = LatLng(lat, lng)
                    viewModel.lat.set(lat.toString())
                    viewModel.lng.set(lng.toString())
                    viewModel.latLng.set(p1.toString())
                    viewModel.location.set("$lat,$lng")
                    Timber.d("MapLocationError1234 $lat, $lng")
                    mapIconAndLocation()
                }else{
                    viewModel.isAddressResolved = false
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.something_went_wrong))
                    Timber.d("MapLocationError1232 $lat, $lng")
                    Timber.d("Enable geoCoding api or check the billing account enable in google cloud console")
                }

            }catch (e: Exception){
                viewModel.isAddressResolved = false
                baseActivity!!.showError(e)
                 Timber.d("MapLocationError $e")
            }
        },Response.ErrorListener {
            viewModel.isAddressResolved = false
            baseActivity!!.showError(it)
            Timber.d("MapLocationError12")
            it.printStackTrace()
        })
        viewModel.requestQueue?.add(request)
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