package android.support.v4.view;

import android.support.annotation.Nullable;

public interface NestedScrollingChild {
    @Override // android.support.v4.view.NestedScrollingChild
    boolean dispatchNestedFling(float f, float f2, boolean z);

    @Override // android.support.v4.view.NestedScrollingChild
    boolean dispatchNestedPreFling(float f, float f2);

    @Override // android.support.v4.view.NestedScrollingChild
    boolean dispatchNestedPreScroll(int i, int i2, @Nullable int[] iArr, @Nullable int[] iArr2);

    @Override // android.support.v4.view.NestedScrollingChild
    boolean dispatchNestedScroll(int i, int i2, int i3, int i4, @Nullable int[] iArr);

    @Override // android.support.v4.view.NestedScrollingChild
    boolean hasNestedScrollingParent();

    @Override // android.support.v4.view.NestedScrollingChild
    boolean isNestedScrollingEnabled();

    @Override // android.support.v4.view.NestedScrollingChild
    void setNestedScrollingEnabled(boolean z);

    @Override // android.support.v4.view.NestedScrollingChild
    boolean startNestedScroll(int i);

    @Override // android.support.v4.view.NestedScrollingChild
    void stopNestedScroll();
}
