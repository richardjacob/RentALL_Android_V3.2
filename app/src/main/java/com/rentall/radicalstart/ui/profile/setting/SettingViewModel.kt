package com.rentall.radicalstart.ui.profile.setting

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class SettingViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<SettingsNavigator>(dataManager, resourceProvider){

    var baseCurrency = ObservableField("")

    var appLanguage = ObservableField("")

    lateinit var currencies : MutableLiveData<List<GetCurrenciesListQuery.Result>>
    lateinit var language: MutableLiveData<List<UserPreferredLanguagesQuery.Result>>

  //  val langName = arrayOf("English","Español","Français","Italiano","Português","Arabic","Hebrew-HE","Hebrew-Iw")
  val langName = arrayOf("English","Español","Français","Italiano","Português","العربية","ישראל")
    val langCode = arrayOf("en","es","fr","it","pt","ar","iw")

    fun loadSettingData() : MutableLiveData<List<GetCurrenciesListQuery.Result>> {
        if (!::currencies.isInitialized) {
            currencies = MutableLiveData()
//            language = MutableLiveData()
            baseCurrency.set(dataManager.currentUserCurrency)
//            appLanguage.set(dataManager.currentUserLanguage)
            getCurrency()
        }
        return currencies
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCurrencyList(query)
                .doOnSubscribe { setIsLoading(true)}
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()!!.currencies
                    if (data?.status() == 200) {
                        setLang()
                        currencies.value = data.results()
                    } else if(data?.status() == 500) {
                        navigator.showError()
                    }
                    //getLanguages()
                }, {
                    handleException(it)
                } )
        )
    }

    fun setLang(){
        langCode.forEachIndexed { index, s ->
            if(s.equals(dataManager.currentUserLanguage)){
                appLanguage.set(langName[index])
            }
        }
    }

    fun getLanguages() {
        val query = UserPreferredLanguagesQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.doGetLanguagesApiCall(query)
                .doOnSubscribe {  setIsLoading(true) }
                .doFinally {  setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data()!!.userLanguages()
                        if (data?.status() == 200) {
                            data.result()!!.forEachIndexed { index, result ->

                            }
                            language.value = data.result()
                        } else if(data?.status() == 500) {
                            navigator.openSessionExpire()
                        } else {
                            navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                    }
                }, {
                    handleException(it, true)
                } )
        )
    }

    fun signOut() {
        val buildQuery = LogoutMutation
                .builder()
                .deviceType(Constants.deviceType)
                .deviceId(dataManager.firebaseToken.toString())
                .build()
        compositeDisposable.add(dataManager.doLogoutApiCall(buildQuery)
                //  .delay(5, TimeUnit.SECONDS)
                //  .doOnSubscribe { setIsLoading(true) }
                .doFinally { //setIsLoading(false);
                    afterSignOut()
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe( {
                    afterSignOut()
                    navigator.navigateToSplash()
                }, {
                    afterSignOut()
                    navigator.navigateToSplash()
                })
        )
    }

    fun updateProfile(fieldName: String, fieldValue: String) {
        try {
            val mutate = EditProfileMutation
                    .builder()
                    .fieldName(fieldName)
                    .fieldValue(fieldValue)
                    .deviceId(dataManager.firebaseToken!!)
                    .userId(dataManager.currentUserId!!)
                    .deviceType(Constants.deviceType)
                    .build()

            compositeDisposable.add(dataManager.doEditProfileApiCall(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data()!!.userUpdate()
                            if (data?.status() == 200) {
                                setReturnData(fieldValue)
                            } else if(data?.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                navigator.showToast(resourceProvider.getString(R.string.Error_msg, fieldName.capitalize()))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        navigator.showToast(resourceProvider.getString(R.string.Error_msg, fieldName.capitalize()))
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }

    }

    private fun setReturnData(fieldValue: String) {
        try {
            dataManager.currentUserCurrency = fieldValue
            navigator.openSplashScreen()

        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun updateCurrency(it: String) {
        baseCurrency.set(it)
        dataManager.currentUserCurrency = it
        navigator.openSplashScreen()
        //updateProfile("preferredCurrency",it)
    }

    fun updateLangauge(it: String,label: String){
        appLanguage.set(label)
        dataManager.currentUserLanguage = it
        navigator.setLocale(it)
    }

    private fun afterSignOut() {
        dataManager.setUserAsLoggedOut()
    }
}