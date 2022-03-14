package com.rentall.radicalstart.ui.host.step_three.instantBook

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentInstantBookBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class InstantBookFragment : BaseFragment<HostFragmentInstantBookBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentInstantBookBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_instant_book
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)


    private var isSelected = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.instantBookToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.instantBookToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.instantBookToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.instantBookToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.instantBookToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.instantBookToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        mBinding.rvInstantBook.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.instant_book_title))
                paddingTop(true)
                paddingBottom(true)
            }
            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.instant_book_text))
                paddingTop(false)
                paddingBottom(false)
            }
            viewholderUserName {
                id("whoBooked")
                name(getString(R.string.who_booked))
                paddingTop(true)
                paddingBottom(false)
            }
            viewholderGrayTextview {
                id("chooseBooked")
                text(getString(R.string.choose_book))
            }
            viewholderRadioTextSub {
                id("1")
                text(getString(R.string.auto_approval_text))
                subText(getString(R.string.auto_sub))
                radioVisibility(viewModel.selectArray[0])
                onClick(View.OnClickListener {
                    viewModel.bookingType = "instant"
                    selector(0)  })
                direction(true)
            }
            viewholderReportUserRadio {
                id("2")
                text(getString(R.string.not_auto_approve))
                radioVisibility(viewModel.selectArray[1])
                onClick(View.OnClickListener {
                    viewModel.bookingType = "request"
                    selector(1)  })
                direction(true)
            }


        }
    }

    private fun selector(index: Int) {
        viewModel.selectArray.forEachIndexed { i: Int, _: Boolean ->
            viewModel.selectArray[i] = index == i
            isSelected = true
        }
        mBinding.rvInstantBook.requestModelBuild()
    }

    override fun onRetry() {

    }
}