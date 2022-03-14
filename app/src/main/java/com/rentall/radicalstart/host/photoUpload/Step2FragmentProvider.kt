package com.rentall.radicalstart.host.photoUpload

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class Step2FragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideAddListTitleFragment() : AddListTitleFragment

    @ContributesAndroidInjector
    abstract fun provideAddListDescFragment() : AddListDescFragment
}