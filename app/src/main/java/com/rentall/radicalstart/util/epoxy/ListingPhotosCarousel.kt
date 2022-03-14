package com.rentall.radicalstart.util.epoxy

import android.content.Context
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView
import com.rentall.radicalstart.util.Dectorator


@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListingPhotosCarousel(context: Context) : Carousel(context) {

    init {
        addItemDecoration(Dectorator(context.resources, context, this))
    }

    @NonNull
    override fun createLayoutManager(): androidx.recyclerview.widget.RecyclerView.LayoutManager {
        return LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
    }

   /* override fun onDrawForeground(canvas: Canvas?) {
        val paint = Paint()
        paint.setColor(Color.BLACK)
        paint.setTextSize(100F)
        canvas?.drawText("Some Text", 10F, 25F, paint)
    }

    override fun onDraw(c: Canvas?) {
        super.onDraw(c)
    }*/

}
