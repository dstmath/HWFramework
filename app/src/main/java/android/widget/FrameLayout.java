package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewHierarchyEncoder;
import android.view.inputmethod.EditorInfo;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;

@RemoteView
public class FrameLayout extends ViewGroup {
    private static final int DEFAULT_CHILD_GRAVITY = 8388659;
    @ExportedProperty(category = "padding")
    private int mForegroundPaddingBottom;
    @ExportedProperty(category = "padding")
    private int mForegroundPaddingLeft;
    @ExportedProperty(category = "padding")
    private int mForegroundPaddingRight;
    @ExportedProperty(category = "padding")
    private int mForegroundPaddingTop;
    private final ArrayList<View> mMatchParentChildren;
    @ExportedProperty(category = "measurement")
    boolean mMeasureAllChildren;

    public static class LayoutParams extends MarginLayoutParams {
        public static final int UNSPECIFIED_GRAVITY = -1;
        public int gravity;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = UNSPECIFIED_GRAVITY;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FrameLayout_Layout);
            this.gravity = a.getInt(0, UNSPECIFIED_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = UNSPECIFIED_GRAVITY;
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = UNSPECIFIED_GRAVITY;
            this.gravity = gravity;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.gravity = UNSPECIFIED_GRAVITY;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.gravity = UNSPECIFIED_GRAVITY;
        }

        public LayoutParams(LayoutParams source) {
            super((MarginLayoutParams) source);
            this.gravity = UNSPECIFIED_GRAVITY;
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
        this.mMatchParentChildren = new ArrayList(1);
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
        this.mMatchParentChildren = new ArrayList(1);
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
            if (getForegroundGravity() != LogPower.HW_PUSH_FINISH || foreground == null) {
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

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    int getPaddingLeftWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingLeft, this.mForegroundPaddingLeft);
        }
        return this.mPaddingLeft + this.mForegroundPaddingLeft;
    }

    int getPaddingRightWithForeground() {
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int count = getChildCount();
        boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) == EditorInfo.IME_FLAG_NO_ENTER_ACTION ? MeasureSpec.getMode(heightMeasureSpec) != EditorInfo.IME_FLAG_NO_ENTER_ACTION : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (this.mMeasureAllChildren || child.getVisibility() != 8) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, (child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin);
                maxHeight = Math.max(maxHeight, (child.getMeasuredHeight() + lp.topMargin) + lp.bottomMargin);
                childState = View.combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child);
                }
            }
        }
        maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        maxHeight = Math.max(maxHeight + (getPaddingTopWithForeground() + getPaddingBottomWithForeground()), getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }
        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState), View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState << 16));
        count = this.mMatchParentChildren.size();
        if (count > 1) {
            for (i = 0; i < count; i++) {
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;
                child = (View) this.mMatchParentChildren.get(i);
                MarginLayoutParams lp2 = (MarginLayoutParams) child.getLayoutParams();
                if (lp2.width == -1) {
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredWidth() - getPaddingLeftWithForeground()) - getPaddingRightWithForeground()) - lp2.leftMargin) - lp2.rightMargin), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                } else {
                    childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, ((getPaddingLeftWithForeground() + getPaddingRightWithForeground()) + lp2.leftMargin) + lp2.rightMargin, lp2.width);
                }
                if (lp2.height == -1) {
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredHeight() - getPaddingTopWithForeground()) - getPaddingBottomWithForeground()) - lp2.topMargin) - lp2.bottomMargin), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                } else {
                    childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, ((getPaddingTopWithForeground() + getPaddingBottomWithForeground()) + lp2.topMargin) + lp2.bottomMargin, lp2.height);
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        int count = getChildCount();
        int parentLeft = getPaddingLeftWithForeground();
        int parentRight = (right - left) - getPaddingRightWithForeground();
        int parentTop = getPaddingTopWithForeground();
        int parentBottom = (bottom - top) - getPaddingBottomWithForeground();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                int i2;
                int childLeft;
                int childTop;
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }
                int verticalGravity = gravity & LogPower.APP_PROCESS_EXIT;
                switch (Gravity.getAbsoluteGravity(gravity, getLayoutDirection()) & 7) {
                    case HwCfgFilePolicy.EMUI /*1*/:
                        i2 = lp.leftMargin;
                        childLeft = (((((parentRight - parentLeft) - width) / 2) + parentLeft) + r0) - lp.rightMargin;
                        break;
                    case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                        if (!forceLeftGravity) {
                            childLeft = (parentRight - width) - lp.rightMargin;
                            break;
                        }
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                        break;
                }
                switch (verticalGravity) {
                    case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                        i2 = lp.topMargin;
                        childTop = (((((parentBottom - parentTop) - height) / 2) + parentTop) + r0) - lp.bottomMargin;
                        break;
                    case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                        childTop = (parentBottom - height) - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                        break;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
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

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    public CharSequence getAccessibilityClassName() {
        return FrameLayout.class.getName();
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("measurement:measureAllChildren", this.mMeasureAllChildren);
        encoder.addProperty("padding:foregroundPaddingLeft", this.mForegroundPaddingLeft);
        encoder.addProperty("padding:foregroundPaddingTop", this.mForegroundPaddingTop);
        encoder.addProperty("padding:foregroundPaddingRight", this.mForegroundPaddingRight);
        encoder.addProperty("padding:foregroundPaddingBottom", this.mForegroundPaddingBottom);
    }
}
