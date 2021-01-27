package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hdm.HwDeviceManager;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.NetworkInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.IWifiCfgCallback;
import android.net.wifi.IWifiRepeaterConfirmListener;
import android.net.wifi.PPPOEConfig;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiDeviceConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.aware.WifiAwareManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.NetLocationStrategy;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.util.wifi.HwHiLog;
import android.util.wifi.HwHiSlog;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.StateMachine;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.hidata.HwHiDataManager;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.hidata.wavemapping.dataprovider.FrequentLocation;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.MSS.HwMssHandler;
import com.android.server.wifi.MSS.HwMssUtils;
import com.android.server.wifi.aware.WifiAwareService;
import com.android.server.wifi.aware.WifiAwareServiceImpl;
import com.android.server.wifi.aware.WifiAwareStateManager;
import com.android.server.wifi.cast.CastOptChr;
import com.android.server.wifi.cast.CastOptManager;
import com.android.server.wifi.cast.P2pSharing.P2pSharingDispatcher;
import com.android.server.wifi.dc.DcController;
import com.android.server.wifi.dc.DcUtils;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;
import com.android.server.wifi.wifipro.HwWifiProServiceProxy;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconServiceCallback;
import com.huawei.android.server.wifi.cast.avsync.AvSyncLatencyInfo;
import com.huawei.server.hwdfu.SystemServiceUtil;
import com.huawei.utils.reflect.EasyInvokeFactory;
import com.mediatek.ims.impl.IMtkMapconService;
import com.mediatek.ims.impl.IMtkMapconServiceCallback;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwWifiService extends WifiServiceImpl {
    private static final String ACCESS_WIFI_FILTER_PERMISSION = "com.huawei.wifi.permission.ACCESS_FILTER";
    private static final String ACCESS_WIFI_PERMISSION = "huawei.permission.ACCESS_WIFI_SERVICE";
    private static final String[] ACCESS_WIFI_WHITELIST = {"com.huawei.smarthome"};
    private static final String ACTION_VOWIFI_STARTED = "com.hisi.vowifi.started";
    private static final String ACTION_VOWIFI_STARTED_MTK = "com.huawei.vowifi.started";
    private static final int AWARE_INFO_LEN = 128;
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static final int BOTH_2G_AND_5G = 2;
    private static final int CHIP_PLATFORM_HISI = 1;
    private static final int CHIP_PLATFORM_MTK = 2;
    private static final int CODE_BIND_MPLINK = 5002;
    private static final int CODE_CONFIG_NSP_SLAVE_WIFI = 4203;
    private static final int CODE_CONFIRM_WIFI_REPEATER = 1022;
    private static final int CODE_CTRL_HW_WIFI_NETWORK = 1017;
    private static final int CODE_DBAC_GET_RECOMMEND_CHANNEL = 4117;
    private static final int CODE_DBAC_GET_SELF_WIFI_INFO = 4113;
    private static final int CODE_DBAC_REGISTER_CALLBACK = 4115;
    private static final int CODE_DBAC_SET_PEER_WIFI_INFO = 4114;
    private static final int CODE_DBAC_UNREGISTER_CALLBACK = 4116;
    private static final int CODE_DISABLE_RX_FILTER = 3021;
    private static final int CODE_ENABLE_HILINK_HANDSHAKE = 2001;
    private static final int CODE_ENABLE_RX_FILTER = 3022;
    private static final int CODE_ENABLE_WIFICHIP_CHECK = 4021;
    private static final int CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P = 2006;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_CONNECTION_RAW_PSK = 2002;
    private static final int CODE_GET_PPPOE_INFO_CONFIG = 1004;
    private static final int CODE_GET_RSDB_SUPPORTED_MODE = 2008;
    private static final int CODE_GET_SINGNAL_INFO = 1011;
    private static final int CODE_GET_SLAVE_WIFI_INFO = 4204;
    private static final int CODE_GET_SLAVE_WIFI_LINKPROPERTY = 4205;
    private static final int CODE_GET_SLAVE_WIFI_NETWORK_IFNO = 4206;
    private static final int CODE_GET_SOFTAP_BANDWIDTH = 4012;
    private static final int CODE_GET_SOFTAP_CHANNEL_LIST = 1009;
    private static final int CODE_GET_VOWIFI_DETECT_MODE = 1013;
    private static final int CODE_GET_VOWIFI_DETECT_PERIOD = 1015;
    private static final int CODE_GET_WIFI_AWARE = 4108;
    private static final int CODE_GET_WIFI_MODE = 4112;
    private static final int CODE_GET_WIFI_REPEATER_MODE = 1021;
    private static final int CODE_GET_WPA_SUPP_CONFIG = 1001;
    private static final int CODE_HANDLE_HID2D_PARAMS = 4105;
    private static final int CODE_IS_BG_LIMIT_ALLOWED = 3008;
    private static final int CODE_IS_FEATURE_SUPPORTED = 4011;
    private static final int CODE_IS_IN_MPLINK_STATE = 5003;
    private static final int CODE_IS_SUPPORT_DUAL_WIFI = 4202;
    private static final int CODE_IS_SUPPORT_VOWIFI_DETECT = 1016;
    private static final int CODE_PROXY_WIFI_LOCK = 3009;
    private static final int CODE_QUERY_ARP = 4120;
    private static final int CODE_QUERY_CSI = 4118;
    private static final int CODE_QUERY_SNIFFER = 4119;
    private static final int CODE_REPORT_SPEED_RESULT = 4103;
    private static final int CODE_REQUEST_FRESH_WHITE_LIST = 2007;
    private static final int CODE_REQUEST_WIFI_AWARE = 4106;
    private static final int CODE_REQUEST_WIFI_ENABLE = 2004;
    private static final int CODE_RESTRICT_WIFI_SCAN = 4001;
    private static final int CODE_ROAM_TO_NETWORK = 4122;
    private static final int CODE_SET_FEM_TXPOWER = 4013;
    private static final int CODE_SET_HIGH_PRIORITY_TRANSMIT = 4104;
    private static final int CODE_SET_PERFORMANCE_MODE = 4102;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_VOWIFI_DETECT_MODE = 1012;
    private static final int CODE_SET_VOWIFI_DETECT_PERIOD = 1014;
    private static final int CODE_SET_WIFI_ANTSET = 3007;
    private static final int CODE_SET_WIFI_AP_EVALUATE_ENABLED = 1010;
    private static final int CODE_SET_WIFI_AWARE = 4107;
    private static final int CODE_SET_WIFI_LOW_LATENCY_MODE = 4121;
    private static final int CODE_SET_WIFI_MODE = 4111;
    private static final int CODE_SET_WIFI_TXPOWER = 2005;
    private static final int CODE_START_PPPOE_CONFIG = 1002;
    private static final int CODE_START_WIFI_KEEP_ALIVE = 3023;
    private static final int CODE_STOP_PPPOE_CONFIG = 1003;
    private static final int CODE_STOP_WIFI_KEEP_ALIVE = 3024;
    private static final int CODE_UPDATE_APP_EXPERIENCE_STATUS = 3006;
    private static final int CODE_UPDATE_APP_RUNNING_STATUS = 3005;
    private static final int CODE_UPDATE_LIMIT_SPEED_STATUS = 3025;
    private static final int CODE_UPDATE_WM_FREQ_LOC = 4002;
    private static final int CODE_USER_HANDOVER_WIFI = 1008;
    private static final int CODE_WIFI_DC_CONNECT = 4014;
    private static final int CODE_WIFI_DC_CONNECT_Z = 5004;
    private static final int CODE_WIFI_DC_DISCONNECT = 4015;
    private static final int CODE_WIFI_IS_DC_ACTIVE = 5005;
    private static final int CODE_WIFI_QOE_EVALUATE = 3003;
    private static final int CODE_WIFI_QOE_START_MONITOR = 3001;
    private static final int CODE_WIFI_QOE_STOP_MONITOR = 3002;
    private static final int CODE_WIFI_QOE_UPDATE_STATUS = 3004;
    private static final String CONF_FILE_NAME = "/system/etc/xml/wifi_factory_mac_whitelist.xml";
    private static final int DEFAULT_VALUE = 0;
    private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
    private static final int FACTORY_MAC_NO_CHECK_VERSION = -1;
    private static final int[] FREQUENCYS = {2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472};
    private static final String GNSS_LOCATION_FIX_STATUS = "GNSS_LOCATION_FIX_STATUS";
    private static final boolean IS_DEBUG_ON = true;
    private static final boolean IS_THREE_STATE_SUPPORT = SystemProperties.getBoolean("hw_mc.wifi.three_state", (boolean) IS_DEBUG_ON);
    private static final String KEY_APK_NAME = "Apk";
    private static final String KEY_BAND_CHANGE = "Band";
    private static final String KEY_BLACKLIST_CHANGE = "IsBlacklistChanged";
    private static final String KEY_CHANNEL_CHANGE = "Channel";
    private static final String KEY_PASSWORD_CHANGE = "IsPwdChanged";
    private static final String KEY_SECURITY_TYPE = "SecurityType";
    private static final String KEY_SSID_CHANGE = "IsSsidChanged";
    private static final long LOG_SCAN_RESULTS_INTERVAL_MS = 3000;
    private static final int MAPCON_SERVICE_SHUTDOWN_TIMEOUT = 5000;
    private static final int MSG_AIRPLANE_TOGGLED_MAPCON_TIMEOUT = 1;
    private static final int MSG_DISABLE_WIFI_MAPCON_TIMEOUT = 2;
    private static final int MSG_FORGET_NETWORK_MAPCON_TIMEOUT = 0;
    private static final int ONLY_2G = 1;
    private static final String OWE_TRANSITION_IN_CAPABILITY = "OWE_TRANSITION";
    private static final String PERMISSION_SET_WIFI_MODE_ASSISTANT = "com.huawei.permission.ASSOCIATE_ASSISTANT_SET_WIFI_MODE";
    private static final String PERMISSION_SET_WIFI_MODE_HWSHARE = "com.huawei.permission.HWSHARE_SET_WIFI_MODE";
    private static final String PERMISSION_SET_WIFI_MODE_MIRACAST = "com.huawei.permission.MIRACAST_SET_WIFI_MODE";
    private static final String PERMISSION_SET_WIFI_MODE_WAUDIO = "com.huawei.permission.WAUDIOSVC_SET_WIFI_MODE";
    private static final String PG_AR_STATE_ACTION = "com.huawei.intent.action.PG_AR_STATE_ACTION";
    private static final String PG_RECEIVER_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final String PMFR_IN_CAPABILITY = "PMFR";
    private static final String PPPOE_TAG = "PPPOEWIFIService";
    private static final String PROCESS_BD = "com.baidu.map.location";
    private static final String PROCESS_GD = "com.amap.android.ams";
    private static final String QTTFF_WIFI_SCAN_ENABLED = "qttff_wifi_scan_enabled";
    private static final int QTTFF_WIFI_SCAN_INTERVAL_MS = 5000;
    private static final int QUERY_AWARE_AVAILABLE = 2;
    private static final int REQUEST_AWARE_DISABLE = 3;
    private static final int REQUEST_AWARE_START = 1;
    private static final int REQUEST_AWARE_STOP = 2;
    private static final int REQUEST_ID_DISABLE_AWARE_USAGE = 6;
    private static final int REQUEST_ID_DISABLE_OTHER_ONLY = 4;
    private static final int REQUEST_ID_ENABLE_AWARE_USAGE = 5;
    private static final int REQUEST_ID_ENABLE_OTHER_ONLY = 3;
    private static final int REQUEST_ID_START_AWARE_SERVICE = 1;
    private static final int REQUEST_ID_STOP_AWARE_SERVICE = 2;
    private static final int SCANRESULTS_COUNT_MAX = 200;
    private static final String SECURITY_NO_CHANGE = "NoChange";
    private static final int SERVER_CODE_CONTROL_HIDATA_OPTIMIZE = 3031;
    public static final boolean SHOULD_NETWORK_SHARING_INTEGRATION;
    private static final String TAG = "HwWifiService";
    private static final int TYPE_CHANNELS_BW160 = 1;
    private static final String VOWIFI_WIFI_DETECT_PERMISSION = "com.huawei.permission.VOWIFI_WIFI_DETECT";
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    private static final int WIFISCANSTRATEGY_ALLOWABLE = 0;
    private static final int WIFISCANSTRATEGY_FORBIDDEN = -1;
    private static final String[] WIFI_CAST_OPT_PACKAGE_LIST = {"com.huawei.pcassistant"};
    private static final Object WIFI_LOCK = new Object();
    private static final String[] WIFI_LOW_LATENCY_MODE_WHITE_PACKAGE_LIST = {"com.huawei.nearby"};
    private static final int WIFI_MODE_ASSOCIATE_ASSISTANTE = 1002;
    private static final int WIFI_MODE_ASSOCIATE_ASSISTANTE_SUCC = 1003;
    private static final int WIFI_MODE_CAST_PLUS_START = 3003;
    private static final int WIFI_MODE_CAST_PLUS_STOP = 3004;
    private static final int WIFI_MODE_HWSHARE_INTERMEDIATE_STATE = 1007;
    private static final int WIFI_MODE_HWSHARE_LARGE_FILE = 1005;
    private static final int WIFI_MODE_HWSHARE_LARGE_FILE_SUCC = 1006;
    private static final int WIFI_MODE_HWSHARE_WORKING = 1008;
    private static final int WIFI_MODE_MIRACAST_START = 3001;
    private static final int WIFI_MODE_MIRACAST_STOP = 3002;
    private static final int WIFI_MODE_P2P_HIGH_PERF_START = 2000;
    private static final int WIFI_MODE_P2P_HIGH_PERF_STOP = 2001;
    private static final int WIFI_MODE_SET_LISTEN_MODE = 1004;
    private static final int WIFI_MODE_STEREO_AUDIO_WORKING = 1009;
    private static final String[] WIFI_MODE_WHITE_PACKAGE_LIST = {"com.huawei.associateassistant", "com.huawei.pcassistant", "com.huawei.android.instantshare", "com.huawei.nearby", "com.huawei.waudio", "com.huawei.android.airsharing", "com.huawei.hiview", "com.hisilicon.miracast"};
    private static final String XML_TAG_MAC_WHITELIST = "mac_whitelist";
    private static final String XML_TAG_PACKAGE = "package";
    private static final String XML_TAG_VERSION = "version_number";
    private static final String XML_TAG_VERSION_MAX = "version_max";
    private static final String XML_TAG_VERSION_MIN = "version_min";
    private static HashSet<String> restrictWifiScanPkgSet = new HashSet<>();
    private static WifiServiceUtils wifiServiceUtils = EasyInvokeFactory.getInvokeUtils(WifiServiceUtils.class);
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private final ServiceConnection conn;
    private boolean isPppoe;
    private long lastScanResultsAvailableTime;
    private final ActivityManager mActivityManager;
    private final IMapconServiceCallback mAirPlaneCallback;
    private final AppOpsManager mAppOps;
    private SystemService mAwareService;
    private final IMapconServiceCallback mCallback;
    private int mChipPlatform;
    private final Clock mClock;
    private Context mContext;
    Map<String, FactoryMacWhiteList> mFactoryMacWhiteListMap = new HashMap();
    private List<HwFilterLock> mFilterLockList;
    private final Object mFilterSynchronizeLock;
    private Message mForgetNetworkMsg;
    private boolean mHasScanned;
    private IHwBehaviorCollectManager mHwBehaviorManager;
    private HwWifi2Manager mHwWifi2Manager;
    private HwWifiCHRService mHwWifiChrService;
    private HwWifiProServiceManager mHwWifiProServiceManager;
    private HwWifiProServiceProxy mHwWifiProServiceProxy;
    private boolean mIsAbsoluteRest;
    private volatile boolean mIsRxFilterDisabled;
    private boolean mIsVowifiServiceOn;
    private long mLastLogScanResultsTime;
    private String mMacFilterRecord;
    private Handler mMapconHandler;
    private HandlerThread mMapconHandlerTread;
    private IMapconService mMapconService;
    private final IMtkMapconServiceCallback mMtkAirPlaneCallback;
    private final IMtkMapconServiceCallback mMtkCallback;
    private IMtkMapconService mMtkMapconService;
    private Handler mNetworkResetHandler;
    private int mPluggedType;
    private PowerManager mPowerManager;
    private PPPOEStateMachine mPppoeStateMachine;
    private int mWifiMode;
    private final ArraySet<String> mWifiScanBlacklist;

    static {
        boolean z = false;
        if (HwSoftApManager.shouldUseLiteUi() && SystemProperties.getBoolean("ro.config.hw_wifibridge", false) && SystemProperties.getBoolean("ro.feature.mobile_network_sharing_integration", (boolean) IS_DEBUG_ON)) {
            z = true;
        }
        SHOULD_NETWORK_SHARING_INTEGRATION = z;
    }

    public HwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        super(context, wifiInjector, asyncChannel);
        this.isPppoe = SystemProperties.getInt("ro.config.pppoe_enable", 0) != 1 ? false : IS_DEBUG_ON;
        this.lastScanResultsAvailableTime = 0;
        this.mWifiScanBlacklist = new ArraySet<>();
        this.mIsAbsoluteRest = false;
        this.mHasScanned = false;
        this.mHwWifiChrService = null;
        this.mMacFilterRecord = "";
        this.mWifiMode = 0;
        this.mMapconService = null;
        this.mMtkMapconService = null;
        this.mAwareService = null;
        this.mFilterLockList = new ArrayList();
        this.mIsRxFilterDisabled = false;
        this.mFilterSynchronizeLock = new Object();
        this.mLastLogScanResultsTime = 0;
        this.conn = new ServiceConnection() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass1 */

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                HwHiLog.d(HwWifiService.TAG, false, "onServiceDisconnected,IMapconService", new Object[0]);
                HwWifiService.this.mMapconService = null;
                if (HwWifiService.this.mChipPlatform == 2) {
                    HwWifiService.this.onVoWifiCloseForServiceDisconnect();
                    HwWifiService.this.mMtkMapconService = null;
                }
                HwWifiService.this.mIsVowifiServiceOn = false;
                HwWifiService.this.mMapconHandlerTread.quit();
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                HwHiLog.d(HwWifiService.TAG, false, "onServiceConnected,IMapconService", new Object[0]);
                HwWifiService.this.mMapconHandlerTread = new HandlerThread("MapconHandler");
                HwHiLog.d(HwWifiService.TAG, false, "onServiceConnected,IMapconService and chip platform is %{public}d ", new Object[]{Integer.valueOf(HwWifiService.this.mChipPlatform)});
                if (HwWifiService.this.mChipPlatform == 1) {
                    HwWifiService.this.mMapconService = IMapconService.Stub.asInterface(service);
                } else if (HwWifiService.this.mChipPlatform == 2) {
                    HwWifiService.this.mMtkMapconService = IMtkMapconService.Stub.asInterface(service);
                } else {
                    HwHiLog.d(HwWifiService.TAG, false, "onServiceConnected,chip platform is not hisi or mtk ", new Object[0]);
                    return;
                }
                HwWifiService.this.mMapconHandlerTread.start();
                HwWifiService hwWifiService = HwWifiService.this;
                hwWifiService.mMapconHandler = new Handler(hwWifiService.mMapconHandlerTread.getLooper()) {
                    /* class com.android.server.wifi.HwWifiService.AnonymousClass1.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        HwHiLog.d(HwWifiService.TAG, false, "handle TimeoutMessage,msg:%{public}d", new Object[]{Integer.valueOf(msg.what)});
                        WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                        int i = msg.what;
                        if (i == 0) {
                            HwWifiService.this.mClientModeImpl.sendMessage(Message.obtain((Message) msg.obj));
                        } else if (i != 1) {
                            if (i == 2 && controller != null) {
                                controller.sendMessage(155656);
                            }
                        } else if (controller != null) {
                            controller.sendMessage(155657);
                        }
                    }
                };
                HwWifiService.this.mIsVowifiServiceOn = HwWifiService.IS_DEBUG_ON;
            }
        };
        this.mAirPlaneCallback = new IMapconServiceCallback.Stub() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass2 */

            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForAirplaneToggled();
            }
        };
        this.mMtkAirPlaneCallback = new IMtkMapconServiceCallback.Stub() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass3 */

            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForAirplaneToggled();
            }
        };
        this.mCallback = new IMapconServiceCallback.Stub() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass4 */

            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForToggled();
            }
        };
        this.mMtkCallback = new IMtkMapconServiceCallback.Stub() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass5 */

            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForToggled();
            }
        };
        this.mNetworkResetHandler = new Handler();
        this.mContext = context;
        parseFactoryMacWhiteListFile(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.isPppoe) {
            this.mPppoeStateMachine = new PPPOEStateMachine(this.mContext, PPPOE_TAG);
            this.mPppoeStateMachine.start();
        }
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        this.mClock = wifiInjector.getClock();
        registerFilterAction(new IntentFilter());
        this.mHwWifi2Manager = new HwWifi2Manager(this.mContext);
    }

    private void registerFilterAction(IntentFilter filter) {
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "android.net.wifi.SCAN_RESULTS".equals(intent.getAction()) && intent.getBooleanExtra("resultsUpdated", false)) {
                    HwWifiService hwWifiService = HwWifiService.this;
                    hwWifiService.lastScanResultsAvailableTime = hwWifiService.mClock.getElapsedSinceBootMillis();
                }
            }
        }, filter);
        loadWifiScanBlacklist();
        BackgroundAppScanManager.getInstance().registerBlackListChangeListener(new BlacklistListener() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass7 */

            public void onBlacklistChange(List<String> list) {
                HwWifiService.this.updateWifiScanblacklist();
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass8 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    HwWifiService.this.mIsAbsoluteRest = intent.getBooleanExtra("stationary", false);
                    HwHiLog.d(HwWifiService.TAG, false, "mIsAbsoluteRest = %{public}s", new Object[]{String.valueOf(HwWifiService.this.mIsAbsoluteRest)});
                    if (HwWifiService.this.mIsAbsoluteRest) {
                        HwWifiService.this.mHasScanned = false;
                    }
                }
            }
        }, new IntentFilter(PG_AR_STATE_ACTION), PG_RECEIVER_PERMISSION, null);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass9 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwWifiService.this.updatePluggedType(intent);
            }
        }, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePluggedType(Intent intent) {
        if (intent != null) {
            this.mPluggedType = intent.getIntExtra("plugged", 0);
            if (this.mPluggedType == 0 && (this.mClientModeImpl instanceof HwWifiStateMachine) && this.mClientModeImpl.getChargingState()) {
                this.mPluggedType = 2;
            }
            HwHiLog.d(TAG, false, "mPluggedType = %{public}d", new Object[]{Integer.valueOf(this.mPluggedType)});
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    /* access modifiers changed from: protected */
    public boolean enforceStopScanSreenOff() {
        if (this.mPowerManager.isScreenOn() || "com.huawei.ca".equals(getAppName(Binder.getCallingPid()))) {
            return false;
        }
        HwHiSlog.i(TAG, false, "Screen is off, %{public}s startScan is skipped.", new Object[]{getAppName(Binder.getCallingPid())});
        return IS_DEBUG_ON;
    }

    private void restrictWifiScan(List<String> pkgs, boolean isRestrict) {
        if (isRestrict) {
            restrictWifiScanPkgSet.addAll(pkgs);
        } else if (pkgs == null) {
            restrictWifiScanPkgSet.clear();
        } else {
            restrictWifiScanPkgSet.removeAll(pkgs);
        }
    }

    /* access modifiers changed from: protected */
    public boolean restrictWifiScanRequest(String packageName) {
        if (restrictWifiScanPkgSet.contains(packageName)) {
            return IS_DEBUG_ON;
        }
        return false;
    }

    private boolean isInSetWifiModeWhiteList(String packageName, int mode) {
        for (String whitePackageName : WIFI_MODE_WHITE_PACKAGE_LIST) {
            if (whitePackageName.equals(packageName)) {
                if (mode == 0) {
                    return IS_DEBUG_ON;
                } else {
                    if (mode != 2000 && mode != 2001) {
                        switch (mode) {
                            case 1002:
                            case 1003:
                                if (!"com.huawei.associateassistant".equals(packageName)) {
                                    return false;
                                }
                                enforceCallerPermission(PERMISSION_SET_WIFI_MODE_ASSISTANT);
                                return IS_DEBUG_ON;
                            case 1004:
                            case 1009:
                                return isPackagePermission(packageName, mode);
                            case 1005:
                            case 1006:
                                if (!("com.huawei.android.instantshare".equals(packageName) || "com.huawei.nearby".equals(packageName))) {
                                    return false;
                                }
                                enforceCallerPermission(PERMISSION_SET_WIFI_MODE_HWSHARE);
                                return IS_DEBUG_ON;
                            case 1007:
                            case HwWifi2Manager.CLOSE_WIFI2_WIFI2_DISCONNECTED /* 1008 */:
                                if (!("com.huawei.android.instantshare".equals(packageName) || "com.huawei.nearby".equals(packageName) || "com.huawei.hiview".equals(packageName))) {
                                    return false;
                                }
                                enforceCallerPermission(PERMISSION_SET_WIFI_MODE_HWSHARE);
                                return IS_DEBUG_ON;
                            default:
                                switch (mode) {
                                    case 3001:
                                    case 3002:
                                    case 3003:
                                    case 3004:
                                        return isPackagePermission(packageName, mode);
                                    default:
                                        HwHiSlog.i(TAG, false, "%{public}d has no permission", new Object[]{Integer.valueOf(mode)});
                                        return false;
                                }
                        }
                    } else if (!"com.huawei.android.instantshare".equals(packageName) && !"com.huawei.nearby".equals(packageName)) {
                        return false;
                    } else {
                        enforceCallerPermission(PERMISSION_SET_WIFI_MODE_HWSHARE);
                        return IS_DEBUG_ON;
                    }
                }
            }
        }
        HwHiSlog.i(TAG, false, "%{public}s not in white list to set wifi mode", new Object[]{packageName});
        return false;
    }

    private boolean isPackagePermission(String packageName, int mode) {
        if (mode == 1004 && ("com.huawei.pcassistant".equals(packageName) || "com.huawei.android.airsharing".equals(packageName))) {
            enforceCallerPermission(PERMISSION_SET_WIFI_MODE_ASSISTANT);
            return IS_DEBUG_ON;
        } else if (mode == 1009 && "com.huawei.waudio".equals(packageName)) {
            enforceCallerPermission(PERMISSION_SET_WIFI_MODE_WAUDIO);
            return IS_DEBUG_ON;
        } else if ((mode == 3001 || mode == 3002) && ("com.huawei.android.airsharing".equals(packageName) || "com.hisilicon.miracast".equals(packageName) || "com.huawei.associateassistant".equals(packageName))) {
            enforceCallerPermission(PERMISSION_SET_WIFI_MODE_MIRACAST);
            return IS_DEBUG_ON;
        } else if (mode != 3003 && mode != 3004) {
            return false;
        } else {
            if (!"com.huawei.android.airsharing".equals(packageName) && !"com.hisilicon.miracast".equals(packageName)) {
                return false;
            }
            enforceCallerPermission(PERMISSION_SET_WIFI_MODE_MIRACAST);
            return IS_DEBUG_ON;
        }
    }

    private boolean setThreeStateMode(String packageName, HwWifiStateMachine hwWifiStateMachine) {
        if (!IS_THREE_STATE_SUPPORT) {
            HwHiSlog.e(TAG, false, "setThreeStateMode: wifi is not disabled", new Object[0]);
            this.mWifiMode = -1;
            return false;
        } else if (hwWifiStateMachine.setWifiMode(packageName, 63)) {
            hwWifiStateMachine.sendAssistantTimeoutMessage();
            return IS_DEBUG_ON;
        } else {
            this.mWifiMode = -1;
            return false;
        }
    }

    private boolean isWifiModeHandled(String packageName, int mode) {
        if (!(this.mClientModeImpl instanceof HwWifiStateMachine)) {
            HwHiSlog.i(TAG, false, "mClientModeImpl is not a instanceof HwWifiStateMachine", new Object[0]);
            return false;
        }
        HwWifiStateMachine hwWifiStateMachine = (HwWifiStateMachine) this.mClientModeImpl;
        if (mode == 0) {
            HwHiSlog.i(TAG, false, "WIFI_MODE_RESET", new Object[0]);
            this.mWifiMode = mode;
            return hwWifiStateMachine.setWifiMode(packageName, 0);
        } else if (mode == 2000) {
            hwWifiStateMachine.setP2pHighPerf(IS_DEBUG_ON);
            return IS_DEBUG_ON;
        } else if (mode != 2001) {
            switch (mode) {
                case 1002:
                    return hasAssocAssisHandled(packageName, hwWifiStateMachine);
                case 1003:
                case 1006:
                    hwWifiStateMachine.removeAssistantTimeoutMessage();
                    return IS_DEBUG_ON;
                case 1004:
                    hwWifiStateMachine.setWifiP2pListenMode();
                    return IS_DEBUG_ON;
                case 1005:
                    return hasHwShareLargeFileHandled(packageName, hwWifiStateMachine);
                case 1007:
                    this.mWifiMode = 1007;
                    return setThreeStateMode(packageName, hwWifiStateMachine);
                case HwWifi2Manager.CLOSE_WIFI2_WIFI2_DISCONNECTED /* 1008 */:
                    if (this.mWifiMode != 1007) {
                        return false;
                    }
                    hwWifiStateMachine.removeAssistantTimeoutMessage();
                    return IS_DEBUG_ON;
                case 1009:
                    hwWifiStateMachine.setStereoAudioWorkingFlag(IS_DEBUG_ON);
                    return IS_DEBUG_ON;
                default:
                    switch (mode) {
                        case 3001:
                        case 3003:
                            handleCastStart(hwWifiStateMachine, mode);
                            return IS_DEBUG_ON;
                        case 3002:
                        case 3004:
                            handleCastStop(hwWifiStateMachine);
                            return IS_DEBUG_ON;
                        default:
                            return false;
                    }
            }
        } else {
            hwWifiStateMachine.setP2pHighPerf(false);
            return IS_DEBUG_ON;
        }
    }

    private boolean hasAssocAssisHandled(String packageName, HwWifiStateMachine hwWifiStateMachine) {
        if (!hwWifiStateMachine.setWifiMode(packageName, 3)) {
            return false;
        }
        hwWifiStateMachine.sendAssistantTimeoutMessage();
        return IS_DEBUG_ON;
    }

    private boolean hasHwShareLargeFileHandled(String packageName, HwWifiStateMachine hwWifiStateMachine) {
        if (!hwWifiStateMachine.setWifiMode(packageName, 7)) {
            return false;
        }
        hwWifiStateMachine.sendAssistantTimeoutMessage();
        return IS_DEBUG_ON;
    }

    private void handleCastStart(HwWifiStateMachine hwWifiStateMachine, int mode) {
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.setTriggerType(1);
        }
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager != null) {
            castOptManager.triggerToRecognizeCastType(mode);
        }
        setCastOptParam(IS_DEBUG_ON, hwWifiStateMachine);
        if (castOptManager != null && castOptManager.isP2pGroupOwner()) {
            P2pSharingDispatcher.getInstance().setUp(this.mContext, IS_DEBUG_ON);
        }
    }

    private void handleCastStop(HwWifiStateMachine hwWifiStateMachine) {
        setCastOptParam(false, hwWifiStateMachine);
    }

    private void setCastOptParam(boolean isCastOptScenes, HwWifiStateMachine hwWifiStateMachine) {
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager != null) {
            castOptManager.setCastOptScenes(isCastOptScenes);
        }
        hwWifiStateMachine.setIgnoreScanInCastScene(isCastOptScenes);
    }

    private byte[] getAwareAbility() {
        byte[] awareAbility = new byte[128];
        if (HwMssUtils.is1105()) {
            awareAbility[0] = 1;
        } else {
            awareAbility[0] = 0;
        }
        return awareAbility;
    }

    private synchronized void startAwareService() {
        if (this.mAwareService != null) {
            HwHiLog.e(TAG, false, "WifiAwareService already started", new Object[0]);
            return;
        }
        SystemService awareService = ((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).startService("com.android.server.wifi.aware.WifiAwareService");
        HwHiLog.d(TAG, false, "Start WifiAwareService onBootPhase", new Object[0]);
        awareService.onBootPhase(500);
        awareService.onBootPhase(1000);
        this.mAwareService = awareService;
    }

    private synchronized void stopAwareService() {
        if (this.mAwareService != null) {
            HwHiLog.d(TAG, false, "Stop WifiAwareService", new Object[0]);
            ((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).stopService(this.mAwareService);
            SystemServiceUtil.removeBinderService("wifiaware", false);
            this.mAwareService = null;
        } else {
            HwHiLog.e(TAG, false, "WifiAwareService already stopped", new Object[0]);
        }
    }

    private synchronized void disableAwareService(Object stateManager) {
        if (stateManager == null) {
            HwHiLog.e(TAG, false, "aware statemanager is null", new Object[0]);
            return;
        }
        try {
            Field awareUsageEnabled = WifiAwareStateManager.class.getDeclaredField("mUsageEnabled");
            awareUsageEnabled.setAccessible(IS_DEBUG_ON);
            awareUsageEnabled.set(stateManager, Boolean.valueOf((boolean) IS_DEBUG_ON));
            WifiAwareStateManager.class.getMethod("disableUsage", new Class[0]).invoke(stateManager, new Object[0]);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            HwHiLog.e(TAG, false, "disableAwareService Exception", new Object[0]);
        }
    }

    private synchronized void destroyAwareService(Object stateManager) {
        if (stateManager == null) {
            HwHiLog.e(TAG, false, "aware statemanager is null", new Object[0]);
            return;
        }
        try {
            disableAwareService(stateManager);
            Field stateMachine = WifiAwareStateManager.class.getDeclaredField("mSm");
            stateMachine.setAccessible(IS_DEBUG_ON);
            Object awareStateManager = stateMachine.get(stateManager);
            if (awareStateManager instanceof StateMachine) {
                ((StateMachine) awareStateManager).quitNow();
            }
            WifiInjector wifiInjector = WifiInjector.getInstance();
            wifiInjector.getWifiAwareHandlerThread().quit();
            Field wifiAwareHandlerThread = WifiInjector.class.getDeclaredField("mWifiAwareHandlerThread");
            wifiAwareHandlerThread.setAccessible(IS_DEBUG_ON);
            wifiAwareHandlerThread.set(wifiInjector, null);
            stopAwareService();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLog.e(TAG, false, "destroyAwareService Exception", new Object[0]);
        }
    }

    private synchronized void requestWifiAware(int id) {
        if (this.mAwareService == null) {
            if (id == 5) {
                startAwareService();
            }
            return;
        }
        WifiAwareManager awareManager = (WifiAwareManager) this.mContext.getSystemService("wifiaware");
        if (awareManager != null) {
            HwHiLog.d(TAG, false, "before when AwareService is available: " + awareManager.isAvailable(), new Object[0]);
        } else {
            HwHiLog.e(TAG, false, "Can not get wifi aware service", new Object[0]);
        }
        try {
            Field field = WifiAwareService.class.getDeclaredField("mImpl");
            field.setAccessible(IS_DEBUG_ON);
            Object awareImpl = field.get(this.mAwareService);
            Field field2 = WifiAwareServiceImpl.class.getDeclaredField("mStateManager");
            field2.setAccessible(IS_DEBUG_ON);
            Object awareStateManager = field2.get(awareImpl);
            Field awareUsageEnabled = WifiAwareStateManager.class.getDeclaredField("mUsageEnabled");
            awareUsageEnabled.setAccessible(IS_DEBUG_ON);
            if (id == 4) {
                awareUsageEnabled.set(awareStateManager, false);
            } else if (id == 5) {
                WifiAwareStateManager.class.getMethod("enableUsage", new Class[0]).invoke(awareStateManager, new Object[0]);
            } else if (id == 6) {
                disableAwareService(awareStateManager);
            } else if (id == 2) {
                destroyAwareService(awareStateManager);
            } else {
                HwHiLog.e(TAG, false, "unsupported operation id: %{public}d", new Object[]{Integer.valueOf(id)});
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            HwHiLog.e(TAG, false, "Exception, %{public}s" + e.getMessage(), new Object[0]);
        }
        WifiAwareManager awareManager2 = (WifiAwareManager) this.mContext.getSystemService("wifiaware");
        if (awareManager2 != null) {
            HwHiLog.d(TAG, false, "after when AwareService is available: " + awareManager2.isAvailable(), new Object[0]);
        } else {
            HwHiLog.e(TAG, false, "Can not get wifi aware service", new Object[0]);
        }
    }

    private boolean isWifiAwareSet(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            return false;
        }
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "isWifiAwareSet, is not systemapp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        HwHiLog.e(TAG, false, "isWifiAwareSet not support the query", new Object[0]);
        reply.writeNoException();
        reply.writeInt(1);
        return IS_DEBUG_ON;
    }

    private boolean isWifiAwareGet(Parcel data, Parcel reply) {
        byte[] resultQuery;
        if (data == null || reply == null) {
            return false;
        }
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "isWifiAwareGet, is not systemapp", new Object[0]);
            reply.writeNoException();
            reply.writeByteArray(new byte[0]);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        if (data.readInt() == 2) {
            resultQuery = getAwareAbility();
        } else {
            HwHiLog.e(TAG, false, "isWifiAwareGet not support the query", new Object[0]);
            resultQuery = new byte[0];
        }
        reply.writeNoException();
        reply.writeByteArray(resultQuery);
        return IS_DEBUG_ON;
    }

    private boolean requestWifiAwareInternal(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            return false;
        }
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "request aware request, is not systemapp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int mode = data.readInt();
        HwHiLog.i(TAG, false, "CODE_REQUEST_WIFI_AWARE is called mode=%{public}d", new Object[]{Integer.valueOf(mode)});
        if (mode == 1) {
            requestWifiAware(5);
        } else if (mode == 2) {
            requestWifiAware(6);
        } else if (mode == 3) {
            requestWifiAware(4);
        } else {
            HwHiLog.e(TAG, false, "requestWifiAware not support the request", new Object[0]);
        }
        reply.writeNoException();
        reply.writeInt(1);
        return IS_DEBUG_ON;
    }

    private boolean shouldSetWifiMode(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "setWifiMode: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "CODE_SET_WIFI_MODE: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            int mode = data.readInt();
            if (!isInSetWifiModeWhiteList(packageName, mode)) {
                HwHiLog.e(TAG, false, "CODE_SET_WIFI_MODE: is not in white list", new Object[0]);
                reply.writeNoException();
                reply.writeInt(0);
                return IS_DEBUG_ON;
            }
            int currentUserId = UserHandle.getUserId(Binder.getCallingUid());
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(packageName);
            stringBuffer.append('/');
            stringBuffer.append(currentUserId);
            String callPackageName = stringBuffer.toString();
            HwHiLog.i(TAG, false, "SET_WIFI_MODE is called callPackageName = %{public}s mode=%{public}d", new Object[]{callPackageName, Integer.valueOf(mode)});
            boolean isWifiModeHandled = isWifiModeHandled(callPackageName, mode);
            reply.writeNoException();
            reply.writeInt(isWifiModeHandled ? 1 : 0);
            return IS_DEBUG_ON;
        }
    }

    private boolean shouldGetWifiMode(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "getWifiMode: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "getWifiMode: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            HwHiLog.i(TAG, false, "getWifiMode is called packageName=%{public}s", new Object[]{packageName});
            if (!IS_THREE_STATE_SUPPORT || !isInSetWifiModeWhiteList(packageName, 1007)) {
                HwHiLog.e(TAG, false, "getWifiMode: is not in white list", new Object[0]);
                reply.writeNoException();
                reply.writeInt(-1);
                return IS_DEBUG_ON;
            }
            int mode = -1;
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
                int wifiState = hwWifiStateMachine.syncGetWifiState();
                int wifiModeInternal = hwWifiStateMachine.getWifiMode();
                if (wifiState == 3) {
                    mode = 1;
                } else {
                    mode = 0;
                }
                if (this.mWifiMode == 1007 && (wifiModeInternal & 8) != 0) {
                    mode = 2;
                }
            }
            reply.writeNoException();
            reply.writeInt(mode);
            return IS_DEBUG_ON;
        }
    }

    private boolean shouldHandleInterferenceParams(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "handleInterferenceParams: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiSlog.d(TAG, false, "CODE_HANDLE_HID2D_PARAMS SIGNATURE_NO_MATCH", new Object[0]);
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            int result = -1;
            int paramType = data.readInt();
            int paramValue = data.readInt();
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                result = this.mClientModeImpl.handleInterferenceParams(paramType, paramValue);
            }
            reply.writeNoException();
            reply.writeInt(result);
            return IS_DEBUG_ON;
        }
    }

    private boolean isSupportDualWifi(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "isSupportDualWifi: data or reply is null", new Object[0]);
            return false;
        }
        boolean[] resultArray = {false};
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "isSupportDualWifi: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBooleanArray(resultArray);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        reply.writeNoException();
        resultArray[0] = this.mHwWifi2Manager.isSupportDualWifi();
        reply.writeBooleanArray(resultArray);
        return IS_DEBUG_ON;
    }

    private boolean setSlaveWifiNetworkSelectionPara(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "setSlaveWifiNetworkSelectionPara: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "setSlaveWifiNetworkSelectionPara: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else if (!this.mHwWifi2Manager.isSupportDualWifi()) {
            HwHiLog.e(TAG, false, "setSlaveWifiNetworkSelectionPara: dual wifi is not supported", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            this.mHwWifi2Manager.setSlaveWifiNetworkSelectionPara(data.readInt(), data.readInt(), data.readInt());
            reply.writeNoException();
            return IS_DEBUG_ON;
        }
    }

    private boolean getSlaveWifiConnectionInfo(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "getSlaveWifiConnectionInfo: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "getSlaveWifiConnectionInfo: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else if (!this.mHwWifi2Manager.isSupportDualWifi()) {
            HwHiLog.e(TAG, false, "getSlaveWifiConnectionInfo: dual wifi is not supported", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            WifiInfo wifiInfo = this.mHwWifi2Manager.getSlaveWifiConnectionInfo();
            reply.writeNoException();
            reply.writeParcelable(wifiInfo, 0);
            return IS_DEBUG_ON;
        }
    }

    private boolean getLinkPropertiesForSlaveWifi(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "getLinkPropertiesForSlaveWifi: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "getLinkPropertiesForSlaveWifi: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else if (!this.mHwWifi2Manager.isSupportDualWifi()) {
            HwHiLog.e(TAG, false, "getSlaveWifiConnectionInfo: dual wifi is not supported", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            LinkProperties lp = this.mHwWifi2Manager.getLinkPropertiesForSlaveWifi();
            reply.writeNoException();
            reply.writeParcelable(lp, 0);
            return IS_DEBUG_ON;
        }
    }

    private boolean getNetworkInfoForSlaveWifi(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "getNetworkInfoForSlaveWifi: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "getNetworkInfoForSlaveWifi: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else if (!this.mHwWifi2Manager.isSupportDualWifi()) {
            HwHiLog.e(TAG, false, "getSlaveWifiConnectionInfo: dual wifi is not supported", new Object[0]);
            reply.writeNoException();
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            NetworkInfo networkInfo = this.mHwWifi2Manager.getNetworkInfoForSlaveWifi();
            reply.writeNoException();
            reply.writeParcelable(networkInfo, 0);
            return IS_DEBUG_ON;
        }
    }

    private boolean onWifi2Transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case CODE_IS_SUPPORT_DUAL_WIFI /* 4202 */:
                return isSupportDualWifi(data, reply);
            case CODE_CONFIG_NSP_SLAVE_WIFI /* 4203 */:
                return setSlaveWifiNetworkSelectionPara(data, reply);
            case CODE_GET_SLAVE_WIFI_INFO /* 4204 */:
                return getSlaveWifiConnectionInfo(data, reply);
            case CODE_GET_SLAVE_WIFI_LINKPROPERTY /* 4205 */:
                return getLinkPropertiesForSlaveWifi(data, reply);
            case CODE_GET_SLAVE_WIFI_NETWORK_IFNO /* 4206 */:
                return getNetworkInfoForSlaveWifi(data, reply);
            default:
                return HwWifiService.super.onTransact(code, data, reply, flags);
        }
    }

    private boolean isInCastOptWhiteList(String packageName) {
        for (String whitePackageName : WIFI_CAST_OPT_PACKAGE_LIST) {
            if (whitePackageName.equals(packageName) && "com.huawei.pcassistant".equals(packageName)) {
                return IS_DEBUG_ON;
            }
        }
        HwHiSlog.i(TAG, false, "%{public}s not in white list to set wifi mode", new Object[]{packageName});
        return false;
    }

    private boolean isInSetWifiLowLatencyModeWhiteList(String packageName) {
        for (String whitePackageName : WIFI_LOW_LATENCY_MODE_WHITE_PACKAGE_LIST) {
            if (whitePackageName.equals(packageName)) {
                return IS_DEBUG_ON;
            }
        }
        HwHiSlog.i(TAG, false, "%{public}s not in white list to set wifi low latency mode", new Object[]{packageName});
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == CODE_GET_WIFI_REPEATER_MODE) {
            return shouldGetWifiRepeaterMode(data, reply);
        }
        if (code == CODE_CONFIRM_WIFI_REPEATER) {
            return hasConfirmWifiRepeater(data, reply);
        }
        if (code == 2001) {
            return isHilinkHandshakeEnabled(data);
        }
        if (code == CODE_GET_CONNECTION_RAW_PSK) {
            return shouldGetConnRawPsk(data, reply);
        }
        if (code == CODE_RESTRICT_WIFI_SCAN) {
            return isRestrictScanSuccess(data, reply);
        }
        if (code == CODE_UPDATE_WM_FREQ_LOC) {
            return shouldUpdateWmFreqLocation(data, reply);
        }
        switch (code) {
            case 1001:
                return isGetWpaSuppConfig(data, reply);
            case 1002:
                return shouldStartPppoeConfig(data, reply);
            case 1003:
                return shouldStopPppoeConfig(data, reply);
            case 1004:
                return shouldGetPppoeInfo(data, reply);
            case 1005:
                return shouldGetApLinkedStaList(data, reply);
            case 1006:
                return isSoftapMacFilterSetSuccess(data, reply);
            case 1007:
                return isSoftapDisassocHandled(data, reply);
            case HwWifi2Manager.CLOSE_WIFI2_WIFI2_DISCONNECTED /* 1008 */:
                return shouldHandoverWifi(data, reply);
            case 1009:
                return shouldGetSoftapChannelList(data, reply);
            case 1010:
                return isApEvaluateEnabled(data, reply);
            case 1011:
                return shouldGetSignalInfo(data, reply);
            case 1012:
                return shouldSetVoWifiDetectMode(data, reply);
            case 1013:
                return shouldGetVoWifiDetectMode(data, reply);
            case 1014:
                return shouldSetVoWifiDetectPeriod(data, reply);
            case 1015:
                return shouldGetVoWifiDetectPeriod(data, reply);
            case CODE_IS_SUPPORT_VOWIFI_DETECT /* 1016 */:
                return isSupportVoWifiDetect(data, reply);
            case CODE_CTRL_HW_WIFI_NETWORK /* 1017 */:
                return shouldCtrlWifiNetwork(data, reply);
            case SERVER_CODE_CONTROL_HIDATA_OPTIMIZE /* 3031 */:
                return shouldCtrlHidataOpt(data, reply);
            case CODE_ENABLE_WIFICHIP_CHECK /* 4021 */:
                return isWifiChipEnabled(data, reply);
            case CODE_BIND_MPLINK /* 5002 */:
                return shouldBindMplink(data, reply);
            case CODE_IS_IN_MPLINK_STATE /* 5003 */:
                return isInMplinkState(data, reply);
            case CODE_WIFI_DC_CONNECT_Z /* 5004 */:
                return shouldDcConnectByZ(data, reply);
            case CODE_WIFI_IS_DC_ACTIVE /* 5005 */:
                return isDcActive(data, reply);
            default:
                switch (code) {
                    case CODE_REQUEST_WIFI_ENABLE /* 2004 */:
                        return hasRequestWifiEnabled(data);
                    case CODE_SET_WIFI_TXPOWER /* 2005 */:
                        return shouldSetWifiTxPower(data, reply);
                    case CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P /* 2006 */:
                        return shouldExtendWifiScanPeriodForP2p(data);
                    case CODE_REQUEST_FRESH_WHITE_LIST /* 2007 */:
                        return hasRequestFreshWhiteList(data, reply);
                    case CODE_GET_RSDB_SUPPORTED_MODE /* 2008 */:
                        return shouldGetTsdbMode(data, reply);
                    default:
                        switch (code) {
                            case 3001:
                                return shouldStartQoeMonitor(data, reply);
                            case 3002:
                                return shouldStopQoeMonitor(data, reply);
                            case 3003:
                                return shouldEvaluateQoe(data, reply);
                            case 3004:
                                return shouldUpdateQoeStatus(data, reply);
                            case CODE_UPDATE_APP_RUNNING_STATUS /* 3005 */:
                                return shouldUpdateAppRunningStatus(data, reply);
                            case CODE_UPDATE_APP_EXPERIENCE_STATUS /* 3006 */:
                                return shouldUpdateAppExperienceStatus(data, reply);
                            case CODE_SET_WIFI_ANTSET /* 3007 */:
                                return shouldSetAntset(data, reply);
                            case CODE_IS_BG_LIMIT_ALLOWED /* 3008 */:
                                return isAllowedBgLimit(data, reply);
                            default:
                                switch (code) {
                                    case CODE_DISABLE_RX_FILTER /* 3021 */:
                                        return shouldDisableRxFilter(data, reply);
                                    case CODE_ENABLE_RX_FILTER /* 3022 */:
                                        return shouldEnableRxFilter(data, reply);
                                    case CODE_START_WIFI_KEEP_ALIVE /* 3023 */:
                                        return shouldKeepWifiAlive(data, reply);
                                    case CODE_STOP_WIFI_KEEP_ALIVE /* 3024 */:
                                        return shouldStopWifiAlive(data, reply);
                                    case CODE_UPDATE_LIMIT_SPEED_STATUS /* 3025 */:
                                        return shouldUpdateSpeedLimit(data, reply);
                                    default:
                                        switch (code) {
                                            case CODE_IS_FEATURE_SUPPORTED /* 4011 */:
                                                return isFeatureSupportHandled(data, reply);
                                            case CODE_GET_SOFTAP_BANDWIDTH /* 4012 */:
                                                return hasSetSoftapBandWidth(data, reply);
                                            case CODE_SET_FEM_TXPOWER /* 4013 */:
                                                return hasSetFemTxPower(data, reply);
                                            case CODE_WIFI_DC_CONNECT /* 4014 */:
                                                return shouldDcConnect(data, reply);
                                            case CODE_WIFI_DC_DISCONNECT /* 4015 */:
                                                return shouldDcDisconnect(data, reply);
                                            default:
                                                switch (code) {
                                                    case CODE_SET_PERFORMANCE_MODE /* 4102 */:
                                                        return shouldSetPerformanceMode(data, reply);
                                                    case CODE_REPORT_SPEED_RESULT /* 4103 */:
                                                        return shouldReportSpeedResult(data, reply);
                                                    case CODE_SET_HIGH_PRIORITY_TRANSMIT /* 4104 */:
                                                        return isPriorityTransmitHandled(data, reply);
                                                    case CODE_HANDLE_HID2D_PARAMS /* 4105 */:
                                                        return shouldHandleInterferenceParams(data, reply);
                                                    case CODE_REQUEST_WIFI_AWARE /* 4106 */:
                                                        return requestWifiAwareInternal(data, reply);
                                                    case CODE_SET_WIFI_AWARE /* 4107 */:
                                                        return isWifiAwareSet(data, reply);
                                                    case CODE_GET_WIFI_AWARE /* 4108 */:
                                                        return isWifiAwareGet(data, reply);
                                                    default:
                                                        switch (code) {
                                                            case CODE_SET_WIFI_MODE /* 4111 */:
                                                                return shouldSetWifiMode(data, reply);
                                                            case CODE_GET_WIFI_MODE /* 4112 */:
                                                                return shouldGetWifiMode(data, reply);
                                                            case CODE_DBAC_GET_SELF_WIFI_INFO /* 4113 */:
                                                                return isSelfWifiInfoHandled(data, reply);
                                                            case CODE_DBAC_SET_PEER_WIFI_INFO /* 4114 */:
                                                                return isPeerWifiInfoHandled(data, reply);
                                                            case CODE_DBAC_REGISTER_CALLBACK /* 4115 */:
                                                                return isDbacRegisterCallbackHandled(data, reply);
                                                            case CODE_DBAC_UNREGISTER_CALLBACK /* 4116 */:
                                                                return isDbacUnregisterCallbackHandled(data, reply);
                                                            case CODE_DBAC_GET_RECOMMEND_CHANNEL /* 4117 */:
                                                                return isDbacRecommendChannelHandled(data, reply);
                                                            case CODE_QUERY_CSI /* 4118 */:
                                                                return isQueryCsiHandled(data, reply);
                                                            case CODE_QUERY_SNIFFER /* 4119 */:
                                                                return isQuerySnifferHandled(data, reply);
                                                            case CODE_QUERY_ARP /* 4120 */:
                                                                return isQueryArpHandled(data, reply);
                                                            case CODE_SET_WIFI_LOW_LATENCY_MODE /* 4121 */:
                                                                return isSetWifiLowLatencyModeHandled(data, reply);
                                                            case CODE_ROAM_TO_NETWORK /* 4122 */:
                                                                return isHandleRoamToNetwork(data, reply);
                                                            default:
                                                                return onWifi2Transact(code, data, reply, flags);
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }

    private boolean isDcActive(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "check dc state: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        DcController dcController = DcController.getInstance();
        boolean isSuccess = false;
        if (dcController != null) {
            isSuccess = dcController.isWifiDcActive();
        }
        reply.writeNoException();
        reply.writeBoolean(isSuccess);
        return IS_DEBUG_ON;
    }

    private boolean shouldDcConnectByZ(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "connect dc: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        WifiDeviceConfig deviceConfig = null;
        if (data.readInt() == 1) {
            deviceConfig = (WifiDeviceConfig) WifiDeviceConfig.CREATOR.createFromParcel(data);
        }
        WifiConfiguration wifiConfig = null;
        if (deviceConfig != null) {
            wifiConfig = deviceConfig.toWifiConfig(this.mContext);
        }
        if (deviceConfig == null || wifiConfig == null) {
            HwHiSlog.w(TAG, false, "wifi config is invalid", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return false;
        }
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.dcConnect(wifiConfig, null);
        }
        reply.writeNoException();
        reply.writeBoolean(IS_DEBUG_ON);
        return IS_DEBUG_ON;
    }

    private boolean isInMplinkState(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "check mplink state: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int uid = data.readInt();
        boolean isSuccess = false;
        HwArbitrationManager hwArbitrationManager = HwArbitrationManager.getInstance();
        if (hwArbitrationManager != null) {
            isSuccess = hwArbitrationManager.isInMpLink(uid);
        }
        reply.writeNoException();
        reply.writeBoolean(isSuccess);
        return IS_DEBUG_ON;
    }

    private boolean shouldBindMplink(Parcel data, Parcel reply) {
        boolean shouldEnableMp = false;
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "bind mplink: SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        if (data.readInt() == 1) {
            shouldEnableMp = true;
        }
        String pkg = this.mContext.getPackageManager().getNameForUid(data.readInt());
        boolean isControl = false;
        HwHiDataManager hidataManager = HwHiDataManager.getInstance();
        if (hidataManager != null) {
            isControl = hidataManager.controlHiDataOptimize(pkg, 0, shouldEnableMp);
        }
        reply.writeNoException();
        reply.writeBoolean(isControl);
        return IS_DEBUG_ON;
    }

    private boolean shouldReportSpeedResult(Parcel data, Parcel reply) {
        if (isSignMatchOrSystemApp()) {
            HwHiSlog.v(TAG, false, "CODE_SET_PERFORMANCE_MODE sign match", new Object[0]);
        } else if (isAllowAccessWifiService()) {
            enforceCallerPermission(ACCESS_WIFI_PERMISSION);
        } else {
            HwHiSlog.d(TAG, false, "CODE_SET_PERFORMANCE_MODE SIGNATURE_NO_MATCH", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean reportSpeedMeasureResult = HwWifiSpeedMeasure.reportSpeedMeasureResult(this.mHwWifiChrService, data.readString());
        reply.writeNoException();
        reply.writeInt(reportSpeedMeasureResult ? 1 : 0);
        return IS_DEBUG_ON;
    }

    private boolean shouldSetPerformanceMode(Parcel data, Parcel reply) {
        int result = 0;
        if (isSignMatchOrSystemApp()) {
            HwHiSlog.v(TAG, false, "CODE_SET_PERFORMANCE_MODE SIGNATURE_MATCH", new Object[0]);
        } else if (isAllowAccessWifiService()) {
            enforceCallerPermission(ACCESS_WIFI_PERMISSION);
        } else {
            HwHiSlog.d(TAG, false, "CODE_SET_PERFORMANCE_MODE SIGNATURE_NO_MATCH", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int mode = data.readInt();
        if (mode == 0 || mode == 1) {
            result = 1;
        }
        reply.writeNoException();
        reply.writeInt(result);
        return IS_DEBUG_ON;
    }

    private boolean shouldDcDisconnect(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_WIFI_DC_DISCONNECT SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        HwHiSlog.i(TAG, false, "HwWifiService CODE_WIFI_DC_DISCONNECT", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        boolean isDcDisconnect = false;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            isDcDisconnect = this.mClientModeImpl.dcDisconnect();
        }
        reply.writeNoException();
        reply.writeBooleanArray(new boolean[]{isDcDisconnect});
        return IS_DEBUG_ON;
    }

    private boolean shouldDcConnect(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_WIFI_DC_CONNECT SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        HwHiSlog.i(TAG, false, "HwWifiService CODE_WIFI_DC_CONNECT", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        WifiConfiguration wifiConfig = null;
        if (data.readInt() == 1) {
            wifiConfig = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
        }
        IWifiActionListener actionListener = IWifiActionListener.Stub.asInterface(data.readStrongBinder());
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.dcConnect(wifiConfig, actionListener);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean hasSetSoftapBandWidth(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.d(TAG, false, "CODE_IS_FEATURE_SUPPORTED SIGNATURE_NO_MATCH", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int result = 0;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            result = this.mClientModeImpl.getApBandwidth();
        }
        reply.writeNoException();
        reply.writeInt(result);
        return IS_DEBUG_ON;
    }

    private boolean hasSetFemTxPower(Parcel data, Parcel reply) {
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.d(TAG, false, "CODE_IS_FEATURE_SUPPORTED SIGNATURE_NO_MATCH", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isSuccess = false;
        int action = data.readInt();
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            isSuccess = this.mClientModeImpl.reduceTxPower(action);
        }
        reply.writeNoException();
        if (isSuccess) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean isFeatureSupportHandled(Parcel data, Parcel reply) {
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.d(TAG, false, "CODE_IS_FEATURE_SUPPORTED SIGNATURE_NO_MATCH", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isSupported = false;
        int featrue = data.readInt();
        int ifaceType = data.readInt();
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            isSupported = this.mClientModeImpl.isFeatureSupported(featrue, ifaceType);
        }
        reply.writeNoException();
        if (isSupported) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean shouldUpdateWmFreqLocation(Parcel data, Parcel reply) {
        boolean isUpdateWaveMapp = false;
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_UPDATE_WM_FREQ_LOC SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int location = data.readInt();
        int action = data.readInt();
        FrequentLocation frequentLocation = FrequentLocation.getInstance();
        if (frequentLocation != null) {
            isUpdateWaveMapp = frequentLocation.updateWaveMapping(location, action);
        }
        reply.writeNoException();
        if (isUpdateWaveMapp) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean shouldStopWifiAlive(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_START_WIFI_KEEP_ALIVE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        Message msg = (Message) Message.CREATOR.createFromParcel(data);
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.stopPacketKeepalive(msg);
        }
        reply.writeNoException();
        reply.writeInt(1);
        return IS_DEBUG_ON;
    }

    private boolean shouldKeepWifiAlive(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_START_WIFI_KEEP_ALIVE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        Message msg = (Message) Message.CREATOR.createFromParcel(data);
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.startPacketKeepalive(msg);
        }
        reply.writeNoException();
        reply.writeInt(1);
        return IS_DEBUG_ON;
    }

    private boolean shouldEnableRxFilter(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(ACCESS_WIFI_FILTER_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "enableWifiFilter: No ACCESS_FILTER permission", new Object[0]);
            return false;
        } else if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            HwHiSlog.d(TAG, false, "you have no permission to call enableWifiFilter from uid:%{public}d", new Object[]{Integer.valueOf(Binder.getCallingUid())});
            return false;
        } else {
            HwHiSlog.i(TAG, false, "call binder enableWifiFilter %{public}s", new Object[]{getAppName(Binder.getCallingPid())});
            boolean isEnableFilter = enableWifiFilter(data.readStrongBinder());
            reply.writeNoException();
            reply.writeBooleanArray(new boolean[]{isEnableFilter});
            return IS_DEBUG_ON;
        }
    }

    private boolean shouldDisableRxFilter(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(ACCESS_WIFI_FILTER_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "disableWifiFilter: No ACCESS_FILTER permission", new Object[0]);
            return false;
        } else if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            HwHiSlog.d(TAG, false, "you have no permission to call disableWifiFilter from uid:%{public}d", new Object[]{Integer.valueOf(Binder.getCallingUid())});
            return false;
        } else {
            HwHiSlog.i(TAG, false, "call binder disableWifiFilter %{public}s", new Object[]{getAppName(Binder.getCallingPid())});
            boolean isDisableFilter = disableWifiFilter(data.readStrongBinder());
            reply.writeNoException();
            reply.writeBooleanArray(new boolean[]{isDisableFilter});
            return IS_DEBUG_ON;
        }
    }

    private boolean shouldSetAntset(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_WIFI_ANTSET SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String iface = data.readString();
        int mode = data.readInt();
        int op = data.readInt();
        HwMssHandler mssHandler = HwMssHandler.getInstance();
        if (mssHandler != null) {
            mssHandler.setWifiAnt(iface, mode, op);
            HwHiSlog.d(TAG, false, "mssHandler hwSetWifiAnt", new Object[0]);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldGetTsdbMode(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_GET_RSDB_SUPPORTED_MODE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBooleanArray(new boolean[]{false});
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isSupport = false;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                isSupport = hwWifiStateMachine.isRSDBSupported();
            } else {
                HwHiSlog.e(TAG, false, "Exception mWifiStateMachineChannel is not initialized", new Object[0]);
            }
        }
        reply.writeNoException();
        reply.writeBooleanArray(new boolean[]{isSupport});
        return IS_DEBUG_ON;
    }

    private boolean hasRequestFreshWhiteList(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_REQUEST_FRESH_WHITE_LIST SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int type = data.readInt();
        List<String> packageWhiteList = new ArrayList<>();
        data.readStringList(packageWhiteList);
        if (type == 7) {
            HwQoEService wifiSleepQoe = HwQoEService.getInstance();
            if (wifiSleepQoe != null) {
                wifiSleepQoe.updateWifiSleepWhiteList(type, packageWhiteList);
            }
        } else {
            BackgroundAppScanManager.getInstance().refreshPackageWhitelist(type, packageWhiteList);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldUpdateAppExperienceStatus(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_UPDATE_APP_EXPERIENCE_STATUS SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        int uid = data.readInt();
        int experience = data.readInt();
        long rttExperience = data.readLong();
        data.readInt();
        HwHiSlog.d(TAG, false, "updateAppExperienceStatus  uid:%{public}d, experience:%{public}d, rtt:%{public}s", new Object[]{Integer.valueOf(uid), Integer.valueOf(experience), String.valueOf(rttExperience)});
        reply.writeNoException();
        reply.writeInt(1);
        return IS_DEBUG_ON;
    }

    private boolean shouldUpdateAppRunningStatus(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_UPDATE_APP_RUNNING_STATUS SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        int runningUid = data.readInt();
        int runningType = data.readInt();
        int runningStatus = data.readInt();
        int runningScene = data.readInt();
        data.readInt();
        HwHiSlog.d(TAG, false, " updateAppRunningStatus  uid:%{public}d, type:%{public}d, status:%{public}d scene: %{public}d", new Object[]{Integer.valueOf(runningUid), Integer.valueOf(runningType), Integer.valueOf(runningStatus), Integer.valueOf(runningScene)});
        reply.writeNoException();
        reply.writeInt(1);
        return IS_DEBUG_ON;
    }

    private boolean shouldCtrlHidataOpt(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiLog.e(TAG, false, "hidata control SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String pkgName = data.readString();
        int controlAction = data.readInt();
        boolean isEnable = data.readBoolean();
        boolean isControlled = false;
        if (controlAction != 6060) {
            HwHiDataManager hidataManager = HwHiDataManager.getInstance();
            if (hidataManager != null) {
                isControlled = hidataManager.controlHiDataOptimize(pkgName, controlAction, isEnable);
            }
        } else if (!DcUtils.isDcSupported()) {
            HwHiSlog.e(TAG, false, "Do not support DC, return", new Object[0]);
            reply.writeNoException();
            reply.writeBoolean(false);
            return IS_DEBUG_ON;
        } else {
            DcController dcController = DcController.getInstance();
            if (dcController != null) {
                isControlled = dcController.isDcDisconnectSuccess(pkgName);
            }
        }
        reply.writeNoException();
        reply.writeBoolean(isControlled);
        return IS_DEBUG_ON;
    }

    private boolean shouldUpdateSpeedLimit(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        int runningMode = data.readInt();
        int runningReserve1 = data.readInt();
        int runningReserve2 = data.readInt();
        HwHiSlog.d(TAG, false, " updateLimitSpeedStatus mode: %{public}d, reserve1: %{public}d reserve2: %{public}d", new Object[]{Integer.valueOf(runningMode), Integer.valueOf(runningReserve1), Integer.valueOf(runningReserve2)});
        boolean isUpdateLimitSpeed = false;
        HwQoEService qoeServiceAppRunning = HwQoEService.getInstance();
        if (qoeServiceAppRunning != null) {
            isUpdateLimitSpeed = qoeServiceAppRunning.updateLimitSpeedStatus(runningMode, runningReserve1, runningReserve2);
        }
        reply.writeNoException();
        reply.writeBooleanArray(new boolean[]{isUpdateLimitSpeed});
        return IS_DEBUG_ON;
    }

    private boolean isAllowedBgLimit(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_IS_BG_LIMIT_ALLOWED SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeBooleanArray(new boolean[]{false});
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        int uid = data.readInt();
        boolean isAllowed = false;
        HwQoEService qoeSevice = HwQoEService.getInstance();
        if (qoeSevice != null) {
            isAllowed = qoeSevice.isBgLimitAllowed(uid);
        }
        reply.writeNoException();
        reply.writeBooleanArray(new boolean[]{isAllowed});
        return IS_DEBUG_ON;
    }

    private boolean shouldUpdateQoeStatus(Parcel data, Parcel reply) {
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_WIFI_QOE_UPDATE_STATUS SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isUpdated = false;
        int state = data.readInt();
        HwQoEService hwQoeService = HwQoEService.getInstance();
        if (hwQoeService != null) {
            isUpdated = hwQoeService.updateVoWiFiState(state);
        }
        reply.writeNoException();
        if (isUpdated) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean shouldEvaluateQoe(Parcel data, Parcel reply) {
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_WIFI_QOE_EVALUATE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        IHwQoECallback callback = IHwQoECallback.Stub.asInterface(data.readStrongBinder());
        boolean isEvaluated = false;
        HwQoEService hwQoeService = HwQoEService.getInstance();
        if (hwQoeService != null) {
            isEvaluated = hwQoeService.evaluateNetworkQuality(callback);
        }
        reply.writeNoException();
        if (isEvaluated) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean shouldStopQoeMonitor(Parcel data, Parcel reply) {
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_WIFI_QOE_START_MONITOR SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isSuccess = false;
        int monitorType = data.readInt();
        HwQoEService hwQoeService = HwQoEService.getInstance();
        if (hwQoeService != null) {
            isSuccess = hwQoeService.unRegisterHwQoEMonitor(monitorType);
        }
        reply.writeNoException();
        if (isSuccess) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean shouldStartQoeMonitor(Parcel data, Parcel reply) {
        int i = 0;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_WIFI_QOE_START_MONITOR SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isSuccess = false;
        int monitorType = data.readInt();
        int period = data.readInt();
        IHwQoECallback callback = IHwQoECallback.Stub.asInterface(data.readStrongBinder());
        HwQoEService hwQoeService = HwQoEService.getInstance();
        if (hwQoeService != null) {
            isSuccess = hwQoeService.registerHwQoEMonitor(monitorType, period, callback);
        }
        reply.writeNoException();
        if (isSuccess) {
            i = 1;
        }
        reply.writeInt(i);
        return IS_DEBUG_ON;
    }

    private boolean shouldSetWifiTxPower(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_WIFI_TXPOWER SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int result = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.setWifiTxPowerHw(data.readInt());
        reply.writeNoException();
        reply.writeInt(result);
        return IS_DEBUG_ON;
    }

    private boolean shouldExtendWifiScanPeriodForP2p(Parcel data) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService EXTEND_WIFI_SCAN_PERIOD_FOR_P2P SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "HwWifiService  EXTEND_WIFI_SCAN_PERIOD_FOR_P2P", new Object[0]);
        return shouldExternWifiScanPeriodForP2p(data);
    }

    private boolean hasRequestWifiEnabled(Parcel data) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService REQUEST_WIFI_ENABLE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "HwWifiService REQUEST_WIFI_ENABLE", new Object[0]);
        return isRequestWifiEnableSuccess(data);
    }

    private boolean shouldGetConnRawPsk(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_GET_CONNECTION_RAW_PSK SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeString(null);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String result = null;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            result = this.mClientModeImpl.getConnectionRawPsk();
        }
        reply.writeNoException();
        reply.writeString(result);
        return IS_DEBUG_ON;
    }

    private boolean isHilinkHandshakeEnabled(Parcel data) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_ENABLE_HILINK_HANDSHAKE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        boolean isHiLinkEnable = false;
        if (data.readInt() == 1) {
            isHiLinkEnable = IS_DEBUG_ON;
        }
        String bssid = data.readString();
        WifiConfiguration config = null;
        if (data.readInt() == 1) {
            config = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
        }
        this.mClientModeImpl.enableHiLinkHandshake(isHiLinkEnable, bssid, config);
        return IS_DEBUG_ON;
    }

    private boolean isSupportVoWifiDetect(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_IS_SUPPORT_VOWIFI_DETECT SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService  IS_SUPPORT_VOWIFI_DETECT ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "isSupportVoWifiDetect(): permissin deny", new Object[0]);
            return false;
        }
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                hwWifiStateMachine.syncGetSupportedVoWifiDetect(wifiServiceUtils.getWifiStateMachineChannel(this));
            } else {
                HwHiSlog.e(TAG, false, "Exception mWifiStateMachineChannel is not initialized", new Object[0]);
            }
        }
        reply.writeNoException();
        reply.writeBooleanArray(new boolean[]{IS_DEBUG_ON});
        return IS_DEBUG_ON;
    }

    private boolean shouldGetVoWifiDetectPeriod(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_GET_VOWIFI_DETECT_PERIOD SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(-1);
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService  GET_VOWIFI_DETECT_PERIOD ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "getVoWifiDetectPeriod(): permissin deny", new Object[0]);
            return false;
        }
        int result = -1;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            result = this.mClientModeImpl.getVoWifiDetectPeriod();
        }
        reply.writeNoException();
        reply.writeInt(result);
        return IS_DEBUG_ON;
    }

    private boolean shouldSetVoWifiDetectPeriod(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_VOWIFI_DETECT_PERIOD SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService  SET_VOWIFI_DETECT_PERIOD ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "setVoWifiDetectPeriod(): permissin deny", new Object[0]);
            return false;
        }
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.setVoWifiDetectPeriod(data.readInt());
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldGetVoWifiDetectMode(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_GET_VOWIFI_DETECT_MODE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService  GET_VOWIFI_DETECT_MODE ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        WifiDetectConfInfo result = null;
        if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "getVoWifiDetectMode(): permissin deny", new Object[0]);
            return false;
        }
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            result = this.mClientModeImpl.getVoWifiDetectMode();
        }
        reply.writeNoException();
        if (result != null) {
            reply.writeInt(1);
            result.writeToParcel(reply, 1);
        } else {
            reply.writeInt(0);
        }
        return IS_DEBUG_ON;
    }

    private boolean shouldSetVoWifiDetectMode(Parcel data, Parcel reply) {
        WifiDetectConfInfo config;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_VOWIFI_DETECT_MODE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService  SET_VOWIFI_DETECT_MODE ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "setVoWifiDetectMode(): permissin deny", new Object[0]);
            return false;
        }
        if (data.readInt() != 0) {
            config = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(data);
        } else {
            config = null;
        }
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.setVoWifiDetectMode(config);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldGetSignalInfo(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_GET_SINGNAL_INFO SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeByteArray(null);
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService FETCH_WIFI_SIGNAL_INFO_FOR_VOWIFI ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
            HwHiSlog.d(TAG, false, "fetchWifiSignalInfoForVoWiFi(): permissin deny", new Object[0]);
            return false;
        }
        byte[] result = null;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            result = this.mClientModeImpl.fetchWifiSignalInfoForVoWiFi();
        }
        reply.writeNoException();
        reply.writeByteArray(result);
        return IS_DEBUG_ON;
    }

    private boolean shouldGetSoftapChannelList(Parcel data, Parcel reply) {
        HwHiSlog.d(TAG, false, "Receive CODE_GET_SOFTAP_CHANNEL_LIST", new Object[0]);
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_GET_SOFTAP_CHANNEL_LIST SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeIntArray(null);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int type = data.readInt();
        int[] result = null;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (type == 1) {
                result = hwWifiStateMachine.getSoftApWideBandWidthChannels();
            } else {
                result = hwWifiStateMachine.getSoftApChannelListFor5G();
            }
        }
        reply.writeNoException();
        reply.writeIntArray(result);
        return IS_DEBUG_ON;
    }

    private boolean isApEvaluateEnabled(Parcel data, Parcel reply) {
        boolean isEnabled = false;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_WIFI_AP_EVALUATE_ENABLED SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        HwHiSlog.d(TAG, false, "HwWifiService  SET_WIFI_AP_EVALUATE_ENABLED ", new Object[0]);
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        if (data.readInt() == 1) {
            isEnabled = true;
        }
        if (this.mHwWifiProServiceManager.isWifiProStateMachineStarted()) {
            this.mHwWifiProServiceManager.setWifiApEvaluateEnabled(isEnabled);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldCtrlWifiNetwork(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            Slog.e(TAG, "WifiService CODE_CTRL_HW_WIFI_NETWORK SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String pkgName = data.readString();
        int interfaceId = data.readInt();
        Bundle bundle = data.readBundle();
        Slog.d(TAG, "HwWifiService  CODE_CTRL_HW_WIFI_NETWORK");
        this.mHwWifiProServiceProxy = HwWifiProServiceProxy.getHwWifiProServiceProxy(this.mContext);
        Bundle result = new Bundle();
        HwWifiProServiceProxy hwWifiProServiceProxy = this.mHwWifiProServiceProxy;
        if (hwWifiProServiceProxy != null) {
            result = hwWifiProServiceProxy.ctrlHwWifiNetwork(pkgName, interfaceId, bundle);
        }
        reply.writeNoException();
        reply.writeBundle(result);
        return IS_DEBUG_ON;
    }

    private boolean shouldHandoverWifi(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_USER_HANDOVER_WIFI SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "HwWifiService  userHandoverWiFi ", new Object[0]);
        if (this.mHwWifiProServiceManager.isWifiProStateMachineStarted()) {
            this.mHwWifiProServiceManager.userHandoverWifi();
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean hasConfirmWifiRepeater(Parcel data, Parcel reply) {
        HwHiSlog.d(TAG, false, "CODE_CONFIRM_WIFI_REPEATER enter", new Object[0]);
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "CODE_CONFIRM_WIFI_REPEATER SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        int repeaterMode = data.readInt();
        IWifiRepeaterConfirmListener listener = IWifiRepeaterConfirmListener.Stub.asInterface(data.readStrongBinder());
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.confirmWifiRepeater(repeaterMode, listener);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldGetWifiRepeaterMode(Parcel data, Parcel reply) {
        int wifiRepeaterMode = -1;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "CODE_GET_WIFI_REPEATER_MODE SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(-1);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            wifiRepeaterMode = this.mClientModeImpl.getWifiRepeaterMode();
        }
        reply.writeNoException();
        reply.writeInt(wifiRepeaterMode);
        return IS_DEBUG_ON;
    }

    private boolean isSoftapDisassocHandled(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_SOFTAP_DISASSOCIATESTA SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String mac = data.readString();
        HwHiSlog.d(TAG, false, "Receive CODE_SET_SOFTAP_DISASSOCIATESTA, mac = %{private}s", new Object[]{mac});
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (isSoftApEnabled()) {
                hwWifiStateMachine.setSoftapDisassociateSta(mac);
            } else if (!SHOULD_NETWORK_SHARING_INTEGRATION || !hwWifiStateMachine.isWifiRepeaterStarted()) {
                HwHiSlog.w(TAG, false, "Receive CODE_SET_SOFTAP_DISASSOCIATESTA when softap state is not enabled", new Object[0]);
            } else {
                hwWifiStateMachine.setWifiRepeaterDisassociateSta(mac);
            }
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean isSoftapMacFilterSetSuccess(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService CODE_SET_SOFTAP_MACFILTER SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        String macFilter = data.readString();
        HwHiSlog.d(TAG, false, "Receive CODE_SET_SOFTAP_MACFILTER, macFilter:%{private}s", new Object[]{macFilter});
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (isSoftApEnabled()) {
                hwWifiStateMachine.setSoftapMacFilter(macFilter);
                notifyBlacklistEvent(macFilter);
            } else if (!SHOULD_NETWORK_SHARING_INTEGRATION || !hwWifiStateMachine.isWifiRepeaterStarted()) {
                HwHiSlog.w(TAG, false, "Receive CODE_SET_SOFTAP_MACFILTER when softap state is not enabled", new Object[0]);
            } else {
                hwWifiStateMachine.setWifiRepeaterMacFilter(macFilter);
            }
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldGetApLinkedStaList(Parcel data, Parcel reply) {
        HwHiSlog.d(TAG, false, "Receive CODE_GET_APLINKED_STA_LIST", new Object[0]);
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_GET_APLINKED_STA_LIST SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeStringList(null);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        List<String> result = null;
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (isSoftApEnabled()) {
                result = hwWifiStateMachine.getApLinkedStaList();
            } else if (!SHOULD_NETWORK_SHARING_INTEGRATION || !hwWifiStateMachine.isWifiRepeaterStarted()) {
                HwHiSlog.w(TAG, false, "Receive CODE_GET_APLINKED_STA_LIST when softap state is not enabled", new Object[0]);
            } else {
                result = hwWifiStateMachine.getRepeaterLinkedClientList();
            }
        }
        if (result == null) {
            HwHiSlog.d(TAG, false, "getApLinkedStaList result = null", new Object[0]);
        } else {
            HwHiSlog.d(TAG, false, "getApLinkedStaList result = %{private}s", new Object[]{result.toString().replaceAll("\\.[\\d]{1,3}\\.[\\d]{1,3}\\.", ".*.*.").replaceAll(":[\\w]{1,}:[\\w]{1,}:", ":**:**:")});
        }
        reply.writeNoException();
        reply.writeStringList(result);
        return IS_DEBUG_ON;
    }

    private boolean shouldGetPppoeInfo(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_GET_PPPOE_INFO_CONFIG SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeInt(0);
            reply.writeNoException();
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "WifiService  get PPPOE info", new Object[0]);
        if (!this.isPppoe) {
            HwHiSlog.w(TAG, false, "the PPPOE function is closed.", new Object[0]);
            return false;
        }
        PPPOEInfo result = this.mPppoeStateMachine.getPPPOEInfo();
        reply.writeNoException();
        if (result != null) {
            reply.writeInt(1);
            result.writeToParcel(reply, 1);
        } else {
            reply.writeInt(0);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldStopPppoeConfig(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_STOP_PPPOE_CONFIG SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "WifiService  stopPPPOE", new Object[0]);
        if (!this.isPppoe) {
            HwHiSlog.w(TAG, false, "the PPPOE function is closed.", new Object[0]);
            return false;
        }
        this.mPppoeStateMachine.sendMessage(589826);
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean shouldStartPppoeConfig(Parcel data, Parcel reply) {
        PPPOEConfig config;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_START_PPPOE_CONFIG SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "WifiService  startPPPOE", new Object[0]);
        if (!this.isPppoe) {
            HwHiSlog.w(TAG, false, "the PPPOE function is closed.", new Object[0]);
            return false;
        }
        if (data.readInt() != 0) {
            config = (PPPOEConfig) PPPOEConfig.CREATOR.createFromParcel(data);
        } else {
            config = null;
        }
        this.mPppoeStateMachine.sendMessage(589825, config);
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean isGetWpaSuppConfig(Parcel data, Parcel reply) {
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_GET_WPA_SUPP_CONFIG SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            reply.writeString(null);
            return IS_DEBUG_ON;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        HwHiSlog.d(TAG, false, "WifiService getWpaSuppConfig", new Object[0]);
        String result = "";
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            result = this.mClientModeImpl.getWpaSuppConfig();
        }
        reply.writeNoException();
        reply.writeString(result);
        return IS_DEBUG_ON;
    }

    private boolean isWifiChipEnabled(Parcel data, Parcel reply) {
        boolean isRestrict = false;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_ENABLE_WIFICHIP_CHECKER SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        if (data.readInt() != 0) {
            isRestrict = true;
        }
        if (this.mClientModeImpl instanceof HwWifiStateMachine) {
            this.mClientModeImpl.enbaleWifichipCheck(isRestrict);
        }
        return IS_DEBUG_ON;
    }

    private boolean isRestrictScanSuccess(Parcel data, Parcel reply) {
        boolean isRestrict = false;
        if (!isSignMatchOrSystemApp()) {
            HwHiSlog.e(TAG, false, "WifiService  CODE_RESTRICT_WIFI_SCAN SIGNATURE_NO_MATCH or not systemApp", new Object[0]);
            reply.writeNoException();
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        enforceAccessPermission();
        List<String> pkgs = new ArrayList<>();
        data.readStringList(pkgs);
        if (pkgs.isEmpty()) {
            pkgs = null;
        }
        if (data.readInt() != 0) {
            isRestrict = true;
        }
        restrictWifiScan(pkgs, isRestrict);
        return IS_DEBUG_ON;
    }

    private boolean isCsiSnifferArpWhiteList(HwQueryCsiSnifferArp instance, String packageName) {
        if (instance == null) {
            HwHiSlog.e(TAG, false, "QUERY_CSI_SNIFF_ARP instance is null", new Object[0]);
            return false;
        } else if (!instance.isInWhiteList(packageName)) {
            HwHiSlog.e(TAG, false, "QUERY_CSI_SNIFF_ARP packageName=%{public}s not in whitelist", new Object[]{packageName});
            return false;
        } else {
            enforceCallerPermission(instance.getPermission());
            return IS_DEBUG_ON;
        }
    }

    private boolean isQueryCsiHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isQueryCsiHandled: data or reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_QUERY_CSI SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            HwHiLog.i(TAG, false, "CODE_QUERY_CSI is called packageName=%{public}s", new Object[]{packageName});
            int ret = -1;
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                HwQueryCsiSnifferArp instance = HwQueryCsiSnifferArp.getInstance(this.mClientModeImpl, this.mHwWifiProServiceManager);
                if (isCsiSnifferArpWhiteList(instance, packageName)) {
                    boolean isEnabled = data.readInt() == 1;
                    ArrayList arrayList = new ArrayList();
                    data.readTypedList(arrayList, MacAddress.CREATOR);
                    ret = instance.queryCsi(isEnabled, arrayList, data.readInt(), IWifiActionListener.Stub.asInterface(data.readStrongBinder()), getCallingPid());
                }
            }
            reply.writeNoException();
            reply.writeInt(ret);
            return IS_DEBUG_ON;
        }
    }

    private boolean isQuerySnifferHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isQuerySnifferHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_QUERY_SNIFFER SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            HwHiLog.i(TAG, false, "CODE_QUERY_SNIFFER is called packageName=%{public}s", new Object[]{packageName});
            int ret = -1;
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                HwQueryCsiSnifferArp instance = HwQueryCsiSnifferArp.getInstance(this.mClientModeImpl, this.mHwWifiProServiceManager);
                if (isCsiSnifferArpWhiteList(instance, packageName)) {
                    ret = instance.querySniffer(data.readInt(), (MacAddress) data.readTypedObject(MacAddress.CREATOR), data.readString(), IWifiActionListener.Stub.asInterface(data.readStrongBinder()), getCallingPid());
                }
            }
            reply.writeNoException();
            reply.writeInt(ret);
            return IS_DEBUG_ON;
        }
    }

    private boolean isHandleRoamToNetwork(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "handleRoamToNetwork: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_ROAM_TO_NETWORK SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeBoolean(false);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            boolean isResult = false;
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                String packageName = data.readString();
                ScanResult targetNetwork = (ScanResult) data.readTypedObject(ScanResult.CREATOR);
                int networkId = data.readInt();
                ArrayList arrayList = new ArrayList();
                data.readTypedList(arrayList, MacAddress.CREATOR);
                if (!isCsiSnifferArpWhiteList(HwQueryCsiSnifferArp.getInstance(this.mClientModeImpl, this.mHwWifiProServiceManager), packageName)) {
                    HwHiLog.d(TAG, false, "handleRoamToNetwork: not in whitelist", new Object[0]);
                } else if (targetNetwork == null || targetNetwork.BSSID == null) {
                    HwHiLog.d(TAG, false, "handleRoamToNetwork: targetNetwork or bssid is null", new Object[0]);
                } else if (!arrayList.isEmpty()) {
                    HwHiLog.d(TAG, false, "handleRoamToNetwork: blockBssidList not support", new Object[0]);
                } else {
                    this.mClientModeImpl.startRoamToNetwork(networkId, targetNetwork);
                    isResult = IS_DEBUG_ON;
                }
            }
            reply.writeNoException();
            reply.writeBoolean(isResult);
            return IS_DEBUG_ON;
        }
    }

    private boolean isQueryArpHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isQueryArpHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_QUERY_ARP SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeString("");
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            String ret = "";
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                HwQueryCsiSnifferArp instance = HwQueryCsiSnifferArp.getInstance(this.mClientModeImpl, this.mHwWifiProServiceManager);
                if (isCsiSnifferArpWhiteList(instance, packageName)) {
                    List<String> ipList = new ArrayList<>();
                    data.readStringList(ipList);
                    ret = instance.getArpByIp(ipList);
                }
            }
            reply.writeNoException();
            reply.writeString(ret);
            return IS_DEBUG_ON;
        }
    }

    private boolean isSelfWifiInfoHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isSelfWifiInfoHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_DBAC_GET_SELF_WIFI_INFO SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeByteArray(new byte[0]);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            int cfgType = data.readInt();
            HwHiLog.i(TAG, false, "CODE_DBAC_GET_SELF_WIFI_INFO is called packageName=%{public}s", new Object[]{packageName});
            if (!isInCastOptWhiteList(packageName)) {
                Slog.e(TAG, "isSelfWifiInfoHandled: packageName is not in white list");
                reply.writeNoException();
                reply.writeByteArray(new byte[0]);
                return IS_DEBUG_ON;
            }
            byte[] resultArray = null;
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (castOptManager != null) {
                resultArray = castOptManager.getSelfWifiCfgInfo(cfgType);
            }
            reply.writeNoException();
            reply.writeByteArray(resultArray);
            return IS_DEBUG_ON;
        }
    }

    private boolean isPeerWifiInfoHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isPeerWifiInfoHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_DBAC_SET_PEER_WIFI_INFO SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            int cfgType = data.readInt();
            byte[] cfgData = data.createByteArray();
            HwHiLog.i(TAG, false, "packageName=%{public}s, cfgType=%{public}d, cfgData.length =%{public}d ", new Object[]{packageName, Integer.valueOf(cfgType), Integer.valueOf(cfgData.length)});
            if (!isInCastOptWhiteList(packageName)) {
                Slog.e(TAG, "isPeerWifiInfoHandled:packageName is not in white list");
                reply.writeNoException();
                reply.writeInt(-1);
                return IS_DEBUG_ON;
            }
            int result = -1;
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (castOptManager != null) {
                result = castOptManager.setPeerWifiCfgInfo(cfgType, cfgData);
            }
            reply.writeNoException();
            reply.writeInt(result);
            return IS_DEBUG_ON;
        }
    }

    private boolean isDbacRegisterCallbackHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isDbacRegisterCallbackHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_DBAC_CALLBACK_INFO SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            IWifiCfgCallback callback = IWifiCfgCallback.Stub.asInterface(data.readStrongBinder());
            HwHiLog.i(TAG, false, "CODE_DBAC_CALLBACK_INFO is called packageName=%{public}s", new Object[]{packageName});
            if (!isInCastOptWhiteList(packageName)) {
                Slog.e(TAG, "packageName is not in white list");
                reply.writeNoException();
                reply.writeInt(-1);
                return IS_DEBUG_ON;
            }
            int result = -1;
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (castOptManager != null) {
                result = castOptManager.registerWifiCfgCallback(callback);
                if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                    this.mClientModeImpl.setIgnoreScanInCastScene(IS_DEBUG_ON);
                }
            }
            reply.writeNoException();
            reply.writeInt(result);
            return IS_DEBUG_ON;
        }
    }

    private boolean isDbacUnregisterCallbackHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isDbacUnregisterCallbackHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_DBAC_UNREGISTER_CALLBACK SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            if (!isInCastOptWhiteList(data.readString())) {
                Slog.e(TAG, "packageName is not in white list");
                reply.writeNoException();
                reply.writeInt(-1);
                return IS_DEBUG_ON;
            }
            int result = -1;
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (castOptManager != null) {
                result = castOptManager.unregisterWifiCfgCallback();
                if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                    this.mClientModeImpl.setIgnoreScanInCastScene(false);
                }
            }
            reply.writeNoException();
            reply.writeInt(result);
            return IS_DEBUG_ON;
        }
    }

    private boolean isDbacRecommendChannelHandled(Parcel data, Parcel reply) {
        if (reply == null) {
            HwHiLog.e(TAG, false, "isDbacRecommendChannelHandled: reply is null", new Object[0]);
            return false;
        } else if (data == null || !isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_DBAC_GET_RECOMMEND_CHANNEL SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(0);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            HwHiLog.i(TAG, false, "CODE_DBAC_GET_RECOMMEND_CHANNEL is called packageName=%{public}s", new Object[]{packageName});
            if (!isInCastOptWhiteList(packageName)) {
                Slog.e(TAG, "packageName is not in white list");
                reply.writeNoException();
                reply.writeInt(0);
                return IS_DEBUG_ON;
            }
            int result = -1;
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (castOptManager != null) {
                result = castOptManager.getP2pRecommendChannel();
            }
            reply.writeNoException();
            reply.writeInt(result);
            return IS_DEBUG_ON;
        }
    }

    private boolean isSetWifiLowLatencyModeHandled(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "isSetWifiLowLatencyModeHandled: data or reply is null", new Object[0]);
            return false;
        } else if (!isSignMatchOrSystemApp()) {
            Slog.e(TAG, "CODE_SET_WIFI_LOW_LATENCY_MODE SIGNATURE_NO_MATCH or not systemApp");
            reply.writeNoException();
            reply.writeInt(-1);
            return IS_DEBUG_ON;
        } else {
            data.enforceInterface(DESCRIPTOR);
            enforceAccessPermission();
            String packageName = data.readString();
            boolean isEnabled = data.readInt() != 0;
            HwHiLog.i(TAG, false, "CODE_SET_WIFI_LOW_LATENCY_MODE is called packageName=%{public}s", new Object[]{packageName});
            if (!isInSetWifiLowLatencyModeWhiteList(packageName)) {
                Slog.e(TAG, "packageName is not in white list");
                reply.writeNoException();
                reply.writeInt(-1);
                return IS_DEBUG_ON;
            }
            int result = -1;
            if (this.mClientModeImpl instanceof HwWifiStateMachine) {
                result = this.mClientModeImpl.setWifiLowLatencyMode(packageName, isEnabled);
            }
            reply.writeNoException();
            reply.writeInt(result);
            return IS_DEBUG_ON;
        }
    }

    private boolean isPriorityTransmitHandled(Parcel data, Parcel reply) {
        if (data == null || reply == null) {
            HwHiLog.e(TAG, false, "isPriorityTransmitHandled: data or reply is null", new Object[0]);
            return false;
        }
        data.enforceInterface(DESCRIPTOR);
        int uid = data.readInt();
        int protocolType = data.readInt();
        int mode = data.readInt();
        HwHiLog.i(TAG, false, "setWifiSlicing uid = %{public}d, protocolType = %{public}d, mode = %{public}d", new Object[]{Integer.valueOf(uid), Integer.valueOf(protocolType), Integer.valueOf(mode)});
        HwQoEService qoeService = HwQoEService.getInstance();
        if (qoeService != null) {
            qoeService.setWifiSlicing(uid, protocolType, mode);
        }
        reply.writeNoException();
        return IS_DEBUG_ON;
    }

    private boolean disableWifiFilter(IBinder token) {
        if (token == null) {
            return false;
        }
        synchronized (this.mFilterSynchronizeLock) {
            if (findFilterIndex(token) >= 0) {
                HwHiSlog.d(TAG, false, "attempted to add filterlock when already holding one", new Object[0]);
                return false;
            }
            HwFilterLock filterLock = new HwFilterLock(token);
            try {
                token.linkToDeath(filterLock, 0);
                this.mFilterLockList.add(filterLock);
                return updateWifiFilterState();
            } catch (RemoteException e) {
                HwHiSlog.d(TAG, false, "Filter lock is already dead.", new Object[0]);
                return false;
            }
        }
    }

    private boolean enableWifiFilter(IBinder token) {
        if (token == null) {
            return false;
        }
        synchronized (this.mFilterSynchronizeLock) {
            int index = findFilterIndex(token);
            if (index < 0) {
                HwHiSlog.d(TAG, false, "cannot find wifi filter", new Object[0]);
                return false;
            }
            HwFilterLock filterLock = this.mFilterLockList.get(index);
            this.mFilterLockList.remove(index);
            filterLock.mToken.unlinkToDeath(filterLock, 0);
            return updateWifiFilterState();
        }
    }

    private boolean updateWifiFilterState() {
        synchronized (this.mFilterSynchronizeLock) {
            if (this.mFilterLockList.size() == 0) {
                if (this.mIsRxFilterDisabled) {
                    HwHiSlog.d(TAG, false, "enableWifiFilter", new Object[0]);
                    this.mIsRxFilterDisabled = false;
                    return this.mClientModeImpl.enableWifiFilter();
                }
            } else if (!this.mIsRxFilterDisabled) {
                HwHiSlog.d(TAG, false, "disableWifiFilter", new Object[0]);
                this.mIsRxFilterDisabled = IS_DEBUG_ON;
                return this.mClientModeImpl.disableWifiFilter();
            }
            return IS_DEBUG_ON;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFilterLockDeath(HwFilterLock filterLock) {
        HwHiSlog.d(TAG, false, "handleFilterLockDeath: lock=%{public}d", new Object[]{Integer.valueOf(Objects.hashCode(filterLock.mToken))});
        synchronized (this.mFilterSynchronizeLock) {
            int index = findFilterIndex(filterLock.mToken);
            if (index < 0) {
                HwHiSlog.d(TAG, false, "cannot find wifi filter", new Object[0]);
                return;
            }
            this.mFilterLockList.remove(index);
            updateWifiFilterState();
        }
    }

    private int findFilterIndex(IBinder token) {
        synchronized (this.mFilterSynchronizeLock) {
            int count = this.mFilterLockList.size();
            for (int i = 0; i < count; i++) {
                if (this.mFilterLockList.get(i).mToken == token) {
                    return i;
                }
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public final class HwFilterLock implements IBinder.DeathRecipient {
        public final IBinder mToken;

        HwFilterLock(IBinder token) {
            this.mToken = token;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HwWifiService.this.handleFilterLockDeath(this);
        }
    }

    private boolean isRequestWifiEnableSuccess(Parcel data) {
        HwHiSlog.e(TAG, false, "HwWifiService REQUEST_WIFI_ENABLE is waived!!!", new Object[0]);
        return false;
    }

    private boolean shouldExternWifiScanPeriodForP2p(Parcel data) {
        boolean isExtend = data.readInt() == 1;
        int iTimes = data.readInt();
        WifiConnectivityManager wifiConnectivityManager = wifiStateMachineUtils.getWifiConnectivityManager(this.mClientModeImpl);
        if (wifiConnectivityManager instanceof HwWifiConnectivityManager) {
            HwHiSlog.d(TAG, false, "HwWifiService  EXTEND_WIFI_SCAN_PERIOD_FOR_P2P: %{public}s, Times =%{public}d", new Object[]{String.valueOf(isExtend), Integer.valueOf(iTimes)});
            ((HwWifiConnectivityManager) wifiConnectivityManager).extendWifiScanPeriodForP2p(isExtend, iTimes);
            return IS_DEBUG_ON;
        }
        HwHiSlog.d(TAG, false, "EXTEND_WIFI_SCAN_PERIOD_FOR_P2P: Exception wifiConnectivityManager is not initialized", new Object[0]);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoWifiCloseForServiceDisconnect() {
        HwHiLog.d(TAG, false, "onVoWifiCloseForServiceDisconnect: send and cancel delayed message", new Object[0]);
        if (this.mMapconHandler.hasMessages(0)) {
            this.mMapconHandler.removeMessages(0);
            this.mClientModeImpl.sendMessage(this.mForgetNetworkMsg);
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller == null) {
            HwHiLog.d(TAG, false, "onVoWifiCloseForServiceDisconnect: get WifiController is NULL", new Object[0]);
            return;
        }
        if (this.mMapconHandler.hasMessages(1)) {
            this.mMapconHandler.removeMessages(1);
            controller.sendMessage(155657);
        }
        if (this.mMapconHandler.hasMessages(2)) {
            this.mMapconHandler.removeMessages(2);
            controller.sendMessage(155656);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoWifiCloseDoneForToggled() {
        HwHiLog.d(TAG, false, "onVoWifiCloseDone: cancel delayed message,send CMD_WIFI_TOGGLED", new Object[0]);
        if (this.mMapconHandler.hasMessages(2)) {
            this.mMapconHandler.removeMessages(2);
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller != null) {
            controller.sendMessage(155656);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoWifiCloseDoneForAirplaneToggled() {
        HwHiLog.d(TAG, false, "onVoWifiCloseDone: cancel delayed message, send CMD_AIRPLANE_TOGGLED", new Object[0]);
        if (this.mMapconHandler.hasMessages(1)) {
            this.mMapconHandler.removeMessages(1);
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller != null) {
            controller.sendMessage(155657);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoWifiCloseDoneForForgetNetwork() {
        HwHiLog.d(TAG, false, "onVoWifiCloseDone: cancel delayed message and send FORGET_NETWORK", new Object[0]);
        if (this.mMapconHandler.hasMessages(0)) {
            this.mMapconHandler.removeMessages(0);
        }
    }

    /* access modifiers changed from: protected */
    public void handleForgetNetwork(final Message msg) {
        WifiConfiguration currentWifiConfiguration = this.mClientModeImpl.getCurrentWifiConfiguration();
        HwHiLog.d(TAG, false, "handleForgetNetwork networkId = %{public}d", new Object[]{Integer.valueOf(msg.arg1)});
        if (!this.mIsVowifiServiceOn || currentWifiConfiguration == null || msg.arg1 != currentWifiConfiguration.networkId) {
            int currentNetId = -1;
            if (currentWifiConfiguration != null) {
                currentNetId = currentWifiConfiguration.networkId;
            }
            HwHiLog.d(TAG, false, "handleForgetNetwork current networkId = %{public}d", new Object[]{Integer.valueOf(currentNetId)});
            this.mClientModeImpl.sendMessage(Message.obtain(msg));
            return;
        }
        HwHiLog.d(TAG, false, "handleForgetNetwork enter.", new Object[0]);
        Handler handler = this.mMapconHandler;
        handler.sendMessageDelayed(handler.obtainMessage(0, msg), 5000);
        this.mForgetNetworkMsg = Message.obtain(msg);
        try {
            if (this.mMapconService != null) {
                this.mMapconService.notifyWifiOff(new IMapconServiceCallback.Stub() {
                    /* class com.android.server.wifi.HwWifiService.AnonymousClass10 */

                    public void onVoWifiCloseDone() {
                        HwWifiService.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        HwWifiService.this.onVoWifiCloseDoneForForgetNetwork();
                    }
                });
            } else if (this.mMtkMapconService != null) {
                this.mMtkMapconService.notifyWifiOff(new IMtkMapconServiceCallback.Stub() {
                    /* class com.android.server.wifi.HwWifiService.AnonymousClass11 */

                    public void onVoWifiCloseDone() {
                        HwWifiService.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        HwWifiService.this.onVoWifiCloseDoneForForgetNetwork();
                    }
                });
            } else {
                HwHiLog.d(TAG, false, "handleForgetNetwork: MapconService is null", new Object[0]);
            }
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "handleForgetNetwork Exception", new Object[0]);
            this.mClientModeImpl.sendMessage(Message.obtain(msg));
            onVoWifiCloseDoneForForgetNetwork();
        }
    }

    /* access modifiers changed from: protected */
    public void handleAirplaneModeToggled() {
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (this.mIsVowifiServiceOn) {
            if (this.mSettingsStore.isAirplaneModeOn()) {
                HwHiLog.d(TAG, false, "handleAirplaneModeToggled, sendMessageDelayed", new Object[0]);
                Handler handler = this.mMapconHandler;
                handler.sendMessageDelayed(handler.obtainMessage(1), 5000);
                try {
                    HwHiLog.d(TAG, false, "airplane mode enter, notify MapconService to shutdown", new Object[0]);
                    if (this.mMapconService != null) {
                        this.mMapconService.notifyWifiOff(this.mAirPlaneCallback);
                    } else if (this.mMtkMapconService != null) {
                        this.mMtkMapconService.notifyWifiOff(this.mMtkAirPlaneCallback);
                    } else {
                        HwHiLog.d(TAG, false, "handleAirplaneModeToggled: MapconService is null", new Object[0]);
                    }
                } catch (RemoteException e) {
                    HwHiLog.e(TAG, false, "handleAirplaneModeToggled failed", new Object[0]);
                }
            } else if (controller != null) {
                controller.sendMessage(155657);
            }
        } else if (controller != null) {
            controller.sendMessage(155657);
        }
    }

    /* access modifiers changed from: protected */
    public void setWifiEnabledAfterVoWifiOff(boolean enable) {
        HwHiLog.d(TAG, false, "setWifiEnabled %{public}s", new Object[]{String.valueOf(enable)});
        if (WifiProCommonUtils.isWifiSelfCuring() || !this.mIsVowifiServiceOn || getWifiEnabledState() != 3 || this.mSettingsStore.isWifiToggleEnabled()) {
            WifiController controller = wifiServiceUtils.getWifiController(this);
            if (controller != null) {
                controller.sendMessage(155656);
                return;
            }
            return;
        }
        HwHiLog.d(TAG, false, "setWifiEnabled: sendMessageDelayed", new Object[0]);
        Handler handler = this.mMapconHandler;
        handler.sendMessageDelayed(handler.obtainMessage(2), 5000);
        try {
            HwHiLog.d(TAG, false, "setWifiEnabled enter, notify MapconService to shutdown", new Object[0]);
            if (this.mMapconService != null) {
                this.mMapconService.notifyWifiOff(this.mCallback);
            } else if (this.mMtkMapconService != null) {
                this.mMtkMapconService.notifyWifiOff(this.mMtkCallback);
            } else {
                HwHiLog.d(TAG, false, "notifyWifiOff fail", new Object[0]);
                return;
            }
        } catch (RemoteException e) {
            HwHiLog.d(TAG, false, "notifyWifiOff Exception", new Object[0]);
        }
        while (this.mMapconHandler.hasMessages(2)) {
            try {
                HwHiLog.d(TAG, false, "setWifiEnabled ++++", new Object[0]);
                Thread.sleep(5);
            } catch (InterruptedException e2) {
                HwHiLog.d(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onReceiveEx(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            HwHiSlog.d(TAG, false, "onReceive, action:%{public}s", new Object[]{action});
            Boolean isVoWifiOn = Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false));
            if (isVoWifiOn.booleanValue() && ACTION_VOWIFI_STARTED.equals(action)) {
                this.mChipPlatform = 1;
                HwHiLog.d(TAG, false, "received broadcast ACTION_VOWIFI_STARTED, try to bind MapconService", new Object[0]);
                this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), this.conn, 1, UserHandle.OWNER);
            } else if (!isVoWifiOn.booleanValue() || !ACTION_VOWIFI_STARTED_MTK.equals(action)) {
                HwHiLog.d(TAG, false, "received wrong ACTION", new Object[0]);
            } else {
                this.mChipPlatform = 2;
                HwHiLog.d(TAG, false, "received broadcast ACTION_VOWIFI_STARTED_MTK, try to bind MtkMapconService", new Object[0]);
                this.mContext.bindServiceAsUser(new Intent().setClassName("com.mediatek.ims", "com.mediatek.ims.impl.MtkMapconService"), this.conn, 1, UserHandle.OWNER);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerForBroadcastsEx(IntentFilter intentFilter) {
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
            intentFilter.addAction(ACTION_VOWIFI_STARTED);
            intentFilter.addAction(ACTION_VOWIFI_STARTED_MTK);
        }
    }

    /* access modifiers changed from: protected */
    public boolean mdmForPolicyForceOpenWifi(boolean showToast, boolean enable) {
        if (!HwDeviceManager.disallowOp(52) || enable) {
            return false;
        }
        if (!showToast) {
            return IS_DEBUG_ON;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass12 */

            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(HwWifiService.this.mContext, HwWifiService.this.mContext.getString(33686052), 0).show();
            }
        });
        return IS_DEBUG_ON;
    }

    public void factoryReset(String packageName) {
        HwWifiService.super.factoryReset(packageName);
        if (SystemProperties.getBoolean("ro.config.hw_preset_ap", false)) {
            int i = Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
            boolean isWifiScanningAlwaysAvailable = IS_DEBUG_ON;
            if (i != 1) {
                isWifiScanningAlwaysAvailable = false;
            }
            if (isWifiScanningAlwaysAvailable) {
                Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
            }
            setWifiEnabled(this.mContext.getPackageName(), false);
            this.mNetworkResetHandler.postDelayed(new Runnable() {
                /* class com.android.server.wifi.HwWifiService.AnonymousClass13 */

                @Override // java.lang.Runnable
                public void run() {
                    HwWifiService.this.isWpaSupplicantConfRemoved();
                    HwWifiService hwWifiService = HwWifiService.this;
                    hwWifiService.setWifiEnabled(hwWifiService.mContext.getPackageName(), HwWifiService.IS_DEBUG_ON);
                }
            }, LOG_SCAN_RESULTS_INTERVAL_MS);
        }
    }

    /* access modifiers changed from: protected */
    public boolean startQuickttffScan(String packageName) {
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (!"com.huawei.lbs".equals(packageName) || ((castOptManager == null || !castOptManager.isCastOptWorking()) && Settings.Global.getInt(this.mContext.getContentResolver(), QTTFF_WIFI_SCAN_ENABLED, 0) != 1)) {
            return false;
        }
        HwHiSlog.d(TAG, false, "quickttff request  2.4G wifi scan", new Object[0]);
        if (this.lastScanResultsAvailableTime == 0 || this.mClock.getElapsedSinceBootMillis() - this.lastScanResultsAvailableTime >= 5000) {
            HwHiSlog.d(TAG, false, "Start 2.4G wifi scan.", new Object[0]);
            if (!WifiInjector.getInstance().getClientModeImplHandler().runWithScissors(new Runnable(packageName) {
                /* class com.android.server.wifi.$$Lambda$HwWifiService$bNOLyr8oakjuxLhdkXG9XdmZD4 */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifiService.this.lambda$startQuickttffScan$0$HwWifiService(this.f$1);
                }
            }, 0)) {
                HwHiLog.w(TAG, false, "Failed to post runnable to start scan in startQuickttffScan", new Object[0]);
                return false;
            }
        } else {
            HwHiSlog.d(TAG, false, "The scan results is fresh.", new Object[0]);
            Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
            intent.addFlags(67108864);
            intent.putExtra("resultsUpdated", IS_DEBUG_ON);
            intent.setPackage("com.huawei.lbs");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
        return IS_DEBUG_ON;
    }

    public /* synthetic */ void lambda$startQuickttffScan$0$HwWifiService(String packageName) {
        wifiStateMachineUtils.getScanRequestProxy(this.mClientModeImpl).startScanForSpecBand(Binder.getCallingUid(), packageName, 1);
    }

    private boolean isNeedIgnoreScanInGameAppMode(String packageName) {
        if (!this.mClientModeImpl.isDisconnected() && !TextUtils.isEmpty(packageName) && (this.mClientModeImpl instanceof HwWifiStateMachine)) {
            HwWifiStateMachine hwWifiStateMachine = this.mClientModeImpl;
            if (hwWifiStateMachine.isInGameAppMode() && !hwWifiStateMachine.isForgroundApp(packageName)) {
                Log.i(TAG, "Ignore this scan because the game is in progress at forground");
                return IS_DEBUG_ON;
            }
        }
        return false;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean limitForegroundWifiScanRequest(String packageName, int uid) {
        if (isPackageLimitScan(packageName) || isNeedIgnoreScanInGameAppMode(packageName)) {
            return IS_DEBUG_ON;
        }
        long id = Binder.clearCallingIdentity();
        try {
            NetLocationStrategy wifiScanStrategy = HwServiceFactory.getNetLocationStrategy(packageName, uid, 1);
            Binder.restoreCallingIdentity(id);
            if (wifiScanStrategy == null) {
                HwHiSlog.e(TAG, false, "Get wifiScanStrategy from iAware is null.", new Object[0]);
                return false;
            }
            HwHiSlog.d(TAG, false, "Get wifiScanStrategy from iAware, WifiScanStrategy = %{public}s", new Object[]{wifiScanStrategy.toString()});
            if (wifiScanStrategy.getCycle() == -1) {
                return IS_DEBUG_ON;
            }
            if (wifiScanStrategy.getCycle() == 0) {
                return false;
            }
            if (wifiScanStrategy.getCycle() <= 0) {
                HwHiSlog.e(TAG, false, "Invalid wifiScanStrategy.", new Object[0]);
                return false;
            } else if (this.lastScanResultsAvailableTime > wifiScanStrategy.getTimeStamp()) {
                long msSinceLastScan = this.mClock.getElapsedSinceBootMillis() - this.lastScanResultsAvailableTime;
                if (msSinceLastScan <= wifiScanStrategy.getCycle()) {
                    return IS_DEBUG_ON;
                }
                HwHiSlog.d(TAG, false, "Last scan started %{public}s ms ago, cann't limit current scan request.", new Object[]{String.valueOf(msSinceLastScan)});
                return false;
            } else {
                HwHiSlog.d(TAG, false, "Cann't limit current scan request, lastScanResultsAvailableTime = %{public}s", new Object[]{String.valueOf(this.lastScanResultsAvailableTime)});
                return false;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
    }

    private boolean isPackageLimitScan(String packageName) {
        if (!WifiInjector.getInstance().getClientModeImpl().isNeedIgnoreScan()) {
            CastOptManager castOptManager = CastOptManager.getInstance();
            if (!(castOptManager != null && castOptManager.isCastOptWorking())) {
                return false;
            }
            if ("com.huawei.lbs".equals(packageName)) {
                if (HwMssUtils.is1102A() || WifiCommonUtils.IS_TV) {
                    return IS_DEBUG_ON;
                }
                if (ScanResult.is24GHz(castOptManager.getStaFrequency())) {
                    return false;
                }
            }
            if ("com.android.settings".equals(packageName) || "com.huawei.homevision.settings".equals(packageName)) {
                return false;
            }
            HwHiLog.i(TAG, false, "Ignore this scan because cast is working packageName = %{public}s", new Object[]{packageName});
            return IS_DEBUG_ON;
        } else if ("com.huawei.homevision.settings".equals(packageName) || "com.android.settings".equals(packageName)) {
            return false;
        } else {
            HwHiLog.i(TAG, false, "Ignore this scan because miracast is working packageName = %{public}s", new Object[]{packageName});
            return IS_DEBUG_ON;
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldLimitWifiScanRequest(String packageName) {
        if (this.mWifiScanBlacklist.contains(packageName)) {
            return isGnssLocationFix();
        }
        return false;
    }

    private boolean isGnssLocationFix() {
        boolean isGnssLocationFix = Settings.Global.getInt(this.mContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) == 1;
        HwHiLog.d(TAG, false, "isGnssLocationFix =%{public}s", new Object[]{String.valueOf(isGnssLocationFix)});
        return isGnssLocationFix;
    }

    private void loadWifiScanBlacklist() {
        String[] blackList = this.mContext.getResources().getStringArray(33816593);
        this.mWifiScanBlacklist.clear();
        if (blackList != null) {
            this.mWifiScanBlacklist.addAll(Arrays.asList(blackList));
            HwHiLog.d(TAG, false, "mWifiScanBlacklist =%{public}s", new Object[]{this.mWifiScanBlacklist.toString()});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiScanblacklist() {
        this.mWifiScanBlacklist.clear();
        this.mWifiScanBlacklist.addAll(BackgroundAppScanManager.getInstance().getPackagBlackList());
    }

    /* access modifiers changed from: protected */
    public boolean shouldLimitWifiScanInAbsoluteRest(String packageName) {
        boolean isRequestFromBackground = isRequestFromBackground(packageName);
        HwHiLog.d(TAG, false, "mIsAbsoluteRest =%{public}s, mPluggedType =%{public}d, mHasScanned =%{public}s, isRequestFromBackground =%{public}s", new Object[]{String.valueOf(this.mIsAbsoluteRest), Integer.valueOf(this.mPluggedType), String.valueOf(this.mHasScanned), String.valueOf(isRequestFromBackground)});
        if (this.mIsAbsoluteRest && this.mPluggedType == 0 && isRequestFromBackground && this.mHasScanned) {
            return IS_DEBUG_ON;
        }
        this.mHasScanned = IS_DEBUG_ON;
        return false;
    }

    private boolean isRequestFromBackground(String packageName) {
        boolean z = false;
        if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1010 || TextUtils.isEmpty(packageName) || PROCESS_BD.equals(packageName) || PROCESS_GD.equals(packageName)) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), packageName);
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            if (this.mActivityManager.getPackageImportance(packageName) > 125) {
                z = IS_DEBUG_ON;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWpaSupplicantConfRemoved() {
        Object[] objArr;
        boolean ret = false;
        try {
            File conf = Environment.buildPath(Environment.getDataDirectory(), new String[]{"misc", "wifi", "wpa_supplicant.conf"});
            HwHiSlog.d(TAG, false, "conf path: %{public}s", new Object[]{conf.getPath()});
            if (conf.exists()) {
                ret = conf.delete();
            }
            objArr = new Object[]{String.valueOf(ret)};
        } catch (SecurityException e) {
            HwHiSlog.e(TAG, false, "delete conf error SecurityException", new Object[0]);
            objArr = new Object[]{String.valueOf(false)};
        } catch (Throwable th) {
            HwHiSlog.i(TAG, false, "delete conf result : %{public}s", new Object[]{String.valueOf(false)});
            throw th;
        }
        HwHiSlog.i(TAG, false, "delete conf result : %{public}s", objArr);
        return ret;
    }

    private boolean isSoftApEnabled() {
        if (wifiServiceUtils.getSoftApState(this).intValue() == 13) {
            return IS_DEBUG_ON;
        }
        return false;
    }

    private boolean isSignMatchOrSystemApp() {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        int matchResult = pm.checkSignatures(Binder.getCallingUid(), Process.myUid());
        if (matchResult == 0) {
            return IS_DEBUG_ON;
        }
        try {
            String pckName = getAppName(Binder.getCallingPid());
            if (pckName == null) {
                HwHiSlog.e(TAG, false, "pckName is null", new Object[0]);
                return false;
            }
            ApplicationInfo info = pm.getApplicationInfo(pckName, 0);
            if (info != null && (info.flags & 1) != 0) {
                return IS_DEBUG_ON;
            }
            HwHiSlog.d(TAG, false, "HwWifiService  isSignMatchOrSystemAppMatch matchRe=%{public}d pckName=%{public}s", new Object[]{Integer.valueOf(matchResult), pckName});
            return false;
        } catch (Exception e) {
            HwHiSlog.e(TAG, false, "isSystemApp not found", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void sendFailedScanDirectionalBroadcast(String packageName) {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
            intent.addFlags(67108864);
            intent.putExtra("resultsUpdated", false);
            intent.setPackage(packageName);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: protected */
    public List<ScanResult> getFilterScanResults(String callingPackage, int uid, List<ScanResult> scanResults) {
        List<ScanResult> scanResultsList = scanResults;
        Iterator<ScanResult> it = scanResultsList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ScanResult scanResult = it.next();
            if (scanResult.capabilities.contains(OWE_TRANSITION_IN_CAPABILITY) && scanResult.capabilities.contains(PMFR_IN_CAPABILITY) && !TextUtils.isEmpty(scanResult.SSID)) {
                scanResult.SSID = "";
                break;
            }
        }
        Collections.sort(scanResultsList, new Comparator<ScanResult>() {
            /* class com.android.server.wifi.HwWifiService.AnonymousClass14 */

            public int compare(ScanResult o1, ScanResult o2) {
                if (o1.timestamp > o2.timestamp) {
                    return -1;
                }
                if (o1.timestamp < o2.timestamp) {
                    return 1;
                }
                return 0;
            }
        });
        if (scanResultsList.size() > 200) {
            HwHiLog.d(TAG, false, "ScanResults exceed the max count. size = %{public}d", new Object[]{Integer.valueOf(scanResultsList.size())});
            scanResultsList = scanResultsList.subList(0, 200);
        }
        logScanResultsListRestrictively(callingPackage, scanResultsList);
        return scanResultsList;
    }

    private void logScanResultsListRestrictively(String callingPackage, List<ScanResult> scanResultsList) {
        long currentLogTime = this.mClock.getElapsedSinceBootMillis();
        if ("com.android.settings".equals(callingPackage) && scanResultsList != null && currentLogTime - this.mLastLogScanResultsTime > LOG_SCAN_RESULTS_INTERVAL_MS) {
            Set<String> ssids = new HashSet<>();
            StringBuilder sb = new StringBuilder();
            for (ScanResult scanResult : scanResultsList) {
                String ssid = scanResult.SSID;
                if (!ssids.contains(ssid)) {
                    ssids.add(ssid);
                    sb.append(StringUtilEx.safeDisplaySsid(ssid));
                    sb.append("|");
                    sb.append(scanResult.isHiLinkNetwork);
                    sb.append("|");
                    sb.append(scanResult.dot11vNetwork);
                    sb.append(" ");
                }
            }
            HwHiLog.d(TAG, false, "getScanResults: calling by %{public}s  includes: %{public}s", new Object[]{callingPackage, sb.toString()});
            this.mLastLogScanResultsTime = currentLogTime;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isScreenOff(String packageName) {
        HwHiSlog.d(TAG, false, "startScan, pid:%{public}d, uid:%{public}d, appName:%{public}s", new Object[]{Integer.valueOf(Binder.getCallingPid()), Integer.valueOf(Binder.getCallingUid()), packageName});
        if (this.mPowerManager.isScreenOn() || "com.huawei.ca".equals(packageName) || "com.huawei.parentcontrol".equals(packageName) || Binder.getCallingUid() == 1000 || "com.huawei.hidisk".equals(packageName)) {
            return false;
        }
        HwHiSlog.i(TAG, false, "Screen is off, %{public}s startScan is skipped.", new Object[]{packageName});
        return IS_DEBUG_ON;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiDualBandSupport() {
        int value = Settings.Global.getInt(this.mContext.getContentResolver(), "hw_wifi_ap_band", 0);
        if (value == 1) {
            return false;
        }
        if (value == 2) {
            return IS_DEBUG_ON;
        }
        return this.mContext.getResources().getBoolean(17891580);
    }

    /* access modifiers changed from: protected */
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

    /* access modifiers changed from: protected */
    public String getPackageName(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid && appProcess.pkgList != null && appProcess.pkgList.length > 0) {
                return appProcess.pkgList[0];
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiScanRequestRefused(String packageName) {
        if (shouldLimitWifiScanRequest(packageName)) {
            HwHiLog.d(TAG, false, "current scan request is refused %{public}s", new Object[]{packageName});
            sendFailedScanDirectionalBroadcast(packageName);
            return IS_DEBUG_ON;
        } else if (shouldLimitWifiScanInAbsoluteRest(packageName)) {
            HwHiLog.d(TAG, false, "absolute rest, scan request is refused %{public}s", new Object[]{packageName});
            sendFailedScanDirectionalBroadcast(packageName);
            return IS_DEBUG_ON;
        } else if (!restrictWifiScanRequest(packageName)) {
            return false;
        } else {
            HwHiSlog.i(TAG, false, "scan ctrl by PG, skip %{public}s", new Object[]{packageName});
            return IS_DEBUG_ON;
        }
    }

    /* access modifiers changed from: protected */
    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        synchronized (WIFI_LOCK) {
            if (this.mHwBehaviorManager == null) {
                this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
            }
        }
        IHwBehaviorCollectManager iHwBehaviorCollectManager = this.mHwBehaviorManager;
        if (iHwBehaviorCollectManager != null) {
            iHwBehaviorCollectManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
        } else {
            HwHiLog.w(TAG, false, "HwBehaviorCollectManager is null", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public class FactoryMacWhiteList {
        protected String mPackageName;
        protected int mVersionMax;
        protected int mVersionMin;

        public FactoryMacWhiteList(String packageName, int versionMin, int versionMax) {
            this.mPackageName = packageName;
            this.mVersionMin = versionMin;
            this.mVersionMax = versionMax;
        }

        /* access modifiers changed from: protected */
        public int getVersionMin() {
            return this.mVersionMin;
        }

        /* access modifiers changed from: protected */
        public int getVersionMax() {
            return this.mVersionMax;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAppInFactoryMacWhiteList(int uid) {
        Map<String, FactoryMacWhiteList> map;
        String packageName = this.mContext.getPackageManager().getNameForUid(uid);
        if (TextUtils.isEmpty(packageName) || (map = this.mFactoryMacWhiteListMap) == null || map.size() == 0 || !this.mFactoryMacWhiteListMap.containsKey(packageName)) {
            return false;
        }
        int versionCode = -1;
        FactoryMacWhiteList factoryMacWhiteList = this.mFactoryMacWhiteListMap.get(packageName);
        if (factoryMacWhiteList != null) {
            try {
                ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
                if (appInfo != null) {
                    versionCode = appInfo.versionCode;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "randommac, NameNotFoundException");
            }
            if ((factoryMacWhiteList.getVersionMin() == -1 && factoryMacWhiteList.getVersionMax() == -1) || (versionCode >= factoryMacWhiteList.getVersionMin() && versionCode <= factoryMacWhiteList.getVersionMax())) {
                Log.d(TAG, "randommac, packageName:" + packageName + " is in white list, versionCode = " + versionCode);
                return IS_DEBUG_ON;
            }
        }
        return false;
    }

    private void parseFactoryMacWhiteListFile(Context context) {
        String packageName = null;
        int versionMin = -1;
        InputStream inputStream = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            File whitelistFile = new File(CONF_FILE_NAME);
            if (!whitelistFile.exists()) {
                Log.e(TAG, "wifi factory mac whitelist doesn't exist");
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "randommac, IOException: close input stream error");
                    }
                }
            } else {
                InputStream inputStream2 = new FileInputStream(whitelistFile);
                parser.setInput(inputStream2, "UTF-8");
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType == 0) {
                        Log.d(TAG, "randommac, START_DOCUMENT");
                    } else if (eventType != 2) {
                        if (eventType != 3) {
                        }
                    } else if (XML_TAG_VERSION.equals(parser.getName())) {
                        printVersion(parser.nextText());
                    } else if (XML_TAG_PACKAGE.equals(parser.getName())) {
                        packageName = parser.nextText();
                    } else if ("version_min".equals(parser.getName())) {
                        try {
                            versionMin = Integer.parseInt(parser.nextText());
                        } catch (NumberFormatException e2) {
                            Log.e(TAG, "randommac, NumberFormatException");
                        }
                    } else if ("version_max".equals(parser.getName())) {
                        try {
                            updateFactoryWhiteList(packageName, versionMin, Integer.parseInt(parser.nextText()));
                        } catch (NumberFormatException e3) {
                            Log.e(TAG, "randommac, NumberFormatException");
                        }
                    } else {
                        Log.d(TAG, "no match xml tag.");
                    }
                }
                try {
                    inputStream2.close();
                } catch (IOException e4) {
                    Log.e(TAG, "randommac, IOException: close input stream error");
                }
            }
        } catch (IOException e5) {
            Log.e(TAG, "randommac, IOException: open file error");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (XmlPullParserException e6) {
            Log.e(TAG, "randommac, XmlPullParserException: prase file error");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e7) {
                    Log.e(TAG, "randommac, IOException: close input stream error");
                }
            }
            throw th;
        }
    }

    private void printVersion(String version) {
        Log.d(TAG, "randommac, wifi_factory_mac_whitelist VERSION = " + version);
    }

    private void updateFactoryWhiteList(String packageName, int versionMin, int versionMax) {
        if (!TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "randommac, packageName: " + packageName + ", versionMin: " + versionMin + ", versionMax: " + versionMax);
            this.mFactoryMacWhiteListMap.put(packageName, new FactoryMacWhiteList(packageName, versionMin, versionMax));
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWifiConfigChanged(WifiConfiguration newConfig, String packageName, boolean isBlacklistChanged) {
        WifiConfiguration oldConfig;
        if (this.mHwWifiChrService != null && newConfig != null && packageName != null && (oldConfig = getWifiApConfiguration()) != null) {
            int ssidTag = 0;
            if (oldConfig.SSID != null && !oldConfig.SSID.equals(newConfig.SSID)) {
                ssidTag = 1;
            }
            String securityType = SECURITY_NO_CHANGE;
            if (!(oldConfig.getAuthType() == newConfig.getAuthType() || WifiConfiguration.KeyMgmt.strings[newConfig.getAuthType()] == null)) {
                securityType = WifiConfiguration.KeyMgmt.strings[newConfig.getAuthType()];
            }
            int passwordTag = 0;
            if ((oldConfig.preSharedKey != null && !oldConfig.preSharedKey.equals(newConfig.preSharedKey)) || (oldConfig.preSharedKey == null && newConfig.preSharedKey != null)) {
                passwordTag = 1;
            }
            int apBand = -1;
            if (oldConfig.apBand != newConfig.apBand) {
                apBand = newConfig.apBand;
            }
            int apChannel = -1;
            if (oldConfig.apChannel != newConfig.apChannel) {
                apChannel = newConfig.apChannel;
            }
            int blacklistTag = 0;
            if (isBlacklistChanged) {
                blacklistTag = 1;
            }
            if (!SECURITY_NO_CHANGE.equals(securityType) || ssidTag != 0 || passwordTag != 0 || apBand != -1 || apChannel != -1 || blacklistTag != 0) {
                Bundle data = new Bundle();
                data.putString(KEY_APK_NAME, packageName);
                data.putString(KEY_SECURITY_TYPE, securityType);
                data.putInt(KEY_SSID_CHANGE, ssidTag);
                data.putInt(KEY_PASSWORD_CHANGE, passwordTag);
                data.putInt(KEY_BAND_CHANGE, apBand);
                data.putInt(KEY_CHANNEL_CHANGE, apChannel);
                data.putInt(KEY_BLACKLIST_CHANGE, blacklistTag);
                this.mHwWifiChrService.uploadDFTEvent(24, data);
            }
        }
    }

    private void notifyBlacklistEvent(String macFilter) {
        if (this.mHwWifiChrService != null && !TextUtils.isEmpty(this.mMacFilterRecord) && !TextUtils.isEmpty(macFilter) && !this.mMacFilterRecord.equals(macFilter)) {
            WifiConfiguration config = getWifiApConfiguration();
            String packageName = getAppName(Binder.getCallingPid());
            if (!(config == null || packageName == null)) {
                notifyWifiConfigChanged(config, packageName, IS_DEBUG_ON);
            }
        }
        this.mMacFilterRecord = macFilter;
    }

    private boolean isAllowAccessWifiService() {
        String packageName = getAppName(Binder.getCallingPid());
        for (String allowName : ACCESS_WIFI_WHITELIST) {
            if (allowName.equals(packageName)) {
                return IS_DEBUG_ON;
            }
        }
        return false;
    }

    private void enforceCallerPermission(String permission) {
        this.mContext.enforceCallingOrSelfPermission(permission, "WifiService");
    }

    /* access modifiers changed from: protected */
    public Bundle parseZcaller(String caller) {
        if (TextUtils.isEmpty(caller)) {
            return Bundle.EMPTY;
        }
        String[] infos = caller.split(";");
        if (infos == null || infos.length != 3) {
            return Bundle.EMPTY;
        }
        Bundle bundle = new Bundle();
        for (String info : infos) {
            String[] kv = info.split(":");
            if (kv == null || kv.length != 2) {
                return Bundle.EMPTY;
            }
            try {
                if ("uid".equals(kv[0])) {
                    bundle.putInt(kv[0], Integer.valueOf(kv[1]).intValue());
                } else if ("pid".equals(kv[0])) {
                    bundle.putInt(kv[0], Integer.valueOf(kv[1]).intValue());
                } else if (!AvSyncLatencyInfo.TAG_PKG_NAME.equals(kv[0]) || TextUtils.isEmpty(kv[1])) {
                    Log.d(TAG, "Not contain any valid info: " + caller);
                } else {
                    bundle.putString(kv[0], kv[1]);
                }
            } catch (NumberFormatException e) {
                return Bundle.EMPTY;
            }
        }
        if (bundle.size() == 3) {
            return bundle;
        }
        return Bundle.EMPTY;
    }

    /* access modifiers changed from: protected */
    public void handleAirplaneNotSensitiveWifi() {
        if (this.mSettingsStore.isAirplaneModeOn()) {
            HwHiLog.i(TAG, false, "Airplane mode toggled, shutdown all modes except wifi", new Object[0]);
            this.mActiveModeWarden.stopSoftAPMode(-1);
        }
    }
}
