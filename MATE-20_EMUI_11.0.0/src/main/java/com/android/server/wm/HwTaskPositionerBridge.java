package com.android.server.wm;

public class HwTaskPositionerBridge implements IHwTaskPositionerEx {
    private HwTaskPositionerBridgeEx mHwTaskPositionerBridgeEx;

    public HwTaskPositionerBridge(WindowManagerService service) {
    }

    public void setHwTaskPositionerBridgeEx(HwTaskPositionerBridgeEx hwTaskPositionerBridgeEx) {
        this.mHwTaskPositionerBridgeEx = hwTaskPositionerBridgeEx;
    }

    public void updateFreeFormOutLine(int color) {
        this.mHwTaskPositionerBridgeEx.updateFreeFormOutLine(color);
    }

    public void processPCWindowDragHitHotArea(TaskRecord taskRecord, float newX, float newY) {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        this.mHwTaskPositionerBridgeEx.processPCWindowDragHitHotArea(taskRecordEx, newX, newY);
    }

    public void processPCWindowFinishDragHitHotArea(TaskRecord taskRecord, float newX, float newY) {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        this.mHwTaskPositionerBridgeEx.processPCWindowFinishDragHitHotArea(taskRecordEx, newX, newY);
    }

    public int limitPCWindowSize(int legnth, int limitType) {
        return this.mHwTaskPositionerBridgeEx.limitPCWindowSize(legnth, limitType);
    }
}
