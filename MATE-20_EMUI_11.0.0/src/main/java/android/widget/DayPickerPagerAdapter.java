package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleMonthView;
import com.android.internal.widget.PagerAdapter;

/* access modifiers changed from: package-private */
public class DayPickerPagerAdapter extends PagerAdapter {
    private static final int MONTHS_IN_YEAR = 12;
    private ColorStateList mCalendarTextColor;
    private final int mCalendarViewId;
    private int mCount;
    private ColorStateList mDayHighlightColor;
    private int mDayOfWeekTextAppearance;
    private ColorStateList mDaySelectorColor;
    private int mDayTextAppearance;
    private int mFirstDayOfWeek;
    private final LayoutInflater mInflater;
    private final SparseArray<ViewHolder> mItems = new SparseArray<>();
    private final int mLayoutResId;
    private final Calendar mMaxDate = Calendar.getInstance();
    private final Calendar mMinDate = Calendar.getInstance();
    private int mMonthTextAppearance;
    private final SimpleMonthView.OnDayClickListener mOnDayClickListener = new SimpleMonthView.OnDayClickListener() {
        /* class android.widget.DayPickerPagerAdapter.AnonymousClass1 */

        @Override // android.widget.SimpleMonthView.OnDayClickListener
        public void onDayClick(SimpleMonthView view, Calendar day) {
            if (day != null) {
                DayPickerPagerAdapter.this.setSelectedDay(day);
                if (DayPickerPagerAdapter.this.mOnDaySelectedListener != null) {
                    DayPickerPagerAdapter.this.mOnDaySelectedListener.onDaySelected(DayPickerPagerAdapter.this, day);
                }
            }
        }
    };
    private OnDaySelectedListener mOnDaySelectedListener;
    private Calendar mSelectedDay = null;

    public interface OnDaySelectedListener {
        void onDaySelected(DayPickerPagerAdapter dayPickerPagerAdapter, Calendar calendar);
    }

    public DayPickerPagerAdapter(Context context, int layoutResId, int calendarViewId) {
        this.mInflater = LayoutInflater.from(context);
        this.mLayoutResId = layoutResId;
        this.mCalendarViewId = calendarViewId;
        TypedArray ta = context.obtainStyledAttributes(new int[]{16843820});
        this.mDayHighlightColor = ta.getColorStateList(0);
        ta.recycle();
    }

    public void setRange(Calendar min, Calendar max) {
        this.mMinDate.setTimeInMillis(min.getTimeInMillis());
        this.mMaxDate.setTimeInMillis(max.getTimeInMillis());
        int diffYear = this.mMaxDate.get(1) - this.mMinDate.get(1);
        this.mCount = (diffYear * 12) + (this.mMaxDate.get(2) - this.mMinDate.get(2)) + 1;
        notifyDataSetChanged();
    }

    public void setFirstDayOfWeek(int weekStart) {
        this.mFirstDayOfWeek = weekStart;
        int count = this.mItems.size();
        for (int i = 0; i < count; i++) {
            this.mItems.valueAt(i).calendar.setFirstDayOfWeek(weekStart);
        }
    }

    public int getFirstDayOfWeek() {
        return this.mFirstDayOfWeek;
    }

    public boolean getBoundsForDate(Calendar day, Rect outBounds) {
        ViewHolder monthView = this.mItems.get(getPositionForDay(day), null);
        if (monthView == null) {
            return false;
        }
        return monthView.calendar.getBoundsForDay(day.get(5), outBounds);
    }

