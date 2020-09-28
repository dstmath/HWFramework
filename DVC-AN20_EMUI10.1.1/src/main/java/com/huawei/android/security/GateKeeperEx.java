package com.huawei.android.security;

import android.security.GateKeeper;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class GateKeeperEx {
    public static long getSecureUserId() {
        return GateKeeper.getSecureUserId();
    }
}
