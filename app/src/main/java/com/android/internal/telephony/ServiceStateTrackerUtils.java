package com.android.internal.telephony;

import android.telephony.ServiceState;

public class ServiceStateTrackerUtils {
    private static final String TAG = "ServiceStateTrackerUtils";

    public static ServiceState getNewSS(ServiceStateTracker sst) {
        if (sst != null) {
            return sst.mNewSS;
        }
        return null;
    }
}
