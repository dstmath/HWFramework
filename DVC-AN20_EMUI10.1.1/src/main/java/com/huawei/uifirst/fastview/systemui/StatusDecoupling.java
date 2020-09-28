package com.huawei.uifirst.fastview.systemui;

public interface StatusDecoupling {
    void associationHandle();

    void checkTaskAdditionNeeded(int i);

    void exitPolicy();

    boolean getDesireState();

    boolean getTaskProcessingState();

    boolean getUiProcessingState();

    boolean hasListeningCallbackRemoveSkipped();

    boolean isListeningCallbackRemoveSkipNeeded();

    void onDeviceStateChanged(boolean z);

    void onHandleClick(int i);

    void onHandleClick(boolean z);

    void onHandleUpdateState(boolean z);

    void refreshUiState(boolean z);

    void resetListeningCallbackRemoveState();

    void setDesireState(boolean z);

    void setTaskInProcessing(boolean z);

    void setUiProcessingState(boolean z);

    void taskAddition(boolean z);

    void timeoutHandle();
}
