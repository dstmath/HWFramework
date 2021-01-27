package com.huawei.dmsdp.devicevirtualization;

public interface IDiscoveryCallback {
    void onFound(VirtualDevice virtualDevice, int i);

    void onState(int i);
}
