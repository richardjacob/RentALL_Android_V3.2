package com.rentall.radicalstart.ui.saved

import com.rentall.radicalstart.ui.base.BaseNavigator

interface SavedNavigator : BaseNavigator {

    fun moveUpScreen()

    fun showEmptyMessageGroup()

    fun reloadExplore()
}