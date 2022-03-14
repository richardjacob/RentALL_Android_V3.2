package com.rentall.radicalstart.host.calendar

import com.rentall.radicalstart.ui.base.BaseNavigator

interface CalendarAvailabilityNavigator : BaseNavigator {

    fun moveBackToScreen()

    fun hideCalendar(flag: Boolean)

    fun hideWholeView(flag: Boolean)

    fun closeAvailability(flag: Boolean)
}