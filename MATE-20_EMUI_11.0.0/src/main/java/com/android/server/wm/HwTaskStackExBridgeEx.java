package com.android.server.wm;

public class HwTaskStackExBridgeEx {
    private HwTaskStackExBridge mHwTaskStackExBridge;

    public HwTaskStackExBridgeEx(TaskStackEx taskStack, WindowManagerServiceEx wms) {
        this.mHwTaskStackExBridge = new HwTaskStackExBridge(taskStack.getTaskStack(), wms.getWindowManagerService());
        this.mHwTaskStackExBridge.setHwTaskStackExBridgeEx(this);
    }

    public HwTaskStackExBridge getHwTaskStackExBridge() {
        return this.mHwTaskStackExBridge;
    }

    public boolean findTaskInFreeform(int x, int y, int delta, TaskForResizePointSearchResultEx result) {
        return false;
    }
}
