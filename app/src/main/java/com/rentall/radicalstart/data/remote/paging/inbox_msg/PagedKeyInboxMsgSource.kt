package com.rentall.radicalstart.data.remote.paging.inbox_msg

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.rentall.radicalstart.GetThreadsQuery
import com.rentall.radicalstart.data.remote.paging.NetworkState
import java.util.concurrent.Executor

class PageKeyedInboxMsgSource(
        private val apolloClient: ApolloClient,
        private val query: GetThreadsQuery.Builder,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, GetThreadsQuery.ThreadItem>() {

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

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, GetThreadsQuery.ThreadItem>) {
        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)
            val request = query
                    .currentPage(params.key.toInt() + 1)
                    .build()
            apolloClient.query(request)
                    .enqueue(object : ApolloCall.Callback<GetThreadsQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            retry = { loadBefore(params, callback) }
                            networkState.postValue(NetworkState.error(e))
                        }
                        override fun onResponse(response: Response<GetThreadsQuery.Data>) {
                            try {
                                if (response.data()?.threads?.status() == 200) {
                                    val items = ArrayList(response.data()?.threads?.results()!!.threadItems()!!)
                                    items.reverse()
                                    retry = null
                                    if (items.size < 10) {
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
                            }
                        }
                    })
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, GetThreadsQuery.ThreadItem>) { }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, GetThreadsQuery.ThreadItem>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val request = query
                .currentPage(1)
                .build()
        apolloClient.query(request)
                .enqueue(object : ApolloCall.Callback<GetThreadsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        retry = { loadInitial(params, callback) }
                        val error = NetworkState.error(e)
                        networkState.postValue(error)
                        initialLoad.postValue(error)
                    }
                    override fun onResponse(response: Response<GetThreadsQuery.Data>) {
                        try {
                            if (response.data()?.threads?.status() == 200) {
                                val items = ArrayList(response.data()?.threads?.results()!!.threadItems()!!)
                                items.reverse()
                                retry = null
                                networkState.postValue(NetworkState.LOADED)
                                initialLoad.postValue(NetworkState.LOADED)
                                if (items.size < 10) {
                                    callback.onResult(items, "", "2")
                                } else {
                                    callback.onResult(items, "1", "2")
                                }
                            } else {
                                retry = null
                                networkState.postValue(NetworkState.SUCCESSNODATA)
                                initialLoad.postValue(NetworkState.LOADED)
                            }
                        } catch (e: Exception) {
                           val error = NetworkState.error(e)
                            networkState.postValue(error)
                            initialLoad.postValue(error)
                            e.printStackTrace()
                        }
                    }
                })
    }
}