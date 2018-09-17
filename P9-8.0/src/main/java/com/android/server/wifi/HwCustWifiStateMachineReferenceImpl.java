package com.android.server.wifi;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.atomic.AtomicInteger;

public class HwCustWifiStateMachineReferenceImpl extends HwCustWifiStateMachineReference {
    private static final String TAG = "HwCustWifiStateMachineReferenceImpl";

    public boolean setHwCustCountryCode(WifiNative wn) {
        String wifiCountry = SystemProperties.get("ro.huawei.cust.wifi.country", null);
        if (TextUtils.isEmpty(wifiCountry)) {
            return false;
        }
        wn.setCountryCode(wifiCountry);
        Log.d(TAG, "done set country code , wifiCountry is  " + wifiCountry);
        return true;
    }

    public boolean setHwCustWifiBand(WifiNative wn, AtomicInteger fb) {
        int bandcust = SystemProperties.getInt("ro.huawei.cust.wifi.band", -1);
        if (bandcust >= 0 && bandcust <= 2) {
            Log.d(TAG, "Failed to set frequency bandcust " + bandcust);
        }
        return false;
    }
}
