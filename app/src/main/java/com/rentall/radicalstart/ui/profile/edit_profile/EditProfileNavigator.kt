package com.rentall.radicalstart.ui.profile.edit_profile

import com.rentall.radicalstart.ui.base.BaseNavigator


interface EditProfileNavigator: BaseNavigator {

    fun openEditScreen()

    fun openSplashScreen()

    fun moveToBackScreen()

    fun showLayout()

    fun setLocale(key: String)
}