package android.support.v4.view;

import android.support.annotation.NonNull;
import android.view.View;

public interface NestedScrollingParent {
    @Override // android.support.v4.view.NestedScrollingParent
    int getNestedScrollAxes();

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    boolean onNestedFling(@NonNull View view, float f, float f2, boolean z);

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    boolean onNestedPreFling(@NonNull View view, float f, float f2);

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    void onNestedPreScroll(@NonNull View view, int i, int i2, @NonNull int[] iArr);

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    void onNestedScroll(@NonNull View view, int i, int i2, int i3, int i4);

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    void onNestedScrollAccepted(@NonNull View view, @NonNull View view2, int i);

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    boolean onStartNestedScroll(@NonNull View view, @NonNull View view2, int i);

    @Override // android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    void onStopNestedScroll(@NonNull View view);
}
