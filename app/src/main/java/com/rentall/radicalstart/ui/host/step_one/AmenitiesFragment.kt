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

class AmenitiesFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_beds
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfBedsBinding

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
                    Log.d("address ","is :::: "+viewModel.address.get().toString())
                    if (viewModel.country.get().isNullOrEmpty() || viewModel.street.get().isNullOrEmpty() || viewModel.city.get().isNullOrEmpty() || viewModel.state.get().isNullOrEmpty() || viewModel.zipcode.get().isNullOrEmpty()) {
                        baseActivity!!.showSnackbar(getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page), getString(R.string.please_fill_them))
                    } else if (viewModel.isEdit && viewModel.location.get().isNullOrEmpty()) {
                        if (isNetworkConnected){

                            baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.you_have_entered_an_incorrect_location))
                        }else{
                            baseActivity!!.showSnackbar(resources.getString(R.string.error), resources.getString(R.string.currently_offline))
                        }
                    } else {
                        viewModel.updateHostStepOne()
                    }}
                }
        }else{
            mBinding.actionBar.tvRightside.visibility = View.GONE
        }
        mBinding.actionBar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.actionBar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.tvNext.setOnClickListener {
            Utils.clickWithDebounce(mBinding.tvNext){
                viewModel.onContinueClick(StepOneViewModel.NextScreen.SAFETY_PRIVACY)
            }
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.amenitiesList.observe(viewLifecycleOwner, Observer {
            setUp(it)
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            if (mBinding.rvStepOne.adapter != null) {
                mBinding.rvStepOne.requestModelBuild()
            }
        })
    }

    private fun setUp(it: GetListingSettingQuery.Amenities) {
        mBinding.rvStepOne.withModels {
            viewholderUserName {
                id("amenties")
                name(getString(R.string.hwat_amenities_will_you_offer))
            }

            /*viewholderUserName {
                id("essential")
                name(it.typeLabel())
                paddingTop(true)
                paddingBottom(false)
            }*/

            it.listSettings()?.forEachIndexed { i, s ->
                viewholderHostCheckbox {
                    id("essentials $i")
                        text(s.itemName())
                        visibility(false)

                    isChecked(viewModel.selectedAmenities.contains(s.id()))
                    onClick(View.OnClickListener {
                        if (viewModel.selectedAmenities.contains(s.id())) {
                            viewModel.selectedAmenities.removeAt(viewModel.selectedAmenities.indexOf(s.id()))
                            viewModel.amenitiedId.value = viewModel.selectedAmenities
                            Log.d("selected ","amenities are "+viewModel.selectedAmenities)

                        } else {
                            viewModel.selectedAmenities.add(s.id()!!)
                            viewModel.amenitiedId.value = viewModel.selectedAmenities
                            Log.d("selected ","amenities are "+viewModel.selectedAmenities)
                        }
                        if (mBinding.rvStepOne.adapter != null) {
                            requestModelBuild()
                        }
                    })
                }
                if (i != it.listSettings()?.lastIndex){
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