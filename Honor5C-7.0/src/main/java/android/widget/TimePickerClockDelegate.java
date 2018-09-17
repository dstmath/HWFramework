package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.TtsSpan.VerbatimBuilder;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.RadialTimePickerView.OnValueSelectedListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TimePicker.OnTimeChangedListener;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.util.AsyncService;
import com.android.internal.widget.NumericTextView;
import com.android.internal.widget.NumericTextView.OnValueChangedListener;
import com.huawei.pgmng.log.LogPower;
import java.util.Calendar;

class TimePickerClockDelegate extends AbstractTimePickerDelegate {
    private static final int AM = 0;
    private static final int AMPM_INDEX = 2;
    private static final int[] ATTRS_DISABLED_ALPHA = null;
    private static final int[] ATTRS_TEXT_COLOR = null;
    private static final long DELAY_COMMIT_MILLIS = 2000;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOUR_INDEX = 0;
    private static final int MINUTE_INDEX = 1;
    private static final int PM = 1;
    private boolean mAllowAutoAdvance;
    private final RadioButton mAmLabel;
    private final View mAmPmLayout;
    private final OnClickListener mClickListener;
    private final Runnable mCommitHour;
    private final Runnable mCommitMinute;
    private int mCurrentHour;
    private int mCurrentMinute;
    private final OnValueChangedListener mDigitEnteredListener;
    private final OnFocusChangeListener mFocusListener;
    private boolean mHourFormatShowLeadingZero;
    private boolean mHourFormatStartsAtZero;
    private final NumericTextView mHourView;
    private boolean mIs24Hour;
    private boolean mIsAmPmAtStart;
    private boolean mIsEnabled;
    private boolean mLastAnnouncedIsHour;
    private CharSequence mLastAnnouncedText;
    private final NumericTextView mMinuteView;
    private final OnValueSelectedListener mOnValueSelectedListener;
    private final RadioButton mPmLabel;
    private final RadialTimePickerView mRadialTimePickerView;
    private final String mSelectHours;
    private final String mSelectMinutes;
    private final TextView mSeparatorView;
    private final Calendar mTempCalendar;

    private static class ClickActionDelegate extends AccessibilityDelegate {
        private final AccessibilityAction mClickAction;

        public ClickActionDelegate(Context context, int resId) {
            this.mClickAction = new AccessibilityAction(16, context.getString(resId));
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(this.mClickAction);
        }
    }

    private static class NearestTouchDelegate implements OnTouchListener {
        private View mInitialTouchTarget;

