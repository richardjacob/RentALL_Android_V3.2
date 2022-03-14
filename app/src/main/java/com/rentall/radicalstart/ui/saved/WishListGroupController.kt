package com.rentall.radicalstart.ui.saved

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.ViewholderSavedListCarouselBindingModel_
import com.rentall.radicalstart.util.epoxy.ListingSimilarCarouselModel_
import com.rentall.radicalstart.vo.SavedList
import timber.log.Timber

class WishListGroupController : PagedListEpoxyController<SavedList>() {

    var isLoading = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    override fun buildItemModel(currentPosition: Int, item: SavedList?): EpoxyModel<*> {

        try {
            return ListingSimilarCarouselModel_().apply {
                Carousel.setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                    override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                        return PagerSnapHelper()
                    }
                })
                id("Carousel - ${item?.id}")
                padding(Carousel.Padding.dp(20, 0, 0, 0, 0))
                models(mutableListOf<ViewholderSavedListCarouselBindingModel_>().apply {
                    add(ViewholderSavedListCarouselBindingModel_()
                            .id("savedList - ${item?.id}")
                            .url(item?.img)
                           // .url(list.wishListCover()!!.listData()!!.listPhotos()!![0].name())
                            .name(item?.title)
                            .wishListStatus(item?.isWishList)
                            .progress(item?.progress)
                            .heartClickListener(View.OnClickListener {

                            })
                            .clickListener(View.OnClickListener {

                            })
                    )
                })
                onBind { model, view, position ->
                    // (view as RecyclerView).onDrawForeground()
                }
            }
        } catch (e: Exception) {
            Timber.e(e,"CRASH")
        }
        return ListingSimilarCarouselModel_()
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        try {
            super.addModels(models)
        } catch (e: Exception) {
            Timber.e(e,"CRASH")
        }
        /*if (isLoading) {
            viewholderLoader {
                id("loading")
                isLoading(isLoading)
            }
        }*/
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }
}
