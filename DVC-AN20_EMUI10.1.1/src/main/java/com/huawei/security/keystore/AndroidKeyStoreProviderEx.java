package com.huawei.security.keystore;

import android.security.keystore.AndroidKeyStoreProvider;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AndroidKeyStoreProviderEx {
    @HwSystemApi
    public static long getKeyStoreOperationHandle(Object cryptoPrimitive) {
        return AndroidKeyStoreProvider.getKeyStoreOperationHandle(cryptoPrimitive);
    }
}
