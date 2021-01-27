package com.android.server.wifi;

import android.app.ActivityManager;
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
import android.net.MatchAllNetworkSpecifier;
import android.net.NattKeepalivePacketData;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.SocketKeepalive;
import android.net.StaticIpConfiguration;
import android.net.TcpKeepalivePacketData;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientManager;
import android.net.shared.ProvisioningConfiguration;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkAgentSpecifier;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.IBinder;
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
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.WifiBackupRestore;
import com.android.server.wifi.WifiMulticastLockManager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil;
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

public class ClientModeImpl extends AbsWifiStateMachine implements IHwWifiStateMachineInner {
    private static final long ALLOW_SEND_HILINK_SCAN_RESULTS_BROADCAST_INTERVAL_MS = 3000;
    private static final String[] APP_PACKAGE_WHITE_LIST = {"com.arashivision.insta360akiko", "com.gopro.smarty", "com.google.android.projection.gearhead"};
    static final int BASE = 131072;
    private static final String BSSID_TO_CONNECT = "bssid_to_connect";
    private static final String CHIPSET_BCM = "bcm";
    private static final String CHIPSET_TYPE = SystemProperties.get("ro.connectivity.sub_chiptype", "none");
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF = 131281;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG = 131178;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    public static final int CMD_ASSOCIATE_ASSISTANTE_TIMEOUT = 131899;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    static final int CMD_BOOT_COMPLETED = 131206;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    static final int CMD_DIAGS_CONNECT_TIMEOUT = 131324;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_ENABLE_NETWORK = 131126;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS = 131240;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_CONFIG = 131171;
    static final int CMD_GET_MATCHING_OSU_PROVIDERS = 131181;
    static final int CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS = 131182;
    static final int CMD_GET_PASSPOINT_CONFIGS = 131180;
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS = 131134;
    static final int CMD_GET_SUPPORTED_FEATURES = 131133;
    static final int CMD_GET_SUPPORT_VOWIFI_DETECT = 131774;
    static final int CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES = 131184;
    static final int CMD_INITIALIZE = 131207;
    static final int CMD_INSTALL_PACKET_FILTER = 131274;
    static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    static final int CMD_IP_CONFIGURATION_LOST = 131211;
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    static final int CMD_IP_REACHABILITY_LOST = 131221;
    static final int CMD_MATCH_PROVIDER_NETWORK = 131177;
    static final int CMD_NETWORK_STATUS = 131220;
    private static final int CMD_OBTAINING_IP_TIMEOUT = 131331;
    static final int CMD_ONESHOT_RSSI_POLL = 131156;
    static final int CMD_PNO_PERIODIC_SCAN = 131575;
    private static final int CMD_POST_DHCP_ACTION = 131329;
    @VisibleForTesting
    static final int CMD_PRE_DHCP_ACTION = 131327;
    private static final int CMD_PRE_DHCP_ACTION_COMPLETE = 131328;
    static final int CMD_QUERY_OSU_ICON = 131176;
    static final int CMD_READ_PACKET_FILTER = 131280;
    static final int CMD_REASSOCIATE = 131147;
    static final int CMD_RECONNECT = 131146;
    static final int CMD_REMOVE_APP_CONFIGURATIONS = 131169;
    static final int CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF = 131282;
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
    private static final int CMD_SET_SCREEN_STATE_PARAM = 126;
    static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    static final int CMD_START_CONNECT = 131215;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_ROAM = 131217;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    private static final int CMD_START_SUBSCRIPTION_PROVISIONING = 131326;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    static final int CMD_TARGET_BSSID = 131213;
    static final int CMD_TEST_NETWORK_DISCONNECT = 131161;
    private static final int CMD_TRY_CACHED_IP = 131330;
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
    private static boolean DBG = false;
    private static final int DEFAULT_MTU = 1500;
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    @VisibleForTesting
    public static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60000;
    public static final int DISABLED_MODE = 4;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final int DRIVER_STARTED = 1;
    private static final int DRIVER_STOPPED = 2;
    private static final boolean ENABLE_DHCP_AFTER_ROAM = SystemProperties.getBoolean("ro.config.roam_force_dhcp", false);
    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final String EXTRA_OSU_PROVIDER = "OsuProvider";
    private static final String EXTRA_PACKAGE_NAME = "PackageName";
    private static final String EXTRA_PASSPOINT_CONFIGURATION = "PasspointConfiguration";
    private static final String EXTRA_UID = "uid";
    private static final String FACTORY_MODE = "factory";
    private static final int FAILURE = -1;
    public static final int GOOD_LINK_DETECTED = 131874;
    private static final String GOOGLE_OUI = "DA-A1-19";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
    private static final int IMSI_RECONNECT_LIMIT = 3;
    public static final int INVALID_LINK_DETECTED = 131875;
    private static final int IPCLIENT_TIMEOUT_MS = 10000;
    private static final String KEY_IS_RANDOM_MAC = "isRandom";
    private static final String KEY_IS_RANDOM_SELF_CURE = "isRandomSelfCure";
    private static final long LAST_AUTH_FAILURE_GAP = 100;
    @VisibleForTesting
    public static final int LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS = 30000;
    private static final int LINK_FLAPPING_DEBOUNCE_MSEC = 4000;
    private static final String LOGD_LEVEL_DEBUG = "D";
    private static final String LOGD_LEVEL_VERBOSE = "V";
    public static final int MAYBE_POOR_LINK = 131876;
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
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    private static final int NET_ID_NONE = -1;
    private static final String NORMAL_MODE = "normal";
    @VisibleForTesting
    public static final short NUM_LOG_RECS_NORMAL = 100;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE = 3000;
    @VisibleForTesting
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = 200;
    private static final int OBTAINING_IP_TIMEOUT_VALUE = 30000;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static boolean PDBG = false;
    private static final String PHYSICAL_DISPLAY = "physical_display virtual_push_button";
    public static final int POOR_LINK_DETECTED = 131873;
    private static final int PORTAL_NOTIFICATION_NOT_SHOWN = 0;
    private static final String PRODUCT_MANUFACTURER = "ro.product.manufacturer";
    private static final String PRODUCT_MODEL = "ro.product.model";
    private static final String PRODUCT_NAME = "ro.product.name";
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    private static final String RO_RUN_MODE = "ro.runmode";
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
    protected static final int SCORE_QUALITY_FILTER = 60;
    private static final char SCREEN_OFF = 'N';
    private static final char SCREEN_ON = 'Y';
    private static final String SERIAL_NUMBER = "ro.serialno";
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_BSSID_ANY = "any";
    private static final int SUPPLICANT_RESTART_INTERVAL_MSECS = 5000;
    private static final int SUPPLICANT_RESTART_TRIES = 5;
    private static final String SUPPORT_WIFI_COEX_ETH = "persist.sys.coexistence";
    private static final int SUSPEND_DUE_TO_DHCP = 1;
    private static final int SUSPEND_DUE_TO_HIGH_PERF = 2;
    private static final int SUSPEND_DUE_TO_SCREEN = 4;
    private static final String SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL = "log.tag.WifiHAL";
    private static final String TAG = "WifiClientModeImpl";
    private static final String TYPE_GET_CACHE_DHCP_RESULT = "getCachedDhcpResultsForCurrentConfig";
    private static boolean USE_PAUSE_SCANS = false;
    private static boolean VDBG = false;
    private static boolean VVDBG = false;
    public static final int WIFIPRO_SOFT_CONNECT_TIMEOUT = 131897;
    private static final String WIFI_DRIVER_CHANGE_ACTION = "huawei.intent.action.WIFI_DRIVER_CHANGE";
    private static final String WIFI_DRIVER_CHANGE_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final String WIFI_DRIVER_STATE = "wifi_driver_state";
    private static final int WIFI_SCORE_BEST = 99;
    private static final int WIFI_SCORE_GOOD = 60;
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static final int WPS_PIN_RETRY_INTERVAL_MSECS = 50000;
    private static boolean flagIpv4Provisioned = false;
    private static boolean mLogMessages;
    private static final SparseArray<String> sGetWhatToString = MessageUtils.findMessageNames(sMessageClasses);
    private static final Class[] sMessageClasses = {AsyncChannel.class, ClientModeImpl.class};
    private static int sScanAlarmIntentCount = 0;
    private DataProvider dataProvider;
    private boolean isBootCompleted = false;
    private boolean isRandomSelfCure = false;
    private boolean isUseRandomMac = false;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    private boolean mBluetoothConnectionActive = false;
    private final BuildProperties mBuildProperties;
    private ClientModeManager.Listener mClientModeCallback = null;
    private final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    private boolean mConnectedMacRandomzationSupported;
    private State mConnectedState = new ConnectedState();
    private long mConnectingStartTimestamp = 0;
    private Context mContext;
    private final WifiCountryCode mCountryCode;
    HwCustWifiStateMachineReference mCust = ((HwCustWifiStateMachineReference) HwCustUtils.createObj(HwCustWifiStateMachineReference.class, new Object[0]));
    private State mDefaultState = new DefaultState();
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private boolean mDidBlackListBSSID = false;
    private State mDisconnectedState = new DisconnectedState();
    private State mDisconnectingState = new DisconnectingState();
    int mDisconnectingWatchdogCount = 0;
    private boolean mEnableRssiPolling = false;
    private FrameworkFacade mFacade;
    private long mFeatureSet = 0;
    private HwMSSHandlerManager mHwMssHandler;
    private HwWifiCHRService mHwWifiCHRService;
    public final IHwWifiStateMachineEx mHwWifiStateMachineEx;
    private String mInterfaceName;
    private volatile IpClientManager mIpClient;
    private IpClientCallbacksImpl mIpClientCallbacks;
    private boolean mIpReachabilityDisconnectEnabled = true;
    private boolean mIsAutoRoaming = false;
    private boolean mIsFirstConnect = false;
    private boolean mIsImsiAvailable = true;
    public boolean mIsRandomMacCleared = false;
    private boolean mIsRealReboot = false;
    private boolean mIsRunning = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private long mLastAllowSendHiLinkScanResultsBroadcastTime = 0;
    private long mLastAuthFailureTimestamp = Long.MIN_VALUE;
    private String mLastBssid;
    private long mLastConnectAttemptTimestamp = 0;
    private volatile WifiConfiguration mLastConnectConfig = null;
    private long mLastDriverRoamAttempt = 0;
    private Pair<String, String> mLastL2KeyAndGroupHint = null;
    private WifiLinkLayerStats mLastLinkLayerStats;
    private long mLastLinkLayerStatsUpdate = 0;
    private int mLastNetworkId;
    private long mLastOntimeReportTimeStamp = 0;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    private long mLastScreenStateChangeTimeStamp = 0;
    private int mLastSignalLevel = -1;
    private final LinkProbeManager mLinkProbeManager;
    private LinkProperties mLinkProperties;
    private final McastLockManagerFilterController mMcastLockManagerFilterController;
    private int mMessageHandlingStatus = 0;
    private boolean mModeChange = false;
    @GuardedBy({"mNetworkAgentLock"})
    private WifiNetworkAgent mNetworkAgent;
    private final Object mNetworkAgentLock = new Object();
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private WifiNetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc = new NetworkMisc();
    private AtomicInteger mNullMessageCounter = new AtomicInteger(0);
    private INetworkManagementService mNwService;
    private State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    private int mOperationalMode = 4;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    private final PasspointManager mPasspointManager;
    private int mPeriodicScanToken = 0;
    private volatile int mPollRssiIntervalMsecs = DEFAULT_POLL_RSSI_INTERVAL_MSECS;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    private int mRoamFailCount = 0;
    int mRoamWatchdogCount = 0;
    private State mRoamingState = new RoamingState();
    private int mRssiPollToken = 0;
    private byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    private final SarManager mSarManager;
    private ScanRequestProxy mScanRequestProxy;
    private boolean mScreenOn = false;
    private long mSupplicantScanIntervalMs;
    private SupplicantStateTracker mSupplicantStateTracker;
    private int mSuspendOptNeedsDisabled = 0;
    private PowerManager.WakeLock mSuspendWakeLock;
    private int mTargetNetworkId = -1;
    private String mTargetRoamBSSID = "any";
    private WifiConfiguration mTargetWifiConfiguration = null;
    private final String mTcpBufferSizes;
    private TelephonyManager mTelephonyManager;
    private boolean mTemporarilyDisconnectWifi = false;
    private int mTrackEapAuthFailCount = 0;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    private boolean mVerboseLoggingEnabled = false;
    private PowerManager.WakeLock mWakeLock;
    private final WifiConfigManager mWifiConfigManager;
    protected WifiConnectivityManager mWifiConnectivityManager;
    private final WifiDataStall mWifiDataStall;
    private BaseWifiDiagnostics mWifiDiagnostics;
    private final ExtendedWifiInfo mWifiInfo;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;
    private final WifiMonitor mWifiMonitor;
    private final WifiNative mWifiNative;
    private WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private AsyncChannel mWifiP2pChannel;
    protected WifiP2pServiceImpl mWifiP2pServiceImpl;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private WifiRepeater mWifiRepeater;
    private final WifiScoreCard mWifiScoreCard;
    private final WifiScoreReport mWifiScoreReport;
    private WifiSettingsStore mWifiSettingStore;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    private WifiStateTracker mWifiStateTracker;
    private final WifiTrafficPoller mWifiTrafficPoller;
    private State mWpsRunningState = new WpsRunningState();
    private final WrongPasswordNotifier mWrongPasswordNotifier;

    static /* synthetic */ int access$10208(ClientModeImpl x0) {
        int i = x0.mRssiPollToken;
        x0.mRssiPollToken = i + 1;
        return i;
    }

