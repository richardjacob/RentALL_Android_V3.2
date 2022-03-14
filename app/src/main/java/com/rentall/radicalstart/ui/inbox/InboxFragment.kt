package com.rentall.radicalstart.ui.inbox

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
import com.rentall.radicalstart.databinding.FragmentInboxBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.inbox.msg_detail.NewInboxMsgActivity
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.vo.InboxMsgInitData
import timber.log.Timber
import javax.inject.Inject

class InboxFragment : BaseFragment<FragmentInboxBinding, InboxBoxViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentInboxBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_inbox
    override val viewModel: InboxBoxViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(InboxBoxViewModel::class.java)

    private var pagingController = InboxListController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        if (::mViewModelFactory.isInitialized && isAdded && activity != null) {
            Log.d("onInbox123", "inbox - value")
        }
        subscribeToLiveData()
        mBinding.startExlporing.onClick {
            (baseActivity as HomeActivity).viewDataBinding?.let {
                it.vpHome.setCurrentItem(0, false)
                it.bnExplore.setCurrentItem(0, false)
            }
        }
    }

    private fun initView() {
      /*  mBinding.srlInbox.setOnRefreshListener {
            viewModel.isRefreshing =true
            viewModel.inboxRefresh()
        }*/
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
                    }
                    NetworkState.LOADING -> {
                        mBinding.rlInboxNomessagePlaceholder.gone()
                        if(viewModel.isRefreshing) {
                            pagingController.isLoading = true
                        }
                    }
                    NetworkState.EXPIRED -> {
                        openSessionExpire()
                    }
                    NetworkState.LOADED -> {
//                        mBinding.srlInbox.isRefreshing = false
                        mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = false
                        mBinding.rvInbox.visible()
                        mBinding.ltLoadingView.gone()
                        if(viewModel.isRefreshing){
                            viewModel.isRefreshing = false
                        }
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
          *//*  if(viewModel.isRefreshing) {
                mBinding.srlInbox.isRefreshing = it == NetworkState.LOADING
            }*//*
        })*/
    }

    override fun onDestroyView() {
        mBinding.rvInbox.adapter = null
        super.onDestroyView()
    }

   /* override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser){
            onRefresh()
        }
    }*/

    fun onRefresh() {
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
            try{
                return InboxListEpoxyGroup(context!!,currentPosition, item , clickListener = {
                    NewInboxMsgActivity.openInboxMsgDetailsActivity(baseActivity!!, InboxMsgInitData(
                            threadId = item!!.threadItem()?.threadId()!!,
                            guestId = item.guest()!!,
                            guestName = item.guestProfile()?.displayName()!!,
                            guestPicture = item.guestProfile()?.picture(),
                            hostId = item.host()!!,
                            hostName = item.hostProfile()?.displayName()!!,
                            hostPicture = item.hostProfile()?.picture(),
                            senderID = item.guestProfile()!!.profileId()!!,
                            receiverID = item.hostProfile()!!.profileId()!!,
                            listID = item.listId()
                    ))
                })
            }catch (e: Exception){
                Timber.d("CrashException12 $e")
                e.printStackTrace()
                showError()
            }
            return  InboxListEpoxyGroup(mBinding.ivNoInbox.context, currentPosition, item, clickListener = {})
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
            } catch (e: Exception) {
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