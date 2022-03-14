package com.rentall.radicalstart.ui.saved

import android.os.SystemClock
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.rentall.radicalstart.ViewholderSavedListingBindingModel_
import com.rentall.radicalstart.util.CurrencyUtil
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.Utils.Companion.clickWithDebounce
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.viewholderListingDetailsSectionHeader
import com.rentall.radicalstart.viewholderLoader
import com.rentall.radicalstart.viewholderPagingRetry
import com.rentall.radicalstart.vo.SearchListing
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SavedDetailsController(
        val id: Int,
        val name1: String,
        val base: String,
        val rate: String,
        val userCurrency: String,
        private val clickListener: (item: SearchListing, view: View) -> Unit,
        private val onWishListClick: (item: SearchListing) -> Unit,
        private val retryListener: () -> Unit
) : EpoxyController() {
    var lastClickTime=0L
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
        for (i in 0 until 1) {
            viewholderListingDetailsSectionHeader {
                id("header")
                header(name1)
            }
        }

        list.forEach {item ->
            add(ViewholderSavedListingBindingModel_()
                    .id("savedList - ${item.id}")
                    .title(item.title)
                    .roomType(item.roomType)
                    .price(currencyConverter(base, rate, userCurrency, item.currency ,item.basePrice))
                    .reviewsCount(item.reviewsCount)
                    .ratingStarCount(item.reviewsStarRating)
                    .url(item.listPhotoName)
                    .bookType(item.bookingType)
                    .wishListStatus(true)
                    .isOwnerList(false)
                    .heartClickListener(View.OnClickListener {
                        onWishListClick(item)
                    })
                    .onClick(View.OnClickListener {
                        it.clickWithDebounce { clickListener(item,it) }
                    })
            )
        }

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
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}

fun currencyConverter(base: String, rate: String, userCurrency: String, currency: String, total: Double): String {
    return BindingAdapters.getCurrencySymbol(userCurrency) + Utils.formatDecimal(CurrencyUtil.getRate(
            base = base,
            to = userCurrency,
            from = currency,
            rateStr = rate,
            amount = total
    ))
}


