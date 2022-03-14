package com.rentall.radicalstart.ui.host.step_one

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostActivityStepOneBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.host.HostFinalActivity
import com.rentall.radicalstart.util.addFragmentToActivity
import com.rentall.radicalstart.util.replaceFragmentInActivity
import com.rentall.radicalstart.util.replaceFragmentToActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class StepOneActivity : BaseActivity<HostActivityStepOneBinding, StepOneViewModel>(), StepOneNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostActivityStepOneBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.rentall.radicalstart.R.layout.host_activity_step_one
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(StepOneViewModel::class.java)
    var strUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        viewModel.requestQueue = Volley.newRequestQueue(this)
        if (getIntent().hasExtra("from")) {
            strUser = intent.getStringExtra("from").orEmpty()
        }
        if (strUser.isNotEmpty() && strUser.equals("steps")) {
            viewModel.listId.set(intent.getStringExtra("listID"))
            viewModel.isListAdded = true
            viewModel.yesNoString.set(intent.getStringExtra("yesNoString"))
            viewModel.bathroomCapacity.set(intent.getStringExtra("bathroomCapacity"))
            viewModel.country.set(intent.getStringExtra("country"))
            viewModel.countryCode.set(intent.getStringExtra("countryCode"))
            viewModel.street.set(intent.getStringExtra("street"))
            viewModel.buildingName.set(intent.getStringExtra("buildingName"))
            viewModel.city.set(intent.getStringExtra("city"))
            viewModel.state.set(intent.getStringExtra("state"))
            viewModel.zipcode.set(intent.getStringExtra("zipcode"))
            viewModel.lat.set(intent.getStringExtra("lat"))
            viewModel.lng.set(intent.getStringExtra("lng"))
            viewModel.isEdit = true
            viewModel.getListingSetting("edit")
        }else{
            viewModel.isEdit = false
            addFragmentToActivity(mBinding.flSteps.id, TypeOfSpaceFragment(), "TYPE_OF_SPACE")
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    fun openFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .replace(mBinding.flSteps.id, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun popFragment(fragment: Fragment, tag: String) {
        replaceFragmentToActivity(mBinding.flSteps.id, fragment, tag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flSteps.id, fragment, tag)
    }

    override fun onRetry() {
        Log.d("Retry","called")
        if(viewModel.retryCalled.equals("")) {
            viewModel.step1Retry(strUser)
        }else{
            viewModel.updateHostStepOne(true)
            viewModel.getCountryCode()
        }
    }

    override fun navigateScreen(NextScreen: StepOneViewModel.NextScreen) {
        Log.d("clicked", "continue")
        hideKeyboard()
        when (NextScreen) {
            StepOneViewModel.NextScreen.NO_OF_GUEST -> replaceFragment(NoOfGuestFragment(), "NO_OF_GUEST")
            StepOneViewModel.NextScreen.KIND_OF_PLACE -> { replaceFragment(KindOfPlaceFragment(),"KIND_OF_PLACE") }
            StepOneViewModel.NextScreen.TYPE_OF_BEDS -> replaceFragment(TypeOfBedsFragment(),"BedRooms")
            StepOneViewModel.NextScreen.NO_OF_BATHROOM ->{
                viewModel.editBedCount = 0
                viewModel.updateCount.value?.forEachIndexed { index, s ->
                    viewModel.editBedCount = viewModel.editBedCount + s.toInt()
                }
                if(viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount){
                    showSnackbar(getString(R.string.bed_count), getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count))
                }else {
                    replaceFragment(NoOfBathroomFragment(), "NO_OF_BATHROOM")
                }
            }
            StepOneViewModel.NextScreen.ADDRESS -> replaceFragment(AddressFragment(),"ADDRESS")
            StepOneViewModel.NextScreen.MAP_LOCATION -> replaceFragment(MaplocationFragment(), "MAP_LOCATION")
            StepOneViewModel.NextScreen.AMENITIES -> replaceFragment(AmenitiesFragment(), "AMENITIES")
            StepOneViewModel.NextScreen.SAFETY_PRIVACY -> replaceFragment(SafetynPrivacyFragment(), "SAFETY_PRIVACY")
            StepOneViewModel.NextScreen.GUEST_SPACE -> replaceFragment(GuestSpacesFragment(), "GUEST_SPACE")
            StepOneViewModel.NextScreen.SELECT_COUNTRY -> replaceFragment(SelectCountry(), "SELECT_COUNTRY")
            StepOneViewModel.NextScreen.FINISHED -> {
                val intent = Intent(this@StepOneActivity, HostFinalActivity::class.java)
                intent.putExtra("listId", viewModel.listId.get())
                intent.putExtra("yesNoString", "Yes")
                intent.putExtra("bathroomCapacity", "0")
                intent.putExtra("country", "")
                intent.putExtra("countryCode","")
                intent.putExtra("street", "")
                intent.putExtra("buildingName", "")
                intent.putExtra("city", "")
                intent.putExtra("state", "")
                intent.putExtra("zipcode", "")
                intent.putExtra("lat","")
                intent.putExtra("lng","")
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    override fun navigateBack(BackScreen: StepOneViewModel.BackScreen) {
        hideKeyboard()
        when (BackScreen) {
            StepOneViewModel.BackScreen.KIND_OF_PLACE -> { popFragment(KindOfPlaceFragment(),"KIND_OF_PLACE") }
            StepOneViewModel.BackScreen.TYPE_OF_BEDS -> popFragment(TypeOfBedsFragment(), "BedRooms")
            StepOneViewModel.BackScreen.NO_OF_BATHROOM -> popFragment(NoOfBathroomFragment(), "NO_OF_BATHROOM")
            StepOneViewModel.BackScreen.NO_OF_GUEST -> popFragment(NoOfGuestFragment(), "NO_OF_GUEST")
            StepOneViewModel.BackScreen.ADDRESS -> popFragment(AddressFragment(),"ADDRESS")
            StepOneViewModel.BackScreen.MAP_LOCATION -> popFragment(MaplocationFragment(), "MAP_LOCATION")
            StepOneViewModel.BackScreen.TYPE_OF_SPACE -> popFragment(TypeOfSpaceFragment(), "TYPE_OF_SPACE")
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onBackPressed() {
        try {
            viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
            getLocationFromAddress(viewModel.address.get().toString())
            val myFrag = supportFragmentManager.findFragmentByTag("KIND_OF_PLACE")
            if (myFrag != null && myFrag.isVisible && myFrag.isAdded) {
                if (viewModel.isEdit){
                    finish()
                }else{
                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_SPACE)
                }
            } else {
                val test = supportFragmentManager.findFragmentByTag("ADDRESS")
                if (test != null && test.isVisible() && test.isAdded()) {
                    if (viewModel.buildingName.get().isNullOrEmpty()) {
                        viewModel.buildingName.set("")
                    }
                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_BATHROOM)
                }else{
                    val test = supportFragmentManager.findFragmentByTag("NO_OF_GUEST")
                    if(test != null && test.isVisible){
                        viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
                    }else{
                        val test1 = supportFragmentManager.findFragmentByTag("MAP_LOCATION")
                        if(test1 != null && test1.isVisible){
                            viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.ADDRESS)
                        }else{
                            val test2 = supportFragmentManager.findFragmentByTag("NO_OF_BATHROOM")
                            if(test2 != null && test2.isVisible && test2.isAdded){
                                if (viewModel.isEdit){
                                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_BEDS)
                                }else{
                                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_BEDS) }
                            }else{
                                val test3 = supportFragmentManager.findFragmentByTag("TYPE_OF_SPACE")
                                if(test3 !=null && test3.isVisible && test3.isAdded){
                                    finish()
                                }else{
                                    val test = supportFragmentManager.findFragmentByTag("BedRooms")
                                    if (test != null && test!!.isVisible()) {
                                        viewModel.editBedCount = 0
                                        viewModel.updateCount.value!!.forEachIndexed { index, s ->
                                            viewModel.editBedCount = viewModel.editBedCount + s.toInt()
                                        }
                                        if(viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount){
                                            showSnackbar(getString(R.string.bed_count), getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count))
                                        }else {
                                            viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
                                        }
                                    }else {
                                        super.onBackPressed()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError(e)
        }
    }
    fun getLocationFromAddress(strAddress: String): String? {
        Log.d("123963", " strAddress ::  $strAddress")
        val coder = Geocoder(this)
        val address: List<Address>?
        try {
            address = coder.getFromLocationName(strAddress, 1)
            if (address == null) {
                return null
            }
            val location = address.getOrNull(0)
            val lat = location?.latitude
            val lng = location?.longitude
            viewModel.lat.set(lat.toString())
            viewModel.lng.set(lng.toString())
            if (lat != null && lng != null)
            viewModel.location.set(LatLng(lat, lng).toString())
            return "$lat,$lng"
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    override fun show404Page() {
        showToast(getString(R.string.list_not_available))
        val intent = Intent(this@StepOneActivity, HostFinalActivity::class.java)
        intent.putExtra("listId", viewModel.listId.get())
        intent.putExtra("yesNoString", "Yes")
        intent.putExtra("bathroomCapacity", "0")
        intent.putExtra("country", "")
        intent.putExtra("countryCode","")
        intent.putExtra("street", "")
        intent.putExtra("buildingName", "")
        intent.putExtra("city", "")
        intent.putExtra("state", "")
        intent.putExtra("zipcode", "")
        intent.putExtra("lat","")
        intent.putExtra("lng","")
        startActivity(intent)
        this.finish()
    }

}