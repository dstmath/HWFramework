package com.android.server.wifi.HwUtil;

import com.android.server.wifi.hwUtil.IHwConstantUtilsEx;

public class HwConstantUtils implements IHwConstantUtilsEx {
    private static HwConstantUtils mHwConstantUtils = null;

    public static synchronized HwConstantUtils getDefault() {
        HwConstantUtils hwConstantUtils;
        synchronized (HwConstantUtils.class) {
            if (mHwConstantUtils == null) {
                mHwConstantUtils = new HwConstantUtils();
            }
            hwConstantUtils = mHwConstantUtils;
        }
        return hwConstantUtils;
    }

    public int getWifiP2pDisabledStateVal() {
        return 0;
    }

    public int getWifiP2pCreateGroupPskVal() {
        return 141268;
    }

    public int getConfigurationBetaUserVal() {
        return 3;
    }

    public int getTypeWifiSurfingVal() {
        return 51;
    }
}
