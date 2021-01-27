package com.huawei.uifirst.fastview.common;

public class StatusDecouplingEmpty {
    public boolean getDesireState() {
        return false;
    }

    public void setDesireState(boolean isOpenState) {
    }

    public boolean getTaskProcessingState() {
        return false;
    }

    public void setTaskInProcessing(boolean isInProcessing) {
    }

    public void onHandleUpdateState(boolean isOpenState) {
    }

    public void checkTaskAdditionNeeded(int deviceActualState) {
    }

    public void taskAddition(boolean isOpenState) {
    }

    public void timeoutHandle() {
    }

    public void exitPolicy() {
    }
}
