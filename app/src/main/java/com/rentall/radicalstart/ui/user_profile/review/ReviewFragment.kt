package com.rentall.radicalstart.ui.user_profile.review

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.rentall.radicalstart.*
import com.rentall.radicalstart.data.remote.paging.NetworkState
import com.rentall.radicalstart.data.remote.paging.Status
import com.rentall.radicalstart.databinding.FragmentUserProfileBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.user_profile.UserProfileActivity
import com.rentall.radicalstart.ui.user_profile.UserProfileViewModel
import com.rentall.radicalstart.util.gone
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ReviewFragment : BaseFragment<FragmentUserProfileBinding, UserProfileViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentUserProfileBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_user_profile
    override val viewModel: UserProfileViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(UserProfileViewModel::class.java)
    private lateinit var pagingController: UserReviewController
    private var reviewCount = 0
    private var name = ""
//    val app_name = resources.getString(R.string.app_name)

    companion object {
        private const val REVIEWCOUNT = "param1"
        private const val NAME = "param2"
        @JvmStatic
        fun newInstance(count: Int? = 0, name: String) =
                ReviewFragment().apply {
                    arguments = Bundle().apply {
                        count?.let {
                            putInt(REVIEWCOUNT, it)
                        } ?: putInt(REVIEWCOUNT, 0)
                        putString(NAME, name)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
        viewModel.getReview()
    }

    private fun initView() {
        mBinding.llBottomBtn.gone()
        mBinding.actionBar.root.gone()
        arguments?.let {
            reviewCount = it.getInt(REVIEWCOUNT, 0)
            name = it.getString(NAME, "")
        }

        val dividerItemDecoration = DividerItemDecoration(context,
                ((mBinding.rvUserProfile.layoutManager) as androidx.recyclerview.widget.LinearLayoutManager).orientation)
        mBinding.rvUserProfile.addItemDecoration(dividerItemDecoration)
        pagingController = UserReviewController(
                listClickListener = { item -> openListingDetail(item) },
                profileClickListener = { item -> openProfile(item) },
                reviewCount = reviewCount,
                name = name,
                verifiedString = resources.getString(R.string.verified_by) + " " + resources.getString(R.string.app_name)//viewModel.getSiteName()
        )
    }

    private fun subscribeToLiveData() {

        viewModel.posts.observe(viewLifecycleOwner, Observer<PagedList<UserReviewsQuery.Result>> { pagedList ->
            pagedList?.let {
                if (mBinding.rvUserProfile.adapter == pagingController.adapter) {
                    pagingController.submitList(it)
                } else {
                    mBinding.rvUserProfile.adapter = pagingController.adapter
                    pagingController.submitList(it)
                }
            }
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                       // mBinding.srlInbox.isRefreshing = false
                        //mBinding.rlInboxNomessagePlaceholder.visible()
                    }
                    NetworkState.LOADING -> {
                       // mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = true
                    }
                    NetworkState.LOADED -> {
                       // mBinding.srlInbox.isRefreshing = false
                       // mBinding.rlInboxNomessagePlaceholder.gone()
                        pagingController.isLoading = false
                    }
                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            it.msg?.let { thr ->
                                viewModel.handleException(thr)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })
    }

    private fun openProfile(item: UserReviewsQuery.Result) {
        try {
            UserProfileActivity.openProfileActivity(requireContext(), item.authorData()?.fragments()?.profileFields()?.profileId()!!)
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    private fun openListingDetail(item: UserReviewsQuery.Result) {

    }

    override fun onRetry() {
        viewModel.reviewRetry()
    }

    inner class UserReviewController(
            private val listClickListener: (item: UserReviewsQuery.Result) -> Unit,
            private val profileClickListener: (item: UserReviewsQuery.Result) -> Unit,
            private val reviewCount: Int,
            private val name: String,
            private val verifiedString: String
    ) : PagedListEpoxyController<UserReviewsQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(currentPosition: Int, item: UserReviewsQuery.Result?): EpoxyModel<*> {
            return UserReviewEpoxyGroup(currentPosition, item, listClickListener, profileClickListener, verifiedString,resources.getString(R.string.app_name))
        }

        override fun addModels(models: List<EpoxyModel<*>>) {

            try {
                viewholderListingDetailsSectionHeader {
                    id("header")
                    header(resources.getQuantityString(
                            R.plurals.review_count_profile,
                            reviewCount,
                            reviewCount,
                            name
                    ))
                }
                if (isLoading) {
                    viewholderLoader {
                        id("loading")
                        isLoading(true)
                    }
                }
                super.addModels(models)
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

    class UserReviewEpoxyGroup(currentPosition: Int, item: UserReviewsQuery.Result?,
                               listClickListener: (item: UserReviewsQuery.Result) -> Unit,
                               profileClickListener: (item: UserReviewsQuery.Result) -> Unit,
                               verifiedString: String,
                               appname: String) :
            EpoxyModelGroup(R.layout.model_user_review_group, buildModels(item, listClickListener, profileClickListener, verifiedString,appname)) {
        init {
            id("UserReview - $currentPosition")
        }
    }
}

fun buildModels(item: UserReviewsQuery.Result?,
                listClickListener: (item: UserReviewsQuery.Result) -> Unit,
                profileClickListener: (item: UserReviewsQuery.Result) -> Unit,
                verifiedString: String,appname: String): List<EpoxyModel<*>> {
    val models = ArrayList<EpoxyModel<*>>()
    try {
        item?.let {
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

            models.add(ViewholderListingDetailsReviewsBindingModel_()
                    .comment(item.reviewContent())
                    .date(item.createdAt())
                    .imgUrl(image)
                    .isAdmin(item.isAdmin)
                    .name(name)
                    .padding(true)
                    .onAvatarClick(View.OnClickListener {
                        if (item.isAdmin!!.not()) {
                            profileClickListener(item)
                        }
                    }))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        return models
    }
}
