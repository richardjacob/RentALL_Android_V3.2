package com.rentall.radicalstart.host.photoUpload

import com.rentall.radicalstart.ui.base.BaseNavigator

interface Step2Navigator : BaseNavigator {

    fun navigateToScreen(screen : Step2ViewModel.NextScreen, vararg params: String?)

    fun show404Page()


}