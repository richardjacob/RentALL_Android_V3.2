package com.rentall.radicalstart.ui.profile.review.controller

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.GetUserReviewsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ViewholderReviewInfoBindingModel_
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.Utils


class ReviewListController(val context: Context,val type: String) : PagedListEpoxyController<GetUserReviewsQuery.Result>() {
    override fun buildItemModel(currentPosition: Int, item: GetUserReviewsQuery.Result?): EpoxyModel<*> {
        var name=""
        var ltrDirection= true
        name = if(type == "aboutYou"){
            if(item?.isAdmin!!){
                context.getString(R.string.verified_by)+ " "+context.getString(R.string.app_name)
            }else{
                item.authorData()?.fragments()?.profileFields()?.firstName()+" "+item.authorData()?.fragments()?.profileFields()?.lastName()
            }
        }else{
            item?.userData()?.fragments()?.profileFields()?.firstName()!!+" "+item?.userData()?.fragments()?.profileFields()?.lastName()
        }
        ltrDirection = context.resources.getBoolean(R.bool.is_left_to_right_layout)


        var profileId=0
        var image=""
        if(item.isAdmin?.not()!!){
            profileId= if(type == "aboutYou"){
                item.authorData()?.fragments()?.profileFields()?.profileId() ?: 0
            }else{
                item.userData()?.fragments()?.profileFields()?.profileId() ?: 0
            }
            image= if(type == "aboutYou"){
                item.authorData()?.fragments()?.profileFields()?.picture() ?: ""
            }else{
                item.userData()?.fragments()?.profileFields()?.picture() ?: ""
            }
        }



        return try{
            ViewholderReviewInfoBindingModel_()
                    .id("viewholder- ${item!!.id()}")
                    .name(name)
                    .comment(item.reviewContent())
                    .imgUrl(image)
                    .isAdmin(item.isAdmin)
                    .type(type)
                    .ltrDirection(ltrDirection)
                    .ratingTotal(item.rating())
                    .reviewsTotal(1)
                    .date(item.createdAt())
                    .profileId(profileId)
                    .onAvatarClick(View.OnClickListener {
                        if((item?.isAdmin)?.not()!!){
                            Utils.clickWithDebounce(it) {
                                if(type == "aboutYou"){
                                    UserProfileActivity.openProfileActivity(this.context, item.authorData()?.fragments()?.profileFields()?.profileId() ?: 0)
                                }else{
                                    UserProfileActivity.openProfileActivity(this.context, item.userData()?.fragments()?.profileFields()?.profileId() ?: 0)
                                }

                            }
                        }
                    })
        }catch (e: Exception){
            ViewholderReviewInfoBindingModel_()
        }
    }
}