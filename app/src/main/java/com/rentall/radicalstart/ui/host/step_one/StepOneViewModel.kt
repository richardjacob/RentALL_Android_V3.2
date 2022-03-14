package com.rentall.radicalstart.ui.host.step_one

import android.location.Address
import android.location.Geocoder
import android.os.Handler
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.BecomeHostStep1
import com.rentall.radicalstart.vo.PersonCount
import org.json.JSONArray
import timber.log.Timber
import java.net.URLEncoder
import java.text.DecimalFormat
import java.util.ArrayList
import javax.inject.Inject
import kotlin.collections.HashMap

class StepOneViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<StepOneNavigator>(dataManager, resourceProvider) {

    var isEdit = false

    var totalBedCount = 0
    var editBedCount = 0

    val msg = ObservableField("")
    var selectedAmenities = ArrayList<Int>()
    var selectedDetectors = ArrayList<Int>()
    var selectedSpace = ArrayList<Int>()
    var selectedBeds = ArrayList<Int>()
    var address = ObservableField<String>()
    var isAddressResolved = false
    lateinit var defaultSettings: MutableLiveData<GetListingSettingQuery.Results>

    val roomtypelist = MutableLiveData<GetListingSettingQuery.RoomType>()
    val roomType = MutableLiveData<String>()
    private lateinit var p1: LatLng
    val personCapacity = MutableLiveData<GetListingSettingQuery.PersonCapacity>()
    val capacity = MutableLiveData<String>()

    val housetypelist = MutableLiveData<GetListingSettingQuery.HouseType>()
    val houseType = MutableLiveData<String>()

    val roomSizelist = MutableLiveData<GetListingSettingQuery.BuildingSize>()
    val roomSizeType = MutableLiveData<String>()

    val yesNoType = MutableLiveData<String>()
    val yesNoString = ObservableField<String>()
    val personCapacity1 = ObservableField<String>()
    val guestCapacity = MutableLiveData<String>()
    val roomCapacity = ObservableField<String>()
    val roomNoCapacity = MutableLiveData<String>()
    val bedCapacity = ObservableField<String>()
    val bedNoCapacity = MutableLiveData<String>()
    val guestPlaceType = MutableLiveData<String>()

    var defaultselectedBedTypeId = ArrayList<String>()
    val bedroomlist = MutableLiveData<GetListingSettingQuery.Bedrooms>()
    val bedTypelist = MutableLiveData<GetListingSettingQuery.BedType>()
    val bedType = MutableLiveData<String>()
    var typeOfBeds = MutableLiveData<ArrayList<PersonCount>>()

    val bathroomlist = MutableLiveData<GetListingSettingQuery.BathroomType>()
    val bathroomType = MutableLiveData<String>()

    val bathroomCapacity = ObservableField<String>()
    val noOfBathroom = MutableLiveData<GetListingSettingQuery.Bathrooms>()
    val bathroomCount = MutableLiveData<String>()
    val updateCount = MutableLiveData<ArrayList<String>>()
    val beds = MutableLiveData<GetListingSettingQuery.Beds>()
    val bedCount = MutableLiveData<GetListingSettingQuery.Beds>()
    val noOfBed = MutableLiveData<String>()

    val list = MutableLiveData<List<GetCountrycodeQuery.Result>>()
    val listSearch = MutableLiveData<ArrayList<GetCountrycodeQuery.Result>>()
    val isCountryCodeLoad = ObservableField(false)

    val amenitiedId = MutableLiveData<ArrayList<Int>>()
    val aafetyAmenitiedId = MutableLiveData<ArrayList<Int>>()
    val spacesId = MutableLiveData<ArrayList<Int>>()
    val bedTypesId = MutableLiveData<ArrayList<Int>>()
    val amenitiesList = MutableLiveData<GetListingSettingQuery.Amenities>()
    val safetyAmenitiesList = MutableLiveData<GetListingSettingQuery.SafetyAmenities>()
    val sharedSpaceList = MutableLiveData<GetListingSettingQuery.Spaces>()