    static /* synthetic */ int access$13908(ClientModeImpl x0) {
        int i = x0.mRoamFailCount;
        x0.mRoamFailCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$6908(ClientModeImpl x0) {
        int i = x0.mTrackEapAuthFailCount;
        x0.mTrackEapAuthFailCount = i + 1;
        return i;
    }

    static {
        boolean z = HWFLOW;
        DBG = z;
        mLogMessages = z;
        PDBG = z;
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
        Log.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.i(getName(), s);
    }

    public WifiScoreReport getWifiScoreReport() {
        return this.mWifiScoreReport;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processRssiThreshold(byte curRssi, int reason, WifiNative.WifiRssiEventHandler rssiHandler) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + ((int) curRssi));
            return;
        }
        int i = 0;
        while (true) {
            byte[] bArr = this.mRssiRanges;
            if (i >= bArr.length) {
                return;
            }
            if (curRssi < bArr[i]) {
                byte maxRssi = bArr[i];
                byte minRssi = bArr[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities();
                int ret = startRssiMonitoringOffload(maxRssi, minRssi, rssiHandler);
                Log.i(TAG, "Re-program RSSI thresholds for " + getWhatToString(reason) + ": [" + ((int) minRssi) + ", " + ((int) maxRssi) + "], curRssi=" + ((int) curRssi) + " ret=" + ret);
                return;
            }
            i++;
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
            Log.i(TAG, "force BSSID to " + StringUtilEx.safeDisplayBssid(bssid) + "due to config");
        }
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearTargetBssid " + StringUtilEx.safeDisplayBssid(bssid) + " ssid =" + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
        }
        this.mTargetRoamBSSID = bssid;
        return this.mWifiNative.setConfiguredNetworkBSSID(this.mInterfaceName, "any");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        String str;
        if (config == null || bssid == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            Log.i(TAG, "force BSSID to " + StringUtilEx.safeDisplayBssid(bssid) + "due to config");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("setTargetBssid set to ");
        sb.append(StringUtilEx.safeDisplayBssid(bssid));
        sb.append(" key=");
        sb.append(StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
        sb.append(" authType = ");
        if (config.SSID == null) {
            str = config.getSsidAndSecurityTypeString();
        } else {
            str = config.getSsidAndSecurityTypeString().substring(config.SSID.length());
        }
        sb.append(str);
        Log.i(TAG, sb.toString());
        this.mTargetRoamBSSID = bssid;
        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
    }

    public ClientModeImpl(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager) {
        super(TAG, looper);
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mWifiScoreCard = wifiInjector.getWifiScoreCard();
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiNative = wifiNative;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mWrongPasswordNotifier = wrongPasswordNotifier;
        this.mSarManager = sarManager;
        this.mWifiTrafficPoller = wifiTrafficPoller;
        this.mLinkProbeManager = linkProbeManager;
        this.mHwWifiStateMachineEx = HwWifiServiceFactory.getHwWifiStateMachineEx(this);
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
        this.mWifiDataStall = this.mWifiInjector.getWifiDataStall();
        this.mWifiInfo = new ExtendedWifiInfo();
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mWifiConnectivityManager = this.mWifiInjector.makeWifiConnectivityManager(this);
        this.mLinkProperties = new LinkProperties();
        this.mMcastLockManagerFilterController = new McastLockManagerFilterController();
        this.dataProvider = DataProvider.getInstance();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039887);
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
        this.mNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        this.mNetworkFactory = this.mWifiInjector.makeWifiNetworkFactory(this.mNetworkCapabilitiesFilter, this.mWifiConnectivityManager);
        this.mUntrustedNetworkFactory = this.mWifiInjector.makeUntrustedWifiNetworkFactory(this.mNetworkCapabilitiesFilter, this.mWifiConnectivityManager);
        this.mWifiNetworkSuggestionsManager = this.mWifiInjector.getWifiNetworkSuggestionsManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ClientModeImpl.VDBG) {
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.log("receive action: " + action);
                }
                if (action != null && action.equals("android.intent.action.SCREEN_ON")) {
                    ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_SCREEN_STATE_CHANGED, 1);
                } else if (action != null && action.equals("android.intent.action.SCREEN_OFF")) {
                    ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_SCREEN_STATE_CHANGED, 0);
                }
            }
        }, filter);
        this.mFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                AtomicBoolean atomicBoolean = ClientModeImpl.this.mUserWantsSuspendOpt;
                boolean z = true;
                if (ClientModeImpl.this.mFacade.getIntegerSetting(ClientModeImpl.this.mContext, "wifi_suspend_optimizations_enabled", 1) != 1) {
                    z = false;
                }
                atomicBoolean.set(z);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(getHandler()) {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.ClientModeImpl.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                boolean z = false;
                ClientModeImpl.this.mIsRunning = false;
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                if (getSendingUserId() == -1) {
                    z = true;
                }
                clientModeImpl.mIsRealReboot = z;
                Log.i(ClientModeImpl.TAG, "onReceive: mIsRealReboot = " + ClientModeImpl.this.mIsRealReboot);
                if (ClientModeImpl.DBG) {
                    ClientModeImpl.this.log("shut down so update battery");
                }
                ClientModeImpl.this.updateBatteryWorkSource(null);
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        this.mUserWantsSuspendOpt.set(this.mFacade.getIntegerSetting(this.mContext, "wifi_suspend_optimizations_enabled", 1) == 1);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        boolean z = true;
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mConnectedMacRandomzationSupported = (!this.mContext.getResources().getBoolean(17891578) || !WifiConfiguration.IS_WIFI_RANDOM_MAC_EN) ? false : z;
        this.mWifiInfo.setEnableConnectedMacRandomization(this.mConnectedMacRandomzationSupported);
        this.mWifiMetrics.setIsMacRandomizationOn(this.mConnectedMacRandomzationSupported);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039891);
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
        if (ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) {
        }
        setLogRecSize(100);
        setLogOnlyTransitions(false);
    }

    public void start() {
        super.start();
        handleScreenStateChanged(((PowerManager) this.mContext.getSystemService("power")).isInteractive());
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        HwWifiServiceFactory.getHwWifiDevicePolicy().registerBroadcasts(this.mContext);
    }

    private void registerForWifiMonitorEvents() {
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, 147499, getHandler());
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
        this.mWifiMonitor.registerHandler(this.mInterfaceName, 147499, this.mWifiMetrics.getHandler());
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
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPA3_CONNECT_FAIL_EVENT, getHandler());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMulticastFilter(boolean enabled) {
        if (this.mIpClient != null) {
            this.mIpClient.setMulticastFilter(enabled);
        }
    }

    /* access modifiers changed from: package-private */
    public class McastLockManagerFilterController implements WifiMulticastLockManager.FilterController {
        McastLockManagerFilterController() {
        }

        @Override // com.android.server.wifi.WifiMulticastLockManager.FilterController
        public void startFilteringMulticastPackets() {
            ClientModeImpl.this.setMulticastFilter(true);
        }

        @Override // com.android.server.wifi.WifiMulticastLockManager.FilterController
        public void stopFilteringMulticastPackets() {
            ClientModeImpl.this.setMulticastFilter(false);
        }
    }

    /* access modifiers changed from: package-private */
    public class IpClientCallbacksImpl extends IpClientCallbacks {
        private final ConditionVariable mWaitForCreationCv = new ConditionVariable(false);
        private final ConditionVariable mWaitForStopCv = new ConditionVariable(false);

        IpClientCallbacksImpl() {
        }

        public void onIpClientCreated(IIpClient ipClient) {
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.mIpClient = new IpClientManager(ipClient, clientModeImpl.getName());
            this.mWaitForCreationCv.open();
        }

        public void onPreDhcpAction() {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_PRE_DHCP_ACTION);
        }

        public void onPostDhcpAction() {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_POST_DHCP_ACTION);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults == null) {
                if (ClientModeImpl.this.mHwWifiCHRService != null) {
                    ClientModeImpl.this.mHwWifiCHRService.updateDhcpState(4);
                }
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IPV4_PROVISIONING_FAILURE);
            } else if ("CMD_TRY_CACHED_IP".equals(dhcpResults.domains)) {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_TRY_CACHED_IP);
            } else {
                ClientModeImpl.this.uploadDhcpState(dhcpResults);
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
            }
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            ClientModeImpl.this.mWifiMetrics.logStaEvent(7);
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_UPDATE_LINKPROPERTIES, newLp);
            if (newLp == null || !newLp.hasIpv4Address()) {
                boolean unused = ClientModeImpl.flagIpv4Provisioned = false;
                return;
            }
            boolean unused2 = ClientModeImpl.flagIpv4Provisioned = true;
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            ClientModeImpl.this.mWifiMetrics.logStaEvent(8);
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_UPDATE_LINKPROPERTIES, newLp);
            if (newLp != null && newLp.hasIpv4Address() && !ClientModeImpl.flagIpv4Provisioned) {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL);
            }
        }

        public void onReachabilityLost(String logMsg) {
            ClientModeImpl.this.mWifiMetrics.logStaEvent(9);
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_REACHABILITY_LOST, logMsg);
        }

        public void installPacketFilter(byte[] filter) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_INSTALL_PACKET_FILTER, filter);
        }

        public void startReadPacketFilter() {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_READ_PACKET_FILTER);
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_CONFIG_ND_OFFLOAD, enabled ? 1 : 0);
        }

        public void doArpDetection(final int type, final String uniqueStr, final DhcpResults dhcpResults) {
            new Thread(new Runnable() {
                /* class com.android.server.wifi.ClientModeImpl.IpClientCallbacksImpl.AnonymousClass1 */
                final String arpStr = uniqueStr;

                @Override // java.lang.Runnable
                public void run() {
                    boolean isArpSuccess = ClientModeImpl.this.doArpTest(type, (Inet4Address) dhcpResults.ipAddress.getAddress());
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.reportArpResult(type, this.arpStr, isArpSuccess);
                    }
                }
            }).start();
        }

        public void onQuit() {
            this.mWaitForStopCv.open();
        }

        /* access modifiers changed from: package-private */
        public boolean awaitCreation() {
            return this.mWaitForCreationCv.block(RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
        }

        /* access modifiers changed from: package-private */
        public boolean awaitShutdown() {
            return this.mWaitForStopCv.block(RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopIpClient() {
        handlePostDhcpSetup();
        if (this.mIpClient != null) {
            this.mIpClient.stop();
        }
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
        this.mNetworkFactory.enableVerboseLogging(verbose);
        this.mLinkProbeManager.enableVerboseLogging(this.mVerboseLoggingEnabled);
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x002a: APUT  
      (r2v1 'ouiBytes' byte[] A[D('ouiBytes' byte[]), IMMUTABLE_TYPE])
      (0 ??[int, short, byte, char])
      (wrap: byte : 0x0029: CAST (r4v4 byte A[IMMUTABLE_TYPE]) = (byte) (wrap: int : 0x0027: ARITH  (r4v3 int) = (wrap: int : 0x0023: INVOKE  (r4v2 int) = 
      (wrap: java.lang.String : 0x001f: AGET  (r4v1 java.lang.String) = (r1v3 'ouiParts' java.lang.String[] A[D('ouiParts' java.lang.String[])]), (0 ??[int, short, byte, char]))
      (16 int)
     type: STATIC call: java.lang.Integer.parseInt(java.lang.String, int):int) & (wrap: ?? : ?: SGET   com.android.server.wifi.hotspot2.anqp.Constants.BYTE_MASK int)))
     */
    public boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039888);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = new byte[3];
        try {
            ouiBytes[0] = (byte) (Integer.parseInt(ouiParts[0], 16) & Constants.BYTE_MASK);
            ouiBytes[1] = (byte) (Integer.parseInt(ouiParts[1], 16) & Constants.BYTE_MASK);
            ouiBytes[2] = (byte) (Integer.parseInt(ouiParts[2], 16) & Constants.BYTE_MASK);
            logd("Setting OUI to " + oui);
            return this.mWifiNative.setScanningMacOui(this.mInterfaceName, ouiBytes);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Exception happened in setRandomMacOui()");
            return false;
        }
    }

    public boolean clearRandomMacOui() {
        logd("Clear random OUI");
        return this.mWifiNative.setScanningMacOui(this.mInterfaceName, new byte[]{0, 0, 0});
    }

    public void gameKOGAdjustSpeed(int mode) {
        this.mWifiNative.mHwWifiNativeEx.gameKOGAdjustSpeed(this.mWifiInfo.getFrequency(), mode);
    }

    public int setCmdToWifiChip(String iface, int mode, int type, int action, int param) {
        return this.mWifiNative.mHwWifiNativeEx.setCmdToWifiChip(iface, mode, type, action, param);
    }

    public void onMssSyncResultEvent(NativeMssResult mssstru) {
        if (checkAndGetHwMSSHandlerManager() != null) {
            this.mHwMssHandler.onMssDrvEvent(mssstru);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean connectToUserSelectNetwork(int netId, int uid, boolean forceReconnect) {
        logd("connectToUserSelectNetwork netId " + netId + ", uid " + uid + ", forceReconnect = " + forceReconnect);
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (config == null) {
            loge("connectToUserSelectNetwork Invalid network Id=" + netId);
            return false;
        }
        if (HuaweiTelephonyConfigs.isChinaMobile()) {
            this.mWifiConfigManager.mHwWifiConfigManagerEx.updatePriority(config, uid);
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
            if (uid == 1000) {
                this.mWifiMetrics.setNominatorForNetwork(config.networkId, 1);
            }
            if (!config.hiddenSSID || !TextUtils.isEmpty(config.oriSsid)) {
                startConnectToUserSelectNetwork(netId, uid, selectBestCandidate(config));
            } else {
                Log.i(TAG, "INTERCEPT1 the connect request since hidden and null oriSsid.");
                this.mScanRequestProxy.mHwScanRequestProxyEx.startScanForHiddenNetwork(uid, config);
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

    private boolean checkOrDeferScanAllowed(Message msg) {
        long now = this.mClock.getWallClockMillis();
        long j = this.mLastConnectAttemptTimestamp;
        if (j == 0 || now - j >= RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS) {
            return true;
        }
        if (now - j < 0) {
            logd("checkOrDeferScanAllowed time is jump!!!");
            this.mLastConnectAttemptTimestamp = now;
        }
        sendMessageDelayed(Message.obtain(msg), 11000 - (now - this.mLastConnectAttemptTimestamp));
        return false;
    }

    /* access modifiers changed from: package-private */
    public String reportOnTime() {
        long now = this.mClock.getWallClockMillis();
        StringBuilder sb = new StringBuilder();
        int i = this.mOnTime;
        int on = i - this.mOnTimeLastReport;
        this.mOnTimeLastReport = i;
        int i2 = this.mTxTime;
        int tx = i2 - this.mTxTimeLastReport;
        this.mTxTimeLastReport = i2;
        int i3 = this.mRxTime;
        int rx = i3 - this.mRxTimeLastReport;
        this.mRxTimeLastReport = i3;
        int period = (int) (now - this.mLastOntimeReportTimeStamp);
        this.mLastOntimeReportTimeStamp = now;
        sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", Integer.valueOf(on), Integer.valueOf(tx), Integer.valueOf(rx), Integer.valueOf(period)));
        sb.append(String.format(" from screen [on:%d period:%d]", Integer.valueOf(this.mOnTime - this.mOnTimeScreenStateChange), Integer.valueOf((int) (now - this.mLastScreenStateChangeTimeStamp))));
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public WifiLinkLayerStats getWifiLinkLayerStats() {
        if (this.mInterfaceName == null) {
            loge("getWifiLinkLayerStats called without an interface");
            return null;
        }
        this.mLastLinkLayerStatsUpdate = this.mClock.getWallClockMillis();
        WifiLinkLayerStats stats = this.mWifiNative.getWifiLinkLayerStats(this.mInterfaceName);
        if (stats != null) {
            this.mOnTime = stats.on_time;
            this.mTxTime = stats.tx_time;
            this.mRxTime = stats.rx_time;
            this.mRunningBeaconCount = stats.beacon_rx;
            this.mWifiInfo.updatePacketRates(stats, this.mLastLinkLayerStatsUpdate);
        } else {
            this.mWifiInfo.updatePacketRates(this.mFacade.getTxPackets(this.mInterfaceName), this.mFacade.getRxPackets(this.mInterfaceName), this.mLastLinkLayerStatsUpdate);
        }
        return stats;
    }

    private byte[] getDstMacForKeepalive(KeepalivePacketData packetData) throws SocketKeepalive.InvalidPacketException {
        try {
            return NativeUtil.macAddressToByteArray(macAddressFromRoute(RouteInfo.selectBestRoute(this.mLinkProperties.getRoutes(), packetData.dstAddress).getGateway().getHostAddress()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new SocketKeepalive.InvalidPacketException(-21);
        }
    }

    private static int getEtherProtoForKeepalive(KeepalivePacketData packetData) throws SocketKeepalive.InvalidPacketException {
        if (packetData.dstAddress instanceof Inet4Address) {
            return OsConstants.ETH_P_IP;
        }
        if (packetData.dstAddress instanceof Inet6Address) {
            return OsConstants.ETH_P_IPV6;
        }
        throw new SocketKeepalive.InvalidPacketException(-21);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        SocketKeepalive.InvalidPacketException e;
        if (packetData == null) {
            loge("startWifiIPPacketOffload, packetData is null");
            return -21;
        }
        try {
            byte[] packet = packetData.getPacket();
            try {
            } catch (SocketKeepalive.InvalidPacketException e2) {
                e = e2;
                return e.error;
            }
            try {
                int ret = this.mWifiNative.startSendingOffloadedPacket(this.mInterfaceName, slot, getDstMacForKeepalive(packetData), packet, getEtherProtoForKeepalive(packetData), intervalSeconds * 1000);
                if (ret == 0) {
                    return 0;
                }
                loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds + "): hardware error " + ret);
                return -31;
            } catch (SocketKeepalive.InvalidPacketException e3) {
                e = e3;
                return e.error;
            }
        } catch (SocketKeepalive.InvalidPacketException e4) {
            e = e4;
            return e.error;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int stopWifiIPPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(this.mInterfaceName, slot);
        if (ret == 0) {
            return 0;
        }
        loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
        return -31;
    }

    private int startRssiMonitoringOffload(byte maxRssi, byte minRssi, WifiNative.WifiRssiEventHandler rssiHandler) {
        return this.mWifiNative.startRssiMonitoring(this.mInterfaceName, maxRssi, minRssi, rssiHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring(this.mInterfaceName);
    }

    public void setWifiStateForApiCalls(int newState) {
        if (newState == 0 || newState == 1 || newState == 2 || newState == 3 || newState == 4) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "setting wifi state to: " + newState);
            }
            this.mWifiState.set(newState);
            log("setWifiState: " + syncGetWifiStateByName());
            return;
        }
        Log.i(TAG, "attempted to set an invalid state: " + newState);
    }

    public int syncGetWifiState() {
        return this.mWifiState.get();
    }

    public String syncGetWifiStateByName() {
        int i = this.mWifiState.get();
        if (i == 0) {
            return "disabling";
        }
        if (i == 1) {
            return "disabled";
        }
        if (i == 2) {
            return "enabling";
        }
        if (i == 3) {
            return "enabled";
        }
        if (i != 4) {
            return "[invalid state]";
        }
        return "unknown state";
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
            if (!this.mVerboseLoggingEnabled) {
                return true;
            }
            Log.i(TAG, "Supplicant is under transient state: " + supplicantState);
            return true;
        } else if (!this.mVerboseLoggingEnabled) {
            return false;
        } else {
            Log.i(TAG, "Supplicant is under steady state: " + supplicantState);
            return false;
        }
    }

    public WifiInfo syncRequestConnectionInfo(String callingPackage, int uid) {
        if (!isWifiSelfCuring() && !isWiFiProSwitchOnGoing()) {
            return new WifiInfo(this.mWifiInfo);
        }
        Log.i(TAG, "syncRequestConnectionInfo isWifiSelfCuring = " + isWifiSelfCuring() + ", isWiFiProSwitchOnGoing = " + isWiFiProSwitchOnGoing());
        return this.mHwWifiStateMachineEx.hwSyncRequestConnectionInfo(this.mWifiInfo);
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
        if (hasMessages(WifiMonitor.NETWORK_CONNECTION_EVENT)) {
            removeMessages(WifiMonitor.NETWORK_CONNECTION_EVENT);
        }
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
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_QUERY_OSU_ICON, bundle);
        if (messageIsNull(resultMsg)) {
            return false;
        }
        int result = resultMsg.arg1;
        resultMsg.recycle();
        if (result == 1) {
            return true;
        }
        return false;
    }

    public int matchProviderWithCurrentNetwork(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_MATCH_PROVIDER_NETWORK, fqdn);
        if (messageIsNull(resultMsg)) {
            return -1;
        }
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public void deauthenticateNetwork(AsyncChannel channel, long holdoff, boolean ess) {
    }

    public void disableEphemeralNetwork(String ssid) {
        if (ssid != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, ssid);
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

    private boolean messageIsNull(Message resultMsg) {
        if (resultMsg != null) {
            return false;
        }
        if (this.mNullMessageCounter.getAndIncrement() <= 0) {
            return true;
        }
        Log.wtf(TAG, "Persistent null Message", new RuntimeException());
        return true;
    }

    public int syncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration config) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_ADD_OR_UPDATE_NETWORK, config);
        if (messageIsNull(resultMsg)) {
            return -1;
        }
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel, int targetUid) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_CONFIGURED_NETWORKS, uuid, targetUid);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /* access modifiers changed from: package-private */
    public Map<String, Map<Integer, List<ScanResult>>> syncGetAllMatchingFqdnsForScanResults(List<ScanResult> scanResults, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS, scanResults);
        if (messageIsNull(resultMsg)) {
            return new HashMap();
        }
        Map<String, Map<Integer, List<ScanResult>>> configs = (Map) resultMsg.obj;
        resultMsg.recycle();
        return configs;
    }

    public Map<OsuProvider, List<ScanResult>> syncGetMatchingOsuProviders(List<ScanResult> scanResults, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_MATCHING_OSU_PROVIDERS, scanResults);
        if (messageIsNull(resultMsg)) {
            return new HashMap();
        }
        Map<OsuProvider, List<ScanResult>> providers = (Map) resultMsg.obj;
        resultMsg.recycle();
        return providers;
    }

    public Map<OsuProvider, PasspointConfiguration> syncGetMatchingPasspointConfigsForOsuProviders(List<OsuProvider> osuProviders, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS, osuProviders);
        if (messageIsNull(resultMsg)) {
            return new HashMap();
        }
        Map<OsuProvider, PasspointConfiguration> result = (Map) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetWifiConfigsForPasspointProfiles(List<String> fqdnList, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES, fqdnList);
        if (messageIsNull(resultMsg)) {
            return new ArrayList();
        }
        List<WifiConfiguration> result = (List) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel, PasspointConfiguration config, int uid, String packageName) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        bundle.putString(EXTRA_PACKAGE_NAME, packageName);
        bundle.putParcelable(EXTRA_PASSPOINT_CONFIGURATION, config);
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG, bundle);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 == 1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemovePasspointConfig(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_REMOVE_PASSPOINT_CONFIG, fqdn);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 == 1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public List<PasspointConfiguration> syncGetPasspointConfigs(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_PASSPOINT_CONFIGS);
        if (messageIsNull(resultMsg)) {
            return null;
        }
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
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != 0) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public long syncGetSupportedFeatures(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_SUPPORTED_FEATURES);
        if (messageIsNull(resultMsg)) {
            return 0;
        }
        long supportedFeatureSet = ((Long) resultMsg.obj).longValue();
        resultMsg.recycle();
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
            return supportedFeatureSet & -385;
        }
        return supportedFeatureSet;
    }

    public WifiLinkLayerStats syncGetLinkLayerStats(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_GET_LINK_LAYER_STATS);
        if (messageIsNull(resultMsg)) {
            return null;
        }
        WifiLinkLayerStats result = (WifiLinkLayerStats) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemoveNetwork(AsyncChannel channel, int networkId) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_REMOVE_NETWORK, networkId);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != -1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncEnableNetwork(AsyncChannel channel, int netId, boolean disableOthers) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_ENABLE_NETWORK, netId, disableOthers ? 1 : 0);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != -1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean syncDisableNetwork(AsyncChannel channel, int netId) {
        Message resultMsg = channel.sendMessageSynchronously(151569, netId);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.what != 151570) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public void enableRssiPolling(boolean enabled) {
        sendMessage(CMD_ENABLE_RSSI_POLL, enabled ? 1 : 0, 0);
    }

    public void setHighPerfModeEnabled(boolean enable) {
        sendMessage(CMD_SET_HIGH_PERF_MODE, enable ? 1 : 0, 0);
    }

    public synchronized void resetSimAuthNetworks(boolean simPresent) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simPresent ? 1 : 0);
    }

    public void notifyImsiAvailabe(boolean imsiAvailabe) {
        this.mIsImsiAvailable = imsiAvailabe;
    }

    public Network getCurrentNetwork() {
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent == null) {
                return null;
            }
            return new Network(this.mNetworkAgent.netId);
        }
    }

    public void enableTdls(String remoteMacAddress, boolean enable) {
        sendMessage(CMD_ENABLE_TDLS, enable ? 1 : 0, 0, remoteMacAddress);
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
        }
    }

    public void dumpIpClient(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mIpClient != null) {
            pw.println("IpClient logs have moved to dumpsys network_stack");
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
        pw.println("mLastBssid " + StringUtilEx.safeDisplayBssid(this.mLastBssid));
        pw.println("mLastNetworkId " + this.mLastNetworkId);
        pw.println("mOperationalMode " + this.mOperationalMode);
        pw.println("mUserWantsSuspendOpt " + this.mUserWantsSuspendOpt);
        pw.println("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
        this.mCountryCode.dump(fd, pw, args);
        this.mNetworkFactory.dump(fd, pw, args);
        this.mUntrustedNetworkFactory.dump(fd, pw, args);
        pw.println("Wlan Wake Reasons:" + this.mWifiNative.getWlanWakeReasonCount());
        pw.println();
        this.mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        this.mPasspointManager.dump(pw);
        pw.println();
        this.mWifiDiagnostics.captureBugReportData(7);
        this.mWifiDiagnostics.dump(fd, pw, args);
        dumpIpClient(fd, pw, args);
        this.mWifiConnectivityManager.dump(fd, pw, args);
        this.mWifiInjector.getWakeupController().dump(fd, pw, args);
        this.mLinkProbeManager.dump(fd, pw, args);
        this.mWifiInjector.getWifiLastResortWatchdog().dump(fd, pw, args);
    }

    public void handleBootCompleted() {
        sendMessage(CMD_BOOT_COMPLETED);
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
    /* access modifiers changed from: public */
    private void logStateAndMessage(Message message, State state) {
        this.mMessageHandlingStatus = 0;
        String currentStateTag = "";
        if (state == getCurrentState()) {
            currentStateTag = "$";
        }
        if (mLogMessages) {
            switch (message.what) {
                case CMD_GET_CONFIGURED_NETWORKS /* 131131 */:
                case CMD_GET_SUPPORTED_FEATURES /* 131133 */:
                case CMD_GET_LINK_LAYER_STATS /* 131135 */:
                case CMD_RSSI_POLL /* 131155 */:
                case CMD_UPDATE_LINKPROPERTIES /* 131212 */:
                case WifiMonitor.SCAN_RESULTS_EVENT /* 147461 */:
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
        sb.append("screen=");
        sb.append(this.mScreenOn ? "on" : "off");
        if (this.mMessageHandlingStatus != 0) {
            sb.append("(");
            sb.append(this.mMessageHandlingStatus);
            sb.append(")");
        }
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=" + msg.sendingUid);
        }
        long duration = this.mClock.getUptimeSinceBootMillis() - msg.getWhen();
        if (duration > 1000) {
            sb.append(" dur:");
            TimeUtils.formatDuration(duration, sb);
        }
        switch (msg.what) {
            case CMD_ADD_OR_UPDATE_NETWORK /* 131124 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    WifiConfiguration config = (WifiConfiguration) msg.obj;
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                    sb.append(" prio=");
                    sb.append(config.priority);
                    sb.append(" status=");
                    sb.append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ");
                        sb.append(StringUtilEx.safeDisplayBssid(config.BSSID));
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (!curConfig.configKey().equals(config.configKey())) {
                            sb.append(" current=");
                            sb.append(StringUtilEx.safeDisplaySsid(curConfig.getPrintableSsid()));
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
            case CMD_ENABLE_NETWORK /* 131126 */:
            case 151569:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=");
                    sb.append(StringUtilEx.safeDisplaySsid(key));
                }
                WifiConfiguration config2 = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config2 != null && (key == null || !config2.configKey().equals(key))) {
                    sb.append(" target=");
                    sb.append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /* 131131 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=");
                sb.append(this.mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_RSSI_POLL /* 131155 */:
            case CMD_ONESHOT_RSSI_POLL /* 131156 */:
            case CMD_UNWANTED_NETWORK /* 131216 */:
            case 151572:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (!(this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID() == null)) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplaySsid(this.mWifiInfo.getSSID()));
                }
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplayBssid(this.mWifiInfo.getBSSID()));
                }
                sb.append(" rssi=");
                sb.append(this.mWifiInfo.getRssi());
                sb.append(" f=");
                sb.append(this.mWifiInfo.getFrequency());
                sb.append(" sc=");
                sb.append(this.mWifiInfo.score);
                sb.append(" link=");
                sb.append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", Double.valueOf(this.mWifiInfo.txSuccessRate)));
                sb.append(String.format(" %.1f,", Double.valueOf(this.mWifiInfo.txRetriesRate)));
                sb.append(String.format(" %.1f ", Double.valueOf(this.mWifiInfo.txBadRate)));
                sb.append(String.format(" rx=%.1f", Double.valueOf(this.mWifiInfo.rxSuccessRate)));
                sb.append(String.format(" bcn=%d", Integer.valueOf(this.mRunningBeaconCount)));
                String report = reportOnTime();
                if (report != null) {
                    sb.append(" ");
                    sb.append(report);
                }
                sb.append(String.format(" score=%d", Integer.valueOf(this.mWifiInfo.score)));
                break;
            case CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mRoamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /* 131168 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=");
                sb.append(this.mDisconnectingWatchdogCount);
                break;
            case CMD_IP_CONFIGURATION_LOST /* 131211 */:
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
                    sb.append(StringUtilEx.safeDisplayBssid(this.mWifiInfo.getBSSID()));
                }
                sb.append(String.format(" bcn=%d", Integer.valueOf(this.mRunningBeaconCount)));
                break;
            case CMD_UPDATE_LINKPROPERTIES /* 131212 */:
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
            case CMD_TARGET_BSSID /* 131213 */:
            case CMD_ASSOCIATED_BSSID /* 131219 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" BSSID=");
                    sb.append(StringUtilEx.safeDisplayBssid((String) msg.obj));
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" Target=");
                    sb.append(StringUtilEx.safeDisplayBssid(this.mTargetRoamBSSID));
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                break;
            case CMD_START_CONNECT /* 131215 */:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config3 = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config3 != null) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplaySsid(config3.getPrintableSsid()));
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplayBssid(this.mTargetRoamBSSID));
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                WifiConfiguration config4 = getCurrentWifiConfiguration();
                if (config4 != null) {
                    sb.append(StringUtilEx.safeDisplaySsid(config4.getPrintableSsid()));
                    break;
                }
                break;
            case CMD_START_ROAM /* 131217 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = (ScanResult) msg.obj;
                if (result != null) {
                    Long now = Long.valueOf(this.mClock.getWallClockMillis());
                    sb.append(" bssid=");
                    sb.append(StringUtilEx.safeDisplayBssid(result.BSSID));
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
                    sb.append(StringUtilEx.safeDisplayBssid(this.mTargetRoamBSSID));
                }
                sb.append(" roam=");
                sb.append(Boolean.toString(this.mIsAutoRoaming));
                sb.append(" fail count=");
                sb.append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_IP_REACHABILITY_LOST /* 131221 */:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append((String) msg.obj);
                    break;
                }
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
            case CMD_RSSI_THRESHOLD_BREACHED /* 131236 */:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS /* 131272 */:
                sb.append(" ");
                sb.append(msg.obj);
                break;
            case CMD_INSTALL_PACKET_FILTER /* 131274 */:
                sb.append(" len=" + ((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /* 131275 */:
                sb.append(" enabled=" + ((Boolean) msg.obj).booleanValue());
                break;
            case CMD_USER_SWITCH /* 131277 */:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_PRE_DHCP_ACTION /* 131327 */:
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
            case CMD_POST_DHCP_ACTION /* 131329 */:
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
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
            case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ");
                sb.append(StringUtilEx.safeDisplayBssid(this.mLastBssid));
                sb.append(" nid=");
                sb.append(this.mLastNetworkId);
                WifiConfiguration config5 = getCurrentWifiConfiguration();
                if (config5 != null) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplaySsid(config5.getPrintableSsid()));
                }
                String key2 = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key2 != null) {
                    sb.append(" last=");
                    sb.append(StringUtilEx.safeDisplaySsid(key2));
                    break;
                }
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplayBssid((String) msg.obj));
                }
                sb.append(" nid=");
                sb.append(msg.arg1);
                sb.append(" reason=");
                sb.append(msg.arg2);
                if (this.mLastBssid != null) {
                    sb.append(" lastbssid=");
                    sb.append(StringUtilEx.safeDisplayBssid(this.mLastBssid));
                }
                if (this.mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=");
                    sb.append(this.mWifiInfo.getFrequency());
                    sb.append(" rssi=");
                    sb.append(this.mWifiInfo.getRssi());
                    break;
                }
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
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
            case 147499:
                sb.append(" ");
                sb.append(" timedOut=" + Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = (String) msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplayBssid(bssid));
                }
                sb.append(" blacklist=" + Boolean.toString(this.mDidBlackListBSSID));
                break;
            case 151556:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                WifiConfiguration config6 = (WifiConfiguration) msg.obj;
                if (config6 != null) {
                    sb.append(" ");
                    sb.append(StringUtilEx.safeDisplaySsid(config6.getPrintableSsid()));
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
                    sb.append(StringUtilEx.safeDisplaySsid(config7.getPrintableSsid()));
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
            default:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                break;
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        String s = sGetWhatToString.get(what);
        if (s != null) {
            return s;
        }
        switch (what) {
            case 69632:
                return "CMD_CHANNEL_HALF_CONNECTED";
            case 69636:
                return "CMD_CHANNEL_DISCONNECTED";
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /* 143361 */:
                return "GROUP_CREATING_TIMED_OUT";
            case WifiMonitor.SUP_REQUEST_IDENTITY /* 147471 */:
                return "SUP_REQUEST_IDENTITY";
            case WifiMonitor.HS20_REMEDIATION_EVENT /* 147517 */:
                return "HS20_REMEDIATION_EVENT";
            case 151553:
                return "CONNECT_NETWORK";
            case 151556:
                return "FORGET_NETWORK";
            case 151559:
                return "SAVE_NETWORK";
            case 151572:
                return "RSSI_PKTCNT_FETCH";
            default:
                switch (what) {
                    case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
                        return "P2P_CONNECTION_CHANGED";
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                        return "DISCONNECT_WIFI_REQUEST";
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /* 143373 */:
                        return "DISCONNECT_WIFI_RESPONSE";
                    case WifiP2pServiceImpl.SET_MIRACAST_MODE /* 143374 */:
                        return "SET_MIRACAST_MODE";
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /* 143375 */:
                        return "BLOCK_DISCOVERY";
                    default:
                        switch (what) {
                            case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                                return "NETWORK_CONNECTION_EVENT";
                            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                                return "NETWORK_DISCONNECTION_EVENT";
                            default:
                                switch (what) {
                                    case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                                        return "SUPPLICANT_STATE_CHANGE_EVENT";
                                    case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                                        return "AUTHENTICATION_FAILURE_EVENT";
                                    case WifiMonitor.WPS_SUCCESS_EVENT /* 147464 */:
                                        return "WPS_SUCCESS_EVENT";
                                    case WifiMonitor.WPS_FAIL_EVENT /* 147465 */:
                                        return "WPS_FAIL_EVENT";
                                    default:
                                        switch (what) {
                                            case 147499:
                                                return "ASSOCIATION_REJECTION_EVENT";
                                            case WifiMonitor.ANQP_DONE_EVENT /* 147500 */:
                                                return "ANQP_DONE_EVENT";
                                            default:
                                                switch (what) {
                                                    case WifiMonitor.GAS_QUERY_START_EVENT /* 147507 */:
                                                        return "GAS_QUERY_START_EVENT";
                                                    case WifiMonitor.GAS_QUERY_DONE_EVENT /* 147508 */:
                                                        return "GAS_QUERY_DONE_EVENT";
                                                    case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /* 147509 */:
                                                        return "RX_HS20_ANQP_ICON_EVENT";
                                                    default:
                                                        switch (what) {
                                                            case 151562:
                                                                return "START_WPS";
                                                            case 151563:
                                                                return "START_WPS_SUCCEEDED";
                                                            case 151564:
                                                                return "WPS_FAILED";
                                                            case 151565:
                                                                return "WPS_COMPLETED";
                                                            case 151566:
                                                                return "CANCEL_WPS";
                                                            case 151567:
                                                                return "CANCEL_WPS_FAILED";
                                                            case 151568:
                                                                return "CANCEL_WPS_SUCCEDED";
                                                            case 151569:
                                                                return "DISABLE_NETWORK";
                                                            default:
                                                                return "what:" + Integer.toString(what);
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }

    private void initializeWpsDetails() {
        String detail = WifiCommonUtils.getPersistedDeviceName(this.mWifiInjector, this.mContext);
        if (!this.mWifiNative.setDeviceName(this.mInterfaceName, detail)) {
            loge("Failed to set device name " + detail);
        }
        String detail2 = this.mPropertyService.get(PRODUCT_MANUFACTURER, "");
        if (!this.mWifiNative.setManufacturer(this.mInterfaceName, detail2)) {
            loge("Failed to set manufacturer " + detail2);
        }
        String detail3 = this.mPropertyService.get(PRODUCT_MODEL, "");
        if (!this.mWifiNative.setModelName(this.mInterfaceName, detail3)) {
            loge("Failed to set model name " + detail3);
        }
        String detail4 = this.mPropertyService.get(PRODUCT_MODEL, "");
        if (!this.mWifiNative.setModelNumber(this.mInterfaceName, detail4)) {
            loge("Failed to set model number " + detail4);
        }
        if (!this.mWifiNative.setSerialNumber(this.mInterfaceName, this.mPropertyService.get(SERIAL_NUMBER, ""))) {
            loge("Failed to set serial number");
        }
        if (!this.mWifiNative.setConfigMethods(this.mInterfaceName, PHYSICAL_DISPLAY)) {
            loge("Failed to set WPS config methods");
        }
        if (!this.mWifiNative.setDeviceType(this.mInterfaceName, this.mPrimaryDeviceType)) {
            loge("Failed to set primary device type " + this.mPrimaryDeviceType);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
        if (this.mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
        }
        enableRssiPolling(screenOn);
        logd("apf dbg, screen state change: screenOn=" + screenOn);
        notifyScreenState(screenOn);
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
        if (this.mIsRunning) {
            getWifiLinkLayerStats();
            this.mOnTimeScreenStateChange = this.mOnTime;
            this.mLastScreenStateChangeTimeStamp = this.mLastLinkLayerStatsUpdate;
        }
        this.mWifiMetrics.setScreenState(screenOn);
        long currenTime = this.mClock.getElapsedSinceBootMillis();
        if (screenOn && currenTime - this.mLastAllowSendHiLinkScanResultsBroadcastTime > 3000) {
            Log.i(TAG, "handleScreenStateChanged: allow send HiLink scan results broadcast.");
            this.mScanRequestProxy.mHwScanRequestProxyEx.setAllowHiLinkScanResultsBroadcast(true);
            this.mLastAllowSendHiLinkScanResultsBroadcastTime = currenTime;
        }
        this.mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        this.mNetworkFactory.handleScreenStateChanged(screenOn);
        WifiLockManager wifiLockManager = this.mWifiInjector.getWifiLockManager();
        if (wifiLockManager == null) {
            Log.w(TAG, "WifiLockManager not initialized, skipping screen state notification");
        } else {
            wifiLockManager.handleScreenStateChanged(screenOn);
        }
        this.mSarManager.handleScreenStateChanged(screenOn);
        if (this.mVerboseLoggingEnabled) {
            log("handleScreenStateChanged Exit: " + screenOn);
        }
    }

    private boolean checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        if (this.mCm != null) {
            return true;
        }
        Log.e(TAG, "Cannot retrieve connectivity service");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSuspendOptimizationsNative(int reason, boolean enabled) {
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
    /* access modifiers changed from: public */
    private void setSuspendOptimizations(int reason, boolean enabled) {
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
            if (this.isBootCompleted) {
                WifiP2pServiceImpl wifiP2pServiceImpl = this.mWifiP2pServiceImpl;
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
    /* access modifiers changed from: public */
    private void fetchRssiLinkSpeedAndFrequencyNative() {
        if (SupplicantState.ASSOCIATED.compareTo(this.mWifiInfo.getSupplicantState()) > 0 || SupplicantState.COMPLETED.compareTo(this.mWifiInfo.getSupplicantState()) < 0) {
            loge("error state to fetch rssi");
            return;
        }
        WifiNative.SignalPollResult pollResult = this.mWifiNative.signalPoll(this.mInterfaceName);
        if (pollResult != null) {
            int newRssi = pollResult.currentRssi;
            int newTxLinkSpeed = pollResult.txBitrate;
            int newFrequency = pollResult.associationFrequency;
            int newRxLinkSpeed = pollResult.rxBitrate;
            this.mWifiInfo.setNoise(pollResult.currentNoise);
            this.mWifiInfo.setSnr(pollResult.currentSnr);
            this.mWifiInfo.setChload(pollResult.currentChload);
            this.mWifiInfo.setUlDelay(pollResult.currentUlDelay);
            if (this.mVerboseLoggingEnabled) {
                logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi + " TxLinkspeed=" + newTxLinkSpeed + " freq=" + newFrequency + " RxLinkSpeed=" + newRxLinkSpeed);
            }
            if (newRssi <= -127 || newRssi >= 200) {
                this.mWifiInfo.setRssi(WifiMetrics.MIN_RSSI_DELTA);
                updateCapabilities();
            } else {
                if (newRssi > 5) {
                    newRssi -= 256;
                }
                this.mWifiInfo.setRssi(newRssi);
                if (checkAndGetHwMSSHandlerManager() != null) {
                    this.mHwMssHandler.mssSwitchCheck(newRssi);
                }
                if (isAllowedManualWifiPwrBoost() == 0) {
                    this.mWifiNative.mHwWifiNativeEx.pwrPercentBoostModeset(newRssi);
                }
                int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), newRssi);
                if (newSignalLevel != this.mLastSignalLevel) {
                    updateCapabilities();
                    sendRssiChangeBroadcast(newRssi);
                }
                this.mLastSignalLevel = newSignalLevel;
            }
            if (newTxLinkSpeed > 0) {
                this.mWifiInfo.setLinkSpeed(newTxLinkSpeed);
                this.mWifiInfo.setTxLinkSpeedMbps(newTxLinkSpeed);
            }
            if (newRxLinkSpeed > 0) {
                this.mWifiInfo.setRxLinkSpeedMbps(newRxLinkSpeed);
            }
            if (newFrequency > 0) {
                this.mWifiInfo.setFrequency(newFrequency);
                sendStaFrequency(newFrequency);
            }
            this.mWifiConfigManager.updateScanDetailCacheFromWifiInfo(this.mWifiInfo);
            this.mWifiMetrics.handlePollResult(this.mWifiInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanWifiScore() {
        ExtendedWifiInfo extendedWifiInfo = this.mWifiInfo;
        extendedWifiInfo.txBadRate = 0.0d;
        extendedWifiInfo.txSuccessRate = 0.0d;
        extendedWifiInfo.txRetriesRate = 0.0d;
        extendedWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport.reset();
        this.mLastLinkLayerStats = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLinkProperties(LinkProperties newLp) {
        if (this.mVerboseLoggingEnabled) {
            log("Link configuration changed for netId: " + this.mLastNetworkId + " old: " + this.mLinkProperties + " new: " + newLp);
        }
        this.mLinkProperties = newLp;
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendLinkProperties(this.mLinkProperties);
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
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
    }

    private String updateDefaultRouteMacAddress(int timeout) {
        String address = null;
        for (RouteInfo route : this.mLinkProperties.getRoutes()) {
            if (route != null && route.isDefaultRoute() && route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                if (gateway instanceof Inet4Address) {
                    if (this.mVerboseLoggingEnabled) {
                        logd("updateDefaultRouteMacAddress found Ipv4 default :" + StringUtilEx.safeDisplayBssid(gateway.getHostAddress()));
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
    /* access modifiers changed from: public */
    private void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        StatsLog.write(38, WifiManager.calculateSignalLevel(newRssi, 5));
        Intent intent = new Intent("android.net.wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkStateChangeBroadcast(String bssid) {
        WifiRepeater wifiRepeater;
        Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
        intent.addFlags(67108864);
        NetworkInfo networkInfo = new NetworkInfo(this.mNetworkInfo);
        networkInfo.setExtraInfo(null);
        intent.putExtra("networkInfo", networkInfo);
        checkSelfCureWifiResult(102);
        if (!ignoreNetworkStateChange(this.mNetworkInfo)) {
            Log.i(TAG, "NetworkStateChange " + this.mNetworkInfo.getState() + "/" + this.mNetworkInfo.getDetailedState());
            if (this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                notifyWpa3SelfCureConnectSucc();
                WifiRepeater wifiRepeater2 = this.mWifiRepeater;
                if (wifiRepeater2 != null) {
                    ExtendedWifiInfo extendedWifiInfo = this.mWifiInfo;
                    wifiRepeater2.handleWifiConnect(extendedWifiInfo, this.mWifiConfigManager.getConfiguredNetwork(extendedWifiInfo.getNetworkId()));
                }
            }
            if (this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED && (wifiRepeater = this.mWifiRepeater) != null) {
                wifiRepeater.handleWifiDisconnect();
            }
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
    /* access modifiers changed from: public */
    private boolean setNetworkDetailedState(NetworkInfo.DetailedState state) {
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
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private NetworkInfo.DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SupplicantState handleSupplicantStateChange(Message message) {
        ScanDetail scanDetail;
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        SupplicantState state = stateChangeResult.state;
        this.mWifiScoreCard.noteSupplicantStateChanging(this.mWifiInfo, state);
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
        updateL2KeyAndGroupHint();
        updateCapabilities();
        if (SupplicantState.AUTHENTICATING == state || SupplicantState.ASSOCIATED == state) {
            fetchRssiLinkSpeedAndFrequencyNative();
        }
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            stateChangeResult.networkId = config.networkId;
            this.mWifiInfo.setEphemeral(config.ephemeral);
            this.mWifiInfo.setTrusted(config.trusted);
            this.mWifiInfo.setOsuAp(config.osu);
            if (config.fromWifiNetworkSpecifier || config.fromWifiNetworkSuggestion) {
                this.mWifiInfo.setNetworkSuggestionOrSpecifierPackageName(config.creatorName);
            }
            ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
            if (!(scanDetailCache == null || (scanDetail = scanDetailCache.getScanDetail(stateChangeResult.BSSID)) == null)) {
                this.mWifiInfo.setFrequency(scanDetail.getScanResult().frequency);
                NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                if (networkDetail != null && networkDetail.getAnt() == NetworkDetail.Ant.ChargeablePublic) {
                    this.mWifiInfo.setMeteredHint(true);
                }
            }
        }
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
        } else {
            this.mWifiInfo.setNetworkId(-1);
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        this.mWifiScoreCard.noteSupplicantStateChanged(this.mWifiInfo);
        return state;
    }

    private void updateDefaultMtu() {
        LinkProperties linkProperties = this.mLinkProperties;
        if (linkProperties == null || this.mNwService == null) {
            loge("LinkProperties or NwService is null.");
            return;
        }
        String iface = linkProperties.getInterfaceName();
        if (!TextUtils.isEmpty(iface)) {
            if (DEFAULT_MTU == this.mLinkProperties.getMtu()) {
                log("MTU is same as the default: 1500");
                return;
            }
            this.mLinkProperties.setMtu(DEFAULT_MTU);
            try {
                log("Setting MTU size: " + iface + ", " + DEFAULT_MTU);
                this.mNwService.setMtu(iface, (int) DEFAULT_MTU);
            } catch (Exception e) {
                loge("exception in setMtu()");
            }
        }
    }

    private void updateL2KeyAndGroupHint() {
        if (this.mIpClient != null) {
            Pair<String, String> p = this.mWifiScoreCard.getL2KeyAndGroupHint(this.mWifiInfo);
            if (p.equals(this.mLastL2KeyAndGroupHint)) {
                return;
            }
            if (this.mIpClient.setL2KeyAndGroupHint((String) p.first, (String) p.second)) {
                this.mLastL2KeyAndGroupHint = p;
            } else {
                this.mLastL2KeyAndGroupHint = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wifi.AbsWifiStateMachine
    public void handleNetworkDisconnect() {
        if (DBG) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (wifiConfig != null) {
            this.mWifiInjector.getWakeupController().setLastDisconnectInfo(ScanResultMatchInfo.fromWifiConfiguration(wifiConfig));
            this.mWifiNetworkSuggestionsManager.handleDisconnect(wifiConfig, getCurrentBSSID());
        }
        stopRssiMonitoringOffload();
        clearTargetBssid("handleNetworkDisconnect");
        stopIpClient();
        this.mWifiScoreReport.reset();
        this.mWifiInfo.reset();
        this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
        this.mIsAutoRoaming = false;
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent != null) {
                this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
                this.mNetworkAgent = null;
            }
        }
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (TelephonyUtil.isSimConfig(config) && TelephonyUtil.getSimIdentity(getTelephonyManager(), new TelephonyUtil(), config, this.mWifiInjector.getCarrierNetworkConfig()) == null) {
            Log.i(TAG, "remove EAP-SIM/AKA/AKA' network " + this.mLastNetworkId + " from wpa_s since identity was absent");
            this.mWifiNative.removeAllNetworks(this.mInterfaceName);
        }
        updateDefaultMtu();
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        this.mLastBssid = null;
        this.mLastLinkLayerStats = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
        this.mWifiScoreCard.resetConnectionState();
        updateL2KeyAndGroupHint();
    }

    /* access modifiers changed from: package-private */
    public void handlePreDhcpSetup() {
        this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 1);
        notifyScreenState(true);
        setSuspendOptimizationsNative(1, false);
        setPowerSave(false);
        getWifiLinkLayerStats();
        if (this.mWifiP2pChannel != null) {
            Message msg = Message.obtain();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = 1;
            msg.arg2 = CMD_PRE_DHCP_ACTION_COMPLETE;
            msg.obj = this;
            this.mWifiP2pChannel.sendMessage(msg);
            return;
        }
        sendMessage(CMD_PRE_DHCP_ACTION_COMPLETE);
    }

    /* access modifiers changed from: package-private */
    public void handlePostDhcpSetup() {
        if (!this.mScreenOn) {
            notifyScreenState(false);
        }
        setSuspendOptimizationsNative(1, true);
        setPowerSave(true);
        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 2);
    }

    public boolean setPowerSave(boolean ps) {
        if (this.mInterfaceName != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Setting power save for: " + this.mInterfaceName + " to: " + ps);
            }
            this.mWifiNative.setPowerSave(this.mInterfaceName, ps);
            return true;
        }
        Log.e(TAG, "Failed to setPowerSave, interfaceName is null");
        return false;
    }

    public boolean setLowLatencyMode(boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Setting low latency mode to " + enabled);
        }
        if (this.mWifiNative.setLowLatencyMode(enabled)) {
            return true;
        }
        Log.e(TAG, "Failed to setLowLatencyMode");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportConnectionAttemptStart(WifiConfiguration config, String targetBSSID, int roamType) {
        this.mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        this.mWifiDiagnostics.reportConnectionEvent((byte) 0);
        removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT, 60000);
    }

    private void handleConnectionAttemptEndForDiagnostics(int level2FailureCode) {
        if (level2FailureCode != 1 && level2FailureCode != 5) {
            removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
            this.mWifiDiagnostics.reportConnectionEvent((byte) 2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode, int level2FailureReason) {
        if (level2FailureCode != 1) {
            this.mWifiScoreCard.noteConnectionFailure(this.mWifiInfo, level2FailureCode, connectivityFailureCode);
        }
        WifiConfiguration configuration = getCurrentWifiConfiguration();
        if (configuration == null) {
            configuration = getTargetWifiConfiguration();
        }
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode, level2FailureReason);
        this.mWifiConnectivityManager.handleConnectionAttemptEnded(level2FailureCode);
        if (configuration != null) {
            this.mNetworkFactory.handleConnectionAttemptEnded(level2FailureCode, configuration);
            this.mWifiNetworkSuggestionsManager.handleConnectionAttemptEnded(level2FailureCode, configuration, getCurrentBSSID());
        }
        handleConnectionAttemptEndForDiagnostics(level2FailureCode);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
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
            this.mWifiInfo.setTrusted(config.trusted);
        }
        if (dhcpResults.hasMeteredHint() || hasMeteredHintForWi(addr)) {
            this.mWifiInfo.setMeteredHint(true);
        } else {
            this.mWifiInfo.setMeteredHint(false);
        }
        updateCapabilities(config);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSuccessfulIpConfiguration() {
        this.mLastSignalLevel = -1;
        WifiConfiguration c = getCurrentWifiConfiguration();
        if (c != null) {
            c.getNetworkSelectionStatus().clearDisableReasonCounter(4);
            updateCapabilities(c);
        }
        this.mWifiScoreCard.noteIpConfiguration(this.mWifiInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIPv4Failure() {
        this.mWifiDiagnostics.captureBugReportData(4);
        if (this.mVerboseLoggingEnabled) {
            int count = -1;
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (config != null) {
                count = config.getNetworkSelectionStatus().getDisableReasonCounter(4);
            }
            log("DHCP failure count=" + count);
        }
        reportConnectionAttemptEnd(10, 2, 0);
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
    /* access modifiers changed from: public */
    private void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        if (DBG) {
            loge("handleIpConfigurationLost: SSID = " + StringUtilEx.safeDisplaySsid(this.mWifiInfo.getSSID()) + ", BSSID = " + StringUtilEx.safeDisplayBssid(this.mWifiInfo.getBSSID()));
        }
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        notifyWifiConnFailedInfo(this.mLastNetworkId, this.mWifiInfo.getBSSID(), this.mWifiInfo.getRssi(), 4, this.mWifiConnectivityManager);
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIpReachabilityLost() {
        this.mWifiScoreCard.noteIpReachabilityLost(this.mWifiInfo);
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    private String macAddressFromRoute(String ipAddress) {
        String macAddress = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            reader2.readLine();
            while (true) {
                String line = reader2.readLine();
                if (line == null) {
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
                loge("Did not find remoteAddress {" + StringUtilEx.safeDisplayIpAddress(ipAddress) + "} in /proc/net/arp");
            }
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            loge("Could not open /proc/net/arp to lookup mac address");
            if (0 != 0) {
                reader.close();
            }
        } catch (IOException e3) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
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
    /* access modifiers changed from: public */
    private boolean isPermanentWrongPasswordFailure(int networkId, int reasonCode) {
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
    public void registerNetworkFactory() {
        if (checkAndSetConnectivityInstance()) {
            this.mNetworkFactory.setScoreFilter(60);
            this.mNetworkFactory.register();
            this.mUntrustedNetworkFactory.register();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getAdditionalWifiServiceInterfaces() {
        if (this.mP2pSupported) {
            this.mWifiP2pServiceImpl = IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
            if (this.mWifiP2pServiceImpl != null && this.mWifiP2pChannel == null) {
                this.mWifiP2pChannel = new AsyncChannel();
                this.mWifiP2pChannel.connect(this.mContext, getHandler(), this.mWifiP2pServiceImpl.getP2pStateMachineMessenger());
                this.mWifiRepeater = this.mWifiP2pServiceImpl.getWifiRepeater();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void configureRandomizedMacAddress(WifiConfiguration config) {
        if (config == null) {
            Log.e(TAG, "No config to change MAC address to");
        } else if (shouldUseFactoryMac(config)) {
            setCurrentMacToFactoryMac(config);
            this.isRandomSelfCure = true;
            this.isUseRandomMac = false;
        } else {
            if (config.mIsCreatedByClone) {
                Log.i(TAG, "randommac, this config is created by clone, should update random mac");
                MacAddress persistentMac = this.mWifiConfigManager.getPersistentMacAddress(config);
                if (persistentMac != null) {
                    config.setRandomizedMacAddress(persistentMac);
                    config.mIsCreatedByClone = false;
                    if (!this.mWifiConfigManager.addOrUpdateNetwork(config, 1000).isSuccess()) {
                        Log.e(TAG, "randommac, update config failed for network: " + config.networkId);
                    }
                } else {
                    Log.e(TAG, "randommac, persistentMac is null");
                }
            }
            String strCurrentMac = this.mWifiNative.getMacAddress(this.mInterfaceName);
            if (strCurrentMac != null) {
                MacAddress currentMac = MacAddress.fromString(strCurrentMac);
                MacAddress newMac = config.getOrCreateRandomizedMacAddress();
                this.mWifiConfigManager.setNetworkRandomizedMacAddress(config.networkId, newMac);
                if (!WifiConfiguration.isValidMacAddressForRandomization(newMac)) {
                    Log.wtf(TAG, "Config generated an invalid MAC address");
                } else if (currentMac.equals(newMac)) {
                    Log.i(TAG, "No changes in MAC address");
                } else {
                    this.mWifiMetrics.logStaEvent(17, config);
                    boolean setMacSuccess = this.mWifiNative.setMacAddress(this.mInterfaceName, newMac);
                    Log.i(TAG, "ConnectedMacRandomization SSID(" + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + "). setMacAddress(" + StringUtilEx.safeDisplayBssid(newMac.toString()) + ") from " + StringUtilEx.safeDisplayBssid(currentMac.toString()) + " = " + setMacSuccess);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setCurrentMacToFactoryMac(WifiConfiguration config) {
        MacAddress factoryMac = this.mWifiNative.getFactoryMacAddress(this.mInterfaceName);
        if (factoryMac == null) {
            Log.e(TAG, "Fail to set factory MAC address. Factory MAC is null.");
        } else if (TextUtils.equals(this.mWifiNative.getMacAddress(this.mInterfaceName), factoryMac.toString())) {
        } else {
            if (this.mWifiNative.setMacAddress(this.mInterfaceName, factoryMac)) {
                this.mWifiMetrics.logStaEvent(17, config);
                return;
            }
            Log.e(TAG, "Failed to set MAC address to '" + StringUtilEx.safeDisplayBssid(factoryMac.toString()) + "'");
        }
    }

    public boolean isConnectedMacRandomizationEnabled() {
        return this.mConnectedMacRandomzationSupported;
    }

    public void failureDetected(int reason) {
        this.mWifiInjector.getSelfRecovery().trigger(2);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int removeResult = -1;
            boolean disableOthers = false;
            switch (message.what) {
                case 0:
                    Log.wtf(ClientModeImpl.TAG, "Error! empty message encountered");
                    break;
                case 69632:
                    if (((AsyncChannel) message.obj) == ClientModeImpl.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            ClientModeImpl.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        } else {
                            ClientModeImpl.this.p2pSendMessage(69633);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("got HALF_CONNECTED for unknown channel");
                        break;
                    }
                case 69636:
                    if (((AsyncChannel) message.obj) == ClientModeImpl.this.mWifiP2pChannel) {
                        ClientModeImpl.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        disableOthers = true;
                    }
                    clientModeImpl.mBluetoothConnectionActive = disableOthers;
                    break;
                case ClientModeImpl.CMD_ADD_OR_UPDATE_NETWORK /* 131124 */:
                    NetworkUpdateResult result = ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork((WifiConfiguration) message.obj, message.sendingUid);
                    if (!result.isSuccess()) {
                        ClientModeImpl.this.mMessageHandlingStatus = -2;
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, result.getNetworkId());
                    break;
                case ClientModeImpl.CMD_REMOVE_NETWORK /* 131125 */:
                    ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, false);
                    break;
                case ClientModeImpl.CMD_ENABLE_NETWORK /* 131126 */:
                    if (message.arg2 == 1) {
                        disableOthers = true;
                    }
                    boolean ok = ClientModeImpl.this.mWifiConfigManager.enableNetwork(message.arg1, disableOthers, message.sendingUid);
                    if (!ok) {
                        ClientModeImpl.this.mMessageHandlingStatus = -2;
                    }
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    int i = message.what;
                    if (ok) {
                        removeResult = 1;
                    }
                    clientModeImpl2.replyToMessage(message, i, removeResult);
                    break;
                case ClientModeImpl.CMD_GET_CONFIGURED_NETWORKS /* 131131 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mWifiConfigManager.getSavedNetworks(message.arg2));
                    break;
                case ClientModeImpl.CMD_GET_SUPPORTED_FEATURES /* 131133 */:
                    if (ClientModeImpl.this.mFeatureSet <= 0) {
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        clientModeImpl3.mFeatureSet = clientModeImpl3.mWifiNative.getSupportedFeatureSet(ClientModeImpl.this.mInterfaceName);
                        if (ClientModeImpl.DBG) {
                            Log.i(ClientModeImpl.TAG, "CMD_GET_SUPPORTED_FEATURES: " + ClientModeImpl.this.mFeatureSet);
                        }
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, Long.valueOf(ClientModeImpl.this.mFeatureSet));
                    break;
                case ClientModeImpl.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /* 131134 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case ClientModeImpl.CMD_GET_LINK_LAYER_STATS /* 131135 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, (Object) null);
                    break;
                case ClientModeImpl.CMD_SET_OPERATIONAL_MODE /* 131144 */:
                    break;
                case ClientModeImpl.CMD_DISCONNECT /* 131145 */:
                case ClientModeImpl.CMD_RECONNECT /* 131146 */:
                case ClientModeImpl.CMD_REASSOCIATE /* 131147 */:
                case ClientModeImpl.CMD_RSSI_POLL /* 131155 */:
                case ClientModeImpl.CMD_ONESHOT_RSSI_POLL /* 131156 */:
                case ClientModeImpl.CMD_TEST_NETWORK_DISCONNECT /* 131161 */:
                case ClientModeImpl.CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                case ClientModeImpl.CMD_DISCONNECTING_WATCHDOG_TIMER /* 131168 */:
                case ClientModeImpl.CMD_DISABLE_EPHEMERAL_NETWORK /* 131170 */:
                case ClientModeImpl.CMD_TARGET_BSSID /* 131213 */:
                case ClientModeImpl.CMD_START_CONNECT /* 131215 */:
                case ClientModeImpl.CMD_UNWANTED_NETWORK /* 131216 */:
                case ClientModeImpl.CMD_START_ROAM /* 131217 */:
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /* 131219 */:
                case ClientModeImpl.CMD_PRE_DHCP_ACTION /* 131327 */:
                case ClientModeImpl.CMD_PRE_DHCP_ACTION_COMPLETE /* 131328 */:
                case ClientModeImpl.CMD_POST_DHCP_ACTION /* 131329 */:
                case ClientModeImpl.CMD_TRY_CACHED_IP /* 131330 */:
                case ClientModeImpl.CMD_WPS_PIN_RETRY /* 131576 */:
                case ClientModeImpl.CMD_SET_DETECTMODE_CONF /* 131772 */:
                case ClientModeImpl.CMD_SET_DETECT_PERIOD /* 131773 */:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                case WifiMonitor.WPS_OVERLAP_EVENT /* 147466 */:
                case WifiMonitor.SUP_REQUEST_IDENTITY /* 147471 */:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /* 147472 */:
                case 147474:
                case WifiMonitor.WAPI_CERTIFICATION_FAILURE_EVENT /* 147475 */:
                case 147499:
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /* 147520 */:
                case WifiMonitor.WPS_START_OKC_EVENT /* 147656 */:
                case WifiMonitor.WPA3_CONNECT_FAIL_EVENT /* 147666 */:
                case WifiMonitor.EAP_ERRORCODE_REPORT_EVENT /* 147956 */:
                case 151575:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case ClientModeImpl.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizations(2, true);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizations(2, false);
                        break;
                    }
                case ClientModeImpl.CMD_ENABLE_RSSI_POLL /* 131154 */:
                    ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                    if (message.arg1 == 1) {
                        disableOthers = true;
                    }
                    clientModeImpl4.mEnableRssiPolling = disableOthers;
                    break;
                case ClientModeImpl.CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizations(4, false);
                        break;
                    } else {
                        if (message.arg2 == 1) {
                            ClientModeImpl.this.mSuspendWakeLock.release();
                        }
                        ClientModeImpl.this.setSuspendOptimizations(4, true);
                        break;
                    }
                case ClientModeImpl.CMD_SCREEN_STATE_CHANGED /* 131167 */:
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        disableOthers = true;
                    }
                    clientModeImpl5.handleScreenStateChanged(disableOthers);
                    break;
                case ClientModeImpl.CMD_REMOVE_APP_CONFIGURATIONS /* 131169 */:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_RESET_SIM_NETWORKS /* 131173 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_QUERY_OSU_ICON /* 131176 */:
                case ClientModeImpl.CMD_MATCH_PROVIDER_NETWORK /* 131177 */:
                    ClientModeImpl.this.replyToMessage(message, message.what);
                    break;
                case ClientModeImpl.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /* 131178 */:
                    Bundle bundle = (Bundle) message.obj;
                    if (bundle != null) {
                        if (ClientModeImpl.this.mPasspointManager.addOrUpdateProvider((PasspointConfiguration) bundle.getParcelable(ClientModeImpl.EXTRA_PASSPOINT_CONFIGURATION), bundle.getInt(ClientModeImpl.EXTRA_UID), bundle.getString(ClientModeImpl.EXTRA_PACKAGE_NAME))) {
                            removeResult = 1;
                        }
                        ClientModeImpl.this.replyToMessage(message, message.what, removeResult);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_REMOVE_PASSPOINT_CONFIG /* 131179 */:
                    if (ClientModeImpl.this.mPasspointManager.removeProvider((String) message.obj)) {
                        removeResult = 1;
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, removeResult);
                    break;
                case ClientModeImpl.CMD_GET_PASSPOINT_CONFIGS /* 131180 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getProviderConfigs());
                    break;
                case ClientModeImpl.CMD_GET_MATCHING_OSU_PROVIDERS /* 131181 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, new HashMap());
                    break;
                case ClientModeImpl.CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS /* 131182 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, new HashMap());
                    break;
                case ClientModeImpl.CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES /* 131184 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, new ArrayList());
                    break;
                case ClientModeImpl.CMD_BOOT_COMPLETED /* 131206 */:
                    ClientModeImpl.this.isBootCompleted = true;
                    ClientModeImpl.this.getAdditionalWifiServiceInterfaces();
                    new MemoryStoreImpl(ClientModeImpl.this.mContext, ClientModeImpl.this.mWifiInjector, ClientModeImpl.this.mWifiScoreCard).start();
                    if (!ClientModeImpl.this.mWifiConfigManager.loadFromStore()) {
                        Log.e(ClientModeImpl.TAG, "Failed to load from config store");
                    }
                    ClientModeImpl.this.registerNetworkFactory();
                    break;
                case ClientModeImpl.CMD_INITIALIZE /* 131207 */:
                    boolean ok2 = ClientModeImpl.this.mWifiNative.initialize();
                    ClientModeImpl.this.mPasspointManager.initializeProvisioner(ClientModeImpl.this.mWifiInjector.getWifiServiceHandlerThread().getLooper());
                    ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                    int i2 = message.what;
                    if (ok2) {
                        removeResult = 1;
                    }
                    clientModeImpl6.replyToMessage(message, i2, removeResult);
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL /* 131210 */:
                case ClientModeImpl.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                case ClientModeImpl.CMD_IP_REACHABILITY_LOST /* 131221 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case ClientModeImpl.CMD_UPDATE_LINKPROPERTIES /* 131212 */:
                    ClientModeImpl.this.updateLinkProperties((LinkProperties) message.obj);
                    break;
                case ClientModeImpl.CMD_REMOVE_USER_CONFIGURATIONS /* 131224 */:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_START_IP_PACKET_OFFLOAD /* 131232 */:
                case ClientModeImpl.CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                case ClientModeImpl.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /* 131281 */:
                case ClientModeImpl.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /* 131282 */:
                    if (ClientModeImpl.this.mNetworkAgent != null) {
                        ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case ClientModeImpl.CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS /* 131240 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, new HashMap());
                    break;
                case ClientModeImpl.CMD_INSTALL_PACKET_FILTER /* 131274 */:
                    ClientModeImpl.this.mWifiNative.installPacketFilter(ClientModeImpl.this.mInterfaceName, (byte[]) message.obj);
                    break;
                case ClientModeImpl.CMD_SET_FALLBACK_PACKET_FILTERING /* 131275 */:
                    if (!((Boolean) message.obj).booleanValue()) {
                        ClientModeImpl.this.mWifiNative.stopFilteringMulticastV4Packets(ClientModeImpl.this.mInterfaceName);
                        break;
                    } else {
                        ClientModeImpl.this.mWifiNative.startFilteringMulticastV4Packets(ClientModeImpl.this.mInterfaceName);
                        break;
                    }
                case ClientModeImpl.CMD_USER_SWITCH /* 131277 */:
                    Set<Integer> removedNetworkIds = ClientModeImpl.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mLastNetworkId))) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_USER_UNLOCK /* 131278 */:
                    ClientModeImpl.this.mWifiConfigManager.handleUserUnlock(message.arg1);
                    break;
                case ClientModeImpl.CMD_USER_STOP /* 131279 */:
                    ClientModeImpl.this.mWifiConfigManager.handleUserStop(message.arg1);
                    break;
                case ClientModeImpl.CMD_READ_PACKET_FILTER /* 131280 */:
                    byte[] data = ClientModeImpl.this.mWifiNative.readPacketFilter(ClientModeImpl.this.mInterfaceName);
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.readPacketFilterComplete(data);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_DIAGS_CONNECT_TIMEOUT /* 131324 */:
                    ClientModeImpl.this.mWifiDiagnostics.reportConnectionEvent((byte) 3);
                    break;
                case ClientModeImpl.CMD_START_SUBSCRIPTION_PROVISIONING /* 131326 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, 0);
                    break;
                case ClientModeImpl.CMD_GET_CHANNEL_LIST_5G /* 131572 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, (Object) null);
                    break;
                case ClientModeImpl.CMD_PNO_PERIODIC_SCAN /* 131575 */:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_GET_SUPPORT_VOWIFI_DETECT /* 131774 */:
                    ClientModeImpl.this.processIsSupportVoWifiDetect(message);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
                    ClientModeImpl.this.mP2pConnected.set(((NetworkInfo) message.obj).isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                    ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                    if (message.arg1 == 1) {
                        disableOthers = true;
                    }
                    clientModeImpl7.mTemporarilyDisconnectWifi = disableOthers;
                    ClientModeImpl.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (ClientModeImpl.CHIPSET_TYPE.contains(ClientModeImpl.CHIPSET_BCM) && stateChangeResult != null && stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        Log.e(ClientModeImpl.TAG, "Detected driver hang, recover");
                        ClientModeImpl.this.mWifiInjector.getSelfRecovery().trigger(1);
                        break;
                    } else {
                        ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    }
                case WifiMonitor.EVENT_ANT_CORE_ROB /* 147757 */:
                    ClientModeImpl.this.handleAntenaPreempted();
                    break;
                case 151553:
                    ClientModeImpl.this.replyToMessage(message, 151554, 2);
                    break;
                case 151556:
                    ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, true);
                    break;
                case 151559:
                    ClientModeImpl.this.saveNetworkConfigAndSendReply(message);
                    break;
                case 151562:
                    ClientModeImpl.this.replyToMessage(message, 151564, 2);
                    break;
                case 151566:
                    ClientModeImpl.this.replyToMessage(message, 151567, 2);
                    break;
                case 151569:
                    ClientModeImpl.this.replyToMessage(message, 151570, 2);
                    break;
                case 151572:
                    ClientModeImpl.this.replyToMessage(message, 151574, 2);
                    break;
                default:
                    if (!ClientModeImpl.this.mHwWifiStateMachineEx.handleHwPrivateMsgInDefaultState(message)) {
                        ClientModeImpl.this.loge("Error! unhandled message" + message);
                        break;
                    }
                    break;
            }
            if (1 == 1) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setupClientMode() {
        Log.i(TAG, "setupClientMode() ifacename = " + this.mInterfaceName);
        setHighPerfModeEnabled(false);
        initializeWpsDetails();
        this.mWifiStateTracker.updateState(0);
        this.mIpClientCallbacks = new IpClientCallbacksImpl();
        this.mFacade.makeIpClient(this.mContext, this.mInterfaceName, this.mIpClientCallbacks);
        if (!this.mIpClientCallbacks.awaitCreation()) {
            loge("Timeout waiting for IpClient");
        }
        setMulticastFilter(true);
        registerForWifiMonitorEvents();
        this.mWifiInjector.getWifiLastResortWatchdog().clearAllFailureCounts();
        setSupplicantLogLevel();
        this.mSupplicantStateTracker.sendMessage(CMD_RESET_SUPPLICANT_STATE);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mWifiInfo.setMacAddress(this.mWifiNative.getMacAddress(this.mInterfaceName));
        sendSupplicantConnectionChangedBroadcast(true);
        this.mWifiNative.setExternalSim(this.mInterfaceName, true);
        setRandomMacOui();
        this.mCountryCode.setReadyForChange(true);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        this.mIsRunning = true;
        updateBatteryWorkSource(null);
        this.mWifiNative.setBluetoothCoexistenceScanMode(this.mInterfaceName, this.mBluetoothConnectionActive);
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        this.mWifiNative.stopFilteringMulticastV4Packets(this.mInterfaceName);
        this.mWifiNative.stopFilteringMulticastV6Packets(this.mInterfaceName);
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get());
        notifyScreenState(this.mScreenOn);
        setPowerSave(true);
        this.mWifiNative.enableStaAutoReconnect(this.mInterfaceName, false);
        this.mWifiNative.setConcurrencyPriority(true);
        this.mWifiNative.mHwWifiNativeEx.initPrivFeatureCapability();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopClientMode() {
        this.mWifiDiagnostics.stopLogging();
        this.mIsRunning = false;
        updateBatteryWorkSource(null);
        if (this.mIpClient != null && this.mIpClient.shutdown()) {
            this.mIpClientCallbacks.awaitShutdown();
        }
        this.mNetworkInfo.setIsAvailable(false);
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        this.mCountryCode.setReadyForChange(false);
        this.mInterfaceName = null;
        sendSupplicantConnectionChangedBroadcast(false);
        this.mWifiConfigManager.removeAllEphemeralOrPasspointConfiguredNetworks();
    }

    /* access modifiers changed from: package-private */
    public void registerConnected() {
        if (this.mLastNetworkId != -1) {
            updateRandomizedMacConfigWhenConnected(this.mInterfaceName, this.mLastBssid);
            this.mWifiConfigManager.updateNetworkAfterConnect(this.mLastNetworkId);
            WifiConfiguration currentNetwork = getCurrentWifiConfiguration();
            if (currentNetwork != null && currentNetwork.isPasspoint()) {
                this.mPasspointManager.onPasspointNetworkConnected(currentNetwork.FQDN);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerDisconnected() {
        int i = this.mLastNetworkId;
        if (i != -1) {
            this.mWifiConfigManager.updateNetworkAfterDisconnect(i);
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        int i = this.mLastNetworkId;
        if (i == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(i);
    }

    private WifiConfiguration getTargetWifiConfiguration() {
        int i = this.mTargetNetworkId;
        if (i == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(i);
    }

    /* access modifiers changed from: package-private */
    public ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String bssid = this.mWifiInfo.getBSSID();
        if (bssid == null) {
            bssid = this.mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache == null) {
            return null;
        }
        return scanDetailCache.getScanResult(bssid);
    }

    /* access modifiers changed from: package-private */
    public String getCurrentBSSID() {
        return this.mLastBssid;
    }

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            Log.i(ClientModeImpl.TAG, "entering ConnectModeState: ifaceName = " + ClientModeImpl.this.mInterfaceName);
            ClientModeImpl.this.mOperationalMode = 1;
            ClientModeImpl.this.setupClientMode();
            if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                ClientModeImpl.this.loge("Failed to remove networks on entering connect mode");
            }
            ClientModeImpl.this.mWifiInfo.reset();
            ClientModeImpl.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            ClientModeImpl.this.mWifiInjector.getWakeupController().reset();
            ClientModeImpl.this.mNetworkInfo.setIsAvailable(true);
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.mNetworkAgent.sendNetworkInfo(ClientModeImpl.this.mNetworkInfo);
            }
            ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            ClientModeImpl.this.mWifiConnectivityManager.setWifiEnabled(true);
            ClientModeImpl.this.mNetworkFactory.setWifiState(true);
            ClientModeImpl.this.mWifiMetrics.setWifiState(2);
            ClientModeImpl.this.mWifiMetrics.logStaEvent(18);
            ClientModeImpl.this.mSarManager.setClientWifiState(3);
            ClientModeImpl.this.mWifiScoreCard.noteSupplicantStateChanged(ClientModeImpl.this.mWifiInfo);
        }

        public void exit() {
            ClientModeImpl.this.mOperationalMode = 4;
            ClientModeImpl.this.mNetworkInfo.setIsAvailable(false);
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.mNetworkAgent.sendNetworkInfo(ClientModeImpl.this.mNetworkInfo);
            }
            ClientModeImpl.this.mWifiConnectivityManager.setWifiEnabled(false);
            ClientModeImpl.this.mNetworkFactory.setWifiState(false);
            ClientModeImpl.this.mWifiMetrics.setWifiState(1);
            ClientModeImpl.this.mWifiMetrics.logStaEvent(19);
            ClientModeImpl.this.mWifiScoreCard.noteWifiDisabled(ClientModeImpl.this.mWifiInfo);
            ClientModeImpl.this.mSarManager.setClientWifiState(1);
            if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                ClientModeImpl.this.loge("Failed to remove networks on exiting connect mode");
            }
            ClientModeImpl.this.mWifiInfo.reset();
            ClientModeImpl.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            ClientModeImpl.this.mWifiScoreCard.noteSupplicantStateChanged(ClientModeImpl.this.mWifiInfo);
            ClientModeImpl.this.stopClientMode();
        }

        /* JADX INFO: Multiple debug info for r3v102 int: [D('networkUpdateResult' com.android.server.wifi.NetworkUpdateResult), D('reasonCode' int)] */
        /* JADX INFO: Multiple debug info for r3v129 int: [D('requestData' com.android.server.wifi.util.TelephonyUtil$SimAuthRequestData), D('netId' int)] */
        /* JADX WARNING: Code restructure failed: missing block: B:173:0x060f, code lost:
            if (r22.this$0.mTargetWifiConfiguration.SSID.equals("\"" + r7 + "\"") != false) goto L_0x0611;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:421:0x0f98, code lost:
            if (r4.isProviderOwnedNetwork(r4.mLastNetworkId, r3) != false) goto L_0x0f9a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:432:0x1008, code lost:
            if (r6.isProviderOwnedNetwork(r6.mLastNetworkId, r5) != false) goto L_0x100a;
         */
        public boolean processMessage(Message message) {
            boolean ok;
            ScanDetailCache scanDetailCache;
            ScanResult scanResult;
            ClientModeImpl clientModeImpl;
            NetworkUpdateResult networkUpdateResult;
            int level2FailureReason;
            WifiConfiguration failureConfig;
            WifiConfiguration rejectConfig;
            boolean handleStatus = true;
            ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
            boolean z = true;
            if (clientModeImpl2.handleWapiFailureEvent(message, clientModeImpl2.mSupplicantStateTracker)) {
                return true;
            }
            int i = -1;
            int i2 = 2;
            boolean z2 = false;
            int res = 0;
            boolean enabled = false;
            switch (message.what) {
                case ClientModeImpl.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                    boolean z3 = false;
                    ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        z3 = true;
                    }
                    clientModeImpl3.mBluetoothConnectionActive = z3;
                    ClientModeImpl.this.mWifiNative.setBluetoothCoexistenceScanMode(ClientModeImpl.this.mInterfaceName, ClientModeImpl.this.mBluetoothConnectionActive);
                    break;
                case ClientModeImpl.CMD_REMOVE_NETWORK /* 131125 */:
                    if (ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, false)) {
                        int netId = message.arg1;
                        if (netId == ClientModeImpl.this.mTargetNetworkId || netId == ClientModeImpl.this.mLastNetworkId) {
                            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.mMessageHandlingStatus = -2;
                        break;
                    }
                case ClientModeImpl.CMD_ENABLE_NETWORK /* 131126 */:
                    boolean disableOthers = message.arg2 == 1;
                    int netId2 = message.arg1;
                    WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(netId2);
                    if (disableOthers) {
                        ok = ClientModeImpl.this.connectToUserSelectNetwork(netId2, message.sendingUid, false);
                        ClientModeImpl.this.saveConnectingNetwork(config, netId2, false);
                    } else if (!ClientModeImpl.this.processConnectModeSetMode(message)) {
                        ok = ClientModeImpl.this.mWifiConfigManager.enableNetwork(netId2, false, message.sendingUid);
                    }
                    if (!ok) {
                        ClientModeImpl.this.mMessageHandlingStatus = -2;
                    }
                    ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                    int i3 = message.what;
                    if (ok) {
                        i = 1;
                    }
                    clientModeImpl4.replyToMessage(message, i3, i);
                    break;
                case ClientModeImpl.CMD_GET_LINK_LAYER_STATS /* 131135 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.getWifiLinkLayerStats());
                    break;
                case ClientModeImpl.CMD_RECONNECT /* 131146 */:
                    ClientModeImpl.this.mWifiConnectivityManager.forceConnectivityScan((WorkSource) message.obj);
                    break;
                case ClientModeImpl.CMD_REASSOCIATE /* 131147 */:
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    clientModeImpl5.mLastConnectAttemptTimestamp = clientModeImpl5.mClock.getWallClockMillis();
                    ClientModeImpl.this.log("ConnectModeState, case CMD_REASSOCIATE, do reassociate");
                    ClientModeImpl.this.mWifiNative.reassociate(ClientModeImpl.this.mInterfaceName);
                    break;
                case ClientModeImpl.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizationsNative(2, true);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizationsNative(2, false);
                        break;
                    }
                case ClientModeImpl.CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.setSuspendOptimizationsNative(4, false);
                        break;
                    } else {
                        ClientModeImpl.this.setSuspendOptimizationsNative(4, true);
                        if (message.arg2 == 1) {
                            ClientModeImpl.this.mSuspendWakeLock.release();
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_ENABLE_TDLS /* 131164 */:
                    if (message.obj != null) {
                        ClientModeImpl.this.mWifiNative.startTdls(ClientModeImpl.this.mInterfaceName, (String) message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_REMOVE_APP_CONFIGURATIONS /* 131169 */:
                    Set<Integer> removedNetworkIds = ClientModeImpl.this.mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    if (removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(ClientModeImpl.this.mLastNetworkId))) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_DISABLE_EPHEMERAL_NETWORK /* 131170 */:
                    WifiConfiguration config2 = ClientModeImpl.this.mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config2 != null && (config2.networkId == ClientModeImpl.this.mTargetNetworkId || config2.networkId == ClientModeImpl.this.mLastNetworkId)) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_GET_MATCHING_CONFIG /* 131171 */:
                    break;
                case ClientModeImpl.CMD_RESET_SIM_NETWORKS /* 131173 */:
                    if (message.arg1 == 1) {
                        Log.i(ClientModeImpl.TAG, "enable EAP-SIM/AKA/AKA' networks since SIM was loaded");
                        ClientModeImpl.this.mWifiConfigManager.mHwWifiConfigManagerEx.enableSimNetworks();
                    }
                    ClientModeImpl.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    if (!(message.arg1 == 1)) {
                        ClientModeImpl.this.mPasspointManager.removeEphemeralProviders();
                        ClientModeImpl.this.mWifiConfigManager.resetSimNetworks();
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_QUERY_OSU_ICON /* 131176 */:
                    ClientModeImpl.this.mPasspointManager.queryPasspointIcon(((Bundle) message.obj).getLong("BSSID"), ((Bundle) message.obj).getString(ClientModeImpl.EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case ClientModeImpl.CMD_MATCH_PROVIDER_NETWORK /* 131177 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, 0);
                    break;
                case ClientModeImpl.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /* 131178 */:
                    Bundle bundle = (Bundle) message.obj;
                    if (bundle != null) {
                        PasspointConfiguration passpointConfig = (PasspointConfiguration) bundle.getParcelable(ClientModeImpl.EXTRA_PASSPOINT_CONFIGURATION);
                        if (!ClientModeImpl.this.mPasspointManager.addOrUpdateProvider(passpointConfig, bundle.getInt(ClientModeImpl.EXTRA_UID), bundle.getString(ClientModeImpl.EXTRA_PACKAGE_NAME))) {
                            ClientModeImpl.this.replyToMessage(message, message.what, -1);
                            break;
                        } else {
                            String fqdn = passpointConfig.getHomeSp().getFqdn();
                            ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                            if (!clientModeImpl6.isProviderOwnedNetwork(clientModeImpl6.mTargetNetworkId, fqdn)) {
                                ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                                break;
                            }
                            ClientModeImpl.this.logd("Disconnect from current network since its provider is updated");
                            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                            ClientModeImpl.this.replyToMessage(message, message.what, 1);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_REMOVE_PASSPOINT_CONFIG /* 131179 */:
                    String fqdn2 = (String) message.obj;
                    if (!ClientModeImpl.this.mPasspointManager.removeProvider(fqdn2)) {
                        ClientModeImpl.this.replyToMessage(message, message.what, -1);
                        break;
                    } else {
                        ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                        if (!clientModeImpl8.isProviderOwnedNetwork(clientModeImpl8.mTargetNetworkId, fqdn2)) {
                            ClientModeImpl clientModeImpl9 = ClientModeImpl.this;
                            break;
                        }
                        ClientModeImpl.this.logd("Disconnect from current network since its provider is removed");
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        ClientModeImpl.this.mWifiConfigManager.removePasspointConfiguredNetwork(fqdn2);
                        ClientModeImpl.this.replyToMessage(message, message.what, 1);
                        break;
                    }
                case ClientModeImpl.CMD_GET_MATCHING_OSU_PROVIDERS /* 131181 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getMatchingOsuProviders((List) message.obj));
                    break;
                case ClientModeImpl.CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS /* 131182 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getMatchingPasspointConfigsForOsuProviders((List) message.obj));
                    break;
                case ClientModeImpl.CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES /* 131184 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getWifiConfigsForPasspointProfiles((List) message.obj));
                    break;
                case ClientModeImpl.CMD_TARGET_BSSID /* 131213 */:
                    if (message.obj != null) {
                        ClientModeImpl.this.mTargetRoamBSSID = (String) message.obj;
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_CONNECT /* 131215 */:
                    if (!ClientModeImpl.this.isHiLinkActive()) {
                        if ((ClientModeImpl.this.getWifiMode() & 2) == 0) {
                            Bundle bundleInfo = (Bundle) message.obj;
                            if (bundleInfo != null) {
                                boolean connectFromUser = bundleInfo.getBoolean(ClientModeImpl.CONNECT_FROM_USER);
                                Log.i(ClientModeImpl.TAG, "connectFromUser =" + connectFromUser);
                                if (!ClientModeImpl.this.attemptAutoConnect() && !connectFromUser) {
                                    ClientModeImpl.this.logd("SupplicantState is TransientState, refuse auto connect");
                                    break;
                                } else {
                                    int netId3 = message.arg1;
                                    int uid = message.arg2;
                                    if (uid >= 10000 && uid <= 19999 && ClientModeImpl.this.mHwWifiStateMachineEx.isNeedIgnoreConnect()) {
                                        Log.i(ClientModeImpl.TAG, "third app in cast scene, ingore this connect");
                                        break;
                                    } else {
                                        String bssid = bundleInfo.getString(ClientModeImpl.BSSID_TO_CONNECT);
                                        if (!ClientModeImpl.this.hasConnectionRequests()) {
                                            if (ClientModeImpl.this.mNetworkAgent != null) {
                                                if (!connectFromUser && !ClientModeImpl.this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                                                    ClientModeImpl.this.loge("CMD_START_CONNECT but no requests and connected, but app does not have sufficient permissions, bailing");
                                                    if ((!WifiCommonUtils.IS_TV || !ClientModeImpl.FACTORY_MODE.equals(SystemProperties.get(ClientModeImpl.RO_RUN_MODE, ClientModeImpl.NORMAL_MODE))) && !SystemProperties.getBoolean(ClientModeImpl.SUPPORT_WIFI_COEX_ETH, false)) {
                                                        ClientModeImpl.this.loge("wifi and eth coex has no permission too");
                                                        break;
                                                    }
                                                }
                                            } else {
                                                ClientModeImpl.this.loge("CMD_START_CONNECT but no requests and not connected, bailing");
                                                if ((!WifiCommonUtils.IS_TV || !ClientModeImpl.FACTORY_MODE.equals(SystemProperties.get(ClientModeImpl.RO_RUN_MODE, ClientModeImpl.NORMAL_MODE))) && !SystemProperties.getBoolean(ClientModeImpl.SUPPORT_WIFI_COEX_ETH, false)) {
                                                    ClientModeImpl.this.loge("wifi and eth coex has no permission");
                                                    break;
                                                }
                                            }
                                        }
                                        WifiConfiguration config3 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId3);
                                        ClientModeImpl.this.logd("CMD_START_CONNECT sup state " + ClientModeImpl.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + ClientModeImpl.this.getCurrentState().getName() + " nid=" + Integer.toString(netId3) + " roam=" + Boolean.toString(ClientModeImpl.this.mIsAutoRoaming));
                                        if (config3 != null) {
                                            if (connectFromUser && bssid != null && bssid.equals("any") && config3.BSSID != null && !isApInScanList(config3.BSSID)) {
                                                ClientModeImpl.this.logd("bssid not match, connect with ssid");
                                                config3.BSSID = null;
                                            }
                                            if (!WifiCommonUtils.doesNotWifiConnectRejectByCust(config3.getNetworkSelectionStatus(), config3.SSID, ClientModeImpl.this.mContext)) {
                                                if (!HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config3, false)) {
                                                    if (!TelephonyUtil.isSimPresent(ClientModeImpl.this.mWifiInjector.mSubscriptionManager)) {
                                                        ClientModeImpl.this.handleSimAbsent(config3);
                                                    }
                                                    if (ClientModeImpl.this.isEnterpriseHotspot(config3)) {
                                                        ClientModeImpl.this.logd(StringUtilEx.safeDisplaySsid(config3.SSID) + "is enterprise hotspot ");
                                                        ClientModeImpl.this.mTargetRoamBSSID = "any";
                                                    }
                                                    ClientModeImpl.this.mWifiScoreCard.noteConnectionAttempt(ClientModeImpl.this.mWifiInfo);
                                                    ClientModeImpl.this.mTargetNetworkId = netId3;
                                                    ClientModeImpl.this.setTargetBssid(config3, bssid);
                                                    ClientModeImpl clientModeImpl10 = ClientModeImpl.this;
                                                    clientModeImpl10.reportConnectionAttemptStart(config3, clientModeImpl10.mTargetRoamBSSID, 5);
                                                    ClientModeImpl.this.isRandomSelfCure = false;
                                                    ClientModeImpl.this.isUseRandomMac = true;
                                                    if (config3.macRandomizationSetting != 1 || !ClientModeImpl.this.mConnectedMacRandomzationSupported) {
                                                        ClientModeImpl.this.isUseRandomMac = false;
                                                        ClientModeImpl.this.setCurrentMacToFactoryMac(config3);
                                                    } else {
                                                        ClientModeImpl.this.configureRandomizedMacAddress(config3);
                                                    }
                                                    Bundle data = new Bundle();
                                                    data.putBoolean(ClientModeImpl.KEY_IS_RANDOM_MAC, ClientModeImpl.this.isUseRandomMac);
                                                    data.putBoolean(ClientModeImpl.KEY_IS_RANDOM_SELF_CURE, ClientModeImpl.this.isRandomSelfCure);
                                                    ClientModeImpl.this.mHwWifiCHRService.uploadDFTEvent(18, data);
                                                    String currentMacAddress = ClientModeImpl.this.mWifiNative.getMacAddress(ClientModeImpl.this.mInterfaceName);
                                                    ClientModeImpl.this.mWifiInfo.setMacAddress(currentMacAddress);
                                                    Log.i(ClientModeImpl.TAG, "Connecting with " + StringUtilEx.safeDisplayBssid(currentMacAddress) + " as the mac address");
                                                    if (config3.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config3.enterpriseConfig.getEapMethod()) && ClientModeImpl.this.mWifiInjector.getCarrierNetworkConfig().isCarrierEncryptionInfoAvailable() && TextUtils.isEmpty(config3.enterpriseConfig.getAnonymousIdentity())) {
                                                        config3.enterpriseConfig.setAnonymousIdentity(TelephonyUtil.getAnonymousIdentityWith3GppRealm(ClientModeImpl.this.getTelephonyManager()));
                                                    }
                                                    ClientModeImpl.this.saveConnectingNetwork(config3, netId3, true);
                                                    if (!ClientModeImpl.this.mWifiNative.connectToNetwork(ClientModeImpl.this.mInterfaceName, config3)) {
                                                        ClientModeImpl.this.loge("CMD_START_CONNECT Failed to start connection to network " + config3);
                                                        ClientModeImpl.this.reportConnectionAttemptEnd(5, 1, 0);
                                                        ClientModeImpl.this.replyToMessage(message, 151554, 0);
                                                        break;
                                                    } else {
                                                        ClientModeImpl.this.mWifiMetrics.logStaEvent(11, config3);
                                                        ClientModeImpl clientModeImpl11 = ClientModeImpl.this;
                                                        clientModeImpl11.mLastConnectAttemptTimestamp = clientModeImpl11.mClock.getWallClockMillis();
                                                        ClientModeImpl.this.mTargetWifiConfiguration = config3;
                                                        ClientModeImpl.this.mIsAutoRoaming = false;
                                                        if (ClientModeImpl.this.getCurrentState() != ClientModeImpl.this.mDisconnectedState) {
                                                            ClientModeImpl clientModeImpl12 = ClientModeImpl.this;
                                                            clientModeImpl12.transitionTo(clientModeImpl12.mDisconnectingState);
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    Log.w(ClientModeImpl.TAG, "CMD_START_CONNECT: MDM deny connect to restricted network!");
                                                    break;
                                                }
                                            } else {
                                                Log.i(ClientModeImpl.TAG, "break CMD_START_CONNECT with WifiConnectRejectByCust");
                                                break;
                                            }
                                        } else {
                                            ClientModeImpl.this.loge("CMD_START_CONNECT and no config, bail out...");
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.i(ClientModeImpl.TAG, "wifi mode is set, refuse auto connect");
                            break;
                        }
                    } else {
                        Log.i(ClientModeImpl.TAG, "HiLink is active, refuse auto connect");
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_ROAM /* 131217 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /* 131219 */:
                    String someBssid = (String) message.obj;
                    if (!(someBssid == null || (scanDetailCache = ClientModeImpl.this.mWifiConfigManager.getScanDetailCacheForNetwork(ClientModeImpl.this.mTargetNetworkId)) == null)) {
                        ClientModeImpl.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                    }
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_REMOVE_USER_CONFIGURATIONS /* 131224 */:
                    Set<Integer> removedNetworkIds2 = ClientModeImpl.this.mWifiConfigManager.removeNetworksForUser(Integer.valueOf(message.arg1).intValue());
                    if (removedNetworkIds2.contains(Integer.valueOf(ClientModeImpl.this.mTargetNetworkId)) || removedNetworkIds2.contains(Integer.valueOf(ClientModeImpl.this.mLastNetworkId))) {
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    }
                case ClientModeImpl.CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                    int slot = message.arg1;
                    int ret = ClientModeImpl.this.stopWifiIPPacketOffload(slot);
                    if (ClientModeImpl.this.mNetworkAgent != null) {
                        ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /* 131238 */:
                    WifiConnectivityManager wifiConnectivityManager = ClientModeImpl.this.mWifiConnectivityManager;
                    if (message.arg1 == 1) {
                        z2 = true;
                    }
                    wifiConnectivityManager.enable(z2);
                    break;
                case ClientModeImpl.CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS /* 131240 */:
                    ClientModeImpl.this.replyToMessage(message, message.what, ClientModeImpl.this.mPasspointManager.getAllMatchingFqdnsForScanResults((List) message.obj));
                    break;
                case ClientModeImpl.CMD_CONFIG_ND_OFFLOAD /* 131276 */:
                    if (message.arg1 > 0) {
                        enabled = true;
                    }
                    ClientModeImpl.this.mWifiNative.configureNeighborDiscoveryOffload(ClientModeImpl.this.mInterfaceName, enabled);
                    break;
                case ClientModeImpl.CMD_START_SUBSCRIPTION_PROVISIONING /* 131326 */:
                    if (ClientModeImpl.this.mPasspointManager.startSubscriptionProvisioning(message.arg1, message.getData().getParcelable(ClientModeImpl.EXTRA_OSU_PROVIDER), (IProvisioningCallback) message.obj)) {
                        res = 1;
                    }
                    ClientModeImpl.this.replyToMessage(message, message.what, res);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                    if (message.arg1 != 1) {
                        ClientModeImpl.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do reconnect");
                        ClientModeImpl.this.mWifiNative.reconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl.this.mTemporarilyDisconnectWifi = false;
                        break;
                    } else {
                        ClientModeImpl.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 5);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl.this.mTemporarilyDisconnectWifi = true;
                        break;
                    }
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Network connection established");
                    }
                    ClientModeImpl.this.mLastNetworkId = message.arg1;
                    ClientModeImpl.this.mWifiConfigManager.clearRecentFailureReason(ClientModeImpl.this.mLastNetworkId);
                    if (ClientModeImpl.this.mHwWifiCHRService != null) {
                        ClientModeImpl.this.mHwWifiCHRService.updateWIFIConfiguraionByConfig(ClientModeImpl.this.getCurrentWifiConfiguration());
                    }
                    ClientModeImpl.this.mLastBssid = (String) message.obj;
                    if (ClientModeImpl.this.mLastNetworkId == -1 && (networkUpdateResult = (clientModeImpl = ClientModeImpl.this).saveWpsOkcConfiguration(clientModeImpl.mLastNetworkId, ClientModeImpl.this.mLastBssid)) != null) {
                        ClientModeImpl.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                    }
                    int reasonCode = message.arg2;
                    WifiConfiguration config4 = ClientModeImpl.this.getCurrentWifiConfiguration();
                    ClientModeImpl.this.setLastConnectConfig(config4);
                    ClientModeImpl.this.setSupportedWifiCategory();
                    if (config4 == null) {
                        ClientModeImpl.this.logw("Connected to unknown networkId " + ClientModeImpl.this.mLastNetworkId + ", disconnecting...");
                        ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                        break;
                    } else {
                        ClientModeImpl.this.mWifiInfo.setBSSID(ClientModeImpl.this.mLastBssid);
                        ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                        ClientModeImpl.this.mWifiInfo.setMacAddress(ClientModeImpl.this.mWifiNative.getMacAddress(ClientModeImpl.this.mInterfaceName));
                        ScanDetailCache scanDetailCache2 = ClientModeImpl.this.mWifiConfigManager.getScanDetailCacheForNetwork(config4.networkId);
                        if (!(scanDetailCache2 == null || ClientModeImpl.this.mLastBssid == null || (scanResult = scanDetailCache2.getScanResult(ClientModeImpl.this.mLastBssid)) == null)) {
                            ClientModeImpl.this.mWifiInfo.setFrequency(scanResult.frequency);
                        }
                        ClientModeImpl.this.mWifiConnectivityManager.trackBssid(ClientModeImpl.this.mLastBssid, true, reasonCode);
                        ClientModeImpl.this.dataProvider.e(54, "{RT:6,SPEED:" + ClientModeImpl.this.mWifiInfo.getLinkSpeed() + "}");
                        if (!ClientModeImpl.this.mHwWifiStateMachineEx.isTempCreated(config4) && config4.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config4.enterpriseConfig.getEapMethod()) && !TelephonyUtil.isAnonymousAtRealmIdentity(config4.enterpriseConfig.getAnonymousIdentity())) {
                            String anonymousIdentity = ClientModeImpl.this.mWifiNative.getEapAnonymousIdentity(ClientModeImpl.this.mInterfaceName);
                            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                ClientModeImpl.this.log("EAP Pseudonym: " + anonymousIdentity);
                            }
                            config4.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                            ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config4, 1010);
                        }
                        ClientModeImpl clientModeImpl13 = ClientModeImpl.this;
                        clientModeImpl13.sendNetworkStateChangeBroadcast(clientModeImpl13.mLastBssid);
                        ClientModeImpl.this.log("ConnectModeState, case WifiMonitor.NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                        ClientModeImpl clientModeImpl14 = ClientModeImpl.this;
                        clientModeImpl14.transitionTo(clientModeImpl14.mObtainingIpState);
                        break;
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("ConnectModeState: Network connection lost ");
                    }
                    if (ClientModeImpl.this.disassociatedReason(message.arg2)) {
                        Log.i(ClientModeImpl.TAG, "DISABLED_DISASSOC_REASON for network " + ClientModeImpl.this.mTargetNetworkId + " is " + message.arg2);
                        ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetNetworkId, 17);
                    }
                    ClientModeImpl.this.handleNetworkDisconnect();
                    ClientModeImpl clientModeImpl15 = ClientModeImpl.this;
                    clientModeImpl15.transitionTo(clientModeImpl15.mDisconnectedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    SupplicantState state = ClientModeImpl.this.handleSupplicantStateChange(message);
                    if (!ClientModeImpl.CHIPSET_TYPE.contains(ClientModeImpl.CHIPSET_BCM) || state != SupplicantState.INTERFACE_DISABLED) {
                        if (state == SupplicantState.DISCONNECTED && ClientModeImpl.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                                ClientModeImpl.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                            }
                            ClientModeImpl.this.handleNetworkDisconnect();
                            ClientModeImpl clientModeImpl16 = ClientModeImpl.this;
                            clientModeImpl16.transitionTo(clientModeImpl16.mDisconnectedState);
                        }
                        if (state == SupplicantState.COMPLETED) {
                            if (ClientModeImpl.this.mIpClient != null) {
                                ClientModeImpl.this.mIpClient.confirmConfiguration();
                            }
                            ClientModeImpl.this.mWifiScoreReport.noteIpCheck();
                        }
                        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                        if (stateChangeResult != null) {
                            int disconnectId = stateChangeResult.networkId;
                            WifiConfiguration disconnectConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(disconnectId);
                            if (ClientModeImpl.this.mTargetNetworkId == disconnectId && disconnectConfig != null && disconnectConfig.getNetworkSelectionStatus().isNetworkEnabled() && disconnectConfig.getNetworkSelectionStatus().getDisableReasonCounter(3) > 0 && ClientModeImpl.this.mClock.getElapsedSinceBootMillis() - ClientModeImpl.this.mLastAuthFailureTimestamp < ClientModeImpl.LAST_AUTH_FAILURE_GAP && !ClientModeImpl.this.isConnected() && state == SupplicantState.DISCONNECTED) {
                                Log.i(ClientModeImpl.TAG, "start an immediate connection for network " + disconnectId);
                                ClientModeImpl.this.startConnectToNetwork(disconnectId, 1010, "any");
                                break;
                            }
                        }
                    } else {
                        return false;
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(2);
                    ClientModeImpl.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    int disableReason = 3;
                    int reasonCode2 = message.arg1;
                    ClientModeImpl clientModeImpl17 = ClientModeImpl.this;
                    if (clientModeImpl17.isPermanentWrongPasswordFailure(clientModeImpl17.mTargetNetworkId, reasonCode2)) {
                        disableReason = 13;
                    } else if (reasonCode2 == 3) {
                        int errorCode = message.arg2;
                        ClientModeImpl clientModeImpl18 = ClientModeImpl.this;
                        clientModeImpl18.handleEapAuthFailure(clientModeImpl18.mTargetNetworkId, errorCode);
                        if (errorCode == 1031) {
                            disableReason = 14;
                        }
                    }
                    Log.i(ClientModeImpl.TAG, "Authentication Failure event: reasonCode=" + reasonCode2);
                    ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetNetworkId, disableReason);
                    ClientModeImpl.this.mWifiConfigManager.clearRecentFailureReason(ClientModeImpl.this.mTargetNetworkId);
                    ClientModeImpl clientModeImpl19 = ClientModeImpl.this;
                    clientModeImpl19.mLastAuthFailureTimestamp = clientModeImpl19.mClock.getElapsedSinceBootMillis();
                    ClientModeImpl clientModeImpl20 = ClientModeImpl.this;
                    clientModeImpl20.notifyWifiConnFailedInfo(clientModeImpl20.mTargetNetworkId, null, -200, 3, ClientModeImpl.this.mWifiConnectivityManager);
                    if (reasonCode2 == 0) {
                        level2FailureReason = 1;
                    } else if (reasonCode2 == 1) {
                        level2FailureReason = 2;
                    } else if (reasonCode2 == 2) {
                        level2FailureReason = 3;
                    } else if (reasonCode2 != 3) {
                        level2FailureReason = 0;
                    } else {
                        level2FailureReason = 4;
                    }
                    ClientModeImpl.this.reportConnectionAttemptEnd(3, 1, level2FailureReason);
                    if (ClientModeImpl.this.mCust != null && ClientModeImpl.this.mCust.isShowWifiAuthenticationFailurerNotification()) {
                        ClientModeImpl.this.mCust.handleWifiAuthenticationFailureEvent(ClientModeImpl.this.mContext, ClientModeImpl.this);
                    }
                    if (reasonCode2 != 2) {
                        ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), ClientModeImpl.this.mTargetRoamBSSID, 2);
                    }
                    if (ClientModeImpl.this.mScreenOn && reasonCode2 != 2 && (failureConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mTargetNetworkId)) != null && failureConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
                        ClientModeImpl.this.mWifiConnectivityManager.forceConnectivityScan(ClientModeImpl.WIFI_WORK_SOURCE);
                        break;
                    }
                case WifiMonitor.SUP_REQUEST_IDENTITY /* 147471 */:
                    int netId4 = message.arg2;
                    boolean identitySent = false;
                    if (ClientModeImpl.this.mTargetWifiConfiguration != null && ClientModeImpl.this.mTargetWifiConfiguration.networkId == netId4 && TelephonyUtil.isSimConfig(ClientModeImpl.this.mTargetWifiConfiguration)) {
                        Pair<String, String> identityPair = TelephonyUtil.getSimIdentity(ClientModeImpl.this.getTelephonyManager(), new TelephonyUtil(), ClientModeImpl.this.mTargetWifiConfiguration, ClientModeImpl.this.mWifiInjector.getCarrierNetworkConfig());
                        Log.i(ClientModeImpl.TAG, "SUP_REQUEST_IDENTITY");
                        if (identityPair == null || identityPair.first == null) {
                            Log.e(ClientModeImpl.TAG, "Unable to retrieve identity from Telephony");
                        } else {
                            identitySent = ClientModeImpl.this.mWifiNative.simIdentityResponse(ClientModeImpl.this.mInterfaceName, netId4, (String) identityPair.first, (String) identityPair.second);
                        }
                    }
                    if (!identitySent) {
                        String ssid = (String) message.obj;
                        if (!(ClientModeImpl.this.mTargetWifiConfiguration == null || ssid == null || ClientModeImpl.this.mTargetWifiConfiguration.SSID == null)) {
                            if (!ClientModeImpl.this.mTargetWifiConfiguration.SSID.equals(ssid)) {
                                break;
                            }
                            if (ClientModeImpl.this.mTrackEapAuthFailCount >= 3) {
                                Log.i(ClientModeImpl.TAG, "updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                                ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetWifiConfiguration.networkId, 9);
                                ClientModeImpl.this.mTrackEapAuthFailCount = 0;
                            } else if (!ClientModeImpl.this.mIsImsiAvailable) {
                                Log.i(ClientModeImpl.TAG, "sim is not available,updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                                ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetWifiConfiguration.networkId, 9);
                                ClientModeImpl.this.mTrackEapAuthFailCount = 0;
                            } else {
                                ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetWifiConfiguration.networkId, 0);
                                ClientModeImpl.access$6908(ClientModeImpl.this);
                                Log.i(ClientModeImpl.TAG, "sim is not ready and retry mTrackEapAuthFailCount " + ClientModeImpl.this.mTrackEapAuthFailCount);
                            }
                        }
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 2);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /* 147472 */:
                    ClientModeImpl.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    TelephonyUtil.SimAuthRequestData requestData = (TelephonyUtil.SimAuthRequestData) message.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                ClientModeImpl.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        } else {
                            ClientModeImpl.this.handleGsmAuthRequest(requestData);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("Invalid SIM auth request");
                        break;
                    }
                case 147499:
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(1);
                    ClientModeImpl.this.mDidBlackListBSSID = false;
                    String bssid2 = (String) message.obj;
                    boolean timedOut = message.arg1 > 0;
                    int reasonCode3 = message.arg2;
                    Log.i(ClientModeImpl.TAG, "Association Rejection event: bssid=" + StringUtilEx.safeDisplayBssid(bssid2) + " reason code=" + reasonCode3 + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid2 == null || TextUtils.isEmpty(bssid2)) {
                        bssid2 = ClientModeImpl.this.mTargetRoamBSSID;
                    }
                    if (bssid2 != null) {
                        ClientModeImpl clientModeImpl21 = ClientModeImpl.this;
                        clientModeImpl21.mDidBlackListBSSID = clientModeImpl21.mWifiConnectivityManager.trackBssid(bssid2, false, reasonCode3);
                    }
                    ClientModeImpl clientModeImpl22 = ClientModeImpl.this;
                    clientModeImpl22.updateRandomizedMacConfigWhenDisconnected(clientModeImpl22.mTargetNetworkId, reasonCode3);
                    ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(ClientModeImpl.this.mTargetNetworkId, 2);
                    ClientModeImpl.this.mWifiConfigManager.setRecentFailureAssociationStatus(ClientModeImpl.this.mTargetNetworkId, reasonCode3);
                    ClientModeImpl clientModeImpl23 = ClientModeImpl.this;
                    clientModeImpl23.notifyWifiConnFailedInfo(clientModeImpl23.mTargetNetworkId, bssid2, -200, 2, ClientModeImpl.this.mWifiConnectivityManager);
                    ClientModeImpl.this.recordAssociationRejectStatusCode(message.arg2);
                    ClientModeImpl.this.mSupplicantStateTracker.sendMessage(147499);
                    ClientModeImpl clientModeImpl24 = ClientModeImpl.this;
                    if (timedOut) {
                        i2 = 11;
                    }
                    clientModeImpl24.reportConnectionAttemptEnd(i2, 1, 0);
                    ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), bssid2, 1);
                    if (ClientModeImpl.this.mScreenOn && (rejectConfig = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mTargetNetworkId)) != null && rejectConfig.getNetworkSelectionStatus().isNetworkEnabled()) {
                        ClientModeImpl.this.mWifiConnectivityManager.forceConnectivityScan(ClientModeImpl.WIFI_WORK_SOURCE);
                        break;
                    }
                case WifiMonitor.ANQP_DONE_EVENT /* 147500 */:
                    ClientModeImpl.this.mPasspointManager.notifyANQPDone((AnqpEvent) message.obj);
                    break;
                case WifiMonitor.AUTHENTICATION_TIMEOUT_EVENT /* 147501 */:
                    if (ClientModeImpl.this.mWifiInfo != null && ClientModeImpl.this.mWifiInfo.getSupplicantState() == SupplicantState.ASSOCIATED) {
                        ClientModeImpl.this.loge("auth timeout in associated state, handle as associate reject event");
                        ClientModeImpl.this.mSupplicantStateTracker.sendMessage(147499);
                        break;
                    }
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /* 147509 */:
                    ClientModeImpl.this.mPasspointManager.notifyIconDone((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /* 147517 */:
                    ClientModeImpl.this.mPasspointManager.receivedWnmFrame((WnmData) message.obj);
                    break;
                case WifiMonitor.WPS_START_OKC_EVENT /* 147656 */:
                    ClientModeImpl.this.sendWpsOkcStartedBroadcast();
                    if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                        ClientModeImpl.this.loge("Failed to remove networks before HiLink OKC");
                    }
                    String hilinkBssid = (String) message.obj;
                    if (!TextUtils.isEmpty(hilinkBssid)) {
                        ClientModeImpl.this.mWifiNative.startWpsPbc(ClientModeImpl.this.mInterfaceName, hilinkBssid);
                        break;
                    }
                    break;
                case WifiMonitor.WPA3_CONNECT_FAIL_EVENT /* 147666 */:
                    ClientModeImpl.this.handleWpa3ConnectFailReport((String) message.obj);
                    break;
                case WifiMonitor.EAP_ERRORCODE_REPORT_EVENT /* 147956 */:
                    if (ClientModeImpl.this.mTargetWifiConfiguration != null && ClientModeImpl.this.mTargetWifiConfiguration.networkId == message.arg1) {
                        ClientModeImpl.this.handleEapErrorcodeReport(message.arg1, (String) message.obj, message.arg2);
                        break;
                    }
                case 151553:
                    int netId5 = message.arg1;
                    WifiConfiguration config5 = (WifiConfiguration) message.obj;
                    String strConfigCRC = "*";
                    boolean hasCredentialChanged = false;
                    boolean forceReconnect = false;
                    if (config5 != null) {
                        ClientModeImpl.this.mIsFirstConnect = true;
                        strConfigCRC = config5.preSharedKey;
                        forceReconnect = ClientModeImpl.this.mHwWifiStateMachineEx.checkForceReconnect(ClientModeImpl.this.mNetworkInfo, config5, false);
                        NetworkUpdateResult result = ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config5, message.sendingUid);
                        if (!result.isSuccess()) {
                            ClientModeImpl.this.loge("CONNECT_NETWORK adding/updating config=" + config5 + " failed");
                            ClientModeImpl.this.mMessageHandlingStatus = -2;
                            ClientModeImpl.this.replyToMessage(message, 151554, 0);
                            break;
                        } else {
                            netId5 = result.getNetworkId();
                            hasCredentialChanged = result.hasCredentialChanged();
                        }
                    } else {
                        ClientModeImpl.this.mIsFirstConnect = false;
                    }
                    if (ClientModeImpl.this.mHwWifiCHRService != null) {
                        WifiConfiguration newWifiConfig = new WifiConfiguration(config5);
                        newWifiConfig.preSharedKey = strConfigCRC;
                        ClientModeImpl.this.mHwWifiCHRService.connectFromUserByConfig(newWifiConfig);
                    }
                    ClientModeImpl.this.saveConnectingNetwork(config5, netId5, false);
                    ClientModeImpl.this.exitWifiSelfCure(151553, -1);
                    if (!(ClientModeImpl.this.mLastNetworkId == -1 || ClientModeImpl.this.mLastNetworkId != netId5 || ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mDisconnectedState)) {
                        ClientModeImpl.this.logd("disconnect old");
                        forceReconnect = true;
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl25 = ClientModeImpl.this;
                        clientModeImpl25.transitionTo(clientModeImpl25.mDisconnectingState);
                    }
                    ClientModeImpl clientModeImpl26 = ClientModeImpl.this;
                    int i4 = message.sendingUid;
                    if (!hasCredentialChanged && !forceReconnect) {
                        z = false;
                    }
                    if (clientModeImpl26.connectToUserSelectNetwork(netId5, i4, z)) {
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(13, config5);
                        ClientModeImpl.this.broadcastWifiCredentialChanged(0, config5);
                        ClientModeImpl.this.replyToMessage(message, 151555);
                        break;
                    } else {
                        ClientModeImpl.this.mMessageHandlingStatus = -2;
                        if (-1 != netId5 && config5 == null) {
                            config5 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(netId5);
                        }
                        if (config5 != null && HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config5, false)) {
                            ClientModeImpl.this.replyToMessage(message, 151554, 1000);
                            break;
                        } else {
                            ClientModeImpl.this.replyToMessage(message, 151554, 9);
                            break;
                        }
                    }
                    break;
                case 151556:
                    if (ClientModeImpl.this.deleteNetworkConfigAndSendReply(message, true)) {
                        int netId6 = message.arg1;
                        ClientModeImpl.this.exitWifiSelfCure(151556, netId6);
                        if (netId6 == ClientModeImpl.this.mTargetNetworkId || netId6 == ClientModeImpl.this.mLastNetworkId) {
                            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                            break;
                        }
                    }
                    break;
                case 151559:
                    NetworkUpdateResult result2 = ClientModeImpl.this.saveNetworkConfigAndSendReply(message);
                    int netId7 = result2.getNetworkId();
                    if (result2.isSuccess() && ClientModeImpl.this.mWifiInfo.getNetworkId() == netId7) {
                        if (!result2.hasCredentialChanged()) {
                            if (result2.hasProxyChanged() && ClientModeImpl.this.mIpClient != null) {
                                ClientModeImpl.this.log("Reconfiguring proxy on connection");
                                ClientModeImpl.this.mIpClient.setHttpProxy(ClientModeImpl.this.getProxyProperties());
                            }
                            if (result2.hasIpChanged()) {
                                Log.i(ClientModeImpl.TAG, "Reconfiguring IP if current state == mConnectedState");
                                if (!ClientModeImpl.this.isConnected()) {
                                    Log.i(ClientModeImpl.TAG, "ignore reconfiguring IP because current state != mConnectedState");
                                    break;
                                } else {
                                    ClientModeImpl.this.log("Reconfiguring IP on connection");
                                    ClientModeImpl clientModeImpl27 = ClientModeImpl.this;
                                    clientModeImpl27.transitionTo(clientModeImpl27.mObtainingIpState);
                                    break;
                                }
                            }
                        } else {
                            ClientModeImpl.this.logi("SAVE_NETWORK credential changed for ssid =" + StringUtilEx.safeDisplaySsid(((WifiConfiguration) message.obj).getPrintableSsid()) + ", Reconnecting.");
                            ClientModeImpl.this.startConnectToNetwork(netId7, message.sendingUid, "any");
                            break;
                        }
                    }
                    break;
                case 151562:
                    WpsInfo wpsInfo = (WpsInfo) message.obj;
                    if (wpsInfo != null) {
                        WpsResult wpsResult = new WpsResult();
                        if (!ClientModeImpl.this.mWifiNative.removeAllNetworks(ClientModeImpl.this.mInterfaceName)) {
                            ClientModeImpl.this.loge("Failed to remove networks before WPS");
                        }
                        int i5 = wpsInfo.setup;
                        if (i5 == 0) {
                            ClientModeImpl.this.clearRandomMacOui();
                            ClientModeImpl clientModeImpl28 = ClientModeImpl.this;
                            clientModeImpl28.mIsRandomMacCleared = true;
                            if (clientModeImpl28.mWifiNative.startWpsPbc(ClientModeImpl.this.mInterfaceName, wpsInfo.BSSID)) {
                                wpsResult.status = WpsResult.Status.SUCCESS;
                            } else {
                                ClientModeImpl.this.loge("Failed to start WPS push button configuration");
                                wpsResult.status = WpsResult.Status.FAILURE;
                            }
                        } else if (i5 == 1) {
                            wpsResult.pin = ClientModeImpl.this.mWifiNative.startWpsPinDisplay(ClientModeImpl.this.mInterfaceName, wpsInfo.BSSID);
                            if (!TextUtils.isEmpty(wpsResult.pin)) {
                                wpsResult.status = WpsResult.Status.SUCCESS;
                                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_WPS_PIN_RETRY, wpsResult);
                            } else {
                                ClientModeImpl.this.loge("Failed to start WPS pin method configuration");
                                wpsResult.status = WpsResult.Status.FAILURE;
                            }
                        } else if (i5 != 2) {
                            wpsResult = new WpsResult(WpsResult.Status.FAILURE);
                            ClientModeImpl.this.loge("Invalid setup for WPS");
                        } else if (ClientModeImpl.this.mWifiNative.startWpsRegistrar(ClientModeImpl.this.mInterfaceName, wpsInfo.BSSID, wpsInfo.pin)) {
                            wpsResult.status = WpsResult.Status.SUCCESS;
                        } else {
                            ClientModeImpl.this.loge("Failed to start WPS push button configuration");
                            wpsResult.status = WpsResult.Status.FAILURE;
                        }
                        if (wpsResult.status != WpsResult.Status.SUCCESS) {
                            ClientModeImpl.this.loge("Failed to start WPS");
                            ClientModeImpl.this.replyToMessage(message, 151564, 0);
                            break;
                        } else {
                            ClientModeImpl.this.replyToMessage(message, 151563, wpsResult);
                            ClientModeImpl clientModeImpl29 = ClientModeImpl.this;
                            clientModeImpl29.transitionTo(clientModeImpl29.mWpsRunningState);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("Cannot start WPS with null WpsInfo object");
                        ClientModeImpl.this.replyToMessage(message, 151564, 0);
                        break;
                    }
                case 151569:
                    int netId8 = message.arg1;
                    if (!ClientModeImpl.this.mWifiConfigManager.disableNetwork(netId8, message.sendingUid)) {
                        ClientModeImpl.this.loge("Failed to disable network");
                        ClientModeImpl.this.mMessageHandlingStatus = -2;
                        ClientModeImpl.this.replyToMessage(message, 151570, 0);
                        break;
                    } else {
                        ClientModeImpl.this.replyToMessage(message, 151571);
                        if (netId8 == ClientModeImpl.this.mTargetNetworkId || netId8 == ClientModeImpl.this.mLastNetworkId) {
                            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_DISCONNECT);
                            break;
                        }
                    }
                default:
                    if (!ClientModeImpl.this.mHwWifiStateMachineEx.handleHwPrivateMsgInConnectModeState(message)) {
                        handleStatus = false;
                        break;
                    }
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        private boolean isApInScanList(String bssid) {
            ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
            if (scanProxy == null || bssid == null) {
                return false;
            }
            synchronized (scanProxy) {
                List<ScanResult> cachedScanResults = scanProxy.getScanResults();
                if (cachedScanResults != null) {
                    for (ScanResult result : cachedScanResults) {
                        if (!(result == null || result.BSSID == null || !bssid.equals(result.BSSID))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

    class WpsRunningState extends State {
        private Message mSourceMessage;

        WpsRunningState() {
        }

        public void enter() {
            if (ClientModeImpl.DBG) {
                ClientModeImpl.this.log(getName());
            }
            this.mSourceMessage = Message.obtain(ClientModeImpl.this.getCurrentMessage());
        }

        public boolean processMessage(Message message) {
            ClientModeImpl.this.logStateAndMessage(message, this);
            switch (message.what) {
                case ClientModeImpl.CMD_ENABLE_NETWORK /* 131126 */:
                case ClientModeImpl.CMD_RECONNECT /* 131146 */:
                case 151553:
                    ClientModeImpl.this.log(" Ignore CMD_RECONNECT request because wps is running");
                    return true;
                case ClientModeImpl.CMD_SET_OPERATIONAL_MODE /* 131144 */:
                    ClientModeImpl.this.mOperationalMode = message.arg1;
                    if (ClientModeImpl.this.mOperationalMode == 3) {
                        ClientModeImpl clientModeImpl = ClientModeImpl.this;
                        clientModeImpl.transitionTo(clientModeImpl.mDefaultState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_REASSOCIATE /* 131147 */:
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_START_CONNECT /* 131215 */:
                case ClientModeImpl.CMD_START_ROAM /* 131217 */:
                    ((ClientModeImpl) ClientModeImpl.this).mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case ClientModeImpl.CMD_WPS_PIN_RETRY /* 131576 */:
                    WpsResult wpsResult = (WpsResult) message.obj;
                    if (!TextUtils.isEmpty(wpsResult.pin)) {
                        ClientModeImpl.this.mWifiNative.startWpsPinKeypad(ClientModeImpl.this.mInterfaceName, wpsResult.pin);
                        ClientModeImpl.this.sendMessageDelayed(ClientModeImpl.CMD_WPS_PIN_RETRY, wpsResult, 50000);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_WPS_PIN_RETRY);
                    Pair<Boolean, Integer> loadResult = loadNetworksFromSupplicantAfterWps();
                    boolean success = ((Boolean) loadResult.first).booleanValue();
                    int netId = ((Integer) loadResult.second).intValue();
                    if (success) {
                        message.arg1 = netId;
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151565);
                    } else {
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, 0);
                    }
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    ClientModeImpl.this.deferMessage(message);
                    ClientModeImpl.this.saveWpsNetIdInWifiPro(message.arg1);
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    clientModeImpl2.transitionTo(clientModeImpl2.mDisconnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Network connection lost");
                    }
                    ClientModeImpl.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    return false;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT /* 147464 */:
                    break;
                case WifiMonitor.WPS_FAIL_EVENT /* 147465 */:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    } else {
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                        this.mSourceMessage.recycle();
                        this.mSourceMessage = null;
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        clientModeImpl3.transitionTo(clientModeImpl3.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT /* 147466 */:
                    ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                    clientModeImpl4.transitionTo(clientModeImpl4.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT /* 147467 */:
                    ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_WPS_PIN_RETRY);
                    ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    clientModeImpl5.transitionTo(clientModeImpl5.mDisconnectedState);
                    break;
                case 147499:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Ignore Assoc reject event during WPS Connection");
                        break;
                    }
                    break;
                case 151562:
                    ClientModeImpl.this.replyToMessage(message, 151564, 1);
                    break;
                case 151566:
                    ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_WPS_PIN_RETRY);
                    if (ClientModeImpl.this.mWifiNative.cancelWps(ClientModeImpl.this.mInterfaceName)) {
                        ClientModeImpl.this.replyToMessage(message, 151568);
                    } else {
                        ClientModeImpl.this.replyToMessage(message, 151567, 0);
                    }
                    ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                    clientModeImpl6.transitionTo(clientModeImpl6.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            if (ClientModeImpl.this.mIsRandomMacCleared) {
                ClientModeImpl.this.setRandomMacOui();
                ClientModeImpl.this.mIsRandomMacCleared = false;
            }
        }

        private Pair<Boolean, Integer> loadNetworksFromSupplicantAfterWps() {
            Map<String, WifiConfiguration> configs = new HashMap<>();
            int netId = -1;
            int i = -1;
            if (!ClientModeImpl.this.mWifiNative.migrateNetworksFromSupplicant(ClientModeImpl.this.mInterfaceName, configs, new SparseArray<>())) {
                ClientModeImpl.this.loge("Failed to load networks from wpa_supplicant after Wps");
                return Pair.create(false, -1);
            }
            for (Map.Entry<String, WifiConfiguration> entry : configs.entrySet()) {
                WifiConfiguration config = entry.getValue();
                config.networkId = -1;
                NetworkUpdateResult result = ClientModeImpl.this.mWifiConfigManager.addOrUpdateNetwork(config, this.mSourceMessage.sendingUid);
                if (!result.isSuccess()) {
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.loge("Failed to add network after WPS: " + entry.getValue());
                    return Pair.create(false, -1);
                } else if (!ClientModeImpl.this.mWifiConfigManager.enableNetwork(result.getNetworkId(), true, this.mSourceMessage.sendingUid)) {
                    Log.wtf(ClientModeImpl.TAG, "Failed to enable network after WPS: " + entry.getValue());
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

    private WifiNetworkAgentSpecifier createNetworkAgentSpecifier(WifiConfiguration currentWifiConfiguration, String currentBssid, int specificRequestUid, String specificRequestPackageName) {
        currentWifiConfiguration.BSSID = currentBssid;
        return new WifiNetworkAgentSpecifier(currentWifiConfiguration, specificRequestUid, specificRequestPackageName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkCapabilities getCapabilities(WifiConfiguration currentWifiConfiguration) {
        NetworkCapabilities result = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        result.setNetworkSpecifier(null);
        if (currentWifiConfiguration == null) {
            return result;
        }
        if (!this.mWifiInfo.isTrusted()) {
            result.removeCapability(14);
        } else {
            result.addCapability(14);
        }
        if (!WifiConfiguration.isMetered(currentWifiConfiguration, this.mWifiInfo)) {
            result.addCapability(11);
        } else {
            result.removeCapability(11);
        }
        if (this.mWifiInfo.getRssi() != -127) {
            result.setSignalStrength(this.mWifiInfo.getRssi());
        } else {
            result.setSignalStrength(WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
        }
        if (currentWifiConfiguration.osu) {
            result.removeCapability(12);
        }
        if (!this.mWifiInfo.getSSID().equals("<unknown ssid>")) {
            result.setSSID(this.mWifiInfo.getSSID());
        } else {
            result.setSSID(null);
        }
        Pair<Integer, String> specificRequestUidAndPackageName = this.mNetworkFactory.getSpecificNetworkRequestUidAndPackageName(currentWifiConfiguration);
        if (((Integer) specificRequestUidAndPackageName.first).intValue() != -1) {
            result.removeCapability(12);
        }
        result.setNetworkSpecifier(createNetworkAgentSpecifier(currentWifiConfiguration, getCurrentBSSID(), ((Integer) specificRequestUidAndPackageName.first).intValue(), (String) specificRequestUidAndPackageName.second));
        return result;
    }

    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration currentWifiConfiguration) {
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (wifiNetworkAgent != null) {
            wifiNetworkAgent.sendNetworkCapabilities(getCapabilities(currentWifiConfiguration));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ProxyInfo getProxyProperties() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        return config.getHttpProxy();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        WifiConfiguration config;
        if (networkId == -1 || (config = this.mWifiConfigManager.getConfiguredNetwork(networkId)) == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEapAuthFailure(int networkId, int errorCode) {
        WifiConfiguration targetedNetwork = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (targetedNetwork != null) {
            int eapMethod = targetedNetwork.enterpriseConfig.getEapMethod();
            if ((eapMethod == 4 || eapMethod == 5 || eapMethod == 6) && errorCode == 16385) {
                getTelephonyManager().createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).resetCarrierKeysForImsiEncryption();
            }
        }
    }

    /* access modifiers changed from: private */
    public class WifiNetworkAgent extends HwNetworkAgent {
        private int mLastNetworkStatus = -1;

        public WifiNetworkAgent(Looper l, Context c, String tag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, tag, ni, nc, lp, score, misc);
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                }
                ClientModeImpl.this.unwantedNetwork(0);
            }
        }

        /* access modifiers changed from: protected */
        public void networkStatus(int status, String redirectUrl) {
            if (this == ClientModeImpl.this.mNetworkAgent && status != this.mLastNetworkStatus) {
                this.mLastNetworkStatus = status;
                if (status == 2) {
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                    }
                    ClientModeImpl.this.unwantedNetwork(1);
                } else if (status == 1) {
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(ClientModeImpl.this.mWifiInfo.score));
                    }
                    ClientModeImpl.this.mWifiMetrics.logStaEvent(14);
                    ClientModeImpl.this.doNetworkStatus(status);
                }
                ClientModeImpl.this.mHwWifiStateMachineEx.handleWifiproPrivateStatus(status);
            }
        }

        /* access modifiers changed from: protected */
        public void saveAcceptUnvalidated(boolean accept) {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
            }
        }

        /* access modifiers changed from: protected */
        public void startSocketKeepalive(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void stopSocketKeepalive(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void addKeepalivePacketFilter(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void removeKeepalivePacketFilter(Message msg) {
            ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.sendMessage(ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD, clientModeImpl.mWifiInfo.getRssi());
                return;
            }
            int[] rssiVals = Arrays.copyOf(thresholds, thresholds.length + 2);
            rssiVals[rssiVals.length - 2] = -128;
            rssiVals[rssiVals.length - 1] = 127;
            Arrays.sort(rssiVals);
            byte[] rssiRange = new byte[rssiVals.length];
            for (int i = 0; i < rssiVals.length; i++) {
                int val = rssiVals[i];
                if (val > 127 || val < -128) {
                    Log.e(ClientModeImpl.TAG, "Illegal value " + val + " for RSSI thresholds: " + Arrays.toString(rssiVals));
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    clientModeImpl2.sendMessage(ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD, clientModeImpl2.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            ClientModeImpl.this.mRssiRanges = rssiRange;
            ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
            clientModeImpl3.sendMessage(ClientModeImpl.CMD_START_RSSI_MONITORING_OFFLOAD, clientModeImpl3.mWifiInfo.getRssi());
        }

        /* access modifiers changed from: protected */
        public void preventAutomaticReconnect() {
            if (this == ClientModeImpl.this.mNetworkAgent) {
                ClientModeImpl.this.unwantedNetwork(2);
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
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    class L2ConnectedState extends State {
        RssiEventHandler mRssiEventHandler = new RssiEventHandler();

        class RssiEventHandler implements WifiNative.WifiRssiEventHandler {
            RssiEventHandler() {
            }

            @Override // com.android.server.wifi.WifiNative.WifiRssiEventHandler
            public void onRssiThresholdBreached(byte curRssi) {
                if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                    Log.e(ClientModeImpl.TAG, "onRssiThresholdBreach event. Cur Rssi = " + ((int) curRssi));
                }
                ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_RSSI_THRESHOLD_BREACHED, curRssi);
            }
        }

        L2ConnectedState() {
        }

        public void enter() {
            int netId;
            if (ClientModeImpl.DBG) {
                ClientModeImpl.this.log(getName());
            }
            ClientModeImpl.access$10208(ClientModeImpl.this);
            if (ClientModeImpl.this.mEnableRssiPolling) {
                ClientModeImpl.this.mLinkProbeManager.resetOnNewConnection();
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.sendMessage(ClientModeImpl.CMD_RSSI_POLL, clientModeImpl.mRssiPollToken, 0);
            }
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.loge("Have NetworkAgent when entering L2Connected");
                ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            }
            ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTING);
            ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
            NetworkCapabilities nc = clientModeImpl2.getCapabilities(clientModeImpl2.getCurrentWifiConfiguration());
            synchronized (ClientModeImpl.this.mNetworkAgentLock) {
                ClientModeImpl.this.mNetworkAgent = new WifiNetworkAgent(ClientModeImpl.this.getHandler().getLooper(), ClientModeImpl.this.mContext, "WifiNetworkAgent", ClientModeImpl.this.mNetworkInfo, nc, ClientModeImpl.this.mLinkProperties, ClientModeImpl.this.reportWifiScoreDelayed() ? 99 : 60, ClientModeImpl.this.mNetworkMisc);
                netId = ClientModeImpl.this.mNetworkAgent.netId;
            }
            HwWifiServiceFactory.getHwWifiScoreReportEx().setLowScoreCount(0);
            ClientModeImpl.this.clearTargetBssid("L2ConnectedState");
            ClientModeImpl.this.mCountryCode.setReadyForChange(false);
            ClientModeImpl.this.mWifiMetrics.setWifiState(3);
            ClientModeImpl.this.mWifiScoreCard.noteNetworkAgentCreated(ClientModeImpl.this.mWifiInfo, netId);
        }

        public void exit() {
            if (ClientModeImpl.this.mIpClient != null) {
                ClientModeImpl.this.mIpClient.stop();
            }
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=" + Integer.toString(ClientModeImpl.this.mLastNetworkId));
                if (ClientModeImpl.this.mLastBssid != null) {
                    sb.append(" ");
                    sb.append(ClientModeImpl.this.mLastBssid);
                }
            }
            if (!(ClientModeImpl.this.mLastBssid == null && ClientModeImpl.this.mLastNetworkId == -1)) {
                ClientModeImpl.this.handleNetworkDisconnect();
            }
            ClientModeImpl.this.mCountryCode.setReadyForChange(true);
            ClientModeImpl.this.mWifiMetrics.setWifiState(2);
            ClientModeImpl.this.mWifiStateTracker.updateState(2);
            ClientModeImpl.this.mWifiInjector.getWifiLockManager().updateWifiClientConnected(false);
        }

        /* JADX INFO: Multiple debug info for r1v88 int: [D('currRssi' byte), D('slot' int)] */
        /* JADX INFO: Multiple debug info for r1v159 int: [D('netId' int), D('info' android.net.wifi.RssiPacketCountInfo)] */
        public boolean processMessage(Message message) {
            ScanDetailCache scanDetailCache;
            ScanResult scanResult;
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_DISCONNECT /* 131145 */:
                    ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 2);
                    ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    clientModeImpl.transitionTo(clientModeImpl.mDisconnectingState);
                    break;
                case ClientModeImpl.CMD_RECONNECT /* 131146 */:
                    ClientModeImpl.this.log(" Ignore CMD_RECONNECT request because wifi is already connected");
                    break;
                case ClientModeImpl.CMD_ENABLE_RSSI_POLL /* 131154 */:
                    ClientModeImpl.this.cleanWifiScore();
                    ClientModeImpl.this.mEnableRssiPolling = message.arg1 == 1;
                    ClientModeImpl.access$10208(ClientModeImpl.this);
                    if (ClientModeImpl.this.mEnableRssiPolling) {
                        ClientModeImpl.this.mLastSignalLevel = -1;
                        ClientModeImpl.this.mLinkProbeManager.resetOnScreenTurnedOn();
                        ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.sendMessageDelayed(clientModeImpl2.obtainMessage(ClientModeImpl.CMD_RSSI_POLL, clientModeImpl2.mRssiPollToken, 0), (long) ClientModeImpl.this.mPollRssiIntervalMsecs);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_RSSI_POLL /* 131155 */:
                    if (message.arg1 == ClientModeImpl.this.mRssiPollToken) {
                        WifiLinkLayerStats stats = updateLinkLayerStatsRssiAndScoreReportInternal();
                        ClientModeImpl.this.mWifiMetrics.updateWifiUsabilityStatsEntries(ClientModeImpl.this.mWifiInfo, stats);
                        if (ClientModeImpl.this.mWifiScoreReport.shouldCheckIpLayer()) {
                            if (ClientModeImpl.this.mIpClient != null) {
                                ClientModeImpl.this.mIpClient.confirmConfiguration();
                            }
                            ClientModeImpl.this.mWifiScoreReport.noteIpCheck();
                        }
                        int statusDataStall = ClientModeImpl.this.mWifiDataStall.checkForDataStall(ClientModeImpl.this.mLastLinkLayerStats, stats);
                        if (statusDataStall != 0) {
                            ClientModeImpl.this.mWifiMetrics.addToWifiUsabilityStatsList(2, ClientModeImpl.convertToUsabilityStatsTriggerType(statusDataStall), -1);
                        }
                        ClientModeImpl.this.mWifiMetrics.incrementWifiLinkLayerUsageStats(stats);
                        ClientModeImpl.this.mLastLinkLayerStats = stats;
                        ClientModeImpl.this.mWifiScoreCard.noteSignalPoll(ClientModeImpl.this.mWifiInfo);
                        ClientModeImpl.this.mLinkProbeManager.updateConnectionStats(ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        clientModeImpl3.sendMessageDelayed(clientModeImpl3.obtainMessage(ClientModeImpl.CMD_RSSI_POLL, clientModeImpl3.mRssiPollToken, 0), (long) ClientModeImpl.this.mPollRssiIntervalMsecs);
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                            clientModeImpl4.sendRssiChangeBroadcast(clientModeImpl4.mWifiInfo.getRssi());
                        }
                        ClientModeImpl.this.mWifiTrafficPoller.notifyOnDataActivity(ClientModeImpl.this.mWifiInfo.txSuccess, ClientModeImpl.this.mWifiInfo.rxSuccess);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_ONESHOT_RSSI_POLL /* 131156 */:
                    if (!ClientModeImpl.this.mEnableRssiPolling) {
                        updateLinkLayerStatsRssiAndScoreReportInternal();
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_RESET_SIM_NETWORKS /* 131173 */:
                    if (message.arg1 == 0 && ClientModeImpl.this.mLastNetworkId != -1) {
                        WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mLastNetworkId);
                        if (TelephonyUtil.isSimConfig(config)) {
                            ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 6);
                            ClientModeImpl.this.handleSimAbsent(config);
                            ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                            ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                            clientModeImpl5.transitionTo(clientModeImpl5.mDisconnectingState);
                        }
                    }
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_SUCCESSFUL /* 131210 */:
                    ClientModeImpl.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL");
                    if (ClientModeImpl.this.getCurrentWifiConfiguration() != null) {
                        ClientModeImpl.this.handleSuccessfulIpConfiguration();
                        if (ClientModeImpl.this.isHiLinkActive()) {
                            ClientModeImpl.this.setWifiBackgroundReason(6);
                        }
                        ClientModeImpl.this.notifyIpConfigCompleted();
                        if (!ClientModeImpl.this.mHwWifiStateMachineEx.ignoreEnterConnectedStateByWifipro()) {
                            int wifiApType = ClientModeImpl.this.getWifiApTypeFromMpLink();
                            ClientModeImpl.this.mWifiInfo.setWifiApType(wifiApType);
                            ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                            clientModeImpl6.log("setWifiApType " + wifiApType);
                            if (!ClientModeImpl.this.reportWifiScoreDelayed()) {
                                ClientModeImpl.this.mWifiScoreReport.calculateAndReportScore(ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mNetworkAgent, ClientModeImpl.this.mWifiMetrics);
                            }
                            ClientModeImpl.this.sendConnectedState();
                            ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                            clientModeImpl7.transitionTo(clientModeImpl7.mConnectedState);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.reportConnectionAttemptEnd(6, 1, 0);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                        clientModeImpl8.transitionTo(clientModeImpl8.mDisconnectingState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                    ClientModeImpl.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_LOST");
                    ClientModeImpl.this.getWifiLinkLayerStats();
                    ClientModeImpl clientModeImpl9 = ClientModeImpl.this;
                    if (!clientModeImpl9.notifyIpConfigLostAndFixedBySce(clientModeImpl9.getCurrentWifiConfiguration())) {
                        ClientModeImpl.this.handleIpConfigurationLost();
                        ClientModeImpl.this.reportConnectionAttemptEnd(10, 1, 0);
                        ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), ClientModeImpl.this.mTargetRoamBSSID, 3);
                        ClientModeImpl clientModeImpl10 = ClientModeImpl.this;
                        clientModeImpl10.transitionTo(clientModeImpl10.mDisconnectingState);
                        break;
                    } else {
                        ClientModeImpl.this.log("L2ConnectedState, notifyIpConfigLostAndFixedBySce!!!!");
                        break;
                    }
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /* 131219 */:
                    if (((String) message.obj) != null) {
                        ClientModeImpl.this.mLastBssid = (String) message.obj;
                        if (ClientModeImpl.this.mLastBssid != null && (ClientModeImpl.this.mWifiInfo.getBSSID() == null || !ClientModeImpl.this.mLastBssid.equals(ClientModeImpl.this.mWifiInfo.getBSSID()))) {
                            ClientModeImpl.this.mWifiInfo.setBSSID(ClientModeImpl.this.mLastBssid);
                            ClientModeImpl.this.setSupportedWifiCategory();
                            WifiConfiguration config2 = ClientModeImpl.this.getCurrentWifiConfiguration();
                            if (!(config2 == null || (scanDetailCache = ClientModeImpl.this.mWifiConfigManager.getScanDetailCacheForNetwork(config2.networkId)) == null || (scanResult = scanDetailCache.getScanResult(ClientModeImpl.this.mLastBssid)) == null)) {
                                ClientModeImpl.this.mWifiInfo.setFrequency(scanResult.frequency);
                            }
                            if (!ClientModeImpl.this.isWifiSelfCuring()) {
                                Log.i(ClientModeImpl.TAG, "CMD_ASSOCIATED_BSSID, isWiFiProSwitchOnGoing = " + ClientModeImpl.this.isWiFiProSwitchOnGoing());
                                if (ClientModeImpl.this.isWiFiProSwitchOnGoing() && ClientModeImpl.this.getWiFiProRoamingSSID() != null && ClientModeImpl.this.getCurrentState() == ClientModeImpl.this.mRoamingState) {
                                    ClientModeImpl.this.mWifiInfo.setSSID(ClientModeImpl.this.getWiFiProRoamingSSID());
                                }
                                ClientModeImpl clientModeImpl11 = ClientModeImpl.this;
                                clientModeImpl11.sendNetworkStateChangeBroadcast(clientModeImpl11.mLastBssid);
                                break;
                            } else {
                                ClientModeImpl.this.logd("CMD_ASSOCIATED_BSSID, WifiSelfCuring, ignore associated bssid change message.");
                                break;
                            }
                        }
                    } else {
                        ClientModeImpl.this.logw("Associated command w/o BSSID");
                        break;
                    }
                case ClientModeImpl.CMD_IP_REACHABILITY_LOST /* 131221 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled && message.obj != null) {
                        ClientModeImpl.this.log((String) message.obj);
                    }
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(9);
                    ClientModeImpl.this.mWifiMetrics.logWifiIsUnusableEvent(5);
                    ClientModeImpl.this.mWifiMetrics.addToWifiUsabilityStatsList(2, 5, -1);
                    if (!ClientModeImpl.this.mIpReachabilityDisconnectEnabled) {
                        ClientModeImpl.this.logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        break;
                    } else {
                        ClientModeImpl.this.handleIpReachabilityLost();
                        ClientModeImpl clientModeImpl12 = ClientModeImpl.this;
                        clientModeImpl12.transitionTo(clientModeImpl12.mDisconnectingState);
                        break;
                    }
                case ClientModeImpl.CMD_START_IP_PACKET_OFFLOAD /* 131232 */:
                    int slot = message.arg1;
                    int result = ClientModeImpl.this.startWifiIPPacketOffload(slot, (KeepalivePacketData) message.obj, message.arg2);
                    if (ClientModeImpl.this.mNetworkAgent != null) {
                        ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(slot, result);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
                case ClientModeImpl.CMD_RSSI_THRESHOLD_BREACHED /* 131236 */:
                    ClientModeImpl.this.processRssiThreshold((byte) message.arg1, message.what, this.mRssiEventHandler);
                    break;
                case ClientModeImpl.CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
                    ClientModeImpl.this.stopRssiMonitoringOffload();
                    break;
                case ClientModeImpl.CMD_IPV4_PROVISIONING_SUCCESS /* 131272 */:
                    ClientModeImpl.this.handleIPv4Success((DhcpResults) message.obj);
                    ClientModeImpl.this.makeHwDefaultIPTable((DhcpResults) message.obj);
                    ClientModeImpl clientModeImpl13 = ClientModeImpl.this;
                    clientModeImpl13.sendNetworkStateChangeBroadcast(clientModeImpl13.mLastBssid);
                    break;
                case ClientModeImpl.CMD_IPV4_PROVISIONING_FAILURE /* 131273 */:
                    ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(4);
                    if (ClientModeImpl.DBG) {
                        ClientModeImpl.this.getCurrentWifiConfiguration();
                        ClientModeImpl clientModeImpl14 = ClientModeImpl.this;
                        clientModeImpl14.log("DHCP failure count=-1");
                    }
                    ClientModeImpl.this.handleIPv4Failure();
                    ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), ClientModeImpl.this.mTargetRoamBSSID, 3);
                    break;
                case ClientModeImpl.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /* 131281 */:
                    if (ClientModeImpl.this.mIpClient != null) {
                        int slot2 = message.arg1;
                        if (!(message.obj instanceof NattKeepalivePacketData)) {
                            if (message.obj instanceof TcpKeepalivePacketData) {
                                ClientModeImpl.this.mIpClient.addKeepalivePacketFilter(slot2, (TcpKeepalivePacketData) message.obj);
                                break;
                            }
                        } else {
                            ClientModeImpl.this.mIpClient.addKeepalivePacketFilter(slot2, (NattKeepalivePacketData) message.obj);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /* 131282 */:
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.removeKeepalivePacketFilter(message.arg1);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_PRE_DHCP_ACTION /* 131327 */:
                    ClientModeImpl.this.handlePreDhcpSetup();
                    if (ClientModeImpl.this.mHwWifiCHRService != null) {
                        if (NetworkInfo.DetailedState.OBTAINING_IPADDR != ClientModeImpl.this.mNetworkInfo.getDetailedState()) {
                            if (NetworkInfo.DetailedState.CONNECTED == ClientModeImpl.this.mNetworkInfo.getDetailedState()) {
                                ClientModeImpl.this.mHwWifiCHRService.updateDhcpState(10);
                                break;
                            }
                        } else {
                            ClientModeImpl.this.mHwWifiCHRService.updateDhcpState(0);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_PRE_DHCP_ACTION_COMPLETE /* 131328 */:
                    if (ClientModeImpl.this.mIpClient != null) {
                        ClientModeImpl.this.mIpClient.completedPreDhcpAction();
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_POST_DHCP_ACTION /* 131329 */:
                    ClientModeImpl.this.handlePostDhcpSetup();
                    break;
                case ClientModeImpl.CMD_TRY_CACHED_IP /* 131330 */:
                    DhcpResults dhcpResults = ClientModeImpl.this.getCachedDhcpResultsForCurrentConfig();
                    if (!(dhcpResults == null || ClientModeImpl.this.mIpClient == null)) {
                        ClientModeImpl.this.stopIpClient();
                        dhcpResults.domains = ClientModeImpl.TYPE_GET_CACHE_DHCP_RESULT;
                        ClientModeImpl.this.mIpClient.startProvisioning(new ProvisioningConfiguration.Builder().withStaticConfiguration(dhcpResults.toStaticIpConfiguration()).withoutIpReachabilityMonitor().withApfCapabilities(ClientModeImpl.this.mWifiNative.getApfCapabilities(ClientModeImpl.this.mInterfaceName)).build());
                        break;
                    }
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /* 143372 */:
                    if (message.arg1 == 1) {
                        ClientModeImpl.this.log("L2ConnectedState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 5);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl.this.mTemporarilyDisconnectWifi = true;
                        ClientModeImpl clientModeImpl15 = ClientModeImpl.this;
                        clientModeImpl15.transitionTo(clientModeImpl15.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    ClientModeImpl.this.mWifiInfo.setBSSID((String) message.obj);
                    ClientModeImpl.this.mLastNetworkId = message.arg1;
                    ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                    ClientModeImpl.this.mWifiInfo.setMacAddress(ClientModeImpl.this.mWifiNative.getMacAddress(ClientModeImpl.this.mInterfaceName));
                    if (ClientModeImpl.this.mLastBssid != null && !ClientModeImpl.this.mLastBssid.equals(message.obj)) {
                        ClientModeImpl.this.mLastBssid = (String) message.obj;
                        ClientModeImpl clientModeImpl16 = ClientModeImpl.this;
                        clientModeImpl16.sendNetworkStateChangeBroadcast(clientModeImpl16.mLastBssid);
                    }
                    ClientModeImpl.this.checkSelfCureWifiResult(103);
                    if (ClientModeImpl.this.mLastNetworkId == -1) {
                        ClientModeImpl clientModeImpl17 = ClientModeImpl.this;
                        NetworkUpdateResult networkUpdateResult = clientModeImpl17.saveWpsOkcConfiguration(clientModeImpl17.mLastNetworkId, ClientModeImpl.this.mLastBssid);
                        if (networkUpdateResult != null) {
                            ClientModeImpl.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                        } else {
                            WifiConfiguration config3 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(ClientModeImpl.this.mWifiConfigManager.getLastSelectedNetwork());
                            if (!(config3 == null || config3.SSID == null || !config3.SSID.equals(ClientModeImpl.this.mWifiInfo.getSSID()))) {
                                ClientModeImpl.this.mLastNetworkId = config3.networkId;
                                ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                            }
                        }
                    }
                    ClientModeImpl clientModeImpl18 = ClientModeImpl.this;
                    clientModeImpl18.notifyWifiRoamingCompleted(clientModeImpl18.mLastBssid);
                    ClientModeImpl clientModeImpl19 = ClientModeImpl.this;
                    clientModeImpl19.notifyWlanChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(clientModeImpl19.mWifiInfo.getFrequency()));
                    ClientModeImpl clientModeImpl20 = ClientModeImpl.this;
                    clientModeImpl20.setLastConnectConfig(clientModeImpl20.getCurrentWifiConfiguration());
                    if (ClientModeImpl.ENABLE_DHCP_AFTER_ROAM) {
                        ClientModeImpl.this.log("L2ConnectedState_NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                        ClientModeImpl clientModeImpl21 = ClientModeImpl.this;
                        clientModeImpl21.transitionTo(clientModeImpl21.mObtainingIpState);
                        break;
                    }
                    break;
                case 151553:
                    if (ClientModeImpl.this.mWifiInfo.getNetworkId() == message.arg1) {
                        if (!ClientModeImpl.this.isWifiProEvaluatingAP()) {
                            ClientModeImpl.this.replyToMessage(message, 151555);
                            break;
                        } else {
                            ClientModeImpl.this.logd("==connection to same network==");
                            return false;
                        }
                    } else {
                        handleStatus = false;
                        break;
                    }
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = ClientModeImpl.this.mWifiInfo.getRssi();
                    WifiNative.TxPacketCounters counters = ClientModeImpl.this.mWifiNative.getTxPacketCounters(ClientModeImpl.this.mInterfaceName);
                    if (counters == null) {
                        ClientModeImpl.this.replyToMessage(message, 151574, 0);
                        break;
                    } else {
                        info.txgood = counters.txSucceeded;
                        info.txbad = counters.txFailed;
                        ClientModeImpl.this.replyToMessage(message, 151573, info);
                        break;
                    }
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        private WifiLinkLayerStats updateLinkLayerStatsRssiAndScoreReportInternal() {
            WifiLinkLayerStats stats = ClientModeImpl.this.getWifiLinkLayerStats();
            ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
            if (!ClientModeImpl.this.reportWifiScoreDelayed()) {
                ClientModeImpl.this.mWifiScoreReport.calculateAndReportScore(ClientModeImpl.this.mWifiInfo, ClientModeImpl.this.mNetworkAgent, ClientModeImpl.this.mWifiMetrics);
            }
            return stats;
        }
    }

    public void updateLinkLayerStatsRssiAndScoreReport() {
        sendMessage(CMD_ONESHOT_RSSI_POLL);
    }

    /* access modifiers changed from: private */
    public static int convertToUsabilityStatsTriggerType(int unusableEventTriggerType) {
        if (unusableEventTriggerType == 1) {
            return 1;
        }
        if (unusableEventTriggerType == 2) {
            return 2;
        }
        if (unusableEventTriggerType == 3) {
            return 3;
        }
        if (unusableEventTriggerType == 4) {
            return 4;
        }
        if (unusableEventTriggerType == 5) {
            return 5;
        }
        Log.e(TAG, "Unknown WifiIsUnusableEvent: " + unusableEventTriggerType);
        return 0;
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            StaticIpConfiguration staticIpConfig;
            WifiConfiguration currentConfig = ClientModeImpl.this.getCurrentWifiConfiguration();
            boolean isUsingStaticIp = false;
            if (currentConfig != null && currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                isUsingStaticIp = true;
            }
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                String key = null;
                if (currentConfig != null) {
                    key = currentConfig.configKey();
                }
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.log("enter ObtainingIpState netId=" + Integer.toString(ClientModeImpl.this.mLastNetworkId) + " " + key + "  roam=" + ClientModeImpl.this.mIsAutoRoaming + " static=" + isUsingStaticIp);
            }
            if (ClientModeImpl.this.hasMessages(ClientModeImpl.CMD_OBTAINING_IP_TIMEOUT)) {
                ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_OBTAINING_IP_TIMEOUT);
            }
            if (ClientModeImpl.this.mIpClient == null) {
                ClientModeImpl.this.sendMessageDelayed(ClientModeImpl.CMD_OBTAINING_IP_TIMEOUT, 30000);
            }
            ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR);
            ClientModeImpl.this.clearTargetBssid("ObtainingIpAddress");
            ClientModeImpl.this.stopIpClient();
            if (ClientModeImpl.this.mIpClient != null) {
                ClientModeImpl.this.mIpClient.setHttpProxy(ClientModeImpl.this.getProxyProperties());
                if (!TextUtils.isEmpty(ClientModeImpl.this.mTcpBufferSizes)) {
                    ClientModeImpl.this.mIpClient.setTcpBufferSizes(ClientModeImpl.this.mTcpBufferSizes);
                }
            }
            ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
            clientModeImpl2.tryUseStaticIpForFastConnecting(clientModeImpl2.mLastNetworkId);
            if (!isUsingStaticIp) {
                staticIpConfig = new ProvisioningConfiguration.Builder().withPreDhcpAction().withoutIpReachabilityMonitor().withApfCapabilities(ClientModeImpl.this.mWifiNative.getApfCapabilities(ClientModeImpl.this.mInterfaceName)).withNetwork(ClientModeImpl.this.getCurrentNetwork()).withDisplayName(currentConfig != null ? currentConfig.SSID : "").withRandomMacAddress().build();
                if (ClientModeImpl.this.mIpClient != null) {
                    ClientModeImpl.this.mIpClient.putPendingSSID(ClientModeImpl.this.mWifiInfo.getBSSID());
                }
                ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                clientModeImpl3.setForceDhcpDiscovery(clientModeImpl3.mIpClient);
            } else {
                StaticIpConfiguration prov = new ProvisioningConfiguration.Builder().withStaticConfiguration(currentConfig.getStaticIpConfiguration()).withoutIpReachabilityMonitor().withApfCapabilities(ClientModeImpl.this.mWifiNative.getApfCapabilities(ClientModeImpl.this.mInterfaceName)).withNetwork(ClientModeImpl.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).build();
                if (ClientModeImpl.this.mHwWifiCHRService != null) {
                    ClientModeImpl.this.mHwWifiCHRService.updateDhcpState(8);
                }
                staticIpConfig = prov;
            }
            if (((ClientModeImpl) ClientModeImpl.this).mIpClient != null) {
                ClientModeImpl.this.mIpClient.startProvisioning(staticIpConfig);
            }
            ClientModeImpl.this.getWifiLinkLayerStats();
        }

        public void exit() {
            if (ClientModeImpl.this.hasMessages(ClientModeImpl.CMD_OBTAINING_IP_TIMEOUT)) {
                ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_OBTAINING_IP_TIMEOUT);
            }
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                case ClientModeImpl.CMD_START_CONNECT /* 131215 */:
                case ClientModeImpl.CMD_START_ROAM /* 131217 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case ClientModeImpl.CMD_OBTAINING_IP_TIMEOUT /* 131331 */:
                    ClientModeImpl.this.setMulticastFilter(true);
                    ClientModeImpl.this.sendMessage(ClientModeImpl.CMD_IP_CONFIGURATION_LOST);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    ClientModeImpl.this.reportConnectionAttemptEnd(6, 1, 0);
                    handleStatus = false;
                    break;
                case 151553:
                    if (ClientModeImpl.this.mWifiInfo.getNetworkId() != message.arg1) {
                        ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                        ClientModeImpl.this.deferMessage(message);
                        ClientModeImpl.this.mWifiScoreCard.noteConnectionAttempt(ClientModeImpl.this.mWifiInfo);
                        ClientModeImpl.this.reportConnectionAttemptEnd(7, 1, 0);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl = ClientModeImpl.this;
                        clientModeImpl.transitionTo(clientModeImpl.mDisconnectingState);
                        break;
                    } else {
                        handleStatus = false;
                        break;
                    }
                case 151559:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                default:
                    if (!ClientModeImpl.this.mHwWifiStateMachineEx.handleHwPrivateMsgInObtainingIpState(message)) {
                        handleStatus = false;
                        break;
                    }
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    @VisibleForTesting
    public boolean shouldEvaluateWhetherToSendExplicitlySelected(WifiConfiguration currentConfig) {
        if (currentConfig == null) {
            Log.wtf(TAG, "Current WifiConfiguration is null, but IP provisioning just succeeded");
            return false;
        }
        long currentTimeMillis = this.mClock.getElapsedSinceBootMillis();
        if (this.mWifiConfigManager.getLastSelectedNetwork() != currentConfig.networkId || currentTimeMillis - this.mWifiConfigManager.getLastSelectedTimeStamp() >= 30000) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        boolean explicitlySelected = false;
        if (shouldEvaluateWhetherToSendExplicitlySelected(config)) {
            notifyNetworkUserConnect(true);
            explicitlySelected = true;
            String callingApp = this.mContext.getPackageManager().getNameForUid(config.lastConnectUid);
            String[] strArr = APP_PACKAGE_WHITE_LIST;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(callingApp)) {
                    log("Network selected by Android Auto, uid is " + config.lastConnectUid);
                    explicitlySelected = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(config.lastConnectUid);
                    break;
                } else {
                    i++;
                }
            }
            if (this.mVerboseLoggingEnabled) {
                log("Network selected by UID " + config.lastConnectUid + " explicitlySelected=" + explicitlySelected);
            }
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("explictlySelected=");
            sb.append(explicitlySelected);
            sb.append(" acceptUnvalidated=");
            sb.append(config == null ? "config is null" : Boolean.valueOf(config.noInternetAccessExpected));
            log(sb.toString());
        }
        if (isWifiSelfCuring()) {
            log("is wifi selfCuring, set explictlySelected to false");
            explicitlySelected = false;
        }
        WifiNetworkAgent wifiNetworkAgent = this.mNetworkAgent;
        if (!(wifiNetworkAgent == null || config == null)) {
            wifiNetworkAgent.explicitlySelected(explicitlySelected, config.noInternetAccessExpected);
            this.mNetworkAgent.duplexSelected(config.connectToCellularAndWLAN, config.noInternetAccessExpected);
        }
        setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl.this.log("RoamingState Enter mScreenOn=" + ClientModeImpl.this.mScreenOn);
            }
            ClientModeImpl.this.enterConnectedStateByMode();
            ClientModeImpl.this.mRoamWatchdogCount++;
            ClientModeImpl.this.logd("Start Roam Watchdog " + ClientModeImpl.this.mRoamWatchdogCount);
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.sendMessageDelayed(clientModeImpl.obtainMessage(ClientModeImpl.CMD_ROAM_WATCHDOG_TIMER, clientModeImpl.mRoamWatchdogCount, 0), 15000);
            this.mAssociated = false;
            ClientModeImpl.this.setWiFiProRoamingSSID(null);
        }

        public boolean processMessage(Message message) {
            ClientModeImpl clientModeImpl;
            NetworkUpdateResult networkUpdateResult;
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                    if (ClientModeImpl.this.mRoamWatchdogCount == message.arg1) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("roaming watchdog! -> disconnect");
                        }
                        ClientModeImpl.this.mWifiMetrics.endConnectionEvent(9, 1, 0);
                        ClientModeImpl.access$13908(ClientModeImpl.this);
                        ClientModeImpl.this.handleNetworkDisconnect();
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 4);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.transitionTo(clientModeImpl2.mDisconnectedState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                    if (ClientModeImpl.this.getCurrentWifiConfiguration() != null) {
                        ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(3);
                    }
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_UNWANTED_NETWORK /* 131216 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Roaming and CS doesn't want the network -> ignore");
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    if (!this.mAssociated) {
                        ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    } else {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("roaming and Network connection established");
                        }
                        ClientModeImpl.this.mLastNetworkId = message.arg1;
                        ClientModeImpl.this.mLastBssid = (String) message.obj;
                        if (ClientModeImpl.this.mLastNetworkId == -1 && (networkUpdateResult = (clientModeImpl = ClientModeImpl.this).saveWpsOkcConfiguration(clientModeImpl.mLastNetworkId, ClientModeImpl.this.mLastBssid)) != null) {
                            ClientModeImpl.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                        }
                        ClientModeImpl.this.mWifiInfo.setBSSID(ClientModeImpl.this.mLastBssid);
                        ClientModeImpl.this.mWifiInfo.setNetworkId(ClientModeImpl.this.mLastNetworkId);
                        ClientModeImpl.this.mWifiConnectivityManager.trackBssid(ClientModeImpl.this.mLastBssid, true, message.arg2);
                        ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                        clientModeImpl3.sendNetworkStateChangeBroadcast(clientModeImpl3.mLastBssid);
                        ClientModeImpl.this.reportConnectionAttemptEnd(1, 1, 0);
                        ClientModeImpl.this.clearTargetBssid("RoamingCompleted");
                        ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                        clientModeImpl4.notifyWifiRoamingCompleted(clientModeImpl4.mLastBssid);
                        ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                        clientModeImpl5.setLastConnectConfig(clientModeImpl5.getCurrentWifiConfiguration());
                        if (!ClientModeImpl.ENABLE_DHCP_AFTER_ROAM) {
                            ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                            clientModeImpl6.transitionTo(clientModeImpl6.mConnectedState);
                            break;
                        } else {
                            ClientModeImpl.this.log("RoamingState_NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                            ClientModeImpl clientModeImpl7 = ClientModeImpl.this;
                            clientModeImpl7.transitionTo(clientModeImpl7.mObtainingIpState);
                            break;
                        }
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    String bssid = (String) message.obj;
                    String target = "";
                    if (ClientModeImpl.this.mTargetRoamBSSID != null) {
                        target = ClientModeImpl.this.mTargetRoamBSSID;
                    }
                    ClientModeImpl clientModeImpl8 = ClientModeImpl.this;
                    clientModeImpl8.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + StringUtilEx.safeDisplayBssid(bssid) + " target=" + StringUtilEx.safeDisplayBssid(target));
                    if (message.arg2 == 15 || message.arg2 == 2) {
                        ClientModeImpl.this.handleDualbandHandoverFailed(3);
                    }
                    if (bssid != null && bssid.equals(ClientModeImpl.this.mTargetRoamBSSID)) {
                        ClientModeImpl.this.handleNetworkDisconnect();
                        ClientModeImpl clientModeImpl9 = ClientModeImpl.this;
                        clientModeImpl9.transitionTo(clientModeImpl9.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl clientModeImpl10 = ClientModeImpl.this;
                            clientModeImpl10.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(ClientModeImpl.this.mTargetRoamBSSID)) {
                            ClientModeImpl.this.handleNetworkDisconnect();
                            ClientModeImpl clientModeImpl11 = ClientModeImpl.this;
                            clientModeImpl11.transitionTo(clientModeImpl11.mDisconnectedState);
                        }
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            ClientModeImpl.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                        }
                        ClientModeImpl.this.notifyWifiRoamingStarted();
                        ClientModeImpl.this.setWiFiProRoamingSSID(stateChangeResult.wifiSsid);
                        break;
                    }
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            ClientModeImpl.this.logd("ClientModeImpl: Leaving Roaming state");
            ClientModeImpl.this.setWiFiProRoamingSSID(null);
        }
    }

    public void triggerUpdateAPInfo() {
        Log.i(TAG, "triggerUpdateAPInfo");
    }

    class ConnectedState extends State {
        private Message mSourceMessage = null;

        ConnectedState() {
        }

        public void enter() {
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.logd("WifiStateMachine: enter Connected state" + getName());
            ClientModeImpl.this.processStatistics(0);
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                clientModeImpl2.log("Enter ConnectedState  mScreenOn=" + ClientModeImpl.this.mScreenOn);
            }
            ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
            clientModeImpl3.triggerRoamingNetworkMonitor(clientModeImpl3.mIsAutoRoaming);
            ClientModeImpl.this.handleConnectedInWifiPro();
            ClientModeImpl.this.reportConnectionAttemptEnd(1, 1, 0);
            if (ClientModeImpl.this.mWifiConnectivityManager != null) {
                ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
            }
            ClientModeImpl.this.registerConnected();
            ClientModeImpl.this.mLastConnectAttemptTimestamp = 0;
            ClientModeImpl.this.mTargetWifiConfiguration = null;
            ClientModeImpl.this.mWifiScoreReport.reset();
            ClientModeImpl.this.mLastSignalLevel = -1;
            ClientModeImpl.this.mIsAutoRoaming = false;
            ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            ClientModeImpl.this.mTargetNetworkId = -1;
            ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            ClientModeImpl.this.triggerUpdateAPInfo();
            ClientModeImpl.this.mWifiStateTracker.updateState(3);
            ClientModeImpl.this.mWifiInjector.getWifiLockManager().updateWifiClientConnected(true);
            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
            clientModeImpl4.notifyWlanChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(clientModeImpl4.mWifiInfo.getFrequency()));
            if (ClientModeImpl.this.mNetworkAgent != null) {
                ClientModeImpl.this.mNetworkAgent.sendWifiApType(ClientModeImpl.this.getWifiApTypeFromMpLink());
            }
        }

        public boolean processMessage(Message message) {
            String str;
            String str2;
            boolean handleStatus = true;
            boolean accept = false;
            switch (message.what) {
                case ClientModeImpl.CMD_UNWANTED_NETWORK /* 131216 */:
                    if (message.arg1 != 0) {
                        if (message.arg1 == 2 || message.arg1 == 1 || message.arg1 == 3) {
                            if (message.arg1 == 2) {
                                str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                            } else {
                                str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                            }
                            Log.i(ClientModeImpl.TAG, str);
                            WifiConfiguration config = ClientModeImpl.this.getCurrentWifiConfiguration();
                            if (config != null) {
                                if (message.arg1 == 2) {
                                    ClientModeImpl.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, false);
                                    Log.i(ClientModeImpl.TAG, "updateNetworkSelectionStatus(DISABLED_NO_INTERNET)");
                                    ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(config.networkId, 10);
                                } else {
                                    ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_DIAGS_CONNECT_TIMEOUT);
                                    ClientModeImpl.this.mWifiDiagnostics.reportConnectionEvent((byte) 2);
                                    ClientModeImpl.this.mWifiConfigManager.incrementNetworkNoInternetAccessReports(config.networkId);
                                }
                                ClientModeImpl.this.handleUnwantedNetworkInWifiPro(config, message.arg1);
                                break;
                            }
                        }
                    } else {
                        ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 3);
                        ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                        ClientModeImpl clientModeImpl = ClientModeImpl.this;
                        clientModeImpl.transitionTo(clientModeImpl.mDisconnectingState);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_START_ROAM /* 131217 */:
                    ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    WifiConfiguration config2 = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    if (config2 != null) {
                        ClientModeImpl.this.mWifiScoreCard.noteConnectionAttempt(ClientModeImpl.this.mWifiInfo);
                        ClientModeImpl.this.setTargetBssid(config2, bssid);
                        ClientModeImpl.this.mTargetNetworkId = netId;
                        ClientModeImpl.this.logd("CMD_START_ROAM sup state " + ClientModeImpl.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + ClientModeImpl.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " ssid " + StringUtilEx.safeDisplaySsid(config2.getPrintableSsid()) + " targetRoamBSSID " + StringUtilEx.safeDisplayBssid(ClientModeImpl.this.mTargetRoamBSSID));
                        ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                        clientModeImpl2.reportConnectionAttemptStart(config2, clientModeImpl2.mTargetRoamBSSID, 3);
                        if (!ClientModeImpl.this.mWifiNative.roamToNetwork(ClientModeImpl.this.mInterfaceName, config2)) {
                            ClientModeImpl.this.loge("CMD_START_ROAM Failed to start roaming to network " + config2);
                            ClientModeImpl.this.reportConnectionAttemptEnd(5, 1, 0);
                            ClientModeImpl.this.replyToMessage(message, 151554, 0);
                            ClientModeImpl.this.mMessageHandlingStatus = -2;
                            break;
                        } else {
                            ClientModeImpl clientModeImpl3 = ClientModeImpl.this;
                            clientModeImpl3.mLastConnectAttemptTimestamp = clientModeImpl3.mClock.getWallClockMillis();
                            ClientModeImpl.this.mTargetWifiConfiguration = config2;
                            ClientModeImpl.this.mIsAutoRoaming = true;
                            ClientModeImpl.this.mWifiMetrics.logStaEvent(12, config2);
                            ClientModeImpl clientModeImpl4 = ClientModeImpl.this;
                            clientModeImpl4.transitionTo(clientModeImpl4.mRoamingState);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }
                case ClientModeImpl.CMD_ASSOCIATED_BSSID /* 131219 */:
                    ClientModeImpl clientModeImpl5 = ClientModeImpl.this;
                    clientModeImpl5.mLastDriverRoamAttempt = clientModeImpl5.mClock.getWallClockMillis();
                    ClientModeImpl.this.notifyWifiRoamingStarted();
                    handleStatus = false;
                    break;
                case ClientModeImpl.CMD_NETWORK_STATUS /* 131220 */:
                    if (message.arg1 == 1) {
                        ClientModeImpl.this.removeMessages(ClientModeImpl.CMD_DIAGS_CONNECT_TIMEOUT);
                        ClientModeImpl.this.mWifiDiagnostics.reportConnectionEvent((byte) 1);
                        ClientModeImpl.this.mWifiScoreCard.noteValidationSuccess(ClientModeImpl.this.mWifiInfo);
                        WifiConfiguration config3 = ClientModeImpl.this.getCurrentWifiConfiguration();
                        if (config3 != null) {
                            ClientModeImpl.this.handleValidNetworkInWifiPro(config3);
                            ClientModeImpl.this.mWifiConfigManager.updateNetworkSelectionStatus(config3.networkId, 0);
                            ClientModeImpl.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config3.networkId, true);
                            break;
                        }
                    }
                    break;
                case ClientModeImpl.CMD_ACCEPT_UNVALIDATED /* 131225 */:
                    if (message.arg1 != 0) {
                        accept = true;
                    }
                    ClientModeImpl.this.mWifiConfigManager.setNetworkNoInternetAccessExpected(ClientModeImpl.this.mLastNetworkId, accept);
                    break;
                case ClientModeImpl.CMD_SET_DETECTMODE_CONF /* 131772 */:
                    ClientModeImpl.this.processSetVoWifiDetectMode(message);
                    break;
                case ClientModeImpl.CMD_SET_DETECT_PERIOD /* 131773 */:
                    ClientModeImpl.this.processSetVoWifiDetectPeriod(message);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    ClientModeImpl.this.reportConnectionAttemptEnd(6, 1, 0);
                    if (ClientModeImpl.this.mLastDriverRoamAttempt != 0) {
                        long lastRoam = ClientModeImpl.this.mClock.getWallClockMillis() - ClientModeImpl.this.mLastDriverRoamAttempt;
                        ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    }
                    if (ClientModeImpl.unexpectedDisconnectedReason(message.arg2)) {
                        ClientModeImpl.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    WifiConfiguration config4 = ClientModeImpl.this.getCurrentWifiConfiguration();
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl clientModeImpl6 = ClientModeImpl.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=");
                        sb.append(StringUtilEx.safeDisplayBssid(ClientModeImpl.this.mWifiInfo.getBSSID()));
                        sb.append(" RSSI=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getRssi());
                        sb.append(" freq=");
                        sb.append(ClientModeImpl.this.mWifiInfo.getFrequency());
                        sb.append(" reason=");
                        sb.append(message.arg2);
                        sb.append(" Network Selection Status=");
                        if (config4 == null) {
                            str2 = "Unavailable";
                        } else {
                            str2 = config4.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        sb.append(str2);
                        clientModeImpl6.log(sb.toString());
                        break;
                    }
                    break;
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /* 147520 */:
                    ClientModeImpl.this.logd("receive Vo WifiDetect event 1");
                    if (this.mSourceMessage != null) {
                        ClientModeImpl.this.logd("receive Vo WifiDetect event 2");
                        ClientModeImpl.this.replyToMessage(this.mSourceMessage, 151576);
                        break;
                    }
                    break;
                case 151575:
                    ClientModeImpl.this.logd("start VoWifiDetect ");
                    this.mSourceMessage = Message.obtain(message);
                    break;
                default:
                    if (!ClientModeImpl.this.mHwWifiStateMachineEx.handleHwPrivateMsgInConnectedState(message)) {
                        handleStatus = false;
                        break;
                    }
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            ClientModeImpl.this.logd("ClientModeImpl: Leaving Connected state");
            ClientModeImpl.this.processStatistics(1);
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
            ClientModeImpl.this.notifyWlanState(WifiCommonUtils.STATE_DISCONNECTED);
            Settings.Global.putInt(ClientModeImpl.this.mContext.getContentResolver(), "captive_portal_notification_shown", 0);
            Log.i(ClientModeImpl.TAG, "disconnect, change CAPTIVE_PORTAL_NOTIFICATION_SHOWN to 0");
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl.this.logd(" Enter DisconnectingState State screenOn=" + ClientModeImpl.this.mScreenOn);
            }
            ClientModeImpl.this.mDisconnectingWatchdogCount++;
            ClientModeImpl.this.logd("Start Disconnecting Watchdog " + ClientModeImpl.this.mDisconnectingWatchdogCount);
            ClientModeImpl clientModeImpl = ClientModeImpl.this;
            clientModeImpl.sendMessageDelayed(clientModeImpl.obtainMessage(ClientModeImpl.CMD_DISCONNECTING_WATCHDOG_TIMER, clientModeImpl.mDisconnectingWatchdogCount, 0), RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            switch (message.what) {
                case ClientModeImpl.CMD_SET_OPERATIONAL_MODE /* 131144 */:
                    if (message.arg1 == 1) {
                        if (ClientModeImpl.this.hasDeferredMessagesForArg1(ClientModeImpl.CMD_SET_OPERATIONAL_MODE, 4)) {
                            ClientModeImpl.this.log("Has deferred DISABLED_MODE, deffer CONNECT_MODE");
                            ClientModeImpl.this.deferMessage(message);
                            break;
                        }
                    } else {
                        ClientModeImpl.this.deferMessage(message);
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_DISCONNECT /* 131145 */:
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.log("Ignore CMD_DISCONNECT when already disconnecting.");
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_DISCONNECTING_WATCHDOG_TIMER /* 131168 */:
                    if (ClientModeImpl.this.mDisconnectingWatchdogCount == message.arg1) {
                        if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                            ClientModeImpl.this.log("disconnecting watchdog! -> disconnect");
                        }
                        ClientModeImpl.this.handleNetworkDisconnect();
                        ClientModeImpl clientModeImpl = ClientModeImpl.this;
                        clientModeImpl.transitionTo(clientModeImpl.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    ClientModeImpl.this.handleNetworkDisconnect();
                    ClientModeImpl clientModeImpl2 = ClientModeImpl.this;
                    clientModeImpl2.transitionTo(clientModeImpl2.mDisconnectedState);
                    break;
                case 151553:
                    ClientModeImpl.this.mMessageHandlingStatus = ClientModeImpl.MESSAGE_HANDLING_STATUS_DEFERRED;
                    ClientModeImpl.this.deferMessage(message);
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            Log.i(ClientModeImpl.TAG, "disconnectedstate enter");
            boolean unused = ClientModeImpl.flagIpv4Provisioned = false;
            if (ClientModeImpl.DBG) {
                ClientModeImpl.this.log(getName());
            }
            if (ClientModeImpl.this.mTemporarilyDisconnectWifi) {
                ClientModeImpl.this.p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                ClientModeImpl clientModeImpl = ClientModeImpl.this;
                clientModeImpl.logd(" Enter DisconnectedState screenOn=" + ClientModeImpl.this.mScreenOn);
            }
            ClientModeImpl.this.handleDisconnectedInWifiPro();
            ClientModeImpl.this.mIsAutoRoaming = false;
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
        }

        public boolean processMessage(Message message) {
            boolean handleStatus = true;
            boolean z = false;
            switch (message.what) {
                case ClientModeImpl.CMD_DISCONNECT /* 131145 */:
                    ClientModeImpl.this.mWifiMetrics.logStaEvent(15, 2);
                    ClientModeImpl.this.mWifiNative.disconnect(ClientModeImpl.this.mInterfaceName);
                    break;
                case ClientModeImpl.CMD_RECONNECT /* 131146 */:
                case ClientModeImpl.CMD_REASSOCIATE /* 131147 */:
                    if (!ClientModeImpl.this.mTemporarilyDisconnectWifi) {
                        handleStatus = false;
                        break;
                    }
                    break;
                case ClientModeImpl.CMD_SCREEN_STATE_CHANGED /* 131167 */:
                    ClientModeImpl clientModeImpl = ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    clientModeImpl.handleScreenStateChanged(z);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /* 143371 */:
                    ClientModeImpl.this.mP2pConnected.set(((NetworkInfo) message.obj).isConnected());
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    if (message.arg2 == 15) {
                        ClientModeImpl.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(ClientModeImpl.this.getTargetSsid(), message.obj == null ? ClientModeImpl.this.mTargetRoamBSSID : (String) message.obj, 2);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (ClientModeImpl.this.mVerboseLoggingEnabled) {
                        ClientModeImpl.this.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    }
                    if (SupplicantState.isConnecting(stateChangeResult.state)) {
                        WifiConfiguration config = ClientModeImpl.this.mWifiConfigManager.getConfiguredNetwork(stateChangeResult.networkId);
                        ClientModeImpl.this.mWifiInfo.setFQDN(null);
                        ClientModeImpl.this.mWifiInfo.setOsuAp(false);
                        ClientModeImpl.this.mWifiInfo.setProviderFriendlyName(null);
                        if (config != null && (config.isPasspoint() || config.osu)) {
                            if (config.isPasspoint()) {
                                ClientModeImpl.this.mWifiInfo.setFQDN(config.FQDN);
                            } else {
                                ClientModeImpl.this.mWifiInfo.setOsuAp(true);
                            }
                            ClientModeImpl.this.mWifiInfo.setProviderFriendlyName(config.providerFriendlyName);
                        }
                    }
                    ClientModeImpl.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    handleStatus = false;
                    break;
                default:
                    handleStatus = false;
                    break;
            }
            if (handleStatus) {
                ClientModeImpl.this.logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        public void exit() {
            ClientModeImpl.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what, Object obj) {
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
    /* access modifiers changed from: public */
    private void broadcastWifiCredentialChanged(int wifiCredentialEventType, WifiConfiguration config) {
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
        WifiConfiguration wifiConfiguration = this.mTargetWifiConfiguration;
        if (wifiConfiguration == null || wifiConfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            String response = TelephonyUtil.getGsmSimAuthResponse(requestData.data, getTelephonyManager());
            if (response == null && (response = TelephonyUtil.getGsmSimpleSimAuthResponse(requestData.data, getTelephonyManager())) == null) {
                response = TelephonyUtil.getGsmSimpleSimNoLengthAuthResponse(requestData.data, getTelephonyManager());
            }
            if (response == null || response.length() == 0) {
                this.mWifiNative.simAuthFailedResponse(this.mInterfaceName, requestData.networkId);
                return;
            }
            logv("Supplicant Response -" + response);
            this.mWifiNative.simAuthResponse(this.mInterfaceName, requestData.networkId, WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    /* access modifiers changed from: package-private */
    public void handle3GAuthRequest(TelephonyUtil.SimAuthRequestData requestData) {
        WifiConfiguration wifiConfiguration = this.mTargetWifiConfiguration;
        if (wifiConfiguration == null || wifiConfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            TelephonyUtil.SimAuthResponseData response = TelephonyUtil.get3GAuthResponse(requestData, getTelephonyManager());
            if (response != null) {
                this.mWifiNative.simAuthResponse(this.mInterfaceName, requestData.networkId, response.type, response.response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(this.mInterfaceName, requestData.networkId);
            }
        } else {
            logd("id does not match targetWifiConfiguration");
        }
    }

    public void startConnectToNetwork(int networkId, int uid, String bssid) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(CONNECT_FROM_USER, false);
        bundle.putString(BSSID_TO_CONNECT, bssid);
        sendMessage(CMD_START_CONNECT, networkId, uid, bundle);
    }

    public void startConnectToUserSelectNetwork(int networkId, int uid, String bssid) {
        WifiNetworkFactory wifiNetworkFactory;
        Bundle bundle = new Bundle();
        bundle.putBoolean(CONNECT_FROM_USER, true);
        bundle.putString(BSSID_TO_CONNECT, bssid);
        if (this.mNetworkAgent != null || hasConnectionRequests() || (wifiNetworkFactory = this.mNetworkFactory) == null || wifiNetworkFactory.hasMessages(536576)) {
            sendMessage(CMD_START_CONNECT, networkId, uid, bundle);
            return;
        }
        Log.w(TAG, "delay connect request");
        sendMessageDelayed(CMD_START_CONNECT, networkId, uid, bundle, 50);
    }

    public boolean isFirstConnect() {
        return this.mIsFirstConnect;
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_START_ROAM, networkId, 0, scanResult);
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        sendMessage(CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER, enabled ? 1 : 0);
    }

    static boolean unexpectedDisconnectedReason(int reason) {
        return reason == 2 || reason == 6 || reason == 7 || reason == 8 || reason == 9 || reason == 14 || reason == 15 || reason == 16 || reason == 18 || reason == 19 || reason == 23 || reason == 34;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean disassociatedReason(int reason) {
        return reason == 2 || reason == 4 || reason == 5 || reason == 8 || reason == 23 || reason == 34;
    }

    public int getWifiApTypeFromMpLink() {
        return 0;
    }

    public void updateWifiMetrics() {
        this.mWifiMetrics.updateSavedNetworks(this.mWifiConfigManager.getSavedNetworks(1010));
        this.mPasspointManager.updateMetrics();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
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
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, message.what, -1);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkUpdateResult saveNetworkConfigAndSendReply(Message message) {
        WifiConfiguration config = (WifiConfiguration) message.obj;
        if (config == null) {
            loge("SAVE_NETWORK with null configuration " + this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + getCurrentState().getName());
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return new NetworkUpdateResult(-1);
        }
        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
        if (!result.isSuccess()) {
            loge("SAVE_NETWORK adding/updating config=" + config + " failed");
            this.mMessageHandlingStatus = -2;
            replyToMessage(message, 151560, 0);
            return result;
        } else if (!this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, message.sendingUid)) {
            loge("SAVE_NETWORK enabling config=" + config + " failed");
            this.mMessageHandlingStatus = -2;
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
    /* access modifiers changed from: public */
    private String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean p2pSendMessage(int what) {
        AsyncChannel asyncChannel = this.mWifiP2pChannel;
        if (asyncChannel == null) {
            return false;
        }
        asyncChannel.sendMessage(what);
        return true;
    }

    private boolean p2pSendMessage(int what, int arg1) {
        AsyncChannel asyncChannel = this.mWifiP2pChannel;
        if (asyncChannel == null) {
            return false;
        }
        asyncChannel.sendMessage(what, arg1);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasConnectionRequests() {
        return this.mNetworkFactory.hasConnectionRequests() || this.mUntrustedNetworkFactory.hasConnectionRequests();
    }

    public boolean getIpReachabilityDisconnectEnabled() {
        return this.mIpReachabilityDisconnectEnabled;
    }

    public void setIpReachabilityDisconnectEnabled(boolean enabled) {
        this.mIpReachabilityDisconnectEnabled = enabled;
    }

    public boolean syncInitialize(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously((int) CMD_INITIALIZE);
        boolean result = false;
        if (messageIsNull(resultMsg)) {
            return false;
        }
        if (resultMsg.arg1 != -1) {
            result = true;
        }
        resultMsg.recycle();
        return result;
    }

    public boolean disableWifiFilter() {
        return this.mWifiNative.mHwWifiNativeEx.stopRxFilter(this.mInterfaceName);
    }

    public boolean enableWifiFilter() {
        return this.mWifiNative.mHwWifiNativeEx.startRxFilter(this.mInterfaceName);
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
        this.mFeatureSet = (long) featureSet;
    }

    public void saveLastNetIdForAp() {
        ExtendedWifiInfo extendedWifiInfo = this.mWifiInfo;
        if (extendedWifiInfo != null) {
            extendedWifiInfo.setLastNetIdForAp(extendedWifiInfo.getNetworkId());
        }
    }

    public void clearLastNetIdForAp() {
        ExtendedWifiInfo extendedWifiInfo = this.mWifiInfo;
        if (extendedWifiInfo != null) {
            extendedWifiInfo.setLastNetIdForAp(-1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLastConnectConfig(WifiConfiguration config) {
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

    public void addNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        this.mNetworkFactory.addCallback(binder, callback, callbackIdentifier);
    }

    public void removeNetworkRequestMatchCallback(int callbackIdentifier) {
        this.mNetworkFactory.removeCallback(callbackIdentifier);
    }

    public void removeNetworkRequestUserApprovedAccessPointsForApp(String packageName) {
        this.mNetworkFactory.removeUserApprovedAccessPointsForApp(packageName);
    }

    public void clearNetworkRequestUserApprovedAccessPoints() {
        this.mNetworkFactory.clear();
    }

    public String getFactoryMacAddress() {
        MacAddress macAddress = this.mWifiNative.getFactoryMacAddress(this.mInterfaceName);
        if (macAddress != null) {
            return macAddress.toString();
        }
        if (!this.mConnectedMacRandomzationSupported) {
            return this.mWifiNative.getMacAddress(this.mInterfaceName);
        }
        return null;
    }

    public void setDeviceMobilityState(int state) {
        this.mWifiConnectivityManager.setDeviceMobilityState(state);
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public void hwSendNetworkStateChangeBroadcast(String bssid) {
        sendNetworkStateChangeBroadcast(bssid);
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public boolean hwSetNetworkDetailedState(NetworkInfo.DetailedState state) {
        return setNetworkDetailedState(state);
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public void hwSendConnectedState() {
        sendConnectedState();
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public void hwStopIpClient() {
        stopIpClient();
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public void hwHandleNetworkDisconnect() {
        handleNetworkDisconnect();
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public LinkProperties getLinkProperties() {
        return this.mLinkProperties;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public IpClientManager getIpClient() {
        return this.mIpClient;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public State getObtainingIpState() {
        return this.mObtainingIpState;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public WifiNative getWifiNative() {
        return this.mWifiNative;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public State getConnectedState() {
        return this.mConnectedState;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public State getDisconnectedState() {
        return this.mDisconnectedState;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public NetworkAgent getNetworkAgent() {
        return this.mNetworkAgent;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public NetworkAgent getNewWifiNetworkAgent() {
        return new WifiNetworkAgent(getHandler().getLooper(), this.mContext, "WifiNetworkAgent", this.mNetworkInfo, this.mNetworkCapabilitiesFilter, this.mLinkProperties, 100, this.mNetworkMisc);
    }

    @Override // com.android.server.wifi.IHwWifiStateMachineInner
    public String getLastBssid() {
        return this.mLastBssid;
    }

    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        this.mWifiMetrics.incrementWifiUsabilityScoreCount(seqNum, score, predictionHorizonSec);
    }

    @VisibleForTesting
    public void probeLink(WifiNative.SendMgmtFrameCallback callback, int mcs) {
        this.mWifiNative.probeLink(this.mInterfaceName, MacAddress.fromString(this.mWifiInfo.getBSSID()), callback, mcs);
    }

    public boolean shouldUseFactoryMac(WifiConfiguration config) {
        return false;
    }

    public void updateRandomizedMacConfigWhenConnected(String iface, String bssid) {
    }

    public void updateRandomizedMacConfigWhenDisconnected(int networkId, int reasonCode) {
    }

    public void handleWpa3ConnectFailReport(String eventInfo) {
    }

    public void notifyWpa3SelfCureConnectSucc() {
    }

    public boolean isDisallowedSelfRecovery() {
        return false;
    }

    public boolean isWifiInObtainingIpState() {
        return false;
    }

    public boolean doArpTest(int type, Inet4Address address) {
        return false;
    }

    public boolean isNeedIgnoreScan() {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadDhcpState(DhcpResults dhcpResults) {
        if (this.mHwWifiCHRService != null && dhcpResults.ipAddress != null && this.mWifiConfigManager != null && this.mNetworkInfo != null) {
            WifiConfiguration currentConfig = getCurrentWifiConfiguration();
            if (currentConfig != null && currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                this.mHwWifiCHRService.updateDhcpState(9);
            } else if (this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                if (TYPE_GET_CACHE_DHCP_RESULT.equals(dhcpResults.domains)) {
                    this.mHwWifiCHRService.updateDhcpState(16);
                } else {
                    this.mHwWifiCHRService.updateDhcpState(2);
                }
            } else if (this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                this.mHwWifiCHRService.updateDhcpState(3);
            }
        }
    }

    private void notifyScreenState(boolean isScreenOn) {
        if (this.mInterfaceName != null) {
            logd("apf dbg, set screen state to driver, screenOn=" + isScreenOn);
            if (isScreenOn) {
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(this.mInterfaceName, CMD_SET_SCREEN_STATE_PARAM, new byte[]{89});
            } else {
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(this.mInterfaceName, CMD_SET_SCREEN_STATE_PARAM, new byte[]{78});
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSupportedWifiCategory() {
        if (this.mLastBssid != null) {
            int wifiCategory = ScanResultRecords.getDefault().getWifiCategory(this.mLastBssid);
            logd("setSupportedWifiCategory, wifi category is " + wifiCategory);
            this.mWifiInfo.setSupportedWifiCategory(wifiCategory);
        }
    }

    public boolean isScreenOn() {
        return this.mScreenOn;
    }

    public String selectBestCandidate(WifiConfiguration config) {
        return "any";
    }

    public void sendWifiStateBroadcast(int newState, int currentState) {
        Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", newState);
        intent.putExtra("previous_wifi_state", currentState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
