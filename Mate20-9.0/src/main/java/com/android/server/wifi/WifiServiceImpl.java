package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.Log;
import android.util.MutableInt;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.PowerProfile;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.LocalOnlyHotspotRequestInfo;
import com.android.server.wifi.hotspot2.PasspointProvider;
import com.android.server.wifi.util.WifiHandler;
import com.android.server.wifi.util.WifiPermissionsUtil;
import huawei.android.security.IHwBehaviorCollectManager;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class WifiServiceImpl extends IWifiManager.Stub {
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static final int BOTH_2G_AND_5G = 2;
    private static final boolean DBG = true;
    private static final long DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final int DEFAULT_VALUE = 0;
    private static final boolean IS_ATT;
    private static final boolean IS_VERIZON = ("389".equals(SystemProperties.get("ro.config.hw_opta", "0")) && "840".equals(SystemProperties.get("ro.config.hw_optb", "0")));
    private static final long LOG_SCAN_RESULTS_INTERVAL_MS = 3000;
    private static final int MAX_SLEEP_RETRY_TIMES = 10;
    private static final String NO_KNOW_NAME = "android";
    private static final int NUM_SOFT_AP_CALLBACKS_WARN_LIMIT = 10;
    private static final int NUM_SOFT_AP_CALLBACKS_WTF_LIMIT = 20;
    private static final int ONLY_2G = 1;
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    private static final int SCANRESULTS_COUNT_MAX = 200;
    private static final int SCAN_PROXY_RESULTS_MAX_AGE_IN_MILLIS = 30000;
    private static final String SET_SSID_NAME = "set_hotspot_ssid_name";
    private static final String TAG = "WifiService";
    private static final String VALUE_DISABLE = "value_disable";
    private static final boolean VDBG = false;
    private static final int WAIT_SLEEP_TIME = 100;
    /* access modifiers changed from: private */
    public static final boolean mIsWpsDisabled = SystemProperties.getBoolean("ro.config.hw_wifi_wps_disable", false);
    private static final Object mWifiLock = new Object();
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public ClientHandler mClientHandler;
    /* access modifiers changed from: private */
    public final Clock mClock;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFacade;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public HiSiWifiComm mHiSiWifiComm;
    private IHwBehaviorCollectManager mHwBehaviorManager;
    /* access modifiers changed from: private */
    public HwWifiCHRService mHwWifiCHRService;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private final ConcurrentHashMap<String, Integer> mIfaceIpModes;
    boolean mInIdleMode;
    boolean mInLightIdleMode;
    /* access modifiers changed from: private */
    public boolean mIsApDialogNeedShow;
    /* access modifiers changed from: private */
    public boolean mIsDialogNeedShow;
    /* access modifiers changed from: private */
    public boolean mIsP2pCloseDialogExist;
    private long mLastLogScanResultsTime;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private WifiConfiguration mLocalOnlyHotspotConfig;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private final HashMap<Integer, LocalOnlyHotspotRequestInfo> mLocalOnlyHotspotRequests;
    private WifiLog mLog;
    private final BroadcastReceiver mPackageOrUserReceiver;
    private final boolean mPermissionReviewRequired;
    private final PowerManager mPowerManager;
    PowerProfile mPowerProfile;
    private final BroadcastReceiver mReceiver;
    /* access modifiers changed from: private */
    public final HashMap<Integer, ISoftApCallback> mRegisteredSoftApCallbacks;
    boolean mScanPending;
    final ScanRequestProxy mScanRequestProxy;
    final WifiSettingsStore mSettingsStore;
    /* access modifiers changed from: private */
    public int mSoftApNumClients;
    /* access modifiers changed from: private */
    public int mSoftApState;
    /* access modifiers changed from: private */
    public WifiTrafficPoller mTrafficPoller;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled;
    private WifiApConfigStore mWifiApConfigStore;
    private final WifiBackupRestore mWifiBackupRestore;
    /* access modifiers changed from: private */
    public WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private final WifiLockManager mWifiLockManager;
    private final WifiMetrics mWifiMetrics;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private WifiNative mWifiNative;
    /* access modifiers changed from: private */
    public WifiPermissionsUtil mWifiPermissionsUtil;
    WifiServiceHisiExt mWifiServiceHisiExt = null;
    final WifiStateMachine mWifiStateMachine;
    /* access modifiers changed from: private */
    public AsyncChannel mWifiStateMachineChannel;
    WifiStateMachineHandler mWifiStateMachineHandler;
    final WifiStateMachinePrime mWifiStateMachinePrime;
    private int scanRequestCounter;
    private DataUploader uploader;

    private class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                        Slog.w(WifiServiceImpl.TAG, "Send failed, client connection lost");
                    } else {
                        Slog.w(WifiServiceImpl.TAG, "Client connection lost with reason: " + msg.arg1);
                    }
                    WifiServiceImpl.this.mTrafficPoller.removeClient(msg.replyTo);
                    return;
                case 151553:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151554)) {
                        WifiConfiguration config = (WifiConfiguration) msg.obj;
                        int networkId = msg.arg1;
                        Slog.d(WifiServiceImpl.TAG, "CONNECT  nid=" + Integer.toString(networkId) + " config=" + config + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        WifiServiceImpl.this.mWifiStateMachine.setCHRConnectingSartTimestamp(WifiServiceImpl.this.mClock.getElapsedSinceBootMillis());
                        if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                            WifiServiceImpl.this.mHwWifiCHRService.updateApkChangewWifiStatus(10, WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        }
                        WifiServiceImpl.this.mWifiController.updateWMUserAction(WifiServiceImpl.this.mContext, "ACTION_SELECT_WIFINETWORK", WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config != null && WifiServiceImpl.isValid(config)) {
                            if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                                WifiServiceImpl.this.mHwWifiCHRService.updateConnectType("FIRST_CONNECT");
                            }
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        } else if (config != null || networkId == -1) {
                            Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                            replyFailed(msg, 151554, 8);
                            return;
                        } else {
                            if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                                WifiServiceImpl.this.mHwWifiCHRService.updateConnectType("SELECT_CONNECT");
                            }
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        }
                    } else {
                        return;
                    }
                case 151556:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151557)) {
                        if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                            WifiServiceImpl.this.mHwWifiCHRService.reportHwCHRAccessNetworkEventInfoList(2);
                            WifiServiceImpl.this.mHwWifiCHRService.updateApkChangewWifiStatus(11, WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        }
                        WifiServiceImpl.this.mWifiController.updateWMUserAction(WifiServiceImpl.this.mContext, "ACTION_FORGET_WIFINETWORK", WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
                            WifiServiceImpl.this.handleForgetNetwork(Message.obtain(msg));
                            return;
                        } else {
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        }
                    } else {
                        return;
                    }
                case 151559:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151560)) {
                        WifiConfiguration config2 = (WifiConfiguration) msg.obj;
                        int networkId2 = msg.arg1;
                        Slog.d(WifiServiceImpl.TAG, "SAVE nid=" + Integer.toString(networkId2) + " config=" + config2 + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config2 != null) {
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        }
                        Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                        replyFailed(msg, 151560, 8);
                        return;
                    }
                    return;
                case 151562:
                    if (!checkChangePermissionAndReplyIfNotAuthorized(msg, 151564)) {
                        return;
                    }
                    if (WifiServiceImpl.mIsWpsDisabled) {
                        replyFailed(msg, 151564, 0);
                        return;
                    } else {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                case 151566:
                    if (!checkChangePermissionAndReplyIfNotAuthorized(msg, 151567)) {
                        return;
                    }
                    if (WifiServiceImpl.mIsWpsDisabled) {
                        replyFailed(msg, 151567, 0);
                        return;
                    } else {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                case 151569:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151570)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151572:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151574)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151575:
                    WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }

        private boolean checkChangePermissionAndReplyIfNotAuthorized(Message msg, int replyWhat) {
            if (WifiServiceImpl.this.mWifiPermissionsUtil.checkChangePermission(msg.sendingUid)) {
                return true;
            }
            Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring unauthorized msg=" + msg);
            replyFailed(msg, replyWhat, 9);
            return false;
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

    public final class LocalOnlyRequestorCallback implements LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback {
        public LocalOnlyRequestorCallback() {
        }

        public void onLocalOnlyHotspotRequestorDeath(LocalOnlyHotspotRequestInfo requestor) {
            WifiServiceImpl.this.unregisterCallingAppAndStopLocalOnlyHotspot(requestor);
        }
    }

    private final class SoftApCallbackImpl implements WifiManager.SoftApCallback {
        private SoftApCallbackImpl() {
        }

        public void onStateChanged(int state, int failureReason) {
            int unused = WifiServiceImpl.this.mSoftApState = state;
            Iterator<ISoftApCallback> iterator = WifiServiceImpl.this.mRegisteredSoftApCallbacks.values().iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().onStateChanged(state, failureReason);
                } catch (RemoteException e) {
                    Log.e(WifiServiceImpl.TAG, "onStateChanged: remote exception -- " + e);
                    iterator.remove();
                }
            }
        }

        public void onNumClientsChanged(int numClients) {
            int unused = WifiServiceImpl.this.mSoftApNumClients = numClients;
            Iterator<ISoftApCallback> iterator = WifiServiceImpl.this.mRegisteredSoftApCallbacks.values().iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().onNumClientsChanged(numClients);
                } catch (RemoteException e) {
                    Log.e(WifiServiceImpl.TAG, "onNumClientsChanged: remote exception -- " + e);
                    iterator.remove();
                }
            }
        }
    }

    class TdlsTask extends AsyncTask<TdlsTaskParams, Integer, Integer> {
        TdlsTask() {
        }

        /* access modifiers changed from: protected */
        public Integer doInBackground(TdlsTaskParams... params) {
            TdlsTaskParams param = params[0];
            String remoteIpAddress = param.remoteIpAddress.trim();
            boolean enable = param.enable;
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
                        if (remoteIpAddress.equals(ip)) {
                            macAddress = mac;
                            break;
                        }
                    }
                }
                if (macAddress == null) {
                    Slog.w(WifiServiceImpl.TAG, "Did not find remoteAddress {" + remoteIpAddress + "} in /proc/net/arp");
                } else {
                    WifiServiceImpl.this.enableTdlsWithMacAddress(macAddress, enable);
                }
                try {
                    reader2.close();
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e2) {
                Slog.e(WifiServiceImpl.TAG, "Could not open /proc/net/arp to lookup mac address");
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e3) {
                Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
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
            return 0;
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
                        AsyncChannel unused = WifiServiceImpl.this.mWifiStateMachineChannel = this.mWsmChannel;
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine connection failure, error=" + msg.arg1);
                    AsyncChannel unused2 = WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    return;
                case 69636:
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine channel lost, msg.arg1 =" + msg.arg1);
                    AsyncChannel unused3 = WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
                    return;
                case WifiStateMachine.CMD_CHANGE_TO_STA_P2P_CONNECT /*131573*/:
                    Slog.e(WifiServiceImpl.TAG, "handleMessage CMD_CHANGE_TO_STA_P2P_CONNECT");
                    WifiServiceImpl.this.showP2pToStaDialog();
                    return;
                case WifiStateMachine.CMD_CHANGE_TO_AP_P2P_CONNECT /*131574*/:
                    Slog.e(WifiServiceImpl.TAG, "handleMessage CMD_CHANGE_TO_STA_AP_CONNECT");
                    Bundle data = msg.getData();
                    boolean enabled = data.getBoolean("isWifiApEnabled");
                    WifiServiceImpl.this.showP2pToAPDialog((WifiConfiguration) data.getParcelable("wifiConfig"), enabled);
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "WifiStateMachineHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }
    }

    static {
        boolean z = true;
        if (!"07".equals(SystemProperties.get("ro.config.hw_opta", "0")) || !"840".equals(SystemProperties.get("ro.config.hw_optb", "0"))) {
            z = false;
        }
        IS_ATT = z;
    }

    public WifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        boolean z = false;
        this.scanRequestCounter = 0;
        this.mIsP2pCloseDialogExist = false;
        this.mVerboseLoggingEnabled = false;
        this.mLocalOnlyHotspotConfig = null;
        this.mSoftApState = 11;
        this.mSoftApNumClients = 0;
        this.mLastLogScanResultsTime = 0;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                        if (action.equals("android.intent.action.USER_PRESENT")) {
                            WifiServiceImpl.this.mWifiController.sendMessage(155660);
                        } else if (action.equals("android.intent.action.USER_REMOVED")) {
                            WifiServiceImpl.this.mWifiStateMachine.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
                        } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                            WifiServiceImpl.this.mWifiStateMachine.sendBluetoothAdapterStateChange(intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0));
                        } else if (action.equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                            WifiServiceImpl.this.mWifiController.sendMessage(155649, intent.getBooleanExtra("phoneinECMState", false), 0);
                        } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                            WifiServiceImpl.this.mWifiController.sendMessage(155662, intent.getBooleanExtra("phoneInEmergencyCall", false), 0);
                        } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                            WifiServiceImpl.this.handleIdleModeChanged();
                        } else if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                            WifiServiceImpl.this.handleLightIdleModeChanged();
                        } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                            int wifiApState = intent.getIntExtra("wifi_state", 14);
                            Slog.d(WifiServiceImpl.TAG, "wifiApState=" + wifiApState);
                            if (wifiApState == 14) {
                                WifiServiceImpl.this.stopSoftAp();
                            }
                        } else {
                            WifiServiceImpl.this.onReceiveEx(context, intent);
                        }
                    }
                }
            }
        };
        this.mPackageOrUserReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:12:0x0047  */
            /* JADX WARNING: Removed duplicated region for block: B:13:0x0063  */
            /* JADX WARNING: Removed duplicated region for block: B:14:0x0071  */
            public void onReceive(Context context, Intent intent) {
                boolean z;
                String action = intent.getAction();
                Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                String action2 = intent.getAction();
                int hashCode = action2.hashCode();
                if (hashCode != -2061058799) {
                    if (hashCode == 525384130 && action2.equals("android.intent.action.PACKAGE_REMOVED")) {
                        z = false;
                        switch (z) {
                            case false:
                                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                                    Uri uri = intent.getData();
                                    if (uid != -1 && uri != null) {
                                        WifiServiceImpl.this.mWifiStateMachine.removeAppConfigs(uri.getSchemeSpecificPart(), uid);
                                        break;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            case true:
                                WifiServiceImpl.this.mWifiStateMachine.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
                                break;
                            default:
                                Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action + " no handle");
                                break;
                        }
                    }
                } else if (action2.equals("android.intent.action.USER_REMOVED")) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                    }
                }
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                }
            }
        };
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mClock = wifiInjector.getClock();
        this.mFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mTrafficPoller = this.mWifiInjector.getWifiTrafficPoller();
        this.mUserManager = this.mWifiInjector.getUserManager();
        this.mCountryCode = this.mWifiInjector.getWifiCountryCode();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mWifiStateMachine = this.mWifiInjector.getWifiStateMachine();
        this.mWifiNative = this.mWifiInjector.getWifiNative();
        this.mWifiStateMachinePrime = this.mWifiInjector.getWifiStateMachinePrime();
        this.mWifiStateMachine.enableRssiPolling(true);
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mSettingsStore = this.mWifiInjector.getWifiSettingsStore();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mWifiLockManager = this.mWifiInjector.getWifiLockManager();
        this.mWifiMulticastLockManager = this.mWifiInjector.getWifiMulticastLockManager();
        HandlerThread wifiServiceHandlerThread = this.mWifiInjector.getWifiServiceHandlerThread();
        this.mClientHandler = new ClientHandler(TAG, wifiServiceHandlerThread.getLooper());
        this.mWifiStateMachineHandler = new WifiStateMachineHandler(TAG, wifiServiceHandlerThread.getLooper(), asyncChannel);
        this.mWifiController = this.mWifiInjector.getWifiController();
        this.mWifiBackupRestore = this.mWifiInjector.getWifiBackupRestore();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        this.mPermissionReviewRequired = (Build.PERMISSIONS_REVIEW_REQUIRED || context.getResources().getBoolean(17957000)) ? true : z;
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mLog = this.mWifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mIfaceIpModes = new ConcurrentHashMap<>();
        this.mLocalOnlyHotspotRequests = new HashMap<>();
        enableVerboseLoggingInternal(getVerboseLoggingLevel());
        this.mRegisteredSoftApCallbacks = new HashMap<>();
        this.mWifiInjector.getWifiStateMachinePrime().registerSoftApCallback(new SoftApCallbackImpl());
        this.mPowerProfile = this.mWifiInjector.getPowerProfile();
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt = new WifiServiceHisiExt(this.mContext);
            this.mHiSiWifiComm = new HiSiWifiComm(this.mContext);
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt = this.mWifiStateMachine.mWifiStateMachineHisiExt;
        }
        this.mWifiStateMachine.setmSettingsStore(this.mSettingsStore);
        this.uploader = DataUploader.getInstance();
        this.uploader.setContext(this.mContext);
    }

    @VisibleForTesting
    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mClientHandler.setWifiLog(log);
    }

    public void checkAndStartWifi() {
        if (this.mFrameworkFacade.inStorageManagerCryptKeeperBounce()) {
            Log.d(TAG, "Device still encrypted. Need to restart SystemServer.  Do not start wifi.");
            return;
        }
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        StringBuilder sb = new StringBuilder();
        sb.append("WifiService starting up with Wi-Fi ");
        sb.append(wifiEnabled ? "enabled" : "disabled");
        Slog.i(TAG, sb.toString());
        this.mWifiStateMachine.setWifiRepeaterStoped();
        registerForScanModeChange();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiServiceImpl.this.mdmForPolicyForceOpenWifi(false, false)) {
                    Slog.w(WifiServiceImpl.TAG, "mdm force open wifi, not allow airplane close wifi");
                    return;
                }
                if (WifiServiceImpl.this.mSettingsStore.handleAirplaneModeToggled()) {
                    if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
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
        } else if (!this.mWifiServiceHisiExt.mIsReceiverRegistered) {
            registerForBroadcasts();
            this.mWifiServiceHisiExt.mIsReceiverRegistered = true;
        }
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        this.mInLightIdleMode = this.mPowerManager.isLightDeviceIdleMode();
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
        if (wifiEnabled) {
            try {
                setWifiEnabled(this.mContext.getPackageName(), wifiEnabled);
            } catch (RemoteException e2) {
            }
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c9, code lost:
        if (r12 == null) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00cf, code lost:
        if (r12.length() == 0) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00d1, code lost:
        r11.mWifiPermissionsUtil.enforceCanAccessScanResults(r12, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00d7, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00da, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00dd, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e6, code lost:
        if (r11.mWifiStateMachine.disallowWifiScanRequest(r2) == false) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00e8, code lost:
        android.util.Slog.d(TAG, "wifi_scan reject because the interval isn't arrived");
        sendFailedScanDirectionalBroadcast(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f2, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f6, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00fb, code lost:
        if (startQuickttffScan(r12) == false) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fd, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0101, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0106, code lost:
        if (limitWifiScanRequest(r12) == false) goto L_0x0126;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0108, code lost:
        android.util.Log.d(TAG, "current scan request is refused " + r12);
        sendFailedScanDirectionalBroadcast(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0121, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0125, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x012a, code lost:
        if (limitWifiScanInAbsoluteRest(r12) == false) goto L_0x014a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x012c, code lost:
        android.util.Log.d(TAG, "absolute rest, scan request is refused " + r12);
        sendFailedScanDirectionalBroadcast(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0145, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0149, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x014e, code lost:
        if (restrictWifiScanRequest(r12) == false) goto L_0x016b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0150, code lost:
        android.util.Slog.i(TAG, "scan ctrl by PG, skip " + r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0166, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x016a, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x016d, code lost:
        if (r11.mHwWifiCHRService == null) goto L_0x0175;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x016f, code lost:
        r11.mHwWifiCHRService.updateApkChangewWifiStatus(4, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0175, code lost:
        r5 = new com.android.server.wifi.util.GeneralUtil.Mutable<>();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x018b, code lost:
        if (r11.mWifiInjector.getWifiStateMachineHandler().runWithScissors(new com.android.server.wifi.$$Lambda$WifiServiceImpl$71KWGZ9o3U1lf_2vP7tmY9cz4qQ(r11, r5, r0, r12), 4000) != false) goto L_0x019c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x018d, code lost:
        android.util.Log.e(TAG, "Failed to post runnable to start scan");
        sendFailedScanBroadcast();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0197, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x019b, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01a4, code lost:
        if (((java.lang.Boolean) r5.value).booleanValue() != false) goto L_0x01b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01a6, code lost:
        android.util.Log.e(TAG, "Failed to start scan");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01ad, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01b1, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01b2, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01b6, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:?, code lost:
        android.util.Log.w(TAG, "Exception:" + r5.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01d2, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01d6, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        android.util.Log.w(TAG, "SecurityException:" + r5.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01f2, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01f6, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01f7, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01fa, code lost:
        throw r1;
     */
    public boolean startScan(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_STARTSCAN);
        Slog.d(TAG, "startScan, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", appName:" + packageName);
        if (!this.mPowerManager.isScreenOn() && !"com.huawei.ca".equals(packageName) && !"com.huawei.parentcontrol".equals(packageName) && Binder.getCallingUid() != 1000 && !"com.huawei.hidisk".equals(packageName)) {
            Slog.i(TAG, "Screen is off, " + packageName + " startScan is skipped.");
            return false;
        } else if (enforceChangePermission(packageName) != 0) {
            return false;
        } else {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long ident = Binder.clearCallingIdentity();
            this.mLog.info("startScan uid=%").c((long) callingUid).flush();
            if (limitForegroundWifiScanRequest(packageName, callingUid)) {
                Log.d(TAG, "current foreground scan request is refused " + packageName);
                sendFailedScanDirectionalBroadcast(packageName);
                return false;
            }
            synchronized (this) {
                if (this.mInIdleMode) {
                    sendFailedScanBroadcast();
                    this.mScanPending = true;
                    return false;
                }
            }
        }
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

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        enforceConnectivityInternalPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCurrentNetworkWpsNfcConfigurationToken uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void handleIdleModeChanged() {
        boolean doScan = false;
        synchronized (this) {
            boolean idle = this.mPowerManager.isDeviceIdleMode();
            if (this.mInIdleMode != idle) {
                this.mInIdleMode = idle;
                if (!idle && this.mScanPending) {
                    this.mScanPending = false;
                    doScan = true;
                }
                handleLightIdleModeChanged();
            }
        }
        if (doScan) {
            startScan(this.mContext.getOpPackageName());
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x003f  */
    public void handleLightIdleModeChanged() {
        boolean combinedIdle;
        synchronized (this) {
            boolean lightIdle = this.mPowerManager.isLightDeviceIdleMode();
            boolean deepIdle = this.mPowerManager.isDeviceIdleMode();
            if (!lightIdle) {
                if (!deepIdle) {
                    combinedIdle = false;
                    Slog.d(TAG, "handleLightIdleModeChanged: lightIdle:" + lightIdle + ",deepIdle:" + deepIdle + ",combinedIdle:" + combinedIdle);
                    if (this.mInLightIdleMode != combinedIdle) {
                        this.mInLightIdleMode = combinedIdle;
                        setFilterEnable(combinedIdle);
                    }
                }
            }
            combinedIdle = true;
            Slog.d(TAG, "handleLightIdleModeChanged: lightIdle:" + lightIdle + ",deepIdle:" + deepIdle + ",combinedIdle:" + combinedIdle);
            if (this.mInLightIdleMode != combinedIdle) {
            }
        }
    }

    private boolean checkNetworkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0;
    }

    private void setFilterEnable(boolean enable) {
        Slog.d(TAG, "setFilterEnable:" + enable);
        this.mWifiNative.setFilterEnable(this.mWifiNative.getClientInterfaceName(), enable);
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

    private int enforceChangePermission(String callingPackage) {
        if (checkNetworkSettingsPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            return 0;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
        return this.mAppOps.noteOp("android:change_wifi_state", Binder.getCallingUid(), callingPackage);
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

    /* access modifiers changed from: protected */
    public String getAppName(int pID) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void setHisiWifiApEnabled(boolean enabled, WifiConfiguration wifiConfig, WifiController mWifiController2) {
        this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiApEnabled(enabled);
        if (wifiConfig == null || isValid(wifiConfig)) {
            mWifiController2.obtainMessage(155658, enabled, 0, wifiConfig).sendToTarget();
        } else {
            Slog.e(TAG, "Invalid WifiConfiguration");
        }
    }

    /* access modifiers changed from: private */
    public void showP2pToAPDialog(final WifiConfiguration wifiConfig, final boolean enabled) {
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
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean unused = WifiServiceImpl.this.mIsApDialogNeedShow = isChecked;
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(33685813)).setMessage(r.getString(33685814)).setView(checkBox).setNegativeButton(r.getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean unused = WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "NegativeButton is click");
                WifiServiceImpl.this.setWifiApStateByManual(false);
            }
        }).setPositiveButton(r.getString(17039370), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean unused = WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
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

    /* access modifiers changed from: private */
    public void showP2pToStaDialog() {
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
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean unused = WifiServiceImpl.this.mIsDialogNeedShow = isChecked;
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(33685811)).setMessage(r.getString(33685812)).setView(checkBox).setNegativeButton(r.getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean unused = WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "NegativeButton is click");
                WifiServiceImpl.this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.sendWifiStateDisabledBroadcast();
            }
        }).setPositiveButton(r.getString(17039370), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean unused = WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
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

    private boolean checkWifiPermissionWhenPermissionReviewRequired() {
        boolean z = false;
        if (!this.mPermissionReviewRequired) {
            return false;
        }
        if (this.mContext.checkCallingPermission("android.permission.MANAGE_WIFI_WHEN_PERMISSION_REVIEW_REQUIRED") == 0) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:147:0x0281, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0133, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x020a A[Catch:{ all -> 0x0282 }] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0228 A[SYNTHETIC, Splitter:B:119:0x0228] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00e4 A[Catch:{ all -> 0x0282 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fe A[Catch:{ all -> 0x0282 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0108 A[Catch:{ all -> 0x0282 }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x013b A[Catch:{ all -> 0x0282 }] */
    public synchronized boolean setWifiEnabled(String packageName, boolean enable) throws RemoteException {
        boolean apEnabled;
        long ident;
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_SETWIFIENABLED);
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (HwDeviceManager.disallowOp(0)) {
            Slog.i(TAG, "Wifi has been restricted by MDM apk.");
            return false;
        } else if (mdmForPolicyForceOpenWifi(true, enable)) {
            Slog.w(TAG, "mdm force open wifi, not allow close wifi");
            return false;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("setWifiEnabled: ");
            sb.append(enable);
            sb.append(" pid=");
            sb.append(Binder.getCallingPid());
            sb.append(", uid=");
            sb.append(Binder.getCallingUid());
            sb.append(", package=");
            sb.append(packageName);
            sb.append(", Stack=");
            sb.append(NO_KNOW_NAME.equals(packageName) ? Log.getStackTraceString(new Throwable()) : null);
            Slog.d(TAG, sb.toString());
            this.mLog.info("setWifiEnabled package=% uid=% enable=%").c(packageName).c((long) Binder.getCallingUid()).c(enable).flush();
            boolean isFromSettings = checkNetworkSettingsPermission(Binder.getCallingPid(), Binder.getCallingUid());
            if (!this.mSettingsStore.isAirplaneModeOn() || isFromSettings) {
                if (this.mSoftApState != 13) {
                    if (this.mSoftApState != 12) {
                        apEnabled = false;
                        if (apEnabled || isFromSettings) {
                            if (this.mHwWifiCHRService != null) {
                                if (enable) {
                                    this.mHwWifiCHRService.updateApkChangewWifiStatus(5, packageName);
                                    this.mWifiStateMachine.notifyApkChangeWifiStatus(true, packageName);
                                } else {
                                    this.mHwWifiCHRService.updateApkChangewWifiStatus(1, packageName);
                                    this.mWifiStateMachine.notifyApkChangeWifiStatus(false, packageName);
                                }
                            }
                            if (!enable) {
                                this.mWifiController.updateWMUserAction(this.mContext, "ACTION_ENABLE_WIFI_TRUE", packageName);
                            } else {
                                this.mWifiController.updateWMUserAction(this.mContext, "ACTION_ENABLE_WIFI_FALSE", packageName);
                            }
                            if (this.mContext != null || HwSystemManager.allowOp(this.mContext, 2097152, enable)) {
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
                                    } else if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && !enable) {
                                        if (!this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                                            Slog.d(TAG, "the software have no some important permissions.");
                                            return false;
                                        }
                                        this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                                    }
                                }
                                if (Binder.getCallingUid() != 0 || !"factory".equals(SystemProperties.get("ro.runmode", "normal")) || SystemProperties.getInt("wlan.wltest.status", 0) <= 0) {
                                    ident = Binder.clearCallingIdentity();
                                    if (this.mSettingsStore.handleWifiToggled(enable)) {
                                        Slog.d(TAG, "setWifiEnabled,Nothing to do if wifi cannot be toggled.");
                                        if (enable) {
                                            this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:cannot be toggled}");
                                        } else {
                                            this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:cannot be toggled}");
                                        }
                                        return true;
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                    if (this.mPermissionReviewRequired) {
                                        int wiFiEnabledState = getWifiEnabledState();
                                        if (enable) {
                                            if ((wiFiEnabledState == 0 || wiFiEnabledState == 1) && startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_ENABLE")) {
                                                return true;
                                            }
                                        } else if (wiFiEnabledState == 2 || wiFiEnabledState == 3) {
                                            if (startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_DISABLE")) {
                                                return true;
                                            }
                                        }
                                    }
                                    if (this.mHwWifiCHRService != null) {
                                        this.mHwWifiCHRService.updateWifiTriggerState(enable);
                                    }
                                    if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
                                        setWifiEnabledAfterVoWifiOff(enable);
                                    } else {
                                        this.mWifiController.sendMessage(155656);
                                    }
                                } else {
                                    Slog.e(TAG, "in wltest mode, dont allow to enable WiFi");
                                    return false;
                                }
                            } else if (enable) {
                                this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:permission deny}");
                            } else {
                                this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:permission deny}");
                            }
                        } else {
                            this.mLog.info("setWifiEnabled SoftAp not disabled: only Settings can enable wifi").flush();
                            return false;
                        }
                    }
                }
                apEnabled = true;
                if (apEnabled) {
                }
                if (this.mHwWifiCHRService != null) {
                }
                if (!enable) {
                }
                if (this.mContext != null) {
                }
                if (WifiServiceHisiExt.hisiWifiEnabled()) {
                }
                if (Binder.getCallingUid() != 0) {
                }
                ident = Binder.clearCallingIdentity();
                try {
                    if (this.mSettingsStore.handleWifiToggled(enable)) {
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                this.mLog.info("setWifiEnabled in Airplane mode: only Settings can enable wifi").flush();
                return false;
            }
        }
    }

    public void setWifiStateByManual(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            Slog.d(TAG, "setWifiStateByManual:" + enable + ",mIsReceiverRegistered:" + this.mWifiServiceHisiExt.mIsReceiverRegistered);
            if (enable) {
                if (!this.mWifiServiceHisiExt.mIsReceiverRegistered) {
                    registerForBroadcasts();
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
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETWIFIENABLESTATE);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getWifiEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mWifiStateMachine.syncGetWifiState();
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getWifiApEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        MutableInt apState = new MutableInt(11);
        this.mClientHandler.runWithScissors(new Runnable(apState) {
            private final /* synthetic */ MutableInt f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                this.f$1.value = WifiServiceImpl.this.mSoftApState;
            }
        }, 4000);
        return apState.value;
    }

    public void updateInterfaceIpState(String ifaceName, int mode) {
        enforceNetworkStackPermission();
        this.mLog.info("updateInterfaceIpState uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientHandler.post(new Runnable(ifaceName, mode) {
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                WifiServiceImpl.this.updateInterfaceIpStateInternal(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x009e, code lost:
        return;
     */
    public void updateInterfaceIpStateInternal(String ifaceName, int mode) {
        synchronized (this.mLocalOnlyHotspotRequests) {
            int previousMode = -1;
            if (ifaceName != null) {
                previousMode = this.mIfaceIpModes.put(ifaceName, Integer.valueOf(mode));
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
                    Slog.d(TAG, "IP mode config error - need to clean up");
                    if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                        Slog.d(TAG, "no LOHS requests, stop softap");
                        stopSoftAp();
                    } else {
                        Slog.d(TAG, "we have LOHS requests, clean them up");
                        sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                    }
                    updateInterfaceIpStateInternal(null, -1);
                    break;
                case 1:
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(3);
                    break;
                case 2:
                    if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                        sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked();
                        break;
                    } else {
                        stopSoftAp();
                        updateInterfaceIpStateInternal(null, -1);
                        return;
                    }
                default:
                    this.mLog.warn("updateInterfaceIpStateInternal: unknown mode %").c((long) mode).flush();
                    break;
            }
        }
    }

    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        boolean startSoftApInternal;
        enforceNetworkStackPermission();
        this.mLog.info("startSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
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
        if (wifiConfig == null || WifiApConfigStore.validateApWifiConfiguration(wifiConfig)) {
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
        int uid = Binder.getCallingUid();
        Slog.d(TAG, "stopSoftAp: pid=" + pid + ", uid=" + uid + ", name=" + getAppName(pid));
        this.mLog.info("stopSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLog.trace("Call to stop Tethering while LOHS is active, Registered LOHS callers will be updated when softap stopped.").flush();
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

    public void registerSoftApCallback(final IBinder binder, ISoftApCallback callback, final int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerSoftApCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        binder.unlinkToDeath(this, 0);
                        WifiServiceImpl.this.mClientHandler.post(new Runnable(callbackIdentifier) {
                            private final /* synthetic */ int f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                WifiServiceImpl.this.mRegisteredSoftApCallbacks.remove(Integer.valueOf(this.f$1));
                            }
                        });
                    }
                }, 0);
                this.mClientHandler.post(new Runnable(callbackIdentifier, callback) {
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ ISoftApCallback f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        WifiServiceImpl.lambda$registerSoftApCallback$3(WifiServiceImpl.this, this.f$1, this.f$2);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "Error on linkToDeath - " + e);
            }
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public static /* synthetic */ void lambda$registerSoftApCallback$3(WifiServiceImpl wifiServiceImpl, int callbackIdentifier, ISoftApCallback callback) {
        wifiServiceImpl.mRegisteredSoftApCallbacks.put(Integer.valueOf(callbackIdentifier), callback);
        if (wifiServiceImpl.mRegisteredSoftApCallbacks.size() > 20) {
            Log.wtf(TAG, "Too many soft AP callbacks: " + wifiServiceImpl.mRegisteredSoftApCallbacks.size());
        } else if (wifiServiceImpl.mRegisteredSoftApCallbacks.size() > 10) {
            Log.w(TAG, "Too many soft AP callbacks: " + wifiServiceImpl.mRegisteredSoftApCallbacks.size());
        }
        try {
            callback.onStateChanged(wifiServiceImpl.mSoftApState, 0);
            callback.onNumClientsChanged(wifiServiceImpl.mSoftApNumClients);
        } catch (RemoteException e) {
            Log.e(TAG, "registerSoftApCallback: remote exception -- " + e);
        }
    }

    public void unregisterSoftApCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterSoftApCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mClientHandler.post(new Runnable(callbackIdentifier) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.mRegisteredSoftApCallbacks.remove(Integer.valueOf(this.f$1));
            }
        });
    }

    /* access modifiers changed from: private */
    public void handleWifiApStateChange(int currentState, int previousState, int errorCode, String ifaceName, int mode) {
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
                if (this.mIfaceIpModes.contains(2)) {
                    sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked();
                } else {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                }
                updateInterfaceIpState(null, -1);
            }
        }
    }

    @GuardedBy("mLocalOnlyHotspotRequests")
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

    @GuardedBy("mLocalOnlyHotspotRequests")
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

    @GuardedBy("mLocalOnlyHotspotRequests")
    private void sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked() {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void registerLOHSForTest(int pid, LocalOnlyHotspotRequestInfo request) {
        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
    }

    public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (enforceChangePermission(packageName) != 0) {
            return 2;
        }
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
                this.mLog.info("startLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
                synchronized (this.mLocalOnlyHotspotRequests) {
                    if (this.mIfaceIpModes.contains(1)) {
                        this.mLog.info("Cannot start localOnlyHotspot when WiFi Tethering is active.").flush();
                        return 3;
                    } else if (this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid)) == null) {
                        LocalOnlyHotspotRequestInfo request = new LocalOnlyHotspotRequestInfo(binder, messenger, new LocalOnlyRequestorCallback());
                        if (this.mIfaceIpModes.contains(2)) {
                            try {
                                this.mLog.trace("LOHS already up, trigger onStarted callback").flush();
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
                    } else {
                        this.mLog.trace("caller already has an active request").flush();
                        throw new IllegalStateException("Caller already has an active LocalOnlyHotspot request");
                    }
                }
            } catch (RemoteException e2) {
                this.mLog.warn("RemoteException during isAppForeground when calling startLOHS").flush();
                return 3;
            }
        }
    }

    public void stopLocalOnlyHotspot() {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        Slog.d(TAG, "stopLocalOnlyHotspot: pid=" + pid + ", uid=" + uid + ", name=" + getAppName(pid));
        this.mLog.info("stopLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            LocalOnlyHotspotRequestInfo requestInfo = this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid));
            if (requestInfo != null) {
                requestInfo.unlinkDeathRecipient();
                unregisterCallingAppAndStopLocalOnlyHotspot(requestInfo);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0054, code lost:
        return;
     */
    public void unregisterCallingAppAndStopLocalOnlyHotspot(LocalOnlyHotspotRequestInfo request) {
        this.mLog.trace("unregisterCallingAppAndStopLocalOnlyHotspot pid=%").c((long) request.getPid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (this.mLocalOnlyHotspotRequests.remove(Integer.valueOf(request.getPid())) == null) {
                this.mLog.trace("LocalOnlyHotspotRequestInfo not found to remove").flush();
            } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLocalOnlyHotspotConfig = null;
                updateInterfaceIpStateInternal(null, -1);
                long identity = Binder.clearCallingIdentity();
                try {
                    stopSoftApInternal();
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        }
    }

    public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) {
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
            this.mLog.info("getWifiApConfiguration uid=%").c((long) uid).flush();
            Log.d(TAG, "getWifiApConfiguration, uid=" + uid);
            return this.mWifiApConfigStore.getApConfiguration();
        }
        throw new SecurityException("App not allowed to read or update stored WiFi Ap config (uid = " + uid + ")");
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkConfigOverridePermission(uid)) {
            this.mLog.info("setWifiApConfiguration uid=%").c((long) uid).flush();
            if (wifiConfig == null) {
                return false;
            }
            if (WifiApConfigStore.validateApWifiConfiguration(wifiConfig)) {
                Log.d(TAG, "apBand set to " + wifiConfig.apBand);
                return this.mWifiStateMachine.hwSetApConfiguration(wifiConfig, packageName);
            }
            Slog.e(TAG, "Invalid WifiConfiguration");
            return false;
        }
        throw new SecurityException("App not allowed to read or update stored WiFi AP config (uid = " + uid + ")");
    }

    public boolean isScanAlwaysAvailable() {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_ISSACNALWAYSAVAILABLE);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isScanAlwaysAvailable uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public void disconnect(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_DISCONNECT);
        Slog.d(TAG, "disconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("disconnect uid=%").c((long) Binder.getCallingUid()).flush();
            this.mWifiStateMachine.disconnectCommand();
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateApkChangewWifiStatus(8, getAppName(Binder.getCallingPid()));
            }
        }
    }

    public void reconnect(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_RECONNECT);
        Slog.d(TAG, "reconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("reconnect uid=%").c((long) Binder.getCallingUid()).flush();
            this.mWifiStateMachine.reconnectCommand(new WorkSource(Binder.getCallingUid()));
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateApkChangewWifiStatus(6, getAppName(Binder.getCallingPid()));
            }
        }
    }

    public void reassociate(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_REASOCIATE);
        Slog.d(TAG, "reassociate:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("reassociate uid=%").c((long) Binder.getCallingUid()).flush();
            this.mWifiStateMachine.reassociateCommand();
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateApkChangewWifiStatus(7, getAppName(Binder.getCallingPid()));
            }
        }
    }

    public int getSupportedFeatures() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getSupportedFeatures uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetSupportedFeatures(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return 0;
    }

    public void requestActivityInfo(ResultReceiver result) {
        Bundle bundle = new Bundle();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("requestActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    public WifiActivityEnergyInfo reportActivityInfo() {
        long[] txTimePerLevel;
        long[] txTimePerLevel2;
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("reportActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo energyInfo = null;
        if (this.mWifiStateMachineChannel != null) {
            WifiLinkLayerStats stats = this.mWifiStateMachine.syncGetLinkLayerStats(this.mWifiStateMachineChannel);
            if (stats != null) {
                double rxIdleCurrent = this.mPowerProfile.getAveragePower("wifi.controller.idle");
                double rxCurrent = this.mPowerProfile.getAveragePower("wifi.controller.rx");
                double txCurrent = this.mPowerProfile.getAveragePower("wifi.controller.tx");
                double voltage = this.mPowerProfile.getAveragePower("wifi.controller.voltage") / 1000.0d;
                long rxIdleTime = (long) ((stats.on_time - stats.tx_time) - stats.rx_time);
                int i = 0;
                if (stats.tx_time_per_level != null) {
                    txTimePerLevel = new long[stats.tx_time_per_level.length];
                    while (i < txTimePerLevel.length) {
                        txTimePerLevel[i] = (long) stats.tx_time_per_level[i];
                        i++;
                        energyInfo = energyInfo;
                    }
                } else {
                    txTimePerLevel = new long[0];
                }
                long[] txTimePerLevel3 = txTimePerLevel;
                long energyUsed = (long) (((((double) stats.tx_time) * txCurrent) + (((double) stats.rx_time) * rxCurrent) + (((double) rxIdleTime) * rxIdleCurrent)) * voltage);
                if (rxIdleTime < 0 || stats.on_time < 0 || stats.tx_time < 0 || stats.rx_time < 0 || stats.on_time_scan < 0 || energyUsed < 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" rxIdleCur=" + rxIdleCurrent);
                    sb.append(" rxCur=" + rxCurrent);
                    sb.append(" txCur=" + txCurrent);
                    sb.append(" voltage=" + voltage);
                    sb.append(" on_time=" + stats.on_time);
                    sb.append(" tx_time=" + stats.tx_time);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(" tx_time_per_level=");
                    double d = rxIdleCurrent;
                    txTimePerLevel2 = txTimePerLevel3;
                    sb2.append(Arrays.toString(txTimePerLevel2));
                    sb.append(sb2.toString());
                    sb.append(" rx_time=" + stats.rx_time);
                    sb.append(" rxIdleTime=" + rxIdleTime);
                    sb.append(" scan_time=" + stats.on_time_scan);
                    sb.append(" energy=" + energyUsed);
                    Log.d(TAG, " reportActivityInfo: " + sb.toString());
                } else {
                    double d2 = rxIdleCurrent;
                    txTimePerLevel2 = txTimePerLevel3;
                }
                double d3 = rxCurrent;
                double d4 = txCurrent;
                WifiLinkLayerStats wifiLinkLayerStats = stats;
                WifiActivityEnergyInfo wifiActivityEnergyInfo = new WifiActivityEnergyInfo(this.mClock.getElapsedSinceBootMillis(), 3, (long) stats.tx_time, txTimePerLevel2, (long) stats.rx_time, (long) stats.on_time_scan, rxIdleTime, energyUsed);
                energyInfo = wifiActivityEnergyInfo;
            } else {
                WifiLinkLayerStats wifiLinkLayerStats2 = stats;
            }
            if (energyInfo == null || !energyInfo.isValid()) {
                return null;
            }
            return energyInfo;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getConfiguredNetworks() {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETCONFIGUREDNETWORKS);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getConfiguredNetworks uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> configs = this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
            if (configs != null) {
                return new ParceledListSlice<>(configs);
            }
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        }
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getPrivilegedConfiguredNetworks uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> configs = this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel);
            if (configs != null) {
                return new ParceledListSlice<>(configs);
            }
        } else {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        }
        return null;
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getMatchingWifiConfig uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncGetMatchingWifiConfig(scanResult, this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public List<WifiConfiguration> getAllMatchingWifiConfigs(ScanResult scanResult) {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getMatchingPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.getAllMatchingWifiConfigs(scanResult, this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public List<OsuProvider> getMatchingOsuProviders(ScanResult scanResult) {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getMatchingOsuProviders uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncGetMatchingOsuProviders(scanResult, this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int addOrUpdateNetwork(WifiConfiguration config, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_ADDORUPDATENETWORK);
        Slog.d(TAG, "addOrUpdateNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", config:" + config + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return -1;
        }
        this.mLog.info("addOrUpdateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (config.isPasspoint()) {
            PasspointConfiguration passpointConfig = PasspointProvider.convertFromWifiConfig(config);
            if (passpointConfig.getCredential() == null) {
                Slog.e(TAG, "Missing credential for Passpoint profile");
                return -1;
            }
            passpointConfig.getCredential().setCaCertificate(config.enterpriseConfig.getCaCertificate());
            passpointConfig.getCredential().setClientCertificateChain(config.enterpriseConfig.getClientCertificateChain());
            passpointConfig.getCredential().setClientPrivateKey(config.enterpriseConfig.getClientPrivateKey());
            if (addOrUpdatePasspointConfiguration(passpointConfig, packageName)) {
                return 0;
            }
            Slog.e(TAG, "Failed to add Passpoint profile");
            return -1;
        } else if (config != null) {
            Slog.i("addOrUpdateNetwork", " uid = " + Integer.toString(Binder.getCallingUid()) + " SSID " + config.SSID + " nid=" + Integer.toString(config.networkId));
            if (config.networkId == -1) {
                config.creatorUid = Binder.getCallingUid();
            } else {
                config.lastUpdateUid = Binder.getCallingUid();
            }
            this.mWifiController.updateWMUserAction(this.mContext, "ACTION_SELECT_WIFINETWORK", getAppName(Binder.getCallingPid()));
            if (this.mWifiStateMachineChannel != null) {
                return this.mWifiStateMachine.hwSyncAddOrUpdateNetwork(this.mWifiStateMachineChannel, config, packageName);
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

    public boolean removeNetwork(int netId, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_REMOVENETWORK);
        Slog.d(TAG, "removeNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mLog.info("removeNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiController.updateWMUserAction(this.mContext, "ACTION_REMOVE_WIFINETWORK", getAppName(Binder.getCallingPid()));
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.hwSyncRemoveNetwork(this.mWifiStateMachineChannel, netId, packageName);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_ENABLENETWORK);
        Slog.d(TAG, "enableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", disableOthers:" + disableOthers + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mLog.info("enableNetwork uid=% disableOthers=%").c((long) Binder.getCallingUid()).c(disableOthers).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_DISABLENETWORK);
        String appName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "disableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + appName);
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(2, appName);
        }
        this.mLog.info("disableNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDisableNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo(String callingPackage) {
        WifiInfo result;
        boolean hideDefaultMacAddress;
        boolean hideBssidAndSsid;
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETCONNECTIONINFO);
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getConnectionInfo uid=%").c((long) uid).flush();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            result = this.mWifiStateMachine.syncRequestConnectionInfo(callingPackage, uid);
            hideDefaultMacAddress = true;
            hideBssidAndSsid = true;
            if (this.mWifiInjector.getWifiPermissionsWrapper().getLocalMacAddressPermission(uid) == 0) {
                hideDefaultMacAddress = false;
            }
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(callingPackage, uid);
            hideBssidAndSsid = false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error checking receiver permission", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "enforceCanAccessScanResults: hiding ssid and bssid" + e2.getMessage());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        if (hideDefaultMacAddress) {
            result.setMacAddress("02:00:00:00:00:00");
        }
        if (hideBssidAndSsid) {
            result.setBSSID("02:00:00:00:00:00");
            result.setSSID(WifiSsid.createFromHex(null));
        }
        if (this.mVerboseLoggingEnabled && (hideBssidAndSsid || hideDefaultMacAddress)) {
            WifiLog wifiLog = this.mLog;
            wifiLog.v("getConnectionInfo: hideBssidAndSSid=" + hideBssidAndSsid + ", hideDefaultMacAddress=" + hideDefaultMacAddress);
        }
        Binder.restoreCallingIdentity(ident);
        return result;
    }

    private ScanResult[] filterScanProxyResultsByAge(List<ScanResult> scanResultsList, List<ScanResult> CachedScanResultsList) {
        ScanResult[] filterScanProxyScanResults = (ScanResult[]) scanResultsList.stream().filter(new Predicate(this.mClock.getElapsedSinceBootMillis()) {
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return WifiServiceImpl.lambda$filterScanProxyResultsByAge$5(this.f$0, (ScanResult) obj);
            }
        }).toArray($$Lambda$WifiServiceImpl$BwofqEhGlhLBNFHuJX2d0TlzyA.INSTANCE);
        if (filterScanProxyScanResults.length == 0) {
            return (ScanResult[]) CachedScanResultsList.toArray(new ScanResult[CachedScanResultsList.size()]);
        }
        return filterScanProxyScanResults;
    }

    static /* synthetic */ boolean lambda$filterScanProxyResultsByAge$5(long currentTimeInMillis, ScanResult ScanResult) {
        return currentTimeInMillis - (ScanResult.timestamp / 1000) < 30000;
    }

    static /* synthetic */ ScanResult[] lambda$filterScanProxyResultsByAge$6(int x$0) {
        return new ScanResult[x$0];
    }

    public List<ScanResult> getScanResults(String callingPackage) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETSCANRESULTS);
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getScanResults uid=%").c((long) uid).flush();
        }
        try {
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(callingPackage, uid);
            List<ScanResult> scanResults = new ArrayList<>();
            if (!this.mWifiInjector.getWifiStateMachineHandler().runWithScissors(new Runnable(scanResults) {
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    this.f$1.addAll(WifiServiceImpl.this.mScanRequestProxy.getScanResults());
                }
            }, 4000)) {
                Log.e(TAG, "Failed to post runnable to fetch scan results");
            }
            List<ScanResult> scanResultsList = scanResults;
            if (1000 == uid) {
                scanResultsList = Arrays.asList(filterScanProxyResultsByAge(scanResults, this.mWifiInjector.getWifiScanner().getSingleScanResults()));
            }
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
            return scanResultsList;
        } catch (SecurityException e) {
            return new ArrayList();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void logScanResultsListRestrictively(String callingPackage, List<ScanResult> scanResultsList) {
        long currentLogTime = this.mClock.getElapsedSinceBootMillis();
        if ("com.android.settings".equals(callingPackage) && scanResultsList != null && currentLogTime - this.mLastLogScanResultsTime > 3000) {
            Set<String> ssids = new HashSet<>();
            StringBuilder sb = new StringBuilder();
            for (ScanResult scanResult : scanResultsList) {
                String ssid = scanResult.SSID;
                if (!ssids.contains(ssid)) {
                    ssids.add(ssid);
                    sb.append(ssid);
                    sb.append("|");
                    sb.append(scanResult.isHiLinkNetwork);
                    sb.append("|");
                    sb.append(scanResult.dot11vNetwork);
                    sb.append(" ");
                }
            }
            Log.d(TAG, "getScanResults: calling by " + callingPackage + "   includes: " + sb.toString());
            this.mLastLogScanResultsTime = currentLogTime;
        }
    }

    public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mLog.info("addorUpdatePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.hwSyncAddOrUpdatePasspointConfig(this.mWifiStateMachineChannel, config, Binder.getCallingUid(), packageName);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public boolean removePasspointConfiguration(String fqdn, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mLog.info("removePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.hwSyncRemovePasspointConfig(this.mWifiStateMachineChannel, fqdn, packageName);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public List<PasspointConfiguration> getPasspointConfigurations() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncGetPasspointConfigs(this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        enforceAccessPermission();
        this.mLog.info("queryPasspointIcon uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            this.mWifiStateMachine.syncQueryPasspointIcon(this.mWifiStateMachineChannel, bssid, fileName);
            return;
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        this.mLog.info("matchProviderWithCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.matchProviderWithCurrentNetwork(this.mWifiStateMachineChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mLog.info("deauthenticateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.deauthenticateNetwork(this.mWifiStateMachineChannel, holdoff, ess);
    }

    public void setCountryCode(String countryCode) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode);
        enforceConnectivityInternalPermission();
        this.mLog.info("setCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        long token = Binder.clearCallingIdentity();
        this.mCountryCode.setCountryCode(countryCode);
        Binder.restoreCallingIdentity(token);
    }

    public String getCountryCode() {
        enforceConnectivityInternalPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mCountryCode.getCountryCode();
    }

    public boolean isDualBandSupported() {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isDualBandSupported uid=%").c((long) Binder.getCallingUid()).flush();
        }
        int value = Settings.Global.getInt(this.mContext.getContentResolver(), "hw_wifi_ap_band", 0);
        if (value == 1) {
            return false;
        }
        if (value == 2) {
            return true;
        }
        return this.mContext.getResources().getBoolean(17957074);
    }

    public boolean needs5GHzToAnyApBandConversion() {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("needs5GHzToAnyApBandConversion uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mContext.getResources().getBoolean(17957073);
    }

    @Deprecated
    public DhcpInfo getDhcpInfo() {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETPHCPINFO);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getDhcpInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
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
        Iterator it = dhcpResults.dnsServers.iterator();
        while (it.hasNext()) {
            InetAddress dns = (InetAddress) it.next();
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
        if (remoteAddress != null) {
            this.mLog.info("enableTdls uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
            TdlsTaskParams params = new TdlsTaskParams();
            params.remoteIpAddress = remoteAddress;
            params.enable = enable;
            new TdlsTask().execute(new TdlsTaskParams[]{params});
            return;
        }
        throw new IllegalArgumentException("remoteAddress cannot be null");
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        this.mLog.info("enableTdlsWithMacAddress uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        if (remoteMacAddress != null) {
            this.mWifiStateMachine.enableTdls(remoteMacAddress, enable);
            return;
        }
        throw new IllegalArgumentException("remoteMacAddress cannot be null");
    }

    public Messenger getWifiServiceMessenger(String packageName) throws RemoteException {
        enforceAccessPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("getWifiServiceMessenger uid=%").c((long) Binder.getCallingUid()).flush();
            return new Messenger(this.mClientHandler);
        }
        throw new SecurityException("Could not create wifi service messenger");
    }

    public void disableEphemeralNetwork(String SSID, String packageName) {
        enforceAccessPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("disableEphemeralNetwork uid=%").c((long) Binder.getCallingUid()).flush();
            this.mWifiStateMachine.disableEphemeralNetwork(SSID);
        }
    }

    private boolean startConsentUi(String packageName, int callingUid, String intentAction) throws RemoteException {
        if (UserHandle.getAppId(callingUid) == 1000 || checkWifiPermissionWhenPermissionReviewRequired()) {
            return false;
        }
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 268435456, UserHandle.getUserId(callingUid)).uid == callingUid) {
                Intent intent = new Intent(intentAction);
                intent.addFlags(276824064);
                intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
                this.mContext.startActivity(intent);
                return true;
            }
            throw new SecurityException("Package " + packageName + " not in uid " + callingUid);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private void registerForScanModeChange() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.mSettingsStore.handleWifiScanAlwaysAvailableToggled();
                WifiServiceImpl.this.mWifiController.sendMessage(155655);
            }
        });
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        if (this.mContext.getResources().getBoolean(17957075)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        }
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        intentFilter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        if (this.mContext.getResources().getBoolean(17957088)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
        }
        registerForBroadcastsEx(intentFilter);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_FULLY_REMOVED");
        intentFilter2.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.PACKAGE_FULLY_REMOVED")) {
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    Uri uri = intent.getData();
                    if (uid != -1 && uri != null) {
                        String pkgName = uri.getSchemeSpecificPart();
                        WifiServiceImpl.this.mWifiStateMachine.removeAppConfigs(pkgName, uid);
                        WifiServiceImpl.this.mWifiStateMachineHandler.post(new Runnable(pkgName, uid) {
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                WifiServiceImpl.this.mScanRequestProxy.clearScanRequestTimestampsForApp(this.f$1, this.f$2);
                            }
                        });
                    }
                }
            }
        }, intentFilter2);
        this.mContext.registerReceiverAsUser(this.mPackageOrUserReceiver, UserHandle.ALL, intentFilter2, null, null);
    }

    /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new WifiShellCommand(this.mWifiStateMachine).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        if (args != null && args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args != null && args.length > 0 && "ipclient".equals(args[0])) {
            String[] ipClientArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipClientArgs, 0, ipClientArgs.length);
            this.mWifiStateMachine.dumpIpClient(fd, pw, ipClientArgs);
        } else if (args == null || args.length <= 0 || !WifiScoreReport.DUMP_ARG.equals(args[0])) {
            pw.println("Wi-Fi is " + this.mWifiStateMachine.syncGetWifiStateByName());
            StringBuilder sb = new StringBuilder();
            sb.append("Verbose logging is ");
            sb.append(this.mVerboseLoggingEnabled ? "on" : "off");
            pw.println(sb.toString());
            pw.println("Stay-awake conditions: " + this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", 0));
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mTrafficPoller.dump(fd, pw, args);
            pw.println();
            pw.println("Locks held:");
            this.mWifiLockManager.dump(pw);
            pw.println();
            this.mWifiMulticastLockManager.dump(pw);
            pw.println();
            this.mWifiStateMachinePrime.dump(fd, pw, args);
            pw.println();
            this.mWifiStateMachine.dump(fd, pw, args);
            pw.println();
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
            pw.println();
            this.mWifiBackupRestore.dump(fd, pw, args);
            pw.println();
            pw.println("ScoringParams: settings put global wifi_score_params " + this.mWifiInjector.getScoringParams());
            pw.println();
            WifiScoreReport wifiScoreReport = this.mWifiStateMachine.getWifiScoreReport();
            if (wifiScoreReport != null) {
                pw.println("WifiScoreReport:");
                wifiScoreReport.dump(fd, pw, args);
            }
            pw.println();
        } else {
            WifiScoreReport wifiScoreReport2 = this.mWifiStateMachine.getWifiScoreReport();
            if (wifiScoreReport2 != null) {
                wifiScoreReport2.dump(fd, pw, args);
            }
        }
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        this.mLog.info("acquireWifiLock uid=% lockMode=%").c((long) Binder.getCallingUid()).c((long) lockMode).flush();
        Slog.d(TAG, "acquireWifiLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", binder:" + binder + ", lockMode:" + lockMode + ", tag:" + tag + ", ws:" + ws);
        if (this.mWifiLockManager.acquireWifiLock(lockMode, tag, binder, ws)) {
            return true;
        }
        return false;
    }

    public void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        this.mLog.info("updateWifiLockWorkSource uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiLockManager.updateWifiLockWorkSource(binder, ws);
    }

    public boolean releaseWifiLock(IBinder binder) {
        this.mLog.info("releaseWifiLock uid=%").c((long) Binder.getCallingUid()).flush();
        Slog.d(TAG, "releaseWifiLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", binder:" + binder);
        if (this.mWifiLockManager.releaseWifiLock(binder)) {
            return true;
        }
        return false;
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();
        this.mLog.info("initializeMulticastFiltering uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.initializeFiltering();
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();
        this.mLog.info("acquireMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.acquireLock(binder, tag);
    }

    public void releaseMulticastLock() {
        enforceMulticastChangePermission();
        this.mLog.info("releaseMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.releaseLock();
    }

    public boolean isMulticastEnabled() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isMulticastEnabled uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mWifiMulticastLockManager.isMulticastEnabled();
    }

    public void enableVerboseLogging(int verbose) {
        enforceAccessPermission();
        enforceNetworkSettingsPermission();
        this.mLog.info("enableVerboseLogging uid=% verbose=%").c((long) Binder.getCallingUid()).c((long) verbose).flush();
        this.mFacade.setIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", verbose);
        enableVerboseLoggingInternal(verbose);
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLoggingInternal(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
        this.mWifiStateMachine.enableVerboseLogging(verbose);
        this.mWifiLockManager.enableVerboseLogging(verbose);
        this.mWifiMulticastLockManager.enableVerboseLogging(verbose);
        this.mWifiInjector.enableVerboseLogging(verbose);
    }

    public int getVerboseLoggingLevel() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getVerboseLoggingLevel uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0);
    }

    public void factoryReset(String packageName) {
        enforceConnectivityInternalPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("factoryReset uid=%").c((long) Binder.getCallingUid()).flush();
            if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
                if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                    stopSoftApInternal();
                    if (this.mContext != null && (IS_ATT || IS_VERIZON)) {
                        Settings.System.putInt(this.mContext.getContentResolver(), SET_SSID_NAME, 1);
                    }
                }
                if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                    int i = 0;
                    while (true) {
                        if (i >= 10) {
                            break;
                        } else if (this.mWifiStateMachineChannel != null) {
                            List<WifiConfiguration> networks = this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
                            if (networks != null) {
                                for (WifiConfiguration config : networks) {
                                    removeNetwork(config.networkId, packageName);
                                }
                            } else {
                                i++;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
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
        return validity == null || logAndReturnFalse(validity);
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
            if (!config.allowedKeyManagement.get(3) && !config.allowedKeyManagement.get(1)) {
                return "not PSK or 8021X";
            }
        }
        if (config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
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
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mWifiStateMachine.getCurrentNetwork();
    }

    public static String toHexString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\'');
        sb.append(s);
        sb.append('\'');
        for (int n = 0; n < s.length(); n++) {
            sb.append(String.format(" %02x", new Object[]{Integer.valueOf(s.charAt(n) & 65535)}));
        }
        return sb.toString();
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mLog.info("enableWifiConnectivityManager uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        this.mWifiStateMachine.enableWifiConnectivityManager(enabled);
    }

    public byte[] retrieveBackupData() {
        enforceNetworkSettingsPermission();
        this.mLog.info("retrieveBackupData uid=%").c((long) Binder.getCallingUid()).flush();
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
        this.mLog.info("restoreBackupData uid=%").c((long) Binder.getCallingUid()).flush();
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

    public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                int uid = Binder.getCallingUid();
                this.mLog.trace("startSubscriptionProvisioning uid=%").c((long) uid).flush();
                if (this.mWifiStateMachine.syncStartSubscriptionProvisioning(uid, provider, callback, this.mWifiStateMachineChannel)) {
                    this.mLog.trace("Subscription provisioning started with %").c(provider.toString()).flush();
                    return;
                }
                return;
            }
            throw new UnsupportedOperationException("Passpoint not enabled");
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    /* access modifiers changed from: protected */
    public void handleAirplaneModeToggled() {
        this.mWifiController.sendMessage(155657);
    }

    /* access modifiers changed from: protected */
    public void onReceiveEx(Context context, Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void registerForBroadcastsEx(IntentFilter intentFilter) {
    }

    /* access modifiers changed from: protected */
    public void setWifiEnabledAfterVoWifiOff(boolean enable) {
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleForgetNetwork(Message msg) {
    }

    /* access modifiers changed from: protected */
    public boolean mdmForPolicyForceOpenWifi(boolean showToast, boolean enable) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean startQuickttffScan(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean restrictWifiScanRequest(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean limitForegroundWifiScanRequest(String packageName, int uid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean limitWifiScanRequest(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean limitWifiScanInAbsoluteRest(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public String getPackageName(int pID) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID && appProcess.pkgList != null && appProcess.pkgList.length > 0) {
                return appProcess.pkgList[0];
            }
        }
        return null;
    }

    private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        if (this.mHwBehaviorManager == null) {
            synchronized (mWifiLock) {
                if (this.mHwBehaviorManager == null) {
                    this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
                }
            }
        }
        if (this.mHwBehaviorManager != null) {
            try {
                this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
            } catch (Exception e) {
                Log.w(TAG, "sendBehavior:" + e);
            }
        } else {
            Log.w(TAG, "HwBehaviorCollectManager is null");
        }
    }
}
