package com.rentall.radicalstart.ui.profile.review

import android.view.View
import com.rentall.radicalstart.ui.base.BaseNavigator

interface ReviewNavigator : BaseNavigator{

    fun moveToScreen(screen: ReviewViewModel.ViewScreen)

    fun openWriteReview(reservationId: Int)

    fun show404Page()
}