package com.rentall.radicalstart.ui.inbox.msg_detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.rentall.radicalstart.GetThreadsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.adapter.NetworkStateItemViewHolder
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.databinding.ViewholderInboxInfoBinding
import com.rentall.radicalstart.databinding.ViewholderInboxReceiverMsgBinding
import com.rentall.radicalstart.databinding.ViewholderInboxSenderMsgBinding
import com.rentall.radicalstart.util.Utils

class InboxMsgAdapter(
        private val hostId: String,
        private val hostPicture: String?,
        private val guestPicture: String?,
        private val sendID: Int?,
        private val receiverID: Int?,
        private val clickCallback: (item: GetThreadsQuery.ThreadItem) -> Unit,
        private val retryCallback: () -> Unit
        )
    : PagedListAdapter<GetThreadsQuery.ThreadItem, androidx.recyclerview.widget.RecyclerView.ViewHolder>(NOTIFICATION_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        try {
            when (getItemViewType(position)) {
                R.layout.viewholder_inbox_info -> (holder as ViewHolderInfo).bind(getItem(position.minus(getExtraRow()))!!)
                R.layout.viewholder_inbox_sender_msg -> (holder as ViewHolderSender).bind(getItem(position.minus(getExtraRow()))!!)
                R.layout.viewholder_inbox_receiver_msg -> (holder as ViewHolderReceiver).bind(getItem(position.minus(getExtraRow()))!!)
                R.layout.network_state_item -> { (holder as NetworkStateItemViewHolder).bindTo(networkState) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
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
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
       // Log.d("getItemViewType",  "f "+ networkState!!.status.name + position)
        return if (hasExtraRow() && position == 0) {
          //  Log.d("inboxAdapter - ", "network_state_item")
            R.layout.network_state_item
        } else {
           // Log.d("inboxAdapter - ", position.toString() + "  " + itemCount)
            if (position + getExtraRow() != itemCount &&
                    getItem(position)?.type() != "message" &&
                    getItem(position)?.content() == null) {
                R.layout.viewholder_inbox_info
            } else {
                if (position + getExtraRow() != itemCount && getItem(position)?.sentBy() == hostId) {
                    R.layout.viewholder_inbox_receiver_msg
                } else {
                    R.layout.viewholder_inbox_sender_msg
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    private fun getExtraRow(): Int {
        return if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(0)
        }
    }

    inner class ViewHolderInfo(val binding: ViewholderInboxInfoBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetThreadsQuery.ThreadItem) {
            if (item.startDate() != null && item.endDate() != null) {
                with(binding) {
                    date = Utils.epochToDate(item.startDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!) + " - " + Utils.epochToDate(item.endDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!)
                }
            }
            item.type()?.let {
                binding.setInfo(Utils.reservationLabel(it))
            }
        }
    }

    inner class ViewHolderReceiver(val binding: ViewholderInboxReceiverMsgBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetThreadsQuery.ThreadItem) {
            with(binding) {
                content = item.content()
                item.createdAt()?.let {
                    date = Utils.epochToDate(it.toLong(), Utils.getCurrentLocale(this.root.context)!!)
                }
                imgAvatar = hostPicture
                /*receiverClick = View.OnClickListener {
                    UserProfileActivity.openProfileActivity(binding.root.context, receiverID!!)
                }*/
                infoVisibility = if (item.type() == "message") {
                    false
                } else  {
                    item.type()?.let {
                        setInfo(Utils.reservationLabel(it))
                    }
                    if (item.startDate() != null && item.endDate() != null) {
                        setInfoDate(Utils.epochToDate(item.startDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!) + " - " + Utils.epochToDate(item.endDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!))
                    }
                    true
                }
            }
        }
    }

    inner class ViewHolderSender(val binding: ViewholderInboxSenderMsgBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetThreadsQuery.ThreadItem) {
            with(binding) {
                content = item.content()
                item.createdAt()?.let {
                    date = Utils.epochToDate(it.toLong(), Utils.getCurrentLocale(this.root.context)!!)
                }
                imgAvatar = guestPicture

                /*senderClick = View.OnClickListener {
                    UserProfileActivity.openProfileActivity(binding.root.context, sendID!!)
                }*/
                infoVisibility = if (item.type() == "message") {
                    false
                } else  {
                    item.type()?.let {
                        setInfo(Utils.reservationLabel(it))
                    }
                    if (item.startDate() != null && item.endDate() != null) {
                        setInfoDate(Utils.epochToDate(item.startDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!) + " - " + Utils.epochToDate(item.endDate()!!.toLong(), Utils.getCurrentLocale(this.root.context)!!))
                    }
                    true
                }
            }
        }
    }

    companion object {
        private val PAYLOAD_SCORE = Any()
        val NOTIFICATION_COMPARATOR = object : DiffUtil.ItemCallback<GetThreadsQuery.ThreadItem>() {
            override fun areContentsTheSame(oldItem: GetThreadsQuery.ThreadItem, newItem: GetThreadsQuery.ThreadItem): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: GetThreadsQuery.ThreadItem, newItem: GetThreadsQuery.ThreadItem): Boolean =
                    oldItem.id() == newItem.id()
        }
    }
}
