package com.rentall.radicalstart.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentProfileBinding
import com.rentall.radicalstart.host.payout.editpayout.EditPayoutActivity
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_one.StepOneActivity
import com.rentall.radicalstart.ui.WebViewActivity
import com.rentall.radicalstart.ui.profile.confirmPhonenumber.ConfirmPhnoActivity
import com.rentall.radicalstart.ui.profile.edit_profile.EditProfileActivity
import com.rentall.radicalstart.ui.profile.edit_profile.REQUEST_CODE
import com.rentall.radicalstart.ui.profile.feedback.FeedbackActivity
import com.rentall.radicalstart.ui.profile.review.ReviewActivity
import com.rentall.radicalstart.ui.profile.setting.SettingActivity
import com.rentall.radicalstart.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>(), ProfileNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentProfileBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_profile
    override val viewModel: ProfileViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ProfileViewModel::class.java)

//    val activity : Activity = this!!.getActivity()!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        if (::mViewModelFactory.isInitialized && isAdded && activity != null) {
            subscribeToLiveData()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadProfileDetails().observe(this, Observer {
            it?.let { showProfileDetails() }
        })
    }

    private fun initView() {
        mGoogleSignInClient = GoogleSignIn.getClient(context!!,
                GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
        )
    }

    fun setUI(){
        try {
            mBinding.rvProfile.withModels {
                viewModel.profileDetails.value?.let { profileDetails ->
                    viewholderProfileHeader {
                        id("profileHeader")
                        userName(profileDetails.userName)
                        url(profileDetails.picture)
                        onProfileClick(View.OnClickListener {
                            Utils.clickWithDebounce(it){
                                startActivity(Intent(context, EditProfileActivity::class.java))
                            }
                        })
                    }

                    if (viewModel.dataManager.isHostOrGuest) {
                        viewholderProfileListBold {
                            id("switching1")
                            name(resources.getString(R.string.switch_to_travelling))
                            image(R.drawable.hosting)
                            onClick(View.OnClickListener {
                                viewModel.dataManager.isHostOrGuest = false
                                navigateToSplash()
                            })
                        }
                    }else{
                        if (profileDetails.addedList == false) {
                            viewholderProfileLists {
                                id("switching2")
                                name(resources.getString(R.string.become_a_host))
                                image(R.drawable.listspace)
                                onClick(View.OnClickListener {
                                    val intent = Intent(activity!!, StepOneActivity::class.java)
                                    startActivity(intent)

                                })
                            }
                        }else if (profileDetails.addedList == true){
                            viewholderProfileListBold {
                                id("switching3")
                                name(resources.getString(R.string.switch_to_hosting))
                                image(R.drawable.hosting)
                                onClick(View.OnClickListener {
                                    viewModel.dataManager.isHostOrGuest = true
                                    navigateToSplash()
                                })
                            }
                        }
                    }

                    viewholderDivider {
                        id("divReview")
                    }

                    viewholderProfileLists {
                        id("reviewHolder")
                        name(resources.getString(R.string.reviews))
                        image(R.drawable.ic_star)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it){
                                startActivity(Intent(baseActivity!!,ReviewActivity::class.java))
                            }
                        })
                    }

                    viewholderDivider {
                        id("div1")
                    }

                    viewholderProfileLists {
                        id("setting")
                        name(resources.getString(R.string.setting))
                        image(R.drawable.setting)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it){
                                startActivity(Intent(baseActivity!!,SettingActivity::class.java))
                            }
                        })
                    }

                    viewholderDivider {
                        id("div2")
                    }

                    viewholderProfileLists {
                        id("getHelp")
                        name(resources.getString(R.string.get_help))
                        image(R.drawable.ques)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it){
                                WebViewActivity.openWebViewActivity(context!!, Constants.helpURL, resources.getString(R.string.get_help))
                            }
                        })
                    }

                    viewholderDivider {
                        id("div3")
                    }

                    viewholderProfileLists {
                        id("feedback")
                        name(resources.getString(R.string.give_feedback))
                        image(R.drawable.feedback)
                        onClick(View.OnClickListener {
                            Utils.clickWithDebounce(it){
                                startActivity(Intent(baseActivity!!,FeedbackActivity::class.java))
                            }
                        })
                    }

                    /*viewholderBuildNumber {
                        id("Build Number")
                    }*/

                }
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    private fun getBitmapFromVectorDrawable(@DrawableRes drawableId: Int): Bitmap? {
        var drawable = AppCompatResources.getDrawable(baseActivity!!, drawableId) ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun setUp() {
        try {
            mBinding.rvProfile.withModels {
                viewModel.profileDetails.value?.let { profileDetails ->
                    viewholderUserImage {
                        id("viewHolderUserImage")
                        pic(profileDetails.picture)
                        editVisibility(true)
                        onEditClick(View.OnClickListener {
                            startActivity(Intent(context, EditProfileActivity::class.java))
                        })
                    }
                    viewholderUserName {
                        id("name")
                        name(profileDetails.userName)
                        paddingTop(true)
                    }
                    viewholderUserNormalText {
                        id("memberSince")
                        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                        val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
                        text( resources.getString(R.string.member_since) + " " + Utils.memberSince(profileDetails.createdAt,langType))
                        paddingBottom(true)
                        paddingTop(true)
                    }
                    viewholderDivider {
                        id("viewHolderDivider - 1")
                    }
                    if (viewModel.dataManager.isHostOrGuest) {
                        viewholderHostProfile {
                            id("Switch to travelling")
                            listSpace(resources.getString(R.string.switch_to_travelling))
                            isVisible(false)
                            spaceVisibility(true)
                            swap(View.OnClickListener {
                                viewModel.dataManager.isHostOrGuest = false
                                navigateToSplash()
                            })
                        }
                    } else {
                        if (profileDetails.addedList == false) {
                            viewholderHostProfile {
                                id("list space")
                                listSpace(resources.getString(R.string.become_a_host))
                                isVisible(false)
                                spaceVisibility(true)
                                swap(View.OnClickListener {
                                    val intent = Intent(activity!!, StepOneActivity::class.java)
                                    startActivity(intent)
                                })
                            }
                        } else if (profileDetails.addedList == true) {
                            viewholderHostProfile {
                                id("switch to host")
                                listSpace(resources.getString(R.string.switch_to_hosting))
                                isVisible(false)
                                spaceVisibility(true)
                                swap(View.OnClickListener {
                                    viewModel.dataManager.isHostOrGuest = true
                                    navigateToSplash()
                                })
                            }
                        }
                    }

                    viewholderHostProfile {
                        id("Payout Preference")
                        listSpace(resources.getString(R.string.payout_preference))
                        isVisible(false)
                        spaceVisibility(true)
                        swap(View.OnClickListener {
                            val intent = Intent(activity!!, EditPayoutActivity::class.java)
                            startActivity(intent)
                        })
                    }
                    viewholderDivider {
                        id("viewHolderDivider - listspace")
                    }
                    viewholderUserName {
                        id("header")
                        name(resources.getString(R.string.verified_info))
                        paddingTop(true)
                        paddingBottom(true)
                    }
                    viewholderVerifiedInfo {
                        id("emailVerified")
                        profileDetails.emailVerification?.let {
                            if(it){
                                verifiedText(resources.getString(R.string.email_confirmed))
                            }else {
                                verifiedText(resources.getString(R.string.confirm_email))
                            }
                            isVerified(it)
                            onClicked(View.OnClickListener {
                                if (viewModel.dataManager.isHostOrGuest) {
                                    TrustAndVerifyActivity.openActivity(baseActivity!!, "profile", "email")
                                }else{
                                    TrustAndVerifyActivity.openActivity(baseActivity!!, "profile", "email")
                                }
                            })
                        }
                    }
                   /* viewholderVerifiedInfo {
                        id("idVerified")
                        verifiedText(resources.getString(R.string.document_verification))
                        isVerified(profileDetails.idVerification)
                    }*/
                    viewholderVerifiedInfo {
                        id("fbVerified")
                        profileDetails.fbVerification?.let {
                            if(it) {
                                verifiedText(resources.getString(R.string.facebook_connected))
                            }else{
                                verifiedText(getString(R.string.connect_fb))
                            }
                            isVerified(it)
                            onClicked(View.OnClickListener {
                                if (viewModel.dataManager.isHostOrGuest) {
                                    TrustAndVerifyActivity.openActivity(baseActivity!!, "profile", "facebook")
                                }else{
                                    TrustAndVerifyActivity.openActivity(baseActivity!!, "profile", "facebook")
                                }
                            })
                        }
                    }
                    viewholderVerifiedInfo {
                        id("googleVerified")
                        profileDetails.googleVerification?.let {
                            if (it) {
                                verifiedText(resources.getString(R.string.google_connected))
                            }else{
                                verifiedText(getString(R.string.connect_google))
                            }
                            isVerified(it)
                            onClicked(View.OnClickListener {
                                if (viewModel.dataManager.isHostOrGuest) {
                                    TrustAndVerifyActivity.openActivity(baseActivity!!, "profile", "google")
                                }else{
                                    TrustAndVerifyActivity.openActivity(baseActivity!!, "profile", "google")
                                }
                            })
                        }
                    }
                    viewholderVerifiedInfo {
                        id("phoneVerified")
                        profileDetails.phoneVerification?.let {
                            if(it) {
                                verifiedText(resources.getString(R.string.phone_verified))
                            }else{
                                verifiedText(getString(R.string.verify_phone))
                            }
                            isVerified(it)
                            onClicked(View.OnClickListener {
                                val intent = Intent(context, ConfirmPhnoActivity::class.java)
                                startActivityForResult(intent, REQUEST_CODE)
                            })
                        }
                    }
                    viewholderProfileLogoutBtn {
                        id("Logout")
                        onClick(View.OnClickListener { showAlertDialog() })
                    }
                    /*viewholderProfileLogoutBtn {
                        id("sp")
                        onClick(View.OnClickListener {
                            LocaleHelper.setLocale(it.context, "fr")
                            navigateToSplash()
                        })
                    }
                    viewholderProfileLogoutBtn {
                        id("pt")
                        onClick(View.OnClickListener {
                            LocaleHelper.setLocale(it.context, "pt")
                            navigateToSplash()
                        })
                    }*/
                }
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(context!!)
                .setTitle("Logout")
                .setMessage(resources.getString(R.string.are_you_sure_you_want_to_logout))
                .setPositiveButton(resources.getString(R.string.log_out)) { _, _ -> viewModel.signOut() }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    fun onRefresh() {
        if (isAdded) {
            if(::mViewModelFactory.isInitialized) {
                viewModel.loading.value?.let {
                    if (it) {
                        viewModel.getProfileDetails()
                    }
                }
            }
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flProfileFragment.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun showProfileDetails() {
        if (isAdded) {
            if (mBinding.rvProfile.adapter != null) {
                mBinding.rvProfile.requestModelBuild()
            } else {
                //setUp()
                setUI()
            }
        }
    }

    override fun onResume() {
        viewModel.getProfileDetails()
        viewModel.getDataFromPref()
        super.onResume()
    }

    override fun onDestroyView() {
        mBinding.rvProfile.adapter = null
        super.onDestroyView()
    }

    override fun navigateToSplash() {
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        baseActivity?.finish()
    }

    override fun onRetry() {
        viewModel.getProfileDetails()
    }

    override fun onDestroy() {
        mBinding.rvProfile.adapter = null
        super.onDestroy()
    }

}