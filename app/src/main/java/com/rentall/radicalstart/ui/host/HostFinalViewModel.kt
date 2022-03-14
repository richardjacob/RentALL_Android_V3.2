package com.rentall.radicalstart.ui.host

import android.os.Handler
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class HostFinalViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<HostFinalNavigator>(dataManager,resourceProvider) {

    val listId = ObservableField("")
    lateinit var stepsSummary : MutableLiveData<ShowListingStepsQuery.Results>
    var step1Status : String = ""
    var step2Status : String = ""
    var step3Status : String = ""
    var isPublish = ObservableField(false)
    var listApprovelStatus = ObservableField<String?>()
    var isReady = ObservableField(false)
    var listingDetails = MutableLiveData<ViewListingDetailsQuery.Results>()
    val yesNostr = ObservableField("")
    val street = ObservableField("")
    val country = ObservableField("")
    val countryCode = ObservableField("")
    val buildingName = ObservableField("")
    val city = ObservableField("")
    val state = ObservableField("")
    val zipcode = ObservableField("")
    val bathroomCapacity = ObservableField<String>()
    val lat = ObservableField("")
    val lng = ObservableField("")
    val publishBoolean=ObservableField(true)
    var isPhotoAdded : Boolean = false

    var retryCalled = ""

    fun getStepDetails() : MutableLiveData<ShowListingStepsQuery.Results>{
        if(!::stepsSummary.isInitialized) {
            stepsSummary = MutableLiveData()
           // getStepsSummary()
            defaultSettingsInCache()
        }
        return stepsSummary
    }

    private fun setSiteName(it: GetDefaultSettingQuery.SiteSettings?) {
        try {
            if (it?.results()!![0].name() == "siteName") {
                dataManager.siteName = it.results()!![0].value()
            }
            dataManager.listingApproval = it.results()!!.find { it.name() == "listingApproval" }?.value()?.toIntOrNull() ?: 0
            getStepsSummary()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun submitForVerification(status : String) {
        val query = SubmitForVerificationMutation.builder()
                .id(listId.get()!!.toInt())
                .listApprovalStatus(status).build()

        compositeDisposable.add(dataManager.submitForVerification(query)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        Handler().postDelayed({
                            publishBoolean.set(true)
                        },1000)
                        val data = response.data()!!.submitForVerification()
                        if(data?.status() == 200){
                            retryCalled=""
                            isPublish.set(false)
                            getStepsSummary()
                            //defaultSettingsInCache()

                        }else if(data!!.status() == 400){
                            navigator.showToast(data.errorMessage().toString())
                        }else{
                            navigator.openSessionExpire()
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()

                    } }, {

                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }

    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.clearHttpCache()
                .doOnSubscribe { setIsLoading(true)  }
                .doFinally { setIsLoading(false) }
                .flatMap { dataManager.doGetDefaultSettingApiCall(request).toObservable() }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            setSiteName(it.data()?.siteSettings())
                        },
                        {

                        }
                ))
    }

    fun getStepsSummary() {
        val buildQuery = ShowListingStepsQuery
                .builder()
                .listId(listId.get().toString())
                .build()
        compositeDisposable.add(dataManager.doShowListingSteps(buildQuery)
                .doOnSubscribe { setIsLoading(true)  }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.showListingSteps()
                        if (data!!.status() == 200) {
                            retryCalled=""
                            isPublish.set(data!!.results()!!.listing()!!.isPublished!!)
                            listApprovelStatus.set(data!!.results()!!.listing()!!.listApprovalStatus())
                            isReady.set(data!!.results()!!.listing()!!.isReady!!)
                            step1Status = data!!.results()!!.step1()!!
                            step2Status = data!!.results()!!.step2()!!
                            step3Status = data!!.results()!!.step3()!!
                            isPhotoAdded = data!!.results()!!.isPhotosAdded!!
                            stepsSummary.value = data!!.results()

                        } else if(data.status() == 500) {
                                navigator.openSessionExpire()
                        } else {
                            data.errorMessage()?.let {
                                navigator.show404Screen()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    } }, {

                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }





    fun publishListing(action:String){
        var buildQuery = ManagePublishStatusMutation
                .builder()
                .listId(listId.get()!!.toInt())
                .action(action)
                .build()
        compositeDisposable.add(dataManager.doManagePublishStatus(buildQuery)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        Handler().postDelayed({
                            publishBoolean.set(true)
                        },1000)
                        val data = response.data()!!.managePublishStatus()!!
                        if(data.status() == 200){
                            retryCalled=""
                            if(action.equals("unPublish")) {
                                isPublish.set(false)
                            }
                            else{
                                isPublish.set(true)
                            }
                             getStepsSummary()
                          //  defaultSettingsInCache()
                        }else if(data!!.status() == 400){
                            navigator.showToast(data.errorMessage().toString())
                        }else{
                            navigator.openSessionExpire()
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()

                    } }, {

                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }

    fun getListingDetails() {
            val buildQuery = ViewListingDetailsQuery
                    .builder()
                    .listId(listId.get()!!.toInt())
                    .preview(true)
                    .build()

            compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data()?.viewListing()
                            if (data!!.status() == 200) {
                                retryCalled=""
                                listingDetails.value = data.results()
                                navigator.showListDetails()

                            } else if(data!!.status() == 500) {
                                (navigator as BaseNavigator).openSessionExpire()
                            }
                            else {
                                navigator.show404Screen()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, { handleException(it) }
                    )
            )
    }

}