package com.rentall.radicalstart.ui.inbox

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.rentall.radicalstart.GetAllThreadsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ViewholderDividerBindingModel_
import com.rentall.radicalstart.ViewholderInboxListMessagesBindingModel_
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import java.util.*

class InboxListEpoxyGroup (
        context : Context,
        currentPosition: Int,
        val item: GetAllThreadsQuery.Result?,
        val clickListener: (item: GetAllThreadsQuery.Result) -> Unit
) : EpoxyModelGroup(R.layout.model_inbox_group, buildModels(context,item, currentPosition, clickListener)) {

    init {
        id("InboxListEpoxyGroup - $currentPosition")
    }

}

fun buildModels(context: Context,item: GetAllThreadsQuery.Result?, currentPosition: Int, clickListener: (item: GetAllThreadsQuery.Result) -> Unit): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    item?.let {
        var isRead = item.threadItem()!!.isRead
        if ((item.threadItem()!!.sentBy() != item.guest())
                && item.threadItem()!!.isRead == false) {
            isRead = false
        }else if(item.threadItem()!!.sentBy()!!.equals(item.guest())){
            isRead = true
        }else{
            isRead = true
        }
        var status = true
        if(item.threadItem()?.type()!!.equals("message")){
            status = false
        }
        models.add(ViewholderInboxListMessagesBindingModel_()
                .id("viewholder- ${item.id()}")
                .status(item.threadItem()?.type()!!)
                .isStatus(status)
                .avatar(item.hostProfile()?.picture())
                .content(item.threadItem()?.content())
                .createdAt(com.rentall.radicalstart.util.Utils.inboxDateFormat(item.threadItem()?.createdAt()!!))
                .hostName(item.hostProfile()?.displayName())
                .isRead(isRead)
                .onClick { _ -> clickListener(item) }
                .avatarClick(View.OnClickListener { _ ->
                    UserProfileActivity.openProfileActivity(context!!, it.hostProfile()?.profileId()!!)
                }))



        models.add(ViewholderDividerBindingModel_()
                .id("viewholder_divider - $currentPosition"))
    }
    return models
}
