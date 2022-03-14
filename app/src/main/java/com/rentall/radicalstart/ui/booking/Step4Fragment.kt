package com.rentall.radicalstart.ui.booking

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rentall.radicalstart.*
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentBookingStep1Binding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.EnableAlpha
import com.rentall.radicalstart.util.invisible
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import net.gotev.uploadservice.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import javax.inject.Inject

const val RC_LOCATION_PERM: Int = 123

class Step4Fragment: BaseFragment<FragmentBookingStep1Binding, BookingViewModel>(), EasyPermissions.PermissionCallbacks {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: BookingViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(BookingViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding
    var avatar: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        setUp()
    }

    private fun initView() {
        mBinding.tvListingCheckAvailability.text = resources.getString(R.string.next).toLowerCase().capitalize()
        mBinding.rlListingPricedetails.invisible()
        if (viewModel.avatar.value == null) {
            mBinding.tvListingCheckAvailability.EnableAlpha(false)
        } else {
            mBinding.tvListingCheckAvailability.EnableAlpha(true)
        }
        mBinding.tvListingCheckAvailability.onClick {
            //            viewModel.uiEventLiveData.value = UiEventWrapper(UiEvent.Navigate1(5))
            if (viewModel.isUploading.not())
            (baseActivity as BookingActivity).navigateToScreen(5)
        }
        mBinding.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.ivNavigateup.onClick { if (viewModel.isUploading.not()){ baseActivity?.onBackPressed() } }
    }

    private fun setUp() {
        mBinding.rlBooking.withModels {
            viewholderBookingSteper {
                id("sd123")
                step(resources.getString(R.string.step_3_of_4))
                title(resources.getString(R.string.add_your_profile_photo))
                info(resources.getString(R.string.profile_photo_desc))
                infoVisibility(true)
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderBookingUploadPhoto {
                id(987654)
                img(viewModel.avatar.value)
                onClick(View.OnClickListener {
                    askCameraPermission()
                })
            }
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (EasyPermissions.hasPermissions(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)) {
            pickImage()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    resources.getString(R.string.grant_camera_permission),
                    RC_LOCATION_PERM, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }
    }

    private fun pickImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setAspectRatio(1, 1)
                .start(requireContext(), this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    private fun uploadMultipart(uri: Uri) {
        try {
            viewModel.isLoading.set(true)
            val config = UploadNotificationConfig()
            config.completed.autoClear = true
            config.error.autoClear = true
            config.cancelled.autoClear = true

            MultipartUploadRequest(context, Constants.WEBSITE + Constants.uploadPhoto)
                    .addFileToUpload(uri.path, "file")
                    .addHeader("auth", viewModel.getAccessToken())
                    .setNotificationConfig(config)
                    .setMaxRetries(2)
                    .setDelegate(object : UploadStatusDelegate {
                        override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                            Log.d("upload", "onCancelled")
                            viewModel.isLoading.set(false)
                            viewModel.isUploading = false
                        }

                        override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
                            Log.d("upload", "onProgress+"+ " "+uploadInfo?.progressPercent)
                            // viewModel.isLoading.set(false)
                            viewModel.isUploading = true
                        }

                        override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
                            Log.d("upload", "onError+"+ " "+uploadInfo?.progressPercent+ "  "+serverResponse?.bodyAsString+" "+ exception.toString())
                            viewModel.isLoading.set(false)
                            viewModel.isUploading = false
                        }

                        override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
                            viewModel.isUploading = false
                            Log.d("upload", "onComplete+"+ " "+uploadInfo?.progressPercent+ "  "+serverResponse?.bodyAsString)
                            val convertedObject =  Gson().fromJson(serverResponse?.bodyAsString, JsonObject::class.java)
                            if (convertedObject.get("status").asInt == 200) {
                                val array = convertedObject.get("file").asJsonObject
                                Log.d("fileName", array.get("filename").asString)
                              //  viewModel.pic.set(array.get("filename").asString)
                                viewModel.avatar.value = array.get("filename").asString
                                viewModel.setPictureInPref(array.get("filename").asString)
                                mBinding.rlBooking.requestModelBuild()
                                mBinding.tvListingCheckAvailability.EnableAlpha(true)
                            }
                            viewModel.isLoading.set(false)
                        }
                    })
                    .startUpload()

        } catch (exc: Exception) {
            //Log.e("AndroidUploadService", exc.message, exc)
            viewModel.isLoading.set(false)
            viewModel.isUploading = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(context, "R.string.returned_from_app_settings_to_activity", Toast.LENGTH_SHORT)
                    .show()
        }
        if (Activity.RESULT_OK == resultCode && data != null) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = result.uri
                        validateFileSize(resultUri)
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                        val error = result.error
                        showToast(resources.getString(R.string.something_went_wrong))
                    }
                }
                AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                    Log.d("ProfileFragment", "Camera Permission Denied!!")
                }
            }
        }
    }

    private fun validateFileSize(uri: Uri) {
        val file = File(uri.path)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        if (fileSizeInMB <= Constants.PROFILEIMAGESIZEINMB) {
            try {
                if(baseActivity?.isNetworkConnected!!) {
                    uploadMultipart(uri)
                    /*Glide.with(this@Step4Fragment)
                            .load(resultUri)
                            .into(iv_avatar)*/
                } else {
                    showToast(resources.getString(R.string.currently_offline))
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            showToast(resources.getString(R.string.image_size_is_too_large))
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    override fun onRetry() {

    }
}