package com.rentall.radicalstart.ui.host.step_three

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.databinding.HostFragmentOptionsBinding
import com.rentall.radicalstart.ui.base.BaseBottomSheet
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderDivider
import com.rentall.radicalstart.viewholderOptionText
import javax.inject.Inject

class OptionsSubFragment : BaseBottomSheet<HostFragmentOptionsBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentOptionsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.rentall.radicalstart.R.layout.host_fragment_options
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)



    private var isSelected = false

    var selectedValue : Int? = null

    var optionsArray = ArrayList<Boolean>()

    var type : String = ""

    companion object {
        @JvmStatic
        fun newInstance(type : String) =
            OptionsSubFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding=viewDataBinding!!
        Log.d("fg", "Fd123+ $type")

        arguments?.let {
            type=it.getString("type", "ert")
            Log.d("fg", "Fd+ $type")
        }
        mBinding.rvOptions.setHasFixedSize(true)
        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        if(type.equals("options")) {
            val options = viewModel.listSettingArray.value!!.bookingNoticeTime()!!.listSettings()
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                options?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("option"+index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s.itemName())
                        size(20.toFloat())
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        clickListener(View.OnClickListener {
                                val data = viewModel.listDetailsStep3.value
                                data?.noticePeriod = s.itemName()
                                viewModel.listDetailsStep3.value = data
                                viewModel.noticeTime = s.id().toString()
                                dismiss()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }else if(type.equals("from")){
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                viewModel.fromOptions?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("from"+index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
                                val data = viewModel.listDetailsStep3.value
                                data?.noticeFrom = s
                                viewModel.listDetailsStep3.value = data
                                viewModel.fromChoosen = viewModel.fromTime.get(index)
                                viewModel.noticeFrom = index.toString()
                                dismiss()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }else if(type.equals("to")){
            val options = viewModel.listSettingArray.value!!.maxDaysNotice()!!.listSettings()
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                viewModel.toOptions?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("to"+index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {

//                            if(selectedValue == null){
//                                selectedValue = index
//                                optionsArray.set(index,true)
                                val data = viewModel.listDetailsStep3.value
                                data?.noticeTo = s
                                viewModel.listDetailsStep3.value = data
//                            }else{
//                                if(selectedValue == index){
//                                    optionsArray.set(index,false)
//                                    selectedValue = null
//                                }else{
//                                    for(i in 0 until optionsArray.size){
//                                        if(optionsArray.get(i)){
//                                            optionsArray.set(i,false)
//                                        }
//                                    }
//                                    selectedValue = index
//                                    optionsArray.set(index,true)
//                                    val data = viewModel.listDetailsStep3.value
//                                    data?.noticeTo = s
//                                    viewModel.listDetailsStep3.value = data
//                                }
//                            }
                            viewModel.toChoosen = viewModel.toTime.get(index)
                            viewModel.noticeTo = index.toString()
                            dismiss()
//                            requestModelBuild()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }

        else if(type.equals("dates")){
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.datesAvailable.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("dates"+index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
//                            if(selectedValue == null){
//                                selectedValue = index
//                                optionsArray.set(index,true)
                                val data = viewModel.listDetailsStep3.value
                                data?.availableDate = s
                                viewModel.listDetailsStep3.value = data

//                            }else{
//                                if(selectedValue == index){
//                                    optionsArray.set(index,false)
//                                    selectedValue = null
//                                }else{
//                                    for(i in 0 until optionsArray.size){
//                                        if(optionsArray.get(i)){
//                                            optionsArray.set(i,false)
//                                        }
//                                    }
//                                    selectedValue = index
//                                    optionsArray.set(index,true)
//                                    val data = viewModel.listDetailsStep3.value
//                                    data?.availableDate = s
//                                    viewModel.listDetailsStep3.value = data
//                                }
//                            }
//                            requestModelBuild()
                            viewModel.bookWind = viewModel.availOptions.get(index)
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }

        else if(type.equals("policy")){
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.cancellationPolicy.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("policy"+index)
                        paddingBottom(true)
                        paddingTop(true)
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        desc(s)
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
//                            if(selectedValue == null){
//                                selectedValue = index
//                                optionsArray.set(index,true)
                                val data = viewModel.listDetailsStep3.value
                                data?.cancellationPolicy = s
                                viewModel.listDetailsStep3.value = data

//                            }else{
//                                if(selectedValue == index){
//                                    optionsArray.set(index,false)
//                                    selectedValue = null
//                                }else{
//                                    for(i in 0 until optionsArray.size){
//                                        if(optionsArray.get(i)){
//                                            optionsArray.set(i,false)
//                                        }
//                                    }
//                                    selectedValue = index
//                                    optionsArray.set(index,true)
//                                    val data = viewModel.listDetailsStep3.value
//                                    data?.cancellationPolicy = s
//                                    viewModel.listDetailsStep3.value = data
//                                }
//                            }
                            viewModel.cancelPolicy = index + 1
                            dismiss()
                            //requestModelBuild()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }

        else if(type.equals("price")){
            optionsArray.clear()
            mBinding.rvOptions.withModels {

                viewModel.currency.value!!.results()!!.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("price"+index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s.symbol())
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        size(20.toFloat())
                        clickListener(View.OnClickListener {
//                            if(selectedValue == null){
//                                selectedValue = index
//                                optionsArray.set(index,true)
                                val data = viewModel.listDetailsStep3.value
                                data?.currency = s.symbol()
                                viewModel.listDetailsStep3.value = data
                            dismiss()
//                            }else{
//                                if(selectedValue == index){
//                                    optionsArray.set(index,false)
//                                    selectedValue = null
//                                }else{
//                                    for(i in 0 until optionsArray.size){
//                                        if(optionsArray.get(i)){
//                                            optionsArray.set(i,false)
//                                        }
//                                    }
//                                    selectedValue = index
//                                    optionsArray.set(index,true)
//                                    val data = viewModel.listDetailsStep3.value
//                                    data?.currency = s.symbol()
//                                    viewModel.listDetailsStep3.value = data
//                                }
//                            }
                           // requestModelBuild()

                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
        }

    }


    override fun onRetry() {

    }

}