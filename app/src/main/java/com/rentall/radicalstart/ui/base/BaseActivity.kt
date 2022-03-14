package com.rentall.radicalstart.ui.base

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ui.AuthTokenExpireActivity
import com.rentall.radicalstart.util.LocaleHelper
import com.rentall.radicalstart.util.LocaleHelper.onAttachAndGetConfig
import com.rentall.radicalstart.util.NetworkUtils
import com.rentall.radicalstart.util.Utils
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber


abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel<*>> : DaggerAppCompatActivity(), BaseFragment.Callback, BaseNavigator, DialogInterface {


    var viewDataBinding: T? = null
        private set
    private var mViewModel: V? = null
    var snackbar: Snackbar? = null
    var topView: View? = null

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    abstract val bindingVariable: Int

    /**
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract val viewModel: V

    val isNetworkConnected: Boolean
        get() = NetworkUtils.isNetworkConnected(applicationContext)

    abstract fun onRetry()

    override fun onFragmentAttached() {

    }

    override fun onFragmentDetached(tag: String) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        performDependencyInjection()
        super.onCreate(savedInstanceState)
        performDataBinding()
    }

    /*@TargetApi(Build.VERSION_CODES.M)
    fun hasPermission(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }*/

    override fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun hideLoading() {

    }

    override fun openSessionExpire() {
        AuthTokenExpireActivity.openActivity(this)
    }

    fun performDependencyInjection() {
        AndroidInjection.inject(this)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsSafely(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    fun showLoading() {
//        hideLoading()
//        mProgressDialog = CommonUtils.showLoadingDialog(this)
    }

    private fun performDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView(this, layoutId)
        this.mViewModel = if (mViewModel == null) viewModel else mViewModel
        viewDataBinding!!.setVariable(bindingVariable, mViewModel)
        viewDataBinding!!.executePendingBindings()
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun showSnackbar(title: String, msg: String, action: String?) {
        hideSnackbar()
        snackbar = if (topView == null) {
            Utils.showSnackBar(this, viewDataBinding!!.root, title, msg)
        } else {
            Utils.showSnackBar(this, topView!!, title, msg)
        }
    }

    override fun showSnackbarWithRetry() {
        hideSnackbar()
    }

    override fun dismiss() {
        dismiss()
    }

    override fun cancel() {
        dismiss()
    }

    override fun showError(e: Exception?) {
        hideSnackbar()
        Timber.e(e,"ERROR_APP")
        snackbar = if (topView == null) {
            Utils.showSnackBar(this, viewDataBinding!!.root, resources.getString(R.string.error),
                    resources.getString(R.string.something_went_wrong) ) // + e?.message
        } else {
            Utils.showSnackBar(this, topView!!, resources.getString(R.string.error),
                    resources.getString(R.string.something_went_wrong) ) // + e?.message
        }
    }

    override fun showOffline() {
        hideSnackbar()
        snackbar = if (topView == null) {
            Utils.showSnackbarWithAction2(this,
                    viewDataBinding!!.root,
                    Utils.getHtmlText(this,resources.getString(R.string.error), resources.getString(R.string.currently_offline)),
                    resources.getString(R.string.retry)) { onRetry() }
        } else {
            Utils.showSnackbarWithAction2(this,
                    topView!!,
                    Utils.getHtmlText(this,resources.getString(R.string.error), resources.getString(R.string.currently_offline)),
                    resources.getString(R.string.retry)) { onRetry() }
        }
    }

    override fun hideSnackbar() {
        snackbar?.let {
            if (it.isShown) {
                it.dismiss()
            }
        }
    }

    fun checkNetwork(action: () -> Unit) {
        if (isNetworkConnected) {
            action()
        } else {
            showOffline()
        }
    }

    override fun onPause() {
        //viewModel.clearUnreadCountApi()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        //viewModel.getUnreadCountAndBanStatus()
    }

    override fun attachBaseContext(base: Context) {
        val context = LocaleHelper.onAttach(base)
        applyOverrideConfiguration(onAttachAndGetConfig(base))
        Timber.d("LangCheck attachBaseContext baseact " + context.resources.configuration.locale.displayLanguage)
        super.attachBaseContext(base)
    }

}

