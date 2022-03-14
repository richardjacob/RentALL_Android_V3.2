package com.rentall.radicalstart.ui.profile.confirmPhonenumber

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.databinding.FragmentFourdigitCodeBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import javax.inject.Inject

class EnterCodeFragment : BaseFragment<FragmentFourdigitCodeBinding, ConfirmPhnoViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.rentall.radicalstart.R.layout.fragment_fourdigit_code
    override val viewModel: ConfirmPhnoViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ConfirmPhnoViewModel::class.java)
    lateinit var mBinding: FragmentFourdigitCodeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    private fun initView() {
        mBinding.ivClose.setOnClick { baseActivity?.onBackPressed() }
    }

    override fun onRetry() {
        viewModel.verifyCode()
    }

}