package com.rentall.radicalstart.ui.base


interface BaseNavigator {

  //  fun navigateScreen(screen: AuthViewModel.PHScreen, vararg params: String?)

    fun showToast(msg: String)

    fun showSnackbar(title: String, msg: String, action: String? = null)

    fun showSnackbarWithRetry()

    fun showError(e: Exception? = null)

    fun showOffline()

    fun hideSnackbar()

    fun hideKeyboard()

    fun openSessionExpire()

}