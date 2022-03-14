package com.rentall.radicalstart.ui.host.step_three

import com.rentall.radicalstart.ui.base.BaseNavigator

interface StepThreeNavigator : BaseNavigator {

    fun navigateToScreen(screen : StepThreeViewModel.NextStep)

    fun show404Page()
}