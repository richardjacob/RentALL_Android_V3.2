package com.rentall.radicalstart.ui.host.step_two

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.R
import com.rentall.radicalstart.databinding.HostFragmentListDescBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import com.rentall.radicalstart.viewholderListDescEt
import com.rentall.radicalstart.viewholderUserName
import com.rentall.radicalstart.viewholderUserNormalText
import javax.inject.Inject

class AddListDescFragment : BaseFragment<HostFragmentListDescBinding,StepTwoViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentListDescBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_desc
    override val viewModel: StepTwoViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepTwoViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.descToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.descToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.descToolbar.tvRightside.onClick {
                if(viewModel.desc.get().equals("")){
                    showSnackbar( "Please add a description to your list.", "Add description")
                }else {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
            }
        }else{
            mBinding.descToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.descToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.descToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed() }
        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        mBinding.rvListDesc.withModels {
            viewholderUserName {
                id("header")
                name(getString(R.string.edit_desc))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("desc_sub_text")
                text(getString(R.string.summary))
                paddingBottom(true)
                paddingTop(false)
            }

            viewholderListDescEt {
                id("desc")
                text(viewModel.desc)
                title(getString(R.string.desc))
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