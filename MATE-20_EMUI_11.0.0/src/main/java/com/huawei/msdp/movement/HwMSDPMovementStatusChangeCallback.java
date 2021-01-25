package com.huawei.msdp.movement;

public interface HwMSDPMovementStatusChangeCallback {
    void onMovementStatusChanged(int i, HwMSDPMovementChangeEvent hwMSDPMovementChangeEvent);
}
