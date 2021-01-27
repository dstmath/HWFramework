package com.android.server.wm;

import android.util.HwPCUtils;

public class HwTaskStack extends TaskStackBridgeEx {
    private static final String TAG = "HwTaskStack";

    public HwTaskStack(WindowManagerServiceEx service, int stackId, ActivityStackEx activityStack) {
        super(service, stackId, activityStack);
    }

    /* access modifiers changed from: protected */
    public void setAnimationBackground(WindowStateAnimatorEx winAnimator, int color) {
        if (!HwPCUtils.isPcDynamicStack(this.mStackId)) {
            HwTaskStack.super.setAospAnimationBackground(winAnimator, color);
        }
    }

    /* access modifiers changed from: protected */
    public void findTaskForResizePoint(int xPoint, int yPoint, int delta, TaskForResizePointSearchResultEx results) {
        try {
            if (!HwPCUtils.isPcDynamicStack(this.mStackId) || isVisible()) {
                HwTaskStack.super.aospFindTaskForResizePoint(xPoint, yPoint, delta, results);
            }
        } catch (IndexOutOfBoundsException e) {
            HwPCUtils.log(TAG, "findTaskForResizePoint");
        }
    }
}
