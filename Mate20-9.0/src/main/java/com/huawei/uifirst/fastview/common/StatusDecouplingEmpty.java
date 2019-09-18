package com.huawei.uifirst.fastview.common;

public class StatusDecouplingEmpty {
    public boolean getDesireState() {
        return false;
    }

    public void setDesireState(boolean realState) {
    }

    public boolean getTaskProcessingState() {
        return false;
    }

    public void setTaskInProcessing(boolean isInProcessing) {
    }

    public void onHandleClick(Object arg) {
    }

    public void onDeviceStateChanged(Object arg) {
    }

    public void onHandleUpdateState(Object arg) {
    }

    public void checkTaskAdditionNeeded(int deviceActualState) {
    }

    public void taskAddition(boolean desireState) {
    }

    public void timeoutHandle() {
    }

    public void exitPolicy() {
    }
}
