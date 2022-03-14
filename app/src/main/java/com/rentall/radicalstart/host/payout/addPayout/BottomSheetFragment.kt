package com.rentall.radicalstart.host.payout.addPayout

import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentOptionsBinding
import com.rentall.radicalstart.ui.base.BaseBottomSheet
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderDivider
import com.rentall.radicalstart.viewholderOptionText
import javax.inject.Inject

class BottomSheetFragment : BaseBottomSheet<HostFragmentOptionsBinding, AddPayoutViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentOptionsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.rentall.radicalstart.R.layout.host_fragment_options
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(AddPayoutViewModel::class.java)



    private var isSelected = false

    var selectedValue : Int? = null

    var optionsArray = ArrayList<Boolean>()

    var type : String = ""

    companion object {
        @JvmStatic
        fun newInstance(type : String) =
                BottomSheetFragment().apply {
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
            val options = arrayOf(getString(R.string.individual), getString(R.string.company))
            optionsArray.clear()
            mBinding.rvOptions.withModels {
                options?.forEachIndexed { index, s ->
                    optionsArray.add(false)
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s)
                        size(20.toFloat())
                        txtColor(optionsArray.get(index))
                        isSelected(optionsArray.get(index))
                        clickListener(View.OnClickListener {
                            viewModel.accountType.set(s)
                            if(s.equals(getString(R.string.individual))){
                                viewModel.listPref.set(s)
                                viewModel.lastNameVisible.value = false
                            }else{
                                viewModel.listPref.set(s)
                                viewModel.lastNameVisible.value = true
                            }
                            Log.d("viewModel.lastNa",viewModel.lastNameVisible.value!!.toString())
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }

            }
    }


    override fun onRetry() {

    }

}