package com.rentall.radicalstart.host.payout.editpayout

import com.rentall.radicalstart.host.payout.addPayout.AddPayoutActivity
import com.rentall.radicalstart.ui.base.BaseNavigator

interface EditPayoutNavigator: BaseNavigator {

    fun disableCountrySearch(flag: Boolean)
    fun moveToScreen(screen: EditPayoutActivity.Screen)
}