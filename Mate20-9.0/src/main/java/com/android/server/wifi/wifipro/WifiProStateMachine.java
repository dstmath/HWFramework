package com.android.server.wifi.wifipro;

import android.app.ActivityManager;
import android.app.Notification;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HiLinkUtil;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.LocalServices;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.hidata.mplink.HwMplinkManager;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wifi.HwPortalExceptionManager;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.LAA.HwLaaController;
import com.android.server.wifi.LAA.HwLaaUtils;
import com.android.server.wifi.SavedNetworkEvaluator;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WifiProStateMachine extends StateMachine implements INetworkQosCallBack, INetworksHandoverCallBack, IWifiProUICallBack, IDualBandManagerCallback {
    private static final int ACCESS_TYPE = 1;
    private static final boolean AUTO_EVALUATE_SWITCH = false;
    private static final int BASE = 136168;
    private static final boolean BQE_TEST = false;
    private static final int CHR_AVAILIABLE_AP_COUNTER = 2;
    private static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final String COUNTRY_CODE_CN = "460";
    private static final int CSP_INVISIBILITY = 0;
    private static final int CSP_VISIBILITY = 1;
    private static final boolean DBG = true;
    private static final boolean DDBG = false;
    private static final boolean DEFAULT_WIFI_PRO_ENABLED = false;
    private static final int DELAYED_TIME_LAUNCH_BROWSER = 500;
    private static final int DELAY_EVALUTE_NEXT_AP_TIME = 2000;
    private static final int DELAY_PERIODIC_PORTAL_CHECK_TIME_FAST = 2000;
    private static final int DELAY_PERIODIC_PORTAL_CHECK_TIME_SLOW = 600000;
    private static final int DELAY_START_WIFI_EVALUTE_TIME = 6000;
    private static final long DELAY_UPLOAD_MS = 120000;
    private static final String DOCOMO_HW_OPTA = "341";
    private static final String DOCOMO_HW_OPTB = "392";
    private static final int DUALBAND_HANDOVER_FAILED_COUNT = 2;
    private static final int DUALBAND_HANDOVER_INBLACK_LIST_COUNT = 4;
    private static final int DUALBAND_HANDOVER_SCORE_NOT_SATISFY_COUNT = 3;
    private static final int DUALBAND_HANDOVER_SUC_COUNT = 1;
    private static final int EVALUATE_ALL_TIMEOUT = 75000;
    private static final int EVALUATE_VALIDITY_TIMEOUT = 120000;
    private static final int EVALUATE_WIFI_CONNECTED_TIMEOUT = 35000;
    private static final int EVALUATE_WIFI_RTT_BQE_INTERVAL = 3000;
    private static final int EVENT_BQE_ANALYZE_NETWORK_QUALITY = 136317;
    private static final int EVENT_CALL_STATE_CHANGED = 136201;
    private static final int EVENT_CHECK_AVAILABLE_AP_RESULT = 136176;
    private static final int EVENT_CHECK_MOBILE_QOS_RESULT = 136180;
    private static final int EVENT_CHECK_PORTAL_AUTH_CHECK_RESULT = 136208;
    private static final int EVENT_CHECK_WIFI_INTERNET = 136192;
    private static final int EVENT_CHECK_WIFI_INTERNET_RESULT = 136181;
    private static final int EVENT_CHR_ALARM_EXPIRED = 136321;
    private static final int EVENT_CONFIGURATION_CHANGED = 136197;
    private static final int EVENT_CONFIGURED_NETWORKS_CHANGED = 136308;
    public static final int EVENT_DELAY_EVALUTE_NEXT_AP = 136314;
    private static final int EVENT_DELAY_REINITIALIZE_WIFI_MONITOR = 136184;
    private static final int EVENT_DEVICE_SCREEN_OFF = 136206;
    private static final int EVENT_DEVICE_SCREEN_ON = 136170;
    private static final int EVENT_DEVICE_USER_PRESENT = 136207;
    private static final int EVENT_DIALOG_CANCEL = 136183;
    private static final int EVENT_DIALOG_OK = 136182;
    private static final int EVENT_DUALBAND_5GAP_AVAILABLE = 136370;
    private static final int EVENT_DUALBAND_DELAY_RETRY = 136372;
    private static final int EVENT_DUALBAND_NETWROK_TYPE = 136316;
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
    private static final int EVENT_LAA_STATUS_CHANGED = 136200;
    private static final int EVENT_LAST_EVALUTE_VALID = 136302;
    private static final int EVENT_LAUNCH_BROWSER = 136320;
    public static final int EVENT_LOAD_CONFIG_INTERNET_INFO = 136315;
    private static final int EVENT_MOBILE_CONNECTIVITY = 136175;
    private static final int EVENT_MOBILE_DATA_STATE_CHANGED_ACTION = 136186;
    private static final int EVENT_MOBILE_QOS_CHANGE = 136173;
    private static final int EVENT_MOBILE_RECOVERY_TO_WIFI = 136189;
    private static final int EVENT_MOBILE_SWITCH_DELAY = 136194;
    private static final int EVENT_NETWORK_CONNECTIVITY_CHANGE = 136177;
    private static final int EVENT_NETWORK_USER_CONNECT = 136202;
    private static final int EVENT_NOTIFY_WIFI_LINK_POOR = 136198;
    private static final int EVENT_PERIODIC_PORTAL_CHECK_FAST = 136205;
    private static final int EVENT_PERIODIC_PORTAL_CHECK_SLOW = 136204;
    private static final int EVENT_PORTAL_SELECTED = 136319;
    private static final int EVENT_REQUEST_SCAN_DELAY = 136196;
    private static final int EVENT_RETRY_WIFI_TO_WIFI = 136191;
    private static final int EVENT_SCAN_RESULTS_AVAILABLE = 136293;
    private static final int EVENT_START_BQE = 136306;
    private static final int EVENT_SUPPLICANT_STATE_CHANGE = 136297;
    private static final int EVENT_TRY_WIFI_ROVE_OUT = 136199;
    private static final int EVENT_USER_ROVE_IN = 136193;
    private static final int EVENT_WIFIPRO_EVALUTE_STATE_CHANGE = 136298;
    private static final int EVENT_WIFIPRO_WORKING_STATE_CHANGE = 136171;
    private static final int EVENT_WIFI_CHECK_UNKOWN = 136309;
    private static final int EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED = 136203;
    private static final int EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT = 136301;
    private static final int EVENT_WIFI_EVALUTE_TCPRTT_RESULT = 136299;
    private static final int EVENT_WIFI_GOOD_INTERVAL_TIMEOUT = 136187;
    private static final int EVENT_WIFI_HANDOVER_WIFI_RESULT = 136178;
    private static final int EVENT_WIFI_NETWORK_STATE_CHANGE = 136169;
    private static final int EVENT_WIFI_NO_INTERNET_NOTIFICATION = 136318;
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
    private static final String HWSYNC_DEVICE_CONNECTED_KEY = "huaweishare_device_connected";
    private static final String ILLEGAL_BSSID_01 = "any";
    private static final String ILLEGAL_BSSID_02 = "00:00:00:00:00:00";
    private static final int INVALID_CHR_RCD_TIME = 0;
    private static final int INVALID_LINK_DETECTED = 131875;
    private static final int INVALID_PID = -1;
    /* access modifiers changed from: private */
    public static final boolean IS_DOCOMO;
    private static final int JUDGE_WIFI_FAST_REOPEN_TIME = 30000;
    private static final String KEY_EMUI_WIFI_TO_PDP = "wifi_to_pdp";
    private static final int KEY_MOBILE_HANDOVER_WIFI = 2;
    public static final String KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY = "wifipro_manual_connect_ap_configkey";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK = "wifipro_auto_recommend";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK_SAVED_STATE = "wifipro_auto_recommend_saved_state";
    private static final int KEY_WIFI_HANDOVER_MOBILE = 1;
    private static final int LAST_CONNECTED_NETWORK_EXPIRATION_AGE_MILLIS = 10000;
    private static final int LAST_WIFIPRO_DISABLE_EXPIRATION_AGE_MILLIS = 7200000;
    private static final String MAPS_LOCATION_FLAG = "hw_higeo_maps_location";
    private static final int MILLISECONDS_OF_ONE_SECOND = 1000;
    private static final int MOBILE = 0;
    private static final int MOBILE_DATA_OFF_SWITCH_DELAY_MS = 3000;
    private static final int NETWORK_POOR_LEVEL_THRESHOLD = 2;
    /* access modifiers changed from: private */
    public static final int[] NORMAL_SCAN_INTERVAL = {15000, 15000, 30000};
    /* access modifiers changed from: private */
    public static final int[] NORMAL_SCAN_MAX_COUNTER = {4, 4, 2};
    private static final int OOBE_COMPLETE = 1;
    private static final int POOR_LINK_DETECTED = 131873;
    private static final int PORTAL_HANDOVER_DELAY_TIME = 15000;
    private static final String PORTAL_STATUS_BAR_TAG = "wifipro_portal_expired_status_bar";
    private static final int QOS_LEVEL = 2;
    private static final int QOS_SCORE = 3;
    /* access modifiers changed from: private */
    public static final int[] QUICK_SCAN_INTERVAL = {LAST_CONNECTED_NETWORK_EXPIRATION_AGE_MILLIS, LAST_CONNECTED_NETWORK_EXPIRATION_AGE_MILLIS, 15000};
    /* access modifiers changed from: private */
    public static final int[] QUICK_SCAN_MAX_COUNTER = {20, 20, 10};
    private static final String RESP_CODE_INTERNET_AVAILABLE = "204";
    private static final String RESP_CODE_INTERNET_UNREACHABLE = "599";
    private static final String RESP_CODE_PORTAL = "302";
    private static final int SEND_MESSAGE_DELAY_TIME = 30000;
    private static final String SETTING_SECURE_CONN_WIFI_PID = "wifipro_connect_wifi_app_pid";
    private static final String SETTING_SECURE_VPN_WORK_VALUE = "wifipro_network_vpn_state";
    private static final String SETTING_SECURE_WIFI_NO_INT = "wifi_no_internet_access";
    private static final int SIGNAL_LEVEL_3 = 3;
    private static final int SYSTEM_UID = 1000;
    private static final String SYS_OPER_CMCC = "ro.config.operators";
    private static final String SYS_PROPERT_PDP = "hw_RemindWifiToPdp";
    private static final String TAG = "WiFi_PRO_WifiProStateMachine";
    private static final int TCP_IP = 101;
    private static final int THRESHOD_RSSI = -82;
    private static final int THRESHOD_RSSI_HIGH = -76;
    private static final int THRESHOD_RSSI_LOW = -88;
    private static final int TURN_OFF_MOBILE = 0;
    private static final int TURN_OFF_WIFI = 1;
    private static final int TURN_OFF_WIFI_PRO = 2;
    private static final int TURN_ON_WIFI_PRO = 3;
    private static final int TYPE_USER_PREFERENCE = 1;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    private static final String VEHICLE_STATE_FLAG = "hw_higeo_vehicle_state";
    private static final int WIFI = 1;
    private static final int WIFI_CHECK_DELAY_TIME = 30000;
    private static final int WIFI_CHECK_UNKNOW_TIMER = 1;
    private static final String WIFI_CSP_DISPALY_STATE = "wifi_csp_dispaly_state";
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_GOOD_LINK_MAX_TIME_LIMIT = 1800000;
    private static final int WIFI_HANDOVER_MOBILE_TIMER_LIMIT = 4;
    private static final int WIFI_HANDOVER_TIMERS = 2;
    private static final int WIFI_NO_INTERNET_DIVISOR = 4;
    private static final int WIFI_NO_INTERNET_MAX = 12;
    private static final int WIFI_REPEATER_OPEN = 1;
    private static final int WIFI_REPEATER_OPEN_GO_WITHOUT_THTHER = 6;
    private static final int WIFI_SCAN_COUNT = 4;
    private static final int WIFI_SCAN_INTERVAL_MAX = 12;
    private static final long WIFI_SWITCH_RECORD_MAX_TIME = 1209600000;
    private static final int WIFI_TCPRX_STATISTICS_INTERVAL = 5000;
    private static final int WIFI_TO_WIFI_THRESHOLD = 3;
    private static final int WIFI_VERYY_INTERVAL_TIME = 30000;
    /* access modifiers changed from: private */
    public static boolean mIsWifiManualEvaluating = false;
    /* access modifiers changed from: private */
    public static boolean mIsWifiSemiAutoEvaluating = false;
    private static WifiProStateMachine mWifiProStateMachine;
    /* access modifiers changed from: private */
    public long connectStartTime;
    /* access modifiers changed from: private */
    public int detectionNumSlow;
    /* access modifiers changed from: private */
    public boolean isDialogUpWhenConnected;
    /* access modifiers changed from: private */
    public boolean isMapNavigating;
    /* access modifiers changed from: private */
    public boolean isPeriodicDet;
    private boolean isVariableInited;
    /* access modifiers changed from: private */
    public boolean isVehicleState;
    private AbsPhoneWindowManager mAbsPhoneWindowManager;
    private List<String> mAppWhitelists;
    /* access modifiers changed from: private */
    public int mAvailable5GAPAuthType = 0;
    /* access modifiers changed from: private */
    public String mAvailable5GAPBssid = null;
    /* access modifiers changed from: private */
    public String mAvailable5GAPSsid = null;
    /* access modifiers changed from: private */
    public String mBadBssid;
    /* access modifiers changed from: private */
    public String mBadSsid;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public long mChrRoveOutStartTime = 0;
    /* access modifiers changed from: private */
    public long mChrWifiDidableStartTime = 0;
    /* access modifiers changed from: private */
    public long mChrWifiDisconnectStartTime = 0;
    private boolean mCloseBySystemui = false;
    /* access modifiers changed from: private */
    public int mConnectWiFiAppPid;
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public WifiInfo mCurrWifiInfo;
    /* access modifiers changed from: private */
    public String mCurrentBssid;
    /* access modifiers changed from: private */
    public int mCurrentRssi;
    /* access modifiers changed from: private */
    public String mCurrentSsid;
    /* access modifiers changed from: private */
    public int mCurrentVerfyCounter;
    /* access modifiers changed from: private */
    public WifiConfiguration mCurrentWifiConfig;
    /* access modifiers changed from: private */
    public int mCurrentWifiLevel;
    private DefaultState mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public boolean mDelayedRssiChangedByCalling = false;
    /* access modifiers changed from: private */
    public boolean mDelayedRssiChangedByFullScreen = false;
    /* access modifiers changed from: private */
    public String mDualBandConnectAPSsid = null;
    /* access modifiers changed from: private */
    public long mDualBandConnectTime;
    private ArrayList<WifiProEstimateApInfo> mDualBandEstimateApList = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mDualBandEstimateInfoSize = 0;
    HwDualBandManager mDualBandManager;
    /* access modifiers changed from: private */
    public ArrayList<HwDualBandMonitorInfo> mDualBandMonitorApList = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mDualBandMonitorInfoSize = 0;
    /* access modifiers changed from: private */
    public boolean mDualBandMonitorStart = false;
    /* access modifiers changed from: private */
    public int mDuanBandHandoverType = 0;
    /* access modifiers changed from: private */
    public volatile int mEmuiPdpSwichValue;
    private BroadcastReceiver mHMDBroadcastReceiver;
    private IntentFilter mHMDIntentFilter;
    /* access modifiers changed from: private */
    public boolean mHiLinkUnconfig = false;
    /* access modifiers changed from: private */
    public HwDualBandBlackListManager mHwDualBandBlackListMgr;
    /* access modifiers changed from: private */
    public HwIntelligenceWiFiManager mHwIntelligenceWiFiManager;
    private HwQoEService mHwQoEService;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public boolean mIsAllowEvaluate;
    /* access modifiers changed from: private */
    public boolean mIsMobileDataEnabled;
    /* access modifiers changed from: private */
    public boolean mIsNetworkAuthen;
    /* access modifiers changed from: private */
    public boolean mIsP2PConnectedOrConnecting;
    /* access modifiers changed from: private */
    public boolean mIsPortalAp;
    /* access modifiers changed from: private */
    public boolean mIsPrimaryUser;
    /* access modifiers changed from: private */
    public boolean mIsRoveOutToDisconn = false;
    /* access modifiers changed from: private */
    public boolean mIsScanedRssiLow;
    /* access modifiers changed from: private */
    public boolean mIsScanedRssiMiddle;
    /* access modifiers changed from: private */
    public boolean mIsUserHandoverWiFi;
    /* access modifiers changed from: private */
    public boolean mIsUserManualConnectSuccess = false;
    /* access modifiers changed from: private */
    public volatile boolean mIsVpnWorking;
    /* access modifiers changed from: private */
    public boolean mIsWiFiInternetCHRFlag;
    /* access modifiers changed from: private */
    public boolean mIsWiFiNoInternet;
    /* access modifiers changed from: private */
    public boolean mIsWiFiProAutoEvaluateAP;
    /* access modifiers changed from: private */
    public boolean mIsWiFiProEnabled;
    /* access modifiers changed from: private */
    public boolean mIsWifiSemiAutoEvaluateComplete;
    /* access modifiers changed from: private */
    public boolean mIsWifiproDisableOnReboot;
    private int mLastCSPState;
    /* access modifiers changed from: private */
    public String mLastConnectedSsid = "";
    /* access modifiers changed from: private */
    public int mLastDisconnectedRssi;
    /* access modifiers changed from: private */
    public long mLastDisconnectedTime;
    /* access modifiers changed from: private */
    public long mLastDisconnectedTimeStamp = -1;
    /* access modifiers changed from: private */
    public int mLastWifiLevel;
    private long mLastWifiproDisableTime = 0;
    /* access modifiers changed from: private */
    public boolean mLoseInetRoveOut = false;
    /* access modifiers changed from: private */
    public String mManualConnectAp = "";
    private ContentObserver mMapNavigatingStateChangeObserver;
    /* access modifiers changed from: private */
    public boolean mNeedRetryMonitor;
    /* access modifiers changed from: private */
    public NetworkBlackListManager mNetworkBlackListManager;
    /* access modifiers changed from: private */
    public HwNetworkPropertyChecker mNetworkPropertyChecker = null;
    /* access modifiers changed from: private */
    public NetworkQosMonitor mNetworkQosMonitor = null;
    /* access modifiers changed from: private */
    public String mNewSelect_bssid;
    /* access modifiers changed from: private */
    public int mOpenAvailableAPCounter;
    /* access modifiers changed from: private */
    public boolean mPhoneStateListenerRegisted = false;
    /* access modifiers changed from: private */
    public int mPortalNotificationId;
    private String mPortalUsedUrl = null;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public String mRoSsid = null;
    /* access modifiers changed from: private */
    public boolean mRoveOutStarted = false;
    private SavedNetworkEvaluator mSavedNetworkEvaluator;
    /* access modifiers changed from: private */
    public List<ScanResult> mScanResultList;
    /* access modifiers changed from: private */
    public TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public String mUserManualConnecConfigKey = "";
    private ContentObserver mVehicleStateChangeObserver;
    /* access modifiers changed from: private */
    public boolean mVerfyingToConnectedState = false;
    /* access modifiers changed from: private */
    public WiFiLinkMonitorState mWiFiLinkMonitorState = new WiFiLinkMonitorState();
    /* access modifiers changed from: private */
    public int mWiFiNoInternetReason;
    /* access modifiers changed from: private */
    public WifiProCHRManager mWiFiProCHRMgr;
    private WiFiProDisabledState mWiFiProDisabledState = new WiFiProDisabledState();
    /* access modifiers changed from: private */
    public WiFiProEnableState mWiFiProEnableState = new WiFiProEnableState();
    /* access modifiers changed from: private */
    public WiFiProEvaluateController mWiFiProEvaluateController;
    /* access modifiers changed from: private */
    public volatile int mWiFiProPdpSwichValue;
    /* access modifiers changed from: private */
    public WiFiProVerfyingLinkState mWiFiProVerfyingLinkState = new WiFiProVerfyingLinkState();
    /* access modifiers changed from: private */
    public WifiConnectedState mWifiConnectedState = new WifiConnectedState();
    /* access modifiers changed from: private */
    public WifiDisConnectedState mWifiDisConnectedState = new WifiDisConnectedState();
    /* access modifiers changed from: private */
    public WifiHandover mWifiHandover;
    private WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiProConfigStore mWifiProConfigStore;
    private WifiProConfigurationManager mWifiProConfigurationManager;
    /* access modifiers changed from: private */
    public WifiProStatisticsManager mWifiProStatisticsManager;
    /* access modifiers changed from: private */
    public WifiProUIDisplayManager mWifiProUIDisplayManager;
    /* access modifiers changed from: private */
    public WifiSemiAutoEvaluateState mWifiSemiAutoEvaluateState = new WifiSemiAutoEvaluateState();
    /* access modifiers changed from: private */
    public WifiSemiAutoScoreState mWifiSemiAutoScoreState = new WifiSemiAutoScoreState();
    private WifiStateMachine mWifiStateMachine;
    /* access modifiers changed from: private */
    public int mWifiTcpRxCount;
    /* access modifiers changed from: private */
    public int mWifiToWifiType = 0;
    /* access modifiers changed from: private */
    public AsyncChannel mWsmChannel;
    /* access modifiers changed from: private */
    public WifiProPhoneStateListener phoneStateListener = null;
    /* access modifiers changed from: private */
    public String respCodeChrInfo;

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
            if (msg.what != WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE) {
                return false;
            }
            if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                WifiProStateMachine.this.onDisableWiFiPro();
            } else {
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
            }
            return true;
        }
    }

    class WiFiLinkMonitorState extends State {
        private int currWifiPoorlevel;
        private int detectCounter = 0;
        private int internetFailureDetectedCnt;
        private boolean isAllowWiFiHandoverMobile;
        private boolean isBQERequestCheckWiFi;
        private boolean isCancelCHRTypeReport;
        private boolean isCheckWiFiForUpdateSetting;
        private boolean isDialogDisplayed;
        private boolean isDisableWifiAutoSwitch = false;
        private boolean isNoInternetDialogShowing;
        private boolean isNotifyInvalidLinkDetection = false;
        private boolean isRequestWifInetCheck = false;
        private boolean isScreenOffMonitor;
        private boolean isSwitching;
        private boolean isToastDisplayed;
        private boolean isWiFiHandoverPriority;
        private boolean isWifi2MobileUIShowing;
        private boolean isWifi2WifiProcess;
        private int mLastUpdatedQosLevel = 0;
        private int mWifi2WifiThreshod = WifiHandover.INVALID_RSSI;
        private boolean portalCheck = false;
        private int rssiLevel0Or1ScanedCounter = 0;
        private int rssiLevel2ScanedCounter = 0;
        private long wifiLinkHoldTime;
        private int wifiMonitorCounter;

        WiFiLinkMonitorState() {
        }

        private void wiFiLinkMonitorStateInit(boolean internetRecheck) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("wiFiLinkMonitorStateInit is Start, internetRecheck = " + internetRecheck);
            String unused = WifiProStateMachine.this.mBadBssid = null;
            this.isSwitching = false;
            this.isWifi2WifiProcess = false;
            this.isWifi2MobileUIShowing = false;
            this.isCheckWiFiForUpdateSetting = false;
            this.isDialogDisplayed = false;
            this.isNoInternetDialogShowing = false;
            this.detectCounter = 0;
            WifiProStateMachine.this.setWifiCSPState(1);
            this.mLastUpdatedQosLevel = 0;
            if (!internetRecheck) {
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logD("mIsWiFiNoInternet is true,sendMessage wifi Qos is -1");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, -1, 0, false);
                } else {
                    HwSelfCureEngine.getInstance().notifyInternetAccessRecovery();
                    WifiProStateMachine.this.setWifiMonitorEnabled(true);
                }
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            boolean unused2 = WifiProStateMachine.this.mNeedRetryMonitor = false;
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiLinkMonitorState is Enter");
            NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifi_info != null && wifi_info.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logD(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
            }
            this.wifiMonitorCounter = 0;
            this.internetFailureDetectedCnt = 0;
            this.rssiLevel2ScanedCounter = 0;
            this.rssiLevel0Or1ScanedCounter = 0;
            this.isScreenOffMonitor = false;
            this.isAllowWiFiHandoverMobile = true;
            this.isCancelCHRTypeReport = false;
            this.isDisableWifiAutoSwitch = false;
            this.isRequestWifInetCheck = false;
            this.isNotifyInvalidLinkDetection = false;
            wiFiLinkMonitorStateInit(false);
            this.currWifiPoorlevel = 3;
            this.wifiLinkHoldTime = System.currentTimeMillis();
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime && (WifiProStateMachine.this.mChrWifiDisconnectStartTime > WifiProStateMachine.this.mChrRoveOutStartTime || WifiProStateMachine.this.mChrWifiDidableStartTime > WifiProStateMachine.this.mChrRoveOutStartTime)) {
                long disableRestoreTime = System.currentTimeMillis() - WifiProStateMachine.this.mChrWifiDidableStartTime;
                boolean ssidIsSame = false;
                if (!(WifiProStateMachine.this.mRoSsid == null || WifiProStateMachine.this.mCurrentSsid == null)) {
                    ssidIsSame = WifiProStateMachine.this.mRoSsid.equals(WifiProStateMachine.this.mCurrentSsid);
                }
                if (ssidIsSame && disableRestoreTime <= 30000) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserReopenWifiRiCount();
                }
            }
            long unused = WifiProStateMachine.this.mChrRoveOutStartTime = 0;
            long unused2 = WifiProStateMachine.this.mChrWifiDisconnectStartTime = 0;
            long unused3 = WifiProStateMachine.this.mChrWifiDidableStartTime = 0;
            if (WifiProStateMachine.this.mCurrentWifiConfig.portalNetwork) {
                this.portalCheck = true;
                String unused4 = WifiProStateMachine.this.respCodeChrInfo = "";
                int unused5 = WifiProStateMachine.this.detectionNumSlow = 0;
                long unused6 = WifiProStateMachine.this.connectStartTime = System.currentTimeMillis();
                if (WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp == 0) {
                    WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp = System.currentTimeMillis();
                    WifiProStateMachine.this.updateWifiConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logD("periodic portal check: update portalAuthTimestamp =" + WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, HwQoEService.GAME_RTT_NOTIFY_INTERVAL);
            }
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiLinkMonitorState is Exit");
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.setWifiMonitorEnabled(false);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY);
            WifiProStateMachine.this.stopDualBandMonitor();
            this.detectCounter = 0;
            this.portalCheck = false;
            this.isToastDisplayed = false;
            this.isDialogDisplayed = false;
            boolean unused = WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = false;
            boolean unused2 = WifiProStateMachine.this.mDelayedRssiChangedByCalling = false;
            this.isWiFiHandoverPriority = false;
            if (System.currentTimeMillis() - this.wifiLinkHoldTime > 1800000) {
                int unused3 = WifiProStateMachine.this.mCurrentVerfyCounter = 0;
            }
        }

        public boolean processMessage(Message msg) {
            int roReason;
            int accessType;
            int i = msg.what;
            switch (i) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    if (networkInfo == null || NetworkInfo.DetailedState.VERIFYING_POOR_LINK != networkInfo.getDetailedState()) {
                        if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                            WifiProStateMachine.this.logD("wifi has disconnected,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                            WifiProStateMachine.this.updatePortalNetworkInfo();
                            if (!this.isWifi2WifiProcess && !WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.mCurrentRssi < -75) {
                                WifiProStateMachine.this.setLastDisconnectNetwork();
                            }
                            if (!this.isWifi2WifiProcess || !WifiProStateMachine.this.mWifiManager.isWifiEnabled()) {
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                                break;
                            }
                        }
                    } else {
                        WifiProStateMachine.this.logD("wifi handover mobile is Complete!");
                        this.isSwitching = false;
                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(1);
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProVerfyingLinkState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                    if (!this.isScreenOffMonitor) {
                        WifiProStateMachine.this.logD("device screen on,but isScreenOffMonitor is false");
                        break;
                    } else {
                        WifiProStateMachine.this.logD("device screen on,reinitialize wifi monitor");
                        this.isScreenOffMonitor = false;
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
                        break;
                    }
                default:
                    switch (i) {
                        case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                            handleCheckResultInLinkMonitorState(msg);
                            break;
                        case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                            handleNetworkConnectivityChange(msg);
                            break;
                        case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /*136178*/:
                            WifiProStateMachine.this.logD("receive wifi handover wifi Result,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                            if (this.isWifi2WifiProcess) {
                                if (!((Boolean) msg.obj).booleanValue()) {
                                    wifi2WifiFailed();
                                    break;
                                } else {
                                    WifiProStateMachine.this.logD(" wifi --> wifi is  succeed");
                                    this.isSwitching = false;
                                    boolean unused = WifiProStateMachine.this.mIsUserManualConnectSuccess = false;
                                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mBadSsid);
                                    WifiProStateMachine.this.addDualBandBlackList(WifiProStateMachine.this.mBadSsid);
                                    WifiProStateMachine.this.refreshConnectedNetWork();
                                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                                    WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                                    break;
                                }
                            }
                            break;
                        case WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE /*136179*/:
                            handleRssiChangedInLinkMonitorState(msg);
                            break;
                        case WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT /*136180*/:
                            tryWifi2Mobile(msg.arg1);
                            break;
                        case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                            handleWifiInternetResultInLinkMonitorState(msg);
                            break;
                        case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                            if (msg.arg1 != 101) {
                                if (this.isWifi2MobileUIShowing) {
                                    this.isDialogDisplayed = false;
                                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                                    this.isWifi2MobileUIShowing = false;
                                    int unused2 = WifiProStateMachine.this.mWiFiProPdpSwichValue = 1;
                                    WifiProStateMachine.this.setWifiCSPState(0);
                                    WifiProStateMachine.this.logD("Click OK ,is send message to wifi handover mobile ,WiFiProPdp is AUTO");
                                    if (WifiProStateMachine.this.mIsMobileDataEnabled && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue != 2) {
                                        this.isAllowWiFiHandoverMobile = true;
                                        WifiProStateMachine.this.logD("mWsmChannel send Poor Link Detected");
                                        WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.POOR_LINK_DETECTED);
                                        if (this.currWifiPoorlevel == -1) {
                                            roReason = 2;
                                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetHandoverCount();
                                        } else {
                                            roReason = 1;
                                        }
                                        WifiProStateMachine.this.logD("roReason = " + roReason);
                                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveOutEvent(roReason);
                                        WifiProStateMachine.this.mWifiProStatisticsManager.uploadWifiproEvent(909002057);
                                    }
                                    WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                                    break;
                                }
                            } else {
                                WifiProStateMachine.this.logD("WiFiLinkMonitorState::Click OK ,User start wifi switch.");
                                this.isDisableWifiAutoSwitch = false;
                                this.isNoInternetDialogShowing = false;
                                this.isSwitching = true;
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_TRY_WIFI_ROVE_OUT);
                                break;
                            }
                            break;
                        case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                            if (msg.arg1 != 101) {
                                if (this.isWifi2MobileUIShowing) {
                                    WifiProStateMachine.this.logD("isDialogDisplayed : " + this.isDialogDisplayed + ", mIsWiFiNoInternet " + WifiProStateMachine.this.mIsWiFiNoInternet);
                                    if (this.isDialogDisplayed) {
                                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
                                        } else {
                                            WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(2);
                                        }
                                    } else if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                                    } else {
                                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseBQE_BadSettingCancelCount();
                                    }
                                    this.isDialogDisplayed = false;
                                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                                    this.isWifi2MobileUIShowing = false;
                                    this.isAllowWiFiHandoverMobile = false;
                                    WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                                    this.isSwitching = false;
                                    int unused3 = WifiProStateMachine.this.mWiFiProPdpSwichValue = 2;
                                    WifiProStateMachine.this.logD("Click Cancel ,is not allow wifi handover mobile, WiFiProPdp is CANNOT");
                                    if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                                        WifiProStateMachine.this.setWifiMonitorEnabled(true);
                                        break;
                                    } else {
                                        this.isCheckWiFiForUpdateSetting = true;
                                        if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                                            int unused4 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                            }
                                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                                            break;
                                        } else {
                                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                WifiProStateMachine.this.logD("WiFiLinkMonitorState::Click CANCEL ,User don't want wifi switch.");
                                this.isDisableWifiAutoSwitch = true;
                                this.isNoInternetDialogShowing = false;
                                break;
                            }
                            break;
                        case WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR /*136184*/:
                            WifiProStateMachine.this.logD("ReIniitalize,ScreenOn == " + WifiProStateMachine.this.mPowerManager.isScreenOn());
                            if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                                this.isScreenOffMonitor = true;
                                break;
                            } else {
                                this.wifiMonitorCounter++;
                                if (this.wifiMonitorCounter >= 4) {
                                    this.wifiMonitorCounter = Math.min(this.wifiMonitorCounter, 12);
                                    long delay_time = ((long) Math.pow(2.0d, (double) (this.wifiMonitorCounter / 4))) * 60 * 1000;
                                    WifiProStateMachine.this.logD("delay_time = " + delay_time);
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delay_time);
                                    if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isCheckWiFiForUpdateSetting) {
                                        this.isCheckWiFiForUpdateSetting = true;
                                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                                    }
                                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                                        int unused5 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                        }
                                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                                    }
                                } else {
                                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                                }
                                WifiProStateMachine.this.logD("wifiMonitorCounter = " + this.wifiMonitorCounter);
                                break;
                            }
                        default:
                            switch (i) {
                                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /*136190*/:
                                    handleEmuiCspSettingChange();
                                    break;
                                case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /*136191*/:
                                    WifiProStateMachine.this.logD("receive : EVENT_RETRY_WIFI_TO_WIFI, no internet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
                                    boolean internetRecheck = false;
                                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                        internetRecheck = true;
                                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                                    }
                                    this.isCheckWiFiForUpdateSetting = false;
                                    wiFiLinkMonitorStateInit(internetRecheck);
                                    break;
                                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                                    if (WifiProStateMachine.this.mIsWiFiNoInternet && (this.isCheckWiFiForUpdateSetting || this.isDialogDisplayed)) {
                                        WifiProStateMachine.this.logD("queryNetworkQos for wifi , isCheckWiFiForUpdateSetting =" + this.isCheckWiFiForUpdateSetting + ", isDialogDisplayed =" + this.isDialogDisplayed);
                                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                                        int unused6 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                        }
                                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                                        break;
                                    }
                                default:
                                    switch (i) {
                                        case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /*136195*/:
                                            if (msg.obj == null || !((Boolean) msg.obj).booleanValue()) {
                                                if (msg.obj != null && !((Boolean) msg.obj).booleanValue()) {
                                                    WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT = false, SCE force to request wifi switch.");
                                                    boolean unused7 = WifiProStateMachine.this.mIsWiFiNoInternet = true;
                                                    this.isNotifyInvalidLinkDetection = true;
                                                    if (WifiProStateMachine.IS_DOCOMO && WifiProStateMachine.this.mIsUserManualConnectSuccess && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                                                        WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.INVALID_LINK_DETECTED);
                                                    }
                                                    WifiProStateMachine.this.updateWifiInternetStateChange(-1);
                                                    boolean unused8 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = true;
                                                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, 0, 0, false);
                                                    break;
                                                }
                                            } else {
                                                this.internetFailureDetectedCnt = 0;
                                                boolean unused9 = WifiProStateMachine.this.mIsWiFiNoInternet = false;
                                                boolean unused10 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                                                WifiProStateMachine.this.onNetworkDetectionResult(1, 5);
                                                break;
                                            }
                                            break;
                                        case WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY /*136196*/:
                                            handleReuqestScanInLinkMonitorState(msg);
                                            break;
                                        case WifiProStateMachine.EVENT_CONFIGURATION_CHANGED /*136197*/:
                                            handleOrientationChanged(msg);
                                            break;
                                        case WifiProStateMachine.EVENT_NOTIFY_WIFI_LINK_POOR /*136198*/:
                                            WifiProStateMachine.this.logD("EVENT_NOTIFY_WIFI_LINK_POOR isDisableWifiAutoSwitch = " + this.isDisableWifiAutoSwitch);
                                            if (!this.isDisableWifiAutoSwitch) {
                                                int unused11 = WifiProStateMachine.this.mCurrentWifiLevel = 0;
                                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, 0, 0, false);
                                                break;
                                            }
                                            break;
                                        case WifiProStateMachine.EVENT_TRY_WIFI_ROVE_OUT /*136199*/:
                                            handleWiFiRoveOut();
                                            break;
                                        default:
                                            switch (i) {
                                                case WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW /*136204*/:
                                                    WifiProStateMachine.this.logD("receive : EVENT_PERIODIC_PORTAL_CHECK_SLOW");
                                                    boolean unused12 = WifiProStateMachine.this.isPeriodicDet = true;
                                                    if (HwAutoConnectManager.getInstance() != null) {
                                                        HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
                                                    }
                                                    int unused13 = WifiProStateMachine.this.detectionNumSlow = WifiProStateMachine.this.detectionNumSlow + 1;
                                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, HwQoEService.GAME_RTT_NOTIFY_INTERVAL);
                                                    break;
                                                case WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST /*136205*/:
                                                    if (HwAutoConnectManager.getInstance() != null) {
                                                        HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
                                                        break;
                                                    }
                                                    break;
                                                case WifiProStateMachine.EVENT_DEVICE_SCREEN_OFF /*136206*/:
                                                    if (this.portalCheck) {
                                                        WifiProStateMachine.this.logD("periodic portal check: screen off, remove msg");
                                                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                                                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                                                        break;
                                                    }
                                                    break;
                                                case WifiProStateMachine.EVENT_DEVICE_USER_PRESENT /*136207*/:
                                                    if (this.portalCheck) {
                                                        WifiProStateMachine.this.logD("periodic portal check: screen unlocked, perform portal check right now");
                                                        boolean unused14 = WifiProStateMachine.this.isPeriodicDet = false;
                                                        if (HwAutoConnectManager.getInstance() != null) {
                                                            HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
                                                        }
                                                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                                                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                                                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, HwQoEService.GAME_RTT_NOTIFY_INTERVAL);
                                                        break;
                                                    }
                                                    break;
                                                case WifiProStateMachine.EVENT_CHECK_PORTAL_AUTH_CHECK_RESULT /*136208*/:
                                                    handlePortalAuthCheckResultInLinkMonitorState(msg);
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /*136299*/:
                                                            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                                int rttlevel = msg.arg1;
                                                                WifiProStateMachine.this.logD(WifiProStateMachine.this.mCurrentSsid + "  TCPRTT  level = " + rttlevel);
                                                                if (rttlevel <= 0 || rttlevel > 3) {
                                                                    rttlevel = 0;
                                                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                                                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(WifiProStateMachine.this.mCurrentSsid);
                                                                }
                                                                updateWifiQosLevel(false, rttlevel);
                                                                break;
                                                            }
                                                            break;
                                                        case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                                                            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                                                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
                                                                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                                    accessType = 4;
                                                                } else {
                                                                    accessType = 2;
                                                                }
                                                                if (WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, accessType)) {
                                                                    WifiProStateMachine.this.logD("mCurrentSsid   = " + WifiProStateMachine.this.mCurrentSsid + ", updateScoreInfoType  " + accessType);
                                                                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, accessType, WifiProStateMachine.this.mCurrentSsid);
                                                                }
                                                                if (accessType == 4) {
                                                                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        default:
                                                            switch (i) {
                                                                case WifiProStateMachine.EVENT_DUALBAND_RSSITH_RESULT /*136368*/:
                                                                    if (!this.isWifi2WifiProcess) {
                                                                        WifiProEstimateApInfo apInfo = (WifiProEstimateApInfo) msg.obj;
                                                                        if (WifiProStateMachine.this.mDualBandMonitorInfoSize > 0) {
                                                                            int unused15 = WifiProStateMachine.this.mDualBandMonitorInfoSize = WifiProStateMachine.this.mDualBandMonitorInfoSize - 1;
                                                                            WifiProStateMachine.this.updateDualBandMonitorInfo(apInfo);
                                                                        }
                                                                        if (WifiProStateMachine.this.mDualBandMonitorInfoSize == 0) {
                                                                            boolean unused16 = WifiProStateMachine.this.mDualBandMonitorStart = true;
                                                                            WifiProStateMachine.this.logD("Start dual band Manager monitor");
                                                                            WifiProStateMachine.this.mDualBandManager.startMonitor(WifiProStateMachine.this.mDualBandMonitorApList);
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        WifiProStateMachine.this.logD("isWifi2WifiProcess is true, ignore this message");
                                                                        boolean unused17 = WifiProStateMachine.this.mNeedRetryMonitor = true;
                                                                        break;
                                                                    }
                                                                    break;
                                                                case WifiProStateMachine.EVENT_DUALBAND_SCORE_RESULT /*136369*/:
                                                                    if (!this.isWifi2WifiProcess) {
                                                                        WifiProEstimateApInfo estimateApInfo = (WifiProEstimateApInfo) msg.obj;
                                                                        WifiProStateMachine.this.logD("EVENT_DUALBAND_SCORE_RESULT estimateApInfo: " + estimateApInfo.toString());
                                                                        if (WifiProStateMachine.this.mDualBandEstimateInfoSize > 0) {
                                                                            int unused18 = WifiProStateMachine.this.mDualBandEstimateInfoSize = WifiProStateMachine.this.mDualBandEstimateInfoSize - 1;
                                                                            WifiProStateMachine.this.updateDualBandEstimateInfo(estimateApInfo);
                                                                        }
                                                                        WifiProStateMachine.this.logD("mDualBandEstimateInfoSize = " + WifiProStateMachine.this.mDualBandEstimateInfoSize);
                                                                        if (WifiProStateMachine.this.mDualBandEstimateInfoSize == 0) {
                                                                            WifiProStateMachine.this.chooseAvalibleDualBandAp();
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        WifiProStateMachine.this.logD("isWifi2WifiProcess is true, ignore this message");
                                                                        boolean unused19 = WifiProStateMachine.this.mNeedRetryMonitor = true;
                                                                        break;
                                                                    }
                                                                    break;
                                                                case WifiProStateMachine.EVENT_DUALBAND_5GAP_AVAILABLE /*136370*/:
                                                                    handleDualbandApAvailable();
                                                                    break;
                                                                case WifiProStateMachine.EVENT_DUALBAND_WIFI_HANDOVER_RESULT /*136371*/:
                                                                    handleDualbandHandoverResult(msg);
                                                                    break;
                                                                case WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY /*136372*/:
                                                                    WifiProStateMachine.this.logD("receive dual band wifi handover delay retry");
                                                                    WifiProStateMachine.this.retryDualBandAPMonitor();
                                                                    break;
                                                                default:
                                                                    switch (i) {
                                                                        case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                                                                            handleWifiQosChangedInLinkMonitorState(msg);
                                                                            break;
                                                                        case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                                                                            WifiProStateMachine.this.logD("WiFiLinkMonitorState : Receive Mobile changed");
                                                                            if (WifiProStateMachine.this.isMobileDataConnected() && this.isAllowWiFiHandoverMobile) {
                                                                                this.isCheckWiFiForUpdateSetting = false;
                                                                                if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                                                    WifiProStateMachine.this.setWifiMonitorEnabled(true);
                                                                                    break;
                                                                                } else {
                                                                                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                                                                                    break;
                                                                                }
                                                                            }
                                                                        case WifiProStateMachine.EVENT_CALL_STATE_CHANGED /*136201*/:
                                                                            handleCallStateChanged(msg);
                                                                            break;
                                                                        case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /*136311*/:
                                                                            WifiProStateMachine.this.handleGetWifiTcpRx();
                                                                            break;
                                                                        default:
                                                                            return false;
                                                                    }
                                                            }
                                                            break;
                                                    }
                                            }
                                    }
                            }
                            break;
                    }
            }
            return true;
        }

        private boolean isStrongRssi() {
            return WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo()) >= 3;
        }

        private void handleNetworkConnectivityChange(Message msg) {
            Intent connIntent = (Intent) msg.obj;
            if (connIntent != null) {
                int networkType = connIntent.getIntExtra("networkType", 1);
                NetworkInfo mobileInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(0);
                if (networkType == 0 && mobileInfo != null && NetworkInfo.DetailedState.CONNECTED == mobileInfo.getDetailedState()) {
                    WifiProStateMachine.this.logD("network change to mobile,show toast.");
                    WifiProUIDisplayManager access$1200 = WifiProStateMachine.this.mWifiProUIDisplayManager;
                    WifiProUIDisplayManager unused = WifiProStateMachine.this.mWifiProUIDisplayManager;
                    access$1200.showWifiProToast(1);
                    if (WifiProStateMachine.IS_DOCOMO) {
                        WifiProStateMachine.this.updateWifiInternetStateChange(-1);
                        this.isCheckWiFiForUpdateSetting = true;
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, -1);
                    }
                }
            }
        }

        private void showPortalStatusBar() {
            if (WifiProStateMachine.this.mCurrentWifiConfig != null && !TextUtils.isEmpty(WifiProStateMachine.this.mCurrentWifiConfig.SSID) && !WifiProStateMachine.this.mCurrentWifiConfig.SSID.equals("<unknown ssid>")) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("periodic portal check: showPortalStatusBar, portal network = " + WifiProStateMachine.this.mCurrentWifiConfig.configKey());
                if (WifiProStateMachine.this.mPortalNotificationId == -1) {
                    int unused = WifiProStateMachine.this.mPortalNotificationId = new SecureRandom().nextInt(100000);
                }
                Notification.Builder showPortalNotificationStatusBar = WifiProStateMachine.this.mWifiProUIDisplayManager.showPortalNotificationStatusBar(WifiProStateMachine.this.mCurrentWifiConfig.SSID, WifiProStateMachine.PORTAL_STATUS_BAR_TAG, WifiProStateMachine.this.mPortalNotificationId, null);
                WifiProStateMachine.this.notifyPortalStatusChanged(true, WifiProStateMachine.this.mCurrentWifiConfig.configKey(), WifiProStateMachine.this.mCurrentWifiConfig.lastHasInternetTimestamp > 0);
            }
        }

        private void handlePortalAuthCheckResultInLinkMonitorState(Message msg) {
            int wifiInternetLevel = msg.arg1;
            WifiProStateMachine.this.logD("periodic portal check, handlePortalAuthCheckResultInLinkMonitorState : wifi_internet_level = " + wifiInternetLevel);
            if (6 == msg.arg1) {
                WifiProStateMachine.this.logD("periodic portal check: detectCounter = " + this.detectCounter);
                if (WifiProStateMachine.this.respCodeChrInfo.length() != 0) {
                    WifiProStateMachine.access$8684(WifiProStateMachine.this, "/");
                }
                WifiProStateMachine.access$8684(WifiProStateMachine.this, WifiProStateMachine.RESP_CODE_PORTAL);
                if (this.detectCounter >= 2) {
                    long mPortalValidityDuration = System.currentTimeMillis() - WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp;
                    if (((WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration != 0 && WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration > mPortalValidityDuration) || WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration == 0) && mPortalValidityDuration > 0) {
                        WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration = mPortalValidityDuration;
                    }
                    if (WifiProStateMachine.this.mNetworkPropertyChecker != null) {
                        Settings.Global.putString(WifiProStateMachine.this.mContext.getContentResolver(), "captive_portal_server", WifiProStateMachine.this.mNetworkPropertyChecker.getCaptiveUsedServer());
                    }
                    WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp = 0;
                    WifiProStateMachine.this.updateWifiConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                    showPortalStatusBar();
                    this.detectCounter = 0;
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED, WifiProStateMachine.DELAY_UPLOAD_MS);
                    WifiProStateMachine.this.deferMessage(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, 6));
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    return;
                }
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST, 2000);
                this.detectCounter++;
            } else if (this.detectCounter > 0) {
                if (wifiInternetLevel < 1 || wifiInternetLevel > 5) {
                    WifiProStateMachine.access$8684(WifiProStateMachine.this, "/");
                    WifiProStateMachine.access$8684(WifiProStateMachine.this, WifiProStateMachine.RESP_CODE_INTERNET_UNREACHABLE);
                } else {
                    WifiProStateMachine.access$8684(WifiProStateMachine.this, "/");
                    WifiProStateMachine.access$8684(WifiProStateMachine.this, WifiProStateMachine.RESP_CODE_INTERNET_AVAILABLE);
                }
                WifiProStateMachine.this.logD("respCode changes in consecutive checks, upload CHR");
                WifiProStateMachine.this.uploadPortalAuthExpirationStatistics(false);
                this.detectCounter = 0;
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, HwQoEService.GAME_RTT_NOTIFY_INTERVAL);
            }
        }

        private void handleWifiInternetResultInLinkMonitorState(Message msg) {
            int wifi_internet_level = msg.arg1;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("WiFiLinkMonitorState : wifi_internet_level = " + wifi_internet_level);
            if (-1 == msg.arg1 || 6 == msg.arg1) {
                HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
                if (mHwWifiCHRService != null && !WifiProStateMachine.this.mIsWiFiInternetCHRFlag) {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logD("upload WIFI_ACCESS_INTERNET_FAILED event for TRANS_TO_NO_INTERNET,ssid:" + WifiProStateMachine.this.mCurrentSsid);
                    mHwWifiCHRService.updateWifiException(87, "TRANS_TO_NO_INTERNET");
                }
                boolean unused = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = true;
                boolean unused2 = WifiProStateMachine.this.mIsWiFiNoInternet = true;
                this.currWifiPoorlevel = -1;
                wifi_internet_level = this.currWifiPoorlevel;
                if (this.isBQERequestCheckWiFi) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(false);
                }
                if (this.isCheckWiFiForUpdateSetting) {
                    int unused3 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                }
            } else {
                this.wifiMonitorCounter = 0;
                this.isCheckWiFiForUpdateSetting = false;
                boolean unused4 = WifiProStateMachine.this.mIsWiFiNoInternet = false;
                boolean unused5 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                WifiProStateMachine.this.reSetWifiInternetState();
                WifiProStateMachine.this.setWifiMonitorEnabled(true);
            }
            this.isBQERequestCheckWiFi = false;
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, wifi_internet_level, 0, false);
        }

        private void handleWifiQosChangedInLinkMonitorState(Message msg) {
            if (pendingMsgBySelfCureEngine(msg.arg1) || handleMsgBySwitchOrDialogStatus(msg.arg1)) {
                return;
            }
            if (!((Boolean) msg.obj).booleanValue() || -103 == msg.arg1 || -104 == msg.arg1) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("WiFiLinkMonitorState receive wifi Qos currWifiPoorlevel = " + msg.arg1 + ", dialog = " + this.isNoInternetDialogShowing + ", updateSettings = " + this.isCheckWiFiForUpdateSetting);
                if (-103 == msg.arg1) {
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                    this.isBQERequestCheckWiFi = true;
                    this.isRequestWifInetCheck = true;
                } else if (-104 == msg.arg1) {
                    WifiProStateMachine.this.logD("REQUEST_POOR_RSSI_INET_CHECK, no HTTP GET, wait APPs to report poor link.");
                } else if (this.isCheckWiFiForUpdateSetting) {
                    if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.setWifiMonitorEnabled(true);
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                    }
                } else {
                    this.currWifiPoorlevel = msg.arg1;
                    if (msg.arg1 <= 2 && !this.isNoInternetDialogShowing) {
                        if (this.currWifiPoorlevel == -1) {
                            boolean unused = WifiProStateMachine.this.mIsWiFiNoInternet = true;
                            boolean unused2 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = true;
                        }
                        int unused3 = WifiProStateMachine.this.mWifiToWifiType = 0;
                        if (this.currWifiPoorlevel == -2) {
                            WifiProStateMachine.this.refreshConnectedNetWork();
                            tryWifiHandoverPreferentially(WifiProStateMachine.this.mCurrentRssi);
                            return;
                        }
                        updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, 1);
                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                            int unused4 = WifiProStateMachine.this.mWifiToWifiType = 1;
                        }
                        WifiProStateMachine.this.logW("WiFiLinkMonitorState : try wifi --> wifi --> mobile data");
                        this.isWiFiHandoverPriority = false;
                        tryWifi2Wifi();
                    }
                    if (this.isRequestWifInetCheck && this.currWifiPoorlevel == -1) {
                        this.isRequestWifInetCheck = false;
                        this.isNotifyInvalidLinkDetection = true;
                        WifiProStateMachine.this.logD("Monitoring to the broken network, Maybe needs to be informed the message to networkmonitor");
                    }
                }
            } else {
                this.currWifiPoorlevel = msg.arg1;
                if (msg.arg1 == 0 || msg.arg1 == 1) {
                    updateWifiQosLevel(false, 1);
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet && msg.arg1 == 3) {
                    WifiProStateMachine.this.updateWifiInternetStateChange(msg.arg1);
                    boolean unused5 = WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    HwSelfCureEngine.getInstance().notifyInternetAccessRecovery();
                }
            }
        }

        private void handleOrientationChanged(Message msg) {
            if (WifiProStateMachine.this.mDelayedRssiChangedByFullScreen && !WifiProStateMachine.this.isFullscreen()) {
                boolean unused = WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = false;
                if (WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo()) < 3) {
                    WifiProStateMachine.this.logD("handleOrientationChanged, continue full screen skiped scan.");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord()));
                }
            }
        }

        private void handleCallStateChanged(Message msg) {
            if (WifiProStateMachine.this.mDelayedRssiChangedByCalling && msg.arg1 == 0) {
                boolean unused = WifiProStateMachine.this.mDelayedRssiChangedByCalling = false;
                if (WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo()) < 3) {
                    WifiProStateMachine.this.logD("handleCallStateChanged, continue scan.");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord()));
                }
            }
        }

        private void handleEmuiCspSettingChange() {
            if (WifiProStateMachine.this.mEmuiPdpSwichValue != 2) {
                this.isAllowWiFiHandoverMobile = true;
                this.isCheckWiFiForUpdateSetting = false;
                this.isSwitching = false;
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                } else {
                    WifiProStateMachine.this.setWifiMonitorEnabled(true);
                }
            }
        }

        private void handleRssiChangedInLinkMonitorState(Message msg) {
            int unused = WifiProStateMachine.this.mCurrentRssi = ((Intent) msg.obj).getIntExtra("newRssi", WifiHandover.INVALID_RSSI);
            if (WifiProStateMachine.this.isFullscreen()) {
                boolean unused2 = WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = true;
            } else if (WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext)) {
                boolean unused3 = WifiProStateMachine.this.mDelayedRssiChangedByCalling = true;
            } else {
                boolean unused4 = WifiProStateMachine.this.mDelayedRssiChangedByCalling = false;
                boolean unused5 = WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = false;
                if (!this.isWiFiHandoverPriority && !this.isWifi2WifiProcess && !WifiProStateMachine.this.mIsWiFiNoInternet && !WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY)) {
                    int rssilevel = WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo());
                    boolean hasSwitchRecord = hasWifiSwitchRecord();
                    if (rssilevel >= 3) {
                        if (rssilevel == 4 && hasSwitchRecord) {
                            this.rssiLevel2ScanedCounter = 0;
                            this.rssiLevel0Or1ScanedCounter = 0;
                        }
                        if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY);
                        }
                        return;
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasSwitchRecord));
                }
            }
        }

        private boolean hasWifiSwitchRecord() {
            WifiProStateMachine.this.refreshConnectedNetWork();
            if (WifiProStateMachine.this.mCurrentWifiConfig == null || WifiProStateMachine.this.mCurrentWifiConfig.lastTrySwitchWifiTimestamp <= 0) {
                return false;
            }
            return System.currentTimeMillis() - WifiProStateMachine.this.mCurrentWifiConfig.lastTrySwitchWifiTimestamp < WifiProStateMachine.WIFI_SWITCH_RECORD_MAX_TIME;
        }

        private void handleReuqestScanInLinkMonitorState(Message msg) {
            int scanMaxCounter;
            int scanInterval;
            if (WifiProStateMachine.this.isFullscreen()) {
                boolean unused = WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = true;
                WifiProStateMachine.this.logD("handleReuqestScanInLinkMonitorState, don't try to swithch wifi when full screen.");
            } else if (WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext)) {
                WifiProStateMachine.this.logD("handleReuqestScanInLinkMonitorState, don't try to swithch wifi when calling.");
                boolean unused2 = WifiProStateMachine.this.mDelayedRssiChangedByCalling = true;
            } else {
                boolean hasWifiSwitchRecord = ((Boolean) msg.obj).booleanValue();
                if (HwSelfCureEngine.getInstance().isSelfCureOngoing()) {
                    if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord)), 10000);
                    return;
                }
                int rssilevel = WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo());
                if (rssilevel < 3) {
                    if (!WifiProStateMachine.this.mIsUserManualConnectSuccess || rssilevel != 2 || this.currWifiPoorlevel <= 2) {
                        if (hasWifiSwitchRecord) {
                            scanInterval = WifiProStateMachine.QUICK_SCAN_INTERVAL[rssilevel];
                            scanMaxCounter = WifiProStateMachine.QUICK_SCAN_MAX_COUNTER[rssilevel];
                        } else {
                            scanInterval = WifiProStateMachine.NORMAL_SCAN_INTERVAL[rssilevel];
                            scanMaxCounter = WifiProStateMachine.NORMAL_SCAN_MAX_COUNTER[rssilevel];
                        }
                        if (rssilevel == 2 && this.rssiLevel2ScanedCounter < scanMaxCounter) {
                            this.rssiLevel2ScanedCounter++;
                            WifiProStateMachine.this.mWifiManager.startScan();
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord)), (long) scanInterval);
                        } else if (rssilevel < 2 && this.rssiLevel0Or1ScanedCounter < scanMaxCounter) {
                            this.rssiLevel0Or1ScanedCounter++;
                            WifiProStateMachine.this.mWifiManager.startScan();
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord)), (long) scanInterval);
                        }
                        return;
                    }
                    WifiProStateMachine.this.logD("handleReuqestScanInLinkMonitorState, user click and signal = 2, but wifi link is good, don't trigger scan.");
                }
            }
        }

        private void handleCheckResultInLinkMonitorState(Message msg) {
            int preferType = msg.arg2;
            if (WifiProStateMachine.this.isFullscreen() || WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext) || WifiProStateMachine.this.mIsWiFiNoInternet || !((Boolean) msg.obj).booleanValue()) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logW("mIsWiFiNoInternet" + WifiProStateMachine.this.mIsWiFiNoInternet + "isFullscreen" + WifiProStateMachine.this.isFullscreen());
            } else if (this.isWiFiHandoverPriority || this.isWifi2WifiProcess || HwSelfCureEngine.getInstance().isSelfCureOngoing()) {
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logW("isWiFiHandoverPriority" + this.isWiFiHandoverPriority + "isWifi2WifiProcess" + this.isWifi2WifiProcess);
            } else if (WifiProStateMachine.this.mNetworkQosMonitor.isHighDataFlowModel()) {
                WifiProStateMachine.this.logw("has good rssi network, but user is in high data mode, don't handle wifi switch.");
            } else {
                WifiInfo wifiInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (wifiInfo == null || wifiInfo.getRssi() == -127) {
                    WifiProStateMachine.this.logW("wifiInfo RSSI is invalid");
                    return;
                }
                int curRssiLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.logD("handleCheckResultInLinkMonitorState, prefer=" + preferType + ", manual=" + WifiProStateMachine.this.mIsUserManualConnectSuccess);
                if (1 == preferType) {
                    WifiProStateMachine.this.logD("handleCheckResultInLinkMonitorState, go wifi2wifi");
                    tryWifiHandoverWithoutRssiCheck(curRssiLevel);
                } else if (curRssiLevel < 3) {
                    int targetRssiLevel = Integer.valueOf(msg.arg1).intValue();
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.logD("curRssiLevel = " + curRssiLevel + ", targetRssiLevel " + targetRssiLevel);
                    if (targetRssiLevel - curRssiLevel >= 2) {
                        tryWifiHandoverPreferentially(curRssiLevel);
                    }
                }
            }
        }

        private void handleDualbandApAvailable() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("receive EVENT_DUALBAND_5GAP_AVAILABLE isSwitching = " + this.isSwitching);
            if (this.isSwitching) {
                boolean unused = WifiProStateMachine.this.mNeedRetryMonitor = true;
                return;
            }
            if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.SSID != null && WifiProStateMachine.this.mAvailable5GAPSsid != null && WifiProStateMachine.this.mCurrentWifiConfig.SSID.equals(WifiProStateMachine.this.mAvailable5GAPSsid) && WifiProStateMachine.this.mCurrentWifiConfig.allowedKeyManagement.cardinality() <= 1 && WifiProStateMachine.this.mCurrentWifiConfig.getAuthType() == WifiProStateMachine.this.mAvailable5GAPAuthType) {
                int unused2 = WifiProStateMachine.this.mDuanBandHandoverType = 1;
                WifiProStateMachine.this.logD("handleDualbandApAvailable 5G and 2.4G AP have the same ssid and auth type");
            }
            int switchType = WifiProStateMachine.this.mDuanBandHandoverType == 1 ? 2 : 1;
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logD("do dual band wifi handover, switchType = " + switchType);
            if (WifiProStateMachine.this.isFullscreen() || WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext) || HwSelfCureEngine.getInstance().isSelfCureOngoing()) {
                WifiProStateMachine.this.logD("keep in current AP,now is in calling/full screen/selfcure and switch by hardhandover");
                return;
            }
            this.isSwitching = true;
            this.isWifi2WifiProcess = true;
            String unused3 = WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
            String unused4 = WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
            wifiProStateMachine3.logD("do dual band wifi handover, mCurrentSsid:" + WifiProStateMachine.this.mCurrentSsid + ", mAvailable5GAPSsid =" + WifiProStateMachine.this.mAvailable5GAPSsid + ", mDuanBandHandoverType = " + WifiProStateMachine.this.mDuanBandHandoverType);
            String unused5 = WifiProStateMachine.this.mNewSelect_bssid = WifiProStateMachine.this.mAvailable5GAPBssid;
            int dualbandReason = WifiProStateMachine.this.mWifiHandover.handleDualBandWifiConnect(WifiProStateMachine.this.mAvailable5GAPBssid, WifiProStateMachine.this.mAvailable5GAPSsid, WifiProStateMachine.this.mAvailable5GAPAuthType, switchType);
            if (dualbandReason != 0) {
                dualBandhandoverFailed(dualbandReason);
            }
        }

        private void tryWifiHandoverPreferentially(int curRssiLevel) {
            if (curRssiLevel <= 2) {
                if (curRssiLevel < 1 && !WifiProStateMachine.this.mIsScanedRssiLow) {
                    boolean unused = WifiProStateMachine.this.mIsScanedRssiLow = true;
                } else if (curRssiLevel >= 1 && !WifiProStateMachine.this.mIsScanedRssiMiddle) {
                    boolean unused2 = WifiProStateMachine.this.mIsScanedRssiMiddle = true;
                } else {
                    return;
                }
                this.isWiFiHandoverPriority = true;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logW("try wifi --> wifi only, current rssi = " + WifiProStateMachine.this.mCurrentRssi);
                this.mWifi2WifiThreshod = WifiProStateMachine.this.mCurrentRssi;
                tryWifi2Wifi();
            }
        }

        private void tryWifiHandoverWithoutRssiCheck(int curRssiLevel) {
            if (!WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("tryWifiHandoverWithoutRssiCheck: mIsWiFiProEnabled = " + WifiProStateMachine.this.mIsWiFiProEnabled);
                return;
            }
            this.isWiFiHandoverPriority = true;
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logW("try wifi --> wifi only, current rssi = " + WifiProStateMachine.this.mCurrentRssi);
            this.mWifi2WifiThreshod = WifiProStateMachine.this.mCurrentRssi;
            tryWifi2Wifi();
        }

        private void tryWifi2Wifi() {
            if ((!WifiProStateMachine.this.mIsUserManualConnectSuccess || WifiProStateMachine.this.mIsWiFiProEnabled) && !WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                this.isSwitching = true;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_TRY_WIFI_ROVE_OUT);
                return;
            }
            WifiProStateMachine.this.logD("User manual connect wifi, and wifi+ disabled. don't try wifi switch!");
        }

        private void tryWifi2Mobile(int mobile_level) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("Receive mobile QOS  mobile_level = " + mobile_level + ", isSwitching =" + this.isSwitching);
            boolean wifiProFromBrainFlag = false;
            if (HwArbitrationManager.getInstance() != null) {
                wifiProFromBrainFlag = HwArbitrationManager.getInstance().getWifiPlusFlagFromHiData();
            }
            if (this.isWifi2WifiProcess || !WifiProStateMachine.this.isAllowWifi2Mobile() || !this.isAllowWiFiHandoverMobile || !WifiProStateMachine.this.mPowerManager.isScreenOn() || WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext) || WifiProStateMachine.this.mIsWiFiNoInternet || ((isStrongRssi() && !WifiProCommonUtils.isOpenAndPortal(WifiProStateMachine.this.mCurrentWifiConfig)) || wifiProFromBrainFlag)) {
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logD("isWifi2WifiProcess = " + this.isWifi2WifiProcess + ", isAllowWifi2Mobile = " + WifiProStateMachine.this.isAllowWifi2Mobile() + ", mIsAllowWiFiHandoverMobile = " + this.isAllowWiFiHandoverMobile + ", mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + ", isStrongRssi = " + isStrongRssi() + ", isOpenAndPortal = " + WifiProCommonUtils.isOpenAndPortal(WifiProStateMachine.this.mCurrentWifiConfig) + ", wifiProFromBrainFlag = " + wifiProFromBrainFlag);
                this.isSwitching = false;
                if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR)) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                }
                return;
            }
            if (!WifiProStateMachine.this.isWiFiPoorer(this.currWifiPoorlevel, mobile_level)) {
                WifiProStateMachine.this.logD("mobile is poorer,continue monitor");
                this.isSwitching = false;
                if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                    this.isToastDisplayed = true;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
            } else if (!this.isSwitching || !WifiProStateMachine.this.isAllowWifi2Mobile()) {
                WifiProStateMachine.this.logW("no handover,DELAY Transit to Monitor");
                this.isSwitching = false;
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
            } else {
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.logD("mobile is better than wifi,and ScreenOn, try wifi --> mobile,show Dialog mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ", mIsWiFiNoInternet =" + WifiProStateMachine.this.mIsWiFiNoInternet);
                if (this.isWifi2MobileUIShowing) {
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.logD("isWifi2MobileUIShowing = true, not dispaly " + this.isWifi2MobileUIShowing);
                    return;
                }
                this.isWifi2MobileUIShowing = true;
                if (WifiProStateMachine.this.isPdpAvailable()) {
                    WifiProStateMachine wifiProStateMachine5 = WifiProStateMachine.this;
                    wifiProStateMachine5.logD("mobile is cmcc and wifi pdp, mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + " ,mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
                    WifiProStateMachine wifiProStateMachine6 = WifiProStateMachine.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("WiFi switch to mobile, last rssi signal : ");
                    sb.append(WifiProStateMachine.this.mCurrentRssi);
                    wifiProStateMachine6.logD(sb.toString());
                    int emuiPdpSwichType = WifiProStateMachine.this.mEmuiPdpSwichValue;
                    if (!WifiProStateMachine.this.isDialogUpWhenConnected) {
                        if (emuiPdpSwichType == 0) {
                            emuiPdpSwichType = 1;
                        }
                        switch (emuiPdpSwichType) {
                            case 1:
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                                break;
                            case 2:
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
                                break;
                        }
                    } else {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                    }
                } else {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                }
            }
        }

        private void wifi2WifiFailed() {
            if (WifiProStateMachine.this.mNewSelect_bssid != null && !WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadSsid)) {
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
            }
            WifiProStateMachine.this.logD("wifi to Wifi Failed Finally!");
            if (this.isNotifyInvalidLinkDetection && WifiProStateMachine.this.mIsWiFiNoInternet) {
                this.isNotifyInvalidLinkDetection = false;
                WifiProStateMachine.this.logD("We detection no internet, And wifi2WifiFailed, So we need notify msg to networkmonitor");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.INVALID_LINK_DETECTED);
            }
            this.isWifi2WifiProcess = false;
            this.isSwitching = false;
            if (WifiProCommonUtils.isWifiConnectedOrConnecting(WifiProStateMachine.this.mWifiManager)) {
                if (WifiProStateMachine.this.mNeedRetryMonitor) {
                    WifiProStateMachine.this.logD("need retry dualband handover monitor");
                    WifiProStateMachine.this.retryDualBandAPMonitor();
                    boolean unused = WifiProStateMachine.this.mNeedRetryMonitor = false;
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                }
                if (this.isWiFiHandoverPriority) {
                    WifiProStateMachine.this.logD("wifi handover wifi failed,continue monitor wifi Qos");
                    if (!WifiProStateMachine.this.mIsUserManualConnectSuccess || this.currWifiPoorlevel != -2) {
                        this.isWiFiHandoverPriority = false;
                    }
                    return;
                }
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("wifi --> wifi is Failure, but wifi is connected, isMobileDataConnected() = " + WifiProStateMachine.this.isMobileDataConnected() + ", isAllowWiFiHandoverMobile =  " + this.isAllowWiFiHandoverMobile + " , mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ", mPowerManager.isScreenOn =" + WifiProStateMachine.this.mPowerManager.isScreenOn() + ", currWifiPoorlevel = " + this.currWifiPoorlevel + ", mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
                if (WifiProStateMachine.this.mIsWiFiNoInternet || ((isStrongRssi() && !WifiProCommonUtils.isOpenAndPortal(WifiProStateMachine.this.mCurrentWifiConfig)) || !WifiProStateMachine.this.isAllowWifi2Mobile())) {
                    WifiProStateMachine.this.logD("wifi --> wifi is Failure,and can not handover to mobile ,delay 30s go to Monitor");
                    if (WifiProStateMachine.this.isMobileDataConnected() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue == 2 && !this.isCancelCHRTypeReport) {
                        this.isCancelCHRTypeReport = true;
                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.logD("call increaseNotInetSettingCancelCount.");
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                        } else {
                            WifiProStateMachine.this.logD("call increaseBQE_BadSettingCancelCount.");
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseBQE_BadSettingCancelCount();
                        }
                    }
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                        this.isToastDisplayed = true;
                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                    }
                    WifiProStateMachine.this.setWifiMonitorEnabled(false);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                        int unused2 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                    }
                } else {
                    WifiProStateMachine.this.logD("try to wifi --> mobile,Query mobile Qos");
                    this.isSwitching = true;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(0, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                }
            } else {
                WifiProStateMachine.this.logD("wifi handover over Failed and system auto conning ap");
                if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logD("try to connect : " + WifiProStateMachine.this.mBadSsid);
                    WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
                }
            }
        }

        private void dualBandhandoverFailed(int reason) {
            HwWifiCHRService mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            if (mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                data.putInt("failCnt", reason);
                mHwWifiCHRService.uploadDFTEvent(909002063, data);
            }
            if (!(10 == reason || 11 == reason)) {
                if (WifiProStateMachine.this.mNewSelect_bssid != null && !WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadBssid)) {
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid);
                    WifiProStateMachine.this.mHwDualBandBlackListMgr.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid, false);
                }
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("dualBandhandoverFailed  mAvailable5GAPSsid = " + WifiProStateMachine.this.mAvailable5GAPSsid);
                if (!(WifiProStateMachine.this.mAvailable5GAPBssid == null || WifiProStateMachine.this.mAvailable5GAPSsid == null)) {
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid);
                    WifiProStateMachine.this.mHwDualBandBlackListMgr.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid, false);
                    WifiProStateMachine.this.mHwDualBandBlackListMgr.addPermanentWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid, WifiProStateMachine.this.mAvailable5GAPBssid);
                }
            }
            this.isWifi2WifiProcess = false;
            this.isSwitching = false;
            if (!WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("wifi dual band handover over Failed and system auto connecting ap");
                WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
            }
        }

        private void updateWifiQosLevel(boolean isWiFiNoInternet, int qosLevel) {
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, 1);
            if (!WifiProStateMachine.this.mPowerManager.isScreenOn() && isWiFiNoInternet && this.mLastUpdatedQosLevel == 2) {
                return;
            }
            if (WifiProStateMachine.this.mPowerManager.isScreenOn() || isWiFiNoInternet || WifiProStateMachine.this.mCurrentWifiConfig == null || WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("updateWifiQosLevel, mCurrentSsid: " + WifiProStateMachine.this.mCurrentSsid + " ,isWiFiNoInternet: " + isWiFiNoInternet + ", qosLevel: " + qosLevel);
                if (isWiFiNoInternet) {
                    this.mLastUpdatedQosLevel = 2;
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                } else {
                    this.mLastUpdatedQosLevel = qosLevel;
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, qosLevel);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, qosLevel, 0);
                }
                return;
            }
            this.mLastUpdatedQosLevel = qosLevel;
        }

        private boolean handleMsgBySwitchOrDialogStatus(int level) {
            if (!this.isSwitching || !this.isDialogDisplayed) {
                return this.isSwitching || !this.isAllowWiFiHandoverMobile;
            }
            if (level > 2 && level != 6) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("Dialog is  Displayed, Qos is" + level + ", Cancel dialog.");
                WifiProStateMachine.this.updateWifiInternetStateChange(level);
                WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                boolean unused = WifiProStateMachine.this.mIsWiFiNoInternet = false;
                boolean unused2 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                wiFiLinkMonitorStateInit(false);
            }
            return true;
        }

        private void handleDualbandHandoverResult(Message msg) {
            WifiProStateMachine.this.logD("receive dual band wifi handover resust");
            if (this.isWifi2WifiProcess) {
                if (((Boolean) msg.obj).booleanValue()) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logD("dual band wifi handover is  succeed, ssid =" + WifiProStateMachine.this.mNewSelect_bssid + ", mBadSsid = " + WifiProStateMachine.this.mBadSsid);
                    this.isSwitching = false;
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mBadSsid);
                    String unused = WifiProStateMachine.this.mDualBandConnectAPSsid = WifiProStateMachine.this.mNewSelect_bssid;
                    long unused2 = WifiProStateMachine.this.mDualBandConnectTime = System.currentTimeMillis();
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                } else {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logD("dual band wifi handover is  failure, error reason = " + msg.arg1);
                    dualBandhandoverFailed(msg.arg1);
                }
                uploadDualbandSwitchInfo(((Boolean) msg.obj).booleanValue());
            }
        }

        private void uploadDualbandSwitchInfo(boolean success) {
            HwWifiCHRService mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            if (mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                int i = 0;
                data.putInt("Roam5GSuccCnt", (WifiProStateMachine.this.mDuanBandHandoverType != 1 || !success) ? 0 : 1);
                data.putInt("Roam5GFailedCnt", (WifiProStateMachine.this.mDuanBandHandoverType != 1 || success) ? 0 : 1);
                data.putInt("NoRoam5GSuccCnt", (WifiProStateMachine.this.mDuanBandHandoverType != 0 || !success) ? 0 : 1);
                if (WifiProStateMachine.this.mDuanBandHandoverType == 0 && !success) {
                    i = 1;
                }
                data.putInt("NoRoam5GFailedCnt", i);
                mHwWifiCHRService.uploadDFTEvent(909009065, data);
            }
        }

        private boolean pendingMsgBySelfCureEngine(int level) {
            if (level == -103 && !this.isSwitching) {
                if (HwSelfCureEngine.getInstance().isSelfCureOngoing() || !WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine.this.logD("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of self curing or supplicant not completed.");
                    return true;
                } else if (this.internetFailureDetectedCnt == 0 && !HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(WifiProStateMachine.this.mContext) && WifiProStateMachine.this.mCurrentRssi >= WifiProStateMachine.HANDOVER_5G_DIRECTLY_RSSI) {
                    this.internetFailureDetectedCnt++;
                    HwSelfCureEngine.getInstance().notifyInternetFailureDetected(false);
                    WifiProStateMachine.this.logD("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of requesting self cure.");
                    return true;
                }
            }
            return level == 0 && !this.isSwitching && HwSelfCureEngine.getInstance().isSelfCureOngoing();
        }

        private void handleWiFiRoveOut() {
            if (this.isDisableWifiAutoSwitch || WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext)) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logW("Disable Wifi Auto Switch, isDisableWifiAutoSwitch = " + this.isDisableWifiAutoSwitch);
                this.isSwitching = false;
                return;
            }
            if (HwQoEService.getInstance().isWeChating() && !HwQoEService.getInstance().isHandoverToMobile()) {
                WifiProStateMachine.this.logW("isWeChating so rove out handler by Streaming !");
                WifiInfo info = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                int currRssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(info.getFrequency(), info.getRssi());
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logW("isWeChating do not handover currRssiLevel = " + currRssiLevel);
                if (currRssiLevel <= 2) {
                    this.isSwitching = false;
                    return;
                }
            }
            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
            wifiProStateMachine3.logD("EVENT_TRY_WIFI_ROVE_OUT, allow wifi to mobile " + (!this.isWiFiHandoverPriority));
            String unused = WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
            String unused2 = WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
            this.isWifi2WifiProcess = true;
            int threshodRssi = WifiProStateMachine.THRESHOD_RSSI;
            if (this.isWiFiHandoverPriority) {
                threshodRssi = this.mWifi2WifiThreshod + 10;
            }
            if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), threshodRssi, 0)) {
                wifi2WifiFailed();
            }
        }
    }

    class WiFiProDisabledState extends State {
        WiFiProDisabledState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProDisabledState is Enter");
            boolean unused = WifiProStateMachine.mIsWifiManualEvaluating = false;
            boolean unused2 = WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.unRegisterCallBack();
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
            WifiProStateMachine.this.mHwIntelligenceWiFiManager.stop();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(false);
            WifiProStateMachine.this.stopDualBandManager();
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("WiFiProDisabledState , wifi is connect ");
                WifiInfo cInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && NetworkInfo.DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                    WifiProStateMachine.this.logD("wifi State == VERIFYING_POOR_LINK");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                }
                WifiProStateMachine.this.setWifiCSPState(1);
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
                    NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    if (networkInfo == null || NetworkInfo.DetailedState.VERIFYING_POOR_LINK != networkInfo.getDetailedState()) {
                        if (networkInfo == null || NetworkInfo.State.CONNECTING != networkInfo.getState()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                                WifiProStateMachine.this.setWifiCSPState(1);
                                break;
                            }
                        } else {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            break;
                        }
                    } else {
                        WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                        break;
                    } else {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = (Intent) msg.obj;
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                    if (conn_cfg != null) {
                        int change_reason = confg_intent.getIntExtra("changeReason", -1);
                        if (conn_cfg.isTempCreated && change_reason != 1) {
                            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                            wifiProStateMachine.logD("WiFiProDisabledState, forget " + conn_cfg.SSID);
                            WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            break;
                        }
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WiFiProEnableState extends State {
        WiFiProEnableState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProEnableState is Enter");
            boolean unused = WifiProStateMachine.this.mIsWiFiNoInternet = false;
            int unused2 = WifiProStateMachine.this.mWiFiProPdpSwichValue = 0;
            WifiProStateMachine.this.registerCallBack();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(true);
            boolean unused3 = WifiProStateMachine.mIsWifiManualEvaluating = false;
            boolean unused4 = WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            boolean unused5 = WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = false;
            if (WifiProStateMachine.this.mIsWiFiProEnabled) {
                boolean unused6 = WifiProStateMachine.this.mIsWifiproDisableOnReboot = false;
                WifiProStateMachine.this.startDualBandManager();
                WifiProStateMachine.this.mHwIntelligenceWiFiManager.start();
            }
            transitionNetState();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProEnableState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                        break;
                    } else {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != 1) {
                        if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.mWiFiProEvaluateController.initWifiProEvaluateRecords();
                            break;
                        }
                    } else {
                        WifiProStateMachine.this.logD("wifi state is DISABLED, go to wifi disconnected");
                        if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                            WifiProStateMachine.this.logD("BQE bad rove out, wifi disable time recorded.");
                            long unused = WifiProStateMachine.this.mChrWifiDidableStartTime = System.currentTimeMillis();
                        }
                        if (WifiProStateMachine.this.shouldUploadCloseWifiEvent()) {
                            WifiProStateMachine.this.mWifiProStatisticsManager.uploadWifiproEvent(909002058);
                        }
                        WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
                        if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiDisConnectedState) {
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    if (TextUtils.isEmpty(WifiProStateMachine.this.mUserManualConnecConfigKey)) {
                        if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mWifiManager.isWifiEnabled()) {
                            if (System.currentTimeMillis() - WifiProStateMachine.this.mLastDisconnectedTime >= 6000) {
                                NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                                if (wifi_info != null) {
                                    List unused2 = WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWifiManager.getScanResults();
                                    List unused3 = WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mScanResultList);
                                    if (!(WifiProStateMachine.this.mScanResultList == null || WifiProStateMachine.this.mScanResultList.size() == 0)) {
                                        boolean issetting = WifiProStateMachine.this.isSettingsActivity();
                                        int evaluate_type = 0;
                                        if (issetting) {
                                            evaluate_type = 1;
                                        }
                                        if (!WifiProCommonUtils.isWifiConnectedOrConnecting(WifiProStateMachine.this.mWifiManager) && wifi_info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                                            if (!WifiProStateMachine.this.isMapNavigating && !WifiProStateMachine.this.isVehicleState) {
                                                if (!WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                                                    if (WifiProStateMachine.this.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList)) {
                                                        for (ScanResult scanResult : WifiProStateMachine.this.mScanResultList) {
                                                            if (WifiProStateMachine.this.mWiFiProEvaluateController.isAllowEvaluate(scanResult, evaluate_type) && !WifiProStateMachine.this.mWiFiProEvaluateController.isLastEvaluateValid(scanResult, evaluate_type)) {
                                                                WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(scanResult, evaluate_type);
                                                            }
                                                        }
                                                        WifiProStateMachine.this.mWiFiProEvaluateController.orderByRssi();
                                                        boolean isfactorymode = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
                                                        if (!WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty() && !isfactorymode) {
                                                            WifiProStateMachine.this.mWiFiProEvaluateController.unEvaluateAPQueueDump();
                                                            WifiProStateMachine.this.logD("transition to mwifiSemiAutoEvaluateState, to evaluate ap");
                                                            if (issetting) {
                                                                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(2);
                                                            } else {
                                                                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(1);
                                                            }
                                                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                                            break;
                                                        } else {
                                                            WifiProStateMachine.this.logD("UnEvaluateAPRecords is Empty");
                                                            break;
                                                        }
                                                    } else {
                                                        WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                                        break;
                                                    }
                                                } else {
                                                    WifiProStateMachine.this.logD("P2PConnectedOrConnecting, ignor this scan result");
                                                    WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                                    break;
                                                }
                                            } else {
                                                WifiProStateMachine.this.logD("MapNavigatingOrVehicleState, ignor this scan result");
                                                break;
                                            }
                                        } else {
                                            WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                WifiProStateMachine.this.logD("Disconnected time less than 6s, ignor this scan result");
                                break;
                            }
                        }
                    } else {
                        WifiProStateMachine.this.logD("User manual connecting ap, ignor this evaluate scan result");
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_EVALUTE_STATE_CHANGE /*136298*/:
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                case WifiProStateMachine.EVENT_LOAD_CONFIG_INTERNET_INFO /*136315*/:
                    WifiProStateMachine.this.logD("WiFiProEnableState EVENT_LOAD_CONFIG_INTERNET_INFO");
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.mWiFiProEvaluateController.initWifiProEvaluateRecords();
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_NETWROK_TYPE /*136316*/:
                    handleDualBandNetworkType(msg);
                    break;
                default:
                    return false;
            }
            return true;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v11, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.util.List} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void handleDualBandNetworkType(Message msg) {
            List<HwDualBandMonitorInfo> apList = null;
            int type = msg.arg1;
            if (msg.obj != null) {
                apList = msg.obj;
            }
            if (apList == null || apList.size() == 0) {
                WifiProStateMachine.this.loge("onDualBandNetWorkType apList null error");
            } else if (WifiProStateMachine.this.mIsUserManualConnectSuccess) {
                WifiProStateMachine.this.logD("keep curreny connect,ignore dualband ap handover");
            } else {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("onDualBandNetWorkType type = " + type + " apList.size() = " + apList.size());
                WifiProStateMachine.this.mDualBandMonitorApList.clear();
                int unused = WifiProStateMachine.this.mDualBandMonitorInfoSize = apList.size();
                for (HwDualBandMonitorInfo monitorInfo : apList) {
                    WifiProStateMachine.this.mDualBandMonitorApList.add(monitorInfo);
                    WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                    apInfo.setApBssid(monitorInfo.mBssid);
                    apInfo.setApRssi(monitorInfo.mCurrentRssi);
                    apInfo.setApAuthType(monitorInfo.mAuthType);
                    WifiProStateMachine.this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
                }
            }
        }

        private void transitionNetState() {
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("WiFiProEnableState,go to WifiConnectedState");
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                return;
            }
            WifiProStateMachine.this.logD("WiFiProEnableState, go to mWifiDisConnectedState");
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
        }
    }

    class WiFiProVerfyingLinkState extends State {
        private int internetFailureDetectedCount;
        private volatile boolean isRecoveryWifi;
        private boolean isWifiGoodIntervalTimerOut;
        private volatile boolean isWifiHandoverWifi;
        private boolean isWifiRecoveryTimerOut;
        private boolean isWifiScanScreenOff;
        private int wifiNoInternetCounter;
        private int wifiQosLevel;
        private int wifiScanCounter;
        private int wifi_internet_level;

        WiFiProVerfyingLinkState() {
        }

        private void startScanAndMonitor(long time) {
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, WifiProStateMachine.DELAY_UPLOAD_MS);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(true);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(true);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                int unused = WifiProStateMachine.this.mCurrentWifiLevel = -1;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("WiFiProVerfyingLinkState, wifi is No Internet,delay check time = " + time);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, time);
                return;
            }
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(2, true);
        }

        private void cancelScanAndMonitor() {
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(false);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(false);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(2, false);
        }

        private void restoreWifiConnect() {
            cancelScanAndMonitor();
            WifiProStateMachine.this.logD("restoreWifiConnect, mWsmChannel send GOOD Link Detected");
            WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            WifiProStateMachine.this.notifyManualConnectAP(WifiProStateMachine.this.mIsUserManualConnectSuccess, WifiProStateMachine.this.mIsUserHandoverWiFi);
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState is Enter");
            this.isRecoveryWifi = false;
            this.isWifiHandoverWifi = false;
            this.isWifiRecoveryTimerOut = false;
            this.isWifiGoodIntervalTimerOut = true;
            boolean unused = WifiProStateMachine.this.mIsUserManualConnectSuccess = false;
            this.wifiNoInternetCounter = 0;
            this.internetFailureDetectedCount = 0;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            this.wifiScanCounter = 0;
            this.isWifiScanScreenOff = false;
            if (WifiProStateMachine.this.mCurrentVerfyCounter > 4) {
                int unused2 = WifiProStateMachine.this.mCurrentVerfyCounter = 4;
            }
            long delay_time = ((long) Math.pow(2.0d, (double) WifiProStateMachine.this.mCurrentVerfyCounter)) * 60 * 1000;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("WiFiProVerfyingLinkState : CurrentWifiLevel = " + WifiProStateMachine.this.mCurrentWifiLevel + ", CurrentVerfyCounter = " + WifiProStateMachine.this.mCurrentVerfyCounter + ", delay_time = " + delay_time);
            int unused3 = WifiProStateMachine.this.mCurrentVerfyCounter = WifiProStateMachine.this.mCurrentVerfyCounter + 1;
            startScanAndMonitor(delay_time);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT, delay_time);
            if (WifiProStateMachine.this.mCurrentVerfyCounter == 3) {
                WifiProStateMachine.this.logW("network has handover 3 times,maybe ping-pong");
                WifiProStateMachine.this.mWifiProStatisticsManager.increasePingPongCount();
            }
            HwWifiConnectivityMonitor.getInstance().notifyVerifyingLinkState(true);
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(1);
            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.logD("BQE bad rove out started.");
                long unused4 = WifiProStateMachine.this.mChrRoveOutStartTime = System.currentTimeMillis();
                String unused5 = WifiProStateMachine.this.mRoSsid = WifiProStateMachine.this.mCurrentSsid;
                boolean unused6 = WifiProStateMachine.this.mLoseInetRoveOut = false;
            } else {
                boolean unused7 = WifiProStateMachine.this.mLoseInetRoveOut = true;
            }
            boolean unused8 = WifiProStateMachine.this.mRoveOutStarted = true;
            boolean unused9 = WifiProStateMachine.this.mIsRoveOutToDisconn = false;
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_LAA_STATUS_CHANGED, 3000);
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState is Exit");
            cancelScanAndMonitor();
            boolean unused = WifiProStateMachine.this.mIsWiFiNoInternet = false;
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_MOBILE_SWITCH_DELAY);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_LAA_STATUS_CHANGED);
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(0);
            HwWifiConnectivityMonitor.getInstance().notifyVerifyingLinkState(false);
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i != WifiProStateMachine.EVENT_LAA_STATUS_CHANGED) {
                if (i != WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE) {
                    int delayMs = 0;
                    switch (i) {
                        case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                            Intent intent = (Intent) msg.obj;
                            if (intent != null && !this.isWifiHandoverWifi) {
                                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                                if (networkInfo != null) {
                                    WifiProStateMachine.this.logD("WiFiProVerfyingLinkState :Network state change " + networkInfo.getDetailedState());
                                }
                                if (networkInfo == null || NetworkInfo.State.DISCONNECTED != networkInfo.getState()) {
                                    if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                                        this.isRecoveryWifi = false;
                                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(4);
                                        WifiProStateMachine.this.logD("WiFiProVerfyingLinkState: Restore the wifi connection successful,go to mWiFiLinkMonitorState");
                                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, true);
                                        boolean unused = WifiProStateMachine.this.mVerfyingToConnectedState = true;
                                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                                        break;
                                    }
                                } else {
                                    WifiProStateMachine.this.logD("WiFiProVerfyingLinkState : wifi has disconnected");
                                    WifiProStateMachine.this.updatePortalNetworkInfo();
                                    boolean unused2 = WifiProStateMachine.this.mIsRoveOutToDisconn = true;
                                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                                    break;
                                }
                            }
                            break;
                        case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                            if (!this.isWifiScanScreenOff) {
                                WifiProStateMachine.this.logD("isWifiScanScreenOff = false, wait a moment, retry scan wifi later");
                                break;
                            } else {
                                WifiProStateMachine.this.logD("isWifiScanScreenOff = true, retry scan wifi");
                                this.isWifiScanScreenOff = false;
                                this.wifiScanCounter = 0;
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                                break;
                            }
                        case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                            if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                                WifiProStateMachine.this.logD("wifiprochr user close wifipro");
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(5);
                                boolean unused3 = WifiProStateMachine.this.mRoveOutStarted = false;
                                WifiProStateMachine.this.onDisableWiFiPro();
                                break;
                            }
                        case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                            handleWifiQosChangedInVerifyLinkState(msg);
                            break;
                        case WifiProStateMachine.EVENT_MOBILE_QOS_CHANGE /*136173*/:
                            if (msg.arg1 <= 2 && this.isWifiRecoveryTimerOut) {
                                if (!WifiProStateMachine.this.mIsWiFiNoInternet || msg.arg1 > 0) {
                                    if (!this.isRecoveryWifi && !this.isWifiHandoverWifi && WifiProStateMachine.this.mCurrentWifiLevel != 0 && !WifiProStateMachine.this.isWiFiPoorer(WifiProStateMachine.this.mCurrentWifiLevel, msg.arg1)) {
                                        WifiProStateMachine.this.logD("Mobile Qos is poor,try restore wifi,mobile handover wifi");
                                        this.isRecoveryWifi = true;
                                        restoreWifiConnect();
                                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(7);
                                        break;
                                    }
                                } else {
                                    WifiProStateMachine.this.logD("both wifi and mobile is unusable,can not restore wifi ");
                                    break;
                                }
                            }
                            break;
                        default:
                            switch (i) {
                                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                                    if (!this.isRecoveryWifi && !this.isWifiHandoverWifi) {
                                        if (!((Boolean) msg.obj).booleanValue()) {
                                            WifiProStateMachine.this.logD("There is no vailble ap, continue verfyinglink");
                                            break;
                                        } else {
                                            WifiProStateMachine.this.logD("Exist a vailable AP,connect this AP and cancel Sacn Timer");
                                            this.isWifiHandoverWifi = true;
                                            if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, 0)) {
                                                this.isWifiHandoverWifi = false;
                                                break;
                                            }
                                        }
                                    } else {
                                        WifiProStateMachine.this.logD("receive check available ap result,but is isRecoveryWifi");
                                        break;
                                    }
                                    break;
                                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                                    NetworkInfo mobileInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(0);
                                    WifiProStateMachine.this.logD("networkConnetc change :mobileInfo : " + mobileInfo + ", mIsMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
                                    if (WifiProStateMachine.this.mIsMobileDataEnabled && mobileInfo != null && NetworkInfo.State.DISCONNECTED == mobileInfo.getState()) {
                                        WifiProStateMachine.this.logD("mobile network service is disconnected, mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
                                        NetworkInfo wInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                                        if (!WifiProStateMachine.this.mIsWiFiNoInternet && wInfo != null && NetworkInfo.DetailedState.VERIFYING_POOR_LINK == wInfo.getDetailedState()) {
                                            this.isWifiHandoverWifi = false;
                                            restoreWifiConnect();
                                            break;
                                        }
                                    }
                                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /*136178*/:
                                    this.isWifiHandoverWifi = false;
                                    if (!((Boolean) msg.obj).booleanValue()) {
                                        if (WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mCurrentSsid)) {
                                            if (WifiProStateMachine.this.isWifiConnected()) {
                                                WifiProStateMachine.this.logD("wifi handover wifi fail, continue monitor");
                                                break;
                                            }
                                        } else {
                                            WifiProStateMachine.this.logW("connect other AP wifi : Fallure");
                                            WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
                                            WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mCurrentBssid);
                                            break;
                                        }
                                    } else {
                                        WifiProStateMachine.this.logD("connect other AP wifi : succeed ,go to WifiConnectedState, add WifiBlacklist: " + WifiProStateMachine.this.mCurrentSsid);
                                        WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mCurrentBssid);
                                        cancelScanAndMonitor();
                                        WifiProStateMachine.this.refreshConnectedNetWork();
                                        WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                                        WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                                        restoreWifiConnect();
                                        break;
                                    }
                                    break;
                                default:
                                    switch (i) {
                                        case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                                            handleCheckInternetResultInVerifyLinkState(msg);
                                            break;
                                        case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                                        case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                                            break;
                                        default:
                                            switch (i) {
                                                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                                                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != 1) {
                                                        if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                                                            WifiProStateMachine.this.logD("wifi state is : enabled, forgetUntrustedOpenAp");
                                                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                                            break;
                                                        }
                                                    } else {
                                                        WifiProStateMachine.this.logD("wifi state is : " + WifiProStateMachine.this.mWifiManager.getWifiState() + " ,go to wifi disconnected");
                                                        boolean unused4 = WifiProStateMachine.this.mIsRoveOutToDisconn = true;
                                                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                                                        break;
                                                    }
                                                    break;
                                                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                                                    if (WifiProStateMachine.this.mIsMobileDataEnabled) {
                                                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI)) {
                                                            WifiProStateMachine.this.logD("In verifying link state, MOBILE DATA is ON within delay time, cancel switching back to wifi.");
                                                            WifiProStateMachine.this.getHandler().removeMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI);
                                                            break;
                                                        }
                                                    } else {
                                                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                            delayMs = HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT;
                                                        }
                                                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                                                        wifiProStateMachine.logD("In verifying link state, MOBILE DATA is OFF, try to delay " + delayMs + " ms to switch back to wifi.");
                                                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI), (long) delayMs);
                                                        break;
                                                    }
                                                    break;
                                                case WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT /*136187*/:
                                                    this.isWifiGoodIntervalTimerOut = true;
                                                    break;
                                                case WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT /*136188*/:
                                                    this.isWifiRecoveryTimerOut = true;
                                                    WifiProStateMachine.this.logD("isWifiRecoveryTimerOut is true,mobile can handover to wifi");
                                                    break;
                                                case WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI /*136189*/:
                                                    WifiProStateMachine.this.logW("WiFiProVerfyingLinkState::EVENT_MOBILE_RECOVERY_TO_WIFI, handle it.");
                                                    this.isWifiHandoverWifi = false;
                                                    restoreWifiConnect();
                                                    WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(3);
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /*136191*/:
                                                            if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                                                                this.isWifiScanScreenOff = true;
                                                                break;
                                                            } else {
                                                                WifiProStateMachine.this.logD("inquire the surrounding AP for wifiHandover");
                                                                WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                                                                this.wifiScanCounter++;
                                                                this.wifiScanCounter = Math.min(this.wifiScanCounter, 12);
                                                                long delay_scan_time = ((long) Math.pow(2.0d, (double) (this.wifiScanCounter / 4))) * 60 * 1000;
                                                                WifiProStateMachine.this.logD("delay_scan_time = " + delay_scan_time);
                                                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delay_scan_time);
                                                                break;
                                                            }
                                                        case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                                                            handleCheckInternetInVerifyLinkState(msg);
                                                            break;
                                                        case WifiProStateMachine.EVENT_USER_ROVE_IN /*136193*/:
                                                            boolean unused5 = WifiProStateMachine.this.mIsUserHandoverWiFi = true;
                                                            this.isWifiHandoverWifi = false;
                                                            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserManualRICount();
                                                            } else {
                                                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(4);
                                                            }
                                                            restoreWifiConnect();
                                                            break;
                                                        default:
                                                            return false;
                                                    }
                                            }
                                    }
                            }
                            break;
                    }
                }
            } else {
                boolean is24gConnected = !WifiProCommonUtils.isWifi5GConnected(WifiProStateMachine.this.mWifiManager);
                if (HwLaaUtils.isLaaPlusEnable() && HwLaaController.getInstrance() != null) {
                    HwLaaController.getInstrance().setLAAEnabled(is24gConnected, 4);
                }
            }
            return true;
        }

        private void handleCheckInternetResultInVerifyLinkState(Message msg) {
            this.wifi_internet_level = msg.arg1;
            WifiProStateMachine.this.logD("WiFi internet level = " + this.wifi_internet_level + ", wifiQosLevel = " + this.wifiQosLevel);
            WifiProStateMachine.this.logD("mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + " ,isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut);
            if (this.isWifiRecoveryTimerOut) {
                if (this.wifi_internet_level == -1 || this.wifi_internet_level == 6) {
                    WifiProStateMachine.this.logD("WiFiProVerfyingLinkState wifi no internet detected time = " + this.wifiNoInternetCounter);
                    this.wifiQosLevel = 0;
                    this.wifiNoInternetCounter = this.wifiNoInternetCounter + 1;
                    if (this.wifi_internet_level == -1 && this.internetFailureDetectedCount == 0 && !HwSelfCureEngine.getInstance().isSelfCureOngoing() && WifiProStateMachine.this.isWifiConnected()) {
                        int unused = WifiProStateMachine.this.mCurrentRssi = WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager);
                        WifiProStateMachine.this.logD("WiFiProVerfyingLinkState mCurrentRssi = " + WifiProStateMachine.this.mCurrentRssi);
                        if (WifiProStateMachine.this.mCurrentRssi >= WifiProStateMachine.HANDOVER_5G_DIRECTLY_RSSI) {
                            HwSelfCureEngine.getInstance().notifyInternetFailureDetected(true);
                            this.internetFailureDetectedCount++;
                        }
                    }
                }
                if (!WifiProStateMachine.this.mIsWiFiNoInternet && !this.isWifiHandoverWifi && this.isRecoveryWifi) {
                    if (this.wifi_internet_level == -1 || this.wifi_internet_level == 6 || this.wifiQosLevel <= 2) {
                        this.isRecoveryWifi = false;
                    } else {
                        WifiProStateMachine.this.logD("wifi Qos is [" + this.wifiQosLevel + " ]Ok, wifi_internet_level is [" + this.wifi_internet_level + "] Restore the wifi connection");
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(1);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, 3);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, 3, 0);
                        restoreWifiConnect();
                    }
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet && this.wifi_internet_level != -1 && this.wifi_internet_level != 6 && !this.isWifiHandoverWifi) {
                    boolean unused2 = WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    if (!this.isRecoveryWifi) {
                        this.isRecoveryWifi = true;
                        WifiProStateMachine.this.logD("wifi Internet is better ,try restore wifi 2,mobile handover wifi");
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetRestoreRICount();
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 4, WifiProStateMachine.this.mCurrentSsid);
                        restoreWifiConnect();
                    }
                }
            }
        }

        private void handleCheckInternetInVerifyLinkState(Message msg) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logW("start check wifi internet, wifiNoInternetCounter = " + this.wifiNoInternetCounter);
            int i = 12;
            if (this.wifiNoInternetCounter <= 12) {
                i = this.wifiNoInternetCounter;
            }
            this.wifiNoInternetCounter = i;
            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, true);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, ((long) Math.pow(2.0d, Math.floor(((double) this.wifiNoInternetCounter) / 4.0d))) * 30000);
        }

        private void handleWifiQosChangedInVerifyLinkState(Message msg) {
            if (!((Boolean) msg.obj).booleanValue()) {
                if (msg.arg1 == 3) {
                    this.isWifiRecoveryTimerOut = true;
                }
                if (this.isRecoveryWifi || this.isWifiHandoverWifi || !this.isWifiRecoveryTimerOut || !WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logD("isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut + ", isRecoveryWifi = " + this.isRecoveryWifi);
                    return;
                }
                this.wifiQosLevel = msg.arg1;
                if (this.wifiQosLevel > 2 && !WifiProStateMachine.this.mIsWiFiNoInternet && this.isWifiGoodIntervalTimerOut) {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logD("wifi Qos is [" + this.wifiQosLevel + " ]Ok, start check wifi internet, wifiNoInternetCounter = " + this.wifiNoInternetCounter);
                    this.isRecoveryWifi = true;
                    this.isWifiGoodIntervalTimerOut = false;
                    int i = 12;
                    if (this.wifiNoInternetCounter <= 12) {
                        i = this.wifiNoInternetCounter;
                    }
                    this.wifiNoInternetCounter = i;
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT, ((long) Math.pow(2.0d, Math.floor(((double) this.wifiNoInternetCounter) / 4.0d))) * 30000);
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, true);
                }
            }
        }
    }

    class WifiConnectedState extends State {
        private int internetFailureDetectedCount;
        private boolean isChrShouldReport;
        private boolean isIgnorAvailableWifiCheck;
        private boolean isKeepConnected;
        private boolean isPortalAP;
        private boolean isPortalChrEverUploaded;
        private boolean isToastDisplayed;
        private int oldType;
        private int portalCheckCounter;

        WifiConnectedState() {
        }

        private void initConnectedState() {
            WifiProStateMachine.this.setWifiEvaluateTag(false);
            boolean unused = WifiProStateMachine.this.mIsWiFiNoInternet = false;
            boolean unused2 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
            this.isKeepConnected = false;
            this.isPortalAP = false;
            this.portalCheckCounter = 0;
            boolean unused3 = WifiProStateMachine.this.mIsScanedRssiLow = false;
            boolean unused4 = WifiProStateMachine.this.mIsScanedRssiMiddle = false;
            this.isIgnorAvailableWifiCheck = true;
            boolean unused5 = WifiProStateMachine.this.isDialogUpWhenConnected = false;
            boolean unused6 = WifiProStateMachine.this.mIsPortalAp = false;
            boolean unused7 = WifiProStateMachine.this.mIsNetworkAuthen = false;
            this.isPortalChrEverUploaded = false;
            WifiProStateMachine.this.refreshConnectedNetWork();
            int unused8 = WifiProStateMachine.this.mLastWifiLevel = 0;
            this.internetFailureDetectedCount = 0;
            WifiProStateMachine.this.setWifiCSPState(1);
            boolean unused9 = WifiProStateMachine.this.mHiLinkUnconfig = isHiLinkUnconfigRouter();
            if (!TextUtils.isEmpty(WifiProStateMachine.this.mUserManualConnecConfigKey) && WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mUserManualConnecConfigKey.equals(WifiProStateMachine.this.mCurrentWifiConfig.configKey())) {
                boolean unused10 = WifiProStateMachine.this.mIsUserManualConnectSuccess = true;
                long deltaTime = System.currentTimeMillis() - WifiProStateMachine.this.mLastDisconnectedTimeStamp;
                if (WifiProStateMachine.this.mCurrentSsid != null && !WifiProStateMachine.this.mCurrentSsid.equals(WifiProStateMachine.this.mLastConnectedSsid) && deltaTime < 10000 && WifiProStateMachine.this.mLastDisconnectedRssi < -75) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.uploadWifiproEvent(909002059);
                }
                WifiProStateMachine.this.logD("User manual connect ap success!");
            }
            String unused11 = WifiProStateMachine.this.mUserManualConnecConfigKey = "";
            WifiProStateMachine.this.notifyManualConnectAP(WifiProStateMachine.this.mIsUserManualConnectSuccess, WifiProStateMachine.this.mIsUserHandoverWiFi);
            if (WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                WifiProStateMachine.this.refreshConnectedNetWork();
                WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
            }
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("isAllowWiFiAutoEvaluate == " + WifiProStateMachine.this.isAllowWiFiAutoEvaluate());
            WifiConfiguration cfg = WifiProCommonUtils.getCurrentWifiConfig(WifiProStateMachine.this.mWifiManager);
            if (cfg != null) {
                int accessType = cfg.internetAccessType;
                int qosLevel = cfg.networkQosLevel;
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logD("accessType = : " + accessType + ",qosLevel = " + qosLevel + ",wifiProNoInternetAccess = " + cfg.wifiProNoInternetAccess);
                if (cfg.isTempCreated) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(19);
                }
                if (4 == accessType) {
                    int temporaryQoeLevel = WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel();
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, temporaryQoeLevel);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, temporaryQoeLevel, 0);
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.logD("WiFiProConnected temporaryQosLevel = " + temporaryQoeLevel);
                } else if (3 == accessType || 2 == accessType) {
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                } else {
                    WiFiProScoreInfo wiFiProScoreInfo = WiFiProEvaluateController.getCurrentWiFiProScore(WifiProStateMachine.this.mCurrentSsid);
                    if (wiFiProScoreInfo != null && (3 == wiFiProScoreInfo.internetAccessType || 2 == wiFiProScoreInfo.internetAccessType)) {
                        WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                        wifiProStateMachine4.logD("WiFiProConnected internetAccessType = " + wiFiProScoreInfo.internetAccessType);
                        WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                    }
                }
            } else {
                WifiProStateMachine.this.logD("cfg= null ");
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, 1);
            }
        }

        private void reportDiffTypeCHR(int newType) {
            if (!this.isChrShouldReport) {
                this.isChrShouldReport = true;
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(WifiProStateMachine.this.mCurrentSsid, 0);
                int diffType = WifiProStateMachine.this.mWiFiProEvaluateController.getChrDiffType(this.oldType, newType);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("reportDiffTypeCHR is Enter, diffType  == " + diffType);
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
            WifiProStateMachine.this.logD("WifiConnectedState is Enter");
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
            WifiProStateMachine.this.refreshConnectedNetWork();
            this.oldType = WifiProStateMachine.this.mWiFiProEvaluateController.getOldNetworkType(WifiProStateMachine.this.mCurrentSsid);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("WiFiProConnected oldType = " + this.oldType);
            NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifi_info != null && wifi_info.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logD(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            }
            if (WifiProStateMachine.this.mNetworkBlackListManager.isInTempWifiBlackList(WifiProStateMachine.this.mWifiManager.getConnectionInfo().getBSSID())) {
                WifiProStateMachine.this.logD("cleanTempBlackList for this bssid.");
                WifiProStateMachine.this.mNetworkBlackListManager.cleanTempWifiBlackList();
            }
            if (!WifiProStateMachine.this.mPhoneStateListenerRegisted) {
                WifiProStateMachine.this.logD("start PhoneStateListener");
                WifiProStateMachine.this.mTelephonyManager.listen(WifiProStateMachine.this.phoneStateListener, 32);
                boolean unused = WifiProStateMachine.this.mPhoneStateListenerRegisted = true;
            }
            initConnectedState();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiConnectedState is Exit");
            this.isToastDisplayed = false;
            this.isChrShouldReport = false;
            this.oldType = 0;
            boolean unused = WifiProStateMachine.this.mVerfyingToConnectedState = false;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            cancelPortalExpiredNotifyStatusBar();
            String unused2 = WifiProStateMachine.this.respCodeChrInfo = "";
            int unused3 = WifiProStateMachine.this.detectionNumSlow = 0;
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
        }

        public boolean processMessage(Message msg) {
            int result = -1;
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo n = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    if (n != null && NetworkInfo.State.DISCONNECTED == n.getState()) {
                        WifiProStateMachine.this.updatePortalNetworkInfo();
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                        break;
                    }
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /*136190*/:
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork && WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.isAllowWifi2Mobile()) {
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        break;
                    }
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    handleCheckAvailableApResult(msg);
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    handleCheckWifiInternetResultWithConnected(msg);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                    handleUserSelectDialogOk();
                    break;
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                    handleDialogCancel();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.logD("Screen off, cancel network check! mIsPortalAp " + WifiProStateMachine.this.mIsPortalAp);
                        if (WifiProStateMachine.this.mIsPortalAp) {
                            result = 6;
                        }
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, result);
                        break;
                    } else {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                        break;
                    }
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /*136195*/:
                    if (msg.obj == null || !((Boolean) msg.obj).booleanValue()) {
                        if (msg.obj != null && !((Boolean) msg.obj).booleanValue()) {
                            WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ the http unreachable.");
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, -1);
                            break;
                        }
                    } else {
                        this.internetFailureDetectedCount = 0;
                        WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ to check wifi immediately.");
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_NETWORK_USER_CONNECT /*136202*/:
                    if (msg.obj != null && ((Boolean) msg.obj).booleanValue()) {
                        boolean unused = WifiProStateMachine.this.mIsUserManualConnectSuccess = true;
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.logD("receive EVENT_NETWORK_USER_CONNECT, set mIsUserManualConnectSuccess = " + WifiProStateMachine.this.mIsUserManualConnectSuccess);
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_CHECK_UNKOWN /*136309*/:
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /*136311*/:
                    WifiProStateMachine.this.handleGetWifiTcpRx();
                    break;
                case WifiProStateMachine.EVENT_WIFI_NO_INTERNET_NOTIFICATION /*136318*/:
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, true, 0, false);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                    break;
                case WifiProStateMachine.EVENT_PORTAL_SELECTED /*136319*/:
                    WifiProStateMachine.this.logD("###MSG_PORTAL_SELECTED");
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                        if (!this.isPortalChrEverUploaded) {
                            WifiProStateMachine.this.logD("user clicks the notification, upload CHR");
                            WifiProStateMachine.this.uploadPortalAuthExpirationStatistics(true);
                            this.isPortalChrEverUploaded = true;
                        }
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED);
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_LAUNCH_BROWSER, 500);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_LAUNCH_BROWSER /*136320*/:
                    String usedUrl = Settings.Global.getString(WifiProStateMachine.this.mContext.getContentResolver(), "captive_portal_server");
                    if (HwAutoConnectManager.getInstance() != null) {
                        HwAutoConnectManager.getInstance().launchBrowserForPortalLogin(usedUrl, WifiProStateMachine.this.mCurrentWifiConfig.configKey());
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED /*136321*/:
                    WifiProStateMachine.this.logD("alarm expired, upload CHR");
                    if (!this.isPortalChrEverUploaded) {
                        WifiProStateMachine.this.uploadPortalAuthExpirationStatistics(false);
                        this.isPortalChrEverUploaded = true;
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void cancelPortalExpiredNotifyStatusBar() {
            if (WifiProStateMachine.this.mPortalNotificationId != -1 && WifiProStateMachine.this.mCurrentWifiConfig != null) {
                WifiProStateMachine.this.mWifiProUIDisplayManager.cancelPortalNotificationStatusBar(WifiProStateMachine.PORTAL_STATUS_BAR_TAG, WifiProStateMachine.this.mPortalNotificationId);
                WifiProStateMachine.this.notifyPortalStatusChanged(false, WifiProStateMachine.this.mCurrentWifiConfig.configKey(), WifiProStateMachine.this.mCurrentWifiConfig.lastHasInternetTimestamp > 0);
                int unused = WifiProStateMachine.this.mPortalNotificationId = -1;
            }
        }

        private void handleCheckWifiInternetResultWithConnected(Message msg) {
            Message message = msg;
            WifiProStateMachine.this.logD("WiFi internet check level = " + message.arg1 + ", isKeepConnected = " + this.isKeepConnected + ", mIsUserHandoverWiFi = " + WifiProStateMachine.this.mIsUserHandoverWiFi);
            WifiProStateMachine.this.notifyNetworkCheckResult(message.arg1);
            reportDiffTypeCHR(WifiProStateMachine.this.mWiFiProEvaluateController.getNewNetworkType(message.arg1));
            if (!this.isKeepConnected || WifiProStateMachine.this.mIsUserHandoverWiFi) {
                this.isKeepConnected = false;
                int internet_level = message.arg1;
                if (!WifiProStateMachine.this.isDialogUpWhenConnected || !(internet_level == -1 || internet_level == 6)) {
                    if (this.isPortalAP) {
                        if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                            this.portalCheckCounter++;
                        }
                        WifiProStateMachine.this.logD("portalCheckCounter = " + this.portalCheckCounter);
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        if (internet_level == 6 || internet_level == -1) {
                            if (internet_level == -1) {
                                internetFailureSelfcure();
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                            return;
                        }
                    }
                    if (internet_level == -1) {
                        WifiProStateMachine.this.logD("WiFi NO internet,isPortalAP = " + this.isPortalAP);
                        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
                        if (mHwWifiCHRService != null && !WifiProStateMachine.this.mIsWiFiInternetCHRFlag && !this.isPortalAP) {
                            WifiProStateMachine.this.logD("upload WIFI_ACCESS_INTERNET_FAILED event for FIRST_CONNECT_NO_INTERNET,ssid:" + WifiProStateMachine.this.mCurrentSsid);
                            mHwWifiCHRService.updateWifiException(87, "FIRST_CONNECT_NO_INTERNET");
                        }
                        boolean unused = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = true;
                        boolean unused2 = WifiProStateMachine.this.mIsWiFiNoInternet = true;
                        if (WifiProStateMachine.this.mIsUserManualConnectSuccess) {
                            int unused3 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                            return;
                        }
                        if (WifiProStateMachine.this.mVerfyingToConnectedState && !this.isPortalAP) {
                            internetFailureSelfcure();
                        }
                        if (this.isPortalAP) {
                            this.isPortalAP = false;
                            int unused4 = WifiProStateMachine.this.mWiFiNoInternetReason = 1;
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 3);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 3, WifiProStateMachine.this.mCurrentSsid);
                        } else {
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                            int unused5 = WifiProStateMachine.this.mWiFiNoInternetReason = 0;
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(true);
                        }
                        if (this.isIgnorAvailableWifiCheck) {
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        } else if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork);
                        }
                        if (this.isIgnorAvailableWifiCheck && !HwSelfCureEngine.getInstance().isSelfCureOngoing() && !WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                            WifiProStateMachine.this.logD("inquire the surrounding AP for wifiHandover");
                            this.isIgnorAvailableWifiCheck = false;
                            WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                        } else if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                            int unused6 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                        } else {
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        }
                    } else if (internet_level == 6) {
                        WifiProStateMachine.this.logD("WifiConnectedState: WiFi is protal");
                        this.isPortalAP = true;
                        WifiProStateMachine.this.setWifiMonitorEnabled(true);
                        boolean unused7 = WifiProStateMachine.this.mIsPortalAp = true;
                        boolean unused8 = WifiProStateMachine.this.mIsNetworkAuthen = false;
                        int unused9 = WifiProStateMachine.this.mWiFiNoInternetReason = 1;
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 3);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 3, WifiProStateMachine.this.mCurrentSsid);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, true, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        if (WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp != 0) {
                            WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp = 0;
                            WifiProStateMachine.this.updateWifiConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                        if (WifiProStateMachine.this.mIsWiFiProEnabled) {
                            boolean access$1400 = WifiProStateMachine.this.mIsPrimaryUser;
                        }
                    } else {
                        HwWifiCHRService mHwWifiCHRService2 = HwWifiCHRServiceImpl.getInstance();
                        if (mHwWifiCHRService2 != null) {
                            mHwWifiCHRService2.incrAccessWebRecord(0, true, this.isPortalAP);
                        }
                        this.isKeepConnected = false;
                        boolean unused10 = WifiProStateMachine.this.mIsWiFiNoInternet = false;
                        boolean unused11 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                        boolean unused12 = WifiProStateMachine.this.mIsNetworkAuthen = true;
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 4, WifiProStateMachine.this.mCurrentSsid);
                        HwPortalExceptionManager.getInstance(WifiProStateMachine.this.mContext).notifyPortalAuthenStatus(true);
                        if (this.isPortalAP) {
                            notifyPortalHasInternetAccess();
                        }
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                    }
                    return;
                }
                WifiProStateMachine.this.logD("AP is noInternet or Protal AP , Continue DisplayDialog");
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                return;
            }
            if (-1 != message.arg1 && 6 != message.arg1) {
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, false, 0, false);
            } else if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                int unused13 = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
            } else {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
            }
        }

        private void notifyPortalHasInternetAccess() {
            if (isProvisioned(WifiProStateMachine.this.mContext)) {
                Log.d(WifiProStateMachine.TAG, "portal has internet access, force network re-evaluation");
                ConnectivityManager connMgr = ConnectivityManager.from(WifiProStateMachine.this.mContext);
                Network[] info = connMgr.getAllNetworks();
                int length = info.length;
                int i = 0;
                while (i < length) {
                    Network nw = info[i];
                    NetworkCapabilities nc = connMgr.getNetworkCapabilities(nw);
                    if (!nc.hasTransport(1) || !nc.hasCapability(12)) {
                        i++;
                    } else {
                        connMgr.reportNetworkConnectivity(nw, false);
                        return;
                    }
                }
            }
        }

        private boolean isProvisioned(Context context) {
            return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1;
        }

        private void handleCheckAvailableApResult(Message msg) {
            if (!this.isIgnorAvailableWifiCheck) {
                if (WifiProStateMachine.this.mIsWiFiNoInternet && ((Boolean) msg.obj).booleanValue()) {
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                    if (1 != WifiProStateMachine.this.mWiFiNoInternetReason) {
                        WifiProStateMachine.this.logD("AllowWifi2Wifi, transitionTo mWiFiLinkMonitorState");
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                    }
                } else if (!this.isToastDisplayed) {
                    WifiProStateMachine.this.logW("There is no network can switch");
                    this.isToastDisplayed = true;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, true);
                    if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                        int unused = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                    } else {
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, HidataWechatTraffic.MIN_VALID_TIME);
                    }
                }
            }
        }

        private void handleUserSelectDialogOk() {
            WifiProStateMachine.this.logD("Intelligent choice other network,go to mWiFiLinkMonitorState");
            boolean unused = WifiProStateMachine.this.mIsWiFiNoInternet = true;
            boolean unused2 = WifiProStateMachine.this.mIsWiFiInternetCHRFlag = true;
            this.isKeepConnected = false;
            boolean unused3 = WifiProStateMachine.this.mIsUserManualConnectSuccess = false;
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
        }

        private boolean isHiLinkUnconfigRouter() {
            int result = 0;
            if (WifiProStateMachine.this.mContext != null && !TextUtils.isEmpty(WifiProStateMachine.this.mCurrentSsid)) {
                result = HiLinkUtil.getHiLinkSsidType(WifiProStateMachine.this.mContext, WifiInfo.removeDoubleQuotes(WifiProStateMachine.this.mCurrentSsid), WifiProStateMachine.this.mCurrentBssid);
            }
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("isHiLinkUnconfigRouter, getHiLinkSsidType = " + result);
            return result == 1;
        }

        private void handleDialogCancel() {
            WifiProStateMachine.this.logD("Keep this network,do nothing!!!");
            this.isIgnorAvailableWifiCheck = true;
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            this.isKeepConnected = true;
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                    int unused = WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                }
            }
            if (WifiProStateMachine.this.isDialogUpWhenConnected && WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
            }
        }

        private void internetFailureSelfcure() {
            if (!HwSelfCureEngine.getInstance().isSelfCureOngoing() && this.internetFailureDetectedCount == 0 && WifiProStateMachine.this.isWifiConnected()) {
                int unused = WifiProStateMachine.this.mCurrentRssi = WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager);
                WifiProStateMachine.this.logD("internetFailureSelfcure mCurrentRssi = " + WifiProStateMachine.this.mCurrentRssi);
                if (WifiProStateMachine.this.mCurrentRssi >= WifiProStateMachine.HANDOVER_5G_DIRECTLY_RSSI) {
                    HwSelfCureEngine.getInstance().notifyInternetFailureDetected(true);
                    this.internetFailureDetectedCount++;
                    boolean unused2 = WifiProStateMachine.this.mVerfyingToConnectedState = false;
                }
            }
        }
    }

    class WifiDisConnectedState extends State {
        WifiDisConnectedState() {
        }

        public void enter() {
            boolean unused = WifiProStateMachine.mIsWifiManualEvaluating = false;
            boolean unused2 = WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.setWifiEvaluateTag(false);
            if (WifiProStateMachine.this.mOpenAvailableAPCounter >= 2) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                int unused3 = WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
            }
            WifiProStateMachine.this.logD("WifiDisConnectedState is Enter");
            long unused4 = WifiProStateMachine.this.mLastDisconnectedTime = System.currentTimeMillis();
            boolean unused5 = WifiProStateMachine.this.mIsPortalAp = false;
            boolean unused6 = WifiProStateMachine.this.mIsNetworkAuthen = false;
            WifiProStateMachine.this.resetVariables();
            boolean unused7 = WifiProStateMachine.this.mVerfyingToConnectedState = false;
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                WifiProStateMachine.this.logD("BQE bad rove out, disconnect time recorded.");
                long unused8 = WifiProStateMachine.this.mChrWifiDisconnectStartTime = System.currentTimeMillis();
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
            if (WifiProStateMachine.this.mPhoneStateListenerRegisted) {
                WifiProStateMachine.this.logD("stop PhoneStateListener");
                WifiProStateMachine.this.mTelephonyManager.listen(WifiProStateMachine.this.phoneStateListener, 0);
                boolean unused9 = WifiProStateMachine.this.mPhoneStateListenerRegisted = false;
            }
            boolean unused10 = WifiProStateMachine.this.mRoveOutStarted = false;
            boolean unused11 = WifiProStateMachine.this.mIsRoveOutToDisconn = false;
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiDisConnectedState is Exit");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE) {
                NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState() && WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine.this.logD("WifiDisConnectedState: wifi connect,to go connected");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                } else if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                }
            } else if (i != WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION) {
                if (i != WifiProStateMachine.EVENT_NETWORK_USER_CONNECT) {
                    if (i != WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED) {
                        return false;
                    }
                    Intent confg_intent = (Intent) msg.obj;
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                    if (conn_cfg != null) {
                        int change_reason = confg_intent.getIntExtra("changeReason", -1);
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.logD("WifiDisConnectedState, change reson " + change_reason + ", isTempCreated = " + conn_cfg.isTempCreated);
                        if (conn_cfg.isTempCreated && change_reason != 1) {
                            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                            wifiProStateMachine2.logD("WifiDisConnectedState, forget " + conn_cfg.SSID);
                            WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                        }
                    }
                } else if (msg.obj != null && ((Boolean) msg.obj).booleanValue()) {
                    boolean unused = WifiProStateMachine.this.mIsUserManualConnectSuccess = true;
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.logD("receive EVENT_NETWORK_USER_CONNECT, set mIsUserManualConnectSuccess = " + WifiProStateMachine.this.mIsUserManualConnectSuccess);
                }
            } else if (!WifiProStateMachine.this.isMobileDataConnected()) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE);
            }
            return true;
        }
    }

    private class WifiProPhoneStateListener extends PhoneStateListener {
        private WifiProPhoneStateListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CALL_STATE_CHANGED, state, -1);
        }
    }

    class WifiSemiAutoEvaluateState extends State {
        WifiSemiAutoEvaluateState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiSemiAutoEvaluateState enter");
            WifiProStateMachine.this.setWifiCSPState(0);
            if (!WifiProStateMachine.mIsWifiSemiAutoEvaluating) {
                WifiProStateMachine.this.setWifiEvaluateTag(true);
                boolean unused = WifiProStateMachine.mIsWifiSemiAutoEvaluating = true;
                boolean unused2 = WifiProStateMachine.this.mIsAllowEvaluate = true;
                if (!WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty()) {
                    int unused3 = WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    return;
                }
                WifiProStateMachine.this.logW("UnEvaluate AP records is empty !");
            }
            boolean unused4 = WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiSemiAutoEvaluateState exit");
            if (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.setWifiEvaluateTag(false);
                WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateCacheRecords();
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                case WifiProStateMachine.EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED /*136203*/:
                    NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    if (networkInfo == null || NetworkInfo.DetailedState.CONNECTED != networkInfo.getDetailedState() || !WifiProStateMachine.this.isWifiConnected()) {
                        if (networkInfo != null && NetworkInfo.DetailedState.DISCONNECTED == networkInfo.getDetailedState() && (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate())) {
                            WifiProStateMachine.this.logW("Evaluate has complete, go to mWifiDisConnectedState");
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            WifiProStateMachine.this.setWifiEvaluateTag(false);
                            break;
                        }
                    } else {
                        WifiProStateMachine.this.setWifiEvaluateTag(false);
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.logD("mIsWifiSemiAutoEvaluateComplete == " + WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete);
                        WifiProStateMachine.this.logD("******WifiSemiAutoEvaluateState go to mWifiConnectedState *****");
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_COMPLETE /*136295*/:
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logD("Evaluate has complete, restore wifi Config, mOpenAvailableAPCounter = " + WifiProStateMachine.this.mOpenAvailableAPCounter);
                    if (WifiProStateMachine.this.mOpenAvailableAPCounter >= 2) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                        int unused = WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
                    }
                    WiFiProEvaluateController unused2 = WifiProStateMachine.this.mWiFiProEvaluateController;
                    WiFiProEvaluateController.evaluateAPHashMapDump();
                    WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                    boolean unused3 = WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
                    boolean unused4 = WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = true;
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                    if (wifi_info != null) {
                        if (wifi_info.getState() == NetworkInfo.State.DISCONNECTED) {
                            WifiProStateMachine.this.logD("wifi has disconnected, go to mWifiDisConnectedState");
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            break;
                        }
                    } else {
                        WifiProStateMachine.this.logD("wifi_info is null, go to mWiFiProEnableState");
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (!WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WifiSemiAutoScoreState extends State {
        private int checkCounter;
        private long checkTime;
        private long connectTime;
        private boolean isCheckRuning;
        private String nextBSSID = null;
        private String nextSSID = null;

        WifiSemiAutoScoreState() {
        }

        public void enter() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logD("WifiSemiAutoScoreState enter,  mIsAllowEvaluate = " + WifiProStateMachine.this.mIsAllowEvaluate);
            if (isStopEvaluteNextAP()) {
                WifiProStateMachine.this.logD("WiFiPro auto Evaluate has  closed");
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                return;
            }
            this.connectTime = 0;
            this.checkTime = 0;
            this.checkCounter = 0;
            this.isCheckRuning = false;
            this.nextSSID = WifiProStateMachine.this.mWiFiProEvaluateController.getNextEvaluateWiFiSSID();
            if (TextUtils.isEmpty(this.nextSSID)) {
                WifiProStateMachine.this.logD("ALL SemiAutoScore has Evaluate complete!");
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                return;
            }
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logD("***********start SemiAuto Evaluate nextSSID :" + this.nextSSID);
            if (WifiProStateMachine.this.mWiFiProEvaluateController.isAbandonEvaluate(this.nextSSID)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUTE_ABANDON);
                return;
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT, 75000);
            WifiProUIDisplayManager access$1200 = WifiProStateMachine.this.mWifiProUIDisplayManager;
            access$1200.showToastL("start  evaluate :" + this.nextSSID);
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, 0);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 0, this.nextSSID);
            WifiProStateMachine.this.refreshConnectedNetWork();
            if (!WifiProStateMachine.this.isWifiConnected() || !this.nextSSID.equals(WifiProStateMachine.this.mCurrentSsid)) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                return;
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, true);
            this.isCheckRuning = true;
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
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(this.nextSSID, 1);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                case WifiProStateMachine.EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED /*136203*/:
                    NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    WifiInfo cInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                    if (cInfo != null && networkInfo != null) {
                        WifiProStateMachine.this.logD(", nextSSID SSID = " + this.nextSSID + ", networkInfo = " + networkInfo);
                        if (NetworkInfo.State.DISCONNECTED != networkInfo.getState()) {
                            if (NetworkInfo.State.CONNECTED != networkInfo.getState()) {
                                if (NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                                    String currssid = cInfo.getSSID();
                                    if (!TextUtils.isEmpty(this.nextSSID) && !TextUtils.isEmpty(currssid) && !this.nextSSID.equals(currssid)) {
                                        WifiProStateMachine.this.logD("Connect other ap ,ssid : " + currssid);
                                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                        break;
                                    }
                                }
                            } else {
                                String extssid = cInfo.getSSID();
                                if (!TextUtils.isEmpty(this.nextSSID) && !TextUtils.isEmpty(extssid) && !this.nextSSID.equals(extssid)) {
                                    WifiProStateMachine.this.logD("Connected other ap ,ssid : " + extssid);
                                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                    break;
                                } else {
                                    if (WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID) != null) {
                                        int tag = Settings.Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.WIFI_EVALUATE_TAG, -1);
                                        WifiProStateMachine.this.logD(this.nextSSID + "is Connected, wifiConfig isTempCreated = " + wifiConfig.isTempCreated + ", Tag = " + tag);
                                    }
                                    WifiProStateMachine.this.logD("receive connect msg ,ssid : " + extssid);
                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(16);
                                    break;
                                }
                            }
                        } else if ((!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(cInfo.getSSID())) || "<unknown ssid>".equals(cInfo.getSSID())) {
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(22);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                            WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                            break;
                        }
                    } else {
                        WifiProStateMachine.this.logD("EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED:cInfo or networkInfo is null.");
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    handleInternetCheckReusltInAutoScoreState(msg);
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    List unused = WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mWifiManager.getScanResults());
                    boolean unused2 = WifiProStateMachine.this.mIsAllowEvaluate = WifiProStateMachine.this.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList);
                    if (!WifiProStateMachine.this.mIsAllowEvaluate) {
                        WifiProStateMachine.this.logD("discover save ap, stop allow evaluate");
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
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 2, level, this.nextSSID);
                    }
                    if (level == 0) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    }
                    boolean enabled = WifiProCommonUtils.isWifiSecDetectOn(WifiProStateMachine.this.mContext);
                    int security = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiSecurityInfo(this.nextSSID);
                    WifiProStateMachine.this.logD("security switch enabled = " + enabled + ", current security value = " + security);
                    if (enabled && WifiProCommonUtils.isWifiConnected(WifiProStateMachine.this.mWifiManager) && (security == -1 || security == 1)) {
                        this.nextBSSID = WifiProCommonUtils.getCurrentBssid(WifiProStateMachine.this.mWifiManager);
                        WifiProStateMachine.this.logD("recv BQE level = " + level + ", start to query wifi security, ssid = " + this.nextSSID);
                        WifiProStateMachine.this.mNetworkQosMonitor.queryWifiSecurity(this.nextSSID, this.nextBSSID);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT, 30000);
                        break;
                    } else {
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, true);
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT /*136301*/:
                    WifiProStateMachine.this.logD(this.nextSSID + " Conenct Time Out,connect fail! conenct Time = 35s");
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(15);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(20);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_LAST_EVALUTE_VALID /*136302*/:
                    WiFiProScoreInfo wiFiProScoreInfo = WifiProStateMachine.this.mWiFiProEvaluateController.getCurrentWiFiProScoreInfo(this.nextSSID);
                    if (wiFiProScoreInfo != null) {
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), wiFiProScoreInfo.internetAccessType, wiFiProScoreInfo.networkQosLevel, wiFiProScoreInfo.networkQosScore);
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_TIMEOUT /*136304*/:
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine.this.logD(this.nextSSID + " evaluate Time = 70s");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_ABANDON /*136305*/:
                    WifiProStateMachine.this.logD(this.nextSSID + "abandon evalute ");
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET /*136307*/:
                    WifiProStateMachine.this.logW("wifi conenct, start check internet,  checkCounter =   " + this.checkCounter);
                    if (this.checkCounter == 0) {
                        this.connectTime = (System.currentTimeMillis() - this.connectTime) / 1000;
                        WifiProStateMachine.this.logD(this.nextSSID + " background conenct Time =" + this.connectTime + " s");
                        WifiProStateMachine.this.mWiFiProCHRMgr.updateSSID(this.nextSSID);
                        WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiproTimeLen((short) ((int) this.connectTime));
                        WifiProCHRManager access$20500 = WifiProStateMachine.this.mWiFiProCHRMgr;
                        WifiProCHRManager unused3 = WifiProStateMachine.this.mWiFiProCHRMgr;
                        access$20500.updateWifiException(HwQoEUtils.QOE_MSG_UPDATE_QUALITY_INFO, "BG_CONN_AP_TIME_LEN");
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
                    }
                    this.checkTime = System.currentTimeMillis();
                    this.checkCounter++;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, true);
                    this.isCheckRuning = true;
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = (Intent) msg.obj;
                    int change_reason = confg_intent.getIntExtra("changeReason", -1);
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                    if (conn_cfg != null) {
                        WifiProStateMachine.this.logD(", nextSSID SSID = " + this.nextSSID + ", conf  " + conn_cfg.SSID);
                        if (change_reason != 0) {
                            if (change_reason == 2) {
                                WifiProStateMachine.this.logD("--- change_reason =change,  change a ssid = " + conn_cfg.SSID + ", status = " + conn_cfg.status + " isTempCreated " + conn_cfg.isTempCreated);
                                if (!conn_cfg.isTempCreated) {
                                    if (!WifiProStateMachine.this.isWifiConnected()) {
                                        WifiProStateMachine.this.logD("--- wifi has disconnect ----");
                                    } else if (!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(conn_cfg.SSID)) {
                                        WifiProStateMachine.this.mWiFiProEvaluateController.clearUntrustedOpenApList();
                                        WifiProStateMachine.this.mWifiProConfigStore.resetTempCreatedConfig(conn_cfg);
                                        if (conn_cfg.status == 1) {
                                            WifiProStateMachine.this.mWifiManager.connect(conn_cfg, null);
                                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(18);
                                        }
                                    }
                                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                    break;
                                }
                            }
                        } else {
                            handleWifiConfgChange(change_reason, conn_cfg);
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED /*136310*/:
                    if (WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.logD("P2PConnectedOrConnecting  , stop allow evaluate");
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_SECURITY_RESPONSE /*136312*/:
                case WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT /*136313*/:
                    if (msg.obj != null) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT);
                        Bundle bundle = (Bundle) msg.obj;
                        String ssid = bundle.getString("com.huawei.wifipro.FLAG_SSID");
                        if (ssid == null || !ssid.equals(this.nextSSID)) {
                            WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, it's invalid ssid = " + ssid + ", ignore the result.");
                            break;
                        } else {
                            String string = bundle.getString("com.huawei.wifipro.FLAG_BSSID");
                            int status = bundle.getInt("com.huawei.wifipro.FLAG_SECURITY_STATUS");
                            WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, ssid = " + ssid + ", status = " + status);
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, status);
                            if (status >= 2) {
                                WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, unsecurity, upload CHR statistic.");
                                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(4);
                            }
                        }
                    } else {
                        WifiProStateMachine.this.logW("EVENT_WIFI_SECURITY_RESPONSE, timeout happend.");
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, -1);
                    }
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, true);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP /*136314*/:
                    if (!WifiProStateMachine.this.isWifiConnected()) {
                        evaluteNextAP();
                        break;
                    } else {
                        WifiProStateMachine.this.logD("wifi still connectd, delay 2s to evalute next ap");
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                        break;
                    }
                case WifiProStateMachine.EVENT_BQE_ANALYZE_NETWORK_QUALITY /*136317*/:
                    if (!WifiProStateMachine.this.mNetworkQosMonitor.isBqeServicesStarted()) {
                        WifiProStateMachine.this.logD("EVENT_BQE_ANALYZE_NETWORK_QUALITY, isBqeServicesStarted = false.");
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                        break;
                    } else {
                        WifiProStateMachine.this.mNetworkQosMonitor.startWiFiBqeDetect(HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT);
                        break;
                    }
                default:
                    return false;
            }
            return true;
        }

        private void handleInternetCheckReusltInAutoScoreState(Message msg) {
            if (!TextUtils.isEmpty(this.nextSSID) && this.isCheckRuning) {
                this.checkTime = (System.currentTimeMillis() - this.checkTime) / 1000;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD(this.nextSSID + " checkTime = " + this.checkTime + " s");
                int result = msg.arg1;
                int type = handleWifiCheckResult(result);
                if (7 == result) {
                    if (this.checkCounter == 1) {
                        WifiProStateMachine.this.logD("internet check timeout ,check again");
                        this.checkTime = System.currentTimeMillis();
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                        return;
                    }
                    type = 1;
                    WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(21);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                }
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logD(this.nextSSID + " type = " + type);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, type);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, type, this.nextSSID);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, true);
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.logD("clean evaluate ap :" + this.nextSSID);
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
            }
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

        private int handleWifiCheckResult(int result) {
            if (7 != result) {
                WifiProStateMachine.this.mWiFiProCHRMgr.updateSSID(this.nextSSID);
                WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiproTimeLen((short) ((int) this.checkTime));
                WifiProCHRManager access$20500 = WifiProStateMachine.this.mWiFiProCHRMgr;
                WifiProCHRManager unused = WifiProStateMachine.this.mWiFiProCHRMgr;
                access$20500.updateWifiException(HwQoEUtils.QOE_MSG_UPDATE_QUALITY_INFO, "BG_AC_TIME_LEN");
            }
            if (5 == result) {
                int unused2 = WifiProStateMachine.this.mOpenAvailableAPCounter = WifiProStateMachine.this.mOpenAvailableAPCounter + 1;
                WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(true);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(3);
                return 4;
            } else if (6 == result) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(6);
                return 3;
            } else if (-1 != result) {
                return 0;
            } else {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(5);
                return 2;
            }
        }

        private void handleWifiConfgChange(int reason, WifiConfiguration conn_cfg) {
            if (conn_cfg != null && reason == 0) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("add a new conn_cfg,isTempCreated : " + conn_cfg.isTempCreated);
                if (!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(conn_cfg.SSID) && conn_cfg.isTempCreated) {
                    WifiProStateMachine.this.mWiFiProEvaluateController.addUntrustedOpenApList(conn_cfg.SSID);
                } else if (!TextUtils.isEmpty(conn_cfg.SSID)) {
                    WifiProStateMachine.this.logD("system connecting ap,stop background evaluate");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                }
            }
        }

        private boolean isStopEvaluteNextAP() {
            return !WifiProStateMachine.this.isAllowWiFiAutoEvaluate() || !TextUtils.isEmpty(WifiProStateMachine.this.mUserManualConnecConfigKey) || !WifiProStateMachine.this.mIsAllowEvaluate || WifiProCommonUtils.isWifiConnectedOrConnecting(WifiProStateMachine.this.mWifiManager);
        }
    }

    static /* synthetic */ String access$8684(WifiProStateMachine x0, Object x1) {
        String str = x0.respCodeChrInfo + x1;
        x0.respCodeChrInfo = str;
        return str;
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def) == 1;
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.Global.getInt(cr, name, def) == 1;
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsSecureBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.Secure.getInt(cr, name, def) == 1;
    }

    /* access modifiers changed from: private */
    public static int getSettingsSystemInt(ContentResolver cr, String name, int def) {
        return Settings.System.getInt(cr, name, def);
    }

    static {
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", "").equals(DOCOMO_HW_OPTA) && SystemProperties.get("ro.config.hw_optb", "").equals(DOCOMO_HW_OPTB)) {
            z = true;
        }
        IS_DOCOMO = z;
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
        Looper looper = null;
        boolean z = true;
        this.mIsWifiproDisableOnReboot = true;
        this.mLastCSPState = -1;
        this.mPortalNotificationId = -1;
        this.connectStartTime = 0;
        this.isPeriodicDet = false;
        this.respCodeChrInfo = "";
        this.detectionNumSlow = 0;
        this.mVehicleStateChangeObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                boolean z = true;
                if (Settings.Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.VEHICLE_STATE_FLAG, 0) != 1) {
                    z = false;
                }
                boolean unused = wifiProStateMachine.isVehicleState = z;
                WifiProStateMachine.this.logD("VehicleState state change, VehicleState: " + WifiProStateMachine.this.isVehicleState);
            }
        };
        this.mMapNavigatingStateChangeObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                boolean z = true;
                if (Settings.Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.MAPS_LOCATION_FLAG, 0) != 1) {
                    z = false;
                }
                boolean unused = wifiProStateMachine.isMapNavigating = z;
                WifiProStateMachine.this.logD("MapNavigating state change, MapNavigating: " + WifiProStateMachine.this.isMapNavigating);
            }
        };
        this.mContext = context;
        this.mWsmChannel = new AsyncChannel();
        this.mWsmChannel.connectSync(this.mContext, getHandler(), dstMessenger);
        this.mContentResolver = context.getContentResolver();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        WifiProStatisticsManager.initStatisticsManager(this.mContext, getHandler() != null ? getHandler().getLooper() : null);
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
        this.mIsWiFiProEnabled = WifiProCommonUtils.isWifiProSwitchOn(context);
        this.mIsPrimaryUser = ActivityManager.getCurrentUser() != 0 ? false : z;
        logD("UserID =  " + ActivityManager.getCurrentUser() + ", mIsPrimaryUser = " + this.mIsPrimaryUser);
        this.mDualBandManager = HwDualBandManager.createInstance(context, this);
        this.mHwDualBandBlackListMgr = HwDualBandBlackListManager.getHwDualBandBlackListMgrInstance();
        this.phoneStateListener = new WifiProPhoneStateListener();
        this.mWifiInjector = WifiInjector.getInstance();
        this.mWifiStateMachine = this.mWifiInjector.getWifiStateMachine();
        this.mSavedNetworkEvaluator = this.mWifiInjector.getSavedNetworkEvaluator();
        if (this.mNetworkQosMonitor != null) {
            this.mNetworkPropertyChecker = this.mNetworkQosMonitor.getNetworkPropertyChecker();
        }
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
        this.mIsPortalAp = false;
        this.mIsNetworkAuthen = false;
        registerMapNavigatingStateChanges();
        registerVehicleStateChanges();
        registerForSettingsChanges();
        registerForMobileDataChanges();
        registerForMobilePDPSwitchChanges();
        registerNetworkReceiver();
        registerOOBECompleted();
        registerForVpnSettingsChanges();
        registerForAppPidChanges();
        registerForAPEvaluateChanges();
        registerForManualConnectChanges();
        HwAutoConnectManager.getInstance(context, this.mNetworkQosMonitor).init(getHandler() != null ? getHandler().getLooper() : looper);
        HwPortalExceptionManager.getInstance(context).init();
        WifiScanGenieController.createWifiScanGenieControllerImpl(context);
        setInitialState(this.mWiFiProEnableState);
        logD("System Create WifiProStateMachine begin to initialize portal database.");
        PortalDataBaseManager instance = PortalDataBaseManager.getInstance(this.mContext);
        logD("System Create WifiProStateMachine Complete!");
    }

    /* access modifiers changed from: private */
    public void defaulVariableInit() {
        if (!this.isVariableInited) {
            this.mIsMobileDataEnabled = getSettingsGlobalBoolean(this.mContentResolver, "mobile_data", false);
            this.mEmuiPdpSwichValue = getSettingsSystemInt(this.mContentResolver, KEY_EMUI_WIFI_TO_PDP, 1);
            this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, false);
            this.mIsVpnWorking = getSettingsSystemBoolean(this.mContentResolver, SETTING_SECURE_VPN_WORK_VALUE, false);
            if (this.mIsVpnWorking) {
                Settings.System.putInt(this.mContext.getContentResolver(), SETTING_SECURE_VPN_WORK_VALUE, 0);
                this.mIsVpnWorking = false;
            }
            Settings.System.putString(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY, "");
            setWifiEvaluateTag(false);
            this.isVariableInited = true;
            logD("Variable Init Complete!");
        }
    }

    private void registerNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                boolean z = false;
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (WifiProStateMachine.isWifiEvaluating() && WifiProStateMachine.this.mIsWiFiProEnabled) {
                        String unused = WifiProStateMachine.this.mManualConnectAp = Settings.System.getString(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY);
                        if (!TextUtils.isEmpty(WifiProStateMachine.this.mManualConnectAp)) {
                            WifiProStateMachine.this.logD("ManualConnectedWiFi  AP, ,isWifiEvaluating ");
                            WifiProStateMachine.this.setWifiEvaluateTag(false);
                            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                        }
                    }
                    if (info != null && NetworkInfo.DetailedState.OBTAINING_IPADDR == info.getDetailedState()) {
                        WifiProStateMachine.this.logD("wifi is conencted, WiFiProEnabled = " + WifiProStateMachine.this.mIsWiFiProEnabled + ", VpnWorking " + WifiProStateMachine.this.mIsVpnWorking);
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE, intent);
                } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE, intent);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() == 1) {
                        String unused2 = WifiProStateMachine.this.mUserManualConnecConfigKey = "";
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_SCREEN_ON);
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendScreenOnEvent();
                    }
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_USER_PRESENT);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_SCREEN_OFF);
                } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CONFIGURATION_CHANGED, intent);
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                    if (WifiProStateMachine.this.mWifiProUIDisplayManager.mIsNotificationShown && WifiProStateMachine.this.mWiFiProEvaluateController.isAccessAPOutOfRange(WifiProStateMachine.this.mWifiManager.getScanResults())) {
                        WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE);
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE, intent);
                } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(intent);
                    if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiSemiAutoScoreState) {
                        WifiConfiguration conn_cfg = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                        if (conn_cfg != null) {
                            int change_reason = intent.getIntExtra("changeReason", -1);
                            WifiProStateMachine.this.logD("ssid = " + conn_cfg.SSID + ", change reson " + change_reason + ", isTempCreated = " + conn_cfg.isTempCreated);
                            if (conn_cfg.isTempCreated && change_reason != 1) {
                                WifiProStateMachine.this.logD("WiFiProDisabledState, forget " + conn_cfg.SSID);
                                WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            }
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED, intent);
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE, intent);
                } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    int userID = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    if (userID == 0) {
                        z = true;
                    }
                    boolean unused3 = wifiProStateMachine.mIsPrimaryUser = z;
                    WifiProStateMachine.this.logD("user has switched,new userID = " + userID);
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null) {
                        boolean unused4 = WifiProStateMachine.this.mIsP2PConnectedOrConnecting = p2pNetworkInfo.isConnectedOrConnecting();
                    }
                    if (!WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED);
                    }
                } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                    int p2pState = intent.getIntExtra("extraState", -1);
                    if (p2pState == 1 || p2pState == 2) {
                        boolean unused5 = WifiProStateMachine.this.mIsP2PConnectedOrConnecting = true;
                    } else {
                        boolean unused6 = WifiProStateMachine.this.mIsP2PConnectedOrConnecting = false;
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED);
                } else if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_LOAD_CONFIG_INTERNET_INFO, 5000);
                } else if ("com.huawei.wifi.action.FIRST_CHECK_NO_INTERNET_NOTIFICATION".equals(action)) {
                    WifiProStateMachine.this.logD("broadcast WifiProCommonDefs.ACTION_FIRST_CHECK_NO_INTERNET_NOTIFICATION received");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_NO_INTERNET_NOTIFICATION);
                } else if ("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER".equals(intent.getAction())) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_PORTAL_SELECTED);
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mIntentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN);
        this.mIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE);
        this.mIntentFilter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mIntentFilter.addAction("com.huawei.wifi.action.FIRST_CHECK_NO_INTERNET_NOTIFICATION");
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mIntentFilter.addAction("android.intent.action.USER_PRESENT");
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
                        WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(0);
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
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, "device_provisioned", 0) == 1) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateInitialWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
                }
            }
        });
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean unused = WifiProStateMachine.this.mIsWiFiProEnabled = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, "smart_network_switching", false);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("Wifi pro setting has changed,WiFiProEnabled == " + WifiProStateMachine.this.mIsWiFiProEnabled);
                if (WifiProStateMachine.isWifiEvaluating() && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                    boolean unused2 = WifiProStateMachine.this.restoreWiFiConfig();
                    WifiProStateMachine.this.setWifiEvaluateTag(false);
                }
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
            }
        });
    }

    private void registerForMobileDataChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean unused = WifiProStateMachine.this.mIsMobileDataEnabled = WifiProStateMachine.getSettingsGlobalBoolean(WifiProStateMachine.this.mContentResolver, "mobile_data", false);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("MobileData has changed,isMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
            }
        });
    }

    private void registerForMobilePDPSwitchChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_EMUI_WIFI_TO_PDP), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                int unused = WifiProStateMachine.this.mEmuiPdpSwichValue = WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_EMUI_WIFI_TO_PDP, 1);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE);
                int unused2 = WifiProStateMachine.this.mWiFiProPdpSwichValue = WifiProStateMachine.this.mEmuiPdpSwichValue;
                if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseSelCspSettingChgCount(WifiProStateMachine.this.mWiFiProPdpSwichValue);
                }
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("Mobile PDP setting changed, mWiFiProPdpSwichValue = mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
            }
        });
    }

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTING_SECURE_VPN_WORK_VALUE), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean unused = WifiProStateMachine.this.mIsVpnWorking = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.SETTING_SECURE_VPN_WORK_VALUE, false);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("vpn state has changed,mIsVpnWorking == " + WifiProStateMachine.this.mIsVpnWorking);
                WifiProStateMachine.this.notifyVPNStateChanged(WifiProStateMachine.this.mIsVpnWorking);
                if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiDisConnectedState) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                }
            }
        });
    }

    private void registerForAppPidChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTING_SECURE_CONN_WIFI_PID), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                int unused = WifiProStateMachine.this.mConnectWiFiAppPid = WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.SETTING_SECURE_CONN_WIFI_PID, -1);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("current APP name == " + WifiProStateMachine.this.getAppName(WifiProStateMachine.this.mConnectWiFiAppPid));
            }
        });
    }

    private void registerForAPEvaluateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_WIFIPRO_RECOMMEND_NETWORK), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean unused = WifiProStateMachine.this.mIsWiFiProAutoEvaluateAP = WifiProStateMachine.getSettingsSecureBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_RECOMMEND_NETWORK, false);
            }
        });
    }

    private void registerForManualConnectChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                String unused = WifiProStateMachine.this.mManualConnectAp = Settings.System.getString(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logD("mManualConnectAp has change:  " + WifiProStateMachine.this.mManualConnectAp + ", wifipro state = " + WifiProStateMachine.this.getCurrentState().getName());
                if (!TextUtils.isEmpty(WifiProStateMachine.this.mManualConnectAp)) {
                    String unused2 = WifiProStateMachine.this.mUserManualConnecConfigKey = WifiProStateMachine.this.mManualConnectAp;
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void resetVariables() {
        this.mNetworkQosMonitor.stopBqeService();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mIsWiFiInternetCHRFlag = false;
        this.mWiFiProPdpSwichValue = 0;
        this.mNetworkQosMonitor.stopALLMonitor();
        this.mNetworkQosMonitor.resetMonitorStatus();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = 0;
        this.mIsUserHandoverWiFi = false;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = false;
        this.mIsUserManualConnectSuccess = false;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    /* access modifiers changed from: private */
    public void updateWifiInternetStateChange(int lenvel) {
        if (WifiProCommonUtils.isWifiConnectedOrConnecting(this.mWifiManager)) {
            if (this.mLastWifiLevel == lenvel) {
                logD("wifi lenvel is not change, don't report, lenvel = " + lenvel);
                return;
            }
            this.mLastWifiLevel = lenvel;
            if (-1 == lenvel) {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                Settings.Secure.putString(contentResolver, SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                logD("mIsPortalAp = " + this.mIsPortalAp + ", mIsNetworkAuthen = " + this.mIsNetworkAuthen);
                if (!this.mIsPortalAp || this.mIsNetworkAuthen) {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, true, 0, false);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 2, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 2);
                } else {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, true, 1, false);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 3, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
                }
            } else if (6 == lenvel) {
                ContentResolver contentResolver2 = this.mContext.getContentResolver();
                Settings.Secure.putString(contentResolver2, SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, true, 1, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 3, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
            } else {
                Settings.Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
                this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, false, 0, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 4, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 4);
            }
        }
    }

    /* access modifiers changed from: private */
    public void reSetWifiInternetState() {
        logD("reSetWifiInternetState");
        Settings.Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
    }

    /* access modifiers changed from: private */
    public void setWifiCSPState(int state) {
        if (this.mLastCSPState == state) {
            logD("setWifiCSPState state is not change,ignor! mLastCSPState:" + this.mLastCSPState);
            return;
        }
        logD("setWifiCSPState new state = " + state);
        this.mLastCSPState = state;
        Settings.System.putInt(this.mContext.getContentResolver(), WIFI_CSP_DISPALY_STATE, state);
    }

    /* access modifiers changed from: private */
    public void registerCallBack() {
        this.mNetworkQosMonitor.registerCallBack(this);
        this.mWifiHandover.registerCallBack(this, this.mNetworkQosMonitor);
        this.mWifiProUIDisplayManager.registerCallBack(this);
    }

    /* access modifiers changed from: private */
    public void unRegisterCallBack() {
        this.mNetworkQosMonitor.unRegisterCallBack();
        this.mWifiHandover.unRegisterCallBack();
        this.mWifiProUIDisplayManager.unRegisterCallBack();
    }

    /* access modifiers changed from: private */
    public boolean isWiFiPoorer(int wifi_level, int mobile_level) {
        logD("WiFi Qos =[ " + wifi_level + " ] ,  Mobile Qos =[ " + mobile_level + "]");
        boolean z = false;
        if (mobile_level == 0) {
            return false;
        }
        if (this.mIsWiFiNoInternet) {
            if (-1 < mobile_level) {
                z = true;
            }
            return z;
        }
        if (wifi_level < mobile_level) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isMobileDataConnected() {
        if (5 != this.mTelephonyManager.getSimState() || !this.mIsMobileDataEnabled || isAirModeOn()) {
            return false;
        }
        return true;
    }

    private boolean isAirModeOn() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public synchronized boolean isWifiConnected() {
        if (this.mWifiManager.isWifiEnabled()) {
            WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
            if (!(conInfo == null || conInfo.getNetworkId() == -1 || conInfo.getBSSID() == null || "00:00:00:00:00:00".equals(conInfo.getBSSID()) || conInfo.getSupplicantState() != SupplicantState.COMPLETED)) {
                return true;
            }
        }
        return false;
    }

    public static void putConnectWifiAppPid(Context context, int pid) {
    }

    /* access modifiers changed from: private */
    public void notifyManualConnectAP(boolean isUserManualConnect, boolean isUserHandoverWiFi) {
        if (this.mHwQoEService == null) {
            this.mHwQoEService = HwQoEService.getInstance();
        }
        if (this.mHwQoEService != null) {
            this.mHwQoEService.updateWifiConnectionMode(isUserManualConnect, isUserHandoverWiFi);
        }
    }

    /* access modifiers changed from: private */
    public void notifyVPNStateChanged(boolean isVpnConnected) {
        if (this.mHwQoEService == null) {
            this.mHwQoEService = HwQoEService.getInstance();
        }
        if (this.mHwQoEService != null) {
            this.mHwQoEService.updateVNPStateChanged(isVpnConnected);
        }
    }

    /* access modifiers changed from: private */
    public boolean isKeepCurrWiFiConnected() {
        if (this.mIsVpnWorking) {
            logW("vpn is working,shuld keep current connect");
        }
        if (this.mIsUserManualConnectSuccess && !this.mIsWiFiProEnabled) {
            logW("user manual connect and wifi+ disabled, keep connect and no dialog.");
        }
        return this.mIsVpnWorking || this.mIsUserHandoverWiFi || this.mHiLinkUnconfig || isAppinWhitelists() || isWifiRepeaterOn() || HwMplinkManager.isKeepCurrMplinkConnected(this.mCurrWifiInfo);
    }

    private boolean isWifiRepeaterOn() {
        int state = Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0);
        return 1 == state || 6 == state;
    }

    private boolean isHwSyncClinetConnected() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), HWSYNC_DEVICE_CONNECTED_KEY, 0) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isAllowWiFiAutoEvaluate() {
        boolean z = this.mIsWiFiProAutoEvaluateAP;
        return this.mIsWiFiProEnabled && !this.mIsVpnWorking;
    }

    /* access modifiers changed from: private */
    public void refreshConnectedNetWork() {
        if (WifiProCommonUtils.isWifiConnectedOrConnecting(this.mWifiManager)) {
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

    /* access modifiers changed from: private */
    public boolean isAllowWifi2Mobile() {
        if (!this.mIsWiFiProEnabled || !this.mIsPrimaryUser || !isMobileDataConnected() || !this.mPowerManager.isScreenOn() || this.mEmuiPdpSwichValue == 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isPdpAvailable() {
        if ("true".equals(Settings.Global.getString(this.mContext.getContentResolver(), SYS_PROPERT_PDP))) {
            logD("SYS_PROPERT_PDP hw_RemindWifiToPdp is true");
            return true;
        }
        logD("SYS_PROPERT_PDP hw_RemindWifiToPdp is false");
        return false;
    }

    /* access modifiers changed from: private */
    public String getAppName(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private boolean isAppinWhitelists() {
        if (this.mCurrentWifiConfig != null) {
            String currAppName = this.mCurrentWifiConfig.lastUpdateName;
            logD("isAppinWhitelists, currAppName 11 =  " + currAppName);
            if (TextUtils.isEmpty(currAppName)) {
                currAppName = this.mCurrentWifiConfig.creatorName;
                logD("isAppinWhitelists, currAppName 22 =  " + currAppName);
            }
            if (!TextUtils.isEmpty(currAppName) && this.mAppWhitelists != null) {
                for (String str : this.mAppWhitelists) {
                    if (currAppName.equals(str)) {
                        logD("curr name in the  Whitelists ");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void resetWifiProManualConnect() {
        if (!TextUtils.isEmpty(this.mManualConnectAp)) {
            Settings.System.putString(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY, "");
        }
    }

    /* access modifiers changed from: private */
    public void notifyPortalStatusChanged(boolean popUp, String configKey, boolean hasInternetAccess) {
        if (this.mSavedNetworkEvaluator != null) {
            this.mSavedNetworkEvaluator.portalNotifyChanged(popUp, configKey, hasInternetAccess);
        }
    }

    /* access modifiers changed from: private */
    public void updateWifiConfig(WifiConfiguration config) {
        if (config != null) {
            Message msg = Message.obtain();
            msg.what = 131672;
            msg.obj = config;
            this.mWifiStateMachine.sendMessage(msg);
        }
    }

    public static boolean isWifiEvaluating() {
        return mIsWifiManualEvaluating || mIsWifiSemiAutoEvaluating;
    }

    /* access modifiers changed from: private */
    public boolean isSettingsActivity() {
        return WifiProCommonUtils.isQueryActivityMatched(this.mContext, "com.android.settings.Settings$WifiSettingsActivity");
    }

    public void setWifiApEvaluateEnabled(boolean enable) {
        logD("setWifiApEvaluateEnabled enabled " + enable);
        logD("system can not eavluate ap, ignor setting cmd");
    }

    /* access modifiers changed from: private */
    public void setWifiEvaluateTag(boolean evaluate) {
        logD("setWifiEvaluateTag Tag :" + evaluate);
        Settings.Secure.putInt(this.mContentResolver, WIFI_EVALUATE_TAG, evaluate);
    }

    /* access modifiers changed from: private */
    public void updatePortalNetworkInfo() {
        logD("updatePortalNetworkInfo, mCurrentSsid = " + this.mCurrentSsid + ", mIsPortalAp = " + this.mIsPortalAp);
        if (this.mIsPortalAp) {
            this.mWiFiProEvaluateController.restorePortalEvaluateRecord(this.mCurrentSsid);
        }
    }

    /* access modifiers changed from: private */
    public boolean restoreWiFiConfig() {
        this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, false);
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        NetworkInfo wifi_info = this.mConnectivityManager.getNetworkInfo(1);
        if (wifi_info == null || wifi_info.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            return false;
        }
        this.mWifiManager.disconnect();
        return true;
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public synchronized void onNetworkQosChange(int type, int level, boolean updateUiOnly) {
        if (1 == type) {
            try {
                this.mCurrentWifiLevel = level;
                logD("onNetworkQosChange, currentWifiLevel == " + level + ", wifiNoInternet = " + this.mIsWiFiNoInternet + ", updateUiOnly = " + updateUiOnly);
                sendMessage(EVENT_WIFI_QOS_CHANGE, level, 0, Boolean.valueOf(updateUiOnly));
            } catch (Throwable th) {
                throw th;
            }
        } else if (type == 0) {
            sendMessage(EVENT_MOBILE_QOS_CHANGE, level);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0030, code lost:
        if (r3.mIsWiFiNoInternet == false) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0034, code lost:
        if (r3.mIsUserHandoverWiFi != false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
        logD("wifi no internet and recovered, notify SCE");
        com.android.server.wifi.HwSelfCureEngine.getInstance().notifyInternetAccessRecovery();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0044, code lost:
        if (-101 != r5) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0046, code lost:
        sendMessage(EVENT_WIFI_CHECK_UNKOWN, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        if (isWifiEvaluating() != false) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0058, code lost:
        if (7 != r5) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005a, code lost:
        r5 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        if (IS_DOCOMO == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0060, code lost:
        if (r5 != -1) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0064, code lost:
        if (r5 == r3.mLastWifiLevel) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0068, code lost:
        if (r3.mIsUserManualConnectSuccess == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006c, code lost:
        if (r3.mIsWiFiProEnabled != false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006e, code lost:
        r3.mWsmChannel.sendMessage(INVALID_LINK_DETECTED);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0076, code lost:
        updateWifiInternetStateChange(r5);
        sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007d, code lost:
        sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x008f, code lost:
        return;
     */
    public synchronized void onNetworkDetectionResult(int type, int level) {
        if (1 == type) {
            try {
                logD("wifi Detection level == " + level);
                if (HwSelfCureEngine.getInstance().isSelfCureOngoing() && 5 != level) {
                    logD("SelfCureOngoing, ignore wifi check result");
                } else if (5 == level) {
                }
            } catch (Throwable th) {
                throw th;
            }
        } else if (type == 0) {
            sendMessage(EVENT_CHECK_MOBILE_QOS_RESULT, level);
        }
    }

    public void onPortalAuthCheckResult(int respCode) {
        int level = -1;
        if (respCode == 204) {
            level = 5;
        } else if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(respCode)) {
            level = 6;
        } else if (respCode == 600) {
            level = 7;
        }
        sendMessage(EVENT_CHECK_PORTAL_AUTH_CHECK_RESULT, level);
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public synchronized void onWifiHandoverChange(int type, boolean result, String bssid, int errorReason) {
        if (1 == type) {
            if (result) {
                try {
                    this.mWifiProStatisticsManager.increaseWiFiHandoverWiFiCount(this.mWifiToWifiType);
                } catch (Throwable th) {
                    throw th;
                }
            }
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_WIFI_HANDOVER_WIFI_RESULT, Boolean.valueOf(result));
        } else if (4 == type) {
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_DUALBAND_WIFI_HANDOVER_RESULT, errorReason, -1, Boolean.valueOf(result));
        }
    }

    public void onDualBandNetWorkType(int type, List<HwDualBandMonitorInfo> apList) {
        sendMessage(EVENT_DUALBAND_NETWROK_TYPE, type, -1, apList);
    }

    public synchronized void onDualBandNetWorkFind(List<HwDualBandMonitorInfo> apList) {
        if (apList != null) {
            if (apList.size() != 0) {
                if (this.mDualBandMonitorStart) {
                    logD("onDualBandNetWorkFind  apList.size() = " + apList.size());
                    this.mDualBandMonitorStart = false;
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
                    this.mDualBandEstimateInfoSize++;
                    WifiProEstimateApInfo currentApInfo = new WifiProEstimateApInfo();
                    currentApInfo.setApBssid(this.mCurrentBssid);
                    currentApInfo.setEstimateApSsid(this.mCurrentSsid);
                    currentApInfo.setApRssi(this.mCurrentRssi);
                    currentApInfo.set5GAP(false);
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
        } else {
            sendMessage(EVENT_DUALBAND_RSSITH_RESULT, apInfo);
        }
    }

    public synchronized void onWifiBqeReturnHistoryScore(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnHistoryScore apInfo null error");
        } else {
            sendMessage(EVENT_DUALBAND_SCORE_RESULT, apInfo);
        }
    }

    public synchronized void onWifiBqeReturnCurrentRssi(int rssi) {
        this.mDualBandManager.updateCurrentRssi(rssi);
    }

    /* access modifiers changed from: private */
    public void retryDualBandAPMonitor() {
        this.mDualBandMonitorInfoSize = this.mDualBandMonitorApList.size();
        if (this.mDualBandMonitorInfoSize == 0) {
            loge("retry dual band monitor error, monitorinfo size is zero");
            return;
        }
        Iterator<HwDualBandMonitorInfo> it = this.mDualBandMonitorApList.iterator();
        while (it.hasNext()) {
            HwDualBandMonitorInfo monitorInfo = it.next();
            WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
            apInfo.setApBssid(monitorInfo.mBssid);
            apInfo.setApRssi(monitorInfo.mCurrentRssi);
            this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
        }
    }

    /* access modifiers changed from: private */
    public void handleGetWifiTcpRx() {
        if (this.mNetworkQosMonitor.requestTcpRxPacketsCounter() - this.mWifiTcpRxCount <= 3 || !this.mPowerManager.isScreenOn()) {
            if (getHandler().hasMessages(EVENT_GET_WIFI_TCPRX)) {
                removeMessages(EVENT_GET_WIFI_TCPRX);
            }
            sendMessageDelayed(EVENT_GET_WIFI_TCPRX, 5000);
            return;
        }
        logD("(current_rx - last_rx) > 0, to do HTTP query to check the internet status.");
        this.mNetworkQosMonitor.queryNetworkQos(1, this.mIsPortalAp, this.mIsNetworkAuthen, false);
    }

    /* access modifiers changed from: private */
    public void updateDualBandMonitorInfo(WifiProEstimateApInfo apInfo) {
        Iterator<HwDualBandMonitorInfo> it = this.mDualBandMonitorApList.iterator();
        while (it.hasNext()) {
            HwDualBandMonitorInfo monitorInfo = it.next();
            String bssid = monitorInfo.mBssid;
            if (bssid != null && bssid.equals(apInfo.getApBssid())) {
                monitorInfo.mTargetRssi = apInfo.getRetRssiTH();
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateDualBandEstimateInfo(WifiProEstimateApInfo apInfo) {
        Iterator<WifiProEstimateApInfo> it = this.mDualBandEstimateApList.iterator();
        while (it.hasNext()) {
            WifiProEstimateApInfo estimateApInfo = it.next();
            String bssid = estimateApInfo.getApBssid();
            if (bssid != null && bssid.equals(apInfo.getApBssid())) {
                estimateApInfo.setRetHistoryScore(apInfo.getRetHistoryScore());
                return;
            }
        }
    }

    private void showNoInternetDialog(int reason) {
        if (1 == reason) {
            this.mWifiProUIDisplayManager.showWifiProDialog(5);
        } else if (reason == 0) {
            this.mWifiProUIDisplayManager.showWifiProDialog(4);
        } else if (3 == reason) {
            this.mWifiProUIDisplayManager.showWifiProDialog(6);
        }
    }

    /* access modifiers changed from: private */
    public void chooseAvalibleDualBandAp() {
        HwDualBandMonitorInfo deleteInfo;
        long expiretime;
        long expiretime2;
        logD("chooseAvalibleDualBandAp DualBandEstimateApList =" + this.mDualBandEstimateApList.toString());
        if (this.mDualBandEstimateApList.size() == 0 || this.mCurrentBssid == null) {
            Log.e(TAG, "chooseAvalibleDualBandAp ap size error");
            return;
        }
        this.mAvailable5GAPBssid = null;
        this.mAvailable5GAPSsid = null;
        this.mAvailable5GAPAuthType = 0;
        this.mDuanBandHandoverType = 0;
        WifiProEstimateApInfo bestAp = new WifiProEstimateApInfo();
        int currentApScore = 0;
        Iterator<WifiProEstimateApInfo> it = this.mDualBandEstimateApList.iterator();
        while (it.hasNext()) {
            WifiProEstimateApInfo apInfo = it.next();
            if (this.mCurrentBssid.equals(apInfo.getApBssid())) {
                currentApScore = apInfo.getRetHistoryScore();
            } else if (apInfo.getRetHistoryScore() > bestAp.getRetHistoryScore()) {
                bestAp = apInfo;
            }
        }
        logD("chooseAvalibleDualBandAp bestAp =" + bestAp.toString() + ", currentApScore =" + currentApScore);
        int score = bestAp.getRetHistoryScore();
        if (score >= 40 && bestAp.getApRssi() >= HANDOVER_5G_DIRECTLY_RSSI) {
            this.mAvailable5GAPBssid = bestAp.getApBssid();
            this.mAvailable5GAPSsid = bestAp.getApSsid();
            this.mAvailable5GAPAuthType = bestAp.getApAuthType();
        } else if ((score >= currentApScore + 5 && bestAp.getApRssi() >= HANDOVER_5G_DIRECTLY_RSSI) || (bestAp.getDualbandAPType() == 1 && bestAp.getApRssi() >= -55)) {
            this.mAvailable5GAPBssid = bestAp.getApBssid();
            this.mAvailable5GAPSsid = bestAp.getApSsid();
            this.mAvailable5GAPAuthType = bestAp.getApAuthType();
        }
        if (this.mAvailable5GAPSsid == null) {
            ArrayList<HwDualBandMonitorInfo> mDualBandDeleteList = new ArrayList<>();
            Iterator<HwDualBandMonitorInfo> it2 = this.mDualBandMonitorApList.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                HwDualBandMonitorInfo monitorInfo = it2.next();
                String bssid = monitorInfo.mBssid;
                if (bssid != null && bssid.equals(bestAp.getApBssid()) && monitorInfo.mTargetRssi < -45) {
                    monitorInfo.mTargetRssi += 10;
                    break;
                } else if (monitorInfo.mCurrentRssi >= -45) {
                    mDualBandDeleteList.add(monitorInfo);
                }
            }
            if (mDualBandDeleteList.size() > 0) {
                Iterator<HwDualBandMonitorInfo> it3 = mDualBandDeleteList.iterator();
                while (it3.hasNext()) {
                    logD("remove mix AP for RSSI > -45 DB RSSi = " + it3.next().mSsid);
                    this.mDualBandMonitorApList.remove(deleteInfo);
                }
            }
            if (this.mDualBandMonitorApList.size() != 0) {
                this.mDualBandMonitorStart = true;
                this.mDualBandManager.startMonitor(this.mDualBandMonitorApList);
            }
        } else if (!this.mHwDualBandBlackListMgr.isInWifiBlacklist(this.mAvailable5GAPSsid) && !this.mNetworkBlackListManager.isInWifiBlacklist(this.mAvailable5GAPSsid) && !this.mHwDualBandBlackListMgr.isInPermanentWifiBlacklist(this.mAvailable5GAPSsid)) {
            logD("do dualband handover : " + bestAp.toString());
            sendMessage(EVENT_DUALBAND_5GAP_AVAILABLE);
        } else if (this.mHwDualBandBlackListMgr.isInPermanentWifiBlacklist(this.mAvailable5GAPSsid)) {
            logD("getPermanentExpireTimeForRetry for ssid " + this.mAvailable5GAPSsid + ", time =" + expiretime2);
            sendMessageDelayed(EVENT_DUALBAND_DELAY_RETRY, expiretime2);
        } else {
            logD("getExpireTimeForRetry for ssid " + this.mAvailable5GAPSsid + ", time =" + expiretime);
            sendMessageDelayed(EVENT_DUALBAND_DELAY_RETRY, expiretime);
        }
    }

    /* access modifiers changed from: private */
    public void addDualBandBlackList(String ssid) {
        logD("addDualBandBlackList ssid = " + ssid + ", mDualBandConnectAPSsid = " + this.mDualBandConnectAPSsid);
        if (ssid == null || this.mDualBandConnectAPSsid == null || !this.mDualBandConnectAPSsid.equals(ssid)) {
            logD("addDualBandBlackList do nothing");
            return;
        }
        this.mDualBandConnectAPSsid = null;
        if (System.currentTimeMillis() - this.mDualBandConnectTime > 1800000) {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(ssid, true);
        } else {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(ssid, false);
        }
    }

    /* access modifiers changed from: private */
    public void startDualBandManager() {
        this.mDualBandManager.startDualBandManger();
    }

    /* access modifiers changed from: private */
    public void stopDualBandManager() {
        stopDualBandMonitor();
        this.mDualBandManager.stopDualBandManger();
    }

    /* access modifiers changed from: private */
    public void stopDualBandMonitor() {
        if (this.mDualBandMonitorStart) {
            this.mDualBandMonitorStart = false;
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

    /* access modifiers changed from: private */
    public void notifyNetworkCheckResult(int result) {
        int internet_level = result;
        if (internet_level == 5 && this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 102)) {
            internet_level = 6;
        }
        sendNetworkCheckingStatus("huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", internet_level);
    }

    public void onWifiConnected(boolean result, int reason) {
    }

    public void onCheckAvailableWifi(boolean exist, int bestRssi, String targetSsid, int preferType, int freq) {
        if (!isKeepCurrWiFiConnected()) {
            int rssilevel = WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo());
            int targetRssiLevel = WifiProCommonUtils.getSignalLevel(freq, bestRssi);
            if (exist && this.mNetworkBlackListManager.containedInWifiBlacklists(targetSsid) && (targetRssiLevel <= 3 || targetRssiLevel - rssilevel < 2)) {
                logW("onCheckAvailableWifi, but wifi blacklists contain it, ignore the result.");
                exist = false;
            }
            logD("onCheckAvailableWifi, send EVENT_CHECK_AVAILABLE_AP_RESULT: targetSsid=" + targetSsid + ", exist=" + exist + ", prefer=" + preferType);
            sendMessage(EVENT_CHECK_AVAILABLE_AP_RESULT, targetRssiLevel, preferType, Boolean.valueOf(exist));
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

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    public synchronized void onUserConfirm(int type, int status) {
        if (2 == status) {
            try {
                logD("UserConfirm  is OK ");
                sendMessage(EVENT_DIALOG_OK, type, -1);
            } catch (Throwable th) {
                throw th;
            }
        } else if (1 == status) {
            logD("UserConfirm  is CANCEL");
            sendMessage(EVENT_DIALOG_CANCEL, type, -1);
        }
    }

    public synchronized void userHandoverWifi() {
        logD("User Chose Rove In WiFi");
        sendMessage(EVENT_USER_ROVE_IN);
    }

    public void notifyHttpReachable(boolean isReachable) {
        if (isReachable || this.mPowerManager.isScreenOn()) {
            logD("SEC notifyHttpReachable " + isReachable);
            this.mNetworkQosMonitor.syncNotifyPowerSaveGenie(isReachable, 100, false);
        } else {
            logD("do not notify the PowerSaveGenie when the internet is unreachable becasue the screen is off ");
        }
        sendMessage(EVENT_HTTP_REACHABLE_RESULT, Boolean.valueOf(isReachable));
    }

    public void notifyWifiLinkPoor(boolean poorLink) {
        logD("HwWifiConnectivityMonitor notifyWifiLinkPoor = " + poorLink);
        if (isKeepCurrWiFiConnected()) {
            return;
        }
        if (poorLink) {
            sendMessage(EVENT_NOTIFY_WIFI_LINK_POOR, false);
        } else if (getCurrentState() == this.mWiFiProVerfyingLinkState) {
            onNetworkQosChange(1, 3, false);
        }
    }

    public void notifyRoamingCompleted(String newBssid) {
        if (newBssid != null && getCurrentState() == this.mWiFiProVerfyingLinkState) {
            sendMessageDelayed(EVENT_LAA_STATUS_CHANGED, 3000);
        }
    }

    /* access modifiers changed from: private */
    public void logD(String info) {
        Log.d(TAG, info);
    }

    /* access modifiers changed from: private */
    public void logW(String info) {
        Log.w(TAG, info);
    }

    public static void resetParameter() {
        mIsWifiManualEvaluating = false;
        mIsWifiSemiAutoEvaluating = false;
    }

    public void onDisableWiFiPro() {
        logD("WiFiProDisabledState is Enter");
        resetParameter();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mWifiProUIDisplayManager.shownAccessNotification(false);
        this.mWiFiProEvaluateController.cleanEvaluateRecords();
        this.mHwIntelligenceWiFiManager.stop();
        stopDualBandManager();
        if (isWifiConnected()) {
            logD("WiFiProDisabledState , wifi is connect ");
            WifiInfo cInfo = this.mWifiManager.getConnectionInfo();
            if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && NetworkInfo.DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                logD("wifi State == VERIFYING_POOR_LINK");
                this.mWsmChannel.sendMessage(GOOD_LINK_DETECTED);
            }
            setWifiCSPState(1);
        }
        diableResetVariables();
        disableTransitionNetState();
        if (this.mIsPrimaryUser) {
            uploadWifiproDisabledStatistics();
        }
    }

    private void diableResetVariables() {
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWiFiProPdpSwichValue = 0;
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = 0;
        this.mIsUserHandoverWiFi = false;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = false;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    private void disableTransitionNetState() {
        if (isWifiConnected()) {
            logD("onDisableWiFiPro,go to WifiConnectedState");
            this.mNetworkQosMonitor.queryNetworkQos(1, this.mIsPortalAp, this.mIsNetworkAuthen, false);
            transitionTo(this.mWifiConnectedState);
            return;
        }
        logD("onDisableWiFiPro, go to mWifiDisConnectedState");
        transitionTo(this.mWifiDisConnectedState);
    }

    private void registerMapNavigatingStateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(MAPS_LOCATION_FLAG), false, this.mMapNavigatingStateChangeObserver);
    }

    private void registerVehicleStateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(VEHICLE_STATE_FLAG), false, this.mVehicleStateChangeObserver);
    }

    /* access modifiers changed from: private */
    public void setWifiMonitorEnabled(boolean enabled) {
        logD("setWifiLinkDataMonitorEnabled  is " + enabled);
        this.mNetworkQosMonitor.setMonitorWifiQos(1, enabled);
        this.mNetworkQosMonitor.setIpQosEnabled(enabled);
    }

    /* access modifiers changed from: private */
    public boolean isFullscreen() {
        return this.mAbsPhoneWindowManager != null && this.mAbsPhoneWindowManager.isTopIsFullscreen();
    }

    public void sendInternetCheckRequest() {
        logD("sendInternetCheckRequest");
        sendMessage(EVENT_WIFI_QOS_CHANGE, -1, 0, false);
    }

    public void notifyNetworkUserConnect(boolean isUserConnect) {
        logD("notifyNetworkUserConnect: isUserConnect = " + isUserConnect);
        sendMessage(EVENT_NETWORK_USER_CONNECT, Boolean.valueOf(isUserConnect));
    }

    public void notifyApkChangeWifiStatus(boolean enable, String packageName) {
        if (enable) {
            this.mCloseBySystemui = false;
        } else if (packageName.equals("com.android.systemui")) {
            this.mCloseBySystemui = true;
        }
    }

    public void notifyWifiDisconnected(Intent intent) {
        logD("notifyWifiDisconnected:EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED");
        sendMessage(EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED, intent);
    }

    public void uploadWifiproDisabledStatistics() {
        long currentTimeMillis = SystemClock.elapsedRealtime();
        int topUid = -1;
        String pktName = "";
        HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
        if (autoConnectManager != null) {
            topUid = autoConnectManager.getCurrentTopUid();
            pktName = autoConnectManager.getCurrentPackageName();
            if (pktName != null && pktName.equals("com.huawei.hwstartupguide")) {
                this.mIsWifiproDisableOnReboot = false;
            }
        }
        HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
        if (chrInstance != null && topUid != -1 && pktName != null && !this.mIsWifiproDisableOnReboot) {
            if (this.mLastWifiproDisableTime == 0 || currentTimeMillis - this.mLastWifiproDisableTime > 7200000) {
                this.mLastWifiproDisableTime = currentTimeMillis;
                Bundle data = new Bundle();
                if (pktName.equals("com.android.settings")) {
                    data.putInt("appType", 101);
                    logD("appType == com.android.settings");
                } else if (pktName.equals("com.huawei.hwstartupguide")) {
                    data.putInt("appType", 102);
                    logD("appType == com.huawei.hwstartupguide");
                } else {
                    data.putInt("appType", 103);
                    logD("appType == 103");
                }
                chrInstance.uploadDFTEvent(909002066, data);
            }
        }
    }

    public void uploadPortalAuthExpirationStatistics(boolean isNotificationClicked) {
        HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
        int validityDura = 0;
        int connDura = 0;
        if (this.mCurrentWifiConfig.portalValidityDuration < 86400000) {
            validityDura = (int) this.mCurrentWifiConfig.portalValidityDuration;
        }
        if (System.currentTimeMillis() - this.connectStartTime < 86400000) {
            connDura = (int) (System.currentTimeMillis() - this.connectStartTime);
        }
        if (chrInstance != null) {
            logD("upload portal chr");
            Bundle data = new Bundle();
            data.putInt("dura", validityDura);
            data.putInt("isPeriodicDet", this.isPeriodicDet ? 1 : 0);
            data.putString("respCode", this.respCodeChrInfo);
            data.putInt("detNum", this.detectionNumSlow);
            data.putInt("connDura", connDura);
            data.putInt("isNotificationClicked", isNotificationClicked);
            chrInstance.uploadDFTEvent(909009072, data);
        }
        this.respCodeChrInfo = "";
        this.detectionNumSlow = 0;
    }

    /* access modifiers changed from: private */
    public boolean shouldUploadCloseWifiEvent() {
        if (this.mIsWiFiNoInternet || WifiProCommonUtils.getAirplaneModeOn(this.mContext) || this.mWifiManager.isWifiApEnabled()) {
            return false;
        }
        long deltaTime = System.currentTimeMillis() - this.mLastDisconnectedTimeStamp;
        if ((getCurrentState() != this.mWifiDisConnectedState && this.mCurrentRssi >= -75) || (getCurrentState() == this.mWifiDisConnectedState && (deltaTime > 10000 || this.mLastDisconnectedRssi >= -75))) {
            return false;
        }
        String pktName = "";
        HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
        if (autoConnectManager != null) {
            pktName = autoConnectManager.getCurrentPackageName();
        }
        if ("com.android.settings".equals(pktName) || this.mCloseBySystemui) {
            return true;
        }
        return false;
    }

    public void setLastDisconnectNetwork() {
        this.mLastConnectedSsid = this.mCurrentSsid;
        this.mLastDisconnectedTimeStamp = System.currentTimeMillis();
        this.mLastDisconnectedRssi = this.mCurrentRssi;
    }
}
