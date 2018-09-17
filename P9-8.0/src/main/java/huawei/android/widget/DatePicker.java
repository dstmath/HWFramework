package huawei.android.widget;

import android.content.Context;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

public class DatePicker extends android.widget.DatePicker {
    private static final int DEFAULT_DATE_ORDER_FIRST = 1;
    private static final int DEFAULT_DATE_ORDER_SECOND = 2;
    private static final String TAG = "DatePicker";
    private View mDatedivider1;
    private View mDatedivider2;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 16843612);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (getDaySpinner() == null || getMonthSpinner() == null || getYearSpinner() == null) {
            Log.w(TAG, "getDaySpinner = " + getDaySpinner() + ", getMonthSpinner = " + getMonthSpinner() + ", getYearSpinner() = " + getYearSpinner());
            return;
        }
        addFireLists(getDaySpinner(), getMonthSpinner(), getYearSpinner());
        setDivider();
    }

    protected boolean hwValueChange(NumberPicker picker, int oldVal, int newVal) {
        Calendar dateTemp = getTempDate();
        if (dateTemp == null) {
            throw new RuntimeException("can not get temp date.");
        }
        if (picker == getDaySpinner()) {
            dateTemp.add(5, newVal - oldVal);
        } else if (picker == getMonthSpinner()) {
            dateTemp.add(2, newVal - oldVal);
        } else if (picker == getYearSpinner()) {
            dateTemp.add(1, newVal - oldVal);
        } else {
            throw new IllegalArgumentException();
        }
        return true;
    }

    private void setDivider() {
        this.mDatedivider1 = findViewById(34603101);
        this.mDatedivider2 = findViewById(34603102);
        LinearLayout spinnerGroup = getSpinners();
        if (spinnerGroup == null) {
            throw new RuntimeException("can not get the spinners.");
        }
        int childCount = spinnerGroup.getChildCount();
        if (childCount > 2 && this.mDatedivider2 != null) {
            spinnerGroup.addView(this.mDatedivider2, 2);
        }
        if (childCount > 1 && this.mDatedivider1 != null) {
            spinnerGroup.addView(this.mDatedivider1, 1);
        }
    }

    private void addFireLists(NumberPicker daySpinner, NumberPicker monthSpinner, NumberPicker yearSpinner) {
        daySpinner.addFireList(monthSpinner);
        daySpinner.addFireList(yearSpinner);
        monthSpinner.addFireList(daySpinner);
        monthSpinner.addFireList(yearSpinner);
        yearSpinner.addFireList(daySpinner);
        yearSpinner.addFireList(monthSpinner);
    }
}
