package com.android.server.wifi.hwUtil;

public interface IScanResultRecords {
    int getWifiCategory(String str);

    void recordWifiCategory(String str, int i);
}
