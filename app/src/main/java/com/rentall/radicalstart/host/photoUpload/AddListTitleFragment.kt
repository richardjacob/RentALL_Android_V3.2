package com.rentall.radicalstart.host.photoUpload

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentListTitleBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListEt
import com.rentall.radicalstart.viewholderUserName
import com.rentall.radicalstart.viewholderUserNormalText
import javax.inject.Inject

class AddListTitleFragment : BaseFragment<HostFragmentListTitleBinding, Step2ViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentListTitleBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_title
    override val viewModel: Step2ViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(Step2ViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.titleToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.titleToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.tvNext.onClick {
            Utils.clickWithDebounce(mBinding.tvNext){
                if(viewModel.title.get()?.trim().isNullOrEmpty()) {
                    showSnackbar( getString(R.string.add_title_alert), getString(R.string.add_title))
                } else {
                    viewModel.navigator.navigateToScreen(Step2ViewModel.NextScreen.LISTDESC)
                }
            }
        }
        subscribeToLiveData()
        initView()
    }

    private fun initView() {
        if(viewModel.getListAddedStatus()) {
            mBinding.titleToolbar.tvRightside.visibility = View.GONE
        } else {
            mBinding.titleToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.titleToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.titleToolbar.tvRightside.onClick {
                if(viewModel.checkFilledData()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
            }
        }
    }

    fun subscribeToLiveData(){
        viewModel.step2Result.observe(viewLifecycleOwner, Observer {
            it?.let {
                initView()
            }
        })
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