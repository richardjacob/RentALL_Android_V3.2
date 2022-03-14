package com.rentall.radicalstart.ui.payment

import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import com.apollographql.apollo.exception.ApolloNetworkException
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.BillingDetails
import com.rentall.radicalstart.vo.Outcome
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import javax.inject.Inject

class PaymentViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<PaymentNavigator>(dataManager,resourceProvider) {

    val stripeCard = MutableLiveData<Card?>()
    val billingDetails = MutableLiveData<BillingDetails>()
    val msg = MutableLiveData<String>()
    val token = MutableLiveData<String>()
    var selectedPaymentType=0
    var selectedCurrency=ObservableField("")
    val paymentIntentSecret = MutableLiveData<String>()
    val stripeReqAdditionAction = MutableLiveData<String>()
    val paymentIntentLiveData= MutableLiveData<String>()
    val reservationId = MutableLiveData<Int>()
    var currencies : MutableLiveData<List<GetCurrenciesListQuery.Result>> = MutableLiveData()
    val stripeResponse: LiveData<Outcome<Token>>? = switchMap(stripeCard) {
        it?.let { it1 -> dataManager.createToken(it1) }
    }


    init {
        selectedCurrency.set(resourceProvider.getString(R.string.currency))
    }


    fun initData(intent: Intent?) {
        intent?.let {
            billingDetails.value = intent.getParcelableExtra("billingDetails")
            msg.value = intent.getStringExtra("msg")
        }
    }

    fun validateToken() {
        token.value?.let {
            if (it.isNotEmpty()) {
                createReservation(it)
            }
        } ?: navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
    }

    fun createReservation(stripeToken: String) {
        try {
            val query = CreateReservationMutation.builder()
                    .cardToken(stripeToken)
                    .basePrice(billingDetails.value!!.basePrice)
                    .bookingType(billingDetails.value!!.bookingType)
                    .checkIn(billingDetails.value!!.checkIn)
                    .checkOut(billingDetails.value!!.checkOut)
                    .cleaningPrice(billingDetails.value!!.cleaningPrice)
                    .currency(billingDetails.value!!.currency)
                    .discount(billingDetails.value!!.discount)
                    .discountType(billingDetails.value!!.discountLabel)
                    .guestServiceFee(billingDetails.value!!.guestServiceFee)
                    .guests(billingDetails.value!!.guest)
                    .hostServiceFee(billingDetails.value!!.hostServiceFee)
                    .listId(billingDetails.value!!.listId)
                    .total(billingDetails.value!!.total)
                    .message(msg.value!!)
                    .paymentType(selectedPaymentType)
                    .convCurrency(getUserCurrency())
                    .averagePrice(billingDetails.value!!.averagePrice)
                    .nights(billingDetails.value!!.nights)
                    .specialPricing(billingDetails.value!!.specialPricing)
                    .paymentCurrency(selectedCurrency.get())
                    .build()

            compositeDisposable.add(dataManager.createReservation(query)
                    .doOnSubscribe { setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( {
                        try {
                            token.value = null
                            if (it.data()?.createReservation()?.status() == 200) {
                                if(selectedPaymentType==1){
                                    if(it.data?.createReservation()?.redirectUrl() !=null){
                                        setIsLoading(true)
                                        reservationId.value = it.data()?.createReservation()?.reservationId() ?: 0
                                        navigator.moveToPayPalWebView(it.data?.createReservation()?.redirectUrl() ?: "")
                                    }else{
                                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                    }
                                }else{
                                    navigator.moveToReservation(it!!.data()!!.createReservation()!!.results()!!.id()!!)
                                }
                            } else if(it.data()?.createReservation()?.status() == 500) {
                                navigator.openSessionExpire()
                            }  else {
                                if(it.data()?.createReservation()?.errorMessage()==null){
                                    if(it.data()?.createReservation()?.requireAdditionalAction()==true){
                                        paymentIntentSecret.value=it.data()?.createReservation()?.paymentIntentSecret()!!
                                        reservationId.value=it.data()?.createReservation()?.reservationId()!!

                                        stripeReqAdditionAction.value="1"
                                    }else{
                                        stripeReqAdditionAction.value="0"
                                    }
                                }else{
                                    navigator.showToast(it.data()?.createReservation()?.errorMessage()!!)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace() }
                    }, {
                        if (it is ApolloNetworkException) {
                            navigator.showOffline()
                        } else {
                            navigator.showError()
                            navigator.finishScreen()
                        }
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun  confirmReservation(PaymentIntentId: String){
        val query = ConfirmReservationMutation.builder()
                .reservationId(reservationId.value!!)
                .paymentIntentId(PaymentIntentId)
                .build()
        compositeDisposable.add(dataManager.confirmReservation(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( {
                    try {
                        print(it.data())
                        if(it.data()?.confirmReservation()?.status()==200){
                            navigator.moveToReservation(it!!.data()!!.confirmReservation()!!.results()!!.id()!!)
                        }else if(it.data()?.confirmReservation()?.status()==500){
                            navigator.openSessionExpire()
                        }else{
                            if(it.data()?.confirmReservation()?.requireAdditionalAction()==true){
                                paymentIntentSecret.value=it.data()?.confirmReservation()?.paymentIntentSecret()!!
                                reservationId.value=it.data()?.confirmReservation()?.reservationId()!!
                                stripeReqAdditionAction.value="1"
                            }else{
                                stripeReqAdditionAction.value="0"
                            }
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                },{
                    if (it is ApolloNetworkException) {
                        navigator.showOffline()
                    } else {
                        navigator.showError()
                        navigator.finishScreen()
                    }
                }))
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery
                .builder()
                .build()
        compositeDisposable.add(dataManager.getCurrencyList(query)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe{setIsLoading(true)}
                .doFinally{setIsLoading(false)}
                .subscribe( { response ->
                    val data = response.data()!!.currencies
                    if (data?.status() == 200) {
                        currencies.value = data.results()!!
                    } else if(data?.status() == 500) {
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    fun confirmPayPalPayment(paymentId: String, payerId: String){
      if(paymentId!=""&&payerId!=""){
       val query = ConfirmPayPalExecuteMutation
               .builder()
               .paymentId(paymentId)
               .payerId(payerId)
               .build()

          compositeDisposable.add(dataManager.confirmPayPalPayment(query)
                  .performOnBackOutOnMain(scheduler)
                  .doOnSubscribe{setIsLoading(true)}
                  .doFinally{setIsLoading(false)}
                  .subscribe({response ->
                      val result = response.data?.confirmPayPalExecute()
                      when {
                          result?.status()==200 -> {
                              if(result.reservationId()!=null){
                                  navigator.moveToReservation(result.reservationId()!!)
                              }else{
                                  navigator.showToast(resourceProvider.getString(R.string.reservation_id_not_found))
                              }
                          }
                          result?.status()==400 -> {
                              result.errorMessage().let {
                                  if(it!=null){
                                      navigator.showToast(it)
                                  }else{
                                      navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                  }
                              }
                          }
                          else -> {
                              navigator.showError()
                          }
                      }
                  },{
                      handleException(it)
                  }))

      }else{
        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
      }
    }
}