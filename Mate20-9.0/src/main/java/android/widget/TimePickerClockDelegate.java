package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.text.DecimalFormatSymbols;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable;
import android.provider.SettingsStringUtil;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.TtsSpan;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadialTimePickerView;
import android.widget.RelativeLayout;
import android.widget.TextInputTimePickerView;
import android.widget.TimePicker;
import com.android.internal.R;
import com.android.internal.widget.NumericTextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.List;

class TimePickerClockDelegate extends TimePicker.AbstractTimePickerDelegate {
    private static final int AM = 0;
    private static final int[] ATTRS_DISABLED_ALPHA = {16842803};
    private static final int[] ATTRS_TEXT_COLOR = {16842904};
    private static final long DELAY_COMMIT_MILLIS = 2000;
    private static final int FROM_EXTERNAL_API = 0;
    private static final int FROM_INPUT_PICKER = 2;
    private static final int FROM_RADIAL_PICKER = 1;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOUR_INDEX = 0;
    private static final int MINUTE_INDEX = 1;
    private static final int PM = 1;
    /* access modifiers changed from: private */
    public boolean mAllowAutoAdvance;
    private final RadioButton mAmLabel;
    private final View mAmPmLayout;
    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == 16908733) {
                TimePickerClockDelegate.this.setAmOrPm(0);
            } else if (id == 16908974) {
                TimePickerClockDelegate.this.setCurrentItemShowing(0, true, true);
            } else if (id == 16909093) {
                TimePickerClockDelegate.this.setCurrentItemShowing(1, true, true);
            } else if (id == 16909211) {
                TimePickerClockDelegate.this.setAmOrPm(1);
            } else {
                return;
            }
            TimePickerClockDelegate.this.tryVibrate();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mCommitHour = new Runnable() {
        public void run() {
            TimePickerClockDelegate.this.setHour(TimePickerClockDelegate.this.mHourView.getValue());
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mCommitMinute = new Runnable() {
        public void run() {
            TimePickerClockDelegate.this.setMinute(TimePickerClockDelegate.this.mMinuteView.getValue());
        }
    };
    private int mCurrentHour;
    private int mCurrentMinute;
    private final NumericTextView.OnValueChangedListener mDigitEnteredListener = new NumericTextView.OnValueChangedListener() {
        public void onValueChanged(NumericTextView view, int value, boolean isValid, boolean isFinished) {
            View nextFocusTarget;
            Runnable commitCallback;
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
    private final View.OnFocusChangeListener mFocusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean focused) {
            if (focused) {
                int id = v.getId();
                if (id == 16908733) {
                    TimePickerClockDelegate.this.setAmOrPm(0);
                } else if (id == 16908974) {
                    TimePickerClockDelegate.this.setCurrentItemShowing(0, true, true);
                } else if (id == 16909093) {
                    TimePickerClockDelegate.this.setCurrentItemShowing(1, true, true);
                } else if (id == 16909211) {
                    TimePickerClockDelegate.this.setAmOrPm(1);
                } else {
                    return;
                }
                TimePickerClockDelegate.this.tryVibrate();
            }
        }
    };
    private boolean mHourFormatShowLeadingZero;
    private boolean mHourFormatStartsAtZero;
    /* access modifiers changed from: private */
    public final NumericTextView mHourView;
    private boolean mIs24Hour;
    private boolean mIsAmPmAtLeft = false;
    private boolean mIsAmPmAtTop = false;
    private boolean mIsEnabled = true;
    private boolean mLastAnnouncedIsHour;
    private CharSequence mLastAnnouncedText;
    /* access modifiers changed from: private */
    public final NumericTextView mMinuteView;
    private final RadialTimePickerView.OnValueSelectedListener mOnValueSelectedListener = new RadialTimePickerView.OnValueSelectedListener() {
        public void onValueSelected(int pickerType, int newValue, boolean autoAdvance) {
            boolean valueChanged = false;
            switch (pickerType) {
                case 0:
                    if (TimePickerClockDelegate.this.getHour() != newValue) {
                        valueChanged = true;
                    }
                    boolean isTransition = TimePickerClockDelegate.this.mAllowAutoAdvance && autoAdvance;
                    TimePickerClockDelegate.this.setHourInternal(newValue, 1, !isTransition, true);
                    if (isTransition) {
                        TimePickerClockDelegate.this.setCurrentItemShowing(1, true, false);
                        int localizedHour = TimePickerClockDelegate.this.getLocalizedHour(newValue);
                        TimePicker timePicker = TimePickerClockDelegate.this.mDelegator;
                        timePicker.announceForAccessibility(localizedHour + ". " + TimePickerClockDelegate.this.mSelectMinutes);
                        break;
                    }
                    break;
                case 1:
                    if (TimePickerClockDelegate.this.getMinute() != newValue) {
                        valueChanged = true;
                    }
                    TimePickerClockDelegate.this.setMinuteInternal(newValue, 1, true);
                    break;
            }
            if (TimePickerClockDelegate.this.mOnTimeChangedListener != null && valueChanged) {
                TimePickerClockDelegate.this.mOnTimeChangedListener.onTimeChanged(TimePickerClockDelegate.this.mDelegator, TimePickerClockDelegate.this.getHour(), TimePickerClockDelegate.this.getMinute());
            }
        }
    };
    private final TextInputTimePickerView.OnValueTypedListener mOnValueTypedListener = new TextInputTimePickerView.OnValueTypedListener() {
        public void onValueChanged(int pickerType, int newValue) {
            switch (pickerType) {
                case 0:
                    TimePickerClockDelegate.this.setHourInternal(newValue, 2, false, true);
                    return;
                case 1:
                    TimePickerClockDelegate.this.setMinuteInternal(newValue, 2, true);
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
    /* access modifiers changed from: private */
    public final String mSelectMinutes;
    private final TextView mSeparatorView;
    private final Calendar mTempCalendar;
    private final View mTextInputPickerHeader;
    private final String mTextInputPickerModeEnabledDescription;
    private final TextInputTimePickerView mTextInputPickerView;

    @Retention(RetentionPolicy.SOURCE)
    private @interface ChangeSource {
    }

    private static class ClickActionDelegate extends View.AccessibilityDelegate {
        private final AccessibilityNodeInfo.AccessibilityAction mClickAction;

        public ClickActionDelegate(Context context, int resId) {
            this.mClickAction = new AccessibilityNodeInfo.AccessibilityAction(16, context.getString(resId));
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(this.mClickAction);
        }
    }

    private static class NearestTouchDelegate implements View.OnTouchListener {
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TimePickerClockDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        Context context2 = context;
        AttributeSet attributeSet = attrs;
        int i = defStyleAttr;
        int i2 = defStyleRes;
        TypedArray a = this.mContext.obtainStyledAttributes(attributeSet, R.styleable.TimePicker, i, i2);
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        Resources res = this.mContext.getResources();
        this.mSelectHours = res.getString(17041061);
        this.mSelectMinutes = res.getString(17041065);
        View mainView = inflater.inflate(a.getResourceId(12, 17367319), (ViewGroup) delegator);
        mainView.setSaveFromParentEnabled(false);
        this.mRadialTimePickerHeader = mainView.findViewById(16909438);
        this.mRadialTimePickerHeader.setOnTouchListener(new NearestTouchDelegate());
        this.mHourView = mainView.findViewById(16908974);
        this.mHourView.setOnClickListener(this.mClickListener);
        this.mHourView.setOnFocusChangeListener(this.mFocusListener);
        this.mHourView.setOnDigitEnteredListener(this.mDigitEnteredListener);
        this.mHourView.setAccessibilityDelegate(new ClickActionDelegate(context2, 17041061));
        this.mSeparatorView = (TextView) mainView.findViewById(16909307);
        this.mMinuteView = mainView.findViewById(16909093);
        this.mMinuteView.setOnClickListener(this.mClickListener);
        this.mMinuteView.setOnFocusChangeListener(this.mFocusListener);
        this.mMinuteView.setOnDigitEnteredListener(this.mDigitEnteredListener);
        this.mMinuteView.setAccessibilityDelegate(new ClickActionDelegate(context2, 17041065));
        this.mMinuteView.setRange(0, 59);
        this.mAmPmLayout = mainView.findViewById(16908735);
        this.mAmPmLayout.setOnTouchListener(new NearestTouchDelegate());
        String[] amPmStrings = TimePicker.getAmPmStrings(context);
        this.mAmLabel = (RadioButton) this.mAmPmLayout.findViewById(16908733);
        this.mAmLabel.setText(obtainVerbatim(amPmStrings[0]));
        this.mAmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mAmLabel);
        this.mPmLabel = (RadioButton) this.mAmPmLayout.findViewById(16909211);
        this.mPmLabel.setText(obtainVerbatim(amPmStrings[1]));
        this.mPmLabel.setOnClickListener(this.mClickListener);
        ensureMinimumTextWidth(this.mPmLabel);
        ColorStateList headerTextColor = null;
        int timeHeaderTextAppearance = a.getResourceId(1, 0);
        if (timeHeaderTextAppearance != 0) {
            String[] strArr = amPmStrings;
            LayoutInflater layoutInflater = inflater;
            TypedArray textAppearance = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, timeHeaderTextAppearance);
            headerTextColor = applyLegacyColorFixes(textAppearance.getColorStateList(0));
            textAppearance.recycle();
        } else {
            LayoutInflater layoutInflater2 = inflater;
        }
        headerTextColor = headerTextColor == null ? a.getColorStateList(11) : headerTextColor;
        this.mTextInputPickerHeader = mainView.findViewById(16909003);
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
        this.mRadialTimePickerView = (RadialTimePickerView) mainView.findViewById(16909236);
        this.mRadialTimePickerView.applyAttributes(attributeSet, i, i2);
        this.mRadialTimePickerView.setOnValueSelectedListener(this.mOnValueSelectedListener);
        this.mTextInputPickerView = (TextInputTimePickerView) mainView.findViewById(16909006);
        this.mTextInputPickerView.setListener(this.mOnValueTypedListener);
        this.mRadialTimePickerModeButton = (ImageButton) mainView.findViewById(16909455);
        this.mRadialTimePickerModeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TimePickerClockDelegate.this.toggleRadialPickerMode();
            }
        });
        this.mRadialTimePickerModeEnabledDescription = context.getResources().getString(17041259);
        this.mTextInputPickerModeEnabledDescription = context.getResources().getString(17041260);
        this.mAllowAutoAdvance = true;
        updateHourFormat();
        this.mTempCalendar = Calendar.getInstance(this.mLocale);
        initialize(this.mTempCalendar.get(11), this.mTempCalendar.get(12), this.mIs24Hour, 0);
    }

    /* access modifiers changed from: private */
    public void toggleRadialPickerMode() {
        if (this.mRadialPickerModeEnabled) {
            this.mRadialTimePickerView.setVisibility(8);
            this.mRadialTimePickerHeader.setVisibility(8);
            this.mTextInputPickerHeader.setVisibility(0);
            this.mTextInputPickerView.setVisibility(0);
            this.mRadialTimePickerModeButton.setImageResource(17301787);
            this.mRadialTimePickerModeButton.setContentDescription(this.mRadialTimePickerModeEnabledDescription);
            this.mRadialPickerModeEnabled = false;
            return;
        }
        this.mRadialTimePickerView.setVisibility(0);
        this.mRadialTimePickerHeader.setVisibility(0);
        this.mTextInputPickerHeader.setVisibility(8);
        this.mTextInputPickerView.setVisibility(8);
        this.mRadialTimePickerModeButton.setImageResource(17301864);
        this.mRadialTimePickerModeButton.setContentDescription(this.mTextInputPickerModeEnabledDescription);
        updateTextInputPicker();
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        if ((r5 + 1) >= r1) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003c, code lost:
        if (r8 != r0.charAt(r5 + 1)) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        r2 = true;
     */
    private void updateHourFormat() {
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24Hour ? "Hm" : "hm");
        int lengthPattern = bestDateTimePattern.length();
        boolean showLeadingZero = false;
        char hourFormat = 0;
        int i = 0;
        while (true) {
            if (i >= lengthPattern) {
                break;
            }
            char c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == 'h' || c == 'K' || c == 'k') {
                hourFormat = c;
            } else {
                i++;
            }
        }
        this.mHourFormatShowLeadingZero = showLeadingZero;
        this.mHourFormatStartsAtZero = hourFormat == 'K' || hourFormat == 'H';
        int minHour = 1 ^ this.mHourFormatStartsAtZero;
        this.mHourView.setRange(minHour, (this.mIs24Hour ? 23 : 11) + ((int) minHour));
        this.mHourView.setShowLeadingZeroes(this.mHourFormatShowLeadingZero);
        String[] digits = DecimalFormatSymbols.getInstance(this.mLocale).getDigitStrings();
        int maxCharLength = 0;
        for (int i2 = 0; i2 < 10; i2++) {
            maxCharLength = Math.max(maxCharLength, digits[i2].length());
        }
        this.mTextInputPickerView.setHourFormat(maxCharLength * 2);
    }

    static final CharSequence obtainVerbatim(String text) {
        return new SpannableStringBuilder().append((CharSequence) text, (Object) new TtsSpan.VerbatimBuilder(text).build(), 0);
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
        setAmPmStart(DateFormat.getBestDateTimePattern(this.mLocale, "hm").startsWith("a"));
        updateAmPmLabelStates(this.mCurrentHour < 12 ? 0 : 1);
    }

    private void setAmPmStart(boolean isAmPmAtStart) {
        boolean isAmPmAtLeft;
        int otherViewId;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.mAmPmLayout.getLayoutParams();
        if (params.getRule(1) != 0 || params.getRule(0) != 0) {
            if (TextUtils.getLayoutDirectionFromLocale(this.mLocale) == 0) {
                isAmPmAtLeft = isAmPmAtStart;
            } else {
                isAmPmAtLeft = !isAmPmAtStart;
            }
            if (this.mIsAmPmAtLeft != isAmPmAtLeft) {
                if (isAmPmAtLeft) {
                    params.removeRule(1);
                    params.addRule(0, this.mHourView.getId());
                } else {
                    params.removeRule(0);
                    params.addRule(1, this.mMinuteView.getId());
                }
                this.mIsAmPmAtLeft = isAmPmAtLeft;
            } else {
                return;
            }
        } else if (!(params.getRule(3) == 0 && params.getRule(2) == 0)) {
            if (this.mIsAmPmAtTop != isAmPmAtStart) {
                if (isAmPmAtStart) {
                    otherViewId = params.getRule(3);
                    params.removeRule(3);
                    params.addRule(2, otherViewId);
                } else {
                    otherViewId = params.getRule(2);
                    params.removeRule(2);
                    params.addRule(3, otherViewId);
                }
                View otherView = this.mRadialTimePickerHeader.findViewById(otherViewId);
                otherView.setPadding(otherView.getPaddingLeft(), otherView.getPaddingBottom(), otherView.getPaddingRight(), otherView.getPaddingTop());
                this.mIsAmPmAtTop = isAmPmAtStart;
            } else {
                return;
            }
        }
        this.mAmPmLayout.setLayoutParams(params);
    }

    public void setDate(int hour, int minute) {
        setHourInternal(hour, 0, true, false);
        setMinuteInternal(minute, 0, false);
        onTimeChanged();
    }

    public void setHour(int hour) {
        setHourInternal(hour, 0, true, true);
    }

    /* access modifiers changed from: private */
    public void setHourInternal(int hour, int source, boolean announce, boolean notify) {
        if (this.mCurrentHour != hour) {
            resetAutofilledValue();
            this.mCurrentHour = hour;
            updateHeaderHour(hour, announce);
            updateHeaderAmPm();
            int i = 1;
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
            if (notify) {
                onTimeChanged();
            }
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
        setMinuteInternal(minute, 0, true);
    }

    /* access modifiers changed from: private */
    public void setMinuteInternal(int minute, int source, boolean notify) {
        if (this.mCurrentMinute != minute) {
            resetAutofilledValue();
            this.mCurrentMinute = minute;
            updateHeaderMinute(minute, true);
            if (source != 1) {
                this.mRadialTimePickerView.setCurrentMinute(minute);
            }
            if (source != 2) {
                updateTextInputPicker();
            }
            this.mDelegator.invalidate();
            if (notify) {
                onTimeChanged();
            }
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
        TimePicker.AbstractTimePickerDelegate.SavedState savedState = new TimePicker.AbstractTimePickerDelegate.SavedState(superState, getHour(), getMinute(), is24Hour(), getCurrentItemShowing());
        return savedState;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof TimePicker.AbstractTimePickerDelegate.SavedState) {
            TimePicker.AbstractTimePickerDelegate.SavedState ss = (TimePicker.AbstractTimePickerDelegate.SavedState) state;
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
            flags = 1 | 128;
        } else {
            flags = 1 | 64;
        }
        this.mTempCalendar.set(11, getHour());
        this.mTempCalendar.set(12, getMinute());
        String selectedTime = DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags);
        String selectionMode = this.mRadialTimePickerView.getCurrentItemShowing() == 0 ? this.mSelectHours : this.mSelectMinutes;
        List<CharSequence> text = event.getText();
        text.add(selectedTime + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + selectionMode);
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

    /* access modifiers changed from: private */
    public void tryVibrate() {
        this.mDelegator.performHapticFeedback(4);
    }

    private void updateAmPmLabelStates(int amOrPm) {
        boolean isPm = false;
        boolean isAm = amOrPm == 0;
        this.mAmLabel.setActivated(isAm);
        this.mAmLabel.setChecked(isAm);
        if (amOrPm == 1) {
            isPm = true;
        }
        this.mPmLabel.setActivated(isPm);
        this.mPmLabel.setChecked(isPm);
    }

    /* access modifiers changed from: private */
    public int getLocalizedHour(int hourOfDay) {
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
        String separatorText = getHourMinSeparatorFromPattern(DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24Hour ? "Hm" : "hm"));
        this.mSeparatorView.setText((CharSequence) separatorText);
        this.mTextInputPickerView.updateSeparator(separatorText);
    }

    private static String getHourMinSeparatorFromPattern(String dateTimePattern) {
        boolean foundHourPattern = false;
        for (int i = 0; i < dateTimePattern.length(); i++) {
            char charAt = dateTimePattern.charAt(i);
            if (charAt != ' ') {
                if (charAt != '\'') {
                    if (charAt == 'H' || charAt == 'K' || charAt == 'h' || charAt == 'k') {
                        foundHourPattern = true;
                    } else if (foundHourPattern) {
                        return Character.toString(dateTimePattern.charAt(i));
                    }
                } else if (foundHourPattern) {
                    SpannableStringBuilder quotedSubstring = new SpannableStringBuilder(dateTimePattern.substring(i));
                    return quotedSubstring.subSequence(0, DateFormat.appendQuotedText(quotedSubstring, 0)).toString();
                }
            }
        }
        return SettingsStringUtil.DELIMITER;
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
        if (this.mLastAnnouncedIsHour != isHour || !text.equals(this.mLastAnnouncedText)) {
            this.mDelegator.announceForAccessibility(text);
            this.mLastAnnouncedText = text;
            this.mLastAnnouncedIsHour = isHour;
        }
    }

    /* access modifiers changed from: private */
    public void setCurrentItemShowing(int index, boolean animateCircle, boolean announce) {
        this.mRadialTimePickerView.setCurrentItemShowing(index, animateCircle);
        if (index == 0) {
            if (announce) {
                this.mDelegator.announceForAccessibility(this.mSelectHours);
            }
        } else if (announce) {
            this.mDelegator.announceForAccessibility(this.mSelectMinutes);
        }
        boolean z = false;
        this.mHourView.setActivated(index == 0);
        NumericTextView numericTextView = this.mMinuteView;
        if (index == 1) {
            z = true;
        }
        numericTextView.setActivated(z);
    }

    /* access modifiers changed from: private */
    public void setAmOrPm(int amOrPm) {
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
