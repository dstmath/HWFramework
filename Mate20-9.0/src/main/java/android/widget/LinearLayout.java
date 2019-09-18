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
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@RemoteViews.RemoteView
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
    private static boolean sCompatibilityDone = false;
    private static boolean sRemeasureWeightedChildren = true;
    private final boolean mAllowInconsistentMeasurement;
    @ViewDebug.ExportedProperty(category = "layout")
    private boolean mBaselineAligned;
    @ViewDebug.ExportedProperty(category = "layout")
    private int mBaselineAlignedChildIndex;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    @ViewDebug.ExportedProperty(category = "measurement", flagMapping = {@ViewDebug.FlagToString(equals = -1, mask = -1, name = "NONE"), @ViewDebug.FlagToString(equals = 0, mask = 0, name = "NONE"), @ViewDebug.FlagToString(equals = 48, mask = 48, name = "TOP"), @ViewDebug.FlagToString(equals = 80, mask = 80, name = "BOTTOM"), @ViewDebug.FlagToString(equals = 3, mask = 3, name = "LEFT"), @ViewDebug.FlagToString(equals = 5, mask = 5, name = "RIGHT"), @ViewDebug.FlagToString(equals = 8388611, mask = 8388611, name = "START"), @ViewDebug.FlagToString(equals = 8388613, mask = 8388613, name = "END"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "CENTER_VERTICAL"), @ViewDebug.FlagToString(equals = 112, mask = 112, name = "FILL_VERTICAL"), @ViewDebug.FlagToString(equals = 1, mask = 1, name = "CENTER_HORIZONTAL"), @ViewDebug.FlagToString(equals = 7, mask = 7, name = "FILL_HORIZONTAL"), @ViewDebug.FlagToString(equals = 17, mask = 17, name = "CENTER"), @ViewDebug.FlagToString(equals = 119, mask = 119, name = "FILL"), @ViewDebug.FlagToString(equals = 8388608, mask = 8388608, name = "RELATIVE")}, formatToHexString = true)
    private int mGravity;
    public boolean mHwActionBarTabLayoutUsed;
    public boolean mHwActionBarViewUsed;
    private int mLayoutDirection;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mOrientation;
    private int mShowDividers;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mTotalLength;
    @ViewDebug.ExportedProperty(category = "layout")
    private boolean mUseLargestChild;
    @ViewDebug.ExportedProperty(category = "layout")
    private float mWeightSum;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerMode {
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = -1, to = "NONE"), @ViewDebug.IntToString(from = 0, to = "NONE"), @ViewDebug.IntToString(from = 48, to = "TOP"), @ViewDebug.IntToString(from = 80, to = "BOTTOM"), @ViewDebug.IntToString(from = 3, to = "LEFT"), @ViewDebug.IntToString(from = 5, to = "RIGHT"), @ViewDebug.IntToString(from = 8388611, to = "START"), @ViewDebug.IntToString(from = 8388613, to = "END"), @ViewDebug.IntToString(from = 16, to = "CENTER_VERTICAL"), @ViewDebug.IntToString(from = 112, to = "FILL_VERTICAL"), @ViewDebug.IntToString(from = 1, to = "CENTER_HORIZONTAL"), @ViewDebug.IntToString(from = 7, to = "FILL_HORIZONTAL"), @ViewDebug.IntToString(from = 17, to = "CENTER"), @ViewDebug.IntToString(from = 119, to = "FILL")})
        public int gravity;
        @ViewDebug.ExportedProperty(category = "layout")
        public float weight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = -1;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.LinearLayout_Layout);
            this.weight = a.getFloat(3, 0.0f);
            this.gravity = a.getInt(0, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(int width, int height, float weight2) {
            super(width, height);
            this.gravity = -1;
            this.weight = weight2;
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
            this.gravity = -1;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
            this.gravity = -1;
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.MarginLayoutParams) source);
            this.gravity = -1;
            this.weight = source.weight;
            this.gravity = source.gravity;
        }

        public String debug(String output) {
            return output + "LinearLayout.LayoutParams={width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " weight=" + this.weight + "}";
        }

        /* access modifiers changed from: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:weight", this.weight);
            encoder.addProperty("layout:gravity", this.gravity);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public LinearLayout(Context context) {
        this(context, null);
    }

    public LinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        boolean z = true;
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        this.mLayoutDirection = -1;
        if (!sCompatibilityDone && context != null) {
            sRemeasureWeightedChildren = context.getApplicationInfo().targetSdkVersion >= 28;
            sCompatibilityDone = true;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinearLayout, defStyleAttr, defStyleRes);
        int index = a.getInt(1, -1);
        if (index >= 0) {
            setOrientation(index);
        }
        int index2 = a.getInt(0, -1);
        if (index2 >= 0) {
            setGravity(index2);
        }
        boolean baselineAligned = a.getBoolean(2, true);
        if (!baselineAligned) {
            setBaselineAligned(baselineAligned);
        }
        this.mWeightSum = a.getFloat(4, -1.0f);
        this.mBaselineAlignedChildIndex = a.getInt(3, -1);
        this.mUseLargestChild = a.getBoolean(6, false);
        this.mShowDividers = a.getInt(7, 0);
        this.mDividerPadding = a.getDimensionPixelSize(8, 0);
        setDividerDrawable(a.getDrawable(5));
        this.mAllowInconsistentMeasurement = context.getApplicationInfo().targetSdkVersion > 23 ? false : z;
        a.recycle();
    }

    private boolean isShowingDividers() {
        return (this.mShowDividers == 0 || this.mDivider == null) ? false : true;
    }

    public void setShowDividers(int showDividers) {
        if (showDividers != this.mShowDividers) {
            this.mShowDividers = showDividers;
            setWillNotDraw(!isShowingDividers());
            requestLayout();
        }
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
        if (divider != this.mDivider) {
            this.mDivider = divider;
            if (divider != null) {
                this.mDividerWidth = divider.getIntrinsicWidth();
                this.mDividerHeight = divider.getIntrinsicHeight();
            } else {
                this.mDividerWidth = 0;
                this.mDividerHeight = 0;
            }
            setWillNotDraw(!isShowingDividers());
            requestLayout();
        }
    }

    public void setDividerPadding(int padding) {
        if (padding != this.mDividerPadding) {
            this.mDividerPadding = padding;
            if (isShowingDividers()) {
                requestLayout();
                invalidate();
            }
        }
    }

    public int getDividerPadding() {
        return this.mDividerPadding;
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation == 1) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void drawDividersVertical(Canvas canvas) {
        int bottom;
        int count = getVirtualChildCount();
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                drawHorizontalDivider(canvas, (child.getTop() - ((LayoutParams) child.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
        }
        if (hasDividerBeforeChildAt(count) != 0) {
            View child2 = getLastNonGoneChild();
            if (child2 == null) {
                bottom = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                bottom = child2.getBottom() + ((LayoutParams) child2.getLayoutParams()).bottomMargin;
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

    /* access modifiers changed from: package-private */
    public void drawDividersHorizontal(Canvas canvas) {
        int position;
        int position2;
        int count = getVirtualChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!isLayoutRtl) {
                    position2 = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                } else if (i == 0) {
                    position2 = (child.getRight() + lp.rightMargin) - this.mDividerWidth;
                } else {
                    position2 = child.getRight() + lp.rightMargin;
                }
                drawVerticalDivider(canvas, position2);
            }
        }
        if (hasDividerBeforeChildAt(count) != 0) {
            View child2 = getLastNonGoneChild();
            if (child2 != null) {
                LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                if (isLayoutRtl) {
                    position = (child2.getLeft() - lp2.leftMargin) - this.mDividerWidth;
                } else {
                    position = child2.getRight() + lp2.rightMargin;
                }
            } else if (isLayoutRtl) {
                position = getPaddingLeft();
            } else {
                position = (getWidth() - getPaddingRight()) - this.mDividerWidth;
            }
            drawVerticalDivider(canvas, position);
        }
    }

    /* access modifiers changed from: package-private */
    public void drawHorizontalDivider(Canvas canvas, int top) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, top, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + top);
        this.mDivider.draw(canvas);
    }

    /* access modifiers changed from: package-private */
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
        if (getChildCount() > this.mBaselineAlignedChildIndex) {
            View child = getChildAt(this.mBaselineAlignedChildIndex);
            int childBaseline = child.getBaseline();
            if (childBaseline != -1) {
                int childTop = this.mBaselineChildTop;
                if (this.mOrientation == 1) {
                    int majorGravity = this.mGravity & 112;
                    if (majorGravity != 48) {
                        if (majorGravity == 16) {
                            childTop += ((((this.mBottom - this.mTop) - this.mPaddingTop) - this.mPaddingBottom) - this.mTotalLength) / 2;
                        } else if (majorGravity == 80) {
                            childTop = ((this.mBottom - this.mTop) - this.mPaddingBottom) - this.mTotalLength;
                        }
                    }
                }
                return ((LayoutParams) child.getLayoutParams()).topMargin + childTop + childBaseline;
            } else if (this.mBaselineAlignedChildIndex == 0) {
                return -1;
            } else {
                throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
            }
        } else {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
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

    /* access modifiers changed from: package-private */
    public View getVirtualChildAt(int index) {
        return getChildAt(index);
    }

    /* access modifiers changed from: package-private */
    public int getVirtualChildCount() {
        return getChildCount();
    }

    public float getWeightSum() {
        return this.mWeightSum;
    }

    @RemotableViewMethod
    public void setWeightSum(float weightSum) {
        this.mWeightSum = Math.max(0.0f, weightSum);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == 1) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasDividerBeforeChildAt(int childIndex) {
        boolean z = false;
        if (childIndex == getVirtualChildCount()) {
            if ((this.mShowDividers & 4) != 0) {
                z = true;
            }
            return z;
        } else if (allViewsAreGoneBefore(childIndex)) {
            if ((this.mShowDividers & 1) != 0) {
                z = true;
            }
            return z;
        } else {
            if ((this.mShowDividers & 2) != 0) {
                z = true;
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x03e4  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x03e7  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x03ee  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x03f8  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x046e  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0474  */
    public void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int count;
        int childState;
        int heightMode;
        int count2;
        int heightMode2;
        int alternativeMaxWidth;
        int childState2;
        float totalWeight;
        int remainingExcess;
        int heightMode3;
        int count3;
        int baselineChildIndex;
        boolean useLargestChild;
        int remainingExcess2;
        int margin;
        boolean matchWidthLocally;
        boolean allFillParent;
        int childHeight;
        float totalWeight2;
        int weightedMaxWidth;
        int remainingExcess3;
        int i;
        int childState3;
        int count4;
        int heightMode4;
        int childState4;
        int alternativeMaxWidth2;
        int weightedMaxWidth2;
        boolean skippedMeasure;
        int i2;
        int maxWidth;
        int heightMode5;
        int count5;
        View child;
        LayoutParams lp;
        int i3;
        int i4;
        int childState5;
        boolean allFillParent2;
        int childState6;
        int weightedMaxWidth3;
        int i5 = widthMeasureSpec;
        int childState7 = heightMeasureSpec;
        this.mTotalLength = 0;
        float totalWeight3 = 0.0f;
        int count6 = getVirtualChildCount();
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode6 = View.MeasureSpec.getMode(heightMeasureSpec);
        boolean skippedMeasure2 = false;
        int baselineChildIndex2 = this.mBaselineAlignedChildIndex;
        boolean useLargestChild2 = this.mUseLargestChild;
        int consumedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        boolean matchWidth = false;
        int maxWidth2 = 0;
        int maxWidth3 = 0;
        int i6 = 0;
        int alternativeMaxWidth3 = 0;
        int weightedMaxWidth4 = 0;
        boolean allFillParent3 = true;
        int weightedMaxWidth5 = Integer.MIN_VALUE;
        while (true) {
            int largestChildHeight = weightedMaxWidth5;
            if (i6 < count6) {
                View child2 = getVirtualChildAt(i6);
                if (child2 == null) {
                    this.mTotalLength += measureNullChild(i6);
                    count4 = count6;
                    heightMode4 = heightMode6;
                    weightedMaxWidth5 = largestChildHeight;
                    maxWidth3 = maxWidth3;
                } else {
                    int maxWidth4 = maxWidth3;
                    int weightedMaxWidth6 = weightedMaxWidth4;
                    if (child2.getVisibility() == 8) {
                        i6 += getChildrenSkipCount(child2, i6);
                        count4 = count6;
                        heightMode4 = heightMode6;
                        weightedMaxWidth5 = largestChildHeight;
                        maxWidth3 = maxWidth4;
                        weightedMaxWidth4 = weightedMaxWidth6;
                    } else {
                        nonSkippedChildCount++;
                        if (hasDividerBeforeChildAt(i6)) {
                            this.mTotalLength += this.mDividerHeight;
                        }
                        LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                        float totalWeight4 = totalWeight3 + lp2.weight;
                        boolean useExcessSpace = lp2.height == 0 && lp2.weight > 0.0f;
                        if (heightMode6 != 1073741824 || !useExcessSpace) {
                            int i7 = i6;
                            if (useExcessSpace) {
                                lp2.height = -2;
                            }
                            i2 = i7;
                            skippedMeasure = skippedMeasure2;
                            maxWidth = maxWidth4;
                            LayoutParams lp3 = lp2;
                            weightedMaxWidth2 = weightedMaxWidth6;
                            heightMode4 = heightMode6;
                            int heightMode7 = alternativeMaxWidth3;
                            int alternativeMaxWidth4 = i5;
                            child = child2;
                            count4 = count6;
                            alternativeMaxWidth2 = heightMode7;
                            count5 = largestChildHeight;
                            heightMode5 = 1073741824;
                            childState4 = maxWidth2;
                            measureChildBeforeLayout(child2, i2, alternativeMaxWidth4, 0, childState7, totalWeight4 == 0.0f ? this.mTotalLength : 0);
                            int childHeight2 = child.getMeasuredHeight();
                            if (useExcessSpace) {
                                lp = lp3;
                                lp.height = 0;
                                consumedExcessSpace += childHeight2;
                            } else {
                                lp = lp3;
                            }
                            int totalLength = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength, totalLength + childHeight2 + lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
                            if (useLargestChild2) {
                                count5 = Math.max(childHeight2, count5);
                            }
                        } else {
                            int totalLength2 = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength2, lp2.topMargin + totalLength2 + lp2.bottomMargin);
                            lp = lp2;
                            alternativeMaxWidth2 = alternativeMaxWidth3;
                            child = child2;
                            childState4 = maxWidth2;
                            count4 = count6;
                            heightMode4 = heightMode6;
                            skippedMeasure = true;
                            count5 = largestChildHeight;
                            maxWidth = maxWidth4;
                            weightedMaxWidth2 = weightedMaxWidth6;
                            i2 = i6;
                            heightMode5 = 1073741824;
                        }
                        if (baselineChildIndex2 >= 0) {
                            i3 = i2;
                            if (baselineChildIndex2 == i3 + 1) {
                                this.mBaselineChildTop = this.mTotalLength;
                            }
                        } else {
                            i3 = i2;
                        }
                        if (i3 >= baselineChildIndex2 || lp.weight <= 0.0f) {
                            boolean matchWidthLocally2 = false;
                            if (widthMode != heightMode5) {
                                i4 = -1;
                                if (lp.width == -1) {
                                    matchWidth = true;
                                    matchWidthLocally2 = true;
                                }
                            } else {
                                i4 = -1;
                            }
                            int margin2 = lp.leftMargin + lp.rightMargin;
                            int measuredWidth = child.getMeasuredWidth() + margin2;
                            int maxWidth5 = Math.max(maxWidth, measuredWidth);
                            int childState8 = combineMeasuredStates(childState4, child.getMeasuredState());
                            boolean allFillParent4 = allFillParent3 && lp.width == i4;
                            if (lp.weight > 0.0f) {
                                allFillParent2 = allFillParent4;
                                weightedMaxWidth3 = Math.max(weightedMaxWidth2, matchWidthLocally2 ? margin2 : measuredWidth);
                                childState5 = childState8;
                                childState6 = alternativeMaxWidth2;
                            } else {
                                allFillParent2 = allFillParent4;
                                weightedMaxWidth3 = weightedMaxWidth2;
                                childState5 = childState8;
                                childState6 = Math.max(alternativeMaxWidth2, matchWidthLocally2 ? margin2 : measuredWidth);
                            }
                            i6 = i3 + getChildrenSkipCount(child, i3);
                            weightedMaxWidth4 = weightedMaxWidth3;
                            maxWidth3 = maxWidth5;
                            weightedMaxWidth5 = count5;
                            alternativeMaxWidth3 = childState6;
                            totalWeight3 = totalWeight4;
                            skippedMeasure2 = skippedMeasure;
                            allFillParent3 = allFillParent2;
                            maxWidth2 = childState5;
                        } else {
                            throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                        }
                    }
                }
                i6++;
                heightMode6 = heightMode4;
                count6 = count4;
                i5 = widthMeasureSpec;
            } else {
                int weightedMaxWidth7 = weightedMaxWidth4;
                int count7 = count6;
                int heightMode8 = heightMode6;
                boolean skippedMeasure3 = skippedMeasure2;
                int largestChildHeight2 = largestChildHeight;
                int maxWidth6 = maxWidth3;
                int heightMode9 = alternativeMaxWidth3;
                int childState9 = maxWidth2;
                if (nonSkippedChildCount > 0) {
                    count = count7;
                    if (hasDividerBeforeChildAt(count)) {
                        this.mTotalLength += this.mDividerHeight;
                    }
                } else {
                    count = count7;
                }
                if (useLargestChild2) {
                    heightMode = heightMode8;
                    if (heightMode == Integer.MIN_VALUE || heightMode == 0) {
                        this.mTotalLength = 0;
                        int i8 = 0;
                        while (i8 < count) {
                            View child3 = getVirtualChildAt(i8);
                            if (child3 == null) {
                                this.mTotalLength += measureNullChild(i8);
                                childState3 = childState9;
                            } else {
                                childState3 = childState9;
                                if (child3.getVisibility() == 8) {
                                    i8 += getChildrenSkipCount(child3, i8);
                                } else {
                                    LayoutParams lp4 = (LayoutParams) child3.getLayoutParams();
                                    int totalLength3 = this.mTotalLength;
                                    i = i8;
                                    this.mTotalLength = Math.max(totalLength3, totalLength3 + largestChildHeight2 + lp4.topMargin + lp4.bottomMargin + getNextLocationOffset(child3));
                                    i8 = i + 1;
                                    childState9 = childState3;
                                }
                            }
                            i = i8;
                            i8 = i + 1;
                            childState9 = childState3;
                        }
                        childState = childState9;
                    } else {
                        childState = childState9;
                    }
                } else {
                    childState = childState9;
                    heightMode = heightMode8;
                }
                this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
                int heightSizeAndState = resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), childState7, 0);
                int heightSize = heightSizeAndState & 16777215;
                int remainingExcess4 = (heightSize - this.mTotalLength) + (this.mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
                if (skippedMeasure3) {
                    remainingExcess = remainingExcess4;
                    int i9 = weightedMaxWidth7;
                    totalWeight = totalWeight3;
                } else if ((sRemeasureWeightedChildren || remainingExcess4 != 0) && totalWeight3 > 0.0f) {
                    int i10 = heightSize;
                    remainingExcess = remainingExcess4;
                    int i11 = weightedMaxWidth7;
                    totalWeight = totalWeight3;
                } else {
                    alternativeMaxWidth = Math.max(heightMode9, weightedMaxWidth7);
                    if (useLargestChild2 && heightMode != 1073741824) {
                        int i12 = 0;
                        while (true) {
                            int i13 = i12;
                            if (i13 >= count) {
                                break;
                            }
                            int heightSize2 = heightSize;
                            View child4 = getVirtualChildAt(i13);
                            if (child4 != null) {
                                remainingExcess3 = remainingExcess4;
                                weightedMaxWidth = weightedMaxWidth7;
                                if (child4.getVisibility() == 8) {
                                    totalWeight2 = totalWeight3;
                                } else {
                                    LayoutParams lp5 = (LayoutParams) child4.getLayoutParams();
                                    float childExtra = lp5.weight;
                                    if (childExtra > 0.0f) {
                                        LayoutParams layoutParams = lp5;
                                        float f = childExtra;
                                        totalWeight2 = totalWeight3;
                                        child4.measure(View.MeasureSpec.makeMeasureSpec(child4.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(largestChildHeight2, 1073741824));
                                    } else {
                                        totalWeight2 = totalWeight3;
                                    }
                                }
                            } else {
                                remainingExcess3 = remainingExcess4;
                                weightedMaxWidth = weightedMaxWidth7;
                                totalWeight2 = totalWeight3;
                            }
                            i12 = i13 + 1;
                            heightSize = heightSize2;
                            remainingExcess4 = remainingExcess3;
                            weightedMaxWidth7 = weightedMaxWidth;
                            totalWeight3 = totalWeight2;
                        }
                    }
                    int i14 = remainingExcess4;
                    int i15 = weightedMaxWidth7;
                    float f2 = totalWeight3;
                    count2 = count;
                    int i16 = heightMode;
                    boolean z = useLargestChild2;
                    int i17 = baselineChildIndex2;
                    childState2 = childState;
                    heightMode2 = widthMeasureSpec;
                    if (!allFillParent3 && widthMode != 1073741824) {
                        maxWidth6 = alternativeMaxWidth;
                    }
                    setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth6 + this.mPaddingLeft + this.mPaddingRight, getSuggestedMinimumWidth()), heightMode2, childState2), heightSizeAndState);
                    if (!matchWidth) {
                        forceUniformWidth(count2, childState7);
                        return;
                    } else {
                        int i18 = count2;
                        return;
                    }
                }
                float remainingWeightSum = this.mWeightSum > 0.0f ? this.mWeightSum : totalWeight;
                this.mTotalLength = 0;
                float remainingWeightSum2 = remainingWeightSum;
                alternativeMaxWidth = heightMode9;
                childState2 = childState;
                int remainingExcess5 = remainingExcess;
                int i19 = 0;
                while (i19 < count) {
                    View child5 = getVirtualChildAt(i19);
                    if (child5 != null) {
                        useLargestChild = useLargestChild2;
                        baselineChildIndex = baselineChildIndex2;
                        if (child5.getVisibility() == 8) {
                            count3 = count;
                            heightMode3 = heightMode;
                            int heightMode10 = widthMeasureSpec;
                        } else {
                            LayoutParams lp6 = (LayoutParams) child5.getLayoutParams();
                            float childWeight = lp6.weight;
                            if (childWeight > 0.0f) {
                                count3 = count;
                                int share = (int) ((((float) remainingExcess5) * childWeight) / remainingWeightSum2);
                                int remainingExcess6 = remainingExcess5 - share;
                                float remainingWeightSum3 = remainingWeightSum2 - childWeight;
                                if (this.mUseLargestChild && heightMode != 1073741824) {
                                    childHeight = largestChildHeight2;
                                } else if (lp6.height != 0 || (this.mAllowInconsistentMeasurement && heightMode != 1073741824)) {
                                    childHeight = child5.getMeasuredHeight() + share;
                                } else {
                                    childHeight = share;
                                }
                                int i20 = share;
                                remainingExcess2 = remainingExcess6;
                                int i21 = childHeight;
                                heightMode3 = heightMode;
                                child5.measure(getChildMeasureSpec(widthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp6.leftMargin + lp6.rightMargin, lp6.width), View.MeasureSpec.makeMeasureSpec(Math.max(0, childHeight), 1073741824));
                                childState2 = combineMeasuredStates(childState2, child5.getMeasuredState() & InputDevice.SOURCE_ANY);
                                remainingWeightSum2 = remainingWeightSum3;
                            } else {
                                count3 = count;
                                heightMode3 = heightMode;
                                int heightMode11 = widthMeasureSpec;
                                remainingExcess2 = remainingExcess5;
                            }
                            int margin3 = lp6.leftMargin + lp6.rightMargin;
                            int measuredWidth2 = child5.getMeasuredWidth() + margin3;
                            maxWidth6 = Math.max(maxWidth6, measuredWidth2);
                            float remainingWeightSum4 = remainingWeightSum2;
                            if (widthMode != 1073741824) {
                                margin = margin3;
                                if (lp6.width == -1) {
                                    matchWidthLocally = true;
                                    int alternativeMaxWidth5 = Math.max(alternativeMaxWidth, !matchWidthLocally ? margin : measuredWidth2);
                                    if (!allFillParent3) {
                                        boolean z2 = matchWidthLocally;
                                        if (lp6.width == -1) {
                                            allFillParent = true;
                                            int totalLength4 = this.mTotalLength;
                                            this.mTotalLength = Math.max(totalLength4, totalLength4 + child5.getMeasuredHeight() + lp6.topMargin + lp6.bottomMargin + getNextLocationOffset(child5));
                                            allFillParent3 = allFillParent;
                                            remainingExcess5 = remainingExcess2;
                                            remainingWeightSum2 = remainingWeightSum4;
                                            alternativeMaxWidth = alternativeMaxWidth5;
                                        }
                                    }
                                    allFillParent = false;
                                    int totalLength42 = this.mTotalLength;
                                    this.mTotalLength = Math.max(totalLength42, totalLength42 + child5.getMeasuredHeight() + lp6.topMargin + lp6.bottomMargin + getNextLocationOffset(child5));
                                    allFillParent3 = allFillParent;
                                    remainingExcess5 = remainingExcess2;
                                    remainingWeightSum2 = remainingWeightSum4;
                                    alternativeMaxWidth = alternativeMaxWidth5;
                                }
                            } else {
                                margin = margin3;
                            }
                            matchWidthLocally = false;
                            int alternativeMaxWidth52 = Math.max(alternativeMaxWidth, !matchWidthLocally ? margin : measuredWidth2);
                            if (!allFillParent3) {
                            }
                            allFillParent = false;
                            int totalLength422 = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength422, totalLength422 + child5.getMeasuredHeight() + lp6.topMargin + lp6.bottomMargin + getNextLocationOffset(child5));
                            allFillParent3 = allFillParent;
                            remainingExcess5 = remainingExcess2;
                            remainingWeightSum2 = remainingWeightSum4;
                            alternativeMaxWidth = alternativeMaxWidth52;
                        }
                    } else {
                        count3 = count;
                        heightMode3 = heightMode;
                        useLargestChild = useLargestChild2;
                        baselineChildIndex = baselineChildIndex2;
                        int heightMode12 = widthMeasureSpec;
                    }
                    i19++;
                    useLargestChild2 = useLargestChild;
                    baselineChildIndex2 = baselineChildIndex;
                    count = count3;
                    heightMode = heightMode3;
                }
                count2 = count;
                int i22 = heightMode;
                boolean z3 = useLargestChild2;
                int i23 = baselineChildIndex2;
                heightMode2 = widthMeasureSpec;
                this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
                int i24 = remainingExcess5;
                maxWidth6 = alternativeMaxWidth;
                setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth6 + this.mPaddingLeft + this.mPaddingRight, getSuggestedMinimumWidth()), heightMode2, childState2), heightSizeAndState);
                if (!matchWidth) {
                }
            }
        }
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.width == -1) {
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:210:0x0505  */
    /* JADX WARNING: Removed duplicated region for block: B:218:0x053b  */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x05e4  */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x05ec  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01f7  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0208  */
    public void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int childState;
        int count;
        int alternativeMaxHeight;
        int maxHeight;
        int count2;
        float totalWeight;
        int widthMode;
        int largestChildWidth;
        int count3;
        boolean useLargestChild;
        int remainingExcess;
        float remainingWeightSum;
        int widthMode2;
        boolean matchHeightLocally;
        boolean allFillParent;
        int childWidth;
        float totalWeight2;
        int maxHeight2;
        int widthSize;
        int i;
        int i2;
        boolean baselineAligned;
        int maxHeight3;
        int alternativeMaxHeight2;
        int weightedMaxHeight;
        int i3;
        int i4;
        LayoutParams lp;
        int margin;
        int weightedMaxHeight2;
        int alternativeMaxHeight3;
        int i5;
        int i6 = widthMeasureSpec;
        int i7 = heightMeasureSpec;
        this.mTotalLength = 0;
        int maxHeight4 = 0;
        float totalWeight3 = 0.0f;
        int count4 = getVirtualChildCount();
        int widthMode3 = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (this.mMaxAscent == null || this.mMaxDescent == null) {
            this.mMaxAscent = new int[4];
            this.mMaxDescent = new int[4];
        }
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
        boolean matchHeight = false;
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent[3] = -1;
        maxDescent[2] = -1;
        maxDescent[1] = -1;
        maxDescent[0] = -1;
        boolean baselineAligned2 = this.mBaselineAligned;
        boolean skippedMeasure = false;
        boolean useLargestChild2 = this.mUseLargestChild;
        int[] maxDescent2 = maxDescent;
        boolean isExactly = widthMode3 == 1073741824;
        int usedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        int childState2 = 0;
        int i8 = 0;
        int childHeight = 0;
        int i9 = 0;
        boolean allFillParent2 = true;
        int usedWidth = Integer.MIN_VALUE;
        while (i9 < count4) {
            View child = getVirtualChildAt(i9);
            if (child == null) {
                this.mTotalLength += measureNullChild(i9);
                baselineAligned = baselineAligned2;
                i8 = i8;
            } else {
                int weightedMaxHeight3 = i8;
                int alternativeMaxHeight4 = maxHeight4;
                if (child.getVisibility() == 8) {
                    i9 += getChildrenSkipCount(child, i9);
                    baselineAligned = baselineAligned2;
                    i8 = weightedMaxHeight3;
                    maxHeight4 = alternativeMaxHeight4;
                } else {
                    nonSkippedChildCount++;
                    if (hasDividerBeforeChildAt(i9)) {
                        this.mTotalLength += this.mDividerWidth;
                    }
                    LayoutParams lp2 = (LayoutParams) child.getLayoutParams();
                    float totalWeight4 = totalWeight3 + lp2.weight;
                    boolean useExcessSpace = lp2.width == 0 && lp2.weight > 0.0f;
                    if (widthMode3 != 1073741824 || !useExcessSpace) {
                        int i10 = i9;
                        if (useExcessSpace) {
                            lp2.width = -2;
                        }
                        i3 = i10;
                        weightedMaxHeight = weightedMaxHeight3;
                        LayoutParams lp3 = lp2;
                        alternativeMaxHeight2 = alternativeMaxHeight4;
                        maxHeight3 = childHeight;
                        int maxHeight5 = i6;
                        int largestChildWidth2 = usedWidth;
                        int largestChildWidth3 = i7;
                        baselineAligned = baselineAligned2;
                        i4 = -1;
                        measureChildBeforeLayout(child, i3, maxHeight5, totalWeight4 == 0.0f ? this.mTotalLength : 0, largestChildWidth3, 0);
                        int childWidth2 = child.getMeasuredWidth();
                        if (useExcessSpace) {
                            lp = lp3;
                            lp.width = 0;
                            usedExcessSpace += childWidth2;
                        } else {
                            lp = lp3;
                        }
                        if (isExactly) {
                            this.mTotalLength += lp.leftMargin + childWidth2 + lp.rightMargin + getNextLocationOffset(child);
                        } else {
                            int totalLength = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength, totalLength + childWidth2 + lp.leftMargin + lp.rightMargin + getNextLocationOffset(child));
                        }
                        if (useLargestChild2) {
                            usedWidth = Math.max(childWidth2, largestChildWidth2);
                        } else {
                            usedWidth = largestChildWidth2;
                        }
                    } else {
                        if (isExactly) {
                            i5 = i9;
                            this.mTotalLength += lp2.leftMargin + lp2.rightMargin;
                        } else {
                            i5 = i9;
                            int i11 = this.mTotalLength;
                            this.mTotalLength = Math.max(i11, lp2.leftMargin + i11 + lp2.rightMargin);
                        }
                        if (baselineAligned2) {
                            child.measure(View.MeasureSpec.makeSafeMeasureSpec(View.MeasureSpec.getSize(widthMeasureSpec), 0), View.MeasureSpec.makeSafeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), 0));
                        } else {
                            skippedMeasure = true;
                        }
                        lp = lp2;
                        maxHeight3 = childHeight;
                        baselineAligned = baselineAligned2;
                        weightedMaxHeight = weightedMaxHeight3;
                        alternativeMaxHeight2 = alternativeMaxHeight4;
                        i3 = i5;
                        i4 = -1;
                    }
                    boolean matchHeightLocally2 = false;
                    if (heightMode != 1073741824 && lp.height == i4) {
                        matchHeight = true;
                        matchHeightLocally2 = true;
                    }
                    int margin2 = lp.topMargin + lp.bottomMargin;
                    int childHeight2 = child.getMeasuredHeight() + margin2;
                    int childState3 = combineMeasuredStates(childState2, child.getMeasuredState());
                    if (baselineAligned) {
                        int childBaseline = child.getBaseline();
                        if (childBaseline != i4) {
                            int index = ((((lp.gravity < 0 ? this.mGravity : lp.gravity) & 112) >> 4) & -2) >> 1;
                            maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                            margin = margin2;
                            maxDescent2[index] = Math.max(maxDescent2[index], childHeight2 - childBaseline);
                            int maxHeight6 = Math.max(maxHeight3, childHeight2);
                            boolean allFillParent3 = !allFillParent2 && lp.height == -1;
                            if (lp.weight <= 0.0f) {
                                weightedMaxHeight2 = Math.max(weightedMaxHeight, matchHeightLocally2 ? margin : childHeight2);
                                boolean z = matchHeightLocally2;
                                alternativeMaxHeight3 = alternativeMaxHeight2;
                            } else {
                                int weightedMaxHeight4 = weightedMaxHeight;
                                boolean z2 = matchHeightLocally2;
                                alternativeMaxHeight3 = Math.max(alternativeMaxHeight2, matchHeightLocally2 ? margin : childHeight2);
                                weightedMaxHeight2 = weightedMaxHeight4;
                            }
                            int i12 = i3;
                            childHeight = maxHeight6;
                            childState2 = childState3;
                            allFillParent2 = allFillParent3;
                            totalWeight3 = totalWeight4;
                            maxHeight4 = alternativeMaxHeight3;
                            i9 = i12 + getChildrenSkipCount(child, i12);
                            i8 = weightedMaxHeight2;
                        }
                    }
                    margin = margin2;
                    int maxHeight62 = Math.max(maxHeight3, childHeight2);
                    if (!allFillParent2) {
                    }
                    if (lp.weight <= 0.0f) {
                    }
                    int i122 = i3;
                    childHeight = maxHeight62;
                    childState2 = childState3;
                    allFillParent2 = allFillParent3;
                    totalWeight3 = totalWeight4;
                    maxHeight4 = alternativeMaxHeight3;
                    i9 = i122 + getChildrenSkipCount(child, i122);
                    i8 = weightedMaxHeight2;
                }
            }
            i9++;
            baselineAligned2 = baselineAligned;
            i6 = widthMeasureSpec;
            i7 = heightMeasureSpec;
        }
        int weightedMaxHeight5 = i8;
        int alternativeMaxHeight5 = maxHeight4;
        int maxHeight7 = childHeight;
        int largestChildWidth4 = usedWidth;
        boolean baselineAligned3 = baselineAligned2;
        int childState4 = childState2;
        if (nonSkippedChildCount > 0 && hasDividerBeforeChildAt(count4)) {
            this.mTotalLength += this.mDividerWidth;
        }
        if (maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1 && maxAscent[3] == -1) {
            childState = childState4;
        } else {
            childState = childState4;
            maxHeight7 = Math.max(maxHeight7, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent2[3], Math.max(maxDescent2[0], Math.max(maxDescent2[1], maxDescent2[2]))));
        }
        if (useLargestChild2 && (widthMode3 == Integer.MIN_VALUE || widthMode3 == 0)) {
            this.mTotalLength = 0;
            int i13 = 0;
            while (i13 < count4) {
                View child2 = getVirtualChildAt(i13);
                if (child2 == null) {
                    this.mTotalLength += measureNullChild(i13);
                    i2 = i13;
                } else if (child2.getVisibility() == 8) {
                    i = i13 + getChildrenSkipCount(child2, i13);
                    i13 = i + 1;
                } else {
                    if (this.mHwActionBarTabLayoutUsed && hasDividerBeforeChildAt(i13)) {
                        this.mTotalLength += this.mDividerWidth;
                    }
                    LayoutParams lp4 = (LayoutParams) child2.getLayoutParams();
                    if (isExactly) {
                        i2 = i13;
                        this.mTotalLength += lp4.leftMargin + largestChildWidth4 + lp4.rightMargin + getNextLocationOffset(child2);
                    } else {
                        i2 = i13;
                        int i14 = this.mTotalLength;
                        this.mTotalLength = Math.max(i14, i14 + largestChildWidth4 + lp4.leftMargin + lp4.rightMargin + getNextLocationOffset(child2));
                    }
                }
                i = i2;
                i13 = i + 1;
            }
        }
        this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
        int largestChildWidth5 = largestChildWidth4;
        int largestChildWidth6 = resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, 0);
        int widthSize2 = largestChildWidth6 & 16777215;
        int remainingExcess2 = (widthSize2 - this.mTotalLength) + (this.mAllowInconsistentMeasurement ? 0 : usedExcessSpace);
        if (skippedMeasure) {
            int i15 = maxHeight7;
            totalWeight = totalWeight3;
        } else if ((sRemeasureWeightedChildren || remainingExcess2 != 0) && totalWeight3 > 0.0f) {
            int i16 = widthSize2;
            int i17 = maxHeight7;
            totalWeight = totalWeight3;
        } else {
            int alternativeMaxHeight6 = Math.max(alternativeMaxHeight5, weightedMaxHeight5);
            if (useLargestChild2 && widthMode3 != 1073741824) {
                int i18 = 0;
                while (true) {
                    int i19 = i18;
                    if (i19 >= count4) {
                        break;
                    }
                    int alternativeMaxHeight7 = alternativeMaxHeight6;
                    View child3 = getVirtualChildAt(i19);
                    if (child3 != null) {
                        widthSize = widthSize2;
                        maxHeight2 = maxHeight7;
                        if (child3.getVisibility() == 8) {
                            totalWeight2 = totalWeight3;
                        } else {
                            LayoutParams lp5 = (LayoutParams) child3.getLayoutParams();
                            float childExtra = lp5.weight;
                            if (childExtra > 0.0f) {
                                LayoutParams layoutParams = lp5;
                                float f = childExtra;
                                totalWeight2 = totalWeight3;
                                child3.measure(View.MeasureSpec.makeMeasureSpec(largestChildWidth5, 1073741824), View.MeasureSpec.makeMeasureSpec(child3.getMeasuredHeight(), 1073741824));
                            } else {
                                totalWeight2 = totalWeight3;
                            }
                        }
                    } else {
                        widthSize = widthSize2;
                        maxHeight2 = maxHeight7;
                        totalWeight2 = totalWeight3;
                    }
                    i18 = i19 + 1;
                    alternativeMaxHeight6 = alternativeMaxHeight7;
                    widthSize2 = widthSize;
                    maxHeight7 = maxHeight2;
                    totalWeight3 = totalWeight2;
                }
            }
            alternativeMaxHeight = alternativeMaxHeight6;
            int i20 = widthSize2;
            int maxHeight8 = maxHeight7;
            float f2 = totalWeight3;
            int i21 = largestChildWidth5;
            int i22 = remainingExcess2;
            int i23 = weightedMaxHeight5;
            count = count4;
            int i24 = widthMode3;
            boolean z3 = useLargestChild2;
            maxHeight = maxHeight8;
            count2 = heightMeasureSpec;
            if (!allFillParent2 && heightMode != 1073741824) {
                maxHeight = alternativeMaxHeight;
            }
            setMeasuredDimension((childState & -16777216) | largestChildWidth6, resolveSizeAndState(Math.max(maxHeight + this.mPaddingTop + this.mPaddingBottom, getSuggestedMinimumHeight()), count2, childState << 16));
            if (!matchHeight) {
                forceUniformHeight(count, widthMeasureSpec);
                return;
            }
            int i25 = count;
            int i26 = widthMeasureSpec;
            return;
        }
        float remainingWeightSum2 = this.mWeightSum > 0.0f ? this.mWeightSum : totalWeight;
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent2[3] = -1;
        maxDescent2[2] = -1;
        maxDescent2[1] = -1;
        maxDescent2[0] = -1;
        maxHeight = -1;
        this.mTotalLength = 0;
        int remainingExcess3 = remainingExcess2;
        int childState5 = childState;
        int alternativeMaxHeight8 = alternativeMaxHeight5;
        int i27 = 0;
        while (i27 < count4) {
            int weightedMaxHeight6 = weightedMaxHeight5;
            View child4 = getVirtualChildAt(i27);
            if (child4 != null) {
                useLargestChild = useLargestChild2;
                if (child4.getVisibility() == 8) {
                    largestChildWidth = largestChildWidth5;
                    count3 = count4;
                    widthMode = widthMode3;
                    int count5 = heightMeasureSpec;
                } else {
                    if (hasDividerBeforeChildAt(i27)) {
                        this.mTotalLength += this.mDividerWidth;
                    }
                    LayoutParams lp6 = (LayoutParams) child4.getLayoutParams();
                    float childWeight = lp6.weight;
                    if (childWeight > 0.0f) {
                        count3 = count4;
                        int share = (int) ((((float) remainingExcess3) * childWeight) / remainingWeightSum2);
                        int remainingExcess4 = remainingExcess3 - share;
                        remainingWeightSum = remainingWeightSum2 - childWeight;
                        if (this.mUseLargestChild && widthMode3 != 1073741824) {
                            childWidth = largestChildWidth5;
                        } else if (lp6.width != 0 || (this.mAllowInconsistentMeasurement && widthMode3 != 1073741824)) {
                            childWidth = child4.getMeasuredWidth() + share;
                        } else {
                            childWidth = share;
                        }
                        remainingExcess = remainingExcess4;
                        largestChildWidth = largestChildWidth5;
                        int i28 = childWidth;
                        int i29 = share;
                        widthMode = widthMode3;
                        widthMode2 = -1;
                        child4.measure(View.MeasureSpec.makeMeasureSpec(Math.max(0, childWidth), 1073741824), getChildMeasureSpec(heightMeasureSpec, this.mPaddingTop + this.mPaddingBottom + lp6.topMargin + lp6.bottomMargin, lp6.height));
                        childState5 = combineMeasuredStates(childState5, child4.getMeasuredState() & -16777216);
                    } else {
                        largestChildWidth = largestChildWidth5;
                        count3 = count4;
                        widthMode = widthMode3;
                        int count6 = heightMeasureSpec;
                        widthMode2 = -1;
                        remainingWeightSum = remainingWeightSum2;
                        remainingExcess = remainingExcess3;
                    }
                    if (isExactly) {
                        this.mTotalLength += child4.getMeasuredWidth() + lp6.leftMargin + lp6.rightMargin + getNextLocationOffset(child4);
                    } else {
                        int totalLength2 = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength2, child4.getMeasuredWidth() + totalLength2 + lp6.leftMargin + lp6.rightMargin + getNextLocationOffset(child4));
                    }
                    boolean matchHeightLocally3 = heightMode != 1073741824 && lp6.height == widthMode2;
                    int margin3 = lp6.topMargin + lp6.bottomMargin;
                    int childHeight3 = child4.getMeasuredHeight() + margin3;
                    maxHeight = Math.max(maxHeight, childHeight3);
                    alternativeMaxHeight8 = Math.max(alternativeMaxHeight8, matchHeightLocally3 ? margin3 : childHeight3);
                    if (allFillParent2) {
                        boolean z4 = matchHeightLocally3;
                        if (lp6.height == -1) {
                            matchHeightLocally = true;
                            if (!baselineAligned3) {
                                int childBaseline2 = child4.getBaseline();
                                allFillParent = matchHeightLocally;
                                if (childBaseline2 != -1) {
                                    int gravity = (lp6.gravity < 0 ? this.mGravity : lp6.gravity) & 112;
                                    int index2 = ((gravity >> 4) & -2) >> 1;
                                    int i30 = gravity;
                                    maxAscent[index2] = Math.max(maxAscent[index2], childBaseline2);
                                    int i31 = margin3;
                                    maxDescent2[index2] = Math.max(maxDescent2[index2], childHeight3 - childBaseline2);
                                }
                            } else {
                                allFillParent = matchHeightLocally;
                            }
                            remainingWeightSum2 = remainingWeightSum;
                            remainingExcess3 = remainingExcess;
                            allFillParent2 = allFillParent;
                        }
                    }
                    matchHeightLocally = false;
                    if (!baselineAligned3) {
                    }
                    remainingWeightSum2 = remainingWeightSum;
                    remainingExcess3 = remainingExcess;
                    allFillParent2 = allFillParent;
                }
            } else {
                largestChildWidth = largestChildWidth5;
                count3 = count4;
                widthMode = widthMode3;
                useLargestChild = useLargestChild2;
                int count7 = heightMeasureSpec;
            }
            i27++;
            weightedMaxHeight5 = weightedMaxHeight6;
            useLargestChild2 = useLargestChild;
            count4 = count3;
            largestChildWidth5 = largestChildWidth;
            widthMode3 = widthMode;
            int i32 = widthMeasureSpec;
        }
        int i33 = weightedMaxHeight5;
        count = count4;
        int i34 = widthMode3;
        boolean z5 = useLargestChild2;
        count2 = heightMeasureSpec;
        this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
        if (!(maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1 && maxAscent[3] == -1)) {
            maxHeight = Math.max(maxHeight, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent2[3], Math.max(maxDescent2[0], Math.max(maxDescent2[1], maxDescent2[2]))));
        }
        childState = childState5;
        alternativeMaxHeight = alternativeMaxHeight8;
        maxHeight = alternativeMaxHeight;
        setMeasuredDimension((childState & -16777216) | largestChildWidth6, resolveSizeAndState(Math.max(maxHeight + this.mPaddingTop + this.mPaddingBottom, getSuggestedMinimumHeight()), count2, childState << 16));
        if (!matchHeight) {
        }
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.height == -1) {
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getChildrenSkipCount(View child, int index) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int measureNullChild(int childIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    /* access modifiers changed from: package-private */
    public int getLocationOffset(View child) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getNextLocationOffset(View child) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mOrientation == 1) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    /* access modifiers changed from: package-private */
    public void layoutVertical(int left, int top, int right, int bottom) {
        int childTop;
        int paddingLeft;
        int majorGravity;
        int childLeft;
        int paddingLeft2 = this.mPaddingLeft;
        int width = right - left;
        int childRight = width - this.mPaddingRight;
        int childSpace = (width - paddingLeft2) - this.mPaddingRight;
        int count = getVirtualChildCount();
        int majorGravity2 = this.mGravity & 112;
        int minorGravity = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (majorGravity2 == 16) {
            childTop = this.mPaddingTop + (((bottom - top) - this.mTotalLength) / 2);
        } else if (majorGravity2 != 80) {
            childTop = this.mPaddingTop;
        } else {
            childTop = ((this.mPaddingTop + bottom) - top) - this.mTotalLength;
        }
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < count) {
                View child = getVirtualChildAt(i2);
                if (child == null) {
                    childTop += measureNullChild(i2);
                    majorGravity = majorGravity2;
                    paddingLeft = paddingLeft2;
                } else if (child.getVisibility() != 8) {
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    int gravity = lp.gravity;
                    if (gravity < 0) {
                        gravity = minorGravity;
                    }
                    int layoutDirection = getLayoutDirection();
                    int gravity2 = gravity;
                    int gravity3 = Gravity.getAbsoluteGravity(gravity, layoutDirection) & 7;
                    majorGravity = majorGravity2;
                    if (gravity3 != 1) {
                        childLeft = gravity3 != 5 ? lp.leftMargin + paddingLeft2 : (childRight - childWidth) - lp.rightMargin;
                    } else {
                        childLeft = ((((childSpace - childWidth) / 2) + paddingLeft2) + lp.leftMargin) - lp.rightMargin;
                    }
                    int i3 = gravity2;
                    if (hasDividerBeforeChildAt(i2)) {
                        childTop += this.mDividerHeight;
                    }
                    int childTop2 = childTop + lp.topMargin;
                    int i4 = layoutDirection;
                    LayoutParams lp2 = lp;
                    View child2 = child;
                    paddingLeft = paddingLeft2;
                    int i5 = i2;
                    setChildFrame(child, childLeft, childTop2 + getLocationOffset(child), childWidth, childHeight);
                    int childTop3 = childTop2 + childHeight + lp2.bottomMargin + getNextLocationOffset(child2);
                    i2 = i5 + getChildrenSkipCount(child2, i5);
                    childTop = childTop3;
                } else {
                    majorGravity = majorGravity2;
                    paddingLeft = paddingLeft2;
                    int paddingLeft3 = i2;
                }
                i = i2 + 1;
                majorGravity2 = majorGravity;
                paddingLeft2 = paddingLeft;
            } else {
                int i6 = paddingLeft2;
                return;
            }
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ef  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0102  */
    public void layoutHorizontal(int left, int top, int right, int bottom) {
        int childLeft;
        boolean isLayoutRtl;
        int[] maxAscent;
        int count;
        int[] maxDescent;
        boolean baselineAligned;
        int majorGravity;
        int childBaseline;
        int gravity;
        int i;
        int childTop;
        boolean isLayoutRtl2 = isLayoutRtl();
        int paddingTop = this.mPaddingTop;
        int height = bottom - top;
        int childBottom = height - this.mPaddingBottom;
        int childSpace = (height - paddingTop) - this.mPaddingBottom;
        int count2 = getVirtualChildCount();
        int majorGravity2 = this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int minorGravity = this.mGravity & 112;
        boolean baselineAligned2 = this.mBaselineAligned;
        int[] maxAscent2 = this.mMaxAscent;
        int[] maxDescent2 = this.mMaxDescent;
        int layoutDirection = getLayoutDirection();
        int absoluteGravity = Gravity.getAbsoluteGravity(majorGravity2, layoutDirection);
        if (absoluteGravity != 1) {
            if (absoluteGravity != 5) {
                childLeft = this.mPaddingLeft;
            } else {
                childLeft = ((this.mPaddingLeft + right) - left) - this.mTotalLength;
            }
        } else {
            int i2 = layoutDirection;
            childLeft = this.mPaddingLeft + (((right - left) - this.mTotalLength) / 2);
        }
        int childLeft2 = childLeft;
        int start = 0;
        int dir = 1;
        if (isLayoutRtl2) {
            start = count2 - 1;
            dir = -1;
        }
        int i3 = 0;
        int childLeft3 = childLeft2;
        while (true) {
            int childTop2 = i3;
            if (childTop2 < count2) {
                int childIndex = start + (dir * childTop2);
                View child = getVirtualChildAt(childIndex);
                if (child == null) {
                    childLeft3 += measureNullChild(childIndex);
                    maxDescent = maxDescent2;
                    maxAscent = maxAscent2;
                    baselineAligned = baselineAligned2;
                    majorGravity = majorGravity2;
                    count = count2;
                    isLayoutRtl = isLayoutRtl2;
                } else {
                    int i4 = childTop2;
                    majorGravity = majorGravity2;
                    if (child.getVisibility() != 8) {
                        int childWidth = child.getMeasuredWidth();
                        int childHeight = child.getMeasuredHeight();
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        if (baselineAligned2) {
                            baselineAligned = baselineAligned2;
                            if (lp.height != -1) {
                                childBaseline = child.getBaseline();
                                gravity = lp.gravity;
                                if (gravity < 0) {
                                    gravity = minorGravity;
                                }
                                i = gravity & 112;
                                count = count2;
                                if (i != 16) {
                                    childTop = ((((childSpace - childHeight) / 2) + paddingTop) + lp.topMargin) - lp.bottomMargin;
                                } else if (i == 48) {
                                    childTop = lp.topMargin + paddingTop;
                                    if (childBaseline != -1) {
                                        childTop += maxAscent2[1] - childBaseline;
                                    }
                                } else if (i != 80) {
                                    childTop = paddingTop;
                                } else {
                                    childTop = (childBottom - childHeight) - lp.bottomMargin;
                                    if (childBaseline != -1) {
                                        childTop -= maxDescent2[2] - (child.getMeasuredHeight() - childBaseline);
                                    }
                                }
                                if (hasDividerBeforeChildAt(childIndex)) {
                                    childLeft3 += this.mDividerWidth;
                                }
                                int childLeft4 = childLeft3 + lp.leftMargin;
                                maxDescent = maxDescent2;
                                maxAscent = maxAscent2;
                                int i5 = childBaseline;
                                isLayoutRtl = isLayoutRtl2;
                                setChildFrame(child, childLeft4 + getLocationOffset(child), childTop, childWidth, childHeight);
                                childLeft3 = childLeft4 + childWidth + lp.rightMargin + getNextLocationOffset(child);
                                childTop2 = i4 + getChildrenSkipCount(child, childIndex);
                            }
                        } else {
                            baselineAligned = baselineAligned2;
                        }
                        childBaseline = -1;
                        gravity = lp.gravity;
                        if (gravity < 0) {
                        }
                        i = gravity & 112;
                        count = count2;
                        if (i != 16) {
                        }
                        if (hasDividerBeforeChildAt(childIndex)) {
                        }
                        int childLeft42 = childLeft3 + lp.leftMargin;
                        maxDescent = maxDescent2;
                        maxAscent = maxAscent2;
                        int i52 = childBaseline;
                        isLayoutRtl = isLayoutRtl2;
                        setChildFrame(child, childLeft42 + getLocationOffset(child), childTop, childWidth, childHeight);
                        childLeft3 = childLeft42 + childWidth + lp.rightMargin + getNextLocationOffset(child);
                        childTop2 = i4 + getChildrenSkipCount(child, childIndex);
                    } else {
                        maxDescent = maxDescent2;
                        maxAscent = maxAscent2;
                        baselineAligned = baselineAligned2;
                        count = count2;
                        isLayoutRtl = isLayoutRtl2;
                        childTop2 = i4;
                    }
                }
                i3 = childTop2 + 1;
                majorGravity2 = majorGravity;
                baselineAligned2 = baselineAligned;
                maxDescent2 = maxDescent;
                count2 = count;
                maxAscent2 = maxAscent;
                isLayoutRtl2 = isLayoutRtl;
            } else {
                int[] iArr = maxAscent2;
                boolean z = baselineAligned2;
                int i6 = majorGravity2;
                int i7 = count2;
                boolean z2 = isLayoutRtl2;
                return;
            }
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
            if ((8388615 & gravity) == 0) {
                gravity |= Gravity.START;
            }
            if ((gravity & 112) == 0) {
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
        if ((8388615 & this.mGravity) != gravity) {
            this.mGravity = (this.mGravity & -8388616) | gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setVerticalGravity(int verticalGravity) {
        int gravity = verticalGravity & 112;
        if ((this.mGravity & 112) != gravity) {
            this.mGravity = (this.mGravity & -113) | gravity;
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (sPreserveMarginParamsInLayoutParamConversion) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            }
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
            }
        }
        return new LayoutParams(lp);
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public CharSequence getAccessibilityClassName() {
        return LinearLayout.class.getName();
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
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
