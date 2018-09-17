package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.text.DecimalFormatSymbols;
import android.os.Parcelable;
import android.provider.SettingsStringUtil;
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
import android.widget.RelativeLayout.LayoutParams;
import com.android.internal.R;
import com.android.internal.widget.NumericTextView;
import com.android.internal.widget.NumericTextView.OnValueChangedListener;
import java.util.Calendar;

class TimePickerClockDelegate extends AbstractTimePickerDelegate {
    private static final int AM = 0;
    private static final int[] ATTRS_DISABLED_ALPHA = new int[]{R.attr.disabledAlpha};
    private static final int[] ATTRS_TEXT_COLOR = new int[]{R.attr.textColor};
    private static final long DELAY_COMMIT_MILLIS = 2000;
    private static final int FROM_EXTERNAL_API = 0;
    private static final int FROM_INPUT_PICKER = 2;
    private static final int FROM_RADIAL_PICKER = 1;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOUR_INDEX = 0;
    private static final int MINUTE_INDEX = 1;
    private static final int PM = 1;
    private boolean mAllowAutoAdvance;
    private final RadioButton mAmLabel;
    private final View mAmPmLayout;
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.am_label /*16908720*/:
                    TimePickerClockDelegate.this.setAmOrPm(0);
                    break;
                case R.id.hours /*16908948*/:
                    TimePickerClockDelegate.this.setCurrentItemShowing(0, true, true);
                    break;
                case R.id.minutes /*16909059*/:
                    TimePickerClockDelegate.this.setCurrentItemShowing(1, true, true);
                    break;
                case R.id.pm_label /*16909172*/:
                    TimePickerClockDelegate.this.setAmOrPm(1);
                    break;
                default:
                    return;
            }
            TimePickerClockDelegate.this.tryVibrate();
        }
    };
    private final Runnable mCommitHour = new Runnable() {
        public void run() {
            TimePickerClockDelegate.this.setHour(TimePickerClockDelegate.this.mHourView.getValue());
        }
    };
    private final Runnable mCommitMinute = new Runnable() {
        public void run() {
            TimePickerClockDelegate.this.setMinute(TimePickerClockDelegate.this.mMinuteView.getValue());
        }
    };
    private int mCurrentHour;
    private int mCurrentMinute;
    private final OnValueChangedListener mDigitEnteredListener = new OnValueChangedListener() {
        public void onValueChanged(NumericTextView view, int value, boolean isValid, boolean isFinished) {
            Runnable commitCallback;
            View nextFocusTarget;
            if (view == TimePickerClockDelegate.this.mHourView) {
                commitCallback = TimePickerClockDelegate.this.mCommitHour;
                nextFocusTarget = view.isFocused() ? TimePickerClockDelegate.this.mMinuteView : null;
            } else if (view == TimePickerClockDelegate.this.mMinuteView) {
                commitCallback = TimePickerClockDelegate.this.mCommitMinute;
                nextFocusTarget = null;
            } else {
                return;
            }
            view.removeCallbacks(commitCallback);
            if (isValid) {
                if (isFinished) {
                    commitCallback.run();
                    if (nextFocusTarget != null) {
                        nextFocusTarget.requestFocus();
                    }
                } else {
                    view.postDelayed(commitCallback, TimePickerClockDelegate.DELAY_COMMIT_MILLIS);
                }
            }
        }
    };
    private final OnFocusChangeListener mFocusListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean focused) {
            if (focused) {
                switch (v.getId()) {
                    case R.id.am_label /*16908720*/:
                        TimePickerClockDelegate.this.setAmOrPm(0);
                        break;
                    case R.id.hours /*16908948*/:
                        TimePickerClockDelegate.this.setCurrentItemShowing(0, true, true);
                        break;
                    case R.id.minutes /*16909059*/:
                        TimePickerClockDelegate.this.setCurrentItemShowing(1, true, true);
                        break;
                    case R.id.pm_label /*16909172*/:
                        TimePickerClockDelegate.this.setAmOrPm(1);
                        break;
                    default:
                        return;
                }
                TimePickerClockDelegate.this.tryVibrate();
            }
        }
    };
    private boolean mHourFormatShowLeadingZero;
    private boolean mHourFormatStartsAtZero;
    private final NumericTextView mHourView;
    private boolean mIs24Hour;
    private boolean mIsAmPmAtStart;
    private boolean mIsEnabled = true;
    private boolean mLastAnnouncedIsHour;
    private CharSequence mLastAnnouncedText;
    private final NumericTextView mMinuteView;
    private final OnValueSelectedListener mOnValueSelectedListener = new OnValueSelectedListener() {
        public void onValueSelected(int pickerType, int newValue, boolean autoAdvance) {
            boolean valueChanged = false;
            switch (pickerType) {
                case 0:
                    if (TimePickerClockDelegate.this.getHour() != newValue) {
                        valueChanged = true;
                    }
                    int isTransition = TimePickerClockDelegate.this.mAllowAutoAdvance ? autoAdvance : 0;
                    TimePickerClockDelegate.this.setHourInternal(newValue, 1, isTransition ^ 1);
                    if (isTransition != 0) {
                        TimePickerClockDelegate.this.setCurrentItemShowing(1, true, false);
                        TimePickerClockDelegate.this.mDelegator.announceForAccessibility(TimePickerClockDelegate.this.getLocalizedHour(newValue) + ". " + TimePickerClockDelegate.this.mSelectMinutes);
                        break;
                    }
                    break;
                case 1:
                    if (TimePickerClockDelegate.this.getMinute() != newValue) {
                        valueChanged = true;
                    }
                    TimePickerClockDelegate.this.setMinuteInternal(newValue, 1);
                    break;
            }
            if (TimePickerClockDelegate.this.mOnTimeChangedListener != null && valueChanged) {
                TimePickerClockDelegate.this.mOnTimeChangedListener.onTimeChanged(TimePickerClockDelegate.this.mDelegator, TimePickerClockDelegate.this.getHour(), TimePickerClockDelegate.this.getMinute());
            }
        }
    };
    private final OnValueTypedListener mOnValueTypedListener = new OnValueTypedListener() {
        public void onValueChanged(int pickerType, int newValue) {
            switch (pickerType) {
                case 0:
                    TimePickerClockDelegate.this.setHourInternal(newValue, 2, false);
                    return;
                case 1:
                    TimePickerClockDelegate.this.setMinuteInternal(newValue, 2);
                    return;
                case 2:
                    TimePickerClockDelegate.this.setAmOrPm(newValue);
                    return;
                default:
                    return;
            }
        }
    };
    private final RadioButton mPmLabel;
    private boolean mRadialPickerModeEnabled = true;
    private final View mRadialTimePickerHeader;
    private final ImageButton mRadialTimePickerModeButton;
    private final String mRadialTimePickerModeEnabledDescription;
    private final RadialTimePickerView mRadialTimePickerView;
    private final String mSelectHours;
    private final String mSelectMinutes;
    private final TextView mSeparatorView;
    private final Calendar mTempCalendar;
    private final View mTextInputPickerHeader;
    private final String mTextInputPickerModeEnabledDescription;
    private final TextInputTimePickerView mTextInputPickerView;

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

        /* synthetic */ NearestTouchDelegate(NearestTouchDelegate -this0) {
            this();
        }

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
            if (actionMasked == 1 || actionMasked == 3) {
                this.mInitialTouchTarget = null;
            }
            return handled;
        }

        private View findNearestChild(ViewGroup v, int x, int y) {
            View bestChild = null;
            int bestDist = Integer.MAX_VALUE;
            int count = v.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = v.getChildAt(i);
                int dX = x - (child.getLeft() + (child.getWidth() / 2));
                int dY = y - (child.getTop() + (child.getHeight() / 2));
                int dist = (dX * dX) + (dY * dY);
                if (bestDist > dist) {
                    bestChild = child;
                    bestDist = dist;
                }
            }
            return bestChild;
        }
    }

    public TimePickerClockDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        Resources res = this.mContext.getResources();
        this.mSelectHours = res.getString(R.string.select_hours);
        this.mSelectMinutes = res.getString(R.string.select_minutes);
        View mainView = inflater.inflate(a.getResourceId(12, R.layout.time_picker_material), (ViewGroup) delegator);
        mainView.setSaveFromParentEnabled(false);
        this.mRadialTimePickerHeader = mainView.findViewById(R.id.time_header);
        this.mRadialTimePickerHeader.setOnTouchListener(new NearestTouchDelegate());
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
        this.mMinuteView.setRange(0, 59);
        this.mAmPmLayout = mainView.findViewById(R.id.ampm_layout);
        this.mAmPmLayout.setOnTouchListener(new NearestTouchDelegate());
        String[] amPmStrings = TimePicker.getAmPmStrings(context);
        this.mAmLabel = (RadioButton) this.mAmPmLayout.findViewById(R.id.am_label);
        this.mAmLabel.setText(obtainVerbatim(amPmStrings[0]));
        this.mAmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mAmLabel);
        this.mPmLabel = (RadioButton) this.mAmPmLayout.findViewById(R.id.pm_label);
        this.mPmLabel.setText(obtainVerbatim(amPmStrings[1]));
        this.mPmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mPmLabel);
        ColorStateList headerTextColor = null;
        int timeHeaderTextAppearance = a.getResourceId(1, 0);
        if (timeHeaderTextAppearance != 0) {
            TypedArray textAppearance = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, timeHeaderTextAppearance);
            headerTextColor = applyLegacyColorFixes(textAppearance.getColorStateList(0));
            textAppearance.recycle();
        }
        if (headerTextColor == null) {
            headerTextColor = a.getColorStateList(11);
        }
        this.mTextInputPickerHeader = mainView.findViewById(R.id.input_header);
        if (headerTextColor != null) {
            this.mHourView.setTextColor(headerTextColor);
            this.mSeparatorView.setTextColor(headerTextColor);
            this.mMinuteView.setTextColor(headerTextColor);
            this.mAmLabel.setTextColor(headerTextColor);
            this.mPmLabel.setTextColor(headerTextColor);
        }
        if (a.hasValueOrEmpty(0)) {
            this.mRadialTimePickerHeader.setBackground(a.getDrawable(0));
            this.mTextInputPickerHeader.setBackground(a.getDrawable(0));
        }
        a.recycle();
        this.mRadialTimePickerView = (RadialTimePickerView) mainView.findViewById(R.id.radial_picker);
        this.mRadialTimePickerView.applyAttributes(attrs, defStyleAttr, defStyleRes);
        this.mRadialTimePickerView.setOnValueSelectedListener(this.mOnValueSelectedListener);
        this.mTextInputPickerView = (TextInputTimePickerView) mainView.findViewById(R.id.input_mode);
        this.mTextInputPickerView.setListener(this.mOnValueTypedListener);
        this.mRadialTimePickerModeButton = (ImageButton) mainView.findViewById(R.id.toggle_mode);
        this.mRadialTimePickerModeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePickerClockDelegate.this.toggleRadialPickerMode();
            }
        });
        this.mRadialTimePickerModeEnabledDescription = context.getResources().getString(R.string.time_picker_radial_mode_description);
        this.mTextInputPickerModeEnabledDescription = context.getResources().getString(R.string.time_picker_text_input_mode_description);
        this.mAllowAutoAdvance = true;
        updateHourFormat();
        this.mTempCalendar = Calendar.getInstance(this.mLocale);
        initialize(this.mTempCalendar.get(11), this.mTempCalendar.get(12), this.mIs24Hour, 0);
    }

    private void toggleRadialPickerMode() {
        if (this.mRadialPickerModeEnabled) {
            this.mRadialTimePickerView.setVisibility(8);
            this.mRadialTimePickerHeader.setVisibility(8);
            this.mTextInputPickerHeader.setVisibility(0);
            this.mTextInputPickerView.setVisibility(0);
            this.mRadialTimePickerModeButton.setImageResource(R.drawable.btn_clock_material);
            this.mRadialTimePickerModeButton.setContentDescription(this.mRadialTimePickerModeEnabledDescription);
            this.mRadialPickerModeEnabled = false;
            return;
        }
        this.mRadialTimePickerView.setVisibility(0);
        this.mRadialTimePickerHeader.setVisibility(0);
        this.mTextInputPickerHeader.setVisibility(8);
        this.mTextInputPickerView.setVisibility(8);
        this.mRadialTimePickerModeButton.setImageResource(R.drawable.btn_keyboard_key_material);
        this.mRadialTimePickerModeButton.setContentDescription(this.mTextInputPickerModeEnabledDescription);
        updateTextInputPicker();
        this.mRadialPickerModeEnabled = true;
    }

    public boolean validateInput() {
        return this.mTextInputPickerView.validateInput();
    }

    private static void ensureMinimumTextWidth(TextView v) {
        v.measure(0, 0);
        int minWidth = v.getMeasuredWidth();
        v.setMinWidth(minWidth);
        v.setMinimumWidth(minWidth);
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065 A:{LOOP_END, LOOP:1: B:25:0x0061->B:27:0x0065} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateHourFormat() {
        boolean z;
        int minHour;
        String[] digits;
        int maxCharLength;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24Hour ? "Hm" : "hm");
        int lengthPattern = bestDateTimePattern.length();
        boolean showLeadingZero = false;
        char hourFormat = 0;
        int i = 0;
        while (i < lengthPattern) {
            char c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == DateFormat.HOUR || c == 'K' || c == DateFormat.HOUR_OF_DAY) {
                hourFormat = c;
                if (i + 1 < lengthPattern && c == bestDateTimePattern.charAt(i + 1)) {
                    showLeadingZero = true;
                }
                this.mHourFormatShowLeadingZero = showLeadingZero;
                z = hourFormat != 'K' || hourFormat == 'H';
                this.mHourFormatStartsAtZero = z;
                minHour = this.mHourFormatStartsAtZero ? 0 : 1;
                this.mHourView.setRange(minHour, (this.mIs24Hour ? 23 : 11) + minHour);
                this.mHourView.setShowLeadingZeroes(this.mHourFormatShowLeadingZero);
                digits = DecimalFormatSymbols.getInstance(this.mLocale).getDigitStrings();
                maxCharLength = 0;
                for (i = 0; i < 10; i++) {
                    maxCharLength = Math.max(maxCharLength, digits[i].length());
                }
                this.mTextInputPickerView.setHourFormat(maxCharLength * 2);
            }
            i++;
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
        digits = DecimalFormatSymbols.getInstance(this.mLocale).getDigitStrings();
        maxCharLength = 0;
        while (i < 10) {
        }
        this.mTextInputPickerView.setHourFormat(maxCharLength * 2);
    }

    static final CharSequence obtainVerbatim(String text) {
        return new SpannableStringBuilder().append((CharSequence) text, new VerbatimBuilder(text).build(), 0);
    }

    private ColorStateList applyLegacyColorFixes(ColorStateList color) {
        if (color == null || color.hasState(R.attr.state_activated)) {
            return color;
        }
        int activatedColor;
        int defaultColor;
        if (color.hasState(R.attr.state_selected)) {
            activatedColor = color.getColorForState(StateSet.get(10), 0);
            defaultColor = color.getColorForState(StateSet.get(8), 0);
        } else {
            activatedColor = color.getDefaultColor();
            defaultColor = multiplyAlphaComponent(activatedColor, this.mContext.obtainStyledAttributes(ATTRS_DISABLED_ALPHA).getFloat(0, 0.3f));
        }
        if (activatedColor == 0 || defaultColor == 0) {
            return null;
        }
        stateSet = new int[2][];
        stateSet[0] = new int[]{R.attr.state_activated};
        stateSet[1] = new int[0];
        return new ColorStateList(stateSet, new int[]{activatedColor, defaultColor});
    }

    private int multiplyAlphaComponent(int color, float alphaMod) {
        return (((int) ((((float) ((color >> 24) & 255)) * alphaMod) + 0.5f)) << 24) | (color & 16777215);
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
        updateTextInputPicker();
        this.mDelegator.invalidate();
    }

    private void updateTextInputPicker() {
        this.mTextInputPickerView.updateTextInputValues(getLocalizedHour(this.mCurrentHour), this.mCurrentMinute, this.mCurrentHour < 12 ? 0 : 1, this.mIs24Hour, this.mHourFormatStartsAtZero);
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
        updateAmPmLabelStates(this.mCurrentHour < 12 ? 0 : 1);
    }

    private void setAmPmAtStart(boolean isAmPmAtStart) {
        if (this.mIsAmPmAtStart != isAmPmAtStart) {
            this.mIsAmPmAtStart = isAmPmAtStart;
            LayoutParams params = (LayoutParams) this.mAmPmLayout.getLayoutParams();
            if (!(params.getRule(1) == 0 && params.getRule(0) == 0)) {
                if (isAmPmAtStart) {
                    params.removeRule(1);
                    params.addRule(0, this.mHourView.getId());
                } else {
                    params.removeRule(0);
                    params.addRule(1, this.mMinuteView.getId());
                }
            }
            this.mAmPmLayout.-wrap18(params);
        }
    }

    public void setHour(int hour) {
        setHourInternal(hour, 0, true);
    }

    private void setHourInternal(int hour, int source, boolean announce) {
        int i = 1;
        if (this.mCurrentHour != hour) {
            this.mCurrentHour = hour;
            updateHeaderHour(hour, announce);
            updateHeaderAmPm();
            if (source != 1) {
                this.mRadialTimePickerView.setCurrentHour(hour);
                RadialTimePickerView radialTimePickerView = this.mRadialTimePickerView;
                if (hour < 12) {
                    i = 0;
                }
                radialTimePickerView.setAmOrPm(i);
            }
            if (source != 2) {
                updateTextInputPicker();
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
        if (this.mRadialTimePickerView.getAmOrPm() == 1) {
            return (currentHour % 12) + 12;
        }
        return currentHour % 12;
    }

    public void setMinute(int minute) {
        setMinuteInternal(minute, 0);
    }

    private void setMinuteInternal(int minute, int source) {
        if (this.mCurrentMinute != minute) {
            this.mCurrentMinute = minute;
            updateHeaderMinute(minute, true);
            if (source != 1) {
                this.mRadialTimePickerView.setCurrentMinute(minute);
            }
            if (source != 2) {
                updateTextInputPicker();
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
            flags = 129;
        } else {
            flags = 65;
        }
        this.mTempCalendar.set(11, getHour());
        this.mTempCalendar.set(12, getMinute());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags) + " " + (this.mRadialTimePickerView.getCurrentItemShowing() == 0 ? this.mSelectHours : this.mSelectMinutes));
    }

    public View getHourView() {
        return this.mHourView;
    }

    public View getMinuteView() {
        return this.mMinuteView;
    }

    public View getAmView() {
        return this.mAmLabel;
    }

    public View getPmView() {
        return this.mPmLabel;
    }

    private int getCurrentItemShowing() {
        return this.mRadialTimePickerView.getCurrentItemShowing();
    }

    private void onTimeChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
        if (this.mAutoFillChangeListener != null) {
            this.mAutoFillChangeListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
    }

    private void tryVibrate() {
        this.mDelegator.performHapticFeedback(4);
    }

    private void updateAmPmLabelStates(int amOrPm) {
        boolean isAm = amOrPm == 0;
        this.mAmLabel.setActivated(isAm);
        this.mAmLabel.setChecked(isAm);
        boolean isPm = amOrPm == 1;
        this.mPmLabel.setActivated(isPm);
        this.mPmLabel.setChecked(isPm);
    }

    private int getLocalizedHour(int hourOfDay) {
        if (!this.mIs24Hour) {
            hourOfDay %= 12;
        }
        if (this.mHourFormatStartsAtZero || hourOfDay != 0) {
            return hourOfDay;
        }
        return this.mIs24Hour ? 24 : 12;
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
            separatorText = SettingsStringUtil.DELIMITER;
        } else {
            separatorText = Character.toString(bestDateTimePattern.charAt(hIndex + 1));
        }
        this.mSeparatorView.setText(separatorText);
        this.mTextInputPickerView.updateSeparator(separatorText);
    }

    private static int lastIndexOfAny(String str, char[] any) {
        if (lengthAny > 0) {
            for (int i = str.length() - 1; i >= 0; i--) {
                char c = str.charAt(i);
                for (char c2 : any) {
                    if (c == c2) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void tryAnnounceForAccessibility(CharSequence text, boolean isHour) {
        if (this.mLastAnnouncedIsHour != isHour || (text.equals(this.mLastAnnouncedText) ^ 1) != 0) {
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
        if (index != 1) {
            z2 = false;
        }
        numericTextView2.setActivated(z2);
    }

    private void setAmOrPm(int amOrPm) {
        updateAmPmLabelStates(amOrPm);
        if (this.mRadialTimePickerView.setAmOrPm(amOrPm)) {
            this.mCurrentHour = getHour();
            updateTextInputPicker();
            if (this.mOnTimeChangedListener != null) {
                this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
            }
        }
    }
}
