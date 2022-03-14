package com.rentall.radicalstart.ui.inbox.msg_detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.GetThreadsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.SendMessageMutation
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.databinding.ActivityInboxMessagesBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.inbox.InboxNavigator
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.vo.InboxMsgInitData
import org.jetbrains.annotations.Nullable
import javax.inject.Inject

class InboxMsgActivity : BaseActivity<ActivityInboxMessagesBinding, InboxMsgViewModel>(), InboxNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityInboxMessagesBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_inbox_messages
    override val viewModel: InboxMsgViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(InboxMsgViewModel::class.java)

    private lateinit var adapter: InboxMsgAdapter

    companion object {
        @JvmStatic fun openInboxMsgDetailsActivity(activity: Activity, inboxMsgData: InboxMsgInitData) {
            val intent = Intent(activity, InboxMsgActivity::class.java)
            intent.putExtra("inboxInitData", inboxMsgData)
            activity.startActivityForResult(intent, 53)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        topView = mBinding.rlInboxDetailSendMsg
        viewModel.setInitialData(intent)
        initView()
        subscribeToLiveData()
        mBinding.tvNewMsgPill.invisible()
        mBinding.tvNewMsgPill.onClick {
            mBinding.tvNewMsgPill.slideDown()
            viewModel.notificationRefresh()
        }
    }

    private fun initView() {
        mBinding.tvInboxSend.onClick {
            checkNetwork { viewModel.sendMsg() }
        }
        mBinding.toolbarInbox.ivCameraToolbar.gone()
        mBinding.toolbarInbox.ivNavigateup.onClick { finish() }
        mBinding.toolbarInbox.tvToolbarHeading.text = resources.getString(R.string.message)
        val layout = LinearLayoutManager(this)
        layout.stackFromEnd = true
        mBinding.rvInboxDetails.layoutManager = layout
    }

    private fun subscribeToLiveData() {
        viewModel.inboxInitData.observe(this, Observer { initData ->
            initData?.let { it ->
                adapter = InboxMsgAdapter(
                        it.hostId,
                        it.hostPicture,
                        it.guestPicture,
                        it.senderID,
                        it.receiverID,
                        clickCallback = { },
                        retryCallback = { viewModel.notificationRetry() })
                mBinding.rvInboxDetails.adapter = adapter
                viewModel.getInboxMsg()
            }
        })

        viewModel.posts.observe(this, Observer<PagedList<GetThreadsQuery.ThreadItem>> { pagedList ->
            pagedList?.let {
                adapter.submitList(it)
               // readMessage()
            }
        })

        viewModel.networkState.observe(this, Observer {
            it?.let { networkState ->
             //   Log.d("netword", it.status.name)
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {}
                    NetworkState.LOADING -> {
               //         Log.d("netword12345", it.status.name)
                        adapter.setNetworkState(it)
                    }
                    NetworkState.LOADED -> {
                 //       Log.d("netword123456789", it.status.name)
                         adapter.setNetworkState(it)
                    }
                    NetworkState.FAILED -> {
                   //     Log.d("netword123", it.status.name)
                        adapter.setNetworkState(it)
                    }
                    else -> {
                     //   Log.d("netword1", it.status.name)
                        adapter.setNetworkState(it)
                    }
                }
            }
        })

        viewModel.isNewMessage.observe(this, Observer {
            it?.let { gotNewMsg ->
                if (gotNewMsg) mBinding.tvNewMsgPill.slideUp()
            }
        })
    }

    private fun readMessage() {
        try {
            if(viewModel.posts.value!!.size <= 10) {
                viewModel.readMessage()
                viewModel.newMsg()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun openListingDetails() {

    }

    override fun moveToBackScreen() {
        val intent = Intent()
        setResult(23, intent)
    }

    override fun onRetry() {
        viewModel.sendMsg()
    }

    override fun onBackPressed() {
        viewModel.dataManager.deleteMessage()
        super.onBackPressed()
    }

    override fun addMessage(text: @Nullable SendMessageMutation.Results) {
        Log.d("DSfa","SADf")
        val list = viewModel.posts.value
        /*list?.add(0, GetThreadsQuery.ThreadItem(
                "",23,32, 23, text, "233", "23", "322", "2332", "23"
        ))
        viewModel.posts.value?.add(0, GetThreadsQuery.ThreadItem(
         "",23,32, 23, text, "233", "23", "322", "2332", "23"
        ))*/
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun hideTopView(msg: String) {

    }

    override fun openBillingActivity() {

    }
}