package com.rentall.radicalstart.ui.auth.login

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.LoginQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Event
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.Outcome
import javax.inject.Inject

class LoginViewModel @Inject constructor (
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

    val email = ObservableField("")
    val password = ObservableField("")
    val lottieProgress = ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)
    val showPassword = ObservableField(false)

    private val generateFirebase = MutableLiveData<String>()
    val fireBaseResponse: LiveData<Event<Outcome<String>>>? = Transformations.switchMap(generateFirebase) {
        dataManager.generateFirebaseToken()
    }

    private fun validateFirebase() : Boolean {
        return dataManager.firebaseToken != null
    }

    fun checkLogin() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        try {
            if (validateFirebase()) {
                if (Utils.isValidEmail(email.get()!!) && password.get()!!.isNotEmpty()) {
                    loginUser()
                } else {
                    navigator.showSnackbar(resourceProvider.getString(R.string.invalid_email),
                            resourceProvider.getString(R.string.invalid_email_desc))
                }
            } else {
                generateFirebase.value = "Login"
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun showPassword() {
        navigator.hideSnackbar()
        showPassword.set(showPassword.get()?.not())
    }

    private fun loginUser() {
        val buildQuery = LoginQuery
                .builder()
                .email(email.get()!!)
                .password(password.get()!!)
                .deviceType(Constants.deviceType)
                .deviceDetail("")
                .deviceId(dataManager.firebaseToken!!)
                .build()

        compositeDisposable.add(dataManager.doServerLoginApiCall(buildQuery)
                .doOnSubscribe {
                    setIsLoading(true)
                    lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
                }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.userLogin()?.status() == 200) {
                            lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                            val data = response.data()!!.userLogin()
                            saveDataInPref(data!!.result()!!)
                        }else if(response.data()?.userLogin()?.status() == 500) {
                            if ("blocked"  in response.data()?.userLogin()?.errorMessage() ?: "" ) {
                                navigator.showToast(
                                    response.data()?.userLogin()?.errorMessage() ?: ""

                                )
                                lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                            }
                            else
                                navigator.openSessionExpire()
                        }
                        else {
                            lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_login_credentials),
                                    resourceProvider.getString(R.string.show_password), resourceProvider.getString(R.string.login_txt))
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                        navigator.showError()
                    }
                }, {
                    lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                    handleException(it)
                } )
        )
    }

    private fun saveDataInPref(data: LoginQuery.Result?) {
        val userCurrency = if (data?.user()?.preferredCurrency() == null) {
            dataManager.currencyBase
        } else {
            data.user()?.preferredCurrency()
        }
        dataManager.updateUserInfo(
                data?.userToken(),
                data?.userId(),
                DataManager.LoggedInMode.LOGGED_IN_MODE_SERVER,
                data?.user()?.firstName() + " " + data?.user()?.lastName(),
                null,
                data?.user()?.picture(),
                userCurrency,
                data?.user()?.preferredLanguage(),
                data?.user()?.createdAt()
        )
        try {
            data?.user()?.verification()?.let {
                dataManager.updateVerification(it.isPhoneVerified,
                        it.isEmailConfirmed,
                        it.isIdVerification,
                        it.isGoogleConnected,
                        it.isFacebookConnected)
            }
            moveToScreen()
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun moveToScreen() {
        navigator.navigateScreen(AuthViewModel.Screen.MOVETOHOME)
    }
}