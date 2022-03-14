package com.rentall.radicalstart.data


import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.api.Response
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.firebase.iid.FirebaseInstanceId
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.local.db.DbHelper
import com.rentall.radicalstart.data.local.prefs.PreferencesHelper
import com.rentall.radicalstart.data.model.db.DefaultListing
import com.rentall.radicalstart.data.model.db.Message
import com.rentall.radicalstart.data.remote.ApiHelper
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.util.Event
import com.rentall.radicalstart.vo.Outcome
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.reactivex.Observable
import io.reactivex.Single
import org.jetbrains.annotations.Nullable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataManager @Inject constructor(
        private val mPreferencesHelper: PreferencesHelper,
        private val mDbHelper: DbHelper,
        private val mApiHelper: ApiHelper,
        private val firebaseInstanceId: FirebaseInstanceId) : DataManager {


    override fun getListSpecialBlockedDates(request: GetListingSpecialPriceQuery): Single<Response<GetListingSpecialPriceQuery.Data>> {
        return mApiHelper.getListSpecialBlockedDates(request)
    }

    override fun getUpdateSpecialListBlockedDates(request: UpdateSpecialPriceMutation): Single<Response<UpdateSpecialPriceMutation.Data>> {
        return mApiHelper.getUpdateSpecialListBlockedDates(request)
    }

    override fun getWishList(query: GetWishListGroupQuery): Single<Response<GetWishListGroupQuery.Data>> {
        return mApiHelper.getWishList(query)
    }

    override fun getSearchListing(request: SearchListingQuery): Single<Response<SearchListingQuery.Data>> {
        return mApiHelper.getSearchListing(request)
    }

    override fun doGetStep2ListDetailsQuery(request: Step2ListDetailsQuery): Single<Response<Step2ListDetailsQuery.Data>> {
        return mApiHelper.doGetStep2ListDetailsQuery(request)
    }

    override fun getPayoutsMethod(request: GetPaymentMethodsQuery): Single<Response<GetPaymentMethodsQuery.Data>> {
        return mApiHelper.getPayoutsMethod(request)
    }

    override fun setDefaultPayout(request: SetDefaultPayoutMutation): Single<Response<SetDefaultPayoutMutation.Data>> {
        return mApiHelper.setDefaultPayout(request)
    }

    override fun confirmReservation(request: ConfirmReservationMutation): Single<Response<ConfirmReservationMutation.Data>> {
        return mApiHelper.confirmReservation(request)
    }

    override fun getPayouts(request: GetPayoutsQuery): Single<Response<GetPayoutsQuery.Data>> {
        return mApiHelper.getPayouts(request)
    }

    override fun addPayout(request: AddPayoutMutation): Single<Response<AddPayoutMutation.Data>> {
        return mApiHelper.addPayout(request)
    }

    override fun setPayout(request: ConfirmPayoutMutation): Single<Response<ConfirmPayoutMutation.Data>> {
        return mApiHelper.setPayout(request)
    }

    override fun confirmPayout(request: VerifyPayoutMutation): Single<Response<VerifyPayoutMutation.Data>> {
        return mApiHelper.confirmPayout(request)
    }

    override fun insertDefaultListing(defaultListing: DefaultListing): Observable<Boolean> {
        return mDbHelper.insertDefaultListing(defaultListing)
    }

    override fun listOfInboxMsg1(query: GetThreadsQuery): Single<Response<GetThreadsQuery.Data>> {
        return mApiHelper.listOfInboxMsg1(query)
    }

    override fun deleteMessage(): Observable<Boolean> {
        return mDbHelper.deleteMessage()
    }

    override fun loadAllMessage(): DataSource.Factory<Int, Message> {
        return mDbHelper.loadAllMessage()
    }

    override fun listOfWishListWithoutPage(request: GetAllWishListGroupWithoutPageQuery): Single<Response<GetAllWishListGroupWithoutPageQuery.Data>> {
        return mApiHelper.listOfWishListWithoutPage(request)
    }

    override fun listOfWishListGroup(query: GetAllWishListGroupQuery.Builder, pageSize: Int): Listing<GetAllWishListGroupQuery.Result> {
        return mApiHelper.listOfWishListGroup(query, pageSize)
    }

    override fun listOfWishList(query: GetWishListGroupQuery.Builder, pageSize: Int): Listing<GetWishListGroupQuery.WishList> {
        return mApiHelper.listOfWishList(query, pageSize)
    }

    override fun createWishListGroup(request: CreateWishListGroupMutation): Single<Response<CreateWishListGroupMutation.Data>> {
        return mApiHelper.createWishListGroup(request)
    }

    override fun CreateWishList(request: CreateWishListMutation): Single<Response<CreateWishListMutation.Data>> {
        return mApiHelper.CreateWishList(request)
    }

    override fun writeReview(mutate: WriteUserReviewMutation): Single<Response<WriteUserReviewMutation.Data>> {
        return mApiHelper.writeReview(mutate)
    }

    override fun deleteWishListGroup(request: DeleteWishListGroupMutation): Single<Response<DeleteWishListGroupMutation.Data>> {
        return mApiHelper.deleteWishListGroup(request)
    }

    override fun updateWishListGroup(request: UpdateWishListGroupMutation): Single<Response<UpdateWishListGroupMutation.Data>> {
        return mApiHelper.updateWishListGroup(request)
    }

    override fun getAllWishListGroup(request: GetAllWishListGroupQuery): Single<Response<GetAllWishListGroupQuery.Data>> {
        return mApiHelper.getAllWishListGroup(request)
    }

    override fun getWishListGroup(request: GetWishListGroupQuery): Observable<Response<GetWishListGroupQuery.Data>> {
        return mApiHelper.getWishListGroup(request)
    }


    override var siteName: String?
        get() = mPreferencesHelper.siteName
        set(siteName) {
            mPreferencesHelper.siteName = siteName
        }
    override var listingApproval: Int
        get() = mPreferencesHelper.listingApproval
        set(value) {
            mPreferencesHelper.listingApproval = value
        }

    override fun getNewMessage(request: GetUnReadThreadCountQuery): Observable<Response<GetUnReadThreadCountQuery.Data>> {
        return mApiHelper.getNewMessage(request)
    }

    override fun contactSupport(request: ContactSupportQuery): Single<Response<ContactSupportQuery.Data>> {
        return mApiHelper.contactSupport(request)
    }

    override fun isUserLoggedIn(): Boolean {
        return currentUserLoggedInMode != DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.type
    }

    override fun getUserBanStatus(request: UserBanStatusQuery): Observable<Response<UserBanStatusQuery.Data>> {
        return mApiHelper.getUserBanStatus(request)
    }

    override fun getCancellationDetails(request: CancellationDataQuery): Single<Response<CancellationDataQuery.Data>> {
        return mApiHelper.getCancellationDetails(request)
    }

    override fun cancelReservation(request: CancelReservationMutation): Single<Response<CancelReservationMutation.Data>> {
        return mApiHelper.cancelReservation(request)
    }

    override fun listOfUserReview(builder: UserReviewsQuery.Builder, pageSize: Int): Listing<UserReviewsQuery.Result> {
        return mApiHelper.listOfUserReview(builder, pageSize)
    }

    override fun createReportUser(request: CreateReportUserMutation): Single<Response<CreateReportUserMutation.Data>> {
        return mApiHelper.createReportUser(request)
    }

    override fun getUserProfile(request: ShowUserProfileQuery): Single<Response<ShowUserProfileQuery.Data>> {
        return mApiHelper.getUserProfile(request)
    }

    override fun contactHost(request: ContactHostMutation): Single<Response<ContactHostMutation.Data>> {
        return mApiHelper.contactHost(request)
    }

    override fun setReadMessage(request: ReadMessageMutation): Single<Response<ReadMessageMutation.Data>> {
        return mApiHelper.setReadMessage(request)
    }

    override fun createToken(card: Card): MutableLiveData<Outcome<Token>> {
        return mApiHelper.createToken(card)
    }

    override fun createReservation(request: CreateReservationMutation): Single<Response<CreateReservationMutation.Data>> {
        return mApiHelper.createReservation(request)
    }

    override fun getUnreadCount(query: GetUnReadCountQuery): Observable<Response<GetUnReadCountQuery.Data>> {
        return mApiHelper.getUnreadCount(query)
    }

    override fun sendMessage(mutate: SendMessageMutation): Single<Response<SendMessageMutation.Data>> {
        return mApiHelper.sendMessage(mutate)
    }

    override fun listOfInboxMsg(query: GetThreadsQuery.Builder, pageSize: Int): Listing<GetThreadsQuery.ThreadItem> {
        return mApiHelper.listOfInboxMsg(query, pageSize)
    }

    override fun listOfInbox(query: GetAllThreadsQuery.Builder, pageSize: Int): Listing<GetAllThreadsQuery.Result> {
        return mApiHelper.listOfInbox(query, pageSize)
    }

    override fun listOfTripsList(query: GetAllReservationQuery.Builder, pageSize: Int): Listing<GetAllReservationQuery.Result> {
        return mApiHelper.listOfTripsList(query, pageSize)
    }

    override fun getUserReviews(query: GetUserReviewsQuery.Builder, pageSize: Int): Listing<GetUserReviewsQuery.Result> {
        return mApiHelper.getUserReviews(query, pageSize)
    }

    override fun getPendingUserReviews(query: GetPendingUserReviewsQuery.Builder, pageSize: Int): Listing<GetPendingUserReviewsQuery.Result> {
        return mApiHelper.getPendingUserReviews(query, pageSize)
    }

    override fun getPendingUserReview(request: GetPendingUserReviewQuery): Single<Response<GetPendingUserReviewQuery.Data>> {
        return mApiHelper.getPendingUserReview(request)
    }

    override fun getTripsDetails(request: GetAllReservationQuery): Single<Response<GetAllReservationQuery.Data>> {
        return mApiHelper.getTripsDetails(request)
    }

/*    override fun setCurrencyRates(result: List<GetCurrencyRateQuery.Rate>): Observable<Boolean> {
        result.forEach {
            insertCurrencyRate(CurrencyRates(it.currencyCode()!!, it.rate()!!))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{ t -> Log.d("dd", t.toString()) }
        }
        return Observable.just(true)
    }

    override fun getCurrencyRates(request: GetCurrencyRateQuery): Observable<MutableList<GetCurrencyRateQuery.Rate>> {
        return mApiHelper.getCurrencyRates(request)
    }*/

/*    override fun insertCurrencyRate(currencyRates: CurrencyRates): Observable<Boolean> {
        return mDbHelper.insertCurrencyRate(currencyRates)
    }

    override fun insertCurrencyRatesList(currencyRatesList: List<CurrencyRates>): Observable<Boolean> {
        return mDbHelper.insertCurrencyRatesList(currencyRatesList)
    }

    override fun isCurrencyRatesListEmpty(): Observable<Boolean> {
        return mDbHelper.isCurrencyRatesListEmpty()
    }

    override fun loadAllCurrencyRates(): Observable<List<CurrencyRates>> {
        return mDbHelper.loadAllCurrencyRates()
    }

    override fun loadCurrencyRatesByCode(currencyCode: String): Observable<CurrencyRates> {
        return mDbHelper.loadCurrencyRatesByCode(currencyCode)
    }*/

    override fun clearHttpCache(): Observable<Boolean> {
        return mApiHelper.clearHttpCache()
    }

    override fun getLocationAutoComplete(location: String): Observable<List<AutocompletePrediction>?> {
        return mApiHelper.getLocationAutoComplete(location)
    }

   /* override fun loadAllFiltersList(): Observable<List<FilterSubList>> {
        return mDbHelper.loadAllFiltersList()
    }
*/
   /* override fun setFilters(result: List<ListingSettingsCommonQuery.Result>): Observable<Boolean> {
        result.forEach {
            insertFilter(Filter(it.id()!!, it.typeLabel()!!, it.typeName()!!))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{ t -> }
            it.listSettings()?.forEach { item ->
                insertFilterList(FilterSubList(item.id()!!, item.itemName()!!, item.typeId()!!, item.otherItemName(), item.startValue(), item.endValue(), item.maximum(), item.minimum()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{ t -> }
            }
        }
        return Observable.just(true)
    }*/

    /*override fun loadAllFilters(): Observable<List<Filter>> {
        return mDbHelper.loadAllFilters()
    }

    override fun loadFilterListsForFilterId(filterId: Int): Observable<List<FilterSubList>> {
        return mDbHelper.loadFilterListsForFilterId(filterId)
    }

    override fun isFilterListEmpty(): Observable<Boolean> {
        return mDbHelper.isFilterListEmpty()
    }

    override fun isFilterEmpty(): Observable<Boolean> {
        return mDbHelper.isFilterEmpty()
    }

    override fun insertFilterList(filterSubList: FilterSubList): Observable<Boolean> {
        return mDbHelper.insertFilterList(filterSubList)
    }

    override fun insertFilterSubList(filterSubList: List<FilterSubList>): Observable<Boolean> {
        return mDbHelper.insertFilterSubList(filterSubList)
    }

    override fun insertFilter(filter: Filter): Observable<Boolean> {
        return mDbHelper.insertFilter(filter)
    }

    override fun insertFilterList(filterList: List<Filter>): Observable<Boolean> {
        return mDbHelper.insertFilterList(filterList)
    }*/

//    override fun insertDefaultListing(defaultListing: DefaultListing): Observable<Boolean> {
//        return mDbHelper.insertDefaultListing(defaultListing)
//    }
//
//    override fun insertDefaultListingList(defaultListing: List<DefaultListing>): Observable<Boolean> {
//        return mDbHelper.insertDefaultListingList(defaultListing)
//    }
//
//    override fun loadAllDefaultListing(listingType: String): Observable<List<DefaultListing>> {
//        return mDbHelper.loadAllDefaultListing(listingType)
//    }

    override var currentUserFirstName: String?
        get() = mPreferencesHelper.currentUserFirstName
        set(firstName) {
            mPreferencesHelper.currentUserFirstName = firstName
        }

    override var currentUserLastName: String?
       get() = mPreferencesHelper.currentUserLastName
        set(lastName) {
            mPreferencesHelper.currentUserLastName = lastName
        }

    override var isUserFromDeepLink: Boolean
        get() = mPreferencesHelper.isUserFromDeepLink
        set(isUserFromDeepLink) {
            mPreferencesHelper.isUserFromDeepLink = isUserFromDeepLink
        }

    override var firebaseToken: String?
        get() = mPreferencesHelper.firebaseToken
        set(firebaseToken) {
            mPreferencesHelper.firebaseToken = firebaseToken
        }

    override var accessToken: String?
        get() = mPreferencesHelper.accessToken
        set(accessToken) {
            mPreferencesHelper.accessToken = accessToken
        }

    override var currentUserEmail: String?
        get() = mPreferencesHelper.currentUserEmail
        set(email) {
            mPreferencesHelper.currentUserEmail = email
        }

    override var currentUserId: String?
        get() = mPreferencesHelper.currentUserId
        set(userId) {
            mPreferencesHelper.currentUserId = userId
        }

    override val currentUserLoggedInMode: Int
        get() = mPreferencesHelper.currentUserLoggedInMode

    override var currentUserName: String?
        get() = mPreferencesHelper.currentUserName
        set(userName) {
            mPreferencesHelper.currentUserName = userName
        }

    override var currentUserProfilePicUrl: String?
        get() = mPreferencesHelper.currentUserProfilePicUrl
        set(profilePicUrl) {
            mPreferencesHelper.currentUserProfilePicUrl = profilePicUrl
        }

    override var currentUserCurrency: String?
        get() = mPreferencesHelper.currentUserCurrency
        set(currency) {
            mPreferencesHelper.currentUserCurrency = currency
        }

    override var currencyBase: String?
        get() = mPreferencesHelper.currencyBase
        set(currencyBase) {
            mPreferencesHelper.currencyBase = currencyBase
        }

    override var currencyRates: String?
        get() = mPreferencesHelper.currencyRates
        set(currencyRates) {
            mPreferencesHelper.currencyRates = currencyRates
        }

    override var isDOB: Boolean?
        get() = mPreferencesHelper.isDOB//To change initializer of created properties use File | Settings | File Templates.
        set(isDOB) {
            mPreferencesHelper.isDOB = isDOB
        }

    override var isPhoneVerified: Boolean?
        get() = mPreferencesHelper.isPhoneVerified
        set(value) {
            mPreferencesHelper.isPhoneVerified = value
        }

    override var isIdVerified: Boolean?
        get() = mPreferencesHelper.isIdVerified
        set(value) {
            mPreferencesHelper.isIdVerified = value
        }

    override var isEmailVerified: Boolean?
        get() = mPreferencesHelper.isEmailVerified
        set(value) {
            mPreferencesHelper.isEmailVerified = value
        }

    override var isFBVerified: Boolean?
        get() = mPreferencesHelper.isFBVerified
        set(value) {
            mPreferencesHelper.isFBVerified = value
        }

    override var isGoogleVerified: Boolean?
        get() = mPreferencesHelper.isGoogleVerified
        set(value) {
            mPreferencesHelper.isGoogleVerified = value
        }

    override var haveNotification: Boolean
        get() = mPreferencesHelper.haveNotification
        set(value) {
            mPreferencesHelper.haveNotification = value
        }

    override var confirmCode: String?
        get() = mPreferencesHelper.confirmCode
        set(value) {
            mPreferencesHelper.confirmCode = value
        }

    override var currentUserPhoneNo: String?
        get() = mPreferencesHelper.currentUserPhoneNo
        set(value) {
            mPreferencesHelper.currentUserPhoneNo = value
        }

    override var currentUserType: String?
        get() = mPreferencesHelper.currentUserType
        set(value) {
            mPreferencesHelper.currentUserType = value
        }

    override var isListAdded: Boolean?
        get() = mPreferencesHelper.isListAdded
        set(value) {
            mPreferencesHelper.isListAdded = value
        }

    override var isHostOrGuest: Boolean
        get() = mPreferencesHelper.isHostOrGuest
        set(value) {
            mPreferencesHelper.isHostOrGuest = value
        }

    override var adminCurrency: String?
        get() = mPreferencesHelper.adminCurrency
        set(value) {
            mPreferencesHelper.adminCurrency = value
        }

    override fun dogetListingSettings(request: GetListingSettingQuery): Single<Response<GetListingSettingQuery.Data>> {
        return mApiHelper.dogetListingSettings(request)
    }

    override fun getPref(): SharedPreferences {
        return mPreferencesHelper.getPref()
    }

    override fun doServerLoginApiCall(request: LoginQuery): Single<Response<LoginQuery.Data>> {
        return mApiHelper.doServerLoginApiCall(request)
    }

    override fun doLogoutApiCall(request: LogoutMutation): Single<Response<LogoutMutation.Data>> {
        return mApiHelper.doLogoutApiCall(request)
    }

    override fun doEmailVerificationApiCall(request: CheckEmailExistsQuery): Single<Response<CheckEmailExistsQuery.Data>> {
        return mApiHelper.doEmailVerificationApiCall(request)
    }

    override fun setCurrentUserLoggedInMode(mode: DataManager.LoggedInMode) {
        mPreferencesHelper.setCurrentUserLoggedInMode(mode)
    }

    override fun doGetProfileDetailsApiCall(request: GetProfileQuery): Single<Response<GetProfileQuery.Data>> {
        return mApiHelper.doGetProfileDetailsApiCall(request)
    }

    override fun getPropertyReviews(query: GetPropertyReviewsQuery): Single<Response<GetPropertyReviewsQuery.Data>> {
        return mApiHelper.getPropertyReviews(query)
    }

    override fun doEditProfileApiCall(request: EditProfileMutation): Single<Response<EditProfileMutation.Data>> {
        return mApiHelper.doEditProfileApiCall(request)
    }

    override fun doSignupApiCall(request: SignupMutation): Single<Response<SignupMutation.Data>> {
        return mApiHelper.doSignupApiCall(request)
    }

    override fun doForgotPasswordApiCall(request: ForgotPasswordMutation): Single<Response<ForgotPasswordMutation.Data>> {
        return mApiHelper.doForgotPasswordApiCall(request)
    }

    override fun doForgotPasswordVerificationApiCall(request: ForgotPasswordVerificationQuery): Single<Response<ForgotPasswordVerificationQuery.Data>> {
        return mApiHelper.doForgotPasswordVerificationApiCall(request)
    }

    override fun doResetPasswordApiCall(request: ResetPasswordMutation): Single<Response<ResetPasswordMutation.Data>> {
        return mApiHelper.doResetPasswordApiCall(request)
    }

    override fun doSocailLoginApiCall(request: SocialLoginQuery): Single<Response<SocialLoginQuery.Data>> {
        return mApiHelper.doSocailLoginApiCall(request)
    }

    override fun doGetLanguagesApiCall(request: UserPreferredLanguagesQuery): Single<Response<UserPreferredLanguagesQuery.Data>> {
        return mApiHelper.doGetLanguagesApiCall(request)
    }

    override fun doGetDefaultSettingApiCall(request: GetDefaultSettingQuery): Single<Response<GetDefaultSettingQuery.Data>> {
        return mApiHelper.doGetDefaultSettingApiCall(request)
    }

    override fun dogetPaymentSettingsApiCall(request: GetPaymentSettingsQuery): Single<Response<GetPaymentSettingsQuery.Data>> {
        return mApiHelper.dogetPaymentSettingsApiCall(request)
    }

    override fun doSimilarListingApiCall(request: GetSimilarListingQuery): Single<Response<GetSimilarListingQuery.Data>> {
        return mApiHelper.doSimilarListingApiCall(request)
    }

    override fun doListingDetailsApiCall(request: ViewListingDetailsQuery): Single<Response<ViewListingDetailsQuery.Data>> {
        return mApiHelper.doListingDetailsApiCall(request)
    }

    override fun listOfReview(listId: Int, hostId: String, pageSize: Int): Listing<GetPropertyReviewsQuery.Result> {
        return mApiHelper.listOfReview(listId, hostId, pageSize)
    }

    /*override fun getSearchListing(request: SearchListingQuery): Single<Response<SearchListingQuery.Data>> {
        return mApiHelper.getSearchListing(request)
    }*/

    override fun listOfSearchListing(query: SearchListingQuery.Builder, pageSize: Int): Listing<SearchListingQuery.Result> {
        return mApiHelper.listOfSearchListing(query, pageSize)
    }

    override fun getCurrencyList(request: GetCurrenciesListQuery): Single<Response<GetCurrenciesListQuery.Data>> {
        return mApiHelper.getCurrencyList(request)
    }

    override fun getBillingCalculation(request: GetBillingCalculationQuery): Single<Response<GetBillingCalculationQuery.Data>> {
        return mApiHelper.getBillingCalculation(request)
    }

    override fun getReservationDetails(request: GetReservationQuery): Single<Response<GetReservationQuery.Data>> {
        return mApiHelper.getReservationDetails(request)
    }

    override fun sendConfirmationEmail(request: SendConfirmEmailQuery): Single<Response<SendConfirmEmailQuery.Data>> {
        return mApiHelper.sendConfirmationEmail(request)
    }

    override fun ConfirmCodeApiCall(request: CodeVerificationMutation): Single<Response<CodeVerificationMutation.Data>> {
        return mApiHelper.ConfirmCodeApiCall(request)
    }

    override fun SocialLoginVerify(request: SocialLoginVerifyMutation): Single<Response<SocialLoginVerifyMutation.Data>> {
        return mApiHelper.SocialLoginVerify(request)
    }

    override fun generateFirebaseToken(): MutableLiveData<Event<Outcome<String>>> {
        val response = MutableLiveData<Event<Outcome<String>>>()
        response.value = Event(Outcome.loading(true))
        firebaseInstanceId.instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.tag("FirebaseToken").d(task.result?.token)
                response.value = Event(Outcome.loading(false))
                response.value = Event(Outcome.success(task.result?.token!!))
                mPreferencesHelper.firebaseToken = task.result?.token!!
            }
        }
        firebaseInstanceId.instanceId.addOnFailureListener { exception: Exception ->
            Timber.tag("firebaseToken - Failure").d(exception)
            mPreferencesHelper.firebaseToken = null
            response.value = Event(Outcome.loading(false))
            response.value = Event(Outcome.error(exception))
        }
        firebaseInstanceId.instanceId.addOnCanceledListener {
            Timber.tag("firebasetoken").d("canceled")
        }
        return response
    }

    override fun setUserAsLoggedOut() {
        updateUserInfo(null,
                null,
                DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT,
                null,
                null,
                null,
                null,
                null,
                null
        )
        clearPrefs()
    }

    override fun updateUserInfo (
            accezzToken: String?,
            userId: String?,
            loggedInMode: DataManager.LoggedInMode,
            userName: String?,
            email: String?,
            profilePicPath: String?,
            currency: String?,
            language: String?,
            createdAt: String?
    ) {
        accessToken = accezzToken
        currentUserId = userId
        currentUserName = userName
        currentUserEmail = email
        currentUserProfilePicUrl = profilePicPath
        currentUserCurrency = currency
        currentUserCreatedAt = createdAt
        setCurrentUserLoggedInMode(loggedInMode)
    }

    override fun updateAccessToken(accezzToken: String?) {
        accessToken = accezzToken
    }

    override fun updateVerification(
            isPhoneVerification: @Nullable Boolean?,
            isEmailConfirmed: @Nullable Boolean?,
            isIdVerification: @Nullable Boolean?,
            isGoogleConnected: @Nullable Boolean?,
            isFacebookConnected: @Nullable Boolean?) {
        isEmailVerified = isEmailConfirmed
        isFBVerified = isFacebookConnected
        isGoogleVerified = isGoogleConnected
        isIdVerified = isIdVerification
        isPhoneVerified = isPhoneVerification
    }

    override fun clearPrefs() {
        mPreferencesHelper.clearPrefs()
    }

    override var currentUserLanguage: String?
        get() = mPreferencesHelper.currentUserLanguage
        set(language) {
            mPreferencesHelper.currentUserLanguage = language
        }

    override var currentUserCreatedAt: String?
        get() = mPreferencesHelper.currentUserCreatedAt
        set(createdAt) {
            mPreferencesHelper.currentUserCreatedAt = createdAt
        }

    override fun getPhoneNumber(request: GetEnteredPhoneNoQuery): Single<Response<GetEnteredPhoneNoQuery.Data>> {
        return mApiHelper.getPhoneNumber(request)
    }

    override fun getCountryCode(request: GetCountrycodeQuery): Single<Response<GetCountrycodeQuery.Data>> {
        return mApiHelper.getCountryCode(request)
    }

    override fun addPhoneNumber(request: AddPhoneNumberMutation): Single<Response<AddPhoneNumberMutation.Data>> {
        return mApiHelper.addPhoneNumber(request)
    }

    override fun verifyPhoneNumber(request: VerifyPhoneNumberMutation): Single<Response<VerifyPhoneNumberMutation.Data>> {
        return mApiHelper.verifyPhoneNumber(request)
    }

    override fun getExploreListing(request: GetExploreListingsQuery): Single<Response<GetExploreListingsQuery.Data>> {
        return mApiHelper.getExploreListing(request)
    }

    override fun doUpdateListingStep2(request: UpdateListingStep2Mutation): Single<Response<UpdateListingStep2Mutation.Data>> {
        return mApiHelper.doUpdateListingStep2(request)
    }

    override fun doUpdateListingStep3(request: UpdateListingStep3Mutation): Single<Response<UpdateListingStep3Mutation.Data>> {
        return mApiHelper.doUpdateListingStep3(request)
    }

    override fun ShowListPhotosQuery(request: ShowListPhotosQuery): Single<Response<ShowListPhotosQuery.Data>> {
        return mApiHelper.ShowListPhotosQuery(request)
    }

    override fun doGetListingSettings(request: GetListingSettingQuery): Single<Response<GetListingSettingQuery.Data>> {
        return mApiHelper.doGetListingSettings(request)
    }

    override fun doCreateListing(request: CreateListingMutation): Single<Response<CreateListingMutation.Data>> {
        return mApiHelper.doCreateListing(request)
    }

    override fun doGetListingDetailsStep2Query(request: GetListingDetailsStep2Query): Single<Response<GetListingDetailsStep2Query.Data>> {
        return mApiHelper.doGetListingDetailsStep2Query(request)
    }

    override fun doManageListingSteps(request: ManageListingStepsMutation): Single<Response<ManageListingStepsMutation.Data>> {
        return mApiHelper.doManageListingSteps(request)
    }

    override fun doShowListingSteps(request: ShowListingStepsQuery): Single<Response<ShowListingStepsQuery.Data>> {
        return mApiHelper.doShowListingSteps(request)
    }
    override fun doGetStep1ListingDetailsQuery(request: GetStep1ListingDetailsQuery): Single<Response<GetStep1ListingDetailsQuery.Data>> {
        return mApiHelper.doGetStep1ListingDetailsQuery(request)
    }

    override fun doGetStep3Details(request: GetListingDetailsStep3Query): Single<Response<GetListingDetailsStep3Query.Data>> {
        return mApiHelper.doGetStep3Details(request)
    }

    override fun doRemoveListPhotos(request: RemoveListPhotosMutation): Single<Response<RemoveListPhotosMutation.Data>> {
        return mApiHelper.doRemoveListPhotos(request)
    }

    override fun getReseravtionStatus(request: ReservationStatusMutation): Single<Response<ReservationStatusMutation.Data>> {
        return mApiHelper.getReseravtionStatus(request)
    }

    override fun submitForVerification(request: SubmitForVerificationMutation): Single<Response<SubmitForVerificationMutation.Data>> {
      return  mApiHelper.submitForVerification(request)
    }

    override fun getListBlockedDates(request: ListBlockedDatesQuery): Single<Response<ListBlockedDatesQuery.Data>> {
        return mApiHelper.getListBlockedDates(request)
    }

    override fun getUpdateListBlockedDates(request: UpdateListBlockedDatesMutation): Single<Response<UpdateListBlockedDatesMutation.Data>> {
        return mApiHelper.getUpdateListBlockedDates(request)
    }

    override fun getManageListings(request: ManageListingsQuery): Single<Response<ManageListingsQuery.Data>> {
        return mApiHelper.getManageListings(request)
    }

    override fun doRemoveListingMutation(request: RemoveListingMutation): Single<Response<RemoveListingMutation.Data>> {
        return mApiHelper.doRemoveListingMutation(request)
    }

    override fun doRemoveMultiPhotosMutation(request: RemoveMultiPhotosMutation): Single<Response<RemoveMultiPhotosMutation.Data>> {
        return mApiHelper.doRemoveMultiPhotosMutation(request)
    }

    override fun doManagePublishStatus(request: ManagePublishStatusMutation): Single<Response<ManagePublishStatusMutation.Data>> {
        return mApiHelper.doManagePublishStatus(request)
    }

    override fun sendfeedBack(request: SendUserFeedbackMutation): Single<Response<SendUserFeedbackMutation.Data>> {
        return mApiHelper.sendfeedBack(request)
    }

    override fun confirmPayPalPayment(request: ConfirmPayPalExecuteMutation): Single<Response<ConfirmPayPalExecuteMutation.Data>> {
        return mApiHelper.confirmPayPalPayment(request)
    }
}
