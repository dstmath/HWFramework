package com.huawei.nearbysdk.clone;

public interface CloneAdapterCallback {
    void onAdapterGet(CloneAdapter cloneAdapter);

    void onBinderDied();
}
