package com.rentall.radicalstart.ui.auth

import com.rentall.radicalstart.ui.auth.birthday.BirthdayFragment
import com.rentall.radicalstart.ui.auth.email.EmailFragment
import com.rentall.radicalstart.ui.auth.forgotpassword.ForgotPasswordFragment
import com.rentall.radicalstart.ui.auth.login.LoginFragment
import com.rentall.radicalstart.ui.auth.name.NameCreationFragment
import com.rentall.radicalstart.ui.auth.password.PasswordFragment
import com.rentall.radicalstart.ui.auth.resetPassword.ResetPasswordFragment
import com.rentall.radicalstart.ui.auth.signup.SignupFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class AuthFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideSignupFragmentFactory(): SignupFragment

    @ContributesAndroidInjector
    abstract fun provideNameCreationFragmentFactory(): NameCreationFragment

    @ContributesAndroidInjector
    abstract fun provideEmailFragmentFactory(): EmailFragment

    @ContributesAndroidInjector
    abstract fun providePasswordFragmentFactory(): PasswordFragment

    @ContributesAndroidInjector
    abstract fun provideBirthdayFragmentFactory(): BirthdayFragment

    @ContributesAndroidInjector
    abstract fun provideLoginFragmentFactory(): LoginFragment

    @ContributesAndroidInjector
    abstract fun provideForgotPasswordFragmentFactory(): ForgotPasswordFragment

    @ContributesAndroidInjector
    abstract fun provideResetPasswordFragmentFactory(): ResetPasswordFragment
}
