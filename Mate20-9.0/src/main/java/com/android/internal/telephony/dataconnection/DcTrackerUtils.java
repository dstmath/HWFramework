package com.android.internal.telephony.dataconnection;

import android.telephony.Rlog;

public class DcTrackerUtils {
    private static final String TAG = "DcTrackerUtils";

    public static boolean cleanUpAllConnections(DcTracker tracker, boolean tearDown, String reason) {
        log("cleanUpAllConnections !");
        if (tracker != null) {
            return tracker.cleanUpAllConnections(tearDown, reason);
        }
        return false;
    }

    public static void onTrySetupData(DcTracker tracker, String reason) {
        log("onTrySetupData !");
        tracker.onTrySetupData(reason);
    }

    public static void cleanUpConnection(DcTracker tracker, boolean tearDown, ApnContext apnContext) {
        log("cleanUpConnection!");
        if (tracker != null) {
            tracker.cleanUpConnection(tearDown, apnContext);
        }
    }

    private static void log(String string) {
        Rlog.d(TAG, string);
    }
}
