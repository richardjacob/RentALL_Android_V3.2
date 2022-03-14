package com.rentall.radicalstart.host.payout.editpayout

import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.Payout
import javax.inject.Inject

class PayoutViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<EditPayoutNavigator>(dataManager,resourceProvider) {

    init {
        dataManager.clearHttpCache()
    }

    lateinit var list : MutableLiveData<List<GetCountrycodeQuery.Result>>
    val listSearch =  MutableLiveData<ArrayList<GetCountrycodeQuery.Result>>()
    lateinit var payoutsList :  MutableLiveData<ArrayList<Payout>>

    var connectingURL: String = ""
    var accountID: String = ""

    fun loadCountryCode() : MutableLiveData<List<GetCountrycodeQuery.Result>> {
        if (!::list.isInitialized) {
            list = MutableLiveData()
            getCountryCode()
        }
        return list
    }

    fun loadPayouts() : MutableLiveData<ArrayList<Payout>> {
        if (!::payoutsList.isInitialized) {
            payoutsList = MutableLiveData()
            getPayouts()
        }
        return payoutsList
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

    fun getCountryCode() {
        navigator.disableCountrySearch(false)
        val query = GetCountrycodeQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCountryCode(query)
                //.delay(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.countries
                        if (data?.status() == 200) {
                            navigator.disableCountrySearch(true)
                            list.value = response.data()!!.countries!!.results()
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
        )
    }

    fun getPayouts() {
        val query = GetPayoutsQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getPayouts(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.payouts
                        if (response.data()!!.payouts!!.results() != null) {
                            //payoutsList.value = ArrayList(response.data()!!.payouts!!.results())
                            parseResult(response.data()!!.payouts!!.results()!!)
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
        )
    }


    fun setPayout() {
        val query = ConfirmPayoutMutation
                .builder()
                .currentAccountId(accountID)
                .build()
        compositeDisposable.add(dataManager.setPayout(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.confirmPayout()
                        if (data!!.status() == 200) {
//                            navigator.moveToScreen(EditPayoutActivity.Screen.FINISH)
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
        )
    }



    fun verifyPayout(id:String) {
        val query = VerifyPayoutMutation
                .builder()
                .stripeAccount(id)
                .build()
        compositeDisposable.add(dataManager.confirmPayout(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.verifyPayout()
                        if (response.data()!!.verifyPayout()!!.status() == 200) {
                            connectingURL = data?.connectUrl()!!
                            accountID = data?.stripeAccountId()!!
                            navigator.moveToScreen(EditPayoutActivity.Screen.WEBVIEW)
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
        )
    }

    private fun parseResult(results:  MutableList<GetPayoutsQuery.Result>) {
        val list = ArrayList<Payout>()
        results.forEach {
            val verified= it.isVerified
            if(verified != null){
                list.add(
                        Payout(id = it.id()!!,
                                name = it.paymentMethod()!!.name()!!,
                                method = it.paymentMethod()!!.id()!!,
                                currency = it.currency()!!,
                                isDefault = it.default_()!!,
                                payEmail = it.payEmail(),
                                pinDigit = it.last4Digits(),
                                isVerified = it.isVerified!!)
                )
            }else{
                list.add(
                        Payout(id = it.id()!!,
                                name = it.paymentMethod()!!.name()!!,
                                method = it.paymentMethod()!!.id()!!,
                                currency = it.currency()!!,
                                isDefault = it.default_()!!,
                                payEmail = it.payEmail(),
                                pinDigit = it.last4Digits(),
                                isVerified = false)
                )
            }


        }
        payoutsList.value = list
    }

    private fun removePayout(id: Int) {
        val payoutList = payoutsList.value
        for ( i in 0 until payoutList!!.size) {
            if (payoutList[i].id == id) {
                payoutList.removeAt(i)
                break
            }
        }
        payoutsList.value = payoutList
    }

    private fun setDefaultPayout(id: Int) {
        val payoutList = payoutsList.value
        for ( i in 0 until payoutList!!.size) {
            payoutList[i].isDefault = payoutList[i].id == id
        }
        payoutsList.value = payoutList
    }

    fun setDefaultRemovePayputs(type: String, id: Int) {
        val mutate = SetDefaultPayoutMutation
                .builder()
                .type(type)
                .id(id)
                .build()
        compositeDisposable.add(dataManager.setDefaultPayout(mutate)
//                .doOnSubscribe { setIsLoading(true) }
//                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.setDefaultPayout()
                        if (data?.status() == 200) {
                            if (type == "remove") {
                                removePayout(id)
                            } else {
                                setDefaultPayout(id)
                            }
                        } else {
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it, true)
                })
        )
    }

    fun clearHttp() {
        dataManager.clearHttpCache()
    }


}