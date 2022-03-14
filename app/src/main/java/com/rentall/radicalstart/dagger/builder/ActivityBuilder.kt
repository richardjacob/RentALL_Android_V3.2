package com.rentall.radicalstart.dagger.builder

import com.rentall.radicalstart.host.payout.addPayout.AddPayoutActivity
import com.rentall.radicalstart.host.payout.addPayout.AddPayoutFragmentProvider
import com.rentall.radicalstart.host.payout.addPayout.StripeWebViewActivity
import com.rentall.radicalstart.host.payout.editpayout.EditPayoutActivity
import com.rentall.radicalstart.host.payout.editpayout.EditPayoutFragmentProvider
import com.rentall.radicalstart.host.photoUpload.Step2FragmentProvider
import com.rentall.radicalstart.host.photoUpload.UploadPhotoActivity
import com.rentall.radicalstart.ui.AuthTokenExpireActivity
import com.rentall.radicalstart.ui.WebViewActivity
import com.rentall.radicalstart.ui.auth.AuthActivity
import com.rentall.radicalstart.ui.auth.AuthFragmentProvider
import com.rentall.radicalstart.ui.booking.BookingActivity
import com.rentall.radicalstart.ui.booking.BookingFragmentProvider
import com.rentall.radicalstart.ui.cancellation.CancellationActivity
import com.rentall.radicalstart.ui.entry.EntryActivity
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.home.HomeFragmentProvider
import com.rentall.radicalstart.ui.host.HostFinalActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.ui.host.hostHome.HostHomeFragmentProvider
import com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail.HostInboxMsgActivity
import com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail.HostNewInboxMsgActivity
import com.rentall.radicalstart.ui.host.step_one.StepOneActivity
import com.rentall.radicalstart.ui.host.step_one.StepOneFragmentProvider
import com.rentall.radicalstart.ui.host.step_three.StepThreeActivity
import com.rentall.radicalstart.ui.host.step_three.StepThreeFragmentProvider
import com.rentall.radicalstart.ui.host.step_two.StepTwoActivity
import com.rentall.radicalstart.ui.host.step_two.StepTwoFragmentProvider
import com.rentall.radicalstart.ui.inbox.msg_detail.InboxMsgActivity
import com.rentall.radicalstart.ui.inbox.msg_detail.NewInboxMsgActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.listing.ListingDetailsFragmentProvider
import com.rentall.radicalstart.ui.payment.PaymentTypeActivity
import com.rentall.radicalstart.ui.payment.PaymentTypeFragmentProvider
import com.rentall.radicalstart.ui.profile.about.AboutActivity
import com.rentall.radicalstart.ui.profile.about.why_Host.WhyHostActivity
import com.rentall.radicalstart.ui.profile.confirmPhonenumber.ConfirmPhnoActivity
import com.rentall.radicalstart.ui.profile.confirmPhonenumber.ConfirmPhnoFragmentProvider
import com.rentall.radicalstart.ui.profile.edit_profile.EditProfileActivity
import com.rentall.radicalstart.ui.profile.edit_profile.EditProfileFragmentProvider
import com.rentall.radicalstart.ui.profile.feedback.FeedbackActivity
import com.rentall.radicalstart.ui.profile.review.ReviewActivity
import com.rentall.radicalstart.ui.profile.review.ReviewFragmentProvider
import com.rentall.radicalstart.ui.profile.setting.SettingActivity
import com.rentall.radicalstart.ui.profile.setting.SettingFragmentProvider
import com.rentall.radicalstart.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.rentall.radicalstart.ui.reservation.ReservationActivity
import com.rentall.radicalstart.ui.reservation.ReservationFragmentProvider
import com.rentall.radicalstart.ui.saved.createlist.CreateListActivity
import com.rentall.radicalstart.ui.splash.SplashActivity
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.ui.user_profile.UserProfileFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun bindEntryActivity(): EntryActivity

    @ContributesAndroidInjector(modules = [AuthFragmentProvider::class])
    abstract fun bindAuthActivity(): AuthActivity

    @ContributesAndroidInjector
    abstract fun bindSplashActivity(): SplashActivity

    @ContributesAndroidInjector(modules = [HomeFragmentProvider::class])
    abstract fun bindHomeActivity(): HomeActivity

    @ContributesAndroidInjector(modules = [EditProfileFragmentProvider::class])
    abstract fun bindEditProfileActivity(): EditProfileActivity

    @ContributesAndroidInjector(modules = [ListingDetailsFragmentProvider::class])
    abstract fun bindListingDetailActivity(): ListingDetails

    @ContributesAndroidInjector(modules = [BookingFragmentProvider::class])
    abstract fun bindBookingActivity(): BookingActivity

    @ContributesAndroidInjector(modules = [ReservationFragmentProvider::class])
    abstract fun bindReservationActivity(): ReservationActivity

    @ContributesAndroidInjector
    abstract fun bindCancellationActivity(): CancellationActivity

    @ContributesAndroidInjector
    abstract fun bindInboxMsgActivity(): InboxMsgActivity

    @ContributesAndroidInjector(modules = [UserProfileFragmentProvider::class])
    abstract fun bindUserProfileActivity(): UserProfileActivity

    @ContributesAndroidInjector
    abstract fun bindAuthTokenExpireActivity(): AuthTokenExpireActivity

    @ContributesAndroidInjector
    abstract fun bindCreateListActivity(): CreateListActivity

    @ContributesAndroidInjector(modules = [ConfirmPhnoFragmentProvider::class])
    abstract fun bindConfirmPhnoActivity(): ConfirmPhnoActivity

    @ContributesAndroidInjector
    abstract fun bindTrustAndVerifyActivity() : TrustAndVerifyActivity

    @ContributesAndroidInjector(modules = [StepOneFragmentProvider::class])
    abstract fun bindStep_one_Activity(): StepOneActivity

    @ContributesAndroidInjector
    abstract fun bindHostFinalActivity(): HostFinalActivity

    @ContributesAndroidInjector(modules = [HostHomeFragmentProvider::class])
    abstract fun bindHostHomeActivity(): HostHomeActivity

    @ContributesAndroidInjector
    abstract fun bindHostInboxMsgActivity(): HostInboxMsgActivity

    @ContributesAndroidInjector
    abstract fun bindNewInboxMsgActivity() : NewInboxMsgActivity

    @ContributesAndroidInjector
    abstract fun bindHostNewInboxMsgActivity() : HostNewInboxMsgActivity

    @ContributesAndroidInjector(modules = [StepTwoFragmentProvider::class])
    abstract fun bindStepTwoActivity() : StepTwoActivity

    @ContributesAndroidInjector(modules = [StepThreeFragmentProvider::class])
    abstract fun bindStepThreeActivity() : StepThreeActivity

    @ContributesAndroidInjector(modules = [EditPayoutFragmentProvider::class])
    abstract fun bindEditPayoutActivity(): EditPayoutActivity

    @ContributesAndroidInjector(modules = [AddPayoutFragmentProvider::class])
    abstract fun bindAddPayoutActivity(): AddPayoutActivity

    @ContributesAndroidInjector(modules = [Step2FragmentProvider::class])
    abstract fun bindUploadPhotoActivity(): UploadPhotoActivity

    @ContributesAndroidInjector(modules = [SettingFragmentProvider::class])
    abstract fun bindSettingActivity(): SettingActivity

    @ContributesAndroidInjector
    abstract fun bindFeedbackActivity(): FeedbackActivity

    @ContributesAndroidInjector
    abstract fun bindStripeWebViewActivity(): StripeWebViewActivity

    @ContributesAndroidInjector
    abstract fun bindAboutActivity(): AboutActivity

    @ContributesAndroidInjector
    abstract fun bindWhyHostFragment(): WhyHostActivity

    @ContributesAndroidInjector(modules = [ReviewFragmentProvider::class])
    abstract fun bindReviewActivity(): ReviewActivity


    @ContributesAndroidInjector(modules = [PaymentTypeFragmentProvider::class])
    abstract fun bindPaymentActivity(): PaymentTypeActivity

    @ContributesAndroidInjector
    abstract fun bindWebViewActivity(): WebViewActivity
}
