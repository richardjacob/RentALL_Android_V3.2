package com.rentall.radicalstart.ui.host.step_three.houseRules

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentHouseRuleBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class HouseRuleFragment : BaseFragment<HostFragmentHouseRuleBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentHouseRuleBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_house_rule
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
            if(viewModel.isListAdded) {
                mBinding.houseRuleToolbar.tvRightside.text = getText(R.string.save_exit)
                mBinding.houseRuleToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                mBinding.houseRuleToolbar.tvRightside.onClick {
                    if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {
                        viewModel.retryCalled = "update"
                        viewModel.updateListStep3("edit")
                    }
                }
            }else{
                mBinding.houseRuleToolbar.tvRightside.visibility = View.GONE
            }
        mBinding.houseRuleToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.houseRuleToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed() }

        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        viewModel.listSettingArray.observe(viewLifecycleOwner, Observer {
            it?.let {rulesList ->
                setUI()
            }

        })
    }

    fun setUI(){
        try {
            mBinding.rvHouseRule.withModels {
                viewholderUserName {
                    id("header")
                    name(getString(R.string.set_house_rule))
                    paddingTop(true)
                    paddingBottom(true)
                }

                viewholderUserNormalText {
                    id("subText")
                    text(getString(R.string.house_rule_sub))
                    paddingTop(false)
                    paddingBottom(true)
                }
                val rules = viewModel.listSettingArray.value!!.houseRules()!!.listSettings()
                rules?.forEachIndexed { index, s ->
                    viewholderFilterCheckbox {
                        id("rule"+index)
                        text(s.itemName())
                        isChecked(viewModel.selectedRules.contains(s.id()!!.toInt()))
                        onClick(View.OnClickListener {
                            if (viewModel.selectedRules.contains(s.id()!!.toInt())) {
                                viewModel.selectedRules.removeAt(viewModel.selectedRules.indexOf(s.id()))
                            } else {
                                viewModel.selectedRules.add(s.id()!!.toInt())
                            }
                            requestModelBuild()
                        })
                    }
                    if (index != rules.lastIndex){
                        viewholderDivider {
                            id("Divider - $index")
                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun onRetry() {

    }
}