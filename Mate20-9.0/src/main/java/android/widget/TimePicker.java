package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import libcore.icu.LocaleData;

public class TimePicker extends FrameLayout {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TimePicker.class.getSimpleName();
    public static final int MODE_CLOCK = 2;
    public static final int MODE_SPINNER = 1;
    private final TimePickerDelegate mDelegate;
    private final int mMode;

    static abstract class AbstractTimePickerDelegate implements TimePickerDelegate {
        protected OnTimeChangedListener mAutoFillChangeListener;
        private long mAutofilledValue;
        protected final Context mContext;
        protected final TimePicker mDelegator;
        protected final Locale mLocale;
        protected OnTimeChangedListener mOnTimeChangedListener;

        protected static class SavedState extends View.BaseSavedState {
            public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
            private final int mCurrentItemShowing;
            private final int mHour;
            private final boolean mIs24HourMode;
            private final int mMinute;

            public SavedState(Parcelable superState, int hour, int minute, boolean is24HourMode) {
                this(superState, hour, minute, is24HourMode, 0);
            }

            public SavedState(Parcelable superState, int hour, int minute, boolean is24HourMode, int currentItemShowing) {
                super(superState);
                this.mHour = hour;
                this.mMinute = minute;
                this.mIs24HourMode = is24HourMode;
                this.mCurrentItemShowing = currentItemShowing;
            }

            private SavedState(Parcel in) {
                super(in);
                this.mHour = in.readInt();
                this.mMinute = in.readInt();
                this.mIs24HourMode = in.readInt() != 1 ? false : true;
                this.mCurrentItemShowing = in.readInt();
            }

            public int getHour() {
                return this.mHour;
            }

            public int getMinute() {
                return this.mMinute;
            }

            public boolean is24HourMode() {
                return this.mIs24HourMode;
            }

            public int getCurrentItemShowing() {
                return this.mCurrentItemShowing;
            }

            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(this.mHour);
                dest.writeInt(this.mMinute);
                dest.writeInt(this.mIs24HourMode ? 1 : 0);
                dest.writeInt(this.mCurrentItemShowing);
            }
        }

        public AbstractTimePickerDelegate(TimePicker delegator, Context context) {
            this.mDelegator = delegator;
            this.mContext = context;
            this.mLocale = context.getResources().getConfiguration().locale;
        }

        public void setOnTimeChangedListener(OnTimeChangedListener callback) {
            this.mOnTimeChangedListener = callback;
        }

        public void setAutoFillChangeListener(OnTimeChangedListener callback) {
            this.mAutoFillChangeListener = callback;
        }

        public final void autofill(AutofillValue value) {
            if (value == null || !value.isDate()) {
                String access$000 = TimePicker.LOG_TAG;
                Log.w(access$000, value + " could not be autofilled into " + this);
                return;
            }
            long time = value.getDateValue();
            Calendar cal = Calendar.getInstance(this.mLocale);
            cal.setTimeInMillis(time);
            setDate(cal.get(11), cal.get(12));
            this.mAutofilledValue = time;
        }

        public final AutofillValue getAutofillValue() {
            if (this.mAutofilledValue != 0) {
                return AutofillValue.forDate(this.mAutofilledValue);
            }
            Calendar cal = Calendar.getInstance(this.mLocale);
            cal.set(11, getHour());
            cal.set(12, getMinute());
            return AutofillValue.forDate(cal.getTimeInMillis());
        }

