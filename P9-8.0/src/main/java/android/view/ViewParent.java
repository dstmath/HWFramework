package android.view;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ActionMode.Callback;
import android.view.accessibility.AccessibilityEvent;

public interface ViewParent {
    void bringChildToFront(View view);

    boolean canResolveLayoutDirection();

    boolean canResolveTextAlignment();

    boolean canResolveTextDirection();

    void childDrawableStateChanged(View view);

    void childHasTransientStateChanged(View view, boolean z);

    void clearChildFocus(View view);

    void createContextMenu(ContextMenu contextMenu);

    View focusSearch(View view, int i);

    void focusableViewAvailable(View view);

    boolean getChildVisibleRect(View view, Rect rect, Point point);

    int getLayoutDirection();

    ViewParent getParent();

    ViewParent getParentForAccessibility();

    int getTextAlignment();

    int getTextDirection();

    @Deprecated
    void invalidateChild(View view, Rect rect);

    @Deprecated
    ViewParent invalidateChildInParent(int[] iArr, Rect rect);

    boolean isLayoutDirectionResolved();

    boolean isLayoutRequested();

    boolean isTextAlignmentResolved();

    boolean isTextDirectionResolved();

    View keyboardNavigationClusterSearch(View view, int i);

    void notifySubtreeAccessibilityStateChanged(View view, View view2, int i);

    boolean onNestedFling(View view, float f, float f2, boolean z);

    boolean onNestedPreFling(View view, float f, float f2);

    boolean onNestedPrePerformAccessibilityAction(View view, int i, Bundle bundle);

    void onNestedPreScroll(View view, int i, int i2, int[] iArr);

    void onNestedScroll(View view, int i, int i2, int i3, int i4);

    void onNestedScrollAccepted(View view, View view2, int i);

    boolean onStartNestedScroll(View view, View view2, int i);

    void onStopNestedScroll(View view);

    void recomputeViewAttributes(View view);

    void requestChildFocus(View view, View view2);

    boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z);

    void requestDisallowInterceptTouchEvent(boolean z);

    void requestFitSystemWindows();

    void requestLayout();

    boolean requestSendAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

    void requestTransparentRegion(View view);

    boolean showContextMenuForChild(View view);

    boolean showContextMenuForChild(View view, float f, float f2);

    ActionMode startActionModeForChild(View view, Callback callback);

    ActionMode startActionModeForChild(View view, Callback callback, int i);

    void onDescendantInvalidated(View child, View target) {
        if (getParent() != null) {
            getParent().onDescendantInvalidated(child, target);
        }
    }
}
