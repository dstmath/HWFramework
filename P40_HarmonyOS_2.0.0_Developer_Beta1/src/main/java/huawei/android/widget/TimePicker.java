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
    private Class mGoogleTimePickerClass;
    private Class mTimePickerSpinnerClass;

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
        final Object obj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleTimePickerClass);
        if (obj != null) {
            Object objNumberPicker = ReflectUtil.getObject(obj, "mMinuteSpinner", this.mTimePickerSpinnerClass);
            if (objNumberPicker instanceof NumberPicker) {
                ((NumberPicker) objNumberPicker).setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    /* class huawei.android.widget.TimePicker.AnonymousClass1 */

                    @Override // android.widget.NumberPicker.OnValueChangeListener
                    public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                        ReflectUtil.callMethod(obj, "updateInputState", null, null, TimePicker.this.mTimePickerSpinnerClass);
                        ReflectUtil.callMethod(obj, "onTimeChanged", null, null, TimePicker.this.mTimePickerSpinnerClass);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateAmPmControl() {
        Object obj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleTimePickerClass);
        if (obj != null) {
            ReflectUtil.callMethod(obj, "updateAmPmControl", null, null, this.mTimePickerSpinnerClass);
            View timeDividerMinuteAndAmPm = findViewById(34603117);
            if (timeDividerMinuteAndAmPm != null) {
                timeDividerMinuteAndAmPm.setVisibility(is24HourView() ? 8 : 0);
            }
        }
    }

    private void updateAmPmStart() {
        ViewGroup amPmParent;
        View timeDividerMinuteAndAmPm;
        Object obj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleTimePickerClass);
        if (obj != null) {
            Object objBool = ReflectUtil.callMethod(obj, "isAmPmAtStart", null, null, this.mTimePickerSpinnerClass);
            if ((objBool instanceof Boolean) && ((Boolean) objBool).booleanValue() && (timeDividerMinuteAndAmPm = (amPmParent = (ViewGroup) findViewById(16909507)).findViewById(34603117)) != null) {
                amPmParent.removeView(timeDividerMinuteAndAmPm);
                amPmParent.addView(timeDividerMinuteAndAmPm, 1);
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
        Object delegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleTimePickerClass);
        if (delegateObj == null) {
            return null;
        }
        Object obj = ReflectUtil.getObject(delegateObj, "mHourSpinner", this.mTimePickerSpinnerClass);
        if (obj instanceof NumberPicker) {
            return (NumberPicker) obj;
        }
        return null;
    }

    private NumberPicker getMinuteSpinner() {
        Object delegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleTimePickerClass);
        if (delegateObj == null) {
            return null;
        }
        Object obj = ReflectUtil.getObject(delegateObj, "mMinuteSpinner", this.mTimePickerSpinnerClass);
        if (obj instanceof NumberPicker) {
            return (NumberPicker) obj;
        }
        return null;
    }

    private NumberPicker getAmPmSpinner() {
        Object delegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleTimePickerClass);
        if (delegateObj == null) {
            return null;
        }
        Object obj = ReflectUtil.getObject(delegateObj, "mAmPmSpinner", this.mTimePickerSpinnerClass);
        if (obj instanceof NumberPicker) {
            return (NumberPicker) obj;
        }
        return null;
    }

    private void initclass() {
        if (this.mTimePickerSpinnerClass == null) {
            try {
                this.mTimePickerSpinnerClass = Class.forName(TP_SPINNER_CLASSNAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mTimePickerSpinnerClass not found");
            }
        }
        if (this.mGoogleTimePickerClass == null) {
            try {
                this.mGoogleTimePickerClass = Class.forName(GOOGLE_TP_CLASSNAME);
            } catch (ClassNotFoundException e2) {
                Log.e(TAG, "mGoogleTimePickerClass not found");
            }
        }
    }
}
