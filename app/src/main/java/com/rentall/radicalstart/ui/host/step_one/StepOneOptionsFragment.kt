package com.rentall.radicalstart.ui.host.step_one

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentBottomOptionsBinding
import com.rentall.radicalstart.ui.base.BaseBottomSheet
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderDivider
import com.rentall.radicalstart.viewholderHostBottomOptions
import javax.inject.Inject


class StepOneOptionsFragment : BaseBottomSheet<HostFragmentBottomOptionsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_bottom_options
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentBottomOptionsBinding
    var type: String = ""
    var guestfragment: FragmentManager? = null
    private var yesNo = arrayOf("Yes","No")

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
                StepOneOptionsFragment().apply {
                    arguments = Bundle().apply {
                        putString("type", type)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
        arguments?.let {
            type = it.getString("type").orEmpty()
        }
        mBinding.rvStepOne.setHasFixedSize(true)
        initView()
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.becomeHostStep1.observe(this, Observer {
            requestModelBuildIt()
        })
    }

    fun requestModelBuildIt() {
        if (mBinding.rvStepOne.adapter != null) {
            mBinding.rvStepOne.requestModelBuild()
        }
    }

    private fun initView() {
        if (type.equals("placeOptions")) {
            val options = viewModel.roomtypelist.value!!.listSettings()
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { i, s ->
                    viewholderHostBottomOptions {
                        id("selected - $i")
                        list(s.itemName())
                        clickListener(View.OnClickListener {
                            viewModel.roomType.value = s.itemName()
                            viewModel.becomeHostStep1.value!!.placeType = s.id().toString()
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id("divider - $i")
                    }
                }
            }
        } else if (type.equals("guestOptions")) {
            val start = viewModel.personCapacity!!.value!!.listSettings()?.get(0)!!.startValue()!!
            val end = viewModel.personCapacity!!.value!!.listSettings()?.get(0)!!.endValue()!!
            mBinding.rvStepOne.withModels {
                for (i in start until (end + 1)) {
                    viewholderHostBottomOptions {
                        id("selected - $i")
                        Log.d("i ", "is " + i)
                        if (i == 1) {
                            list(getString(R.string.For)+" $i " + viewModel.personCapacity!!.value!!.listSettings()?.get(0)!!.itemName())
                            viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                        } else {
                            list(getString(R.string.For)+" $i " + viewModel.personCapacity!!.value!!.listSettings()?.get(0)!!.otherItemName())
                            viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                        }
                        clickListener(View.OnClickListener {
                            if (i == 1) {
                                viewModel.capacity.value = getString(R.string.For)+" $i " + viewModel.personCapacity!!.value!!.listSettings()?.get(0)!!.itemName()
                                viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                            } else {
                                viewModel.capacity.value = getString(R.string.For)+" $i " + viewModel.personCapacity!!.value!!.listSettings()?.get(0)!!.otherItemName()
                                viewModel.becomeHostStep1.value!!.guestCapacity = "$i"
                            }

                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id("divider - $i")
                    }
                }
            }
        }else if (type.equals("houseOptions")){
                val options = viewModel.housetypelist.value!!.listSettings()
                mBinding.rvStepOne.withModels {
                    options?.forEachIndexed { index, s ->
                        viewholderHostBottomOptions {
                            id("selected - $index")
                            list(s.itemName())
                            clickListener(View.OnClickListener {
                                viewModel.houseType.value = s.itemName()
                                viewModel.becomeHostStep1.value!!.houseType = s.id().toString()
                                dismiss()
                            })
                        }
                        viewholderDivider {
                            id("divider - $index")
                        }
                    }
            }
        }else if (type.equals("guestPlaceOptions")) {
            val options = viewModel.roomtypelist.value!!.listSettings()
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { i, s ->
                    viewholderHostBottomOptions {
                        id("selected - $i")
                        list(s.itemName())
                        clickListener(View.OnClickListener {
                            viewModel.guestPlaceType.value = s.itemName()
                            viewModel.becomeHostStep1.value!!.guestSpace = s.id().toString()
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id("divider - $i")
                    }
                }
            }
        } else if (type.equals("roomSizeOptions")){
                val options = viewModel.roomSizelist.value!!.listSettings()
                mBinding.rvStepOne.withModels {
                    options?.forEachIndexed { index, s ->
                        viewholderHostBottomOptions {
                            id("selected - $index")
                            list(s.itemName())
                            clickListener(View.OnClickListener {
                                viewModel.roomSizeType.value = s.itemName()
                                viewModel.becomeHostStep1.value!!.roomCapacity = s.id().toString()
                                dismiss()
                            })
                        }
                        viewholderDivider {
                            id("divider - $index")
                        }
                    }
            }
        }else if (type.equals("yesNoOptions")){
            mBinding.rvStepOne.withModels {
                yesNo = arrayOf("Yes","No")

                yesNo?.forEachIndexed { index, s ->
                    viewholderHostBottomOptions {
                        id("selected - $index")
                        list(yesNo[index])
                        clickListener(View.OnClickListener {
                            viewModel.yesNoString!!.set(yesNo[index]).toString()
                            Log.d("yes no string ","is "+viewModel.yesNoString.get())

                            if (viewModel.yesNoString!!.get().equals("Yes")){
                                viewModel.becomeHostStep1.value!!.yesNoOptions = "1"
                                viewModel.yesNoType.value = viewModel.yesNoString!!.get()
                            }else{
                                viewModel.becomeHostStep1.value!!.yesNoOptions = "0"
                                viewModel.yesNoType.value = viewModel.yesNoString!!.get()
                            }


                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id("divider - $index")
                    }
                }
            }
        }else  if (type.equals("bathroomOptions")) {
            val options = viewModel.bathroomlist.value!!.listSettings()
            mBinding.rvStepOne.withModels {
                options?.forEachIndexed { index, s ->
                    viewholderHostBottomOptions {
                        id("selected - $index")
                        list(s.itemName())
                        clickListener(View.OnClickListener {
                            viewModel.bathroomType.value = s.itemName()
                            viewModel.becomeHostStep1.value!!.bathroomSpace = s.id().toString()
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id("divider - $index")
                    }
                }
            }
        }
    }

    override fun onRetry() {

    }

}