package com.rentall.radicalstart.ui.entry

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.local.prefs.AppPreferencesHelper
import com.rentall.radicalstart.databinding.ActivityEntryBinding
import com.rentall.radicalstart.ui.auth.AuthActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.profile.review.ReviewActivity
import com.rentall.radicalstart.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.vo.FromDeeplinks
import com.rentall.radicalstart.vo.ListingInitData
import javax.inject.Inject


class EntryActivity: BaseActivity<ActivityEntryBinding, EntryViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_entry
    override val viewModel: EntryViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(EntryViewModel::class.java)

    override fun onRetry() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let {
            entryPoint(it)
        } ?: finish()
    }

    private fun entryPoint(data: Uri) {
        try {
            data.path?.let {
                //  Log.d("Deeplink", "paths" + it.substringAfter("/").substringBefore("/"))// regexValidation(it.split("/")[1]))
                when(it.substringAfter("/").substringBefore("/")) {
                    "rooms" -> { openList(data) }
                    "password" -> { openAuth(data) }
                    "user" -> {openEmailVerification(data)}
                    "review" -> {openReview(data)}
                    "message" -> { openMessage (data) }
                    else -> { openBrowser(data) }
                }
            }
        } catch (e: Exception) {
            openBrowser(data)
        }
    }

    private fun openMessage(data: Uri) {
        var intent : Intent? =  null
        val userType = viewModel.dataManager.currentUserType
      //  if (userType == "guest") {
        //    intent = Intent(this, HomeActivity::class.java)
      //  } else if (userType == "host") {
            intent = Intent(this, HostHomeActivity::class.java)
       // }
       // intent?.putExtra("inboxInitData", initData)
        intent?.putExtra("from", "trip")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun openBrowser(data: Uri) {
        data.host?.let {
            Utils.openBrowser(it, this)
            finish()
        }
    }

    private fun openList(data: Uri) {
        try {
            data.lastPathSegment?.let {
                val listId = if (Utils.isInt(it)) {
                    it.toInt()
                } else {
                    it.substring(it.lastIndexOf("-") + 1).toInt()
                }
                if (listId != 0) {
                    val pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    val userFirstLogin = pref.getInt(AppPreferencesHelper.PREF_KEY_USER_LOGGED_IN_MODE, 0)
                    if (userFirstLogin != 0) {
                        ListingDetails.openListDetailsActivity(this, ListingInitData(id = listId))
                    } else {
                        val intent = Intent(this, AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    finish()
                } else {
                    openBrowser(data)
                }
            } ?: openBrowser(data)
        } catch (e: Exception) {
            openBrowser(data)
        }
    }

    private fun openAuth(data: Uri) {
        try {
            data.lastPathSegment?.let {
                if(it == "verification") {
                    val token = data.getQueryParameter("token")
                    val email = data.getQueryParameter("email")
                    if (!token.isNullOrEmpty() && !email.isNullOrEmpty()) {
                        AuthActivity.openActivity(this, true, FromDeeplinks.ResetPassword, email, token)
                        finish()
                    } else {
                        openBrowser(data)
                    }
                } else {
                    openBrowser(data)
                }
            } ?: openBrowser(data)
        } catch (e: Exception) {
            openBrowser(data)
        }
    }


    private fun openReview(data: Uri){
        data.lastPathSegment?.let {
            if(Utils.isInt(it)){
                val pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                val userFirstLogin = pref.getInt(AppPreferencesHelper.PREF_KEY_USER_LOGGED_IN_MODE, 0)
                if (userFirstLogin != 0) {
                    ReviewActivity.openActivity(this, true, FromDeeplinks.REVIEW, it.toInt())
                } else {
                    val intent = Intent(this, AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                finish()
            }
        }
    }

    private fun openEmailVerification(data: Uri) {
        try {
            data.lastPathSegment?.let {
                if(it == "verification") {
                    val confirm = data.getQueryParameter("confirm")
                    val email = data.getQueryParameter("email")
                    if (!confirm.isNullOrEmpty() && !email.isNullOrEmpty()) {
                        val pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                        val userFirstLogin = pref.getInt(AppPreferencesHelper.PREF_KEY_USER_LOGGED_IN_MODE, 0)
                        if(userFirstLogin == 0){
                            AuthActivity.openActivity(this, true, FromDeeplinks.EmailVerification, email, confirm)
                            finish()
                        } else{
                            TrustAndVerifyActivity.openFromVerify(this, confirm, email, "deeplink")
                            finish()
                        }
                    } else {
                        openBrowser(data)
                    }
                } else {
                    openBrowser(data)
                }
            } ?: openBrowser(data)
        } catch (e: Exception) {
            openBrowser(data)
        }
    }

}