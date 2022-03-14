package com.rentall.radicalstart.ui.auth

import com.rentall.radicalstart.ui.base.BaseNavigator

interface AuthNavigator: BaseNavigator {

    fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?)

}