package com.huawei.uifirst.fastview.settings;

public interface IStatusDecoupling {
    void checkTaskAdditionNeeded(int i);

    void exitPolicy();

    boolean getDesireState();

    boolean getTaskProcessingState();

    void onDeviceStateChanged(Object obj);

    void onHandleClick(Object obj);

    void onHandleUpdateState(Object obj);

    void setDesireState(boolean z);

    void setTaskInProcessing(boolean z);

    void taskAddition(boolean z);

    void timeoutHandle();
}
