package com.rentall.radicalstart.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentSavedDetailBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.base.BaseNavigator
import com.rentall.radicalstart.ui.explore.ExploreFragment
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.util.*
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.vo.ListingInitData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class SavedDetailFragment : BaseFragment<FragmentSavedDetailBinding, SavedViewModel>(), SavedNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentSavedDetailBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_saved_detail
    override val viewModel: SavedViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(SavedViewModel::class.java)
    private lateinit var pagingController1 : SavedDetailsController
    private var name: String? = null
    private var id: Int? = null
    private var count: Int? = null

    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem: Int = 0
    private var totalItemCount:Int = 0
    private var isLoadedAll = false
    private lateinit var disposable: Disposable
    private var activityResultFinder=0

    enum class State {
        EXPANDED,
        IDLE
    }

    companion object {
        private const val GROUPID = "param1"
        private const val GROUPNAME = "param2"
        private const val GROUPTYPE = "param3"
        private const val HOMECOUNT = "param4"
        @JvmStatic
        fun newInstance(type: Int, name: String, listtype: Int, count: Int) =
                SavedDetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt(GROUPID, type)
                        putString(GROUPNAME, name)
                        putInt(GROUPTYPE, listtype)
                        putInt(HOMECOUNT, count)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        arguments?.let {
            name = it.getString(GROUPNAME)
            id = it.getInt(GROUPID)
            count = it.getInt(HOMECOUNT)
            viewModel.wishlistType = it.getInt(GROUPTYPE)
        }
        viewModel.listId.value = id
        /*if(::pagingController.isInitialized.not()) {
            try {
                pagingController = WishListController(
                        id!!,
                        name!!,
                        viewModel.getCurrencyBase(),
                        viewModel.getCurrencyRates(),
                        viewModel.getUserCurrency()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }*/

        if(::pagingController1.isInitialized.not()) {
            try {
                pagingController1 = SavedDetailsController(
                        id!!,
                        name!!,
                        viewModel.getCurrencyBase(),
                        viewModel.getCurrencyRates(),
                        viewModel.getUserCurrency(),
                        clickListener = {item , itView->
                            Utils.clickWithDebounce(itView){
                                try {
                                    val currency = currencyConverter(viewModel.getCurrencyBase(), viewModel.getCurrencyRates(), viewModel.getUserCurrency(), item.currency, item.basePrice)
                                    val photo = ArrayList<String>()
                                    photo.add(item.listPhotoName!!)
                                    val intent = Intent(context, ListingDetails::class.java)
                                    intent.putExtra("listingInitData", ListingInitData(
                                            item.title, photo , item.id, item.roomType!!,
                                            item.reviewsStarRating, item.reviewsCount, currency,
                                            0,
                                            selectedCurrency = viewModel.getUserCurrency(),
                                            currencyBase = viewModel.getCurrencyBase(),
                                            currencyRate = viewModel.getCurrencyRates(),
                                            startDate = "0",
                                            endDate = "0",
                                            bookingType = item.bookingType,
                                            isWishList = item.wishListStatus!!
                                    ))
                                    startActivityForResult(intent, 90)
                                } catch (e: KotlinNullPointerException) { showError() }
                            }
                        },
                        onWishListClick = {item->
                            viewModel.retryCalled = "create-${item.id}-${viewModel.listId.value!!}-false-true"
                            viewModel.createWishList(item.id, viewModel.listId.value!!, false, true)
                        },
                        retryListener = {

                        }
                )
                val layoutManager = LinearLayoutManager(activity)
                mBinding.rvSavedDetail.layoutManager = layoutManager
                mBinding.rvSavedDetail.setController(pagingController1)
                pagingController1.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        if(positionStart == 0) {
                            layoutManager.scrollToPosition(0)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mBinding.ivClose.onClick {
            baseActivity?.onBackPressed()
        }
        EpoxyVisibilityTracker().attach(mBinding.rvSavedDetail)
        mBinding.ivOption.onClick {
            val popup = PopupMenu(requireContext(), mBinding.ivOption, Gravity.END, R.attr.actionOverflowMenuStyle, 0)
            popup.menuInflater.inflate(R.menu.saved_details_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.delete -> {
                        showAlertDialog()
                    }
                   /* R.id.make_it_public -> {
                        viewModel.updateWishListGroup(id!!)
                    }*/
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
        mBinding.tvStartExplore.onClick {
            Timber.d("hellowrong1!!!")
            (baseActivity as HomeActivity).viewDataBinding?.let {
                (baseActivity as HomeActivity).setExplore()
            }
        }
       // mBinding.srlSavedDetails.setOnRefreshListener { refreshListing() }
        subscribeToLiveData()
        setUpLoadMoreListener()
        subscribeForData()
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_list))
                .setMessage(resources.getString(R.string.are_you_sure_you_want_to_delete)+ " $name?")
                .setPositiveButton(resources.getString(R.string.DELETE)) { _, _ ->
                    viewModel.retryCalled = "delete-$id"
                    viewModel.deleteWishListGroup(id!!) }
                .setNegativeButton(resources.getString(R.string.CANCEL)) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun subscribeToLiveData() {
        id?.let {
            /*viewModel.loadwishList(it).observe(this, Observer<PagedList<GetWishListGroupQuery.WishList>> { pagedList ->
                pagedList?.let { result ->
                    if (::pagingController.isInitialized) {
                        if (mBinding.rvSavedDetail.adapter == pagingController.adapter) {
                            pagingController.submitList(result)
                        } else {
                            mBinding.rvSavedDetail.adapter = pagingController.adapter
                            pagingController.submitList(result)
                        }
                    }
                }
            })*/
        }

       /* viewModel.wishListNetworkState.observe(this, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.srlSavedDetails.isRefreshing = false
                        pagingController.isLoading = false
                        mBinding.rlSaveNoListPlaceholder.visible()
                        mBinding.rvSavedDetail.gone()
                       // mBinding.ivOption.gone()
                    }
                    NetworkState.LOADING -> {
                        mBinding.rlSaveNoListPlaceholder.gone()
                        pagingController.isLoading = true
                    }
                    NetworkState.LOADED -> {
                        mBinding.srlSavedDetails.isRefreshing = false
                        mBinding.rlSaveNoListPlaceholder.gone()
                        mBinding.rvSavedDetail.visible()
                        pagingController.isLoading = false
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            it.msg?.let {thr ->
                                viewModel.handleException(thr)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })*/

        viewModel.wishListNetworkState.observe(viewLifecycleOwner, Observer {
         //   mBinding.srlSavedDetails.isRefreshing = it == NetworkState.LOADING
        })

        viewModel.searchPageResult12.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    mBinding.rvSavedDetail.visible()
                    mBinding.rlSaveNoListPlaceholder.gone()
                    pagingController1.list = it
                    pagingController1.requestModelBuild()
                    mBinding.flSearchLoading.gone()
                } else {
                    if (viewModel.firstSetValue.value!!) {
                        mBinding.rlSaveNoListPlaceholder.visible()
                        if(activityResultFinder==1){
                            activityResultFinder=0
                        }else{
                            mBinding.flSearchLoading.gone()
                        }
                        mBinding.rvSavedDetail.gone()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        mBinding.rvSavedDetail.adapter = null
        super.onDestroyView()
    }

    fun onRefresh() {
        if (::mViewModelFactory.isInitialized) {
            refreshListing()
        }
    }

    fun refreshListing() {
        pageNumber = 1
        isLoadedAll = false
        lastVisibleItem = 0
        totalItemCount = 0
        viewModel.searchPageResult12.value = ArrayList()
        setUpLoadMoreListener()
        subscribeForData()
    }

    override fun onRetry() {
        if(::mViewModelFactory.isInitialized) {
            if(viewModel.retryCalled.contains("delete")){
                val text = viewModel.retryCalled.split("-")
                viewModel.deleteWishListGroup(text[1].toInt())
            }else if(viewModel.retryCalled.contains("create")){
                val text1 = viewModel.retryCalled.split("-")
                if(text1.size == 5){
                    viewModel.createWishList(text1[1].toInt(), text1[2].toInt(), false, true)
                }else {
                    viewModel.wishListGroup.value?.forEachIndexed { index, s ->
                        if (s.isRetry.equals(viewModel.retryCalled)) {
                            val text = s.isRetry.split("-")
//                            if (text.size == 5) {
//                                viewModel.createWishList(text[1].toInt(), text[2].toInt(), false, true)
//                            } else {
                                viewModel.createWishList(text[1].toInt(), text[2].toInt(), text[3].toBoolean())
//                            }
                        }
                    }
                }

            }else {
                subscribeForData()
            }
        }
    }

    override fun moveUpScreen() {
        baseActivity?.onBackPressed()
    }

    fun currencyConverter(base: String, rate: String, userCurrency: String, currency: String, total: Double): String {
        return BindingAdapters.getCurrencySymbol(userCurrency) + Utils.formatDecimal(CurrencyUtil.getRate(
                base = base,
                to = userCurrency,
                from = currency,
                rateStr = rate,
                amount = total
        ))
    }

    override fun showEmptyMessageGroup() {

    }

    override fun reloadExplore() {
        (baseActivity as HomeActivity).setWishList(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (89 == resultCode && 90 == requestCode) {
           // viewModel.wishListRefresh()
        }
        pageNumber = 1
        isLoadedAll = false
        activityResultFinder=1
        lastVisibleItem = 0
        totalItemCount = 0
        viewModel.searchPageResult12.value = ArrayList()
        subscribeForData()
    }

    private fun setUpLoadMoreListener() {
        mBinding.rvSavedDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView,
                                    dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = mBinding.rvSavedDetail.layoutManager!!.itemCount
                lastVisibleItem = (mBinding.rvSavedDetail.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()
                if (!isLoadedAll && !loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    pageNumber++
                    paginator.onNext(pageNumber)
                    loading = true
                }
            }
        })
    }

    private fun subscribeForData() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }
        disposable = paginator
                .onBackpressureDrop()
                .doOnNext { page ->
                    loading = true
                    if (page == 1) {
                        mBinding.flSearchLoading.visible()
                        mBinding.rlSaveNoListPlaceholder.gone()
                    }
                    viewModel.increaseCurrentPage(page)
                    pagingController1.isLoading = true
                }
                .concatMapSingle { page ->
                    viewModel.getSavedDetails()
                              //.delay(5, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .doOnError { throwable ->
                                // showToast("Something went wrong. Please try again.${throwable.printStackTrace()}")
                            }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ items ->
                    pagingController1.isLoading = false
                    if (items!!.data()!!.wishListGroup!!.status() == 200) {
                        viewModel.firstSetValue.value = true

                        if (items.data()!!.wishListGroup!!.results()!!.wishLists()!!.isNotEmpty()) {
                            if (items.data()!!.wishListGroup!!.results()!!.wishLists()!!.size < 10) {
                                isLoadedAll = true
                            }
                            viewModel.setSavedData(items.data()!!.wishListGroup!!.results()!!.wishLists()!!)
                        } else {
                            if (pageNumber == 1) {
                                mBinding.flSearchLoading.gone()
                                mBinding.rlSaveNoListPlaceholder.visible()
                                mBinding.rvSavedDetail.gone()
                            } else {
//                                viewModel.firstSetValue.value = true
                            }
                            isLoadedAll = true
                        }
                    }  else if(items!!.data()!!.wishListGroup!!.status() == 500) {
                       openSessionExpire()
                    }
                    else {
                        if (pageNumber == 1) {
                            mBinding.flSearchLoading.gone()
                            viewModel.firstSetValue.value = true
                            mBinding.rlSaveNoListPlaceholder.visible()
                            mBinding.rvSavedDetail.gone()
                        }
                        isLoadedAll = true
                    }
                    loading = false
//                        fl_search_loading.gone()
                }, { t -> viewModel.handleException(t)  })
        compositeDisposable.add(disposable)
        paginator.onNext(pageNumber)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}