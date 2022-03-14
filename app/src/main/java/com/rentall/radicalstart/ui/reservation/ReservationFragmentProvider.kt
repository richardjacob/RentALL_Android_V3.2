package com.rentall.radicalstart.ui.reservation

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReservationFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideItineraryFragmentFactory(): ItineraryFragment

    @ContributesAndroidInjector
    abstract fun provideReceiptFragmentFactory(): ReceiptFragment
}