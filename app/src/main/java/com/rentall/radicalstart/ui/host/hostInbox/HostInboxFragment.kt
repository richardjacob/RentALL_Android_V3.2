package com.rentall.radicalstart.ui.host.hostInbox

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.HostFragmentInboxBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.hostInbox.host_msg_detail.HostNewInboxMsgActivity
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.vo.InboxMsgInitData
import javax.inject.Inject

class HostInboxFragment : BaseFragment<HostFragmentInboxBinding, HostInboxViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostFragmentInboxBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_inbox
    override val viewModel: HostInboxViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(HostInboxViewModel::class.java)

    private var pagingController = InboxListController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        if (::mViewModelFactory.isInitialized && isAdded && activity != null)  {
            Log.d("onInbox123", "inbox - value")
        }
        subscribeToLiveData()
    }

    private fun initView() {
//        mBinding.srlInbox.setOnRefreshListener { viewModel.inboxRefresh() }
    }

    private fun subscribeToLiveData() {
        viewModel.loadInbox().observe(this, Observer<PagedList<GetAllThreadsQuery.Result>> { pagedList ->
            pagedList?.let {
                if (mBinding.rvInbox.adapter == pagingController.adapter) {
                    pagingController.submitList(it)
                } else {
                    mBinding.rvInbox.adapter = pagingController.adapter
                    pagingController.submitList(it)
                }
            }
        })

        viewModel.networkState.observe(this, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
//                        mBinding.srlInbox.isRefreshing = false
                        mBinding.rlInboxNomessagePlaceholder.visible()
                        pagingController.isLoading = false
                    }
                    NetworkState.LOADING -> {
                        mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = true
                    }
                    NetworkState.EXPIRED -> {
                        openSessionExpire()
                    }
                    NetworkState.LOADED -> {
//                        mBinding.srlInbox.isRefreshing = false
                        /*mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = false
                        mBinding.ltLoadingView.gone()*/
                        mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = false
                        mBinding.rvInbox.visible()
                        mBinding.ltLoadingView.gone()
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            it.msg?.let {error ->
                                viewModel.handleException(error)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })

       /* viewModel.refreshState.observe(this, Observer {
//            mBinding.srlInbox.isRefreshing = it == NetworkState.LOADING
        })*/
    }

    override fun onDestroyView() {
        mBinding.rvInbox.adapter = null
        super.onDestroyView()
    }

    fun onRefresh() {
       /* if (::mViewModelFactory.isInitialized) {
            *//*if (mBinding.srlInbox.isRefreshing.not()) {
                Log.d("onInbox123", "inbox - refresh")
                viewModel.inboxRefresh()
            }*//*
            viewModel.inboxRefresh()
        }*/
        if (::mViewModelFactory.isInitialized && isAdded && activity != null) {
            Log.d("onInbox123", "inbox - refresh")
            mBinding.rvInbox.gone()
            mBinding.ltLoadingView.visible()
            viewModel.inboxRefresh()
        }
    }

    override fun onRetry() {
        Log.d("onInbox123", "inbox - onRetry")
        if(::mViewModelFactory.isInitialized && isAdded && activity != null) {
            viewModel.inboxRetry()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onInbox123", "onDestroy - Inbox")
    }

    inner class InboxListController : PagedListEpoxyController<GetAllThreadsQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(currentPosition: Int, item: GetAllThreadsQuery.Result?): EpoxyModel<*> {
            try {
                return HostInboxListEpoxyGroup(currentPosition, item!! , clickListener = {

                    HostNewInboxMsgActivity.openInboxMsgDetailsActivity(baseActivity!!, InboxMsgInitData(
                            threadId = item.threadItem()?.threadId()!!,
                            guestId = item.guest()!!,
                            guestName = item.guestProfile()?.displayName()!!,
                            guestPicture = item.guestProfile()?.picture(),
                            hostId = item.host()!!,
                            hostName = item.hostProfile()?.displayName()!!,
                            hostPicture = item.hostProfile()?.picture(),
                            senderID = item.guestProfile()?.profileId()!!,
                            receiverID = item.hostProfile()?.profileId()!!,
                            listID = item.listId()
                    ))
                },avatarClick = {
                    UserProfileActivity.openProfileActivity(context!!, it.guestProfile()?.profileId()!!)
                })
            } catch (e: Exception) {
                e.printStackTrace()
                showError()
            }
            return HostInboxListEpoxyGroup(currentPosition, item!! , clickListener = {},avatarClick = {
            })
        }

        override fun addModels(models: List<EpoxyModel<*>>) {
            try {
                viewholderListingDetailsSectionHeader {
                    id("header")
                    header(resources.getString(R.string.inbox))
                }
                super.addModels(models)
                if (isLoading) {
                    viewholderLoader {
                        id("loading")
                        isLoading(isLoading)
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
                showError()
            }
        }

        init {
            isDebugLoggingEnabled = true
        }

        override fun onExceptionSwallowed(exception: RuntimeException) {
            throw exception
        }
    }
}