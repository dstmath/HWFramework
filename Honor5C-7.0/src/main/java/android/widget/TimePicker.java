package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View.BaseSavedState;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.R;
import java.util.Locale;
import libcore.icu.LocaleData;

public class TimePicker extends FrameLayout {
    private static final int MODE_CLOCK = 2;
    private static final int MODE_SPINNER = 1;
    private final TimePickerDelegate mDelegate;

    interface TimePickerDelegate {
        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        int getBaseline();

        int getHour();

        int getMinute();

        boolean is24Hour();

        boolean isEnabled();

        void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onRestoreInstanceState(Parcelable parcelable);

        Parcelable onSaveInstanceState(Parcelable parcelable);

        void setEnabled(boolean z);

        void setHour(int i);

        void setIs24Hour(boolean z);

        void setMinute(int i);

        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);
    }

    static abstract class AbstractTimePickerDelegate implements TimePickerDelegate {
        protected final Context mContext;
        protected final TimePicker mDelegator;
        protected final Locale mLocale;
        protected OnTimeChangedListener mOnTimeChangedListener;

        protected static class SavedState extends BaseSavedState {
            public static final Creator<SavedState> CREATOR = null;
            private final int mCurrentItemShowing;
            private final int mHour;
            private final boolean mIs24HourMode;
            private final int mMinute;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<clinit>():void");
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
                if (in.readInt() != TimePicker.MODE_SPINNER) {
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
                dest.writeInt(this.mIs24HourMode ? TimePicker.MODE_SPINNER : 0);
                dest.writeInt(this.mCurrentItemShowing);
            }
        }

        public AbstractTimePickerDelegate(TimePicker delegator, Context context) {
            this.mDelegator = delegator;
            this.mContext = context;
            this.mLocale = context.getResources().getConfiguration().locale;
        }
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2);
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        int mode = a.getInt(8, MODE_SPINNER);
        a.recycle();
        switch (mode) {
            case MODE_CLOCK /*2*/:
                this.mDelegate = new TimePickerClockDelegate(this, context, attrs, defStyleAttr, defStyleRes);
            default:
                this.mDelegate = new TimePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
        }
    }

    public void setHour(int hour) {
        this.mDelegate.setHour(hour);
    }

    public int getHour() {
        return this.mDelegate.getHour();
    }

    public void setMinute(int minute) {
        this.mDelegate.setMinute(minute);
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
        return Integer.valueOf(this.mDelegate.getHour());
    }

    @Deprecated
    public void setCurrentMinute(Integer currentMinute) {
        this.mDelegate.setMinute(currentMinute.intValue());
    }

    @Deprecated
    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mDelegate.getMinute());
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

    protected Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.onSaveInstanceState());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        BaseSavedState ss = (BaseSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDelegate.onRestoreInstanceState(ss);
    }

    public CharSequence getAccessibilityClassName() {
        return TimePicker.class.getName();
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        return this.mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    static String[] getAmPmStrings(Context context) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        String[] result = new String[MODE_CLOCK];
        result[0] = d.amPm[0].length() > 4 ? d.narrowAm : d.amPm[0];
        result[MODE_SPINNER] = d.amPm[MODE_SPINNER].length() > 4 ? d.narrowPm : d.amPm[MODE_SPINNER];
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
}
