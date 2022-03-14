package com.rentall.radicalstart.ui.profile.setting

import com.rentall.radicalstart.ui.base.BaseNavigator

interface SettingsNavigator : BaseNavigator  {

    fun openSplashScreen()

    fun navigateToSplash()

    fun setLocale(key: String)

}