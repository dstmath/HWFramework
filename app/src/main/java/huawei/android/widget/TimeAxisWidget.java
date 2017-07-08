package huawei.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import huawei.android.text.format.HwDateUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeAxisWidget extends LinearLayout {
    public static final int DATE_TIME_VIEW_INDEX = 0;
    public static final int FLAG_DATE_TIME = 4;
    public static final int FLAG_DEFAULT = 0;
    public static final int FLAG_NONE_INFO = 3;
    public static final int FLAG_ONLY_DATE = 1;
    public static final int FLAG_ONLY_TIME = 2;
    public static final int STYLE_DEFAULT = 0;
    public static final int STYLE_GALLERY = 1;
    public static final String TAG = "TimeAxisWidget";
    public static final int TIME_AXIS_IMAGE_INDEX = 1;
    public static final int VIEW_GROUP_INDEX = 2;
    private int axisStyle;
    private Calendar cal;
    private View contentView;
    private Context context;
    private DateFormat dMothDay;
    private DateFormat dTime;
    private TextView mAmPm;
    private int mCurrentAxisStyle;
    private TextView mDate;
    private String mDateFormat;
    private RelativeLayout mDateTimeView;
    private boolean mNeedUpdate;
    private float mScale;
    private TextView mTime;
    private ImageView mTimeAxisImage;
    private String mTimeIs12Or24;
    private LinearLayout mViewGroup;
    private boolean misFirst;
    private int mode;

    public static class Builder {
        private int mAxisStyle;
        private Calendar mCal;
        private View mContentView;
        private Context mContext;
        private int mMode;

        public Builder(Context context) {
            this.mCal = null;
            this.mMode = TimeAxisWidget.STYLE_DEFAULT;
            this.mAxisStyle = TimeAxisWidget.STYLE_DEFAULT;
            this.mContext = context;
        }

        public TimeAxisWidget build() {
            TimeAxisWidget taw = new TimeAxisWidget(this.mContext);
            taw.mode = this.mMode;
            taw.axisStyle = this.mAxisStyle;
            taw.cal = this.mCal;
            taw.updateStyle();
            taw.setContent(this.mContentView);
            return taw;
        }

        public Builder setMode(int flag) {
            if (flag == 0 || flag == TimeAxisWidget.TIME_AXIS_IMAGE_INDEX || flag == TimeAxisWidget.VIEW_GROUP_INDEX || flag == TimeAxisWidget.FLAG_NONE_INFO || flag == TimeAxisWidget.FLAG_DATE_TIME) {
                this.mMode = flag;
                return this;
            }
            Log.w(TimeAxisWidget.TAG, "The input flag of mode is not correct.");
            return this;
        }

        public Builder setAxisStyle(int style) {
            if (style == TimeAxisWidget.TIME_AXIS_IMAGE_INDEX || style == 0) {
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

    public TimeAxisWidget(Context context) {
        this(context, null);
    }

    public TimeAxisWidget(Context context, AttributeSet attrs) {
        this(context, attrs, STYLE_DEFAULT);
    }

    public TimeAxisWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.cal = null;
        this.mode = STYLE_DEFAULT;
        this.axisStyle = STYLE_DEFAULT;
        this.mCurrentAxisStyle = STYLE_DEFAULT;
        this.mScale = 1.0f;
        this.mNeedUpdate = true;
        this.context = context;
        this.mScale = context.getResources().getDisplayMetrics().density;
        this.mDateFormat = System.getString(context.getContentResolver(), "date_format");
        this.mTimeIs12Or24 = System.getString(context.getContentResolver(), "time_12_24");
        this.misFirst = true;
        this.mNeedUpdate = true;
        initTimeAxis();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        updateStyle();
    }

    private void initTimeAxis() {
        if (!isInEditMode()) {
            initRootView();
            this.mDateTimeView = (RelativeLayout) ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(34013269, null);
            int paddingend = dip2px(5);
            int paddingstart = dip2px(VIEW_GROUP_INDEX);
            LayoutParams datetimeview = new LayoutParams(this.context.getResources().getDimensionPixelOffset(34472107), -1);
            if (this.mDateTimeView != null) {
                this.mDateTimeView.setPadding(paddingstart, STYLE_DEFAULT, paddingend, STYLE_DEFAULT);
                this.mDate = (TextView) this.mDateTimeView.findViewById(34603198);
                this.mTime = (TextView) this.mDateTimeView.findViewById(34603199);
                this.mAmPm = (TextView) this.mDateTimeView.findViewById(34603200);
                if (this.mAmPm != null && Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
                    this.mAmPm.setTypeface(Typeface.CHNFZSLIM);
                }
            }
            addView(this.mDateTimeView, STYLE_DEFAULT, datetimeview);
            this.mTimeAxisImage = new ImageView(this.context);
            this.mTimeAxisImage.setScaleType(ScaleType.CENTER);
            this.mTimeAxisImage.setBackgroundResource(33751402);
            addView(this.mTimeAxisImage, TIME_AXIS_IMAGE_INDEX, new LinearLayout.LayoutParams(-2, -1));
        }
    }

    private void initRootView() {
        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        TypedValue outValue = new TypedValue();
        this.context.getTheme().resolveAttribute(16842829, outValue, true);
        setMinimumHeight((int) outValue.getDimension(this.context.getResources().getDisplayMetrics()));
        setOrientation(STYLE_DEFAULT);
        this.context.getTheme().resolveAttribute(16843534, outValue, true);
        setBackgroundResource(outValue.resourceId);
    }

    private int dip2px(int dpValue) {
        return (int) ((((float) dpValue) * this.mScale) + 0.5f);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TimeAxisWidget.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TimeAxisWidget.class.getName());
    }

    public TextView getDate() {
        return this.mDate;
    }

    public TextView getTime() {
        return this.mTime;
    }

    public TextView getAmPm() {
        return this.mAmPm;
    }

    public int getMode() {
        return this.mode;
    }

    public int getAxisStyle() {
        return this.axisStyle;
    }

    public View getContent() {
        return this.contentView;
    }

    private boolean settingDateFormatChange() {
        String dateformat = System.getString(this.context.getContentResolver(), "date_format");
        if (this.mDateFormat == null && dateformat != null) {
            this.mDateFormat = dateformat;
            return true;
        } else if (this.mDateFormat == null || this.mDateFormat.equals(dateformat)) {
            return false;
        } else {
            this.mDateFormat = dateformat;
            return true;
        }
    }

    private boolean setting12Or24FormatChange() {
        String timeIs12Or24 = System.getString(this.context.getContentResolver(), "time_12_24");
        if (this.mTimeIs12Or24 == null && timeIs12Or24 != null) {
            this.mTimeIs12Or24 = timeIs12Or24;
            return true;
        } else if (this.mTimeIs12Or24 == null || this.mTimeIs12Or24.equals(timeIs12Or24)) {
            return false;
        } else {
            this.mTimeIs12Or24 = timeIs12Or24;
            return true;
        }
    }

    public void setCalendar(Calendar calendar) {
        if (settingDateFormatChange() || setting12Or24FormatChange()) {
            this.mNeedUpdate = true;
        } else {
            this.mNeedUpdate = false;
        }
        if (this.cal == null || !this.cal.equals(calendar) || this.mNeedUpdate) {
            this.cal = calendar;
            updateStyle();
        }
    }

    public void setMode(int flag) {
        if (flag != 0 && flag != TIME_AXIS_IMAGE_INDEX && flag != VIEW_GROUP_INDEX && flag != FLAG_NONE_INFO && flag != FLAG_DATE_TIME) {
            Log.w(TAG, "The input flag of mode is not correct.");
        } else if (this.mode != flag) {
            this.mode = flag;
            updateStyle();
        }
    }

    public void setAxisStyle(int style) {
        if (style != TIME_AXIS_IMAGE_INDEX && style != 0) {
            Log.w(TAG, "The axis style must be either STYLE_DEFAULT or STYLE_GALLERY.");
        } else if (this.axisStyle != style) {
            this.axisStyle = style;
            updateStyle();
        }
    }

    public void setContent(View customView) {
        if (customView != null) {
            if (this.contentView == null) {
                this.mViewGroup = new LinearLayout(this.context);
                this.mViewGroup.setOrientation(STYLE_DEFAULT);
                int paddingstart = dip2px(18);
                if (isRtlLocale()) {
                    this.mViewGroup.setPadding(STYLE_DEFAULT, STYLE_DEFAULT, paddingstart, STYLE_DEFAULT);
                } else {
                    this.mViewGroup.setPadding(paddingstart, STYLE_DEFAULT, STYLE_DEFAULT, STYLE_DEFAULT);
                }
                LinearLayout.LayoutParams viewgroup = new LinearLayout.LayoutParams(-1, -2);
                viewgroup.gravity = 17;
                addView(this.mViewGroup, VIEW_GROUP_INDEX, viewgroup);
            }
            if (this.mViewGroup != null && this.mViewGroup.getChildCount() > 0) {
                this.mViewGroup.removeAllViews();
            }
            this.contentView = customView;
            if (this.mViewGroup != null) {
                this.mViewGroup.addView(this.contentView);
            }
        }
    }

    public void clearContentView() {
        this.contentView = null;
        if (this.mViewGroup != null) {
            removeView(this.mViewGroup);
            this.mViewGroup = null;
        }
    }

    private void setDateTime(Calendar cal) {
        Calendar calendar = cal != null ? cal : Calendar.getInstance();
        if (isNeedFormatUpdate()) {
            this.dMothDay = getDigitMonthDayFormat(this.context);
            this.dTime = android.text.format.DateFormat.getTimeFormat(this.context);
        }
        this.dMothDay.setCalendar(calendar);
        if (this.mDate != null) {
            this.mDate.setText(this.dMothDay.format(calendar.getTime()));
        }
        this.dTime.setCalendar(calendar);
        String timeStr = this.dTime.format(calendar.getTime());
        if (!android.text.format.DateFormat.is24HourFormat(this.context)) {
            timeStr = HwDateUtils.formatChinaDateTime(this.context, timeStr);
            int ampmStart = STYLE_DEFAULT;
            int ampmEnd = STYLE_DEFAULT;
            int digitStart = findFirstDigit(timeStr);
            int digitEnd = findLastDigit(timeStr) + TIME_AXIS_IMAGE_INDEX;
            if (findFirstDigit(timeStr) == 0) {
                ampmStart = findLastDigit(timeStr) + TIME_AXIS_IMAGE_INDEX;
                ampmEnd = timeStr.length();
            }
            if (findLastDigit(timeStr) == timeStr.length() - 1) {
                ampmEnd = findFirstDigit(timeStr);
            }
            if (this.mTime != null && this.mAmPm != null) {
                if (digitStart >= 0 && digitStart <= digitEnd && digitEnd <= timeStr.length()) {
                    this.mTime.setText(timeStr.substring(digitStart, digitEnd).trim());
                }
                if (ampmStart >= 0 && ampmStart <= ampmEnd && ampmEnd <= timeStr.length()) {
                    this.mAmPm.setText(timeStr.substring(ampmStart, ampmEnd).trim());
                }
            }
        } else if (this.mTime != null) {
            this.mTime.setText(timeStr);
        }
    }

    private void setDateTimeStyle() {
        int i = 8;
        TextView textView;
        switch (this.mode) {
            case STYLE_DEFAULT /*0*/:
                if (this.mTime != null && this.mDate != null && this.mAmPm != null) {
                    if (isSameDate(this.cal)) {
                        this.mDate.setVisibility(8);
                        this.mTime.setVisibility(STYLE_DEFAULT);
                        textView = this.mAmPm;
                        if (!android.text.format.DateFormat.is24HourFormat(this.context)) {
                            i = STYLE_DEFAULT;
                        }
                        textView.setVisibility(i);
                        return;
                    }
                    this.mDate.setVisibility(STYLE_DEFAULT);
                    this.mTime.setVisibility(8);
                    this.mAmPm.setVisibility(8);
                }
            case TIME_AXIS_IMAGE_INDEX /*1*/:
                if (this.mTime != null && this.mDate != null && this.mAmPm != null) {
                    this.mDate.setVisibility(STYLE_DEFAULT);
                    this.mTime.setVisibility(8);
                    this.mAmPm.setVisibility(8);
                }
            case VIEW_GROUP_INDEX /*2*/:
                if (this.mTime != null && this.mDate != null && this.mAmPm != null) {
                    this.mDate.setVisibility(8);
                    this.mTime.setVisibility(STYLE_DEFAULT);
                    if (android.text.format.DateFormat.is24HourFormat(this.context)) {
                        this.mAmPm.setVisibility(8);
                    } else {
                        this.mAmPm.setVisibility(STYLE_DEFAULT);
                    }
                }
            case FLAG_NONE_INFO /*3*/:
                if (this.mTime != null && this.mDate != null && this.mAmPm != null) {
                    this.mDate.setVisibility(8);
                    this.mTime.setVisibility(8);
                    this.mAmPm.setVisibility(8);
                }
            case FLAG_DATE_TIME /*4*/:
                if (this.mTime != null && this.mDate != null && this.mAmPm != null) {
                    this.mDate.setVisibility(STYLE_DEFAULT);
                    this.mTime.setVisibility(STYLE_DEFAULT);
                    textView = this.mAmPm;
                    if (!android.text.format.DateFormat.is24HourFormat(this.context)) {
                        i = STYLE_DEFAULT;
                    }
                    textView.setVisibility(i);
                }
            default:
        }
    }

    private void setAxisStyle() {
        if (this.axisStyle != this.mCurrentAxisStyle && this.mDateTimeView != null && this.mTimeAxisImage != null) {
            if (TIME_AXIS_IMAGE_INDEX == this.axisStyle) {
                this.mDateTimeView.setGravity(48);
                this.mTimeAxisImage.setBackgroundResource(33751403);
            } else if (this.axisStyle == 0) {
                this.mDateTimeView.setGravity(17);
                this.mTimeAxisImage.setBackgroundResource(33751402);
            }
            this.mCurrentAxisStyle = this.axisStyle;
        }
    }

    private void updateStyle() {
        setDateTime(this.cal);
        setDateTimeStyle();
        setAxisStyle();
    }

    private boolean isNeedFormatUpdate() {
        if (this.misFirst) {
            this.misFirst = false;
            return true;
        } else if (this.mNeedUpdate) {
            return true;
        } else {
            this.mNeedUpdate = true;
            return false;
        }
    }

    private DateFormat getDigitMonthDayFormat(Context context) {
        return getDigitMonthDayFormatForSetting(Locale.getDefault(), this.mDateFormat);
    }

    private DateFormat getDigitMonthDayFormatForSetting(Locale locale, String value) {
        return new SimpleDateFormat(getDigitMonthDay(locale, value), locale);
    }

    @SuppressLint({"NewApi"})
    private String getDigitMonthDay(Locale locale, String value) {
        String digitMonthDay = android.text.format.DateFormat.getBestDateTimePattern(locale, "Md");
        if (value != null) {
            int month = value.indexOf(77);
            int day = value.indexOf(100);
            if (month >= 0 && day >= 0) {
                String template = getStringForDate(digitMonthDay);
                Object[] objArr;
                if (month < day) {
                    objArr = new Object[VIEW_GROUP_INDEX];
                    objArr[STYLE_DEFAULT] = "MM";
                    objArr[TIME_AXIS_IMAGE_INDEX] = "dd";
                    value = String.format(template, objArr);
                } else {
                    objArr = new Object[VIEW_GROUP_INDEX];
                    objArr[STYLE_DEFAULT] = "dd";
                    objArr[TIME_AXIS_IMAGE_INDEX] = "MM";
                    value = String.format(template, objArr);
                }
                return value;
            }
        }
        return digitMonthDay;
    }

    private String getStringForDate(String dateStr) {
        StringBuilder sb = new StringBuilder();
        int i = STYLE_DEFAULT;
        while (i < dateStr.length()) {
            char c = dateStr.charAt(i);
            if (i == 0 || (i > 0 && c != dateStr.charAt(i - 1))) {
                if (c == 'M' || c == 'd') {
                    sb.append("%s");
                } else {
                    sb.append(c);
                }
            }
            i += TIME_AXIS_IMAGE_INDEX;
        }
        return sb.toString();
    }

    private int findFirstDigit(String timeStr) {
        for (int i = STYLE_DEFAULT; i < timeStr.length(); i += TIME_AXIS_IMAGE_INDEX) {
            if (Character.isDigit(timeStr.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findLastDigit(String timeStr) {
        int index = -1;
        for (int i = STYLE_DEFAULT; i < timeStr.length(); i += TIME_AXIS_IMAGE_INDEX) {
            if (Character.isDigit(timeStr.charAt(i))) {
                index = i;
            }
        }
        return index;
    }

    private boolean isSameDate(Calendar calendar) {
        boolean z = true;
        if (calendar == null) {
            return true;
        }
        Calendar CalendarNow = Calendar.getInstance();
        if (calendar.get(TIME_AXIS_IMAGE_INDEX) != CalendarNow.get(TIME_AXIS_IMAGE_INDEX) || calendar.get(VIEW_GROUP_INDEX) != CalendarNow.get(VIEW_GROUP_INDEX)) {
            z = false;
        } else if (calendar.get(5) != CalendarNow.get(5)) {
            z = false;
        }
        return z;
    }
}
