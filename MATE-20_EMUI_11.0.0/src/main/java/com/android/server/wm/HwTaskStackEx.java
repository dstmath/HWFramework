package com.android.server.wm;

import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;

public class HwTaskStackEx extends HwTaskStackExBridgeEx {
    final WindowManagerServiceEx mService;
    final TaskStackEx mTaskStack;

    public HwTaskStackEx(TaskStackEx taskStack, WindowManagerServiceEx wms) {
        super(taskStack, wms);
        this.mTaskStack = taskStack;
        this.mService = wms;
    }

    public boolean findTaskInFreeform(int posX, int posY, int delta, TaskForResizePointSearchResultEx results) {
        TaskEx task;
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return false;
        }
        DisplayContentEx dc = this.mTaskStack.getDisplayContent();
        if (!dc.isStackVisible(5) || (task = dc.getStack(5, 1).getTopChild()) == null) {
            return false;
        }
        Rect tmpRect = new Rect();
        task.getDimBounds(tmpRect);
        tmpRect.inset(-delta, -delta);
        if (!tmpRect.contains(posX, posY)) {
            return false;
        }
        int insizeDelta = this.mService.dipToPixel(WindowStateEx.getInsizeDp(), dc.getDisplayMetrics());
        tmpRect.inset(delta + insizeDelta, delta + insizeDelta);
        results.setSearchDone(true);
        if (tmpRect.contains(posX, posY)) {
            return false;
        }
        results.setTaskForResize(task);
        tmpRect.inset(-insizeDelta, -insizeDelta);
        return true;
    }
}
