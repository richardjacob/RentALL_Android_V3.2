package com.rentall.radicalstart.ui.splash

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.GetDefaultSettingQuery
import com.rentall.radicalstart.GetPaymentSettingsQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<SplashNavigator>(dataManager, resourceProvider) {

    var langauge = ""
    val isStripeReady = MutableLiveData<Int>().apply { value = 0 }

    lateinit var intent: Intent
//    init {
//        defaultSettingsInCache()
//    }

    fun getPaymentStripeSettings() {
        val request = GetPaymentSettingsQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.clearHttpCache()
                .flatMap { dataManager.dogetPaymentSettingsApiCall(request).toObservable() }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            Constants.stripePublishableKey = it.data()?.paymentSettings?.result()?.publishableKey()
                                    ?: ""
                            isStripeReady.value = 1

                        },
                        {
                            Timber.e(it, "Stripe error ")
                            isStripeReady.value = 2
                        }
                ))
    }

    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.clearHttpCache()
                .flatMap { dataManager.doGetDefaultSettingApiCall(request).toObservable() }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            setCurrency(it.data()?.Currency())
                            setSiteName(it.data()?.siteSettings())
                            setLanguage()
                        },
                        {
                            loginStatus()
                        }
                ))
    }

    private fun setSiteName(it: GetDefaultSettingQuery.SiteSettings?) {
        try {
            if (it?.results()!![0].name() == "siteName") {
                dataManager.siteName = it.results()!![0].value()
            }
            dataManager.listingApproval = it.results()!!.find { it.name() == "listingApproval" }?.value()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCurrency(it: GetDefaultSettingQuery.Currency?) {
        try {
            if (it?.status() == 200) {
                if (dataManager.currentUserCurrency == null) {
                    dataManager.currentUserCurrency = it.result()!!.base()!!
                }
                dataManager.currencyBase = it.result()!!.base()
                dataManager.currencyRates = it.result()!!.rates()
            }
            loginStatus()
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

    private fun setLanguage() {
        try {
            if (dataManager.currentUserLanguage == null) {
                dataManager.currentUserLanguage = langauge

            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

    fun loginStatus() {
        compositeDisposable.add(Observable.just(dataManager.currentUserLoggedInMode)
                .performOnBackOutOnMain(scheduler)
                .subscribe { decideNextActivity(it) }
        )
    }

    private fun decideNextActivity(type: Int) {
        if (intent.hasExtra("content")) {
            navigator.openInboxActivity()
        }
       else if (type == DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.type) {
            navigator.openLoginActivity()
        } else {
            if (dataManager.isHostOrGuest) {
                navigator.openHostActivity()
            } else {
                navigator.openMainActivity()
            }
        }
    }


}
