package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hdm.HwDeviceManager;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.PPPOEConfig;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.os.Binder;
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
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.server.HwServiceFactory;
import com.android.server.PPPOEStateMachine;
import com.android.server.hidata.wavemapping.dataprovider.FrequentLocation;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.MSS.HwMSSHandler;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconServiceCallback;
import com.huawei.utils.reflect.EasyInvokeFactory;
import com.mediatek.wfo.impl.IMtkMapconService;
import com.mediatek.wfo.impl.IMtkMapconServiceCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class HwWifiService extends WifiServiceImpl {
    private static final String ACCESS_WIFI_FILTER_PERMISSION = "com.huawei.wifi.permission.ACCESS_FILTER";
    private static final String ACTION_VOWIFI_STARTED = "com.hisi.vowifi.started";
    private static final String ACTION_VOWIFI_STARTED_MTK = "com.huawei.vowifi.started";
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static final int CHIP_PLATFORM_HISI = 1;
    private static final int CHIP_PLATFORM_MTK = 2;
    private static final int CODE_DISABLE_RX_FILTER = 3021;
    private static final int CODE_ENABLE_HILINK_HANDSHAKE = 2001;
    private static final int CODE_ENABLE_RX_FILTER = 3022;
    private static final int CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P = 2006;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_CONNECTION_RAW_PSK = 2002;
    private static final int CODE_GET_PPPOE_INFO_CONFIG = 1004;
    private static final int CODE_GET_RSDB_SUPPORTED_MODE = 2008;
    private static final int CODE_GET_SINGNAL_INFO = 1011;
    private static final int CODE_GET_SOFTAP_CHANNEL_LIST = 1009;
    private static final int CODE_GET_SUPPORT_LIST = 3026;
    private static final int CODE_GET_VOWIFI_DETECT_MODE = 1013;
    private static final int CODE_GET_VOWIFI_DETECT_PERIOD = 1015;
    private static final int CODE_GET_WPA_SUPP_CONFIG = 1001;
    private static final int CODE_IS_BG_LIMIT_ALLOWED = 3008;
    private static final int CODE_IS_SUPPORT_VOWIFI_DETECT = 1016;
    private static final int CODE_NOTIFY_UI_EVENT = 3027;
    private static final int CODE_PROXY_WIFI_LOCK = 3009;
    private static final int CODE_REQUEST_FRESH_WHITE_LIST = 2007;
    private static final int CODE_REQUEST_WIFI_ENABLE = 2004;
    private static final int CODE_RESTRICT_WIFI_SCAN = 4001;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_VOWIFI_DETECT_MODE = 1012;
    private static final int CODE_SET_VOWIFI_DETECT_PERIOD = 1014;
    private static final int CODE_SET_WIFI_ANTSET = 3007;
    private static final int CODE_SET_WIFI_AP_EVALUATE_ENABLED = 1010;
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
    private static final int CODE_WIFI_QOE_EVALUATE = 3003;
    private static final int CODE_WIFI_QOE_START_MONITOR = 3001;
    private static final int CODE_WIFI_QOE_STOP_MONITOR = 3002;
    private static final int CODE_WIFI_QOE_UPDATE_STATUS = 3004;
    private static final boolean DBG = true;
    private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
    private static final int[] FREQUENCYS = {2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472};
    private static final String GNSS_LOCATION_FIX_STATUS = "GNSS_LOCATION_FIX_STATUS";
    private static final int MAPCON_SERVICE_SHUTDOWN_TIMEOUT = 5000;
    private static final int MSG_AIRPLANE_TOGGLED_MAPCON_TIMEOUT = 1;
    private static final int MSG_DISABLE_WIFI_MAPCON_TIMEOUT = 2;
    private static final int MSG_FORGET_NETWORK_MAPCON_TIMEOUT = 0;
    private static final String PG_AR_STATE_ACTION = "com.huawei.intent.action.PG_AR_STATE_ACTION";
    private static final String PG_RECEIVER_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final String PPPOE_TAG = "PPPOEWIFIService";
    private static final String PROCESS_BD = "com.baidu.map.location";
    private static final String PROCESS_GD = "com.amap.android.ams";
    private static final String QTTFF_WIFI_SCAN_ENABLED = "qttff_wifi_scan_enabled";
    private static final int QTTFF_WIFI_SCAN_INTERVAL_MS = 5000;
    private static final String TAG = "HwWifiService";
    private static final String VOWIFI_WIFI_DETECT_PERMISSION = "com.huawei.permission.VOWIFI_WIFI_DETECT";
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    private static final int WIFISCANSTRATEGY_ALLOWABLE = 0;
    private static final int WIFISCANSTRATEGY_FORBIDDEN = -1;
    private static HashSet<String> restrictWifiScanPkgSet = new HashSet<>();
    /* access modifiers changed from: private */
    public static WifiServiceUtils wifiServiceUtils = EasyInvokeFactory.getInvokeUtils(WifiServiceUtils.class);
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private final ServiceConnection conn;
    private boolean isPPPOE;
    private volatile boolean isRxFilterDisabled;
    /* access modifiers changed from: private */
    public long lastScanResultsAvailableTime;
    private final ActivityManager mActivityManager;
    private final IMapconServiceCallback mAirPlaneCallback;
    private final AppOpsManager mAppOps;
    private final IMapconServiceCallback mCallback;
    /* access modifiers changed from: private */
    public int mChipPlatform;
    /* access modifiers changed from: private */
    public final Clock mClock;
    /* access modifiers changed from: private */
    public Context mContext;
    private List<HwFilterLock> mFilterLockList;
    private final Object mFilterSynchronizeLock;
    private Message mForgetNetworkMsg;
    /* access modifiers changed from: private */
    public boolean mHasScanned;
    /* access modifiers changed from: private */
    public boolean mIsAbsoluteRest;
    /* access modifiers changed from: private */
    public Handler mMapconHandler;
    /* access modifiers changed from: private */
    public HandlerThread mMapconHandlerTread;
    /* access modifiers changed from: private */
    public IMapconService mMapconService;
    private final IMtkMapconServiceCallback mMtkAirPlaneCallback;
    private final IMtkMapconServiceCallback mMtkCallback;
    /* access modifiers changed from: private */
    public IMtkMapconService mMtkMapconService;
    private Handler mNetworkResetHandler;
    private PPPOEStateMachine mPPPOEStateMachine;
    /* access modifiers changed from: private */
    public int mPluggedType;
    private PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public boolean mVowifiServiceOn;
    private WifiProStateMachine mWifiProStateMachine;
    private final ArraySet<String> mWifiScanBlacklist;

    private final class HwFilterLock implements IBinder.DeathRecipient {
        public final IBinder mToken;

        HwFilterLock(IBinder token) {
            this.mToken = token;
        }

        public void binderDied() {
            HwWifiService.this.handleFilterLockDeath(this);
        }
    }

    public HwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        super(context, wifiInjector, asyncChannel);
        this.isPPPOE = SystemProperties.getInt("ro.config.pppoe_enable", 0) != 1 ? false : true;
        this.lastScanResultsAvailableTime = 0;
        this.mWifiScanBlacklist = new ArraySet<>();
        this.mIsAbsoluteRest = false;
        this.mHasScanned = false;
        this.mFilterLockList = new ArrayList();
        this.isRxFilterDisabled = false;
        this.mFilterSynchronizeLock = new Object();
        this.mMapconService = null;
        this.mMtkMapconService = null;
        this.conn = new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {
                Log.d(HwWifiService.TAG, "onServiceDisconnected,IMapconService");
                IMapconService unused = HwWifiService.this.mMapconService = null;
                if (HwWifiService.this.mChipPlatform == 2) {
                    HwWifiService.this.onVoWifiCloseForServiceDisconnect();
                    IMtkMapconService unused2 = HwWifiService.this.mMtkMapconService = null;
                }
                boolean unused3 = HwWifiService.this.mVowifiServiceOn = false;
                HwWifiService.this.mMapconHandlerTread.quit();
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(HwWifiService.TAG, "onServiceConnected,IMapconService and chip platform is " + HwWifiService.this.mChipPlatform);
                if (HwWifiService.this.mChipPlatform == 1) {
                    IMapconService unused = HwWifiService.this.mMapconService = IMapconService.Stub.asInterface(service);
                } else if (HwWifiService.this.mChipPlatform == 2) {
                    IMtkMapconService unused2 = HwWifiService.this.mMtkMapconService = IMtkMapconService.Stub.asInterface(service);
                }
                HandlerThread unused3 = HwWifiService.this.mMapconHandlerTread = new HandlerThread("MapconHandler");
                HwWifiService.this.mMapconHandlerTread.start();
                Handler unused4 = HwWifiService.this.mMapconHandler = new Handler(HwWifiService.this.mMapconHandlerTread.getLooper()) {
                    public void handleMessage(Message msg) {
                        Log.d(HwWifiService.TAG, "handle TimeoutMessage,msg:" + msg.what);
                        WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                        switch (msg.what) {
                            case 0:
                                HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain((Message) msg.obj));
                                return;
                            case 1:
                                if (controller != null) {
                                    controller.sendMessage(155657);
                                    return;
                                }
                                return;
                            case 2:
                                if (controller != null) {
                                    controller.sendMessage(155656);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    }
                };
                boolean unused5 = HwWifiService.this.mVowifiServiceOn = true;
            }
        };
        this.mCallback = new IMapconServiceCallback.Stub() {
            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForToggled();
            }
        };
        this.mMtkCallback = new IMtkMapconServiceCallback.Stub() {
            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForToggled();
            }
        };
        this.mAirPlaneCallback = new IMapconServiceCallback.Stub() {
            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForAirplaneToggled();
            }
        };
        this.mMtkAirPlaneCallback = new IMtkMapconServiceCallback.Stub() {
            public void onVoWifiCloseDone() {
                HwWifiService.this.onVoWifiCloseDoneForAirplaneToggled();
            }
        };
        this.mNetworkResetHandler = new Handler();
        this.mContext = context;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        if (this.isPPPOE) {
            this.mPPPOEStateMachine = new PPPOEStateMachine(this.mContext, PPPOE_TAG);
            this.mPPPOEStateMachine.start();
        }
        this.mClock = wifiInjector.getClock();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction()) && intent.getBooleanExtra("resultsUpdated", false)) {
                    long unused = HwWifiService.this.lastScanResultsAvailableTime = HwWifiService.this.mClock.getElapsedSinceBootMillis();
                }
            }
        }, filter);
        loadWifiScanBlacklist();
        BackgroundAppScanManager.getInstance().registerBlackListChangeListener(new BlacklistListener() {
            public void onBlacklistChange(List<String> list) {
                HwWifiService.this.updateWifiScanblacklist();
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    boolean unused = HwWifiService.this.mIsAbsoluteRest = intent.getBooleanExtra("stationary", false);
                    Log.d(HwWifiService.TAG, "mIsAbsoluteRest =" + HwWifiService.this.mIsAbsoluteRest);
                    if (HwWifiService.this.mIsAbsoluteRest) {
                        boolean unused2 = HwWifiService.this.mHasScanned = false;
                    }
                }
            }
        }, new IntentFilter(PG_AR_STATE_ACTION), PG_RECEIVER_PERMISSION, null);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    int unused = HwWifiService.this.mPluggedType = intent.getIntExtra("plugged", 0);
                    if (HwWifiService.this.mPluggedType == 0 && (HwWifiService.this.mWifiStateMachine instanceof HwWifiStateMachine) && HwWifiService.this.mWifiStateMachine.getChargingState()) {
                        int unused2 = HwWifiService.this.mPluggedType = 2;
                    }
                    Log.d(HwWifiService.TAG, "mPluggedType =" + HwWifiService.this.mPluggedType);
                }
            }
        }, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    /* access modifiers changed from: protected */
    public boolean enforceStopScanSreenOff() {
        if (this.mPowerManager.isScreenOn() || "com.huawei.ca".equals(getAppName(Binder.getCallingPid()))) {
            return false;
        }
        Slog.i(TAG, "Screen is off, " + getAppName(Binder.getCallingPid()) + " startScan is skipped.");
        return true;
    }

    private void restrictWifiScan(List<String> pkgs, Boolean restrict) {
        if (restrict.booleanValue()) {
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
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.net.wifi.PPPOEConfig} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v86, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v58, resolved type: android.net.wifi.PPPOEConfig} */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        String macFilter;
        String mac;
        int mode_running;
        int reserve1_running;
        int reserve2_running;
        PPPOEConfig _arg0 = null;
        boolean enableHiLink = false;
        switch (code) {
            case CODE_GET_WPA_SUPP_CONFIG /*1001*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService  CODE_GET_WPA_SUPP_CONFIG SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeString(null);
                    return true;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  getWpaSuppConfig");
                String result = "";
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result = this.mWifiStateMachine.getWpaSuppConfig();
                }
                reply.writeNoException();
                reply.writeString(result);
                return true;
            case CODE_START_PPPOE_CONFIG /*1002*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService  CODE_START_PPPOE_CONFIG SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  startPPPOE");
                if (!this.isPPPOE) {
                    Slog.w(TAG, "the PPPOE function is closed.");
                    return false;
                }
                if (data.readInt() != 0) {
                    _arg0 = PPPOEConfig.CREATOR.createFromParcel(data);
                }
                this.mPPPOEStateMachine.sendMessage(589825, _arg0);
                reply.writeNoException();
                return true;
            case CODE_STOP_PPPOE_CONFIG /*1003*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService  CODE_STOP_PPPOE_CONFIG SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  stopPPPOE");
                if (!this.isPPPOE) {
                    Slog.w(TAG, "the PPPOE function is closed.");
                    return false;
                }
                this.mPPPOEStateMachine.sendMessage(589826);
                reply.writeNoException();
                return true;
            case CODE_GET_PPPOE_INFO_CONFIG /*1004*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService  CODE_GET_PPPOE_INFO_CONFIG SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeInt(0);
                    reply.writeNoException();
                    return true;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  get PPPOE info");
                if (!this.isPPPOE) {
                    Slog.w(TAG, "the PPPOE function is closed.");
                    return false;
                }
                PPPOEInfo _result = this.mPPPOEStateMachine.getPPPOEInfo();
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                reply.writeNoException();
                return true;
            case CODE_GET_APLINKED_STA_LIST /*1005*/:
                Slog.d(TAG, "Receive CODE_GET_APLINKED_STA_LIST");
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService  CODE_GET_APLINKED_STA_LIST SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeStringList(null);
                    return true;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                List<String> result2 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    HwWifiStateMachine mHwWifiStateMachine = this.mWifiStateMachine;
                    if (isSoftApEnabled()) {
                        result2 = mHwWifiStateMachine.getApLinkedStaList();
                        if (result2 == null) {
                            Slog.d(TAG, "getApLinkedStaList result = null");
                        } else {
                            Slog.d(TAG, "getApLinkedStaList result = " + result2.toString().replaceAll("\\.[\\d]{1,3}\\.[\\d]{1,3}\\.", ".*.*.").replaceAll(":[\\w]{1,}:[\\w]{1,}:", ":**:**:"));
                        }
                    } else {
                        Slog.w(TAG, "Receive CODE_GET_APLINKED_STA_LIST when softap state is not enabled");
                    }
                }
                reply.writeNoException();
                reply.writeStringList(result2);
                return true;
            case CODE_SET_SOFTAP_MACFILTER /*1006*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService  CODE_SET_SOFTAP_MACFILTER SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "Receive CODE_SET_SOFTAP_MACFILTER, macFilter:" + macFilter);
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    HwWifiStateMachine mHwWifiStateMachine2 = this.mWifiStateMachine;
                    if (isSoftApEnabled()) {
                        mHwWifiStateMachine2.setSoftapMacFilter(macFilter);
                    } else {
                        Slog.w(TAG, "Receive CODE_SET_SOFTAP_MACFILTER when softap state is not enabled");
                    }
                }
                reply.writeNoException();
                return true;
            case CODE_SET_SOFTAP_DISASSOCIATESTA /*1007*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_SET_SOFTAP_DISASSOCIATESTA SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "Receive CODE_SET_SOFTAP_DISASSOCIATESTA, mac = " + mac);
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    HwWifiStateMachine mHwWifiStateMachine3 = this.mWifiStateMachine;
                    if (isSoftApEnabled()) {
                        mHwWifiStateMachine3.setSoftapDisassociateSta(mac);
                    } else {
                        Slog.w(TAG, "Receive CODE_SET_SOFTAP_DISASSOCIATESTA when softap state is not enabled");
                    }
                }
                reply.writeNoException();
                return true;
            case CODE_USER_HANDOVER_WIFI /*1008*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_USER_HANDOVER_WIFI SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "HwWifiService  userHandoverWiFi ");
                this.mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (this.mWifiProStateMachine != null) {
                    this.mWifiProStateMachine.userHandoverWifi();
                }
                reply.writeNoException();
                return true;
            case CODE_GET_SOFTAP_CHANNEL_LIST /*1009*/:
                Slog.d(TAG, "Receive CODE_GET_SOFTAP_CHANNEL_LIST");
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_GET_SOFTAP_CHANNEL_LIST SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeIntArray(null);
                    return true;
                }
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                int[] _result2 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    _result2 = this.mWifiStateMachine.getSoftApChannelListFor5G();
                }
                reply.writeNoException();
                reply.writeIntArray(_result2);
                return true;
            case CODE_SET_WIFI_AP_EVALUATE_ENABLED /*1010*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_SET_WIFI_AP_EVALUATE_ENABLED SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                Slog.d(TAG, "HwWifiService  SET_WIFI_AP_EVALUATE_ENABLED ");
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                if (data.readInt() == 1) {
                    enableHiLink = true;
                }
                boolean enablen = enableHiLink;
                this.mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (this.mWifiProStateMachine != null) {
                    this.mWifiProStateMachine.setWifiApEvaluateEnabled(enablen);
                }
                reply.writeNoException();
                return true;
            case CODE_GET_SINGNAL_INFO /*1011*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_GET_SINGNAL_INFO SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeByteArray(null);
                    return false;
                }
                Slog.d(TAG, "HwWifiService  FETCH_WIFI_SIGNAL_INFO_FOR_VOWIFI ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "fetchWifiSignalInfoForVoWiFi(): permissin deny");
                    return false;
                }
                byte[] result3 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result3 = this.mWifiStateMachine.fetchWifiSignalInfoForVoWiFi();
                }
                reply.writeNoException();
                reply.writeByteArray(result3);
                return true;
            case CODE_SET_VOWIFI_DETECT_MODE /*1012*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_SET_VOWIFI_DETECT_MODE SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                Slog.d(TAG, "HwWifiService  SET_VOWIFI_DETECT_MODE ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "setVoWifiDetectMode(): permissin deny");
                    return false;
                }
                if (data.readInt() != 0) {
                    _arg0 = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(data);
                }
                PPPOEConfig pPPOEConfig = _arg0;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    this.mWifiStateMachine.setVoWifiDetectMode(pPPOEConfig);
                }
                reply.writeNoException();
                return true;
            case CODE_GET_VOWIFI_DETECT_MODE /*1013*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_GET_VOWIFI_DETECT_MODE SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeInt(0);
                    return false;
                }
                Slog.d(TAG, "HwWifiService  GET_VOWIFI_DETECT_MODE ");
                data.enforceInterface(DESCRIPTOR);
                WifiDetectConfInfo result4 = null;
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "getVoWifiDetectMode(): permissin deny");
                    return false;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result4 = this.mWifiStateMachine.getVoWifiDetectMode();
                }
                reply.writeNoException();
                if (result4 != null) {
                    reply.writeInt(1);
                    result4.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case CODE_SET_VOWIFI_DETECT_PERIOD /*1014*/:
                if (checkSignMatchOrIsSystemApp() == 0) {
                    Slog.e(TAG, "WifiService CODE_SET_VOWIFI_DETECT_PERIOD SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    return false;
                }
                Slog.d(TAG, "HwWifiService  SET_VOWIFI_DETECT_PERIOD ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "setVoWifiDetectPeriod(): permissin deny");
                    return false;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    this.mWifiStateMachine.setVoWifiDetectPeriod(data.readInt());
                }
                reply.writeNoException();
                return true;
            case CODE_GET_VOWIFI_DETECT_PERIOD /*1015*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_GET_VOWIFI_DETECT_PERIOD SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeNoException();
                    reply.writeInt(-1);
                    return false;
                }
                Slog.d(TAG, "HwWifiService  GET_VOWIFI_DETECT_PERIOD ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "getVoWifiDetectPeriod(): permissin deny");
                    return false;
                }
                int result5 = -1;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result5 = this.mWifiStateMachine.getVoWifiDetectPeriod();
                }
                reply.writeNoException();
                reply.writeInt(result5);
                return true;
            case CODE_IS_SUPPORT_VOWIFI_DETECT /*1016*/:
                if (!checkSignMatchOrIsSystemApp()) {
                    Slog.e(TAG, "WifiService CODE_IS_SUPPORT_VOWIFI_DETECT SIGNATURE_NO_MATCH or not systemApp");
                    return false;
                }
                Slog.d(TAG, "HwWifiService  IS_SUPPORT_VOWIFI_DETECT ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "isSupportVoWifiDetect(): permissin deny");
                    return false;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    HwWifiStateMachine mHwWifiStateMachine4 = this.mWifiStateMachine;
                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                        boolean _result3 = mHwWifiStateMachine4.syncGetSupportedVoWifiDetect(wifiServiceUtils.getWifiStateMachineChannel(this));
                    } else {
                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                    }
                }
                reply.writeNoException();
                reply.writeBooleanArray(new boolean[]{true});
                return true;
            default:
                switch (code) {
                    case CODE_ENABLE_HILINK_HANDSHAKE /*2001*/:
                        if (!checkSignMatchOrIsSystemApp()) {
                            Slog.e(TAG, "WifiService CODE_ENABLE_HILINK_HANDSHAKE SIGNATURE_NO_MATCH or not systemApp");
                            return false;
                        }
                        data.enforceInterface(DESCRIPTOR);
                        enforceAccessPermission();
                        if (data.readInt() == 1) {
                            enableHiLink = true;
                        }
                        this.mWifiStateMachine.enableHiLinkHandshake(enableHiLink, data.readString());
                        return true;
                    case CODE_GET_CONNECTION_RAW_PSK /*2002*/:
                        if (!checkSignMatchOrIsSystemApp()) {
                            Slog.e(TAG, "WifiService CODE_GET_CONNECTION_RAW_PSK SIGNATURE_NO_MATCH or not systemApp");
                            reply.writeNoException();
                            reply.writeString(null);
                            return true;
                        }
                        data.enforceInterface(DESCRIPTOR);
                        enforceAccessPermission();
                        String _result4 = null;
                        if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                            _result4 = this.mWifiStateMachine.getConnectionRawPsk();
                        }
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    default:
                        switch (code) {
                            case CODE_REQUEST_WIFI_ENABLE /*2004*/:
                                if (checkSignMatchOrIsSystemApp() == 0) {
                                    Slog.e(TAG, "WifiService REQUEST_WIFI_ENABLE SIGNATURE_NO_MATCH or not systemApp");
                                    return false;
                                }
                                data.enforceInterface(DESCRIPTOR);
                                enforceAccessPermission();
                                Slog.d(TAG, "HwWifiService REQUEST_WIFI_ENABLE");
                                return requestWifiEnable(data);
                            case CODE_SET_WIFI_TXPOWER /*2005*/:
                                if (!checkSignMatchOrIsSystemApp()) {
                                    Slog.e(TAG, "WifiService CODE_SET_WIFI_TXPOWER SIGNATURE_NO_MATCH or not systemApp");
                                    reply.writeNoException();
                                    reply.writeInt(-1);
                                    return true;
                                }
                                data.enforceInterface(DESCRIPTOR);
                                enforceAccessPermission();
                                int result6 = WifiInjector.getInstance().getWifiNative().setWifiTxPowerHw(data.readInt());
                                reply.writeNoException();
                                reply.writeInt(result6);
                                return true;
                            case CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P /*2006*/:
                                if (checkSignMatchOrIsSystemApp() == 0) {
                                    Slog.e(TAG, "WifiService EXTEND_WIFI_SCAN_PERIOD_FOR_P2P SIGNATURE_NO_MATCH or not systemApp");
                                    return false;
                                }
                                data.enforceInterface(DESCRIPTOR);
                                enforceAccessPermission();
                                Slog.d(TAG, "HwWifiService  EXTEND_WIFI_SCAN_PERIOD_FOR_P2P");
                                return externWifiScanPeriodForP2p(data);
                            case CODE_REQUEST_FRESH_WHITE_LIST /*2007*/:
                                if (!checkSignMatchOrIsSystemApp()) {
                                    Slog.e(TAG, "WifiService CODE_REQUEST_FRESH_WHITE_LIST SIGNATURE_NO_MATCH or not systemApp");
                                    reply.writeNoException();
                                    return false;
                                }
                                data.enforceInterface(DESCRIPTOR);
                                enforceAccessPermission();
                                int type = data.readInt();
                                List<String> packageWhiteList = new ArrayList<>();
                                data.readStringList(packageWhiteList);
                                if (type == 7) {
                                    HwQoEService qoeService_wifiSleep = HwQoEService.getInstance();
                                    if (qoeService_wifiSleep != null) {
                                        qoeService_wifiSleep.updateWifiSleepWhiteList(type, packageWhiteList);
                                    }
                                } else {
                                    BackgroundAppScanManager.getInstance().refreshPackageWhitelist(type, packageWhiteList);
                                }
                                reply.writeNoException();
                                return true;
                            case CODE_GET_RSDB_SUPPORTED_MODE /*2008*/:
                                if (!checkSignMatchOrIsSystemApp()) {
                                    Slog.e(TAG, "WifiService CODE_GET_RSDB_SUPPORTED_MODE SIGNATURE_NO_MATCH or not systemApp");
                                    reply.writeNoException();
                                    reply.writeBooleanArray(new boolean[]{false});
                                    return true;
                                }
                                data.enforceInterface(DESCRIPTOR);
                                enforceAccessPermission();
                                boolean result7 = false;
                                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                                    HwWifiStateMachine mHwWifiStateMachine5 = this.mWifiStateMachine;
                                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                                        result7 = mHwWifiStateMachine5.isRSDBSupported();
                                    } else {
                                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                                    }
                                }
                                reply.writeNoException();
                                reply.writeBooleanArray(new boolean[]{result7});
                                return true;
                            default:
                                switch (code) {
                                    case CODE_WIFI_QOE_START_MONITOR /*3001*/:
                                        if (!checkSignMatchOrIsSystemApp()) {
                                            Slog.e(TAG, "WifiService CODE_WIFI_QOE_START_MONITOR SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeInt(0);
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        enforceAccessPermission();
                                        boolean result8 = false;
                                        int monitorType = data.readInt();
                                        int period = data.readInt();
                                        IHwQoECallback callback = IHwQoECallback.Stub.asInterface(data.readStrongBinder());
                                        HwQoEService mHwQoEService = HwQoEService.getInstance();
                                        if (mHwQoEService != null) {
                                            result8 = mHwQoEService.registerHwQoEMonitor(monitorType, period, callback);
                                        }
                                        reply.writeNoException();
                                        if (result8) {
                                            enableHiLink = true;
                                        }
                                        reply.writeInt(enableHiLink);
                                        return true;
                                    case CODE_WIFI_QOE_STOP_MONITOR /*3002*/:
                                        if (!checkSignMatchOrIsSystemApp()) {
                                            Slog.e(TAG, "WifiService CODE_WIFI_QOE_START_MONITOR SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeInt(0);
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        enforceAccessPermission();
                                        boolean result9 = false;
                                        int monitorType2 = data.readInt();
                                        HwQoEService mHwQoEService2 = HwQoEService.getInstance();
                                        if (mHwQoEService2 != null) {
                                            result9 = mHwQoEService2.unRegisterHwQoEMonitor(monitorType2);
                                        }
                                        reply.writeNoException();
                                        if (result9) {
                                            enableHiLink = true;
                                        }
                                        reply.writeInt(enableHiLink);
                                        return true;
                                    case CODE_WIFI_QOE_EVALUATE /*3003*/:
                                        if (!checkSignMatchOrIsSystemApp()) {
                                            Slog.e(TAG, "WifiService CODE_WIFI_QOE_EVALUATE SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeInt(0);
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        enforceAccessPermission();
                                        IHwQoECallback callback2 = IHwQoECallback.Stub.asInterface(data.readStrongBinder());
                                        boolean result10 = false;
                                        HwQoEService mHwQoEService3 = HwQoEService.getInstance();
                                        if (mHwQoEService3 != null) {
                                            result10 = mHwQoEService3.evaluateNetworkQuality(callback2);
                                        }
                                        reply.writeNoException();
                                        if (result10) {
                                            enableHiLink = true;
                                        }
                                        reply.writeInt(enableHiLink);
                                        return true;
                                    case CODE_WIFI_QOE_UPDATE_STATUS /*3004*/:
                                        if (checkSignMatchOrIsSystemApp() == 0) {
                                            Slog.e(TAG, "WifiService CODE_WIFI_QOE_UPDATE_STATUS SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeInt(0);
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        enforceAccessPermission();
                                        boolean result11 = false;
                                        int state = data.readInt();
                                        HwQoEService mHwQoEService4 = HwQoEService.getInstance();
                                        if (mHwQoEService4 != null) {
                                            result11 = mHwQoEService4.updateVOWIFIState(state);
                                        }
                                        reply.writeNoException();
                                        if (result11) {
                                            enableHiLink = true;
                                        }
                                        reply.writeInt(enableHiLink);
                                        return true;
                                    case CODE_UPDATE_APP_RUNNING_STATUS /*3005*/:
                                        if (checkSignMatchOrIsSystemApp() == 0) {
                                            Slog.e(TAG, "WifiService CODE_UPDATE_APP_RUNNING_STATUS SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeInt(0);
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        int uid_running = data.readInt();
                                        int type_running = data.readInt();
                                        int status_running = data.readInt();
                                        int scene_running = data.readInt();
                                        int readInt = data.readInt();
                                        Slog.d(TAG, " updateAppRunningStatus  uid:" + uid_running + ", type:" + type_running + ",status:" + status_running + "scene: " + scene_running);
                                        reply.writeNoException();
                                        reply.writeInt(1);
                                        return true;
                                    case CODE_UPDATE_APP_EXPERIENCE_STATUS /*3006*/:
                                        if (!checkSignMatchOrIsSystemApp()) {
                                            Slog.e(TAG, "WifiService CODE_UPDATE_APP_EXPERIENCE_STATUS SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeInt(0);
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        int uid_experience = data.readInt();
                                        int experience = data.readInt();
                                        long rtt_experience = data.readLong();
                                        int readInt2 = data.readInt();
                                        Slog.d(TAG, "updateAppExperienceStatus  uid:" + uid_experience + ", experience:" + experience + ",rtt:" + rtt_experience);
                                        reply.writeNoException();
                                        reply.writeInt(1);
                                        return true;
                                    case CODE_SET_WIFI_ANTSET /*3007*/:
                                        if (checkSignMatchOrIsSystemApp() == 0) {
                                            Slog.e(TAG, "WifiService CODE_SET_WIFI_ANTSET SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            return false;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        enforceAccessPermission();
                                        String iface = data.readString();
                                        int mode = data.readInt();
                                        int op = data.readInt();
                                        HwMSSHandler mssHandler = HwMSSHandler.getInstance();
                                        if (mssHandler != null) {
                                            mssHandler.setWifiAnt(iface, mode, op);
                                            Slog.d(TAG, "mssHandler hwSetWifiAnt");
                                        }
                                        reply.writeNoException();
                                        return true;
                                    case CODE_IS_BG_LIMIT_ALLOWED /*3008*/:
                                        if (!checkSignMatchOrIsSystemApp()) {
                                            Slog.e(TAG, "WifiService CODE_IS_BG_LIMIT_ALLOWED SIGNATURE_NO_MATCH or not systemApp");
                                            reply.writeNoException();
                                            reply.writeBooleanArray(new boolean[]{false});
                                            return true;
                                        }
                                        data.enforceInterface(DESCRIPTOR);
                                        int uid = data.readInt();
                                        boolean result12 = false;
                                        HwQoEService qoeService_check_bg_limit = HwQoEService.getInstance();
                                        if (qoeService_check_bg_limit != null) {
                                            result12 = qoeService_check_bg_limit.isBgLimitAllowed(uid);
                                        }
                                        reply.writeNoException();
                                        reply.writeBooleanArray(new boolean[]{result12});
                                        return true;
                                    default:
                                        switch (code) {
                                            case CODE_DISABLE_RX_FILTER /*3021*/:
                                                data.enforceInterface(DESCRIPTOR);
                                                if (this.mContext.checkCallingPermission(ACCESS_WIFI_FILTER_PERMISSION) != 0) {
                                                    Slog.d(TAG, "disableWifiFilter: No ACCESS_FILTER permission");
                                                    return false;
                                                } else if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
                                                    Slog.d(TAG, "you have no permission to call disableWifiFilter from uid:" + Binder.getCallingUid());
                                                    return false;
                                                } else {
                                                    Slog.i(TAG, "call binder disableWifiFilter " + getAppName(Binder.getCallingPid()));
                                                    boolean result13 = disableWifiFilter(data.readStrongBinder());
                                                    reply.writeNoException();
                                                    reply.writeBooleanArray(new boolean[]{result13});
                                                    return true;
                                                }
                                            case CODE_ENABLE_RX_FILTER /*3022*/:
                                                data.enforceInterface(DESCRIPTOR);
                                                if (this.mContext.checkCallingPermission(ACCESS_WIFI_FILTER_PERMISSION) != 0) {
                                                    Slog.d(TAG, "enableWifiFilter: No ACCESS_FILTER permission");
                                                    return false;
                                                } else if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
                                                    Slog.d(TAG, "you have no permission to call enableWifiFilter from uid:" + Binder.getCallingUid());
                                                    return false;
                                                } else {
                                                    Slog.i(TAG, "call binder enableWifiFilter " + getAppName(Binder.getCallingPid()));
                                                    boolean result14 = enableWifiFilter(data.readStrongBinder());
                                                    reply.writeNoException();
                                                    reply.writeBooleanArray(new boolean[]{result14});
                                                    return true;
                                                }
                                            case CODE_START_WIFI_KEEP_ALIVE /*3023*/:
                                                if (!checkSignMatchOrIsSystemApp()) {
                                                    Slog.e(TAG, "WifiService CODE_START_WIFI_KEEP_ALIVE SIGNATURE_NO_MATCH or not systemApp");
                                                    reply.writeNoException();
                                                    reply.writeInt(0);
                                                    return true;
                                                }
                                                data.enforceInterface(DESCRIPTOR);
                                                enforceAccessPermission();
                                                Message msg = (Message) Message.CREATOR.createFromParcel(data);
                                                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                                                    this.mWifiStateMachine.startPacketKeepalive(msg);
                                                }
                                                reply.writeNoException();
                                                reply.writeInt(1);
                                                return true;
                                            case CODE_STOP_WIFI_KEEP_ALIVE /*3024*/:
                                                if (checkSignMatchOrIsSystemApp() == 0) {
                                                    Slog.e(TAG, "WifiService CODE_START_WIFI_KEEP_ALIVE SIGNATURE_NO_MATCH or not systemApp");
                                                    reply.writeNoException();
                                                    reply.writeInt(0);
                                                    return true;
                                                }
                                                data.enforceInterface(DESCRIPTOR);
                                                enforceAccessPermission();
                                                Message msg2 = (Message) Message.CREATOR.createFromParcel(data);
                                                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                                                    this.mWifiStateMachine.stopPacketKeepalive(msg2);
                                                }
                                                reply.writeNoException();
                                                reply.writeInt(1);
                                                return true;
                                            case CODE_UPDATE_LIMIT_SPEED_STATUS /*3025*/:
                                                data.enforceInterface(DESCRIPTOR);
                                                Slog.d(TAG, " updatelimitSpeedStatus mode: " + mode_running + ", reserve1: " + reserve1_running + " reserve2: " + reserve2_running);
                                                boolean result15 = false;
                                                HwQoEService qoeService_app_running = HwQoEService.getInstance();
                                                if (qoeService_app_running != null) {
                                                    result15 = qoeService_app_running.updatelimitSpeedStatus(mode_running, reserve1_running, reserve2_running);
                                                }
                                                reply.writeNoException();
                                                reply.writeBooleanArray(new boolean[]{result15});
                                                return true;
                                            case CODE_GET_SUPPORT_LIST /*3026*/:
                                                data.enforceInterface(DESCRIPTOR);
                                                Slog.d(TAG, "getSupportList");
                                                List<String> result16 = new ArrayList<>();
                                                HwQoEService qoeService_app_running2 = HwQoEService.getInstance();
                                                if (qoeService_app_running2 != null) {
                                                    result16 = qoeService_app_running2.getSupportList();
                                                }
                                                reply.writeNoException();
                                                reply.writeStringList(result16);
                                                return true;
                                            case CODE_NOTIFY_UI_EVENT /*3027*/:
                                                data.enforceInterface(DESCRIPTOR);
                                                int event = data.readInt();
                                                Slog.d(TAG, "notifyUIEvent");
                                                HwQoEService qoeService_app_running3 = HwQoEService.getInstance();
                                                if (qoeService_app_running3 != null) {
                                                    qoeService_app_running3.notifyUIEvent(event);
                                                }
                                                reply.writeNoException();
                                                return true;
                                            default:
                                                switch (code) {
                                                    case CODE_RESTRICT_WIFI_SCAN /*4001*/:
                                                        if (!checkSignMatchOrIsSystemApp()) {
                                                            Slog.e(TAG, "WifiService  CODE_RESTRICT_WIFI_SCAN SIGNATURE_NO_MATCH or not systemApp");
                                                            reply.writeNoException();
                                                            return false;
                                                        }
                                                        data.enforceInterface(DESCRIPTOR);
                                                        enforceAccessPermission();
                                                        List<String> pkgs = new ArrayList<>();
                                                        data.readStringList(pkgs);
                                                        if (pkgs.size() == 0) {
                                                            pkgs = null;
                                                        }
                                                        if (data.readInt() != 0) {
                                                            enableHiLink = true;
                                                        }
                                                        restrictWifiScan(pkgs, Boolean.valueOf(enableHiLink));
                                                        return true;
                                                    case CODE_UPDATE_WM_FREQ_LOC /*4002*/:
                                                        boolean result17 = false;
                                                        if (!checkSignMatchOrIsSystemApp()) {
                                                            Slog.e(TAG, "WifiService  CODE_UPDATE_WM_FREQ_LOC SIGNATURE_NO_MATCH or not systemApp");
                                                            reply.writeNoException();
                                                            reply.writeInt(0);
                                                            return false;
                                                        }
                                                        data.enforceInterface(DESCRIPTOR);
                                                        enforceAccessPermission();
                                                        int location = data.readInt();
                                                        int action = data.readInt();
                                                        FrequentLocation mFrequentLocation = FrequentLocation.getInstance();
                                                        if (mFrequentLocation != null) {
                                                            result17 = mFrequentLocation.updateWaveMapping(location, action);
                                                        }
                                                        reply.writeNoException();
                                                        if (result17) {
                                                            enableHiLink = true;
                                                        }
                                                        reply.writeInt(enableHiLink);
                                                        return true;
                                                    default:
                                                        return HwWifiService.super.onTransact(code, data, reply, flags);
                                                }
                                        }
                                }
                        }
                }
        }
    }

    private boolean disableWifiFilter(IBinder token) {
        if (token == null) {
            return false;
        }
        synchronized (this.mFilterSynchronizeLock) {
            if (findFilterIndex(token) >= 0) {
                Slog.d(TAG, "attempted to add filterlock when already holding one");
                return false;
            }
            HwFilterLock filterLock = new HwFilterLock(token);
            try {
                token.linkToDeath(filterLock, 0);
                this.mFilterLockList.add(filterLock);
                boolean updateWifiFilterState = updateWifiFilterState();
                return updateWifiFilterState;
            } catch (RemoteException e) {
                Slog.d(TAG, "Filter lock is already dead.");
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
                Slog.d(TAG, "cannot find wifi filter");
                return false;
            }
            HwFilterLock filterLock = this.mFilterLockList.get(index);
            this.mFilterLockList.remove(index);
            filterLock.mToken.unlinkToDeath(filterLock, 0);
            boolean updateWifiFilterState = updateWifiFilterState();
            return updateWifiFilterState;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        return true;
     */
    private boolean updateWifiFilterState() {
        synchronized (this.mFilterSynchronizeLock) {
            if (this.mFilterLockList.size() == 0) {
                if (this.isRxFilterDisabled) {
                    Slog.d(TAG, "enableWifiFilter");
                    this.isRxFilterDisabled = false;
                    boolean enableWifiFilter = this.mWifiStateMachine.enableWifiFilter();
                    return enableWifiFilter;
                }
            } else if (!this.isRxFilterDisabled) {
                Slog.d(TAG, "disableWifiFilter");
                this.isRxFilterDisabled = true;
                boolean disableWifiFilter = this.mWifiStateMachine.disableWifiFilter();
                return disableWifiFilter;
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFilterLockDeath(HwFilterLock filterLock) {
        Slog.d(TAG, "handleFilterLockDeath: lock=" + Objects.hashCode(filterLock.mToken));
        synchronized (this.mFilterSynchronizeLock) {
            int index = findFilterIndex(filterLock.mToken);
            if (index < 0) {
                Slog.d(TAG, "cannot find wifi filter");
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

    private boolean requestWifiEnable(Parcel data) {
        Slog.e(TAG, "HwWifiService REQUEST_WIFI_ENABLE is waived!!!");
        return false;
    }

    private boolean externWifiScanPeriodForP2p(Parcel data) {
        boolean bExtend = data.readInt() == 1;
        int iTimes = data.readInt();
        WifiConnectivityManager wifiConnectivityManager = wifiStateMachineUtils.getWifiConnectivityManager(this.mWifiStateMachine);
        if (wifiConnectivityManager == null || !(wifiConnectivityManager instanceof HwWifiConnectivityManager)) {
            Slog.d(TAG, "EXTEND_WIFI_SCAN_PERIOD_FOR_P2P: Exception wifiConnectivityManager is not initialized");
            return false;
        }
        Slog.d(TAG, "HwWifiService  EXTEND_WIFI_SCAN_PERIOD_FOR_P2P: " + bExtend + ", Times =" + iTimes);
        ((HwWifiConnectivityManager) wifiConnectivityManager).extendWifiScanPeriodForP2p(bExtend, iTimes);
        return true;
    }

    /* access modifiers changed from: private */
    public void onVoWifiCloseForServiceDisconnect() {
        Log.d(TAG, "onVoWifiCloseForServiceDisconnect: send and cancel delayed message");
        if (this.mMapconHandler.hasMessages(0)) {
            this.mMapconHandler.removeMessages(0);
            this.mWifiStateMachine.sendMessage(this.mForgetNetworkMsg);
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller != null) {
            if (this.mMapconHandler.hasMessages(1)) {
                this.mMapconHandler.removeMessages(1);
                controller.sendMessage(155657);
            }
            if (this.mMapconHandler.hasMessages(2)) {
                this.mMapconHandler.removeMessages(2);
                controller.sendMessage(155656);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onVoWifiCloseDoneForToggled() {
        Log.d(TAG, "onVoWifiCloseDone: cancel delayed message,send CMD_WIFI_TOGGLED");
        if (this.mMapconHandler.hasMessages(2)) {
            this.mMapconHandler.removeMessages(2);
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller != null) {
            controller.sendMessage(155656);
        }
    }

    /* access modifiers changed from: private */
    public void onVoWifiCloseDoneForAirplaneToggled() {
        Log.d(TAG, "onVoWifiCloseDone: cancel delayed message, send CMD_AIRPLANE_TOGGLED");
        if (this.mMapconHandler.hasMessages(1)) {
            this.mMapconHandler.removeMessages(1);
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller != null) {
            controller.sendMessage(155657);
        }
    }

    /* access modifiers changed from: private */
    public void onVoWifiCloseDoneForForgetNetwork() {
        Log.d(TAG, "onVoWifiCloseDone: cancel delayed message and send FORGET_NETWORK");
        if (this.mMapconHandler.hasMessages(0)) {
            this.mMapconHandler.removeMessages(0);
        }
    }

    /* access modifiers changed from: protected */
    public void handleForgetNetwork(final Message msg) {
        WifiConfiguration currentWifiConfiguration = this.mWifiStateMachine.getCurrentWifiConfiguration();
        Log.d(TAG, "handleForgetNetwork networkId = " + msg.arg1);
        if (!this.mVowifiServiceOn || currentWifiConfiguration == null || msg.arg1 != currentWifiConfiguration.networkId) {
            int currentNetId = -1;
            if (currentWifiConfiguration != null) {
                currentNetId = currentWifiConfiguration.networkId;
            }
            Log.d(TAG, "handleForgetNetwork current networkId = " + currentNetId);
            this.mWifiStateMachine.sendMessage(Message.obtain(msg));
            return;
        }
        Log.d(TAG, "handleForgetNetwork enter.");
        this.mMapconHandler.sendMessageDelayed(this.mMapconHandler.obtainMessage(0, msg), 5000);
        this.mForgetNetworkMsg = Message.obtain(msg);
        try {
            if (this.mMapconService != null) {
                this.mMapconService.notifyWifiOff(new IMapconServiceCallback.Stub() {
                    public void onVoWifiCloseDone() {
                        HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        HwWifiService.this.onVoWifiCloseDoneForForgetNetwork();
                    }
                });
            } else if (this.mMtkMapconService != null) {
                this.mMtkMapconService.notifyWifiOff(new IMtkMapconServiceCallback.Stub() {
                    public void onVoWifiCloseDone() {
                        HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        HwWifiService.this.onVoWifiCloseDoneForForgetNetwork();
                    }
                });
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Exception:", e);
            this.mWifiStateMachine.sendMessage(Message.obtain(msg));
            onVoWifiCloseDoneForForgetNetwork();
        }
    }

    /* access modifiers changed from: protected */
    public void handleAirplaneModeToggled() {
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (this.mVowifiServiceOn) {
            if (this.mSettingsStore.isAirplaneModeOn()) {
                Log.d(TAG, "handleAirplaneModeToggled, sendMessageDelayed");
                this.mMapconHandler.sendMessageDelayed(this.mMapconHandler.obtainMessage(1), 5000);
                try {
                    Log.d(TAG, "airplane mode enter, notify MapconService to shutdown");
                    if (this.mMapconService != null) {
                        this.mMapconService.notifyWifiOff(this.mAirPlaneCallback);
                    } else if (this.mMtkMapconService != null) {
                        this.mMtkMapconService.notifyWifiOff(this.mMtkAirPlaneCallback);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
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
        Log.d(TAG, "setWifiEnabled " + enable);
        if (WifiProCommonUtils.isWifiSelfCuring() || !this.mVowifiServiceOn || 3 != getWifiEnabledState() || this.mSettingsStore.isWifiToggleEnabled()) {
            WifiController controller = wifiServiceUtils.getWifiController(this);
            if (controller != null) {
                controller.sendMessage(155656);
                return;
            }
            return;
        }
        Log.d(TAG, "setWifiEnabled: sendMessageDelayed");
        this.mMapconHandler.sendMessageDelayed(this.mMapconHandler.obtainMessage(2), 5000);
        try {
            Log.d(TAG, "setWifiEnabled enter, notify MapconService to shutdown");
            if (this.mMapconService != null) {
                this.mMapconService.notifyWifiOff(this.mCallback);
            } else if (this.mMtkMapconService != null) {
                this.mMtkMapconService.notifyWifiOff(this.mMtkCallback);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "notifyWifiOff" + e.toString());
        }
        while (this.mMapconHandler.hasMessages(2)) {
            try {
                Log.d(TAG, "setWifiEnabled ++++");
                Thread.sleep(5);
            } catch (InterruptedException e2) {
                Log.d(TAG, e2.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onReceiveEx(Context context, Intent intent) {
        String action = intent.getAction();
        Slog.d(TAG, "onReceive, action:" + action);
        Boolean isVoWifiOn = Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false));
        if (isVoWifiOn.booleanValue() && ACTION_VOWIFI_STARTED.equals(action)) {
            this.mChipPlatform = 1;
            Log.d(TAG, "received broadcast ACTION_VOWIFI_STARTED, try to bind MapconService");
            this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), this.conn, 1, UserHandle.OWNER);
        } else if (isVoWifiOn.booleanValue() && ACTION_VOWIFI_STARTED_MTK.equals(action)) {
            this.mChipPlatform = 2;
            Log.d(TAG, "received broadcast ACTION_VOWIFI_STARTED_MTK, try to bind MtkMapconService");
            this.mContext.bindServiceAsUser(new Intent().setClassName("com.mediatek.wfo.impl", "com.mediatek.wfo.impl.MtkMapconService"), this.conn, 1, UserHandle.OWNER);
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
        if (showToast) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(HwWifiService.this.mContext, HwWifiService.this.mContext.getString(33686052), 0).show();
                }
            });
        }
        return true;
    }

    public void factoryReset(String packageName) {
        HwWifiService.super.factoryReset(packageName);
        if (SystemProperties.getBoolean("ro.config.hw_preset_ap", false)) {
            boolean isWifiScanningAlwaysAvailable = true;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 1) {
                isWifiScanningAlwaysAvailable = false;
            }
            if (isWifiScanningAlwaysAvailable) {
                Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
            }
            try {
                setWifiEnabled(this.mContext.getPackageName(), false);
            } catch (RemoteException e) {
                Slog.e(TAG, "setWifiEnabled false exception: " + e.getMessage());
            }
            this.mNetworkResetHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean unused = HwWifiService.this.removeWpaSupplicantConf();
                    try {
                        HwWifiService.this.setWifiEnabled(HwWifiService.this.mContext.getPackageName(), true);
                    } catch (RemoteException e) {
                        Slog.e(HwWifiService.TAG, "setWifiEnabled true exception: " + e.getMessage());
                    }
                }
            }, 3000);
        }
    }

    /* access modifiers changed from: protected */
    public boolean startQuickttffScan(String packageName) {
        if (!"com.huawei.lbs".equals(packageName) || Settings.Global.getInt(this.mContext.getContentResolver(), QTTFF_WIFI_SCAN_ENABLED, 0) != 1) {
            return false;
        }
        Slog.d(TAG, "quickttff request  2.4G wifi scan");
        if (this.lastScanResultsAvailableTime == 0 || this.mClock.getElapsedSinceBootMillis() - this.lastScanResultsAvailableTime >= 5000) {
            Slog.d(TAG, "Start 2.4G wifi scan.");
            if (!WifiInjector.getInstance().getWifiStateMachineHandler().runWithScissors(new Runnable(packageName) {
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    HwWifiService.wifiStateMachineUtils.getScanRequestProxy(HwWifiService.this.mWifiStateMachine).startScanForSpecBand(Binder.getCallingUid(), this.f$1, 1);
                }
            }, 0)) {
                Log.w(TAG, "Failed to post runnable to start scan in startQuickttffScan");
                return false;
            }
        } else {
            Slog.d(TAG, "The scan results is fresh.");
            Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
            intent.addFlags(67108864);
            intent.putExtra("resultsUpdated", true);
            intent.setPackage("com.huawei.lbs");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean limitForegroundWifiScanRequest(String packageName, int uid) {
        long id = Binder.clearCallingIdentity();
        try {
            NetLocationStrategy wifiScanStrategy = HwServiceFactory.getNetLocationStrategy(packageName, uid, 1);
            Binder.restoreCallingIdentity(id);
            if (wifiScanStrategy == null) {
                Slog.e(TAG, "Get wifiScanStrategy from iAware is null.");
                return false;
            }
            Slog.d(TAG, "Get wifiScanStrategy from iAware, WifiScanStrategy = " + wifiScanStrategy);
            if (wifiScanStrategy.getCycle() == -1) {
                return true;
            }
            if (wifiScanStrategy.getCycle() == 0) {
                return false;
            }
            if (wifiScanStrategy.getCycle() <= 0) {
                Slog.e(TAG, "Invalid wifiScanStrategy.");
                return false;
            } else if (this.lastScanResultsAvailableTime > wifiScanStrategy.getTimeStamp()) {
                long msSinceLastScan = this.mClock.getElapsedSinceBootMillis() - this.lastScanResultsAvailableTime;
                if (msSinceLastScan <= wifiScanStrategy.getCycle()) {
                    return true;
                }
                Slog.d(TAG, "Last scan started " + msSinceLastScan + "ms ago, cann't limit current scan request.");
                return false;
            } else {
                Slog.d(TAG, "Cann't limit current scan request, lastScanResultsAvailableTime = " + this.lastScanResultsAvailableTime);
                return false;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public boolean limitWifiScanRequest(String packageName) {
        if (this.mWifiScanBlacklist.contains(packageName)) {
            return isGnssLocationFix();
        }
        return false;
    }

    private boolean isGnssLocationFix() {
        boolean isGnssLocationFix;
        boolean isGnssLocationFix2 = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) != 1) {
            isGnssLocationFix2 = false;
        }
        Log.d(TAG, "isGnssLocationFix =" + isGnssLocationFix);
        return isGnssLocationFix;
    }

    private void loadWifiScanBlacklist() {
        String[] blackList = this.mContext.getResources().getStringArray(33816585);
        this.mWifiScanBlacklist.clear();
        if (blackList != null) {
            this.mWifiScanBlacklist.addAll(Arrays.asList(blackList));
            Log.d(TAG, "mWifiScanBlacklist =" + this.mWifiScanBlacklist);
        }
    }

    /* access modifiers changed from: private */
    public void updateWifiScanblacklist() {
        this.mWifiScanBlacklist.clear();
        this.mWifiScanBlacklist.addAll(BackgroundAppScanManager.getInstance().getPackagBlackList());
    }

    /* access modifiers changed from: protected */
    public boolean limitWifiScanInAbsoluteRest(String packageName) {
        boolean requestFromBackground = isRequestFromBackground(packageName);
        Log.d(TAG, "mIsAbsoluteRest =" + this.mIsAbsoluteRest + ", mPluggedType =" + this.mPluggedType + ", mHasScanned =" + this.mHasScanned + ", requestFromBackground =" + requestFromBackground);
        if (this.mIsAbsoluteRest && this.mPluggedType == 0 && requestFromBackground && this.mHasScanned) {
            return true;
        }
        this.mHasScanned = true;
        return false;
    }

    private boolean isRequestFromBackground(String packageName) {
        boolean z = false;
        if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == CODE_SET_WIFI_AP_EVALUATE_ENABLED || TextUtils.isEmpty(packageName) || PROCESS_BD.equals(packageName) || PROCESS_GD.equals(packageName)) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), packageName);
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            if (this.mActivityManager.getPackageImportance(packageName) > 125) {
                z = true;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    public boolean removeWpaSupplicantConf() {
        StringBuilder sb;
        String str;
        boolean ret = false;
        try {
            File conf = Environment.buildPath(Environment.getDataDirectory(), new String[]{"misc", "wifi", "wpa_supplicant.conf"});
            Slog.d(TAG, "conf path: " + conf.getPath());
            if (conf.exists()) {
                ret = conf.delete();
            }
            str = TAG;
            sb = new StringBuilder();
        } catch (SecurityException e) {
            Slog.e(TAG, "delete conf error : " + e.getMessage());
            str = TAG;
            sb = new StringBuilder();
        } catch (Throwable th) {
            Slog.i(TAG, "delete conf result : " + false);
            throw th;
        }
        sb.append("delete conf result : ");
        sb.append(ret);
        Slog.i(str, sb.toString());
        return ret;
    }

    private boolean isSoftApEnabled() {
        return wifiServiceUtils.getSoftApState(this).intValue() == 13;
    }

    private boolean checkSignMatchOrIsSystemApp() {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        int matchResult = pm.checkSignatures(Binder.getCallingUid(), Process.myUid());
        if (matchResult == 0) {
            return true;
        }
        try {
            String pckName = getAppName(Binder.getCallingPid());
            if (pckName == null) {
                Slog.e(TAG, "pckName is null");
                return false;
            }
            ApplicationInfo info = pm.getApplicationInfo(pckName, 0);
            if (info != null && (info.flags & 1) != 0) {
                return true;
            }
            Slog.d(TAG, "HwWifiService  checkSignMatchOrIsSystemAppMatch matchRe=" + matchResult + "pckName=" + pckName);
            return false;
        } catch (Exception ex) {
            Slog.e(TAG, "isSystemApp not found app" + "" + "exception=" + ex.toString());
            return false;
        }
    }
}
