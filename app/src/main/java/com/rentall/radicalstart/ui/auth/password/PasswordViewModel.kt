package com.rentall.radicalstart.ui.auth.password

import androidx.databinding.ObservableField
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class PasswordViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

    val password = ObservableField("")
    val passwordError = ObservableField(false)
    val showPassword = ObservableField(false)

    fun checkPassword() {
        try {
            navigator.hideKeyboard()
            if (password.get()!!.trim().length > 7) {
                navigator.navigateScreen(AuthViewModel.Screen.BIRTHDAY, password.get())
            } else {
                passwordError.set(true)
                navigator.showSnackbar(resourceProvider.getString(R.string.password_error),
                        resourceProvider.getString(R.string.password_limit))
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun onPasswordTextChanged() {
        if (passwordError.get()!!) {
            navigator.hideSnackbar()
        }
        passwordError.set(false)
    }

    fun showPassword() {
        showPassword.set(showPassword.get()?.not())
    }

}