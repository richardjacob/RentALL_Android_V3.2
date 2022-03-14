package com.rentall.radicalstart.ui.host.step_three.localLaws

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentLocalLawsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListingDetailsHeader
import com.rentall.radicalstart.viewholderUserName
import com.rentall.radicalstart.viewholderUserNormalText
import javax.inject.Inject

class LocalLawsFragment : BaseFragment<HostFragmentLocalLawsBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentLocalLawsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_local_laws
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.localLawsToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.localLawsToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.localLawsToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.localLawsToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.localLawsToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.localLawsToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()

        mBinding.tvNext.setOnClickListener {
            viewModel.retryCalled = "update"
            viewModel.updateListStep3("add")
//            val fm = fragmentManager
//            val count = fm?.backStackEntryCount
//            for (i in 0 until count!!) {
//                fm.popBackStack()
//                baseActivity!!.finish()
//            }
        }
    }

    fun subscribeToLiveData(){
        mBinding.rvLocalLaws.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.local_laws))
                paddingBottom(true)
                paddingBottom(true)
            }
            viewholderListingDetailsHeader {
                id("subHeader")
                header(getString(R.string.local_laws_text))
                isBlack(true)
                large(true)
            }

            viewholderUserNormalText {
                id("content")
                text(getString(R.string.local_laws_content))
                paddingTop(true)
                paddingBottom(false)
            }

        }
    }

    override fun onRetry() {
    }
}