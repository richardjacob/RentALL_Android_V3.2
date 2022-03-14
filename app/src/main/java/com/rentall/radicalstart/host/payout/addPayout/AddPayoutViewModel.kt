package com.rentall.radicalstart.host.payout.addPayout

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import timber.log.Timber
import javax.inject.Inject
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.*
import org.json.JSONObject

class AddPayoutViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<AddPayoutNavigator>(dataManager,resourceProvider) {

    val firstName = ObservableField("")
    val accountType = ObservableField(resourceProvider.getString(R.string.individual))
    val lastNameVisible = MutableLiveData<Boolean>()
    val lastName = ObservableField("")
    val account = ObservableField("")
    val cnfAccount = ObservableField("")
    val ssn = ObservableField("")
    val routingNo = ObservableField("")
    val country = ObservableField("")
    val countryCode = ObservableField("")
    val address1 = ObservableField("")
    val address2 = ObservableField("")
    val city = ObservableField("")
    val state = ObservableField("")
    val zip = ObservableField("")
    val listPref=ObservableField("")
    val email = ObservableField("")
    val currency = ObservableField("")
    var isDOB = false
    var connectingURL: String = ""
    var accountID: String = ""
    var onRetryCalled : String = ""
    private  var personToken: String? = null
    private lateinit var accountToken: String
    lateinit var stripe: Stripe
    val europeCountries = listOf("AT", "BE", "BG", "CY", "CZ", "DK", "EE",
            "FI", "FR", "DE", "GR", "HU", "IE", "IT",
            "LV", "LT", "LU", "MT", "NL", "NO", "PL",
            "PT", "RO", "SK", "SI", "ES", "SE", "CH")
    lateinit var paymentMethods : MutableLiveData<List<GetPaymentMethodsQuery.Result>>

    init {
        isDOB = dataManager.isDOB!!
        listPref.set(resourceProvider.getString(R.string.individual))
    }

    fun loadPayoutMethods() : MutableLiveData<List<GetPaymentMethodsQuery.Result>> {
        if (!::paymentMethods.isInitialized) {
            paymentMethods = MutableLiveData()
            getPayoutMethods()
        }
        return paymentMethods
    }

