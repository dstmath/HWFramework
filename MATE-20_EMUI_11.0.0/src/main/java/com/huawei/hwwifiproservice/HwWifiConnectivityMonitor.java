package com.huawei.hwwifiproservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwWifiConnectivityMonitor extends StateMachine {
    private static final int BAD_AVE_RTT = 800;
    private static final int BAD_DNS_FAIL_COUNT = 3;
    private static final int BAD_ENTERPRISE_AP_LIMIT_COUNT = 4;
    private static final int BAD_NETWORK_FOUR_CNT = 4;
    private static final int BAD_NETWORK_ONE_CNT = 1;
    private static final int BAD_NETWORK_THREE_CNT = 3;
    private static final int BAD_NETWORK_TWO_CNT = 2;
    private static final int CIRCLE_MAX_SIZE = 5;
    private static final int CMD_11V_ROAMING_PENALIZE_TIMEOUT = 117;
    private static final int CMD_11V_ROAMING_TIMEOUT = 108;
    private static final int CMD_BG_WIFI_LINK_STATUS = 113;
    private static final int CMD_DISCONNECT_POOR_LINK = 105;
    private static final int CMD_FOREGROUND_APP_CHANGED = 116;
    private static final int CMD_HANDOVER_CONDITIONS_ENABLED = 119;
    private static final int CMD_LEAVE_POOR_WIFI_LINK = 110;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 101;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 102;
    private static final int CMD_NEW_RSSI_RCVD = 104;
    private static final int CMD_QUERY_11V_ROAMING_NETWORK = 103;
    private static final int CMD_REQUEST_ROAMING_NETWORK = 109;
    private static final int CMD_ROAMING_COMPLETED_RCVD = 107;
    private static final int CMD_ROAMING_STARTED_RCVD = 106;
    private static final int CMD_TOP_UID_INTERNET_STATUS = 112;
    private static final int CMD_USER_MOVE_DETECTED = 111;
    private static final int CMD_VERIFY_WIFI_LINK_STATE = 114;
    private static final int CURR_UID_INTERNET_BAD = 1;
    private static final int CURR_UID_INTERNET_GOOD = 0;
    private static final int CURR_UID_INTERNET_UNKNOWN = -1;
    private static final int CURR_UID_INTERNET_VERY_BAD = 2;
    private static final int[] DELAYED_MS_TABLE = {2000, POOR_LINK_MONITOR_MS, 10000, 30000, 0};
    private static final int DOUBLE_PACKET = 2;
    private static final String[] DOWNLOAD_APP_PKT_NAME = {"com.huawei.appmarket", "com.xunlei.downloadprovider", "com.wandoujia.phoenix2", "com.tencent.android.qqdownloader"};
    private static final int FAST_RECOVERY_WIFI_CNT = 6;
    private static final int GOOD_DELTA_RSSI = 8;
    private static final int GOOD_LINK_MONITOR_MS = 8000;
    private static final int INCREASE_CNT = 1;
    private static final int INITIAL_CNT = 0;
    private static final int INVALID_REASON = -1;
    private static final int INVALID_RSSI = -200;
    private static final int INVALID_RSSI_LEVEL = -1;
    private static final long INVALID_TIME = -1;
    private static final int INVALID_UID = -1;
    private static final float LESS_PKTS_BAD_RATE = 0.3f;
    private static final float LESS_PKTS_VERY_BAD_RATE = 0.4f;
    private static final int MAX_ENTERPRISE_AP_COUNT = 5;
    private static final int MIN_RX_PKTS = 100;
    private static final int MIN_TX_PKTS = 3;
    private static final float MORE_PKTS_BAD_RATE = 0.2f;
    private static final float MORE_PKTS_VERY_BAD_RATE = 0.3f;
    private static final int MORE_TX_PKTS = 20;
    private static final int POOR_LINK_MONITOR_MS = 4000;
    private static final String PROP_DISABLE_AUTO_DISC = "hw.wifi.disable_auto_disc";
    private static final int QUERY_11V_ROAMING_NETWORK_DELAYED_MS = 5000;
    private static final int QUERY_REASON_LOW_RSSI = 16;
    private static final int QUERY_REASON_PREFERRED_BSS = 19;
    public static final int REASON_FULL_SCREEN = 203;
    public static final int REASON_LANDSCAPE_MODE = 204;
    public static final int REASON_MOBILE_DATA_INACTIVE = 201;
    public static final int REASON_SCREEN_OFF = 202;
    public static final int REASON_SIGNAL_LEVEL_1_NON_WPA_ACCESS_INTERNET = 107;
    private static final int REASON_SIGNAL_LEVEL_1_TOP_UID_BAD = 101;
    private static final int REASON_SIGNAL_LEVEL_1_URGENT_MINI_APP = 108;
    private static final int REASON_SIGNAL_LEVEL_1_USER_MOVE_AND_ACCESS_INTERNET = 109;
    private static final int REASON_SIGNAL_LEVEL_2_NON_WPA_NORMAL_APP = 105;
    private static final int REASON_SIGNAL_LEVEL_2_NON_WPA_URGENT_APP = 104;
    public static final int REASON_SIGNAL_LEVEL_2_TOP_UID_BAD = 206;
    private static final int REASON_SIGNAL_LEVEL_2_URGENT_MINI_APP = 102;
    private static final int REASON_SIGNAL_LEVEL_2_WPA = 103;
    private static final int REASON_SIGNAL_LEVEL_3_PORTAL = 106;
    public static final int REASON_SIGNAL_LEVEL_3_TOP_UID_BAD = 207;
    public static final int REASON_SIGNAL_LEVEL_4_TOP_UID_BAD = 208;
    public static final int REASON_SIGNAL_LSSS_LEVEL_1_TOP_UID_BAD = 205;
    private static final int REASON_SWITCH_BACK_TO_WIFI = 100;
    private static final int ROAMING_11V_NETWORK_TIMEOUT_MS = 8000;
    private static final int ROAMING_PENALIZE_TIMEOUT_MS = 1800000;
    private static final int ROAMING_TIMEOUT = 8000;
    private static final int SIGNAL_LEVEL_0 = 0;
    private static final int SIGNAL_LEVEL_1 = 1;
    private static final int SIGNAL_LEVEL_2 = 2;
    private static final int SIGNAL_LEVEL_3 = 3;
    private static final int SIGNAL_LEVEL_4 = 4;
    private static final int SLOW_RECOVERY_WIFI_CNT = 16;
    private static final int STEP_INCREASE_INTERVAL_MS = 2000;
    private static final int STEP_INCREASE_THRESHOLD = 8;
    private static final int STRONG_DELTA_RSSI = 5;
    private static final String TAG = "HwWifiConnectivityMonitor";
    private static final float TX_GOOD_RATE = 0.3f;
    private static final String[] URGENT_APP_PKT_NAME = {"com.android.browser", "com.huawei.browser", "com.UCMobile", "com.tencent.mtt", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.sina.news", "com.tencent.news", "com.sohu.newsclient", "com.ifeng.news2", "com.android.chrome", "com.myzaker.ZAKER_Phone", "com.sina.weibo", "com.hexin.plat.android", "com.android.email", "com.google.android.gm"};
    private static final String[] URGENT_MINI_APP_PKT_NAME = {"com.tencent.mm", "com.tencent.mobileqq", "com.eg.android.AlipayGphone", "com.sdu.didi.psnger", "com.didi.es.psngr", "com.meituan.qcs.c.android", "com.didapinche.booking", "com.jingyao.easybike", "cn.caocaokeji.user", "com.szzc.ucar.pilot", "com.ichinait.gbpassenger", "com.mobike.mobikeapp", "so.ofo.labofo", "com.baidu.BaiduMap", "com.autonavi.minimap", "com.google.android.apps.maps", "com.huawei.health", "com.huawei.espacev2", "com.baidu.searchbox", "com.whatsapp", "com.facebook.katana", "com.ichinait.gbpassenger", "com.huawei.works", "huawei.w3", "com.ss.android.ugc.aweme", "com.ss.android.ugc.live", "com.smile.gifmaker"};
    private static final int VERY_BAD_AVE_RTT = 1200;
    private static final int VERY_BAD_DNS_FAIL_COUNT = 5;
    private static HwWifiConnectivityMonitor sWifiConnectivityMonitor = null;
    private int m11vRoamingDisconectedCounter = 0;
    private AtomicBoolean mAccSensorRegistered = new AtomicBoolean(false);
    private long mChrConnectedAndScreenOnStartTime = 0;
    private long mChrWifiConnectedAndScreenOnDuration = 0;
    private final Object mCircleStatLock = new Object();
    private List<String> mCircleStats = new ArrayList();
    private State mConnectedMonitorState = new ConnectedMonitorState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private boolean mInitialized = false;
    private boolean mIsChrLastTimingState = false;
    private boolean mIsChrScreenOn = true;
    private boolean mIsChrWifiConnected = false;
    private boolean mIsWeakSingnalFastSwitchAllowed = true;
    private boolean mIsWifi2CellInStrongSignalEnabled = false;
    private boolean mIsWifiAdvancedChipUser = false;
    private boolean mIsWifiSwitchRobotAlgorithmEnabled = false;
    private int mLastStopWifiSwitchReason = -1;
    private int mNotifyWifiLinkPoorReason = -1;
    private PowerManager mPowerManager;
    private long mRoamingTimer = 0;
    private final StepSensorEventListener mSensorEventListener = new StepSensorEventListener();
    private SensorManager mSensorManager;
    private Sensor mStepCntSensor;
    private int mStopWifiSwitchReason = -1;
    private WifiProChrUploadManager mUploadManager;
    private WifiManager mWifiManager;

    static /* synthetic */ int access$212(HwWifiConnectivityMonitor x0, int x1) {
        int i = x0.m11vRoamingDisconectedCounter + x1;
        x0.m11vRoamingDisconectedCounter = i;
        return i;
    }

    private HwWifiConnectivityMonitor(Context context) {
        super(TAG);
        this.mContext = context;
        this.mIsWifiSwitchRobotAlgorithmEnabled = WifiProCommonUtils.isWifiSwitchRobotAlgorithmEnabled();
        this.mIsWifi2CellInStrongSignalEnabled = WifiProCommonUtils.isWifi2CellInStrongSiganalEnabled();
        this.mIsWifiAdvancedChipUser = WifiProCommonUtils.isAdvancedChipUser();
        this.mIsWeakSingnalFastSwitchAllowed = WifiProCommonUtils.isWeakSingnalFastSwitchAllowed();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mStepCntSensor = this.mSensorManager.getDefaultSensor(19);
        this.mUploadManager = WifiProChrUploadManager.getInstance(context);
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
        setInitialState(this.mDisconnectedMonitorState);
        start();
    }

    public static synchronized HwWifiConnectivityMonitor getInstance(Context context) {
        HwWifiConnectivityMonitor hwWifiConnectivityMonitor;
        synchronized (HwWifiConnectivityMonitor.class) {
            if (sWifiConnectivityMonitor == null) {
                sWifiConnectivityMonitor = new HwWifiConnectivityMonitor(context);
            }
            hwWifiConnectivityMonitor = sWifiConnectivityMonitor;
        }
        return hwWifiConnectivityMonitor;
    }

    public static synchronized HwWifiConnectivityMonitor getInstance() {
        HwWifiConnectivityMonitor hwWifiConnectivityMonitor;
        synchronized (HwWifiConnectivityMonitor.class) {
            hwWifiConnectivityMonitor = sWifiConnectivityMonitor;
        }
        return hwWifiConnectivityMonitor;
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            logI("setup DONE!");
            registerReceivers();
        }
    }

    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.HwWifiConnectivityMonitor.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int newRssi;
                if (intent == null) {
                    HwWifiConnectivityMonitor.this.logE("received intent is null, return.");
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    Object infoTmp = intent.getExtra("networkInfo");
                    NetworkInfo info = null;
                    if (infoTmp instanceof NetworkInfo) {
                        info = (NetworkInfo) infoTmp;
                    }
                    if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        HwWifiConnectivityMonitor.this.handleChrWifiStateChange(false);
                        HwWifiConnectivityMonitor.this.sendMessage(102);
                    } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        HwWifiConnectivityMonitor.this.handleChrWifiStateChange(true);
                        HwWifiConnectivityMonitor.this.sendMessage(101);
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    HwWifiConnectivityMonitor.this.handleChrScreenStateChange(true);
                    HwWifiConnectivityMonitor.this.notifyHandoverConditionsChangeToEnabled();
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    HwWifiConnectivityMonitor.this.handleChrScreenStateChange(false);
                } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction()) && (newRssi = intent.getIntExtra("newRssi", -127)) != -127) {
                    HwWifiConnectivityMonitor.this.sendMessage(104, newRssi, 0);
                }
            }
        }, intentFilter);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            if (message.what != HwWifiConnectivityMonitor.CMD_11V_ROAMING_PENALIZE_TIMEOUT) {
                return true;
            }
            HwWifiConnectivityMonitor.this.logI("DefaultState receive CMD_11V_ROAMING_PENALIZE_TIMEOUT");
            HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter = 0;
            return true;
        }
    }

    class ConnectedMonitorState extends State {
        private boolean m11vBssidSupported = false;
        private int m11vRoamingFailedCounter;
        private boolean m11vRoamingOnGoing;
        private WifiConfiguration mConnectedConfig = null;
        private int mCurrRssiVal;
        private int mCurrTopUidBadCnt = 0;
        private int mCurrTopUidEnterpriseApBadCnt = 0;
        private int mCurrTopUidVeryBadCnt = 0;
        private boolean mEnterVerifyLinkState = false;
        private boolean mIsConditionChangedToEnabled = false;
        private long mLast11vRoamingFailedTs;
        private int mLastSignalLevel;
        private int mPoorLinkRssi = -200;
        private boolean mRoamingOnGoing;
        private int mRssiBeforeSwitchWifiRssi = -200;
        private int mRssiGoodCnt = 0;
        private int mStrongRssiCnt = 0;
        private HashMap<Integer, String> mTopAppWhiteList;

        ConnectedMonitorState() {
        }

        public void enter() {
            HwWifiConnectivityMonitor.this.logI("###ConnectedMonitorState, enter()");
            this.mRoamingOnGoing = false;
            this.m11vRoamingOnGoing = false;
            this.m11vRoamingFailedCounter = 0;
            this.mLast11vRoamingFailedTs = 0;
            this.mConnectedConfig = WifiproUtils.getCurrentWifiConfig(HwWifiConnectivityMonitor.this.mWifiManager);
            this.mEnterVerifyLinkState = false;
            this.mRssiGoodCnt = 0;
            this.mStrongRssiCnt = 0;
            this.mPoorLinkRssi = -200;
            this.mCurrTopUidBadCnt = 0;
            this.mCurrTopUidVeryBadCnt = 0;
            this.mRssiBeforeSwitchWifiRssi = -200;
            WifiInfo wifiInfo = HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
                this.mCurrRssiVal = wifiInfo.getRssi();
                this.m11vBssidSupported = is11vNetworkConnected();
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.logI("ConnectedMonitorState, network = " + StringUtilEx.safeDisplaySsid(wifiInfo.getSSID()) + ", 802.11v = " + this.m11vBssidSupported + ", 2.4GHz = " + wifiInfo.is24GHz() + ", current level = " + this.mLastSignalLevel);
                if (this.m11vBssidSupported && (wifiInfo.is24GHz() || this.mLastSignalLevel <= 2)) {
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(109, 5000);
                }
            }
            if (this.mTopAppWhiteList == null) {
                this.mTopAppWhiteList = WifiProCommonUtils.getAppInWhitelist();
            }
        }

        public boolean processMessage(Message message) {
            int i;
            int i2 = message.what;
            if (i2 == HwWifiConnectivityMonitor.CMD_FOREGROUND_APP_CHANGED) {
                handleForegroundAppChanged(message);
            } else if (i2 != HwWifiConnectivityMonitor.CMD_HANDOVER_CONDITIONS_ENABLED) {
                switch (i2) {
                    case 102:
                        HwWifiConnectivityMonitor.this.removeMessages(103);
                        if (HwWifiConnectivityMonitor.this.hasMessages(108)) {
                            HwWifiConnectivityMonitor.this.removeMessages(108);
                            HwWifiConnectivityMonitor.access$212(HwWifiConnectivityMonitor.this, 1);
                            HwWifiConnectivityMonitor.this.logI("Has messages CMD_11V_ROAMING_TIMEOUT, m11vRoamingDisconectedCounter = " + HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter);
                            if (HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter >= 2) {
                                HwWifiConnectivityMonitor.this.sendMessageDelayed(HwWifiConnectivityMonitor.CMD_11V_ROAMING_PENALIZE_TIMEOUT, 1800000);
                            }
                        }
                        HwWifiConnectivityMonitor.this.removeMessages(105);
                        HwWifiConnectivityMonitor.this.removeMessages(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK);
                        HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                        hwWifiConnectivityMonitor.transitionTo(hwWifiConnectivityMonitor.mDisconnectedMonitorState);
                        break;
                    case 103:
                        if (this.m11vBssidSupported && this.m11vRoamingFailedCounter <= 1 && HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter <= 1) {
                            query11vRoamingNetowrk(16);
                            break;
                        }
                    case 104:
                        handleNewRssiRcvd(message.arg1);
                        break;
                    case 105:
                        disconnectPoorWifiConnection();
                        break;
                    case 106:
                        updateBssidSwitchEvent("wifiSwitchCnt");
                        HwWifiConnectivityMonitor.this.mRoamingTimer = SystemClock.elapsedRealtime();
                        if (HwWifiConnectivityMonitor.this.hasMessages(105)) {
                            HwWifiConnectivityMonitor.this.logI("CMD_DISCONNECT_POOR_LINK remove due to roaming received.");
                            HwWifiConnectivityMonitor.this.removeMessages(105);
                        }
                        this.mRoamingOnGoing = true;
                        break;
                    case 107:
                        if (HwWifiConnectivityMonitor.this.hasMessages(108)) {
                            HwWifiConnectivityMonitor.this.logI("CMD_11V_ROAMING_TIMEOUT remove due to roaming completed received.");
                            HwWifiConnectivityMonitor.this.removeMessages(108);
                        }
                        if (this.mLastSignalLevel >= 3) {
                            HwWifiConnectivityMonitor.this.removeMessages(103);
                        }
                        this.m11vBssidSupported = is11vNetworkConnected();
                        if (this.mRoamingOnGoing && SystemClock.elapsedRealtime() - HwWifiConnectivityMonitor.this.mRoamingTimer < 8000) {
                            updateBssidSwitchEvent("wifiSwitchSuccCnt");
                        }
                        this.mRoamingOnGoing = false;
                        this.m11vRoamingOnGoing = false;
                        this.m11vRoamingFailedCounter = 0;
                        this.mLast11vRoamingFailedTs = 0;
                        break;
                    case 108:
                        if (HwWifiConnectivityMonitor.this.hasMessages(103)) {
                            HwWifiConnectivityMonitor.this.removeMessages(103);
                        }
                        this.m11vRoamingOnGoing = false;
                        this.m11vRoamingFailedCounter++;
                        this.mLast11vRoamingFailedTs = System.currentTimeMillis();
                        HwWifiConnectivityMonitor.this.logI("CMD_11V_ROAMING_TIMEOUT received, counter = " + this.m11vRoamingFailedCounter + ", ts = " + DateFormat.getDateTimeInstance().format(new Date(this.mLast11vRoamingFailedTs)));
                        if (this.mLastSignalLevel == 0) {
                            disconnectPoorWifiConnection();
                            break;
                        }
                        break;
                    case 109:
                        if (this.m11vBssidSupported && this.m11vRoamingFailedCounter <= 1 && HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter <= 1) {
                            if (HwWifiConnectivityMonitor.this.hasMessages(103)) {
                                HwWifiConnectivityMonitor.this.removeMessages(103);
                            }
                            query11vRoamingNetowrk(16);
                            break;
                        }
                    case HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK /* 110 */:
                        handleSignalPoorLevelOne();
                        break;
                    case HwWifiConnectivityMonitor.CMD_USER_MOVE_DETECTED /* 111 */:
                        if (this.mLastSignalLevel <= 1) {
                            handleUserMoveDetected();
                            break;
                        }
                        break;
                    case HwWifiConnectivityMonitor.CMD_TOP_UID_INTERNET_STATUS /* 112 */:
                        handleOldSchemeTopUidInternetStatusChanged(message.arg1, message.arg2);
                        switchWifiNetworkQuickly(message.arg1);
                        break;
                    case HwWifiConnectivityMonitor.CMD_BG_WIFI_LINK_STATUS /* 113 */:
                        handleBgWifiLinkStatusChanged(message.arg1, ((Boolean) message.obj).booleanValue());
                        break;
                    case HwWifiConnectivityMonitor.CMD_VERIFY_WIFI_LINK_STATE /* 114 */:
                        boolean newState = ((Boolean) message.obj).booleanValue();
                        HwWifiConnectivityMonitor.this.logI("CMD_VERIFY_WIFI_LINK_STATE, newState = " + newState + ", oldState = " + this.mEnterVerifyLinkState + ", mPoorLinkRssi = " + this.mPoorLinkRssi);
                        if ((!newState || !this.mEnterVerifyLinkState) && ((newState || this.mEnterVerifyLinkState) && (i = this.mPoorLinkRssi) != -200)) {
                            this.mEnterVerifyLinkState = newState;
                            this.mRssiGoodCnt = 0;
                            this.mStrongRssiCnt = 0;
                            if (!this.mEnterVerifyLinkState) {
                                this.mRssiBeforeSwitchWifiRssi = -200;
                                this.mPoorLinkRssi = -200;
                                break;
                            } else {
                                this.mRssiBeforeSwitchWifiRssi = i;
                                break;
                            }
                        }
                    default:
                        return false;
                }
            } else {
                this.mIsConditionChangedToEnabled = true;
                handleNewRssiRcvd(message.arg1);
            }
            return true;
        }

        private void updateBssidSwitchEvent(String switchType) {
            if (HwWifiConnectivityMonitor.this.mUploadManager != null && switchType != null) {
                Bundle bssidEvent = new Bundle();
                bssidEvent.putInt("index", 1);
                if ("wifiSwitchCnt".equals(switchType)) {
                    HwWifiConnectivityMonitor.this.mUploadManager.addChrBundleStat("wifiSwitchCntEvent", "wifiSwitchCnt", bssidEvent);
                } else {
                    HwWifiConnectivityMonitor.this.mUploadManager.addChrBundleStat("wifiSwitchCntEvent", "wifiSwitchSuccCnt", bssidEvent);
                }
            }
        }

        private void handleForegroundAppChanged(Message message) {
            if (this.mLastSignalLevel == 1 && !HwWifiConnectivityMonitor.this.mIsWifiSwitchRobotAlgorithmEnabled) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.logI("handleForegroundAppChanged, current app : " + message.obj);
                if (HwWifiConnectivityMonitor.this.hasMessages(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK)) {
                    HwWifiConnectivityMonitor.this.removeMessages(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK);
                }
                HwWifiConnectivityMonitor.this.sendMessageDelayed(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK, 3000);
            }
        }

        private void handleOldSchemeTopUidInternetStatusChanged(int uid, int status) {
            HwWifiConnectivityMonitor.this.logI("handleOldSchemeTopUidInternetStatusChanged, uid = " + uid + ", status = " + status);
            if (status == 0) {
                this.mCurrTopUidVeryBadCnt = 0;
                this.mCurrTopUidBadCnt = 0;
            } else if (status == 1) {
                this.mCurrTopUidBadCnt++;
            } else if (status == 2) {
                this.mCurrTopUidVeryBadCnt++;
            }
        }

        private void handlePoorSingalLevelSwitch() {
            if (this.mCurrTopUidBadCnt >= 1 || this.mCurrTopUidVeryBadCnt >= 1) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.logI("signal level = 1, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                notifyWifiLinkPoor(true, 101);
            }
        }

        private void handleSingalMiddleLevelSwitch(String pktName) {
            int i;
            int i2;
            if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME) && (this.mCurrTopUidVeryBadCnt >= 1 || this.mCurrTopUidBadCnt >= 1)) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.logI("signal level = 2, URGENT_MINI, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                notifyWifiLinkPoor(true, 102);
            } else if (WifiProCommonUtils.isEncrypted(this.mConnectedConfig)) {
                int i3 = this.mCurrTopUidBadCnt;
                if (i3 >= 2 || (i2 = this.mCurrTopUidVeryBadCnt) >= 2 || (i3 == 1 && i2 == 1)) {
                    HwWifiConnectivityMonitor hwWifiConnectivityMonitor2 = HwWifiConnectivityMonitor.this;
                    hwWifiConnectivityMonitor2.logI("signal level = 2, WPA2, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                    notifyWifiLinkPoor(true, 103);
                }
            } else if (!WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_APP_PKT_NAME)) {
                int i4 = this.mCurrTopUidBadCnt;
                if (i4 >= 2 || (i = this.mCurrTopUidVeryBadCnt) >= 2 || (i4 == 1 && i == 1)) {
                    HwWifiConnectivityMonitor hwWifiConnectivityMonitor3 = HwWifiConnectivityMonitor.this;
                    hwWifiConnectivityMonitor3.logI("signal level = 2, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                    notifyWifiLinkPoor(true, 105);
                }
            } else if (this.mCurrTopUidBadCnt >= 1 || this.mCurrTopUidVeryBadCnt >= 1) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor4 = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor4.logI("signal level = 2, URGENT, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                notifyWifiLinkPoor(true, 104);
            }
        }

        private void handleStrongSingalLevelSwitch() {
            if (!WifiProCommonUtils.isOpenAndPortal(this.mConnectedConfig)) {
                this.mCurrTopUidVeryBadCnt = 0;
                this.mCurrTopUidBadCnt = 0;
                return;
            }
            int i = this.mCurrTopUidVeryBadCnt;
            if (i >= 4 || (this.mCurrTopUidBadCnt >= 1 && i == 3)) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.logI("signal level = 4, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt + ", rssi = " + this.mCurrRssiVal);
                notifyWifiLinkPoor(true, 106);
            }
        }

        private void switchWifiNetworkQuickly(int uid) {
            int topUid = -1;
            String pktName = "";
            HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
            if (autoConnectManager != null) {
                topUid = autoConnectManager.getCurrentTopUid();
                pktName = autoConnectManager.getCurrentPackageName();
            }
            if (uid != -1 && uid == topUid && !HwWifiConnectivityMonitor.this.isStopWifiSwitch()) {
                WifiInfo wifiInfo = HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    this.mCurrRssiVal = wifiInfo.getRssi();
                    int i = this.mLastSignalLevel;
                    if (i == 0 || i == 1) {
                        handlePoorSingalLevelSwitch();
                    } else if (i == 2) {
                        handleSingalMiddleLevelSwitch(pktName);
                    } else if (i == 3 || i == 4) {
                        handleStrongSingalLevelSwitch();
                    } else {
                        HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                        hwWifiConnectivityMonitor.logI("switchWifiNetworkQuickly, unknown mLastSignalLevel = " + this.mLastSignalLevel);
                    }
                } else {
                    HwWifiConnectivityMonitor.this.logI("switchWifiNetworkQuickly, can't get rssi from wifi info!");
                }
            }
        }

        private void handleBgWifiLinkStatusChanged(int currentRssi, boolean txGood) {
            HwWifiConnectivityMonitor.this.logI("currentRssi=" + currentRssi + " txGood=" + txGood + " reason=" + HwWifiConnectivityMonitor.this.mNotifyWifiLinkPoorReason);
            if (!this.mEnterVerifyLinkState || this.mRssiBeforeSwitchWifiRssi == -200 || HwWifiConnectivityMonitor.this.mNotifyWifiLinkPoorReason != 107) {
                HwWifiConnectivityMonitor.this.logI("handleOldSchemeBgWifiLinkStatusChanged blocked mEnterVerifyLinkState = " + this.mEnterVerifyLinkState + ", mRssiBeforeSwitchWifiRssi = " + this.mRssiBeforeSwitchWifiRssi);
            } else if (WifiProCommonUtils.isHiSiAdvancedChipUser()) {
                HwWifiConnectivityMonitor.this.logI("HiSiAdvancedChipUser use qoe reback to wifi, return");
            } else {
                if (!txGood) {
                    HwWifiConnectivityMonitor.this.logI("handleOldSchemeBgWifiLinkStatusChanged blocked tx is bad");
                    this.mRssiGoodCnt = 0;
                    this.mStrongRssiCnt = 0;
                }
                int i = this.mRssiBeforeSwitchWifiRssi;
                if (i >= -65 || currentRssi < -65) {
                    this.mStrongRssiCnt = 0;
                } else if (currentRssi - i >= 5) {
                    this.mStrongRssiCnt++;
                } else {
                    this.mStrongRssiCnt = 0;
                }
                if (currentRssi - this.mRssiBeforeSwitchWifiRssi >= 8) {
                    this.mRssiGoodCnt++;
                } else {
                    this.mRssiGoodCnt = 0;
                }
                if (this.mStrongRssiCnt == 6 || this.mRssiGoodCnt == 16) {
                    HwWifiConnectivityMonitor.this.logI("handleBgWifiLinkStatusChanged, notify switch back to stable wifi, curr rssi = " + currentRssi + ", last rssi = " + this.mRssiBeforeSwitchWifiRssi + ", strong cnt = " + this.mStrongRssiCnt + ", good cnt = " + this.mRssiGoodCnt);
                    notifyWifiLinkPoor(false, 100);
                    this.mRssiGoodCnt = 0;
                    this.mStrongRssiCnt = 0;
                }
            }
        }

        private void handleNewRssiRcvd(int newRssi) {
            this.mCurrRssiVal = newRssi;
            int currentSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo());
            if ((currentSignalLevel >= 0 && currentSignalLevel != this.mLastSignalLevel) || (this.mIsConditionChangedToEnabled && currentSignalLevel == 1)) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.logI("signal level changed: " + this.mLastSignalLevel + " --> " + currentSignalLevel + ", 802.11v = " + this.m11vBssidSupported);
                if (currentSignalLevel == 2) {
                    HwWifiConnectivityMonitor.this.registerStepCntSensor();
                } else if (currentSignalLevel == 1 && !HwWifiConnectivityMonitor.this.hasMessages(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK)) {
                    HwWifiConnectivityMonitor.this.registerStepCntSensor();
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
                } else if (currentSignalLevel == 4) {
                    HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
                }
                if (currentSignalLevel == 0 && !HwWifiConnectivityMonitor.this.hasMessages(105)) {
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(105, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
                } else if (currentSignalLevel >= 2) {
                    HwWifiConnectivityMonitor.this.removeMessages(105);
                    HwWifiConnectivityMonitor.this.removeMessages(HwWifiConnectivityMonitor.CMD_LEAVE_POOR_WIFI_LINK);
                } else if (currentSignalLevel > 0) {
                    HwWifiConnectivityMonitor.this.removeMessages(105);
                }
                if (this.m11vBssidSupported && !this.m11vRoamingOnGoing) {
                    if (currentSignalLevel > 2 || this.m11vRoamingFailedCounter > 1 || HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter > 1) {
                        if (currentSignalLevel >= 3) {
                            HwWifiConnectivityMonitor.this.removeMessages(103);
                        } else {
                            HwWifiConnectivityMonitor.this.logI("Enter handleNewRssiRcvd m11vBssidSupported else");
                        }
                    } else if (!HwWifiConnectivityMonitor.this.hasMessages(103)) {
                        HwWifiConnectivityMonitor hwWifiConnectivityMonitor2 = HwWifiConnectivityMonitor.this;
                        hwWifiConnectivityMonitor2.logI("to delay " + HwWifiConnectivityMonitor.DELAYED_MS_TABLE[currentSignalLevel] + " ms to request roaming 802.11v network.");
                        HwWifiConnectivityMonitor.this.sendMessageDelayed(103, (long) HwWifiConnectivityMonitor.DELAYED_MS_TABLE[currentSignalLevel]);
                    }
                }
            }
            this.mIsConditionChangedToEnabled = false;
            this.mLastSignalLevel = currentSignalLevel;
        }

        private void disconnectPoorWifiConnection() {
            boolean isDisableAutoDisconnect = SystemProperties.getBoolean(HwWifiConnectivityMonitor.PROP_DISABLE_AUTO_DISC, false);
            if (HwWifiConnectivityMonitor.this.mIsWifiSwitchRobotAlgorithmEnabled && HwWifiConnectivityMonitor.this.mWifiManager != null && !isDisableAutoDisconnect && !WifiProCommonUtils.isNoSIMCard(HwWifiConnectivityMonitor.this.mContext)) {
                HwWifiConnectivityMonitor.this.logI("single level 0 stay 4s, disconnectPoorWifiConnection");
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (wifiProStateMachine != null) {
                    wifiProStateMachine.setChrWifiDisconnectedReason();
                    wifiProStateMachine.uploadWifiSwitchStatistics(true);
                }
                HwWifiConnectivityMonitor.this.mWifiManager.disconnect();
            } else if (HwWifiConnectivityMonitor.this.mWifiManager != null && !isDisableAutoDisconnect && (((!HwWifiConnectivityMonitor.this.isFullScreen() && !WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext)) || HwWifiConnectivityMonitor.this.isNeedDiscInGame()) && !HwWifiConnectivityMonitor.this.isMobileDataInactive() && !WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext))) {
                HwWifiConnectivityMonitor.this.logI("WARN: to auto disconnect network quickly due to poor rssi and no roaming (signal level = 0)");
                HwWifiConnectivityMonitor.this.mWifiManager.disconnect();
            } else if (this.mLastSignalLevel == 0 && !HwWifiConnectivityMonitor.this.hasMessages(105)) {
                HwWifiConnectivityMonitor.this.sendMessageDelayed(105, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
            }
        }

        private void query11vRoamingNetowrk(int reason) {
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.logI("query11vRoamingNetowrk, mRoamingOnGoing = " + this.mRoamingOnGoing + ", m11vRoamingOnGoing = " + this.m11vRoamingOnGoing);
            if (!this.mRoamingOnGoing && !this.m11vRoamingOnGoing) {
                Bundle data = new Bundle();
                data.putInt("reason", reason);
                WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 26, data);
                this.m11vRoamingOnGoing = true;
                if (HwWifiConnectivityMonitor.this.hasMessages(108)) {
                    HwWifiConnectivityMonitor.this.removeMessages(108);
                }
                HwWifiConnectivityMonitor.this.sendMessageDelayed(108, 8000);
            }
        }

        private boolean is11vNetworkConnected() {
            List<ScanResult> scanResults;
            String currentBssid = WifiProCommonUtils.getCurrentBssid(HwWifiConnectivityMonitor.this.mWifiManager);
            if (HwWifiConnectivityMonitor.this.mWifiManager == null || currentBssid == null || (scanResults = WifiproUtils.getScanResultsFromWsm()) == null) {
                return false;
            }
            for (ScanResult scanResult : scanResults) {
                if (currentBssid.equals(scanResult.BSSID) && scanResult.dot11vNetwork) {
                    return true;
                }
            }
            return false;
        }

        private void handleSignalPoorLevelOne() {
            HashMap<Integer, String> hashMap;
            if (!HwWifiConnectivityMonitor.this.mIsWifiSwitchRobotAlgorithmEnabled || !HwWifiConnectivityMonitor.this.mIsWifiAdvancedChipUser || !HwWifiConnectivityMonitor.this.mIsWeakSingnalFastSwitchAllowed) {
                if (this.mConnectedConfig != null && !HwWifiConnectivityMonitor.this.isMobileDataInactive()) {
                    String pktName = "";
                    HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
                    if (autoConnectManager != null) {
                        autoConnectManager.getCurrentTopUid();
                        pktName = autoConnectManager.getCurrentPackageName();
                    }
                    if (this.mTopAppWhiteList != null) {
                        HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                        hwWifiConnectivityMonitor.logI("mTopAppWhiteList.size() = " + this.mTopAppWhiteList.size());
                    }
                    if (!WifiProCommonUtils.isEncrypted(this.mConnectedConfig) && (hashMap = this.mTopAppWhiteList) != null && hashMap.containsValue(pktName)) {
                        notifyWifiLinkPoor(true, 107);
                    } else if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME)) {
                        HwWifiConnectivityMonitor.this.logI("handleSignalPoorLevelOne, URGENT_MINI_APP_PKT_NAME matched.");
                        notifyWifiLinkPoor(true, 108);
                    }
                }
            } else if (this.mConnectedConfig != null && HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn()) {
                HwWifiConnectivityMonitor.this.logI("single level 1 stay 4s, notifyWifiLinkPoor");
                notifyWifiLinkPoor(true, 107);
            }
        }

        private void handleUserMoveDetected() {
            int foregroundUid = -1;
            String pktName = "";
            HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
            if (autoConnectManager != null) {
                foregroundUid = autoConnectManager.getCurrentTopUid();
                pktName = autoConnectManager.getCurrentPackageName();
            }
            boolean isAppAccessInternet = HwUidTcpMonitor.getInstance(HwWifiConnectivityMonitor.this.mContext).isAppAccessInternet(foregroundUid);
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.logI("handleUserMoveDetected, isScreenOn = " + HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() + ", isMobileDataInactive = " + HwWifiConnectivityMonitor.this.isMobileDataInactive() + ", isFullScreen = " + HwWifiConnectivityMonitor.this.isFullScreen() + ", isAppAccessInternet = " + isAppAccessInternet);
            if (!HwWifiConnectivityMonitor.this.mIsWifiSwitchRobotAlgorithmEnabled) {
                boolean fullScreen = HwWifiConnectivityMonitor.this.isFullScreen() && !WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME);
                if (HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() && !HwWifiConnectivityMonitor.this.isMobileDataInactive() && !fullScreen && !WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext) && !WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext) && isAppAccessInternet) {
                    notifyWifiLinkPoor(true, 109);
                    HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
                }
            } else if (HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() && isAppAccessInternet) {
                HwWifiConnectivityMonitor.this.logI("handleUserMoveDetected");
                HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
            }
        }

        private void notifyWifiLinkPoor(boolean poorLink, int reason) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
            if (wifiProStateMachine != null) {
                if (poorLink) {
                    this.mPoorLinkRssi = this.mCurrRssiVal;
                }
                synchronized (HwWifiConnectivityMonitor.this.mCircleStatLock) {
                    HwWifiConnectivityMonitor.this.mNotifyWifiLinkPoorReason = reason;
                }
                wifiProStateMachine.notifyWifiLinkPoor(poorLink, reason);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFullScreen() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 9, new Bundle());
        if (result != null) {
            return result.getBoolean("isFullscreen");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedDiscInGame() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 27, new Bundle());
        if (result != null) {
            return result.getBoolean("isInGameAndNeedDisc");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerStepCntSensor() {
        if (!this.mAccSensorRegistered.get()) {
            logI("registerStepCntSensor, mSensorEventListener");
            this.mSensorEventListener.reset();
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mStepCntSensor, 3);
            this.mAccSensorRegistered.set(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterStepCntSensor() {
        if (this.mAccSensorRegistered.get() && this.mSensorEventListener != null) {
            logI("unregisterStepCntSensor, mSensorEventListener");
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mAccSensorRegistered.set(false);
        }
    }

    /* access modifiers changed from: package-private */
    public class StepSensorEventListener implements SensorEventListener {
        private int mLastStepCnt = 0;
        private int mMotionDetectedCnt = 0;
        private long mSensorEventRcvdTs = HwWifiConnectivityMonitor.INVALID_TIME;

        public StepSensorEventListener() {
        }

        public void reset() {
            this.mLastStepCnt = 0;
            this.mMotionDetectedCnt = 0;
            this.mSensorEventRcvdTs = HwWifiConnectivityMonitor.INVALID_TIME;
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (event != null && event.sensor != null && event.sensor.getType() == 19) {
                long currentTimestamp = System.currentTimeMillis();
                int currentStepCnt = (int) event.values[0];
                if (currentStepCnt - this.mLastStepCnt > 0) {
                    this.mMotionDetectedCnt++;
                    if (this.mMotionDetectedCnt == 8) {
                        this.mMotionDetectedCnt = 0;
                        HwWifiConnectivityMonitor.this.sendMessage(HwWifiConnectivityMonitor.CMD_USER_MOVE_DETECTED);
                    }
                } else {
                    long j = this.mSensorEventRcvdTs;
                    if (j > 0 && currentTimestamp - j > 2000) {
                        this.mMotionDetectedCnt = 0;
                    }
                }
                this.mLastStepCnt = currentStepCnt;
                this.mSensorEventRcvdTs = currentTimestamp;
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.logI("SensorEventListener::onAccuracyChanged, accuracy = " + accuracy);
        }
    }

    class DisconnectedMonitorState extends State {
        DisconnectedMonitorState() {
        }

        public void enter() {
            HwWifiConnectivityMonitor.this.logI("###DisconnectedMonitorState, enter()");
            HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
        }

        public boolean processMessage(Message message) {
            if (message.what != 101) {
                return false;
            }
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.transitionTo(hwWifiConnectivityMonitor.mConnectedMonitorState);
            return true;
        }
    }

    public synchronized boolean notifyTopUidTcpInfo(int uid, int tx, int rx, int reTx, int rtt, int rttPkts) {
        float aveRtt;
        logI("ENTER: notifyTopUidTcpInfo, mInitialized = " + this.mInitialized + ", uid = " + uid + ", tx = " + tx);
        if (this.mInitialized && uid != -1 && tx > 0) {
            float tr = ((float) reTx) / ((float) tx);
            if (rtt <= 0 || rttPkts <= 0) {
                aveRtt = 0.0f;
            } else {
                aveRtt = ((float) rtt) / ((float) rttPkts);
            }
            logI("notifyTopUidTcpInfo sample apkName = " + HwAutoConnectManager.getInstance().getCurrentPackageName() + ", tx = " + tx + ", rx = " + rx + ", reTx = " + reTx + ", rtt = " + rtt + ", rttPkts = " + rttPkts + ", tr = " + tr + ", aveRtt = " + aveRtt);
            updateTopUidInfoStatistics(tx, reTx, rx, rtt, rttPkts, 0);
            boolean isReported = false;
            int internetStatus = getInternetStatusByTcpInfo(tx, rx, tr, aveRtt);
            if (internetStatus != -1) {
                isReported = true;
            }
            if (!this.mIsWifiSwitchRobotAlgorithmEnabled && isReported) {
                sendMessage(CMD_TOP_UID_INTERNET_STATUS, uid, internetStatus);
                return true;
            }
        }
        return false;
    }

    public synchronized void notifyTopUidDnsInfo(int uid, int dnsFailCount) {
        if (uid > 0 && dnsFailCount > 0) {
            updateTopUidInfoStatistics(0, 0, 0, 0, 0, dnsFailCount);
            if (!this.mIsWifiSwitchRobotAlgorithmEnabled) {
                logI("ENTER: notifyTopUidDnsInfo, dnsFailCount = " + dnsFailCount + ", uid = " + uid);
                if (dnsFailCount > 5) {
                    sendMessage(CMD_TOP_UID_INTERNET_STATUS, uid, 2);
                } else if (dnsFailCount >= 3) {
                    sendMessage(CMD_TOP_UID_INTERNET_STATUS, uid, 1);
                }
            }
        }
    }

    public synchronized void notifyBackgroundWifiLinkInfo(int rssi, int txgood, int txbad, int rxgood) {
        if (this.mInitialized && txgood > 0) {
            float badRate = ((float) txbad) / ((float) (txbad + txgood));
            logI("notifyBackgroundWifiLinkInfo badRate = " + badRate);
            if (badRate < 0.3f) {
                sendMessage(CMD_BG_WIFI_LINK_STATUS, rssi, 0, true);
            } else {
                sendMessage(CMD_BG_WIFI_LINK_STATUS, rssi, 0, false);
            }
        } else if (this.mInitialized && txgood <= 0) {
            logI("ENTER: notifyBackgroundWifiLinkInfo tx was bad, rssi = " + rssi + ", txgood = " + txgood + ", txbad = " + txbad + ", rxgood = " + rxgood);
        }
    }

    public synchronized void notifyWifiRoamingStarted() {
        logI("ENTER: notifyWifiRoamingStarted()");
        if (this.mInitialized) {
            sendMessage(106);
        }
    }

    public synchronized void notifyWifiRoamingCompleted() {
        logI("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized) {
            sendMessage(107);
        }
    }

    public synchronized void requestRoamingByNoInternet() {
        logI("ENTER: requestRoamingByNoInternet()");
        if (this.mInitialized) {
            sendMessage(109);
        }
    }

    public synchronized void notifyWifiDisconnected() {
        if (this.mInitialized) {
            sendMessage(102);
        }
    }

    public synchronized void notifyVerifyingLinkState(boolean enterVerifyingLinkState) {
        if (this.mInitialized) {
            sendMessage(CMD_VERIFY_WIFI_LINK_STATE, Boolean.valueOf(enterVerifyingLinkState));
        }
    }

    public void notifyHandoverConditionsChangeToEnabled() {
        if (this.mPowerManager.isScreenOn() && !isMobileDataInactive()) {
            logI("handover conditions changed to enable, try to trigger handover.");
            sendMessage(CMD_HANDOVER_CONDITIONS_ENABLED, WifiProCommonUtils.getCurrentRssi(this.mWifiManager), 0);
        }
    }

    public synchronized void disconnectePoorWifi() {
        if (this.mInitialized) {
            sendMessage(105);
        }
    }

    public synchronized void notifyForegroundAppChanged(String appPackageName) {
        if (this.mInitialized) {
            sendMessage(CMD_FOREGROUND_APP_CHANGED, appPackageName);
        }
    }

    public List<String> getCircleStat() {
        List<String> list;
        synchronized (this.mCircleStatLock) {
            list = this.mCircleStats;
        }
        return list;
    }

    public void resetCircleStat() {
        synchronized (this.mCircleStatLock) {
            this.mCircleStats.clear();
            this.mNotifyWifiLinkPoorReason = -1;
            this.mStopWifiSwitchReason = -1;
        }
    }

    public void logI(String msg) {
        Log.i(TAG, msg);
    }

    public void logE(String msg) {
        Log.e(TAG, msg);
    }

    private int getInternetStatusByTcpInfo(int tx, int rx, float tr, float aveRtt) {
        if ((tr >= 0.3f && tx >= 20 && rx <= 100) || (tr >= 0.4f && tx < 20 && tx >= 3 && rx <= 200)) {
            logI("notifyTopUidTcpInfo internet status very bad");
            return 2;
        } else if ((tr >= MORE_PKTS_BAD_RATE && tx >= 20 && rx <= 100) || (tr >= 0.3f && tx < 20 && tx >= 3 && rx <= 200)) {
            logI("notifyTopUidTcpInfo internet status bad");
            return 1;
        } else if (aveRtt > 1200.0f) {
            logI("notifyTopUidTcpInfo internet status very bad for rtt > 1200");
            return 2;
        } else if (aveRtt > 800.0f) {
            logI("notifyTopUidTcpInfo internet status very bad for rtt > 800");
            return 1;
        } else if (rx <= 1) {
            return -1;
        } else {
            logI("notifyTopUidTcpInfo internet status good");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMobileDataInactive() {
        return WifiProCommonUtils.isMobileDataOff(this.mContext) || WifiProCommonUtils.isNoSIMCard(this.mContext);
    }

    private void updateTopUidInfoStatistics(int tx, int reTx, int rx, int rtt, int rttPkts, int dnsFailCount) {
        String newCircleStat = tx + "|" + reTx + "|" + rx + "|" + rtt + "|" + rttPkts + "|" + dnsFailCount + "|" + HwAutoConnectManager.getInstance().getCurrentPackageName();
        synchronized (this.mCircleStatLock) {
            if (this.mCircleStats.size() < 5) {
                this.mCircleStats.add(newCircleStat);
            } else {
                this.mCircleStats.remove(0);
                this.mCircleStats.add(newCircleStat);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStopWifiSwitch() {
        synchronized (this.mCircleStatLock) {
            if (!this.mIsWifiSwitchRobotAlgorithmEnabled && isMobileDataInactive()) {
                this.mStopWifiSwitchReason = REASON_MOBILE_DATA_INACTIVE;
                return true;
            } else if (!this.mPowerManager.isScreenOn()) {
                this.mStopWifiSwitchReason = REASON_SCREEN_OFF;
                return true;
            } else if (isFullScreen()) {
                this.mStopWifiSwitchReason = REASON_FULL_SCREEN;
                return true;
            } else if (WifiProCommonUtils.isLandscapeMode(this.mContext)) {
                this.mStopWifiSwitchReason = REASON_LANDSCAPE_MODE;
                return true;
            } else {
                this.mStopWifiSwitchReason = -1;
                return false;
            }
        }
    }

    private boolean isEnterpriseSecurity(int networkId) {
        List<WifiConfiguration> configs = WifiproUtils.getAllConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            logE("isEnterpriseSecurity configs is invalid");
            return false;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && networkId == config.networkId) {
                return config.isEnterprise();
            }
        }
        return false;
    }

    private String createQuotedSsid(String ssid) {
        return "\"" + ssid + "\"";
    }

    private int getEnterpriseCount() {
        WifiInfo info = this.mWifiManager.getConnectionInfo();
        int enterpriseApCount = 0;
        if (info == null) {
            logE("getEnterpriseCount wifi info invalid");
            return 0;
        } else if (!isEnterpriseSecurity(info.getNetworkId())) {
            return 0;
        } else {
            for (ScanResult result : this.mWifiManager.getScanResults()) {
                if (result != null && createQuotedSsid(result.SSID).equals(info.getSSID())) {
                    enterpriseApCount++;
                }
            }
            return enterpriseApCount;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChrWifiStateChange(boolean isWifiConnected) {
        this.mIsChrWifiConnected = isWifiConnected;
        calculateChrWifiConnectedAndScreenOnDuration();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChrScreenStateChange(boolean isScreenOn) {
        this.mIsChrScreenOn = isScreenOn;
        calculateChrWifiConnectedAndScreenOnDuration();
    }

    private void calculateChrWifiConnectedAndScreenOnDuration() {
        boolean isChrStartTiming = this.mIsChrWifiConnected && this.mIsChrScreenOn;
        if (isChrStartTiming != this.mIsChrLastTimingState) {
            if (isChrStartTiming) {
                this.mChrConnectedAndScreenOnStartTime = SystemClock.elapsedRealtime();
            } else if (this.mChrConnectedAndScreenOnStartTime > 0) {
                this.mChrWifiConnectedAndScreenOnDuration += SystemClock.elapsedRealtime() - this.mChrConnectedAndScreenOnStartTime;
            }
        }
        this.mIsChrLastTimingState = isChrStartTiming;
    }

    public long getChrWifiConnectedAndScreenOnDuration() {
        this.mIsChrWifiConnected = false;
        calculateChrWifiConnectedAndScreenOnDuration();
        long chrDurationResult = this.mChrWifiConnectedAndScreenOnDuration;
        this.mChrConnectedAndScreenOnStartTime = 0;
        this.mChrWifiConnectedAndScreenOnDuration = 0;
        return chrDurationResult;
    }
}
