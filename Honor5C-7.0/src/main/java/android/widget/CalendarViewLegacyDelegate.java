package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CalendarView.OnDateChangeListener;
import com.android.internal.R;
import java.util.Locale;
import libcore.icu.LocaleData;

class CalendarViewLegacyDelegate extends AbstractCalendarViewDelegate {
    private static final int ADJUSTMENT_SCROLL_DURATION = 500;
    private static final int DAYS_PER_WEEK = 7;
    private static final int DEFAULT_DATE_TEXT_SIZE = 14;
    private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;
    private static final boolean DEFAULT_SHOW_WEEK_NUMBER = true;
    private static final int DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID = -1;
    private static final int GOTO_SCROLL_DURATION = 1000;
    private static final long MILLIS_IN_DAY = 86400000;
    private static final long MILLIS_IN_WEEK = 604800000;
    private static final int SCROLL_CHANGE_DELAY = 40;
    private static final int SCROLL_HYST_WEEKS = 2;
    private static final int UNSCALED_BOTTOM_BUFFER = 20;
    private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 2;
    private static final int UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH = 6;
    private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;
    private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;
    private WeeksAdapter mAdapter;
    private int mBottomBuffer;
    private int mCurrentMonthDisplayed;
    private int mCurrentScrollState;
    private int mDateTextAppearanceResId;
    private int mDateTextSize;
    private ViewGroup mDayNamesHeader;
    private String[] mDayNamesLong;
    private String[] mDayNamesShort;
    private int mDaysPerWeek;
    private Calendar mFirstDayOfMonth;
    private int mFirstDayOfWeek;
    private int mFocusedMonthDateColor;
    private float mFriction;
    private boolean mIsScrollingUp;
    private int mListScrollTopOffset;
    private ListView mListView;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private TextView mMonthName;
    private OnDateChangeListener mOnDateChangeListener;
    private long mPreviousScrollPosition;
    private int mPreviousScrollState;
    private ScrollStateRunnable mScrollStateChangedRunnable;
    private Drawable mSelectedDateVerticalBar;
    private final int mSelectedDateVerticalBarWidth;
    private int mSelectedWeekBackgroundColor;
    private boolean mShowWeekNumber;
    private int mShownWeekCount;
    private Calendar mTempDate;
    private int mUnfocusedMonthDateColor;
    private float mVelocityScale;
    private int mWeekDayTextAppearanceResId;
    private int mWeekMinVisibleHeight;
    private int mWeekNumberColor;
    private int mWeekSeparatorLineColor;
    private final int mWeekSeperatorLineWidth;

    private class ScrollStateRunnable implements Runnable {
        private int mNewState;
        private AbsListView mView;

        private ScrollStateRunnable() {
        }

        public void doScrollStateChange(AbsListView view, int scrollState) {
            this.mView = view;
            this.mNewState = scrollState;
            CalendarViewLegacyDelegate.this.mDelegator.removeCallbacks(this);
            CalendarViewLegacyDelegate.this.mDelegator.postDelayed(this, 40);
        }

        public void run() {
            CalendarViewLegacyDelegate.this.mCurrentScrollState = this.mNewState;
            if (this.mNewState == 0 && CalendarViewLegacyDelegate.this.mPreviousScrollState != 0) {
                View child = this.mView.getChildAt(0);
                if (child != null) {
                    int dist = child.getBottom() - CalendarViewLegacyDelegate.this.mListScrollTopOffset;
                    if (dist > CalendarViewLegacyDelegate.this.mListScrollTopOffset) {
                        if (CalendarViewLegacyDelegate.this.mIsScrollingUp) {
                            this.mView.smoothScrollBy(dist - child.getHeight(), CalendarViewLegacyDelegate.ADJUSTMENT_SCROLL_DURATION);
                        } else {
                            this.mView.smoothScrollBy(dist, CalendarViewLegacyDelegate.ADJUSTMENT_SCROLL_DURATION);
                        }
                    }
                } else {
                    return;
                }
            }
            CalendarViewLegacyDelegate.this.mPreviousScrollState = this.mNewState;
        }
    }

    private class WeekView extends View {
        private String[] mDayNumbers;
        private final Paint mDrawPaint;
        private Calendar mFirstDay;
        private boolean[] mFocusDay;
        private boolean mHasFocusedDay;
        private boolean mHasSelectedDay;
        private boolean mHasUnfocusedDay;
        private int mHeight;
        private int mLastWeekDayMonth;
        private final Paint mMonthNumDrawPaint;
        private int mMonthOfFirstWeekDay;
        private int mNumCells;
        private int mSelectedDay;
        private int mSelectedLeft;
        private int mSelectedRight;
        private final Rect mTempRect;
        private int mWeek;
        private int mWidth;

        public WeekView(Context context) {
            super(context);
            this.mTempRect = new Rect();
            this.mDrawPaint = new Paint();
            this.mMonthNumDrawPaint = new Paint();
            this.mMonthOfFirstWeekDay = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            this.mLastWeekDayMonth = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            this.mWeek = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            this.mHasSelectedDay = false;
            this.mSelectedDay = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            this.mSelectedLeft = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            this.mSelectedRight = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            initilaizePaints();
        }

