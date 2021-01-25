package com.android.server.am;

import android.app.PendingIntent;
import android.content.IIntentSender;

public class HwActivityManagerServiceUtil {
    static final boolean IS_DEBUG_CONSUMPTION = false;
    static final String TAG = "HwActivityManagerServiceUtil";

    public static boolean isPendingIntentCanceled(PendingIntent intent) {
        if (intent == null) {
            return false;
        }
        IIntentSender target = intent.getTarget();
        if (target instanceof PendingIntentRecord) {
            return ((PendingIntentRecord) target).canceled;
        }
        return false;
    }
}
