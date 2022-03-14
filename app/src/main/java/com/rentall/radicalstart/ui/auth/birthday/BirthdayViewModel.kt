package com.rentall.radicalstart.ui.auth.birthday

import androidx.databinding.ObservableField
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.resource.ResourceProvider
import javax.inject.Inject

class BirthdayViewModel @Inject constructor(
        dataManager: DataManager,
        private val resourceProvider: ResourceProvider
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

    val dob = ObservableField<Array<Int>>()
    val yearLimit = ObservableField<Array<Int>>()

    init {
        dob.set(Utils.get18YearLimit())
    }

    fun showError() {
        navigator.showSnackbar(resourceProvider.getString(R.string.birthday_error),
                resourceProvider.getString(R.string.to_sign_up))
    }

    fun signUpUser() {
        try {
            navigator.navigateScreen(AuthViewModel.Screen.HOME,
                    dob.get()!![1].plus(1).toString()+"-"+dob.get()!![0].toString() +"-"+ dob.get()!![2].toString())
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

}