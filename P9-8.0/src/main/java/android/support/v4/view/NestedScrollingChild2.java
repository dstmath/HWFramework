package android.support.v4.view;

import android.support.annotation.Nullable;

public interface NestedScrollingChild2 extends NestedScrollingChild {
    boolean dispatchNestedPreScroll(int i, int i2, @Nullable int[] iArr, @Nullable int[] iArr2, int i3);

    boolean dispatchNestedScroll(int i, int i2, int i3, int i4, @Nullable int[] iArr, int i5);

    boolean hasNestedScrollingParent(int i);

    boolean startNestedScroll(int i, int i2);

    void stopNestedScroll(int i);
}
