package com.rentall.radicalstart.ui.host.hostInbox

import android.view.View
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.rentall.radicalstart.GetAllThreadsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ViewholderDividerBindingModel_
import com.rentall.radicalstart.ViewholderInboxListMessagesBindingModel_
import java.util.*

class HostInboxListEpoxyGroup(
        currentPosition: Int,
        val item: GetAllThreadsQuery.Result,
        val clickListener: (item: GetAllThreadsQuery.Result) -> Unit,
        val avatarClick: (item: GetAllThreadsQuery.Result) -> Unit
) : EpoxyModelGroup(R.layout.model_inbox_group, buildModels(item, currentPosition, clickListener, avatarClick)) {

    init {
        id("InboxListEpoxyGroup - $currentPosition")
    }

}

fun buildModels(item: GetAllThreadsQuery.Result?, currentPosition: Int, clickListener: (item: GetAllThreadsQuery.Result) -> Unit,
                avatarClick: (item: GetAllThreadsQuery.Result) -> Unit): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    item?.let {
        var isRead = item.threadItem()!!.isRead
        if ((item.threadItem()!!.sentBy() == item.guest())
                && item.threadItem()!!.isRead == false) {
            isRead = false
        }else if(item.threadItem()!!.sentBy()!!.equals(item.host())){
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
                .avatar(item.guestProfile()?.picture())
                .content(item.threadItem()?.content())
                .createdAt(com.rentall.radicalstart.util.Utils.inboxDateFormat(item.threadItem()?.createdAt()!!))
                .hostName(item.guestProfile()?.displayName())
                .isRead(isRead)
                .onClick { _ -> clickListener(item) }
                .avatarClick(View.OnClickListener { _ ->
                    avatarClick(item)
                    //UserProfileActivity.openProfileActivity( it.guestProfile()?.profileId()!!)
                }))



        models.add(ViewholderDividerBindingModel_()
                .id("viewholder_divider - $currentPosition"))
    }
    return models
}


/*fun buildModels(context: Context, item: GetAllThreadsQuery.Result, currentPosition: Int, clickListener: (item: GetAllThreadsQuery.Result) -> Unit): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    var isRead = item.threadItem()!!.isRead
    if ((item.threadItem()!!.sentBy() == item.host())
            && item.threadItem()!!.isRead == false) {
        isRead = true
    }
    models.add(ViewholderInboxListMessagesBindingModel_()
            .id("viewholder- ${item.id()}")
            .status(Utils.reservationLabel(item.threadItem()?.type()!!))
            .avatar(item.guestProfile()?.picture())
            .content(item.threadItem()?.content())
            .createdAt(com.rentall.radicalstart.util.Utils.epochToDate(item.threadItem()?.createdAt()!!.toLong()))
            .hostName(item.guestProfile()?.displayName())
            .isRead(isRead)
            .onClick { _ -> clickListener(item) }
            .avatarClick(View.OnClickListener { _ ->
                UserProfileActivity.openProfileActivity(context!!, it.hostProfile()?.profileId()!!)
            })


    models.add(ViewholderDividerBindingModel_()
            .id("viewholder_divider - $currentPosition"))

    return models
}*/
