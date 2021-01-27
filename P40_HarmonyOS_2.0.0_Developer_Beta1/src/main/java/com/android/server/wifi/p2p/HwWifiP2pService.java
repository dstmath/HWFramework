package com.android.server.wifi.p2p;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Pair;
import android.util.wifi.HwHiLog;
import android.util.wifi.HwHiSlog;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwSoftApManager;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiService;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.MSS.HwMssUtils;
import com.android.server.wifi.SoftApChannelXmlParse;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNativeUtils;
import com.android.server.wifi.WifiRepeater;
import com.android.server.wifi.WifiRepeaterConfigStore;
import com.android.server.wifi.WifiRepeaterController;
import com.android.server.wifi.hwUtil.HwApConfigUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.pc.HwPCManagerEx;
import com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyInfo;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;

public class HwWifiP2pService extends WifiP2pServiceImpl {
    private static final String ACTION_DEVICE_DELAY_IDLE = "huawei.wifi.p2p.action.DEVICE_DELAY_IDLE";
    private static final String[] AP_NO_DHCP_WHITE_PACKAGE_NAME_LIST = new String[0];
    private static final int BAND_ERROR = -1;
    public static final int BASE = 143360;
    private static final String[] BLACKLIST_P2P_FIND = {"com.hp.android.printservice"};
    private static final String CARRY_DATA_MIRACAST = "1";
    private static final int CHANNEL_ERROR = -1;
    private static final int CHANNEL_INDEX_OF_DISCOVER = 16;
    public static final int CMD_BATTERY_CHANGED = 143469;
    public static final int CMD_DEVICE_DELAY_IDLE = 143465;
    public static final int CMD_LINKSPEED_POLL = 143470;
    public static final int CMD_REQUEST_REPEATER_CONFIG = 143463;
    public static final int CMD_RESPONSE_REPEATER_CONFIG = 143464;
    public static final int CMD_SCREEN_OFF = 143467;
    public static final int CMD_SCREEN_ON = 143466;
    public static final int CMD_SELFCURE_GO_CREATE_FAIL = 143471;
    private static final int CMD_SET_P2P_HIGH_PERF = 141;
    public static final int CMD_SET_REPEATER_CONFIG = 143461;
    public static final int CMD_SET_REPEATER_CONFIG_COMPLETED = 143462;
    private static final int CMD_TYPE_SET = 2;
    public static final int CMD_USER_PRESENT = 143468;
    private static final int CODE_DISABLE_P2P_GC_DHCP = 1006;
    private static final int CODE_GET_GROUP_CONFIG_INFO = 1005;
    private static final int CODE_GET_WIFI_REPEATER_CONFIG = 1001;
    private static final int CODE_MAGICLINK_APPLY_IP = 1009;
    private static final int CODE_NOTIFY_P2P_BINDER_ADD = 1124;
    private static final int CODE_NOTIFY_P2P_BINDER_REMOVE = 1125;
    private static final int CODE_REQUEST_DFS_STATUS = 1007;
    private static final int CODE_SET_WIFI_REPEATER_CONFIG = 1002;
    private static final int CODE_UPDATE_DFS_STATUS = 1008;
    private static final int CODE_WIFI_MAGICLINK_CONFIG_IP = 1003;
    private static final int CODE_WIFI_MAGICLINK_RELEASE_IP = 1004;
    private static final int[] COMMON_CHANNELS_2G = {1, 6, 11};
    private static final String COMM_IFACE = "wlan0";
    private static final int CONNECT_FAILURE = -1;
    private static final int CONNECT_SUCCESS = 0;
    private static final int DATA_TYPE_HOMEVISION_SINK_P2P_IE = 3;
    private static final int DATA_TYPE_P2P_BUSINESS = 1;
    private static final int DATA_TYPE_SET_LISTEN_MODE = 4;
    private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
    private static final long DEFAULT_IDLE_MS = 1800000;
    private static final long DEFAULT_LOW_DATA_TRAFFIC_LINE = 102400;
    private static final long DELAY_IDLE_MS = 60000;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    private static final String[] DISABLE_DHCP_WHITE_PACKAGE_NAME_LIST = new String[0];
    private static final int DISABLE_P2P_GC_DHCP_WAIT_TIME_MS = 10000;
    private static final String[] DISABLE_P2P_RANDOM_MAC_WHITE_PACKAGE_NAME_LIST = {"com.huawei.android.airsharing", "com.huawei.android.mirrorshare"};
    private static final String EXTRA_CLIENT_INFO = "macInfo";
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String FLASHLIGHT_RUNNING = "flashlight_running";
    private static final long INTERVAL_DISALLOW_P2P_FIND = 130000;
    private static final int INVALID_NET_ID = -1;
    private static final int IP_ADDR_LEN = 24;
    private static final int IP_ROUTE_INFO_NUM = 3;
    private static final boolean IS_DBG_ON = true;
    private static final boolean IS_HWDBG_ON = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean IS_HWLOGW_ON = true;
    private static final boolean IS_RELOAD = true;
    private static final boolean IS_TABLET_WINDOWS_CAST_ENABLED = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final boolean IS_TRY_REINVOCATION = true;
    private static final boolean IS_TV;
    private static final boolean IS_WINDOWS_CAST_ENABLED = SystemProperties.getBoolean("ro.config.hw_emui_cast_mode", false);
    private static final String KEY_OF_P2P_CONFIG = "p2pcfg";
    private static final String KEY_OF_P2P_CONFIG_TYPE = "p2pconfigtype";
    private static final String KEY_OF_P2P_DEV = "p2pdev";
    private static final String KEY_OF_P2P_DEV_ADDR = "p2pdevaddr";
    private static final String KEY_OF_P2P_ERRCODE = "p2perrcode";
    private static final String KEY_OF_P2P_EVENT = "p2pevent";
    private static final String KEY_OF_P2P_GROUP = "p2pgrp";
    private static final String KEY_OF_P2P_PACKAGE = "apppackagename";
    private static final String KEY_OF_P2P_STATUS = "p2pstatus";
    private static final int LEGACYGO_FLAG_INDEX = 4;
    private static final int LINKSPEED_ESTIMATE_TIMES = 4;
    private static final int LINKSPEED_INVALID_VALUE = -1;
    private static final int LINKSPEED_POLL_INTERVAL = 1000;
    private static final int LINKSPEED_TOTAL_WEIGHTS = 100;
    private static final int[] LINKSPEED_WEIGHTS = {15, 20, 30, 35};
    private static final int MAGICLINK_CONNECT_AP_DHCP = 1;
    private static final int MAGICLINK_CONNECT_AP_NODHCP = 2;
    private static final String MAGICLINK_CONNECT_GC_AP_MODE = "1";
    private static final String MAGICLINK_CONNECT_GC_GO_MODE = "0";
    private static final int MAGICLINK_CONNECT_GO_NODHCP = 0;
    private static final String MAGICLINK_CREATE_GROUP_160M_FLAG = "w";
    private static final String MAGICLINK_REPLACED_IP = "192.168.49.101";
    private static final String MAGICLINK_STATIC_IP = "192.168.49.2";
    private static final int MAX_P2P_CREATE_GO_FAIL_NUM = 2;
    private static final long MS_TO_S = 1000;
    private static final int NO_PEERS_GO_TIMEOUT = 600000;
    private static final String ONEHOP_LISTEN_MODE = "1";
    private static final int P2P_BAND_2G = 0;
    private static final int P2P_BAND_5G = 1;
    private static final int P2P_CHOOSE_CHANNEL_RANDOM = 0;
    private static final int P2P_DEVICE_OF_MIRACAST = 7;
    private static final String[] P2P_REUSE_WHITE_PACKAGE_NAME_LIST = {"com.huawei.nearby", "com.hisilicon.miracast", "com.huawei.waudio"};
    private static final String PATTERN_MAC = "^[a-f0-9]{2}+:[a-f0-9]{2}+:[a-f0-9]{2}+:[a-f0-9]{2}+:[a-f0-9]{2}+:[a-f0-9]{2}$";
    private static final String PERMISSION_DISABLE_P2P_GC_DHCP = "huawei.android.permission.WIFI_DISABLE_P2P_GC_DHCP";
    private static final String PERMISSION_DISABLE_P2P_RANDOM_MAC = "huawei.android.permission.WIFI_DISABLE_P2P_RANDOM_MAC";
    private static final String PERMISSION_P2P_LINK_REUSE = "com.huawei.permission.WIFI_P2P_LINK_REUSE";
    private static final String PERMISSION_SET_SINK_CONFIG = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final String[] REQUEST_DFS_STATUS_WHITE_PACKAGE_LIST = {"com.huawei.nearby", "com.huawei.android.instantshare"};
    private static final int RUNNING_TASK_NUM_MAX = 1;
    private static final int SEGMENT_LENGTH_MIN = 2;
    private static final String SERVER_ADDRESS_WIFI_BRIDGE = "192.168.43.1";
    private static final String SERVER_ADDRESS_WIFI_BRIDGE_OTHER = "192.168.50.1";
    private static final String[] SET_HWSINK_CONFIG_WHITE_PACKAGE_NAME_LIST = {"com.hisilicon.miracast"};
    private static final String SPLIT_DOT = ",";
    private static final String SPLIT_EQUAL = "=";
    private static final int SSID_POST_FIX_BYTES_MAX = 22;
    private static final int SSID_POST_FIX_BYTES_TV = 14;
    private static final int SYSTEM_UID_VALUE_MAX = 10000;
    private static final String TAG = "HwWifiP2pService";
    private static final int TIMEOUT_MASK_OF_DISCOVER = 255;
    private static final int UUID_APPEND_CUSTOM_NAME_BEGIN = 24;
    private static final int UUID_APPEND_CUSTOM_NAME_END = 28;
    private static final int WHITELIST_DURATION_MS = 15000;
    private static final int WIFI_DISABLE_P2P_GC_DHCP_FOREVER = 2;
    private static final int WIFI_DISABLE_P2P_GC_DHCP_NONE = 0;
    private static final int WIFI_DISABLE_P2P_GC_DHCP_ONCE = 1;
    private static final int WIFI_REPEATER_CLIENT_JOIN = 0;
    private static final String WIFI_REPEATER_CLIENT_JOIN_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_CLIENT_JOIN";
    private static final int WIFI_REPEATER_CLIENT_LEAVE = 1;
    private static final String WIFI_REPEATER_CLIENT_LEAVE_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_CLIENT_LEAVE";
    private static final int WIFI_REPEATER_MAX_CLIENT = 4;
    private static WifiNativeUtils wifiNativeUtils = EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class);
    private static WifiP2pServiceUtils wifiP2pServiceUtils = EasyInvokeFactory.getInvokeUtils(WifiP2pServiceUtils.class);
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.p2p.HwWifiP2pService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                HwHiSlog.d(HwWifiP2pService.TAG, false, "onReceive, action:%{public}s", new Object[]{action});
                if (HwWifiP2pService.ACTION_DEVICE_DELAY_IDLE.equals(action)) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage((int) HwWifiP2pService.CMD_DEVICE_DELAY_IDLE);
                }
            }
        }
    };
    private Message mCacheMsgToReSendWlan1Down = null;
    private AlarmManager.OnAlarmListener mClearGroupAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.p2p.HwWifiP2pService.AnonymousClass2 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HwHiSlog.i(HwWifiP2pService.TAG, false, "ClearGroupAlarmListener Alarm received", new Object[0]);
            if (HwWifiP2pService.wifiP2pServiceUtils != null && HwWifiP2pService.this.mP2pStateMachine != null) {
                WifiP2pGroup wifiP2pGroup = HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine);
                if (wifiP2pGroup == null || !wifiP2pGroup.isGroupOwner() || !wifiP2pGroup.isClientListEmpty()) {
                    HwHiSlog.i(HwWifiP2pService.TAG, false, "Alarm received client not empty, reset alarm", new Object[0]);
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mClearGroupAlarmListener);
                    HwWifiP2pService.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + HwQoEService.GAME_RTT_NOTIFY_INTERVAL, HwWifiP2pService.TAG, HwWifiP2pService.this.mClearGroupAlarmListener, null);
                    return;
                }
                HwHiSlog.i(HwWifiP2pService.TAG, false, "no peers 10 mins, receive clear group alarm, ready to remove group", new Object[0]);
                HwWifiP2pService.this.mP2pStateMachine.sendMessage(139280);
            }
        }
    };
    private String mConfigInfo;
    private Collection<WifiP2pDevice> mConnectedClientList = new ArrayList();
    private Context mContext;
    private String mCurDisbleDhcpPackageName = "";
    private final ConcurrentHashMap<IBinder, DeathHandlerData> mDeathDataByBinder = new ConcurrentHashMap<>();
    private PendingIntent mDefaultIdleIntent;
    private PendingIntent mDelayIdleIntent;
    private HashMap<String, Integer> mDisbleGcDhcpList = new HashMap<>();
    private long mDisbleP2pGcDhcpTime = -10000;
    private String mGcP2pIfName = "";
    private String mGoIpAddress = "";
    private HwDfsMonitor mHwDfsMonitor;
    private HwP2pStateMachine mHwP2pStateMachine;
    private HwWifiCHRService mHwWifiChrService;
    private String mInterface = "";
    private boolean mIsGoCreated = false;
    private boolean mIsLegacyGo = false;
    private boolean mIsMagicLinkDevice = false;
    private boolean mIsP2pConnected = false;
    private boolean mIsP2pHighPerfEnabled = false;
    private boolean mIsScreenOn = true;
    private boolean mIsUsingHwShare = false;
    private boolean mIsWifiRepeaterEnabled = false;
    private boolean mIsWifiRepeaterTetherStarted = false;
    private volatile int mLastLinkSpeed = 0;
    private long mLastRxBytes = 0;
    private long mLastTxBytes = 0;
    private int mLinkSpeedCounter = 0;
    private int mLinkSpeedPollToken = 0;
    private int[] mLinkSpeeds = new int[4];
    private final Object mLock = new Object();
    private int mMacFilterStaCount = 0;
    private String mMacFilterStr = "";
    private ConcurrentHashMap<String, String> mMagiclinkGcIpMap = new ConcurrentHashMap<>();
    private NetworkInfo mNetworkInfo = new NetworkInfo(1, 0, "WIFI", "");
    private int mP2pCreateGoFailTimes = 0;
    private List<P2pFindProcessInfo> mP2pFindProcessInfoList = null;
    private String mP2pInterface = "p2p0";
    private NetworkInfo mP2pNetworkInfo = new NetworkInfo(13, 0, "WIFI_P2P", "");
    private int mP2pReuseCnt = 0;
    private PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.p2p.HwWifiP2pService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HwHiSlog.d(HwWifiP2pService.TAG, false, "onReceive, action:%{public}s", new Object[]{action});
            if (action != null) {
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage((int) HwWifiP2pService.CMD_SCREEN_ON);
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage((int) HwWifiP2pService.CMD_USER_PRESENT);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage((int) HwWifiP2pService.CMD_SCREEN_OFF);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        HwWifiP2pService.this.mNetworkInfo = networkInfo;
                    }
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    HwWifiP2pService.this.handleP2pConnectionChanged(intent);
                } else if ("huawei.wifi.WIFI_MODE_STATE".equals(action)) {
                    WifiInjector wifiInjector = WifiInjector.getInstance();
                    if (wifiInjector != null) {
                        int wifiMode = wifiInjector.getClientModeImpl().getWifiMode();
                        if ((wifiMode & 8) != 0 && (wifiMode & 32) != 0) {
                            HwWifiP2pService.this.mP2pStateMachine.setWifiEnabledFlag(true);
                            HwWifiP2pService.this.mP2pStateMachine.sendMessage(143376);
                        }
                    }
                } else if ("android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
                    HwWifiP2pService.this.handlePeersChange(intent);
                } else if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action)) {
                    HwWifiP2pService.this.handleDeviceChange(intent);
                } else if ("huawei.net.slave_wifi.WIFI_STATE_CHANGED".equals(action)) {
                    HwWifiP2pService.this.handleWifi2StateChange(intent);
                } else {
                    HwHiSlog.d(HwWifiP2pService.TAG, false, "onReceive, other action", new Object[0]);
                }
            }
        }
    };
    private String mTetherInterfaceName;
    private List<Pair<String, Long>> mValidDeivceList = new ArrayList();
    private final AtomicInteger mWifi2State = new AtomicInteger(1);
    private WifiP2pChrHandler mWifiP2pChrHandle;
    private Handler mWifiP2pDataTrafficHandler;
    private WifiP2pMonitor mWifiP2pMonitor;
    private WifiRepeater mWifiRepeater;
    private long mWifiRepeaterBeginWorkTime = 0;
    private Collection<WifiP2pDevice> mWifiRepeaterClientList = new ArrayList();
    private AsyncChannel mWifiRepeaterConfigChannel;
    private WifiRepeaterConfigStore mWifiRepeaterConfigStore;
    private long mWifiRepeaterEndWorkTime = 0;
    private int mWifiRepeaterFreq = 0;
    private HandlerThread wifip2pThread = new HandlerThread("WifiP2pService");

    static /* synthetic */ int access$2306(HwWifiP2pService x0) {
        int i = x0.mP2pReuseCnt - 1;
        x0.mP2pReuseCnt = i;
        return i;
    }

    static /* synthetic */ int access$2308(HwWifiP2pService x0) {
        int i = x0.mP2pReuseCnt;
        x0.mP2pReuseCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$2604(HwWifiP2pService x0) {
        int i = x0.mLinkSpeedPollToken + 1;
        x0.mLinkSpeedPollToken = i;
        return i;
    }

    static /* synthetic */ int access$2708(HwWifiP2pService x0) {
        int i = x0.mLinkSpeedCounter;
        x0.mLinkSpeedCounter = i + 1;
        return i;
    }

    static {
        boolean z = true;
        if (!"tv".equals(SystemProperties.get("ro.build.characteristics", "default")) && !"mobiletv".equals(SystemProperties.get("ro.build.characteristics", "default"))) {
            z = false;
        }
        IS_TV = z;
    }

    public HwWifiP2pService(Context context, WifiInjector wifiInjector) {
        super(context);
        this.mContext = context;
        if (this.mP2pStateMachine instanceof HwP2pStateMachine) {
            this.mHwP2pStateMachine = this.mP2pStateMachine;
        }
        this.wifip2pThread.start();
        this.mWifiP2pDataTrafficHandler = new WifiP2pDataTrafficHandler(this.wifip2pThread.getLooper());
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDefaultIdleIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DEVICE_DELAY_IDLE, (Uri) null), 0);
        this.mDelayIdleIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DEVICE_DELAY_IDLE, (Uri) null), 0);
        registerForBroadcasts();
        this.mWifiRepeater = new WifiRepeaterController(this.mContext, getP2pStateMachineMessenger());
        this.mHwWifiChrService = HwWifiCHRServiceImpl.getInstance();
        this.mP2pFindProcessInfoList = new ArrayList();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mHwDfsMonitor = HwDfsMonitor.createHwDfsMonitor(this.mContext);
        this.mWifiP2pMonitor = WifiInjector.getInstance().getWifiP2pMonitor();
        this.mWifiP2pChrHandle = new WifiP2pChrHandler(this.mContext.getMainLooper());
        if (this.mWifiP2pChrHandle != null && this.mWifiP2pMonitor != null) {
            registerForWifiP2pMonitorEvents();
        }
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("huawei.wifi.WIFI_MODE_STATE");
        intentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        intentFilter.addAction("huawei.net.slave_wifi.WIFI_STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mContext.registerReceiver(this.mAlarmReceiver, new IntentFilter(ACTION_DEVICE_DELAY_IDLE));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetP2pReuseResources() {
        this.mIsP2pConnected = false;
        this.mP2pReuseCnt = 0;
        this.mGoIpAddress = "";
        this.mConnectedClientList.clear();
        this.mMagiclinkGcIpMap.clear();
    }

    private void resetMagiclinkIpPool(WifiP2pInfo wifiP2pInfo) {
        if (wifiP2pInfo != null && wifiP2pInfo.groupOwnerAddress != null) {
            String ip = wifiP2pInfo.groupOwnerAddress.getHostAddress();
            if (!TextUtils.isEmpty(ip) && !ip.equals(this.mGoIpAddress)) {
                this.mGoIpAddress = ip;
                HwMagiclinkIpManager.resetIpPool(this.mGoIpAddress);
                HwHiSlog.i(TAG, false, "resetMagiclinkIpPool ip pool", new Object[0]);
            }
        }
    }

    private void p2pNetworkInfoProc(NetworkInfo p2pNetworkInfo) {
        if (p2pNetworkInfo == null) {
            HwHiSlog.e(TAG, false, "p2pNetworkInfo is null", new Object[0]);
        } else if (!p2pNetworkInfo.isConnected()) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector != null && ((wifiInjector.getClientModeImpl().getWifiMode() & 8) == 0 || p2pNetworkInfo.getDetailedState() != this.mP2pNetworkInfo.getDetailedState())) {
                HwHiSlog.i(TAG, false, "P2P_CONNECTION_CHANGED, set wifi mode 0", new Object[0]);
                wifiInjector.getClientModeImpl().setWifiMode("android", 0);
            }
            setP2pHighPerf(false);
            resetP2pReuseResources();
            if (p2pNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                this.mGcP2pIfName = "";
            }
            Settings.Global.putInt(this.mContext.getContentResolver(), FLASHLIGHT_RUNNING, 0);
            HwHiSlog.i(TAG, false, "NetworkInfo changed disconnect.", new Object[0]);
        } else {
            if (this.mP2pReuseCnt <= 0) {
                this.mP2pReuseCnt = 1;
            }
            this.mIsP2pConnected = true;
            HwHiSlog.i(TAG, false, "NetworkInfo changed connect.", new Object[0]);
        }
    }

    private void connectedMapRefresh(WifiP2pGroup wifiP2pGroup) {
        if (wifiP2pGroup == null) {
            HwHiSlog.e(TAG, false, "wifiP2pGroup is null", new Object[0]);
            return;
        }
        Collection<WifiP2pDevice> curClientList = new ArrayList<>(wifiP2pGroup.getClientList());
        if (!this.mConnectedClientList.isEmpty() && curClientList.isEmpty()) {
            if (!IS_TV) {
                this.mP2pReuseCnt = 0;
            } else {
                this.mP2pReuseCnt = 1;
            }
            HwHiSlog.i(TAG, false, "Gc changes no, Reset reuse count.", new Object[0]);
        }
        this.mConnectedClientList.removeAll(curClientList);
        if (!this.mConnectedClientList.isEmpty()) {
            for (WifiP2pDevice item : this.mConnectedClientList) {
                String gcIp = this.mMagiclinkGcIpMap.get(item.deviceAddress);
                if (!TextUtils.isEmpty(gcIp)) {
                    this.mMagiclinkGcIpMap.remove(item.deviceAddress);
                    HwMagiclinkIpManager.releaseIp(gcIp);
                    HwHiSlog.i(TAG, false, "Release ip to magiclink ip pool.", new Object[0]);
                }
            }
        }
        this.mConnectedClientList = new ArrayList(wifiP2pGroup.getClientList());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectionChanged(Intent intent) {
        NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (p2pNetworkInfo != null) {
            this.mP2pNetworkInfo = p2pNetworkInfo;
            p2pNetworkInfoProc(p2pNetworkInfo);
        }
        WifiP2pGroup wifiP2pGroup = (WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo");
        if (wifiP2pGroup != null && wifiP2pGroup.isGroupOwner()) {
            connectedMapRefresh(wifiP2pGroup);
            WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra("wifiP2pInfo");
            if (wifiP2pInfo != null) {
                resetMagiclinkIpPool(wifiP2pInfo);
            }
        }
        if (p2pNetworkInfo != null && !p2pNetworkInfo.isConnected()) {
            if (wifiP2pGroup == null) {
                HwHiSlog.i(TAG, false, "clear binder map", new Object[0]);
                this.mDeathDataByBinder.clear();
            } else if (!wifiP2pGroup.isGroupOwner()) {
                HwHiSlog.i(TAG, false, "clear binder map", new Object[0]);
                this.mDeathDataByBinder.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDeviceChange(Intent intent) {
        WifiP2pServiceUtils wifiP2pServiceUtils2;
        if (!this.mIsScreenOn || intent == null || this.mIsWifiRepeaterEnabled) {
            HwHiSlog.e(TAG, false, "handleDeviceChange mIsWifiRepeaterEnabled is %{public}b", new Object[]{Boolean.valueOf(this.mIsWifiRepeaterEnabled)});
            return;
        }
        Object object = intent.getParcelableExtra("wifiP2pDevice");
        if (!(object instanceof WifiP2pDevice) || (wifiP2pServiceUtils2 = wifiP2pServiceUtils) == null) {
            HwHiSlog.e(TAG, false, "handleDeviceChange thisDevice == null", new Object[0]);
            return;
        }
        WifiP2pDevice thisDevice = (WifiP2pDevice) object;
        WifiP2pGroup wifiP2pGroup = wifiP2pServiceUtils2.getmGroup(this.mP2pStateMachine);
        if (wifiP2pGroup != null) {
            HwHiSlog.i(TAG, false, "handleDeviceChange thisDevice.status is %{public}d, isGroupOwner is %{public}b", new Object[]{Integer.valueOf(thisDevice.status), Boolean.valueOf(wifiP2pGroup.isGroupOwner())});
        }
        if (thisDevice.status != 0 || wifiP2pGroup == null || !wifiP2pGroup.isGroupOwner()) {
            this.mIsGoCreated = false;
            this.mAlarmManager.cancel(this.mClearGroupAlarmListener);
            return;
        }
        HwHiSlog.i(TAG, false, "handleDeviceChange set alarm", new Object[0]);
        this.mIsGoCreated = true;
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + HwQoEService.GAME_RTT_NOTIFY_INTERVAL, TAG, this.mClearGroupAlarmListener, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePeersChange(Intent intent) {
        if (this.mIsScreenOn && this.mIsGoCreated && intent != null) {
            WifiP2pServiceUtils wifiP2pServiceUtils2 = wifiP2pServiceUtils;
            if (wifiP2pServiceUtils2 == null) {
                HwHiSlog.e(TAG, false, "handlePeersChange wifiP2pServiceUtils == null", new Object[0]);
                return;
            }
            WifiP2pGroup wifiP2pGroup = wifiP2pServiceUtils2.getmGroup(this.mP2pStateMachine);
            if (wifiP2pGroup == null || wifiP2pGroup.isClientListEmpty()) {
                HwHiSlog.i(TAG, false, "handlePeersChange peers is empty set alarm", new Object[0]);
                this.mAlarmManager.cancel(this.mClearGroupAlarmListener);
                this.mAlarmManager.set(2, HwQoEService.GAME_RTT_NOTIFY_INTERVAL + SystemClock.elapsedRealtime(), TAG, this.mClearGroupAlarmListener, null);
                return;
            }
            HwHiSlog.i(TAG, false, "handlePeersChange peers is not empty cancel alarm", new Object[0]);
            this.mAlarmManager.cancel(this.mClearGroupAlarmListener);
        }
    }

    /* access modifiers changed from: private */
    public class WifiP2pChrHandler extends Handler {
        WifiP2pChrHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            HwWifiP2pService.this.uploadP2pChrInfo(message);
        }
    }

    private void registerForWifiP2pMonitorEvents() {
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147489, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147491, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147492, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147498, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147497, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147482, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147478, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147477, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147488, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147495, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147484, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147486, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147485, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147578, this.mWifiP2pChrHandle);
        this.mWifiP2pMonitor.registerHandler(this.mP2pInterface, 147579, this.mWifiP2pChrHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadP2pChrInfo(Message message) {
        if (message == null || message.obj == null) {
            HwHiSlog.e(TAG, false, "uploadP2pChrInfo Message is null", new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_OF_P2P_EVENT, message.what);
        switch (message.what) {
            case 139363:
                data.putString(KEY_OF_P2P_PACKAGE, this.mP2pStateMachine.getCallingPkgNameEx(message.sendingUid, message.replyTo));
                data.putInt(KEY_OF_P2P_CONFIG_TYPE, message.arg1);
                if (message.arg1 == 139271) {
                    data.putParcelable(KEY_OF_P2P_CONFIG, (WifiP2pConfig) message.obj);
                    break;
                }
                break;
            case 147477:
            case 147478:
            case 147498:
                data.putParcelable(KEY_OF_P2P_DEV, (WifiP2pDevice) message.obj);
                break;
            case 147482:
            case 147484:
            case 147488:
                data.putInt(KEY_OF_P2P_STATUS, ((WifiP2pServiceImpl.P2pStatus) message.obj).ordinal());
                break;
            case 147485:
            case 147486:
                data.putParcelable(KEY_OF_P2P_GROUP, (WifiP2pGroup) message.obj);
                break;
            case 147489:
            case 147491:
            case 147492:
                WifiP2pProvDiscEvent provDiscEvent = (WifiP2pProvDiscEvent) message.obj;
                if (provDiscEvent.device != null) {
                    data.putString(KEY_OF_P2P_DEV_ADDR, provDiscEvent.device.deviceAddress);
                    break;
                }
                break;
            case 147578:
                data.putString(KEY_OF_P2P_ERRCODE, (String) message.obj);
                break;
            case 147579:
                data.putString(KEY_OF_P2P_DEV_ADDR, (String) message.obj);
                break;
        }
        this.mHwWifiChrService.uploadDFTEvent(31, data);
    }

    public boolean isWifiRepeaterStarted() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldDisconnectWifiP2p() {
        if (!this.mIsWifiRepeaterEnabled) {
            return true;
        }
        HwHiSlog.i(TAG, false, "WifiRepeater is open.", new Object[0]);
        return false;
    }

    private class WifiP2pDataTrafficHandler extends Handler {
        private static final int MSG_UPDATA_DATA_TAFFIC = 0;

        WifiP2pDataTrafficHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwWifiP2pService.this.handleUpdataDateTraffic();
            }
        }
    }

    private boolean checkP2pDataTrafficLine() {
        WifiP2pGroup wifiP2pGroup = wifiP2pServiceUtils.getmGroup(this.mP2pStateMachine);
        if (wifiP2pGroup == null) {
            HwHiSlog.d(TAG, false, "WifiP2pGroup is null.", new Object[0]);
            return true;
        }
        this.mInterface = wifiP2pGroup.getInterface();
        HwHiSlog.d(TAG, false, "mInterface: %{public}s", new Object[]{this.mInterface});
        long txBytes = TrafficStats.getTxBytes(this.mInterface);
        long rxBytes = TrafficStats.getRxBytes(this.mInterface);
        long txSpeed = txBytes - this.mLastTxBytes;
        long rxSpeed = rxBytes - this.mLastRxBytes;
        HwHiSlog.d(TAG, false, " txBytes:%{public}s rxBytes:%{public}s txSpeed:%{public}s rxSpeed:%{public}s mLowDataTrafficLine:%{public}s DELAY_IDLE_MS:%{public}s", new Object[]{String.valueOf(txBytes), String.valueOf(rxBytes), String.valueOf(txSpeed), String.valueOf(rxSpeed), String.valueOf((long) DEFAULT_LOW_DATA_TRAFFIC_LINE), String.valueOf((long) DELAY_IDLE_MS)});
        if (this.mLastTxBytes == 0 && this.mLastRxBytes == 0) {
            this.mLastTxBytes = txBytes;
            this.mLastRxBytes = rxBytes;
            return false;
        }
        this.mLastTxBytes = txBytes;
        this.mLastRxBytes = rxBytes;
        return txSpeed + rxSpeed < DEFAULT_LOW_DATA_TRAFFIC_LINE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdataDateTraffic() {
        HwHiSlog.d(TAG, false, "handleUpdataDateTraffic", new Object[0]);
        if (!this.mP2pNetworkInfo.isConnected()) {
            HwHiSlog.d(TAG, false, "p2p is disconnected.", new Object[0]);
        } else if (shouldDisconnectP2p()) {
            HwHiSlog.w(TAG, false, "remove group, disconnect wifi p2p", new Object[0]);
            this.mP2pStateMachine.sendMessage(139280);
        } else {
            this.mAlarmManager.setExact(0, System.currentTimeMillis() + DELAY_IDLE_MS, this.mDelayIdleIntent);
        }
    }

    private boolean shouldDisconnectP2p() {
        if (checkP2pDataTrafficLine() && !isPcManagerRunning() && !isHuaweiShareOnGoing() && !isFlashlightRunning() && !isInstantOnlineRunning()) {
            return true;
        }
        return false;
    }

    private boolean isInstantOnlineRunning() {
        HwHiSlog.i(TAG, false, "hotspot = %{public}d", new Object[]{Integer.valueOf(Settings.Global.getInt(this.mContext.getContentResolver(), "hotspot", 0))});
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "hotspot", 0) == 1) {
            return true;
        }
        return false;
    }

    private boolean isFlashlightRunning() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), FLASHLIGHT_RUNNING, 0) == 1;
    }

    private boolean isPcManagerRunning() {
        if (IS_WINDOWS_CAST_ENABLED && HwPCManagerEx.isInWindowsCastMode()) {
            return true;
        }
        if (!IS_TABLET_WINDOWS_CAST_ENABLED || !HwPCManagerEx.isInSinkWindowsCastMode()) {
            return false;
        }
        return true;
    }

    private boolean isHuaweiShareOnGoing() {
        boolean isHuaweiShareSending = SystemPropertiesEx.getBoolean("instantshare.sending", false);
        boolean isHuaweiShareReceiving = SystemPropertiesEx.getBoolean("instantshare.receiving", false);
        HwHiSlog.i(TAG, false, "isHuaweiShareSending = %{public}s isHuaweiShareReceiving = %{public}s", new Object[]{String.valueOf(isHuaweiShareSending), String.valueOf(isHuaweiShareReceiving)});
        if (isHuaweiShareSending || isHuaweiShareReceiving) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public Object getHwP2pStateMachine(String name, Looper looper, boolean isP2pSupported) {
        return new HwP2pStateMachine(name, looper, isP2pSupported);
    }

    /* access modifiers changed from: protected */
    public boolean handleDefaultStateMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleDefaultStateMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleP2pNotSupportedStateMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleP2pNotSupportedStateMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleInactiveStateMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleInactiveStateMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleP2pEnabledStateExMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleP2pEnabledStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGroupNegotiationStateExMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleGroupNegotiationStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGroupCreatedStateExMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleGroupCreatedStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleOngoingGroupRemovalStateExMessage(Message message) {
        HwP2pStateMachine hwP2pStateMachine = this.mHwP2pStateMachine;
        if (hwP2pStateMachine != null) {
            return hwP2pStateMachine.handleOngoingGroupRemovalStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendGroupConfigInfo(WifiP2pGroup group) {
        this.mConfigInfo = group.getNetworkName() + "\n" + group.getOwner().deviceAddress + "\n" + group.getPassphrase() + "\n" + group.getFrequency();
        this.mContext.sendBroadcastAsUser(new Intent("android.net.wifi.p2p.CONFIG_INFO"), UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendInterfaceCreatedBroadcast(String ifName) {
        logd("sending interface created broadcast", new Object[0]);
        this.mGcP2pIfName = ifName;
        Intent intent = new Intent("android.net.wifi.p2p.INTERFACE_CREATED");
        intent.putExtra("p2pInterfaceName", ifName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkConnectedBroadcast(String bssid) {
        logd("sending network connected broadcast", new Object[0]);
        Intent intent = new Intent("android.net.wifi.p2p.NETWORK_CONNECTED_ACTION");
        intent.putExtra("bssid", bssid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkDisconnectedBroadcast(String bssid) {
        logd("sending network disconnected broadcast", new Object[0]);
        Intent intent = new Intent("android.net.wifi.p2p.NETWORK_DISCONNECTED_ACTION");
        intent.putExtra("bssid", bssid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLinkSpeedChangedBroadcast() {
        logd("sending linkspeed changed broadcast " + this.mLastLinkSpeed, new Object[0]);
        Intent intent = new Intent("com.huawei.net.wifi.p2p.LINK_SPEED");
        intent.putExtra("linkSpeed", this.mLastLinkSpeed);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wfd.permission.ACCESS_P2P_LINKSPEED");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendHwP2pDeviceExInfoBroadcast(byte[] info) {
        logd("sending HwP2pDeviceExInfo broadcast ", new Object[0]);
        Intent intent = new Intent("com.huawei.net.wifi.p2p.peers.hw.extend.info");
        intent.putExtra("exinfo", info);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wfd.permission.ACCESS_P2P_LINKSPEED");
    }

    /* access modifiers changed from: protected */
    public void handleTetheringDhcpRange(String[] tetheringDhcpRanges) {
        for (int i = tetheringDhcpRanges.length - 1; i >= 0; i--) {
            if (MAGICLINK_STATIC_IP.equals(tetheringDhcpRanges[i])) {
                tetheringDhcpRanges[i] = MAGICLINK_REPLACED_IP;
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleClientHwMessage(Message message) {
        int i = message.what;
        if (i != 139363) {
            switch (i) {
                case 141264:
                case 141265:
                case 141266:
                case 141267:
                case 141268:
                case 141269:
                case 141270:
                case 141271:
                case 141272:
                case 141273:
                case 141274:
                case 141275:
                    this.mP2pStateMachine.sendMessage(message);
                    return true;
                default:
                    HwHiSlog.d(TAG, false, "ClientHandler.handleMessage ignoring msg=%{public}s", new Object[]{message.toString()});
                    return false;
            }
        } else {
            uploadP2pChrInfo(message);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class HwP2pStateMachine extends WifiP2pServiceImpl.P2pStateMachine {
        private Message mCreatPskGroupMsg;

        HwP2pStateMachine(String name, Looper looper, boolean isP2pSupported) {
            super(HwWifiP2pService.this, name, looper, isP2pSupported);
        }

        private void handleAddP2pVaildDeviceMessage(Message message) {
            if (message.getData() == null) {
                HwHiSlog.d(HwWifiP2pService.TAG, false, "handleAddP2pVaildDeviceMessage message.getData is null", new Object[0]);
                return;
            }
            String addDeviceAddress = message.getData().getString("avlidDevice");
            HwWifiP2pService.this.logd("add p2p deivce valid addDeviceAddress = %{private}s", addDeviceAddress);
            addP2pValidDevice(addDeviceAddress);
        }

        private void handleRemoveP2pVaildDeviceMessage(Message message) {
            if (message.getData() == null) {
                HwHiSlog.d(HwWifiP2pService.TAG, false, "handleRemoveP2pVaildDeviceMessage message.getData is null", new Object[0]);
                return;
            }
            String removeDeviceAddress = message.getData().getString("avlidDevice");
            HwWifiP2pService.this.logd("remove p2p valid deivce removeDeviceAddress = %{private}s", removeDeviceAddress);
            removeP2pValidDevice(removeDeviceAddress);
        }

        private void handleScreenOffMessage() {
            HwWifiP2pService.this.mLastTxBytes = 0;
            HwWifiP2pService.this.mLastRxBytes = 0;
            if (!HwWifiP2pService.this.shouldDisconnectWifiP2p()) {
                return;
            }
            if (HwWifiP2pService.this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED && HwWifiP2pService.this.mP2pNetworkInfo.isConnected()) {
                WifiP2pGroup wifiP2pGroup = HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine);
                long delayTimeMs = HwWifiP2pService.DEFAULT_IDLE_MS;
                if (wifiP2pGroup != null && wifiP2pGroup.isGroupOwner() && wifiP2pGroup.getClientList().size() == 0) {
                    delayTimeMs = HwQoEService.GAME_RTT_NOTIFY_INTERVAL;
                }
                HwHiSlog.d(HwWifiP2pService.TAG, false, "set default idle timer: %{public}s ms", new Object[]{String.valueOf(delayTimeMs)});
                HwWifiP2pService.this.mAlarmManager.set(0, System.currentTimeMillis() + delayTimeMs, HwWifiP2pService.this.mDefaultIdleIntent);
            } else if (HwWifiP2pService.this.mP2pNetworkInfo.isConnected()) {
                HwHiSlog.d(HwWifiP2pService.TAG, false, "start to removeP2PGroup.", new Object[0]);
                HwWifiP2pService.this.handleUpdataDateTraffic();
            } else {
                HwHiSlog.d(HwWifiP2pService.TAG, false, "do not need to removeP2PGroup.", new Object[0]);
            }
        }

        private boolean handleMagicinkMessageInDefaultState(Message message) {
            switch (message.what) {
                case 141268:
                case 141270:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 2);
                    return true;
                case 141269:
                case 141274:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 2);
                    return true;
                case 141271:
                case 141275:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 2);
                    if (!HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        return true;
                    }
                    HwWifiP2pService.this.stopWifiRepeater(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine));
                    return true;
                case 141272:
                case 141273:
                default:
                    HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                    hwWifiP2pService.loge("Unhandled message " + message, new Object[0]);
                    return false;
            }
        }

        public boolean handleDefaultStateMessage(Message message) {
            switch (message.what) {
                case HwWifiStateMachine.CMD_STOP_WIFI_REPEATER /* 131577 */:
                    if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        sendMessage(139280);
                        break;
                    }
                    break;
                case 141264:
                    handleAddP2pVaildDeviceMessage(message);
                    break;
                case 141265:
                    handleRemoveP2pVaildDeviceMessage(message);
                    break;
                case 141266:
                    HwWifiP2pService.this.logd("clear p2p valid deivce", new Object[0]);
                    clearP2pValidDevice();
                    break;
                case HwWifiP2pService.CMD_DEVICE_DELAY_IDLE /* 143465 */:
                    HwWifiP2pService.this.mWifiP2pDataTrafficHandler.sendMessage(Message.obtain(HwWifiP2pService.this.mWifiP2pDataTrafficHandler, 0));
                    break;
                case HwWifiP2pService.CMD_SCREEN_ON /* 143466 */:
                    HwHiSlog.d(HwWifiP2pService.TAG, false, "cancel alarm.", new Object[0]);
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mDefaultIdleIntent);
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mDelayIdleIntent);
                    HwWifiP2pService.this.mIsScreenOn = true;
                    break;
                case HwWifiP2pService.CMD_SCREEN_OFF /* 143467 */:
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mClearGroupAlarmListener);
                    HwWifiP2pService.this.mIsScreenOn = false;
                    handleScreenOffMessage();
                    break;
                case 147459:
                    if (message.obj instanceof String) {
                        HwWifiP2pService.this.sendNetworkConnectedBroadcast((String) message.obj);
                        break;
                    }
                    break;
                case 147460:
                    if (message.obj instanceof String) {
                        HwWifiP2pService.this.sendNetworkDisconnectedBroadcast((String) message.obj);
                        break;
                    }
                    break;
                case 147558:
                    updatePersistentNetworks(true);
                    break;
                case 147577:
                    break;
                default:
                    return handleMagicinkMessageInDefaultState(message);
            }
            return true;
        }

        public boolean handleP2pEnabledStateExMessage(Message message) {
            int i = message.what;
            if (i == 143471) {
                selfcureP2pGoCreateFail();
                return true;
            } else if (i != 147577) {
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message, new Object[0]);
                return false;
            } else {
                HwWifiP2pService.this.sendHwP2pDeviceExInfoBroadcast((byte[]) message.obj);
                return true;
            }
        }

        public boolean handleOngoingGroupRemovalStateExMessage(Message message) {
            int i = message.what;
            if (i != 141268) {
                if (i == 141271 || i == 141275) {
                    replyToMessage(message, 139282);
                    return true;
                }
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message, new Object[0]);
                return false;
            } else if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                return true;
            } else {
                deferMessage(message);
                return true;
            }
        }

        private void removeP2pGroupNegotiationState(Message message, String p2pInterface) {
            if (TextUtils.isEmpty(p2pInterface)) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
                transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                return;
            }
            logd(getName() + " MAGICLINK_REMOVE_GC_GROUP,p2pInterface !=null,now remove it");
            if (this.mWifiNative.p2pGroupRemove(p2pInterface)) {
                replyToMessage(message, 139282);
                HwWifiP2pService.wifiP2pServiceUtils.sendP2pConnectionChangedBroadcast(HwWifiP2pService.this.mP2pStateMachine);
            } else {
                HwWifiP2pService.wifiP2pServiceUtils.handleGroupRemoved(HwWifiP2pService.this.mP2pStateMachine);
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
            }
            transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
        }

        private boolean isRemoveGroupAllowed(Message message) {
            if (HwWifiP2pServiceEx.getInstance() == null || HwWifiP2pServiceEx.getInstance().isRemoveGroupAllowed(message.sendingUid, getCallingPkgNameEx(message.sendingUid, message.replyTo))) {
                return true;
            }
            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
            return false;
        }

        private void updatePkgListBeforeCreatedState(Message message) {
            if (HwWifiP2pServiceEx.getInstance() != null) {
                HwWifiP2pServiceEx.getInstance().updateGroupCreatedPkgList(message.sendingUid, getCallingPkgNameEx(message.sendingUid, message.replyTo), true, false);
            }
        }

        public boolean handleGroupNegotiationStateExMessage(Message message) {
            switch (message.what) {
                case 141271:
                    logd(getName() + " MAGICLINK_REMOVE_GC_GROUP");
                    if (!isRemoveGroupAllowed(message)) {
                        return true;
                    }
                    HwWifiP2pService.this.mConfigInfo = "";
                    if (!(message.obj instanceof Bundle)) {
                        HwHiSlog.e(HwWifiP2pService.TAG, false, "MAGICLINK_REMOVE_GC_GROUP: message.obj is null", new Object[0]);
                        HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
                        transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                        return true;
                    }
                    String p2pInterface = ((Bundle) message.obj).getString("iface");
                    logd(getName() + "p2pInterface :" + p2pInterface);
                    removeP2pGroupNegotiationState(message, p2pInterface);
                    return true;
                case 141272:
                case 141273:
                default:
                    HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                    hwWifiP2pService.loge("Unhandled message " + message, new Object[0]);
                    return false;
                case 141274:
                    applyP2pReuseMsgProc(message);
                    return true;
                case 141275:
                    if (!isRemoveGroupAllowed(message)) {
                        return true;
                    }
                    if (HwWifiP2pService.access$2306(HwWifiP2pService.this) > 0) {
                        logd(getName() + "handleGroupCreatedStateExMessage p2p reuse count: " + HwWifiP2pService.this.mP2pReuseCnt);
                        replyToMessage(message, 139282);
                        return true;
                    } else if (TextUtils.isEmpty(HwWifiP2pService.this.mGcP2pIfName)) {
                        HwHiSlog.e(HwWifiP2pService.TAG, false, "mGcP2pIfName is null ", new Object[0]);
                        replyToMessage(message, 139282);
                        transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                        return true;
                    } else {
                        removeP2pGroupNegotiationState(message, HwWifiP2pService.this.mGcP2pIfName);
                        HwWifiP2pService.this.mGcP2pIfName = "";
                        return true;
                    }
            }
        }

        private void handleMagiclinkRemoveGcGroupMessage(Message message) {
            HwWifiP2pService.wifiP2pServiceUtils.enableBtCoex(HwWifiP2pService.this.mP2pStateMachine);
            HwWifiP2pService.this.mConfigInfo = "";
            if (this.mWifiNative.p2pGroupRemove(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine).getInterface())) {
                transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmOngoingGroupRemovalState(HwWifiP2pService.this.mP2pStateMachine));
                replyToMessage(message, 139282);
            } else {
                HwWifiP2pService.wifiP2pServiceUtils.handleGroupRemoved(HwWifiP2pService.this.mP2pStateMachine);
                transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
            }
            if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                HwWifiP2pService.this.stopWifiRepeater(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine));
            }
        }

        private void handleLinkSpeedPollMessage(Message message) {
            if (HwWifiP2pService.this.mLinkSpeedPollToken == message.arg1) {
                String ifname = HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine).getInterface();
                int linkSpeed = SystemProperties.getInt("wfd.config.linkspeed", 0);
                if (linkSpeed == 0) {
                    linkSpeed = this.mWifiNative.mHwWifiP2pNativeEx.getP2pLinkSpeed(ifname);
                }
                logd("ifname: " + ifname + ", get linkspeed from wpa: " + linkSpeed + ", mLinkSpeed " + linkSpeed);
                if (HwWifiP2pService.this.mLinkSpeedCounter < 4) {
                    HwWifiP2pService.this.mLinkSpeeds[HwWifiP2pService.access$2708(HwWifiP2pService.this)] = linkSpeed;
                }
                if (HwWifiP2pService.this.mLinkSpeedCounter >= 4) {
                    int avarageLinkSpeed = 0;
                    for (int i = 0; i < 4; i++) {
                        avarageLinkSpeed += HwWifiP2pService.this.mLinkSpeeds[i] * HwWifiP2pService.LINKSPEED_WEIGHTS[i];
                    }
                    int avarageLinkSpeed2 = avarageLinkSpeed / 100;
                    if (HwWifiP2pService.this.mLastLinkSpeed != avarageLinkSpeed2) {
                        HwWifiP2pService.this.mLastLinkSpeed = avarageLinkSpeed2;
                        HwWifiP2pService.this.sendLinkSpeedChangedBroadcast();
                    }
                    HwWifiP2pService.this.mLinkSpeedCounter = 0;
                }
                sendMessageDelayed(HwWifiP2pService.CMD_LINKSPEED_POLL, HwWifiP2pService.access$2604(HwWifiP2pService.this), HwWifiP2pService.MS_TO_S);
            }
        }

        public boolean handleGroupCreatedStateExMessage(Message message) {
            switch (message.what) {
                case 141271:
                    logd(getName() + " MAGICLINK_REMOVE_GC_GROUP");
                    if (isRemoveGroupAllowed(message)) {
                        handleMagiclinkRemoveGcGroupMessage(message);
                        break;
                    }
                    break;
                case 141274:
                    applyP2pReuseMsgProc(message);
                    break;
                case 141275:
                    if (isRemoveGroupAllowed(message)) {
                        if (HwWifiP2pService.access$2306(HwWifiP2pService.this) <= 0) {
                            handleMagiclinkRemoveGcGroupMessage(message);
                            break;
                        } else {
                            logd(getName() + "handleGroupCreatedStateExMessage p2p reuse count: " + HwWifiP2pService.this.mP2pReuseCnt);
                            replyToMessage(message, 139282);
                            break;
                        }
                    }
                    break;
                case 143374:
                    logd(" SET_MIRACAST_MODE: " + message.arg1);
                    if (message.arg1 == 1) {
                        HwWifiP2pService.this.mLastLinkSpeed = -1;
                        HwWifiP2pService.this.mLinkSpeedCounter = 0;
                        HwWifiP2pService.this.mLinkSpeedPollToken = 0;
                        sendMessage(HwWifiP2pService.CMD_LINKSPEED_POLL, HwWifiP2pService.this.mLinkSpeedPollToken);
                    }
                    return false;
                case HwWifiP2pService.CMD_LINKSPEED_POLL /* 143470 */:
                    handleLinkSpeedPollMessage(message);
                    break;
                default:
                    HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                    hwWifiP2pService.loge("Unhandled message when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2, new Object[0]);
                    return false;
            }
            return true;
        }

        public boolean handleP2pNotSupportedStateMessage(Message message) {
            if (message.what != 141268) {
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message, new Object[0]);
                return false;
            }
            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 1);
            return true;
        }

        private void handleBeamConnectMessage(Message message) {
            HwWifiP2pService.this.removeDisableP2pGcDhcp(true);
            if (!(message.obj instanceof WifiP2pConfig) || !isAllowP2pCnnect(message)) {
                HwHiSlog.e(HwWifiP2pService.TAG, false, "handleBeamConnectMessage: Illegal argument(s)", new Object[0]);
                replyToMessage(message, 139272);
                return;
            }
            WifiP2pConfig beamConfig = (WifiP2pConfig) message.obj;
            HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, false);
            HwWifiP2pService.this.updateGroupCapability(this.mPeers, beamConfig.deviceAddress, this.mWifiNative.getGroupCapability(beamConfig.deviceAddress));
            if (beamConnect(beamConfig, true) == -1) {
                replyToMessage(message, 139272);
                return;
            }
            updatePkgListBeforeCreatedState(message);
            HwWifiP2pService.this.updateStatus(this.mPeers, this.mSavedPeerConfig.deviceAddress, 1);
            sendPeersChangedBroadcast();
            replyToMessage(message, 139273);
            transitionTo(this.mGroupNegotiationState);
        }

        private void handleCreateGroupPskMessage(Message message) {
            if (isAllowP2pCnnect(message)) {
                if (HwWifiP2pService.this.mWifiRepeater.isEncryptionTypeTetheringAllowed()) {
                    HwWifiP2pService.this.setWifiRepeaterState(3);
                    HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, true);
                    if (HwWifiP2pService.this.mWifiRepeaterConfigChannel != null) {
                        this.mCreatPskGroupMsg = message;
                        if (message.obj == null) {
                            HwWifiP2pService.this.mWifiRepeaterConfigChannel.sendMessage((int) HwWifiP2pService.CMD_REQUEST_REPEATER_CONFIG);
                        } else if (!(message.obj instanceof WifiConfiguration)) {
                            logd("handleCreateGroupPskMessage: Illegal argument(s)");
                            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                            HwWifiP2pService.this.setWifiRepeaterState(5);
                        } else {
                            WifiConfiguration userconfig = (WifiConfiguration) message.obj;
                            HwWifiP2pService.this.mWifiRepeaterConfigChannel.sendMessage((int) HwWifiP2pService.CMD_SET_REPEATER_CONFIG, userconfig);
                            creatGroupForRepeater(userconfig);
                        }
                    }
                } else {
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                    HwWifiP2pService.this.setWifiRepeaterState(5);
                }
            }
        }

        private void handleRespRepeaterConfigMessage(Message message) {
            if (!(message.obj instanceof WifiConfiguration)) {
                loge("CMD_RESPONSE_REPEATER_CONFIG: message invalid");
                return;
            }
            WifiConfiguration config = (WifiConfiguration) message.obj;
            if (config != null) {
                creatGroupForRepeater(config);
            } else {
                HwWifiP2pService.this.loge("wifi repeater config is null!", new Object[0]);
            }
        }

        private void handleMagiclinkConnectMessage(Message message) {
            if (!(message.obj instanceof Bundle) || !isAllowP2pCnnect(message)) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 0);
                return;
            }
            HwWifiP2pService.this.setCurDisbleDhcpPackageName(message.sendingUid);
            if (!sendMagiclinkConnectCommand(((Bundle) message.obj).getString("cfg"), message.sendingUid)) {
                HwHiSlog.e(HwWifiP2pService.TAG, false, "MAGICLINK_CONNECT fail", new Object[0]);
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 0);
                return;
            }
            updatePkgListBeforeCreatedState(message);
        }

        private void handleMagiclinkCreateGroupMessage(Message message) {
            boolean isSuccss;
            if (!(message.obj instanceof Bundle) || !isAllowP2pCnnect(message)) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 0);
                return;
            }
            resetP2pChannelSet();
            HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, true);
            int netId = message.arg1;
            String freq = checkDfsWhenMagiclinkCreateGroup(((Bundle) message.obj).getString("freq"));
            HwHiSlog.i(HwWifiP2pService.TAG, false, "MAGICLINK_CREATE_GROUP freq=%{public}s", new Object[]{freq});
            if (netId == -2) {
                int netId2 = this.mGroups.getNetworkId(HwWifiP2pService.wifiP2pServiceUtils.getmThisDevice(HwWifiP2pService.this).deviceAddress);
                if (netId2 != -1) {
                    isSuccss = this.mWifiNative.mHwWifiP2pNativeEx.magiclinkGroupAdd(netId2, freq);
                } else {
                    isSuccss = this.mWifiNative.mHwWifiP2pNativeEx.magiclinkGroupAdd(true, freq);
                }
            } else {
                isSuccss = this.mWifiNative.mHwWifiP2pNativeEx.magiclinkGroupAdd(false, freq);
            }
            if (isSuccss) {
                replyToMessage(message, 139279);
                updatePkgListBeforeCreatedState(message);
                transitionTo(this.mGroupNegotiationState);
            } else {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 0);
            }
            HwWifiP2pService.this.updateP2pGoCreateStatus(isSuccss);
        }

        private void handleSetSinkConfigMessage(Message message) {
            if (!HwWifiP2pService.this.isAbleToSetSinkConfig(message.sendingUid)) {
                return;
            }
            if (!(message.obj instanceof Bundle)) {
                loge("SET_HWSINKCONFIG fail, message invalid");
                return;
            }
            String sinkConfig = ((Bundle) message.obj).getString("sinkConfig", "");
            logd("HwWifiP2pService: setHwSinkConfig");
            this.mWifiNative.mHwWifiP2pNativeEx.deliverP2pData(2, 3, sinkConfig);
        }

        public boolean handleInactiveStateMessage(Message message) {
            switch (message.what) {
                case 141267:
                    handleBeamConnectMessage(message);
                    break;
                case 141268:
                    handleCreateGroupPskMessage(message);
                    break;
                case 141269:
                    handleMagiclinkConnectMessage(message);
                    HwWifiP2pService.this.uploadP2pChrInfo(message);
                    break;
                case 141270:
                    handleMagiclinkCreateGroupMessage(message);
                    HwWifiP2pService.this.uploadP2pChrInfo(message);
                    break;
                case 141272:
                    HwHiSlog.i(HwWifiP2pService.TAG, false, "receive disable p2p random mac message", new Object[0]);
                    if (HwWifiP2pService.this.ableToDisableP2pRandomMac(message.sendingUid)) {
                        HwHiSlog.i(HwWifiP2pService.TAG, false, "deliverP2pData to disable p2p random mac", new Object[0]);
                        this.mWifiNative.mHwWifiP2pNativeEx.deliverP2pData(2, 1, "1");
                        break;
                    }
                    break;
                case 141273:
                    handleSetSinkConfigMessage(message);
                    break;
                case HwWifiP2pService.CMD_RESPONSE_REPEATER_CONFIG /* 143464 */:
                    handleRespRepeaterConfigMessage(message);
                    break;
                case 147557:
                    if (message.obj instanceof String) {
                        HwWifiP2pService.this.sendInterfaceCreatedBroadcast((String) message.obj);
                    }
                    HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                    hwWifiP2pService.mIsMagicLinkDevice = !hwWifiP2pService.mIsLegacyGo;
                    transitionTo(this.mGroupNegotiationState);
                    break;
                default:
                    HwWifiP2pService hwWifiP2pService2 = HwWifiP2pService.this;
                    hwWifiP2pService2.loge("Unhandled message when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2, new Object[0]);
                    return false;
            }
            return true;
        }

        private boolean isInP2pReuseWhiteList(String packageName) {
            for (String whitePackageName : HwWifiP2pService.P2P_REUSE_WHITE_PACKAGE_NAME_LIST) {
                if (whitePackageName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasP2pLinkReusePermission(int uid) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector == null) {
                return false;
            }
            WifiPermissionsWrapper wifiPermissionsWrapper = wifiInjector.getWifiPermissionsWrapper();
            if (wifiPermissionsWrapper == null) {
                HwHiSlog.i(HwWifiP2pService.TAG, false, "wifiPermissionsWrapper is null when check p2p reuse permission", new Object[0]);
                return false;
            } else if (wifiPermissionsWrapper.getUidPermission(HwWifiP2pService.PERMISSION_P2P_LINK_REUSE, uid) != -1) {
                return true;
            } else {
                return false;
            }
        }

        private void applyP2pReuseMsgProc(Message message) {
            if (!(message.obj instanceof Bundle)) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 0);
                return;
            }
            String packageName = ((Bundle) message.obj).getString(AvSyncLatencyInfo.TAG_PKG_NAME);
            if (!hasP2pLinkReusePermission(message.sendingUid) || !isInP2pReuseWhiteList(packageName)) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 0);
                HwHiSlog.e(HwWifiP2pService.TAG, false, "packageName=%{public}s do not have permission or not in white list", new Object[]{packageName});
            } else if (HwWifiP2pService.this.mP2pReuseCnt < 0 || !HwWifiP2pService.this.mIsP2pConnected) {
                HwWifiP2pService.this.mP2pReuseCnt = 0;
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 0);
                HwHiSlog.e(HwWifiP2pService.TAG, false, "applyP2pReuseMsgProc mP2pReuseCnt err: " + HwWifiP2pService.this.mP2pReuseCnt, new Object[0]);
            } else {
                HwWifiP2pService.access$2308(HwWifiP2pService.this);
                if (HwWifiP2pServiceEx.getInstance() != null) {
                    HwWifiP2pServiceEx.getInstance().updateGroupCreatedPkgList(message.sendingUid, packageName, true, true);
                }
                HwHiLog.i(HwWifiP2pService.TAG, false, "applyP2pReuseMsgProc packageName=%{public}s, reuse count%{public}d", new Object[]{packageName, Integer.valueOf(HwWifiP2pService.this.mP2pReuseCnt)});
                replyToMessage(message, 139273);
            }
        }

        private boolean hasDisableDhcpPermission(int uid) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector == null) {
                return false;
            }
            WifiPermissionsWrapper wifiPermissionsWrapper = wifiInjector.getWifiPermissionsWrapper();
            if (wifiPermissionsWrapper == null) {
                HwHiSlog.i(HwWifiP2pService.TAG, false, "wifiPermissionsWrapper is null when check disable dhcp permission", new Object[0]);
                return false;
            } else if (wifiPermissionsWrapper.getUidPermission(HwWifiP2pService.PERMISSION_DISABLE_P2P_GC_DHCP, uid) != -1) {
                return true;
            } else {
                return false;
            }
        }

        private boolean isInApNoDhcpWhiteList(int uid) {
            if (uid == 1000) {
                return true;
            }
            String packageName = HwWifiP2pService.this.mContext.getPackageManager().getNameForUid(uid);
            for (String whitePackageName : HwWifiP2pService.AP_NO_DHCP_WHITE_PACKAGE_NAME_LIST) {
                if (whitePackageName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        private String convertMagiclinkConnectMode(String connectMode, int uid) {
            if (TextUtils.isEmpty(connectMode)) {
                return "";
            }
            try {
                int mode = Integer.parseInt(connectMode);
                if (mode == 0) {
                    HwWifiP2pService.this.mIsLegacyGo = false;
                    return HwWifiP2pService.MAGICLINK_CONNECT_GC_GO_MODE;
                } else if (mode == 1) {
                    HwWifiP2pService.this.mIsLegacyGo = true;
                    return "1";
                } else if (mode != 2) {
                    HwWifiP2pService.this.mIsLegacyGo = false;
                    return connectMode;
                } else if (!hasDisableDhcpPermission(uid) || !isInApNoDhcpWhiteList(uid)) {
                    HwHiSlog.e(HwWifiP2pService.TAG, false, "uid %{public}d do not have permission or not in white list", new Object[]{Integer.valueOf(uid)});
                    return "";
                } else {
                    HwWifiP2pService.this.mIsLegacyGo = false;
                    return "1";
                }
            } catch (NumberFormatException e) {
                HwHiSlog.e(HwWifiP2pService.TAG, false, "connectMode parseInt fail", new Object[0]);
                return "";
            }
        }

        private boolean sendMagiclinkConnectCommand(String info, int uid) {
            if (TextUtils.isEmpty(info)) {
                return false;
            }
            String[] tokens = info.split("\n");
            if (tokens.length < 4) {
                return false;
            }
            StringBuffer buf = new StringBuffer();
            buf.append("P\"" + tokens[0] + "\"\n" + tokens[1] + "\n\"" + tokens[2] + "\"\n" + tokens[3]);
            for (int i = 4; i < tokens.length; i++) {
                if (i == 4) {
                    HwHiSlog.i(HwWifiP2pService.TAG, false, "LegacyGO flag = %{public}s", new Object[]{tokens[i]});
                    String p2pMode = convertMagiclinkConnectMode(tokens[i], uid);
                    if (TextUtils.isEmpty(p2pMode)) {
                        return false;
                    }
                    buf.append("\n" + p2pMode);
                } else {
                    buf.append("\n" + tokens[i]);
                }
            }
            resetP2pChannelSet();
            this.mWifiNative.mHwWifiP2pNativeEx.magiclinkConnect(buf.toString());
            return true;
        }

        private void creatGroupForRepeater(WifiConfiguration config) {
            if (config == null) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                HwWifiP2pService.this.setWifiRepeaterState(5);
                HwWifiP2pService.this.loge("wifirpt: config is null", new Object[0]);
                return;
            }
            HwWifiP2pService.this.mIsWifiRepeaterEnabled = true;
            config.apChannel = HwWifiP2pService.this.mWifiRepeater.retrieveDownstreamChannel();
            config.apBand = HwWifiP2pService.this.mWifiRepeater.retrieveDownstreamBand();
            if (config.apChannel == -1 || config.apBand == -1) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                HwWifiP2pService.this.setWifiRepeaterState(5);
                HwWifiP2pService.this.mIsWifiRepeaterEnabled = false;
                return;
            }
            if (HwWifiP2pService.this.isWifiConnected()) {
                StringBuilder sb = new StringBuilder("WifiRepeater=y");
                sb.append("\nssid=");
                sb.append(config.SSID);
                sb.append("\npsk=");
                sb.append(new SensitiveObj(config.preSharedKey));
                sb.append("\nchannel=");
                sb.append(config.apChannel);
                sb.append("\nband=");
                StringBuilder repeaterConf = sb.append(config.apBand);
                resetP2pChannelSet();
                boolean isSuccesss = this.mWifiNative.mHwWifiP2pNativeEx.addP2pRptGroup(repeaterConf.toString());
                if (!isSuccesss) {
                    HwWifiP2pService.this.mIsWifiRepeaterEnabled = false;
                }
                HwWifiP2pService.this.updateP2pGoCreateStatus(isSuccesss);
            } else {
                HwWifiP2pService.this.mIsWifiRepeaterEnabled = false;
                HwHiSlog.e(HwWifiP2pService.TAG, false, "wifirpt: isWifiConnected = false", new Object[0]);
            }
            if (HwWifiP2pService.this.mIsWifiRepeaterEnabled) {
                Settings.Global.putInt(HwWifiP2pService.this.mContext.getContentResolver(), "wifi_repeater_on", 6);
                replyToMessage(this.mCreatPskGroupMsg, 139279);
                updatePkgListBeforeCreatedState(this.mCreatPskGroupMsg);
                transitionTo(this.mGroupNegotiationState);
                HwHiSlog.d(HwWifiP2pService.TAG, false, "wifirpt: CREATE_GROUP_PSK SUCCEEDED, now transitionTo GroupNegotiationState", new Object[0]);
                return;
            }
            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
            HwWifiP2pService.this.setWifiRepeaterState(5);
            HwWifiP2pService.this.loge("wifirpt: CREATE_GROUP_PSK FAILED, remain at this state.", new Object[0]);
        }

        private synchronized void addP2pValidDevice(String deviceAddress) {
            if (deviceAddress != null) {
                Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
                while (iter.hasNext()) {
                    if (((String) iter.next().first).equals(deviceAddress)) {
                        iter.remove();
                    }
                }
                HwWifiP2pService.this.mValidDeivceList.add(new Pair(deviceAddress, Long.valueOf(SystemClock.elapsedRealtime())));
            }
        }

        private synchronized void removeP2pValidDevice(String deviceAddress) {
            if (HwWifiP2pService.this.mValidDeivceList != null) {
                Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
                while (iter.hasNext()) {
                    if (((String) iter.next().first).equals(deviceAddress)) {
                        iter.remove();
                    }
                }
            }
        }

        private void cleanupValidDevicelist() {
            long curTime = SystemClock.elapsedRealtime();
            Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
            while (iter.hasNext()) {
                if (curTime - ((Long) iter.next().second).longValue() > 15000) {
                    iter.remove();
                }
            }
        }

        private synchronized boolean isP2pValidDevice(String deviceAddress) {
            cleanupValidDevicelist();
            for (Pair<String, Long> entry : HwWifiP2pService.this.mValidDeivceList) {
                if (((String) entry.first).equals(deviceAddress)) {
                    return true;
                }
            }
            return false;
        }

        private synchronized void clearP2pValidDevice() {
            HwWifiP2pService.this.mValidDeivceList.clear();
        }

        private boolean isP2pReinvoked(WifiP2pConfig config, WifiP2pDevice dev) {
            if (config == null || dev == null) {
                HwWifiP2pService.this.loge("p2pReinvoke() failed, config or dev is null", new Object[0]);
                return false;
            }
            int netId = -2;
            if (config.netId < 0) {
                netId = HwWifiP2pService.this.getNetworkId(this.mGroups, dev.deviceAddress);
            } else if (config.deviceAddress.equals(HwWifiP2pService.this.getOwnerAddr(this.mGroups, config.netId))) {
                netId = config.netId;
            }
            if (netId < 0) {
                netId = getNetworkIdFromClientList(dev.deviceAddress);
            }
            HwWifiP2pService.this.logd("netId related with %{private}s = %{public}d", dev.deviceAddress, Integer.valueOf(netId));
            if (netId >= 0) {
                if (this.mWifiNative.p2pReinvoke(netId, dev.deviceAddress)) {
                    this.mSavedPeerConfig.netId = netId;
                    return true;
                }
                HwWifiP2pService.this.loge("p2pReinvoke() failed, update networks", new Object[0]);
                updatePersistentNetworks(true);
            }
            return false;
        }

        private int beamConnect(WifiP2pConfig config, boolean isTryingInvocation) {
            if (config == null) {
                HwWifiP2pService.this.loge("config is null", new Object[0]);
                return -1;
            }
            this.mSavedPeerConfig = config;
            WifiP2pDevice dev = this.mPeers.get(config.deviceAddress);
            if (dev == null) {
                HwWifiP2pService.this.loge("target device not found ", new Object[0]);
                return -1;
            }
            boolean isGroupOwner = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            HwWifiP2pService.this.logd("target ssid is %{public}s isGroupOwner:%{public}s", StringUtilEx.safeDisplaySsid(ssid), String.valueOf(isGroupOwner));
            if (isGroupOwner && dev.isGroupLimit()) {
                HwWifiP2pService.this.logd("target device reaches group limit.", new Object[0]);
                isGroupOwner = false;
            } else if (isGroupOwner) {
                int netId = HwWifiP2pService.this.getNetworkId(this.mGroups, dev.deviceAddress, ssid);
                if (netId >= 0) {
                    if (!this.mWifiNative.p2pGroupAdd(netId)) {
                        HwWifiP2pService.this.updateP2pGoCreateStatus(false);
                        return -1;
                    }
                    HwWifiP2pService.this.updateP2pGoCreateStatus(true);
                    return 0;
                }
            } else {
                HwWifiP2pService.this.logd("device is not GroupOwner", new Object[0]);
            }
            if (!isGroupOwner && dev.isDeviceLimit()) {
                HwWifiP2pService.this.loge("target device reaches the device limit.", new Object[0]);
                return -1;
            } else if (!isGroupOwner && isTryingInvocation && dev.isInvitationCapable() && isP2pReinvoked(config, dev)) {
                return 0;
            } else {
                this.mWifiNative.p2pStopFind();
                p2pBeamConnectWithPinDisplay(config);
                return 0;
            }
        }

        private void p2pBeamConnectWithPinDisplay(WifiP2pConfig config) {
            WifiP2pDevice dev = this.mPeers.get(config.deviceAddress);
            if (dev == null) {
                HwWifiP2pService.this.loge("target device is not found ", new Object[0]);
                return;
            }
            String pin = this.mWifiNative.p2pConnect(config, dev.isGroupOwner());
            try {
                Integer.parseInt(pin);
                notifyInvitationSent(pin, config.deviceAddress);
            } catch (NumberFormatException e) {
                HwWifiP2pService.this.loge("do nothing if p2pConnect did not return a pin", new Object[0]);
            }
        }

        private void sendPeersChangedBroadcast() {
            Intent intent = new Intent("android.net.wifi.p2p.PEERS_CHANGED");
            intent.putExtra("wifiP2pDeviceList", new WifiP2pDeviceList(this.mPeers));
            intent.addFlags(67108864);
            HwWifiP2pService.this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendP2pConnectionStateBroadcast(int state) {
            HwWifiP2pService.this.logd("sending p2p connection state broadcast and state = %{public}d", Integer.valueOf(state));
            Intent intent = new Intent("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("extraState", state);
            if (this.mSavedPeerConfig == null || state != 2) {
                HwWifiP2pService.this.loge("GroupCreatedState:mSavedConnectConfig is null", new Object[0]);
            } else {
                String opposeInterfaceAddressString = this.mSavedPeerConfig.deviceAddress;
                String conDeviceName = null;
                intent.putExtra("interfaceAddress", opposeInterfaceAddressString);
                Iterator<WifiP2pDevice> it = this.mPeers.getDeviceList().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    WifiP2pDevice device = it.next();
                    if (device.deviceAddress != null && device.deviceAddress.equals(this.mSavedPeerConfig.deviceAddress)) {
                        conDeviceName = device.deviceName;
                        break;
                    }
                }
                intent.putExtra("oppDeviceName", conDeviceName);
                HwWifiP2pService.this.logd("oppDeviceName = %{public}s", conDeviceName);
                HwWifiP2pService.this.logd("opposeInterfaceAddressString = %{private}s", opposeInterfaceAddressString);
            }
            if (state == 2) {
                if (HwWifiP2pService.this.mP2pReuseCnt <= 0) {
                    HwWifiP2pService.this.mP2pReuseCnt = 1;
                }
                HwWifiP2pService.this.mIsP2pConnected = true;
            } else {
                HwWifiP2pService.this.resetP2pReuseResources();
            }
            HwWifiP2pService.this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
        }

        public boolean autoAcceptConnection() {
            if (!isP2pValidDevice(this.mSavedPeerConfig.deviceAddress) && !isP2pValidDevice(getDeviceName(this.mSavedPeerConfig.deviceAddress))) {
                return false;
            }
            HwWifiP2pService.this.logd("notifyInvitationReceived is a valid device", new Object[0]);
            removeP2pValidDevice(this.mSavedPeerConfig.deviceAddress);
            sendMessage(HwWifiP2pService.wifiP2pServiceUtils.getPeerConnectionUserAccept(HwWifiP2pService.this));
            return true;
        }

        private String getDeviceName(String deviceAddress) {
            WifiP2pDevice device = this.mPeers.get(deviceAddress);
            if (device != null) {
                return device.deviceName;
            }
            return deviceAddress;
        }

        private void selfcureP2pGoCreateFail() {
            loge("selfcureP2pGoCreatedFail times >= 2, then reset wifi");
            WifiInjector wifiInjector = WifiInjector.getInstance();
            HwWifiP2pService.this.mP2pCreateGoFailTimes = 0;
            wifiInjector.getSelfRecovery().trigger(1);
        }

        private void resetP2pChannelSet() {
            loge("resetP2pChannelSet");
            this.mWifiNative.p2pSetChannel(0, 0);
        }

        private int getRandom2gChannel() {
            int result = HwWifiP2pService.COMMON_CHANNELS_2G[new SecureRandom().nextInt(HwWifiP2pService.COMMON_CHANNELS_2G.length)];
            HwHiSlog.d(HwWifiP2pService.TAG, false, "getRandom2gChannel: %{public}d", new Object[]{Integer.valueOf(result)});
            return result;
        }

        private int getUsable5gChannel() {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            String countryCode = "";
            if (wifiInjector != null) {
                countryCode = wifiInjector.getWifiCountryCode().getCountryCodeSentToDriver();
            }
            if (TextUtils.isEmpty(countryCode)) {
                return WifiCommonUtils.convertChannelToFrequency(getRandom2gChannel());
            }
            if ("CN".equals(countryCode)) {
                return 5180;
            }
            int selectedChannel = HwApConfigUtilEx.getSelected5GChannel(SoftApChannelXmlParse.convertChannelListToFrequency(HwSoftApManager.getChannelListFor5GWithoutIndoor()));
            if (selectedChannel == -1) {
                selectedChannel = getRandom2gChannel();
            }
            return WifiCommonUtils.convertChannelToFrequency(selectedChannel);
        }

        private int getFreqWhenSetDfsChannel(int frequency) {
            WifiManager wifiManager = (WifiManager) HwWifiP2pService.this.mContext.getSystemService("wifi");
            WifiInfo wifiInfo = null;
            if (wifiManager != null) {
                wifiInfo = wifiManager.getConnectionInfo();
            }
            if (!HwWifiP2pService.this.isWifiConnected() || wifiInfo == null) {
                return getUsable5gChannel();
            }
            if (HwWifiP2pService.this.mHwDfsMonitor != null && wifiInfo.getFrequency() == frequency) {
                HwWifiP2pService.this.mHwDfsMonitor.closeGoCac(0);
            }
            return wifiInfo.getFrequency();
        }

        private String checkDfsWhenMagiclinkCreateGroup(String freq) {
            HwHiSlog.i(HwWifiP2pService.TAG, false, "checkDfsWhenMagiclinkCreateGroup freq=%{public}s", new Object[]{freq});
            String sourceFreq = freq;
            String freqBw80M = freq;
            boolean is160M = false;
            if (!TextUtils.isEmpty(sourceFreq) && sourceFreq.endsWith(HwWifiP2pService.MAGICLINK_CREATE_GROUP_160M_FLAG)) {
                freqBw80M = sourceFreq.replace(HwWifiP2pService.MAGICLINK_CREATE_GROUP_160M_FLAG, "");
                is160M = true;
            }
            try {
                int frequency = Integer.parseInt(freqBw80M);
                WifiInjector wifiInjector = WifiInjector.getInstance();
                boolean isDfsChannel = false;
                if (wifiInjector != null) {
                    isDfsChannel = wifiInjector.getWifiNative().mHwWifiNativeEx.isDfsChannel(frequency);
                }
                if (!HwWifiP2pService.this.mIsUsingHwShare || (!is160M && !isDfsChannel)) {
                    if (HwWifiP2pService.this.mHwDfsMonitor != null && isDfsChannel) {
                        HwWifiP2pService.this.mHwDfsMonitor.closeGoCac(0);
                    }
                    HwWifiP2pService.this.mIsUsingHwShare = false;
                    return is160M ? freqBw80M : sourceFreq;
                }
                HwWifiP2pService.this.mIsUsingHwShare = false;
                if (is160M && HwMssUtils.is1105() && !HwWifiP2pService.this.isWifiConnected() && frequency >= 5500) {
                    frequency = getUsable5gChannel();
                    sourceFreq = String.valueOf(frequency) + HwWifiP2pService.MAGICLINK_CREATE_GROUP_160M_FLAG;
                }
                if (HwWifiP2pService.this.mHwDfsMonitor != null && HwWifiP2pService.this.mHwDfsMonitor.isDfsUsable(frequency)) {
                    HwWifiP2pService.this.mHwDfsMonitor.closeGoCac(0);
                    return isDfsChannel ? freqBw80M : sourceFreq;
                } else if (isDfsChannel) {
                    return String.valueOf(getFreqWhenSetDfsChannel(frequency));
                } else {
                    return freqBw80M;
                }
            } catch (NumberFormatException e) {
                HwHiSlog.e(HwWifiP2pService.TAG, false, "freq parseInt fail", new Object[0]);
                return sourceFreq;
            }
        }

        private boolean isAllowP2pCnnect(Message message) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector != null) {
                String wifiModeCallerPackageName = wifiInjector.getClientModeImpl().getWifiModeCallerPackageName();
                String callingPkgName = getCallingPkgNameEx(message.sendingUid, message.replyTo);
                int wifiMode = wifiInjector.getClientModeImpl().getWifiMode();
                if ((wifiMode & 16) != 0) {
                    if ((wifiMode & 32) == 0 || !"com.huawei.nearby".equals(callingPkgName) || (!wifiModeCallerPackageName.contains("com.huawei.android.instantshare") && !wifiModeCallerPackageName.contains("com.huawei.nearby"))) {
                        HwHiSlog.i(HwWifiP2pService.TAG, false, "wifi mode is set, do not allow other p2p connect but HwShare", new Object[0]);
                        return false;
                    }
                    HwHiSlog.i(HwWifiP2pService.TAG, false, "wifi mode is set, only allow HwShare connect", new Object[0]);
                    return true;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void sendP2pConnectingStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this, new Object[0]);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(1);
    }

    /* access modifiers changed from: protected */
    public void sendP2pFailStateBroadcast() {
        HwWifiCHRService hwWifiCHRService;
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this, new Object[0]);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(3);
        if (this.mIsWifiRepeaterTetherStarted && (hwWifiCHRService = this.mHwWifiChrService) != null) {
            hwWifiCHRService.addRepeaterConnFailedCount(1);
        }
    }

    /* access modifiers changed from: protected */
    public void sendP2pConnectedStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this, new Object[0]);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(2);
    }

    /* access modifiers changed from: protected */
    public void clearValidDeivceList() {
        this.mValidDeivceList.clear();
    }

    /* access modifiers changed from: protected */
    public boolean autoAcceptConnection() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this, new Object[0]);
        return this.mHwP2pStateMachine.autoAcceptConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String string, Object... args) {
        HwHiSlog.e(TAG, false, string, args);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String string, Object... args) {
        HwHiSlog.d(TAG, false, string, args);
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifiConnected() {
        NetworkInfo networkInfo = this.mNetworkInfo;
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }

    public static class SensitiveObj {
        private final Object mObj;

        public SensitiveObj(Object obj) {
            this.mObj = obj;
        }

        public String toString() {
            return String.valueOf(this.mObj);
        }
    }

    /* access modifiers changed from: protected */
    public boolean startWifiRepeater(WifiP2pGroup group) {
        this.mTetherInterfaceName = group.getInterface();
        if (IS_HWDBG_ON) {
            logd("start wifi repeater, ifaceName=" + this.mTetherInterfaceName + ", mIsWifiRepeaterEnabled=" + this.mIsWifiRepeaterEnabled + ", isWifiConnected=" + isWifiConnected(), new Object[0]);
        }
        this.mWifiRepeaterFreq = group.getFrequency();
        if (isWifiConnected()) {
            int resultCode = getConnectivityManager().tether(this.mTetherInterfaceName);
            if (IS_HWDBG_ON) {
                logd("ConnectivityManager.tether resultCode = " + resultCode, new Object[0]);
            }
            if (resultCode == 0) {
                this.mWifiRepeater.handleP2pTethered(group);
                this.mIsWifiRepeaterTetherStarted = true;
                setWifiRepeaterState(1);
                HwWifiCHRService hwWifiCHRService = this.mHwWifiChrService;
                if (hwWifiCHRService != null) {
                    hwWifiCHRService.addWifiRepeaterOpenedCount(1);
                    this.mHwWifiChrService.setWifiRepeaterStatus(true);
                }
                return true;
            }
        }
        setWifiRepeaterState(5);
        return false;
    }

    /* access modifiers changed from: protected */
    public String getWifiRepeaterServerAddress() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            if (IS_HWDBG_ON) {
                logd("getWifiRepeaterServerAddress use SERVER_ADDRESS_WIFI_BRIDGE because wifiManageris null", new Object[0]);
            }
            return SERVER_ADDRESS_WIFI_BRIDGE;
        }
        int defaultAddress = NetworkUtils.inetAddressToInt((Inet4Address) NetworkUtils.numericToInetAddress(SERVER_ADDRESS_WIFI_BRIDGE));
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo == null || (dhcpInfo.gateway & 16777215) != (16777215 & defaultAddress)) {
            if (IS_HWDBG_ON) {
                logd("getWifiRepeaterServerAddress use SERVER_ADDRESS_WIFI_BRIDGE", new Object[0]);
            }
            return SERVER_ADDRESS_WIFI_BRIDGE;
        } else if (!IS_HWDBG_ON) {
            return SERVER_ADDRESS_WIFI_BRIDGE_OTHER;
        } else {
            logd("getWifiRepeaterServerAddress use SERVER_ADDRESS_WIFI_BRIDGE_OTHER", new Object[0]);
            return SERVER_ADDRESS_WIFI_BRIDGE_OTHER;
        }
    }

    public WifiRepeater getWifiRepeater() {
        return this.mWifiRepeater;
    }

    public void notifyRptGroupRemoved() {
        this.mWifiRepeater.handleP2pUntethered();
    }

    public int getWifiRepeaterFreq() {
        return this.mWifiRepeaterFreq;
    }

    public int getWifiRepeaterChannel() {
        return WifiCommonUtils.convertFrequencyToChannelNumber(this.mWifiRepeaterFreq);
    }

    public boolean getWifiRepeaterTetherStarted() {
        return this.mIsWifiRepeaterTetherStarted;
    }

    public void handleClientConnect(WifiP2pGroup group) {
        if (this.mIsWifiRepeaterTetherStarted && group != null) {
            if (group.getClientList().size() >= 1) {
                DecisionUtil.bindService(this.mContext);
                HwHiLog.d(TAG, false, "bindService", new Object[0]);
            }
            this.mWifiRepeater.handleClientListChanged(group);
            if (HwWifiService.SHOULD_NETWORK_SHARING_INTEGRATION) {
                handleClientChangedAction(group, 0);
            }
            if (this.mWifiRepeaterBeginWorkTime == 0 && group.getClientList().size() == 1) {
                this.mWifiRepeaterBeginWorkTime = SystemClock.elapsedRealtime();
            }
            this.mHwWifiChrService.setRepeaterMaxClientCount(group.getClientList().size() > 0 ? group.getClientList().size() : 0);
        }
    }

    public void handleClientDisconnect(WifiP2pGroup group) {
        if (group != null && this.mIsWifiRepeaterTetherStarted) {
            this.mWifiRepeater.handleClientListChanged(group);
            if (HwWifiService.SHOULD_NETWORK_SHARING_INTEGRATION) {
                handleClientChangedAction(group, 1);
            }
            if (group.getClientList().size() == 0 && this.mHwWifiChrService != null) {
                this.mWifiRepeaterEndWorkTime = SystemClock.elapsedRealtime();
                this.mHwWifiChrService.setWifiRepeaterWorkingTime((this.mWifiRepeaterEndWorkTime - this.mWifiRepeaterBeginWorkTime) / MS_TO_S);
                this.mWifiRepeaterBeginWorkTime = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopWifiRepeater(WifiP2pGroup group) {
        HwWifiCHRService hwWifiCHRService;
        setWifiRepeaterState(2);
        this.mIsWifiRepeaterEnabled = false;
        this.mWifiRepeaterEndWorkTime = SystemClock.elapsedRealtime();
        if (!(group == null || group.getClientList().size() <= 0 || (hwWifiCHRService = this.mHwWifiChrService) == null)) {
            hwWifiCHRService.setWifiRepeaterWorkingTime((this.mWifiRepeaterEndWorkTime - this.mWifiRepeaterBeginWorkTime) / MS_TO_S);
        }
        this.mWifiRepeaterBeginWorkTime = 0;
        this.mWifiRepeaterEndWorkTime = 0;
        this.mWifiRepeaterFreq = 0;
        HwWifiCHRService hwWifiCHRService2 = this.mHwWifiChrService;
        if (hwWifiCHRService2 != null) {
            hwWifiCHRService2.setWifiRepeaterFreq(this.mWifiRepeaterFreq);
        }
        if (this.mIsWifiRepeaterTetherStarted) {
            int resultCode = getConnectivityManager().untether(this.mTetherInterfaceName);
            if (IS_HWDBG_ON) {
                logd("ConnectivityManager.untether resultCode = " + resultCode, new Object[0]);
            }
            this.mIsWifiRepeaterTetherStarted = false;
            this.mWifiRepeaterClientList.clear();
            if (resultCode == 0) {
                setWifiRepeaterState(0);
                HwWifiCHRService hwWifiCHRService3 = this.mHwWifiChrService;
                if (hwWifiCHRService3 != null) {
                    hwWifiCHRService3.setWifiRepeaterStatus(false);
                    return;
                }
                return;
            }
            loge("Untether initiate failed!", new Object[0]);
            setWifiRepeaterState(4);
            return;
        }
        setWifiRepeaterState(0);
        this.mWifiRepeaterClientList.clear();
    }

    public void setWifiRepeaterState(int state) {
        Context context = this.mContext;
        if (context != null) {
            Settings.Global.putInt(context.getContentResolver(), "wifi_repeater_on", state);
            Intent intent = new Intent("com.huawei.android.net.wifi.p2p.action.WIFI_RPT_STATE_CHANGED");
            intent.putExtra("wifi_rpt_state", state);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getWifiRepeaterEnabled() {
        return this.mIsWifiRepeaterEnabled;
    }

    /* access modifiers changed from: protected */
    public void initWifiRepeaterConfig() {
        if (this.mWifiRepeaterConfigChannel == null) {
            this.mWifiRepeaterConfigChannel = new AsyncChannel();
            this.mWifiRepeaterConfigStore = WifiRepeaterConfigStore.makeWifiRepeaterConfigStore(this.mP2pStateMachine.getHandler());
            this.mWifiRepeaterConfigStore.loadRepeaterConfiguration();
            this.mWifiRepeaterConfigChannel.connectSync(this.mContext, this.mP2pStateMachine.getHandler(), this.mWifiRepeaterConfigStore.getMessenger());
        }
    }

    public void setWifiRepeaterConfiguration(WifiConfiguration config) {
        AsyncChannel asyncChannel = this.mWifiRepeaterConfigChannel;
        if (asyncChannel != null && config != null) {
            asyncChannel.sendMessage((int) CMD_SET_REPEATER_CONFIG, config);
        }
    }

    public WifiConfiguration syncGetWifiRepeaterConfiguration() {
        Message resultMsg;
        AsyncChannel asyncChannel = this.mWifiRepeaterConfigChannel;
        if (asyncChannel == null || (resultMsg = asyncChannel.sendMessageSynchronously((int) CMD_REQUEST_REPEATER_CONFIG)) == null) {
            return null;
        }
        WifiConfiguration ret = (WifiConfiguration) resultMsg.obj;
        resultMsg.recycle();
        return ret;
    }

    private void handleClientChangedAction(WifiP2pGroup group, int clientAction) {
        int curLinkedCliectCount = group.getClientList().size();
        if (curLinkedCliectCount > 4) {
            HwHiLog.d(TAG, false, "LinkedCliectCount over flow, need synchronize. CliectCount=%{public}d", new Object[]{Integer.valueOf(curLinkedCliectCount)});
            return;
        }
        String action = null;
        long currentTime = System.currentTimeMillis();
        String deviceAddress = null;
        Collection<WifiP2pDevice> curClientList = new ArrayList<>(group.getClientList());
        if (clientAction == 0) {
            action = WIFI_REPEATER_CLIENT_JOIN_ACTION;
            curClientList.removeAll(this.mWifiRepeaterClientList);
            if (curClientList.isEmpty()) {
                HwHiLog.d(TAG, false, "no new client join", new Object[0]);
                return;
            }
            deviceAddress = new ArrayList<>(curClientList).get(0).deviceAddress;
        }
        if (clientAction == 1) {
            action = WIFI_REPEATER_CLIENT_LEAVE_ACTION;
            this.mWifiRepeaterClientList.removeAll(curClientList);
            if (this.mWifiRepeaterClientList.isEmpty()) {
                HwHiLog.d(TAG, false, "no client leave", new Object[0]);
                return;
            }
            deviceAddress = new ArrayList<>(this.mWifiRepeaterClientList).get(0).deviceAddress;
        }
        this.mWifiRepeaterClientList.clear();
        this.mWifiRepeaterClientList.addAll(group.getClientList());
        if (deviceAddress != null && action != null) {
            String.format("MAC=%s TIME=%d STACNT=%d", StringUtilEx.safeDisplayBssid(deviceAddress), Long.valueOf(currentTime), Integer.valueOf(curLinkedCliectCount));
            HwHiLog.d(TAG, false, "Send broadcast: %{public}s, extraInfo: MAC=%{private}s TIME=%{public}d STACNT=%{public}d", new Object[]{action, StringUtilEx.safeDisplayBssid(deviceAddress), Long.valueOf(currentTime), Integer.valueOf(curLinkedCliectCount)});
            Intent intent = new Intent(action);
            intent.addFlags(16777216);
            intent.putExtra(EXTRA_CLIENT_INFO, deviceAddress);
            intent.putExtra(EXTRA_CURRENT_TIME, currentTime);
            intent.putExtra(EXTRA_STA_COUNT, curLinkedCliectCount);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
        }
    }

    public List<String> getRepeaterLinkedClientList() {
        if (!this.mIsWifiRepeaterTetherStarted) {
            HwHiLog.d(TAG, false, "mWifiRepeaterClientList is null or empty", new Object[0]);
            return Collections.emptyList();
        }
        WifiP2pGroup wifiP2pGroup = wifiP2pServiceUtils.getmGroup(this.mP2pStateMachine);
        if (wifiP2pGroup == null) {
            return Collections.emptyList();
        }
        Collection<WifiP2pDevice> curClientList = new ArrayList<>(wifiP2pGroup.getClientList());
        List<String> dhcpList = HwSoftApManager.readSoftapStaDhcpInfo();
        List<String> infoList = new ArrayList<>();
        for (WifiP2pDevice p2pDevice : curClientList) {
            infoList.add(HwSoftApManager.getApLinkedStaInfo(p2pDevice.deviceAddress, dhcpList));
        }
        HwHiLog.d(TAG, false, "linkedClientInfo: info size=%{public}d", new Object[]{Integer.valueOf(infoList.size())});
        return infoList;
    }

    public void setWifiRepeaterDisassociateSta(String mac) {
        if (!this.mIsWifiRepeaterTetherStarted || TextUtils.isEmpty(mac)) {
            HwHiLog.d(TAG, false, "setWifiRepeaterDisassociateSta called when WifiRepeaterTether is not Started", new Object[0]);
        } else if (TextUtils.isEmpty(this.mTetherInterfaceName)) {
            HwHiLog.d(TAG, false, "setWifiRepeaterDisassociateSta mInterface is empty", new Object[0]);
        } else if (!WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.disassociateWifiRepeaterStationHw(this.mTetherInterfaceName, mac)) {
            HwHiLog.e(TAG, false, "Failed to setWifiRepeaterDisassociateSta", new Object[0]);
        }
    }

    public void setWifiRepeaterMacFilter(String macFilter) {
        if (!this.mIsWifiRepeaterTetherStarted || TextUtils.isEmpty(this.mTetherInterfaceName) || TextUtils.isEmpty(macFilter)) {
            HwHiLog.d(TAG, false, "setWifiRepeaterMacFilter called when WifiRepeaterTether is not Started", new Object[0]);
        } else if (!WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setWifiRepeaterMacFilterHw(this.mTetherInterfaceName, macFilter)) {
            HwHiLog.e(TAG, false, "Failed to setWifiRepeaterMacFilter", new Object[0]);
        } else {
            if (this.mHwWifiChrService != null && !this.mMacFilterStr.equals(macFilter)) {
                String[] macFilterStrs = macFilter.split(SPLIT_DOT);
                if (macFilterStrs.length < 2) {
                    HwHiLog.e(TAG, false, "length of macFilterStrs is not enough ", new Object[0]);
                    return;
                }
                String[] macFilterCountStrs = macFilterStrs[1].split(SPLIT_EQUAL);
                if (macFilterCountStrs.length < 2) {
                    HwHiLog.e(TAG, false, "length of macFilterCntStrs is not enough ", new Object[0]);
                    return;
                }
                try {
                    int macFilterCount = Integer.parseInt(macFilterCountStrs[1]);
                    HwHiLog.d(TAG, false, "setWifiRepeaterMacFilter count = %{public}d", new Object[]{Integer.valueOf(macFilterCount)});
                    if (macFilterCount > 0 && macFilterCount >= this.mMacFilterStaCount) {
                        Bundle data = new Bundle();
                        data.putInt("repeaterBlacklistCnt", 1);
                        this.mHwWifiChrService.uploadDFTEvent(14, data);
                    }
                    this.mMacFilterStaCount = macFilterCount;
                } catch (NumberFormatException e) {
                    HwHiLog.e(TAG, false, "Exception happens", new Object[0]);
                    return;
                }
            }
            this.mMacFilterStr = macFilter;
        }
    }

    public boolean hasP2pService() {
        WifiP2pGroup wifiP2pGroup = wifiP2pServiceUtils.getmGroup(this.mP2pStateMachine);
        if (wifiP2pGroup == null) {
            return false;
        }
        String p2pGroupInterface = wifiP2pGroup.getInterface();
        if (TextUtils.isEmpty(p2pGroupInterface)) {
            return false;
        }
        try {
            NetworkInterface p2pInterface = NetworkInterface.getByName(p2pGroupInterface);
            if (p2pInterface != null && !p2pInterface.isLoopback()) {
                if (p2pInterface.isUp()) {
                    Enumeration<InetAddress> addrs = p2pInterface.getInetAddresses();
                    if (addrs == null || !addrs.hasMoreElements()) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "SocketException exception when get p2pInterface", new Object[0]);
            return false;
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    private void enforceDisableDhcpPermission() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION_DISABLE_P2P_GC_DHCP, TAG);
    }

    private boolean isInRequsetDfsStatusWhiteList(String packageName) {
        for (String whitePackageName : REQUEST_DFS_STATUS_WHITE_PACKAGE_LIST) {
            if (whitePackageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean requsetDfsStatus(Parcel data, Parcel reply) {
        if (!wifiP2pServiceUtils.checkSignMatchOrIsSystemApp(this.mContext)) {
            HwHiLog.e(TAG, false, "REQUEST_DFS_STATUS SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String packageName = data.readString();
        HwHiLog.i(TAG, false, "REQUEST_DFS_STATUS packageName=%{public}s", new Object[]{packageName});
        if (!isInRequsetDfsStatusWhiteList(packageName)) {
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        this.mIsUsingHwShare = true;
        int frequency = data.readInt();
        int bandWidth = data.readInt();
        IWifiActionListener actionListener = IWifiActionListener.Stub.asInterface(data.readStrongBinder());
        int result = 0;
        HwDfsMonitor hwDfsMonitor = this.mHwDfsMonitor;
        if (hwDfsMonitor != null) {
            result = hwDfsMonitor.requestDfsStatus(frequency, bandWidth, actionListener);
        }
        reply.writeNoException();
        reply.writeInt(result);
        return true;
    }

    private boolean updateDfsStatus(Parcel data, Parcel reply) {
        if (!wifiP2pServiceUtils.checkSignMatchOrIsSystemApp(this.mContext)) {
            HwHiLog.e(TAG, false, "UPDATE_DFS_STATUS SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String packageName = data.readString();
        HwHiLog.i(TAG, false, "UPDATE_DFS_STATUS packageName=%{public}s", new Object[]{packageName});
        if (!isInRequsetDfsStatusWhiteList(packageName)) {
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        int transferResult = data.readInt();
        int transferRate = data.readInt();
        HwDfsMonitor hwDfsMonitor = this.mHwDfsMonitor;
        if (hwDfsMonitor != null) {
            hwDfsMonitor.updateDfsStatus(transferResult, transferRate);
        }
        reply.writeNoException();
        reply.writeInt(1);
        return true;
    }

    private boolean validateMacAddress(String mac) {
        if (mac != null) {
            return Pattern.compile(PATTERN_MAC).matcher(mac).matches();
        }
        return false;
    }

    private boolean applyMagiclinkGcIp(Parcel data, Parcel reply) {
        String ifP2p0Mac;
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "applyMagiclinkGcIp ", new Object[0]);
        if (data.readInt() != 0) {
            ifP2p0Mac = data.readString();
        } else {
            ifP2p0Mac = null;
        }
        reply.writeNoException();
        if (!validateMacAddress(ifP2p0Mac)) {
            reply.writeInt(0);
            return true;
        }
        String ip = HwMagiclinkIpManager.getIp();
        if (ip == null) {
            reply.writeInt(0);
            return true;
        }
        this.mMagiclinkGcIpMap.put(ifP2p0Mac, ip);
        reply.writeInt(1);
        reply.writeString(ip);
        return true;
    }

    private boolean getP2pGroupConfigInfo(Parcel data, Parcel reply) {
        if (!wifiP2pServiceUtils.checkSignMatchOrIsSystemApp(this.mContext)) {
            HwHiLog.e(TAG, false, "WifiP2pService CODE_GET_GROUP_CONFIG_INFO SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            reply.writeString("");
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        reply.writeNoException();
        String configInfo = this.mConfigInfo;
        if (configInfo == null) {
            reply.writeInt(0);
            return true;
        }
        reply.writeInt(1);
        reply.writeString(configInfo);
        return true;
    }

    private boolean getWifiRepeaterConfig(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "GetWifiRepeaterConfiguration ", new Object[0]);
        WifiConfiguration config = syncGetWifiRepeaterConfiguration();
        reply.writeNoException();
        if (config != null) {
            reply.writeInt(1);
            config.writeToParcel(reply, 1);
        } else {
            reply.writeInt(0);
        }
        return true;
    }

    private boolean setWifiRepeaterConfig(Parcel data, Parcel reply) {
        WifiConfiguration config;
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "setWifiRepeaterConfiguration ", new Object[0]);
        if (data.readInt() != 0) {
            config = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
        } else {
            config = null;
        }
        setWifiRepeaterConfiguration(config);
        reply.writeNoException();
        return true;
    }

    private boolean configMagiclinkConnectIp(Parcel data, Parcel reply) {
        String gateway;
        String ipAddr;
        String ifaceName;
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "configIpAddr ", new Object[0]);
        if (data.readInt() != 0) {
            ifaceName = data.readString();
            ipAddr = data.readString();
            gateway = data.readString();
        } else {
            ifaceName = null;
            ipAddr = null;
            gateway = null;
        }
        configIpAddr(ifaceName, ipAddr, gateway);
        reply.writeNoException();
        return true;
    }

    private boolean releaseMagiclinkConnectIp(Parcel data, Parcel reply) {
        String interfaceName;
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "setWifiRepeaterConfiguration ", new Object[0]);
        if (data.readInt() != 0) {
            interfaceName = data.readString();
        } else {
            interfaceName = null;
        }
        releaseIpAddr(interfaceName);
        reply.writeNoException();
        return true;
    }

    private boolean disableP2pGcDhcpTransact(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        enforceDisableDhcpPermission();
        HwHiSlog.i(TAG, false, "disableP2pGcDhcp", new Object[0]);
        boolean isSuccess = disableP2pGcDhcp(data.readString(), Binder.getCallingUid(), data.readInt());
        reply.writeNoException();
        reply.writeBooleanArray(new boolean[]{isSuccess});
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseBinder(IBinder binder) {
        HwHiLog.i(TAG, false, "releaseToken, token: " + binder, new Object[0]);
        DeathHandlerData dhd = this.mDeathDataByBinder.remove(binder);
        if (dhd != null) {
            binder.unlinkToDeath(dhd.mDeathRecipient, 0);
            Message msg = Message.obtain();
            msg.what = 141275;
            msg.arg1 = 0;
            msg.arg2 = 0;
            msg.obj = new Bundle();
            handleClientHwMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class TokenDeathRecipient implements IBinder.DeathRecipient {
        private final IBinder mBinder;

        TokenDeathRecipient(IBinder binder) throws RemoteException {
            this.mBinder = binder;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HwWifiP2pService.this.releaseBinder(this.mBinder);
        }
    }

    /* access modifiers changed from: private */
    public class DeathHandlerData {
        IBinder.DeathRecipient mDeathRecipient;
        int mPid;

        DeathHandlerData(IBinder.DeathRecipient dr, int pid) {
            this.mDeathRecipient = dr;
            this.mPid = pid;
        }

        public String toString() {
            return "deathRecipient=" + this.mDeathRecipient + ", pid=" + this.mPid;
        }
    }

    private void linkToBinderDeath(IBinder binder, int pid) {
        HwHiLog.i(TAG, false, "linkToBinderDeath is called binder=" + binder + ", pid=" + pid, new Object[0]);
        if (binder == null) {
            HwHiLog.e(TAG, false, "linkToBinderDeath, binder is null", new Object[0]);
            return;
        }
        try {
            TokenDeathRecipient deathRecipient = new TokenDeathRecipient(binder);
            binder.linkToDeath(deathRecipient, 0);
            this.mDeathDataByBinder.put(binder, new DeathHandlerData(deathRecipient, pid));
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "linkToBinderDeath, Error on linkToDeath", new Object[0]);
        }
    }

    private boolean addBinderMap(Parcel data, Parcel reply) {
        if (data == null || reply == null || !wifiP2pServiceUtils.checkSignMatchOrIsSystemApp(this.mContext)) {
            HwHiLog.e(TAG, false, "addBinderMap: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        linkToBinderDeath(data.readStrongBinder(), Binder.getCallingPid());
        reply.writeNoException();
        return true;
    }

    private boolean removeBinderMap(Parcel data, Parcel reply) {
        if (data == null || reply == null || !wifiP2pServiceUtils.checkSignMatchOrIsSystemApp(this.mContext)) {
            HwHiLog.e(TAG, false, "removeBinderMap: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        IBinder binder = data.readStrongBinder();
        HwHiLog.i(TAG, false, "removeBinderMap is called binder=" + binder, new Object[0]);
        this.mDeathDataByBinder.remove(binder);
        reply.writeNoException();
        return true;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "onTransact: data or reply is null", new Object[0]);
            return false;
        } else if (code == CODE_NOTIFY_P2P_BINDER_ADD) {
            return addBinderMap(data, reply);
        } else {
            if (code == CODE_NOTIFY_P2P_BINDER_REMOVE) {
                return removeBinderMap(data, reply);
            }
            switch (code) {
                case 1001:
                    return getWifiRepeaterConfig(data, reply);
                case 1002:
                    return setWifiRepeaterConfig(data, reply);
                case 1003:
                    return configMagiclinkConnectIp(data, reply);
                case 1004:
                    return releaseMagiclinkConnectIp(data, reply);
                case 1005:
                    return getP2pGroupConfigInfo(data, reply);
                case 1006:
                    return disableP2pGcDhcpTransact(data, reply);
                case 1007:
                    return requsetDfsStatus(data, reply);
                case 1008:
                    return updateDfsStatus(data, reply);
                case 1009:
                    return applyMagiclinkGcIp(data, reply);
                default:
                    return HwWifiP2pService.super.onTransact(code, data, reply, flags);
            }
        }
    }

    private boolean isP2pCollisionInDefaultState(Message msg) {
        switch (msg.what) {
            case 139265:
            case 139268:
            case 139271:
            case 139274:
            case 139277:
            case 139315:
            case 139318:
            case 139321:
                if (!this.mIsWifiRepeaterEnabled) {
                    return false;
                }
                showUserToastIfP2pCollision();
                return true;
            default:
                return false;
        }
    }

    private boolean isP2pCollisionInP2pEnabledState(Message msg) {
        switch (msg.what) {
            case 139265:
            case 139318:
            case 139329:
            case 139332:
            case 139335:
                if (!this.mIsWifiRepeaterEnabled) {
                    return false;
                }
                showUserToastIfP2pCollision();
                return true;
            default:
                return false;
        }
    }

    private boolean isP2pCollisionInInactiveState(Message msg) {
        switch (msg.what) {
            case 139265:
            case 139274:
            case 139277:
            case 139329:
            case 139332:
            case 139335:
                if (!this.mIsWifiRepeaterEnabled) {
                    return false;
                }
                showUserToastIfP2pCollision();
                return true;
            default:
                return false;
        }
    }

    private boolean isP2pCollisionInGroupCreatingState(Message msg) {
        int i = msg.what;
        if ((i != 139265 && i != 139274) || !this.mIsWifiRepeaterEnabled) {
            return false;
        }
        showUserToastIfP2pCollision();
        return true;
    }

    private boolean isP2pCollisionInGroupCreatedState(Message msg) {
        int i = msg.what;
        if (i != 139265) {
            if (i != 139271 || !this.mIsWifiRepeaterEnabled) {
                return false;
            }
            showUserToastIfP2pCollision();
            return true;
        } else if (!this.mIsWifiRepeaterEnabled) {
            return false;
        } else {
            if (shouldShowP2pCollisionToast(msg)) {
                showUserToastIfP2pCollision();
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessageForP2pCollision(Message msg, State state) {
        boolean isP2pCollision = false;
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.DefaultState) {
            isP2pCollision = isP2pCollisionInDefaultState(msg);
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.P2pEnabledState) {
            isP2pCollision = isP2pCollisionInP2pEnabledState(msg);
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.InactiveState) {
            isP2pCollision = isP2pCollisionInInactiveState(msg);
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.GroupCreatingState) {
            isP2pCollision = isP2pCollisionInGroupCreatingState(msg);
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.GroupCreatedState) {
            isP2pCollision = isP2pCollisionInGroupCreatedState(msg);
        }
        return isP2pCollision || handleP2pWlan1Collision(msg);
    }

    private boolean handleP2pWlan1Collision(Message msg) {
        if (msg.what == 139274) {
            this.mCacheMsgToReSendWlan1Down = null;
            return false;
        } else if (msg.what != 139271 && msg.what != 139277 && msg.what != 141269 && msg.what != 141270 && msg.what != 141267 && msg.what != 141268) {
            return false;
        } else {
            HwWifi2Manager hwWifi2Manager = HwWifi2Manager.getInstance();
            if (hwWifi2Manager == null) {
                HwHiLog.e(TAG, false, "handleP2pWlan1Collision: hwWifi2Manager is null", new Object[0]);
                return false;
            }
            NetworkInfo networkInfo = hwWifi2Manager.getNetworkInfoForSlaveWifi();
            if (networkInfo == null) {
                HwHiLog.e(TAG, false, "handleP2pWlan1Collision: networkInfo is null", new Object[0]);
                return false;
            }
            NetworkInfo.DetailedState state = networkInfo.getDetailedState();
            if (NetworkInfo.DetailedState.CONNECTED.equals(state) || NetworkInfo.DetailedState.CONNECTING.equals(state) || this.mWifi2State.get() != 1) {
                HwHiLog.i(TAG, false, "wlan1 is active p2p msg is %{public}d, begin handle collision.", new Object[]{Integer.valueOf(msg.what)});
                this.mCacheMsgToReSendWlan1Down = Message.obtain(msg);
                hwWifi2Manager.handleP2pConnectCommand(msg.what);
                return true;
            }
            hwWifi2Manager.handleP2pConnectCommand(msg.what);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifi2StateChange(Intent intent) {
        Message message;
        Object extraWifi2State = intent.getExtra("wifi_state", -1);
        if (extraWifi2State instanceof Integer) {
            this.mWifi2State.set(((Integer) extraWifi2State).intValue());
        }
        HwHiLog.i(TAG, false, "handleWifi2StateChange : new state is %{public}d", new Object[]{Integer.valueOf(this.mWifi2State.get())});
        if (this.mWifi2State.get() == 1 && (message = this.mCacheMsgToReSendWlan1Down) != null) {
            HwHiLog.i(TAG, false, "wifi2 is disable, resend msg: %{public}d", new Object[]{Integer.valueOf(message.what)});
            this.mHwP2pStateMachine.sendMessage(this.mCacheMsgToReSendWlan1Down);
            this.mCacheMsgToReSendWlan1Down = null;
        }
    }

    private boolean shouldShowP2pCollisionToast(Message msg) {
        int uid = msg.sendingUid;
        String currentPkgName = getCurrentPkgName();
        String[] callingPkgNames = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (callingPkgNames == null || callingPkgNames.length == 0 || currentPkgName == null) {
            return false;
        }
        boolean isForeground = false;
        int length = callingPkgNames.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (callingPkgNames[i].equals(currentPkgName)) {
                isForeground = true;
                break;
            } else {
                i++;
            }
        }
        HwHiLog.d(TAG, false, "P2pCollision uid=%{public}d, currentPkgName=%{public}s, isForeground=%{public}s", new Object[]{Integer.valueOf(uid), currentPkgName, Boolean.valueOf(isForeground)});
        return isForeground;
    }

    private String getCurrentPkgName() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return null;
            }
            return tasks.get(0).topActivity.getPackageName();
        } catch (SecurityException e) {
            HwHiLog.d(TAG, false, "SecurityException: get current package name error", new Object[0]);
            return null;
        }
    }

    private void showUserToastIfP2pCollision() {
        Toast.makeText(this.mContext, 33685839, 0).show();
    }

    /* access modifiers changed from: protected */
    public boolean getMagicLinkDeviceFlag() {
        return this.mIsMagicLinkDevice;
    }

    /* access modifiers changed from: protected */
    public void setMagicLinkDeviceFlag(boolean isMagicLinkDevice) {
        this.mIsMagicLinkDevice = isMagicLinkDevice;
        if (!this.mIsMagicLinkDevice) {
            this.mIsLegacyGo = false;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyP2pChannelNumber(int channel) {
        int realChannel = channel;
        if (channel > 13) {
            realChannel = 0;
        }
        WifiCommonUtils.notifyDeviceState("WLAN-P2P", String.valueOf(realChannel), "");
    }

    /* access modifiers changed from: protected */
    public void notifyP2pState(String state) {
        WifiCommonUtils.notifyDeviceState("WLAN-P2P", state, "");
    }

    private boolean configIpAddr(String ifName, String ipAddr, String gateway) {
        HwHiSlog.d(TAG, false, "configIpAddr: %{public}s %{private}s", new Object[]{ifName, StringUtilEx.safeDisplayIpAddress(ipAddr)});
        try {
            this.mNwService.enableIpv6(ifName);
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(ipAddr), 24));
            ifcg.setInterfaceUp();
            this.mNwService.setInterfaceConfig(ifName, ifcg);
            RouteInfo connectedRoute = new RouteInfo(new LinkAddress((Inet4Address) NetworkUtils.numericToInetAddress(ipAddr), 24), null, ifName);
            List<RouteInfo> routes = new ArrayList<>(3);
            routes.add(connectedRoute);
            routes.add(new RouteInfo(null, NetworkUtils.numericToInetAddress(gateway), ifName));
            HwHiLog.e(TAG, false, "add new RouteInfo() gateway:%{private}s iface:%{public}s", new Object[]{StringUtilEx.safeDisplayIpAddress(gateway), ifName});
            this.mNwService.addInterfaceToLocalNetwork(ifName, routes);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            HwHiLog.e(TAG, false, "configIpAddr fail", new Object[0]);
        }
        HwHiSlog.d(TAG, false, "configIpAddr: %{public}s %{private}s* ok", new Object[]{ifName, StringUtilEx.safeDisplayIpAddress(ipAddr)});
        return true;
    }

    private boolean releaseIpAddr(String ifName) {
        if (ifName == null) {
            return false;
        }
        try {
            this.mNwService.disableIpv6(ifName);
            this.mNwService.clearInterfaceAddresses(ifName);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            HwHiLog.e(TAG, false, "Failed to clear addresses or disable IPv6", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public int addScanChannelInTimeout(int channelId, int timeout) {
        int ret = (channelId << 16) + (timeout & TIMEOUT_MASK_OF_DISCOVER);
        logd("discover time " + ret, new Object[0]);
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean allowP2pFind(int uid) {
        boolean isAllowed;
        if (Process.isCoreUid(uid)) {
            return true;
        }
        boolean isBlackApp = isInBlacklistForP2pFind(uid);
        if (isScreenOn()) {
            if (isBlackApp) {
                isAllowed = allowP2pFindByTime(uid);
            } else {
                isAllowed = true;
            }
        } else if (isBlackApp) {
            isAllowed = false;
        } else {
            isAllowed = allowP2pFindByTime(uid);
        }
        if (!isAllowed) {
            HwHiLog.d(TAG, false, "p2p find disallowed, uid:%{public}d", new Object[]{Integer.valueOf(uid)});
        }
        return isAllowed;
    }

    /* access modifiers changed from: protected */
    public synchronized void handleP2pStopFind(int uid) {
        if (this.mP2pFindProcessInfoList != null) {
            if (uid < 0) {
                this.mP2pFindProcessInfoList.clear();
            }
            Iterator<P2pFindProcessInfo> it = this.mP2pFindProcessInfoList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                P2pFindProcessInfo p2pInfo = it.next();
                if (uid == p2pInfo.getUid()) {
                    this.mP2pFindProcessInfoList.remove(p2pInfo);
                    break;
                }
            }
        }
    }

    private boolean isScreenOn() {
        PowerManager powerManager = this.mPowerManager;
        if (powerManager == null || powerManager.isScreenOn()) {
            return true;
        }
        return false;
    }

    private boolean isInBlacklistForP2pFind(int uid) {
        PackageManager pkgMgr;
        Context context = this.mContext;
        if (context == null || (pkgMgr = context.getPackageManager()) == null) {
            return false;
        }
        String pkgName = pkgMgr.getNameForUid(uid);
        for (String black : BLACKLIST_P2P_FIND) {
            if (black.equals(pkgName)) {
                HwHiLog.d(TAG, false, "p2p-find blacklist: %{public}s", new Object[]{pkgName});
                return true;
            }
        }
        return false;
    }

    private synchronized boolean allowP2pFindByTime(int uid) {
        if (this.mP2pFindProcessInfoList == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        this.mP2pFindProcessInfoList.clear();
        this.mP2pFindProcessInfoList.addAll((List) this.mP2pFindProcessInfoList.stream().filter(new Predicate(now) {
            /* class com.android.server.wifi.p2p.$$Lambda$HwWifiP2pService$mTGzbNymvkAXSXqBrM8otRFbQ0 */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HwWifiP2pService.lambda$allowP2pFindByTime$0(this.f$0, (HwWifiP2pService.P2pFindProcessInfo) obj);
            }
        }).collect(Collectors.toList()));
        for (P2pFindProcessInfo p2pInfo : this.mP2pFindProcessInfoList) {
            if (uid == p2pInfo.getUid()) {
                return false;
            }
        }
        this.mP2pFindProcessInfoList.add(new P2pFindProcessInfo(uid, now));
        return true;
    }

    static /* synthetic */ boolean lambda$allowP2pFindByTime$0(long now, P2pFindProcessInfo P2pFindProcessInfo2) {
        return now - P2pFindProcessInfo2.getLastP2pFindTimestamp() <= INTERVAL_DISALLOW_P2P_FIND;
    }

    /* access modifiers changed from: private */
    public class P2pFindProcessInfo {
        private long mLastP2pFindTimestamp;
        private int mUid;

        P2pFindProcessInfo(int uid, long p2pFindTimestamp) {
            this.mUid = uid;
            this.mLastP2pFindTimestamp = p2pFindTimestamp;
        }

        public int getUid() {
            return this.mUid;
        }

        public long getLastP2pFindTimestamp() {
            return this.mLastP2pFindTimestamp;
        }
    }

    /* access modifiers changed from: protected */
    public void processStatistics(Context context, int eventId, int choice) {
        JSONObject eventMsg = new JSONObject();
        try {
            eventMsg.put("choice", choice);
        } catch (JSONException e) {
            loge("processStatistics put error.", new Object[0]);
        }
        Flog.bdReport(context, eventId, eventMsg);
    }

    /* access modifiers changed from: protected */
    public boolean isMiracastDevice(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        String[] tokens = deviceType.split("-");
        try {
            if (tokens.length > 0 && Integer.parseInt(tokens[0]) == 7) {
                logd("As connecting miracast device ,set go_intent = 14 to let it works as GO ", new Object[0]);
                return true;
            }
        } catch (NumberFormatException e) {
            loge("isMiracastDevice: exception happens", new Object[0]);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean wifiIsConnected() {
        WifiManager wifiMgr;
        NetworkInfo wifiInfo;
        Context context = this.mContext;
        if (context == null || (wifiMgr = (WifiManager) context.getSystemService("wifi")) == null || wifiMgr.getWifiState() != 3 || (wifiInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1)) == null) {
            return false;
        }
        logd("wifiIsConnected: " + wifiInfo.isConnected(), new Object[0]);
        return wifiInfo.isConnected();
    }

    /* access modifiers changed from: protected */
    public void sendReinvokePersisentGrouBroadcast(int netId) {
        Intent intent = new Intent("com.huawei.net.wifi.p2p.REINVOKE_PERSISTENT_GROUP_ACTION");
        intent.putExtra("com.huawei.net.wifi.p2p.EXTRA_REINVOKE_NETID", netId);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.permission.REINVOKE_PERSISTENT");
    }

    /* access modifiers changed from: protected */
    public String getSsidPostFix(String deviceName) {
        String ssidPostFix = deviceName;
        if (ssidPostFix == null) {
            return ssidPostFix;
        }
        byte[] ssidPostFixBytes = ssidPostFix.getBytes();
        while (ssidPostFixBytes.length > 22) {
            ssidPostFix = ssidPostFix.substring(0, ssidPostFix.length() - 1);
            ssidPostFixBytes = ssidPostFix.getBytes();
        }
        if (ssidPostFixBytes.length != 14) {
            return ssidPostFix;
        }
        return ssidPostFix + " ";
    }

    /* access modifiers changed from: protected */
    public boolean isWifiP2pForbidden(int msgWhat) {
        boolean isConnect = msgWhat == 139271;
        boolean isDiscoverPeers = msgWhat == 139265;
        boolean isRequestPeers = msgWhat == 139283;
        if (!HwDeviceManager.disallowOp(45) || (!isConnect && !isDiscoverPeers && !isRequestPeers)) {
            return false;
        }
        HwHiSlog.d(TAG, false, "wifiP2P function is forbidden, msg.what = %{public}d", new Object[]{Integer.valueOf(msgWhat)});
        Context context = this.mContext;
        Toast.makeText(context, context.getResources().getString(33686008), 0).show();
        return true;
    }

    private boolean isInDisableDhcpWhiteList(String packageName) {
        for (String whitePackageName : DISABLE_DHCP_WHITE_PACKAGE_NAME_LIST) {
            if (whitePackageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurDisbleDhcpPackageName(int uid) {
        if (!TextUtils.isEmpty(this.mCurDisbleDhcpPackageName) && uid > 10000) {
            if (!this.mCurDisbleDhcpPackageName.equals(this.mContext.getPackageManager().getNameForUid(uid))) {
                this.mCurDisbleDhcpPackageName = "";
            }
        }
    }

    public boolean disableP2pGcDhcp(String packageName, int uid, int type) {
        if (type != 1) {
            HwHiSlog.e(TAG, false, "disableP2pGcDhcp: type is none", new Object[0]);
            return false;
        }
        String realPackageName = packageName;
        if (uid > 10000) {
            String appName = this.mContext.getPackageManager().getNameForUid(uid);
            if (appName == null) {
                HwHiSlog.e(TAG, false, "disableP2pGcDhcp: appName is null", new Object[0]);
                return false;
            }
            realPackageName = appName;
        }
        this.mCurDisbleDhcpPackageName = realPackageName;
        HwHiSlog.i(TAG, false, "disableP2pGcDhcp: appName disable p2p Gc Dhcp %{public}s", new Object[]{this.mCurDisbleDhcpPackageName});
        if (!isInDisableDhcpWhiteList(realPackageName)) {
            HwHiSlog.e(TAG, false, "disableP2pGcDhcp: appName is not in white list", new Object[0]);
            return false;
        }
        this.mDisbleP2pGcDhcpTime = SystemClock.elapsedRealtime();
        synchronized (this.mLock) {
            this.mDisbleGcDhcpList.put(realPackageName, Integer.valueOf(type));
        }
        return true;
    }

    public boolean shouldDisableP2pGcDhcp() {
        String packageName = this.mCurDisbleDhcpPackageName;
        synchronized (this.mLock) {
            if (this.mDisbleGcDhcpList != null && !this.mDisbleGcDhcpList.isEmpty()) {
                if (TextUtils.isEmpty(packageName)) {
                }
            }
            HwHiSlog.e(TAG, false, "shouldDisableP2pGcDhcp: mDisbleGcDhcpList is null, do not need DisableP2pGcDhcp", new Object[0]);
            return false;
        }
        if (SystemClock.elapsedRealtime() - this.mDisbleP2pGcDhcpTime > 10000) {
            HwHiSlog.i(TAG, false, "called shouldDisableP2pGcDhcp after disableP2pGcDhcp too long time", new Object[0]);
            removeDisableP2pGcDhcp(true);
            return false;
        }
        synchronized (this.mLock) {
            if (!this.mDisbleGcDhcpList.containsKey(packageName)) {
                return false;
            }
            HwHiSlog.i(TAG, false, "should Disable P2p Gc Dhcp", new Object[0]);
            return true;
        }
    }

    public void removeDisableP2pGcDhcp(boolean shouldRemoveAll) {
        String packageName = this.mCurDisbleDhcpPackageName;
        synchronized (this.mLock) {
            if (this.mDisbleGcDhcpList == null) {
                HwHiSlog.e(TAG, false, "removeDisableP2pGcDhcp: mDisbleGcDhcpList is null", new Object[0]);
                return;
            }
            if (shouldRemoveAll) {
                this.mDisbleGcDhcpList.clear();
            } else if (!TextUtils.isEmpty(packageName) && this.mDisbleGcDhcpList.containsKey(packageName) && this.mDisbleGcDhcpList.get(packageName).equals(1)) {
                this.mDisbleGcDhcpList.remove(packageName);
                HwHiSlog.i(TAG, false, "removeDisableP2pGcDhcp enter", new Object[0]);
            }
            this.mCurDisbleDhcpPackageName = "";
            this.mDisbleP2pGcDhcpTime = -10000;
        }
    }

    private boolean hasDisableP2pRandomMacPermission(int uid) {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null) {
            return false;
        }
        WifiPermissionsWrapper wifiPermissionsWrapper = wifiInjector.getWifiPermissionsWrapper();
        if (wifiPermissionsWrapper == null) {
            HwHiSlog.i(TAG, false, "wifiPermissionsWrapper is null when check disable p2p random mac permission", new Object[0]);
            return false;
        }
        boolean isDisableP2pRandomMacPermission = wifiPermissionsWrapper.getUidPermission(PERMISSION_DISABLE_P2P_RANDOM_MAC, uid) != -1;
        boolean isAccessWifiStatePermission = wifiPermissionsWrapper.getUidPermission("android.permission.ACCESS_WIFI_STATE", uid) != -1;
        if (!isDisableP2pRandomMacPermission || !isAccessWifiStatePermission) {
            return false;
        }
        return true;
    }

    private boolean isInDisableP2pRandomMacWhiteList(int uid, String packageName) {
        if (uid == 1000) {
            return true;
        }
        for (String whitePackageName : DISABLE_P2P_RANDOM_MAC_WHITE_PACKAGE_NAME_LIST) {
            if (whitePackageName.equals(packageName)) {
                return true;
            }
        }
        HwHiSlog.i(TAG, false, "not in white list to disable p2p random mac", new Object[0]);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean ableToDisableP2pRandomMac(int uid) {
        if (this.mContext == null) {
            return false;
        }
        try {
            if (!this.mContext.getResources().getBoolean(17891594)) {
                HwHiSlog.i(TAG, false, "not support feature: P2P MAC randomization", new Object[0]);
                return false;
            }
            PackageManager pkgMgr = this.mContext.getPackageManager();
            if (pkgMgr == null) {
                return false;
            }
            String pkgName = pkgMgr.getNameForUid(uid);
            if (pkgName == null) {
                HwHiSlog.i(TAG, false, "pkgName is null when get name for uid in ableToDisableP2pRandomMac", new Object[0]);
                return false;
            } else if (hasDisableP2pRandomMacPermission(uid) && isInDisableP2pRandomMacWhiteList(uid, pkgName)) {
                return true;
            } else {
                HwHiSlog.i(TAG, false, "no permission or not in white list to disable p2p random mac", new Object[0]);
                return false;
            }
        } catch (Resources.NotFoundException e) {
            HwHiSlog.w(TAG, false, "not found config_wifi_p2p_mac_randomization_supported", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAbleToSetSinkConfig(int uid) {
        PackageManager pkgMgr;
        if (!IS_TV) {
            loge("setSinkConfig only works on TV.", new Object[0]);
            return false;
        }
        Context context = this.mContext;
        if (context == null || (pkgMgr = context.getPackageManager()) == null) {
            return false;
        }
        String pkgName = pkgMgr.getNameForUid(uid);
        if (pkgName == null) {
            loge("calling package name is null.", new Object[0]);
            return false;
        } else if (isInSetSinkConfigWhiteList(uid, pkgName) && hasSetSinkConfigPermission(uid)) {
            return true;
        } else {
            loge("calling package is not in whitelist or no permission.", new Object[0]);
            return false;
        }
    }

    private boolean isInSetSinkConfigWhiteList(int uid, String packageName) {
        if (uid == 1000) {
            return true;
        }
        for (String whitePackageName : SET_HWSINK_CONFIG_WHITE_PACKAGE_NAME_LIST) {
            if (whitePackageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSetSinkConfigPermission(int uid) {
        WifiPermissionsWrapper wifiPermissionsWrapper;
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null || (wifiPermissionsWrapper = wifiInjector.getWifiPermissionsWrapper()) == null || wifiPermissionsWrapper.getUidPermission(PERMISSION_SET_SINK_CONFIG, uid) == -1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void updateP2pGoCreateStatus(boolean isSuccessed) {
        loge("updateP2pGoCreateStatus for create GO failure", new Object[0]);
        if (isSuccessed) {
            this.mP2pCreateGoFailTimes = 0;
            return;
        }
        this.mP2pCreateGoFailTimes++;
        if (this.mP2pCreateGoFailTimes >= 2) {
            this.mP2pCreateGoFailTimes = 0;
            this.mP2pStateMachine.sendMessage((int) CMD_SELFCURE_GO_CREATE_FAIL);
        }
    }

    public void setWifiP2pListenMode() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector != null) {
            WifiP2pNative wifiNative = wifiInjector.getWifiP2pNative();
            if (wifiNative == null || wifiNative.mHwWifiP2pNativeEx == null) {
                loge("setWifiP2pListenMode wifiP2pNative is null", new Object[0]);
                return;
            }
            logd("setWifiP2pListenMode", new Object[0]);
            wifiNative.mHwWifiP2pNativeEx.deliverP2pData(2, 4, "1");
        }
    }

    public void setStereoAudioWorkingFlag(boolean isActive) {
        WifiRepeater wifiRepeater = this.mWifiRepeater;
        if (wifiRepeater != null) {
            wifiRepeater.setStereoAudioWorkingFlag(isActive);
        }
    }

    public boolean isStereoAudioWorking() {
        WifiRepeater wifiRepeater = this.mWifiRepeater;
        if (wifiRepeater != null) {
            return wifiRepeater.isStereoAudioWorking();
        }
        return false;
    }

    public void setP2pHighPerf(boolean isEnable) {
        if (this.mIsP2pHighPerfEnabled == isEnable) {
            HwHiSlog.i(TAG, false, "high perf mode do not change, ignore cmd, state = %{public}s", new Object[]{String.valueOf(isEnable)});
            return;
        }
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null) {
            HwHiSlog.e(TAG, false, "setP2pHighPerf wifiInjector is null", new Object[0]);
            return;
        }
        WifiNative wifiNative = wifiInjector.getWifiNative();
        if (wifiNative == null) {
            HwHiSlog.e(TAG, false, "setP2pHighPerf wifiNative is null", new Object[0]);
            return;
        }
        HwHiSlog.i(TAG, false, "setP2pHighPerf = %{public}s", new Object[]{String.valueOf(isEnable)});
        if (isEnable) {
            wifiNative.mHwWifiNativeEx.sendCmdToDriver(COMM_IFACE, (int) CMD_SET_P2P_HIGH_PERF, new byte[]{1});
        } else {
            wifiNative.mHwWifiNativeEx.sendCmdToDriver(COMM_IFACE, (int) CMD_SET_P2P_HIGH_PERF, new byte[]{0});
        }
        this.mIsP2pHighPerfEnabled = isEnable;
    }
}
