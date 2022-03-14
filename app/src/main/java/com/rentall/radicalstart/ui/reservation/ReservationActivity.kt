package com.rentall.radicalstart.ui.reservation

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.ActivityBookingBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.home.HomeActivity
import com.rentall.radicalstart.util.*
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class ReservationActivity : BaseActivity<ActivityBookingBinding, ReservationViewModel>(), ReservationNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityBookingBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_booking
    override val viewModel: ReservationViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(ReservationViewModel::class.java)
    private var userType = ""
    private var is404PageShown = false
    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        val type = intent.getIntExtra("type", 1)
        viewModel.reservationId.value = intent.getIntExtra("reservationId", 0)
        intent?.extras?.let {
            userType = it.getString("userType", "")
        }
        if (type == 1) {
            addFragment(ItineraryFragment(), "ItineraryFragment")
        } else {
            addFragment(ReceiptFragment(), "ReceiptFragment")
        }

        mBinding.btnExplore.onClick {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    private fun addFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        addFragmentToActivity(mBinding.flBooking.id, fragment, tag)
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flBooking.id, fragment, tag)
    }

    private fun subscribeToLiveData() {
        viewModel.reservationId.observe(this, Observer {
            viewModel.getReservationDetails()
        })
    }

    override fun navigateToScreen(screen: Int) {
        when (screen) {
            9 -> { replaceFragment(ReceiptFragment(), "ReceiptFragment")}
        }
    }

    override fun onRetry() {
            viewModel.getReservationDetails()
    }

    override fun show404Page() {
        is404PageShown =true
        mBinding.flBooking.gone()
        mBinding.ll404Page.visible()
    }

    override fun onBackPressed() {
        if(is404PageShown){
            mBinding.ll404Page.gone()
        }
        super.onBackPressed()
    }
}