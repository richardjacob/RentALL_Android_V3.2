package com.rentall.radicalstart.host.payout.addPayout

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.GetCountrycodeQuery
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAddPayoutIntroBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

class PaymentIntroFragment : BaseFragment<FragmentAddPayoutIntroBinding, AddPayoutViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_add_payout_intro
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(AddPayoutViewModel::class.java)
    lateinit var mBinding: FragmentAddPayoutIntroBinding
    private var list = ArrayList<GetCountrycodeQuery.Result>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    private fun initView() {
        mBinding.btnNext.onClick {
            viewModel.navigator.moveToScreen(AddPayoutActivity.Screen.INFO)
        }
    }

    override fun onRetry() {

    }
}