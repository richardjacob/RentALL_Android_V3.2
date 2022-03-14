package com.rentall.radicalstart.ui.auth.name

import androidx.databinding.ObservableField
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class NameCreationViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val firstName = ObservableField("")
    val lastName = ObservableField("")

//    fun validateData(): Boolean {
//        return !(firstName.get()!!.trim().isNullOrEmpty() || lastName.get()!!.trim().isNullOrEmpty())
//    }

}