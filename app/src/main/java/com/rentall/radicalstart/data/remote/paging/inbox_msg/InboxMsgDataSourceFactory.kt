package com.rentall.radicalstart.data.remote.paging.inbox_msg

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.rentall.radicalstart.GetThreadsQuery
import java.util.concurrent.Executor

class InboxMsgDataSourceFactory(
        private val apolloClient: ApolloClient,
        private val query: GetThreadsQuery.Builder,
        private val executor: Executor) : DataSource.Factory<String, GetThreadsQuery.ThreadItem>() {
    val sourceLiveData = MutableLiveData<PageKeyedInboxMsgSource>()
    override fun create(): DataSource<String, GetThreadsQuery.ThreadItem> {
        val source = PageKeyedInboxMsgSource(apolloClient, query, executor)
        sourceLiveData.postValue(source)
        return source
    }
}