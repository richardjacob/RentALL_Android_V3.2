package com.rentall.radicalstart.ui.profile.feedback

import androidx.databinding.ObservableField
import com.rentall.radicalstart.R
import com.rentall.radicalstart.SendUserFeedbackMutation
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.base.BaseViewModel
import com.rentall.radicalstart.util.performOnBackOutOnMain
import com.rentall.radicalstart.util.resource.ResourceProvider
import com.rentall.radicalstart.util.rx.Scheduler
import javax.inject.Inject

class FeedbackViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<FeedbackNavigator>(dataManager, resourceProvider) {

    val msg = ObservableField("")
    val feedbackType = ObservableField("")


    fun sendFeedback(typeOfFeedback : String, msg : String){
        try {
            val mutate = SendUserFeedbackMutation
                    .builder()
                    .type(typeOfFeedback)
                    .message(msg)
                    .build()

            compositeDisposable.add(dataManager.sendfeedBack(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe({ response ->
                        try {
                            val data = response.data()!!.userFeedback()
                            if (data?.status() == 200) {
                                if (typeOfFeedback.equals("Feed Back")){
                                    navigator.showToast(resourceProvider.getString(R.string.feedback_sent))
                                }else {
                                    navigator.showToast(resourceProvider.getString(R.string.your_feedback)+" "+ typeOfFeedback + " "+resourceProvider.getString(R.string.has_been_sent))
                                }
                            } else if (data?.status() == 500) {
                                navigator.openSessionExpire()
                            } else {
                                navigator.showError()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        handleException(it)
                    } )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

}