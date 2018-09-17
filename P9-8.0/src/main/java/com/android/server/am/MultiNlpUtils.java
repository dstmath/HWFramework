package com.android.server.am;

import android.content.Intent;
import android.util.Slog;
import com.android.server.LocationManagerServiceUtil;

public class MultiNlpUtils {
    private static final String TAG = "LocationManagerServiceUtil";

    public static boolean shouldSkipGoogleNlp(Intent intent, String processName) {
        if (processName == null) {
            return false;
        }
        if (LocationManagerServiceUtil.GOOGLE_NETWORK_PROCESS.equals(processName)) {
            if (isCanSkipGoogleNlp()) {
                Slog.w(TAG, "shouldSkipGoogleNlp : return true for process google network");
                return true;
            }
            Slog.w(TAG, "shouldSkipGoogleNlp : return false");
        }
        if (intent != null && (("android.location.PROVIDERS_CHANGED".equals(intent.getAction()) || "com.android.settings.location.MODE_CHANGING".equals(intent.getAction())) && (LocationManagerServiceUtil.GOOGLE_GMS_PROCESS.equals(processName) || LocationManagerServiceUtil.GOOGLE_GMS_UI_PROCESS.equals(processName) || "com.google.android.gms.persistent".equals(processName)))) {
            if (isCanSkipGoogleNlp()) {
                Slog.w(TAG, "shouldSkipGoogleNlp : return true for gms process for action " + intent.getAction());
                return true;
            }
            Slog.w(TAG, "shouldSkipGoogleNlp : return false");
        }
        return false;
    }

    private static boolean isCanSkipGoogleNlp() {
        if (LocationManagerServiceUtil.getDefault() != null && LocationManagerServiceUtil.getDefault().isMultiNlpEnable() && LocationManagerServiceUtil.getDefault().skipGooglePrompt) {
            return 1 != LocationManagerServiceUtil.getDefault().googleMapState;
        } else {
            return false;
        }
    }
}
