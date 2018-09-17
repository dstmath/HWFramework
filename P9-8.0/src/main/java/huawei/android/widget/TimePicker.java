package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

public class TimePicker extends android.widget.TimePicker {
    private static final String TAG = "TimePicker";

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
        if (getHourSpinner() == null || getMinuteSpinner() == null || getAmPmSpinner() == null) {
            Log.w(TAG, " getHourSpinner = " + getHourSpinner() + ", getMinuteSpinner = " + getMinuteSpinner() + ", getAmPmSpinner() = " + getAmPmSpinner());
        } else {
            addFireLists(getHourSpinner(), getMinuteSpinner(), getAmPmSpinner());
        }
    }

    protected void updateAmPmControl() {
        super.updateAmPmControl();
        View timedivider2 = findViewById(34603117);
        if (timedivider2 != null) {
            int i;
            if (is24HourView()) {
                i = 8;
            } else {
                i = 0;
            }
            timedivider2.setVisibility(i);
        }
    }

    protected boolean hwValueChange() {
        updateInputStateOuter();
        onTimeChangedOuter();
        return true;
    }

    protected void updateAmPmStart(ViewGroup amPmParent) {
        View timedivider2 = amPmParent.findViewById(34603117);
        if (timedivider2 != null) {
            amPmParent.removeView(timedivider2);
            amPmParent.addView(timedivider2, 0);
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
}
