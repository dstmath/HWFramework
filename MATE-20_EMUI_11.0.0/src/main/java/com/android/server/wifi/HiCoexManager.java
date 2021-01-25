package com.android.server.wifi;

import java.util.List;

public interface HiCoexManager {
    List<Integer> getRecommendWiFiChannel();

    void notifyForegroundScan(boolean z, String str);

    void notifyWifiConnecting(boolean z);
}
