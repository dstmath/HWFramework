package com.android.server.wifi.hwUtil;

import android.net.wifi.ScanResult;

public interface IHwInformationElementUtilEx {
    int getWifiCategoryFromIes(ScanResult.InformationElement[] informationElementArr, int i);
}
