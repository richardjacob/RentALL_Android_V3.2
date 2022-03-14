package com.rentall.radicalstart.ui.host.step_one

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class StepOneFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideStepOneOptionsFragment(): StepOneOptionsFragment

    @ContributesAndroidInjector
    abstract fun provideStepTwoFragment(): KindOfPlaceFragment

    @ContributesAndroidInjector
    abstract fun provideTypeOfSpaceFragment(): TypeOfSpaceFragment

    @ContributesAndroidInjector
    abstract fun provideNoOfGuestFragment(): NoOfGuestFragment

    @ContributesAndroidInjector
    abstract fun provideNoOfBathroomFragment(): NoOfBathroomFragment

    @ContributesAndroidInjector
    abstract fun provideTypeOfBedsFragment(): TypeOfBedsFragment

    @ContributesAndroidInjector
    abstract fun provideAddressFragment(): AddressFragment

    @ContributesAndroidInjector
    abstract fun provideMaplocationFragment(): MaplocationFragment

    @ContributesAndroidInjector
    abstract fun provideAmenitiesFragment(): AmenitiesFragment

    @ContributesAndroidInjector
    abstract fun provideSafetynPrivacyFragment(): SafetynPrivacyFragment

    @ContributesAndroidInjector
    abstract fun provideGuestSpacesFragment(): GuestSpacesFragment

    @ContributesAndroidInjector
    abstract fun provideSelectCountry(): SelectCountry

}