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
package com.yongbeom.aircalendar.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.yongbeom.aircalendar.R;
import com.yongbeom.aircalendar.core.util.AirCalendarUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AirMonthView extends View {
    private String TAG = "AirMonthView";

    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
    public static final String VIEW_PARAMS_SELECTED_LAST_DAY = "selected_last_day";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
    public static final String VIEW_PARAMS_SELECTED_LAST_MONTH = "selected_last_month";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
    public static final String VIEW_PARAMS_SELECTED_LAST_YEAR = "selected_last_year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    private static final int SELECTED_CIRCLE_ALPHA = 128;
    protected static int DEFAULT_HEIGHT = 32;
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static int DAY_SELECTED_CIRCLE_SIZE;
    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int MIN_HEIGHT = 10;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_SIZE;
    protected static int MONTH_LABEL_TEXT_SIZE;

    protected int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    protected Paint mMonthDayLabelPaint;
    protected Paint mMonthNumPaint;
    protected Paint mMonthTitleBGPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mSelectedIntervalPaint;
    protected Paint mSelectedCirclePaint;
    protected Paint mWeekDayLinePaint;
    protected int mCurrentDayTextColor;
    protected int mMonthTextColor;
    protected int mDayTextColor;
    protected int mDayNumColor;
    protected int mMonthTitleBGColor;
    protected int mPreviousDayColor;
    protected int mSelectedDaysBgColor;
    protected int mSelectedDaysColor;
    protected int mWeekDayLineColor;
    protected int mWeekEndColor;

    private final StringBuilder mStringBuilder;

    protected boolean mHasToday = false;
    protected boolean mIsPrev = false;
    protected int mSelectedBeginDay = -1;
    protected int mSelectedLastDay = -1;
    protected int mSelectedBeginMonth = -1;
    protected int mSelectedLastMonth = -1;
    protected int mSelectedBeginYear = -1;
    protected int mSelectedLastYear = -1;
    protected int mToday = -1;
    protected int mWeekStart = 1;
    protected int mNumDays = 7;
    protected int mNumCells = mNumDays;
    private int mDayOfWeekStart = 0;
    protected int mMonth;
    protected int mMonthPlus3;
    protected Boolean mDrawRect;
    protected int mRowHeight = DEFAULT_HEIGHT;
    protected int mWidth;
    protected int mYear;
    protected int mMaxActiveMonth = -1;
    final Time today;

    private final Calendar mCalendar;
    private final Calendar mDayLabelCalendar;
    private final Boolean isPrevDayEnabled;
    private int mNumRows = DEFAULT_NUM_ROWS;
    private String[] mWeekLabels;
    private OnDayClickListener mOnDayClickListener;
    private boolean isShowBooking = false;
    private boolean isMonthDayLabels = false;
    private Context mContext;
    SharedPreferences preferences;
    private String langType = "en";
    private ArrayList<String> bookingDateArray;

    private Typeface mFont;

    public AirMonthView(Context context, TypedArray typedArray , boolean showBooking , boolean monthDayLabels , ArrayList<String> bookingdates , int maxActiveMonth) {
        super(context);

        isMonthDayLabels = monthDayLabels;
        isShowBooking = showBooking;
        mContext = context;
        bookingDateArray = bookingdates;
        mMaxActiveMonth = maxActiveMonth;

        preferences= PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        langType= preferences.getString("Locale.Helper.Selected.Language", "en");

        Resources resources = context.getResources();
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        mCurrentDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorCurrentDay, resources.getColor(R.color.normal_day));
        mMonthTextColor = typedArray.getColor(R.styleable.DayPickerView_colorMonthName, resources.getColor(R.color.colorMonthTextColor));
        mDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorDayName, resources.getColor(R.color.normal_day));
        mDayNumColor = typedArray.getColor(R.styleable.DayPickerView_colorNormalDay, resources.getColor(R.color.normal_day));
        mPreviousDayColor = typedArray.getColor(R.styleable.DayPickerView_colorPreviousDay, resources.getColor(R.color.normal_day));
        mSelectedDaysBgColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayBackground, resources.getColor(R.color.selected_day_background));
        mSelectedDaysColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayBackground, resources.getColor(R.color.selected_day_interval_text));
        mMonthTitleBGColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayText, resources.getColor(R.color.selected_day_text));
        mWeekDayLineColor = typedArray.getColor(R.styleable.DayPickerView_colorWeekDayLineColor, resources.getColor(R.color.colorWeekDayLineColor));
        mWeekEndColor = typedArray.getColor(R.styleable.DayPickerView_colorWeekEndColor, resources.getColor(R.color.colorWeekEndColor));

        mMonthPlus3 = (today.month)+3;

        mDrawRect = typedArray.getBoolean(R.styleable.DayPickerView_drawRoundRect, false);

        mStringBuilder = new StringBuilder(50);

        MINI_DAY_NUMBER_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDay, resources.getDimensionPixelSize(R.dimen.text_size_day));
        MONTH_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeMonth, resources.getDimensionPixelSize(R.dimen.text_size_month));

        MONTH_DAY_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDayName, resources.getDimensionPixelSize(R.dimen.text_size_day_name));

        MONTH_HEADER_SIZE = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_headerMonthHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        DAY_SELECTED_CIRCLE_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));

        mRowHeight = ((typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarHeight, resources.getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_SIZE) / 6);

        isPrevDayEnabled = typedArray.getBoolean(R.styleable.DayPickerView_enablePreviousDay, true);

        mWeekLabels = getResources().getStringArray(R.array.label_calender_week);

        mFont = ResourcesCompat.getFont(getContext(), R.font.linetocircular);

        initView();

    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }


    /**
     * ?????? ???~?????? ?????????
     * @param canvas
     */
    private void drawMonthDayLabels(Canvas canvas) {
        int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mWeekLabels[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.US), x, y, mMonthDayLabelPaint);
        }
        canvas.drawLine(0, y + (MONTH_DAY_LABEL_TEXT_SIZE / 2), mWidth, y + (MONTH_DAY_LABEL_TEXT_SIZE / 2), mWeekDayLinePaint);
    }

    /**
     * yyyy??? MM?????? ?????????
     * @param canvas
     */
    private void drawMonthTitle(Canvas canvas) {
//        int x = (mWidth + 2 * mPadding) / 2;

        float x=mWidth;

        boolean isLeftToRight =mContext.getResources().getBoolean(R.bool.air_calendar_is_left_to_right_layout);

        if(!isLeftToRight){
            x = mWidth/Float.parseFloat("1.1");
            mMonthTitlePaint.setTextAlign(Align.RIGHT);
        }else{
            x = mWidth/Float.parseFloat("13");
            mMonthTitlePaint.setTextAlign(Align.LEFT);
        }

        int y = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        Log.d(this.getClass().getSimpleName(),"widthOfTheMonth"+mWidth);
        canvas.drawText(stringBuilder.toString(), x, y, mMonthTitlePaint);
    }

    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);

        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);    // ???????????? ???????????? ??????
    }

    private String tempStartDate = null;
    private String tempEndDate = null;

    private void onDayClick(AirMonthAdapter.CalendarDay calendarDay) {

        boolean isClick = false;
        if(mToday > calendarDay.day){
            isClick = false;
        }else{
            if(isShowBooking){
                if(bookingDateArray != null && bookingDateArray.size() != 0){
                    for(int i=0; i< bookingDateArray.size(); i++){
                        String month_str = (mMonth + 1)+"";
                        if((mMonth + 1) < 10){
                            month_str = "0"+month_str;
                        }
                        String day_str = calendarDay.day+"";
                        if(calendarDay.day < 10){
                            day_str = "0"+day_str;
                        }

                        String BOOKING_DATE = mYear+"-"+month_str+"-"+day_str;
                        if(bookingDateArray.get(i).equals(BOOKING_DATE)){
                            isClick = false;
                            break;
                        }else{
                            isClick = true;
                        }
                    }
                }else{
                    isClick = true;
                }

//                if((calendarDay.month) > mMonthPlus3){
//                    isClick = false;
//                }
            }else{
                isClick = true;
            }
        }

        if(mMaxActiveMonth != -1 && mMaxActiveMonth > 0){
            DateTime getViewDate = new DateTime();
            DateTime getTouchDate = new DateTime();
            DateTime lastMonth = new DateTime();
            try{
                DateFormat readDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                Log.d("touchDate123", "readDate123 : " + readDate.toString());
                Date date = readDate.parse(calendarDay.year +"-"+(calendarDay.month+1)+"-"+(calendarDay.day+1)+" 00:00:00");
//                Log.d("touchDate123", "date : " + date.toString());
                Date touchDate = readDate.parse(calendarDay.year +"-"+(calendarDay.month+1)+"-"+calendarDay.day+" 00:00:00");
//                Log.d("touchDate123", "touchDate123 : " + touchDate.toString());
                getViewDate = new DateTime(date);
//                Log.d("touchDate123", "getViewDate : " +touchDate.toString());
                getTouchDate = new DateTime(touchDate);
//                Log.d("touchDate123", "getTouchDate : " +touchDate.toString());

                LocalDate maxiumDayInMonth = new DateTime().plusMonths(mMaxActiveMonth).toLocalDate().dayOfMonth().withMaximumValue();
//                Log.d("touchDate123", "maxiumDayInMonth123 : " + maxiumDayInMonth.toString());
                Date date2 = readDate.parse(maxiumDayInMonth.toString("yyyy-MM-dd")+" 00:00:00");
//                Log.d("touchDate123", "date2 : " + date2.toString());
                lastMonth = new DateTime(date2);
//                Log.d("touchDate123", "lastMonth : " +touchDate.toString());
                lastMonth = lastMonth.plusDays(1);
//                Log.d("touchDate123", "lastMonthPlua : " +touchDate.toString());

            }catch (Exception e){
                e.printStackTrace();
            }


            // ?????? ????????? 3?????? ?????? ??????
            DateTime setThreeMonth = new DateTime();
//            Log.d("touchDate123", "setThreeMonth : " +setThreeMonth.toString());

            setThreeMonth = setThreeMonth.plusMonths(mMaxActiveMonth);
//            Log.d("touchDate123", "setThreeMonthPlus : " +setThreeMonth.toString());

            int compare = setThreeMonth.compareTo(getViewDate);
            if(compare == 0 || compare == -1){
                isClick = false;
                // Log.d("qwer", "getTouchDate : " +getTouchDate.toString("yyyy-MM-dd"));
                // Log.d("qwer", "lastMonth : " +lastMonth.dayOfMonth().withMinimumValue().toString("yyyy-MM-dd"));
               /* if(getTouchDate.toString("yyyy-MM-dd").equals(lastMonth.dayOfMonth().withMinimumValue().toString("yyyy-MM-dd"))){
                    isClick = true;
                }*/
            }
        }


        if(((calendarDay.month)+1) == 2){
            if(calendarDay.day == 29){
                isClick = false;
            }
        }

        if(isClick){
            if (mOnDayClickListener != null && (isPrevDayEnabled || !((calendarDay.month == today.month) && (calendarDay.year == today.year) && calendarDay.day < today.monthDay))) {
                mOnDayClickListener.onDayClick(this, calendarDay);
            }
        }

    }

    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    private boolean prevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) || (mMonth == time.month && monthDay < time.monthDay);
    }

    /**
     * @param canvas
     */
    protected void drawMonthNums(Canvas canvas) {
        int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_SIZE;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;
        int bgw = mWidth / mNumDays;

        while (day <= mNumCells) {

            int x = paddingDay * (1 + dayOffset * 2) + mPadding;

            if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear) || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                if (mDrawRect) {
                    RectF rectF = new RectF(x - DAY_SELECTED_CIRCLE_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + DAY_SELECTED_CIRCLE_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                    canvas.drawRoundRect(rectF, 150.0f, 150.0f, mSelectedCirclePaint);
                } else {
                    canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                }
            }

            if (mHasToday && (mToday == day)) {
                mMonthNumPaint.setColor(mCurrentDayTextColor);
//                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mMonthNumPaint.setTypeface(mFont);

            } if(mToday > day){
                mMonthNumPaint.setColor(getResources().getColor(R.color.color_old_day_text_color));
//                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mMonthNumPaint.setTypeface(mFont);
            } else {
                mMonthNumPaint.setColor(mDayNumColor);
//                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                mMonthNumPaint.setTypeface(mFont);
            }

            if (AirCalendarUtils.isWeekend(mYear + "-" + (mMonth + 1) + "-" + day)) {

                if(mToday > day){
                    mMonthNumPaint.setColor(getResources().getColor(R.color.color_old_day_text_color));
                } else {
                    mMonthNumPaint.setColor(mWeekEndColor);
                }
            }


            if(mMaxActiveMonth != -1 && mMaxActiveMonth > 0){
                DateTime getViewDate = new DateTime();
                try{
                    DateFormat readDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = readDate.parse(mYear +"-"+mMonth+"-"+(day+1)+" 00:00:00");
                    getViewDate = new DateTime(date);
//                    Log.d("touchD123", "readDate : " +readDate.toString());
//                    Log.d("touchD123", "date : " +date.toString());
//                    Log.d("touchD123", "getViewDate : " +getViewDate.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }

                // ?????? ????????? 3?????? ?????? ??????
                DateTime setThreeMonth = new DateTime();
//                Log.d("touchD123", "setThreeMonth : " +setThreeMonth.toString());
                setThreeMonth = setThreeMonth.plusMonths(mMaxActiveMonth);
//                Log.d("touchD123", "setThreeMonth : " +setThreeMonth.toString());

                DateTime ii = setThreeMonth.minusMonths(1);
//                Log.d("touchD123", "ii : " + ii);
                int compare = ii.compareTo(getViewDate);
//                Log.d("touchD123", "compare : " + compare);
                if(ii.getMillis() < getViewDate.getMillis() ) {//(compare == 0 || compare == -1){
                    mMonthNumPaint.setColor(getResources().getColor(R.color.color_old_day_text_color));
                    mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                }
            }

            if(isShowBooking){

//                if(mMonth < (mMonth+3)){
//                    mMonthNumPaint.setColor(getResources().getColor(R.color.color_old_day_text_color));
//                    mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//                }else{
//                    mMonthNumPaint.setColor(mDayNumColor);
//                    mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//                }

                if(bookingDateArray != null && bookingDateArray.size() != 0){
                    for(int i=0; i< bookingDateArray.size(); i++){
                        String month_str = (mMonth + 1)+"";
                        if((mMonth + 1) < 10){
                            month_str = "0"+month_str;
                        }
                        String day_str = day+"";
                        if(day < 10){
                            day_str = "0"+day_str;
                        }

                        String BOOKING_DATE = mYear+"-"+month_str+"-"+day_str;
                        if(bookingDateArray.get(i).equals(BOOKING_DATE)){
                            mMonthNumPaint.setColor(getResources().getColor(R.color.normal_day));
                            mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            mMonthNumPaint.setStyle(Paint.Style.FILL);
                            mMonthNumPaint.setStrokeWidth(5);
                            canvas.drawLine(x-30,((y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE)+30,x + (bgw / 2)-30,((y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE)-30,mMonthNumPaint);
                        }
                    }
                }
            }

            if ((mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear) || (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1) && (mSelectedLastMonth != mSelectedBeginMonth || mSelectedBeginDay != mSelectedLastDay) && (mMonth == mSelectedBeginMonth && mSelectedBeginDay == day && mSelectedBeginYear == mYear)) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
                RectF rectF = new RectF(x, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + (bgw / 2), (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                canvas.drawRoundRect(rectF, 0.0f, 0.0f, mSelectedIntervalPaint);
            }
            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1) && (mSelectedLastMonth != mSelectedBeginMonth || mSelectedBeginDay != mSelectedLastDay) && (mMonth == mSelectedLastMonth && mSelectedLastDay == day && mSelectedLastYear == mYear)) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
                RectF rectF = new RectF(x - (bgw / 2), (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
                canvas.drawRoundRect(rectF, 0.0f, 0.0f, mSelectedIntervalPaint);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear &&
                    mSelectedBeginMonth == mSelectedLastMonth &&
                    mSelectedBeginDay == mSelectedLastDay &&
                    day == mSelectedBeginDay &&
                    mMonth == mSelectedBeginMonth &&
                    mYear == mSelectedBeginYear)) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mSelectedBeginYear == mYear) &&
                    (((mMonth == mSelectedBeginMonth && mSelectedLastMonth == mSelectedBeginMonth) && ((mSelectedBeginDay < mSelectedLastDay && day > mSelectedBeginDay && day < mSelectedLastDay) || (mSelectedBeginDay > mSelectedLastDay && day < mSelectedBeginDay && day > mSelectedLastDay))) ||
                            ((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)))) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
                drawSelectorIntervalDayBackground(canvas, x, y, bgw);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear && ((mSelectedBeginYear == mYear && mMonth == mSelectedBeginMonth) || (mSelectedLastYear == mYear && mMonth == mSelectedLastMonth)) &&
                    (((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay))))) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
                drawSelectorIntervalDayBackground(canvas, x, y, bgw);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mYear == mSelectedBeginYear) &&
                    ((mMonth > mSelectedBeginMonth && mMonth < mSelectedLastMonth && mSelectedBeginMonth < mSelectedLastMonth) ||
                            (mMonth < mSelectedBeginMonth && mMonth > mSelectedLastMonth && mSelectedBeginMonth > mSelectedLastMonth))) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
                drawSelectorIntervalDayBackground(canvas, x, y, bgw);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear) &&
                    ((mSelectedBeginYear < mSelectedLastYear && ((mMonth > mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth < mSelectedLastMonth && mYear == mSelectedLastYear))) ||
                            (mSelectedBeginYear > mSelectedLastYear && ((mMonth < mSelectedBeginMonth && mYear == mSelectedBeginYear) || (mMonth > mSelectedLastMonth && mYear == mSelectedLastYear))))) {
                mMonthNumPaint.setColor(mSelectedDaysColor);
                drawSelectorIntervalDayBackground(canvas, x, y, bgw);
            }

            if (!isPrevDayEnabled && prevDay(day, today) && today.month == mMonth && today.year == mYear) {
                mMonthNumPaint.setColor(mPreviousDayColor);
                mMonthNumPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                if (AirCalendarUtils.isWeekend(mYear + "-" + (mMonth + 1) + "-" + day)) {
                    mMonthNumPaint.setColor(mWeekEndColor);
                }
            }
            if((mMonth + 1) == 2){
                if(day == 29){
                    Log.d(TAG , "## Duplicate!");
                }else{
                    canvas.drawText(String.format(Locale.US,"%d", day), x, y, mMonthNumPaint);
                }
            }else{
                canvas.drawText(String.format(Locale.US,"%d", day), x, y, mMonthNumPaint);

            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }

    /**
     * @param canvas
     * @param x
     * @param y
     */
    private void drawSelectorIntervalDayBackground(Canvas canvas, int x, int y, int paddingDay) {

        RectF rectF = new RectF(x - (paddingDay / 2), (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_CIRCLE_SIZE, x + (paddingDay / 2), (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_CIRCLE_SIZE);
        canvas.drawRoundRect(rectF, 0.0f, 0.0f, mSelectedIntervalPaint);

    }

    public AirMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {

        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        return new AirMonthAdapter.CalendarDay(mYear, mMonth, day);
    }

    protected void initView() {
        Typeface font = ResourcesCompat.getFont(getContext(), R.font.linetocircular);

        // ?????? ???????????? ??????
        mMonthTitlePaint = new Paint();
//        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(font);
//        mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setStyle(Style.FILL);

        mMonthTitleBGPaint = new Paint();
        mMonthTitleBGPaint.setFakeBoldText(true);
        mMonthTitleBGPaint.setAntiAlias(true);
        mMonthTitleBGPaint.setColor(mMonthTitleBGColor);
        mMonthTitleBGPaint.setTextAlign(Align.CENTER);
        mMonthTitleBGPaint.setStyle(Style.FILL);
        mMonthTitleBGPaint.setTypeface(font);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mSelectedDaysBgColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);
        mSelectedCirclePaint.setTypeface(font);

        mSelectedIntervalPaint = new Paint();
        mSelectedIntervalPaint.setAntiAlias(true);
        mSelectedIntervalPaint.setColor(mSelectedDaysBgColor);
        mSelectedIntervalPaint.setTypeface(font);

//        mSelectedIntervalPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(getResources().getColor(R.color.colorMonthDayLabelPaint));
        mMonthDayLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mMonthDayLabelPaint.setStyle(Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);
        mMonthDayLabelPaint.setTypeface(font);

        mWeekDayLinePaint = new Paint();
        mWeekDayLinePaint.setAntiAlias(true);
        mWeekDayLinePaint.setStrokeWidth((float) 2.0);
        mWeekDayLinePaint.setColor(mWeekDayLineColor);
        mWeekDayLinePaint.setTypeface(font);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTextAlign(Align.CENTER);
        mMonthNumPaint.setTypeface(font);
        mMonthNumPaint.setFakeBoldText(false);
    }

    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);

        if(isMonthDayLabels){
            drawMonthDayLabels(canvas);
        }
        drawMonthNums(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            AirMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay != null) {
                onDayClick(calendarDay);
            }
        }
        return true;
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DAY)) {
            mSelectedBeginDay = params.get(VIEW_PARAMS_SELECTED_BEGIN_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DAY)) {
            mSelectedLastDay = params.get(VIEW_PARAMS_SELECTED_LAST_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_MONTH)) {
            mSelectedBeginMonth = params.get(VIEW_PARAMS_SELECTED_BEGIN_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_MONTH)) {
            mSelectedLastMonth = params.get(VIEW_PARAMS_SELECTED_LAST_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_YEAR)) {
            mSelectedBeginYear = params.get(VIEW_PARAMS_SELECTED_BEGIN_YEAR);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_YEAR)) {
            mSelectedLastYear = params.get(VIEW_PARAMS_SELECTED_LAST_YEAR);
        }

        mMonth = params.get(VIEW_PARAMS_MONTH);
        mYear = params.get(VIEW_PARAMS_YEAR);

        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            int weekStart = params.get(VIEW_PARAMS_WEEK_START);
            try{
                weekStart = mCalendar.getFirstDayOfWeek();
            }catch (RuntimeException e){}
            mWeekStart = weekStart;
        }

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }

            mIsPrev = prevDay(day, today);
        }

        mNumRows = calculateNumRows();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public static abstract interface OnDayClickListener {
        public abstract void onDayClick(AirMonthView airMonthView, AirMonthAdapter.CalendarDay calendarDay);
    }






}