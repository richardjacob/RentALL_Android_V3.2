package com.rentall.radicalstart.ui.profile

import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.GetProfileQuery
import com.rentall.radicalstart.LogoutMutation
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.ProfileDetails
import javax.inject.Inject


class ProfileViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<ProfileNavigator>(dataManager,resourceProvider) {

    lateinit var profileDetails: MutableLiveData<ProfileDetails>
    val loading = MutableLiveData<Boolean>()

    init {
        loading.value = true
    }

    fun loadProfileDetails() : MutableLiveData<ProfileDetails> {
        if (!::profileDetails.isInitialized) {
            profileDetails = MutableLiveData()
            getDataFromPref()
        }
        return profileDetails
    }

    fun getDataFromPref() {
        profileDetails.value = ProfileDetails(
                userName = dataManager.currentUserName,
                createdAt = dataManager.currentUserCreatedAt,
                picture = dataManager.currentUserProfilePicUrl,
                emailVerification = dataManager.isEmailVerified,
                idVerification = dataManager.isIdVerified,
                googleVerification = dataManager.isGoogleVerified,
                fbVerification = dataManager.isFBVerified,
                phoneVerification = dataManager.isPhoneVerified,
                email = dataManager.currentUserEmail,
                userType = dataManager.currentUserType,
                addedList = dataManager.isListAdded
        )
    }

    fun getProfileDetails() {
        navigator.showProfileDetails()

        val buildQuery = GetProfileQuery
                        .builder()
                        .build()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
                .doOnSubscribe { loading.postValue(false) }
                .doFinally { loading.postValue(true) }
                //.delay(10, TimeUnit.SECONDS)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.userAccount()
                        if (!data?.result()?.preferredCurrency().isNullOrEmpty()) {
//                            if (dataManager.currentUserCurrency != data?.result()?.preferredCurrency()) {
//                                navigator.openSessionExpire()
//                            } else {
//                                dataManager.currentUserCurrency = data?.result()?.preferredCurrency()
//                            }
                        }
                        responseValidation(data?.status()!!, action = { saveData(data.result()!!) })
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    private fun saveData(data: GetProfileQuery.Result) {
        try {
            if (!data.preferredCurrency().isNullOrEmpty()) {
//                if (dataManager.currentUserCurrency != data.preferredCurrency()) {
//                    navigator.openSessionExpire()
//                } else {
//                    dataManager.currentUserCurrency = data.preferredCurrency()
//                }
            }

            profileDetails.value = ProfileDetails(
                    userName = data.firstName() + " " + data.lastName(),
                    createdAt = data.createdAt(),
                    picture = data.picture(),
                    email = data.email(),
                    emailVerification = data.verification()?.isEmailConfirmed,
                    idVerification = data.verification()?.isIdVerification,
                    googleVerification= data.verification()?.isGoogleConnected,
                    fbVerification = data.verification()?.isFacebookConnected,
                    phoneVerification = data.verification()?.isPhoneVerified,
                    userType = data.loginUserType(),
                    addedList = data.isAddedList
            )
            
            // Save data in pref
            dataManager.currentUserProfilePicUrl = data.picture()
            dataManager.currentUserFirstName = data.firstName()
            dataManager.currentUserLastName = data.lastName()
            dataManager.isEmailVerified = data.verification()?.isEmailConfirmed
            dataManager.isIdVerified = data.verification()?.isIdVerification
            dataManager.isGoogleVerified = data.verification()?.isGoogleConnected
            dataManager.isFBVerified = data.verification()?.isFacebookConnected
            dataManager.isPhoneVerified = data.verification()?.isPhoneVerified
            dataManager.currentUserCreatedAt = data.createdAt()
            dataManager.currentUserEmail = data.email()
            dataManager.currentUserType = data.loginUserType()
            dataManager.isListAdded = data.isAddedList
            if(data.dateOfBirth().isNullOrEmpty()){
                dataManager.isDOB = false
            }else{
                dataManager.isDOB = true
            }

            navigator.showProfileDetails()
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun signOut() {
        val buildQuery = LogoutMutation
                .builder()
                .deviceType(Constants.deviceType)
                .deviceId(dataManager.firebaseToken.toString())
                .build()
        compositeDisposable.add(dataManager.doLogoutApiCall(buildQuery)
              //  .delay(5, TimeUnit.SECONDS)
              //  .doOnSubscribe { setIsLoading(true) }
                .doFinally { //setIsLoading(false);
                    afterSignOut()
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe( {
                    afterSignOut()
                    navigator.navigateToSplash()
                }, {
                    afterSignOut()
                    navigator.navigateToSplash()
                })
        )
    }

    private fun afterSignOut() {
        dataManager.setUserAsLoggedOut()
    }

//    private inner class Client : WebViewClient() {
//        override fun onReceivedError(view: WebView, request: WebResourceRequest,
//                                     error: WebResourceError) {
//            super.onReceivedError(view, request, error)
////            setHideProgress(true)
//        }
//
//        override fun onPageFinished(view: WebView, url: String) {
//            super.onPageFinished(view, url)
////            setHideProgress(true)
//        }
//    }
//
//    public fun getWebClient(): WebViewClient{
//        return Client()
//    }

}