package com.huawei.dmsdpsdk2;

public interface DataListener {
    void onDataReceive(DMSDPDevice dMSDPDevice, int i, byte[] bArr);
}
