package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import com.android.internal.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

/* access modifiers changed from: package-private */
public class DatePickerSpinnerDelegate extends DatePicker.AbstractDatePickerDelegate {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final boolean DEFAULT_SPINNERS_SHOWN = true;
    private static final int DEFAULT_START_YEAR = 1900;
    private final CalendarView mCalendarView;
    private final DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
    private final NumberPicker mDaySpinner;
    private final EditText mDaySpinnerInput;
    private boolean mIsEnabled = true;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private final NumberPicker mMonthSpinner;
    private final EditText mMonthSpinnerInput;
    private int mNumberOfMonths;
    private String[] mShortMonths;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;
    private final NumberPicker mYearSpinner;
    private final EditText mYearSpinnerInput;

    DatePickerSpinnerDelegate(DatePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        this.mDelegator = delegator;
        this.mContext = context;
        setCurrentLocale(Locale.getDefault());
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        boolean spinnersShown = attributesArray.getBoolean(6, true);
        boolean calendarViewShown = attributesArray.getBoolean(7, true);
        int startYear = attributesArray.getInt(1, 1900);
        int endYear = attributesArray.getInt(2, 2100);
        String minDate = attributesArray.getString(4);
        String maxDate = attributesArray.getString(5);
        int layoutResourceId = attributesArray.getResourceId(20, R.layout.date_picker_legacy);
        attributesArray.recycle();
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResourceId, (ViewGroup) this.mDelegator, true).setSaveFromParentEnabled(false);
        NumberPicker.OnValueChangeListener onChangeListener = new NumberPicker.OnValueChangeListener() {
            /* class android.widget.DatePickerSpinnerDelegate.AnonymousClass1 */

            @Override // android.widget.NumberPicker.OnValueChangeListener
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                DatePickerSpinnerDelegate.this.updateInputState();
                DatePickerSpinnerDelegate.this.mTempDate.setTimeInMillis(DatePickerSpinnerDelegate.this.mCurrentDate.getTimeInMillis());
                if (picker == DatePickerSpinnerDelegate.this.mDaySpinner) {
                    int maxDayOfMonth = DatePickerSpinnerDelegate.this.mTempDate.getActualMaximum(5);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        DatePickerSpinnerDelegate.this.mTempDate.add(5, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        DatePickerSpinnerDelegate.this.mTempDate.add(5, -1);
                    } else {
                        DatePickerSpinnerDelegate.this.mTempDate.add(5, newVal - oldVal);
                    }
                } else if (picker == DatePickerSpinnerDelegate.this.mMonthSpinner) {
                    if (oldVal == 11 && newVal == 0) {
                        DatePickerSpinnerDelegate.this.mTempDate.add(2, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        DatePickerSpinnerDelegate.this.mTempDate.add(2, -1);
                    } else {
                        DatePickerSpinnerDelegate.this.mTempDate.add(2, newVal - oldVal);
                    }
                } else if (picker == DatePickerSpinnerDelegate.this.mYearSpinner) {
                    DatePickerSpinnerDelegate.this.mTempDate.set(1, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                DatePickerSpinnerDelegate datePickerSpinnerDelegate = DatePickerSpinnerDelegate.this;
                datePickerSpinnerDelegate.setDate(datePickerSpinnerDelegate.mTempDate.get(1), DatePickerSpinnerDelegate.this.mTempDate.get(2), DatePickerSpinnerDelegate.this.mTempDate.get(5));
                DatePickerSpinnerDelegate.this.updateSpinners();
                DatePickerSpinnerDelegate.this.updateCalendarView();
                DatePickerSpinnerDelegate.this.notifyDateChanged();
            }
        };
        this.mSpinners = (LinearLayout) this.mDelegator.findViewById(R.id.pickers);
        this.mCalendarView = (CalendarView) this.mDelegator.findViewById(R.id.calendar_view);
        this.mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            /* class android.widget.DatePickerSpinnerDelegate.AnonymousClass2 */

            @Override // android.widget.CalendarView.OnDateChangeListener
            public void onSelectedDayChange(CalendarView view, int year, int month, int monthDay) {
                DatePickerSpinnerDelegate.this.setDate(year, month, monthDay);
                DatePickerSpinnerDelegate.this.updateSpinners();
                DatePickerSpinnerDelegate.this.notifyDateChanged();
            }
        });
        this.mDaySpinner = (NumberPicker) this.mDelegator.findViewById(R.id.day);
        this.mDaySpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        this.mDaySpinner.setOnLongPressUpdateInterval(100);
        this.mDaySpinner.setOnValueChangedListener(onChangeListener);
        this.mDaySpinnerInput = (EditText) this.mDaySpinner.findViewById(R.id.numberpicker_input);
        this.mMonthSpinner = (NumberPicker) this.mDelegator.findViewById(R.id.month);
        this.mMonthSpinner.setMinValue(0);
        this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
        this.mMonthSpinner.setDisplayedValues(this.mShortMonths);
        this.mMonthSpinner.setOnLongPressUpdateInterval(200);
        this.mMonthSpinner.setOnValueChangedListener(onChangeListener);
        this.mMonthSpinnerInput = (EditText) this.mMonthSpinner.findViewById(R.id.numberpicker_input);
        this.mYearSpinner = (NumberPicker) this.mDelegator.findViewById(R.id.year);
        this.mYearSpinner.setOnLongPressUpdateInterval(100);
        this.mYearSpinner.setOnValueChangedListener(onChangeListener);
        this.mYearSpinnerInput = (EditText) this.mYearSpinner.findViewById(R.id.numberpicker_input);
        if (spinnersShown || calendarViewShown) {
            setSpinnersShown(spinnersShown);
            setCalendarViewShown(calendarViewShown);
        } else {
            setSpinnersShown(true);
        }
        this.mTempDate.clear();
        if (TextUtils.isEmpty(minDate)) {
            this.mTempDate.set(startYear, 0, 1);
        } else if (!parseDate(minDate, this.mTempDate)) {
            this.mTempDate.set(startYear, 0, 1);
        }
        setMinDate(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        if (TextUtils.isEmpty(maxDate)) {
            this.mTempDate.set(endYear, 11, 31);
        } else if (!parseDate(maxDate, this.mTempDate)) {
            this.mTempDate.set(endYear, 11, 31);
        }
        setMaxDate(this.mTempDate.getTimeInMillis());
        this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5), null);
        reorderSpinners();
        setContentDescriptions();
        if (this.mDelegator.getImportantForAccessibility() == 0) {
            this.mDelegator.setImportantForAccessibility(1);
        }
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void init(int year, int monthOfYear, int dayOfMonth, DatePicker.OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        this.mOnDateChangedListener = onDateChangedListener;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void updateDate(int year, int month, int dayOfMonth) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        }
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mCalendarView.setFirstDayOfWeek(firstDayOfWeek);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public int getFirstDayOfWeek() {
        return this.mCalendarView.getFirstDayOfWeek();
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) != this.mMinDate.get(6)) {
            this.mMinDate.setTimeInMillis(minDate);
            this.mCalendarView.setMinDate(minDate);
            if (this.mCalendarView.getMinDate() != this.mMinDate.getTimeInMillis()) {
                this.mMinDate.setTimeInMillis(this.mCalendarView.getMinDate());
            }
            if (this.mCurrentDate.before(this.mMinDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public Calendar getMinDate() {
        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(this.mCalendarView.getMinDate());
        return minDate;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) != this.mMaxDate.get(6)) {
            this.mMaxDate.setTimeInMillis(maxDate);
            this.mCalendarView.setMaxDate(maxDate);
            if (this.mCurrentDate.after(this.mMaxDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public Calendar getMaxDate() {
        Calendar maxDate = Calendar.getInstance();
        maxDate.setTimeInMillis(this.mCalendarView.getMaxDate());
        return maxDate;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void setEnabled(boolean enabled) {
        this.mDaySpinner.setEnabled(enabled);
        this.mMonthSpinner.setEnabled(enabled);
        this.mYearSpinner.setEnabled(enabled);
        this.mCalendarView.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public CalendarView getCalendarView() {
        return this.mCalendarView;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void setCalendarViewShown(boolean shown) {
        this.mCalendarView.setVisibility(shown ? 0 : 8);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public boolean getCalendarViewShown() {
        return this.mCalendarView.getVisibility() == 0;
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void setSpinnersShown(boolean shown) {
        this.mSpinners.setVisibility(shown ? 0 : 8);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public boolean getSpinnersShown() {
        return this.mSpinners.isShown();
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void onConfigurationChanged(Configuration newConfig) {
        setCurrentLocale(newConfig.locale);
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public Parcelable onSaveInstanceState(Parcelable superState) {
        return new DatePicker.AbstractDatePickerDelegate.SavedState(superState, getYear(), getMonth(), getDayOfMonth(), getMinDate().getTimeInMillis(), getMaxDate().getTimeInMillis());
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof DatePicker.AbstractDatePickerDelegate.SavedState) {
            DatePicker.AbstractDatePickerDelegate.SavedState ss = (DatePicker.AbstractDatePickerDelegate.SavedState) state;
            setDate(ss.getSelectedYear(), ss.getSelectedMonth(), ss.getSelectedDay());
            updateSpinners();
            updateCalendarView();
        }
    }

    @Override // android.widget.DatePicker.DatePickerDelegate
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.DatePicker.AbstractDatePickerDelegate
    public void setCurrentLocale(Locale locale) {
        super.setCurrentLocale(locale);
        this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
        this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
        this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
        this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        this.mNumberOfMonths = this.mTempDate.getActualMaximum(2) + 1;
        this.mShortMonths = LocaleData.get(locale).shortStandAloneMonthNames;
        if (usingNumericMonths()) {
            this.mShortMonths = new String[this.mNumberOfMonths];
            for (int i = 0; i < this.mNumberOfMonths; i++) {
                this.mShortMonths[i] = String.format("%d", Integer.valueOf(i + 1));
            }
        }
    }

    private boolean usingNumericMonths() {
        return Character.isDigit(this.mShortMonths[0].charAt(0));
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar newCalendar = Calendar.getInstance(locale);
        newCalendar.setTimeInMillis(currentTimeMillis);
        return newCalendar;
    }

    private void reorderSpinners() {
        this.mSpinners.removeAllViews();
        char[] order = ICU.getDateFormatOrder(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMdd"));
        int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            char c = order[i];
            if (c == 'M') {
                this.mSpinners.addView(this.mMonthSpinner);
                setImeOptions(this.mMonthSpinner, spinnerCount, i);
            } else if (c == 'd') {
                this.mSpinners.addView(this.mDaySpinner);
                setImeOptions(this.mDaySpinner, spinnerCount, i);
            } else if (c == 'y') {
                this.mSpinners.addView(this.mYearSpinner);
                setImeOptions(this.mYearSpinner, spinnerCount, i);
            } else {
                throw new IllegalArgumentException(Arrays.toString(order));
            }
        }
    }

    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(this.mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        if (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == month && this.mCurrentDate.get(5) == dayOfMonth) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        resetAutofilledValue();
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void updateSpinners() {
        if (this.mCurrentDate.equals(this.mMinDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.getActualMaximum(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else if (this.mCurrentDate.equals(this.mMaxDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.getActualMinimum(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.getActualMinimum(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else {
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(true);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(0);
            this.mMonthSpinner.setMaxValue(11);
            this.mMonthSpinner.setWrapSelectorWheel(true);
        }
        this.mMonthSpinner.setDisplayedValues((String[]) Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1));
        this.mYearSpinner.setMinValue(this.mMinDate.get(1));
        this.mYearSpinner.setMaxValue(this.mMaxDate.get(1));
        this.mYearSpinner.setWrapSelectorWheel(false);
        this.mYearSpinner.setValue(this.mCurrentDate.get(1));
        this.mMonthSpinner.setValue(this.mCurrentDate.get(2));
        this.mDaySpinner.setValue(this.mCurrentDate.get(5));
        if (usingNumericMonths()) {
            this.mMonthSpinnerInput.setRawInputType(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void updateCalendarView() {
        this.mCalendarView.setDate(this.mCurrentDate.getTimeInMillis(), false, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void notifyDateChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onDateChanged(this.mDelegator, getYear(), getMonth(), getDayOfMonth());
        }
        if (this.mAutoFillChangeListener != null) {
            this.mAutoFillChangeListener.onDateChanged(this.mDelegator, getYear(), getMonth(), getDayOfMonth());
        }
    }

    private void setImeOptions(NumberPicker spinner, int spinnerCount, int spinnerIndex) {
        int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = 5;
        } else {
            imeOptions = 6;
        }
        ((TextView) spinner.findViewById(R.id.numberpicker_input)).setImeOptions(imeOptions);
    }

    private void setContentDescriptions() {
        trySetContentDescription(this.mDaySpinner, R.id.increment, R.string.date_picker_increment_day_button);
        trySetContentDescription(this.mDaySpinner, R.id.decrement, R.string.date_picker_decrement_day_button);
        trySetContentDescription(this.mMonthSpinner, R.id.increment, R.string.date_picker_increment_month_button);
        trySetContentDescription(this.mMonthSpinner, R.id.decrement, R.string.date_picker_decrement_month_button);
        trySetContentDescription(this.mYearSpinner, R.id.increment, R.string.date_picker_increment_year_button);
        trySetContentDescription(this.mYearSpinner, R.id.decrement, R.string.date_picker_decrement_year_button);
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(this.mContext.getString(contDescResId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService(InputMethodManager.class);
        if (inputMethodManager == null) {
            return;
        }
        if (inputMethodManager.isActive(this.mYearSpinnerInput)) {
            this.mYearSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mMonthSpinnerInput)) {
            this.mMonthSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mDaySpinnerInput)) {
            this.mDaySpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        }
    }
}
