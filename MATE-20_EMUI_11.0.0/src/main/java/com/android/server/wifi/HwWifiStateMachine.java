package com.android.server.wifi;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.display.WifiDisplayStatus;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.NetworkInfo;
import android.net.StaticIpConfiguration;
import android.net.dhcp.HwArpClient;
import android.net.ip.IpClientManager;
import android.net.shared.ProvisioningConfiguration;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.IWifiRepeaterConfirmListener;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.net.wifi.hwUtil.HwWifiSsidEx;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.LruCache;
import android.util.wifi.HwHiLog;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IState;
import com.android.server.HwServiceFactory;
import com.android.server.hidata.arbitration.HwArbitrationStateMachine;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.os.GetUDIDNative;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.ABS.HwABSWiFiHandler;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.MSS.HwMSSHandler;
import com.android.server.wifi.MSS.HwMSSUtils;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.cast.CastOptChr;
import com.android.server.wifi.dc.DcMonitor;
import com.android.server.wifi.fastsleep.FsArbitration;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class HwWifiStateMachine extends ClientModeImpl {
    private static final String ACTIVITY_LIFE_STATE = "activityLifeState";
    public static final int AP_CAP_CACHE_COUNT = 1000;
    public static final String AP_CAP_KEY = "AP_CAP";
    private static final String AP_INFO_PERMISSION = "com.huawei.permission.ACCESS_AP_INFORMATION";
    private static final String[] AP_INFO_WHITE_LIST = {"com.android.nfc", "com.android.mediacenter", "com.android.settings", "com.android.imedia.syncplay", "com.huawei.smarthome", "com.huaweioverseas.smarthome", "com.huawei.smartspeaker"};
    private static final int ASSOCIATE_ASSISTANTE_TIMEOUT_VALUE = 90000;
    private static final String ASSOCIATION_REJECT_STATUS_CODE = "wifi_association_reject_status_code";
    private static final String BRCM_CHIP_4359 = "bcm4359";
    private static final int BW160_SUPPORTED = 1;
    private static final String CHIPSET_WIFI_CATEGORY = "chipset_wifi_category";
    public static final int CMD_AP_STARTED_GET_STA_LIST = 131104;
    public static final int CMD_AP_STARTED_SET_DISASSOCIATE_STA = 131106;
    public static final int CMD_AP_STARTED_SET_MAC_FILTER = 131105;
    private static final int CMD_GET_AP160_SUPPORTED = 112;
    private static final int CMD_GET_AP_BANDWIDTH = 114;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    private static final int CMD_GET_DEVICE_FILTER_PKTS = 107;
    private static final int CMD_GET_WIFI_CATEGORY = 127;
    private static final int CMD_QUERY_CSI = 164;
    public static final int CMD_SCREEN_OFF_SCAN = 131578;
    private static final int CMD_SET_FEM_LOWPOWER = 113;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    public static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final String COLON_SYMBOL = ":";
    private static final String COMM_IFACE = "wlan0";
    private static final String DBKEY_HOTSPOT20_VALUE = "hw_wifi_hotspot2_on";
    private static final int DEFAULT_ARP_DETECT_TIME = 5;
    private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
    private static final int DHCP_RESULT_CACHE_SIZE = 50;
    public static final int EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO = 909002071;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    private static final String EVENT_CHIP_RECOVERY = "ChipRecoveryEnter";
    private static final String EVENT_DUALBAND_SWITCH = "DualBandSwitchEnter";
    private static final String EVENT_REASSOCIATION_RECOVERY = "ReassociationEnter";
    private static final String EVENT_RECOVERY_FINISHED = "RecoveryFinished";
    private static final String EXTRA_FLAG_HILINK_DETECT_NOT_PORTAL = "detect_not_portal";
    private static final int FAC_MAC_REASSOC = 2;
    private static final int FAILURE_CODE_ASSOCIATION = 1;
    private static final int FAST_ARP_DETECTION = 1;
    private static final int FILTER_CMD_LENGTH = 4;
    private static final String FLAG_HOME_ROUTER = "flag_home_router";
    private static final int GET_WIFI_CATEGORY_TIMEOUT = 100;
    private static final String HIGEO_PACKAGE_NAME = "com.huawei.lbs";
    private static final int HIGEO_STATE_DEFAULT_MODE = 0;
    private static final int HIGEO_STATE_WIFI_SCAN_MODE = 1;
    private static final int IFACE_TYPE_AP = 1;
    private static final int IFACE_TYPE_GC = 4;
    private static final int IFACE_TYPE_GO = 3;
    private static final int IFACE_TYPE_STA = 2;
    private static final int INVALID_UID = -1;
    private static final boolean IS_REPEATER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_wifibridge", false);
    private static final String KEY_COMPONENT = "comp";
    private static final String KEY_RADOMMAC_SSID = "random_mac_ssid";
    private static final String KEY_RADOMMAC_STATE = "random_mac_state";
    private static final String KEY_STATE = "state";
    private static final String KEY_UID = "uid";
    private static final int NORMAL_REASSOC = 1;
    private static final String ON_RESUME = "onResume";
    public static final int PM_LOWPWR = 7;
    public static final int PM_NORMAL = 6;
    private static final int RANDOM_MAC_CONNECT_SUCCESS = 0;
    private static final int RANDOM_MAC_SELF_CURE_SUCCESS = 2;
    private static final int RANDOM_MAC_START_SELF_CURE = 1;
    private static final int RANDOM_MAC_TRIGGER_SELFCURE_THR = 2;
    private static final int RAND_MAC_REASSOC = 3;
    public static final int SCAN_ONLY_CONNECT_MODE = 100;
    private static final int SLOW_ARP_DETECTION = 2;
    private static final String SOFTAP_IFACE = "wlan0";
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_WAPI_EVENT = "android.net.wifi.supplicant.WAPI_EVENT";
    private static final String TAG = "HwWifiStateMachine";
    private static final int THE_FIRST_INDEX_OF_STRING = 0;
    private static final int THE_MIN_OFFSET = 1;
    private static final int THE_RETURN_VALUE_OF_NOT_FINDING_STRING = -1;
    private static final long TIMEOUT_CONTROL_SCAN_ASSOCIATED = 5000;
    private static final long TIMEOUT_CONTROL_SCAN_ASSOCIATING = 2000;
    private static final String TV_PRIMARY_DEVICE_TYPE = "7-0050F204-1";
    private static final int TYPE_NOT_PORTAL = 3;
    private static final int TYPE_NO_INTERNET = 0;
    private static final String TYPE_PORTAL = "2";
    private static final String USB_SUPPLY = "/sys/class/power_supply/USB/online";
    private static final String USB_SUPPLY_QCOM = "/sys/class/power_supply/usb/online";
    public static final int WAPI_AUTHENTICATION_FAILURE_EVENT = 147474;
    public static final int WAPI_CERTIFICATION_FAILURE_EVENT = 147475;
    public static final int WAPI_EVENT_AUTH_FAIL_CODE = 16;
    public static final int WAPI_EVENT_CERT_FAIL_CODE = 17;
    private static final int WIFI_CATEGORY_DEFAULT = 1;
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_FEATURE_BW160 = 1;
    private static final int WIFI_GLOBAL_SCAN_CTRL_FOUL_INTERVAL = 5000;
    private static final int WIFI_GLOBAL_SCAN_CTRL_FREED_INTERVAL = 10000;
    private static final int WIFI_LINK_DETECT_CNT = 3;
    private static final int WIFI_MAX_FOUL_TIMES = 5;
    private static final int WIFI_MAX_FREED_TIMES = 5;
    private static final long WIFI_SCAN_BLACKLIST_REMOVE_INTERVAL = 7200000;
    private static final String WIFI_SCAN_CONNECTED_LIMITED_WHITE_PACKAGENAME = "wifi_scan_connected_limited_white_packagename";
    private static final String WIFI_SCAN_INTERVAL_WHITE_WLAN_CONNECTED = "wifi_scan_interval_white_wlan_connected";
    private static final String WIFI_SCAN_INTERVAL_WLAN_CLOSE = "wifi_scan_interval_wlan_close";
    private static final long WIFI_SCAN_INTERVAL_WLAN_CLOSE_DEFAULT = 20000;
    private static final String WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED = "wifi_scan_interval_wlan_not_connected";
    private static final long WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED_DEFAULT = 10000;
    private static final long WIFI_SCAN_INTERVAL_WLAN_WHITE_CONNECTED_DEFAULT = 10000;
    private static final long WIFI_SCAN_OVER_INTERVAL_MAX_COUNT = 10;
    private static final long WIFI_SCAN_RESULT_DELAY_TIME_DEFAULT = 300;
    private static final String WIFI_SCAN_WHITE_PACKAGENAME = "wifi_scan_white_packagename";
    private static final int WIFI_START_EVALUATE_TAG = 1;
    private static final int WIFI_STOP_EVALUATE_TAG = 0;
    private static final int WLAN_STATUS_AP_UNABLE_TO_HANDLE_NEW_STA = 17;
    private static final int WLAN_STATUS_AUTH_TIMEOUT = 16;
    private static int mFrequency = 0;
    private static WifiNativeUtils wifiNativeUtils = EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class);
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private boolean isInGlobalScanCtrl = false;
    private long lastConnectTime = -1;
    private HashMap<String, String> lastDhcps = new HashMap<>();
    private long lastScanResultTimestamp = 0;
    private ActivityManager mActivityManager;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass1 */

        public void call(Bundle extras) {
            HwWifiStateMachine.this.mIsInWifiSettings = false;
            if (extras == null) {
                Log.e(HwWifiStateMachine.TAG, "extras is null");
                return;
            }
            Object tempComponentName = extras.getParcelable(HwWifiStateMachine.KEY_COMPONENT);
            if (tempComponentName instanceof ComponentName) {
                ComponentName componentName = (ComponentName) tempComponentName;
                int uid = extras.getInt(HwWifiStateMachine.KEY_UID);
                String currentState = extras.getString(HwWifiStateMachine.KEY_STATE);
                if (!(componentName == null || uid == -1 || !WifiCommonUtils.getSettingActivityName().equals(componentName.getClassName()))) {
                    HwWifiStateMachine.this.mIsInWifiSettings = HwWifiStateMachine.ON_RESUME.equals(currentState);
                }
                if (HwWifiStateMachine.ON_RESUME.equals(extras.getString(HwWifiStateMachine.KEY_STATE)) && componentName != null && uid != -1) {
                    HwWifiStateMachine.this.mForgroundAppName = componentName.getPackageName();
                }
            }
        }
    };
    private HashMap<String, Boolean> mApCapChr = new HashMap<>();
    private AppTypeRecoManager mAppTypeRecoManager;
    private HwArpClient mArpClient;
    private int mBQEUid;
    private BroadcastReceiver mBcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("huawei.wifi.WIFI_MODE_STATE".equals(action) && intent.getIntExtra("wifi_mode_state", 0) == 2) {
                    HwWifiStateMachine.this.mWifiInjector.getWifiSettingsStore().handleWifiToggled(false);
                }
                String chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "");
                WifiInfo wifiInfo = HwWifiStateMachine.wifiStateMachineUtils.getWifiInfo(HwWifiStateMachine.this);
                boolean isMobileAP = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwWifiStateMachine.this.myContext);
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    int PluggedType = intent.getIntExtra("plugged", 0);
                    if (PluggedType == 2 || PluggedType == 5) {
                        HwWifiStateMachine.this.mIsScanCtrlPluggedin = true;
                    } else if (!HwWifiStateMachine.this.getChargingState()) {
                        HwWifiStateMachine.this.mIsScanCtrlPluggedin = false;
                    }
                    HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                    hwWifiStateMachine.logd("mBcastReceiver: PluggedType = " + PluggedType + " mIsScanCtrlPluggedin = " + HwWifiStateMachine.this.mIsScanCtrlPluggedin);
                } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(action)) {
                    HwWifiStateMachine.this.handleMiracastStateChangeMsg(intent);
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    HwWifiStateMachine.this.handleP2pConnectionChangedAction(intent);
                } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                    HwWifiStateMachine.this.handleP2pConnectedChangedAction(intent);
                } else {
                    HwWifiStateMachine hwWifiStateMachine2 = HwWifiStateMachine.this;
                    hwWifiStateMachine2.logd("unknow:action = " + action);
                }
                if (HwWifiStateMachine.BRCM_CHIP_4359.equals(chipName)) {
                    if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                        int PluggedType2 = intent.getIntExtra("plugged", 0);
                        if (PluggedType2 == 2 || PluggedType2 == 5) {
                            HwWifiStateMachine.this.mIsChargePluggedin = true;
                            return;
                        }
                        HwWifiStateMachine.this.mIsChargePluggedin = false;
                        HwWifiStateMachine.this.mIsAllowedManualPwrBoost = 0;
                    } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null) {
                            int i = AnonymousClass8.$SwitchMap$android$net$NetworkInfo$DetailedState[networkInfo.getDetailedState().ordinal()];
                            if (i == 1) {
                                HwWifiStateMachine.this.logd("setpmlock:CONNECTED");
                                HwWifiStateMachine.this.mWifiConnectState = true;
                                if (wifiInfo != null) {
                                    HwWifiStateMachine.this.mSsid = wifiInfo.getSSID();
                                }
                                HwWifiStateMachine hwWifiStateMachine3 = HwWifiStateMachine.this;
                                hwWifiStateMachine3.setLowPwrMode(hwWifiStateMachine3.mWifiConnectState, HwWifiStateMachine.this.mSsid, isMobileAP, HwWifiStateMachine.this.mScreenState);
                            } else if (i == 2) {
                                HwWifiStateMachine.this.logd("setpmlock:DISCONNECTED");
                                HwWifiStateMachine.this.mWifiConnectState = false;
                                HwWifiStateMachine hwWifiStateMachine4 = HwWifiStateMachine.this;
                                hwWifiStateMachine4.setLowPwrMode(hwWifiStateMachine4.mWifiConnectState, null, isMobileAP, HwWifiStateMachine.this.mScreenState);
                            }
                        }
                    } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                        HwWifiStateMachine hwWifiStateMachine5 = HwWifiStateMachine.this;
                        hwWifiStateMachine5.logd("setpmlock:action = " + action);
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        HwWifiStateMachine hwWifiStateMachine6 = HwWifiStateMachine.this;
                        hwWifiStateMachine6.logd("setpmlock:action = " + action);
                    }
                }
            }
        }
    };
    private ConnectivityManager mConnMgr = null;
    private int mConnectFailedCnt = 0;
    public boolean mCurrNetworkHistoryInserted = false;
    private int mCurrentConfigNetId = -1;
    private String mCurrentConfigurationKey = null;
    private boolean mCurrentPwrBoostStat = false;
    private boolean mDelayWifiScoreBySelfCureOrSwitch = false;
    private Queue<IState> mDestStates = null;
    private final LruCache<String, DhcpResults> mDhcpResultCache = new LruCache<>(50);
    private String mForgroundAppName = "";
    private int mFoulTimes = 0;
    private int mFreedTimes = 0;
    private HiLinkController mHiLinkController = null;
    private HwInnerNetworkManagerImpl mHwInnerNetworkManagerImpl;
    private HwSoftApManager mHwSoftApManager;
    private HwWifiCHRService mHwWifiCHRService;
    private HwWifiProServiceManager mHwWifiProServiceManager;
    public int mIsAllowedManualPwrBoost = 0;
    private boolean mIsChargePluggedin = false;
    private boolean mIsFinishLinkDetect = false;
    private boolean mIsIgnoreScanInCastScene = false;
    private boolean mIsInWifiSettings = false;
    private boolean mIsNeedIgnoreScan = false;
    private boolean mIsP2pConnected = false;
    private boolean mIsP2pHasTvDevice = false;
    private boolean mIsReassocUseWithFactoryMacAddress = false;
    private boolean mIsScanCtrlPluggedin = false;
    private int mLastConnectNetworkId = -1;
    private long mLastScanTimestamp = 0;
    private int mLastTxPktCnt = 0;
    private WifiP2pManager.Channel mP2pChannel;
    private AtomicBoolean mP2pGroupCreated = new AtomicBoolean(false);
    private HashMap<String, Integer> mPidBlackList = new HashMap<>();
    private long mPidBlackListInteval = 0;
    private HashMap<String, Integer> mPidConnectedBlackList = new HashMap<>();
    private HashMap<Integer, Long> mPidLastScanSuccTimestamp = new HashMap<>();
    private HashMap<Integer, Long> mPidLastScanTimestamp = new HashMap<>();
    private HashMap<Integer, Integer> mPidWifiScanCount = new HashMap<>();
    private int mPwrBoostOffcnt = 0;
    private int mPwrBoostOncnt = 0;
    private int mRandomMacState = 0;
    private AtomicBoolean mRenewDhcpSelfCuring = new AtomicBoolean(false);
    private int mScreenOffScanToken = 0;
    private boolean mScreenState = true;
    public WifiConfiguration mSelectedConfig = null;
    private NetworkInfo.DetailedState mSelfCureNetworkLastState = NetworkInfo.DetailedState.IDLE;
    private int mSelfCureWifiConnectRetry = 0;
    private int mSelfCureWifiLastState = -1;
    private boolean mShouldStartNewScan = false;
    private SoftApChannelXmlParse mSoftApChannelXmlParse;
    private String mSsid = null;
    private long mTimeLastCtrlScanDuringObtainingIp = 0;
    private long mTimeOutScanControlForAssoc = 0;
    private long mTimeStampScanControlForAssoc = 0;
    public boolean mUserCloseWifiWhenSelfCure = false;
    private WifiSsid mWiFiProRoamingSSID = null;
    public boolean mWifiAlwaysOnBeforeCure = false;
    public boolean mWifiBackgroundConnected = false;
    private boolean mWifiConnectState = false;
    private WifiDetectConfInfo mWifiDetectConfInfo = new WifiDetectConfInfo();
    private int mWifiDetectperiod = -1;
    private long mWifiEnabledTimeStamp = 0;
    private WifiInjector mWifiInjector;
    private int mWifiMode = 0;
    private String mWifiModeCallerPackageName = "";
    private WifiP2pManager mWifiP2pManager;
    private WifiRepeaterDialog mWifiRepeaterDialog;
    private int mWifiSelfCureState = 0;
    private AtomicBoolean mWifiSelfCuring = new AtomicBoolean(false);
    private AtomicBoolean mWifiSoftSwitchRunning = new AtomicBoolean(false);
    public boolean mWifiSwitchOnGoing = false;
    private HwWifiChipPowerAbnormal mWifichipCheck = null;
    private Wpa3SelfCureImpl mWpa3SelfCureImpl;
    private HwMSSArbitrager mssArbi = null;
    private Context myContext;
    private final Object selectConfigLock = new Object();
    private boolean usingStaticIpConfig = false;
    private int wifiConnectedBackgroundReason = 0;
    private WifiEapUIManager wifiEapUIManager;

    static /* synthetic */ int access$2608(HwWifiStateMachine x0) {
        int i = x0.mPwrBoostOncnt;
        x0.mPwrBoostOncnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$2708(HwWifiStateMachine x0) {
        int i = x0.mPwrBoostOffcnt;
        x0.mPwrBoostOffcnt = i + 1;
        return i;
    }

    public HwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager) {
        super(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative, wrongPasswordNotifier, sarManager, wifiTrafficPoller, linkProbeManager);
        this.myContext = context;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mBQEUid = 1000;
        this.mHwInnerNetworkManagerImpl = HwFrameworkFactory.getHwInnerNetworkManager();
        registerReceiverInWifiPro(context);
        registerForWifiEvaluateChanges();
        this.mssArbi = HwMSSArbitrager.getInstance(context);
        if (WifiRadioPowerController.isRadioPowerEnabled()) {
            WifiRadioPowerController.setInstance(context, this, wifiStateMachineUtils.getWifiNative(this), HwFrameworkFactory.getHwInnerNetworkManager());
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            registerForPasspointChanges();
        }
        this.mHiLinkController = new HiLinkController(context, this);
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mDestStates = new LinkedList();
        pwrBoostRegisterBcastReceiver();
        if (PreconfiguredNetworkManager.IS_R1) {
            this.wifiEapUIManager = new WifiEapUIManager(context);
        }
        wifiStateMachineUtils.getWifiConfigManager(this).setSupportWapiType();
        this.mSoftApChannelXmlParse = new SoftApChannelXmlParse(context);
        this.mWifiInjector = wifiInjector;
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        this.mWpa3SelfCureImpl = Wpa3SelfCureImpl.createSelfCureImpl(context);
        this.mArpClient = new HwArpClient(this.myContext);
        this.mWifiRepeaterDialog = WifiRepeaterDialog.createWifiRepeaterDialog(context);
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, ACTIVITY_LIFE_STATE);
        this.mWifichipCheck = new HwWifiChipPowerAbnormal(context, this.mHwWifiCHRService, this);
        registerP2pStateChange();
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
    }

    public boolean isForgroundApp(String packageName) {
        if (TextUtils.isEmpty(packageName) || !packageName.equals(this.mForgroundAppName)) {
            return false;
        }
        HwHiLog.i(TAG, false, "%{public}s is forground app", new Object[]{this.mForgroundAppName});
        return true;
    }

    public boolean isInGameAppMode() {
        if (TextUtils.isEmpty(this.mForgroundAppName)) {
            return false;
        }
        int type = this.mAppTypeRecoManager.getAppType(this.mForgroundAppName);
        if (type != 305 && type != 9) {
            return false;
        }
        HwHiLog.i(TAG, false, "%{public}s is in game app mode", new Object[]{this.mForgroundAppName});
        return true;
    }

    private void registerP2pStateChange() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        this.myContext.registerReceiver(this.mBcastReceiver, intentFilter);
    }

    private String dealWpaSuppConfig() {
        if (WifiInjector.getInstance().getWifiNative() == null || WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx == null || WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.getWpaSuppConfig() == null) {
            HwHiLog.e(TAG, false, "can not get getWpaSuppConfig", new Object[0]);
            return null;
        }
        String tmpWpaSuppConfig = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.getWpaSuppConfig();
        int firstColonIndex = tmpWpaSuppConfig.indexOf(COLON_SYMBOL);
        if (firstColonIndex == -1) {
            HwHiLog.e(TAG, false, "firstColonIndex is invalid ", new Object[0]);
            return null;
        }
        int secondColonIndex = tmpWpaSuppConfig.indexOf(COLON_SYMBOL, firstColonIndex + 1);
        if (secondColonIndex == -1) {
            HwHiLog.e(TAG, false, "secondColonIndex is invalid ", new Object[0]);
            return null;
        }
        try {
            int ssidBase64Len = Integer.parseInt(tmpWpaSuppConfig.substring(0, firstColonIndex));
            try {
                byte[] ssidByte = Base64.getDecoder().decode(tmpWpaSuppConfig.substring(secondColonIndex + 1, secondColonIndex + ssidBase64Len + 1));
                if (ssidByte != null) {
                    int ssidByteLen = ssidByte.length;
                    String ssid = HwWifiSsidEx.encodingWithCharset(ssidByte);
                    HwHiLog.d(TAG, false, "wpaSuppConfig ssid = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid)});
                    String authType = tmpWpaSuppConfig.substring(firstColonIndex + 1, secondColonIndex);
                    String shareKey = tmpWpaSuppConfig.substring(secondColonIndex + ssidBase64Len + 1);
                    return String.valueOf(ssidByteLen) + COLON_SYMBOL + authType + COLON_SYMBOL + ssid + shareKey;
                }
                HwHiLog.e(TAG, false, "SSIDByte = null and return null ", new Object[0]);
                return null;
            } catch (IllegalArgumentException e) {
                HwHiLog.e(TAG, false, "decoder.decode fail", new Object[0]);
                return null;
            }
        } catch (NumberFormatException e2) {
            HwHiLog.e(TAG, false, "SSIDBase64Len is invalid", new Object[0]);
            return null;
        }
    }

    private String getAppName(int processId) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == processId) {
                return appProcess.processName;
            }
        }
        return "";
    }

    private boolean hasSignaturePermission(String packageName) {
        PackageManager packageManager = this.myContext.getPackageManager();
        if (packageManager == null) {
            HwHiLog.e(TAG, false, "hasSignaturePermission packageManager is null", new Object[0]);
            return false;
        }
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                return applicationInfo.isSignedWithPlatformKey();
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            HwHiLog.e(TAG, false, "hasSignaturePermission failed", new Object[0]);
            return false;
        }
    }

    private boolean isApInfoWhiteListPackage(String packageName) {
        for (String whiteListPackage : AP_INFO_WHITE_LIST) {
            if (whiteListPackage.equals(packageName)) {
                return true;
            }
        }
        HwHiLog.d(TAG, false, "isApInfoWhiteListPackage " + packageName + " is not white list app", new Object[0]);
        return false;
    }

    public String getWpaSuppConfig() {
        HwHiLog.d(TAG, false, "WiFIStateMachine  getWpaSuppConfig InterfaceName ", new Object[0]);
        if (this.myContext.checkCallingPermission(AP_INFO_PERMISSION) != 0) {
            HwHiLog.w(TAG, false, "getWpaSuppConfig(): permissin deny", new Object[0]);
            return null;
        }
        String packageName = getAppName(Binder.getCallingPid());
        if (packageName == null || packageName.equals("")) {
            HwHiLog.e(TAG, false, "getWpaSuppConfig packageName is null", new Object[0]);
            return null;
        } else if (hasSignaturePermission(packageName) || isApInfoWhiteListPackage(packageName)) {
            return dealWpaSuppConfig();
        } else {
            HwHiLog.d(TAG, false, "getWpaSuppConfig package has no signature permission and is not in white list", new Object[0]);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void enableAllNetworksByMode() {
        HwHiLog.d(TAG, false, "enableAllNetworks mOperationalMode: %{public}d", new Object[]{Integer.valueOf(wifiStateMachineUtils.getOperationalMode(this))});
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        }
    }

    /* access modifiers changed from: protected */
    public void handleNetworkDisconnect() {
        HwHiLog.d(TAG, false, "handle network disconnect mOperationalMode: %{public}d", new Object[]{Integer.valueOf(wifiStateMachineUtils.getOperationalMode(this))});
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            HwDisableLastNetwork();
        }
        HwHiLog.d(TAG, false, "handleNetworkDisconnect,resetWifiProManualConnect", new Object[0]);
        resetWifiProManualConnect();
        HwWifiStateMachineEx.resetWifiPoorLink();
        HwWifiStateMachine.super.handleNetworkDisconnect();
    }

    /* access modifiers changed from: protected */
    public void loadAndEnableAllNetworksByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            HwHiLog.d(TAG, false, "supplicant connection mOperationalMode: %{public}d", new Object[]{Integer.valueOf(wifiStateMachineUtils.getOperationalMode(this))});
            WifiConfigStoreUtils.loadConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        } else {
            WifiConfigStoreUtils.loadAndEnableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        }
        if (isWifiSelfCuring()) {
            updateNetworkId();
        }
    }

    private void HwDisableLastNetwork() {
        HwHiLog.d(TAG, false, "HwDisableLastNetwork, currentState:%{public}s, mLastNetworkId:%{public}d", new Object[]{getCurrentState(), Integer.valueOf(wifiStateMachineUtils.getLastNetworkId(this))});
    }

    /* access modifiers changed from: protected */
    public boolean processScanModeSetMode(Message message, int mLastOperationMode) {
        if (message.arg1 != 100) {
            return false;
        }
        HwHiLog.d(TAG, false, "SCAN_ONLY_CONNECT_MODE, do not enable all networks here.", new Object[0]);
        if (mLastOperationMode == 3) {
            WifiConfigStoreUtils.loadConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            if (wifiStateMachineUtils.getWifiP2pChannel(this) == null) {
                wifiStateMachineUtils.getAdditionalWifiServiceInterfaces(this);
                HwHiLog.d(TAG, false, "mWifiP2pChannel retry init", new Object[0]);
            }
        }
        wifiStateMachineUtils.setOperationalMode(this, 100);
        transitionTo(wifiStateMachineUtils.getDisconnectedState(this));
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processConnectModeSetMode(Message message) {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100 || message.arg2 != 0) {
            return false;
        }
        HwHiLog.d(TAG, false, "CMD_ENABLE_NETWORK command is ignored.", new Object[0]);
        wifiStateMachineUtils.replyToMessage((ClientModeImpl) this, message, message.what, 1);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processL2ConnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            if (!wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
                sendMessage(131145);
            }
            disableAllNetworksExceptLastConnected();
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != 1) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processDisconnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        HwHiLog.d(TAG, false, "set operation mode mOperationalMode: %{public}d", new Object[]{Integer.valueOf(wifiStateMachineUtils.getOperationalMode(this))});
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != 1) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            wifiStateMachineUtils.getWifiNative(this).reconnect(wifiStateMachineUtils.getInterfaceName(this));
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void enterConnectedStateByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            HwHiLog.d(TAG, false, "wifi connected. disable other networks.", new Object[0]);
            disableAllNetworksExceptLastConnected();
        }
    }

    /* access modifiers changed from: protected */
    public boolean enterDriverStartedStateByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            return false;
        }
        HwHiLog.d(TAG, false, "SCAN_ONLY_CONNECT_MODE, disable all networks.", new Object[0]);
        wifiStateMachineUtils.getWifiNative(this).disconnect(wifiStateMachineUtils.getInterfaceName(this));
        WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        transitionTo(wifiStateMachineUtils.getDisconnectedState(this));
        return true;
    }

    private void disableAllNetworksExceptLastConnected() {
        HwHiLog.d(TAG, false, "disable all networks except last connected. currentState:%{public}s, mLastNetworkId:%{public}d", new Object[]{getCurrentState(), Integer.valueOf(wifiStateMachineUtils.getLastNetworkId(this))});
        for (WifiConfiguration network : WifiConfigStoreUtils.getConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this))) {
            if (network.networkId != wifiStateMachineUtils.getLastNetworkId(this)) {
                int i = network.status;
            }
        }
    }

    public boolean isScanAndManualConnectMode() {
        return wifiStateMachineUtils.getOperationalMode(this) == 100;
    }

    /* access modifiers changed from: protected */
    public boolean processConnectModeAutoConnectByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            return false;
        }
        HwHiLog.d(TAG, false, "CMD_AUTO_CONNECT command is ignored..", new Object[0]);
        return true;
    }

    /* access modifiers changed from: protected */
    public void recordAssociationRejectStatusCode(int statusCode) {
        Settings.System.putInt(this.myContext.getContentResolver(), ASSOCIATION_REJECT_STATUS_CODE, statusCode);
    }

    /* access modifiers changed from: protected */
    public void startScreenOffScan() {
        int configNetworksSize = wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM).size();
        if (!wifiStateMachineUtils.getScreenOn(this) && configNetworksSize > 0) {
            logd("begin scan when screen off");
            int i = this.mScreenOffScanToken + 1;
            this.mScreenOffScanToken = i;
            sendMessageDelayed(obtainMessage(CMD_SCREEN_OFF_SCAN, i, 0), wifiStateMachineUtils.getSupplicantScanIntervalMs(this));
        }
    }

    /* access modifiers changed from: protected */
    public boolean processScreenOffScan(Message message) {
        if (131578 != message.what) {
            return false;
        }
        if (message.arg1 != this.mScreenOffScanToken) {
            return true;
        }
        startScreenOffScan();
        return true;
    }

    /* access modifiers changed from: protected */
    public void makeHwDefaultIPTable(DhcpResults dhcpResults) {
        synchronized (this.mDhcpResultCache) {
            try {
                String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
                if (key == null) {
                    HwHiLog.w(TAG, false, "makeHwDefaultIPTable key is null!", new Object[0]);
                    return;
                }
                if (this.mDhcpResultCache.get(key) != null) {
                    HwHiLog.d(TAG, false, "make default IP configuration map, remove old rec.", new Object[0]);
                    this.mDhcpResultCache.remove(key);
                }
                boolean isPublicESS = false;
                int count = 0;
                String ssid = "";
                String capabilities = "";
                List<ScanResult> scanList = new ArrayList<>();
                if (!WifiInjector.getInstance().getClientModeImplHandler().runWithScissors(new Runnable(scanList) {
                    /* class com.android.server.wifi.$$Lambda$HwWifiStateMachine$iSDo2643LM7D37HI0i8CX3IwIOM */
                    private final /* synthetic */ List f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwWifiStateMachine.this.lambda$makeHwDefaultIPTable$0$HwWifiStateMachine(this.f$1);
                    }
                }, 4000)) {
                    HwHiLog.e(TAG, false, "Failed to post runnable to fetch scan results", new Object[0]);
                    return;
                }
                try {
                    Iterator<ScanResult> it = scanList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ScanResult result = it.next();
                        if (key.equals(result.BSSID)) {
                            ssid = result.SSID;
                            capabilities = result.capabilities;
                            HwHiLog.d(TAG, false, "ESS: SSID:%{private}s, capabilities:%{public}s", new Object[]{ssid, capabilities});
                            break;
                        }
                    }
                    for (ScanResult result2 : scanList) {
                        if (ssid.equals(result2.SSID) && capabilities.equals(result2.capabilities) && (count = count + 1) >= 3) {
                            isPublicESS = true;
                        }
                    }
                } catch (Exception e) {
                }
                if (isPublicESS) {
                    HwHiLog.d(TAG, false, "current network is public ESS, dont make default IP", new Object[0]);
                    return;
                }
                this.mDhcpResultCache.put(key, new DhcpResults(dhcpResults));
                HwHiLog.d(TAG, false, "make default IP configuration map, add rec for %{private}s", new Object[]{StringUtilEx.safeDisplayBssid(key)});
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public /* synthetic */ void lambda$makeHwDefaultIPTable$0$HwWifiStateMachine(List scanList) {
        scanList.addAll(wifiStateMachineUtils.getScanRequestProxy(this).getScanResults());
    }

    /* access modifiers changed from: protected */
    public boolean handleHwDefaultIPConfiguration() {
        boolean isCurrentNetworkWEPSecurity = false;
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (!(config == null || config.wepKeys == null)) {
            int idx = config.wepTxKeyIndex;
            isCurrentNetworkWEPSecurity = idx >= 0 && idx < config.wepKeys.length && config.wepKeys[idx] != null;
        }
        if (isCurrentNetworkWEPSecurity) {
            HwHiLog.d(TAG, false, "current network is WEP, dot set default IP configuration", new Object[0]);
            return false;
        }
        String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
        HwHiLog.d(TAG, false, "try to set default IP configuration for %{private}s", new Object[]{key});
        if (key == null) {
            HwHiLog.w(TAG, false, "handleHwDefaultIPConfiguration key is null!", new Object[0]);
            return false;
        }
        DhcpResults dhcpResult = this.mDhcpResultCache.get(key);
        if (dhcpResult == null) {
            HwHiLog.d(TAG, false, "set default IP configuration failed for no rec found", new Object[0]);
            return false;
        }
        DhcpResults dhcpResults = new DhcpResults(dhcpResult);
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        try {
            ifcg.setLinkAddress(dhcpResults.ipAddress);
            ifcg.setInterfaceUp();
            wifiStateMachineUtils.handleIPv4Success(this, dhcpResults);
            HwHiLog.d(TAG, false, "set default IP configuration succeeded", new Object[0]);
            return true;
        } catch (Exception e) {
            loge("set default IP configuration failed");
            return false;
        }
    }

    public DhcpResults getCachedDhcpResultsForCurrentConfig() {
        boolean isCurrentNetworkWEPSecurity = false;
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (!(config == null || config.wepKeys == null)) {
            int idx = config.wepTxKeyIndex;
            isCurrentNetworkWEPSecurity = idx >= 0 && idx < config.wepKeys.length && config.wepKeys[idx] != null;
        }
        if (isCurrentNetworkWEPSecurity) {
            HwHiLog.d(TAG, false, "current network is WEP, dot set default IP configuration", new Object[0]);
            return null;
        }
        String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
        int currRssi = wifiStateMachineUtils.getWifiInfo(this).getRssi();
        HwHiLog.d(TAG, false, "try to set default IP configuration currRssi = %{public}d", new Object[]{Integer.valueOf(currRssi)});
        if (key != null && currRssi >= -75) {
            return this.mDhcpResultCache.get(key);
        }
        HwHiLog.w(TAG, false, "getCachedDhcpResultsForCurrentConfig key is null!", new Object[0]);
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean hasMeteredHintForWi(Inet4Address ip) {
        boolean isIphone = false;
        boolean isWindowsPhone = false;
        if (SystemProperties.get("dhcp.wlan0.vendorInfo", "").startsWith("hostname:") && ip != null && ip.toString().startsWith("/172.20.10.")) {
            HwHiLog.d(TAG, false, "isiphone = true", new Object[0]);
            isIphone = true;
        }
        if (SystemProperties.get("dhcp.wlan0.domain", "").equals("mshome.net")) {
            HwHiLog.d(TAG, false, "isWindowsPhone = true", new Object[0]);
            isWindowsPhone = true;
        }
        if (isIphone || isWindowsPhone) {
            return true;
        }
        return false;
    }

    public int[] syncGetApChannelListFor5G(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_CHANNEL_LIST_5G);
        int[] channels = null;
        if (resultMsg == null) {
            return null;
        }
        if (resultMsg.obj != null) {
            channels = (int[]) resultMsg.obj;
        }
        resultMsg.recycle();
        return channels;
    }

    public void setLocalMacAddressFromMacfile() {
        String ret = "02:00:00:00:00:00";
        String oriMacString = GetUDIDNative.getWifiMacAddress();
        if (oriMacString == null || oriMacString.length() != 12) {
            HwHiLog.e(TAG, false, "MacString: %{private}s from UDIDNative is unvalid. Use default MAC address", new Object[]{oriMacString});
        } else {
            StringBuilder macBuilder = new StringBuilder();
            for (int i = 0; i < oriMacString.length(); i += 2) {
                macBuilder.append(oriMacString.substring(i, i + 2));
                if (i + 2 < oriMacString.length() - 1) {
                    macBuilder.append(COLON_SYMBOL);
                }
            }
            try {
                ret = MacAddress.fromString(macBuilder.toString()).toString();
            } catch (IllegalArgumentException e) {
                HwHiLog.e(TAG, false, "Formatted MacString is unvalid, message %{public}s Use default MAC address", new Object[]{e.getMessage()});
            }
        }
        HwHiLog.i(TAG, false, "setLocalMacAddress: %{private}s", new Object[]{StringUtilEx.safeDisplayBssid(ret)});
        wifiStateMachineUtils.getWifiInfo(this).setMacAddress(ret);
    }

    public void setVoWifiDetectMode(WifiDetectConfInfo info) {
        if (info != null && !this.mWifiDetectConfInfo.isEqual(info)) {
            this.mWifiDetectConfInfo = info;
            sendMessage(131772, info);
        }
    }

    /* access modifiers changed from: protected */
    public void processSetVoWifiDetectMode(Message msg) {
        WifiDetectConfInfo info = (WifiDetectConfInfo) msg.obj;
        HwHiLog.d(TAG, false, "%{public}s", new Object[]{"set VoWifi Detect Mode " + info});
        boolean ret = false;
        if (info != null) {
            if (info.mWifiDetectMode == 1) {
                IHwWifiNativeEx iHwWifiNativeEx = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
                ret = iHwWifiNativeEx.voWifiDetectSet("LOW_THRESHOLD " + info.mThreshold);
            } else if (info.mWifiDetectMode == 2) {
                IHwWifiNativeEx iHwWifiNativeEx2 = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
                ret = iHwWifiNativeEx2.voWifiDetectSet("HIGH_THRESHOLD " + info.mThreshold);
            } else {
                IHwWifiNativeEx iHwWifiNativeEx3 = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
                ret = iHwWifiNativeEx3.voWifiDetectSet("MODE " + info.mWifiDetectMode);
            }
            if (ret) {
                IHwWifiNativeEx iHwWifiNativeEx4 = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
                if (iHwWifiNativeEx4.voWifiDetectSet("TRIGGER_COUNT " + info.mEnvalueCount)) {
                    IHwWifiNativeEx iHwWifiNativeEx5 = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
                    ret = iHwWifiNativeEx5.voWifiDetectSet("MODE " + info.mWifiDetectMode);
                }
            }
        }
        if (ret) {
            HwHiLog.d(TAG, false, "%{public}s", new Object[]{"done set  VoWifi Detect Mode " + info});
            return;
        }
        HwHiLog.d(TAG, false, "%{public}s", new Object[]{"Failed to set VoWifi Detect Mode " + info});
    }

    public WifiDetectConfInfo getVoWifiDetectMode() {
        return this.mWifiDetectConfInfo;
    }

    public void setVoWifiDetectPeriod(int period) {
        if (period != this.mWifiDetectperiod) {
            this.mWifiDetectperiod = period;
            sendMessage(131773, period);
        }
    }

    /* access modifiers changed from: protected */
    public void processSetVoWifiDetectPeriod(Message msg) {
        int period = msg.arg1;
        HwHiLog.d(TAG, false, "set VoWifiDetect Period %{public}d", new Object[]{Integer.valueOf(period)});
        IHwWifiNativeEx iHwWifiNativeEx = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
        if (iHwWifiNativeEx.voWifiDetectSet("PERIOD " + period)) {
            HwHiLog.d(TAG, false, "done set set VoWifiDetect  Period %{public}d", new Object[]{Integer.valueOf(period)});
        } else {
            HwHiLog.d(TAG, false, "set VoWifiDetect Period %{public}d", new Object[]{Integer.valueOf(period)});
        }
    }

    public int getVoWifiDetectPeriod() {
        return this.mWifiDetectperiod;
    }

    public boolean syncGetSupportedVoWifiDetect(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(131774);
        if (resultMsg == null) {
            return false;
        }
        boolean supportedVoWifiDetect = resultMsg.arg1 == 0;
        resultMsg.recycle();
        HwHiLog.e(TAG, false, "syncGetSupportedVoWifiDetect %{public}s", new Object[]{String.valueOf(supportedVoWifiDetect)});
        return supportedVoWifiDetect;
    }

    /* access modifiers changed from: protected */
    public void processIsSupportVoWifiDetect(Message msg) {
        wifiStateMachineUtils.replyToMessage((ClientModeImpl) this, msg, msg.what, WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.isSupportVoWifiDetect() ? 0 : -1);
    }

    /* access modifiers changed from: protected */
    public void processStatistics(int event) {
        if (event == 0) {
            this.lastConnectTime = System.currentTimeMillis();
            Flog.bdReport(this.myContext, (int) HwQoEService.KOG_LATENCY_TIME_THRESHOLD);
        } else if (1 == event) {
            Flog.bdReport(this.myContext, 201);
            if (-1 != this.lastConnectTime) {
                JSONObject eventMsg = new JSONObject();
                try {
                    eventMsg.put("duration", (System.currentTimeMillis() - this.lastConnectTime) / 1000);
                } catch (JSONException e) {
                    HwHiLog.e(TAG, false, "processStatistics put error.%{public}s", new Object[]{e.getMessage()});
                }
                Flog.bdReport(this.myContext, 202, eventMsg);
                this.lastConnectTime = -1;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r1v2 byte[]: [D('bssid' java.lang.String), D('macBytes' byte[])] */
    public byte[] fetchWifiSignalInfoForVoWiFi() {
        int rssi;
        int rssi2;
        int frequency;
        String macStr;
        char c;
        byte[] macBytes;
        boolean z;
        String str;
        ByteBuffer rawByteBuffer = ByteBuffer.allocate(52);
        rawByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        WifiNative.SignalPollResult signalInfo = WifiInjector.getInstance().getWifiNative().signalPoll(wifiStateMachineUtils.getInterfaceName(this));
        if (signalInfo != null) {
            int rssi3 = signalInfo.currentRssi;
            int linkSpeed = signalInfo.txBitrate;
            rssi = rssi3;
            rssi2 = signalInfo.associationFrequency;
            frequency = linkSpeed;
        } else {
            rssi = -1;
            rssi2 = -1;
            frequency = -1;
        }
        RssiPacketCountInfo info = new RssiPacketCountInfo();
        WifiNative.TxPacketCounters txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters(wifiStateMachineUtils.getInterfaceName(this));
        long nativeTxGood = 0;
        long nativeTxBad = 0;
        if (txPacketCounters != null) {
            info.txgood = txPacketCounters.txSucceeded;
            nativeTxGood = (long) txPacketCounters.txSucceeded;
            info.txbad = txPacketCounters.txFailed;
            nativeTxBad = (long) txPacketCounters.txFailed;
        }
        rawByteBuffer.putInt(rssi);
        rawByteBuffer.putInt(0);
        rawByteBuffer.putInt((int) ((((double) info.txbad) / ((double) (info.txgood + info.txbad))) * 100.0d));
        int dpktcnt = info.txgood - this.mLastTxPktCnt;
        this.mLastTxPktCnt = info.txgood;
        rawByteBuffer.putInt(dpktcnt);
        rawByteBuffer.putInt(convertToAccessType(frequency, rssi2));
        rawByteBuffer.putInt(0);
        rawByteBuffer.putLong(nativeTxGood);
        rawByteBuffer.putLong(nativeTxBad);
        String bssid = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
        if (!TextUtils.isEmpty(bssid)) {
            macStr = bssid.replace(COLON_SYMBOL, "");
        } else {
            macStr = "ffffffffffff";
        }
        byte[] macBytes2 = new byte[16];
        try {
            macBytes = macStr.getBytes("US-ASCII");
            c = 0;
        } catch (UnsupportedEncodingException e) {
            c = 0;
            HwHiLog.e(TAG, false, "fetchWifiSignalInfoForVoWiFi failed", new Object[0]);
            macBytes = macBytes2;
        }
        rawByteBuffer.put(macBytes);
        Object[] objArr = new Object[8];
        objArr[c] = Integer.valueOf(rssi);
        objArr[1] = String.valueOf(nativeTxBad);
        objArr[2] = String.valueOf(nativeTxGood);
        objArr[3] = Integer.valueOf(dpktcnt);
        objArr[4] = Integer.valueOf(frequency);
        objArr[5] = Integer.valueOf(rssi2);
        objArr[6] = 0;
        if (macStr.length() >= 6) {
            z = false;
            str = macStr.substring(0, 6);
        } else {
            z = false;
            str = "ffffff";
        }
        objArr[7] = str;
        HwHiLog.d(TAG, z, "rssi=%{public}d, nativeTxBad=%{public}s, nativeTxGood=%{public}s, dpktcnt=%{public}d, linkSpeed=%{public}d, frequency=%{public}d, noise=%{public}d, mac=%{private}s", objArr);
        return rawByteBuffer.array();
    }

    private static int convertToAccessType(int linkSpeed, int frequency) {
        return 0;
    }

    private void recordPortalHistory(WifiConfiguration config) {
        boolean isHomeRouter = Settings.Global.getInt(this.myContext.getContentResolver(), FLAG_HOME_ROUTER, 0) == 1;
        String currentBssid = getCurrentBSSID();
        if (isHomeRouter || (!TextUtils.isEmpty(currentBssid) && ScanResultRecords.getDefault().getHiLinkAp(currentBssid) == 1)) {
            HwHiLog.d(TAG, false, "redirected by home router, clear the portal history", new Object[0]);
            config.noInternetAccess = true;
            config.validatedInternetAccess = false;
            config.portalNetwork = false;
            if (!this.mCurrNetworkHistoryInserted) {
                config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 0);
            }
            if (config.internetHistory != null && config.internetHistory.contains(String.valueOf(2))) {
                config.internetHistory = config.internetHistory.replace(String.valueOf(2), String.valueOf(0));
                return;
            }
            return;
        }
        config.portalNetwork = true;
        config.noInternetAccess = false;
        config.validatedInternetAccess = true;
        if (!this.mCurrNetworkHistoryInserted) {
            config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 2);
        }
    }

    private void closeInputStream(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSameApWifiEvaluateConfig(WifiConfiguration config) {
        if (config != null && config.networkId != -1) {
            WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
            wifiConfigManager.updateWifiConfigByWifiPro(config, false);
            wifiConfigManager.saveToStore(true);
        }
    }

    private void registerReceiverInWifiPro(Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                Object tempConfig = intent.getParcelableExtra("new_wifi_config");
                if (!(tempConfig instanceof WifiConfiguration)) {
                    HwHiLog.e(HwWifiStateMachine.TAG, false, "onReceive:tempConfig is not match the class", new Object[0]);
                    return;
                }
                WifiConfiguration newConfig = (WifiConfiguration) tempConfig;
                WifiConfiguration currentConfig = HwWifiStateMachine.this.getCurrentWifiConfiguration();
                if (newConfig != null && currentConfig != null) {
                    if (currentConfig.networkId != newConfig.networkId) {
                        HwWifiStateMachine.this.log("try to update the other bssid wifi internet type of same ap");
                        HwWifiStateMachine.this.updateSameApWifiEvaluateConfig(newConfig);
                        return;
                    }
                    WifiConfigManager wifiConfigManager = HwWifiStateMachine.wifiStateMachineUtils.getWifiConfigManager(HwWifiStateMachine.this);
                    if (intent.getIntExtra(HwWifiStateMachine.EXTRA_FLAG_HILINK_DETECT_NOT_PORTAL, -1) == 3) {
                        newConfig = currentConfig;
                    }
                    if (wifiConfigManager != null) {
                        HwHiLog.d(HwWifiStateMachine.TAG, false, "sync update network history, internetHistory = %{public}s", new Object[]{newConfig.internetHistory});
                        currentConfig.noInternetAccess = newConfig.noInternetAccess;
                        currentConfig.validatedInternetAccess = newConfig.validatedInternetAccess;
                        currentConfig.numNoInternetAccessReports = newConfig.numNoInternetAccessReports;
                        currentConfig.portalNetwork = newConfig.portalNetwork;
                        currentConfig.portalCheckStatus = newConfig.portalCheckStatus;
                        currentConfig.internetHistory = newConfig.internetHistory;
                        currentConfig.lastHasInternetTimestamp = newConfig.lastHasInternetTimestamp;
                        wifiConfigManager.updateInternetInfoByWifiPro(currentConfig);
                        wifiConfigManager.saveToStore(true);
                    }
                }
            }
        }, new IntentFilter("com.huawei.wifipro.ACTION_UPDATE_CONFIG_HISTORY"), "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER", null);
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int switchType = intent.getIntExtra(HwWifiProServiceManager.WIFI_HANDOVER_NETWORK_SWITCHTYPE, 1);
                WifiConfiguration changeConfig = (WifiConfiguration) intent.getParcelableExtra(HwWifiProServiceManager.WIFI_HANDOVER_NETWORK_WIFICONFIG);
                HwHiLog.d(HwWifiStateMachine.TAG, false, "ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER, switchType = %{public}d", new Object[]{Integer.valueOf(switchType)});
                if (!HwWifiStateMachine.this.mWifiSwitchOnGoing && changeConfig != null) {
                    HwWifiStateMachine.this.mHwWifiProServiceManager.uploadDisconnectedEvent(HwWifiStateMachine.EVENT_DUALBAND_SWITCH);
                    HwWifiStateMachine.this.updateDualBandSwitchEvent();
                    if (switchType == 1) {
                        HwWifiStateMachine.this.requestWifiSoftSwitch();
                        HwWifiStateMachine.this.startConnectToUserSelectNetwork(changeConfig.networkId, Binder.getCallingUid(), changeConfig.BSSID);
                    } else {
                        ScanResult roamScanResult = new ScanResult();
                        roamScanResult.BSSID = changeConfig.BSSID;
                        HwWifiStateMachine.this.startRoamToNetwork(changeConfig.networkId, roamScanResult);
                        HwHiLog.d(HwWifiStateMachine.TAG, false, "roamScanResult, call startRoamToNetwork", new Object[0]);
                    }
                    HwWifiStateMachine.this.saveCurrentConfig();
                    HwWifiStateMachine.this.mWifiSwitchOnGoing = true;
                }
            }
        }, new IntentFilter(HwWifiProServiceManager.ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER), HwWifiProServiceManager.WIFI_HANDOVER_RECV_PERMISSION, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDualBandSwitchEvent() {
        this.mHwWifiProServiceManager.updateDualBandSwitchEvent();
    }

    public void startWifi2WifiRequest() {
        saveCurrentConfig();
        this.mWifiSwitchOnGoing = true;
    }

    public boolean isWifiProEnabled() {
        return WifiProCommonUtils.isWifiProSwitchOn(this.myContext);
    }

    public int resetScoreByInetAccess(int score) {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (!isWifiProEnabled() || networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            return score;
        }
        return 0;
    }

    public void getConfiguredNetworks(Message message) {
        wifiStateMachineUtils.replyToMessage((ClientModeImpl) this, message, message.what, (Object) wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM));
    }

    public void saveConnectingNetwork(WifiConfiguration config, int netId, boolean autoJoin) {
        synchronized (this.selectConfigLock) {
            if (config == null && netId != -1) {
                config = wifiStateMachineUtils.getWifiConfigManager(this).getConfiguredNetwork(netId);
            }
            this.mSelectedConfig = config;
            if (this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted()) {
                this.mHwWifiProServiceManager.releaseBlackListBssid(config, autoJoin);
            }
        }
    }

    public void reportPortalNetworkStatus() {
        unwantedNetwork(3);
    }

    public boolean ignoreEnterConnectedState() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (!isWifiProEnabled() || networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            return false;
        }
        HwHiLog.d(TAG, false, "L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL, ignore to enter CONNECTED State", new Object[0]);
        return true;
    }

    public void wifiNetworkExplicitlyUnselected() {
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (wifiInfo != null) {
            wifiInfo.score = 40;
        }
        if (networkAgent != null) {
            networkAgent.sendNetworkScore(40);
        }
    }

    public void wifiNetworkExplicitlySelected() {
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (wifiInfo != null) {
            wifiInfo.score = 60;
        }
        if (networkAgent != null) {
            networkAgent.sendNetworkScore(60);
        }
    }

    public void handleConnectedInWifiPro() {
        String strDhcpResults;
        WifiConfiguration config;
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        handleWiFiConnectedByScanGenie(wifiConfigManager);
        if (this.mWifiSwitchOnGoing) {
            String bssid = null;
            String ssid = null;
            String configKey = null;
            synchronized (this.selectConfigLock) {
                if (this.mSelectedConfig != null) {
                    config = this.mSelectedConfig;
                } else {
                    config = wifiConfigManager.getConfiguredNetwork(wifiStateMachineUtils.getLastNetworkId(this));
                }
            }
            if (config != null) {
                bssid = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
                ssid = config.SSID;
                configKey = config.configKey();
            }
            sendWifiHandoverCompletedBroadcast(0, bssid, ssid, configKey);
        }
        int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        WifiConfiguration connectedConfig = wifiConfigManager.getConfiguredNetwork(lastNetworkId);
        if (connectedConfig != null) {
            if (connectedConfig.portalNetwork) {
                Bundle data = new Bundle();
                data.putBoolean("protalflag", connectedConfig.portalNetwork);
                this.mHwWifiCHRService.uploadDFTEvent(3, data);
            }
            for (WifiConfiguration config2 : wifiConfigManager.getSavedNetworks((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM)) {
                if (config2.getNetworkSelectionStatus().getConnectChoice() != null) {
                    wifiConfigManager.clearNetworkConnectChoice(config2.networkId);
                }
            }
            if (connectedConfig.portalCheckStatus == 1) {
                HwHiLog.d(TAG, false, "handleConnectedInWifiPro reset HAS_INTERNET to INTERNET_UNKNOWN!!", new Object[0]);
                connectedConfig.portalCheckStatus = 0;
            }
            if (connectedConfig.internetRecoveryStatus == 5) {
                HwHiLog.d(TAG, false, "handleConnectedInWifiPro reset RECOVERED to INTERNET_UNKNOWN!!", new Object[0]);
                connectedConfig.internetRecoveryStatus = 3;
            }
            wifiConfigManager.updateInternetInfoByWifiPro(connectedConfig);
            if (isWifiProEvaluatingAP() && !this.usingStaticIpConfig && connectedConfig.SSID != null && !connectedConfig.SSID.equals("<unknown ssid>") && (strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), WifiProCommonUtils.getCurrentCellId())) != null && connectedConfig.configKey() != null) {
                HwHiLog.d(TAG, false, "handleConnectedInWifiPro, lastDhcpResults = %{private}s, ssid = %{public}s", new Object[]{strDhcpResults, StringUtilEx.safeDisplaySsid(connectedConfig.SSID)});
                this.lastDhcps.put(connectedConfig.configKey(), strDhcpResults);
            }
        }
        if (!isWifiProEvaluatingAP()) {
            try {
                this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(false);
            } catch (Exception e) {
                HwHiLog.d(TAG, false, "wifi connected, Disable WifiproFirewall again", new Object[0]);
            }
        }
        synchronized (this.selectConfigLock) {
            this.mSelectedConfig = null;
        }
        this.usingStaticIpConfig = false;
        resetSelfCureCandidateLostCnt();
        wifiConfigManager.resetNetworkConnFailedInfo(lastNetworkId);
        wifiConfigManager.updateRssiDiscNonLocally(lastNetworkId, false, 0, 0);
        if (this.mWifiSoftSwitchRunning.get()) {
            HwHiLog.d(TAG, false, "wifi connected, reset mWifiSoftSwitchRunning and SCE state", new Object[0]);
            this.mWifiSoftSwitchRunning.set(false);
            updateNetworkId();
            WifiProCommonUtils.setWifiSelfCureStatus(0);
        }
        removeMessages(131897);
    }

    public void handleDisconnectedInWifiPro() {
        this.mHwWifiProServiceManager.handleWiFiDisconnected();
        this.mCurrNetworkHistoryInserted = false;
        int i = this.wifiConnectedBackgroundReason;
        if (i == 2 || i == 3) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
        }
        this.wifiConnectedBackgroundReason = 0;
        this.mHwWifiProServiceManager.notifyAutoConnectManagerDisconnected();
        synchronized (this.selectConfigLock) {
            this.mSelectedConfig = null;
        }
        this.usingStaticIpConfig = false;
        this.mRenewDhcpSelfCuring.set(false);
        this.mDelayWifiScoreBySelfCureOrSwitch = false;
    }

    public void handleUnwantedNetworkInWifiPro(WifiConfiguration config, int unwantedType) {
        if (config != null) {
            boolean updated = false;
            if (unwantedType == wifiStateMachineUtils.getUnwantedValidationFailed(this)) {
                if (this.mCurrNetworkHistoryInserted) {
                    HwHiLog.d(TAG, false, "don't update history for UNWANTED_VALIDATION_FAILED", new Object[0]);
                    return;
                }
                config.noInternetAccess = true;
                config.validatedInternetAccess = false;
                config.portalNetwork = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
                if (!this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 0);
                    this.mHwWifiProServiceManager.notifyFirstConnectProbeResult(599);
                    this.mCurrNetworkHistoryInserted = true;
                }
                updated = true;
            } else if (unwantedType == 3) {
                if (this.mCurrNetworkHistoryInserted) {
                    HwHiLog.d(TAG, false, "don't update history for NETWORK_STATUS_UNWANTED_PORTAL", new Object[0]);
                    return;
                }
                recordPortalHistory(config);
                if (!this.mCurrNetworkHistoryInserted) {
                    this.mHwWifiProServiceManager.notifyFirstConnectProbeResult(302);
                    this.mCurrNetworkHistoryInserted = true;
                    Bundle data = new Bundle();
                    data.putBoolean("protalflag", config.portalNetwork);
                    this.mHwWifiCHRService.uploadDFTEvent(3, data);
                }
                updated = true;
            }
            if (updated) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateInternetInfoByWifiPro(config);
                wifiConfigManager.saveToStore(false);
            }
            this.mDelayWifiScoreBySelfCureOrSwitch = false;
        }
    }

    public void handleValidNetworkInWifiPro(WifiConfiguration config) {
        if (config != null) {
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), -1);
            if (strDhcpResults != null) {
                config.lastDhcpResults = strDhcpResults;
                if (!isWifiProEvaluatingAP()) {
                    this.mHwWifiProServiceManager.notifyDhcpResultsInternetOk(strDhcpResults);
                }
            }
            if (!config.portalNetwork || !this.mCurrNetworkHistoryInserted) {
                config.noInternetAccess = false;
                if (!this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 1);
                    this.mHwWifiProServiceManager.notifyFirstConnectProbeResult(204);
                    this.mCurrNetworkHistoryInserted = true;
                } else {
                    config.internetHistory = WifiProCommonUtils.updateWifiConfigHistory(config.internetHistory, 1);
                }
                config.lastHasInternetTimestamp = System.currentTimeMillis();
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateInternetInfoByWifiPro(config);
                wifiConfigManager.saveToStore(false);
                this.mDelayWifiScoreBySelfCureOrSwitch = false;
            }
        }
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        HwWifiStateMachine.super.startRoamToNetwork(networkId, scanResult);
    }

    public void handleConnectFailedInWifiPro(int netId, int disableReason) {
        if (this.mWifiSwitchOnGoing && disableReason >= 2 && disableReason <= 4) {
            HwHiLog.d(TAG, false, "handleConnectFailedInWifiPro, netId = %{public}d, disableReason = %{public}d", new Object[]{Integer.valueOf(netId), Integer.valueOf(disableReason)});
            String failedBssid = null;
            String failedSsid = null;
            int status = -6;
            if (disableReason != 2) {
                status = -7;
            }
            synchronized (this.selectConfigLock) {
                if (this.mSelectedConfig != null) {
                    failedBssid = this.mSelectedConfig.BSSID;
                    failedSsid = this.mSelectedConfig.SSID;
                }
            }
            sendWifiHandoverCompletedBroadcast(status, failedBssid, failedSsid, null);
        }
    }

    private void sendWifiHandoverCompletedBroadcast(int statusCode, String bssid, String ssid, String configKey) {
        if (this.mWifiSwitchOnGoing) {
            this.mWifiSwitchOnGoing = false;
            synchronized (this.selectConfigLock) {
                this.mSelectedConfig = null;
            }
            Intent intent = new Intent();
            if (this.mHwWifiProServiceManager.getNetwoksHandoverType() == 1) {
                intent.setAction(HwWifiProServiceManager.ACTION_RESPONSE_WIFI_2_WIFI);
            } else {
                intent.setAction(HwWifiProServiceManager.ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER);
            }
            intent.putExtra(HwWifiProServiceManager.WIFI_HANDOVER_COMPLETED_STATUS, statusCode);
            intent.putExtra(HwWifiProServiceManager.WIFI_HANDOVER_NETWORK_BSSID, bssid);
            intent.putExtra(HwWifiProServiceManager.WIFI_HANDOVER_NETWORK_SSID, ssid);
            intent.putExtra(HwWifiProServiceManager.WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL, HwWifiProServiceManager.WIFI_HANDOVER_RECV_PERMISSION);
        }
    }

    public void updateWifiproWifiConfiguration(Message message) {
        if (message != null) {
            WifiConfiguration config = (WifiConfiguration) message.obj;
            boolean uiOnly = message.arg1 == 1;
            if (config != null && config.networkId != -1) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateWifiConfigByWifiPro(config, uiOnly);
                if (config.configKey() != null && config.wifiProNoInternetAccess) {
                    HwHiLog.d(TAG, false, "updateWifiproWifiConfiguration, noInternetReason = %{public}d, ssid = %{public}s", new Object[]{Integer.valueOf(config.wifiProNoInternetReason), StringUtilEx.safeDisplaySsid(config.SSID)});
                    this.lastDhcps.remove(config.configKey());
                }
                PowerManager powerManager = (PowerManager) this.myContext.getSystemService("power");
                if (powerManager == null || powerManager.isScreenOn()) {
                    wifiConfigManager.saveToStore(false);
                } else {
                    wifiConfigManager.saveToStore(true);
                }
            }
        }
    }

    public void notifyWifiConnFailedInfo(int netId, String bssid, int rssi, int reason, WifiConnectivityManager wcm) {
        ScanResult scanResult;
        if (netId == -1) {
            return;
        }
        if (reason == 3 || reason == 2 || reason == 4) {
            HwHiLog.d(TAG, false, "updateNetworkConnFailedInfo, netId = %{public}d, rssi = %{public}d, reason = %{public}d", new Object[]{Integer.valueOf(netId), Integer.valueOf(rssi), Integer.valueOf(reason)});
            WifiConfigManager configManager = wifiStateMachineUtils.getWifiConfigManager(this);
            WifiConfiguration selectedConfig = configManager.getConfiguredNetwork(netId);
            if (reason == 4) {
                configManager.updateNetworkConnFailedInfo(netId, rssi, reason);
            } else {
                if (!(selectedConfig == null || (scanResult = selectedConfig.getNetworkSelectionStatus().getCandidate()) == null)) {
                    rssi = scanResult.level;
                }
                configManager.updateNetworkConnFailedInfo(netId, rssi, reason);
            }
            if (this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted()) {
                this.mHwWifiProServiceManager.notifyWifiConnFailedInfo(selectedConfig, bssid, rssi, reason);
            }
        }
    }

    public void notifyNetworkUserConnect(boolean isUserConnect) {
        HwHiLog.d(TAG, false, "notifyNetworkUserConnect : %{public}s", new Object[]{String.valueOf(isUserConnect)});
        if (this.mHwWifiProServiceManager.isWifiProStateMachineStarted()) {
            this.mHwWifiProServiceManager.notifyNetworkUserConnect(isUserConnect);
        }
    }

    public void notifyApkChangeWifiStatus(boolean enable, String packageName) {
        HwHiLog.d(TAG, false, "notifyApkChangeWifiStatus enable= %{public}s, packageName =%{public}s", new Object[]{String.valueOf(enable), packageName});
        if (!this.mHwWifiProServiceManager.isWifiProStateMachineStarted()) {
            if (enable) {
                this.mHwWifiProServiceManager.notifyApkChangeWifiStatus(true, packageName);
            } else if (packageName.equals("com.android.systemui")) {
                this.mHwWifiProServiceManager.notifyApkChangeWifiStatus(false, packageName);
            }
        }
    }

    public void handleDisconnectedReason(WifiConfiguration config, int rssi, int local, int reason) {
        if (config != null && local == 0 && rssi != -127) {
            HwHiLog.d(TAG, false, "handleDisconnectedReason, rssi = %{public}d, reason = %{public}d, ssid = %{public}s", new Object[]{Integer.valueOf(rssi), Integer.valueOf(reason), StringUtilEx.safeDisplaySsid(config.SSID)});
            if (reason == 0 || reason == 3 || reason == 8) {
                wifiStateMachineUtils.getWifiConfigManager(this).updateRssiDiscNonLocally(config.networkId, true, rssi, System.currentTimeMillis());
            }
        }
    }

    public void setWiFiProScanResultList(List<ScanResult> list) {
        if (isWifiProEnabled()) {
            this.mHwWifiProServiceManager.setWiFiProScanResultList(list);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0034: APUT  (r6v0 java.lang.Object[]), (0 ??[int, short, byte, char]), (r7v0 java.lang.String) */
    public boolean isWifiProEvaluatingAP() {
        boolean z = true;
        if (this.wifiConnectedBackgroundReason == 2) {
            HwHiLog.d(TAG, false, "isWifiProEvaluatingAP, WIFI_BACKGROUND_PORTAL_CHECKING", new Object[0]);
            return true;
        }
        if (isWifiProEnabled()) {
            WifiConfiguration connectedConfig = wifiStateMachineUtils.getWifiConfigManager(this).getConfiguredNetwork(wifiStateMachineUtils.getLastNetworkId(this));
            Object[] objArr = new Object[1];
            objArr[0] = connectedConfig != null ? StringUtilEx.safeDisplaySsid(connectedConfig.SSID) : "null";
            HwHiLog.d(TAG, false, "isWifiProEvaluatingAP, connectedConfig = %{public}s", objArr);
            int i = this.wifiConnectedBackgroundReason;
            if (i == 2 || i == 3) {
                HwHiLog.d(TAG, false, "isWifiProEvaluatingAP, wifi connected at background matched, reason = %{public}d", new Object[]{Integer.valueOf(this.wifiConnectedBackgroundReason)});
                return true;
            } else if (connectedConfig != null) {
                HwHiLog.d(TAG, false, "isWifiProEvaluatingAP, isTempCreated = %{public}s, evaluating = %{public}s, wifiConnectedBackgroundReason = %{public}d", new Object[]{String.valueOf(connectedConfig.isTempCreated), String.valueOf(this.mHwWifiProServiceManager.isWifiEvaluating()), Integer.valueOf(this.wifiConnectedBackgroundReason)});
                if (this.mHwWifiProServiceManager.isWifiEvaluating() && connectedConfig.isTempCreated) {
                    this.wifiConnectedBackgroundReason = 1;
                    return true;
                }
            } else {
                synchronized (this.selectConfigLock) {
                    if (this.mSelectedConfig != null) {
                        HwHiLog.d(TAG, false, "isWifiProEvaluatingAP = %{public}s, mSelectedConfig isTempCreated = %{public}s", new Object[]{String.valueOf(this.mHwWifiProServiceManager.isWifiEvaluating()), String.valueOf(this.mSelectedConfig.isTempCreated)});
                        if (!this.mHwWifiProServiceManager.isWifiEvaluating() || !this.mSelectedConfig.isTempCreated) {
                            z = false;
                        }
                        return z;
                    }
                    HwHiLog.d(TAG, false, "==connectedConfig&mSelectedConfig are null, backgroundReason = %{public}d", new Object[]{Integer.valueOf(this.wifiConnectedBackgroundReason)});
                    if (!this.mHwWifiProServiceManager.isWifiEvaluating()) {
                        if (this.wifiConnectedBackgroundReason < 1) {
                            z = false;
                        }
                    }
                    return z;
                }
            }
        }
        return false;
    }

    private void updateScanDetail(ScanDetail scanDetail) {
        ScanResult sc = scanDetail.getScanResult();
        if (sc != null) {
            if (isWifiProEnabled()) {
                this.mHwWifiProServiceManager.updateScanDetailByWifiPro(sc);
                return;
            }
            sc.internetAccessType = 0;
            sc.networkQosLevel = 0;
            sc.networkQosScore = 0;
        }
    }

    public void updateScanDetailByWifiPro(List<ScanDetail> scanResults) {
        if (scanResults != null) {
            for (ScanDetail scanDetail : scanResults) {
                updateScanDetail(scanDetail);
            }
        }
    }

    public void tryUseStaticIpForFastConnecting(int lastNid) {
        if (isWifiProEnabled() && lastNid != -1 && isWifiProEvaluatingAP()) {
            synchronized (this.selectConfigLock) {
                if (!(this.mSelectedConfig == null || this.mSelectedConfig.configKey() == null)) {
                    WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                    this.mSelectedConfig.lastDhcpResults = this.lastDhcps.get(this.mSelectedConfig.configKey());
                    HwHiLog.d(TAG, false, "tryUseStaticIpForFastConnecting, lastDhcpResults = %{private}s", new Object[]{this.mSelectedConfig.lastDhcpResults});
                    if (this.mSelectedConfig.lastDhcpResults != null && this.mSelectedConfig.lastDhcpResults.length() > 0 && this.mSelectedConfig.getStaticIpConfiguration() == null && wifiConfigManager.tryUseStaticIpForFastConnecting(lastNid)) {
                        this.usingStaticIpConfig = true;
                    }
                }
            }
        }
    }

    public void updateNetworkConcurrently() {
        NetworkInfo.DetailedState state = NetworkInfo.DetailedState.CONNECTED;
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        wifiStateMachineUtils.getWifiConfigManager(this);
        int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (!(networkInfo.getExtraInfo() == null || wifiInfo.getSSID() == null || wifiInfo.getSSID().equals("<unknown ssid>"))) {
            networkInfo.setExtraInfo(wifiInfo.getSSID());
        }
        if (state != networkInfo.getDetailedState()) {
            networkInfo.setDetailedState(state, null, wifiInfo.getSSID());
            if (networkAgent != null) {
                networkAgent.updateNetworkConcurrently(networkInfo);
            }
        }
        HwHiLog.d(TAG, false, "updateNetworkConcurrently, lastNetworkId = %{public}d", new Object[]{Integer.valueOf(lastNetworkId)});
        int i = this.wifiConnectedBackgroundReason;
        if (i == 2 || i == 3) {
            this.mHwWifiProServiceManager.notifySelfCureWifiConnectedBackground();
            this.mHwWifiProServiceManager.notifyWifiConnectedBackground();
        }
    }

    public void triggerRoamingNetworkMonitor(boolean autoRoaming) {
        if (autoRoaming) {
            NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
            HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
            if (networkAgent != null) {
                networkAgent.triggerRoamingNetworkMonitor(networkInfo);
            }
        }
    }

    public boolean isDualbandScanning() {
        if (this.mHwWifiProServiceManager.isHwDualBandManagerStarted()) {
            return this.mHwWifiProServiceManager.isDualbandScanning();
        }
        return false;
    }

    public void triggerInvalidlinkNetworkMonitor() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (networkAgent != null) {
            networkAgent.triggerInvalidlinkNetworkMonitor(networkInfo);
        }
    }

    public void notifyWifiConnectedBackgroundReady() {
        int i = this.wifiConnectedBackgroundReason;
        if (i == 1) {
            HwHiLog.d(TAG, false, "notifyWifiConnectedBackgroundReady, ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY sent", new Object[0]);
            Intent intent = new Intent(HwWifiProServiceManager.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY);
            intent.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else if (i == 2) {
            HwHiLog.d(TAG, false, "notifyWifiConnectedBackgroundReady, WIFI_BACKGROUND_PORTAL_CHECKING sent", new Object[0]);
            Intent intent2 = new Intent(HwWifiProServiceManager.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND);
            intent2.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent2, UserHandle.ALL);
        } else if (i == 3) {
            HwHiLog.d(TAG, false, "notifyWifiConnectedBackgroundReady, ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND sent", new Object[0]);
            Intent intent3 = new Intent(HwWifiProServiceManager.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND);
            intent3.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent3, UserHandle.ALL);
        }
    }

    public void setWifiBackgroundReason(int status) {
        HwHiLog.d(TAG, false, "setWifiBackgroundReason, status = %{public}d", new Object[]{Integer.valueOf(status)});
        if (status == 0) {
            this.wifiConnectedBackgroundReason = 2;
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, true);
        } else if (status == 1) {
            this.wifiConnectedBackgroundReason = 0;
        } else if (status == 3) {
            this.wifiConnectedBackgroundReason = 3;
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, true);
        } else if (status == 5) {
            this.wifiConnectedBackgroundReason = 0;
        } else if (status == 6) {
            this.wifiConnectedBackgroundReason = 0;
        }
    }

    public void updateWifiBackgroudStatus(int msgType) {
        if (msgType == 2) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
            this.wifiConnectedBackgroundReason = 0;
        }
    }

    public boolean isWiFiProSwitchOnGoing() {
        return this.mWifiSwitchOnGoing;
    }

    public void resetWifiproEvaluateConfig(WifiInfo mWifiInfo, int netId) {
        if (isWifiProEvaluatingAP() && mWifiInfo != null && mWifiInfo.getNetworkId() == netId) {
            int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
            WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
            WifiConfiguration connectedConfig = wifiConfigManager.getConfiguredNetwork(lastNetworkId);
            synchronized (this.selectConfigLock) {
                if (connectedConfig == null) {
                    connectedConfig = this.mSelectedConfig;
                }
            }
            if (connectedConfig != null) {
                connectedConfig.isTempCreated = false;
                HwHiLog.d(TAG, false, "resetWifiproEvaluateConfig,ssid = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(connectedConfig.SSID)});
                wifiConfigManager.updateWifiConfigByWifiPro(connectedConfig, true);
            }
        }
    }

    public boolean ignoreNetworkStateChange(NetworkInfo networkInfo) {
        if (networkInfo == null || (!this.mHwWifiProServiceManager.isWifiProServiceReady() && !this.mHwWifiProServiceManager.isHwSelfCureServiceStarted())) {
            return false;
        }
        boolean isEvaluatingAP = isWifiProEvaluatingAP();
        if ((!isEvaluatingAP || !(networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING || networkInfo.getDetailedState() == NetworkInfo.DetailedState.SCANNING || networkInfo.getDetailedState() == NetworkInfo.DetailedState.AUTHENTICATING || networkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED)) && !selfCureIgnoreNetworkStateChange(networkInfo) && !softSwitchIgnoreNetworkStateChanged(networkInfo)) {
            if (isEvaluatingAP && networkInfo.getState() == NetworkInfo.State.DISCONNECTED && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED && this.mHwWifiProServiceManager.isWifiProStateMachineStarted()) {
                HwHiLog.d("WiFi_PRO", false, "notifyWifiDisconnected, DetailedState = %{public}s", new Object[]{String.valueOf(networkInfo.getDetailedState())});
                Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
                intent.putExtra("networkInfo", new NetworkInfo(networkInfo));
                this.mHwWifiProServiceManager.notifyWifiDisconnected(intent);
            }
            return false;
        }
        HwHiLog.d("WiFi_PRO", false, "ignoreNetworkStateChange, DetailedState = %{public}s", new Object[]{networkInfo.getDetailedState()});
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING && ((this.mWifiSoftSwitchRunning.get() || isWifiSelfCureByReset()) && !isMobileNetworkActive())) {
            this.mDelayWifiScoreBySelfCureOrSwitch = true;
        }
        closeWifi2WhenWifi1Disconnected(networkInfo);
        return true;
    }

    private void closeWifi2WhenWifi1Disconnected(NetworkInfo networkInfo) {
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            HwWifi2Manager hwWifi2Manager = HwWifi2Manager.getInstance();
            if (hwWifi2Manager == null) {
                HwHiLog.e(TAG, false, "closeWifi2WhenWifi1Disconncted: hwWifi2Manager is null", new Object[0]);
                return;
            }
            HwHiLog.i(TAG, false, "closeWifi2WhenWifi1Disconncted: disable wifi2", new Object[0]);
            hwWifi2Manager.setWifi2Enable(false);
        }
    }

    public boolean selfCureIgnoreNetworkStateChange(NetworkInfo networkInfo) {
        if ((!isWifiSelfCuring() || !this.mWifiBackgroundConnected) && ((!isWifiSelfCuring() || this.mWifiBackgroundConnected || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) && (!isRenewDhcpSelfCuring() || networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED))) {
            return false;
        }
        HwHiLog.d("HwSelfCureEngine", false, "selfCureIgnoreNetworkStateChange, detailedState = %{public}s", new Object[]{networkInfo.getDetailedState()});
        return true;
    }

    private boolean selfCureIgnoreSuppStateChange(SupplicantState state) {
        if (!isWifiSelfCuring() && !isRenewDhcpSelfCuring() && !this.mWifiSoftSwitchRunning.get()) {
            return false;
        }
        if (state == SupplicantState.ASSOCIATING && ((this.mWifiSoftSwitchRunning.get() || isWifiSelfCureByReset()) && !isMobileNetworkActive())) {
            this.mDelayWifiScoreBySelfCureOrSwitch = true;
        }
        return true;
    }

    private boolean isWifiSelfCureByReset() {
        return 102 == WifiProCommonUtils.getSelfCuringState();
    }

    private boolean isMobileNetworkActive() {
        NetworkInfo activeNetInfo;
        if (this.mConnMgr == null) {
            this.mConnMgr = (ConnectivityManager) this.myContext.getSystemService("connectivity");
        }
        ConnectivityManager connectivityManager = this.mConnMgr;
        if (connectivityManager == null || (activeNetInfo = connectivityManager.getActiveNetworkInfo()) == null || activeNetInfo.getType() != 0) {
            return false;
        }
        return true;
    }

    public boolean softSwitchIgnoreNetworkStateChanged(NetworkInfo networkInfo) {
        if (!this.mWifiSoftSwitchRunning.get() || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            return false;
        }
        HwHiLog.d("WIFIPRO", false, "softSwitchIgnoreNetworkStateChanged, detailedState = %{public}d", new Object[]{networkInfo.getDetailedState()});
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            this.mHwWifiProServiceManager.notifySelfCureWifiDisconnected();
            this.mHwWifiProServiceManager.notifyWifiMonitorDisconnected();
            this.mHwWifiProServiceManager.notifyAutoConnectManagerDisconnected();
            HwMSSHandler.getInstance().notifyWifiDisconnected();
        }
        return true;
    }

    public boolean ignoreSupplicantStateChange(SupplicantState state) {
        if (state == SupplicantState.ASSOCIATING) {
            this.mTimeStampScanControlForAssoc = SystemClock.elapsedRealtime();
            this.mTimeOutScanControlForAssoc = TIMEOUT_CONTROL_SCAN_ASSOCIATING;
        } else if (state == SupplicantState.ASSOCIATED) {
            this.mTimeStampScanControlForAssoc = SystemClock.elapsedRealtime();
            this.mTimeOutScanControlForAssoc = TIMEOUT_CONTROL_SCAN_ASSOCIATED;
        } else if (!(state == SupplicantState.FOUR_WAY_HANDSHAKE || state == SupplicantState.AUTHENTICATING || state == SupplicantState.GROUP_HANDSHAKE)) {
            this.mTimeStampScanControlForAssoc = SystemClock.elapsedRealtime();
            this.mTimeOutScanControlForAssoc = 0;
        }
        HwHiLog.d(TAG, false, "update the timeout parameter for the scan control, timeout = %{public}s, state = %{public}s", new Object[]{String.valueOf(this.mTimeOutScanControlForAssoc), state});
        if ((!isWifiProEvaluatingAP() || (state != SupplicantState.SCANNING && state != SupplicantState.ASSOCIATING && state != SupplicantState.AUTHENTICATING && state != SupplicantState.ASSOCIATED && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.AUTHENTICATING && state != SupplicantState.GROUP_HANDSHAKE && state != SupplicantState.COMPLETED)) && !selfCureIgnoreSuppStateChange(state)) {
            return false;
        }
        HwHiLog.d("WiFi_PRO", false, "ignoreSupplicantStateChange, state = %{public}s", new Object[]{state});
        return true;
    }

    private boolean disallowWifiScanForConnection() {
        long now = SystemClock.elapsedRealtime();
        long j = this.mTimeStampScanControlForAssoc;
        if (now - j <= this.mTimeOutScanControlForAssoc) {
            HwHiLog.d(TAG, false, "disallowWifiScanForConnection, mTimeStampScanControlForAssoc = %{public}s mTimeOutScanControlForAssoc = %{public}s", new Object[]{String.valueOf(j), String.valueOf(this.mTimeOutScanControlForAssoc)});
            return true;
        } else if (!ClientModeImpl.ObtainingIpState.class.equals(getCurrentState().getClass())) {
            this.mTimeLastCtrlScanDuringObtainingIp = 0;
            return false;
        } else {
            long j2 = this.mTimeLastCtrlScanDuringObtainingIp;
            if (j2 == 0) {
                this.mTimeLastCtrlScanDuringObtainingIp = now;
                HwHiLog.d(TAG, false, "disallowWifiScanForConnection, mTimeLastCtrlScanDuringObtainingIp = %{public}s", new Object[]{String.valueOf(this.mTimeLastCtrlScanDuringObtainingIp)});
                return true;
            } else if (now - j2 > TIMEOUT_CONTROL_SCAN_ASSOCIATED) {
                return false;
            } else {
                HwHiLog.d(TAG, false, "disallowWifiScanForConnection, mTimeLastCtrlScanDuringObtainingIp = %{public}s", new Object[]{String.valueOf(j2)});
                return true;
            }
        }
    }

    private void resetWifiProManualConnect() {
        Settings.System.putInt(this.myContext.getContentResolver(), "wifipro_manual_connect_ap", 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getAppUid(String processName) {
        try {
            ApplicationInfo ai = this.myContext.getPackageManager().getApplicationInfo(processName, 1);
            if (ai != null) {
                return ai.uid;
            }
            return 1000;
        } catch (PackageManager.NameNotFoundException e) {
            HwHiLog.e(TAG, false, "getAppUid failed", new Object[0]);
            return 1000;
        }
    }

    private void registerForWifiEvaluateChanges() {
        this.myContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(WIFI_EVALUATE_TAG), false, new ContentObserver(null) {
            /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                int tag = Settings.Secure.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.WIFI_EVALUATE_TAG, 0);
                if (HwWifiStateMachine.this.mBQEUid == 1000) {
                    HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                    hwWifiStateMachine.mBQEUid = hwWifiStateMachine.getAppUid("com.huawei.wifiprobqeservice");
                }
                HwWifiStateMachine hwWifiStateMachine2 = HwWifiStateMachine.this;
                hwWifiStateMachine2.logd("**wifipro tag is chenge, setWifiproFirewallEnable**,tag =" + tag);
                if (tag == 1) {
                    try {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(true);
                        if (HwWifiStateMachine.this.mBQEUid != 1000) {
                            HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(HwWifiStateMachine.this.mBQEUid);
                        }
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(1000);
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallDrop();
                    } catch (Exception e) {
                        HwWifiStateMachine.this.loge("**setWifiproCmdEnable Error");
                    }
                } else if (tag == 0) {
                    try {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(false);
                    } catch (Exception e2) {
                        HwWifiStateMachine.this.loge("**Disable WifiproCmdEnable Error");
                    }
                }
            }
        });
    }

    private void registerForPasspointChanges() {
        this.myContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(DBKEY_HOTSPOT20_VALUE), false, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiConfiguration config;
                if (Settings.Global.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.DBKEY_HOTSPOT20_VALUE, 1) == 0 && (config = HwWifiStateMachine.this.getCurrentWifiConfiguration()) != null && config.isPasspoint()) {
                    HwWifiStateMachine.this.disconnectCommand();
                }
            }
        });
    }

    private void handleWiFiConnectedByScanGenie(WifiConfigManager wifiConfigManager) {
        HwHiLog.d(TAG, false, "handleWiFiConnectedByScanGenie", new Object[0]);
        if (HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.myContext)) {
            HwHiLog.d(TAG, false, "this is mobile ap,ScanGenie ignor it", new Object[0]);
            return;
        }
        WifiConfiguration currentWifiConfig = getCurrentWifiConfiguration();
        if (currentWifiConfig != null && !currentWifiConfig.isTempCreated) {
            HwHiLog.d(TAG, false, "mWifiScanGenieController.handleWiFiConnected", new Object[0]);
            this.mHwWifiProServiceManager.handleWiFiConnected(currentWifiConfig, false);
        }
    }

    public void notifyWifiScanResultsAvailable(boolean success) {
        this.mHwWifiProServiceManager.notifySelfCureWifiScanResultsAvailable(success);
    }

    public void notifyWifiRoamingStarted() {
        this.mHwWifiProServiceManager.notifyWifiRoamingStarted();
    }

    public void notifyWifiRoamingCompleted(String newBssid) {
        if (newBssid != null) {
            HwQoEService mHwQoEService = HwQoEService.getInstance();
            if (mHwQoEService != null) {
                mHwQoEService.notifyNetworkRoaming();
            }
            this.mHwWifiProServiceManager.notifySelfCureWifiRoamingCompleted(newBssid);
            this.mHwWifiProServiceManager.notifyWifiConnectivityRoamingCompleted();
            this.mHwWifiProServiceManager.notifyNetworkRoamingCompleted(newBssid);
            DcMonitor dcMonitor = DcMonitor.getInstance();
            if (dcMonitor != null) {
                dcMonitor.notifyNetworkRoamingCompleted(newBssid);
            }
            FsArbitration fsArbitration = FsArbitration.getInstance();
            if (fsArbitration != null) {
                fsArbitration.notifyNetworkRoamingCompleted();
            }
        }
    }

    public void notifyEnableSameNetworkId(int netId) {
        if (this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted()) {
            this.mHwWifiProServiceManager.notifyEnableSameNetworkId(netId);
        }
    }

    public boolean isWlanSettingsActivity() {
        ComponentName cn;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (runningTaskInfos == null || runningTaskInfos.isEmpty() || (cn = runningTaskInfos.get(0).topActivity) == null || cn.getClassName() == null || !cn.getClassName().startsWith(WifiCommonUtils.getSettingActivityName())) {
            return false;
        }
        return true;
    }

    public void requestUpdateDnsServers(ArrayList<String> dnses) {
        if (dnses != null && !dnses.isEmpty()) {
            sendMessage(131882, dnses);
        }
    }

    public void sendUpdateDnsServersRequest(Message msg, LinkProperties lp) {
        if (!(msg == null || msg.obj == null)) {
            ArrayList<String> dnsesStr = (ArrayList) msg.obj;
            ArrayList<InetAddress> dnses = new ArrayList<>();
            for (int i = 0; i < dnsesStr.size(); i++) {
                try {
                    dnses.add(Inet4Address.getByName(dnsesStr.get(i)));
                } catch (Exception e) {
                    HwHiLog.e(TAG, false, "sendUpdateDnsServersRequest failed", new Object[0]);
                }
            }
            if (!dnses.isEmpty()) {
                HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
                LinkProperties newLp = new LinkProperties(lp);
                newLp.setDnsServers(dnses);
                logd("sendUpdateDnsServersRequest, renew dns server newLp is: " + newLp);
                if (networkAgent != null) {
                    networkAgent.sendLinkProperties(newLp);
                }
            }
        }
    }

    public void requestRenewDhcp() {
        this.mRenewDhcpSelfCuring.set(true);
        sendMessage(131883);
    }

    public void handleInvalidIpAddr() {
        sendMessage(131895);
    }

    public void startSelfCureReconnect() {
        resetSelfCureParam();
        if (saveCurrentConfig()) {
            this.mWifiSelfCuring.set(true);
            resetSelfCureCandidateLostCnt();
            WifiProCommonUtils.setWifiSelfCureStatus(103);
            checkWifiBackgroundStatus();
            setSelfCureWifiTimeOut(5);
        }
    }

    public void handleNoInternetIp() {
        sendMessage(131898);
    }

    public void setForceDhcpDiscovery(IpClientManager ipClient) {
        if (ipClient == null) {
            return;
        }
        if (this.mRenewDhcpSelfCuring.get() || this.mWifiSelfCuring.get()) {
            logd("setForceDhcpDiscovery, force dhcp discovery for sce background cure internet.");
            ipClient.setForceDhcpDiscovery();
        }
    }

    public void resetIpConfigStatus() {
        this.mRenewDhcpSelfCuring.set(false);
    }

    public boolean isRenewDhcpSelfCuring() {
        return this.mRenewDhcpSelfCuring.get();
    }

    public void requestUseStaticIpConfig(StaticIpConfiguration staticIpConfig) {
        sendMessage(131884, staticIpConfig);
    }

    public void handleStaticIpConfig(IpClientManager ipClient, WifiNative wifiNative, StaticIpConfiguration config) {
        if (ipClient != null && wifiNative != null && config != null) {
            ipClient.startProvisioning(new ProvisioningConfiguration.Builder().withStaticConfiguration(config).withoutIpReachabilityMonitor().withApfCapabilities(wifiNative.getApfCapabilities(wifiStateMachineUtils.getInterfaceName(this))).build());
        }
    }

    public void notifyIpConfigCompleted() {
        this.mHwWifiProServiceManager.notifySelfCureIpConfigCompleted();
        HwArbitrationStateMachine.getInstance(this.myContext).notifyIpConfigCompleted();
    }

    public int getWifiApTypeFromMpLink() {
        return HwMpLinkContentAware.getInstance(this.myContext).getWifiApTypeAndSendMsg(getCurrentWifiConfiguration());
    }

    public boolean notifyIpConfigLostAndFixedBySce(WifiConfiguration config) {
        return this.mHwWifiProServiceManager.notifySelfCureIpConfigLostAndHandle(config);
    }

    public void requestResetWifi() {
        sendMessage(131887);
    }

    public void requestReassocLink(int useWithReassocType) {
        sendMessage(131886, useWithReassocType);
    }

    public void startSelfCureWifiReset() {
        resetSelfCureParam();
        if (!saveCurrentConfig()) {
            stopSelfCureDelay(1, 0);
            return;
        }
        this.mHwWifiProServiceManager.uploadDisconnectedEvent(EVENT_CHIP_RECOVERY);
        this.mWifiSelfCuring.set(true);
        resetSelfCureCandidateLostCnt();
        WifiProCommonUtils.setWifiSelfCureStatus(102);
        checkWifiBackgroundStatus();
        selfCureWifiDisable();
    }

    public void startSelfCureWifiReassoc(int useWithReassocType) {
        resetSelfCureParam();
        if (!saveCurrentConfig()) {
            stopSelfCureDelay(1, 0);
            return;
        }
        this.mHwWifiProServiceManager.uploadDisconnectedEvent(EVENT_REASSOCIATION_RECOVERY);
        this.mWifiSelfCuring.set(true);
        resetSelfCureCandidateLostCnt();
        WifiProCommonUtils.setWifiSelfCureStatus(101);
        checkWifiBackgroundStatus();
        if (useWithReassocType == 1) {
            reassociateCommand();
            this.mIsReassocUseWithFactoryMacAddress = false;
        } else if (useWithReassocType == 2) {
            this.mIsReassocUseWithFactoryMacAddress = true;
            startReassocWithMac();
        } else if (useWithReassocType != 3) {
            HwHiLog.w(TAG, false, "useWithReassocType is invalid:%{public}d", new Object[]{Integer.valueOf(useWithReassocType)});
        } else {
            this.mIsReassocUseWithFactoryMacAddress = false;
            startReassocWithMac();
        }
        setSelfCureWifiTimeOut(4);
    }

    public void requestWifiSoftSwitch() {
        this.mWifiSoftSwitchRunning.set(true);
        WifiProCommonUtils.setWifiSelfCureStatus((int) HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET);
        sendMessageDelayed(131897, -4, 0, 15000);
    }

    private void startReassocWithMac() {
        WifiManager wifiManager;
        WifiInfo wifiInfo;
        Context context = this.myContext;
        if (context != null && (wifiManager = (WifiManager) context.getSystemService("wifi")) != null && (wifiInfo = wifiManager.getConnectionInfo()) != null) {
            startConnectToUserSelectNetwork(this.mCurrentConfigNetId, HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM, wifiInfo.getBSSID());
        }
    }

    private void handleAssocRejectedWithFacMac(int reasonCode) {
        if (isWifiSelfCuring() && this.mIsReassocUseWithFactoryMacAddress) {
            if (reasonCode == 16 || reasonCode == 17 || reasonCode == 1) {
                stopSelfCureDelay(-5, 0);
                this.mIsReassocUseWithFactoryMacAddress = false;
                startReassocWithMac();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean saveCurrentConfig() {
        WifiConfiguration currentConfiguration = getCurrentWifiConfiguration();
        if (currentConfiguration == null) {
            stopSelfCureDelay(1, 0);
            return false;
        }
        this.mCurrentConfigurationKey = currentConfiguration.configKey();
        this.mCurrentConfigNetId = currentConfiguration.networkId;
        logd("saveCurrentConfig >> configKey=" + this.mCurrentConfigurationKey + " netid=" + this.mCurrentConfigNetId);
        return true;
    }

    private void updateNetworkId() {
        WifiConfiguration wifiConfig;
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        if (!(wifiConfigManager == null || (wifiConfig = wifiConfigManager.getConfiguredNetwork(this.mCurrentConfigurationKey)) == null)) {
            this.mCurrentConfigNetId = wifiConfig.networkId;
        }
        logd("updateNetworkId >> configKey=" + this.mCurrentConfigurationKey + " netid=" + this.mCurrentConfigNetId);
    }

    private void resetSelfCureParam() {
        logd("ENTER: resetSelfCureParam");
        this.mWifiSelfCuring.set(false);
        WifiProCommonUtils.setWifiSelfCureStatus(0);
        this.mWifiAlwaysOnBeforeCure = false;
        this.mWifiBackgroundConnected = false;
        this.mCurrentConfigurationKey = null;
        this.mCurrentConfigNetId = -1;
        this.mSelfCureWifiLastState = -1;
        this.mUserCloseWifiWhenSelfCure = false;
        this.mSelfCureNetworkLastState = NetworkInfo.DetailedState.IDLE;
        this.mSelfCureWifiConnectRetry = 0;
        removeMessages(131888);
        removeMessages(131889);
        removeMessages(131890);
        removeMessages(131891);
    }

    private void checkWifiBackgroundStatus() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (networkInfo != null) {
            logd("checkWifiBackgroundStatus: detailstate=" + networkInfo.getDetailedState() + " isMobileDataInactive=" + WifiProCommonUtils.isMobileDataInactive(this.myContext));
        }
        setWifiBackgroundStatus(networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK && !WifiProCommonUtils.isMobileDataInactive(this.myContext));
    }

    public void setWifiBackgroundStatus(boolean background) {
        if (isWifiSelfCuring()) {
            logd("setWifiBackgroundStatus: " + background + " wifiBackgroundConnected=" + this.mWifiBackgroundConnected);
            this.mWifiBackgroundConnected = background;
        }
    }

    private void selfCureWifiDisable() {
        this.mHwWifiProServiceManager.requestChangeWifiStatus(false);
        setSelfCureWifiTimeOut(1);
    }

    private void selfCureWifiEnable() {
        this.mHwWifiProServiceManager.requestChangeWifiStatus(true);
        setSelfCureWifiTimeOut(2);
    }

    private void setSelfCureWifiTimeOut(int wifiSelfCureState) {
        int i;
        this.mWifiSelfCureState = wifiSelfCureState;
        int i2 = this.mWifiSelfCureState;
        if (i2 == 1) {
            logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_OFF_TIMEOUT 2000");
            sendMessageDelayed(131888, -1, 0, TIMEOUT_CONTROL_SCAN_ASSOCIATING);
        } else if (i2 == 2) {
            logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_ON_TIMEOUT 3000");
            sendMessageDelayed(131889, -1, 0, 3000);
        } else if (i2 == 3) {
            if (((PowerManager) this.myContext.getSystemService("power")).isScreenOn()) {
                i = 15000;
            } else {
                i = 30000;
            }
            long delayedMs = (long) i;
            logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_CONNECT_TIMEOUT " + delayedMs);
            sendMessageDelayed(131890, -1, 0, delayedMs);
        } else if (i2 == 4) {
            logd("selfCureWifiResetCheck send delay messgae SCE_WIFI_REASSOC_STATE 12000");
            sendMessageDelayed(131891, -1, 0, 12000);
        } else if (i2 == 5) {
            logd("selfCureWifiResetCheck send delay messgae SCE_WIFI_RECONNECT_STATE 15000");
            sendMessageDelayed(131896, -1, 0, 15000);
        }
    }

    public String selectBestCandidate(WifiConfiguration config) {
        ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
        List<ScanResult> cachedScanResults = null;
        if (scanProxy != null) {
            synchronized (scanProxy) {
                cachedScanResults = scanProxy.getScanResults();
            }
        }
        if (cachedScanResults == null) {
            return HwABSWiFiHandler.SUPPLICANT_BSSID_ANY;
        }
        ScanResult candidate = null;
        for (ScanResult result : cachedScanResults) {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            sb.append(result.SSID);
            sb.append("\"");
            if ((config.SSID.equals(sb.toString()) && WifiProCommonUtils.isSameEncryptType(result.capabilities, config.configKey())) && !this.mHwWifiProServiceManager.isBssidMatchedBlacklist(result.BSSID)) {
                if (candidate != null) {
                    int newScore = WifiProCommonUtils.calculateScore(result);
                    int currentScore = WifiProCommonUtils.calculateScore(candidate);
                    logd("result bssid = " + StringUtilEx.safeDisplayBssid(result.BSSID) + ", rssi = " + result.level + ", WifiCategory = " + result.supportedWifiCategory + ", currentScore = " + currentScore + ", newScore = " + newScore);
                    if (newScore > currentScore || (newScore == currentScore && result.level > candidate.level)) {
                        candidate = result;
                    }
                } else {
                    candidate = result;
                }
            }
        }
        return candidate == null ? HwABSWiFiHandler.SUPPLICANT_BSSID_ANY : candidate.BSSID;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ec, code lost:
        if (r17 == 101) goto L_0x00ee;
     */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01fd  */
    public boolean checkSelfCureWifiResult(int event) {
        int i;
        int wifiState = syncGetWifiState();
        if (wifiState == 2) {
            this.mWifiEnabledTimeStamp = System.currentTimeMillis();
        }
        int i2 = -1;
        if (wifiState == 0) {
            if (!isWifiSelfCuring() && wifiStateMachineUtils.getScreenOn(this)) {
                WifiConfigManager wifiConfigMgr = wifiStateMachineUtils.getWifiConfigManager(this);
                for (WifiConfiguration config : wifiConfigMgr.getConfiguredNetworks()) {
                    if (config.portalCheckStatus != 0) {
                        config.portalCheckStatus = 0;
                        wifiConfigMgr.updateInternetInfoByWifiPro(config);
                    }
                }
                List<ScanResult> scanResults = new ArrayList<>();
                ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
                if (scanProxy != null) {
                    synchronized (scanProxy) {
                        for (ScanResult result : scanProxy.getScanResults()) {
                            scanResults.add(new ScanResult(result));
                        }
                    }
                    if (scanResults.size() > 0) {
                        setWiFiProScanResultList(scanResults);
                    }
                }
            }
            if (isWifiSelfCuring() && !this.mUserCloseWifiWhenSelfCure && !isWifiSelfCureByReset()) {
                logd("checkSelfCureWifiResult, user close wifi during reassoc or reconnect self-cure going.");
                this.mUserCloseWifiWhenSelfCure = true;
                removeMessages(131891);
                removeMessages(131896);
                exitWifiSelfCure(1, -1);
                return false;
            }
        }
        if (!this.mWifiSoftSwitchRunning.get() || wifiState != 0) {
            if (isWifiSelfCuring() && !this.mUserCloseWifiWhenSelfCure) {
                if (wifiState != 4) {
                    boolean ret = true;
                    if (this.mSelfCureWifiLastState <= wifiState || this.mWifiSelfCureState == 1) {
                        if (!(wifiState == 0 && this.mSelfCureWifiLastState == wifiState)) {
                            this.mSelfCureWifiLastState = wifiState;
                            i = this.mWifiSelfCureState;
                            if (i == 1) {
                                if (i != 2) {
                                    if (i == 3) {
                                        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                                        if ((!isDuplicateNetworkState(networkInfo) && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) || networkInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                                            logd("wifi connect > CMD_SCE_WIFI_CONNECT_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                                            removeMessages(131890);
                                            boolean connSucc = isWifiConnectToSameAP();
                                            if (connSucc) {
                                                i2 = 0;
                                            }
                                            notifySelfCureComplete(connSucc, i2);
                                        }
                                    } else if (i == 4 || i == 5) {
                                        NetworkInfo networkInfo2 = wifiStateMachineUtils.getNetworkInfo(this);
                                        if ((!isDuplicateNetworkState(networkInfo2) && networkInfo2.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) || networkInfo2.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                                            logd("wifi reassociate/reconnect > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo2.getDetailedState());
                                            removeMessages(131891);
                                            removeMessages(131896);
                                            boolean connSucc2 = isWifiConnectToSameAP();
                                            if (connSucc2) {
                                                i2 = 0;
                                            }
                                            notifySelfCureComplete(connSucc2, i2);
                                        } else if (!isDuplicateNetworkState(networkInfo2) && networkInfo2.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                                            logd("wifi reassociate/reconnect > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo2.getDetailedState());
                                            removeMessages(131891);
                                            removeMessages(131896);
                                            notifySelfCureComplete(false, -1);
                                        }
                                    }
                                } else if (wifiState == 3) {
                                    removeMessages(131889);
                                    logd("wifi enabled > CMD_SCE_WIFI_ON_TIMEOUT msg removed");
                                    notifySelfCureComplete(true, 0);
                                }
                            } else if (wifiState == 1) {
                                removeMessages(131888);
                                logd("wifi disabled > CMD_SCE_WIFI_OFF_TIMEOUT msg removed");
                                notifySelfCureComplete(true, 0);
                            }
                            return ret;
                        }
                    }
                    logd("last state =" + this.mSelfCureWifiLastState + ", current state=" + wifiState + ", user may toggle wifi! stop selfcure");
                    exitWifiSelfCure(1, -1);
                    this.mUserCloseWifiWhenSelfCure = true;
                    ret = false;
                    this.mSelfCureWifiLastState = wifiState;
                    i = this.mWifiSelfCureState;
                    if (i == 1) {
                    }
                    return ret;
                }
            }
            logd("userCloseWifiWhenSelfCure = " + this.mUserCloseWifiWhenSelfCure + ", wifiState = " + wifiState);
            return false;
        }
        logd("checkSelfCureWifiResult, WifiSoftSwitchRunning, WIFI_STATE_DISABLING.");
        removeMessages(131897);
        sendMessage(131897, -4, 0);
        return false;
    }

    private boolean isDuplicateNetworkState(NetworkInfo networkInfo) {
        boolean ret = false;
        if (networkInfo == null) {
            return false;
        }
        if (this.mSelfCureNetworkLastState == networkInfo.getDetailedState()) {
            HwHiLog.d(TAG, false, "duplicate network state non-change %{public}s", new Object[]{String.valueOf(networkInfo.getDetailedState())});
            ret = true;
        }
        this.mSelfCureNetworkLastState = networkInfo.getDetailedState();
        return ret;
    }

    private boolean isWifiConnectToSameAP() {
        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (this.mCurrentConfigurationKey == null || wifiConfig == null || wifiConfig.configKey() == null || !this.mCurrentConfigurationKey.equals(wifiConfig.configKey())) {
            return false;
        }
        return true;
    }

    public boolean isBssidDisabled(String bssid) {
        return false;
    }

    public void resetSelfCureCandidateLostCnt() {
        WifiInjector.getInstance().getSavedNetworkEvaluator().resetSelfCureCandidateLostCnt();
    }

    public boolean isWifiSelfCuring() {
        return this.mWifiSelfCuring.get();
    }

    public int getSelfCureNetworkId() {
        return this.mCurrentConfigNetId;
    }

    public long getWifiEnabledTimeStamp() {
        return this.mWifiEnabledTimeStamp;
    }

    public boolean reportWifiScoreDelayed() {
        return this.mDelayWifiScoreBySelfCureOrSwitch;
    }

    public void notifySelfCureComplete(boolean success, int reasonCode) {
        if (!success && reasonCode == -4) {
            HwHiLog.d("WIFIPRO", false, "notifySelfCureComplete SOFT_CONNECT_FAILED, timeout happend", new Object[0]);
            this.mWifiSoftSwitchRunning.set(false);
            WifiProCommonUtils.setWifiSelfCureStatus(0);
            stopSelfCureDelay(-4, 0);
        } else if (!isWifiSelfCuring()) {
            logd("notifySelfCureComplete: not Curing!");
            stopSelfCureDelay(1, 0);
        } else if (success) {
            handleSelfCureNormal();
        } else {
            handleSelfCureException(reasonCode);
        }
    }

    public void notifySelfCureNetworkLost() {
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        if (wifiInfo != null) {
            SupplicantState supplicantState = wifiInfo.getSupplicantState();
            if (supplicantState == SupplicantState.ASSOCIATED || isSupplicantTransientState()) {
                Log.i(TAG, "Supplicant is under transient state: " + supplicantState);
            } else if (hasMessages(131890)) {
                logd("notifySelfCureNetworkLost, Stop Reset");
                removeMessages(131890);
                sendMessage(131890, -2, 0);
            } else if (hasMessages(131891)) {
                logd("notifySelfCureNetworkLost, Stop Reassociate");
                removeMessages(131891);
                sendMessage(131891, -2, 0);
            } else {
                logd("notifySelfCureNetworkLost, No delay message found.");
            }
        }
    }

    private void handleSelfCureNormal() {
        HwABSDetectorService service;
        int i = this.mWifiSelfCureState;
        if (i == 1) {
            logd("handleSelfCureNormal, wifi off OK! -> wifi on");
            selfCureWifiEnable();
        } else if (i == 2) {
            logd("handleSelfCureNormal, wifi on OK! -> wifi connect");
            setSelfCureWifiTimeOut(3);
            if (HwABSUtils.getABSEnable() && (service = HwABSDetectorService.getInstance()) != null) {
                service.notifySelEngineEnableWiFi();
            }
        } else if (i == 3 || i == 4 || i == 5) {
            logd("handleSelfCureNormal, wifi connect/reassoc/reconnect OK!");
            if (this.mWifiBackgroundConnected) {
                logd("handleSelfCureNormal, wifiBackgroundConnected, wifiNetworkExplicitlyUnselected");
                wifiNetworkExplicitlyUnselected();
            }
            this.mIsReassocUseWithFactoryMacAddress = false;
            stopSelfCureDelay(0, 500);
        }
    }

    private void handleSelfCureException(int reasonCode) {
        int i = this.mWifiSelfCureState;
        if (i == 1) {
            stopSelfCureDelay(-1, 0);
            logd("handleSelfCureException, wifi off fail! -> wifi off");
            this.mHwWifiProServiceManager.requestChangeWifiStatus(false);
        } else if (i == 2) {
            stopSelfCureDelay(-1, 0);
            logd("handleSelfCureException, wifi on fail! -> wifi on");
            this.mHwWifiProServiceManager.requestChangeWifiStatus(true);
        } else if (i == 3 || i == 4 || i == 5) {
            logd("handleSelfCureException, wifi connect/reassoc/reconnect failed! retry = " + this.mSelfCureWifiConnectRetry + ", reason = " + reasonCode);
            int i2 = this.mSelfCureWifiConnectRetry;
            if (i2 >= 1 || reasonCode == -2) {
                stopSelfCureDelay(reasonCode == -2 ? -2 : -1, 0);
                if (this.mWifiBackgroundConnected) {
                    disconnectCommand();
                } else {
                    if (reasonCode != -2 || this.mIsReassocUseWithFactoryMacAddress) {
                        startConnectToUserSelectNetwork(this.mCurrentConfigNetId, Binder.getCallingUid(), null);
                    }
                    this.mCurrentConfigNetId = -1;
                }
            } else {
                this.mSelfCureWifiConnectRetry = i2 + 1;
                startConnectToUserSelectNetwork(this.mCurrentConfigNetId, Binder.getCallingUid(), null);
                setSelfCureWifiTimeOut(3);
            }
            this.mIsReassocUseWithFactoryMacAddress = false;
        }
    }

    public void stopSelfCureWifi(int status) {
        HwHiLog.d(TAG, false, "stopSelfCureWifi, status =%{public}d", new Object[]{Integer.valueOf(status)});
        if (status == -4) {
            HwHiLog.d(TAG, false, "notify soft connect time out failed.", new Object[0]);
            sendWifiHandoverCompletedBroadcast(-6, null, null, null);
            sendMessage(131893);
        } else if (isWifiSelfCuring()) {
            this.mHwWifiProServiceManager.uploadDisconnectedEvent(EVENT_RECOVERY_FINISHED);
            NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
            if (this.mWifiBackgroundConnected && networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                logd("stopSelfCureWifi,  CONNECTED => POOR_LINK_DETECTED");
                sendMessage(131873);
            }
            this.mHwWifiProServiceManager.notifySefCureCompleted(status);
            if (WifiCommonUtils.IS_TV) {
                int selfCureState = WifiProCommonUtils.getSelfCuringState();
                resetSelfCureParam();
                boolean isTvStrState = "str".equals(SystemProperties.get("sys.hw_mc.tvpower.suspend_mode", ""));
                if (selfCureState == 102 && isTvStrState) {
                    this.mHwWifiProServiceManager.requestChangeWifiStatus(false);
                    logd("str state, turn off wifi");
                }
            } else {
                resetSelfCureParam();
            }
            sendMessage(131893);
        }
    }

    public void stopSelfCureDelay(int status, int delay) {
        if (hasMessages(131892)) {
            removeMessages(131892);
        }
        sendMessageDelayed(obtainMessage(131892, status, 0), (long) delay);
    }

    public void exitWifiSelfCure(int exitedType, int networkId) {
        if (!isWifiSelfCuring()) {
            return;
        }
        if (networkId == -1 || networkId == getSelfCureNetworkId()) {
            logd("exitWifiSelfCure, CONNECT_NETWORK/FORGET_NETWORK/CLOSE_WIFI stop SCE, type = " + exitedType);
            WifiProCommonUtils.setWifiSelfCureStatus(0);
            this.mHwWifiProServiceManager.notifySelfCureWifiDisconnected();
            int status = 1;
            if (exitedType == 151553 || exitedType == 151556 || exitedType == 1) {
                status = -3;
            }
            stopSelfCureDelay(status, 0);
            return;
        }
        logd("exitWifiSelfCure, user forget other network, do nothing.");
    }

    @Deprecated
    public List<String> syncGetApLinkedStaList(AsyncChannel channel) {
        HwHiLog.d(TAG, false, "HwWiFIStateMachine syncGetApLinkedStaList", new Object[0]);
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_AP_STARTED_GET_STA_LIST);
        if (resultMsg == null) {
            return Collections.emptyList();
        }
        List<String> ret = (List) resultMsg.obj;
        resultMsg.recycle();
        return ret;
    }

    @Deprecated
    public void handleSetSoftapMacFilter(String macFilter) {
        HwHiLog.d(TAG, false, "HwWifiStateMachine handleSetSoftapMacFilter is called, macFilter =%{private}s", new Object[]{macFilter});
        WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setSoftapMacFltrHw(macFilter);
    }

    @Deprecated
    public void handleSetSoftapDisassociateSta(String mac) {
        HwHiLog.d(TAG, false, "HwWifiStateMachine handleSetSoftapDisassociateSta is called, mac =%{private}s", new Object[]{mac});
        WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.disassociateSoftapStaHw(mac);
    }

    public boolean handleWapiFailureEvent(Message message, SupplicantStateTracker mSupplicantStateTracker) {
        if (147474 == message.what) {
            HwHiLog.d(TAG, false, "Handling WAPI_EVENT, msg [%{public}d]", new Object[]{Integer.valueOf(message.what)});
            Intent intent = new Intent(SUPPLICANT_WAPI_EVENT);
            intent.putExtra("wapi_string", 16);
            this.myContext.sendBroadcast(intent);
            mSupplicantStateTracker.sendMessage(147474);
            return true;
        } else if (147475 != message.what) {
            return false;
        } else {
            HwHiLog.d(TAG, false, "Handling WAPI_EVENT, msg [%{public}d]", new Object[]{Integer.valueOf(message.what)});
            Intent intent2 = new Intent(SUPPLICANT_WAPI_EVENT);
            intent2.putExtra("wapi_string", 17);
            this.myContext.sendBroadcast(intent2);
            return true;
        }
    }

    public void handleStopWifiRepeater(AsyncChannel wifiP2pChannel) {
        wifiP2pChannel.sendMessage((int) CMD_STOP_WIFI_REPEATER);
    }

    public boolean isWifiRepeaterStarted() {
        return 1 == Settings.Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0) || 6 == Settings.Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    public void setWifiRepeaterStoped() {
        Settings.Global.putInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    public void triggerUpdateAPInfo() {
        List<ScanResult> cachedScanResults;
        if (HWFLOW) {
            HwHiLog.d(TAG, false, "triggerUpdateAPInfo", new Object[0]);
        }
        String currentBssid = getCurrentBSSID();
        if (!(currentBssid == null || currentBssid.isEmpty() || this.mApCapChr.containsKey(currentBssid) || (cachedScanResults = wifiStateMachineUtils.getScanRequestProxy(this).getScanResults()) == null || cachedScanResults.size() == 0)) {
            for (ScanResult result : cachedScanResults) {
                if (result != null && result.BSSID != null && !result.BSSID.isEmpty() && currentBssid.equals(result.BSSID)) {
                    NetworkDetail networkDetail = new NetworkDetail(result.BSSID, result.informationElements, (List) null, result.frequency);
                    int stream1 = networkDetail.getStream1();
                    int stream2 = networkDetail.getStream2();
                    int stream3 = networkDetail.getStream3();
                    int stream4 = networkDetail.getStream4();
                    if (!(stream1 == 0 && stream2 == 0 && stream3 == 0 && stream4 == 0)) {
                        String apData = "{AP_CAP:" + (((result.frequency / 1000) * 10) + stream1 + stream2 + stream3 + stream4) + "}";
                        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
                        if (hwWifiCHRService != null) {
                            hwWifiCHRService.updateWifiException(909002029, apData);
                            this.mApCapChr.put(currentBssid, true);
                            if (this.mApCapChr.size() > 1000) {
                                this.mApCapChr.clear();
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    public void sendStaFrequency(int frequency) {
        if (mFrequency != frequency && frequency >= 5180) {
            mFrequency = frequency;
            HwHiLog.d(TAG, false, "sendStaFrequency %{public}d", new Object[]{Integer.valueOf(mFrequency)});
            Intent intent = new Intent("android.net.wifi.p2p.STA_FREQUENCY_CREATED");
            intent.putExtra("freq", String.valueOf(frequency));
            this.myContext.sendBroadcast(intent);
        }
    }

    public boolean isHiLinkActive() {
        HiLinkController hiLinkController = this.mHiLinkController;
        if (hiLinkController != null) {
            return hiLinkController.isHiLinkActive();
        }
        return HwWifiStateMachine.super.isHiLinkActive();
    }

    public void enableHiLinkHandshake(boolean isEnableHiLink, String bssid, WifiConfiguration config) {
        if (isEnableHiLink) {
            clearRandomMacOui();
            this.mIsRandomMacCleared = true;
        } else if (this.mIsRandomMacCleared) {
            setRandomMacOui();
            this.mIsRandomMacCleared = false;
        }
        if (config == null || !isConnectedMacRandomizationEnabled()) {
            this.mHiLinkController.enableHiLinkHandshake(isEnableHiLink, bssid);
        } else {
            this.mHiLinkController.enableHiLinkHandshake(isEnableHiLink, bssid, config);
        }
    }

    public void sendWpsOkcStartedBroadcast() {
        this.mHiLinkController.sendWpsOkcStartedBroadcast();
    }

    public NetworkUpdateResult saveWpsOkcConfiguration(int connectionNetId, String connectionBssid) {
        List<ScanResult> scanResults = new ArrayList<>();
        if (!WifiInjector.getInstance().getClientModeImplHandler().runWithScissors(new Runnable(scanResults) {
            /* class com.android.server.wifi.$$Lambda$HwWifiStateMachine$L5we5jgkUzLvi8d8SVTYbk_Z1s */
            private final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwWifiStateMachine.this.lambda$saveWpsOkcConfiguration$1$HwWifiStateMachine(this.f$1);
            }
        }, 4000)) {
            HwHiLog.e(TAG, false, "Failed to post runnable to fetch scan results", new Object[0]);
        }
        return this.mHiLinkController.saveWpsOkcConfiguration(connectionNetId, connectionBssid, scanResults);
    }

    public /* synthetic */ void lambda$saveWpsOkcConfiguration$1$HwWifiStateMachine(List scanResults) {
        scanResults.addAll(wifiStateMachineUtils.getScanRequestProxy(this).getScanResults());
    }

    public void handleAntenaPreempted() {
        HwHiLog.d(TAG, false, "%{public}s EVENT_ANT_CORE_ROB", new Object[]{getName()});
        this.myContext.sendBroadcastAsUser(new Intent(HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED), UserHandle.ALL, HwABSUtils.HUAWEI_BUSSINESS_PERMISSION);
    }

    public void handleDualbandHandoverFailed(int disableReason) {
        if (this.mWifiSwitchOnGoing && disableReason == 3 && this.mHwWifiProServiceManager.getNetwoksHandoverType() == 4) {
            HwHiLog.d(TAG, false, "handleDualbandHandoverFailed, disableReason = %{public}d", new Object[]{Integer.valueOf(disableReason)});
            String failedBssid = null;
            String failedSsid = null;
            synchronized (this.selectConfigLock) {
                if (this.mSelectedConfig != null) {
                    failedBssid = this.mSelectedConfig.BSSID;
                    failedSsid = this.mSelectedConfig.SSID;
                }
            }
            HwHiLog.d(TAG, false, "handleDualbandHandoverFailed, sendWifiHandoverCompletedBroadcast, status = %{public}d", new Object[]{-7});
            sendWifiHandoverCompletedBroadcast(-7, failedBssid, failedSsid, null);
        }
    }

    public void setWiFiProRoamingSSID(WifiSsid SSID) {
        this.mWiFiProRoamingSSID = SSID;
    }

    public WifiSsid getWiFiProRoamingSSID() {
        return this.mWiFiProRoamingSSID;
    }

    public boolean isEnterpriseHotspot(WifiConfiguration config) {
        if (config != null) {
            String currentSsid = config.SSID;
            String configKey = config.configKey();
            if (TextUtils.isEmpty(currentSsid) || TextUtils.isEmpty(configKey)) {
                return false;
            }
            List<ScanResult> scanResults = new ArrayList<>();
            if (!WifiInjector.getInstance().getClientModeImplHandler().runWithScissors(new Runnable(scanResults) {
                /* class com.android.server.wifi.$$Lambda$HwWifiStateMachine$Rtaw3MiMjSAm9A80baumon7OXAI */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifiStateMachine.this.lambda$isEnterpriseHotspot$2$HwWifiStateMachine(this.f$1);
                }
            }, 4000)) {
                HwHiLog.e(TAG, false, "Failed to post runnable to fetch scan results", new Object[0]);
                return false;
            }
            int foundCounter = 0;
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult nextResult = scanResults.get(i);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String capabilities = nextResult.capabilities;
                if (currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(capabilities, configKey) && (foundCounter = foundCounter + 1) >= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    public /* synthetic */ void lambda$isEnterpriseHotspot$2$HwWifiStateMachine(List scanResults) {
        scanResults.addAll(wifiStateMachineUtils.getScanRequestProxy(this).getScanResults());
    }

    public String getConnectionRawPsk() {
        HwHiLog.d(TAG, false, "getConnectionRawPsk.", new Object[0]);
        if (this.myContext.checkCallingPermission(AP_INFO_PERMISSION) != 0) {
            HwHiLog.d(TAG, false, "getConnectionRawPsk: permissin denied.", new Object[0]);
            return null;
        } else if (-1 != wifiStateMachineUtils.getWifiInfo(this).getNetworkId()) {
            String ret = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.getConnectionRawPsk();
            HwHiLog.d(TAG, false, "getConnectionRawPsk: OK", new Object[0]);
            return ret;
        } else {
            HwHiLog.e(TAG, false, "getConnectionRawPsk: netId is invalid.", new Object[0]);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWlanChannelNumber(int channel) {
        if (channel > 13) {
            channel = 0;
        }
        WifiCommonUtils.notifyDeviceState("WLAN", String.valueOf(channel), "");
    }

    /* access modifiers changed from: protected */
    public void notifyWlanState(String state) {
        WifiCommonUtils.notifyDeviceState("WLAN", state, "");
    }

    private long getScanInterval() {
        long scanInterval;
        if (wifiStateMachineUtils.getOperationalMode(this) == 3) {
            scanInterval = Settings.Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WLAN_CLOSE, WIFI_SCAN_INTERVAL_WLAN_CLOSE_DEFAULT);
        } else if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            scanInterval = Settings.Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WHITE_WLAN_CONNECTED, 10000);
        } else {
            scanInterval = Settings.Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED, 10000);
        }
        logd("the wifi_scan interval is:" + scanInterval);
        return scanInterval;
    }

    public synchronized boolean disallowWifiScanRequest(int pid) {
        if (disallowWifiScanForConnection()) {
            HwHiLog.d(TAG, false, "disallowWifiScanForConnection", new Object[0]);
            return true;
        }
        ActivityManager.RunningAppProcessInfo appProcessInfo = getAppProcessInfoByPid(pid);
        if (pid <= 0 || appProcessInfo == null || appProcessInfo.pkgList == null || this.mIsScanCtrlPluggedin) {
            logd("wifi_scan pid[" + pid + "] is not correct or is charging. mIsScanCtrlPluggedin = " + this.mIsScanCtrlPluggedin + " isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            return false;
        } else if (isGlobalScanCtrl(appProcessInfo)) {
            logd("isGlobalScanCtrl contrl scan ");
            sendMessageDelayed(CMD_SCREEN_OFF_SCAN, WIFI_SCAN_RESULT_DELAY_TIME_DEFAULT);
            return true;
        } else {
            wifiScanBlackListLearning(appProcessInfo);
            long scanInterval = getScanInterval();
            if (isWifiScanBlacklisted(appProcessInfo, scanInterval)) {
                long now = SystemClock.elapsedRealtime();
                long appLastScanRequestTimestamp = 0;
                if (this.mPidLastScanSuccTimestamp.containsKey(Integer.valueOf(pid))) {
                    appLastScanRequestTimestamp = this.mPidLastScanSuccTimestamp.get(Integer.valueOf(pid)).longValue();
                }
                if (this.lastScanResultTimestamp == 0 || (now - this.lastScanResultTimestamp >= scanInterval && now - appLastScanRequestTimestamp >= scanInterval)) {
                    this.mPidLastScanSuccTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
                } else {
                    if (now - this.lastScanResultTimestamp < 0) {
                        logd("wifi_scan the last scan time is jump!!!");
                        this.lastScanResultTimestamp = now;
                    }
                    sendMessageDelayed(CMD_SCREEN_OFF_SCAN, WIFI_SCAN_RESULT_DELAY_TIME_DEFAULT);
                    return true;
                }
            }
            updateGlobalScanTimes();
            return false;
        }
    }

    public boolean isRSDBSupported() {
        return WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.isSupportRsdbByDriver();
    }

    /* access modifiers changed from: protected */
    public void handleSimAbsent(WifiConfiguration config) {
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        if (PreconfiguredNetworkManager.IS_R1 && config.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod()) && PreconfiguredNetworkManager.getInstance().isPreconfiguredNetwork(config.SSID)) {
            wifiConfigManager.disableNetwork(config.networkId, 1000);
            this.wifiEapUIManager.showDialog(Resources.getSystem().getString(33686254), Resources.getSystem().getString(33686250));
        }
    }

    /* access modifiers changed from: protected */
    public void handleEapErrorcodeReport(int networkId, String ssid, int errorCode) {
        if (PreconfiguredNetworkManager.IS_R1 && PreconfiguredNetworkManager.getInstance().isPreconfiguredNetwork(ssid)) {
            wifiStateMachineUtils.getWifiConfigManager(this).updateNetworkSelectionStatus(networkId, 16);
            this.wifiEapUIManager.showDialog(errorCode);
        }
    }

    public void handleWpa3ConnectFailReport(String eventInfo) {
        this.mWpa3SelfCureImpl.handleConnectFailReport(wifiStateMachineUtils.getWifiConfigManager(this), eventInfo);
    }

    public void notifyWpa3SelfCureConnectSucc() {
        this.mWpa3SelfCureImpl.handleSelfCureConnectSucc();
    }

    private void wifiScanBlackListLearning(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        long now = SystemClock.elapsedRealtime();
        long scanInterval = getScanInterval();
        int pid = appProcessInfo.pid;
        clearDeadPidCache();
        if (!this.mPidLastScanTimestamp.containsKey(Integer.valueOf(pid))) {
            this.mPidLastScanTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
            this.mPidWifiScanCount.put(Integer.valueOf(pid), 0);
            return;
        }
        if (!this.mPidWifiScanCount.containsKey(Integer.valueOf(pid))) {
            this.mPidWifiScanCount.put(Integer.valueOf(pid), 0);
        }
        long tmpLastScanRequestTimestamp = this.mPidLastScanTimestamp.get(Integer.valueOf(pid)).longValue();
        this.mPidLastScanTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
        if (tmpLastScanRequestTimestamp != 0 && now >= tmpLastScanRequestTimestamp) {
            if (isWifiScanInBlacklistCache(pid) || now - tmpLastScanRequestTimestamp >= scanInterval) {
                if (isWifiScanInBlacklistCache(pid) && now - tmpLastScanRequestTimestamp > WIFI_SCAN_BLACKLIST_REMOVE_INTERVAL) {
                    logd("wifi_scan blacklist cache remove pid:" + pid);
                    removeWifiScanBlacklistCache(pid);
                }
                this.mPidWifiScanCount.put(Integer.valueOf(pid), 0);
                return;
            }
            int count = this.mPidWifiScanCount.get(Integer.valueOf(pid)).intValue() + 1;
            this.mPidWifiScanCount.put(Integer.valueOf(pid), Integer.valueOf(count));
            if (((long) count) >= WIFI_SCAN_OVER_INTERVAL_MAX_COUNT) {
                this.mPidLastScanTimestamp.remove(Integer.valueOf(pid));
                this.mPidWifiScanCount.remove(Integer.valueOf(pid));
                logd("pid:" + pid + " wifi_scan interval is frequent");
                if (!isWifiScanWhitelisted(appProcessInfo)) {
                    addWifiScanBlacklistCache(appProcessInfo);
                }
            }
        }
    }

    private boolean isWifiScanInBlacklistCache(int pid) {
        for (Map.Entry<String, Integer> entry : this.mPidBlackList.entrySet()) {
            if (pid == entry.getValue().intValue()) {
                logd("pid:" + pid + " in wifi_scan cache blacklist, appname=" + entry.getKey());
                return true;
            }
        }
        for (Map.Entry<String, Integer> entry2 : this.mPidConnectedBlackList.entrySet()) {
            if (pid == entry2.getValue().intValue()) {
                logd("pid:" + pid + " in wifi_scan connected cache blacklist, appname=" + entry2.getKey());
                return true;
            }
        }
        return false;
    }

    private void removeWifiScanBlacklistCache(int pid) {
        this.mPidLastScanSuccTimestamp.remove(Integer.valueOf(pid));
        this.mPidLastScanTimestamp.remove(Integer.valueOf(pid));
        this.mPidWifiScanCount.remove(Integer.valueOf(pid));
        Iterator iter = this.mPidBlackList.entrySet().iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            Map.Entry<String, Integer> entry = iter.next();
            if (pid == entry.getValue().intValue()) {
                logd("pid:" + pid + " remove from wifi_scan cache blacklist success, appname=" + entry.getKey());
                iter.remove();
                break;
            }
        }
        Iterator iter2 = this.mPidConnectedBlackList.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry<String, Integer> entry2 = iter2.next();
            if (pid == entry2.getValue().intValue()) {
                logd("pid:" + pid + " remove from wifi_scan connected cache blacklist success, appname=" + entry2.getKey());
                iter2.remove();
                return;
            }
        }
    }

    private void addWifiScanBlacklistCache(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        int pid = appProcessInfo.pid;
        String appName = appProcessInfo.pkgList[0];
        logd("pid:" + pid + " add to wifi_scan connected limited blacklist");
        if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            this.mPidConnectedBlackList.put(appName, Integer.valueOf(pid));
        } else {
            this.mPidBlackList.put(appName, Integer.valueOf(pid));
        }
    }

    private boolean isWifiScanBlacklisted(ActivityManager.RunningAppProcessInfo appProcessInfo, long scanInterval) {
        if (isPackagesNamesMatched(appProcessInfo.pkgList, this.myContext.getResources().getStringArray(33816587), null)) {
            logd("config blacklist wifi_scan name:callingPkgNames[pid=" + appProcessInfo.pid + "]=" + appProcessInfo.processName);
            return true;
        }
        if (!wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            this.mPidConnectedBlackList.clear();
        } else {
            this.mPidBlackList.clear();
        }
        if (!isWifiScanConnectedLimitedWhitelisted(appProcessInfo)) {
            long j = this.mPidBlackListInteval;
            if (j > 0 && j != scanInterval) {
                logd("wifi_scan blacklist clear because the interval is change");
                this.mPidBlackList.clear();
                this.mPidBlackListInteval = 0;
            }
        }
        return isWifiScanInBlacklistCache(appProcessInfo.pid);
    }

    private boolean isPackagesNamesMatched(String[] callingPkgNames, String[] whitePkgs, String whiteDbPkgs) {
        int whitePkgsLength = 0;
        if (whitePkgs != null) {
            whitePkgsLength = whitePkgs.length;
        }
        if (callingPkgNames == null || (whiteDbPkgs == null && whitePkgsLength == 0)) {
            logd("wifi_scan input PkgNames are not correct");
            return false;
        }
        for (int j = 0; j < whitePkgsLength; j++) {
            logd("config--list:" + whitePkgs[j]);
        }
        logd("config--db:" + whiteDbPkgs);
        for (int i = 0; i < callingPkgNames.length; i++) {
            for (int j2 = 0; j2 < whitePkgsLength; j2++) {
                if (callingPkgNames[i].equals(whitePkgs[j2])) {
                    logd("config white wifi_scan name:callingPkgNames[" + Integer.toString(i) + "]=" + callingPkgNames[i]);
                    return true;
                }
            }
            if (whiteDbPkgs != null && TextUtils.delimitedStringContains(whiteDbPkgs, ',', callingPkgNames[i])) {
                logd("db white wifi_scan name:callingPkgNames[" + Integer.toString(i) + "]=" + callingPkgNames[i]);
                return true;
            }
        }
        return false;
    }

    private boolean isWifiScanConnectedLimitedWhitelisted(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        String[] callingPkgNames = appProcessInfo.pkgList;
        String[] whitePkgs = this.myContext.getResources().getStringArray(33816588);
        String whiteDbPkgs = Settings.Global.getString(this.myContext.getContentResolver(), WIFI_SCAN_CONNECTED_LIMITED_WHITE_PACKAGENAME);
        if (appProcessInfo.uid == 1000) {
            return true;
        }
        if (!isPackagesNamesMatched(callingPkgNames, whitePkgs, whiteDbPkgs)) {
            return false;
        }
        logd("wifi_scan pkgname is in connected whitelist pkgs");
        return true;
    }

    private boolean isWifiScanWhitelisted(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        if (isPackagesNamesMatched(appProcessInfo.pkgList, this.myContext.getResources().getStringArray(33816589), Settings.Global.getString(this.myContext.getContentResolver(), WIFI_SCAN_WHITE_PACKAGENAME))) {
            logd("wifi_scan pkgname is in whitelist pkgs");
            return true;
        } else if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1010) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void updateLastScanRequestTimestamp() {
        this.lastScanResultTimestamp = SystemClock.elapsedRealtime();
        logd("wifi_scan update lastScanResultTimestamp=" + this.lastScanResultTimestamp);
    }

    private ActivityManager.RunningAppProcessInfo getAppProcessInfoByPid(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.myContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                logd("PkgInfo--uid=" + appProcess.uid + ", processName=" + appProcess.processName + ",pid=" + pid);
                return appProcess;
            }
        }
        return null;
    }

    private void clearDeadPidCache() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.myContext.getSystemService("activity")).getRunningAppProcesses();
        ArrayList<Integer> tmpPidSet = new ArrayList<>();
        Iterator iter = this.mPidLastScanTimestamp.entrySet().iterator();
        if (appProcessList != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                tmpPidSet.add(Integer.valueOf(appProcess.pid));
            }
            while (iter.hasNext()) {
                Integer key = iter.next().getKey();
                if (!tmpPidSet.contains(key)) {
                    iter.remove();
                    this.mPidWifiScanCount.remove(key);
                    this.mPidLastScanSuccTimestamp.remove(key);
                }
            }
        }
    }

    public void transitionToCallback(IState destState) {
        Queue<IState> queue = this.mDestStates;
        if (queue != null) {
            queue.offer(destState);
        }
        HwHiLog.i(TAG, false, "transition to %{public}s begining.", new Object[]{destState.getClass().getSimpleName()});
    }

    /* access modifiers changed from: protected */
    public void onPostHandleMessage(Message msg) {
        IState destState;
        Queue<IState> queue = this.mDestStates;
        if (queue != null && (destState = queue.poll()) != null) {
            HwHiLog.i(TAG, false, "transition to %{public}s finished.", new Object[]{destState.getClass().getSimpleName()});
        }
    }

    /* renamed from: com.android.server.wifi.HwWifiStateMachine$8  reason: invalid class name */
    static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pGroupInfoAvailable(WifiP2pGroup info) {
        this.mIsP2pHasTvDevice = false;
        logd("handleP2pGroupInfoAvailable");
        if (info != null) {
            if (info.isGroupOwner()) {
                Iterator<WifiP2pDevice> it = info.getClientList().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (TV_PRIMARY_DEVICE_TYPE.equals(it.next().primaryDeviceType)) {
                            this.mIsP2pHasTvDevice = true;
                            logd("Group Owner, P2p device is Tv Device");
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else if (info.getOwner() != null) {
                if (TV_PRIMARY_DEVICE_TYPE.equals(info.getOwner().primaryDeviceType)) {
                    this.mIsP2pHasTvDevice = true;
                    logd("Group Client, P2p device is Tv Device");
                }
            } else {
                return;
            }
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null && this.mIsP2pHasTvDevice) {
                castOptChr.notifyP2pGroupCreatedWithTvDevice();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectionChangedAction(Intent intent) {
        if (intent != null) {
            if (this.mWifiP2pManager == null) {
                this.mWifiP2pManager = (WifiP2pManager) this.myContext.getSystemService("wifip2p");
                this.mP2pChannel = this.mWifiP2pManager.initialize(this.myContext, WifiInjector.getInstance().getClientModeImplHandler().getLooper(), null);
            }
            NetworkInfo p2pNetworkInfo = null;
            if (intent.getParcelableExtra("networkInfo") instanceof NetworkInfo) {
                p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            }
            if (p2pNetworkInfo == null) {
                this.mIsP2pConnected = false;
                this.mP2pGroupCreated.set(false);
                this.mIsP2pHasTvDevice = false;
                this.mIsNeedIgnoreScan = false;
                return;
            }
            this.mIsP2pConnected = p2pNetworkInfo.isConnected();
            this.mP2pGroupCreated.set(this.mIsP2pConnected);
            if (this.mIsP2pConnected) {
                this.mWifiP2pManager.requestGroupInfo(this.mP2pChannel, new WifiP2pManager.GroupInfoListener() {
                    /* class com.android.server.wifi.HwWifiStateMachine.AnonymousClass7 */

                    @Override // android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
                    public void onGroupInfoAvailable(WifiP2pGroup info) {
                        HwWifiStateMachine.this.handleP2pGroupInfoAvailable(info);
                    }
                });
                return;
            }
            this.mIsP2pHasTvDevice = false;
            this.mIsNeedIgnoreScan = false;
            this.mIsIgnoreScanInCastScene = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectedChangedAction(Intent intent) {
        if (intent != null && intent.getIntExtra("extraState", -1) == 2) {
            Log.i(TAG, "handleP2pConnectedChangedAction set mP2pGroupCreated");
            this.mP2pGroupCreated.set(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMiracastStateChangeMsg(Intent intent) {
        if (intent != null) {
            WifiDisplayStatus status = intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS");
            boolean z = false;
            if (status == null) {
                this.mIsNeedIgnoreScan = false;
                return;
            }
            int state = status.getActiveDisplayState();
            logd("state : " + state);
            if (state != 2) {
                this.mIsNeedIgnoreScan = false;
                return;
            }
            if (WifiCommonUtils.IS_TV) {
                this.mIsNeedIgnoreScan = this.mIsP2pConnected;
            } else {
                if (this.mIsP2pConnected && this.mIsP2pHasTvDevice) {
                    z = true;
                }
                this.mIsNeedIgnoreScan = z;
            }
            logd("mIsNeedIgnoreScan : " + this.mIsNeedIgnoreScan + " mIsP2pConnected : " + this.mIsP2pConnected + " mIsP2pHasTvDevice : " + this.mIsP2pHasTvDevice);
        }
    }

    public boolean isNeedIgnoreScan() {
        return this.mIsNeedIgnoreScan || this.mIsIgnoreScanInCastScene;
    }

    public void setIgnoreScanInCastScene(boolean isIgnoreScanInCastScene) {
        logd("setIgnoreScanInCastScene isIgnoreScanInCastScene: " + isIgnoreScanInCastScene);
        this.mIsIgnoreScanInCastScene = isIgnoreScanInCastScene;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLowPwrMode(boolean isConnected, String ssid, boolean isMobileAP, boolean isScreenOn) {
        boolean isHwSsid = false;
        boolean isCloneSsid = false;
        if (ssid != null) {
            isHwSsid = ssid.equals("\"Huawei-Employee\"");
            isCloneSsid = ssid.contains("CloudClone");
        }
        logd("setpmlock:isConnected: " + isConnected + " ssid:" + ssid + " isMobileAP:" + isMobileAP + " isAndroidMobileAP:" + isAndroidMobileAP());
        if (!isConnected || (!isHwSsid && ((!isMobileAP || !isAndroidMobileAP() || isCloneSsid) && isScreenOn && !this.mssArbi.matchAllowMSSApkList()))) {
            WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.gameKOGAdjustSpeed(0, 6);
        } else {
            WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.gameKOGAdjustSpeed(0, 7);
        }
    }

    private void pwrBoostRegisterBcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addCategory("android.net.wifi.STATE_CHANGE@hwBrExpand@WifiNetStatus=WIFICON|WifiNetStatus=WIFIDSCON");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("huawei.wifi.WIFI_MODE_STATE");
        this.myContext.registerReceiver(this.mBcastReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void linkMeasureAndStatic(boolean enable) {
        long ret = 0;
        long arpRtt = 0;
        int arpCnt = 0;
        WifiNative.TxPacketCounters txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters(wifiStateMachineUtils.getInterfaceName(this));
        if (txPacketCounters != null) {
            int lastTxGoodCnt = txPacketCounters.txSucceeded;
            int lastTxBadCnt = txPacketCounters.txFailed;
            long lastTxRetries = wifiStateMachineUtils.getWifiInfo(this).txRetries;
            HwArpUtils hwArpUtils = new HwArpUtils(this.myContext);
            int i = 0;
            while (i < 5) {
                ret = hwArpUtils.getGateWayArpRTT(1000);
                if (ret != -1) {
                    arpRtt += ret;
                    arpCnt++;
                }
                i++;
                txPacketCounters = txPacketCounters;
            }
            WifiNative.TxPacketCounters txPacketCounters2 = WifiInjector.getInstance().getWifiNative().getTxPacketCounters(wifiStateMachineUtils.getInterfaceName(this));
            if (txPacketCounters2 != null) {
                int dltTxGoodCnt = txPacketCounters2.txSucceeded - lastTxGoodCnt;
                int dltTxBadCnt = txPacketCounters2.txFailed - lastTxBadCnt;
                long dltTxRetries = wifiStateMachineUtils.getWifiInfo(this).txRetries - lastTxRetries;
                logd("pwr:dltTxGoodCnt:" + dltTxGoodCnt + " dltTxBadCnt:" + dltTxBadCnt + " dltTxRetries:" + dltTxRetries + " arpRtt:" + arpRtt + " arpCnt:" + arpCnt + " enable:" + enable);
                this.mHwWifiCHRService.txPwrBoostChrStatic(Boolean.valueOf(enable), (int) arpRtt, arpCnt, dltTxGoodCnt, dltTxBadCnt, (int) dltTxRetries);
            }
        }
    }

    public int isAllowedManualWifiPwrBoost() {
        return this.mIsAllowedManualPwrBoost;
    }

    public boolean isWifiConnectivityManagerEnabled() {
        return this.mWifiConnectivityManager != null && this.mWifiConnectivityManager.isWifiConnectivityManagerEnabled();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearPwrBoostChrStatus() {
        this.mCurrentPwrBoostStat = false;
        this.mIsFinishLinkDetect = false;
        this.mPwrBoostOncnt = 0;
        this.mPwrBoostOffcnt = 0;
    }

    private class pwrBoostHandler extends Handler {
        private static final int PWR_BOOST_END_MSG = 1;
        private static final int PWR_BOOST_MANUAL_DISABLE = 0;
        private static final int PWR_BOOST_MANUAL_ENABLE = 1;
        private static final int PWR_BOOST_START_MSG = 0;

        pwrBoostHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    HwWifiStateMachine.this.clearPwrBoostChrStatus();
                }
            } else if (!HwWifiStateMachine.this.mWifiConnectState) {
                HwWifiStateMachine.this.clearPwrBoostChrStatus();
            } else if (HwWifiStateMachine.this.mIsChargePluggedin && HwWifiStateMachine.this.mIsFinishLinkDetect) {
                HwWifiStateMachine.this.mIsAllowedManualPwrBoost = 0;
            } else {
                if (HwWifiStateMachine.this.mIsChargePluggedin && !HwWifiStateMachine.this.mIsFinishLinkDetect) {
                    HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                    hwWifiStateMachine.mIsAllowedManualPwrBoost = 1;
                    if (!hwWifiStateMachine.mCurrentPwrBoostStat) {
                        WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setPwrBoost(1);
                        HwWifiStateMachine.this.mCurrentPwrBoostStat = true;
                        HwWifiStateMachine hwWifiStateMachine2 = HwWifiStateMachine.this;
                        hwWifiStateMachine2.linkMeasureAndStatic(hwWifiStateMachine2.mCurrentPwrBoostStat);
                        HwWifiStateMachine.access$2608(HwWifiStateMachine.this);
                    } else if (HwWifiStateMachine.this.mCurrentPwrBoostStat) {
                        WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setPwrBoost(0);
                        HwWifiStateMachine.this.mCurrentPwrBoostStat = false;
                        HwWifiStateMachine hwWifiStateMachine3 = HwWifiStateMachine.this;
                        hwWifiStateMachine3.linkMeasureAndStatic(hwWifiStateMachine3.mCurrentPwrBoostStat);
                        HwWifiStateMachine.access$2708(HwWifiStateMachine.this);
                    }
                }
                if (HwWifiStateMachine.this.mPwrBoostOncnt < 3 || HwWifiStateMachine.this.mPwrBoostOffcnt < 3) {
                    HwWifiStateMachine.this.mIsFinishLinkDetect = false;
                } else {
                    HwWifiStateMachine.this.mIsFinishLinkDetect = true;
                }
            }
        }
    }

    private boolean isGlobalScanCtrl(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        logd("isGlobalScanCtrl begin ");
        if (!isWifiScanWhitelisted(appProcessInfo)) {
            logd("wifi_scan return isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            if (!this.isInGlobalScanCtrl || SystemClock.elapsedRealtime() - this.mLastScanTimestamp > TIMEOUT_CONTROL_SCAN_ASSOCIATED) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void updateGlobalScanTimes() {
        long now = SystemClock.elapsedRealtime();
        long scanInterval = now - this.mLastScanTimestamp;
        this.mLastScanTimestamp = now;
        if (scanInterval > 0) {
            logd("wifi_scan interval = " + scanInterval + " mFoulTimes = " + this.mFoulTimes + " mFreedTimes = " + this.mFreedTimes + " isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            if (this.isInGlobalScanCtrl) {
                if (scanInterval > 10000) {
                    this.mFreedTimes++;
                } else {
                    this.mFreedTimes = 0;
                }
                if (this.mFreedTimes >= 5) {
                    this.mFoulTimes = 0;
                    this.mFreedTimes = 0;
                    this.isInGlobalScanCtrl = false;
                    return;
                }
                return;
            }
            if (scanInterval < TIMEOUT_CONTROL_SCAN_ASSOCIATED) {
                this.mFoulTimes++;
            } else {
                this.mFoulTimes = 0;
            }
            if (this.mFoulTimes >= 5) {
                this.mFoulTimes = 0;
                this.mFreedTimes = 0;
                this.isInGlobalScanCtrl = true;
            }
        }
    }

    public boolean getChargingState() {
        String usb = HwArpUtils.readFileByChars(USB_SUPPLY);
        if (usb.length() == 0) {
            usb = HwArpUtils.readFileByChars(USB_SUPPLY_QCOM);
        }
        if ("1".equals(usb.trim())) {
            return true;
        }
        logd("getChargingState return false");
        return false;
    }

    /* access modifiers changed from: package-private */
    public void registHwSoftApManager(HwSoftApManager hwSoftApManager) {
        this.mHwSoftApManager = hwSoftApManager;
        HwHiLog.d(TAG, false, "HwSoftApManager registed", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public void clearHwSoftApManager() {
        HwHiLog.d(TAG, false, "Clear HwSoftApManager", new Object[0]);
        HwSoftApManager hwSoftApManager = this.mHwSoftApManager;
        if (hwSoftApManager != null) {
            hwSoftApManager.clearCallbacksAndMessages();
        }
        this.mHwSoftApManager = null;
    }

    public List<String> getApLinkedStaList() {
        HwSoftApManager hwSoftApManager = this.mHwSoftApManager;
        if (hwSoftApManager != null) {
            return hwSoftApManager.getApLinkedStaList();
        }
        HwHiLog.w(TAG, false, "getApLinkedStaList called when mHwSoftApManager is not registed", new Object[0]);
        return Collections.emptyList();
    }

    public int[] getSoftApChannelListFor5G() {
        HwSoftApManager hwSoftApManager = this.mHwSoftApManager;
        return HwSoftApManager.getSoftApChannelListFor5G();
    }

    public void setSoftapDisassociateSta(String mac) {
        HwSoftApManager hwSoftApManager = this.mHwSoftApManager;
        if (hwSoftApManager != null) {
            hwSoftApManager.setSoftApDisassociateSta(mac);
        } else {
            HwHiLog.w(TAG, false, "setSoftapDisassociateSta called when mHwSoftApManager is not registed", new Object[0]);
        }
    }

    public void setSoftapMacFilter(String macFilter) {
        HwSoftApManager hwSoftApManager = this.mHwSoftApManager;
        if (hwSoftApManager != null) {
            hwSoftApManager.setSoftapMacFilter(macFilter);
        } else {
            HwHiLog.w(TAG, false, "setSoftapMacFilter called when mHwSoftApManager is not registed", new Object[0]);
        }
    }

    public boolean isAndroidMobileAP() {
        String ipAddress = "";
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            ipAddress = intIpToStringIp(wifiInfo.getIpAddress());
        }
        if (ipAddress == null || !ipAddress.startsWith("192.168.43.")) {
            return false;
        }
        return true;
    }

    private String intIpToStringIp(int ip) {
        return String.format("%d.%d.%d.%d", Integer.valueOf(ip & 255), Integer.valueOf((ip >> 8) & 255), Integer.valueOf((ip >> 16) & 255), Integer.valueOf((ip >> 24) & 255));
    }

    public void startPacketKeepalive(Message msg) {
        KeepalivePacketData data = (KeepalivePacketData) msg.obj;
        if (data != null) {
            HwHiLog.e(TAG, false, "startPacketKeepalive msg.arg1 = %{public}d msg.arg2 =%{public}d srcPort = %{public}d dstPort = %{public}d", new Object[]{Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2), Integer.valueOf(data.srcPort), Integer.valueOf(data.dstPort)});
        } else {
            HwHiLog.e(TAG, false, "startPacketKeepalive data == null", new Object[0]);
        }
        sendMessage(131232, msg.arg1, msg.arg2, msg.obj);
    }

    public void stopPacketKeepalive(Message msg) {
        KeepalivePacketData data = (KeepalivePacketData) msg.obj;
        if (data != null) {
            HwHiLog.e(TAG, false, "stopPacketKeepalive msg.arg1 = %{public}d msg.arg2 =%{public}d srcPort = %{public}d dstPort = %{public}d", new Object[]{Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2), Integer.valueOf(data.srcPort), Integer.valueOf(data.dstPort)});
        } else {
            HwHiLog.e(TAG, false, "stopPacketKeepalive data == null", new Object[0]);
        }
        sendMessage(131233, msg.arg1, msg.arg2, msg.obj);
    }

    public SoftApChannelXmlParse getSoftApChannelXmlParse() {
        return this.mSoftApChannelXmlParse;
    }

    public int getWifiRepeaterMode() {
        if (!IS_REPEATER_SUPPORT) {
            HwHiLog.d(TAG, false, "hw_wifibridge is disable", new Object[0]);
            return 3;
        }
        int wifiState = syncGetWifiState();
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        boolean isConnected = networkInfo != null && networkInfo.isConnected() && WifiRepeaterController.isWifiConnected();
        if (wifiState != 3) {
            HwHiLog.d(TAG, false, "do not allow to start WifiRepeater because wifi is disabled", new Object[0]);
            return 0;
        } else if (!isConnected) {
            HwHiLog.d(TAG, false, "do not allow to start WifiRepeater because wifi is disconneted", new Object[0]);
            return 1;
        } else {
            boolean isP2pServiceExist = false;
            boolean isWifiRepeaterStarted = false;
            boolean isP2pEnabled = false;
            if (this.mWifiP2pServiceImpl != null && (this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
                HwWifiP2pService hwWifiP2pService = this.mWifiP2pServiceImpl;
                isP2pServiceExist = hwWifiP2pService.hasP2pService();
                isWifiRepeaterStarted = hwWifiP2pService.getWifiRepeaterTetherStarted();
                isP2pEnabled = this.mWifiP2pServiceImpl.isP2pEnabled();
            }
            if (!isEncryptionTypeAllowed()) {
                HwHiLog.d(TAG, false, "do not allow to start WifiRepeater because EncryptionType is EAP", new Object[0]);
                return 2;
            } else if (isRepeaterSsidSameWithWifi()) {
                HwHiLog.d(TAG, false, "allow to start WifiRepeater but SSID is same with connected wifi", new Object[0]);
                return 12;
            } else if (isWifiRepeaterStarted) {
                HwHiLog.d(TAG, false, "WifiRepeater is already open, allow to start WifiRepeater", new Object[0]);
                return 20;
            } else if (isP2pServiceExist) {
                HwHiLog.d(TAG, false, "allow to start WifiRepeater but has p2p service", new Object[0]);
                return 10;
            } else if (!isP2pEnabled) {
                HwHiLog.d(TAG, false, "allow to start WifiRepeater but p2p is disabled", new Object[0]);
                return 11;
            } else if (!isChannelAllowed()) {
                HwHiLog.d(TAG, false, "allow to start WifiRepeater but channel is dfs", new Object[0]);
                return 13;
            } else {
                HwHiLog.d(TAG, false, "allow to start WifiRepeater", new Object[0]);
                return 20;
            }
        }
    }

    private boolean isEncryptionTypeAllowed() {
        WifiConfiguration connectionConfig = getCurrentWifiConfiguration();
        if (connectionConfig == null) {
            HwHiLog.d(TAG, false, "isEncryptionTypeAllowed: connectionConfig==null", new Object[0]);
            return false;
        } else if (connectionConfig.enterpriseConfig == null) {
            HwHiLog.d(TAG, false, "isEncryptionTypeAllowed: enterpriseConfig is null, return true.", new Object[0]);
            return true;
        } else {
            int eapMethod = connectionConfig.enterpriseConfig.getEapMethod();
            HwHiLog.d(TAG, false, "isEncryptionTypeAllowed: eapMethod=%{public}d", new Object[]{Integer.valueOf(eapMethod)});
            if (eapMethod == 1 || eapMethod == 2) {
                return false;
            }
            return true;
        }
    }

    private boolean isChannelAllowed() {
        if (!isStereoAudioWorking()) {
            return true;
        }
        setStereoAudioWorkingFlag(false);
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null) {
            HwHiLog.e(TAG, false, "isChannelAllowed: wifiInjector is null", new Object[0]);
            return false;
        }
        WifiNative wifiNative = wifiInjector.getWifiNative();
        if (wifiNative == null || wifiNative.mHwWifiNativeEx == null) {
            HwHiLog.e(TAG, false, "isChannelAllowed: wifiNative or mHwWifiNativeEx is null", new Object[0]);
            return false;
        }
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        if (wifiInfo == null) {
            HwHiLog.e(TAG, false, "isChannelAllowed: wifiInfo is null", new Object[0]);
            return false;
        } else if (!wifiNative.mHwWifiNativeEx.isDfsChannel(wifiInfo.getFrequency())) {
            return true;
        } else {
            HwHiLog.d(TAG, false, "isChannelAllowed: current channel is dfs", new Object[0]);
            return false;
        }
    }

    private boolean isRepeaterSsidSameWithWifi() {
        WifiManager wifiManager = (WifiManager) this.myContext.getSystemService("wifi");
        if (wifiManager == null) {
            return false;
        }
        WifiConfiguration wifiConfig = wifiStateMachineUtils.getWifiConfigManager(this).getConfiguredNetworkWithoutMasking(wifiStateMachineUtils.getLastNetworkId(this));
        WifiConfiguration wifiRepeaterConfig = wifiManager.getWifiApConfiguration();
        if (wifiConfig == null || wifiRepeaterConfig == null || wifiConfig.SSID == null || wifiRepeaterConfig.SSID == null) {
            HwHiLog.e(TAG, false, "wifiConfig, wifiRepeaterConfig or ssid is null", new Object[0]);
            return false;
        }
        int wifiAuthType = 0;
        String wifiSecurityTypeString = wifiConfig.getSsidAndSecurityTypeString().substring(wifiConfig.SSID.length());
        int i = 0;
        while (true) {
            if (i >= WifiConfiguration.KeyMgmt.strings.length) {
                break;
            } else if (WifiConfiguration.KeyMgmt.strings[i].equals(wifiSecurityTypeString)) {
                wifiAuthType = i;
                break;
            } else {
                i++;
            }
        }
        int wifiRepeaterAuthType = 0;
        if (wifiRepeaterConfig.allowedKeyManagement != null && wifiRepeaterConfig.allowedKeyManagement.cardinality() <= 1) {
            wifiRepeaterAuthType = wifiRepeaterConfig.getAuthType();
        }
        String wifiRepeaterPassword = "\"" + wifiRepeaterConfig.preSharedKey + "\"";
        boolean isSameSSID = (wifiConfig.SSID == null || wifiRepeaterConfig.SSID == null || !wifiConfig.SSID.equals("\"" + wifiRepeaterConfig.SSID + "\"")) ? false : true;
        boolean isSamePassword = (wifiConfig.preSharedKey == null && wifiRepeaterConfig.preSharedKey == null) || !(wifiConfig.preSharedKey == null || wifiRepeaterConfig.preSharedKey == null || !wifiConfig.preSharedKey.equals(wifiRepeaterPassword));
        boolean isSameAuthType = wifiAuthType == wifiRepeaterAuthType || (wifiRepeaterAuthType == 4 && (wifiAuthType == 1 || wifiAuthType == 4));
        if (!isSameSSID || !isSameAuthType || isSamePassword) {
            return false;
        }
        return true;
    }

    public List<String> getRepeaterLinkedClientList() {
        if (this.mWifiP2pServiceImpl == null || !(this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            HwHiLog.w(TAG, false, "getRepeaterLinkedClientList called when wifiP2pServiceImpl is null", new Object[0]);
        } else {
            HwWifiP2pService hwWifiP2pService = this.mWifiP2pServiceImpl;
            if (hwWifiP2pService.getWifiRepeaterTetherStarted()) {
                return hwWifiP2pService.getRepeaterLinkedClientList();
            }
        }
        return Collections.emptyList();
    }

    public void setWifiRepeaterDisassociateSta(String mac) {
        if (this.mWifiP2pServiceImpl == null || !(this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            HwHiLog.w(TAG, false, "setWifiRepeaterDisassociateSta called when mWifiP2pServiceImpl is null", new Object[0]);
            return;
        }
        HwWifiP2pService hwWifiP2pService = this.mWifiP2pServiceImpl;
        if (hwWifiP2pService.getWifiRepeaterTetherStarted()) {
            hwWifiP2pService.setWifiRepeaterDisassociateSta(mac);
        }
    }

    public void setWifiRepeaterMacFilter(String macFilter) {
        if (this.mWifiP2pServiceImpl == null || !(this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            HwHiLog.w(TAG, false, "setWifiRepeaterMacFilter called when mWifiP2pServiceImpl is null", new Object[0]);
            return;
        }
        HwWifiP2pService hwWifiP2pService = this.mWifiP2pServiceImpl;
        if (hwWifiP2pService.getWifiRepeaterTetherStarted()) {
            hwWifiP2pService.setWifiRepeaterMacFilter(macFilter);
        }
    }

    public void confirmWifiRepeater(int mode, IWifiRepeaterConfirmListener listener) {
        if (listener == null) {
            HwHiLog.w(TAG, false, "confirmWifiRepeater called when listener is null", new Object[0]);
        } else {
            this.mWifiRepeaterDialog.confirmWifiRepeater(mode, listener);
        }
    }

    public boolean isFeatureSupported(int feature, int ifaceType) {
        if (feature == 1) {
            return isWideBandwidthSupported(ifaceType);
        }
        return false;
    }

    private boolean isWideBandwidthSupported(int ifaceType) {
        if (ifaceType == 1 || ifaceType == 3) {
            byte[] buff = {0};
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            if (wifiNative == null || wifiNative.mHwWifiNativeEx.sendCmdToDriver("wlan0", 112, buff) != 1) {
                return false;
            }
            if (ifaceType == 3 && HwMSSUtils.is1103()) {
                return false;
            }
        } else if (ifaceType != 2 && ifaceType != 4) {
            return false;
        } else {
            if (!HwMSSUtils.is1103() && !HwMSSUtils.is1105()) {
                return false;
            }
        }
        int[] channels = this.mSoftApChannelXmlParse.getVht160Channels(WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver());
        return channels != null && channels.length > 0;
    }

    public boolean reduceTxPower(int action) {
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (wifiNative != null && wifiNative.mHwWifiNativeEx.sendCmdToDriver("wlan0", 113, new byte[]{(byte) action}) == 0) {
            return true;
        }
        return false;
    }

    public int getApBandwidth() {
        int ret;
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (wifiNative == null || (ret = wifiNative.mHwWifiNativeEx.sendCmdToDriver("wlan0", 114, new byte[]{0})) < 0) {
            return 0;
        }
        HwPhoneCloneChr phoneCloneChr = HwPhoneCloneChr.getInstance();
        if (phoneCloneChr != null) {
            phoneCloneChr.setResponseBandWidth((byte) ret);
        }
        return ret;
    }

    public int[] getSoftApWideBandWidthChannels() {
        return this.mSoftApChannelXmlParse.getVht160Channels(WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver());
    }

    public boolean doArpTest(int type, Inet4Address address) {
        if (type == 1) {
            return this.mArpClient.doFastArpTest(address);
        }
        if (type == 2) {
            return this.mArpClient.doSlowArpTest(address);
        }
        loge("reportArpResult: invalid arp type");
        return false;
    }

    public boolean shouldUseFactoryMac(WifiConfiguration config) {
        if (config == null || config.networkId == -1) {
            Log.e(TAG, "randommac, parameter error, config is invalid");
            return false;
        }
        WifiConfigManager wifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        if (wifiConfigManager == null) {
            Log.e(TAG, "randommac, get wifiConfigManager failed, wifiConfigManager is null");
            return false;
        } else if (!shouldRandomizedMacTakeEffect(config)) {
            return false;
        } else {
            if (this.mLastConnectNetworkId != config.networkId) {
                this.mLastConnectNetworkId = config.networkId;
                this.mConnectFailedCnt = 0;
            }
            Log.i(TAG, "randommac, networkId: " + config.networkId + ", mRandomizedMacSuccessEver: " + config.mRandomizedMacSuccessEver + ", mConnectFailedCnt: " + this.mConnectFailedCnt);
            if (wifiConfigManager.isRetreatAlgorithmOn() && !config.mRandomizedMacSuccessEver && !WifiConfigurationUtil.isConfigForOpenNetwork(config) && this.mConnectFailedCnt == 2) {
                Log.i(TAG, "randommac, use factory mac to connect");
                this.mRandomMacState = 1;
                uploadRandomMacInfo(config.SSID, this.mRandomMacState);
                return true;
            } else if (config.isReassocSelfcureWithFactoryMacAddress || this.mIsReassocUseWithFactoryMacAddress) {
                Log.i(TAG, "randommac, use factory mac to reassoc");
                return true;
            } else {
                Log.i(TAG, "randommac, use random mac to connect");
                return false;
            }
        }
    }

    public void updateRandomizedMacConfigWhenDisconnected(int networkId, int reasonCode) {
        if (networkId == -1) {
            Log.e(TAG, "randommac, networkId is invalid");
            return;
        }
        handleAssocRejectedWithFacMac(reasonCode);
        if (!shouldRandomMacSelfCure(reasonCode)) {
            Log.i(TAG, "randommac, this reasonCode do not need trigger self cure");
            return;
        }
        WifiConfigManager wifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        if (wifiConfigManager == null) {
            Log.e(TAG, "randommac, get wifiConfigManager failed, wifiConfigManager is null");
            return;
        }
        WifiConfiguration config = wifiConfigManager.getConfiguredNetwork(networkId);
        if (config == null) {
            Log.e(TAG, "randommac, get config from networkId failed, config is null");
        } else if (shouldRandomizedMacTakeEffect(config)) {
            Log.i(TAG, "randommac, wifi disconnect, config network id: " + config.networkId);
            if (this.mLastConnectNetworkId != config.networkId) {
                Log.e(TAG, "randommac, network id is not match, record network id: " + this.mLastConnectNetworkId);
            } else if (wifiConfigManager.isRetreatAlgorithmOn() && !config.mRandomizedMacSuccessEver && !WifiConfigurationUtil.isConfigForOpenNetwork(config)) {
                this.mConnectFailedCnt++;
                Log.i(TAG, "randommac, record network id: " + this.mLastConnectNetworkId + ", failed cnt: " + this.mConnectFailedCnt);
                if (this.mConnectFailedCnt <= 2) {
                    startConnectToNetwork(config.networkId, HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM, HwABSWiFiHandler.SUPPLICANT_BSSID_ANY);
                } else {
                    this.mConnectFailedCnt = 0;
                }
            }
        }
    }

    public void updateRandomizedMacConfigWhenConnected(String iface, String bssid) {
        if (iface == null || bssid == null) {
            Log.e(TAG, "randommac, parameter err, iface or bssid is null");
            return;
        }
        WifiConfigManager wifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        if (wifiConfigManager == null) {
            Log.e(TAG, "randommac, get wifiConfigManager failed, wifiConfigManager is null");
            return;
        }
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null || config.networkId == -1) {
            Log.e(TAG, "randommac, get current config failed, config is invalid");
        } else if (shouldRandomizedMacTakeEffect(config)) {
            Log.i(TAG, "randommac, wifi connected, config network id: " + config.networkId);
            this.mConnectFailedCnt = 0;
            boolean isRandomizedMac = currentIsRandomizedMac(iface);
            if (wifiConfigManager.isBssidAlgorithmOn() && isRandomizedMac) {
                wifiConfigManager.updateRandomizedMacAddressPlus(config, bssid);
                this.mRandomMacState = 0;
            }
            if (wifiConfigManager.isRetreatAlgorithmOn() && !config.mRandomizedMacSuccessEver && !WifiConfigurationUtil.isConfigForOpenNetwork(config)) {
                if (isRandomizedMac) {
                    config.mRandomizedMacSuccessEver = true;
                    if (!wifiConfigManager.addOrUpdateNetwork(config, 1000).isSuccess()) {
                        Log.e(TAG, "randommac, update config failed for network: " + config.networkId);
                    }
                } else {
                    this.mRandomMacState = 2;
                }
            }
            uploadRandomMacInfo(config.SSID, this.mRandomMacState);
        }
    }

    private boolean shouldRandomMacSelfCure(int reasonCode) {
        if (reasonCode == 16 || reasonCode == 17) {
            return false;
        }
        return true;
    }

    private boolean shouldRandomizedMacTakeEffect(WifiConfiguration config) {
        if (config != null && config.macRandomizationSetting == 1 && isConnectedMacRandomizationEnabled()) {
            return true;
        }
        return false;
    }

    private boolean currentIsRandomizedMac(String mInterfaceName) {
        WifiNative wifiNative = getWifiNative();
        if (wifiNative == null) {
            Log.e(TAG, "randommac, get wifiNative failed, wifiNative is null");
            return false;
        }
        MacAddress factoryMac = wifiNative.getFactoryMacAddress(mInterfaceName);
        if (factoryMac == null) {
            Log.e(TAG, "randommac, factory mac is null.");
            return false;
        } else if (!TextUtils.equals(wifiNative.getMacAddress(mInterfaceName), factoryMac.toString())) {
            Log.i(TAG, "randommac, current connect use random mac");
            return true;
        } else {
            Log.i(TAG, "randommac, current connect use factory mac");
            return false;
        }
    }

    private void uploadRandomMacInfo(String ssid, int randomMacState) {
        if (this.mHwWifiCHRService != null && ssid != null) {
            Bundle data = new Bundle();
            data.putString(KEY_RADOMMAC_SSID, ssid);
            data.putInt(KEY_RADOMMAC_STATE, randomMacState);
            this.mHwWifiCHRService.uploadDFTEvent(20, data);
        }
    }

    public boolean isDisallowedSelfRecovery() {
        return this.mIsInWifiSettings || this.mP2pGroupCreated.get();
    }

    /* access modifiers changed from: protected */
    public void enbaleWifichipCheck(boolean isEnable) {
        if (this.mWifichipCheck != null) {
            String action = isEnable ? "enable" : "disable";
            Log.i(TAG, action + " wifichip check.");
            this.mWifichipCheck.enableCheckAlarm(isEnable);
        }
    }

    public boolean isWifiInObtainingIpState() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (!isWifiProEnabled() || !HwWifiStateMachineEx.isWifiPoorLink() || !isMobileNetworkActive() || networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.OBTAINING_IPADDR) {
            return false;
        }
        return true;
    }

    public void dcConnect(WifiConfiguration configuration, IWifiActionListener listener) {
        DcMonitor dcMonitor = DcMonitor.getInstance();
        if (dcMonitor != null) {
            dcMonitor.dcConnect(configuration, listener);
        }
    }

    public boolean dcDisconnect() {
        DcMonitor dcMonitor = DcMonitor.getInstance();
        if (dcMonitor != null) {
            return dcMonitor.dcDisconnect();
        }
        return false;
    }

    private void setWifiEnabled(boolean isWifiEnabled) {
        WifiManager wifiManager = (WifiManager) this.myContext.getSystemService("wifi");
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "setWifiEnabled wifiManager is null", new Object[0]);
            return;
        }
        int wifiState = syncGetWifiState();
        HwHiLog.i(TAG, false, "setWifiEnabled state = %{public}d", new Object[]{Integer.valueOf(wifiState)});
        if (isWifiEnabled) {
            if (wifiState == 1 && (this.mWifiMode & 8) != 0) {
                wifiManager.setWifiEnabled(isWifiEnabled);
            }
        } else if (wifiState == 3 && (this.mWifiMode & 8) != 0) {
            this.mWifiMode = 0;
            wifiManager.setWifiEnabled(isWifiEnabled);
        }
    }

    public boolean setWifiMode(String packageName, int mode) {
        HwHiLog.i(TAG, false, "setWifiMode packageName=%{public}s mode=%{public}d", new Object[]{packageName, Integer.valueOf(mode)});
        if (packageName == null) {
            return false;
        }
        if (packageName.contains("com.huawei.android.instantshare") || packageName.contains("com.huawei.nearby")) {
            try {
                StringBuffer stringBuffer = new StringBuffer("com.huawei.nearby");
                stringBuffer.append(packageName.substring(packageName.lastIndexOf(47)));
                packageName = stringBuffer.toString();
            } catch (StringIndexOutOfBoundsException e) {
                return false;
            }
        }
        int realMode = Integer.MAX_VALUE & mode;
        if ("android".equals(packageName) || ((realMode & 7) == 0 && packageName.equals(this.mWifiModeCallerPackageName))) {
            boolean isActionBitSet = (Integer.MIN_VALUE & mode) != 0;
            boolean isSending = SystemPropertiesEx.getBoolean("instantshare.sending", false);
            HwHiLog.i(TAG, false, "isSending=%{public}s, wifiMode=%{public}d, isActionBitSet=%{public}s", new Object[]{String.valueOf(isSending), Integer.valueOf(this.mWifiMode), String.valueOf(isActionBitSet)});
            if (!isActionBitSet && !isSending && this.mHwSoftApManager == null) {
                setWifiEnabled(false);
            }
            int i = this.mWifiMode;
            if (i > 0) {
                this.mShouldStartNewScan = (i & 1) != 0;
            }
            this.mWifiMode = 0;
            removeAssistantTimeoutMessage();
            if (this.mWifiConnectivityManager != null && this.mShouldStartNewScan && "android".equals(packageName)) {
                HwHiLog.i(TAG, false, "setWifiMode: start a new scan when reset wifi mode", new Object[0]);
                this.mShouldStartNewScan = false;
                this.mWifiConnectivityManager.startConnectivityScan(true, false);
            }
            this.mWifiModeCallerPackageName = "";
        } else if ((realMode & 7) != 0) {
            if (TextUtils.isEmpty(this.mWifiModeCallerPackageName) || packageName.equals(this.mWifiModeCallerPackageName)) {
                this.mShouldStartNewScan = false;
                if ((realMode & 1) != 0) {
                    disconnectCommand();
                }
                this.mWifiMode |= realMode;
                this.mWifiModeCallerPackageName = packageName;
                setWifiEnabled(true);
            } else {
                HwHiLog.i(TAG, false, "packageName is not the same", new Object[0]);
                return false;
            }
        }
        return true;
    }

    public int getWifiMode() {
        return this.mWifiMode;
    }

    public String getWifiModeCallerPackageName() {
        return this.mWifiModeCallerPackageName;
    }

    public void sendAssistantTimeoutMessage() {
        HwHiLog.i(TAG, false, "sendAssistantTimeoutMessage", new Object[0]);
        if (hasMessages(131899)) {
            removeMessages(131899);
        }
        sendMessageDelayed(131899, 90000);
    }

    public void removeAssistantTimeoutMessage() {
        HwHiLog.i(TAG, false, "removeAssistantTimeoutMessage", new Object[0]);
        if (hasMessages(131899)) {
            removeMessages(131899);
        }
    }

    public void setWifiP2pListenMode() {
        if (this.mWifiP2pServiceImpl != null && (this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            this.mWifiP2pServiceImpl.setWifiP2pListenMode();
        }
    }

    public void setStereoAudioWorkingFlag(boolean isActive) {
        if (this.mWifiP2pServiceImpl != null && (this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            this.mWifiP2pServiceImpl.setStereoAudioWorkingFlag(isActive);
        }
    }

    public boolean isStereoAudioWorking() {
        if (this.mWifiP2pServiceImpl == null || !(this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            return false;
        }
        return this.mWifiP2pServiceImpl.isStereoAudioWorking();
    }

    public void setP2pHighPerf(boolean isEnable) {
        HwHiLog.i(TAG, false, "setP2pHighPerf = %{public}s", new Object[]{String.valueOf(isEnable)});
        if (this.mWifiP2pServiceImpl != null && (this.mWifiP2pServiceImpl instanceof HwWifiP2pService)) {
            this.mWifiP2pServiceImpl.setP2pHighPerf(isEnable);
        }
    }

    public int handleInterferenceParams(int type, int value) {
        IHwWifiNativeEx wifiNativeEx = null;
        if (!(WifiInjector.getInstance() == null || WifiInjector.getInstance().getWifiNative() == null)) {
            wifiNativeEx = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
        }
        if (wifiNativeEx != null) {
            return wifiNativeEx.sendCmdToDriver("wlan0", 147, new byte[]{(byte) type, (byte) (type >> 8), (byte) (type >> 16), (byte) (type >> 24), (byte) value, (byte) (value >> 8), (byte) (value >> 16), (byte) (value >> 24)});
        }
        return -1;
    }

    public boolean canReportWifiScoreDelayed() {
        if (!isWifiProEnabled() || this.mCurrNetworkHistoryInserted) {
            return false;
        }
        int score = HwServiceFactory.getHwConnectivityManager().getNetworkAgentInfoScore();
        HwHiLog.i(TAG, false, "score = %{public}d", new Object[]{Integer.valueOf(score)});
        if (score == 0) {
            return true;
        }
        return false;
    }

    public boolean queryCsi(MacAddress targetMac, boolean enable, int bwMask, int frMask) {
        if (targetMac == null) {
            return false;
        }
        IHwWifiNativeEx wifiNativeEx = null;
        if (!(WifiInjector.getInstance() == null || WifiInjector.getInstance().getWifiNative() == null)) {
            wifiNativeEx = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx;
        }
        if (wifiNativeEx != null && wifiNativeEx.sendCmdToDriver("wlan0", (int) CMD_QUERY_CSI, String.format(Locale.ROOT, "%s %d %d %d 0 0", targetMac.toString(), Integer.valueOf(enable ? 1 : 0), Integer.valueOf(bwMask), Integer.valueOf(frMask)).getBytes(StandardCharsets.UTF_8)) == 0) {
            return true;
        }
        return false;
    }
}
