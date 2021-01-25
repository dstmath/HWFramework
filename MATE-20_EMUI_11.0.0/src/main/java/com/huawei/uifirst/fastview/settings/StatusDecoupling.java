package com.huawei.uifirst.fastview.settings;

public interface StatusDecoupling {
    void checkTaskAdditionNeeded(int i);

    void exitPolicy();

    boolean getDesireState();

    boolean getTaskProcessingState();

    void onDeviceStateChanged(int i);

    void onHandleClick(int i);

    void onHandleUpdateState(boolean z);

    void setDesireState(boolean z);

    void setTaskInProcessing(boolean z);

    void taskAddition(boolean z);

    void timeoutHandle();
}
