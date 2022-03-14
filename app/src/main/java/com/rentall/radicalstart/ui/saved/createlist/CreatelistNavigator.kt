package com.rentall.radicalstart.ui.saved.createlist

import com.rentall.radicalstart.ui.base.BaseNavigator

interface CreatelistNavigator: BaseNavigator {

    fun navigate(isLoadSaved: Boolean)

}