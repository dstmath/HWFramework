package com.android.server.wifi.wifipro;

import android.app.KeyguardManager;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HwArpVerifier;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.LAA.HwLaaController;
import com.android.server.wifi.LAA.HwLaaUtils;
import com.android.server.wifi.SavedNetworkEvaluator;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
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
    private AtomicBoolean mBackGroundRunning = new AtomicBoolean(false);
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Handler mHandler;
    private IntentFilter mIntentFilter;
    private KeyguardManager mKeyguardManager;
    private boolean mLaaDiabledRequest = false;
    private Object mNetworkCheckLock = new Object();
    private HwNetworkPropertyRechecker mNetworkPropertyRechecker;
    private int mPortalNotificationId = -1;
    private String mPortalNotifiedSsid;
    private WifiConfiguration mPortalNotifyDelayWifiConfig;
    private AtomicBoolean mPortalNotifyDelayed = new AtomicBoolean(false);
    private PowerManager mPowerManager;
    private SavedNetworkEvaluator mSavedNetworkEvaluator;
    private WifiInjector mWifiInjector;
    private WifiManager mWifiManager;
    private WifiProUIDisplayManager mWifiProUIDisplayManager;
    private WifiStateMachine mWifiStateMachine;

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
                int respCode = HwAutoConnectManager.this.mNetworkPropertyRechecker.syncRequestNetworkCheck(this.portalNetwork, false, true);
                int msg = this.portalNetwork ? 101 : 104;
                if (HwAutoConnectManager.this.mBackGroundRunning.get() && WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, msg, this.checkCounter, respCode, this.bundle));
                }
            }
        }
    }

    public HwAutoConnectManager(Context context, NetworkQosMonitor networkQosMonitor) {
        this.mContext = context;
        this.mWifiInjector = WifiInjector.getInstance();
        this.mWifiStateMachine = this.mWifiInjector.getWifiStateMachine();
        this.mSavedNetworkEvaluator = this.mWifiInjector.getSavedNetworkEvaluator();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
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
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                boolean z = true;
                boolean z2 = false;
                int i;
                switch (msg.what) {
                    case 101:
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            int resultReason = -1;
                            if (msg.obj != null) {
                                resultReason = msg.obj.getInt(HwAutoConnectManager.RECHECK_REASON_TYPE);
                            }
                            HwAutoConnectManager hwAutoConnectManager = HwAutoConnectManager.this;
                            int i2 = msg.arg1;
                            i = msg.arg2;
                            if (resultReason != 101) {
                                z = false;
                            }
                            hwAutoConnectManager.handleRespCodeForPortal(i2, i, z);
                            break;
                        }
                        break;
                    case 102:
                    case 111:
                        if (WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                            HwAutoConnectManager.this.LOGW("MSG_DISCONNECT_NETWORK|MSG_BACKGROUND_CONN_LEAVE msg handled");
                            HwAutoConnectManager.this.mWifiManager.disconnect();
                            break;
                        }
                        break;
                    case 103:
                        HwAutoConnectManager.this.mBackGroundRunning.set(false);
                        WifiProCommonUtils.portalBackgroundStatusChanged(false);
                        HwAutoConnectManager.this.removeDelayedMessage(103);
                        if (HwAutoConnectManager.this.mLaaDiabledRequest) {
                            HwAutoConnectManager.this.mLaaDiabledRequest = false;
                            if (HwLaaUtils.isLaaPlusEnable() && HwLaaController.getInstrance() != null) {
                                HwLaaController.getInstrance().setLAAEnabled(true, 4);
                                break;
                            }
                        }
                        break;
                    case 104:
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            HwAutoConnectManager.this.LOGD("MSG_NO_INTERNET_RECOVERY_CHECK, counter = " + msg.arg1 + ", resp = " + msg.arg2);
                            HwAutoConnectManager.this.handleCheckingResult(msg.arg2, msg.what);
                            break;
                        }
                        break;
                    case 105:
                        if (WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                            if (!WifiProCommonUtils.isQueryActivityMatched(HwAutoConnectManager.this.mContext, "com.android.settings.Settings$WifiSettingsActivity")) {
                                HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 105), HwAutoConnectManager.MONITOR_INTERVAL_MS);
                                break;
                            }
                            HwAutoConnectManager.this.LOGD("User enters WlanSettings, disconnect the \"background\" connection immediately.");
                            HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 102), 0);
                            break;
                        }
                        break;
                    case 106:
                        HwAutoConnectManager.this.showPortalStatusBar();
                        break;
                    case 107:
                        HwAutoConnectManager.this.mPortalNotificationId = -1;
                        HwAutoConnectManager.this.mPortalNotifiedSsid = null;
                        if (HwAutoConnectManager.this.mHandler.hasMessages(105)) {
                            HwAutoConnectManager.this.LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                            HwAutoConnectManager.this.mHandler.removeMessages(105);
                        }
                        if (HwAutoConnectManager.this.mHandler.hasMessages(102)) {
                            HwAutoConnectManager.this.LOGW("MSG_DISCONNECT_NETWORK msg removed");
                            HwAutoConnectManager.this.mHandler.removeMessages(102);
                        }
                        if (HwAutoConnectManager.this.mHandler.hasMessages(111)) {
                            HwAutoConnectManager.this.LOGW("MSG_BACKGROUND_CONN_LEAVE msg removed");
                            HwAutoConnectManager.this.mHandler.removeMessages(111);
                            HwAutoConnectManager.this.switchWifiForeground();
                            HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 112), 500);
                        } else {
                            try {
                                PendingIntent settings = TaskStackBuilder.create(HwAutoConnectManager.this.mContext).addNextIntentWithParentStack(new Intent("android.settings.WIFI_SETTINGS")).getPendingIntent(0, 0, null, UserHandle.CURRENT);
                                if (settings != null) {
                                    HwAutoConnectManager.this.setUserSelectPortalFlag(true);
                                    settings.send();
                                }
                            } catch (CanceledException e) {
                            }
                        }
                        HwAutoConnectManager.this.notifyPortalStatusChanged(false, HwAutoConnectManager.this.mPortalNotifiedSsid);
                        WifiProStatisticsManager.getInstance().increasePortalRefusedButUserTouchCnt();
                        break;
                    case 108:
                        HwAutoConnectManager.this.mPortalNotificationId = -1;
                        HwAutoConnectManager.this.mPortalNotifiedSsid = null;
                        if (HwAutoConnectManager.this.mHandler.hasMessages(105)) {
                            HwAutoConnectManager.this.LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                            HwAutoConnectManager.this.mHandler.removeMessages(105);
                        }
                        HwAutoConnectManager.this.notifyPortalStatusChanged(false, HwAutoConnectManager.this.mPortalNotifiedSsid);
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 102), 500);
                        break;
                    case 109:
                        if (!(HwAutoConnectManager.this.mWifiProUIDisplayManager == null || HwAutoConnectManager.this.mPortalNotificationId == -1)) {
                            HwAutoConnectManager.this.LOGD("MSG_PORTAL_OUT_OF_RANGE, nid = " + HwAutoConnectManager.this.mPortalNotificationId + ", ssid = " + HwAutoConnectManager.this.mPortalNotifiedSsid);
                            HwAutoConnectManager.this.mWifiProUIDisplayManager.cancelPortalNotificationStatusBar(HwAutoConnectManager.PORTAL_STATUS_BAR_TAG, HwAutoConnectManager.this.mPortalNotificationId);
                            HwAutoConnectManager.this.mPortalNotifiedSsid = null;
                            HwAutoConnectManager.this.mPortalNotificationId = -1;
                            HwAutoConnectManager.this.notifyPortalStatusChanged(false, HwAutoConnectManager.this.mPortalNotifiedSsid);
                        }
                        HwAutoConnectManager.this.setUserSelectPortalFlag(false);
                        break;
                    case 112:
                        HwAutoConnectManager.this.launchBrowserForPortalLogin();
                        break;
                    case 113:
                        Bundle bundle;
                        HwAutoConnectManager.this.LOGD("MSG_HTTP_RECHECK, type = " + msg.arg1 + ", last counter = " + msg.arg2);
                        HwAutoConnectManager hwAutoConnectManager2 = HwAutoConnectManager.this;
                        if (msg.arg1 == 101) {
                            z2 = true;
                        }
                        i = msg.arg2 + 1;
                        msg.arg2 = i;
                        if (msg.obj != null) {
                            bundle = (Bundle) msg.obj;
                        } else {
                            bundle = null;
                        }
                        new NetworkCheckThread(z2, i, bundle).start();
                        break;
                    case 114:
                        if (!HwAutoConnectManager.this.mHandler.hasMessages(105)) {
                            HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 105), PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        }
                        HwAutoConnectManager.this.resetPortalNotifyDelay();
                        break;
                    case 115:
                        HwAutoConnectManager.this.mWifiStateMachine.sendMessage(131874, 2);
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND received.");
                    HwAutoConnectManager.this.mBackGroundRunning.set(true);
                    if (!HwAutoConnectManager.this.mLaaDiabledRequest && WifiProCommonUtils.isWifi5GConnected(HwAutoConnectManager.this.mWifiManager)) {
                        HwAutoConnectManager.this.mLaaDiabledRequest = true;
                        if (HwLaaUtils.isLaaPlusEnable() && HwLaaController.getInstrance() != null) {
                            HwLaaController.getInstrance().setLAAEnabled(false, 4);
                        }
                    }
                    new NetworkCheckThread(true, 1, null).start();
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 105), HwAutoConnectManager.MONITOR_INTERVAL_MS);
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 103));
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 109));
                    }
                } else if (WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND received.");
                    HwAutoConnectManager.this.mBackGroundRunning.set(true);
                    new NetworkCheckThread(false, 1, null).start();
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 105), HwAutoConnectManager.MONITOR_INTERVAL_MS);
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER".equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_PORTAL_USED_BY_USER received.");
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 107), HwAutoConnectManager.DELAYED_TIME_SWITCH_WIFI);
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER".equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_PORTAL_CANCELED_BY_USER received.");
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 108));
                } else if (WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_PORTAL_OUT_OF_RANGE received.");
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 109));
                } else if (!"android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                } else {
                    if (!HwAutoConnectManager.this.mWifiManager.isWifiEnabled() && HwAutoConnectManager.this.mPowerManager.isScreenOn()) {
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 109), HwAutoConnectManager.DELAYED_TIME_SWITCH_WIFI);
                    } else if (HwAutoConnectManager.this.mWifiManager.isWifiEnabled() && HwAutoConnectManager.this.mPowerManager.isScreenOn()) {
                        HwAutoConnectManager.this.mHandler.removeMessages(109);
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WifiproUtils.PERMISSION_RECV_WIFI_CONNECTED_CONCURRENTLY, null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_USER_PRESENT received, has PortalNotifyDelayed = " + HwAutoConnectManager.this.mPortalNotifyDelayed.get());
                    if (HwAutoConnectManager.this.mPortalNotifyDelayed.get()) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 114));
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    if (HwAutoConnectManager.this.mPortalNotifyDelayed.get() && (WifiProCommonUtils.isCalling(HwAutoConnectManager.this.mContext) ^ 1) != 0 && (HwAutoConnectManager.this.mKeyguardManager.inKeyguardRestrictedInputMode() ^ 1) != 0) {
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 114), HwAutoConnectManager.DELAYED_TIME_SWITCH_WIFI);
                    }
                } else if ("android.intent.action.PHONE_STATE".equals(intent.getAction()) && HwAutoConnectManager.this.mPortalNotifyDelayed.get() && HwAutoConnectManager.this.mPowerManager.isScreenOn() && (WifiProCommonUtils.isCalling(HwAutoConnectManager.this.mContext) ^ 1) != 0) {
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 114), HwAutoConnectManager.DELAYED_TIME_SWITCH_WIFI);
                }
            }
        }, intentFilter);
    }

    private void showPortalStatusBar() {
        if (this.mWifiProUIDisplayManager != null && this.mPortalNotificationId == -1 && this.mPortalNotifyDelayWifiConfig != null && (TextUtils.isEmpty(this.mPortalNotifyDelayWifiConfig.SSID) ^ 1) != 0 && (this.mPortalNotifyDelayWifiConfig.SSID.equals("<unknown ssid>") ^ 1) != 0) {
            this.mPortalNotificationId = new SecureRandom().nextInt(100000);
            this.mPortalNotifiedSsid = this.mPortalNotifyDelayWifiConfig.SSID;
            this.mWifiProUIDisplayManager.showPortalNotificationStatusBar(this.mPortalNotifiedSsid, PORTAL_STATUS_BAR_TAG, this.mPortalNotificationId);
            WifiProCommonUtils.portalBackgroundStatusChanged(true);
            notifyPortalStatusChanged(true, this.mPortalNotifiedSsid);
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 111), PORTAL_BACKGROUND_CONN_LIVE_TIME);
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
                updateWifiConfig(this.mPortalNotifyDelayWifiConfig);
                this.mPortalNotifyDelayWifiConfig = null;
            } else {
                LOGD("resetPortalNotifyDelay show MSG_PORTAL_STATUS_BAR");
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 106), 500);
            }
        }
    }

    private void removeDelayedMessage(int reason) {
        if (reason == 103) {
            if (this.mHandler.hasMessages(101)) {
                LOGW("MSG_CHECK_PORTAL_NETWORK msg removed");
                this.mHandler.removeMessages(101);
            }
            if (this.mHandler.hasMessages(102)) {
                LOGW("MSG_DISCONNECT_NETWORK msg removed");
                this.mHandler.removeMessages(102);
            }
            if (this.mHandler.hasMessages(111)) {
                LOGW("MSG_BACKGROUND_CONN_LEAVE msg removed");
                this.mHandler.removeMessages(111);
            }
            if (this.mHandler.hasMessages(105)) {
                LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                this.mHandler.removeMessages(105);
            }
            if (this.mHandler.hasMessages(112)) {
                LOGW("MSG_LAUNCH_BROWSER msg removed");
                this.mHandler.removeMessages(105);
            }
            if (this.mHandler.hasMessages(106)) {
                LOGW("MSG_PORTAL_STATUS_BAR msg removed");
                this.mHandler.removeMessages(106);
            }
            if (this.mHandler.hasMessages(115)) {
                this.mHandler.removeMessages(115);
            }
        }
    }

    private void handleRespCodeForPortal(int counter, int respCode, boolean portalLastTime) {
        LOGD("handleRespCodeForPortal, current counter = " + counter + ", resp = " + respCode + ", recheck portal = " + portalLastTime);
        if (!WifiProCommonUtils.unreachableRespCode(respCode) || counter >= 2) {
            if (WifiProCommonUtils.isRedirectedRespCode(respCode) && (portalLastTime ^ 1) != 0) {
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
                    if (type == 101) {
                        respCode = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putInt(RECHECK_REASON_TYPE, 101);
                        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 113, 101, counter, bundle), 1000);
                        return;
                    }
                }
            } else if (WifiProCommonUtils.unreachableRespCode(respCode) && portalLastTime) {
                respCode = 302;
            }
            handleCheckingResult(respCode, 101);
            return;
        }
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 113, 101, counter), 1000);
    }

    private void handleCheckingResult(int respCode, int type) {
        LOGD("handleCheckingResult, respCode = " + respCode + ", type = " + type);
        WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config != null) {
            boolean openNoInternet = false;
            if (type == 101) {
                config.internetRecoveryCheckTimestamp = System.currentTimeMillis();
                if (respCode == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                    config.portalCheckStatus = 1;
                    config.noInternetAccess = false;
                    config.validatedInternetAccess = true;
                    config.wifiProNoInternetAccess = false;
                } else if (WifiProCommonUtils.isRedirectedRespCode(respCode)) {
                    config.portalCheckStatus = 2;
                    config.portalNetwork = true;
                    config.noInternetAccess = false;
                    config.wifiProNoInternetAccess = true;
                    config.wifiProNoInternetReason = 1;
                } else if (WifiProCommonUtils.unreachableRespCode(respCode)) {
                    if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103)) {
                        openNoInternet = true;
                        config.noInternetAccess = true;
                        config.validatedInternetAccess = false;
                        config.wifiProNoInternetAccess = true;
                        config.wifiProNoInternetReason = 0;
                        config.wifiProNoHandoverNetwork = false;
                        config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 0);
                    } else if (WifiProCommonUtils.isPortalAPHaveInternetLastTime(config)) {
                        LOGD("handleCheckingResult connect portal AP because it's history have internet");
                        config.portalCheckStatus = 1;
                        config.noInternetAccess = false;
                        config.validatedInternetAccess = true;
                        config.wifiProNoInternetAccess = false;
                    } else {
                        config.portalCheckStatus = 2;
                        config.portalNetwork = true;
                        config.noInternetAccess = false;
                        config.wifiProNoInternetAccess = true;
                        config.wifiProNoInternetReason = 1;
                    }
                }
            } else if (type == 104) {
                config.internetRecoveryCheckTimestamp = System.currentTimeMillis();
                config.internetRecoveryStatus = respCode == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC ? 5 : 4;
                if (config.internetRecoveryStatus == 5) {
                    config.noInternetAccess = false;
                    config.validatedInternetAccess = true;
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 1);
                }
            }
            updateWifiConfig(config);
            LOGD("handleCheckingResult, isScreenOn = " + this.mPowerManager.isScreenOn() + ", nid = " + this.mPortalNotificationId + ", status = " + config.portalCheckStatus + ", noInetOpen = " + openNoInternet);
            if (type == 101) {
                if (config.portalCheckStatus == 1) {
                    if (this.mHandler.hasMessages(105)) {
                        this.mHandler.removeMessages(105);
                    }
                    switchWifiForeground();
                } else if (this.mPortalNotificationId != -1 || openNoInternet) {
                    this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 102), 500);
                } else if (config.portalCheckStatus == 2 && this.mPowerManager.isScreenOn()) {
                    this.mPortalNotifyDelayWifiConfig = config;
                    this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 106), 500);
                } else if (config.portalCheckStatus == 2 && (this.mPowerManager.isScreenOn() ^ 1) != 0) {
                    this.mPortalNotifyDelayed.set(true);
                    this.mPortalNotifyDelayWifiConfig = config;
                    if (this.mHandler.hasMessages(105)) {
                        LOGW("MSG_MONITOR_ENTER_SETTINGS msg removed");
                        this.mHandler.removeMessages(105);
                    }
                }
            } else if (type == 104) {
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 102), 500);
            }
        }
    }

    private boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0 || !operator.startsWith(COUNTRY_CODE_CN)) {
            return true;
        }
        return false;
    }

    private void switchWifiForeground() {
        this.mWifiStateMachine.sendMessage(131873);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 115), DELAYED_TIME_WIFI_ICON);
    }

    private void notifyPortalStatusChanged(boolean popUp, String configKey) {
        if (this.mSavedNetworkEvaluator != null) {
            this.mSavedNetworkEvaluator.portalNotifyChanged(popUp, configKey);
        }
    }

    private void updateWifiConfig(WifiConfiguration config) {
        if (config != null) {
            Message msg = Message.obtain();
            msg.what = HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS;
            msg.obj = config;
            this.mWifiStateMachine.sendMessage(msg);
        }
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
                } catch (ActivityNotFoundException e3) {
                    Log.e(TAG, "startActivity failed, message", e3);
                }
            }
        } catch (MalformedURLException e2) {
        }
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }

    private void setUserSelectPortalFlag(boolean flag) {
        if (this.mSavedNetworkEvaluator != null) {
            this.mSavedNetworkEvaluator.setUserSelectPortalFlag(flag);
        }
    }
}
