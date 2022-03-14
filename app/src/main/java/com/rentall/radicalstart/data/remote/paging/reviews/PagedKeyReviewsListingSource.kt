package com.rentall.radicalstart.data.remote.paging.reviews

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.rentall.radicalstart.GetUserReviewsQuery
import com.rentall.radicalstart.data.remote.paging.NetworkState
import java.util.concurrent.Executor

class PagedKeyReviewsListingSource(
        private val apolloClient: ApolloClient,
        private val query: GetUserReviewsQuery.Builder,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, GetUserReviewsQuery.Result>() {

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

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, GetUserReviewsQuery.Result>) {}

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, GetUserReviewsQuery.Result>) {
        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)
            val request = query
                    .currentPage(params.key.toInt())
                    .build()
            apolloClient.query(request)
                    .enqueue(object : ApolloCall.Callback<GetUserReviewsQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            retry = { loadAfter(params, callback) }
                            val error = NetworkState.error(e)
                            networkState.postValue(error)
                        }
                        override fun onResponse(response: Response<GetUserReviewsQuery.Data>) {
                            try {
                                if (response.data()?.userReviews?.status() == 200) {
                                    val items = response.data()?.userReviews?.results()
                                    retry = null
                                    if (items!!.size < 10) {
                                        callback.onResult(items, "")
                                    } else {
                                        callback.onResult(items, (params.key.toInt() + 1).toString())
                                    }
                                    networkState.postValue(NetworkState.LOADED)
                                }else if(response.data()?.userReviews?.status() == 500){
                                    retry = null
                                    networkState.postValue(NetworkState.EXPIRED)
                                }
                                else {
                                    retry = null
                                    callback.onResult(emptyList(), (params.key.toInt() + 1).toString())
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

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, GetUserReviewsQuery.Result>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val request = query
                .currentPage(1)
                .build()
        apolloClient.query(request)
                .enqueue(object : ApolloCall.Callback<GetUserReviewsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        retry = { loadInitial(params, callback) }
                        val error = NetworkState.error(e)
                        networkState.postValue(error)
                        initialLoad.postValue(error)
                    }
                    override fun onResponse(response: Response<GetUserReviewsQuery.Data>) {
                        try {
                            if (response.data()?.userReviews?.status() == 200) {
                                val items = response.data()?.userReviews?.results()
                                retry = null
                                networkState.postValue(NetworkState.LOADED)
                                initialLoad.postValue(NetworkState.LOADED)
                                if (items!!.size < 10) {
                                    callback.onResult(items, "1", "")
                                } else {
                                    callback.onResult(items, "1", "2")
                                }
                            }else if(response.data()?.userReviews?.status() == 500){
                                retry = null
                                networkState.postValue(NetworkState.EXPIRED)
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