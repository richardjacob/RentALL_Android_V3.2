package com.rentall.radicalstart.ui.profile.setting

import com.rentall.radicalstart.ui.profile.setting.currency.CurrencyBottomSheet
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class SettingFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideCurrencyBottomSheet() : CurrencyBottomSheet


}