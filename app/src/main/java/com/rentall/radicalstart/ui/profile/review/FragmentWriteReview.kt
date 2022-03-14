package com.rentall.radicalstart.ui.profile.review

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentWriteReviewBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.*
import javax.inject.Inject
import javax.inject.Named

class FragmentWriteReview(val reservationId: Int) : BaseFragment<FragmentWriteReviewBinding,ReviewViewModel>(){

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentWriteReviewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_write_review
    override val viewModel: ReviewViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(ReviewViewModel::class.java)
    var listId=0
    var receiverId =""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.relSubmit.gone()
        mBinding.ivClose.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_back_black_24dp))
        mBinding.ivClose.root.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        mBinding.tvSubmit.onClick{
            Utils.clickWithDebounce(mBinding.tvSubmit) {
                when {
                    viewModel.reviewDesc.get()?.trim().isNullOrEmpty() -> {
                        viewModel.navigator.showSnackbar(getString(R.string.required),getString(R.string.description))
                    }
                    viewModel.userRating.get()!!<=0.0 -> {
                        viewModel.navigator.showSnackbar(getString(R.string.required),getString(R.string.rating))
                    }
                    else -> {
                        viewModel.writeUserReview(listId,receiverId,reservationId)
                    }
                }
            }
        }
        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        viewModel.pendingReviewResult.observe(viewLifecycleOwner, Observer {
            it?.let {
                listId = it.listId() ?: 0
                receiverId = if(viewModel.dataManager.currentUserId.equals(it.guestId())){
                    it.hostId() ?: ""
                }else{
                    it.guestId() ?: ""
                }
                mBinding.relSubmit.visible()
                setup(it)
            }
        })
        viewModel.getPendingUserReview(reservationId)
    }


    fun  setup(result: GetPendingUserReviewQuery.Result){
        mBinding.rlFragReview.withModels {
           viewholderDisplayReviewListing {
               id("listingReview")
               title(result.listData()?.title())
               address(result.listData()?.city() +", "+ result.listData()?.state()+ ", "+result.listData()?.country())
               ratingTotal(result.listData()?.reviewsStarRating()?.toDouble() ?: 0.0)
               reviewsTotal(result.listData()?.reviewsCount())
               if(result.listData()?.reviewsCount() ==0){
                   reviewsCountText(result.listData()?.reviewsCount().toString()+" "+context?.resources?.getString(R.string.review_small))
               }else{
                   reviewsCountText(result.listData()?.reviewsCount().toString()+" "+context?.resources?.getQuantityString(R.plurals.review_count,result.listData()?.reviewsCount() ?: 1))
               }

               imgUrl(result.listData()?.listPhotoName())
           }
            viewholderDividerPadding {
                id("padding-1")
            }
            viewholderDescribeExperience {
                id("describeExperience")
                title(getString(R.string.describe_your_experience))
                if(viewModel.dataManager.currentUserId.equals(result.guestId())){
                    subTitle(getString(R.string.your_review_will_be_public_on_your_host_profile))
                }else{
                    subTitle(getString(R.string.your_review_will_be_public))
                }
                hint(getString(R.string.what_was_it_like_to_host_this_guest))
                text(viewModel.reviewDesc)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_descriptionBox)
                    editText.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(false)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_descriptionBox)
                    editText.setOnTouchListener(null)
                }
            }
            viewholderOverallRating {
                id("overALLRating")
                viewModel(viewModel)
                title(getString(R.string.overall_rating))
            }
            viewholderDividerPadding {
                id("padding-2")
            }
        }

    }

    override fun onDestroyView() {
        try{
            viewModel.navigator.hideKeyboard()
            viewModel.navigator.hideSnackbar()
            mBinding.rlFragReview.adapter = null
            viewModel.pendingReviewResult= MutableLiveData()
            viewModel.userRating.set(0.toFloat())
            viewModel.reviewDesc.set("")
        }catch (e: Exception){
            e.printStackTrace()
        }
        super.onDestroyView()
    }

    override fun onRetry() {
    }
}