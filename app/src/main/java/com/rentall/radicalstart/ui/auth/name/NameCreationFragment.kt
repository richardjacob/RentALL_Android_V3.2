package com.rentall.radicalstart.ui.auth.name

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAuthNameCreationBinding
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.UiEvent
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NameCreationFragment : BaseFragment<FragmentAuthNameCreationBinding, NameCreationViewModel>() {

    private lateinit var mBinding: FragmentAuthNameCreationBinding
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_name_creation
    override val viewModel: NameCreationViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(NameCreationViewModel::class.java)
    private var param1: String = ""
    private var param2: String = ""

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                NameCreationFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1, "")
            param2 = it.getString(ARG_PARAM2, "")
        }
        viewModel.firstName.set(param1)
        viewModel.lastName.set(param2)
    }

    private fun initView() {
        mBinding.actionBar.tvRightside.gone()
        mBinding.actionBar.rlToolbarNavigateup.onClick { activity?.onBackPressed() }
        mBinding.rlLottie.onClick {
            hideKeyboard()
            if(viewModel.firstName.get()!!.trim().isNullOrEmpty() && viewModel.lastName.get()!!.trim().isNullOrEmpty()){
                showSnackbar(getString(R.string.invalid_name),getString(R.string.invalid_name_desc))
            }else if(viewModel.firstName.get()!!.trim().isNullOrEmpty()) {
                showSnackbar(getString(R.string.invalid_name),getString(R.string.invalid_firstname_desc))
            }else if(viewModel.lastName.get()!!.trim().isNullOrEmpty()) {
                showSnackbar(getString(R.string.invalid_name),getString(R.string.invalid_lastname_desc))
            }else{
                RxBus.publish(UiEvent.Navigate(AuthViewModel.Screen.EMAIL, viewModel.firstName.get()!!.trim(), viewModel.lastName.get()!!.trim()))
            }
        }
    }

    override fun onRetry() {

    }

}