package huawei.android.widget.appbar;

import android.view.View;
import android.view.ViewGroup;

public class NestedScrollingParentHelper {
    private int mNestedScrollAxes;
    private final ViewGroup mViewGroup;

    public NestedScrollingParentHelper(ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        onNestedScrollAccepted(child, target, axes, 0);
    }

    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        this.mNestedScrollAxes = axes;
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollAxes;
    }

    public void onStopNestedScroll(View target) {
        onStopNestedScroll(target, 0);
    }

    public void onStopNestedScroll(View target, int type) {
        this.mNestedScrollAxes = 0;
    }
}
