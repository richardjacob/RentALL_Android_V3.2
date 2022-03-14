package com.rentall.radicalstart.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityAuthenticationBinding
import com.rentall.radicalstart.ui.auth.AuthViewModel.Screen.*
import com.rentall.radicalstart.ui.auth.birthday.BirthdayFragment
import com.rentall.radicalstart.ui.auth.email.EmailFragment
import com.rentall.radicalstart.ui.auth.forgotpassword.ForgotPasswordFragment
import com.rentall.radicalstart.ui.auth.login.LoginFragment
import com.rentall.radicalstart.ui.auth.name.NameCreationFragment
import com.rentall.radicalstart.ui.auth.password.PasswordFragment
import com.rentall.radicalstart.ui.auth.resetPassword.ResetPasswordFragment
import com.rentall.radicalstart.ui.auth.signup.SignupFragment
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.vo.FromDeeplinks
import com.rentall.radicalstart.vo.Outcome
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val RC_SIGN_IN: Int = 99

class AuthActivity : BaseActivity<ActivityAuthenticationBinding, AuthViewModel>(),
         AuthNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    private lateinit var mBinding: ActivityAuthenticationBinding
    private var eventCompositeDisposal = CompositeDisposable()
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mFbCallbackManager: CallbackManager? = null
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_authentication
    override val viewModel: AuthViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(AuthViewModel::class.java)
    private lateinit var auth: FirebaseAuth

    companion object {
        @JvmStatic fun openActivity(activity: Activity, isDeepLink: Boolean, from: FromDeeplinks, email: String, token: String) {
            val intent = Intent(activity, AuthActivity::class.java)
            intent.putExtra("deepLink", isDeepLink)
            intent.putExtra("email", email)
            intent.putExtra("token", token)
            intent.putExtra("from", from.ordinal)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        entryPoint(savedInstanceState)
        initSocialLoginSdk()
        initRxBusListener()
        subscribeToLiveData()
    }

    private fun entryPoint(savedInstanceState: Bundle?) {
        val token = intent.getStringExtra("token")
        val email = intent.getStringExtra("email")
        val from = intent.getIntExtra("from", 0)
        if (!token.isNullOrEmpty() && !email.isNullOrEmpty()) {
            viewModel.token.value = (token)
            viewModel.resetEmail.value = (email)
            viewModel.deeplinks.value = from
            when (from) {
                FromDeeplinks.ResetPassword.ordinal -> {
                    viewModel.currentScreen.value = AuthViewModel.Screen.CHANGEPASSWORD
                    addFragmentToActivity(mBinding.flAuth.id, ResetPasswordFragment.newInstance(token, email), "ResetPassword")
                    if (viewModel.validateForgotPassToken()) {
                        viewModel.tokenVerification()
                    }
                }
                FromDeeplinks.EmailVerification.ordinal -> {
                    addFragmentToActivity(mBinding.flAuth.id, SignupFragment(),"SignUp")
                    showToast(resources.getString(R.string.please_login_to_verify_your_email))
                    viewModel.currentScreen.value = AuthViewModel.Screen.SIGNUP
                }
            }
        } else if (savedInstanceState == null) {
            addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
        }
    }

    private fun initSocialLoginSdk() {
        auth = FirebaseAuth.getInstance()
        LoginManager.getInstance().logOut()
        mFbCallbackManager = CallbackManager.Factory.create()
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build())
    }

    private fun initRxBusListener() {
        eventCompositeDisposal.add(RxBus.listen(UiEvent.Navigate::class.java)
                .debounce(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { hideSnackbar() }
                .subscribe { navigateScreen(it.screen, *it.params) })
    }

    private fun subscribeToLiveData() {
        viewModel.fireBaseResponse?.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { outcome ->
                when(outcome) {
                    is Outcome.Error -> {
                        showError()
                    }
                    is Outcome.Failure -> {
                        showError()
                    }
                    is Outcome.Success -> {
                        if (viewModel.validateDetails()) {
                            if (viewModel.generateFirebase.value == Constants.registerTypeEMAIL) {
                                viewModel.signupUser()
                            } else {
                                viewModel.socialLogin(viewModel.generateFirebase.value!!)
                            }
                        }
                    }
                    is Outcome.Progress -> {
                        viewModel.isLoading.set(outcome.loading)
                    }
                }
            }
        })
    }

    private fun fbSignIn() {
        mFbCallbackManager?.let {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"))
            LoginManager.getInstance().registerCallback(it, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
                        try {
                            if(`object`?.has("email") == true){
                                val fbEmail =`object`.getString("email")
                                val fbFirstName = `object`.getString("first_name")
                                val fbLastName = `object`.getString("last_name")
                                val fbPhoto = "https://graph.facebook.com/"+ loginResult.accessToken.userId +"/picture?height=255&width=255"
                                viewModel.firstName.set(fbFirstName)
                                viewModel.lastName.set(fbLastName)
                                viewModel.email.set(fbEmail)
                                viewModel.profilePic.value = fbPhoto
                                validateData(Constants.registerTypeFB)
                            }else{
                                showToast(getString(R.string.email_not_exist))
                            }
                        } catch (e: Exception) {
                            viewModel.resetValues()
                            e.printStackTrace()
                            showError()
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id, name, first_name, last_name, email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    LoginManager.getInstance().logOut()
                }

                override fun onError(exception: FacebookException) {
                    viewModel.resetValues()
                    LoginManager.getInstance().logOut()
                }
            })
        } ?: showError()
    }

    private fun googleSignIn() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener {
            if (it.isSuccessful) {
                mGoogleSignInClient?.let {
                    val signInIntent = mGoogleSignInClient?.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            } else {
                showError()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            mFbCallbackManager?.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            updateUI(account)
        } catch (e: ApiException) {
            Timber.tag("error").w("signInResult:failed code=%s", e.statusCode)
            mGoogleSignInClient?.signOut()
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        try {
            val pic = account!!.photoUrl.toString() + "?sz=250"
            val name = account.displayName.toString()
            val firstName = name.split("\\s".toRegex())[0]
            val lastName = name.split("\\s".toRegex())[1]
            val email = account.email
            viewModel.firstName.set(firstName)
            viewModel.lastName.set(lastName)
            viewModel.email.set(email)
            viewModel.profilePic.value = pic
            validateData(Constants.registerTypeGOOGLE)
        } catch (e: Exception) {
            e.printStackTrace()
            mGoogleSignInClient?.signOut()
            viewModel.resetValues()
            showError()
        }
    }

    private fun setAction(type: String) {
        if (type == "Login") {
            if (supportFragmentManager.findFragmentByTag("Login") is LoginFragment) {
                (supportFragmentManager.findFragmentByTag("Login") as LoginFragment?)?.showPassword()
            }
            hideSnackbar()
        } else if (type == "Signup"){
            if (supportFragmentManager.findFragmentByTag("Email") is EmailFragment) {
                navigateScreen(AuthViewModel.Screen.AuthScreen)
            }
        }
        else {
            removeAllBackstack()
        }
    }

    private fun moveToHomeScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun moveToEmailScreen(firstName: String, lastName: String) {
        viewModel.setFirstName(firstName)
        viewModel.setLastName(lastName)
        replaceFragmentInActivity(mBinding.flAuth.id, EmailFragment.newInstance(viewModel.email.get()!!), "Email")
    }

    private fun moveToPasswordScreen(email: String) {
        viewModel.setEmail(email)
        replaceFragmentInActivity(mBinding.flAuth.id, PasswordFragment.newInstance(viewModel.password.get()!!), "Password")
    }

    private fun moveToBirthdayScreen(password: String) {
        viewModel.setPassword(password)
        replaceFragmentInActivity(mBinding.flAuth.id, BirthdayFragment(), "Birthday")
    }

    private fun startEmailVerify() {
        TrustAndVerifyActivity.openFromVerify(this, viewModel.token.value!!, viewModel.resetEmail.value!!, "deeplink")
        finish()
    }

    private fun signUpUser(birthday: String) {
        viewModel.setBirthday(birthday)
        validateData(Constants.registerTypeEMAIL)
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        try {
            viewModel.currentScreen.value = screen
            when (screen) {
                SIGNUP -> addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                NAME -> replaceFragmentInActivity(mBinding.flAuth.id, NameCreationFragment.newInstance(viewModel.firstName.get()!!, viewModel.lastName.get()!!), "Name")
                EMAIL -> { moveToEmailScreen(params[0]!!, params[1]!!) }
                PASSWORD -> { moveToPasswordScreen(params[0]!!) }
                BIRTHDAY -> { moveToBirthdayScreen(params[0]!!) }
                LOGIN -> {
                    replaceFragmentInActivity(mBinding.flAuth.id, LoginFragment(), "Login")
                }
                FORGOTPASSWORD -> {
                    viewModel.deeplinks.value = null
                    viewModel.resetEmail.value = null
                    viewModel.token.value = null
                    replaceFragmentInActivity(mBinding.flAuth.id, ForgotPasswordFragment(), "ForgotPassword")
                }
                REMOVEALLBACKSTACK -> removeAllBackstack()
                POPUPSTACK -> this.onBackPressed()
                FB -> checkNetwork { fbSignIn() }
                GOOGLE -> checkNetwork { googleSignIn() }
                HOME -> { signUpUser(params[0]!!) }
                MOVETOHOME -> {
                    if (viewModel.isFromDeeplink() && viewModel.deeplinks.value == FromDeeplinks.EmailVerification.ordinal) {
                        startEmailVerify()
                    } else {
                        moveToHomeScreen()
                    }
                }
                MOVETOEMAILVERIFY -> { startEmailVerify() }
                AuthScreen -> { addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "Signup") }
                CHANGEPASSWORD -> { }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    private fun validateData(type: String) {
        if (viewModel.validateFirebase()) {
            if (viewModel.validateDetails()) {
                if (type == Constants.registerTypeEMAIL) {
                    if (viewModel.validateDetailForEmail()) {
                        viewModel.signupUser()
                    } else showError()
                } else viewModel.socialLogin(type)
            } else showError()
        } else viewModel.generateFirebase.value = type
    }

    override fun showSnackbar(title: String, msg: String, action: String?) {
        if (!action.isNullOrEmpty()) {
            snackbar = Utils.showSnackbarWithAction(this,viewDataBinding!!.root, title, msg) { setAction(action) }
        } else {
            super.showSnackbar(title, msg, action)
        }
    }

    override fun onBackPressed() {
        hideSnackbar()
        if (supportFragmentManager.backStackEntryCount == 1) {
            viewModel.resetValues()
        }
        if (viewModel.currentScreen.value == AuthViewModel.Screen.CHANGEPASSWORD) {
            if (viewModel.getLoginstatus()) {
                val intent = Intent(this, SplashActivity::class.java)
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_right)
                startActivity(intent)
                finish()
            } else {
                viewModel.currentScreen.value = AuthViewModel.Screen.SIGNUP
                addFragmentToActivity(mBinding.flAuth.id, SignupFragment(), "SignUp")
                return
            }
        }
        if (viewModel.isLoading.get()) {
            viewModel.isLoading.set(false)
            viewModel.clearCompositeDisposal()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("firstName", viewModel.firstName.get())
        outState.putString("lastName", viewModel.lastName.get())
        outState.putString("email", viewModel.email.get())
        outState.putString("password", viewModel.password.get())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.firstName.set(savedInstanceState?.getString("firstName", ""))
        viewModel.lastName.set(savedInstanceState?.getString("lastName", ""))
        viewModel.email.set(savedInstanceState?.getString("email", ""))
        viewModel.password.set(savedInstanceState?.getString("password", ""))
    }

    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flAuth.id)
        if (fragment is BaseFragment<*,*>) {
            fragment.onRetry()
        }
    }

}