package com.android.server.wm;

import android.graphics.Rect;

interface BoundsAnimationTarget {
    void onAnimationEnd(boolean z, Rect rect, boolean z2);

    void onAnimationStart(boolean z, boolean z2);

    boolean setPinnedStackSize(Rect rect, Rect rect2);

    boolean shouldDeferStartOnMoveToFullscreen();
}
