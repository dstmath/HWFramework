package huawei.android.widget.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import huawei.android.widget.appbar.HwCoordinatorLayout;

/* access modifiers changed from: package-private */
public class ViewOffsetBehavior<V extends View> extends HwCoordinatorLayout.Behavior<V> {
    private int mTempLeftRightOffset = 0;
    private int mTempTopBottomOffset = 0;
    private ViewOffsetHelper mViewOffsetHelper;

    ViewOffsetBehavior() {
    }

    ViewOffsetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
    public boolean onLayoutChild(HwCoordinatorLayout parent, V child, int layoutDirection) {
        layoutChild(parent, child, layoutDirection);
        if (this.mViewOffsetHelper == null) {
            this.mViewOffsetHelper = new ViewOffsetHelper(child);
        }
        this.mViewOffsetHelper.onViewLayout();
        int i = this.mTempTopBottomOffset;
        if (i != 0) {
            this.mViewOffsetHelper.setTopAndBottomOffset(i);
            this.mTempTopBottomOffset = 0;
        }
        int i2 = this.mTempLeftRightOffset;
        if (i2 == 0) {
            return true;
        }
        this.mViewOffsetHelper.setLeftAndRightOffset(i2);
        this.mTempLeftRightOffset = 0;
        return true;
    }

    /* access modifiers changed from: protected */
    public void layoutChild(HwCoordinatorLayout parent, V child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
    }

    public boolean setTopAndBottomOffset(int offset) {
        ViewOffsetHelper viewOffsetHelper = this.mViewOffsetHelper;
        if (viewOffsetHelper != null) {
            return viewOffsetHelper.setTopAndBottomOffset(offset);
        }
        this.mTempTopBottomOffset = offset;
        return false;
    }

    public boolean setLeftAndRightOffset(int offset) {
        ViewOffsetHelper viewOffsetHelper = this.mViewOffsetHelper;
        if (viewOffsetHelper != null) {
            return viewOffsetHelper.setLeftAndRightOffset(offset);
        }
        this.mTempLeftRightOffset = offset;
        return false;
    }

    public int getTopAndBottomOffset() {
        ViewOffsetHelper viewOffsetHelper = this.mViewOffsetHelper;
        if (viewOffsetHelper != null) {
            return viewOffsetHelper.getTopAndBottomOffset();
        }
        return 0;
    }

    public int getLeftAndRightOffset() {
        ViewOffsetHelper viewOffsetHelper = this.mViewOffsetHelper;
        if (viewOffsetHelper != null) {
            return viewOffsetHelper.getLeftAndRightOffset();
        }
        return 0;
    }
}
