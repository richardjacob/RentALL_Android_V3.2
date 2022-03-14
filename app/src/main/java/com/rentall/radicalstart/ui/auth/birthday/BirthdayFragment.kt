package com.rentall.radicalstart.ui.auth.birthday

import android.app.DatePickerDialog

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.FragmentAuthBirthdayBinding
import com.rentall.radicalstart.ui.auth.AuthNavigator
import com.rentall.radicalstart.ui.auth.AuthViewModel
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.dobcalendar.BirthdayDialog
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.UiEvent
import com.rentall.radicalstart.util.gone
import com.rentall.radicalstart.util.onClick
import javax.inject.Inject


class BirthdayFragment : BaseFragment<FragmentAuthBirthdayBinding, BirthdayViewModel>(), AuthNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_birthday
    override val viewModel: BirthdayViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(BirthdayViewModel::class.java)
    lateinit var mBinding: FragmentAuthBirthdayBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
    }

    private fun initView() {
        mBinding.actionBar.tvRightside.gone()
        mBinding.actionBar.rlToolbarNavigateup.onClick { activity?.onBackPressed() }
        mBinding.ltLoadingView.setImageResource(R.drawable.ic_right_arrow_blue)
        mBinding.rlBirthday.onClick {
            hideSnackbar()
            openCalender1()
        }
    }

    private fun openCalender1() {
        val birthdayDialog = BirthdayDialog.newInstance(
                viewModel.dob.get()!![0],
                viewModel.dob.get()!![1],
                viewModel.dob.get()!![2]
        )
        birthdayDialog.show(childFragmentManager)
        birthdayDialog.setCallBack(DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            viewModel.yearLimit.set( arrayOf( year, month + 1, dayOfMonth ) )
            viewModel.dob.set( arrayOf( year, month, dayOfMonth ) )
        })
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {
        viewModel.signUpUser()
    }

}
