package com.rentall.radicalstart.host.payout.addPayout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentAddPayoutAccountDetailsBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.profile.currency.CurrencyDialog
import com.rentall.radicalstart.util.RxBus
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class PayoutPaypalDetailsFragment : BaseFragment<FragmentAddPayoutAccountDetailsBinding, AddPayoutViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_add_payout_account_details
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(AddPayoutViewModel::class.java)
    lateinit var mBinding: FragmentAddPayoutAccountDetailsBinding
    private var eventCompositeDisposal = CompositeDisposable()
    lateinit var fragment: DialogFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        initRxBus()
    }

    private fun initRxBus() {
        eventCompositeDisposal.add(RxBus.listen(Array<String>::class.java)
                .subscribe { event ->
                    event?.let {
                        viewModel.currency.set(it[0]) // paypal currency dialog's selection
                        fragment.dismiss()
                        mBinding.rlAddPayout.requestModelBuild()
                    }
                })
    }

    private fun initView() {
        mBinding.btnNext.text =  resources.getString(R.string.finish)
        mBinding.btnNext.onClick { checkDetails() }
        mBinding.rlAddPayout.withModels {
            viewholderCenterText {
                id("12")
                text(resources.getString(R.string.address_of_payout))
                paddingTop(true)
            }
            viewholderListingDetailsSectionHeader {
                id("sectionheader")
                header(resources.getString(R.string.paypal))
            }
            viewholderListingDetailsDesc {
                id("df")
                desc(resources.getString(R.string.paypal_desc))
            }
            viewholderPayoutPaypalDetails {
                id(14)
                email(viewModel.email)
                currency(viewModel.currency)
                payoutCurrency(resources.getString(R.string.currency_payout)+" "+viewModel.currency.get())
                currencyClick { _ ->
                    viewModel.currency.get()?.let {
                        fragment = CurrencyDialog.newInstance(it)
                        (fragment as CurrencyDialog).show(childFragmentManager)
                    }
                }
            }
        }
    }

    fun checkDetails() {
        if (viewModel.checkPaypalInfo() && viewModel.checkAccountInfo()) {
            viewModel.addPayout(1)
        }
    }

    override fun onDestroy() {
        viewModel.email.set("")
        viewModel.currency.set("")
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    override fun onRetry() {
        checkDetails()
    }
}