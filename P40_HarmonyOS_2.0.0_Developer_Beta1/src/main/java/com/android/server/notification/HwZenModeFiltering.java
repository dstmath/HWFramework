package com.android.server.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.util.Slog;
import com.android.internal.notification.SystemNotificationChannels;

public class HwZenModeFiltering {
    private static final String ACTION_SET_PREVENT_MODE = "com.huawei.android.preventmode.change";
    private static final String EXTRA_NUMBER_IS_SMS = "com.huawei.hsm.number_type_sms";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_ZEN_CALL_WHITE_LIST_ENABLED = "zen_call_white_list_enabled";
    private static final String NTF_EXTRA_DISABLE_NTF_ICON_IN_BAR = "hw_disable_ntf_icon_in_bar";
    private static final String NTF_EXTRA_DISABLE_SHOW_IN_KEYGUARD = "hw_disable_ntf_show_in_keyguard";
    private static final String PKG_SETTING = "com.android.settings";
    private static final String PREVENT_MODE_MARK = "PreventModechange";
    private static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "HwZenModeFiltering";
    private static final String TOOLBOX_NAME = "com.android.toolbox";
    private static final String ZENMODE_NOTIFICATION_TAG = "zenmode_notification_tag";
    private final Context mContext;
    private BroadcastReceiver mLocalLanguageReceiver;
    private ContentObserver mZenModeContentObserver;
    private Notification notification;
    private NotificationManager notificationManager;

    public HwZenModeFiltering(Context context) {
        this.mContext = context;
        initZenmodeChangeObserver();
    }

    public static boolean matchesCallFilter(Context context, int zen, NotificationManager.Policy consolidatedPolicy, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
        boolean isWhiteListMode = false;
        boolean isSms = extras.getBoolean(EXTRA_NUMBER_IS_SMS, false);
        Slog.w(TAG, "matchesCallFilter, isSms:" + isSms + ",allow message:" + config.allowMessages + ",allow from:" + config.allowMessagesFrom);
        if (!isSms) {
            if (Settings.Secure.getInt(context.getContentResolver(), KEY_ZEN_CALL_WHITE_LIST_ENABLED, 0) != 0) {
                isWhiteListMode = true;
            }
            if (zen != 1 || !isWhiteListMode) {
                return ZenModeFiltering.matchesCallFilter(context, zen, consolidatedPolicy, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
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
                    float contactAffinity = validator.getContactAffinity(userHandle, extras, contactsTimeoutMs, timeoutAffinity);
                    Slog.w(TAG, "matchesCallFilter , affinit:" + contactAffinity);
                    return audienceMatches(config.allowMessagesFrom, contactAffinity);
                }
            }
            return true;
        }
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        if (source == 0) {
            return true;
        }
        if (source == 1) {
            return contactAffinity >= 0.5f;
        }
        if (source == 2) {
            return contactAffinity >= 1.0f;
        }
        Slog.w(TAG, "Encountered unknown source: " + source);
        return true;
    }

    public void initZenmodeChangeObserver() {
        Slog.d(TAG, "initZenmodeChangeObserver");
        this.mZenModeContentObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.notification.HwZenModeFiltering.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                Slog.d(HwZenModeFiltering.TAG, "onChange: zen mode changed.");
                HwZenModeFiltering.this.refreshNotification();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("zen_mode"), false, this.mZenModeContentObserver);
        this.mZenModeContentObserver.onChange(true);
        registerLocalChangeReceiver();
    }

    /* access modifiers changed from: private */
    public class LocaleChangeReceiver extends BroadcastReceiver {
        private LocaleChangeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.e(HwZenModeFiltering.TAG, "onReceive: context or intent is null !!!");
                return;
            }
            String action = intent.getAction();
            Slog.i(HwZenModeFiltering.TAG, "onReceive: local language change");
            if (action != null && action.equals("android.intent.action.LOCALE_CHANGED")) {
                HwZenModeFiltering.this.refreshNotification();
            }
        }
    }

    private void buildAndSendZenmodeNotification() {
        if (this.mContext.getSystemService("notification") instanceof NotificationManager) {
            this.notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        }
        Notification.Builder builder = new Notification.Builder(this.mContext);
        builder.setContentTitle(this.mContext.getString(33686333)).setSmallIcon(33752550).setContentIntent(createToSettingsIntent()).setShowWhen(false).setAppName(this.mContext.getString(33686334)).setActions(createClosedAction());
        builder.setChannelId(SystemNotificationChannels.DO_NOT_DISTURB);
        builder.setOngoing(true);
        builder.setVisibility(1);
        this.notification = builder.build();
        this.notification.extras.putBoolean(NTF_EXTRA_DISABLE_NTF_ICON_IN_BAR, true);
        this.notification.extras.putBoolean(NTF_EXTRA_DISABLE_SHOW_IN_KEYGUARD, true);
        NotificationManager notificationManager2 = this.notificationManager;
        if (notificationManager2 != null) {
            notificationManager2.notify(ZENMODE_NOTIFICATION_TAG, 1, this.notification);
        }
    }

    private void cancelNotification() {
        NotificationManager notificationManager2 = this.notificationManager;
        if (notificationManager2 != null) {
            notificationManager2.cancel(ZENMODE_NOTIFICATION_TAG, 1);
        }
    }

    private void registerLocalChangeReceiver() {
        this.mLocalLanguageReceiver = new LocaleChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(this.mLocalLanguageReceiver, filter);
    }

    private Notification.Action createClosedAction() {
        Intent intent = new Intent(ACTION_SET_PREVENT_MODE);
        intent.putExtra(PREVENT_MODE_MARK, 0);
        intent.putExtra("package_name", TOOLBOX_NAME);
        intent.setPackage("com.android.settings");
        return new Notification.Action.Builder((Icon) null, this.mContext.getString(33686332), PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456)).build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshNotification() {
        boolean isZenmodOn = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0) == 1) {
            isZenmodOn = true;
        }
        Slog.d(TAG, "refreshNotification isZenmodOn :" + isZenmodOn);
        if (isZenmodOn) {
            buildAndSendZenmodeNotification();
        } else {
            cancelNotification();
        }
    }

    private PendingIntent createToSettingsIntent() {
        Intent intent = new Intent("android.settings.ZEN_MODE_SETTINGS");
        intent.setFlags(335544320);
        return PendingIntent.getActivity(this.mContext, 1, intent, 268435456);
    }
}
