package com.huawei.uifirst.fastview.systemui;

import com.huawei.uifirst.fastview.common.StatusDecouplingEmpty;

public class StatusDecouplingDummy extends StatusDecouplingEmpty implements IStatusDecoupling {
    public boolean isListeningCallbackRemoveSkipNeeded() {
        return false;
    }

    public boolean hasListeningCallbackRemoveSkipped() {
        return false;
    }

    public void resetListeningCallbackRemoveState() {
    }

    public void associationHandle() {
    }

    public void refreshUIState(Object arg_state) {
    }

    public boolean getUIProcessingState() {
        return false;
    }

    public void setUIProcessingState(boolean uiProcessingState) {
    }
}