        private NearestTouchDelegate() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                if (view instanceof ViewGroup) {
                    this.mInitialTouchTarget = findNearestChild((ViewGroup) view, (int) motionEvent.getX(), (int) motionEvent.getY());
                } else {
                    this.mInitialTouchTarget = null;
                }
            }
            View child = this.mInitialTouchTarget;
            if (child == null) {
                return false;
            }
            float offsetX = (float) (view.getScrollX() - child.getLeft());
            float offsetY = (float) (view.getScrollY() - child.getTop());
            motionEvent.offsetLocation(offsetX, offsetY);
            boolean handled = child.dispatchTouchEvent(motionEvent);
            motionEvent.offsetLocation(-offsetX, -offsetY);
            if (actionMasked == TimePickerClockDelegate.PM || actionMasked == 3) {
                this.mInitialTouchTarget = null;
            }
            return handled;
        }

        private View findNearestChild(ViewGroup v, int x, int y) {
            View bestChild = null;
            int bestDist = HwBootFail.STAGE_BOOT_SUCCESS;
            int count = v.getChildCount();
            for (int i = TimePickerClockDelegate.HOUR_INDEX; i < count; i += TimePickerClockDelegate.PM) {
                View child = v.getChildAt(i);
                int dX = x - (child.getLeft() + (child.getWidth() / TimePickerClockDelegate.AMPM_INDEX));
                int dY = y - (child.getTop() + (child.getHeight() / TimePickerClockDelegate.AMPM_INDEX));
                int dist = (dX * dX) + (dY * dY);
                if (bestDist > dist) {
                    bestChild = child;
                    bestDist = dist;
                }
            }
            return bestChild;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.TimePickerClockDelegate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.TimePickerClockDelegate.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePickerClockDelegate.<clinit>():void");
    }

    public TimePickerClockDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        this.mIsEnabled = true;
        this.mOnValueSelectedListener = new OnValueSelectedListener() {
            public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
                switch (pickerIndex) {
                    case TimePickerClockDelegate.HOUR_INDEX /*0*/:
                        boolean z;
                        boolean z2 = TimePickerClockDelegate.this.mAllowAutoAdvance ? autoAdvance : false;
                        TimePickerClockDelegate timePickerClockDelegate = TimePickerClockDelegate.this;
                        if (z2) {
                            z = false;
                        } else {
                            z = true;
                        }
                        timePickerClockDelegate.setHourInternal(newValue, true, z);
                        if (z2) {
                            TimePickerClockDelegate.this.setCurrentItemShowing(TimePickerClockDelegate.PM, true, false);
                            TimePickerClockDelegate.this.mDelegator.announceForAccessibility(newValue + ". " + TimePickerClockDelegate.this.mSelectMinutes);
                            break;
                        }
                        break;
                    case TimePickerClockDelegate.PM /*1*/:
                        TimePickerClockDelegate.this.setMinuteInternal(newValue, true);
                        break;
                    case TimePickerClockDelegate.AMPM_INDEX /*2*/:
                        TimePickerClockDelegate.this.updateAmPmLabelStates(newValue);
                        break;
                }
                if (TimePickerClockDelegate.this.mOnTimeChangedListener != null) {
                    TimePickerClockDelegate.this.mOnTimeChangedListener.onTimeChanged(TimePickerClockDelegate.this.mDelegator, TimePickerClockDelegate.this.getHour(), TimePickerClockDelegate.this.getMinute());
                }
            }
        };
        this.mDigitEnteredListener = new OnValueChangedListener() {
            public void onValueChanged(NumericTextView view, int value, boolean isValid, boolean isFinished) {
                Runnable commitCallback;
                View -get4;
                if (view == TimePickerClockDelegate.this.mHourView) {
                    commitCallback = TimePickerClockDelegate.this.mCommitHour;
                    -get4 = view.isFocused() ? TimePickerClockDelegate.this.mMinuteView : null;
                } else if (view == TimePickerClockDelegate.this.mMinuteView) {
                    commitCallback = TimePickerClockDelegate.this.mCommitMinute;
                    -get4 = null;
                } else {
                    return;
                }
                view.removeCallbacks(commitCallback);
                if (isValid) {
                    if (isFinished) {
                        commitCallback.run();
                        if (-get4 != null) {
                            -get4.requestFocus();
                        }
                    } else {
                        view.postDelayed(commitCallback, TimePickerClockDelegate.DELAY_COMMIT_MILLIS);
                    }
                }
            }
        };
        this.mCommitHour = new Runnable() {
            public void run() {
                TimePickerClockDelegate.this.setHour(TimePickerClockDelegate.this.mHourView.getValue());
            }
        };
        this.mCommitMinute = new Runnable() {
            public void run() {
                TimePickerClockDelegate.this.setMinute(TimePickerClockDelegate.this.mMinuteView.getValue());
            }
        };
        this.mFocusListener = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean focused) {
                if (focused) {
                    switch (v.getId()) {
                        case R.id.hours /*16909353*/:
                            TimePickerClockDelegate.this.setCurrentItemShowing(TimePickerClockDelegate.HOUR_INDEX, true, true);
                            break;
                        case R.id.minutes /*16909355*/:
                            TimePickerClockDelegate.this.setCurrentItemShowing(TimePickerClockDelegate.PM, true, true);
                            break;
                        case R.id.am_label /*16909356*/:
                            TimePickerClockDelegate.this.setAmOrPm(TimePickerClockDelegate.HOUR_INDEX);
                            break;
                        case R.id.pm_label /*16909358*/:
                            TimePickerClockDelegate.this.setAmOrPm(TimePickerClockDelegate.PM);
                            break;
                        default:
                            return;
                    }
                    TimePickerClockDelegate.this.tryVibrate();
                }
            }
        };
        this.mClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.hours /*16909353*/:
                        TimePickerClockDelegate.this.setCurrentItemShowing(TimePickerClockDelegate.HOUR_INDEX, true, true);
                        break;
                    case R.id.minutes /*16909355*/:
                        TimePickerClockDelegate.this.setCurrentItemShowing(TimePickerClockDelegate.PM, true, true);
                        break;
                    case R.id.am_label /*16909356*/:
                        TimePickerClockDelegate.this.setAmOrPm(TimePickerClockDelegate.HOUR_INDEX);
                        break;
                    case R.id.pm_label /*16909358*/:
                        TimePickerClockDelegate.this.setAmOrPm(TimePickerClockDelegate.PM);
                        break;
                    default:
                        return;
                }
                TimePickerClockDelegate.this.tryVibrate();
            }
        };
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        Resources res = this.mContext.getResources();
        this.mSelectHours = res.getString(R.string.select_hours);
        this.mSelectMinutes = res.getString(R.string.select_minutes);
        View mainView = inflater.inflate(a.getResourceId(10, R.layout.time_picker_material), (ViewGroup) delegator);
        View headerView = mainView.findViewById(R.id.time_header);
        headerView.setOnTouchListener(new NearestTouchDelegate());
        this.mHourView = (NumericTextView) mainView.findViewById(R.id.hours);
        this.mHourView.setOnClickListener(this.mClickListener);
        this.mHourView.setOnFocusChangeListener(this.mFocusListener);
        this.mHourView.setOnDigitEnteredListener(this.mDigitEnteredListener);
        this.mHourView.setAccessibilityDelegate(new ClickActionDelegate(context, R.string.select_hours));
        this.mSeparatorView = (TextView) mainView.findViewById(R.id.separator);
        this.mMinuteView = (NumericTextView) mainView.findViewById(R.id.minutes);
        this.mMinuteView.setOnClickListener(this.mClickListener);
        this.mMinuteView.setOnFocusChangeListener(this.mFocusListener);
        this.mMinuteView.setOnDigitEnteredListener(this.mDigitEnteredListener);
        this.mMinuteView.setAccessibilityDelegate(new ClickActionDelegate(context, R.string.select_minutes));
        this.mMinuteView.setRange(HOUR_INDEX, 59);
        this.mAmPmLayout = mainView.findViewById(R.id.ampm_layout);
        this.mAmPmLayout.setOnTouchListener(new NearestTouchDelegate());
        String[] amPmStrings = TimePicker.getAmPmStrings(context);
        this.mAmLabel = (RadioButton) this.mAmPmLayout.findViewById(R.id.am_label);
        this.mAmLabel.setText(obtainVerbatim(amPmStrings[HOUR_INDEX]));
        this.mAmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mAmLabel);
        this.mPmLabel = (RadioButton) this.mAmPmLayout.findViewById(R.id.pm_label);
        this.mPmLabel.setText(obtainVerbatim(amPmStrings[PM]));
        this.mPmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mPmLabel);
        ColorStateList colorStateList = null;
        int timeHeaderTextAppearance = a.getResourceId(PM, HOUR_INDEX);
        if (timeHeaderTextAppearance != 0) {
            TypedArray textAppearance = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, HOUR_INDEX, timeHeaderTextAppearance);
            colorStateList = applyLegacyColorFixes(textAppearance.getColorStateList(HOUR_INDEX));
            textAppearance.recycle();
        }
        if (colorStateList == null) {
            colorStateList = a.getColorStateList(11);
        }
        if (colorStateList != null) {
            this.mHourView.setTextColor(colorStateList);
            this.mSeparatorView.setTextColor(colorStateList);
            this.mMinuteView.setTextColor(colorStateList);
            this.mAmLabel.setTextColor(colorStateList);
            this.mPmLabel.setTextColor(colorStateList);
        }
        if (a.hasValueOrEmpty(HOUR_INDEX)) {
            headerView.setBackground(a.getDrawable(HOUR_INDEX));
        }
        a.recycle();
        this.mRadialTimePickerView = (RadialTimePickerView) mainView.findViewById(R.id.radial_picker);
        this.mRadialTimePickerView.applyAttributes(attrs, defStyleAttr, defStyleRes);
        this.mRadialTimePickerView.setOnValueSelectedListener(this.mOnValueSelectedListener);
        this.mAllowAutoAdvance = true;
        updateHourFormat();
        this.mTempCalendar = Calendar.getInstance(this.mLocale);
        initialize(this.mTempCalendar.get(11), this.mTempCalendar.get(HOURS_IN_HALF_DAY), this.mIs24Hour, HOUR_INDEX);
    }

    private static void ensureMinimumTextWidth(TextView v) {
        v.measure(HOUR_INDEX, HOUR_INDEX);
        int minWidth = v.getMeasuredWidth();
        v.setMinWidth(minWidth);
        v.setMinimumWidth(minWidth);
    }

    private void updateHourFormat() {
        boolean z;
        int minHour;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24Hour ? "Hm" : "hm");
        int lengthPattern = bestDateTimePattern.length();
        boolean showLeadingZero = false;
        char hourFormat = '\u0000';
        int i = HOUR_INDEX;
        while (i < lengthPattern) {
            char c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == DateFormat.HOUR || c == 'K' || c == DateFormat.HOUR_OF_DAY) {
                hourFormat = c;
                if (i + PM < lengthPattern && c == bestDateTimePattern.charAt(i + PM)) {
                    showLeadingZero = true;
                }
                this.mHourFormatShowLeadingZero = showLeadingZero;
                z = hourFormat != 'K' || hourFormat == 'H';
                this.mHourFormatStartsAtZero = z;
                minHour = this.mHourFormatStartsAtZero ? HOUR_INDEX : PM;
                this.mHourView.setRange(minHour, (this.mIs24Hour ? 23 : 11) + minHour);
                this.mHourView.setShowLeadingZeroes(this.mHourFormatShowLeadingZero);
            }
            i += PM;
        }
        this.mHourFormatShowLeadingZero = showLeadingZero;
        if (hourFormat != 'K') {
        }
        this.mHourFormatStartsAtZero = z;
        if (this.mHourFormatStartsAtZero) {
        }
        if (this.mIs24Hour) {
        }
        this.mHourView.setRange(minHour, (this.mIs24Hour ? 23 : 11) + minHour);
        this.mHourView.setShowLeadingZeroes(this.mHourFormatShowLeadingZero);
    }

    private static final CharSequence obtainVerbatim(String text) {
        return new SpannableStringBuilder().append((CharSequence) text, new VerbatimBuilder(text).build(), (int) HOUR_INDEX);
    }

    private ColorStateList applyLegacyColorFixes(ColorStateList color) {
        if (color == null || color.hasState(R.attr.state_activated)) {
            return color;
        }
        int activatedColor;
        int defaultColor;
        if (color.hasState(R.attr.state_selected)) {
            activatedColor = color.getColorForState(StateSet.get(10), HOUR_INDEX);
            defaultColor = color.getColorForState(StateSet.get(8), HOUR_INDEX);
        } else {
            activatedColor = color.getDefaultColor();
            defaultColor = multiplyAlphaComponent(activatedColor, this.mContext.obtainStyledAttributes(ATTRS_DISABLED_ALPHA).getFloat(HOUR_INDEX, 0.3f));
        }
        if (activatedColor == 0 || defaultColor == 0) {
            return null;
        }
        int[][] stateSet = new int[AMPM_INDEX][];
        int[] iArr = new int[PM];
        iArr[HOUR_INDEX] = R.attr.state_activated;
        stateSet[HOUR_INDEX] = iArr;
        stateSet[PM] = new int[HOUR_INDEX];
        int[] colors = new int[AMPM_INDEX];
        colors[HOUR_INDEX] = activatedColor;
        colors[PM] = defaultColor;
        return new ColorStateList(stateSet, colors);
    }

    private int multiplyAlphaComponent(int color, float alphaMod) {
        return (((int) ((((float) ((color >> 24) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE)) * alphaMod) + 0.5f)) << 24) | (color & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT);
    }

    private void initialize(int hourOfDay, int minute, boolean is24HourView, int index) {
        this.mCurrentHour = hourOfDay;
        this.mCurrentMinute = minute;
        this.mIs24Hour = is24HourView;
        updateUI(index);
    }

    private void updateUI(int index) {
        updateHeaderAmPm();
        updateHeaderHour(this.mCurrentHour, false);
        updateHeaderSeparator();
        updateHeaderMinute(this.mCurrentMinute, false);
        updateRadialPicker(index);
        this.mDelegator.invalidate();
    }

    private void updateRadialPicker(int index) {
        this.mRadialTimePickerView.initialize(this.mCurrentHour, this.mCurrentMinute, this.mIs24Hour);
        setCurrentItemShowing(index, false, true);
    }

    private void updateHeaderAmPm() {
        if (this.mIs24Hour) {
            this.mAmPmLayout.setVisibility(8);
            return;
        }
        setAmPmAtStart(DateFormat.getBestDateTimePattern(this.mLocale, "hm").startsWith("a"));
        updateAmPmLabelStates(this.mCurrentHour < HOURS_IN_HALF_DAY ? HOUR_INDEX : PM);
    }

    private void setAmPmAtStart(boolean isAmPmAtStart) {
        if (this.mIsAmPmAtStart != isAmPmAtStart) {
            this.mIsAmPmAtStart = isAmPmAtStart;
            LayoutParams params = (LayoutParams) this.mAmPmLayout.getLayoutParams();
            if (!(params.getRule(PM) == 0 && params.getRule(HOUR_INDEX) == 0)) {
                if (isAmPmAtStart) {
                    params.removeRule(PM);
                    params.addRule(HOUR_INDEX, this.mHourView.getId());
                } else {
                    params.removeRule(HOUR_INDEX);
                    params.addRule(PM, this.mMinuteView.getId());
                }
            }
            this.mAmPmLayout.setLayoutParams(params);
        }
    }

    public void setHour(int hour) {
        setHourInternal(hour, false, true);
    }

    private void setHourInternal(int hour, boolean isFromPicker, boolean announce) {
        if (this.mCurrentHour != hour) {
            this.mCurrentHour = hour;
            updateHeaderHour(hour, announce);
            updateHeaderAmPm();
            if (!isFromPicker) {
                this.mRadialTimePickerView.setCurrentHour(hour);
                this.mRadialTimePickerView.setAmOrPm(hour < HOURS_IN_HALF_DAY ? HOUR_INDEX : PM);
            }
            this.mDelegator.invalidate();
            onTimeChanged();
        }
    }

    public int getHour() {
        int currentHour = this.mRadialTimePickerView.getCurrentHour();
        if (this.mIs24Hour) {
            return currentHour;
        }
        if (this.mRadialTimePickerView.getAmOrPm() == PM) {
            return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
        }
        return currentHour % HOURS_IN_HALF_DAY;
    }

    public void setMinute(int minute) {
        setMinuteInternal(minute, false);
    }

    private void setMinuteInternal(int minute, boolean isFromPicker) {
        if (this.mCurrentMinute != minute) {
            this.mCurrentMinute = minute;
            updateHeaderMinute(minute, true);
            if (!isFromPicker) {
                this.mRadialTimePickerView.setCurrentMinute(minute);
            }
            this.mDelegator.invalidate();
            onTimeChanged();
        }
    }

    public int getMinute() {
        return this.mRadialTimePickerView.getCurrentMinute();
    }

    public void setIs24Hour(boolean is24Hour) {
        if (this.mIs24Hour != is24Hour) {
            this.mIs24Hour = is24Hour;
            this.mCurrentHour = getHour();
            updateHourFormat();
            updateUI(this.mRadialTimePickerView.getCurrentItemShowing());
        }
    }

    public boolean is24Hour() {
        return this.mIs24Hour;
    }

    public void setOnTimeChangedListener(OnTimeChangedListener callback) {
        this.mOnTimeChangedListener = callback;
    }

    public void setEnabled(boolean enabled) {
        this.mHourView.setEnabled(enabled);
        this.mMinuteView.setEnabled(enabled);
        this.mAmLabel.setEnabled(enabled);
        this.mPmLabel.setEnabled(enabled);
        this.mRadialTimePickerView.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public int getBaseline() {
        return -1;
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        return new SavedState(superState, getHour(), getMinute(), is24Hour(), getCurrentItemShowing());
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            initialize(ss.getHour(), ss.getMinute(), ss.is24HourMode(), ss.getCurrentItemShowing());
            this.mRadialTimePickerView.invalidate();
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        if (this.mIs24Hour) {
            flags = LogPower.START_CAMERA;
        } else {
            flags = 65;
        }
        this.mTempCalendar.set(11, getHour());
        this.mTempCalendar.set(HOURS_IN_HALF_DAY, getMinute());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags) + " " + (this.mRadialTimePickerView.getCurrentItemShowing() == 0 ? this.mSelectHours : this.mSelectMinutes));
    }

    private int getCurrentItemShowing() {
        return this.mRadialTimePickerView.getCurrentItemShowing();
    }

    private void onTimeChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
    }

    private void tryVibrate() {
        this.mDelegator.performHapticFeedback(4);
    }

    private void updateAmPmLabelStates(int amOrPm) {
        boolean isAm = amOrPm == 0;
        this.mAmLabel.setActivated(isAm);
        this.mAmLabel.setChecked(isAm);
        boolean isPm = amOrPm == PM;
        this.mPmLabel.setActivated(isPm);
        this.mPmLabel.setChecked(isPm);
    }

    private int getLocalizedHour(int hourOfDay) {
        if (!this.mIs24Hour) {
            hourOfDay %= HOURS_IN_HALF_DAY;
        }
        if (this.mHourFormatStartsAtZero || hourOfDay != 0) {
            return hourOfDay;
        }
        return this.mIs24Hour ? 24 : HOURS_IN_HALF_DAY;
    }

    private void updateHeaderHour(int hourOfDay, boolean announce) {
        this.mHourView.setValue(getLocalizedHour(hourOfDay));
        if (announce) {
            tryAnnounceForAccessibility(this.mHourView.getText(), true);
        }
    }

    private void updateHeaderMinute(int minuteOfHour, boolean announce) {
        this.mMinuteView.setValue(minuteOfHour);
        if (announce) {
            tryAnnounceForAccessibility(this.mMinuteView.getText(), false);
        }
    }

    private void updateHeaderSeparator() {
        CharSequence separatorText;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24Hour ? "Hm" : "hm");
        int hIndex = lastIndexOfAny(bestDateTimePattern, new char[]{'H', DateFormat.HOUR, 'K', DateFormat.HOUR_OF_DAY});
        if (hIndex == -1) {
            separatorText = ":";
        } else {
            separatorText = Character.toString(bestDateTimePattern.charAt(hIndex + PM));
        }
        this.mSeparatorView.setText(separatorText);
    }

    private static int lastIndexOfAny(String str, char[] any) {
        int lengthAny = any.length;
        if (lengthAny > 0) {
            for (int i = str.length() - 1; i >= 0; i--) {
                char c = str.charAt(i);
                for (int j = HOUR_INDEX; j < lengthAny; j += PM) {
                    if (c == any[j]) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void tryAnnounceForAccessibility(CharSequence text, boolean isHour) {
        if (this.mLastAnnouncedIsHour != isHour || !text.equals(this.mLastAnnouncedText)) {
            this.mDelegator.announceForAccessibility(text);
            this.mLastAnnouncedText = text;
            this.mLastAnnouncedIsHour = isHour;
        }
    }

    private void setCurrentItemShowing(int index, boolean animateCircle, boolean announce) {
        boolean z;
        boolean z2 = true;
        this.mRadialTimePickerView.setCurrentItemShowing(index, animateCircle);
        if (index == 0) {
            if (announce) {
                this.mDelegator.announceForAccessibility(this.mSelectHours);
            }
        } else if (announce) {
            this.mDelegator.announceForAccessibility(this.mSelectMinutes);
        }
        NumericTextView numericTextView = this.mHourView;
        if (index == 0) {
            z = true;
        } else {
            z = false;
        }
        numericTextView.setActivated(z);
        NumericTextView numericTextView2 = this.mMinuteView;
        if (index != PM) {
            z2 = false;
        }
        numericTextView2.setActivated(z2);
    }

    private void setAmOrPm(int amOrPm) {
        updateAmPmLabelStates(amOrPm);
        if (this.mRadialTimePickerView.setAmOrPm(amOrPm)) {
            this.mCurrentHour = getHour();
            if (this.mOnTimeChangedListener != null) {
                this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
            }
        }
    }
}
