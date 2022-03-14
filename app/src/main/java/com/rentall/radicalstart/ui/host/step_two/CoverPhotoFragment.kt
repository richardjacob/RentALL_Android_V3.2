package com.rentall.radicalstart.ui.host.step_two

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentCoverPhotoBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderHostCoverPhoto
import com.rentall.radicalstart.viewholderUserName
import javax.inject.Inject

class CoverPhotoFragment : BaseFragment<HostFragmentCoverPhotoBinding,StepTwoViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentCoverPhotoBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_cover_photo
    override val viewModel: StepTwoViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepTwoViewModel::class.java)

    val selectedArray = ArrayList<Boolean>()

    private var isSelected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.coverToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.coverToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.coverToolbar.tvRightside.onClick {
                viewModel.retryCalled = "update"
                viewModel.updateStep2()
            }
        }else{
            mBinding.coverToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.coverToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.coverToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed() }


        viewModel.showPhotosList.value?.forEachIndexed { index, s ->
            if(viewModel.isCoverPhoto == index)
                selectedArray.add(true)
            selectedArray.add(false)
        }
        subscribeToLiveData()
    }


    fun subscribeToLiveData(){
        mBinding.rvCoverPhoto.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.add_cover_header))
                paddingBottom(true)
                paddingTop(true)
            }

            viewModel.showPhotosList.value?.forEachIndexed { index, s ->
                viewholderHostCoverPhoto {
                    id("cover"+index)
                    image(viewModel.showPhotosList.value!![index]!!.name())
                    if(index == 0){
                        marginTop(true)
                    }else{
                        marginTop(false)
                    }
                    marginBottom(true)
                    onSelected(selectedArray[index])
                    onClick(View.OnClickListener {
                        selector(index)
                        viewModel.listDetailsStep2.value!!.coverPhoto = viewModel.showPhotosList.value!![index]!!.id()
                    })
                }
            }
        }
    }

    private fun selector(index: Int) {
        selectedArray.forEachIndexed { i: Int, _: Boolean ->
            selectedArray[i] = index == i
            isSelected = true
        }
        mBinding.rvCoverPhoto.requestModelBuild()
    }

    override fun onRetry() {
        viewModel.getListDetailsStep2()
    }
}