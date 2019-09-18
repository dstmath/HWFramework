package com.huawei.uifirst.fastview.settings;

import com.huawei.uifirst.fastview.common.StatusDecoupling;

public class StatusDecouplingPolicy extends StatusDecoupling implements IStatusDecoupling {
    public final CallBack mCallback;

    public interface CallBack {
        int stateTransformation(Object obj);

        void taskAddition(boolean z);

        void timeoutHandle();
    }

    public StatusDecouplingPolicy(CallBack cb, int DelayTime, String modelName) {
        this.mCallback = cb;
        this.mDelayTime = DelayTime;
        this.mModelName = modelName;
    }

    public void onHandleClick(Object arg) {
        this.mUserClickEnable = true;
        int newState = this.mCallback.stateTransformation(arg);
        switch (newState) {
            case 0:
                this.mUserClickSkipped = false;
                this.mDesiredState = false;
                break;
            case 1:
                this.mUserClickSkipped = false;
                this.mDesiredState = true;
                break;
            case 2:
                this.mUserClickSkipped = true;
                this.mDesiredState = !this.mDesiredState;
                break;
            default:
                this.mUserClickSkipped = false;
                this.mDesiredState = false;
                break;
        }
        locolLog(2, "onHandleClick  newState=" + newState + " , mDesiredState =" + this.mDesiredState + ", mUserClickSkipped=" + this.mUserClickSkipped);
        setTaskInProcessing(true);
    }

    public void onDeviceStateChanged(Object arg) {
        if (this.mUserClickEnable) {
            locolLog(2, "onDeviceStateChanged");
            checkTaskAdditionNeeded(this.mCallback.stateTransformation(arg));
            if (this.mIsTaskAdditionNeeded) {
                taskAddition(this.mDesiredState);
                setTaskInProcessing(true);
            }
        }
    }

    public void onHandleUpdateState(Object arg) {
        locolLog(2, "onHandleUpdateState arg= " + arg);
    }

    public void taskAddition(boolean desireState) {
        this.mCallback.taskAddition(desireState);
    }

    public void timeoutHandle() {
        this.mIsTaskInProcessing = false;
        this.mCallback.timeoutHandle();
    }

    public void exitPolicy() {
        locolLog(3, "exitPolicy !!!");
        this.mDesiredState = false;
        setTaskInProcessing(false);
        this.mUserClickSkipped = false;
        this.mIsTaskAdditionNeeded = false;
        this.mUserClickEnable = false;
    }
}
