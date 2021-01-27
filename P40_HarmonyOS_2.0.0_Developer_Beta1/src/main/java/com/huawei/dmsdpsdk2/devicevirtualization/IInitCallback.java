package com.huawei.dmsdpsdk2.devicevirtualization;

public interface IInitCallback {
    void onBinderDied();

    void onInitFail(int i);

    void onInitSuccess(VirtualizationAdapter virtualizationAdapter);
}
