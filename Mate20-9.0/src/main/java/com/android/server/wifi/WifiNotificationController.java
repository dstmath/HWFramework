package com.android.server.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class WifiNotificationController {
    private static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND";
    private static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE";
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int ICON_NETWORKS_AVAILABLE = 17303481;
    /* access modifiers changed from: private */
    public static final boolean NOTIFY_OPEN_NETWORKS_VALUE = SystemProperties.getBoolean("ro.config.notify_open_networks", false);
    private static final int NUM_SCANS_BEFORE_ACTUALLY_SCANNING = 3;
    private static final String TAG = "WifiNotificationController";
    private final long NOTIFICATION_REPEAT_DELAY_MS;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public NetworkInfo.DetailedState mDetailedState = NetworkInfo.DetailedState.IDLE;
    /* access modifiers changed from: private */
    public FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public boolean mIsAccessAPFound;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    private Notification.Builder mNotificationBuilder;
    /* access modifiers changed from: private */
    public boolean mNotificationEnabled;
    private NotificationEnabledSettingObserver mNotificationEnabledSettingObserver;
    private long mNotificationRepeatTime;
    /* access modifiers changed from: private */
    public boolean mNotificationShown;
    private int mNumScansSinceNetworkStateChange;
    /* access modifiers changed from: private */
    public WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public WifiScanner mWifiScanner;
    /* access modifiers changed from: private */
    public volatile int mWifiState = 4;

    /* renamed from: com.android.server.wifi.WifiNotificationController$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.DISCONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.IDLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.SCANNING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.AUTHENTICATING.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.OBTAINING_IPADDR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.SUSPENDED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.FAILED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.BLOCKED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.VERIFYING_POOR_LINK.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private class NotificationEnabledSettingObserver extends ContentObserver {
        public NotificationEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            WifiNotificationController.this.mFrameworkFacade.registerContentObserver(WifiNotificationController.this.mContext, Settings.Global.getUriFor("wifi_networks_available_notification_on"), true, this);
            synchronized (WifiNotificationController.this) {
                boolean unused = WifiNotificationController.this.mNotificationEnabled = getValue();
            }
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (WifiNotificationController.this) {
                boolean unused = WifiNotificationController.this.mNotificationEnabled = getValue();
                WifiNotificationController.this.resetNotification();
            }
        }

        private boolean getValue() {
            return WifiNotificationController.this.mFrameworkFacade.getIntegerSetting(WifiNotificationController.this.mContext, "wifi_networks_available_notification_on", 1) == 1;
        }
    }

    WifiNotificationController(Context context, Looper looper, FrameworkFacade framework, Notification.Builder builder, WifiInjector wifiInjector) {
        this.mContext = context;
        this.mFrameworkFacade = framework;
        this.mNotificationBuilder = builder;
        this.mWifiInjector = wifiInjector;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        filter.addAction(ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND);
        filter.addAction(ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x0084, code lost:
                if (com.android.server.wifi.WifiNotificationController.access$500(r4.this$0) != false) goto L_0x0174;
             */
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    int unused = WifiNotificationController.this.mWifiState = intent.getIntExtra("wifi_state", 4);
                    WifiNotificationController.this.resetNotification();
                } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo unused2 = WifiNotificationController.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (WifiNotificationController.this.mNetworkInfo != null) {
                        NetworkInfo.DetailedState detailedState = WifiNotificationController.this.mNetworkInfo.getDetailedState();
                        if (!(detailedState == NetworkInfo.DetailedState.SCANNING || detailedState == WifiNotificationController.this.mDetailedState)) {
                            NetworkInfo.DetailedState unused3 = WifiNotificationController.this.mDetailedState = detailedState;
                            switch (AnonymousClass2.$SwitchMap$android$net$NetworkInfo$DetailedState[WifiNotificationController.this.mDetailedState.ordinal()]) {
                                case 1:
                                    if (WifiNotificationController.this.mWifiInjector.getWifiStateMachine().isWifiProEnabled()) {
                                        break;
                                    }
                                case 2:
                                case 3:
                                    WifiNotificationController.this.resetNotification();
                                    break;
                            }
                        }
                    }
                } else if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                    if (WifiNotificationController.this.mWifiScanner == null) {
                        WifiScanner unused4 = WifiNotificationController.this.mWifiScanner = WifiNotificationController.this.mWifiInjector.getWifiScanner();
                    }
                    if (!WifiNotificationController.this.mWifiInjector.getWifiStateMachine().isWifiProEnabled() || WifiNotificationController.NOTIFY_OPEN_NETWORKS_VALUE) {
                        WifiNotificationController.this.checkAndSetNotification(WifiNotificationController.this.mNetworkInfo, WifiNotificationController.this.mWifiScanner.getSingleScanResults());
                    }
                } else if (intent.getAction().equals(WifiNotificationController.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND)) {
                    Log.d(WifiNotificationController.TAG, "find access ap, mNotificationShown = " + WifiNotificationController.this.mNotificationShown);
                    if (WifiNotificationController.this.mNotificationEnabled && !WifiNotificationController.this.mNotificationShown) {
                        boolean unused5 = WifiNotificationController.this.mIsAccessAPFound = true;
                        WifiNotificationController.this.setNotificationVisible(false, 1, false, 0);
                    }
                } else if (intent.getAction().equals(WifiNotificationController.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE)) {
                    Log.d(WifiNotificationController.TAG, "access ap , mNotificationShown = " + WifiNotificationController.this.mNotificationShown + ", mIsAccessAPFound = " + WifiNotificationController.this.mIsAccessAPFound);
                    if (WifiNotificationController.this.mIsAccessAPFound && WifiNotificationController.this.mNotificationShown) {
                        boolean unused6 = WifiNotificationController.this.mIsAccessAPFound = false;
                        WifiNotificationController.this.resetNotification();
                    }
                }
            }
        }, filter);
        this.NOTIFICATION_REPEAT_DELAY_MS = ((long) this.mFrameworkFacade.getIntegerSetting(context, "wifi_networks_available_repeat_delay", 900)) * 1000;
        this.mNotificationEnabledSettingObserver = new NotificationEnabledSettingObserver(new Handler(looper));
        this.mNotificationEnabledSettingObserver.register();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0096, code lost:
        return;
     */
    public synchronized void checkAndSetNotification(NetworkInfo networkInfo, List<ScanResult> scanResults) {
        if (this.mNotificationEnabled) {
            if (this.mWifiState == 3) {
                if (!UserManager.get(this.mContext).hasUserRestriction("no_config_wifi", UserHandle.CURRENT)) {
                    NetworkInfo.State state = NetworkInfo.State.DISCONNECTED;
                    if (networkInfo != null) {
                        state = networkInfo.getState();
                    }
                    if (HWFLOW) {
                        Log.i(TAG, "checkAndSetNotification, state:" + state);
                    }
                    if (state == NetworkInfo.State.DISCONNECTED || state == NetworkInfo.State.UNKNOWN) {
                        if (scanResults != null) {
                            int numOpenNetworks = 0;
                            for (int i = scanResults.size() - 1; i >= 0; i--) {
                                ScanResult scanResult = scanResults.get(i);
                                if (scanResult.capabilities != null && scanResult.capabilities.equals("[ESS]")) {
                                    numOpenNetworks++;
                                }
                            }
                            Log.d(TAG, "Open network num:" + numOpenNetworks);
                            if (numOpenNetworks > 0) {
                                int i2 = this.mNumScansSinceNetworkStateChange + 1;
                                this.mNumScansSinceNetworkStateChange = i2;
                                if (i2 >= 3) {
                                    setNotificationVisible(NOTIFY_OPEN_NETWORKS_VALUE, numOpenNetworks, false, 0);
                                }
                            }
                        } else {
                            Log.d(TAG, "scanResults is null");
                        }
                    }
                    setNotificationVisible(false, 0, false, 0);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public synchronized void resetNotification() {
        this.mNotificationRepeatTime = 0;
        this.mNumScansSinceNetworkStateChange = 0;
        setNotificationVisible(false, 0, false, 0);
    }

    /* access modifiers changed from: private */
    public void setNotificationVisible(boolean visible, int numNetworks, boolean force, int delay) {
        if (visible || this.mNotificationShown || force) {
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (!visible) {
                notificationManager.cancelAsUser(null, 13, UserHandle.ALL);
            } else if (System.currentTimeMillis() >= this.mNotificationRepeatTime) {
                if (this.mNotificationBuilder == null) {
                    this.mNotificationBuilder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_AVAILABLE).setWhen(0).setSmallIcon(ICON_NETWORKS_AVAILABLE).setAutoCancel(true).setContentIntent(TaskStackBuilder.create(this.mContext).addNextIntentWithParentStack(new Intent("android.net.wifi.PICK_WIFI_NETWORK")).getPendingIntent(0, 0, null, UserHandle.CURRENT)).setColor(this.mContext.getResources().getColor(17170784));
                    Bitmap bmp = BitmapFactory.decodeResource(this.mContext.getResources(), 33751683);
                    if (bmp != null) {
                        this.mNotificationBuilder.setLargeIcon(bmp);
                    }
                }
                CharSequence title = this.mContext.getResources().getQuantityText(18153501, numNetworks);
                CharSequence details = this.mContext.getResources().getQuantityText(18153502, numNetworks);
                this.mNotificationBuilder.setTicker(title);
                this.mNotificationBuilder.setContentTitle(title);
                this.mNotificationBuilder.setContentText(details);
                this.mNotificationRepeatTime = System.currentTimeMillis() + this.NOTIFICATION_REPEAT_DELAY_MS;
                Notification build = this.mNotificationBuilder.build();
                notificationManager.notifyAsUser(null, 13, this.mNotificationBuilder.build(), UserHandle.ALL);
            } else {
                return;
            }
            this.mNotificationShown = visible;
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mNotificationEnabled " + this.mNotificationEnabled);
        pw.println("mNotificationRepeatTime " + this.mNotificationRepeatTime);
        pw.println("mNotificationShown " + this.mNotificationShown);
        pw.println("mNumScansSinceNetworkStateChange " + this.mNumScansSinceNetworkStateChange);
    }
}
