package com.android.internal.util;

import android.app.Notification;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import java.util.Objects;

public class NotificationMessagingUtil {
    private static final String DEFAULT_SMS_APP_SETTING = "sms_default_application";
    private final Context mContext;
    private ArrayMap<Integer, String> mDefaultSmsApp = new ArrayMap<>();
    private final ContentObserver mSmsContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (Settings.Secure.getUriFor(NotificationMessagingUtil.DEFAULT_SMS_APP_SETTING).equals(uri)) {
                NotificationMessagingUtil.this.cacheDefaultSmsApp(userId);
            }
        }
    };

    public NotificationMessagingUtil(Context context) {
        this.mContext = context;
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(DEFAULT_SMS_APP_SETTING), false, this.mSmsContentObserver);
    }

    public boolean isImportantMessaging(StatusBarNotification sbn, int importance) {
        boolean z = false;
        if (importance < 2) {
            return false;
        }
        if (hasMessagingStyle(sbn) || (isCategoryMessage(sbn) && isDefaultMessagingApp(sbn))) {
            z = true;
        }
        return z;
    }

    public boolean isMessaging(StatusBarNotification sbn) {
        return hasMessagingStyle(sbn) || isDefaultMessagingApp(sbn) || isCategoryMessage(sbn);
    }

    private boolean isDefaultMessagingApp(StatusBarNotification sbn) {
        int userId = sbn.getUserId();
        if (userId == -10000 || userId == -1) {
            return false;
        }
        if (this.mDefaultSmsApp.get(Integer.valueOf(userId)) == null) {
            cacheDefaultSmsApp(userId);
        }
        return Objects.equals(this.mDefaultSmsApp.get(Integer.valueOf(userId)), sbn.getPackageName());
    }

    /* access modifiers changed from: private */
    public void cacheDefaultSmsApp(int userId) {
        this.mDefaultSmsApp.put(Integer.valueOf(userId), Settings.Secure.getStringForUser(this.mContext.getContentResolver(), DEFAULT_SMS_APP_SETTING, userId));
    }

    private boolean hasMessagingStyle(StatusBarNotification sbn) {
        return Notification.MessagingStyle.class.equals(sbn.getNotification().getNotificationStyle());
    }

    private boolean isCategoryMessage(StatusBarNotification sbn) {
        return "msg".equals(sbn.getNotification().category);
    }
}
