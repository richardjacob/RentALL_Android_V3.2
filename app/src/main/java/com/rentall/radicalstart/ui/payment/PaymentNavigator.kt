package com.rentall.radicalstart.ui.payment

import com.rentall.radicalstart.ui.base.BaseNavigator
import org.jetbrains.annotations.Nullable

interface PaymentNavigator: BaseNavigator {

    fun moveToReservation(id: Int)

    fun finishScreen()

    fun moveToPayPalWebView(redirectUrl: String)
}