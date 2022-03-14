package com.rentall.radicalstart.ui.host.step_two

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentUploadListphotoBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderAddListing
import com.rentall.radicalstart.vo.PhotoList
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import net.gotev.uploadservice.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class PhotoUploadFragment : BaseFragment<HostFragmentUploadListphotoBinding, StepTwoViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentUploadListphotoBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_upload_listphoto
    override val viewModel: StepTwoViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepTwoViewModel::class.java)

    private val uploadReceiver: UploadServiceSingleBroadcastReceiver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        viewModel.showListPhotos1()
    }

    private fun initView() {
        mBinding.setOnClick {
            askCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setup(it: ArrayList<PhotoList>) {
        mBinding.rvListPhotos.withModels {
            it.forEachIndexed { index, s ->
                viewholderAddListing {
                    //Log.d("jmij", s.name)
                    id("photo$index")
                    url(s.name)
                    isRetry(s.isRetry)
                    isLoading(s.isLoading)
                    onClick(View.OnClickListener {

                    })
                    deleteClick(View.OnClickListener {

                    })
                }
            }
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (EasyPermissions.hasPermissions(
                        baseActivity!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)) {
            pickImage()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }
    }

    private fun pickImage() {
        FilePickerBuilder.Companion.instance
                .setMaxCount(10)
                .setActivityTheme(R.style.AppTheme)
                .setActivityTitle("Select List Photos")
                .enableImagePicker(true)
                .enableVideoPicker(false)
                .enableCameraSupport(true)
                .showFolderView(false)
                .showGifs(false)
                .enableSelectAll(false)
                .pickPhoto(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val photoPaths = ArrayList<String>()
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!)
                    val list = ArrayList<PhotoList>()
                    photoPaths.forEachIndexed { _, s ->
                        list.add(PhotoList(
                            UUID.randomUUID().toString(), null, s, null, null, isRetry = false, isLoading = true))
                    }
                    val lss = viewModel.toUploadphotoList.value
                    lss?.addAll(list)
                    viewModel.toUploadphotoList.value = lss
                    Log.d("fddf", lss.toString())

                    val photoList =  viewModel.photoList.value
                    photoList?.addAll(list)
                    viewModel.photoList.value = photoList
                }
            }
        }
    }

    private fun uploadMultipart(uri: PhotoList) {
        try {
            val config = UploadNotificationConfig()
           /* config.completed.autoClear = true
            config.error.autoClear = true
            config.cancelled.autoClear = true*/
            MultipartUploadRequest(baseActivity, Constants.WEBSITE + Constants.uploadListPhoto)
                    .addFileToUpload(Uri.parse(uri.name).path, "file")
                    .addHeader("auth", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjVmZmU3MzgwLTViODUtMTFlOS04OGFhLTM5MzVjYWRmMGY2YyIsImVtYWlsIjoiZ29rdWxAcmFkaWNhbHN0YXJ0LmNvbSIsImlhdCI6MTU1NzM4NjU3MSwiZXhwIjoxNTcyOTM4NTcxfQ.3Q3s2qP2JlSP39pwBumiDeO7lhfeXE2D2StMbUqZczk")
                    .addParameter("listId", "733")
                    .setNotificationConfig(config)
                    .setMaxRetries(1)
                    .setDelegate( object : UploadStatusDelegate {

                        override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                            Timber.tag("ProfileFragment").d("upload - onCancelled - %d", uploadInfo?.progressPercent)
                        }

                        override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
                            Timber.tag("ProfileFragment").d("upload - onProgress - %d", uploadInfo?.progressPercent)
                        }

                        override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
                            Timber.tag("ProfileFragment").d("upload - onError - %d, response - %s, exception - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, exception.toString())
                        }

                        override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
                            Timber.tag("ProfileFragment").d("upload - onComplete - %d, response - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString)
                            val convertedObject = Gson().fromJson(serverResponse?.bodyAsString, JsonObject::class.java)
                            if (convertedObject.get("status").asInt == 200) {
                                val array = convertedObject.get("files").asJsonArray
                                val obj = array.get(0).asJsonObject
                                Timber.tag("fileName").d(obj.get("filename").asString)
                            }
                            val photoList = viewModel.photoList.value
                            photoList?.forEachIndexed { _, photo ->
                                if (photo.refId == uri.refId) {
                                    photo.isLoading = false
                                }
                            }
                            viewModel.photoList.value = photoList
                            val list = viewModel.toUploadphotoList.value
                            list!!.removeAt(0)
                            viewModel.toUploadphotoList.value = list
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Timber.tag("AndroidUploadService").e(exc)
            showError()
        }
    }

    fun subscribeToLiveData() {
        viewModel.photoList.observe(this, Observer {
           it?.let {
               mBinding.visiblity = true
               setup(it)
           }
        })
        viewModel.toUploadphotoList.observe(this, Observer {
            it?.let { photo ->
                Log.d("km0", photo.toString())
                if (photo.isNotEmpty()) {
                    uploadMultipart(photo[0])
                }
            }
        })
    }

    override fun onRetry() {
        viewModel.showListPhotos1()
    }
}