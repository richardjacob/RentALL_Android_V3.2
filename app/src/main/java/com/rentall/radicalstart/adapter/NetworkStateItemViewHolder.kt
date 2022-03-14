package com.rentall.radicalstart.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.apollographql.apollo.exception.ApolloNetworkException
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.Status

class NetworkStateItemViewHolder(view: View,
                                 private val retryCallback: () -> Unit)
    : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<LottieAnimationView>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMsg = view.findViewById<TextView>(R.id.error_msg)

    init {
        retry.setOnClickListener {
            retryCallback()
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindTo(networkState: NetworkState?) {
        retry.visibility = toVisbility(networkState?.status == Status.FAILED)
        progressBar.visibility = toVisbility(networkState?.status == Status.RUNNING)
        Log.d("NetworkItemViewHolder", "kkk"+networkState?.status?.name)
        errorMsg.visibility = toVisbility(networkState?.msg != null)
        if (networkState?.msg is ApolloNetworkException) {
            errorMsg.text = "Your are Currently Offline.."
        } else {
            errorMsg.text = "Something went wrong.."
        }
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(view, retryCallback)
        }

        fun toVisbility(constraint : Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}