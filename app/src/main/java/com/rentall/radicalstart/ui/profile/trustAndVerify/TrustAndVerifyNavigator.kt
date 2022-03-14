package com.rentall.radicalstart.ui.profile.trustAndVerify

import com.rentall.radicalstart.ui.base.BaseNavigator

interface TrustAndVerifyNavigator : BaseNavigator{

    fun show404Error(message : String )

    fun navigateToSplash()

    fun moveToPreviousScreen()
}