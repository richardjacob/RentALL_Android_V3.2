package com.rentall.radicalstart.ui.base

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.apollographql.apollo.exception.ApolloNetworkException
import com.rentall.radicalstart.GetUnReadCountQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.util.CurrencyUtil
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.vo.CurrencyException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


abstract class BaseViewModel<N>(val dataManager: DataManager,val baseResourceProvider: ResourceProvider) : ViewModel() {

    val isLoading = ObservableBoolean(false)

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var mNavigator: WeakReference<N>? = null

    private lateinit var disposable: Disposable

    var navigator: N
        get() = mNavigator!!.get()!!
        set(navigator) {
            this.mNavigator = WeakReference(navigator)
        }

    fun setIsLoading(isLoading: Boolean) {
        this.isLoading.set(isLoading)
    }

    fun getAccessToken() : String {
        try {
            return dataManager.accessToken!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire()
        }
        return ""
    }

    fun getUserId(): String {
        try {
            return dataManager.currentUserId!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire()
        }
        return ""
    }

    fun getUserCurrency(): String {
        try {
            return dataManager.currentUserCurrency!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire()
        }
        return ""
    }

    fun getCurrencyRates(): String {
        try {
            return dataManager.currencyRates!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire()
        }
        return ""
    }

    fun getCurrencyBase(): String {
        try {
            return dataManager.currencyBase!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire()
        }
        return ""
    }

    fun getConvertedRate(from: String, amount: Double): Double {
        var rate = 0.0
        try {
           rate = CurrencyUtil.getRate(
                        base = getCurrencyBase(),
                        to = getUserCurrency(),
                        from = from,
                        rateStr = getCurrencyRates(),
                        amount = amount
                    )
        } catch (e: CurrencyException) {
            (navigator as BaseNavigator).openSessionExpire()
            e.printStackTrace()
        } catch (e: Exception) {
            (navigator as BaseNavigator).showError()
            e.printStackTrace()
        }
        return rate
    }

    fun getCurrencySymbol(): String {
        return CurrencyUtil.getCurrencySymbol(getUserCurrency())
    }

    open fun handleException(e: Throwable, showToast: Boolean = false) {
        if(e.cause is SocketTimeoutException){
            (navigator as BaseNavigator).showToast(baseResourceProvider.getString(R.string.server_error))
        }else if (e is ApolloNetworkException) {
            if (showToast) {
                (navigator as BaseNavigator).showToast(baseResourceProvider.getString(R.string.currently_offline))
            } else {
                (navigator as BaseNavigator).showOffline()
            }
        } else {
            if (showToast) {
                (navigator as BaseNavigator).showToast(baseResourceProvider.getString(R.string.something_went_wrong_action))
            } else {
                (navigator as BaseNavigator).showError()
            }
        }
    }

    fun getUnreadCountAndBanStatus() {
        val query = GetUnReadCountQuery
                .builder()
                .build()

        disposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
                .filter { dataManager.isUserLoggedIn() }
                .switchMap { dataManager.getUnreadCount(query)
                        .onErrorResumeNext { _: Throwable -> Observable.empty() }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    try {
                        val result = it.data()?.unReadCount
                        if (result!!.status() == 200) {
                            if (result.results()!!.userBanStatus() == 1) {
                                (navigator as BaseNavigator).openSessionExpire()
                            }
                            else {
                                dataManager.haveNotification = result.results()?.guestCount()!! > 0
                            }
                        }else if(result!!.status() == 500) {
                            (navigator as BaseNavigator).openSessionExpire()
                        }
                    } catch (e:  Exception) {
                        e.printStackTrace()
                    }
                }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun clearUnreadCountApi() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }
    }

    fun responseValidation(status: Int, action: () -> Unit) {
        when (status) {
            200 -> action()
            500 -> (navigator as BaseNavigator).openSessionExpire()
            else -> (navigator as BaseNavigator).showError()
        }
    }

    fun checkException() {

    }

    fun catchAll(message: String, action: () -> Unit) {
        try {
            action()
        } catch (t: Throwable) {
            Log.e("Failed to $message. ${t.message}", t.toString())
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    /*fun getSiteName(): String {
        return dataManager.siteName!!
    }*/
}
