package com.rentall.radicalstart.ui.profile.edit_profile

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.EditProfileMutation
import com.rentall.radicalstart.GetProfileQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<EditProfileNavigator>(dataManager,resourceProvider) {

    val firstName = ObservableField("")
    val lastName = ObservableField("")
    val aboutMe = ObservableField("")
    val gender = ObservableField("")
    val genderLanguage = ObservableField("")
    val pic = ObservableField("")
    val dob = ObservableField("")
    val email = ObservableField("")
    val phone = ObservableField("")
    val languages = ObservableField("")
    val languagesValue = ObservableField("")
    val location = ObservableField("")
    val currency = ObservableField("")
    val createdAt = ObservableField("")
    val emailVerified = ObservableField<Boolean>()
    val fbVerified = ObservableField<Boolean>()
    val googleVerified = ObservableField<Boolean>()
    val phVerified = ObservableField<Boolean>()
    val isProgressLoading = ObservableBoolean(false)

    val temp = ObservableField("")
    val temp1 = ObservableField("")

    val layoutId = ObservableField(0)

    val dob1 = ObservableField<Array<Int>>()

    fun done() {
        if (checkIdAndToken()) {
            when (layoutId.get()) {
                R.layout.include_edit_email -> { checkEmail() }
                R.layout.include_edit_phone -> { checkPhone() }
                R.layout.include_edit_location -> { checkLocation() }
                R.layout.include_edit_aboutme -> { checkAboutMe() }
                R.layout.include_edit_name -> { checkName() }
            }
        } else {
            navigator.showError()
        }
    }

    private fun checkIdAndToken(): Boolean {
        return !(dataManager.firebaseToken.isNullOrEmpty() && dataManager.currentUserId.isNullOrEmpty())
    }

    private fun checkName() {
        if (temp.get().toString().trim().isNotEmpty() && temp1.get().toString().trim().isNotEmpty()) {
            val strings = arrayOf(temp.get().toString().trim(), temp1.get().toString().trim())
            updateProfile("firstName", Gson().toJson(strings))
            navigator.moveToBackScreen()
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_name),
                    resourceProvider.getString(R.string.invalid_name_desc))
        }
    }

    private fun checkEmail() {
        if (Utils.isValidEmail(temp.get().toString().trim())) {
            updateProfile("email", temp.get().toString().trim())
            navigator.moveToBackScreen()
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_email),
                    resourceProvider.getString(R.string.invalid_email_desc))
        }
    }

    private fun checkPhone() {
        if (temp.get().toString().trim().length > 6) {
            updateProfile("phoneNumber", temp.get().toString().trim())
            navigator.moveToBackScreen()
        } else {
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_phone),
                    resourceProvider.getString(R.string.invalid_phone_desc))
        }
    }

    private fun checkLocation() {

        if(temp.get()!!.trim().isNotEmpty()){
            updateProfile("location", temp.get().toString().trim())
            navigator.moveToBackScreen()
        }else{
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_location),
                    resourceProvider.getString(R.string.location_should_not_be_empty))
        }

    }

    private fun checkAboutMe() {
        if(temp.get()!!.trim().isNotEmpty()){
            updateProfile("info", temp.get().toString().trim())
            navigator.moveToBackScreen()
        }else{
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_user_info),
                    resourceProvider.getString(R.string.invalid_info))
        }

    }

    fun updateProfile(fieldName: String, fieldValue: String) {
        try {
            val mutate = EditProfileMutation
                    .builder()
                    .fieldName(fieldName)
                    .fieldValue(fieldValue)
                    .deviceId(dataManager.firebaseToken!!)
                    .userId(dataManager.currentUserId!!)
                    .deviceType(Constants.deviceType)
                    .build()

            compositeDisposable.add(dataManager.doEditProfileApiCall(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data()!!.userUpdate()
                            if (data?.status() == 200) {
                                setReturnData(data, fieldName, fieldValue)
                            } else if(data?.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                navigator.showToast(resourceProvider.getString(R.string.Error_msg, fieldName.capitalize()))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        navigator.showToast(resourceProvider.getString(R.string.Error_msg, fieldName.capitalize()))
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }

    }

    private fun setReturnData(data: EditProfileMutation.UserUpdate, fieldName: String, fieldValue: String) {
        try {
            when (fieldName) {
                "firstName" -> {
                    firstName.set(temp.get())
                    lastName.set(temp1.get())
                    dataManager.currentUserName = firstName.get() +" "+ lastName.get()
                    dataManager.currentUserFirstName = firstName.get()
                    dataManager.currentUserLastName = lastName.get()
                }
                "phoneNumber" -> { phone.set(temp.get()) }
                "location" -> { location.set(temp.get()) }
                "info" -> { aboutMe.set(temp.get()) }
                "gender" -> {
                    when (fieldValue.capitalize()) {
                        "Male" -> {
                            genderLanguage.set(resourceProvider.getString(R.string.radio_male))
                        }
                        "Female" -> {
                            genderLanguage.set(resourceProvider.getString(R.string.radio_female))
                        }
                        "Other" -> {
                            genderLanguage.set(resourceProvider.getString(R.string.radio_other))
                        }
                    }
                    gender.set(fieldValue.capitalize())
                }
                "preferredLanguage" -> {
                    languages.set(temp.get())
                    languagesValue.set(fieldValue)
                    //navigator.setLocale(fieldValue)

                }
                "dateOfBirth" -> {
                    dob.set(dob1.get()!![1].toString() + "-" + dob1.get()!![0].toString() + "-" + dob1.get()!![2].toString())
                }
                "email" -> {
                    if (!data.userToken().isNullOrEmpty()) {
                        dataManager.updateAccessToken(data.userToken())
                    }
                    email.set(temp.get())
                }
                "preferredCurrency" -> {
                    currency.set(fieldValue.capitalize())
//                    dataManager.currentUserCurrency = fieldValue
//                    navigator.openSplashScreen()
                }
                "isEmailConfirmed" -> {

                }
                "isFacebookConnected" -> {

                }
                "isGoogleConnected" -> {

                }
                "isPhoneVerified" -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun setPictureInPref(pic: String?) {
        dataManager.currentUserProfilePicUrl = pic
    }

    fun updateCurrency(it: String) {
        currency.set(it)
        //dataManager.currentUserCurrency = it
    }

    fun getProfileDetails() {
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
                            setData(data.result()!!)
                            navigator.showLayout()
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    fun setData(data: GetProfileQuery.Result) {
        dataManager.currentUserProfilePicUrl = data.picture()
        pic.set(data.picture())

        if (!data.preferredCurrency().isNullOrEmpty()) {
            currency.set(data.preferredCurrency())
//            if (dataManager.currentUserCurrency != data.preferredCurrency()) {
//                navigator.openSessionExpire()
//            } else {
//                dataManager.currentUserCurrency = data.preferredCurrency()
//            }
        }

        if(!data.firstName().isNullOrEmpty() && !data.lastName().isNullOrEmpty()) {
            firstName.set(data.firstName())
            lastName.set(data.lastName())
            dataManager.currentUserName = firstName.get() +" "+ lastName.get()
            dataManager.currentUserFirstName = firstName.get()
            dataManager.currentUserLastName = lastName.get()
        }

        if(!data.info().isNullOrEmpty()) {
            aboutMe.set(data.info())
        }
        if(!data.gender().isNullOrEmpty()) {
            gender.set(data.gender()?.capitalize())
            when (data.gender()?.capitalize()) {
                "Male" -> {
                    genderLanguage.set(resourceProvider.getString(R.string.radio_male))
                }
                "Female" -> {
                    genderLanguage.set(resourceProvider.getString(R.string.radio_female))
                }
                "Other" -> {
                    genderLanguage.set(resourceProvider.getString(R.string.radio_other))
                }
            }
        }
        if(!data.dateOfBirth().isNullOrEmpty()) {
            dob.set(data.dateOfBirth())
            val string = data.dateOfBirth()?.split("-")
            string?.let {
                if (it.isNotEmpty()) {
                    dob1.set(arrayOf(string[1].toInt(), string[0].toInt(), string[2].toInt()))
                }
            }
        } else {
            dob1.set((Utils.get18YearLimit()))
        }
        if(!data.email().isNullOrEmpty()) {
            email.set(data.email())
        }
        if(!data.phoneNumber().isNullOrEmpty()) {
            phone.set(data.countryCode() + " " + data.phoneNumber())
            dataManager.currentUserPhoneNo = data.countryCode() + " " + data.phoneNumber()
        }
        if(!data.preferredLanguageName().isNullOrEmpty()) {
            languages.set(data.preferredLanguageName())
            languagesValue.set(data.preferredLanguage())
//            dataManager.currentUserLanguage = data.preferredLanguage()
        }
        if(!data.location().isNullOrEmpty()) {
            location.set(data.location())
        }
        if (!data.createdAt().isNullOrEmpty()) {
            createdAt.set(data.createdAt())
        }

        data.verification()?.let {
            dataManager.isEmailVerified = it.isEmailConfirmed
            dataManager.isIdVerified = it.isIdVerification
            dataManager.isGoogleVerified = it.isGoogleConnected
            dataManager.isFBVerified = it.isFacebookConnected
            dataManager.isPhoneVerified = it.isPhoneVerified
            emailVerified.set(dataManager.isEmailVerified)
            fbVerified.set(dataManager.isFBVerified)
            googleVerified.set(dataManager.isGoogleVerified)
            phVerified.set(dataManager.isPhoneVerified)
        }
    }

    fun onTextChanged(text: CharSequence) {
        temp.set(text.toString())
        navigator.hideSnackbar()
    }

    fun onTextChanged1(text: CharSequence) {
        temp1.set(text.toString())
        navigator.hideSnackbar()
    }

    fun clickEvent(layoutID: Int) {
        layoutId.set(layoutID)
        navigator.openEditScreen()
    }
}