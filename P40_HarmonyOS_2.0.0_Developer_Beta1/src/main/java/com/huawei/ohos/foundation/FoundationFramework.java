package com.huawei.ohos.foundation;

public class FoundationFramework {
    private static final String FOUNDATION_XML = "/system/profile/foundation.xml";
    private static final String TAG = "FoundationFramework";

    private native int initNativeFoundationService(String str);

    public void start() {
        initNativeFoundationService(FOUNDATION_XML);
    }
}
