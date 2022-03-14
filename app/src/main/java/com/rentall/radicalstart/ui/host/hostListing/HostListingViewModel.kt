package com.rentall.radicalstart.ui.host.hostListing

import android.os.Handler
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.ManageList
import org.json.JSONArray
import javax.inject.Inject

class HostListingViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<HostListingNavigator>(dataManager,resourceProvider) {

    lateinit var manageListing : MutableLiveData<List<ManageListingsQuery.Result>>
    var allList = MutableLiveData<ArrayList<ManageList>>()
    var listingDetails = MutableLiveData<ViewListingDetailsQuery.Results>()
    var retryCalled = ""
    val publishBoolean= ObservableField(true)
    var removeListRes =ArrayList<HashMap<String,String>>()

    fun loadListing() : MutableLiveData<ArrayList<ManageList>> {
        if (!::manageListing.isInitialized) {
            manageListing = MutableLiveData()
          //  getList()
            defaultSettingsInCache()
        }
        return allList
    }


    private fun setSiteName(it: GetDefaultSettingQuery.SiteSettings?) {
        try {
            if (it?.results()!![0].name() == "siteName") {
                dataManager.siteName = it.results()!![0].value()
            }
            dataManager.listingApproval = it.results()!!.find { it.name() == "listingApproval" }?.value()?.toIntOrNull() ?: 0
            getList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun submitForVerification(status : String , listId: Int) {
        val query = SubmitForVerificationMutation.builder()
                .id(listId)
                .listApprovalStatus(status).build()

        compositeDisposable.add(dataManager.submitForVerification(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false)  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        Handler().postDelayed({
                            publishBoolean.set(true)
                        },1000)
                        val data = response.data()!!.submitForVerification()
                        if(data?.status() == 200){
                            retryCalled=""
                            setPublishStatus(listId,false)
                            getList()
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


    fun getList(){
        val buildQuery = ManageListingsQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getManageListings(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .subscribe( { response ->
                    val data = response.data()?.ManageListings()!!
                    try {
                        if(data.status() == 200) {
                            dataManager.isHostOrGuest = true
                            retryCalled = ""
                            if(data.results().isNullOrEmpty()){
                                navigator.showNoListMessage()
                            }else{
                                manageListing.value = data.results()
                                setData()
                            }


                        }else if(data.status() == 500) {
                            navigator.openSessionExpire()
                        }else {
                            navigator.showNoListMessage()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, {
                    navigator.hideLoading()
                    handleException(it)
                }
                )
        )

    }

    fun setData(){
        var inprogress = ArrayList<ManageList>()
        manageListing.value!!.forEachIndexed { index, result ->
            var listPhoto = ""
            var listTitle = ""
            if(result.listPhotos()!!.size > 0){
                if(result.coverPhoto() != null){
                    result.listPhotos()!!.forEachIndexed { index, listPhotos ->
                        if(listPhotos.id()!! ==  result.coverPhoto()!!){
                            listPhoto = result?.listPhotos()!![index].name()!!
                        }
                    }
                    if(listPhoto.isNullOrEmpty()){
                        listPhoto = result?.listPhotos()!![0].name()!!
                    }
                }else{
                    listPhoto = result?.listPhotos()!![0].name()!!
                }
            }
            if(result.title() !=null){
                listTitle = result.title()!!.trim().replace("\\s+", " ")
            }
            var roomtype = ""
            if(result.settingsData()!!.size>0){
                if(result.settingsData()!![0].listsettings()?.itemName() == null){
                    roomtype = "Room Type"
                }else{
                    roomtype = result.settingsData()!![0].listsettings()!!.itemName()!!
                }
            }
            var step1 = ""
            var step2 = ""
            var step3 = ""
            if(result.listingSteps() == null){
                step1 = "active"
                step2 = "inactive"
                step3 = "inactive"
            }else{
                step1 = result.listingSteps()!!.step1()!!
                step2 = result.listingSteps()!!.step2()!!
                step3 = result.listingSteps()!!.step3()!!
            }
            val list = ManageList(
                    id = result.id()!!,
                    title = listTitle,
                    imageName = listPhoto,
                    isReady = result.isReady!!,
                    isPublish = result.isPublished!!,
                    step1Status = step1,
                    step2Status = step2,
                    step3Status = step3,
                    location = result.city(),
                    roomType = roomtype,
                    created = result.updatedAt()!!,
                    listApprovelStatus = result.listApprovalStatus()
            )
            inprogress.add(list)
        }
        allList.value = inprogress
    }

    private fun setPublishStatus(id: Int,action: Boolean) {
        val listValues = allList.value
        for ( i in 0 until listValues!!.size) {
            if(listValues[i].id == id) {
                listValues[i].isPublish =action
            }
        }
        allList.value = listValues
    }

    fun publishListing(action:String,listId: Int,pos: Int){
        var buildQuery = ManagePublishStatusMutation
                .builder()
                .listId(listId)
                .action(action)
                .build()
        compositeDisposable.add(dataManager.doManagePublishStatus(buildQuery)
                .doOnSubscribe { }
                .doFinally { }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        Handler().postDelayed({
                            publishBoolean.set(true)
                        },1000)
                        val data = response.data()!!.managePublishStatus()!!
                        if(data.status() == 200){
                            retryCalled = ""
                            if(action.equals("unPublish")) {
                                setPublishStatus(listId,false)
                            }
                            else{
                                setPublishStatus(listId,true)
                            }
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

    fun getListingDetails(listId: Int) {
        val buildQuery = ViewListingDetailsQuery
                .builder()
                .listId(listId)
                .preview(true)
                .build()

        compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()?.viewListing()
                        if (data!!.status() == 200) {
                            retryCalled = ""
                            listingDetails.value = data.results()
                            navigator.showListDetails()
                        }else if(data!!.status() == 400){
                            navigator.show404Screen()
                        }else{
                            navigator.openSessionExpire()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, { handleException(it) }
                )
        )
    }

    fun removeList(listId: Int,pos: Int,from: String){
        val buildQuery = RemoveListingMutation
                .builder()
                .listId(listId)
                .build()
        compositeDisposable.add(dataManager.doRemoveListingMutation(buildQuery)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.RemoveListing()
                        retryCalled = ""
                        if(data!!.status() == 200){

                            val removePhotos = data.results()
                            if(removePhotos!!.size > 0) {
                                removePhotos!!.forEachIndexed { index, result ->
                                    val temp = HashMap<String, String>()
                                    temp.put("name", result.name().toString())
                                    temp.put("id", result.id().toString())
                                    removeListRes.add(temp)
                                }
                                removeListPhotos(listId)
                            }else{
                                removeListEntry(listId)
                            }
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

    private fun removeListEntry(id: Int) {
        val entireList = allList.value
        for ( i in 0 until entireList!!.size) {
            if (entireList[i].id == id) {
                entireList.removeAt(i)
                break
            }
        }
        allList.value = entireList
    }

    fun removeListPhotos(listId: Int){
        val json = JSONArray(removeListRes)
        val buildQuery = RemoveMultiPhotosMutation
                .builder()
                .photos(json.toString())
                .build()
        compositeDisposable.add(dataManager.doRemoveMultiPhotosMutation(buildQuery)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.RemoveMultiPhotos()
                        if(data!!.status() == 200) {
                            removeListEntry(listId)
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
    fun listRefresh(){
        getList()
    }

    fun onDeleteClick(){
        Log.d("LongClicked","LongClicked")
    }
}