package com.rentall.radicalstart.ui.home

import com.rentall.radicalstart.ui.explore.ExploreFragment
import com.rentall.radicalstart.ui.explore.filter.FilterFragment
import com.rentall.radicalstart.ui.explore.guest.GuestFragment
import com.rentall.radicalstart.ui.explore.map.ListingMapFragment
import com.rentall.radicalstart.ui.explore.search.SearchLocationFragment
import com.rentall.radicalstart.ui.inbox.InboxFragment
import com.rentall.radicalstart.ui.profile.ProfileFragment
import com.rentall.radicalstart.ui.saved.SavedBotomSheet
import com.rentall.radicalstart.ui.saved.SavedDetailFragment
import com.rentall.radicalstart.ui.saved.SavedFragment
import com.rentall.radicalstart.ui.trips.TripsFragment
import com.rentall.radicalstart.ui.trips.TripsListFragment
import com.rentall.radicalstart.ui.trips.contactus.ContactSupport
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class HomeFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideProfileFragmentFactory(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun provideExploreFragmentFactory(): ExploreFragment

    @ContributesAndroidInjector
    abstract fun provideSearchFragmentFactory(): SearchLocationFragment

    @ContributesAndroidInjector
    abstract fun provideFilterFragmentFactory(): FilterFragment

    @ContributesAndroidInjector
    abstract fun provideGuestFragmentFactory(): GuestFragment

    @ContributesAndroidInjector
    abstract fun provideListingMapFragmentFactory(): ListingMapFragment

    @ContributesAndroidInjector
    abstract fun provideTripsFragmentFactory(): TripsFragment

    @ContributesAndroidInjector
    abstract fun provideTripsListFragmentFactory(): TripsListFragment

    @ContributesAndroidInjector
    abstract fun provideInboxFragmentFactory(): InboxFragment

    @ContributesAndroidInjector
    abstract fun provideSavedFragmentFactory(): SavedFragment

    @ContributesAndroidInjector
    abstract fun provideSavedDetailFragmentFactory(): SavedDetailFragment

    @ContributesAndroidInjector
    abstract fun provideSavedBotomSheetFragmentFactory(): SavedBotomSheet

    @ContributesAndroidInjector
    abstract fun provideContactSupportFragmentFactory(): ContactSupport

}