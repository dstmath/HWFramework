package huawei.android.widget.appbar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import huawei.android.widget.appbar.HwCoordinatorLayout;
import java.util.List;

abstract class HeaderScrollingViewBehavior extends ViewOffsetBehavior<View> {
    private int mOverlayTop;
    final Rect mTempRect1 = new Rect();
    final Rect mTempRect2 = new Rect();
    private int mVerticalLayoutGap = 0;

    /* access modifiers changed from: package-private */
    public abstract View findFirstDependency(List<View> list);

    public HeaderScrollingViewBehavior() {
    }

    public HeaderScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
    public boolean onMeasureChild(HwCoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int availableHeight;
        int i;
        int childLpHeight = child.getLayoutParams().height;
        if (childLpHeight != -1 && childLpHeight != -2) {
            return false;
        }
        View header = findFirstDependency(parent.getDependencies(child));
        if (header == null || header.getVisibility() == 8) {
            return false;
        }
        if (header.getFitsSystemWindows()) {
            if (!child.getFitsSystemWindows()) {
                child.setFitsSystemWindows(true);
                if (child.getFitsSystemWindows()) {
                    child.requestLayout();
                    return true;
                }
            }
        }
        int availableHeight2 = View.MeasureSpec.getSize(parentHeightMeasureSpec);
        if (availableHeight2 == 0) {
            availableHeight = parent.getHeight();
        } else {
            availableHeight = availableHeight2;
        }
        int height = (availableHeight - header.getMeasuredHeight()) + getScrollRange(header);
        if (childLpHeight == -1) {
            i = 1073741824;
        } else {
            i = Integer.MIN_VALUE;
        }
        parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, View.MeasureSpec.makeMeasureSpec(height, i), heightUsed);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // huawei.android.widget.appbar.ViewOffsetBehavior
    public void layoutChild(HwCoordinatorLayout parent, View child, int layoutDirection) {
        View header = findFirstDependency(parent.getDependencies(child));
        if (header == null || header.getVisibility() == 8) {
            super.layoutChild(parent, child, layoutDirection);
            this.mVerticalLayoutGap = 0;
            return;
        }
        HwCoordinatorLayout.LayoutParams lp = (HwCoordinatorLayout.LayoutParams) child.getLayoutParams();
        Rect available = this.mTempRect1;
        available.set(parent.getPaddingLeft() + lp.leftMargin, header.getBottom() + lp.topMargin, (parent.getWidth() - parent.getPaddingRight()) - lp.rightMargin, ((parent.getHeight() + header.getBottom()) - parent.getPaddingBottom()) - lp.bottomMargin);
        WindowInsets parentInsets = parent.getLastWindowInsets();
        if (parentInsets != null && parent.getFitsSystemWindows() && !child.getFitsSystemWindows()) {
            available.left += parentInsets.getSystemWindowInsetLeft();
            available.right -= parentInsets.getSystemWindowInsetRight();
        }
        Rect out = this.mTempRect2;
        Gravity.apply(resolveGravity(lp.mGravity), child.getMeasuredWidth(), child.getMeasuredHeight(), available, out, layoutDirection);
        int overlap = getOverlapPixelsForOffset(header);
        child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
        this.mVerticalLayoutGap = out.top - header.getBottom();
    }

    /* access modifiers changed from: package-private */
    public float getOverlapRatioForOffset(View header) {
        return 1.0f;
    }

    /* access modifiers changed from: package-private */
    public final int getOverlapPixelsForOffset(View header) {
        if (this.mOverlayTop == 0) {
            return 0;
        }
        float overlapRatioForOffset = getOverlapRatioForOffset(header);
        int i = this.mOverlayTop;
        return MathUtils.clamp((int) (overlapRatioForOffset * ((float) i)), 0, i);
    }

    private static int resolveGravity(int gravity) {
        if (gravity == 0) {
            return 8388659;
        }
        return gravity;
    }

    /* access modifiers changed from: package-private */
    public int getScrollRange(View v) {
        return v.getMeasuredHeight();
    }

    /* access modifiers changed from: package-private */
    public final int getVerticalLayoutGap() {
        return this.mVerticalLayoutGap;
    }

    public final void setOverlayTop(int overlayTop) {
        this.mOverlayTop = overlayTop;
    }

    public final int getOverlayTop() {
        return this.mOverlayTop;
    }
}
