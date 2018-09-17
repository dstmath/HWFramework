package com.android.server.wifi;

import android.net.wifi.WifiInfo;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class WifiConnectivityManagerUtils extends EasyInvokeUtils {
    private FieldObject<Boolean> mScreenOn;
    private FieldObject<WifiInfo> mWifiInfo;
    private FieldObject<Integer> mWifiState;
    private MethodObject<Void> startSingleScan;

    @GetField(fieldObject = "mWifiState")
    public int getWifiState(WifiConnectivityManager wifiConnectivityManager) {
        return ((Integer) getField(this.mWifiState, wifiConnectivityManager)).intValue();
    }

    @GetField(fieldObject = "mWifiInfo")
    public WifiInfo getWifiInfo(WifiConnectivityManager wifiConnectivityManager) {
        return (WifiInfo) getField(this.mWifiInfo, wifiConnectivityManager);
    }

    @GetField(fieldObject = "mScreenOn")
    public Boolean getScreenOn(WifiConnectivityManager wifiConnectivityManager) {
        return (Boolean) getField(this.mScreenOn, wifiConnectivityManager);
    }

    @InvokeMethod(methodObject = "startSingleScan")
    public void startSingleScan(WifiConnectivityManager wifiConnectivityManager, boolean isWatchdogTriggered, boolean isFullBandScan) {
        invokeMethod(this.startSingleScan, wifiConnectivityManager, new Object[]{Boolean.valueOf(isWatchdogTriggered), Boolean.valueOf(isFullBandScan)});
    }
}
