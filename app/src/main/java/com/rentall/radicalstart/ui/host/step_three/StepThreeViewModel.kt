package com.rentall.radicalstart.ui.host.step_three

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.ListDetailsStep3
import javax.inject.Inject

class StepThreeViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<StepThreeNavigator>(dataManager,resourceProvider){

    var listID : String = ""

    var retryCalled = ""

    var baseCurrency = ""

    val selectedRules = ArrayList<Int?>()

    var isSnackbarShown : Boolean = false

    var fromChoosen : String = "Flexible"

    var toChoosen : String = "Flexible"

    val availOptions = arrayOf("unavailable","3months","6months","9months","12months","available")

    val fromTime = arrayOf("Flexible","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25")

    val toTime =arrayOf("Flexible","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26")

    val fromOptions = arrayOf("Flexible","8AM","9AM","10AM","11AM","12PM(noon)","1PM","2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM","12AM (mid night)","1AM (next day)")

    val toOptions = arrayOf("Flexible","9AM","10AM","11AM","12PM(noon)","1PM","2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM","12AM (mid night)","1AM (next day)","2PM (next day)")

    val datesAvailable = arrayOf(
            resourceProvider.getString(R.string.unavailable),resourceProvider.getString(R.string.three_month),resourceProvider.getString(R.string.six_month),
            resourceProvider.getString(R.string.nine_month),resourceProvider.getString(R.string.twelve_month),resourceProvider.getString(R.string.available_by_default)
    )

    val cancellationPolicy = arrayOf(resourceProvider.getString(R.string.flexible),resourceProvider.getString(R.string.moderate),resourceProvider.getString(R.string.strict))

    var isListAdded : Boolean = false

    val currency = MutableLiveData<GetCurrenciesListQuery.GetCurrencies>()

    var noticeTime : String? = ""
    var noticeFrom : String? = ""
    var noticeTo : String? =""
    var cancelPolicy : Int? = 1
    var bookWind : String? = ""
    var bookingType : String = "instant"


    val minNight = ObservableField<String>()
    val maxNight = ObservableField<String>()

    val basePrice = ObservableField("")
    val cleaningPrice = ObservableField("")
    val weekDiscount = ObservableField("")
    val monthDiscount = ObservableField("")

    val selectedCurrency = MutableLiveData<String>()
    var selectArray = arrayOf(true, false)


    lateinit var listSettingArray : MutableLiveData<GetListingSettingQuery.Results>
    var listSettingArrayTemp : MutableLiveData<GetListingSettingQuery.Results> = MutableLiveData()

    lateinit var listDetailsArray : GetListingDetailsStep3Query.Results

    lateinit var listDetailsStep3 : MutableLiveData<ListDetailsStep3>

    fun listSetting() : MutableLiveData<GetListingSettingQuery.Results>{
        if (!::listSettingArray.isInitialized) {
            listSettingArray = MutableLiveData()
            listDetailsStep3 = MutableLiveData()
            getListStep3Details()
        }
        return listSettingArray
    }


    enum class NextStep{
        HOUSERULE,
        GUESTBOOK,
        GUESTNOTICE,
        BOOKWINDOW,
        TRIPLENGTH,
        LISTPRICE,
        DISCOUNTPRICE,
        INSTANTBOOK,
        LAWS,
        FINISH,
        NODATA
    }

