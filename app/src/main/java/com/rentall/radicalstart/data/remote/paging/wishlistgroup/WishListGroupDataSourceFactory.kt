package com.rentall.radicalstart.data.remote.paging.wishlistgroup

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.rentall.radicalstart.GetAllWishListGroupQuery
import java.util.concurrent.Executor


class WishListGroupDataSourceFactory(
        private val apolloClient: ApolloClient,
        private val builder: GetAllWishListGroupQuery.Builder,
        private val executor: Executor) : DataSource.Factory<String, GetAllWishListGroupQuery.Result>() {
    val sourceLiveData = MutableLiveData<PagedKeyWishListGroupSource>()
    override fun create(): DataSource<String, GetAllWishListGroupQuery.Result> {
        val source = PagedKeyWishListGroupSource(apolloClient, builder, executor)
        sourceLiveData.postValue(source)
        return source
    }
}