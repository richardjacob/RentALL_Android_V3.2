package com.rentall.radicalstart.ui.host.step_three.bookingWindow

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentBookWindowBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.OptionsSubFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class BookWindowFragment : BaseFragment<HostFragmentBookWindowBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentBookWindowBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_book_window
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.bookWindowToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.bookWindowToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.bookWindowToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount() && viewModel.checkTripLength()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.bookWindowToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.bookWindowToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.bookWindowToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
        observeData()
    }

    fun observeData(){
        viewModel.listDetailsStep3.observe(viewLifecycleOwner, Observer {
            if(isAdded){
                if(mBinding.rvBookWindow.adapter!=null) {
                    mBinding.rvBookWindow.requestModelBuild()
                }
            }
        })

    }

    fun subscribeToLiveData(){
        mBinding.rvBookWindow.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.booking_window))
                paddingBottom(true)
                paddingTop(true)
            }

            viewholderListTv {
                id("dates")
                hint(viewModel.listDetailsStep3.value?.availableDate)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    OptionsSubFragment.newInstance("dates").show(childFragmentManager, "dates")
                })
            }

            viewholderDivider {
                id("dateDiv")
            }

            viewholderListingDetailsHeader {
                id("policyTxt")
                header(getString(R.string.cancel_policy))
                isBlack(true)
                large(true)
            }

            viewholderListTv {
                id("policy")
                hint(viewModel.listDetailsStep3.value?.cancellationPolicy)
                etHeight(false)
                maxLength(50)
                onNoticeClick(View.OnClickListener {
                    OptionsSubFragment.newInstance("policy").show(childFragmentManager,"policy")
                })
            }

            viewholderDivider {
                id("policyDiv")
            }
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flSubFragment.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onRetry() {

    }
}