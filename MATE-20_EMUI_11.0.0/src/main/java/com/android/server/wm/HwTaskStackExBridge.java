package com.android.server.wm;

import com.android.server.wm.DisplayContent;

public class HwTaskStackExBridge implements IHwTaskStackEx {
    private HwTaskStackExBridgeEx mHwTaskStackExBridgeEx;

    public HwTaskStackExBridge(TaskStack taskStack, WindowManagerService wms) {
    }

    public void setHwTaskStackExBridgeEx(HwTaskStackExBridgeEx hwTaskStackExBridgeEx) {
        this.mHwTaskStackExBridgeEx = hwTaskStackExBridgeEx;
    }

    public boolean findTaskInFreeform(int pointX, int pointY, int delta, DisplayContent.TaskForResizePointSearchResult result) {
        TaskForResizePointSearchResultEx resultEx = new TaskForResizePointSearchResultEx();
        resultEx.setTaskForResizePointSearchResult(result);
        return this.mHwTaskStackExBridgeEx.findTaskInFreeform(pointX, pointY, delta, resultEx);
    }
}
