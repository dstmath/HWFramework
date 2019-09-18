package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import com.android.internal.R;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

class TimePickerSpinnerDelegate extends TimePicker.AbstractTimePickerDelegate {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int HOURS_IN_HALF_DAY = 12;
    private final Button mAmPmButton;
    private final NumberPicker mAmPmSpinner;
    private final EditText mAmPmSpinnerInput;
    private final String[] mAmPmStrings;
    private final TextView mDivider;
    private char mHourFormat;
    /* access modifiers changed from: private */
    public final NumberPicker mHourSpinner;
    private final EditText mHourSpinnerInput;
    private boolean mHourWithTwoDigit;
    private boolean mIs24HourView;
    /* access modifiers changed from: private */
    public boolean mIsAm;
    private boolean mIsEnabled = true;
    /* access modifiers changed from: private */
    public final NumberPicker mMinuteSpinner;
    private final EditText mMinuteSpinnerInput;
    private final Calendar mTempCalendar;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TimePickerSpinnerDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        TimePicker timePicker = delegator;
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        int layoutResourceId = a.getResourceId(13, 17367317);
        a.recycle();
        LayoutInflater.from(this.mContext).inflate(layoutResourceId, (ViewGroup) this.mDelegator, true).setSaveFromParentEnabled(false);
        this.mHourSpinner = (NumberPicker) timePicker.findViewById(16908973);
        this.mHourSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                TimePickerSpinnerDelegate.this.updateInputState();
                if (!TimePickerSpinnerDelegate.this.is24Hour() && ((oldVal == 11 && newVal == 12) || (oldVal == 12 && newVal == 11))) {
                    boolean unused = TimePickerSpinnerDelegate.this.mIsAm = !TimePickerSpinnerDelegate.this.mIsAm;
                    TimePickerSpinnerDelegate.this.updateAmPmControl();
                }
                TimePickerSpinnerDelegate.this.onTimeChanged();
            }
        });
        this.mHourSpinnerInput = (EditText) this.mHourSpinner.findViewById(16909153);
        this.mHourSpinnerInput.setImeOptions(5);
        this.mDivider = (TextView) this.mDelegator.findViewById(16908868);
        if (this.mDivider != null) {
            setDividerText();
        }
        this.mMinuteSpinner = (NumberPicker) this.mDelegator.findViewById(16909092);
        this.mMinuteSpinner.setMinValue(0);
        this.mMinuteSpinner.setMaxValue(59);
        this.mMinuteSpinner.setOnLongPressUpdateInterval(100);
        this.mMinuteSpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        this.mMinuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                TimePickerSpinnerDelegate.this.updateInputState();
                int minValue = TimePickerSpinnerDelegate.this.mMinuteSpinner.getMinValue();
                int maxValue = TimePickerSpinnerDelegate.this.mMinuteSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = TimePickerSpinnerDelegate.this.mHourSpinner.getValue() + 1;
                    if (!TimePickerSpinnerDelegate.this.is24Hour() && newHour == 12) {
                        boolean unused = TimePickerSpinnerDelegate.this.mIsAm = !TimePickerSpinnerDelegate.this.mIsAm;
                        TimePickerSpinnerDelegate.this.updateAmPmControl();
                    }
                    TimePickerSpinnerDelegate.this.mHourSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour2 = TimePickerSpinnerDelegate.this.mHourSpinner.getValue() - 1;
                    if (!TimePickerSpinnerDelegate.this.is24Hour() && newHour2 == 11) {
                        boolean unused2 = TimePickerSpinnerDelegate.this.mIsAm = !TimePickerSpinnerDelegate.this.mIsAm;
                        TimePickerSpinnerDelegate.this.updateAmPmControl();
                    }
                    TimePickerSpinnerDelegate.this.mHourSpinner.setValue(newHour2);
                }
                TimePickerSpinnerDelegate.this.onTimeChanged();
            }
        });
        this.mMinuteSpinnerInput = (EditText) this.mMinuteSpinner.findViewById(16909153);
        this.mMinuteSpinnerInput.setImeOptions(5);
        this.mAmPmStrings = getAmPmStrings(context);
        View amPmView = this.mDelegator.findViewById(16908732);
        if (amPmView instanceof Button) {
            this.mAmPmSpinner = null;
            this.mAmPmSpinnerInput = null;
            this.mAmPmButton = (Button) amPmView;
            this.mAmPmButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    boolean unused = TimePickerSpinnerDelegate.this.mIsAm = !TimePickerSpinnerDelegate.this.mIsAm;
                    TimePickerSpinnerDelegate.this.updateAmPmControl();
                    TimePickerSpinnerDelegate.this.onTimeChanged();
                }
            });
        } else {
            this.mAmPmButton = null;
            this.mAmPmSpinner = (NumberPicker) amPmView;
            this.mAmPmSpinner.setMinValue(0);
            this.mAmPmSpinner.setMaxValue(1);
            this.mAmPmSpinner.setDisplayedValues(this.mAmPmStrings);
            this.mAmPmSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    TimePickerSpinnerDelegate.this.updateInputState();
                    picker.requestFocus();
                    boolean unused = TimePickerSpinnerDelegate.this.mIsAm = !TimePickerSpinnerDelegate.this.mIsAm;
                    TimePickerSpinnerDelegate.this.updateAmPmControl();
                    TimePickerSpinnerDelegate.this.onTimeChanged();
                }
            });
            this.mAmPmSpinnerInput = (EditText) this.mAmPmSpinner.findViewById(16909153);
            this.mAmPmSpinnerInput.setImeOptions(6);
        }
        if (isAmPmAtStart()) {
            ViewGroup amPmParent = (ViewGroup) timePicker.findViewById(16909435);
            amPmParent.removeView(amPmView);
            amPmParent.addView(amPmView, 0);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) amPmView.getLayoutParams();
            int startMargin = lp.getMarginStart();
            int endMargin = lp.getMarginEnd();
            if (startMargin != endMargin) {
                lp.setMarginStart(endMargin);
                lp.setMarginEnd(startMargin);
            }
        }
        getHourFormatData();
        updateHourControl();
        updateMinuteControl();
        updateAmPmControl();
        this.mTempCalendar = Calendar.getInstance(this.mLocale);
        setHour(this.mTempCalendar.get(11));
        setMinute(this.mTempCalendar.get(12));
        if (!isEnabled()) {
            setEnabled(false);
        }
        setContentDescriptions();
        if (this.mDelegator.getImportantForAccessibility() == 0) {
            this.mDelegator.setImportantForAccessibility(1);
        }
    }

    public boolean validateInput() {
        return true;
    }

    private void getHourFormatData() {
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24HourView ? "Hm" : "hm");
        int lengthPattern = bestDateTimePattern.length();
        this.mHourWithTwoDigit = false;
        for (int i = 0; i < lengthPattern; i++) {
            char c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == 'h' || c == 'K' || c == 'k') {
                this.mHourFormat = c;
                if (i + 1 < lengthPattern && c == bestDateTimePattern.charAt(i + 1)) {
                    this.mHourWithTwoDigit = true;
                    return;
                }
                return;
            }
        }
    }

    private boolean isAmPmAtStart() {
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, "hm");
        Locale defaultLocale = Locale.getDefault();
        if ("ur".equals(defaultLocale.getLanguage())) {
            return bestDateTimePattern.startsWith("a");
        }
        boolean z = true;
        if (!bestDateTimePattern.startsWith("a") && TextUtils.getLayoutDirectionFromLocale(defaultLocale) != 1) {
            z = false;
        }
        return z;
    }

    private void setDividerText() {
        String separatorText;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24HourView ? "Hm" : "hm");
        int hourIndex = bestDateTimePattern.lastIndexOf(72);
        if (hourIndex == -1) {
            hourIndex = bestDateTimePattern.lastIndexOf(104);
        }
        if (hourIndex == -1) {
            separatorText = SettingsStringUtil.DELIMITER;
        } else {
            int minuteIndex = bestDateTimePattern.indexOf(109, hourIndex + 1);
            if (minuteIndex == -1) {
                separatorText = Character.toString(bestDateTimePattern.charAt(hourIndex + 1));
            } else {
                separatorText = bestDateTimePattern.substring(hourIndex + 1, minuteIndex);
            }
        }
        this.mDivider.setText((CharSequence) separatorText);
    }

    public void setDate(int hour, int minute) {
        setCurrentHour(hour, false);
        setCurrentMinute(minute, false);
        onTimeChanged();
    }

    public void setHour(int hour) {
        setCurrentHour(hour, true);
    }

    private void setCurrentHour(int currentHour, boolean notifyTimeChanged) {
        if (currentHour != getHour()) {
            resetAutofilledValue();
            if (!is24Hour()) {
                if (currentHour >= 12) {
                    this.mIsAm = false;
                    if (currentHour > 12) {
                        currentHour -= 12;
                    }
                } else {
                    this.mIsAm = true;
                    if (currentHour == 0) {
                        currentHour = 12;
                    }
                }
                updateAmPmControl();
            }
            this.mHourSpinner.setValue(currentHour);
            if (notifyTimeChanged) {
                onTimeChanged();
            }
        }
    }

    public int getHour() {
        int currentHour = this.mHourSpinner.getValue();
        if (is24Hour()) {
            return currentHour;
        }
        if (this.mIsAm) {
            return currentHour % 12;
        }
        return (currentHour % 12) + 12;
    }

    public void setMinute(int minute) {
        setCurrentMinute(minute, true);
    }

    private void setCurrentMinute(int minute, boolean notifyTimeChanged) {
        if (minute != getMinute()) {
            resetAutofilledValue();
            this.mMinuteSpinner.setValue(minute);
            if (notifyTimeChanged) {
                onTimeChanged();
            }
        }
    }

    public int getMinute() {
        return this.mMinuteSpinner.getValue();
    }

    public void setIs24Hour(boolean is24Hour) {
        if (this.mIs24HourView != is24Hour) {
            int currentHour = getHour();
            this.mIs24HourView = is24Hour;
            getHourFormatData();
            updateHourControl();
            setCurrentHour(currentHour, false);
            updateMinuteControl();
            updateAmPmControl();
        }
    }

    public boolean is24Hour() {
        return this.mIs24HourView;
    }

    public void setEnabled(boolean enabled) {
        this.mMinuteSpinner.setEnabled(enabled);
        if (this.mDivider != null) {
            this.mDivider.setEnabled(enabled);
        }
        this.mHourSpinner.setEnabled(enabled);
        if (this.mAmPmSpinner != null) {
            this.mAmPmSpinner.setEnabled(enabled);
        } else {
            this.mAmPmButton.setEnabled(enabled);
        }
        this.mIsEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public int getBaseline() {
        return this.mHourSpinner.getBaseline();
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        return new TimePicker.AbstractTimePickerDelegate.SavedState(superState, getHour(), getMinute(), is24Hour());
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof TimePicker.AbstractTimePickerDelegate.SavedState) {
            TimePicker.AbstractTimePickerDelegate.SavedState ss = (TimePicker.AbstractTimePickerDelegate.SavedState) state;
            setHour(ss.getHour());
            setMinute(ss.getMinute());
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        if (this.mIs24HourView) {
            flags = 1 | 128;
        } else {
            flags = 1 | 64;
        }
        this.mTempCalendar.set(11, getHour());
        this.mTempCalendar.set(12, getMinute());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags));
    }

    public View getHourView() {
        return this.mHourSpinnerInput;
    }

    public View getMinuteView() {
        return this.mMinuteSpinnerInput;
    }

    public View getAmView() {
        return this.mAmPmSpinnerInput;
    }

    public View getPmView() {
        return this.mAmPmSpinnerInput;
    }

    /* access modifiers changed from: private */
    public void updateInputState() {
        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
        if (inputMethodManager == null) {
            return;
        }
        if (inputMethodManager.isActive(this.mHourSpinnerInput)) {
            this.mHourSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mMinuteSpinnerInput)) {
            this.mMinuteSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mAmPmSpinnerInput)) {
            this.mAmPmSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
        }
    }

    /* access modifiers changed from: private */
    public void updateAmPmControl() {
        if (!is24Hour()) {
            int index = !this.mIsAm;
            if (this.mAmPmSpinner != null) {
                this.mAmPmSpinner.setValue(index);
                this.mAmPmSpinner.setVisibility(0);
            } else {
                this.mAmPmButton.setText((CharSequence) this.mAmPmStrings[index]);
                this.mAmPmButton.setVisibility(0);
            }
        } else if (this.mAmPmSpinner != null) {
            this.mAmPmSpinner.setVisibility(8);
        } else {
            this.mAmPmButton.setVisibility(8);
        }
        this.mDelegator.sendAccessibilityEvent(4);
    }

    /* access modifiers changed from: private */
    public void onTimeChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
        if (this.mAutoFillChangeListener != null) {
            this.mAutoFillChangeListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
    }

    private void updateHourControl() {
        if (is24Hour()) {
            if (this.mHourFormat == 'k') {
                this.mHourSpinner.setMinValue(1);
                this.mHourSpinner.setMaxValue(24);
            } else {
                this.mHourSpinner.setMinValue(0);
                this.mHourSpinner.setMaxValue(23);
            }
        } else if (this.mHourFormat == 'K') {
            this.mHourSpinner.setMinValue(0);
            this.mHourSpinner.setMaxValue(11);
        } else {
            this.mHourSpinner.setMinValue(1);
            this.mHourSpinner.setMaxValue(12);
        }
        this.mHourSpinner.setFormatter(this.mHourWithTwoDigit ? NumberPicker.getTwoDigitFormatter() : null);
    }

    private void updateMinuteControl() {
        if (is24Hour()) {
            this.mMinuteSpinnerInput.setImeOptions(6);
        } else {
            this.mMinuteSpinnerInput.setImeOptions(5);
        }
    }

    private void setContentDescriptions() {
        trySetContentDescription(this.mMinuteSpinner, 16908995, 17041253);
        trySetContentDescription(this.mMinuteSpinner, 16908855, 17041247);
        trySetContentDescription(this.mHourSpinner, 16908995, 17041252);
        trySetContentDescription(this.mHourSpinner, 16908855, 17041246);
        if (this.mAmPmSpinner != null) {
            trySetContentDescription(this.mAmPmSpinner, 16908995, 17041254);
            trySetContentDescription(this.mAmPmSpinner, 16908855, 17041248);
        }
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(this.mContext.getString(contDescResId));
        }
    }

    public static String[] getAmPmStrings(Context context) {
        return LocaleData.get(context.getResources().getConfiguration().locale).amPm;
    }
}
