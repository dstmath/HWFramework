package com.android.server.wifi;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.wifi.util.NativeUtil;

public class WrongPasswordNotifier {
    private static final long CANCEL_TIMEOUT_MILLISECONDS = 300000;
    @VisibleForTesting
    public static final int NOTIFICATION_ID = 42;
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final NotificationManager mNotificationManager = ((NotificationManager) this.mContext.getSystemService("notification"));
    private boolean mWrongPasswordDetected;

    public WrongPasswordNotifier(Context context, FrameworkFacade frameworkFacade) {
        this.mContext = context;
        this.mFrameworkFacade = frameworkFacade;
    }

    public void onWrongPasswordError(String ssid) {
        showNotification(ssid);
        this.mWrongPasswordDetected = true;
    }

    public void onNewConnectionAttempt() {
        if (this.mWrongPasswordDetected) {
            dismissNotification();
            this.mWrongPasswordDetected = false;
        }
    }

    private void showNotification(String ssid) {
        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
        intent.putExtra("wifi_start_connect_ssid", NativeUtil.removeEnclosingQuotes(ssid));
        this.mNotificationManager.notify(42, this.mFrameworkFacade.makeNotificationBuilder(this.mContext, SystemNotificationChannels.NETWORK_ALERTS).setAutoCancel(true).setTimeoutAfter(CANCEL_TIMEOUT_MILLISECONDS).setSmallIcon(17303543).setContentTitle(this.mContext.getString(17041542)).setContentText(ssid).setContentIntent(this.mFrameworkFacade.getActivity(this.mContext, 0, intent, 134217728)).setColor(this.mContext.getResources().getColor(17170460)).build());
    }

    private void dismissNotification() {
        this.mNotificationManager.cancel(null, 42);
    }
}
