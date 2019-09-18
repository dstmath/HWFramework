package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.icu.util.Calendar;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.DatePicker;
import android.widget.DayPickerView;
import android.widget.YearPickerView;
import com.android.internal.R;
import java.util.Locale;

class DatePickerCalendarDelegate extends DatePicker.AbstractDatePickerDelegate {
    private static final int ANIMATION_DURATION = 300;
    private static final int[] ATTRS_DISABLED_ALPHA = {16842803};
    private static final int[] ATTRS_TEXT_COLOR = {16842904};
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int UNINITIALIZED = -1;
    private static final int USE_LOCALE = 0;
    private static final int VIEW_MONTH_DAY = 0;
    private static final int VIEW_YEAR = 1;
    private ViewAnimator mAnimator;
    private ViewGroup mContainer;
    private int mCurrentView = -1;
    private DayPickerView mDayPickerView;
    private int mFirstDayOfWeek = 0;
    private TextView mHeaderMonthDay;
    /* access modifiers changed from: private */
    public TextView mHeaderYear;
    private final Calendar mMaxDate;
    private final Calendar mMinDate;
    private DateFormat mMonthDayFormat;
    private final DayPickerView.OnDaySelectedListener mOnDaySelectedListener = new DayPickerView.OnDaySelectedListener() {
        public void onDaySelected(DayPickerView view, Calendar day) {
            DatePickerCalendarDelegate.this.mCurrentDate.setTimeInMillis(day.getTimeInMillis());
            DatePickerCalendarDelegate.this.onDateChanged(true, true);
        }
    };
    private final View.OnClickListener mOnHeaderClickListener = new View.OnClickListener() {
        public final void onClick(View view) {
            DatePickerCalendarDelegate.lambda$new$0(DatePickerCalendarDelegate.this, view);
        }
    };
    private final YearPickerView.OnYearSelectedListener mOnYearSelectedListener = new YearPickerView.OnYearSelectedListener() {
        public void onYearChanged(YearPickerView view, int year) {
            int day = DatePickerCalendarDelegate.this.mCurrentDate.get(5);
            int daysInMonth = DatePickerCalendarDelegate.getDaysInMonth(DatePickerCalendarDelegate.this.mCurrentDate.get(2), year);
            if (day > daysInMonth) {
                DatePickerCalendarDelegate.this.mCurrentDate.set(5, daysInMonth);
            }
            DatePickerCalendarDelegate.this.mCurrentDate.set(1, year);
            DatePickerCalendarDelegate.this.onDateChanged(true, true);
            DatePickerCalendarDelegate.this.setCurrentView(0);
            DatePickerCalendarDelegate.this.mHeaderYear.requestFocus();
        }
    };
    private String mSelectDay;
    private String mSelectYear;
    private final Calendar mTempDate;
    private DateFormat mYearFormat;
    private YearPickerView mYearPickerView;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DatePickerCalendarDelegate(DatePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        Locale locale = this.mCurrentLocale;
        this.mCurrentDate = Calendar.getInstance(locale);
        this.mTempDate = Calendar.getInstance(locale);
        this.mMinDate = Calendar.getInstance(locale);
        this.mMaxDate = Calendar.getInstance(locale);
        this.mMinDate.set(1900, 0, 1);
        this.mMaxDate.set(2100, 11, 31);
        Resources res = this.mDelegator.getResources();
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        this.mContainer = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(a.getResourceId(19, 17367123), (ViewGroup) this.mDelegator, false);
        this.mContainer.setSaveFromParentEnabled(false);
        this.mDelegator.addView(this.mContainer);
        ViewGroup header = (ViewGroup) this.mContainer.findViewById(16908845);
        this.mHeaderYear = (TextView) header.findViewById(16908847);
        this.mHeaderYear.setOnClickListener(this.mOnHeaderClickListener);
        this.mHeaderMonthDay = (TextView) header.findViewById(16908846);
        this.mHeaderMonthDay.setOnClickListener(this.mOnHeaderClickListener);
        ColorStateList headerTextColor = null;
        int monthHeaderTextAppearance = a.getResourceId(10, 0);
        if (monthHeaderTextAppearance != 0) {
            TypedArray textAppearance = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, monthHeaderTextAppearance);
            headerTextColor = applyLegacyColorFixes(textAppearance.getColorStateList(0));
            textAppearance.recycle();
        }
        headerTextColor = headerTextColor == null ? a.getColorStateList(18) : headerTextColor;
        if (headerTextColor != null) {
            this.mHeaderYear.setTextColor(headerTextColor);
            this.mHeaderMonthDay.setTextColor(headerTextColor);
        }
        if (a.hasValueOrEmpty(0)) {
            header.setBackground(a.getDrawable(0));
        }
        a.recycle();
        this.mAnimator = (ViewAnimator) this.mContainer.findViewById(16908737);
        this.mDayPickerView = (DayPickerView) this.mAnimator.findViewById(16908844);
        this.mDayPickerView.setFirstDayOfWeek(this.mFirstDayOfWeek);
        this.mDayPickerView.setMinDate(this.mMinDate.getTimeInMillis());
        this.mDayPickerView.setMaxDate(this.mMaxDate.getTimeInMillis());
        this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
        this.mDayPickerView.setOnDaySelectedListener(this.mOnDaySelectedListener);
        this.mYearPickerView = (YearPickerView) this.mAnimator.findViewById(16908848);
        this.mYearPickerView.setRange(this.mMinDate, this.mMaxDate);
        this.mYearPickerView.setYear(this.mCurrentDate.get(1));
        this.mYearPickerView.setOnYearSelectedListener(this.mOnYearSelectedListener);
        this.mSelectDay = res.getString(17041060);
        this.mSelectYear = res.getString(17041066);
        onLocaleChanged(this.mCurrentLocale);
        setCurrentView(0);
    }

    private ColorStateList applyLegacyColorFixes(ColorStateList color) {
        int defaultColor;
        int activatedColor;
        if (color == null || color.hasState(16843518)) {
            return color;
        }
        if (color.hasState(16842913)) {
            activatedColor = color.getColorForState(StateSet.get(10), 0);
            defaultColor = color.getColorForState(StateSet.get(8), 0);
        } else {
            activatedColor = color.getDefaultColor();
            defaultColor = multiplyAlphaComponent(activatedColor, this.mContext.obtainStyledAttributes(ATTRS_DISABLED_ALPHA).getFloat(0, 0.3f));
        }
        if (activatedColor == 0 || defaultColor == 0) {
            return null;
        }
        return new ColorStateList(new int[][]{new int[]{16843518}, new int[0]}, new int[]{activatedColor, defaultColor});
    }

    private int multiplyAlphaComponent(int color, float alphaMod) {
        return (((int) ((((float) ((color >> 24) & 255)) * alphaMod) + 0.5f)) << 24) | (16777215 & color);
    }

    public static /* synthetic */ void lambda$new$0(DatePickerCalendarDelegate datePickerCalendarDelegate, View v) {
        datePickerCalendarDelegate.tryVibrate();
        switch (v.getId()) {
            case 16908846:
                datePickerCalendarDelegate.setCurrentView(0);
                return;
            case 16908847:
                datePickerCalendarDelegate.setCurrentView(1);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void onLocaleChanged(Locale locale) {
        if (this.mHeaderYear != null) {
            this.mMonthDayFormat = DateFormat.getInstanceForSkeleton("EMMMd", locale);
            this.mMonthDayFormat.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            this.mYearFormat = DateFormat.getInstanceForSkeleton("y", locale);
            onCurrentDateChanged(false);
        }
    }

    private void onCurrentDateChanged(boolean announce) {
        if (this.mHeaderYear != null) {
            this.mHeaderYear.setText((CharSequence) this.mYearFormat.format(this.mCurrentDate.getTime()));
            this.mHeaderMonthDay.setText((CharSequence) this.mMonthDayFormat.format(this.mCurrentDate.getTime()));
            if (announce) {
                this.mAnimator.announceForAccessibility(getFormattedCurrentDate());
            }
        }
    }

    /* access modifiers changed from: private */
    public void setCurrentView(int viewIndex) {
        switch (viewIndex) {
            case 0:
                this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
                if (this.mCurrentView != viewIndex) {
                    this.mHeaderMonthDay.setActivated(true);
                    this.mHeaderYear.setActivated(false);
                    this.mAnimator.setDisplayedChild(0);
                    this.mCurrentView = viewIndex;
                }
                this.mAnimator.announceForAccessibility(this.mSelectDay);
                return;
            case 1:
                this.mYearPickerView.setYear(this.mCurrentDate.get(1));
                this.mYearPickerView.post(new Runnable() {
                    public final void run() {
                        DatePickerCalendarDelegate.lambda$setCurrentView$1(DatePickerCalendarDelegate.this);
                    }
                });
                if (this.mCurrentView != viewIndex) {
                    this.mHeaderMonthDay.setActivated(false);
                    this.mHeaderYear.setActivated(true);
                    this.mAnimator.setDisplayedChild(1);
                    this.mCurrentView = viewIndex;
                }
                this.mAnimator.announceForAccessibility(this.mSelectYear);
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$setCurrentView$1(DatePickerCalendarDelegate datePickerCalendarDelegate) {
        datePickerCalendarDelegate.mYearPickerView.requestFocus();
        View selected = datePickerCalendarDelegate.mYearPickerView.getSelectedView();
        if (selected != null) {
            selected.requestFocus();
        }
    }

    public void init(int year, int month, int dayOfMonth, DatePicker.OnDateChangedListener callBack) {
        setDate(year, month, dayOfMonth);
        onDateChanged(false, false);
        this.mOnDateChangedListener = callBack;
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        onDateChanged(false, true);
    }

    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(1, year);
        this.mCurrentDate.set(2, month);
        this.mCurrentDate.set(5, dayOfMonth);
        resetAutofilledValue();
    }

    /* access modifiers changed from: private */
    public void onDateChanged(boolean fromUser, boolean callbackToClient) {
        int year = this.mCurrentDate.get(1);
        if (callbackToClient && !(this.mOnDateChangedListener == null && this.mAutoFillChangeListener == null)) {
            int monthOfYear = this.mCurrentDate.get(2);
            int dayOfMonth = this.mCurrentDate.get(5);
            if (this.mOnDateChangedListener != null) {
                this.mOnDateChangedListener.onDateChanged(this.mDelegator, year, monthOfYear, dayOfMonth);
            }
            if (this.mAutoFillChangeListener != null) {
                this.mAutoFillChangeListener.onDateChanged(this.mDelegator, year, monthOfYear, dayOfMonth);
            }
        }
        this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
        this.mYearPickerView.setYear(year);
        onCurrentDateChanged(fromUser);
        if (fromUser) {
            tryVibrate();
        }
    }

    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) != this.mMinDate.get(6)) {
            if (this.mCurrentDate.before(this.mTempDate)) {
                this.mCurrentDate.setTimeInMillis(minDate);
                onDateChanged(false, true);
            }
            this.mMinDate.setTimeInMillis(minDate);
            this.mDayPickerView.setMinDate(minDate);
            this.mYearPickerView.setRange(this.mMinDate, this.mMaxDate);
        }
    }

    public Calendar getMinDate() {
        return this.mMinDate;
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) != this.mMaxDate.get(6)) {
            if (this.mCurrentDate.after(this.mTempDate)) {
                this.mCurrentDate.setTimeInMillis(maxDate);
                onDateChanged(false, true);
            }
            this.mMaxDate.setTimeInMillis(maxDate);
            this.mDayPickerView.setMaxDate(maxDate);
            this.mYearPickerView.setRange(this.mMinDate, this.mMaxDate);
        }
    }

    public Calendar getMaxDate() {
        return this.mMaxDate;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mFirstDayOfWeek = firstDayOfWeek;
        this.mDayPickerView.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        if (this.mFirstDayOfWeek != 0) {
            return this.mFirstDayOfWeek;
        }
        return this.mCurrentDate.getFirstDayOfWeek();
    }

    public void setEnabled(boolean enabled) {
        this.mContainer.setEnabled(enabled);
        this.mDayPickerView.setEnabled(enabled);
        this.mYearPickerView.setEnabled(enabled);
        this.mHeaderYear.setEnabled(enabled);
        this.mHeaderMonthDay.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return this.mContainer.isEnabled();
    }

    public CalendarView getCalendarView() {
        throw new UnsupportedOperationException("Not supported by calendar-mode DatePicker");
    }

    public void setCalendarViewShown(boolean shown) {
    }

    public boolean getCalendarViewShown() {
        return false;
    }

    public void setSpinnersShown(boolean shown) {
    }

    public boolean getSpinnersShown() {
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setCurrentLocale(newConfig.locale);
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        int listPositionOffset;
        int listPosition;
        int year = this.mCurrentDate.get(1);
        int month = this.mCurrentDate.get(2);
        int day = this.mCurrentDate.get(5);
        if (this.mCurrentView == 0) {
            listPosition = this.mDayPickerView.getMostVisiblePosition();
        } else if (this.mCurrentView == 1) {
            listPosition = this.mYearPickerView.getFirstVisiblePosition();
            listPositionOffset = this.mYearPickerView.getFirstPositionOffset();
            DatePicker.AbstractDatePickerDelegate.SavedState savedState = new DatePicker.AbstractDatePickerDelegate.SavedState(superState, year, month, day, this.mMinDate.getTimeInMillis(), this.mMaxDate.getTimeInMillis(), this.mCurrentView, listPosition, listPositionOffset);
            return savedState;
        } else {
            listPosition = -1;
        }
        listPositionOffset = -1;
        DatePicker.AbstractDatePickerDelegate.SavedState savedState2 = new DatePicker.AbstractDatePickerDelegate.SavedState(superState, year, month, day, this.mMinDate.getTimeInMillis(), this.mMaxDate.getTimeInMillis(), this.mCurrentView, listPosition, listPositionOffset);
        return savedState2;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof DatePicker.AbstractDatePickerDelegate.SavedState) {
            DatePicker.AbstractDatePickerDelegate.SavedState ss = (DatePicker.AbstractDatePickerDelegate.SavedState) state;
            this.mCurrentDate.set(ss.getSelectedYear(), ss.getSelectedMonth(), ss.getSelectedDay());
            this.mMinDate.setTimeInMillis(ss.getMinDate());
            this.mMaxDate.setTimeInMillis(ss.getMaxDate());
            onCurrentDateChanged(false);
            int currentView = ss.getCurrentView();
            setCurrentView(currentView);
            int listPosition = ss.getListPosition();
            if (listPosition == -1) {
                return;
            }
            if (currentView == 0) {
                this.mDayPickerView.setPosition(listPosition);
            } else if (currentView == 1) {
                this.mYearPickerView.setSelectionFromTop(listPosition, ss.getListPositionOffset());
            }
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public CharSequence getAccessibilityClassName() {
        return DatePicker.class.getName();
    }

    public static int getDaysInMonth(int month, int year) {
        switch (month) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                return year % 4 == 0 ? 29 : 28;
            case 3:
            case 5:
            case 8:
            case 10:
                return 30;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    private void tryVibrate() {
        this.mDelegator.performHapticFeedback(5);
    }
}
