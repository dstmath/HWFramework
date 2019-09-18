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
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.DayPickerPagerAdapter;
import com.android.internal.R;
import com.android.internal.widget.ViewPager;
import java.util.Locale;
import libcore.icu.LocaleData;

class DayPickerView extends ViewGroup {
    private static final int[] ATTRS_TEXT_COLOR = {16842904};
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_LAYOUT = 17367126;
    private static final int DEFAULT_START_YEAR = 1900;
    /* access modifiers changed from: private */
    public final AccessibilityManager mAccessibilityManager;
    private final DayPickerPagerAdapter mAdapter;
    private final Calendar mMaxDate;
    private final Calendar mMinDate;
    /* access modifiers changed from: private */
    public final ImageButton mNextButton;
    private final View.OnClickListener mOnClickListener;
    /* access modifiers changed from: private */
    public OnDaySelectedListener mOnDaySelectedListener;
    private final ViewPager.OnPageChangeListener mOnPageChangedListener;
    /* access modifiers changed from: private */
    public final ImageButton mPrevButton;
    private final Calendar mSelectedDay;
    private Calendar mTempCalendar;
    /* access modifiers changed from: private */
    public final ViewPager mViewPager;

    public interface OnDaySelectedListener {
        void onDaySelected(DayPickerView dayPickerView, Calendar calendar);
    }

    public DayPickerView(Context context) {
        this(context, null);
    }

    public DayPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843613);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DayPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Context context2 = context;
        this.mSelectedDay = Calendar.getInstance();
        this.mMinDate = Calendar.getInstance();
        this.mMaxDate = Calendar.getInstance();
        this.mOnPageChangedListener = new ViewPager.OnPageChangeListener() {
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
        this.mOnClickListener = new View.OnClickListener() {
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
                DayPickerView.this.mViewPager.setCurrentItem(nextItem, !DayPickerView.this.mAccessibilityManager.isEnabled());
            }
        };
        this.mAccessibilityManager = (AccessibilityManager) context2.getSystemService("accessibility");
        TypedArray a = context2.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
        int firstDayOfWeek = a.getInt(0, LocaleData.get(Locale.getDefault()).firstDayOfWeek.intValue());
        String minDate = a.getString(2);
        String maxDate = a.getString(3);
        int monthTextAppearanceResId = a.getResourceId(16, 16974753);
        int dayOfWeekTextAppearanceResId = a.getResourceId(11, 16974752);
        int dayTextAppearanceResId = a.getResourceId(12, 16974751);
        ColorStateList daySelectorColor = a.getColorStateList(15);
        a.recycle();
        this.mAdapter = new DayPickerPagerAdapter(context2, 17367124, 16909106);
        this.mAdapter.setMonthTextAppearance(monthTextAppearanceResId);
        this.mAdapter.setDayOfWeekTextAppearance(dayOfWeekTextAppearanceResId);
        this.mAdapter.setDayTextAppearance(dayTextAppearanceResId);
        this.mAdapter.setDaySelectorColor(daySelectorColor);
        LayoutInflater inflater = LayoutInflater.from(context);
        int i = 0;
        ViewGroup content = (ViewGroup) inflater.inflate((int) DEFAULT_LAYOUT, (ViewGroup) this, false);
        while (content.getChildCount() > 0) {
            View child = content.getChildAt(i);
            content.removeViewAt(i);
            addView(child);
            Context context3 = context;
            i = 0;
        }
        this.mPrevButton = (ImageButton) findViewById(16909222);
        this.mPrevButton.setOnClickListener(this.mOnClickListener);
        this.mNextButton = (ImageButton) findViewById(16909121);
        this.mNextButton.setOnClickListener(this.mOnClickListener);
        this.mViewPager = findViewById(16908852);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOnPageChangeListener(this.mOnPageChangedListener);
        if (monthTextAppearanceResId != 0) {
            TypedArray typedArray = a;
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
        if (maxDateMillis >= minDateMillis) {
            LayoutInflater layoutInflater = inflater;
            String str = minDate;
            long setDateMillis = MathUtils.constrain(System.currentTimeMillis(), minDateMillis, maxDateMillis);
            setFirstDayOfWeek(firstDayOfWeek);
            setMinDate(minDateMillis);
            setMaxDate(maxDateMillis);
            setDate(setDateMillis, false);
            Calendar calendar = tempDate;
            this.mAdapter.setOnDaySelectedListener(new DayPickerPagerAdapter.OnDaySelectedListener() {
                public void onDaySelected(DayPickerPagerAdapter adapter, Calendar day) {
                    if (DayPickerView.this.mOnDaySelectedListener != null) {
                        DayPickerView.this.mOnDaySelectedListener.onDaySelected(DayPickerView.this, day);
                    }
                }
            });
            return;
        }
        LayoutInflater layoutInflater2 = inflater;
        String str2 = minDate;
        throw new IllegalArgumentException("maxDate must be >= minDate");
    }

    /* access modifiers changed from: private */
    public void updateButtonVisibility(int position) {
        boolean hasNext = true;
        int i = 0;
        boolean hasPrev = position > 0;
        if (position >= this.mAdapter.getCount() - 1) {
            hasNext = false;
        }
        this.mPrevButton.setVisibility(hasPrev ? 0 : 4);
        ImageButton imageButton = this.mNextButton;
        if (!hasNext) {
            i = 4;
        }
        imageButton.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewPager viewPager = this.mViewPager;
        measureChild(viewPager, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(viewPager.getMeasuredWidthAndState(), viewPager.getMeasuredHeightAndState());
        int pagerWidth = viewPager.getMeasuredWidth();
        int pagerHeight = viewPager.getMeasuredHeight();
        int buttonWidthSpec = View.MeasureSpec.makeMeasureSpec(pagerWidth, Integer.MIN_VALUE);
        int buttonHeightSpec = View.MeasureSpec.makeMeasureSpec(pagerHeight, Integer.MIN_VALUE);
        this.mPrevButton.measure(buttonWidthSpec, buttonHeightSpec);
        this.mNextButton.measure(buttonWidthSpec, buttonHeightSpec);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ImageButton rightButton;
        ImageButton leftButton;
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
        ImageButton imageButton = leftButton;
        int rightIconTop = monthView.getPaddingTop() + ((monthHeight - rightDH) / 2);
        int rightIconRight = (width - monthView.getPaddingRight()) - ((cellWidth - rightDW) / 2);
        int i = width;
        int i2 = rightDW;
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
        return (end.get(2) - start.get(2)) + (12 * (end.get(1) - start.get(1)));
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
