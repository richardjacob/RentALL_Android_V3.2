package com.rentall.radicalstart.ui.auth.signup

import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class SignupViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<AuthNavigator>(dataManager,resourceProvider) {

}