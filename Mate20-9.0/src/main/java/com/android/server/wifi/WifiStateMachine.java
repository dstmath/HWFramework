package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.dhcp.DhcpClient;
import android.net.ip.IpClient;
import android.net.wifi.IClientInterface;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.system.OsConstants;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiBackupRestore;
import com.android.server.wifi.WifiMulticastLockManager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.StringUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.android.server.wifi.wificond.NativeMssResult;
import huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiStateMachine extends AbsWifiStateMachine {
    private static final long ALLOW_SEND_HILINK_SCAN_RESULTS_BROADCAST_INTERVAL_MS = 3000;
    static final int BASE = 131072;
    private static final String BSSID_TO_CONNECT = "bssid_to_connect";
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG = 131178;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    static final int CMD_BOOT_COMPLETED = 131206;
    public static final int CMD_CHANGE_TO_AP_P2P_CONNECT = 131574;
    public static final int CMD_CHANGE_TO_STA_P2P_CONNECT = 131573;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    private static final int CMD_DIAGS_CONNECT_TIMEOUT = 131324;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    public static final int CMD_DISABLE_P2P_REQ = 131204;
    public static final int CMD_DISABLE_P2P_RSP = 131205;
    static final int CMD_DISABLE_P2P_WATCHDOG_TIMER = 131184;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_ENABLE_NETWORK = 131126;
    public static final int CMD_ENABLE_P2P = 131203;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_GET_ALL_MATCHING_CONFIGS = 131240;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_CONFIG = 131171;
    static final int CMD_GET_MATCHING_OSU_PROVIDERS = 131181;
    static final int CMD_GET_PASSPOINT_CONFIGS = 131180;
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS = 131134;
    static final int CMD_GET_SUPPORTED_FEATURES = 131133;
    static final int CMD_GET_SUPPORT_VOWIFI_DETECT = 131774;
    static final int CMD_INITIALIZE = 131207;
    static final int CMD_INSTALL_PACKET_FILTER = 131274;
    static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    static final int CMD_IP_CONFIGURATION_LOST = 131211;
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    static final int CMD_IP_REACHABILITY_LOST = 131221;
    static final int CMD_MATCH_PROVIDER_NETWORK = 131177;
    static final int CMD_NETWORK_STATUS = 131220;
    static final int CMD_PNO_PERIODIC_SCAN = 131575;
    static final int CMD_QUERY_OSU_ICON = 131176;
    static final int CMD_READ_PACKET_FILTER = 131280;
    static final int CMD_REASSOCIATE = 131147;
    static final int CMD_RECONNECT = 131146;
    static final int CMD_RELOAD_TLS_AND_RECONNECT = 131214;
    static final int CMD_REMOVE_APP_CONFIGURATIONS = 131169;
    static final int CMD_REMOVE_NETWORK = 131125;
    static final int CMD_REMOVE_PASSPOINT_CONFIG = 131179;
    static final int CMD_REMOVE_USER_CONFIGURATIONS = 131224;
    static final int CMD_RESET_SIM_NETWORKS = 131173;
    static final int CMD_RESET_SUPPLICANT_STATE = 131183;
    static final int CMD_ROAM_WATCHDOG_TIMER = 131166;
    static final int CMD_RSSI_POLL = 131155;
    static final int CMD_RSSI_THRESHOLD_BREACHED = 131236;
    public static final int CMD_SCE_HANDLE_IP_INVALID = 131895;
    public static final int CMD_SCE_HANDLE_IP_NO_INTERNET = 131898;
    public static final int CMD_SCE_NOTIFY_WIFI_DISABLED = 131894;
    public static final int CMD_SCE_RESTORE = 131893;
    public static final int CMD_SCE_STOP_SELF_CURE = 131892;
    public static final int CMD_SCE_WIFI_CONNECT_TIMEOUT = 131890;
    public static final int CMD_SCE_WIFI_OFF_TIMEOUT = 131888;
    public static final int CMD_SCE_WIFI_ON_TIMEOUT = 131889;
    public static final int CMD_SCE_WIFI_REASSOC_TIMEOUT = 131891;
    public static final int CMD_SCE_WIFI_RECONNECT_TIMEOUT = 131896;
    static final int CMD_SCREEN_STATE_CHANGED = 131167;
    static final int CMD_SET_DETECTMODE_CONF = 131772;
    static final int CMD_SET_DETECT_PERIOD = 131773;
    static final int CMD_SET_FALLBACK_PACKET_FILTERING = 131275;
    static final int CMD_SET_HIGH_PERF_MODE = 131149;
    static final int CMD_SET_OPERATIONAL_MODE = 131144;
    static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    static final int CMD_START_CONNECT = 131215;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_ROAM = 131217;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    private static final int CMD_START_SUBSCRIPTION_PROVISIONING = 131326;
    static final int CMD_STATIC_IP_FAILURE = 131088;
    static final int CMD_STATIC_IP_SUCCESS = 131087;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    static final int CMD_TARGET_BSSID = 131213;
    static final int CMD_TEST_NETWORK_DISCONNECT = 131161;
    static final int CMD_UNWANTED_NETWORK = 131216;
    static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    public static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    static final int CMD_USER_STOP = 131279;
    static final int CMD_USER_SWITCH = 131277;
    static final int CMD_USER_UNLOCK = 131278;
    public static final int CMD_WIFI_SCAN_REJECT_SEND_SCAN_RESULT = 131578;
    static final int CMD_WPS_PIN_RETRY = 131576;
    private static final String CONNECT_FROM_USER = "connect_from_user";
    public static final int CONNECT_MODE = 1;
    private static final int CONNECT_REQUEST_DELAY_MSECS = 50;
    /* access modifiers changed from: private */
    public static boolean DBG = HWFLOW;
    private static final int DEFAULT_MTU = 1500;
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60000;
    public static final int DISABLED_MODE = 4;
    static final int DISABLE_P2P_GUARD_TIMER_MSEC = 2000;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final int DRIVER_STARTED = 1;
    private static final int DRIVER_STOPPED = 2;
    /* access modifiers changed from: private */
    public static final boolean ENABLE_DHCP_AFTER_ROAM = SystemProperties.getBoolean("ro.config.roam_force_dhcp", false);
    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final String EXTRA_OSU_PROVIDER = "OsuProvider";
    private static final int FAILURE = -1;
    public static final int GOOD_LINK_DETECTED = 131874;
    private static final String GOOGLE_OUI = "DA-A1-19";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
    private static final int IMSI_RECONNECT_LIMIT = 3;
    public static final int INVALID_LINK_DETECTED = 131875;
    private static final long LAST_AUTH_FAILURE_GAP = 100;
    @VisibleForTesting
    public static final int LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS = 30000;
    private static final int LINK_FLAPPING_DEBOUNCE_MSEC = 4000;
    private static final String LOGD_LEVEL_DEBUG = "D";
    private static final String LOGD_LEVEL_VERBOSE = "V";
    private static final int MESSAGE_HANDLING_STATUS_DEFERRED = -4;
    private static final int MESSAGE_HANDLING_STATUS_DISCARD = -5;
    private static final int MESSAGE_HANDLING_STATUS_FAIL = -2;
    private static final int MESSAGE_HANDLING_STATUS_HANDLING_ERROR = -7;
    private static final int MESSAGE_HANDLING_STATUS_LOOPED = -6;
    private static final int MESSAGE_HANDLING_STATUS_OBSOLETE = -3;
    private static final int MESSAGE_HANDLING_STATUS_OK = 1;
    private static final int MESSAGE_HANDLING_STATUS_PROCESSED = 2;
    private static final int MESSAGE_HANDLING_STATUS_REFUSED = -1;
    private static final int MESSAGE_HANDLING_STATUS_UNKNOWN = 0;
    private static final String NETWORKTYPE = "WIFI";
    private static final String NETWORKTYPE_UNTRUSTED = "WIFI_UT";
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    private static final int NET_ID_NONE = -1;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_NORMAL = 100;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE = 3000;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = 200;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static boolean PDBG = HWFLOW;
    public static final int POOR_LINK_DETECTED = 131873;
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    public static final int SCAN_ONLY_MODE = 2;
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
    public static final int SCE_EVENT_CONN_CHANGED = 103;
    public static final int SCE_EVENT_NET_INFO_CHANGED = 102;
    public static final int SCE_EVENT_WIFI_STATE_CHANGED = 101;
    public static final int SCE_REQUEST_REASSOC_WIFI = 131886;
    public static final int SCE_REQUEST_RENEW_DHCP = 131883;
    public static final int SCE_REQUEST_RESET_WIFI = 131887;
    public static final int SCE_REQUEST_SET_STATIC_IP = 131884;
    public static final int SCE_REQUEST_UPDATE_DNS_SERVER = 131882;
    public static final int SCE_START_SET_STATIC_IP = 131885;
    private static final String SOFTAP_IFACE = "wlan0";
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_BSSID_ANY = "any";
    private static final int SUPPLICANT_RESTART_INTERVAL_MSECS = 5000;
    private static final int SUPPLICANT_RESTART_TRIES = 5;
    private static final int SUSPEND_DUE_TO_DHCP = 1;
    private static final int SUSPEND_DUE_TO_HIGH_PERF = 2;
    private static final int SUSPEND_DUE_TO_SCREEN = 4;
    private static final String SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL = "log.tag.WifiHAL";
    private static final String TAG = "WifiStateMachine";
    private static boolean USE_PAUSE_SCANS = false;
    /* access modifiers changed from: private */
    public static boolean VDBG = false;
    private static boolean VVDBG = false;
    public static final int WIFIPRO_SOFT_CONNECT_TIMEOUT = 131897;
    private static final String WIFI_DRIVER_CHANGE_ACTION = "huawei.intent.action.WIFI_DRIVER_CHANGE";
    private static final String WIFI_DRIVER_CHANGE_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final String WIFI_DRIVER_STATE = "wifi_driver_state";
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static final int WPS_PIN_RETRY_INTERVAL_MSECS = 50000;
    private static boolean mLogMessages = HWFLOW;
    private static final Class[] sMessageClasses = {AsyncChannel.class, WifiStateMachine.class, DhcpClient.class};
    private static int sScanAlarmIntentCount = 0;
    private static final SparseArray<String> sSmToString = MessageUtils.findMessageNames(sMessageClasses);
    /* access modifiers changed from: private */
    public boolean didBlackListBSSID = false;
    int disconnectingWatchdogCount = 0;
    /* access modifiers changed from: private */
    public boolean isBootCompleted = false;
    /* access modifiers changed from: private */
    public long lastConnectAttemptTimestamp = 0;
    private long lastLinkLayerStatsUpdate = 0;
    private long lastOntimeReportTimeStamp = 0;
    private Set<Integer> lastScanFreqs = null;
    private long lastScreenStateChangeTimeStamp = 0;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public boolean mBluetoothConnectionActive = false;
    private final BuildProperties mBuildProperties;
    private IClientInterface mClientInterface;
    private ClientModeManager.Listener mClientModeCallback = null;
    /* access modifiers changed from: private */
    public final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    /* access modifiers changed from: private */
    public State mConnectedState = new ConnectedState();
    private long mConnectingStartTimestamp = 0;
    /* access modifiers changed from: private */
    @GuardedBy("mWifiReqCountLock")
    public int mConnectionReqCount = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public final WifiCountryCode mCountryCode;
    private int mCurrentAssociateNetworkId = -1;
    HwCustWifiStateMachineReference mCust = ((HwCustWifiStateMachineReference) HwCustUtils.createObj(HwCustWifiStateMachineReference.class, new Object[0]));
    /* access modifiers changed from: private */
    public State mDefaultState = new DefaultState();
    private final NetworkCapabilities mDfltNetworkCapabilities;
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private long mDiagsConnectionStartMillis = -1;
    int mDisableP2pWatchdogCount = 0;
    /* access modifiers changed from: private */
    public State mDisconnectedState = new DisconnectedState();
    /* access modifiers changed from: private */
    public long mDisconnectedTimeStamp = 0;
    /* access modifiers changed from: private */
    public State mDisconnectingState = new DisconnectingState();
    /* access modifiers changed from: private */
    public AtomicBoolean mEnableConnectedMacRandomization = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public boolean mEnableRssiPolling = false;
    /* access modifiers changed from: private */
    public FrameworkFacade mFacade;
    /* access modifiers changed from: private */
    public int mFeatureSet = 0;
    private HwMSSHandlerManager mHwMssHandler;
    /* access modifiers changed from: private */
    public HwWifiCHRService mHwWifiCHRService;
    /* access modifiers changed from: private */
    public String mInterfaceName;
    /* access modifiers changed from: private */
    public IpClient mIpClient;
    /* access modifiers changed from: private */
    public boolean mIpReachabilityDisconnectEnabled = true;
    /* access modifiers changed from: private */
    public boolean mIsAutoRoaming = false;
    private boolean mIsFactoryFirstEnter = true;
    /* access modifiers changed from: private */
    public boolean mIsImsiAvailable = true;
    public boolean mIsRandomMacCleared = false;
    /* access modifiers changed from: private */
    public boolean mIsRealReboot = false;
    /* access modifiers changed from: private */
    public boolean mIsRunning = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private long mLastAllowSendHiLinkScanResultsBroadcastTime = 0;
    /* access modifiers changed from: private */
    public long mLastAuthFailureTimestamp = Long.MIN_VALUE;
    /* access modifiers changed from: private */
    public String mLastBssid;
    private volatile WifiConfiguration mLastConnectConfig = null;
    /* access modifiers changed from: private */
    public long mLastDriverRoamAttempt = 0;
    /* access modifiers changed from: private */
    public int mLastNetworkId;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    private int mLastSignalLevel = -1;
    /* access modifiers changed from: private */
    public LinkProperties mLinkProperties;
    private final McastLockManagerFilterController mMcastLockManagerFilterController;
    private boolean mModeChange = false;
    /* access modifiers changed from: private */
    public WifiNetworkAgent mNetworkAgent;
    private final Object mNetworkAgentLock = new Object();
    /* access modifiers changed from: private */
    public final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private WifiNetworkFactory mNetworkFactory;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    /* access modifiers changed from: private */
    public final NetworkMisc mNetworkMisc = new NetworkMisc();
    private INetworkManagementService mNwService;
    /* access modifiers changed from: private */
    public State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    /* access modifiers changed from: private */
    public int mOperationalMode = 4;
    /* access modifiers changed from: private */
    public final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    /* access modifiers changed from: private */
    public final PasspointManager mPasspointManager;
    private int mPeriodicScanToken = 0;
    /* access modifiers changed from: private */
    public volatile int mPollRssiIntervalMsecs = DEFAULT_POLL_RSSI_INTERVAL_MSECS;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    /* access modifiers changed from: private */
    public int mRoamFailCount = 0;
    /* access modifiers changed from: private */
    public State mRoamingState = new RoamingState();
    /* access modifiers changed from: private */
    public int mRssiPollToken = 0;
    /* access modifiers changed from: private */
    public byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    /* access modifiers changed from: private */
    public final SarManager mSarManager;
    /* access modifiers changed from: private */
    public ScanRequestProxy mScanRequestProxy;
    /* access modifiers changed from: private */
    public boolean mScreenOn = false;
    private long mSupplicantScanIntervalMs;
    /* access modifiers changed from: private */
    public SupplicantStateTracker mSupplicantStateTracker;
    private int mSuspendOptNeedsDisabled = 0;
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mSuspendWakeLock;
    /* access modifiers changed from: private */
    public int mTargetNetworkId = -1;
    /* access modifiers changed from: private */
    public String mTargetRoamBSSID = "any";
    /* access modifiers changed from: private */
    public final String mTcpBufferSizes;
    private TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public boolean mTemporarilyDisconnectWifi = false;
    private String mTls12ConfKey = null;
    /* access modifiers changed from: private */
    public int mTrackEapAuthFailCount = 0;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    /* access modifiers changed from: private */
    @GuardedBy("mWifiReqCountLock")
    public int mUntrustedReqCount = 0;
    /* access modifiers changed from: private */
    public AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    /* access modifiers changed from: private */
    public boolean mVerboseLoggingEnabled = false;
    private int mVerboseLoggingLevel = 0;
    private PowerManager.WakeLock mWakeLock;
    private final AtomicInteger mWifiApState = new AtomicInteger(1);
    /* access modifiers changed from: private */
    public WifiConfigManager mWifiConfigManager;
    protected WifiConnectivityManager mWifiConnectivityManager;
    /* access modifiers changed from: private */
    public BaseWifiDiagnostics mWifiDiagnostics;
    /* access modifiers changed from: private */
    public final ExtendedWifiInfo mWifiInfo;
    /* access modifiers changed from: private */
    public WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public WifiMetrics mWifiMetrics;
    private WifiMonitor mWifiMonitor;
    /* access modifiers changed from: private */
    public WifiNative mWifiNative;
    /* access modifiers changed from: private */
    public AsyncChannel mWifiP2pChannel;
    private WifiP2pServiceImpl mWifiP2pServiceImpl;
    /* access modifiers changed from: private */
    public WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    /* access modifiers changed from: private */
    public WifiRepeater mWifiRepeater;
    /* access modifiers changed from: private */
    public final Object mWifiReqCountLock = new Object();
    /* access modifiers changed from: private */
    public final WifiScoreReport mWifiScoreReport;
    private WifiSettingsStore mWifiSettingStore;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    public WifiStateMachineHisiExt mWifiStateMachineHisiExt = null;
    /* access modifiers changed from: private */
    public WifiStateTracker mWifiStateTracker;
    /* access modifiers changed from: private */
    public State mWpsRunningState = new WpsRunningState();
    private final WrongPasswordNotifier mWrongPasswordNotifier;
    /* access modifiers changed from: private */
    public int messageHandlingStatus = 0;
    int roamWatchdogCount = 0;
    /* access modifiers changed from: private */
    public WifiConfiguration targetWificonfiguration = null;
    /* access modifiers changed from: private */
    public boolean testNetworkDisconnect = false;
    /* access modifiers changed from: private */
    public int testNetworkDisconnectCounter = 0;
    /* access modifiers changed from: private */
    public DataUploader uploader;

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            Log.d(WifiStateMachine.TAG, "entering ConnectModeState: ifaceName = " + WifiStateMachine.this.mInterfaceName);
            int unused = WifiStateMachine.this.mOperationalMode = 1;
            WifiStateMachine.this.setupClientMode();
            if (!WifiStateMachine.this.mWifiNative.removeAllNetworks(WifiStateMachine.this.mInterfaceName)) {
                WifiStateMachine.this.loge("Failed to remove networks on entering connect mode");
            }
            WifiStateMachine.this.mScanRequestProxy.enableScanningForHiddenNetworks(true);
            WifiStateMachine.this.mWifiInfo.reset();
            WifiStateMachine.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            WifiStateMachine.this.mWifiInjector.getWakeupController().reset();
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(true);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            boolean unused2 = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
            boolean unused3 = WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
            WifiStateMachine.this.mSarManager.setClientWifiState(3);
        }

        public void exit() {
            int unused = WifiStateMachine.this.mOperationalMode = 4;
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(false);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(1);
            WifiStateMachine.this.mSarManager.setClientWifiState(1);
            if (!WifiStateMachine.this.mWifiNative.removeAllNetworks(WifiStateMachine.this.mInterfaceName)) {
                WifiStateMachine.this.loge("Failed to remove networks on exiting connect mode");
            }
            WifiStateMachine.this.mScanRequestProxy.enableScanningForHiddenNetworks(false);
            WifiStateMachine.this.mScanRequestProxy.clearScanResults();
            WifiStateMachine.this.mWifiInfo.reset();
            WifiStateMachine.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            WifiStateMachine.this.stopClientMode();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:170:0x05da, code lost:
            if (com.android.server.wifi.WifiStateMachine.access$7100(r1.this$0).SSID.equals("\"" + r7 + "\"") != false) goto L_0x05dc;
         */
        public boolean processMessage(Message message) {
            boolean ok;
            Message message2 = message;
            WifiStateMachine.this.logStateAndMessage(message2, this);
            if (WifiStateMachine.this.handleWapiFailureEvent(message2, WifiStateMachine.this.mSupplicantStateTracker)) {
                return true;
            }
            int i = -1;
            int i2 = 2;
            boolean enabled = false;
            switch (message2.what) {
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (message2.arg1 != 0) {
                        enabled = true;
                    }
                    boolean unused = wifiStateMachine.mBluetoothConnectionActive = enabled;
                    WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mInterfaceName, WifiStateMachine.this.mBluetoothConnectionActive);
                    break;
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    if (WifiStateMachine.this.deleteNetworkConfigAndSendReply(message2, false)) {
                        int netId = message2.arg1;
                        if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            break;
                        }
                    } else {
                        int unused2 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                        break;
                    }
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    boolean disableOthers = message2.arg2 == 1;
                    int netId2 = message2.arg1;
                    WifiConfiguration config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(netId2);
                    if (disableOthers) {
                        ok = WifiStateMachine.this.connectToUserSelectNetwork(netId2, message2.sendingUid, false);
                        WifiStateMachine.this.saveConnectingNetwork(config, netId2, false);
                    } else if (!WifiStateMachine.this.processConnectModeSetMode(message2)) {
                        ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(netId2, false, message2.sendingUid);
                    }
                    boolean ok2 = ok;
                    if (!ok2) {
                        int unused3 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                    int i3 = message2.what;
                    if (ok2) {
                        i = 1;
                    }
                    wifiStateMachine2.replyToMessage(message2, i3, i);
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.getWifiLinkLayerStats());
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                    WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan((WorkSource) message2.obj);
                    break;
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    long unused4 = WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                    WifiStateMachine.this.log("ConnectModeState, case CMD_REASSOCIATE, do reassociate");
                    WifiStateMachine.this.mWifiNative.reassociate(WifiStateMachine.this.mInterfaceName);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message2.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(2, true);
                        break;
                    } else {
                        WifiStateMachine.this.setSuspendOptimizationsNative(2, false);
                        break;
                    }
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message2.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(4, false);
                        break;
                    } else {
                        WifiStateMachine.this.setSuspendOptimizationsNative(4, true);
                        if (message2.arg2 == 1) {
                            WifiStateMachine.this.mSuspendWakeLock.release();
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_TDLS /*131164*/:
                    if (message2.obj != null) {
                        String remoteAddress = (String) message2.obj;
                        if (message2.arg1 == 1) {
                            enabled = true;
                        }
                        WifiStateMachine.this.mWifiNative.startTdls(WifiStateMachine.this.mInterfaceName, remoteAddress, enabled);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    Set<Integer> removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message2.obj);
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                    WifiConfiguration config2 = WifiStateMachine.this.mWifiConfigManager.disableEphemeralNetwork((String) message2.obj);
                    if (config2 != null && (config2.networkId == WifiStateMachine.this.mTargetNetworkId || config2.networkId == WifiStateMachine.this.mLastNetworkId)) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.mPasspointManager.getMatchingWifiConfig((ScanResult) message2.obj));
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (message2.arg1 == 1) {
                        Log.d(WifiStateMachine.TAG, "enable EAP-SIM/AKA/AKA' networks since SIM was loaded");
                        WifiStateMachine.this.mWifiConfigManager.enableSimNetworks();
                    }
                    WifiStateMachine.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    WifiConfigManager access$800 = WifiStateMachine.this.mWifiConfigManager;
                    if (message2.arg1 == 1) {
                        enabled = true;
                    }
                    access$800.resetSimNetworks(enabled);
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                    WifiStateMachine.this.mPasspointManager.queryPasspointIcon(((Bundle) message2.obj).getLong("BSSID"), ((Bundle) message2.obj).getString(WifiStateMachine.EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, 0);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*131178*/:
                    PasspointConfiguration passpointConfig = (PasspointConfiguration) message2.obj;
                    if (!WifiStateMachine.this.mPasspointManager.addOrUpdateProvider(passpointConfig, message2.arg1)) {
                        WifiStateMachine.this.replyToMessage(message2, message2.what, -1);
                        break;
                    } else {
                        String fqdn = passpointConfig.getHomeSp().getFqdn();
                        if (WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mTargetNetworkId, fqdn) || WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mLastNetworkId, fqdn)) {
                            WifiStateMachine.this.logd("Disconnect from current network since its provider is updated");
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        }
                        WifiStateMachine.this.replyToMessage(message2, message2.what, 1);
                        break;
                    }
                case WifiStateMachine.CMD_REMOVE_PASSPOINT_CONFIG /*131179*/:
                    String fqdn2 = (String) message2.obj;
                    if (!WifiStateMachine.this.mPasspointManager.removeProvider(fqdn2)) {
                        WifiStateMachine.this.replyToMessage(message2, message2.what, -1);
                        break;
                    } else {
                        if (WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mTargetNetworkId, fqdn2) || WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mLastNetworkId, fqdn2)) {
                            WifiStateMachine.this.logd("Disconnect from current network since its provider is removed");
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        }
                        WifiStateMachine.this.replyToMessage(message2, message2.what, 1);
                        break;
                    }
                case WifiStateMachine.CMD_GET_MATCHING_OSU_PROVIDERS /*131181*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.mPasspointManager.getMatchingOsuProviders((ScanResult) message2.obj));
                    break;
                case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    boolean unused5 = WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                    break;
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                    if (message2.obj != null) {
                        String unused6 = WifiStateMachine.this.mTargetRoamBSSID = (String) message2.obj;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                    WifiConfiguration wifiConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if ((wifiConfig == null || wifiConfig.cloudSecurityCheck == 0) && (wifiConfig == null || (wifiConfig.allowedKeyManagement.get(2) && wifiConfig.allowedKeyManagement.get(3)))) {
                        WifiStateMachine.this.log("currentWifiConfiguration is EAP type or no currentWifiConfiguration");
                        if (WifiStateMachine.this.mWifiConfigManager.needsUnlockedKeyStore() && !WifiStateMachine.this.isConnected()) {
                            WifiStateMachine.this.logd("Reconnecting to give a chance to un-connected TLS networks");
                            WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                            long unused7 = WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                            WifiStateMachine.this.mWifiNative.reconnect(WifiStateMachine.this.mInterfaceName);
                            break;
                        }
                    }
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                    if (!WifiStateMachine.this.isHiLinkActive()) {
                        Bundle bundle = (Bundle) message2.obj;
                        boolean connectFromUser = bundle.getBoolean(WifiStateMachine.CONNECT_FROM_USER);
                        Log.d(WifiStateMachine.TAG, "connectFromUser =" + connectFromUser);
                        if (!WifiStateMachine.this.attemptAutoConnect() && !connectFromUser) {
                            WifiStateMachine.this.logd("SupplicantState is TransientState, refuse auto connect");
                            break;
                        } else {
                            int netId3 = message2.arg1;
                            int uid = message2.arg2;
                            String bssid = bundle.getString(WifiStateMachine.BSSID_TO_CONNECT);
                            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                                if (!WifiStateMachine.this.hasConnectionRequests()) {
                                    if (WifiStateMachine.this.mNetworkAgent != null) {
                                        if (!connectFromUser && !WifiStateMachine.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                                            WifiStateMachine.this.loge("CMD_START_CONNECT but no requests and connected, but app does not have sufficient permissions, bailing");
                                            break;
                                        }
                                    } else {
                                        WifiStateMachine.this.loge("CMD_START_CONNECT but no requests and not connected, bailing");
                                        break;
                                    }
                                }
                                WifiConfiguration config3 = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId3);
                                WifiStateMachine.this.logd("CMD_START_CONNECT sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId3) + " roam=" + Boolean.toString(WifiStateMachine.this.mIsAutoRoaming));
                                if (config3 != null) {
                                    if (connectFromUser && bssid != null && bssid.equals("any") && config3.BSSID != null && !isApInScanList(config3.BSSID)) {
                                        WifiStateMachine.this.logd("bssid not match, connect with ssid");
                                        config3.BSSID = null;
                                    }
                                    if (!WifiCommonUtils.doesNotWifiConnectRejectByCust(config3.getNetworkSelectionStatus(), config3.SSID, WifiStateMachine.this.mContext)) {
                                        if (!HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config3, false)) {
                                            if (!WifiStateMachine.this.mWifiConfigManager.isSimPresent()) {
                                                WifiStateMachine.this.handleSimAbsent(config3);
                                            }
                                            if (WifiStateMachine.this.isEnterpriseHotspot(config3)) {
                                                WifiStateMachine.this.logd(config3.SSID + "is enterprise hotspot ");
                                                String unused8 = WifiStateMachine.this.mTargetRoamBSSID = "any";
                                            }
                                            int unused9 = WifiStateMachine.this.mTargetNetworkId = netId3;
                                            boolean unused10 = WifiStateMachine.this.setTargetBssid(config3, bssid);
                                            if (WifiStateMachine.this.mEnableConnectedMacRandomization.get()) {
                                                WifiStateMachine.this.configureRandomizedMacAddress(config3);
                                            }
                                            WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress(WifiStateMachine.this.mInterfaceName));
                                            Log.i(WifiStateMachine.TAG, "Connecting with " + StringUtil.safeDisplayBssid(currentMacAddress) + " as the mac address");
                                            WifiStateMachine.this.reportConnectionAttemptStart(config3, WifiStateMachine.this.mTargetRoamBSSID, 5);
                                            WifiStateMachine.this.saveConnectingNetwork(config3, netId3, true);
                                            if (!WifiStateMachine.this.mWifiNative.connectToNetwork(WifiStateMachine.this.mInterfaceName, config3)) {
                                                WifiStateMachine.this.loge("CMD_START_CONNECT Failed to start connection to network " + config3);
                                                WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                                WifiStateMachine.this.replyToMessage(message2, 151554, 0);
                                                break;
                                            } else {
                                                WifiStateMachine.this.mWifiMetrics.logStaEvent(11, config3);
                                                long unused11 = WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                                                WifiConfiguration unused12 = WifiStateMachine.this.targetWificonfiguration = config3;
                                                boolean unused13 = WifiStateMachine.this.mIsAutoRoaming = false;
                                                if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                                                    break;
                                                }
                                            }
                                        } else {
                                            Log.w(WifiStateMachine.TAG, "CMD_START_CONNECT: MDM deny connect to restricted network!");
                                            break;
                                        }
                                    } else {
                                        Log.d(WifiStateMachine.TAG, "break CMD_START_CONNECT with WifiConnectRejectByCust");
                                        break;
                                    }
                                } else {
                                    WifiStateMachine.this.loge("CMD_START_CONNECT and no config, bail out...");
                                    break;
                                }
                            }
                        }
                    } else {
                        Log.d(WifiStateMachine.TAG, "HiLink is active, refuse auto connect");
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    int unused14 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    String someBssid = (String) message2.obj;
                    if (someBssid != null) {
                        ScanDetailCache scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(WifiStateMachine.this.mTargetNetworkId);
                        if (scanDetailCache != null) {
                            WifiStateMachine.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    Set<Integer> removedNetworkIds2 = WifiStateMachine.this.mWifiConfigManager.removeNetworksForUser(Integer.valueOf(message2.arg1).intValue());
                    if (removedNetworkIds2.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds2.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    int slot = message2.arg1;
                    int ret = WifiStateMachine.this.stopWifiIPPacketOffload(slot);
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /*131238*/:
                    WifiConnectivityManager wifiConnectivityManager = WifiStateMachine.this.mWifiConnectivityManager;
                    if (message2.arg1 == 1) {
                        enabled = true;
                    }
                    wifiConnectivityManager.enable(enabled);
                    break;
                case WifiStateMachine.CMD_GET_ALL_MATCHING_CONFIGS /*131240*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.mPasspointManager.getAllMatchingWifiConfigs((ScanResult) message2.obj));
                    break;
                case WifiStateMachine.CMD_CONFIG_ND_OFFLOAD /*131276*/:
                    if (message2.arg1 > 0) {
                        enabled = true;
                    }
                    WifiStateMachine.this.mWifiNative.configureNeighborDiscoveryOffload(WifiStateMachine.this.mInterfaceName, enabled);
                    break;
                case WifiStateMachine.CMD_START_SUBSCRIPTION_PROVISIONING /*131326*/:
                    if (WifiStateMachine.this.mPasspointManager.startSubscriptionProvisioning(message2.arg1, message.getData().getParcelable(WifiStateMachine.EXTRA_OSU_PROVIDER), (IProvisioningCallback) message2.obj)) {
                        enabled = true;
                    }
                    WifiStateMachine.this.replyToMessage(message2, message2.what, enabled ? 1 : 0);
                    break;
                case WifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS /*131672*/:
                    WifiStateMachine.this.updateWifiproWifiConfiguration(message2);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message2.arg1 != 1) {
                        WifiStateMachine.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do reconnect");
                        WifiStateMachine.this.mWifiNative.reconnect(WifiStateMachine.this.mInterfaceName);
                        boolean unused15 = WifiStateMachine.this.mTemporarilyDisconnectWifi = false;
                        break;
                    } else {
                        WifiStateMachine.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 5);
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        boolean unused16 = WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                        break;
                    }
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection established");
                    }
                    int unused17 = WifiStateMachine.this.mLastNetworkId = message2.arg1;
                    WifiStateMachine.this.mWifiConfigManager.clearRecentFailureReason(WifiStateMachine.this.mLastNetworkId);
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiStateMachine.this.mHwWifiCHRService.updateWIFIConfiguraionByConfig(WifiStateMachine.this.getCurrentWifiConfiguration());
                    }
                    String unused18 = WifiStateMachine.this.mLastBssid = (String) message2.obj;
                    if (WifiStateMachine.this.mLastNetworkId == -1) {
                        NetworkUpdateResult networkUpdateResult = WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                        if (networkUpdateResult != null) {
                            int unused19 = WifiStateMachine.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                        }
                    }
                    int reasonCode = message2.arg2;
                    WifiConfiguration config4 = WifiStateMachine.this.getCurrentWifiConfiguration();
                    WifiStateMachine.this.setLastConnectConfig(config4);
                    if (config4 == null) {
                        WifiStateMachine.this.logw("Connected to unknown networkId " + WifiStateMachine.this.mLastNetworkId + ", disconnecting...");
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    } else {
                        WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                        WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                        WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress(WifiStateMachine.this.mInterfaceName));
                        ScanDetailCache scanDetailCache2 = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(config4.networkId);
                        if (!(scanDetailCache2 == null || WifiStateMachine.this.mLastBssid == null)) {
                            ScanResult scanResult = scanDetailCache2.getScanResult(WifiStateMachine.this.mLastBssid);
                            if (scanResult != null) {
                                WifiStateMachine.this.mWifiInfo.setFrequency(scanResult.frequency);
                            }
                        }
                        WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, reasonCode);
                        WifiStateMachine.this.uploader.e(54, "{RT:6,SPEED:" + WifiStateMachine.this.mWifiInfo.getLinkSpeed() + "}");
                        if (!config4.isTempCreated && config4.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config4.enterpriseConfig.getEapMethod())) {
                            String anonymousIdentity = WifiStateMachine.this.mWifiNative.getEapAnonymousIdentity(WifiStateMachine.this.mInterfaceName);
                            if (anonymousIdentity != null) {
                                config4.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                            } else {
                                Log.d(WifiStateMachine.TAG, "Failed to get updated anonymous identity from supplicant, reset it in WifiConfiguration.");
                                config4.enterpriseConfig.setAnonymousIdentity(null);
                            }
                            WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config4, 1010);
                        }
                        WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                        WifiStateMachine.this.log("ConnectModeState, case WifiMonitor.NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                        break;
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("ConnectModeState: Network connection lost ");
                    }
                    if (WifiStateMachine.this.disassociatedReason(message2.arg2)) {
                        Log.d(WifiStateMachine.TAG, "DISABLED_DISASSOC_REASON for network " + WifiStateMachine.this.mTargetNetworkId + " is " + message2.arg2);
                        WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 16);
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message2);
                    if (state != SupplicantState.INTERFACE_DISABLED) {
                        if (state == SupplicantState.DISCONNECTED && WifiStateMachine.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                WifiStateMachine.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                            }
                            WifiStateMachine.this.handleNetworkDisconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        }
                        if (state == SupplicantState.COMPLETED) {
                            WifiStateMachine.this.mIpClient.confirmConfiguration();
                            WifiStateMachine.this.mWifiScoreReport.noteIpCheck();
                        }
                        StateChangeResult stateChangeResult = (StateChangeResult) message2.obj;
                        if (stateChangeResult != null) {
                            int disconnectId = stateChangeResult.networkId;
                            WifiConfiguration disconnectConfig = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(disconnectId);
                            if (disconnectConfig != null && disconnectConfig.getNetworkSelectionStatus().isNetworkEnabled() && disconnectConfig.getNetworkSelectionStatus().getDisableReasonCounter(3) > 0 && WifiStateMachine.this.mClock.getElapsedSinceBootMillis() - WifiStateMachine.this.mLastAuthFailureTimestamp < WifiStateMachine.LAST_AUTH_FAILURE_GAP && !WifiStateMachine.this.isConnected() && state == SupplicantState.DISCONNECTED) {
                                Log.d(WifiStateMachine.TAG, "start an immediate connection for network " + disconnectId);
                                WifiStateMachine.this.startConnectToNetwork(disconnectId, 1010, "any");
                                break;
                            }
                        }
                    } else {
                        return false;
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    int disableReason = 3;
                    int reasonCode2 = message2.arg1;
                    if (WifiStateMachine.this.isPermanentWrongPasswordFailure(WifiStateMachine.this.mTargetNetworkId, reasonCode2)) {
                        disableReason = 13;
                    } else if (reasonCode2 == 3) {
                        WifiStateMachine.this.handleEapAuthFailure(WifiStateMachine.this.mTargetNetworkId, message2.arg2);
                    }
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, disableReason);
                    WifiStateMachine.this.mWifiConfigManager.clearRecentFailureReason(WifiStateMachine.this.mTargetNetworkId);
                    long unused20 = WifiStateMachine.this.mLastAuthFailureTimestamp = WifiStateMachine.this.mClock.getElapsedSinceBootMillis();
                    WifiStateMachine.this.notifyWifiConnFailedInfo(WifiStateMachine.this.mTargetNetworkId, null, WifiServiceHisiExt.MIN_RSSI, 3, WifiStateMachine.this.mWifiConnectivityManager);
                    WifiStateMachine.this.reportConnectionAttemptEnd(3, 1);
                    if (WifiStateMachine.this.mCust != null && WifiStateMachine.this.mCust.isShowWifiAuthenticationFailurerNotification()) {
                        WifiStateMachine.this.mCust.handleWifiAuthenticationFailureEvent(WifiStateMachine.this.mContext, WifiStateMachine.this);
                    }
                    WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 2);
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY:
                    int netId4 = message2.arg2;
                    boolean identitySent = false;
                    if (WifiStateMachine.this.targetWificonfiguration != null && WifiStateMachine.this.targetWificonfiguration.networkId == netId4 && TelephonyUtil.isSimConfig(WifiStateMachine.this.targetWificonfiguration)) {
                        Pair<String, String> identityPair = TelephonyUtil.getSimIdentity(WifiStateMachine.this.getTelephonyManager(), new TelephonyUtil(), WifiStateMachine.this.targetWificonfiguration);
                        if (identityPair == null || identityPair.first == null) {
                            Log.e(WifiStateMachine.TAG, "Unable to retrieve identity from Telephony");
                        } else {
                            identitySent = WifiStateMachine.this.mWifiNative.simIdentityResponse(WifiStateMachine.this.mInterfaceName, netId4, (String) identityPair.first, (String) identityPair.second);
                        }
                    }
                    if (!identitySent) {
                        String ssid = (String) message2.obj;
                        if (!(WifiStateMachine.this.targetWificonfiguration == null || ssid == null || WifiStateMachine.this.targetWificonfiguration.SSID == null)) {
                            if (!WifiStateMachine.this.targetWificonfiguration.SSID.equals(ssid)) {
                                break;
                            }
                            if (WifiStateMachine.this.mTrackEapAuthFailCount >= 3) {
                                Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 9);
                                int unused21 = WifiStateMachine.this.mTrackEapAuthFailCount = 0;
                            } else if (!WifiStateMachine.this.mIsImsiAvailable) {
                                Log.d(WifiStateMachine.TAG, "sim is not available,updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 9);
                                int unused22 = WifiStateMachine.this.mTrackEapAuthFailCount = 0;
                            } else {
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 0);
                                int unused23 = WifiStateMachine.this.mTrackEapAuthFailCount = WifiStateMachine.this.mTrackEapAuthFailCount + 1;
                                Log.d(WifiStateMachine.TAG, "sim is not ready and retry mTrackEapAuthFailCount " + WifiStateMachine.this.mTrackEapAuthFailCount);
                            }
                        }
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 2);
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH:
                    WifiStateMachine.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    TelephonyUtil.SimAuthRequestData requestData = (TelephonyUtil.SimAuthRequestData) message2.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                WifiStateMachine.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        } else {
                            WifiStateMachine.this.handleGsmAuthRequest(requestData);
                            break;
                        }
                    } else {
                        WifiStateMachine.this.loge("Invalid sim auth request");
                        break;
                    }
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(1);
                    boolean unused24 = WifiStateMachine.this.didBlackListBSSID = false;
                    String bssid2 = (String) message2.obj;
                    boolean timedOut = message2.arg1 > 0;
                    int reasonCode3 = message2.arg2;
                    Log.d(WifiStateMachine.TAG, "Assocation Rejection event: bssid=" + StringUtil.safeDisplayBssid(bssid2) + " reason code=" + reasonCode3 + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid2 == null || TextUtils.isEmpty(bssid2)) {
                        bssid2 = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    if (bssid2 != null) {
                        boolean unused25 = WifiStateMachine.this.didBlackListBSSID = WifiStateMachine.this.mWifiConnectivityManager.trackBssid(bssid2, false, reasonCode3);
                    }
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 2);
                    WifiStateMachine.this.mWifiConfigManager.setRecentFailureAssociationStatus(WifiStateMachine.this.mTargetNetworkId, reasonCode3);
                    WifiStateMachine.this.notifyWifiConnFailedInfo(WifiStateMachine.this.mTargetNetworkId, bssid2, WifiServiceHisiExt.MIN_RSSI, 2, WifiStateMachine.this.mWifiConnectivityManager);
                    WifiStateMachine.this.recordAssociationRejectStatusCode(message2.arg2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                    WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                    if (timedOut) {
                        i2 = 11;
                    }
                    wifiStateMachine3.reportConnectionAttemptEnd(i2, 1);
                    WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), bssid2, 1);
                    break;
                case WifiMonitor.ANQP_DONE_EVENT:
                    WifiStateMachine.this.mPasspointManager.notifyANQPDone((AnqpEvent) message2.obj);
                    break;
                case WifiMonitor.AUTHENTICATION_TIMEOUT_EVENT:
                    if (WifiStateMachine.this.mWifiInfo != null && WifiStateMachine.this.mWifiInfo.getSupplicantState() == SupplicantState.ASSOCIATED) {
                        WifiStateMachine.this.loge("auth timeout in associated state, handle as associate reject event");
                        WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                        break;
                    }
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT:
                    WifiStateMachine.this.mPasspointManager.notifyIconDone((IconEvent) message2.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT:
                    WifiStateMachine.this.mPasspointManager.receivedWnmFrame((WnmData) message2.obj);
                    break;
                case WifiMonitor.WPS_START_OKC_EVENT:
                    WifiStateMachine.this.sendWpsOkcStartedBroadcast();
                    if (!WifiStateMachine.this.mWifiNative.removeAllNetworks(WifiStateMachine.this.mInterfaceName)) {
                        WifiStateMachine.this.loge("Failed to remove networks before HiLink OKC");
                    }
                    String hilinkBssid = (String) message2.obj;
                    if (!TextUtils.isEmpty(hilinkBssid)) {
                        WifiStateMachine.this.mWifiNative.startWpsPbc(WifiStateMachine.this.mInterfaceName, hilinkBssid);
                        break;
                    }
                    break;
                case WifiMonitor.EAP_ERRORCODE_REPORT_EVENT:
                    if (WifiStateMachine.this.targetWificonfiguration != null && WifiStateMachine.this.targetWificonfiguration.networkId == message2.arg1) {
                        WifiStateMachine.this.handleEapErrorcodeReport(message2.arg1, (String) message2.obj, message2.arg2);
                        break;
                    }
                case 151553:
                    int netId5 = message2.arg1;
                    WifiConfiguration config5 = (WifiConfiguration) message2.obj;
                    String strConfigCRC = "*";
                    boolean hasCredentialChanged = false;
                    boolean forceReconnect = false;
                    if (config5 != null) {
                        strConfigCRC = config5.preSharedKey;
                        if (WifiStateMachine.this.mNetworkInfo != null && WifiStateMachine.this.mNetworkInfo.isConnectedOrConnecting() && config5.isTempCreated && WifiStateMachine.this.isWifiProEvaluatingAP()) {
                            WifiStateMachine.this.logd("CONNECT_NETWORK user connect network, stop background evaluating and force reconnect");
                            config5.isTempCreated = false;
                            forceReconnect = true;
                        }
                        NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config5, message2.sendingUid);
                        if (!result.isSuccess()) {
                            WifiStateMachine.this.loge("CONNECT_NETWORK adding/updating config=" + config5 + " failed");
                            int unused26 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            WifiStateMachine.this.replyToMessage(message2, 151554, 0);
                            break;
                        } else {
                            netId5 = result.getNetworkId();
                            hasCredentialChanged = result.hasCredentialChanged();
                        }
                    }
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiConfiguration newWifiConfig = new WifiConfiguration(config5);
                        newWifiConfig.preSharedKey = strConfigCRC;
                        WifiStateMachine.this.mHwWifiCHRService.connectFromUserByConfig(newWifiConfig);
                    }
                    WifiStateMachine.this.saveConnectingNetwork(config5, netId5, false);
                    WifiStateMachine.this.exitWifiSelfCure(151553, -1);
                    if (!(WifiStateMachine.this.mLastNetworkId == -1 || WifiStateMachine.this.mLastNetworkId != netId5 || WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mDisconnectedState)) {
                        WifiStateMachine.this.logd("disconnect old");
                        forceReconnect = true;
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    }
                    if (WifiStateMachine.this.connectToUserSelectNetwork(netId5, message2.sendingUid, hasCredentialChanged || forceReconnect)) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(13, config5);
                        WifiStateMachine.this.broadcastWifiCredentialChanged(0, config5);
                        WifiStateMachine.this.replyToMessage(message2, 151555);
                        break;
                    } else {
                        int unused27 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                        if (-1 != netId5 && config5 == null) {
                            config5 = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(netId5);
                        }
                        if (config5 != null && HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config5, false)) {
                            WifiStateMachine.this.replyToMessage(message2, 151554, 1000);
                            break;
                        } else {
                            WifiStateMachine.this.replyToMessage(message2, 151554, 9);
                            break;
                        }
                    }
                    break;
                case 151556:
                    if (WifiStateMachine.this.deleteNetworkConfigAndSendReply(message2, true)) {
                        int netId6 = message2.arg1;
                        WifiStateMachine.this.exitWifiSelfCure(151556, netId6);
                        if (netId6 == WifiStateMachine.this.mTargetNetworkId || netId6 == WifiStateMachine.this.mLastNetworkId) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            break;
                        }
                    }
                    break;
                case 151559:
                    NetworkUpdateResult result2 = WifiStateMachine.this.saveNetworkConfigAndSendReply(message2);
                    int netId7 = result2.getNetworkId();
                    if (result2.isSuccess() && WifiStateMachine.this.mWifiInfo.getNetworkId() == netId7) {
                        if (!result2.hasCredentialChanged()) {
                            if (result2.hasProxyChanged()) {
                                WifiStateMachine.this.log("Reconfiguring proxy on connection");
                                WifiStateMachine.this.mIpClient.setHttpProxy(WifiStateMachine.this.getProxyProperties());
                            }
                            if (result2.hasIpChanged()) {
                                Log.d(WifiStateMachine.TAG, "Reconfiguring IP if current state == mConnectedState");
                                if (!WifiStateMachine.this.isConnected()) {
                                    Log.d(WifiStateMachine.TAG, "ignore reconfiguring IP because current state != mConnectedState");
                                    break;
                                } else {
                                    WifiStateMachine.this.log("Reconfiguring IP on connection");
                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                                    break;
                                }
                            }
                        } else {
                            WifiStateMachine.this.logi("SAVE_NETWORK credential changed for config=" + ((WifiConfiguration) message2.obj).configKey() + ", Reconnecting.");
                            WifiStateMachine.this.startConnectToNetwork(netId7, message2.sendingUid, "any");
                            break;
                        }
                    }
                    break;
                case 151562:
                    WpsInfo wpsInfo = (WpsInfo) message2.obj;
                    if (wpsInfo != null) {
                        WpsResult wpsResult = new WpsResult();
                        if (!WifiStateMachine.this.mWifiNative.removeAllNetworks(WifiStateMachine.this.mInterfaceName)) {
                            WifiStateMachine.this.loge("Failed to remove networks before WPS");
                        }
                        switch (wpsInfo.setup) {
                            case 0:
                                WifiStateMachine.this.clearRandomMacOui();
                                WifiStateMachine.this.mIsRandomMacCleared = true;
                                if (!WifiStateMachine.this.mWifiNative.startWpsPbc(WifiStateMachine.this.mInterfaceName, wpsInfo.BSSID)) {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS push button configuration");
                                    wpsResult.status = WpsResult.Status.FAILURE;
                                    break;
                                } else {
                                    wpsResult.status = WpsResult.Status.SUCCESS;
                                    break;
                                }
                            case 1:
                                wpsResult.pin = WifiStateMachine.this.mWifiNative.startWpsPinDisplay(WifiStateMachine.this.mInterfaceName, wpsInfo.BSSID);
                                if (TextUtils.isEmpty(wpsResult.pin)) {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS pin method configuration");
                                    wpsResult.status = WpsResult.Status.FAILURE;
                                    break;
                                } else {
                                    wpsResult.status = WpsResult.Status.SUCCESS;
                                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_WPS_PIN_RETRY, wpsResult);
                                    break;
                                }
                            case 2:
                                if (!WifiStateMachine.this.mWifiNative.startWpsRegistrar(WifiStateMachine.this.mInterfaceName, wpsInfo.BSSID, wpsInfo.pin)) {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS push button configuration");
                                    wpsResult.status = WpsResult.Status.FAILURE;
                                    break;
                                } else {
                                    wpsResult.status = WpsResult.Status.SUCCESS;
                                    break;
                                }
                            default:
                                wpsResult = new WpsResult(WpsResult.Status.FAILURE);
                                WifiStateMachine.this.loge("Invalid setup for WPS");
                                break;
                        }
                        if (wpsResult.status != WpsResult.Status.SUCCESS) {
                            WifiStateMachine.this.loge("Failed to start WPS with config " + wpsInfo.toString());
                            WifiStateMachine.this.replyToMessage(message2, 151564, 0);
                            break;
                        } else {
                            WifiStateMachine.this.replyToMessage(message2, 151563, (Object) wpsResult);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWpsRunningState);
                            break;
                        }
                    } else {
                        WifiStateMachine.this.loge("Cannot start WPS with null WpsInfo object");
                        WifiStateMachine.this.replyToMessage(message2, 151564, 0);
                        break;
                    }
                case 151569:
                    int netId8 = message2.arg1;
                    if (!WifiStateMachine.this.mWifiConfigManager.disableNetwork(netId8, message2.sendingUid)) {
                        WifiStateMachine.this.loge("Failed to disable network");
                        int unused28 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                        WifiStateMachine.this.replyToMessage(message2, 151570, 0);
                        break;
                    } else {
                        WifiStateMachine.this.replyToMessage(message2, 151571);
                        if (netId8 == WifiStateMachine.this.mTargetNetworkId || netId8 == WifiStateMachine.this.mLastNetworkId) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            break;
                        }
                    }
                default:
                    return false;
            }
            return true;
        }

        private boolean isApInScanList(String bssid) {
            ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
            if (!(scanProxy == null || bssid == null)) {
                synchronized (scanProxy) {
                    List<ScanResult> cachedScanResults = scanProxy.getScanResults();
                    if (cachedScanResults != null) {
                        for (ScanResult result : cachedScanResults) {
                            if (result != null && result.BSSID != null && bssid.equals(result.BSSID)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    class ConnectedState extends State {
        private Message mSourceMessage = null;

        ConnectedState() {
        }

        public void enter() {
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.logd("WifiStateMachine: enter Connected state" + getName());
            WifiStateMachine.this.processStatistics(0);
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                wifiStateMachine2.log("Enter ConnectedState  mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.triggerRoamingNetworkMonitor(WifiStateMachine.this.mIsAutoRoaming);
            WifiStateMachine.this.handleConnectedInWifiPro();
            if (WifiStateMachine.this.mWifiRepeater != null) {
                WifiStateMachine.this.mWifiRepeater.handleWifiConnect(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mWifiInfo.getNetworkId()));
            }
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
            }
            WifiStateMachine.this.registerConnected();
            long unused = WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiConfiguration unused2 = WifiStateMachine.this.targetWificonfiguration = null;
            boolean unused3 = WifiStateMachine.this.mIsAutoRoaming = false;
            if (WifiStateMachine.this.testNetworkDisconnect) {
                int unused4 = WifiStateMachine.this.testNetworkDisconnectCounter = WifiStateMachine.this.testNetworkDisconnectCounter + 1;
                WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                wifiStateMachine3.logd("ConnectedState Enter start disconnect test " + WifiStateMachine.this.testNetworkDisconnectCounter);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT, WifiStateMachine.this.testNetworkDisconnectCounter, 0), 15000);
            }
            long unused5 = WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            int unused6 = WifiStateMachine.this.mTargetNetworkId = -1;
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            WifiStateMachine.this.triggerUpdateAPInfo();
            WifiStateMachine.this.mWifiStateTracker.updateState(3);
            WifiStateMachine.this.notifyWlanChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(WifiStateMachine.this.mWifiInfo.getFrequency()));
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendWifiApType(WifiStateMachine.this.getWifiApTypeFromMpLink());
            }
        }

        public boolean processMessage(Message message) {
            String str;
            String str2;
            WifiStateMachine.this.logStateAndMessage(message, this);
            boolean accept = false;
            switch (message.what) {
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                    if (message.arg1 == WifiStateMachine.this.testNetworkDisconnectCounter) {
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 0) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 3);
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    } else if (message.arg1 == 2 || message.arg1 == 1 || message.arg1 == 3) {
                        if (message.arg1 == 2) {
                            str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                        } else {
                            str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                        }
                        Log.d(WifiStateMachine.TAG, str);
                        WifiConfiguration config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            if (message.arg1 == 2) {
                                WifiStateMachine.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, false);
                                Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus(DISABLED_NO_INTERNET)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config.networkId, 10);
                            } else {
                                WifiStateMachine.this.mWifiConfigManager.incrementNetworkNoInternetAccessReports(config.networkId);
                            }
                            WifiStateMachine.this.handleUnwantedNetworkInWifiPro(config, message.arg1);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    long unused = WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    WifiConfiguration config2 = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    if (config2 != null) {
                        boolean unused2 = WifiStateMachine.this.setTargetBssid(config2, bssid);
                        int unused3 = WifiStateMachine.this.mTargetNetworkId = netId;
                        WifiStateMachine.this.logd("CMD_START_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config2.configKey() + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                        WifiStateMachine.this.reportConnectionAttemptStart(config2, WifiStateMachine.this.mTargetRoamBSSID, 3);
                        if (!WifiStateMachine.this.mWifiNative.roamToNetwork(WifiStateMachine.this.mInterfaceName, config2)) {
                            WifiStateMachine.this.loge("CMD_START_ROAM Failed to start roaming to network " + config2);
                            WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                            WifiStateMachine.this.replyToMessage(message, 151554, 0);
                            int unused4 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            break;
                        } else {
                            long unused5 = WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                            WifiConfiguration unused6 = WifiStateMachine.this.targetWificonfiguration = config2;
                            boolean unused7 = WifiStateMachine.this.mIsAutoRoaming = true;
                            WifiStateMachine.this.mWifiMetrics.logStaEvent(12, config2);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                            break;
                        }
                    } else {
                        WifiStateMachine.this.loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    long unused8 = WifiStateMachine.this.mLastDriverRoamAttempt = WifiStateMachine.this.mClock.getWallClockMillis();
                    WifiStateMachine.this.notifyWifiRoamingStarted();
                    return false;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1) {
                        WifiConfiguration config3 = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config3 != null) {
                            WifiStateMachine.this.handleValidNetworkInWifiPro(config3);
                            WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config3.networkId, 0);
                            WifiStateMachine.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config3.networkId, true);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_ACCEPT_UNVALIDATED /*131225*/:
                    if (message.arg1 != 0) {
                        accept = true;
                    }
                    WifiStateMachine.this.mWifiConfigManager.setNetworkNoInternetAccessExpected(WifiStateMachine.this.mLastNetworkId, accept);
                    return true;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    int slot = message.arg1;
                    int result = WifiStateMachine.this.startWifiIPPacketOffload(slot, (KeepalivePacketData) message.obj, message.arg2);
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, result);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_SET_DETECTMODE_CONF /*131772*/:
                    WifiStateMachine.this.processSetVoWifiDetectMode(message);
                    break;
                case WifiStateMachine.CMD_SET_DETECT_PERIOD /*131773*/:
                    WifiStateMachine.this.processSetVoWifiDetectPeriod(message);
                    break;
                case WifiStateMachine.POOR_LINK_DETECTED /*131873*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: POOR_LINK_DETECTED");
                    WifiStateMachine.this.wifiNetworkExplicitlyUnselected();
                    boolean unused9 = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.VERIFYING_POOR_LINK);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.GOOD_LINK_DETECTED /*131874*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: GOOD_LINK_DETECTED");
                    WifiStateMachine.this.updateWifiBackgroudStatus(message.arg1);
                    WifiStateMachine.this.wifiNetworkExplicitlySelected();
                    WifiStateMachine.this.setWifiBackgroundStatus(false);
                    WifiStateMachine.this.sendConnectedState();
                    break;
                case WifiStateMachine.INVALID_LINK_DETECTED /*131875*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: INVALID_LINK_DETECTED");
                    WifiStateMachine.this.triggerInvalidlinkNetworkMonitor();
                    break;
                case WifiStateMachine.SCE_REQUEST_UPDATE_DNS_SERVER /*131882*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_UPDATE_DNS_SERVER");
                    WifiStateMachine.this.sendUpdateDnsServersRequest(message, WifiStateMachine.this.mLinkProperties);
                    break;
                case WifiStateMachine.SCE_REQUEST_RENEW_DHCP /*131883*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_RENEW_DHCP");
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    break;
                case WifiStateMachine.SCE_REQUEST_SET_STATIC_IP /*131884*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_SET_STATIC_IP");
                    WifiStateMachine.this.stopIpClient();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.SCE_START_SET_STATIC_IP, message.obj, 1000);
                    break;
                case WifiStateMachine.SCE_START_SET_STATIC_IP /*131885*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_START_SET_STATIC_IP");
                    WifiStateMachine.this.handleStaticIpConfig(WifiStateMachine.this.mIpClient, WifiStateMachine.this.mWifiNative, (StaticIpConfiguration) message.obj);
                    break;
                case WifiStateMachine.SCE_REQUEST_REASSOC_WIFI /*131886*/:
                    WifiStateMachine.this.startSelfCureWifiReassoc();
                    break;
                case WifiStateMachine.SCE_REQUEST_RESET_WIFI /*131887*/:
                    WifiStateMachine.this.startSelfCureWifiReset();
                    break;
                case WifiStateMachine.CMD_SCE_HANDLE_IP_INVALID /*131895*/:
                    WifiStateMachine.this.startSelfCureReconnect();
                    WifiStateMachine.this.mIpClient.forceRemoveDhcpCache();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_DISCONNECT, 500);
                    break;
                case WifiStateMachine.CMD_SCE_HANDLE_IP_NO_INTERNET /*131898*/:
                    WifiStateMachine.this.mIpClient.forceRemoveDhcpCache();
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    if (WifiStateMachine.this.mLastDriverRoamAttempt != 0) {
                        long lastRoam = WifiStateMachine.this.mClock.getWallClockMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        long unused10 = WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    WifiConfiguration config4 = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=");
                        sb.append(WifiStateMachine.this.mWifiInfo.getBSSID());
                        sb.append(" RSSI=");
                        sb.append(WifiStateMachine.this.mWifiInfo.getRssi());
                        sb.append(" freq=");
                        sb.append(WifiStateMachine.this.mWifiInfo.getFrequency());
                        sb.append(" reason=");
                        sb.append(message.arg2);
                        sb.append(" Network Selection Status=");
                        if (config4 == null) {
                            str2 = "Unavailable";
                        } else {
                            str2 = config4.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        sb.append(str2);
                        wifiStateMachine.log(sb.toString());
                        break;
                    }
                    break;
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT:
                    WifiStateMachine.this.logd("receive Vo WifiDetect event 1");
                    if (this.mSourceMessage != null) {
                        WifiStateMachine.this.logd("receive Vo WifiDetect event 2");
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151576);
                        break;
                    }
                    break;
                case 151575:
                    WifiStateMachine.this.logd("start VoWifiDetect ");
                    this.mSourceMessage = Message.obtain(message);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.logd("WifiStateMachine: Leaving Connected state");
            WifiStateMachine.this.processStatistics(1);
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            long unused = WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
            WifiStateMachine.this.notifyWlanState(WifiCommonUtils.STATE_DISCONNECTED);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            Message message2 = message;
            WifiStateMachine.this.logStateAndMessage(message2, this);
            int removeResult = -1;
            boolean disableOthers = false;
            switch (message2.what) {
                case 0:
                    Log.wtf(WifiStateMachine.TAG, "Error! empty message encountered");
                    break;
                case 69632:
                    if (((AsyncChannel) message2.obj) == WifiStateMachine.this.mWifiP2pChannel) {
                        if (message2.arg1 != 0) {
                            WifiStateMachine.this.loge("WifiP2pService connection failure, error=" + message2.arg1);
                            break;
                        } else {
                            boolean unused = WifiStateMachine.this.p2pSendMessage(69633);
                            if (WifiStateMachine.this.mOperationalMode == 1) {
                                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                                break;
                            }
                        }
                    } else {
                        WifiStateMachine.this.loge("got HALF_CONNECTED for unknown channel");
                        break;
                    }
                    break;
                case 69636:
                    if (((AsyncChannel) message2.obj) == WifiStateMachine.this.mWifiP2pChannel) {
                        WifiStateMachine.this.loge("WifiP2pService channel lost, message.arg1 =" + message2.arg1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (message2.arg1 != 0) {
                        disableOthers = true;
                    }
                    boolean unused2 = wifiStateMachine.mBluetoothConnectionActive = disableOthers;
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                    NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork((WifiConfiguration) message2.obj, message2.sendingUid);
                    if (!result.isSuccess()) {
                        int unused3 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine.this.replyToMessage(message2, message2.what, result.getNetworkId());
                    break;
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    boolean unused4 = WifiStateMachine.this.deleteNetworkConfigAndSendReply(message2, false);
                    break;
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    if (message2.arg2 == 1) {
                        disableOthers = true;
                    }
                    boolean ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(message2.arg1, disableOthers, message2.sendingUid);
                    if (!ok) {
                        int unused5 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                    int i = message2.what;
                    if (ok) {
                        removeResult = 1;
                    }
                    wifiStateMachine2.replyToMessage(message2, i, removeResult);
                    break;
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.mWifiConfigManager.getSavedNetworks());
                    break;
                case WifiStateMachine.CMD_GET_SUPPORTED_FEATURES /*131133*/:
                    if (WifiStateMachine.this.mFeatureSet <= 0) {
                        int unused6 = WifiStateMachine.this.mFeatureSet = WifiStateMachine.this.mWifiNative.getSupportedFeatureSet(WifiStateMachine.this.mInterfaceName);
                        if (WifiStateMachine.DBG) {
                            Log.d(WifiStateMachine.TAG, "CMD_GET_SUPPORTED_FEATURES: " + WifiStateMachine.this.mFeatureSet);
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message2, message2.what, WifiStateMachine.this.mFeatureSet);
                    break;
                case WifiStateMachine.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*131134*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) null);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS /*131672*/:
                case WifiStateMachine.INVALID_LINK_DETECTED /*131875*/:
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                case WifiStateMachine.CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                case WifiStateMachine.CMD_WPS_PIN_RETRY /*131576*/:
                case WifiStateMachine.CMD_SET_DETECTMODE_CONF /*131772*/:
                case WifiStateMachine.CMD_SET_DETECT_PERIOD /*131773*/:
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                case WifiMonitor.WPS_OVERLAP_EVENT:
                case WifiMonitor.SUP_REQUEST_IDENTITY:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH:
                case 147474:
                case WifiMonitor.WAPI_CERTIFICATION_FAILURE_EVENT:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT:
                case WifiMonitor.WPS_START_OKC_EVENT:
                case WifiMonitor.EAP_ERRORCODE_REPORT_EVENT:
                case 151575:
                case 196611:
                case 196612:
                case 196614:
                case 196618:
                    int unused7 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message2.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(2, true);
                        break;
                    } else {
                        WifiStateMachine.this.setSuspendOptimizations(2, false);
                        break;
                    }
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                    if (message2.arg1 == 1) {
                        disableOthers = true;
                    }
                    boolean unused8 = wifiStateMachine3.mEnableRssiPolling = disableOthers;
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message2.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(4, false);
                        break;
                    } else {
                        if (message2.arg2 == 1) {
                            WifiStateMachine.this.mSuspendWakeLock.release();
                        }
                        WifiStateMachine.this.setSuspendOptimizations(4, true);
                        break;
                    }
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    WifiStateMachine wifiStateMachine4 = WifiStateMachine.this;
                    if (message2.arg1 != 0) {
                        disableOthers = true;
                    }
                    wifiStateMachine4.handleScreenStateChanged(disableOthers);
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    WifiStateMachine.this.deferMessage(message2);
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what);
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    int unused9 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message2);
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*131178*/:
                    if (WifiStateMachine.this.mPasspointManager.addOrUpdateProvider((PasspointConfiguration) message2.obj, message2.arg1)) {
                        removeResult = 1;
                    }
                    WifiStateMachine.this.replyToMessage(message2, message2.what, removeResult);
                    break;
                case WifiStateMachine.CMD_REMOVE_PASSPOINT_CONFIG /*131179*/:
                    if (WifiStateMachine.this.mPasspointManager.removeProvider((String) message2.obj)) {
                        removeResult = 1;
                    }
                    WifiStateMachine.this.replyToMessage(message2, message2.what, removeResult);
                    break;
                case WifiStateMachine.CMD_GET_PASSPOINT_CONFIGS /*131180*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) WifiStateMachine.this.mPasspointManager.getProviderConfigs());
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_OSU_PROVIDERS /*131181*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) new ArrayList());
                    break;
                case WifiStateMachine.CMD_BOOT_COMPLETED /*131206*/:
                    boolean unused10 = WifiStateMachine.this.isBootCompleted = true;
                    WifiStateMachine.this.getAdditionalWifiServiceInterfaces();
                    if (!WifiStateMachine.this.mWifiConfigManager.loadFromStore()) {
                        Log.e(WifiStateMachine.TAG, "Failed to load from config store");
                    }
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    break;
                case WifiStateMachine.CMD_INITIALIZE /*131207*/:
                    boolean ok2 = WifiStateMachine.this.mWifiNative.initialize();
                    WifiStateMachine.this.mPasspointManager.initializeProvisioner(WifiStateMachine.this.mWifiInjector.getWifiServiceHandlerThread().getLooper());
                    WifiStateMachine wifiStateMachine5 = WifiStateMachine.this;
                    int i2 = message2.what;
                    if (ok2) {
                        removeResult = 1;
                    }
                    wifiStateMachine5.replyToMessage(message2, i2, removeResult);
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    int unused11 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_UPDATE_LINKPROPERTIES /*131212*/:
                    WifiStateMachine.this.updateLinkProperties((LinkProperties) message2.obj);
                    break;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    WifiStateMachine.this.deferMessage(message2);
                    break;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message2.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message2.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                    int unused12 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    int unused13 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_GET_ALL_MATCHING_CONFIGS /*131240*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) new ArrayList());
                    break;
                case WifiStateMachine.CMD_INSTALL_PACKET_FILTER /*131274*/:
                    WifiStateMachine.this.mWifiNative.installPacketFilter(WifiStateMachine.this.mInterfaceName, (byte[]) message2.obj);
                    break;
                case WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                    if (!((Boolean) message2.obj).booleanValue()) {
                        WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets(WifiStateMachine.this.mInterfaceName);
                        break;
                    } else {
                        WifiStateMachine.this.mWifiNative.startFilteringMulticastV4Packets(WifiStateMachine.this.mInterfaceName);
                        break;
                    }
                case WifiStateMachine.CMD_USER_SWITCH /*131277*/:
                    Set<Integer> removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.handleUserSwitch(message2.arg1);
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_USER_UNLOCK /*131278*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserUnlock(message2.arg1);
                    break;
                case WifiStateMachine.CMD_USER_STOP /*131279*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserStop(message2.arg1);
                    break;
                case WifiStateMachine.CMD_READ_PACKET_FILTER /*131280*/:
                    WifiStateMachine.this.mIpClient.readPacketFilterComplete(WifiStateMachine.this.mWifiNative.readPacketFilter(WifiStateMachine.this.mInterfaceName));
                    break;
                case WifiStateMachine.CMD_DIAGS_CONNECT_TIMEOUT /*131324*/:
                    WifiStateMachine.this.mWifiDiagnostics.reportConnectionEvent(((Long) message2.obj).longValue(), (byte) 2);
                    break;
                case WifiStateMachine.CMD_START_SUBSCRIPTION_PROVISIONING /*131326*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, 0);
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    WifiStateMachine.this.replyToMessage(message2, message2.what, (Object) null);
                    break;
                case WifiStateMachine.CMD_PNO_PERIODIC_SCAN /*131575*/:
                    WifiStateMachine.this.deferMessage(message2);
                    break;
                case WifiStateMachine.CMD_GET_SUPPORT_VOWIFI_DETECT /*131774*/:
                    WifiStateMachine.this.processIsSupportVoWifiDetect(message2);
                    break;
                case WifiStateMachine.GOOD_LINK_DETECTED /*131874*/:
                    WifiStateMachine.this.log("GOOD_LINK_DETECTED, state = DefaultState");
                    WifiStateMachine.this.setWifiBackgroundStatus(false);
                    break;
                case WifiStateMachine.CMD_SCE_WIFI_OFF_TIMEOUT /*131888*/:
                case WifiStateMachine.CMD_SCE_WIFI_ON_TIMEOUT /*131889*/:
                case WifiStateMachine.CMD_SCE_WIFI_CONNECT_TIMEOUT /*131890*/:
                case WifiStateMachine.CMD_SCE_WIFI_REASSOC_TIMEOUT /*131891*/:
                case WifiStateMachine.CMD_SCE_WIFI_RECONNECT_TIMEOUT /*131896*/:
                case WifiStateMachine.WIFIPRO_SOFT_CONNECT_TIMEOUT /*131897*/:
                    WifiStateMachine.this.log("wifi self cure timeout, message type = " + message2.what);
                    WifiStateMachine.this.notifySelfCureComplete(false, message2.arg1);
                    break;
                case WifiStateMachine.CMD_SCE_STOP_SELF_CURE /*131892*/:
                    WifiStateMachine.this.log("CMD_SCE_STOP_SELF_CURE, arg1 =" + message2.arg1);
                    WifiStateMachine.this.stopSelfCureWifi(message2.arg1);
                    if (message2.arg1 < 0) {
                        if (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mDisconnectedState) {
                            boolean unused14 = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
                            WifiStateMachine.this.sendNetworkStateChangeBroadcast(null);
                            break;
                        } else {
                            WifiStateMachine.this.log("CMD_SCE_STOP_SELF_CURE, to disconnect because of wifi self cure failed.");
                            WifiStateMachine.this.removeMessages(WifiMonitor.NETWORK_CONNECTION_EVENT);
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            WifiStateMachine.this.handleNetworkDisconnect();
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_SCE_RESTORE /*131893*/:
                    if (WifiStateMachine.this.mNetworkAgent == null) {
                        WifiStateMachine.this.log("CMD_SCE_RESTORE, use networkAgent to sendNetworkInfo");
                        boolean unused15 = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
                        WifiNetworkAgent wifiNetworkAgent = new WifiNetworkAgent(WifiStateMachine.this, WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, WifiStateMachine.this.mNetworkCapabilitiesFilter, WifiStateMachine.this.mLinkProperties, 100, WifiStateMachine.this.mNetworkMisc);
                        wifiNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
                        break;
                    } else {
                        WifiStateMachine.this.log("CMD_SCE_RESTORE, use mNetworkAgent to sendNetworkInfo");
                        WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
                        break;
                    }
                case WifiStateMachine.CMD_SCE_NOTIFY_WIFI_DISABLED /*131894*/:
                    WifiStateMachine.this.log("CMD_SCE_NOTIFY_WIFI_DISABLED, set WIFI_STATE_DISABLED");
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(((NetworkInfo) message2.obj).isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    WifiStateMachine wifiStateMachine6 = WifiStateMachine.this;
                    if (message2.arg1 == 1) {
                        disableOthers = true;
                    }
                    boolean unused16 = wifiStateMachine6.mTemporarilyDisconnectWifi = disableOthers;
                    WifiStateMachine.this.replyToMessage(message2, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    if (((StateChangeResult) message2.obj).state == SupplicantState.INTERFACE_DISABLED) {
                        Log.e(WifiStateMachine.TAG, "Detected drive hang , recover");
                        WifiStateMachine.this.mWifiInjector.getSelfRecovery().trigger(1);
                        break;
                    }
                    break;
                case WifiMonitor.EVENT_ANT_CORE_ROB:
                    WifiStateMachine.this.handleAntenaPreempted();
                    break;
                case 151553:
                    WifiStateMachine.this.replyToMessage(message2, 151554, 2);
                    break;
                case 151556:
                    boolean unused17 = WifiStateMachine.this.deleteNetworkConfigAndSendReply(message2, true);
                    break;
                case 151559:
                    NetworkUpdateResult unused18 = WifiStateMachine.this.saveNetworkConfigAndSendReply(message2);
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message2, 151564, 2);
                    break;
                case 151566:
                    WifiStateMachine.this.replyToMessage(message2, 151567, 2);
                    break;
                case 151569:
                    WifiStateMachine.this.replyToMessage(message2, 151570, 2);
                    break;
                case 151572:
                    WifiStateMachine.this.replyToMessage(message2, 151574, 2);
                    break;
                default:
                    WifiStateMachine.this.loge("Error! unhandled message" + message2);
                    break;
            }
            return true;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            Log.i(WifiStateMachine.TAG, "disconnectedstate enter");
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            if (WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                boolean unused = WifiStateMachine.this.p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                wifiStateMachine.logd(" Enter DisconnectedState screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.handleDisconnectedInWifiPro();
            if (WifiStateMachine.this.mWifiRepeater != null) {
                WifiStateMachine.this.mWifiRepeater.handleWifiDisconnect();
            }
            boolean unused2 = WifiStateMachine.this.mIsAutoRoaming = false;
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
            long unused3 = WifiStateMachine.this.mDisconnectedTimeStamp = WifiStateMachine.this.mClock.getWallClockMillis();
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 2);
                    WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                    return true;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    if (WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                        return true;
                    }
                    return false;
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    WifiStateMachine.this.handleScreenStateChanged(message.arg1 != 0);
                    return true;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(((NetworkInfo) message.obj).isConnected());
                    return true;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    return true;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    }
                    boolean unused = WifiStateMachine.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    return false;
                default:
                    return false;
            }
        }

        public void exit() {
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.logd(" Enter DisconnectingState State screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.disconnectingWatchdogCount++;
            WifiStateMachine.this.logd("Start Disconnecting Watchdog " + WifiStateMachine.this.disconnectingWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER, WifiStateMachine.this.disconnectingWatchdogCount, 0), 5000);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 == 1) {
                        if (WifiStateMachine.this.hasDeferredMessagesForArg1(WifiStateMachine.CMD_SET_OPERATIONAL_MODE, 4)) {
                            WifiStateMachine.this.log("Has deferred DISABLED_MODE, deffer CONNECT_MODE");
                            WifiStateMachine.this.deferMessage(message);
                            break;
                        }
                    } else {
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Ignore CMD_DISCONNECT when already disconnecting.");
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                    if (WifiStateMachine.this.disconnectingWatchdogCount == message.arg1) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("disconnecting watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class HiddenScanListener implements WifiScanner.ScanListener {
        private WifiConfiguration mConfig = null;
        private List<ScanResult> mScanResults = new ArrayList();
        private int mSendingUid = -1;

        HiddenScanListener(WifiConfiguration config, int uid) {
            this.mConfig = config;
            this.mSendingUid = uid;
        }

        private void quit() {
            this.mConfig = null;
            this.mSendingUid = -1;
            this.mScanResults.clear();
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
            if (this.mConfig == null || WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(this.mConfig.configKey()) == null || this.mScanResults.size() == 0) {
                Log.d(WifiStateMachine.TAG, "HiddenScanListener: return since config removed.");
                return;
            }
            String ssid = NativeUtil.removeEnclosingQuotes(this.mConfig.SSID);
            int size = this.mScanResults.size();
            for (int i = 0; i < size; i++) {
                ScanResult result = this.mScanResults.get(i);
                if (result != null && result.wifiSsid != null && !TextUtils.isEmpty(result.wifiSsid.oriSsid) && !TextUtils.isEmpty(result.SSID) && result.SSID.equals(ssid)) {
                    if (!WifiStateMachine.this.mactchResultAndConfigSecurity(result, this.mConfig)) {
                        Log.d(WifiStateMachine.TAG, "ResultAndConfigSecurity not mactch");
                    } else {
                        this.mConfig.oriSsid = result.wifiSsid.oriSsid;
                        Log.d(WifiStateMachine.TAG, "HiddenScanListener: find SSID=" + ssid + " oriSsid=" + this.mConfig.oriSsid);
                        WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(this.mConfig, this.mSendingUid);
                        WifiStateMachine.this.startConnectToUserSelectNetwork(this.mConfig.networkId, this.mSendingUid, "any");
                        quit();
                        return;
                    }
                }
            }
            Log.d(WifiStateMachine.TAG, "HiddenScanListener: can't find SSID=" + ssid);
            quit();
        }

        public void onFullResult(ScanResult scanResult) {
            this.mScanResults.add(scanResult);
        }

        public void onSuccess() {
        }

        public void onFailure(int i, String s) {
        }

        public void onPeriodChanged(int i) {
        }
    }

    class IpClientCallback extends IpClient.Callback {
        IpClientCallback() {
        }

        public void onPreDhcpAction() {
            WifiStateMachine.this.sendMessage(196611);
        }

        public void onPostDhcpAction() {
            WifiStateMachine.this.sendMessage(196612);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults == null) {
                if (WifiStateMachine.this.mHwWifiCHRService != null) {
                    if (WifiStateMachine.this.mIpClient.getDhcpFlag() == 196609) {
                        WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(4);
                    } else {
                        WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(5);
                    }
                }
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE);
                WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 3);
            } else if ("CMD_TRY_CACHED_IP".equals(dhcpResults.domains)) {
                WifiStateMachine.this.sendMessage(196618);
            } else {
                if (!(WifiStateMachine.this.mHwWifiCHRService == null || dhcpResults.ipAddress == null || WifiStateMachine.this.mWifiConfigManager == null || WifiStateMachine.this.mNetworkInfo == null)) {
                    WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                    boolean isUsingStaticIp = false;
                    if (currentConfig != null && currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                        isUsingStaticIp = true;
                    }
                    if (isUsingStaticIp) {
                        WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(9);
                    } else if (NetworkInfo.DetailedState.OBTAINING_IPADDR == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                        if (!"getCachedDhcpResultsForCurrentConfig".equals(dhcpResults.domains)) {
                            WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(2);
                        } else {
                            WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(16);
                        }
                    } else if (NetworkInfo.DetailedState.CONNECTED == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                        WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(3);
                    }
                }
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
            }
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            WifiStateMachine.this.mWifiMetrics.logStaEvent(7);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            WifiStateMachine.this.mWifiMetrics.logStaEvent(8);
            if (WifiStateMachine.this.mHwWifiCHRService != null && 1 < WifiStateMachine.this.syncGetWifiState()) {
                WifiStateMachine.this.mHwWifiCHRService.uploadDhcpException(DhcpClient.mDhcpError);
            }
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            WifiStateMachine.this.mWifiMetrics.logStaEvent(9);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_REACHABILITY_LOST, logMsg);
        }

        public void installPacketFilter(byte[] filter) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_INSTALL_PACKET_FILTER, filter);
        }

        public void startReadPacketFilter() {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_READ_PACKET_FILTER);
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_CONFIG_ND_OFFLOAD, enabled);
        }
    }

    class L2ConnectedState extends State {
        RssiEventHandler mRssiEventHandler = new RssiEventHandler();

        class RssiEventHandler implements WifiNative.WifiRssiEventHandler {
            RssiEventHandler() {
            }

            public void onRssiThresholdBreached(byte curRssi) {
                if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                    Log.e(WifiStateMachine.TAG, "onRssiThresholdBreach event. Cur Rssi = " + curRssi);
                }
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_RSSI_THRESHOLD_BREACHED, curRssi);
            }
        }

        L2ConnectedState() {
        }

        public void enter() {
            NetworkCapabilities nc;
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            int unused = WifiStateMachine.this.mRssiPollToken = WifiStateMachine.this.mRssiPollToken + 1;
            if (WifiStateMachine.this.mEnableRssiPolling) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0);
            }
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.loge("Have NetworkAgent when entering L2Connected");
                boolean unused2 = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            }
            boolean unused3 = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTING);
            if (WifiStateMachine.this.mWifiInfo == null || WifiStateMachine.this.mWifiInfo.getSSID().equals("<unknown ssid>")) {
                nc = WifiStateMachine.this.mNetworkCapabilitiesFilter;
            } else {
                nc = new NetworkCapabilities(WifiStateMachine.this.mNetworkCapabilitiesFilter);
                nc.setSSID(WifiStateMachine.this.mWifiInfo.getSSID());
            }
            NetworkCapabilities nc2 = nc;
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            WifiNetworkAgent wifiNetworkAgent = new WifiNetworkAgent(WifiStateMachine.this, WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, nc2, WifiStateMachine.this.mLinkProperties, WifiStateMachine.this.reportWifiScoreDelayed() ? 99 : 60, WifiStateMachine.this.mNetworkMisc);
            WifiNetworkAgent unused4 = wifiStateMachine.mNetworkAgent = wifiNetworkAgent;
            WifiStateMachine.this.mWifiScoreReport.setLowScoreCount(0);
            WifiStateMachine.this.clearTargetBssid("L2ConnectedState");
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(3);
        }

        public void exit() {
            WifiStateMachine.this.mIpClient.stop();
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=" + Integer.toString(WifiStateMachine.this.mLastNetworkId));
                if (WifiStateMachine.this.mLastBssid != null) {
                    sb.append(" ");
                    sb.append(WifiStateMachine.this.mLastBssid);
                }
            }
            if (!(WifiStateMachine.this.mLastBssid == null && WifiStateMachine.this.mLastNetworkId == -1)) {
                WifiStateMachine.this.handleNetworkDisconnect();
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
            WifiStateMachine.this.mWifiStateTracker.updateState(2);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_DISCONNECT, do disconnect");
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 2);
                    WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                    WifiStateMachine.this.log(" Ignore CMD_RECONNECT request because wifi is already connected");
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.cleanWifiScore();
                    boolean unused = WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    int unused2 = WifiStateMachine.this.mRssiPollToken = WifiStateMachine.this.mRssiPollToken + 1;
                    if (WifiStateMachine.this.mEnableRssiPolling) {
                        WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), (long) WifiStateMachine.this.mPollRssiIntervalMsecs);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                    if (message.arg1 == WifiStateMachine.this.mRssiPollToken) {
                        WifiStateMachine.this.getWifiLinkLayerStats();
                        WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        if (!WifiStateMachine.this.reportWifiScoreDelayed()) {
                            WifiStateMachine.this.mWifiScoreReport.calculateAndReportScore(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mNetworkAgent, WifiStateMachine.this.mWifiMetrics);
                        }
                        if (WifiStateMachine.this.mWifiScoreReport.shouldCheckIpLayer()) {
                            WifiStateMachine.this.mIpClient.confirmConfiguration();
                            WifiStateMachine.this.mWifiScoreReport.noteIpCheck();
                        }
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), (long) WifiStateMachine.this.mPollRssiIntervalMsecs);
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.sendRssiChangeBroadcast(WifiStateMachine.this.mWifiInfo.getRssi());
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (message.arg1 == 0 && WifiStateMachine.this.mLastNetworkId != -1) {
                        WifiConfiguration config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mLastNetworkId);
                        if (TelephonyUtil.isSimConfig(config)) {
                            WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 6);
                            WifiStateMachine.this.handleSimAbsent(config);
                            WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL");
                    WifiStateMachine.this.handleSuccessfulIpConfiguration();
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                        if (WifiStateMachine.this.isHiLinkActive()) {
                            WifiStateMachine.this.setWifiBackgroundReason(6);
                        }
                        WifiStateMachine.this.notifyIpConfigCompleted();
                        if (!WifiStateMachine.this.ignoreEnterConnectedState()) {
                            if (!WifiStateMachine.this.isWifiProEvaluatingAP()) {
                                if (!WifiStateMachine.this.reportWifiScoreDelayed()) {
                                    WifiStateMachine.this.mWifiScoreReport.calculateAndReportScore(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mNetworkAgent, WifiStateMachine.this.mWifiMetrics);
                                }
                                WifiStateMachine.this.sendConnectedState();
                                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                                break;
                            } else {
                                WifiStateMachine.this.log("****WiFi's connected background, don't let Mobile Data down, keep dual networks up.");
                                WifiStateMachine.this.updateNetworkConcurrently();
                                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                                break;
                            }
                        }
                    } else {
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_LOST");
                    WifiStateMachine.this.getWifiLinkLayerStats();
                    if (!WifiStateMachine.this.notifyIpConfigLostAndFixedBySce(WifiStateMachine.this.getCurrentWifiConfiguration())) {
                        WifiStateMachine.this.handleIpConfigurationLost();
                        WifiStateMachine.this.reportConnectionAttemptEnd(10, 1);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    } else {
                        WifiStateMachine.this.log("L2ConnectedState, notifyIpConfigLostAndFixedBySce!!!!");
                        break;
                    }
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    if (((String) message.obj) != null) {
                        String unused3 = WifiStateMachine.this.mLastBssid = (String) message.obj;
                        if (WifiStateMachine.this.mLastBssid != null && (WifiStateMachine.this.mWifiInfo.getBSSID() == null || !WifiStateMachine.this.mLastBssid.equals(WifiStateMachine.this.mWifiInfo.getBSSID()))) {
                            WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                            WifiConfiguration config2 = WifiStateMachine.this.getCurrentWifiConfiguration();
                            if (config2 != null) {
                                ScanDetailCache scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(config2.networkId);
                                if (scanDetailCache != null) {
                                    ScanResult scanResult = scanDetailCache.getScanResult(WifiStateMachine.this.mLastBssid);
                                    if (scanResult != null) {
                                        WifiStateMachine.this.mWifiInfo.setFrequency(scanResult.frequency);
                                    }
                                }
                            }
                            if (!WifiStateMachine.this.isWifiSelfCuring()) {
                                if (WifiStateMachine.this.isWiFiProSwitchOnGoing() && WifiStateMachine.this.getWiFiProRoamingSSID() != null && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mRoamingState) {
                                    WifiStateMachine.this.mWifiInfo.setSSID(WifiStateMachine.this.getWiFiProRoamingSSID());
                                }
                                WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                                break;
                            } else {
                                WifiStateMachine.this.logd("CMD_ASSOCIATED_BSSID, WifiSelfCuring, ignore associated bssid change message.");
                                break;
                            }
                        }
                    } else {
                        WifiStateMachine.this.logw("Associated command w/o BSSID");
                        break;
                    }
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled && message.obj != null) {
                        WifiStateMachine.this.log((String) message.obj);
                    }
                    if (!WifiStateMachine.this.mIpReachabilityDisconnectEnabled) {
                        WifiStateMachine.this.logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        break;
                    } else {
                        WifiStateMachine.this.handleIpReachabilityLost();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                case WifiStateMachine.CMD_RSSI_THRESHOLD_BREACHED /*131236*/:
                    WifiStateMachine.this.processRssiThreshold((byte) message.arg1, message.what, this.mRssiEventHandler);
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.stopRssiMonitoringOffload();
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                    WifiStateMachine.this.handleIPv4Success((DhcpResults) message.obj);
                    WifiStateMachine.this.makeHwDefaultIPTable((DhcpResults) message.obj);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(4);
                    if (WifiStateMachine.DBG) {
                        WifiConfiguration currentWifiConfiguration = WifiStateMachine.this.getCurrentWifiConfiguration();
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.log("DHCP failure count=" + -1);
                    }
                    WifiStateMachine.this.handleIPv4Failure();
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.log("L2ConnectedState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 5);
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        boolean unused4 = WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                    int unused5 = WifiStateMachine.this.mLastNetworkId = message.arg1;
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress(WifiStateMachine.this.mInterfaceName));
                    if (WifiStateMachine.this.mLastBssid != null && !WifiStateMachine.this.mLastBssid.equals(message.obj)) {
                        String unused6 = WifiStateMachine.this.mLastBssid = (String) message.obj;
                        WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    }
                    WifiStateMachine.this.checkSelfCureWifiResult(103);
                    if (WifiStateMachine.this.mLastNetworkId == -1) {
                        NetworkUpdateResult networkUpdateResult = WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                        if (networkUpdateResult != null) {
                            int unused7 = WifiStateMachine.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                        } else {
                            WifiConfiguration config3 = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mWifiConfigManager.getLastSelectedNetwork());
                            if (!(config3 == null || config3.SSID == null || !config3.SSID.equals(WifiStateMachine.this.mWifiInfo.getSSID()))) {
                                int unused8 = WifiStateMachine.this.mLastNetworkId = config3.networkId;
                                WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                            }
                        }
                    }
                    WifiStateMachine.this.notifyWifiRoamingCompleted(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.notifyWlanChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(WifiStateMachine.this.mWifiInfo.getFrequency()));
                    WifiStateMachine.this.setLastConnectConfig(WifiStateMachine.this.getCurrentWifiConfiguration());
                    if (WifiStateMachine.ENABLE_DHCP_AFTER_ROAM) {
                        WifiStateMachine.this.log("L2ConnectedState_NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                        break;
                    }
                    break;
                case 151553:
                    if (WifiStateMachine.this.mWifiInfo.getNetworkId() == message.arg1) {
                        if (!WifiStateMachine.this.isWifiProEvaluatingAP()) {
                            WifiStateMachine.this.replyToMessage(message, 151555);
                            break;
                        } else {
                            WifiStateMachine.this.logd("==connection to same network==");
                            return false;
                        }
                    } else {
                        return false;
                    }
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = WifiStateMachine.this.mWifiInfo.getRssi();
                    WifiNative.TxPacketCounters counters = WifiStateMachine.this.mWifiNative.getTxPacketCounters(WifiStateMachine.this.mInterfaceName);
                    if (counters == null) {
                        WifiStateMachine.this.replyToMessage(message, 151574, 0);
                        break;
                    } else {
                        info.txgood = counters.txSucceeded;
                        info.txbad = counters.txFailed;
                        WifiStateMachine.this.replyToMessage(message, 151573, (Object) info);
                        break;
                    }
                case 196611:
                    WifiStateMachine.this.handlePreDhcpSetup();
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        if (NetworkInfo.DetailedState.OBTAINING_IPADDR != WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                            if (NetworkInfo.DetailedState.CONNECTED == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                                WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(10);
                                break;
                            }
                        } else {
                            WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(0);
                            break;
                        }
                    }
                    break;
                case 196612:
                    WifiStateMachine.this.handlePostDhcpSetup();
                    break;
                case 196614:
                    WifiStateMachine.this.mIpClient.completedPreDhcpAction();
                    break;
                case 196618:
                    DhcpResults dhcpResults = WifiStateMachine.this.getCachedDhcpResultsForCurrentConfig();
                    if (dhcpResults != null) {
                        WifiStateMachine.this.stopIpClient();
                        dhcpResults.domains = "getCachedDhcpResultsForCurrentConfig";
                        IpClient unused9 = WifiStateMachine.this.mIpClient;
                        WifiStateMachine.this.mIpClient.startProvisioning(IpClient.buildProvisioningConfiguration().withStaticConfiguration(dhcpResults).withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities(WifiStateMachine.this.mInterfaceName)).build());
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class McastLockManagerFilterController implements WifiMulticastLockManager.FilterController {
        McastLockManagerFilterController() {
        }

        public void startFilteringMulticastPackets() {
            if (WifiStateMachine.this.mIpClient != null) {
                WifiStateMachine.this.mIpClient.setMulticastFilter(true);
            }
        }

        public void stopFilteringMulticastPackets() {
            if (WifiStateMachine.this.mIpClient != null) {
                WifiStateMachine.this.mIpClient.setMulticastFilter(false);
            }
        }
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            StaticIpConfiguration staticIpConfig;
            WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
            boolean isUsingStaticIp = false;
            if (currentConfig != null && currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                isUsingStaticIp = true;
            }
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                String key = null;
                if (currentConfig != null) {
                    key = currentConfig.configKey();
                }
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                wifiStateMachine.log("enter ObtainingIpState netId=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " " + key + "  roam=" + WifiStateMachine.this.mIsAutoRoaming + " static=" + isUsingStaticIp);
            }
            boolean unused = WifiStateMachine.this.setNetworkDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR);
            WifiStateMachine.this.clearTargetBssid("ObtainingIpAddress");
            WifiStateMachine.this.stopIpClient();
            WifiStateMachine.this.mIpClient.setHttpProxy(WifiStateMachine.this.getProxyProperties());
            if (!TextUtils.isEmpty(WifiStateMachine.this.mTcpBufferSizes)) {
                WifiStateMachine.this.mIpClient.setTcpBufferSizes(WifiStateMachine.this.mTcpBufferSizes);
            }
            WifiStateMachine.this.tryUseStaticIpForFastConnecting(WifiStateMachine.this.mLastNetworkId);
            if (!isUsingStaticIp) {
                staticIpConfig = IpClient.buildProvisioningConfiguration().withPreDhcpAction().withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities(WifiStateMachine.this.mInterfaceName)).withNetwork(WifiStateMachine.this.getCurrentNetwork()).withDisplayName(currentConfig != null ? currentConfig.SSID : "").withRandomMacAddress().build();
                WifiStateMachine.this.mIpClient.putPendingSSID(WifiStateMachine.this.mWifiInfo.getBSSID());
                WifiStateMachine.this.setForceDhcpDiscovery(WifiStateMachine.this.mIpClient);
            } else {
                StaticIpConfiguration prov = IpClient.buildProvisioningConfiguration().withStaticConfiguration(currentConfig.getStaticIpConfiguration()).withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities(WifiStateMachine.this.mInterfaceName)).withNetwork(WifiStateMachine.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).build();
                if (WifiStateMachine.this.mHwWifiCHRService != null) {
                    WifiStateMachine.this.mHwWifiCHRService.updateDhcpState(8);
                }
                staticIpConfig = prov;
            }
            WifiStateMachine.this.mIpClient.startProvisioning(staticIpConfig);
            WifiStateMachine.this.getWifiLinkLayerStats();
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    int unused = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    int unused2 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.SCE_REQUEST_SET_STATIC_IP /*131884*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_SET_STATIC_IP.");
                    WifiStateMachine.this.stopIpClient();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.SCE_START_SET_STATIC_IP, message.obj, 1000);
                    break;
                case WifiStateMachine.SCE_START_SET_STATIC_IP /*131885*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_START_SET_STATIC_IP.");
                    WifiStateMachine.this.handleStaticIpConfig(WifiStateMachine.this.mIpClient, WifiStateMachine.this.mWifiNative, (StaticIpConfiguration) message.obj);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    return false;
                case 151559:
                    int unused3 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.log("RoamingState Enter mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.enterConnectedStateByMode();
            WifiStateMachine.this.roamWatchdogCount++;
            WifiStateMachine.this.logd("Start Roam Watchdog " + WifiStateMachine.this.roamWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER, WifiStateMachine.this.roamWatchdogCount, 0), 15000);
            this.mAssociated = false;
            WifiStateMachine.this.setWiFiProRoamingSSID(null);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                    if (WifiStateMachine.this.roamWatchdogCount == message.arg1) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("roaming watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.mWifiMetrics.endConnectionEvent(9, 1);
                        int unused = WifiStateMachine.this.mRoamFailCount = WifiStateMachine.this.mRoamFailCount + 1;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 4);
                        WifiStateMachine.this.mWifiNative.disconnect(WifiStateMachine.this.mInterfaceName);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(3);
                    }
                    return false;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Roaming and CS doesnt want the network -> ignore");
                    }
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (!this.mAssociated) {
                        int unused2 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    } else {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("roaming and Network connection established");
                        }
                        int unused3 = WifiStateMachine.this.mLastNetworkId = message.arg1;
                        String unused4 = WifiStateMachine.this.mLastBssid = (String) message.obj;
                        if (WifiStateMachine.this.mLastNetworkId == -1) {
                            NetworkUpdateResult networkUpdateResult = WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                            if (networkUpdateResult != null) {
                                int unused5 = WifiStateMachine.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                            }
                        }
                        WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                        WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                        WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, message.arg2);
                        WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                        WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                        WifiStateMachine.this.clearTargetBssid("RoamingCompleted");
                        WifiStateMachine.this.notifyWifiRoamingCompleted(WifiStateMachine.this.mLastBssid);
                        WifiStateMachine.this.setLastConnectConfig(WifiStateMachine.this.getCurrentWifiConfiguration());
                        if (!WifiStateMachine.ENABLE_DHCP_AFTER_ROAM) {
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                            break;
                        } else {
                            WifiStateMachine.this.log("RoamingState_NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                            break;
                        }
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    String bssid = (String) message.obj;
                    String target = "";
                    if (WifiStateMachine.this.mTargetRoamBSSID != null) {
                        target = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + StringUtil.safeDisplayBssid(bssid) + " target=" + target);
                    if (message.arg2 == 15 || message.arg2 == 2) {
                        WifiStateMachine.this.handleDualbandHandoverFailed(3);
                    }
                    if (bssid != null && bssid.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                            wifiStateMachine2.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                            WifiStateMachine.this.handleNetworkDisconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        }
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            String unused6 = WifiStateMachine.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                        }
                        WifiStateMachine.this.notifyWifiRoamingStarted();
                        WifiStateMachine.this.setWiFiProRoamingSSID(stateChangeResult.wifiSsid);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.logd("WifiStateMachine: Leaving Roaming state");
            WifiStateMachine.this.setWiFiProRoamingSSID(null);
        }
    }

    private class UntrustedWifiNetworkFactory extends NetworkFactory {
        public UntrustedWifiNetworkFactory(Looper l, Context c, String tag, NetworkCapabilities f) {
            super(l, c, tag, f);
        }

        /* access modifiers changed from: protected */
        public void needNetworkFor(NetworkRequest networkRequest, int score) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    if (WifiStateMachine.access$1904(WifiStateMachine.this) == 1 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        if (WifiStateMachine.this.mConnectionReqCount == 0) {
                            WifiStateMachine.this.mWifiConnectivityManager.enable(true);
                        }
                        WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(true);
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void releaseNetworkFor(NetworkRequest networkRequest) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    if (WifiStateMachine.access$1906(WifiStateMachine.this) == 0 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(false);
                        if (WifiStateMachine.this.mConnectionReqCount == 0) {
                            WifiStateMachine.this.mWifiConnectivityManager.enable(false);
                        }
                    }
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mUntrustedReqCount " + WifiStateMachine.this.mUntrustedReqCount);
        }
    }

    private class WifiNetworkAgent extends HwNetworkAgent {
        private int mLastNetworkStatus = -1;
        final /* synthetic */ WifiStateMachine this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public WifiNetworkAgent(WifiStateMachine wifiStateMachine, Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
            this.this$0 = wifiStateMachine;
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            if (this == this.this$0.mNetworkAgent) {
                if (this.this$0.mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(this.this$0.mWifiInfo.score));
                }
                this.this$0.unwantedNetwork(0);
            }
        }

        /* access modifiers changed from: protected */
        public void networkStatus(int status, String redirectUrl) {
            if (this == this.this$0.mNetworkAgent && status != this.mLastNetworkStatus) {
                this.mLastNetworkStatus = status;
                if (status == 2) {
                    if (this.this$0.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(this.this$0.mWifiInfo.score));
                    }
                    this.this$0.unwantedNetwork(1);
                } else if (status == 1) {
                    if (this.this$0.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(this.this$0.mWifiInfo.score));
                    }
                    this.this$0.mWifiMetrics.logStaEvent(14);
                    this.this$0.doNetworkStatus(status);
                } else if (status == 3) {
                    this.this$0.reportPortalNetworkStatus();
                } else if (status == 4) {
                    this.this$0.notifyWifiConnectedBackgroundReady();
                }
            }
        }

        /* access modifiers changed from: protected */
        public void saveAcceptUnvalidated(boolean accept) {
            if (this == this.this$0.mNetworkAgent) {
                this.this$0.sendMessage(WifiStateMachine.CMD_ACCEPT_UNVALIDATED, accept);
            }
        }

        /* access modifiers changed from: protected */
        public void startPacketKeepalive(Message msg) {
            this.this$0.sendMessage(WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void stopPacketKeepalive(Message msg) {
            this.this$0.sendMessage(WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                this.this$0.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, this.this$0.mWifiInfo.getRssi());
                return;
            }
            int[] rssiVals = Arrays.copyOf(thresholds, thresholds.length + 2);
            rssiVals[rssiVals.length + WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL] = -128;
            rssiVals[rssiVals.length - 1] = 127;
            Arrays.sort(rssiVals);
            byte[] rssiRange = new byte[rssiVals.length];
            for (int i = 0; i < rssiVals.length; i++) {
                int val = rssiVals[i];
                if (val > 127 || val < -128) {
                    Log.e(WifiStateMachine.TAG, "Illegal value " + val + " for RSSI thresholds: " + Arrays.toString(rssiVals));
                    this.this$0.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, this.this$0.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            byte[] unused = this.this$0.mRssiRanges = rssiRange;
            this.this$0.sendMessage(WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD, this.this$0.mWifiInfo.getRssi());
        }

        /* access modifiers changed from: protected */
        public void preventAutomaticReconnect() {
            if (this == this.this$0.mNetworkAgent) {
                this.this$0.unwantedNetwork(2);
            }
        }
    }

    private class WifiNetworkFactory extends NetworkFactory {
        public WifiNetworkFactory(Looper l, Context c, String TAG, NetworkCapabilities f) {
            super(l, c, TAG, f);
        }

        /* access modifiers changed from: protected */
        public void needNetworkFor(NetworkRequest networkRequest, int score) {
            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                if (WifiStateMachine.access$1804(WifiStateMachine.this) == 1 && WifiStateMachine.this.mWifiConnectivityManager != null && WifiStateMachine.this.mUntrustedReqCount == 0) {
                    WifiStateMachine.this.mWifiConnectivityManager.enable(true);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void releaseNetworkFor(NetworkRequest networkRequest) {
            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                if (WifiStateMachine.access$1806(WifiStateMachine.this) == 0 && WifiStateMachine.this.mWifiConnectivityManager != null && WifiStateMachine.this.mUntrustedReqCount == 0) {
                    WifiStateMachine.this.mWifiConnectivityManager.enable(false);
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mConnectionReqCount " + WifiStateMachine.this.mConnectionReqCount);
        }
    }

    class WpsRunningState extends State {
        private Message mSourceMessage;

        WpsRunningState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            this.mSourceMessage = Message.obtain(WifiStateMachine.this.getCurrentMessage());
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case 151553:
                    WifiStateMachine.this.log(" Ignore CMD_RECONNECT request because wps is running");
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    int unused = WifiStateMachine.this.mOperationalMode = message.arg1;
                    if (WifiStateMachine.this.mOperationalMode == 3) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDefaultState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    int unused2 = WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_WPS_PIN_RETRY /*131576*/:
                    WpsResult wpsResult = (WpsResult) message.obj;
                    if (!TextUtils.isEmpty(wpsResult.pin)) {
                        WifiStateMachine.this.mWifiNative.startWpsPinKeypad(WifiStateMachine.this.mInterfaceName, wpsResult.pin);
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_WPS_PIN_RETRY, wpsResult, 50000);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    Pair<Boolean, Integer> loadResult = loadNetworksFromSupplicantAfterWps();
                    boolean success = ((Boolean) loadResult.first).booleanValue();
                    int netId = ((Integer) loadResult.second).intValue();
                    if (success) {
                        message.arg1 = netId;
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151565);
                    } else {
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 0);
                    }
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.this.saveWpsNetIdInWifiPro(message.arg1);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection lost");
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    return false;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT:
                    break;
                case WifiMonitor.WPS_FAIL_EVENT:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    } else {
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                        this.mSourceMessage.recycle();
                        this.mSourceMessage = null;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Ignore Assoc reject event during WPS Connection");
                        break;
                    }
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message, 151564, 1);
                    break;
                case 151566:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    if (WifiStateMachine.this.mWifiNative.cancelWps(WifiStateMachine.this.mInterfaceName)) {
                        WifiStateMachine.this.replyToMessage(message, 151568);
                    } else {
                        WifiStateMachine.this.replyToMessage(message, 151567, 0);
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (WifiStateMachine.this.mIsRandomMacCleared) {
                WifiStateMachine.this.setRandomMacOui();
                WifiStateMachine.this.mIsRandomMacCleared = false;
            }
        }

        private Pair<Boolean, Integer> loadNetworksFromSupplicantAfterWps() {
            Map<String, WifiConfiguration> configs = new HashMap<>();
            int netId = -1;
            int i = -1;
            if (!WifiStateMachine.this.mWifiNative.migrateNetworksFromSupplicant(WifiStateMachine.this.mInterfaceName, configs, new SparseArray<>())) {
                WifiStateMachine.this.loge("Failed to load networks from wpa_supplicant after Wps");
                return Pair.create(false, -1);
            }
            for (Map.Entry<String, WifiConfiguration> entry : configs.entrySet()) {
                WifiConfiguration config = entry.getValue();
                config.networkId = -1;
                NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, this.mSourceMessage.sendingUid);
                if (!result.isSuccess()) {
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.loge("Failed to add network after WPS: " + entry.getValue());
                    return Pair.create(false, -1);
                } else if (!WifiStateMachine.this.mWifiConfigManager.enableNetwork(result.getNetworkId(), true, this.mSourceMessage.sendingUid)) {
                    Log.wtf(WifiStateMachine.TAG, "Failed to enable network after WPS: " + entry.getValue());
                    return Pair.create(false, -1);
                } else {
                    netId = result.getNetworkId();
                }
            }
            if (configs.size() == 1) {
                i = netId;
            }
            return Pair.create(true, Integer.valueOf(i));
        }
    }

    static /* synthetic */ int access$1804(WifiStateMachine x0) {
        int i = x0.mConnectionReqCount + 1;
        x0.mConnectionReqCount = i;
        return i;
    }

    static /* synthetic */ int access$1806(WifiStateMachine x0) {
        int i = x0.mConnectionReqCount - 1;
        x0.mConnectionReqCount = i;
        return i;
    }

    static /* synthetic */ int access$1904(WifiStateMachine x0) {
        int i = x0.mUntrustedReqCount + 1;
        x0.mUntrustedReqCount = i;
        return i;
    }

    static /* synthetic */ int access$1906(WifiStateMachine x0) {
        int i = x0.mUntrustedReqCount - 1;
        x0.mUntrustedReqCount = i;
        return i;
    }

    private HwMSSHandlerManager checkAndGetHwMSSHandlerManager() {
        if (this.mHwMssHandler == null) {
            this.mHwMssHandler = HwWifiServiceFactory.getHwMSSHandlerManager(this.mContext, this.mWifiNative, this.mWifiInfo);
        }
        return this.mHwMssHandler;
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Log.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Log.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.d(getName(), s);
    }

    public WifiScoreReport getWifiScoreReport() {
        return this.mWifiScoreReport;
    }

    /* access modifiers changed from: private */
    public void processRssiThreshold(byte curRssi, int reason, WifiNative.WifiRssiEventHandler rssiHandler) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + curRssi);
            return;
        }
        int i = 0;
        while (true) {
            if (i >= this.mRssiRanges.length) {
                break;
            } else if (curRssi < this.mRssiRanges[i]) {
                byte maxRssi = this.mRssiRanges[i];
                byte minRssi = this.mRssiRanges[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities();
                int ret = startRssiMonitoringOffload(maxRssi, minRssi, rssiHandler);
                Log.d(TAG, "Re-program RSSI thresholds for " + smToString(reason) + ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi + " ret=" + ret);
                break;
            } else {
                i++;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getPollRssiIntervalMsecs() {
        return this.mPollRssiIntervalMsecs;
    }

    /* access modifiers changed from: package-private */
    public void setPollRssiIntervalMsecs(int newPollIntervalMsecs) {
        this.mPollRssiIntervalMsecs = newPollIntervalMsecs;
    }

    public boolean clearTargetBssid(String dbg) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (config == null) {
            return false;
        }
        String bssid = "any";
        if (config.BSSID != null) {
            bssid = config.BSSID;
            Log.d(TAG, "force BSSID to " + StringUtil.safeDisplayBssid(bssid) + "due to config");
        }
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearTargetBssid " + StringUtil.safeDisplayBssid(bssid) + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        return this.mWifiNative.setConfiguredNetworkBSSID(this.mInterfaceName, "any");
    }

    /* access modifiers changed from: private */
    public boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null || bssid == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "force BSSID to " + StringUtil.safeDisplayBssid(bssid) + "due to config");
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "setTargetBssid set to " + StringUtil.safeDisplayBssid(bssid) + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
        return true;
    }

    /* access modifiers changed from: private */
    public TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public WifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager) {
        super(TAG, looper);
        Context context2 = context;
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mContext = context2;
        this.mFacade = facade;
        this.mWifiNative = wifiNative;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mWrongPasswordNotifier = wrongPasswordNotifier;
        this.mSarManager = sarManager;
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE, "");
        this.mBatteryStats = IBatteryStats.Stub.asInterface(this.mFacade.getService("batterystats"));
        this.mWifiStateTracker = wifiInjector.getWifiStateTracker();
        this.mNwService = INetworkManagementService.Stub.asInterface(this.mFacade.getService("network_management"));
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        this.mPasspointManager = this.mWifiInjector.getPasspointManager();
        this.mWifiMonitor = this.mWifiInjector.getWifiMonitor();
        this.mWifiDiagnostics = this.mWifiInjector.getWifiDiagnostics();
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mWifiPermissionsWrapper = this.mWifiInjector.getWifiPermissionsWrapper();
        this.mWifiInfo = new ExtendedWifiInfo();
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context2, this.mWifiConfigManager, getHandler());
        this.mLinkProperties = new LinkProperties();
        this.mMcastLockManagerFilterController = new McastLockManagerFilterController();
        if (WifiStateMachineHisiExt.hisiWifiEnabled()) {
            this.mWifiStateMachineHisiExt = new WifiStateMachineHisiExt(this.mContext, this.mWifiConfigManager, this.mWifiState, this.mWifiApState);
        }
        this.uploader = DataUploader.getInstance();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039849);
        this.mCountryCode = countryCode;
        this.mWifiScoreReport = new WifiScoreReport(this.mWifiInjector.getScoringParams(), this.mClock);
        this.mNetworkCapabilitiesFilter.addTransportType(1);
        this.mNetworkCapabilitiesFilter.addCapability(12);
        this.mNetworkCapabilitiesFilter.addCapability(11);
        this.mNetworkCapabilitiesFilter.addCapability(18);
        this.mNetworkCapabilitiesFilter.addCapability(20);
        this.mNetworkCapabilitiesFilter.addCapability(13);
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1048576);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1048576);
        this.mDfltNetworkCapabilities = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiStateMachine.VDBG) {
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.log("receive action: " + action);
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 1);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 0);
                }
            }
        }, filter);
        this.mFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                AtomicBoolean access$300 = WifiStateMachine.this.mUserWantsSuspendOpt;
                boolean z = true;
                if (WifiStateMachine.this.mFacade.getIntegerSetting(WifiStateMachine.this.mContext, "wifi_suspend_optimizations_enabled", 1) != 1) {
                    z = false;
                }
                access$300.set(z);
            }
        });
        this.mFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_connected_mac_randomization_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiStateMachine.this.updateConnectedMacRandomizationSetting();
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_BOOT_COMPLETED);
            }
        }, new IntentFilter("android.intent.action.LOCKED_BOOT_COMPLETED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = false;
                boolean unused = WifiStateMachine.this.mIsRunning = false;
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (getSendingUserId() == -1) {
                    z = true;
                }
                boolean unused2 = wifiStateMachine.mIsRealReboot = z;
                Log.d(WifiStateMachine.TAG, "onReceive: mIsRealReboot = " + WifiStateMachine.this.mIsRealReboot);
                if (WifiStateMachine.DBG) {
                    WifiStateMachine.this.log("shut down so update battery");
                }
                WifiStateMachine.this.updateBatteryWorkSource(null);
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        this.mUserWantsSuspendOpt.set(this.mFacade.getIntegerSetting(this.mContext, "wifi_suspend_optimizations_enabled", 1) == 1);
        updateConnectedMacRandomizationSetting();
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039851);
        addState(this.mDefaultState);
        addState(this.mConnectModeState, this.mDefaultState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mWpsRunningState, this.mConnectModeState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        setInitialState(this.mDefaultState);
        if (!ActivityManager.isLowRamDeviceStatic()) {
            boolean z = SystemProperties.getBoolean("ro.config.hw_low_ram", false);
        }
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        start();
        handleScreenStateChanged(powerManager.isInteractive());
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        HwWifiServiceFactory.getHwWifiDevicePolicy().registerBroadcasts(this.mContext);
    }

    private void registerForWifiMonitorEvents() {
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_START_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.HS20_REMEDIATION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.RX_HS20_ANQP_ICON_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, this.mWifiMetrics.getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_START_OKC_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.EVENT_ANT_CORE_ROB, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.EAP_ERRORCODE_REPORT_EVENT, getHandler());
    }

    /* access modifiers changed from: private */
    public boolean mactchResultAndConfigSecurity(ScanResult scanResult, WifiConfiguration config) {
        boolean z = true;
        if (ScanResultUtil.isScanResultForWapiPskNetwork(scanResult)) {
            Log.d(TAG, "isScanResultForWapiPskNetwork");
            if (!config.allowedKeyManagement.get(8) && !config.allowedKeyManagement.get(10)) {
                z = false;
            }
            return z;
        } else if (ScanResultUtil.isScanResultForCertNetwork(scanResult)) {
            Log.d(TAG, "isScanResultForCertNetwork");
            if (!config.allowedKeyManagement.get(9) && !config.allowedKeyManagement.get(11)) {
                z = false;
            }
            return z;
        } else if (ScanResultUtil.isScanResultForEapNetwork(scanResult)) {
            Log.d(TAG, "isScanResultForEapNetwork");
            if (!config.allowedKeyManagement.get(2) && !config.allowedKeyManagement.get(3) && !config.allowedKeyManagement.get(7)) {
                z = false;
            }
            return z;
        } else if (ScanResultUtil.isScanResultForPskNetwork(scanResult)) {
            Log.d(TAG, "isScanResultForPskNetwork");
            if (!config.allowedKeyManagement.get(1) && !config.allowedKeyManagement.get(6)) {
                z = false;
            }
            return z;
        } else if (ScanResultUtil.isScanResultForWepNetwork(scanResult)) {
            Log.d(TAG, "isScanResultForWepNetwork");
            if (config.wepKeys[0] == null) {
                z = false;
            }
            return z;
        } else {
            Log.d(TAG, "isScanResultForNone");
            if (config.wepKeys[0] != null) {
                z = false;
            }
            return z;
        }
    }

    public void setScreenOffMulticastFilter(boolean enabled) {
        if (this.mIpClient != null) {
            this.mIpClient.setScreenOffMulticastFilter(enabled);
        }
    }

    /* access modifiers changed from: private */
    public void stopIpClient() {
        handlePostDhcpSetup();
        this.mIpClient.stop();
    }

    /* access modifiers changed from: package-private */
    public PendingIntent getPrivateBroadcast(String action, int requestCode) {
        Intent intent = new Intent(action, null);
        intent.addFlags(67108864);
        intent.setPackage("android");
        return this.mFacade.getBroadcast(this.mContext, requestCode, intent, 0);
    }

    /* access modifiers changed from: package-private */
    public void setSupplicantLogLevel() {
        this.mWifiNative.setSupplicantLogLevel(this.mVerboseLoggingEnabled);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
            setLogRecSize(ActivityManager.isLowRamDeviceStatic() ? ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS : DEFAULT_POLL_RSSI_INTERVAL_MSECS);
        } else {
            this.mVerboseLoggingEnabled = false;
            setLogRecSize(100);
        }
        configureVerboseHalLogging(this.mVerboseLoggingEnabled);
        setSupplicantLogLevel();
        this.mCountryCode.enableVerboseLogging(verbose);
        this.mWifiScoreReport.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        this.mWifiMonitor.enableVerboseLogging(verbose);
        this.mWifiNative.enableVerboseLogging(verbose);
        this.mWifiConfigManager.enableVerboseLogging(verbose);
        this.mSupplicantStateTracker.enableVerboseLogging(verbose);
        this.mPasspointManager.enableVerboseLogging(verbose);
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    public void clearANQPCache() {
    }

    public boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039850);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = {(byte) (Integer.parseInt(ouiParts[0], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[1], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[2], 16) & Constants.BYTE_MASK)};
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(this.mInterfaceName, ouiBytes);
    }

    public boolean clearRandomMacOui() {
        logd("Clear random OUI");
        return this.mWifiNative.setScanningMacOui(this.mInterfaceName, new byte[]{0, 0, 0});
    }

    public void gameKOGAdjustSpeed(int mode) {
        this.mWifiNative.gameKOGAdjustSpeed(this.mWifiInfo.getFrequency(), mode);
    }

    public int setCmdToWifiChip(String iface, int mode, int type, int action, int param) {
        return this.mWifiNative.setCmdToWifiChip(iface, mode, type, action, param);
    }

    public void onMssSyncResultEvent(NativeMssResult mssstru) {
        if (checkAndGetHwMSSHandlerManager() != null) {
            this.mHwMssHandler.onMssDrvEvent(mssstru);
        }
    }

    /* access modifiers changed from: private */
    public boolean connectToUserSelectNetwork(int netId, int uid, boolean forceReconnect) {
        logd("connectToUserSelectNetwork netId " + netId + ", uid " + uid + ", forceReconnect = " + forceReconnect);
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (config == null) {
            loge("connectToUserSelectNetwork Invalid network Id=" + netId);
            return false;
        }
        if (HuaweiTelephonyConfigs.isChinaMobile()) {
            this.mWifiConfigManager.updatePriority(config, uid);
        }
        if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, true)) {
            return false;
        }
        if (!this.mWifiConfigManager.enableNetwork(netId, true, uid) || !this.mWifiConfigManager.updateLastConnectUid(netId, uid)) {
            logi("connectToUserSelectNetwork Allowing uid " + uid + " with insufficient permissions to connect=" + netId);
        } else if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
            this.mWifiConnectivityManager.setUserConnectChoice(netId);
        }
        if (forceReconnect || this.mWifiInfo.getNetworkId() != netId) {
            this.mWifiConnectivityManager.prepareForForcedConnection(netId);
            if (!config.hiddenSSID || !TextUtils.isEmpty(config.oriSsid)) {
                startConnectToUserSelectNetwork(netId, uid, "any");
            } else {
                Log.d(TAG, "INTERCEPT1 the connect request since hidden and null oriSsid.");
                this.mScanRequestProxy.startScanForHiddenNetwork(new HiddenScanListener(config, uid), config);
            }
        } else {
            logi("connectToUserSelectNetwork already connecting/connected=" + netId);
            notifyEnableSameNetworkId(netId);
        }
        return true;
    }

    public boolean isP2pConnected() {
        return this.mP2pConnected.get();
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    public long getDisconnectedTimeMilli() {
        if (getCurrentState() != this.mDisconnectedState || this.mDisconnectedTimeStamp == 0) {
            return 0;
        }
        return this.mClock.getWallClockMillis() - this.mDisconnectedTimeStamp;
    }

    private boolean checkOrDeferScanAllowed(Message msg) {
        long now = this.mClock.getWallClockMillis();
        if (this.lastConnectAttemptTimestamp == 0 || now - this.lastConnectAttemptTimestamp >= 10000) {
            return true;
        }
        if (now - this.lastConnectAttemptTimestamp < 0) {
            logd("checkOrDeferScanAllowed time is jump!!!");
            this.lastConnectAttemptTimestamp = now;
        }
        sendMessageDelayed(Message.obtain(msg), 11000 - (now - this.lastConnectAttemptTimestamp));
        return false;
    }

    /* access modifiers changed from: package-private */
    public String reportOnTime() {
        long now = this.mClock.getWallClockMillis();
        StringBuilder sb = new StringBuilder();
        int on = this.mOnTime - this.mOnTimeLastReport;
        this.mOnTimeLastReport = this.mOnTime;
        int tx = this.mTxTime - this.mTxTimeLastReport;
        this.mTxTimeLastReport = this.mTxTime;
        int rx = this.mRxTime - this.mRxTimeLastReport;
        this.mRxTimeLastReport = this.mRxTime;
        this.lastOntimeReportTimeStamp = now;
        sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(tx), Integer.valueOf(rx), Integer.valueOf((int) (now - this.lastOntimeReportTimeStamp))}));
        sb.append(String.format(" from screen [on:%d period:%d]", new Object[]{Integer.valueOf(this.mOnTime - this.mOnTimeScreenStateChange), Integer.valueOf((int) (now - this.lastScreenStateChangeTimeStamp))}));
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public WifiLinkLayerStats getWifiLinkLayerStats() {
        if (this.mInterfaceName == null) {
            loge("getWifiLinkLayerStats called without an interface");
            return null;
        }
        this.lastLinkLayerStatsUpdate = this.mClock.getWallClockMillis();
        WifiLinkLayerStats stats = this.mWifiNative.getWifiLinkLayerStats(this.mInterfaceName);
        if (stats != null) {
            this.mOnTime = stats.on_time;
            this.mTxTime = stats.tx_time;
            this.mRxTime = stats.rx_time;
            this.mRunningBeaconCount = stats.beacon_rx;
            this.mWifiInfo.updatePacketRates(stats, this.lastLinkLayerStatsUpdate);
        } else {
            long mTxPkts = this.mFacade.getTxPackets(this.mInterfaceName);
            this.mWifiInfo.updatePacketRates(mTxPkts, this.mFacade.getRxPackets(this.mInterfaceName), this.lastLinkLayerStatsUpdate);
        }
        return stats;
    }

    private byte[] getDstMacForKeepalive(KeepalivePacketData packetData) throws KeepalivePacketData.InvalidPacketException {
        try {
            return NativeUtil.macAddressToByteArray(macAddressFromRoute(RouteInfo.selectBestRoute(this.mLinkProperties.getRoutes(), packetData.dstAddress).getGateway().getHostAddress()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new KeepalivePacketData.InvalidPacketException(-21);
        }
    }

    private static int getEtherProtoForKeepalive(KeepalivePacketData packetData) throws KeepalivePacketData.InvalidPacketException {
        if (packetData.dstAddress instanceof Inet4Address) {
            return OsConstants.ETH_P_IP;
        }
        if (packetData.dstAddress instanceof Inet6Address) {
            return OsConstants.ETH_P_IPV6;
        }
        throw new KeepalivePacketData.InvalidPacketException(-21);
    }

    /* access modifiers changed from: package-private */
    public int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        KeepalivePacketData.InvalidPacketException e;
        try {
            byte[] packet = packetData.getPacket();
            try {
                byte[] dstMac = getDstMacForKeepalive(packetData);
                try {
                    int ret = this.mWifiNative.startSendingOffloadedPacket(this.mInterfaceName, slot, dstMac, packet, getEtherProtoForKeepalive(packetData), intervalSeconds * 1000);
                    if (ret == 0) {
                        return 0;
                    }
                    loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds + "): hardware error " + ret);
                    return -31;
                } catch (KeepalivePacketData.InvalidPacketException e2) {
                    e = e2;
                    byte[] bArr = dstMac;
                    return e.error;
                }
            } catch (KeepalivePacketData.InvalidPacketException e3) {
                e = e3;
                return e.error;
            }
        } catch (KeepalivePacketData.InvalidPacketException e4) {
            e = e4;
            return e.error;
        }
    }

    /* access modifiers changed from: package-private */
    public int stopWifiIPPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(this.mInterfaceName, slot);
        if (ret == 0) {
            return 0;
        }
        loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
        return -31;
    }

    /* access modifiers changed from: package-private */
    public int startRssiMonitoringOffload(byte maxRssi, byte minRssi, WifiNative.WifiRssiEventHandler rssiHandler) {
        return this.mWifiNative.startRssiMonitoring(this.mInterfaceName, maxRssi, minRssi, rssiHandler);
    }

    /* access modifiers changed from: package-private */
    public int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring(this.mInterfaceName);
    }

    public void setWifiStateForApiCalls(int newState) {
        switch (newState) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "setting wifi state to: " + newState);
                }
                this.mWifiState.set(newState);
                log("setWifiState: " + syncGetWifiStateByName());
                return;
            default:
                Log.d(TAG, "attempted to set an invalid state: " + newState);
                return;
        }
    }

    public int syncGetWifiState() {
        return this.mWifiState.get();
    }

    public String syncGetWifiStateByName() {
        switch (this.mWifiState.get()) {
            case 0:
                return "disabling";
            case 1:
                return "disabled";
            case 2:
                return "enabling";
            case 3:
                return "enabled";
            case 4:
                return "unknown state";
            default:
                return "[invalid state]";
        }
    }

    public boolean isConnected() {
        return getCurrentState() == this.mConnectedState;
    }

    public boolean isDisconnected() {
        return getCurrentState() == this.mDisconnectedState;
    }

    public boolean isSupplicantTransientState() {
        SupplicantState supplicantState = this.mWifiInfo.getSupplicantState();
        if (supplicantState == SupplicantState.ASSOCIATING || supplicantState == SupplicantState.AUTHENTICATING || supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || supplicantState == SupplicantState.GROUP_HANDSHAKE) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Supplicant is under transient state: " + supplicantState);
            }
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Supplicant is under steady state: " + supplicantState);
        }
        return false;
    }

    public WifiInfo syncRequestConnectionInfo(String callingPackage, int uid) {
        if (!isWifiSelfCuring()) {
            return new WifiInfo(this.mWifiInfo);
        }
        WifiInfo result = new WifiInfo(this.mWifiInfo);
        result.setNetworkId(getSelfCureNetworkId());
        if (result.getRssi() <= -127) {
            result.setRssi(-70);
        }
        result.setSupplicantState(SupplicantState.COMPLETED);
        return result;
    }

    public WifiInfo getWifiInfo() {
        return this.mWifiInfo;
    }

    public DhcpResults syncGetDhcpResults() {
        DhcpResults dhcpResults;
        synchronized (this.mDhcpResultsLock) {
            dhcpResults = new DhcpResults(this.mDhcpResults);
        }
        return dhcpResults;
    }

    public void handleIfaceDestroyed() {
        handleNetworkDisconnect();
    }

    public void setOperationalMode(int mode, String ifaceName) {
        if (this.mVerboseLoggingEnabled) {
            log("setting operational mode to " + String.valueOf(mode) + " for iface: " + ifaceName);
        }
        this.mModeChange = true;
        if (mode != 1) {
            transitionTo(this.mDefaultState);
        } else if (ifaceName != null) {
            this.mInterfaceName = ifaceName;
            transitionTo(this.mDisconnectedState);
        } else {
            Log.e(TAG, "supposed to enter connect mode, but iface is null -> DefaultState");
            transitionTo(this.mDefaultState);
        }
        sendMessageAtFrontOfQueue(CMD_SET_OPERATIONAL_MODE);
    }

    public void takeBugReport(String bugTitle, String bugDetail) {
        this.mWifiDiagnostics.takeBugReport(bugTitle, bugDetail);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getOperationalModeForTest() {
        return this.mOperationalMode;
    }

    /* access modifiers changed from: protected */
    public WifiMulticastLockManager.FilterController getMcastLockManagerFilterController() {
        return this.mMcastLockManagerFilterController;
    }

    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        Bundle bundle = new Bundle();
        bundle.putLong("BSSID", bssid);
        bundle.putString(EXTRA_OSU_ICON_QUERY_FILENAME, fileName);
        Message resultMsg = channel.sendMessageSynchronously(CMD_QUERY_OSU_ICON, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result == 1;
    }

    public int matchProviderWithCurrentNetwork(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_MATCH_PROVIDER_NETWORK, fqdn);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public void deauthenticateNetwork(AsyncChannel channel, long holdoff, boolean ess) {
    }

    public void disableEphemeralNetwork(String SSID) {
        if (SSID != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, SSID);
        }
    }

    public List<ScanDetail> getScanResultsListNoCopyUnsync() {
        return null;
    }

    public void disconnectCommand() {
        sendMessage(CMD_DISCONNECT);
    }

    public void disconnectCommand(int uid, int reason) {
        sendMessage(CMD_DISCONNECT, uid, reason);
    }

    public void reconnectCommand(WorkSource workSource) {
        sendMessage(CMD_RECONNECT, workSource);
    }

    public void reassociateCommand() {
        sendMessage(CMD_REASSOCIATE);
    }

    public void reloadTlsNetworksAndReconnect() {
        sendMessage(CMD_RELOAD_TLS_AND_RECONNECT);
    }

    public int syncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration config) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_NETWORK, config);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONFIGURED_NETWORKS, uuid);
        if (resultMsg == null) {
            return null;
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public WifiConfiguration syncGetMatchingWifiConfig(ScanResult scanResult, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_MATCHING_CONFIG, scanResult);
        WifiConfiguration config = (WifiConfiguration) resultMsg.obj;
        resultMsg.recycle();
        return config;
    }

    /* access modifiers changed from: package-private */
    public List<WifiConfiguration> getAllMatchingWifiConfigs(ScanResult scanResult, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_ALL_MATCHING_CONFIGS, scanResult);
        List<WifiConfiguration> configs = (List) resultMsg.obj;
        resultMsg.recycle();
        return configs;
    }

    public List<OsuProvider> syncGetMatchingOsuProviders(ScanResult scanResult, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_MATCHING_OSU_PROVIDERS, scanResult);
        List<OsuProvider> providers = (List) resultMsg.obj;
        resultMsg.recycle();
        return providers;
    }

    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel, PasspointConfiguration config, int uid) {
        boolean result = false;
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG, uid, 0, config);
        if (resultMsg.arg1 == 1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemovePasspointConfig(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_PASSPOINT_CONFIG, fqdn);
        boolean z = true;
        if (resultMsg.arg1 != 1) {
            z = false;
        }
        boolean result = z;
        resultMsg.recycle();
        return result;
    }

    public List<PasspointConfiguration> syncGetPasspointConfigs(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PASSPOINT_CONFIGS);
        List<PasspointConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncStartSubscriptionProvisioning(int callingUid, OsuProvider provider, IProvisioningCallback callback, AsyncChannel channel) {
        Message msg = Message.obtain();
        msg.what = CMD_START_SUBSCRIPTION_PROVISIONING;
        msg.arg1 = callingUid;
        msg.obj = callback;
        msg.getData().putParcelable(EXTRA_OSU_PROVIDER, provider);
        Message resultMsg = channel.sendMessageSynchronously(msg);
        boolean result = resultMsg.arg1 != 0;
        resultMsg.recycle();
        return result;
    }

    public int syncGetSupportedFeatures(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_SUPPORTED_FEATURES);
        int supportedFeatureSet = resultMsg.arg1;
        resultMsg.recycle();
        if (this.mPropertyService.getBoolean("config.disable_rtt", false)) {
            return supportedFeatureSet & -385;
        }
        return supportedFeatureSet;
    }

    public WifiLinkLayerStats syncGetLinkLayerStats(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_LINK_LAYER_STATS);
        WifiLinkLayerStats result = (WifiLinkLayerStats) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemoveNetwork(AsyncChannel channel, int networkId) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_NETWORK, networkId);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncEnableNetwork(AsyncChannel channel, int netId, boolean disableOthers) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ENABLE_NETWORK, netId, disableOthers);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncDisableNetwork(AsyncChannel channel, int netId) {
        Message resultMsg = channel.sendMessageSynchronously(151569, netId);
        boolean result = resultMsg.what != 151570;
        resultMsg.recycle();
        return result;
    }

    public void enableRssiPolling(boolean enabled) {
        sendMessage(CMD_ENABLE_RSSI_POLL, enabled, 0);
    }

    public void setHighPerfModeEnabled(boolean enable) {
        sendMessage(CMD_SET_HIGH_PERF_MODE, enable, 0);
    }

    public synchronized void resetSimAuthNetworks(boolean simPresent) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simPresent);
    }

    public void notifyImsiAvailabe(boolean imsiAvailabe) {
        this.mIsImsiAvailable = imsiAvailabe;
    }

    public Network getCurrentNetwork() {
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent == null) {
                return null;
            }
            Network network = new Network(this.mNetworkAgent.netId);
            return network;
        }
    }

    public void enableTdls(String remoteMacAddress, boolean enable) {
        sendMessage(CMD_ENABLE_TDLS, (int) enable, 0, remoteMacAddress);
    }

    public void sendBluetoothAdapterStateChange(int state) {
        sendMessage(CMD_BLUETOOTH_ADAPTER_STATE_CHANGE, state, 0);
    }

    public void removeAppConfigs(String packageName, int uid) {
        ApplicationInfo ai = new ApplicationInfo();
        ai.packageName = packageName;
        ai.uid = uid;
        sendMessage(CMD_REMOVE_APP_CONFIGURATIONS, ai);
    }

    public void removeUserConfigs(int userId) {
        sendMessage(CMD_REMOVE_USER_CONFIGURATIONS, userId);
    }

    public void updateBatteryWorkSource(WorkSource newSource) {
        synchronized (this.mRunningWifiUids) {
            if (newSource != null) {
                try {
                    this.mRunningWifiUids.set(newSource);
                } catch (RemoteException e) {
                }
            }
            if (this.mIsRunning) {
                if (!this.mReportedRunning) {
                    this.mBatteryStats.noteWifiRunning(this.mRunningWifiUids);
                    this.mLastRunningWifiUids.set(this.mRunningWifiUids);
                    this.mReportedRunning = true;
                } else if (!this.mLastRunningWifiUids.equals(this.mRunningWifiUids)) {
                    this.mBatteryStats.noteWifiRunningChanged(this.mLastRunningWifiUids, this.mRunningWifiUids);
                    this.mLastRunningWifiUids.set(this.mRunningWifiUids);
                }
            } else if (this.mReportedRunning) {
                this.mBatteryStats.noteWifiStopped(this.mLastRunningWifiUids);
                this.mLastRunningWifiUids.clear();
                this.mReportedRunning = false;
            }
            this.mWakeLock.setWorkSource(newSource);
            try {
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void dumpIpClient(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mIpClient != null) {
            this.mIpClient.dump(fd, pw, args);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        this.mWifiInjector.dump(fd, pw, args);
        this.mSupplicantStateTracker.dump(fd, pw, args);
        pw.println("mLinkProperties " + this.mLinkProperties);
        pw.println("mWifiInfo " + this.mWifiInfo);
        pw.println("mDhcpResults " + this.mDhcpResults);
        pw.println("mNetworkInfo " + this.mNetworkInfo);
        pw.println("mLastSignalLevel " + this.mLastSignalLevel);
        pw.println("mLastBssid " + StringUtil.safeDisplayBssid(this.mLastBssid));
        pw.println("mLastNetworkId " + this.mLastNetworkId);
        pw.println("mOperationalMode " + this.mOperationalMode);
        pw.println("mUserWantsSuspendOpt " + this.mUserWantsSuspendOpt);
        pw.println("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
        this.mCountryCode.dump(fd, pw, args);
        if (this.mNetworkFactory != null) {
            this.mNetworkFactory.dump(fd, pw, args);
        } else {
            pw.println("mNetworkFactory is not initialized");
        }
        if (this.mUntrustedNetworkFactory != null) {
            this.mUntrustedNetworkFactory.dump(fd, pw, args);
        } else {
            pw.println("mUntrustedNetworkFactory is not initialized");
        }
        pw.println("Wlan Wake Reasons:" + this.mWifiNative.getWlanWakeReasonCount());
        pw.println();
        this.mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        this.mPasspointManager.dump(pw);
        pw.println();
        this.mWifiDiagnostics.captureBugReportData(7);
        this.mWifiDiagnostics.dump(fd, pw, args);
        dumpIpClient(fd, pw, args);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.dump(fd, pw, args);
        } else {
            pw.println("mWifiConnectivityManager is not initialized");
        }
        this.mWifiInjector.getWakeupController().dump(fd, pw, args);
    }

    public void handleUserSwitch(int userId) {
        sendMessage(CMD_USER_SWITCH, userId);
    }

    public void handleUserUnlock(int userId) {
        sendMessage(CMD_USER_UNLOCK, userId);
    }

    public void handleUserStop(int userId) {
        sendMessage(CMD_USER_STOP, userId);
    }

    /* access modifiers changed from: private */
    public void logStateAndMessage(Message message, State state) {
        this.messageHandlingStatus = 0;
        String currentStateTag = "";
        if (state == getCurrentState()) {
            currentStateTag = "$";
        }
        if (mLogMessages) {
            switch (message.what) {
                case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                case CMD_GET_SUPPORTED_FEATURES /*131133*/:
                case CMD_GET_LINK_LAYER_STATS /*131135*/:
                case CMD_RSSI_POLL /*131155*/:
                case CMD_UPDATE_LINKPROPERTIES /*131212*/:
                case WifiMonitor.SCAN_RESULTS_EVENT:
                case 151572:
                    if (VDBG) {
                        logd(currentStateTag + state.getClass().getSimpleName() + " " + getLogRecString(message));
                        return;
                    }
                    return;
                default:
                    logd(currentStateTag + state.getClass().getSimpleName() + " " + getLogRecString(message));
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean recordLogRec(Message msg) {
        if (msg.what != CMD_RSSI_POLL) {
            return true;
        }
        return this.mVerboseLoggingEnabled;
    }

    /* access modifiers changed from: protected */
    public String getLogRecString(Message msg) {
        StringBuilder sb = new StringBuilder();
        if (this.mScreenOn) {
            sb.append("!");
        }
        if (this.messageHandlingStatus != 0) {
            sb.append("(");
            sb.append(this.messageHandlingStatus);
            sb.append(")");
        }
        sb.append(smToString(msg));
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=" + msg.sendingUid);
        }
        long duration = this.mClock.getUptimeSinceBootMillis() - msg.getWhen();
        if (duration > 1000) {
            sb.append(" dur:");
            TimeUtils.formatDuration(duration, sb);
        }
        sb.append(" rt=");
        sb.append(this.mClock.getUptimeSinceBootMillis());
        sb.append("/");
        sb.append(this.mClock.getElapsedSinceBootMillis());
        switch (msg.what) {
            case CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    WifiConfiguration config = (WifiConfiguration) msg.obj;
                    sb.append(" ");
                    sb.append(config.configKey());
                    sb.append(" prio=");
                    sb.append(config.priority);
                    sb.append(" status=");
                    sb.append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ");
                        sb.append(StringUtil.safeDisplayBssid(config.BSSID));
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (!curConfig.configKey().equals(config.configKey())) {
                            sb.append(" current=");
                            sb.append(curConfig.configKey());
                            sb.append(" prio=");
                            sb.append(curConfig.priority);
                            sb.append(" status=");
                            sb.append(curConfig.status);
                            break;
                        } else {
                            sb.append(" is current");
                            break;
                        }
                    }
                }
                break;
            case CMD_ENABLE_NETWORK /*131126*/:
            case 151569:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=");
                    sb.append(key);
                }
                WifiConfiguration config2 = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config2 != null && (key == null || !config2.configKey().equals(key))) {
                    sb.append(" target=");
                    sb.append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=");
                sb.append(this.mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_RSSI_POLL /*131155*/:
            case CMD_UNWANTED_NETWORK /*131216*/:
            case 151572:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (!(this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID() == null)) {
                    sb.append(" ");
                    sb.append(this.mWifiInfo.getSSID());
                }
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ");
                    sb.append(StringUtil.safeDisplayBssid(this.mWifiInfo.getBSSID()));
                }
                sb.append(" rssi=");
                sb.append(this.mWifiInfo.getRssi());
                sb.append(" f=");
                sb.append(this.mWifiInfo.getFrequency());
                sb.append(" sc=");
                sb.append(this.mWifiInfo.score);
                sb.append(" link=");
                sb.append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                String report = reportOnTime();
                if (report != null) {
                    sb.append(" ");
                    sb.append(report);
                }
                sb.append(String.format(" score=%d", new Object[]{Integer.valueOf(this.mWifiInfo.score)}));
                break;
            case CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.roamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.disconnectingWatchdogCount);
                break;
            case CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mDisableP2pWatchdogCount);
                break;
            case CMD_IP_CONFIGURATION_LOST /*131211*/:
                int count = -1;
                WifiConfiguration c = getCurrentWifiConfiguration();
                if (c != null) {
                    count = c.getNetworkSelectionStatus().getDisableReasonCounter(4);
                }
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" failures: ");
                sb.append(Integer.toString(count));
                sb.append("/");
                sb.append(Integer.toString(this.mFacade.getIntegerSetting(this.mContext, "wifi_max_dhcp_retry_count", 0)));
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ");
                    sb.append(StringUtil.safeDisplayBssid(this.mWifiInfo.getBSSID()));
                }
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                break;
            case CMD_UPDATE_LINKPROPERTIES /*131212*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            case CMD_TARGET_BSSID /*131213*/:
            case CMD_ASSOCIATED_BSSID /*131219*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" BSSID=");
                    sb.append(StringUtil.safeDisplayBssid((String) msg.obj));
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" Target=");
                    sb.append(StringUtil.safeDisplayBssid(this.mTargetRoamBSSID));
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                break;
            case CMD_START_CONNECT /*131215*/:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config3 = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config3 != null) {
                    sb.append(" ");
                    sb.append(config3.configKey());
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ");
                    sb.append(StringUtil.safeDisplayBssid(this.mTargetRoamBSSID));
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                WifiConfiguration config4 = getCurrentWifiConfiguration();
                if (config4 != null) {
                    sb.append(config4.configKey());
                    break;
                }
                break;
            case CMD_START_ROAM /*131217*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = (ScanResult) msg.obj;
                if (result != null) {
                    Long now = Long.valueOf(this.mClock.getWallClockMillis());
                    sb.append(" bssid=");
                    sb.append(StringUtil.safeDisplayBssid(result.BSSID));
                    sb.append(" rssi=");
                    sb.append(result.level);
                    sb.append(" freq=");
                    sb.append(result.frequency);
                    if (result.seen <= 0 || result.seen >= now.longValue()) {
                        sb.append(" !seen=");
                        sb.append(result.seen);
                    } else {
                        sb.append(" seen=");
                        sb.append(now.longValue() - result.seen);
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ");
                    sb.append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                sb.append(" fail count=");
                sb.append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_IP_REACHABILITY_LOST /*131221*/:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append((String) msg.obj);
                    break;
                }
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
            case CMD_RSSI_THRESHOLD_BREACHED /*131236*/:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                sb.append(" ");
                if (msg.arg1 != 1) {
                    if (msg.arg1 != CMD_STATIC_IP_SUCCESS) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    } else {
                        sb.append("STATIC_OK");
                        break;
                    }
                } else {
                    sb.append("DHCP_OK");
                    break;
                }
            case CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                sb.append(" ");
                if (msg.arg1 != 2) {
                    if (msg.arg1 != CMD_STATIC_IP_FAILURE) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    } else {
                        sb.append("STATIC_FAIL");
                        break;
                    }
                } else {
                    sb.append("DHCP_FAIL");
                    break;
                }
            case CMD_INSTALL_PACKET_FILTER /*131274*/:
                sb.append(" len=" + ((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                sb.append(" enabled=" + ((Boolean) msg.obj).booleanValue());
                break;
            case CMD_USER_SWITCH /*131277*/:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    NetworkInfo info = (NetworkInfo) msg.obj;
                    NetworkInfo.State state = info.getState();
                    NetworkInfo.DetailedState detailedState = info.getDetailedState();
                    if (state != null) {
                        sb.append(" st=");
                        sb.append(state);
                    }
                    if (detailedState != null) {
                        sb.append("/");
                        sb.append(detailedState);
                        break;
                    }
                }
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ");
                sb.append(StringUtil.safeDisplayBssid(this.mLastBssid));
                sb.append(" nid=");
                sb.append(this.mLastNetworkId);
                WifiConfiguration config5 = getCurrentWifiConfiguration();
                if (config5 != null) {
                    sb.append(" ");
                    sb.append(config5.configKey());
                }
                String key2 = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key2 != null) {
                    sb.append(" last=");
                    sb.append(key2);
                    break;
                }
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append((String) msg.obj);
                }
                sb.append(" nid=");
                sb.append(msg.arg1);
                sb.append(" reason=");
                sb.append(msg.arg2);
                if (this.mLastBssid != null) {
                    sb.append(" lastbssid=");
                    sb.append(StringUtil.safeDisplayBssid(this.mLastBssid));
                }
                if (this.mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=");
                    sb.append(this.mWifiInfo.getFrequency());
                    sb.append(" rssi=");
                    sb.append(this.mWifiInfo.getRssi());
                    break;
                }
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                StateChangeResult stateChangeResult = (StateChangeResult) msg.obj;
                if (stateChangeResult != null) {
                    sb.append(stateChangeResult.toString());
                    break;
                }
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                sb.append(" ");
                sb.append(" timedOut=" + Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = (String) msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(StringUtil.safeDisplayBssid(bssid));
                }
                sb.append(" blacklist=" + Boolean.toString(this.didBlackListBSSID));
                break;
            case 151556:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config6 = (WifiConfiguration) msg.obj;
                if (config6 != null) {
                    sb.append(" ");
                    sb.append(config6.configKey());
                    sb.append(" nid=");
                    sb.append(config6.networkId);
                    if (config6.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config6.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (config6.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config6.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=");
                    sb.append(config6.creatorUid);
                    sb.append(" suid=");
                    sb.append(config6.lastUpdateUid);
                    WifiConfiguration.NetworkSelectionStatus netWorkSelectionStatus = config6.getNetworkSelectionStatus();
                    sb.append(" ajst=");
                    sb.append(netWorkSelectionStatus.getNetworkStatusString());
                    break;
                }
                break;
            case 151559:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config7 = (WifiConfiguration) msg.obj;
                if (config7 != null) {
                    sb.append(" ");
                    sb.append(config7.configKey());
                    sb.append(" nid=");
                    sb.append(config7.networkId);
                    if (config7.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config7.preSharedKey != null && !config7.preSharedKey.equals("*")) {
                        sb.append(" hasPSK");
                    }
                    if (config7.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config7.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=");
                    sb.append(config7.creatorUid);
                    sb.append(" suid=");
                    sb.append(config7.lastUpdateUid);
                    break;
                }
                break;
            case 196611:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=");
                sb.append(this.mWifiInfo.txSuccess);
                sb.append(",");
                sb.append(this.mWifiInfo.txBad);
                sb.append(",");
                sb.append(this.mWifiInfo.txRetries);
                break;
            case 196612:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.arg1 == 1) {
                    sb.append(" OK ");
                } else if (msg.arg1 == 2) {
                    sb.append(" FAIL ");
                }
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            default:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                break;
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
        if (this.mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
        }
        enableRssiPolling(screenOn);
        if (this.mUserWantsSuspendOpt.get()) {
            int shouldReleaseWakeLock = 0;
            if (screenOn) {
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 0, 0);
            } else {
                if (isConnected()) {
                    this.mSuspendWakeLock.acquire(2000);
                    shouldReleaseWakeLock = 1;
                }
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 1, shouldReleaseWakeLock);
            }
        }
        if (this.mIsRunning != 0) {
            getWifiLinkLayerStats();
            this.mOnTimeScreenStateChange = this.mOnTime;
            this.lastScreenStateChangeTimeStamp = this.lastLinkLayerStatsUpdate;
        }
        this.mWifiMetrics.setScreenState(screenOn);
        if (this.mWifiConnectivityManager != null) {
            long currenTime = this.mClock.getElapsedSinceBootMillis();
            if (screenOn && currenTime - this.mLastAllowSendHiLinkScanResultsBroadcastTime > 3000) {
                Log.d(TAG, "handleScreenStateChanged: allow send HiLink scan results broadcast.");
                this.mScanRequestProxy.mAllowSendHiLinkScanResultsBroadcast = true;
                this.mLastAllowSendHiLinkScanResultsBroadcastTime = currenTime;
                this.mScanRequestProxy.mSendHiLinkScanResultsBroadcastTries = 0;
            }
            this.mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        }
        if (this.mVerboseLoggingEnabled) {
            log("handleScreenStateChanged Exit: " + screenOn);
        }
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    /* access modifiers changed from: private */
    public void setSuspendOptimizationsNative(int reason, boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            log("setSuspendOptimizationsNative: " + reason + " " + enabled + " -want " + this.mUserWantsSuspendOpt.get() + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
            if (this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get()) {
                if (this.mVerboseLoggingEnabled) {
                    log("setSuspendOptimizationsNative do it " + reason + " " + enabled + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
                }
                this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, true);
                return;
            }
            return;
        }
        this.mSuspendOptNeedsDisabled |= reason;
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, false);
    }

    /* access modifiers changed from: private */
    public void setSuspendOptimizations(int reason, boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            log("setSuspendOptimizations: " + reason + " " + enabled);
        }
        if (enabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
        } else {
            this.mSuspendOptNeedsDisabled |= reason;
        }
        if (this.mVerboseLoggingEnabled) {
            log("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
        }
    }

    public void setWifiStateForHw(int wifiState) {
        if (wifiState == 3) {
            broadcastWifiDriverChanged(1);
        } else if (wifiState == 1) {
            broadcastWifiDriverChanged(2);
            if (this.isBootCompleted && this.mWifiP2pServiceImpl != null) {
                this.mWifiP2pServiceImpl.setWifiRepeaterState(HwWifiServiceFactory.getHwConstantUtils().getWifiP2pDisabledStateVal());
            }
        }
    }

    public void setmSettingsStore(WifiSettingsStore settingsStore) {
        this.mWifiSettingStore = settingsStore;
    }

    public boolean attemptAutoConnect() {
        SupplicantState state = this.mWifiInfo.getSupplicantState();
        if (getCurrentState() != this.mRoamingState && getCurrentState() != this.mObtainingIpState && getCurrentState() != this.mDisconnectingState && state != SupplicantState.ASSOCIATING && state != SupplicantState.ASSOCIATED && state != SupplicantState.AUTHENTICATING && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.GROUP_HANDSHAKE) {
            return true;
        }
        Log.w(TAG, "attemptAutoConnect: false");
        return false;
    }

    public void setCHRConnectingSartTimestamp(long connectingStartTimestamp) {
        if (connectingStartTimestamp > 0) {
            this.mConnectingStartTimestamp = connectingStartTimestamp;
        }
    }

    /* access modifiers changed from: private */
    public void fetchRssiLinkSpeedAndFrequencyNative() {
        if (SupplicantState.ASSOCIATED.compareTo(this.mWifiInfo.getSupplicantState()) > 0 || SupplicantState.COMPLETED.compareTo(this.mWifiInfo.getSupplicantState()) < 0) {
            loge("error state to fetch rssi");
            return;
        }
        WifiNative.SignalPollResult pollResult = this.mWifiNative.signalPoll(this.mInterfaceName);
        if (pollResult != null) {
            Integer newRssi = Integer.valueOf(pollResult.currentRssi);
            Integer newLinkSpeed = Integer.valueOf(pollResult.txBitrate);
            Integer newFrequency = Integer.valueOf(pollResult.associationFrequency);
            this.mWifiInfo.setNoise(pollResult.currentNoise);
            this.mWifiInfo.setSnr(pollResult.currentSnr);
            this.mWifiInfo.setChload(pollResult.currentChload);
            if (this.mVerboseLoggingEnabled) {
                logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi + " linkspeed=" + newLinkSpeed + " freq=" + newFrequency);
            }
            if (newRssi == null || newRssi.intValue() <= -127 || newRssi.intValue() >= 200) {
                this.mWifiInfo.setRssi(WifiMetrics.MIN_RSSI_DELTA);
                updateCapabilities();
            } else {
                if (newRssi.intValue() > 5) {
                    newRssi = Integer.valueOf(newRssi.intValue() - 256);
                }
                this.mWifiInfo.setRssi(newRssi.intValue());
                if (checkAndGetHwMSSHandlerManager() != null) {
                    this.mHwMssHandler.mssSwitchCheck(newRssi.intValue());
                }
                if (isAllowedManualWifiPwrBoost() == 0) {
                    this.mWifiNative.pwrPercentBoostModeset(newRssi.intValue());
                }
                int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), newRssi.intValue());
                if (newSignalLevel != this.mLastSignalLevel) {
                    updateCapabilities();
                    sendRssiChangeBroadcast(newRssi.intValue());
                }
                this.mLastSignalLevel = newSignalLevel;
            }
            if (newLinkSpeed != null) {
                this.mWifiInfo.setLinkSpeed(newLinkSpeed.intValue());
            }
            if (newFrequency != null && newFrequency.intValue() > 0) {
                this.mWifiInfo.setFrequency(newFrequency.intValue());
                sendStaFrequency(newFrequency.intValue());
            }
            this.mWifiConfigManager.updateScanDetailCacheFromWifiInfo(this.mWifiInfo);
            if (!(newRssi == null || newLinkSpeed == null || newFrequency == null)) {
                this.mWifiMetrics.handlePollResult(this.mWifiInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    public void cleanWifiScore() {
        this.mWifiInfo.txBadRate = 0.0d;
        this.mWifiInfo.txSuccessRate = 0.0d;
        this.mWifiInfo.txRetriesRate = 0.0d;
        this.mWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport.reset();
    }

    /* access modifiers changed from: private */
    public void updateLinkProperties(LinkProperties newLp) {
        if (this.mVerboseLoggingEnabled) {
            log("Link configuration changed for netId: " + this.mLastNetworkId + " old: " + this.mLinkProperties + " new: " + newLp);
        }
        this.mLinkProperties = newLp;
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        if (getNetworkDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            sendLinkConfigurationChangedBroadcast();
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateLinkProperties nid: " + this.mLastNetworkId);
            sb.append(" state: " + getNetworkDetailedState());
            if (this.mLinkProperties != null) {
                sb.append(" ");
                sb.append(getLinkPropertiesSummary(this.mLinkProperties));
            }
            logd(sb.toString());
        }
    }

    private void clearLinkProperties() {
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        this.mLinkProperties.clear();
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
    }

    private String updateDefaultRouteMacAddress(int timeout) {
        String address = null;
        for (RouteInfo route : this.mLinkProperties.getRoutes()) {
            if (route != null && route.isDefaultRoute() && route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                if (gateway instanceof Inet4Address) {
                    if (this.mVerboseLoggingEnabled) {
                        logd("updateDefaultRouteMacAddress found Ipv4 default :" + gateway.getHostAddress());
                    }
                    address = macAddressFromRoute(gateway.getHostAddress());
                    if (address == null && timeout > 0) {
                        try {
                            Thread.sleep((long) timeout);
                        } catch (InterruptedException e) {
                        }
                        address = macAddressFromRoute(gateway.getHostAddress());
                    }
                    if (address != null) {
                        this.mWifiConfigManager.setNetworkDefaultGwMacAddress(this.mLastNetworkId, address);
                    }
                }
            }
        }
        return address;
    }

    /* access modifiers changed from: private */
    public void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        Intent intent = new Intent("android.net.wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public void sendNetworkStateChangeBroadcast(String bssid) {
        Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
        intent.addFlags(67108864);
        NetworkInfo networkInfo = new NetworkInfo(this.mNetworkInfo);
        networkInfo.setExtraInfo(null);
        intent.putExtra("networkInfo", networkInfo);
        checkSelfCureWifiResult(102);
        if (!ignoreNetworkStateChange(this.mNetworkInfo)) {
            Log.i(TAG, "NetworkStateChange " + this.mNetworkInfo.getState() + "/" + this.mNetworkInfo.getDetailedState());
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void sendLinkConfigurationChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendSupplicantConnectionChangedBroadcast(boolean connected) {
        Intent intent = new Intent("android.net.wifi.supplicant.CONNECTION_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("connected", connected);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public boolean setNetworkDetailedState(NetworkInfo.DetailedState state) {
        boolean hidden = false;
        if (this.mIsAutoRoaming) {
            hidden = true;
        }
        if (this.mVerboseLoggingEnabled) {
            log("setDetailed state, old =" + this.mNetworkInfo.getDetailedState() + " and new state=" + state + " hidden=" + hidden);
        }
        if (hidden || state == this.mNetworkInfo.getDetailedState()) {
            return false;
        }
        this.mNetworkInfo.setDetailedState(state, null, null);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private NetworkInfo.DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    /* access modifiers changed from: private */
    public SupplicantState handleSupplicantStateChange(Message message) {
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        SupplicantState state = stateChangeResult.state;
        this.mWifiInfo.setSupplicantState(state);
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
            this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
            this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        } else {
            this.mWifiInfo.setNetworkId(-1);
            this.mWifiInfo.setBSSID(null);
            this.mWifiInfo.setSSID(null);
        }
        updateCapabilities();
        if (SupplicantState.AUTHENTICATING == state || SupplicantState.ASSOCIATED == state) {
            fetchRssiLinkSpeedAndFrequencyNative();
        }
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            stateChangeResult.networkId = config.networkId;
            this.mWifiInfo.setEphemeral(config.ephemeral);
            ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
            if (scanDetailCache != null) {
                ScanDetail scanDetail = scanDetailCache.getScanDetail(stateChangeResult.BSSID);
                if (scanDetail != null) {
                    this.mWifiInfo.setFrequency(scanDetail.getScanResult().frequency);
                    NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                    if (networkDetail != null && networkDetail.getAnt() == NetworkDetail.Ant.ChargeablePublic) {
                        this.mWifiInfo.setMeteredHint(true);
                    }
                }
            }
        }
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
        } else {
            this.mWifiInfo.setNetworkId(-1);
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        return state;
    }

    private void updateDefaultMtu() {
        if (this.mLinkProperties == null || this.mNwService == null) {
            loge("LinkProperties or NwService is null.");
            return;
        }
        String iface = this.mLinkProperties.getInterfaceName();
        if (!TextUtils.isEmpty(iface)) {
            if (DEFAULT_MTU == this.mLinkProperties.getMtu()) {
                log("MTU is same as the default: 1500");
                return;
            }
            this.mLinkProperties.setMtu(DEFAULT_MTU);
            try {
                log("Setting MTU size: " + iface + ", " + DEFAULT_MTU);
                this.mNwService.setMtu(iface, DEFAULT_MTU);
            } catch (Exception e) {
                loge("exception in setMtu()" + e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleNetworkDisconnect() {
        if (DBG) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        stopRssiMonitoringOffload();
        clearTargetBssid("handleNetworkDisconnect");
        stopIpClient();
        this.mWifiScoreReport.reset();
        this.mWifiInfo.reset();
        this.mIsAutoRoaming = false;
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent != null) {
                this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
                this.mNetworkAgent = null;
            }
        }
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (TelephonyUtil.isSimConfig(config) && TelephonyUtil.getSimIdentity(getTelephonyManager(), new TelephonyUtil(), config) == null) {
            Log.d(TAG, "remove EAP-SIM/AKA/AKA' network " + this.mLastNetworkId + " from wpa_supplicant since identity was absent.");
            this.mWifiNative.removeAllNetworks(this.mInterfaceName);
        }
        updateDefaultMtu();
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        this.mLastBssid = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
    }

    /* access modifiers changed from: package-private */
    public void handlePreDhcpSetup() {
        this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 1);
        setSuspendOptimizationsNative(1, false);
        this.mWifiNative.setPowerSave(this.mInterfaceName, false);
        getWifiLinkLayerStats();
        if (this.mWifiP2pChannel != null) {
            Message msg = new Message();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = 1;
            msg.arg2 = 196614;
            msg.obj = this;
            this.mWifiP2pChannel.sendMessage(msg);
            return;
        }
        sendMessage(196614);
    }

    /* access modifiers changed from: package-private */
    public void handlePostDhcpSetup() {
        setSuspendOptimizationsNative(1, true);
        this.mWifiNative.setPowerSave(this.mInterfaceName, true);
        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 2);
    }

    /* access modifiers changed from: private */
    public void reportConnectionAttemptStart(WifiConfiguration config, String targetBSSID, int roamType) {
        this.mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        this.mDiagsConnectionStartMillis = this.mClock.getElapsedSinceBootMillis();
        this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 0);
        Long lDiagsConnectionStartMillis = Long.valueOf(this.mDiagsConnectionStartMillis);
        if (lDiagsConnectionStartMillis == null) {
            loge("reportConnectionAttemptStart : lDiagsConnectionStartMillis is null");
        }
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT, lDiagsConnectionStartMillis, 60000);
    }

    /* access modifiers changed from: private */
    public void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode) {
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode);
        this.mWifiConnectivityManager.handleConnectionAttemptEnded(level2FailureCode);
        if (level2FailureCode == 1) {
            this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 1);
        } else if (!(level2FailureCode == 5 || level2FailureCode == 8)) {
            this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 2);
        }
        this.mDiagsConnectionStartMillis = -1;
    }

    /* access modifiers changed from: private */
    public void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            StringBuilder sb = new StringBuilder();
            sb.append("link address ");
            sb.append(dhcpResults.ipAddress);
            logd(sb.toString());
        }
        synchronized (this.mDhcpResultsLock) {
            this.mDhcpResults = dhcpResults;
            addr = (Inet4Address) dhcpResults.ipAddress.getAddress();
        }
        if (this.mIsAutoRoaming && this.mWifiInfo.getIpAddress() != NetworkUtils.inetAddressToInt(addr)) {
            logd("handleIPv4Success, roaming and address changed" + this.mWifiInfo + " got: " + addr);
        }
        this.mWifiInfo.setInetAddress(addr);
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            this.mWifiInfo.setEphemeral(config.ephemeral);
        }
        if (dhcpResults.hasMeteredHint() || hasMeteredHintForWi(addr)) {
            this.mWifiInfo.setMeteredHint(true);
        }
        updateCapabilities(config);
    }

    /* access modifiers changed from: private */
    public void handleSuccessfulIpConfiguration() {
        this.mLastSignalLevel = -1;
        WifiConfiguration c = getCurrentWifiConfiguration();
        if (c != null) {
            c.getNetworkSelectionStatus().clearDisableReasonCounter(4);
            updateCapabilities(c);
        }
    }

    /* access modifiers changed from: private */
    public void handleIPv4Failure() {
        this.mWifiDiagnostics.captureBugReportData(4);
        if (this.mVerboseLoggingEnabled) {
            int count = -1;
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (config != null) {
                count = config.getNetworkSelectionStatus().getDisableReasonCounter(4);
            }
            log("DHCP failure count=" + count);
        }
        reportConnectionAttemptEnd(10, 2);
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Failure");
        }
    }

    /* access modifiers changed from: private */
    public void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        if (DBG) {
            loge("handleIpConfigurationLost: SSID = " + this.mWifiInfo.getSSID() + ", BSSID = " + this.mWifiInfo.getBSSID());
        }
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        notifyWifiConnFailedInfo(this.mLastNetworkId, this.mWifiInfo.getBSSID(), this.mWifiInfo.getRssi(), 4, this.mWifiConnectivityManager);
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    /* access modifiers changed from: private */
    public void handleIpReachabilityLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    private String macAddressFromRoute(String ipAddress) {
        String macAddress = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            String readLine = reader2.readLine();
            while (true) {
                String readLine2 = reader2.readLine();
                String line = readLine2;
                if (readLine2 == null) {
                    break;
                }
                String[] tokens = line.split("[ ]+");
                if (tokens.length >= 6) {
                    String ip = tokens[0];
                    String mac = tokens[3];
                    if (ipAddress.equals(ip)) {
                        macAddress = mac;
                        break;
                    }
                }
            }
            if (macAddress == null) {
                loge("Did not find remoteAddress {" + ipAddress + "} in /proc/net/arp");
            }
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            loge("Could not open /proc/net/arp to lookup mac address");
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
        return macAddress;
    }

    /* access modifiers changed from: private */
    public boolean isPermanentWrongPasswordFailure(int networkId, int reasonCode) {
        if (reasonCode != 2) {
            return false;
        }
        WifiConfiguration network = this.mWifiConfigManager.getConfiguredNetwork(networkId);
        if (network == null || !network.getNetworkSelectionStatus().getHasEverConnected()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void maybeRegisterNetworkFactory() {
        if (this.mNetworkFactory == null) {
            checkAndSetConnectivityInstance();
            if (this.mCm != null) {
                WifiNetworkFactory wifiNetworkFactory = new WifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE, this.mNetworkCapabilitiesFilter);
                this.mNetworkFactory = wifiNetworkFactory;
                this.mNetworkFactory.setScoreFilter(59);
                this.mNetworkFactory.register();
                UntrustedWifiNetworkFactory untrustedWifiNetworkFactory = new UntrustedWifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE_UNTRUSTED, this.mNetworkCapabilitiesFilter);
                this.mUntrustedNetworkFactory = untrustedWifiNetworkFactory;
                this.mUntrustedNetworkFactory.setScoreFilter(ScoringParams.Values.MAX_EXPID);
                this.mUntrustedNetworkFactory.register();
            }
        }
    }

    /* access modifiers changed from: private */
    public void getAdditionalWifiServiceInterfaces() {
        if (this.mP2pSupported) {
            this.mWifiP2pServiceImpl = IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
            if (this.mWifiP2pServiceImpl != null && this.mWifiP2pChannel == null) {
                this.mWifiP2pChannel = new AsyncChannel();
                this.mWifiP2pChannel.connect(this.mContext, getHandler(), this.mWifiP2pServiceImpl.getP2pStateMachineMessenger());
                this.mWifiRepeater = this.mWifiP2pServiceImpl.getWifiRepeater();
            }
        }
    }

    /* access modifiers changed from: private */
    public void configureRandomizedMacAddress(WifiConfiguration config) {
        if (config == null) {
            Log.e(TAG, "No config to change MAC address to");
            return;
        }
        MacAddress currentMac = MacAddress.fromString(this.mWifiNative.getMacAddress(this.mInterfaceName));
        MacAddress newMac = config.getOrCreateRandomizedMacAddress();
        this.mWifiConfigManager.setNetworkRandomizedMacAddress(config.networkId, newMac);
        if (!WifiConfiguration.isValidMacAddressForRandomization(newMac)) {
            Log.wtf(TAG, "Config generated an invalid MAC address");
        } else if (currentMac.equals(newMac)) {
            Log.d(TAG, "No changes in MAC address");
        } else {
            this.mWifiMetrics.logStaEvent(17, config);
            boolean setMacSuccess = this.mWifiNative.setMacAddress(this.mInterfaceName, newMac);
            Log.d(TAG, "ConnectedMacRandomization SSID(" + config.getPrintableSsid() + "). setMacAddress(" + newMac.toString() + ") from " + currentMac.toString() + " = " + setMacSuccess);
        }
    }

    /* access modifiers changed from: private */
    public void updateConnectedMacRandomizationSetting() {
        boolean macRandomizationEnabled = true;
        if (this.mFacade.getIntegerSetting(this.mContext, "wifi_connected_mac_randomization_enabled", 0) != 1) {
            macRandomizationEnabled = false;
        }
        this.mEnableConnectedMacRandomization.set(macRandomizationEnabled);
        this.mWifiInfo.setEnableConnectedMacRandomization(macRandomizationEnabled);
        this.mWifiMetrics.setIsMacRandomizationOn(macRandomizationEnabled);
        Log.d(TAG, "EnableConnectedMacRandomization Setting changed to " + macRandomizationEnabled);
    }

    public boolean isConnectedMacRandomizationEnabled() {
        return this.mEnableConnectedMacRandomization.get();
    }

    public void failureDetected(int reason) {
        this.mWifiInjector.getSelfRecovery().trigger(2);
    }

    /* access modifiers changed from: package-private */
    public String smToString(Message message) {
        return smToString(message.what);
    }

    /* access modifiers changed from: package-private */
    public String smToString(int what) {
        String s;
        String s2 = sSmToString.get(what);
        if (s2 != null) {
            return s2;
        }
        switch (what) {
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                s = "P2P_CONNECTION_CHANGED";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                s = "WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                s = "P2P.DISCONNECT_WIFI_RESPONSE";
                break;
            case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                s = "P2P.SET_MIRACAST_MODE";
                break;
            case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                s = "P2P.BLOCK_DISCOVERY";
                break;
            default:
                switch (what) {
                    case WifiMonitor.NETWORK_CONNECTION_EVENT:
                        s = "NETWORK_CONNECTION_EVENT";
                        break;
                    case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                        s = "NETWORK_DISCONNECTION_EVENT";
                        break;
                    default:
                        switch (what) {
                            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                                s = "SUPPLICANT_STATE_CHANGE_EVENT";
                                break;
                            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                                s = "AUTHENTICATION_FAILURE_EVENT";
                                break;
                            case WifiMonitor.WPS_SUCCESS_EVENT:
                                s = "WPS_SUCCESS_EVENT";
                                break;
                            case WifiMonitor.WPS_FAIL_EVENT:
                                s = "WPS_FAIL_EVENT";
                                break;
                            default:
                                switch (what) {
                                    case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                                        s = "ASSOCIATION_REJECTION_EVENT";
                                        break;
                                    case WifiMonitor.ANQP_DONE_EVENT:
                                        s = "WifiMonitor.ANQP_DONE_EVENT";
                                        break;
                                    default:
                                        switch (what) {
                                            case WifiMonitor.GAS_QUERY_START_EVENT:
                                                s = "WifiMonitor.GAS_QUERY_START_EVENT";
                                                break;
                                            case WifiMonitor.GAS_QUERY_DONE_EVENT:
                                                s = "WifiMonitor.GAS_QUERY_DONE_EVENT";
                                                break;
                                            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT:
                                                s = "WifiMonitor.RX_HS20_ANQP_ICON_EVENT";
                                                break;
                                            default:
                                                switch (what) {
                                                    case 151562:
                                                        s = "START_WPS";
                                                        break;
                                                    case 151563:
                                                        s = "START_WPS_SUCCEEDED";
                                                        break;
                                                    case 151564:
                                                        s = "WPS_FAILED";
                                                        break;
                                                    case 151565:
                                                        s = "WPS_COMPLETED";
                                                        break;
                                                    case 151566:
                                                        s = "CANCEL_WPS";
                                                        break;
                                                    case 151567:
                                                        s = "CANCEL_WPS_FAILED";
                                                        break;
                                                    case 151568:
                                                        s = "CANCEL_WPS_SUCCEDED";
                                                        break;
                                                    case 151569:
                                                        s = "WifiManager.DISABLE_NETWORK";
                                                        break;
                                                    default:
                                                        switch (what) {
                                                            case 69632:
                                                                s = "AsyncChannel.CMD_CHANNEL_HALF_CONNECTED";
                                                                break;
                                                            case 69636:
                                                                s = "AsyncChannel.CMD_CHANNEL_DISCONNECTED";
                                                                break;
                                                            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                                                                s = "GROUP_CREATING_TIMED_OUT";
                                                                break;
                                                            case WifiMonitor.SUP_REQUEST_IDENTITY:
                                                                s = "SUP_REQUEST_IDENTITY";
                                                                break;
                                                            case WifiMonitor.HS20_REMEDIATION_EVENT:
                                                                s = "WifiMonitor.HS20_REMEDIATION_EVENT";
                                                                break;
                                                            case 151553:
                                                                s = "CONNECT_NETWORK";
                                                                break;
                                                            case 151556:
                                                                s = "FORGET_NETWORK";
                                                                break;
                                                            case 151559:
                                                                s = "SAVE_NETWORK";
                                                                break;
                                                            case 151572:
                                                                s = "RSSI_PKTCNT_FETCH";
                                                                break;
                                                            default:
                                                                s = "what:" + Integer.toString(what);
                                                                break;
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
        return s;
    }

    private void initializeWpsDetails() {
        String detail = this.mPropertyService.get("ro.product.name", "");
        if (!this.mWifiNative.setDeviceName(this.mInterfaceName, detail)) {
            loge("Failed to set device name " + detail);
        }
        String detail2 = this.mPropertyService.get("ro.product.manufacturer", "");
        if (!this.mWifiNative.setManufacturer(this.mInterfaceName, detail2)) {
            loge("Failed to set manufacturer " + detail2);
        }
        String detail3 = this.mPropertyService.get("ro.product.model", "");
        if (!this.mWifiNative.setModelName(this.mInterfaceName, detail3)) {
            loge("Failed to set model name " + detail3);
        }
        String detail4 = this.mPropertyService.get("ro.product.model", "");
        if (!this.mWifiNative.setModelNumber(this.mInterfaceName, detail4)) {
            loge("Failed to set model number " + detail4);
        }
        String detail5 = this.mPropertyService.get("ro.serialno", "");
        if (!this.mWifiNative.setSerialNumber(this.mInterfaceName, detail5)) {
            loge("Failed to set serial number " + detail5);
        }
        if (!this.mWifiNative.setConfigMethods(this.mInterfaceName, "physical_display virtual_push_button")) {
            loge("Failed to set WPS config methods");
        }
        if (!this.mWifiNative.setDeviceType(this.mInterfaceName, this.mPrimaryDeviceType)) {
            loge("Failed to set primary device type " + this.mPrimaryDeviceType);
        }
    }

    /* access modifiers changed from: private */
    public void setupClientMode() {
        Log.d(TAG, "setupClientMode() ifacename = " + this.mInterfaceName);
        this.mWifiStateTracker.updateState(0);
        if (this.mWifiConnectivityManager == null) {
            synchronized (this.mWifiReqCountLock) {
                this.mWifiConnectivityManager = this.mWifiInjector.makeWifiConnectivityManager(this.mWifiInfo, hasConnectionRequests());
                this.mWifiConnectivityManager.setUntrustedConnectionAllowed(this.mUntrustedReqCount > 0);
                this.mWifiConnectivityManager.handleScreenStateChanged(this.mScreenOn);
            }
        }
        this.mIpClient = this.mFacade.makeIpClient(this.mContext, this.mInterfaceName, new IpClientCallback());
        this.mIpClient.setMulticastFilter(true);
        registerForWifiMonitorEvents();
        this.mWifiInjector.getWifiLastResortWatchdog().clearAllFailureCounts();
        setSupplicantLogLevel();
        this.mSupplicantStateTracker.sendMessage(CMD_RESET_SUPPLICANT_STATE);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mWifiInfo.setMacAddress(this.mWifiNative.getMacAddress(this.mInterfaceName));
        if (!this.mWifiConfigManager.migrateFromLegacyStore()) {
            Log.e(TAG, "Failed to migrate from legacy config store");
        }
        sendSupplicantConnectionChangedBroadcast(true);
        this.mWifiNative.setExternalSim(this.mInterfaceName, true);
        setRandomMacOui();
        initializeWpsDetails();
        this.mCountryCode.setReadyForChange(true);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        this.mIsRunning = true;
        updateBatteryWorkSource(null);
        this.mWifiNative.setBluetoothCoexistenceScanMode(this.mInterfaceName, this.mBluetoothConnectionActive);
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        this.mWifiNative.stopFilteringMulticastV4Packets(this.mInterfaceName);
        this.mWifiNative.stopFilteringMulticastV6Packets(this.mInterfaceName);
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get());
        this.mWifiNative.setPowerSave(this.mInterfaceName, true);
        if (this.mP2pSupported) {
            p2pSendMessage(CMD_ENABLE_P2P);
        }
        this.mWifiNative.enableStaAutoReconnect(this.mInterfaceName, false);
        this.mWifiNative.setConcurrencyPriority(true);
        this.mWifiNative.initPrivFeatureCapability();
    }

    /* access modifiers changed from: private */
    public void stopClientMode() {
        this.mWifiDiagnostics.stopLogging();
        if (this.mP2pSupported) {
            p2pSendMessage(CMD_DISABLE_P2P_REQ);
        }
        this.mIsRunning = false;
        updateBatteryWorkSource(null);
        if (this.mIpClient != null) {
            this.mIpClient.shutdown();
            this.mIpClient.awaitShutdown();
        }
        this.mNetworkInfo.setIsAvailable(false);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        this.mCountryCode.setReadyForChange(false);
        this.mInterfaceName = null;
        sendSupplicantConnectionChangedBroadcast(false);
    }

    /* access modifiers changed from: package-private */
    public void registerConnected() {
        if (this.mLastNetworkId != -1) {
            this.mWifiConfigManager.updateNetworkAfterConnect(this.mLastNetworkId);
            this.mWifiScoreReport.reset();
            WifiConfiguration currentNetwork = getCurrentWifiConfiguration();
            if (currentNetwork != null && currentNetwork.isPasspoint()) {
                this.mPasspointManager.onPasspointNetworkConnected(currentNetwork.FQDN);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerDisconnected() {
        if (this.mLastNetworkId != -1) {
            this.mWifiConfigManager.updateNetworkAfterDisconnect(this.mLastNetworkId);
            this.mWifiConfigManager.removeAllEphemeralOrPasspointConfiguredNetworks();
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        if (this.mLastNetworkId == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(this.mLastNetworkId);
    }

    /* access modifiers changed from: package-private */
    public ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String BSSID = this.mWifiInfo.getBSSID();
        if (BSSID == null) {
            BSSID = this.mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache == null) {
            return null;
        }
        return scanDetailCache.getScanResult(BSSID);
    }

    /* access modifiers changed from: package-private */
    public String getCurrentBSSID() {
        return this.mLastBssid;
    }

    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration config) {
        if (this.mNetworkAgent != null) {
            NetworkCapabilities result = new NetworkCapabilities(this.mDfltNetworkCapabilities);
            if (this.mWifiInfo == null || this.mWifiInfo.isEphemeral()) {
                result.removeCapability(14);
            } else {
                result.addCapability(14);
            }
            if (this.mWifiInfo == null || WifiConfiguration.isMetered(config, this.mWifiInfo)) {
                result.removeCapability(11);
            } else {
                result.addCapability(11);
            }
            if (this.mWifiInfo == null || this.mWifiInfo.getRssi() == -127) {
                result.setSignalStrength(Integer.MIN_VALUE);
            } else {
                result.setSignalStrength(this.mWifiInfo.getRssi());
            }
            if (this.mWifiInfo == null || this.mWifiInfo.getSSID().equals("<unknown ssid>")) {
                result.setSSID(null);
            } else {
                result.setSSID(this.mWifiInfo.getSSID());
            }
            this.mNetworkAgent.sendNetworkCapabilities(result);
        }
    }

    /* access modifiers changed from: private */
    public ProxyInfo getProxyProperties() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        return config.getHttpProxy();
    }

    /* access modifiers changed from: private */
    public boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        if (networkId == -1) {
            return false;
        }
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
    }

    /* access modifiers changed from: private */
    public void handleEapAuthFailure(int networkId, int errorCode) {
        WifiConfiguration targetedNetwork = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (targetedNetwork != null) {
            switch (targetedNetwork.enterpriseConfig.getEapMethod()) {
                case 4:
                case 5:
                case 6:
                    if (errorCode == 16385) {
                        getTelephonyManager().resetCarrierKeysForImsiEncryption();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
    }

    /* access modifiers changed from: package-private */
    public void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
    }

    private String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        String prefix;
        String mnc;
        String mcc;
        if (imsi == null || imsi.isEmpty()) {
            return "";
        }
        if (eapMethod == 4) {
            prefix = "1";
        } else if (eapMethod == 5) {
            prefix = "0";
        } else if (eapMethod != 6) {
            return "";
        } else {
            prefix = "6";
        }
        if (mccMnc == null || mccMnc.isEmpty()) {
            String substring = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
            mcc = substring;
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    @VisibleForTesting
    public boolean shouldEvaluateWhetherToSendExplicitlySelected(WifiConfiguration currentConfig) {
        boolean z = false;
        if (currentConfig == null) {
            Log.wtf(TAG, "Current WifiConfiguration is null, but IP provisioning just succeeded");
            return false;
        }
        long currentTimeMillis = this.mClock.getElapsedSinceBootMillis();
        if (this.mWifiConfigManager.getLastSelectedNetwork() == currentConfig.networkId && currentTimeMillis - this.mWifiConfigManager.getLastSelectedTimeStamp() < 30000) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (shouldEvaluateWhetherToSendExplicitlySelected(config)) {
            notifyNetworkUserConnect(true);
            boolean prompt = true;
            if ("com.google.android.projection.gearhead".equals(this.mContext.getPackageManager().getNameForUid(config.lastConnectUid))) {
                log("Network selected by Android Auto, uid is " + config.lastConnectUid);
                prompt = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(config.lastConnectUid);
            }
            if (this.mVerboseLoggingEnabled) {
                log("Network selected by UID " + config.lastConnectUid + " prompt=" + prompt);
            }
            if (prompt) {
                if (this.mVerboseLoggingEnabled) {
                    log("explictlySelected acceptUnvalidated=" + config.noInternetAccessExpected);
                }
                if (this.mNetworkAgent != null) {
                    this.mNetworkAgent.explicitlySelected(config.noInternetAccessExpected);
                    Log.d(TAG, "duplexSelected connectToCellularAndWLAN=" + config.connectToCellularAndWLAN);
                }
            }
        }
        if (!(this.mNetworkAgent == null || config == null)) {
            this.mNetworkAgent.duplexSelected(config.connectToCellularAndWLAN, config.noInternetAccessExpected);
        }
        setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
    }

    public void triggerUpdateAPInfo() {
        Log.d(TAG, "triggerUpdateAPInfo");
    }

    /* access modifiers changed from: private */
    public void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    /* access modifiers changed from: private */
    public void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    public void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.obj = obj;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private Message obtainMessageWithWhatAndArg2(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    /* access modifiers changed from: private */
    public void broadcastWifiCredentialChanged(int wifiCredentialEventType, WifiConfiguration config) {
        if (config != null && config.preSharedKey != null) {
            Intent intent = new Intent("android.net.wifi.WIFI_CREDENTIAL_CHANGED");
            intent.putExtra(WifiBackupRestore.SupplicantBackupMigration.SUPPLICANT_KEY_SSID, config.SSID);
            intent.putExtra("et", wifiCredentialEventType);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.RECEIVE_WIFI_CREDENTIAL_CHANGE");
        }
    }

    private void broadcastWifiDriverChanged(int state) {
        if (this.isBootCompleted) {
            logd("broadcastWifiDriverChanged statte : " + state);
            Intent intent = new Intent(WIFI_DRIVER_CHANGE_ACTION);
            intent.putExtra(WIFI_DRIVER_STATE, state);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_DRIVER_CHANGE_PERMISSION);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleGsmAuthRequest(TelephonyUtil.SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            String response = TelephonyUtil.getGsmSimAuthResponse(requestData.data, getTelephonyManager());
            if (response == null) {
                this.mWifiNative.simAuthFailedResponse(this.mInterfaceName, requestData.networkId);
            } else {
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(this.mInterfaceName, requestData.networkId, WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    /* access modifiers changed from: package-private */
    public void handle3GAuthRequest(TelephonyUtil.SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            TelephonyUtil.SimAuthResponseData response = TelephonyUtil.get3GAuthResponse(requestData, getTelephonyManager());
            if (response != null) {
                this.mWifiNative.simAuthResponse(this.mInterfaceName, requestData.networkId, response.type, response.response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(this.mInterfaceName, requestData.networkId);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    public void startConnectToNetwork(int networkId, int uid, String bssid) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(CONNECT_FROM_USER, false);
        bundle.putString(BSSID_TO_CONNECT, bssid);
        sendMessage(CMD_START_CONNECT, networkId, uid, bundle);
    }

    public void startConnectToUserSelectNetwork(int networkId, int uid, String bssid) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(CONNECT_FROM_USER, true);
        bundle.putString(BSSID_TO_CONNECT, bssid);
        if (this.mNetworkAgent != null || hasConnectionRequests() || this.mNetworkFactory == null || this.mNetworkFactory.hasMessages(536576)) {
            sendMessage(CMD_START_CONNECT, networkId, uid, bundle);
            return;
        }
        Log.w(TAG, "delay connect request");
        sendMessageDelayed(CMD_START_CONNECT, networkId, uid, bundle, 50);
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_START_ROAM, networkId, 0, scanResult);
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        sendMessage(CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER, enabled);
    }

    static boolean unexpectedDisconnectedReason(int reason) {
        return reason == 2 || reason == 6 || reason == 7 || reason == 8 || reason == 9 || reason == 14 || reason == 15 || reason == 16 || reason == 18 || reason == 19 || reason == 23 || reason == 34;
    }

    /* access modifiers changed from: private */
    public boolean disassociatedReason(int reason) {
        return reason == 2 || reason == 4 || reason == 5 || reason == 8 || reason == 34;
    }

    public int getWifiApTypeFromMpLink() {
        return 0;
    }

    public void updateWifiMetrics() {
        this.mWifiMetrics.updateSavedNetworks(this.mWifiConfigManager.getSavedNetworks());
        this.mPasspointManager.updateMetrics();
    }

    /* access modifiers changed from: private */
    public boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
        boolean success = this.mWifiConfigManager.removeNetwork(message.arg1, message.sendingUid);
        if (!success) {
            loge("Failed to remove network");
        } else if (this.mLastConnectConfig != null && this.mLastConnectConfig.networkId == message.arg1) {
            log("delete network, set mLastConnectConfig null");
            setLastConnectConfig(null);
        }
        if (calledFromForget) {
            if (success) {
                replyToMessage(message, 151558);
                broadcastWifiCredentialChanged(1, (WifiConfiguration) message.obj);
                return true;
            }
            replyToMessage(message, 151557, 0);
            return false;
        } else if (success) {
            replyToMessage(message, message.what, 1);
            return true;
        } else {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, message.what, -1);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public NetworkUpdateResult saveNetworkConfigAndSendReply(Message message) {
        WifiConfiguration config = (WifiConfiguration) message.obj;
        if (config == null) {
            loge("SAVE_NETWORK with null configuration " + this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + getCurrentState().getName());
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        }
        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
        if (!result.isSuccess()) {
            loge("SAVE_NETWORK adding/updating config=" + config + " failed");
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, 151560, 0);
            return result;
        } else if (!this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, message.sendingUid)) {
            loge("SAVE_NETWORK enabling config=" + config + " failed");
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        } else {
            broadcastWifiCredentialChanged(0, config);
            replyToMessage(message, 151561);
            return result;
        }
    }

    private static String getLinkPropertiesSummary(LinkProperties lp) {
        List<String> attributes = new ArrayList<>(6);
        if (lp.hasIPv4Address()) {
            attributes.add("v4");
        }
        if (lp.hasIPv4DefaultRoute()) {
            attributes.add("v4r");
        }
        if (lp.hasIPv4DnsServer()) {
            attributes.add("v4dns");
        }
        if (lp.hasGlobalIPv6Address()) {
            attributes.add("v6");
        }
        if (lp.hasIPv6DefaultRoute()) {
            attributes.add("v6r");
        }
        if (lp.hasIPv6DnsServer()) {
            attributes.add("v6dns");
        }
        return TextUtils.join(" ", attributes);
    }

    /* access modifiers changed from: private */
    public String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean p2pSendMessage(int what) {
        if (this.mWifiP2pChannel == null) {
            return false;
        }
        this.mWifiP2pChannel.sendMessage(what);
        return true;
    }

    private boolean p2pSendMessage(int what, int arg1) {
        if (this.mWifiP2pChannel == null) {
            return false;
        }
        this.mWifiP2pChannel.sendMessage(what, arg1);
        return true;
    }

    /* access modifiers changed from: private */
    public boolean hasConnectionRequests() {
        return this.mConnectionReqCount > 0 || this.mUntrustedReqCount > 0;
    }

    public boolean getIpReachabilityDisconnectEnabled() {
        return this.mIpReachabilityDisconnectEnabled;
    }

    public void setIpReachabilityDisconnectEnabled(boolean enabled) {
        this.mIpReachabilityDisconnectEnabled = enabled;
    }

    public boolean syncInitialize(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_INITIALIZE);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    /* access modifiers changed from: package-private */
    public void sendScanResultsAvailableBroadcast(boolean scanSucceeded) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", scanSucceeded);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public boolean disableWifiFilter() {
        return this.mWifiNative.stopRxFilter(this.mInterfaceName);
    }

    public boolean enableWifiFilter() {
        return this.mWifiNative.startRxFilter(this.mInterfaceName);
    }

    public void notifyEnableSameNetworkId(int netId) {
    }

    public boolean reportWifiScoreDelayed() {
        return false;
    }

    public void saveConnectingNetwork(WifiConfiguration config, int netId, boolean autoJoin) {
    }

    public void notifyWifiConnFailedInfo(int netId, String bssid, int rssi, int reason, WifiConnectivityManager wcm) {
    }

    public void notifyNetworkUserConnect(boolean isUserConnect) {
    }

    public void notifyApkChangeWifiStatus(boolean enable, String packageName) {
    }

    public void initialFeatureSet(int featureSet) {
        this.mFeatureSet = featureSet;
    }

    public void saveLastNetIdForAp() {
        if (this.mWifiInfo != null) {
            this.mWifiInfo.setLastNetIdForAp(this.mWifiInfo.getNetworkId());
        }
    }

    public void clearLastNetIdForAp() {
        if (this.mWifiInfo != null) {
            this.mWifiInfo.setLastNetIdForAp(-1);
        }
    }

    /* access modifiers changed from: private */
    public void setLastConnectConfig(WifiConfiguration config) {
        this.mLastConnectConfig = config;
        StringBuilder sb = new StringBuilder();
        sb.append("set mLastConnectConfig, isPortalConnect:");
        sb.append(config != null ? Boolean.valueOf(config.isPortalConnect) : null);
        log(sb.toString());
    }

    public void updateLastPortalConnect(WifiConfiguration config) {
        if (this.mLastConnectConfig != null && config != null && config.networkId == this.mLastConnectConfig.networkId) {
            log("updateLastPortalConnect: isPortalConnect-->" + config.isPortalConnect);
            this.mLastConnectConfig.isPortalConnect = config.isPortalConnect;
        }
    }

    public boolean isPortalConnectLast() {
        if (this.mLastConnectConfig != null) {
            return this.mLastConnectConfig.isPortalConnect;
        }
        return false;
    }
}
