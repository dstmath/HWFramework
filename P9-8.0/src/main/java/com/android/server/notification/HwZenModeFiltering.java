package com.android.server.notification;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.ZenModeConfig;
import android.util.Slog;

public class HwZenModeFiltering {
    private static final String EXTRA_NUMBER_IS_SMS = "com.huawei.hsm.number_type_sms";
    private static final String TAG = "HwZenModeFiltering";

    public static boolean matchesCallFilter(Context context, int zen, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
        boolean isSms = extras.getBoolean(EXTRA_NUMBER_IS_SMS, false);
        Slog.w(TAG, "matchesCallFilter, isSms:" + isSms + ",allow message:" + config.allowMessages + ",allow from:" + config.allowMessagesFrom);
        if (!isSms) {
            return ZenModeFiltering.matchesCallFilter(context, zen, config, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
        }
        if (zen == 2 || zen == 3) {
            return false;
        }
        if (zen == 1) {
            if (!config.allowMessages) {
                return false;
            }
            if (validator != null) {
                float contactAffinity = validator.getContactAffinity(userHandle, extras, contactsTimeoutMs, timeoutAffinity);
                Slog.w(TAG, "matchesCallFilter , affinit:" + contactAffinity);
                return audienceMatches(config.allowMessagesFrom, contactAffinity);
            }
        }
        return true;
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        boolean z = true;
        switch (source) {
            case 0:
                return true;
            case 1:
                if (contactAffinity < 0.5f) {
                    z = false;
                }
                return z;
            case 2:
                if (contactAffinity < 1.0f) {
                    z = false;
                }
                return z;
            default:
                Slog.w(TAG, "Encountered unknown source: " + source);
                return true;
        }
    }
}
