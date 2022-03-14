package com.rentall.radicalstart.ui.profile.review.controller

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.*
import com.rentall.radicalstart.ui.profile.review.ReviewViewModel
import com.rentall.radicalstart.ui.reservation.ReservationActivity
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.Utils

class PendingReviewsListController(val context: Context,val viewModel: ReviewViewModel) : PagedListEpoxyController<GetPendingUserReviewsQuery.Result>() {
    override fun buildItemModel(currentPosition: Int, item: GetPendingUserReviewsQuery.Result?): EpoxyModel<*> {
        val ltrDirection= !context.resources.getBoolean(R.bool.is_left_to_right_layout).not()
        try{
            return if(viewModel.dataManager.currentUserId.equals(item?.guestId())){
                ViewholderPendingReviewInfoBindingModel_()
                        .id("viewholder- ${item!!.id()}")
                        .name(item.hostData()?.firstName()+" "+item.hostData()?.lastName())
                        .type("writeReview")
                        .imgUrl(item.hostData()?.picture())
                        .ltrDirection(ltrDirection)
                        .onItineraryClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                try {
                                    val intent = Intent(context, ReservationActivity::class.java)
                                    intent.putExtra("type", 1)
                                    intent.putExtra("reservationId", item.id())
                                    intent.putExtra("userType","Guest")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                        .onWriteReviewClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                viewModel.navigator.openWriteReview(item.id() ?: 0)
                            }
                        })
                        .onAvatarClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                UserProfileActivity.openProfileActivity(this.context, item.hostData()?.profileId()!!)
                            }
                        })
                        .profileId(item.hostData()?.profileId()!!)
            }else{
                ViewholderPendingReviewInfoBindingModel_()
                        .id("viewholder- ${item!!.id()}")
                        .name(item.guestData()?.firstName()+" "+item.guestData()?.lastName())
                        .type("writeReview")
                        .imgUrl(item.guestData()?.picture())
                        .ltrDirection(ltrDirection)
                        .onItineraryClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                try {
                                    val intent = Intent(context, ReservationActivity::class.java)
                                    intent.putExtra("type", 1)
                                    intent.putExtra("reservationId", item.id())
                                    intent.putExtra("userType","Guest")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                        .onWriteReviewClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                viewModel.navigator.openWriteReview(item.id() ?: 0)
                            }
                        })
                        .onAvatarClick(View.OnClickListener {
                            Utils.clickWithDebounce(it) {
                                UserProfileActivity.openProfileActivity(this.context, item.guestData()?.profileId()!!)
                            }
                        })
                        .profileId(item.guestData()?.profileId()!!)
            }
        }catch (e: Exception){
            return ViewholderPendingReviewInfoBindingModel_()
        }
    }
}