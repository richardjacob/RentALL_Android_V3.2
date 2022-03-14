package com.rentall.radicalstart.firebase

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rentall.radicalstart.data.local.prefs.PreferencesHelper
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail.HostNewInboxMsgActivity
import com.rentall.radicalstart.ui.inbox.msg_detail.NewInboxMsgActivity
import com.rentall.radicalstart.vo.InboxMsgInitData
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject
import androidx.core.app.RemoteInput
import com.google.gson.JsonObject
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.ui.auth.AuthActivity
import com.rentall.radicalstart.ui.host.HostFinalActivity
import com.rentall.radicalstart.ui.splash.SplashActivity
import org.json.JSONObject


class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var mPreferencesHelper: PreferencesHelper

    @Inject
    lateinit var dataManager: DataManager

    companion object {
         val REPLY_ACTION = "com.rentall.radicalstart.firebase.REPLY_ACTION"
         val KEY_REPLY = "key_reply_message"

        fun getReplyMessage(intent: Intent): CharSequence? {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            return remoteInput?.getCharSequence(KEY_REPLY)
        }
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        Timber.tag("OnFirebaseNewToken").d("Refreshed token: %s", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage?.let {
            try {
                if (mPreferencesHelper.currentUserLoggedInMode != 0) {
                    Log.d("PUSHNOTI",remoteMessage?.toString())

                    Log.d("PUSHNOTI",remoteMessage.data?.toString())
                    val content = JSONObject(remoteMessage.data?.get("content"))
                    Log.d("PUSHNOTI",content.toString())
                    Log.d("PUSHNOTI",content.getString("message"))
                    val msg = content.getString("message")
                    val title = content.getString("title")
                    val id = content.getString("notificationId")?.toInt()
                    val screen = content.getString("screenType")
                    msg?.let {
                        showNotification(title!!, msg, id!!, remoteMessage, screen!!)
                    }
                }
            } catch (e: Exception) {
                    e.printStackTrace()
            }
        }
    }

    private fun validateResponse(remoteMessage: RemoteMessage?): Intent? {
        if(mPreferencesHelper.currentUserLoggedInMode == 0){
            openLogin()
        }
        else if(!appInForeground(this)){
            return parseToSplash()
        }
        else {
            val content = JSONObject(remoteMessage!!.data?.get("content"))
            val screen = content.getString("screenType")
            screen?.let { it ->
                when (it) {
                    "message" -> {
                        return parseInboxData(remoteMessage)
                    }
                    "trips" -> {
                        return openGuestTrips(remoteMessage)
                    }
                    "becomeahost" -> {
                        return parseBecomeHostData(remoteMessage)
                    }
                    else -> {
                        return Intent()
                    }
                }

            }
        }
        return Intent()
    }

    private fun openLogin():Intent?{
        val intent = Intent(this, AuthActivity::class.java)
        return intent
    }

    fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        val result = runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
        Log.d("appInForeground",result.toString())
        return result
    }

    private fun parseToSplash(): Intent?{
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intent
    }

    private fun parseBecomeHostData(remoteMessage: RemoteMessage): Intent? {
        Timber.d("PUSHNOTI becameahost")
        try {
            val content = JSONObject(remoteMessage!!.data?.get("content"))
            val listid = content.optString("listId")

            if (listid.isNotBlank()) {
                val intent = Intent(this, HostFinalActivity::class.java)
                intent.putExtra("listId", listid)
                intent.putExtra("yesNoString", "Yes")
                intent.putExtra("bathroomCapacity", "0")
                intent.putExtra("country", "")
                intent.putExtra("countryCode","")
                intent.putExtra("street", "")
                intent.putExtra("buildingName", "")
                intent.putExtra("city", "")
                intent.putExtra("state", "")
                intent.putExtra("zipcode", "")
                intent.putExtra("lat","")
                intent.putExtra("lng","")
                intent.putExtra("isDeep",true)
                return intent
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Intent(this, SplashActivity::class.java)
    }

    private fun parseInboxData(remoteMessage: RemoteMessage): Intent? {
        try {
            val content = JSONObject(remoteMessage!!.data?.get("content"))
            val userType = content.getString("userType")
            userType?.let {
                val inboxMsgInitData = InboxMsgInitData(
                        threadId = content.getString("threadId")!!.toInt(),
                        receiverID = content.getString("hostProfileId")!!.toInt(),
                        senderID = content.getString("guestProfileId")!!.toInt(),
                        guestId = content.getString("guestId")!!,
                        guestName = content.getString("guestName")!!,
                        guestPicture = content.getString("guestPicture")!!,
                        hostId = content.getString("hostId")!!,
                        hostName = content.getString("hostName")!!,
                        hostPicture = content.getString("hostPicture")!!,
                        listID = content.getString("listId")!!.toInt()
                )
                return openGuestInboxDetail(inboxMsgInitData, userType)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
       return Intent()
    }

    private fun showNotification(title: String, msg: String, id: Int, remoteMessage: RemoteMessage, screen: String) {
        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(title, msg, id, remoteMessage, screen)
        } else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, "ssa")
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)

            val contentIntent = PendingIntent.getActivity(
                    this,
                    0,
                    validateResponse(remoteMessage), PendingIntent.FLAG_CANCEL_CURRENT)

            notificationBuilder.setContentIntent(contentIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(id, notificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannels(title: String, msg: String, id: Int, remoteMessage: RemoteMessage, screen: String) {

        val CHANNEL_ID = "my_channel_01_$screen"
        val name = screen
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)

        // Create a notification and set the notification channel.
        val notification = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
        notification.setContentTitle(title)
        notification.setContentText(msg)
        notification.setSmallIcon(R.drawable.ic_logo)
        notification.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
        notification.setChannelId(CHANNEL_ID)
        notification.setAutoCancel(true)
        if (screen == "message") {
            // 1. Build label
            val replyLabel = getString(R.string.notif_action_reply)
            val remoteInput = RemoteInput.Builder(KEY_REPLY)
                    .setLabel(replyLabel)
                    .build()

            // 2. Build action
            val replyAction = NotificationCompat.Action.Builder(
                    R.drawable.ic_right_arrow_red, replyLabel, getReplyPendingIntent(remoteMessage))
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build()

            notification.addAction(replyAction)
        }
        val builder = notification.build()
        val contentIntent = PendingIntent.getActivity(
                this,
                0,
                validateResponse(remoteMessage), PendingIntent.FLAG_UPDATE_CURRENT)

        builder.contentIntent = (contentIntent)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.createNotificationChannel(mChannel)
        // Issue the notification.
        mNotificationManager.notify( id, builder)
    }

    private fun openGuestTrips(remoteMessage: RemoteMessage): Intent? {
        val content = JSONObject(remoteMessage.data?.get("content"))
        val userType = content.getString("userType")
        var intent : Intent? =  null
        if (userType == "guest") {
            intent = Intent(this, HomeActivity::class.java)
        } else if (userType == "host") {
            intent = Intent(this, HostHomeActivity::class.java)
        }
        intent?.putExtra("from", "trip")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intent
    }

    private fun openGuestInboxDetail(initData: InboxMsgInitData, userType: String): Intent? {
        var intent : Intent? =  null
        if (userType == "guest") {
            intent = Intent(this, HomeActivity::class.java)
        } else if (userType == "host") {
            intent = Intent(this, HostHomeActivity::class.java)
        }
        intent?.putExtra("inboxInitData", initData)
        intent?.putExtra("from", "fcm")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intent
    }

    private fun getReplyPendingIntent(remoteMessage: RemoteMessage): PendingIntent {
        val content = JSONObject(remoteMessage.data?.get("content"))
        val notificationId = content.getString("notificationId")?.toInt()
        val threadId = content.getString("threadId")!!.toInt()
        val messageId = remoteMessage.messageId!!

        val intent: Intent
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = NotificationIntentService.getReplyMessageIntent(this, notificationId!!, messageId, threadId)
            PendingIntent.getService(applicationContext, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            // start your activity
            PendingIntent.getActivity(this, 100, parseInboxData(remoteMessage), PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
