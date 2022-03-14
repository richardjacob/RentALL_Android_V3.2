package com.rentall.radicalstart.ui.host.step_three

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostActivityStepThreeBinding
import com.rentall.radicalstart.ui.base.BaseActivity
import com.rentall.radicalstart.ui.host.step_three.bookingWindow.BookWindowFragment
import com.rentall.radicalstart.ui.host.step_three.discount.DiscountPriceFragment
import com.rentall.radicalstart.ui.host.step_three.guestBooking.GuestBookingFragment
import com.rentall.radicalstart.ui.host.step_three.guestNotice.GuestNoticeFragment
import com.rentall.radicalstart.ui.host.step_three.guestReq.GuestReqFragment
import com.rentall.radicalstart.ui.host.step_three.houseRules.HouseRuleFragment
import com.rentall.radicalstart.ui.host.step_three.instantBook.InstantBookFragment
import com.rentall.radicalstart.ui.host.step_three.listingPrice.ListingPriceFragment
import com.rentall.radicalstart.ui.host.step_three.localLaws.LocalLawsFragment
import com.rentall.radicalstart.ui.host.step_three.tripLength.TripLengthFragment
import com.rentall.radicalstart.util.addFragmentToActivity
import com.rentall.radicalstart.util.replaceFragmentInActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class StepThreeActivity : BaseActivity<HostActivityStepThreeBinding,StepThreeViewModel>(),StepThreeNavigator{

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostActivityStepThreeBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_step_three
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(this,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator =this
        viewModel.listID = intent.getStringExtra("listID").orEmpty()
//        viewModel.listID = "428"
        subscribeToLiveData(savedInstanceState)
    }

    fun subscribeToLiveData(savedInstanceState: Bundle?){
        viewModel.listSetting().observe(this, Observer {
            it.let {
                if(savedInstanceState == null){
                    initView()
                }
            }
        })
    }

    fun initView(){
        addFragment(GuestReqFragment(),"GuestReq")
    }


    private fun addFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        addFragmentToActivity(mBinding.flStepThree.id, fragment, tag)
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flStepThree.id, fragment, tag)
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun navigateToScreen(screen: StepThreeViewModel.NextStep) {
        try {
            when (screen) {
                StepThreeViewModel.NextStep.HOUSERULE -> {
                    replaceFragment(HouseRuleFragment(), "houseRule")
                }
                StepThreeViewModel.NextStep.GUESTBOOK -> {
                    Log.d("Selected Rules:", viewModel.selectedRules.toString())
                    replaceFragment(GuestBookingFragment(), "guestBook")
                }
                StepThreeViewModel.NextStep.GUESTNOTICE -> {
                    replaceFragment(GuestNoticeFragment(), "guestNotice")
                }
                StepThreeViewModel.NextStep.BOOKWINDOW -> {
                    if ((viewModel.fromChoosen.equals("Flexible") && viewModel.toChoosen.equals("Flexible"))) {
                        replaceFragment(BookWindowFragment(), "bookWindow")
                    } else if (viewModel.fromChoosen.equals("Flexible") || viewModel.toChoosen.equals("Flexible")) {
                        replaceFragment(BookWindowFragment(), "bookWindow")
                    } else {
                        if (viewModel.fromChoosen.toInt() >= viewModel.toChoosen.toInt()) {
                            viewModel.isSnackbarShown = true
                            showSnackbar(getString(R.string.time), getString(R.string.checkin_error_text))
                        } else {
                            replaceFragment(BookWindowFragment(), "bookWindow")
                        }
                    }
                }
                StepThreeViewModel.NextStep.TRIPLENGTH -> {
                    replaceFragment(TripLengthFragment(), "tripLength")
                }
                StepThreeViewModel.NextStep.LISTPRICE -> {
                    if(viewModel.checkTripLength()){
                            hideSnackbar()
                            replaceFragment(ListingPriceFragment(), "listPrice")
                    }
                }

                StepThreeViewModel.NextStep.DISCOUNTPRICE -> {
                    replaceFragment(DiscountPriceFragment(), "discountPrice")
                }
                StepThreeViewModel.NextStep.INSTANTBOOK -> {
                        replaceFragment(InstantBookFragment(), "instantBook")
                }
                StepThreeViewModel.NextStep.LAWS -> {
                    replaceFragment(LocalLawsFragment(), "localLaws")
                }
                StepThreeViewModel.NextStep.FINISH -> {
                    this.finish()
                }
                StepThreeViewModel.NextStep.NODATA -> {

                }
            }
        }catch(e: Exception){
            e.printStackTrace()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onRetry() {
        if(viewModel.retryCalled.equals("")){
            viewModel.getListStep3Details()
        }else if(viewModel.retryCalled.equals("update")){
            viewModel.updateListStep3("edit")
        }
    }

    override fun onBackPressed() {
        Log.d("backCalled", "backCalled")
        hideKeyboard()
        super.onBackPressed()
    }

    override fun show404Page() {
        showToast(getString(R.string.list_not_available))
        this.finish()
    }
}