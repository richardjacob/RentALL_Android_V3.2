package com.rentall.radicalstart.data.remote.paging.user_review

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.rentall.radicalstart.UserReviewsQuery
import java.util.concurrent.Executor

class UserReviewDataSourceFactory(
        private val apolloClient: ApolloClient,
        private val builder: UserReviewsQuery.Builder,
        private val executor: Executor) : DataSource.Factory<String, UserReviewsQuery.Result>() {
    val sourceLiveData = MutableLiveData<PageKeyedUserReviewDataSource>()
    override fun create(): DataSource<String, UserReviewsQuery.Result> {
        val source = PageKeyedUserReviewDataSource(apolloClient, builder, executor)
        sourceLiveData.postValue(source)
        return source
    }
}