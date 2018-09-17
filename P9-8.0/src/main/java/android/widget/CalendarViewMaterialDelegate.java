package android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.DayPickerView.OnDaySelectedListener;

class CalendarViewMaterialDelegate extends AbstractCalendarViewDelegate {
    private final DayPickerView mDayPickerView;
    private OnDateChangeListener mOnDateChangeListener;
    private final OnDaySelectedListener mOnDaySelectedListener = new OnDaySelectedListener() {
        public void onDaySelected(DayPickerView view, Calendar day) {
            if (CalendarViewMaterialDelegate.this.mOnDateChangeListener != null) {
                CalendarViewMaterialDelegate.this.mOnDateChangeListener.onSelectedDayChange(CalendarViewMaterialDelegate.this.mDelegator, day.get(1), day.get(2), day.get(5));
            }
        }
    };

    public CalendarViewMaterialDelegate(CalendarView delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        this.mDayPickerView = new DayPickerView(context, attrs, defStyleAttr, defStyleRes);
        this.mDayPickerView.setOnDaySelectedListener(this.mOnDaySelectedListener);
        delegator.addView(this.mDayPickerView);
    }

    public void setWeekDayTextAppearance(int resId) {
        this.mDayPickerView.setDayOfWeekTextAppearance(resId);
    }

    public int getWeekDayTextAppearance() {
        return this.mDayPickerView.getDayOfWeekTextAppearance();
    }

    public void setDateTextAppearance(int resId) {
        this.mDayPickerView.setDayTextAppearance(resId);
    }

    public int getDateTextAppearance() {
        return this.mDayPickerView.getDayTextAppearance();
    }

    public void setMinDate(long minDate) {
        this.mDayPickerView.setMinDate(minDate);
    }

    public long getMinDate() {
        return this.mDayPickerView.getMinDate();
    }

    public void setMaxDate(long maxDate) {
        this.mDayPickerView.setMaxDate(maxDate);
    }

    public long getMaxDate() {
        return this.mDayPickerView.getMaxDate();
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mDayPickerView.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return this.mDayPickerView.getFirstDayOfWeek();
    }

    public void setDate(long date) {
        this.mDayPickerView.setDate(date, true);
    }

    public void setDate(long date, boolean animate, boolean center) {
        this.mDayPickerView.setDate(date, animate);
    }

    public long getDate() {
        return this.mDayPickerView.getDate();
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.mOnDateChangeListener = listener;
    }

    public boolean getBoundsForDate(long date, Rect outBounds) {
        if (!this.mDayPickerView.getBoundsForDate(date, outBounds)) {
            return false;
        }
        int[] dayPickerPositionOnScreen = new int[2];
        int[] delegatorPositionOnScreen = new int[2];
        this.mDayPickerView.getLocationOnScreen(dayPickerPositionOnScreen);
        this.mDelegator.getLocationOnScreen(delegatorPositionOnScreen);
        int extraVerticalOffset = dayPickerPositionOnScreen[1] - delegatorPositionOnScreen[1];
        outBounds.top += extraVerticalOffset;
        outBounds.bottom += extraVerticalOffset;
        return true;
    }
}
