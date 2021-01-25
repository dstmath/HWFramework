package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import java.util.List;

public interface IHwScanRequestProxyInner {
    Context getContext();

    List<ScanResult> getScanResult();

    WifiScanner getWifiScanner();

    boolean hwRetrieveWifiScannerIfNecessary();
}
