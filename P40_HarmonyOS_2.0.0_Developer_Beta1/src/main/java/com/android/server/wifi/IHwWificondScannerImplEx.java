package com.android.server.wifi;

import android.net.wifi.ScanResult;

public interface IHwWificondScannerImplEx {
    StringBuffer getResultsString();

    void pollLatestScanData(ScanResult scanResult, long j, boolean z);
}
