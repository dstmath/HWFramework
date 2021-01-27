package com.android.server.wm;

public interface IHwTaskPositionerEx {
    int limitPCWindowSize(int i, int i2);

    void processPCWindowDragHitHotArea(TaskRecord taskRecord, float f, float f2);

    void processPCWindowFinishDragHitHotArea(TaskRecord taskRecord, float f, float f2);

    void updateFreeFormOutLine(int i);
}
