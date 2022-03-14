package com.rentall.radicalstart.ui.host.hostListing

import com.rentall.radicalstart.ui.base.BaseNavigator

interface HostListingNavigator : BaseNavigator {
    fun show404Screen()

    fun showListDetails()

    fun showNoListMessage()

    fun hideLoading()
}