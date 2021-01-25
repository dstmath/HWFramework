package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwparttelephony.BuildConfig;

public class HwFullNetworkManagerUtils {
    private static HwFullNetworkManagerUtils sInstance;

    public static synchronized HwFullNetworkManagerUtils getInstance() {
        HwFullNetworkManagerUtils hwFullNetworkManagerUtils;
        synchronized (HwFullNetworkManagerUtils.class) {
            if (sInstance == null) {
                sInstance = new HwFullNetworkManagerUtils();
            }
            hwFullNetworkManagerUtils = sInstance;
        }
        return hwFullNetworkManagerUtils;
    }

    public void setSimContactLoaded(int slotId, boolean isLoaded) {
        HwFullNetworkManager.getInstance().setSimContactLoaded(slotId, isLoaded);
    }

    public boolean isIsSmart4gDsdxEnable() {
        return HuaweiTelephonyConfigs.isMTKPlatform() && "smart".equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", BuildConfig.FLAVOR));
    }
}
