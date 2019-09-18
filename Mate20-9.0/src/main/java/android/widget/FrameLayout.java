package android.widget;

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
import android.widget.RemoteViews;
import com.android.internal.R;
import java.util.ArrayList;

@RemoteViews.RemoteView
public class FrameLayout extends ViewGroup {
    private static final int DEFAULT_CHILD_GRAVITY = 8388659;
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingBottom;
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingLeft;
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingRight;
    @ViewDebug.ExportedProperty(category = "padding")
    private int mForegroundPaddingTop;
    private final ArrayList<View> mMatchParentChildren;
    @ViewDebug.ExportedProperty(category = "measurement")
    boolean mMeasureAllChildren;

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public static final int UNSPECIFIED_GRAVITY = -1;
        public int gravity = -1;

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
        if (a.getBoolean(0, false)) {
            setMeasureAllChildren(true);
        }
        a.recycle();
    }

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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int i;
        int i2 = widthMeasureSpec;
        int i3 = heightMeasureSpec;
        int count = getChildCount();
        boolean measureMatchParentChildren = (View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && View.MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        int maxHeight2 = 0;
        while (true) {
            int i4 = maxHeight2;
            if (i4 >= count) {
                break;
            }
            View child = getChildAt(i4);
            if (this.mMeasureAllChildren || child.getVisibility() != 8) {
                View child2 = child;
                i = i4;
                measureChildWithMargins(child, i2, 0, i3, 0);
                LayoutParams lp = (LayoutParams) child2.getLayoutParams();
                int maxWidth2 = Math.max(maxWidth, child2.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                int maxHeight3 = Math.max(maxHeight, child2.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                int childState2 = combineMeasuredStates(childState, child2.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child2);
                }
                maxWidth = maxWidth2;
                maxHeight = maxHeight3;
                childState = childState2;
            } else {
                i = i4;
            }
            maxHeight2 = i + 1;
        }
        int i5 = -1;
        int childState3 = childState;
        int maxWidth3 = maxWidth + getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        int maxHeight4 = Math.max(maxHeight + getPaddingTopWithForeground() + getPaddingBottomWithForeground(), getSuggestedMinimumHeight());
        int maxWidth4 = Math.max(maxWidth3, getSuggestedMinimumWidth());
        Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight4 = Math.max(maxHeight4, drawable.getMinimumHeight());
            maxWidth4 = Math.max(maxWidth4, drawable.getMinimumWidth());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth4, i2, childState3), resolveSizeAndState(maxHeight4, i3, childState3 << 16));
        int count2 = this.mMatchParentChildren.size();
        if (count2 > 1) {
            int i6 = 0;
            while (i6 < count2) {
                View child3 = this.mMatchParentChildren.get(i6);
                ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams) child3.getLayoutParams();
                if (lp2.width == i5) {
                    width = View.MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredWidth() - getPaddingLeftWithForeground()) - getPaddingRightWithForeground()) - lp2.leftMargin) - lp2.rightMargin), 1073741824);
                } else {
                    width = getChildMeasureSpec(i2, getPaddingLeftWithForeground() + getPaddingRightWithForeground() + lp2.leftMargin + lp2.rightMargin, lp2.width);
                }
                if (lp2.height == i5) {
                    height = View.MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredHeight() - getPaddingTopWithForeground()) - getPaddingBottomWithForeground()) - lp2.topMargin) - lp2.bottomMargin), 1073741824);
                } else {
                    height = getChildMeasureSpec(i3, getPaddingTopWithForeground() + getPaddingBottomWithForeground() + lp2.topMargin + lp2.bottomMargin, lp2.height);
                }
                child3.measure(width, height);
                i6++;
                i5 = -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false);
    }

    /* access modifiers changed from: package-private */
    public void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        int parentLeft;
        int count;
        int childLeft;
        int childTop;
        int count2 = getChildCount();
        int parentLeft2 = getPaddingLeftWithForeground();
        int parentRight = (right - left) - getPaddingRightWithForeground();
        int parentTop = getPaddingTopWithForeground();
        int parentBottom = (bottom - top) - getPaddingBottomWithForeground();
        int i = 0;
        while (i < count2) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == 8) {
                count = count2;
                parentLeft = parentLeft2;
            } else {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                count = count2;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }
                int layoutDirection = getLayoutDirection();
                int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                int i2 = layoutDirection;
                int layoutDirection2 = gravity & 112;
                int i3 = absoluteGravity & 7;
                if (i3 != 1) {
                    childLeft = (i3 == 5 && !forceLeftGravity) ? (parentRight - width) - lp.rightMargin : parentLeft2 + lp.leftMargin;
                } else {
                    childLeft = (((((parentRight - parentLeft2) - width) / 2) + parentLeft2) + lp.leftMargin) - lp.rightMargin;
                }
                int childLeft2 = childLeft;
                if (layoutDirection2 == 16) {
                    int verticalGravity = layoutDirection2;
                    childTop = (((((parentBottom - parentTop) - height) / 2) + parentTop) + lp.topMargin) - lp.bottomMargin;
                } else if (layoutDirection2 == 48) {
                    int verticalGravity2 = layoutDirection2;
                    childTop = parentTop + lp.topMargin;
                } else if (layoutDirection2 != 80) {
                    childTop = lp.topMargin + parentTop;
                    int i4 = layoutDirection2;
                } else {
                    int i5 = layoutDirection2;
                    childTop = (parentBottom - height) - lp.bottomMargin;
                }
                int childTop2 = childTop;
                parentLeft = parentLeft2;
                child.layout(childLeft2, childTop2, childLeft2 + width, childTop2 + height);
            }
            i++;
            count2 = count;
            parentLeft2 = parentLeft;
        }
        int i6 = count2;
        int i7 = parentLeft2;
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

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
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

    public CharSequence getAccessibilityClassName() {
        return FrameLayout.class.getName();
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("measurement:measureAllChildren", this.mMeasureAllChildren);
        encoder.addProperty("padding:foregroundPaddingLeft", this.mForegroundPaddingLeft);
        encoder.addProperty("padding:foregroundPaddingTop", this.mForegroundPaddingTop);
        encoder.addProperty("padding:foregroundPaddingRight", this.mForegroundPaddingRight);
        encoder.addProperty("padding:foregroundPaddingBottom", this.mForegroundPaddingBottom);
    }
}
