package com.huawei.dmsdp.devicevirtualization;

public interface IVirtualDeviceObserver {
    void onDeviceCapabilityStateChange(VirtualDevice virtualDevice, Capability capability, int i);

    void onDeviceStateChange(VirtualDevice virtualDevice, int i);
}
