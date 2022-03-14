package com.rentall.radicalstart.ui.profile.confirmPhonenumber

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentConfirmPhonenumberBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import javax.inject.Inject


class ConfirmPhnoFragment : BaseFragment<FragmentConfirmPhonenumberBinding, ConfirmPhnoViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_confirm_phonenumber
    override val viewModel: ConfirmPhnoViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(ConfirmPhnoViewModel::class.java)
    lateinit var mBinding: FragmentConfirmPhonenumberBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    private fun initView() {
        mBinding.ivClose.root.setOnClickListener {
            baseActivity?.finish()
        }
    }

    override fun onRetry() {
        viewModel.getCountryCodes()
    }

}

