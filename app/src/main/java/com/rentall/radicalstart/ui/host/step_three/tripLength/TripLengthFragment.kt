package com.rentall.radicalstart.ui.host.step_three.tripLength

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentTripLengthBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderHostPlusMinus
import com.rentall.radicalstart.viewholderUserName
import javax.inject.Inject

class TripLengthFragment : BaseFragment<HostFragmentTripLengthBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentTripLengthBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_trip_length
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.tripLengthToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.tripLengthToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.tripLengthToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.tripLengthToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.tripLengthToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.tripLengthToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
        observeData()
    }

    fun subscribeToLiveData(){
        try {
            mBinding.rvTripLength.withModels {
                viewholderUserName {
                    id("header")
                    name(getString(R.string.trip_length))
                    paddingTop(true)
                    paddingBottom(true)
                }

                viewholderHostPlusMinus {
                    id("min")
                    text(getString(R.string.min_stay))
                    minusLimit1(viewModel.listSettingArray!!.value!!.minNight()!!.listSettings()!![0].startValue())
                    plusLimit1(viewModel.listSettingArray!!.value!!.minNight()!!.listSettings()!![0].endValue())
                    personCapacity1(viewModel.minNight.get())
                    clickMinus(View.OnClickListener {
                        viewModel.minNight.get()?.let {
                            viewModel.minNight.set(it.toInt().minus(1).toString())
                            val data = viewModel.listDetailsStep3.value
                            data?.minStay = viewModel.minNight.get()
                            viewModel.listDetailsStep3.value = data
                        }
                    })
                    clickPlus(View.OnClickListener {
                        viewModel.minNight.get()?.let {
                            viewModel.minNight.set(it.toInt().plus(1).toString())
                            val data = viewModel.listDetailsStep3.value
                            data?.minStay = viewModel.minNight.get()
                            viewModel.listDetailsStep3.value = data
                        }
                    })
                }

                viewholderHostPlusMinus {
                    id("max")
                    text(getString(R.string.max_stay))
                    minusLimit1(viewModel.listSettingArray!!.value!!.maxNight()!!.listSettings()!![0].startValue())
                    plusLimit1(viewModel.listSettingArray!!.value!!.maxNight()!!.listSettings()!![0].endValue())
                    personCapacity1(viewModel.maxNight.get())
                    clickMinus(View.OnClickListener {
                        viewModel.maxNight.get()?.let {
                            viewModel.maxNight.set(it.toInt().minus(1).toString())
                            val data = viewModel.listDetailsStep3.value
                            data?.maxStay = viewModel.maxNight.get()
                            viewModel.listDetailsStep3.value = data
                        }
                    })
                    clickPlus(View.OnClickListener {
                        viewModel.maxNight.get()?.let {
                            viewModel.maxNight.set(it.toInt().plus(1).toString())
                            val data = viewModel.listDetailsStep3.value
                            data?.maxStay = viewModel.maxNight.get()
                            viewModel.listDetailsStep3.value = data
                        }
                    })
                }

            }
        } catch (E: Exception) {
            showError()
        }
    }

    fun observeData(){
            viewModel.listDetailsStep3.observe(viewLifecycleOwner, Observer {
                if(isAdded) {
                    if (mBinding.rvTripLength.adapter != null) {
                        if(viewModel.isSnackbarShown){
                            hideSnackbar()
                            viewModel.isSnackbarShown = false
                        }
                        mBinding.rvTripLength.requestModelBuild()
                    }
                }
            })
    }

    override fun onRetry() {

    }
}