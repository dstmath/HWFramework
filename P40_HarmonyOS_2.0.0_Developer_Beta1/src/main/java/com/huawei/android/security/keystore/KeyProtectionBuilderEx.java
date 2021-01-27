package com.huawei.android.security.keystore;

import android.security.keystore.KeyProtection;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class KeyProtectionBuilderEx {
    private KeyProtectionBuilderEx() {
    }

    public static KeyProtection.Builder setCriticalToDeviceEncryption(KeyProtection.Builder builder, boolean critical) {
        return builder.setCriticalToDeviceEncryption(critical);
    }
}
