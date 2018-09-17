package com.huawei.internal.telephony;

import com.android.internal.telephony.VirtualNet;

public class VirtualNetEx {
    public static boolean isVirtualNet() {
        return VirtualNet.isVirtualNet();
    }

    public static String getApnFilter() {
        return VirtualNet.getApnFilter();
    }

    public static boolean isVirtualNet(int subId) {
        return VirtualNet.isVirtualNet(subId);
    }

    public static String getApnFilter(int subId) {
        return VirtualNet.getApnFilter(subId);
    }
}
