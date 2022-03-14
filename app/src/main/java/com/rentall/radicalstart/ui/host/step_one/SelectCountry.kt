package com.rentall.radicalstart.ui.host.step_one

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostSelectCountryBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class SelectCountry : BaseFragment<HostSelectCountryBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_select_country
    override val viewModel: StepOneViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostSelectCountryBinding
    private var list = ArrayList<GetCountrycodeQuery.Result>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
        subscribeToLiveData()
        mBinding.ivClose.onClick {
            hideKeyboard()
            baseActivity?.onBackPressed()
        }
        viewModel.list.value?.let {
            viewModel.listSearch.value = java.util.ArrayList(it)
        }
    }

    private fun subscribeToLiveData() {
        viewModel.list.observe(this, Observer {
            it?.let { result ->
                list = ArrayList(result)
                setUp()
            }
        })
        viewModel.listSearch.observe(this, Observer {
            it?.let { result ->
                list = result
                if (mBinding.rlCountryCodes.adapter != null){
                    mBinding.rlCountryCodes.requestModelBuild()
                }

            }
        })
    }

    private fun setUp() {
        mBinding.rlCountryCodes.withModels {
            viewholderSavedPlaceholder {
                id("country")
                header(getString(R.string.select_country))
                large(true)
                isBlack(true)
            }
            if (list.isNotEmpty()) {
                for (index in 0 until list.size) {
                    viewholderCountryCodes {
                        id("countries - $index")
                        header(list[index].countryName())
                        large(false)
                        isWhite(false)
                        switcher(true)
                        onClick(View.OnClickListener {
                            list[index].countryName()?.let {
                                viewModel.country.set(it)
                                viewModel.countryCode.set(list[index].countryCode())
                                baseActivity?.onBackPressed()
                            }
                        })
                    }
                }
            } else {
                viewholderCountryCodes {
                    id("noresult")
                    header(getString(R.string.result_not_found))
                    large(false)
                    switcher(false)
                }
            }
        }
    }

    override fun onRetry() {
        viewModel.getCountryCode()
    }
}