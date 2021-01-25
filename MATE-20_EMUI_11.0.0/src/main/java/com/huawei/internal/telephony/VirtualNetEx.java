package com.huawei.internal.telephony;

import com.huawei.android.util.NoExtAPIException;

public class VirtualNetEx {
    @Deprecated
    public static boolean isVirtualNet() {
        throw new NoExtAPIException("method not supported.");
    }

    @Deprecated
    public static String getApnFilter() {
        throw new NoExtAPIException("method not supported.");
    }

    @Deprecated
    public static boolean isVirtualNet(int subId) {
        throw new NoExtAPIException("method not supported.");
    }

    @Deprecated
    public static String getApnFilter(int subId) {
        throw new NoExtAPIException("method not supported.");
    }
}
