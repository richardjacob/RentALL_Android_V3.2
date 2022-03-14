package com.rentall.radicalstart.ui.host.step_one

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.model.LatLng
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentAddressBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddressFragment : BaseFragment<HostFragmentAddressBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_address
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentAddressBinding
    private var loction: LatLng? = null
    private var list = ArrayList<GetCountrycodeQuery.Result>()
    var strUser: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if (baseActivity!!.getIntent().hasExtra("from")){
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
            if (strUser.isNotEmpty() && strUser.equals("steps"))
                viewModel.isEdit = true
            else
                viewModel.isEdit = false
        }
        subscribeToLiveData()
        setUp()

        if(Geocoder.isPresent()){
            Timber.d("geocoder is present")
        }else{
            Timber.d("geocoder is not present")
        }

        val test = requireFragmentManager().findFragmentByTag("ADDRESS")
        Log.d("from feagment "," map location ::: "+test)


        if (viewModel.isListAdded) {
            mBinding.actionBar.tvRightside.text = getText(R.string.save_and_exit)
            mBinding.actionBar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_bar_color))
            mBinding.actionBar.tvRightside.onClick {
                viewModel.retryCalled = "update"
                viewModel.address.set("")
                viewModel.location.set("")
                viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                viewModel.getLocationFromGoogle(viewModel.address.get().toString(),false){if (viewModel.country.get()!!.trim().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_country))
                } else if (viewModel.street.get()!!.trim().isEmpty()) {
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_street))
                } else if (viewModel.city.get()!!.trim().isEmpty()) {
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_city))
                } else if (viewModel.state.get()!!.trim().isEmpty()) {
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_state))
                } else if (viewModel.zipcode.get()!!.trim().isEmpty()) {
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_zip_code))
                }  else if (viewModel.location.get().isNullOrEmpty()) {
                    if (isNetworkConnected){
                        baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
                    }else{
                        baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                    }
                }  else {
                    viewModel.updateHostStepOne()
                }}
            }
        } else {
            mBinding.actionBar.tvRightside.visibility = View.GONE
        }
        mBinding.actionBar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.actionBar.ivNavigateup.onClick {
            viewModel.address.set("")
            viewModel.location.set("")
            viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
            viewModel.getLocationFromGoogle(viewModel.address.get().toString(),false){}
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_BATHROOM)
        }

   /*     RxView.clicks(mBinding.tvNext).debounce(500, TimeUnit.MILLISECONDS).subscribe { empty ->
            hideKeyboard()
            viewModel.address.set("")
            viewModel.location.set("")
            if (viewModel.isEdit){
                viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
            }else{
                viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
            }
            viewModel.location.set(viewModel.getLocationFromAddress(viewModel.address.get().toString()))
            if (viewModel.country.get().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_country))
            } else if (viewModel.street.get().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_street))
            } else if (viewModel.city.get().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_city))
            } else if (viewModel.state.get().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_state))
            } else if (viewModel.zipcode.get().isNullOrEmpty()) {
                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.please_enter_zip_code))
            } else if (viewModel.location.get().isNullOrEmpty() &&  viewModel.isEdit) {
                if (isNetworkConnected){
                    baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
                }else{
                    baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                }
            } else {
                if (viewModel.buildingName.get().isNullOrEmpty()) {
                    viewModel.buildingName.set("")
                }
                hideSnackbar()
                if (isNetworkConnected) {
                    if (viewModel.location.get().isNullOrEmpty()){
                        baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
                    }else {
                        hideSnackbar()
                        if (viewModel.listId.get()!!.isEmpty() || viewModel.listId.get()!!.isBlank()) {
                            viewModel.createHostStepOne()
                        }else{
                            viewModel.onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                        }
                    }
                }else{
                    baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                }
            }
        }*/
    }

    private fun subscribeToLiveData() {
        viewModel.list.observe(viewLifecycleOwner, Observer {
            it?.let { result ->
                list = ArrayList(result)
                mBinding.rvStepOne.requestModelBuild()
            }
        })
    }

    private fun setUp() {

        mBinding.rvStepOne.withModels {
            viewholderUserName {
                id("your place")
                name(getString(R.string.where_your_place_located))
            }
            viewholderHostStepOne {
                id("guests like to know")
                textSize(false)
                paddingTop(false)
                paddingBottom(false)
                isBlack(false)
                visibility(false)
            }
            viewholderSavedPlaceholder {
                id("country")
                header(getString(R.string.country_region))
                large(false)
                isBlack(true)
            }
            viewholderHostSelectCountry {
                id("country select")
                msg(getString(R.string.afghanistan))
                if (list.isNotEmpty()) {
                    for (index in 0 until list.size) {
                        if (viewModel.country.get().toString().isNotEmpty()) {
                            if (viewModel.country.get().toString().equals(list[index].countryCode())) {
                                viewModel.country.set(list[index].countryName())
                            }
                        }
                    }
                }
                observableText(viewModel.country)
                onBind { _, view, _ ->
                    val textView = view.dataBinding.root.findViewById<TextView>(R.id.et_title)
                    textView.setOnClickListener {
                        Utils.clickWithDebounce(textView){
                            hideSnackbar()
                            hideKeyboard()
                            viewModel.onContinueClick(StepOneViewModel.NextScreen.SELECT_COUNTRY)
                            textView.isEnabled = false
                        }
                    }
                    textView.isEnabled = true
                }
            }
            viewholderDivider {
                id("divider 1")
            }
            viewholderSavedPlaceholder {
                id("street")
                header(getString(R.string.street))
                large(false)
                isBlack(true)
            }
            viewholderHostAddressEt {
                id("street et")
                msg(getString(R.string.main_st)).toString()
                observableText(viewModel.street)
            }
            viewholderSavedPlaceholder {
                id("apt")
                header(getString(R.string.apt_suite_etc))
                large(false)
                isBlack(true)
            }
            viewholderHostAddressEt {
                id("apt et")
                msg(getString(R.string.apt))
                observableText(viewModel.buildingName)
            }
            viewholderSavedPlaceholder {
                id("city")
                header(getString(R.string.city))
                large(false)
                isBlack(true)
            }
            viewholderHostAddressEt {
                id("city et")
                msg(getString(R.string.san_francisco))
                observableText(viewModel.city)
            }
            viewholderSavedPlaceholder {
                id("state")
                header(getString(R.string.state))
                large(false)
                isBlack(true)
            }
            viewholderHostAddressEt {
                id("state et")
                msg(getString(R.string.ca))
                observableText(viewModel.state)
            }
            viewholderSavedPlaceholder {
                id("zip")
                header(getString(R.string.zip_postal_code))
                large(false)
                isBlack(true)
            }
            viewholderHostAddressEt {
                id("code et")
                msg(getString(R.string.code))
                observableText(viewModel.zipcode)
            }

            /*viewholderSavedPlaceholder {
                id("sampleResponse")
                header("Response")
                large(false)
                isBlack(true)
            }
            viewholderDescriptionTextbox {
                id("response text")
                textValue(viewModel.responseFromApi)
                onBind{_,view,_ ->
                    val et = view.dataBinding.root.findViewById<EditText>(R.id.et_descriptionBox)
                    // viewModel.descText = et
//                    et.requestFocus()
                    et.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (et.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(false)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }

            }*/


        }
    }

    fun getLocationFromAddress(strAddress: String): String? {
    Log.d("   "," strAddress ::  "+strAddress)
        val coder = Geocoder(activity)
        val address: List<Address>?
        try {
            address = coder.getFromLocationName(strAddress, 1)
            if (address == null || address.isEmpty()) {
                return null
            }
            val location = address[0]
            val lat = location.latitude
            val lng = location.longitude
            loction = LatLng(lat, lng)
            viewModel.lat.set(lat.toString())
            viewModel.lng.set(lng.toString())
            viewModel.location.set(loction.toString())

            return "$lat,$lng"

        } catch (e: Exception) {
            return null
        }

    }

    override fun onRetry() {

    }

}