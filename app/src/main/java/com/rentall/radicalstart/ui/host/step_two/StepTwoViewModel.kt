package com.rentall.radicalstart.ui.host.step_two

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.ListDetailsStep2
import com.rentall.radicalstart.vo.PhotoList
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class StepTwoViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<StepTwoNavigator>(dataManager,resourceProvider){

    var listID : String = ""

    var previousSize = 0

    var photoPaths = ArrayList<String>()

    var isPhotoUploaded = MutableLiveData<Boolean>()

    var uploadStatusArray = MutableLiveData<ArrayList<Boolean>>()

    var retryStatus = MutableLiveData<ArrayList<Boolean>>()

    var listPhotoNames = MutableLiveData<ArrayList<String>>()

    val title = ObservableField("")

    val desc = ObservableField("")

    var uploadNextText = ObservableField("Skip for now")

    var uploadBG = ObservableField(false)

    lateinit var listDetailsStep2 : MutableLiveData<ListDetailsStep2>

    lateinit var showPhotosList : MutableLiveData<List<ShowListPhotosQuery.Result?>>

    var isCoverPhoto = 0

    var photoFromAPI = false

    var isListAdded = false

    var unloadedPhotos = ArrayList<String>()

    var retryCalled = ""

    init {
       dataManager.clearHttpCache()
    }

    fun listSetting() : MutableLiveData<ListDetailsStep2>{
        if (!::listDetailsStep2.isInitialized) {
            listDetailsStep2 = MutableLiveData()
            showPhotosList = MutableLiveData()
            showListPhotos("list")
        }

        return listDetailsStep2
    }

    enum class NextScreen{
        COVER,
        LISTTITLE,
        LISTDESC,
        FINISH,
        APIUPDATE,
        UPLOAD
    }

    fun setData(){
        listDetailsStep2.value = ListDetailsStep2(
                listPhotos = HashSet(),
                coverPhoto = 0,
                title = "",
                desc = ""
        )
    }

    fun showListPhotos(from : String?){
        val buildQuery = ShowListPhotosQuery
                .builder()
                .listId(listID.toInt())
                .build()
        compositeDisposable.add(dataManager.ShowListPhotosQuery(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()
                    try {
                        if (data?.showListPhotos()!!.status() == 200) {
                            photoFromAPI = true
                            showPhotosList.value = data?.showListPhotos()!!.results()!!
                            if(from!!.contains("deleteAct")){
                                val text =from.split("-")
                                deletePhotos(showPhotosList!!.value!![text[1].toInt()]!!.name().toString())
                            }else {
                                photoPaths.clear()
                                val list = ArrayList<Boolean>()
                                val retryList = ArrayList<Boolean>()
                                for (index in 0 until showPhotosList.value!!.size) {
                                    val result = showPhotosList!!.value!![index]
                                    photoPaths.add(result!!.name().toString())
                                    list.add(index, true)
                                    retryList.add(index, false)
                                }
                                uploadNextText.set("Next")
                                uploadBG.set(true)
                                uploadStatusArray.value = list
                                retryStatus.value = retryList
                                isPhotoUploaded.value = true
                                if(listDetailsStep2.value != null) {
                                    if (listDetailsStep2.value!!.coverPhoto != 0) {
                                        setCoverIndex()
                                    }
                                }
                                if (from.equals("cover")) {
                                    onClick(NextScreen.COVER)
                                }else if(from.equals("list")){
                                    getListDetailsStep2()
                                }
                                Log.d("viewModel uploadStatus", uploadStatusArray.value!!.toList().toString())
                                Log.d("viewModel retryStatus", retryStatus.value!!.toList().toString())
                            }
                        } else if(data?.showListPhotos()!!.status() == 500) {
                            isPhotoUploaded.value = false
                            navigator.openSessionExpire()
                        } else {
                            if(from.equals("list")){
                                getListDetailsStep2()
                            }
                            isPhotoUploaded.value = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, { handleException(it) } )
        )
    }

    fun updateStep2(){

        val buildQuery = UpdateListingStep2Mutation
                .builder()
                .title(title.get().toString().trim().replace("\\s+", " "))
                .coverPhoto(listDetailsStep2.value!!.coverPhoto)
                .description(desc.get())
                .id(listID.toInt())
                .build()
        compositeDisposable.add(dataManager.doUpdateListingStep2(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()?.updateListingStep2()
                    try {
                        if (data?.status() == 200) {
                            retryCalled = ""
                            updateStepDetails()
                            //navigator.navigateToScreen(NextScreen.FINISH)
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

    fun updateStepDetails(){
        val buildQuery = ManageListingStepsMutation
                .builder()
                .listId(listID)
                .currentStep(2)
                .build()
        compositeDisposable.add(dataManager.doManageListingSteps(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()?.ManageListingSteps()
                    try {
                        if (data?.status() == 200) {
                            navigator.navigateToScreen(NextScreen.FINISH)
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

//    fun nextCheck(){
//        if(photoPaths.isNullOrEmpty()){
//            onClick(NextScreen.LISTTITLE)
//        }else{
//            var list : HashSet<String>
//            list = HashSet()
//            photoPaths.forEachIndexed { index, s ->
//                list.add(s)
//            }
//            if(retryStatus.value!! != null) {
//                if (retryStatus.value!!.contains(true)) {
//                    unloadedPhotos.addAll(photoPaths)
//                }
//            }
//            Log.d("unloadedPhotos",unloadedPhotos.toList().toString())
//            listDetailsStep2.value!!.listPhotos = list
//            showListPhotos("cover")
//        }
//    }

    fun onClick(screen : NextScreen){
        Log.d("Sceen",screen.toString())
        navigator.navigateToScreen(screen)
    }

    fun getListDetailsStep2(){
        val buildQuery = GetListingDetailsStep2Query
                .builder()
                .listId(listID)
                .preview(true)
                .build()
        compositeDisposable.add(dataManager.doGetListingDetailsStep2Query(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()!!.listingDetails
                    try {
                        if (data?.status() == 200) {
                            retryCalled = ""
                            uploadNextText.set("Next")
                            uploadBG.set(true)
                            if(data.results()!!.coverPhoto() == null && data.results()!!.title().isNullOrEmpty() && data.results()!!.description().isNullOrEmpty()){
                                setData()
                                isListAdded = false
                            }else {
                                isListAdded = true
                                listDetailsStep2.value = ListDetailsStep2(
                                        listPhotos = HashSet(),
                                        coverPhoto = data.results()!!.coverPhoto(),
                                        title = data.results()!!.title(),
                                        desc = data.results()!!.description()
                                )
                                title.set(data.results()!!.title())
                                desc.set(data.results()!!.description())
                            }
                        } else if(data?.status() == 500) {
                            isListAdded=false
                            setData()
                            navigator.openSessionExpire()
                        } else {
                            isListAdded=false
                            setData()
                            //navigator.showError()
                        }
                        setCoverIndex()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, {
                    handleException(it)
                } )
        )
    }


    fun setCoverIndex(){
        if(showPhotosList.value!!.size > 0) {
            showPhotosList.value!!.forEachIndexed { index, result ->
                if (result!!.id() == listDetailsStep2.value!!.coverPhoto) {
                    isCoverPhoto = index
                }
            }
        }
    }

    fun deletePhotos(filename: String){
        val buildQuery = RemoveListPhotosMutation
                .builder()
                .listId(listID.toInt())
                .name(filename)
                .build()
        compositeDisposable.add(dataManager.doRemoveListPhotos(buildQuery)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()!!.RemoveListPhotos()
                    try {
                        var deleteIndex = 0
                        if (data?.status() == 200) {
                            showPhotosList.value!!.forEachIndexed { index, s ->
                                if(filename.equals(s!!.name())){
                                    deleteIndex = index
                                }
                            }
                            Log.d("deleteIndex",deleteIndex.toString())
                            photoPaths.removeAt(deleteIndex)
                            if(photoPaths.size==0){
                                uploadNextText.set("Skip for now")
                                uploadBG.set(false)
                            }
                            val retryArray = retryStatus.value
                            retryArray!!.removeAt(deleteIndex)
                            retryStatus.value = retryArray
                            val uploadArray = uploadStatusArray.value
                            uploadArray!!.removeAt(deleteIndex)
                            uploadStatusArray.value = uploadArray
                        }else if(data?.status()==400){
                            navigator.showSnackbar("Delete Photo",data?.errorMessage().toString())
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

    fun step2Retry(){
        getListDetailsStep2()
    }
    val photoList = MutableLiveData<ArrayList<PhotoList>>().apply { value = ArrayList() }
    val toUploadphotoList = MutableLiveData<ArrayList<PhotoList>>().apply { value = ArrayList() }
    val step2Result = MutableLiveData<GetListingDetailsStep2Query.Results>()

    fun showListPhotos1(){
        val buildQuery = ShowListPhotosQuery
                .builder()
                .listId(733)
                .build()
        compositeDisposable.add(dataManager.ShowListPhotosQuery(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()
                        if (data?.showListPhotos()!!.status() == 200) {
                            parseData(data.showListPhotos()!!.results()!!)
                        } else if(data.showListPhotos()!!.status() == 500) {
                            navigator.openSessionExpire()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, { handleException(it, true);  } )
        )
    }

    private fun parseData(results: MutableList<ShowListPhotosQuery.Result>) {
        val list = ArrayList<PhotoList>()
        results.forEachIndexed { _, result ->
            list.add(PhotoList(
                    UUID.randomUUID().toString(),
                    result.id(),
                    result.name(),
                    result.type(),
                    result.isCover
            ))
        }
        photoList.value = list
    }

    fun addPhotos(photoPaths: ArrayList<String>) {
        val list = photoList.value
        photoPaths.forEach {
            list?.add(PhotoList(
                    UUID.randomUUID().toString(),
                    -1,
                    it,
                    "",
                    0,
                    isRetry = false,
                    isLoading = true
            ))
        }
        photoList.value = list
    }

    fun setCompleted(bodyAsString: String) {
        val convertedObject = Gson().fromJson(bodyAsString, JsonObject::class.java)
        if (convertedObject.get("status").asInt == 200) {
            val array = convertedObject.get("files").asJsonArray
            val s = photoList.value
            array.forEach {
                val obj = it.asJsonObject
                val originalname = obj["originalname"].asString
                val fileName = obj["filename"].asString
               s?.forEach {
                    val file = File(Uri.parse(it.name).path)
                    if (file.name == originalname ) {
                        it.isLoading = false
                        it.name = fileName
                    }
                }
            }
            Handler(Looper.getMainLooper()).postDelayed(Runnable {photoList.value = s}, 2000)
        }
    }

    fun deletephoto(filename: String) {
        val buildQuery = RemoveListPhotosMutation
                .builder()
                .listId(733)
                .name(filename)
                .build()
        compositeDisposable.add(dataManager.doRemoveListPhotos(buildQuery)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.RemoveListPhotos()
                        if (data?.status() == 200) {
                            deletePhotoInList(filename)
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

    private fun deletePhotoInList(filename: String) {
        val list = photoList.value
        for ( i in 0 until list!!.size) {
            if (list[i].name == filename) {
                list.removeAt(i)
                break
            }
        }
        photoList.value = list
    }


    fun get22ListDetailsStep2(){
        val buildQuery = GetListingDetailsStep2Query
                .builder()
                .listId(listID)
                .preview(true)
                .build()
        compositeDisposable.add(dataManager.doGetListingDetailsStep2Query(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.listingDetails
                        if (data?.status() == 200) {
                            retryCalled = ""
                            uploadNextText.set("Next")
                            uploadBG.set(true)
                            step2Result.value = data.results()
                            /*if(data.results()!!.coverPhoto() == null && data.results()!!.title().isNullOrEmpty() && data.results()!!.description().isNullOrEmpty()){
                                isListAdded = false
                            }else {
                                isListAdded = true
                                title.set(data.results()!!.title())
                                desc.set(data.results()!!.description())
                            }*/
                        } else if(data?.status() == 500) {
                            isListAdded=false
                            setData()
                            navigator.openSessionExpire()
                        } else {
                            isListAdded=false
                            setData()
                            //navigator.showError()
                        }
                        setCoverIndex()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, {
                    handleException(it)
                } )
        )
    }
}