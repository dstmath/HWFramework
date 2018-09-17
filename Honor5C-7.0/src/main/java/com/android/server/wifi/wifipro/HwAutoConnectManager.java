package com.android.server.wifi.wifipro;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.HwArpVerifier;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwAutoConnectManager {
    private static final String COUNTRY_CODE_CN = "460";
    private static final long DELAYED_TIME_DISCONNECT = 500;
    private static final long DELAYED_TIME_HTTP_RECHECK = 1000;
    private static final long DELAYED_TIME_LAUNCH_BROWSER = 500;
    private static final long DELAYED_TIME_STATUS_BAR = 500;
    private static final long DELAYED_TIME_SWITCH_WIFI = 2000;
    private static final long DELAYED_TIME_WIFI_ICON = 200;
    private static final int MAX_NO_INET_HTTP_TIMES = 1;
    private static final int MAX_PORTAL_HTTP_TIMES = 2;
    private static final long MONITOR_INTERVAL_MS = 1500;
    private static final int MSG_BACKGROUND_CONN_LEAVE = 111;
    private static final int MSG_CHECK_PORTAL_NETWORK = 101;
    private static final int MSG_DISCONNECT_NETWORK = 102;
    private static final int MSG_HTTP_RECHECK = 113;
    private static final int MSG_LAUNCH_BROWSER = 112;
    private static final int MSG_MONITOR_ENTER_SETTINGS = 105;
    private static final int MSG_NO_INTERNET_RECOVERY_CHECK = 104;
    private static final int MSG_PORTAL_CANCELED = 108;
    private static final int MSG_PORTAL_OUT_OF_RANGE = 109;
    private static final int MSG_PORTAL_SELECTED = 107;
    private static final int MSG_PORTAL_STATUS_BAR = 106;
    private static final int MSG_RECV_NETWORK_DISCONNECTED = 103;
    private static final int MSG_SWITCH_WIFI_FOREGROUND = 115;
    private static final int MSG_USER_PRESENT = 114;
    private static final int MSG_WIFI_CLOSED = 110;
    private static final long PORTAL_BACKGROUND_CONN_LIVE_TIME = 30000;
    private static final String PORTAL_STATUS_BAR_TAG = "wifipro_portal_status_bar";
    private static final String RECHECK_REASON_TYPE = "recheck_reason_type";
    private static final String TAG = "HwAutoConnectManager";
    private static final int TYPE_WEBVIEW_LOAD_COMPLETE = 101;
    private AtomicBoolean mBackGroundRunning;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Handler mHandler;
    private IntentFilter mIntentFilter;
    private Object mNetworkCheckLock;
    private HwNetworkPropertyRechecker mNetworkPropertyRechecker;
    private int mPortalNotificationId;
    private String mPortalNotifiedSsid;
    private WifiConfiguration mPortalNotifyDelayWifiConfig;
    private AtomicBoolean mPortalNotifyDelayed;
    private PowerManager mPowerManager;
    private WifiManager mWifiManager;
    private WifiProUIDisplayManager mWifiProUIDisplayManager;
    private AsyncChannel mWsmChannel;

    /* renamed from: com.android.server.wifi.wifipro.HwAutoConnectManager.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case HwAutoConnectManager.TYPE_WEBVIEW_LOAD_COMPLETE /*101*/:
                    if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                        int resultReason = -1;
                        if (msg.obj != null) {
                            resultReason = msg.obj.getInt(HwAutoConnectManager.RECHECK_REASON_TYPE);
                        }
                        HwAutoConnectManager hwAutoConnectManager = HwAutoConnectManager.this;
                        int i = msg.arg1;
                        int i2 = msg.arg2;
                        if (resultReason == HwAutoConnectManager.TYPE_WEBVIEW_LOAD_COMPLETE) {
                            z = true;
                        }
                        hwAutoConnectManager.handleRespCodeForPortal(i, i2, z);
                        break;
                    }
                    break;
                case HwAutoConnectManager.MSG_DISCONNECT_NETWORK /*102*/:
                case HwAutoConnectManager.MSG_BACKGROUND_CONN_LEAVE /*111*/:
                    if (WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                        HwAutoConnectManager.this.LOGW("MSG_DISCONNECT_NETWORK|MSG_BACKGROUND_CONN_LEAVE msg handled");
                        HwAutoConnectManager.this.mWifiManager.disconnect();
                        break;
                    }
                    break;
                case HwAutoConnectManager.MSG_RECV_NETWORK_DISCONNECTED /*103*/:
                    HwAutoConnectManager.this.mBackGroundRunning.set(false);
                    WifiProCommonUtils.portalBackgroundStatusChanged(false);
                    HwAutoConnectManager.this.removeDelayedMessage(HwAutoConnectManager.MSG_RECV_NETWORK_DISCONNECTED);
                    break;
                case HwAutoConnectManager.MSG_NO_INTERNET_RECOVERY_CHECK /*104*/:
                    if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                        HwAutoConnectManager.this.LOGD("MSG_NO_INTERNET_RECOVERY_CHECK, counter = " + msg.arg1 + ", resp = " + msg.arg2);
                        HwAutoConnectManager.this.handleCheckingResult(msg.arg2, msg.what);
                        break;
                    }
                    break;
                case HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS /*105*/:
                    if (WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                        if (!WifiProCommonUtils.isQueryActivityMatched(HwAutoConnectManager.this.mContext, "com.android.settings.Settings$WifiSettingsActivity")) {
                            HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS), HwAutoConnectManager.MONITOR_INTERVAL_MS);
                            break;
                        }
                        HwAutoConnectManager.this.LOGD("User enters WlanSettings, disconnect the \"background\" connection immediately.");
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_DISCONNECT_NETWORK), 0);
                        break;
                    }
                    break;
                case HwAutoConnectManager.MSG_PORTAL_STATUS_BAR /*106*/:
                    HwAutoConnectManager.this.showPortalStatusBar();
                    break;
                case HwAutoConnectManager.MSG_PORTAL_SELECTED /*107*/:
                    HwAutoConnectManager.this.mPortalNotificationId = -1;
                    HwAutoConnectManager.this.mPortalNotifiedSsid = null;
                    if (HwAutoConnectManager.this.mHandler.hasMessages(HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS)) {
                        HwAutoConnectManager.this.LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                        HwAutoConnectManager.this.mHandler.removeMessages(HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS);
                    }
                    if (HwAutoConnectManager.this.mHandler.hasMessages(HwAutoConnectManager.MSG_DISCONNECT_NETWORK)) {
                        HwAutoConnectManager.this.LOGW("MSG_DISCONNECT_NETWORK msg removed");
                        HwAutoConnectManager.this.mHandler.removeMessages(HwAutoConnectManager.MSG_DISCONNECT_NETWORK);
                    }
                    if (HwAutoConnectManager.this.mHandler.hasMessages(HwAutoConnectManager.MSG_BACKGROUND_CONN_LEAVE)) {
                        HwAutoConnectManager.this.LOGW("MSG_BACKGROUND_CONN_LEAVE msg removed");
                        HwAutoConnectManager.this.mHandler.removeMessages(HwAutoConnectManager.MSG_BACKGROUND_CONN_LEAVE);
                        HwAutoConnectManager.this.switchWifiForeground();
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_LAUNCH_BROWSER), HwAutoConnectManager.DELAYED_TIME_STATUS_BAR);
                    } else {
                        try {
                            PendingIntent settings = TaskStackBuilder.create(HwAutoConnectManager.this.mContext).addNextIntentWithParentStack(new Intent("android.settings.WIFI_SETTINGS")).getPendingIntent(0, 0, null, UserHandle.CURRENT);
                            if (settings != null) {
                                HwAutoConnectManager.this.setAutoConnectFlag(true);
                                settings.send();
                            }
                        } catch (CanceledException e) {
                        }
                    }
                    HwAutoConnectManager.this.mWsmChannel.sendMessage(131875, 0, 0, HwAutoConnectManager.this.mPortalNotifiedSsid);
                    WifiProStatisticsManager.getInstance().increasePortalRefusedButUserTouchCnt();
                    break;
                case HwAutoConnectManager.MSG_PORTAL_CANCELED /*108*/:
                    HwAutoConnectManager.this.mPortalNotificationId = -1;
                    HwAutoConnectManager.this.mPortalNotifiedSsid = null;
                    if (HwAutoConnectManager.this.mHandler.hasMessages(HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS)) {
                        HwAutoConnectManager.this.LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                        HwAutoConnectManager.this.mHandler.removeMessages(HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS);
                    }
                    HwAutoConnectManager.this.mWsmChannel.sendMessage(131875, 0, 0, HwAutoConnectManager.this.mPortalNotifiedSsid);
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_DISCONNECT_NETWORK), HwAutoConnectManager.DELAYED_TIME_STATUS_BAR);
                    break;
                case HwAutoConnectManager.MSG_PORTAL_OUT_OF_RANGE /*109*/:
                    if (!(HwAutoConnectManager.this.mWifiProUIDisplayManager == null || HwAutoConnectManager.this.mPortalNotificationId == -1)) {
                        HwAutoConnectManager.this.LOGD("MSG_PORTAL_OUT_OF_RANGE, nid = " + HwAutoConnectManager.this.mPortalNotificationId + ", ssid = " + HwAutoConnectManager.this.mPortalNotifiedSsid);
                        HwAutoConnectManager.this.mWifiProUIDisplayManager.cancelPortalNotificationStatusBar(HwAutoConnectManager.PORTAL_STATUS_BAR_TAG, HwAutoConnectManager.this.mPortalNotificationId);
                        HwAutoConnectManager.this.mPortalNotifiedSsid = null;
                        HwAutoConnectManager.this.mPortalNotificationId = -1;
                        HwAutoConnectManager.this.mWsmChannel.sendMessage(131875, 0, 0, HwAutoConnectManager.this.mPortalNotifiedSsid);
                    }
                    HwAutoConnectManager.this.setAutoConnectFlag(false);
                    break;
                case HwAutoConnectManager.MSG_LAUNCH_BROWSER /*112*/:
                    HwAutoConnectManager.this.launchBrowserForPortalLogin();
                    break;
                case HwAutoConnectManager.MSG_HTTP_RECHECK /*113*/:
                    boolean z2;
                    Bundle bundle;
                    HwAutoConnectManager.this.LOGD("MSG_HTTP_RECHECK, type = " + msg.arg1 + ", last counter = " + msg.arg2);
                    HwAutoConnectManager hwAutoConnectManager2 = HwAutoConnectManager.this;
                    if (msg.arg1 == HwAutoConnectManager.TYPE_WEBVIEW_LOAD_COMPLETE) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    int i3 = msg.arg2 + HwAutoConnectManager.MAX_NO_INET_HTTP_TIMES;
                    msg.arg2 = i3;
                    if (msg.obj != null) {
                        bundle = (Bundle) msg.obj;
                    } else {
                        bundle = null;
                    }
                    new NetworkCheckThread(z2, i3, bundle).start();
                    break;
                case HwAutoConnectManager.MSG_USER_PRESENT /*114*/:
                    if (!HwAutoConnectManager.this.mHandler.hasMessages(HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS)) {
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS), PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                    }
                    HwAutoConnectManager.this.resetPortalNotifyDelay();
                    break;
                case HwAutoConnectManager.MSG_SWITCH_WIFI_FOREGROUND /*115*/:
                    HwAutoConnectManager.this.mWsmChannel.sendMessage(131874, HwAutoConnectManager.MAX_PORTAL_HTTP_TIMES);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private class NetworkCheckThread extends Thread {
        private Bundle bundle;
        private int checkCounter;
        private boolean portalNetwork;

        public NetworkCheckThread(boolean portal, int counter, Bundle bundle) {
            this.portalNetwork = portal;
            this.checkCounter = counter;
            this.bundle = bundle;
        }

        public void run() {
            synchronized (HwAutoConnectManager.this.mNetworkCheckLock) {
                int respCode = HwAutoConnectManager.this.mNetworkPropertyRechecker.syncRequestNetworkCheck(this.portalNetwork, false);
                int msg = this.portalNetwork ? HwAutoConnectManager.TYPE_WEBVIEW_LOAD_COMPLETE : HwAutoConnectManager.MSG_NO_INTERNET_RECOVERY_CHECK;
                if (HwAutoConnectManager.this.mBackGroundRunning.get() && WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, msg, this.checkCounter, respCode, this.bundle));
                }
            }
        }
    }

    public HwAutoConnectManager(Context context, AsyncChannel asyncChannel, NetworkQosMonitor networkQosMonitor) {
        this.mNetworkCheckLock = new Object();
        this.mBackGroundRunning = new AtomicBoolean(false);
        this.mPortalNotifyDelayed = new AtomicBoolean(false);
        this.mPortalNotificationId = -1;
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWsmChannel = asyncChannel;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mNetworkPropertyRechecker = networkQosMonitor.getNetworkPropertyRechecker();
        this.mWifiProUIDisplayManager = networkQosMonitor.getWifiProUIDisplayManager();
        Log.d(TAG, "HwAutoConnectManager init Complete! ");
    }

    public void init() {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND);
        this.mIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND);
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER");
        this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER");
        this.mIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE);
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.intent.action.USER_PRESENT");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
        HandlerThread handlerThread = new HandlerThread("wifipro_auto_conn_manager_handler_thread");
        handlerThread.start();
        this.mHandler = new AnonymousClass1(handlerThread.getLooper());
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND received.");
                    HwAutoConnectManager.this.mBackGroundRunning.set(true);
                    new NetworkCheckThread(true, HwAutoConnectManager.MAX_NO_INET_HTTP_TIMES, null).start();
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS), HwAutoConnectManager.MONITOR_INTERVAL_MS);
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_RECV_NETWORK_DISCONNECTED));
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_PORTAL_OUT_OF_RANGE));
                    }
                } else if (WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND received.");
                    HwAutoConnectManager.this.mBackGroundRunning.set(true);
                    new NetworkCheckThread(false, HwAutoConnectManager.MAX_NO_INET_HTTP_TIMES, null).start();
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_MONITOR_ENTER_SETTINGS), HwAutoConnectManager.MONITOR_INTERVAL_MS);
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER".equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_PORTAL_USED_BY_USER received.");
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_PORTAL_SELECTED), HwAutoConnectManager.DELAYED_TIME_SWITCH_WIFI);
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER".equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_PORTAL_CANCELED_BY_USER received.");
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_PORTAL_CANCELED));
                } else if (WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_PORTAL_OUT_OF_RANGE received.");
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_PORTAL_OUT_OF_RANGE));
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) && !HwAutoConnectManager.this.mWifiManager.isWifiEnabled()) {
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_PORTAL_OUT_OF_RANGE));
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WifiproUtils.PERMISSION_RECV_WIFI_CONNECTED_CONCURRENTLY, null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_USER_PRESENT received, has PortalNotifyDelayed = " + HwAutoConnectManager.this.mPortalNotifyDelayed.get());
                    if (HwAutoConnectManager.this.mPortalNotifyDelayed.get()) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, HwAutoConnectManager.MSG_USER_PRESENT));
                    }
                }
            }
        }, intentFilter);
        setAutoConnectFlag(false);
    }

    private void showPortalStatusBar() {
        if (this.mWifiProUIDisplayManager != null && this.mPortalNotificationId == -1 && this.mPortalNotifyDelayWifiConfig != null && !TextUtils.isEmpty(this.mPortalNotifyDelayWifiConfig.SSID) && !this.mPortalNotifyDelayWifiConfig.SSID.equals("<unknown ssid>")) {
            this.mPortalNotificationId = new SecureRandom().nextInt(100000);
            this.mPortalNotifiedSsid = this.mPortalNotifyDelayWifiConfig.SSID;
            this.mWifiProUIDisplayManager.showPortalNotificationStatusBar(this.mPortalNotifiedSsid, PORTAL_STATUS_BAR_TAG, this.mPortalNotificationId);
            WifiProCommonUtils.portalBackgroundStatusChanged(true);
            this.mWsmChannel.sendMessage(131875, MAX_NO_INET_HTTP_TIMES, 0, this.mPortalNotifiedSsid);
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_BACKGROUND_CONN_LEAVE), PORTAL_BACKGROUND_CONN_LIVE_TIME);
            WifiProStatisticsManager.getInstance().increasePortalNoAutoConnCnt();
            this.mPortalNotifyDelayWifiConfig = null;
        }
    }

    private void resetPortalNotifyDelay() {
        if (this.mPortalNotifyDelayed.get() && this.mPortalNotifyDelayWifiConfig != null) {
            this.mPortalNotifyDelayed.set(false);
            WifiConfiguration currConfig = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
            if (currConfig == null || currConfig.configKey() == null || !currConfig.configKey().equals(this.mPortalNotifyDelayWifiConfig.configKey())) {
                LOGD("resetPortalNotifyDelay Reset portalCheckStatus UNKNOWN");
                this.mPortalNotifyDelayWifiConfig.portalCheckStatus = 0;
                this.mPortalNotifyDelayWifiConfig.noInternetAccess = false;
                WifiProCommonUtils.updateWifiConfig(this.mPortalNotifyDelayWifiConfig, HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, this.mWsmChannel);
                this.mPortalNotifyDelayWifiConfig = null;
            } else {
                LOGD("resetPortalNotifyDelay show MSG_PORTAL_STATUS_BAR");
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_PORTAL_STATUS_BAR), DELAYED_TIME_STATUS_BAR);
            }
        }
    }

    private void removeDelayedMessage(int reason) {
        if (reason == MSG_RECV_NETWORK_DISCONNECTED) {
            if (this.mHandler.hasMessages(TYPE_WEBVIEW_LOAD_COMPLETE)) {
                LOGW("MSG_CHECK_PORTAL_NETWORK msg removed");
                this.mHandler.removeMessages(TYPE_WEBVIEW_LOAD_COMPLETE);
            }
            if (this.mHandler.hasMessages(MSG_DISCONNECT_NETWORK)) {
                LOGW("MSG_DISCONNECT_NETWORK msg removed");
                this.mHandler.removeMessages(MSG_DISCONNECT_NETWORK);
            }
            if (this.mHandler.hasMessages(MSG_BACKGROUND_CONN_LEAVE)) {
                LOGW("MSG_BACKGROUND_CONN_LEAVE msg removed");
                this.mHandler.removeMessages(MSG_BACKGROUND_CONN_LEAVE);
            }
            if (this.mHandler.hasMessages(MSG_MONITOR_ENTER_SETTINGS)) {
                LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                this.mHandler.removeMessages(MSG_MONITOR_ENTER_SETTINGS);
            }
            if (this.mHandler.hasMessages(MSG_LAUNCH_BROWSER)) {
                LOGW("MSG_LAUNCH_BROWSER msg removed");
                this.mHandler.removeMessages(MSG_MONITOR_ENTER_SETTINGS);
            }
            if (this.mHandler.hasMessages(MSG_PORTAL_STATUS_BAR)) {
                LOGW("MSG_PORTAL_STATUS_BAR msg removed");
                this.mHandler.removeMessages(MSG_PORTAL_STATUS_BAR);
            }
            if (this.mHandler.hasMessages(MSG_SWITCH_WIFI_FOREGROUND)) {
                this.mHandler.removeMessages(MSG_SWITCH_WIFI_FOREGROUND);
            }
        }
    }

    private void handleRespCodeForPortal(int counter, int respCode, boolean portalLastTime) {
        LOGD("handleRespCodeForPortal, current counter = " + counter + ", resp = " + respCode + ", recheck portal = " + portalLastTime);
        if (!WifiProCommonUtils.unreachableRespCode(respCode) || counter >= MAX_PORTAL_HTTP_TIMES) {
            if (WifiProCommonUtils.isRedirectedRespCode(respCode) && !portalLastTime) {
                boolean accoutLogin;
                boolean oversea = useOperatorOverSea();
                PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
                if (oversea) {
                    accoutLogin = false;
                } else {
                    accoutLogin = database.syncQueryPortalNetwork(WifiProCommonUtils.getCurrentSsid(this.mWifiManager));
                }
                boolean withinTrusted = WifiProCommonUtils.withinTrustedPeriod(WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager), 259200000);
                LOGD("handleRespCodeForPortal, accoutLogin = " + accoutLogin + ", withinTrusted = " + withinTrusted + ", oversea = " + oversea);
                if (!accoutLogin && withinTrusted) {
                    int type = this.mNetworkPropertyRechecker.syncRecheckBasedOnWebView(oversea);
                    LOGD("handleRespCodeForPortal, end of webview loading, type = " + type);
                    if (type == TYPE_WEBVIEW_LOAD_COMPLETE) {
                        respCode = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putInt(RECHECK_REASON_TYPE, TYPE_WEBVIEW_LOAD_COMPLETE);
                        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_HTTP_RECHECK, TYPE_WEBVIEW_LOAD_COMPLETE, counter, bundle), DELAYED_TIME_HTTP_RECHECK);
                        return;
                    }
                }
            } else if (WifiProCommonUtils.unreachableRespCode(respCode) && portalLastTime) {
                respCode = 302;
            }
            handleCheckingResult(respCode, TYPE_WEBVIEW_LOAD_COMPLETE);
            return;
        }
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_HTTP_RECHECK, TYPE_WEBVIEW_LOAD_COMPLETE, counter), DELAYED_TIME_HTTP_RECHECK);
    }

    private void handleCheckingResult(int respCode, int type) {
        LOGD("handleCheckingResult, respCode = " + respCode + ", type = " + type);
        WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config != null) {
            boolean openNoInternet = false;
            if (type == TYPE_WEBVIEW_LOAD_COMPLETE) {
                config.internetRecoveryCheckTimestamp = System.currentTimeMillis();
                if (respCode == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                    config.portalCheckStatus = MAX_NO_INET_HTTP_TIMES;
                    config.noInternetAccess = false;
                    config.validatedInternetAccess = true;
                    config.wifiProNoInternetAccess = false;
                } else if (WifiProCommonUtils.isRedirectedRespCode(respCode)) {
                    config.portalCheckStatus = MAX_PORTAL_HTTP_TIMES;
                    config.portalNetwork = true;
                    config.noInternetAccess = false;
                    config.wifiProNoInternetAccess = true;
                    config.wifiProNoInternetReason = MAX_NO_INET_HTTP_TIMES;
                } else if (WifiProCommonUtils.unreachableRespCode(respCode)) {
                    if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, MSG_RECV_NETWORK_DISCONNECTED)) {
                        openNoInternet = true;
                        config.noInternetAccess = true;
                        config.validatedInternetAccess = false;
                        config.wifiProNoInternetAccess = true;
                        config.wifiProNoInternetReason = 0;
                        config.wifiProNoHandoverNetwork = false;
                        config.internetHistory = NetworkHistoryUtils.insertWifiConfigHistory(config.internetHistory, 0);
                    } else if (WifiProCommonUtils.isPortalAPHaveInternetLastTime(config)) {
                        LOGD("handleCheckingResult connect portal AP because it's history have internet");
                        config.portalCheckStatus = MAX_NO_INET_HTTP_TIMES;
                        config.noInternetAccess = false;
                        config.validatedInternetAccess = true;
                        config.wifiProNoInternetAccess = false;
                    } else {
                        config.portalCheckStatus = MAX_PORTAL_HTTP_TIMES;
                        config.portalNetwork = true;
                        config.noInternetAccess = false;
                        config.wifiProNoInternetAccess = true;
                        config.wifiProNoInternetReason = MAX_NO_INET_HTTP_TIMES;
                    }
                }
            } else if (type == MSG_NO_INTERNET_RECOVERY_CHECK) {
                config.internetRecoveryCheckTimestamp = System.currentTimeMillis();
                config.internetRecoveryStatus = respCode == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC ? 5 : 4;
                if (config.internetRecoveryStatus == 5) {
                    config.noInternetAccess = false;
                    config.validatedInternetAccess = true;
                    config.internetHistory = NetworkHistoryUtils.insertWifiConfigHistory(config.internetHistory, MAX_NO_INET_HTTP_TIMES);
                }
            }
            WifiProCommonUtils.updateWifiConfig(config, HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, this.mWsmChannel);
            LOGD("handleCheckingResult, isScreenOn = " + this.mPowerManager.isScreenOn() + ", nid = " + this.mPortalNotificationId + ", status = " + config.portalCheckStatus + ", noInetOpen = " + openNoInternet);
            if (type == TYPE_WEBVIEW_LOAD_COMPLETE) {
                if (config.portalCheckStatus == MAX_NO_INET_HTTP_TIMES) {
                    if (this.mHandler.hasMessages(MSG_MONITOR_ENTER_SETTINGS)) {
                        this.mHandler.removeMessages(MSG_MONITOR_ENTER_SETTINGS);
                    }
                    switchWifiForeground();
                } else if (this.mPortalNotificationId != -1 || openNoInternet) {
                    this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_DISCONNECT_NETWORK), DELAYED_TIME_STATUS_BAR);
                } else if (config.portalCheckStatus == MAX_PORTAL_HTTP_TIMES && this.mPowerManager.isScreenOn()) {
                    this.mPortalNotifyDelayWifiConfig = config;
                    this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_PORTAL_STATUS_BAR), DELAYED_TIME_STATUS_BAR);
                } else if (config.portalCheckStatus == MAX_PORTAL_HTTP_TIMES && !this.mPowerManager.isScreenOn()) {
                    this.mPortalNotifyDelayed.set(true);
                    this.mPortalNotifyDelayWifiConfig = config;
                    if (this.mHandler.hasMessages(MSG_MONITOR_ENTER_SETTINGS)) {
                        LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                        this.mHandler.removeMessages(MSG_MONITOR_ENTER_SETTINGS);
                    }
                }
            } else if (type == MSG_NO_INTERNET_RECOVERY_CHECK) {
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_DISCONNECT_NETWORK), DELAYED_TIME_STATUS_BAR);
            }
        }
    }

    private boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0 || operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        }
        return true;
    }

    private void switchWifiForeground() {
        this.mWsmChannel.sendMessage(131873);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_SWITCH_WIFI_FOREGROUND), DELAYED_TIME_WIFI_ICON);
    }

    private void launchBrowserForPortalLogin() {
        try {
            URL url = new URL(HwArpVerifier.WEB_BAIDU);
            if (useOperatorOverSea()) {
                url = new URL("http://connectivitycheck.android.com/generate_204");
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url.toString()));
            intent.setFlags(272629760);
            try {
                intent.setClassName(PortalAutoFillManager.BROWSER_PACKET_NAME, "com.android.browser.BrowserActivity");
                this.mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                try {
                    this.mContext.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url.toString())));
                } catch (ActivityNotFoundException e2) {
                    Log.e(TAG, "startActivity failed, message : ActivityNotFoundException rcvd.");
                }
            }
        } catch (MalformedURLException e3) {
        }
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }

    private void setAutoConnectFlag(boolean flag) {
        LOGD("setAutoConnectFlag flag  = " + flag);
        int autoConnect = flag ? MAX_NO_INET_HTTP_TIMES : 0;
        if (this.mContext != null) {
            Global.putInt(this.mContext.getContentResolver(), "wifi_pro_auto_connect_portal_flag", autoConnect);
        }
    }
}
