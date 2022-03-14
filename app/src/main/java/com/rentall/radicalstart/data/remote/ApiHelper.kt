package com.rentall.radicalstart.data.remote

import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.api.Response
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.vo.Outcome
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.reactivex.Observable
import io.reactivex.Single

interface ApiHelper {

    //ClearHttpCache
    fun clearHttpCache() : Observable<Boolean>

    //Auth
    fun doSocailLoginApiCall(request: SocialLoginQuery): Single<Response<SocialLoginQuery.Data>>

    fun doServerLoginApiCall(request: LoginQuery): Single<Response<LoginQuery.Data>>

    fun doLogoutApiCall(request: LogoutMutation): Single<Response<LogoutMutation.Data>>

    fun doEmailVerificationApiCall(request: CheckEmailExistsQuery): Single<Response<CheckEmailExistsQuery.Data>>

    fun doSignupApiCall(request: SignupMutation): Single<Response<SignupMutation.Data>>

    fun doForgotPasswordApiCall(request: ForgotPasswordMutation): Single<Response<ForgotPasswordMutation.Data>>

    fun doForgotPasswordVerificationApiCall(request: ForgotPasswordVerificationQuery): Single<Response<ForgotPasswordVerificationQuery.Data>>

    fun doResetPasswordApiCall(request: ResetPasswordMutation): Single<Response<ResetPasswordMutation.Data>>

    //Profile
    fun doGetProfileDetailsApiCall(request: GetProfileQuery): Single<Response<GetProfileQuery.Data>>

    fun doEditProfileApiCall(request: EditProfileMutation): Single<Response<EditProfileMutation.Data>>

    fun doGetLanguagesApiCall(request: UserPreferredLanguagesQuery): Single<Response<UserPreferredLanguagesQuery.Data>>

    //Listing
    fun doGetDefaultSettingApiCall(request: GetDefaultSettingQuery): Single<Response<GetDefaultSettingQuery.Data>>

    fun dogetPaymentSettingsApiCall(request: GetPaymentSettingsQuery): Single<Response<GetPaymentSettingsQuery.Data>>

    //Listing Details
    fun doSimilarListingApiCall(request: GetSimilarListingQuery): Single<Response<GetSimilarListingQuery.Data>>

    fun doListingDetailsApiCall(request: ViewListingDetailsQuery): Single<Response<ViewListingDetailsQuery.Data>>

    fun listOfReview(listId: Int, hostId: String, pageSize: Int): Listing<GetPropertyReviewsQuery.Result>

    fun contactHost(request: ContactHostMutation): Single<Response<ContactHostMutation.Data>>

    //Search
    fun getSearchListing(request: SearchListingQuery): Single<Response<SearchListingQuery.Data>>

    fun listOfSearchListing(query: SearchListingQuery.Builder, pageSize: Int): Listing<SearchListingQuery.Result>

    fun getLocationAutoComplete(location: String): Observable<List<AutocompletePrediction>?>

    // CurrencyList
    fun getCurrencyList(request: GetCurrenciesListQuery): Single<Response<GetCurrenciesListQuery.Data>>

    fun getBillingCalculation(request: GetBillingCalculationQuery): Single<Response<GetBillingCalculationQuery.Data>>

    fun getReservationDetails(request: GetReservationQuery): Single<Response<GetReservationQuery.Data>>

    fun getTripsDetails(request: GetAllReservationQuery): Single<Response<GetAllReservationQuery.Data>>

    fun listOfTripsList(query: GetAllReservationQuery.Builder, pageSize: Int): Listing<GetAllReservationQuery.Result>


    fun getUserReviews(query: GetUserReviewsQuery.Builder,pageSize: Int): Listing<GetUserReviewsQuery.Result>

    fun getPendingUserReviews(query: GetPendingUserReviewsQuery.Builder,pageSize: Int):Listing<GetPendingUserReviewsQuery.Result>

    fun getPendingUserReview(request: GetPendingUserReviewQuery) : Single<Response<GetPendingUserReviewQuery.Data>>

    fun writeReview(mutate: WriteUserReviewMutation): Single<Response<WriteUserReviewMutation.Data>>

    fun getPropertyReviews(query: GetPropertyReviewsQuery): Single<Response<GetPropertyReviewsQuery.Data>>

    fun listOfInbox(query: GetAllThreadsQuery.Builder, pageSize: Int): Listing<GetAllThreadsQuery.Result>

