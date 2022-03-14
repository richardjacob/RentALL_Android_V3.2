package com.rentall.radicalstart.ui.profile.currency

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.GetCurrenciesListQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Event
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.Outcome
import javax.inject.Inject

@SuppressLint("LogNotTimber")
class CurrencyVIewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val postsOutcome = MutableLiveData<Event<Outcome<List<GetCurrenciesListQuery.Result>>>>()
    val preSelectedLanguages = MutableLiveData<String>()

    init {
        getCurrency()
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCurrencyList(query)
                .doOnSubscribe { postsOutcome.postValue(Event(Outcome.loading(true))) }
                .doFinally { postsOutcome.postValue(Event(Outcome.loading(false))) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.currencies
                        if (data?.status() == 200) {
                            postsOutcome.value = Event(Outcome.success(data.results()!!))
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            postsOutcome.value = Event(Outcome.error(Throwable()))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        postsOutcome.value = Event(Outcome.error(Throwable()))
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                    }
                }, {
                    postsOutcome.value = Event(Outcome.error(Throwable()))
                    handleException(it, true)
                } )
        )
    }

    fun updateCurrency(it: String) {
       // dataManager.currentUserCurrency = it
    }

}