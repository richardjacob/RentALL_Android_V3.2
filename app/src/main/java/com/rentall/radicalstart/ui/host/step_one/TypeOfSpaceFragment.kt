package com.rentall.radicalstart.ui.host.step_one

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentTypeOfSpaceBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class TypeOfSpaceFragment : BaseFragment<HostFragmentTypeOfSpaceBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_space
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfSpaceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
        subscribeToLiveData()
        initView()
    }

    private fun initView() {
        viewModel.roomType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.capacity.observe( viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
    }

    fun requestModelBuildIt() {
        if (mBinding.rvStepOne.adapter != null) {
            mBinding.rvStepOne.requestModelBuild()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadDefaultSettings().observe(viewLifecycleOwner, Observer {
            it?.let {
                try {
                    if(isAdded){
                        if(mBinding.rvStepOne.adapter!=null) {
                            mBinding.rvStepOne.requestModelBuild()
                        }
                    }
                    setUp()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun setUp() {
        viewModel.becomeHostStep1.value.let {
        mBinding.rvStepOne.withModels {
            viewholderItineraryTextNormal {
                id("heading")
                text(resources.getString(R.string.lets_get_ready))
                large(true)
                isRed(false)
                paddingTop(true)
                paddingBottom(false)
            }
            viewholderHostStepOne {
                id("step")
                step(getString(R.string.STEP1))
                textSize(false)
                paddingTop(true)
                paddingBottom(false)
                isBlack(false)
                visibility(false)
            }
            viewholderUserName {
                id("what property")
                name(resources.getString(R.string.what_kindof_place))
                paddingTop(false)
                paddingBottom(false)
            }

            viewholderHostStepOne {
                id("place")
                step(viewModel.roomType.value)
                textSize(true)
                paddingTop(true)
                paddingBottom(true)
                showDivider(false)
                isBlack(true)
                visibility(false)
                clickListener(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("placeOptions").show(childFragmentManager, "placeOptions")
                })
            }
            viewholderDividerNoPadding {
                id("divider 1")
            }

            viewholderHostStepOne {
                id("guest")
                step(viewModel.capacity.value)
                textSize(true)
                text(getString(R.string.continuee))
                showDivider(true)
                visibility(true)
                paddingTop(true)
                paddingBottom(true)
                isBlack(true)
                clickListener(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("guestOptions").show(childFragmentManager, "guestOptions")
                })
                continuePress(View.OnClickListener {
                    Utils.clickWithDebounce(it){
                        viewModel.onContinueClick(StepOneViewModel.NextScreen.KIND_OF_PLACE)
                    }
                })
            }
 /*           viewholderDividerPadding {
                id("divider 1")
            }*/
        }
    }
    }


    override fun onRetry() {

    }
}