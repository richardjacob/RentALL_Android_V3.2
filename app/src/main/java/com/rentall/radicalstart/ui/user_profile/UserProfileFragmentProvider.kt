package com.rentall.radicalstart.ui.user_profile

import com.rentall.radicalstart.ui.profile.confirmPhonenumber.ConfirmPhnoFragment
import com.rentall.radicalstart.ui.user_profile.report_user.ReportUserFragment
import com.rentall.radicalstart.ui.user_profile.review.ReviewFragment
import com.rentall.radicalstart.ui.user_profile.verified.VerifiedInfoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class UserProfileFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideReportUserFragment(): ReportUserFragment

    @ContributesAndroidInjector
    abstract fun provideReviewFragment(): ReviewFragment

    @ContributesAndroidInjector
    abstract fun provideVerifiedInfoFragment(): VerifiedInfoFragment


}