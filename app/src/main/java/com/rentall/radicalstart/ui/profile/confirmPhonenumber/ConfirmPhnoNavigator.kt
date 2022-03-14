package com.rentall.radicalstart.ui.profile.confirmPhonenumber

import com.rentall.radicalstart.ui.base.BaseNavigator

interface ConfirmPhnoNavigator : BaseNavigator {

    fun navigateScreen(PHScreen: ConfirmPhnoViewModel.PHScreen, vararg params: String?)

}