package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.dhcp.DhcpClient;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.ip.IpManager.ProvisioningConfiguration;
import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.WpsResult.Status;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiBackupRestore.SupplicantBackupMigration;
import com.android.server.wifi.WifiMulticastLockManager.FilterController;
import com.android.server.wifi.WifiNative.SignalPollResult;
import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.android.server.wifi.WifiNative.VendorHalDeathEventHandler;
import com.android.server.wifi.WifiNative.WifiRssiEventHandler;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.android.server.wifi.util.ApConfigUtil;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.StringUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.TelephonyUtil.SimAuthRequestData;
import com.android.server.wifi.util.TelephonyUtil.SimAuthResponseData;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.util.WifiPermissionsUtil;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiStateMachine extends AbsWifiStateMachine implements WifiRssiEventHandler, FilterController {
    private static final int ADD_OR_UPDATE_SOURCE = -3;
    private static final long ALLOW_SEND_HILINK_SCAN_RESULTS_BROADCAST_INTERVAL_MS = 3000;
    private static final int ALLOW_SEND_HILINK_SCAN_RESULTS_BROADCAST_TRIES = 10;
    static final int BASE = 131072;
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG = 131178;
    static final int CMD_AP_STARTED_GET_STA_LIST = 131104;
    static final int CMD_AP_STARTED_SET_DISASSOCIATE_STA = 131106;
    static final int CMD_AP_STARTED_SET_MAC_FILTER = 131105;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    static final int CMD_BOOT_COMPLETED = 131206;
    public static final int CMD_CHANGE_TO_AP_P2P_CONNECT = 131574;
    public static final int CMD_CHANGE_TO_STA_P2P_CONNECT = 131573;
    private static final int CMD_CLIENT_INTERFACE_BINDER_DEATH = 131322;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    static final int CMD_DELAYED_NETWORK_DISCONNECT = 131159;
    private static final int CMD_DIAGS_CONNECT_TIMEOUT = 131324;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    public static final int CMD_DISABLE_P2P_REQ = 131204;
    public static final int CMD_DISABLE_P2P_RSP = 131205;
    static final int CMD_DISABLE_P2P_WATCHDOG_TIMER = 131184;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_DRIVER_START_TIMED_OUT = 131091;
    static final int CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED = 131239;
    static final int CMD_ENABLE_NETWORK = 131126;
    public static final int CMD_ENABLE_P2P = 131203;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_FIRMWARE_ALERT = 131172;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_CONNECTION_STATISTICS = 131148;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_CONFIG = 131171;
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
    static final int CMD_NO_NETWORKS_PERIODIC_SCAN = 131160;
    static final int CMD_PNO_PERIODIC_SCAN = 131575;
    static final int CMD_QUERY_OSU_ICON = 131176;
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
    static final int CMD_RSSI_THRESHOLD_BREACH = 131236;
    static final int CMD_SAVE_CONFIG = 131130;
    public static final int CMD_SCE_HANDLE_IP_INVALID = 131895;
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
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_START_CONNECT = 131215;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_ROAM = 131217;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    static final int CMD_START_SCAN = 131143;
    static final int CMD_START_SUPPLICANT = 131083;
    static final int CMD_STATIC_IP_FAILURE = 131088;
    static final int CMD_STATIC_IP_SUCCESS = 131087;
    static final int CMD_STOP_AP = 131095;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    static final int CMD_STOP_SUPPLICANT = 131084;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    static final int CMD_TARGET_BSSID = 131213;
    static final int CMD_TEST_NETWORK_DISCONNECT = 131161;
    static final int CMD_UNWANTED_NETWORK = 131216;
    static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    public static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    static final int CMD_USER_STOP = 131279;
    static final int CMD_USER_SWITCH = 131277;
    static final int CMD_USER_UNLOCK = 131278;
    private static final int CMD_VENDOR_HAL_HWBINDER_DEATH = 131323;
    public static final int CMD_WIFI_SCAN_REJECT_SEND_SCAN_RESULT = 131578;
    static final int CMD_WPS_PIN_RETRY = 131576;
    public static final int CONNECT_MODE = 1;
    private static final String CUSTOMIZED_SCAN_SETTING = "customized_scan_settings";
    private static final String CUSTOMIZED_SCAN_WORKSOURCE = "customized_scan_worksource";
    private static boolean DBG = HWFLOW;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60000;
    public static final int DISABLED_MODE = 4;
    static final int DISABLE_P2P_GUARD_TIMER_MSEC = 2000;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final int DRIVER_STARTED = 1;
    private static final int DRIVER_STOPPED = 2;
    private static final boolean ENABLE_DHCP_AFTER_ROAM = SystemProperties.getBoolean("ro.config.roam_force_dhcp", false);
    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final int FAILURE = -1;
    public static final int GOOD_LINK_DETECTED = 131874;
    private static final String GOOGLE_OUI = "DA-A1-19";
    private static final String HILINK_STATE_CHANGE_ACTION = "com.android.server.wifi.huawei.action.NETWORK_CONNECTED";
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    private static final int IMSI_RECONNECT_LIMIT = 3;
    private static final long LAST_AUTH_FAILURE_GAP = 100;
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
    private static final boolean NOTIFY_OPEN_NETWORKS_VALUE = SystemProperties.getBoolean("ro.config.notify_open_networks", false);
    public static final short NUM_LOG_RECS_NORMAL = (short) 100;
    public static final short NUM_LOG_RECS_VERBOSE = (short) 3000;
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = (short) 200;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static boolean PDBG = HWFLOW;
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final int POLL_RSSI_INTERVAL_MSECS = 3000;
    public static final int POOR_LINK_DETECTED = 131873;
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    public static final int SCAN_ONLY_MODE = 2;
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
    private static final int SCAN_REQUEST_BUFFER_MAX_SIZE = 10;
    private static final String SCAN_REQUEST_TIME = "scan_request_time";
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
    private static final int UNKNOWN_SCAN_SOURCE = -1;
    private static boolean USE_PAUSE_SCANS = false;
    private static final String VALUE_DISABLE = "value_disable";
    private static boolean VDBG = false;
    private static boolean VVDBG = false;
    public static final int WIFIPRO_SOFT_CONNECT_TIMEOUT = 131897;
    private static final String WIFI_DRIVER_CHANGE_ACTION = "huawei.intent.action.WIFI_DRIVER_CHANGE";
    private static final String WIFI_DRIVER_CHANGE_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final String WIFI_DRIVER_STATE = "wifi_driver_state";
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static final int WPS_PIN_RETRY_INTERVAL_MSECS = 50000;
    private static boolean mLogMessages = HWFLOW;
    private static final Class[] sMessageClasses = new Class[]{AsyncChannel.class, WifiStateMachine.class, DhcpClient.class};
    private static int sScanAlarmIntentCount = 0;
    private static final SparseArray<String> sSmToString = MessageUtils.findMessageNames(sMessageClasses);
    private boolean didBlackListBSSID = false;
    int disconnectingWatchdogCount = 0;
    private boolean isBootCompleted = false;
    private long lastConnectAttemptTimestamp = 0;
    private long lastLinkLayerStatsUpdate = 0;
    private long lastOntimeReportTimeStamp = 0;
    private Set<Integer> lastScanFreqs = null;
    private long lastScreenStateChangeTimeStamp = 0;
    private int mAggressiveHandover = 0;
    private boolean mAllowSendHiLinkScanResultsBroadcast = false;
    private int mAlwaysEnableScansWhileAssociated;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    private boolean mBluetoothConnectionActive = false;
    private final Queue<Message> mBufferedScanMsg = new LinkedList();
    private final BuildProperties mBuildProperties;
    private IClientInterface mClientInterface;
    private final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    private State mConnectedState = new ConnectedState();
    private long mConnectingStartTimestamp = 0;
    @GuardedBy("mWifiReqCountLock")
    private int mConnectionReqCount = 0;
    private Context mContext;
    private final WifiCountryCode mCountryCode;
    private int mCurrentAssociateNetworkId = -1;
    HwCustWifiStateMachineReference mCust = ((HwCustWifiStateMachineReference) HwCustUtils.createObj(HwCustWifiStateMachineReference.class, new Object[0]));
    private final StateMachineDeathRecipient mDeathRecipient = new StateMachineDeathRecipient(this, CMD_CLIENT_INTERFACE_BINDER_DEATH);
    private State mDefaultState = new DefaultState();
    private final NetworkCapabilities mDfltNetworkCapabilities;
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private long mDiagsConnectionStartMillis = -1;
    int mDisableP2pWatchdogCount = 0;
    private State mDisconnectedState = new DisconnectedState();
    private long mDisconnectedTimeStamp = 0;
    private State mDisconnectingState = new DisconnectingState();
    private int mEmptyScanResultCount = 0;
    private boolean mEnableAutoJoinWhenAssociated;
    private final boolean mEnableChipWakeUpWhenAssociated;
    private final boolean mEnableLinkDebouncing;
    private final boolean mEnableRssiPollWhenAssociated;
    private boolean mEnableRssiPolling = false;
    private FrameworkFacade mFacade;
    private int mFeatureSet = 0;
    private long mHilinkLastHashCode = 0;
    private long mHilinkLastLevelCode = 0;
    private HwMSSHandlerManager mHwMssHandler;
    private HwWifiCHRService mHwWifiCHRService;
    private HwWifiDFTUtil mHwWifiDFTUtil;
    private State mInitialState = new InitialState();
    private final String mInterfaceName;
    private final IpManager mIpManager;
    private boolean mIpReachabilityDisconnectEnabled = true;
    private boolean mIsAutoRoaming = false;
    private boolean mIsFullScanOngoing = false;
    private boolean mIsImsiAvailable = true;
    private boolean mIsLinkDebouncing = false;
    public boolean mIsRandomMacCleared = false;
    private boolean mIsRealReboot = false;
    private boolean mIsRunning = false;
    private boolean mIsScanOngoing = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private long mLastAllowSendHiLinkScanResultsBroadcastTime = 0;
    private long mLastAuthFailureTimestamp = Long.MIN_VALUE;
    private String mLastBssid;
    private long mLastDriverRoamAttempt = 0;
    private int mLastNetworkId;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    private int mLastSignalLevel = -1;
    private LinkProperties mLinkProperties;
    private WifiNetworkAgent mNetworkAgent;
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private WifiNetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc = new NetworkMisc();
    private final int mNoNetworksPeriodicScan;
    private int mNumScanResultsKnown;
    private int mNumScanResultsReturned;
    private INetworkManagementService mNwService;
    private State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    private int mOperationalMode = 1;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    private final PasspointManager mPasspointManager;
    private int mPeriodicScanToken = 0;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    private int mRoamFailCount = 0;
    private State mRoamingState = new RoamingState();
    private int mRssiPollToken = 0;
    private byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    private State mScanModeState = new ScanModeState();
    private List<ScanDetail> mScanResults = new ArrayList();
    private final Object mScanResultsLock = new Object();
    private boolean mScreenOn = false;
    private int mSendHiLinkScanResultsBroadcastTries = 0;
    private boolean mSendScanResultsBroadcast = false;
    private State mSoftApState = new SoftApState();
    private int mSupplicantRestartCount = 0;
    private long mSupplicantScanIntervalMs;
    private State mSupplicantStartedState = new SupplicantStartedState();
    private State mSupplicantStartingState = new SupplicantStartingState();
    private SupplicantStateTracker mSupplicantStateTracker;
    private State mSupplicantStoppingState = new SupplicantStoppingState();
    private int mSuspendOptNeedsDisabled = 0;
    private WakeLock mSuspendWakeLock;
    private int mTargetNetworkId = -1;
    private String mTargetRoamBSSID = "any";
    private final String mTcpBufferSizes;
    private TelephonyManager mTelephonyManager;
    private boolean mTemporarilyDisconnectWifi = false;
    private final int mThresholdMinimumRssi24;
    private final int mThresholdMinimumRssi5;
    private final int mThresholdQualifiedRssi24;
    private final int mThresholdQualifiedRssi5;
    private final int mThresholdSaturatedRssi24;
    private final int mThresholdSaturatedRssi5;
    private String mTls12ConfKey = null;
    private int mTrackEapAuthFailCount = 0;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    @GuardedBy("mWifiReqCountLock")
    private int mUntrustedReqCount = 0;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    private final VendorHalDeathEventHandler mVendorHalDeathRecipient = new -$Lambda$YuIVlKWZZmb4gGMvJqVJEVQ4abs(this);
    private boolean mVerboseLoggingEnabled = false;
    private int mVerboseLoggingLevel = 0;
    private State mWaitForP2pDisableState = new WaitForP2pDisableState();
    private WakeLock mWakeLock;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private WifiApConfigStore mWifiApConfigStore;
    private final AtomicInteger mWifiApState = new AtomicInteger(11);
    private WifiConfigManager mWifiConfigManager;
    private WifiConnectionStatistics mWifiConnectionStatistics = new WifiConnectionStatistics();
    protected WifiConnectivityManager mWifiConnectivityManager;
    private BaseWifiDiagnostics mWifiDiagnostics;
    private final WifiInfo mWifiInfo;
    private WifiInjector mWifiInjector;
    private int mWifiLinkLayerStatsSupported = 4;
    private WifiMetrics mWifiMetrics;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative;
    private AsyncChannel mWifiP2pChannel;
    private WifiP2pServiceImpl mWifiP2pServiceImpl;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiRepeater mWifiRepeater;
    private final Object mWifiReqCountLock = new Object();
    private WifiScanner mWifiScanner;
    private final WifiScoreReport mWifiScoreReport;
    private WifiSettingsStore mWifiSettingStore;
    private HwWifiStatStore mWifiStatStore;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    public WifiStateMachineHisiExt mWifiStateMachineHisiExt = null;
    private WifiStateTracker mWifiStateTracker;
    private State mWpsRunningState = new WpsRunningState();
    private int messageHandlingStatus = 0;
    int roamWatchdogCount = 0;
    private WifiConfiguration targetWificonfiguration = null;
    private boolean testNetworkDisconnect = false;
    private int testNetworkDisconnectCounter = 0;
    private DataUploader uploader;

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                WifiStateMachine.this.loge("Failed to remove networks on entering connect mode");
            }
            WifiStateMachine.this.mWifiInfo.reset();
            WifiStateMachine.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            WifiStateMachine.this.setWifiState(3);
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(true);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
            WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
        }

        public void exit() {
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(false);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(1);
            if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                WifiStateMachine.this.loge("Failed to remove networks on exiting connect mode");
            }
            WifiStateMachine.this.mWifiInfo.reset();
            WifiStateMachine.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
        }

        /* JADX WARNING: Missing block: B:173:0x09b1, code:
            if ((r37 ^ 1) == 0) goto L_0x09b3;
     */
        /* JADX WARNING: Missing block: B:387:0x1862, code:
            if (com.android.server.wifi.WifiStateMachine.-wrap9(r42.this$0, com.android.server.wifi.WifiStateMachine.-get39(r42.this$0), r12) != false) goto L_0x1864;
     */
        /* JADX WARNING: Missing block: B:396:0x18fc, code:
            if (com.android.server.wifi.WifiStateMachine.-wrap9(r42.this$0, com.android.server.wifi.WifiStateMachine.-get39(r42.this$0), r12) != false) goto L_0x18fe;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            if (WifiStateMachine.this.handleWapiFailureEvent(message, WifiStateMachine.this.mSupplicantStateTracker)) {
                return true;
            }
            int netId;
            WifiConfiguration config;
            int i;
            Set<Integer> removedNetworkIds;
            String fqdn;
            String bssid;
            int reasonCode;
            WifiStateMachine wifiStateMachine;
            WifiConnectionStatistics -get85;
            NetworkUpdateResult result;
            switch (message.what) {
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    if (!WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, false)) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                        break;
                    }
                    netId = message.arg1;
                    if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    boolean ok;
                    boolean disableOthers = message.arg2 == 1;
                    netId = message.arg1;
                    config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(netId);
                    if (disableOthers) {
                        ok = WifiStateMachine.this.connectToUserSelectNetwork(netId, message.sendingUid, false);
                        WifiStateMachine.this.saveConnectingNetwork(config);
                    } else if (!WifiStateMachine.this.processConnectModeSetMode(message)) {
                        ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(netId, false, message.sendingUid);
                    }
                    if (!ok) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                    int i2 = message.what;
                    if (ok) {
                        i = 1;
                    } else {
                        i = -1;
                    }
                    wifiStateMachine2.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    WifiStateMachine.this.replyToMessage(message, WifiStateMachine.CMD_SAVE_CONFIG, WifiStateMachine.this.mWifiConfigManager.saveToStore(true) ? 1 : -1);
                    WifiStateMachine.this.mBackupManagerProxy.notifyDataChanged();
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                    WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan();
                    break;
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                    WifiStateMachine.this.log("ConnectModeState, case CMD_REASSOCIATE, do reassociate");
                    WifiStateMachine.this.mWifiNative.reassociate();
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                    config = WifiStateMachine.this.mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config != null && (config.networkId == WifiStateMachine.this.mTargetNetworkId || config.networkId == WifiStateMachine.this.mLastNetworkId)) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mPasspointManager.getMatchingWifiConfig((ScanResult) message.obj));
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                    WifiStateMachine.this.mPasspointManager.queryPasspointIcon(((Bundle) message.obj).getLong("BSSID"), ((Bundle) message.obj).getString(WifiStateMachine.EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, 0);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*131178*/:
                    PasspointConfiguration passpointConfig = message.obj;
                    if (!WifiStateMachine.this.mPasspointManager.addOrUpdateProvider(passpointConfig, message.arg1)) {
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    fqdn = passpointConfig.getHomeSp().getFqdn();
                    if (!WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mTargetNetworkId, fqdn)) {
                        break;
                    }
                    WifiStateMachine.this.logd("Disconnect from current network since its provider is updated");
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                    WifiStateMachine.this.replyToMessage(message, message.what, 1);
                    break;
                case WifiStateMachine.CMD_REMOVE_PASSPOINT_CONFIG /*131179*/:
                    fqdn = (String) message.obj;
                    if (!WifiStateMachine.this.mPasspointManager.removeProvider(fqdn)) {
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    if (!WifiStateMachine.this.isProviderOwnedNetwork(WifiStateMachine.this.mTargetNetworkId, fqdn)) {
                        break;
                    }
                    WifiStateMachine.this.logd("Disconnect from current network since its provider is removed");
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                    WifiStateMachine.this.replyToMessage(message, message.what, 1);
                    break;
                case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                    break;
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                    WifiConfiguration wifiConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (wifiConfig == null || wifiConfig.cloudSecurityCheck == 0) {
                        if (wifiConfig != null) {
                            if (wifiConfig.allowedKeyManagement.get(2)) {
                                i = wifiConfig.allowedKeyManagement.get(3);
                            } else {
                                i = 0;
                            }
                            break;
                        }
                        WifiStateMachine.this.log("currentWifiConfiguration is EAP type or no currentWifiConfiguration");
                        if (WifiStateMachine.this.mWifiConfigManager.needsUnlockedKeyStore() && (WifiStateMachine.this.isConnected() ^ 1) != 0) {
                            WifiStateMachine.this.logd("Reconnecting to give a chance to un-connected TLS networks");
                            WifiStateMachine.this.mWifiNative.disconnect();
                            WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                            WifiStateMachine.this.mWifiNative.reconnect();
                            break;
                        }
                    }
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                    if (!WifiStateMachine.this.isHiLinkActive()) {
                        boolean connectFromUser = message.arg2 == 1;
                        Log.d(WifiStateMachine.TAG, "connectFromUser =" + connectFromUser);
                        if (!WifiStateMachine.this.attemptAutoConnect() && (connectFromUser ^ 1) != 0) {
                            WifiStateMachine.this.logd("SupplicantState is TransientState, refuse auto connect");
                            break;
                        }
                        netId = message.arg1;
                        bssid = (String) message.obj;
                        config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                        WifiStateMachine.this.logd("CMD_START_CONNECT sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " roam=" + Boolean.toString(WifiStateMachine.this.mIsAutoRoaming));
                        if (config != null) {
                            if (!HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, false)) {
                                if (WifiStateMachine.this.isEnterpriseHotspot(config)) {
                                    WifiStateMachine.this.logd(config.SSID + "is enterprise hotspot ");
                                    WifiStateMachine.this.mTargetRoamBSSID = "any";
                                }
                                WifiStateMachine.this.mTargetNetworkId = netId;
                                WifiStateMachine.this.setTargetBssid(config, bssid);
                                WifiStateMachine.this.reportConnectionAttemptStart(config, WifiStateMachine.this.mTargetRoamBSSID, 5);
                                WifiStateMachine.this.saveConnectingNetwork(config);
                                if (!WifiStateMachine.this.mWifiNative.connectToNetwork(config)) {
                                    WifiStateMachine.this.loge("CMD_START_CONNECT Failed to start connection to network " + config);
                                    WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                    WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                    break;
                                }
                                WifiStateMachine.this.mWifiMetrics.logStaEvent(11, config);
                                WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                                WifiStateMachine.this.targetWificonfiguration = config;
                                WifiStateMachine.this.mIsAutoRoaming = false;
                                if (WifiStateMachine.this.isLinkDebouncing()) {
                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                                } else {
                                    if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                                    }
                                }
                                if (WifiStateMachine.this.mWiFiCHRManager != null) {
                                    WifiStateMachine.this.mWiFiCHRManager.updateAPSsid(config.SSID);
                                }
                                if (WifiStateMachine.this.mWifiStatStore != null) {
                                    WifiStateMachine.this.logd("CMD_START_CONNECT update connect total count");
                                    WifiStateMachine.this.mWifiStatStore.updateConnectCnt();
                                    break;
                                }
                            }
                            Log.w(WifiStateMachine.TAG, "CMD_START_CONNECT: MDM deny connect to restricted network!");
                            break;
                        }
                        WifiStateMachine.this.loge("CMD_START_CONNECT and no config, bail out...");
                        break;
                    }
                    Log.d(WifiStateMachine.TAG, "HiLink is active, refuse auto connect");
                    break;
                    break;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    String someBssid = message.obj;
                    if (someBssid != null) {
                        ScanDetailCache scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCacheForNetwork(WifiStateMachine.this.mTargetNetworkId);
                        if (scanDetailCache != null) {
                            WifiStateMachine.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.removeNetworksForUser(Integer.valueOf(message.arg1).intValue());
                    if (removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId)) || removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS /*131672*/:
                    WifiStateMachine.this.updateWifiproWifiConfiguration(message);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do reconnect");
                        WifiStateMachine.this.mWifiNative.reconnect();
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = false;
                        break;
                    }
                    WifiStateMachine.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 5);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection established");
                    }
                    WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiStateMachine.this.mHwWifiCHRService.updateWIFIConfiguraionByConfig(WifiStateMachine.this.getCurrentWifiConfiguration());
                    }
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    if (WifiStateMachine.this.mLastNetworkId == -1) {
                        NetworkUpdateResult networkUpdateResult = WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                        if (networkUpdateResult != null) {
                            WifiStateMachine.this.mLastNetworkId = networkUpdateResult.getNetworkId();
                        }
                    }
                    reasonCode = message.arg2;
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config == null) {
                        WifiStateMachine.this.logw("Connected to unknown networkId " + WifiStateMachine.this.mLastNetworkId + ", disconnecting...");
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, reasonCode);
                    WifiStateMachine.this.uploader.e(54, "{RT:6,SPEED:" + WifiStateMachine.this.mWifiInfo.getLinkSpeed() + "}");
                    if (!(config.isTempCreated || config.enterpriseConfig == null || !TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod()))) {
                        String anonymousIdentity = WifiStateMachine.this.mWifiNative.getEapAnonymousIdentity();
                        if (anonymousIdentity != null) {
                            config.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                        } else {
                            Log.d(WifiStateMachine.TAG, "Failed to get updated anonymous identity from supplicant, reset it in WifiConfiguration.");
                            config.enterpriseConfig.setAnonymousIdentity(null);
                        }
                        WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, 1010);
                    }
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.log("ConnectModeState, case WifiMonitor.NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("ConnectModeState: Network connection lost ");
                    }
                    if (WifiStateMachine.this.disassociatedReason(message.arg2)) {
                        Log.d(WifiStateMachine.TAG, "DISABLED_DISASSOC_REASON for network " + WifiStateMachine.this.mTargetNetworkId + " is " + message.arg2);
                        WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 15);
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message);
                    if (!SupplicantState.isDriverActive(state)) {
                        if (WifiStateMachine.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                            WifiStateMachine.this.handleNetworkDisconnect();
                        }
                        WifiStateMachine.this.log("Detected an interface down, restart driver");
                        WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_SUPPLICANT);
                        break;
                    }
                    StateChangeResult stateChangeResult;
                    if (!(WifiStateMachine.this.isLinkDebouncing() || state != SupplicantState.DISCONNECTED || WifiStateMachine.this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED)) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    }
                    if (state == SupplicantState.COMPLETED) {
                        WifiStateMachine.this.mIpManager.confirmConfiguration();
                    }
                    if (state == SupplicantState.ASSOCIATED) {
                        stateChangeResult = message.obj;
                        if (stateChangeResult != null) {
                            WifiStateMachine.this.mCurrentAssociateNetworkId = stateChangeResult.networkId;
                        }
                    }
                    stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult != null) {
                        int disconnectId = stateChangeResult.networkId;
                        WifiConfiguration disconnectConfig = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(disconnectId);
                        if (disconnectConfig != null && disconnectConfig.getNetworkSelectionStatus().isNetworkEnabled() && disconnectConfig.getNetworkSelectionStatus().getDisableReasonCounter(3) > 0 && WifiStateMachine.this.mClock.getElapsedSinceBootMillis() - WifiStateMachine.this.mLastAuthFailureTimestamp < WifiStateMachine.LAST_AUTH_FAILURE_GAP && (WifiStateMachine.this.isConnected() ^ 1) != 0 && state == SupplicantState.DISCONNECTED) {
                            Log.d(WifiStateMachine.TAG, "start an immediate connection for network " + disconnectId);
                            WifiStateMachine.this.startConnectToNetwork(disconnectId, "any");
                            break;
                        }
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 3);
                    WifiStateMachine.this.mLastAuthFailureTimestamp = WifiStateMachine.this.mClock.getElapsedSinceBootMillis();
                    WifiStateMachine.this.updateNetworkConnFailedInfo(WifiStateMachine.this.mTargetNetworkId, WifiServiceHisiExt.MIN_RSSI, 3);
                    WifiStateMachine.this.reportConnectionAttemptEnd(3, 1);
                    if (WifiStateMachine.this.mCust != null && WifiStateMachine.this.mCust.isShowWifiAuthenticationFailurerNotification()) {
                        WifiStateMachine.this.mCust.handleWifiAuthenticationFailureEvent(WifiStateMachine.this.mContext, WifiStateMachine.this);
                    }
                    WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 2);
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                    int supplicantNetworkId = message.arg2;
                    netId = WifiStateMachine.this.lookupFrameworkNetworkId(supplicantNetworkId);
                    boolean identitySent = false;
                    if (WifiStateMachine.this.targetWificonfiguration != null && WifiStateMachine.this.targetWificonfiguration.networkId == netId && TelephonyUtil.isSimConfig(WifiStateMachine.this.targetWificonfiguration)) {
                        String identity = TelephonyUtil.getSimIdentity(WifiStateMachine.this.getTelephonyManager(), WifiStateMachine.this.targetWificonfiguration);
                        if (identity != null) {
                            identitySent = WifiStateMachine.this.mWifiNative.simIdentityResponse(supplicantNetworkId, identity);
                        } else {
                            Log.e(WifiStateMachine.TAG, "Unable to retrieve identity from Telephony");
                        }
                    }
                    if (!identitySent) {
                        String ssid = message.obj;
                        if (!(WifiStateMachine.this.targetWificonfiguration == null || ssid == null || WifiStateMachine.this.targetWificonfiguration.SSID == null || (!WifiStateMachine.this.targetWificonfiguration.SSID.equals(ssid) && !WifiStateMachine.this.targetWificonfiguration.SSID.equals("\"" + ssid + "\"")))) {
                            if (WifiStateMachine.this.mTrackEapAuthFailCount >= 3) {
                                Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 8);
                                WifiStateMachine.this.mTrackEapAuthFailCount = 0;
                            } else if (WifiStateMachine.this.mIsImsiAvailable) {
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 0);
                                wifiStateMachine = WifiStateMachine.this;
                                wifiStateMachine.mTrackEapAuthFailCount = wifiStateMachine.mTrackEapAuthFailCount + 1;
                                Log.d(WifiStateMachine.TAG, "sim is not ready and retry mTrackEapAuthFailCount " + WifiStateMachine.this.mTrackEapAuthFailCount);
                            } else {
                                Log.d(WifiStateMachine.TAG, "sim is not available,updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration.networkId, 8);
                                WifiStateMachine.this.mTrackEapAuthFailCount = 0;
                            }
                        }
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 2);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                    WifiStateMachine.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    SimAuthRequestData requestData = message.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                WifiStateMachine.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        }
                        WifiStateMachine.this.handleGsmAuthRequest(requestData);
                        break;
                    }
                    WifiStateMachine.this.loge("Invalid sim auth request");
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(1);
                    WifiStateMachine.this.didBlackListBSSID = false;
                    bssid = message.obj;
                    boolean timedOut = message.arg1 > 0;
                    reasonCode = message.arg2;
                    Log.d(WifiStateMachine.TAG, "Assocation Rejection event: bssid=" + bssid + " reason code=" + reasonCode + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid == null || TextUtils.isEmpty(bssid)) {
                        bssid = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    if (bssid != null) {
                        WifiStateMachine.this.didBlackListBSSID = WifiStateMachine.this.mWifiConnectivityManager.trackBssid(bssid, false, reasonCode);
                    }
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 2);
                    WifiStateMachine.this.updateNetworkConnFailedInfo(WifiStateMachine.this.mTargetNetworkId, WifiServiceHisiExt.MIN_RSSI, 2);
                    WifiStateMachine.this.recordAssociationRejectStatusCode(message.arg2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                    WifiStateMachine.this.reportConnectionAttemptEnd(2, 1);
                    WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), bssid, 1);
                    break;
                case WifiMonitor.AUTHENTICATION_TIMEOUT_EVENT /*147501*/:
                    if (WifiStateMachine.this.mWifiInfo != null && WifiStateMachine.this.mWifiInfo.getSupplicantState() == SupplicantState.ASSOCIATED) {
                        WifiStateMachine.this.loge("auth timeout in associated state, handle as associate reject event");
                        WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                        break;
                    }
                case WifiMonitor.WPS_START_OKC_EVENT /*147656*/:
                    WifiStateMachine.this.sendWpsOkcStartedBroadcast();
                    if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                        WifiStateMachine.this.loge("Failed to remove networks before HiLink OKC");
                    }
                    String hilinkBssid = message.obj;
                    if (!TextUtils.isEmpty(hilinkBssid)) {
                        WifiStateMachine.this.mWifiNative.startWpsPbc(hilinkBssid);
                        break;
                    }
                    break;
                case 151553:
                    netId = message.arg1;
                    config = (WifiConfiguration) message.obj;
                    if (config == null || !config.hiddenSSID || !TextUtils.isEmpty(config.oriSsid)) {
                        -get85 = WifiStateMachine.this.mWifiConnectionStatistics;
                        -get85.numWifiManagerJoinAttempt++;
                        String strConfigCRC = WifiConfigManager.PASSWORD_MASK;
                        boolean hasCredentialChanged = false;
                        boolean forceReconnect = false;
                        if (config != null) {
                            strConfigCRC = config.preSharedKey;
                            if (WifiStateMachine.this.mNetworkInfo != null && WifiStateMachine.this.mNetworkInfo.isConnectedOrConnecting() && config.isTempCreated && WifiStateMachine.this.isWifiProEvaluatingAP()) {
                                WifiStateMachine.this.logd("CONNECT_NETWORK user connect network, stop background evaluating and force reconnect");
                                config.isTempCreated = false;
                                forceReconnect = true;
                            }
                            result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                            if (!result.isSuccess()) {
                                WifiStateMachine.this.loge("CONNECT_NETWORK adding/updating config=" + config + " failed");
                                WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                                WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                break;
                            }
                            netId = result.getNetworkId();
                            hasCredentialChanged = result.hasCredentialChanged();
                            if (WifiStateMachine.this.mWiFiCHRManager != null) {
                                WifiStateMachine.this.mWiFiCHRManager.setLastNetIdFromUI(config, netId);
                            }
                        }
                        if (WifiStateMachine.this.mHwWifiCHRService != null) {
                            WifiConfiguration wifiConfiguration = new WifiConfiguration(config);
                            wifiConfiguration.preSharedKey = strConfigCRC;
                            WifiStateMachine.this.mHwWifiCHRService.connectFromUserByConfig(wifiConfiguration);
                        }
                        WifiStateMachine.this.saveConnectingNetwork(config);
                        WifiStateMachine.this.exitWifiSelfCure(151553, -1);
                        if (!(WifiStateMachine.this.mLastNetworkId == -1 || WifiStateMachine.this.mLastNetworkId != netId || WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mDisconnectedState)) {
                            WifiStateMachine.this.logd("disconnect old");
                            WifiStateMachine.this.mWifiNative.disconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        }
                        wifiStateMachine = WifiStateMachine.this;
                        int i3 = message.sendingUid;
                        if (hasCredentialChanged) {
                            forceReconnect = true;
                        }
                        if (!wifiStateMachine.connectToUserSelectNetwork(netId, i3, forceReconnect)) {
                            WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            if (-1 != netId && config == null) {
                                config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(netId);
                            }
                            if (config != null && HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, false)) {
                                WifiStateMachine.this.replyToMessage(message, 151554, 1000);
                                break;
                            }
                            WifiStateMachine.this.replyToMessage(message, 151554, 9);
                            break;
                        }
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(13, config);
                        WifiStateMachine.this.broadcastWifiCredentialChanged(0, config);
                        WifiStateMachine.this.replyToMessage(message, 151555);
                        break;
                    }
                    Log.d(WifiStateMachine.TAG, "INTERCEPT the connect request since hidden and null oriSsid.");
                    ApConfigUtil.startScanForHiddenNetwork(WifiStateMachine.this.mWifiScanner, new HiddenScanListener(config, Message.obtain(message)), config);
                    break;
                    break;
                case 151556:
                    if (WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, true)) {
                        netId = message.arg1;
                        WifiStateMachine.this.exitWifiSelfCure(151556, netId);
                        if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            break;
                        }
                    }
                    break;
                case 151559:
                    config = (WifiConfiguration) message.obj;
                    -get85 = WifiStateMachine.this.mWifiConnectionStatistics;
                    -get85.numWifiManagerJoinAttempt++;
                    if (config != null) {
                        result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                        if (!result.isSuccess()) {
                            WifiStateMachine.this.loge("SAVE_NETWORK adding/updating config=" + config + " failed");
                            WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            WifiStateMachine.this.replyToMessage(message, 151560, 0);
                            break;
                        }
                        if (!WifiStateMachine.this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, message.sendingUid)) {
                            WifiStateMachine.this.loge("SAVE_NETWORK enabling config=" + config + " failed");
                            WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            WifiStateMachine.this.replyToMessage(message, 151560, 0);
                            break;
                        }
                        netId = result.getNetworkId();
                        if (WifiStateMachine.this.mWifiInfo.getNetworkId() == netId) {
                            if (result.hasCredentialChanged()) {
                                WifiStateMachine.this.logi("SAVE_NETWORK credential changed for config=" + config.configKey() + ", Reconnecting.");
                                WifiStateMachine.this.startConnectToNetwork(netId, "any");
                            } else {
                                if (result.hasProxyChanged()) {
                                    WifiStateMachine.this.log("Reconfiguring proxy on connection");
                                    WifiStateMachine.this.mIpManager.setHttpProxy(WifiStateMachine.this.getProxyProperties());
                                }
                                if (result.hasIpChanged()) {
                                    Log.d(WifiStateMachine.TAG, "Reconfiguring IP if current state == mConnectedState");
                                    if (WifiStateMachine.this.isConnected()) {
                                        WifiStateMachine.this.log("Reconfiguring IP on connection");
                                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                                    } else {
                                        Log.d(WifiStateMachine.TAG, "ignore reconfiguring IP because current state != mConnectedState");
                                    }
                                }
                            }
                        }
                        WifiStateMachine.this.broadcastWifiCredentialChanged(0, config);
                        WifiStateMachine.this.replyToMessage(message, 151561);
                        break;
                    }
                    WifiStateMachine.this.loge("SAVE_NETWORK with null configuration" + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName());
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, 151560, 0);
                    break;
                case 151562:
                    WpsInfo wpsInfo = message.obj;
                    if (wpsInfo != null) {
                        WpsResult wpsResult = new WpsResult();
                        if (!WifiStateMachine.this.mWifiNative.removeAllNetworks()) {
                            WifiStateMachine.this.loge("Failed to remove networks before WPS");
                        }
                        switch (wpsInfo.setup) {
                            case 0:
                                if (WifiStateMachine.this.mWifiNative.startWpsPbc(wpsInfo.BSSID)) {
                                    wpsResult.status = Status.SUCCESS;
                                } else {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS push button configuration");
                                    wpsResult.status = Status.FAILURE;
                                }
                                WifiStateMachine.this.clearRandomMacOui();
                                WifiStateMachine.this.mIsRandomMacCleared = true;
                                break;
                            case 1:
                                wpsResult.pin = WifiStateMachine.this.mWifiNative.startWpsPinDisplay(wpsInfo.BSSID);
                                if (!TextUtils.isEmpty(wpsResult.pin)) {
                                    wpsResult.status = Status.SUCCESS;
                                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_WPS_PIN_RETRY, wpsResult);
                                    break;
                                }
                                Log.e(WifiStateMachine.TAG, "Failed to start WPS pin method configuration");
                                wpsResult.status = Status.FAILURE;
                                break;
                            case 2:
                                if (!WifiStateMachine.this.mWifiNative.startWpsRegistrar(wpsInfo.BSSID, wpsInfo.pin)) {
                                    Log.e(WifiStateMachine.TAG, "Failed to start WPS push button configuration");
                                    wpsResult.status = Status.FAILURE;
                                    break;
                                }
                                wpsResult.status = Status.SUCCESS;
                                break;
                            default:
                                wpsResult = new WpsResult(Status.FAILURE);
                                WifiStateMachine.this.loge("Invalid setup for WPS");
                                break;
                        }
                        if (wpsResult.status != Status.SUCCESS) {
                            WifiStateMachine.this.loge("Failed to start WPS with config " + wpsInfo.toString());
                            WifiStateMachine.this.replyToMessage(message, 151564, 0);
                            break;
                        }
                        WifiStateMachine.this.replyToMessage(message, 151563, (Object) wpsResult);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWpsRunningState);
                        break;
                    }
                    WifiStateMachine.this.loge("Cannot start WPS with null WpsInfo object");
                    WifiStateMachine.this.replyToMessage(message, 151564, 0);
                    break;
                case 151569:
                    netId = message.arg1;
                    if (!WifiStateMachine.this.mWifiConfigManager.disableNetwork(netId, message.sendingUid)) {
                        WifiStateMachine.this.loge("Failed to disable network");
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                        WifiStateMachine.this.replyToMessage(message, 151570, 0);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151571);
                    if (netId == WifiStateMachine.this.mTargetNetworkId || netId == WifiStateMachine.this.mLastNetworkId) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                default:
                    return false;
            }
            return true;
        }
    }

    class ConnectedState extends State {
        private Message mSourceMessage = null;

        ConnectedState() {
        }

        public void enter() {
            WifiStateMachine.this.logd("WifiStateMachine: enter Connected state" + getName());
            WifiStateMachine.this.processStatistics(0);
            new Thread(new Runnable() {
                public void run() {
                    WifiStateMachine.this.updateDefaultRouteMacAddress(1000);
                }
            }).start();
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.log("Enter ConnectedState  mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.triggerRoamingNetworkMonitor(WifiStateMachine.this.mIsAutoRoaming);
            WifiStateMachine.this.handleConnectedInWifiPro();
            if (WifiStateMachine.this.mWifiRepeater != null) {
                WifiStateMachine.this.mWifiRepeater.handleWifiConnect(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mWifiInfo.getNetworkId()));
            }
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
                if (WifiStateMachine.this.mWifiStatStore != null && WifiStateMachine.this.mConnectingStartTimestamp > 0) {
                    WifiStateMachine.this.mWifiStatStore.triggerCHRConnectingDuration(WifiStateMachine.this.mClock.getElapsedSinceBootMillis() - WifiStateMachine.this.mConnectingStartTimestamp);
                    WifiStateMachine.this.mConnectingStartTimestamp = 0;
                }
            }
            WifiStateMachine.this.registerConnected();
            WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiStateMachine.this.targetWificonfiguration = null;
            WifiStateMachine.this.mIsLinkDebouncing = false;
            WifiStateMachine.this.mIsAutoRoaming = false;
            if (WifiStateMachine.this.testNetworkDisconnect) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                wifiStateMachine.testNetworkDisconnectCounter = wifiStateMachine.testNetworkDisconnectCounter + 1;
                WifiStateMachine.this.logd("ConnectedState Enter start disconnect test " + WifiStateMachine.this.testNetworkDisconnectCounter);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT, WifiStateMachine.this.testNetworkDisconnectCounter, 0), 15000);
            }
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mTargetNetworkId = -1;
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            WifiStateMachine.this.triggerUpdateAPInfo();
            WifiStateMachine.this.mWifiStateTracker.updateState(3);
            WifiStateMachine.this.notifyWlanChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(WifiStateMachine.this.mWifiInfo.getFrequency()));
        }

        /* JADX WARNING: Removed duplicated region for block: B:104:0x06e9 A:{Splitter: B:101:0x0692, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
        /* JADX WARNING: Missing block: B:105:0x06ea, code:
            r26.this$0.loge("Can't find MAC address for next hop to " + r18.dstAddress);
            com.android.server.wifi.WifiStateMachine.-get41(r26.this$0).onPacketKeepaliveEvent(r20, -21);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            String str;
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                    if (message.arg1 == WifiStateMachine.this.testNetworkDisconnectCounter) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 0) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 3);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    } else if (message.arg1 == 2 || message.arg1 == 1 || message.arg1 == 3) {
                        String str2 = WifiStateMachine.TAG;
                        if (message.arg1 == 2) {
                            str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                        } else {
                            str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                        }
                        Log.d(str2, str);
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            if (message.arg1 == 2) {
                                WifiStateMachine.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, false);
                                Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus(DISABLED_NO_INTERNET)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config.networkId, 9);
                            }
                            WifiStateMachine.this.mWifiConfigManager.incrementNetworkNoInternetAccessReports(config.networkId);
                            WifiStateMachine.this.handleUnwantedNetworkInWifiPro(config, message.arg1);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    int netId = message.arg1;
                    ScanResult candidate = message.obj;
                    String bssid = "any";
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    config = WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                    if (config != null) {
                        WifiStateMachine.this.setTargetBssid(config, bssid);
                        WifiStateMachine.this.mTargetNetworkId = netId;
                        WifiStateMachine.this.logd("CMD_START_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config.configKey() + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                        WifiStateMachine.this.reportConnectionAttemptStart(config, WifiStateMachine.this.mTargetRoamBSSID, 3);
                        if (!WifiStateMachine.this.mWifiNative.roamToNetwork(config)) {
                            WifiStateMachine.this.loge("CMD_START_ROAM Failed to start roaming to network " + config);
                            WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                            WifiStateMachine.this.replyToMessage(message, 151554, 0);
                            WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            break;
                        }
                        WifiStateMachine.this.lastConnectAttemptTimestamp = WifiStateMachine.this.mClock.getWallClockMillis();
                        WifiStateMachine.this.targetWificonfiguration = config;
                        WifiStateMachine.this.mIsAutoRoaming = true;
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(12, config);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                        break;
                    }
                    WifiStateMachine.this.loge("CMD_START_ROAM and no config, bail out...");
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = WifiStateMachine.this.mClock.getWallClockMillis();
                    WifiStateMachine.this.notifyWifiRoamingStarted();
                    return false;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1) {
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            WifiStateMachine.this.handleValidNetworkInWifiPro(config);
                            WifiStateMachine.this.mWifiConfigManager.setNetworkValidatedInternetAccess(config.networkId, true);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_ACCEPT_UNVALIDATED /*131225*/:
                    WifiStateMachine.this.mWifiConfigManager.setNetworkNoInternetAccessExpected(WifiStateMachine.this.mLastNetworkId, message.arg1 != 0);
                    return true;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    int slot = message.arg1;
                    int intervalSeconds = message.arg2;
                    KeepalivePacketData pkt = message.obj;
                    try {
                        pkt.dstMac = NativeUtil.macAddressToByteArray(WifiStateMachine.this.macAddressFromRoute(RouteInfo.selectBestRoute(WifiStateMachine.this.mLinkProperties.getRoutes(), pkt.dstAddress).getGateway().getHostAddress()));
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, WifiStateMachine.this.startWifiIPPacketOffload(slot, pkt, intervalSeconds));
                        break;
                    } catch (NullPointerException e) {
                    }
                case WifiStateMachine.CMD_SET_DETECTMODE_CONF /*131772*/:
                    WifiStateMachine.this.processSetVoWifiDetectMode(message);
                    break;
                case WifiStateMachine.CMD_SET_DETECT_PERIOD /*131773*/:
                    WifiStateMachine.this.processSetVoWifiDetectPeriod(message);
                    break;
                case WifiStateMachine.POOR_LINK_DETECTED /*131873*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: POOR_LINK_DETECTED");
                    WifiStateMachine.this.wifiNetworkExplicitlyUnselected();
                    WifiStateMachine.this.setNetworkDetailedState(DetailedState.VERIFYING_POOR_LINK);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.GOOD_LINK_DETECTED /*131874*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: GOOD_LINK_DETECTED");
                    WifiStateMachine.this.updateWifiBackgroudStatus(message.arg1);
                    WifiStateMachine.this.wifiNetworkExplicitlySelected();
                    WifiStateMachine.this.setWifiBackgroundStatus(false);
                    WifiStateMachine.this.sendConnectedState();
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
                    WifiStateMachine.this.stopIpManager();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.SCE_START_SET_STATIC_IP, message.obj, 1000);
                    break;
                case WifiStateMachine.SCE_START_SET_STATIC_IP /*131885*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_START_SET_STATIC_IP");
                    WifiStateMachine.this.handleStaticIpConfig(WifiStateMachine.this.mIpManager, WifiStateMachine.this.mWifiNative, (StaticIpConfiguration) message.obj);
                    break;
                case WifiStateMachine.SCE_REQUEST_REASSOC_WIFI /*131886*/:
                    WifiStateMachine.this.startSelfCureWifiReassoc();
                    break;
                case WifiStateMachine.SCE_REQUEST_RESET_WIFI /*131887*/:
                    WifiStateMachine.this.startSelfCureWifiReset();
                    break;
                case WifiStateMachine.CMD_SCE_HANDLE_IP_INVALID /*131895*/:
                    WifiStateMachine.this.startSelfCureReconnect();
                    WifiStateMachine.this.mIpManager.forceRemoveDhcpCache();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_DISCONNECT, 500);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    long lastRoam = 0;
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    if (WifiStateMachine.this.mLastDriverRoamAttempt != 0) {
                        lastRoam = WifiStateMachine.this.mClock.getWallClockMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(5);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    WifiStateMachine.this.handleDisconnectedReason(config, WifiStateMachine.this.mWifiInfo.getRssi(), message.arg1, message.arg2);
                    if (!WifiStateMachine.this.mEnableLinkDebouncing || ((!WifiStateMachine.this.mScreenOn && !WifiStateMachine.this.isWifiRepeaterStarted()) || (WifiStateMachine.this.isLinkDebouncing() ^ 1) == 0 || config == null || !config.getNetworkSelectionStatus().isNetworkEnabled() || config.networkId == WifiStateMachine.this.mWifiConfigManager.getLastSelectedNetwork() || ((Math.abs(message.arg2) == 3 && (lastRoam <= 0 || lastRoam >= 2000)) || ((!ScanResult.is24GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mThresholdQualifiedRssi5) && (!ScanResult.is5GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= WifiStateMachine.this.mThresholdQualifiedRssi5))))) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            StringBuilder append = new StringBuilder().append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=").append(StringUtil.safeDisplayBssid(WifiStateMachine.this.mWifiInfo.getBSSID())).append(" RSSI=").append(WifiStateMachine.this.mWifiInfo.getRssi()).append(" freq=").append(WifiStateMachine.this.mWifiInfo.getFrequency()).append(" was debouncing=").append(WifiStateMachine.this.isLinkDebouncing()).append(" reason=").append(message.arg2).append(" Network Selection Status=");
                            if (config == null) {
                                str = "Unavailable";
                            } else {
                                str = config.getNetworkSelectionStatus().getNetworkStatusString();
                            }
                            wifiStateMachine.log(append.append(str).toString());
                            break;
                        }
                    }
                    WifiStateMachine.this.startScanForConfiguration(WifiStateMachine.this.getCurrentWifiConfiguration());
                    WifiStateMachine.this.mIsLinkDebouncing = true;
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT, 0, WifiStateMachine.this.mLastNetworkId), 4000);
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in connected state BSSID=" + StringUtil.safeDisplayBssid(WifiStateMachine.this.mWifiInfo.getBSSID()) + " RSSI=" + WifiStateMachine.this.mWifiInfo.getRssi() + " freq=" + WifiStateMachine.this.mWifiInfo.getFrequency() + " reason=" + message.arg2 + " -> debounce");
                    }
                    return true;
                    break;
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /*147520*/:
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
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
            WifiStateMachine.this.notifyWlanState(WifiCommonUtils.STATE_DISCONNECTED);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        /* JADX WARNING: Missing block: B:114:0x0499, code:
            if (r22.contains(java.lang.Integer.valueOf(com.android.server.wifi.WifiStateMachine.-get39(r24.this$0))) != false) goto L_0x049b;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case 69632:
                    if (message.obj == WifiStateMachine.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            WifiStateMachine.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        }
                        WifiStateMachine.this.p2pSendMessage(69633);
                        if (WifiStateMachine.this.mOperationalMode == 1) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                            break;
                        }
                    }
                    WifiStateMachine.this.loge("got HALF_CONNECTED for unknown channel");
                    break;
                    break;
                case 69636:
                    if (((AsyncChannel) message.obj) == WifiStateMachine.this.mWifiP2pChannel) {
                        WifiStateMachine.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_DRIVER_START_TIMED_OUT /*131091*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
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
                case 147457:
                case 147458:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                case 147474:
                case WifiMonitor.WAPI_CERTIFICATION_FAILURE_EVENT /*147475*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /*147520*/:
                case WifiMonitor.WPS_START_OKC_EVENT /*147656*/:
                case 151575:
                case 196611:
                case 196612:
                case 196614:
                case 196618:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    break;
                case WifiStateMachine.CMD_AP_STARTED_GET_STA_LIST /*131104*/:
                    WifiStateMachine.this.loge("DefaultState: cannot get Soft AP current connected stations list");
                    WifiStateMachine.this.mReplyChannel.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                    NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(message.obj, message.sendingUid);
                    if (!result.isSuccess()) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, result.getNetworkId());
                    break;
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, false);
                    break;
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    int i;
                    boolean ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(message.arg1, message.arg2 == 1, message.sendingUid);
                    if (!ok) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    int i2 = message.what;
                    if (ok) {
                        i = 1;
                    } else {
                        i = -1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, -1);
                    break;
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getSavedNetworks());
                    break;
                case WifiStateMachine.CMD_GET_SUPPORTED_FEATURES /*131133*/:
                    if (WifiStateMachine.this.mFeatureSet <= 0) {
                        WifiStateMachine.this.mFeatureSet = WifiStateMachine.this.mWifiNative.getSupportedFeatureSet();
                        if (WifiStateMachine.DBG) {
                            Log.d(WifiStateMachine.TAG, "CMD_GET_SUPPORTED_FEATURES: " + WifiStateMachine.this.mFeatureSet);
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mFeatureSet);
                    break;
                case WifiStateMachine.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*131134*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_GET_CONNECTION_STATISTICS /*131148*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConnectionStatistics);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(2, true);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizations(2, false);
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(4, false);
                        break;
                    }
                    if (message.arg2 == 1) {
                        WifiStateMachine.this.mSuspendWakeLock.release();
                    }
                    WifiStateMachine.this.setSuspendOptimizations(4, true);
                    break;
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    WifiStateMachine.this.handleScreenStateChanged(message.arg1 != 0);
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_FIRMWARE_ALERT /*131172*/:
                    if (WifiStateMachine.this.mWifiDiagnostics != null) {
                        byte[] buffer = message.obj;
                        int alertReason = message.arg1;
                        WifiStateMachine.this.mWifiDiagnostics.captureAlertData(alertReason, buffer);
                        WifiStateMachine.this.mWifiMetrics.incrementAlertReasonCount(alertReason);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG /*131178*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mPasspointManager.addOrUpdateProvider((PasspointConfiguration) message.obj, message.arg1) ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_REMOVE_PASSPOINT_CONFIG /*131179*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mPasspointManager.removeProvider((String) message.obj) ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_GET_PASSPOINT_CONFIGS /*131180*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mPasspointManager.getProviderConfigs());
                    break;
                case WifiStateMachine.CMD_BOOT_COMPLETED /*131206*/:
                    WifiStateMachine.this.isBootCompleted = true;
                    WifiStateMachine.this.getAdditionalWifiServiceInterfaces();
                    if (!WifiStateMachine.this.mWifiConfigManager.loadFromStore()) {
                        Log.e(WifiStateMachine.TAG, "Failed to load from config store");
                    }
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    break;
                case WifiStateMachine.CMD_INITIALIZE /*131207*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiNative.initializeVendorHal(WifiStateMachine.this.mVendorHalDeathRecipient) ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_UPDATE_LINKPROPERTIES /*131212*/:
                    WifiStateMachine.this.updateLinkProperties((LinkProperties) message.obj);
                    break;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_INSTALL_PACKET_FILTER /*131274*/:
                    WifiStateMachine.this.mWifiNative.installPacketFilter((byte[]) message.obj);
                    break;
                case WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                    if (!((Boolean) message.obj).booleanValue()) {
                        WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
                        break;
                    }
                    WifiStateMachine.this.mWifiNative.startFilteringMulticastV4Packets();
                    break;
                case WifiStateMachine.CMD_USER_SWITCH /*131277*/:
                    Set<Integer> removedNetworkIds = WifiStateMachine.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (!removedNetworkIds.contains(Integer.valueOf(WifiStateMachine.this.mTargetNetworkId))) {
                        break;
                    }
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                    break;
                case WifiStateMachine.CMD_USER_UNLOCK /*131278*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserUnlock(message.arg1);
                    break;
                case WifiStateMachine.CMD_USER_STOP /*131279*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserStop(message.arg1);
                    break;
                case WifiStateMachine.CMD_CLIENT_INTERFACE_BINDER_DEATH /*131322*/:
                    Log.d(WifiStateMachine.TAG, "message.what = CMD_CLIENT_INTERFACE_BINDER_DEATH ,mIsRealReboot = " + WifiStateMachine.this.mIsRealReboot);
                    if (!WifiStateMachine.this.mIsRealReboot) {
                        Log.e(WifiStateMachine.TAG, "wificond died unexpectedly. Triggering recovery");
                        WifiStateMachine.this.mWifiMetrics.incrementNumWificondCrashes();
                        WifiStateMachine.this.mWifiInjector.getSelfRecovery().trigger(2);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_VENDOR_HAL_HWBINDER_DEATH /*131323*/:
                    Log.d(WifiStateMachine.TAG, "message.what = CMD_VENDOR_HAL_HWBINDER_DEATH ,mIsRealReboot = " + WifiStateMachine.this.mIsRealReboot);
                    if (!WifiStateMachine.this.mIsRealReboot) {
                        Log.e(WifiStateMachine.TAG, "Vendor HAL died unexpectedly. Triggering recovery");
                        WifiStateMachine.this.mWifiMetrics.incrementNumHalCrashes();
                        WifiStateMachine.this.mWifiInjector.getSelfRecovery().trigger(1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DIAGS_CONNECT_TIMEOUT /*131324*/:
                    WifiStateMachine.this.mWifiDiagnostics.reportConnectionEvent(((Long) message.obj).longValue(), (byte) 2);
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_PNO_PERIODIC_SCAN /*131575*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS /*131672*/:
                    break;
                case WifiStateMachine.CMD_GET_SUPPORT_VOWIFI_DETECT /*131774*/:
                    WifiStateMachine.this.processIsSupportVoWifiDetect(message);
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
                    WifiStateMachine.this.log("wifi self cure timeout, message type = " + message.what);
                    WifiStateMachine.this.notifySelfCureComplete(false, message.arg1);
                    break;
                case WifiStateMachine.CMD_SCE_STOP_SELF_CURE /*131892*/:
                    WifiStateMachine.this.log("CMD_SCE_STOP_SELF_CURE, arg1 =" + message.arg1);
                    WifiStateMachine.this.stopSelfCureWifi(message.arg1);
                    if (message.arg1 < 0) {
                        if (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mDisconnectedState) {
                            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
                            WifiStateMachine.this.sendNetworkStateChangeBroadcast(null);
                            break;
                        }
                        WifiStateMachine.this.log("CMD_SCE_STOP_SELF_CURE, to disconnect because of wifi self cure failed.");
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        WifiStateMachine.this.handleNetworkDisconnect();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_SCE_RESTORE /*131893*/:
                    if (WifiStateMachine.this.mNetworkAgent == null) {
                        WifiStateMachine.this.log("CMD_SCE_RESTORE, use networkAgent to sendNetworkInfo");
                        new WifiNetworkAgent(WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, WifiStateMachine.this.mNetworkCapabilitiesFilter, WifiStateMachine.this.mLinkProperties, 100, WifiStateMachine.this.mNetworkMisc).sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
                        break;
                    }
                    WifiStateMachine.this.log("CMD_SCE_RESTORE, use mNetworkAgent to sendNetworkInfo");
                    WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
                    break;
                case WifiStateMachine.CMD_SCE_NOTIFY_WIFI_DISABLED /*131894*/:
                    WifiStateMachine.this.log("CMD_SCE_NOTIFY_WIFI_DISABLED, set WIFI_STATE_DISABLED");
                    WifiStateMachine.this.setWifiState(1);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    WifiStateMachine.this.mTemporarilyDisconnectWifi = message.arg1 == 1;
                    WifiStateMachine.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiMonitor.EVENT_ANT_CORE_ROB /*147757*/:
                    WifiStateMachine.this.handleAntenaPreempted();
                    break;
                case 151553:
                    WifiStateMachine.this.replyToMessage(message, 151554, 2);
                    break;
                case 151556:
                    WifiStateMachine.this.deleteNetworkConfigAndSendReply(message, true);
                    break;
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, 151560, 2);
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message, 151564, 2);
                    break;
                case 151566:
                    WifiStateMachine.this.replyToMessage(message, 151567, 2);
                    break;
                case 151569:
                    WifiStateMachine.this.replyToMessage(message, 151570, 2);
                    break;
                case 151572:
                    WifiStateMachine.this.replyToMessage(message, 151574, 2);
                    break;
                default:
                    WifiStateMachine.this.loge("Error! unhandled message" + message);
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
                WifiStateMachine.this.p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.logd(" Enter DisconnectedState screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.handleDisconnectedInWifiPro();
            if (WifiStateMachine.this.mWifiRepeater != null) {
                WifiStateMachine.this.mWifiRepeater.handleWifiDisconnect();
            }
            WifiStateMachine.this.mIsAutoRoaming = false;
            WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
            if (!(WifiStateMachine.this.mNoNetworksPeriodicScan == 0 || (WifiStateMachine.this.mP2pConnected.get() ^ 1) == 0 || WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() != 0)) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
            }
            WifiStateMachine.this.mDisconnectedTimeStamp = WifiStateMachine.this.mClock.getWallClockMillis();
            WifiStateMachine.this.mWifiStateTracker.updateState(2);
        }

        public boolean processMessage(Message message) {
            boolean z = true;
            boolean ret = true;
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiStateMachine wifiStateMachine;
            WifiStateMachine wifiStateMachine2;
            WifiStateMachine wifiStateMachine3;
            switch (message.what) {
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                case 151556:
                    wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine2 = WifiStateMachine.this;
                    wifiStateMachine3 = WifiStateMachine.this;
                    wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                    ret = false;
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    if (WifiStateMachine.this.checkOrDeferScanAllowed(message)) {
                        ret = false;
                        break;
                    }
                    WifiStateMachine.this.messageHandlingStatus = -1;
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (!WifiStateMachine.this.processDisconnectedSetMode(message)) {
                        if (message.arg1 != 1) {
                            WifiStateMachine.this.mOperationalMode = message.arg1;
                            if (WifiStateMachine.this.mOperationalMode != 4) {
                                if (WifiStateMachine.this.mOperationalMode == 2 || WifiStateMachine.this.mOperationalMode == 3) {
                                    WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                                    WifiStateMachine.this.setWifiState(1);
                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
                                    break;
                                }
                            }
                            WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                            break;
                        }
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    Log.d("HwWifiStateMachine", "DisconnectedState process CMD_SET_OPERATIONAL_MODE");
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 0);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    if (!WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                        ret = false;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                    if (!WifiStateMachine.this.mP2pConnected.get() && WifiStateMachine.this.mNoNetworksPeriodicScan != 0 && message.arg1 == WifiStateMachine.this.mPeriodicScanToken && WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
                        WifiStateMachine.this.startScan(-1, -1, null, WifiStateMachine.WIFI_WORK_SOURCE);
                        wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine2 = WifiStateMachine.this;
                        wifiStateMachine3 = WifiStateMachine.this;
                        wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                        break;
                    }
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    wifiStateMachine2 = WifiStateMachine.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    wifiStateMachine2.handleScreenStateChanged(z);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    if (!WifiStateMachine.this.mP2pConnected.get() && WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Turn on scanning after p2p disconnected");
                        }
                        wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine2 = WifiStateMachine.this;
                        wifiStateMachine3 = WifiStateMachine.this;
                        wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                        break;
                    }
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state) + " debouncing=" + WifiStateMachine.this.isLinkDebouncing());
                    }
                    WifiStateMachine.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    ret = false;
                    break;
                default:
                    ret = false;
                    break;
            }
            return ret;
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
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.disconnectingWatchdogCount++;
            WifiStateMachine.this.logd("Start Disconnecting Watchdog " + WifiStateMachine.this.disconnectingWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER, WifiStateMachine.this.disconnectingWatchdogCount, 0), 5000);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.deferMessage(message);
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.deferMessage(message);
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
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
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

    private class HiddenScanListener implements ScanListener {
        private WifiConfiguration mConfig = null;
        private Message mMessage = null;
        private List<ScanResult> mScanResults = new ArrayList();

        HiddenScanListener(WifiConfiguration config, Message message) {
            this.mConfig = config;
            this.mMessage = message;
        }

        private void quit() {
            this.mConfig = null;
            this.mMessage = null;
            this.mScanResults.clear();
        }

        public void onResults(ScanData[] scanDatas) {
            if (this.mConfig == null || WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(this.mConfig.configKey()) == null || this.mScanResults.size() == 0) {
                Log.d(WifiStateMachine.TAG, "HiddenScanListener: return since config removed.");
                return;
            }
            String ssid = NativeUtil.removeEnclosingQuotes(this.mConfig.SSID);
            int size = this.mScanResults.size();
            int i = 0;
            while (i < size) {
                ScanResult result = (ScanResult) this.mScanResults.get(i);
                if (result == null || result.wifiSsid == null || (TextUtils.isEmpty(result.wifiSsid.oriSsid) ^ 1) == 0 || (TextUtils.isEmpty(result.SSID) ^ 1) == 0 || !result.SSID.equals(ssid)) {
                    i++;
                } else {
                    this.mConfig.oriSsid = result.wifiSsid.oriSsid;
                    Log.d(WifiStateMachine.TAG, "HiddenScanListener: find SSID=" + ssid + " oriSsid=" + this.mConfig.oriSsid);
                    WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(this.mConfig, this.mMessage.sendingUid);
                    WifiStateMachine.this.sendMessage(Message.obtain(this.mMessage));
                    quit();
                    return;
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

    class InitialState extends State {
        InitialState() {
        }

        private void cleanup() {
            WifiStateMachine.this.mWifiMonitor.stopAllMonitoring();
            WifiStateMachine.this.mDeathRecipient.unlinkToDeath();
            WifiStateMachine.this.mWifiNative.tearDown();
            ScanResultRecords.getDefault().cleanup();
        }

        public void enter() {
            WifiStateMachine.this.mWifiStateTracker.updateState(0);
            cleanup();
            WifiStateMachine.this.mFeatureSet = 0;
        }

        /* JADX WARNING: Missing block: B:5:0x0020, code:
            if (com.android.server.wifi.WifiStateMachine.-get48(r10.this$0) == 4) goto L_0x0022;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                    WifiStateMachine.this.mClientInterface = WifiStateMachine.this.mWifiNative.setupForClientMode();
                    if (WifiStateMachine.this.mClientInterface != null && (WifiStateMachine.this.mDeathRecipient.linkToDeath(WifiStateMachine.this.mClientInterface.asBinder()) ^ 1) == 0) {
                        if (WifiStateMachine.this.mWifiStatStore != null) {
                            WifiStateMachine.this.mWifiStatStore.updateWifiTriggerState(true);
                        }
                        try {
                            WifiStateMachine.this.mNwService.clearInterfaceAddresses(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.mNwService.setInterfaceIpv6PrivacyExtensions(WifiStateMachine.this.mInterfaceName, true);
                            WifiStateMachine.this.mNwService.disableIpv6(WifiStateMachine.this.mInterfaceName);
                        } catch (RemoteException re) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + re);
                        } catch (IllegalStateException ie) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + ie);
                        }
                        if (!WifiStateMachine.this.mWifiNative.enableSupplicant()) {
                            WifiStateMachine.this.loge("Failed to start supplicant!");
                            WifiStateMachine.this.setWifiState(4);
                            if (WifiStateMachine.this.mWiFiCHRManager != null) {
                                WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "START_SUPPLICANT_FAILED");
                            }
                            cleanup();
                            break;
                        }
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Supplicant start successful");
                        }
                        if (WifiStateMachine.this.mWifiMonitor.startMonitoring(WifiStateMachine.this.mInterfaceName, true) && WifiStateMachine.this.hasMessages(147458)) {
                            Log.w(WifiStateMachine.TAG, "has message SUP_DISCONNECTION_EVENT when starting supplicant.");
                            WifiStateMachine.this.removeMessages(147458);
                        }
                        WifiStateMachine.this.setSupplicantLogLevel();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStartingState);
                        break;
                    }
                    WifiStateMachine.this.setWifiState(4);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "START_HAL_FAILED");
                    }
                    cleanup();
                    break;
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSoftApState);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class IpManagerCallback extends Callback {
        IpManagerCallback() {
        }

        public void onPreDhcpAction() {
            WifiStateMachine.this.sendMessage(196611);
        }

        public void onPostDhcpAction() {
            WifiStateMachine.this.sendMessage(196612);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults == null) {
                if (WifiStateMachine.this.mWifiStatStore != null) {
                    if (WifiStateMachine.this.mIpManager.getDhcpFlag() == 196609) {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(4);
                    } else {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(5);
                    }
                }
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE);
                WifiStateMachine.this.mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 3);
            } else if ("CMD_TRY_CACHED_IP".equals(dhcpResults.domains)) {
                WifiStateMachine.this.sendMessage(196618);
            } else {
                if (!(WifiStateMachine.this.mWifiStatStore == null || dhcpResults.ipAddress == null || WifiStateMachine.this.mWifiConfigManager == null || WifiStateMachine.this.mNetworkInfo == null)) {
                    WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                    boolean isUsingStaticIp = currentConfig != null && currentConfig.getIpAssignment() == IpAssignment.STATIC;
                    if (isUsingStaticIp) {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(9);
                    } else if (DetailedState.OBTAINING_IPADDR == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                        if ("getCachedDhcpResultsForCurrentConfig".equals(dhcpResults.domains)) {
                            WifiStateMachine.this.mWifiStatStore.updateDhcpState(16);
                        } else {
                            WifiStateMachine.this.mWifiStatStore.updateDhcpState(2);
                        }
                    } else if (DetailedState.CONNECTED == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(3);
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
            if (WifiStateMachine.this.mWiFiCHRManager != null) {
                WifiStateMachine.this.mWiFiCHRManager.uploadDhcpException(DhcpClient.mDhcpError);
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

        public void setFallbackMulticastFilter(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_CONFIG_ND_OFFLOAD, enabled ? 1 : 0);
        }
    }

    class L2ConnectedState extends State {
        L2ConnectedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
            if (WifiStateMachine.this.mEnableRssiPolling) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0);
            }
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.loge("Have NetworkAgent when entering L2Connected");
                WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.CONNECTING);
            WifiStateMachine.this.mNetworkAgent = new WifiNetworkAgent(WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, WifiStateMachine.this.mNetworkCapabilitiesFilter, WifiStateMachine.this.mLinkProperties, 100, WifiStateMachine.this.mNetworkMisc);
            WifiStateMachine.this.mWifiScoreReport.setLowScoreCount(0);
            WifiStateMachine.this.clearTargetBssid("L2ConnectedState");
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(3);
        }

        public void exit() {
            WifiStateMachine.this.mIpManager.stop();
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=").append(Integer.toString(WifiStateMachine.this.mLastNetworkId));
                if (WifiStateMachine.this.mLastBssid != null) {
                    sb.append(" ").append(WifiStateMachine.this.mLastBssid);
                }
            }
            if (!(WifiStateMachine.this.mLastBssid == null && WifiStateMachine.this.mLastNetworkId == -1)) {
                WifiStateMachine.this.handleNetworkDisconnect();
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (!WifiStateMachine.this.processL2ConnectedSetMode(message)) {
                        if (message.arg1 != 1) {
                            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                            WifiStateMachine.this.deferMessage(message);
                            break;
                        }
                    }
                    Log.d("HwWifiStateMachine", "L2ConnectedState process CMD_SET_OPERATIONAL_MODE");
                    break;
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_DISCONNECT, do disconnect");
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 0);
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.cleanWifiScore();
                    if (WifiStateMachine.this.mEnableRssiPollWhenAssociated) {
                        WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    } else {
                        WifiStateMachine.this.mEnableRssiPolling = false;
                    }
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
                    if (WifiStateMachine.this.mEnableRssiPolling) {
                        WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), 3000);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                    if (message.arg1 == WifiStateMachine.this.mRssiPollToken) {
                        if (WifiStateMachine.this.mEnableChipWakeUpWhenAssociated) {
                            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                                WifiStateMachine.this.log(" get link layer stats " + WifiStateMachine.this.mWifiLinkLayerStatsSupported);
                            }
                            WifiLinkLayerStats stats = WifiStateMachine.this.getWifiLinkLayerStats();
                            if (!(stats == null || WifiStateMachine.this.mWifiInfo.getRssi() == -127 || (stats.rssi_mgmt != 0 && stats.beacon_rx != 0))) {
                            }
                            WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                            WifiStateMachine.this.mWifiScoreReport.calculateAndReportScore(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mNetworkAgent, WifiStateMachine.this.mAggressiveHandover, WifiStateMachine.this.mWifiMetrics);
                        }
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), 3000);
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.sendRssiChangeBroadcast(WifiStateMachine.this.mWifiInfo.getRssi());
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT /*131159*/:
                    if (WifiStateMachine.this.isLinkDebouncing()) {
                        WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and debouncing - disconnect " + message.arg1);
                        WifiStateMachine.this.mIsLinkDebouncing = false;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and not debouncing - ignore " + message.arg1);
                    return true;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (message.arg1 == 0 && WifiStateMachine.this.mLastNetworkId != -1 && TelephonyUtil.isSimConfig(WifiStateMachine.this.mWifiConfigManager.getConfiguredNetwork(WifiStateMachine.this.mLastNetworkId))) {
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 6);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    }
                    return false;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL");
                    WifiStateMachine.this.handleSuccessfulIpConfiguration();
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    if (WifiStateMachine.this.isHiLinkActive()) {
                        WifiStateMachine.this.setWifiBackgroundReason(6);
                    }
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.setLastNetIdFromUI(-1);
                    }
                    WifiStateMachine.this.notifyIpConfigCompleted();
                    if (!WifiStateMachine.this.ignoreEnterConnectedState()) {
                        if (!WifiStateMachine.this.isWifiProEvaluatingAP()) {
                            WifiStateMachine.this.sendConnectedState();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                            break;
                        }
                        WifiStateMachine.this.log("****WiFi's connected background, don't let Mobile Data down, keep dual networks up.");
                        WifiStateMachine.this.updateNetworkConcurrently();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
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
                    }
                    WifiStateMachine.this.log("L2ConnectedState, notifyIpConfigLostAndFixedBySce!!!!");
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    if (((String) message.obj) != null) {
                        WifiStateMachine.this.mLastBssid = (String) message.obj;
                        if (WifiStateMachine.this.mLastBssid != null && (WifiStateMachine.this.mWifiInfo.getBSSID() == null || (WifiStateMachine.this.mLastBssid.equals(WifiStateMachine.this.mWifiInfo.getBSSID()) ^ 1) != 0)) {
                            WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                            if (!WifiStateMachine.this.isWifiSelfCuring()) {
                                WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                                break;
                            }
                            WifiStateMachine.this.logd("CMD_ASSOCIATED_BSSID, WifiSelfCuring, ignore associated bssid change message.");
                            break;
                        }
                    }
                    WifiStateMachine.this.logw("Associated command w/o BSSID");
                    break;
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled && message.obj != null) {
                        WifiStateMachine.this.log((String) message.obj);
                    }
                    if (!WifiStateMachine.this.mIpReachabilityDisconnectEnabled) {
                        WifiStateMachine.this.logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        break;
                    }
                    WifiStateMachine.this.handleIpReachabilityLost();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                case WifiStateMachine.CMD_RSSI_THRESHOLD_BREACH /*131236*/:
                    WifiStateMachine.this.processRssiThreshold((byte) message.arg1, message.what);
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.stopRssiMonitoringOffload();
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                    WifiStateMachine.this.handleIPv4Success((DhcpResults) message.obj);
                    WifiStateMachine.this.makeHwDefaultIPTable((DhcpResults) message.obj);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        DhcpResults dr = message.obj;
                        WifiStateMachine.this.mWiFiCHRManager.updateLeaseIP((long) (dr.leaseDuration >= 0 ? dr.leaseDuration : 31536000));
                    }
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                    WifiStateMachine.this.mWifiDiagnostics.captureBugReportData(4);
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiStateMachine.this.mHwWifiCHRService.updateDhcpFailedState();
                    }
                    if (WifiStateMachine.DBG) {
                        WifiConfiguration config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        WifiStateMachine.this.log("DHCP failure count=" + -1);
                    }
                    WifiStateMachine.this.handleIPv4Failure();
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.log("L2ConnectedState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 5);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                    WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    if (!(WifiStateMachine.this.mLastBssid == null || (WifiStateMachine.this.mLastBssid.equals(message.obj) ^ 1) == 0)) {
                        WifiStateMachine.this.mLastBssid = (String) message.obj;
                        WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    }
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT);
                    WifiStateMachine.this.checkSelfCureWifiResult();
                    WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.notifyWifiRoamingCompleted(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.notifyWlanChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(WifiStateMachine.this.mWifiInfo.getFrequency()));
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
                        }
                        WifiStateMachine.this.logd("==connection to same network==");
                        return false;
                    }
                    return false;
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = WifiStateMachine.this.mWifiInfo.getRssi();
                    TxPacketCounters counters = WifiStateMachine.this.mWifiNative.getTxPacketCounters();
                    if (counters == null) {
                        WifiStateMachine.this.replyToMessage(message, 151574, 0);
                        break;
                    }
                    info.txgood = counters.txSucceeded;
                    info.txbad = counters.txFailed;
                    WifiStateMachine.this.replyToMessage(message, 151573, (Object) info);
                    break;
                case 196611:
                    WifiStateMachine.this.handlePreDhcpSetup();
                    if (WifiStateMachine.this.mWifiStatStore != null) {
                        if (DetailedState.OBTAINING_IPADDR != WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                            if (DetailedState.CONNECTED == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                                WifiStateMachine.this.mWifiStatStore.updateDhcpState(10);
                                break;
                            }
                        }
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(0);
                        break;
                    }
                    break;
                case 196612:
                    WifiStateMachine.this.handlePostDhcpSetup();
                    break;
                case 196614:
                    WifiStateMachine.this.mIpManager.completedPreDhcpAction();
                    break;
                case 196618:
                    DhcpResults dhcpResults = WifiStateMachine.this.getCachedDhcpResultsForCurrentConfig();
                    if (dhcpResults != null) {
                        WifiStateMachine.this.stopIpManager();
                        dhcpResults.domains = "getCachedDhcpResultsForCurrentConfig";
                        WifiStateMachine.this.mIpManager;
                        WifiStateMachine.this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withStaticConfiguration(dhcpResults).withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build());
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            ProvisioningConfiguration prov;
            WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
            boolean isUsingStaticIp = currentConfig != null && currentConfig.getIpAssignment() == IpAssignment.STATIC;
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                String key = "";
                if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                    key = WifiStateMachine.this.getCurrentWifiConfiguration().configKey();
                }
                WifiStateMachine.this.log("enter ObtainingIpState netId=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " " + key + " " + " roam=" + WifiStateMachine.this.mIsAutoRoaming + " static=" + isUsingStaticIp);
            }
            WifiStateMachine.this.mIsLinkDebouncing = false;
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.OBTAINING_IPADDR);
            WifiStateMachine.this.clearTargetBssid("ObtainingIpAddress");
            WifiStateMachine.this.stopIpManager();
            WifiStateMachine.this.mIpManager.setHttpProxy(WifiStateMachine.this.getProxyProperties());
            if (!TextUtils.isEmpty(WifiStateMachine.this.mTcpBufferSizes)) {
                WifiStateMachine.this.mIpManager.setTcpBufferSizes(WifiStateMachine.this.mTcpBufferSizes);
            }
            WifiStateMachine.this.tryUseStaticIpForFastConnecting(WifiStateMachine.this.mLastNetworkId);
            if (isUsingStaticIp) {
                prov = IpManager.buildProvisioningConfiguration().withStaticConfiguration(currentConfig.getStaticIpConfiguration()).withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build();
                if (WifiStateMachine.this.mWifiStatStore != null) {
                    WifiStateMachine.this.mWifiStatStore.updateDhcpState(8);
                }
            } else {
                prov = IpManager.buildProvisioningConfiguration().withPreDhcpAction().withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build();
                WifiStateMachine.this.mIpManager.putPendingSSID(WifiStateMachine.this.mWifiInfo.getBSSID());
                WifiStateMachine.this.setForceDhcpDiscovery(WifiStateMachine.this.mIpManager);
            }
            WifiStateMachine.this.mIpManager.startProvisioning(prov);
            WifiStateMachine.this.getWifiLinkLayerStats();
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.SCE_REQUEST_SET_STATIC_IP /*131884*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_SET_STATIC_IP.");
                    WifiStateMachine.this.stopIpManager();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.SCE_START_SET_STATIC_IP, message.obj, 1000);
                    break;
                case WifiStateMachine.SCE_START_SET_STATIC_IP /*131885*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_START_SET_STATIC_IP.");
                    WifiStateMachine.this.handleStaticIpConfig(WifiStateMachine.this.mIpManager, WifiStateMachine.this.mWifiNative, (StaticIpConfiguration) message.obj);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    return false;
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
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
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.roamWatchdogCount++;
            WifiStateMachine.this.logd("Start Roam Watchdog " + WifiStateMachine.this.roamWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER, WifiStateMachine.this.roamWatchdogCount, 0), 15000);
            this.mAssociated = false;
            WifiStateMachine.this.setWiFiProRoamingSSID(null);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                    if (WifiStateMachine.this.roamWatchdogCount == message.arg1) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("roaming watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.mWifiMetrics.endConnectionEvent(9, 1);
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.mRoamFailCount = wifiStateMachine.mRoamFailCount + 1;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiMetrics.logStaEvent(15, 4);
                        WifiStateMachine.this.mWifiNative.disconnect();
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
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    if (!this.mAssociated) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    }
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("roaming and Network connection established");
                    }
                    WifiStateMachine.this.mLastNetworkId = WifiStateMachine.this.lookupFrameworkNetworkId(message.arg1);
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    int reasonCode = message.arg2;
                    if (WifiStateMachine.this.isWiFiProSwitchOnGoing() && WifiStateMachine.this.getWiFiProRoamingSSID() != null) {
                        WifiStateMachine.this.mWifiInfo.setSSID(WifiStateMachine.this.getWiFiProRoamingSSID());
                    }
                    WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true, reasonCode);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    WifiStateMachine.this.clearTargetBssid("RoamingCompleted");
                    WifiStateMachine.this.notifyWifiRoamingCompleted(WifiStateMachine.this.mLastBssid);
                    if (!WifiStateMachine.ENABLE_DHCP_AFTER_ROAM) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    WifiStateMachine.this.log("RoamingState_NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    String bssid = message.obj;
                    String target = "";
                    if (WifiStateMachine.this.mTargetRoamBSSID != null) {
                        target = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + StringUtil.safeDisplayBssid(bssid) + " target=" + target);
                    if (message.arg2 == 15 || message.arg2 == 2) {
                        WifiStateMachine.this.handleDualbandHandoverFailed(3);
                    }
                    if (bssid != null && bssid.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                            WifiStateMachine.this.handleNetworkDisconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        }
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            WifiStateMachine.this.mTargetRoamBSSID = stateChangeResult.BSSID;
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
        }
    }

    class ScanModeState extends State {
        private int mLastOperationMode;

        ScanModeState() {
        }

        public void enter() {
            this.mLastOperationMode = WifiStateMachine.this.mOperationalMode;
            WifiStateMachine.this.mWifiStateTracker.updateState(1);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.getConfiguredNetworks(message);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        if (message.arg1 != 4) {
                            if (WifiStateMachine.this.processScanModeSetMode(message, this.mLastOperationMode)) {
                                Log.d("HwWifiStateMachine", "ScanModeState process CMD_SET_OPERATIONAL_MODE");
                                break;
                            }
                        }
                        WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
                    WifiStateMachine.this.mOperationalMode = 1;
                    if (WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                    }
                    WifiStateMachine.this.setWifiState(2);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SoftApState extends State {
        private String mIfaceName;
        private int mMode;
        private SoftApManager mSoftApManager;

        private class SoftApListener implements Listener {
            /* synthetic */ SoftApListener(SoftApState this$1, SoftApListener -this1) {
                this();
            }

            private SoftApListener() {
            }

            public void onStateChanged(int state, int reason) {
                if (state == 11) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_AP_STOPPED);
                } else if (state == 14) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_AP_FAILURE);
                } else if (state == 13) {
                    WifiStateMachine.this.handleSetWifiApConfigurationHw(String.valueOf(SoftApState.this.mSoftApManager.getApChannel(WifiInjector.getInstance().getWifiApConfigStore().getApConfiguration())));
                }
                WifiStateMachine.this.setWifiApState(state, reason, SoftApState.this.mIfaceName, SoftApState.this.mMode);
            }
        }

        SoftApState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            Message message = WifiStateMachine.this.getCurrentMessage();
            if (message.what != WifiStateMachine.CMD_START_AP) {
                throw new RuntimeException("Illegal transition to SoftApState: " + message);
            }
            SoftApModeConfiguration config = message.obj;
            this.mMode = config.getTargetMode();
            Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, WifiStateMachine.POLICY_OPEN_HOTSPOT);
            if (bundle != null) {
                WifiConfiguration apConfig = config.getWifiConfiguration();
                if (apConfig == null) {
                    apConfig = WifiStateMachine.this.mWifiApConfigStore.getApConfiguration();
                }
                if ((apConfig == null || apConfig.preSharedKey == null) && bundle.getBoolean(WifiStateMachine.VALUE_DISABLE)) {
                    Log.w(WifiStateMachine.TAG, "SoftApState: MDM deny start unsecure soft ap!");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            Toast.makeText(WifiStateMachine.this.mContext, WifiStateMachine.this.mContext.getString(33685942), 0).show();
                        }
                    });
                    WifiStateMachine.this.setWifiApState(14, 0, null, this.mMode);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    return;
                }
            }
            IApInterface apInterface = WifiStateMachine.this.mWifiNative.setupForSoftApMode();
            if (apInterface == null) {
                WifiStateMachine.this.setWifiApState(14, 0, null, this.mMode);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                return;
            }
            try {
                this.mIfaceName = apInterface.getInterfaceName();
            } catch (RemoteException e) {
            }
            WifiStateMachine.this.checkAndSetConnectivityInstance();
            this.mSoftApManager = WifiStateMachine.this.mWifiInjector.makeSoftApManager(WifiStateMachine.this.mNwService, new SoftApListener(this, null), apInterface, config.getWifiConfiguration());
            this.mSoftApManager.start();
            WifiStateMachine.this.mWifiStateTracker.updateState(4);
        }

        public void exit() {
            if (this.mSoftApManager != null) {
                this.mSoftApManager.clearCallbacksAndMessages();
            }
            this.mSoftApManager = null;
            this.mIfaceName = null;
            this.mMode = -1;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    break;
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                    this.mSoftApManager.stop();
                    break;
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                case WifiStateMachine.CMD_AP_STARTED_GET_STA_LIST /*131104*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Get Soft AP current connected stations list");
                    }
                    Object mStaList = null;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        mStaList = this.mSoftApManager.getApLinkedStaList();
                    }
                    WifiStateMachine.this.mReplyChannel.replyToMessage(message, message.what, mStaList);
                    break;
                case WifiStateMachine.CMD_AP_STARTED_SET_MAC_FILTER /*131105*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Set Soft AP MAC filter rule");
                    }
                    String macFilter = message.obj;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        WifiStateMachine.this.handleSetSoftapMacFilter(macFilter);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_AP_STARTED_SET_DISASSOCIATE_STA /*131106*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Set Soft AP disassociate a sta");
                    }
                    String mac = message.obj;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        WifiStateMachine.this.handleSetSoftapDisassociateSta(mac);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    Object channel = null;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        channel = WifiStateMachine.this.mWifiNative.getChannelsForBand(2);
                        if (channel != null && channel.length > 0) {
                            for (int i = 0; i < channel.length; i++) {
                                channel[i] = WifiCommonUtils.convertFrequencyToChannelNumber(channel[i]);
                            }
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, channel);
                    break;
                case WifiMonitor.AP_LINKED_EVENT /*147856*/:
                    WifiStateMachine.this.log("SoftApState: AP_LINKED_EVENT");
                    this.mSoftApManager.notifyApLinkedStaListChange((Bundle) message.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SupplicantStartedState extends State {
        SupplicantStartedState() {
        }

        public void enter() {
            boolean z;
            if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                WifiStateMachine.this.logd("SupplicantStartedState enter");
            }
            WifiStateMachine.this.mWifiNative.setExternalSim(true);
            WifiStateMachine.this.setRandomMacOui();
            WifiStateMachine.this.mWifiConfigManager.setSupportWapiType();
            WifiStateMachine.this.mCountryCode.setCountryCode(WifiStateMachine.this.mCountryCode.getCountryCodeSentToDriver());
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            if (WifiStateMachine.this.mWifiScanner == null) {
                WifiStateMachine.this.mWifiScanner = WifiStateMachine.this.mWifiInjector.getWifiScanner();
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine.this.mWifiConnectivityManager = WifiStateMachine.this.mWifiInjector.makeWifiConnectivityManager(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.hasConnectionRequests());
                    WifiConnectivityManager wifiConnectivityManager = WifiStateMachine.this.mWifiConnectivityManager;
                    if (WifiStateMachine.this.mUntrustedReqCount > 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    wifiConnectivityManager.setUntrustedConnectionAllowed(z);
                    WifiStateMachine.this.mWifiConnectivityManager.handleScreenStateChanged(WifiStateMachine.this.mScreenOn);
                }
            }
            WifiStateMachine.this.mWifiDiagnostics.startLogging(WifiStateMachine.this.mVerboseLoggingEnabled);
            WifiStateMachine.this.mIsRunning = true;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV6Packets();
            if (WifiStateMachine.this.enterDriverStartedStateByMode()) {
                Log.d("HwWifiStateMachine", "DriverStartedState enter transitionTo mDisconnectedState");
            } else if (WifiStateMachine.this.mOperationalMode == 2 || WifiStateMachine.this.mOperationalMode == 3) {
                WifiStateMachine.this.mWifiNative.disconnect();
                WifiStateMachine.this.setWifiState(1);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
            } else if (WifiStateMachine.this.mOperationalMode == 1) {
                WifiStateMachine.this.setWifiState(2);
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
            } else if (WifiStateMachine.this.mOperationalMode == 4) {
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
            }
            WifiNative -get92 = WifiStateMachine.this.mWifiNative;
            if (WifiStateMachine.this.mSuspendOptNeedsDisabled == 0) {
                z = WifiStateMachine.this.mUserWantsSuspendOpt.get();
            } else {
                z = false;
            }
            -get92.setSuspendOptimizations(z);
            WifiStateMachine.this.mWifiNative.setPowerSave(true);
            if (WifiStateMachine.this.mP2pSupported && WifiStateMachine.this.mOperationalMode == 1) {
                WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
            }
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 3);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.mWifiNative.enableStaAutoReconnect(false);
            WifiStateMachine.this.mWifiNative.setConcurrencyPriority(true);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                    if (WifiStateMachine.this.mWifiStatStore != null) {
                        WifiStateMachine.this.mWifiStatStore.updateWifiTriggerState(false);
                    }
                    if (!WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    break;
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    WifiStateMachine.this.loge("Failed to start soft AP with a running supplicant");
                    WifiStateMachine.this.setWifiApState(14, 0, null, -1);
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.getWifiLinkLayerStats());
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    if (WifiStateMachine.this.mOperationalMode == 4) {
                        WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(2, true);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizationsNative(2, false);
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(4, false);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizationsNative(4, true);
                    if (message.arg2 == 1) {
                        WifiStateMachine.this.mSuspendWakeLock.release();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_TDLS /*131164*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mWifiNative.startTdls(message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (message.arg1 == 1) {
                        Log.d(WifiStateMachine.TAG, "enable EAP-SIM/AKA/AKA' networks since SIM was loaded");
                        WifiStateMachine.this.mWifiConfigManager.enableSimNetworks();
                    }
                    WifiStateMachine.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    WifiStateMachine.this.mWifiConfigManager.resetSimNetworks();
                    break;
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mTargetRoamBSSID = (String) message.obj;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    int slot = message.arg1;
                    int ret = WifiStateMachine.this.stopWifiIPPacketOffload(slot);
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /*131238*/:
                    WifiStateMachine.this.mWifiConnectivityManager.enable(message.arg1 == 1);
                    break;
                case WifiStateMachine.CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED /*131239*/:
                    boolean allowed = message.arg1 > 0;
                    boolean old_state = WifiStateMachine.this.mEnableAutoJoinWhenAssociated;
                    WifiStateMachine.this.mEnableAutoJoinWhenAssociated = allowed;
                    if (!old_state && allowed && WifiStateMachine.this.mScreenOn && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState) {
                        WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan();
                        break;
                    }
                case WifiStateMachine.CMD_CONFIG_ND_OFFLOAD /*131276*/:
                    WifiStateMachine.this.mWifiNative.configureNeighborDiscoveryOffload(message.arg1 > 0);
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    int[] channel = WifiStateMachine.this.mWifiNative.getChannelsForBand(2);
                    if (channel != null && channel.length > 0) {
                        for (int i = 0; i < channel.length; i++) {
                            channel[i] = WifiCommonUtils.convertFrequencyToChannelNumber(channel[i]);
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) channel);
                    break;
                case WifiStateMachine.CMD_WIFI_SCAN_REJECT_SEND_SCAN_RESULT /*131578*/:
                    WifiStateMachine.this.logd("**wifi_scan_reject sendScanResultsAvailableBroadcast*****");
                    WifiStateMachine.this.sendScanResultsAvailableBroadcast(false);
                    break;
                case 147458:
                    WifiStateMachine.this.loge("Connection lost, restart supplicant");
                    WifiStateMachine.this.handleSupplicantConnectionLoss(true);
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
                    if (WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    } else {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    }
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_START_SUPPLICANT, 5000);
                    break;
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    WifiStateMachine.this.setScanResults();
                    if (WifiStateMachine.this.isWlanSettingsActivity()) {
                        WifiStateMachine.this.mSendScanResultsBroadcast = true;
                    }
                    if (WifiStateMachine.this.mIsFullScanOngoing || WifiStateMachine.this.mSendScanResultsBroadcast || WifiStateMachine.this.mAllowSendHiLinkScanResultsBroadcast || WifiStateMachine.this.isWifiProEvaluatingAP()) {
                        boolean scanSucceeded = message.what == WifiMonitor.SCAN_RESULTS_EVENT;
                        if (!(WifiStateMachine.this.mIsFullScanOngoing || (WifiStateMachine.this.mSendScanResultsBroadcast ^ 1) == 0)) {
                            WifiStateMachine.this.logd("******send sendScanResultsAvailableBroadcast*****");
                        }
                        if (scanSucceeded && WifiStateMachine.this.mIsFullScanOngoing) {
                            WifiStateMachine.this.updateLastScanRequestTimestamp();
                        }
                        WifiStateMachine.this.sendHilinkscanResultBroadcast();
                    } else {
                        WifiStateMachine.this.notifyWifiScanResultsAvailable(message.what == WifiMonitor.SCAN_RESULTS_EVENT);
                    }
                    if (WifiStateMachine.NOTIFY_OPEN_NETWORKS_VALUE) {
                        WifiStateMachine.this.sendScanResultsAvailableBroadcast(message.what == WifiMonitor.SCAN_RESULTS_EVENT);
                    }
                    WifiStateMachine.this.mSendScanResultsBroadcast = false;
                    WifiStateMachine.this.mIsScanOngoing = false;
                    WifiStateMachine.this.mIsFullScanOngoing = false;
                    if (WifiStateMachine.this.mBufferedScanMsg.size() > 0) {
                        WifiStateMachine.this.sendMessage((Message) WifiStateMachine.this.mBufferedScanMsg.remove());
                        break;
                    }
                    break;
                case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                    WifiStateMachine.this.mPasspointManager.notifyANQPDone((AnqpEvent) message.obj);
                    break;
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                    WifiStateMachine.this.mPasspointManager.notifyIconDone((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
                    WifiStateMachine.this.mPasspointManager.receivedWnmFrame((WnmData) message.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mWifiDiagnostics.stopLogging();
            WifiStateMachine.this.mIsRunning = false;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.setWiFiProScanResultList(WifiStateMachine.this.syncGetScanResultsList());
            WifiStateMachine.this.mScanResults = new ArrayList();
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 1);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.mBufferedScanMsg.clear();
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(false);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
        }
    }

    class SupplicantStartingState extends State {
        SupplicantStartingState() {
        }

        private void initializeWpsDetails() {
            String detail = WifiStateMachine.this.mPropertyService.get("ro.product.name", "");
            if (!WifiStateMachine.this.mWifiNative.setDeviceName(detail)) {
                WifiStateMachine.this.loge("Failed to set device name " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.manufacturer", "");
            if (!WifiStateMachine.this.mWifiNative.setManufacturer(detail)) {
                WifiStateMachine.this.loge("Failed to set manufacturer " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.model", "");
            if (!WifiStateMachine.this.mWifiNative.setModelName(detail)) {
                WifiStateMachine.this.loge("Failed to set model name " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.model", "");
            if (!WifiStateMachine.this.mWifiNative.setModelNumber(detail)) {
                WifiStateMachine.this.loge("Failed to set model number " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.serialno", "");
            if (!WifiStateMachine.this.mWifiNative.setSerialNumber(detail)) {
                WifiStateMachine.this.loge("Failed to set serial number " + detail);
            }
            if (!WifiStateMachine.this.mWifiNative.setConfigMethods("physical_display virtual_push_button")) {
                WifiStateMachine.this.loge("Failed to set WPS config methods");
            }
            if (!WifiStateMachine.this.mWifiNative.setDeviceType(WifiStateMachine.this.mPrimaryDeviceType)) {
                WifiStateMachine.this.loge("Failed to set primary device type " + WifiStateMachine.this.mPrimaryDeviceType);
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (!WifiStateMachine.this.processSupplicantStartingSetMode(message)) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    break;
                case 147457:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Supplicant connection established");
                    }
                    WifiStateMachine.this.mSupplicantRestartCount = 0;
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
                    WifiStateMachine.this.mLastBssid = null;
                    WifiStateMachine.this.mLastNetworkId = -1;
                    WifiStateMachine.this.mLastSignalLevel = -1;
                    WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress());
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateStaMAC(WifiStateMachine.this.mWifiInfo.getMacAddress());
                    }
                    WifiStateMachine.this.loadAndEnableAllNetworksByMode();
                    if (!WifiStateMachine.this.mWifiConfigManager.migrateFromLegacyStore()) {
                        Log.e(WifiStateMachine.TAG, "Failed to migrate from legacy config store");
                    }
                    initializeWpsDetails();
                    if (WifiStateMachineHisiExt.hisiWifiEnabled()) {
                        WifiStateMachine.this.mWifiStateMachineHisiExt.startWifiForP2pCheck();
                    }
                    WifiStateMachine.this.sendSupplicantConnectionChangedBroadcast(true);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStartedState);
                    break;
                case 147458:
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mSupplicantRestartCount = wifiStateMachine.mSupplicantRestartCount + 1 > 5) {
                        if (WifiStateMachine.this.mWiFiCHRManager != null) {
                            WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "CONNECT_SUPPLICANT_FAILED");
                        }
                        WifiStateMachine.this.loge("Failed " + WifiStateMachine.this.mSupplicantRestartCount + " times to start supplicant, unload driver");
                        WifiStateMachine.this.mSupplicantRestartCount = 0;
                        WifiStateMachine.this.setWifiState(4);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    WifiStateMachine.this.loge("Failed to setup control channel, restart supplicant");
                    WifiStateMachine.this.mWifiMonitor.stopAllMonitoring();
                    WifiStateMachine.this.mWifiNative.disableSupplicant();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_START_SUPPLICANT, 5000);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SupplicantStoppingState extends State {
        SupplicantStoppingState() {
        }

        public void enter() {
            WifiStateMachine.this.handleNetworkDisconnect();
            String suppState = System.getProperty("init.svc.wpa_supplicant");
            if (suppState == null) {
                suppState = "unknown";
            }
            WifiStateMachine.this.setWifiState(0);
            WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
            WifiStateMachine.this.logd("SupplicantStoppingState: disableSupplicant  init.svc.wpa_supplicant=" + suppState);
            if (WifiStateMachine.this.mWifiNative.disableSupplicant()) {
                WifiStateMachine.this.mWifiNative.closeSupplicantConnection();
                WifiStateMachine.this.sendSupplicantConnectionChangedBroadcast(false);
                WifiStateMachine.this.setWifiState(1);
            } else {
                WifiStateMachine.this.handleSupplicantConnectionLoss(true);
                if (WifiStateMachine.this.mWiFiCHRManager != null) {
                    WifiStateMachine.this.mWiFiCHRManager.handleSupplicantException();
                }
            }
            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
        }
    }

    private class UntrustedWifiNetworkFactory extends NetworkFactory {
        public UntrustedWifiNetworkFactory(Looper l, Context c, String tag, NetworkCapabilities f) {
            super(l, c, tag, f);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mUntrustedReqCount = wifiStateMachine.mUntrustedReqCount + 1 == 1 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        if (WifiStateMachine.this.mConnectionReqCount == 0) {
                            WifiStateMachine.this.mWifiConnectivityManager.enable(true);
                        }
                        WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(true);
                    }
                }
            }
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mUntrustedReqCount = wifiStateMachine.mUntrustedReqCount - 1 == 0 && WifiStateMachine.this.mWifiConnectivityManager != null) {
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

    class WaitForP2pDisableState extends State {
        private State mTransitionToState;

        WaitForP2pDisableState() {
        }

        public void enter() {
            switch (WifiStateMachine.this.getCurrentMessage().what) {
                case 147458:
                    this.mTransitionToState = WifiStateMachine.this.mInitialState;
                    break;
                default:
                    this.mTransitionToState = WifiStateMachine.this.mSupplicantStoppingState;
                    break;
            }
            if (WifiStateMachine.this.p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ)) {
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DISABLE_P2P_WATCHDOG_TIMER, WifiStateMachine.this.mDisableP2pWatchdogCount, 0), 2000);
            } else {
                WifiStateMachine.this.transitionTo(this.mTransitionToState);
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                    if (WifiStateMachine.this.mDisableP2pWatchdogCount == message.arg1) {
                        WifiStateMachine.this.logd("Timeout waiting for CMD_DISABLE_P2P_RSP");
                        WifiStateMachine.this.transitionTo(this.mTransitionToState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                    WifiStateMachine.this.transitionTo(this.mTransitionToState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class WifiNetworkAgent extends HwNetworkAgent {
        public WifiNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                }
                WifiStateMachine.this.unwantedNetwork(0);
            }
        }

        protected void networkStatus(int status, String redirectUrl) {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (status == 2) {
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.unwantedNetwork(1);
                } else if (status == 1) {
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.mWifiMetrics.logStaEvent(14);
                    WifiStateMachine.this.doNetworkStatus(status);
                } else if (status == 3) {
                    WifiStateMachine.this.reportPortalNetworkStatus();
                } else if (status == 4) {
                    WifiStateMachine.this.notifyWifiConnectedBackgroundReady();
                }
            }
        }

        protected void saveAcceptUnvalidated(boolean accept) {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
            }
        }

        protected void startPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        protected void stopPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        protected void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
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
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            WifiStateMachine.this.mRssiRanges = rssiRange;
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
        }

        protected void preventAutomaticReconnect() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                WifiStateMachine.this.unwantedNetwork(2);
            }
        }
    }

    private class WifiNetworkFactory extends NetworkFactory {
        public WifiNetworkFactory(Looper l, Context c, String TAG, NetworkCapabilities f) {
            super(l, c, TAG, f);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (wifiStateMachine.mConnectionReqCount = wifiStateMachine.mConnectionReqCount + 1 == 1 && WifiStateMachine.this.mWifiConnectivityManager != null && WifiStateMachine.this.mUntrustedReqCount == 0) {
                    WifiStateMachine.this.mWifiConnectivityManager.enable(true);
                }
            }
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            synchronized (WifiStateMachine.this.mWifiReqCountLock) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (wifiStateMachine.mConnectionReqCount = wifiStateMachine.mConnectionReqCount - 1 == 0 && WifiStateMachine.this.mWifiConnectivityManager != null && WifiStateMachine.this.mUntrustedReqCount == 0) {
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
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case 151553:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_START_CONNECT /*131215*/:
                case WifiStateMachine.CMD_START_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_WPS_PIN_RETRY /*131576*/:
                    WpsResult wpsResult = message.obj;
                    if (!TextUtils.isEmpty(wpsResult.pin)) {
                        WifiStateMachine.this.mWifiNative.startWpsPinKeypad(wpsResult.pin);
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_WPS_PIN_RETRY, wpsResult, 50000);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    NetworkUpdateResult networkUpdateResult = loadNetworksFromSupplicantAfterWps();
                    if (networkUpdateResult != null) {
                        message.arg1 = networkUpdateResult.getNetworkId();
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
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Network connection lost");
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    return false;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                        WifiStateMachine.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                    break;
                case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (WifiStateMachine.this.mVerboseLoggingEnabled) {
                            WifiStateMachine.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    }
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
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
                    if (WifiStateMachine.this.mWifiNative.cancelWps()) {
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

        private NetworkUpdateResult loadNetworksFromSupplicantAfterWps() {
            Map<String, WifiConfiguration> configs = new HashMap();
            if (WifiStateMachine.this.mWifiNative.migrateNetworksFromSupplicant(configs, new SparseArray())) {
                NetworkUpdateResult result = null;
                for (Entry<String, WifiConfiguration> entry : configs.entrySet()) {
                    WifiConfiguration config = (WifiConfiguration) entry.getValue();
                    config.networkId = -1;
                    result = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, this.mSourceMessage.sendingUid);
                    if (!result.isSuccess()) {
                        WifiStateMachine.this.loge("Failed to add network after WPS: " + entry.getValue());
                        return null;
                    } else if (!WifiStateMachine.this.mWifiConfigManager.enableNetwork(result.getNetworkId(), true, this.mSourceMessage.sendingUid)) {
                        WifiStateMachine.this.loge("Failed to enable network after WPS: " + entry.getValue());
                        return null;
                    }
                }
                return result;
            }
            WifiStateMachine.this.loge("Failed to load networks from wpa_supplicant after Wps");
            return null;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    protected void loge(String s) {
        Log.e(getName(), s);
    }

    protected void logd(String s) {
        Log.d(getName(), s);
    }

    protected void log(String s) {
        Log.d(getName(), s);
    }

    /* synthetic */ void lambda$-com_android_server_wifi_WifiStateMachine_14512() {
        sendMessage(CMD_VENDOR_HAL_HWBINDER_DEATH);
    }

    public void onRssiThresholdBreached(byte curRssi) {
        if (this.mVerboseLoggingEnabled) {
            Log.e(TAG, "onRssiThresholdBreach event. Cur Rssi = " + curRssi);
        }
        sendMessage(CMD_RSSI_THRESHOLD_BREACH, curRssi);
    }

    public void processRssiThreshold(byte curRssi, int reason) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + curRssi);
            return;
        }
        for (int i = 0; i < this.mRssiRanges.length; i++) {
            if (curRssi < this.mRssiRanges[i]) {
                byte maxRssi = this.mRssiRanges[i];
                byte minRssi = this.mRssiRanges[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities(getCurrentWifiConfiguration());
                Log.d(TAG, "Re-program RSSI thresholds for " + smToString(reason) + ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi + " ret=" + startRssiMonitoringOffload(maxRssi, minRssi));
                break;
            }
        }
    }

    public boolean clearTargetBssid(String dbg) {
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (config == null) {
            return false;
        }
        String bssid = "any";
        if (config.BSSID != null) {
            bssid = config.BSSID;
            Log.d(TAG, "force BSSID to " + bssid + "due to config");
        }
        if (this.mVerboseLoggingEnabled) {
            logd(dbg + " clearTargetBssid " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        return this.mWifiNative.setConfiguredNetworkBSSID("any");
    }

    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null || bssid == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "setTargetBssid set to " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
        return true;
    }

    private TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
    }

    public WifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative) {
        super(TAG, looper);
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiNative = wifiNative;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mInterfaceName = this.mWifiNative.getInterfaceName();
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE, "");
        this.mBatteryStats = Stub.asInterface(this.mFacade.getService("batterystats"));
        this.mWifiStateTracker = wifiInjector.getWifiStateTracker();
        this.mNwService = INetworkManagementService.Stub.asInterface(this.mFacade.getService("network_management"));
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        this.mPasspointManager = this.mWifiInjector.getPasspointManager();
        this.mWifiMonitor = this.mWifiInjector.getWifiMonitor();
        this.mWifiDiagnostics = this.mWifiInjector.makeWifiDiagnostics(this.mWifiNative);
        this.mWifiInfo = new WifiInfo();
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mLinkProperties = new LinkProperties();
        if (WifiStateMachineHisiExt.hisiWifiEnabled()) {
            this.mWifiStateMachineHisiExt = new WifiStateMachineHisiExt(this.mContext, this.mWifiConfigManager, this.mWifiState, this.mWifiApState);
        }
        this.uploader = DataUploader.getInstance();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mIpManager = this.mFacade.makeIpManager(this.mContext, this.mInterfaceName, new IpManagerCallback());
        this.mIpManager.setMulticastFilter(true);
        this.mNoNetworksPeriodicScan = this.mContext.getResources().getInteger(17694902);
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039815);
        this.mCountryCode = countryCode;
        this.mWifiScoreReport = new WifiScoreReport(this.mContext, this.mWifiConfigManager);
        this.mUserWantsSuspendOpt.set(this.mFacade.getIntegerSetting(this.mContext, "wifi_suspend_optimizations_enabled", 1) == 1);
        this.mNetworkCapabilitiesFilter.addTransportType(1);
        this.mNetworkCapabilitiesFilter.addCapability(12);
        this.mNetworkCapabilitiesFilter.addCapability(11);
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
                    WifiStateMachine.this.log("receive action: " + action);
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 1);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateScreenState(true);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 0);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateScreenState(false);
                    }
                }
            }
        }, filter);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                AtomicBoolean -get77 = WifiStateMachine.this.mUserWantsSuspendOpt;
                if (WifiStateMachine.this.mFacade.getIntegerSetting(WifiStateMachine.this.mContext, "wifi_suspend_optimizations_enabled", 1) != 1) {
                    z = false;
                }
                -get77.set(z);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiScanAlwaysState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_sleep_policy"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiSleepPolicyState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_networks_available_notification_on"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiNetworkNotificationState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("wifi_to_pdp"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiToPdpState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("smart_network_switching"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiProState(WifiStateMachine.this.mContext);
                }
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
                WifiStateMachine.this.mIsRunning = false;
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                if (getSendingUserId() == -1) {
                    z = true;
                }
                wifiStateMachine.mIsRealReboot = z;
                Log.d(WifiStateMachine.TAG, "onReceive: mIsRealReboot = " + WifiStateMachine.this.mIsRealReboot);
                if (WifiStateMachine.DBG) {
                    WifiStateMachine.this.log("shut down so update battery");
                }
                WifiStateMachine.this.updateBatteryWorkSource(null);
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039817);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957058);
        this.mThresholdQualifiedRssi24 = context.getResources().getInteger(17694896);
        this.mThresholdQualifiedRssi5 = context.getResources().getInteger(17694897);
        this.mThresholdSaturatedRssi24 = context.getResources().getInteger(17694894);
        this.mThresholdSaturatedRssi5 = context.getResources().getInteger(17694895);
        this.mThresholdMinimumRssi5 = context.getResources().getInteger(17694891);
        this.mThresholdMinimumRssi24 = context.getResources().getInteger(17694890);
        this.mEnableLinkDebouncing = this.mContext.getResources().getBoolean(17957053);
        this.mEnableChipWakeUpWhenAssociated = true;
        this.mEnableRssiPollWhenAssociated = true;
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mSupplicantStartingState, this.mDefaultState);
        addState(this.mSupplicantStartedState, this.mDefaultState);
        addState(this.mScanModeState, this.mSupplicantStartedState);
        addState(this.mConnectModeState, this.mSupplicantStartedState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        addState(this.mWpsRunningState, this.mConnectModeState);
        addState(this.mWaitForP2pDisableState, this.mSupplicantStartedState);
        addState(this.mSupplicantStoppingState, this.mDefaultState);
        addState(this.mSoftApState, this.mDefaultState);
        setInitialState(this.mInitialState);
        if (ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) {
        }
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        start();
        handleScreenStateChanged(powerManager.isInteractive());
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
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_FAILED_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, 147457, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, 147458, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_START_OKC_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.EVENT_ANT_CORE_ROB, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AP_LINKED_EVENT, getHandler());
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mHwWifiDFTUtil = HwWifiServiceFactory.getHwWifiDFTUtil();
        this.mHwMssHandler = HwWifiServiceFactory.getHwMSSHandlerManager(this.mContext, this.mWifiNative, this.mWifiInfo);
        Intent intent = new Intent("wifi_scan_available");
        intent.addFlags(67108864);
        intent.putExtra("scan_enabled", 1);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        HwWifiServiceFactory.getIsmCoexWifiStateTrack(context, this, this.mWifiNative);
        HwWifiServiceFactory.getHwWifiDevicePolicy().registerBroadcasts(this.mContext);
    }

    private void stopIpManager() {
        handlePostDhcpSetup();
        this.mIpManager.stop();
    }

    PendingIntent getPrivateBroadcast(String action, int requestCode) {
        Intent intent = new Intent(action, null);
        intent.addFlags(67108864);
        intent.setPackage("android");
        return this.mFacade.getBroadcast(this.mContext, requestCode, intent, 0);
    }

    void setSupplicantLogLevel() {
        this.mWifiNative.setSupplicantLogLevel(this.mVerboseLoggingEnabled);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
            setLogRecSize(ActivityManager.isLowRamDeviceStatic() ? 200 : POLL_RSSI_INTERVAL_MSECS);
        } else {
            this.mVerboseLoggingEnabled = false;
            setLogRecSize(100);
        }
        configureVerboseHalLogging(this.mVerboseLoggingEnabled);
        setSupplicantLogLevel();
        if (this.mWifiScanner != null) {
            this.mWifiScanner.enableVerboseLogging(verbose);
        }
        this.mCountryCode.enableVerboseLogging(verbose);
        this.mWifiScoreReport.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiDiagnostics.startLogging(this.mVerboseLoggingEnabled);
        this.mWifiMonitor.enableVerboseLogging(verbose);
        this.mWifiNative.enableVerboseLogging(verbose);
        this.mWifiConfigManager.enableVerboseLogging(verbose);
        this.mSupplicantStateTracker.enableVerboseLogging(verbose);
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    int getAggressiveHandover() {
        return this.mAggressiveHandover;
    }

    void enableAggressiveHandover(int enabled) {
        this.mAggressiveHandover = enabled;
    }

    public void clearANQPCache() {
    }

    public void setAllowScansWithTraffic(int enabled) {
        this.mAlwaysEnableScansWhileAssociated = enabled;
    }

    public int getAllowScansWithTraffic() {
        return this.mAlwaysEnableScansWhileAssociated;
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        sendMessage(CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED, enabled ? 1 : 0);
        return true;
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return this.mEnableAutoJoinWhenAssociated;
    }

    public boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039816);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = new byte[]{(byte) (Integer.parseInt(ouiParts[0], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[1], 16) & Constants.BYTE_MASK), (byte) (Integer.parseInt(ouiParts[2], 16) & Constants.BYTE_MASK)};
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    public boolean clearRandomMacOui() {
        byte[] ouiBytes = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        logd("Clear random OUI");
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    public void gameKOGAdjustSpeed(int mode) {
        this.mWifiNative.gameKOGAdjustSpeed(this.mWifiInfo.getFrequency(), mode);
    }

    private int lookupFrameworkNetworkId(int supplicantNetworkId) {
        return this.mWifiNative.getFrameworkNetworkId(supplicantNetworkId);
    }

    private boolean connectToUserSelectNetwork(int netId, int uid, boolean forceReconnect) {
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
        if (this.mWifiConfigManager.enableNetwork(netId, true, uid) && (this.mWifiConfigManager.checkAndUpdateLastConnectUid(netId, uid) ^ 1) == 0) {
            this.mWifiConnectivityManager.setUserConnectChoice(netId);
        } else {
            logi("connectToUserSelectNetwork Allowing uid " + uid + " with insufficient permissions to connect=" + netId);
        }
        if (forceReconnect || this.mWifiInfo.getNetworkId() != netId) {
            this.mWifiConnectivityManager.prepareForForcedConnection(netId);
            startConnectToUserSelectNetwork(netId, "any");
        } else {
            logi("connectToUserSelectNetwork already connecting/connected=" + netId);
        }
        return true;
    }

    public boolean isP2pConnected() {
        return this.mP2pConnected.get();
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    public void startScan(int callingUid, int scanCounter, ScanSettings settings, WorkSource workSource) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, settings);
        bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, workSource);
        bundle.putLong(SCAN_REQUEST_TIME, this.mClock.getWallClockMillis());
        sendMessage(CMD_START_SCAN, callingUid, scanCounter, bundle);
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

    String reportOnTime() {
        long now = this.mClock.getWallClockMillis();
        StringBuilder sb = new StringBuilder();
        int on = this.mOnTime - this.mOnTimeLastReport;
        this.mOnTimeLastReport = this.mOnTime;
        int tx = this.mTxTime - this.mTxTimeLastReport;
        this.mTxTimeLastReport = this.mTxTime;
        int rx = this.mRxTime - this.mRxTimeLastReport;
        this.mRxTimeLastReport = this.mRxTime;
        int period = (int) (now - this.lastOntimeReportTimeStamp);
        this.lastOntimeReportTimeStamp = now;
        sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(tx), Integer.valueOf(rx), Integer.valueOf(period)}));
        on = this.mOnTime - this.mOnTimeScreenStateChange;
        period = (int) (now - this.lastScreenStateChangeTimeStamp);
        sb.append(String.format(" from screen [on:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(period)}));
        return sb.toString();
    }

    WifiLinkLayerStats getWifiLinkLayerStats() {
        WifiLinkLayerStats stats = null;
        if (this.mWifiLinkLayerStatsSupported > 0) {
            String name = "wlan0";
            stats = this.mWifiNative.getWifiLinkLayerStats(name);
            if (name != null && stats == null && this.mWifiLinkLayerStatsSupported > 0) {
                this.mWifiLinkLayerStatsSupported--;
            } else if (stats != null) {
                this.lastLinkLayerStatsUpdate = this.mClock.getWallClockMillis();
                this.mOnTime = stats.on_time;
                this.mTxTime = stats.tx_time;
                this.mRxTime = stats.rx_time;
                this.mRunningBeaconCount = stats.beacon_rx;
            }
        }
        if (stats == null || this.mWifiLinkLayerStatsSupported <= 0) {
            this.mWifiInfo.updatePacketRates(this.mFacade.getTxPackets(this.mInterfaceName), this.mFacade.getRxPackets(this.mInterfaceName));
        } else {
            this.mWifiInfo.updatePacketRates(stats, this.lastLinkLayerStatsUpdate);
        }
        return stats;
    }

    int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        int ret = this.mWifiNative.startSendingOffloadedPacket(slot, packetData, intervalSeconds * 1000);
        if (ret == 0) {
            return 0;
        }
        loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds + "): hardware error " + ret);
        return -31;
    }

    int stopWifiIPPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(slot);
        if (ret == 0) {
            return 0;
        }
        loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
        return -31;
    }

    int startRssiMonitoringOffload(byte maxRssi, byte minRssi) {
        return this.mWifiNative.startRssiMonitoring(maxRssi, minRssi, this);
    }

    int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring();
    }

    private void handleScanRequest(Message message) {
        ScanSettings settings = null;
        Parcelable parcelable = null;
        Bundle bundle = message.obj;
        if (bundle != null) {
            settings = (ScanSettings) bundle.getParcelable(CUSTOMIZED_SCAN_SETTING);
            parcelable = (WorkSource) bundle.getParcelable(CUSTOMIZED_SCAN_WORKSOURCE);
        }
        Set freqs = null;
        if (!(settings == null || settings.channelSet == null)) {
            freqs = new HashSet();
            for (WifiChannel channel : settings.channelSet) {
                freqs.add(Integer.valueOf(channel.freqMHz));
            }
        }
        if (startScanNative(freqs, this.mWifiConfigManager.retrieveHiddenNetworkList(), parcelable)) {
            if (freqs == null) {
                this.mBufferedScanMsg.clear();
            }
            this.messageHandlingStatus = 1;
            return;
        }
        if (!this.mIsScanOngoing) {
            if (this.mBufferedScanMsg.size() > 0) {
                sendMessage((Message) this.mBufferedScanMsg.remove());
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
        } else if (this.mIsFullScanOngoing) {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
        } else {
            if (freqs == null) {
                this.mBufferedScanMsg.clear();
            }
            if (this.mBufferedScanMsg.size() < 10) {
                this.mBufferedScanMsg.add(obtainMessage(CMD_START_SCAN, message.arg1, message.arg2, bundle));
            } else {
                bundle = new Bundle();
                bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, null);
                bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, parcelable);
                Message msg = obtainMessage(CMD_START_SCAN, message.arg1, message.arg2, bundle);
                this.mBufferedScanMsg.clear();
                this.mBufferedScanMsg.add(msg);
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_LOOPED;
        }
    }

    private boolean startScanNative(Set<Integer> freqs, List<HiddenNetwork> hiddenNetworkList, WorkSource workSource) {
        boolean z;
        WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
        if (freqs == null) {
            settings.band = 7;
        } else {
            settings.band = 0;
            int index = 0;
            settings.channels = new ChannelSpec[freqs.size()];
            for (Integer freq : freqs) {
                int index2 = index + 1;
                settings.channels[index] = new ChannelSpec(freq.intValue());
                index = index2;
            }
        }
        settings.reportEvents = 3;
        settings.hiddenNetworks = (HiddenNetwork[]) hiddenNetworkList.toArray(new HiddenNetwork[hiddenNetworkList.size()]);
        this.mWifiScanner.startScan(settings, new ScanListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason, String description) {
                WifiStateMachine.this.mIsScanOngoing = false;
                WifiStateMachine.this.mIsFullScanOngoing = false;
            }

            public void onResults(ScanData[] results) {
            }

            public void onFullResult(ScanResult fullScanResult) {
            }

            public void onPeriodChanged(int periodInMs) {
            }
        }, workSource);
        this.mIsScanOngoing = true;
        if (freqs == null) {
            z = true;
        } else {
            z = false;
        }
        this.mIsFullScanOngoing = z;
        this.lastScanFreqs = freqs;
        return true;
    }

    public void setSupplicantRunning(boolean enable) {
        if (enable) {
            sendMessage(CMD_START_SUPPLICANT);
            return;
        }
        if (hasMessages(CMD_START_SUPPLICANT)) {
            Log.w(TAG, "has message CMD_START_SUPPLICANT when stop supplicant.");
            removeMessages(CMD_START_SUPPLICANT);
            if (hasMessages(CMD_SET_OPERATIONAL_MODE)) {
                removeMessages(CMD_SET_OPERATIONAL_MODE);
            }
        }
        sendMessage(CMD_STOP_SUPPLICANT);
    }

    public void setHostApRunning(SoftApModeConfiguration wifiConfig, boolean enable) {
        if (enable) {
            sendMessage(CMD_START_AP, wifiConfig);
        } else {
            sendMessage(CMD_STOP_AP);
        }
    }

    public void setWifiApConfiguration(WifiConfiguration config) {
        this.mWifiApConfigStore.setApConfiguration(config);
    }

    public WifiConfiguration syncGetWifiApConfiguration() {
        return this.mWifiApConfigStore.getApConfiguration();
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

    public int syncGetWifiApState() {
        return this.mWifiApState.get();
    }

    public String syncGetWifiApStateByName() {
        switch (this.mWifiApState.get()) {
            case 10:
                return "disabling";
            case 11:
                return "disabled";
            case 12:
                return "enabling";
            case 13:
                return "enabled";
            case 14:
                return "failed";
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

    public boolean isLinkDebouncing() {
        return this.mIsLinkDebouncing;
    }

    public WifiInfo syncRequestConnectionInfo() {
        return getWiFiInfoForUid(Binder.getCallingUid());
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

    public void setOperationalMode(int mode) {
        if (this.mVerboseLoggingEnabled) {
            log("setting operational mode to " + String.valueOf(mode));
        }
        sendMessage(CMD_SET_OPERATIONAL_MODE, mode, 0);
    }

    protected int getOperationalModeForTest() {
        return this.mOperationalMode;
    }

    public List<ScanResult> syncGetScanResultsList() {
        List<ScanResult> scanList;
        synchronized (this.mScanResultsLock) {
            scanList = new ArrayList();
            for (ScanDetail result : this.mScanResults) {
                scanList.add(new ScanResult(result.getScanResult()));
            }
            if (scanList.size() == 0) {
                Log.w(TAG, "Can't find ssid.");
            }
        }
        return scanList;
    }

    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        Bundle bundle = new Bundle();
        bundle.putLong("BSSID", bssid);
        bundle.putString(EXTRA_OSU_ICON_QUERY_FILENAME, fileName);
        Message resultMsg = channel.sendMessageSynchronously(CMD_QUERY_OSU_ICON, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        if (result == 1) {
            return true;
        }
        return false;
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
        return this.mScanResults;
    }

    public void disconnectCommand() {
        sendMessage(CMD_DISCONNECT);
    }

    public void disconnectCommand(int uid, int reason) {
        sendMessage(CMD_DISCONNECT, uid, reason);
    }

    public void reconnectCommand() {
        sendMessage(CMD_RECONNECT);
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
            Log.e(TAG, "An error has occurred, resultMsg is null");
            return null;
        }
        List<WifiConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        List<WifiConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public WifiConfiguration syncGetMatchingWifiConfig(ScanResult scanResult, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_MATCHING_CONFIG, scanResult);
        WifiConfiguration config = resultMsg.obj;
        resultMsg.recycle();
        return config;
    }

    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel, PasspointConfiguration config, int uid) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG, uid, 0, config);
        boolean result = resultMsg.arg1 == 1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemovePasspointConfig(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_PASSPOINT_CONFIG, fqdn);
        boolean result = resultMsg.arg1 == 1;
        resultMsg.recycle();
        return result;
    }

    public List<PasspointConfiguration> syncGetPasspointConfigs(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PASSPOINT_CONFIGS);
        List<PasspointConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public WifiConnectionStatistics syncGetConnectionStatistics(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONNECTION_STATISTICS);
        WifiConnectionStatistics result = resultMsg.obj;
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
        WifiLinkLayerStats result = resultMsg.obj;
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
        Message resultMsg = channel.sendMessageSynchronously(CMD_ENABLE_NETWORK, netId, disableOthers ? 1 : 0);
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

    public String syncGetCurrentNetworkWpsNfcConfigurationToken() {
        return this.mWifiNative.getCurrentNetworkWpsNfcConfigurationToken();
    }

    public void enableRssiPolling(boolean enabled) {
        int i;
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(CMD_ENABLE_RSSI_POLL, i, 0);
    }

    public void startFilteringMulticastPackets() {
        this.mIpManager.setMulticastFilter(true);
    }

    public void stopFilteringMulticastPackets() {
        this.mIpManager.setMulticastFilter(false);
    }

    public void setScreenOffMulticastFilter(boolean enabled) {
        this.mIpManager.setScreenOffMulticastFilter(enabled);
    }

    public void setHighPerfModeEnabled(boolean enable) {
        int i;
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(CMD_SET_HIGH_PERF_MODE, i, 0);
    }

    public synchronized void resetSimAuthNetworks(boolean simPresent) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simPresent ? 1 : 0);
    }

    public void notifyImsiAvailabe(boolean imsiAvailabe) {
        this.mIsImsiAvailable = imsiAvailabe;
    }

    public Network getCurrentNetwork() {
        if (this.mNetworkAgent != null) {
            return new Network(this.mNetworkAgent.netId);
        }
        return null;
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

    public boolean syncSaveConfig(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_SAVE_CONFIG);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
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
                } else if (this.mLastRunningWifiUids.diff(this.mRunningWifiUids)) {
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

    public void dumpIpManager(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mIpManager.dump(fd, pw, args);
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
        if (this.mCountryCode.getCountryCodeSentToDriver() != null) {
            pw.println("CountryCode sent to driver " + this.mCountryCode.getCountryCodeSentToDriver());
        } else if (this.mCountryCode.getCountryCode() != null) {
            pw.println("CountryCode: " + this.mCountryCode.getCountryCode() + " was not sent to driver");
        } else {
            pw.println("CountryCode was not initialized");
        }
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
        dumpIpManager(fd, pw, args);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.dump(fd, pw, args);
        } else {
            pw.println("mWifiConnectivityManager is not initialized");
        }
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

    private void logStateAndMessage(Message message, State state) {
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
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
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

    protected boolean recordLogRec(Message msg) {
        switch (msg.what) {
            case CMD_RSSI_POLL /*131155*/:
                return this.mVerboseLoggingEnabled;
            default:
                return true;
        }
    }

    protected String getLogRecString(Message msg) {
        StringBuilder sb = new StringBuilder();
        if (this.mScreenOn) {
            sb.append("!");
        }
        if (this.messageHandlingStatus != 0) {
            sb.append("(").append(this.messageHandlingStatus).append(")");
        }
        sb.append(smToString(msg));
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=").append(msg.sendingUid);
        }
        sb.append(" rt=").append(this.mClock.getUptimeSinceBootMillis());
        sb.append("/").append(this.mClock.getElapsedSinceBootMillis());
        WifiConfiguration config;
        String key;
        Long now;
        String report;
        switch (msg.what) {
            case CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    config = (WifiConfiguration) msg.obj;
                    sb.append(" ").append(config.configKey());
                    sb.append(" prio=").append(config.priority);
                    sb.append(" status=").append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ").append(config.BSSID);
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (!curConfig.configKey().equals(config.configKey())) {
                            sb.append(" current=").append(curConfig.configKey());
                            sb.append(" prio=").append(curConfig.priority);
                            sb.append(" status=").append(curConfig.status);
                            break;
                        }
                        sb.append(" is current");
                        break;
                    }
                }
                break;
            case CMD_ENABLE_NETWORK /*131126*/:
            case 151569:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=").append(key);
                }
                config = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config != null && (key == null || (config.configKey().equals(key) ^ 1) != 0)) {
                    sb.append(" target=").append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=").append(this.mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_START_SCAN /*131143*/:
                now = Long.valueOf(this.mClock.getWallClockMillis());
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ic=");
                sb.append(Integer.toString(sScanAlarmIntentCount));
                if (msg.obj != null) {
                    Long request = Long.valueOf(msg.obj.getLong(SCAN_REQUEST_TIME, 0));
                    if (request.longValue() != 0) {
                        sb.append(" proc(ms):").append(now.longValue() - request.longValue());
                    }
                }
                if (this.mIsScanOngoing) {
                    sb.append(" onGoing");
                }
                if (this.mIsFullScanOngoing) {
                    sb.append(" full");
                }
                sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                sb.append(" f=").append(this.mWifiInfo.getFrequency());
                sb.append(" sc=").append(this.mWifiInfo.score);
                sb.append(" link=").append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
                if (this.lastScanFreqs != null) {
                    sb.append(" list=");
                    for (Integer intValue : this.lastScanFreqs) {
                        sb.append(intValue.intValue()).append(",");
                    }
                }
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                    break;
                }
                break;
            case CMD_RSSI_POLL /*131155*/:
            case CMD_UNWANTED_NETWORK /*131216*/:
            case 151572:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (!(this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID() == null)) {
                    sb.append(" ").append(this.mWifiInfo.getSSID());
                }
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
                }
                sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                sb.append(" f=").append(this.mWifiInfo.getFrequency());
                sb.append(" sc=").append(this.mWifiInfo.score);
                sb.append(" link=").append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                }
                if (this.mWifiScoreReport.isLastReportValid()) {
                    sb.append(this.mWifiScoreReport.getLastReport());
                    break;
                }
                break;
            case CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.roamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.disconnectingWatchdogCount);
                break;
            case CMD_DISABLE_P2P_WATCHDOG_TIMER /*131184*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.mDisableP2pWatchdogCount);
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
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
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
                    sb.append(" BSSID=").append(StringUtil.safeDisplayBssid((String) msg.obj));
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" Target=").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mIsAutoRoaming));
                break;
            case CMD_START_CONNECT /*131215*/:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = this.mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mIsAutoRoaming));
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                        break;
                    }
                }
                break;
            case CMD_START_ROAM /*131217*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = msg.obj;
                if (result != null) {
                    now = Long.valueOf(this.mClock.getWallClockMillis());
                    sb.append(" bssid=").append(result.BSSID);
                    sb.append(" rssi=").append(result.level);
                    sb.append(" freq=").append(result.frequency);
                    if (result.seen <= 0 || result.seen >= now.longValue()) {
                        sb.append(" !seen=").append(result.seen);
                    } else {
                        sb.append(" seen=").append(now.longValue() - result.seen);
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mIsAutoRoaming));
                sb.append(" fail count=").append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_IP_REACHABILITY_LOST /*131221*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                    break;
                }
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
            case CMD_RSSI_THRESHOLD_BREACH /*131236*/:
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
                    }
                    sb.append("STATIC_OK");
                    break;
                }
                sb.append("DHCP_OK");
                break;
            case CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                sb.append(" ");
                if (msg.arg1 != 2) {
                    if (msg.arg1 != CMD_STATIC_IP_FAILURE) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    }
                    sb.append("STATIC_FAIL");
                    break;
                }
                sb.append("DHCP_FAIL");
                break;
            case CMD_INSTALL_PACKET_FILTER /*131274*/:
                sb.append(" len=").append(((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                sb.append(" enabled=").append(((Boolean) msg.obj).booleanValue());
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
                    NetworkInfo info = msg.obj;
                    NetworkInfo.State state = info.getState();
                    DetailedState detailedState = info.getDetailedState();
                    if (state != null) {
                        sb.append(" st=").append(state);
                    }
                    if (detailedState != null) {
                        sb.append("/").append(detailedState);
                        break;
                    }
                }
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ").append(this.mLastBssid);
                sb.append(" nid=").append(this.mLastNetworkId);
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                }
                key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=").append(key);
                    break;
                }
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                sb.append(" nid=").append(msg.arg1);
                sb.append(" reason=").append(msg.arg2);
                if (this.mLastBssid != null) {
                    sb.append(" lastbssid=").append(this.mLastBssid);
                }
                if (this.mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=").append(this.mWifiInfo.getFrequency());
                    sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                }
                if (isLinkDebouncing()) {
                    sb.append(" debounce");
                    break;
                }
                break;
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.mScanResults != null) {
                    sb.append(" found=");
                    sb.append(this.mScanResults.size());
                }
                sb.append(" known=").append(this.mNumScanResultsKnown);
                sb.append(" got=").append(this.mNumScanResultsReturned);
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                sb.append(String.format(" con=%d", new Object[]{Integer.valueOf(this.mConnectionReqCount)}));
                sb.append(String.format(" untrustedcn=%d", new Object[]{Integer.valueOf(this.mUntrustedReqCount)}));
                key = this.mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=").append(key);
                    break;
                }
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                StateChangeResult stateChangeResult = msg.obj;
                if (stateChangeResult != null) {
                    sb.append(stateChangeResult.toString());
                    break;
                }
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                sb.append(" ");
                sb.append(" timedOut=").append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(bssid);
                }
                sb.append(" blacklist=").append(Boolean.toString(this.didBlackListBSSID));
                break;
            case 151556:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = (WifiConfiguration) msg.obj;
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    sb.append(" nid=").append(config.networkId);
                    if (config.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (config.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(config.creatorUid);
                    sb.append(" suid=").append(config.lastUpdateUid);
                    sb.append(" ajst=").append(config.getNetworkSelectionStatus().getNetworkStatusString());
                    break;
                }
                break;
            case 151559:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = msg.obj;
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    sb.append(" nid=").append(config.networkId);
                    if (config.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (!(config.preSharedKey == null || (config.preSharedKey.equals(WifiConfigManager.PASSWORD_MASK) ^ 1) == 0)) {
                        sb.append(" hasPSK");
                    }
                    if (config.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(config.creatorUid);
                    sb.append(" suid=").append(config.lastUpdateUid);
                    break;
                }
                break;
            case 196611:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=").append(this.mWifiInfo.txSuccess);
                sb.append(",").append(this.mWifiInfo.txBad);
                sb.append(",").append(this.mWifiInfo.txRetries);
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

    private void handleScreenStateChanged(boolean screenOn) {
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
        if (this.mIsRunning) {
            getWifiLinkLayerStats();
            this.mOnTimeScreenStateChange = this.mOnTime;
            this.lastScreenStateChangeTimeStamp = this.lastLinkLayerStatsUpdate;
        }
        this.mWifiMetrics.setScreenState(screenOn);
        if (this.mWifiConnectivityManager != null) {
            long currenTime = this.mClock.getElapsedSinceBootMillis();
            if (screenOn && currenTime - this.mLastAllowSendHiLinkScanResultsBroadcastTime > 3000) {
                Log.d(TAG, "handleScreenStateChanged: allow send HiLink scan results broadcast.");
                this.mAllowSendHiLinkScanResultsBroadcast = true;
                this.mLastAllowSendHiLinkScanResultsBroadcastTime = currenTime;
                this.mSendHiLinkScanResultsBroadcastTries = 0;
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
                this.mWifiNative.setSuspendOptimizations(true);
                return;
            }
            return;
        }
        this.mSuspendOptNeedsDisabled |= reason;
        this.mWifiNative.setSuspendOptimizations(false);
    }

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

    private void setWifiState(int wifiState) {
        int previousWifiState = this.mWifiState.get();
        if (wifiState == 3) {
            try {
                broadcastWifiDriverChanged(1);
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                loge("Failed to note battery stats in wifi");
            }
        } else if (wifiState == 1) {
            broadcastWifiDriverChanged(2);
            this.mBatteryStats.noteWifiOff();
        }
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateWifiState(wifiState);
        }
        this.mWifiState.set(wifiState);
        log("setWifiState: " + syncGetWifiStateByName());
        if (checkSelfCureWifiResult()) {
            log("setWifiState, ignore to send intent due to wifi self curing.");
            return;
        }
        Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
        intent.addFlags(83886080);
        intent.putExtra("wifi_state", wifiState);
        intent.putExtra("previous_wifi_state", previousWifiState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setWifiApState(int wifiApState, int reason, String ifaceName, int mode) {
        int previousWifiApState = this.mWifiApState.get();
        if (wifiApState == 13) {
            try {
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                loge("Failed to note battery stats in wifi");
            }
        } else if (wifiApState == 11) {
            this.mBatteryStats.noteWifiOff();
        }
        this.mWifiApState.set(wifiApState);
        if (this.mVerboseLoggingEnabled) {
            log("setWifiApState: " + syncGetWifiApStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(83886080);
        intent.putExtra("wifi_state", wifiApState);
        intent.putExtra("previous_wifi_state", previousWifiApState);
        if (wifiApState == 14) {
            intent.putExtra("wifi_ap_error_code", reason);
        }
        if (ifaceName == null) {
            loge("Updating wifiApState with a null iface name");
        }
        intent.putExtra("wifi_ap_interface_name", ifaceName);
        intent.putExtra("wifi_ap_mode", mode);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void setmSettingsStore(WifiSettingsStore settingsStore) {
        this.mWifiSettingStore = settingsStore;
    }

    private void setScanResults() {
        this.mNumScanResultsKnown = 0;
        this.mNumScanResultsReturned = 0;
        ArrayList<ScanDetail> scanResults = this.mWifiNative.getScanResults();
        if (scanResults.isEmpty()) {
            this.mScanResults = new ArrayList();
            this.mEmptyScanResultCount++;
            if (this.mEmptyScanResultCount > 10 && this.mWiFiCHRManager != null) {
                this.mWiFiCHRManager.syncSetScanResultsList(this.mScanResults);
            }
            return;
        }
        this.mEmptyScanResultCount = 0;
        if (this.mLastBssid != null) {
            try {
                long activeBssid = Utils.parseMac(this.mLastBssid);
            } catch (IllegalArgumentException e) {
            }
        }
        synchronized (this.mScanResultsLock) {
            this.mScanResults = scanResults;
            this.mNumScanResultsReturned = this.mScanResults.size();
            updateScanDetailByWifiPro(this.mScanResults);
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.syncSetScanResultsList(this.mScanResults);
        }
        if (isLinkDebouncing()) {
            sendMessage(CMD_START_ROAM, this.mLastNetworkId, 1, null);
        }
    }

    public boolean attemptAutoConnect() {
        SupplicantState state = this.mWifiInfo.getSupplicantState();
        if (getCurrentState() != this.mRoamingState && getCurrentState() != this.mObtainingIpState && getCurrentState() != this.mScanModeState && getCurrentState() != this.mDisconnectingState && ((getCurrentState() != this.mConnectedState || (getEnableAutoJoinWhenAssociated() ^ 1) == 0) && !isLinkDebouncing() && state != SupplicantState.ASSOCIATING && state != SupplicantState.ASSOCIATED && state != SupplicantState.AUTHENTICATING && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.GROUP_HANDSHAKE)) {
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

    private void fetchRssiLinkSpeedAndFrequencyNative() {
        if (SupplicantState.ASSOCIATED.compareTo(this.mWifiInfo.getSupplicantState()) > 0 || SupplicantState.COMPLETED.compareTo(this.mWifiInfo.getSupplicantState()) < 0) {
            loge("error state to fetch rssi");
            return;
        }
        SignalPollResult pollResult = this.mWifiNative.signalPoll();
        if (pollResult != null) {
            Integer newRssi = Integer.valueOf(pollResult.currentRssi);
            Integer newLinkSpeed = Integer.valueOf(pollResult.txBitrate);
            Integer newFrequency = Integer.valueOf(pollResult.associationFrequency);
            this.mWifiInfo.setNoise(pollResult.currentNoise);
            this.mWifiInfo.setSnr(pollResult.currentSnr);
            this.mWifiInfo.setChload(pollResult.currentChload);
            if (this.mWiFiCHRManager != null) {
                this.mWiFiCHRManager.updateLinkSpeed(newLinkSpeed.intValue());
            }
            if (this.mWiFiCHRManager != null) {
                this.mWiFiCHRManager.updateChannel(newFrequency.intValue());
            }
            if (this.mVerboseLoggingEnabled) {
                logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi + " linkspeed=" + newLinkSpeed + " freq=" + newFrequency);
            }
            if (newRssi == null || newRssi.intValue() <= WifiMetrics.MIN_RSSI_DELTA || newRssi.intValue() >= 200) {
                this.mWifiInfo.setRssi(WifiMetrics.MIN_RSSI_DELTA);
                updateCapabilities(getCurrentWifiConfiguration());
            } else {
                if (newRssi.intValue() > 0) {
                    newRssi = Integer.valueOf(newRssi.intValue() - 256);
                }
                if (this.mWiFiCHRManager != null) {
                    this.mWiFiCHRManager.updateRSSI(newRssi.intValue());
                }
                this.mWifiInfo.setRssi(newRssi.intValue());
                if (this.mHwMssHandler != null) {
                    this.mHwMssHandler.mssSwitchCheck(newRssi.intValue());
                }
                if (isAllowedManualWifiPwrBoost() == 0) {
                    this.mWifiNative.pwrPercentBoostModeset(newRssi.intValue());
                }
                int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(newRssi.intValue());
                if (newSignalLevel != this.mLastSignalLevel) {
                    updateCapabilities(getCurrentWifiConfiguration());
                    sendRssiChangeBroadcast(newRssi.intValue());
                }
                this.mLastSignalLevel = newSignalLevel;
            }
            if (newLinkSpeed != null) {
                this.mWifiInfo.setLinkSpeed(newLinkSpeed.intValue());
            }
            if (newFrequency != null && newFrequency.intValue() > 0) {
                WifiConnectionStatistics wifiConnectionStatistics;
                if (ScanResult.is5GHz(newFrequency.intValue())) {
                    wifiConnectionStatistics = this.mWifiConnectionStatistics;
                    wifiConnectionStatistics.num5GhzConnected++;
                }
                if (ScanResult.is24GHz(newFrequency.intValue())) {
                    wifiConnectionStatistics = this.mWifiConnectionStatistics;
                    wifiConnectionStatistics.num24GhzConnected++;
                }
                this.mWifiInfo.setFrequency(newFrequency.intValue());
                sendStaFrequency(newFrequency.intValue());
            }
            this.mWifiConfigManager.updateScanDetailCacheFromWifiInfo(this.mWifiInfo);
            if (!(newRssi == null || newLinkSpeed == null || newFrequency == null)) {
                this.mWifiMetrics.handlePollResult(this.mWifiInfo);
            }
        }
    }

    private void cleanWifiScore() {
        this.mWifiInfo.txBadRate = 0.0d;
        this.mWifiInfo.txSuccessRate = 0.0d;
        this.mWifiInfo.txRetriesRate = 0.0d;
        this.mWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport.reset();
    }

    private void updateLinkProperties(LinkProperties newLp) {
        if (this.mVerboseLoggingEnabled) {
            log("Link configuration changed for netId: " + this.mLastNetworkId + " old: " + this.mLinkProperties + " new: " + newLp);
        }
        this.mLinkProperties = newLp;
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        if (getNetworkDetailedState() == DetailedState.CONNECTED) {
            sendLinkConfigurationChangedBroadcast();
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateLinkProperties nid: ").append(this.mLastNetworkId);
            sb.append(" state: ").append(getNetworkDetailedState());
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

    private void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        Intent intent = new Intent("android.net.wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendNetworkStateChangeBroadcast(String bssid) {
        Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("networkInfo", new NetworkInfo(this.mNetworkInfo));
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        if (bssid != null) {
            intent.putExtra("bssid", bssid);
        }
        if (this.mNetworkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK || this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            fetchRssiLinkSpeedAndFrequencyNative();
            WifiInfo sentWifiInfo = new WifiInfo(this.mWifiInfo);
            sentWifiInfo.setMacAddress("02:00:00:00:00:00");
            intent.putExtra("wifiInfo", sentWifiInfo);
        }
        checkSelfCureWifiResult();
        if (!ignoreNetworkStateChange(this.mNetworkInfo)) {
            StringBuilder detailLog = new StringBuilder();
            detailLog.append("NetworkStateChange ");
            detailLog.append(this.mNetworkInfo.getState()).append("/").append(this.mNetworkInfo.getDetailedState());
            Log.i(TAG, detailLog.toString());
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0066, code:
            if (r4.checkUidPermission("android.permission.LOCAL_MAC_ADDRESS", r9) == 0) goto L_0x0068;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private WifiInfo getWiFiInfoForUid(int uid) {
        WifiInfo result;
        if (isWifiSelfCuring()) {
            result = new WifiInfo(this.mWifiInfo);
            result.setNetworkId(getSelfCureNetworkId());
            if (result.getRssi() <= WifiMetrics.MIN_RSSI_DELTA) {
                result.setRssi(-70);
            }
            result.setSupplicantState(SupplicantState.COMPLETED);
            return result;
        } else if (Binder.getCallingUid() == Process.myUid()) {
            return this.mWifiInfo;
        } else {
            boolean apiLevel23App;
            result = new WifiInfo(this.mWifiInfo);
            result.setMacAddress("02:00:00:00:00:00");
            IPackageManager packageManager = IPackageManager.Stub.asInterface(this.mFacade.getService("package"));
            try {
                apiLevel23App = isMApp(this.mContext, getAppName(Binder.getCallingPid()));
            } catch (Exception e) {
                apiLevel23App = true;
            }
            if (apiLevel23App) {
                try {
                } catch (RemoteException e2) {
                    Log.e(TAG, "Error checking receiver permission", e2);
                }
            }
            result.setMacAddress(this.mWifiInfo.getMacAddress());
            return result;
        }
    }

    private String getAppName(int pID) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private static boolean isMApp(Context context, String pkgName) {
        boolean z = true;
        if (pkgName == null) {
            return true;
        }
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion < 23) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            return true;
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

    private boolean setNetworkDetailedState(DetailedState state) {
        boolean hidden = false;
        if (isLinkDebouncing() || this.mIsAutoRoaming) {
            hidden = true;
        }
        if (this.mVerboseLoggingEnabled) {
            log("setDetailed state, old =" + this.mNetworkInfo.getDetailedState() + " and new state=" + state + " hidden=" + hidden);
        }
        if (!(this.mNetworkInfo.getExtraInfo() == null || this.mWifiInfo.getSSID() == null || (this.mWifiInfo.getSSID().equals("<unknown ssid>") ^ 1) == 0 || this.mNetworkInfo.getExtraInfo().equals(this.mWifiInfo.getSSID()))) {
            if (this.mVerboseLoggingEnabled) {
                log("setDetailed state send new extra info" + this.mWifiInfo.getSSID());
            }
            this.mNetworkInfo.setExtraInfo(this.mWifiInfo.getSSID());
            sendNetworkStateChangeBroadcast(null);
        }
        if (hidden || state == this.mNetworkInfo.getDetailedState()) {
            return false;
        }
        this.mNetworkInfo.setDetailedState(state, null, this.mWifiInfo.getSSID());
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    private SupplicantState handleSupplicantStateChange(Message message) {
        StateChangeResult stateChangeResult = message.obj;
        SupplicantState state = stateChangeResult.state;
        this.mWifiInfo.setSupplicantState(state);
        if ((stateChangeResult.wifiSsid == null || stateChangeResult.wifiSsid.toString().isEmpty()) && isLinkDebouncing()) {
            return state;
        }
        if (SupplicantState.AUTHENTICATING == state || SupplicantState.ASSOCIATED == state) {
            fetchRssiLinkSpeedAndFrequencyNative();
        }
        this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
        this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            stateChangeResult.networkId = config.networkId;
            ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
            if (scanDetailCache != null) {
                ScanDetail scanDetail = scanDetailCache.getScanDetail(stateChangeResult.BSSID);
                if (scanDetail != null) {
                    NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                    if (networkDetail != null && networkDetail.getAnt() == Ant.ChargeablePublic) {
                        this.mWifiInfo.setMeteredHint(true);
                    }
                }
            }
            this.mWifiInfo.setEphemeral(config.ephemeral);
            if (!this.mWifiInfo.getMeteredHint()) {
                this.mWifiInfo.setMeteredHint(config.meteredHint);
            }
        }
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(lookupFrameworkNetworkId(stateChangeResult.networkId));
        } else {
            this.mWifiInfo.setNetworkId(-1);
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        return state;
    }

    protected void handleNetworkDisconnect() {
        if (DBG) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        stopRssiMonitoringOffload();
        clearTargetBssid("handleNetworkDisconnect");
        stopIpManager();
        this.mWifiScoreReport.reset();
        this.mWifiInfo.reset();
        this.mIsLinkDebouncing = false;
        this.mIsAutoRoaming = false;
        setNetworkDetailedState(DetailedState.DISCONNECTED);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent = null;
        }
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (TelephonyUtil.isSimConfig(config) && TelephonyUtil.getSimIdentity(getTelephonyManager(), config) == null) {
            Log.d(TAG, "remove EAP-SIM/AKA/AKA' network " + this.mLastNetworkId + " from wpa_supplicant since identity was absent.");
            this.mWifiNative.removeAllNetworks();
        }
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        this.mLastBssid = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
    }

    private void handleSupplicantConnectionLoss(boolean killSupplicant) {
        if (killSupplicant) {
            this.mWifiMonitor.stopAllMonitoring();
            if (!this.mWifiNative.disableSupplicant()) {
                loge("Failed to disable supplicant after connection loss");
            }
        }
        this.mWifiNative.closeSupplicantConnection();
        sendSupplicantConnectionChangedBroadcast(false);
        setWifiState(1);
    }

    void handlePreDhcpSetup() {
        this.mWifiNative.setBluetoothCoexistenceMode(1);
        setSuspendOptimizationsNative(1, false);
        this.mWifiNative.setPowerSave(false);
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

    void handlePostDhcpSetup() {
        setSuspendOptimizationsNative(1, true);
        this.mWifiNative.setPowerSave(true);
        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(2);
    }

    private void reportConnectionAttemptStart(WifiConfiguration config, String targetBSSID, int roamType) {
        this.mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        this.mDiagsConnectionStartMillis = this.mClock.getElapsedSinceBootMillis();
        this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 0);
        Long lDiagsConnectionStartMillis = Long.valueOf(this.mDiagsConnectionStartMillis);
        if (lDiagsConnectionStartMillis == null) {
            loge("reportConnectionAttemptStart : lDiagsConnectionStartMillis is null");
        }
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT, lDiagsConnectionStartMillis, 60000);
    }

    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode) {
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode);
        switch (level2FailureCode) {
            case 1:
                this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 1);
                break;
            case 5:
            case 8:
                break;
            default:
                this.mWifiDiagnostics.reportConnectionEvent(this.mDiagsConnectionStartMillis, (byte) 2);
                break;
        }
        this.mDiagsConnectionStartMillis = -1;
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (this.mVerboseLoggingEnabled) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            logd("link address " + dhcpResults.ipAddress);
        }
        synchronized (this.mDhcpResultsLock) {
            this.mDhcpResults = dhcpResults;
            addr = (Inet4Address) dhcpResults.ipAddress.getAddress();
        }
        if (this.mIsAutoRoaming && this.mWifiInfo.getIpAddress() != NetworkUtils.inetAddressToInt(addr)) {
            logd("handleIPv4Success, roaming and address changed" + this.mWifiInfo + " got: " + addr);
        }
        this.mWifiInfo.setInetAddress(addr);
        if (!this.mWifiInfo.getMeteredHint()) {
            this.mWifiInfo.setMeteredHint(!dhcpResults.hasMeteredHint() ? hasMeteredHintForWi(addr) : true);
            updateCapabilities(getCurrentWifiConfiguration());
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.handleIPv4SuccessException(dhcpResults);
        }
    }

    private void handleSuccessfulIpConfiguration() {
        this.mLastSignalLevel = -1;
        WifiConfiguration c = getCurrentWifiConfiguration();
        if (c != null) {
            c.getNetworkSelectionStatus().clearDisableReasonCounter(4);
            updateCapabilities(c);
        }
        if (c != null) {
            ScanResult result = getCurrentScanResult();
            if (result == null) {
                logd("WifiStateMachine: handleSuccessfulIpConfiguration and no scan results" + c.configKey());
            } else {
                result.numIpConfigFailures = 0;
            }
        }
    }

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

    private void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        if (DBG) {
            loge("handleIpConfigurationLost: SSID = " + this.mWifiInfo.getSSID() + ", BSSID = " + this.mWifiInfo.getBSSID());
        }
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        updateNetworkConnFailedInfo(this.mLastNetworkId, this.mWifiInfo.getRssi(), 4);
        this.mWifiNative.disconnect();
    }

    private void handleIpReachabilityLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect();
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0079 A:{SYNTHETIC, Splitter: B:31:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006a A:{SYNTHETIC, Splitter: B:24:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0082 A:{SYNTHETIC, Splitter: B:36:0x0082} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String macAddressFromRoute(String ipAddress) {
        Throwable th;
        String macAddress = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
            try {
                String readLine = reader2.readLine();
                while (true) {
                    readLine = reader2.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] tokens = readLine.split("[ ]+");
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
                    loge("Did not find remoteAddress {" + ipAddress + "} in " + "/proc/net/arp");
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e2) {
                reader = reader2;
                loge("Could not open /proc/net/arp to lookup mac address");
                if (reader != null) {
                }
                return macAddress;
            } catch (IOException e3) {
                reader = reader2;
                try {
                    loge("Could not read /proc/net/arp to lookup mac address");
                    if (reader != null) {
                    }
                    return macAddress;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            loge("Could not open /proc/net/arp to lookup mac address");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                }
            }
            return macAddress;
        } catch (IOException e7) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e8) {
                }
            }
            return macAddress;
        }
        return macAddress;
    }

    void maybeRegisterNetworkFactory() {
        if (this.mNetworkFactory == null) {
            checkAndSetConnectivityInstance();
            if (this.mCm != null) {
                this.mNetworkFactory = new WifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE, this.mNetworkCapabilitiesFilter);
                this.mNetworkFactory.setScoreFilter(99);
                this.mNetworkFactory.register();
                this.mUntrustedNetworkFactory = new UntrustedWifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE_UNTRUSTED, this.mNetworkCapabilitiesFilter);
                this.mUntrustedNetworkFactory.setScoreFilter(Integer.MAX_VALUE);
                this.mUntrustedNetworkFactory.register();
            }
        }
    }

    private void getAdditionalWifiServiceInterfaces() {
        if (this.mP2pSupported) {
            this.mWifiP2pServiceImpl = (WifiP2pServiceImpl) IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
            if (this.mWifiP2pServiceImpl != null) {
                this.mWifiP2pChannel = new AsyncChannel();
                this.mWifiP2pChannel.connect(this.mContext, getHandler(), this.mWifiP2pServiceImpl.getP2pStateMachineMessenger());
                this.mWifiRepeater = this.mWifiP2pServiceImpl.getWifiRepeater();
            }
        }
    }

    String smToString(Message message) {
        return smToString(message.what);
    }

    String smToString(int what) {
        String s = (String) sSmToString.get(what);
        if (s != null) {
            return s;
        }
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
            case 147457:
                s = "SUP_CONNECTION_EVENT";
                break;
            case 147458:
                s = "SUP_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                s = "NETWORK_CONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                s = "NETWORK_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                s = "SCAN_RESULTS_EVENT";
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                s = "SUPPLICANT_STATE_CHANGE_EVENT";
                break;
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                s = "AUTHENTICATION_FAILURE_EVENT";
                break;
            case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                s = "WPS_SUCCESS_EVENT";
                break;
            case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                s = "WPS_FAIL_EVENT";
                break;
            case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                s = "SUP_REQUEST_IDENTITY";
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                s = "SCAN_FAILED_EVENT";
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                s = "ASSOCIATION_REJECTION_EVENT";
                break;
            case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                s = "WifiMonitor.ANQP_DONE_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_START_EVENT /*147507*/:
                s = "WifiMonitor.GAS_QUERY_START_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_DONE_EVENT /*147508*/:
                s = "WifiMonitor.GAS_QUERY_DONE_EVENT";
                break;
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                s = "WifiMonitor.RX_HS20_ANQP_ICON_EVENT";
                break;
            case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
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
            case 151572:
                s = "RSSI_PKTCNT_FETCH";
                break;
            default:
                s = "what:" + Integer.toString(what);
                break;
        }
        return s;
    }

    void registerConnected() {
        if (this.mLastNetworkId != -1) {
            this.mWifiConfigManager.updateNetworkAfterConnect(this.mLastNetworkId);
            this.mWifiScoreReport.reset();
        }
    }

    void registerDisconnected() {
        if (this.mLastNetworkId != -1) {
            this.mWifiConfigManager.updateNetworkAfterDisconnect(this.mLastNetworkId);
            WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(this.mLastNetworkId);
            if (config == null) {
                return;
            }
            if (config.ephemeral || config.isPasspoint()) {
                this.mWifiConfigManager.removeNetwork(this.mLastNetworkId, 1010);
            }
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        if (this.mLastNetworkId == -1) {
            return null;
        }
        return this.mWifiConfigManager.getConfiguredNetwork(this.mLastNetworkId);
    }

    ScanResult getCurrentScanResult() {
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
        return scanDetailCache.get(BSSID);
    }

    String getCurrentBSSID() {
        if (isLinkDebouncing()) {
            return null;
        }
        return this.mLastBssid;
    }

    private ProxyInfo getProxyProperties() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        return config.getHttpProxy();
    }

    private void updateCapabilities(WifiConfiguration config) {
        NetworkCapabilities networkCapabilities = new NetworkCapabilities(this.mDfltNetworkCapabilities);
        if (config != null) {
            int rssi;
            if (config.ephemeral) {
                networkCapabilities.removeCapability(14);
            } else {
                networkCapabilities.addCapability(14);
            }
            if (this.mWifiInfo.getRssi() != WifiMetrics.MIN_RSSI_DELTA) {
                rssi = this.mWifiInfo.getRssi();
            } else {
                rssi = Integer.MIN_VALUE;
            }
            networkCapabilities.setSignalStrength(rssi);
        }
        if (this.mWifiInfo.getMeteredHint()) {
            networkCapabilities.removeCapability(11);
        }
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkCapabilities(networkCapabilities);
        }
    }

    private boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        if (networkId == -1) {
            return false;
        }
        WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
    }

    void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
    }

    void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
    }

    private String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        if (imsi == null || imsi.isEmpty()) {
            return "";
        }
        String prefix;
        String mcc;
        String mnc;
        if (eapMethod == 4) {
            prefix = "1";
        } else if (eapMethod == 5) {
            prefix = HwWifiCHRStateManager.TYPE_AP_VENDOR;
        } else if (eapMethod != 6) {
            return "";
        } else {
            prefix = "6";
        }
        if (mccMnc == null || (mccMnc.isEmpty() ^ 1) == 0) {
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = HwWifiCHRStateManager.TYPE_AP_VENDOR + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    boolean startScanForConfiguration(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
        if (scanDetailCache == null || (config.allowedKeyManagement.get(1) ^ 1) != 0 || scanDetailCache.size() > 6) {
            return true;
        }
        Set<Integer> freqs = this.mWifiConfigManager.fetchChannelSetForNetworkForPartialScan(config.networkId, ANQPData.DATA_LIFETIME_MILLISECONDS, this.mWifiInfo.getFrequency());
        if (freqs == null || freqs.size() == 0) {
            if (this.mVerboseLoggingEnabled) {
                logd("no channels for " + config.configKey());
            }
            return false;
        }
        logd("starting scan for " + config.configKey() + " with " + freqs);
        List<HiddenNetwork> hiddenNetworks = new ArrayList();
        if (config.hiddenSSID) {
            hiddenNetworks.add(new HiddenNetwork(TextUtils.isEmpty(config.oriSsid) ? config.SSID : config.oriSsid));
        }
        if (startScanNative(freqs, hiddenNetworks, WIFI_WORK_SOURCE)) {
            this.messageHandlingStatus = 1;
        } else {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_HANDLING_ERROR;
        }
        return true;
    }

    private void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            Log.wtf(TAG, "Current WifiConfiguration is null, but IP provisioning just succeeded");
        } else if (this.mWifiConfigManager.getLastSelectedNetwork() == config.networkId) {
            boolean prompt = this.mWifiPermissionsUtil.checkConfigOverridePermission(config.lastConnectUid);
            if (this.mVerboseLoggingEnabled) {
                log("Network selected by UID " + config.lastConnectUid + " prompt=" + prompt);
            }
            if (prompt) {
                if (this.mVerboseLoggingEnabled) {
                    log("explictlySelected acceptUnvalidated=" + config.noInternetAccessExpected);
                }
                this.mNetworkAgent.explicitlySelected(config.noInternetAccessExpected);
            }
        }
        setNetworkDetailedState(DetailedState.CONNECTED);
        this.mWifiConfigManager.updateNetworkAfterConnect(this.mLastNetworkId);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        if (this.mWifiStatStore != null) {
            this.mWifiStatStore.updateConnectState(true);
        }
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateConnectStateByConfig(getCurrentWifiConfiguration());
        }
    }

    public void triggerUpdateAPInfo() {
        Log.d(TAG, "triggerUpdateAPInfo");
    }

    /* JADX WARNING: Missing block: B:28:0x0056, code:
            if (r2 != r15.mHilinkLastHashCode) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:30:0x005c, code:
            if (r4 <= r15.mHilinkLastLevelCode) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:31:0x005e, code:
            android.util.Log.d(TAG, "Hilink sendHilinkscanResultBroadcast");
            r0 = new android.content.Intent(HILINK_STATE_CHANGE_ACTION);
            r0.putExtra("TYPE", "SCAN_RESULTS");
            r15.mContext.sendBroadcastAsUser(r0, android.os.UserHandle.ALL);
            r15.mHilinkLastHashCode = r2;
            r15.mAllowSendHiLinkScanResultsBroadcast = false;
     */
    /* JADX WARNING: Missing block: B:32:0x0083, code:
            r15.mHilinkLastLevelCode = r4;
            r9 = r15.mSendHiLinkScanResultsBroadcastTries + 1;
            r15.mSendHiLinkScanResultsBroadcastTries = r9;
     */
    /* JADX WARNING: Missing block: B:33:0x008d, code:
            if (r9 <= 10) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:34:0x008f, code:
            r15.mAllowSendHiLinkScanResultsBroadcast = false;
     */
    /* JADX WARNING: Missing block: B:35:0x0091, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendHilinkscanResultBroadcast() {
        long currentHilinkHashCode = 0;
        long currentLevelHilinkHashCode = 0;
        synchronized (this.mScanResultsLock) {
            if (this.mScanResults == null) {
                return;
            }
            for (ScanDetail scanDetail : this.mScanResults) {
                if (scanDetail != null) {
                    ScanResult item = scanDetail.getScanResult();
                    if (item != null && item.SSID.length() == 32 && item.SSID.startsWith("Hi")) {
                        int itemHashCode = item.SSID.hashCode();
                        if (itemHashCode < 0) {
                            itemHashCode = -itemHashCode;
                        }
                        currentHilinkHashCode += (long) itemHashCode;
                        if (item.level >= -45) {
                            currentLevelHilinkHashCode += (long) itemHashCode;
                        }
                    }
                }
            }
        }
    }

    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

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

    private void broadcastWifiCredentialChanged(int wifiCredentialEventType, WifiConfiguration config) {
        if (config != null && config.preSharedKey != null) {
            Intent intent = new Intent("android.net.wifi.WIFI_CREDENTIAL_CHANGED");
            intent.putExtra(SupplicantBackupMigration.SUPPLICANT_KEY_SSID, config.SSID);
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

    void handleGsmAuthRequest(SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == lookupFrameworkNetworkId(requestData.networkId)) {
            logd("id matches targetWifiConfiguration");
            String response = TelephonyUtil.getGsmSimAuthResponse(requestData.data, getTelephonyManager());
            if (response == null) {
                this.mWifiNative.simAuthFailedResponse(requestData.networkId);
            } else {
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(requestData.networkId, WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    void handle3GAuthRequest(SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == lookupFrameworkNetworkId(requestData.networkId)) {
            logd("id matches targetWifiConfiguration");
            SimAuthResponseData response = TelephonyUtil.get3GAuthResponse(requestData, getTelephonyManager());
            if (response != null) {
                this.mWifiNative.simAuthResponse(requestData.networkId, response.type, response.response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(requestData.networkId);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    public void startConnectToNetwork(int networkId, String bssid) {
        sendMessage(CMD_START_CONNECT, networkId, 0, bssid);
    }

    public void startConnectToUserSelectNetwork(int networkId, String bssid) {
        sendMessage(CMD_START_CONNECT, networkId, 1, bssid);
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_START_ROAM, networkId, 0, scanResult);
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        sendMessage(CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER, enabled ? 1 : 0);
    }

    static boolean unexpectedDisconnectedReason(int reason) {
        if (reason == 2 || reason == 6 || reason == 7 || reason == 8 || reason == 9 || reason == 14 || reason == 15 || reason == 16 || reason == 18 || reason == 19 || reason == 23 || reason == 34) {
            return true;
        }
        return false;
    }

    private boolean disassociatedReason(int reason) {
        if (reason == 2 || reason == 4 || reason == 5 || reason == 8 || reason == 34) {
            return true;
        }
        return false;
    }

    public void updateWifiMetrics() {
        this.mWifiMetrics.updateSavedNetworks(this.mWifiConfigManager.getSavedNetworks());
    }

    private boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
        boolean success = this.mWifiConfigManager.removeNetwork(message.arg1, message.sendingUid);
        if (!success) {
            loge("Failed to remove network");
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

    private static String getLinkPropertiesSummary(LinkProperties lp) {
        List<String> attributes = new ArrayList(6);
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

    private String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getConfiguredNetwork(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    private boolean p2pSendMessage(int what) {
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

    private boolean hasConnectionRequests() {
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

    void sendScanResultsAvailableBroadcast(boolean scanSucceeded) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", scanSucceeded);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }
}
