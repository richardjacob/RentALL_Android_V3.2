package com.rentall.radicalstart.ui.profile.about.why_Host

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.rentall.radicalstart.R
import com.rentall.radicalstart.util.GlideApp

class SlidingImageAdapter(private val context: Context, private val imageModelArrayList: ArrayList<ImageModel>) : PagerAdapter() {

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imageModelArrayList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(context).inflate(R.layout.sliding_images, view, false)!!
        val imageView = imageLayout.findViewById(R.id.image) as ImageView
        val textView = imageLayout.findViewById(R.id.sliding_texts) as TextView
        textView.text = this.context.getString(imageModelArrayList[position].getText_srings())
        GlideApp.with(imageView.context)
                .load(imageModelArrayList[position].getImage_drawables())
                .into(imageView)
        view.addView(imageLayout, 0)
        return imageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

}