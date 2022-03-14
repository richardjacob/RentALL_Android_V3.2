package com.rentall.radicalstart.ui.inbox

import com.rentall.radicalstart.SendMessageMutation
import com.rentall.radicalstart.ui.base.BaseNavigator

interface InboxNavigator: BaseNavigator {

    fun moveToBackScreen()

    fun addMessage(text: SendMessageMutation.Results)

    fun openBillingActivity()

    fun openListingDetails()

    fun hideTopView(msg:String)
}