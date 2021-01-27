package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntFunction;
import libcore.icu.LocaleData;

public class TimePicker extends FrameLayout {
    private static final String LOG_TAG = TimePicker.class.getSimpleName();
    public static final int MODE_CLOCK = 2;
    public static final int MODE_SPINNER = 1;
    @UnsupportedAppUsage
    private final TimePickerDelegate mDelegate;
    private final int mMode;

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2);
    }

    /* access modifiers changed from: package-private */
    public interface TimePickerDelegate {
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

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<TimePicker> {
        private int m24HourId;
        private int mHourId;
        private int mMinuteId;
        private boolean mPropertiesMapped = false;
        private int mTimePickerModeId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.m24HourId = propertyMapper.mapBoolean("24Hour", 0);
            this.mHourId = propertyMapper.mapInt("hour", 0);
            this.mMinuteId = propertyMapper.mapInt("minute", 0);
            SparseArray<String> timePickerModeEnumMapping = new SparseArray<>();
            timePickerModeEnumMapping.put(1, "spinner");
            timePickerModeEnumMapping.put(2, "clock");
            Objects.requireNonNull(timePickerModeEnumMapping);
            this.mTimePickerModeId = propertyMapper.mapIntEnum("timePickerMode", 16843956, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mPropertiesMapped = true;
        }

        public void readProperties(TimePicker node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.m24HourId, node.is24HourView());
                propertyReader.readInt(this.mHourId, node.getHour());
                propertyReader.readInt(this.mMinuteId, node.getMinute());
                propertyReader.readIntEnum(this.mTimePickerModeId, node.getMode());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
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
        saveAttributeDataForStyleable(context, R.styleable.TimePicker, attrs, a, defStyleAttr, defStyleRes);
        boolean isDialogMode = a.getBoolean(10, false);
        int requestedMode = a.getInt(8, 1);
        a.recycle();
        if (requestedMode != 2 || !isDialogMode) {
            this.mMode = requestedMode;
        } else {
            this.mMode = context.getResources().getInteger(R.integer.time_picker_mode);
        }
        if (this.mMode != 2) {
            this.mDelegate = new TimePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
        } else {
            this.mDelegate = new TimePickerClockDelegate(this, context, attrs, defStyleAttr, defStyleRes);
        }
        this.mDelegate.setAutoFillChangeListener(new OnTimeChangedListener(context) {
            /* class android.widget.$$Lambda$TimePicker$2FhAB9WgnLgn4zn4f9rRT7DNfjw */
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.widget.TimePicker.OnTimeChangedListener
            public final void onTimeChanged(TimePicker timePicker, int i, int i2) {
                TimePicker.this.lambda$new$0$TimePicker(this.f$1, timePicker, i, i2);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$TimePicker(Context context, TimePicker v, int h, int m) {
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

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mDelegate.setEnabled(enabled);
    }

    @Override // android.view.View
    public boolean isEnabled() {
        return this.mDelegate.isEnabled();
    }

    @Override // android.view.View
    public int getBaseline() {
        return this.mDelegate.getBaseline();
    }

    public boolean validateInput() {
        return this.mDelegate.validateInput();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.onSaveInstanceState());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        View.BaseSavedState ss = (View.BaseSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDelegate.onRestoreInstanceState(ss);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return TimePicker.class.getName();
    }

    @Override // android.view.ViewGroup, android.view.View
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

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0024: APUT  
      (r2v1 'result' java.lang.String[] A[D('result' java.lang.String[])])
      (0 ??[int, short, byte, char])
      (r3v3 java.lang.String)
     */
    static String[] getAmPmStrings(Context context) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        String[] result = new String[2];
        result[0] = d.amPm[0].length() > 4 ? d.narrowAm : d.amPm[0];
        result[1] = d.amPm[1].length() > 4 ? d.narrowPm : d.amPm[1];
        return result;
    }

    static abstract class AbstractTimePickerDelegate implements TimePickerDelegate {
        protected OnTimeChangedListener mAutoFillChangeListener;
        private long mAutofilledValue;
        protected final Context mContext;
        protected final TimePicker mDelegator;
        protected final Locale mLocale;
        protected OnTimeChangedListener mOnTimeChangedListener;

        public AbstractTimePickerDelegate(TimePicker delegator, Context context) {
            this.mDelegator = delegator;
            this.mContext = context;
            this.mLocale = context.getResources().getConfiguration().locale;
        }

        @Override // android.widget.TimePicker.TimePickerDelegate
        public void setOnTimeChangedListener(OnTimeChangedListener callback) {
            this.mOnTimeChangedListener = callback;
        }

        @Override // android.widget.TimePicker.TimePickerDelegate
        public void setAutoFillChangeListener(OnTimeChangedListener callback) {
            this.mAutoFillChangeListener = callback;
        }

        @Override // android.widget.TimePicker.TimePickerDelegate
        public final void autofill(AutofillValue value) {
            if (value == null || !value.isDate()) {
                String str = TimePicker.LOG_TAG;
                Log.w(str, value + " could not be autofilled into " + this);
                return;
            }
            long time = value.getDateValue();
            Calendar cal = Calendar.getInstance(this.mLocale);
            cal.setTimeInMillis(time);
            setDate(cal.get(11), cal.get(12));
            this.mAutofilledValue = time;
        }

        @Override // android.widget.TimePicker.TimePickerDelegate
        public final AutofillValue getAutofillValue() {
            long j = this.mAutofilledValue;
            if (j != 0) {
                return AutofillValue.forDate(j);
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

        /* access modifiers changed from: protected */
        public static class SavedState extends View.BaseSavedState {
            public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
                /* class android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.AnonymousClass1 */

                @Override // android.os.Parcelable.Creator
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                @Override // android.os.Parcelable.Creator
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

            @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(this.mHour);
                dest.writeInt(this.mMinute);
                dest.writeInt(this.mIs24HourMode ? 1 : 0);
                dest.writeInt(this.mCurrentItemShowing);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        structure.setAutofillId(getAutofillId());
        onProvideAutofillStructure(structure, flags);
    }

    @Override // android.view.View
    public void autofill(AutofillValue value) {
        if (isEnabled()) {
            this.mDelegate.autofill(value);
        }
    }

    @Override // android.view.View
    public int getAutofillType() {
        return isEnabled() ? 4 : 0;
    }

    @Override // android.view.View
    public AutofillValue getAutofillValue() {
        if (isEnabled()) {
            return this.mDelegate.getAutofillValue();
        }
        return null;
    }
}
