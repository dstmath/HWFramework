package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hdm.HwDeviceManager;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.PPPOEConfig;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.ScanSettings;
import android.net.wifi.WifiChannel;
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
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.rms.iaware.NetLocationStrategy;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.server.HwServiceFactory;
import com.android.server.PPPOEStateMachine;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconService.Stub;
import com.hisi.mapcon.IMapconServiceCallback;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwWifiService extends WifiServiceImpl {
    private static final String ACTION_VOWIFI_STARTED = "com.hisi.vowifi.started";
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static final int CODE_ENABLE_HILINK_HANDSHAKE = 2001;
    private static final int CODE_EXTEND_WIFI_SCAN_PERIOD_FOR_P2P = 2006;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_CONNECTION_RAW_PSK = 2002;
    private static final int CODE_GET_PPPOE_INFO_CONFIG = 1004;
    private static final int CODE_GET_RSDB_SUPPORTED_MODE = 2008;
    private static final int CODE_GET_SINGNAL_INFO = 1011;
    private static final int CODE_GET_SOFTAP_CHANNEL_LIST = 1009;
    private static final int CODE_GET_VOWIFI_DETECT_MODE = 1013;
    private static final int CODE_GET_VOWIFI_DETECT_PERIOD = 1015;
    private static final int CODE_GET_WPA_SUPP_CONFIG = 1001;
    private static final int CODE_IS_BG_LIMIT_ALLOWED = 3008;
    private static final int CODE_IS_SUPPORT_VOWIFI_DETECT = 1016;
    private static final int CODE_PROXY_WIFI_LOCK = 3009;
    private static final int CODE_REQUEST_FRESH_WHITE_LIST = 2007;
    private static final int CODE_REQUEST_WIFI_ENABLE = 2004;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_VOWIFI_DETECT_MODE = 1012;
    private static final int CODE_SET_VOWIFI_DETECT_PERIOD = 1014;
    private static final int CODE_SET_WIFI_AP_EVALUATE_ENABLED = 1010;
    private static final int CODE_SET_WIFI_TXPOWER = 2005;
    private static final int CODE_START_PPPOE_CONFIG = 1002;
    private static final int CODE_STOP_PPPOE_CONFIG = 1003;
    private static final int CODE_UPDATE_APP_EXPERIENCE_STATUS = 3006;
    private static final int CODE_UPDATE_APP_RUNNING_STATUS = 3005;
    private static final int CODE_USER_HANDOVER_WIFI = 1008;
    private static final int CODE_WIFI_QOE_EVALUATE = 3003;
    private static final int CODE_WIFI_QOE_START_MONITOR = 3001;
    private static final int CODE_WIFI_QOE_STOP_MONITOR = 3002;
    private static final int CODE_WIFI_QOE_UPDATE_STATUS = 3004;
    private static final boolean DBG = true;
    private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
    private static final int[] FREQUENCYS = new int[]{WifiScanGenieController.CHANNEL_1_FREQ, 2417, 2422, 2427, 2432, WifiScanGenieController.CHANNEL_6_FREQ, 2442, 2447, 2452, 2457, WifiScanGenieController.CHANNEL_11_FREQ, 2467, 2472};
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
    private static ScanSettings settings_24G = new ScanSettings();
    private static WifiServiceUtils wifiServiceUtils = ((WifiServiceUtils) EasyInvokeFactory.getInvokeUtils(WifiServiceUtils.class));
    private static WifiStateMachineUtils wifiStateMachineUtils = ((WifiStateMachineUtils) EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class));
    private final ServiceConnection conn;
    private boolean isPPPOE;
    private long lastScanResultsAvailableTime;
    private final ActivityManager mActivityManager;
    private final IMapconServiceCallback mAirPlaneCallback;
    private final AppOpsManager mAppOps;
    private final IMapconServiceCallback mCallback;
    private final Clock mClock;
    private Context mContext;
    private boolean mHasScanned;
    private boolean mIsAbsoluteRest;
    private Handler mMapconHandler;
    private HandlerThread mMapconHandlerTread;
    private IMapconService mMapconService;
    private Handler mNetworkResetHandler;
    private PPPOEStateMachine mPPPOEStateMachine;
    private int mPluggedType;
    private PowerManager mPowerManager;
    private boolean mVowifiServiceOn;
    private WifiProStateMachine mWifiProStateMachine;
    private final ArraySet<String> mWifiScanBlacklist;

    static {
        settings_24G.channelSet = new ArrayList();
        for (int freq : FREQUENCYS) {
            WifiChannel wifiChannel = new WifiChannel();
            wifiChannel.channelNum = freq;
            settings_24G.channelSet.add(wifiChannel);
        }
    }

    public HwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        boolean z = true;
        super(context, wifiInjector, asyncChannel);
        if (SystemProperties.getInt("ro.config.pppoe_enable", 0) != 1) {
            z = false;
        }
        this.isPPPOE = z;
        this.lastScanResultsAvailableTime = 0;
        this.mWifiScanBlacklist = new ArraySet();
        this.mIsAbsoluteRest = false;
        this.mHasScanned = false;
        this.conn = new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {
                Log.d(HwWifiService.TAG, "onServiceDisconnected,IMapconService");
                HwWifiService.this.mMapconService = null;
                HwWifiService.this.mVowifiServiceOn = false;
                HwWifiService.this.mMapconHandlerTread.quit();
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(HwWifiService.TAG, "onServiceConnected,IMapconService");
                HwWifiService.this.mMapconService = Stub.asInterface(service);
                HwWifiService.this.mMapconHandlerTread = new HandlerThread("MapconHandler");
                HwWifiService.this.mMapconHandlerTread.start();
                HwWifiService.this.mMapconHandler = new Handler(HwWifiService.this.mMapconHandlerTread.getLooper()) {
                    public void handleMessage(Message msg) {
                        Log.d(HwWifiService.TAG, "handle TimeoutMessage,msg:" + msg.what);
                        WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                        switch (msg.what) {
                            case 0:
                                HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain(msg.obj));
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
                HwWifiService.this.mVowifiServiceOn = true;
            }
        };
        this.mCallback = new IMapconServiceCallback.Stub() {
            public void onVoWifiCloseDone() {
                Log.d(HwWifiService.TAG, "onVoWifiCloseDone: cancel delayed message,send CMD_WIFI_TOGGLED");
                if (HwWifiService.this.mMapconHandler.hasMessages(2)) {
                    HwWifiService.this.mMapconHandler.removeMessages(2);
                }
                WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                if (controller != null) {
                    controller.sendMessage(155656);
                }
            }
        };
        this.mAirPlaneCallback = new IMapconServiceCallback.Stub() {
            public void onVoWifiCloseDone() {
                Log.d(HwWifiService.TAG, "onVoWifiCloseDone: cancel delayed message, send CMD_AIRPLANE_TOGGLED");
                if (HwWifiService.this.mMapconHandler.hasMessages(1)) {
                    HwWifiService.this.mMapconHandler.removeMessages(1);
                }
                WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                if (controller != null) {
                    controller.sendMessage(155657);
                }
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
                    HwWifiService.this.lastScanResultsAvailableTime = HwWifiService.this.mClock.getElapsedSinceBootMillis();
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
                    HwWifiService.this.mIsAbsoluteRest = intent.getBooleanExtra("stationary", false);
                    Log.d(HwWifiService.TAG, "mIsAbsoluteRest =" + HwWifiService.this.mIsAbsoluteRest);
                    if (HwWifiService.this.mIsAbsoluteRest) {
                        HwWifiService.this.mHasScanned = false;
                    }
                }
            }
        }, new IntentFilter(PG_AR_STATE_ACTION), PG_RECEIVER_PERMISSION, null);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    HwWifiService.this.mPluggedType = intent.getIntExtra("plugged", 0);
                    if (HwWifiService.this.mPluggedType == 0 && (HwWifiService.this.mWifiStateMachine instanceof HwWifiStateMachine) && HwWifiService.this.mWifiStateMachine.getChargingState()) {
                        HwWifiService.this.mPluggedType = 2;
                    }
                    Log.d(HwWifiService.TAG, "mPluggedType =" + HwWifiService.this.mPluggedType);
                }
            }
        }, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    protected boolean enforceStopScanSreenOff() {
        if (this.mPowerManager.isScreenOn() || ("com.huawei.ca".equals(getAppName(Binder.getCallingPid())) ^ 1) == 0) {
            return false;
        }
        Slog.i(TAG, "Screen is off, " + getAppName(Binder.getCallingPid()) + " startScan is skipped.");
        return true;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        HwWifiStateMachine mHwWifiStateMachine;
        int result;
        boolean result2;
        int monitorType;
        IHwQoECallback callback;
        HwQoEService mHwQoEService;
        int reserved_running;
        int uid;
        switch (code) {
            case 1001:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  getWpaSuppConfig");
                String result3 = "";
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result3 = ((HwWifiStateMachine) this.mWifiStateMachine).getWpaSuppConfig();
                }
                reply.writeNoException();
                reply.writeString(result3);
                return true;
            case 1002:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  startPPPOE");
                if (this.isPPPOE) {
                    Object _arg0;
                    if (data.readInt() != 0) {
                        _arg0 = (PPPOEConfig) PPPOEConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.mPPPOEStateMachine.sendMessage(589825, _arg0);
                    reply.writeNoException();
                    return true;
                }
                Slog.w(TAG, "the PPPOE function is closed.");
                return false;
            case 1003:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  stopPPPOE");
                if (this.isPPPOE) {
                    this.mPPPOEStateMachine.sendMessage(589826);
                    reply.writeNoException();
                    return true;
                }
                Slog.w(TAG, "the PPPOE function is closed.");
                return false;
            case CODE_GET_PPPOE_INFO_CONFIG /*1004*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  get PPPOE info");
                if (this.isPPPOE) {
                    PPPOEInfo _result = this.mPPPOEStateMachine.getPPPOEInfo();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                Slog.w(TAG, "the PPPOE function is closed.");
                return false;
            case 1005:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "HwWifiService getApLinkedStaList");
                List result4 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    mHwWifiStateMachine = (HwWifiStateMachine) this.mWifiStateMachine;
                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                        result4 = mHwWifiStateMachine.syncGetApLinkedStaList(wifiServiceUtils.getWifiStateMachineChannel(this));
                    } else {
                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                    }
                    String regularIP = "\\.[\\d]{1,3}\\.[\\d]{1,3}\\.";
                    String regularMAC = ":[\\w]{1,}:[\\w]{1,}:";
                    if (result4 == null) {
                        Slog.d(TAG, "HwWifiService getApLinkedStaList result = null");
                    } else {
                        Slog.d(TAG, "HwWifiService getApLinkedStaList result = " + result4.toString().replaceAll(regularIP, ".*.*.").replaceAll(regularMAC, ":**:**:"));
                    }
                }
                reply.writeNoException();
                reply.writeStringList(result4);
                return true;
            case 1006:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                String macFilter = data.readString();
                Slog.d(TAG, "HwWifiService  getWpaSuppConfig macFilter");
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setSoftapMacFilter(macFilter);
                }
                reply.writeNoException();
                return true;
            case 1007:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                String mac = data.readString();
                Slog.d(TAG, "HwWifiService  getWpaSuppConfig mac = " + mac);
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setSoftapDisassociateSta(mac);
                }
                reply.writeNoException();
                return true;
            case 1008:
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
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                int[] _result2 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    mHwWifiStateMachine = (HwWifiStateMachine) this.mWifiStateMachine;
                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                        _result2 = mHwWifiStateMachine.syncGetApChannelListFor5G(wifiServiceUtils.getWifiStateMachineChannel(this));
                    } else {
                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                    }
                }
                reply.writeNoException();
                reply.writeIntArray(_result2);
                return true;
            case 1010:
                Slog.d(TAG, "HwWifiService  SET_WIFI_AP_EVALUATE_ENABLED ");
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                boolean enablen = data.readInt() == 1;
                this.mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (this.mWifiProStateMachine != null) {
                    this.mWifiProStateMachine.setWifiApEvaluateEnabled(enablen);
                }
                reply.writeNoException();
                return true;
            case 1011:
                Slog.d(TAG, "HwWifiService  FETCH_WIFI_SIGNAL_INFO_FOR_VOWIFI ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "fetchWifiSignalInfoForVoWiFi(): permissin deny");
                    return false;
                }
                byte[] result5 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result5 = ((HwWifiStateMachine) this.mWifiStateMachine).fetchWifiSignalInfoForVoWiFi();
                }
                reply.writeNoException();
                reply.writeByteArray(result5);
                return true;
            case CODE_SET_VOWIFI_DETECT_MODE /*1012*/:
                Slog.d(TAG, "HwWifiService  SET_VOWIFI_DETECT_MODE ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "setVoWifiDetectMode(): permissin deny");
                    return false;
                }
                WifiDetectConfInfo _arg02;
                if (data.readInt() != 0) {
                    _arg02 = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setVoWifiDetectMode(_arg02);
                }
                reply.writeNoException();
                return true;
            case CODE_GET_VOWIFI_DETECT_MODE /*1013*/:
                Slog.d(TAG, "HwWifiService  GET_VOWIFI_DETECT_MODE ");
                data.enforceInterface(DESCRIPTOR);
                WifiDetectConfInfo result6 = null;
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "getVoWifiDetectMode(): permissin deny");
                    return false;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result6 = ((HwWifiStateMachine) this.mWifiStateMachine).getVoWifiDetectMode();
                }
                reply.writeNoException();
                if (result6 != null) {
                    reply.writeInt(1);
                    result6.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 1014:
                Slog.d(TAG, "HwWifiService  SET_VOWIFI_DETECT_PERIOD ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "setVoWifiDetectPeriod(): permissin deny");
                    return false;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setVoWifiDetectPeriod(data.readInt());
                }
                reply.writeNoException();
                return true;
            case 1015:
                Slog.d(TAG, "HwWifiService  GET_VOWIFI_DETECT_PERIOD ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "getVoWifiDetectPeriod(): permissin deny");
                    return false;
                }
                result = -1;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result = ((HwWifiStateMachine) this.mWifiStateMachine).getVoWifiDetectPeriod();
                }
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case CODE_IS_SUPPORT_VOWIFI_DETECT /*1016*/:
                Slog.d(TAG, "HwWifiService  IS_SUPPORT_VOWIFI_DETECT ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "isSupportVoWifiDetect(): permissin deny");
                    return false;
                }
                boolean _result3 = false;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    mHwWifiStateMachine = (HwWifiStateMachine) this.mWifiStateMachine;
                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                        _result3 = mHwWifiStateMachine.syncGetSupportedVoWifiDetect(wifiServiceUtils.getWifiStateMachineChannel(this));
                    } else {
                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                    }
                }
                reply.writeNoException();
                reply.writeInt(1);
                reply.writeInt(_result3 ? 1 : 0);
                return true;
            case CODE_ENABLE_HILINK_HANDSHAKE /*2001*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                this.mWifiStateMachine.enableHiLinkHandshake(data.readInt() == 1, data.readString());
                return true;
            case 2002:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                String _result4 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    _result4 = ((HwWifiStateMachine) this.mWifiStateMachine).getConnectionRawPsk();
                }
                reply.writeNoException();
                reply.writeString(_result4);
                return true;
            case 2004:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "HwWifiService REQUEST_WIFI_ENABLE");
                return requestWifiEnable(data);
            case 2005:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                result = WifiInjector.getInstance().getWifiNative().setWifiTxPowerHw(data.readInt());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case 2006:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "HwWifiService  EXTEND_WIFI_SCAN_PERIOD_FOR_P2P");
                return externWifiScanPeriodForP2p(data);
            case 2007:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                int type = data.readInt();
                List<String> packageWhiteList = new ArrayList();
                data.readStringList(packageWhiteList);
                if (type == 7) {
                    HwQoEService qoeService_wifiSleep = HwQoEService.getInstance();
                    if (qoeService_wifiSleep != null) {
                        qoeService_wifiSleep.updateWifiSleepWhiteList(type, packageWhiteList);
                    }
                } else {
                    BackgroundAppScanManager.getInstance().refreshPackageWhitelist(type, packageWhiteList);
                }
                return true;
            case 2008:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                result2 = false;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    mHwWifiStateMachine = (HwWifiStateMachine) this.mWifiStateMachine;
                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                        result2 = mHwWifiStateMachine.isRSDBSupported();
                    } else {
                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                    }
                }
                reply.writeNoException();
                reply.writeBooleanArray(new boolean[]{result2});
                return true;
            case CODE_WIFI_QOE_START_MONITOR /*3001*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                result2 = false;
                monitorType = data.readInt();
                int period = data.readInt();
                callback = IHwQoECallback.Stub.asInterface(data.readStrongBinder());
                mHwQoEService = HwQoEService.getInstance();
                if (mHwQoEService != null) {
                    result2 = mHwQoEService.registerHwQoEMonitor(monitorType, period, callback);
                }
                reply.writeNoException();
                reply.writeInt(result2 ? 1 : 0);
                return true;
            case CODE_WIFI_QOE_STOP_MONITOR /*3002*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                result2 = false;
                monitorType = data.readInt();
                mHwQoEService = HwQoEService.getInstance();
                if (mHwQoEService != null) {
                    result2 = mHwQoEService.unRegisterHwQoEMonitor(monitorType);
                }
                reply.writeNoException();
                reply.writeInt(result2 ? 1 : 0);
                return true;
            case CODE_WIFI_QOE_EVALUATE /*3003*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                callback = IHwQoECallback.Stub.asInterface(data.readStrongBinder());
                result2 = false;
                mHwQoEService = HwQoEService.getInstance();
                if (mHwQoEService != null) {
                    result2 = mHwQoEService.evaluateNetworkQuality(callback);
                }
                reply.writeNoException();
                reply.writeInt(result2 ? 1 : 0);
                return true;
            case CODE_WIFI_QOE_UPDATE_STATUS /*3004*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                result2 = false;
                int state = data.readInt();
                mHwQoEService = HwQoEService.getInstance();
                if (mHwQoEService != null) {
                    result2 = mHwQoEService.updateVOWIFIState(state);
                }
                reply.writeNoException();
                reply.writeInt(result2 ? 1 : 0);
                return true;
            case CODE_UPDATE_APP_RUNNING_STATUS /*3005*/:
                data.enforceInterface(DESCRIPTOR);
                int uid_running = data.readInt();
                int type_running = data.readInt();
                int status_running = data.readInt();
                int scene_running = data.readInt();
                reserved_running = data.readInt();
                Slog.d(TAG, " updateAppRunningStatus  uid:" + uid_running + ", type:" + type_running + ",status:" + status_running + "scene: " + scene_running);
                HwQoEService qoeService_app_running = HwQoEService.getInstance();
                if (qoeService_app_running != null) {
                    qoeService_app_running.updateAppRunningStatus(uid_running, type_running, status_running, scene_running, reserved_running);
                }
                reply.writeNoException();
                return true;
            case CODE_UPDATE_APP_EXPERIENCE_STATUS /*3006*/:
                data.enforceInterface(DESCRIPTOR);
                int uid_experience = data.readInt();
                int experience = data.readInt();
                long rtt_experience = data.readLong();
                int reserved_experience = data.readInt();
                reserved_running = data.readInt();
                Slog.d(TAG, "updateAppExperienceStatus  uid:" + uid_experience + ", experience:" + experience + ",rtt:" + rtt_experience);
                HwQoEService qoeService_app_experience = HwQoEService.getInstance();
                if (qoeService_app_experience != null) {
                    qoeService_app_experience.updateAppExperienceStatus(uid_experience, experience, rtt_experience, reserved_experience);
                }
                reply.writeNoException();
                return true;
            case CODE_IS_BG_LIMIT_ALLOWED /*3008*/:
                data.enforceInterface(DESCRIPTOR);
                uid = data.readInt();
                result2 = false;
                HwQoEService qoeService_check_bg_limit = HwQoEService.getInstance();
                if (qoeService_check_bg_limit != null) {
                    result2 = qoeService_check_bg_limit.isBgLimitAllowed(uid);
                }
                reply.writeNoException();
                reply.writeBooleanArray(new boolean[]{result2});
                return true;
            case CODE_PROXY_WIFI_LOCK /*3009*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                uid = data.readInt();
                boolean proxy = data.readBoolean();
                Slog.d(TAG, "binder uid:" + uid + ", proxy :" + proxy);
                WifiInjector.getInstance().getWifiLockManager().proxyWifiLock(uid, proxy);
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    private boolean requestWifiEnable(Parcel data) {
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller == null) {
            return false;
        }
        int flag = data.readInt();
        Slog.d(TAG, "HwWifiService REQUEST_WIFI_ENABLE enable = " + flag);
        if (flag == 1) {
            Slog.d(TAG, "CMD_REQUEST_WIFI_ENABLE");
            controller.sendMessage(155699);
        } else {
            Slog.d(TAG, "CMD_REQUEST_WIFI_DISABLE");
            controller.sendMessage(155700);
        }
        return true;
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

    protected void handleForgetNetwork(final Message msg) {
        WifiConfiguration currentWifiConfiguration = this.mWifiStateMachine.getCurrentWifiConfiguration();
        Log.d(TAG, "handleForgetNetwork networkId = " + msg.arg1);
        if (this.mVowifiServiceOn && currentWifiConfiguration != null && msg.arg1 == currentWifiConfiguration.networkId) {
            Log.d(TAG, "handleForgetNetwork enter.");
            this.mMapconHandler.sendMessageDelayed(this.mMapconHandler.obtainMessage(0, msg), 5000);
            if (this.mMapconService != null) {
                try {
                    this.mMapconService.notifyWifiOff(new IMapconServiceCallback.Stub() {
                        public void onVoWifiCloseDone() {
                            Log.d(HwWifiService.TAG, "onVoWifiCloseDone: cancel delayed message and send FORGET_NETWORK");
                            HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            if (HwWifiService.this.mMapconHandler.hasMessages(0)) {
                                HwWifiService.this.mMapconHandler.removeMessages(0);
                            }
                        }
                    });
                    return;
                } catch (RemoteException e) {
                    Log.e(TAG, "Exception:", e);
                    this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    if (this.mMapconHandler.hasMessages(0)) {
                        this.mMapconHandler.removeMessages(0);
                        return;
                    }
                    return;
                }
            }
            return;
        }
        int currentNetId = -1;
        if (currentWifiConfiguration != null) {
            currentNetId = currentWifiConfiguration.networkId;
        }
        Log.d(TAG, "handleForgetNetwork current networkId = " + currentNetId);
        this.mWifiStateMachine.sendMessage(Message.obtain(msg));
    }

    protected void handleAirplaneModeToggled() {
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (this.mVowifiServiceOn) {
            if (this.mSettingsStore.isAirplaneModeOn()) {
                Log.d(TAG, "handleAirplaneModeToggled, sendMessageDelayed");
                this.mMapconHandler.sendMessageDelayed(this.mMapconHandler.obtainMessage(1), 5000);
                if (this.mMapconService != null) {
                    try {
                        Log.d(TAG, "airplane mode enter, notify MapconService to shutdown");
                        this.mMapconService.notifyWifiOff(this.mAirPlaneCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (controller != null) {
                controller.sendMessage(155657);
            }
        } else if (controller != null) {
            controller.sendMessage(155657);
        }
    }

    protected void setWifiEnabledAfterVoWifiOff(boolean enable) {
        Log.d(TAG, "setWifiEnabled " + enable);
        if (WifiProCommonUtils.isWifiSelfCuring() || !this.mVowifiServiceOn || 3 != getWifiEnabledState() || (this.mSettingsStore.isWifiToggleEnabled() ^ 1) == 0) {
            WifiController controller = wifiServiceUtils.getWifiController(this);
            if (controller != null) {
                controller.sendMessage(155656);
                return;
            }
            return;
        }
        Log.d(TAG, "setWifiEnabled: sendMessageDelayed");
        this.mMapconHandler.sendMessageDelayed(this.mMapconHandler.obtainMessage(2), 5000);
        if (this.mMapconService != null) {
            try {
                Log.d(TAG, "setWifiEnabled enter, notify MapconService to shutdown");
                this.mMapconService.notifyWifiOff(this.mCallback);
            } catch (RemoteException e) {
                Log.d(TAG, "notifyWifiOff" + e.toString());
            }
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

    protected void onReceiveEx(Context context, Intent intent) {
        String action = intent.getAction();
        Slog.d(TAG, "onReceive, action:" + action);
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue() && ACTION_VOWIFI_STARTED.equals(action)) {
            Log.d(TAG, "received broadcast ACTION_VOWIFI_STARTED, try to bind MapconService");
            this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), this.conn, 1, UserHandle.OWNER);
        }
    }

    protected void registerForBroadcastsEx(IntentFilter intentFilter) {
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
            intentFilter.addAction(ACTION_VOWIFI_STARTED);
        }
    }

    protected boolean mdmForPolicyForceOpenWifi(boolean showToast, boolean enable) {
        if (!HwDeviceManager.disallowOp(52) || (enable ^ 1) == 0) {
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

    public void factoryReset() {
        super.factoryReset();
        if (SystemProperties.getBoolean("ro.config.hw_preset_ap", false)) {
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1) {
                Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
            }
            try {
                setWifiEnabled(this.mContext.getPackageName(), false);
            } catch (RemoteException e) {
                Slog.e(TAG, "setWifiEnabled false exception: " + e.getMessage());
            }
            this.mNetworkResetHandler.postDelayed(new Runnable() {
                public void run() {
                    HwWifiService.this.removeWpaSupplicantConf();
                    try {
                        HwWifiService.this.setWifiEnabled(HwWifiService.this.mContext.getPackageName(), true);
                    } catch (RemoteException e) {
                        Slog.e(HwWifiService.TAG, "setWifiEnabled true exception: " + e.getMessage());
                    }
                }
            }, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
        }
    }

    protected boolean startQuickttffScan(WorkSource workSource, String packageName, int scanRequestCounter) {
        if (!"com.huawei.lbs".equals(packageName) || Global.getInt(this.mContext.getContentResolver(), QTTFF_WIFI_SCAN_ENABLED, 0) != 1) {
            return false;
        }
        Slog.d(TAG, "quickttff request  2.4G wifi scan");
        if (this.lastScanResultsAvailableTime == 0 || this.mClock.getElapsedSinceBootMillis() - this.lastScanResultsAvailableTime >= 5000) {
            Slog.d(TAG, "Start 2.4G wifi scan.");
            this.mWifiStateMachine.startScan(Binder.getCallingUid(), scanRequestCounter, settings_24G, workSource);
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

    protected boolean limitForegroundWifiScanRequest(String packageName, int uid) {
        long id = Binder.clearCallingIdentity();
        NetLocationStrategy wifiScanStrategy = null;
        try {
            wifiScanStrategy = HwServiceFactory.getNetLocationStrategy(packageName, uid, 1);
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
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    protected boolean limitWifiScanRequest(String packageName) {
        if (this.mWifiScanBlacklist.contains(packageName)) {
            return isGnssLocationFix();
        }
        return false;
    }

    private boolean isGnssLocationFix() {
        boolean isGnssLocationFix = Global.getInt(this.mContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) == 1;
        Log.d(TAG, "isGnssLocationFix =" + isGnssLocationFix);
        return isGnssLocationFix;
    }

    private void loadWifiScanBlacklist() {
        String[] blackList = this.mContext.getResources().getStringArray(33816590);
        this.mWifiScanBlacklist.clear();
        if (blackList != null) {
            this.mWifiScanBlacklist.addAll(Arrays.asList(blackList));
            Log.d(TAG, "mWifiScanBlacklist =" + this.mWifiScanBlacklist);
        }
    }

    private void updateWifiScanblacklist() {
        this.mWifiScanBlacklist.clear();
        this.mWifiScanBlacklist.addAll(BackgroundAppScanManager.getInstance().getPackagBlackList());
    }

    protected boolean limitWifiScanInAbsoluteRest(String packageName) {
        boolean requestFromBackground = isRequestFromBackground(packageName);
        Log.d(TAG, "mIsAbsoluteRest =" + this.mIsAbsoluteRest + ", mPluggedType =" + this.mPluggedType + ", mHasScanned =" + this.mHasScanned + ", requestFromBackground =" + requestFromBackground);
        if (this.mIsAbsoluteRest && this.mPluggedType == 0 && requestFromBackground && this.mHasScanned) {
            return true;
        }
        this.mHasScanned = true;
        return false;
    }

    /* JADX WARNING: Missing block: B:4:0x0011, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isRequestFromBackground(String packageName) {
        boolean z = false;
        if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1010 || TextUtils.isEmpty(packageName) || PROCESS_BD.equals(packageName) || PROCESS_GD.equals(packageName)) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), packageName);
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            if (this.mActivityManager.getPackageImportance(packageName) > BACKGROUND_IMPORTANCE_CUTOFF) {
                z = true;
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return z;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private boolean removeWpaSupplicantConf() {
        boolean ret = false;
        try {
            File conf = Environment.buildPath(Environment.getDataDirectory(), new String[]{"misc", "wifi", "wpa_supplicant.conf"});
            Slog.d(TAG, "conf path: " + conf.getPath());
            if (conf.exists()) {
                ret = conf.delete();
            }
            Slog.i(TAG, "delete conf result : " + ret);
        } catch (SecurityException e) {
            Slog.e(TAG, "delete conf error : " + e.getMessage());
            Slog.i(TAG, "delete conf result : " + false);
        } catch (Throwable th) {
            Slog.i(TAG, "delete conf result : " + false);
            throw th;
        }
        return ret;
    }
}
