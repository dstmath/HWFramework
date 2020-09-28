package huawei.android.widget;

import android.annotation.SuppressLint;
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
    private final Object mLock;
    private boolean mNeedUpdate;
    private float mScale;
    private TextView mTime;
    private ImageView mTimeAxisImage;
    private String mTimeIs12Or24;
    private LinearLayout mViewGroup;
    private boolean misFirst;
    private int mode;

    public TimeAxisWidget(Context context2) {
        this(context2, null);
    }

    public TimeAxisWidget(Context context2, AttributeSet attrs) {
        this(context2, attrs, 0);
    }

    public TimeAxisWidget(Context context2, AttributeSet attrs, int defStyle) {
        super(context2, attrs, defStyle);
        this.cal = null;
        this.mode = 0;
        this.axisStyle = 0;
        this.mCurrentAxisStyle = 0;
        this.mScale = 1.0f;
        this.mLock = new Object();
        this.mNeedUpdate = true;
        this.context = context2;
        this.mScale = context2.getResources().getDisplayMetrics().density;
        this.mDateFormat = Settings.System.getString(context2.getContentResolver(), "date_format");
        this.mTimeIs12Or24 = Settings.System.getString(context2.getContentResolver(), "time_12_24");
        this.misFirst = true;
        this.mNeedUpdate = true;
        initTimeAxis();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        updateStyle();
    }

    private void initTimeAxis() {
        if (!isInEditMode()) {
            initRootView();
            this.mDateTimeView = (RelativeLayout) ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(34013269, (ViewGroup) null);
            int paddingend = dip2px(5);
            int paddingstart = dip2px(2);
            RelativeLayout.LayoutParams datetimeview = new RelativeLayout.LayoutParams(this.context.getResources().getDimensionPixelOffset(34472098), -1);
            RelativeLayout relativeLayout = this.mDateTimeView;
            if (relativeLayout != null) {
                relativeLayout.setPadding(paddingstart, 0, paddingend, 0);
                this.mDate = (TextView) this.mDateTimeView.findViewById(34603121);
                this.mTime = (TextView) this.mDateTimeView.findViewById(34603122);
                this.mAmPm = (TextView) this.mDateTimeView.findViewById(34603123);
            }
            addView(this.mDateTimeView, 0, datetimeview);
            this.mTimeAxisImage = new ImageView(this.context);
            this.mTimeAxisImage.setScaleType(ImageView.ScaleType.CENTER);
            this.mTimeAxisImage.setBackgroundResource(33751460);
            addView(this.mTimeAxisImage, 1, new LinearLayout.LayoutParams(-2, -1));
        }
    }

    private void initRootView() {
        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        TypedValue outValue = new TypedValue();
        this.context.getTheme().resolveAttribute(16842829, outValue, true);
        setMinimumHeight((int) outValue.getDimension(this.context.getResources().getDisplayMetrics()));
        setOrientation(0);
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
        String dateformat = Settings.System.getString(this.context.getContentResolver(), "date_format");
        if (this.mDateFormat != null || dateformat == null) {
            String str = this.mDateFormat;
            if (str == null || str.equals(dateformat)) {
                return false;
            }
            this.mDateFormat = dateformat;
            return true;
        }
        this.mDateFormat = dateformat;
        return true;
    }

    private boolean setting12Or24FormatChange() {
        String timeIs12Or24 = Settings.System.getString(this.context.getContentResolver(), "time_12_24");
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
        if (settingDateFormatChange() || setting12Or24FormatChange()) {
            this.mNeedUpdate = true;
        } else {
            this.mNeedUpdate = false;
        }
        Calendar calendar2 = this.cal;
        if (calendar2 == null || !calendar2.equals(calendar) || this.mNeedUpdate) {
            this.cal = calendar;
            updateStyle();
        }
    }

    public void setMode(int flag) {
        if (flag != 0 && flag != 1 && flag != 2 && flag != 3 && flag != 4) {
            Log.w(TAG, "The input flag of mode is not correct.");
        } else if (this.mode != flag) {
            this.mode = flag;
            updateStyle();
        }
    }

    public void setAxisStyle(int style) {
        if (style != 1 && style != 0) {
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
                this.mViewGroup.setOrientation(0);
                int paddingstart = dip2px(18);
                if (isRtlLocale()) {
                    this.mViewGroup.setPadding(0, 0, paddingstart, 0);
                } else {
                    this.mViewGroup.setPadding(paddingstart, 0, 0, 0);
                }
                LinearLayout.LayoutParams viewgroup = new LinearLayout.LayoutParams(-1, -2);
                viewgroup.gravity = 17;
                addView(this.mViewGroup, 2, viewgroup);
            }
            LinearLayout linearLayout = this.mViewGroup;
            if (linearLayout != null && linearLayout.getChildCount() > 0) {
                this.mViewGroup.removeAllViews();
            }
            this.contentView = customView;
            LinearLayout linearLayout2 = this.mViewGroup;
            if (linearLayout2 != null) {
                linearLayout2.addView(this.contentView);
            }
        }
    }

    public void clearContentView() {
        this.contentView = null;
        LinearLayout linearLayout = this.mViewGroup;
        if (linearLayout != null) {
            removeView(linearLayout);
            this.mViewGroup = null;
        }
    }

    private void setDateTime(Calendar cal2) {
        String timeStr;
        String mothDayStr;
        Calendar calendar = cal2 != null ? cal2 : Calendar.getInstance();
        if (isNeedFormatUpdate()) {
            this.dMothDay = getDigitMonthDayFormat(this.context);
            this.dTime = android.text.format.DateFormat.getTimeFormat(this.context);
        }
        this.dMothDay.setCalendar(calendar);
        if (this.mDate != null) {
            synchronized (this.mLock) {
                mothDayStr = this.dMothDay.format(calendar.getTime());
            }
            this.mDate.setText(mothDayStr);
        }
        this.dTime.setCalendar(calendar);
        synchronized (this.mLock) {
            timeStr = this.dTime.format(calendar.getTime());
        }
        if (android.text.format.DateFormat.is24HourFormat(this.context)) {
            TextView textView = this.mTime;
            if (textView != null) {
                textView.setText(timeStr);
                return;
            }
            return;
        }
        String timeStr2 = HwDateUtils.formatChinaDateTime(this.context, timeStr);
        int ampmStart = 0;
        int ampmEnd = 0;
        int digitStart = findFirstDigit(timeStr2);
        int digitEnd = findLastDigit(timeStr2) + 1;
        if (findFirstDigit(timeStr2) == 0) {
            ampmStart = findLastDigit(timeStr2) + 1;
            ampmEnd = timeStr2.length();
        }
        if (findLastDigit(timeStr2) == timeStr2.length() - 1) {
            ampmEnd = findFirstDigit(timeStr2);
        }
        if (this.mTime != null && this.mAmPm != null) {
            if (digitStart >= 0 && digitStart <= digitEnd && digitEnd <= timeStr2.length()) {
                this.mTime.setText(timeStr2.substring(digitStart, digitEnd).trim());
            }
            if (ampmStart >= 0 && ampmStart <= ampmEnd && ampmEnd <= timeStr2.length()) {
                this.mAmPm.setText(timeStr2.substring(ampmStart, ampmEnd).trim());
            }
        }
    }

    private void setDateTimeStyle() {
        TextView textView;
        TextView textView2;
        TextView textView3;
        TextView textView4;
        int i = this.mode;
        int i2 = 0;
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i == 4 && this.mTime != null && (textView4 = this.mDate) != null && this.mAmPm != null) {
                            textView4.setVisibility(0);
                            this.mTime.setVisibility(0);
                            TextView textView5 = this.mAmPm;
                            if (android.text.format.DateFormat.is24HourFormat(this.context)) {
                                i2 = 8;
                            }
                            textView5.setVisibility(i2);
                        }
                    } else if (this.mTime != null && (textView3 = this.mDate) != null && this.mAmPm != null) {
                        textView3.setVisibility(8);
                        this.mTime.setVisibility(8);
                        this.mAmPm.setVisibility(8);
                    }
                } else if (this.mTime != null && (textView2 = this.mDate) != null && this.mAmPm != null) {
                    textView2.setVisibility(8);
                    this.mTime.setVisibility(0);
                    if (android.text.format.DateFormat.is24HourFormat(this.context)) {
                        this.mAmPm.setVisibility(8);
                    } else {
                        this.mAmPm.setVisibility(0);
                    }
                }
            } else if (this.mTime != null && (textView = this.mDate) != null && this.mAmPm != null) {
                textView.setVisibility(0);
                this.mTime.setVisibility(8);
                this.mAmPm.setVisibility(8);
            }
        } else if (this.mTime != null && this.mDate != null && this.mAmPm != null) {
            if (isSameDate(this.cal)) {
                this.mDate.setVisibility(8);
                this.mTime.setVisibility(0);
                TextView textView6 = this.mAmPm;
                if (android.text.format.DateFormat.is24HourFormat(this.context)) {
                    i2 = 8;
                }
                textView6.setVisibility(i2);
                return;
            }
            this.mDate.setVisibility(0);
            this.mTime.setVisibility(8);
            this.mAmPm.setVisibility(8);
        }
    }

    private void setAxisStyle() {
        RelativeLayout relativeLayout;
        int i = this.axisStyle;
        if (i != this.mCurrentAxisStyle && (relativeLayout = this.mDateTimeView) != null && this.mTimeAxisImage != null) {
            if (1 == i) {
                relativeLayout.setGravity(48);
                this.mTimeAxisImage.setBackgroundResource(33751553);
            } else if (i == 0) {
                relativeLayout.setGravity(17);
                this.mTimeAxisImage.setBackgroundResource(33751460);
            }
            this.mCurrentAxisStyle = this.axisStyle;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStyle() {
        setDateTime(this.cal);
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
            TimeAxisWidget taw = new TimeAxisWidget(this.mContext);
            taw.mode = this.mMode;
            taw.axisStyle = this.mAxisStyle;
            taw.cal = this.mCal;
            taw.updateStyle();
            taw.setContent(this.mContentView);
            return taw;
        }

        public Builder setMode(int flag) {
            if (flag == 0 || flag == 1 || flag == 2 || flag == 3 || flag == 4) {
                this.mMode = flag;
                return this;
            }
            Log.w(TimeAxisWidget.TAG, "The input flag of mode is not correct.");
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

    private DateFormat getDigitMonthDayFormat(Context context2) {
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
                if (month < day) {
                    return String.format(template, "MM", "dd");
                }
                return String.format(template, "dd", "MM");
            }
        }
        return digitMonthDay;
    }

    private String getStringForDate(String dateStr) {
        StringBuilder sb = new StringBuilder();
        int length = dateStr.length();
        for (int i = 0; i < length; i++) {
            char c = dateStr.charAt(i);
            if (i == 0 || (i > 0 && c != dateStr.charAt(i - 1))) {
                if (c == 'M' || c == 'd') {
                    sb.append("%s");
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
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
        Calendar CalendarNow = Calendar.getInstance();
        if (calendar.get(1) == CalendarNow.get(1) && calendar.get(2) == CalendarNow.get(2) && calendar.get(5) == CalendarNow.get(5)) {
            return true;
        }
        return false;
    }
}
