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

class NoOfGuestFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

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
        mBinding.actionBar.ivNavigateup.onClick {
            if (viewModel.isEdit){
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.KIND_OF_PLACE)
            }else{
                baseActivity?.onBackPressed() }
        }
        mBinding.tvNext.onClick {
                viewModel.onContinueClick(StepOneViewModel.NextScreen.TYPE_OF_BEDS)
        }
        viewModel.personCapacity1.set("1")
        viewModel.roomCapacity.set("1")
        viewModel.bedCapacity.set("1")
        subscribeToLiveData()
        setUp()
    }

    private fun subscribeToLiveData() {
        viewModel.guestCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.personCapacity1.set(viewModel.guestCapacity.value)
            requestModelBuildIt()
        })
        viewModel.roomNoCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.roomCapacity.set(viewModel.roomNoCapacity.value)
            requestModelBuildIt()
        })
        viewModel.bedNoCapacity.observe(viewLifecycleOwner, Observer {
            viewModel.bedCapacity.set(viewModel.bedNoCapacity.value)
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
                    id("how many guests")
                    name(getString(R.string.how_many_guests_can_stay))
                }
                viewholderHostPlusMinus {
                    id("total guest")
                    if(viewModel.personCapacity1.get().toString().equals("1")){
                        text(getString(R.string.total_guest))
                    }else{
                        text(getString(R.string.total_guests))
                    }
                    personCapacity1(viewModel.personCapacity1.get().toString())
                    plusLimit1(viewModel.personCapacity.value!!.listSettings()!![0].endValue())
                    minusLimit1(viewModel.personCapacity.value!!.listSettings()!![0].startValue())
                    clickPlus(View.OnClickListener {
                        Log.d("inside","plus click")
                        viewModel.personCapacity1.get()?.let {
                            viewModel.personCapacity1.set(it.toInt().plus(1).toString())
                            viewModel.guestCapacity.value = viewModel.personCapacity1.get()
                            viewModel.becomeHostStep1.value!!.totalGuestCount = viewModel.personCapacity1.get()!!.toInt()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.personCapacity1.get()?.let {
                            viewModel.personCapacity1.set(it.toInt().minus(1).toString())
                            viewModel.guestCapacity.value = viewModel.personCapacity1.get()
                            viewModel.becomeHostStep1.value!!.totalGuestCount = viewModel.personCapacity1.get()!!.toInt()
                        }
                    })
                }
                viewholderDivider {
                    id("divider 1")
                }
                viewholderHostPlusMinus {
                    id("total bedrooms")
                    if(viewModel.roomCapacity.get().toString().equals("1")){
                        text(getString(R.string.bedroom_for_guest))
                    }else {
                        text(getString(R.string.Bedroom_for_guests))
                    }
                    personCapacity1(viewModel.roomCapacity.get().toString())
                    plusLimit1(viewModel.bedroomlist.value!!.listSettings()!![0].endValue())
                    minusLimit1(viewModel.bedroomlist.value!!.listSettings()!![0].startValue())
                    clickPlus(View.OnClickListener {
                        Log.d("inside","plus click")
                        viewModel.roomCapacity.get()?.let {
                            viewModel.roomCapacity.set(it.toInt().plus(1).toString())
                            viewModel.roomNoCapacity.value = viewModel.roomCapacity.get()
                            viewModel.becomeHostStep1.value!!.bedroomCount = viewModel.roomCapacity.get()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.roomCapacity.get()?.let {
                            viewModel.roomCapacity.set(it.toInt().minus(1).toString())
                            viewModel.roomNoCapacity.value = viewModel.roomCapacity.get()
                            viewModel.becomeHostStep1.value!!.bedroomCount = viewModel.roomCapacity.get()
                        }
                    })
                }
                viewholderDivider {
                    id("divider 2")
                }
                viewholderHostPlusMinus {
                    id("total beds")
                    if(viewModel.bedCapacity.get().toString().equals("1")){
                        text(getString(R.string.bed_for_guest))
                    }else {
                        text(getString(R.string.bed_for_guests))
                    }
                    personCapacity1(viewModel.bedCapacity.get().toString())
                    plusLimit1(viewModel.personCapacity.value!!.listSettings()!![0].endValue())
                    minusLimit1(viewModel.personCapacity.value!!.listSettings()!![0].startValue())
                    clickPlus(View.OnClickListener {
                        Log.d("inside","plus click")
                        viewModel.bedCapacity.get()?.let {
                            viewModel.bedCapacity.set(it.toInt().plus(1).toString())
                            viewModel.bedNoCapacity.value = viewModel.bedCapacity.get()
                            viewModel.becomeHostStep1.value!!.beds = viewModel.bedCapacity.get()!!.toInt()
                        }
                    })
                    clickMinus(View.OnClickListener {
                        viewModel.bedCapacity.get()?.let {
                            viewModel.bedCapacity.set(it.toInt().minus(1).toString())
                            viewModel.bedNoCapacity.value = viewModel.bedCapacity.get()
                            viewModel.becomeHostStep1.value!!.beds = viewModel.bedCapacity.get()!!.toInt()
                        }
                    })
                }
            }
        } catch (E: Exception) {
            showError()
        }
    }

    override fun onRetry() {
    }
}