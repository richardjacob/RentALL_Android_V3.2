package com.rentall.radicalstart.ui.listing.guest

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityGuestBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.listing.ListingDetailsViewModel
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class GuestFragment : BaseFragment<ActivityGuestBinding, ListingDetailsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_guest
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: ActivityGuestBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.personCapacity1 = viewModel.personCapacity1
        viewModel.initialValue.value?.let {
            viewModel.personCapacity1.set(it.guestCount.toString())
        }
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
                val initialValues = viewModel.initialValue.value!!
                initialValues.guestCount = viewModel.personCapacity1.get()!!.toInt()
                viewModel.initialValue.value = initialValues
                // viewModel.getBillingCalculation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer {
            it?.let { listDetails ->
                mBinding.minusLimit1 = 1
                mBinding.plusLimit1 = listDetails.personCapacity()
            }
        })
    }

    override fun onRetry() {

    }
}