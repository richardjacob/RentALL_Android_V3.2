package com.rentall.radicalstart.ui.host.step_two

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
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
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderAddListing
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import net.gotev.uploadservice.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import javax.inject.Inject


const val RC_LOCATION_PERM: Int = 123
val REQUEST_CODE: Int = 1

class UploadListingPhotos : BaseFragment<HostFragmentUploadListphotoBinding, StepTwoViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentUploadListphotoBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_upload_listphoto
    override val viewModel: StepTwoViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepTwoViewModel::class.java)

    var handler = Handler()
    var runnable: Runnable? = null

    var uploadIndex: Int = 0

    var isErrorOnUpload: Boolean = true
    lateinit var fmContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fmContext = context
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!

        if(viewModel.isListAdded) {
            mBinding.uploadToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.uploadToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.uploadToolbar.tvRightside.onClick {
                handler.removeCallbacks(runnable ?: Runnable { } )
                viewModel.retryCalled = "update"
                viewModel.updateStep2()
            }
            if(viewModel.photoPaths.size == 0) {
                mBinding.tvNext.setText(getString(R.string.skip_for_now))
                mBinding.tvNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_transparent)
                mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.ic_next_arrow_green), null)
            }else{
                mBinding.tvNext.setText(getString(R.string.next))
                mBinding.tvNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_green)
                mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.ic_next_arrow),null)
            }

        }else{
            mBinding.uploadToolbar.tvRightside.visibility = View.GONE
            if(viewModel.photoPaths.size == 0) {
                mBinding.tvNext.setText(getString(R.string.skip_for_now))
                mBinding.tvNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_transparent)
                mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.ic_next_arrow_green), null)
            }else{
                mBinding.tvNext.setText(getString(R.string.next))
                mBinding.tvNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_green)
                mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.ic_next_arrow),null)
            }
        }
        mBinding.uploadToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.uploadToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }

        mBinding.setOnClick {
            askCameraPermission()
        }

        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        //mBinding.visiblity = true
        //setUI()

        viewModel.isPhotoUploaded.observe(viewLifecycleOwner, Observer {
            if (it) {
                mBinding.visiblity = it
                setUI()
            } else {
                mBinding.visiblity = it
            }
        })

        viewModel.uploadStatusArray.observe(viewLifecycleOwner, Observer {
            Log.d("checkLog", viewModel.uploadStatusArray.toString())
            if (mBinding.rvListPhotos.adapter != null) {
                mBinding.rvListPhotos.requestModelBuild()
            }
        })
    }

    fun setUI() {
        if (viewModel.unloadedPhotos.isNotEmpty()) {
            if (viewModel.unloadedPhotos.size > viewModel.photoPaths.size) {
                Log.d("unloadedPhotos", viewModel.unloadedPhotos.toList().toString())
                val photosSize = viewModel.photoPaths.size
                val unloadedSize = viewModel.unloadedPhotos.size
                val list = ArrayList<Boolean>()
                val retryList = ArrayList<Boolean>()
                for (i in 0 until (unloadedSize - photosSize)) {
                    list.add(i, false)
                    retryList.add(i, true)
                }
                viewModel.uploadStatusArray.value!!.addAll(photosSize, list)
                Log.d("uploadStatus",viewModel.uploadStatusArray.value!!.toList().toString())
                viewModel.retryStatus.value!!.addAll(photosSize, retryList)
                Log.d("retryStatus",viewModel.retryStatus.value!!.toList().toString())
                viewModel.photoPaths.clear()
                viewModel.photoPaths.addAll(viewModel.unloadedPhotos)
                viewModel.unloadedPhotos.clear()
            }
        }
        mBinding.rvListPhotos.withModels {
            Log.d("uploadStatus", viewModel.uploadStatusArray.value!!.toList().toString())
            Log.d("retryStatus", viewModel.retryStatus.value!!.toList().toString())
            viewModel.photoPaths.forEachIndexed { index, s ->
                viewholderAddListing {
                    id("photo$index")
                    if (s.contains("/storage/")) {

                        url("file://$s")
                    } else {
                        url(s)
                    }
                    isRetry(viewModel.retryStatus.value!![index])
                    isLoading(viewModel.uploadStatusArray.value!![index])
                    onClick(View.OnClickListener {
                        if (viewModel.retryStatus.value!![index]) {

                            val uri = viewModel.photoPaths.get(index)
                            val data = viewModel.retryStatus.value
                            data!![index] = true
                            viewModel.retryStatus.value = data
                            val data1 = viewModel.uploadStatusArray.value
                            data1!![index] = false
                            viewModel.uploadStatusArray.value = data1

                            validateFileSize(Uri.parse("file://$uri"), index, uri)
                        }
                    })

                    deleteClick(View.OnClickListener {
                        if(viewModel.photoPaths.size == 1){
                            mBinding.tvNext.setText(getString(R.string.skip_for_now))
                            mBinding.tvNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                            mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_transparent)
                            mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.ic_next_arrow_green),null)
                        }
                        viewModel.retryCalled = "deleteAct-$index"
                        viewModel.showListPhotos("deleteAct-$index")
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
        Log.d("REQUEST_CODE_PHOTO", FilePickerConst.REQUEST_CODE_PHOTO.toString())
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    viewModel.photoFromAPI = false
                    val size = viewModel.photoPaths.size
                    viewModel.previousSize = size
                    if (size > 0) {
                        viewModel.photoPaths.addAll(size, data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!)

                        val list = viewModel.uploadStatusArray.value
                        val retryList = viewModel.retryStatus.value
                        for (i in list!!.size until viewModel.photoPaths.size) {
                            list.add(i, false)
                            retryList!!.add(i, false)
                        }
                        viewModel.retryStatus.value = retryList
                        viewModel.uploadStatusArray.value = list

                    } else {
                        viewModel.photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!)
                        val list = ArrayList<Boolean>()
                        val retryList = ArrayList<Boolean>()
                        viewModel.photoPaths.forEachIndexed { index, s ->
                            list.add(false)
                            retryList.add(false)
                        }
                        viewModel.retryStatus.value = retryList
                        viewModel.uploadStatusArray.value = list

                    }
                    if (viewModel.photoPaths.size > 0) {
                        viewModel.isPhotoUploaded.value = true
                        mBinding.tvNext.setText(getString(R.string.next))
                        mBinding.tvNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_green)
                        mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.ic_next_arrow),null)
                    }
                    Log.d("uploadStatus",viewModel.uploadStatusArray.value!!.toList().toString())
                    Log.d("retryStatus",viewModel.retryStatus.value!!.toList().toString())
                    for (i in viewModel.previousSize until viewModel.photoPaths.size) {
                        validateFileSize(Uri.parse("file://" + viewModel.photoPaths.get(i)), i, viewModel.photoPaths.get(i))
                    }
                }
                Log.d("PhotoPaths", viewModel.photoPaths.toString())
            }
        }
    }

    private fun validateFileSize(uri: Uri, pos: Int, path: String) {
        val file = File(uri.path)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        if (fileSizeInMB < Constants.PROFILEIMAGESIZEINMB) {
            try {
                if (isNetworkConnected) {
                    uploadMultipart(uri, pos)
                    runnable = Runnable {
                        Log.d("isError", isErrorOnUpload.toString())
                        if (isErrorOnUpload) {
                            val retryValues = viewModel.retryStatus.value
                            retryValues!![pos] = false
                            viewModel.retryStatus.value = retryValues
                            val uploadValues = viewModel.uploadStatusArray.value
                            uploadValues!![pos]= true
                            viewModel.uploadStatusArray.value = uploadValues

                            Log.d("uploadStatus",viewModel.uploadStatusArray.value!!.toList().toString())
                            Log.d("retryStatus",viewModel.retryStatus.value!!.toList().toString())

                        } else {
                            val retryValues = viewModel.retryStatus
                            retryValues.value!![pos] = true
                            viewModel.retryStatus.value = retryValues.value
                            val uploadValues = viewModel.uploadStatusArray
                            uploadValues!!.value!![pos]= false
                            viewModel.uploadStatusArray.value = uploadValues.value
                        }
                    }
                    handler.postDelayed(runnable ?: Runnable {  }, 5000)
                } else {
                    showToast(resources.getString(R.string.currently_offline))
                    val retryData = viewModel.retryStatus
                    retryData.value!![pos] = true
                    viewModel.retryStatus.value = retryData.value
                    val uploadedData = viewModel.uploadStatusArray
                    uploadedData.value!![pos] = true
                    viewModel.uploadStatusArray.value = uploadedData.value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            viewModel.photoPaths.removeAt(pos)
            val retryArray = viewModel.retryStatus.value
            retryArray!!.removeAt(pos)
            viewModel.retryStatus.value = retryArray
            val uploadArray = viewModel.uploadStatusArray.value
            uploadArray!!.removeAt(pos)
            viewModel.uploadStatusArray.value = uploadArray
            showToast("Image size is too large.")
        }
    }

    private fun uploadMultipart(uri: Uri, pos: Int) {
        try {
            //viewModel.isLoading.set(true)
            Log.d("uploadMultiPart", "Called 1")
            val config = UploadNotificationConfig()
            config.completed.autoClear = true
            config.error.autoClear = true
            config.cancelled.autoClear = true
            MultipartUploadRequest(baseActivity, Constants.WEBSITE + Constants.uploadListPhoto)
                    .addFileToUpload(uri.path, "file")
                    .addHeader("auth", viewModel.getAccessToken())
                    .addParameter("listId", viewModel.listID)
                    .setNotificationConfig(config)
                    .setMaxRetries(1)
                    .setDelegate(object : UploadStatusDelegate {

                        override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                            Log.d("called", "called")
                            isErrorOnUpload = false
                        }

                        override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
                            Timber.tag("ProfileFragment").d("upload - onProgress - %d", uploadInfo?.progressPercent)
                        }

                        override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
                            isErrorOnUpload = false
                            Timber.tag("ProfileFragment").d("upload - onError - %d, response - %s, exception - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, exception.toString())
                            showError()
                        }

                        override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
                            Log.d("checkLog123", "Completed")

                            Timber.tag("ProfileFragment").d("upload - onComplete - %d, response - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString)
                            val convertedObject = Gson().fromJson(serverResponse?.bodyAsString, JsonObject::class.java)
                            if (convertedObject.get("status").asInt == 200) {
                                isErrorOnUpload = true
                                val array = convertedObject.get("files").asJsonArray
                                val obj = array.get(0).asJsonObject
                                Timber.tag("fileName").d(obj.get("filename").asString)
                                Log.d("checkLog", "Completed")
                            }else{
                                isErrorOnUpload = false
                            }
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Timber.tag("AndroidUploadService").e(exc)
            showError()
            // viewModel.isLoading.set(false)
        }
        Log.d("uploadMultiPart", "Finished 1")
    }

    override fun onRetry() {
        Log.d("Retry","called")
        viewModel.step2Retry()
    }
}