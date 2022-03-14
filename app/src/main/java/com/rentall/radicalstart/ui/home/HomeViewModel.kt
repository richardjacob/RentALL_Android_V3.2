package com.rentall.radicalstart.ui.home

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.GetDefaultSettingQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.resource.ResourceProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HomeViewModel @Inject constructor(
        dataManager: DataManager,
        val resourceProvider: ResourceProvider
): BaseViewModel<HomeNavigator>(dataManager,resourceProvider) {

    val notification = MutableLiveData<Boolean>()
    var pref: SharedPreferences = dataManager.getPref()

    var loginStatus = 3

    init{
        loginStatus = dataManager.currentUserLoggedInMode
    }

    fun validateData() {
        if (dataManager.currencyBase == null || dataManager.currencyRates == null) {
            defaultSetting()
        } else {
            setIsLoading(false)
            navigator.initialAdapter()
        }
    }

    fun clearHttpCache() {
        dataManager.clearHttpCache()
    }

    fun disposeObservable() {
        compositeDisposable.clear()
    }

    fun setNotification(notify: Boolean) {
        if (notification.value != notify) {
            notification.value = notify
        }
    }

    fun defaultSetting() {
        val request = GetDefaultSettingQuery
                .builder()
                .build()

        compositeDisposable.add(dataManager.doGetDefaultSettingApiCall(request)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            try {
                                val response = it.data()!!
                                if (response.searchSettings?.status() == 200 &&
                                        response.Currency()?.status() == 200 &&
                                        response.siteSettings()?.status() == 200) {
                                    if (setSiteName(response.siteSettings()) && setCurrency(response.Currency())) {
                                        navigator.initialAdapter()
                                    }
                                    else {
                                        navigator.showError()
                                    }
                                } else {
                                    (navigator as BaseNavigator).showError()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                (navigator as BaseNavigator).showError()
                            }
                        },
                        {
                            handleException(it)
                        }
                ))
    }

    private fun setSiteName(it: GetDefaultSettingQuery.SiteSettings?): Boolean {
        try {
            if (it!!.results()!![0].name() == "siteName") {
                dataManager.siteName = it.results()!![0].value()
                return true
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            return false
        }
        return false
    }

    private fun setCurrency (it: GetDefaultSettingQuery.Currency?): Boolean {
        try {
            if (it?.status() == 200) {
                if (dataManager.currentUserCurrency == null) {
                    dataManager.currentUserCurrency = it.result()!!.base()!!
                }
                dataManager.currencyBase = it.result()!!.base()
                dataManager.currencyRates = it.result()!!.rates()
                return true
            }else if(it!!.status() == 500) {
                (navigator as BaseNavigator).openSessionExpire()
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            return false
        }
        return false
    }
}