package com.android.server.wm;

public class HwTaskPositionerBridgeEx {
    private HwTaskPositionerBridge mHwTaskPositionerBridge;

    public HwTaskPositionerBridgeEx(WindowManagerServiceEx service) {
        if (service != null) {
            this.mHwTaskPositionerBridge = new HwTaskPositionerBridge(service.getWindowManagerService());
            this.mHwTaskPositionerBridge.setHwTaskPositionerBridgeEx(this);
        }
    }

    public HwTaskPositionerBridge getHwTaskPositionerBridge() {
        return this.mHwTaskPositionerBridge;
    }

    public void updateFreeFormOutLine(int color) {
    }

    public void processPCWindowDragHitHotArea(TaskRecordEx taskRecord, float newX, float newY) {
    }

    public void processPCWindowFinishDragHitHotArea(TaskRecordEx taskRecord, float newX, float newY) {
    }

    public int limitPCWindowSize(int legnth, int limitType) {
        return 0;
    }
}
