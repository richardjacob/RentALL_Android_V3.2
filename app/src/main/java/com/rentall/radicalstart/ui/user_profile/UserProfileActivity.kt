package com.rentall.radicalstart.ui.user_profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.ActivityUserprofileBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.user_profile.report_user.ReportUserFragment
import com.rentall.radicalstart.ui.user_profile.review.ReviewFragment
import com.rentall.radicalstart.ui.user_profile.verified.VerifiedInfoFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class UserProfileActivity: BaseActivity<ActivityUserprofileBinding, UserProfileViewModel>() {

    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityUserprofileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_userprofile
    override val viewModel: UserProfileViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(UserProfileViewModel::class.java)

     companion object {
        @JvmStatic fun openProfileActivity(context: Context, profileId: Int) {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("profileId", profileId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.userProfile.observe(this, Observer { results ->
            results?.let {
                setUp(it)
            }
        })
    }

    private fun initView() {
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.tvToolbarHeading.gone()
        mBinding.actionBar.ivNavigateup.onClick {
           onBackPressed()
        }
        viewModel.setValuesFromIntent(intent)
    }

    private fun setUp(it: ShowUserProfileQuery.Results) {
        mBinding.rlUserProfile.withModels {
            viewholderUserImage {
                id("viewHolderUserImage")
                pic(it.picture())
            }
            viewholderUserName {
                id("name")
                name(it.firstName() + " " + it.lastName())
                paddingTop(true)
                paddingBottom(true)
            }
            if (it.location() != null && it.location().isNullOrBlank().not()) {

                viewholderDivider {
                    id("viewHolderDivider - loc")
                }

                viewholderUserNormalText {
                    id("address")
                    text(it.location())
                    paddingTop(true)
                    paddingBottom(true)
                }
            }
            if (it.info() != null && it.info().isNullOrEmpty().not()) {
                viewholderDivider {
                    id("viewHolderDivider - 1")
                }
                viewholderUserNormalText {
                    id("info")
                    text(it.info())
                    paddingBottom(true)
                    paddingTop(true)
                }
            }
            viewholderDivider {
                id("viewHolderDivider - 2")
            }
            viewholderUserNormalText {
                id("memberSince")
                val preferences = PreferenceManager.getDefaultSharedPreferences(this@UserProfileActivity)
                val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
                text(resources.getString(R.string.member_since) +" " + Utils.memberSince(it.createdAt(),langType))
                paddingBottom(true)
                paddingTop(true)
            }
            viewholderDivider {
                id("viewHolderDivider - 3")
            }
            if (it.reviewsCount() != null) {
                if (it.reviewsCount()!! > 0) {
                    viewholderUserName {
                        id("reviews")
                        if (it.reviewsCount()!! > 0) {
                            name(resources.getQuantityString(R.plurals.review_count, it.reviewsCount()!!))
                        } else {
                            name("${it.reviewsCount()}" + resources.getString(R.string.review))
                        }
                        paddingTop(true)
                        paddingBottom(true)
                    }
                    viewholderListingDetailsListShowmore {
                        id("see all reviews")
                        paddingTop(true)
                        if(it.reviewsCount()!! > 1) {
                            text(resources.getString(R.string.read_all) + " ${it.reviewsCount()} " + resources.getString(R.string.reviews))
                        }else{
                            text(resources.getString(R.string.read_all) + " ${it.reviewsCount()} " + resources.getString(R.string.review))
                        }
                        clickListener(View.OnClickListener {
                            try {
                                viewModel.userProfile.value?.let {
                                    openFragment(ReviewFragment.newInstance(it.reviewsCount(), it.firstName() + it.lastName()))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showError()
                            }
                        })
                    }
                    viewholderDivider {
                        id("viewHolderDivider - 4")
                    }
                }
            }

            val verifyInfo = viewModel.userProfile.value?.userVerifiedInfo()!!
            if(verifyInfo.isEmailConfirmed!! || verifyInfo.isGoogleConnected!! || verifyInfo.isFacebookConnected!! || verifyInfo.isIdVerification!! || verifyInfo.isPhoneVerified!!) {
                viewholderUserVerifiedHeader {
                    id("VerifiedHeader")
                    text(resources.getString(R.string.verified_info))
                    paddingTop(true)
                }
                viewholderUserVerifiedText {
                    id("VerifiedText")
                    var str = ""
                    if (verifyInfo.isEmailConfirmed!!) {
                        str = resources.getString(R.string.email_confirmed)+ ", "
                    }
                    if (verifyInfo.isGoogleConnected!!) {
                        str = str + resources.getString(R.string.google_connected)+ ", "
                    }
                    if (verifyInfo.isFacebookConnected!!) {
                        str = str +  resources.getString(R.string.facebook_connected)+ ", "
                    }
                    if (verifyInfo.isIdVerification!!) {
                        str = str +  resources.getString(R.string.document_verification)+ ", "
                    }
                    if (verifyInfo.isPhoneVerified!!) {
                        str = str +  resources.getString(R.string.phone_verified)+ ", "
                    }
                    str = str.substring(0, str.length - 2)

                    text(str)
                    paddingBottom(true)
                }
                viewholderListingDetailsListShowmore {
                    id("learnMore")
                    paddingTop(true)
                    text(resources.getString(R.string.learn_more))
                    clickListener(View.OnClickListener { openFragment(VerifiedInfoFragment()) })
                }
                viewholderDivider {
                    id("viewHolderDivider - 5")
                }
            }

            if (it.userId() != viewModel.getUserId()) {
                viewholderUserNormalText {
                    id("ReportThisUser")
                    text(resources.getString(R.string.report_this_user))
                    paddingBottom(true)
                    paddingTop(true)
                    clickListener(View.OnClickListener {
                        openFragment(ReportUserFragment())
                    })
                }
            }
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flUserprofile.id, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onRetry() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            (supportFragmentManager.fragments[supportFragmentManager.backStackEntryCount] as BaseFragment<*,*>).onRetry()
        } else {
            viewModel.getUserProfile()
        }
    }
}