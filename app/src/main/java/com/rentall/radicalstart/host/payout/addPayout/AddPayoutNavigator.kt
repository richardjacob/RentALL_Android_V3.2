package com.rentall.radicalstart.host.payout.addPayout

import com.rentall.radicalstart.ui.base.BaseNavigator


interface AddPayoutNavigator: BaseNavigator {

    fun moveToScreen(screen: AddPayoutActivity.Screen)
}