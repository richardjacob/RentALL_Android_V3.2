package com.rentall.radicalstart.data.remote.paging.wishlist

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.rentall.radicalstart.GetWishListGroupQuery
import java.util.concurrent.Executor


class WishListDataSourceFactory(
        private val apolloClient: ApolloClient,
        private val builder: GetWishListGroupQuery.Builder,
        private val executor: Executor) : DataSource.Factory<String, GetWishListGroupQuery.WishList>() {
    val sourceLiveData = MutableLiveData<PagedKeyWishListSource>()
    override fun create(): DataSource<String, GetWishListGroupQuery.WishList> {
        val source = PagedKeyWishListSource(apolloClient, builder, executor)
        sourceLiveData.postValue(source)
        return source
    }
}
