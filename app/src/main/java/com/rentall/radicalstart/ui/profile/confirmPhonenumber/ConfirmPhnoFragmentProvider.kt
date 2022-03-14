package com.rentall.radicalstart.ui.profile.confirmPhonenumber

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ConfirmPhnoFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideConfirmPhnoFragment(): ConfirmPhnoFragment

    @ContributesAndroidInjector
    abstract fun provideCountryCodeFragment(): CountryCodeFragment

    @ContributesAndroidInjector
    abstract fun provideEnterCodeFragment(): EnterCodeFragment

}