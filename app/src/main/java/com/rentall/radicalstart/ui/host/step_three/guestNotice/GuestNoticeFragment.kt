package com.rentall.radicalstart.ui.host.step_three.guestNotice

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.HostFragmentGuestNoticeBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.step_three.OptionsSubFragment
import com.rentall.radicalstart.ui.host.step_three.StepThreeViewModel
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class GuestNoticeFragment : BaseFragment<HostFragmentGuestNoticeBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentGuestNoticeBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_guest_notice
    override val viewModel: StepThreeViewModel
        get() = ViewModelProviders.of(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        if(viewModel.isListAdded) {
            mBinding.guestNoticeToolbar.tvRightside.text = getText(R.string.save_exit)
            mBinding.guestNoticeToolbar.tvRightside.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.guestNoticeToolbar.tvRightside.onClick {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()) {

                    if(viewModel.fromChoosen=="Flexible" || viewModel.toChoosen=="Flexible"){
                        viewModel.retryCalled = "update"
                        viewModel.updateListStep3("edit")
                    }else{
                        if (viewModel.fromChoosen.toInt() >= viewModel.toChoosen.toInt()) {
                            viewModel.isSnackbarShown = true
                            showSnackbar(getString(R.string.time), getString(R.string.checkin_error_text))
                        }else{
                            viewModel.retryCalled = "update"
                            viewModel.updateListStep3("edit")
                        }
                    }
                }
            }
        }else{
            mBinding.guestNoticeToolbar.tvRightside.visibility = View.GONE
        }
        mBinding.guestNoticeToolbar.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.guestNoticeToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }

        subscribeToLiveData()
        observeOption()
    }

    fun subscribeToLiveData(){
        viewModel.listSettingArray.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (mBinding.rvGuestNotice.adapter != null) {
                    mBinding.rvGuestNotice.requestModelBuild()
                } else {
                    setUI()
                }
            }

        })
    }

    fun observeOption(){

        viewModel.listDetailsStep3.observe(viewLifecycleOwner, Observer {
            if(isAdded) {
                if (mBinding.rvGuestNotice.adapter != null) {
                    mBinding.rvGuestNotice.requestModelBuild()
                }
            }
        })
    }

    fun setUI(){
        mBinding.rvGuestNotice.withModels {

            viewModel.listDetailsStep3.value?.let { listDetailsStep3 ->
                viewholderUserName {
                    id("header")
                    name(getString(R.string.notice_header))
                    paddingBottom(true)
                    paddingTop(false)
                }

                viewholderUserName {
                    id("bookingWindow")
                    name(getString(R.string.booking_window))
                    paddingTop(false)
                    paddingBottom(false)

                }

                viewholderItineraryTextBold {
                    id("advanceBooking")
                    text(getString(R.string.advance_notice))
                    isRed(false)
                    large(false)
                    paddingBottom(true)
                    paddingTop(true)
                }

                viewholderUserNormalText {
                    id("bookingText")
                    text(getString(R.string.adv_notice_text))
                    paddingTop(false)
                    paddingBottom(false)
                }

                viewholderListTv {
                    id("noticeOption")
                    hint(listDetailsStep3.noticePeriod)
                    etHeight(false)
                    maxLength(50)
                    onNoticeClick(View.OnClickListener {
                        OptionsSubFragment.newInstance("options").show(childFragmentManager, "options")
                    })
                }

                viewholderDivider {
                    id("optiondiv")
                }

                viewholderUserNormalText {
                    id("checkText")
                    text(getString(R.string.when_guest_checkin))
                    paddingTop(true)
                    paddingBottom(false)
                }

                viewholderUserNormalText {
                    id("from")
                    text(getString(R.string.from))
                    paddingTop(true)
                    paddingBottom(false)
                }

                viewholderListTv {
                    id("fromOptions")
                    hint(listDetailsStep3.noticeFrom)
                    etHeight(false)
                    maxLength(100)
                    onNoticeClick(View.OnClickListener {
                        OptionsSubFragment.newInstance("from").show(childFragmentManager, "from")
                    })
                }

                viewholderDivider {
                    id("fromdiv")
                }

                viewholderUserNormalText {
                    id("to")
                    text(getString(R.string.to))
                    paddingTop(true)
                    paddingBottom(false)
                }

                viewholderListTv {
                    id("toOptions")
                    hint(listDetailsStep3.noticeTo)
                    etHeight(false)
                    maxLength(100)
                    onNoticeClick(View.OnClickListener {
                        OptionsSubFragment.newInstance("to").show(childFragmentManager, "to")
                    })
                }

                viewholderDivider {
                    id("todiv")
                }
            }
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flSubFragment.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onRetry() {

    }
}