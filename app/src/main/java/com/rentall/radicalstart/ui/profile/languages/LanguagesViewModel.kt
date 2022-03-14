package com.rentall.radicalstart.ui.profile.languages

import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.R
import com.rentall.radicalstart.UserPreferredLanguagesQuery
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Event
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.Outcome
import javax.inject.Inject

class LanguagesViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val postsOutcome = MutableLiveData<Event<Outcome<List<UserPreferredLanguagesQuery.Result>>>>()
    val preSelectedLanguages = MutableLiveData<String>()

    init {
        getLanguages()
    }

    fun getLanguages() {
        val query = UserPreferredLanguagesQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.doGetLanguagesApiCall(query)
                .doOnSubscribe { postsOutcome.postValue(Event(Outcome.loading(true))) }
                .doFinally { postsOutcome.postValue(Event(Outcome.loading(false))) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.userLanguages()
                        if (data?.status() == 200) {
                            postsOutcome.value = Event(Outcome.success(data.result()!!.toList()))
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            postsOutcome.value = Event(Outcome.error(Throwable()))
                            navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
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

}