package com.rentall.radicalstart.host.photoUpload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentUploadListphotoBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.host.step_two.RC_LOCATION_PERM
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.UploadService
import com.rentall.radicalstart.vo.PhotoList
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import net.gotev.uploadservice.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class UploadPhotoActivity:  BaseActivity<HostFragmentUploadListphotoBinding, Step2ViewModel>(),EasyPermissions.PermissionCallbacks,
        Step2Navigator, UploadStatusDelegate {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentUploadListphotoBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_upload_listphoto
    override val viewModel: Step2ViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(Step2ViewModel::class.java)
    private lateinit var uploadReceiver: UploadService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.rvListPhotos.gone()
        mBinding.tvNext.gone()
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        viewModel.setInitialValuesFromIntent(intent)
        uploadReceiver = UploadService(this)
        mBinding.uploadToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.uploadToolbar.ivNavigateup.onClick { onBackPressed() }
        mBinding.tvNext.onClick {
            replaceFragment(AddListTitleFragment(),"AddListPhoto")
        }
        viewModel.getListDetailsStep2()
    }

    private fun setup(it: ArrayList<PhotoList>) {
        try {
            mBinding.rvListPhotos.withModels {
                viewholderPlaceholderAddphotos {
                    id("placeholder")
                    onClick { _ -> askCameraPermission() }
                }
                it.forEachIndexed { index, s ->
                    viewholderAddListing {
                        id(getString(R.string.photo)+ s.refId)
                        url(s.name)
                        isRetry(s.isRetry)
                        isLoading(s.isLoading)
                        onClick(View.OnClickListener {

                        })
                        onRetryClick { _ ->
                            viewModel.retryPhotos(s.refId)
                            uploadMultipartSeperate(s.name!!, s.refId)
                        }
                        deleteClick(View.OnClickListener {
                            viewModel.retryCalled = "delete-${s.name}"
                            viewModel.deletephoto(s.name!!)
                        })
                        if (viewModel.getCoverPhotoId() == s.id) {
                                onSelected(true)
                        } else {
                                onSelected(false)
                        }
                        if(it.size==1){
                            onSelected(true)
                        }
                        if(viewModel.coverPhoto.value==-2){
                            if(index==0){
                                onSelected(true)
                            }
                        }
                        onClickq(View.OnClickListener {
                            viewModel.coverPhoto.value = s.id
                            requestModelBuild()
                        })
                    }
                }
            }
        } catch (E: java.lang.Exception) {
            showError()
        }
    }

    override fun onResume() {
        super.onResume()
        uploadReceiver.register(this)
    }

    override fun onPause() {
        super.onPause()
        uploadReceiver.unregister(this)
    }

   /* fun uploadMultipart(uri: ArrayList<String>) {
        try {
            val a =  MultipartUploadRequest(this, Constants.WEBSITE + Constants.uploadListPhoto)
            for (i in 0 until uri.size) {
                a.addFileToUpload(Uri.parse(uri[i]).path, "file")
            }
                    a.addHeader("auth", viewModel.getAccessToken())
                    a.addParameter("listId", "838")
                    a.setNotificationConfig(UploadNotificationConfig())
                    a.setMaxRetries(2)
                    a.startUpload()
        } catch (exc: Exception) {
           // Log.e("FragmentActivity.TAG", exc.message, exc)
        }
    }*/

    fun uploadMultipartSeperate(uri: String, id: String) {
        try {
            val config = UploadNotificationConfig()
            config.completed.autoClear = true
            config.error.autoClear = true
            config.cancelled.autoClear = true
            val a =  MultipartUploadRequest(this, id, Constants.WEBSITE + Constants.uploadListPhoto)
            a.addFileToUpload(Uri.parse(uri).path, "file")
            a.addHeader("auth", viewModel.getAccessToken())
            a.addParameter("listId", viewModel.listID.value)
            a.setNotificationConfig(UploadNotificationConfig())
            a.setMaxRetries(2)
            a.startUpload()
        } catch (exc: Exception) {
            // Log.e("FragmentActivity.TAG", exc.message, exc)
        }
    }

    fun subscribeToLiveData() {
        mBinding.tvNext.text = getString(R.string.skip_for_now)
        mBinding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_transparent)
        mBinding.tvNext.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.ic_next_arrow_green), null)
        viewModel.photoList.observe(this, Observer {
            it?.let {
                mBinding.visiblity = true
                if(it.size > 0) {
                    viewModel.photoListSize=1
                    mBinding.tvNext.text = getString(R.string.next)
                    mBinding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.white))
                    mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_green)
                    mBinding.tvNext.setCompoundDrawablesRelativeWithIntrinsicBounds( null, null,resources.getDrawable(R.drawable.ic_next_arrow), null)
                }else{
                    viewModel.coverPhoto.value=-2
                    viewModel.photoListSize=0
                    mBinding.tvNext.setText(getString(R.string.skip_for_now))
                    mBinding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_transparent)
                    mBinding.tvNext.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,resources.getDrawable(R.drawable.ic_next_arrow_green), null)
                }
                if (mBinding.rvListPhotos.adapter == null) {
                    if(viewModel.isListActive) {
                        mBinding.rvListPhotos.visible()
                        mBinding.tvNext.visible()
                    }else{
                        mBinding.rvListPhotos.gone()
                        mBinding.tvNext.gone()
                    }
                    setup(it)
                } else {
                    mBinding.rvListPhotos.requestModelBuild()
                }
                mBinding.rvListPhotos.requestModelBuild()

            }
        })
        viewModel.step2Result.observe(this, Observer {
            it?.let {
                if(viewModel.getListAddedStatus()) {
                    mBinding.uploadToolbar.tvRightside.visibility = View.GONE
                } else {
                    mBinding.uploadToolbar.tvRightside.text = getText(R.string.save_exit)
                    mBinding.uploadToolbar.tvRightside.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    mBinding.uploadToolbar.tvRightside.visibility = View.VISIBLE
                    mBinding.uploadToolbar.tvRightside.onClick {
                        if(viewModel.checkFilledData()) {
                            viewModel.retryCalled = "update"
                            viewModel.updateStep2()
                        }
                    }
                }
            }
        })
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (EasyPermissions.hasPermissions(
                        this,
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}


    private fun pickImage() {
        FilePickerBuilder.Companion.instance
                .setMaxCount(10)
                .setActivityTheme(R.style.AppTheme)
                .setActivityTitle( getString(R.string.select_list_photos))
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
                    if (data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA) != null)
                        photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!)
                    val uriPaths = data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                    if (uriPaths != null && uriPaths.isNotEmpty())
                        validateFileSize(uriPaths)
                }
            }
        }
    }



    private fun validateFileSize(photoPaths: ArrayList<Uri>) {
        var noOfFilesLargeInSize = 0
        val toUploadPhotos = ArrayList<PhotoList>()
        photoPaths.forEach {
            val file = File(ContentUriUtils.getFilePath(this, it))
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            if (fileSizeInMB <= Constants.LISTINGIMAGESIZEINMB) {
                ContentUriUtils.getFilePath(this, it).let {
                    val list = viewModel.addPhoto(it!!)
                    toUploadPhotos.add(list)
                    uploadMultipartSeperate(list.name!!, list.refId)
                }

            } else {
                noOfFilesLargeInSize++
            }
        }
        if (noOfFilesLargeInSize == 1) {
            showToast(getString(R.string.image_size_is_too_large))
        } else if(noOfFilesLargeInSize > 1) {
            showToast("$noOfFilesLargeInSize "+ getString(R.string.image_size_is_too_large))
        }
        viewModel.addPhotos(toUploadPhotos)
    }


    override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
        Timber.tag("ProfileFragment").d("upload - onCancelled - %d", uploadInfo?.progressPercent)
        viewModel.setError(uploadInfo?.uploadId)
    }

    override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
        Timber.tag("ProfileFragment12").d("upload - 123onProgress - %d", uploadInfo?.progressPercent)
    }

    override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
        Timber.tag("ProfileFragment").d("upload - onError - %d, response - %s, exception - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, exception.toString())
        viewModel.setError(uploadInfo?.uploadId)
    }

    override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
        Timber.tag("errorMessage").d("upload - onComplete - %d, response - %s, id - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, uploadInfo?.uploadId)
        viewModel.setCompleted(serverResponse?.bodyAsString!!)
    }


    private fun addFragment(fragment: Fragment, tag: String) {
        addFragmentToActivityAnim(mBinding.flStepTwo.id, fragment, tag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flStepTwo.id, fragment, tag)
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }



    fun openFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_right, R.anim.slide_left)
                .replace(mBinding.flStepTwo.id, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun navigateToScreen(screen: Step2ViewModel.NextScreen, vararg params: String?) {
        when(screen) {
            Step2ViewModel.NextScreen.LISTTITLE -> {
                replaceFragment(AddListTitleFragment(), "AddListPhoto")
            }
            Step2ViewModel.NextScreen.LISTDESC -> {
                if(viewModel.title.get()!!.trim().isNullOrEmpty()) {
                    showSnackbar( getString(R.string.please_add_a_title_to_your_list), getString(R.string.add_title))
                }else {
                    hideKeyboard()
                    replaceFragment(AddListDescFragment(), "ListDesc")
                }
            }
            Step2ViewModel.NextScreen.FINISH -> { this.finish() }
            else -> { }
        }
    }

    override fun onRetry() {
        if(viewModel.retryCalled.equals("")){
            viewModel.getListDetailsStep2()
        }else if(viewModel.retryCalled.contains("delete")){
            val text = viewModel.retryCalled.split("-")
            viewModel.deletephoto(text[1])
        }else {
            viewModel.updateStep2()
        }
    }

    override fun show404Page() {
        showToast(getString(R.string.list_not_available))
        this.finish()
    }
}