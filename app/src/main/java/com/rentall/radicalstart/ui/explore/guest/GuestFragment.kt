package com.rentall.radicalstart.ui.explore.guest

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityGuestBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.explore.ExploreViewModel
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class GuestFragment : BaseFragment<ActivityGuestBinding, ExploreViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_guest
    override val viewModel: ExploreViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ExploreViewModel::class.java)
    lateinit var mBinding: ActivityGuestBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
       // mBinding.viewModel = viewModel
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.personCapacity1 = viewModel.personCapacity1
        viewModel.personCapacity1.set(viewModel.personCapacity.value)
        mBinding.inlToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.ibGuestMinus.onClick {
            viewModel.personCapacity1.get()?.let {
                viewModel.personCapacity1.set(it.toInt().minus(1).toString())
            }
        }
        mBinding.ibGuestPlus.onClick {
            viewModel.personCapacity1.get()?.let {
                viewModel.personCapacity1.set(it.toInt().plus(1).toString())
            }
        }
        mBinding.btnGuestSeeresult.onClick {
            try {
                baseActivity?.onBackPressed()
                if (viewModel.personCapacity1.get() != null && viewModel.personCapacity1.get()!!.toInt() > 0) {
                    viewModel.personCapacity.value = viewModel.personCapacity1.get()
//                viewModel.getSearchListing1()
                    viewModel.startSearching()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.exploreLists1.observe(viewLifecycleOwner, Observer {
            it?.listingSettingsCommon?.results()?.let { list ->
                list.forEachIndexed { _, item ->
                    if (item.id() == 2) {
                        mBinding.minusLimit1 = item.listSettings()?.get(0)?.startValue()
                        mBinding.plusLimit1 = item.listSettings()?.get(0)?.endValue()
                    }
                    return@forEachIndexed
                }
            }
        })
    }

    override fun onRetry() {

    }
}