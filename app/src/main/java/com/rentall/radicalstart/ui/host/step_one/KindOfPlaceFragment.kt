package com.rentall.radicalstart.ui.host.step_one

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentTypeOfBedsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.HostFinalActivity
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class KindOfPlaceFragment : BaseFragment<HostFragmentTypeOfBedsBinding, StepOneViewModel>() {

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
        if (baseActivity?.getIntent()!!.hasExtra("from")){
            strUser = baseActivity?.getIntent()!!.getStringExtra("from").orEmpty()
            if (strUser.isNotEmpty() && strUser.equals("steps"))
                viewModel.isEdit = true
            else
                viewModel.isEdit = false
        }
        if(viewModel.isListAdded) {
            mBinding.actionBar.tvRightside.text = getText(R.string.save_and_exit)
            mBinding.actionBar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_bar_color))
            mBinding.actionBar.tvRightside.onClick {  viewModel.retryCalled = "update"
                viewModel.address.set("")
                viewModel.location.set("")
                if (viewModel.isEdit){
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.countryCode.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                }else{
                    viewModel.address.set(viewModel.street.get() + ", " + viewModel.country.get() + ", " + viewModel.state.get() + ", " + viewModel.city.get())
                }
                viewModel.getLocationFromGoogle(viewModel.address.get().toString(),false){
                    if (viewModel.country.get()!!.trim().isNullOrEmpty() || viewModel.street.get()!!.trim().isNullOrEmpty() || viewModel.city.get()!!.trim().isNullOrEmpty() || viewModel.state.get()!!.trim().isNullOrEmpty() || viewModel.zipcode.get()!!.trim().isNullOrEmpty()) {
                    baseActivity!!.showSnackbar(resources.getString(R.string.it_seems_you_have_missed_some_required_fields_in_address_page), resources.getString(R.string.please_fill_them))
                } else if (viewModel.isEdit && viewModel.location.get().isNullOrEmpty()) {
                    if (isNetworkConnected){
                        baseActivity!!.showSnackbar(getString(R.string.error_1), getString(R.string.incorrect_location))
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
        mBinding.actionBar.ivNavigateup.onClick {
            if (viewModel.isEdit){
                val intent = Intent(context, HostFinalActivity::class.java)
                intent.putExtra("listId", viewModel.listId.get())
                intent.putExtra("yesNoString", "Yes")
                intent.putExtra("bathroomCapacity", "0")
                intent.putExtra("country", "")
                intent.putExtra("countryCode","")
                intent.putExtra("street", "")
                intent.putExtra("buildingName", "")
                intent.putExtra("city", "")
                intent.putExtra("state", "")
                intent.putExtra("zipcode", "")
                intent.putExtra("lat","")
                intent.putExtra("lng","")
                startActivity(intent)
                baseActivity?.finish()

            }
            else {
                viewModel.navigator.navigateBack(StepOneViewModel.BackScreen.TYPE_OF_SPACE)
            }
        }
/*            Utils.clickWithDebounce(mBinding.tvNext){
                viewModel.onContinueClick(StepOneViewModel.NextScreen.NO_OF_GUEST)
            }*/
        mBinding.tvNext.onClick {
            viewModel.onContinueClick(StepOneViewModel.NextScreen.NO_OF_GUEST)
        }
        subscribeToLiveData()
        setUp()
    }

    private fun subscribeToLiveData() {
        viewModel.houseType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.roomSizeType.observe( viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.yesNoType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.becomeHostStep1.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
        viewModel.roomType.observe(viewLifecycleOwner, Observer {
            requestModelBuildIt()
        })
    }

    fun requestModelBuildIt() {
        if (mBinding.rvStepOne.adapter != null) {
            mBinding.rvStepOne.requestModelBuild()
        }
    }

    private fun setUp() {
        mBinding.rvStepOne.withModels {
            viewholderUserName{
                id("kind of place")
                name(getString(R.string.what_kind_of_place_are_you_listing))
                paddingTop(false)
                paddingBottom(true)
            }
            viewholderHostStepOne {
                id("type of property")
                step(getString(R.string.what_type_of_property_is_this))
                textSize(true)
                paddingTop(false)
                paddingBottom(false)
                isBlack(false)
                visibility(false)
            }
            viewholderHostStepOne {
                id("entire place")
                step(viewModel.houseType.value)
                textSize(true)
                paddingTop(true)
                paddingBottom(true)
                isBlack(true)
                visibility(false)
                clickListener(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("houseOptions").show(childFragmentManager, "houseOptions")
                    requestModelBuild()                })
            }
            viewholderDivider {
                id("Divider - 1" )
            }
            viewholderHostStepOne {
                id("what guest have")
                step(getString(R.string.what_will_guest_have))
                textSize(true)
                paddingTop(true)
                paddingBottom(false)
                isBlack(false)
                visibility(false)
            }
            viewholderHostStepOne {
                id("type of room")
                step(viewModel.roomType.value)
                textSize(true)
                paddingTop(true)
                paddingBottom(true)
                isBlack(true)
                visibility(false)
                clickListener(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("placeOptions").show(childFragmentManager, "placeOptions")
                                    })
            }
            viewholderDivider {
                id("Divider - 2" )
            }
            viewholderHostStepOne {
                id("how many rooms")
                step(getString(R.string.how_many_total_rooms_does_your_property_have))
                textSize(true)
                paddingTop(true)
                paddingBottom(false)
                isBlack(false)
                visibility(false)
            }
            viewholderHostStepOne {
                id("no of room")
                step(viewModel.roomSizeType.value)
                textSize(true)
                paddingTop(true)
                paddingBottom(true)
                isBlack(true)
                visibility(false)
                clickListener(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("roomSizeOptions").show(childFragmentManager, "roomSizeOptions")
                })
            }
            viewholderDivider {
                id("Divider - 3" )
            }
            viewholderHostStepOne {
                id("is this personal")
                step(getString(R.string.ss_this_your_personal_home))
                textSize(true)
                paddingTop(true)
                paddingBottom(false)
                isBlack(false)
                visibility(false)
            }
            viewholderHostStepOne {
                id("yes/no")
                step(viewModel.yesNoType.value)
                textSize(true)
                paddingTop(true)
                paddingBottom(true)
                isBlack(true)
                visibility(false)
                clickListener(View.OnClickListener {
                    StepOneOptionsFragment.newInstance("yesNoOptions").show(childFragmentManager, "yesNoOptions")
                })
                viewModel.yesNoString.set(viewModel.yesNoType.value)
                if (viewModel.yesNoString?.get().equals("Yes")){
                    viewModel.becomeHostStep1.value!!.yesNoOptions = "1"
                }


            }
            viewholderDivider {
                id("Divider - 4" )
            }
            viewholderHostStepOne {
                id("guests like to know")
                step(getString(R.string.guests_like_to_know_if_this_is_a_place_you_live_and_keep_your_personal_belongings_in))
                textSize(false)
                paddingTop(false)
                paddingBottom(true)
                isBlack(false)
                visibility(false)
            }
        }
    }
    override fun onRetry() {

    }

    }