package com.android.server.wifi;

import android.content.Context;

public interface HwWifiDFTUtil {
    void checkAndCreatWifiLogDir();

    void clearSwCnt();

    short getAccessNetFailedCount();

    int getDFTEventType(int i);

    int getScanAlwaysSwCnt();

    boolean getWifiAlwaysScanState();

    boolean getWifiNetworkNotificationState();

    int getWifiNotifationSwCnt();

    boolean getWifiProState();

    int getWifiProSwcnt();

    byte getWifiSleepPolicyState();

    int getWifiSleepSwCnt();

    byte getWifiToPdpState();

    int getWifiToPdpSwCnt();

    void updateWifiDFTEvent(int i, String str);

    void updateWifiNetworkNotificationState(Context context);

    void updateWifiProState(Context context);

    void updateWifiScanAlwaysState(Context context);

    void updateWifiSleepPolicyState(Context context);

    void updateWifiToPdpState(Context context);
}