   // fun listOfInboxMsg(query: GetThreadsQuery.Builder, pageSize: Int): Listing<Message>

    fun listOfInboxMsg(query: GetThreadsQuery.Builder, pageSize: Int): Listing<GetThreadsQuery.ThreadItem>

    fun listOfInboxMsg1(query: GetThreadsQuery): Single<Response<GetThreadsQuery.Data>>

    fun sendMessage(mutate: SendMessageMutation): Single<Response<SendMessageMutation.Data>>

    fun getUnreadCount(query: GetUnReadCountQuery): Observable<Response<GetUnReadCountQuery.Data>>

    fun createReservation(request: CreateReservationMutation): Single<Response<CreateReservationMutation.Data>>

    fun createToken(card: Card): MutableLiveData<Outcome<Token>>

    fun setReadMessage(request: ReadMessageMutation): Single<Response<ReadMessageMutation.Data>>

    fun confirmReservation(request: ConfirmReservationMutation): Single<Response<ConfirmReservationMutation.Data>>

    fun getNewMessage(request: GetUnReadThreadCountQuery): Observable<Response<GetUnReadThreadCountQuery.Data>>

    //User Profile
    fun getUserProfile(request: ShowUserProfileQuery): Single<Response<ShowUserProfileQuery.Data>>

    fun createReportUser(request: CreateReportUserMutation): Single<Response<CreateReportUserMutation.Data>>

    fun listOfUserReview(builder: UserReviewsQuery.Builder, pageSize: Int): Listing<UserReviewsQuery.Result>

    //Cancellation
    fun getCancellationDetails(request: CancellationDataQuery): Single<Response<CancellationDataQuery.Data>>

    fun cancelReservation(request: CancelReservationMutation): Single<Response<CancelReservationMutation.Data>>

    //User Ban Status
    fun getUserBanStatus(request: UserBanStatusQuery): Observable<Response<UserBanStatusQuery.Data>>

    fun contactSupport(request: ContactSupportQuery): Single<Response<ContactSupportQuery.Data>>


    fun getCountryCode(request: GetCountrycodeQuery): Single<Response<GetCountrycodeQuery.Data>>

    //WishList
    fun createWishListGroup(request: CreateWishListGroupMutation): Single<Response<CreateWishListGroupMutation.Data>>

    fun CreateWishList(request: CreateWishListMutation): Single<Response<CreateWishListMutation.Data>>

    fun deleteWishListGroup(request: DeleteWishListGroupMutation): Single<Response<DeleteWishListGroupMutation.Data>>

    fun updateWishListGroup(request: UpdateWishListGroupMutation): Single<Response<UpdateWishListGroupMutation.Data>>

    fun getAllWishListGroup(request: GetAllWishListGroupQuery): Single<Response<GetAllWishListGroupQuery.Data>>

    fun getWishListGroup(request: GetWishListGroupQuery): Observable<Response<GetWishListGroupQuery.Data>>

    fun listOfWishListGroup(query: GetAllWishListGroupQuery.Builder, pageSize: Int): Listing<GetAllWishListGroupQuery.Result>

    fun listOfWishListWithoutPage(request: GetAllWishListGroupWithoutPageQuery): Single<Response<GetAllWishListGroupWithoutPageQuery.Data>>

    fun listOfWishList(query: GetWishListGroupQuery.Builder, pageSize: Int): Listing<GetWishListGroupQuery.WishList>

    fun getPhoneNumber(request: GetEnteredPhoneNoQuery): Single<Response<GetEnteredPhoneNoQuery.Data>>

    fun addPhoneNumber(request: AddPhoneNumberMutation): Single<Response<AddPhoneNumberMutation.Data>>

    fun verifyPhoneNumber(request: VerifyPhoneNumberMutation): Single<Response<VerifyPhoneNumberMutation.Data>>

    fun getWishList(query: GetWishListGroupQuery): Single<Response<GetWishListGroupQuery.Data>>


    //Email Verification
    fun sendConfirmationEmail(request: SendConfirmEmailQuery): Single<Response<SendConfirmEmailQuery.Data>>

    fun ConfirmCodeApiCall(request: CodeVerificationMutation): Single<Response<CodeVerificationMutation.Data>>

    fun SocialLoginVerify(request : SocialLoginVerifyMutation): Single<Response<SocialLoginVerifyMutation.Data>>

