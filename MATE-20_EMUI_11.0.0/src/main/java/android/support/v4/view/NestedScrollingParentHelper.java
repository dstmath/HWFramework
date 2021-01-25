package android.support.v4.view;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public class NestedScrollingParentHelper {
    private int mNestedScrollAxes;
    private final ViewGroup mViewGroup;

    public NestedScrollingParentHelper(@NonNull ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
    }

    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        onNestedScrollAccepted(child, target, axes, 0);
    }

    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        this.mNestedScrollAxes = axes;
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollAxes;
    }

    public void onStopNestedScroll(@NonNull View target) {
        onStopNestedScroll(target, 0);
    }

    public void onStopNestedScroll(@NonNull View target, int type) {
        this.mNestedScrollAxes = 0;
    }
}
