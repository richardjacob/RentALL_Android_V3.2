package com.rentall.radicalstart.ui.explore

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ViewholderExploreSearchListingItemBindingModel_
import com.rentall.radicalstart.ViewholderHeartSavedBindingModel_
import com.rentall.radicalstart.ViewholderListingDetailsCarouselBindingModel_
import com.rentall.radicalstart.util.CurrencyUtil
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.util.epoxy.ListingPhotosCarouselModel_
import com.rentall.radicalstart.vo.ListingInitData
import com.rentall.radicalstart.vo.SearchListing
import java.util.*

class SearchListingEpoxyGroup1 (
        val item: SearchListing,
        listingInitData: ListingInitData,
        val clickListener: (item: SearchListing, listingInitData: ListingInitData,view: View) -> Unit,
        val onWishListClick: (item: SearchListing, listingInitData: ListingInitData) -> Unit
) : EpoxyModelGroup(R.layout.model_carousel_group, buildModels1(item, listingInitData, clickListener, onWishListClick)) {

    init {
        id("SearchListing - ${item.id}")
    }
}

fun buildModels1(item: SearchListing, listingInitData: ListingInitData,
                clickListener: (item: SearchListing, listingInitData: ListingInitData,view: View) -> Unit,
                onWishListClick: (item: SearchListing, listingInitData: ListingInitData) -> Unit
): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    try {
        val convertedPrice = getCurrencyRate1(item, listingInitData)
        val images = ArrayList<String>()


        /*if (item.id == 1) {
        }*/

        item.listPhotoName?.let {
            images.add(it)
        }

        item.listPhotos.forEach {
            if (images.contains(it.name).not()) {
                it.name.let { name ->
                    images.add(name)
                }
            }
        }

        models.add(ListingPhotosCarouselModel_().apply {
            Carousel.setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                    return PagerSnapHelper()
                }
            })
            id("Carousel - ${item.id}")
            padding(Carousel.Padding.dp(0, 20, 0, 0, 0))
            models(mutableListOf<ViewholderListingDetailsCarouselBindingModel_>().apply {
                images.forEachIndexed { index, name: String ->
                    add(ViewholderListingDetailsCarouselBindingModel_()
                            .id("images-$name")
                            .url(name)
                            .clickListener(View.OnClickListener {
                                listingInitData.price = convertedPrice
                                clickListener(item, listingInitData,it)
                            })
                    )}
            })
        })

        models.add(ViewholderHeartSavedBindingModel_()
                .id("heart - ${item.id}")
                .wishListStatus(item.wishListStatus)
                .isOwnerList(item.isListOwner)
                .heartClickListener(View.OnClickListener {
                    onWishListClick(item, listingInitData)
                }))

        models.add(ViewholderExploreSearchListingItemBindingModel_()
                .id(item.id)
                .title(item.title.trim().replace("\\s+", " "))
                .roomType(item.roomType)
                .bookType(item.bookingType)
                .ratingStarCount(item.reviewsStarRating)
                .reviewsCount(item.reviewsCount)
                .price(convertedPrice)
                .onClick(View.OnClickListener {
                    listingInitData.price = convertedPrice
                    clickListener(item, listingInitData,it)
                })

                )

        //   return models
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        return models
    }
}

fun getCurrencyRate1(item: SearchListing, listingInitData: ListingInitData): String {
    return try {
        (BindingAdapters.getCurrencySymbol(listingInitData.selectedCurrency) + Utils.formatDecimal(CurrencyUtil.getRate(
                base = listingInitData.currencyBase,
                to = listingInitData.selectedCurrency,
                from = item.currency,
                rateStr = listingInitData.currencyRate,
                amount = item.basePrice)
        ))
    } catch (e: Exception) {
        e.printStackTrace()
        "0"
    }
}
