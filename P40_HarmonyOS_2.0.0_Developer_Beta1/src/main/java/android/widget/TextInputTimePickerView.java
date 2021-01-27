package android.widget;

import android.content.Context;
import android.os.LocaleList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import com.android.internal.R;

public class TextInputTimePickerView extends RelativeLayout {
    private static final int AM = 0;
    public static final int AMPM = 2;
    public static final int HOURS = 0;
    public static final int MINUTES = 1;
    private static final int PM = 1;
    private final Spinner mAmPmSpinner;
    private final TextView mErrorLabel;
    private boolean mErrorShowing;
    private final EditText mHourEditText;
    private boolean mHourFormatStartsAtZero;
    private final TextView mHourLabel;
    private final TextView mInputSeparatorView;
    private boolean mIs24Hour;
    private OnValueTypedListener mListener;
    private final EditText mMinuteEditText;
    private final TextView mMinuteLabel;
    private boolean mTimeSet;

    /* access modifiers changed from: package-private */
    public interface OnValueTypedListener {
        void onValueChanged(int i, int i2);
    }

    public TextInputTimePickerView(Context context) {
        this(context, null);
    }

    public TextInputTimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextInputTimePickerView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public TextInputTimePickerView(final Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        inflate(context, R.layout.time_picker_text_input_material, this);
        this.mHourEditText = (EditText) findViewById(R.id.input_hour);
        this.mMinuteEditText = (EditText) findViewById(R.id.input_minute);
        this.mInputSeparatorView = (TextView) findViewById(R.id.input_separator);
        this.mErrorLabel = (TextView) findViewById(R.id.label_error);
        this.mHourLabel = (TextView) findViewById(R.id.label_hour);
        this.mMinuteLabel = (TextView) findViewById(R.id.label_minute);
        this.mHourEditText.addTextChangedListener(new TextWatcher() {
            /* class android.widget.TextInputTimePickerView.AnonymousClass1 */

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                if (TextInputTimePickerView.this.parseAndSetHourInternal(editable.toString()) && editable.length() > 1 && !((AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
                    TextInputTimePickerView.this.mMinuteEditText.requestFocus();
                }
            }
        });
        this.mMinuteEditText.addTextChangedListener(new TextWatcher() {
            /* class android.widget.TextInputTimePickerView.AnonymousClass2 */

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                TextInputTimePickerView.this.parseAndSetMinuteInternal(editable.toString());
            }
        });
        this.mAmPmSpinner = (Spinner) findViewById(R.id.am_pm_spinner);
        String[] amPmStrings = TimePicker.getAmPmStrings(context);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, 17367049);
        adapter.add(TimePickerClockDelegate.obtainVerbatim(amPmStrings[0]));
        adapter.add(TimePickerClockDelegate.obtainVerbatim(amPmStrings[1]));
        this.mAmPmSpinner.setAdapter((SpinnerAdapter) adapter);
        this.mAmPmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /* class android.widget.TextInputTimePickerView.AnonymousClass3 */

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    TextInputTimePickerView.this.mListener.onValueChanged(2, 0);
                } else {
                    TextInputTimePickerView.this.mListener.onValueChanged(2, 1);
                }
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void setListener(OnValueTypedListener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setHourFormat(int maxCharLength) {
        this.mHourEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxCharLength)});
        this.mMinuteEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxCharLength)});
        LocaleList locales = this.mContext.getResources().getConfiguration().getLocales();
        this.mHourEditText.setImeHintLocales(locales);
        this.mMinuteEditText.setImeHintLocales(locales);
    }

    /* access modifiers changed from: package-private */
    public boolean validateInput() {
        String hourText;
        String minuteText;
        if (TextUtils.isEmpty(this.mHourEditText.getText())) {
            hourText = this.mHourEditText.getHint().toString();
        } else {
            hourText = this.mHourEditText.getText().toString();
        }
        if (TextUtils.isEmpty(this.mMinuteEditText.getText())) {
            minuteText = this.mMinuteEditText.getHint().toString();
        } else {
            minuteText = this.mMinuteEditText.getText().toString();
        }
        boolean z = true;
        boolean inputValid = parseAndSetHourInternal(hourText) && parseAndSetMinuteInternal(minuteText);
        if (inputValid) {
            z = false;
        }
        setError(z);
        return inputValid;
    }

    /* access modifiers changed from: package-private */
    public void updateSeparator(String separatorText) {
        this.mInputSeparatorView.setText(separatorText);
    }

    private void setError(boolean enabled) {
        this.mErrorShowing = enabled;
        int i = 0;
        this.mErrorLabel.setVisibility(enabled ? 0 : 4);
        this.mHourLabel.setVisibility(enabled ? 4 : 0);
        TextView textView = this.mMinuteLabel;
        if (enabled) {
            i = 4;
        }
        textView.setVisibility(i);
    }

    private void setTimeSet(boolean timeSet) {
        this.mTimeSet = this.mTimeSet || timeSet;
    }

    private boolean isTimeSet() {
        return this.mTimeSet;
    }

    /* access modifiers changed from: package-private */
    public void updateTextInputValues(int localizedHour, int minute, int amOrPm, boolean is24Hour, boolean hourFormatStartsAtZero) {
        this.mIs24Hour = is24Hour;
        this.mHourFormatStartsAtZero = hourFormatStartsAtZero;
        this.mAmPmSpinner.setVisibility(is24Hour ? 4 : 0);
        if (amOrPm == 0) {
            this.mAmPmSpinner.setSelection(0);
        } else {
            this.mAmPmSpinner.setSelection(1);
        }
        if (isTimeSet()) {
            this.mHourEditText.setText(String.format("%d", Integer.valueOf(localizedHour)));
            this.mMinuteEditText.setText(String.format("%02d", Integer.valueOf(minute)));
        } else {
            this.mHourEditText.setHint(String.format("%d", Integer.valueOf(localizedHour)));
            this.mMinuteEditText.setHint(String.format("%02d", Integer.valueOf(minute)));
        }
        if (this.mErrorShowing) {
            validateInput();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean parseAndSetHourInternal(String input) {
        try {
            int hour = Integer.parseInt(input);
            int minHour = 1;
            if (!isValidLocalizedHour(hour)) {
                if (this.mHourFormatStartsAtZero) {
                    minHour = 0;
                }
                this.mListener.onValueChanged(0, getHourOfDayFromLocalizedHour(MathUtils.constrain(hour, minHour, this.mIs24Hour ? 23 : minHour + 11)));
                return false;
            }
            this.mListener.onValueChanged(0, getHourOfDayFromLocalizedHour(hour));
            setTimeSet(true);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean parseAndSetMinuteInternal(String input) {
        try {
            int minutes = Integer.parseInt(input);
            if (minutes >= 0) {
                if (minutes <= 59) {
                    this.mListener.onValueChanged(1, minutes);
                    setTimeSet(true);
                    return true;
                }
            }
            this.mListener.onValueChanged(1, MathUtils.constrain(minutes, 0, 59));
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidLocalizedHour(int localizedHour) {
        int minHour = !this.mHourFormatStartsAtZero ? 1 : 0;
        int maxHour = (this.mIs24Hour ? 23 : 11) + minHour;
        if (localizedHour < minHour || localizedHour > maxHour) {
            return false;
        }
        return true;
    }

    private int getHourOfDayFromLocalizedHour(int localizedHour) {
        int hourOfDay = localizedHour;
        if (!this.mIs24Hour) {
            if (!this.mHourFormatStartsAtZero && localizedHour == 12) {
                hourOfDay = 0;
            }
            if (this.mAmPmSpinner.getSelectedItemPosition() == 1) {
                return hourOfDay + 12;
            }
            return hourOfDay;
        } else if (this.mHourFormatStartsAtZero || localizedHour != 24) {
            return hourOfDay;
        } else {
            return 0;
        }
    }
}
