package com.android.server.wifi;

import android.content.Context;
import android.os.Looper;
import com.android.internal.app.IBatteryStats;
import com.android.server.wifi.scanner.WifiScannerImpl;
import com.android.server.wifi.scanner.WifiScanningServiceImpl;

public class HwWifiScanningServiceImpl extends WifiScanningServiceImpl {
    private Context mContext;

    public HwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        super(context, looper, scannerImplFactory, batteryStats, wifiInjector);
        this.mContext = context;
    }
}
