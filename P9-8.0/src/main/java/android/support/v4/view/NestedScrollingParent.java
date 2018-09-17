package android.support.v4.view;

import android.support.annotation.NonNull;
import android.view.View;

public interface NestedScrollingParent {
    int getNestedScrollAxes();

    boolean onNestedFling(@NonNull View view, float f, float f2, boolean z);

    boolean onNestedPreFling(@NonNull View view, float f, float f2);

    void onNestedPreScroll(@NonNull View view, int i, int i2, @NonNull int[] iArr);

    void onNestedScroll(@NonNull View view, int i, int i2, int i3, int i4);

    void onNestedScrollAccepted(@NonNull View view, @NonNull View view2, int i);

    boolean onStartNestedScroll(@NonNull View view, @NonNull View view2, int i);

    void onStopNestedScroll(@NonNull View view);
}
