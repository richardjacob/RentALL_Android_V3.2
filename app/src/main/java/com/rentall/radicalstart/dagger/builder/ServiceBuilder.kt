package com.rentall.radicalstart.dagger.builder

import com.rentall.radicalstart.firebase.MyFirebaseMessagingService
import com.rentall.radicalstart.firebase.NotificationIntentService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ServiceBuilder {

    @ContributesAndroidInjector
    abstract fun bindFirebaseService(): MyFirebaseMessagingService

    @ContributesAndroidInjector
    abstract fun bindFirebaseService12(): NotificationIntentService
}