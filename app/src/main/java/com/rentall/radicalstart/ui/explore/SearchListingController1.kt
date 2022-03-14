package com.rentall.radicalstart.ui.explore

import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.rentall.radicalstart.viewholderLoader
import com.rentall.radicalstart.viewholderPagingRetry
import com.rentall.radicalstart.vo.ListingInitData
import com.rentall.radicalstart.vo.SearchListing


class SearchListingController1(private val listingInitData: ListingInitData,
                              private val clickListener: (item: SearchListing, listingInitData: ListingInitData,view : View) -> Unit,
                              private val onWishListClick: (item: SearchListing, listingInitData: ListingInitData) -> Unit,
                              private val retryListener: () -> Unit
) : EpoxyController() {

    var list = ArrayList<SearchListing>()
        set(value) {
            if (value != field) {
                field = value
               // requestModelBuild()
            }
        }

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

    override fun buildModels() {
        list.forEach {
            add(SearchListingEpoxyGroup1( it, listingInitData, clickListener, onWishListClick))
        }

        if (retry) {
            viewholderPagingRetry {
                id("retry")
                clickListener(View.OnClickListener { retryListener() })
            }
        }
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}