package com.rentall.radicalstart.ui.profile.setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivitySettingBinding
import com.rentall.radicalstart.host.payout.editpayout.EditPayoutActivity
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.profile.about.AboutActivity
import com.rentall.radicalstart.ui.profile.setting.currency.CurrencyBottomSheet
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.util.LocaleHelper
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderDivider
import com.rentall.radicalstart.viewholderProfileLists
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject
import kotlin.system.exitProcess

class SettingActivity: BaseActivity<ActivitySettingBinding, SettingViewModel>(),SettingsNavigator{

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_setting
    override val viewModel: SettingViewModel
        get() = ViewModelProviders.of(this,mViewModelFactory).get(SettingViewModel::class.java)
    lateinit var mBinding : ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.account_setting)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }
        initView()
        subscribeToLiveData(savedInstanceState)
    }

    private fun initView() {
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
        )
    }

    fun subscribeToLiveData(savedInstanceState: Bundle?){
        viewModel.loadSettingData().observe(this, Observer {
            if(it.isNullOrEmpty().not()){
                setUp()
            }
        })
    }

    fun setUp(){
        mBinding.rvSetting.withModels {
            viewholderProfileLists {
                id("payoutPreference")
                name(getString(R.string.payout_preference))
                paddingbottam(true)
                paddingtop(true)
                iconVisible(true)
                onClick(View.OnClickListener {
                    val intent = Intent(this@SettingActivity, EditPayoutActivity::class.java)
                    startActivity(intent)
                })
            }

                viewholderDivider {
                    id("div1")
                }

            viewholderProfileLists {
                id("language")
                name(getString(R.string.languages))
                paddingbottam(true)
                paddingtop(true)
                iconVisible(true)
                textVisible(true)
                currencyText(viewModel.appLanguage.get())
                onClick(View.OnClickListener {
                    viewModel.appLanguage.get().let {
                        CurrencyBottomSheet.newInstance("language").show(supportFragmentManager, "language")
                    }
                })
            }

            viewholderDivider {
                id("div1")
            }

                viewholderProfileLists {
                    id("currency")
                    name(getString(R.string.currency))
                    iconVisible(true)
                    paddingbottam(true)
                    paddingtop(true)
                    textVisible(true)
                    currencyText(viewModel.baseCurrency.get())
                    onClick(View.OnClickListener {
                        viewModel.baseCurrency.get()?.let {
                            CurrencyBottomSheet.newInstance("currency").show(supportFragmentManager, "currency")
                           //CurrencyBottomSheet().show(supportFragmentManager,"currency")
                        }
                    })
                }
                viewholderDivider {
                    id("div2")
                }

                viewholderProfileLists {
                    id("about")
                    name(getString(R.string.about))
                    iconVisible(true)
                    paddingbottam(true)
                    paddingtop(true)
                    onClick(View.OnClickListener {
                        val intent = Intent(this@SettingActivity, AboutActivity::class.java)
                        startActivity(intent)
                    })
                }
                viewholderDivider {
                    id("div3")
                }

                viewholderProfileLists {
                    id("logout")
                    name(getString(R.string.logout))
                    iconVisible(true)
                    paddingbottam(true)
                    paddingtop(true)
                    onClick(View.OnClickListener {
                        showAlertDialog()
                    })
                }

        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.are_you_sure_you_want_to_logout))
                .setPositiveButton(getString(R.string.log_out)) { _, _ -> viewModel.signOut() }
                .setNegativeButton(getString(R.string.CANCEL)) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


/*    override fun openSplashScreen() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }*/

    override fun openSplashScreen() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    override fun onRetry() {
       viewModel.getCurrency()
    }

    override fun navigateToSplash() {
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        val intent = Intent(this@SettingActivity, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

    override fun setLocale(key: String) {

        if (key == "en") {
            LocaleHelper.setLocale(applicationContext, "en")
            openSplashScreen()
        } else if(key == "es") {
            LocaleHelper.setLocale(applicationContext, "es")
            openSplashScreen()
        } else if(key == "fr") {
            LocaleHelper.setLocale(applicationContext, "fr")
            openSplashScreen()
        } else if(key == "pt") {
            LocaleHelper.setLocale(applicationContext, "pt")
            openSplashScreen()
        } else if(key == "it") {
            LocaleHelper.setLocale(applicationContext, "it")
            openSplashScreen()
        }else if(key == "ar") {
            LocaleHelper.setLocale(applicationContext, "ar")
            openSplashScreen()
        }
        else if(key == "iw") {
            LocaleHelper.setLocale(applicationContext, "iw")
            openSplashScreen()
        }
        else if(key == "he") {
            LocaleHelper.setLocale(applicationContext, "he")
            openSplashScreen()
        }

    }
}