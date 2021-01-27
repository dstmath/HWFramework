package com.huawei.distributed.net;

public class DistributedNetworkConstants {
    public static final int BANDWIDTH_KBPS_BLUETOOTH = 1600;
    public static final String DNS_SERVER1 = "114.114.114.114";
    public static final String DNS_SERVER2 = "114.114.115.115";
    public static final String NETWORK_NAME = "Distributed";
    public static final String NET_IFACE_ADDRESS = "127.0.0.1";
    public static final String NET_IFACE_NAME = "lo";
    public static final int NET_MTU = 1500;
    public static final int NET_SUBTYPE_ID = 0;
    public static final String NET_TYPE_NAME = "HW_DISTRIBUTED_NET";
    public static final String REASON_CONNECTED = "CONNECTED";
    public static final String REASON_DISCONNECTED = "DISCONNECTED";
    public static final String REASON_UNWANTED = "UNWANTED";
    public static final int SCORE_HIGH_PRIORITY = 70;
    public static final int SCORE_MIDDLE_PROIORITY = 55;
    public static final String SUBTYPE_NAME_BLUETOOTH = "BLUETOOTH";

    private DistributedNetworkConstants() {
    }

    public static int getNetworkScoreWithChargingState(boolean isCharging) {
        return isCharging ? 55 : 70;
    }
}
