package com.rentall.radicalstart.ui.booking

import android.content.Intent
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.ObservableField
import androidx.databinding.library.R
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.GetCurrenciesListQuery
import com.rentall.radicalstart.GetProfileQuery
import com.rentall.radicalstart.GetReservationQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.BillingDetails
import com.rentall.radicalstart.vo.ListingInitData
import timber.log.Timber
import javax.inject.Inject

class BookingViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BookingNavigator>(dataManager, resourceProvider) {

    val billingDetails = MutableLiveData<BillingDetails>()

    var listDetails = ListingInitData()

    val msg = ObservableField("")

    val avatar = MutableLiveData<String>()

    val reservation = MutableLiveData<GetReservationQuery.Results>()

    var isUploading = false


    fun setPictureInPref(pic: String?) {
        dataManager.currentUserProfilePicUrl = pic
    }



    fun setInitialData(intent: Intent) {
        try {
            listDetails = intent.getParcelableExtra("lisitingDetails")!!
            billingDetails.value = BillingDetails(
                    checkIn = intent.getStringExtra("checkIn").orEmpty(),
                    checkOut = intent.getStringExtra("checkOut").orEmpty(),
                    basePrice = intent.getDoubleExtra("basePrice", 0.0),
                    nights = intent.getIntExtra("nights", 0),
                    guestServiceFee = intent.getDoubleExtra("guestServiceFee", 0.0),
                    cleaningPrice = intent.getDoubleExtra("cleaningPrice", 0.0),
                    discount = intent.getDoubleExtra("discount", 0.0),
                    discountLabel = intent.getStringExtra("discountLabel"),
                    total = intent.getDoubleExtra("total", 0.0),
                    houseRule = intent.getStringArrayListExtra("houseRules")!!,
                    title = intent.getStringExtra("title").orEmpty(),
                    image = intent.getStringExtra("image").orEmpty(),
                    cancellation = intent.getStringExtra("cancellation").orEmpty(),
                    cancellationContent = intent.getStringExtra("cancellationContent").orEmpty(),
                    guest = intent.getIntExtra("guest", 0),
                    hostServiceFee = intent.getDoubleExtra("hostServiceFee", 0.0),
                    currency = intent.getStringExtra("currency").orEmpty(),
                    listId = intent.getIntExtra("listId", 0),
                    bookingType = intent.getStringExtra("bookingType").orEmpty(),
                    isProfilePresent = intent.getBooleanExtra("isProfilePresent", false),
                    averagePrice = intent.getDoubleExtra("averagePrice",0.0),
                    priceForDays = intent.getDoubleExtra("priceForDays",0.0),
                    specialPricing = intent.getStringExtra("specialPricing").orEmpty(),
                    isSpecialPriceAssigned = intent.getBooleanExtra("isSpecialPriceAssigned",false)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkVerification() {
        val buildQuery = GetProfileQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.userAccount()
                        if (data?.status() == 200) {
                            val result = data.result()
                            if(result!!.picture().isNullOrEmpty()) {
                                navigator.navigateToScreen(4)
                                billingDetails.value?.isProfilePresent = false
                            } else {
                                dataManager.currentUserProfilePicUrl = result.picture()
                                navigator.navigateToScreen(5)
                                billingDetails.value?.isProfilePresent = true
                            }
                        } else if(data?.status() == 500) {
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
}