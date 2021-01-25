package huawei.android.widget;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.text.format.HwDateUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeAxisWidget extends LinearLayout {
    public static final int DATE_TIME_VIEW_INDEX = 0;
    private static final int DATE_TIME_VIEW_PADDING_END = 5;
    private static final int DATE_TIME_VIEW_PADDING_START = 2;
    public static final int FLAG_DATE_TIME = 4;
    public static final int FLAG_DEFAULT = 0;
    public static final int FLAG_NONE_INFO = 3;
    public static final int FLAG_ONLY_DATE = 1;
    public static final int FLAG_ONLY_TIME = 2;
    private static final float FLOAT_STRONG_TO_INT_FACTOR = 0.5f;
    private static final int INVALID_INDEX = -1;
    private static final int PADDING_START = 18;
    public static final int STYLE_DEFAULT = 0;
    public static final int STYLE_GALLERY = 1;
    public static final String TAG = "TimeAxisWidget";
    public static final int TIME_AXIS_IMAGE_INDEX = 1;
    public static final int VIEW_GROUP_INDEX = 2;
    private TextView mAmPm;
    private int mAxisStyle;
    private Calendar mCalendar;
    private View mContentView;
    private Context mContext;
    private int mCurrentAxisStyle;
    private TextView mDate;
    private String mDateFormat;
    private RelativeLayout mDateTimeView;
    private boolean mIsFirst;
    private boolean mIsNeedUpdate;
    private final Object mLock;
    private int mMode;
    private DateFormat mMothDay;
    private float mScale;
    private DateFormat mTime;
    private ImageView mTimeAxisImage;
    private String mTimeIs12Or24;
    private TextView mTimeView;
    private LinearLayout mViewGroup;

    public TimeAxisWidget(Context context) {
        this(context, null);
    }

    public TimeAxisWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeAxisWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCalendar = null;
        this.mMode = 0;
        this.mAxisStyle = 0;
        this.mCurrentAxisStyle = 0;
        this.mScale = 1.0f;
        this.mLock = new Object();
        this.mIsNeedUpdate = true;
        this.mContext = context;
        this.mScale = context.getResources().getDisplayMetrics().density;
        this.mDateFormat = Settings.System.getString(context.getContentResolver(), "date_format");
        this.mTimeIs12Or24 = Settings.System.getString(context.getContentResolver(), "time_12_24");
        this.mIsFirst = true;
        this.mIsNeedUpdate = true;
        initTimeAxis();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        updateStyle();
    }

    private void initTimeAxis() {
        if (!isInEditMode()) {
            initRootView();
            Object object = getContext().getSystemService("layout_inflater");
            if (!(object instanceof LayoutInflater)) {
                Log.w(TAG, "getSystemService(Context.LAYOUT_INFLATER_SERVICE) return null or Non LayoutInflater!");
                return;
            }
            View view = ((LayoutInflater) object).inflate(34013269, (ViewGroup) null);
            if (view instanceof RelativeLayout) {
                this.mDateTimeView = (RelativeLayout) view;
                int paddingEnd = dip2px(5);
                this.mDateTimeView.setPadding(dip2px(2), 0, paddingEnd, 0);
                this.mDate = (TextView) this.mDateTimeView.findViewById(34603121);
                this.mTimeView = (TextView) this.mDateTimeView.findViewById(34603122);
                this.mAmPm = (TextView) this.mDateTimeView.findViewById(34603123);
                addView(this.mDateTimeView, 0, new RelativeLayout.LayoutParams(this.mContext.getResources().getDimensionPixelOffset(34472098), -1));
                this.mTimeAxisImage = new ImageView(this.mContext);
                this.mTimeAxisImage.setScaleType(ImageView.ScaleType.CENTER);
                this.mTimeAxisImage.setBackgroundResource(33751460);
                addView(this.mTimeAxisImage, 1, new LinearLayout.LayoutParams(-2, -1));
            }
        }
    }

    private void initRootView() {
        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        TypedValue outValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16842829, outValue, true);
        setMinimumHeight((int) outValue.getDimension(this.mContext.getResources().getDisplayMetrics()));
        setOrientation(0);
        this.mContext.getTheme().resolveAttribute(16843534, outValue, true);
        setBackgroundResource(outValue.resourceId);
    }

    private int dip2px(int dpValue) {
        return (int) ((((float) dpValue) * this.mScale) + 0.5f);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TimeAxisWidget.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TimeAxisWidget.class.getName());
    }

    public TextView getDate() {
        return this.mDate;
    }

    public TextView getTime() {
        return this.mTimeView;
    }

    public TextView getAmPm() {
        return this.mAmPm;
    }

    public int getMode() {
        return this.mMode;
    }

    public int getAxisStyle() {
        return this.mAxisStyle;
    }

    public View getContent() {
        return this.mContentView;
    }

    private boolean settingDateFormatChange() {
        String dateFormat = Settings.System.getString(this.mContext.getContentResolver(), "date_format");
        if (this.mDateFormat != null || dateFormat == null) {
            String str = this.mDateFormat;
            if (str == null || str.equals(dateFormat)) {
                return false;
            }
            this.mDateFormat = dateFormat;
            return true;
        }
        this.mDateFormat = dateFormat;
        return true;
    }

    private boolean setting12Or24FormatChange() {
        String timeIs12Or24 = Settings.System.getString(this.mContext.getContentResolver(), "time_12_24");
        if (this.mTimeIs12Or24 != null || timeIs12Or24 == null) {
            String str = this.mTimeIs12Or24;
            if (str == null || str.equals(timeIs12Or24)) {
                return false;
            }
            this.mTimeIs12Or24 = timeIs12Or24;
            return true;
        }
        this.mTimeIs12Or24 = timeIs12Or24;
        return true;
    }

    public void setCalendar(Calendar calendar) {
        this.mIsNeedUpdate = settingDateFormatChange() || setting12Or24FormatChange();
        Calendar calendar2 = this.mCalendar;
        if (calendar2 == null || !calendar2.equals(calendar) || this.mIsNeedUpdate) {
            this.mCalendar = calendar;
            updateStyle();
        }
    }

    public void setMode(int flag) {
        boolean isInvalidFlag = false;
        if (!(!((flag == 0 || flag == 1 || flag == 2) ? false : true) || flag == 3 || flag == 4)) {
            isInvalidFlag = true;
        }
        if (isInvalidFlag) {
            Log.w(TAG, "The input flag of mMode is not correct.");
        } else if (this.mMode != flag) {
            this.mMode = flag;
            updateStyle();
        }
    }

    public void setAxisStyle(int style) {
        if (style != 1 && style != 0) {
            Log.w(TAG, "The axis style must be either STYLE_DEFAULT or STYLE_GALLERY.");
        } else if (this.mAxisStyle != style) {
            this.mAxisStyle = style;
            updateStyle();
        }
    }

    private void setAxisStyle() {
        RelativeLayout relativeLayout;
        int i = this.mAxisStyle;
        if (i != this.mCurrentAxisStyle && (relativeLayout = this.mDateTimeView) != null && this.mTimeAxisImage != null) {
            if (i == 1) {
                relativeLayout.setGravity(48);
                this.mTimeAxisImage.setBackgroundResource(33751553);
            } else if (i == 0) {
                relativeLayout.setGravity(17);
                this.mTimeAxisImage.setBackgroundResource(33751460);
            } else {
                Log.w(TAG, "invalid style");
            }
            this.mCurrentAxisStyle = this.mAxisStyle;
        }
    }

    public void setContent(View customView) {
        if (customView != null) {
            if (this.mContentView == null) {
                this.mViewGroup = new LinearLayout(this.mContext);
                this.mViewGroup.setOrientation(0);
                int paddingStart = dip2px(18);
                if (isRtlLocale()) {
                    this.mViewGroup.setPadding(0, 0, paddingStart, 0);
                } else {
                    this.mViewGroup.setPadding(paddingStart, 0, 0, 0);
                }
                LinearLayout.LayoutParams viewGroup = new LinearLayout.LayoutParams(-1, -2);
                viewGroup.gravity = 17;
                addView(this.mViewGroup, 2, viewGroup);
            }
            LinearLayout linearLayout = this.mViewGroup;
            if (linearLayout != null && linearLayout.getChildCount() > 0) {
                this.mViewGroup.removeAllViews();
            }
            this.mContentView = customView;
            LinearLayout linearLayout2 = this.mViewGroup;
            if (linearLayout2 != null) {
                linearLayout2.addView(this.mContentView);
            }
        }
    }

    public void clearContentView() {
        this.mContentView = null;
        LinearLayout linearLayout = this.mViewGroup;
        if (linearLayout != null) {
            removeView(linearLayout);
            this.mViewGroup = null;
        }
    }

    private void setDateTime(Calendar cal) {
        String timeStr;
        String mothDayStr;
        Calendar calendar = cal != null ? cal : Calendar.getInstance();
        if (isNeedFormatUpdate()) {
            this.mMothDay = getDigitMonthDayFormat();
            this.mTime = android.text.format.DateFormat.getTimeFormat(this.mContext);
        }
        this.mMothDay.setCalendar(calendar);
        if (this.mDate != null) {
            synchronized (this.mLock) {
                mothDayStr = this.mMothDay.format(calendar.getTime());
            }
            this.mDate.setText(mothDayStr);
        }
        this.mTime.setCalendar(calendar);
        synchronized (this.mLock) {
            timeStr = this.mTime.format(calendar.getTime());
        }
        if (android.text.format.DateFormat.is24HourFormat(this.mContext)) {
            TextView textView = this.mTimeView;
            if (textView != null) {
                textView.setText(timeStr);
                return;
            }
            return;
        }
        String timeStr2 = HwDateUtils.formatChinaDateTime(this.mContext, timeStr);
        int amPmStart = 0;
        int amPmEnd = 0;
        int digitStart = findFirstDigit(timeStr2);
        int digitEnd = findLastDigit(timeStr2) + 1;
        if (findFirstDigit(timeStr2) == 0) {
            amPmStart = findLastDigit(timeStr2) + 1;
            amPmEnd = timeStr2.length();
        }
        if (findLastDigit(timeStr2) == timeStr2.length() - 1) {
            amPmEnd = findFirstDigit(timeStr2);
        }
        if (this.mTimeView != null && this.mAmPm != null) {
            if (digitStart >= 0 && digitStart <= digitEnd && digitEnd <= timeStr2.length()) {
                this.mTimeView.setText(timeStr2.substring(digitStart, digitEnd).trim());
            }
            if (amPmStart >= 0 && amPmStart <= amPmEnd && amPmEnd <= timeStr2.length()) {
                this.mAmPm.setText(timeStr2.substring(amPmStart, amPmEnd).trim());
            }
        }
    }

    private void setDateTimeStyle() {
        TextView textView;
        if (this.mTimeView != null && (textView = this.mDate) != null && this.mAmPm != null) {
            int i = this.mMode;
            int i2 = 0;
            if (i != 0) {
                if (i == 1) {
                    textView.setVisibility(0);
                    this.mTimeView.setVisibility(8);
                    this.mAmPm.setVisibility(8);
                } else if (i == 2) {
                    textView.setVisibility(8);
                    this.mTimeView.setVisibility(0);
                    if (android.text.format.DateFormat.is24HourFormat(this.mContext)) {
                        this.mAmPm.setVisibility(8);
                    } else {
                        this.mAmPm.setVisibility(0);
                    }
                } else if (i == 3) {
                    textView.setVisibility(8);
                    this.mTimeView.setVisibility(8);
                    this.mAmPm.setVisibility(8);
                } else if (i == 4) {
                    textView.setVisibility(0);
                    this.mTimeView.setVisibility(0);
                    TextView textView2 = this.mAmPm;
                    if (android.text.format.DateFormat.is24HourFormat(this.mContext)) {
                        i2 = 8;
                    }
                    textView2.setVisibility(i2);
                }
            } else if (isSameDate(this.mCalendar)) {
                this.mDate.setVisibility(8);
                this.mTimeView.setVisibility(0);
                TextView textView3 = this.mAmPm;
                if (android.text.format.DateFormat.is24HourFormat(this.mContext)) {
                    i2 = 8;
                }
                textView3.setVisibility(i2);
            } else {
                this.mDate.setVisibility(0);
                this.mTimeView.setVisibility(8);
                this.mAmPm.setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStyle() {
        setDateTime(this.mCalendar);
        setDateTimeStyle();
        setAxisStyle();
    }

    public static class Builder {
        private int mAxisStyle = 0;
        private Calendar mCal = null;
        private View mContentView;
        private Context mContext;
        private int mMode = 0;

        public Builder(Context context) {
            this.mContext = context;
        }

        public TimeAxisWidget build() {
            TimeAxisWidget timeAxisWidget = new TimeAxisWidget(this.mContext);
            timeAxisWidget.mMode = this.mMode;
            timeAxisWidget.mAxisStyle = this.mAxisStyle;
            timeAxisWidget.mCalendar = this.mCal;
            timeAxisWidget.updateStyle();
            timeAxisWidget.setContent(this.mContentView);
            return timeAxisWidget;
        }

        public Builder setMode(int flag) {
            boolean isInvalidFlag = false;
            if (!(!((flag == 0 || flag == 1 || flag == 2) ? false : true) || flag == 3 || flag == 4)) {
                isInvalidFlag = true;
            }
            if (isInvalidFlag) {
                Log.w(TimeAxisWidget.TAG, "The input flag of mode is not correct.");
                return this;
            }
            this.mMode = flag;
            return this;
        }

        public Builder setAxisStyle(int style) {
            if (style == 1 || style == 0) {
                this.mAxisStyle = style;
                return this;
            }
            Log.w(TimeAxisWidget.TAG, "The axis style must be either STYLE_DEFAULT or STYLE_GALLERY.");
            return this;
        }

        public Builder setCalendar(Calendar cal) {
            this.mCal = cal;
            return this;
        }

        public Builder setContent(View view) {
            this.mContentView = view;
            return this;
        }
    }

    private boolean isNeedFormatUpdate() {
        if (this.mIsFirst) {
            this.mIsFirst = false;
            return true;
        } else if (this.mIsNeedUpdate) {
            return true;
        } else {
            this.mIsNeedUpdate = true;
            return false;
        }
    }

    private DateFormat getDigitMonthDayFormat() {
        return getDigitMonthDayFormatForSetting(Locale.getDefault(), this.mDateFormat);
    }

    private DateFormat getDigitMonthDayFormatForSetting(Locale locale, String value) {
        return new SimpleDateFormat(getDigitMonthDay(locale, value), locale);
    }

    private String getDigitMonthDay(Locale locale, String value) {
        String digitMonthDay = android.text.format.DateFormat.getBestDateTimePattern(locale, "Md");
        if (value != null) {
            int month = value.indexOf(77);
            int day = value.indexOf(100);
            if (month >= 0 && day >= 0) {
                String template = getStringForDate(digitMonthDay);
                if (month < day) {
                    return String.format(template, "MM", "dd");
                }
                return String.format(template, "dd", "MM");
            }
        }
        return digitMonthDay;
    }

    private String getStringForDate(String dateStr) {
        StringBuilder stringBuilder = new StringBuilder();
        int length = dateStr.length();
        for (int i = 0; i < length; i++) {
            char chr = dateStr.charAt(i);
            if (i == 0 || chr != dateStr.charAt(i - 1)) {
                if (chr == 'M' || chr == 'd') {
                    stringBuilder.append("%s");
                } else {
                    stringBuilder.append(chr);
                }
            }
        }
        return stringBuilder.toString();
    }

    private int findFirstDigit(String timeStr) {
        int length = timeStr.length();
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(timeStr.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findLastDigit(String timeStr) {
        int index = -1;
        int length = timeStr.length();
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(timeStr.charAt(i))) {
                index = i;
            }
        }
        return index;
    }

    private boolean isSameDate(Calendar calendar) {
        if (calendar == null) {
            return true;
        }
        Calendar calendarNow = Calendar.getInstance();
        if (calendar.get(1) == calendarNow.get(1) && calendar.get(2) == calendarNow.get(2) && calendar.get(5) == calendarNow.get(5)) {
            return true;
        }
        return false;
    }
}
