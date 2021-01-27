package com.huawei.uifirst.fastview.settings;

import com.huawei.uifirst.fastview.common.StatusDecouplingImpl;

public class StatusDecouplingPolicy extends StatusDecouplingImpl implements StatusDecoupling {
    private final CallBack mCallback;

    public interface CallBack {
        int stateTransformation(int i);

        void taskAddition(boolean z);

        void timeoutHandle();
    }

    public StatusDecouplingPolicy(CallBack cb, int delayTime, String modelName) {
        this.mCallback = cb;
        this.mDelayTime = delayTime;
        this.mModelName = modelName;
    }

    @Override // com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void onHandleClick(int clickState) {
        this.mIsUserClickEnable = true;
        int newState = this.mCallback.stateTransformation(clickState);
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

    @Override // com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void onDeviceStateChanged(int newDeviceState) {
        if (this.mIsUserClickEnable) {
            locolLog(2, "onDeviceStateChanged");
            checkTaskAdditionNeeded(this.mCallback.stateTransformation(newDeviceState));
            if (this.mIsTaskAdditionNeeded) {
                taskAddition(this.mIsDesiredState);
                setTaskInProcessing(true);
            }
        }
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void onHandleUpdateState(boolean isOpenState) {
        locolLog(2, "onHandleUpdateState arg= " + isOpenState);
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void taskAddition(boolean isOpenState) {
        this.mCallback.taskAddition(isOpenState);
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void timeoutHandle() {
        this.mIsTaskInProcessing = false;
        this.mCallback.timeoutHandle();
    }

    @Override // com.huawei.uifirst.fastview.common.StatusDecouplingImpl, com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void exitPolicy() {
        locolLog(3, "exitPolicy");
        this.mIsDesiredState = false;
        setTaskInProcessing(false);
        this.mIsUserClickSkipped = false;
        this.mIsTaskAdditionNeeded = false;
        this.mIsUserClickEnable = false;
    }
}
