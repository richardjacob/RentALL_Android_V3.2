package com.rentall.radicalstart.ui.splash

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.messaging.RemoteMessage
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivitySplashBinding
import com.rentall.radicalstart.ui.auth.AuthActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.host.HostFinalActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.util.LocaleHelper
import com.rentall.radicalstart.vo.InboxMsgInitData
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SplashActivity: BaseActivity<ActivitySplashBinding, SplashViewModel>(), SplashNavigator {

    companion object {
        @JvmStatic
        fun openActivity(activity: Activity) {
            val intent = Intent(activity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashViewModel
        get() = ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.langauge = preferences.getString("Locale.Helper.Selected.Language", "en").toString()
        viewModel.intent = intent
        viewModel.defaultSettingsInCache()
//        window.decorView.layoutDirection = resources.configuration.layoutDirection
//        Log.d("layoutDirection", "onCreate: ${resources.configuration.layoutDirection}");
        viewModel.navigator = this

    }

    fun cancelAlaram()
    {
        val mStartActivity = Intent(this, SplashActivity::class.java)

        val mPendingIntentId = 123456
        val mPendingIntent: PendingIntent = PendingIntent.getActivity(this,
                mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
        val mgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.cancel(mPendingIntent)

    }
    override fun openLoginActivity() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    override fun openMainActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun openHostActivity() {
        startActivity(Intent(this, HostHomeActivity::class.java))
        finish()
    }

    override fun openInboxActivity() {
        checkFirebaseNotificationIntent()
    }
    override fun onRetry() {

    }

    fun checkFirebaseNotificationIntent ()
    {
/*        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                Timber.d( "getExtra :: "  +key + " : " + if (bundle[key] != null) bundle[key] else "NULL")
            }
        }*/
        if (intent.hasExtra("content")) {
            Timber.d("getExtra ${intent.extras?.get("content").toString()}")
            startActivity ( validateResponse(intent.getStringExtra("content")) )
        } else {
            Timber.d("getExtra NO firebase")
        }
    }

    private fun openGuestTrips(remoteMessage: String): Intent? {
        val content = JSONObject(remoteMessage)
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

    private fun validateResponse(remoteMessage: String?): Intent? {

            val content = JSONObject(remoteMessage)
            val screen = content.getString("screenType")
            screen?.let { it ->
                when (it) {
                    "message" -> {
                        return parseInboxData(remoteMessage!!)
                    }
                    "trips" -> {
                        return openGuestTrips(remoteMessage!!)
                    }
                    "becomeahost" -> {
                        return parseBecomeHostData(remoteMessage!!)
                    }
                    else -> {
                        return Intent()
                    }
                }

            }

        return Intent()
    }

    private fun parseBecomeHostData(remoteMessage: String): Intent? {
        Timber.d("PUSHNOTI becameahost")
        try {
            val content = JSONObject(remoteMessage)
            val listid = content.optString("listId")
            Timber.d("PUSHNOTI becameahost $listid")
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

    private fun parseInboxData(remoteMessage: String): Intent? {
        try {
            val content = JSONObject(remoteMessage)
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

}