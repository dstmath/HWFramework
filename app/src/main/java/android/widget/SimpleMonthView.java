package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.icu.text.DisplayContext;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.MathUtils;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.android.internal.widget.ExploreByTouchHelper;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;
import java.text.NumberFormat;
import java.util.Locale;
import libcore.icu.LocaleData;

class SimpleMonthView extends View {
    private static final int DAYS_IN_WEEK = 7;
    private static final int DEFAULT_SELECTED_DAY = -1;
    private static final int DEFAULT_WEEK_START = 1;
    private static final int MAX_WEEKS_IN_MONTH = 6;
    private static final String MONTH_YEAR_FORMAT = "MMMMy";
    private static final int SELECTED_HIGHLIGHT_ALPHA = 176;
    private int mActivatedDay;
    private final Calendar mCalendar;
    private int mCellWidth;
    private final NumberFormat mDayFormatter;
    private int mDayHeight;
    private final Paint mDayHighlightPaint;
    private final Paint mDayHighlightSelectorPaint;
    private int mDayOfWeekHeight;
    private final String[] mDayOfWeekLabels;
    private final TextPaint mDayOfWeekPaint;
    private int mDayOfWeekStart;
    private final TextPaint mDayPaint;
    private final Paint mDaySelectorPaint;
    private int mDaySelectorRadius;
    private ColorStateList mDayTextColor;
    private int mDaysInMonth;
    private final int mDesiredCellWidth;
    private final int mDesiredDayHeight;
    private final int mDesiredDayOfWeekHeight;
    private final int mDesiredDaySelectorRadius;
    private final int mDesiredMonthHeight;
    private int mEnabledDayEnd;
    private int mEnabledDayStart;
    private int mHighlightedDay;
    private boolean mIsTouchHighlighted;
    private final Locale mLocale;
    private int mMonth;
    private int mMonthHeight;
    private final TextPaint mMonthPaint;
    private String mMonthYearLabel;
    private OnDayClickListener mOnDayClickListener;
    private int mPaddedHeight;
    private int mPaddedWidth;
    private int mPreviouslyHighlightedDay;
    private int mToday;
    private final MonthViewTouchHelper mTouchHelper;
    private int mWeekStart;
    private int mYear;

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, Calendar calendar);
    }

    private class MonthViewTouchHelper extends ExploreByTouchHelper {
        private static final String DATE_FORMAT = "dd MMMM yyyy";
        private final Calendar mTempCalendar;
        private final Rect mTempRect;

        public MonthViewTouchHelper(View host) {
            super(host);
            this.mTempRect = new Rect();
            this.mTempCalendar = Calendar.getInstance();
        }

        protected int getVirtualViewAt(float x, float y) {
            int day = SimpleMonthView.this.getDayAtLocation((int) (x + 0.5f), (int) (0.5f + y));
            if (day != SimpleMonthView.DEFAULT_SELECTED_DAY) {
                return day;
            }
            return RtlSpacingHelper.UNDEFINED;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int day = SimpleMonthView.DEFAULT_WEEK_START; day <= SimpleMonthView.this.mDaysInMonth; day += SimpleMonthView.DEFAULT_WEEK_START) {
                virtualViewIds.add(day);
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(getDayDescription(virtualViewId));
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            if (SimpleMonthView.this.getBoundsForDay(virtualViewId, this.mTempRect)) {
                node.setText(getDayText(virtualViewId));
                node.setContentDescription(getDayDescription(virtualViewId));
                node.setBoundsInParent(this.mTempRect);
                boolean isDayEnabled = SimpleMonthView.this.isDayEnabled(virtualViewId);
                if (isDayEnabled) {
                    node.addAction(AccessibilityAction.ACTION_CLICK);
                }
                node.setEnabled(isDayEnabled);
                if (virtualViewId == SimpleMonthView.this.mActivatedDay) {
                    node.setChecked(true);
                }
                return;
            }
            this.mTempRect.setEmpty();
            node.setContentDescription("");
            node.setBoundsInParent(this.mTempRect);
            node.setVisibleToUser(false);
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    return SimpleMonthView.this.onDayClicked(virtualViewId);
                default:
                    return false;
            }
        }

        private CharSequence getDayDescription(int id) {
            if (!SimpleMonthView.this.isValidDayOfMonth(id)) {
                return "";
            }
            this.mTempCalendar.set(SimpleMonthView.this.mYear, SimpleMonthView.this.mMonth, id);
            return DateFormat.format(DATE_FORMAT, this.mTempCalendar.getTimeInMillis());
        }

        private CharSequence getDayText(int id) {
            if (SimpleMonthView.this.isValidDayOfMonth(id)) {
                return SimpleMonthView.this.mDayFormatter.format((long) id);
            }
            return null;
        }
    }

    public SimpleMonthView(Context context) {
        this(context, null);
    }

    public SimpleMonthView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.datePickerStyle);
    }

    public SimpleMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SimpleMonthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMonthPaint = new TextPaint();
        this.mDayOfWeekPaint = new TextPaint();
        this.mDayPaint = new TextPaint();
        this.mDaySelectorPaint = new Paint();
        this.mDayHighlightPaint = new Paint();
        this.mDayHighlightSelectorPaint = new Paint();
        this.mDayOfWeekLabels = new String[DAYS_IN_WEEK];
        this.mActivatedDay = DEFAULT_SELECTED_DAY;
        this.mToday = DEFAULT_SELECTED_DAY;
        this.mWeekStart = DEFAULT_WEEK_START;
        this.mEnabledDayStart = DEFAULT_WEEK_START;
        this.mEnabledDayEnd = 31;
        this.mHighlightedDay = DEFAULT_SELECTED_DAY;
        this.mPreviouslyHighlightedDay = DEFAULT_SELECTED_DAY;
        this.mIsTouchHighlighted = false;
        Resources res = context.getResources();
        this.mDesiredMonthHeight = res.getDimensionPixelSize(R.dimen.date_picker_month_height);
        this.mDesiredDayOfWeekHeight = res.getDimensionPixelSize(R.dimen.date_picker_day_of_week_height);
        this.mDesiredDayHeight = res.getDimensionPixelSize(R.dimen.date_picker_day_height);
        this.mDesiredCellWidth = res.getDimensionPixelSize(R.dimen.date_picker_day_width);
        this.mDesiredDaySelectorRadius = res.getDimensionPixelSize(R.dimen.date_picker_day_selector_radius);
        this.mTouchHelper = new MonthViewTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(DEFAULT_WEEK_START);
        this.mLocale = res.getConfiguration().locale;
        this.mCalendar = Calendar.getInstance(this.mLocale);
        this.mDayFormatter = NumberFormat.getIntegerInstance(this.mLocale);
        updateMonthYearLabel();
        updateDayOfWeekLabels();
        initPaints(res);
    }

    private void updateMonthYearLabel() {
        SimpleDateFormat formatter = new SimpleDateFormat(DateFormat.getBestDateTimePattern(this.mLocale, MONTH_YEAR_FORMAT), this.mLocale);
        formatter.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
        this.mMonthYearLabel = formatter.format(this.mCalendar.getTime());
    }

    private void updateDayOfWeekLabels() {
        String[] tinyWeekdayNames = LocaleData.get(this.mLocale).tinyWeekdayNames;
        for (int i = 0; i < DAYS_IN_WEEK; i += DEFAULT_WEEK_START) {
            this.mDayOfWeekLabels[i] = tinyWeekdayNames[(((this.mWeekStart + i) + DEFAULT_SELECTED_DAY) % DAYS_IN_WEEK) + DEFAULT_WEEK_START];
        }
    }

    private ColorStateList applyTextAppearance(Paint p, int resId) {
        TypedArray ta = this.mContext.obtainStyledAttributes(null, R.styleable.TextAppearance, 0, resId);
        String fontFamily = ta.getString(12);
        if (fontFamily != null) {
            p.setTypeface(Typeface.create(fontFamily, 0));
        }
        p.setTextSize((float) ta.getDimensionPixelSize(0, (int) p.getTextSize()));
        ColorStateList textColor = ta.getColorStateList(3);
        if (textColor != null) {
            p.setColor(textColor.getColorForState(ENABLED_STATE_SET, 0));
        }
        ta.recycle();
        return textColor;
    }

    public int getMonthHeight() {
        return this.mMonthHeight;
    }

    public int getCellWidth() {
        return this.mCellWidth;
    }

    public void setMonthTextAppearance(int resId) {
        applyTextAppearance(this.mMonthPaint, resId);
        invalidate();
    }

    public void setDayOfWeekTextAppearance(int resId) {
        applyTextAppearance(this.mDayOfWeekPaint, resId);
        invalidate();
    }

    public void setDayTextAppearance(int resId) {
        ColorStateList textColor = applyTextAppearance(this.mDayPaint, resId);
        if (textColor != null) {
            this.mDayTextColor = textColor;
        }
        invalidate();
    }

    private void initPaints(Resources res) {
        String monthTypeface = res.getString(R.string.date_picker_month_typeface);
        String dayOfWeekTypeface = res.getString(R.string.date_picker_day_of_week_typeface);
        String dayTypeface = res.getString(R.string.date_picker_day_typeface);
        int monthTextSize = res.getDimensionPixelSize(R.dimen.date_picker_month_text_size);
        int dayOfWeekTextSize = res.getDimensionPixelSize(R.dimen.date_picker_day_of_week_text_size);
        int dayTextSize = res.getDimensionPixelSize(R.dimen.date_picker_day_text_size);
        this.mMonthPaint.setAntiAlias(true);
        this.mMonthPaint.setTextSize((float) monthTextSize);
        this.mMonthPaint.setTypeface(Typeface.create(monthTypeface, 0));
        this.mMonthPaint.setTextAlign(Align.CENTER);
        this.mMonthPaint.setStyle(Style.FILL);
        this.mDayOfWeekPaint.setAntiAlias(true);
        this.mDayOfWeekPaint.setTextSize((float) dayOfWeekTextSize);
        this.mDayOfWeekPaint.setTypeface(Typeface.create(dayOfWeekTypeface, 0));
        this.mDayOfWeekPaint.setTextAlign(Align.CENTER);
        this.mDayOfWeekPaint.setStyle(Style.FILL);
        this.mDaySelectorPaint.setAntiAlias(true);
        this.mDaySelectorPaint.setStyle(Style.FILL);
        this.mDayHighlightPaint.setAntiAlias(true);
        this.mDayHighlightPaint.setStyle(Style.FILL);
        this.mDayHighlightSelectorPaint.setAntiAlias(true);
        this.mDayHighlightSelectorPaint.setStyle(Style.FILL);
        this.mDayPaint.setAntiAlias(true);
        this.mDayPaint.setTextSize((float) dayTextSize);
        this.mDayPaint.setTypeface(Typeface.create(dayTypeface, 0));
        this.mDayPaint.setTextAlign(Align.CENTER);
        this.mDayPaint.setStyle(Style.FILL);
    }

    void setMonthTextColor(ColorStateList monthTextColor) {
        this.mMonthPaint.setColor(monthTextColor.getColorForState(ENABLED_STATE_SET, 0));
        invalidate();
    }

    void setDayOfWeekTextColor(ColorStateList dayOfWeekTextColor) {
        this.mDayOfWeekPaint.setColor(dayOfWeekTextColor.getColorForState(ENABLED_STATE_SET, 0));
        invalidate();
    }

    void setDayTextColor(ColorStateList dayTextColor) {
        this.mDayTextColor = dayTextColor;
        invalidate();
    }

    void setDaySelectorColor(ColorStateList dayBackgroundColor) {
        int activatedColor = dayBackgroundColor.getColorForState(StateSet.get(40), 0);
        this.mDaySelectorPaint.setColor(activatedColor);
        this.mDayHighlightSelectorPaint.setColor(activatedColor);
        this.mDayHighlightSelectorPaint.setAlpha(SELECTED_HIGHLIGHT_ALPHA);
        invalidate();
    }

    void setDayHighlightColor(ColorStateList dayHighlightColor) {
        this.mDayHighlightPaint.setColor(dayHighlightColor.getColorForState(StateSet.get(24), 0));
        invalidate();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.mOnDayClickListener = listener;
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        return !this.mTouchHelper.dispatchHoverEvent(event) ? super.dispatchHoverEvent(event) : true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) (event.getX() + 0.5f);
        int y = (int) (event.getY() + 0.5f);
        int action = event.getAction();
        switch (action) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
            case HwCfgFilePolicy.PC /*2*/:
                int touchedItem = getDayAtLocation(x, y);
                this.mIsTouchHighlighted = true;
                if (this.mHighlightedDay != touchedItem) {
                    this.mHighlightedDay = touchedItem;
                    this.mPreviouslyHighlightedDay = touchedItem;
                    invalidate();
                }
                return action != 0 || touchedItem >= 0;
            case DEFAULT_WEEK_START /*1*/:
                onDayClicked(getDayAtLocation(x, y));
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                break;
        }
        this.mHighlightedDay = DEFAULT_SELECTED_DAY;
        this.mIsTouchHighlighted = false;
        invalidate();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean z = false;
        boolean focusChanged = false;
        switch (event.getKeyCode()) {
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                if (event.hasNoModifiers()) {
                    ensureFocusedDay();
                    if (this.mHighlightedDay > DAYS_IN_WEEK) {
                        this.mHighlightedDay -= 7;
                        focusChanged = true;
                        break;
                    }
                }
                break;
            case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                if (event.hasNoModifiers()) {
                    ensureFocusedDay();
                    if (this.mHighlightedDay <= this.mDaysInMonth - 7) {
                        this.mHighlightedDay += DAYS_IN_WEEK;
                        focusChanged = true;
                        break;
                    }
                }
                break;
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                if (event.hasNoModifiers()) {
                    focusChanged = moveOneDay(isLayoutRtl());
                    break;
                }
                break;
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                if (event.hasNoModifiers()) {
                    if (!isLayoutRtl()) {
                        z = true;
                    }
                    focusChanged = moveOneDay(z);
                    break;
                }
                break;
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (this.mHighlightedDay != DEFAULT_SELECTED_DAY) {
                    onDayClicked(this.mHighlightedDay);
                    return true;
                }
                break;
            case StatisticalConstant.TYPE_WIFI_END /*61*/:
                int focusChangeDirection = 0;
                if (event.hasNoModifiers()) {
                    focusChangeDirection = 2;
                } else if (event.hasModifiers(DEFAULT_WEEK_START)) {
                    focusChangeDirection = DEFAULT_WEEK_START;
                }
                if (focusChangeDirection != 0) {
                    ViewParent parent = getParent();
                    View nextFocus = this;
                    do {
                        nextFocus = nextFocus.focusSearch(focusChangeDirection);
                        if (nextFocus == null || nextFocus == this) {
                            if (nextFocus != null) {
                                nextFocus.requestFocus();
                                return true;
                            }
                        }
                    } while (nextFocus.getParent() == parent);
                    if (nextFocus != null) {
                        nextFocus.requestFocus();
                        return true;
                    }
                }
                break;
        }
        if (!focusChanged) {
            return super.onKeyDown(keyCode, event);
        }
        invalidate();
        return true;
    }

    private boolean moveOneDay(boolean positive) {
        ensureFocusedDay();
        if (positive) {
            if (isLastDayOfWeek(this.mHighlightedDay) || this.mHighlightedDay >= this.mDaysInMonth) {
                return false;
            }
            this.mHighlightedDay += DEFAULT_WEEK_START;
            return true;
        } else if (isFirstDayOfWeek(this.mHighlightedDay) || this.mHighlightedDay <= DEFAULT_WEEK_START) {
            return false;
        } else {
            this.mHighlightedDay += DEFAULT_SELECTED_DAY;
            return true;
        }
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        int i = DEFAULT_WEEK_START;
        if (gainFocus) {
            int offset = findDayOffset();
            int day;
            switch (direction) {
                case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                    this.mHighlightedDay = Math.min(this.mDaysInMonth, ((findClosestRow(previouslyFocusedRect) + DEFAULT_WEEK_START) * DAYS_IN_WEEK) - offset);
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                    day = ((findClosestColumn(previouslyFocusedRect) - offset) + (((this.mDaysInMonth + offset) / DAYS_IN_WEEK) * DAYS_IN_WEEK)) + DEFAULT_WEEK_START;
                    if (day > this.mDaysInMonth) {
                        day -= 7;
                    }
                    this.mHighlightedDay = day;
                    break;
                case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                    int row = findClosestRow(previouslyFocusedRect);
                    if (row != 0) {
                        i = ((row * DAYS_IN_WEEK) - offset) + DEFAULT_WEEK_START;
                    }
                    this.mHighlightedDay = i;
                    break;
                case LogPower.END_CHG_ROTATION /*130*/:
                    day = (findClosestColumn(previouslyFocusedRect) - offset) + DEFAULT_WEEK_START;
                    if (day < DEFAULT_WEEK_START) {
                        day += DAYS_IN_WEEK;
                    }
                    this.mHighlightedDay = day;
                    break;
            }
            ensureFocusedDay();
            invalidate();
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    private int findClosestRow(Rect previouslyFocusedRect) {
        if (previouslyFocusedRect == null) {
            return 3;
        }
        int i;
        int centerY = previouslyFocusedRect.centerY();
        TextPaint p = this.mDayPaint;
        int headerHeight = this.mMonthHeight + this.mDayOfWeekHeight;
        int rowHeight = this.mDayHeight;
        int row = Math.round(((float) ((int) (((float) centerY) - (((float) (headerHeight + (rowHeight / 2))) - ((p.ascent() + p.descent()) / 2.0f))))) / ((float) rowHeight));
        int maxDay = findDayOffset() + this.mDaysInMonth;
        int i2 = maxDay / DAYS_IN_WEEK;
        if (maxDay % DAYS_IN_WEEK == 0) {
            i = DEFAULT_WEEK_START;
        } else {
            i = 0;
        }
        return MathUtils.constrain(row, 0, i2 - i);
    }

    private int findClosestColumn(Rect previouslyFocusedRect) {
        if (previouslyFocusedRect == null) {
            return 3;
        }
        int columnFromLeft = MathUtils.constrain((previouslyFocusedRect.centerX() - this.mPaddingLeft) / this.mCellWidth, 0, (int) MAX_WEEKS_IN_MONTH);
        if (isLayoutRtl()) {
            columnFromLeft = (7 - columnFromLeft) + DEFAULT_SELECTED_DAY;
        }
        return columnFromLeft;
    }

    public void getFocusedRect(Rect r) {
        if (this.mHighlightedDay > 0) {
            getBoundsForDay(this.mHighlightedDay, r);
        } else {
            super.getFocusedRect(r);
        }
    }

    protected void onFocusLost() {
        if (!this.mIsTouchHighlighted) {
            this.mPreviouslyHighlightedDay = this.mHighlightedDay;
            this.mHighlightedDay = DEFAULT_SELECTED_DAY;
            invalidate();
        }
        super.onFocusLost();
    }

    private void ensureFocusedDay() {
        if (this.mHighlightedDay == DEFAULT_SELECTED_DAY) {
            if (this.mPreviouslyHighlightedDay != DEFAULT_SELECTED_DAY) {
                this.mHighlightedDay = this.mPreviouslyHighlightedDay;
            } else if (this.mActivatedDay != DEFAULT_SELECTED_DAY) {
                this.mHighlightedDay = this.mActivatedDay;
            } else {
                this.mHighlightedDay = DEFAULT_WEEK_START;
            }
        }
    }

    private boolean isFirstDayOfWeek(int day) {
        if (((findDayOffset() + day) + DEFAULT_SELECTED_DAY) % DAYS_IN_WEEK == 0) {
            return true;
        }
        return false;
    }

    private boolean isLastDayOfWeek(int day) {
        if ((findDayOffset() + day) % DAYS_IN_WEEK == 0) {
            return true;
        }
        return false;
    }

    protected void onDraw(Canvas canvas) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        canvas.translate((float) paddingLeft, (float) paddingTop);
        drawMonth(canvas);
        drawDaysOfWeek(canvas);
        drawDays(canvas);
        canvas.translate((float) (-paddingLeft), (float) (-paddingTop));
    }

    private void drawMonth(Canvas canvas) {
        canvas.drawText(this.mMonthYearLabel, ((float) this.mPaddedWidth) / 2.0f, (((float) this.mMonthHeight) - (this.mMonthPaint.ascent() + this.mMonthPaint.descent())) / 2.0f, this.mMonthPaint);
    }

    public String getMonthYearLabel() {
        return this.mMonthYearLabel;
    }

    private void drawDaysOfWeek(Canvas canvas) {
        TextPaint p = this.mDayOfWeekPaint;
        int headerHeight = this.mMonthHeight;
        int rowHeight = this.mDayOfWeekHeight;
        int colWidth = this.mCellWidth;
        float halfLineHeight = (p.ascent() + p.descent()) / 2.0f;
        int rowCenter = headerHeight + (rowHeight / 2);
        for (int col = 0; col < DAYS_IN_WEEK; col += DEFAULT_WEEK_START) {
            int colCenterRtl;
            int colCenter = (colWidth * col) + (colWidth / 2);
            if (isLayoutRtl()) {
                colCenterRtl = this.mPaddedWidth - colCenter;
            } else {
                colCenterRtl = colCenter;
            }
            canvas.drawText(this.mDayOfWeekLabels[col], (float) colCenterRtl, ((float) rowCenter) - halfLineHeight, p);
        }
    }

    private void drawDays(Canvas canvas) {
        Paint p = this.mDayPaint;
        int headerHeight = this.mMonthHeight + this.mDayOfWeekHeight;
        int rowHeight = this.mDayHeight;
        int colWidth = this.mCellWidth;
        float halfLineHeight = (p.ascent() + p.descent()) / 2.0f;
        int rowCenter = headerHeight + (rowHeight / 2);
        int day = DEFAULT_WEEK_START;
        int col = findDayOffset();
        while (true) {
            int i = this.mDaysInMonth;
            if (day <= r0) {
                int colCenterRtl;
                int dayTextColor;
                int colCenter = (colWidth * col) + (colWidth / 2);
                if (isLayoutRtl()) {
                    colCenterRtl = this.mPaddedWidth - colCenter;
                } else {
                    colCenterRtl = colCenter;
                }
                int stateMask = 0;
                boolean isDayEnabled = isDayEnabled(day);
                if (isDayEnabled) {
                    stateMask = 8;
                }
                i = this.mActivatedDay;
                boolean isDayActivated = r0 == day;
                i = this.mHighlightedDay;
                boolean isDayHighlighted = r0 == day;
                if (isDayActivated) {
                    Paint paint;
                    stateMask |= 32;
                    if (isDayHighlighted) {
                        paint = this.mDayHighlightSelectorPaint;
                    } else {
                        paint = this.mDaySelectorPaint;
                    }
                    canvas.drawCircle((float) colCenterRtl, (float) rowCenter, (float) this.mDaySelectorRadius, paint);
                } else if (isDayHighlighted) {
                    stateMask |= 16;
                    if (isDayEnabled) {
                        canvas.drawCircle((float) colCenterRtl, (float) rowCenter, (float) this.mDaySelectorRadius, this.mDayHighlightPaint);
                    }
                }
                i = this.mToday;
                if (!(r0 == day) || isDayActivated) {
                    dayTextColor = this.mDayTextColor.getColorForState(StateSet.get(stateMask), 0);
                } else {
                    dayTextColor = this.mDaySelectorPaint.getColor();
                }
                p.setColor(dayTextColor);
                canvas.drawText(this.mDayFormatter.format((long) day), (float) colCenterRtl, ((float) rowCenter) - halfLineHeight, p);
                col += DEFAULT_WEEK_START;
                if (col == DAYS_IN_WEEK) {
                    col = 0;
                    rowCenter += rowHeight;
                }
                day += DEFAULT_WEEK_START;
            } else {
                return;
            }
        }
    }

    private boolean isDayEnabled(int day) {
        return day >= this.mEnabledDayStart && day <= this.mEnabledDayEnd;
    }

    private boolean isValidDayOfMonth(int day) {
        return day >= DEFAULT_WEEK_START && day <= this.mDaysInMonth;
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= DEFAULT_WEEK_START && day <= DAYS_IN_WEEK;
    }

    private static boolean isValidMonth(int month) {
        return month >= 0 && month <= 11;
    }

    public void setSelectedDay(int dayOfMonth) {
        this.mActivatedDay = dayOfMonth;
        this.mTouchHelper.invalidateRoot();
        invalidate();
    }

    public void setFirstDayOfWeek(int weekStart) {
        if (isValidDayOfWeek(weekStart)) {
            this.mWeekStart = weekStart;
        } else {
            this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
        }
        updateDayOfWeekLabels();
        this.mTouchHelper.invalidateRoot();
        invalidate();
    }

    void setMonthParams(int selectedDay, int month, int year, int weekStart, int enabledDayStart, int enabledDayEnd) {
        this.mActivatedDay = selectedDay;
        if (isValidMonth(month)) {
            this.mMonth = month;
        }
        this.mYear = year;
        this.mCalendar.set(2, this.mMonth);
        this.mCalendar.set(DEFAULT_WEEK_START, this.mYear);
        this.mCalendar.set(5, DEFAULT_WEEK_START);
        this.mDayOfWeekStart = this.mCalendar.get(DAYS_IN_WEEK);
        if (isValidDayOfWeek(weekStart)) {
            this.mWeekStart = weekStart;
        } else {
            this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
        }
        Calendar today = Calendar.getInstance();
        this.mToday = DEFAULT_SELECTED_DAY;
        this.mDaysInMonth = getDaysInMonth(this.mMonth, this.mYear);
        for (int i = 0; i < this.mDaysInMonth; i += DEFAULT_WEEK_START) {
            int day = i + DEFAULT_WEEK_START;
            if (sameDay(day, today)) {
                this.mToday = day;
            }
        }
        this.mEnabledDayStart = MathUtils.constrain(enabledDayStart, (int) DEFAULT_WEEK_START, this.mDaysInMonth);
        this.mEnabledDayEnd = MathUtils.constrain(enabledDayEnd, this.mEnabledDayStart, this.mDaysInMonth);
        updateMonthYearLabel();
        updateDayOfWeekLabels();
        this.mTouchHelper.invalidateRoot();
        invalidate();
    }

    private static int getDaysInMonth(int month, int year) {
        switch (month) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
            case HwCfgFilePolicy.PC /*2*/:
            case HwCfgFilePolicy.CUST /*4*/:
            case MAX_WEEKS_IN_MONTH /*6*/:
            case DAYS_IN_WEEK /*7*/:
            case PGSdk.TYPE_SCRLOCK /*9*/:
            case PGSdk.TYPE_IM /*11*/:
                return 31;
            case DEFAULT_WEEK_START /*1*/:
                return year % 4 == 0 ? 29 : 28;
            case HwCfgFilePolicy.BASE /*3*/:
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
            case PGSdk.TYPE_VIDEO /*8*/:
            case PGSdk.TYPE_CLOCK /*10*/:
                return 30;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    private boolean sameDay(int day, Calendar today) {
        if (this.mYear == today.get(DEFAULT_WEEK_START) && this.mMonth == today.get(2)) {
            return day == today.get(5);
        } else {
            return false;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.resolveSize(((this.mDesiredCellWidth * DAYS_IN_WEEK) + getPaddingStart()) + getPaddingEnd(), widthMeasureSpec), View.resolveSize(((((this.mDesiredDayHeight * MAX_WEEKS_IN_MONTH) + this.mDesiredDayOfWeekHeight) + this.mDesiredMonthHeight) + getPaddingTop()) + getPaddingBottom(), heightMeasureSpec));
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        requestLayout();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            int w = right - left;
            int h = bottom - top;
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            int paddedWidth = (w - paddingRight) - paddingLeft;
            int paddedHeight = (h - paddingBottom) - paddingTop;
            int i = this.mPaddedWidth;
            if (paddedWidth != r0) {
                i = this.mPaddedHeight;
                if (paddedHeight != r0) {
                    this.mPaddedWidth = paddedWidth;
                    this.mPaddedHeight = paddedHeight;
                    float scaleH = ((float) paddedHeight) / ((float) ((getMeasuredHeight() - paddingTop) - paddingBottom));
                    int monthHeight = (int) (((float) this.mDesiredMonthHeight) * scaleH);
                    int cellWidth = this.mPaddedWidth / DAYS_IN_WEEK;
                    this.mMonthHeight = monthHeight;
                    this.mDayOfWeekHeight = (int) (((float) this.mDesiredDayOfWeekHeight) * scaleH);
                    this.mDayHeight = (int) (((float) this.mDesiredDayHeight) * scaleH);
                    this.mCellWidth = cellWidth;
                    int maxSelectorWidth = (cellWidth / 2) + Math.min(paddingLeft, paddingRight);
                    int maxSelectorHeight = (this.mDayHeight / 2) + paddingBottom;
                    this.mDaySelectorRadius = Math.min(this.mDesiredDaySelectorRadius, Math.min(maxSelectorWidth, maxSelectorHeight));
                    this.mTouchHelper.invalidateRoot();
                }
            }
        }
    }

    private int findDayOffset() {
        int offset = this.mDayOfWeekStart - this.mWeekStart;
        if (this.mDayOfWeekStart < this.mWeekStart) {
            return offset + DAYS_IN_WEEK;
        }
        return offset;
    }

    private int getDayAtLocation(int x, int y) {
        int paddedX = x - getPaddingLeft();
        if (paddedX < 0 || paddedX >= this.mPaddedWidth) {
            return DEFAULT_SELECTED_DAY;
        }
        int headerHeight = this.mMonthHeight + this.mDayOfWeekHeight;
        int paddedY = y - getPaddingTop();
        if (paddedY < headerHeight || paddedY >= this.mPaddedHeight) {
            return DEFAULT_SELECTED_DAY;
        }
        int paddedXRtl;
        if (isLayoutRtl()) {
            paddedXRtl = this.mPaddedWidth - paddedX;
        } else {
            paddedXRtl = paddedX;
        }
        int day = ((((paddedXRtl * DAYS_IN_WEEK) / this.mPaddedWidth) + (((paddedY - headerHeight) / this.mDayHeight) * DAYS_IN_WEEK)) + DEFAULT_WEEK_START) - findDayOffset();
        if (isValidDayOfMonth(day)) {
            return day;
        }
        return DEFAULT_SELECTED_DAY;
    }

    private boolean getBoundsForDay(int id, Rect outBounds) {
        if (!isValidDayOfMonth(id)) {
            return false;
        }
        int left;
        int index = (id + DEFAULT_SELECTED_DAY) + findDayOffset();
        int col = index % DAYS_IN_WEEK;
        int colWidth = this.mCellWidth;
        if (isLayoutRtl()) {
            left = (getWidth() - getPaddingRight()) - ((col + DEFAULT_WEEK_START) * colWidth);
        } else {
            left = getPaddingLeft() + (col * colWidth);
        }
        int row = index / DAYS_IN_WEEK;
        int rowHeight = this.mDayHeight;
        int top = (getPaddingTop() + (this.mMonthHeight + this.mDayOfWeekHeight)) + (row * rowHeight);
        outBounds.set(left, top, left + colWidth, top + rowHeight);
        return true;
    }

    private boolean onDayClicked(int day) {
        if (!isValidDayOfMonth(day) || !isDayEnabled(day)) {
            return false;
        }
        if (this.mOnDayClickListener != null) {
            Calendar date = Calendar.getInstance();
            date.set(this.mYear, this.mMonth, day);
            this.mOnDayClickListener.onDayClick(this, date);
        }
        this.mTouchHelper.sendEventForVirtualView(day, DEFAULT_WEEK_START);
        return true;
    }
}
