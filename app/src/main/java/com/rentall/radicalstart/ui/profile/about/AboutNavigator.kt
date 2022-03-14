package com.rentall.radicalstart.ui.profile.about

import com.rentall.radicalstart.ui.base.BaseNavigator


interface AboutNavigator : BaseNavigator{

    fun navigateScreen(OpenScreen: AboutViewModel.OpenScreen, vararg params: String?)
}