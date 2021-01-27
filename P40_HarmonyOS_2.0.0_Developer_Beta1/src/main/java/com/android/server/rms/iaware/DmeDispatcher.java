package com.android.server.rms.iaware;

import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;

public class DmeDispatcher {
    private static final String TAG = "RDMEDispatcher";

    public static void dispatchPolicy(RPolicyData policy) {
        if (policy == null) {
            AwareLog.e(TAG, "dispatchPolicy null policy!");
        }
    }
}
