package com.android.server.wifi.wifipro;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManagerPolicy;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.HwWifiCHRConstImpl;
import com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class WifiProStateMachine extends StateMachine implements INetworkQosCallBack, INetworksHandoverCallBack, IWifiProUICallBack, IDualBandManagerCallback {
    private static final int ACCESS_TYPE = 1;
    private static final boolean AUTO_EVALUATE_SWITCH = false;
    private static final int BASE = 136168;
    private static final boolean BQE_TEST = false;
    private static final int CHR_AVAILIABLE_AP_COUNTER = 2;
    private static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final int CSP_INVISIBILITY = 0;
    private static final int CSP_VISIBILITY = 1;
    private static final boolean DBG = true;
    private static final boolean DDBG = false;
    private static final boolean DEFAULT_WIFI_PRO_ENABLED = false;
    private static final int DELAY_EVALUTE_NEXT_AP_TIME = 2000;
    private static int DUALBAND_CHR_HANDOVER_THRESHOLD = 0;
    private static int DUALBAND_CHR_PINGPANG_THRESHOLD = 0;
    private static int DUALBAND_CHR_VERIFY_THRESHOLD = 0;
    private static final int DUALBAND_HANDOVER_FAILED_COUNT = 2;
    private static final int DUALBAND_HANDOVER_INBLACK_LIST_COUNT = 4;
    private static final int DUALBAND_HANDOVER_SCORE_NOT_SATISFY_COUNT = 3;
    private static final int DUALBAND_HANDOVER_SUC_COUNT = 1;
    private static int DUALBAND_TYPE_MIX = 0;
    private static int DUALBAND_TYPE_SINGLE = 0;
    private static int DUALBAND_TYPE_UNKONW = 0;
    private static final int EVALUATE_ALL_TIMEOUT = 75000;
    private static final int EVALUATE_VALIDITY_TIMEOUT = 120000;
    private static final int EVALUATE_WIFI_CONNECTED_TIMEOUT = 35000;
    private static final int EVALUATE_WIFI_RTT_BQE_INTERVAL = 3000;
    private static final int EVENT_CHECK_AVAILABLE_AP_RESULT = 136176;
    private static final int EVENT_CHECK_MOBILE_QOS_RESULT = 136180;
    private static final int EVENT_CHECK_WIFI_INTERNET = 136192;
    private static final int EVENT_CHECK_WIFI_INTERNET_RESULT = 136181;
    private static final int EVENT_CONFIGURED_NETWORKS_CHANGED = 136308;
    public static final int EVENT_DELAY_EVALUTE_NEXT_AP = 136314;
    private static final int EVENT_DELAY_REINITIALIZE_WIFI_MONITOR = 136184;
    private static final int EVENT_DEVICE_SCREEN_ON = 136170;
    private static final int EVENT_DIALOG_CANCEL = 136183;
    private static final int EVENT_DIALOG_OK = 136182;
    private static final int EVENT_DUALBAND_5GAP_AVAILABLE = 136370;
    private static final int EVENT_DUALBAND_DELAY_RETRY = 136372;
    private static final int EVENT_DUALBAND_RSSITH_RESULT = 136368;
    private static final int EVENT_DUALBAND_SCORE_RESULT = 136369;
    private static final int EVENT_DUALBAND_WIFI_HANDOVER_RESULT = 136371;
    private static final int EVENT_EMUI_CSP_SETTINGS_CHANGE = 136190;
    private static final int EVENT_EVALUATE_COMPLETE = 136295;
    private static final int EVENT_EVALUATE_INITIATE = 136294;
    private static final int EVENT_EVALUATE_START_CHECK_INTERNET = 136307;
    private static final int EVENT_EVALUATE_VALIDITY_TIMEOUT = 136296;
    private static final int EVENT_EVALUTE_ABANDON = 136305;
    private static final int EVENT_EVALUTE_STOP = 136303;
    private static final int EVENT_EVALUTE_TIMEOUT = 136304;
    private static final int EVENT_GET_WIFI_TCPRX = 136311;
    private static final int EVENT_HTTP_REACHABLE_RESULT = 136195;
    private static final int EVENT_LAST_EVALUTE_VALID = 136302;
    private static final int EVENT_MOBILE_CONNECTIVITY = 136175;
    private static final int EVENT_MOBILE_DATA_STATE_CHANGED_ACTION = 136186;
    private static final int EVENT_MOBILE_QOS_CHANGE = 136173;
    private static final int EVENT_MOBILE_RECOVERY_TO_WIFI = 136189;
    private static final int EVENT_MOBILE_SWITCH_DELAY = 136194;
    private static final int EVENT_NETWORK_CONNECTIVITY_CHANGE = 136177;
    private static final int EVENT_RETRY_WIFI_TO_WIFI = 136191;
    private static final int EVENT_SCAN_RESULTS_AVAILABLE = 136293;
    private static final int EVENT_START_BQE = 136306;
    private static final int EVENT_SUPPLICANT_STATE_CHANGE = 136297;
    private static final int EVENT_USER_ROVE_IN = 136193;
    private static final int EVENT_WIFIPRO_EVALUTE_STATE_CHANGE = 136298;
    private static final int EVENT_WIFIPRO_WORKING_STATE_CHANGE = 136171;
    private static final int EVENT_WIFI_CHECK_UNKOWN = 136309;
    private static final int EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT = 136301;
    private static final int EVENT_WIFI_EVALUTE_TCPRTT_RESULT = 136299;
    private static final int EVENT_WIFI_GOOD_INTERVAL_TIMEOUT = 136187;
    private static final int EVENT_WIFI_HANDOVER_WIFI_RESULT = 136178;
    private static final int EVENT_WIFI_NETWORK_STATE_CHANGE = 136169;
    private static final int EVENT_WIFI_P2P_CONNECTION_CHANGED = 136310;
    private static final int EVENT_WIFI_QOS_CHANGE = 136172;
    private static final int EVENT_WIFI_RECOVERY_TIMEOUT = 136188;
    private static final int EVENT_WIFI_RSSI_CHANGE = 136179;
    private static final int EVENT_WIFI_SECURITY_QUERY_TIMEOUT = 136313;
    private static final int EVENT_WIFI_SECURITY_RESPONSE = 136312;
    private static final int EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE = 136300;
    private static final int EVENT_WIFI_STATE_CHANGED_ACTION = 136185;
    private static final int GOOD_LINK_DETECTED = 131874;
    public static final int HANDOVER_5G_DIFFERENCE_SCORE = 5;
    private static final int HANDOVER_5G_DIRECTLY_RSSI = -70;
    public static final int HANDOVER_5G_DIRECTLY_SCORE = 40;
    public static final int HANDOVER_5G_MAX_RSSI = -45;
    public static final int HANDOVER_5G_SINGLE_RSSI = -55;
    private static final int HANDOVER_MIN_LEVEL_INTERVAL = 2;
    private static final String HUAWEI_SETTINGS = "com.android.settings.Settings$WifiSettingsActivity";
    private static final String ILLEGAL_BSSID_01 = "any";
    private static final String ILLEGAL_BSSID_02 = "00:00:00:00:00:00";
    private static final int INVALID_CHR_RCD_TIME = 0;
    private static final int INVALID_PID = -1;
    private static final int JUDGE_WIFI_FAST_REOPEN_TIME = 30000;
    private static final String KEY_EMUI_WIFI_TO_PDP = "wifi_to_pdp";
    private static final int KEY_MOBILE_HANDOVER_WIFI = 2;
    private static final String KEY_SMART_NETWORK_SWITCHING = "smart_network_switching";
    private static final String KEY_WIFIPRO_MANUAL_CONNECT = "wifipro_manual_connect_ap";
    private static final String KEY_WIFIPRO_RECOMMENDING_ACCESS_POINTS = "wifipro_recommending_access_points";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK = "wifipro_auto_recommend";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK_SAVED_STATE = "wifipro_auto_recommend_saved_state";
    private static final int KEY_WIFI_HANDOVER_MOBILE = 1;
    private static final int MILLISECONDS_OF_ONE_SECOND = 1000;
    private static final int MOBILE = 0;
    private static final int MOBILE_DATA_OFF_SWITCH_DELAY_MS = 3000;
    private static final int NETWORK_POOR_LEVEL_THRESHOLD = 2;
    private static final int OOBE_COMPLETE = 1;
    private static final int POOR_LINK_DETECTED = 131873;
    private static final int PORTAL_CHECK_MAX_TIMERS = 20;
    private static final int PORTAL_HANDOVER_DELAY_TIME = 15000;
    private static final int QOS_LEVEL = 2;
    private static final int QOS_SCORE = 3;
    private static final int SEND_MESSAGE_DELAY_TIME = 30000;
    private static final String SETTING_SECURE_CONN_WIFI_PID = "wifipro_connect_wifi_app_pid";
    private static final String SETTING_SECURE_VPN_WORK_VALUE = "wifipro_network_vpn_state";
    private static final String SETTING_SECURE_WIFI_NO_INT = "wifi_no_internet_access";
    private static final int SYSTEM_UID = 1000;
    private static final String SYS_OPER_CMCC = "ro.config.operators";
    private static final String SYS_PROPERT_PDP = "ro.config.hw_RemindWifiToPdp";
    private static final String TAG = "WiFi_PRO_WifiProStateMachine";
    private static final int TCP_IP = 101;
    private static final int THRESHOD_RSSI = -82;
    private static final int THRESHOD_RSSI_HIGH = -76;
    private static final int THRESHOD_RSSI_LOW = -88;
    private static final int TURN_OFF_MOBILE = 0;
    private static final int TURN_OFF_WIFI = 1;
    private static final int TURN_OFF_WIFI_PRO = 2;
    private static final int TURN_ON_WIFI_PRO = 3;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    private static final int WIFI = 1;
    private static final int WIFI_CHECK_DELAY_TIME = 30000;
    private static final int WIFI_CHECK_UNKNOW_TIMER = 1;
    private static final String WIFI_CSP_DISPALY_STATE = "wifi_csp_dispaly_state";
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_GOOD_LINK_MAX_TIME_LIMIT = 1800000;
    private static final int WIFI_HANDOVER_MOBILE_TIMER_LIMIT = 4;
    private static final int WIFI_HANDOVER_TIMERS = 2;
    private static final int WIFI_SCAN_COUNT = 4;
    private static final int WIFI_SCAN_INTERVAL_MAX = 12;
    private static final int WIFI_TCPRX_STATISTICS_INTERVAL = 3000;
    private static final int WIFI_TO_WIFI_THRESHOLD = 3;
    private static final int WIFI_VERYY_INTERVAL_TIME = 30000;
    private static boolean mIsWifiManualEvaluating;
    private static boolean mIsWifiSemiAutoEvaluating;
    private static WifiProStateMachine mWifiProStateMachine;
    private boolean isVariableInited;
    private AbsPhoneWindowManager mAbsPhoneWindowManager;
    private ActivityManager mActivityManager;
    private List<String> mAppWhitelists;
    private int mAvailable5GAPAuthType;
    private String mAvailable5GAPBssid;
    private String mAvailable5GAPSsid;
    private String mBadBssid;
    private String mBadSsid;
    private BroadcastReceiver mBroadcastReceiver;
    private long mChrRoveOutStartTime;
    private long mChrWifiDidableStartTime;
    private long mChrWifiDisconnectStartTime;
    private int mConnectWiFiAppPid;
    private ConnectivityManager mConnectivityManager;
    private ContentResolver mContentResolver;
    private Context mContext;
    private WifiInfo mCurrWifiInfo;
    private String mCurrentBssid;
    private int mCurrentRssi;
    private String mCurrentSsid;
    private int mCurrentVerfyCounter;
    private WifiConfiguration mCurrentWifiConfig;
    private int mCurrentWifiLevel;
    private DefaultState mDefaultState;
    private boolean mDisableDuanBandHandover;
    private ArrayList<HwDualBandMonitorInfo> mDualBandCloneMonitorApList;
    private String mDualBandConnectAPSsid;
    private long mDualBandConnectTime;
    private ArrayList<WifiProEstimateApInfo> mDualBandEstimateApList;
    private int mDualBandEstimateInfoSize;
    HwDualBandManager mDualBandManager;
    private ArrayList<HwDualBandMonitorInfo> mDualBandMonitorApList;
    private int mDualBandMonitorInfoSize;
    private boolean mDualBandMonitorStart;
    private WifiProEstimateApInfo mDualbandCurrentAPInfo;
    private long mDualbandPingPangTime;
    private int mDualbandRoveInConut;
    private WifiProEstimateApInfo mDualbandSelectAPInfo;
    private long mDualbandhanoverTime;
    private int mDuanBandHandoverType;
    private volatile int mEmuiPdpSwichValue;
    private BroadcastReceiver mHMDBroadcastReceiver;
    private IntentFilter mHMDIntentFilter;
    private HwAutoConnectManager mHwAutoConnectManager;
    private HwDualBandBlackListManager mHwDualBandBlackListMgr;
    private HwIntelligenceWiFiManager mHwIntelligenceWiFiManager;
    private IntentFilter mIntentFilter;
    private boolean mIsAllowEvaluate;
    private boolean mIsDualbandhandover;
    private boolean mIsDualbandhandoverSucc;
    private boolean mIsManualConnectedWiFi;
    private boolean mIsMobileDataEnabled;
    private boolean mIsNetworkAuthen;
    private boolean mIsP2PConnectedOrConnecting;
    private boolean mIsPoorRssiRequestCheckWiFi;
    private boolean mIsPortalAp;
    private boolean mIsPrimaryUser;
    private boolean mIsRoveOutToDisconn;
    private boolean mIsScanedRssiLow;
    private boolean mIsScanedRssiMiddle;
    private boolean mIsUserHandoverWiFi;
    private boolean mIsUserManualConnectAp;
    private volatile boolean mIsVpnWorking;
    private boolean mIsWiFiNoInternet;
    private boolean mIsWiFiProAutoEvaluateAP;
    private boolean mIsWiFiProConnected;
    private boolean mIsWiFiProEnabled;
    private boolean mIsWifiSemiAutoEvaluateComplete;
    private int mLastCSPState;
    private String mLastConnect5GAP;
    private int mLastWifiLevel;
    private boolean mLoseInetRoveOut;
    private boolean mNeedRetryMonitor;
    private NetworkBlackListManager mNetworkBlackListManager;
    private NetworkQosMonitor mNetworkQosMonitor;
    private String mNewSelect_bssid;
    private int mOpenAvailableAPCounter;
    private PowerManager mPowerManager;
    private String mRoSsid;
    private boolean mRoveOutStarted;
    private SampleCollectionManager mSampleCollectionManager;
    private List<ScanResult> mScanResultList;
    private TelephonyManager mTelephonyManager;
    private WiFiLinkMonitorState mWiFiLinkMonitorState;
    private int mWiFiNoInternetReason;
    private WifiProCHRManager mWiFiProCHRMgr;
    private WiFiProDisabledState mWiFiProDisabledState;
    private WiFiProEnableState mWiFiProEnableState;
    private WiFiProEvaluateController mWiFiProEvaluateController;
    private volatile int mWiFiProPdpSwichValue;
    private WiFiProPortalController mWiFiProProtalController;
    private WiFiProVerfyingLinkState mWiFiProVerfyingLinkState;
    private WifiConnectedState mWifiConnectedState;
    private WifiDisConnectedState mWifiDisConnectedState;
    private WifiHandover mWifiHandover;
    private WifiManager mWifiManager;
    private WifiProConfigStore mWifiProConfigStore;
    private WifiProConfigurationManager mWifiProConfigurationManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private WifiProUIDisplayManager mWifiProUIDisplayManager;
    private WifiSemiAutoEvaluateState mWifiSemiAutoEvaluateState;
    private WifiSemiAutoScoreState mWifiSemiAutoScoreState;
    private int mWifiTcpRxCount;
    private int mWifiToWifiType;
    private AsyncChannel mWsmChannel;

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.10 */
    class AnonymousClass10 extends ContentObserver {
        AnonymousClass10(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mIsManualConnectedWiFi = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.logD("mIsManualConnectedWiFi has change:  " + WifiProStateMachine.this.mIsManualConnectedWiFi + ", wifipro state = " + WifiProStateMachine.this.getCurrentState().getName());
            if (WifiProStateMachine.this.mIsManualConnectedWiFi) {
                WifiProStateMachine.this.mIsUserManualConnectAp = WifiProStateMachine.DBG;
            }
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            if (WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, "device_provisioned", WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG) == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateInitialWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
            }
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.4 */
    class AnonymousClass4 extends ContentObserver {
        AnonymousClass4(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mIsWiFiProEnabled = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_SMART_NETWORK_SWITCHING, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.logD("Wifi pro setting has changed,WiFiProEnabled == " + WifiProStateMachine.this.mIsWiFiProEnabled);
            if (WifiProStateMachine.isWifiEvaluating() && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine.this.restoreWiFiConfig();
                WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            }
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.5 */
    class AnonymousClass5 extends ContentObserver {
        AnonymousClass5(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mIsMobileDataEnabled = WifiProStateMachine.getSettingsGlobalBoolean(WifiProStateMachine.this.mContentResolver, "mobile_data", WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION);
            WifiProStateMachine.this.logD("MobileData has changed,isMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.6 */
    class AnonymousClass6 extends ContentObserver {
        AnonymousClass6(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mEmuiPdpSwichValue = WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_EMUI_WIFI_TO_PDP, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE);
            WifiProStateMachine.this.mWiFiProPdpSwichValue = WifiProStateMachine.this.mEmuiPdpSwichValue;
            if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseSelCspSettingChgCount(WifiProStateMachine.this.mWiFiProPdpSwichValue);
            }
            WifiProStateMachine.this.logD("Mobile PDP setting changed, mWiFiProPdpSwichValue = mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.7 */
    class AnonymousClass7 extends ContentObserver {
        AnonymousClass7(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mIsVpnWorking = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.SETTING_SECURE_VPN_WORK_VALUE, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.logD("vpn state has changed,mIsVpnWorking == " + WifiProStateMachine.this.mIsVpnWorking);
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.8 */
    class AnonymousClass8 extends ContentObserver {
        AnonymousClass8(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mConnectWiFiAppPid = WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.SETTING_SECURE_CONN_WIFI_PID, WifiProStateMachine.INVALID_PID);
            WifiProStateMachine.this.logD("current APP name == " + WifiProStateMachine.this.getAppName(WifiProStateMachine.this.mConnectWiFiAppPid));
        }
    }

    /* renamed from: com.android.server.wifi.wifipro.WifiProStateMachine.9 */
    class AnonymousClass9 extends ContentObserver {
        AnonymousClass9(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiProStateMachine.this.mIsWiFiProAutoEvaluateAP = WifiProStateMachine.getSettingsSecureBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_RECOMMEND_NETWORK, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("DefaultState is Enter");
            WifiProStateMachine.this.defaulVariableInit();
        }

        public void exit() {
            WifiProStateMachine.this.logD("DefaultState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    } else {
                        WifiProStateMachine.this.onDisableWiFiPro();
                    }
                    return WifiProStateMachine.DBG;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
        }
    }

    class WiFiLinkMonitorState extends State {
        private int currWifiPoorlevel;
        private int internetFailureDetectedCnt;
        private boolean isAllowWiFiHandoverMobile;
        private boolean isBQERequestCheckWiFi;
        private boolean isCancelCHRTypeReport;
        private boolean isCheckWiFiForUpdateSetting;
        private boolean isDialogDisplayed;
        private boolean isRssiLevel0Scaned;
        private boolean isRssiLevel1Scaned;
        private boolean isRssiLevel2Scaned;
        private boolean isScreenOffMonitor;
        private boolean isSwitching;
        private boolean isToastDisplayed;
        private boolean isWiFiHandoverPriority;
        private boolean isWifi2MobileUIShowing;
        private boolean isWifi2WifiProcess;
        private int tcpRxStatisticsTimer;
        private long wifiLinkHoldTime;
        private int wifiMonitorCounter;

        WiFiLinkMonitorState() {
        }

        private void wiFiLinkMonitorStateInit() {
            WifiProStateMachine.this.logD("wiFiLinkMonitorStateInit is Start");
            WifiProStateMachine.this.mBadBssid = null;
            this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isWifi2WifiProcess = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isWifi2MobileUIShowing = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isDialogDisplayed = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.setWifiCSPState(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.logD("mIsWiFiNoInternet is true,sendMessage wifi Qos is -1");
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, WifiProStateMachine.INVALID_PID);
            } else {
                WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiLinkMonitorState is Enter");
            NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            if (wifi_info != null && wifi_info.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logD(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
            }
            this.wifiMonitorCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            this.tcpRxStatisticsTimer = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            this.internetFailureDetectedCnt = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            this.isScreenOffMonitor = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isAllowWiFiHandoverMobile = WifiProStateMachine.DBG;
            this.isCancelCHRTypeReport = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isRssiLevel2Scaned = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isRssiLevel1Scaned = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isRssiLevel0Scaned = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            wiFiLinkMonitorStateInit();
            this.wifiLinkHoldTime = System.currentTimeMillis();
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime && (WifiProStateMachine.this.mChrWifiDisconnectStartTime > WifiProStateMachine.this.mChrRoveOutStartTime || WifiProStateMachine.this.mChrWifiDidableStartTime > WifiProStateMachine.this.mChrRoveOutStartTime)) {
                long disableRestoreTime = System.currentTimeMillis() - WifiProStateMachine.this.mChrWifiDidableStartTime;
                boolean ssidIsSame = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                if (!(WifiProStateMachine.this.mRoSsid == null || WifiProStateMachine.this.mCurrentSsid == null)) {
                    ssidIsSame = WifiProStateMachine.this.mRoSsid.equals(WifiProStateMachine.this.mCurrentSsid);
                }
                if (ssidIsSame && disableRestoreTime <= 30000) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserReopenWifiRiCount();
                }
            }
            WifiProStateMachine.this.mChrRoveOutStartTime = 0;
            WifiProStateMachine.this.mChrWifiDisconnectStartTime = 0;
            WifiProStateMachine.this.mChrWifiDidableStartTime = 0;
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiLinkMonitorState is Exit");
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY);
            WifiProStateMachine.this.stopDualBandMonitor();
            this.isToastDisplayed = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isDialogDisplayed = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mIsPoorRssiRequestCheckWiFi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isWiFiHandoverPriority = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            if (System.currentTimeMillis() - this.wifiLinkHoldTime > 1800000) {
                WifiProStateMachine.this.mCurrentVerfyCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message msg) {
            int i;
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || DetailedState.VERIFYING_POOR_LINK != networkInfo.getDetailedState()) {
                        if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                            WifiProStateMachine.this.logD("wifi has disconnected,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                            if (this.isWifi2WifiProcess) {
                                break;
                            }
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi handover mobile is Complete!");
                    this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProVerfyingLinkState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                    if (!this.isScreenOffMonitor) {
                        WifiProStateMachine.this.logD("device screen on,but isScreenOffMonitor is false");
                        break;
                    }
                    WifiProStateMachine.this.logD("device screen on,reinitialize wifi monitor");
                    this.isScreenOffMonitor = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    this.wifiMonitorCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
                    break;
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                    if (!pendingMsgBySelfCureEngine(msg.arg1)) {
                        if (!handleMsgBySwitchOrDialogStatus(msg.arg1)) {
                            WifiProStateMachine.this.logD("WiFiLinkMonitorState receive wifi Qos currWifiPoorlevel == " + msg.arg1);
                            if (-103 != msg.arg1) {
                                if (-104 != msg.arg1) {
                                    if (!this.isCheckWiFiForUpdateSetting) {
                                        i = msg.arg1;
                                        if (r0 <= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                            this.currWifiPoorlevel = msg.arg1;
                                            WifiProStateMachine.this.logD("currWifiPoorlevel == " + this.currWifiPoorlevel);
                                            i = this.currWifiPoorlevel;
                                            if (r0 == WifiProStateMachine.INVALID_PID) {
                                                WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                                            }
                                            WifiProStateMachine.this.mWifiToWifiType = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                                            i = this.currWifiPoorlevel;
                                            if (r0 != -2) {
                                                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                                                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                    WifiProStateMachine.this.mWifiToWifiType = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                                                }
                                                this.isSwitching = WifiProStateMachine.DBG;
                                                WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
                                                WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
                                                WifiProStateMachine.this.logW("WiFiLinkMonitorState : try wifi --> wifi");
                                                this.isWiFiHandoverPriority = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                                                this.isWifi2WifiProcess = WifiProStateMachine.DBG;
                                                if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG)) {
                                                    wifi2WifiFailed();
                                                    break;
                                                }
                                            }
                                            WifiProStateMachine.this.refreshConnectedNetWork();
                                            tryWifiHandoverPreferentially(WifiProStateMachine.this.mCurrentRssi);
                                            break;
                                        }
                                    }
                                    if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                                        WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                                        break;
                                    }
                                }
                                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                                WifiProStateMachine.this.mIsPoorRssiRequestCheckWiFi = WifiProStateMachine.DBG;
                                this.isBQERequestCheckWiFi = WifiProStateMachine.DBG;
                                break;
                            }
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            WifiProStateMachine.this.mIsPoorRssiRequestCheckWiFi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            this.isBQERequestCheckWiFi = WifiProStateMachine.DBG;
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    if (!WifiProStateMachine.this.isFullscreen()) {
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            if (!(!((Boolean) msg.obj).booleanValue() || this.isWiFiHandoverPriority || this.isWifi2WifiProcess)) {
                                if (!WifiProStateMachine.this.mNetworkQosMonitor.isHighDataFlowModel()) {
                                    if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                                        i = WifiProStateMachine.this.mCurrentWifiConfig.networkQosLevel;
                                        break;
                                    }
                                    int curRssiLevel = WifiProStateMachine.this.mWiFiProEvaluateController.calculateSignalLevelHW(WifiProStateMachine.this.mCurrentRssi);
                                    if (curRssiLevel < WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                                        int targetRssiLevel = WifiProStateMachine.this.mWiFiProEvaluateController.calculateSignalLevelHW(Integer.valueOf(msg.arg1).intValue());
                                        WifiProStateMachine.this.logD("curRssiLevel = " + curRssiLevel + ", targetRssiLevel " + targetRssiLevel);
                                        if (targetRssiLevel - curRssiLevel >= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                            tryWifiHandoverPreferentially(WifiProStateMachine.this.mCurrentRssi);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /*136178*/:
                    WifiProStateMachine.this.logD("receive wifi handover wifi Result,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                    if (this.isWifi2WifiProcess) {
                        if (!((Boolean) msg.obj).booleanValue()) {
                            wifi2WifiFailed();
                            break;
                        }
                        WifiProStateMachine.this.logD(" wifi --> wifi is  succeed");
                        this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mBadSsid);
                        WifiProStateMachine.this.addDualBandBlackList(WifiProStateMachine.this.mBadSsid);
                        WifiProStateMachine.this.mIsWiFiProConnected = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.refreshConnectedNetWork();
                        WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                        WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE /*136179*/:
                    WifiProStateMachine.this.mCurrentRssi = msg.obj.getIntExtra("newRssi", WifiHandover.INVALID_RSSI);
                    if (!WifiProStateMachine.this.isFullscreen()) {
                        int rssilevel = WifiProStateMachine.this.mWiFiProEvaluateController.calculateSignalLevelHW(WifiProStateMachine.this.mCurrentRssi);
                        if (rssilevel < WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                            if (!(WifiProStateMachine.this.mIsWiFiNoInternet || ((this.isRssiLevel2Scaned && this.isRssiLevel1Scaned && this.isRssiLevel0Scaned) || this.isWiFiHandoverPriority || this.isWifi2WifiProcess))) {
                                if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                                    if (!WifiProStateMachine.this.isSettingsActivity()) {
                                        if (rssilevel != WifiProStateMachine.WIFI_HANDOVER_TIMERS || this.isRssiLevel2Scaned) {
                                            if (rssilevel != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER || this.isRssiLevel1Scaned) {
                                                if (rssilevel == 0 && !this.isRssiLevel0Scaned) {
                                                    WifiProStateMachine.this.logD("rssilevel = " + rssilevel + ", isRssiLevel0Scaned " + this.isRssiLevel0Scaned);
                                                    this.isRssiLevel0Scaned = WifiProStateMachine.DBG;
                                                    WifiProStateMachine.this.mWifiManager.startScan();
                                                    break;
                                                }
                                            }
                                            WifiProStateMachine.this.logD("rssilevel = " + rssilevel + ", isRssiLevel1Scaned " + this.isRssiLevel1Scaned);
                                            this.isRssiLevel1Scaned = WifiProStateMachine.DBG;
                                            WifiProStateMachine.this.mWifiManager.startScan();
                                            break;
                                        }
                                        this.isRssiLevel2Scaned = WifiProStateMachine.DBG;
                                        WifiProStateMachine.this.logD("rssilevel = " + rssilevel + ", isRssiLevel2Scaned " + this.isRssiLevel2Scaned);
                                        WifiProStateMachine.this.mWifiManager.startScan();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT /*136180*/:
                    tryWifi2Mobile(msg.arg1);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    int wifi_internet_level = msg.arg1;
                    WifiProStateMachine.this.logD("WiFiLinkMonitorState : wifi_internet_level = " + wifi_internet_level);
                    if (WifiProStateMachine.INVALID_PID == msg.arg1 || 6 == msg.arg1) {
                        WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                        this.currWifiPoorlevel = WifiProStateMachine.INVALID_PID;
                        wifi_internet_level = this.currWifiPoorlevel;
                        if (this.isBQERequestCheckWiFi) {
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        }
                        if (this.isCheckWiFiForUpdateSetting) {
                            WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        }
                    } else {
                        WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                        WifiProStateMachine.this.reSetWifiInternetState();
                    }
                    this.isBQERequestCheckWiFi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, wifi_internet_level);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                    if (this.isWifi2MobileUIShowing) {
                        this.isDialogDisplayed = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        this.isWifi2MobileUIShowing = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.mWiFiProPdpSwichValue = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                        WifiProStateMachine.this.setWifiCSPState(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                        WifiProStateMachine.this.logD("Click OK ,is send message to wifi handover mobile ,WiFiProPdp is AUTO");
                        if (WifiProStateMachine.this.mIsMobileDataEnabled) {
                            if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                                if (WifiProStateMachine.this.mEmuiPdpSwichValue != WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                    int roReason;
                                    this.isAllowWiFiHandoverMobile = WifiProStateMachine.DBG;
                                    WifiProStateMachine.this.logD("mWsmChannel send Poor Link Detected");
                                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetWorkHandover(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.POOR_LINK_DETECTED);
                                    i = this.currWifiPoorlevel;
                                    if (r0 == WifiProStateMachine.INVALID_PID) {
                                        roReason = WifiProStateMachine.WIFI_HANDOVER_TIMERS;
                                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetHandoverCount();
                                    } else {
                                        roReason = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                                    }
                                    WifiProStateMachine.this.logD("roReason = " + roReason);
                                    WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveOutEvent(roReason);
                                }
                            }
                        }
                        WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                    if (this.isWifi2MobileUIShowing) {
                        WifiProStateMachine.this.logD("isDialogDisplayed : " + this.isDialogDisplayed + ", mIsWiFiNoInternet " + WifiProStateMachine.this.mIsWiFiNoInternet);
                        if (this.isDialogDisplayed) {
                            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
                            } else {
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(WifiProStateMachine.WIFI_HANDOVER_TIMERS);
                            }
                        } else {
                            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                            } else {
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseBQE_BadSettingCancelCount();
                            }
                        }
                        this.isDialogDisplayed = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        this.isWifi2MobileUIShowing = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        this.isAllowWiFiHandoverMobile = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                        this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.mWiFiProPdpSwichValue = WifiProStateMachine.WIFI_HANDOVER_TIMERS;
                        WifiProStateMachine.this.logD("Click Cancel ,is not allow wifi handover mobile, WiFiProPdp is CANNOT");
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                            break;
                        }
                        this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                            if (WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess) {
                                if (WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                                    WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                    }
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                    break;
                                }
                            }
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR /*136184*/:
                    WifiProStateMachine.this.logD("ReIniitalize,ScreenOn == " + WifiProStateMachine.this.mPowerManager.isScreenOn());
                    if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                        this.isScreenOffMonitor = WifiProStateMachine.DBG;
                        break;
                    }
                    this.wifiMonitorCounter += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                    i = this.wifiMonitorCounter;
                    if (r0 >= WifiProStateMachine.WIFI_SCAN_COUNT) {
                        this.wifiMonitorCounter = Math.min(this.wifiMonitorCounter, WifiProStateMachine.WIFI_SCAN_INTERVAL_MAX);
                        long delay_time = (((long) Math.pow(2.0d, (double) (this.wifiMonitorCounter / WifiProStateMachine.WIFI_SCAN_COUNT))) * 60) * 1000;
                        WifiProStateMachine.this.logD("delay_time = " + delay_time);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delay_time);
                        if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isCheckWiFiForUpdateSetting) {
                            this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DBG;
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        }
                        if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                            if (WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess) {
                                if (WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                                    WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                    }
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                }
                            }
                        }
                    } else {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                    }
                    WifiProStateMachine.this.logD("wifiMonitorCounter = " + this.wifiMonitorCounter);
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    WifiProStateMachine.this.logD("WiFiLinkMonitorState : Receive Mobile changed");
                    if (WifiProStateMachine.this.isMobileDataConnected() && this.isAllowWiFiHandoverMobile) {
                        this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                            break;
                        }
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        break;
                    }
                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /*136190*/:
                    if (WifiProStateMachine.this.mEmuiPdpSwichValue != WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                        this.isAllowWiFiHandoverMobile = WifiProStateMachine.DBG;
                        this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                            break;
                        }
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /*136191*/:
                    WifiProStateMachine.this.logD("receive : EVENT_RETRY_WIFI_TO_WIFI");
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    }
                    WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    wiFiLinkMonitorStateInit();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && (this.isCheckWiFiForUpdateSetting || this.isDialogDisplayed)) {
                        WifiProStateMachine.this.logD("queryNetworkQos for wifi , isCheckWiFiForUpdateSetting =" + this.isCheckWiFiForUpdateSetting + ", isDialogDisplayed =" + this.isDialogDisplayed);
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        break;
                    }
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /*136195*/:
                    if (msg.obj != null) {
                        if (((Boolean) msg.obj).booleanValue()) {
                            this.internetFailureDetectedCnt = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                            break;
                        }
                    }
                    if (msg.obj != null) {
                        if (!((Boolean) msg.obj).booleanValue()) {
                            WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT = false, SCE force to request wifi switch.");
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /*136299*/:
                    int rttlevel = msg.arg1;
                    WifiProStateMachine.this.logD(WifiProStateMachine.this.mCurrentSsid + "  TCPRTT  level = " + rttlevel);
                    if (rttlevel <= 0 || rttlevel > WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                        rttlevel = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(WifiProStateMachine.this.mCurrentSsid);
                    }
                    updateWifiQosLevel(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED, rttlevel);
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        int accessType;
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                            accessType = WifiProStateMachine.WIFI_SCAN_COUNT;
                        } else {
                            accessType = WifiProStateMachine.WIFI_HANDOVER_TIMERS;
                        }
                        if (WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, accessType)) {
                            WifiProStateMachine.this.logD("mCurrentSsid   = " + WifiProStateMachine.this.mCurrentSsid + ", updateScoreInfoType  " + accessType);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, accessType, WifiProStateMachine.this.mCurrentSsid);
                        }
                        if (accessType == WifiProStateMachine.WIFI_SCAN_COUNT) {
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /*136311*/:
                    int currentWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    WifiProStateMachine.this.logD("current rx= " + currentWifiTcpRxCount + ", last rx = " + WifiProStateMachine.this.mWifiTcpRxCount);
                    if (currentWifiTcpRxCount - WifiProStateMachine.this.mWifiTcpRxCount <= 0) {
                        WifiProStateMachine.this.logD("tcpRxStatisticsTimer = " + this.tcpRxStatisticsTimer + ", % 10 = " + (this.tcpRxStatisticsTimer % 10));
                        if (this.tcpRxStatisticsTimer > 0) {
                            if (this.tcpRxStatisticsTimer % 10 == 0) {
                                this.tcpRxStatisticsTimer = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                                break;
                            }
                        }
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        this.tcpRxStatisticsTimer += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        break;
                    }
                    this.tcpRxStatisticsTimer = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_RSSITH_RESULT /*136368*/:
                    if (!this.isWifi2WifiProcess) {
                        WifiProEstimateApInfo apInfo = msg.obj;
                        if (WifiProStateMachine.this.mDualBandMonitorInfoSize > 0) {
                            WifiProStateMachine.this.mDualBandMonitorInfoSize = WifiProStateMachine.this.mDualBandMonitorInfoSize + WifiProStateMachine.INVALID_PID;
                            WifiProStateMachine.this.updateDualBandMonitorInfo(apInfo);
                        }
                        if (WifiProStateMachine.this.mDualBandMonitorInfoSize == 0) {
                            WifiProStateMachine.this.mDualBandMonitorStart = WifiProStateMachine.DBG;
                            WifiProStateMachine.this.logD("Start dual band Manager monitor");
                            WifiProStateMachine.this.mDualBandManager.startMonitor(WifiProStateMachine.this.mDualBandMonitorApList);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("isWifi2WifiProcess is true, ignore this message");
                    WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DBG;
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_SCORE_RESULT /*136369*/:
                    if (!this.isWifi2WifiProcess) {
                        WifiProEstimateApInfo estimateApInfo = msg.obj;
                        WifiProStateMachine.this.logD("EVENT_DUALBAND_SCORE_RESULT estimateApInfo: " + estimateApInfo.toString());
                        if (WifiProStateMachine.this.mDualBandEstimateInfoSize > 0) {
                            WifiProStateMachine.this.mDualBandEstimateInfoSize = WifiProStateMachine.this.mDualBandEstimateInfoSize + WifiProStateMachine.INVALID_PID;
                            WifiProStateMachine.this.updateDualBandEstimateInfo(estimateApInfo);
                        }
                        WifiProStateMachine.this.logD("mDualBandEstimateInfoSize = " + WifiProStateMachine.this.mDualBandEstimateInfoSize);
                        if (WifiProStateMachine.this.mDualBandEstimateInfoSize == 0) {
                            WifiProStateMachine.this.chooseAvalibleDualBandAp();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("isWifi2WifiProcess is true, ignore this message");
                    WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DBG;
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_5GAP_AVAILABLE /*136370*/:
                    WifiProStateMachine.this.logD("tbc receive EVENT_DUALBAND_5GAP_AVAILABLE isSwitching = " + this.isSwitching);
                    if (!this.isSwitching) {
                        this.isSwitching = WifiProStateMachine.DBG;
                        this.isWifi2WifiProcess = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
                        WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
                        WifiProStateMachine.this.logD("do dual band wifi handover, mCurrentBssid:" + WifiProStateMachine.this.mCurrentBssid + ", mCurrentSsid:" + WifiProStateMachine.this.mCurrentSsid + ", mAvailable5GAPBssid = " + WifiProStateMachine.this.mAvailable5GAPBssid + ", mAvailable5GAPSsid =" + WifiProStateMachine.this.mAvailable5GAPSsid + ", mDuanBandHandoverType = " + WifiProStateMachine.this.mDuanBandHandoverType);
                        int switchType = WifiProStateMachine.this.mDuanBandHandoverType == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER ? WifiProStateMachine.WIFI_HANDOVER_TIMERS : WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                        WifiProStateMachine.this.logD("do dual band wifi handover, switchType = " + switchType);
                        if (!WifiProStateMachine.this.isFullscreen() || switchType != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                            if (!WifiProStateMachine.this.mWifiHandover.handleDualBandWifiConnect(WifiProStateMachine.this.mAvailable5GAPBssid, WifiProStateMachine.this.mAvailable5GAPSsid, WifiProStateMachine.this.mAvailable5GAPAuthType, switchType)) {
                                dualBandhandoverFailed(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("keep in current AP,now is in full screen and switch by hardhandover");
                        break;
                    }
                    WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DBG;
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_WIFI_HANDOVER_RESULT /*136371*/:
                    WifiProStateMachine.this.logD("receive dual band wifi handover resust");
                    if (this.isWifi2WifiProcess) {
                        int errorReason = msg.arg1;
                        if (errorReason != 0) {
                            WifiProStateMachine.this.logD("dual band wifi handover is  failure");
                            dualBandhandoverFailed(errorReason);
                            break;
                        }
                        WifiProStateMachine.this.logD("dual band wifi handover is  succeed, ssid =" + WifiProStateMachine.this.mNewSelect_bssid);
                        this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.mDualBandConnectAPSsid = WifiProStateMachine.this.mNewSelect_bssid;
                        WifiProStateMachine.this.mDualBandConnectTime = System.currentTimeMillis();
                        WifiProStateMachine.this.reportPingPangCHR(WifiProStateMachine.this.mDualBandConnectAPSsid);
                        WifiProStateMachine.this.reportDualbandStatisticsCHR(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY /*136372*/:
                    WifiProStateMachine.this.logD("receive dual band wifi handover delay retry");
                    WifiProStateMachine.this.retryDualBandAPMonitor();
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }

        private void tryWifiHandoverPreferentially(int rssi) {
            if (rssi <= WifiProStateMachine.THRESHOD_RSSI_HIGH) {
                if (rssi < WifiProStateMachine.THRESHOD_RSSI_LOW && !WifiProStateMachine.this.mIsScanedRssiLow) {
                    WifiProStateMachine.this.mIsScanedRssiLow = WifiProStateMachine.DBG;
                } else if (rssi >= WifiProStateMachine.THRESHOD_RSSI_LOW && !WifiProStateMachine.this.mIsScanedRssiMiddle) {
                    WifiProStateMachine.this.mIsScanedRssiMiddle = WifiProStateMachine.DBG;
                } else {
                    return;
                }
                this.isSwitching = WifiProStateMachine.DBG;
                this.isWiFiHandoverPriority = WifiProStateMachine.DBG;
                this.isWifi2WifiProcess = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
                WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
                WifiProStateMachine.this.logW("try wifi --> wifi Preferentially ,current rssi = " + rssi);
                if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), Math.max(WifiProStateMachine.THRESHOD_RSSI, rssi + 10), WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT, Boolean.valueOf(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED));
                }
            }
        }

        private void tryWifi2Mobile(int mobile_level) {
            WifiProStateMachine.this.logD("Receive mobile QOS  mobile_level = " + mobile_level + ", isSwitching =" + this.isSwitching);
            if (!this.isWifi2WifiProcess && WifiProStateMachine.this.isAllowWifi2Mobile() && this.isAllowWiFiHandoverMobile) {
                if (!WifiProStateMachine.this.isWiFiPoorer(this.currWifiPoorlevel, mobile_level)) {
                    WifiProStateMachine.this.logD("mobile is poorer,continue monitor");
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                        this.isToastDisplayed = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                } else if (this.isSwitching && WifiProStateMachine.this.isAllowWifi2Mobile()) {
                    WifiProStateMachine.this.logD("mobile is better than wifi,and ScreenOn, try wifi --> mobile,show Dialog mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ", mIsWiFiNoInternet =" + WifiProStateMachine.this.mIsWiFiNoInternet);
                    if (this.isWifi2MobileUIShowing) {
                        WifiProStateMachine.this.logD("isWifi2MobileUIShowing = true, not dispaly " + this.isWifi2MobileUIShowing);
                        return;
                    }
                    this.isWifi2MobileUIShowing = WifiProStateMachine.DBG;
                    if (WifiProStateMachine.this.isPdpAvailable()) {
                        WifiProStateMachine.this.logD("mobile is cmcc and wifi pdp, mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + " ,mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
                        switch (WifiProStateMachine.this.mEmuiPdpSwichValue) {
                            case WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG /*0*/:
                                if (WifiProStateMachine.this.mWiFiProPdpSwichValue != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                                    if (WifiProStateMachine.this.mWiFiProPdpSwichValue != WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                        int dialogId;
                                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                            dialogId = WifiProStateMachine.WIFI_HANDOVER_TIMERS;
                                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                        } else {
                                            dialogId = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                                        }
                                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProDialog(dialogId);
                                        this.isDialogDisplayed = WifiProStateMachine.DBG;
                                        break;
                                    }
                                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
                                    break;
                                }
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                                break;
                            case WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER /*1*/:
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                                break;
                            case WifiProStateMachine.WIFI_HANDOVER_TIMERS /*2*/:
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
                                break;
                            default:
                                break;
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                } else {
                    WifiProStateMachine.this.logW("no handover,DELAY Transit to Monitor");
                    this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                }
                return;
            }
            WifiProStateMachine.this.logD("isWifi2WifiProcess = " + this.isWifi2WifiProcess + ", isAllowWifi2Mobile = " + WifiProStateMachine.this.isAllowWifi2Mobile() + ", mIsAllowWiFiHandoverMobile = " + this.isAllowWiFiHandoverMobile);
        }

        private void wifi2WifiFailed() {
            if (!(WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadSsid))) {
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
            }
            WifiProStateMachine.this.logD("wifi to Wifi Failed Finally!");
            this.isWifi2WifiProcess = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            if (WifiProStateMachine.this.isWifiConnected()) {
                if (WifiProStateMachine.this.mNeedRetryMonitor) {
                    WifiProStateMachine.this.logD("need retry dualband handover monitor");
                    WifiProStateMachine.this.retryDualBandAPMonitor();
                    WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(WifiProStateMachine.DBG);
                }
                if (this.isWiFiHandoverPriority) {
                    WifiProStateMachine.this.logD("wifi handover wifi failed,continue monitor wifi Qos");
                    this.isWiFiHandoverPriority = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    return;
                }
                WifiProStateMachine.this.logD("wifi --> wifi is Failure, but wifi is connected, isMobileDataConnected() = " + WifiProStateMachine.this.isMobileDataConnected() + ",isAllowWiFiHandoverMobile =  " + this.isAllowWiFiHandoverMobile + " , mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ",mPowerManager.isScreenOn =" + WifiProStateMachine.this.mPowerManager.isScreenOn() + ", currWifiPoorlevel = " + this.currWifiPoorlevel);
                if (WifiProStateMachine.this.isAllowWifi2Mobile()) {
                    WifiProStateMachine.this.logD("try to wifi --> mobile,Query mobile Qos");
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    return;
                }
                WifiProStateMachine.this.logD("wifi --> wifi is Failure,and can not handover to mobile ,delay 30s go to Monitor");
                if (WifiProStateMachine.this.isMobileDataConnected() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue == WifiProStateMachine.WIFI_HANDOVER_TIMERS && !this.isCancelCHRTypeReport) {
                    this.isCancelCHRTypeReport = WifiProStateMachine.DBG;
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.logD("call increaseNotInetSettingCancelCount.");
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                    } else {
                        WifiProStateMachine.this.logD("call increaseBQE_BadSettingCancelCount.");
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseBQE_BadSettingCancelCount();
                    }
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                    this.isToastDisplayed = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                }
                WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                    WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                }
            } else {
                WifiProStateMachine.this.logD("wifi handover over Failed and system auto conning ap");
                if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logD("try to connect : " + WifiProStateMachine.this.mBadSsid);
                    WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
                }
            }
        }

        private void dualBandhandoverFailed(int reason) {
            if (!(WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadBssid))) {
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
                WifiProStateMachine.this.mHwDualBandBlackListMgr.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            }
            WifiProStateMachine.this.logD("dual band handover failed. reason = " + reason);
            if (reason == -7 && WifiProStateMachine.this.mAvailable5GAPBssid != null) {
                WifiProStateMachine.this.logD("dualBandhandoverFailed  mAvailable5GAPBssid = " + WifiProStateMachine.this.mAvailable5GAPBssid);
                WifiProDualBandApInfoRcd mRecrd = HwDualBandInformationManager.getInstance().getDualBandAPInfo(WifiProStateMachine.this.mAvailable5GAPBssid);
                if (mRecrd != null) {
                    mRecrd.isInBlackList = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                    HwDualBandInformationManager.getInstance().updateAPInfo(mRecrd);
                }
            }
            this.isWifi2WifiProcess = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            if (WifiProStateMachine.this.isWifiConnected()) {
                this.isSwitching = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                WifiProStateMachine.this.retryDualBandAPMonitor();
            } else {
                WifiProStateMachine.this.logD("wifi dual band handover over Failed and system auto connecting ap");
                WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
            }
            WifiProStateMachine.this.reportDualbandStatisticsCHR(WifiProStateMachine.WIFI_HANDOVER_TIMERS);
        }

        private void updateWifiQosLevel(boolean isWiFiNoInternet, int qosLevel) {
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            WifiProStateMachine.this.logD("updateWifiQosLevel, mCurrentSsid: " + WifiProStateMachine.this.mCurrentSsid + " ,isWiFiNoInternet: " + isWiFiNoInternet + ", qosLevel: " + qosLevel);
            if (isWiFiNoInternet) {
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_HANDOVER_TIMERS);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.WIFI_HANDOVER_TIMERS, WifiProStateMachine.this.mCurrentSsid);
                return;
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_SCAN_COUNT);
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, qosLevel);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_SCAN_COUNT, qosLevel, (int) WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }

        private boolean handleMsgBySwitchOrDialogStatus(int level) {
            if (!this.isSwitching || !this.isDialogDisplayed) {
                return this.isSwitching ? WifiProStateMachine.DBG : WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            } else {
                if (level > WifiProStateMachine.WIFI_HANDOVER_TIMERS && level != 6) {
                    WifiProStateMachine.this.logD("Dialog is  Displayed, Qos is" + level + ", Cancel dialog.");
                    WifiProStateMachine.this.updateWifiInternetStateChange(level);
                    WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                    WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    wiFiLinkMonitorStateInit();
                }
                return WifiProStateMachine.DBG;
            }
        }

        private boolean pendingMsgBySelfCureEngine(int level) {
            if (level == WifiproUtils.REQUEST_WIFI_INET_CHECK && !WifiProCommonUtils.isOpenType(WifiProStateMachine.this.mCurrentWifiConfig)) {
                this.internetFailureDetectedCnt += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                if (HwSelfCureEngine.getInstance().isSelfCureOngoing()) {
                    WifiProStateMachine.this.logD("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of wifi self cure ongoing.");
                    return WifiProStateMachine.DBG;
                } else if (this.internetFailureDetectedCnt == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER && !HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(WifiProStateMachine.this.mContext) && WifiProStateMachine.this.mCurrentRssi >= -75) {
                    HwSelfCureEngine.getInstance().notifyInternetFailureDetected(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                    WifiProStateMachine.this.logD("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of requesting self cure.");
                    return WifiProStateMachine.DBG;
                }
            }
            return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
        }
    }

    class WiFiProDisabledState extends State {
        WiFiProDisabledState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProDisabledState is Enter");
            WifiProStateMachine.mIsWifiManualEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.unRegisterCallBack();
            WifiProStateMachine.this.mSampleCollectionManager.unRegisterBroadcastReceiver();
            WifiProStateMachine.this.mWiFiProProtalController.handleWifiProStatusChanged(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED, WifiProStateMachine.this.mIsPortalAp);
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
            WifiProStateMachine.this.mHwIntelligenceWiFiManager.stop();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.stopDualBandManager();
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("WiFiProDisabledState , wifi is connect ");
                WifiInfo cInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                    WifiProStateMachine.this.logD("wifi State == VERIFYING_POOR_LINK");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                }
                WifiProStateMachine.this.setWifiCSPState(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            }
            WifiProStateMachine.this.resetVariables();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProDisabledState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.POOR_LINK_DETECTED /*131873*/:
                    WifiProStateMachine.this.logD("receive POOR_LINK_DETECTED sendMessageDelayed");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                    break;
                case WifiProStateMachine.GOOD_LINK_DETECTED /*131874*/:
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || DetailedState.VERIFYING_POOR_LINK != networkInfo.getDetailedState()) {
                        if (networkInfo == null || NetworkInfo.State.CONNECTING != networkInfo.getState()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                                WifiProStateMachine.this.setWifiCSPState(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                                break;
                            }
                        }
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        break;
                    }
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() == WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = msg.obj;
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra(MessageUtil.EXTRA_WIFI_CONFIGURATION);
                    if (conn_cfg != null) {
                        int change_reason = confg_intent.getIntExtra(MessageUtil.EXTRA_CHANGE_REASON, WifiProStateMachine.INVALID_PID);
                        if (conn_cfg.isTempCreated && change_reason != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                            WifiProStateMachine.this.logD("WiFiProDisabledState, forget " + conn_cfg.SSID);
                            WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            break;
                        }
                    }
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }
    }

    class WiFiProEnableState extends State {
        WiFiProEnableState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProEnableState is Enter");
            WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mWiFiProPdpSwichValue = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            WifiProStateMachine.this.registerCallBack();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(WifiProStateMachine.DBG);
            WifiProStateMachine.mIsWifiManualEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            if (WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine.this.startDualBandManager();
                WifiProStateMachine.this.mHwIntelligenceWiFiManager.start();
                WifiProStateMachine.this.mSampleCollectionManager.registerBroadcastReceiver();
                WifiProStateMachine.this.mWiFiProProtalController.handleWifiProStatusChanged(WifiProStateMachine.DBG, WifiProStateMachine.this.mIsPortalAp);
            }
            transitionNetState();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProEnableState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                        if (WifiProStateMachine.this.mWifiManager.getWifiState() == WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.mWiFiProEvaluateController.initWifiProEvaluateRecords();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi state is DISABLED, go to wifi disconnected");
                    if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                        WifiProStateMachine.this.logD("BQE bad rove out, wifi disable time recorded.");
                        WifiProStateMachine.this.mChrWifiDidableStartTime = System.currentTimeMillis();
                    }
                    WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    if (!WifiProStateMachine.this.mIsManualConnectedWiFi) {
                        if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mWifiManager.isWifiEnabled()) {
                            NetworkInfo wifi_info = HwServiceFactory.getHwConnectivityManager().getNetworkInfoForWifi();
                            if (wifi_info != null) {
                                WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWifiManager.getScanResults();
                                WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mScanResultList);
                                if (!(WifiProStateMachine.this.mScanResultList == null || WifiProStateMachine.this.mScanResultList.size() == 0)) {
                                    boolean issetting = WifiProStateMachine.this.isSettingsActivity();
                                    int evaluate_type = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                                    if (issetting) {
                                        evaluate_type = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                                    }
                                    if (wifi_info.getDetailedState() == DetailedState.DISCONNECTED) {
                                        if (!WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                                            if (!WifiProStateMachine.this.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList)) {
                                                WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                                break;
                                            }
                                            for (ScanResult scanResult : WifiProStateMachine.this.mScanResultList) {
                                                if (WifiProStateMachine.this.mWiFiProEvaluateController.isAllowEvaluate(scanResult, evaluate_type) && !WifiProStateMachine.this.mWiFiProEvaluateController.isLastEvaluateValid(scanResult, evaluate_type)) {
                                                    WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(scanResult, evaluate_type);
                                                }
                                            }
                                            WifiProStateMachine.this.mWiFiProEvaluateController.orderByRssi();
                                            if (!WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty()) {
                                                WifiProStateMachine.this.mWiFiProEvaluateController.unEvaluateAPQueueDump();
                                                WifiProStateMachine.this.logD("transition to mwifiSemiAutoEvaluateState, to evaluate ap");
                                                if (issetting) {
                                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(WifiProStateMachine.WIFI_HANDOVER_TIMERS);
                                                } else {
                                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                                                }
                                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                                break;
                                            }
                                            WifiProStateMachine.this.logD("UnEvaluateAPRecords is Empty");
                                            break;
                                        }
                                        WifiProStateMachine.this.logD("P2PConnectedOrConnecting, ignor this scan result");
                                        WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                        break;
                                    }
                                    WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                    break;
                                }
                            }
                        }
                    }
                    WifiProStateMachine.this.logD("mIsManualConnectedWiFi, ignor this scan result");
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_EVALUTE_STATE_CHANGE /*136298*/:
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }

        private void transitionNetState() {
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("WiFiProEnableState,go to WifiConnectedState");
                WifiProStateMachine.this.mIsWiFiProConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                return;
            }
            WifiProStateMachine.this.logD("WiFiProEnableState, go to mWifiDisConnectedState");
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
        }
    }

    class WiFiProVerfyingLinkState extends State {
        private volatile boolean isRecoveryWifi;
        private boolean isWifiGoodIntervalTimerOut;
        private volatile boolean isWifiHandoverWifi;
        private boolean isWifiRecoveryTimerOut;
        private boolean isWifiScanScreenOff;
        private int wifiQosLevel;
        private int wifiScanCounter;
        private int wifi_internet_level;

        WiFiProVerfyingLinkState() {
        }

        private void startScanAndMonitor(long time) {
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, 120000);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(WifiProStateMachine.DBG);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(WifiProStateMachine.DBG);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mCurrentWifiLevel = WifiProStateMachine.INVALID_PID;
                WifiProStateMachine.this.logD("WiFiProVerfyingLinkState, wifi is No Internet,delay check time = " + time);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, time);
                return;
            }
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(WifiProStateMachine.WIFI_HANDOVER_TIMERS, WifiProStateMachine.DBG);
        }

        private void cancelScanAndMonitor() {
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(WifiProStateMachine.WIFI_HANDOVER_TIMERS, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
        }

        private void restoreWifiConnect() {
            cancelScanAndMonitor();
            WifiProStateMachine.this.logD("restoreWifiConnect, mWsmChannel send GOOD Link Detected");
            WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetWorkHandover(WifiProStateMachine.WIFI_HANDOVER_TIMERS);
            WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState is Enter");
            this.isRecoveryWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isWifiHandoverWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isWifiGoodIntervalTimerOut = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            this.wifiScanCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            this.isWifiScanScreenOff = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            if (WifiProStateMachine.this.mCurrentVerfyCounter > WifiProStateMachine.WIFI_SCAN_COUNT) {
                WifiProStateMachine.this.mCurrentVerfyCounter = WifiProStateMachine.WIFI_SCAN_COUNT;
            }
            long delay_time = (((long) Math.pow(2.0d, (double) WifiProStateMachine.this.mCurrentVerfyCounter)) * 60) * 1000;
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState : CurrentWifiLevel = " + WifiProStateMachine.this.mCurrentWifiLevel + ", CurrentVerfyCounter = " + WifiProStateMachine.this.mCurrentVerfyCounter + ", delay_time = " + delay_time);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.mCurrentVerfyCounter = wifiProStateMachine.mCurrentVerfyCounter + WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
            startScanAndMonitor(delay_time);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT, delay_time);
            WifiProStateMachine.this.mSampleCollectionManager.notifyWifiDisconnected(WifiProStateMachine.this.mIsPortalAp);
            if (WifiProStateMachine.this.mCurrentVerfyCounter == WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                WifiProStateMachine.this.logW("network has handover 3 times,maybe ping-pong");
                WifiProStateMachine.this.mWifiProStatisticsManager.increasePingPongCount();
            }
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mLoseInetRoveOut = WifiProStateMachine.DBG;
            } else {
                WifiProStateMachine.this.logD("BQE bad rove out started.");
                WifiProStateMachine.this.mChrRoveOutStartTime = System.currentTimeMillis();
                WifiProStateMachine.this.mRoSsid = WifiProStateMachine.this.mCurrentSsid;
                WifiProStateMachine.this.mLoseInetRoveOut = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            WifiProStateMachine.this.mRoveOutStarted = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mIsRoveOutToDisconn = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.reportDualbandVerifyCHR();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState is Exit");
            cancelScanAndMonitor();
            WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isWifiRecoveryTimerOut = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_MOBILE_SWITCH_DELAY);
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    Intent intent = msg.obj;
                    if (!(intent == null || this.isWifiHandoverWifi)) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null) {
                            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState :Network state change " + networkInfo.getDetailedState());
                        }
                        if (networkInfo == null || NetworkInfo.State.DISCONNECTED != networkInfo.getState()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState() && WifiProStateMachine.this.isWifiConnected()) {
                                this.isRecoveryWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(WifiProStateMachine.WIFI_SCAN_COUNT);
                                WifiProStateMachine.this.logD("WiFiProVerfyingLinkState: Restore the wifi connection successful,go to mWiFiLinkMonitorState");
                                WifiProStateMachine.this.mIsWiFiProConnected = WifiProStateMachine.DBG;
                                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("WiFiProVerfyingLinkState : wifi has disconnected");
                        WifiProStateMachine.this.mIsRoveOutToDisconn = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                    if (!this.isWifiScanScreenOff) {
                        WifiProStateMachine.this.logD("isWifiScanScreenOff = false, wait a moment, retry scan wifi later");
                        break;
                    }
                    WifiProStateMachine.this.logD("isWifiScanScreenOff = true, retry scan wifi");
                    this.isWifiScanScreenOff = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    this.wifiScanCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (!(WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser)) {
                        WifiProStateMachine.this.logD("wifiprochr user close wifipro");
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(WifiProStateMachine.HANDOVER_5G_DIFFERENCE_SCORE);
                        WifiProStateMachine.this.mRoveOutStarted = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                    if (!this.isWifiHandoverWifi && this.isWifiRecoveryTimerOut) {
                        this.wifiQosLevel = msg.arg1;
                        if (this.wifiQosLevel > WifiProStateMachine.WIFI_HANDOVER_TIMERS && !this.isRecoveryWifi && !WifiProStateMachine.this.mIsWiFiNoInternet && this.isWifiGoodIntervalTimerOut) {
                            WifiProStateMachine.this.logD("wifi Qos is [" + this.wifiQosLevel + " ]Ok, start check wifi internet");
                            this.isRecoveryWifi = WifiProStateMachine.DBG;
                            this.isWifiGoodIntervalTimerOut = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT, 30000);
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut);
                    break;
                    break;
                case WifiProStateMachine.EVENT_MOBILE_QOS_CHANGE /*136173*/:
                    if (msg.arg1 <= WifiProStateMachine.WIFI_HANDOVER_TIMERS && this.isWifiRecoveryTimerOut) {
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet || msg.arg1 > 0) {
                            if (!(this.isRecoveryWifi || WifiProStateMachine.this.isWiFiPoorer(WifiProStateMachine.this.mCurrentWifiLevel, msg.arg1) || this.isWifiHandoverWifi)) {
                                WifiProStateMachine.this.logD("Mobile Qos is poor,try restore wifi,mobile handover wifi");
                                this.isRecoveryWifi = WifiProStateMachine.DBG;
                                restoreWifiConnect();
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(7);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("both wifi and mobile is unusable,can not restore wifi ");
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    if (!this.isRecoveryWifi && !this.isWifiHandoverWifi) {
                        if (!((Boolean) msg.obj).booleanValue()) {
                            WifiProStateMachine.this.logD("There is no vailble ap, continue verfyinglink");
                            break;
                        }
                        WifiProStateMachine.this.logD("Exist a vailable AP,connect this AP and cancel Sacn Timer");
                        this.isWifiHandoverWifi = WifiProStateMachine.DBG;
                        if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG)) {
                            this.isWifiHandoverWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("receive check available ap result,but is isRecoveryWifi");
                    break;
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    NetworkInfo mobileInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                    WifiProStateMachine.this.logD("networkConnetc change :mobileInfo : " + mobileInfo + ", mIsMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
                    if (WifiProStateMachine.this.mIsMobileDataEnabled && mobileInfo != null && NetworkInfo.State.DISCONNECTED == mobileInfo.getState()) {
                        WifiProStateMachine.this.logD("mobile network service is disconnected, mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
                        NetworkInfo wInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                        if (!(WifiProStateMachine.this.mIsWiFiNoInternet || wInfo == null || DetailedState.VERIFYING_POOR_LINK != wInfo.getDetailedState())) {
                            this.isWifiHandoverWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            restoreWifiConnect();
                            break;
                        }
                    }
                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /*136178*/:
                    this.isWifiHandoverWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    if (!((Boolean) msg.obj).booleanValue()) {
                        if (WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mCurrentSsid)) {
                            if (WifiProStateMachine.this.isWifiConnected()) {
                                WifiProStateMachine.this.logD("wifi handover wifi fail, continue monitor");
                                break;
                            }
                        }
                        WifiProStateMachine.this.logW("connect other AP wifi : Fallure");
                        WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
                        WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mCurrentBssid);
                        break;
                    }
                    WifiProStateMachine.this.logD("connect other AP wifi : succeed ,go to WifiConnectedState");
                    cancelScanAndMonitor();
                    WifiProStateMachine.this.mIsWiFiProConnected = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.refreshConnectedNetWork();
                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    this.wifi_internet_level = msg.arg1;
                    WifiProStateMachine.this.logD("WiFi internet level = " + this.wifi_internet_level + ", wifiQosLevel = " + this.wifiQosLevel);
                    WifiProStateMachine.this.logD("mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + " ,isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut);
                    if (this.isWifiRecoveryTimerOut) {
                        if (!(WifiProStateMachine.this.mIsWiFiNoInternet || this.isWifiHandoverWifi || !this.isRecoveryWifi)) {
                            if (this.wifi_internet_level == WifiProStateMachine.INVALID_PID || this.wifi_internet_level == 6 || this.wifiQosLevel <= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                this.isRecoveryWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            } else {
                                WifiProStateMachine.this.logD("wifi Qos is [" + this.wifiQosLevel + " ]Ok, wifi_internet_level is [" + this.wifi_internet_level + "] Restore the wifi connection");
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_SCAN_COUNT);
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_SCAN_COUNT, (int) WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD, (int) WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                                restoreWifiConnect();
                            }
                        }
                        if (!(!WifiProStateMachine.this.mIsWiFiNoInternet || this.wifi_internet_level == WifiProStateMachine.INVALID_PID || this.wifi_internet_level == 6 || this.isWifiHandoverWifi)) {
                            WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            if (!this.isRecoveryWifi) {
                                this.isRecoveryWifi = WifiProStateMachine.DBG;
                                WifiProStateMachine.this.logD("wifi Internet is better ,try restore wifi 2,mobile handover wifi");
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetRestoreRICount();
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_SCAN_COUNT);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.WIFI_SCAN_COUNT, WifiProStateMachine.this.mCurrentSsid);
                                restoreWifiConnect();
                                break;
                            }
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                        if (WifiProStateMachine.this.mWifiManager.getWifiState() == WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD) {
                            WifiProStateMachine.this.logD("wifi state is : enabled, forgetUntrustedOpenAp");
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi state is : " + WifiProStateMachine.this.mWifiManager.getWifiState() + " ,go to wifi disconnected");
                    WifiProStateMachine.this.mIsRoveOutToDisconn = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    if (WifiProStateMachine.this.mIsMobileDataEnabled) {
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI)) {
                            WifiProStateMachine.this.logD("In verifying link state, MOBILE DATA is ON within delay time, cancel switching back to wifi.");
                            WifiProStateMachine.this.getHandler().removeMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI);
                            break;
                        }
                    }
                    int delayMs = WifiProStateMachine.this.mIsWiFiNoInternet ? WifiProStateMachine.WIFI_TCPRX_STATISTICS_INTERVAL : WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStateMachine.this.logD("In verifying link state, MOBILE DATA is OFF, try to delay " + delayMs + " ms to switch back to wifi.");
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI), (long) delayMs);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT /*136187*/:
                    this.isWifiGoodIntervalTimerOut = WifiProStateMachine.DBG;
                    break;
                case WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT /*136188*/:
                    this.isWifiRecoveryTimerOut = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.logD("isWifiRecoveryTimerOut is true,mobile can handover to wifi");
                    break;
                case WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI /*136189*/:
                    WifiProStateMachine.this.logW("WiFiProVerfyingLinkState::EVENT_MOBILE_RECOVERY_TO_WIFI, handle it.");
                    this.isWifiHandoverWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    restoreWifiConnect();
                    WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                    break;
                case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /*136191*/:
                    if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                        this.isWifiScanScreenOff = WifiProStateMachine.DBG;
                        break;
                    }
                    WifiProStateMachine.this.logD("inquire the surrounding AP for wifiHandover");
                    WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                    this.wifiScanCounter += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                    this.wifiScanCounter = Math.min(this.wifiScanCounter, WifiProStateMachine.WIFI_SCAN_INTERVAL_MAX);
                    long delay_scan_time = (((long) Math.pow(2.0d, (double) (this.wifiScanCounter / WifiProStateMachine.WIFI_SCAN_COUNT))) * 60) * 1000;
                    WifiProStateMachine.this.logD("delay_scan_time = " + delay_scan_time);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delay_scan_time);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    WifiProStateMachine.this.logW("start check wifi internet ");
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                case WifiProStateMachine.EVENT_USER_ROVE_IN /*136193*/:
                    WifiProStateMachine.this.mIsUserHandoverWiFi = WifiProStateMachine.DBG;
                    this.isWifiHandoverWifi = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserManualRICount();
                    } else {
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(WifiProStateMachine.WIFI_SCAN_COUNT);
                    }
                    restoreWifiConnect();
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }
    }

    class WifiConnectedState extends State {
        private boolean isChrShouldReport;
        private boolean isDialogDisplay;
        private boolean isIgnorAvailableWifiCheck;
        private boolean isKeepConnected;
        private boolean isPortalAP;
        private boolean isToastDisplayed;
        private int oldType;
        private int portalCheckCounter;
        private int tcpRxStatisticsTimer;

        WifiConnectedState() {
        }

        private void initConnectedState() {
            WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isKeepConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isPortalAP = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.portalCheckCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            this.tcpRxStatisticsTimer = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            WifiProStateMachine.this.mIsScanedRssiLow = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mIsScanedRssiMiddle = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isIgnorAvailableWifiCheck = WifiProStateMachine.DBG;
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mLastWifiLevel = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            WifiProStateMachine.this.setWifiCSPState(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            if (WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                WifiProStateMachine.this.refreshConnectedNetWork();
                WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
            }
            WifiProStateMachine.this.logD("isAllowWiFiAutoEvaluate == " + WifiProStateMachine.this.isAllowWiFiAutoEvaluate());
            WifiConfiguration cfg = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(WifiProStateMachine.this.mCurrentSsid);
            if (cfg != null) {
                int accessType = cfg.internetAccessType;
                WifiProStateMachine.this.logD("accessType = : " + accessType + ",qosLevel = " + cfg.networkQosLevel + ",wifiProNoInternetAccess = " + cfg.wifiProNoInternetAccess);
                if (cfg.isTempCreated) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(19);
                }
                if (WifiProStateMachine.WIFI_SCAN_COUNT == accessType) {
                    int temporaryQoeLevel = WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel();
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, temporaryQoeLevel);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_SCAN_COUNT, temporaryQoeLevel, (int) WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                    WifiProStateMachine.this.logD("WiFiProConnected temporaryQosLevel = " + temporaryQoeLevel);
                } else if (WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD == accessType || WifiProStateMachine.WIFI_HANDOVER_TIMERS == accessType) {
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(WifiProStateMachine.DBG);
                } else {
                    WiFiProScoreInfo wiFiProScoreInfo = WiFiProEvaluateController.getCurrentWiFiProScore(WifiProStateMachine.this.mCurrentSsid);
                    if (wiFiProScoreInfo != null && (WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD == wiFiProScoreInfo.internetAccessType || WifiProStateMachine.WIFI_HANDOVER_TIMERS == wiFiProScoreInfo.internetAccessType)) {
                        WifiProStateMachine.this.logD("WiFiProConnected internetAccessType = " + wiFiProScoreInfo.internetAccessType);
                        WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(WifiProStateMachine.DBG);
                    }
                }
            } else {
                WifiProStateMachine.this.logD("cfg= null ");
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            }
        }

        private void showNoInternetDialog(int reason) {
            WifiProStateMachine.this.logD("showNoInternetDialog reason = " + reason);
            if (WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER == reason) {
                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProDialog(WifiProStateMachine.HANDOVER_5G_DIFFERENCE_SCORE);
            } else {
                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProDialog(WifiProStateMachine.WIFI_SCAN_COUNT);
            }
        }

        private void reportDiffTypeCHR(int newType) {
            if (!this.isChrShouldReport) {
                this.isChrShouldReport = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                int diffType = WifiProStateMachine.this.mWiFiProEvaluateController.getChrDiffType(this.oldType, newType);
                WifiProStateMachine.this.logD("reportDiffTypeCHR is Enter, diffType  == " + diffType);
                if (diffType != 0) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseBG_AC_DiffType(diffType);
                }
                if (this.oldType == newType) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseActiveCheckRS_Same();
                }
            }
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiConnectedState is Enter, WiFiProConnected == " + WifiProStateMachine.this.mIsWiFiProConnected);
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            if (!WifiProStateMachine.this.mIsWiFiProConnected) {
                WifiProStateMachine.this.refreshConnectedNetWork();
                this.oldType = WifiProStateMachine.this.mWiFiProEvaluateController.getOldNetworkType(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.logD("WiFiProConnected oldType = " + this.oldType);
            }
            NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            if (wifi_info != null && wifi_info.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logD(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            }
            initConnectedState();
            WifiProStateMachine.this.initDualBandhanoverConnectedCHR();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiConnectedState is Exit");
            this.isDialogDisplay = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isToastDisplayed = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.isChrShouldReport = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            this.oldType = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.mSampleCollectionManager.setPortalAuthenticating(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo n = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (n != null && NetworkInfo.State.DISCONNECTED == n.getState()) {
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                        break;
                    }
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /*136190*/:
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork && WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.isAllowWifi2Mobile()) {
                        if (!this.isDialogDisplay) {
                            WifiProStateMachine.this.logD("surroundings change, allow wifi2mobile,should show intelligent dialog");
                            showNoInternetDialog(WifiProStateMachine.this.mWiFiNoInternetReason);
                            this.isDialogDisplay = WifiProStateMachine.DBG;
                        }
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                    handleWifiQosChangeWithConnected(msg);
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    if (!this.isIgnorAvailableWifiCheck) {
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet || (!((Boolean) msg.obj).booleanValue() && !WifiProStateMachine.this.isAllowWifi2Mobile())) {
                            if (!this.isToastDisplayed) {
                                WifiProStateMachine.this.logW("There is no network can switch");
                                this.isToastDisplayed = WifiProStateMachine.DBG;
                                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.DBG);
                                if (WifiProStateMachine.this.mWiFiNoInternetReason != 0) {
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 60000);
                                    break;
                                }
                                WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                }
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("AllowWifi2Mobile,should show intelligent dialog");
                        if (!this.isDialogDisplay) {
                            showNoInternetDialog(WifiProStateMachine.this.mWiFiNoInternetReason);
                            this.isDialogDisplay = WifiProStateMachine.DBG;
                        }
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    WifiProStateMachine.this.logD("WiFi internet check level = " + msg.arg1 + ", isKeepConnected = " + this.isKeepConnected + ", mIsUserHandoverWiFi = " + WifiProStateMachine.this.mIsUserHandoverWiFi);
                    WifiProStateMachine.this.notifyNetworkCheckResult(msg.arg1);
                    reportDiffTypeCHR(WifiProStateMachine.this.mWiFiProEvaluateController.getNewNetworkType(msg.arg1));
                    WifiProStateMachine.this.reportDualbandInternetResultCHR(msg.arg1);
                    if (this.isKeepConnected || WifiProStateMachine.this.mIsUserHandoverWiFi) {
                        if (WifiProStateMachine.INVALID_PID == msg.arg1 || 6 == msg.arg1) {
                            if (WifiProStateMachine.this.mWiFiNoInternetReason != 0) {
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                break;
                            }
                            WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                            break;
                        }
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED, WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        break;
                    }
                    this.isKeepConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    int internet_level = msg.arg1;
                    if (!this.isDialogDisplay || (internet_level != WifiProStateMachine.INVALID_PID && internet_level != 6)) {
                        if (this.isPortalAP) {
                            this.portalCheckCounter += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                            WifiProStateMachine.this.logD("portalCheckCounter = " + this.portalCheckCounter);
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                            if (this.portalCheckCounter < WifiProStateMachine.PORTAL_CHECK_MAX_TIMERS && (internet_level == 6 || internet_level == WifiProStateMachine.INVALID_PID)) {
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                                break;
                            } else if (internet_level == 6 || internet_level == WifiProStateMachine.INVALID_PID) {
                                internet_level = WifiProStateMachine.INVALID_PID;
                                WifiProStateMachine.this.mWifiProStatisticsManager.increasePortalUnauthCount();
                            }
                        }
                        WifiProStateMachine.this.mSampleCollectionManager.setPortalAuthenticating(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        if (internet_level != WifiProStateMachine.INVALID_PID) {
                            if (internet_level != 6) {
                                this.isKeepConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                                WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                                WifiProStateMachine.this.mIsNetworkAuthen = WifiProStateMachine.DBG;
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_SCAN_COUNT);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.WIFI_SCAN_COUNT, WifiProStateMachine.this.mCurrentSsid);
                                WifiProStateMachine.this.mWiFiProProtalController.notifyPortalAuthenStatus(WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.DBG);
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                                break;
                            }
                            WifiProStateMachine.this.logD("WifiConnectedState: WiFi is protal");
                            this.isPortalAP = WifiProStateMachine.DBG;
                            WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                            WifiProStateMachine.this.mIsPortalAp = WifiProStateMachine.DBG;
                            WifiProStateMachine.this.mIsNetworkAuthen = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            WifiProStateMachine.this.mWiFiNoInternetReason = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD, WifiProStateMachine.this.mCurrentSsid);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.DBG, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                            if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                                WifiProStateMachine.this.mSampleCollectionManager.setPortalAuthenticating(WifiProStateMachine.DBG);
                                WifiProStateMachine.this.mSampleCollectionManager.notifyPortalConnected(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.this.mCurrentBssid);
                                WifiProStateMachine.this.mWiFiProProtalController.notifyPortalConnected(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.this.mCurrentBssid);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("WiFi NO internet,isPortalAP = " + this.isPortalAP);
                        WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                        if (this.isPortalAP) {
                            this.isPortalAP = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            WifiProStateMachine.this.mWiFiNoInternetReason = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD, WifiProStateMachine.this.mCurrentSsid);
                        } else {
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.WIFI_HANDOVER_TIMERS);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.WIFI_HANDOVER_TIMERS, WifiProStateMachine.this.mCurrentSsid);
                            WifiProStateMachine.this.mWiFiNoInternetReason = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(WifiProStateMachine.DBG);
                        }
                        if (this.isIgnorAvailableWifiCheck) {
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        } else if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork);
                        }
                        if (!WifiProStateMachine.this.isNotShowIntelligentDialog()) {
                            if (!this.isIgnorAvailableWifiCheck) {
                                if (WifiProStateMachine.this.mWiFiNoInternetReason != 0) {
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                    break;
                                }
                                WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                }
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                break;
                            }
                            WifiProStateMachine.this.logD("inquire the surrounding AP for wifiHandover");
                            this.isIgnorAvailableWifiCheck = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                            WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                            break;
                        }
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                        break;
                    }
                    WifiProStateMachine.this.logD("AP is noInternet or Protal AP , Continue DisplayDialog");
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                    WifiProStateMachine.this.logD("Intelligent choice other network,go to  mWiFiLinkMonitorState");
                    WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                    this.isKeepConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    WifiProStateMachine.this.mWiFiProProtalController.notifyPortalAuthenStatus(WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                    handleDialogCancel();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    break;
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /*136195*/:
                    if (msg.obj == null || !((Boolean) msg.obj).booleanValue() || !WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET)) {
                        if (!(msg.obj == null || ((Boolean) msg.obj).booleanValue())) {
                            WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ the http unreachable.");
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, WifiProStateMachine.INVALID_PID);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ to check wifi immediately.");
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_CHECK_UNKOWN /*136309*/:
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /*136311*/:
                    int currentWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    WifiProStateMachine.this.logD("current rx= " + currentWifiTcpRxCount + ", last rx = " + WifiProStateMachine.this.mWifiTcpRxCount);
                    if (currentWifiTcpRxCount - WifiProStateMachine.this.mWifiTcpRxCount <= 0) {
                        WifiProStateMachine.this.logD("tcpRxStatisticsTimer = " + this.tcpRxStatisticsTimer + ", % 10 = " + (this.tcpRxStatisticsTimer % 10));
                        if (this.tcpRxStatisticsTimer > 0 && this.tcpRxStatisticsTimer % 10 == 0) {
                            this.tcpRxStatisticsTimer = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            break;
                        }
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        this.tcpRxStatisticsTimer += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        break;
                    }
                    this.tcpRxStatisticsTimer = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    break;
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }

        private void handleDialogCancel() {
            WifiProStateMachine.this.logD("Keep this network,do nothing!!!");
            this.isIgnorAvailableWifiCheck = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            this.isKeepConnected = WifiProStateMachine.DBG;
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                    WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                }
            }
            if (this.isDialogDisplay && WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
            }
        }

        private void handleWifiQosChangeWithConnected(Message msg) {
            if (this.isPortalAP && msg.arg1 <= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                WifiProStateMachine.this.logD("handleWifiQosChangeWithConnected, PortalAP Network Link Poor!");
                this.portalCheckCounter = WifiProStateMachine.PORTAL_CHECK_MAX_TIMERS;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, WifiProStateMachine.INVALID_PID);
            }
        }
    }

    class WifiDisConnectedState extends State {
        WifiDisConnectedState() {
        }

        public void enter() {
            WifiProStateMachine.mIsWifiManualEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
            if (WifiProStateMachine.this.mOpenAvailableAPCounter >= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                WifiProStateMachine.this.mOpenAvailableAPCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            }
            WifiProStateMachine.this.logD("WifiDisConnectedState is Enter");
            if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                WifiProStateMachine.this.mWiFiProProtalController.handleWifiDisconnected(WifiProStateMachine.this.mIsPortalAp);
                WifiProStateMachine.this.mSampleCollectionManager.notifyWifiDisconnected(WifiProStateMachine.this.mIsPortalAp);
            }
            WifiProStateMachine.this.mIsPortalAp = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mIsNetworkAuthen = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.resetVariables();
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                WifiProStateMachine.this.logD("BQE bad rove out, disconnect time recorded.");
                WifiProStateMachine.this.mChrWifiDisconnectStartTime = System.currentTimeMillis();
            }
            if (WifiProStateMachine.this.mRoveOutStarted && WifiProStateMachine.this.mIsRoveOutToDisconn) {
                if (WifiProStateMachine.this.mLoseInetRoveOut) {
                    WifiProStateMachine.this.logD("Not Inet rove out and WIFI disconnect.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.accuNotInetRoDisconnectData();
                } else {
                    WifiProStateMachine.this.logD("Qoe bad rove out and WIFI disconnect.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.accuQOEBadRoDisconnectData();
                }
            }
            WifiProStateMachine.this.mRoveOutStarted = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.mIsRoveOutToDisconn = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiDisConnectedState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || NetworkInfo.State.CONNECTED != networkInfo.getState() || !WifiProStateMachine.this.isWifiConnected()) {
                        if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("WifiDisConnectedState: wifi connect,to go connected");
                    WifiProStateMachine.this.mIsWiFiProConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    if (!WifiProStateMachine.this.isMobileDataConnected()) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = msg.obj;
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra(MessageUtil.EXTRA_WIFI_CONFIGURATION);
                    if (conn_cfg != null) {
                        int change_reason = confg_intent.getIntExtra(MessageUtil.EXTRA_CHANGE_REASON, WifiProStateMachine.INVALID_PID);
                        WifiProStateMachine.this.logD("WifiDisConnectedState, change reson " + change_reason + ", isTempCreated = " + conn_cfg.isTempCreated);
                        if (conn_cfg.isTempCreated && change_reason != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                            WifiProStateMachine.this.logD("WifiDisConnectedState, forget " + conn_cfg.SSID);
                            WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            break;
                        }
                    }
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }
    }

    class WifiSemiAutoEvaluateState extends State {
        WifiSemiAutoEvaluateState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiSemiAutoEvaluateState enter");
            WifiProStateMachine.this.setWifiCSPState(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
            if (!WifiProStateMachine.mIsWifiSemiAutoEvaluating) {
                WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DBG);
                WifiProStateMachine.mIsWifiSemiAutoEvaluating = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mIsAllowEvaluate = WifiProStateMachine.DBG;
                if (WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty()) {
                    WifiProStateMachine.this.logW("UnEvaluate AP records is empty !");
                } else {
                    WifiProStateMachine.this.mNetworkQosMonitor.startBqeService();
                    WifiProStateMachine.this.mOpenAvailableAPCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    return;
                }
            }
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiSemiAutoEvaluateState exit");
            if (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateCacheRecords();
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || DetailedState.CONNECTED != networkInfo.getDetailedState() || !WifiProStateMachine.this.isWifiConnected()) {
                        if (networkInfo != null && DetailedState.DISCONNECTED == networkInfo.getDetailedState() && (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate())) {
                            WifiProStateMachine.this.logW("Evaluate has complete, go to mWifiDisConnectedState");
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                            break;
                        }
                    }
                    WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                    WifiProStateMachine.this.logD("mIsWifiSemiAutoEvaluateComplete == " + WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete);
                    WifiProStateMachine.this.logD("******WifiSemiAutoEvaluateState go to mWifiConnectedState *****");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_COMPLETE /*136295*/:
                    WifiProStateMachine.this.logD("Evaluate has complete, restore wifi Config, mOpenAvailableAPCounter = " + WifiProStateMachine.this.mOpenAvailableAPCounter);
                    if (WifiProStateMachine.this.mOpenAvailableAPCounter >= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                        WifiProStateMachine.this.mOpenAvailableAPCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    }
                    WifiProStateMachine.this.mWiFiProEvaluateController;
                    WiFiProEvaluateController.evaluateAPHashMapDump();
                    WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                    WifiProStateMachine.mIsWifiSemiAutoEvaluating = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                    if (wifi_info != null) {
                        if (wifi_info.getState() == NetworkInfo.State.DISCONNECTED) {
                            WifiProStateMachine.this.logD("wifi has disconnected, go to mWifiDisConnectedState");
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi_info is null, go to mWiFiProEnableState");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (!WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
                        break;
                    }
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }
    }

    class WifiSemiAutoScoreState extends State {
        private int checkCounter;
        private long checkTime;
        private long connectTime;
        private boolean isCheckRuning;
        private String nextBSSID;
        private String nextSSID;

        WifiSemiAutoScoreState() {
            this.nextSSID = null;
            this.nextBSSID = null;
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiSemiAutoScoreState enter,  mIsAllowEvaluate = " + WifiProStateMachine.this.mIsAllowEvaluate);
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate() && !WifiProStateMachine.this.mIsManualConnectedWiFi && WifiProStateMachine.this.mIsAllowEvaluate) {
                this.connectTime = 0;
                this.checkTime = 0;
                this.checkCounter = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                this.isCheckRuning = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                this.nextSSID = WifiProStateMachine.this.mWiFiProEvaluateController.getNextEvaluateWiFiSSID();
                if (TextUtils.isEmpty(this.nextSSID)) {
                    WifiProStateMachine.this.logD("ALL SemiAutoScore has Evaluate complete!");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                    return;
                }
                WifiProStateMachine.this.logD("***********start SemiAuto Evaluate nextSSID :" + this.nextSSID);
                if (WifiProStateMachine.this.mWiFiProEvaluateController.isAbandonEvaluate(this.nextSSID)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUTE_ABANDON);
                    return;
                }
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT, 75000);
                WifiProStateMachine.this.mWifiProUIDisplayManager.showToastL("start  evaluate :" + this.nextSSID);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), (int) WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, (int) WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, this.nextSSID);
                WifiProStateMachine.this.refreshConnectedNetWork();
                if (WifiProStateMachine.this.isWifiConnected() && this.nextSSID.equals(WifiProStateMachine.this.mCurrentSsid)) {
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    this.isCheckRuning = WifiProStateMachine.DBG;
                    return;
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                return;
            }
            WifiProStateMachine.this.logD("WiFiPro auto Evaluate has  closed");
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiSemiAutoScoreState exit");
            WifiProStateMachine.this.mNetworkQosMonitor.resetMonitorStatus();
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP);
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            this.nextBSSID = null;
            if (!TextUtils.isEmpty(this.nextSSID)) {
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(this.nextSSID, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
            }
        }

        public boolean processMessage(Message msg) {
            WifiProStateMachine wifiProStateMachine;
            String str;
            int i;
            WifiProConfigStore -get68;
            WifiProStateMachine wifiProStateMachine2;
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    WifiProStateMachine.this.logD(", nextSSID SSID = " + this.nextSSID + ", networkInfo = " + networkInfo);
                    if (networkInfo == null || NetworkInfo.State.DISCONNECTED != networkInfo.getState()) {
                        if (networkInfo == null || NetworkInfo.State.CONNECTED != networkInfo.getState()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                                String currssid = networkInfo.getExtraInfo();
                                if (!(TextUtils.isEmpty(this.nextSSID) || TextUtils.isEmpty(currssid))) {
                                    if (!this.nextSSID.equals(currssid)) {
                                        WifiProStateMachine.this.logD("Connect other ap ,ssid : " + currssid);
                                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                        break;
                                    }
                                }
                            }
                        }
                        String extssid = networkInfo.getExtraInfo();
                        if (!(TextUtils.isEmpty(this.nextSSID) || TextUtils.isEmpty(extssid))) {
                            if (!this.nextSSID.equals(extssid)) {
                                WifiProStateMachine.this.logD("Connected other ap ,ssid : " + extssid);
                                WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                break;
                            }
                        }
                        wifiProStateMachine = WifiProStateMachine.this;
                        WifiConfiguration wifiConfig = r0.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID);
                        if (wifiConfig != null) {
                            int tag = Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.WIFI_EVALUATE_TAG, WifiProStateMachine.INVALID_PID);
                            wifiProStateMachine = WifiProStateMachine.this;
                            str = this.nextSSID;
                            r0.logD(r0 + "is Connected, wifiConfig isTempCreated = " + wifiConfig.isTempCreated + ", Tag = " + tag);
                        }
                        WifiProStateMachine.this.logD("receive connect msg ,ssid : " + extssid);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(16);
                        break;
                    }
                    if (!TextUtils.isEmpty(this.nextSSID)) {
                        if (this.nextSSID.equals(networkInfo.getExtraInfo())) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(22);
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    if (!TextUtils.isEmpty(this.nextSSID) && this.isCheckRuning) {
                        this.checkTime = (System.currentTimeMillis() - this.checkTime) / 1000;
                        WifiProStateMachine.this.logD(this.nextSSID + " checkTime = " + this.checkTime + " s");
                        int result = msg.arg1;
                        int type = WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                        if (7 != result) {
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWiFiProCHRMgr.updateSSID(this.nextSSID);
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWiFiProCHRMgr.updateWifiproTimeLen((short) ((int) this.checkTime));
                            WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "BG_AC_TIME_LEN");
                        }
                        if (WifiProStateMachine.HANDOVER_5G_DIFFERENCE_SCORE == result) {
                            type = WifiProStateMachine.WIFI_SCAN_COUNT;
                            wifiProStateMachine = WifiProStateMachine.this;
                            wifiProStateMachine.mOpenAvailableAPCounter = wifiProStateMachine.mOpenAvailableAPCounter + WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(WifiProStateMachine.DBG);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD);
                        } else if (6 == result) {
                            type = WifiProStateMachine.WIFI_TO_WIFI_THRESHOLD;
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(6);
                        } else if (WifiProStateMachine.INVALID_PID == result) {
                            type = WifiProStateMachine.WIFI_HANDOVER_TIMERS;
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(WifiProStateMachine.HANDOVER_5G_DIFFERENCE_SCORE);
                        } else if (7 == result) {
                            i = this.checkCounter;
                            if (r0 == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                                WifiProStateMachine.this.logD("internet check timeout ,check again");
                                this.checkTime = System.currentTimeMillis();
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                                break;
                            }
                            type = WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(21);
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                        }
                        WifiProStateMachine.this.logD(this.nextSSID + " type = " + type);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, type);
                        -get68 = WifiProStateMachine.this.mWifiProConfigStore;
                        wifiProStateMachine2 = WifiProStateMachine.this;
                        -get68.updateWifiEvaluateConfig(r0.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, type, this.nextSSID);
                        wifiProStateMachine = WifiProStateMachine.this;
                        r0.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, WifiProStateMachine.DBG);
                        if (type != WifiProStateMachine.WIFI_SCAN_COUNT) {
                            WifiProStateMachine.this.logD("clean evaluate ap :" + this.nextSSID);
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                            break;
                        }
                        WifiProStateMachine.this.mNetworkQosMonitor.startWiFiBqeDetect(WifiProStateMachine.WIFI_TCPRX_STATISTICS_INTERVAL);
                        break;
                    }
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mWifiManager.getScanResults());
                    WifiProStateMachine.this.mIsAllowEvaluate = WifiProStateMachine.this.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList);
                    if (!WifiProStateMachine.this.mIsAllowEvaluate) {
                        WifiProStateMachine.this.logD("discover save ap, stop allow evaluate");
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /*136297*/:
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /*136299*/:
                    int level = msg.arg1;
                    WifiProStateMachine.this.logD(this.nextSSID + "  TCPRTT  level = " + level);
                    if (WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, level)) {
                        -get68 = WifiProStateMachine.this.mWifiProConfigStore;
                        wifiProStateMachine2 = WifiProStateMachine.this;
                        -get68.updateWifiEvaluateConfig(r0.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), WifiProStateMachine.WIFI_HANDOVER_TIMERS, level, this.nextSSID);
                    }
                    if (level == 0) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                        wifiProStateMachine = WifiProStateMachine.this;
                        r0.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    }
                    boolean enabled = WifiProCommonUtils.isWifiSecDetectOn(WifiProStateMachine.this.mContext);
                    wifiProStateMachine = WifiProStateMachine.this;
                    int security = r0.mWiFiProEvaluateController.getWifiSecurityInfo(this.nextSSID);
                    WifiProStateMachine.this.logD("security switch enabled = " + enabled + ", current security value = " + security);
                    if (enabled) {
                        if (WifiProCommonUtils.isWifiConnected(WifiProStateMachine.this.mWifiManager) && (security == WifiProStateMachine.INVALID_PID || security == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER)) {
                            this.nextBSSID = WifiProCommonUtils.getCurrentBssid(WifiProStateMachine.this.mWifiManager);
                            wifiProStateMachine = WifiProStateMachine.this;
                            StringBuilder append = new StringBuilder().append("recv BQE level = ");
                            str = this.nextSSID;
                            r0.logD(r26.append(level).append(", start to query wifi security, ssid = ").append(r0).append(", bssid = ").append(this.nextBSSID).toString());
                            wifiProStateMachine = WifiProStateMachine.this;
                            r0.mNetworkQosMonitor.queryWifiSecurity(this.nextSSID, this.nextBSSID);
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT, 30000);
                            break;
                        }
                    }
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, WifiProStateMachine.DBG);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT /*136301*/:
                    WifiProStateMachine.this.logD(this.nextSSID + " Conenct Time Out,connect fail! conenct Time = 35s");
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(15);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(WifiProStateMachine.PORTAL_CHECK_MAX_TIMERS);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    -get68 = WifiProStateMachine.this.mWifiProConfigStore;
                    wifiProStateMachine2 = WifiProStateMachine.this;
                    -get68.updateWifiEvaluateConfig(r0.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, this.nextSSID);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_LAST_EVALUTE_VALID /*136302*/:
                    wifiProStateMachine = WifiProStateMachine.this;
                    WiFiProScoreInfo wiFiProScoreInfo = r0.mWiFiProEvaluateController.getCurrentWiFiProScoreInfo(this.nextSSID);
                    if (wiFiProScoreInfo != null) {
                        wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine2 = WifiProStateMachine.this;
                        str = this.nextSSID;
                        r0.mWifiProConfigStore.updateWifiEvaluateConfig(r0.mWiFiProEvaluateController.getWifiConfiguration(r0), wiFiProScoreInfo.internetAccessType, wiFiProScoreInfo.networkQosLevel, wiFiProScoreInfo.networkQosScore);
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_TIMEOUT /*136304*/:
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    -get68 = WifiProStateMachine.this.mWifiProConfigStore;
                    wifiProStateMachine2 = WifiProStateMachine.this;
                    -get68.updateWifiEvaluateConfig(r0.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, this.nextSSID);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.logD(this.nextSSID + " evaluate Time = 70s");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_ABANDON /*136305*/:
                    WifiProStateMachine.this.logD(this.nextSSID + "abandon evalute ");
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER);
                    -get68 = WifiProStateMachine.this.mWifiProConfigStore;
                    wifiProStateMachine2 = WifiProStateMachine.this;
                    -get68.updateWifiEvaluateConfig(r0.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, this.nextSSID);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET /*136307*/:
                    WifiProStateMachine.this.logW("wifi conenct, start check internet,  checkCounter =   " + this.checkCounter);
                    if (this.checkCounter == 0) {
                        this.connectTime = (System.currentTimeMillis() - this.connectTime) / 1000;
                        WifiProStateMachine.this.logD(this.nextSSID + " background conenct Time =" + this.connectTime + " s");
                        wifiProStateMachine = WifiProStateMachine.this;
                        r0.mWiFiProCHRMgr.updateSSID(this.nextSSID);
                        wifiProStateMachine = WifiProStateMachine.this;
                        r0.mWiFiProCHRMgr.updateWifiproTimeLen((short) ((int) this.connectTime));
                        WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "BG_CONN_AP_TIME_LEN");
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
                    }
                    this.checkTime = System.currentTimeMillis();
                    this.checkCounter += WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    this.isCheckRuning = WifiProStateMachine.DBG;
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = msg.obj;
                    int change_reason = confg_intent.getIntExtra(MessageUtil.EXTRA_CHANGE_REASON, WifiProStateMachine.INVALID_PID);
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra(MessageUtil.EXTRA_WIFI_CONFIGURATION);
                    if (conn_cfg != null) {
                        wifiProStateMachine = WifiProStateMachine.this;
                        str = this.nextSSID;
                        r0.logD(", nextSSID SSID = " + r0 + ", conf  " + conn_cfg.SSID);
                        if (change_reason != 0) {
                            if (change_reason == WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                WifiProStateMachine.this.logD("--- change_reason =change,  change a ssid = " + conn_cfg.SSID);
                                if (WifiProStateMachine.this.isWifiConnected()) {
                                    if (!TextUtils.isEmpty(this.nextSSID)) {
                                        if (this.nextSSID.equals(conn_cfg.SSID)) {
                                            WifiProStateMachine.this.mWiFiProEvaluateController.clearUntrustedOpenApList();
                                            WifiProStateMachine.this.mWifiProConfigStore.resetTempCreatedConfig(conn_cfg);
                                            i = conn_cfg.status;
                                            if (r0 == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                                                WifiProStateMachine.this.mWifiManager.connect(conn_cfg, null);
                                                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(18);
                                            }
                                        }
                                    }
                                } else {
                                    WifiProStateMachine.this.logD("--- wifi has disconnect ----");
                                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                }
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("--- change_reason =add,  add a new conn_cfg--,isTempCreated : " + conn_cfg.isTempCreated);
                        if (!TextUtils.isEmpty(this.nextSSID)) {
                            if (this.nextSSID.equals(conn_cfg.SSID)) {
                                wifiProStateMachine = WifiProStateMachine.this;
                                r0.mWiFiProEvaluateController.addUntrustedOpenApList(conn_cfg.SSID);
                                break;
                            }
                        }
                        if (!TextUtils.isEmpty(conn_cfg.SSID)) {
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED /*136310*/:
                    if (WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.logD("P2PConnectedOrConnecting  , stop allow evaluate");
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_SECURITY_RESPONSE /*136312*/:
                case WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT /*136313*/:
                    if (msg.obj != null) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT);
                        Bundle bundle = msg.obj;
                        String ssid = bundle.getString("com.huawei.wifipro.FLAG_SSID");
                        if (ssid != null) {
                            if (ssid.equals(this.nextSSID)) {
                                String bssid = bundle.getString("com.huawei.wifipro.FLAG_BSSID");
                                int status = bundle.getInt("com.huawei.wifipro.FLAG_SECURITY_STATUS");
                                WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, ssid = " + ssid + ", bssid = " + bssid + ", status = " + status);
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, status);
                                if (status >= WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                                    WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, unsecurity, upload CHR statistic.");
                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(WifiProStateMachine.WIFI_SCAN_COUNT);
                                }
                            }
                        }
                        WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, it's invalid ssid = " + ssid + ", ignore the result.");
                        break;
                    }
                    WifiProStateMachine.this.logW("EVENT_WIFI_SECURITY_RESPONSE, timeout happend.");
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, WifiProStateMachine.INVALID_PID);
                    wifiProStateMachine = WifiProStateMachine.this;
                    r0.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, WifiProStateMachine.DBG);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP /*136314*/:
                    if (!WifiProStateMachine.this.isWifiConnected()) {
                        evaluteNextAP();
                        break;
                    }
                    WifiProStateMachine.this.logD("wifi still connectd, delay 2s to evalute next ap");
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                    break;
                default:
                    return WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
            }
            return WifiProStateMachine.DBG;
        }

        private void evaluteNextAP() {
            WifiProStateMachine.this.logD("start evalute next ap");
            if (WifiProStateMachine.this.mWiFiProEvaluateController.connectWifi(this.nextSSID)) {
                this.connectTime = System.currentTimeMillis();
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT, 35000);
                return;
            }
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
            WifiProStateMachine.this.logD("background connect fail!");
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiProStateMachine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiProStateMachine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProStateMachine.<clinit>():void");
    }

    private static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return System.getInt(cr, name, def ? WIFI_CHECK_UNKNOW_TIMER : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG) == WIFI_CHECK_UNKNOW_TIMER ? DBG : DEFAULT_WIFI_PRO_ENABLED;
    }

    private static boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Global.getInt(cr, name, def ? WIFI_CHECK_UNKNOW_TIMER : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG) == WIFI_CHECK_UNKNOW_TIMER ? DBG : DEFAULT_WIFI_PRO_ENABLED;
    }

    private static boolean getSettingsSecureBoolean(ContentResolver cr, String name, boolean def) {
        return Secure.getInt(cr, name, def ? WIFI_CHECK_UNKNOW_TIMER : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG) == WIFI_CHECK_UNKNOW_TIMER ? DBG : DEFAULT_WIFI_PRO_ENABLED;
    }

    private static int getSettingsSystemInt(ContentResolver cr, String name, int def) {
        return System.getInt(cr, name, def);
    }

    public static WifiProStateMachine createWifiProStateMachine(Context context, Messenger dstMessenger) {
        if (mWifiProStateMachine == null) {
            mWifiProStateMachine = new WifiProStateMachine(context, dstMessenger);
        }
        mWifiProStateMachine.start();
        return mWifiProStateMachine;
    }

    public static WifiProStateMachine getWifiProStateMachineImpl() {
        return mWifiProStateMachine;
    }

    private WifiProStateMachine(Context context, Messenger dstMessenger) {
        super("WifiProStateMachine");
        this.mDefaultState = new DefaultState();
        this.mWiFiProDisabledState = new WiFiProDisabledState();
        this.mWiFiProEnableState = new WiFiProEnableState();
        this.mWifiConnectedState = new WifiConnectedState();
        this.mWiFiLinkMonitorState = new WiFiLinkMonitorState();
        this.mWiFiProVerfyingLinkState = new WiFiProVerfyingLinkState();
        this.mWifiDisConnectedState = new WifiDisConnectedState();
        this.mWifiSemiAutoEvaluateState = new WifiSemiAutoEvaluateState();
        this.mWifiSemiAutoScoreState = new WifiSemiAutoScoreState();
        this.mWifiToWifiType = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mChrRoveOutStartTime = 0;
        this.mChrWifiDidableStartTime = 0;
        this.mChrWifiDisconnectStartTime = 0;
        this.mRoSsid = null;
        this.mRoveOutStarted = DEFAULT_WIFI_PRO_ENABLED;
        this.mLoseInetRoveOut = DEFAULT_WIFI_PRO_ENABLED;
        this.mIsRoveOutToDisconn = DEFAULT_WIFI_PRO_ENABLED;
        this.mDualBandMonitorInfoSize = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mDualBandEstimateInfoSize = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mDualBandMonitorStart = DEFAULT_WIFI_PRO_ENABLED;
        this.mDuanBandHandoverType = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mAvailable5GAPBssid = null;
        this.mAvailable5GAPSsid = null;
        this.mAvailable5GAPAuthType = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mDualBandConnectAPSsid = null;
        this.mDualBandMonitorApList = new ArrayList();
        this.mDualBandEstimateApList = new ArrayList();
        this.mIsDualbandhandover = DEFAULT_WIFI_PRO_ENABLED;
        this.mIsDualbandhandoverSucc = DEFAULT_WIFI_PRO_ENABLED;
        this.mDualbandhanoverTime = 0;
        this.mDualbandSelectAPInfo = null;
        this.mDualbandCurrentAPInfo = null;
        this.mDualbandPingPangTime = 0;
        this.mDualbandRoveInConut = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mLastConnect5GAP = null;
        this.mLastCSPState = INVALID_PID;
        this.mContext = context;
        this.mWsmChannel = new AsyncChannel();
        this.mWsmChannel.connectSync(this.mContext, getHandler(), dstMessenger);
        this.mContentResolver = context.getContentResolver();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        WifiProStatisticsManager.initStatisticsManager(this.mContext);
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        this.mWiFiProCHRMgr = WifiProCHRManager.getInstance();
        this.mNetworkBlackListManager = NetworkBlackListManager.getNetworkBlackListManagerInstance(this.mContext);
        this.mWifiProUIDisplayManager = WifiProUIDisplayManager.createInstance(context, this);
        this.mHwIntelligenceWiFiManager = HwIntelligenceWiFiManager.createInstance(context, this.mWifiProUIDisplayManager);
        this.mWifiProConfigurationManager = WifiProConfigurationManager.createWifiProConfigurationManager(this.mContext);
        this.mWifiProConfigStore = new WifiProConfigStore(this.mContext, this.mWsmChannel);
        this.mAppWhitelists = this.mWifiProConfigurationManager.getAppWhitelists();
        this.mNetworkQosMonitor = new NetworkQosMonitor(this.mContext, this, dstMessenger, this.mWifiProUIDisplayManager);
        this.mAbsPhoneWindowManager = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        this.mWifiHandover = new WifiHandover(this.mContext, this);
        this.mIsWiFiProEnabled = getSettingsSystemBoolean(this.mContentResolver, KEY_SMART_NETWORK_SWITCHING, DEFAULT_WIFI_PRO_ENABLED);
        this.mIsPrimaryUser = ActivityManager.getCurrentUser() == 0 ? DBG : DEFAULT_WIFI_PRO_ENABLED;
        logD("UserID =  " + ActivityManager.getCurrentUser() + ", mIsPrimaryUser = " + this.mIsPrimaryUser);
        this.mSampleCollectionManager = new SampleCollectionManager(this.mContext, this.mWifiProConfigurationManager);
        this.mWiFiProProtalController = new WiFiProPortalController(this.mContext, this.mTelephonyManager, this.mWifiProConfigurationManager, this.mSampleCollectionManager);
        this.mHwAutoConnectManager = new HwAutoConnectManager(context, this.mWsmChannel, this.mNetworkQosMonitor);
        this.mDualBandManager = HwDualBandManager.createInstance(context, this);
        this.mHwDualBandBlackListMgr = HwDualBandBlackListManager.getHwDualBandBlackListMgrInstance();
        addState(this.mDefaultState);
        addState(this.mWiFiProDisabledState, this.mDefaultState);
        addState(this.mWiFiProEnableState, this.mDefaultState);
        addState(this.mWifiConnectedState, this.mWiFiProEnableState);
        addState(this.mWiFiProVerfyingLinkState, this.mWiFiProEnableState);
        addState(this.mWifiDisConnectedState, this.mWiFiProEnableState);
        addState(this.mWiFiLinkMonitorState, this.mWiFiProEnableState);
        addState(this.mWifiSemiAutoEvaluateState, this.mWiFiProEnableState);
        addState(this.mWifiSemiAutoScoreState, this.mWifiSemiAutoEvaluateState);
        this.mWiFiProEvaluateController = new WiFiProEvaluateController(context);
        this.mIsPortalAp = DEFAULT_WIFI_PRO_ENABLED;
        this.mIsNetworkAuthen = DEFAULT_WIFI_PRO_ENABLED;
        registerForSettingsChanges();
        registerForMobileDataChanges();
        registerForMobilePDPSwitchChanges();
        registerNetworkReceiver();
        registerOOBECompleted();
        registerForVpnSettingsChanges();
        registerForAppPidChanges();
        registerForAPEvaluateChanges();
        registerForManualConnectChanges();
        this.mHwAutoConnectManager.init();
        setInitialState(this.mWiFiProEnableState);
        logD("System Create WifiProStateMachine Complete!");
    }

    private void defaulVariableInit() {
        if (!this.isVariableInited) {
            this.mIsMobileDataEnabled = getSettingsGlobalBoolean(this.mContentResolver, "mobile_data", DEFAULT_WIFI_PRO_ENABLED);
            this.mEmuiPdpSwichValue = getSettingsSystemInt(this.mContentResolver, KEY_EMUI_WIFI_TO_PDP, WIFI_CHECK_UNKNOW_TIMER);
            this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, DEFAULT_WIFI_PRO_ENABLED);
            this.mIsVpnWorking = getSettingsSystemBoolean(this.mContentResolver, SETTING_SECURE_VPN_WORK_VALUE, DEFAULT_WIFI_PRO_ENABLED);
            if (this.mIsVpnWorking) {
                System.putInt(this.mContext.getContentResolver(), SETTING_SECURE_VPN_WORK_VALUE, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                this.mIsVpnWorking = DEFAULT_WIFI_PRO_ENABLED;
            }
            System.putInt(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
            setWifiEvaluateTag(DEFAULT_WIFI_PRO_ENABLED);
            this.isVariableInited = DBG;
            logD("Variable Init Complete!");
        }
    }

    private void registerNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = WifiProStateMachine.DBG;
                String action = intent.getAction();
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (WifiProStateMachine.isWifiEvaluating() && WifiProStateMachine.this.mIsWiFiProEnabled) {
                        WifiProStateMachine.this.mIsManualConnectedWiFi = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT, WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                        if (WifiProStateMachine.this.mIsManualConnectedWiFi) {
                            WifiProStateMachine.this.logD("ManualConnectedWiFi  AP, ,isWifiEvaluating ");
                            WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                        }
                    }
                    if (info != null && DetailedState.OBTAINING_IPADDR == info.getDetailedState()) {
                        WifiProStateMachine.this.mIsWiFiProConnected = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.logD("wifi is conencted, WiFiProEnabled = " + WifiProStateMachine.this.mIsWiFiProEnabled + ", VpnWorking " + WifiProStateMachine.this.mIsVpnWorking);
                    } else if (info != null && DetailedState.CONNECTED == info.getDetailedState()) {
                        WifiProStateMachine.this.setDuanBandManualConnect();
                        WifiProStateMachine.this.resetWifiProManualConnect();
                    } else if (info != null && DetailedState.DISCONNECTED == info.getDetailedState()) {
                        WifiProStateMachine.this.mWiFiProEvaluateController.restoreEvaluateRecord(info);
                        if (WifiProStateMachine.this.mDisableDuanBandHandover) {
                            WifiProStateMachine.this.logD("set mDisableDuanBandHandover false");
                            WifiProStateMachine.this.mDisableDuanBandHandover = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE, intent);
                } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                        WifiProStateMachine.this.logD("wifi state is disabled, set mIsUserManualConnectAp false");
                        WifiProStateMachine.this.mDisableDuanBandHandover = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.mIsUserManualConnectAp = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                        WifiProStateMachine.this.resetDualBandhanoverCHR();
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_SCREEN_ON);
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendScreenOnEvent();
                    }
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                    if (WifiProStateMachine.this.mWifiProUIDisplayManager.mIsNotificationShown && WifiProStateMachine.this.mWiFiProEvaluateController.isAccessAPOutOfRange(WifiProStateMachine.this.mWifiManager.getScanResults())) {
                        WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED);
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE);
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE, intent);
                } else if (MessageUtil.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)) {
                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(intent);
                    if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiSemiAutoScoreState) {
                        WifiConfiguration conn_cfg = (WifiConfiguration) intent.getParcelableExtra(MessageUtil.EXTRA_WIFI_CONFIGURATION);
                        if (conn_cfg != null) {
                            int change_reason = intent.getIntExtra(MessageUtil.EXTRA_CHANGE_REASON, WifiProStateMachine.INVALID_PID);
                            WifiProStateMachine.this.logD("ssid = " + conn_cfg.SSID + ", change reson " + change_reason + ", isTempCreated = " + conn_cfg.isTempCreated);
                            if (conn_cfg.isTempCreated && change_reason != WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER) {
                                WifiProStateMachine.this.logD("WiFiProDisabledState, forget " + conn_cfg.SSID);
                                WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            }
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED, intent);
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE, intent);
                } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    int userID = intent.getIntExtra("android.intent.extra.user_handle", WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    if (userID != 0) {
                        z = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    }
                    wifiProStateMachine.mIsPrimaryUser = z;
                    WifiProStateMachine.this.logD("user has switched,new userID = " + userID);
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null) {
                        WifiProStateMachine.this.mIsP2PConnectedOrConnecting = p2pNetworkInfo.isConnectedOrConnecting();
                    }
                    if (!WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED);
                    }
                } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                    int p2pState = intent.getIntExtra("extraState", WifiProStateMachine.INVALID_PID);
                    if (p2pState == WifiProStateMachine.WIFI_CHECK_UNKNOW_TIMER || p2pState == WifiProStateMachine.WIFI_HANDOVER_TIMERS) {
                        WifiProStateMachine.this.mIsP2PConnectedOrConnecting = WifiProStateMachine.DBG;
                    } else {
                        WifiProStateMachine.this.mIsP2PConnectedOrConnecting = WifiProStateMachine.DEFAULT_WIFI_PRO_ENABLED;
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED);
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN);
        this.mIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE);
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction(MessageUtil.CONFIGURED_NETWORKS_CHANGED_ACTION);
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
        this.mHMDBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN.equals(action)) {
                    WifiProStateMachine.this.logD("ACTION_HIGH_MOBILE_DATA  rove in event received.");
                    WifiProStateMachine.this.userHandoverWifi();
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseHighMobileDataBtnRiCount();
                    }
                } else if (WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE.equals(action)) {
                    WifiProStateMachine.this.logD("ACTION_HIGH_MOBILE_DATA  delete event received, stop notify.");
                    if (WifiProStateMachine.this.mNetworkQosMonitor != null) {
                        WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(WifiProStateMachine.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                    }
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserDelNotifyCount();
                    }
                } else if (WifiproUtils.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY.equals(action)) {
                    WifiProStateMachine.this.logD("**receive wifi connected concurrently********");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                }
            }
        };
        this.mHMDIntentFilter = new IntentFilter();
        this.mHMDIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN);
        this.mHMDIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE);
        this.mHMDIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY);
        this.mContext.registerReceiverAsUser(this.mHMDBroadcastReceiver, UserHandle.ALL, this.mHMDIntentFilter, null, null);
    }

    private void unregisterReceiver() {
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        if (this.mHMDBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mHMDBroadcastReceiver);
            this.mHMDBroadcastReceiver = null;
        }
    }

    private void registerOOBECompleted() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("device_provisioned"), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass3(getHandler()));
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_SMART_NETWORK_SWITCHING), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass4(getHandler()));
    }

    private void registerForMobileDataChanges() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass5(getHandler()));
    }

    private void registerForMobilePDPSwitchChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_EMUI_WIFI_TO_PDP), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass6(getHandler()));
    }

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SETTING_SECURE_VPN_WORK_VALUE), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass7(getHandler()));
    }

    private void registerForAppPidChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SETTING_SECURE_CONN_WIFI_PID), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass8(getHandler()));
    }

    private void registerForAPEvaluateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(KEY_WIFIPRO_RECOMMEND_NETWORK), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass9(getHandler()));
    }

    private void registerForManualConnectChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_WIFIPRO_MANUAL_CONNECT), DEFAULT_WIFI_PRO_ENABLED, new AnonymousClass10(getHandler()));
    }

    private void resetVariables() {
        this.mNetworkQosMonitor.stopBqeService();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, DEFAULT_WIFI_PRO_ENABLED, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, DEFAULT_WIFI_PRO_ENABLED);
        this.mWifiProUIDisplayManager.notificateNetAccessChange(DEFAULT_WIFI_PRO_ENABLED);
        this.mLastWifiLevel = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mIsWiFiNoInternet = DEFAULT_WIFI_PRO_ENABLED;
        this.mWiFiProPdpSwichValue = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        reSetWifiInternetState();
        this.mNetworkQosMonitor.stopALLMonitor();
        this.mNetworkQosMonitor.resetMonitorStatus();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mIsUserHandoverWiFi = DEFAULT_WIFI_PRO_ENABLED;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = DEFAULT_WIFI_PRO_ENABLED;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    private void updateWifiInternetStateChange(int lenvel) {
        if (isWifiConnected()) {
            if (this.mLastWifiLevel == lenvel) {
                logD("wifi lenvel is not change, don't report, lenvel = " + lenvel);
                return;
            }
            this.mLastWifiLevel = lenvel;
            if (INVALID_PID == lenvel) {
                Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(DBG);
                logD("mIsPortalAp = " + this.mIsPortalAp + ", mIsNetworkAuthen = " + this.mIsNetworkAuthen);
                if (!this.mIsPortalAp || this.mIsNetworkAuthen) {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, DBG, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, DEFAULT_WIFI_PRO_ENABLED);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, (int) WIFI_CHECK_UNKNOW_TIMER, (int) WIFI_HANDOVER_TIMERS, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, WIFI_HANDOVER_TIMERS);
                } else {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, DBG, WIFI_CHECK_UNKNOW_TIMER, DEFAULT_WIFI_PRO_ENABLED);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, (int) WIFI_CHECK_UNKNOW_TIMER, (int) WIFI_TO_WIFI_THRESHOLD, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, WIFI_TO_WIFI_THRESHOLD);
                }
            } else if (6 == lenvel) {
                Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(DBG);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, DBG, WIFI_CHECK_UNKNOW_TIMER, DEFAULT_WIFI_PRO_ENABLED);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, (int) WIFI_CHECK_UNKNOW_TIMER, (int) WIFI_TO_WIFI_THRESHOLD, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, WIFI_TO_WIFI_THRESHOLD);
            } else {
                Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
                this.mWifiProUIDisplayManager.notificateNetAccessChange(DEFAULT_WIFI_PRO_ENABLED);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, DEFAULT_WIFI_PRO_ENABLED, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, DEFAULT_WIFI_PRO_ENABLED);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, (int) WIFI_CHECK_UNKNOW_TIMER, (int) WIFI_SCAN_COUNT, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, WIFI_SCAN_COUNT);
            }
        }
    }

    private void reSetWifiInternetState() {
        logD("reSetWifiInternetState");
        Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
    }

    private void setWifiCSPState(int state) {
        if (this.mLastCSPState == state) {
            logD("setWifiCSPState state is not change,ignor! mLastCSPState:" + this.mLastCSPState);
            return;
        }
        logD("setWifiCSPState new state = " + state);
        this.mLastCSPState = state;
        System.putInt(this.mContext.getContentResolver(), WIFI_CSP_DISPALY_STATE, state);
    }

    private void registerCallBack() {
        this.mNetworkQosMonitor.registerCallBack(this);
        this.mWifiHandover.registerCallBack(this, this.mNetworkQosMonitor);
        this.mWifiProUIDisplayManager.registerCallBack(this);
    }

    private void unRegisterCallBack() {
        this.mNetworkQosMonitor.unRegisterCallBack();
        this.mWifiHandover.unRegisterCallBack();
        this.mWifiProUIDisplayManager.unRegisterCallBack();
    }

    private boolean isWiFiPoorer(int wifi_level, int mobile_level) {
        boolean z = DBG;
        logD("WiFi Qos =[ " + wifi_level + " ] ,  Mobile Qos =[ " + mobile_level + "]");
        if (mobile_level == 0) {
            return DEFAULT_WIFI_PRO_ENABLED;
        }
        if (this.mIsWiFiNoInternet) {
            if (INVALID_PID >= mobile_level) {
                z = DEFAULT_WIFI_PRO_ENABLED;
            }
            return z;
        }
        if (wifi_level >= mobile_level) {
            z = DEFAULT_WIFI_PRO_ENABLED;
        }
        return z;
    }

    private boolean isMobileDataConnected() {
        if (HANDOVER_5G_DIFFERENCE_SCORE == this.mTelephonyManager.getSimState() && this.mIsMobileDataEnabled) {
            return DBG;
        }
        return DEFAULT_WIFI_PRO_ENABLED;
    }

    private synchronized boolean isWifiConnected() {
        if (this.mWifiManager.isWifiEnabled()) {
            WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
            if (!(conInfo == null || conInfo.getNetworkId() == INVALID_PID || conInfo.getBSSID() == null || ILLEGAL_BSSID_02.equals(conInfo.getBSSID()))) {
                if (conInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    return DBG;
                }
            }
        }
        return DEFAULT_WIFI_PRO_ENABLED;
    }

    private boolean isNotShowIntelligentDialog() {
        logD("mIsWiFiProConnected = " + this.mIsWiFiProConnected);
        return this.mIsWiFiProConnected;
    }

    public static void putConnectWifiAppPid(Context context, int pid) {
    }

    private boolean isKeepCurrWiFiConnected() {
        if (this.mIsVpnWorking) {
            logW("vpn is working,shuld keep current connect");
        }
        return (this.mIsVpnWorking || this.mIsUserHandoverWiFi) ? DBG : isAppinWhitelists();
    }

    private boolean isAllowWiFiAutoEvaluate() {
        if (this.mIsWiFiProAutoEvaluateAP) {
        }
        if (!this.mIsWiFiProEnabled || this.mIsVpnWorking) {
            return DEFAULT_WIFI_PRO_ENABLED;
        }
        return DBG;
    }

    private void refreshConnectedNetWork() {
        if (isWifiConnected()) {
            WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
            this.mCurrWifiInfo = conInfo;
            if (conInfo != null) {
                this.mCurrentBssid = conInfo.getBSSID();
                this.mCurrentSsid = conInfo.getSSID();
                this.mCurrentRssi = conInfo.getRssi();
                List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
                if (configNetworks != null) {
                    for (WifiConfiguration config : configNetworks) {
                        if (config.networkId == conInfo.getNetworkId()) {
                            this.mCurrentWifiConfig = config;
                        }
                    }
                }
                return;
            }
        }
        this.mCurrentBssid = null;
        this.mCurrentSsid = null;
        this.mCurrentRssi = WifiHandover.INVALID_RSSI;
    }

    private boolean isAllowWifi2Mobile() {
        if (this.mIsWiFiProEnabled && this.mIsPrimaryUser && isMobileDataConnected() && this.mPowerManager.isScreenOn() && this.mEmuiPdpSwichValue != WIFI_HANDOVER_TIMERS) {
            return DBG;
        }
        return DEFAULT_WIFI_PRO_ENABLED;
    }

    private boolean isPdpAvailable() {
        if (SystemProperties.getBoolean(SYS_PROPERT_PDP, DEFAULT_WIFI_PRO_ENABLED)) {
            logD("SYS_PROPERT_PDP RemindWifiToPdp is true");
            return DBG;
        }
        logD("SYS_PROPERT_PDP RemindWifiToPdp is false");
        return DEFAULT_WIFI_PRO_ENABLED;
    }

    private String getAppName(int pid) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private boolean isAppinWhitelists() {
        if (this.mCurrentWifiConfig != null) {
            String currAppName = this.mCurrentWifiConfig.lastUpdateName;
            if (TextUtils.isEmpty(currAppName)) {
                currAppName = this.mCurrentWifiConfig.creatorName;
            }
            if (!(TextUtils.isEmpty(currAppName) || this.mAppWhitelists == null)) {
                for (String str : this.mAppWhitelists) {
                    if (currAppName.equals(str)) {
                        logD("curr name in the  Whitelists ");
                        return DBG;
                    }
                }
            }
        }
        return DEFAULT_WIFI_PRO_ENABLED;
    }

    private void resetWifiProManualConnect() {
        if (this.mIsManualConnectedWiFi) {
            System.putInt(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public static boolean isWifiEvaluating() {
        return !mIsWifiManualEvaluating ? mIsWifiSemiAutoEvaluating : DBG;
    }

    private boolean isSettingsActivity() {
        List<RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(WIFI_CHECK_UNKNOW_TIMER);
        if (!(runningTaskInfos == null || runningTaskInfos.isEmpty())) {
            ComponentName cn = ((RunningTaskInfo) runningTaskInfos.get(VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG)).topActivity;
            return (cn == null || cn.getClassName() == null || !cn.getClassName().startsWith(HUAWEI_SETTINGS)) ? DEFAULT_WIFI_PRO_ENABLED : DBG;
        }
    }

    public void setWifiApEvaluateEnabled(boolean enable) {
        logD("setWifiApEvaluateEnabled enabled " + enable);
        logD("system can not eavluate ap, ignor setting cmd");
    }

    private void setWifiEvaluateTag(boolean evaluate) {
        logD("setWifiEvaluateTag Tag :" + evaluate);
        Secure.putInt(this.mContentResolver, WIFI_EVALUATE_TAG, evaluate ? WIFI_CHECK_UNKNOW_TIMER : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
    }

    private boolean restoreWiFiConfig() {
        this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, DEFAULT_WIFI_PRO_ENABLED);
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        NetworkInfo wifi_info = this.mConnectivityManager.getNetworkInfo(WIFI_CHECK_UNKNOW_TIMER);
        if (wifi_info == null || wifi_info.getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
            return DEFAULT_WIFI_PRO_ENABLED;
        }
        this.mWifiManager.disconnect();
        return DBG;
    }

    public synchronized void onNetworkQosChange(int type, int level) {
        if (WIFI_CHECK_UNKNOW_TIMER == type) {
            this.mCurrentWifiLevel = level;
            logD("receive wifi Qos: currentWifiLevel == " + this.mCurrentWifiLevel + ", mIsWiFiNoInternet = " + this.mIsWiFiNoInternet);
            sendMessage(EVENT_WIFI_QOS_CHANGE, level);
            if (this.mIsWiFiNoInternet && WIFI_TO_WIFI_THRESHOLD == level) {
                updateWifiInternetStateChange(level);
            }
        } else if (type == 0) {
            sendMessage(EVENT_MOBILE_QOS_CHANGE, level);
        }
    }

    public synchronized void onNetworkDetectionResult(int type, int level) {
        if (WIFI_CHECK_UNKNOW_TIMER == type) {
            logD("wifi Detection level == " + level);
            if (isKeepCurrWiFiConnected()) {
                logD("keep curreny connect,ignore wifi check result");
                sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, HANDOVER_5G_DIFFERENCE_SCORE);
                return;
            }
            if (WifiproUtils.NET_INET_QOS_LEVEL_UNKNOWN == level) {
                sendMessage(EVENT_WIFI_CHECK_UNKOWN, level);
                return;
            } else if (isWifiEvaluating()) {
                sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, level);
            } else {
                if (7 == level) {
                    level = INVALID_PID;
                }
                if (!this.mIsPoorRssiRequestCheckWiFi) {
                    updateWifiInternetStateChange(level);
                    sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, level);
                } else if (INVALID_PID == level) {
                    onNetworkQosChange(WIFI_CHECK_UNKNOW_TIMER, WIFI_CHECK_UNKNOW_TIMER);
                }
            }
        } else if (type == 0) {
            sendMessage(EVENT_CHECK_MOBILE_QOS_RESULT, level);
        }
    }

    public synchronized void onWifiHandoverChange(int type, boolean result, String bssid, int errorReason) {
        if (WIFI_CHECK_UNKNOW_TIMER == type) {
            if (result) {
                this.mWifiProStatisticsManager.increaseWiFiHandoverWiFiCount(this.mWifiToWifiType);
            }
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_WIFI_HANDOVER_WIFI_RESULT, Boolean.valueOf(result));
        } else if (WIFI_SCAN_COUNT == type) {
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_DUALBAND_WIFI_HANDOVER_RESULT, errorReason);
        }
    }

    public synchronized void onDualBandNetWorkType(int type, List<HwDualBandMonitorInfo> apList) {
        if (apList != null) {
            if (apList.size() != 0) {
                if (this.mDisableDuanBandHandover) {
                    logD("keep curreny connect,ignore dualband ap handover");
                    return;
                }
                logD("onDualBandNetWorkType type = " + type + " apList.size() = " + apList.size());
                this.mDualBandMonitorApList.clear();
                this.mDualBandMonitorInfoSize = apList.size();
                for (HwDualBandMonitorInfo monitorInfo : apList) {
                    if (this.mCurrentWifiConfig == null || this.mCurrentWifiConfig.SSID == null || !this.mCurrentWifiConfig.SSID.equals(monitorInfo.mSsid) || this.mCurrentWifiConfig.allowedKeyManagement.cardinality() > WIFI_CHECK_UNKNOW_TIMER || this.mCurrentWifiConfig.getAuthType() != monitorInfo.mAuthType) {
                        this.mDualBandMonitorApList.add(monitorInfo);
                        WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                        apInfo.setApBssid(monitorInfo.mBssid);
                        apInfo.setApRssi(monitorInfo.mCurrentRssi);
                        apInfo.setApAuthType(monitorInfo.mAuthType);
                        this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
                    } else {
                        logD("onDualBandNetWorkType 5G and 2.4G AP have the same ssid and auth type");
                    }
                }
                initMonitorApListCHR();
                return;
            }
        }
        loge("onDualBandNetWorkType apList null error");
    }

    public synchronized void onDualBandNetWorkFind(List<HwDualBandMonitorInfo> apList) {
        if (apList != null) {
            if (apList.size() != 0) {
                if (this.mDualBandMonitorStart) {
                    logD("onDualBandNetWorkFind  apList.size() = " + apList.size());
                    this.mDualBandMonitorStart = DEFAULT_WIFI_PRO_ENABLED;
                    this.mDualBandEstimateApList.clear();
                    this.mAvailable5GAPBssid = null;
                    this.mDualBandEstimateInfoSize = apList.size();
                    for (HwDualBandMonitorInfo monitorInfo : apList) {
                        WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                        apInfo.setApBssid(monitorInfo.mBssid);
                        apInfo.setEstimateApSsid(monitorInfo.mSsid);
                        apInfo.setApAuthType(monitorInfo.mAuthType);
                        apInfo.setApRssi(monitorInfo.mCurrentRssi);
                        apInfo.setDualbandAPType(monitorInfo.mIsDualbandAP);
                        this.mDualBandEstimateApList.add(apInfo);
                        this.mNetworkQosMonitor.getApHistoryQualityScore(apInfo);
                    }
                    refreshConnectedNetWork();
                    this.mDualBandEstimateInfoSize += WIFI_CHECK_UNKNOW_TIMER;
                    WifiProEstimateApInfo currentApInfo = new WifiProEstimateApInfo();
                    currentApInfo.setApBssid(this.mCurrentBssid);
                    currentApInfo.setEstimateApSsid(this.mCurrentSsid);
                    currentApInfo.setApRssi(this.mCurrentRssi);
                    currentApInfo.set5GAP(DEFAULT_WIFI_PRO_ENABLED);
                    this.mDualBandEstimateApList.add(currentApInfo);
                    this.mNetworkQosMonitor.getApHistoryQualityScore(currentApInfo);
                    return;
                }
            }
        }
        loge("onDualBandNetWorkFind apList null error or mDualBandMonitorStart = " + this.mDualBandMonitorStart);
    }

    public synchronized void onWifiBqeReturnRssiTH(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnRssiTH apInfo null error");
            return;
        } else {
            sendMessage(EVENT_DUALBAND_RSSITH_RESULT, apInfo);
            return;
        }
    }

    public synchronized void onWifiBqeReturnHistoryScore(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnHistoryScore apInfo null error");
            return;
        } else {
            sendMessage(EVENT_DUALBAND_SCORE_RESULT, apInfo);
            return;
        }
    }

    public synchronized void onWifiBqeReturnCurrentRssi(int rssi) {
        this.mDualBandManager.updateCurrentRssi(rssi);
    }

    private void retryDualBandAPMonitor() {
        this.mDualBandMonitorInfoSize = this.mDualBandMonitorApList.size();
        if (this.mDualBandMonitorInfoSize == 0) {
            loge("retry dual band monitor error, monitorinfo size is zero");
            return;
        }
        for (HwDualBandMonitorInfo monitorInfo : this.mDualBandMonitorApList) {
            WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
            apInfo.setApBssid(monitorInfo.mBssid);
            apInfo.setApRssi(monitorInfo.mCurrentRssi);
            this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
        }
    }

    private void updateDualBandMonitorInfo(WifiProEstimateApInfo apInfo) {
        for (HwDualBandMonitorInfo monitorInfo : this.mDualBandMonitorApList) {
            String bssid = monitorInfo.mBssid;
            if (bssid != null && bssid.equals(apInfo.getApBssid())) {
                monitorInfo.mTargetRssi = apInfo.getRetRssiTH();
                return;
            }
        }
    }

    private void updateDualBandEstimateInfo(WifiProEstimateApInfo apInfo) {
        for (WifiProEstimateApInfo estimateApInfo : this.mDualBandEstimateApList) {
            String bssid = estimateApInfo.getApBssid();
            if (bssid != null && bssid.equals(apInfo.getApBssid())) {
                estimateApInfo.setRetHistoryScore(apInfo.getRetHistoryScore());
                return;
            }
        }
    }

    private void chooseAvalibleDualBandAp() {
        logD("chooseAvalibleDualBandAp DualBandEstimateApList =" + this.mDualBandEstimateApList.toString());
        if (this.mDualBandEstimateApList.size() == 0 || this.mCurrentBssid == null) {
            Log.e(TAG, "chooseAvalibleDualBandAp ap size error");
            return;
        }
        this.mAvailable5GAPBssid = null;
        this.mAvailable5GAPSsid = null;
        this.mAvailable5GAPAuthType = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mDuanBandHandoverType = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        WifiProEstimateApInfo bestAp = new WifiProEstimateApInfo();
        int currentApScore = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        for (WifiProEstimateApInfo apInfo : this.mDualBandEstimateApList) {
            if (this.mCurrentBssid.equals(apInfo.getApBssid())) {
                currentApScore = apInfo.getRetHistoryScore();
                this.mDualbandCurrentAPInfo = apInfo;
            } else if (apInfo.getRetHistoryScore() > bestAp.getRetHistoryScore()) {
                bestAp = apInfo;
            }
        }
        logD("chooseAvalibleDualBandAp bestAp =" + bestAp.toString() + ", currentApScore =" + currentApScore);
        int score = bestAp.getRetHistoryScore();
        if (score >= HANDOVER_5G_DIRECTLY_SCORE && bestAp.getApRssi() >= HANDOVER_5G_DIRECTLY_RSSI) {
            this.mAvailable5GAPBssid = bestAp.getApBssid();
            this.mAvailable5GAPSsid = bestAp.getApSsid();
            this.mAvailable5GAPAuthType = bestAp.getApAuthType();
            this.mDuanBandHandoverType = bestAp.getDualbandAPType();
            this.mDualbandSelectAPInfo = bestAp;
        } else if (score >= currentApScore + HANDOVER_5G_DIFFERENCE_SCORE || (bestAp.getDualbandAPType() == WIFI_CHECK_UNKNOW_TIMER && bestAp.getApRssi() >= HANDOVER_5G_SINGLE_RSSI)) {
            this.mAvailable5GAPBssid = bestAp.getApBssid();
            this.mAvailable5GAPSsid = bestAp.getApSsid();
            this.mAvailable5GAPAuthType = bestAp.getApAuthType();
            this.mDuanBandHandoverType = bestAp.getDualbandAPType();
            this.mDualbandSelectAPInfo = bestAp;
        } else {
            reportDualbandStatisticsCHR(WIFI_TO_WIFI_THRESHOLD);
        }
        if (this.mAvailable5GAPSsid == null) {
            ArrayList<HwDualBandMonitorInfo> mDualBandDeleteList = new ArrayList();
            for (HwDualBandMonitorInfo monitorInfo : this.mDualBandMonitorApList) {
                String bssid = monitorInfo.mBssid;
                if (bssid != null && bssid.equals(bestAp.getApBssid()) && monitorInfo.mTargetRssi < HANDOVER_5G_MAX_RSSI) {
                    monitorInfo.mTargetRssi += 10;
                    break;
                } else if (monitorInfo.mCurrentRssi >= HANDOVER_5G_MAX_RSSI) {
                    mDualBandDeleteList.add(monitorInfo);
                }
            }
            if (mDualBandDeleteList.size() > 0) {
                for (HwDualBandMonitorInfo deleteInfo : mDualBandDeleteList) {
                    logD("remove mix AP for RSSI > -45 DB RSSi = " + deleteInfo.mSsid);
                    this.mDualBandMonitorApList.remove(deleteInfo);
                }
            }
            if (this.mDualBandMonitorApList.size() != 0) {
                this.mDualBandMonitorStart = DBG;
                this.mDualBandManager.startMonitor(this.mDualBandMonitorApList);
            }
        } else if (this.mHwDualBandBlackListMgr.isInWifiBlacklist(this.mAvailable5GAPSsid)) {
            long expiretime = this.mHwDualBandBlackListMgr.getExpireTimeForRetry(this.mAvailable5GAPSsid);
            logD("getExpireTimeForRetry for ssid " + this.mAvailable5GAPSsid + ", time =" + expiretime);
            sendMessageDelayed(EVENT_DUALBAND_DELAY_RETRY, expiretime);
            reportDualbandStatisticsCHR(WIFI_SCAN_COUNT);
        } else {
            logD("do dualband handover : " + bestAp.toString());
            sendMessage(EVENT_DUALBAND_5GAP_AVAILABLE);
        }
    }

    private void addDualBandBlackList(String ssid) {
        logD("addDualBandBlackList ssid = " + ssid + ", mDualBandConnectAPSsid = " + this.mDualBandConnectAPSsid);
        if (ssid == null || this.mDualBandConnectAPSsid == null || !this.mDualBandConnectAPSsid.equals(ssid)) {
            logD("addDualBandBlackList do nothing");
            return;
        }
        this.mDualBandConnectAPSsid = null;
        if (System.currentTimeMillis() - this.mDualBandConnectTime > 1800000) {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(ssid, DBG);
        } else {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(ssid, DEFAULT_WIFI_PRO_ENABLED);
        }
    }

    private void setDuanBandManualConnect() {
        if (this.mIsUserManualConnectAp) {
            logD("set mDisableDuanBandHandover true");
            this.mDisableDuanBandHandover = DBG;
            this.mIsUserManualConnectAp = DEFAULT_WIFI_PRO_ENABLED;
        }
    }

    private void startDualBandManager() {
        this.mDualBandManager.startDualBandManger();
    }

    private void stopDualBandManager() {
        stopDualBandMonitor();
        this.mDualBandManager.stopDualBandManger();
    }

    private void stopDualBandMonitor() {
        if (this.mDualBandMonitorStart) {
            this.mDualBandMonitorStart = DEFAULT_WIFI_PRO_ENABLED;
            this.mDualBandManager.stopMonitor();
        }
    }

    public int getNetwoksHandoverType() {
        return this.mWifiHandover.getNetwoksHandoverType();
    }

    private void sendNetworkCheckingStatus(String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void notifyNetworkCheckResult(int result) {
        int internet_level = result;
        if (result == HANDOVER_5G_DIFFERENCE_SCORE && this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, MessageUtil.CMD_START_SCAN)) {
            internet_level = 6;
        }
        sendNetworkCheckingStatus(MessageUtil.ACTION_NETWORK_CONDITIONS_MEASURED, MessageUtil.EXTRA_IS_INTERNET_READY, internet_level);
    }

    private int getDualbandType() {
        logW("getDualbandType mDualBandCloneMonitorApList.size() = " + this.mDualBandCloneMonitorApList.size());
        if (this.mDualBandCloneMonitorApList.size() == 0) {
            return DUALBAND_TYPE_UNKONW;
        }
        if (this.mDualBandCloneMonitorApList.size() == WIFI_CHECK_UNKNOW_TIMER && ((HwDualBandMonitorInfo) this.mDualBandCloneMonitorApList.get(VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG)).mIsDualbandAP == WIFI_CHECK_UNKNOW_TIMER) {
            return DUALBAND_TYPE_SINGLE;
        }
        return DUALBAND_TYPE_MIX;
    }

    private void reportDualbandStatisticsCHR(int type) {
        int dualBandType = getDualbandType();
        if (dualBandType != DUALBAND_TYPE_UNKONW) {
            logW("reportDualbandStatisticsCHR dualBandType = " + dualBandType + " type = " + type);
            switch (type) {
                case WIFI_CHECK_UNKNOW_TIMER /*1*/:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(8);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(PORTAL_CHECK_MAX_TIMERS);
                    break;
                    break;
                case WIFI_HANDOVER_TIMERS /*2*/:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(9);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(21);
                    break;
                    break;
                case WIFI_TO_WIFI_THRESHOLD /*3*/:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(7);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(19);
                    break;
                    break;
                case WIFI_SCAN_COUNT /*4*/:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(6);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(18);
                    break;
                    break;
            }
        }
    }

    private void reportDualbandInternetResultCHR(int result) {
        if (!this.mIsDualbandhandover) {
            return;
        }
        if (result == INVALID_PID || result == 6) {
            logW("reportDualbandVerifyCHR mIsDualbandhandover = " + this.mIsDualbandhandover + " result = " + result);
            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(27);
        }
    }

    private void reportDualbandVerifyCHR() {
        long timer = System.currentTimeMillis() - this.mDualbandhanoverTime;
        if (this.mIsDualbandhandover && timer <= ((long) DUALBAND_CHR_VERIFY_THRESHOLD)) {
            logW("reportDualbandVerifyCHR");
            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(29);
            uploadDuanBandException(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    private void reportDualbandConnectAP() {
        long timer = System.currentTimeMillis() - this.mDualbandhanoverTime;
        if (this.mIsDualbandhandover && this.mDisableDuanBandHandover && timer <= ((long) DUALBAND_CHR_HANDOVER_THRESHOLD)) {
            logW("reportDualbandConnectAP mIsDualbandhandover = " + this.mIsDualbandhandover + " mIsUserManualConnectAp = " + this.mIsUserManualConnectAp + " mDisableDuanBandHandover = " + this.mDisableDuanBandHandover + " timer = " + timer);
            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(30);
            uploadDuanBandException(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    private void initDualBandhanoverConnectedCHR() {
        logW("mDualBandMonitorApList.size() = " + this.mDualBandMonitorApList.size());
        logW("initDualBandhanoverCHR mIsDualbandhandoverSucc = " + this.mIsDualbandhandoverSucc);
        if (this.mIsDualbandhandoverSucc) {
            this.mIsDualbandhandover = DBG;
            this.mDualbandhanoverTime = System.currentTimeMillis();
        } else {
            reportDualbandConnectAP();
            initDualBandhanoverCHR();
        }
        this.mIsDualbandhandoverSucc = DEFAULT_WIFI_PRO_ENABLED;
    }

    private void initDualBandhanoverCHR() {
        logW("initDualBandhanoverCHR");
        this.mIsDualbandhandover = DEFAULT_WIFI_PRO_ENABLED;
        this.mDualbandhanoverTime = 0;
        this.mDualbandSelectAPInfo = null;
    }

    private void initMonitorApListCHR() {
        if (this.mDualBandMonitorApList.size() > 0) {
            this.mDualBandCloneMonitorApList = (ArrayList) this.mDualBandMonitorApList.clone();
        }
    }

    private void resetDualBandhanoverCHR() {
        logW("resetDualBandhanoverCHR");
        this.mIsDualbandhandover = DEFAULT_WIFI_PRO_ENABLED;
        this.mDualbandhanoverTime = 0;
        this.mDualbandSelectAPInfo = null;
        this.mDualbandCurrentAPInfo = null;
        this.mDualbandPingPangTime = 0;
        this.mDualbandRoveInConut = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mLastConnect5GAP = null;
        this.mDualBandCloneMonitorApList = null;
    }

    private void reportPingPangCHR(String bssid) {
        logW("reportPingPangCHR mLastConnect5GAP = " + this.mLastConnect5GAP + " bssid = " + bssid);
        this.mIsDualbandhandoverSucc = DBG;
        if (this.mLastConnect5GAP == null || this.mLastConnect5GAP.equals(bssid)) {
            logW("reportPingPangCHR mDualbandRoveInConut = " + this.mDualbandRoveInConut);
            if (this.mDualbandRoveInConut >= WIFI_TO_WIFI_THRESHOLD) {
                if (System.currentTimeMillis() - this.mDualbandPingPangTime <= ((long) DUALBAND_CHR_PINGPANG_THRESHOLD)) {
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(31);
                    uploadDuanBandException(WifiProStatisticsManager.SUB_EVENT_DUALBAND_HANDOVER_PINGPONG, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                }
                this.mDualbandPingPangTime = System.currentTimeMillis();
                this.mDualbandRoveInConut = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                return;
            }
            this.mDualbandRoveInConut += WIFI_CHECK_UNKNOW_TIMER;
            return;
        }
        this.mLastConnect5GAP = bssid;
        this.mDualbandPingPangTime = System.currentTimeMillis();
        this.mDualbandRoveInConut = WIFI_CHECK_UNKNOW_TIMER;
    }

    private void uploadDuanBandException(String event, int reason) {
        logW("uploadDuanBandException");
        if (this.mDualbandSelectAPInfo != null && this.mDualbandCurrentAPInfo != null) {
            WifiProDualbandExceptionRecord rec = new WifiProDualbandExceptionRecord();
            rec.mSSID_2G = this.mDualbandCurrentAPInfo.getApSsid();
            rec.mSSID_5G = this.mDualbandSelectAPInfo.getApSsid();
            rec.mSingleOrMixed = (short) this.mDualbandSelectAPInfo.getDualbandAPType();
            rec.mRSSI_2G = (short) this.mDualbandCurrentAPInfo.getApRssi();
            rec.mRSSI_5G = (short) this.mDualbandSelectAPInfo.getApRssi();
            rec.mTarget_RSSI_5G = (short) this.mDualbandSelectAPInfo.getRetRssiTH();
            rec.mScore_2G = (short) this.mDualbandCurrentAPInfo.getRetHistoryScore();
            rec.mScore_5G = (short) this.mDualbandSelectAPInfo.getRetHistoryScore();
            rec.mHandOverErrCode = (short) reason;
            this.mWifiProStatisticsManager.uploadWifiProDualbandExceptionEvent(event, rec);
        }
    }

    public void onWifiConnected(boolean result, int reason) {
    }

    public void onCheckAvailableWifi(boolean exist, int bestRssi, String targetSsid) {
        if (!isKeepCurrWiFiConnected()) {
            if (exist && this.mNetworkBlackListManager.containedInWifiBlacklists(targetSsid)) {
                logW("onCheckAvailableWifi, but wifi blacklists contain it, ignore the result.");
                exist = DEFAULT_WIFI_PRO_ENABLED;
            }
            sendMessage(EVENT_CHECK_AVAILABLE_AP_RESULT, bestRssi, bestRssi, Boolean.valueOf(exist));
        }
    }

    public void onWifiBqeDetectionResult(int result) {
        logD("onWifiBqeDetectionResult =  " + result);
        sendMessage(EVENT_WIFI_EVALUTE_TCPRTT_RESULT, result);
    }

    public void onNotifyWifiSecurityStatus(Bundle bundle) {
        logD("onNotifyWifiSecurityStatus, bundle =  " + bundle);
        sendMessage(EVENT_WIFI_SECURITY_RESPONSE, bundle);
    }

    public synchronized void onUserConfirm(int type) {
        if (WIFI_HANDOVER_TIMERS == type) {
            logD("UserConfirm  is OK ");
            sendMessage(EVENT_DIALOG_OK);
        } else if (WIFI_CHECK_UNKNOW_TIMER == type) {
            logD("UserConfirm  is CANCEL");
            sendMessage(EVENT_DIALOG_CANCEL);
        }
    }

    public synchronized void userHandoverWifi() {
        logD("User Chose Rove In WiFi");
        sendMessage(EVENT_USER_ROVE_IN);
    }

    public void notifyHttpReachable(boolean isReachable) {
        logD("SEC notifyHttpReachable " + isReachable);
        sendMessage(EVENT_HTTP_REACHABLE_RESULT, Boolean.valueOf(isReachable));
    }

    private void logD(String info) {
        Log.d(TAG, info);
    }

    private void logW(String info) {
        Log.w(TAG, info);
    }

    public static void resetParameter() {
        mIsWifiManualEvaluating = DEFAULT_WIFI_PRO_ENABLED;
        mIsWifiSemiAutoEvaluating = DEFAULT_WIFI_PRO_ENABLED;
    }

    public void onDisableWiFiPro() {
        logD("WiFiProDisabledState is Enter");
        resetParameter();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mSampleCollectionManager.unRegisterBroadcastReceiver();
        this.mWiFiProProtalController.handleWifiProStatusChanged(DEFAULT_WIFI_PRO_ENABLED, this.mIsPortalAp);
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mWifiProUIDisplayManager.shownAccessNotification(DEFAULT_WIFI_PRO_ENABLED);
        this.mWiFiProEvaluateController.cleanEvaluateRecords();
        this.mHwIntelligenceWiFiManager.stop();
        stopDualBandManager();
        if (isWifiConnected()) {
            logD("WiFiProDisabledState , wifi is connect ");
            WifiInfo cInfo = this.mWifiManager.getConnectionInfo();
            if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                logD("wifi State == VERIFYING_POOR_LINK");
                this.mWsmChannel.sendMessage(GOOD_LINK_DETECTED);
            }
            setWifiCSPState(WIFI_CHECK_UNKNOW_TIMER);
        }
        diableResetVariables();
        disableTransitionNetState();
    }

    private void diableResetVariables() {
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, DEFAULT_WIFI_PRO_ENABLED, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG, DEFAULT_WIFI_PRO_ENABLED);
        this.mWifiProUIDisplayManager.notificateNetAccessChange(DEFAULT_WIFI_PRO_ENABLED);
        this.mLastWifiLevel = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mIsWiFiNoInternet = DEFAULT_WIFI_PRO_ENABLED;
        this.mWiFiProPdpSwichValue = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        reSetWifiInternetState();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mIsUserHandoverWiFi = DEFAULT_WIFI_PRO_ENABLED;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = DEFAULT_WIFI_PRO_ENABLED;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    private void disableTransitionNetState() {
        if (isWifiConnected()) {
            logD("onDisableWiFiPro,go to WifiConnectedState");
            this.mIsWiFiProConnected = DEFAULT_WIFI_PRO_ENABLED;
            this.mNetworkQosMonitor.queryNetworkQos(WIFI_CHECK_UNKNOW_TIMER, this.mIsPortalAp, this.mIsNetworkAuthen);
            transitionTo(this.mWifiConnectedState);
            return;
        }
        logD("onDisableWiFiPro, go to mWifiDisConnectedState");
        transitionTo(this.mWifiDisConnectedState);
    }

    private void setWifiMonitorEnabled(boolean enabled) {
        logD("setWifiLinkDataMonitorEnabled  is " + enabled);
        this.mNetworkQosMonitor.setMonitorWifiQos(WIFI_CHECK_UNKNOW_TIMER, enabled);
        this.mNetworkQosMonitor.setIpQosEnabled(enabled);
    }

    private boolean isFullscreen() {
        boolean isFullscreen = DEFAULT_WIFI_PRO_ENABLED;
        if (this.mAbsPhoneWindowManager != null) {
            isFullscreen = this.mAbsPhoneWindowManager.isTopIsFullscreen();
        }
        logD("isFullscreen: " + isFullscreen);
        return isFullscreen;
    }
}
