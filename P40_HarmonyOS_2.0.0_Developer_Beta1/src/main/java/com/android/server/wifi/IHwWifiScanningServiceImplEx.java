package com.android.server.wifi;

import android.net.wifi.ScanResult;
import java.util.List;

public interface IHwWifiScanningServiceImplEx {
    void updateScanResultByWifiPro(List<ScanResult> list);
}
