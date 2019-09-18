package com.android.server.wm;

import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import com.android.server.wm.DisplayContent;

public class HwTaskStackEx implements IHwTaskStackEx {
    final WindowManagerService mService;
    final TaskStack mTaskStack;

    public HwTaskStackEx(TaskStack taskStack, WindowManagerService wms) {
        this.mTaskStack = taskStack;
        this.mService = wms;
    }

    public boolean findTaskInFreeform(int x, int y, int delta, DisplayContent.TaskForResizePointSearchResult results) {
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return false;
        }
        DisplayContent dc = this.mTaskStack.getDisplayContent();
        if (!dc.isStackVisible(5)) {
            return false;
        }
        TaskStack taskStack = dc.getStack(5, 1);
        Task t = taskStack != null ? (Task) taskStack.getTopChild() : null;
        if (t == null) {
            return false;
        }
        Rect tmpRect = new Rect();
        t.getDimBounds(tmpRect);
        tmpRect.inset(-delta, -delta);
        if (!tmpRect.contains(x, y)) {
            return false;
        }
        int insizeDelta = WindowManagerService.dipToPixel(10, dc.getDisplayMetrics());
        tmpRect.inset(delta + insizeDelta, delta + insizeDelta);
        results.searchDone = true;
        if (tmpRect.contains(x, y)) {
            return false;
        }
        results.taskForResize = t;
        tmpRect.inset(-insizeDelta, -insizeDelta);
        return true;
    }
}
