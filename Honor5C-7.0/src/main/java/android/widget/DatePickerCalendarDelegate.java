package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.text.DisplayContext;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.DayPickerView.OnDaySelectedListener;
import android.widget.YearPickerView.OnYearSelectedListener;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.AsyncService;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.util.Locale;

class DatePickerCalendarDelegate extends AbstractDatePickerDelegate {
    private static final int ANIMATION_DURATION = 300;
    private static final int[] ATTRS_DISABLED_ALPHA = null;
    private static final int[] ATTRS_TEXT_COLOR = null;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int UNINITIALIZED = -1;
    private static final int USE_LOCALE = 0;
    private static final int VIEW_MONTH_DAY = 0;
    private static final int VIEW_YEAR = 1;
    private ViewAnimator mAnimator;
    private ViewGroup mContainer;
    private final Calendar mCurrentDate;
    private int mCurrentView;
    private OnDateChangedListener mDateChangedListener;
    private DayPickerView mDayPickerView;
    private int mFirstDayOfWeek;
    private TextView mHeaderMonthDay;
    private TextView mHeaderYear;
    private final Calendar mMaxDate;
    private final Calendar mMinDate;
    private SimpleDateFormat mMonthDayFormat;
    private final OnDaySelectedListener mOnDaySelectedListener;
    private final OnClickListener mOnHeaderClickListener;
    private final OnYearSelectedListener mOnYearSelectedListener;
    private String mSelectDay;
    private String mSelectYear;
    private final Calendar mTempDate;
    private SimpleDateFormat mYearFormat;
    private YearPickerView mYearPickerView;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.DatePickerCalendarDelegate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.DatePickerCalendarDelegate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePickerCalendarDelegate.<clinit>():void");
    }

    public DatePickerCalendarDelegate(DatePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        this.mCurrentView = UNINITIALIZED;
        this.mFirstDayOfWeek = VIEW_MONTH_DAY;
        this.mOnDaySelectedListener = new OnDaySelectedListener() {
            public void onDaySelected(DayPickerView view, Calendar day) {
                DatePickerCalendarDelegate.this.mCurrentDate.setTimeInMillis(day.getTimeInMillis());
                DatePickerCalendarDelegate.this.onDateChanged(true, true);
            }
        };
        this.mOnYearSelectedListener = new OnYearSelectedListener() {
            public void onYearChanged(YearPickerView view, int year) {
                int day = DatePickerCalendarDelegate.this.mCurrentDate.get(5);
                int daysInMonth = DatePickerCalendarDelegate.getDaysInMonth(DatePickerCalendarDelegate.this.mCurrentDate.get(2), year);
                if (day > daysInMonth) {
                    DatePickerCalendarDelegate.this.mCurrentDate.set(5, daysInMonth);
                }
                DatePickerCalendarDelegate.this.mCurrentDate.set(DatePickerCalendarDelegate.VIEW_YEAR, year);
                DatePickerCalendarDelegate.this.onDateChanged(true, true);
                DatePickerCalendarDelegate.this.setCurrentView(DatePickerCalendarDelegate.VIEW_MONTH_DAY);
                DatePickerCalendarDelegate.this.mHeaderYear.requestFocus();
            }
        };
        this.mOnHeaderClickListener = new OnClickListener() {
            public void onClick(View v) {
                DatePickerCalendarDelegate.this.tryVibrate();
                switch (v.getId()) {
                    case R.id.date_picker_header_year /*16909131*/:
                        DatePickerCalendarDelegate.this.setCurrentView(DatePickerCalendarDelegate.VIEW_YEAR);
                    case R.id.date_picker_header_date /*16909133*/:
                        DatePickerCalendarDelegate.this.setCurrentView(DatePickerCalendarDelegate.VIEW_MONTH_DAY);
                    default:
                }
            }
        };
        Locale locale = this.mCurrentLocale;
        this.mCurrentDate = Calendar.getInstance(locale);
        this.mTempDate = Calendar.getInstance(locale);
        this.mMinDate = Calendar.getInstance(locale);
        this.mMaxDate = Calendar.getInstance(locale);
        this.mMinDate.set(DEFAULT_START_YEAR, VIEW_MONTH_DAY, VIEW_YEAR);
        this.mMaxDate.set(DEFAULT_END_YEAR, 11, 31);
        Resources res = this.mDelegator.getResources();
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        this.mContainer = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(a.getResourceId(17, R.layout.date_picker_material), this.mDelegator, false);
        this.mDelegator.addView(this.mContainer);
        ViewGroup header = (ViewGroup) this.mContainer.findViewById(R.id.date_picker_header);
        this.mHeaderYear = (TextView) header.findViewById(R.id.date_picker_header_year);
        this.mHeaderYear.setOnClickListener(this.mOnHeaderClickListener);
        this.mHeaderMonthDay = (TextView) header.findViewById(R.id.date_picker_header_date);
        this.mHeaderMonthDay.setOnClickListener(this.mOnHeaderClickListener);
        ColorStateList colorStateList = null;
        int monthHeaderTextAppearance = a.getResourceId(10, VIEW_MONTH_DAY);
        if (monthHeaderTextAppearance != 0) {
            TypedArray textAppearance = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, VIEW_MONTH_DAY, monthHeaderTextAppearance);
            colorStateList = applyLegacyColorFixes(textAppearance.getColorStateList(VIEW_MONTH_DAY));
            textAppearance.recycle();
        }
        if (colorStateList == null) {
            colorStateList = a.getColorStateList(18);
        }
        if (colorStateList != null) {
            this.mHeaderYear.setTextColor(colorStateList);
            this.mHeaderMonthDay.setTextColor(colorStateList);
        }
        if (a.hasValueOrEmpty(VIEW_MONTH_DAY)) {
            header.setBackground(a.getDrawable(VIEW_MONTH_DAY));
        }
        a.recycle();
        this.mAnimator = (ViewAnimator) this.mContainer.findViewById(R.id.animator);
        this.mDayPickerView = (DayPickerView) this.mAnimator.findViewById(R.id.date_picker_day_picker);
        this.mDayPickerView.setFirstDayOfWeek(this.mFirstDayOfWeek);
        this.mDayPickerView.setMinDate(this.mMinDate.getTimeInMillis());
        this.mDayPickerView.setMaxDate(this.mMaxDate.getTimeInMillis());
        this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
        this.mDayPickerView.setOnDaySelectedListener(this.mOnDaySelectedListener);
        this.mYearPickerView = (YearPickerView) this.mAnimator.findViewById(R.id.date_picker_year_picker);
        this.mYearPickerView.setRange(this.mMinDate, this.mMaxDate);
        this.mYearPickerView.setYear(this.mCurrentDate.get(VIEW_YEAR));
        this.mYearPickerView.setOnYearSelectedListener(this.mOnYearSelectedListener);
        this.mSelectDay = res.getString(R.string.select_day);
        this.mSelectYear = res.getString(R.string.select_year);
        onLocaleChanged(this.mCurrentLocale);
        setCurrentView(VIEW_MONTH_DAY);
    }

    private ColorStateList applyLegacyColorFixes(ColorStateList color) {
        if (color == null || color.hasState(R.attr.state_activated)) {
            return color;
        }
        int activatedColor;
        int defaultColor;
        if (color.hasState(R.attr.state_selected)) {
            activatedColor = color.getColorForState(StateSet.get(10), VIEW_MONTH_DAY);
            defaultColor = color.getColorForState(StateSet.get(8), VIEW_MONTH_DAY);
        } else {
            activatedColor = color.getDefaultColor();
            defaultColor = multiplyAlphaComponent(activatedColor, this.mContext.obtainStyledAttributes(ATTRS_DISABLED_ALPHA).getFloat(VIEW_MONTH_DAY, 0.3f));
        }
        if (activatedColor == 0 || defaultColor == 0) {
            return null;
        }
        stateSet = new int[2][];
        int[] iArr = new int[VIEW_YEAR];
        iArr[VIEW_MONTH_DAY] = R.attr.state_activated;
        stateSet[VIEW_MONTH_DAY] = iArr;
        stateSet[VIEW_YEAR] = new int[VIEW_MONTH_DAY];
        return new ColorStateList(stateSet, new int[]{activatedColor, defaultColor});
    }

    private int multiplyAlphaComponent(int color, float alphaMod) {
        return (((int) ((((float) ((color >> 24) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE)) * alphaMod) + 0.5f)) << 24) | (color & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT);
    }

    protected void onLocaleChanged(Locale locale) {
        if (this.mHeaderYear != null) {
            this.mMonthDayFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EMMMd"), locale);
            this.mMonthDayFormat.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            this.mYearFormat = new SimpleDateFormat("y", locale);
            onCurrentDateChanged(false);
        }
    }

    private void onCurrentDateChanged(boolean announce) {
        if (this.mHeaderYear != null) {
            this.mHeaderYear.setText(this.mYearFormat.format(this.mCurrentDate.getTime()));
            this.mHeaderMonthDay.setText(this.mMonthDayFormat.format(this.mCurrentDate.getTime()));
            if (announce) {
                this.mAnimator.announceForAccessibility(DateUtils.formatDateTime(this.mContext, this.mCurrentDate.getTimeInMillis(), 20));
            }
        }
    }

    private void setCurrentView(int viewIndex) {
        switch (viewIndex) {
            case VIEW_MONTH_DAY /*0*/:
                this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
                if (this.mCurrentView != viewIndex) {
                    this.mHeaderMonthDay.setActivated(true);
                    this.mHeaderYear.setActivated(false);
                    this.mAnimator.setDisplayedChild(VIEW_MONTH_DAY);
                    this.mCurrentView = viewIndex;
                }
                this.mAnimator.announceForAccessibility(this.mSelectDay);
            case VIEW_YEAR /*1*/:
                this.mYearPickerView.setYear(this.mCurrentDate.get(VIEW_YEAR));
                this.mYearPickerView.post(new Runnable() {
                    public void run() {
                        DatePickerCalendarDelegate.this.mYearPickerView.requestFocus();
                        View selected = DatePickerCalendarDelegate.this.mYearPickerView.getSelectedView();
                        if (selected != null) {
                            selected.requestFocus();
                        }
                    }
                });
                if (this.mCurrentView != viewIndex) {
                    this.mHeaderMonthDay.setActivated(false);
                    this.mHeaderYear.setActivated(true);
                    this.mAnimator.setDisplayedChild(VIEW_YEAR);
                    this.mCurrentView = viewIndex;
                }
                this.mAnimator.announceForAccessibility(this.mSelectYear);
            default:
        }
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener callBack) {
        this.mCurrentDate.set(VIEW_YEAR, year);
        this.mCurrentDate.set(2, monthOfYear);
        this.mCurrentDate.set(5, dayOfMonth);
        onDateChanged(false, false);
        this.mDateChangedListener = callBack;
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(VIEW_YEAR, year);
        this.mCurrentDate.set(2, month);
        this.mCurrentDate.set(5, dayOfMonth);
        onDateChanged(false, true);
    }

    private void onDateChanged(boolean fromUser, boolean callbackToClient) {
        int year = this.mCurrentDate.get(VIEW_YEAR);
        if (callbackToClient && this.mDateChangedListener != null) {
            this.mDateChangedListener.onDateChanged(this.mDelegator, year, this.mCurrentDate.get(2), this.mCurrentDate.get(5));
        }
        this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
        this.mYearPickerView.setYear(year);
        onCurrentDateChanged(fromUser);
        if (fromUser) {
            tryVibrate();
        }
    }

    public int getYear() {
        return this.mCurrentDate.get(VIEW_YEAR);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(VIEW_YEAR) != this.mMinDate.get(VIEW_YEAR) || this.mTempDate.get(6) != this.mMinDate.get(6)) {
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
        if (this.mTempDate.get(VIEW_YEAR) != this.mMaxDate.get(VIEW_YEAR) || this.mTempDate.get(6) != this.mMaxDate.get(6)) {
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
        int year = this.mCurrentDate.get(VIEW_YEAR);
        int month = this.mCurrentDate.get(2);
        int day = this.mCurrentDate.get(5);
        int listPosition = UNINITIALIZED;
        int listPositionOffset = UNINITIALIZED;
        if (this.mCurrentView == 0) {
            listPosition = this.mDayPickerView.getMostVisiblePosition();
        } else if (this.mCurrentView == VIEW_YEAR) {
            listPosition = this.mYearPickerView.getFirstVisiblePosition();
            listPositionOffset = this.mYearPickerView.getFirstPositionOffset();
        }
        return new SavedState(superState, year, month, day, this.mMinDate.getTimeInMillis(), this.mMaxDate.getTimeInMillis(), this.mCurrentView, listPosition, listPositionOffset);
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            this.mCurrentDate.set(ss.getSelectedYear(), ss.getSelectedMonth(), ss.getSelectedDay());
            this.mMinDate.setTimeInMillis(ss.getMinDate());
            this.mMaxDate.setTimeInMillis(ss.getMaxDate());
            onCurrentDateChanged(false);
            int currentView = ss.getCurrentView();
            setCurrentView(currentView);
            int listPosition = ss.getListPosition();
            if (listPosition == UNINITIALIZED) {
                return;
            }
            if (currentView == 0) {
                this.mDayPickerView.setPosition(listPosition);
            } else if (currentView == VIEW_YEAR) {
                this.mYearPickerView.setSelectionFromTop(listPosition, ss.getListPositionOffset());
            }
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(this.mCurrentDate.getTime().toString());
    }

    public CharSequence getAccessibilityClassName() {
        return DatePicker.class.getName();
    }

    public static int getDaysInMonth(int month, int year) {
        switch (month) {
            case VIEW_MONTH_DAY /*0*/:
            case HwCfgFilePolicy.PC /*2*/:
            case HwCfgFilePolicy.CUST /*4*/:
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
            case HwCfgFilePolicy.CLOUD_APN /*7*/:
            case PGSdk.TYPE_SCRLOCK /*9*/:
            case PGSdk.TYPE_IM /*11*/:
                return 31;
            case VIEW_YEAR /*1*/:
                return year % 4 == 0 ? 29 : 28;
            case HwCfgFilePolicy.BASE /*3*/:
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
            case PGSdk.TYPE_VIDEO /*8*/:
            case PGSdk.TYPE_CLOCK /*10*/:
                return 30;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    private void tryVibrate() {
        this.mDelegator.performHapticFeedback(5);
    }
}
