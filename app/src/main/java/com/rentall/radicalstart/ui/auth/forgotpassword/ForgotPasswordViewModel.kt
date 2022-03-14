package com.rentall.radicalstart.ui.auth.forgotpassword

import androidx.databinding.ObservableField
import com.rentall.radicalstart.ForgotPasswordMutation
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        private val resourceProvider: ResourceProvider
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

    val email = ObservableField("")
    val lottieProgress = ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)

    fun emailValidation() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        try {
            if (email.get()!!.isNotEmpty()) {
                forgotPassword()
            } else {
                navigator.showError()
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun forgotPassword() {
        val buildQuery = ForgotPasswordMutation
                .builder()
                .email(email.get()!!)
                .build()

        compositeDisposable.add(dataManager.doForgotPasswordApiCall(buildQuery)
                .doOnSubscribe {
                    setIsLoading(true)
                    lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
                }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.userForgotPassword()
                        if (data?.status() == 200) {
                            lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                            navigator.showToast(resourceProvider.getString(R.string.link_msg, email.get()!!))
                            navigator.navigateScreen(AuthViewModel.Screen.POPUPSTACK)
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                            navigator.showToast(data?.errorMessage()!!)
                            navigator.navigateScreen(AuthViewModel.Screen.REMOVEALLBACKSTACK)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                    }
                }, {
                    handleException(it)
                    lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                } )
        )
    }

}