    fun getListingSetting(){

        val buildQuery = GetListingSettingQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.dogetListingSettings(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()
                    try {
                        if (data?.listingSettings!!.status() == 200) {
                            val result = data.listingSettings!!.results()
                            listSettingArrayTemp.value = result
                            getCurrency()
                        } else if(data?.listingSettings!!.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, { handleException(it) } )
        )

    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCurrencyList(query)
                .doOnSubscribe { setIsLoading(true)}
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()!!.currencies
                    if (data?.status() == 200) {
                        currency.value = data
                        listSettingArray.value= listSettingArrayTemp.value
                        val currencyResult = currency.value!!.results()
                        currencyResult!!.forEachIndexed { index, cresult ->
                            if(cresult.isBaseCurrency!!){
                                baseCurrency = cresult.symbol()!!
                            }
                        }
                        if(isListAdded){
                            setDatafromAPI()
                        }else {
                            setData()
                        }
                    } else if(data?.status() == 500) {
                        navigator.openSessionExpire()
                    } else {
                        //   postsOutcome.value = Event(Outcome.failure(recommendListing.status().toString()))
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    fun getListStep3Details(){
        val buildQuery = GetListingDetailsStep3Query
                .builder()
                .listId(listID)
                .preview(true)
                .build()
        compositeDisposable.add(dataManager.doGetStep3Details(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()!!.listingDetails
                    try {
                        if (data?.status() == 200) {
                            if(data.results()!!.listingData() != null) {
                                isListAdded = true
                                retryCalled = ""
                                listDetailsArray = data!!.results()!!
                                getListingSetting()
                            }else{
                                isListAdded =false
                                getListingSetting()
                            }
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            isListAdded = false
                            //getListingSetting()
                            navigator.show404Page()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, { handleException(it) } )
        )
    }

    fun updateListStep3(from: String){
        var cleanPri : Double? = null
        var weekDis : Int? = null
        var monthDis : Int? = null
        if(!cleaningPrice.get().isNullOrEmpty()){
            cleanPri = cleaningPrice.get()!!.toDouble()
        }
        if (!weekDiscount.get().isNullOrEmpty()){
            weekDis = weekDiscount.get()!!.toInt()
        }
        if(!monthDiscount.get().isNullOrEmpty()){
            monthDis = monthDiscount.get()!!.toInt()
        }

        if(checkChoosenTime()){
            val query = UpdateListingStep3Mutation
                    .builder()
                    .id(listID.toInt())
                    .houseRules(selectedRules)
                    .bookingNoticeTime(noticeTime)
                    .checkInStart(fromChoosen)
                    .checkInEnd(toChoosen)
                    .maxDaysNotice(bookWind)
                    .minNight(minNight.get()!!.toInt())
                    .maxNight(maxNight.get()!!.toInt())
                    .basePrice(basePrice.get()!!.toDouble())
                    .cleaningPrice(cleanPri)
                    .currency(listDetailsStep3.value!!.currency)
                    .weeklyDiscount(weekDis)
                    .monthlyDiscount(monthDis)
                    .bookingType(bookingType)
                    .cancellationPolicy(cancelPolicy)
                    .build()
            compositeDisposable.add(dataManager.doUpdateListingStep3(query)
                    .doOnSubscribe { setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        val data = response.data()?.updateListingStep3()
                        try {
                            if (data?.status() == 200) {
                                retryCalled = ""
//                            if(from.equals("edit")) {
//                                navigator.navigateToScreen(NextStep.FINISH)
//                            }else{
                                updateStepDetails()
//                            }
                                //navigator.navigateToScreen(StepThreeViewModel.NextStep.FINISH)
                            } else if(data?.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                navigator.navigateToScreen(NextStep.FINISH)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }, { handleException(it) } )
            )
        }
    }

    fun updateStepDetails(){
        val buildQuery = ManageListingStepsMutation
                .builder()
                .listId(listID)
                .currentStep(3)
                .build()
        compositeDisposable.add(dataManager.doManageListingSteps(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()?.ManageListingSteps()
                    try {
                        if (data?.status() == 200) {
                            navigator.navigateToScreen(NextStep.FINISH)
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else if(data?.status()==400){
                            navigator.showSnackbar(resourceProvider.getString(R.string.update_error),data.errorMessage().toString())
                        }else{
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, { handleException(it) } )
        )
    }

    fun setData(){
        listDetailsStep3.value = ListDetailsStep3(
                noticePeriod = listSettingArray.value!!.bookingNoticeTime()!!.listSettings()!![0]!!.itemName(),
                noticeFrom =  fromOptions.get(0),
                noticeTo = toOptions.get(0),
                availableDate = datesAvailable.get(datesAvailable.size-1),
                cancellationPolicy = cancellationPolicy.get(0),
                minStay =  listSettingArray.value!!.minNight()!!.listSettings()!![0]!!.startValue().toString(),
                maxStay = listSettingArray.value!!.minNight()!!.listSettings()!![0]!!.startValue().toString(),
                basePrice = 0.0,
                cleaningPrice = 0.0,
                currency = baseCurrency,
                weekDiscount = 0,
                monthDiscount = 0
        )

        minNight.set(listSettingArray.value!!.minNight()!!.listSettings()!![0]!!.startValue().toString())
        maxNight.set(listSettingArray.value!!.minNight()!!.listSettings()!![0]!!.startValue().toString())
        bookWind = availOptions.get(availOptions.size-1)
        noticeTime = listSettingArray.value!!.bookingNoticeTime()!!.listSettings()!![0]!!.id().toString()
    }

    fun setDatafromAPI(){
        try {
            var bookingTime : String = ""
            var bookID: String = ""
            var bookCheck24: String = ""
            var bookout24: String = ""
            var bookcheckIn : String = ""
            var bookCheckOut : String = ""
            var noticeDur : String = ""
            var cancella : String = ""
            var noticeDur1: String = ""
            if(listDetailsArray.listingData()!!.bookingNoticeTime()!!.equals("")){
                bookingTime = listSettingArray.value!!.bookingNoticeTime()!!.listSettings()!![0].itemName()!!
                bookID =listSettingArray.value!!.bookingNoticeTime()!!.listSettings()!![0].id()!!.toString()
            }else {
                listSettingArray.value!!.bookingNoticeTime()!!.listSettings()!!.forEachIndexed { index, listSetting15 ->
                    if (listSetting15.id()!! == listDetailsArray.listingData()!!.bookingNoticeTime()!!.toInt()) {
                        bookingTime = listSetting15.itemName().toString()
                        bookID = listSetting15.id().toString()
                    }
                }
            }

            noticeTime = bookID

            if(listDetailsArray.houseRules()!!.isNotEmpty()){
                val list = listDetailsArray.houseRules()!!
                list.forEachIndexed { index, houseRule ->
                    selectedRules.add(houseRule.id())
                }
            }

            fromTime.forEachIndexed { index, s ->
                if(s.equals(listDetailsArray.listingData()!!.checkInStart())){
                    bookcheckIn = fromOptions.get(index)
                    bookCheck24 = fromTime.get(index)
                }
            }

            noticeFrom = bookcheckIn

            fromChoosen = bookCheck24

            toTime.forEachIndexed { index, s ->
                if(s.equals(listDetailsArray.listingData()!!.checkInEnd())){
                    bookCheckOut = toOptions.get(index)
                    bookout24 = toTime.get(index)
                }
            }
            noticeTo = bookCheckOut
            toChoosen = bookout24

            availOptions.forEachIndexed { index, s ->
                if(s.equals(listDetailsArray.listingData()!!.maxDaysNotice())){
                    noticeDur = datesAvailable.get(index)
                    noticeDur1 = availOptions.get(index)
                }
            }
            bookWind = noticeDur1
            cancelPolicy=listDetailsArray.listingData()!!.cancellationPolicy()
            minNight.set(listDetailsArray.listingData()!!.minNight().toString())
            maxNight.set(listDetailsArray.listingData()!!.maxNight().toString())
            var base : Double = 0.0
            var clean: Double = 0.0
            var week: Int = 0
            var month: Int = 0
            if(listDetailsArray.listingData()!!.basePrice() == null){
                basePrice.set("")
                base = 0.0
            }else {
                basePrice.set(Utils.formatDecimal(listDetailsArray.listingData()!!.basePrice()!!))
            }

            if(listDetailsArray.listingData()!!.cleaningPrice() == null){
                cleaningPrice.set("")
                clean = 0.0
            }else {
                cleaningPrice.set(Utils.formatDecimal(listDetailsArray.listingData()!!.cleaningPrice()!!))
            }

            if(listDetailsArray!!.listingData()!!.weeklyDiscount() == null){
                weekDiscount.set("")
                week = 0
            }else {

                weekDiscount.set(listDetailsArray!!.listingData()!!.weeklyDiscount().toString())
                try {
                    if (weekDiscount.get()!!.toInt() == 0) {
                        weekDiscount.set("0")
                    }
                }catch (e: Exception){

                }
                week = weekDiscount.get()!!.toInt()
            }

            if(listDetailsArray!!.listingData()!!.monthlyDiscount() == null){
                monthDiscount.set("")
                month = 0
            }else {
                monthDiscount.set(listDetailsArray!!.listingData()!!.monthlyDiscount().toString())
                try {
                    if (monthDiscount.get()!!.toInt() == 0) {
                        monthDiscount.set("0")
                    }
                }catch (e: Exception){

                }
                month = monthDiscount.get()!!.toInt()
            }
            val cancelIndex =listDetailsArray!!.listingData()!!.cancellationPolicy()!!

            bookingType = listDetailsArray!!.bookingType()!!

            if(bookingType.equals("instant")){
                selectArray.set(0,true)
                selectArray.set(1,false)
            }else{
                selectArray.set(0,false)
                selectArray.set(1,true)
            }

            listDetailsStep3.value = ListDetailsStep3(
                    noticePeriod = bookingTime,
                    noticeFrom =  noticeFrom,
                    noticeTo = noticeTo,
                    availableDate = noticeDur,
                    cancellationPolicy = cancellationPolicy.get(cancelIndex-1),
                    minStay =  minNight.get(),
                    maxStay = maxNight.get(),
                    basePrice = base,
                    cleaningPrice = clean,
                    currency = listDetailsArray!!.listingData()!!.currency(),
                    weekDiscount = week,
                    monthDiscount = month
            )
        } catch (E: Exception) {
            navigator.showError()
        }
    }

    fun onClick(screen : StepThreeViewModel.NextStep){
        Log.d("Sceen",screen.toString())
        if(screen == NextStep.DISCOUNTPRICE){
            if(checkPrice()){
                navigator.navigateToScreen(screen)
            }
        }else if(screen == NextStep.INSTANTBOOK){
            if(checkDiscount()){
                navigator.navigateToScreen(screen)
            }
        }
        else {
            navigator.navigateToScreen(screen)
        }
    }

    fun checkPrice(): Boolean{

        if (basePrice.get().isNullOrEmpty()) {
            isSnackbarShown = true
            navigator.showSnackbar(resourceProvider.getString(R.string.add_price),  resourceProvider.getString(R.string.base_price_error))
        } else if (basePrice.get().equals("0") || basePrice.get().equals("0.0")) {
            isSnackbarShown = true
            navigator.showSnackbar(resourceProvider.getString(R.string.add_price), resourceProvider.getString(R.string.base_price_error))
        } else {
            try{
            val price = basePrice.get()!!.toDouble()
            if (price >= 1) {
                var price = basePrice.get()
                var occrence = 0
                for (i in 0 until price!!.length) {
                    val pr = price[i]
                    val ch = '.'
                    if (ch == pr) {
                        occrence++
                    }
                }
                if (occrence == 0 || occrence == 1) {
                    if (!cleaningPrice.get().isNullOrEmpty()) {
                        try {
                            var price = cleaningPrice.get()
                            var occ = 0
                            for (i in 0 until price!!.length) {
                                val pr = price[i]
                                val ch = '.'
                                if (ch == pr) {
                                    occ++
                                }
                            }

                        if (occ == 0 || occ == 1) {
                            navigator.hideKeyboard()
                            navigator.hideSnackbar()
                            val data = listDetailsStep3.value
                            data?.basePrice = basePrice.get()!!.toDouble()
                            if (cleaningPrice.get().isNullOrEmpty()) {
                                data?.cleaningPrice = null
                            } else {
                                data?.cleaningPrice = cleaningPrice.get()!!.toDouble()
                            }
                            listDetailsStep3.value = data
                            return true
                            //navigator.navigateToScreen(NextStep.DISCOUNTPRICE)
                        } else {
                            navigator.showSnackbar(resourceProvider.getString(R.string.add_price), resourceProvider.getString(R.string.cleaning_price_error))
                        }
                        }catch (e: Exception){
                            navigator.showSnackbar(resourceProvider.getString(R.string.add_price), resourceProvider.getString(R.string.cleaning_price_error))
                        }
                    } else {
                        navigator.hideKeyboard()
                        navigator.hideSnackbar()
                        val data = listDetailsStep3.value
                        data?.basePrice = basePrice.get()!!.toDouble()
                        if (cleaningPrice.get().isNullOrEmpty()) {
                            data?.cleaningPrice = null
                        } else {
                            data?.cleaningPrice = cleaningPrice.get()!!.toDouble()
                        }
                        listDetailsStep3.value = data
                        //navigator.navigateToScreen(NextStep.DISCOUNTPRICE)
                        return true
                    }

                } else {
                    isSnackbarShown = true
                    navigator.showSnackbar(resourceProvider.getString(R.string.add_price), resourceProvider.getString(R.string.price_error_text))
                }
            } else {
                navigator.showSnackbar(resourceProvider.getString(R.string.add_price), resourceProvider.getString(R.string.price_error_text))
            }
        }catch(e: NumberFormatException){
                navigator.showSnackbar(resourceProvider.getString(R.string.add_price), resourceProvider.getString(R.string.price_error_text))
            }
        }
        return false
    }

    fun checkDiscount(): Boolean{
        val data = listDetailsStep3.value
        var weekCheck: Boolean = false
        var monthCheck: Boolean = false
        if (!weekDiscount.get().equals("")) {
            if (weekDiscount.get()!!.toLong() >= 100) {
                isSnackbarShown = true
                navigator.showSnackbar(resourceProvider.getString(R.string.discounts), resourceProvider.getString(R.string.discount_error_text))
                return false
            } else {
                weekCheck = true
                data?.weekDiscount = weekDiscount.get()!!.toInt()
            }
        } else {
            weekCheck = true
            data?.weekDiscount = null
        }
        if (!monthDiscount.get().equals("")) {
            if (monthDiscount.get()!!.toLong() >= 100) {
                isSnackbarShown = true
                navigator.showSnackbar(resourceProvider.getString(R.string.discounts), resourceProvider.getString(R.string.discount_error_text))
                return false
            } else {
                monthCheck = true
                data?.monthDiscount = monthDiscount.get()!!.toInt()
            }
        } else {
            monthCheck = true
            data?.monthDiscount = null
        }
        if (weekCheck && monthCheck) {
           return true
        }
        return false
    }
    fun checkTripLength(): Boolean{
        if (minNight.get()!!.toInt() == 0 && maxNight.get()!!.toInt() == 0) {
            navigator.hideSnackbar()
            return true
        } else {
            if(maxNight.get()!!.toInt() != 0) {
                if ((minNight.get()!!.toInt() > maxNight.get()!!.toInt())) {
                    isSnackbarShown = true
                    navigator.showSnackbar(resourceProvider.getString(R.string.trip_length), resourceProvider.getString(R.string.stay_error_text))
                    isSnackbarShown = true
                } else {
                    navigator.hideSnackbar()
                    return true
                }
            }else {
                navigator.hideSnackbar()
                return true
            }
        }
        return false
    }

    fun checkChoosenTime() : Boolean{
        if(fromChoosen=="Flexible" || toChoosen=="Flexible"){
            retryCalled = "update"
            return true
        }else{
            if (fromChoosen.toInt() >= toChoosen.toInt()) {
                isSnackbarShown = true
                navigator.showSnackbar(resourceProvider.getString(R.string.time), resourceProvider.getString(R.string.checkin_error_text))
            }else{
                retryCalled = "update"
                return true
            }
        }
        return false
    }


}