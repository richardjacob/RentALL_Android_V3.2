package com.rentall.radicalstart.data.remote.paging.user_review

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.rentall.radicalstart.UserReviewsQuery
import com.rentall.radicalstart.data.remote.paging.NetworkState
import java.util.concurrent.Executor

class PageKeyedUserReviewDataSource(
        private val apolloClient: ApolloClient,
        private val builder: UserReviewsQuery.Builder,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, UserReviewsQuery.Result>() {

    private var retry: (() -> Any)? = null

    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, UserReviewsQuery.Result>) {}

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, UserReviewsQuery.Result>) {
        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)
            val query = builder.currentPage(params.key.toInt())
                    .build()
            apolloClient.query(query)
                    .enqueue(object : ApolloCall.Callback<UserReviewsQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            retry = { loadAfter(params, callback) }
                            val error = NetworkState.error(e)
                            networkState.postValue(error)
                        }

                        override fun onResponse(response: Response<UserReviewsQuery.Data>) {
                            try {
                                if (response.data()?.userReviews()?.status() == 200) {
                                    val items = response.data()?.userReviews()?.results()
                                    retry = null
                                    if (items!!.size < 10) {
                                        callback.onResult(items, "")
                                    } else {
                                        callback.onResult(items, (params.key.toInt() + 1).toString())
                                    }
                                    networkState.postValue(NetworkState.LOADED)
                                } else {
                                    retry = null
                                    networkState.postValue(NetworkState.LOADED)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                val error = NetworkState.error(e)
                                networkState.postValue(error)
                            }
                        }
                    })
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, UserReviewsQuery.Result>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val query =  builder.currentPage(0)
                .build()
        apolloClient.query(query)
                .enqueue(object : ApolloCall.Callback<UserReviewsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        retry = { loadInitial(params, callback) }
                        val error = NetworkState.error(e)
                        networkState.postValue(error)
                        initialLoad.postValue(error)
                    }
                    override fun onResponse(response: Response<UserReviewsQuery.Data>) {
                        try {
                            if (response.data()?.userReviews()?.status() == 200) {
                                val items = response.data()?.userReviews()?.results()
                                retry = null
                                networkState.postValue(NetworkState.LOADED)
                                initialLoad.postValue(NetworkState.LOADED)
                                if (items!!.size < 10) {
                                    callback.onResult(items, "1", "")
                                } else {
                                    callback.onResult(items, "1", "2")
                                }
                            } else {
                                retry = null
                                networkState.postValue(NetworkState.SUCCESSNODATA)
                                initialLoad.postValue(NetworkState.LOADED)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val error = NetworkState.error(e)
                            networkState.postValue(error)
                            initialLoad.postValue(error)
                        }
                    }
                })
    }
}
