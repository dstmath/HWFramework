package com.android.server.wifi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Network;
import android.net.wifi.PPPOEConfig;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.WifiDetectConfInfo;
import android.os.Binder;
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
import android.util.Log;
import android.util.Slog;
import com.android.server.PPPOEStateMachine;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconServiceCallback;
import com.hisi.mapcon.IMapconServiceCallback.Stub;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HwWifiService extends WifiServiceImpl {
    private static final String ACTION_VOWIFI_STARTED = "com.hisi.vowifi.started";
    private static final int CODE_ENABLE_HILINK_HANDSHAKE = 2001;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_PPPOE_INFO_CONFIG = 1004;
    private static final int CODE_GET_SINGNAL_INFO = 1011;
    private static final int CODE_GET_SOFTAP_CHANNEL_LIST = 1009;
    private static final int CODE_GET_VOWIFI_DETECT_MODE = 1013;
    private static final int CODE_GET_VOWIFI_DETECT_PERIOD = 1015;
    private static final int CODE_GET_WPA_SUPP_CONFIG = 1001;
    private static final int CODE_IS_SUPPORT_VOWIFI_DETECT = 1016;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_VOWIFI_DETECT_MODE = 1012;
    private static final int CODE_SET_VOWIFI_DETECT_PERIOD = 1014;
    private static final int CODE_SET_WIFI_AP_EVALUATE_ENABLED = 1010;
    private static final int CODE_START_PPPOE_CONFIG = 1002;
    private static final int CODE_STOP_PPPOE_CONFIG = 1003;
    private static final int CODE_USER_HANDOVER_WIFI = 1008;
    private static final boolean DBG = true;
    private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
    private static final int MAPCON_SERVICE_SHUTDOWN_TIMEOUT = 5000;
    private static final int MSG_EVENT_MAPCON_DONE = 1;
    private static final int MSG_EVENT_MAPCON_TIMEOUT = 2;
    private static final int MSG_EVENT_NOTIFY_MAPCON = 0;
    private static final String PPPOE_TAG = "PPPOEWIFIService";
    private static final String TAG = "HwWifiService";
    private static final String VOWIFI_WIFI_DETECT_PERMISSION = "com.huawei.permission.VOWIFI_WIFI_DETECT";
    private static WifiServiceUtils wifiServiceUtils;
    private final ServiceConnection conn;
    private boolean isPPPOE;
    private final IMapconServiceCallback mAirPlaneCallback;
    private final IMapconServiceCallback mCallback;
    private Context mContext;
    private Timer mEnableTimer;
    private Handler mMapconHandler;
    private HandlerThread mMapconHandlerTread;
    private IMapconService mMapconService;
    private PPPOEStateMachine mPPPOEStateMachine;
    private PowerManager mPowerManager;
    private boolean mVowifiServiceOn;
    private WifiProStateMachine mWifiProStateMachine;

    /* renamed from: com.android.server.wifi.HwWifiService.4 */
    class AnonymousClass4 extends TimerTask {
        final /* synthetic */ Message val$msg;

        AnonymousClass4(Message val$msg) {
            this.val$msg = val$msg;
        }

        public void run() {
            Log.d(HwWifiService.TAG, "enter forget network : timer timeout");
            HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain(this.val$msg));
            HwWifiService.this.cancelEnableTimer();
        }
    }

    /* renamed from: com.android.server.wifi.HwWifiService.5 */
    class AnonymousClass5 extends Stub {
        final /* synthetic */ Message val$msg;

        AnonymousClass5(Message val$msg) {
            this.val$msg = val$msg;
        }

        public void onVoWifiCloseDone() {
            Log.d(HwWifiService.TAG, "onVoWifiCloseDone: timer is running, send FORGET_NETWORK");
            HwWifiService.this.cancelEnableTimer();
            HwWifiService.this.mWifiStateMachine.sendMessage(Message.obtain(this.val$msg));
        }
    }

    /* renamed from: com.android.server.wifi.HwWifiService.6 */
    class AnonymousClass6 extends TimerTask {
        final /* synthetic */ WifiController val$controller;

        AnonymousClass6(WifiController val$controller) {
            this.val$controller = val$controller;
        }

        public void run() {
            Log.d(HwWifiService.TAG, "enter airplane : timer timeout");
            if (this.val$controller != null) {
                this.val$controller.sendMessage(155657);
            }
            try {
                HwWifiService.this.mEnableTimer.cancel();
                HwWifiService.this.mEnableTimer = null;
            } catch (Exception e) {
                Log.e(HwWifiService.TAG, "Exception:", e);
            }
        }
    }

    static {
        wifiServiceUtils = (WifiServiceUtils) EasyInvokeFactory.getInvokeUtils(WifiServiceUtils.class);
    }

    public HwWifiService(Context context) {
        boolean z = DBG;
        super(context);
        if (SystemProperties.getInt("ro.config.pppoe_enable", MSG_EVENT_NOTIFY_MAPCON) != MSG_EVENT_MAPCON_DONE) {
            z = false;
        }
        this.isPPPOE = z;
        this.conn = new ServiceConnection() {

            /* renamed from: com.android.server.wifi.HwWifiService.1.1 */
            class AnonymousClass1 extends Handler {
                AnonymousClass1(Looper $anonymous0) {
                    super($anonymous0);
                }

                public void handleMessage(Message msg) {
                    Log.d(HwWifiService.TAG, "handleMessage,msg:" + msg.what);
                    switch (msg.what) {
                        case HwWifiService.MSG_EVENT_NOTIFY_MAPCON /*0*/:
                            try {
                                HwWifiService.this.mMapconService.notifyWifiOff(HwWifiService.this.mCallback);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        case HwWifiService.MSG_EVENT_MAPCON_DONE /*1*/:
                        case HwWifiService.MSG_EVENT_MAPCON_TIMEOUT /*2*/:
                            WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                            if (controller != null) {
                                controller.sendMessage(155656);
                            }
                            HwWifiService.this.mEnableTimer = null;
                        default:
                    }
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(HwWifiService.TAG, "onServiceDisconnected,IMapconService");
                HwWifiService.this.mMapconService = null;
                HwWifiService.this.mVowifiServiceOn = false;
                HwWifiService.this.mMapconHandlerTread.quit();
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(HwWifiService.TAG, "onServiceConnected,IMapconService");
                HwWifiService.this.mMapconService = IMapconService.Stub.asInterface(service);
                HwWifiService.this.mVowifiServiceOn = HwWifiService.DBG;
                HwWifiService.this.mMapconHandlerTread = new HandlerThread("MapconHandler");
                HwWifiService.this.mMapconHandlerTread.start();
                HwWifiService.this.mMapconHandler = new AnonymousClass1(HwWifiService.this.mMapconHandlerTread.getLooper());
            }
        };
        this.mCallback = new Stub() {
            public void onVoWifiCloseDone() {
                Log.d(HwWifiService.TAG, "onVoWifiCloseDone: timer is running, cancel it and send message");
                try {
                    HwWifiService.this.mEnableTimer.cancel();
                    HwWifiService.this.mEnableTimer = null;
                    Message.obtain(HwWifiService.this.mMapconHandler, HwWifiService.MSG_EVENT_MAPCON_DONE).sendToTarget();
                } catch (Exception e) {
                    Log.e(HwWifiService.TAG, "Exception:", e);
                }
            }
        };
        this.mAirPlaneCallback = new Stub() {
            public void onVoWifiCloseDone() {
                Log.d(HwWifiService.TAG, "onVoWifiCloseDone: timer is running, send CMD_AIRPLANE_TOGGLED");
                try {
                    HwWifiService.this.mEnableTimer.cancel();
                    HwWifiService.this.mEnableTimer = null;
                } catch (Exception e) {
                    Log.e(HwWifiService.TAG, "Exception:", e);
                }
                WifiController controller = HwWifiService.wifiServiceUtils.getWifiController(HwWifiService.this);
                if (controller != null) {
                    controller.sendMessage(155657);
                }
            }
        };
        this.mContext = context;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        if (this.isPPPOE) {
            this.mPPPOEStateMachine = new PPPOEStateMachine(this.mContext, PPPOE_TAG);
            this.mPPPOEStateMachine.start();
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    protected boolean enforceStopScanSreenOff() {
        if (this.mPowerManager.isScreenOn() || "com.huawei.ca".equals(getAppName(Binder.getCallingPid()))) {
            return false;
        }
        Slog.i(TAG, "Screen is off, " + getAppName(Binder.getCallingPid()) + " startScan is skipped.");
        return DBG;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        HwWifiStateMachine mHwWifiStateMachine;
        switch (code) {
            case CODE_GET_WPA_SUPP_CONFIG /*1001*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  getWpaSuppConfig");
                String result = "";
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result = this.mWifiStateMachine.getWpaSuppConfig();
                }
                reply.writeNoException();
                reply.writeString(result);
                return DBG;
            case CODE_START_PPPOE_CONFIG /*1002*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  startPPPOE");
                if (this.isPPPOE) {
                    Object obj;
                    if (data.readInt() != 0) {
                        obj = (PPPOEConfig) PPPOEConfig.CREATOR.createFromParcel(data);
                    } else {
                        obj = null;
                    }
                    this.mPPPOEStateMachine.sendMessage(589825, obj);
                    reply.writeNoException();
                    return DBG;
                }
                Slog.w(TAG, "the PPPOE function is closed.");
                return false;
            case CODE_STOP_PPPOE_CONFIG /*1003*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "WifiService  stopPPPOE");
                if (this.isPPPOE) {
                    this.mPPPOEStateMachine.sendMessage(589826);
                    reply.writeNoException();
                    return DBG;
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
                        reply.writeInt(MSG_EVENT_MAPCON_DONE);
                        _result.writeToParcel(reply, MSG_EVENT_MAPCON_DONE);
                    } else {
                        reply.writeInt(MSG_EVENT_NOTIFY_MAPCON);
                    }
                    return DBG;
                }
                Slog.w(TAG, "the PPPOE function is closed.");
                return false;
            case CODE_GET_APLINKED_STA_LIST /*1005*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "HwWifiService getApLinkedStaList");
                List<String> list = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    mHwWifiStateMachine = (HwWifiStateMachine) this.mWifiStateMachine;
                    if (wifiServiceUtils.getWifiStateMachineChannel(this) != null) {
                        list = mHwWifiStateMachine.syncGetApLinkedStaList(wifiServiceUtils.getWifiStateMachineChannel(this));
                    } else {
                        Slog.e(TAG, "Exception mWifiStateMachineChannel is not initialized");
                    }
                    Slog.d(TAG, "HwWifiService getApLinkedStaList result = " + list);
                }
                reply.writeNoException();
                reply.writeStringList(list);
                return DBG;
            case CODE_SET_SOFTAP_MACFILTER /*1006*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                String macFilter = data.readString();
                Slog.d(TAG, "HwWifiService  getWpaSuppConfig macFilter");
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setSoftapMacFilter(macFilter);
                }
                reply.writeNoException();
                return DBG;
            case CODE_SET_SOFTAP_DISASSOCIATESTA /*1007*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                String mac = data.readString();
                Slog.d(TAG, "HwWifiService  getWpaSuppConfig mac = " + mac);
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setSoftapDisassociateSta(mac);
                }
                reply.writeNoException();
                return DBG;
            case CODE_USER_HANDOVER_WIFI /*1008*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "HwWifiService  userHandoverWiFi ");
                this.mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (this.mWifiProStateMachine != null) {
                    this.mWifiProStateMachine.userHandoverWifi();
                }
                reply.writeNoException();
                return DBG;
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
                return DBG;
            case CODE_SET_WIFI_AP_EVALUATE_ENABLED /*1010*/:
                Slog.d(TAG, "HwWifiService  SET_WIFI_AP_EVALUATE_ENABLED ");
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                boolean enablen = data.readInt() == MSG_EVENT_MAPCON_DONE ? DBG : false;
                this.mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (this.mWifiProStateMachine != null) {
                    this.mWifiProStateMachine.setWifiApEvaluateEnabled(enablen);
                }
                reply.writeNoException();
                return DBG;
            case CODE_GET_SINGNAL_INFO /*1011*/:
                Slog.d(TAG, "HwWifiService  FETCH_WIFI_SIGNAL_INFO_FOR_VOWIFI ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "fetchWifiSignalInfoForVoWiFi(): permissin deny");
                    return false;
                }
                byte[] result2 = null;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result2 = ((HwWifiStateMachine) this.mWifiStateMachine).fetchWifiSignalInfoForVoWiFi();
                }
                reply.writeNoException();
                reply.writeByteArray(result2);
                return DBG;
            case CODE_SET_VOWIFI_DETECT_MODE /*1012*/:
                Slog.d(TAG, "HwWifiService  SET_VOWIFI_DETECT_MODE ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "setVoWifiDetectMode(): permissin deny");
                    return false;
                }
                WifiDetectConfInfo wifiDetectConfInfo;
                if (data.readInt() != 0) {
                    wifiDetectConfInfo = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(data);
                } else {
                    wifiDetectConfInfo = null;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    ((HwWifiStateMachine) this.mWifiStateMachine).setVoWifiDetectMode(wifiDetectConfInfo);
                }
                reply.writeNoException();
                return DBG;
            case CODE_GET_VOWIFI_DETECT_MODE /*1013*/:
                Slog.d(TAG, "HwWifiService  GET_VOWIFI_DETECT_MODE ");
                data.enforceInterface(DESCRIPTOR);
                WifiDetectConfInfo result3 = null;
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "getVoWifiDetectMode(): permissin deny");
                    return false;
                }
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result3 = ((HwWifiStateMachine) this.mWifiStateMachine).getVoWifiDetectMode();
                }
                reply.writeNoException();
                if (result3 != null) {
                    reply.writeInt(MSG_EVENT_MAPCON_DONE);
                    result3.writeToParcel(reply, MSG_EVENT_MAPCON_DONE);
                } else {
                    reply.writeInt(MSG_EVENT_NOTIFY_MAPCON);
                }
                return DBG;
            case CODE_SET_VOWIFI_DETECT_PERIOD /*1014*/:
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
                return DBG;
            case CODE_GET_VOWIFI_DETECT_PERIOD /*1015*/:
                Slog.d(TAG, "HwWifiService  GET_VOWIFI_DETECT_PERIOD ");
                data.enforceInterface(DESCRIPTOR);
                if (this.mContext.checkCallingPermission(VOWIFI_WIFI_DETECT_PERMISSION) != 0) {
                    Slog.d(TAG, "getVoWifiDetectPeriod(): permissin deny");
                    return false;
                }
                int result4 = -1;
                if (this.mWifiStateMachine instanceof HwWifiStateMachine) {
                    result4 = ((HwWifiStateMachine) this.mWifiStateMachine).getVoWifiDetectPeriod();
                }
                reply.writeNoException();
                reply.writeInt(result4);
                return DBG;
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
                reply.writeInt(MSG_EVENT_MAPCON_DONE);
                reply.writeInt(_result3 ? MSG_EVENT_MAPCON_DONE : MSG_EVENT_NOTIFY_MAPCON);
                return DBG;
            case CODE_ENABLE_HILINK_HANDSHAKE /*2001*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                this.mWifiStateMachine.enableHiLinkHandshake(data.readInt() == MSG_EVENT_MAPCON_DONE ? DBG : false, data.readString());
                return DBG;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    private void cancelEnableTimer() {
        try {
            this.mEnableTimer.cancel();
            this.mEnableTimer = null;
        } catch (Exception e) {
            Log.e(TAG, "Exception:", e);
        }
    }

    protected void handleForgetNetwork(Message msg) {
        Network network = getCurrentNetwork();
        Log.d(TAG, "handleForgetNetwork networkId = " + msg.arg1);
        if (this.mVowifiServiceOn && network != null && msg.arg1 == network.netId) {
            Log.d(TAG, "handleForgetNetwork enter.");
            cancelEnableTimer();
            try {
                this.mEnableTimer = new Timer();
                this.mEnableTimer.schedule(new AnonymousClass4(msg), 5000);
                if (this.mMapconService != null) {
                    try {
                        this.mMapconService.notifyWifiOff(new AnonymousClass5(msg));
                    } catch (RemoteException e) {
                        Log.e(TAG, "Exception:", e);
                        this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        cancelEnableTimer();
                    }
                }
            } catch (Exception e2) {
                Log.e(TAG, "Exception:", e2);
                this.mEnableTimer = null;
                this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                return;
            }
        }
        int currentNetId = -1;
        if (network != null) {
            currentNetId = network.netId;
        }
        Log.d(TAG, "handleForgetNetwork current networkId = " + currentNetId);
        this.mWifiStateMachine.sendMessage(Message.obtain(msg));
    }

    protected void handleAirplaneModeToggled() {
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (this.mVowifiServiceOn) {
            if (this.mSettingsStore.isAirplaneModeOn()) {
                try {
                    this.mEnableTimer.cancel();
                    this.mEnableTimer = null;
                } catch (Exception e) {
                    Log.e(TAG, "Exception:", e);
                }
                this.mEnableTimer = new Timer();
                this.mEnableTimer.schedule(new AnonymousClass6(controller), 5000);
                if (this.mMapconService != null) {
                    try {
                        this.mMapconService.notifyWifiOff(this.mAirPlaneCallback);
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
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
        if (this.mVowifiServiceOn && 3 == getWifiEnabledState()) {
            Log.d(TAG, "setWifiEnabled: timer start");
            try {
                this.mEnableTimer.cancel();
                this.mEnableTimer = null;
            } catch (Exception e) {
                Log.e(TAG, "Exception:", e);
            }
            this.mEnableTimer = new Timer();
            this.mEnableTimer.schedule(new TimerTask() {
                public void run() {
                    Log.d(HwWifiService.TAG, "setWifiEnabled: timer timeout");
                    Message.obtain(HwWifiService.this.mMapconHandler, HwWifiService.MSG_EVENT_MAPCON_TIMEOUT).sendToTarget();
                    try {
                        HwWifiService.this.mEnableTimer.cancel();
                        HwWifiService.this.mEnableTimer = null;
                    } catch (Exception e) {
                        Log.e(HwWifiService.TAG, "Exception:", e);
                    }
                }
            }, 5000);
            Log.d(TAG, "setWifiEnabled: notify MapconService to shutdown");
            Message.obtain(this.mMapconHandler, MSG_EVENT_NOTIFY_MAPCON).sendToTarget();
            while (this.mEnableTimer != null) {
                try {
                    Log.d(TAG, "setWifiEnabled ++++");
                    Thread.sleep(5);
                } catch (InterruptedException e2) {
                    Log.d(TAG, e2.toString());
                }
            }
            return;
        }
        WifiController controller = wifiServiceUtils.getWifiController(this);
        if (controller != null) {
            controller.sendMessage(155656);
        }
    }

    protected void onReceiveEx(Context context, Intent intent) {
        String action = intent.getAction();
        Slog.d(TAG, "onReceive, action:" + action);
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue() && ACTION_VOWIFI_STARTED.equals(action)) {
            this.mVowifiServiceOn = DBG;
            Log.d(TAG, "received broadcast ACTION_VOWIFI_STARTED, try to bind MapconService");
            this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), this.conn, MSG_EVENT_MAPCON_DONE, UserHandle.OWNER);
        }
    }

    protected void registerForBroadcastsEx(IntentFilter intentFilter) {
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
            intentFilter.addAction(ACTION_VOWIFI_STARTED);
        }
    }
}
