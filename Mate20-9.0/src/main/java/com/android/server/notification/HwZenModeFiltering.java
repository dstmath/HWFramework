package com.android.server.notification;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.util.Slog;

public class HwZenModeFiltering {
    private static final String EXTRA_NUMBER_IS_SMS = "com.huawei.hsm.number_type_sms";
    private static final String KEY_ZEN_CALL_WHITE_LIST_ENABLED = "zen_call_white_list_enabled";
    private static final String TAG = "HwZenModeFiltering";

    public static boolean matchesCallFilter(Context context, int zen, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
        boolean isSms;
        float contactAffinity;
        boolean whiteListMode = false;
        Slog.w(TAG, "matchesCallFilter, isSms:" + isSms + ",allow message:" + config.allowMessages + ",allow from:" + config.allowMessagesFrom);
        if (!isSms) {
            if (Settings.Secure.getInt(context.getContentResolver(), KEY_ZEN_CALL_WHITE_LIST_ENABLED, 0) != 0) {
                whiteListMode = true;
            }
            if (zen != 1 || !whiteListMode) {
                return ZenModeFiltering.matchesCallFilter(context, zen, config, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
            }
            return ZenModeFiltering.isRepeatCall(context, zen, config, extras);
        } else if (zen == 2 || zen == 3) {
            return false;
        } else {
            if (zen == 1) {
                if (!config.allowMessages) {
                    return false;
                }
                if (validator != null) {
                    Slog.w(TAG, "matchesCallFilter , affinit:" + contactAffinity);
                    return audienceMatches(config.allowMessagesFrom, contactAffinity);
                }
            }
            return true;
        }
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        boolean z = false;
        switch (source) {
            case 0:
                return true;
            case 1:
                if (contactAffinity >= 0.5f) {
                    z = true;
                }
                return z;
            case 2:
                if (contactAffinity >= 1.0f) {
                    z = true;
                }
                return z;
            default:
                Slog.w(TAG, "Encountered unknown source: " + source);
                return true;
        }
    }
}
