package com.rentall.radicalstart

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.res.Resources
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rentall.radicalstart.dagger.component.DaggerAppComponent
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.util.LocaleHelper
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.stripe.android.PaymentConfiguration
import dagger.android.*
import net.gotev.uploadservice.UploadService
import timber.log.Timber
import javax.inject.Inject
import com.rentall.radicalstart.util.rx.Scheduler
import io.reactivex.disposables.CompositeDisposable


class RentALLApp : DaggerApplication() {

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    private val applicationInjector = DaggerAppComponent.builder().application(this).build()

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var scheduler: Scheduler


    companion object {

        @JvmStatic
        var res: Resources? = null;

        @JvmStatic
        fun getAppRes(): Resources? {
            return res
        }
    }

    override fun attachBaseContext(base: Context) {
        val context = LocaleHelper.onAttach(base)
        Timber.d("LangCheck attachBaseContext App  " + context.resources.configuration.locale.displayLanguage)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            startDebugCrashActivity()
        } else {
            startCrashActivity()
        }

        getPaymentStripeSettings()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return applicationInjector
    }

    private fun startCrashActivity() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(false) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(false) //default: true
                .trackActivities(true) //default: false
                //.minTimeBetweenCrashesMs(2000) //default: 3000
                .restartActivity(SplashActivity::class.java)
                .apply()
    }

    private fun startDebugCrashActivity() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(false) //default: true
                .trackActivities(true) //default: false
                //.minTimeBetweenCrashesMs(2000) //default: 3000
                .restartActivity(SplashActivity::class.java)
                .apply()
    }

    fun getPaymentStripeSettings()
    {
        val request = GetPaymentSettingsQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.clearHttpCache()
                .flatMap { dataManager.dogetPaymentSettingsApiCall(request).toObservable() }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            Timber.d("STRIPE:: APPL ${it.data()?.paymentSettings?.result()?.publishableKey()}")
                            Constants.stripePublishableKey = it.data()?.paymentSettings?.result()?.publishableKey() ?: ""

                            PaymentConfiguration.init(
                                    applicationContext,
                                    Constants.stripePublishableKey
                            )
                        },
                        {
                            Timber.e(it,"Stripe error ")

                        }
                ))
    }
}