        public void init(int weekNumber, int selectedWeekDay, int focusedMonth) {
            int -get2;
            this.mSelectedDay = selectedWeekDay;
            this.mHasSelectedDay = this.mSelectedDay != CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID ? CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER : false;
            if (CalendarViewLegacyDelegate.this.mShowWeekNumber) {
                -get2 = CalendarViewLegacyDelegate.this.mDaysPerWeek + CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
            } else {
                -get2 = CalendarViewLegacyDelegate.this.mDaysPerWeek;
            }
            this.mNumCells = -get2;
            this.mWeek = weekNumber;
            CalendarViewLegacyDelegate.this.mTempDate.setTimeInMillis(CalendarViewLegacyDelegate.this.mMinDate.getTimeInMillis());
            CalendarViewLegacyDelegate.this.mTempDate.add(3, this.mWeek);
            CalendarViewLegacyDelegate.this.mTempDate.setFirstDayOfWeek(CalendarViewLegacyDelegate.this.mFirstDayOfWeek);
            this.mDayNumbers = new String[this.mNumCells];
            this.mFocusDay = new boolean[this.mNumCells];
            int i = 0;
            if (CalendarViewLegacyDelegate.this.mShowWeekNumber) {
                String[] strArr = this.mDayNumbers;
                Object[] objArr = new Object[CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH];
                objArr[0] = Integer.valueOf(CalendarViewLegacyDelegate.this.mTempDate.get(3));
                strArr[0] = String.format(Locale.getDefault(), "%d", objArr);
                i = CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
            }
            CalendarViewLegacyDelegate.this.mTempDate.add(5, CalendarViewLegacyDelegate.this.mFirstDayOfWeek - CalendarViewLegacyDelegate.this.mTempDate.get(CalendarViewLegacyDelegate.DAYS_PER_WEEK));
            this.mFirstDay = (Calendar) CalendarViewLegacyDelegate.this.mTempDate.clone();
            this.mMonthOfFirstWeekDay = CalendarViewLegacyDelegate.this.mTempDate.get(CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET);
            this.mHasUnfocusedDay = CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER;
            for (i = 
            /* Method generation error in method: android.widget.CalendarViewLegacyDelegate.WeekView.init(int, int, int):void
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r1_2 'i' int) = (r1_0 'i' int), (r1_1 'i' int) binds: {(r1_0 'i' int)=B:7:0x005f, (r1_1 'i' int)=B:8:0x0061} in method: android.widget.CalendarViewLegacyDelegate.WeekView.init(int, int, int):void
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:239)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: android.widget.CalendarViewLegacyDelegate.WeekView.init(int, int, int):void
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 18 more
 */

            private void initilaizePaints() {
                this.mDrawPaint.setFakeBoldText(false);
                this.mDrawPaint.setAntiAlias(CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER);
                this.mDrawPaint.setStyle(Style.FILL);
                this.mMonthNumDrawPaint.setFakeBoldText(CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER);
                this.mMonthNumDrawPaint.setAntiAlias(CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER);
                this.mMonthNumDrawPaint.setStyle(Style.FILL);
                this.mMonthNumDrawPaint.setTextAlign(Align.CENTER);
                this.mMonthNumDrawPaint.setTextSize((float) CalendarViewLegacyDelegate.this.mDateTextSize);
            }

            public int getMonthOfFirstWeekDay() {
                return this.mMonthOfFirstWeekDay;
            }

            public int getMonthOfLastWeekDay() {
                return this.mLastWeekDayMonth;
            }

            public Calendar getFirstDay() {
                return this.mFirstDay;
            }

            public boolean getDayFromLocation(float x, Calendar outCalendar) {
                int start;
                int end;
                boolean isLayoutRtl = isLayoutRtl();
                if (isLayoutRtl) {
                    start = 0;
                    end = CalendarViewLegacyDelegate.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
                } else {
                    start = CalendarViewLegacyDelegate.this.mShowWeekNumber ? this.mWidth / this.mNumCells : 0;
                    end = this.mWidth;
                }
                if (x < ((float) start) || x > ((float) end)) {
                    outCalendar.clear();
                    return false;
                }
                int dayPosition = (int) (((x - ((float) start)) * ((float) CalendarViewLegacyDelegate.this.mDaysPerWeek)) / ((float) (end - start)));
                if (isLayoutRtl) {
                    dayPosition = (CalendarViewLegacyDelegate.this.mDaysPerWeek + CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID) - dayPosition;
                }
                outCalendar.setTimeInMillis(this.mFirstDay.getTimeInMillis());
                outCalendar.add(5, dayPosition);
                return CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER;
            }

            protected void onDraw(Canvas canvas) {
                drawBackground(canvas);
                drawWeekNumbersAndDates(canvas);
                drawWeekSeparators(canvas);
                drawSelectedDateVerticalBars(canvas);
            }

            private void drawBackground(Canvas canvas) {
                int i = 0;
                if (this.mHasSelectedDay) {
                    this.mDrawPaint.setColor(CalendarViewLegacyDelegate.this.mSelectedWeekBackgroundColor);
                    this.mTempRect.top = CalendarViewLegacyDelegate.this.mWeekSeperatorLineWidth;
                    this.mTempRect.bottom = this.mHeight;
                    boolean isLayoutRtl = isLayoutRtl();
                    if (isLayoutRtl) {
                        this.mTempRect.left = 0;
                        this.mTempRect.right = this.mSelectedLeft - 2;
                    } else {
                        Rect rect = this.mTempRect;
                        if (CalendarViewLegacyDelegate.this.mShowWeekNumber) {
                            i = this.mWidth / this.mNumCells;
                        }
                        rect.left = i;
                        this.mTempRect.right = this.mSelectedLeft - 2;
                    }
                    canvas.drawRect(this.mTempRect, this.mDrawPaint);
                    if (isLayoutRtl) {
                        this.mTempRect.left = this.mSelectedRight + 3;
                        this.mTempRect.right = CalendarViewLegacyDelegate.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
                    } else {
                        this.mTempRect.left = this.mSelectedRight + 3;
                        this.mTempRect.right = this.mWidth;
                    }
                    canvas.drawRect(this.mTempRect, this.mDrawPaint);
                }
            }

            private void drawWeekNumbersAndDates(Canvas canvas) {
                int y = ((int) ((((float) this.mHeight) + this.mDrawPaint.getTextSize()) / 2.0f)) - CalendarViewLegacyDelegate.this.mWeekSeperatorLineWidth;
                int nDays = this.mNumCells;
                int divisor = nDays * CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET;
                this.mDrawPaint.setTextAlign(Align.CENTER);
                this.mDrawPaint.setTextSize((float) CalendarViewLegacyDelegate.this.mDateTextSize);
                int i = 0;
                int -get4;
                if (isLayoutRtl()) {
                    while (i < nDays + CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID) {
                        Paint paint = this.mMonthNumDrawPaint;
                        if (this.mFocusDay[i]) {
                            -get4 = CalendarViewLegacyDelegate.this.mFocusedMonthDateColor;
                        } else {
                            -get4 = CalendarViewLegacyDelegate.this.mUnfocusedMonthDateColor;
                        }
                        paint.setColor(-get4);
                        canvas.drawText(this.mDayNumbers[(nDays + CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID) - i], (float) ((((i * CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET) + CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) * this.mWidth) / divisor), (float) y, this.mMonthNumDrawPaint);
                        i += CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                    }
                    if (CalendarViewLegacyDelegate.this.mShowWeekNumber) {
                        this.mDrawPaint.setColor(CalendarViewLegacyDelegate.this.mWeekNumberColor);
                        canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth - (this.mWidth / divisor)), (float) y, this.mDrawPaint);
                        return;
                    }
                    return;
                }
                if (CalendarViewLegacyDelegate.this.mShowWeekNumber) {
                    this.mDrawPaint.setColor(CalendarViewLegacyDelegate.this.mWeekNumberColor);
                    canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth / divisor), (float) y, this.mDrawPaint);
                    i = CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                }
                while (i < nDays) {
                    paint = this.mMonthNumDrawPaint;
                    if (this.mFocusDay[i]) {
                        -get4 = CalendarViewLegacyDelegate.this.mFocusedMonthDateColor;
                    } else {
                        -get4 = CalendarViewLegacyDelegate.this.mUnfocusedMonthDateColor;
                    }
                    paint.setColor(-get4);
                    canvas.drawText(this.mDayNumbers[i], (float) ((((i * CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET) + CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) * this.mWidth) / divisor), (float) y, this.mMonthNumDrawPaint);
                    i += CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                }
            }

            private void drawWeekSeparators(Canvas canvas) {
                int i = 0;
                int firstFullyVisiblePosition = CalendarViewLegacyDelegate.this.mListView.getFirstVisiblePosition();
                if (CalendarViewLegacyDelegate.this.mListView.getChildAt(0).getTop() < 0) {
                    firstFullyVisiblePosition += CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                }
                if (firstFullyVisiblePosition != this.mWeek) {
                    float startX;
                    float stopX;
                    this.mDrawPaint.setColor(CalendarViewLegacyDelegate.this.mWeekSeparatorLineColor);
                    this.mDrawPaint.setStrokeWidth((float) CalendarViewLegacyDelegate.this.mWeekSeperatorLineWidth);
                    if (isLayoutRtl()) {
                        startX = 0.0f;
                        stopX = (float) (CalendarViewLegacyDelegate.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth);
                    } else {
                        if (CalendarViewLegacyDelegate.this.mShowWeekNumber) {
                            i = this.mWidth / this.mNumCells;
                        }
                        startX = (float) i;
                        stopX = (float) this.mWidth;
                    }
                    canvas.drawLine(startX, 0.0f, stopX, 0.0f, this.mDrawPaint);
                }
            }

            private void drawSelectedDateVerticalBars(Canvas canvas) {
                if (this.mHasSelectedDay) {
                    CalendarViewLegacyDelegate.this.mSelectedDateVerticalBar.setBounds(this.mSelectedLeft - (CalendarViewLegacyDelegate.this.mSelectedDateVerticalBarWidth / CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET), CalendarViewLegacyDelegate.this.mWeekSeperatorLineWidth, this.mSelectedLeft + (CalendarViewLegacyDelegate.this.mSelectedDateVerticalBarWidth / CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET), this.mHeight);
                    CalendarViewLegacyDelegate.this.mSelectedDateVerticalBar.draw(canvas);
                    CalendarViewLegacyDelegate.this.mSelectedDateVerticalBar.setBounds(this.mSelectedRight - (CalendarViewLegacyDelegate.this.mSelectedDateVerticalBarWidth / CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET), CalendarViewLegacyDelegate.this.mWeekSeperatorLineWidth, this.mSelectedRight + (CalendarViewLegacyDelegate.this.mSelectedDateVerticalBarWidth / CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET), this.mHeight);
                    CalendarViewLegacyDelegate.this.mSelectedDateVerticalBar.draw(canvas);
                }
            }

            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                this.mWidth = w;
                updateSelectionPositions();
            }

            private void updateSelectionPositions() {
                if (this.mHasSelectedDay) {
                    boolean isLayoutRtl = isLayoutRtl();
                    int selectedPosition = this.mSelectedDay - CalendarViewLegacyDelegate.this.mFirstDayOfWeek;
                    if (selectedPosition < 0) {
                        selectedPosition += CalendarViewLegacyDelegate.DAYS_PER_WEEK;
                    }
                    if (CalendarViewLegacyDelegate.this.mShowWeekNumber && !isLayoutRtl) {
                        selectedPosition += CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                    }
                    if (isLayoutRtl) {
                        this.mSelectedLeft = (((CalendarViewLegacyDelegate.this.mDaysPerWeek + CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID) - selectedPosition) * this.mWidth) / this.mNumCells;
                    } else {
                        this.mSelectedLeft = (this.mWidth * selectedPosition) / this.mNumCells;
                    }
                    this.mSelectedRight = this.mSelectedLeft + (this.mWidth / this.mNumCells);
                }
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                this.mHeight = ((CalendarViewLegacyDelegate.this.mListView.getHeight() - CalendarViewLegacyDelegate.this.mListView.getPaddingTop()) - CalendarViewLegacyDelegate.this.mListView.getPaddingBottom()) / CalendarViewLegacyDelegate.this.mShownWeekCount;
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), this.mHeight);
            }
        }

        private class WeeksAdapter extends BaseAdapter implements OnTouchListener {
            private int mFocusedMonth;
            private GestureDetector mGestureDetector;
            private final Calendar mSelectedDate;
            private int mSelectedWeek;
            private int mTotalWeekCount;

            class CalendarGestureListener extends SimpleOnGestureListener {
                CalendarGestureListener() {
                }

                public boolean onSingleTapUp(MotionEvent e) {
                    return CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER;
                }
            }

            public WeeksAdapter(Context context) {
                this.mSelectedDate = Calendar.getInstance();
                CalendarViewLegacyDelegate.this.mContext = context;
                this.mGestureDetector = new GestureDetector(CalendarViewLegacyDelegate.this.mContext, new CalendarGestureListener());
                init();
            }

            private void init() {
                this.mSelectedWeek = CalendarViewLegacyDelegate.this.getWeeksSinceMinDate(this.mSelectedDate);
                this.mTotalWeekCount = CalendarViewLegacyDelegate.this.getWeeksSinceMinDate(CalendarViewLegacyDelegate.this.mMaxDate);
                if (!(CalendarViewLegacyDelegate.this.mMinDate.get(CalendarViewLegacyDelegate.DAYS_PER_WEEK) == CalendarViewLegacyDelegate.this.mFirstDayOfWeek && CalendarViewLegacyDelegate.this.mMaxDate.get(CalendarViewLegacyDelegate.DAYS_PER_WEEK) == CalendarViewLegacyDelegate.this.mFirstDayOfWeek)) {
                    this.mTotalWeekCount += CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                }
                notifyDataSetChanged();
            }

            public void setSelectedDay(Calendar selectedDay) {
                if (selectedDay.get(CalendarViewLegacyDelegate.UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH) != this.mSelectedDate.get(CalendarViewLegacyDelegate.UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH) || selectedDay.get(CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) != this.mSelectedDate.get(CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH)) {
                    this.mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
                    this.mSelectedWeek = CalendarViewLegacyDelegate.this.getWeeksSinceMinDate(this.mSelectedDate);
                    this.mFocusedMonth = this.mSelectedDate.get(CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET);
                    notifyDataSetChanged();
                }
            }

            public Calendar getSelectedDay() {
                return this.mSelectedDate;
            }

            public int getCount() {
                return this.mTotalWeekCount;
            }

            public Object getItem(int position) {
                return null;
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                WeekView weekView;
                int selectedWeekDay;
                if (convertView != null) {
                    weekView = (WeekView) convertView;
                } else {
                    weekView = new WeekView(CalendarViewLegacyDelegate.this.mContext);
                    weekView.setLayoutParams(new LayoutParams(-2, -2));
                    weekView.setClickable(CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER);
                    weekView.setOnTouchListener(this);
                }
                if (this.mSelectedWeek == position) {
                    selectedWeekDay = this.mSelectedDate.get(CalendarViewLegacyDelegate.DAYS_PER_WEEK);
                } else {
                    selectedWeekDay = CalendarViewLegacyDelegate.DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
                }
                weekView.init(position, selectedWeekDay, this.mFocusedMonth);
                return weekView;
            }

            public void setFocusMonth(int month) {
                if (this.mFocusedMonth != month) {
                    this.mFocusedMonth = month;
                    notifyDataSetChanged();
                }
            }

            public boolean onTouch(View v, MotionEvent event) {
                if (!CalendarViewLegacyDelegate.this.mListView.isEnabled() || !this.mGestureDetector.onTouchEvent(event)) {
                    return false;
                }
                if (!((WeekView) v).getDayFromLocation(event.getX(), CalendarViewLegacyDelegate.this.mTempDate) || CalendarViewLegacyDelegate.this.mTempDate.before(CalendarViewLegacyDelegate.this.mMinDate) || CalendarViewLegacyDelegate.this.mTempDate.after(CalendarViewLegacyDelegate.this.mMaxDate)) {
                    return CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER;
                }
                onDateTapped(CalendarViewLegacyDelegate.this.mTempDate);
                return CalendarViewLegacyDelegate.DEFAULT_SHOW_WEEK_NUMBER;
            }

            private void onDateTapped(Calendar day) {
                setSelectedDay(day);
                CalendarViewLegacyDelegate.this.setMonthDisplayed(day);
            }
        }

        CalendarViewLegacyDelegate(CalendarView delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(delegator, context);
            this.mListScrollTopOffset = UNSCALED_LIST_SCROLL_TOP_OFFSET;
            this.mWeekMinVisibleHeight = UNSCALED_WEEK_MIN_VISIBLE_HEIGHT;
            this.mBottomBuffer = UNSCALED_BOTTOM_BUFFER;
            this.mDaysPerWeek = DAYS_PER_WEEK;
            this.mFriction = 0.05f;
            this.mVelocityScale = 0.333f;
            this.mCurrentMonthDisplayed = DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            this.mIsScrollingUp = false;
            this.mPreviousScrollState = 0;
            this.mCurrentScrollState = 0;
            this.mScrollStateChangedRunnable = new ScrollStateRunnable();
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
            this.mShowWeekNumber = a.getBoolean(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, DEFAULT_SHOW_WEEK_NUMBER);
            this.mFirstDayOfWeek = a.getInt(0, LocaleData.get(Locale.getDefault()).firstDayOfWeek.intValue());
            if (!CalendarView.parseDate(a.getString(UNSCALED_LIST_SCROLL_TOP_OFFSET), this.mMinDate)) {
                CalendarView.parseDate("01/01/1900", this.mMinDate);
            }
            if (!CalendarView.parseDate(a.getString(3), this.mMaxDate)) {
                CalendarView.parseDate("01/01/2100", this.mMaxDate);
            }
            if (this.mMaxDate.before(this.mMinDate)) {
                throw new IllegalArgumentException("Max date cannot be before min date.");
            }
            this.mShownWeekCount = a.getInt(4, UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH);
            this.mSelectedWeekBackgroundColor = a.getColor(5, 0);
            this.mFocusedMonthDateColor = a.getColor(UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH, 0);
            this.mUnfocusedMonthDateColor = a.getColor(DAYS_PER_WEEK, 0);
            this.mWeekSeparatorLineColor = a.getColor(9, 0);
            this.mWeekNumberColor = a.getColor(8, 0);
            this.mSelectedDateVerticalBar = a.getDrawable(10);
            this.mDateTextAppearanceResId = a.getResourceId(UNSCALED_WEEK_MIN_VISIBLE_HEIGHT, R.style.TextAppearance_Small);
            updateDateTextSize();
            this.mWeekDayTextAppearanceResId = a.getResourceId(11, DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID);
            a.recycle();
            DisplayMetrics displayMetrics = this.mDelegator.getResources().getDisplayMetrics();
            this.mWeekMinVisibleHeight = (int) TypedValue.applyDimension(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, 12.0f, displayMetrics);
            this.mListScrollTopOffset = (int) TypedValue.applyDimension(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, 2.0f, displayMetrics);
            this.mBottomBuffer = (int) TypedValue.applyDimension(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, 20.0f, displayMetrics);
            this.mSelectedDateVerticalBarWidth = (int) TypedValue.applyDimension(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, 6.0f, displayMetrics);
            this.mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, displayMetrics);
            View content = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate((int) R.layout.calendar_view, null, false);
            this.mDelegator.addView(content);
            this.mListView = (ListView) this.mDelegator.findViewById(R.id.list);
            this.mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names);
            this.mMonthName = (TextView) content.findViewById(R.id.month_name);
            setUpHeader();
            setUpListView();
            setUpAdapter();
            this.mTempDate.setTimeInMillis(System.currentTimeMillis());
            if (this.mTempDate.before(this.mMinDate)) {
                goTo(this.mMinDate, false, DEFAULT_SHOW_WEEK_NUMBER, DEFAULT_SHOW_WEEK_NUMBER);
            } else if (this.mMaxDate.before(this.mTempDate)) {
                goTo(this.mMaxDate, false, DEFAULT_SHOW_WEEK_NUMBER, DEFAULT_SHOW_WEEK_NUMBER);
            } else {
                goTo(this.mTempDate, false, DEFAULT_SHOW_WEEK_NUMBER, DEFAULT_SHOW_WEEK_NUMBER);
            }
            this.mDelegator.invalidate();
        }

        public void setShownWeekCount(int count) {
            if (this.mShownWeekCount != count) {
                this.mShownWeekCount = count;
                this.mDelegator.invalidate();
            }
        }

        public int getShownWeekCount() {
            return this.mShownWeekCount;
        }

        public void setSelectedWeekBackgroundColor(int color) {
            if (this.mSelectedWeekBackgroundColor != color) {
                this.mSelectedWeekBackgroundColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasSelectedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public int getSelectedWeekBackgroundColor() {
            return this.mSelectedWeekBackgroundColor;
        }

        public void setFocusedMonthDateColor(int color) {
            if (this.mFocusedMonthDateColor != color) {
                this.mFocusedMonthDateColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasFocusedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public int getFocusedMonthDateColor() {
            return this.mFocusedMonthDateColor;
        }

        public void setUnfocusedMonthDateColor(int color) {
            if (this.mUnfocusedMonthDateColor != color) {
                this.mUnfocusedMonthDateColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasUnfocusedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public int getUnfocusedMonthDateColor() {
            return this.mUnfocusedMonthDateColor;
        }

        public void setWeekNumberColor(int color) {
            if (this.mWeekNumberColor != color) {
                this.mWeekNumberColor = color;
                if (this.mShowWeekNumber) {
                    invalidateAllWeekViews();
                }
            }
        }

        public int getWeekNumberColor() {
            return this.mWeekNumberColor;
        }

        public void setWeekSeparatorLineColor(int color) {
            if (this.mWeekSeparatorLineColor != color) {
                this.mWeekSeparatorLineColor = color;
                invalidateAllWeekViews();
            }
        }

        public int getWeekSeparatorLineColor() {
            return this.mWeekSeparatorLineColor;
        }

        public void setSelectedDateVerticalBar(int resourceId) {
            setSelectedDateVerticalBar(this.mDelegator.getContext().getDrawable(resourceId));
        }

        public void setSelectedDateVerticalBar(Drawable drawable) {
            if (this.mSelectedDateVerticalBar != drawable) {
                this.mSelectedDateVerticalBar = drawable;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasSelectedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public Drawable getSelectedDateVerticalBar() {
            return this.mSelectedDateVerticalBar;
        }

        public void setWeekDayTextAppearance(int resourceId) {
            if (this.mWeekDayTextAppearanceResId != resourceId) {
                this.mWeekDayTextAppearanceResId = resourceId;
                setUpHeader();
            }
        }

        public int getWeekDayTextAppearance() {
            return this.mWeekDayTextAppearanceResId;
        }

        public void setDateTextAppearance(int resourceId) {
            if (this.mDateTextAppearanceResId != resourceId) {
                this.mDateTextAppearanceResId = resourceId;
                updateDateTextSize();
                invalidateAllWeekViews();
            }
        }

        public int getDateTextAppearance() {
            return this.mDateTextAppearanceResId;
        }

        public void setMinDate(long minDate) {
            this.mTempDate.setTimeInMillis(minDate);
            if (!isSameDate(this.mTempDate, this.mMinDate)) {
                this.mMinDate.setTimeInMillis(minDate);
                Calendar date = this.mAdapter.mSelectedDate;
                if (date.before(this.mMinDate)) {
                    this.mAdapter.setSelectedDay(this.mMinDate);
                }
                this.mAdapter.init();
                if (date.before(this.mMinDate)) {
                    setDate(this.mTempDate.getTimeInMillis());
                } else {
                    goTo(date, false, DEFAULT_SHOW_WEEK_NUMBER, false);
                }
            }
        }

        public long getMinDate() {
            return this.mMinDate.getTimeInMillis();
        }

        public void setMaxDate(long maxDate) {
            this.mTempDate.setTimeInMillis(maxDate);
            if (!isSameDate(this.mTempDate, this.mMaxDate)) {
                this.mMaxDate.setTimeInMillis(maxDate);
                this.mAdapter.init();
                Calendar date = this.mAdapter.mSelectedDate;
                if (date.after(this.mMaxDate)) {
                    setDate(this.mMaxDate.getTimeInMillis());
                } else {
                    goTo(date, false, DEFAULT_SHOW_WEEK_NUMBER, false);
                }
            }
        }

        public long getMaxDate() {
            return this.mMaxDate.getTimeInMillis();
        }

        public void setShowWeekNumber(boolean showWeekNumber) {
            if (this.mShowWeekNumber != showWeekNumber) {
                this.mShowWeekNumber = showWeekNumber;
                this.mAdapter.notifyDataSetChanged();
                setUpHeader();
            }
        }

        public boolean getShowWeekNumber() {
            return this.mShowWeekNumber;
        }

        public void setFirstDayOfWeek(int firstDayOfWeek) {
            if (this.mFirstDayOfWeek != firstDayOfWeek) {
                this.mFirstDayOfWeek = firstDayOfWeek;
                this.mAdapter.init();
                this.mAdapter.notifyDataSetChanged();
                setUpHeader();
            }
        }

        public int getFirstDayOfWeek() {
            return this.mFirstDayOfWeek;
        }

        public void setDate(long date) {
            setDate(date, false, false);
        }

        public void setDate(long date, boolean animate, boolean center) {
            this.mTempDate.setTimeInMillis(date);
            if (!isSameDate(this.mTempDate, this.mAdapter.mSelectedDate)) {
                goTo(this.mTempDate, animate, DEFAULT_SHOW_WEEK_NUMBER, center);
            }
        }

        public long getDate() {
            return this.mAdapter.mSelectedDate.getTimeInMillis();
        }

        public void setOnDateChangeListener(OnDateChangeListener listener) {
            this.mOnDateChangeListener = listener;
        }

        public void onConfigurationChanged(Configuration newConfig) {
            setCurrentLocale(newConfig.locale);
        }

        protected void setCurrentLocale(Locale locale) {
            super.setCurrentLocale(locale);
            this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
            this.mFirstDayOfMonth = getCalendarForLocale(this.mFirstDayOfMonth, locale);
            this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
            this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
        }

        private void updateDateTextSize() {
            TypedArray dateTextAppearance = this.mDelegator.getContext().obtainStyledAttributes(this.mDateTextAppearanceResId, R.styleable.TextAppearance);
            this.mDateTextSize = dateTextAppearance.getDimensionPixelSize(0, DEFAULT_DATE_TEXT_SIZE);
            dateTextAppearance.recycle();
        }

        private void invalidateAllWeekViews() {
            int childCount = this.mListView.getChildCount();
            for (int i = 0; i < childCount; i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                this.mListView.getChildAt(i).invalidate();
            }
        }

        private static Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
            if (oldCalendar == null) {
                return Calendar.getInstance(locale);
            }
            long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }

        private static boolean isSameDate(Calendar firstDate, Calendar secondDate) {
            if (firstDate.get(UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH) == secondDate.get(UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH)) {
                return firstDate.get(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) == secondDate.get(UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) ? DEFAULT_SHOW_WEEK_NUMBER : false;
            } else {
                return false;
            }
        }

        private void setUpAdapter() {
            if (this.mAdapter == null) {
                this.mAdapter = new WeeksAdapter(this.mContext);
                this.mAdapter.registerDataSetObserver(new DataSetObserver() {
                    public void onChanged() {
                        if (CalendarViewLegacyDelegate.this.mOnDateChangeListener != null) {
                            Calendar selectedDay = CalendarViewLegacyDelegate.this.mAdapter.getSelectedDay();
                            CalendarViewLegacyDelegate.this.mOnDateChangeListener.onSelectedDayChange(CalendarViewLegacyDelegate.this.mDelegator, selectedDay.get(CalendarViewLegacyDelegate.UNSCALED_WEEK_SEPARATOR_LINE_WIDTH), selectedDay.get(CalendarViewLegacyDelegate.UNSCALED_LIST_SCROLL_TOP_OFFSET), selectedDay.get(5));
                        }
                    }
                });
                this.mListView.setAdapter(this.mAdapter);
            }
            this.mAdapter.notifyDataSetChanged();
        }

        private void setUpHeader() {
            this.mDayNamesShort = new String[this.mDaysPerWeek];
            this.mDayNamesLong = new String[this.mDaysPerWeek];
            int i = this.mFirstDayOfWeek;
            int count = this.mFirstDayOfWeek + this.mDaysPerWeek;
            while (i < count) {
                int calendarDay = i > DAYS_PER_WEEK ? i - 7 : i;
                this.mDayNamesShort[i - this.mFirstDayOfWeek] = DateUtils.getDayOfWeekString(calendarDay, 50);
                this.mDayNamesLong[i - this.mFirstDayOfWeek] = DateUtils.getDayOfWeekString(calendarDay, 10);
                i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
            }
            TextView label = (TextView) this.mDayNamesHeader.getChildAt(0);
            if (this.mShowWeekNumber) {
                label.setVisibility(0);
            } else {
                label.setVisibility(8);
            }
            count = this.mDayNamesHeader.getChildCount();
            for (i = UNSCALED_WEEK_SEPARATOR_LINE_WIDTH; i < count; i += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                label = (TextView) this.mDayNamesHeader.getChildAt(i);
                if (this.mWeekDayTextAppearanceResId > DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID) {
                    label.setTextAppearance(this.mWeekDayTextAppearanceResId);
                }
                if (i < this.mDaysPerWeek + UNSCALED_WEEK_SEPARATOR_LINE_WIDTH) {
                    label.setText(this.mDayNamesShort[i + DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID]);
                    label.setContentDescription(this.mDayNamesLong[i + DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID]);
                    label.setVisibility(0);
                } else {
                    label.setVisibility(8);
                }
            }
            this.mDayNamesHeader.invalidate();
        }

        private void setUpListView() {
            this.mListView.setDivider(null);
            this.mListView.setItemsCanFocus(DEFAULT_SHOW_WEEK_NUMBER);
            this.mListView.setVerticalScrollBarEnabled(false);
            this.mListView.setOnScrollListener(new OnScrollListener() {
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    CalendarViewLegacyDelegate.this.onScrollStateChanged(view, scrollState);
                }

                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    CalendarViewLegacyDelegate.this.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
            });
            this.mListView.setFriction(this.mFriction);
            this.mListView.setVelocityScale(this.mVelocityScale);
        }

        private void goTo(Calendar date, boolean animate, boolean setSelected, boolean forceScroll) {
            if (date.before(this.mMinDate) || date.after(this.mMaxDate)) {
                throw new IllegalArgumentException("Time not between " + this.mMinDate.getTime() + " and " + this.mMaxDate.getTime());
            }
            int firstFullyVisiblePosition = this.mListView.getFirstVisiblePosition();
            View firstChild = this.mListView.getChildAt(0);
            if (firstChild != null && firstChild.getTop() < 0) {
                firstFullyVisiblePosition += UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
            }
            int lastFullyVisiblePosition = (this.mShownWeekCount + firstFullyVisiblePosition) + DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            if (firstChild != null && firstChild.getTop() > this.mBottomBuffer) {
                lastFullyVisiblePosition += DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
            }
            if (setSelected) {
                this.mAdapter.setSelectedDay(date);
            }
            int position = getWeeksSinceMinDate(date);
            if (position < firstFullyVisiblePosition || position > lastFullyVisiblePosition || forceScroll) {
                this.mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
                this.mFirstDayOfMonth.set(5, UNSCALED_WEEK_SEPARATOR_LINE_WIDTH);
                setMonthDisplayed(this.mFirstDayOfMonth);
                if (this.mFirstDayOfMonth.before(this.mMinDate)) {
                    position = 0;
                } else {
                    position = getWeeksSinceMinDate(this.mFirstDayOfMonth);
                }
                this.mPreviousScrollState = UNSCALED_LIST_SCROLL_TOP_OFFSET;
                if (animate) {
                    this.mListView.smoothScrollToPositionFromTop(position, this.mListScrollTopOffset, GOTO_SCROLL_DURATION);
                    return;
                }
                this.mListView.setSelectionFromTop(position, this.mListScrollTopOffset);
                onScrollStateChanged(this.mListView, 0);
            } else if (setSelected) {
                setMonthDisplayed(date);
            }
        }

        private void onScrollStateChanged(AbsListView view, int scrollState) {
            this.mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
        }

        private void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            WeekView child = (WeekView) view.getChildAt(0);
            if (child != null) {
                long currScroll = (long) ((view.getFirstVisiblePosition() * child.getHeight()) - child.getBottom());
                if (currScroll < this.mPreviousScrollPosition) {
                    this.mIsScrollingUp = DEFAULT_SHOW_WEEK_NUMBER;
                } else if (currScroll > this.mPreviousScrollPosition) {
                    this.mIsScrollingUp = false;
                } else {
                    return;
                }
                int offset = child.getBottom() < this.mWeekMinVisibleHeight ? UNSCALED_WEEK_SEPARATOR_LINE_WIDTH : 0;
                if (this.mIsScrollingUp) {
                    child = (WeekView) view.getChildAt(offset + UNSCALED_LIST_SCROLL_TOP_OFFSET);
                } else if (offset != 0) {
                    child = (WeekView) view.getChildAt(offset);
                }
                if (child != null) {
                    int month;
                    if (this.mIsScrollingUp) {
                        month = child.getMonthOfFirstWeekDay();
                    } else {
                        month = child.getMonthOfLastWeekDay();
                    }
                    int monthDiff;
                    if (this.mCurrentMonthDisplayed == 11 && month == 0) {
                        monthDiff = UNSCALED_WEEK_SEPARATOR_LINE_WIDTH;
                    } else if (this.mCurrentMonthDisplayed == 0 && month == 11) {
                        monthDiff = DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
                    } else {
                        monthDiff = month - this.mCurrentMonthDisplayed;
                    }
                    if (this.mIsScrollingUp || monthDiff <= 0) {
                        if (this.mIsScrollingUp && monthDiff < 0) {
                        }
                    }
                    Calendar firstDay = child.getFirstDay();
                    if (this.mIsScrollingUp) {
                        firstDay.add(5, -7);
                    } else {
                        firstDay.add(5, DAYS_PER_WEEK);
                    }
                    setMonthDisplayed(firstDay);
                }
                this.mPreviousScrollPosition = currScroll;
                this.mPreviousScrollState = this.mCurrentScrollState;
            }
        }

        private void setMonthDisplayed(Calendar calendar) {
            this.mCurrentMonthDisplayed = calendar.get(UNSCALED_LIST_SCROLL_TOP_OFFSET);
            this.mAdapter.setFocusMonth(this.mCurrentMonthDisplayed);
            long millis = calendar.getTimeInMillis();
            this.mMonthName.setText(DateUtils.formatDateRange(this.mContext, millis, millis, 52));
            this.mMonthName.invalidate();
        }

        private int getWeeksSinceMinDate(Calendar date) {
            if (date.before(this.mMinDate)) {
                throw new IllegalArgumentException("fromDate: " + this.mMinDate.getTime() + " does not precede toDate: " + date.getTime());
            }
            return (int) ((((date.getTimeInMillis() + ((long) date.getTimeZone().getOffset(date.getTimeInMillis()))) - (this.mMinDate.getTimeInMillis() + ((long) this.mMinDate.getTimeZone().getOffset(this.mMinDate.getTimeInMillis())))) + (((long) (this.mMinDate.get(DAYS_PER_WEEK) - this.mFirstDayOfWeek)) * MILLIS_IN_DAY)) / MILLIS_IN_WEEK);
        }
    }
