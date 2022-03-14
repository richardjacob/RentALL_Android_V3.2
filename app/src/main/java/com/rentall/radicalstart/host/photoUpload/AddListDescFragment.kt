package com.rentall.radicalstart.host.photoUpload

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentListDescBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.Utils
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListDescEt
import com.rentall.radicalstart.viewholderUserName
import javax.inject.Inject


class AddListDescFragment : BaseFragment<HostFragmentListDescBinding, Step2ViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentListDescBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_desc
    override val viewModel: Step2ViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(Step2ViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.descToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.descToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.tvNext.onClick {
            Utils.clickWithDebounce(mBinding.tvNext){
                if(viewModel.checkFilledData()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
            }
        }
        subscribeToLiveData()
    }

    fun initView() {
        if(viewModel.getListAddedStatus()) {
            mBinding.descToolbar.tvRightside.visibility = View.GONE
        } else {
            mBinding.descToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.descToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.descToolbar.tvRightside.onClick {
                if(viewModel.desc.get()?.trim().isNullOrEmpty()) {
                    showSnackbar( getString(R.string.add_desc_alert), getString(R.string.add_description))
                } else {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
            }
        }
    }

    fun subscribeToLiveData(){
        viewModel.step2Result.observe(viewLifecycleOwner, Observer {
            it?.let {
                initView()
                setup()
            }
        })
    }

    fun setup() {
        mBinding.rvListDesc.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.edit_desc))
                paddingTop(true)
                paddingBottom(true)
            }

//            viewholderUserNormalText {
//                id("desc_sub_text")
//                text(getString(R.string.summary))
//                paddingBottom(true)
//                paddingTop(false)
//            }

            viewholderListDescEt {
                id("desc")
                text(viewModel.desc)
                title(getString(R.string.summary))
                hint(getString(R.string.desc_hint))
                maxLength(1000)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.requestFocus()
                    editText.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(false)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.setOnTouchListener(null)
                }
            }
        }
    }

    override fun onRetry() {
        viewModel.getListDetailsStep2()
    }
}