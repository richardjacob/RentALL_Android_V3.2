package com.rentall.radicalstart.data.remote.paging.wishlist

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.rentall.radicalstart.GetWishListGroupQuery
import com.rentall.radicalstart.data.remote.paging.NetworkState
import java.util.concurrent.Executor


class PagedKeyWishListSource(
        private val apolloClient: ApolloClient,
        private val builder: GetWishListGroupQuery.Builder,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, GetWishListGroupQuery.WishList>() {

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

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, GetWishListGroupQuery.WishList>) {}

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, GetWishListGroupQuery.WishList>) {
        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)
            val query = builder.currentPage(params.key.toInt())
                    .build()
            apolloClient.query(query)
                    .enqueue(object : ApolloCall.Callback<GetWishListGroupQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            retry = { loadAfter(params, callback) }
                            val error = NetworkState.error(e)
                            networkState.postValue(error)
                        }

                        override fun onResponse(response: Response<GetWishListGroupQuery.Data>) {
                            try {
                                if (response.data()?.wishListGroup!!.status() == 200) {
                                    val items = response.data()?.wishListGroup!!.results()!!.wishLists()
                                    retry = null
                                    if (items!!.size < 10) {
                                        callback.onResult(items, "")
                                    } else {
                                        callback.onResult(items, (params.key.toInt() + 1).toString())
                                    }
                                    networkState.postValue(NetworkState.LOADED)
                                }else if(response.data()?.wishListGroup!!.status() == 500){
                                    retry = null
                                    networkState.postValue(NetworkState.EXPIRED)
                                }
                                else {
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

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, GetWishListGroupQuery.WishList>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        val query =  builder.currentPage(1)
                .build()
        apolloClient.query(query)
                .enqueue(object : ApolloCall.Callback<GetWishListGroupQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        retry = { loadInitial(params, callback) }
                        val error = NetworkState.error(e)
                        networkState.postValue(error)
                        initialLoad.postValue(error)
                    }
                    override fun onResponse(response: Response<GetWishListGroupQuery.Data>) {
                        try {
                            if (response.data()?.wishListGroup!!.status() == 200) {
                                val items = response.data()?.wishListGroup!!.results()!!.wishLists()

                                when {
                                    items == null || items.isEmpty() -> {
                                        retry = null
                                        networkState.postValue(NetworkState.SUCCESSNODATA)
                                        initialLoad.postValue(NetworkState.LOADED)
                                    }
                                    items!!.size < 10 -> {
                                        retry = null
                                        networkState.postValue(NetworkState.LOADED)
                                        initialLoad.postValue(NetworkState.LOADED)
                                        callback.onResult(items, "1", "")
                                    }
                                    else -> {
                                        retry = null
                                        networkState.postValue(NetworkState.LOADED)
                                        initialLoad.postValue(NetworkState.LOADED)
                                        callback.onResult(items, "1", "2")
                                    }
                                }
                            }else if(response.data()?.wishListGroup!!.status() == 500){
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
