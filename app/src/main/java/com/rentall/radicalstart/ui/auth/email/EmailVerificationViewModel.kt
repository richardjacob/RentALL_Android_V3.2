package com.rentall.radicalstart.ui.auth.email

import androidx.databinding.ObservableField
import com.rentall.radicalstart.CheckEmailExistsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class EmailVerificationViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

    val email = ObservableField("")
    val emailError = ObservableField(false)
    val lottieProgress = ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)

    fun checkEmail() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        if (Utils.isValidEmail(email.get()!!)) {
            lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
            emailVerification()
        } else {
            emailError.set(true)
            navigator.showSnackbar(resourceProvider.getString(R.string.invalid_email),
                    resourceProvider.getString(R.string.invalid_email_desc))
        }
    }

    fun onEmailTextChanged() {
        emailError.set(false)
    }

    private fun emailVerification() {
        val buildQuery = CheckEmailExistsQuery
                .builder()
                .email(email.get()!!)
                .build()

        compositeDisposable.add(dataManager.doEmailVerificationApiCall(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        if (response.data()?.validateEmailExist()?.status() == 200) {
                            lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                            navigator.navigateScreen(AuthViewModel.Screen.PASSWORD, email.get()!!)
                        }else if(response.data()?.validateEmailExist()?.status() == 500) {
                            navigator.openSessionExpire()
                        }
                        else {
                            lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                            navigator.showSnackbar(resourceProvider.getString(R.string.email_already_exists),
                                    resourceProvider.getString(R.string.login), resourceProvider.getString(R.string.signup))
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                    handleException(it)
                } )
        )
    }

}