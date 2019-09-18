package com.android.server.wm;

import android.util.HwPCUtils;
import com.android.server.wm.DisplayContent;

public class HwTaskStack extends TaskStack {
    private static final String TAG = "HwTaskStack";

    public HwTaskStack(WindowManagerService service, int stackId, StackWindowController controller) {
        super(service, stackId, controller);
    }

    /* access modifiers changed from: protected */
    public void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        if (!HwPCUtils.isPcDynamicStack(this.mStackId)) {
            HwTaskStack.super.setAnimationBackground(winAnimator, color);
        }
    }

    /* access modifiers changed from: protected */
    public void findTaskForResizePoint(int x, int y, int delta, DisplayContent.TaskForResizePointSearchResult results) {
        try {
            if (!HwPCUtils.isPcDynamicStack(this.mStackId) || isVisible()) {
                HwTaskStack.super.findTaskForResizePoint(x, y, delta, results);
            }
        } catch (IndexOutOfBoundsException e) {
            HwPCUtils.log(TAG, "findTaskForResizePoint");
        }
    }
}
