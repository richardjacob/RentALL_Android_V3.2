package com.rentall.radicalstart.ui.dobcalendar

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.rentall.radicalstart.R
import com.rentall.radicalstart.ui.base.BaseDialogFragment
import java.util.*
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class BirthdayDialog : BaseDialogFragment() {

    private val TAG = BirthdayDialog::class.java.simpleName
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: BirthdayViewModel
        get() = ViewModelProviders.of(this, mViewModelFactory).get(BirthdayViewModel::class.java)
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    var onDateSet: DatePickerDialog.OnDateSetListener? = null
    lateinit var datePickerDialog: DatePickerDialog

    companion object {
        fun newInstance(selYear: Int, selMonth: Int, selDay: Int) =
                BirthdayDialog().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM1, selYear)
                        putInt(ARG_PARAM2, selMonth)
                        putInt(ARG_PARAM3, selDay)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            year = it.getInt(ARG_PARAM1)
            month = it.getInt(ARG_PARAM2)
            day = it.getInt(ARG_PARAM3)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Log.d("date", year.toString() +" "+ month +" "+ day)
        datePickerDialog = if ( (year != 0 && day != 0) || month != 0) {
            DatePickerDialog(context!!,
                    R.style.CustomDatePickerDialogTheme, onDateSet, year, month, day)
        } else {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context!!,
                    R.style.CustomDatePickerDialogTheme, onDateSet, year, month, day)
        }
        val tv = TextView(context)
        val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT )
        lp.setMargins(50,50,50,50)
        tv.layoutParams = lp
        tv.setPadding(10, 10, 10, 10)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        val typeface = ResourcesCompat.getFont(context!!, R.font.linetocircular_bold)
        tv.typeface = typeface
        tv.text = resources.getString(R.string.set_your_birthday)
        datePickerDialog.setCustomTitle(tv)
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, resources.getString(R.string.okay), datePickerDialog)
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        return datePickerDialog
    }

    fun dismissDialog() {
        dismissDialog(TAG)
    }

    fun setCallBack(ondate: DatePickerDialog.OnDateSetListener) {
        onDateSet = ondate
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }
}