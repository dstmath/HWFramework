package com.android.server.wifi.wifipro;

import android.app.KeyguardManager;
import android.app.Notification;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.CaptivePortal;
import android.net.ICaptivePortal;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.HwServiceFactory;
import com.android.server.Utils;
import com.android.server.wifi.HwPortalExceptionManager;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.LAA.HwLaaController;
import com.android.server.wifi.LAA.HwLaaUtils;
import com.android.server.wifi.SavedNetworkEvaluator;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwAutoConnectManager {
    private static final int AUTO_JOIN_DISABLED_NETWORK_THRESHOLD = 2;
    /* access modifiers changed from: private */
    public static final String[] BROWSERS_PRE_INSTALLED = {HwPortalExceptionManager.BROWSER_PACKET_NAME, "com.android.browser", "com.android.chrome"};
    private static final String COUNTRY_CODE_CN = "460";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    private static final long DELAYED_TIME_HTTP_RECHECK = 10000;
    private static final long DELAYED_TIME_LAUNCH_BROWSER = 500;
    private static final long DELAYED_TIME_RETRY_LAUNCH_BROWSER = 300;
    private static final long DELAYED_TIME_STATUS_BAR = 500;
    private static final long DELAYED_TIME_SWITCH_WIFI = 2000;
    private static final long DELAYED_TIME_WIFI_ICON = 200;
    public static final String KEY_HUAWEI_EMPLOYEE = "\"Huawei-Employee\"WPA_EAP";
    private static final int LAUNCH_BROWSER_MAX_RETRY_COUNT = 2;
    private static final int MAX_PORTAL_HTTP_TIMES = 3;
    private static final int MAX_START_BROWSER_TIME = 120;
    private static final int MSG_BLACKLIST_BSSID_TIMEOUT = 118;
    private static final int MSG_CHECK_PORTAL_AUTH_RESULT = 124;
    private static final int MSG_CHECK_PORTAL_NETWORK = 101;
    private static final int MSG_DISCONNECT_NETWORK = 102;
    private static final int MSG_ENABLE_SAME_NETWORK_ID = 115;
    private static final int MSG_HTTP_RECHECK = 112;
    private static final int MSG_LAUNCH_BROWSER = 111;
    private static final int MSG_NO_INTERNET_RECOVERY_CHECK = 104;
    private static final int MSG_PORTAL_BROWSER_LAUNCHED = 122;
    private static final int MSG_PORTAL_BROWSER_LAUNCHED_TIMEOUT = 121;
    private static final int MSG_PORTAL_CANCELED = 108;
    private static final int MSG_PORTAL_NETWORK_CONNECTED = 120;
    private static final int MSG_PORTAL_OUT_OF_RANGE = 109;
    private static final int MSG_PORTAL_SELECTED = 107;
    private static final int MSG_PORTAL_STATUS_BAR = 106;
    private static final int MSG_RECV_NETWORK_CONNECTED = 116;
    private static final int MSG_RECV_NETWORK_DISCONNECTED = 103;
    private static final int MSG_REQUEST_CHECK_PORTAL_AUTH = 123;
    private static final int MSG_RSSI_CHANGED_PORTAL_NETWORK = 114;
    private static final int MSG_SWITCH_WIFI_FOREGROUND = 113;
    private static final int MSG_USER_ENTER_WLAN_SETTINGS = 119;
    private static final int MSG_WIFI_CLOSED = 110;
    private static final int MSG_WIFI_DISABLED_RCVD = 117;
    private static final String PORTAL_STATUS_BAR_TAG = "wifipro_portal_status_bar";
    private static final int START_BROWSER_TIMEOUT = -100;
    private static final String TAG = "HwAutoConnectManager";
    private static HwAutoConnectManager mHwAutoConnectManager = null;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        public void call(Bundle extras) {
            if (extras == null) {
                HwAutoConnectManager.this.LOGD("extras == null");
                return;
            }
            ComponentName componentName = (ComponentName) extras.getParcelable("comp");
            int uid = extras.getInt("uid");
            if (!(!"onResume".equals(extras.getString("state")) || componentName == null || uid == -1)) {
                if ("com.android.settings.Settings$WifiSettingsActivity".equals(componentName.getClassName())) {
                    HwAutoConnectManager.this.mHandler.sendMessage(HwAutoConnectManager.this.mHandler.obtainMessage(119));
                    HwSelfCureEngine.getInstance().notifyUserEnterWlanSettings();
                }
                if (WifiProCommonUtils.isInMonitorList(componentName.getPackageName(), HwAutoConnectManager.BROWSERS_PRE_INSTALLED)) {
                    HwAutoConnectManager.this.mHandler.sendMessage(HwAutoConnectManager.this.mHandler.obtainMessage(122));
                }
                synchronized (HwAutoConnectManager.this.mCurrentTopUidLock) {
                    int unused = HwAutoConnectManager.this.mCurrentTopUid = uid;
                    String unused2 = HwAutoConnectManager.this.mCurrentPackageName = componentName.getPackageName();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Object mAutoConnectFilterLock = new Object();
    /* access modifiers changed from: private */
    public ArrayList<String> mAutoJoinBlacklistBssid = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mAutoJoinDisabledNetworkCnt = 0;
    /* access modifiers changed from: private */
    public AtomicBoolean mBackGroundRunning = new AtomicBoolean(false);
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public String mCurrentAutoJoinTargetBssid = null;
    /* access modifiers changed from: private */
    public String mCurrentBlacklistConfigKey = null;
    /* access modifiers changed from: private */
    public WifiConfiguration mCurrentCheckWifiConfig = null;
    /* access modifiers changed from: private */
    public String mCurrentPackageName = "";
    /* access modifiers changed from: private */
    public int mCurrentTopUid = -1;
    /* access modifiers changed from: private */
    public Object mCurrentTopUidLock = new Object();
    /* access modifiers changed from: private */
    public boolean mFirstDetected = false;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public HwNetworkPropertyChecker mHwNetworkPropertychecker = null;
    private IntentFilter mIntentFilter;
    private KeyguardManager mKeyguardManager;
    /* access modifiers changed from: private */
    public boolean mLaaDiabledRequest = false;
    /* access modifiers changed from: private */
    public int mLaunchBrowserRetryCount = 0;
    /* access modifiers changed from: private */
    public Object mNetworkCheckLock = new Object();
    /* access modifiers changed from: private */
    public long mNetworkConnectedTime = -1;
    /* access modifiers changed from: private */
    public WifiConfiguration mPopUpNotifyWifiConfig = null;
    /* access modifiers changed from: private */
    public int mPopUpWifiRssi = WifiHandover.INVALID_RSSI;
    private Notification.Builder mPortalBuilder = null;
    /* access modifiers changed from: private */
    public Object mPortalDatabaseLock = new Object();
    /* access modifiers changed from: private */
    public int mPortalNotificationId = -1;
    /* access modifiers changed from: private */
    public String mPortalRedirectedUrl = null;
    /* access modifiers changed from: private */
    public int mPortalRespCode = 599;
    /* access modifiers changed from: private */
    public Map<String, ArrayList<String>> mPortalUnauthDatabase = new HashMap();
    /* access modifiers changed from: private */
    public String mPortalUsedUrl = null;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private SavedNetworkEvaluator mSavedNetworkEvaluator;
    private WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    private WifiProUIDisplayManager mWifiProUIDisplayManager;
    /* access modifiers changed from: private */
    public WifiStateMachine mWifiStateMachine;

    private class NetworkCheckThread extends Thread {
        private int checkCounter;
        private int msg;

        public NetworkCheckThread(int msg2, int counter) {
            this.msg = msg2;
            this.checkCounter = counter;
        }

        public void run() {
            synchronized (HwAutoConnectManager.this.mNetworkCheckLock) {
                HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, this.msg, this.checkCounter, HwAutoConnectManager.this.mHwNetworkPropertychecker.isCaptivePortal(this.msg != 104, false, true)));
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
        if (networkQosMonitor != null) {
            this.mHwNetworkPropertychecker = networkQosMonitor.getNetworkPropertyChecker();
            this.mWifiProUIDisplayManager = networkQosMonitor.getWifiProUIDisplayManager();
        } else {
            this.mWifiProUIDisplayManager = WifiProUIDisplayManager.createInstance(context, null);
            HwNetworkPropertyChecker hwNetworkPropertyChecker = new HwNetworkPropertyChecker(this.mContext, null, null, true, null, false);
            this.mHwNetworkPropertychecker = hwNetworkPropertyChecker;
        }
        Log.d(TAG, "HwAutoConnectManager init Complete! ");
    }

    public static synchronized HwAutoConnectManager getInstance(Context context, NetworkQosMonitor networkQosMonitor) {
        HwAutoConnectManager hwAutoConnectManager;
        synchronized (HwAutoConnectManager.class) {
            if (mHwAutoConnectManager == null) {
                mHwAutoConnectManager = new HwAutoConnectManager(context, networkQosMonitor);
            }
            hwAutoConnectManager = mHwAutoConnectManager;
        }
        return hwAutoConnectManager;
    }

    public static synchronized HwAutoConnectManager getInstance() {
        HwAutoConnectManager hwAutoConnectManager;
        synchronized (HwAutoConnectManager.class) {
            hwAutoConnectManager = mHwAutoConnectManager;
        }
        return hwAutoConnectManager;
    }

    public void init(Looper looper) {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND);
        this.mIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND);
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER");
        this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER");
        this.mIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE);
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        if (looper == null) {
            HandlerThread handlerThread = new HandlerThread("wifipro_auto_conn_manager_handler_thread");
            handlerThread.start();
            looper = handlerThread.getLooper();
        }
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                Message message = msg;
                long j = 0;
                boolean z = true;
                String str = null;
                switch (message.what) {
                    case 101:
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            HwAutoConnectManager.this.handleRespCodeForPortalCheck(message.arg1, message.arg2);
                            break;
                        }
                        break;
                    case 102:
                        if (WifiProCommonUtils.isWifiConnected(HwAutoConnectManager.this.mWifiManager)) {
                            HwAutoConnectManager.this.LOGW("MSG_DISCONNECT_NETWORK msg handled");
                            HwAutoConnectManager.this.mWifiManager.disconnect();
                        }
                        HwAutoConnectManager.this.mBackGroundRunning.set(false);
                        WifiConfiguration unused = HwAutoConnectManager.this.mCurrentCheckWifiConfig = null;
                        String unused2 = HwAutoConnectManager.this.mPortalRedirectedUrl = null;
                        int unused3 = HwAutoConnectManager.this.mPortalRespCode = 599;
                        boolean unused4 = HwAutoConnectManager.this.mFirstDetected = false;
                        HwSelfCureEngine.getInstance().notifyWifiDisconnected();
                        HwAutoConnectManager.this.mHandler.removeMessages(112);
                        break;
                    case 103:
                        HwAutoConnectManager.this.LOGD("MSG_RECV_NETWORK_DISCONNECTED");
                        HwAutoConnectManager.this.mBackGroundRunning.set(false);
                        WifiConfiguration unused5 = HwAutoConnectManager.this.mCurrentCheckWifiConfig = null;
                        String unused6 = HwAutoConnectManager.this.mPortalRedirectedUrl = null;
                        int unused7 = HwAutoConnectManager.this.mPortalRespCode = 599;
                        boolean unused8 = HwAutoConnectManager.this.mFirstDetected = false;
                        long unused9 = HwAutoConnectManager.this.mNetworkConnectedTime = -1;
                        HwAutoConnectManager.this.removeDelayedMessage(103);
                        HwAutoConnectManager.this.mHandler.removeMessages(112);
                        HwAutoConnectManager.this.mHandler.removeMessages(121);
                        HwPortalExceptionManager.getInstance(HwAutoConnectManager.this.mContext).notifyNetworkDisconnected();
                        Settings.Secure.putInt(HwAutoConnectManager.this.mContext.getContentResolver(), "HW_WIFI_PORTAL_FLAG", 0);
                        if (HwAutoConnectManager.this.mLaaDiabledRequest) {
                            boolean unused10 = HwAutoConnectManager.this.mLaaDiabledRequest = false;
                            if (HwLaaUtils.isLaaPlusEnable() && HwLaaController.getInstrance() != null) {
                                HwLaaController.getInstrance().setLAAEnabled(true, 4);
                                break;
                            }
                        }
                        break;
                    case 104:
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            HwAutoConnectManager.this.handleRespCodeForNoInternetCheck(message.arg1, message.arg2);
                            break;
                        }
                        break;
                    case 106:
                        HwAutoConnectManager.this.LOGD("MSG_PORTAL_STATUS_BAR, update = " + message.obj);
                        HwAutoConnectManager.this.showPortalStatusBar(((Boolean) message.obj).booleanValue());
                        break;
                    case 107:
                        HwAutoConnectManager.this.LOGD("###MSG_PORTAL_SELECTED");
                        if (HwAutoConnectManager.this.mPopUpNotifyWifiConfig != null) {
                            if (!HwAutoConnectManager.this.mBackGroundRunning.get() || (HwAutoConnectManager.this.mCurrentCheckWifiConfig != null && (HwAutoConnectManager.this.mCurrentCheckWifiConfig.configKey() == null || !HwAutoConnectManager.this.mCurrentCheckWifiConfig.configKey().equals(HwAutoConnectManager.this.mPopUpNotifyWifiConfig.configKey())))) {
                                HwAutoConnectManager.this.LOGD("MSG_PORTAL_SELECTED, to connect the notification portal network.");
                                HwAutoConnectManager.this.mWifiManager.connect(HwAutoConnectManager.this.mPopUpNotifyWifiConfig, null);
                                Settings.Secure.putInt(HwAutoConnectManager.this.mContext.getContentResolver(), "HW_WIFI_PORTAL_FLAG", 2);
                            } else {
                                HwAutoConnectManager.this.LOGD("MSG_PORTAL_SELECTED, to switch the portal network foreground.");
                                HwAutoConnectManager.this.switchWifiForeground();
                                int unused11 = HwAutoConnectManager.this.mLaunchBrowserRetryCount = 0;
                                HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 111, HwAutoConnectManager.this.mPopUpNotifyWifiConfig), 500);
                            }
                            HwAutoConnectManager hwAutoConnectManager = HwAutoConnectManager.this;
                            String configKey = HwAutoConnectManager.this.mPopUpNotifyWifiConfig.configKey();
                            if (HwAutoConnectManager.this.mPopUpNotifyWifiConfig.lastHasInternetTimestamp <= 0) {
                                z = false;
                            }
                            hwAutoConnectManager.notifyPortalStatusChanged(false, configKey, z);
                            WifiProStatisticsManager.getInstance().increasePortalRefusedButUserTouchCnt();
                            int unused12 = HwAutoConnectManager.this.mPortalNotificationId = -1;
                            WifiConfiguration unused13 = HwAutoConnectManager.this.mPopUpNotifyWifiConfig = null;
                            int unused14 = HwAutoConnectManager.this.mPopUpWifiRssi = WifiHandover.INVALID_RSSI;
                            HwAutoConnectManager.this.mBackGroundRunning.set(false);
                            synchronized (HwAutoConnectManager.this.mPortalDatabaseLock) {
                                HwAutoConnectManager.this.mPortalUnauthDatabase.clear();
                            }
                            break;
                        }
                        break;
                    case 108:
                        if (HwAutoConnectManager.this.mPopUpNotifyWifiConfig != null) {
                            HwAutoConnectManager hwAutoConnectManager2 = HwAutoConnectManager.this;
                            String configKey2 = HwAutoConnectManager.this.mPopUpNotifyWifiConfig.configKey();
                            if (HwAutoConnectManager.this.mPopUpNotifyWifiConfig.lastHasInternetTimestamp <= 0) {
                                z = false;
                            }
                            hwAutoConnectManager2.notifyPortalStatusChanged(false, configKey2, z);
                            HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 102));
                            int unused15 = HwAutoConnectManager.this.mPortalNotificationId = -1;
                            WifiConfiguration unused16 = HwAutoConnectManager.this.mPopUpNotifyWifiConfig = null;
                            int unused17 = HwAutoConnectManager.this.mPopUpWifiRssi = WifiHandover.INVALID_RSSI;
                            break;
                        }
                        break;
                    case 109:
                        HwAutoConnectManager.this.handlePortalOutOfRange();
                        break;
                    case 111:
                        if (message.obj != null && (message.obj instanceof WifiConfiguration)) {
                            if (!WifiProCommonUtils.isWifiConnectedActive(HwAutoConnectManager.this.mContext)) {
                                HwAutoConnectManager.this.LOGD("Delay launchBrowserForPortalLogin");
                                if (HwAutoConnectManager.this.mLaunchBrowserRetryCount >= 2) {
                                    HwAutoConnectManager.this.LOGD("LaunchBrowserFail");
                                    HwAutoConnectManager.this.mWifiManager.connect((WifiConfiguration) message.obj, null);
                                    break;
                                } else {
                                    int unused18 = HwAutoConnectManager.this.mLaunchBrowserRetryCount = HwAutoConnectManager.this.mLaunchBrowserRetryCount + 1;
                                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 111, message.obj), HwAutoConnectManager.DELAYED_TIME_RETRY_LAUNCH_BROWSER);
                                    break;
                                }
                            } else {
                                HwAutoConnectManager.this.launchBrowserForPortalLogin(HwAutoConnectManager.this.mPortalUsedUrl, ((WifiConfiguration) message.obj).configKey());
                                break;
                            }
                        }
                    case 112:
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            if (message.arg2 == 1) {
                                WifiConfiguration unused19 = HwAutoConnectManager.this.mCurrentCheckWifiConfig = WifiProCommonUtils.getCurrentWifiConfig(HwAutoConnectManager.this.mWifiManager);
                                HwAutoConnectManager hwAutoConnectManager3 = HwAutoConnectManager.this;
                                StringBuilder sb = new StringBuilder();
                                sb.append("MSG_HTTP_RECHECK, current request network = ");
                                if (HwAutoConnectManager.this.mCurrentCheckWifiConfig != null) {
                                    str = HwAutoConnectManager.this.mCurrentCheckWifiConfig.configKey();
                                }
                                sb.append(str);
                                hwAutoConnectManager3.LOGD(sb.toString());
                            }
                            new NetworkCheckThread(message.arg1, message.arg2).start();
                            break;
                        }
                        break;
                    case 113:
                        HwAutoConnectManager.this.mWifiStateMachine.sendMessage(131874, 2);
                        break;
                    case 114:
                        int maxRssi = message.arg1;
                        String configKey3 = (String) message.obj;
                        if (!(configKey3 == null || HwAutoConnectManager.this.mPopUpNotifyWifiConfig == null || !configKey3.equals(HwAutoConnectManager.this.mPopUpNotifyWifiConfig.configKey()))) {
                            int unused20 = HwAutoConnectManager.this.mPopUpWifiRssi = maxRssi;
                            HwAutoConnectManager.this.updateUnauthPortalDatabase(configKey3, maxRssi);
                            break;
                        }
                    case 115:
                        HwAutoConnectManager.this.LOGD("###MSG_ENABLE_SAME_NETWORK_ID, nid = " + message.arg1);
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            HwAutoConnectManager.this.switchWifiForeground();
                        }
                        HwAutoConnectManager.this.mBackGroundRunning.set(false);
                        break;
                    case 116:
                        HwAutoConnectManager.this.LOGD("##MSG_RECV_NETWORK_CONNECTED");
                        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(HwAutoConnectManager.this.mWifiManager);
                        synchronized (HwAutoConnectManager.this.mAutoConnectFilterLock) {
                            if (current != null) {
                                try {
                                    if (current.configKey() != null && current.configKey().equals(HwAutoConnectManager.this.mCurrentBlacklistConfigKey) && !current.getNetworkSelectionStatus().isNetworkEnabled()) {
                                        HwAutoConnectManager.this.LOGD("##enableNetwork currentBlacklistConfigKey networkId = " + current.networkId);
                                        HwAutoConnectManager.this.mWifiManager.enableNetwork(current.networkId, false);
                                    }
                                } catch (Throwable th) {
                                    while (true) {
                                        throw th;
                                        break;
                                    }
                                }
                            }
                            String unused21 = HwAutoConnectManager.this.mCurrentAutoJoinTargetBssid = null;
                            String unused22 = HwAutoConnectManager.this.mCurrentBlacklistConfigKey = null;
                            HwAutoConnectManager.this.mAutoJoinBlacklistBssid.clear();
                            int unused23 = HwAutoConnectManager.this.mAutoJoinDisabledNetworkCnt = 0;
                        }
                        HwAutoConnectManager.this.mHandler.removeMessages(118);
                        long unused24 = HwAutoConnectManager.this.mNetworkConnectedTime = System.currentTimeMillis();
                        HwAutoConnectManager.this.handlePortalOutOfRange();
                        break;
                    case 117:
                        synchronized (HwAutoConnectManager.this.mAutoConnectFilterLock) {
                            String unused25 = HwAutoConnectManager.this.mCurrentBlacklistConfigKey = null;
                            String unused26 = HwAutoConnectManager.this.mCurrentAutoJoinTargetBssid = null;
                            int unused27 = HwAutoConnectManager.this.mAutoJoinDisabledNetworkCnt = 0;
                            HwAutoConnectManager.this.mAutoJoinBlacklistBssid.clear();
                        }
                        HwAutoConnectManager.this.mHandler.removeMessages(118);
                        HwAutoConnectManager.this.handlePortalOutOfRange();
                        break;
                    case 118:
                        HwAutoConnectManager.this.LOGD("###MSG_BLACKLIST_BSSID_TIMEOUT");
                        synchronized (HwAutoConnectManager.this.mAutoConnectFilterLock) {
                            String unused28 = HwAutoConnectManager.this.mCurrentBlacklistConfigKey = null;
                            HwAutoConnectManager.this.mAutoJoinBlacklistBssid.clear();
                            int unused29 = HwAutoConnectManager.this.mAutoJoinDisabledNetworkCnt = 0;
                        }
                        break;
                    case 119:
                        HwAutoConnectManager.this.LOGD("###MSG_USER_ENTER_WLAN_SETTINGS");
                        if (HwAutoConnectManager.this.mBackGroundRunning.get()) {
                            HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 102));
                        }
                        HwAutoConnectManager.this.mHandler.removeMessages(118);
                        synchronized (HwAutoConnectManager.this.mAutoConnectFilterLock) {
                            String unused30 = HwAutoConnectManager.this.mCurrentBlacklistConfigKey = null;
                            HwAutoConnectManager.this.mAutoJoinBlacklistBssid.clear();
                            int unused31 = HwAutoConnectManager.this.mAutoJoinDisabledNetworkCnt = 0;
                        }
                        break;
                    case 120:
                        HwAutoConnectManager.this.LOGD("###MSG_PORTAL_NETWORK_CONNECTED");
                        if (WifiProCommonUtils.isInMonitorList(WifiProCommonUtils.getPackageName(HwAutoConnectManager.this.mContext, WifiProCommonUtils.getForegroundAppUid(HwAutoConnectManager.this.mContext)), HwAutoConnectManager.BROWSERS_PRE_INSTALLED)) {
                            HwAutoConnectManager.this.LOGD("###BROWSERS_PRE_INSTALLED launched!!!");
                            break;
                        } else {
                            HwAutoConnectManager.this.mHandler.removeMessages(121);
                            HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 121), 5000);
                            break;
                        }
                    case 121:
                        HwAutoConnectManager.this.LOGD("###MSG_PORTAL_BROWSER_LAUNCHED_TIMEOUT");
                        HwAutoConnectManager.this.handleBrowserLaunchedTimeout(-100);
                        break;
                    case 122:
                        if (HwAutoConnectManager.this.mHandler.hasMessages(121)) {
                            HwAutoConnectManager.this.LOGD("###MSG_PORTAL_BROWSER_LAUNCHED");
                            HwAutoConnectManager.this.mHandler.removeMessages(121);
                            if (HwAutoConnectManager.this.mNetworkConnectedTime > 0) {
                                j = (System.currentTimeMillis() - HwAutoConnectManager.this.mNetworkConnectedTime) / 1000;
                            }
                            int deltaTimeSec = (int) j;
                            HwAutoConnectManager hwAutoConnectManager4 = HwAutoConnectManager.this;
                            int i = 120;
                            if (deltaTimeSec <= 120) {
                                i = deltaTimeSec;
                            }
                            hwAutoConnectManager4.handleBrowserLaunchedTimeout(i);
                            break;
                        }
                        break;
                    case 123:
                        new NetworkCheckThread(124, 1).start();
                        break;
                    case 124:
                        if (WifiProStateMachine.getWifiProStateMachineImpl() != null) {
                            WifiProStateMachine.getWifiProStateMachineImpl().onPortalAuthCheckResult(message.arg2);
                            break;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
        registerUserBroadcastReceiver();
    }

    private void registerUserBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND received.");
                    HwAutoConnectManager.this.mBackGroundRunning.set(true);
                    HwAutoConnectManager.this.mHandler.removeMessages(107);
                    if (!HwAutoConnectManager.this.mLaaDiabledRequest && WifiProCommonUtils.isWifi5GConnected(HwAutoConnectManager.this.mWifiManager)) {
                        boolean unused = HwAutoConnectManager.this.mLaaDiabledRequest = true;
                        if (HwLaaUtils.isLaaPlusEnable() && HwLaaController.getInstrance() != null) {
                            HwLaaController.getInstrance().setLAAEnabled(false, 4);
                        }
                    }
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 112, 101, 1));
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 103));
                    } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 116));
                    }
                } else if (WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND.equals(intent.getAction())) {
                    HwAutoConnectManager.this.LOGD("ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND received.");
                    HwAutoConnectManager.this.mBackGroundRunning.set(true);
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 112, 104, 1));
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER".equals(intent.getAction())) {
                    HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 107), HwAutoConnectManager.DELAYED_TIME_SWITCH_WIFI);
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER".equals(intent.getAction())) {
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 108));
                } else if (WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE.equals(intent.getAction())) {
                    HwAutoConnectManager.this.mHandler.sendMessage(Message.obtain(HwAutoConnectManager.this.mHandler, 109));
                } else if (!"android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                } else {
                    if (!HwAutoConnectManager.this.mWifiManager.isWifiEnabled() && HwAutoConnectManager.this.mPowerManager.isScreenOn()) {
                        HwAutoConnectManager.this.mHandler.sendMessageDelayed(Message.obtain(HwAutoConnectManager.this.mHandler, 117), HwAutoConnectManager.DELAYED_TIME_WIFI_ICON);
                    } else if (HwAutoConnectManager.this.mWifiManager.isWifiEnabled() && HwAutoConnectManager.this.mPowerManager.isScreenOn()) {
                        HwAutoConnectManager.this.mHandler.removeMessages(117);
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WifiproUtils.PERMISSION_RECV_WIFI_CONNECTED_CONCURRENTLY, null);
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0077, code lost:
        r0 = com.android.server.wifi.HwWifiServiceFactory.getHwWifiCHRService();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007b, code lost:
        if (r0 == null) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007d, code lost:
        r1 = new android.os.Bundle();
        r1.putInt("Server", r5);
        r0.uploadDFTEvent(909002061, r1);
        LOGD("###handleBrowserLaunchedTimeout");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0092, code lost:
        return;
     */
    public void handleBrowserLaunchedTimeout(int deltaTimeSec) {
        LOGD("###handleBrowserLaunchedTimeout, deltaTimeSec = " + deltaTimeSec);
        if (1 != Settings.Global.getInt(this.mContext.getContentResolver(), "hw_disable_portal", 0)) {
            if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) && "CMCC".equals(WifiProCommonUtils.getCurrentSsid(this.mWifiManager))) {
                return;
            }
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0 || !"true".equals(SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
                synchronized (this.mCurrentTopUidLock) {
                    if ("com.huawei.hiskytone".equals(this.mCurrentPackageName)) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePortalOutOfRange() {
        cancelPortalNotifyStatusBar();
        this.mHandler.removeMessages(112);
        this.mBackGroundRunning.set(false);
        this.mPortalNotificationId = -1;
        this.mPopUpNotifyWifiConfig = null;
        this.mCurrentCheckWifiConfig = null;
        synchronized (this.mPortalDatabaseLock) {
            this.mPortalUnauthDatabase.clear();
        }
    }

    /* access modifiers changed from: private */
    public void showPortalStatusBar(boolean updated) {
        if (this.mPopUpNotifyWifiConfig != null && !TextUtils.isEmpty(this.mPopUpNotifyWifiConfig.SSID) && !this.mPopUpNotifyWifiConfig.SSID.equals("<unknown ssid>")) {
            LOGD("showPortalStatusBar, portal network = " + this.mPopUpNotifyWifiConfig.configKey());
            boolean z = false;
            if (!updated && this.mPortalNotificationId == -1) {
                this.mPortalNotificationId = new SecureRandom().nextInt(100000);
                this.mPortalBuilder = this.mWifiProUIDisplayManager.showPortalNotificationStatusBar(this.mPopUpNotifyWifiConfig.SSID, PORTAL_STATUS_BAR_TAG, this.mPortalNotificationId, null);
                String configKey = this.mPopUpNotifyWifiConfig.configKey();
                if (this.mPopUpNotifyWifiConfig.lastHasInternetTimestamp > 0) {
                    z = true;
                }
                notifyPortalStatusChanged(true, configKey, z);
                WifiProStatisticsManager.getInstance().increasePortalNoAutoConnCnt();
            } else if (updated && this.mPortalNotificationId != -1) {
                this.mPortalBuilder = this.mWifiProUIDisplayManager.showPortalNotificationStatusBar(this.mPopUpNotifyWifiConfig.SSID, PORTAL_STATUS_BAR_TAG, this.mPortalNotificationId, this.mPortalBuilder);
                String configKey2 = this.mPopUpNotifyWifiConfig.configKey();
                if (this.mPopUpNotifyWifiConfig.lastHasInternetTimestamp > 0) {
                    z = true;
                }
                notifyPortalStatusChanged(true, configKey2, z);
            }
        }
    }

    private void cancelPortalNotifyStatusBar() {
        if (this.mPortalNotificationId != -1 && this.mPopUpNotifyWifiConfig != null) {
            LOGD("cancelPortalNotifyStatusBar, nid = " + this.mPortalNotificationId + ", ssid = " + this.mPopUpNotifyWifiConfig.configKey());
            this.mWifiProUIDisplayManager.cancelPortalNotificationStatusBar(PORTAL_STATUS_BAR_TAG, this.mPortalNotificationId);
            notifyPortalStatusChanged(false, this.mPopUpNotifyWifiConfig.configKey(), this.mPopUpNotifyWifiConfig.lastHasInternetTimestamp > 0);
        }
    }

    /* access modifiers changed from: private */
    public void removeDelayedMessage(int reason) {
        if (reason == 103) {
            if (this.mHandler.hasMessages(101)) {
                LOGW("MSG_CHECK_PORTAL_NETWORK msg removed");
                this.mHandler.removeMessages(101);
            }
            if (this.mHandler.hasMessages(111)) {
                LOGW("MSG_LAUNCH_BROWSER msg removed");
                this.mHandler.removeMessages(111);
            }
            if (this.mHandler.hasMessages(106)) {
                LOGW("MSG_PORTAL_STATUS_BAR msg removed");
                this.mHandler.removeMessages(106);
            }
            if (this.mHandler.hasMessages(113)) {
                this.mHandler.removeMessages(113);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleRespCodeForPortalCheck(int counter, int respCode) {
        LOGD("handleRespCodeForPortalCheck, counter = " + counter + ", respCode = " + respCode);
        if (respCode == 204) {
            if (this.mCurrentCheckWifiConfig != null) {
                this.mCurrentCheckWifiConfig.portalCheckStatus = 1;
                this.mCurrentCheckWifiConfig.noInternetAccess = false;
                this.mCurrentCheckWifiConfig.validatedInternetAccess = true;
                this.mCurrentCheckWifiConfig.wifiProNoInternetAccess = false;
                this.mCurrentCheckWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                updateWifiConfig(this.mCurrentCheckWifiConfig);
                synchronized (this.mPortalDatabaseLock) {
                    this.mPortalUnauthDatabase.clear();
                }
                this.mCurrentCheckWifiConfig = null;
                switchWifiForeground();
            }
        } else if (WifiProCommonUtils.isRedirectedRespCode(respCode)) {
            boolean empty = false;
            if (this.mCurrentCheckWifiConfig != null) {
                empty = WifiProCommonUtils.matchedRequestByHistory(this.mCurrentCheckWifiConfig.internetHistory, 103);
                if (empty) {
                    this.mCurrentCheckWifiConfig.portalCheckStatus = 2;
                    this.mCurrentCheckWifiConfig.portalNetwork = true;
                    this.mCurrentCheckWifiConfig.noInternetAccess = false;
                    this.mCurrentCheckWifiConfig.wifiProNoInternetAccess = true;
                    this.mCurrentCheckWifiConfig.wifiProNoInternetReason = 1;
                    this.mCurrentCheckWifiConfig.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(this.mCurrentCheckWifiConfig.internetHistory, 2);
                    updateWifiConfig(this.mCurrentCheckWifiConfig);
                }
            }
            if (this.mHwNetworkPropertychecker != null) {
                this.mPortalUsedUrl = this.mHwNetworkPropertychecker.getCaptiveUsedServer();
                this.mPortalRedirectedUrl = this.mHwNetworkPropertychecker.getPortalRedirectedUrl();
                this.mPortalRespCode = this.mHwNetworkPropertychecker.getRawHttpRespCode();
                this.mFirstDetected = empty;
            }
            if (this.mPopUpNotifyWifiConfig == null && this.mCurrentCheckWifiConfig != null) {
                this.mPopUpNotifyWifiConfig = this.mCurrentCheckWifiConfig;
                this.mPopUpWifiRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
                this.mHandler.sendMessage(Message.obtain(this.mHandler, 106, false));
            }
            if (counter < 3) {
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 112, 101, counter + 1), DELAYED_TIME_HTTP_RECHECK);
            } else {
                if (this.mCurrentCheckWifiConfig != null) {
                    saveCurrentUnauthPortalBssid();
                    if (!(this.mPopUpNotifyWifiConfig == null || this.mPopUpNotifyWifiConfig.configKey() == null || this.mPopUpNotifyWifiConfig.configKey().equals(this.mCurrentCheckWifiConfig.configKey()))) {
                        int currCheckNetworkRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
                        if ((this.mPopUpNotifyWifiConfig.lastHasInternetTimestamp == 0 && currCheckNetworkRssi > -80) || (currCheckNetworkRssi >= -75 && currCheckNetworkRssi - this.mPopUpWifiRssi >= 8)) {
                            this.mPopUpNotifyWifiConfig = this.mCurrentCheckWifiConfig;
                            this.mPopUpWifiRssi = currCheckNetworkRssi;
                            this.mHandler.sendMessage(Message.obtain(this.mHandler, 106, true));
                        }
                    }
                }
                this.mHandler.sendMessage(Message.obtain(this.mHandler, 102));
            }
        } else if (WifiProCommonUtils.unreachableRespCode(respCode)) {
            if (counter < 3) {
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 112, 101, counter + 1), DELAYED_TIME_HTTP_RECHECK);
            } else if (this.mCurrentCheckWifiConfig != null) {
                if (WifiProCommonUtils.matchedRequestByHistory(this.mCurrentCheckWifiConfig.internetHistory, 103)) {
                    this.mCurrentCheckWifiConfig.noInternetAccess = true;
                    this.mCurrentCheckWifiConfig.validatedInternetAccess = false;
                    this.mCurrentCheckWifiConfig.wifiProNoInternetAccess = true;
                    this.mCurrentCheckWifiConfig.wifiProNoInternetReason = 0;
                    this.mCurrentCheckWifiConfig.wifiProNoHandoverNetwork = false;
                    this.mCurrentCheckWifiConfig.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(this.mCurrentCheckWifiConfig.internetHistory, 0);
                    updateWifiConfig(this.mCurrentCheckWifiConfig);
                } else {
                    saveCurrentUnauthPortalBssid();
                    if (this.mPopUpNotifyWifiConfig == null) {
                        this.mPopUpNotifyWifiConfig = this.mCurrentCheckWifiConfig;
                        this.mPopUpWifiRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
                        this.mHandler.sendMessage(Message.obtain(this.mHandler, 106, false));
                    }
                }
                this.mHandler.sendMessage(Message.obtain(this.mHandler, 102));
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleRespCodeForNoInternetCheck(int counter, int respCode) {
        LOGD("handleRespCodeForNoInternetCheck, counter = " + counter + ", resp = " + respCode);
        if (this.mCurrentCheckWifiConfig != null) {
            this.mCurrentCheckWifiConfig.internetRecoveryCheckTimestamp = System.currentTimeMillis();
            this.mCurrentCheckWifiConfig.internetRecoveryStatus = respCode == 204 ? 5 : 4;
            if (this.mCurrentCheckWifiConfig.internetRecoveryStatus == 5) {
                this.mCurrentCheckWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                this.mCurrentCheckWifiConfig.noInternetAccess = false;
                this.mCurrentCheckWifiConfig.validatedInternetAccess = true;
                this.mCurrentCheckWifiConfig.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(this.mCurrentCheckWifiConfig.internetHistory, 1);
            }
            updateWifiConfig(this.mCurrentCheckWifiConfig);
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 102));
        }
    }

    private void saveCurrentUnauthPortalBssid() {
        synchronized (this.mPortalDatabaseLock) {
            if (!this.mPortalUnauthDatabase.containsKey(this.mCurrentCheckWifiConfig.configKey())) {
                ArrayList<String> unauthBssidsList = new ArrayList<>();
                unauthBssidsList.add(WifiProCommonUtils.getCurrentBssid(this.mWifiManager));
                this.mPortalUnauthDatabase.put(this.mCurrentCheckWifiConfig.configKey(), unauthBssidsList);
            } else {
                this.mPortalUnauthDatabase.get(this.mCurrentCheckWifiConfig.configKey()).add(WifiProCommonUtils.getCurrentBssid(this.mWifiManager));
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateUnauthPortalDatabase(String configKey, int maxRssi) {
        if (maxRssi <= -80) {
            synchronized (this.mPortalDatabaseLock) {
                ArrayList<String> releasedConfigKeys = new ArrayList<>();
                for (Map.Entry<String, ArrayList<String>> entry : this.mPortalUnauthDatabase.entrySet()) {
                    String currKey = (String) entry.getKey();
                    if (!(currKey == null || configKey == null || currKey.equals(configKey))) {
                        releasedConfigKeys.add(currKey);
                    }
                }
                for (int i = 0; i < releasedConfigKeys.size(); i++) {
                    String key = releasedConfigKeys.get(i);
                    LOGD("updateUnauthPortalDatabase, key = " + key);
                    this.mPortalUnauthDatabase.remove(key);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void switchWifiForeground() {
        this.mWifiStateMachine.sendMessage(131873);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 113), DELAYED_TIME_WIFI_ICON);
    }

    /* access modifiers changed from: private */
    public void notifyPortalStatusChanged(boolean popUp, String configKey, boolean hasInternetAccess) {
        if (this.mSavedNetworkEvaluator != null) {
            this.mSavedNetworkEvaluator.portalNotifyChanged(popUp, configKey, hasInternetAccess);
        }
    }

    public void notifyNetworkDisconnected() {
        if (this.mBackGroundRunning.get()) {
            HwSelfCureEngine.getInstance().notifyWifiDisconnected();
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 103));
        }
    }

    public void notifyEnableSameNetworkId(int netId) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 115, netId, 0));
    }

    public void updatePopUpNetworkRssi(String configKey, int maxRssi) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 114, maxRssi, 0, configKey));
    }

    public void notifyPortalNetworkConnected() {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 120));
    }

    public boolean allowCheckPortalNetwork(String configKey, String bssid) {
        synchronized (this.mPortalDatabaseLock) {
            for (Map.Entry<String, ArrayList<String>> entry : this.mPortalUnauthDatabase.entrySet()) {
                String currKey = (String) entry.getKey();
                if (!(currKey == null || configKey == null || !currKey.equals(configKey))) {
                    ArrayList<String> unauthBssids = this.mPortalUnauthDatabase.get(currKey);
                    if (!(unauthBssids == null || bssid == null)) {
                        if (unauthBssids.size() >= 3) {
                            return false;
                        }
                        for (int i = 0; i < unauthBssids.size(); i++) {
                            if (bssid.equals(unauthBssids.get(i))) {
                                return false;
                            }
                        }
                        continue;
                    }
                }
            }
            return true;
        }
    }

    public boolean isPortalNotifyOn() {
        return this.mPortalNotificationId != -1;
    }

    private void updateWifiConfig(WifiConfiguration config) {
        if (config != null) {
            Message msg = Message.obtain();
            msg.what = HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS;
            msg.obj = config;
            this.mWifiStateMachine.sendMessage(msg);
        }
    }

    private boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0) {
            if ("CN".equalsIgnoreCase(WifiProCommonUtils.getProductLocale())) {
                return false;
            }
        } else if (operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        }
        return true;
    }

    public void saveAutoJoinTargetBssid(WifiConfiguration config, String targetBssid) {
        synchronized (this.mAutoConnectFilterLock) {
            this.mCurrentAutoJoinTargetBssid = targetBssid;
            if (config != null && !config.getNetworkSelectionStatus().isNetworkEnabled()) {
                this.mAutoJoinDisabledNetworkCnt++;
            }
            LOGD("saveAutoJoinTargetBssid, autoJoinDisabedNetworkCnt = " + this.mAutoJoinDisabledNetworkCnt);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0020 A[Catch:{ all -> 0x001b }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0049 A[Catch:{ all -> 0x001b }] */
    public boolean isAutoJoinAllowedSetTargetBssid(WifiConfiguration config, String targetBssid) {
        boolean matchedBlacklistSsid;
        synchronized (this.mAutoConnectFilterLock) {
            if (config != null) {
                try {
                    if (config.configKey() != null && config.configKey().equals(this.mCurrentBlacklistConfigKey)) {
                        matchedBlacklistSsid = true;
                        if (!matchedBlacklistSsid) {
                            this.mCurrentAutoJoinTargetBssid = targetBssid;
                            if (!config.getNetworkSelectionStatus().isNetworkEnabled()) {
                                this.mAutoJoinDisabledNetworkCnt++;
                            }
                            LOGD("isAutoJoinAllowedSetTargetBssid, autoJoinDisabedNetworkCnt = " + this.mAutoJoinDisabledNetworkCnt);
                            return true;
                        }
                        this.mCurrentAutoJoinTargetBssid = targetBssid;
                        return config != null && config.configKey() != null && config.configKey().equals(KEY_HUAWEI_EMPLOYEE);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            matchedBlacklistSsid = false;
            if (!matchedBlacklistSsid) {
            }
        }
    }

    public boolean isBssidMatchedBlacklist(String bssid) {
        synchronized (this.mAutoConnectFilterLock) {
            if (WifiProCommonUtils.isInMonitorList(bssid, (String[]) this.mAutoJoinBlacklistBssid.toArray(new String[0]))) {
                return true;
            }
            return false;
        }
    }

    public void releaseBlackListBssid(WifiConfiguration config, boolean autoJoin) {
        if (!autoJoin && config != null && config.configKey() != null) {
            synchronized (this.mAutoConnectFilterLock) {
                if (config.configKey().equals(this.mCurrentBlacklistConfigKey)) {
                    this.mHandler.removeMessages(118);
                    this.mCurrentBlacklistConfigKey = null;
                    this.mAutoJoinBlacklistBssid.clear();
                    this.mCurrentAutoJoinTargetBssid = null;
                    this.mAutoJoinDisabledNetworkCnt = 0;
                }
            }
        }
    }

    public void notifyWifiConnFailedInfo(WifiConfiguration config, String bssid, int rssi, int reason, WifiConnectivityManager wcm) {
        if (config != null && config.configKey() != null) {
            LOGD("notifyWifiConnFailedInfo, rssi = " + rssi + ", reason = " + reason);
            synchronized (this.mAutoConnectFilterLock) {
                if (reason == 3 || reason == 2) {
                    if (this.mCurrentBlacklistConfigKey != null && !this.mCurrentBlacklistConfigKey.equals(config.configKey())) {
                        this.mAutoJoinDisabledNetworkCnt = 0;
                        this.mAutoJoinBlacklistBssid.clear();
                    }
                    this.mCurrentBlacklistConfigKey = config.configKey();
                    String currBssid = bssid != null ? bssid : this.mCurrentAutoJoinTargetBssid;
                    if (currBssid != null && !WifiProCommonUtils.isInMonitorList(currBssid, (String[]) this.mAutoJoinBlacklistBssid.toArray(new String[0]))) {
                        this.mAutoJoinBlacklistBssid.add(currBssid);
                    }
                    this.mHandler.removeMessages(118);
                    this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 118), 240000);
                    WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                    LOGD("notifyWifiConnFailedInfo, isNetworkEnabled = " + status.isNetworkEnabled() + ", cnt = " + this.mAutoJoinDisabledNetworkCnt);
                    if (!status.isNetworkEnabled() && this.mAutoJoinDisabledNetworkCnt <= 2 && wcm != null) {
                        LOGD("notifyWifiConnFailedInfo, start scan immediately for auto join other bssid again!!!");
                        wcm.handleConnectionStateChanged(2);
                    }
                }
            }
        }
    }

    public boolean allowAutoJoinDisabledNetworkAgain(WifiConfiguration config) {
        boolean z = false;
        if (config == null || config.configKey() == null) {
            return false;
        }
        synchronized (this.mAutoConnectFilterLock) {
            if (config.configKey().equals(this.mCurrentBlacklistConfigKey) && this.mAutoJoinDisabledNetworkCnt < 2) {
                z = true;
            }
        }
        return z;
    }

    /* JADX WARNING: type inference failed for: r9v0, types: [com.android.server.wifi.wifipro.HwAutoConnectManager$3, android.os.IBinder] */
    public void launchBrowserForPortalLogin(String portalUsedUrl, String configKey) {
        try {
            URL url = new URL("http://connectivitycheck.platform.hicloud.com/generate_204");
            if (!TextUtils.isEmpty(portalUsedUrl) && portalUsedUrl.startsWith("http")) {
                Log.d(TAG, "launchBrowserForPortalLogin: use the portal url from the settings");
                url = new URL(portalUsedUrl);
            } else if (useOperatorOverSea()) {
                url = new URL("http://connectivitycheck.gstatic.com/generate_204");
            }
            String packageName = "com.android.browser";
            String className = "com.android.browser.BrowserActivity";
            if (Utils.isPackageInstalled(HwPortalExceptionManager.BROWSER_PACKET_NAME, this.mContext)) {
                packageName = HwPortalExceptionManager.BROWSER_PACKET_NAME;
                className = "com.huawei.browser.Main";
            }
            Settings.Secure.putInt(this.mContext.getContentResolver(), "HW_WIFI_PORTAL_FLAG", 1);
            HwPortalExceptionManager.getInstance(this.mContext).notifyPortalConnectedInfo(configKey, this.mFirstDetected, this.mPortalRespCode, this.mPortalRedirectedUrl);
            if (SystemProperties.getBoolean("runtime.hwwifi.portal_webview_support", false)) {
                Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
                if (network == null) {
                    Log.e(TAG, "Webview network null");
                    return;
                }
                String captivePortalUserAgent = getCaptivePortalUserAgent(this.mContext);
                Intent intentPortal = new Intent("android.net.conn.CAPTIVE_PORTAL");
                intentPortal.putExtra("android.net.extra.NETWORK", network);
                intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
                    public void appResponse(int response) {
                    }
                }));
                intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", url.toString());
                intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_USER_AGENT", captivePortalUserAgent);
                intentPortal.setFlags(272629760);
                this.mContext.startActivity(intentPortal);
                Log.d(TAG, "startCaptivePortalWebView");
            } else {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url.toString()));
                intent.setFlags(272629760);
                intent.putExtra("launch_from", "wifi_portal");
                try {
                    if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
                        intent.setClassName(packageName, className);
                    }
                    this.mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    try {
                        Intent intentOthers = new Intent("android.intent.action.VIEW", Uri.parse(url.toString()));
                        intentOthers.setFlags(272629760);
                        this.mContext.startActivity(intentOthers);
                    } catch (ActivityNotFoundException e3) {
                        Log.e(TAG, "startActivity failed, message", e3);
                    }
                }
            }
        } catch (MalformedURLException e2) {
            Log.e(TAG, "launchBrowserForPortalLogin, MalformedURLException!");
        }
    }

    private static String getCaptivePortalUserAgent(Context context) {
        String value = Settings.Global.getString(context.getContentResolver(), "captive_portal_user_agent");
        return value != null ? value : DEFAULT_USER_AGENT;
    }

    public void checkPortalAuthExpiration() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(123));
    }

    public int getCurrentTopUid() {
        int i;
        synchronized (this.mCurrentTopUidLock) {
            i = this.mCurrentTopUid;
        }
        return i;
    }

    public String getCurrentPackageName() {
        String str;
        synchronized (this.mCurrentTopUidLock) {
            str = this.mCurrentPackageName;
        }
        return str;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
