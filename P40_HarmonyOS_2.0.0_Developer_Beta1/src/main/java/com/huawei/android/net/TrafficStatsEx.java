package com.huawei.android.net;

import android.net.TrafficStats;
import com.huawei.annotation.HwSystemApi;

public class TrafficStatsEx {
    public static final int UID_REMOVED = -4;
    public static final int UID_TETHERING = -5;

    @HwSystemApi
    public static long getTxPackets(String iface) {
        return TrafficStats.getTxPackets(iface);
    }

    @HwSystemApi
    public static long getRxPackets(String iface) {
        return TrafficStats.getRxPackets(iface);
    }

    @HwSystemApi
    public static String[] getMobileIfacesEx() {
        return TrafficStats.getMobileIfacesEx();
    }
}
