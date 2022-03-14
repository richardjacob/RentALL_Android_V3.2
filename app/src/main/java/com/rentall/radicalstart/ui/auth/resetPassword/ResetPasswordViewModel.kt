package com.rentall.radicalstart.ui.auth.resetPassword

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ResetPasswordMutation
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ResetPasswordViewModel @Inject constructor(
        dataManager: DataManager,
        val resourceProvider: ResourceProvider,
        private val scheduler: Scheduler
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

    init {
        dataManager.isUserFromDeepLink = true
    }

    val password = ObservableField("")
    val confirmPassword = ObservableField("")
    val lottieProgress = ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)

    val showPassword = ObservableField(false)
    val showPassword1 = ObservableField(false)

    val email = MutableLiveData<String>()
    val token = MutableLiveData<String>()

    fun showPassword() {
        showPassword.set(showPassword.get()?.not())
    }

    fun showPassword1() {
        showPassword1.set(showPassword1.get()?.not())
    }

    fun validateData() {
        try {
            if (email.value!!.isNotEmpty() &&
                    password.get()!!.isNotEmpty() &&
                    token.value!!.isNotEmpty()) {
                navigator.hideKeyboard()
                resetPassword()
            } else {
                navigator.showError()
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun resetPassword() {
        lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
        val buildQuery = ResetPasswordMutation
                .builder()
                .email(email.value!!)
                .password(password.get()!!)
                .token(token.value!!)
                .build()

        compositeDisposable.add(dataManager.doResetPasswordApiCall(buildQuery)
                .delay(5, TimeUnit.SECONDS)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.updateForgotPassword()
                        if (data?.status() == 200) {
                            lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                            navigator.showToast(resourceProvider.getString(R.string.success_reset_msg))
                            dataManager.setUserAsLoggedOut()
                            navigator.navigateScreen(AuthViewModel.Screen.AuthScreen)
                        } else if (data?.status() == 500) {
                            lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                            navigator.showSnackbar(resourceProvider.getString(R.string.reset_password_error),
                                    resourceProvider.getString(R.string.reset_password_error_desc))
                        } else {
                            lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                            navigator.showToast(data?.errorMessage()!!)
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

    override fun onCleared() {
        dataManager.isUserFromDeepLink = false
        super.onCleared()
    }
}