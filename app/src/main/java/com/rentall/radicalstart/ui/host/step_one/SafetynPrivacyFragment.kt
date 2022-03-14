package com.rentall.radicalstart.ui.host.step_one

import android.os.Bundle
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
import javax.inject.Inject

class SafetynPrivacyFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_beds
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfBedsBinding
    val safetynprivacy = arrayOf(R.string.smoke_detector, R.string.carbon_monoxide_detector, R.string.first_aid_kit, R.string.fire_extinguisher, R.string.private_entrance)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
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
                    if (viewModel.country.get().isNullOrEmpty() || viewModel.street.get().isNullOrEmpty() || viewModel.city.get().isNullOrEmpty() || viewModel.state.get().isNullOrEmpty() || viewModel.zipcode.get().isNullOrEmpty()) {
                        baseActivity!!.showSnackbar(resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page), resources.getString(R.string.please_fill_them))
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
        mBinding.actionBar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.tvNext.setOnClickListener {
            Utils.clickWithDebounce(mBinding.tvNext){
                viewModel.onContinueClick(StepOneViewModel.NextScreen.GUEST_SPACE)
            }
        }
        subscribeToLiveData()
    }
    private fun subscribeToLiveData() {
        viewModel.safetyAmenitiesList.observe(viewLifecycleOwner, Observer {
            setUp(it)
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

    private fun setUp(it: GetListingSettingQuery.SafetyAmenities) {
        mBinding.rvStepOne.withModels {
            viewholderUserName {
                id("safety privacy")
                name(getString(R.string.safety_amenities))
            }
            it.listSettings()!!.forEachIndexed { i, s ->
                viewholderHostCheckbox {
                    id("safety n privacy $i")
                    text(s.itemName())
                    visibility(false)
                    isChecked(viewModel.selectedDetectors.contains(s.id()))
                    onClick(View.OnClickListener {
                        if (viewModel.selectedDetectors.contains(s.id())){
                            viewModel.selectedDetectors.removeAt(viewModel.selectedDetectors.indexOf(s.id()))
                            viewModel.aafetyAmenitiedId.value = viewModel.selectedDetectors
                        }else{
                            s.id()?.let {
                                viewModel.selectedDetectors.add(it)
                                viewModel.aafetyAmenitiedId.value = viewModel.selectedDetectors
                            }
                        }
                        requestModelBuild()
                    })
                }
                if (i != it.listSettings()!!.lastIndex){
                    viewholderDivider {
                        id("Divider - " + i)
                    }
                }
            }
        }

    }
    override fun onRetry() {

    }
}