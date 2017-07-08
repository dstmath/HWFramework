package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.NumberPicker.Formatter;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TimePicker.OnTimeChangedListener;
import com.android.internal.R;
import com.huawei.pgmng.log.LogPower;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

class TimePickerSpinnerDelegate extends AbstractTimePickerDelegate {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int HOURS_IN_HALF_DAY = 12;
    private final Button mAmPmButton;
    private final NumberPicker mAmPmSpinner;
    private final EditText mAmPmSpinnerInput;
    private final String[] mAmPmStrings;
    private final TextView mDivider;
    private char mHourFormat;
    private final NumberPicker mHourSpinner;
    private final EditText mHourSpinnerInput;
    private boolean mHourWithTwoDigit;
    private boolean mIs24HourView;
    private boolean mIsAm;
    private boolean mIsEnabled;
    private final NumberPicker mMinuteSpinner;
    private final EditText mMinuteSpinnerInput;
    private final Calendar mTempCalendar;

    public TimePickerSpinnerDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        this.mIsEnabled = DEFAULT_ENABLED_STATE;
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        int layoutResourceId = a.getResourceId(HOURS_IN_HALF_DAY, R.layout.time_picker_legacy);
        a.recycle();
        LayoutInflater.from(this.mContext).inflate(layoutResourceId, this.mDelegator, (boolean) DEFAULT_ENABLED_STATE);
        this.mHourSpinner = (NumberPicker) delegator.findViewById(R.id.hour);
        this.mHourSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                TimePickerSpinnerDelegate.this.updateInputState();
                if (!TimePickerSpinnerDelegate.this.is24Hour()) {
                    if (!(oldVal == 11 && newVal == TimePickerSpinnerDelegate.HOURS_IN_HALF_DAY)) {
                        if (oldVal == TimePickerSpinnerDelegate.HOURS_IN_HALF_DAY && newVal == 11) {
                        }
                    }
                    TimePickerSpinnerDelegate.this.mIsAm = TimePickerSpinnerDelegate.this.mIsAm ? false : TimePickerSpinnerDelegate.DEFAULT_ENABLED_STATE;
                    TimePickerSpinnerDelegate.this.updateAmPmControl();
                }
                TimePickerSpinnerDelegate.this.onTimeChanged();
            }
        });
        this.mHourSpinnerInput = (EditText) this.mHourSpinner.findViewById(R.id.numberpicker_input);
        this.mHourSpinnerInput.setImeOptions(5);
        this.mDivider = (TextView) this.mDelegator.findViewById(R.id.divider);
        if (this.mDivider != null) {
            setDividerText();
        }
        this.mMinuteSpinner = (NumberPicker) this.mDelegator.findViewById(R.id.minute);
        this.mMinuteSpinner.setMinValue(0);
        this.mMinuteSpinner.setMaxValue(59);
        this.mMinuteSpinner.setOnLongPressUpdateInterval(100);
        this.mMinuteSpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        this.mMinuteSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                boolean z = false;
                if (!TimePickerSpinnerDelegate.this.mDelegator.hwValueChange()) {
                    TimePickerSpinnerDelegate.this.updateInputState();
                    int minValue = TimePickerSpinnerDelegate.this.mMinuteSpinner.getMinValue();
                    int maxValue = TimePickerSpinnerDelegate.this.mMinuteSpinner.getMaxValue();
                    int newHour;
                    TimePickerSpinnerDelegate timePickerSpinnerDelegate;
                    if (oldVal == maxValue && newVal == minValue) {
                        newHour = TimePickerSpinnerDelegate.this.mHourSpinner.getValue() + 1;
                        if (!TimePickerSpinnerDelegate.this.is24Hour() && newHour == TimePickerSpinnerDelegate.HOURS_IN_HALF_DAY) {
                            timePickerSpinnerDelegate = TimePickerSpinnerDelegate.this;
                            if (!TimePickerSpinnerDelegate.this.mIsAm) {
                                z = TimePickerSpinnerDelegate.DEFAULT_ENABLED_STATE;
                            }
                            timePickerSpinnerDelegate.mIsAm = z;
                            TimePickerSpinnerDelegate.this.updateAmPmControl();
                        }
                        TimePickerSpinnerDelegate.this.mHourSpinner.setValue(newHour);
                    } else if (oldVal == minValue && newVal == maxValue) {
                        newHour = TimePickerSpinnerDelegate.this.mHourSpinner.getValue() - 1;
                        if (!TimePickerSpinnerDelegate.this.is24Hour() && newHour == 11) {
                            timePickerSpinnerDelegate = TimePickerSpinnerDelegate.this;
                            if (!TimePickerSpinnerDelegate.this.mIsAm) {
                                z = TimePickerSpinnerDelegate.DEFAULT_ENABLED_STATE;
                            }
                            timePickerSpinnerDelegate.mIsAm = z;
                            TimePickerSpinnerDelegate.this.updateAmPmControl();
                        }
                        TimePickerSpinnerDelegate.this.mHourSpinner.setValue(newHour);
                    }
                    TimePickerSpinnerDelegate.this.onTimeChanged();
                }
            }
        });
        this.mMinuteSpinnerInput = (EditText) this.mMinuteSpinner.findViewById(R.id.numberpicker_input);
        this.mMinuteSpinnerInput.setImeOptions(5);
        this.mAmPmStrings = getAmPmStrings(context);
        View amPmView = this.mDelegator.findViewById(R.id.amPm);
        if (amPmView instanceof Button) {
            this.mAmPmSpinner = null;
            this.mAmPmSpinnerInput = null;
            this.mAmPmButton = (Button) amPmView;
            this.mAmPmButton.setOnClickListener(new OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    TimePickerSpinnerDelegate.this.mIsAm = TimePickerSpinnerDelegate.this.mIsAm ? false : TimePickerSpinnerDelegate.DEFAULT_ENABLED_STATE;
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
            this.mAmPmSpinner.setOnValueChangedListener(new OnValueChangeListener() {
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    TimePickerSpinnerDelegate.this.updateInputState();
                    picker.requestFocus();
                    TimePickerSpinnerDelegate.this.mIsAm = TimePickerSpinnerDelegate.this.mIsAm ? false : TimePickerSpinnerDelegate.DEFAULT_ENABLED_STATE;
                    TimePickerSpinnerDelegate.this.updateAmPmControl();
                    TimePickerSpinnerDelegate.this.onTimeChanged();
                }
            });
            this.mAmPmSpinnerInput = (EditText) this.mAmPmSpinner.findViewById(R.id.numberpicker_input);
            this.mAmPmSpinnerInput.setImeOptions(6);
        }
        if (isAmPmAtStart()) {
            ViewGroup amPmParent = (ViewGroup) delegator.findViewById(R.id.timePickerLayout);
            amPmParent.removeView(amPmView);
            this.mDelegator.updateAmPmStart(amPmParent);
            amPmParent.addView(amPmView, 0);
            MarginLayoutParams lp = (MarginLayoutParams) amPmView.getLayoutParams();
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
        setMinute(this.mTempCalendar.get(HOURS_IN_HALF_DAY));
        if (!isEnabled()) {
            setEnabled(false);
        }
        setContentDescriptions();
        if (this.mDelegator.getImportantForAccessibility() == 0) {
            this.mDelegator.setImportantForAccessibility(1);
        }
    }

    private void getHourFormatData() {
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24HourView ? "Hm" : "hm");
        int lengthPattern = bestDateTimePattern.length();
        this.mHourWithTwoDigit = false;
        int i = 0;
        while (i < lengthPattern) {
            char c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == DateFormat.HOUR || c == 'K' || c == DateFormat.HOUR_OF_DAY) {
                this.mHourFormat = c;
                if (i + 1 < lengthPattern && c == bestDateTimePattern.charAt(i + 1)) {
                    this.mHourWithTwoDigit = DEFAULT_ENABLED_STATE;
                    return;
                }
                return;
            }
            i++;
        }
    }

    private boolean isAmPmAtStart() {
        boolean z = DEFAULT_ENABLED_STATE;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, "hm");
        Locale defaultLocale = Locale.getDefault();
        if ("ur".equals(defaultLocale.getLanguage())) {
            return bestDateTimePattern.startsWith("a");
        }
        if (!(bestDateTimePattern.startsWith("a") || TextUtils.getLayoutDirectionFromLocale(defaultLocale) == 1)) {
            z = false;
        }
        return z;
    }

    private void setDividerText() {
        CharSequence separatorText;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mLocale, this.mIs24HourView ? "Hm" : "hm");
        int hourIndex = bestDateTimePattern.lastIndexOf(72);
        if (hourIndex == -1) {
            hourIndex = bestDateTimePattern.lastIndexOf(LogPower.WEBPAGE_STARTED);
        }
        if (hourIndex == -1) {
            separatorText = ":";
        } else {
            int minuteIndex = bestDateTimePattern.indexOf(LogPower.APP_LAUNCHER, hourIndex + 1);
            if (minuteIndex == -1) {
                separatorText = Character.toString(bestDateTimePattern.charAt(hourIndex + 1));
            } else {
                separatorText = bestDateTimePattern.substring(hourIndex + 1, minuteIndex);
            }
        }
        this.mDivider.setText(separatorText);
    }

    public void setHour(int hour) {
        setCurrentHour(hour, DEFAULT_ENABLED_STATE);
    }

    private void setCurrentHour(int currentHour, boolean notifyTimeChanged) {
        if (currentHour != getHour()) {
            if (!is24Hour()) {
                if (currentHour >= HOURS_IN_HALF_DAY) {
                    this.mIsAm = false;
                    if (currentHour > HOURS_IN_HALF_DAY) {
                        currentHour -= 12;
                    }
                } else {
                    this.mIsAm = DEFAULT_ENABLED_STATE;
                    if (currentHour == 0) {
                        currentHour = HOURS_IN_HALF_DAY;
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
            return currentHour % HOURS_IN_HALF_DAY;
        }
        return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
    }

    public void setMinute(int minute) {
        if (minute != getMinute()) {
            this.mMinuteSpinner.setValue(minute);
            onTimeChanged();
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

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mOnTimeChangedListener = onTimeChangedListener;
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
        return new SavedState(superState, getHour(), getMinute(), is24Hour());
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            setHour(ss.getHour());
            setMinute(ss.getMinute());
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return DEFAULT_ENABLED_STATE;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        if (this.mIs24HourView) {
            flags = LogPower.START_CAMERA;
        } else {
            flags = 65;
        }
        this.mTempCalendar.set(11, getHour());
        this.mTempCalendar.set(HOURS_IN_HALF_DAY, getMinute());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags));
    }

    private void updateInputState() {
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

    protected void updateAmPmControl() {
        if (!is24Hour()) {
            int index = this.mIsAm ? 0 : 1;
            if (this.mAmPmSpinner != null) {
                this.mAmPmSpinner.setValue(index);
                this.mAmPmSpinner.setVisibility(0);
            } else {
                this.mAmPmButton.setText(this.mAmPmStrings[index]);
                this.mAmPmButton.setVisibility(0);
            }
        } else if (this.mAmPmSpinner != null) {
            this.mAmPmSpinner.setVisibility(8);
        } else {
            this.mAmPmButton.setVisibility(8);
        }
        this.mDelegator.sendAccessibilityEvent(4);
    }

    private void onTimeChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getHour(), getMinute());
        }
    }

    private void updateHourControl() {
        Formatter twoDigitFormatter;
        if (is24Hour()) {
            if (this.mHourFormat == DateFormat.HOUR_OF_DAY) {
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
            this.mHourSpinner.setMaxValue(HOURS_IN_HALF_DAY);
        }
        NumberPicker numberPicker = this.mHourSpinner;
        if (this.mHourWithTwoDigit) {
            twoDigitFormatter = NumberPicker.getTwoDigitFormatter();
        } else {
            twoDigitFormatter = null;
        }
        numberPicker.setFormatter(twoDigitFormatter);
    }

    private void updateMinuteControl() {
        if (is24Hour()) {
            this.mMinuteSpinnerInput.setImeOptions(6);
        } else {
            this.mMinuteSpinnerInput.setImeOptions(5);
        }
    }

    private void setContentDescriptions() {
        trySetContentDescription(this.mMinuteSpinner, R.id.increment, R.string.time_picker_increment_minute_button);
        trySetContentDescription(this.mMinuteSpinner, R.id.decrement, R.string.time_picker_decrement_minute_button);
        trySetContentDescription(this.mHourSpinner, R.id.increment, R.string.time_picker_increment_hour_button);
        trySetContentDescription(this.mHourSpinner, R.id.decrement, R.string.time_picker_decrement_hour_button);
        if (this.mAmPmSpinner != null) {
            trySetContentDescription(this.mAmPmSpinner, R.id.increment, R.string.time_picker_increment_set_pm_button);
            trySetContentDescription(this.mAmPmSpinner, R.id.decrement, R.string.time_picker_decrement_set_am_button);
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

    protected void onTimeChangedOuter() {
        onTimeChanged();
    }

    protected void updateInputStateOuter() {
        updateInputState();
    }

    protected NumberPicker getHourSpinner() {
        return this.mHourSpinner;
    }

    protected NumberPicker getMinuteSpinner() {
        return this.mMinuteSpinner;
    }

    protected NumberPicker getAmPmSpinner() {
        return this.mAmPmSpinner;
    }
}
