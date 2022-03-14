package com.rentall.radicalstart.ui.dobcalendar

import android.annotation.SuppressLint
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

@SuppressLint("LogNotTimber")
class BirthdayViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

}