package com.rentall.radicalstart.ui.host.hostHome

import com.rentall.radicalstart.host.calendar.CalendarAvailabilityFragment
import com.rentall.radicalstart.host.calendar.CalendarListingDialog
import com.rentall.radicalstart.host.calendar.CalendarListingFragment
import com.rentall.radicalstart.host.calendar.CalendarListingFragment1
import com.rentall.radicalstart.ui.host.hostInbox.HostInboxFragment
import com.rentall.radicalstart.ui.host.hostListing.HostListingFragment
import com.rentall.radicalstart.ui.host.hostReservation.HostTripsFragment
import com.rentall.radicalstart.ui.host.hostReservation.HostTripsListFragment
import com.rentall.radicalstart.ui.host.hostReservation.hostContactUs.HostContactSupport
import com.rentall.radicalstart.ui.host.step_two.PhotoUploadFragment
import com.rentall.radicalstart.ui.profile.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class HostHomeFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideProfileFragmentFactory(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun provideHostInboxFragmentFactory(): HostInboxFragment

    @ContributesAndroidInjector
    abstract fun provideCalendarListingFragmentFactory(): CalendarListingFragment

    @ContributesAndroidInjector
    abstract fun provideHostListingFragment(): HostListingFragment

    @ContributesAndroidInjector
    abstract fun provideHostTripsFragment(): HostTripsFragment

    @ContributesAndroidInjector
    abstract fun provideHostTripsListFragment(): HostTripsListFragment


    @ContributesAndroidInjector
    abstract fun provideCalendarAvailabilityFragmentFactory(): CalendarAvailabilityFragment

    @ContributesAndroidInjector
    abstract fun provideCalendarAvailabilityFragmentFactory1(): CalendarListingFragment1

    @ContributesAndroidInjector
    abstract fun provideCalendarListingDialogFactory(): CalendarListingDialog

    @ContributesAndroidInjector
    abstract fun providePhotoUploadFragmentFactory(): PhotoUploadFragment

    @ContributesAndroidInjector
    abstract fun provideHostContactSupportFragmentFactory(): HostContactSupport

}