    fun getExploreListing(request : GetExploreListingsQuery): Single<Response<GetExploreListingsQuery.Data>>

    //Become a host
    fun doGetListingSettings(request : GetListingSettingQuery): Single<Response<GetListingSettingQuery.Data>>

    fun doCreateListing(request : CreateListingMutation): Single<Response<CreateListingMutation.Data>>

    //Host Features
    fun dogetListingSettings(request : GetListingSettingQuery): Single<Response<GetListingSettingQuery.Data>>

    fun doUpdateListingStep2(request : UpdateListingStep2Mutation): Single<Response<UpdateListingStep2Mutation.Data>>

    fun doUpdateListingStep3(request: UpdateListingStep3Mutation): Single<Response<UpdateListingStep3Mutation.Data>>

    fun ShowListPhotosQuery(request: ShowListPhotosQuery): Single<Response<ShowListPhotosQuery.Data>>

    fun doGetListingDetailsStep2Query(request: GetListingDetailsStep2Query): Single<Response<GetListingDetailsStep2Query.Data>>

    fun doManageListingSteps(request : ManageListingStepsMutation): Single<Response<ManageListingStepsMutation.Data>>

    fun doShowListingSteps(request: ShowListingStepsQuery): Single<Response<ShowListingStepsQuery.Data>>

    fun doGetStep1ListingDetailsQuery(request: GetStep1ListingDetailsQuery): Single<Response<GetStep1ListingDetailsQuery.Data>>

    fun doGetStep3Details(request: GetListingDetailsStep3Query): Single<Response<GetListingDetailsStep3Query.Data>>

    fun doRemoveListPhotos(request: RemoveListPhotosMutation): Single<Response<RemoveListPhotosMutation.Data>>

    fun doManagePublishStatus(request: ManagePublishStatusMutation): Single<Response<ManagePublishStatusMutation.Data>>

    fun doRemoveListingMutation(request: RemoveListingMutation): Single<Response<RemoveListingMutation.Data>>

    fun doRemoveMultiPhotosMutation(request: RemoveMultiPhotosMutation): Single<Response<RemoveMultiPhotosMutation.Data>>

    fun doGetStep2ListDetailsQuery(request: Step2ListDetailsQuery): Single<Response<Step2ListDetailsQuery.Data>>


    //Payout Preferences
    fun getPayouts(request: GetPayoutsQuery): Single<Response<GetPayoutsQuery.Data>>

    fun getPayoutsMethod(request: GetPaymentMethodsQuery): Single<Response<GetPaymentMethodsQuery.Data>>

    fun setDefaultPayout(request: SetDefaultPayoutMutation): Single<Response<SetDefaultPayoutMutation.Data>>

    fun addPayout(request: AddPayoutMutation): Single<Response<AddPayoutMutation.Data>>

    fun setPayout(request: ConfirmPayoutMutation): Single<Response<ConfirmPayoutMutation.Data>>

    fun confirmPayout(request: VerifyPayoutMutation): Single<Response<VerifyPayoutMutation.Data>>



    //ManageListings
    fun getManageListings(request: ManageListingsQuery): Single<Response<ManageListingsQuery.Data>>

    fun getListBlockedDates(request: ListBlockedDatesQuery): Single<Response<ListBlockedDatesQuery.Data>>

    fun getUpdateListBlockedDates(request: UpdateListBlockedDatesMutation): Single<Response<UpdateListBlockedDatesMutation.Data>>

    fun getListSpecialBlockedDates(request: GetListingSpecialPriceQuery): Single<Response<GetListingSpecialPriceQuery.Data>>

    fun getUpdateSpecialListBlockedDates(request: UpdateSpecialPriceMutation): Single<Response<UpdateSpecialPriceMutation.Data>>

    //Approve Decline Reservation by Host
    fun getReseravtionStatus(request: ReservationStatusMutation): Single<Response<ReservationStatusMutation.Data>>

    fun submitForVerification(request: SubmitForVerificationMutation): Single<Response<SubmitForVerificationMutation.Data>>

    //Send Feedback
    fun sendfeedBack(request: SendUserFeedbackMutation):Single<Response<SendUserFeedbackMutation.Data>>

    fun confirmPayPalPayment(request: ConfirmPayPalExecuteMutation): Single<Response<ConfirmPayPalExecuteMutation.Data>>

}
