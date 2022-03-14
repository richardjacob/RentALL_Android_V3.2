package com.rentall.radicalstart.ui.host.step_two

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentListTitleBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListEt
import com.rentall.radicalstart.viewholderUserName
import com.rentall.radicalstart.viewholderUserNormalText
import javax.inject.Inject

class AddListTitleFragment : BaseFragment<HostFragmentListTitleBinding,StepTwoViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentListTitleBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_title
    override val viewModel: StepTwoViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepTwoViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!

        if(viewModel.isListAdded) {
            mBinding.titleToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.titleToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.titleToolbar.tvRightside.onClick {
                if(viewModel.title.get().equals("")){
                    showSnackbar( "Please add a title to your list.", "Add title")
                }else {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
            }
        }else{
            mBinding.titleToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.titleToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.titleToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        mBinding.rvAddTitle.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.name_your_listing))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.title_sub_text))
                paddingTop(false)
                paddingBottom(true)
            }

            viewholderListEt {
                id("title")
                text(viewModel.title)
                title(getString(R.string.title))
                hint(getString(R.string.add_title))
                maxLength(50)
            }

        }
    }

    override fun onRetry() {
        viewModel.getListDetailsStep2()
    }
}