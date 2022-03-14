package com.rentall.radicalstart.ui.inbox.msg_detail

import android.content.Context
import android.provider.Settings.System.getConfiguration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rentall.radicalstart.GetThreadsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ViewholderInboxInfoBinding
import com.rentall.radicalstart.databinding.ViewholderInboxReceiverMsgBinding
import com.rentall.radicalstart.databinding.ViewholderInboxSenderMsgBinding
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.Utils.Companion.getCurrentLocale
import com.rentall.radicalstart.util.onClick
import java.security.AccessController.getContext
import java.util.*


class PaginationAdapter(private val hostId: String,
                        private val hostPicture: String?,
                        private val guestPicture: String?,
                        private val sendID: Int?,
                        private val receiverID: Int?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var items: MutableList<GetThreadsQuery.ThreadItem> = ArrayList()

    internal fun addItems(items: List<GetThreadsQuery.ThreadItem>) {
        this.items.addAll(items)
    }

    internal fun addItem(items: GetThreadsQuery.ThreadItem) {
        this.items.add(0, items)
    }

    internal fun removeItems() {
        this.items.clear()
    }

    internal fun setStatus(context: Context,status: String): String{
        when(status){
            "inquiry" -> return context.resources.getString(R.string.Inquiry)
            "preApproved" -> return context.resources.getString(R.string.pre_approved)
            "declined" -> return context.resources.getString(R.string.declined)
            "approved" -> return context.resources.getString(R.string.approved)
            "pending" -> return context.resources.getString(R.string.pending)
            "cancelledByHost" -> return context.resources.getString(R.string.cancelled_by_host)
            "cancelledByGuest"-> return context.resources.getString(R.string.cancelled_by_guest)
            "intantBooking" -> return context.resources.getString(R.string.approved)
            "confirmed" -> return context.resources.getString(R.string.booking_confirmed)
            "expired" -> return context.resources.getString(R.string.expired)
            "requestToBook" -> return context.resources.getString(R.string.request_to_book)
            "completed" -> return context.resources.getString(R.string.completed)
            "reflection" -> return context.resources.getString(R.string.reflection)
            "message" -> return context.resources.getString(R.string.message_small)
        }
        return status
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            when (getItemViewType(position)) {
                R.layout.viewholder_inbox_info -> (holder as ViewHolderInfo).bind(items[position])
                R.layout.viewholder_inbox_sender_msg -> (holder as ViewHolderSender).bind(items[position])
                R.layout.viewholder_inbox_receiver_msg -> (holder as ViewHolderReceiver).bind(items[position])
              //  R.layout.network_state_item -> { (holder as NetworkStateItemViewHolder).bindTo(networkState) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).type() != "message" &&
                    getItem(position).content() == null) {
                R.layout.viewholder_inbox_info
            } else {
                if (getItem(position).sentBy() == hostId) {
                    R.layout.viewholder_inbox_receiver_msg
                } else {
                    R.layout.viewholder_inbox_sender_msg
                }
            }
        }


    private fun getItem(position: Int): GetThreadsQuery.ThreadItem {
        return items[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.viewholder_inbox_info -> {
                val binding = ViewholderInboxInfoBinding.inflate(inflater)
                return ViewHolderInfo(binding)
            }
            R.layout.viewholder_inbox_sender_msg -> {
                val binding = ViewholderInboxSenderMsgBinding.inflate(inflater)
                val  isLeftToRight =parent.context.resources.getBoolean(R.bool.is_left_to_right_layout)
                binding.ltrDirection = isLeftToRight
                return ViewHolderSender(binding)
            }
            R.layout.viewholder_inbox_receiver_msg -> {
                val binding = ViewholderInboxReceiverMsgBinding.inflate(inflater)
                val  isLeftToRight =parent.context.resources.getBoolean(R.bool.is_left_to_right_layout)
                binding.ltrDirection = isLeftToRight
                return ViewHolderReceiver(binding)
            }
         //   R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolderInfo(val binding: ViewholderInboxInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetThreadsQuery.ThreadItem) {
            if (item.startDate() != null && item.endDate() != null) {
                with(binding) {
                    date = Utils.epochToDate(item.startDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!) + " - " + Utils.epochToDate(item.endDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!)
                }
            }
            item.type()?.let {
                binding.setInfo(setStatus(binding.root.context,it))
            }
        }
    }

    inner class ViewHolderReceiver(val binding: ViewholderInboxReceiverMsgBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetThreadsQuery.ThreadItem) {
            with(binding) {
                content = item.content()
                item.createdAt()?.let {
                    date = Utils.epochToDate(it.toLong(), Utils.getCurrentLocale(this.root.context)!!)
                }
                binding.ivInboxReceiverAvatar.onClick {
                    UserProfileActivity.openProfileActivity(binding.root.context, receiverID!!)
                }
                imgAvatar = hostPicture
                infoVisibility = if (item.type() == "message") {
                    false
                } else  {
                    item.type()?.let {
                        setInfo(setStatus(binding.root.context,it))
                    }
                    if (item.startDate() != null && item.endDate() != null) {
                        setInfoDate(Utils.epochToDate(item.startDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!) + " - " + Utils.epochToDate(item.endDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!))
                    }
                    true
                }
            }
        }
    }

    inner class ViewHolderSender(val binding: ViewholderInboxSenderMsgBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetThreadsQuery.ThreadItem) {
            with(binding) {
                content = item.content()
                item.createdAt()?.let {
                    date = Utils.epochToDate(it.toLong(), Utils.getCurrentLocale(this.root.context)!!)
                }
                imgAvatar = guestPicture
                binding.ivInboxSenderAvatar.onClick {
                    UserProfileActivity.openProfileActivity(binding.root.context, sendID!!)
                }
                infoVisibility = if (item.type() == "message") {
                    false
                } else {
                    item.type()?.let {
                        setInfo(setStatus(binding.root.context,it))
                    }
                    if (item.startDate() != null && item.endDate() != null) {
                        setInfoDate(Utils.epochToDate(item.startDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!) + " - " + Utils.epochToDate(item.endDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!))
                    }
                    true
                }
            }
        }
    }
}
