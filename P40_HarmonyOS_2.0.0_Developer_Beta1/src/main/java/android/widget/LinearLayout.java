package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.app.slice.Slice;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.TtmlUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.function.IntFunction;

@RemoteViews.RemoteView
public class LinearLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    @UnsupportedAppUsage
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_CENTER_VERTICAL = 0;
    private static final int INDEX_FILL = 3;
    @UnsupportedAppUsage
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
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    private boolean mBaselineAligned;
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    private int mBaselineAlignedChildIndex;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mBaselineChildTop;
    @UnsupportedAppUsage
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = "measurement", flagMapping = {@ViewDebug.FlagToString(equals = -1, mask = -1, name = "NONE"), @ViewDebug.FlagToString(equals = 0, mask = 0, name = "NONE"), @ViewDebug.FlagToString(equals = 48, mask = 48, name = "TOP"), @ViewDebug.FlagToString(equals = 80, mask = 80, name = "BOTTOM"), @ViewDebug.FlagToString(equals = 3, mask = 3, name = "LEFT"), @ViewDebug.FlagToString(equals = 5, mask = 5, name = "RIGHT"), @ViewDebug.FlagToString(equals = 8388611, mask = 8388611, name = "START"), @ViewDebug.FlagToString(equals = 8388613, mask = 8388613, name = "END"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "CENTER_VERTICAL"), @ViewDebug.FlagToString(equals = 112, mask = 112, name = "FILL_VERTICAL"), @ViewDebug.FlagToString(equals = 1, mask = 1, name = "CENTER_HORIZONTAL"), @ViewDebug.FlagToString(equals = 7, mask = 7, name = "FILL_HORIZONTAL"), @ViewDebug.FlagToString(equals = 17, mask = 17, name = "CENTER"), @ViewDebug.FlagToString(equals = 119, mask = 119, name = "FILL"), @ViewDebug.FlagToString(equals = 8388608, mask = 8388608, name = "RELATIVE")}, formatToHexString = true)
    private int mGravity;
    private int mLayoutDirection;
    @UnsupportedAppUsage
    private int[] mMaxAscent;
    @UnsupportedAppUsage
    private int[] mMaxDescent;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mOrientation;
    private int mShowDividers;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mTotalLength;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    private boolean mUseLargestChild;
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    private float mWeightSum;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, mapping = {@ViewDebug.IntToString(from = -1, to = "NONE"), @ViewDebug.IntToString(from = 0, to = "NONE"), @ViewDebug.IntToString(from = 48, to = "TOP"), @ViewDebug.IntToString(from = 80, to = "BOTTOM"), @ViewDebug.IntToString(from = 3, to = "LEFT"), @ViewDebug.IntToString(from = 5, to = "RIGHT"), @ViewDebug.IntToString(from = 8388611, to = "START"), @ViewDebug.IntToString(from = 8388613, to = "END"), @ViewDebug.IntToString(from = 16, to = "CENTER_VERTICAL"), @ViewDebug.IntToString(from = 112, to = "FILL_VERTICAL"), @ViewDebug.IntToString(from = 1, to = "CENTER_HORIZONTAL"), @ViewDebug.IntToString(from = 7, to = "FILL_HORIZONTAL"), @ViewDebug.IntToString(from = 17, to = "CENTER"), @ViewDebug.IntToString(from = 119, to = "FILL")})
        public int gravity;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        public float weight;

        public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<LayoutParams> {
            private int mLayout_gravityId;
            private int mLayout_weightId;
            private boolean mPropertiesMapped = false;

            @Override // android.view.inspector.InspectionCompanion
            public void mapProperties(PropertyMapper propertyMapper) {
                this.mLayout_gravityId = propertyMapper.mapGravity("layout_gravity", 16842931);
                this.mLayout_weightId = propertyMapper.mapFloat("layout_weight", 16843137);
                this.mPropertiesMapped = true;
            }

            public void readProperties(LayoutParams node, PropertyReader propertyReader) {
                if (this.mPropertiesMapped) {
                    propertyReader.readGravity(this.mLayout_gravityId, node.gravity);
                    propertyReader.readFloat(this.mLayout_weightId, node.weight);
                    return;
                }
                throw new InspectionCompanion.UninitializedPropertyMapException();
            }
        }

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

        @Override // android.view.ViewGroup.LayoutParams
        public String debug(String output) {
            return output + "LinearLayout.LayoutParams={width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " weight=" + this.weight + "}";
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewGroup.MarginLayoutParams, android.view.ViewGroup.LayoutParams
        @UnsupportedAppUsage
        public void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:weight", this.weight);
            encoder.addProperty("layout:gravity", this.gravity);
        }
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<LinearLayout> {
        private int mBaselineAlignedChildIndexId;
        private int mBaselineAlignedId;
        private int mDividerId;
        private int mGravityId;
        private int mMeasureWithLargestChildId;
        private int mOrientationId;
        private boolean mPropertiesMapped = false;
        private int mWeightSumId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mBaselineAlignedId = propertyMapper.mapBoolean("baselineAligned", 16843046);
            this.mBaselineAlignedChildIndexId = propertyMapper.mapInt("baselineAlignedChildIndex", 16843047);
            this.mDividerId = propertyMapper.mapObject("divider", 16843049);
            this.mGravityId = propertyMapper.mapGravity("gravity", 16842927);
            this.mMeasureWithLargestChildId = propertyMapper.mapBoolean("measureWithLargestChild", 16843476);
            SparseArray<String> orientationEnumMapping = new SparseArray<>();
            orientationEnumMapping.put(0, Slice.HINT_HORIZONTAL);
            orientationEnumMapping.put(1, "vertical");
            Objects.requireNonNull(orientationEnumMapping);
            this.mOrientationId = propertyMapper.mapIntEnum("orientation", 16842948, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mWeightSumId = propertyMapper.mapFloat("weightSum", 16843048);
            this.mPropertiesMapped = true;
        }

        public void readProperties(LinearLayout node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mBaselineAlignedId, node.isBaselineAligned());
                propertyReader.readInt(this.mBaselineAlignedChildIndexId, node.getBaselineAlignedChildIndex());
                propertyReader.readObject(this.mDividerId, node.getDividerDrawable());
                propertyReader.readGravity(this.mGravityId, node.getGravity());
                propertyReader.readBoolean(this.mMeasureWithLargestChildId, node.isMeasureWithLargestChildEnabled());
                propertyReader.readIntEnum(this.mOrientationId, node.getOrientation());
                propertyReader.readFloat(this.mWeightSumId, node.getWeightSum());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
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
        saveAttributeDataForStyleable(context, R.styleable.LinearLayout, attrs, a, defStyleAttr, defStyleRes);
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

    @Override // android.view.ViewGroup
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
    @Override // android.view.View
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
        if (hasDividerBeforeChildAt(count)) {
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
            if (!(child == null || child.getVisibility() == 8)) {
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
        if (hasDividerBeforeChildAt(count)) {
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

    @Override // android.view.View
    public int getBaseline() {
        int majorGravity;
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        int childCount = getChildCount();
        int i = this.mBaselineAlignedChildIndex;
        if (childCount > i) {
            View child = getChildAt(i);
            int childBaseline = child.getBaseline();
            if (childBaseline != -1) {
                int childTop = this.mBaselineChildTop;
                if (this.mOrientation == 1 && (majorGravity = this.mGravity & 112) != 48) {
                    if (majorGravity == 16) {
                        childTop += ((((this.mBottom - this.mTop) - this.mPaddingTop) - this.mPaddingBottom) - this.mTotalLength) / 2;
                    } else if (majorGravity == 80) {
                        childTop = ((this.mBottom - this.mTop) - this.mPaddingBottom) - this.mTotalLength;
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
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == 1) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasDividerBeforeChildAt(int childIndex) {
        return childIndex == getVirtualChildCount() ? (this.mShowDividers & 4) != 0 : allViewsAreGoneBefore(childIndex) ? (this.mShowDividers & 1) != 0 : (this.mShowDividers & 2) != 0;
    }

    private boolean allViewsAreGoneBefore(int childIndex) {
        for (int i = childIndex - 1; i >= 0; i--) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x03c8  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x03ca  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x03d2  */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0453  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x0459  */
    public void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int count;
        int childState;
        int heightMode;
        int count2;
        int remainingExcess;
        int alternativeMaxWidth;
        int childState2;
        float totalWeight;
        int count3;
        int baselineChildIndex;
        boolean useLargestChild;
        int largestChildHeight;
        int count4;
        int count5;
        int remainingExcess2;
        boolean matchWidthLocally;
        boolean allFillParent;
        int childHeight;
        int alternativeMaxWidth2;
        float totalWeight2;
        int weightedMaxWidth;
        int alternativeMaxWidth3;
        int childState3;
        int count6;
        int heightMode2;
        int alternativeMaxWidth4;
        int alternativeMaxWidth5;
        int heightMode3;
        int count7;
        LayoutParams lp;
        int childState4;
        View child;
        int i;
        int weightedMaxWidth2;
        this.mTotalLength = 0;
        int weightedMaxWidth3 = 0;
        float totalWeight3 = 0.0f;
        int count8 = getVirtualChildCount();
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode4 = View.MeasureSpec.getMode(heightMeasureSpec);
        int baselineChildIndex2 = this.mBaselineAlignedChildIndex;
        boolean useLargestChild2 = this.mUseLargestChild;
        int consumedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        boolean matchWidth = false;
        int margin = 0;
        int maxWidth = 0;
        int alternativeMaxWidth6 = 0;
        int largestChildHeight2 = Integer.MIN_VALUE;
        boolean skippedMeasure = false;
        int i2 = 0;
        boolean allFillParent2 = true;
        while (true) {
            int i3 = 8;
            if (i2 < count8) {
                View child2 = getVirtualChildAt(i2);
                if (child2 == null) {
                    this.mTotalLength += measureNullChild(i2);
                    count6 = count8;
                    heightMode2 = heightMode4;
                    alternativeMaxWidth6 = alternativeMaxWidth6;
                } else if (child2.getVisibility() == 8) {
                    i2 += getChildrenSkipCount(child2, i2);
                    count6 = count8;
                    alternativeMaxWidth6 = alternativeMaxWidth6;
                    maxWidth = maxWidth;
                    heightMode2 = heightMode4;
                } else {
                    nonSkippedChildCount++;
                    if (hasDividerBeforeChildAt(i2)) {
                        this.mTotalLength += this.mDividerHeight;
                    }
                    LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                    float totalWeight4 = totalWeight3 + lp2.weight;
                    boolean useExcessSpace = lp2.height == 0 && lp2.weight > 0.0f;
                    if (heightMode4 != 1073741824 || !useExcessSpace) {
                        if (useExcessSpace) {
                            lp2.height = -2;
                        }
                        childState4 = maxWidth;
                        lp = lp2;
                        heightMode2 = heightMode4;
                        heightMode3 = margin;
                        count6 = count8;
                        alternativeMaxWidth4 = alternativeMaxWidth6;
                        alternativeMaxWidth5 = weightedMaxWidth3;
                        count7 = 1073741824;
                        measureChildBeforeLayout(child2, i2, widthMeasureSpec, 0, heightMeasureSpec, totalWeight4 == 0.0f ? this.mTotalLength : 0);
                        int childHeight2 = child2.getMeasuredHeight();
                        if (useExcessSpace) {
                            lp.height = 0;
                            consumedExcessSpace += childHeight2;
                        }
                        int totalLength = this.mTotalLength;
                        child = child2;
                        this.mTotalLength = Math.max(totalLength, totalLength + childHeight2 + lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
                        if (useLargestChild2) {
                            largestChildHeight2 = Math.max(childHeight2, largestChildHeight2);
                        } else {
                            largestChildHeight2 = largestChildHeight2;
                        }
                    } else {
                        int totalLength2 = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength2, lp2.topMargin + totalLength2 + lp2.bottomMargin);
                        skippedMeasure = true;
                        lp = lp2;
                        alternativeMaxWidth4 = alternativeMaxWidth6;
                        childState4 = maxWidth;
                        alternativeMaxWidth5 = weightedMaxWidth3;
                        child = child2;
                        heightMode2 = heightMode4;
                        heightMode3 = margin;
                        count6 = count8;
                        count7 = 1073741824;
                    }
                    if (baselineChildIndex2 >= 0 && baselineChildIndex2 == i2 + 1) {
                        this.mBaselineChildTop = this.mTotalLength;
                    }
                    if (i2 >= baselineChildIndex2 || lp.weight <= 0.0f) {
                        boolean matchWidthLocally2 = false;
                        if (widthMode != count7) {
                            i = -1;
                            if (lp.width == -1) {
                                matchWidth = true;
                                matchWidthLocally2 = true;
                            }
                        } else {
                            i = -1;
                        }
                        int margin2 = lp.leftMargin + lp.rightMargin;
                        int measuredWidth = child.getMeasuredWidth() + margin2;
                        int maxWidth2 = Math.max(heightMode3, measuredWidth);
                        int childState5 = combineMeasuredStates(childState4, child.getMeasuredState());
                        boolean allFillParent3 = allFillParent2 && lp.width == i;
                        if (lp.weight > 0.0f) {
                            allFillParent2 = allFillParent3;
                            weightedMaxWidth2 = Math.max(alternativeMaxWidth5, matchWidthLocally2 ? margin2 : measuredWidth);
                        } else {
                            weightedMaxWidth2 = alternativeMaxWidth5;
                            allFillParent2 = allFillParent3;
                            alternativeMaxWidth4 = Math.max(alternativeMaxWidth4, matchWidthLocally2 ? margin2 : measuredWidth);
                        }
                        i2 += getChildrenSkipCount(child, i2);
                        margin = maxWidth2;
                        maxWidth = childState5;
                        weightedMaxWidth3 = weightedMaxWidth2;
                        totalWeight3 = totalWeight4;
                        alternativeMaxWidth6 = alternativeMaxWidth4;
                    } else {
                        throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                    }
                }
                i2++;
                heightMode4 = heightMode2;
                count8 = count6;
            } else {
                int childState6 = maxWidth;
                int maxWidth3 = margin;
                if (nonSkippedChildCount > 0) {
                    count = count8;
                    if (hasDividerBeforeChildAt(count)) {
                        this.mTotalLength += this.mDividerHeight;
                    }
                } else {
                    count = count8;
                }
                if (useLargestChild2) {
                    heightMode = heightMode4;
                    if (heightMode == Integer.MIN_VALUE || heightMode == 0) {
                        this.mTotalLength = 0;
                        int i4 = 0;
                        while (i4 < count) {
                            View child3 = getVirtualChildAt(i4);
                            if (child3 == null) {
                                this.mTotalLength += measureNullChild(i4);
                                childState3 = childState6;
                            } else if (child3.getVisibility() == i3) {
                                i4 += getChildrenSkipCount(child3, i4);
                                childState3 = childState6;
                            } else {
                                LayoutParams lp3 = (LayoutParams) child3.getLayoutParams();
                                int totalLength3 = this.mTotalLength;
                                childState3 = childState6;
                                this.mTotalLength = Math.max(totalLength3, totalLength3 + largestChildHeight2 + lp3.topMargin + lp3.bottomMargin + getNextLocationOffset(child3));
                            }
                            i4++;
                            childState6 = childState3;
                            i3 = 8;
                        }
                        childState = childState6;
                    } else {
                        childState = childState6;
                    }
                } else {
                    childState = childState6;
                    heightMode = heightMode4;
                }
                this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
                int heightSizeAndState = resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), heightMeasureSpec, 0);
                int heightSize = heightSizeAndState & 16777215;
                int remainingExcess3 = (heightSize - this.mTotalLength) + (this.mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
                if (skippedMeasure) {
                    totalWeight = totalWeight3;
                } else if ((sRemeasureWeightedChildren || remainingExcess3 != 0) && totalWeight3 > 0.0f) {
                    totalWeight = totalWeight3;
                } else {
                    int alternativeMaxWidth7 = Math.max(alternativeMaxWidth6, weightedMaxWidth3);
                    if (!useLargestChild2 || heightMode == 1073741824) {
                        alternativeMaxWidth2 = alternativeMaxWidth7;
                    } else {
                        int i5 = 0;
                        while (i5 < count) {
                            View child4 = getVirtualChildAt(i5);
                            if (child4 != null) {
                                weightedMaxWidth = weightedMaxWidth3;
                                alternativeMaxWidth3 = alternativeMaxWidth7;
                                if (child4.getVisibility() == 8) {
                                    totalWeight2 = totalWeight3;
                                } else if (((LayoutParams) child4.getLayoutParams()).weight > 0.0f) {
                                    totalWeight2 = totalWeight3;
                                    child4.measure(View.MeasureSpec.makeMeasureSpec(child4.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(largestChildHeight2, 1073741824));
                                } else {
                                    totalWeight2 = totalWeight3;
                                }
                            } else {
                                weightedMaxWidth = weightedMaxWidth3;
                                alternativeMaxWidth3 = alternativeMaxWidth7;
                                totalWeight2 = totalWeight3;
                            }
                            i5++;
                            alternativeMaxWidth7 = alternativeMaxWidth3;
                            heightSize = heightSize;
                            weightedMaxWidth3 = weightedMaxWidth;
                            totalWeight3 = totalWeight2;
                        }
                        alternativeMaxWidth2 = alternativeMaxWidth7;
                    }
                    count2 = count;
                    alternativeMaxWidth = alternativeMaxWidth2;
                    childState2 = childState;
                    remainingExcess = widthMeasureSpec;
                    if (!allFillParent2 && widthMode != 1073741824) {
                        maxWidth3 = alternativeMaxWidth;
                    }
                    setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth3 + this.mPaddingLeft + this.mPaddingRight, getSuggestedMinimumWidth()), remainingExcess, childState2), heightSizeAndState);
                    if (!matchWidth) {
                        forceUniformWidth(count2, heightMeasureSpec);
                        return;
                    }
                    return;
                }
                float totalWeight5 = this.mWeightSum;
                if (totalWeight5 <= 0.0f) {
                    totalWeight5 = totalWeight;
                }
                float remainingWeightSum = totalWeight5;
                this.mTotalLength = 0;
                int i6 = 0;
                alternativeMaxWidth = alternativeMaxWidth6;
                childState2 = childState;
                while (i6 < count) {
                    View child5 = getVirtualChildAt(i6);
                    if (child5 != null) {
                        useLargestChild = useLargestChild2;
                        baselineChildIndex = baselineChildIndex2;
                        if (child5.getVisibility() == 8) {
                            count3 = count;
                            largestChildHeight = largestChildHeight2;
                            count4 = remainingExcess3;
                        } else {
                            LayoutParams lp4 = (LayoutParams) child5.getLayoutParams();
                            float childWeight = lp4.weight;
                            if (childWeight > 0.0f) {
                                count3 = count;
                                int share = (int) ((((float) remainingExcess3) * childWeight) / remainingWeightSum);
                                int remainingExcess4 = remainingExcess3 - share;
                                float remainingWeightSum2 = remainingWeightSum - childWeight;
                                if (this.mUseLargestChild && heightMode != 1073741824) {
                                    childHeight = largestChildHeight2;
                                } else if (lp4.height != 0 || (this.mAllowInconsistentMeasurement && heightMode != 1073741824)) {
                                    childHeight = child5.getMeasuredHeight() + share;
                                } else {
                                    childHeight = share;
                                }
                                largestChildHeight = largestChildHeight2;
                                child5.measure(getChildMeasureSpec(widthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp4.leftMargin + lp4.rightMargin, lp4.width), View.MeasureSpec.makeMeasureSpec(Math.max(0, childHeight), 1073741824));
                                childState2 = combineMeasuredStates(childState2, child5.getMeasuredState() & -256);
                                remainingWeightSum = remainingWeightSum2;
                                count5 = remainingExcess4;
                            } else {
                                count3 = count;
                                largestChildHeight = largestChildHeight2;
                                count5 = remainingExcess3;
                            }
                            int margin3 = lp4.leftMargin + lp4.rightMargin;
                            int measuredWidth2 = child5.getMeasuredWidth() + margin3;
                            maxWidth3 = Math.max(maxWidth3, measuredWidth2);
                            if (widthMode != 1073741824) {
                                remainingExcess2 = count5;
                                if (lp4.width == -1) {
                                    matchWidthLocally = true;
                                    int alternativeMaxWidth8 = Math.max(alternativeMaxWidth, !matchWidthLocally ? margin3 : measuredWidth2);
                                    if (!allFillParent2) {
                                        if (lp4.width == -1) {
                                            allFillParent = true;
                                            int totalLength4 = this.mTotalLength;
                                            this.mTotalLength = Math.max(totalLength4, totalLength4 + child5.getMeasuredHeight() + lp4.topMargin + lp4.bottomMargin + getNextLocationOffset(child5));
                                            allFillParent2 = allFillParent;
                                            remainingWeightSum = remainingWeightSum;
                                            count4 = remainingExcess2;
                                            alternativeMaxWidth = alternativeMaxWidth8;
                                        }
                                    }
                                    allFillParent = false;
                                    int totalLength42 = this.mTotalLength;
                                    this.mTotalLength = Math.max(totalLength42, totalLength42 + child5.getMeasuredHeight() + lp4.topMargin + lp4.bottomMargin + getNextLocationOffset(child5));
                                    allFillParent2 = allFillParent;
                                    remainingWeightSum = remainingWeightSum;
                                    count4 = remainingExcess2;
                                    alternativeMaxWidth = alternativeMaxWidth8;
                                }
                            } else {
                                remainingExcess2 = count5;
                            }
                            matchWidthLocally = false;
                            int alternativeMaxWidth82 = Math.max(alternativeMaxWidth, !matchWidthLocally ? margin3 : measuredWidth2);
                            if (!allFillParent2) {
                            }
                            allFillParent = false;
                            int totalLength422 = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength422, totalLength422 + child5.getMeasuredHeight() + lp4.topMargin + lp4.bottomMargin + getNextLocationOffset(child5));
                            allFillParent2 = allFillParent;
                            remainingWeightSum = remainingWeightSum;
                            count4 = remainingExcess2;
                            alternativeMaxWidth = alternativeMaxWidth82;
                        }
                    } else {
                        count3 = count;
                        largestChildHeight = largestChildHeight2;
                        count4 = remainingExcess3;
                        useLargestChild = useLargestChild2;
                        baselineChildIndex = baselineChildIndex2;
                    }
                    i6++;
                    remainingExcess3 = count4;
                    largestChildHeight2 = largestChildHeight;
                    useLargestChild2 = useLargestChild;
                    baselineChildIndex2 = baselineChildIndex;
                    count = count3;
                }
                count2 = count;
                remainingExcess = widthMeasureSpec;
                this.mTotalLength += this.mPaddingTop + this.mPaddingBottom;
                maxWidth3 = alternativeMaxWidth;
                setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth3 + this.mPaddingLeft + this.mPaddingRight, getSuggestedMinimumWidth()), remainingExcess, childState2), heightSizeAndState);
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

    /* JADX INFO: Multiple debug info for r14v1 boolean: [D('skippedMeasure' boolean), D('useLargestChild' boolean)] */
    /* JADX INFO: Multiple debug info for r2v2 int: [D('alternativeMaxHeight' int), D('largestChildWidth' int)] */
    /* JADX INFO: Multiple debug info for r3v33 int: [D('totalLength' int), D('maxHeight' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x055e  */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x0596  */
    /* JADX WARNING: Removed duplicated region for block: B:240:0x063e  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x0646  */
    public void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int childState;
        int widthMode;
        int widthMode2;
        int widthSizeAndState;
        int childState2;
        int alternativeMaxHeight;
        int widthSizeAndState2;
        int maxHeight;
        int childState3;
        int remainingExcess;
        float totalWeight;
        int largestChildWidth;
        int widthMode3;
        int widthSizeAndState3;
        int count;
        int alternativeMaxHeight2;
        boolean allFillParent;
        int childState4;
        int childWidth;
        int remainingExcess2;
        int remainingExcess3;
        int widthSize;
        int alternativeMaxHeight3;
        int i;
        int maxHeight2;
        int weightedMaxHeight;
        boolean baselineAligned;
        int weightedMaxHeight2;
        int maxHeight3;
        int alternativeMaxHeight4;
        int weightedMaxHeight3;
        int widthMode4;
        int i2;
        int largestChildWidth2;
        LayoutParams lp;
        int margin;
        int largestChildWidth3;
        int weightedMaxHeight4;
        int alternativeMaxHeight5;
        int maxHeight4;
        this.mTotalLength = 0;
        int maxHeight5 = 0;
        int count2 = getVirtualChildCount();
        int widthMode5 = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (this.mMaxAscent == null || this.mMaxDescent == null) {
            this.mMaxAscent = new int[4];
            this.mMaxDescent = new int[4];
        }
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
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
        boolean useLargestChild = this.mUseLargestChild;
        boolean isExactly = widthMode5 == 1073741824;
        int usedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        int childState5 = 0;
        int alternativeMaxHeight6 = 0;
        int weightedMaxHeight5 = 0;
        float totalWeight2 = 0.0f;
        int i3 = 0;
        boolean allFillParent2 = true;
        int childHeight = Integer.MIN_VALUE;
        boolean matchHeight = false;
        while (i3 < count2) {
            View child = getVirtualChildAt(i3);
            if (child == null) {
                this.mTotalLength += measureNullChild(i3);
                baselineAligned = baselineAligned2;
                weightedMaxHeight2 = childState5;
                alternativeMaxHeight6 = alternativeMaxHeight6;
                weightedMaxHeight = widthMode5;
            } else if (child.getVisibility() == 8) {
                i3 += getChildrenSkipCount(child, i3);
                baselineAligned = baselineAligned2;
                weightedMaxHeight2 = childState5;
                alternativeMaxHeight6 = alternativeMaxHeight6;
                maxHeight5 = maxHeight5;
                weightedMaxHeight = widthMode5;
            } else {
                nonSkippedChildCount++;
                if (hasDividerBeforeChildAt(i3)) {
                    this.mTotalLength += this.mDividerWidth;
                }
                LayoutParams lp2 = (LayoutParams) child.getLayoutParams();
                float totalWeight3 = totalWeight2 + lp2.weight;
                boolean useExcessSpace = lp2.width == 0 && lp2.weight > 0.0f;
                if (widthMode5 != 1073741824 || !useExcessSpace) {
                    if (useExcessSpace) {
                        lp2.width = -2;
                    }
                    weightedMaxHeight3 = alternativeMaxHeight6;
                    alternativeMaxHeight4 = maxHeight5;
                    maxHeight3 = weightedMaxHeight5;
                    i2 = i3;
                    baselineAligned = baselineAligned2;
                    weightedMaxHeight = widthMode5;
                    widthMode4 = -1;
                    measureChildBeforeLayout(child, i3, widthMeasureSpec, totalWeight3 == 0.0f ? this.mTotalLength : 0, heightMeasureSpec, 0);
                    int childWidth2 = child.getMeasuredWidth();
                    if (useExcessSpace) {
                        lp = lp2;
                        lp.width = 0;
                        usedExcessSpace += childWidth2;
                    } else {
                        lp = lp2;
                    }
                    if (isExactly) {
                        this.mTotalLength += lp.leftMargin + childWidth2 + lp.rightMargin + getNextLocationOffset(child);
                    } else {
                        int totalLength = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength, totalLength + childWidth2 + lp.leftMargin + lp.rightMargin + getNextLocationOffset(child));
                    }
                    if (useLargestChild) {
                        largestChildWidth2 = Math.max(childWidth2, childHeight);
                    } else {
                        largestChildWidth2 = childHeight;
                    }
                } else {
                    if (isExactly) {
                        maxHeight4 = weightedMaxHeight5;
                        this.mTotalLength += lp2.leftMargin + lp2.rightMargin;
                    } else {
                        maxHeight4 = weightedMaxHeight5;
                        int totalLength2 = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength2, lp2.leftMargin + totalLength2 + lp2.rightMargin);
                    }
                    if (baselineAligned2) {
                        child.measure(View.MeasureSpec.makeSafeMeasureSpec(View.MeasureSpec.getSize(widthMeasureSpec), 0), View.MeasureSpec.makeSafeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), 0));
                        lp = lp2;
                        largestChildWidth2 = childHeight;
                        i2 = i3;
                        baselineAligned = baselineAligned2;
                        weightedMaxHeight3 = alternativeMaxHeight6;
                        alternativeMaxHeight4 = maxHeight5;
                        maxHeight3 = maxHeight4;
                        weightedMaxHeight = widthMode5;
                        widthMode4 = -1;
                    } else {
                        skippedMeasure = true;
                        lp = lp2;
                        largestChildWidth2 = childHeight;
                        i2 = i3;
                        baselineAligned = baselineAligned2;
                        weightedMaxHeight3 = alternativeMaxHeight6;
                        alternativeMaxHeight4 = maxHeight5;
                        maxHeight3 = maxHeight4;
                        weightedMaxHeight = widthMode5;
                        widthMode4 = -1;
                    }
                }
                boolean matchHeightLocally = false;
                if (heightMode != 1073741824 && lp.height == widthMode4) {
                    matchHeight = true;
                    matchHeightLocally = true;
                }
                int margin2 = lp.topMargin + lp.bottomMargin;
                int childHeight2 = child.getMeasuredHeight() + margin2;
                int childState6 = combineMeasuredStates(childState5, child.getMeasuredState());
                if (baselineAligned) {
                    int childBaseline = child.getBaseline();
                    if (childBaseline != widthMode4) {
                        int index = ((((lp.gravity < 0 ? this.mGravity : lp.gravity) & 112) >> 4) & -2) >> 1;
                        largestChildWidth3 = largestChildWidth2;
                        maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                        margin = margin2;
                        maxDescent[index] = Math.max(maxDescent[index], childHeight2 - childBaseline);
                    } else {
                        largestChildWidth3 = largestChildWidth2;
                        margin = margin2;
                    }
                } else {
                    largestChildWidth3 = largestChildWidth2;
                    margin = margin2;
                }
                int maxHeight6 = Math.max(maxHeight3, childHeight2);
                boolean allFillParent3 = allFillParent2 && lp.height == -1;
                if (lp.weight > 0.0f) {
                    weightedMaxHeight4 = Math.max(weightedMaxHeight3, matchHeightLocally ? margin : childHeight2);
                    alternativeMaxHeight5 = alternativeMaxHeight4;
                } else {
                    alternativeMaxHeight5 = Math.max(alternativeMaxHeight4, matchHeightLocally ? margin : childHeight2);
                    weightedMaxHeight4 = weightedMaxHeight3;
                }
                allFillParent2 = allFillParent3;
                totalWeight2 = totalWeight3;
                childHeight = largestChildWidth3;
                weightedMaxHeight5 = maxHeight6;
                maxHeight5 = alternativeMaxHeight5;
                alternativeMaxHeight6 = weightedMaxHeight4;
                weightedMaxHeight2 = childState6;
                i3 = i2 + getChildrenSkipCount(child, i2);
            }
            i3++;
            childState5 = weightedMaxHeight2;
            baselineAligned2 = baselineAligned;
            widthMode5 = weightedMaxHeight;
        }
        int largestChildWidth4 = childHeight;
        if (nonSkippedChildCount > 0 && hasDividerBeforeChildAt(count2)) {
            this.mTotalLength += this.mDividerWidth;
        }
        if (maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1 && maxAscent[3] == -1) {
            childState = childState5;
        } else {
            childState = childState5;
            weightedMaxHeight5 = Math.max(weightedMaxHeight5, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent[3], Math.max(maxDescent[0], Math.max(maxDescent[1], maxDescent[2]))));
        }
        if (useLargestChild) {
            widthMode2 = widthMode5;
            if (widthMode2 == Integer.MIN_VALUE || widthMode2 == 0) {
                this.mTotalLength = 0;
                int i4 = 0;
                while (i4 < count2) {
                    View child2 = getVirtualChildAt(i4);
                    if (child2 == null) {
                        this.mTotalLength += measureNullChild(i4);
                        maxHeight2 = weightedMaxHeight5;
                        i = i4;
                    } else if (child2.getVisibility() == 8) {
                        maxHeight2 = weightedMaxHeight5;
                        i = i4 + getChildrenSkipCount(child2, i4);
                    } else {
                        if (shouldMeasureChildDivider() && hasDividerBeforeChildAt(i4)) {
                            this.mTotalLength += this.mDividerWidth;
                        }
                        LayoutParams lp3 = (LayoutParams) child2.getLayoutParams();
                        if (isExactly) {
                            maxHeight2 = weightedMaxHeight5;
                            i = i4;
                            this.mTotalLength += lp3.leftMargin + largestChildWidth4 + lp3.rightMargin + getNextLocationOffset(child2);
                        } else {
                            maxHeight2 = weightedMaxHeight5;
                            i = i4;
                            int maxHeight7 = this.mTotalLength;
                            this.mTotalLength = Math.max(maxHeight7, maxHeight7 + largestChildWidth4 + lp3.leftMargin + lp3.rightMargin + getNextLocationOffset(child2));
                        }
                    }
                    i4 = i + 1;
                    weightedMaxHeight5 = maxHeight2;
                }
                widthMode = weightedMaxHeight5;
            } else {
                widthMode = weightedMaxHeight5;
            }
        } else {
            widthMode2 = widthMode5;
            widthMode = weightedMaxHeight5;
        }
        this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
        int widthSizeAndState4 = resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, 0);
        int widthSize2 = widthSizeAndState4 & 16777215;
        int remainingExcess4 = (widthSize2 - this.mTotalLength) + (this.mAllowInconsistentMeasurement ? 0 : usedExcessSpace);
        if (skippedMeasure) {
            totalWeight = totalWeight2;
            remainingExcess = remainingExcess4;
        } else if ((sRemeasureWeightedChildren || remainingExcess4 != 0) && totalWeight2 > 0.0f) {
            totalWeight = totalWeight2;
            remainingExcess = remainingExcess4;
        } else {
            int alternativeMaxHeight7 = Math.max(maxHeight5, alternativeMaxHeight6);
            if (!useLargestChild || widthMode2 == 1073741824) {
                alternativeMaxHeight = alternativeMaxHeight7;
                remainingExcess2 = remainingExcess4;
            } else {
                int i5 = 0;
                while (i5 < count2) {
                    View child3 = getVirtualChildAt(i5);
                    if (child3 != null) {
                        alternativeMaxHeight3 = alternativeMaxHeight7;
                        widthSize = widthSize2;
                        if (child3.getVisibility() == 8) {
                            remainingExcess3 = remainingExcess4;
                        } else if (((LayoutParams) child3.getLayoutParams()).weight > 0.0f) {
                            remainingExcess3 = remainingExcess4;
                            child3.measure(View.MeasureSpec.makeMeasureSpec(largestChildWidth4, 1073741824), View.MeasureSpec.makeMeasureSpec(child3.getMeasuredHeight(), 1073741824));
                        } else {
                            remainingExcess3 = remainingExcess4;
                        }
                    } else {
                        alternativeMaxHeight3 = alternativeMaxHeight7;
                        widthSize = widthSize2;
                        remainingExcess3 = remainingExcess4;
                    }
                    i5++;
                    alternativeMaxHeight7 = alternativeMaxHeight3;
                    totalWeight2 = totalWeight2;
                    widthSize2 = widthSize;
                    remainingExcess4 = remainingExcess3;
                }
                alternativeMaxHeight = alternativeMaxHeight7;
                remainingExcess2 = remainingExcess4;
            }
            widthSizeAndState = widthSizeAndState4;
            maxHeight = widthMode;
            childState3 = childState;
            widthSizeAndState2 = heightMeasureSpec;
            childState2 = count2;
            if (!allFillParent2 && heightMode != 1073741824) {
                maxHeight = alternativeMaxHeight;
            }
            setMeasuredDimension(widthSizeAndState | (-16777216 & childState3), resolveSizeAndState(Math.max(maxHeight + this.mPaddingTop + this.mPaddingBottom, getSuggestedMinimumHeight()), widthSizeAndState2, childState3 << 16));
            if (!matchHeight) {
                forceUniformHeight(childState2, widthMeasureSpec);
                return;
            }
            return;
        }
        float remainingWeightSum = this.mWeightSum;
        if (remainingWeightSum <= 0.0f) {
            remainingWeightSum = totalWeight;
        }
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent[3] = -1;
        maxDescent[2] = -1;
        maxDescent[1] = -1;
        maxDescent[0] = -1;
        maxHeight = -1;
        this.mTotalLength = 0;
        int i6 = 0;
        int alternativeMaxHeight8 = maxHeight5;
        childState3 = childState;
        int remainingExcess5 = remainingExcess;
        while (i6 < count2) {
            View child4 = getVirtualChildAt(i6);
            if (child4 != null) {
                count = count2;
                if (child4.getVisibility() == 8) {
                    largestChildWidth = largestChildWidth4;
                    widthMode3 = widthMode2;
                    widthSizeAndState3 = widthSizeAndState4;
                } else {
                    if (hasDividerBeforeChildAt(i6)) {
                        this.mTotalLength += this.mDividerWidth;
                    }
                    LayoutParams lp4 = (LayoutParams) child4.getLayoutParams();
                    float childWeight = lp4.weight;
                    if (childWeight > 0.0f) {
                        widthSizeAndState3 = widthSizeAndState4;
                        int share = (int) ((((float) remainingExcess5) * childWeight) / remainingWeightSum);
                        remainingExcess5 -= share;
                        float remainingWeightSum2 = remainingWeightSum - childWeight;
                        if (this.mUseLargestChild && widthMode2 != 1073741824) {
                            childWidth = largestChildWidth4;
                        } else if (lp4.width != 0 || (this.mAllowInconsistentMeasurement && widthMode2 != 1073741824)) {
                            childWidth = child4.getMeasuredWidth() + share;
                        } else {
                            childWidth = share;
                        }
                        largestChildWidth = largestChildWidth4;
                        widthMode3 = widthMode2;
                        child4.measure(View.MeasureSpec.makeMeasureSpec(Math.max(0, childWidth), 1073741824), getChildMeasureSpec(heightMeasureSpec, this.mPaddingTop + this.mPaddingBottom + lp4.topMargin + lp4.bottomMargin, lp4.height));
                        childState3 = combineMeasuredStates(childState3, child4.getMeasuredState() & -16777216);
                        remainingWeightSum = remainingWeightSum2;
                    } else {
                        largestChildWidth = largestChildWidth4;
                        widthMode3 = widthMode2;
                        widthSizeAndState3 = widthSizeAndState4;
                    }
                    if (isExactly) {
                        this.mTotalLength += child4.getMeasuredWidth() + lp4.leftMargin + lp4.rightMargin + getNextLocationOffset(child4);
                    } else {
                        int totalLength3 = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength3, child4.getMeasuredWidth() + totalLength3 + lp4.leftMargin + lp4.rightMargin + getNextLocationOffset(child4));
                    }
                    boolean matchHeightLocally2 = heightMode != 1073741824 && lp4.height == -1;
                    int margin3 = lp4.topMargin + lp4.bottomMargin;
                    int childHeight3 = child4.getMeasuredHeight() + margin3;
                    maxHeight = Math.max(maxHeight, childHeight3);
                    int alternativeMaxHeight9 = Math.max(alternativeMaxHeight8, matchHeightLocally2 ? margin3 : childHeight3);
                    if (allFillParent2) {
                        alternativeMaxHeight2 = alternativeMaxHeight9;
                        if (lp4.height == -1) {
                            allFillParent = true;
                            if (!baselineAligned2) {
                                int childBaseline2 = child4.getBaseline();
                                allFillParent2 = allFillParent;
                                if (childBaseline2 != -1) {
                                    int index2 = ((((lp4.gravity < 0 ? this.mGravity : lp4.gravity) & 112) >> 4) & -2) >> 1;
                                    maxAscent[index2] = Math.max(maxAscent[index2], childBaseline2);
                                    childState4 = childState3;
                                    maxDescent[index2] = Math.max(maxDescent[index2], childHeight3 - childBaseline2);
                                } else {
                                    childState4 = childState3;
                                }
                            } else {
                                allFillParent2 = allFillParent;
                                childState4 = childState3;
                            }
                            remainingWeightSum = remainingWeightSum;
                            alternativeMaxHeight8 = alternativeMaxHeight2;
                            childState3 = childState4;
                        }
                    } else {
                        alternativeMaxHeight2 = alternativeMaxHeight9;
                    }
                    allFillParent = false;
                    if (!baselineAligned2) {
                    }
                    remainingWeightSum = remainingWeightSum;
                    alternativeMaxHeight8 = alternativeMaxHeight2;
                    childState3 = childState4;
                }
            } else {
                largestChildWidth = largestChildWidth4;
                widthMode3 = widthMode2;
                widthSizeAndState3 = widthSizeAndState4;
                count = count2;
            }
            i6++;
            useLargestChild = useLargestChild;
            count2 = count;
            widthSizeAndState4 = widthSizeAndState3;
            widthMode2 = widthMode3;
            largestChildWidth4 = largestChildWidth;
        }
        widthSizeAndState = widthSizeAndState4;
        childState2 = count2;
        widthSizeAndState2 = heightMeasureSpec;
        this.mTotalLength += this.mPaddingLeft + this.mPaddingRight;
        if (!(maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1 && maxAscent[3] == -1)) {
            maxHeight = Math.max(maxHeight, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent[3], Math.max(maxDescent[0], Math.max(maxDescent[1], maxDescent[2]))));
        }
        alternativeMaxHeight = alternativeMaxHeight8;
        maxHeight = alternativeMaxHeight;
        setMeasuredDimension(widthSizeAndState | (-16777216 & childState3), resolveSizeAndState(Math.max(maxHeight + this.mPaddingTop + this.mPaddingBottom, getSuggestedMinimumHeight()), widthSizeAndState2, childState3 << 16));
        if (!matchHeight) {
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldMeasureChildDivider() {
        return false;
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
    @Override // android.view.ViewGroup, android.view.View
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
        int gravity;
        int childLeft;
        int paddingLeft2 = this.mPaddingLeft;
        int width = right - left;
        int childRight = width - this.mPaddingRight;
        int childSpace = (width - paddingLeft2) - this.mPaddingRight;
        int count = getVirtualChildCount();
        int i = this.mGravity;
        int majorGravity = i & 112;
        int minorGravity = i & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (majorGravity == 16) {
            childTop = this.mPaddingTop + (((bottom - top) - this.mTotalLength) / 2);
        } else if (majorGravity != 80) {
            childTop = this.mPaddingTop;
        } else {
            childTop = ((this.mPaddingTop + bottom) - top) - this.mTotalLength;
        }
        int i2 = 0;
        while (i2 < count) {
            View child = getVirtualChildAt(i2);
            if (child == null) {
                childTop += measureNullChild(i2);
                paddingLeft = paddingLeft2;
            } else if (child.getVisibility() != 8) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int gravity2 = lp.gravity;
                if (gravity2 < 0) {
                    gravity = minorGravity;
                } else {
                    gravity = gravity2;
                }
                int absoluteGravity = Gravity.getAbsoluteGravity(gravity, getLayoutDirection()) & 7;
                if (absoluteGravity == 1) {
                    childLeft = ((((childSpace - childWidth) / 2) + paddingLeft2) + lp.leftMargin) - lp.rightMargin;
                } else if (absoluteGravity != 5) {
                    childLeft = lp.leftMargin + paddingLeft2;
                } else {
                    childLeft = (childRight - childWidth) - lp.rightMargin;
                }
                if (hasDividerBeforeChildAt(i2)) {
                    childTop += this.mDividerHeight;
                }
                int childTop2 = childTop + lp.topMargin;
                paddingLeft = paddingLeft2;
                setChildFrame(child, childLeft, childTop2 + getLocationOffset(child), childWidth, childHeight);
                int childTop3 = childTop2 + childHeight + lp.bottomMargin + getNextLocationOffset(child);
                i2 += getChildrenSkipCount(child, i2);
                childTop = childTop3;
            } else {
                paddingLeft = paddingLeft2;
            }
            i2++;
            paddingLeft2 = paddingLeft;
        }
    }

    @Override // android.view.View
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
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b9  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00fb  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x010e  */
    public void layoutHorizontal(int left, int top, int right, int bottom) {
        int childLeft;
        int dir;
        int start;
        int[] maxAscent;
        int[] maxDescent;
        int paddingTop;
        int count;
        int height;
        int layoutDirection;
        int childBaseline;
        int gravity;
        int gravity2;
        int gravity3;
        int childTop;
        boolean isLayoutRtl = isLayoutRtl();
        int paddingTop2 = this.mPaddingTop;
        int height2 = bottom - top;
        int childBottom = height2 - this.mPaddingBottom;
        int childSpace = (height2 - paddingTop2) - this.mPaddingBottom;
        int count2 = getVirtualChildCount();
        int i = this.mGravity;
        int majorGravity = i & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int minorGravity = i & 112;
        boolean baselineAligned = this.mBaselineAligned;
        int[] maxAscent2 = this.mMaxAscent;
        int[] maxDescent2 = this.mMaxDescent;
        int layoutDirection2 = getLayoutDirection();
        int absoluteGravity = Gravity.getAbsoluteGravity(majorGravity, layoutDirection2);
        if (absoluteGravity == 1) {
            childLeft = this.mPaddingLeft + (((right - left) - this.mTotalLength) / 2);
        } else if (absoluteGravity != 5) {
            childLeft = this.mPaddingLeft;
        } else {
            childLeft = ((this.mPaddingLeft + right) - left) - this.mTotalLength;
        }
        if (isLayoutRtl) {
            start = count2 - 1;
            dir = -1;
        } else {
            start = 0;
            dir = 1;
        }
        int i2 = 0;
        while (i2 < count2) {
            int childIndex = start + (dir * i2);
            View child = getVirtualChildAt(childIndex);
            if (child == null) {
                childLeft += measureNullChild(childIndex);
                layoutDirection = layoutDirection2;
                maxDescent = maxDescent2;
                maxAscent = maxAscent2;
                paddingTop = paddingTop2;
                height = height2;
                count = count2;
            } else {
                layoutDirection = layoutDirection2;
                if (child.getVisibility() != 8) {
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if (baselineAligned) {
                        height = height2;
                        if (lp.height != -1) {
                            childBaseline = child.getBaseline();
                            gravity = lp.gravity;
                            if (gravity >= 0) {
                                gravity2 = minorGravity;
                            } else {
                                gravity2 = gravity;
                            }
                            gravity3 = gravity2 & 112;
                            count = count2;
                            if (gravity3 != 16) {
                                childTop = ((((childSpace - childHeight) / 2) + paddingTop2) + lp.topMargin) - lp.bottomMargin;
                            } else if (gravity3 == 48) {
                                int childTop2 = lp.topMargin + paddingTop2;
                                childTop = childBaseline != -1 ? childTop2 + (maxAscent2[1] - childBaseline) : childTop2;
                            } else if (gravity3 != 80) {
                                childTop = paddingTop2;
                            } else {
                                int childTop3 = (childBottom - childHeight) - lp.bottomMargin;
                                if (childBaseline != -1) {
                                    childTop = childTop3 - (maxDescent2[2] - (child.getMeasuredHeight() - childBaseline));
                                } else {
                                    childTop = childTop3;
                                }
                            }
                            if (hasDividerBeforeChildAt(childIndex)) {
                                childLeft += this.mDividerWidth;
                            }
                            int childLeft2 = childLeft + lp.leftMargin;
                            paddingTop = paddingTop2;
                            maxDescent = maxDescent2;
                            maxAscent = maxAscent2;
                            setChildFrame(child, childLeft2 + getLocationOffset(child), childTop, childWidth, childHeight);
                            int childLeft3 = childLeft2 + childWidth + lp.rightMargin + getNextLocationOffset(child);
                            i2 += getChildrenSkipCount(child, childIndex);
                            childLeft = childLeft3;
                        }
                    } else {
                        height = height2;
                    }
                    childBaseline = -1;
                    gravity = lp.gravity;
                    if (gravity >= 0) {
                    }
                    gravity3 = gravity2 & 112;
                    count = count2;
                    if (gravity3 != 16) {
                    }
                    if (hasDividerBeforeChildAt(childIndex)) {
                    }
                    int childLeft22 = childLeft + lp.leftMargin;
                    paddingTop = paddingTop2;
                    maxDescent = maxDescent2;
                    maxAscent = maxAscent2;
                    setChildFrame(child, childLeft22 + getLocationOffset(child), childTop, childWidth, childHeight);
                    int childLeft32 = childLeft22 + childWidth + lp.rightMargin + getNextLocationOffset(child);
                    i2 += getChildrenSkipCount(child, childIndex);
                    childLeft = childLeft32;
                } else {
                    maxDescent = maxDescent2;
                    maxAscent = maxAscent2;
                    paddingTop = paddingTop2;
                    height = height2;
                    count = count2;
                    i2 = i2;
                }
            }
            i2++;
            isLayoutRtl = isLayoutRtl;
            layoutDirection2 = layoutDirection;
            height2 = height;
            count2 = count;
            paddingTop2 = paddingTop;
            maxDescent2 = maxDescent;
            maxAscent2 = maxAscent;
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
        int i = this.mGravity;
        if ((8388615 & i) != gravity) {
            this.mGravity = (-8388616 & i) | gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setVerticalGravity(int verticalGravity) {
        int gravity = verticalGravity & 112;
        int i = this.mGravity;
        if ((i & 112) != gravity) {
            this.mGravity = (i & -113) | gravity;
            requestLayout();
        }
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        int i = this.mOrientation;
        if (i == 0) {
            return new LayoutParams(-2, -2);
        }
        if (i == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
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
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override // android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return LinearLayout.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
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
