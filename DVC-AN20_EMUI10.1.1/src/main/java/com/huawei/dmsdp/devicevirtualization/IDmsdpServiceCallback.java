package com.huawei.dmsdp.devicevirtualization;

/* access modifiers changed from: package-private */
public interface IDmsdpServiceCallback {
    void onAdapterGet(VirtualService virtualService);

    void onBinderDied();
}
