package com.rentall.radicalstart.ui.listing

import com.rentall.radicalstart.ui.base.BaseNavigator

interface ListingNavigator : BaseNavigator {

    fun openBillingActivity(isProfilePresent: Boolean)

    fun removeSubScreen()

    fun show404Screen()
}