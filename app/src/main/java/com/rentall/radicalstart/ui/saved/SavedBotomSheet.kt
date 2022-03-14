package com.rentall.radicalstart.ui.saved

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ViewholderSavedListCarouselBindingModel_
import com.rentall.radicalstart.databinding.FragmentSavedBottomsheetBinding
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseBottomSheet
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.ui.saved.createlist.CreateListActivity
import com.rentall.radicalstart.util.listingSimilarCarousel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderCenterTextPlaceholder
import com.rentall.radicalstart.vo.SavedList
import javax.inject.Inject


class SavedBotomSheet : BaseBottomSheet<FragmentSavedBottomsheetBinding, SavedViewModel>(), SavedNavigator {
    override fun reloadExplore() {

    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_saved_bottomsheet
    override val viewModel: SavedViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(SavedViewModel::class.java)
    lateinit var mBinding: FragmentSavedBottomsheetBinding
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory
    private var listId = 0
    private var isSimilar = false
    private var listImage = ""
    private var listGroupCount = 0
    private var savedList = ArrayList<SavedList>()

    companion object {
        private const val LISTID = "param1"
        private const val ISSIMILAR = "param2"
        private const val LISTIMAGE = "param3"
        private const val LISTGROUPCOUNT = "param4"
        @JvmStatic
        fun newInstance(type: Int, listImage: String, isSimilar: Boolean = false, groupCount: Int? = 0) =
                SavedBotomSheet().apply {
                    arguments = Bundle().apply {
                        putInt(LISTID, type)
                        groupCount?.let { putInt(LISTGROUPCOUNT, it) }
                        putString(LISTIMAGE, listImage)
                        putBoolean(ISSIMILAR, isSimilar)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        arguments?.let {
            listId = it.getInt(LISTID)
            listGroupCount = it.getInt(LISTGROUPCOUNT)
            isSimilar = it.getBoolean(ISSIMILAR)
            listImage = it.getString(LISTIMAGE, "")
        }
        viewModel.setListDetails(listId, listImage, listGroupCount)
        snapHelperFactory = object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                return PagerSnapHelper()
            }
        }
        mBinding.ivAdd.onClick {
            val intent = Intent(context, CreateListActivity::class.java)
            startActivityForResult(intent, 35)
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.loadWishListGroup().observe(this, Observer<List<SavedList>> { it ->
            it?.let {
                savedList = ArrayList(it)
               // Log.d("cs123","saaaa${savedList.size} ${it.size}")
                if (mBinding.rvSavedWishlist.adapter != null) {
                    mBinding.rvSavedWishlist.requestModelBuild()
                } else {
                    setup()
                    //scrollPosition()
                }
            }
        })

        /*viewModel.wishListGroup.observe(this, Observer {
            it?.let {
                for (i in 0 until it.size) {
                    if (it[i].progress == AuthViewModel.LottieProgress.LOADING) {
                        Log.d("jhg", this.isCancelable.toString())
                        this.isCancelable = false
                        Log.d("jhg123", this.isCancelable.toString())
                        break
                    } else {
                        this.isCancelable = true
                    }
                }
            }
        })*/

        /*viewModel.isLoadingInProgess.observe(this, Observer {
            it?.let {
                Log.d("jhg", this.isCancelable.toString())
                this.isCancelable = false // it <= 0
                Log.d("jhg123", this.isCancelable.toString())
            }
        })*/
    }

    private fun setup() {
        mBinding.rvSavedWishlist.withModels {
            if (savedList.isNotEmpty()) {
                listingSimilarCarousel {
                    id("SimilarCarousel11")
                    Carousel.setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                            models(buildModel())
                }
            } else {
                viewholderCenterTextPlaceholder {
                    id("noresult")
                    header(resources.getString(R.string.no_wishlist_groups_added_please_create_group_by_click_add_icon))
                    large(false)
                    switcher(false)
                }
            }
        }
    }

    private fun buildModel(): MutableList<out EpoxyModel<*>> {
        val models = ArrayList<EpoxyModel<*>>()
       // Log.d("savedListSize123",  " "+ savedList.size.toString())
        for (i in 0 until savedList.size) {
           // Log.d("savedListSize123", savedList[i].id.toString() +" "+ savedList.size.toString())
            models.add(ViewholderSavedListCarouselBindingModel_()
                    .id("savedList - ${savedList[i].id}")
                    .url(savedList[i].img)
                    .name(savedList[i].title)
                    .wishListStatus(savedList[i].isWishList)
                    .wishListCount(savedList[i].wishListCount)
                    .progress(savedList[i].progress)
                    .isRetry(savedList[i].isRetry)
                    .onRetryClick(View.OnClickListener {
                        savedList[i].progress = AuthViewModel.LottieProgress.LOADING
                        savedList[i].isRetry = savedList[i].id.toString()
                        mBinding.rvSavedWishlist.requestModelBuild()
                        viewModel.retryCalled = "create-$listId-${savedList[i].id}-${savedList[i].isWishList.not()}"
                        viewModel.createWishList(listId, savedList[i].id, savedList[i].isWishList.not())
                    })
                    .heartClickListener(View.OnClickListener {
                        //Log.d("savedListSize123", savedList[i].id.toString() +" "+ savedList.size.toString())
                        savedList[i].progress = AuthViewModel.LottieProgress.LOADING
                        mBinding.rvSavedWishlist.requestModelBuild()
                        viewModel.retryCalled = "create-$listId-${savedList[i].id}-${savedList[i].isWishList.not()}"
                        viewModel.createWishList(listId, savedList[i].id, savedList[i].isWishList.not())
                       // mBinding.rvSavedWishlist.requestModelBuild()
                        this.isCancelable = true
                    })
                    .clickListener(View.OnClickListener {

                    }))
        }
        return models
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 2) {
            mBinding.rvSavedWishlist.clear()
            viewModel.getAllWishListGroup()
        }
    }

    override fun onRetry() {
        viewModel.loadWishListGroup()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

       /* if (viewModel.getIsWishListAdded()) {
            Log.d("Adsva432", "dsv"+ viewModel.listGroupCount.value.toString())
            if (viewModel.screen.value!! == "Home") {
                //Log.d("Adsva4", "sfs" + viewModel.listGroupCount.value.toString())
                if (viewModel.listGroupCount.value!! > 0) {
                    RxBus.publish(UiEvent.WishList(viewModel.listId.value!!, true, viewModel.listGroupCount.value!!, "Home"))
                    //(baseActivity as HomeActivity).refreshExploreItems(viewModel.listId.value, true, viewModel.listGroupCount.value!!)
                } else {
                    RxBus.publish(UiEvent.WishList(viewModel.listId.value!!, false, viewModel.listGroupCount.value!!, "Home"))
                    //  (baseActivity as HomeActivity).refreshExploreItems(viewModel.listId.value, false, viewModel.listGroupCount.value!!)
                }
            } else if (viewModel.screen.value!! == "List") {
                if (viewModel.isSimilar.value!!) {
                    RxBus.publish(UiEvent.WishList(viewModel.listId.value!!, false, viewModel.listGroupCount.value!!, "similar"))
                    //   (baseActivity as ListingDetails).loadSimilarDetails()
                } else {
                    RxBus.publish(UiEvent.WishList(viewModel.listId.value!!, false, viewModel.listGroupCount.value!!, "list"))
                    //  (baseActivity as ListingDetails).loadListingDetails()
                }
            }
        }*/
       // (baseActivity as HomeActivity).refreshExploreItems(viewModel.listId.value)
        Log.d("Adsva4321", viewModel.getIsWishListAdded().toString() +" - "+ viewModel.listGroupCount.value.toString())
        if (viewModel.getIsWishListAdded()) {
            Log.d("Adsva432", "dsv"+ viewModel.listGroupCount.value.toString())
            if (baseActivity is HomeActivity) {
                Log.d("Adsva4", "sfs" + viewModel.listGroupCount.value.toString())
                if (viewModel.listGroupCount.value!! > 0) {
                    (baseActivity as HomeActivity).refreshExploreItems(viewModel.listId.value, true, viewModel.listGroupCount.value!!)
                } else {
                    (baseActivity as HomeActivity).refreshExploreItems(viewModel.listId.value, false, viewModel.listGroupCount.value!!)
                }
            } else if (baseActivity is ListingDetails) {
                if (isSimilar) {
                    (baseActivity as ListingDetails).loadSimilarDetails()
                } else {
                    (baseActivity as ListingDetails).loadListingDetails()
                }
            }
        }
    }

    /*fun scrollPosition() {
        mBinding.rvSavedWishlist.adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
              //  super.onItemRangeChanged(positionStart, itemCount)
                Log.d("kjh", "iuasdsdv$positionStart")
                mBinding.rvSavedWishlist.layoutManager?.scrollToPosition(-1)
              *//*  if (positionStart == 0) {
                    mBinding.rvSavedWishlist.layoutManager?.scrollToPosition(0)
                }*//*
            }
        })
    }*/

    override fun moveUpScreen() {

    }

    override fun showEmptyMessageGroup() {

    }
}