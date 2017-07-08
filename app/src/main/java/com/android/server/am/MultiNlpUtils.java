package com.android.server.am;

import android.content.Intent;
import android.util.Slog;
import com.android.server.LocationManagerServiceUtil;

public class MultiNlpUtils {
    private static final String TAG = "LocationManagerServiceUtil";

    public static boolean shouldSkipGoogleNlp(Intent intent, String processName) {
        if (processName != null && LocationManagerServiceUtil.GOOGLE_NETWORK_PROCESS.equals(processName)) {
            if (LocationManagerServiceUtil.getDefault() == null || !LocationManagerServiceUtil.getDefault().isMultiNlpEnable() || !LocationManagerServiceUtil.getDefault().skipGooglePrompt || 1 == LocationManagerServiceUtil.getDefault().googleMapState) {
                Slog.w(TAG, "shouldSkipGoogleNlp : return false");
            } else {
                Slog.w(TAG, "shouldSkipGoogleNlp : return true for process google network");
                return true;
            }
        }
        if (!(processName == null || intent == null || ((!"android.location.PROVIDERS_CHANGED".equals(intent.getAction()) && !"com.android.settings.location.MODE_CHANGING".equals(intent.getAction())) || (!LocationManagerServiceUtil.GOOGLE_GMS_PROCESS.equals(processName) && !LocationManagerServiceUtil.GOOGLE_GMS_UI_PROCESS.equals(processName) && !"com.google.android.gms.persistent".equals(processName))))) {
            if (LocationManagerServiceUtil.getDefault() == null || !LocationManagerServiceUtil.getDefault().isMultiNlpEnable() || !LocationManagerServiceUtil.getDefault().skipGooglePrompt || 1 == LocationManagerServiceUtil.getDefault().googleMapState) {
                Slog.w(TAG, "shouldSkipGoogleNlp : return false");
            } else {
                Slog.w(TAG, "shouldSkipGoogleNlp : return true for gms process for action " + intent.getAction());
                return true;
            }
        }
        return false;
    }
}
