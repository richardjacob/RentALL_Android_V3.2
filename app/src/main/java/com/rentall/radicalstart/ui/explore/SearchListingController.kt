package com.rentall.radicalstart.ui.explore

import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.SearchListingQuery
import com.rentall.radicalstart.viewholderLoader
import com.rentall.radicalstart.viewholderPagingRetry
import com.rentall.radicalstart.vo.ListingInitData
import timber.log.Timber

class SearchListingController(private val listingInitData: ListingInitData,
                              private val clickListener: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
                              private val onWishListClick: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
                              private val retryListener: () -> Unit
) : PagedListEpoxyController<SearchListingQuery.Result>() {

    var isLoading = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    var retry = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    override fun buildItemModel(currentPosition: Int, item: SearchListingQuery.Result?): EpoxyModel<*> {

        return SearchListingEpoxyGroup(currentPosition, item!!, listingInitData, clickListener, onWishListClick)
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        try {
            super.addModels(models)
            if (retry) {
                viewholderPagingRetry {
                    id("retry")
                    clickListener(View.OnClickListener { retryListener() })
                }
            }
            if (isLoading) {
                viewholderLoader {
                    id("loading")
                    isLoading(true)
                }
            }
        } catch (e: Exception) {
            Timber.e(e,"CRASH")
        }
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}