package com.rentall.radicalstart.ui.listing.review

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.databinding.BR
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.FragmentListingReviewsBinding
import com.rentall.radicalstart.databinding.FragmentReviewListingBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.visible
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class ReviewFragment: BaseFragment<FragmentReviewListingBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_review_listing
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentReviewListingBinding
    private lateinit var pagingController: UserReviewController
    private var param1: Int? = null
    private var param2: Int? = null
    private var param3: Int? = null
    companion object {
        @JvmStatic
        fun newInstance(param1: Int?, param2: Int?, param3: Int?) =
                ReviewFragment().apply {
                    arguments = Bundle().apply {
                        var reviewCount = 0
                        var reviewStartCount = 0
                        var position = 0
                        if (param1 != null) {
                            reviewCount = param1
                        }
                        if (param2 != null) {
                            reviewStartCount = param2
                        }
                        if(param3 != null){
                            position = param3
                        }
                        putInt(ARG_PARAM1, reviewCount)
                        putInt(ARG_PARAM2, reviewStartCount)
                        putInt(ARG_PARAM3, position)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.reviewToolbar.ivNavigateup.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_back_black_24dp))
        mBinding.reviewToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            param2 = it.getInt(ARG_PARAM2)
            param3 = it.getInt(ARG_PARAM3)
        }
//        val dividerItemDecoration = DividerItemDecoration(context,
//                ((mBinding.rlListingAmenities.layoutManager) as androidx.recyclerview.widget.LinearLayoutManager).orientation)
//        mBinding.rlListingAmenities.addItemDecoration(dividerItemDecoration)
        pagingController = UserReviewController(
                activity?.baseContext!!,
                listClickListener = {  },
                profileClickListener = { item -> openProfile(item) },
                reviewsCountInt = param1!!,
                reviewsStarRatingInt = param2!!,
                verifiedString = resources.getString(R.string.verified_by) + " "+ resources.getString(R.string.app_name)
        )

        param3?.let{
            if(it!=0){
                val handler = Handler()
                val r = Runnable {
                    mBinding.appBarLayout.setExpanded(false)
                    mBinding.rlListingAmenities.smoothScrollToPosition(it) }
                handler.postDelayed(r, 500)
            }
        }

        subscribeToLiveData()
    }

    private fun openProfile(item: GetPropertyReviewsQuery.Result) {
        try {
            UserProfileActivity.openProfileActivity(requireContext(), item.authorData()?.fragments()?.profileFields()?.profileId()!!)
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.posts.observe(viewLifecycleOwner, Observer<PagedList<GetPropertyReviewsQuery.Result>> {
            if (mBinding.rlListingAmenities.adapter == pagingController.adapter) {
                pagingController.submitList(it)
            } else {
                mBinding.rlListingAmenities.adapter = pagingController.adapter
                pagingController.submitList(it)
            }
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        //  mBinding.rvExploreEpoxy.gone()
                        //mBinding.llNoResult.visible()
                        mBinding.rlListingAmenities.visible()
                        mBinding.ltLoadingView.gone()
                    }
                    NetworkState.LOADING -> {
//                        pagingController.isLoading = true
                        mBinding.rlListingAmenities.gone()
                        mBinding.ltLoadingView.visible()
                    }
                    NetworkState.LOADED -> {
                        mBinding.rlListingAmenities.visible()
                        mBinding.ltLoadingView.gone()
                        pagingController.isLoading = false
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            mBinding.rlListingAmenities.visible()
                            mBinding.ltLoadingView.gone()
                            viewModel.handleException(it.msg!!)
                        }
                    }
                }
            }
        })
    }

    override fun onRetry() {
        Timber.d("retry called1")
        if(::mViewModelFactory.isInitialized && isAdded && activity != null) {
            viewModel.repoResult.value?.retry?.invoke()
        }
    }

    override fun onDestroy() {
        mBinding.rlListingAmenities.adapter == null
        super.onDestroy()
    }

    inner class UserReviewController(
            val context: Context,
            private val listClickListener: (item: GetPropertyReviewsQuery.Result) -> Unit,
            private val profileClickListener: (item: GetPropertyReviewsQuery.Result) -> Unit,
            private val reviewsCountInt: Int,
            private val reviewsStarRatingInt: Int,
            private val verifiedString: String
    ) : PagedListEpoxyController<GetPropertyReviewsQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(currentPosition: Int, item: GetPropertyReviewsQuery.Result?): EpoxyModel<*> {
            return try{
                UserReviewEpoxyGroup(context,currentPosition, item!!, listClickListener, profileClickListener, verifiedString)
            }catch (e: Exception){
                UserReviewEpoxyGroup(mBinding.root.context,0, item!!, listClickListener, profileClickListener, verifiedString)
            }

        }

        override fun addModels(models: List<EpoxyModel<*>>) {

            try {
                viewholderReviewHeader {
                    id("reviewHeader")
                    if(reviewsCountInt>10){
                        val roundOff = round(reviewsStarRatingInt.toDouble()/reviewsCountInt.toDouble())
                        displayCount(roundOff.toInt())
                        reviewsCount(reviewsCountInt)
                    }else{
                        val roundOff = round(reviewsStarRatingInt.toDouble()/reviewsCountInt.toDouble())
                        displayCount(roundOff.toInt())
                        reviewsCount(reviewsCountInt)
                    }
                    isBlack(true)
                    large(true)
                    paddingStart(true)
                }
                super.addModels(models)
                /*if (isLoading) {
                        viewholderLoader {
                            id("loading")
                            isLoading(true)
                        }
                    }*/
            } catch (e: Exception) {
                Timber.e(e,"CRASH")
            }
        }

        init {
            isDebugLoggingEnabled = true
        }

        override fun onExceptionSwallowed(exception: RuntimeException) {
            throw exception
        }
    }

    class UserReviewEpoxyGroup(context: Context,currentPosition: Int, item: GetPropertyReviewsQuery.Result,
                               listClickListener: (item: GetPropertyReviewsQuery.Result) -> Unit,
                               profileClickListener: (item: GetPropertyReviewsQuery.Result) -> Unit,
                               verifiedString: String) :
            EpoxyModelGroup(R.layout.model_user_review_group, buildModels(context,item, listClickListener, profileClickListener, verifiedString)) {
        init {
            id("UserReview - ${item.id()}")
        }
    }
}

