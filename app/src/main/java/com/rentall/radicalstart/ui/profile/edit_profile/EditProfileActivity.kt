package com.rentall.radicalstart.ui.profile.edit_profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityProfileBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.dobcalendar.BirthdayDialog
import com.rentall.radicalstart.ui.profile.confirmPhonenumber.ConfirmPhnoActivity
import com.rentall.radicalstart.ui.profile.currency.CurrencyDialog
import com.rentall.radicalstart.ui.profile.languages.LanguagesDialog
import com.rentall.radicalstart.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.util.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.reactivex.disposables.CompositeDisposable
import net.gotev.uploadservice.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val RC_LOCATION_PERM: Int = 123
val REQUEST_CODE: Int = 1

class EditProfileActivity : BaseActivity<ActivityProfileBinding, EditProfileViewModel>(),
        EasyPermissions.PermissionCallbacks, EditProfileNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityProfileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_profile
    override val viewModel: EditProfileViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(EditProfileViewModel::class.java)
    lateinit var openDialog1: AlertDialog
     var uri: Uri =Uri.EMPTY
    private var eventCompositeDisposal = CompositeDisposable()
    private var isFromImagePicker = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        initRxBus()
    }

    @SuppressLint("LongLogTag")
    private fun initView() {
        viewModel.getProfileDetails()
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.ivAvatar.onClick { askCameraPermission() }
        mBinding.actionBar.ivNavigateup.onClick { finish() }
        mBinding.actionBar.ivCameraToolbar.onClick { askCameraPermission() }
        mBinding.rlGender.onClick { openGenderDialog() }
        mBinding.rlPhone.onClick { openConfirmPhnoActivity() }
        mBinding.rlLanguages.onClick {
            viewModel.languagesValue.get()?.let {
                LanguagesDialog.newInstance(it).show(supportFragmentManager)
            }
        }
        mBinding.rlCurrency.onClick {
            viewModel.currency.get()?.let {
                CurrencyDialog.newInstance(it).show(supportFragmentManager)
            }
        }
        mBinding.rlBirthday.onClick { openCalender() }
        mBinding.rlEmailVerified.onClick {
            TrustAndVerifyActivity.openActivity(this, "profile", "email")
        }
        mBinding.rlFbVerified.onClick {
            TrustAndVerifyActivity.openActivity(this, "profile", "facebook")
        }
        mBinding.rlGoogleVerififed.onClick {
            TrustAndVerifyActivity.openActivity(this, "profile", "google")
        }
        mBinding.rlPhVerified.onClick {
            val intent = Intent(this, ConfirmPhnoActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

        if (viewModel.dataManager.isEmailVerified!!){
            mBinding.ivEmailVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tick))
        }else{
            mBinding.ivEmailVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_right_arrow_red))
        }
        if (viewModel.dataManager.isFBVerified!!){
            mBinding.ivFbVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tick))
        }else{
            mBinding.ivFbVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_right_arrow_red))
        }
        if (viewModel.dataManager.isGoogleVerified!!){
            mBinding.ivGoogleVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tick))
        }else{
            mBinding.ivGoogleVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_right_arrow_red))
        }
        if (viewModel.dataManager.isPhoneVerified!!){
            mBinding.ivPhoneVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tick))
        }else{
            mBinding.ivPhoneVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_right_arrow_red))
        }

        Log.d("getting details of verified info from email",  "fb, google, phone "+viewModel.dataManager.isEmailVerified!!+" " +viewModel.dataManager.isFBVerified +" "+viewModel.dataManager.isGoogleVerified!! +" "+viewModel.dataManager.isPhoneVerified!!)
        if (viewModel.dataManager.isEmailVerified!!) {
            mBinding.tvEmailConfirm.setText(getString(R.string.email_confirmed))
            mBinding.rlEmailVerified.isClickable = false
        }else {
            mBinding.tvEmailConfirm.setText(getString(R.string.confirm_email))
            mBinding.rlEmailVerified.isClickable = true
        }
        if (viewModel.dataManager.isFBVerified!!) {
            mBinding.tvFbVerified.setText(getString(R.string.facebook_connected))
            // rl_fb_verified.isClickable = true
        }else{
            mBinding.tvFbVerified.setText(getString(R.string.connect_fb))
            // rl_fb_verified.isClickable = false
        }
        if (viewModel.dataManager.isGoogleVerified!!){
            mBinding.tvGoogleVerified.setText(getString(R.string.google_connected))
            //rl_google_verififed.isClickable = true
        }else{
            mBinding.tvGoogleVerified.setText(getString(R.string.connect_google))
            //rl_google_verififed.isClickable = false
        }
        if (viewModel.dataManager.isPhoneVerified!!){
            mBinding.tvPhVerified.setText(getString(R.string.phone_verified))
            mBinding.rlPhVerified.isClickable = false
        }else{
            mBinding.tvPhVerified.setText(getString(R.string.verify_phone))
            mBinding.rlPhVerified.isClickable = true
        }
    }

    private fun initRxBus() {
        eventCompositeDisposal.add(RxBus.listen(Array<String>::class.java)
                .subscribe { event ->
                    event?.let {
                        if (it.size == 1) {
                            viewModel.updateProfile("preferredCurrency", it[0])
                        } else {
                            viewModel.updateProfile("preferredLanguage", it[0])
                            viewModel.temp.set(it[1])
                        }
                        this.onBackPressed()
                    }
                })
        eventCompositeDisposal.add(RxBus.listen(String::class.java)
                .subscribe { event ->
                    event?.let {
                        viewModel.updateCurrency(it)
                        this.onBackPressed()
                    }
                })
    }

    @SuppressLint("InflateParams")
    private fun openGenderDialog() {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val dialogView = layoutInflater.inflate(R.layout.dialog_gender, null)
        dialogBuilder.setTitle(resources.getString(R.string.gender))
        dialogBuilder.setView(dialogView)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioSex)
        when (viewModel.gender.get()) {
            "Male" -> {
                radioGroup.check(R.id.radioMale)
            }
            "Female" -> {
                radioGroup.check(R.id.radioFemale)
            }
            "Other" -> {
                radioGroup.check(R.id.radioOther)
            }
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            Log.d("jh123",checkedId.toString())
           // val radioButton = dialogView.findViewById<View>(checkedId) as RadioButton
            when (checkedId) {
                R.id.radioMale -> viewModel.updateProfile("gender", "Male")
                R.id.radioFemale -> viewModel.updateProfile("gender", "Female")
                R.id.radioOther -> viewModel.updateProfile("gender", "Other")
            }
            openDialog1.dismiss()
        }
        openDialog1 = dialogBuilder.create()
        openDialog1.show()
    }

    override fun openEditScreen() {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flProfile.id, EditProfileFragment(), "editProfile")
                .addToBackStack(null)
                .commit()
    }

    private fun openConfirmPhnoActivity() {
        val intent = Intent(this, ConfirmPhnoActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun openCalender() {
        hideSnackbar()
        try {
            val birthdayDialog = BirthdayDialog.newInstance(
                    viewModel.dob1.get()!![0],
                    viewModel.dob1.get()!![1] - 1,
                    viewModel.dob1.get()!![2]
            )
            birthdayDialog.show(supportFragmentManager)
            birthdayDialog.setCallBack(DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                if (Utils.getAge(year, month, dayOfMonth) >= 18) {
                    viewModel.dob1.set(arrayOf(year, month + 1, dayOfMonth))
                    viewModel.updateProfile("dateOfBirth", month.plus(1).toString() + "-" + year.toString() + "-" + dayOfMonth.toString())
                } else {
                    showSnackbar(resources.getString(R.string.birthday_error),
                            resources.getString(R.string.age_18_limit))
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
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
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setAspectRatio(1, 1)
                .start(this)
    }

    private fun uploadMultipart(uri: Uri) {
        try {
      //      viewModel.isLoading.set(true)
            viewModel.isProgressLoading.set(true)
            val config = UploadNotificationConfig()
            config.completed.autoClear = true
            config.error.autoClear = true
            config.cancelled.autoClear = true
            MultipartUploadRequest(this, Constants.WEBSITE + Constants.uploadPhoto)
                    .addFileToUpload(uri.path, "file")
                    .addHeader("auth", viewModel.getAccessToken())
                    .setNotificationConfig(config)
                    .setMaxRetries(2)
                    .setDelegate(object : UploadStatusDelegate {
                        override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                            Timber.tag("ProfileFragment").d("upload - onCancelled")
                //            viewModel.isLoading.set(false)
                            viewModel.isProgressLoading.set(false)
                        }

                        override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {
                            Timber.tag("ProfileFragment").d("upload - onProgress - %d", uploadInfo?.progressPercent)
                 //           viewModel.isLoading.set(true)
                            viewModel.isProgressLoading.set(true)
                        }

                        override fun onError(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?, exception: java.lang.Exception?) {
                            Timber.tag("ProfileFragment").d("upload - onError - %d, response - %s, exception - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString, exception.toString())
                  //          viewModel.isLoading.set(false)
                            viewModel.isProgressLoading.set(false)
                            showError()
                        }

                        override fun onCompleted(context: Context?, uploadInfo: UploadInfo?, serverResponse: ServerResponse?) {
                            Timber.tag("ProfileFragment").d("upload - onComplete - %d, response - %s", uploadInfo?.progressPercent, serverResponse?.bodyAsString)
                            val convertedObject = Gson().fromJson(serverResponse?.bodyAsString, JsonObject::class.java)
                            if (convertedObject.get("status").asInt == 200) {
                   //             viewModel.isLoading.set(false)
                                Handler().postDelayed(Runnable {
                                    viewModel.isProgressLoading.set(false)
                                    val array = convertedObject.get("file").asJsonObject
                                    Timber.tag("fileName").d(array.get("filename").asString)
                                    viewModel.pic.set(array.get("filename").asString)
                                    viewModel.setPictureInPref(array.get("filename").asString)
                                },3000)
                                /*Handler(Looper.getMainLooper()).postDelayed( {

                                }, 2000)*/
                            }else{
                                viewModel.isProgressLoading.set(false)
                                showSnackbar(getString(R.string.upload_failed),convertedObject.get("errorMessage").toString())
                            }
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Timber.tag("AndroidUploadService").e(exc)
            showError()
      //      viewModel.isLoading.set(false)
            viewModel.isProgressLoading.set(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            /*Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                    .show()*/
        }
        if (Activity.RESULT_OK == resultCode && data != null) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = result.uri
                        validateFileSize(resultUri)
                        isFromImagePicker = true
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        showError()
                    }
                }
                AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                    Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
                }
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            showToast(getString(R.string.unsupported_format))
        }

    }

    private fun validateFileSize(uri: Uri) {
        this.uri=uri
        val file = File(uri.path)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        if (fileSizeInMB <= Constants.PROFILEIMAGESIZEINMB) {
            try {
                if (isNetworkConnected) {
                    uploadMultipart(uri)
                } else {
                    showToast(resources.getString(R.string.currently_offline))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showToast(resources.getString(R.string.image_size_is_too_large))
        }
    }

    override fun openSplashScreen() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onDestroy() {
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    override fun moveToBackScreen() {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideSnackbar()
    }

    override fun onRetry() {
        if (isNetworkConnected) {
            if(Uri.EMPTY != uri){
                uploadMultipart(uri)
            }
        } else {
            showToast(resources.getString(R.string.currently_offline))
        }
        viewModel.getProfileDetails()
    }

    override fun onResume() {
        super.onResume()
        initView()
        if (isFromImagePicker.not()) {
            isFromImagePicker = false
        }


    }

    override fun showLayout() {
        mBinding.scrollProfile.visible()
        mBinding.actionBar.ivCameraToolbar.visible()
    }

    override fun setLocale(key: String) {

        if (key == "en") {
            LocaleHelper.setLocale(this, "en")
            openSplashScreen()
        } else if(key == "es") {
            LocaleHelper.setLocale(this, "es")
            openSplashScreen()
        } else if(key == "fr") {
            LocaleHelper.setLocale(this, "fr")
            openSplashScreen()
        } else if(key == "pt") {
            LocaleHelper.setLocale(this, "pt")
            openSplashScreen()
        }else if(key == "ar") {
            LocaleHelper.setLocale(this, "ar")
            openSplashScreen()
        }
        else if(key == "iw") {
            LocaleHelper.setLocale(this, "iw")
            openSplashScreen()
        }
        else if(key == "he") {
            LocaleHelper.setLocale(this, "he")
            openSplashScreen()
        }

    }
}