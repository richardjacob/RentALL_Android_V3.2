package com.rentall.radicalstart.host.payout.addPayout

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentAddPayoutAccountDetailsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.OptionsSubFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class PayoutAccountDetailFragment : BaseFragment<FragmentAddPayoutAccountDetailsBinding, AddPayoutViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_add_payout_account_details
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(AddPayoutViewModel::class.java)
    lateinit var mBinding: FragmentAddPayoutAccountDetailsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.accountType.set(getString(R.string.individual))
        viewModel.lastNameVisible.value = false
        initView()
        viewModel.lastNameVisible.observe(viewLifecycleOwner, Observer {
            it.let {
                mBinding.rlAddPayout.requestModelBuild()
            }
        })
    }

    private fun initView() {
        getString(R.string.individual)
        mBinding.btnNext.text = resources.getString(R.string.finish)
        mBinding.btnNext.onClick {
           checkDetails()
        }
        mBinding.rlAddPayout.withModels {
            viewholderCenterText {
                id("12")
                text(resources.getString(R.string.account_details_of_payout))
                paddingTop(true)
            }

            viewholderPayoutAccountDetails {
                id("qwe")
                accountType(viewModel.accountType)
                onClick(View.OnClickListener {
                    BottomSheetFragment.newInstance("dates").show(childFragmentManager, "dates")
                })
                lastNameVisible(viewModel.lastNameVisible.value!!)
                firstName(viewModel.firstName)
                lastName(viewModel.lastName)
                accountNo(viewModel.account)
                cnfAccountNo(viewModel.cnfAccount)
                routingNo(viewModel.routingNo)

                if (viewModel.europeCountries.contains(viewModel.countryCode.get()))
                {
                    accountHint(getString(R.string.iban_number))
                    isRoutingVisible(false)
                }
                else if (viewModel.countryCode.get() == "MX" || viewModel.countryCode.get() == "NZ")
                {
                    accountHint(getString(R.string.account_number))
                    isRoutingVisible(false)
                }
                else if (viewModel.countryCode.get() == "UK" || viewModel.countryCode.get() == "GB") {
                    accountHint(getString(R.string.account_number))
                    isRoutingVisible(true)
                    routingHint(getString(R.string.sort))
                }
                else{
                    accountHint(getString(R.string.account_number))
                    isRoutingVisible(true)
                    routingHint(getString(R.string.routing_number))
                }
                if(viewModel.country.get().equals("Canada")){
                    countryCheck(true)
                    offsetPos(5)
                }

                //ssnNo(viewModel.ssn)
            }
        }
    }

    override fun onDestroy() {
        viewModel.firstName.set("")
        viewModel.lastName.set("")
        viewModel.account.set("")
        viewModel.cnfAccount.set("")
        viewModel.routingNo.set("")
        viewModel.ssn.set("")
        super.onDestroy()
    }

    fun checkDetails() {
        if (viewModel.checkAccountInfo() && viewModel.checkAccountDetails()) {
            viewModel.getResultAccountToken(2 , viewModel.buildTokenHashMap())
        }
    }

    override fun onRetry() {
        checkDetails()
    }
}