    val street = ObservableField("")
    val country = ObservableField("")
    val countryCode = ObservableField("")
    val buildingName = ObservableField("")
    val city = ObservableField("")
    val state = ObservableField("")
    val zipcode = ObservableField("")
    val responseFromApi=ObservableField("")
    var lat = ObservableField("")
    var lng = ObservableField("")
    val latLng = ObservableField("")
    val listId = ObservableField("")
    val location = ObservableField("")
    var showAmentiesId: Int? = null
    var showSafetyAmentiesId: Int? = null
    var showSpacesId: Int? = null
    var showbedTypesId: Int? = null
    var isListAdded = false
    var retryCalled = ""
    val lottieProgress = ObservableField<StepOneViewModel.LottieProgress>(StepOneViewModel.LottieProgress.LOADING)
    val isNext = ObservableField<Boolean>(false)
    var requestQueue: RequestQueue? = null
    var listSetting = MutableLiveData<GetListingSettingQuery.Results>()

    var becomeHostStep1 = MutableLiveData<BecomeHostStep1>()
    var bedTypesArray = ArrayList<HashMap<String, String>>()

    enum class NextScreen {
        KIND_OF_PLACE,
        NO_OF_GUEST,
        TYPE_OF_BEDS,
        NO_OF_BATHROOM,
        ADDRESS,
        MAP_LOCATION,
        AMENITIES,
        SAFETY_PRIVACY,
        GUEST_SPACE,
        FINISHED,
        SAVE_N_EXIT,
        SELECT_COUNTRY
    }

    enum class BackScreen {
        KIND_OF_PLACE,
        NO_OF_GUEST,
        TYPE_OF_BEDS,
        NO_OF_BATHROOM,
        ADDRESS,
        MAP_LOCATION,
        TYPE_OF_SPACE
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    fun loadDefaultSettings(): MutableLiveData<GetListingSettingQuery.Results> {
        if (!::defaultSettings.isInitialized) {
            defaultSettings = MutableLiveData()
            becomeHostStep1 = MutableLiveData()
            getListingSetting("add")
        }
        return defaultSettings
    }

    fun onContinueClick(screen: NextScreen) {
        navigator.navigateScreen(screen)
    }

