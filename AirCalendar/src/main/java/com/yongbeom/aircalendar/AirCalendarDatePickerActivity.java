/***********************************************************************************
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2017 LeeYongBeom
 * https://github.com/yongbeam
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.yongbeom.aircalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.yongbeom.aircalendar.core.AirMonthAdapter;
import com.yongbeom.aircalendar.core.DatePickerController;
import com.yongbeom.aircalendar.core.DayPickerView;
import com.yongbeom.aircalendar.core.SelectModel;
import com.yongbeom.aircalendar.core.util.AirCalendarUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

import static com.yongbeom.aircalendar.core.util.AirCalendarUtils.showSnackBar;


public class AirCalendarDatePickerActivity extends AppCompatActivity implements DatePickerController {

    public final static String EXTRA_FLAG = "FLAG";
    public final static String EXTRA_IS_BOOIKNG = "IS_BOOING";
    public final static String EXTRA_IS_SELECT = "IS_SELECT";
    public final static String EXTRA_BOOKING_DATES = "BOOKING_DATES";
    public final static String EXTRA_SELECT_DATE_SY = "SELECT_START_DATE_Y";
    public final static String EXTRA_SELECT_DATE_SM = "SELECT_START_DATE_M";
    public final static String EXTRA_SELECT_DATE_SD = "SELECT_START_DATE_D";
    public final static String EXTRA_SELECT_DATE_EY = "SELECT_END_DATE_Y";
    public final static String EXTRA_SELECT_DATE_EM = "SELECT_END_DATE_M";
    public final static String EXTRA_SELECT_DATE_ED = "SELECT_END_DATE_D";
    public final static String EXTRA_IS_MONTH_LABEL = "IS_MONTH_LABEL";
    public final static String EXTRA_IS_SINGLE_SELECT = "IS_SINGLE_SELECT";
    public final static String EXTRA_ACTIVE_MONTH_NUM = "ACTIVE_MONTH_NUMBER";
    public final static String EXTRA_MAX_YEAR = "MAX_YEAR";
    public final static String EXTRA_MAX_BOOKING_DAY = "MAX_BOOKING_DAY";
    public final static String EXTRA_MIN_BOOKING_DAY = "MIN_BOOKING_DAY";

    public final static String RESULT_SELECT_START_DATE = "start_date";
    public final static String RESULT_SELECT_END_DATE = "end_date";
    public final static String RESULT_SELECT_START_VIEW_DATE = "start_date_view";
    public final static String RESULT_SELECT_END_VIEW_DATE = "end_date_view";
    public final static String RESULT_FLAG = "flag";
    public final static String RESULT_TYPE = "result_type";
    public final static String RESULT_STATE = "result_state";

    public final static String EXTRA_CALENDAR_TYPE = "calendar_type";


    private DayPickerView pickerView;
    private TextView tv_minimum_stay;
    private TextView tv_start_placeholder;
    private TextView tv_start_date;
    private TextView tv_start_month;
    private TextView tv_end_placeholder;
    private TextView tv_end_date;
    private TextView tv_end_month;
    private TextView tv_reset;
    private Button btn_save;
    private Button btn_check_save;
    private RelativeLayout rl_done_btn;
    private RelativeLayout rl_done_check_btn;
    private RelativeLayout rl_reset_btn;
    private RelativeLayout rl_iv_back_btn_bg;

    private Snackbar mSnackbar;
    private String SELECT_START_DATE = "";
    private String SELECT_END_DATE = "";
    private int BASE_YEAR = 2018;

    private String FLAG = "all";
    private boolean isSelect = false;
    private boolean isBooking = false;
    private boolean isMonthLabel = false;
    private boolean isSingleSelect = false;
    private ArrayList<String> dates;
    private SelectModel selectDate;

    private int sYear = 0;
    private int sMonth = 0;
    private int sDay = 0;
    private int eYear = 0;
    private int eMonth = 0;
    private int eDay = 0;

    private int maxActivieMonth = -1;
    private int maxYear = -1;

    private int maxBookingDay = -1;
    private int minBookingDay = -1;

    private Boolean mCalendarType = false;

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private View viewDivider;
    private String langType = "en";

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aicalendar_activity_date_picker);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        langType = preferences.getString(SELECTED_LANGUAGE, "en");

        context=setLocale(this,langType);

        Intent getData = getIntent();
        FLAG = getData.getStringExtra(EXTRA_FLAG) != null ? getData.getStringExtra(EXTRA_FLAG):"all";
        isBooking = getData.getBooleanExtra(EXTRA_IS_BOOIKNG , false);
        isSelect = getData.getBooleanExtra(EXTRA_IS_SELECT , false);
        isMonthLabel = getData.getBooleanExtra(EXTRA_IS_MONTH_LABEL , false);
        isSingleSelect = getData.getBooleanExtra(EXTRA_IS_SINGLE_SELECT , false);
        dates = getData.getStringArrayListExtra(EXTRA_BOOKING_DATES);
        maxActivieMonth = getData.getIntExtra(EXTRA_ACTIVE_MONTH_NUM , -1);
        maxYear = getData.getIntExtra(EXTRA_MAX_YEAR , -1);

        sYear = getData.getIntExtra(EXTRA_SELECT_DATE_SY , 0);
        sMonth = getData.getIntExtra(EXTRA_SELECT_DATE_SM , 0);
        sDay = getData.getIntExtra(EXTRA_SELECT_DATE_SD , 0);

        eYear = getData.getIntExtra(EXTRA_SELECT_DATE_EY , 0);
        eMonth = getData.getIntExtra(EXTRA_SELECT_DATE_EM , 0);
        eDay = getData.getIntExtra(EXTRA_SELECT_DATE_ED , 0);

        maxBookingDay = getData.getIntExtra(EXTRA_MAX_BOOKING_DAY , 0);
        minBookingDay = getData.getIntExtra(EXTRA_MIN_BOOKING_DAY , 0);

        mCalendarType = getData.getBooleanExtra(EXTRA_CALENDAR_TYPE, false);

        if(sYear == 0 || sMonth == 0 || sDay == 0
                || eYear == 0 || eMonth == 0 || eDay == 0){
            selectDate = new SelectModel();
            isSelect = false;
        }

        init();

    }

    public static Context setLocale(Context context, String language) {
        //persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }


    private void init(){
        tv_start_placeholder = findViewById(R.id.placeholderStartDate);
        tv_end_placeholder = findViewById(R.id.placeholderEndDate);
        btn_save = findViewById(R.id.btn_save);
        rl_done_btn = findViewById(R.id.rl_done_btn);
        tv_start_date = findViewById(R.id.tv_start_date);
        tv_start_month = findViewById(R.id.tv_start_month);
        tv_end_date = findViewById(R.id.tv_end_date);
        tv_end_month = findViewById(R.id.tv_end_month);
        rl_reset_btn = findViewById(R.id.rl_reset_btn);
        rl_iv_back_btn_bg = findViewById(R.id.rl_iv_back_btn_bg);
        tv_reset = findViewById(R.id.tv_reset);
        viewDivider=findViewById(R.id.viewDivider);
        rl_done_check_btn = findViewById(R.id.rl_done_ckeck_btn);
        tv_minimum_stay = findViewById(R.id.tv_minimum_stay);
        btn_check_save = findViewById(R.id.btn_check_save);

        tv_reset.setText(context.getString(R.string.clear));
        btn_check_save.setText(context.getString(R.string.save));
        btn_save.setText(context.getString(R.string.save));

        if (mCalendarType) {
            btn_save.setVisibility(View.GONE);
            rl_done_check_btn.setVisibility(View.VISIBLE);
            tv_start_placeholder.setText(context.getString(R.string.check_in));
            tv_end_placeholder.setText(context.getString(R.string.check_out));
        } else {
            rl_done_check_btn.setVisibility(View.GONE);
            btn_save.setVisibility(View.VISIBLE);
            tv_start_placeholder.setText(context.getString(R.string.start_date));
            tv_end_placeholder.setText(context.getString(R.string.end_date));
        }

        pickerView = findViewById(R.id.pickerView);
        pickerView.setIsMonthDayLabel(isMonthLabel);
        pickerView.setIsSingleSelect(isSingleSelect);
        pickerView.setMaxActiveMonth(maxActivieMonth);

        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy", Locale.KOREA );
        Date currentTime = new Date ( );
        String dTime = formatter.format ( currentTime );

        if(maxYear != -1 && maxYear > Integer.parseInt(new DateTime().toString("yyyy"))){
            BASE_YEAR = maxYear;
        }else{
            // default : now year + 2 year
            BASE_YEAR = Integer.valueOf(dTime) + 2;
        }

        if(dates != null && dates.size() != 0 && isBooking){
            pickerView.setShowBooking(true);
            pickerView.setBookingDateArray(dates);
        }

        if(isSelect){
            selectDate = new SelectModel();
            selectDate.setSelectd(true);
            selectDate.setFristYear(sYear);
            selectDate.setFristMonth(sMonth);
            selectDate.setFristDay(sDay);
            selectDate.setLastYear(eYear);
            selectDate.setLastMonth(eMonth);
            selectDate.setLastDay(eDay);
            pickerView.setSelected(selectDate);

            tv_start_placeholder.setVisibility(View.GONE);
            tv_start_date.setVisibility(View.VISIBLE);
            tv_start_month.setVisibility(View.VISIBLE);

            Date FirstDate = null;
            Date lastDate = null;

            Calendar cl = Calendar.getInstance();
            try {
                cl.set(sYear, sMonth - 1, sDay);
                setStartDateText(cl);
                 FirstDate = cl.getTime();

                cl.set(eYear, eMonth - 1, eDay);

                 lastDate = cl.getTime();

                setEndDateText(cl);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mCalendarType) {
                long diffInMillies = 0;
                if (FirstDate != null && lastDate != null) {
                    diffInMillies = Math.abs(FirstDate.getTime() - lastDate.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    btn_check_save.setEnabled(true);
                    btn_check_save.setAlpha(1f);
                    tv_minimum_stay.setText(context.getResources().getQuantityString(R.plurals.night_selected, (int)diff, (int)diff));
                }
            }
        } else  {
            if (minBookingDay != 0) {
                tv_minimum_stay.setText(context.getResources().getQuantityString(R.plurals.night_minimum, minBookingDay, minBookingDay));
               // tv_minimum_stay.setText("Minimum required " + minBookingDay + " nights stay.");
            }
        }

        pickerView.setController(this,context);

        if (isSelect) {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH)+1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            LocalDate startDate = new LocalDate(year, month, day);
            LocalDate endDate = new LocalDate(sYear, sMonth, sDay);

            Log.d("fdd", String.valueOf(monthsBetweenIgnoreDays(startDate, endDate)));

            try {
                Objects.requireNonNull(pickerView.getLayoutManager()).scrollToPosition(monthsBetweenIgnoreDays(startDate, endDate));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        boolean isLeftToRight =context.getResources().getBoolean(R.bool.air_calendar_is_left_to_right_layout);

        if(!isLeftToRight){
            viewDivider.setRotation(135);
        }else{
            viewDivider.setRotation(45);
        }

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postResult();
            }
        });

        btn_check_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postResult();
            }
        });

        rl_reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SELECT_START_DATE = "";
                SELECT_END_DATE = "";
                isSelect = false;
                setContentView(R.layout.aicalendar_activity_date_picker);
                init();
            }
        });

        rl_iv_back_btn_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void postResult() {
        if((SELECT_START_DATE == null || SELECT_START_DATE.equals("")) && (SELECT_END_DATE == null || SELECT_END_DATE.equals(""))){
            SELECT_START_DATE = "";
            SELECT_END_DATE = "";
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_SELECT_START_DATE , SELECT_START_DATE );
        resultIntent.putExtra(RESULT_SELECT_END_DATE , SELECT_END_DATE );
        resultIntent.putExtra(RESULT_SELECT_START_VIEW_DATE , tv_start_date.getText().toString() );
        resultIntent.putExtra(RESULT_SELECT_END_VIEW_DATE , tv_end_date.getText().toString() );
        resultIntent.putExtra(RESULT_FLAG , FLAG );
        resultIntent.putExtra(RESULT_TYPE , FLAG );
        resultIntent.putExtra(RESULT_STATE , "done" );
        setResult(RESULT_OK , resultIntent);
        finish();
    }

    private static int monthsBetweenIgnoreDays(LocalDate start, LocalDate end) {
        start = start.withDayOfMonth(1);
        end = end.withDayOfMonth(1);
        return Months.monthsBetween(start, end).getMonths();
    }

    @Override
    public int getMaxYear() {
        return BASE_YEAR;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        try{

            if (mSnackbar != null && mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
            String start_month_str =  String.format(Locale.US,"%02d" , (month+1));
            String start_day_str =  String.format(Locale.US,"%01d" , day);
            String startSetDate = year+start_month_str+start_day_str;

            String startDateDay = AirCalendarUtils.getDateDay(startSetDate , "yyyyMMdd");
            tv_start_placeholder.setVisibility(View.GONE);
            tv_start_date.setVisibility(View.VISIBLE);
            tv_start_month.setVisibility(View.VISIBLE);

            tv_start_date.setText(startDateDay);
            tv_start_date.setTextColor(0xff4a4a4a);

            Calendar cal=Calendar.getInstance();
            SimpleDateFormat month_date = new SimpleDateFormat("MMM");
            cal.set(Calendar.MONTH,month);
            String month_name = month_date.format(cal.getTime());

            tv_start_month.setText(month_name+" "+start_day_str);
            tv_start_month.setTextColor(0xff4a4a4a);

            tv_end_placeholder.setVisibility(View.VISIBLE);
            tv_end_date.setVisibility(View.GONE);
            tv_end_month.setVisibility(View.GONE);
            tv_end_placeholder.setText(context.getString(R.string.end_date));

            Log.d("alpha", String.valueOf(btn_save.getAlpha()));
            if (mCalendarType) {
                Log.d("calendarType", mCalendarType.toString() + maxBookingDay);
                if (maxBookingDay != 0) {
                    Log.d("calendarType123", mCalendarType.toString());
                    //tv_minimum_stay.setText("Maximum required " + maxBookingDay + " nights stay.");
                    tv_minimum_stay.setText(context.getResources().getQuantityString(R.plurals.night_maximum, maxBookingDay, maxBookingDay));
                }
                /*if (minBookingDay != 0) {
                    tv_minimum_stay.setText("Minimum required " + minBookingDay + " nights stay.");
                }*/
                btn_check_save.setEnabled(false);
                btn_check_save.setAlpha(0.1f);
            } else {
                btn_save.setEnabled(false);
                btn_save.setAlpha(0.1f);
            }
            SELECT_END_DATE = "";
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDateRangeSelected(AirMonthAdapter.SelectedDays<AirMonthAdapter.CalendarDay> selectedDays) {

        try{
            if (mSnackbar != null && mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
            Boolean blockedSelected = false;
            String pattern = "MM-dd-yyyy";
            String patternAlt = "MM/dd/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
            SimpleDateFormat sdf = new SimpleDateFormat(patternAlt,Locale.ENGLISH);
            String startDate = simpleDateFormat.format(selectedDays.getFirst().getDate());
            String endDate = simpleDateFormat.format(selectedDays.getLast().getDate());

            String altStart = sdf.format(selectedDays.getFirst().getDate());
            String altEnd = sdf.format(selectedDays.getLast().getDate());

            Log.d("startDate", startDate);
            Log.d("endDate", endDate);

            ArrayList<String> allDates = getDates(altStart,altEnd);

            Calendar mCalendar = Calendar.getInstance();
            if (startDate.equals(endDate)) {
                mCalendar.setTimeInMillis(selectedDays.getFirst().getDate().getTime());
                setStartDateText(mCalendar);
                if (mCalendarType) {
                    btn_check_save.setEnabled(false);
                    btn_check_save.setAlpha(0.1f);
                } else {
                    btn_save.setEnabled(false);
                    btn_save.setAlpha(0.1f);
                }
            } else {
                mCalendar.setTimeInMillis(selectedDays.getFirst().getDate().getTime());
                setStartDateText(mCalendar);
                mCalendar.setTimeInMillis(selectedDays.getLast().getDate().getTime());
                setEndDateText(mCalendar);

                if (mCalendarType) {
                    long diffInMillies = Math.abs(selectedDays.getLast().getDate().getTime() - selectedDays.getFirst().getDate().getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    Log.d("daysDiff", String.valueOf(diff));
                    if (minBookingDay != 0 && minBookingDay > diff) {
                        //tv_minimum_stay.setText("Minimum stay is " + minBookingDay + " nights.");
                        tv_minimum_stay.setText(context.getResources().getQuantityString(R.plurals.night_minimum, minBookingDay, minBookingDay));

                        mSnackbar = showSnackBar(context.getResources().getQuantityString(R.plurals.night_minimum, minBookingDay, minBookingDay), getApplicationContext(), findViewById(R.id.root_layout_calendar));
                        btn_check_save.setEnabled(false);
                        btn_check_save.setAlpha(0.1f);
                        return;
                    }
                    if (maxBookingDay != 0 && maxBookingDay < diff) {
                      //  tv_minimum_stay.setText("Maximum stay is " + maxBookingDay + " nights.");
                        tv_minimum_stay.setText(context.getResources().getQuantityString(R.plurals.night_maximum, maxBookingDay, maxBookingDay));
                        mSnackbar = showSnackBar(context.getResources().getQuantityString(R.plurals.night_maximum, maxBookingDay, maxBookingDay), getApplicationContext(), findViewById(R.id.root_layout_calendar));
                        btn_check_save.setEnabled(false);
                        btn_check_save.setAlpha(0.1f);
                        return;
                    }
                    if(dates !=null && dates.size()>0){
                        for(int i =0;i<dates.size();i++){
                            for(int j=0;j<allDates.size();j++){
                                if(allDates.get(j).equals(dates.get(i))){
                                    blockedSelected = true;
                                }
                            }
                        }
                    }
                    if(blockedSelected){
                        tv_minimum_stay.setText(context.getString(R.string.dates_not_available));
                        mSnackbar = showSnackBar(context.getString(R.string.dates_not_available)    , getApplicationContext(), findViewById(R.id.root_layout_calendar));
                        btn_check_save.setEnabled(false);
                        btn_check_save.setAlpha(0.1f);
                        return;
                    }
                    btn_check_save.setAlpha(1f);
                    btn_check_save.setEnabled(true);
                    tv_minimum_stay.setText(context.getResources().getQuantityString(R.plurals.night_selected, (int)diff, (int)diff));
                } else {
                    btn_save.setEnabled(true);
                    btn_save.setAlpha(1f);
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public ArrayList<String> getDates(String dateString1, String dateString2)
    {
        ArrayList<String> allDates = new ArrayList<>();
        final long ONE_DAY = 24 * 60 * 60 * 1000L;
        long  from=Date.parse(dateString1);

        long to=Date.parse(dateString2);

        int x=0;
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        while(from <= to) {
            x=x+1;
            Date val=new Date(from);
            from += ONE_DAY;
            allDates.add(simpleDateFormat.format(val));
        }
        return allDates;
    }

    private void setStartDateText(Calendar cl) throws Exception {
        int start_month_int = (cl.get(Calendar.MONTH)+1);
        String start_month_str =  String.format(Locale.US,"%02d" , start_month_int);

        int start_day_int = cl.get(Calendar.DAY_OF_MONTH);
        String start_day_str =  String.format(Locale.US,"%01d" , start_day_int);

        String startSetDate = cl.get(Calendar.YEAR)+start_month_str+start_day_str;
        String startDateDay = AirCalendarUtils.getDateDay(startSetDate , "yyyyMMdd");
        String startDate = cl.get(Calendar.YEAR) + "-" + start_month_str + "-" + start_day_str;

        tv_start_date.setText(startDateDay);
        tv_start_date.setTextColor(0xff4a4a4a);

        Calendar cal=Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        cal.set(Calendar.MONTH, start_month_int - 1);
        String month_name = month_date.format(cl.getTime());

        tv_start_month.setText(month_name+" "+start_day_int);
        tv_start_month.setTextColor(0xff4a4a4a);

        SELECT_START_DATE = startDate;
    }

    private void setEndDateText(Calendar cl) throws Exception {
        int end_month_int = (cl.get(Calendar.MONTH)+1);
        String end_month_str = String.format(Locale.US,"%02d" , end_month_int);

        int end_day_int = cl.get(Calendar.DAY_OF_MONTH);
        String end_day_str = String.format(Locale.US,"%01d" , end_day_int);

        String endSetDate = cl.get(Calendar.YEAR)+end_month_str+end_day_str;
        String endDateDay = AirCalendarUtils.getDateDay(endSetDate , "yyyyMMdd");
        String endDate = cl.get(Calendar.YEAR) + "-" + end_month_str + "-" + end_day_str;

        tv_end_date.setText(endDateDay);
        tv_end_date.setTextColor(0xff4a4a4a);

        Calendar caal=Calendar.getInstance();
        SimpleDateFormat month_daate = new SimpleDateFormat("MMM");
        caal.set(Calendar.MONTH, end_month_int - 1);
        String month_naame = month_daate.format(cl.getTime());

        tv_end_month.setText(month_naame+" "+end_day_int);
        tv_end_month.setTextColor(0xff4a4a4a);

        tv_end_placeholder.setVisibility(View.GONE);
        tv_end_date.setVisibility(View.VISIBLE);
        tv_end_month.setVisibility(View.VISIBLE);

        SELECT_END_DATE = endDate;
    }
}
