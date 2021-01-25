package com.android.server.wm;

import android.graphics.Rect;
import com.android.server.wm.BoundsAnimationController;

/* access modifiers changed from: package-private */
public interface BoundsAnimationTarget {
    void onAnimationEnd(boolean z, Rect rect, boolean z2);

    boolean onAnimationStart(boolean z, boolean z2, @BoundsAnimationController.AnimationType int i);

    boolean setPinnedStackAlpha(float f);

    boolean setPinnedStackSize(Rect rect, Rect rect2);

    boolean shouldDeferStartOnMoveToFullscreen();

    default boolean isAttached() {
        return true;
    }
}
