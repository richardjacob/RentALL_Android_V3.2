package com.rentall.radicalstart.ui.host.hostReservation.hostContactUs

import android.os.Bundle
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.exception.ApolloNetworkException
import com.rentall.radicalstart.ContactSupportQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class HostContactUsViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<HostContactUsNavigator>(dataManager,resourceProvider) {

    val msg = ObservableField("")
    val listId = MutableLiveData<Int>()
    val reservationId = MutableLiveData<Int>()

    init {
        isLoading.set(true)
    }

    fun setInitialValuesFromIntent (intent: Bundle?) {
        try {
            listId.value = intent!!.getInt("param1")
            reservationId.value = intent.getInt("param2")
        } catch (e :KotlinNullPointerException) {
            navigator.showError()
        }
    }

    fun sendMessage() {
        val query = ContactSupportQuery
                .builder()
                .listId(listId.value)
                .reservationId(reservationId.value)
                .message(msg.get())
                .userType("host")
                .build()
        compositeDisposable.add(dataManager.contactSupport(query)
                .doOnSubscribe { isLoading.set(false) }
                .doFinally { isLoading.set(true) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    val data = response.data()!!.contactSupport()
                    if (data?.status() == 200) {
                        navigator.showToast(resourceProvider.getString(R.string.your_message_sent_successfully_we_will_contact_you_soon))
                        navigator.closeDialog()
                    } else if(data!!.status() == 500) {
                        (navigator as BaseNavigator).openSessionExpire()
                    }
                    else {
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                } )
        )
    }

    override fun handleException(e: Throwable, showToast: Boolean) {
        if (e is ApolloNetworkException) {
            navigator.showToast(resourceProvider.getString(R.string.currently_offline))
        } else {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun validateMsg() {
        if (listId.value != null
                && reservationId.value != null) {
            if(msg.get()!!.isNotEmpty()) {
                sendMessage()
            } else {
                navigator.showToast(resourceProvider.getString(R.string.please_enter_the_message))
            }
        } else {
            navigator.showError()
        }
    }

}