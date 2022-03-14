package com.rentall.radicalstart.ui.explore

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.rentall.radicalstart.*
import com.rentall.radicalstart.util.CurrencyUtil
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.util.epoxy.ListingPhotosCarouselModel_
import com.rentall.radicalstart.vo.ListingInitData
import java.util.*

class SearchListingEpoxyGroup (
        currentPosition: Int,
        val item: SearchListingQuery.Result,
        listingInitData: ListingInitData,
        val clickListener: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
        val onWishListClick: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit
) : EpoxyModelGroup(R.layout.model_carousel_group, buildModels(item, listingInitData, clickListener, onWishListClick)) {

    init {
        id("SearchListing - ${item.id()}")
    }
}

fun buildModels(item: SearchListingQuery.Result, listingInitData: ListingInitData,
                clickListener: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit,
                onWishListClick: (item: SearchListingQuery.Result, listingInitData: ListingInitData) -> Unit
): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    try {
        val convertedPrice = getCurrencyRate(item, listingInitData)
        val images = ArrayList<String>()

        item.listPhotoName()?.let {
            images.add(it)
        }

        item.listPhotos()?.forEach {
            if (images.contains(it.name()).not()) {
                it.name()?.let { name ->
                    images.add(name)
                }
            }
        }

       /* item.coverPhoto()?.let {
            for (i in 0 until item.listPhotos()!!.size) {
                if (it == item.listPhotos()!![i].id()) {
                    images.add(0, item.listPhotos()!![i].name()!!)
                    break
                }
            }
        }

*/
        models.add(ListingPhotosCarouselModel_().apply {
            Carousel.setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                    return PagerSnapHelper()
                }
            })
            id("Carousel - ${item.id()}")
            padding(Carousel.Padding.dp(0, 20, 0, 0, 0))
            models(mutableListOf<ViewholderListingDetailsCarouselBindingModel_>().apply {
                images.forEachIndexed { index, name: String ->
                    add(ViewholderListingDetailsCarouselBindingModel_()
                            .id("images-$name")
                            .url(name)
                            .clickListener { _ ->
                                listingInitData.price = convertedPrice
                                clickListener(item, listingInitData)
                            }
                    )}
            })
        })

        models.add(ViewholderHeartSavedBindingModel_()
                .id("heart - ${item.id()}")
                .wishListStatus(item.wishListStatus())
                .isOwnerList(item.isListOwner)
                .heartClickListener(View.OnClickListener {
                    onWishListClick(item, listingInitData)
                }))

        models.add(ViewholderExploreSearchListingItemBindingModel_()
                .id(item.id())
                .title(item.title())
                .roomType(item.roomType())
                .bookType(item.bookingType())
                .ratingStarCount(item.reviewsStarRating())
                .reviewsCount(item.reviewsCount())
                .price(convertedPrice)
                .onClick { _ ->
                    listingInitData.price = convertedPrice
                    clickListener(item, listingInitData)
                })

     //   return models
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        return models
    }
}

fun getCurrencyRate(item: SearchListingQuery.Result, listingInitData: ListingInitData): String {
    return try {
        (BindingAdapters.getCurrencySymbol(listingInitData.selectedCurrency) + Utils.formatDecimal(CurrencyUtil.getRate(
                base = listingInitData.currencyBase,
                to = listingInitData.selectedCurrency,
                from = item.listingData()!!.currency()!!,
                rateStr = listingInitData.currencyRate,
                amount = item.listingData()!!.basePrice()!!.toDouble())
        ))
    } catch (e: Exception) {
        e.printStackTrace()
        "0"
    }
}
