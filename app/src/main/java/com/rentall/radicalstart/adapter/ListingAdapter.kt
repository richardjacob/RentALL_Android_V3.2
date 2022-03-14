package com.rentall.radicalstart.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.rentall.radicalstart.Constants
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ItemListingSimilarHomesBinding
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.util.GlideApp
import com.rentall.radicalstart.util.invisible
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.vo.Listing
import com.rentall.radicalstart.vo.ListingInitData
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import kotlin.math.round

class ListingAdapter(val items : ArrayList<Listing>,
                     private val listingInitData: ListingInitData,
                     val clickListener: (item: Listing) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val itemBinding = ItemListingSimilarHomesBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ListingViewHolder(itemBinding)
    }

    @Suppress("SENSELESS_COMPARISON")
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.type.text = items[position].type
        holder.title.text = items[position].title.trim().replace("\\s+", " ")
        holder.price.text = items[position].price + items[position].per_night

        if (items[position].rating != "0") {
            holder.ratingNumber.text = items[position].rating
        } else {
            holder.ratingNumber.text = ""
        }
        if (items[position].bookingType == "instant") {
            holder.bookingType.setImageResource(R.drawable.ic_light)
        } else {
            holder.bookingType.setImageResource(0)
        }
        val ads = Constants.imgListingMedium + items[position].image

        if (items[position].rating != null && items[position].rating != "0" && items[position].ratingStar != null && items[position].ratingStar != 0) {
            val roundOff = round(items[position].ratingStar.toDouble()/items[position].rating.toDouble())
            holder.rating.rating = roundOff.toFloat()
        } else {
            holder.rating.rating = 0.toFloat()
        }
/*
        if(items[position].isOwnerList.not()) {
            if (items[position].isWishList) {
                holder.heartIcon.setImageResource(R.drawable.ic_heart_filled)
            } else {
                holder.heartIcon.setImageResource(R.drawable.ic_heart_white)
            }
        }

        holder.heartIcon.onClick {
            clickListener(items[position])
        }*/

        GlideApp.with(holder.image.context)
                .load(ads)
                .into(holder.image)

        if (items[position].selected) {
            holder.highlighter.visible()
        } else {
            holder.highlighter.invisible()
        }
        holder.root.setOnClickListener {
            try {
                with(items[position]) {
                    try {
                        listingInitData.title = title
                        listingInitData.photo.add(image)
                        listingInitData.id = id
                        listingInitData.roomType = type
                        listingInitData.ratingStarCount = ratingStar
                        listingInitData.reviewCount = rating.toInt()
                        listingInitData.price = price
                        listingInitData.isWishList = items[position].isWishList
                        ListingDetails.openListDetailsActivity(it.context, listingInitData)
                        items.clear()
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }
                }
            }catch (e: KotlinNullPointerException) {
                e.printStackTrace()
            }catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

class ListingViewHolder (itemBinding: ItemListingSimilarHomesBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemBinding.root) {
    val image: ImageView = itemBinding.ivItemListingSimilarImage
    val type: TextView = itemBinding.tvItemListingSimilarType
    val title: TextView = itemBinding.tvItemListingSimilarTitle
    val price: TextView = itemBinding.tvItemListingSimilarPrice
    val rating: RatingBar = itemBinding.tvItemListingSimilarRating
    val ratingNumber: TextView = itemBinding.tvItemListingSimilarRatingNumber
    val highlighter: View = itemBinding.viewListingHighlighter
    val bookingType: ImageView = itemBinding.ivItemListingInstantImage
    val heartIcon: ImageView = itemBinding.ivItemListingHeart
    val root: View = itemBinding.conlRoot
}