        /* access modifiers changed from: protected */
        public void resetAutofilledValue() {
            this.mAutofilledValue = 0;
        }
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2);
    }

    interface TimePickerDelegate {
        void autofill(AutofillValue autofillValue);

        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        View getAmView();

        AutofillValue getAutofillValue();

        int getBaseline();

        int getHour();

        View getHourView();

        int getMinute();

        View getMinuteView();

        View getPmView();

        boolean is24Hour();

        boolean isEnabled();

        void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onRestoreInstanceState(Parcelable parcelable);

        Parcelable onSaveInstanceState(Parcelable parcelable);

        void setAutoFillChangeListener(OnTimeChangedListener onTimeChangedListener);

        void setDate(int i, int i2);

        void setEnabled(boolean z);

        void setHour(int i);

        void setIs24Hour(boolean z);

        void setMinute(int i);

        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);

        boolean validateInput();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TimePickerMode {
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 16843933);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(1);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        boolean isDialogMode = a.getBoolean(10, false);
        int requestedMode = a.getInt(8, 1);
        a.recycle();
        if (requestedMode != 2 || !isDialogMode) {
            this.mMode = requestedMode;
        } else {
            this.mMode = context.getResources().getInteger(17694971);
        }
        if (this.mMode != 2) {
            TimePickerSpinnerDelegate timePickerSpinnerDelegate = new TimePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
            this.mDelegate = timePickerSpinnerDelegate;
        } else {
            TimePickerClockDelegate timePickerClockDelegate = new TimePickerClockDelegate(this, context, attrs, defStyleAttr, defStyleRes);
            this.mDelegate = timePickerClockDelegate;
        }
        this.mDelegate.setAutoFillChangeListener(new OnTimeChangedListener(context) {
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void onTimeChanged(TimePicker timePicker, int i, int i2) {
                TimePicker.lambda$new$0(TimePicker.this, this.f$1, timePicker, i, i2);
            }
        });
    }

    public static /* synthetic */ void lambda$new$0(TimePicker timePicker, Context context, TimePicker v, int h, int m) {
        AutofillManager afm = (AutofillManager) context.getSystemService(AutofillManager.class);
        if (afm != null) {
            afm.notifyValueChanged(timePicker);
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public void setHour(int hour) {
        this.mDelegate.setHour(MathUtils.constrain(hour, 0, 23));
    }

    public int getHour() {
        return this.mDelegate.getHour();
    }

    public void setMinute(int minute) {
        this.mDelegate.setMinute(MathUtils.constrain(minute, 0, 59));
    }

    public int getMinute() {
        return this.mDelegate.getMinute();
    }

    @Deprecated
    public void setCurrentHour(Integer currentHour) {
        setHour(currentHour.intValue());
    }

    @Deprecated
    public Integer getCurrentHour() {
        return Integer.valueOf(getHour());
    }

    @Deprecated
    public void setCurrentMinute(Integer currentMinute) {
        setMinute(currentMinute.intValue());
    }

    @Deprecated
    public Integer getCurrentMinute() {
        return Integer.valueOf(getMinute());
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (is24HourView != null) {
            this.mDelegate.setIs24Hour(is24HourView.booleanValue());
        }
    }

    public boolean is24HourView() {
        return this.mDelegate.is24Hour();
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mDelegate.setOnTimeChangedListener(onTimeChangedListener);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mDelegate.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return this.mDelegate.isEnabled();
    }

    public int getBaseline() {
        return this.mDelegate.getBaseline();
    }

    public boolean validateInput() {
        return this.mDelegate.validateInput();
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.onSaveInstanceState());
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        View.BaseSavedState ss = (View.BaseSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDelegate.onRestoreInstanceState(ss);
    }

    public CharSequence getAccessibilityClassName() {
        return TimePicker.class.getName();
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        return this.mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    public View getHourView() {
        return this.mDelegate.getHourView();
    }

    public View getMinuteView() {
        return this.mDelegate.getMinuteView();
    }

    public View getAmView() {
        return this.mDelegate.getAmView();
    }

    public View getPmView() {
        return this.mDelegate.getPmView();
    }

    static String[] getAmPmStrings(Context context) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        String[] result = new String[2];
        result[0] = d.amPm[0].length() > 4 ? d.narrowAm : d.amPm[0];
        result[1] = d.amPm[1].length() > 4 ? d.narrowPm : d.amPm[1];
        return result;
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        structure.setAutofillId(getAutofillId());
        onProvideAutofillStructure(structure, flags);
    }

    public void autofill(AutofillValue value) {
        if (isEnabled()) {
            this.mDelegate.autofill(value);
        }
    }

    public int getAutofillType() {
        return isEnabled() ? 4 : 0;
    }

    public AutofillValue getAutofillValue() {
        if (isEnabled()) {
            return this.mDelegate.getAutofillValue();
        }
        return null;
    }
}
