package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import libcore.icu.LocaleData;

public class NumberPicker extends LinearLayout {
    private static final int DEFAULT_LAYOUT_RESOURCE_ID = 17367219;
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;
    private static final char[] DIGIT_CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 1632, 1633, 1634, 1635, 1636, 1637, 1638, 1639, 1640, 1641, 1776, 1777, 1778, 1779, 1780, 1781, 1782, 1783, 1784, 1785, 2406, 2407, 2408, 2409, 2410, 2411, 2412, 2413, 2414, 2415, 2534, 2535, 2536, 2537, 2538, 2539, 2540, 2541, 2542, 2543, 3302, 3303, 3304, 3305, 3306, 3307, 3308, 3309, 3310, 3311};
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 4;
    @UnsupportedAppUsage
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 3;
    private static final int SIZE_UNSPECIFIED = -1;
    private static final int SNAP_SCROLL_DURATION = 300;
    private static final String TAG = "NumberPicker";
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
    private static final int UNITS_NUM = 1000;
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48;
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;
    private static final int VELOCITY_INFO_SIZE = 4;
    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();
    @UnsupportedAppUsage
    private int SELECTOR_MIDDLE_ITEM_INDEX;
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    private final Scroller mAdjustScroller;
    private BeginSoftInputOnLongPressCommand mBeginSoftInputOnLongPressCommand;
    private int mBottomSelectionDividerBottom;
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    private final boolean mComputeMaxWidth;
    private int mCurrentScrollOffset;
    private final ImageButton mDecrementButton;
    private boolean mDecrementVirtualButtonPressed;
    private String[] mDisplayedValues;
    @UnsupportedAppUsage
    private final Scroller mFlingScroller;
    private int mFlingState;
    private Formatter mFormatter;
    private final boolean mHasSelectorWheel;
    private boolean mHideWheelUntilFocused;
    private boolean mIgnoreMoveEvents;
    private final ImageButton mIncrementButton;
    private boolean mIncrementVirtualButtonPressed;
    private int mInitialScrollOffset;
    @UnsupportedAppUsage
    private final EditText mInputText;
    private long mLastDownEventTime;
    private float mLastDownEventY;
    private float mLastDownOrMoveEventY;
    private int mLastHandledDownDpadKeyCode;
    private int mLastHoveredChildVirtualViewId;
    private long mLongPressUpdateInterval;
    private final int mMaxHeight;
    @UnsupportedAppUsage
    private int mMaxValue;
    private int mMaxWidth;
    @UnsupportedAppUsage
    private int mMaximumFlingVelocity;
    @UnsupportedAppUsage
    private final int mMinHeight;
    private int mMinValue;
    @UnsupportedAppUsage
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private OnScrollListener mOnScrollListener;
    @UnsupportedAppUsage
    private OnValueChangeListener mOnValueChangeListener;
    private boolean mPerformClickOnTap;
    private final PressedStateHelper mPressedStateHelper;
    private int mPreviousScrollerY;
    private int mRealTimeFlingVelocity;
    private int mRealTimeVelocity;
    private int mScrollState;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private final Drawable mSelectionDivider;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mSelectionDividerHeight;
    private final int mSelectionDividersDistance;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    @UnsupportedAppUsage
    private int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    protected int mSelectorWheelItemCount;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private final Paint mSelectorWheelPaint;
    private SetSelectionCommand mSetSelectionCommand;
    private final int mSolidColor;
    @UnsupportedAppUsage
    private final int mTextSize;
    private int mTopSelectionDividerTop;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private final Drawable mVirtualButtonPressedDrawable;
    private boolean mWrapSelectorWheel;
    private boolean mWrapSelectorWheelPreferred;

    public interface Formatter {
        String format(int i);
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollState {
        }

        void onScrollStateChange(NumberPicker numberPicker, int i);
    }

    public interface OnValueChangeListener {
        void onValueChange(NumberPicker numberPicker, int i, int i2);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [byte, boolean] */
    /* JADX WARNING: Unknown variable types count: 1 */
    static /* synthetic */ boolean access$1280(NumberPicker x0, int x1) {
        ?? r0 = (byte) ((x0.mIncrementVirtualButtonPressed ? 1 : 0) ^ x1);
        x0.mIncrementVirtualButtonPressed = r0;
        return r0;
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [byte, boolean] */
    /* JADX WARNING: Unknown variable types count: 1 */
    static /* synthetic */ boolean access$1680(NumberPicker x0, int x1) {
        ?? r0 = (byte) ((x0.mDecrementVirtualButtonPressed ? 1 : 0) ^ x1);
        x0.mDecrementVirtualButtonPressed = r0;
        return r0;
    }

    /* access modifiers changed from: private */
    public static class TwoDigitFormatter implements Formatter {
        final Object[] mArgs = new Object[1];
        final StringBuilder mBuilder = new StringBuilder();
        java.util.Formatter mFmt;
        char mZeroDigit;

        TwoDigitFormatter() {
            init(Locale.getDefault());
        }

        private void init(Locale locale) {
            this.mFmt = createFormatter(locale);
            this.mZeroDigit = getZeroDigit(locale);
        }

        @Override // android.widget.NumberPicker.Formatter
        public String format(int value) {
            Locale currentLocale = Locale.getDefault();
            if (this.mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale);
            }
            this.mArgs[0] = Integer.valueOf(value);
            StringBuilder sb = this.mBuilder;
            sb.delete(0, sb.length());
            this.mFmt.format("%02d", this.mArgs);
            return this.mFmt.toString();
        }

        private static char getZeroDigit(Locale locale) {
            return LocaleData.get(locale).zeroDigit;
        }

        private java.util.Formatter createFormatter(Locale locale) {
            return new java.util.Formatter(this.mBuilder, locale);
        }
    }

    @UnsupportedAppUsage
    public static final Formatter getTwoDigitFormatter() {
        return sTwoDigitFormatter;
    }

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 16844068);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        boolean z;
        int i;
        int i2;
        this.mSelectorWheelItemCount = 3;
        this.SELECTOR_MIDDLE_ITEM_INDEX = this.mSelectorWheelItemCount / 2;
        this.mWrapSelectorWheelPreferred = true;
        this.mLongPressUpdateInterval = 300;
        this.mSelectorIndexToStringCache = new SparseArray<>();
        this.mSelectorIndices = new int[this.mSelectorWheelItemCount];
        this.mInitialScrollOffset = Integer.MIN_VALUE;
        this.mScrollState = 0;
        this.mLastHandledDownDpadKeyCode = -1;
        this.mFlingState = 0;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.NumberPicker, attrs, attributesArray, defStyleAttr, defStyleRes);
        int layoutResId = attributesArray.getResourceId(3, 17367219);
        this.mHasSelectorWheel = layoutResId != 17367219;
        this.mHideWheelUntilFocused = attributesArray.getBoolean(2, false);
        this.mSolidColor = attributesArray.getColor(0, 0);
        Drawable selectionDivider = attributesArray.getDrawable(8);
        if (selectionDivider != null) {
            selectionDivider.setCallback(this);
            selectionDivider.setLayoutDirection(getLayoutDirection());
            if (selectionDivider.isStateful()) {
                selectionDivider.setState(getDrawableState());
            }
        }
        this.mSelectionDivider = selectionDivider;
        this.mSelectionDividerHeight = attributesArray.getDimensionPixelSize(1, (int) TypedValue.applyDimension(1, 2.0f, getResources().getDisplayMetrics()));
        this.mSelectionDividersDistance = attributesArray.getDimensionPixelSize(9, (int) TypedValue.applyDimension(1, 48.0f, getResources().getDisplayMetrics()));
        this.mMinHeight = attributesArray.getDimensionPixelSize(6, -1);
        this.mMaxHeight = attributesArray.getDimensionPixelSize(4, -1);
        int i3 = this.mMinHeight;
        if (i3 == -1 || (i2 = this.mMaxHeight) == -1 || i3 <= i2) {
            this.mMinWidth = attributesArray.getDimensionPixelSize(7, -1);
            this.mMaxWidth = attributesArray.getDimensionPixelSize(5, -1);
            int i4 = this.mMinWidth;
            if (i4 == -1 || (i = this.mMaxWidth) == -1 || i4 <= i) {
                this.mComputeMaxWidth = this.mMaxWidth == -1;
                this.mVirtualButtonPressedDrawable = attributesArray.getDrawable(10);
                attributesArray.recycle();
                this.mPressedStateHelper = new PressedStateHelper();
                setWillNotDraw(!this.mHasSelectorWheel);
                ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, (ViewGroup) this, true);
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    /* class android.widget.NumberPicker.AnonymousClass1 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        NumberPicker.this.hideSoftInput();
                        NumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == 16909051) {
                            NumberPicker.this.changeValueByOne(true);
                        } else {
                            NumberPicker.this.changeValueByOne(false);
                        }
                    }
                };
                View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                    /* class android.widget.NumberPicker.AnonymousClass2 */

                    @Override // android.view.View.OnLongClickListener
                    public boolean onLongClick(View v) {
                        NumberPicker.this.hideSoftInput();
                        NumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == 16909051) {
                            NumberPicker.this.postChangeCurrentByOneFromLongPress(true, 0);
                        } else {
                            NumberPicker.this.postChangeCurrentByOneFromLongPress(false, 0);
                        }
                        return true;
                    }
                };
                if (!this.mHasSelectorWheel) {
                    this.mIncrementButton = (ImageButton) findViewById(R.id.increment);
                    this.mIncrementButton.setOnClickListener(onClickListener);
                    this.mIncrementButton.setOnLongClickListener(onLongClickListener);
                } else {
                    this.mIncrementButton = null;
                }
                if (!this.mHasSelectorWheel) {
                    this.mDecrementButton = (ImageButton) findViewById(R.id.decrement);
                    this.mDecrementButton.setOnClickListener(onClickListener);
                    this.mDecrementButton.setOnLongClickListener(onLongClickListener);
                } else {
                    this.mDecrementButton = null;
                }
                this.mInputText = (EditText) findViewById(R.id.numberpicker_input);
                this.mInputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    /* class android.widget.NumberPicker.AnonymousClass3 */

                    @Override // android.view.View.OnFocusChangeListener
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            NumberPicker.this.mInputText.selectAll();
                            return;
                        }
                        NumberPicker.this.mInputText.setSelection(0, 0);
                        NumberPicker.this.validateInputTextView(v);
                    }
                });
                this.mInputText.setFilters(new InputFilter[]{new InputTextFilter()});
                this.mInputText.setAccessibilityLiveRegion(1);
                this.mInputText.setRawInputType(2);
                this.mInputText.setImeOptions(6);
                ViewConfiguration configuration = ViewConfiguration.get(context);
                this.mTouchSlop = configuration.getScaledTouchSlop();
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 4;
                this.mTextSize = (int) this.mInputText.getTextSize();
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize((float) this.mTextSize);
                paint.setTypeface(this.mInputText.getTypeface());
                paint.setColor(this.mInputText.getTextColors().getColorForState(ENABLED_STATE_SET, -1));
                this.mSelectorWheelPaint = paint;
                this.mFlingScroller = new Scroller(getContext(), null, true);
                this.mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
                updateInputTextView();
                if (getImportantForAccessibility() == 0) {
                    z = true;
                    setImportantForAccessibility(1);
                } else {
                    z = true;
                }
                if (getFocusable() == 16) {
                    int i5 = z ? 1 : 0;
                    int i6 = z ? 1 : 0;
                    int i7 = z ? 1 : 0;
                    setFocusable(i5);
                    setFocusableInTouchMode(z);
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        throw new IllegalArgumentException("minHeight > maxHeight");
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!this.mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        int msrdWdth = getMeasuredWidth();
        int msrdHght = getMeasuredHeight();
        int inptTxtMsrdWdth = this.mInputText.getMeasuredWidth();
        int inptTxtMsrdHght = this.mInputText.getMeasuredHeight();
        int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        this.mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtLeft + inptTxtMsrdWdth, inptTxtTop + inptTxtMsrdHght);
        if (changed) {
            initializeSelectorWheel();
            initializeFadingEdgesEx();
            int height = getHeight();
            int i = this.mSelectionDividersDistance;
            int i2 = this.mSelectionDividerHeight;
            this.mTopSelectionDividerTop = ((height - i) / 2) - i2;
            this.mBottomSelectionDividerBottom = this.mTopSelectionDividerTop + (i2 * 2) + i;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!this.mHasSelectorWheel) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(makeMeasureSpec(widthMeasureSpec, this.mMaxWidth), makeMeasureSpec(heightMeasureSpec, this.mMaxHeight));
        setMeasuredDimension(resolveSizeAndStateRespectingMinSize(this.mMinWidth, getMeasuredWidth(), widthMeasureSpec), resolveSizeAndStateRespectingMinSize(this.mMinHeight, getMeasuredHeight(), heightMeasureSpec));
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int overshootAdjustment = this.mInitialScrollOffset - ((this.mCurrentScrollOffset + amountToScroll) % this.mSelectorElementHeight);
        if (overshootAdjustment == 0) {
            return false;
        }
        int abs = Math.abs(overshootAdjustment);
        int i = this.mSelectorElementHeight;
        if (abs > i / 2) {
            if (overshootAdjustment > 0) {
                overshootAdjustment -= i;
            } else {
                overshootAdjustment += i;
            }
        }
        scrollBy(0, amountToScroll + overshootAdjustment);
        return true;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!this.mHasSelectorWheel || !isEnabled() || event.getActionMasked() != 0) {
            return false;
        }
        removeAllCallbacks();
        hideSoftInput();
        float y = event.getY();
        this.mLastDownEventY = y;
        this.mLastDownOrMoveEventY = y;
        this.mLastDownEventTime = event.getEventTime();
        this.mIgnoreMoveEvents = false;
        this.mPerformClickOnTap = false;
        float f = this.mLastDownEventY;
        if (f < ((float) this.mTopSelectionDividerTop)) {
            if (this.mScrollState == 0) {
                this.mPressedStateHelper.buttonPressDelayed(2);
            }
        } else if (f > ((float) this.mBottomSelectionDividerBottom) && this.mScrollState == 0) {
            this.mPressedStateHelper.buttonPressDelayed(1);
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        if (!this.mFlingScroller.isFinished()) {
            this.mFlingScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
            onScrollStateChange(0);
        } else if (!this.mAdjustScroller.isFinished()) {
            this.mFlingScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
        } else {
            float f2 = this.mLastDownEventY;
            if (f2 < ((float) this.mTopSelectionDividerTop)) {
                postChangeCurrentByOneFromLongPress(false, (long) ViewConfiguration.getLongPressTimeout());
            } else if (f2 > ((float) this.mBottomSelectionDividerBottom)) {
                postChangeCurrentByOneFromLongPress(true, (long) ViewConfiguration.getLongPressTimeout());
            } else {
                this.mPerformClickOnTap = true;
                postBeginSoftInputOnLongPressCommand();
            }
        }
        return true;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !this.mHasSelectorWheel) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
        this.mRealTimeVelocity = (int) this.mVelocityTracker.getYVelocity();
        this.mFlingState = 0;
        int action = event.getActionMasked();
        if (action == 1) {
            removeBeginSoftInputCommand();
            removeChangeCurrentByOneFromLongPress();
            this.mPressedStateHelper.cancel();
            VelocityTracker velocityTracker = this.mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
            int initialVelocity = (int) velocityTracker.getYVelocity();
            if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
                fling(initialVelocity);
                onScrollStateChange(2);
            } else {
                int eventY = (int) event.getY();
                int deltaMoveY = (int) Math.abs(((float) eventY) - this.mLastDownEventY);
                long deltaTime = event.getEventTime() - this.mLastDownEventTime;
                if (deltaMoveY > this.mTouchSlop || deltaTime >= ((long) ViewConfiguration.getTapTimeout())) {
                    ensureScrollWheelAdjusted();
                } else if (this.mPerformClickOnTap) {
                    this.mPerformClickOnTap = false;
                    performClick();
                } else {
                    int selectorIndexOffset = (eventY / this.mSelectorElementHeight) - this.SELECTOR_MIDDLE_ITEM_INDEX;
                    if (selectorIndexOffset > 0) {
                        changeValueByOne(true);
                        this.mPressedStateHelper.buttonTapped(1);
                    } else if (selectorIndexOffset < 0) {
                        changeValueByOne(false);
                        this.mPressedStateHelper.buttonTapped(2);
                    }
                }
                onScrollStateChange(0);
            }
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        } else if (action == 2 && !this.mIgnoreMoveEvents) {
            float currentMoveY = event.getY();
            if (this.mScrollState == 1) {
                scrollBy(0, (int) (currentMoveY - this.mLastDownOrMoveEventY));
                invalidate();
            } else if (((int) Math.abs(currentMoveY - this.mLastDownEventY)) > this.mTouchSlop) {
                removeAllCallbacks();
                onScrollStateChange(1);
            }
            this.mLastDownOrMoveEventY = currentMoveY;
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 1 || action == 3) {
            removeAllCallbacks();
        }
        return super.dispatchTouchEvent(event);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 19 || keyCode == 20) {
            if (this.mHasSelectorWheel) {
                int action = event.getAction();
                if (action == 0) {
                    if (!this.mWrapSelectorWheel) {
                        if (keyCode == 20) {
                        }
                    }
                    requestFocus();
                    this.mLastHandledDownDpadKeyCode = keyCode;
                    removeAllCallbacks();
                    if (this.mFlingScroller.isFinished()) {
                        changeValueByOne(keyCode == 20);
                    }
                    return true;
                } else if (action == 1 && this.mLastHandledDownDpadKeyCode == keyCode) {
                    this.mLastHandledDownDpadKeyCode = -1;
                    return true;
                }
            }
        } else if (keyCode == 23 || keyCode == 66) {
            removeAllCallbacks();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTrackballEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 1 || action == 3) {
            removeAllCallbacks();
        }
        return super.dispatchTrackballEvent(event);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchHoverEvent(MotionEvent event) {
        int hoveredVirtualViewId;
        if (!this.mHasSelectorWheel) {
            return super.dispatchHoverEvent(event);
        }
        if (!AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            return false;
        }
        int eventY = (int) event.getY();
        if (eventY < this.mTopSelectionDividerTop) {
            hoveredVirtualViewId = 3;
        } else if (eventY > this.mBottomSelectionDividerBottom) {
            hoveredVirtualViewId = 1;
        } else {
            hoveredVirtualViewId = 2;
        }
        int action = event.getActionMasked();
        AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
        if (action == 7) {
            int i = this.mLastHoveredChildVirtualViewId;
            if (i == hoveredVirtualViewId || i == -1) {
                return false;
            }
            provider.sendAccessibilityEventForVirtualView(i, 256);
            provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 128);
            this.mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
            provider.performAction(hoveredVirtualViewId, 64, null);
            return false;
        } else if (action == 9) {
            provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 128);
            this.mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
            provider.performAction(hoveredVirtualViewId, 64, null);
            return false;
        } else if (action != 10) {
            return false;
        } else {
            provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 256);
            this.mLastHoveredChildVirtualViewId = -1;
            return false;
        }
    }

    @Override // android.view.View
    public void computeScroll() {
        Scroller scroller = this.mFlingScroller;
        if (scroller.isFinished()) {
            scroller = this.mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        this.mRealTimeFlingVelocity = (int) scroller.getCurrVelocity();
        int currentScrollerY = scroller.getCurrY();
        if (this.mPreviousScrollerY == 0) {
            this.mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - this.mPreviousScrollerY);
        this.mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!this.mHasSelectorWheel) {
            this.mIncrementButton.setEnabled(enabled);
        }
        if (!this.mHasSelectorWheel) {
            this.mDecrementButton.setEnabled(enabled);
        }
        this.mInputText.setEnabled(enabled);
    }

    @Override // android.view.View
    public void scrollBy(int x, int y) {
        int i;
        int[] selectorIndices = this.mSelectorIndices;
        int startScrollOffset = this.mCurrentScrollOffset;
        if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] <= this.mMinValue) {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
        } else if (this.mWrapSelectorWheel || y >= 0 || selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] < this.mMaxValue) {
            String[] strArr = this.mDisplayedValues;
            int maxCount = strArr != null ? strArr.length + 1 : (this.mMaxValue - this.mMinValue) + 2;
            int maxScrollY = this.mSelectorElementHeight * maxCount;
            if (maxScrollY > 0 && (y > maxScrollY || y < (-maxScrollY))) {
                Log.w(TAG, "the y is abnormal, y:" + y + ", maxCount:" + maxCount + ", maxScrollY:" + maxScrollY);
                int i2 = this.mSelectorElementHeight;
                if (y < 0) {
                    i2 = -i2;
                }
                y = i2;
            }
            this.mCurrentScrollOffset += y;
            while (true) {
                int i3 = this.mCurrentScrollOffset;
                int i4 = i3 - this.mInitialScrollOffset;
                int i5 = this.mSelectorTextGapHeight;
                if (i4 <= i5) {
                    break;
                }
                this.mCurrentScrollOffset = i3 - (this.mTextSize + i5);
                decrementSelectorIndices(selectorIndices);
                setValueInternal(selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX], true);
                if (!this.mWrapSelectorWheel && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] <= this.mMinValue) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
                if (needToPlayIvtEffectWhenScrolling(y)) {
                    playIvtEffect();
                }
                playSound();
            }
            while (true) {
                i = this.mCurrentScrollOffset;
                int i6 = i - this.mInitialScrollOffset;
                int i7 = this.mSelectorTextGapHeight;
                if (i6 >= (-i7)) {
                    break;
                }
                this.mCurrentScrollOffset = i + this.mTextSize + i7;
                incrementSelectorIndices(selectorIndices);
                setValueInternal(selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX], true);
                if (!this.mWrapSelectorWheel && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] >= this.mMaxValue) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
                if (needToPlayIvtEffectWhenScrolling(y)) {
                    playIvtEffect();
                }
                playSound();
            }
            if (startScrollOffset != i) {
                onScrollChanged(0, i, 0, startScrollOffset);
            }
        } else {
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeVerticalScrollOffset() {
        return this.mCurrentScrollOffset;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeVerticalScrollRange() {
        return ((this.mMaxValue - this.mMinValue) + 1) * this.mSelectorElementHeight;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeVerticalScrollExtent() {
        return getHeight();
    }

    @Override // android.view.View
    public int getSolidColor() {
        return this.mSolidColor;
    }

    /* access modifiers changed from: protected */
    public List<Integer> getFlingState() {
        List<Integer> velocityList = new ArrayList<>(4);
        velocityList.add(Integer.valueOf(this.mMaximumFlingVelocity));
        velocityList.add(Integer.valueOf(this.mRealTimeVelocity));
        velocityList.add(Integer.valueOf(this.mRealTimeFlingVelocity));
        velocityList.add(Integer.valueOf(this.mFlingState));
        return velocityList;
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        this.mOnValueChangeListener = onValueChangedListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter != this.mFormatter) {
            this.mFormatter = formatter;
            initializeSelectorWheelIndices();
            updateInputTextView();
        }
    }

    public void setValue(int value) {
        setValueInternal(value, false);
    }

    @Override // android.view.View
    public boolean performClick() {
        if (!this.mHasSelectorWheel) {
            return super.performClick();
        }
        if (super.performClick()) {
            return true;
        }
        showSoftInput();
        return true;
    }

    @Override // android.view.View
    public boolean performLongClick() {
        if (!this.mHasSelectorWheel) {
            return super.performLongClick();
        }
        if (!super.performLongClick()) {
            showSoftInput();
            this.mIgnoreMoveEvents = true;
        }
        return true;
    }

    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (inputMethodManager != null) {
            if (this.mHasSelectorWheel) {
                this.mInputText.setVisibility(0);
            }
            this.mInputText.requestFocus();
            inputMethodManager.showSoftInput(this.mInputText, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (inputMethodManager != null && inputMethodManager.isActive(this.mInputText)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
        if (this.mHasSelectorWheel) {
            this.mInputText.setVisibility(4);
        }
    }

    private void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            int maxTextWidth = 0;
            String[] strArr = this.mDisplayedValues;
            if (strArr == null) {
                float maxDigitWidth = 0.0f;
                for (int i = 0; i <= 9; i++) {
                    float digitWidth = this.mSelectorWheelPaint.measureText(formatNumberWithLocale(i));
                    if (digitWidth > maxDigitWidth) {
                        maxDigitWidth = digitWidth;
                    }
                }
                int numberOfDigits = 0;
                for (int current = this.mMaxValue; current > 0; current /= 10) {
                    numberOfDigits++;
                }
                maxTextWidth = (int) (((float) numberOfDigits) * maxDigitWidth);
            } else {
                int valueCount = strArr.length;
                for (int i2 = 0; i2 < valueCount; i2++) {
                    float textWidth = this.mSelectorWheelPaint.measureText(this.mDisplayedValues[i2]);
                    if (textWidth > ((float) maxTextWidth)) {
                        maxTextWidth = (int) textWidth;
                    }
                }
            }
            int maxTextWidth2 = maxTextWidth + this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight();
            if (this.mMaxWidth != maxTextWidth2) {
                int i3 = this.mMinWidth;
                if (maxTextWidth2 > i3) {
                    this.mMaxWidth = maxTextWidth2;
                } else {
                    this.mMaxWidth = i3;
                }
                invalidate();
            }
        }
    }

    public boolean getWrapSelectorWheel() {
        return this.mWrapSelectorWheel;
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        this.mWrapSelectorWheelPreferred = wrapSelectorWheel;
        updateWrapSelectorWheel();
    }

    private void updateWrapSelectorWheel() {
        boolean z = true;
        if (!(this.mMaxValue - this.mMinValue >= this.mSelectorIndices.length) || !this.mWrapSelectorWheelPreferred) {
            z = false;
        }
        this.mWrapSelectorWheel = z;
    }

    public void setOnLongPressUpdateInterval(long intervalMillis) {
        this.mLongPressUpdateInterval = intervalMillis;
    }

    public int getValue() {
        return this.mValue;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public void setMinValue(int minValue) {
        if (this.mMinValue != minValue) {
            if (minValue >= 0) {
                this.mMinValue = minValue;
                int i = this.mMinValue;
                if (i > this.mValue) {
                    this.mValue = i;
                }
                updateWrapSelectorWheel();
                initializeSelectorWheelIndices();
                updateInputTextView();
                tryComputeMaxWidth();
                invalidate();
                return;
            }
            throw new IllegalArgumentException("minValue must be >= 0");
        }
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        if (this.mMaxValue != maxValue) {
            if (maxValue >= 0) {
                this.mMaxValue = maxValue;
                int i = this.mMaxValue;
                if (i < this.mValue) {
                    this.mValue = i;
                }
                updateWrapSelectorWheel();
                initializeSelectorWheelIndices();
                updateInputTextView();
                tryComputeMaxWidth();
                invalidate();
                return;
            }
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
    }

    public String[] getDisplayedValues() {
        return this.mDisplayedValues;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (this.mDisplayedValues != displayedValues) {
            this.mDisplayedValues = displayedValues;
            if (this.mDisplayedValues != null) {
                this.mInputText.setRawInputType(ConnectivityManager.CALLBACK_PRECHECK);
            } else {
                this.mInputText.setRawInputType(2);
            }
            updateInputTextView();
            initializeSelectorWheelIndices();
            tryComputeMaxWidth();
        }
    }

    public CharSequence getDisplayedValueForCurrentSelection() {
        return this.mSelectorIndexToStringCache.get(getValue());
    }

    public void setSelectionDividerHeight(int height) {
        this.mSelectionDividerHeight = height;
        invalidate();
    }

    public int getSelectionDividerHeight() {
        return this.mSelectionDividerHeight;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public float getTopFadingEdgeStrength() {
        return 0.9f;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public float getBottomFadingEdgeStrength() {
        return 0.9f;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable selectionDivider = this.mSelectionDivider;
        if (selectionDivider != null && selectionDivider.isStateful() && selectionDivider.setState(getDrawableState())) {
            invalidateDrawable(selectionDivider);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable drawable = this.mSelectionDivider;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
    }

    @Override // android.view.View
    public void onResolveDrawables(int layoutDirection) {
        super.onResolveDrawables(layoutDirection);
        Drawable drawable = this.mSelectionDivider;
        if (drawable != null) {
            drawable.setLayoutDirection(layoutDirection);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onDraw(Canvas canvas) {
        Drawable drawable;
        Drawable drawable2;
        if (!this.mHasSelectorWheel) {
            super.onDraw(canvas);
            return;
        }
        boolean showSelectorWheel = this.mHideWheelUntilFocused ? hasFocus() : true;
        float x = (float) ((this.mRight - this.mLeft) / 2);
        float y = (float) this.mCurrentScrollOffset;
        if (showSelectorWheel && (drawable2 = this.mVirtualButtonPressedDrawable) != null && this.mScrollState == 0) {
            if (this.mDecrementVirtualButtonPressed) {
                drawable2.setState(PRESSED_STATE_SET);
                this.mVirtualButtonPressedDrawable.setBounds(0, 0, this.mRight, this.mTopSelectionDividerTop);
                this.mVirtualButtonPressedDrawable.draw(canvas);
            }
            if (this.mIncrementVirtualButtonPressed) {
                this.mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                this.mVirtualButtonPressedDrawable.setBounds(0, this.mBottomSelectionDividerBottom, this.mRight, this.mBottom);
                this.mVirtualButtonPressedDrawable.draw(canvas);
            }
        }
        int[] selectorIndices = this.mSelectorIndices;
        float y2 = y;
        for (int i = 0; i < selectorIndices.length; i++) {
            String scrollSelectorValue = this.mSelectorIndexToStringCache.get(selectorIndices[i]);
            if (!showSelectorWheel || i == this.SELECTOR_MIDDLE_ITEM_INDEX) {
                if (i == this.SELECTOR_MIDDLE_ITEM_INDEX) {
                    if (this.mInputText.getVisibility() == 0) {
                    }
                }
                y2 = adjustYPosition(i, y2) + ((float) this.mSelectorElementHeight);
            }
            setSelectorColor(i, this.mCurrentScrollOffset, this.mInitialScrollOffset, this.SELECTOR_MIDDLE_ITEM_INDEX, this.mSelectorElementHeight, this.mSelectorWheelPaint);
            canvas.drawText(scrollSelectorValue, x, adjustYCoordinate(i, y2), this.mSelectorWheelPaint);
            y2 = adjustYPosition(i, y2) + ((float) this.mSelectorElementHeight);
        }
        if (showSelectorWheel && (drawable = this.mSelectionDivider) != null) {
            int topOfTopDivider = this.mTopSelectionDividerTop;
            drawable.setBounds(0, topOfTopDivider, this.mRight, this.mSelectionDividerHeight + topOfTopDivider);
            this.mSelectionDivider.draw(canvas);
            int bottomOfBottomDivider = this.mBottomSelectionDividerBottom;
            this.mSelectionDivider.setBounds(0, bottomOfBottomDivider - this.mSelectionDividerHeight, this.mRight, bottomOfBottomDivider);
            this.mSelectionDivider.draw(canvas);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setClassName(NumberPicker.class.getName());
        event.setScrollable(true);
        event.setScrollY((this.mMinValue + this.mValue) * this.mSelectorElementHeight);
        event.setMaxScrollY((this.mMaxValue - this.mMinValue) * this.mSelectorElementHeight);
    }

    @Override // android.view.View
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (!this.mHasSelectorWheel) {
            return super.getAccessibilityNodeProvider();
        }
        if (this.mAccessibilityNodeProvider == null) {
            this.mAccessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return this.mAccessibilityNodeProvider;
    }

    public void setTextColor(int color) {
        this.mSelectorWheelPaint.setColor(color);
        this.mInputText.setTextColor(color);
        invalidate();
    }

    public int getTextColor() {
        return this.mSelectorWheelPaint.getColor();
    }

    public void setTextSize(float size) {
        this.mSelectorWheelPaint.setTextSize(size);
        this.mInputText.setTextSize(0, size);
        invalidate();
    }

    public float getTextSize() {
        return this.mSelectorWheelPaint.getTextSize();
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int size = View.MeasureSpec.getSize(measureSpec);
        int mode = View.MeasureSpec.getMode(measureSpec);
        if (mode == Integer.MIN_VALUE) {
            return View.MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), 1073741824);
        }
        if (mode == 0) {
            return View.MeasureSpec.makeMeasureSpec(maxSize, 1073741824);
        }
        if (mode == 1073741824) {
            return measureSpec;
        }
        throw new IllegalArgumentException("Unknown measure mode: " + mode);
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != -1) {
            return resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0);
        }
        return measuredSize;
    }

    @UnsupportedAppUsage
    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        int[] selectorIndices = this.mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            int selectorIndex = (i - this.SELECTOR_MIDDLE_ITEM_INDEX) + current;
            if (this.mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    private void setValueInternal(int current, boolean notifyChange) {
        int current2;
        if (this.mValue != current) {
            if (this.mWrapSelectorWheel) {
                current2 = getWrappedSelectorIndex(current);
            } else {
                current2 = Math.min(Math.max(current, this.mMinValue), this.mMaxValue);
            }
            int previous = this.mValue;
            this.mValue = current2;
            if (this.mScrollState != 2) {
                updateInputTextView();
            }
            if (notifyChange) {
                notifyChange(previous, current2);
                playIvtEffectWhenFling(previous, current2);
            }
            initializeSelectorWheelIndices();
            invalidate();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void changeValueByOne(boolean increment) {
        if (this.mHasSelectorWheel) {
            hideSoftInput();
            if (!moveToFinalScrollerPosition(this.mFlingScroller)) {
                moveToFinalScrollerPosition(this.mAdjustScroller);
            }
            this.mPreviousScrollerY = 0;
            if (increment) {
                this.mFlingScroller.startScroll(0, 0, 0, -this.mSelectorElementHeight, 300);
            } else {
                this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight, 300);
            }
            invalidate();
        } else if (increment) {
            setValueInternal(this.mValue + 1, true);
        } else {
            setValueInternal(this.mValue - 1, true);
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = this.mSelectorIndices;
        this.mSelectorTextGapHeight = (int) ((((float) ((this.mBottom - this.mTop) - (selectorIndices.length * this.mTextSize))) / ((float) selectorIndices.length)) + 0.5f);
        this.mSelectorElementHeight = initializeSelectorElementHeight(this.mTextSize, this.mSelectorTextGapHeight);
        this.mInitialScrollOffset = (this.mInputText.getBaseline() + this.mInputText.getTop()) - (this.mSelectorElementHeight * this.SELECTOR_MIDDLE_ITEM_INDEX);
        this.mCurrentScrollOffset = this.mInitialScrollOffset;
        updateInputTextView();
    }

    /* access modifiers changed from: protected */
    public void initializeFadingEdgesEx() {
        initializeFadingEdges();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(((this.mBottom - this.mTop) - this.mTextSize) / 2);
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller == this.mFlingScroller) {
            ensureScrollWheelAdjusted();
            updateInputTextView();
            onScrollStateChange(0);
        } else if (this.mScrollState != 1) {
            updateInputTextView();
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState != scrollState) {
            this.mScrollState = scrollState;
            OnScrollListener onScrollListener = this.mOnScrollListener;
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChange(this, scrollState);
            }
        }
    }

    private void fling(int velocityY) {
        this.mFlingState = 1;
        this.mPreviousScrollerY = 0;
        if (velocityY > 0) {
            this.mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            this.mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }
        setFlingDirection(velocityY);
        invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getWrappedSelectorIndex(int selectorIndex) {
        int i = this.mMaxValue;
        if (selectorIndex > i) {
            int i2 = this.mMinValue;
            return (i2 + ((selectorIndex - i) % (i - i2))) - 1;
        }
        int i3 = this.mMinValue;
        if (selectorIndex < i3) {
            return (i - ((i3 - selectorIndex) % (i - i3))) + 1;
        }
        return selectorIndex;
    }

    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex > this.mMaxValue) {
            nextScrollSelectorIndex = this.mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex < this.mMinValue) {
            nextScrollSelectorIndex = this.mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        String scrollSelectorValue;
        SparseArray<String> cache = this.mSelectorIndexToStringCache;
        if (cache.get(selectorIndex) == null) {
            int i = this.mMinValue;
            if (selectorIndex < i || selectorIndex > this.mMaxValue) {
                scrollSelectorValue = "";
            } else {
                String[] strArr = this.mDisplayedValues;
                if (strArr != null) {
                    scrollSelectorValue = strArr[selectorIndex - i];
                } else {
                    scrollSelectorValue = formatNumber(selectorIndex);
                }
            }
            cache.put(selectorIndex, scrollSelectorValue);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String formatNumber(int value) {
        Formatter formatter = this.mFormatter;
        return formatter != null ? formatter.format(value) : formatNumberWithLocale(value);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if (TextUtils.isEmpty(str)) {
            updateInputTextView();
        } else {
            setValueInternal(getSelectedPos(str.toString()), true);
        }
    }

    private boolean updateInputTextView() {
        String text;
        String[] strArr = this.mDisplayedValues;
        if (strArr == null) {
            text = formatNumber(this.mValue);
        } else {
            text = strArr[this.mValue - this.mMinValue];
        }
        if (!TextUtils.isEmpty(text)) {
            CharSequence beforeText = this.mInputText.getText();
            if (!text.equals(beforeText.toString())) {
                this.mInputText.setText(text);
                if (!AccessibilityManager.getInstance(this.mContext).isEnabled()) {
                    return true;
                }
                AccessibilityEvent event = AccessibilityEvent.obtain(16);
                this.mInputText.onInitializeAccessibilityEvent(event);
                this.mInputText.onPopulateAccessibilityEvent(event);
                event.setFromIndex(0);
                event.setRemovedCount(beforeText.length());
                event.setAddedCount(text.length());
                event.setBeforeText(beforeText);
                event.setSource(this, 2);
                requestSendAccessibilityEvent(this, event);
                return true;
            }
        }
        return false;
    }

    private void notifyChange(int previous, int current) {
        OnValueChangeListener onValueChangeListener = this.mOnValueChangeListener;
        if (onValueChangeListener != null) {
            onValueChangeListener.onValueChange(this, previous, this.mValue);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand = this.mChangeCurrentByOneFromLongPressCommand;
        if (changeCurrentByOneFromLongPressCommand == null) {
            this.mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
        this.mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(this.mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    private void removeChangeCurrentByOneFromLongPress() {
        ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand = this.mChangeCurrentByOneFromLongPressCommand;
        if (changeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
    }

    private void postBeginSoftInputOnLongPressCommand() {
        BeginSoftInputOnLongPressCommand beginSoftInputOnLongPressCommand = this.mBeginSoftInputOnLongPressCommand;
        if (beginSoftInputOnLongPressCommand == null) {
            this.mBeginSoftInputOnLongPressCommand = new BeginSoftInputOnLongPressCommand();
        } else {
            removeCallbacks(beginSoftInputOnLongPressCommand);
        }
        postDelayed(this.mBeginSoftInputOnLongPressCommand, (long) ViewConfiguration.getLongPressTimeout());
    }

    private void removeBeginSoftInputCommand() {
        BeginSoftInputOnLongPressCommand beginSoftInputOnLongPressCommand = this.mBeginSoftInputOnLongPressCommand;
        if (beginSoftInputOnLongPressCommand != null) {
            removeCallbacks(beginSoftInputOnLongPressCommand);
        }
    }

    private void removeAllCallbacks() {
        ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand = this.mChangeCurrentByOneFromLongPressCommand;
        if (changeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
        SetSelectionCommand setSelectionCommand = this.mSetSelectionCommand;
        if (setSelectionCommand != null) {
            setSelectionCommand.cancel();
        }
        BeginSoftInputOnLongPressCommand beginSoftInputOnLongPressCommand = this.mBeginSoftInputOnLongPressCommand;
        if (beginSoftInputOnLongPressCommand != null) {
            removeCallbacks(beginSoftInputOnLongPressCommand);
        }
        this.mPressedStateHelper.cancel();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getSelectedPos(String value) {
        if (this.mDisplayedValues == null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return this.mMinValue;
            }
        } else {
            for (int i = 0; i < this.mDisplayedValues.length; i++) {
                value = value.toLowerCase();
                if (this.mDisplayedValues[i].toLowerCase().startsWith(value)) {
                    return this.mMinValue + i;
                }
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e2) {
                return this.mMinValue;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        if (this.mSetSelectionCommand == null) {
            this.mSetSelectionCommand = new SetSelectionCommand(this.mInputText);
        }
        this.mSetSelectionCommand.post(selectionStart, selectionEnd);
    }

    class InputTextFilter extends NumberKeyListener {
        InputTextFilter() {
        }

        @Override // android.text.method.KeyListener
        public int getInputType() {
            return 1;
        }

        /* access modifiers changed from: protected */
        @Override // android.text.method.NumberKeyListener
        public char[] getAcceptedChars() {
            return NumberPicker.DIGIT_CHARACTERS;
        }

        @Override // android.text.method.NumberKeyListener, android.text.InputFilter
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (NumberPicker.this.mSetSelectionCommand != null) {
                NumberPicker.this.mSetSelectionCommand.cancel();
            }
            if (NumberPicker.this.mDisplayedValues == null) {
                CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }
                String result = String.valueOf(dest.subSequence(0, dstart)) + ((Object) filtered) + ((Object) dest.subSequence(dend, dest.length()));
                if ("".equals(result)) {
                    return result;
                }
                if (NumberPicker.this.getSelectedPos(result) > NumberPicker.this.mMaxValue || result.length() > String.valueOf(NumberPicker.this.mMaxValue).length()) {
                    return "";
                }
                return filtered;
            }
            CharSequence filtered2 = String.valueOf(source.subSequence(start, end));
            if (TextUtils.isEmpty(filtered2)) {
                return "";
            }
            String result2 = String.valueOf(dest.subSequence(0, dstart)) + ((Object) filtered2) + ((Object) dest.subSequence(dend, dest.length()));
            String str = String.valueOf(result2).toLowerCase();
            String[] strArr = NumberPicker.this.mDisplayedValues;
            for (String val : strArr) {
                if (val.toLowerCase().startsWith(str)) {
                    NumberPicker.this.postSetSelectionCommand(result2.length(), val.length());
                    return val.subSequence(dstart, val.length());
                }
            }
            return "";
        }
    }

    private boolean ensureScrollWheelAdjusted() {
        int deltaY = this.mInitialScrollOffset - this.mCurrentScrollOffset;
        if (deltaY == 0) {
            return false;
        }
        this.mPreviousScrollerY = 0;
        int abs = Math.abs(deltaY);
        int i = this.mSelectorElementHeight;
        if (abs > i / 2) {
            if (deltaY > 0) {
                i = -i;
            }
            deltaY += i;
        }
        this.mAdjustScroller.startScroll(0, 0, 0, deltaY, 800);
        invalidate();
        return true;
    }

    /* access modifiers changed from: package-private */
    public class PressedStateHelper implements Runnable {
        public static final int BUTTON_DECREMENT = 2;
        public static final int BUTTON_INCREMENT = 1;
        private final int MODE_PRESS = 1;
        private final int MODE_TAPPED = 2;
        private int mManagedButton;
        private int mMode;

        PressedStateHelper() {
        }

        public void cancel() {
            this.mMode = 0;
            this.mManagedButton = 0;
            NumberPicker.this.removeCallbacks(this);
            if (NumberPicker.this.mIncrementVirtualButtonPressed) {
                NumberPicker.this.mIncrementVirtualButtonPressed = false;
                NumberPicker numberPicker = NumberPicker.this;
                numberPicker.invalidate(0, numberPicker.mBottomSelectionDividerBottom, NumberPicker.this.mRight, NumberPicker.this.mBottom);
            }
            NumberPicker.this.mDecrementVirtualButtonPressed = false;
            if (NumberPicker.this.mDecrementVirtualButtonPressed) {
                NumberPicker numberPicker2 = NumberPicker.this;
                numberPicker2.invalidate(0, 0, numberPicker2.mRight, NumberPicker.this.mTopSelectionDividerTop);
            }
        }

        public void buttonPressDelayed(int button) {
            cancel();
            this.mMode = 1;
            this.mManagedButton = button;
            NumberPicker.this.postDelayed(this, (long) ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int button) {
            cancel();
            this.mMode = 2;
            this.mManagedButton = button;
            NumberPicker.this.post(this);
        }

        @Override // java.lang.Runnable
        public void run() {
            int i = this.mMode;
            if (i == 1) {
                int i2 = this.mManagedButton;
                if (i2 == 1) {
                    NumberPicker.this.mIncrementVirtualButtonPressed = true;
                    NumberPicker numberPicker = NumberPicker.this;
                    numberPicker.invalidate(0, numberPicker.mBottomSelectionDividerBottom, NumberPicker.this.mRight, NumberPicker.this.mBottom);
                } else if (i2 == 2) {
                    NumberPicker.this.mDecrementVirtualButtonPressed = true;
                    NumberPicker numberPicker2 = NumberPicker.this;
                    numberPicker2.invalidate(0, 0, numberPicker2.mRight, NumberPicker.this.mTopSelectionDividerTop);
                }
            } else if (i == 2) {
                int i3 = this.mManagedButton;
                if (i3 == 1) {
                    if (!NumberPicker.this.mIncrementVirtualButtonPressed) {
                        NumberPicker.this.postDelayed(this, (long) ViewConfiguration.getPressedStateDuration());
                    }
                    NumberPicker.access$1280(NumberPicker.this, 1);
                    NumberPicker numberPicker3 = NumberPicker.this;
                    numberPicker3.invalidate(0, numberPicker3.mBottomSelectionDividerBottom, NumberPicker.this.mRight, NumberPicker.this.mBottom);
                } else if (i3 == 2) {
                    if (!NumberPicker.this.mDecrementVirtualButtonPressed) {
                        NumberPicker.this.postDelayed(this, (long) ViewConfiguration.getPressedStateDuration());
                    }
                    NumberPicker.access$1680(NumberPicker.this, 1);
                    NumberPicker numberPicker4 = NumberPicker.this;
                    numberPicker4.invalidate(0, 0, numberPicker4.mRight, NumberPicker.this.mTopSelectionDividerTop);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SetSelectionCommand implements Runnable {
        private final EditText mInputText;
        private boolean mPosted;
        private int mSelectionEnd;
        private int mSelectionStart;

        public SetSelectionCommand(EditText inputText) {
            this.mInputText = inputText;
        }

        public void post(int selectionStart, int selectionEnd) {
            this.mSelectionStart = selectionStart;
            this.mSelectionEnd = selectionEnd;
            if (!this.mPosted) {
                this.mInputText.post(this);
                this.mPosted = true;
            }
        }

        public void cancel() {
            if (this.mPosted) {
                this.mInputText.removeCallbacks(this);
                this.mPosted = false;
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mPosted = false;
            try {
                this.mInputText.setSelection(this.mSelectionStart, this.mSelectionEnd);
            } catch (IndexOutOfBoundsException e) {
                Log.w(NumberPicker.TAG, "Index out of bounds exception catched.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        ChangeCurrentByOneFromLongPressCommand() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setStep(boolean increment) {
            this.mIncrement = increment;
        }

        @Override // java.lang.Runnable
        public void run() {
            NumberPicker.this.changeValueByOne(this.mIncrement);
            NumberPicker numberPicker = NumberPicker.this;
            numberPicker.postDelayed(this, numberPicker.mLongPressUpdateInterval);
        }
    }

    public static class CustomEditText extends EditText {
        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override // android.widget.TextView
        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == 6) {
                clearFocus();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class BeginSoftInputOnLongPressCommand implements Runnable {
        BeginSoftInputOnLongPressCommand() {
        }

        @Override // java.lang.Runnable
        public void run() {
            NumberPicker.this.performLongClick();
        }
    }

    /* access modifiers changed from: package-private */
    public class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;
        private static final int VIRTUAL_VIEW_ID_DECREMENT = 3;
        private static final int VIRTUAL_VIEW_ID_INCREMENT = 1;
        private static final int VIRTUAL_VIEW_ID_INPUT = 2;
        private int mAccessibilityFocusedView = Integer.MIN_VALUE;
        private final int[] mTempArray = new int[2];
        private final Rect mTempRect = new Rect();

        AccessibilityNodeProviderImpl() {
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            if (virtualViewId == -1) {
                return createAccessibilityNodeInfoForNumberPicker(NumberPicker.this.mScrollX, NumberPicker.this.mScrollY, NumberPicker.this.mScrollX + (NumberPicker.this.mRight - NumberPicker.this.mLeft), NumberPicker.this.mScrollY + (NumberPicker.this.mBottom - NumberPicker.this.mTop));
            }
            if (virtualViewId == 1) {
                return createAccessibilityNodeInfoForVirtualButton(1, getVirtualIncrementButtonText(), NumberPicker.this.mScrollX, NumberPicker.this.mBottomSelectionDividerBottom - NumberPicker.this.mSelectionDividerHeight, NumberPicker.this.mScrollX + (NumberPicker.this.mRight - NumberPicker.this.mLeft), NumberPicker.this.mScrollY + (NumberPicker.this.mBottom - NumberPicker.this.mTop));
            }
            if (virtualViewId == 2) {
                return createAccessibiltyNodeInfoForInputText(NumberPicker.this.mScrollX, NumberPicker.this.mTopSelectionDividerTop + NumberPicker.this.mSelectionDividerHeight, NumberPicker.this.mScrollX + (NumberPicker.this.mRight - NumberPicker.this.mLeft), NumberPicker.this.mBottomSelectionDividerBottom - NumberPicker.this.mSelectionDividerHeight);
            }
            if (virtualViewId != 3) {
                return super.createAccessibilityNodeInfo(virtualViewId);
            }
            return createAccessibilityNodeInfoForVirtualButton(3, getVirtualDecrementButtonText(), NumberPicker.this.mScrollX, NumberPicker.this.mScrollY, NumberPicker.this.mScrollX + (NumberPicker.this.mRight - NumberPicker.this.mLeft), NumberPicker.this.mTopSelectionDividerTop + NumberPicker.this.mSelectionDividerHeight);
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String searched, int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase();
            List<AccessibilityNodeInfo> result = new ArrayList<>();
            if (virtualViewId == -1) {
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 3, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 2, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 1, result);
                return result;
            } else if (virtualViewId != 1 && virtualViewId != 2 && virtualViewId != 3) {
                return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
            } else {
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId, result);
                return result;
            }
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            boolean increment = false;
            if (virtualViewId != -1) {
                if (virtualViewId != 1) {
                    if (virtualViewId != 2) {
                        if (virtualViewId == 3) {
                            if (action != 16) {
                                if (action != 64) {
                                    if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                                        return false;
                                    }
                                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                                    sendAccessibilityEventForVirtualView(virtualViewId, 65536);
                                    NumberPicker numberPicker = NumberPicker.this;
                                    numberPicker.invalidate(0, 0, numberPicker.mRight, NumberPicker.this.mTopSelectionDividerTop);
                                    return true;
                                } else if (this.mAccessibilityFocusedView == virtualViewId) {
                                    return false;
                                } else {
                                    this.mAccessibilityFocusedView = virtualViewId;
                                    sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                                    NumberPicker numberPicker2 = NumberPicker.this;
                                    numberPicker2.invalidate(0, 0, numberPicker2.mRight, NumberPicker.this.mTopSelectionDividerTop);
                                    return true;
                                }
                            } else if (!NumberPicker.this.isEnabled()) {
                                return false;
                            } else {
                                if (virtualViewId == 1) {
                                    increment = true;
                                }
                                NumberPicker.this.changeValueByOne(increment);
                                sendAccessibilityEventForVirtualView(virtualViewId, 1);
                                return true;
                            }
                        }
                    } else if (action != 1) {
                        if (action != 2) {
                            if (action != 16) {
                                if (action != 32) {
                                    if (action != 64) {
                                        if (action != 128) {
                                            return NumberPicker.this.mInputText.performAccessibilityAction(action, arguments);
                                        }
                                        if (this.mAccessibilityFocusedView != virtualViewId) {
                                            return false;
                                        }
                                        this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                                        sendAccessibilityEventForVirtualView(virtualViewId, 65536);
                                        NumberPicker.this.mInputText.invalidate();
                                        return true;
                                    } else if (this.mAccessibilityFocusedView == virtualViewId) {
                                        return false;
                                    } else {
                                        this.mAccessibilityFocusedView = virtualViewId;
                                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                                        NumberPicker.this.mInputText.invalidate();
                                        return true;
                                    }
                                } else if (!NumberPicker.this.isEnabled()) {
                                    return false;
                                } else {
                                    NumberPicker.this.performLongClick();
                                    return true;
                                }
                            } else if (!NumberPicker.this.isEnabled()) {
                                return false;
                            } else {
                                NumberPicker.this.performClick();
                                return true;
                            }
                        } else if (!NumberPicker.this.isEnabled() || !NumberPicker.this.mInputText.isFocused()) {
                            return false;
                        } else {
                            NumberPicker.this.mInputText.clearFocus();
                            return true;
                        }
                    } else if (!NumberPicker.this.isEnabled() || NumberPicker.this.mInputText.isFocused()) {
                        return false;
                    } else {
                        return NumberPicker.this.mInputText.requestFocus();
                    }
                } else if (action != 16) {
                    if (action != 64) {
                        if (action != 128 || this.mAccessibilityFocusedView != virtualViewId) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                        sendAccessibilityEventForVirtualView(virtualViewId, 65536);
                        NumberPicker numberPicker3 = NumberPicker.this;
                        numberPicker3.invalidate(0, numberPicker3.mBottomSelectionDividerBottom, NumberPicker.this.mRight, NumberPicker.this.mBottom);
                        return true;
                    } else if (this.mAccessibilityFocusedView == virtualViewId) {
                        return false;
                    } else {
                        this.mAccessibilityFocusedView = virtualViewId;
                        sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                        NumberPicker numberPicker4 = NumberPicker.this;
                        numberPicker4.invalidate(0, numberPicker4.mBottomSelectionDividerBottom, NumberPicker.this.mRight, NumberPicker.this.mBottom);
                        return true;
                    }
                } else if (!NumberPicker.this.isEnabled()) {
                    return false;
                } else {
                    NumberPicker.this.changeValueByOne(true);
                    sendAccessibilityEventForVirtualView(virtualViewId, 1);
                    return true;
                }
            } else if (action != 64) {
                if (action != 128) {
                    if (action != 4096) {
                        if (action == 8192) {
                            if (!NumberPicker.this.isEnabled() || (!NumberPicker.this.getWrapSelectorWheel() && NumberPicker.this.getValue() <= NumberPicker.this.getMinValue())) {
                                return false;
                            }
                            NumberPicker.this.changeValueByOne(false);
                            return true;
                        }
                    } else if (!NumberPicker.this.isEnabled() || (!NumberPicker.this.getWrapSelectorWheel() && NumberPicker.this.getValue() >= NumberPicker.this.getMaxValue())) {
                        return false;
                    } else {
                        NumberPicker.this.changeValueByOne(true);
                        return true;
                    }
                } else if (this.mAccessibilityFocusedView != virtualViewId) {
                    return false;
                } else {
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    NumberPicker.this.clearAccessibilityFocus();
                    return true;
                }
            } else if (this.mAccessibilityFocusedView == virtualViewId) {
                return false;
            } else {
                this.mAccessibilityFocusedView = virtualViewId;
                NumberPicker.this.requestAccessibilityFocus();
                return true;
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            if (virtualViewId != 1) {
                if (virtualViewId == 2) {
                    sendAccessibilityEventForVirtualText(eventType);
                } else if (virtualViewId == 3 && hasVirtualDecrementButton()) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualDecrementButtonText());
                }
            } else if (hasVirtualIncrementButton()) {
                sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualIncrementButtonText());
            }
        }

        private void sendAccessibilityEventForVirtualText(int eventType) {
            if (AccessibilityManager.getInstance(NumberPicker.this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                NumberPicker.this.mInputText.onInitializeAccessibilityEvent(event);
                NumberPicker.this.mInputText.onPopulateAccessibilityEvent(event);
                event.setSource(NumberPicker.this, 2);
                NumberPicker numberPicker = NumberPicker.this;
                numberPicker.requestSendAccessibilityEvent(numberPicker, event);
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType, String text) {
            if (AccessibilityManager.getInstance(NumberPicker.this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(NumberPicker.this.mContext.getPackageName());
                event.getText().add(text);
                event.setEnabled(NumberPicker.this.isEnabled());
                event.setSource(NumberPicker.this, virtualViewId);
                NumberPicker numberPicker = NumberPicker.this;
                numberPicker.requestSendAccessibilityEvent(numberPicker, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase, int virtualViewId, List<AccessibilityNodeInfo> outResult) {
            if (virtualViewId == 1) {
                String text = getVirtualIncrementButtonText();
                if (!TextUtils.isEmpty(text) && text.toString().toLowerCase().contains(searchedLowerCase)) {
                    outResult.add(createAccessibilityNodeInfo(1));
                }
            } else if (virtualViewId == 2) {
                CharSequence text2 = NumberPicker.this.mInputText.getText();
                if (TextUtils.isEmpty(text2) || !text2.toString().toLowerCase().contains(searchedLowerCase)) {
                    CharSequence contentDesc = NumberPicker.this.mInputText.getText();
                    if (!TextUtils.isEmpty(contentDesc) && contentDesc.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(2));
                        return;
                    }
                    return;
                }
                outResult.add(createAccessibilityNodeInfo(2));
            } else if (virtualViewId == 3) {
                String text3 = getVirtualDecrementButtonText();
                if (!TextUtils.isEmpty(text3) && text3.toString().toLowerCase().contains(searchedLowerCase)) {
                    outResult.add(createAccessibilityNodeInfo(3));
                }
            }
        }

        private AccessibilityNodeInfo createAccessibiltyNodeInfoForInputText(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = NumberPicker.this.mInputText.createAccessibilityNodeInfo();
            info.setSource(NumberPicker.this, 2);
            if (this.mAccessibilityFocusedView != 2) {
                info.addAction(64);
            }
            if (this.mAccessibilityFocusedView == 2) {
                info.addAction(128);
            }
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(NumberPicker.this.isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            int[] locationOnScreen = this.mTempArray;
            NumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInParent);
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int virtualViewId, String text, int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(Button.class.getName());
            info.setPackageName(NumberPicker.this.mContext.getPackageName());
            info.setSource(NumberPicker.this, virtualViewId);
            info.setParent(NumberPicker.this);
            info.setText(text);
            info.setClickable(true);
            info.setLongClickable(true);
            info.setEnabled(NumberPicker.this.isEnabled());
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(NumberPicker.this.isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            int[] locationOnScreen = this.mTempArray;
            NumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != virtualViewId) {
                info.addAction(64);
            }
            if (this.mAccessibilityFocusedView == virtualViewId) {
                info.addAction(128);
            }
            if (NumberPicker.this.isEnabled()) {
                info.addAction(16);
            }
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(NumberPicker.class.getName());
            info.setPackageName(NumberPicker.this.mContext.getPackageName());
            info.setSource(NumberPicker.this);
            if (hasVirtualDecrementButton()) {
                info.addChild(NumberPicker.this, 3);
            }
            info.addChild(NumberPicker.this, 2);
            if (hasVirtualIncrementButton()) {
                info.addChild(NumberPicker.this, 1);
            }
            info.setParent((View) NumberPicker.this.getParentForAccessibility());
            info.setEnabled(NumberPicker.this.isEnabled());
            info.setScrollable(true);
            float applicationScale = NumberPicker.this.getContext().getResources().getCompatibilityInfo().applicationScale;
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            boundsInParent.scale(applicationScale);
            info.setBoundsInParent(boundsInParent);
            info.setVisibleToUser(NumberPicker.this.isVisibleToUser());
            int[] locationOnScreen = this.mTempArray;
            NumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            boundsInParent.scale(applicationScale);
            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != -1) {
                info.addAction(64);
            }
            if (this.mAccessibilityFocusedView == -1) {
                info.addAction(128);
            }
            if (NumberPicker.this.isEnabled()) {
                if (NumberPicker.this.getWrapSelectorWheel() || NumberPicker.this.getValue() < NumberPicker.this.getMaxValue()) {
                    info.addAction(4096);
                }
                if (NumberPicker.this.getWrapSelectorWheel() || NumberPicker.this.getValue() > NumberPicker.this.getMinValue()) {
                    info.addAction(8192);
                }
            }
            return info;
        }

        private boolean hasVirtualDecrementButton() {
            return NumberPicker.this.getWrapSelectorWheel() || NumberPicker.this.getValue() > NumberPicker.this.getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return NumberPicker.this.getWrapSelectorWheel() || NumberPicker.this.getValue() < NumberPicker.this.getMaxValue();
        }

        private String getVirtualDecrementButtonText() {
            int value = NumberPicker.this.mValue - 1;
            if (NumberPicker.this.mWrapSelectorWheel) {
                value = NumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value < NumberPicker.this.mMinValue) {
                return null;
            }
            if (NumberPicker.this.mDisplayedValues == null) {
                return NumberPicker.this.formatNumber(value);
            }
            return NumberPicker.this.mDisplayedValues[value - NumberPicker.this.mMinValue];
        }

        private String getVirtualIncrementButtonText() {
            int value = NumberPicker.this.mValue + 1;
            if (NumberPicker.this.mWrapSelectorWheel) {
                value = NumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value > NumberPicker.this.mMaxValue) {
                return null;
            }
            if (NumberPicker.this.mDisplayedValues == null) {
                return NumberPicker.this.formatNumber(value);
            }
            return NumberPicker.this.mDisplayedValues[value - NumberPicker.this.mMinValue];
        }
    }

    private static String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", Integer.valueOf(value));
    }

    public void addFireList(NumberPicker np) {
    }

    /* access modifiers changed from: protected */
    public int getNormalTextColor(int color) {
        return color;
    }

    /* access modifiers changed from: protected */
    public void setSelectorColor(int i, int currentOffset, int initOffset, int index, int height, Paint paint) {
    }

    /* access modifiers changed from: protected */
    public float adjustYPosition(int i, float y) {
        return y;
    }

    /* access modifiers changed from: protected */
    public int initializeSelectorElementHeight(int textSize, int selectorTextGapHeight) {
        return textSize + selectorTextGapHeight;
    }

    public EditText getInputText() {
        return this.mInputText;
    }

    /* access modifiers changed from: protected */
    public Paint getSelectorWheelPaint() {
        return this.mSelectorWheelPaint;
    }

    /* access modifiers changed from: protected */
    public boolean getHasSelectorWheel() {
        return this.mHasSelectorWheel;
    }

    /* access modifiers changed from: protected */
    public void playIvtEffect() {
    }

    /* access modifiers changed from: protected */
    public boolean needToPlayIvtEffectWhenScrolling(int scrollByY) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void playIvtEffectWhenFling(int previous, int current) {
    }

    /* access modifiers changed from: protected */
    public void setFlingDirection(int velocityY) {
    }

    /* access modifiers changed from: protected */
    public float adjustYCoordinate(int i, float y) {
        return y;
    }

    /* access modifiers changed from: protected */
    public void playSound() {
    }

    public int getSelectorMiddleItemIdex() {
        return this.SELECTOR_MIDDLE_ITEM_INDEX;
    }

    public void setSelectMiddleItemIdex(int selectMiddleItemIdex) {
        this.SELECTOR_MIDDLE_ITEM_INDEX = selectMiddleItemIdex;
    }

    public int[] getSelectorIndices() {
        return this.mSelectorIndices;
    }

    public void setSelectorIndices(int[] selectorIndices) {
        this.mSelectorIndices = selectorIndices;
    }
}