fun buildModels(context: Context,item: GetPropertyReviewsQuery.Result,
                listClickListener: (item: GetPropertyReviewsQuery.Result) -> Unit,
                profileClickListener: (item: GetPropertyReviewsQuery.Result) -> Unit,
                verifiedString: String): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    try {
        val name = if (item.isAdmin!!) {
            verifiedString
        } else {
            item.authorData()?.fragments()?.profileFields()?.firstName() + " " + item.authorData()?.fragments()?.profileFields()?.lastName()
        }
        val image = if (item.isAdmin!!) {
            ""
        } else {
            item.authorData()?.fragments()?.profileFields()?.picture()
        }
        val  ltrDirection = context.resources.getBoolean(R.bool.is_left_to_right_layout)

        var profileId=0

        if(item.isAdmin!!.not()) {
            profileId = item.authorData()?.fragments()?.profileFields()?.profileId()!!
        }

        models.add(ViewholderReviewInfoBindingModel_()
                .id("viewholder- ${item.id()}")
                .name(name)
                .comment(item.reviewContent())
                .imgUrl(item.authorData()?.fragments()?.profileFields()?.picture())
                .isAdmin(item.isAdmin)
                .type("aboutYou")
                .ltrDirection(ltrDirection)
                .ratingTotal(item.rating())
                .reviewsTotal(1)
                .date(item.createdAt())
                .profileId(profileId)
                .onAvatarClick(View.OnClickListener {
                    if(item.isAdmin?.not()!!){
                        Utils.clickWithDebounce(it) {
                                profileClickListener(item)
                        }
                    }
                }))
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        return models
    }
}