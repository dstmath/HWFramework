package com.android.server.am;

import android.app.PendingIntent;
import android.content.IIntentSender;

public class HwActivityManagerServiceUtil {
    static final boolean DEBUG_CONSUMPTION = false;
    static final String TAG = "HwActivityManagerServiceUtil";

    public static boolean isPendingIntentCanceled(PendingIntent intent) {
        if (intent == null) {
            return DEBUG_CONSUMPTION;
        }
        IIntentSender target = intent.getTarget();
        if (target == null || !(target instanceof PendingIntentRecord)) {
            return DEBUG_CONSUMPTION;
        }
        return ((PendingIntentRecord) target).canceled;
    }
}
