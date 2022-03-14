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
import javax.inject.Inject

class GuestSpacesFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {


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
            mBinding.actionBar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
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
        Log.d("yes no string ","is "+viewModel.yesNoString.get())

        mBinding.tvNext.setOnClickListener {
            Utils.clickWithDebounce(it){
                viewModel.retryCalled = "update"
                viewModel.updateHostStepOne(true)
            }
        }





//            viewModel.manageSteps()
//        val intent = Intent(baseActivity!!, HostFinalActivity::class.java)
//            intent.putExtra("listId", viewModel.listId.get())
//            intent.putExtra("yesNoString",viewModel.yesNoString.get())
//            intent.putExtra("bathroomCapacity",viewModel.bathroomCapacity.get())
//            intent.putExtra("country",viewModel.country.get())
//            intent.putExtra("countryCode",viewModel.countryCode.get())
//            intent.putExtra("street",viewModel.street.get())
//            intent.putExtra("buildingName",viewModel.buildingName.get())
//            intent.putExtra("city",viewModel.city.get())
//            intent.putExtra("state",viewModel.state.get())
//            intent.putExtra("zipcode",viewModel.zipcode.get())
//            intent.putExtra("lat",viewModel.lat.get())
//            intent.putExtra("lng",viewModel.lng.get())
//            baseActivity!!.startActivity(intent)

        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.sharedSpaceList.observe(viewLifecycleOwner, Observer {
            setUp(it)
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            if (mBinding.rvStepOne.adapter != null) {
                mBinding.rvStepOne.requestModelBuild() }
        })
    }

    private fun setUp(it: GetListingSettingQuery.Spaces) {
        mBinding.rvStepOne.withModels {
            viewholderUserName {
                id("what spaces ?")
                name(getString(R.string.what_spaces_can_guests_use))
            }
            it.listSettings()!!.forEachIndexed { i, s ->
                viewholderHostCheckbox {
                    id("shared space $i")
                    visibility(false)
                    text(s.itemName())
                    isChecked(viewModel.selectedSpace.contains(s.id()))
                    onClick(View.OnClickListener {
                        if (viewModel.selectedSpace.contains(s.id())){
                            viewModel.selectedSpace.removeAt(viewModel.selectedSpace.indexOf(s.id()))
                            viewModel.spacesId.value = viewModel.selectedSpace
                        }else{
                            s.id()?.let {
                                viewModel.selectedSpace.add(it)
                                viewModel.spacesId.value = viewModel.selectedSpace
                            }
                        }
                        requestModelBuild()
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