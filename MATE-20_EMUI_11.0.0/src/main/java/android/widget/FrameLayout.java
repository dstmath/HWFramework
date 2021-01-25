package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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
import java.util.ArrayList;

@RemoteViews.RemoteView
public class FrameLayout extends ViewGroup {
    private static final int DEFAULT_CHILD_GRAVITY = 8388659;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingBottom;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingLeft;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingRight;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingTop;
    private final ArrayList<View> mMatchParentChildren;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "measurement")
    boolean mMeasureAllChildren;

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public static final int UNSPECIFIED_GRAVITY = -1;
        public int gravity = -1;

        public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<LayoutParams> {
            private int mLayout_gravityId;
            private boolean mPropertiesMapped = false;

            @Override // android.view.inspector.InspectionCompanion
            public void mapProperties(PropertyMapper propertyMapper) {
                this.mLayout_gravityId = propertyMapper.mapGravity("layout_gravity", 16842931);
                this.mPropertiesMapped = true;
            }

            public void readProperties(LayoutParams node, PropertyReader propertyReader) {
                if (this.mPropertiesMapped) {
                    propertyReader.readGravity(this.mLayout_gravityId, node.gravity);
                    return;
                }
                throw new InspectionCompanion.UninitializedPropertyMapException();
            }
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FrameLayout_Layout);
            this.gravity = a.getInt(0, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity2) {
            super(width, height);
            this.gravity = gravity2;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.MarginLayoutParams) source);
            this.gravity = source.gravity;
        }
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<FrameLayout> {
        private int mMeasureAllChildrenId;
        private boolean mPropertiesMapped = false;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mMeasureAllChildrenId = propertyMapper.mapBoolean("measureAllChildren", 16843018);
            this.mPropertiesMapped = true;
        }

        public void readProperties(FrameLayout node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mMeasureAllChildrenId, node.getMeasureAllChildren());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    public FrameLayout(Context context) {
        super(context);
        this.mMeasureAllChildren = false;
        this.mForegroundPaddingLeft = 0;
        this.mForegroundPaddingTop = 0;
        this.mForegroundPaddingRight = 0;
        this.mForegroundPaddingBottom = 0;
        this.mMatchParentChildren = new ArrayList<>(1);
    }

    public FrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMeasureAllChildren = false;
        this.mForegroundPaddingLeft = 0;
        this.mForegroundPaddingTop = 0;
        this.mForegroundPaddingRight = 0;
        this.mForegroundPaddingBottom = 0;
        this.mMatchParentChildren = new ArrayList<>(1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FrameLayout, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.FrameLayout, attrs, a, defStyleAttr, defStyleRes);
        if (a.getBoolean(0, false)) {
            setMeasureAllChildren(true);
        }
        a.recycle();
    }

    @Override // android.view.View
    @RemotableViewMethod
    public void setForegroundGravity(int foregroundGravity) {
        if (getForegroundGravity() != foregroundGravity) {
            super.setForegroundGravity(foregroundGravity);
            Drawable foreground = getForeground();
            if (getForegroundGravity() != 119 || foreground == null) {
                this.mForegroundPaddingLeft = 0;
                this.mForegroundPaddingTop = 0;
                this.mForegroundPaddingRight = 0;
                this.mForegroundPaddingBottom = 0;
            } else {
                Rect padding = new Rect();
                if (foreground.getPadding(padding)) {
                    this.mForegroundPaddingLeft = padding.left;
                    this.mForegroundPaddingTop = padding.top;
                    this.mForegroundPaddingRight = padding.right;
                    this.mForegroundPaddingBottom = padding.bottom;
                }
            }
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    /* access modifiers changed from: package-private */
    public int getPaddingLeftWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingLeft, this.mForegroundPaddingLeft);
        }
        return this.mPaddingLeft + this.mForegroundPaddingLeft;
    }

    /* access modifiers changed from: package-private */
    public int getPaddingRightWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingRight, this.mForegroundPaddingRight);
        }
        return this.mPaddingRight + this.mForegroundPaddingRight;
    }

    private int getPaddingTopWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingTop, this.mForegroundPaddingTop);
        }
        return this.mPaddingTop + this.mForegroundPaddingTop;
    }

    private int getPaddingBottomWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingBottom, this.mForegroundPaddingBottom);
        }
        return this.mPaddingBottom + this.mForegroundPaddingBottom;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int count = getChildCount();
        boolean measureMatchParentChildren = (View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && View.MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (this.mMeasureAllChildren || child.getVisibility() != 8) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int maxWidth2 = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                int maxHeight2 = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                int childState2 = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == -1 || lp.height == -1) {
                        this.mMatchParentChildren.add(child);
                    }
                }
                maxWidth = maxWidth2;
                maxHeight = maxHeight2;
                childState = childState2;
            }
        }
        int i2 = -1;
        int maxWidth3 = maxWidth + getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        int maxHeight3 = Math.max(maxHeight + getPaddingTopWithForeground() + getPaddingBottomWithForeground(), getSuggestedMinimumHeight());
        int maxWidth4 = Math.max(maxWidth3, getSuggestedMinimumWidth());
        Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight3 = Math.max(maxHeight3, drawable.getMinimumHeight());
            maxWidth4 = Math.max(maxWidth4, drawable.getMinimumWidth());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth4, widthMeasureSpec, childState), resolveSizeAndState(maxHeight3, heightMeasureSpec, childState << 16));
        int count2 = this.mMatchParentChildren.size();
        if (count2 > 1) {
            int i3 = 0;
            while (i3 < count2) {
                View child2 = this.mMatchParentChildren.get(i3);
                ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams) child2.getLayoutParams();
                if (lp2.width == i2) {
                    width = View.MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredWidth() - getPaddingLeftWithForeground()) - getPaddingRightWithForeground()) - lp2.leftMargin) - lp2.rightMargin), 1073741824);
                } else {
                    width = getChildMeasureSpec(widthMeasureSpec, getPaddingLeftWithForeground() + getPaddingRightWithForeground() + lp2.leftMargin + lp2.rightMargin, lp2.width);
                }
                if (lp2.height == i2) {
                    height = View.MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredHeight() - getPaddingTopWithForeground()) - getPaddingBottomWithForeground()) - lp2.topMargin) - lp2.bottomMargin), 1073741824);
                } else {
                    height = getChildMeasureSpec(heightMeasureSpec, getPaddingTopWithForeground() + getPaddingBottomWithForeground() + lp2.topMargin + lp2.bottomMargin, lp2.height);
                }
                child2.measure(width, height);
                i3++;
                i2 = -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false);
    }

    /* access modifiers changed from: package-private */
    public void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        int parentRight;
        int parentLeft;
        int count;
        int childLeft;
        int childTop;
        int count2 = getChildCount();
        int parentLeft2 = getPaddingLeftWithForeground();
        int parentRight2 = (right - left) - getPaddingRightWithForeground();
        int parentTop = getPaddingTopWithForeground();
        int parentBottom = (bottom - top) - getPaddingBottomWithForeground();
        int i = 0;
        while (i < count2) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == 8) {
                count = count2;
                parentLeft = parentLeft2;
                parentRight = parentRight2;
            } else {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }
                int verticalGravity = gravity & 112;
                int absoluteGravity = Gravity.getAbsoluteGravity(gravity, getLayoutDirection()) & 7;
                count = count2;
                if (absoluteGravity == 1) {
                    childLeft = (((((parentRight2 - parentLeft2) - width) / 2) + parentLeft2) + lp.leftMargin) - lp.rightMargin;
                } else if (absoluteGravity == 5 && !forceLeftGravity) {
                    childLeft = (parentRight2 - width) - lp.rightMargin;
                } else {
                    childLeft = lp.leftMargin + parentLeft2;
                }
                if (verticalGravity == 16) {
                    parentLeft = parentLeft2;
                    childTop = (((((parentBottom - parentTop) - height) / 2) + parentTop) + lp.topMargin) - lp.bottomMargin;
                } else if (verticalGravity == 48) {
                    parentLeft = parentLeft2;
                    childTop = parentTop + lp.topMargin;
                } else if (verticalGravity != 80) {
                    childTop = lp.topMargin + parentTop;
                    parentLeft = parentLeft2;
                } else {
                    parentLeft = parentLeft2;
                    childTop = (parentBottom - height) - lp.bottomMargin;
                }
                parentRight = parentRight2;
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
            i++;
            count2 = count;
            parentLeft2 = parentLeft;
            parentRight2 = parentRight;
        }
    }

    @RemotableViewMethod
    public void setMeasureAllChildren(boolean measureAll) {
        this.mMeasureAllChildren = measureAll;
    }

    @Deprecated
    public boolean getConsiderGoneChildrenWhenMeasuring() {
        return getMeasureAllChildren();
    }

    public boolean getMeasureAllChildren() {
        return this.mMeasureAllChildren;
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
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

    @Override // android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return FrameLayout.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("measurement:measureAllChildren", this.mMeasureAllChildren);
        encoder.addProperty("padding:foregroundPaddingLeft", this.mForegroundPaddingLeft);
        encoder.addProperty("padding:foregroundPaddingTop", this.mForegroundPaddingTop);
        encoder.addProperty("padding:foregroundPaddingRight", this.mForegroundPaddingRight);
        encoder.addProperty("padding:foregroundPaddingBottom", this.mForegroundPaddingBottom);
    }
}
