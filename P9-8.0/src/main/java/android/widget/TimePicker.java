package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import com.android.internal.R;
import java.util.Locale;
import libcore.icu.LocaleData;

public class TimePicker extends FrameLayout {
    private static final String LOG_TAG = TimePicker.class.getSimpleName();
    public static final int MODE_CLOCK = 2;
    public static final int MODE_SPINNER = 1;
    private final TimePickerDelegate mDelegate;
    private final int mMode;

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2);
    }

    interface TimePickerDelegate {
        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        View getAmView();

        int getBaseline();

        long getDate();

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

        void setDate(long j);

        void setEnabled(boolean z);

        void setHour(int i);

        void setIs24Hour(boolean z);

        void setMinute(int i);

        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);

        boolean validateInput();
    }

    static abstract class AbstractTimePickerDelegate implements TimePickerDelegate {
        protected OnTimeChangedListener mAutoFillChangeListener;
        protected final Context mContext;
        protected final TimePicker mDelegator;
        protected final Locale mLocale;
        protected OnTimeChangedListener mOnTimeChangedListener;

        protected static class SavedState extends BaseSavedState {
            public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in, null);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
            private final int mCurrentItemShowing;
            private final int mHour;
            private final boolean mIs24HourMode;
            private final int mMinute;

            /* synthetic */ SavedState(Parcel in, SavedState -this1) {
                this(in);
            }

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
                boolean z = true;
                super(in);
                this.mHour = in.readInt();
                this.mMinute = in.readInt();
                if (in.readInt() != 1) {
                    z = false;
                }
                this.mIs24HourMode = z;
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

        public void setDate(long date) {
            Calendar cal = Calendar.getInstance(this.mLocale);
            cal.setTimeInMillis(date);
            setHour(cal.get(11));
            setMinute(cal.get(12));
        }

        public long getDate() {
            Calendar cal = Calendar.getInstance(this.mLocale);
            cal.set(11, getHour());
            cal.set(12, getMinute());
            return cal.getTimeInMillis();
        }
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
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
        if (requestedMode == 2 && isDialogMode) {
            this.mMode = context.getResources().getInteger(R.integer.time_picker_mode);
        } else {
            this.mMode = requestedMode;
        }
        switch (this.mMode) {
            case 2:
                this.mDelegate = new TimePickerClockDelegate(this, context, attrs, defStyleAttr, defStyleRes);
                break;
            default:
                this.mDelegate = new TimePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
                break;
        }
        this.mDelegate.setAutoFillChangeListener(new -$Lambda$6tyI8UILr_8rz_HNI0sixHbWoHI(this, context));
    }

    /* synthetic */ void lambda$-android_widget_TimePicker_5102(Context context, TimePicker v, int h, int m) {
        AutofillManager afm = (AutofillManager) context.getSystemService(AutofillManager.class);
        if (afm != null) {
            afm.notifyValueChanged(this);
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

    protected Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.-wrap0());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        BaseSavedState ss = (BaseSavedState) state;
        super.-wrap2(ss.getSuperState());
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

    protected boolean hwValueChange() {
        return false;
    }

    protected void updateAmPmStart(ViewGroup amPmParent) {
    }

    protected void onTimeChangedOuter() {
        if (this.mDelegate instanceof TimePickerSpinnerDelegate) {
            ((TimePickerSpinnerDelegate) this.mDelegate).onTimeChangedOuter();
        }
    }

    protected void updateInputStateOuter() {
        if (this.mDelegate instanceof TimePickerSpinnerDelegate) {
            ((TimePickerSpinnerDelegate) this.mDelegate).updateInputStateOuter();
        }
    }

    protected NumberPicker getHourSpinner() {
        if (this.mDelegate instanceof TimePickerSpinnerDelegate) {
            return ((TimePickerSpinnerDelegate) this.mDelegate).getHourSpinner();
        }
        return null;
    }

    protected NumberPicker getMinuteSpinner() {
        if (this.mDelegate instanceof TimePickerSpinnerDelegate) {
            return ((TimePickerSpinnerDelegate) this.mDelegate).getMinuteSpinner();
        }
        return null;
    }

    protected NumberPicker getAmPmSpinner() {
        if (this.mDelegate instanceof TimePickerSpinnerDelegate) {
            return ((TimePickerSpinnerDelegate) this.mDelegate).getAmPmSpinner();
        }
        return null;
    }

    protected void updateAmPmControl() {
        if (this.mDelegate instanceof TimePickerSpinnerDelegate) {
            ((TimePickerSpinnerDelegate) this.mDelegate).updateAmPmControl();
        }
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        structure.setAutofillId(getAutofillId());
        onProvideAutofillStructure(structure, flags);
    }

    public void autofill(AutofillValue value) {
        if (!isEnabled()) {
            return;
        }
        if (value.isDate()) {
            this.mDelegate.setDate(value.getDateValue());
        } else {
            Log.w(LOG_TAG, value + " could not be autofilled into " + this);
        }
    }

    public int getAutofillType() {
        return isEnabled() ? 4 : 0;
    }

    public AutofillValue getAutofillValue() {
        return isEnabled() ? AutofillValue.forDate(this.mDelegate.getDate()) : null;
    }
}
