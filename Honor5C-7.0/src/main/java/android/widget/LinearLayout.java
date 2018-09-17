package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.FlagToString;
import android.view.ViewDebug.IntToString;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewHierarchyEncoder;
import android.view.inputmethod.EditorInfo;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.util.AsyncService;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;

@RemoteView
public class LinearLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_CENTER_VERTICAL = 0;
    private static final int INDEX_FILL = 3;
    private static final int INDEX_TOP = 1;
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    public static final int SHOW_DIVIDER_END = 4;
    public static final int SHOW_DIVIDER_MIDDLE = 2;
    public static final int SHOW_DIVIDER_NONE = 0;
    public static final int VERTICAL = 1;
    private static final int VERTICAL_GRAVITY_COUNT = 4;
    private final boolean mAllowInconsistentMeasurement;
    @ExportedProperty(category = "layout")
    private boolean mBaselineAligned;
    @ExportedProperty(category = "layout")
    private int mBaselineAlignedChildIndex;
    @ExportedProperty(category = "measurement")
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    @ExportedProperty(category = "measurement", flagMapping = {@FlagToString(equals = -1, mask = -1, name = "NONE"), @FlagToString(equals = 0, mask = 0, name = "NONE"), @FlagToString(equals = 48, mask = 48, name = "TOP"), @FlagToString(equals = 80, mask = 80, name = "BOTTOM"), @FlagToString(equals = 3, mask = 3, name = "LEFT"), @FlagToString(equals = 5, mask = 5, name = "RIGHT"), @FlagToString(equals = 8388611, mask = 8388611, name = "START"), @FlagToString(equals = 8388613, mask = 8388613, name = "END"), @FlagToString(equals = 16, mask = 16, name = "CENTER_VERTICAL"), @FlagToString(equals = 112, mask = 112, name = "FILL_VERTICAL"), @FlagToString(equals = 1, mask = 1, name = "CENTER_HORIZONTAL"), @FlagToString(equals = 7, mask = 7, name = "FILL_HORIZONTAL"), @FlagToString(equals = 17, mask = 17, name = "CENTER"), @FlagToString(equals = 119, mask = 119, name = "FILL"), @FlagToString(equals = 8388608, mask = 8388608, name = "RELATIVE")}, formatToHexString = true)
    private int mGravity;
    public boolean mHwActionBarTabLayoutUsed;
    public boolean mHwActionBarViewUsed;
    private int mLayoutDirection;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    @ExportedProperty(category = "measurement")
    private int mOrientation;
    private int mShowDividers;
    @ExportedProperty(category = "measurement")
    private int mTotalLength;
    @ExportedProperty(category = "layout")
    private boolean mUseLargestChild;
    @ExportedProperty(category = "layout")
    private float mWeightSum;

    public static class LayoutParams extends MarginLayoutParams {
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = -1, to = "NONE"), @IntToString(from = 0, to = "NONE"), @IntToString(from = 48, to = "TOP"), @IntToString(from = 80, to = "BOTTOM"), @IntToString(from = 3, to = "LEFT"), @IntToString(from = 5, to = "RIGHT"), @IntToString(from = 8388611, to = "START"), @IntToString(from = 8388613, to = "END"), @IntToString(from = 16, to = "CENTER_VERTICAL"), @IntToString(from = 112, to = "FILL_VERTICAL"), @IntToString(from = 1, to = "CENTER_HORIZONTAL"), @IntToString(from = 7, to = "FILL_HORIZONTAL"), @IntToString(from = 17, to = "CENTER"), @IntToString(from = 119, to = "FILL")})
        public int gravity;
        @ExportedProperty(category = "layout")
        public float weight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = -1;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.LinearLayout_Layout);
            this.weight = a.getFloat(LinearLayout.INDEX_FILL, 0.0f);
            this.gravity = a.getInt(LinearLayout.SHOW_DIVIDER_NONE, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height);
            this.gravity = -1;
            this.weight = weight;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
            this.gravity = -1;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.gravity = -1;
        }

        public LayoutParams(LayoutParams source) {
            super((MarginLayoutParams) source);
            this.gravity = -1;
            this.weight = source.weight;
            this.gravity = source.gravity;
        }

        public String debug(String output) {
            return output + "LinearLayout.LayoutParams={width=" + android.view.ViewGroup.LayoutParams.sizeToString(this.width) + ", height=" + android.view.ViewGroup.LayoutParams.sizeToString(this.height) + " weight=" + this.weight + "}";
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:weight", this.weight);
            encoder.addProperty("layout:gravity", this.gravity);
        }
    }

    public LinearLayout(Context context) {
        this(context, null);
    }

    public LinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, SHOW_DIVIDER_NONE);
    }

    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, SHOW_DIVIDER_NONE);
    }

    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        boolean z = true;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = SHOW_DIVIDER_NONE;
        this.mGravity = 8388659;
        this.mLayoutDirection = -1;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinearLayout, defStyleAttr, defStyleRes);
        int index = a.getInt(VERTICAL, -1);
        if (index >= 0) {
            setOrientation(index);
        }
        index = a.getInt(SHOW_DIVIDER_NONE, -1);
        if (index >= 0) {
            setGravity(index);
        }
        boolean baselineAligned = a.getBoolean(SHOW_DIVIDER_MIDDLE, true);
        if (!baselineAligned) {
            setBaselineAligned(baselineAligned);
        }
        this.mWeightSum = a.getFloat(VERTICAL_GRAVITY_COUNT, android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        this.mBaselineAlignedChildIndex = a.getInt(INDEX_FILL, -1);
        this.mUseLargestChild = a.getBoolean(6, false);
        setDividerDrawable(a.getDrawable(5));
        this.mShowDividers = a.getInt(7, SHOW_DIVIDER_NONE);
        this.mDividerPadding = a.getDimensionPixelSize(8, SHOW_DIVIDER_NONE);
        if (context.getApplicationInfo().targetSdkVersion > 23) {
            z = false;
        }
        this.mAllowInconsistentMeasurement = z;
        a.recycle();
    }

    public void setShowDividers(int showDividers) {
        if (showDividers != this.mShowDividers) {
            requestLayout();
        }
        this.mShowDividers = showDividers;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public int getShowDividers() {
        return this.mShowDividers;
    }

    public Drawable getDividerDrawable() {
        return this.mDivider;
    }

    public void setDividerDrawable(Drawable divider) {
        boolean z = false;
        if (divider != this.mDivider) {
            this.mDivider = divider;
            if (divider != null) {
                this.mDividerWidth = divider.getIntrinsicWidth();
                this.mDividerHeight = divider.getIntrinsicHeight();
            } else {
                this.mDividerWidth = SHOW_DIVIDER_NONE;
                this.mDividerHeight = SHOW_DIVIDER_NONE;
            }
            if (divider == null) {
                z = true;
            }
            setWillNotDraw(z);
            requestLayout();
        }
    }

    public void setDividerPadding(int padding) {
        this.mDividerPadding = padding;
    }

    public int getDividerPadding() {
        return this.mDividerPadding;
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation == VERTICAL) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
    }

    void drawDividersVertical(Canvas canvas) {
        int count = getVirtualChildCount();
        int i = SHOW_DIVIDER_NONE;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                drawHorizontalDivider(canvas, (child.getTop() - ((LayoutParams) child.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
            i += VERTICAL;
        }
        if (hasDividerBeforeChildAt(count)) {
            int bottom;
            child = getLastNonGoneChild();
            if (child == null) {
                bottom = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                bottom = child.getBottom() + ((LayoutParams) child.getLayoutParams()).bottomMargin;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    private View getLastNonGoneChild() {
        for (int i = getVirtualChildCount() - 1; i >= 0; i--) {
            View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != 8) {
                return child;
            }
        }
        return null;
    }

    public void drawDividersHorizontal(Canvas canvas) {
        LayoutParams lp;
        int count = getVirtualChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        int i = SHOW_DIVIDER_NONE;
        while (i < count) {
            int position;
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                lp = (LayoutParams) child.getLayoutParams();
                if (!isLayoutRtl) {
                    position = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                } else if (i == 0) {
                    position = (child.getRight() + lp.rightMargin) - this.mDividerWidth;
                } else {
                    position = child.getRight() + lp.rightMargin;
                }
                drawVerticalDivider(canvas, position);
            }
            i += VERTICAL;
        }
        if (hasDividerBeforeChildAt(count)) {
            child = getLastNonGoneChild();
            if (child != null) {
                lp = (LayoutParams) child.getLayoutParams();
                if (isLayoutRtl) {
                    position = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                } else {
                    position = child.getRight() + lp.rightMargin;
                }
            } else if (isLayoutRtl) {
                position = getPaddingLeft();
            } else {
                position = (getWidth() - getPaddingRight()) - this.mDividerWidth;
            }
            drawVerticalDivider(canvas, position);
        }
    }

    void drawHorizontalDivider(Canvas canvas, int top) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, top, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + top);
        this.mDivider.draw(canvas);
    }

    public void drawVerticalDivider(Canvas canvas, int left) {
        this.mDivider.setBounds(left, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + left, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    public boolean isBaselineAligned() {
        return this.mBaselineAligned;
    }

    @RemotableViewMethod
    public void setBaselineAligned(boolean baselineAligned) {
        this.mBaselineAligned = baselineAligned;
    }

    public boolean isMeasureWithLargestChildEnabled() {
        return this.mUseLargestChild;
    }

    @RemotableViewMethod
    public void setMeasureWithLargestChildEnabled(boolean enabled) {
        this.mUseLargestChild = enabled;
    }

    public int getBaseline() {
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        if (getChildCount() <= this.mBaselineAlignedChildIndex) {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
        }
        View child = getChildAt(this.mBaselineAlignedChildIndex);
        int childBaseline = child.getBaseline();
        if (childBaseline != -1) {
            int childTop = this.mBaselineChildTop;
            if (this.mOrientation == VERTICAL) {
                int majorGravity = this.mGravity & LogPower.APP_PROCESS_EXIT;
                if (majorGravity != 48) {
                    switch (majorGravity) {
                        case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                            childTop += ((((this.mBottom - this.mTop) - this.mPaddingTop) - this.mPaddingBottom) - this.mTotalLength) / SHOW_DIVIDER_MIDDLE;
                            break;
                        case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                            childTop = ((this.mBottom - this.mTop) - this.mPaddingBottom) - this.mTotalLength;
                            break;
                    }
                }
            }
            return (((LayoutParams) child.getLayoutParams()).topMargin + childTop) + childBaseline;
        } else if (this.mBaselineAlignedChildIndex == 0) {
            return -1;
        } else {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
        }
    }

    public int getBaselineAlignedChildIndex() {
        return this.mBaselineAlignedChildIndex;
    }

    @RemotableViewMethod
    public void setBaselineAlignedChildIndex(int i) {
        if (i < 0 || i >= getChildCount()) {
            throw new IllegalArgumentException("base aligned child index out of range (0, " + getChildCount() + ")");
        }
        this.mBaselineAlignedChildIndex = i;
    }

    View getVirtualChildAt(int index) {
        return getChildAt(index);
    }

    int getVirtualChildCount() {
        return getChildCount();
    }

    public float getWeightSum() {
        return this.mWeightSum;
    }

    @RemotableViewMethod
    public void setWeightSum(float weightSum) {
        this.mWeightSum = Math.max(0.0f, weightSum);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == VERTICAL) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected boolean hasDividerBeforeChildAt(int childIndex) {
        boolean z = true;
        if (childIndex == getVirtualChildCount()) {
            if ((this.mShowDividers & VERTICAL_GRAVITY_COUNT) == 0) {
                z = false;
            }
            return z;
        } else if (allViewsAreGoneBefore(childIndex)) {
            if ((this.mShowDividers & VERTICAL) == 0) {
                z = false;
            }
            return z;
        } else {
            if ((this.mShowDividers & SHOW_DIVIDER_MIDDLE) == 0) {
                z = false;
            }
            return z;
        }
    }

    private boolean allViewsAreGoneBefore(int childIndex) {
        for (int i = childIndex - 1; i >= 0; i--) {
            View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != 8) {
                return false;
            }
        }
        return true;
    }

    void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        this.mTotalLength = SHOW_DIVIDER_NONE;
        int maxWidth = SHOW_DIVIDER_NONE;
        int childState = SHOW_DIVIDER_NONE;
        int alternativeMaxWidth = SHOW_DIVIDER_NONE;
        int weightedMaxWidth = SHOW_DIVIDER_NONE;
        boolean allFillParent = true;
        float totalWeight = 0.0f;
        int count = getVirtualChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchWidth = false;
        boolean skippedMeasure = false;
        int baselineChildIndex = this.mBaselineAlignedChildIndex;
        boolean useLargestChild = this.mUseLargestChild;
        int largestChildHeight = RtlSpacingHelper.UNDEFINED;
        int consumedExcessSpace = SHOW_DIVIDER_NONE;
        int i = SHOW_DIVIDER_NONE;
        while (i < count) {
            LayoutParams lp;
            int totalLength;
            int childHeight;
            boolean matchWidthLocally;
            int margin;
            int measuredWidth;
            View child = getVirtualChildAt(i);
            if (child == null) {
                this.mTotalLength += measureNullChild(i);
            } else if (child.getVisibility() == 8) {
                i += getChildrenSkipCount(child, i);
            } else {
                if (hasDividerBeforeChildAt(i)) {
                    this.mTotalLength += this.mDividerHeight;
                }
                lp = (LayoutParams) child.getLayoutParams();
                totalWeight += lp.weight;
                boolean useExcessSpace = lp.height == 0 && lp.weight > 0.0f;
                if (heightMode == 1073741824 && useExcessSpace) {
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (lp.topMargin + totalLength) + lp.bottomMargin);
                    skippedMeasure = true;
                } else {
                    if (useExcessSpace) {
                        lp.height = -2;
                    }
                    measureChildBeforeLayout(child, i, widthMeasureSpec, SHOW_DIVIDER_NONE, heightMeasureSpec, totalWeight == 0.0f ? this.mTotalLength : SHOW_DIVIDER_NONE);
                    childHeight = child.getMeasuredHeight();
                    if (useExcessSpace) {
                        lp.height = SHOW_DIVIDER_NONE;
                        consumedExcessSpace += childHeight;
                    }
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((totalLength + childHeight) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                    if (useLargestChild) {
                        largestChildHeight = Math.max(childHeight, largestChildHeight);
                    }
                }
                if (baselineChildIndex >= 0 && baselineChildIndex == i + VERTICAL) {
                    this.mBaselineChildTop = this.mTotalLength;
                }
                if (i >= baselineChildIndex || lp.weight <= 0.0f) {
                    matchWidthLocally = false;
                    if (widthMode != 1073741824 && lp.width == -1) {
                        matchWidth = true;
                        matchWidthLocally = true;
                    }
                    margin = lp.leftMargin + lp.rightMargin;
                    measuredWidth = child.getMeasuredWidth() + margin;
                    maxWidth = Math.max(maxWidth, measuredWidth);
                    childState = View.combineMeasuredStates(childState, child.getMeasuredState());
                    allFillParent = allFillParent && lp.width == -1;
                    if (lp.weight > 0.0f) {
                        if (!matchWidthLocally) {
                            margin = measuredWidth;
                        }
                        weightedMaxWidth = Math.max(weightedMaxWidth, margin);
                    } else {
                        if (!matchWidthLocally) {
                            margin = measuredWidth;
                        }
                        alternativeMaxWidth = Math.max(alternativeMaxWidth, margin);
                    }
                    i += getChildrenSkipCount(child, i);
                } else {
                    throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                }
            }
            i += VERTICAL;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(count)) {
            this.mTotalLength += this.mDividerHeight;
        }
        if (useLargestChild && (heightMode == Integer.MIN_VALUE || heightMode == 0)) {
            this.mTotalLength = SHOW_DIVIDER_NONE;
            i = SHOW_DIVIDER_NONE;
            while (i < count) {
                child = getVirtualChildAt(i);
                if (child == null) {
                    this.mTotalLength += measureNullChild(i);
                } else if (child.getVisibility() == 8) {
                    i += getChildrenSkipCount(child, i);
                } else {
                    lp = (LayoutParams) child.getLayoutParams();
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((totalLength + largestChildHeight) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                }
                i += VERTICAL;
            }
        }
        this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
        int heightSizeAndState = View.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), heightMeasureSpec, SHOW_DIVIDER_NONE);
        int i2 = (heightSizeAndState & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT) - this.mTotalLength;
        if (this.mAllowInconsistentMeasurement) {
            consumedExcessSpace = SHOW_DIVIDER_NONE;
        }
        int remainingExcess = i2 + consumedExcessSpace;
        if (skippedMeasure || (remainingExcess != 0 && totalWeight > 0.0f)) {
            float remainingWeightSum = this.mWeightSum > 0.0f ? this.mWeightSum : totalWeight;
            this.mTotalLength = SHOW_DIVIDER_NONE;
            for (i = SHOW_DIVIDER_NONE; i < count; i += VERTICAL) {
                child = getVirtualChildAt(i);
                if (!(child == null || child.getVisibility() == 8)) {
                    lp = (LayoutParams) child.getLayoutParams();
                    float childWeight = lp.weight;
                    if (childWeight > 0.0f) {
                        int share = (int) ((((float) remainingExcess) * childWeight) / remainingWeightSum);
                        remainingExcess -= share;
                        remainingWeightSum -= childWeight;
                        if (this.mUseLargestChild && heightMode != 1073741824) {
                            childHeight = largestChildHeight;
                        } else if (lp.height != 0 || (this.mAllowInconsistentMeasurement && heightMode != 1073741824)) {
                            childHeight = child.getMeasuredHeight() + share;
                        } else {
                            childHeight = share;
                        }
                        child.measure(ViewGroup.getChildMeasureSpec(widthMeasureSpec, ((this.mPaddingLeft + this.mPaddingRight) + lp.leftMargin) + lp.rightMargin, lp.width), MeasureSpec.makeMeasureSpec(Math.max(SHOW_DIVIDER_NONE, childHeight), EditorInfo.IME_FLAG_NO_ENTER_ACTION));
                        childState = View.combineMeasuredStates(childState, child.getMeasuredState() & InputDevice.SOURCE_ANY);
                    }
                    margin = lp.leftMargin + lp.rightMargin;
                    measuredWidth = child.getMeasuredWidth() + margin;
                    maxWidth = Math.max(maxWidth, measuredWidth);
                    matchWidthLocally = widthMode != 1073741824 ? lp.width == -1 : false;
                    if (!matchWidthLocally) {
                        margin = measuredWidth;
                    }
                    alternativeMaxWidth = Math.max(alternativeMaxWidth, margin);
                    allFillParent = allFillParent && lp.width == -1;
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((child.getMeasuredHeight() + totalLength) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                }
            }
            this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
        } else {
            alternativeMaxWidth = Math.max(alternativeMaxWidth, weightedMaxWidth);
            if (useLargestChild && heightMode != 1073741824) {
                for (i = SHOW_DIVIDER_NONE; i < count; i += VERTICAL) {
                    child = getVirtualChildAt(i);
                    if (!(child == null || child.getVisibility() == 8)) {
                        if (((LayoutParams) child.getLayoutParams()).weight > 0.0f) {
                            child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), EditorInfo.IME_FLAG_NO_ENTER_ACTION), MeasureSpec.makeMeasureSpec(largestChildHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION));
                        }
                    }
                }
            }
        }
        if (!(allFillParent || widthMode == 1073741824)) {
            maxWidth = alternativeMaxWidth;
        }
        setMeasuredDimension(View.resolveSizeAndState(Math.max(maxWidth + (this.mPaddingLeft + this.mPaddingRight), getSuggestedMinimumWidth()), widthMeasureSpec, childState), heightSizeAndState);
        if (matchWidth) {
            forceUniformWidth(count, heightMeasureSpec);
        }
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        for (int i = SHOW_DIVIDER_NONE; i < count; i += VERTICAL) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.width == -1) {
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    measureChildWithMargins(child, uniformMeasureSpec, SHOW_DIVIDER_NONE, heightMeasureSpec, SHOW_DIVIDER_NONE);
                    lp.height = oldHeight;
                }
            }
        }
    }

    void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int childHeight;
        int childBaseline;
        int i;
        int widthSizeAndState;
        int remainingExcess;
        float remainingWeightSum;
        float childWeight;
        int share;
        this.mTotalLength = SHOW_DIVIDER_NONE;
        int maxHeight = SHOW_DIVIDER_NONE;
        int childState = SHOW_DIVIDER_NONE;
        int alternativeMaxHeight = SHOW_DIVIDER_NONE;
        int weightedMaxHeight = SHOW_DIVIDER_NONE;
        boolean allFillParent = true;
        float totalWeight = 0.0f;
        int count = getVirtualChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchHeight = false;
        boolean skippedMeasure = false;
        if (this.mMaxAscent == null || this.mMaxDescent == null) {
            this.mMaxAscent = new int[VERTICAL_GRAVITY_COUNT];
            this.mMaxDescent = new int[VERTICAL_GRAVITY_COUNT];
        }
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
        maxAscent[INDEX_FILL] = -1;
        maxAscent[SHOW_DIVIDER_MIDDLE] = -1;
        maxAscent[VERTICAL] = -1;
        maxAscent[SHOW_DIVIDER_NONE] = -1;
        maxDescent[INDEX_FILL] = -1;
        maxDescent[SHOW_DIVIDER_MIDDLE] = -1;
        maxDescent[VERTICAL] = -1;
        maxDescent[SHOW_DIVIDER_NONE] = -1;
        boolean baselineAligned = this.mBaselineAligned;
        boolean useLargestChild = this.mUseLargestChild;
        boolean isExactly = widthMode == 1073741824;
        int largestChildWidth = RtlSpacingHelper.UNDEFINED;
        int usedExcessSpace = SHOW_DIVIDER_NONE;
        int i2 = SHOW_DIVIDER_NONE;
        while (i2 < count) {
            LayoutParams lp;
            int totalLength;
            int childWidth;
            boolean matchHeightLocally;
            int margin;
            View child = getVirtualChildAt(i2);
            if (child == null) {
                this.mTotalLength += measureNullChild(i2);
            } else if (child.getVisibility() == 8) {
                i2 += getChildrenSkipCount(child, i2);
            } else {
                if (hasDividerBeforeChildAt(i2)) {
                    this.mTotalLength += this.mDividerWidth;
                }
                lp = (LayoutParams) child.getLayoutParams();
                totalWeight += lp.weight;
                boolean useExcessSpace = lp.width == 0 && lp.weight > 0.0f;
                if (widthMode == 1073741824 && useExcessSpace) {
                    if (isExactly) {
                        this.mTotalLength += lp.leftMargin + lp.rightMargin;
                    } else {
                        totalLength = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength, (lp.leftMargin + totalLength) + lp.rightMargin);
                    }
                    if (baselineAligned) {
                        child.measure(MeasureSpec.makeSafeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), SHOW_DIVIDER_NONE), MeasureSpec.makeSafeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), SHOW_DIVIDER_NONE));
                    } else {
                        skippedMeasure = true;
                    }
                } else {
                    if (useExcessSpace) {
                        lp.width = -2;
                    }
                    measureChildBeforeLayout(child, i2, widthMeasureSpec, totalWeight == 0.0f ? this.mTotalLength : SHOW_DIVIDER_NONE, heightMeasureSpec, SHOW_DIVIDER_NONE);
                    childWidth = child.getMeasuredWidth();
                    if (useExcessSpace) {
                        lp.width = SHOW_DIVIDER_NONE;
                        usedExcessSpace += childWidth;
                    }
                    if (isExactly) {
                        this.mTotalLength += ((lp.leftMargin + childWidth) + lp.rightMargin) + getNextLocationOffset(child);
                    } else {
                        totalLength = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength, (((totalLength + childWidth) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                    }
                    if (useLargestChild) {
                        largestChildWidth = Math.max(childWidth, largestChildWidth);
                    }
                }
                matchHeightLocally = false;
                if (heightMode != 1073741824 && lp.height == -1) {
                    matchHeight = true;
                    matchHeightLocally = true;
                }
                margin = lp.topMargin + lp.bottomMargin;
                childHeight = child.getMeasuredHeight() + margin;
                childState = View.combineMeasuredStates(childState, child.getMeasuredState());
                if (baselineAligned) {
                    childBaseline = child.getBaseline();
                    if (childBaseline != -1) {
                        if (lp.gravity < 0) {
                            i = this.mGravity;
                        } else {
                            i = lp.gravity;
                        }
                        int index = (((i & LogPower.APP_PROCESS_EXIT) >> VERTICAL_GRAVITY_COUNT) & -2) >> VERTICAL;
                        maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                        maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                    }
                }
                maxHeight = Math.max(maxHeight, childHeight);
                allFillParent = allFillParent && lp.height == -1;
                if (lp.weight > 0.0f) {
                    if (!matchHeightLocally) {
                        margin = childHeight;
                    }
                    weightedMaxHeight = Math.max(weightedMaxHeight, margin);
                } else {
                    if (!matchHeightLocally) {
                        margin = childHeight;
                    }
                    alternativeMaxHeight = Math.max(alternativeMaxHeight, margin);
                }
                i2 += getChildrenSkipCount(child, i2);
            }
            i2 += VERTICAL;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(count)) {
            this.mTotalLength += this.mDividerWidth;
        }
        if (maxAscent[VERTICAL] == -1 && maxAscent[SHOW_DIVIDER_NONE] == -1 && maxAscent[SHOW_DIVIDER_MIDDLE] == -1) {
            if (maxAscent[INDEX_FILL] != -1) {
            }
            if (useLargestChild && (widthMode == Integer.MIN_VALUE || widthMode == 0)) {
                this.mTotalLength = SHOW_DIVIDER_NONE;
                i2 = SHOW_DIVIDER_NONE;
                while (i2 < count) {
                    child = getVirtualChildAt(i2);
                    if (child == null) {
                        this.mTotalLength += measureNullChild(i2);
                    } else if (child.getVisibility() != 8) {
                        i2 += getChildrenSkipCount(child, i2);
                    } else {
                        if (this.mHwActionBarTabLayoutUsed && hasDividerBeforeChildAt(i2)) {
                            this.mTotalLength += this.mDividerWidth;
                        }
                        lp = (LayoutParams) child.getLayoutParams();
                        if (isExactly) {
                            totalLength = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength, (((totalLength + largestChildWidth) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                        } else {
                            this.mTotalLength += ((lp.leftMargin + largestChildWidth) + lp.rightMargin) + getNextLocationOffset(child);
                        }
                    }
                    i2 += VERTICAL;
                }
            }
            this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
            widthSizeAndState = View.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, SHOW_DIVIDER_NONE);
            i = (widthSizeAndState & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT) - this.mTotalLength;
            if (this.mAllowInconsistentMeasurement) {
                usedExcessSpace = SHOW_DIVIDER_NONE;
            }
            remainingExcess = i + usedExcessSpace;
            if (!skippedMeasure || (remainingExcess != 0 && totalWeight > 0.0f)) {
                remainingWeightSum = this.mWeightSum <= 0.0f ? this.mWeightSum : totalWeight;
                maxAscent[INDEX_FILL] = -1;
                maxAscent[SHOW_DIVIDER_MIDDLE] = -1;
                maxAscent[VERTICAL] = -1;
                maxAscent[SHOW_DIVIDER_NONE] = -1;
                maxDescent[INDEX_FILL] = -1;
                maxDescent[SHOW_DIVIDER_MIDDLE] = -1;
                maxDescent[VERTICAL] = -1;
                maxDescent[SHOW_DIVIDER_NONE] = -1;
                maxHeight = -1;
                this.mTotalLength = SHOW_DIVIDER_NONE;
                for (i2 = SHOW_DIVIDER_NONE; i2 < count; i2 += VERTICAL) {
                    child = getVirtualChildAt(i2);
                    if (!(child == null || child.getVisibility() == 8)) {
                        lp = (LayoutParams) child.getLayoutParams();
                        childWeight = lp.weight;
                        if (childWeight > 0.0f) {
                            share = (int) ((((float) remainingExcess) * childWeight) / remainingWeightSum);
                            remainingExcess -= share;
                            remainingWeightSum -= childWeight;
                            if (!this.mUseLargestChild && widthMode != 1073741824) {
                                childWidth = largestChildWidth;
                            } else if (lp.width == 0 || (this.mAllowInconsistentMeasurement && widthMode != 1073741824)) {
                                childWidth = child.getMeasuredWidth() + share;
                            } else {
                                childWidth = share;
                            }
                            child.measure(MeasureSpec.makeMeasureSpec(Math.max(SHOW_DIVIDER_NONE, childWidth), EditorInfo.IME_FLAG_NO_ENTER_ACTION), ViewGroup.getChildMeasureSpec(heightMeasureSpec, ((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin, lp.height));
                            childState = View.combineMeasuredStates(childState, child.getMeasuredState() & View.MEASURED_STATE_MASK);
                        }
                        if (isExactly) {
                            totalLength = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength, (((child.getMeasuredWidth() + totalLength) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                        } else {
                            this.mTotalLength += ((child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child);
                        }
                        matchHeightLocally = heightMode == 1073741824 ? lp.height != -1 : false;
                        margin = lp.topMargin + lp.bottomMargin;
                        childHeight = child.getMeasuredHeight() + margin;
                        maxHeight = Math.max(maxHeight, childHeight);
                        if (!matchHeightLocally) {
                            margin = childHeight;
                        }
                        alternativeMaxHeight = Math.max(alternativeMaxHeight, margin);
                        allFillParent = allFillParent && lp.height == -1;
                        if (baselineAligned) {
                            childBaseline = child.getBaseline();
                            if (childBaseline == -1) {
                                if (lp.gravity >= 0) {
                                    i = this.mGravity;
                                } else {
                                    i = lp.gravity;
                                }
                                index = (((i & LogPower.APP_PROCESS_EXIT) >> VERTICAL_GRAVITY_COUNT) & -2) >> VERTICAL;
                                maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                                maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                            }
                        }
                    }
                }
                this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
                if (maxAscent[VERTICAL] == -1 && maxAscent[SHOW_DIVIDER_NONE] == -1 && maxAscent[SHOW_DIVIDER_MIDDLE] == -1) {
                    if (maxAscent[INDEX_FILL] != -1) {
                    }
                }
                maxHeight = Math.max(maxHeight, Math.max(maxAscent[INDEX_FILL], Math.max(maxAscent[SHOW_DIVIDER_NONE], Math.max(maxAscent[VERTICAL], maxAscent[SHOW_DIVIDER_MIDDLE]))) + Math.max(maxDescent[INDEX_FILL], Math.max(maxDescent[SHOW_DIVIDER_NONE], Math.max(maxDescent[VERTICAL], maxDescent[SHOW_DIVIDER_MIDDLE]))));
            } else {
                alternativeMaxHeight = Math.max(alternativeMaxHeight, weightedMaxHeight);
                if (useLargestChild && widthMode != 1073741824) {
                    for (i2 = SHOW_DIVIDER_NONE; i2 < count; i2 += VERTICAL) {
                        child = getVirtualChildAt(i2);
                        if (!(child == null || child.getVisibility() == 8)) {
                            if (((LayoutParams) child.getLayoutParams()).weight > 0.0f) {
                                child.measure(MeasureSpec.makeMeasureSpec(largestChildWidth, EditorInfo.IME_FLAG_NO_ENTER_ACTION), MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), EditorInfo.IME_FLAG_NO_ENTER_ACTION));
                            }
                        }
                    }
                }
            }
            if (!(allFillParent || heightMode == 1073741824)) {
                maxHeight = alternativeMaxHeight;
            }
            setMeasuredDimension((View.MEASURED_STATE_MASK & childState) | widthSizeAndState, View.resolveSizeAndState(Math.max(maxHeight + (this.mPaddingTop + this.mPaddingBottom), getSuggestedMinimumHeight()), heightMeasureSpec, childState << 16));
            if (matchHeight) {
                forceUniformHeight(count, widthMeasureSpec);
            }
        }
        maxHeight = Math.max(maxHeight, Math.max(maxAscent[INDEX_FILL], Math.max(maxAscent[SHOW_DIVIDER_NONE], Math.max(maxAscent[VERTICAL], maxAscent[SHOW_DIVIDER_MIDDLE]))) + Math.max(maxDescent[INDEX_FILL], Math.max(maxDescent[SHOW_DIVIDER_NONE], Math.max(maxDescent[VERTICAL], maxDescent[SHOW_DIVIDER_MIDDLE]))));
        this.mTotalLength = SHOW_DIVIDER_NONE;
        i2 = SHOW_DIVIDER_NONE;
        while (i2 < count) {
            child = getVirtualChildAt(i2);
            if (child == null) {
                this.mTotalLength += measureNullChild(i2);
            } else if (child.getVisibility() != 8) {
                this.mTotalLength += this.mDividerWidth;
                lp = (LayoutParams) child.getLayoutParams();
                if (isExactly) {
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((totalLength + largestChildWidth) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                } else {
                    this.mTotalLength += ((lp.leftMargin + largestChildWidth) + lp.rightMargin) + getNextLocationOffset(child);
                }
            } else {
                i2 += getChildrenSkipCount(child, i2);
            }
            i2 += VERTICAL;
        }
        this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
        widthSizeAndState = View.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, SHOW_DIVIDER_NONE);
        i = (widthSizeAndState & AsyncService.CMD_ASYNC_SERVICE_ON_START_INTENT) - this.mTotalLength;
        if (this.mAllowInconsistentMeasurement) {
            usedExcessSpace = SHOW_DIVIDER_NONE;
        }
        remainingExcess = i + usedExcessSpace;
        if (skippedMeasure) {
        }
        if (this.mWeightSum <= 0.0f) {
        }
        maxAscent[INDEX_FILL] = -1;
        maxAscent[SHOW_DIVIDER_MIDDLE] = -1;
        maxAscent[VERTICAL] = -1;
        maxAscent[SHOW_DIVIDER_NONE] = -1;
        maxDescent[INDEX_FILL] = -1;
        maxDescent[SHOW_DIVIDER_MIDDLE] = -1;
        maxDescent[VERTICAL] = -1;
        maxDescent[SHOW_DIVIDER_NONE] = -1;
        maxHeight = -1;
        this.mTotalLength = SHOW_DIVIDER_NONE;
        for (i2 = SHOW_DIVIDER_NONE; i2 < count; i2 += VERTICAL) {
            child = getVirtualChildAt(i2);
            lp = (LayoutParams) child.getLayoutParams();
            childWeight = lp.weight;
            if (childWeight > 0.0f) {
                share = (int) ((((float) remainingExcess) * childWeight) / remainingWeightSum);
                remainingExcess -= share;
                remainingWeightSum -= childWeight;
                if (!this.mUseLargestChild) {
                }
                if (lp.width == 0) {
                }
                childWidth = child.getMeasuredWidth() + share;
                child.measure(MeasureSpec.makeMeasureSpec(Math.max(SHOW_DIVIDER_NONE, childWidth), EditorInfo.IME_FLAG_NO_ENTER_ACTION), ViewGroup.getChildMeasureSpec(heightMeasureSpec, ((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin, lp.height));
                childState = View.combineMeasuredStates(childState, child.getMeasuredState() & View.MEASURED_STATE_MASK);
            }
            if (isExactly) {
                totalLength = this.mTotalLength;
                this.mTotalLength = Math.max(totalLength, (((child.getMeasuredWidth() + totalLength) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
            } else {
                this.mTotalLength += ((child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child);
            }
            if (heightMode == 1073741824) {
            }
            margin = lp.topMargin + lp.bottomMargin;
            childHeight = child.getMeasuredHeight() + margin;
            maxHeight = Math.max(maxHeight, childHeight);
            if (matchHeightLocally) {
                margin = childHeight;
            }
            alternativeMaxHeight = Math.max(alternativeMaxHeight, margin);
            if (!allFillParent) {
            }
            if (baselineAligned) {
                childBaseline = child.getBaseline();
                if (childBaseline == -1) {
                    if (lp.gravity >= 0) {
                        i = lp.gravity;
                    } else {
                        i = this.mGravity;
                    }
                    index = (((i & LogPower.APP_PROCESS_EXIT) >> VERTICAL_GRAVITY_COUNT) & -2) >> VERTICAL;
                    maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                    maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                }
            }
        }
        this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
        if (maxAscent[INDEX_FILL] != -1) {
            maxHeight = Math.max(maxHeight, Math.max(maxAscent[INDEX_FILL], Math.max(maxAscent[SHOW_DIVIDER_NONE], Math.max(maxAscent[VERTICAL], maxAscent[SHOW_DIVIDER_MIDDLE]))) + Math.max(maxDescent[INDEX_FILL], Math.max(maxDescent[SHOW_DIVIDER_NONE], Math.max(maxDescent[VERTICAL], maxDescent[SHOW_DIVIDER_MIDDLE]))));
        }
        maxHeight = alternativeMaxHeight;
        setMeasuredDimension((View.MEASURED_STATE_MASK & childState) | widthSizeAndState, View.resolveSizeAndState(Math.max(maxHeight + (this.mPaddingTop + this.mPaddingBottom), getSuggestedMinimumHeight()), heightMeasureSpec, childState << 16));
        if (matchHeight) {
            forceUniformHeight(count, widthMeasureSpec);
        }
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        for (int i = SHOW_DIVIDER_NONE; i < count; i += VERTICAL) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.height == -1) {
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, SHOW_DIVIDER_NONE, uniformMeasureSpec, SHOW_DIVIDER_NONE);
                    lp.width = oldWidth;
                }
            }
        }
    }

    int getChildrenSkipCount(View child, int index) {
        return SHOW_DIVIDER_NONE;
    }

    int measureNullChild(int childIndex) {
        return SHOW_DIVIDER_NONE;
    }

    void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    int getLocationOffset(View child) {
        return SHOW_DIVIDER_NONE;
    }

    int getNextLocationOffset(View child) {
        return SHOW_DIVIDER_NONE;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mOrientation == VERTICAL) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    void layoutVertical(int left, int top, int right, int bottom) {
        int childTop;
        int paddingLeft = this.mPaddingLeft;
        int width = right - left;
        int childRight = width - this.mPaddingRight;
        int childSpace = (width - paddingLeft) - this.mPaddingRight;
        int count = getVirtualChildCount();
        int minorGravity = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        switch (this.mGravity & LogPower.APP_PROCESS_EXIT) {
            case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                childTop = this.mPaddingTop + (((bottom - top) - this.mTotalLength) / SHOW_DIVIDER_MIDDLE);
                break;
            case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                childTop = ((this.mPaddingTop + bottom) - top) - this.mTotalLength;
                break;
            default:
                childTop = this.mPaddingTop;
                break;
        }
        int i = SHOW_DIVIDER_NONE;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (child == null) {
                childTop += measureNullChild(i);
            } else if (child.getVisibility() != 8) {
                int childLeft;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int gravity = lp.gravity;
                if (gravity < 0) {
                    gravity = minorGravity;
                }
                switch (Gravity.getAbsoluteGravity(gravity, getLayoutDirection()) & 7) {
                    case VERTICAL /*1*/:
                        childLeft = ((((childSpace - childWidth) / SHOW_DIVIDER_MIDDLE) + paddingLeft) + lp.leftMargin) - lp.rightMargin;
                        break;
                    case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                        childLeft = (childRight - childWidth) - lp.rightMargin;
                        break;
                    default:
                        childLeft = paddingLeft + lp.leftMargin;
                        break;
                }
                if (hasDividerBeforeChildAt(i)) {
                    childTop += this.mDividerHeight;
                }
                childTop += lp.topMargin;
                setChildFrame(child, childLeft, childTop + getLocationOffset(child), childWidth, childHeight);
                childTop += (lp.bottomMargin + childHeight) + getNextLocationOffset(child);
                i += getChildrenSkipCount(child, i);
            }
            i += VERTICAL;
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (layoutDirection != this.mLayoutDirection) {
            this.mLayoutDirection = layoutDirection;
            if (this.mOrientation == 0) {
                requestLayout();
            }
        }
    }

    void layoutHorizontal(int left, int top, int right, int bottom) {
        int childLeft;
        boolean isLayoutRtl = isLayoutRtl();
        int paddingTop = this.mPaddingTop;
        int height = bottom - top;
        int childBottom = height - this.mPaddingBottom;
        int childSpace = (height - paddingTop) - this.mPaddingBottom;
        int count = getVirtualChildCount();
        int majorGravity = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int minorGravity = this.mGravity & LogPower.APP_PROCESS_EXIT;
        boolean baselineAligned = this.mBaselineAligned;
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
        switch (Gravity.getAbsoluteGravity(majorGravity, getLayoutDirection())) {
            case VERTICAL /*1*/:
                childLeft = this.mPaddingLeft + (((right - left) - this.mTotalLength) / SHOW_DIVIDER_MIDDLE);
                break;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                childLeft = ((this.mPaddingLeft + right) - left) - this.mTotalLength;
                break;
            default:
                childLeft = this.mPaddingLeft;
                break;
        }
        int start = SHOW_DIVIDER_NONE;
        int dir = VERTICAL;
        if (isLayoutRtl) {
            start = count - 1;
            dir = -1;
        }
        int i = SHOW_DIVIDER_NONE;
        while (i < count) {
            int childIndex = start + (dir * i);
            View child = getVirtualChildAt(childIndex);
            if (child == null) {
                childLeft += measureNullChild(childIndex);
            } else if (child.getVisibility() != 8) {
                int childTop;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int childBaseline = -1;
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (baselineAligned && lp.height != -1) {
                    childBaseline = child.getBaseline();
                }
                int gravity = lp.gravity;
                if (gravity < 0) {
                    gravity = minorGravity;
                }
                switch (gravity & LogPower.APP_PROCESS_EXIT) {
                    case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                        childTop = ((((childSpace - childHeight) / SHOW_DIVIDER_MIDDLE) + paddingTop) + lp.topMargin) - lp.bottomMargin;
                        break;
                    case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                        childTop = paddingTop + lp.topMargin;
                        if (childBaseline != -1) {
                            childTop += maxAscent[VERTICAL] - childBaseline;
                            break;
                        }
                        break;
                    case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                        childTop = (childBottom - childHeight) - lp.bottomMargin;
                        if (childBaseline != -1) {
                            childTop -= maxDescent[SHOW_DIVIDER_MIDDLE] - (child.getMeasuredHeight() - childBaseline);
                            break;
                        }
                        break;
                    default:
                        childTop = paddingTop;
                        break;
                }
                if (hasDividerBeforeChildAt(childIndex)) {
                    childLeft += this.mDividerWidth;
                }
                childLeft += lp.leftMargin;
                setChildFrame(child, childLeft + getLocationOffset(child), childTop, childWidth, childHeight);
                childLeft += (lp.rightMargin + childWidth) + getNextLocationOffset(child);
                i += getChildrenSkipCount(child, childIndex);
            }
            i += VERTICAL;
        }
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }

    public void setOrientation(int orientation) {
        if (this.mOrientation != orientation) {
            this.mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    @RemotableViewMethod
    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK & gravity) == 0) {
                gravity |= Gravity.START;
            }
            if ((gravity & LogPower.APP_PROCESS_EXIT) == 0) {
                gravity |= 48;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    public int getGravity() {
        return this.mGravity;
    }

    @RemotableViewMethod
    public void setHorizontalGravity(int horizontalGravity) {
        int gravity = horizontalGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if ((this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) != gravity) {
            this.mGravity = (this.mGravity & -8388616) | gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setVerticalGravity(int verticalGravity) {
        int gravity = verticalGravity & LogPower.APP_PROCESS_EXIT;
        if ((this.mGravity & LogPower.APP_PROCESS_EXIT) != gravity) {
            this.mGravity = (this.mGravity & -113) | gravity;
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation == VERTICAL) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public CharSequence getAccessibilityClassName() {
        return LinearLayout.class.getName();
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("layout:baselineAligned", this.mBaselineAligned);
        encoder.addProperty("layout:baselineAlignedChildIndex", this.mBaselineAlignedChildIndex);
        encoder.addProperty("measurement:baselineChildTop", this.mBaselineChildTop);
        encoder.addProperty("measurement:orientation", this.mOrientation);
        encoder.addProperty("measurement:gravity", this.mGravity);
        encoder.addProperty("measurement:totalLength", this.mTotalLength);
        encoder.addProperty("layout:totalLength", this.mTotalLength);
        encoder.addProperty("layout:useLargestChild", this.mUseLargestChild);
    }
}
