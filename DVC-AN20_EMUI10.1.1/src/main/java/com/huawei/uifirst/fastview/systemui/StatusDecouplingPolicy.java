package com.huawei.uifirst.fastview.systemui;

import com.huawei.uifirst.fastview.common.StatusDecouplingImpl;

public class StatusDecouplingPolicy extends StatusDecouplingImpl implements StatusDecoupling {
    private final CallBack mCallback;
    private boolean mIsListeningSkipped = false;

    public interface CallBack {
        void associationHandle();

        void dropdownListenHandle(boolean z);

        boolean getUiProcessingState();

        void refreshUiState(boolean z);

        void setUiProcessingState(boolean z);

        void taskAddition(boolean z);

        void timeoutHandle();
    }

    public StatusDecouplingPolicy(CallBack cb, int delayTime, String modelName) {
        this.mCallback = cb;
        this.mDelayTime = delayTime;
        this.mModelName = modelName;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onHandleClick(boolean isClickOpen) {
        handleClickEvent(isClickOpen ? 1 : 0);
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onHandleClick(int clickState) {
        handleClickEvent(clickState);
    }

    private void handleClickEvent(int newState) {
        this.mIsUserClickEnable = true;
        if (newState == 0) {
            this.mIsUserClickSkipped = false;
            this.mIsDesiredState = false;
        } else if (newState == 1) {
            this.mIsUserClickSkipped = false;
            this.mIsDesiredState = true;
        } else if (newState != 2) {
            this.mIsUserClickSkipped = false;
            this.mIsDesiredState = false;
        } else {
            this.mIsUserClickSkipped = true;
            this.mIsDesiredState = !this.mIsDesiredState;
        }
        locolLog(2, "onHandleClick newState=" + newState + " , mDesiredState =" + this.mIsDesiredState);
        setTaskInProcessing(true);
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onDeviceStateChanged(boolean isDeviceOpen) {
        if (this.mIsUserClickEnable) {
            locolLog(2, "onDeviceStateChanged");
            checkTaskAdditionNeeded(isDeviceOpen ? 1 : 0);
            if (this.mIsTaskAdditionNeeded) {
                this.mCallback.taskAddition(this.mIsDesiredState);
                setTaskInProcessing(true);
            }
        }
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onHandleUpdateState(boolean isOpenState) {
        locolLog(2, "onHandleUpdateState arg= " + isOpenState);
        this.mCallback.dropdownListenHandle(hasListeningCallbackRemoveSkipped() && !this.mIsTaskInProcessing);
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void taskAddition(boolean isOpenState) {
        this.mCallback.taskAddition(isOpenState);
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void timeoutHandle() {
        this.mCallback.timeoutHandle();
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void exitPolicy() {
        locolLog(3, "exitPolicy !!!");
        this.mIsDesiredState = false;
        setTaskInProcessing(false);
        this.mIsUserClickSkipped = false;
        this.mIsTaskAdditionNeeded = false;
        this.mIsListeningSkipped = false;
        this.mIsUserClickEnable = false;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void associationHandle() {
        this.mCallback.associationHandle();
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void refreshUiState(boolean isUiOpen) {
        this.mCallback.refreshUiState(isUiOpen);
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public boolean getUiProcessingState() {
        return this.mCallback.getUiProcessingState();
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void setUiProcessingState(boolean isUiInProcessingState) {
        this.mCallback.setUiProcessingState(isUiInProcessingState);
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public boolean isListeningCallbackRemoveSkipNeeded() {
        this.mIsListeningSkipped = this.mIsUserClickSkipped && this.mIsTaskInProcessing;
        return this.mIsListeningSkipped;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public boolean hasListeningCallbackRemoveSkipped() {
        return this.mIsListeningSkipped;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void resetListeningCallbackRemoveState() {
        this.mIsListeningSkipped = false;
    }
}
