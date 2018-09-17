package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Looper;
import com.android.internal.app.IBatteryStats;
import com.android.server.wifi.scanner.WifiScannerImpl.WifiScannerImplFactory;
import com.android.server.wifi.scanner.WifiScanningServiceImpl;
import com.android.server.wifi.wifipro.WifiProConfigStore;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.List;

public class HwWifiScanningServiceImpl extends WifiScanningServiceImpl {
    private Context mContext;

    public HwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        super(context, looper, scannerImplFactory, batteryStats, wifiInjector);
        this.mContext = context;
    }

    public void updateScanResultByWifiPro(List<ScanResult> scanResults) {
        if (scanResults != null && WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
            for (int i = 0; i < scanResults.size(); i++) {
                WifiProConfigStore.updateScanDetailByWifiPro((ScanResult) scanResults.get(i));
            }
        }
    }
}
