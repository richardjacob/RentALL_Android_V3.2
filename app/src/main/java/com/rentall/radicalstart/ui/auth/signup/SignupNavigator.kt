package com.rentall.radicalstart.ui.auth.signup

import com.rentall.radicalstart.ui.auth.AuthViewModel

interface SignupNavigator {

/*    fun onCreateAccountButtonClick()

    fun onFacebookButtonClick()

    fun onGoogleButtonClick()

    fun onLoginButtonClick()

    fun onCloseButtonClick()
    */

    fun onButtonClick(screen: AuthViewModel.Screen)

}