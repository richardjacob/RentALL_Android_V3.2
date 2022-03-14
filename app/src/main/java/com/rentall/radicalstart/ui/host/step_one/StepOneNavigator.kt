package com.rentall.radicalstart.ui.host.step_one

import com.rentall.radicalstart.ui.base.BaseNavigator

interface StepOneNavigator : BaseNavigator {

    fun navigateScreen(NextScreen: StepOneViewModel.NextScreen)

    fun navigateBack(BackScreen : StepOneViewModel.BackScreen)

    fun show404Page()

}