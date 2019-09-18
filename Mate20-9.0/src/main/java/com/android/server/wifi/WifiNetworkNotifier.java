package com.android.server.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import java.util.List;

public class WifiNetworkNotifier {
    private static final String TAG = "WifiNetworkNotifier";
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    private final Handler mHandler;
    private Notification mNetworkNotification;
    private NotificationManager mNotificationManager = ((NotificationManager) this.mContext.getSystemService("notification"));
    private boolean mScreenOn = false;
    /* access modifiers changed from: private */
    public boolean mSettingEnabled;

    private class NotificationEnabledSettingObserver extends ContentObserver {
        NotificationEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            WifiNetworkNotifier.this.mFrameworkFacade.registerContentObserver(WifiNetworkNotifier.this.mContext, Settings.Secure.getUriFor("wifi_networks_available_notification_on"), true, this);
            boolean unused = WifiNetworkNotifier.this.mSettingEnabled = getValue();
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean unused = WifiNetworkNotifier.this.mSettingEnabled = getValue();
            WifiNetworkNotifier.this.clearPendingNotification();
        }

        private boolean getValue() {
            boolean enabled = true;
            if (WifiNetworkNotifier.this.mFrameworkFacade.getIntegerSetting(WifiNetworkNotifier.this.mContext, "wifi_networks_available_notification_on", 1) != 1) {
                enabled = false;
            }
            return enabled;
        }
    }

    WifiNetworkNotifier(Context context, Looper looper, FrameworkFacade framework) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mFrameworkFacade = framework;
        new NotificationEnabledSettingObserver(this.mHandler).register();
    }

    private boolean isControllerEnabled() {
        return this.mSettingEnabled;
    }

    public void handleScanResults(List<ScanDetail> availableNetworks) {
        if (!isControllerEnabled()) {
            Log.d(TAG, "settings not enabled");
        } else if (availableNetworks.isEmpty()) {
            Log.d(TAG, "availableNetworks is empty");
        } else if (this.mScreenOn) {
            showAvailableNetworkNotification();
        }
    }

    private void showAvailableNetworkNotification() {
        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
        intent.addFlags(268435456);
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
        Resources resource = Resources.getSystem();
        CharSequence title = resource.getText(33686257);
        CharSequence message = resource.getText(33686256);
        Notification.Builder builder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_AVAILABLE);
        builder.setWhen(0).setSmallIcon(17303481).setContentTitle(title).setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setContentIntent(pi).setAutoCancel(true);
        if (this.mNetworkNotification == null) {
            Log.d(TAG, "showAvailableNetworkNotification");
            this.mNetworkNotification = builder.build();
            this.mNotificationManager.notifyAsUser(null, this.mNetworkNotification.icon, this.mNetworkNotification, UserHandle.ALL);
        }
    }

    public void clearPendingNotification() {
        if (this.mNetworkNotification != null) {
            Log.d(TAG, "clearPendingNotification");
            this.mNotificationManager.cancelAsUser(null, this.mNetworkNotification.icon, UserHandle.ALL);
            this.mNetworkNotification = null;
        }
    }

    public void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
    }
}
