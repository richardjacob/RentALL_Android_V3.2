package com.rentall.radicalstart.ui.profile.edit_profile

import com.rentall.radicalstart.ui.profile.currency.CurrencyDialog
import com.rentall.radicalstart.ui.profile.languages.LanguagesDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class EditProfileFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideEditProfileFragmentFactory(): EditProfileFragment

    @ContributesAndroidInjector
    abstract fun provideLanguagesDialogFactory(): LanguagesDialog

    @ContributesAndroidInjector
    abstract fun provideCurrencyDialogFactory(): CurrencyDialog

}