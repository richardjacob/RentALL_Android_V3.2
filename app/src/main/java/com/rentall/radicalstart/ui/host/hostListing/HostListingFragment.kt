package com.rentall.radicalstart.ui.host.hostListing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.DataManager
import com.rentall.radicalstart.databinding.HostListingFragmentBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.HostFinalActivity
import com.rentall.radicalstart.ui.host.step_one.StepOneActivity
import com.rentall.radicalstart.ui.listing.ListingDetails
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.visible
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderManageList
import com.rentall.radicalstart.viewholderUserName
import com.rentall.radicalstart.vo.ListingInitData
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class HostListingFragment : BaseFragment<HostListingFragmentBinding, HostListingViewModel>(),HostListingNavigator {


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostListingFragmentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_listing_fragment
    override val viewModel: HostListingViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(HostListingViewModel::class.java)

    var completedPercent : Int = 0
    var progressLoaded : Boolean = true
    var completedLoaded : Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.actionBar.ivAddList.visible()
        mBinding.actionBar.backToLsit.gone()
        subscribeToLiveData()
        mBinding.actionBar.ivAddList.setOnClickListener {
            val intent = Intent(activity, StepOneActivity::class.java)
            activity?.startActivity(intent)
        }
        mBinding.postList.setOnClickListener {
            val intent = Intent(activity, StepOneActivity::class.java)
            activity?.startActivity(intent)
        }
        mBinding.actionBar.backToLsit.setOnClickListener {
            mBinding.actionBar.backToLsit.gone()
            mBinding.actionBar.listTitle.visible()
            mBinding.actionBar.ivAddList.visible()
            mBinding.srlManageList.visible()
            mBinding.rvManageList.visible()
            mBinding.rlNoListingMsg.gone()
            mBinding.ll404Page.gone()
            onRefresh()
        }
    }

    fun subscribeToLiveData(){
        mBinding.rvManageList.gone()
        mBinding.ltLoading.visible()
        mBinding.srlManageList.setOnRefreshListener {
            completedLoaded = true
            progressLoaded = true
            mBinding.rvManageList.gone()
            //mBinding.ltLoading.visible()
            viewModel.listRefresh()
        }
       // viewModel.loadListing()
        viewModel.loadListing().observe(viewLifecycleOwner, Observer {
            it.let {
                if(mBinding.rvManageList.adapter != null) {
                    if (it != null) {
                        if (viewModel.allList.value!!.size > 0) {
                            mBinding.rvManageList.visible()
                            mBinding.ltLoading.gone()
                            mBinding.ll404Page.gone()
                            mBinding.rlNoListingMsg.gone()
                            mBinding.rvManageList.requestModelBuild()
                        } else {
                            showNoListMessage()
                        }
                    }
                }else{
                    mBinding.actionBar.listTitle.visible()
                    mBinding.actionBar.backToLsit.gone()
                    mBinding.rvManageList.visible()
                    mBinding.ltLoading.gone()
                    mBinding.ll404Page.gone()
                    mBinding.rlNoListingMsg.gone()
                    setUp()
                }
            }
        })
    }

    fun setUp() {
        try {
            mBinding.rvManageList.withModels {
                /*viewholderListingDetailsSectionHeader {
                    id("header")
                    header(getString(R.string.listing))
                }*/
                if (viewModel.allList.value != null) {
                    viewModel.allList.value!!.forEachIndexed { index, result ->
                        if (result.isReady!!.not()) {
                            if (progressLoaded) {
                                viewholderUserName {
                                    id("inProgress")
                                    name(getString(R.string.in_progress))
                                    paddingBottom(true)
                                    paddingTop(true)
                                }
                                progressLoaded = false
                            }
                            if (result.isReady!!) {
                                completedPercent = 100
                            } else if (result.step1Status.equals("completed") && result.step2Status.equals("completed") &&
                                    result.step3Status.equals("completed") && !result.isReady!!) {
                                if (result.imageName.equals("")) {
                                    completedPercent = 90
                                } else {
                                    completedPercent = 100
                                }
                            } else if (result.step1Status.equals("active")) {
                                completedPercent = 20
                            } else if (result.step1Status.equals("completed")) {
                                if (result.step2Status.equals("completed")) {
                                    if (result.imageName.equals("")) {
                                        if (result.step3Status.equals("completed")) {
                                            completedPercent = 60
                                        } else {
                                            completedPercent = 50
                                        }
                                    } else {
                                        completedPercent = 60
                                    }
                                } else if (!result.imageName.equals("")) {
                                    completedPercent = 40
                                } else {
                                    completedPercent = 30
                                }
                            }
                            val date = Utils.listingEpochToDate(result.created!!.toLong(),Utils.getCurrentLocale(requireContext()) ?: Locale.US )
                            viewholderManageList {
                                id("list$index")
                                if (result.title.equals("")) {
                                    val tit = "${result.roomType} in ${result.location}"
                                    title(tit)
                                } else {
                                    title(result.title)
                                }
                                image(result.imageName)
                                created(getString(R.string.last_updated_on)+" $date")
                                percent(getString(R.string.You_re)+" $completedPercent% "+getString(R.string.done_with_your_listing))
                                listPercent(completedPercent)
                                publishVisible(true)
                                preview(result.isReady!!.not()) //result.isReady!!.not()
                                previewClick(View.OnClickListener {
                                    Utils.clickWithDebounce(it){
                                        val id = viewModel.allList.value!![index].id
                                        viewModel.retryCalled = "view-$id"
                                        viewModel.getListingDetails(id)
                                    }
                                })
                                onclick(View.OnClickListener {
                                    val intent = Intent(context, HostFinalActivity::class.java)
                                    intent.putExtra("listId", viewModel.allList.value!![index].id.toString() )
                                    intent.putExtra("yesNoString", "Yes")
                                    intent.putExtra("bathroomCapacity", "0")
                                    intent.putExtra("country", "")
                                    intent.putExtra("countryCode","")
                                    intent.putExtra("street", "")
                                    intent.putExtra("buildingName", "")
                                    intent.putExtra("city", "")
                                    intent.putExtra("state", "")
                                    intent.putExtra("zipcode", "")
                                    intent.putExtra("lat","")
                                    intent.putExtra("lng","")
                                    startActivity(intent)
                                })
                                onBind { _, view, _ ->
                                    val relativeLay = view.dataBinding.root.findViewById<RelativeLayout>(R.id.deleteClickLay)
                                    relativeLay.setOnLongClickListener(View.OnLongClickListener {
                                        showDeleteConfirm(viewModel.allList.value!![index].id,index,"inprogress")
                                        true
                                    })

                                    relativeLay.setOnClickListener(View.OnClickListener {
                                        val intent = Intent(context, HostFinalActivity::class.java)
                                        intent.putExtra("listId", viewModel.allList.value!![index].id.toString())
                                        intent.putExtra("yesNoString", "Yes")
                                        intent.putExtra("bathroomCapacity", "0")
                                        intent.putExtra("country", "")
                                        intent.putExtra("countryCode","")
                                        intent.putExtra("street", "")
                                        intent.putExtra("buildingName", "")
                                        intent.putExtra("city", "")
                                        intent.putExtra("state", "")
                                        intent.putExtra("zipcode", "")
                                        intent.putExtra("lat","")
                                        intent.putExtra("lng","")
                                        startActivity(intent)
                                    })
                                }
                                onUnbind { _, view ->
                                    val relativeLay = view.dataBinding.root.findViewById<RelativeLayout>(R.id.deleteClickLay)
                                    relativeLay.setOnLongClickListener(null)
                                    relativeLay.setOnClickListener(null)
                                }
                            }
                        }
                    }

                    viewModel.allList.value!!.forEachIndexed { index, result1 ->
                        if (result1.isReady!!) {
                            if (completedLoaded) {
                                viewholderUserName {
                                    id("completed")
                                    name(getString(R.string.completed))
                                    paddingBottom(true)
                                    paddingTop(true)
                                }
                                completedLoaded = false
                            }
                            val date = Utils.listingEpochToDate(result1.created!!.toLong(),Utils.getCurrentLocale(requireContext()) ?: Locale.US )

                            val submissionStateText = if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && result1.isPublish == false)
                                "approved"
                            else if (viewModel.dataManager.listingApproval == DataManager.ListingApproval.OPTIONAL && result1.isPublish == true)
                                "published"
                            else if (result1.listApprovelStatus == "approved" && result1.isPublish == true)
                                "published"
                            else result1.listApprovelStatus

                            viewholderManageList {
                                id("completedlist" + index)
                                title(result1.title)
                                image(result1.imageName)
                                submissionStatus(submissionStateText)
                                percent(getString(R.string.you_re_100_done_with_your_listing))
                                listPercent(100)
                                created(getString(R.string.last_updated_on)+" $date")
                                publishVisible(false)
                                onPublishClick(View.OnClickListener {
                                    if ((result1.listApprovelStatus == null || result1.listApprovelStatus=="declined") && viewModel.dataManager.listingApproval == DataManager.ListingApproval.REQUIRED) {
                                        viewModel.submitForVerification("pending",result1.id)
                                        //showToast(getString(R.string.submitting))
                                        return@OnClickListener
                                    }
                                    val id = result1.id
                                    if(viewModel.publishBoolean.get()!!){
                                        viewModel.publishBoolean.set(false)
                                        if (viewModel.allList!!.value!![index]!!.isPublish!!) {
                                            viewModel.retryCalled = "update-unPublish-$id-$index"
                                            viewModel.publishListing("unPublish", viewModel.allList.value!![index].id, index)
                                        } else {
                                            viewModel.retryCalled = "update-publish-$id-$index"
                                            viewModel.publishListing("publish", viewModel.allList.value!![index].id, index)
                                        }
                                    }
                                })
                                previewClick(View.OnClickListener {
                                    Utils.clickWithDebounce(it){
                                        val id = viewModel.allList.value!![index].id
                                        viewModel.retryCalled = "view-$id"
                                        viewModel.getListingDetails(id)
                                    }
                                })
                                publishTxt(result1.isPublish!!)
                                preview(false) //result1.isReady!!.not()
                                onclick(View.OnClickListener {
                                    val intent = Intent(context, HostFinalActivity::class.java)
                                    intent.putExtra("listId", viewModel.allList.value!![index].id.toString())
                                    intent.putExtra("yesNoString", "Yes")
                                    intent.putExtra("bathroomCapacity", "0")
                                    intent.putExtra("country", "")
                                    intent.putExtra("countryCode","")
                                    intent.putExtra("street", "")
                                    intent.putExtra("buildingName", "")
                                    intent.putExtra("city", "")
                                    intent.putExtra("state", "")
                                    intent.putExtra("zipcode", "")
                                    intent.putExtra("lat","")
                                    intent.putExtra("lng","")
                                    startActivity(intent)
                                })
                                onBind { _, view, _ ->
                                    val relativeLay = view.dataBinding.root.findViewById<RelativeLayout>(R.id.deleteClickLay)
                                    relativeLay.setOnLongClickListener(View.OnLongClickListener {
                                        showDeleteConfirm(viewModel.allList.value!![index].id,index,"completed")
                                        true
                                    })
                                    relativeLay.setOnClickListener(View.OnClickListener {
                                        val intent = Intent(context, HostFinalActivity::class.java)
                                        intent.putExtra("listId", viewModel.allList.value!![index].id.toString())
                                        intent.putExtra("yesNoString", "Yes")
                                        intent.putExtra("bathroomCapacity", "0")
                                        intent.putExtra("country", "")
                                        intent.putExtra("countryCode","")
                                        intent.putExtra("street", "")
                                        intent.putExtra("buildingName", "")
                                        intent.putExtra("city", "")
                                        intent.putExtra("state", "")
                                        intent.putExtra("zipcode", "")
                                        intent.putExtra("lat","")
                                        intent.putExtra("lng","")
                                        startActivity(intent)
                                    })
                                }
                                onUnbind { _, view ->
                                    val relativeLay = view.dataBinding.root.findViewById<RelativeLayout>(R.id.deleteClickLay)
                                    relativeLay.setOnLongClickListener(null)
                                    relativeLay.setOnClickListener(null)
                                }
                            }
                        }
                    }
                }
                completedLoaded = true
                progressLoaded = true
                if(mBinding.srlManageList.isRefreshing){
                    mBinding.srlManageList.isRefreshing = false
                }
            }
        } catch (e: Exception) {
            showError()
        }
    }

    fun showDeleteConfirm(listId: Int,pos: Int,from: String){
        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_listing))
                .setMessage(getString(R.string.confrim_delete))
                .setPositiveButton(getString(R.string.DELETE)) { _, _ ->
                    viewModel.retryCalled = getString(R.string.delete)+"-$listId-$pos-$from"
                    viewModel.removeList(listId,pos,from)
                }
                .setNegativeButton(getString(R.string.CANCEL)) { dialog, _ -> dialog.dismiss()
                }
                .show()
    }

    override fun showListDetails() {
        val item = viewModel.listingDetails
        item?.value?.let {
            val currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.getConvertedRate(it.listingData()?.currency()!!, it.listingData()?.basePrice()!!.toDouble()))
            val photo = ArrayList<String>()
            photo.add(it.listPhotoName()!!)
            var publishStatus : Boolean
            val listDetails = ListingInitData(
                    title = it.title().toString(),
                    id = it.id()!!.toInt(),
                    photo = photo,
                    roomType = it.roomType().toString(),
                    ratingStarCount = it.reviewsStarRating(),
                    reviewCount = it.reviewsCount(),
                    price = currency,
                    guestCount = 0,
                    startDate = "0",
                    endDate = "0",
                    selectedCurrency = viewModel.getUserCurrency(),
                    currencyBase = viewModel.getCurrencyBase(),
                    currencyRate = viewModel.getCurrencyRates(),
                    hostName = it.user()!!.profile()!!.displayName().toString(),
                    bookingType = it.bookingType()!!,
                    isPreview = true)

            ListingDetails.openListDetailsActivity(requireContext(),listDetails)

        }
    }

    override fun show404Screen() {
        mBinding.actionBar.ivAddList.gone()
        mBinding.actionBar.listTitle.gone()
        mBinding.actionBar.backToLsit.visible()
        mBinding.srlManageList.gone()
        mBinding.rvManageList.gone()
        mBinding.rlNoListingMsg.gone()

        mBinding.ll404Page.visible()
    }

    override fun showNoListMessage() {
        mBinding.rlNoListingMsg.visible()
        mBinding.rvManageList.gone()
        mBinding.ltLoading.gone()
        mBinding.ll404Page.gone()
        mBinding.actionBar.ivAddList.visible()
        mBinding.actionBar.backToLsit.gone()
        mBinding.actionBar.listTitle.visible()

        if(mBinding.srlManageList.isRefreshing){
            mBinding.srlManageList.isRefreshing = false
        }
    }

    fun onRefresh() {
        completedLoaded = true
        progressLoaded = true
        if (::mViewModelFactory.isInitialized) {
            if (mBinding.srlManageList.isRefreshing.not()) {
                mBinding.rvManageList.gone()
                mBinding.ltLoading.visible()
                Log.d("onInbox123", "inbox - refresh")
                viewModel.listRefresh()
            }
        }
    }

    override fun onRetry() {
        if(viewModel.retryCalled.equals("")){
            viewModel.getList()
        }else if(viewModel.retryCalled.contains("delete")){
            val text =viewModel.retryCalled.split("-")
            viewModel.removeList(text[1].toInt(),text[2].toInt(),text[3])
        }else if(viewModel.retryCalled.contains("update")){
            val text =viewModel.retryCalled.split("-")
            viewModel.publishListing(text[1],text[2].toInt(),text[3].toInt())
        }else if(viewModel.retryCalled.contains("view")){
            val text =viewModel.retryCalled.split("-")
            viewModel.getListingDetails(text[1].toInt())
        }
    }

    fun onBackPressed(){
        if(mBinding.ll404Page.visibility == View.VISIBLE){
            mBinding.srlManageList.visible()
            mBinding.rvManageList.visible()
            mBinding.rlNoListingMsg.gone()
            mBinding.ll404Page.gone()
        }else{
            baseActivity?.finish()
        }
    }


    override fun onDestroyView() {
        mBinding.rvManageList.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        mBinding.rvManageList.adapter = null
        super.onDestroy()
    }

    override fun hideLoading() {
        mBinding.ltLoading.gone()
    }
}