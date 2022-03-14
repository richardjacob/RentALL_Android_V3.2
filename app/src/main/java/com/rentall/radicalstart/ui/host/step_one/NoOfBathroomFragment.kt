package com.rentall.radicalstart.ui.host.step_one

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentTypeOfBedsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NoOfBathroomFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_beds
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfBedsBinding
    var strUser: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if (baseActivity!!.getIntent().hasExtra("from")){
            strUser = baseActivity!!.getIntent().getStringExtra("from").orEmpty()
            if (strUser.isNotEmpty() && strUser.equals("steps"))
                viewModel.isEdit = true
            else
                viewModel.isEdit = false
        }
        if(viewModel.isListAdded) {
            mBinding.actionBar.tvRightside.text = getText(R.string.save_and_exit)
            mBinding.actionBar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_bar_color))
            mBinding.actionBar.tvRightside.onClick { viewModel.retryCalled = "update"
                viewModel.address.set("")
                viewModel.location.set("")
                if (viewModel.isEdit){
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                }else{
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                }
                viewModel.getLocationFromGoogle(viewModel.address.get().toString(),false){
                    if (viewModel.country.get()!!.trim().isNullOrEmpty() || viewModel.street.get()!!.trim().isNullOrEmpty() || viewModel.city.get()!!.trim().isNullOrEmpty() || viewModel.state.get()!!.trim().isNullOrEmpty() || viewModel.zipcode.get()!!.trim().isNullOrEmpty()) {                    baseActivity!!.showSnackbar(resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page), resources.getString(R.string.please_fill_them))
                    } else if (viewModel.isEdit && viewModel.location.get().isNullOrEmpty()) {
                        if (isNetworkConnected){
                            baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
                        }else{
                            baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                        }
                    } else {
                        viewModel.updateHostStepOne()
                    }
                }
                 }
        }else{
            mBinding.actionBar.tvRightside.visibility = View.GONE
        }
        mBinding.actionBar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.actionBar.ivNavigateup.onClick {
            if (viewModel.isEdit){
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_BEDS)
            }else{
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_BEDS) }
        }
        mBinding.tvNext.setOnClickListener {
            Utils.clickWithDebounce(mBinding.tvNext){
                viewModel.address.set("")
                viewModel.location.set("")
                viewModel.address.set(viewModel.street.get()!!.trim() + ", " + viewModel.countryCode.get()!!.trim() + ", " + viewModel.state.get()!!.trim() + ", " + viewModel.city.get()!!.trim())
                viewModel.getLocationFromGoogle(viewModel.address.get().toString(),false){
                    if (viewModel.isEdit){
                        if (viewModel.country.get().isNullOrEmpty() || viewModel.street.get().isNullOrEmpty() || viewModel.city.get().isNullOrEmpty() || viewModel.state.get().isNullOrEmpty() || viewModel.zipcode.get().isNullOrEmpty()) {
                            baseActivity!!.showSnackbar(getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page), getString(R.string.please_fill_them))
                            viewModel.onContinueClick(StepOneViewModel.NextScreen.ADDRESS)
                        } else if (viewModel.location.get().isNullOrEmpty()) {
                            Log.d("Location is ","=== "+viewModel.location.get())
                            if (isNetworkConnected){
                                baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
                            }else{
                                baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                            }
                            viewModel.onContinueClick(StepOneViewModel.NextScreen.ADDRESS)
                        } else {
                            viewModel.onContinueClick(StepOneViewModel.NextScreen.MAP_LOCATION)
                        }
                    }else{
                        viewModel.onContinueClick(StepOneViewModel.NextScreen.ADDRESS)
                    }
                }

            }
        }
        viewModel.bathroomCapacity.set("1")
        subscribeToLiveData()
        setUp()
    }

    private fun subscribeToLiveData() {
        viewModel.bathroomCount.observe(viewLifecycleOwner, Observer {
            viewModel.bathroomCapacity.set(viewModel.bathroomCount.value)
            requestModelBuildIt()
        })
        viewModel.bathroomType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.noOfBathroom.observe(viewLifecycleOwner, Observer {
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

    private fun setUp() {
        try {
            mBinding.rvStepOne.withModels {
                viewholderUserName {
                    id("how many bathrooms")
                    name(resources.getString(R.string.how_many_bathrooms))
                }
                viewholderHostPlusMinus {
                    id(" bathroom i")
                    if (viewModel.bathroomCapacity.get()!!.toDouble().equals(1.0)) {
                        text(Utils.formatDecimal(viewModel.bathroomCapacity.get().toString().toDouble()) + " " + viewModel.noOfBathroom!!.value!!.listSettings()?.get(0)!!.itemName())
                    } else {
                        text(Utils.formatDecimal(viewModel.bathroomCapacity.get().toString().toDouble()) + " " + viewModel.noOfBathroom!!.value!!.listSettings()?.get(0)!!.otherItemName())
                    }
                    personCapacity1(Utils.formatDecimal(viewModel.bathroomCapacity.get().toString().toDouble()))
                    plusLimit1(viewModel.noOfBathroom!!.value!!.listSettings()?.get(0)!!.endValue()!!)
                    minusLimit1(viewModel.noOfBathroom!!.value!!.listSettings()?.get(0)!!.startValue()!!)
                    clickPlus(View.OnClickListener {
                        Log.d("inside", "plus click")
                        viewModel.bathroomCapacity.get()?.let {
                            viewModel.bathroomCapacity.set(it.toDouble().plus(0.5).toString())
                            viewModel.bathroomCount.value = viewModel.bathroomCapacity.get().toString()
                            viewModel.bathroomCapacity.set(Utils.formatDecimal(viewModel.bathroomCapacity.get()!!.toDouble()))
                            viewModel.becomeHostStep1.value!!.bathroomCount = viewModel.bathroomCapacity.get()!!.toDouble()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.bathroomCapacity.get()?.let {
                            viewModel.bathroomCapacity.set(it.toDouble().minus(0.5).toString())
                            viewModel.bathroomCount.value = viewModel.bathroomCapacity.get().toString()
                            viewModel.bathroomCapacity.set(Utils.formatDecimal(viewModel.bathroomCapacity.get()!!.toDouble()))
                            viewModel.becomeHostStep1.value!!.bathroomCount = viewModel.bathroomCapacity.get()!!.toDouble()
                        }
                    })
                }
                viewholderDivider {
                    id("divider 1")
                }
                viewholderBathroomtype {
                    id("bathroom type")
                    text(viewModel.bathroomType.value)
                    clickListener(View.OnClickListener {
                        StepOneOptionsFragment.newInstance("bathroomOptions").show(childFragmentManager, "bathroomOptions")
                    })
                }
                viewholderDivider {
                    id("divider - 2")
                }
            }
        } catch (e: Exception) {
            showError()
        }
    }

    override fun onRetry() {

    }

}