package com.rentall.radicalstart.data.remote.paging.trips

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.rentall.radicalstart.GetAllReservationQuery
import java.util.concurrent.Executor

class TripsDataSourceFactory(
        private val apolloClient: ApolloClient,
        private val query: GetAllReservationQuery.Builder,
        private val executor: Executor) : DataSource.Factory<String, GetAllReservationQuery.Result>() {
    val sourceLiveData = MutableLiveData<PageKeyedTripsListingSource>()
    override fun create(): DataSource<String, GetAllReservationQuery.Result> {
        val source = PageKeyedTripsListingSource(apolloClient, query, executor)
        sourceLiveData.postValue(source)
        return source
    }
}