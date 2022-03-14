package com.rentall.radicalstart.ui.profile.confirmPhonenumber

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class ConfirmPhnoViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<ConfirmPhnoNavigator>(dataManager,resourceProvider) {

    val phoneno = ObservableField("")

    val countryCode = ObservableField("+91")

    val list = MutableLiveData<List<GetCountrycodeQuery.Result>>()
    val listSearch = MutableLiveData<ArrayList<GetCountrycodeQuery.Result>>()
    val isCountryCodeLoad = ObservableField(false)

    val code = ObservableField("")
    val lottieProgress = ObservableField<ConfirmPhnoViewModel.LottieProgress>(ConfirmPhnoViewModel.LottieProgress.NORMAL)
    val isNext = ObservableField<Boolean>(false)

    init {
        getCountryCodes()
    }

    enum class PHScreen {
        COUNTRYCODE,
        FOURDIGITCODE,
        CONFIRMPHONE,
        FINISHED
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    fun onSearchTextChanged(text: CharSequence) {
        if(text.isNotEmpty()){
            val searchText = text.toString().capitalize()
            val containsItem = ArrayList<GetCountrycodeQuery.Result>()
            list.value?.forEachIndexed { _, result ->
                result.countryName()?.let {
                    if(it.contains(searchText)) {
                        containsItem.add(result)
                    }
                }
            }
            listSearch.value = containsItem
        } else {
            list.value?.let {
                listSearch.value = ArrayList(it)
            }
        }
    }

    fun onCodeTextChanged() {
        navigator.hideSnackbar()
    }

    fun onClick(PHScreen: PHScreen) {
        navigator.navigateScreen(PHScreen)
    }

    fun getCountryCodes() {
        val query = GetCountrycodeQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCountryCode(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.countries
                        if (data?.status() == 200) {
                            isCountryCodeLoad.set(true)
                            list.value = response.data()!!.countries!!.results()
                        }else if(data!!.status() == 500) {
                            (navigator as BaseNavigator).openSessionExpire()
                        }
                        else {
                            isCountryCodeLoad.set(false)
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        isCountryCodeLoad.set(false)
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    isCountryCodeLoad.set(false)
                    handleException(it)
                })
        )
    }

    fun addPhnumber() {
        val query = AddPhoneNumberMutation
                .builder()
                .countryCode(countryCode.get().toString())
                .phoneNumber(phoneno.get().toString())
                .build()
        compositeDisposable.add(dataManager.addPhoneNumber(query)
                .doOnSubscribe {
                    isNext.set(true)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
                }
                .doFinally {
                    isNext.set(false)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.AddPhoneNumber()
                        if (data?.status() == 200) {
                                navigator.navigateScreen(ConfirmPhnoViewModel.PHScreen.FOURDIGITCODE)
                        }else if(data?.status() == 400){
                            data.errorMessage()?.let {
                                navigator.showSnackbar(resourceProvider.getString(R.string.phone_number), it)
                            } ?: navigator.showSnackbar(resourceProvider.getString(R.string.phone_number),resourceProvider.getString(R.string.invalid_phone_number))
                        }

                        else {
                            navigator.hideKeyboard()
                            navigator.showError()
                            (navigator as BaseNavigator).openSessionExpire()
                        }
                    } catch (e: Exception) {
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
        )
    }


    fun sendCodeAgain() {
        val query = AddPhoneNumberMutation
                .builder()
                .countryCode(countryCode.get().toString())
                .phoneNumber(phoneno.get().toString())
                .build()
        compositeDisposable.add(dataManager.addPhoneNumber(query)
                .doOnSubscribe {
                    isNext.set(true)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
                }
                .doFinally {
                    isNext.set(false)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.AddPhoneNumber()
                        if (data?.status() == 200) {
                            navigator.showToast(resourceProvider.getString(R.string.we_sent_the_code_to_your_phone_number))
                        }else if(data?.status() == 400){
                            data.errorMessage()?.let {
                                navigator.showSnackbar(resourceProvider.getString(R.string.phone_number), it)
                            } ?: navigator.showSnackbar(resourceProvider.getString(R.string.phone_number),resourceProvider.getString(R.string.invalid_phone_number))
                        }

                        else {
                            navigator.hideKeyboard()
                            navigator.showError()
                            (navigator as BaseNavigator).openSessionExpire()
                        }
                    } catch (e: Exception) {
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
        )
    }



    fun sendVerification() {
        val query = GetEnteredPhoneNoQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getPhoneNumber(query)
                .doOnSubscribe {
                    isNext.set(true)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
                }
                .doFinally {
                    isNext.set(false)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.phoneData
                        if (data?.status() == 200) {
                            dataManager.currentUserPhoneNo = data.countryCode() + data.phoneNumber()
                            dataManager.isPhoneVerified = data.verification()?.isPhoneVerified
                            navigator.showToast(resourceProvider.getString(R.string.we_sent_the_code_to_your_phone_number))
                        }  else if(data!!.status() == 500) {
                            (navigator as BaseNavigator).openSessionExpire()
                        }
                        else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        navigator.showError()
                    }
                }, {
                    handleException(it, true)
                })
        )
    }

    fun verifyCode() {
        try {
            val query = VerifyPhoneNumberMutation
                    .builder()
                    .verificationCode(code.get()!!.toInt())
                    .build()
            compositeDisposable.add(dataManager.verifyPhoneNumber(query)
                    .doOnSubscribe {
                        isNext.set(true)
                        lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
                    }
                    .doFinally {
                        isNext.set(false)
                        lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
                    }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data()!!.VerifyPhoneNumber()
                            if (data?.status() == 200) {
                                dataManager.isPhoneVerified = true
                                navigator.navigateScreen(ConfirmPhnoViewModel.PHScreen.FINISHED)
                            } else if(data!!.status() == 500) {
                                (navigator as BaseNavigator).openSessionExpire()
                            }
                            else {
                                navigator.hideKeyboard()
                                navigator.showSnackbar(resourceProvider.getString(R.string.error), resourceProvider.getString(R.string.the_entered_4_digit_code_is_incorrect))
                            }
                        } catch (e: Exception) {
                            navigator.showError()
                        }
                    }, {
                        handleException(it)
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }
}