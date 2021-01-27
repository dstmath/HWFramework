package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
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
    @UnsupportedAppUsage
    private final CalendarViewDelegate mDelegate;

    /* access modifiers changed from: private */
    public interface CalendarViewDelegate {
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

    public interface OnDateChangeListener {
        void onSelectedDayChange(CalendarView calendarView, int i, int i2, int i3);
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<CalendarView> {
        private int mDateTextAppearanceId;
        private int mFirstDayOfWeekId;
        private int mFocusedMonthDateColorId;
        private int mMaxDateId;
        private int mMinDateId;
        private boolean mPropertiesMapped = false;
        private int mSelectedDateVerticalBarId;
        private int mSelectedWeekBackgroundColorId;
        private int mShowWeekNumberId;
        private int mShownWeekCountId;
        private int mUnfocusedMonthDateColorId;
        private int mWeekDayTextAppearanceId;
        private int mWeekNumberColorId;
        private int mWeekSeparatorLineColorId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mDateTextAppearanceId = propertyMapper.mapResourceId("dateTextAppearance", 16843593);
            this.mFirstDayOfWeekId = propertyMapper.mapInt("firstDayOfWeek", 16843581);
            this.mFocusedMonthDateColorId = propertyMapper.mapColor("focusedMonthDateColor", 16843587);
            this.mMaxDateId = propertyMapper.mapLong("maxDate", 16843584);
            this.mMinDateId = propertyMapper.mapLong("minDate", 16843583);
            this.mSelectedDateVerticalBarId = propertyMapper.mapObject("selectedDateVerticalBar", 16843591);
            this.mSelectedWeekBackgroundColorId = propertyMapper.mapColor("selectedWeekBackgroundColor", 16843586);
            this.mShowWeekNumberId = propertyMapper.mapBoolean("showWeekNumber", 16843582);
            this.mShownWeekCountId = propertyMapper.mapInt("shownWeekCount", 16843585);
            this.mUnfocusedMonthDateColorId = propertyMapper.mapColor("unfocusedMonthDateColor", 16843588);
            this.mWeekDayTextAppearanceId = propertyMapper.mapResourceId("weekDayTextAppearance", 16843592);
            this.mWeekNumberColorId = propertyMapper.mapColor("weekNumberColor", 16843589);
            this.mWeekSeparatorLineColorId = propertyMapper.mapColor("weekSeparatorLineColor", 16843590);
            this.mPropertiesMapped = true;
        }

        public void readProperties(CalendarView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readResourceId(this.mDateTextAppearanceId, node.getDateTextAppearance());
                propertyReader.readInt(this.mFirstDayOfWeekId, node.getFirstDayOfWeek());
                propertyReader.readColor(this.mFocusedMonthDateColorId, node.getFocusedMonthDateColor());
                propertyReader.readLong(this.mMaxDateId, node.getMaxDate());
                propertyReader.readLong(this.mMinDateId, node.getMinDate());
                propertyReader.readObject(this.mSelectedDateVerticalBarId, node.getSelectedDateVerticalBar());
                propertyReader.readColor(this.mSelectedWeekBackgroundColorId, node.getSelectedWeekBackgroundColor());
                propertyReader.readBoolean(this.mShowWeekNumberId, node.getShowWeekNumber());
                propertyReader.readInt(this.mShownWeekCountId, node.getShownWeekCount());
                propertyReader.readColor(this.mUnfocusedMonthDateColorId, node.getUnfocusedMonthDateColor());
                propertyReader.readResourceId(this.mWeekDayTextAppearanceId, node.getWeekDayTextAppearance());
                propertyReader.readColor(this.mWeekNumberColorId, node.getWeekNumberColor());
                propertyReader.readColor(this.mWeekSeparatorLineColorId, node.getWeekSeparatorLineColor());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843613);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.CalendarView, attrs, a, defStyleAttr, defStyleRes);
        int mode = a.getInt(13, 0);
        a.recycle();
        if (mode == 0) {
            this.mDelegate = new CalendarViewLegacyDelegate(this, context, attrs, defStyleAttr, defStyleRes);
        } else if (mode == 1) {
            this.mDelegate = new CalendarViewMaterialDelegate(this, context, attrs, defStyleAttr, defStyleRes);
        } else {
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

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDelegate.onConfigurationChanged(newConfig);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return CalendarView.class.getName();
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

        /* access modifiers changed from: protected */
        public void setCurrentLocale(Locale locale) {
            if (!locale.equals(this.mCurrentLocale)) {
                this.mCurrentLocale = locale;
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setShownWeekCount(int count) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getShownWeekCount() {
            return 0;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setSelectedWeekBackgroundColor(int color) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getSelectedWeekBackgroundColor() {
            return 0;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setFocusedMonthDateColor(int color) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getFocusedMonthDateColor() {
            return 0;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setUnfocusedMonthDateColor(int color) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getUnfocusedMonthDateColor() {
            return 0;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setWeekNumberColor(int color) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getWeekNumberColor() {
            return 0;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setWeekSeparatorLineColor(int color) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getWeekSeparatorLineColor() {
            return 0;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setSelectedDateVerticalBar(int resId) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setSelectedDateVerticalBar(Drawable drawable) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public Drawable getSelectedDateVerticalBar() {
            return null;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setShowWeekNumber(boolean showWeekNumber) {
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public boolean getShowWeekNumber() {
            return false;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void onConfigurationChanged(Configuration newConfig) {
        }
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
