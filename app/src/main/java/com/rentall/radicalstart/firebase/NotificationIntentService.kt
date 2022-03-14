package com.rentall.radicalstart.firebase

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rentall.radicalstart.SendMessageMutation
import com.rentall.radicalstart.data.DataManager
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class NotificationIntentService: IntentService("NotificationService") {

    @Inject
    lateinit var mDataManager: DataManager

    val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            if (MyFirebaseMessagingService.REPLY_ACTION == intent.action) {
                // do whatever you want with the message. Send to the server or add to the db.
                // for this tutorial, we'll just show it in a toast;
                val message = MyFirebaseMessagingService.getReplyMessage(intent).toString()
                val messageId = intent.getStringExtra(KEY_MESSAGE_ID)
                val threadId = intent.getIntExtra(KEY_THREAD_ID, 0)
                val notifyId = intent.getIntExtra(KEY_NOTIFICATION_ID, 55)
                sendMsg(message, threadId, notifyId)
            }
        }
    }

    private fun updateNotification(notifyId: Int) {
        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.cancel(notifyId)
    }

    private fun sendMsg(msg: String, threadId: Int, notifyId: Int) {
        val mutate = SendMessageMutation
                .builder()
                .threadId(threadId)
                .content(msg)
                .type("message")
                .build()
        compositeDisposable.add(mDataManager.sendMessage(mutate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateNotification(notifyId)
                }, {
                    updateNotification(notifyId)
                }))
    }

    override fun onDestroy() {
       // compositeDisposable.clear()
        super.onDestroy()
    }

    companion object {
        private val KEY_NOTIFICATION_ID = "key_noticiation_id"
        private val KEY_MESSAGE_ID = "key_message_id"
        private val KEY_THREAD_ID = "key_thread_id"

        fun getReplyMessageIntent(context: Context, notificationId: Int, messageId: String, threadId: Int): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = MyFirebaseMessagingService.REPLY_ACTION
            intent.putExtra(KEY_NOTIFICATION_ID, notificationId)
            intent.putExtra(KEY_MESSAGE_ID, messageId)
            intent.putExtra(KEY_THREAD_ID, threadId)
            return intent
        }
    }

}