package com.rentall.radicalstart.util.epoxy

import android.content.Context
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView


@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListingSimilarCarousel(context: Context) : Carousel(context) {

    @NonNull
    override fun createLayoutManager(): androidx.recyclerview.widget.RecyclerView.LayoutManager {
        return LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
    }
}