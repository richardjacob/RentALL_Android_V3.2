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
import com.rentall.radicalstart.vo.PersonCount
import timber.log.Timber
import javax.inject.Inject


class TypeOfBedsFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_type_of_beds
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentTypeOfBedsBinding
   var strUser = ""


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
            mBinding.actionBar.tvRightside.onClick {
                viewModel.editBedCount = 0
                viewModel.updateCount.value!!.forEachIndexed { index, s ->
                    viewModel.editBedCount = viewModel.editBedCount + s.toInt()
                }
                if(viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount){
                    showSnackbar(getString(R.string.bed_count),getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count))
                }else {
                    hideSnackbar()
                    viewModel.retryCalled = "update"
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
            }
        }else{
            mBinding.actionBar.tvRightside.visibility = View.GONE
        }
        mBinding.actionBar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.actionBar.ivNavigateup.onClick {
            viewModel.editBedCount = 0
            viewModel.updateCount.value!!.forEachIndexed { index, s ->
                viewModel.editBedCount = viewModel.editBedCount + s.toInt()
            }
            if(viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount){
                showSnackbar(getString(R.string.bed_count),getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count))
            }else {
                hideSnackbar()
                if (viewModel.isEdit){
                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
                }else{
                    viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.NO_OF_GUEST)
                    //requireActivity().finish()
                }
            }
        }
        mBinding.tvNext.setOnClickListener {
          //  Utils.clickWithDebounce(mBinding.tvNext){
                viewModel.onContinueClick(StepOneViewModel.NextScreen.NO_OF_BATHROOM)
           // }
        }


        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.typeOfBeds.value.let {
            it?.let {
                setUp(it)
            }
        }
        viewModel.bedType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.guestCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.personCapacity1.set(viewModel.guestCapacity.value)
        })
        viewModel.typeOfBeds.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.updateCount.observe(viewLifecycleOwner, Observer {
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


    private fun setUp(it: java.util.ArrayList<PersonCount>) {
        try {
            mBinding.rvStepOne.withModels {

                viewholderUserName {
                    id("how many beds")
                    name(getString(R.string.how_many_beds_can_guest_use))
                }
                it?.forEachIndexed { i, s ->
                    Log.d("size of ", "options is " + it.size)
                    viewholderHostPlusMinus {
                        id("bed type $i")
                        text(s.itemName)
                        personCapacity1(viewModel.updateCount.value!![i])
                        plusLimit1(s.endValue)
                        minusLimit1(0)
                        clickPlus(View.OnClickListener {
                            if(viewModel.bedCapacity.get()!!.toInt() > viewModel.totalBedCount) {
                                viewModel.totalBedCount = viewModel.totalBedCount + 1
                                Log.d("inside", "plus click")
                                val list = viewModel.updateCount.value
                                list?.set(i, list.get(i).toInt().plus(1).toString())
                                viewModel.updateCount.value = list
                                val data = viewModel.typeOfBeds.value!![i]
                                data.updatedCount = viewModel.updateCount.value!![i].toInt()
                                viewModel.typeOfBeds.value!![i] = data
                                viewModel.becomeHostStep1.value!!.bedCount = i
                                viewModel.bedTypesId.value = viewModel.selectedBeds
                            }else{
                                showSnackbar(getString(R.string.bed_count),getString(R.string.maximum_bed_count_is_selected))
                            }
                        })
                        clickMinus(View.OnClickListener {
                            if(viewModel.bedCapacity.get()!!.toInt() <= viewModel.totalBedCount){
                                hideSnackbar()
                            }
                            viewModel.totalBedCount = viewModel.totalBedCount - 1
                            val list = viewModel.updateCount.value
                            list?.set(i, list.get(i).toInt().minus(1).toString())
                            viewModel.updateCount.value = list
                            val data = viewModel.typeOfBeds.value!![i]
                            data.updatedCount = viewModel.updateCount.value!![i].toInt()
                            viewModel.typeOfBeds.value!![i] = data
                            viewModel.becomeHostStep1.value!!.bedCount = i
                            viewModel.bedTypesId.value = viewModel.selectedBeds

                        })
                    }
                    if (i != it!!.lastIndex){
                        viewholderDivider {
                            id("Divider - " + i)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError()
        }
    }

    override fun onRetry() {

    }
}