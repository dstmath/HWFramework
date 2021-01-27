package com.huawei.dmsdpsdk2;

public interface DMSDPAdapterCallback {
    void onAdapterGet(DMSDPAdapter dMSDPAdapter);

    void onBinderDied();
}
