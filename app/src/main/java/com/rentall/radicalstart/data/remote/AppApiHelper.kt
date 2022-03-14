package com.rentall.radicalstart.data.remote

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.rx2.Rx2Apollo
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.local.db.AppDatabase
import com.rentall.radicalstart.data.model.db.Message
import com.rentall.radicalstart.data.remote.paging.Listing
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.inbox.InboxListDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.inbox_msg.InboxMsgDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.listing_review.ReviewDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.reviews.PendingReviewsListDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.reviews.ReviewsListDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.search_listing.SearchListingDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.trips.TripsDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.user_review.UserReviewDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.wishlist.WishListDataSourceFactory
import com.rentall.radicalstart.data.remote.paging.wishlistgroup.WishListGroupDataSourceFactory
import com.rentall.radicalstart.util.performAsynReturnSingle
import com.rentall.radicalstart.util.rx.Scheduler
import com.rentall.radicalstart.vo.Outcome
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppApiHelper @Inject constructor(
        @param:Named("Interceptor") val apolloClient: ApolloClient,
        @param:Named("NoInterceptor") val apolloClientNoInterceptor: ApolloClient,
        val scheduler: Scheduler,
        val application: Application,
        val appDatabase: AppDatabase
) : ApiHelper {


    override fun getListSpecialBlockedDates(request: GetListingSpecialPriceQuery): Single<Response<GetListingSpecialPriceQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getUpdateSpecialListBlockedDates(request: UpdateSpecialPriceMutation): Single<Response<UpdateSpecialPriceMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getWishList(query: GetWishListGroupQuery): Single<Response<GetWishListGroupQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(query))
                .performAsynReturnSingle(scheduler)
    }

    override fun doGetStep2ListDetailsQuery(request: Step2ListDetailsQuery): Single<Response<Step2ListDetailsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }


    override fun listOfInboxMsg1(query: GetThreadsQuery): Single<Response<GetThreadsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(query))
                .performAsynReturnSingle(scheduler)
    }

    override fun dogetPaymentSettingsApiCall(request: GetPaymentSettingsQuery): Single<Response<GetPaymentSettingsQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request)
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .performAsynReturnSingle(scheduler)
    }

    // Default Settings
    override fun doGetDefaultSettingApiCall(request: GetDefaultSettingQuery): Single<Response<GetDefaultSettingQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request)
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .performAsynReturnSingle(scheduler)
    }


    // Auth
    override fun doServerLoginApiCall(request: LoginQuery): Single<Response<LoginQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doLogoutApiCall(request: LogoutMutation): Single<Response<LogoutMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doEmailVerificationApiCall(request: CheckEmailExistsQuery): Single<Response<CheckEmailExistsQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doSignupApiCall(request: SignupMutation): Single<Response<SignupMutation.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doForgotPasswordApiCall(request: ForgotPasswordMutation): Single<Response<ForgotPasswordMutation.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doForgotPasswordVerificationApiCall(request: ForgotPasswordVerificationQuery): Single<Response<ForgotPasswordVerificationQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doResetPasswordApiCall(request: ResetPasswordMutation): Single<Response<ResetPasswordMutation.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doSocailLoginApiCall(request: SocialLoginQuery): Single<Response<SocialLoginQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request))
                .performAsynReturnSingle(scheduler)
    }



    //Explore
    override fun getExploreListing(request: GetExploreListingsQuery): Single<Response<GetExploreListingsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getLocationAutoComplete(location: String): Observable<List<AutocompletePrediction>?> {
        return Observable.fromCallable {
            getAutocompleteSearchLocation(location)
        }
    }

    private fun getAutocompleteSearchLocation(constraint: CharSequence): List<AutocompletePrediction>? {
        Places.initialize(application.applicationContext, Constants.googleMapKey)
        val placesClient = Places.createClient(application.applicationContext)
        val token = AutocompleteSessionToken.newInstance()
        val autocompleteFilter = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setQuery(constraint.toString())
                .setSessionToken(token)
                .build()

        var autocompletePredictions: List<AutocompletePrediction>? = null
        return try {
            placesClient.findAutocompletePredictions(autocompleteFilter).addOnSuccessListener(OnSuccessListener {
                autocompletePredictions = it.autocompletePredictions
                Timber.tag("AutoComplete Size").w("AutoComplete Size=%s", autocompletePredictions!!.size)
            }).addOnFailureListener(OnFailureListener {
                it.printStackTrace()
            })
            autocompletePredictions
        }catch (e: RuntimeExecutionException) {
            Timber.tag("search").e(e, "Error getting autocomplete prediction API call")
            return null
        }


    }


    // Profile
    override fun doGetProfileDetailsApiCall(request: GetProfileQuery): Single<Response<GetProfileQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }



    //CurrencyList
    override fun getCurrencyList(request: GetCurrenciesListQuery): Single<Response<GetCurrenciesListQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }



    //Languages
    override fun doGetLanguagesApiCall(request: UserPreferredLanguagesQuery): Single<Response<UserPreferredLanguagesQuery.Data>> {
        return Rx2Apollo.from(apolloClientNoInterceptor.query(request))
                .performAsynReturnSingle(scheduler)
    }



    //EditProfile
    override fun doEditProfileApiCall(request: EditProfileMutation): Single<Response<EditProfileMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun contactSupport(request: ContactSupportQuery): Single<Response<ContactSupportQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getUserBanStatus(request: UserBanStatusQuery): Observable<Response<UserBanStatusQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .subscribeOn(scheduler.io())
                .observeOn(AndroidSchedulers.mainThread())
    }



    //Cancellation
    override fun getCancellationDetails(request: CancellationDataQuery): Single<Response<CancellationDataQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun cancelReservation(request: CancelReservationMutation): Single<Response<CancelReservationMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun createReportUser(request: CreateReportUserMutation): Single<Response<CreateReportUserMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }



    // User Profile
    override fun getUserProfile(request: ShowUserProfileQuery): Single<Response<ShowUserProfileQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun listOfUserReview(builder: UserReviewsQuery.Builder, pageSize: Int): Listing<UserReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = UserReviewDataSourceFactory(apolloClient, builder, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }



    //Stripe Token
    override fun createToken(card: Card): MutableLiveData<Outcome<Token>> {
        val response = MutableLiveData<Outcome<Token>>()
        val stripe =  Stripe(application, Constants.stripePublishableKey)
        response.value = Outcome.loading(true)
        /*stripe.createToken(
                card,
                object : TokenCallback {
                    override fun onSuccess(token: Token?) {
                        Log.d("token", "fsf  " + token?.id)
                        response.value = Outcome.loading(false)
                        response.value = Outcome.success(token!!)
                    }
                    override fun onError(error: Exception?) {
                        response.value = Outcome.loading(false)
                        response.value = Outcome.error(error!!)
                    }
                }
        )*/
        return response
    }

    //Payment
    override fun createReservation(request: CreateReservationMutation): Single<Response<CreateReservationMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getUnreadCount(query: GetUnReadCountQuery): Observable<Response<GetUnReadCountQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(query))
                .subscribeOn(scheduler.io())
                .observeOn(AndroidSchedulers.mainThread())
        //.doOnNext { throw ConnectException() }
        //.performOnBackOutOnMain(scheduler)
    }

    //Trips
    override fun getTripsDetails(request: GetAllReservationQuery): Single<Response<GetAllReservationQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getReservationDetails(request: GetReservationQuery): Single<Response<GetReservationQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }


    // View Listing
    override fun doListingDetailsApiCall(request: ViewListingDetailsQuery): Single<Response<ViewListingDetailsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getBillingCalculation(request: GetBillingCalculationQuery): Single<Response<GetBillingCalculationQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doSimilarListingApiCall(request: GetSimilarListingQuery): Single<Response<GetSimilarListingQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun contactHost(request: ContactHostMutation): Single<Response<ContactHostMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getPropertyReviews(query: GetPropertyReviewsQuery): Single<Response<GetPropertyReviewsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(query))
                .performAsynReturnSingle(scheduler)
    }

    override fun listOfReview(listId: Int, hostId: String, pageSize: Int): Listing<GetPropertyReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = ReviewDataSourceFactory(apolloClient, listId, hostId, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        val count =  Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.count
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = count
        )
    }



    // Search Listing
    override fun listOfSearchListing(query: SearchListingQuery.Builder, pageSize: Int): Listing<SearchListingQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = SearchListingDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun getSearchListing(request: SearchListingQuery): Single<Response<SearchListingQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }



    //Trips
    override fun listOfTripsList(query: GetAllReservationQuery.Builder, pageSize: Int): Listing<GetAllReservationQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = TripsDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun getUserReviews(query: GetUserReviewsQuery.Builder, pageSize: Int): Listing<GetUserReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()
        val sourceFactory = ReviewsListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun getPendingUserReviews(query: GetPendingUserReviewsQuery.Builder, pageSize: Int): Listing<GetPendingUserReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()
        val sourceFactory = PendingReviewsListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }


    override fun getPendingUserReview(request: GetPendingUserReviewQuery): Single<Response<GetPendingUserReviewQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    //  WishList
    override fun listOfWishListGroup(query: GetAllWishListGroupQuery.Builder, pageSize: Int): Listing<GetAllWishListGroupQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = WishListGroupDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun listOfWishList(query: GetWishListGroupQuery.Builder, pageSize: Int): Listing<GetWishListGroupQuery.WishList> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = WishListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun listOfWishListWithoutPage(request: GetAllWishListGroupWithoutPageQuery): Single<Response<GetAllWishListGroupWithoutPageQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun createWishListGroup(request: CreateWishListGroupMutation): Single<Response<CreateWishListGroupMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun CreateWishList(request: CreateWishListMutation): Single<Response<CreateWishListMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun writeReview(mutate: WriteUserReviewMutation): Single<Response<WriteUserReviewMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(mutate))
                .performAsynReturnSingle(scheduler)
    }

    override fun deleteWishListGroup(request: DeleteWishListGroupMutation): Single<Response<DeleteWishListGroupMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun updateWishListGroup(request: UpdateWishListGroupMutation): Single<Response<UpdateWishListGroupMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getAllWishListGroup(request: GetAllWishListGroupQuery): Single<Response<GetAllWishListGroupQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getWishListGroup(request: GetWishListGroupQuery): Observable<Response<GetWishListGroupQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .subscribeOn(scheduler.io())
                .observeOn(AndroidSchedulers.mainThread())
    }



    // Inbox
    override fun sendMessage(mutate: SendMessageMutation): Single<Response<SendMessageMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(mutate))
                //  .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .performAsynReturnSingle(scheduler)
    }

    override fun confirmReservation(request: ConfirmReservationMutation): Single<Response<ConfirmReservationMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun setReadMessage(request: ReadMessageMutation): Single<Response<ReadMessageMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getNewMessage(request: GetUnReadThreadCountQuery): Observable<Response<GetUnReadThreadCountQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .subscribeOn(scheduler.io())
                .observeOn(AndroidSchedulers.mainThread())
        // .doOnNext { throw Exception() }
    }

    override fun listOfInboxMsg(query: GetThreadsQuery.Builder, pageSize: Int): Listing<GetThreadsQuery.ThreadItem> {
      /*  val boundaryCallback = InboxBoundaryCallback(apolloClient,
                query,
                Executors.newSingleThreadExecutor(),
                Executors.newSingleThreadExecutor(),
                handleResponse = { response, i ->
                    insertResultIntoDb(response, i, Callback = {})
                })
        val dataSourceFactory: DataSource.Factory<Int, Message> =  appDatabase.InboxMessage().loadAll()
        val builder = LivePagedListBuilder(dataSourceFactory, pageSize)
                .setBoundaryCallback(boundaryCallback)
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, { refresh("er") })
        return Listing(pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = { boundaryCallback.helper.retryAllFailed() },
                refresh = { refreshTrigger.value = null },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null)*/

        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = InboxMsgDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    @Synchronized private fun insertResultIntoDb(response: GetThreadsQuery.Data, page: Int, Callback: (Any) -> Unit) {
        try {
        } catch (e :Exception) {
            e.printStackTrace()
        }
        response.threads?.results()?.threadItems()?.forEach { list ->
            appDatabase.runInTransaction {
                appDatabase.InboxMessage().insert(Message(
                        id =  list.id()!!,
                        typeLabel = list.type()!!,
                        createdAt = list.createdAt(),
                        endDate = list.endDate(),
                        reservationId = list.reservationId(),
                        sentBy = list.sentBy(),
                        threadId = list.threadId(),
                        startDate = list.startDate(),
                        typeName = list.__typename(),
                        type = list.type(),
                        page = page,
                        content =  list.content()
                ))
            }
        }
    }

    @MainThread
    private fun refresh(userId: String): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING

        /*val call: Call<Post>? = minitsApi.getHomePost(postRequest)
        call?.enqueue(
                object : Callback<Post> {
                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        // retrofit calls this on main thread so safe to call set value
                        networkState.value = NetworkState.error(t.message)
                    }

                    override fun onResponse(
                            call: Call<Post>,
                            response: Response<Post>) {
                        ioExecutor.execute {
                            db.runInTransaction {
                                db.minitsDao().deleteAllPost()
                                insertResultIntoDb(response.body(), 0, Callback = {any -> empt(any) } )
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                }
        )*/
        return networkState
    }


    override fun listOfInbox(query: GetAllThreadsQuery.Builder, pageSize: Int): Listing<GetAllThreadsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = InboxListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }



    //Email Verification
    override fun sendConfirmationEmail(request: SendConfirmEmailQuery): Single<Response<SendConfirmEmailQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun ConfirmCodeApiCall(request: CodeVerificationMutation): Single<Response<CodeVerificationMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun SocialLoginVerify(request: SocialLoginVerifyMutation): Single<Response<SocialLoginVerifyMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)    }



    //SMS verification
    override fun getPhoneNumber(request: GetEnteredPhoneNoQuery): Single<Response<GetEnteredPhoneNoQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getCountryCode(request: GetCountrycodeQuery): Single<Response<GetCountrycodeQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun addPhoneNumber(request: AddPhoneNumberMutation): Single<Response<AddPhoneNumberMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun verifyPhoneNumber(request: VerifyPhoneNumberMutation): Single<Response<VerifyPhoneNumberMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }



    //ClearCache
    override fun clearHttpCache(): Observable<Boolean> {
        apolloClient.clearHttpCache()
        return Observable.just(true)
    }

    override fun doGetListingSettings(request: GetListingSettingQuery): Single<Response<GetListingSettingQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    //Become Host

    override fun doCreateListing(request: CreateListingMutation): Single<Response<CreateListingMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun dogetListingSettings(request: GetListingSettingQuery): Single<Response<GetListingSettingQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doUpdateListingStep2(request: UpdateListingStep2Mutation): Single<Response<UpdateListingStep2Mutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doUpdateListingStep3(request: UpdateListingStep3Mutation): Single<Response<UpdateListingStep3Mutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doGetListingDetailsStep2Query(request: GetListingDetailsStep2Query): Single<Response<GetListingDetailsStep2Query.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun ShowListPhotosQuery(request: ShowListPhotosQuery): Single<Response<ShowListPhotosQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doManageListingSteps(request: ManageListingStepsMutation): Single<Response<ManageListingStepsMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doShowListingSteps(request: ShowListingStepsQuery): Single<Response<ShowListingStepsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }
    override fun doGetStep1ListingDetailsQuery(request: GetStep1ListingDetailsQuery): Single<Response<GetStep1ListingDetailsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doGetStep3Details(request: GetListingDetailsStep3Query): Single<Response<GetListingDetailsStep3Query.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doRemoveListPhotos(request: RemoveListPhotosMutation): Single<Response<RemoveListPhotosMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doManagePublishStatus(request: ManagePublishStatusMutation): Single<Response<ManagePublishStatusMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doRemoveListingMutation(request: RemoveListingMutation): Single<Response<RemoveListingMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun doRemoveMultiPhotosMutation(request: RemoveMultiPhotosMutation): Single<Response<RemoveMultiPhotosMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    //Payout Preferences
    override fun getPayoutsMethod(request: GetPaymentMethodsQuery): Single<Response<GetPaymentMethodsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun setDefaultPayout(request: SetDefaultPayoutMutation): Single<Response<SetDefaultPayoutMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getPayouts(request: GetPayoutsQuery): Single<Response<GetPayoutsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun addPayout(request: AddPayoutMutation): Single<Response<AddPayoutMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun setPayout(request: ConfirmPayoutMutation): Single<Response<ConfirmPayoutMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun confirmPayout(request: VerifyPayoutMutation): Single<Response<VerifyPayoutMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    //Manage Listing
    override fun getManageListings(request: ManageListingsQuery): Single<Response<ManageListingsQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getListBlockedDates(request: ListBlockedDatesQuery): Single<Response<ListBlockedDatesQuery.Data>> {
        return Rx2Apollo.from(apolloClient.query(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun getUpdateListBlockedDates(request: UpdateListBlockedDatesMutation): Single<Response<UpdateListBlockedDatesMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    //Reservation status
    override fun getReseravtionStatus(request: ReservationStatusMutation): Single<Response<ReservationStatusMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun submitForVerification(request: SubmitForVerificationMutation): Single<Response<SubmitForVerificationMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    //Feedback
    override fun sendfeedBack(request: SendUserFeedbackMutation): Single<Response<SendUserFeedbackMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }

    override fun confirmPayPalPayment(request: ConfirmPayPalExecuteMutation): Single<Response<ConfirmPayPalExecuteMutation.Data>> {
        return Rx2Apollo.from(apolloClient.mutate(request))
                .performAsynReturnSingle(scheduler)
    }
}
