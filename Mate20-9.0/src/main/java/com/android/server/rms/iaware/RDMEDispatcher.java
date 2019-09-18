package com.android.server.rms.iaware;

import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;

public class RDMEDispatcher {
    private static final String TAG = "RDMEDispatcher";

    public static void dispatchRPolicy(RPolicyData policy) {
        if (policy == null) {
            AwareLog.e(TAG, "dispatchRPolicy null policy!");
        }
    }
}
