package com.android.server.wm;

import android.util.HwPCUtils;
import com.android.server.wm.DisplayContent.TaskForResizePointSearchResult;

public class HwTaskStack extends TaskStack {
    private static final String TAG = "HwTaskStack";

    public HwTaskStack(WindowManagerService service, int stackId) {
        super(service, stackId);
    }

    protected void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        if (!HwPCUtils.isPcDynamicStack(this.mStackId)) {
            super.setAnimationBackground(winAnimator, color);
        }
    }

    protected void findTaskForResizePoint(int x, int y, int delta, TaskForResizePointSearchResult results) {
        try {
            if (!HwPCUtils.isPcDynamicStack(this.mStackId) || (isVisible() ^ 1) == 0) {
                super.findTaskForResizePoint(x, y, delta, results);
            }
        } catch (IndexOutOfBoundsException e) {
            HwPCUtils.log(TAG, "findTaskForResizePoint");
        }
    }
}
