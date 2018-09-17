package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.R;
import com.android.internal.widget.ViewPager;
import com.android.internal.widget.ViewPager.OnPageChangeListener;
import java.util.Locale;
import libcore.icu.LocaleData;

class DayPickerView extends ViewGroup {
    private static final int[] ATTRS_TEXT_COLOR = new int[]{R.attr.textColor};
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_LAYOUT = 17367122;
    private static final int DEFAULT_START_YEAR = 1900;
    private final AccessibilityManager mAccessibilityManager;
    private final DayPickerPagerAdapter mAdapter;
    private final Calendar mMaxDate;
    private final Calendar mMinDate;
    private final ImageButton mNextButton;
    private final OnClickListener mOnClickListener;
    private OnDaySelectedListener mOnDaySelectedListener;
    private final OnPageChangeListener mOnPageChangedListener;
    private final ImageButton mPrevButton;
    private final Calendar mSelectedDay;
    private Calendar mTempCalendar;
    private final ViewPager mViewPager;

    public interface OnDaySelectedListener {
        void onDaySelected(DayPickerView dayPickerView, Calendar calendar);
    }

    public DayPickerView(Context context) {
        this(context, null);
    }

    public DayPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.calendarViewStyle);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSelectedDay = Calendar.getInstance();
        this.mMinDate = Calendar.getInstance();
        this.mMaxDate = Calendar.getInstance();
        this.mOnPageChangedListener = new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float alpha = Math.abs(0.5f - positionOffset) * 2.0f;
                DayPickerView.this.mPrevButton.setAlpha(alpha);
                DayPickerView.this.mNextButton.setAlpha(alpha);
            }

            public void onPageScrollStateChanged(int state) {
            }

            public void onPageSelected(int position) {
                DayPickerView.this.updateButtonVisibility(position);
            }
        };
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                int direction;
                if (v == DayPickerView.this.mPrevButton) {
                    direction = -1;
                } else if (v == DayPickerView.this.mNextButton) {
                    direction = 1;
                } else {
                    return;
                }
                int nextItem = DayPickerView.this.mViewPager.getCurrentItem() + direction;
                DayPickerView.this.mViewPager.setCurrentItem(nextItem, DayPickerView.this.mAccessibilityManager.isEnabled() ^ 1);
            }
        };
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
        int firstDayOfWeek = a.getInt(0, LocaleData.get(Locale.getDefault()).firstDayOfWeek.intValue());
        String minDate = a.getString(2);
        String maxDate = a.getString(3);
        int monthTextAppearanceResId = a.getResourceId(16, R.style.TextAppearance_Material_Widget_Calendar_Month);
        int dayOfWeekTextAppearanceResId = a.getResourceId(11, R.style.TextAppearance_Material_Widget_Calendar_DayOfWeek);
        int dayTextAppearanceResId = a.getResourceId(12, R.style.TextAppearance_Material_Widget_Calendar_Day);
        ColorStateList daySelectorColor = a.getColorStateList(15);
        a.recycle();
        this.mAdapter = new DayPickerPagerAdapter(context, R.layout.date_picker_month_item_material, R.id.month_view);
        this.mAdapter.setMonthTextAppearance(monthTextAppearanceResId);
        this.mAdapter.setDayOfWeekTextAppearance(dayOfWeekTextAppearanceResId);
        this.mAdapter.setDayTextAppearance(dayTextAppearanceResId);
        this.mAdapter.setDaySelectorColor(daySelectorColor);
        ViewGroup content = (ViewGroup) LayoutInflater.from(context).inflate(17367122, (ViewGroup) this, false);
        while (content.getChildCount() > 0) {
            View child = content.getChildAt(0);
            content.removeViewAt(0);
            addView(child);
        }
        this.mPrevButton = (ImageButton) findViewById(R.id.prev);
        this.mPrevButton.setOnClickListener(this.mOnClickListener);
        this.mNextButton = (ImageButton) findViewById(R.id.next);
        this.mNextButton.setOnClickListener(this.mOnClickListener);
        this.mViewPager = (ViewPager) findViewById(R.id.day_picker_view_pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOnPageChangeListener(this.mOnPageChangedListener);
        if (monthTextAppearanceResId != 0) {
            TypedArray ta = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, monthTextAppearanceResId);
            ColorStateList monthColor = ta.getColorStateList(0);
            if (monthColor != null) {
                this.mPrevButton.setImageTintList(monthColor);
                this.mNextButton.setImageTintList(monthColor);
            }
            ta.recycle();
        }
        Calendar tempDate = Calendar.getInstance();
        if (!CalendarView.parseDate(minDate, tempDate)) {
            tempDate.set(1900, 0, 1);
        }
        long minDateMillis = tempDate.getTimeInMillis();
        if (!CalendarView.parseDate(maxDate, tempDate)) {
            tempDate.set(2100, 11, 31);
        }
        long maxDateMillis = tempDate.getTimeInMillis();
        if (maxDateMillis < minDateMillis) {
            throw new IllegalArgumentException("maxDate must be >= minDate");
        }
        long setDateMillis = MathUtils.constrain(System.currentTimeMillis(), minDateMillis, maxDateMillis);
        setFirstDayOfWeek(firstDayOfWeek);
        setMinDate(minDateMillis);
        setMaxDate(maxDateMillis);
        setDate(setDateMillis, false);
        this.mAdapter.setOnDaySelectedListener(new android.widget.DayPickerPagerAdapter.OnDaySelectedListener() {
            public void onDaySelected(DayPickerPagerAdapter adapter, Calendar day) {
                if (DayPickerView.this.mOnDaySelectedListener != null) {
                    DayPickerView.this.mOnDaySelectedListener.onDaySelected(DayPickerView.this, day);
                }
            }
        });
    }

    private void updateButtonVisibility(int position) {
        int i;
        int i2 = 0;
        boolean hasPrev = position > 0;
        boolean hasNext = position < this.mAdapter.getCount() + -1;
        ImageButton imageButton = this.mPrevButton;
        if (hasPrev) {
            i = 0;
        } else {
            i = 4;
        }
        imageButton.setVisibility(i);
        ImageButton imageButton2 = this.mNextButton;
        if (!hasNext) {
            i2 = 4;
        }
        imageButton2.setVisibility(i2);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewPager viewPager = this.mViewPager;
        measureChild(viewPager, widthMeasureSpec, heightMeasureSpec);
        -wrap6(viewPager.getMeasuredWidthAndState(), viewPager.getMeasuredHeightAndState());
        int pagerWidth = viewPager.getMeasuredWidth();
        int pagerHeight = viewPager.getMeasuredHeight();
        int buttonWidthSpec = MeasureSpec.makeMeasureSpec(pagerWidth, Integer.MIN_VALUE);
        int buttonHeightSpec = MeasureSpec.makeMeasureSpec(pagerHeight, Integer.MIN_VALUE);
        this.mPrevButton.measure(buttonWidthSpec, buttonHeightSpec);
        this.mNextButton.measure(buttonWidthSpec, buttonHeightSpec);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        requestLayout();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ImageButton leftButton;
        ImageButton rightButton;
        if (isLayoutRtl()) {
            leftButton = this.mNextButton;
            rightButton = this.mPrevButton;
        } else {
            leftButton = this.mPrevButton;
            rightButton = this.mNextButton;
        }
        int width = right - left;
        this.mViewPager.layout(0, 0, width, bottom - top);
        SimpleMonthView monthView = (SimpleMonthView) this.mViewPager.getChildAt(0);
        int monthHeight = monthView.getMonthHeight();
        int cellWidth = monthView.getCellWidth();
        int leftDW = leftButton.getMeasuredWidth();
        int leftDH = leftButton.getMeasuredHeight();
        int leftIconTop = monthView.getPaddingTop() + ((monthHeight - leftDH) / 2);
        int leftIconLeft = monthView.getPaddingLeft() + ((cellWidth - leftDW) / 2);
        leftButton.layout(leftIconLeft, leftIconTop, leftIconLeft + leftDW, leftIconTop + leftDH);
        int rightDW = rightButton.getMeasuredWidth();
        int rightDH = rightButton.getMeasuredHeight();
        int rightIconTop = monthView.getPaddingTop() + ((monthHeight - rightDH) / 2);
        int rightIconRight = (width - monthView.getPaddingRight()) - ((cellWidth - rightDW) / 2);
        rightButton.layout(rightIconRight - rightDW, rightIconTop, rightIconRight, rightIconTop + rightDH);
    }

    public void setDayOfWeekTextAppearance(int resId) {
        this.mAdapter.setDayOfWeekTextAppearance(resId);
    }

    public int getDayOfWeekTextAppearance() {
        return this.mAdapter.getDayOfWeekTextAppearance();
    }

    public void setDayTextAppearance(int resId) {
        this.mAdapter.setDayTextAppearance(resId);
    }

    public int getDayTextAppearance() {
        return this.mAdapter.getDayTextAppearance();
    }

    public void setDate(long timeInMillis) {
        setDate(timeInMillis, false);
    }

    public void setDate(long timeInMillis, boolean animate) {
        setDate(timeInMillis, animate, true);
    }

    private void setDate(long timeInMillis, boolean animate, boolean setSelected) {
        boolean dateClamped = false;
        if (timeInMillis < this.mMinDate.getTimeInMillis()) {
            timeInMillis = this.mMinDate.getTimeInMillis();
            dateClamped = true;
        } else if (timeInMillis > this.mMaxDate.getTimeInMillis()) {
            timeInMillis = this.mMaxDate.getTimeInMillis();
            dateClamped = true;
        }
        getTempCalendarForTime(timeInMillis);
        if (setSelected || dateClamped) {
            this.mSelectedDay.setTimeInMillis(timeInMillis);
        }
        int position = getPositionFromDay(timeInMillis);
        if (position != this.mViewPager.getCurrentItem()) {
            this.mViewPager.setCurrentItem(position, animate);
        }
        this.mAdapter.setSelectedDay(this.mTempCalendar);
    }

    public long getDate() {
        return this.mSelectedDay.getTimeInMillis();
    }

    public boolean getBoundsForDate(long timeInMillis, Rect outBounds) {
        if (getPositionFromDay(timeInMillis) != this.mViewPager.getCurrentItem()) {
            return false;
        }
        this.mTempCalendar.setTimeInMillis(timeInMillis);
        return this.mAdapter.getBoundsForDate(this.mTempCalendar, outBounds);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mAdapter.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return this.mAdapter.getFirstDayOfWeek();
    }

    public void setMinDate(long timeInMillis) {
        this.mMinDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMinDate() {
        return this.mMinDate.getTimeInMillis();
    }

    public void setMaxDate(long timeInMillis) {
        this.mMaxDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMaxDate() {
        return this.mMaxDate.getTimeInMillis();
    }

    public void onRangeChanged() {
        this.mAdapter.setRange(this.mMinDate, this.mMaxDate);
        setDate(this.mSelectedDay.getTimeInMillis(), false, false);
        updateButtonVisibility(this.mViewPager.getCurrentItem());
    }

    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        this.mOnDaySelectedListener = listener;
    }

    private int getDiffMonths(Calendar start, Calendar end) {
        return (end.get(2) - start.get(2)) + ((end.get(1) - start.get(1)) * 12);
    }

    private int getPositionFromDay(long timeInMillis) {
        return MathUtils.constrain(getDiffMonths(this.mMinDate, getTempCalendarForTime(timeInMillis)), 0, getDiffMonths(this.mMinDate, this.mMaxDate));
    }

    private Calendar getTempCalendarForTime(long timeInMillis) {
        if (this.mTempCalendar == null) {
            this.mTempCalendar = Calendar.getInstance();
        }
        this.mTempCalendar.setTimeInMillis(timeInMillis);
        return this.mTempCalendar;
    }

    public int getMostVisiblePosition() {
        return this.mViewPager.getCurrentItem();
    }

    public void setPosition(int position) {
        this.mViewPager.setCurrentItem(position, false);
    }
}
