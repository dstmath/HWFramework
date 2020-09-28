package com.huawei.uifirst.fastview.systemui;

import com.huawei.uifirst.fastview.common.StatusDecouplingEmpty;

public class StatusDecouplingDummy extends StatusDecouplingEmpty implements StatusDecoupling {
    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onHandleClick(boolean isClickOpen) {
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onHandleClick(int clickState) {
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void onDeviceStateChanged(boolean isDeviceOpen) {
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public boolean isListeningCallbackRemoveSkipNeeded() {
        return false;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public boolean hasListeningCallbackRemoveSkipped() {
        return false;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void resetListeningCallbackRemoveState() {
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void associationHandle() {
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void refreshUiState(boolean isUiOpen) {
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public boolean getUiProcessingState() {
        return false;
    }

    @Override // com.huawei.uifirst.fastview.systemui.StatusDecoupling
    public void setUiProcessingState(boolean isUiInProcessingState) {
    }
}
