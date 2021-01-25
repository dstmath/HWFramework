package com.android.server.wifi;

import android.os.SystemProperties;
import android.util.wifi.HwHiLog;

public class HwWifiApConfigStoreEx implements IHwWifiApConfigStoreEx {
    private static final String TAG = "HwWifiApConfigStoreEx";
    private static final int WAPI_CERT_CONST_VALUE_IN_L = 5;
    private static final int WAPI_PSK_CONST_VALUE_IN_L = 4;
    private static final int WPA2_PSK_CONST_VALUE_IN_L = 6;
    private static HwWifiApConfigStoreEx mHwWifiApConfigStoreEx;

    public static HwWifiApConfigStoreEx getDefault() {
        if (mHwWifiApConfigStoreEx == null) {
            mHwWifiApConfigStoreEx = new HwWifiApConfigStoreEx();
        }
        return mHwWifiApConfigStoreEx;
    }

    public int mapApAuth(int softApAuthType, int softApversion) {
        if (softApversion == 3) {
            return softApAuthType;
        }
        String connectivity_chipType = SystemProperties.get("ro.connectivity.chiptype");
        HwHiLog.d(TAG, false, "connectivity_chipType = %{public}s", new Object[]{connectivity_chipType});
        if (softApversion != 1 || !"Qualcomm".equalsIgnoreCase(connectivity_chipType)) {
            return softApAuthType;
        }
        if (softApAuthType == 4) {
            return 16;
        }
        if (softApAuthType == 5) {
            return 17;
        }
        if (softApAuthType != 6) {
            return softApAuthType;
        }
        return 4;
    }
}