    fun showError(){
        navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.currently_offline))
    }

    fun checkValidation() {
        navigator.hideKeyboard()
        address.set("")
        location.set("")
        if (isEdit) {
            address.set(street.get()!!.trim() + ", " + countryCode.get()!!.trim() + ", " + state.get()!!.trim() + ", " + city.get()!!.trim())
        } else {
            address.set(street.get()!!.trim() + ", " + countryCode.get()!!.trim() + ", " + state.get()!!.trim() + ", " + city.get()!!.trim())
        }
        if (country.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.please_enter_country))
        } else if (street.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.please_enter_street))
        } else if (city.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.please_enter_city))
        } else if (state.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.please_enter_state))
        } else if (zipcode.get()!!.trim().isNullOrEmpty()) {
            navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.please_enter_zip_code))
        } else  {
            isNext.set(true)
            lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
            getLocationFromGoogle(address.get()!!,true){}
        }
    }

    fun getLocationFromGoogle(address: String, updateStep1: Boolean, updateFun : () -> Unit){
        val url= "https://maps.googleapis.com/maps/api/geocode/json?address=${URLEncoder.encode(address,"UTF-8")}&key=${Constants.googleMapKey}"
        val request = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
            response ->
            try{
                isNext.set(false)
                lottieProgress.set(LottieProgress.NORMAL)
                val jsonArray= response.getJSONArray("results")
                responseFromApi.set(jsonArray.toString())

                if(jsonArray.length()>0){
                    val jsonObject= jsonArray.getJSONObject(0)
                    val geometry= jsonObject.getJSONObject("geometry")
                    val locations= geometry.getJSONObject("location")
                    location.set("${locations.getDouble("lat")},${locations.getDouble("lng")}")
                    lat.set(locations.getDouble("lat").toString())
                    lng.set(locations.getDouble("lng").toString())
                    updateFun()
                    if(updateStep1){
                        if(listId.get()!!.isEmpty() || listId.get()!!.isBlank()){
                            updateStep1((location.get()!!),false)
                        }else{
                            updateStep1((location.get()!!),true)
                        }

                    }
                    Timber.d("latlng123-->>  $locations")
                }else{
                    location.set("")
                    updateFun()
                    Timber.d("Enable geoCoding api or check the billing account enable in google cloud console")
                    if(updateStep1){
                        if(listId.get()!!.isEmpty() || listId.get()!!.isBlank()){
                            updateStep1("",false)
                        }else{
                            updateStep1("",true)
                        }
                    }
                }
            }catch (e: Exception){
                navigator.showToast("Something went wrong try again later")
            }
        },Response.ErrorListener { error ->
            isNext.set(false)
            responseFromApi.set(error.message)
            lottieProgress.set(LottieProgress.NORMAL)
            navigator.showToast("Something went wrong")
            error.printStackTrace() })
        requestQueue?.add(request)
    }

    fun updateStep1(locationLatLng: String,goto: Boolean){
        locationLatLng.let {
            if (locationLatLng.isNullOrEmpty() && isEdit) {
                navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.incorrect_location))
            } else {
                if (buildingName.get().isNullOrEmpty()) {
                    buildingName.set("")
                }
                navigator.hideSnackbar()
                if (locationLatLng.isNullOrEmpty()) {
                    navigator.showSnackbar(resourceProvider.getString(R.string.error_1), resourceProvider.getString(R.string.incorrect_location))
                } else {
                    if(goto){
                        onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                    }else{
                        navigator.hideSnackbar()
                        if (listId.get()!!.isEmpty() || listId.get()!!.isBlank()) {
                            isNext.set(true)
                            location.set(locationLatLng.toString())
                            lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
                            createHostStepOne()
                        } else {
                            isNext.set(true)
                            lottieProgress.set(StepOneViewModel.LottieProgress.LOADING)
                            isNext.set(false)
                            lottieProgress.set(StepOneViewModel.LottieProgress.NORMAL)
                            location.set(locationLatLng.toString())
                            onContinueClick(StepOneViewModel.NextScreen.KIND_OF_PLACE)
                        }
                    }

                }
            }
        }
    }

    fun createHostStepOne() {

        updateCount.value!!.forEachIndexed { index, s ->
            if (s.equals("0").not()) {
                val temp = HashMap<String, String>()
                temp.put("bedCount", s)
                temp.put("bedType", defaultselectedBedTypeId.get(index))
                bedTypesArray.add(temp)
            }
        }

        val jsonVal = JSONArray(bedTypesArray)
        Log.d("printing ", "jSon val == " + jsonVal.toString())

        var bath = becomeHostStep1.value!!.bathroomCount!!.toString()
        if (bath.contains(",")) {
            bath = bath.replace(",", ".")
        }
        val request = CreateListingMutation
                .builder()
                .roomType(becomeHostStep1.value!!.placeType)
                .personCapacity(becomeHostStep1.value!!.totalGuestCount!!.toInt())
                .residenceType(becomeHostStep1.value!!.yesNoOptions)
                .buildingSize(becomeHostStep1.value!!.roomCapacity)
                .bathroomType(becomeHostStep1.value!!.bathroomSpace)
                .bathrooms(bath.toDouble())
                .beds(becomeHostStep1.value!!.beds!!.toInt())
                .bedTypes(jsonVal.toString())
                .bedType("17")
                .bedrooms(becomeHostStep1.value!!.bedroomCount)
                .city(city.get())
                .amenities(selectedAmenities.toList())
                .houseType(becomeHostStep1.value!!.houseType)
                .safetyAmenities(selectedDetectors.toList())
                .spaces(selectedSpace.toList())
                .state(state.get())
                .country(countryCode.get())
                .zipcode(zipcode.get())
                .street(street.get())
                .buildingName(buildingName.get())
                .isMapTouched(true)
                .build()

        compositeDisposable.add(dataManager.doCreateListing(request)
                .doOnSubscribe {
                }
                .doFinally {
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        { response ->
                            val data = response.data()
                            try {
                                if (data?.createListing()?.status() == 200) {
                                    retryCalled = ""
                                    val actionType = data?.createListing()!!.actionType()
                                    if (actionType.equals("create")) {
                                        retryCalled = ""
                                        listId.set(data.createListing()!!.id().toString())
                                        amenitiedId.value = selectedAmenities
                                        spacesId.value = selectedSpace
                                        aafetyAmenitiedId.value = selectedDetectors
                                        bedTypesId.value = selectedBeds
                                        lat.set(data.createListing()!!.results()!!.lat().toString())
                                        lng.set(data.createListing()!!.results()!!.lng().toString())
                                        manageSteps()
                                        Handler().postDelayed({
                                            isNext.set(false)
                                            lottieProgress.set(StepOneViewModel.LottieProgress.NORMAL)
                                            onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                                        }, 2000)
                                    }
                                } else if (data?.createListing()!!.status() == 500) {
                                    navigator.openSessionExpire()
                                } else {
                                    navigator.showError()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navigator.showError(e)
                            }
                        },
                        {
                            navigator.showSnackbar(resourceProvider.getString(R.string.error), resourceProvider.getString(R.string.currently_offline))
                        }
                ))
    }

    fun updateHostStepOne(flag: Boolean = false) {
        try {
            bedTypesArray.clear()
            val lati = lat.get().toString()
            val longi = lng.get().toString()

            updateCount.value!!.forEachIndexed { index, s ->
                if (s.equals("0").not()) {
                    val temp = HashMap<String, String>()
                    temp.put("bedCount", s)
                    temp.put("bedType", defaultselectedBedTypeId.get(index))
                    bedTypesArray.add(temp)
                }
            }
            var building = ""
            if (buildingName.get().isNullOrEmpty().not()) {
                building = buildingName.get()!!
            }
            var bath = becomeHostStep1.value!!.bathroomCount!!.toString()
            if (bath.contains(",")) {
                bath = bath.replace(",", ".")
            }
            val jsonVal = JSONArray(bedTypesArray)
            val request = CreateListingMutation
                    .builder()
                    .roomType(becomeHostStep1.value!!.placeType)
                    .personCapacity(becomeHostStep1.value!!.totalGuestCount!!.toInt())
                    .residenceType(becomeHostStep1.value!!.yesNoOptions)
                    .buildingSize(becomeHostStep1.value!!.roomCapacity)
                    .bathroomType(becomeHostStep1.value!!.bathroomSpace)
                    .bathrooms(bath.toDouble())
                    .beds(becomeHostStep1.value!!.beds!!.toInt())
                    .bedTypes(jsonVal.toString())
                    .bedType("17")
                    .bedrooms(becomeHostStep1.value!!.bedroomCount)
                    .city(city.get())
                    .amenities(selectedAmenities.toList())
                    .houseType(becomeHostStep1.value!!.houseType)
                    .safetyAmenities(selectedDetectors.toList())
                    .spaces(selectedSpace.toList())
                    .state(state.get())
                    .country(countryCode.get())
                    .zipcode(zipcode.get())
                    .street(street.get())
                    .buildingName(building)
                    .lat(lati.toDouble())
                    .lng(longi.toDouble())
                    .listId(listId.get()!!.toInt())
                    .isMapTouched(true)
                    .build()

            compositeDisposable.add(dataManager.doCreateListing(request)
                    .doOnSubscribe { setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe(
                            { response ->
                                val data = response.data()
                                try {
                                    if (data?.createListing()?.status() == 200) {
                                        val actionType = data?.createListing()!!.actionType()
                                        retryCalled = ""
                                        if (actionType.equals("update")) {
                                            amenitiedId.value = selectedAmenities
                                            spacesId.value = selectedSpace
                                            aafetyAmenitiedId.value = selectedDetectors
                                            bedTypesId.value = selectedBeds
                                            lat.set(lati)
                                            lng.set(longi)
                                            Log.d("updated ", "lati longi is " + data?.createListing()?.results()!!.lat() + " " + data?.createListing()?.results()!!.lng())
                                            if (flag) {
                                                manageSteps(flag)
                                            } else {
                                                navigator.navigateScreen(NextScreen.FINISHED)
                                            }
                                        }

                                    } else if (data?.createListing()!!.status() == 500) {
                                        navigator.openSessionExpire()
                                    } else {
                                        navigator.showSnackbar(resourceProvider.getString(R.string.error),response?.errors()!![0].message().toString())
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    navigator.showError(e)
                                }
                            },
                            {
                                handleException(it)
                            }
                    ))
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError(e)
        }
    }

    fun getListingSetting(from: String) {
        val request = GetListingSettingQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.doGetListingSettings(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        { response ->
                            val data = response.data()
                            try {
                                if (data?.listingSettings?.status() == 200) {
                                    val result = data!!.listingSettings!!.results()
                                    listSetting.value = result
                                    if (from.equals("edit")) {
                                        personCapacity.value = result!!.personCapacity()
                                        roomtypelist.value = result!!.roomType()
                                        housetypelist.value = result!!.houseType()
                                        roomSizelist.value = result!!.buildingSize()
                                        bedTypelist.value = result!!.bedType()
                                        bedroomlist.value = result.bedrooms()
                                        beds.value = result!!.beds()
                                        bedCount.value = result!!.beds()
                                        bathroomlist.value = result!!.bathroomType()
                                        amenitiesList.value = result!!.amenities()
                                        safetyAmenitiesList.value = result!!.safetyAmenities()
                                        sharedSpaceList.value = result!!.spaces()
                                        yesNoType.value = yesNoString.get()
                                        noOfBathroom.value = result!!.bathrooms()
                                        //setData(result!!.bedType(), "edit")
                                    } else {
                                        defaultSettings.value = result
                                        roomtypelist.value = result!!.roomType()
                                        roomType.value = result!!.roomType()!!.listSettings()!![0].itemName()!!
                                        capacity.value = resourceProvider.getString(R.string.For)+" 1 " + result.personCapacity()!!.listSettings()!![0].itemName()!!
                                        personCapacity.value = result!!.personCapacity()
                                        housetypelist.value = result!!.houseType()
                                        houseType.value = result!!.houseType()!!.listSettings()!![0].itemName()!!
                                        guestPlaceType.value = result!!.roomType()!!.listSettings()!![0].itemName()!!
                                        roomSizelist.value = result!!.buildingSize()
                                        roomSizeType.value = result!!.buildingSize()!!.listSettings()!![0].itemName()!!
                                        yesNoType.value = "Yes"
                                        beds.value = result!!.beds()
                                        bedroomlist.value = result!!.bedrooms()
                                        bedTypelist.value = result!!.bedType()
                                        bathroomlist.value = result!!.bathroomType()
                                        noOfBathroom.value = result!!.bathrooms()
                                        bathroomType.value = result!!.bathroomType()!!.listSettings()!![0].itemName()!!
                                        bedCount.value = result!!.beds()
                                        noOfBed.value = result!!.beds()!!.listSettings()!![0].itemName()!!
                                        amenitiesList.value = result!!.amenities()
                                        safetyAmenitiesList.value = result!!.safetyAmenities()
                                        sharedSpaceList.value = result!!.spaces()
                                        setStepOne(result)
                                    }
                                    setData(result!!.bedType(), "add")
                                    if (from.equals("edit")) {
                                        getStep1ListingDetails()
                                    } else {
                                        getCountryCode()
                                    }

                                } else if (data?.listingSettings!!.status() == 500) {
                                    navigator.openSessionExpire()
                                } else {
                                    navigator.showError()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                navigator.showError(e)
                            }
                        },
                        {
                            handleException(it)
                        }
                ))
    }

    private fun setStepOne(result: GetListingSettingQuery.Results?) {
        becomeHostStep1.value = BecomeHostStep1(
                placeType = result!!.roomType()!!.listSettings()!![0].id().toString()!!,
                guestCapacity = result!!.personCapacity()!!.listSettings()!![0].id().toString()!!,
                houseType = result!!.houseType()!!.listSettings()!![0].id().toString()!!,
                guestSpace = result!!.roomType()!!.listSettings()!![0].id().toString()!!,
                roomCapacity = result!!.buildingSize()!!.listSettings()!![0].id().toString()!!,
                yesNoOptions = yesNoString!!.get(),
                totalGuestCount = 1,
                bedroomCount = "1",
                bedCount = 1,
                bedType = "",
                bedTypes = "",
                beds = 1,
                bathroomCount = 1.0,
                bathroomSpace = result!!.bathroomType()!!.listSettings()!![0].id().toString()!!,
                country = "",
                street = "",
                state = "",
                zipcode = "",
                isMapTouched = false,
                buildingName = "",
                lat = 0.0,
                lng = 0.0,
                amentiesSelected = arrayListOf(),
                safetyAmentiesSelected = arrayListOf(),
                guestSpacesSelected = arrayListOf())
    }

    fun getStep1ListingDetails() {
        val format = DecimalFormat("0.#")
        val buildQuery = GetStep1ListingDetailsQuery
                .builder()
                .listId(listId.get().toString())
                .preview(true)
                .build()
        compositeDisposable.add(dataManager.doGetStep1ListingDetailsQuery(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.listingDetails
                        if (data?.status() == 200) {
                            isListAdded = true
                            retryCalled = ""
                            var bathroom = data.results()!!.bathrooms()
//                            if (bathroom.contains(",")) {
//                                bathroom = bathroom.replace(",", ".")
//                            }
                           try {
                               var roomtype = ""
                               var houseType = ""
                               if(data.results()!!.settingsData()!!.size>0){
                                   if(data.results()!!.settingsData()!![0].listsettings() == null){
                                       roomtype = listSetting.value?.roomType()!!.listSettings()!![0].id().toString()!!
                                   }else{
                                       roomtype = data.results()!!.settingsData()!![0].listsettings()!!.id().toString()
                                   }
                               }
                               becomeHostStep1.value = BecomeHostStep1(
                                       placeType = roomtype,
                                       guestCapacity = data.results()!!.personCapacity().toString(),
                                       houseType = data.results()!!.settingsData()!![1].listsettings()!!.id().toString(),
                                       guestSpace = roomtype,
                                       roomCapacity = data.results()!!.settingsData()!![2].listsettings()!!.id().toString(),
                                       yesNoOptions = data.results()!!.residenceType().toString(),
                                       totalGuestCount = data.results()!!.personCapacity(),
                                       bedroomCount = data.results()!!.bedrooms().toString(),
                                       beds = data.results()!!.beds(),
                                       bedCount = data.results()!!.beds(),
                                       bedType = "",
                                       bedTypes = arrayListOf(showbedTypesId).toString(),
                                       bathroomCount = bathroom,
                                       bathroomSpace = data.results()!!.settingsData()!![3].listsettings()!!.id().toString(),
                                       country = data.results()!!.country().toString(),
                                       street = data.results()!!.street().toString(),
                                       state = data.results()!!.state().toString(),
                                       zipcode = data.results()!!.zipcode()!!,
                                       isMapTouched = true,
                                       buildingName = data.results()!!.buildingName().toString(),
                                       lat = data.results()!!.lat(),
                                       lng = data.results()!!.lng(),
                                       amentiesSelected = arrayListOf(showAmentiesId),
                                       safetyAmentiesSelected = arrayListOf(showSafetyAmentiesId),
                                       guestSpacesSelected = arrayListOf(showSpacesId))

                               setPrefilledData(data)
                               setCountry(data.results()!!.country().toString())
                               navigator.navigateScreen(NextScreen.KIND_OF_PLACE)
                           }catch (e: Exception){
                               e.printStackTrace()
                           }

                        } else if (data?.status() == 500) {
                            isListAdded = false
                            navigator.openSessionExpire()
                        } else {
                            isListAdded = false
                            navigator.show404Page()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError(e)
                    }
                }, {

                    it.printStackTrace()
                    handleException(it)
                })
        )
    }

    fun setCountry(county: String) {
        getCountryCode()
    }

    private fun setPrefilledData(data: GetStep1ListingDetailsQuery.GetListingDetails) {
        try {
            var bathroom = data.results()!!.bathrooms()!!.toDouble().toString()
            if (bathroom.contains(",")) {
                bathroom = bathroom.replace(",", ".")
            }
            var roomtype = ""
            if(data.results()!!.settingsData()!![0].listsettings() == null){
                roomtype = listSetting.value?.roomType()!!.listSettings()!![0].itemName()!!
            }else{
                roomtype = data.results()!!.settingsData()!![0].listsettings()!!.itemName()!!
            }
            roomType.value = roomtype
            capacity.value = data.results()!!.personCapacity().toString()
            houseType.value = data.results()!!.settingsData()!![1].listsettings()?.itemName().toString()
            roomSizeType.value = data.results()!!.settingsData()!![2].listsettings()?.itemName().toString()
            guestCapacity.value = data.results()!!.personCapacity().toString()
            roomNoCapacity.value = data.results()!!.bedrooms().toString()
            bedNoCapacity.value = data.results()!!.beds().toString()
            //totalBedCount = data.results()!!.beds()!!.toInt()
            bathroomCount.value = bathroom
            bathroomType.value = data.results()!!.settingsData()!![3].listsettings()!!.itemName().toString()
            country.set(data.results()!!.country().toString())
            countryCode.set(data.results()!!.country().toString())
            city.set(data.results()!!.city().toString())
            if (data.results()!!.residenceType().equals("1")) {
                yesNoString.set("Yes")
                yesNoType.value = yesNoString.get()
            } else {
                yesNoString.set("No")
                yesNoType.value = yesNoString.get()
            }
            if (data.results()!!.buildingName().toString().isNullOrEmpty()) {
                buildingName.set("")
            } else {
                buildingName.set(data.results()!!.buildingName().toString())
            }
            street.set(data.results()!!.street().toString())
            state.set(data.results()!!.state().toString())
            zipcode.set(data.results()!!.zipcode()!!)
            lat.set(data.results()!!.lat().toString())
            lng.set(data.results()!!.lng().toString())
            if (data!!.results()!!.buildingName().isNullOrEmpty()) {
                buildingName.set("")
            }
            data!!.results()!!.userAmenities()!!.forEachIndexed { i, userAmenity ->
                showAmentiesId = data!!.results()!!.userAmenities()!![i].id()
                showAmentiesId?.let { selectedAmenities.add(it) }
            }
            data!!.results()!!.userSafetyAmenities()!!.forEachIndexed { i, safetyAmenity ->
                showSafetyAmentiesId = data!!.results()!!.userSafetyAmenities()!![i].id()
                showSafetyAmentiesId?.let { selectedDetectors.add(it) }
            }
            data!!.results()!!.userSpaces()!!.forEachIndexed { i, userSpace ->
                showSpacesId = data!!.results()!!.userSpaces()!![i].id()
                showSpacesId?.let { selectedSpace.add(it) }
            }
            var userBedsAPI = data.results()!!.userBedsTypes()
            defaultselectedBedTypeId.forEachIndexed { index, s ->
                userBedsAPI?.forEachIndexed { i, userBedsType ->
                    if (s.equals(userBedsType.bedType().toString())) {
                        updateCount.value!!.removeAt(index)
                        updateCount.value!!.add(index, userBedsType.bedCount().toString())
                    }
                }
            }
            updateCount.value?.forEachIndexed { index, s ->
                editBedCount = editBedCount + s.toInt()
            }
            totalBedCount = editBedCount
        } catch (e: Exception) {
            navigator.showError(e)
        }
    }

    fun manageSteps(flag: Boolean = false) {
        val buildQuery = ManageListingStepsMutation
                .builder()
                .listId(listId.get().toString())
                .currentStep(1)
                .build()
        compositeDisposable.add(dataManager.doManageListingSteps(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.ManageListingSteps()
                        if (data!!.status() == 200) {
                            if (flag) {
                                navigator.navigateScreen(NextScreen.FINISHED)
                            }
                        } else if (data.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            data.errorMessage()?.let {
                                navigator.showToast(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError(e)
                    }
                }, {
                    it.printStackTrace()
                    handleException(it)
                })
        )
    }

    fun getLocationFromAddress(strAddress: String): String? {
        Log.d("   ", " strAddress ::  " + strAddress)
        val coder = Geocoder(resourceProvider.context)
        val address: List<Address>?
        try {
            address = coder.getFromLocationName(strAddress, 1)
            if (address == null || address.isEmpty()) {
                return null
            }
            val loction = address!![0]
            val lati = loction.latitude
            val lngi = loction.longitude
            val locaton = LatLng(lati, lngi)
            lat.set(lati.toString())
            lng.set(lngi.toString())
            //          location.set(locaton.toString())

            return "$lat,$lng"

        } catch (e: Exception) {
            return null
        }

    }

    private fun setData(bedType: GetListingSettingQuery.BedType?, from: String) {
        val list = ArrayList<PersonCount>()
        val countList = ArrayList<String>()

        for (i in 0 until bedType!!.listSettings()!!.size) {
            list.add(PersonCount(
                    itemId = bedType!!.listSettings()!![i].id()!!,
                    itemName = bedType!!.listSettings()!![i].itemName()!!,
                    startValue = bedCount.value!!.listSettings()!![0].startValue()!!,
                    endValue = bedCount.value!!.listSettings()!![0].endValue()!!,
                    updatedCount = 0
            ))

            countList.add("0")
            defaultselectedBedTypeId.add(bedType!!.listSettings()!![i].id().toString()!!)
            Log.d("default ", "beds id are :: " + defaultselectedBedTypeId)
        }
        updateCount.value = countList
        typeOfBeds.value = list

        if (from.equals("add")) {
            updateCount.value = countList
        }
    }

    fun getCountryCode() {
        val query = GetCountrycodeQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCountryCode(query)
                .doOnSubscribe { }
                .doFinally { }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.countries
                        if (data?.status() == 200) {
                            isCountryCodeLoad.set(true)
                            list.value = response.data()!!.countries!!.results()

                        }else if(data!!.status() == 500) {
                            (navigator as BaseNavigator).openSessionExpire()
                        }
                        else {
                            isCountryCodeLoad.set(false)
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        isCountryCodeLoad.set(false)
                        e.printStackTrace()
                        navigator.showError(e)
                    }
                }, {
                    isCountryCodeLoad.set(false)
                    handleException(it)
                })
        )
    }

    fun step1Retry(strUser: String?) {
        if (strUser.isNullOrEmpty()) {
            getListingSetting("add")
//            getCountryCode()
        } else {
            getListingSetting("edit")
//            getStep1ListingDetails()
//            getCountryCode()
        }

    }

    fun onSearchTextChanged(text: CharSequence) {
        if (text.isNotEmpty()) {
            val searchText = text.toString().capitalize()
            val containsItem = ArrayList<GetCountrycodeQuery.Result>()
            list.value?.forEachIndexed { _, result ->
                result.countryName()?.let {
                    if (it.contains(searchText)) {
                        containsItem.add(result)
                    }
                }
            }
            listSearch.value = containsItem
        } else {
            list.value?.let {
                listSearch.value = ArrayList(it)
            }
        }
    }
}