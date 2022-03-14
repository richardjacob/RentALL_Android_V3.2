package com.rentall.radicalstart.data.remote.paging.inbox

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.rentall.radicalstart.GetAllThreadsQuery
import java.util.concurrent.Executor

class InboxListDataSourceFactory(
        private val apolloClient: ApolloClient,
        private val query: GetAllThreadsQuery.Builder,
        private val executor: Executor) : DataSource.Factory<String, GetAllThreadsQuery.Result>() {
    val sourceLiveData = MutableLiveData<PageKeyedInboxListSource>()
    override fun create(): DataSource<String, GetAllThreadsQuery.Result> {
        val source = PageKeyedInboxListSource(apolloClient, query, executor)
        sourceLiveData.postValue(source)
        return source
    }
}