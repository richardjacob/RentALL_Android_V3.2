package com.rentall.radicalstart.data.remote.paging.inbox_msg

import android.util.Log
import androidx.annotation.MainThread
import androidx.paging.PagedList
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.rentall.radicalstart.GetThreadsQuery
import com.rentall.radicalstart.data.model.db.Message
import com.rentall.radicalstart.util.PagingRequestHelper
import com.rentall.radicalstart.util.createStatusLiveData
import java.util.concurrent.Executor

class InboxBoundaryCallback(
        private val apolloClient: ApolloClient,
        private val query: GetThreadsQuery.Builder,
        private val retryExecutor: Executor,
        private val ioExecutor: Executor,
        private val handleResponse: (GetThreadsQuery.Data, Int) -> Unit)
    : PagedList.BoundaryCallback<Message>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        val request = query
                .currentPage(1)
                .build()
        Log.d("inboxPage123OnZero", "1")
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            apolloClient.query(request)
                    .enqueue(object : ApolloCall.Callback<GetThreadsQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            it.recordFailure(e)
                        }
                        override fun onResponse(response: Response<GetThreadsQuery.Data>) {
                            try {
                                if (response.data()?.threads?.status() == 200) {
                                    insertItemsIntoDb(response.data()!!, it, 1)
                                }

                            } catch (e: Exception) {
                                it.recordFailure(e)
                            }
                        }
                    })
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Message) {
       /* val page = itemAtEnd.page + 1
        val request = query
                .currentPage(page)
                .build()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE) {
            apolloClient.query(request)
                    .enqueue(object : ApolloCall.Callback<GetThreadsQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            it.recordFailure(e)
                        }
                        override fun onResponse(response: Response<GetThreadsQuery.Data>) {
                            try {
                                if (response.data()?.threads?.status() == 200) {
                                    insertItemsIntoDb(response.data()!!, it, page)
                                }
                            } catch (e: Exception) {
                                it.recordFailure(e)
                            }
                        }
                    })
        }*/
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    override fun onItemAtFrontLoaded(itemAtFront: Message) {
        val page = itemAtFront.page// + 1
        Log.d("inboxPage123", page.toString())
        val request = query
                .currentPage(1)
                .build()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE) {
            apolloClient.query(request)
                    .enqueue(object : ApolloCall.Callback<GetThreadsQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            it.recordFailure(e)
                        }
                        override fun onResponse(response: Response<GetThreadsQuery.Data>) {
                            try {
                                if (response.data()?.threads?.status() == 200) {
                                    insertItemsIntoDb(response.data()!!, it, page)
                                }
                            } catch (e: Exception) {
                                it.recordFailure(e)
                            }
                        }
                    })
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: GetThreadsQuery.Data,
            it: PagingRequestHelper.Request.Callback, page: Int) {
        ioExecutor.execute {
            handleResponse(response, page)
            it.recordSuccess()
        }
    }

}