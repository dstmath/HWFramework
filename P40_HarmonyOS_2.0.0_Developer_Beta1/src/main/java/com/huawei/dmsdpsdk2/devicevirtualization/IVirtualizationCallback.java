package com.huawei.dmsdpsdk2.devicevirtualization;

public interface IVirtualizationCallback {
    long getSecureFileSize(String str);

    void onDeviceChange(String str, int i);

    void onPinCode(String str, String str2);

    byte[] readSecureFile(String str);

    boolean writeSecureFile(String str, byte[] bArr);
}
