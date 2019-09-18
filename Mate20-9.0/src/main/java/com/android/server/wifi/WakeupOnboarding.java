package com.android.server.wifi;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WakeupConfigStoreData;

public class WakeupOnboarding {
    @VisibleForTesting
    static final int NOTIFICATIONS_UNTIL_ONBOARDED = 3;
    private static final long NOT_SHOWN_TIMESTAMP = -1;
    @VisibleForTesting
    static final long REQUIRED_NOTIFICATION_DELAY = 86400000;
    private static final String TAG = "WakeupOnboarding";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x0057  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x005d  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0089  */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode == -1067607823) {
                if (action.equals(WakeupNotificationFactory.ACTION_TURN_OFF_WIFI_WAKE)) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -506616242) {
                if (action.equals(WakeupNotificationFactory.ACTION_OPEN_WIFI_PREFERENCES)) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 1771495157 && action.equals(WakeupNotificationFactory.ACTION_DISMISS_NOTIFICATION)) {
                c = 2;
                switch (c) {
                    case 0:
                        WakeupOnboarding.this.mFrameworkFacade.setIntegerSetting(WakeupOnboarding.this.mContext, "wifi_wakeup_enabled", 0);
                        WakeupOnboarding.this.dismissNotification(true);
                        return;
                    case 1:
                        WakeupOnboarding.this.mContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                        WakeupOnboarding.this.mContext.startActivity(new Intent("android.settings.WIFI_IP_SETTINGS").addFlags(268435456));
                        WakeupOnboarding.this.dismissNotification(true);
                        return;
                    case 2:
                        WakeupOnboarding.this.dismissNotification(true);
                        return;
                    default:
                        Log.e(WakeupOnboarding.TAG, "Unknown action " + intent.getAction());
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    private final Handler mHandler;
    private final IntentFilter mIntentFilter;
    private boolean mIsNotificationShowing;
    /* access modifiers changed from: private */
    public boolean mIsOnboarded;
    private long mLastShownTimestamp = -1;
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public int mTotalNotificationsShown;
    private final WakeupNotificationFactory mWakeupNotificationFactory;
    private final WifiConfigManager mWifiConfigManager;

    private class IsOnboardedDataSource implements WakeupConfigStoreData.DataSource<Boolean> {
        private IsOnboardedDataSource() {
        }

        public Boolean getData() {
            return Boolean.valueOf(WakeupOnboarding.this.mIsOnboarded);
        }

        public void setData(Boolean data) {
            boolean unused = WakeupOnboarding.this.mIsOnboarded = data.booleanValue();
        }
    }

    private class NotificationsDataSource implements WakeupConfigStoreData.DataSource<Integer> {
        private NotificationsDataSource() {
        }

        public Integer getData() {
            return Integer.valueOf(WakeupOnboarding.this.mTotalNotificationsShown);
        }

        public void setData(Integer data) {
            int unused = WakeupOnboarding.this.mTotalNotificationsShown = data.intValue();
        }
    }

    public WakeupOnboarding(Context context, WifiConfigManager wifiConfigManager, Looper looper, FrameworkFacade frameworkFacade, WakeupNotificationFactory wakeupNotificationFactory) {
        this.mContext = context;
        this.mWifiConfigManager = wifiConfigManager;
        this.mHandler = new Handler(looper);
        this.mFrameworkFacade = frameworkFacade;
        this.mWakeupNotificationFactory = wakeupNotificationFactory;
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(WakeupNotificationFactory.ACTION_TURN_OFF_WIFI_WAKE);
        this.mIntentFilter.addAction(WakeupNotificationFactory.ACTION_DISMISS_NOTIFICATION);
        this.mIntentFilter.addAction(WakeupNotificationFactory.ACTION_OPEN_WIFI_PREFERENCES);
    }

    public boolean isOnboarded() {
        return this.mIsOnboarded;
    }

    public void maybeShowNotification() {
        maybeShowNotification(SystemClock.elapsedRealtime());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void maybeShowNotification(long timestamp) {
        if (shouldShowNotification(timestamp)) {
            Log.d(TAG, "Showing onboarding notification.");
            incrementTotalNotificationsShown();
            this.mIsNotificationShowing = true;
            this.mLastShownTimestamp = timestamp;
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, null, this.mHandler);
            getNotificationManager().notify(43, this.mWakeupNotificationFactory.createOnboardingNotification());
        }
    }

    private void incrementTotalNotificationsShown() {
        this.mTotalNotificationsShown++;
        if (this.mTotalNotificationsShown >= 3) {
            setOnboarded();
        } else {
            this.mWifiConfigManager.saveToStore(false);
        }
    }

    private boolean shouldShowNotification(long timestamp) {
        boolean z = false;
        if (isOnboarded() || this.mIsNotificationShowing) {
            return false;
        }
        if (this.mLastShownTimestamp == -1 || timestamp - this.mLastShownTimestamp > REQUIRED_NOTIFICATION_DELAY) {
            z = true;
        }
        return z;
    }

    public void onStop() {
        dismissNotification(false);
    }

    /* access modifiers changed from: private */
    public void dismissNotification(boolean shouldOnboard) {
        if (this.mIsNotificationShowing) {
            if (shouldOnboard) {
                setOnboarded();
            }
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            getNotificationManager().cancel(43);
            this.mIsNotificationShowing = false;
        }
    }

    public void setOnboarded() {
        if (!this.mIsOnboarded) {
            Log.d(TAG, "Setting user as onboarded.");
            this.mIsOnboarded = true;
            this.mWifiConfigManager.saveToStore(false);
        }
    }

    private NotificationManager getNotificationManager() {
        if (this.mNotificationManager == null) {
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        }
        return this.mNotificationManager;
    }

    public WakeupConfigStoreData.DataSource<Boolean> getIsOnboadedDataSource() {
        return new IsOnboardedDataSource();
    }

    public WakeupConfigStoreData.DataSource<Integer> getNotificationsDataSource() {
        return new NotificationsDataSource();
    }
}
