package com.huawei.android.security;

import android.security.KeyStore;
import android.util.Log;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class KeyStoreEx {
    private static final String TAG = "KeyStoreEx";

    public static int addAuthToken(byte[] authToken) {
        KeyStore instance = KeyStore.getInstance();
        if (instance != null) {
            return instance.addAuthToken(authToken);
        }
        Log.e(TAG, "addAuthToken: KeyStore.getInstance() is null!");
        return SubscriptionManagerEx.INVALID_SLOT_ID;
    }
}
