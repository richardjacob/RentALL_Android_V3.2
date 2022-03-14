package com.rentall.radicalstart.ui.reservation

import com.rentall.radicalstart.ui.base.BaseNavigator

interface ReservationNavigator: BaseNavigator {

    fun navigateToScreen(screen : Int)

    fun show404Page()

}