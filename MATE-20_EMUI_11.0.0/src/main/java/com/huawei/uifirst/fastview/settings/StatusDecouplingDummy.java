package com.huawei.uifirst.fastview.settings;

import com.huawei.uifirst.fastview.common.StatusDecouplingEmpty;

public class StatusDecouplingDummy extends StatusDecouplingEmpty implements StatusDecoupling {
    @Override // com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void onHandleClick(int clickState) {
    }

    @Override // com.huawei.uifirst.fastview.settings.StatusDecoupling
    public void onDeviceStateChanged(int newDeviceState) {
    }
}
