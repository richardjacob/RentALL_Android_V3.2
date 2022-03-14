package com.rentall.radicalstart.ui.profile.setting.currency

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentOptionsBinding
import com.rentall.radicalstart.ui.base.BaseBottomSheet
import com.rentall.radicalstart.ui.profile.setting.SettingViewModel
import com.rentall.radicalstart.util.binding.BindingAdapters
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderDivider
import com.rentall.radicalstart.viewholderOptionText
import javax.inject.Inject

class CurrencyBottomSheet : BaseBottomSheet<HostFragmentOptionsBinding, SettingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentOptionsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_options
    override val viewModel: SettingViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(SettingViewModel::class.java)

    var type: String = ""

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
                CurrencyBottomSheet().apply {
                    arguments = Bundle().apply {
                        putString("type", type)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        arguments?.let {
            type = it.getString("type", "ert")
            Log.d("fg", "Fd+ $type")
        }
        mBinding.rvOptions.setHasFixedSize(true)
        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        mBinding.rvOptions.withModels {
            if (type.equals("currency")) {
                viewModel.currencies.value?.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s.symbol() + " - " + BindingAdapters.getCurrencySymbol(s.symbol()))
                        size(20.toFloat())

                        if (s.symbol().equals(viewModel.baseCurrency.get())) {
                            txtColor(true)
                            isSelected(true)
                        } else {
                            txtColor(false)
                            isSelected(false)
                        }
                        clickListener(View.OnClickListener {
                            viewModel.updateCurrency(s.symbol()!!)
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }
            } else {
                viewModel.langName.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        desc(s)
                        size(20.toFloat())

                        if (s == viewModel.appLanguage.get()) {
                            txtColor(true)
                            isSelected(true)
                        } else {
                            txtColor(false)
                            isSelected(false)
                        }
                        clickListener(View.OnClickListener {
                            val code = viewModel.langCode[index]
                            viewModel.updateLangauge(code, s)
                            dismiss()
                        })
                    }
                    viewholderDivider {
                        id(index)
                    }
                }
            }
        }
    }

    override fun onRetry() {

    }
}