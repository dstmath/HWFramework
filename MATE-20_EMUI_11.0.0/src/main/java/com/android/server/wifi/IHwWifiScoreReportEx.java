package com.android.server.wifi;

import android.net.wifi.WifiInfo;

public interface IHwWifiScoreReportEx {
    int getScore();

    boolean isScoreCalculated(WifiInfo wifiInfo, int i);

    void setLowScoreCount(int i);
}
