package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import huawei.android.widget.DecouplingUtil.ReflectUtil;

public class TimePicker extends android.widget.TimePicker {
    private static final String GOOGLE_TP_CLASSNAME = "android.widget.TimePicker";
    private static final String TAG = "TimePicker";
    private static final String TP_SPINNER_CLASSNAME = "android.widget.TimePickerSpinnerDelegate";
    private Class mGTimePickerClass;
    /* access modifiers changed from: private */
    public Class mTimePSpinnerClass;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, context.getResources().getIdentifier("timePickerStyle", "attr", "android"));
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initclass();
        if (getHourSpinner() == null || getMinuteSpinner() == null || getAmPmSpinner() == null) {
            Log.w(TAG, " getHourSpinner = " + getHourSpinner() + ", getMinuteSpinner = " + getMinuteSpinner() + ", getAmPmSpinner() = " + getAmPmSpinner());
            return;
        }
        addFireLists(getHourSpinner(), getMinuteSpinner(), getAmPmSpinner());
        updateAmPmStart();
        final Object obj = ReflectUtil.getObject(this, "mDelegate", this.mGTimePickerClass);
        if (obj != null) {
            NumberPicker minuteSpinner = (NumberPicker) ReflectUtil.getObject(obj, "mMinuteSpinner", this.mTimePSpinnerClass);
            if (minuteSpinner != null) {
                minuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                        ReflectUtil.callMethod(obj, "updateInputState", null, null, TimePicker.this.mTimePSpinnerClass);
                        ReflectUtil.callMethod(obj, "onTimeChanged", null, null, TimePicker.this.mTimePSpinnerClass);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateAmPmControl() {
        int i;
        Object obj = ReflectUtil.getObject(this, "mDelegate", this.mGTimePickerClass);
        if (obj != null) {
            ReflectUtil.callMethod(obj, "updateAmPmControl", null, null, this.mTimePSpinnerClass);
            View timedivider2 = findViewById(34603117);
            if (timedivider2 != null) {
                if (is24HourView()) {
                    i = 8;
                } else {
                    i = 0;
                }
                timedivider2.setVisibility(i);
            }
        }
    }

    private void updateAmPmStart() {
        Object obj = ReflectUtil.getObject(this, "mDelegate", this.mGTimePickerClass);
        if (obj != null && ((Boolean) ReflectUtil.callMethod(obj, "isAmPmAtStart", null, null, this.mTimePSpinnerClass)).booleanValue()) {
            ViewGroup amPmParent = (ViewGroup) findViewById(16909435);
            View timedivider2 = amPmParent.findViewById(34603117);
            if (timedivider2 != null) {
                amPmParent.removeView(timedivider2);
                amPmParent.addView(timedivider2, 0);
            }
        }
    }

    private void addFireLists(NumberPicker hourSpinner, NumberPicker minuteSpinner, NumberPicker amPmSpinner) {
        hourSpinner.addFireList(minuteSpinner);
        hourSpinner.addFireList(amPmSpinner);
        minuteSpinner.addFireList(hourSpinner);
        minuteSpinner.addFireList(amPmSpinner);
        amPmSpinner.addFireList(hourSpinner);
        amPmSpinner.addFireList(minuteSpinner);
    }

    private NumberPicker getHourSpinner() {
        Object mDelegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGTimePickerClass);
        if (mDelegateObj != null) {
            Object obj = ReflectUtil.getObject(mDelegateObj, "mHourSpinner", this.mTimePSpinnerClass);
            if (obj != null) {
                return (NumberPicker) obj;
            }
        }
        return null;
    }

    private NumberPicker getMinuteSpinner() {
        Object mDelegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGTimePickerClass);
        if (mDelegateObj != null) {
            Object obj = ReflectUtil.getObject(mDelegateObj, "mMinuteSpinner", this.mTimePSpinnerClass);
            if (obj != null) {
                return (NumberPicker) obj;
            }
        }
        return null;
    }

    private NumberPicker getAmPmSpinner() {
        Object mDelegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGTimePickerClass);
        if (mDelegateObj != null) {
            Object obj = ReflectUtil.getObject(mDelegateObj, "mAmPmSpinner", this.mTimePSpinnerClass);
            if (obj != null) {
                return (NumberPicker) obj;
            }
        }
        return null;
    }

    private void initclass() {
        if (this.mTimePSpinnerClass == null) {
            try {
                this.mTimePSpinnerClass = Class.forName(TP_SPINNER_CLASSNAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mTimePSpinnerClass not found");
            }
        }
        if (this.mGTimePickerClass == null) {
            try {
                this.mGTimePickerClass = Class.forName(GOOGLE_TP_CLASSNAME);
            } catch (ClassNotFoundException e2) {
                Log.e(TAG, "mGTimePickerClass not found");
            }
        }
    }
}