    public void setSelectedDay(Calendar day) {
        ViewHolder newMonthView;
        ViewHolder oldMonthView;
        int oldPosition = getPositionForDay(this.mSelectedDay);
        int newPosition = getPositionForDay(day);
        if (!(oldPosition == newPosition || oldPosition < 0 || (oldMonthView = this.mItems.get(oldPosition, null)) == null)) {
            oldMonthView.calendar.setSelectedDay(-1);
        }
        if (newPosition >= 0 && (newMonthView = this.mItems.get(newPosition, null)) != null) {
            newMonthView.calendar.setSelectedDay(day.get(5));
        }
        this.mSelectedDay = day;
    }

    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        this.mOnDaySelectedListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void setCalendarTextColor(ColorStateList calendarTextColor) {
        this.mCalendarTextColor = calendarTextColor;
        notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public void setDaySelectorColor(ColorStateList selectorColor) {
        this.mDaySelectorColor = selectorColor;
        notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public void setMonthTextAppearance(int resId) {
        this.mMonthTextAppearance = resId;
        notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public void setDayOfWeekTextAppearance(int resId) {
        this.mDayOfWeekTextAppearance = resId;
        notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public int getDayOfWeekTextAppearance() {
        return this.mDayOfWeekTextAppearance;
    }

    /* access modifiers changed from: package-private */
    public void setDayTextAppearance(int resId) {
        this.mDayTextAppearance = resId;
        notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public int getDayTextAppearance() {
        return this.mDayTextAppearance;
    }

    @Override // com.android.internal.widget.PagerAdapter
    public int getCount() {
        return this.mCount;
    }

    @Override // com.android.internal.widget.PagerAdapter
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ViewHolder) object).container;
    }

    private int getMonthForPosition(int position) {
        return (this.mMinDate.get(2) + position) % 12;
    }

    private int getYearForPosition(int position) {
        return this.mMinDate.get(1) + ((this.mMinDate.get(2) + position) / 12);
    }

    private int getPositionForDay(Calendar day) {
        if (day == null) {
            return -1;
        }
        int yearOffset = day.get(1) - this.mMinDate.get(1);
        return (yearOffset * 12) + (day.get(2) - this.mMinDate.get(2));
    }

    @Override // com.android.internal.widget.PagerAdapter
    public Object instantiateItem(ViewGroup container, int position) {
        int selectedDay;
        int enabledDayRangeStart;
        int enabledDayRangeEnd;
        View itemView = this.mInflater.inflate(this.mLayoutResId, container, false);
        SimpleMonthView v = (SimpleMonthView) itemView.findViewById(this.mCalendarViewId);
        v.setOnDayClickListener(this.mOnDayClickListener);
        v.setMonthTextAppearance(this.mMonthTextAppearance);
        v.setDayOfWeekTextAppearance(this.mDayOfWeekTextAppearance);
        v.setDayTextAppearance(this.mDayTextAppearance);
        ColorStateList colorStateList = this.mDaySelectorColor;
        if (colorStateList != null) {
            v.setDaySelectorColor(colorStateList);
        }
        ColorStateList colorStateList2 = this.mDayHighlightColor;
        if (colorStateList2 != null) {
            v.setDayHighlightColor(colorStateList2);
        }
        ColorStateList colorStateList3 = this.mCalendarTextColor;
        if (colorStateList3 != null) {
            v.setMonthTextColor(colorStateList3);
            v.setDayOfWeekTextColor(this.mCalendarTextColor);
            v.setDayTextColor(this.mCalendarTextColor);
        }
        int month = getMonthForPosition(position);
        int year = getYearForPosition(position);
        Calendar calendar = this.mSelectedDay;
        if (calendar != null && calendar.get(2) == month && this.mSelectedDay.get(1) == year) {
            selectedDay = this.mSelectedDay.get(5);
        } else {
            selectedDay = -1;
        }
        if (this.mMinDate.get(2) == month && this.mMinDate.get(1) == year) {
            enabledDayRangeStart = this.mMinDate.get(5);
        } else {
            enabledDayRangeStart = 1;
        }
        if (this.mMaxDate.get(2) == month && this.mMaxDate.get(1) == year) {
            enabledDayRangeEnd = this.mMaxDate.get(5);
        } else {
            enabledDayRangeEnd = 31;
        }
        v.setMonthParams(selectedDay, month, year, this.mFirstDayOfWeek, enabledDayRangeStart, enabledDayRangeEnd);
        ViewHolder holder = new ViewHolder(position, itemView, v);
        this.mItems.put(position, holder);
        container.addView(itemView);
        return holder;
    }

    @Override // com.android.internal.widget.PagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((ViewHolder) object).container);
        this.mItems.remove(position);
    }

    @Override // com.android.internal.widget.PagerAdapter
    public int getItemPosition(Object object) {
        return ((ViewHolder) object).position;
    }

    @Override // com.android.internal.widget.PagerAdapter
    public CharSequence getPageTitle(int position) {
        SimpleMonthView v = this.mItems.get(position).calendar;
        if (v != null) {
            return v.getMonthYearLabel();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public SimpleMonthView getView(Object object) {
        if (object == null) {
            return null;
        }
        return ((ViewHolder) object).calendar;
    }

    /* access modifiers changed from: private */
    public static class ViewHolder {
        public final SimpleMonthView calendar;
        public final View container;
        public final int position;

        public ViewHolder(int position2, View container2, SimpleMonthView calendar2) {
            this.position = position2;
            this.container = container2;
            this.calendar = calendar2;
        }
    }
}
