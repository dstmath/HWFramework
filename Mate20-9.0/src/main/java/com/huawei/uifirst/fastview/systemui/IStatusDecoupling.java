package com.huawei.uifirst.fastview.systemui;

public interface IStatusDecoupling {
    void associationHandle();

    void checkTaskAdditionNeeded(int i);

    void exitPolicy();

    boolean getDesireState();

    boolean getTaskProcessingState();

    boolean getUIProcessingState();

    boolean hasListeningCallbackRemoveSkipped();

    boolean isListeningCallbackRemoveSkipNeeded();

    void onDeviceStateChanged(Object obj);

    void onHandleClick(Object obj);

    void onHandleUpdateState(Object obj);

    void refreshUIState(Object obj);

    void resetListeningCallbackRemoveState();

    void setDesireState(boolean z);

    void setTaskInProcessing(boolean z);

    void setUIProcessingState(boolean z);

    void taskAddition(boolean z);

    void timeoutHandle();
}
