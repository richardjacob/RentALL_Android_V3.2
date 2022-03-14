package com.rentall.radicalstart.ui.profile.review

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.GetUserReviewsQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.FragmentReviewBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.profile.review.controller.ReviewListController
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.visible
import timber.log.Timber
import javax.inject.Inject

class FragmentReviewByYouPast : BaseFragment<FragmentReviewBinding, ReviewViewModel>(){
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentReviewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_review
    override val viewModel: ReviewViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(ReviewViewModel::class.java)
    private lateinit var pagingController: ReviewListController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding=viewDataBinding!!
        mBinding.tvUpPast.text=resources.getString(R.string.past_reviews)
        pagingController = ReviewListController(this.requireContext(),"byYou")
        subscribeToLiveData()
        viewModel.netWorkStateByYou.observe(this, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.ltLoadingView.gone()
                        mBinding.rvReviewList.gone()
                        mBinding.relNoReviews.visible()
                    }
                    NetworkState.LOADING -> {
                        mBinding.ltLoadingView.visible()
                        mBinding.relNoReviews.gone()
                    }
                    NetworkState.LOADED -> {
                        mBinding.rvReviewList.visible()
                        mBinding.ltLoadingView.gone()
                        mBinding.relNoReviews.gone()
                        mBinding.tvUpPast.visible()
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            it.msg?.let {error ->
                                viewModel.handleException(error)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })
    }


    fun subscribeToLiveData(){
        viewModel.loadByYouList().observe(this, Observer<PagedList<GetUserReviewsQuery.Result>>{
            it?.let {
                if(mBinding.rvReviewList.adapter==pagingController.adapter){
                    pagingController.submitList(it)
                }else{
                    mBinding.rvReviewList.adapter = pagingController.adapter
                    pagingController.submitList(it)
                }
            }
        })
    }

    override fun onRetry() {
    }
}