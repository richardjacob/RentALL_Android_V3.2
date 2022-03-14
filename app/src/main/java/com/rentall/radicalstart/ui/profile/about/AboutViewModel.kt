package com.rentall.radicalstart.ui.profile.about

import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class AboutViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<AboutNavigator>(dataManager, resourceProvider) {


    enum class OpenScreen {
        WHY_HOST,
        FINISHED
    }

    fun onClick(openScreen: AboutViewModel.OpenScreen) {
        navigator.navigateScreen(openScreen)
    }
}