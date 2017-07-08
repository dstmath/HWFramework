package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.util.Log;
import com.android.internal.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

public class CalendarView extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final DateFormat DATE_FORMATTER = null;
    private static final String LOG_TAG = "CalendarView";
    private static final int MODE_HOLO = 0;
    private static final int MODE_MATERIAL = 1;
    private final CalendarViewDelegate mDelegate;

    private interface CalendarViewDelegate {
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
            return CalendarView.MODE_HOLO;
        }

        public void setSelectedWeekBackgroundColor(int color) {
        }

        public int getSelectedWeekBackgroundColor() {
            return CalendarView.MODE_HOLO;
        }

        public void setFocusedMonthDateColor(int color) {
        }

        public int getFocusedMonthDateColor() {
            return CalendarView.MODE_HOLO;
        }

        public void setUnfocusedMonthDateColor(int color) {
        }

        public int getUnfocusedMonthDateColor() {
            return CalendarView.MODE_HOLO;
        }

        public void setWeekNumberColor(int color) {
        }

        public int getWeekNumberColor() {
            return CalendarView.MODE_HOLO;
        }

        public void setWeekSeparatorLineColor(int color) {
        }

        public int getWeekSeparatorLineColor() {
            return CalendarView.MODE_HOLO;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.CalendarView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.CalendarView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.CalendarView.<clinit>():void");
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.calendarViewStyle);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, MODE_HOLO);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
        int mode = a.getInt(16, MODE_HOLO);
        a.recycle();
        switch (mode) {
            case MODE_HOLO /*0*/:
                this.mDelegate = new CalendarViewLegacyDelegate(this, context, attrs, defStyleAttr, defStyleRes);
            case MODE_MATERIAL /*1*/:
                this.mDelegate = new CalendarViewMaterialDelegate(this, context, attrs, defStyleAttr, defStyleRes);
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
