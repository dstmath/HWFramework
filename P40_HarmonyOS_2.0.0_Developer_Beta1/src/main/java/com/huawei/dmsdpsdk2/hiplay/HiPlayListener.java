package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.dmsdp.devicevirtualization.Capability;

public interface HiPlayListener {
    void onDataReceive(HiPlayDevice hiPlayDevice, int i, byte[] bArr);

    void onDeviceStateChange(DeviceState deviceState);

    void onVirtualDeviceFailed(HiPlayDevice hiPlayDevice, Capability capability, int i);

    void onVirtualDeviceSuccess(HiPlayDevice hiPlayDevice, Capability capability, int i);
}