    fun checkPaypalInfo(): Boolean {
        if (email.get()!!.trim().isNotBlank()) {
            if (Utils.isValidEmail(email.get()!!)) {
                if (currency.get()!!.isNotBlank()) {
                    return true
                } else navigator.showToast(resourceProvider.getString(R.string.please_select_currency))
            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_valid_email_address))
        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_email_address))
        return false
    }

    fun checkAccountInfo(): Boolean {
        if (address1.get()!!.trim().isNotBlank()) {
           // if (address2.get()!!.isNotBlank()) {
                if (city.get()!!.trim().isNotBlank()) {
                    if (state.get()!!.trim().isNotBlank()) {
                        if (zip.get()!!.isNotBlank()) {
                            return true
                        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_zip_code))
                    } else navigator.showToast(resourceProvider.getString(R.string.please_enter_the_state))
                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_city))
         //   } else navigator.showToast("Please enter Address 2")
        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_address_1))
        return false
    }

    fun checkAccountDetails(): Boolean {
        if(accountType.get()=="Individual"){
            if(!accountType.get()!!.trim().equals(resourceProvider.getString(R.string.account_type))) {
                if (firstName.get()!!.trim().isNotBlank()) {
                    if (lastName.get()!!.trim().isNotBlank()) {
                        if (routingNo.get()!!.trim().isNotBlank() || (europeCountries.contains(countryCode.get()) || countryCode.get() == "MX" || countryCode.get() == "NZ" ) ) {
                            if (account.get()!!.trim().isNotBlank()) {
                                if (cnfAccount.get()!!.trim().isNotBlank()) {
                                    if (account.get()!! == cnfAccount.get()!!) {
                                        return true
//                                if (ssn.get()!!.trim().isNotBlank()) {
//                                    return true
//                                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_ssn))
                                    } else navigator.showToast(resourceProvider.getString(R.string.please_enter_correct_confirm_account_number, checkCountryAndReturn()))
                                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_confirm_account_number, checkCountryAndReturn()))
                            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_account_number, checkCountryAndReturn()))
                        } else navigator.showToast((if (countryCode.get() == "UK" || countryCode.get() == "GB") resourceProvider.getString(R.string.please_enter_sort_number) else resourceProvider.getString(R.string.please_enter_routing_number)))
                    } else navigator.showToast(resourceProvider.getString(R.string.please_enter_last_name))
                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_first_name))
            } else navigator.showToast(resourceProvider.getString(R.string.account_type_error))
            return false
        }else{
            if(!accountType.get()!!.trim().equals(resourceProvider.getString(R.string.account_type))) {
                if (firstName.get()!!.trim().isNotBlank()) {
                    if (routingNo.get()!!.trim().isNotBlank() || (europeCountries.contains(countryCode.get()) || countryCode.get() == "MX" || countryCode.get() == "NZ" ) ) {
                        if (account.get()!!.trim().isNotBlank()) {
                            if (cnfAccount.get()!!.trim().isNotBlank()) {
                                if (account.get()!! == cnfAccount.get()!!) {
                                    return true
//                                if (ssn.get()!!.trim().isNotBlank()) {
//                                    return true
//                                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_ssn))
                                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_correct_confirm_account_number, checkCountryAndReturn()))
                            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_confirm_account_number, checkCountryAndReturn()))
                        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_account_number, checkCountryAndReturn()))
                    } else navigator.showToast(if (countryCode.get() == "UK" || countryCode.get() == "GB") resourceProvider.getString(R.string.please_enter_sort_number) else resourceProvider.getString(R.string.please_enter_routing_number) )
                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_first_name))
            } else navigator.showToast(resourceProvider.getString(R.string.account_type_error))
            return false
        }
    }
    fun getResultAccountToken(type: Int, accountHashMap: HashMap<String, String>) {
      //  stripe = Stripe(this, Constants.stripePublishableKey)
        setIsLoading(true)
        val payoutEmail: String = if(email.get()!!.isEmpty()){
            dataManager.currentUserEmail.toString()
        }else{
            email.get()!!
        }
        val accountParams : AccountParams = if (accountHashMap["accountHolderType"] == "individual") {
            AccountParams.create(true,
                    AccountParams.BusinessTypeParams.Individual(
                            firstName = accountHashMap["firstName"],
                            lastName = accountHashMap["lastName"],
                            email = payoutEmail,
                            address = Address.fromJson(JSONObject("""{"city": "${accountHashMap["city"]}","country": "${accountHashMap["country"]}","line1": "${accountHashMap["line1"]}","line2": "${accountHashMap["line2"]}","postal_code": "${accountHashMap["postal_code"]}","state": "${accountHashMap["state"]}"}"""))
                    ))
        }else{
            AccountParams.create(true,
                    AccountParams.BusinessTypeParams.Company(
                            name = accountHashMap["firstName"],
                            address = Address.fromJson(JSONObject("""{"city": "${accountHashMap["city"]}","country": "${accountHashMap["country"]}","line1": "${accountHashMap["line1"]}","line2": "${accountHashMap["line2"]}","postal_code": "${accountHashMap["postal_code"]}","state": "${accountHashMap["state"]}"}"""))
                    ))
        }

        stripe.createAccountToken(accountParams, callback = object : ApiResultCallback<Token> {
            override fun onError(e: Exception) {
                Timber.e(e, e.message)
                navigator.showToast(e.message ?: "Token Error")

                setIsLoading(false)
            }

            override fun onSuccess(result: Token) {
                Log.d("ResultFromActivity", result.toString())
               // channelResult.success(result.id)
                accountToken = result.id

                val accType: String = if(accountType.get().equals(resourceProvider.getString(R.string.individual))){
                    "individual"
                }else{
                    "company"
                }
                if (accType == "company") {



                    val personAcc = PersonTokenParams.Builder()
                            .setAddress(Address.fromJson(JSONObject("""{"city": "${accountHashMap["city"]}","country": "${accountHashMap["country"]}","line1": "${accountHashMap["line1"]}","line2": "${accountHashMap["line2"]}","postal_code": "${accountHashMap["postal_code"]}","state": "${accountHashMap["state"]}"}""")))
                            .setEmail(payoutEmail)
                            .setRelationship(PersonTokenParams.Relationship.Builder().setRepresentative(true).build())
                            .setFirstName(accountHashMap["firstName"])
                            .setLastName(accountHashMap["lastName"])
                            .build()

                    stripe.createPersonToken(personAcc, callback = object : ApiResultCallback<Token> {
                        override fun onError(e: Exception) {
                            Timber.e(e, e.message)
                            navigator.showToast(e.message ?: "Token Error")
                            setIsLoading(false)
                        }

                        override fun onSuccess(result: Token) {
                            Log.d("ResultFromActivity", result.toString())
                            // channelResult.success(result.id)
                            personToken = result.id
                            addPayout(type)
                            setIsLoading(false)
                        }
                    })
                }
                else  {
                    personToken = null
                    addPayout(type)
                    setIsLoading(false)
                }
            }
        })

    }

    private fun checkCountryAndReturn(): String {
        return if (europeCountries.contains(countryCode.get())) resourceProvider.getString(R.string.iban_number)
       // else if (countryCode.get() == "UK" || countryCode.get() == "GB") resourceProvider.getString(R.string.sort)
        else resourceProvider.getString(R.string.account_number)
    }
    fun addPayout(type: Int) {


      //  Timber.d("SRTIPE $accountToken :: $personToken")
        var accType: String
        if(accountType.get().equals(resourceProvider.getString(R.string.individual),ignoreCase = true )){
            accType = "individual"
        }else{
            accType = "company"
        }

        var payoutEmail: String
        if(email.get()!!.isEmpty()){
            payoutEmail = dataManager.currentUserEmail.toString()
        }else{
            payoutEmail = email.get()!!
        }

        var mutate : AddPayoutMutation
        if (europeCountries.contains(countryCode.get()).not() && (countryCode.get() != "MX" || countryCode.get() != "NZ" )) {
            mutate = AddPayoutMutation
                    .builder()
                    .methodId(type)
                    .payEmail(payoutEmail)
                    .address1(address1.get()!!)
                    .address2(address2.get()!!)
                    .city(city.get()!!)
                    .country(countryCode.get()!!)
                    .state(state.get()!!)
                    .zipcode(zip.get()!!)
                    .currency(currency.get() ?: "USD")
                    .firstname(firstName.get()!!)
                    .lastname(lastName.get()!!)
                    .accountNumber(account.get()!!)
                    .routingNumber(routingNo.get()!!)
                    .businessType(accType)
                    .accountToken(if (::accountToken.isInitialized) accountToken else null)
                    .personToken(personToken)
                    .build()
        }
        else
        {

            mutate = AddPayoutMutation
                    .builder()
                    .methodId(type)
                    .payEmail(payoutEmail)
                    .address1(address1.get()!!)
                    .address2(address2.get()!!)
                    .city(city.get()!!)
                    .country(countryCode.get()!!)
                    .state(state.get()!!)
                    .zipcode(zip.get()!!)
                    .currency(currency.get() ?: "USD")
                    .firstname(firstName.get()!!)
                    .lastname(lastName.get()!!)
                    .accountNumber(account.get()!!)
                     .businessType(accType)
                    .accountToken(accountToken)
                    .personToken(personToken)
                    .build()
        }
        compositeDisposable.add(dataManager.addPayout(mutate)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.addPayout()
                        if (data?.status() == 200) {
                            if(type==1){
                                navigator.moveToScreen(AddPayoutActivity.Screen.FINISH)
                            }else{
                                connectingURL = data?.connectUrl()!!
                                accountID = data?.stripeAccountId()!!
                                Log.d("AccountID",accountID)
                                navigator.moveToScreen(AddPayoutActivity.Screen.WEBVIEW)
                            }
                        } else if (data?.status() == 400) {
                            navigator.showToast(data.errorMessage()!!)
                        } else if (data?.status() == 500) {
                            navigator.openSessionExpire()
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

     fun buildTokenHashMap(): HashMap<String, String> {
        val hashMap = hashMapOf<String,String>()
        hashMap["firstName"] = firstName.get().toString()
        hashMap["lastName"] = lastName.get().toString()
        hashMap["country"] = countryCode.get().toString()
        hashMap["currency"] = currency.get().toString()
        hashMap["accountNumber"] = account.get().toString()
        hashMap["accountHolderType"] = accountType.get().toString()
        hashMap["routingNumber"] = routingNo.get().toString()
        hashMap["line1"] = address1.get().toString()
        hashMap["line2"] = address2.get().toString()
        hashMap["city"] =city.get().toString()
        hashMap["state"] = state.get().toString()
        hashMap["postal_code"] = zip.get().toString()

        return hashMap

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
                            navigator.moveToScreen(AddPayoutActivity.Screen.FINISH)
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

    fun getPayoutMethods() {
        val query = GetPaymentMethodsQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getPayoutsMethod(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data()!!.paymentMethods
                        if (data!!.status() == 200) {
                            paymentMethods.value = data.results()

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

    fun onRoutingTextChanged(s: CharSequence,start: Int, before: Int, count: Int){
        if(s.length>5){
                 Timber.d("greater than 5")
        }else{
           Timber.d("lesser than 5")
        }
    }
}