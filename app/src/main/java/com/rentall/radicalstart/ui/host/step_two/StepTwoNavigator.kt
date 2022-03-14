package com.rentall.radicalstart.ui.host.step_two

import com.rentall.radicalstart.ui.base.BaseNavigator

interface StepTwoNavigator : BaseNavigator {

    fun navigateToScreen(screen : StepTwoViewModel.NextScreen,vararg params: String?)

}