package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.List;

public class HwWifiScanningServiceImplEx implements IHwWifiScanningServiceImplEx {
    private static HwWifiScanningServiceImplEx mHwWifiScanningServiceImplEx = null;
    private Context mContext;
    private HwWifiProServiceManager mHwWifiProServiceManager;

    private HwWifiScanningServiceImplEx(Context context) {
        this.mContext = context;
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
    }

    public static HwWifiScanningServiceImplEx createHwWifiScanningServiceImplEx(Context context) {
        if (mHwWifiScanningServiceImplEx == null) {
            mHwWifiScanningServiceImplEx = new HwWifiScanningServiceImplEx(context);
        }
        return mHwWifiScanningServiceImplEx;
    }

    public void updateScanResultByWifiPro(List<ScanResult> scanResults) {
        List<ScanResult> scanResultLists;
        if (!(scanResults == null || !WifiProCommonUtils.isWifiProSwitchOn(this.mContext) || (scanResultLists = this.mHwWifiProServiceManager.updateScanResultByWifiPro(scanResults)) == null)) {
            int wifiLength = scanResultLists.size();
            int scanLength = scanResults.size();
            int arrayCopySize = wifiLength > scanLength ? scanLength : wifiLength;
            for (int i = 0; i < arrayCopySize; i++) {
                ScanResult scanResult = scanResults.get(i);
                ScanResult scanResultlist = scanResultLists.get(i);
                scanResult.internetAccessType = scanResultlist.internetAccessType;
                scanResult.networkQosLevel = scanResultlist.networkQosLevel;
                scanResult.networkSecurity = scanResultlist.networkSecurity;
                scanResult.networkQosScore = scanResultlist.networkQosScore;
            }
        }
    }
}
