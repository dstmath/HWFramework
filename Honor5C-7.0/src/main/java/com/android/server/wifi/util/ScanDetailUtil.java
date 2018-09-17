package com.android.server.wifi.util;

import android.net.wifi.ScanResult;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail;

public class ScanDetailUtil {
    private ScanDetailUtil() {
    }

    public static ScanDetail toScanDetail(ScanResult scanResult) {
        return new ScanDetail(scanResult, new NetworkDetail(scanResult.BSSID, scanResult.informationElements, scanResult.anqpLines, scanResult.frequency, scanResult.capabilities), null);
    }
}
