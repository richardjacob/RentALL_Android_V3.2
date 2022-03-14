package com.rentall.radicalstart.dagger.component

import android.app.Application
import com.rentall.radicalstart.RentALLApp
import com.rentall.radicalstart.dagger.builder.ActivityBuilder
import com.rentall.radicalstart.dagger.builder.ServiceBuilder
import com.rentall.radicalstart.dagger.module.AppModule
import com.rentall.radicalstart.data.local.prefs.PreferencesHelper
import com.rentall.radicalstart.firebase.MyFirebaseMessagingService
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuilder::class, ServiceBuilder::class])
interface AppComponent : AndroidInjector<RentALLApp> {

    override fun inject(app: RentALLApp)

    fun getAppPreferencesHelper(): PreferencesHelper

    fun inject(firebase: MyFirebaseMessagingService)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}
