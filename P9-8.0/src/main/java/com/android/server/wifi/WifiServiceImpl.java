package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration.IpAssignment;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.IWifiManager.Stub;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback;
import com.android.server.wifi.hotspot2.PasspointProvider;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.util.WifiHandler;
import com.android.server.wifi.util.WifiPermissionsUtil;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WifiServiceImpl extends Stub {
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static final boolean DBG = true;
    private static final long DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final String DUMP_ARG_SET_IPREACH_DISCONNECT = "set-ipreach-disconnect";
    private static final String DUMP_ARG_SET_IPREACH_DISCONNECT_DISABLED = "disabled";
    private static final String DUMP_ARG_SET_IPREACH_DISCONNECT_ENABLED = "enabled";
    private static final boolean IS_ATT;
    private static final long LOG_SCAN_RESULTS_INTERVAL_MS = 3000;
    private static final int MAX_SLEEP_RETRY_TIMES = 10;
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final int SCANRESULTS_COUNT_MAX = 200;
    private static final String SET_SSID_NAME = "set_hotspot_ssid_name";
    private static final String TAG = "WifiService";
    private static final String VALUE_DISABLE = "value_disable";
    private static final boolean VDBG = false;
    private static final int WAIT_SLEEP_TIME = 100;
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    private long mBackgroundThrottleInterval;
    private final ArraySet<String> mBackgroundThrottlePackageWhitelist = new ArraySet();
    private final WifiCertManager mCertManager;
    private ClientHandler mClientHandler;
    private final Clock mClock;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFacade;
    private final FrameworkFacade mFrameworkFacade;
    private HiSiWifiComm mHiSiWifiComm;
    private HwWifiCHRService mHwWifiCHRService;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private final ConcurrentHashMap<String, Integer> mIfaceIpModes;
    boolean mInIdleMode;
    private boolean mIsApDialogNeedShow;
    private boolean mIsDialogNeedShow;
    private boolean mIsP2pCloseDialogExist = false;
    private long mLastLogScanResultsTime = 0;
    private final ArrayMap<String, Long> mLastScanTimestamps;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private WifiConfiguration mLocalOnlyHotspotConfig = null;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private final HashMap<Integer, LocalOnlyHotspotRequestInfo> mLocalOnlyHotspotRequests;
    private WifiLog mLog;
    private WifiNotificationController mNotificationController;
    private final BroadcastReceiver mPackageOrUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
            String action2 = intent.getAction();
            if (action2.equals("android.intent.action.PACKAGE_REMOVED")) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    Uri uri = intent.getData();
                    if (uid != -1 && uri != null) {
                        WifiServiceImpl.this.mWifiStateMachine.removeAppConfigs(uri.getSchemeSpecificPart(), uid);
                    }
                }
            } else if (action2.equals("android.intent.action.USER_REMOVED")) {
                WifiServiceImpl.this.mWifiStateMachine.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
            } else {
                Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action + " no handle");
            }
        }
    };
    private final boolean mPermissionReviewRequired;
    private final PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int i = 1;
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                    WifiController -get10;
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        WifiServiceImpl.this.mWifiController.sendMessage(155650);
                    } else if (action.equals("android.intent.action.USER_PRESENT")) {
                        WifiServiceImpl.this.mWifiController.sendMessage(155660);
                    } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                        WifiServiceImpl.this.mWifiController.sendMessage(155651);
                    } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        int pluggedType = intent.getIntExtra("plugged", 0);
                        Slog.d(WifiServiceImpl.TAG, "ACTION_BATTERY_CHANGED pluggedType: " + pluggedType);
                        WifiServiceImpl.this.mWifiController.sendMessage(155652, pluggedType, 0, null);
                    } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                        WifiServiceImpl.this.mWifiStateMachine.sendBluetoothAdapterStateChange(intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0));
                    } else if (action.equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                        boolean emergencyMode = intent.getBooleanExtra("phoneinECMState", false);
                        -get10 = WifiServiceImpl.this.mWifiController;
                        if (!emergencyMode) {
                            i = 0;
                        }
                        -get10.sendMessage(155649, i, 0);
                    } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                        boolean inCall = intent.getBooleanExtra("phoneInEmergencyCall", false);
                        -get10 = WifiServiceImpl.this.mWifiController;
                        if (!inCall) {
                            i = 0;
                        }
                        -get10.sendMessage(155662, i, 0);
                    } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                        WifiServiceImpl.this.handleIdleModeChanged();
                    } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                        int wifiApState = intent.getIntExtra("wifi_state", 14);
                        Slog.d(WifiServiceImpl.TAG, "wifiApState=" + wifiApState);
                        if (wifiApState == 14) {
                            WifiServiceImpl.this.setWifiApEnabled(null, false);
                        }
                    } else {
                        WifiServiceImpl.this.onReceiveEx(context, intent);
                    }
                }
            }
        }
    };
    boolean mScanPending;
    final WifiSettingsStore mSettingsStore;
    private WifiTrafficPoller mTrafficPoller;
    private final UserManager mUserManager;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private final WifiBackupRestore mWifiBackupRestore;
    private WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private final WifiLockManager mWifiLockManager;
    private final WifiMetrics mWifiMetrics;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private WifiNative mWifiNative;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiScanner mWifiScanner;
    WifiServiceHisiExt mWifiServiceHisiExt = null;
    private HwWifiStatStore mWifiStatStore;
    final WifiStateMachine mWifiStateMachine;
    private AsyncChannel mWifiStateMachineChannel;
    WifiStateMachineHandler mWifiStateMachineHandler;
    private int scanRequestCounter = 0;
    private DataUploader uploader;

    private class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WifiConfiguration config;
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        Slog.d(WifiServiceImpl.TAG, "New client listening to asynchronous messages");
                        WifiServiceImpl.this.mTrafficPoller.addClient(msg.replyTo);
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "Client connection failure, error=" + msg.arg1);
                    return;
                case 69633:
                    WifiServiceImpl.this.mFrameworkFacade.makeWifiAsyncChannel(WifiServiceImpl.TAG).connect(WifiServiceImpl.this.mContext, this, msg.replyTo);
                    return;
                case 69636:
                    if (msg.arg1 == 2) {
                        Slog.d(WifiServiceImpl.TAG, "Send failed, client connection lost");
                    } else {
                        Slog.d(WifiServiceImpl.TAG, "Client connection lost with reason: " + msg.arg1);
                    }
                    WifiServiceImpl.this.mTrafficPoller.removeClient(msg.replyTo);
                    return;
                case 151553:
                    config = msg.obj;
                    int networkId = msg.arg1;
                    if (config != null) {
                        WifiServiceImpl.this.mWiFiCHRManager.checkAppName(config, WifiServiceImpl.this.mContext);
                    }
                    Slog.d("WiFiServiceImpl ", "CONNECT  nid=" + Integer.toString(networkId) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                    WifiServiceImpl.this.mWifiStateMachine.setCHRConnectingSartTimestamp(WifiServiceImpl.this.mClock.getElapsedSinceBootMillis());
                    if (config != null && WifiServiceImpl.isValid(config)) {
                        Slog.d(WifiServiceImpl.TAG, "Connect with config " + config);
                        if (WifiServiceImpl.this.mWifiStatStore != null) {
                            WifiServiceImpl.this.mWifiStatStore.setWifiConnectType("FIRST_CONNECT");
                        }
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    } else if (config != null || networkId == -1) {
                        Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                        replyFailed(msg, 151554, 8);
                        return;
                    } else {
                        Slog.d(WifiServiceImpl.TAG, "Connect with networkId " + networkId);
                        if (WifiServiceImpl.this.mWifiStatStore != null) {
                            WifiServiceImpl.this.mWifiStatStore.setWifiConnectType("SELECT_CONNECT");
                        }
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                case 151556:
                    WifiServiceImpl.this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
                    if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                        WifiServiceImpl.this.mHwWifiCHRService.forgetFromUser(msg.arg1);
                    }
                    if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
                        WifiServiceImpl.this.handleForgetNetwork(Message.obtain(msg));
                        return;
                    } else {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                case 151559:
                    config = (WifiConfiguration) msg.obj;
                    Slog.d("WiFiServiceImpl ", "SAVE nid=" + Integer.toString(msg.arg1) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                    if (config == null || !WifiServiceImpl.isValid(config)) {
                        Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                        replyFailed(msg, 151560, 8);
                        return;
                    }
                    Slog.d(WifiServiceImpl.TAG, "Save network with config " + config);
                    WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    return;
                case 151562:
                case 151566:
                case 151569:
                case 151572:
                case 151575:
                    WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }

        private void replyFailed(Message msg, int what, int why) {
            if (msg.replyTo != null) {
                Message reply = Message.obtain();
                reply.what = what;
                reply.arg1 = why;
                try {
                    msg.replyTo.send(reply);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public final class LocalOnlyRequestorCallback implements RequestingApplicationDeathCallback {
        public void onLocalOnlyHotspotRequestorDeath(LocalOnlyHotspotRequestInfo requestor) {
            WifiServiceImpl.this.unregisterCallingAppAndStopLocalOnlyHotspot(requestor);
        }
    }

    class TdlsTask extends AsyncTask<TdlsTaskParams, Integer, Integer> {
        TdlsTask() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:35:0x0099 A:{SYNTHETIC, Splitter: B:35:0x0099} */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x00a2 A:{SYNTHETIC, Splitter: B:40:0x00a2} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected Integer doInBackground(TdlsTaskParams... params) {
            Throwable th;
            TdlsTaskParams param = params[0];
            String remoteIpAddress = param.remoteIpAddress.trim();
            boolean enable = param.enable;
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
                            if (remoteIpAddress.equals(ip)) {
                                macAddress = mac;
                                break;
                            }
                        }
                    }
                    if (macAddress == null) {
                        Slog.w(WifiServiceImpl.TAG, "Did not find remoteAddress {" + remoteIpAddress + "} in " + "/proc/net/arp");
                    } else {
                        WifiServiceImpl.this.enableTdlsWithMacAddress(macAddress, enable);
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
                } catch (IOException e3) {
                    reader = reader2;
                    Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                    if (reader != null) {
                    }
                    return Integer.valueOf(0);
                } catch (Throwable th2) {
                    th = th2;
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
                try {
                    Slog.e(WifiServiceImpl.TAG, "Could not open /proc/net/arp to lookup mac address");
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return Integer.valueOf(0);
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                    }
                }
                return Integer.valueOf(0);
            }
            return Integer.valueOf(0);
        }
    }

    class TdlsTaskParams {
        public boolean enable;
        public String remoteIpAddress;

        TdlsTaskParams() {
        }
    }

    private class WifiStateMachineHandler extends WifiHandler {
        private AsyncChannel mWsmChannel;

        WifiStateMachineHandler(String tag, Looper looper, AsyncChannel asyncChannel) {
            super(tag, looper);
            this.mWsmChannel = asyncChannel;
            this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        WifiServiceImpl.this.mWifiStateMachineChannel = this.mWsmChannel;
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine connection failure, error=" + msg.arg1);
                    WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    return;
                case 69636:
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine channel lost, msg.arg1 =" + msg.arg1);
                    WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
                    return;
                case WifiStateMachine.CMD_CHANGE_TO_STA_P2P_CONNECT /*131573*/:
                    Slog.e(WifiServiceImpl.TAG, "handleMessage CMD_CHANGE_TO_STA_P2P_CONNECT");
                    WifiServiceImpl.this.showP2pToStaDialog();
                    return;
                case WifiStateMachine.CMD_CHANGE_TO_AP_P2P_CONNECT /*131574*/:
                    Slog.e(WifiServiceImpl.TAG, "handleMessage CMD_CHANGE_TO_STA_AP_CONNECT");
                    Bundle data = msg.getData();
                    WifiConfiguration wifiConfig = (WifiConfiguration) data.getParcelable("wifiConfig");
                    WifiServiceImpl.this.showP2pToAPDialog(wifiConfig, data.getBoolean("isWifiApEnabled"));
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "WifiStateMachineHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }
    }

    static {
        boolean equals;
        if ("07".equals(SystemProperties.get("ro.config.hw_opta", HwWifiCHRStateManager.TYPE_AP_VENDOR))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", HwWifiCHRStateManager.TYPE_AP_VENDOR));
        } else {
            equals = false;
        }
        IS_ATT = equals;
    }

    public WifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        boolean z;
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mClock = wifiInjector.getClock();
        this.mFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mTrafficPoller = this.mWifiInjector.getWifiTrafficPoller();
        this.mUserManager = this.mWifiInjector.getUserManager();
        this.mCountryCode = this.mWifiInjector.getWifiCountryCode();
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
        this.mWifiStateMachine = this.mWifiInjector.getWifiStateMachine();
        this.mWifiNative = this.mWifiInjector.getWifiNative();
        this.mWifiStateMachine.enableRssiPolling(true);
        this.mSettingsStore = this.mWifiInjector.getWifiSettingsStore();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mCertManager = this.mWifiInjector.getWifiCertManager();
        this.mNotificationController = this.mWifiInjector.getWifiNotificationController();
        this.mWifiLockManager = this.mWifiInjector.getWifiLockManager();
        this.mWifiMulticastLockManager = this.mWifiInjector.getWifiMulticastLockManager();
        HandlerThread wifiServiceHandlerThread = this.mWifiInjector.getWifiServiceHandlerThread();
        this.mClientHandler = new ClientHandler(TAG, wifiServiceHandlerThread.getLooper());
        this.mWifiStateMachineHandler = new WifiStateMachineHandler(TAG, wifiServiceHandlerThread.getLooper(), asyncChannel);
        this.mWifiController = this.mWifiInjector.getWifiController();
        this.mWifiBackupRestore = this.mWifiInjector.getWifiBackupRestore();
        if (Build.PERMISSIONS_REVIEW_REQUIRED) {
            z = true;
        } else {
            z = context.getResources().getBoolean(17956987);
        }
        this.mPermissionReviewRequired = z;
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mLog = this.mWifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mLastScanTimestamps = new ArrayMap();
        updateBackgroundThrottleInterval();
        updateBackgroundThrottlingWhitelist();
        this.mIfaceIpModes = new ConcurrentHashMap();
        this.mLocalOnlyHotspotRequests = new HashMap();
        enableVerboseLoggingInternal(getVerboseLoggingLevel());
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt = new WifiServiceHisiExt(this.mContext);
            this.mHiSiWifiComm = new HiSiWifiComm(this.mContext);
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt = this.mWifiStateMachine.mWifiStateMachineHisiExt;
        }
        this.mWifiStateMachine.setmSettingsStore(this.mSettingsStore);
        this.uploader = DataUploader.getInstance();
        this.uploader.setContext(this.mContext);
        BackgroundAppScanManager.getInstance().registerWhiteListChangeListener(new WhitelistListener() {
            public void onWhitelistChange(List<String> list) {
                WifiServiceImpl.this.updateBackgroundThrottlingWhitelist();
            }
        });
    }

    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mClientHandler.setWifiLog(log);
    }

    public void checkAndStartWifi() {
        if (this.mFrameworkFacade.inStorageManagerCryptKeeperBounce()) {
            Log.d(TAG, "Device still encrypted. Need to restart SystemServer.  Do not start wifi.");
            return;
        }
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        Slog.i(TAG, "WifiService starting up with Wi-Fi " + (wifiEnabled ? DUMP_ARG_SET_IPREACH_DISCONNECT_ENABLED : DUMP_ARG_SET_IPREACH_DISCONNECT_DISABLED));
        this.mWifiStateMachine.setWifiRepeaterStoped();
        registerForScanModeChange();
        registerForBackgroundThrottleChanges();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiServiceImpl.this.mdmForPolicyForceOpenWifi(false, false)) {
                    Slog.w(WifiServiceImpl.TAG, "mdm force open wifi, not allow airplane close wifi");
                    return;
                }
                if (WifiServiceImpl.this.mSettingsStore.handleAirplaneModeToggled()) {
                    if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
                        WifiServiceImpl.this.handleAirplaneModeToggled();
                    } else {
                        WifiServiceImpl.this.mWifiController.sendMessage(155657);
                    }
                }
                if (WifiServiceImpl.this.mSettingsStore.isAirplaneModeOn()) {
                    Log.d(WifiServiceImpl.TAG, "resetting country code because Airplane mode is ON");
                    WifiServiceImpl.this.mCountryCode.airplaneModeEnabled();
                }
            }
        }, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra("ss");
                if ("ABSENT".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was removed");
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks(false);
                    Log.d(WifiServiceImpl.TAG, "resetting country code because SIM is removed");
                    WifiServiceImpl.this.mCountryCode.simCardRemoved();
                    WifiServiceImpl.this.mWifiStateMachine.notifyImsiAvailabe(false);
                } else if ("LOCKED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "SIM is locked");
                    WifiServiceImpl.this.mWifiStateMachine.notifyImsiAvailabe(false);
                } else if ("IMSI".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "SIM is available");
                    WifiServiceImpl.this.mWifiStateMachine.notifyImsiAvailabe(true);
                } else if ("LOADED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was loaded");
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks(true);
                }
            }
        }, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiServiceImpl.this.handleWifiApStateChange(intent.getIntExtra("wifi_state", 11), intent.getIntExtra("previous_wifi_state", 11), intent.getIntExtra("wifi_ap_error_code", -1), intent.getStringExtra("wifi_ap_interface_name"), intent.getIntExtra("wifi_ap_mode", -1));
            }
        }, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        if (!WifiServiceHisiExt.hisiWifiEnabled()) {
            registerForBroadcasts();
            registerForPackageOrUserRemoval();
        } else if (!this.mWifiServiceHisiExt.mIsReceiverRegistered) {
            registerForBroadcasts();
            registerForPackageOrUserRemoval();
            this.mWifiServiceHisiExt.mIsReceiverRegistered = true;
        }
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        int waitRetry = 0;
        while (waitRetry < 10 && this.mWifiStateMachineChannel == null) {
            Log.e(TAG, "wait connect to WifiStateMachineChannel sleep" + waitRetry);
            try {
                Thread.sleep(100);
                waitRetry++;
            } catch (InterruptedException e) {
                Log.e(TAG, "exception happened");
            }
        }
        if (!this.mWifiStateMachine.syncInitialize(this.mWifiStateMachineChannel)) {
            Log.wtf(TAG, "Failed to initialize WifiStateMachine");
        }
        this.mWifiController.start();
        HwWifiServiceFactory.getHwWifiServiceManager().createHwArpVerifier(this.mContext);
        if (wifiEnabled) {
            try {
                setWifiEnabled(this.mContext.getPackageName(), wifiEnabled);
            } catch (RemoteException e2) {
            }
        }
        this.mWifiStateMachine.setLocalMacAddressFromMacfile();
        this.mWifiController.createQoEEngineService(this.mContext, this.mWifiStateMachine);
        this.mWifiController.setupHwSelfCureEngine(this.mContext, this.mWifiStateMachine);
        this.mWifiController.createWifiProStateMachine(this.mContext, this.mWifiStateMachine.getMessenger());
        this.mWifiController.createABSService(this.mContext, this.mWifiStateMachine);
    }

    public void handleUserSwitch(int userId) {
        this.mWifiStateMachine.handleUserSwitch(userId);
    }

    public void handleUserUnlock(int userId) {
        this.mWifiStateMachine.handleUserUnlock(userId);
    }

    public void handleUserStop(int userId) {
        this.mWifiStateMachine.handleUserStop(userId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:88:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x020e  */
    /* JADX WARNING: Missing block: B:51:0x019e, code:
            if (r21 == null) goto L_0x01bc;
     */
    /* JADX WARNING: Missing block: B:52:0x01a0, code:
            r13 = new android.net.wifi.ScanSettings(r21);
     */
    /* JADX WARNING: Missing block: B:53:0x01ab, code:
            if (r13.isValid() != false) goto L_0x01ba;
     */
    /* JADX WARNING: Missing block: B:54:0x01ad, code:
            android.util.Slog.e(TAG, "invalid scan setting");
     */
    /* JADX WARNING: Missing block: B:55:0x01b6, code:
            return;
     */
    /* JADX WARNING: Missing block: B:59:0x01ba, code:
            r21 = r13;
     */
    /* JADX WARNING: Missing block: B:60:0x01bc, code:
            if (r22 == null) goto L_0x01c4;
     */
    /* JADX WARNING: Missing block: B:61:0x01be, code:
            enforceWorkSourcePermission();
            r22.clearNames();
     */
    /* JADX WARNING: Missing block: B:62:0x01c4, code:
            if (r22 != null) goto L_0x01d7;
     */
    /* JADX WARNING: Missing block: B:64:0x01ca, code:
            if (android.os.Binder.getCallingUid() < 0) goto L_0x01d7;
     */
    /* JADX WARNING: Missing block: B:65:0x01cc, code:
            r0 = new android.os.WorkSource(android.os.Binder.getCallingUid());
     */
    /* JADX WARNING: Missing block: B:66:0x01d7, code:
            r14 = android.os.Binder.getCallingUid();
            r8 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:67:0x01df, code:
            if (r23 == null) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:70:0x01e5, code:
            if (r23.length() == 0) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:73:0x01f7, code:
            if ((r20.mWifiPermissionsUtil.canAccessScanResults(r23, r14, 23) ^ 1) == 0) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:75:0x01fd, code:
            android.os.Binder.restoreCallingIdentity(r8);
     */
    /* JADX WARNING: Missing block: B:77:0x020c, code:
            if (r20.mWifiStateMachine.allowWifiScanRequest(android.os.Binder.getCallingPid()) == false) goto L_0x023d;
     */
    /* JADX WARNING: Missing block: B:78:0x020e, code:
            android.util.Slog.d(TAG, "wifi_scan reject because the interval isn't arrived");
     */
    /* JADX WARNING: Missing block: B:79:0x0217, code:
            return;
     */
    /* JADX WARNING: Missing block: B:80:0x0218, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:82:?, code:
            android.util.Log.w(TAG, "Exception:", r5);
     */
    /* JADX WARNING: Missing block: B:83:0x0228, code:
            r12 = move-exception;
     */
    /* JADX WARNING: Missing block: B:85:?, code:
            android.util.Log.w(TAG, "SecurityException:", r12);
     */
    /* JADX WARNING: Missing block: B:87:0x0239, code:
            android.os.Binder.restoreCallingIdentity(r8);
     */
    /* JADX WARNING: Missing block: B:88:0x023d, code:
            r15 = r20.scanRequestCounter;
            r20.scanRequestCounter = r15 + 1;
     */
    /* JADX WARNING: Missing block: B:89:0x0253, code:
            if (startQuickttffScan(r22, r23, r15) != false) goto L_0x0255;
     */
    /* JADX WARNING: Missing block: B:90:0x0255, code:
            return;
     */
    /* JADX WARNING: Missing block: B:92:0x025e, code:
            if (limitWifiScanRequest(r23) != false) goto L_0x0260;
     */
    /* JADX WARNING: Missing block: B:93:0x0260, code:
            android.util.Log.d(TAG, "current scan request is refused " + r23);
            sendFailedScanDirectionalBroadcast(r23);
     */
    /* JADX WARNING: Missing block: B:94:0x0285, code:
            return;
     */
    /* JADX WARNING: Missing block: B:96:0x028e, code:
            if (limitWifiScanInAbsoluteRest(r23) != false) goto L_0x0290;
     */
    /* JADX WARNING: Missing block: B:97:0x0290, code:
            android.util.Log.d(TAG, "absolute rest, scan request is refused " + r23);
            sendFailedScanDirectionalBroadcast(r23);
     */
    /* JADX WARNING: Missing block: B:98:0x02b5, code:
            return;
     */
    /* JADX WARNING: Missing block: B:99:0x02b6, code:
            r20.mHwWifiCHRService = com.android.server.wifi.HwWifiServiceFactory.getHwWifiCHRService();
     */
    /* JADX WARNING: Missing block: B:100:0x02c2, code:
            if (r20.mHwWifiCHRService != null) goto L_0x02c4;
     */
    /* JADX WARNING: Missing block: B:101:0x02c4, code:
            r20.mHwWifiCHRService.updateApkChangewWifiStatus(4, r23);
     */
    /* JADX WARNING: Missing block: B:102:0x02d5, code:
            if (r20.mWifiStatStore != null) goto L_0x02d7;
     */
    /* JADX WARNING: Missing block: B:103:0x02d7, code:
            r20.mWifiStatStore.setBackgroundScanReq(isRequestFromBackground(r23));
     */
    /* JADX WARNING: Missing block: B:104:0x02e6, code:
            r15 = r20.mWifiStateMachine;
            r16 = android.os.Binder.getCallingUid();
            r17 = r20.scanRequestCounter;
            r20.scanRequestCounter = r17 + 1;
            r15.startScan(r16, r17, r21, r22);
     */
    /* JADX WARNING: Missing block: B:105:0x0307, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startScan(ScanSettings settings, WorkSource workSource, String packageName) {
        String appName = packageName;
        Slog.d(TAG, "startScan, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", appName:" + packageName);
        if (this.mPowerManager.isScreenOn() || ("com.huawei.ca".equals(packageName) ^ 1) == 0 || ("com.huawei.parentcontrol".equals(packageName) ^ 1) == 0 || Binder.getCallingUid() == 1000 || ("com.huawei.hidisk".equals(packageName) ^ 1) == 0) {
            enforceChangePermission();
            this.mLog.trace("startScan uid=%").c((long) Binder.getCallingUid()).flush();
            if (isRequestFromBackground(packageName)) {
                long lastScanMs;
                synchronized (this.mLastScanTimestamps) {
                    lastScanMs = ((Long) this.mLastScanTimestamps.getOrDefault(packageName, Long.valueOf(0))).longValue();
                }
                long elapsedRealtime = this.mClock.getElapsedSinceBootMillis();
                if (lastScanMs == 0 || elapsedRealtime - lastScanMs >= this.mBackgroundThrottleInterval) {
                    synchronized (this.mLastScanTimestamps) {
                        this.mLastScanTimestamps.put(packageName, Long.valueOf(elapsedRealtime));
                    }
                } else {
                    Log.d(TAG, "current scan request failed " + packageName);
                    sendFailedScanDirectionalBroadcast(packageName);
                    return;
                }
            }
            if (limitForegroundWifiScanRequest(packageName, Binder.getCallingUid())) {
                Log.d(TAG, "current foreground scan request is refused " + packageName);
                sendFailedScanDirectionalBroadcast(packageName);
                return;
            }
            synchronized (this) {
                if (this.mWifiScanner == null) {
                    this.mWifiScanner = this.mWifiInjector.getWifiScanner();
                }
                if (this.mInIdleMode) {
                    sendFailedScanBroadcast();
                    this.mScanPending = true;
                    return;
                }
            }
        }
        Slog.i(TAG, "Screen is off, " + packageName + " startScan is skipped.");
    }

    private void sendFailedScanBroadcast() {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
            intent.addFlags(67108864);
            intent.putExtra("resultsUpdated", false);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private void sendFailedScanDirectionalBroadcast(String packageName) {
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

    private boolean isRequestFromBackground(String packageName) {
        boolean z = false;
        if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1010) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), packageName);
        if (this.mBackgroundThrottlePackageWhitelist.contains(packageName)) {
            return false;
        }
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

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        enforceConnectivityInternalPermission();
        this.mLog.trace("getCurrentNetworkWpsNfcConfigurationToken uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.syncGetCurrentNetworkWpsNfcConfigurationToken();
    }

    void handleIdleModeChanged() {
        boolean doScan = false;
        synchronized (this) {
            boolean idle = this.mPowerManager.isDeviceIdleMode();
            if (this.mInIdleMode != idle) {
                this.mInIdleMode = idle;
                if (!idle && this.mScanPending) {
                    this.mScanPending = false;
                    doScan = true;
                }
                setFilterEnable(idle);
            }
        }
        if (doScan) {
            startScan(null, null, "");
        }
    }

    private void setFilterEnable(boolean enable) {
        Slog.d(TAG, "setFilterEnable:" + enable);
        this.mWifiNative.setFilterEnable(enable);
    }

    private void enforceNetworkSettingsPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_SETTINGS", TAG);
    }

    private void enforceNetworkStackPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
    }

    private void enforceLocationHardwarePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.LOCATION_HARDWARE", "LocationHardware");
    }

    private void enforceReadCredentialPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_WIFI_CREDENTIAL", TAG);
    }

    private void enforceWorkSourcePermission() {
        this.mContext.enforceCallingPermission("android.permission.UPDATE_DEVICE_STATS", TAG);
    }

    private void enforceMulticastChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_MULTICAST_STATE", TAG);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    protected String getAppName(int pID) {
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

    private void setHisiWifiApEnabled(boolean enabled, WifiConfiguration wifiConfig, WifiController mWifiController) {
        this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiApEnabled(enabled);
        if (wifiConfig == null || isValid(wifiConfig)) {
            int i;
            if (enabled) {
                i = 1;
            } else {
                i = 0;
            }
            mWifiController.obtainMessage(155658, i, 0, wifiConfig).sendToTarget();
            return;
        }
        Slog.e(TAG, "Invalid WifiConfiguration");
    }

    private void showP2pToAPDialog(final WifiConfiguration wifiConfig, final boolean enabled) {
        if (this.mIsP2pCloseDialogExist) {
            Slog.d(TAG, "the dialog already exist don't show dialog again");
            return;
        }
        Slog.d(TAG, "showP2pToAPDialog enter");
        Resources r = Resources.getSystem();
        CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        checkBox.setChecked(false);
        checkBox.setText(r.getString(33685815));
        checkBox.setTextSize(14.0f);
        checkBox.setTextColor(-16777216);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiServiceImpl.this.mIsApDialogNeedShow = isChecked;
            }
        });
        AlertDialog dialog = new Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(33685813)).setMessage(r.getString(33685814)).setView(checkBox).setNegativeButton(r.getString(17039360), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "NegativeButton is click");
                WifiServiceImpl.this.setWifiApStateByManual(false);
            }
        }).setPositiveButton(r.getString(17039370), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "PositiveButton is click");
                if (WifiServiceImpl.this.mWifiServiceHisiExt.isAirplaneModeOn()) {
                    Slog.d(WifiServiceImpl.TAG, "Cann't start AP with airPlaneMode on");
                    return;
                }
                WifiServiceImpl.this.mHiSiWifiComm.changeShowDialogFlag("show_ap_dialog_flag", WifiServiceImpl.this.mIsApDialogNeedShow);
                WifiServiceImpl.this.setHisiWifiApEnabled(enabled, wifiConfig, WifiServiceImpl.this.mWifiController);
            }
        }).create();
        dialog.getWindow().setType(2014);
        dialog.show();
        this.mIsP2pCloseDialogExist = true;
        Slog.d(TAG, "dialog showed");
    }

    private void showP2pToStaDialog() {
        if (this.mIsP2pCloseDialogExist) {
            Slog.d(TAG, "the dialog already exist don't show dialog again");
            return;
        }
        Slog.d(TAG, "showP2pToStaDialog enter");
        Resources r = Resources.getSystem();
        CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        checkBox.setChecked(false);
        checkBox.setText(r.getString(33685815));
        checkBox.setTextSize(14.0f);
        checkBox.setTextColor(-16777216);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiServiceImpl.this.mIsDialogNeedShow = isChecked;
            }
        });
        AlertDialog dialog = new Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(33685811)).setMessage(r.getString(33685812)).setView(checkBox).setNegativeButton(r.getString(17039360), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "NegativeButton is click");
                WifiServiceImpl.this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.sendWifiStateDisabledBroadcast();
            }
        }).setPositiveButton(r.getString(17039370), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "PositiveButton is click");
                if (WifiServiceImpl.this.mWifiServiceHisiExt.isWifiP2pEnabled() || !(1 == WifiServiceImpl.this.getWifiEnabledState() || WifiServiceImpl.this.getWifiEnabledState() == 0)) {
                    WifiServiceImpl.this.mHiSiWifiComm.changeShowDialogFlag("show_sta_dialog_flag", WifiServiceImpl.this.mIsDialogNeedShow);
                    WifiServiceImpl.this.setWifiStateByManual(true);
                    WifiServiceImpl.this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                    return;
                }
                Slog.d(WifiServiceImpl.TAG, "supplicant is closed ,enble wifi with start supplicant");
                try {
                    WifiServiceImpl.this.setWifiEnabled(WifiServiceImpl.this.mContext.getPackageName(), true);
                } catch (RemoteException e) {
                    Slog.d(WifiServiceImpl.TAG, "setWifiEnabled fail, RemoteException e");
                }
            }
        }).create();
        dialog.getWindow().setType(2014);
        dialog.show();
        this.mIsP2pCloseDialogExist = true;
        Slog.d(TAG, "dialog showed");
    }

    private void enforceLocationPermission(String pkgName, int uid) {
        this.mWifiPermissionsUtil.enforceLocationPermission(pkgName, uid);
    }

    private boolean checkNetworkSettingsPermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.NETWORK_SETTINGS") == 0) {
            return true;
        }
        return false;
    }

    public synchronized boolean setWifiEnabled(String packageName, boolean enable) throws RemoteException {
        String appName = getAppName(Binder.getCallingUid());
        enforceChangePermission();
        if (HwDeviceManager.disallowOp(0)) {
            Slog.i(TAG, "Wifi has been restricted by MDM apk.");
            return false;
        } else if (mdmForPolicyForceOpenWifi(true, enable)) {
            Slog.w(TAG, "mdm force open wifi, not allow close wifi");
            return false;
        } else {
            Slog.d(TAG, "setWifiEnabled: " + enable + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", package=" + packageName);
            this.mLog.trace("setWifiEnabled package=% uid=% enable=%").c(packageName).c((long) Binder.getCallingUid()).c(enable).flush();
            boolean apEnabled = this.mWifiStateMachine.syncGetWifiApState() != 13 ? this.mWifiStateMachine.syncGetWifiApState() == 12 : true;
            boolean isFromSettings = checkNetworkSettingsPermission();
            if (!apEnabled || (isFromSettings ^ 1) == 0) {
                this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
                if (this.mHwWifiCHRService != null) {
                    if (enable) {
                        this.mHwWifiCHRService.updateApkChangewWifiStatus(5, packageName);
                    } else {
                        this.mHwWifiCHRService.updateApkChangewWifiStatus(1, packageName);
                    }
                }
                if (this.mContext == null || (HwSystemManager.allowOp(this.mContext, 2097152, enable) ^ 1) == 0) {
                    if (WifiServiceHisiExt.hisiWifiEnabled()) {
                        Slog.d(TAG, "setWifiEnabled, P2P enable flag is " + this.mWifiServiceHisiExt.isWifiP2pEnabled());
                        SystemProperties.set("sys.open_wifi_pid", Integer.toString(Binder.getCallingPid()));
                        if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && enable) {
                            int flag = this.mHiSiWifiComm.getSettingsGlobalIntValue("show_sta_dialog_flag");
                            if (!this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                                Slog.d(TAG, "the software have no some important permissions,return false.");
                                return false;
                            } else if (this.mHiSiWifiComm.isP2pConnect() && flag != 1) {
                                Slog.e(TAG, "sendEmptyMessage CMD_CHANGE_TO_STA_P2P_CONNECT");
                                this.mWifiStateMachineHandler.sendEmptyMessage(WifiStateMachine.CMD_CHANGE_TO_STA_P2P_CONNECT);
                            } else if (this.mIsP2pCloseDialogExist) {
                                Slog.d(TAG, "the p2p to AP dialog already exist cann't open wifi");
                                return false;
                            } else {
                                setWifiStateByManual(true);
                                this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                            }
                        } else if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && (enable ^ 1) != 0) {
                            if (this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                                this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                            } else {
                                Slog.d(TAG, "the software have no some important permissions.");
                                return false;
                            }
                        }
                    }
                    if (Binder.getCallingUid() == 0 || !"factory".equals(SystemProperties.get("ro.runmode", "normal")) || SystemProperties.getInt("wlan.wltest.status", 0) <= 0) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            if (this.mSettingsStore.handleWifiToggled(enable)) {
                                Binder.restoreCallingIdentity(ident);
                                if (this.mPermissionReviewRequired) {
                                    int wiFiEnabledState = getWifiEnabledState();
                                    if (enable) {
                                        if ((wiFiEnabledState == 0 || wiFiEnabledState == 1) && startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_ENABLE")) {
                                            return true;
                                        }
                                    } else if ((wiFiEnabledState == 2 || wiFiEnabledState == 3) && startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_DISABLE")) {
                                        return true;
                                    }
                                }
                                this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
                                if (this.mHwWifiCHRService != null) {
                                    this.mHwWifiCHRService.updateWifiTriggerState(enable);
                                }
                                if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
                                    setWifiEnabledAfterVoWifiOff(enable);
                                } else {
                                    this.mWifiController.sendMessage(155656);
                                }
                                return true;
                            }
                            Slog.d(TAG, "setWifiEnabled,Nothing to do if wifi cannot be toggled.");
                            if (enable) {
                                this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:cannot be toggled}");
                            } else {
                                this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:cannot be toggled}");
                            }
                            return true;
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else {
                        Slog.e(TAG, "in wltest mode, dont allow to enable WiFi");
                        return false;
                    }
                }
                if (enable) {
                    this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:permission deny}");
                } else {
                    this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:permission deny}");
                }
                return false;
            }
            this.mLog.trace("setWifiEnabled SoftAp not disabled: only Settings can enable wifi").flush();
            return false;
        }
    }

    public void setWifiStateByManual(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            Slog.d(TAG, "setWifiStateByManual:" + enable + ",mIsReceiverRegistered:" + this.mWifiServiceHisiExt.mIsReceiverRegistered);
            if (enable) {
                if (!this.mWifiServiceHisiExt.mIsReceiverRegistered) {
                    registerForBroadcasts();
                    registerForPackageOrUserRemoval();
                    this.mWifiServiceHisiExt.mIsReceiverRegistered = true;
                }
            } else if (this.mWifiServiceHisiExt.mIsReceiverRegistered) {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mContext.unregisterReceiver(this.mPackageOrUserReceiver);
                this.mWifiServiceHisiExt.mIsReceiverRegistered = false;
            }
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiStateByManual(enable);
        }
    }

    public void setWifiApStateByManual(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiApStateByManual(enable);
        }
    }

    public void setWifiEnableForP2p(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiEnableForP2p(enable);
        }
    }

    public int getWifiEnabledState() {
        enforceAccessPermission();
        this.mLog.trace("getWifiEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.syncGetWifiState();
    }

    public void setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        if (HwWifiServiceFactory.getHwWifiServiceManager().custSttingsEnableSoftap(getAppName(Binder.getCallingPid()))) {
            Log.d(TAG, "only settings enable softap");
            return;
        }
        enforceChangePermission();
        Slog.d(TAG, "setWifiApEnabled: " + enabled + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        this.mWifiPermissionsUtil.enforceTetherChangePermission(this.mContext);
        this.mLog.trace("setWifiApEnabled uid=% enable=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
            throw new SecurityException("DISALLOW_CONFIG_TETHERING is enabled for this user.");
        }
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, POLICY_OPEN_HOTSPOT);
        if (bundle != null) {
            WifiConfiguration apConfig = wifiConfig == null ? getWifiApConfiguration() : wifiConfig;
            if (enabled && ((apConfig == null || apConfig.preSharedKey == null) && bundle.getBoolean(VALUE_DISABLE))) {
                Slog.w(TAG, "setWifiApEnabled: MDM deny start unsecurity soft ap!");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        Toast.makeText(WifiServiceImpl.this.mContext, WifiServiceImpl.this.mContext.getString(33685942), 0).show();
                    }
                });
                return;
            }
        }
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            int flag = this.mHiSiWifiComm.getSettingsGlobalIntValue("show_ap_dialog_flag");
            if (enabled) {
                SystemProperties.set("sys.ap_open_flag", "1");
                Slog.d(TAG, "sys.ap_open_flag is on");
            } else {
                SystemProperties.set("sys.ap_open_flag", HwWifiCHRStateManager.TYPE_AP_VENDOR);
                Slog.d(TAG, "sys.ap_open_flag is off");
            }
            if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && !this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                Slog.d(TAG, "the software have no some important permissions,start ap failed.");
            } else if (this.mHiSiWifiComm.isP2pConnect() && flag != 1) {
                Slog.e(TAG, "sendEmptyMessage CMD_CHANGE_TO_AP_P2P_CONNECT");
                Bundle data = new Bundle();
                data.putBoolean("isWifiApEnabled", enabled);
                data.putParcelable("wifiConfig", wifiConfig);
                Message msg = new Message();
                msg.what = WifiStateMachine.CMD_CHANGE_TO_AP_P2P_CONNECT;
                msg.setData(data);
                this.mWifiStateMachineHandler.sendMessage(msg);
            } else if (this.mIsP2pCloseDialogExist) {
                Slog.d(TAG, "the p2p to STA dialog already exist cann't open AP");
            } else {
                setHisiWifiApEnabled(enabled, wifiConfig, this.mWifiController);
            }
        } else if (wifiConfig == null || isValid(wifiConfig)) {
            this.mWifiController.sendMessage(155658, enabled ? 1 : 0, 0, new SoftApModeConfiguration(-1, wifiConfig));
        } else {
            Slog.e(TAG, "Invalid WifiConfiguration");
        }
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        this.mLog.trace("getWifiApEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.syncGetWifiApState();
    }

    public void updateInterfaceIpState(String ifaceName, int mode) {
        enforceNetworkStackPermission();
        this.mClientHandler.post(new -$Lambda$keJ9BFfhvqUxFWHG1mguXysQKkc(mode, this, ifaceName));
    }

    /* JADX WARNING: Missing block: B:11:0x005b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateInterfaceIpStateInternal(String ifaceName, int mode) {
        synchronized (this.mLocalOnlyHotspotRequests) {
            Integer previousMode = Integer.valueOf(-1);
            if (ifaceName != null) {
                previousMode = (Integer) this.mIfaceIpModes.put(ifaceName, Integer.valueOf(mode));
            }
            Slog.d(TAG, "updateInterfaceIpState: ifaceName=" + ifaceName + " mode=" + mode + " previous mode= " + previousMode);
            switch (mode) {
                case -1:
                    if (ifaceName == null) {
                        this.mIfaceIpModes.clear();
                        return;
                    }
                    break;
                case 0:
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                    updateInterfaceIpStateInternal(null, -1);
                    break;
                case 1:
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(3);
                    break;
                case 2:
                    if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                        sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked();
                        break;
                    }
                    stopSoftAp();
                    updateInterfaceIpStateInternal(null, -1);
                    return;
                default:
                    this.mLog.trace("updateInterfaceIpStateInternal: unknown mode %").c((long) mode).flush();
                    break;
            }
        }
    }

    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        boolean startSoftApInternal;
        enforceNetworkStackPermission();
        int pid = Binder.getCallingPid();
        Slog.d(TAG, "startSoftAp: pid=" + pid + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(pid));
        this.mLog.trace("startSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                stopSoftApInternal();
            }
            startSoftApInternal = startSoftApInternal(wifiConfig, 1);
        }
        return startSoftApInternal;
    }

    private boolean startSoftApInternal(WifiConfiguration wifiConfig, int mode) {
        this.mLog.trace("startSoftApInternal uid=% mode=%").c((long) Binder.getCallingUid()).c((long) mode).flush();
        if (wifiConfig == null || isValid(wifiConfig)) {
            this.mWifiController.sendMessage(155658, 1, 0, new SoftApModeConfiguration(mode, wifiConfig));
            return true;
        }
        Slog.e(TAG, "Invalid WifiConfiguration");
        return false;
    }

    public boolean stopSoftAp() {
        boolean stopSoftApInternal;
        enforceNetworkStackPermission();
        int pid = Binder.getCallingPid();
        Slog.d(TAG, "stopSoftAp: pid=" + pid + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(pid));
        this.mLog.trace("stopSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLog.trace("Call to stop Tethering while LOHS is active, Registered LOHS callers will be updated when softap stopped.");
            }
            stopSoftApInternal = stopSoftApInternal();
        }
        return stopSoftApInternal;
    }

    private boolean stopSoftApInternal() {
        this.mLog.trace("stopSoftApInternal uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiController.sendMessage(155658, 0, 0);
        return true;
    }

    private void handleWifiApStateChange(int currentState, int previousState, int errorCode, String ifaceName, int mode) {
        Slog.d(TAG, "handleWifiApStateChange: currentState=" + currentState + " previousState=" + previousState + " errorCode= " + errorCode + " ifaceName=" + ifaceName + " mode=" + mode);
        if (currentState == 14) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                int errorToReport = 2;
                if (errorCode == 1) {
                    errorToReport = 1;
                }
                sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(errorToReport);
                updateInterfaceIpStateInternal(null, -1);
            }
        } else if (currentState == 10 || currentState == 11) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                if (this.mIfaceIpModes.contains(Integer.valueOf(2))) {
                    sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked();
                } else {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                }
                updateInterfaceIpState(null, -1);
            }
        }
    }

    private void sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(int arg1) {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotFailedMessage(arg1);
                requestor.unlinkDeathRecipient();
            } catch (RemoteException e) {
            }
        }
        this.mLocalOnlyHotspotRequests.clear();
    }

    private void sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked() {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotStoppedMessage();
                requestor.unlinkDeathRecipient();
            } catch (RemoteException e) {
            }
        }
        this.mLocalOnlyHotspotRequests.clear();
    }

    private void sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked() {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
            } catch (RemoteException e) {
            }
        }
    }

    void registerLOHSForTest(int pid, LocalOnlyHotspotRequestInfo request) {
        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
    }

    public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        enforceChangePermission();
        enforceLocationPermission(packageName, uid);
        if (this.mSettingsStore.getLocationModeSetting(this.mContext) == 0) {
            throw new SecurityException("Location mode is not enabled.");
        } else if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
            return 4;
        } else {
            try {
                if (!this.mFrameworkFacade.isAppForeground(uid)) {
                    return 3;
                }
                Slog.d(TAG, "startLocalOnlyHotspot: pid=" + pid + ", uid=" + uid + ", packageName=" + packageName);
                this.mLog.trace("startLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
                synchronized (this.mLocalOnlyHotspotRequests) {
                    if (this.mIfaceIpModes.contains(Integer.valueOf(1))) {
                        this.mLog.trace("Cannot start localOnlyHotspot when WiFi Tethering is active.");
                        return 3;
                    } else if (((LocalOnlyHotspotRequestInfo) this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid))) != null) {
                        this.mLog.trace("caller already has an active request");
                        throw new IllegalStateException("Caller already has an active LocalOnlyHotspot request");
                    } else {
                        LocalOnlyHotspotRequestInfo request = new LocalOnlyHotspotRequestInfo(binder, messenger, new LocalOnlyRequestorCallback());
                        if (this.mIfaceIpModes.contains(Integer.valueOf(2))) {
                            try {
                                this.mLog.trace("LOHS already up, trigger onStarted callback");
                                request.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
                            } catch (RemoteException e) {
                                return 2;
                            }
                        } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                            this.mLocalOnlyHotspotConfig = WifiApConfigStore.generateLocalOnlyHotspotConfig(this.mContext);
                            startSoftApInternal(this.mLocalOnlyHotspotConfig, 2);
                        }
                        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
                        return 0;
                    }
                }
            } catch (RemoteException e2) {
                this.mLog.trace("RemoteException during isAppForeground when calling startLOHS");
                return 3;
            }
        }
    }

    public void stopLocalOnlyHotspot() {
        enforceChangePermission();
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        Slog.d(TAG, "stopLocalOnlyHotspot: pid=" + pid + ", uid=" + uid + ", name=" + getAppName(pid));
        this.mLog.trace("stopLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            LocalOnlyHotspotRequestInfo requestInfo = (LocalOnlyHotspotRequestInfo) this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid));
            if (requestInfo == null) {
                return;
            }
            requestInfo.unlinkDeathRecipient();
            unregisterCallingAppAndStopLocalOnlyHotspot(requestInfo);
        }
    }

    /* JADX WARNING: Missing block: B:17:0x004d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void unregisterCallingAppAndStopLocalOnlyHotspot(LocalOnlyHotspotRequestInfo request) {
        this.mLog.trace("unregisterCallingAppAndStopLocalOnlyHotspot pid=%").c((long) request.getPid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (this.mLocalOnlyHotspotRequests.remove(Integer.valueOf(request.getPid())) == null) {
                this.mLog.trace("LocalOnlyHotspotRequestInfo not found to remove");
            } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLocalOnlyHotspotConfig = null;
                updateInterfaceIpStateInternal(null, -1);
                long identity = Binder.clearCallingIdentity();
                try {
                    stopSoftApInternal();
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) {
        String packageName = this.mContext.getOpPackageName();
        enforceNetworkSettingsPermission();
        throw new UnsupportedOperationException("LocalOnlyHotspot is still in development");
    }

    public void stopWatchLocalOnlyHotspot() {
        enforceNetworkSettingsPermission();
        throw new UnsupportedOperationException("LocalOnlyHotspot is still in development");
    }

    public WifiConfiguration getWifiApConfiguration() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkConfigOverridePermission(uid)) {
            this.mLog.trace("getWifiApConfiguration uid=%").c((long) uid).flush();
            return this.mWifiStateMachine.syncGetWifiApConfiguration();
        }
        throw new SecurityException("App not allowed to read or update stored WiFi Ap config (uid = " + uid + ")");
    }

    public void setWifiApConfiguration(WifiConfiguration wifiConfig) {
        enforceChangePermission();
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkConfigOverridePermission(uid)) {
            this.mLog.trace("setWifiApConfiguration uid=%").c((long) uid).flush();
            if (wifiConfig != null) {
                if (isValid(wifiConfig)) {
                    this.mWifiStateMachine.setWifiApConfiguration(wifiConfig);
                } else {
                    Slog.e(TAG, "Invalid WifiConfiguration");
                }
                return;
            }
            return;
        }
        throw new SecurityException("App not allowed to read or update stored WiFi AP config (uid = " + uid + ")");
    }

    public boolean isScanAlwaysAvailable() {
        enforceAccessPermission();
        this.mLog.trace("isScanAlwaysAvailable uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public void disconnect() {
        Slog.d(TAG, "disconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mLog.trace("disconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.disconnectCommand();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(8, getAppName(Binder.getCallingPid()));
        }
    }

    public void reconnect() {
        Slog.d(TAG, "reconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mLog.trace("reconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.reconnectCommand();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(6, getAppName(Binder.getCallingPid()));
        }
    }

    public void reassociate() {
        Slog.d(TAG, "reassociate:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mLog.trace("reassociate uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.reassociateCommand();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(7, getAppName(Binder.getCallingPid()));
        }
    }

    public int getSupportedFeatures() {
        enforceAccessPermission();
        this.mLog.trace("getSupportedFeatures uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetSupportedFeatures(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return 0;
    }

    public void requestActivityInfo(ResultReceiver result) {
        Bundle bundle = new Bundle();
        this.mLog.trace("requestActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    public WifiActivityEnergyInfo reportActivityInfo() {
        enforceAccessPermission();
        this.mLog.trace("reportActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo wifiActivityEnergyInfo = null;
        if (this.mWifiStateMachineChannel != null) {
            WifiLinkLayerStats stats = this.mWifiStateMachine.syncGetLinkLayerStats(this.mWifiStateMachineChannel);
            if (stats != null) {
                long[] txTimePerLevel;
                long rxIdleCurrent = (long) this.mContext.getResources().getInteger(17694898);
                long rxCurrent = (long) this.mContext.getResources().getInteger(17694862);
                long txCurrent = (long) this.mContext.getResources().getInteger(17694906);
                double voltage = ((double) this.mContext.getResources().getInteger(17694903)) / 1000.0d;
                long rxIdleTime = (long) ((stats.on_time - stats.tx_time) - stats.rx_time);
                if (stats.tx_time_per_level != null) {
                    txTimePerLevel = new long[stats.tx_time_per_level.length];
                    for (int i = 0; i < txTimePerLevel.length; i++) {
                        txTimePerLevel[i] = (long) stats.tx_time_per_level[i];
                    }
                } else {
                    txTimePerLevel = new long[0];
                }
                long energyUsed = (long) (((double) (((((long) stats.tx_time) * txCurrent) + (((long) stats.rx_time) * rxCurrent)) + (rxIdleTime * rxIdleCurrent))) * voltage);
                if (rxIdleTime < 0 || stats.on_time < 0 || stats.tx_time < 0 || stats.rx_time < 0 || energyUsed < 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" rxIdleCur=").append(rxIdleCurrent);
                    sb.append(" rxCur=").append(rxCurrent);
                    sb.append(" txCur=").append(txCurrent);
                    sb.append(" voltage=").append(voltage);
                    sb.append(" on_time=").append(stats.on_time);
                    sb.append(" tx_time=").append(stats.tx_time);
                    sb.append(" tx_time_per_level=").append(Arrays.toString(txTimePerLevel));
                    sb.append(" rx_time=").append(stats.rx_time);
                    sb.append(" rxIdleTime=").append(rxIdleTime);
                    sb.append(" energy=").append(energyUsed);
                    Log.d(TAG, " reportActivityInfo: " + sb.toString());
                }
                wifiActivityEnergyInfo = new WifiActivityEnergyInfo(this.mClock.getElapsedSinceBootMillis(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, rxIdleTime, energyUsed);
            }
            if (wifiActivityEnergyInfo == null || !wifiActivityEnergyInfo.isValid()) {
                return null;
            }
            return wifiActivityEnergyInfo;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getConfiguredNetworks() {
        enforceAccessPermission();
        this.mLog.trace("getConfiguredNetworks uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> configs = this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
            if (configs != null) {
                return new ParceledListSlice(configs);
            }
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        this.mLog.trace("getPrivilegedConfiguredNetworks uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> configs = this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel);
            if (configs != null) {
                return new ParceledListSlice(configs);
            }
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        enforceAccessPermission();
        this.mLog.trace("getMatchingWifiConfig uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getResources().getBoolean(17957059)) {
            return this.mWifiStateMachine.syncGetMatchingWifiConfig(scanResult, this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int addOrUpdateNetwork(WifiConfiguration config) {
        Slog.d(TAG, "addOrUpdateNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", config:" + config + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mLog.trace("addOrUpdateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (config.isPasspoint()) {
            PasspointConfiguration passpointConfig = PasspointProvider.convertFromWifiConfig(config);
            if (passpointConfig.getCredential() == null) {
                Slog.e(TAG, "Missing credential for Passpoint profile");
                return -1;
            }
            passpointConfig.getCredential().setCaCertificate(config.enterpriseConfig.getCaCertificate());
            passpointConfig.getCredential().setClientCertificateChain(config.enterpriseConfig.getClientCertificateChain());
            passpointConfig.getCredential().setClientPrivateKey(config.enterpriseConfig.getClientPrivateKey());
            if (addOrUpdatePasspointConfiguration(passpointConfig)) {
                return 0;
            }
            Slog.e(TAG, "Failed to add Passpoint profile");
            return -1;
        } else if (isValid(config)) {
            Slog.i("addOrUpdateNetwork", " uid = " + Integer.toString(Binder.getCallingUid()) + " SSID " + config.SSID + " nid=" + Integer.toString(config.networkId));
            if (config.networkId == -1) {
                config.creatorUid = Binder.getCallingUid();
            } else {
                config.lastUpdateUid = Binder.getCallingUid();
            }
            if (this.mWifiStateMachineChannel != null) {
                return this.mWifiStateMachine.syncAddOrUpdateNetwork(this.mWifiStateMachineChannel, config);
            }
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return -1;
        } else {
            Slog.e(TAG, "bad network configuration");
            return -1;
        }
    }

    public static void verifyCert(X509Certificate caCert) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
        CertPath path = factory.generateCertPath(Arrays.asList(new X509Certificate[]{caCert}));
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null, null);
        PKIXParameters params = new PKIXParameters(ks);
        params.setRevocationEnabled(false);
        validator.validate(path, params);
    }

    public boolean removeNetwork(int netId) {
        Slog.d(TAG, "removeNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mLog.trace("removeNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncRemoveNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers) {
        Slog.d(TAG, "enableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", disableOthers:" + disableOthers + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mLog.trace("enableNetwork uid=% disableOthers=%").c((long) Binder.getCallingUid()).c(disableOthers).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId) {
        String appName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "disableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + appName);
        enforceChangePermission();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(2, appName);
        }
        this.mLog.trace("disableNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDisableNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncRequestConnectionInfo();
    }

    public List<ScanResult> getScanResults(String callingPackage) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mWifiPermissionsUtil.canAccessScanResults(callingPackage, uid, 23)) {
                if (this.mWifiScanner == null) {
                    this.mWifiScanner = this.mWifiInjector.getWifiScanner();
                }
                List<ScanResult> scanResultsList = this.mWifiScanner.getSingleScanResults();
                Collections.sort(scanResultsList, new Comparator<ScanResult>() {
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
                    Log.d(TAG, "ScanResults exceed the max count. size = " + scanResultsList.size());
                    scanResultsList = scanResultsList.subList(0, 200);
                }
                logScanResultsListRestrictively(callingPackage, scanResultsList);
                Binder.restoreCallingIdentity(ident);
                return scanResultsList;
            }
            List<ScanResult> arrayList = new ArrayList();
            return arrayList;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void logScanResultsListRestrictively(String callingPackage, List<ScanResult> scanResultsList) {
        long currentLogTime = this.mClock.getElapsedSinceBootMillis();
        if ("com.android.settings".equals(callingPackage) && scanResultsList != null && currentLogTime - this.mLastLogScanResultsTime > 3000) {
            Set<String> ssids = new HashSet();
            StringBuilder sb = new StringBuilder();
            for (ScanResult scanResult : scanResultsList) {
                String ssid = scanResult.SSID;
                if (!ssids.contains(ssid)) {
                    ssids.add(ssid);
                    sb.append(ssid).append("|").append(scanResult.isHiLinkNetwork).append("|").append(scanResult.dot11vNetwork).append(" ");
                }
            }
            Log.d(TAG, "getScanResults: calling by " + callingPackage + "   includes: " + sb.toString());
            this.mLastLogScanResultsTime = currentLogTime;
        }
    }

    public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config) {
        enforceChangePermission();
        this.mLog.trace("addorUpdatePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getResources().getBoolean(17957059)) {
            return this.mWifiStateMachine.syncAddOrUpdatePasspointConfig(this.mWifiStateMachineChannel, config, Binder.getCallingUid());
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public boolean removePasspointConfiguration(String fqdn) {
        enforceChangePermission();
        this.mLog.trace("removePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getResources().getBoolean(17957059)) {
            return this.mWifiStateMachine.syncRemovePasspointConfig(this.mWifiStateMachineChannel, fqdn);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public List<PasspointConfiguration> getPasspointConfigurations() {
        enforceAccessPermission();
        this.mLog.trace("getPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getResources().getBoolean(17957059)) {
            return this.mWifiStateMachine.syncGetPasspointConfigs(this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        enforceAccessPermission();
        this.mLog.trace("queryPasspointIcon uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getResources().getBoolean(17957059)) {
            this.mWifiStateMachine.syncQueryPasspointIcon(this.mWifiStateMachineChannel, bssid, fileName);
            return;
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        this.mLog.trace("matchProviderWithCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.matchProviderWithCurrentNetwork(this.mWifiStateMachineChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mLog.trace("deauthenticateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.deauthenticateNetwork(this.mWifiStateMachineChannel, holdoff, ess);
    }

    public boolean saveConfiguration() {
        Slog.d(TAG, "saveConfiguration, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceChangePermission();
        this.mLog.trace("saveConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSaveConfig(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public void setCountryCode(String countryCode, boolean persist) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode + " with persist set to " + persist);
        enforceConnectivityInternalPermission();
        this.mLog.trace("setCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        long token = Binder.clearCallingIdentity();
        this.mCountryCode.setCountryCode(countryCode);
        Binder.restoreCallingIdentity(token);
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.setCountryCode(countryCode);
        }
    }

    public String getCountryCode() {
        enforceConnectivityInternalPermission();
        this.mLog.trace("getCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mCountryCode.getCountryCode();
    }

    public boolean isDualBandSupported() {
        this.mLog.trace("isDualBandSupported uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mContext.getResources().getBoolean(17957051);
    }

    @Deprecated
    public DhcpInfo getDhcpInfo() {
        enforceAccessPermission();
        this.mLog.trace("getDhcpInfo uid=%").c((long) Binder.getCallingUid()).flush();
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        DhcpInfo info = new DhcpInfo();
        if (dhcpResults.ipAddress != null && (dhcpResults.ipAddress.getAddress() instanceof Inet4Address)) {
            info.ipAddress = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.ipAddress.getAddress());
            info.netmask = NetworkUtils.prefixLengthToNetmaskInt(dhcpResults.ipAddress.getPrefixLength());
            Log.d("wifiserviceimpl", "netmask =" + info.netmask);
        }
        if (dhcpResults.gateway != null && (dhcpResults.gateway instanceof Inet4Address)) {
            info.gateway = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.gateway);
        }
        int dnsFound = 0;
        for (InetAddress dns : dhcpResults.dnsServers) {
            if (dns instanceof Inet4Address) {
                if (dnsFound == 0) {
                    info.dns1 = NetworkUtils.inetAddressToInt((Inet4Address) dns);
                } else {
                    info.dns2 = NetworkUtils.inetAddressToInt((Inet4Address) dns);
                }
                dnsFound++;
                if (dnsFound > 1) {
                    break;
                }
            }
        }
        Inet4Address serverAddress = dhcpResults.serverAddress;
        if (serverAddress != null) {
            info.serverAddress = NetworkUtils.inetAddressToInt(serverAddress);
        }
        info.leaseDuration = dhcpResults.leaseDuration;
        return info;
    }

    public void enableTdls(String remoteAddress, boolean enable) {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress cannot be null");
        }
        this.mLog.trace("enableTdls uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        TdlsTaskParams params = new TdlsTaskParams();
        params.remoteIpAddress = remoteAddress;
        params.enable = enable;
        new TdlsTask().execute(new TdlsTaskParams[]{params});
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        this.mLog.trace("enableTdlsWithMacAddress uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        if (remoteMacAddress == null) {
            throw new IllegalArgumentException("remoteMacAddress cannot be null");
        }
        this.mWifiStateMachine.enableTdls(remoteMacAddress, enable);
    }

    public Messenger getWifiServiceMessenger() {
        Slog.d(TAG, "getWifiServiceMessenger, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceAccessPermission();
        enforceChangePermission();
        this.mLog.trace("getWifiServiceMessenger uid=%").c((long) Binder.getCallingUid()).flush();
        return new Messenger(this.mClientHandler);
    }

    public void disableEphemeralNetwork(String SSID) {
        enforceAccessPermission();
        enforceChangePermission();
        this.mLog.trace("disableEphemeralNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.disableEphemeralNetwork(SSID);
    }

    private boolean startConsentUi(String packageName, int callingUid, String intentAction) throws RemoteException {
        if (UserHandle.getAppId(callingUid) == 1000) {
            return false;
        }
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 268435456, UserHandle.getUserId(callingUid)).uid != callingUid) {
                throw new SecurityException("Package " + callingUid + " not in uid " + callingUid);
            }
            Intent intent = new Intent(intentAction);
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
            this.mContext.startActivity(intent);
            return true;
        } catch (NameNotFoundException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private void registerForScanModeChange() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.mSettingsStore.handleWifiScanAlwaysAvailableToggled();
                WifiServiceImpl.this.mWifiController.sendMessage(155655);
            }
        });
    }

    private void registerForBackgroundThrottleChanges() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_scan_background_throttle_interval_ms"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.updateBackgroundThrottleInterval();
            }
        });
        this.mFrameworkFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_scan_background_throttle_package_whitelist"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.updateBackgroundThrottlingWhitelist();
            }
        });
    }

    private void updateBackgroundThrottleInterval() {
        this.mBackgroundThrottleInterval = this.mFrameworkFacade.getLongSetting(this.mContext, "wifi_scan_background_throttle_interval_ms", DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS);
    }

    private void updateBackgroundThrottlingWhitelist() {
        String setting = this.mFrameworkFacade.getStringSetting(this.mContext, "wifi_scan_background_throttle_package_whitelist");
        this.mBackgroundThrottlePackageWhitelist.clear();
        if (setting != null) {
            this.mBackgroundThrottlePackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        }
        this.mBackgroundThrottlePackageWhitelist.addAll(BackgroundAppScanManager.getInstance().getPackageWhiteList());
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        if (this.mContext.getResources().getBoolean(17957052)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        }
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        if (this.mContext.getResources().getBoolean(17957062)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
        }
        registerForBroadcastsEx(intentFilter);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    private void registerForPackageOrUserRemoval() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mPackageOrUserReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        if (args != null && args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args != null && args.length > 0 && "ipmanager".equals(args[0])) {
            String[] ipManagerArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipManagerArgs, 0, ipManagerArgs.length);
            this.mWifiStateMachine.dumpIpManager(fd, pw, ipManagerArgs);
        } else if (args == null || args.length <= 0 || !DUMP_ARG_SET_IPREACH_DISCONNECT.equals(args[0])) {
            pw.println("Wi-Fi is " + this.mWifiStateMachine.syncGetWifiStateByName());
            pw.println("Stay-awake conditions: " + this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", 0));
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            pw.println("mBackgroundThrottlePackageWhitelist " + this.mBackgroundThrottlePackageWhitelist);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mNotificationController.dump(fd, pw, args);
            this.mTrafficPoller.dump(fd, pw, args);
            pw.println();
            pw.println("Locks held:");
            this.mWifiLockManager.dump(pw);
            pw.println();
            this.mWifiMulticastLockManager.dump(pw);
            pw.println();
            this.mWifiStateMachine.dump(fd, pw, args);
            pw.println();
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
            pw.println();
            this.mWifiBackupRestore.dump(fd, pw, args);
            pw.println();
        } else {
            if (args.length > 1) {
                if (DUMP_ARG_SET_IPREACH_DISCONNECT_ENABLED.equals(args[1])) {
                    this.mWifiStateMachine.setIpReachabilityDisconnectEnabled(true);
                } else if (DUMP_ARG_SET_IPREACH_DISCONNECT_DISABLED.equals(args[1])) {
                    this.mWifiStateMachine.setIpReachabilityDisconnectEnabled(false);
                }
            }
            pw.println("IPREACH_DISCONNECT state is " + this.mWifiStateMachine.getIpReachabilityDisconnectEnabled());
        }
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        Slog.d(TAG, "acquireWifiLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", binder:" + binder + ", lockMode:" + lockMode + ", tag:" + tag + ", ws:" + ws);
        if (!this.mWifiLockManager.acquireWifiLock(lockMode, tag, binder, ws)) {
            return false;
        }
        this.mWifiController.sendMessage(155654);
        return true;
    }

    public void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        this.mLog.trace("updateWifiLockWorkSource uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiLockManager.updateWifiLockWorkSource(binder, ws);
    }

    public boolean releaseWifiLock(IBinder binder) {
        Slog.d(TAG, "releaseWifiLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", binder:" + binder);
        if (!this.mWifiLockManager.releaseWifiLock(binder)) {
            return false;
        }
        this.mWifiController.sendMessage(155654);
        return true;
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();
        this.mLog.trace("initializeMulticastFiltering uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.initializeFiltering();
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();
        this.mLog.trace("acquireMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.acquireLock(binder, tag);
    }

    public void releaseMulticastLock() {
        enforceMulticastChangePermission();
        this.mLog.trace("releaseMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.releaseLock();
    }

    public boolean isMulticastEnabled() {
        enforceAccessPermission();
        this.mLog.trace("isMulticastEnabled uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiMulticastLockManager.isMulticastEnabled();
    }

    public void enableVerboseLogging(int verbose) {
        enforceAccessPermission();
        this.mLog.trace("enableVerboseLogging uid=% verbose=%").c((long) Binder.getCallingUid()).c((long) verbose).flush();
        this.mFacade.setIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", verbose);
        enableVerboseLoggingInternal(verbose);
    }

    void enableVerboseLoggingInternal(int verbose) {
        this.mWifiStateMachine.enableVerboseLogging(verbose);
        this.mWifiLockManager.enableVerboseLogging(verbose);
        this.mWifiMulticastLockManager.enableVerboseLogging(verbose);
        this.mWifiInjector.getWifiLastResortWatchdog().enableVerboseLogging(verbose);
        this.mWifiInjector.getWifiBackupRestore().enableVerboseLogging(verbose);
        LogcatLog.enableVerboseLogging(verbose);
    }

    public int getVerboseLoggingLevel() {
        enforceAccessPermission();
        this.mLog.trace("getVerboseLoggingLevel uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0);
    }

    public void enableAggressiveHandover(int enabled) {
        enforceAccessPermission();
        this.mLog.trace("enableAggressiveHandover uid=% enabled=%").c((long) Binder.getCallingUid()).c((long) enabled).flush();
        this.mWifiStateMachine.enableAggressiveHandover(enabled);
    }

    public int getAggressiveHandover() {
        enforceAccessPermission();
        this.mLog.trace("getAggressiveHandover uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getAggressiveHandover();
    }

    public void setAllowScansWithTraffic(int enabled) {
        enforceAccessPermission();
        this.mLog.trace("setAllowScansWithTraffic uid=% enabled=%").c((long) Binder.getCallingUid()).c((long) enabled).flush();
        this.mWifiStateMachine.setAllowScansWithTraffic(enabled);
    }

    public int getAllowScansWithTraffic() {
        enforceAccessPermission();
        this.mLog.trace("getAllowScansWithTraffic uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getAllowScansWithTraffic();
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        enforceChangePermission();
        this.mLog.trace("setEnableAutoJoinWhenAssociated uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        return this.mWifiStateMachine.setEnableAutoJoinWhenAssociated(enabled);
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        enforceAccessPermission();
        this.mLog.trace("getEnableAutoJoinWhenAssociated uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getEnableAutoJoinWhenAssociated();
    }

    public WifiConnectionStatistics getConnectionStatistics() {
        enforceAccessPermission();
        enforceReadCredentialPermission();
        this.mLog.trace("getConnectionStatistics uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetConnectionStatistics(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        this.mLog.trace("factoryReset uid=%").c((long) Binder.getCallingUid()).flush();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                stopSoftApInternal();
                if (this.mContext != null && IS_ATT) {
                    System.putInt(this.mContext.getContentResolver(), SET_SSID_NAME, 1);
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                try {
                    setWifiEnabled(this.mContext.getOpPackageName(), true);
                } catch (RemoteException e) {
                }
                int i = 0;
                while (i < 10) {
                    if (this.mWifiStateMachineChannel != null) {
                        List<WifiConfiguration> networks = this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
                        if (networks != null) {
                            for (WifiConfiguration config : networks) {
                                removeNetwork(config.networkId);
                            }
                            saveConfiguration();
                        } else {
                            i++;
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    static boolean logAndReturnFalse(String s) {
        Log.d(TAG, s);
        return false;
    }

    public static boolean isValid(WifiConfiguration config) {
        String validity = checkValidity(config);
        return validity != null ? logAndReturnFalse(validity) : true;
    }

    public static String checkValidity(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return "allowed kmgmt";
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.get(10) && config.allowedKeyManagement.get(11)) {
                if (config.allowedKeyManagement.cardinality() != 4) {
                    return "include WAPI_PSK and WAPI_CERT but is still invalid for cardinality != 4";
                }
            } else if (config.allowedKeyManagement.cardinality() != 2) {
                return "invalid for cardinality != 2";
            }
            if (!config.allowedKeyManagement.get(2)) {
                return "not WPA_EAP";
            }
            if (!(config.allowedKeyManagement.get(3) || (config.allowedKeyManagement.get(1) ^ 1) == 0)) {
                return "not PSK or 8021X";
            }
        }
        if (config.getIpAssignment() == IpAssignment.STATIC) {
            StaticIpConfiguration staticIpConf = config.getStaticIpConfiguration();
            if (staticIpConf == null) {
                return "null StaticIpConfiguration";
            }
            if (staticIpConf.ipAddress == null) {
                return "null static ip Address";
            }
        }
        return null;
    }

    public Network getCurrentNetwork() {
        enforceAccessPermission();
        this.mLog.trace("getCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getCurrentNetwork();
    }

    public static String toHexString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\'').append(s).append('\'');
        for (int n = 0; n < s.length(); n++) {
            sb.append(String.format(" %02x", new Object[]{Integer.valueOf(s.charAt(n) & Constants.SHORT_MASK)}));
        }
        return sb.toString();
    }

    public void hideCertFromUnaffiliatedUsers(String alias) {
        this.mCertManager.hideCertFromUnaffiliatedUsers(alias);
    }

    public String[] listClientCertsForCurrentUser() {
        return this.mCertManager.listClientCertsForCurrentUser();
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mLog.trace("enableWifiConnectivityManager uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        this.mWifiStateMachine.enableWifiConnectivityManager(enabled);
    }

    public byte[] retrieveBackupData() {
        enforceNetworkSettingsPermission();
        this.mLog.trace("retrieveBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel == null) {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return null;
        }
        Slog.d(TAG, "Retrieving backup data");
        byte[] backupData = this.mWifiBackupRestore.retrieveBackupDataFromConfigurations(this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel));
        Slog.d(TAG, "Retrieved backup data");
        return backupData;
    }

    private void restoreNetworks(List<WifiConfiguration> configurations) {
        if (configurations == null) {
            Slog.e(TAG, "Backup data parse failed");
            return;
        }
        for (WifiConfiguration configuration : configurations) {
            int networkId = this.mWifiStateMachine.syncAddOrUpdateNetwork(this.mWifiStateMachineChannel, configuration);
            if (networkId == -1) {
                Slog.e(TAG, "Restore network failed: " + configuration.configKey());
            } else {
                this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, networkId, false);
            }
        }
    }

    public void restoreBackupData(byte[] data) {
        enforceNetworkSettingsPermission();
        this.mLog.trace("restoreBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel == null) {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromBackupData(data));
        Slog.d(TAG, "Restored backup data");
    }

    public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        enforceNetworkSettingsPermission();
        this.mLog.trace("restoreSupplicantBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel == null) {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring supplicant backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromSupplicantBackupData(supplicantData, ipConfigData));
        Slog.d(TAG, "Restored supplicant backup data");
    }

    protected void handleAirplaneModeToggled() {
        this.mWifiController.sendMessage(155657);
    }

    protected void onReceiveEx(Context context, Intent intent) {
    }

    protected void registerForBroadcastsEx(IntentFilter intentFilter) {
    }

    protected void setWifiEnabledAfterVoWifiOff(boolean enable) {
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        return false;
    }

    protected void handleForgetNetwork(Message msg) {
    }

    protected boolean mdmForPolicyForceOpenWifi(boolean showToast, boolean enable) {
        return false;
    }

    protected boolean startQuickttffScan(WorkSource workSource, String packageName, int scanRequestCounter) {
        return false;
    }

    protected boolean limitForegroundWifiScanRequest(String packageName, int uid) {
        return false;
    }

    protected boolean limitWifiScanRequest(String packageName) {
        return false;
    }

    protected boolean limitWifiScanInAbsoluteRest(String packageName) {
        return false;
    }

    protected String getPackageName(int pID) {
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID && appProcess.pkgList != null && appProcess.pkgList.length > 0) {
                return appProcess.pkgList[0];
            }
        }
        return null;
    }
}
