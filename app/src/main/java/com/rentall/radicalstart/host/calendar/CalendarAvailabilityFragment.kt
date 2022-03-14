package com.rentall.radicalstart.host.calendar

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.*
import com.rentall.radicalstart.databinding.FragmentCalendarAvailabilityBinding
import com.rentall.radicalstart.ui.base.BaseFragment
import com.rentall.radicalstart.ui.host.hostHome.HostHomeActivity
import com.rentall.radicalstart.util.Utils.Companion.getMonth
import com.rentall.radicalstart.util.Utils.Companion.isNumber
import com.rentall.radicalstart.util.onClick
import com.rentall.radicalstart.util.withModels
import javax.inject.Inject

class CalendarAvailabilityFragment : BaseFragment<FragmentCalendarAvailabilityBinding, CalendarListingViewModel>(), CalendarAvailabilityNavigator {


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCalendarAvailabilityBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_calendar_availability
    override val viewModel: CalendarListingViewModel
        get() = ViewModelProviders.of(baseActivity!!, mViewModelFactory).get(CalendarListingViewModel::class.java)
    private var selectArray = arrayOf(true, false)
    private var isSelected = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.incClose.ivNavigateup.onClick {
            hideKeyboard()
            if (viewModel.navigateBack.get()==true){
                openHostCalendar()
            }else{
                baseActivity!!.onBackPressed()
            }
        }
        mBinding.tvNext.onClick {
            if (selectArray[0]) {
                checkPrice()
            } else if (selectArray[1]) {
                viewModel.calendarStatus.set("blocked")
                viewModel.updateBlockedDates()
            }
        }

        mBinding.rlCalendarEdit.withModels {
            viewholderListingDetailsSectionHeader {
                id(54)
                header(resources.getString(R.string.availability))
            }
            viewholderListingDetailsSectionHeader {
                id(324)
                header(setDateInCalendar(viewModel.startDate.value.toString(), viewModel.endDate.value.toString()))
            }
            viewholderReportUserRadio {
                id("2")
                text(resources.getString(R.string.available))
                radioVisibility(selectArray[0])
                onClick(View.OnClickListener { selector(0) })
            }
            viewholderDivider {
                id("Divider - 2")
            }
            viewholderReportUserRadio {
                id("3")
                text(resources.getString(R.string.blocked))
                radioVisibility(selectArray[1])
                onClick(View.OnClickListener { hideKeyboard(); selector(1) })
            }
            if (selectArray[0]) {
                viewholderSpecialpriceEt {
                    id("23")
                    title(resources.getString(R.string.specialPrice))
                    hint(getString(R.string.price_per_night))
                    text(viewModel.specialPrice)
                }
            }
        }
    }

    fun checkPrice() {
            val f =viewModel.specialPrice.get()!!.toFloatOrNull()
            if(f!=null){
                val i=f.toInt()
                if(i!=0){
                    viewModel.calendarStatus.set("available")
                    viewModel.updateBlockedDates()
                }else{
                    showToast(getString(R.string.spl_price_valid))
                }
            }else{
                viewModel.calendarStatus.set("available")
                viewModel.updateBlockedDates()
            }
    }

    private fun openHostCalendar() {
        var intent : Intent? =  null

        intent = Intent(baseActivity, HostHomeActivity::class.java)
        intent?.putExtra("from", "calendar")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        baseActivity!!.startActivity(intent)
    }


    private fun selector(index: Int) {
        selectArray.forEachIndexed { i: Int, _: Boolean ->
            selectArray[i] = index == i
            isSelected = true
        }
        mBinding.rlCalendarEdit.requestModelBuild()
    }

    fun setDateInCalendar(selStartDate: String, selEndDate: String?): String {
        return try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
            if (selEndDate == "null") {
                val startMonthName = getMonth(langType!!,selStartDate)
                "$startMonthName ${selStartDate.split("-")[2]}"
            } else {
                val startMonthName = getMonth(langType!!,selStartDate)
                val endMonthName = getMonth(langType,selEndDate!!)
                if (startMonthName == endMonthName) {
                    "$startMonthName ${selStartDate.split("-")[2]} - ${selEndDate.split("-")[2]}"
                } else {
                    "$startMonthName ${selStartDate.split("-")[2]} - $endMonthName ${selEndDate.split("-")[2]}"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun onDestroyView() {
       viewModel.specialPrice.set("")
        super.onDestroyView()
    }

    override fun moveBackToScreen() {
        baseActivity?.onBackPressed()
    }

    private fun subscribeToLiveData() {
    }

    fun onRefresh() {
    }

    override fun onRetry() {

    }

    override fun closeAvailability(flag: Boolean) {
    }

    override fun hideCalendar(flag: Boolean) {

    }

    override fun hideWholeView(flag: Boolean) {
    }
}