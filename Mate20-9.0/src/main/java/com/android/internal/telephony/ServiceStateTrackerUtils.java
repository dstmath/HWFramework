package com.android.internal.telephony;

import android.os.SystemProperties;
import android.telephony.ServiceState;

public class ServiceStateTrackerUtils {
    private static final String TAG = "ServiceStateTrackerUtils";

    public static ServiceState getNewSS(ServiceStateTracker sst) {
        if (sst != null) {
            return sst.mNewSS;
        }
        return null;
    }

    public static boolean isDocomo() {
        return SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    }

    public static boolean isDocomoTablet() {
        return SystemProperties.get("ro.product.custom", "NULL").contains("docomo") && "tablet".equals(SystemProperties.get("ro.build.characteristics", "tablet"));
    }
}
