package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.util.Log;
import com.android.internal.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CalendarView extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    private static final String LOG_TAG = "CalendarView";
    private static final int MODE_HOLO = 0;
    private static final int MODE_MATERIAL = 1;
    private final CalendarViewDelegate mDelegate;

    private interface CalendarViewDelegate {
        boolean getBoundsForDate(long j, Rect rect);

        long getDate();

        int getDateTextAppearance();

        int getFirstDayOfWeek();

        int getFocusedMonthDateColor();

        long getMaxDate();

        long getMinDate();

        Drawable getSelectedDateVerticalBar();

        int getSelectedWeekBackgroundColor();

        boolean getShowWeekNumber();

        int getShownWeekCount();

        int getUnfocusedMonthDateColor();

        int getWeekDayTextAppearance();

        int getWeekNumberColor();

        int getWeekSeparatorLineColor();

        void onConfigurationChanged(Configuration configuration);

        void setDate(long j);

        void setDate(long j, boolean z, boolean z2);

        void setDateTextAppearance(int i);

        void setFirstDayOfWeek(int i);

        void setFocusedMonthDateColor(int i);

        void setMaxDate(long j);

        void setMinDate(long j);

        void setOnDateChangeListener(OnDateChangeListener onDateChangeListener);

        void setSelectedDateVerticalBar(int i);

        void setSelectedDateVerticalBar(Drawable drawable);

        void setSelectedWeekBackgroundColor(int i);

        void setShowWeekNumber(boolean z);

        void setShownWeekCount(int i);

        void setUnfocusedMonthDateColor(int i);

        void setWeekDayTextAppearance(int i);

        void setWeekNumberColor(int i);

        void setWeekSeparatorLineColor(int i);
    }

    static abstract class AbstractCalendarViewDelegate implements CalendarViewDelegate {
        protected static final String DEFAULT_MAX_DATE = "01/01/2100";
        protected static final String DEFAULT_MIN_DATE = "01/01/1900";
        protected Context mContext;
        protected Locale mCurrentLocale;
        protected CalendarView mDelegator;

        AbstractCalendarViewDelegate(CalendarView delegator, Context context) {
            this.mDelegator = delegator;
            this.mContext = context;
            setCurrentLocale(Locale.getDefault());
        }

        protected void setCurrentLocale(Locale locale) {
            if (!locale.equals(this.mCurrentLocale)) {
                this.mCurrentLocale = locale;
            }
        }

        public void setShownWeekCount(int count) {
        }

        public int getShownWeekCount() {
            return 0;
        }

        public void setSelectedWeekBackgroundColor(int color) {
        }

        public int getSelectedWeekBackgroundColor() {
            return 0;
        }

        public void setFocusedMonthDateColor(int color) {
        }

        public int getFocusedMonthDateColor() {
            return 0;
        }

        public void setUnfocusedMonthDateColor(int color) {
        }

        public int getUnfocusedMonthDateColor() {
            return 0;
        }

        public void setWeekNumberColor(int color) {
        }

        public int getWeekNumberColor() {
            return 0;
        }

        public void setWeekSeparatorLineColor(int color) {
        }

        public int getWeekSeparatorLineColor() {
            return 0;
        }

        public void setSelectedDateVerticalBar(int resId) {
        }

        public void setSelectedDateVerticalBar(Drawable drawable) {
        }

        public Drawable getSelectedDateVerticalBar() {
            return null;
        }

        public void setShowWeekNumber(boolean showWeekNumber) {
        }

        public boolean getShowWeekNumber() {
            return false;
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }
    }

    public interface OnDateChangeListener {
        void onSelectedDayChange(CalendarView calendarView, int i, int i2, int i3);
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.calendarViewStyle);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
        int mode = a.getInt(13, 0);
        a.recycle();
        switch (mode) {
            case 0:
                this.mDelegate = new CalendarViewLegacyDelegate(this, context, attrs, defStyleAttr, defStyleRes);
                return;
            case 1:
                this.mDelegate = new CalendarViewMaterialDelegate(this, context, attrs, defStyleAttr, defStyleRes);
                return;
            default:
                throw new IllegalArgumentException("invalid calendarViewMode attribute");
        }
    }

    @Deprecated
    public void setShownWeekCount(int count) {
        this.mDelegate.setShownWeekCount(count);
    }

    @Deprecated
    public int getShownWeekCount() {
        return this.mDelegate.getShownWeekCount();
    }

    @Deprecated
    public void setSelectedWeekBackgroundColor(int color) {
        this.mDelegate.setSelectedWeekBackgroundColor(color);
    }

    @Deprecated
    public int getSelectedWeekBackgroundColor() {
        return this.mDelegate.getSelectedWeekBackgroundColor();
    }

    @Deprecated
    public void setFocusedMonthDateColor(int color) {
        this.mDelegate.setFocusedMonthDateColor(color);
    }

    @Deprecated
    public int getFocusedMonthDateColor() {
        return this.mDelegate.getFocusedMonthDateColor();
    }

    @Deprecated
    public void setUnfocusedMonthDateColor(int color) {
        this.mDelegate.setUnfocusedMonthDateColor(color);
    }

    @Deprecated
    public int getUnfocusedMonthDateColor() {
        return this.mDelegate.getUnfocusedMonthDateColor();
    }

    @Deprecated
    public void setWeekNumberColor(int color) {
        this.mDelegate.setWeekNumberColor(color);
    }

    @Deprecated
    public int getWeekNumberColor() {
        return this.mDelegate.getWeekNumberColor();
    }

    @Deprecated
    public void setWeekSeparatorLineColor(int color) {
        this.mDelegate.setWeekSeparatorLineColor(color);
    }

    @Deprecated
    public int getWeekSeparatorLineColor() {
        return this.mDelegate.getWeekSeparatorLineColor();
    }

    @Deprecated
    public void setSelectedDateVerticalBar(int resourceId) {
        this.mDelegate.setSelectedDateVerticalBar(resourceId);
    }

    @Deprecated
    public void setSelectedDateVerticalBar(Drawable drawable) {
        this.mDelegate.setSelectedDateVerticalBar(drawable);
    }

    @Deprecated
    public Drawable getSelectedDateVerticalBar() {
        return this.mDelegate.getSelectedDateVerticalBar();
    }

    public void setWeekDayTextAppearance(int resourceId) {
        this.mDelegate.setWeekDayTextAppearance(resourceId);
    }

    public int getWeekDayTextAppearance() {
        return this.mDelegate.getWeekDayTextAppearance();
    }

    public void setDateTextAppearance(int resourceId) {
        this.mDelegate.setDateTextAppearance(resourceId);
    }

    public int getDateTextAppearance() {
        return this.mDelegate.getDateTextAppearance();
    }

    public long getMinDate() {
        return this.mDelegate.getMinDate();
    }

    public void setMinDate(long minDate) {
        this.mDelegate.setMinDate(minDate);
    }

    public long getMaxDate() {
        return this.mDelegate.getMaxDate();
    }

    public void setMaxDate(long maxDate) {
        this.mDelegate.setMaxDate(maxDate);
    }

    @Deprecated
    public void setShowWeekNumber(boolean showWeekNumber) {
        this.mDelegate.setShowWeekNumber(showWeekNumber);
    }

    @Deprecated
    public boolean getShowWeekNumber() {
        return this.mDelegate.getShowWeekNumber();
    }

    public int getFirstDayOfWeek() {
        return this.mDelegate.getFirstDayOfWeek();
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mDelegate.setFirstDayOfWeek(firstDayOfWeek);
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.mDelegate.setOnDateChangeListener(listener);
    }

    public long getDate() {
        return this.mDelegate.getDate();
    }

    public void setDate(long date) {
        this.mDelegate.setDate(date);
    }

    public void setDate(long date, boolean animate, boolean center) {
        this.mDelegate.setDate(date, animate, center);
    }

    public boolean getBoundsForDate(long date, Rect outBounds) {
        return this.mDelegate.getBoundsForDate(date, outBounds);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDelegate.onConfigurationChanged(newConfig);
    }

    public CharSequence getAccessibilityClassName() {
        return CalendarView.class.getName();
    }

    public static boolean parseDate(String date, Calendar outDate) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        try {
            outDate.setTime(DATE_FORMATTER.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }
}
