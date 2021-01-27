package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.HotspotConfig;
import android.net.wifi.IDppCallback;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.IOnWifiUsabilityStatsListener;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.ITrafficStateCallback;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiDeviceConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkedInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.WifiScanInfo;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.AsyncTask;
import android.os.Binder;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.MutableInt;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.PowerProfile;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.LocalOnlyHotspotRequestInfo;
import com.android.server.wifi.WifiServiceImpl;
import com.android.server.wifi.hotspot2.PasspointProvider;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.ExternalCallbackTracker;
import com.android.server.wifi.util.GeneralUtil;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WifiServiceImpl extends BaseWifiService {
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static final int BOTH_2G_AND_5G = 2;
    private static final String[] CLEAR_WIFI_SAVED_STATE_PACKAGE_NAME_LIST = {WifiCommonUtils.PACKAGE_NAME_ASSOCIATE_SETTINGS, WifiConfigManager.SYSUI_PACKAGE_NAME};
    private static final boolean DBG = true;
    private static final long DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final int DEFAULT_VALUE = 0;
    private static final String KEY_IS_DOZE_ENABLE = "isDozeEnable";
    private static final int MAX_SLEEP_RETRY_TIMES = 10;
    private static final String NO_KNOW_NAME = "android";
    private static final int ONLY_2G = 1;
    private static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    private static final String TAG = "WifiService";
    private static final boolean VDBG = false;
    private static final int WAIT_SLEEP_TIME = 100;
    private static final boolean mIsWpsDisabled = SystemProperties.getBoolean("ro.config.hw_wifi_wps_disable", true);
    private DataProvider dataProvider;
    final ActiveModeWarden mActiveModeWarden;
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    private AsyncChannelExternalClientHandler mAsyncChannelExternalClientHandler;
    final ClientModeImpl mClientModeImpl;
    @VisibleForTesting
    AsyncChannel mClientModeImplChannel;
    ClientModeImplHandler mClientModeImplHandler;
    private final Clock mClock;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final DppManager mDppManager;
    private final FrameworkFacade mFacade;
    private final FrameworkFacade mFrameworkFacade;
    private HiCoexManager mHiCoexManager = null;
    private HwWifiCHRService mHwWifiCHRService;
    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private final ConcurrentHashMap<String, Integer> mIfaceIpModes;
    boolean mInIdleMode;
    boolean mInLightIdleMode;
    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private WifiConfiguration mLocalOnlyHotspotConfig = null;
    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private final HashMap<Integer, LocalOnlyHotspotRequestInfo> mLocalOnlyHotspotRequests;
    private WifiLog mLog;
    private final PowerManager mPowerManager;
    PowerProfile mPowerProfile;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                if (action.equals("android.intent.action.USER_PRESENT")) {
                    WifiServiceImpl.this.mWifiController.sendMessage(155660);
                } else if (action.equals("android.intent.action.USER_REMOVED")) {
                    WifiServiceImpl.this.mClientModeImpl.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                    WifiServiceImpl.this.mClientModeImpl.sendBluetoothAdapterStateChange(intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0));
                } else if (action.equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                    WifiServiceImpl.this.mWifiController.sendMessage(155649, intent.getBooleanExtra("phoneinECMState", false) ? 1 : 0, 0);
                } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                    WifiServiceImpl.this.mWifiController.sendMessage(155662, intent.getBooleanExtra("phoneInEmergencyCall", false) ? 1 : 0, 0);
                } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                    WifiServiceImpl.this.handleIdleModeChanged();
                } else if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                    WifiServiceImpl.this.handleLightIdleModeChanged();
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    int wifiApState = intent.getIntExtra("wifi_state", 14);
                    Slog.i(WifiServiceImpl.TAG, "wifiApState=" + wifiApState);
                    if (wifiApState == 14) {
                        WifiServiceImpl.this.stopSoftAp();
                    }
                } else {
                    WifiServiceImpl.this.onReceiveEx(context, intent);
                }
            }
        }
    };
    private final ExternalCallbackTracker<ISoftApCallback> mRegisteredSoftApCallbacks;
    boolean mScanPending;
    final ScanRequestProxy mScanRequestProxy;
    final WifiSettingsStore mSettingsStore;
    private int mSoftApNumClients = 0;
    private int mSoftApState = 11;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled = false;
    private WifiApConfigStore mWifiApConfigStore;
    private int mWifiApMode = -1;
    private int mWifiApState = 11;
    private final WifiBackupRestore mWifiBackupRestore;
    private WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private final WifiLockManager mWifiLockManager;
    private final WifiMetrics mWifiMetrics;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private WifiNative mWifiNative;
    private final WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiTrafficPoller mWifiTrafficPoller;
    private int scanRequestCounter = 0;

    public final class LocalOnlyRequestorCallback implements LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback {
        public LocalOnlyRequestorCallback() {
        }

        @Override // com.android.server.wifi.LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback
        public void onLocalOnlyHotspotRequestorDeath(LocalOnlyHotspotRequestInfo requestor) {
            WifiServiceImpl.this.unregisterCallingAppAndStopLocalOnlyHotspot(requestor);
        }
    }

    private class AsyncChannelExternalClientHandler extends WifiHandler {
        AsyncChannelExternalClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        @Override // com.android.server.wifi.util.WifiHandler, android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 69633:
                    WifiServiceImpl.this.mFrameworkFacade.makeWifiAsyncChannel(WifiServiceImpl.TAG).connect(WifiServiceImpl.this.mContext, this, msg.replyTo);
                    return;
                case 151553:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151554) || isAllowWifiConnect(msg.sendingUid)) {
                        WifiConfiguration config = null;
                        String callingPackage = "";
                        if (msg.obj instanceof WifiConfiguration) {
                            config = (WifiConfiguration) msg.obj;
                            callingPackage = config.callingPackage;
                        } else if (msg.obj instanceof Bundle) {
                            callingPackage = ((Bundle) msg.obj).getString("callingPackage");
                            msg.obj = null;
                        }
                        Slog.i(WifiServiceImpl.TAG, "CONNECT callingPackage=" + callingPackage);
                        if (WifiCommonUtils.PACKAGE_NAME_ASSOCIATE_SETTINGS.equals(callingPackage)) {
                            WifiServiceImpl.this.mClientModeImpl.setWifiMode("android", 0);
                        }
                        int networkId = msg.arg1;
                        Slog.i(WifiServiceImpl.TAG, "CONNECT  nid=" + Integer.toString(networkId) + " config=" + config + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        WifiServiceImpl.this.mClientModeImpl.setCHRConnectingSartTimestamp(WifiServiceImpl.this.mClock.getElapsedSinceBootMillis());
                        if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                            WifiServiceImpl.this.mHwWifiCHRService.updateApkChangewWifiStatus(10, WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        }
                        WifiServiceImpl.this.mWifiController.updateWMUserAction(WifiServiceImpl.this.mContext, "ACTION_SELECT_WIFINETWORK", WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config != null) {
                            if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                                WifiServiceImpl.this.mHwWifiCHRService.updateConnectType("FIRST_CONNECT");
                            }
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            return;
                        } else if (config != null || networkId == -1) {
                            Slog.e(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring invalid msg=" + msg);
                            replyFailed(msg, 151554, 8);
                            return;
                        } else {
                            if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                                WifiServiceImpl.this.mHwWifiCHRService.updateConnectType("SELECT_CONNECT");
                            }
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            return;
                        }
                    } else {
                        return;
                    }
                case 151556:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151557)) {
                        if (!(WifiServiceImpl.this.mHwWifiCHRService == null || Settings.Secure.getInt(WifiServiceImpl.this.mContext.getContentResolver(), "wifipro_recommending_access_points", 0) == 1)) {
                            WifiServiceImpl.this.mHwWifiCHRService.reportHwCHRAccessNetworkEventInfoList(2);
                            WifiServiceImpl.this.mHwWifiCHRService.updateApkChangewWifiStatus(11, WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        }
                        WifiServiceImpl.this.mWifiController.updateWMUserAction(WifiServiceImpl.this.mContext, "ACTION_FORGET_WIFINETWORK", WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
                            WifiServiceImpl.this.handleForgetNetwork(Message.obtain(msg));
                            return;
                        } else {
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            return;
                        }
                    } else {
                        return;
                    }
                case 151559:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151560)) {
                        WifiConfiguration config2 = (WifiConfiguration) msg.obj;
                        int networkId2 = msg.arg1;
                        Slog.i(WifiServiceImpl.TAG, "SAVE nid=" + Integer.toString(networkId2) + " config=" + config2 + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config2 != null) {
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            return;
                        }
                        Slog.e(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring invalid msg=" + msg);
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
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
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
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        return;
                    }
                case 151569:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151570)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151572:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151574)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151575:
                    WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }

        private boolean isAllowWifiConnect(int uid) {
            if (WifiServiceImpl.this.mContext == null) {
                Slog.e(WifiServiceImpl.TAG, "isInWhiteAppList: mContext is null");
                return false;
            }
            String packageName = WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(uid);
            if (packageName != null) {
                return WifiServiceImpl.this.mWifiPermissionsUtil.isAllowWifiConnect(uid, packageName);
            }
            Slog.e(WifiServiceImpl.TAG, "isInWhiteAppList: appName is null");
            return false;
        }

        private boolean checkChangePermissionAndReplyIfNotAuthorized(Message msg, int replyWhat) {
            if (WifiServiceImpl.this.mWifiPermissionsUtil.checkChangePermission(msg.sendingUid)) {
                return true;
            }
            Slog.e(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring unauthorized msg=" + msg);
            replyFailed(msg, replyWhat, 9);
            return false;
        }

        private boolean checkPrivilegedPermissionsAndReplyIfNotAuthorized(Message msg, int replyWhat) {
            if (WifiServiceImpl.this.isPrivileged(-1, msg.sendingUid)) {
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

    /* access modifiers changed from: private */
    public class ClientModeImplHandler extends WifiHandler {
        private AsyncChannel mCmiChannel;

        ClientModeImplHandler(String tag, Looper looper, AsyncChannel asyncChannel) {
            super(tag, looper);
            this.mCmiChannel = asyncChannel;
            this.mCmiChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mClientModeImpl.getHandler());
        }

        @Override // com.android.server.wifi.util.WifiHandler, android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i != 69632) {
                if (i != 69636) {
                    Slog.d(WifiServiceImpl.TAG, "ClientModeImplHandler.handleMessage ignoring msg=" + msg);
                    return;
                }
                Slog.e(WifiServiceImpl.TAG, "ClientModeImpl channel lost, msg.arg1 =" + msg.arg1);
                WifiServiceImpl wifiServiceImpl = WifiServiceImpl.this;
                wifiServiceImpl.mClientModeImplChannel = null;
                this.mCmiChannel.connect(wifiServiceImpl.mContext, this, WifiServiceImpl.this.mClientModeImpl.getHandler());
            } else if (msg.arg1 == 0) {
                WifiServiceImpl.this.mClientModeImplChannel = this.mCmiChannel;
            } else {
                Slog.e(WifiServiceImpl.TAG, "ClientModeImpl connection failure, error=" + msg.arg1);
                WifiServiceImpl.this.mClientModeImplChannel = null;
            }
        }
    }

    public WifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mClock = wifiInjector.getClock();
        this.mFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mWifiTrafficPoller = this.mWifiInjector.getWifiTrafficPoller();
        this.mUserManager = this.mWifiInjector.getUserManager();
        this.mCountryCode = this.mWifiInjector.getWifiCountryCode();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mClientModeImpl = this.mWifiInjector.getClientModeImpl();
        this.mWifiNative = this.mWifiInjector.getWifiNative();
        this.mActiveModeWarden = this.mWifiInjector.getActiveModeWarden();
        this.mClientModeImpl.enableRssiPolling(true);
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mSettingsStore = this.mWifiInjector.getWifiSettingsStore();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mWifiLockManager = this.mWifiInjector.getWifiLockManager();
        this.mWifiMulticastLockManager = this.mWifiInjector.getWifiMulticastLockManager();
        HandlerThread wifiServiceHandlerThread = this.mWifiInjector.getWifiServiceHandlerThread();
        this.mAsyncChannelExternalClientHandler = new AsyncChannelExternalClientHandler(TAG, wifiServiceHandlerThread.getLooper());
        this.mClientModeImplHandler = new ClientModeImplHandler(TAG, wifiServiceHandlerThread.getLooper(), asyncChannel);
        this.mWifiController = this.mWifiInjector.getWifiController();
        this.mWifiBackupRestore = this.mWifiInjector.getWifiBackupRestore();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mLog = this.mWifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mIfaceIpModes = new ConcurrentHashMap<>();
        this.mLocalOnlyHotspotRequests = new HashMap<>();
        enableVerboseLoggingInternal(getVerboseLoggingLevel());
        this.mRegisteredSoftApCallbacks = new ExternalCallbackTracker<>(this.mClientModeImplHandler);
        this.mWifiInjector.getActiveModeWarden().registerSoftApCallback(new SoftApCallbackImpl());
        this.mPowerProfile = this.mWifiInjector.getPowerProfile();
        this.mWifiNetworkSuggestionsManager = this.mWifiInjector.getWifiNetworkSuggestionsManager();
        this.mDppManager = this.mWifiInjector.getDppManager();
        this.mClientModeImpl.setmSettingsStore(this.mSettingsStore);
        this.dataProvider = DataProvider.getInstance();
        this.dataProvider.setContext(this.mContext);
    }

    @VisibleForTesting
    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mAsyncChannelExternalClientHandler.setWifiLog(log);
    }

    public void checkAndStartWifi() {
        if (this.mFrameworkFacade.inStorageManagerCryptKeeperBounce()) {
            Log.i(TAG, "Device still encrypted. Need to restart SystemServer.  Do not start wifi.");
            return;
        }
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        StringBuilder sb = new StringBuilder();
        sb.append("WifiService starting up with Wi-Fi ");
        sb.append(wifiEnabled ? "enabled" : "disabled");
        Slog.i(TAG, sb.toString());
        this.mClientModeImpl.setWifiRepeaterStoped();
        registerForScanModeChange();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (WifiServiceImpl.this.mdmForPolicyForceOpenWifi(false, false)) {
                    Slog.w(WifiServiceImpl.TAG, "mdm force open wifi, not allow airplane close wifi");
                    return;
                }
                if (!WifiServiceImpl.this.mSettingsStore.handleAirplaneModeToggled()) {
                    WifiServiceImpl.this.handleAirplaneNotSensitiveWifi();
                } else if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
                    WifiServiceImpl.this.handleAirplaneModeToggled();
                } else {
                    WifiServiceImpl.this.mWifiController.sendMessage(155657);
                }
                if (WifiServiceImpl.this.mSettingsStore.isAirplaneModeOn()) {
                    Log.i(WifiServiceImpl.TAG, "resetting country code because Airplane mode is ON");
                    WifiServiceImpl.this.mCountryCode.airplaneModeEnabled();
                }
            }
        }, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra("ss");
                if ("ABSENT".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was removed");
                    WifiServiceImpl.this.mClientModeImpl.resetSimAuthNetworks(false);
                    WifiServiceImpl.this.mClientModeImpl.notifyImsiAvailabe(false);
                } else if ("LOCKED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "SIM is locked");
                    WifiServiceImpl.this.mClientModeImpl.notifyImsiAvailabe(false);
                } else if ("IMSI".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "SIM is available");
                    WifiServiceImpl.this.mClientModeImpl.notifyImsiAvailabe(true);
                } else if ("LOADED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was loaded");
                    WifiServiceImpl.this.mClientModeImpl.resetSimAuthNetworks(true);
                }
            }
        }, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                WifiServiceImpl.this.handleWifiApStateChange(intent.getIntExtra("wifi_state", 11), intent.getIntExtra("previous_wifi_state", 11), intent.getIntExtra("wifi_ap_error_code", -1), intent.getStringExtra("wifi_ap_interface_name"), intent.getIntExtra("wifi_ap_mode", -1));
            }
        }, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        registerForBroadcasts();
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        this.mInLightIdleMode = this.mPowerManager.isLightDeviceIdleMode();
        int waitRetry = 0;
        while (waitRetry < 10 && this.mClientModeImplChannel == null) {
            Log.e(TAG, "wait connect to WifiStateMachineChannel sleep" + waitRetry);
            try {
                Thread.sleep(100);
                waitRetry++;
            } catch (InterruptedException e) {
                Log.e(TAG, "exception happened");
            }
        }
        if (!this.mClientModeImpl.syncInitialize(this.mClientModeImplChannel)) {
            Log.wtf(TAG, "Failed to initialize ClientModeImpl");
        }
        this.mWifiController.start();
        HwWifiServiceFactory.getHwWifiServiceManager().createHwArpVerifier(this.mContext);
        if (wifiEnabled) {
            setWifiEnabled(this.mContext.getPackageName(), wifiEnabled);
        }
    }

    public void handleBootCompleted() {
        Log.i(TAG, "Handle boot completed");
        this.mClientModeImpl.handleBootCompleted();
    }

    public void handleUserSwitch(int userId) {
        Log.d(TAG, "Handle user switch " + userId);
        this.mClientModeImpl.handleUserSwitch(userId);
    }

    public void handleUserUnlock(int userId) {
        Log.d(TAG, "Handle user unlock " + userId);
        this.mClientModeImpl.handleUserUnlock(userId);
    }

    public void handleUserStop(int userId) {
        Log.d(TAG, "Handle user stop " + userId);
        this.mClientModeImpl.handleUserStop(userId);
    }

    public boolean startScan(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_STARTSCAN);
        boolean isCallFromZ = false;
        Bundle zCaller = Bundle.EMPTY;
        if (Binder.getCallingUid() == 1000 && (zCaller = parseZcaller(packageName)) != null && !zCaller.isEmpty()) {
            isCallFromZ = true;
            packageName = String.valueOf(zCaller.getString("pkg"));
        }
        if (isScreenOff(packageName)) {
            return false;
        }
        if (!isCallFromZ && enforceChangePermission(packageName) != 0) {
            return false;
        }
        int callingUid = isCallFromZ ? zCaller.getInt("uid") : Binder.getCallingUid();
        int callingPid = isCallFromZ ? zCaller.getInt("pid") : Binder.getCallingPid();
        long ident = Binder.clearCallingIdentity();
        this.mLog.info("startScan uid=%").c((long) callingUid).flush();
        if (limitForegroundWifiScanRequest(packageName, callingUid)) {
            Log.i(TAG, "current foreground scan request is refused " + packageName);
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
        if (packageName != null) {
            try {
                if (packageName.length() != 0) {
                    this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, callingUid);
                }
            } catch (SecurityException e) {
                Slog.e(TAG, "Permission violation - startScan not allowed for uid=" + callingUid + ", packageName=" + packageName + ", reason=" + e);
                Binder.restoreCallingIdentity(ident);
                return false;
            } catch (Exception e2) {
                Log.w(TAG, "Exception in startScan()");
                Binder.restoreCallingIdentity(ident);
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
        if (this.mClientModeImpl.disallowWifiScanRequest(callingPid)) {
            Slog.i(TAG, "wifi_scan reject because the interval isn't arrived");
            sendFailedScanDirectionalBroadcast(packageName);
            Binder.restoreCallingIdentity(ident);
            return false;
        } else if (startQuickttffScan(packageName)) {
            Binder.restoreCallingIdentity(ident);
            return true;
        } else if (isWifiScanRequestRefused(packageName)) {
            Binder.restoreCallingIdentity(ident);
            return false;
        } else if (restrictWifiScanRequest(packageName)) {
            Slog.i(TAG, "scan ctrl by PG, skip " + packageName);
            Binder.restoreCallingIdentity(ident);
            return false;
        } else {
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateApkChangewWifiStatus(4, packageName);
            }
            if (this.mHiCoexManager == null) {
                this.mHiCoexManager = HwWifiServiceFactory.getHiCoexManager();
            }
            if (this.mHiCoexManager != null) {
                this.mHiCoexManager.notifyForegroundScan(true, packageName);
            }
            GeneralUtil.Mutable<Boolean> scanSuccess = new GeneralUtil.Mutable<>();
            if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanSuccess, callingUid, packageName) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$71KWGZ9o3U1lf_2vP7tmY9cz4qQ */
                private final /* synthetic */ GeneralUtil.Mutable f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ String f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$startScan$0$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            }, 4000)) {
                Log.e(TAG, "Failed to post runnable to start scan");
                sendFailedScanBroadcast();
                Binder.restoreCallingIdentity(ident);
                return false;
            } else if (!scanSuccess.value.booleanValue()) {
                Log.e(TAG, "Failed to start scan");
                Binder.restoreCallingIdentity(ident);
                return false;
            } else {
                Binder.restoreCallingIdentity(ident);
                return true;
            }
        }
    }

    public /* synthetic */ void lambda$startScan$0$WifiServiceImpl(GeneralUtil.Mutable scanSuccess, int callingUid, String callingPkg) {
        scanSuccess.value = (E) Boolean.valueOf(this.mScanRequestProxy.startScan(callingUid, callingPkg));
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

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        enforceConnectivityInternalPermission();
        if (!this.mVerboseLoggingEnabled) {
            return null;
        }
        this.mLog.info("getCurrentNetworkWpsNfcConfigurationToken uid=%").c((long) Binder.getCallingUid()).flush();
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
                    Slog.i(TAG, "handleLightIdleModeChanged: lightIdle:" + lightIdle + ",deepIdle:" + deepIdle + ",combinedIdle:" + combinedIdle);
                    if (this.mInLightIdleMode != combinedIdle) {
                        this.mInLightIdleMode = combinedIdle;
                        setFilterEnable(combinedIdle);
                        if (this.mHwWifiCHRService != null) {
                            Bundle chrData = new Bundle();
                            chrData.putBoolean(KEY_IS_DOZE_ENABLE, combinedIdle);
                            this.mHwWifiCHRService.uploadDFTEvent(26, chrData);
                        }
                    }
                }
            }
            combinedIdle = true;
            Slog.i(TAG, "handleLightIdleModeChanged: lightIdle:" + lightIdle + ",deepIdle:" + deepIdle + ",combinedIdle:" + combinedIdle);
            if (this.mInLightIdleMode != combinedIdle) {
            }
        }
    }

    private boolean checkNetworkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0;
    }

    private boolean checkNetworkSetupWizardPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETUP_WIZARD", pid, uid) == 0;
    }

    private boolean checkNetworkStackPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_STACK", pid, uid) == 0;
    }

    private boolean checkNetworkManagedProvisioningPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_MANAGED_PROVISIONING", pid, uid) == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPrivileged(int pid, int uid) {
        return checkNetworkSettingsPermission(pid, uid) || checkNetworkSetupWizardPermission(pid, uid) || checkNetworkStackPermission(pid, uid) || checkNetworkManagedProvisioningPermission(pid, uid);
    }

    private boolean isSettingsOrSuw(int pid, int uid) {
        return checkNetworkSettingsPermission(pid, uid) || checkNetworkSetupWizardPermission(pid, uid);
    }

    private boolean isSystem(String packageName) {
        long ident = Binder.clearCallingIdentity();
        boolean z = false;
        try {
            ApplicationInfo info = this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
            if (info.isSystemApp() || info.isUpdatedSystemApp()) {
                z = true;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void setFilterEnable(boolean enable) {
        Slog.d(TAG, "setFilterEnable:" + enable);
        this.mWifiNative.mHwWifiNativeEx.setFilterEnable(this.mWifiNative.getClientInterfaceName(), enable);
    }

    private boolean isDeviceOrProfileOwner(int uid) {
        DevicePolicyManagerInternal dpmi = this.mWifiInjector.getWifiPermissionsWrapper().getDevicePolicyManagerInternal();
        if (dpmi == null) {
            return false;
        }
        if (dpmi.isActiveAdminWithPolicy(uid, -2) || dpmi.isActiveAdminWithPolicy(uid, -1)) {
            return true;
        }
        return false;
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
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
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

    private void enforceLocationPermission(String pkgName, int uid) {
        this.mWifiPermissionsUtil.enforceLocationPermission(pkgName, uid);
    }

    private boolean isTargetSdkLessThanQOrPrivileged(String packageName, int pid, int uid) {
        return this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29) || isPrivileged(pid, uid) || isDeviceOrProfileOwner(uid) || isSystem(packageName) || this.mWifiPermissionsUtil.checkSystemAlertWindowPermission(uid, packageName);
    }

    public synchronized boolean setWifiEnabled(String packageName, boolean enable) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_SETWIFIENABLED);
        int i = 0;
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        Slog.i(TAG, "setWifiEnabled: " + enable + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", package=" + packageName);
        if (HwDeviceManager.disallowOp(0)) {
            Slog.i(TAG, "Wifi has been restricted by MDM apk.");
            return false;
        } else if (mdmForPolicyForceOpenWifi(true, enable)) {
            Slog.w(TAG, "mdm force open wifi, not allow close wifi");
            return false;
        } else {
            boolean isPrivileged = isPrivileged(Binder.getCallingPid(), Binder.getCallingUid());
            if (!isPrivileged && !this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29)) {
                this.mLog.info("setWifiEnabled not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
                return false;
            } else if (!this.mSettingsStore.isAirplaneModeOn() || isPrivileged) {
                if (!(this.mSoftApState == 13 || this.mSoftApState == 12 || this.mWifiApState == 13 || this.mWifiApState == 12) || isPrivileged) {
                    this.mLog.info("setWifiEnabled package=% uid=% enable=%").c(packageName).c((long) Binder.getCallingUid()).c(enable).flush();
                    if (this.mHwWifiCHRService != null) {
                        if (enable) {
                            this.mHwWifiCHRService.updateApkChangewWifiStatus(5, packageName);
                            this.mClientModeImpl.notifyApkChangeWifiStatus(true, packageName);
                        } else {
                            this.mHwWifiCHRService.updateApkChangewWifiStatus(1, packageName);
                            this.mClientModeImpl.notifyApkChangeWifiStatus(false, packageName);
                        }
                    }
                    if (enable) {
                        this.mWifiController.updateWMUserAction(this.mContext, "ACTION_ENABLE_WIFI_TRUE", packageName);
                    } else {
                        this.mWifiController.updateWMUserAction(this.mContext, "ACTION_ENABLE_WIFI_FALSE", packageName);
                    }
                    if (this.mContext != null && !HwSystemManager.allowOp(this.mContext, 2097152, enable)) {
                        if (enable) {
                            this.dataProvider.e(52, "{ACT:1,STATUS:failed,DETAIL:permission deny}");
                        } else {
                            this.dataProvider.e(52, "{ACT:0,STATUS:failed,DETAIL:permission deny}");
                        }
                        return false;
                    } else if (Binder.getCallingUid() == 0 || !"factory".equals(SystemProperties.get("ro.runmode", "normal")) || SystemProperties.getInt("wlan.wltest.status", 0) <= 0) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            if (!this.mSettingsStore.handleWifiToggled(packageName, enable)) {
                                if (enable) {
                                    this.dataProvider.e(52, "{ACT:1,STATUS:failed,DETAIL:cannot be toggled}");
                                } else {
                                    this.dataProvider.e(52, "{ACT:0,STATUS:failed,DETAIL:cannot be toggled}");
                                }
                                return true;
                            }
                            Binder.restoreCallingIdentity(ident);
                            if (this.mHwWifiCHRService != null) {
                                this.mHwWifiCHRService.updateWifiTriggerState(enable);
                            }
                            if (Boolean.valueOf(SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false)).booleanValue()) {
                                setWifiEnabledAfterVoWifiOff(enable);
                            } else {
                                this.mWifiController.sendMessage(155656);
                            }
                            if (!enable) {
                                String[] strArr = CLEAR_WIFI_SAVED_STATE_PACKAGE_NAME_LIST;
                                int length = strArr.length;
                                while (true) {
                                    if (i >= length) {
                                        break;
                                    } else if (strArr[i].equals(packageName)) {
                                        this.mWifiController.sendMessage(155671);
                                        Log.i(TAG, "user close wifi, setWifiSavedState to disabled status");
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            this.mWifiMetrics.incrementNumWifiToggles(isPrivileged, enable);
                            return true;
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else {
                        Slog.e(TAG, "in wltest mode, dont allow to enable WiFi");
                        return false;
                    }
                } else {
                    this.mLog.err("setWifiEnabled SoftAp enabled: only Settings can toggle wifi").flush();
                    return false;
                }
            } else {
                this.mLog.err("setWifiEnabled in Airplane mode: only Settings can toggle wifi").flush();
                return false;
            }
        }
    }

    public int getWifiEnabledState() {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETWIFIENABLESTATE);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getWifiEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if ((this.mClientModeImpl.getWifiMode() & 8) != 0) {
            return 1;
        }
        return this.mClientModeImpl.syncGetWifiState();
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getWifiApEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        MutableInt apState = new MutableInt(11);
        this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(apState) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$Tk4v3H_jLeO4POzFwYzi9LRyPtE */
            private final /* synthetic */ MutableInt f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$getWifiApEnabledState$1$WifiServiceImpl(this.f$1);
            }
        }, 4000);
        return apState.value;
    }

    public /* synthetic */ void lambda$getWifiApEnabledState$1$WifiServiceImpl(MutableInt apState) {
        if (this.mWifiApMode == 2) {
            apState.value = this.mWifiApState;
        } else {
            apState.value = this.mSoftApState;
        }
    }

    public void updateInterfaceIpState(String ifaceName, int mode) {
        enforceNetworkStackPermission();
        this.mLog.info("updateInterfaceIpState uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(ifaceName, mode) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$UQ9JbF5sXBV77FhG4oE7wjNFgek */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$updateInterfaceIpState$2$WifiServiceImpl(this.f$1, this.f$2);
            }
        });
        this.mWifiApMode = mode;
    }

    /* access modifiers changed from: private */
    /* renamed from: updateInterfaceIpStateInternal */
    public void lambda$updateInterfaceIpState$2$WifiServiceImpl(String ifaceName, int mode) {
        synchronized (this.mLocalOnlyHotspotRequests) {
            int previousMode = -1;
            if (ifaceName != null) {
                previousMode = this.mIfaceIpModes.put(ifaceName, Integer.valueOf(mode));
            }
            Slog.d(TAG, "updateInterfaceIpState: ifaceName=" + ifaceName + " mode=" + mode + " previous mode= " + previousMode);
            if (mode != -1) {
                if (mode == 0) {
                    Slog.d(TAG, "IP mode config error - need to clean up");
                    if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                        Slog.d(TAG, "no LOHS requests, stop softap");
                        stopSoftAp();
                    } else {
                        Slog.d(TAG, "we have LOHS requests, clean them up");
                        sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                    }
                    lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
                } else if (mode != 1) {
                    if (mode != 2) {
                        this.mLog.warn("updateInterfaceIpStateInternal: unknown mode %").c((long) mode).flush();
                    } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                        stopSoftAp();
                        lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
                    } else {
                        sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked();
                    }
                } else if (!isConcurrentLohsAndTetheringSupported()) {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(3);
                }
            } else if (ifaceName == null) {
                this.mIfaceIpModes.clear();
            }
        }
    }

    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        enforceNetworkStackPermission();
        this.mLog.info("startSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mIfaceIpModes.contains(1) || this.mSoftApState == 11) {
                if (!isConcurrentLohsAndTetheringSupported() && !this.mLocalOnlyHotspotRequests.isEmpty()) {
                    stopSoftApInternal(2);
                }
                return startSoftApInternal(wifiConfig, 1);
            }
            this.mLog.err("Tethering is already active.").flush();
            return false;
        }
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
        Slog.i(TAG, "stopSoftAp: pid=" + pid + ", uid=" + uid + ", name=" + getAppName(pid));
        this.mLog.info("stopSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLog.trace("Call to stop Tethering while LOHS is active, Registered LOHS callers will be updated when softap stopped.").flush();
            }
            stopSoftApInternal = stopSoftApInternal(1);
        }
        return stopSoftApInternal;
    }

    private boolean stopSoftApInternal(int mode) {
        this.mLog.trace("stopSoftApInternal uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiController.sendMessage(155658, 0, mode);
        return true;
    }

    private final class SoftApCallbackImpl implements WifiManager.SoftApCallback {
        private SoftApCallbackImpl() {
        }

        public void onStateChanged(int state, int failureReason) {
            WifiServiceImpl.this.mSoftApState = state;
            Iterator<ISoftApCallback> iterator = WifiServiceImpl.this.mRegisteredSoftApCallbacks.getCallbacks().iterator();
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
            WifiServiceImpl.this.mSoftApNumClients = numClients;
            Iterator<ISoftApCallback> iterator = WifiServiceImpl.this.mRegisteredSoftApCallbacks.getCallbacks().iterator();
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

    public void registerSoftApCallback(IBinder binder, ISoftApCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerSoftApCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$WH1yXObMcpzajFG1KwwEOakTA7o */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ ISoftApCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$registerSoftApCallback$3$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerSoftApCallback$3$WifiServiceImpl(IBinder binder, ISoftApCallback callback, int callbackIdentifier) {
        if (!this.mRegisteredSoftApCallbacks.add(binder, callback, callbackIdentifier)) {
            Log.e(TAG, "registerSoftApCallback: Failed to add callback");
            return;
        }
        try {
            callback.onStateChanged(this.mSoftApState, 0);
            callback.onNumClientsChanged(this.mSoftApNumClients);
        } catch (RemoteException e) {
            Log.e(TAG, "registerSoftApCallback: remote exception -- " + e);
        }
    }

    public void unregisterSoftApCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterSoftApCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$RmshU723eQairQK6HNmdtEWCoRA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$unregisterSoftApCallback$4$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterSoftApCallback$4$WifiServiceImpl(int callbackIdentifier) {
        this.mRegisteredSoftApCallbacks.remove(callbackIdentifier);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiApStateChange(int currentState, int previousState, int errorCode, String ifaceName, int mode) {
        Slog.i(TAG, "handleWifiApStateChange: currentState=" + currentState + " previousState=" + previousState + " errorCode= " + errorCode + " ifaceName=" + ifaceName + " mode=" + mode);
        this.mWifiApState = currentState;
        if (currentState == 14) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                int errorToReport = 2;
                if (errorCode == 1) {
                    errorToReport = 1;
                }
                sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(errorToReport);
                lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
            }
        } else if (currentState == 10 || currentState == 11) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                if (ifaceName != null) {
                    if (this.mIfaceIpModes.getOrDefault(ifaceName, -1).intValue() == 2) {
                        sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked();
                        updateInterfaceIpState(null, -1);
                    }
                }
                if (!isConcurrentLohsAndTetheringSupported()) {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                }
                updateInterfaceIpState(null, -1);
            }
        }
    }

    @GuardedBy({"mLocalOnlyHotspotRequests"})
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

    @GuardedBy({"mLocalOnlyHotspotRequests"})
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

    @GuardedBy({"mLocalOnlyHotspotRequests"})
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

    /* JADX INFO: finally extract failed */
    public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (enforceChangePermission(packageName) != 0) {
            return 2;
        }
        enforceLocationPermission(packageName, uid);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mWifiPermissionsUtil.isLocationModeEnabled()) {
                Binder.restoreCallingIdentity(ident);
                if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
                    return 4;
                }
                if (!this.mFrameworkFacade.isAppForeground(uid)) {
                    return 3;
                }
                Slog.d(TAG, "startLocalOnlyHotspot: pid=" + pid + ", uid=" + uid + ", packageName=" + packageName);
                this.mLog.info("startLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
                synchronized (this.mLocalOnlyHotspotRequests) {
                    int i = 1;
                    if (!isConcurrentLohsAndTetheringSupported() && this.mIfaceIpModes.contains(1)) {
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
                            boolean is5Ghz = hasAutomotiveFeature(this.mContext) && this.mContext.getResources().getBoolean(17891592) && is5GhzSupported();
                            Context context = this.mContext;
                            if (!is5Ghz) {
                                i = 0;
                            }
                            this.mLocalOnlyHotspotConfig = WifiApConfigStore.generateLocalOnlyHotspotConfig(context, i);
                            startSoftApInternal(this.mLocalOnlyHotspotConfig, 2);
                        }
                        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
                        return 0;
                    } else {
                        this.mLog.trace("caller already has an active request").flush();
                        throw new IllegalStateException("Caller already has an active LocalOnlyHotspot request");
                    }
                }
            } else {
                throw new SecurityException("Location mode is not enabled.");
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterCallingAppAndStopLocalOnlyHotspot(LocalOnlyHotspotRequestInfo request) {
        this.mLog.trace("unregisterCallingAppAndStopLocalOnlyHotspot pid=%").c((long) request.getPid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (this.mLocalOnlyHotspotRequests.remove(Integer.valueOf(request.getPid())) == null) {
                this.mLog.trace("LocalOnlyHotspotRequestInfo not found to remove").flush();
                return;
            }
            if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLocalOnlyHotspotConfig = null;
                lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
                long identity = Binder.clearCallingIdentity();
                try {
                    stopSoftApInternal(2);
                } finally {
                    Binder.restoreCallingIdentity(identity);
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
            Log.i(TAG, "getWifiApConfiguration, uid=" + uid);
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
                notifyWifiConfigChanged(wifiConfig, packageName, false);
                Log.i(TAG, "apBand set to " + wifiConfig.apBand);
                this.mClientModeImplHandler.post(new Runnable(wifiConfig) {
                    /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$FOIRgQIsxi1087ZGnuDalgvVY4w */
                    private final /* synthetic */ WifiConfiguration f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        WifiServiceImpl.this.lambda$setWifiApConfiguration$5$WifiServiceImpl(this.f$1);
                    }
                });
                return true;
            }
            Slog.e(TAG, "Invalid WifiConfiguration");
            return false;
        }
        throw new SecurityException("App not allowed to read or update stored WiFi AP config (uid = " + uid + ")");
    }

    public /* synthetic */ void lambda$setWifiApConfiguration$5$WifiServiceImpl(WifiConfiguration wifiConfig) {
        this.mWifiApConfigStore.setApConfiguration(wifiConfig);
    }

    public void notifyUserOfApBandConversion(String packageName) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("notifyUserOfApBandConversion uid=% packageName=%").c((long) Binder.getCallingUid()).c(packageName).flush();
        }
        this.mWifiApConfigStore.notifyUserOfApBandConversion(packageName);
    }

    public boolean isScanAlwaysAvailable() {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_ISSACNALWAYSAVAILABLE);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isScanAlwaysAvailable uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public boolean disconnect(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_DISCONNECT);
        Slog.i(TAG, "disconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("disconnect not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("disconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.disconnectCommand();
        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
        if (hwWifiCHRService == null) {
            return true;
        }
        hwWifiCHRService.updateApkChangewWifiStatus(8, getAppName(Binder.getCallingPid()));
        return true;
    }

    public boolean reconnect(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_RECONNECT);
        Slog.i(TAG, "reconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("reconnect not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("reconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.reconnectCommand(new WorkSource(Binder.getCallingUid()));
        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
        if (hwWifiCHRService == null) {
            return true;
        }
        hwWifiCHRService.updateApkChangewWifiStatus(6, getAppName(Binder.getCallingPid()));
        return true;
    }

    public boolean reassociate(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_REASOCIATE);
        Slog.i(TAG, "reassociate:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("reassociate not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("reassociate uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.reassociateCommand();
        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
        if (hwWifiCHRService == null) {
            return true;
        }
        hwWifiCHRService.updateApkChangewWifiStatus(7, getAppName(Binder.getCallingPid()));
        return true;
    }

    public long getSupportedFeatures() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getSupportedFeatures uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return getSupportedFeaturesInternal();
    }

    public void requestActivityInfo(ResultReceiver result) {
        if (result == null) {
            Log.e(TAG, "Request Activity Info: result is null");
            return;
        }
        Bundle bundle = new Bundle();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("requestActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    /* JADX INFO: Multiple debug info for r7v5 double: [D('rxIdleCurrent' double), D('txCurrent' double)] */
    public WifiActivityEnergyInfo reportActivityInfo() {
        WifiActivityEnergyInfo energyInfo;
        long[] txTimePerLevel;
        double rxIdleCurrent;
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("reportActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo energyInfo2 = null;
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            WifiLinkLayerStats stats = this.mClientModeImpl.syncGetLinkLayerStats(asyncChannel);
            if (stats != null) {
                double rxIdleCurrent2 = this.mPowerProfile.getAveragePower("wifi.controller.idle");
                double rxCurrent = this.mPowerProfile.getAveragePower("wifi.controller.rx");
                double txCurrent = this.mPowerProfile.getAveragePower("wifi.controller.tx");
                double voltage = this.mPowerProfile.getAveragePower("wifi.controller.voltage") / 1000.0d;
                long rxIdleTime = (long) ((stats.on_time - stats.tx_time) - stats.rx_time);
                if (stats.tx_time_per_level != null) {
                    long[] txTimePerLevel2 = new long[stats.tx_time_per_level.length];
                    int i = 0;
                    while (i < txTimePerLevel2.length) {
                        txTimePerLevel2[i] = (long) stats.tx_time_per_level[i];
                        i++;
                        energyInfo2 = energyInfo2;
                    }
                    txTimePerLevel = txTimePerLevel2;
                } else {
                    txTimePerLevel = new long[0];
                }
                long energyUsed = (long) (((((double) stats.tx_time) * txCurrent) + (((double) stats.rx_time) * rxCurrent) + (((double) rxIdleTime) * rxIdleCurrent2)) * voltage);
                if (rxIdleTime < 0 || stats.on_time < 0 || stats.tx_time < 0 || stats.rx_time < 0 || stats.on_time_scan < 0 || energyUsed < 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" rxIdleCur=" + rxIdleCurrent2);
                    sb.append(" rxCur=" + rxCurrent);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(" txCur=");
                    rxIdleCurrent = txCurrent;
                    sb2.append(rxIdleCurrent);
                    sb.append(sb2.toString());
                    sb.append(" voltage=" + voltage);
                    sb.append(" on_time=" + stats.on_time);
                    sb.append(" tx_time=" + stats.tx_time);
                    sb.append(" tx_time_per_level=" + Arrays.toString(txTimePerLevel));
                    sb.append(" rx_time=" + stats.rx_time);
                    sb.append(" rxIdleTime=" + rxIdleTime);
                    sb.append(" scan_time=" + stats.on_time_scan);
                    sb.append(" energy=" + energyUsed);
                    Log.d(TAG, " reportActivityInfo: " + sb.toString());
                } else {
                    rxIdleCurrent = txCurrent;
                }
                energyInfo = new WifiActivityEnergyInfo(this.mClock.getElapsedSinceBootMillis(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, (long) stats.on_time_scan, rxIdleTime, energyUsed);
            } else {
                energyInfo = null;
            }
            if (energyInfo == null || !energyInfo.isValid()) {
                return null;
            }
            return energyInfo;
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getConfiguredNetworks(String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETCONFIGUREDNETWORKS);
        enforceAccessPermission();
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 2000 || callingUid == 0)) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, callingUid);
            } catch (SecurityException e) {
                Slog.e(TAG, "Permission violation - getConfiguredNetworks not allowed for uid=" + callingUid + ", packageName=" + packageName + ", reason=" + e);
                return new ParceledListSlice<>(new ArrayList());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        boolean isTargetSdkLessThanQOrPrivileged = isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), callingUid);
        boolean isCarrierApp = true;
        if (this.mWifiInjector.makeTelephonyManager().checkCarrierPrivilegesForPackageAnyPhone(packageName) != 1) {
            isCarrierApp = false;
        }
        if (isTargetSdkLessThanQOrPrivileged || isCarrierApp) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getConfiguredNetworks uid=%").c((long) callingUid).flush();
            }
            int targetConfigUid = -1;
            if (isPrivileged(getCallingPid(), callingUid) || isDeviceOrProfileOwner(callingUid)) {
                targetConfigUid = 1010;
            } else if (isCarrierApp) {
                targetConfigUid = callingUid;
            }
            AsyncChannel asyncChannel = this.mClientModeImplChannel;
            if (asyncChannel != null) {
                List<WifiConfiguration> configs = this.mClientModeImpl.syncGetConfiguredNetworks(callingUid, asyncChannel, targetConfigUid);
                if (configs == null) {
                    return null;
                }
                if (isTargetSdkLessThanQOrPrivileged) {
                    return new ParceledListSlice<>(configs);
                }
                List<WifiConfiguration> creatorConfigs = new ArrayList<>();
                for (WifiConfiguration config : configs) {
                    if (config.creatorUid == callingUid) {
                        creatorConfigs.add(config);
                    }
                }
                return new ParceledListSlice<>(creatorConfigs);
            }
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return null;
        }
        this.mLog.info("getConfiguredNetworks not allowed for uid=%").c((long) callingUid).flush();
        return new ParceledListSlice<>(new ArrayList());
    }

    public ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks(String packageName) {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, callingUid);
            Binder.restoreCallingIdentity(ident);
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getPrivilegedConfiguredNetworks uid=%").c((long) callingUid).flush();
            }
            AsyncChannel asyncChannel = this.mClientModeImplChannel;
            if (asyncChannel != null) {
                List<WifiConfiguration> configs = this.mClientModeImpl.syncGetPrivilegedConfiguredNetwork(asyncChannel);
                if (configs != null) {
                    return new ParceledListSlice<>(configs);
                }
            } else {
                Slog.e(TAG, "mClientModeImplChannel is not initialized");
            }
            return null;
        } catch (SecurityException e) {
            Slog.e(TAG, "Permission violation - getPrivilegedConfiguredNetworks not allowed for uid=" + callingUid + ", packageName=" + packageName + ", reason=" + e);
            Binder.restoreCallingIdentity(ident);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public Map<String, Map<Integer, List<ScanResult>>> getAllMatchingFqdnsForScanResults(List<ScanResult> scanResults) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getMatchingPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new HashMap();
            }
            return this.mClientModeImpl.syncGetAllMatchingFqdnsForScanResults(scanResults, this.mClientModeImplChannel);
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public Map<OsuProvider, List<ScanResult>> getMatchingOsuProviders(List<ScanResult> scanResults) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getMatchingOsuProviders uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new HashMap();
            }
            return this.mClientModeImpl.syncGetMatchingOsuProviders(scanResults, this.mClientModeImplChannel);
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public Map<OsuProvider, PasspointConfiguration> getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> osuProviders) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getMatchingPasspointConfigsForOsuProviders uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new HashMap();
            }
            if (osuProviders != null) {
                return this.mClientModeImpl.syncGetMatchingPasspointConfigsForOsuProviders(osuProviders, this.mClientModeImplChannel);
            }
            Log.e(TAG, "Attempt to retrieve Passpoint configuration with null osuProviders");
            return new HashMap();
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> fqdnList) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getWifiConfigsForPasspointProfiles uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new ArrayList();
            }
            if (fqdnList != null) {
                return this.mClientModeImpl.syncGetWifiConfigsForPasspointProfiles(fqdnList, this.mClientModeImplChannel);
            }
            Log.e(TAG, "Attempt to retrieve WifiConfiguration with null fqdn List");
            return new ArrayList();
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public int addOrUpdateNetwork(WifiConfiguration config, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_ADDORUPDATENETWORK);
        Slog.i(TAG, "addOrUpdateNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", config:" + config + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return -1;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("addOrUpdateNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return -1;
        }
        this.mLog.info("addOrUpdateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (config == null) {
            Slog.e(TAG, "bad network configuration");
            return -1;
        }
        this.mWifiMetrics.incrementNumAddOrUpdateNetworkCalls();
        if (config.isPasspoint()) {
            PasspointConfiguration passpointConfig = PasspointProvider.convertFromWifiConfig(config);
            if (passpointConfig.getCredential() == null) {
                Slog.e(TAG, "Missing credential for Passpoint profile");
                return -1;
            }
            X509Certificate[] x509Certificates = null;
            if (config.enterpriseConfig.getCaCertificate() != null) {
                x509Certificates = new X509Certificate[]{config.enterpriseConfig.getCaCertificate()};
            }
            passpointConfig.getCredential().setCaCertificates(x509Certificates);
            passpointConfig.getCredential().setClientCertificateChain(config.enterpriseConfig.getClientCertificateChain());
            passpointConfig.getCredential().setClientPrivateKey(config.enterpriseConfig.getClientPrivateKey());
            if (addOrUpdatePasspointConfiguration(passpointConfig, packageName)) {
                return 0;
            }
            Slog.e(TAG, "Failed to add Passpoint profile");
            return -1;
        }
        Slog.i("addOrUpdateNetwork", " uid = " + Integer.toString(Binder.getCallingUid()) + " SSID " + StringUtilEx.safeDisplaySsid(config.SSID) + " nid=" + Integer.toString(config.networkId));
        if (config.networkId == -1) {
            config.creatorUid = Binder.getCallingUid();
        } else {
            config.lastUpdateUid = Binder.getCallingUid();
        }
        this.mWifiController.updateWMUserAction(this.mContext, "ACTION_SELECT_WIFINETWORK", getAppName(Binder.getCallingPid()));
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            return this.mClientModeImpl.syncAddOrUpdateNetwork(asyncChannel, config);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return -1;
    }

    public static void verifyCert(X509Certificate caCert) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
        CertPath path = factory.generateCertPath(Arrays.asList(caCert));
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null, null);
        PKIXParameters params = new PKIXParameters(ks);
        params.setRevocationEnabled(false);
        validator.validate(path, params);
    }

    public boolean removeNetwork(int netId, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_REMOVENETWORK);
        Slog.i(TAG, "removeNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("removeNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("removeNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiController.updateWMUserAction(this.mContext, "ACTION_REMOVE_WIFINETWORK", getAppName(Binder.getCallingPid()));
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            return this.mClientModeImpl.syncRemoveNetwork(asyncChannel, netId);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_ENABLENETWORK);
        Slog.i(TAG, "enableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", disableOthers:" + disableOthers + ", name=" + getAppName(Binder.getCallingPid()));
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("enableNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("enableNetwork uid=% disableOthers=%").c((long) Binder.getCallingUid()).c(disableOthers).flush();
        this.mWifiMetrics.incrementNumEnableNetworkCalls();
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            return this.mClientModeImpl.syncEnableNetwork(asyncChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId, String packageName) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_DISABLENETWORK);
        String appName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.i(TAG, "disableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + appName);
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
        if (hwWifiCHRService != null) {
            hwWifiCHRService.updateApkChangewWifiStatus(2, appName);
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("disableNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("disableNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            return this.mClientModeImpl.syncDisableNetwork(asyncChannel, netId);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo(String callingPackage) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETCONNECTIONINFO);
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getConnectionInfo uid=%").c((long) uid).flush();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            WifiInfo result = this.mClientModeImpl.syncRequestConnectionInfo(callingPackage, uid);
            boolean hideDefaultMacAddress = true;
            boolean hideBssidSsidAndNetworkId = true;
            try {
                if (this.mWifiInjector.getWifiPermissionsWrapper().getLocalMacAddressPermission(uid) == 0) {
                    hideDefaultMacAddress = false;
                }
                this.mWifiPermissionsUtil.enforceCanAccessScanResults(callingPackage, uid);
                hideBssidSsidAndNetworkId = false;
            } catch (RemoteException e) {
                Log.e(TAG, "Error checking receiver permission", e);
            } catch (SecurityException e2) {
                Log.e(TAG, "enforceCanAccessScanResults: hiding ssid and bssid" + e2.getMessage());
            }
            if (hideDefaultMacAddress) {
                result.setMacAddress("02:00:00:00:00:00");
            }
            if (hideBssidSsidAndNetworkId) {
                result.setBSSID("02:00:00:00:00:00");
                result.setSSID(WifiSsid.createFromHex((String) null));
                result.setNetworkId(-1);
            }
            if (this.mVerboseLoggingEnabled && (hideBssidSsidAndNetworkId || hideDefaultMacAddress)) {
                WifiLog wifiLog = this.mLog;
                wifiLog.v("getConnectionInfo: hideBssidSsidAndNetworkId=" + hideBssidSsidAndNetworkId + ", hideDefaultMacAddress=" + hideDefaultMacAddress);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004f A[Catch:{ SecurityException -> 0x0069, all -> 0x0067 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x005d  */
    public List<ScanResult> getScanResults(String callingPackage) {
        boolean isNeedReportCache;
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
            if (uid >= 10000) {
                if (uid <= 19999) {
                    isNeedReportCache = false;
                    if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanResults, isNeedReportCache) {
                        /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$jYfhIkTA4QY0CvQlQdFyAg90co */
                        private final /* synthetic */ List f$1;
                        private final /* synthetic */ boolean f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            WifiServiceImpl.this.lambda$getScanResults$6$WifiServiceImpl(this.f$1, this.f$2);
                        }
                    }, 4000)) {
                        Log.e(TAG, "Failed to post runnable to fetch scan results");
                        return new ArrayList();
                    }
                    List<ScanResult> scanResultsList = getFilterScanResults(callingPackage, uid, scanResults);
                    Binder.restoreCallingIdentity(ident);
                    return scanResultsList;
                }
            }
            isNeedReportCache = true;
            if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanResults, isNeedReportCache) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$jYfhIkTA4QY0CvQlQdFyAg90co */
                private final /* synthetic */ List f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$getScanResults$6$WifiServiceImpl(this.f$1, this.f$2);
                }
            }, 4000)) {
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "Permission violation - getScanResults not allowed for uid=" + uid + ", packageName=" + callingPackage + ", reason=" + e);
            return new ArrayList();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public /* synthetic */ void lambda$getScanResults$6$WifiServiceImpl(List scanResults, boolean isNeedReportCache) {
        ScanRequestProxy scanRequestProxy = this.mScanRequestProxy;
        scanResults.addAll(isNeedReportCache ? scanRequestProxy.getScanResults() : scanRequestProxy.getScanResult());
    }

    public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mLog.info("addorUpdatePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return false;
        }
        return this.mClientModeImpl.syncAddOrUpdatePasspointConfig(this.mClientModeImplChannel, config, Binder.getCallingUid(), packageName);
    }

    public boolean removePasspointConfiguration(String fqdn, String packageName) {
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) || this.mWifiPermissionsUtil.checkNetworkCarrierProvisioningPermission(uid)) {
            this.mLog.info("removePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return false;
            }
            return this.mClientModeImpl.syncRemovePasspointConfig(this.mClientModeImplChannel, fqdn);
        } else if (this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29)) {
            return false;
        } else {
            throw new SecurityException("WifiService: Permission denied");
        }
    }

    public List<PasspointConfiguration> getPasspointConfigurations(String packageName) {
        int uid = Binder.getCallingUid();
        this.mAppOps.checkPackage(uid, packageName);
        if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) || this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(uid)) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new ArrayList();
            }
            return this.mClientModeImpl.syncGetPasspointConfigs(this.mClientModeImplChannel);
        } else if (this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29)) {
            return new ArrayList();
        } else {
            throw new SecurityException("WifiService: Permission denied");
        }
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        enforceAccessPermission();
        this.mLog.info("queryPasspointIcon uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            this.mClientModeImpl.syncQueryPasspointIcon(this.mClientModeImplChannel, bssid, fileName);
            return;
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        this.mLog.info("matchProviderWithCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mClientModeImpl.matchProviderWithCurrentNetwork(this.mClientModeImplChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mLog.info("deauthenticateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.deauthenticateNetwork(this.mClientModeImplChannel, holdoff, ess);
    }

    public void setCountryCode(String countryCode) {
        Slog.i(TAG, "WifiService trying to set country code");
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
        return isWifiDualBandSupport();
    }

    private int getMaxApInterfacesCount() {
        return this.mContext.getResources().getInteger(17694955);
    }

    private boolean isConcurrentLohsAndTetheringSupported() {
        return getMaxApInterfacesCount() >= 2;
    }

    public boolean needs5GHzToAnyApBandConversion() {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("needs5GHzToAnyApBandConversion uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mContext.getResources().getBoolean(17891579);
    }

    @Deprecated
    public DhcpInfo getDhcpInfo() {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.WIFI_GETPHCPINFO);
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getDhcpInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        DhcpResults dhcpResults = this.mClientModeImpl.syncGetDhcpResults();
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

    /* access modifiers changed from: package-private */
    public class TdlsTaskParams {
        public boolean enable;
        public String remoteIpAddress;

        TdlsTaskParams() {
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
                if (0 != 0) {
                    reader.close();
                }
            } catch (IOException e3) {
                Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
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
            return 0;
        }
    }

    public void enableTdls(String remoteAddress, boolean enable) {
        if (remoteAddress != null) {
            this.mLog.info("enableTdls uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
            TdlsTaskParams params = new TdlsTaskParams();
            params.remoteIpAddress = remoteAddress;
            params.enable = enable;
            new TdlsTask().execute(params);
            return;
        }
        throw new IllegalArgumentException("remoteAddress cannot be null");
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        this.mLog.info("enableTdlsWithMacAddress uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        if (remoteMacAddress != null) {
            this.mClientModeImpl.enableTdls(remoteMacAddress, enable);
            return;
        }
        throw new IllegalArgumentException("remoteMacAddress cannot be null");
    }

    public Messenger getWifiServiceMessenger(String packageName) {
        enforceAccessPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("getWifiServiceMessenger uid=%").c((long) Binder.getCallingUid()).flush();
            return new Messenger(this.mAsyncChannelExternalClientHandler);
        }
        throw new SecurityException("Could not create wifi service messenger");
    }

    public void disableEphemeralNetwork(String SSID, String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
        if (!isPrivileged(Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("disableEphemeralNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return;
        }
        this.mLog.info("disableEphemeralNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.disableEphemeralNetwork(SSID);
    }

    private void registerForScanModeChange() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(null) {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass5 */

            @Override // android.database.ContentObserver
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
        if (this.mContext.getResources().getBoolean(17891581)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        }
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        intentFilter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        if (this.mContext.getResources().getBoolean(17891598)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
        }
        registerForBroadcastsEx(intentFilter);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_FULLY_REMOVED");
        intentFilter2.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "android.intent.action.PACKAGE_FULLY_REMOVED".equals(intent.getAction())) {
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    Uri uri = intent.getData();
                    if (uid != -1 && uri != null) {
                        String pkgName = uri.getSchemeSpecificPart();
                        WifiServiceImpl.this.mClientModeImpl.removeAppConfigs(pkgName, uid);
                        WifiServiceImpl.this.mWifiInjector.getClientModeImplHandler().post(new Runnable(pkgName, uid) {
                            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$6$eec3FNKNle8HO3DXfeN4g_OPHw4 */
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                WifiServiceImpl.AnonymousClass6.this.lambda$onReceive$0$WifiServiceImpl$6(this.f$1, this.f$2);
                            }
                        });
                    }
                }
            }

            public /* synthetic */ void lambda$onReceive$0$WifiServiceImpl$6(String pkgName, int uid) {
                WifiServiceImpl.this.mScanRequestProxy.clearScanRequestTimestampsForApp(pkgName, uid);
                WifiServiceImpl.this.mWifiNetworkSuggestionsManager.removeApp(pkgName);
                WifiServiceImpl.this.mClientModeImpl.removeNetworkRequestUserApprovedAccessPointsForApp(pkgName);
                WifiServiceImpl.this.mWifiInjector.getPasspointManager().removePasspointProviderWithPackage(pkgName);
            }
        }, intentFilter2);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.wifi.WifiServiceImpl */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new WifiShellCommand(this.mWifiInjector).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (args != null && args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mClientModeImpl.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args != null && args.length > 0 && "ipclient".equals(args[0])) {
            String[] ipClientArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipClientArgs, 0, ipClientArgs.length);
            this.mClientModeImpl.dumpIpClient(fd, pw, ipClientArgs);
        } else if (args != null && args.length > 0 && WifiScoreReport.DUMP_ARG.equals(args[0])) {
            WifiScoreReport wifiScoreReport = this.mClientModeImpl.getWifiScoreReport();
            if (wifiScoreReport != null) {
                wifiScoreReport.dump(fd, pw, args);
            }
        } else if (args == null || args.length <= 0 || !WifiScoreCard.DUMP_ARG.equals(args[0])) {
            this.mClientModeImpl.updateLinkLayerStatsRssiAndScoreReport();
            pw.println("Wi-Fi is " + this.mClientModeImpl.syncGetWifiStateByName());
            StringBuilder sb = new StringBuilder();
            sb.append("Verbose logging is ");
            sb.append(this.mVerboseLoggingEnabled ? "on" : "off");
            pw.println(sb.toString());
            pw.println("Stay-awake conditions: " + this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", 0));
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mWifiTrafficPoller.dump(fd, pw, args);
            pw.println();
            pw.println("Locks held:");
            this.mWifiLockManager.dump(pw);
            pw.println();
            this.mWifiMulticastLockManager.dump(pw);
            pw.println();
            this.mActiveModeWarden.dump(fd, pw, args);
            pw.println();
            this.mClientModeImpl.dump(fd, pw, args);
            pw.println();
            this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(pw) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$30hAwmJfIirsTRMV7XyzBiqYQ */
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$dump$8$WifiServiceImpl(this.f$1);
                }
            }, 4000);
            this.mClientModeImpl.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
            pw.println();
            this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(fd, pw, args) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$Dw9NSibgsV9VEB49b2cQPkxW0g */
                private final /* synthetic */ FileDescriptor f$1;
                private final /* synthetic */ PrintWriter f$2;
                private final /* synthetic */ String[] f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$dump$9$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            }, 4000);
            this.mWifiBackupRestore.dump(fd, pw, args);
            pw.println();
            pw.println("ScoringParams: settings put global wifi_score_params " + this.mWifiInjector.getScoringParams());
            pw.println();
            WifiScoreReport wifiScoreReport2 = this.mClientModeImpl.getWifiScoreReport();
            if (wifiScoreReport2 != null) {
                pw.println("WifiScoreReport:");
                wifiScoreReport2.dump(fd, pw, args);
            }
            pw.println();
            SarManager sarManager = this.mWifiInjector.getSarManager();
            if (sarManager != null) {
                sarManager.dump(fd, pw, args);
            }
            pw.println();
        } else {
            this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(pw) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$fQZvukCfLVo4MfiNXpCRx56chK4 */
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$dump$7$WifiServiceImpl(this.f$1);
                }
            }, 4000);
        }
    }

    public /* synthetic */ void lambda$dump$7$WifiServiceImpl(PrintWriter pw) {
        WifiScoreCard wifiScoreCard = this.mWifiInjector.getWifiScoreCard();
        if (wifiScoreCard != null) {
            pw.println(wifiScoreCard.getNetworkListBase64(true));
        }
    }

    public /* synthetic */ void lambda$dump$8$WifiServiceImpl(PrintWriter pw) {
        WifiScoreCard wifiScoreCard = this.mWifiInjector.getWifiScoreCard();
        if (wifiScoreCard != null) {
            pw.println("WifiScoreCard:");
            pw.println(wifiScoreCard.getNetworkListBase64(true));
        }
    }

    public /* synthetic */ void lambda$dump$9$WifiServiceImpl(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mWifiNetworkSuggestionsManager.dump(fd, pw, args);
        pw.println();
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        this.mLog.info("acquireWifiLock uid=% lockMode=%").c((long) Binder.getCallingUid()).c((long) lockMode).flush();
        Slog.d(TAG, "acquireWifiLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", binder:" + binder + ", lockMode:" + lockMode + ", tag:" + tag + ", ws:" + ws);
        if (this.mHwWifiCHRService != null) {
            Log.d(TAG, "Upload apk action event: acquire wifi lock");
            this.mHwWifiCHRService.updateApkChangewWifiStatus(22, getAppName(Binder.getCallingPid()));
        }
        if (binder == null) {
            Log.e(TAG, "acquireWifiLock binder param is invalid!");
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        WorkSource updatedWs = (ws == null || ws.isEmpty()) ? new WorkSource(Binder.getCallingUid()) : ws;
        GeneralUtil.Mutable<Boolean> lockSuccess = new GeneralUtil.Mutable<>();
        if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(lockSuccess, lockMode, tag, binder, updatedWs) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$LzoPy_61rowsLCV41PKihFsJZY */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ IBinder f$4;
            private final /* synthetic */ WorkSource f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$acquireWifiLock$10$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        }, 4000)) {
            return lockSuccess.value.booleanValue();
        }
        Log.e(TAG, "Failed to post runnable to acquireWifiLock");
        return false;
    }

    public /* synthetic */ void lambda$acquireWifiLock$10$WifiServiceImpl(GeneralUtil.Mutable lockSuccess, int lockMode, String tag, IBinder binder, WorkSource updatedWs) {
        lockSuccess.value = (E) Boolean.valueOf(this.mWifiLockManager.acquireWifiLock(lockMode, tag, binder, updatedWs));
    }

    public void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        this.mLog.info("updateWifiLockWorkSource uid=%").c((long) Binder.getCallingUid()).flush();
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
        if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(binder, (ws == null || ws.isEmpty()) ? new WorkSource(Binder.getCallingUid()) : ws) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$ua8EdkfeD59dLikgQVAHnMKKkKs */
            private final /* synthetic */ IBinder f$1;
            private final /* synthetic */ WorkSource f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$updateWifiLockWorkSource$11$WifiServiceImpl(this.f$1, this.f$2);
            }
        }, 4000)) {
            Log.e(TAG, "Failed to post runnable to updateWifiLockWorkSource");
        }
    }

    public /* synthetic */ void lambda$updateWifiLockWorkSource$11$WifiServiceImpl(IBinder binder, WorkSource updatedWs) {
        this.mWifiLockManager.updateWifiLockWorkSource(binder, updatedWs);
    }

    public boolean releaseWifiLock(IBinder binder) {
        this.mLog.info("releaseWifiLock uid=%").c((long) Binder.getCallingUid()).flush();
        Slog.d(TAG, "releaseWifiLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", binder:" + binder);
        if (this.mHwWifiCHRService != null) {
            Log.d(TAG, "Upload apk action event: release wifi lock");
            this.mHwWifiCHRService.updateApkChangewWifiStatus(23, getAppName(Binder.getCallingPid()));
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        GeneralUtil.Mutable<Boolean> lockSuccess = new GeneralUtil.Mutable<>();
        if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(lockSuccess, binder) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$6Qut6vV7vZnj2wywTKZ9YZCZBrs */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ IBinder f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$releaseWifiLock$12$WifiServiceImpl(this.f$1, this.f$2);
            }
        }, 4000)) {
            return lockSuccess.value.booleanValue();
        }
        Log.e(TAG, "Failed to post runnable to releaseWifiLock");
        return false;
    }

    public /* synthetic */ void lambda$releaseWifiLock$12$WifiServiceImpl(GeneralUtil.Mutable lockSuccess, IBinder binder) {
        lockSuccess.value = (E) Boolean.valueOf(this.mWifiLockManager.releaseWifiLock(binder));
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();
        this.mLog.info("initializeMulticastFiltering uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.initializeFiltering();
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();
        this.mLog.info("acquireMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        if (binder == null) {
            Log.e(TAG, "acquireMulticastLock binder param is invalid!");
        } else {
            this.mWifiMulticastLockManager.acquireLock(binder, tag);
        }
    }

    public void releaseMulticastLock(String tag) {
        enforceMulticastChangePermission();
        this.mLog.info("releaseMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.releaseLock(tag);
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
        this.mClientModeImpl.enableVerboseLogging(verbose);
        this.mWifiLockManager.enableVerboseLogging(verbose);
        this.mWifiMulticastLockManager.enableVerboseLogging(verbose);
        this.mWifiInjector.enableVerboseLogging(verbose);
    }

    public int getVerboseLoggingLevel() {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getVerboseLoggingLevel uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0);
    }

    public void factoryReset(String packageName) {
        List<PasspointConfiguration> configs;
        enforceConnectivityInternalPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("factoryReset uid=%").c((long) Binder.getCallingUid()).flush();
            if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
                if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                    stopSoftApInternal(-1);
                    if (this.mContext != null && (WifiCommonUtils.IS_ATT || WifiCommonUtils.IS_VERIZON)) {
                        Settings.System.putInt(this.mContext.getContentResolver(), WifiCommonUtils.SET_SSID_NAME, 1);
                    }
                }
                if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                    int i = 0;
                    while (true) {
                        if (i >= 10) {
                            break;
                        } else if (this.mClientModeImplChannel != null) {
                            List<WifiConfiguration> networks = this.mClientModeImpl.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mClientModeImplChannel, 1010);
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
                                if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint") && (configs = this.mClientModeImpl.syncGetPasspointConfigs(this.mClientModeImplChannel)) != null) {
                                    for (PasspointConfiguration config2 : configs) {
                                        removePasspointConfiguration(config2.getHomeSp().getFqdn(), packageName);
                                    }
                                }
                            }
                        }
                    }
                    this.mSettingsStore.getHwWifiSettingsStoreEx().resetAirplaneModeRadios();
                    this.mWifiInjector.getClientModeImplHandler().post(new Runnable() {
                        /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$veUxSAzcrMzOOli8TagORRevas */

                        @Override // java.lang.Runnable
                        public final void run() {
                            WifiServiceImpl.this.lambda$factoryReset$13$WifiServiceImpl();
                        }
                    });
                }
            }
        }
    }

    public /* synthetic */ void lambda$factoryReset$13$WifiServiceImpl() {
        this.mWifiInjector.getWifiConfigManager().clearDeletedEphemeralNetworks();
        this.mClientModeImpl.clearNetworkRequestUserApprovedAccessPoints();
        this.mWifiNetworkSuggestionsManager.clear();
        this.mWifiInjector.getWifiScoreCard().clear();
    }

    static boolean logAndReturnFalse(String s) {
        Log.d(TAG, s);
        return false;
    }

    public Network getCurrentNetwork() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mClientModeImpl.getCurrentNetwork();
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
            sb.append(String.format(" %02x", Integer.valueOf(s.charAt(n) & 65535)));
        }
        return sb.toString();
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mLog.info("enableWifiConnectivityManager uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        this.mClientModeImpl.enableWifiConnectivityManager(enabled);
    }

    public byte[] retrieveBackupData() {
        enforceNetworkSettingsPermission();
        this.mLog.info("retrieveBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel == null) {
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return null;
        }
        Slog.d(TAG, "Retrieving backup data");
        byte[] backupData = this.mWifiBackupRestore.retrieveBackupDataFromConfigurations(this.mClientModeImpl.syncGetPrivilegedConfiguredNetwork(this.mClientModeImplChannel));
        Slog.d(TAG, "Retrieved backup data");
        return backupData;
    }

    private void restoreNetworks(List<WifiConfiguration> configurations) {
        if (configurations == null) {
            Slog.e(TAG, "Backup data parse failed");
            return;
        }
        for (WifiConfiguration configuration : configurations) {
            int networkId = this.mClientModeImpl.syncAddOrUpdateNetwork(this.mClientModeImplChannel, configuration);
            if (networkId == -1) {
                Slog.e(TAG, "Restore network failed: " + StringUtilEx.safeDisplaySsid(configuration.getPrintableSsid()));
            } else {
                this.mClientModeImpl.syncEnableNetwork(this.mClientModeImplChannel, networkId, false);
            }
        }
    }

    public void restoreBackupData(byte[] data) {
        enforceNetworkSettingsPermission();
        this.mLog.info("restoreBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel == null) {
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromBackupData(data));
        Slog.d(TAG, "Restored backup data");
    }

    public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        enforceNetworkSettingsPermission();
        this.mLog.trace("restoreSupplicantBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel == null) {
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring supplicant backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromSupplicantBackupData(supplicantData, ipConfigData));
        Slog.d(TAG, "Restored supplicant backup data");
    }

    public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider must not be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        } else if (!isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            throw new SecurityException("WifiService: Permission denied");
        } else if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            int uid = Binder.getCallingUid();
            this.mLog.trace("startSubscriptionProvisioning uid=%").c((long) uid).flush();
            if (this.mClientModeImpl.syncStartSubscriptionProvisioning(uid, provider, callback, this.mClientModeImplChannel)) {
                this.mLog.trace("Subscription provisioning started with %").c(provider.toString()).flush();
            }
        } else {
            throw new UnsupportedOperationException("Passpoint not enabled");
        }
    }

    public void registerTrafficStateCallback(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerTrafficStateCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$G7Lgrjy_hj09HJAD1Vof8K6KVJ8 */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ ITrafficStateCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$registerTrafficStateCallback$14$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerTrafficStateCallback$14$WifiServiceImpl(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) {
        this.mWifiTrafficPoller.addCallback(binder, callback, callbackIdentifier);
    }

    public void unregisterTrafficStateCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterTrafficStateCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$BLcmpC81UVLB1Yg68nmAPcNyJf0 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$unregisterTrafficStateCallback$15$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterTrafficStateCallback$15$WifiServiceImpl(int callbackIdentifier) {
        this.mWifiTrafficPoller.removeCallback(callbackIdentifier);
    }

    private boolean is5GhzSupported() {
        return (getSupportedFeaturesInternal() & 2) == 2;
    }

    private long getSupportedFeaturesInternal() {
        AsyncChannel channel = this.mClientModeImplChannel;
        if (channel != null) {
            return this.mClientModeImpl.syncGetSupportedFeatures(channel);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return 0;
    }

    private static boolean hasAutomotiveFeature(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
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
    public void notifyWifiConfigChanged(WifiConfiguration newConfig, String packageName, boolean isBlacklistChanged) {
    }

    public void registerNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerNetworkRequestMatchCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$u4PaiRbvTWzWX_QMNKohBEllfw */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ INetworkRequestMatchCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$registerNetworkRequestMatchCallback$16$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerNetworkRequestMatchCallback$16$WifiServiceImpl(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        this.mClientModeImpl.addNetworkRequestMatchCallback(binder, callback, callbackIdentifier);
    }

    public void unregisterNetworkRequestMatchCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterNetworkRequestMatchCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$GPF9nVXP6nkr2GivtY69ZoF0uX8 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$unregisterNetworkRequestMatchCallback$17$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterNetworkRequestMatchCallback$17$WifiServiceImpl(int callbackIdentifier) {
        this.mClientModeImpl.removeNetworkRequestMatchCallback(callbackIdentifier);
    }

    /* access modifiers changed from: protected */
    public void sendFailedScanDirectionalBroadcast(String packageName) {
    }

    /* access modifiers changed from: protected */
    public List<ScanResult> getFilterScanResults(String callingPackage, int uid, List<ScanResult> scanResults) {
        return scanResults;
    }

    /* access modifiers changed from: protected */
    public boolean isScreenOff(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiDualBandSupport() {
        return false;
    }

    /* access modifiers changed from: protected */
    public String getAppName(int pID) {
        return null;
    }

    /* access modifiers changed from: protected */
    public String getPackageName(int pID) {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiScanRequestRefused(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAppInFactoryMacWhiteList(int uid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public Bundle parseZcaller(String caller) {
        return Bundle.EMPTY;
    }

    public int addNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions, String callingPackageName) {
        if (this.mHwWifiCHRService != null) {
            Log.d(TAG, "Upload apk action event: add network suggestion");
            this.mHwWifiCHRService.updateApkChangewWifiStatus(20, callingPackageName);
        }
        if (enforceChangePermission(callingPackageName) != 0) {
            return 2;
        }
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("addNetworkSuggestions uid=%").c((long) Binder.getCallingUid()).flush();
        }
        int callingUid = Binder.getCallingUid();
        GeneralUtil.Mutable<Integer> success = new GeneralUtil.Mutable<>();
        if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(success, networkSuggestions, callingUid, callingPackageName) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$YEQ9FROSaBoIYzw6C_KdC2KmHcU */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ List f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ String f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$addNetworkSuggestions$18$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, 4000)) {
            Log.e(TAG, "Failed to post runnable to add network suggestions");
            return 1;
        }
        if (success.value.intValue() != 0) {
            Log.e(TAG, "Failed to add network suggestions");
        }
        return success.value.intValue();
    }

    public /* synthetic */ void lambda$addNetworkSuggestions$18$WifiServiceImpl(GeneralUtil.Mutable success, List networkSuggestions, int callingUid, String callingPackageName) {
        success.value = (E) Integer.valueOf(this.mWifiNetworkSuggestionsManager.add(networkSuggestions, callingUid, callingPackageName));
    }

    public int removeNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions, String callingPackageName) {
        if (this.mHwWifiCHRService != null) {
            Log.d(TAG, "Upload apk action event: remove network suggestion");
            this.mHwWifiCHRService.updateApkChangewWifiStatus(21, callingPackageName);
        }
        if (enforceChangePermission(callingPackageName) != 0) {
            return 2;
        }
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("removeNetworkSuggestions uid=%").c((long) Binder.getCallingUid()).flush();
        }
        GeneralUtil.Mutable<Integer> success = new GeneralUtil.Mutable<>();
        if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(success, networkSuggestions, callingPackageName) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$AAtlm_QQLUzsZyrWDRnRMmbhfE */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ List f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$removeNetworkSuggestions$19$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
            }
        }, 4000)) {
            Log.e(TAG, "Failed to post runnable to remove network suggestions");
            return 1;
        }
        if (success.value.intValue() != 0) {
            Log.e(TAG, "Failed to remove network suggestions");
        }
        return success.value.intValue();
    }

    public /* synthetic */ void lambda$removeNetworkSuggestions$19$WifiServiceImpl(GeneralUtil.Mutable success, List networkSuggestions, String callingPackageName) {
        success.value = (E) Integer.valueOf(this.mWifiNetworkSuggestionsManager.remove(networkSuggestions, callingPackageName));
    }

    public String[] getFactoryMacAddresses() {
        int uid = Binder.getCallingUid();
        if (isAppInFactoryMacWhiteList(uid) || this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
            List<String> result = new ArrayList<>();
            if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(result) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$CTD6DUZ3qlxg4GzWq_v8hR5vMs */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$getFactoryMacAddresses$20$WifiServiceImpl(this.f$1);
                }
            }, 4000) || result.isEmpty()) {
                return null;
            }
            return (String[]) result.stream().toArray($$Lambda$WifiServiceImpl$wFrgjIV2cvbqN6U0aJyMNiFQIXU.INSTANCE);
        }
        throw new SecurityException("App not allowed to get Wi-Fi factory MAC address (uid = " + uid + ")");
    }

    public /* synthetic */ void lambda$getFactoryMacAddresses$20$WifiServiceImpl(List result) {
        String mac = this.mClientModeImpl.getFactoryMacAddress();
        if (mac != null) {
            result.add(mac);
        }
    }

    static /* synthetic */ String[] lambda$getFactoryMacAddresses$21(int x$0) {
        return new String[x$0];
    }

    public void setDeviceMobilityState(int state) {
        this.mContext.enforceCallingPermission("android.permission.WIFI_SET_DEVICE_MOBILITY_STATE", TAG);
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("setDeviceMobilityState uid=% state=%").c((long) Binder.getCallingUid()).c((long) state).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(state) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$WmSxfiPNcksp99qbh5Li2vNYM */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$setDeviceMobilityState$22$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setDeviceMobilityState$22$WifiServiceImpl(int state) {
        this.mClientModeImpl.setDeviceMobilityState(state);
    }

    public int getMockableCallingUid() {
        return getCallingUid();
    }

    public void startDppAsConfiguratorInitiator(IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (TextUtils.isEmpty(enrolleeUri)) {
            throw new IllegalArgumentException("Enrollee URI must not be null or empty");
        } else if (selectedNetworkId < 0) {
            throw new IllegalArgumentException("Selected network ID invalid");
        } else if (callback != null) {
            int uid = getMockableCallingUid();
            if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
                this.mDppManager.mHandler.post(new Runnable(uid, binder, enrolleeUri, selectedNetworkId, netRole, callback) {
                    /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$_Snu48Ar44I9Q1BTvLLkVAfnwMs */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ IBinder f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ int f$4;
                    private final /* synthetic */ int f$5;
                    private final /* synthetic */ IDppCallback f$6;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                        this.f$6 = r7;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        WifiServiceImpl.this.lambda$startDppAsConfiguratorInitiator$23$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                    }
                });
                return;
            }
            throw new SecurityException("WifiService: Permission denied");
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$startDppAsConfiguratorInitiator$23$WifiServiceImpl(int uid, IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) {
        this.mDppManager.startDppAsConfiguratorInitiator(uid, binder, enrolleeUri, selectedNetworkId, netRole, callback);
    }

    public void startDppAsEnrolleeInitiator(IBinder binder, String configuratorUri, IDppCallback callback) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (TextUtils.isEmpty(configuratorUri)) {
            throw new IllegalArgumentException("Enrollee URI must not be null or empty");
        } else if (callback != null) {
            int uid = getMockableCallingUid();
            if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
                this.mDppManager.mHandler.post(new Runnable(uid, binder, configuratorUri, callback) {
                    /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$O7AqPuxvwd9KGlYqiCkEVo2a5q4 */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ IBinder f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ IDppCallback f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        WifiServiceImpl.this.lambda$startDppAsEnrolleeInitiator$24$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
                return;
            }
            throw new SecurityException("WifiService: Permission denied");
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$startDppAsEnrolleeInitiator$24$WifiServiceImpl(int uid, IBinder binder, String configuratorUri, IDppCallback callback) {
        this.mDppManager.startDppAsEnrolleeInitiator(uid, binder, configuratorUri, callback);
    }

    public void stopDppSession() throws RemoteException {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mDppManager.mHandler.post(new Runnable(getMockableCallingUid()) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$Cxlyf61D7KGv7kLWvJLFGQfG1yA */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$stopDppSession$25$WifiServiceImpl(this.f$1);
                }
            });
            return;
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public /* synthetic */ void lambda$stopDppSession$25$WifiServiceImpl(int uid) {
        this.mDppManager.stopDppSession(uid);
    }

    /* access modifiers changed from: protected */
    public void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
    }

    public void addOnWifiUsabilityStatsListener(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (listener != null) {
            this.mContext.enforceCallingPermission("android.permission.WIFI_UPDATE_USABILITY_STATS_SCORE", TAG);
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("addOnWifiUsabilityStatsListener uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, listener, listenerIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$LX0ZWIg16RMQ3M9DTn5PJgKWig */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ IOnWifiUsabilityStatsListener f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiServiceImpl.this.lambda$addOnWifiUsabilityStatsListener$26$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Listener must not be null");
        }
    }

    public /* synthetic */ void lambda$addOnWifiUsabilityStatsListener$26$WifiServiceImpl(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) {
        this.mWifiMetrics.addOnWifiUsabilityListener(binder, listener, listenerIdentifier);
    }

    public void removeOnWifiUsabilityStatsListener(int listenerIdentifier) {
        this.mContext.enforceCallingPermission("android.permission.WIFI_UPDATE_USABILITY_STATS_SCORE", TAG);
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("removeOnWifiUsabilityStatsListener uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(listenerIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$Y8FNcV1D_NSXtSl4Ks8EoHtgs */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$removeOnWifiUsabilityStatsListener$27$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$removeOnWifiUsabilityStatsListener$27$WifiServiceImpl(int listenerIdentifier) {
        this.mWifiMetrics.removeOnWifiUsabilityListener(listenerIdentifier);
    }

    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        this.mContext.enforceCallingPermission("android.permission.WIFI_UPDATE_USABILITY_STATS_SCORE", TAG);
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("updateWifiUsabilityScore uid=% seqNum=% score=% predictionHorizonSec=%").c((long) Binder.getCallingUid()).c((long) seqNum).c((long) score).c((long) predictionHorizonSec).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(seqNum, score, predictionHorizonSec) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$MipkYDf3yFukRBF4wuUsmDu1quQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                WifiServiceImpl.this.lambda$updateWifiUsabilityScore$28$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$updateWifiUsabilityScore$28$WifiServiceImpl(int seqNum, int score, int predictionHorizonSec) {
        this.mClientModeImpl.updateWifiUsabilityScore(seqNum, score, predictionHorizonSec);
    }

    public int addOrUpdateWifiDeviceConfig(WifiDeviceConfig config, String packageName) {
        if (config != null) {
            return addOrUpdateNetwork(config.toWifiConfig(this.mContext), packageName);
        }
        Slog.e(TAG, "bad network configuration");
        return -1;
    }

    public ParceledListSlice<WifiDeviceConfig> getWifiDeviceConfigs(String packageName) {
        ParceledListSlice<WifiConfiguration> parceledList = getConfiguredNetworks(packageName);
        if (parceledList == null) {
            return new ParceledListSlice<>(new ArrayList());
        }
        List<WifiConfiguration> configs = parceledList.getList();
        List<WifiDeviceConfig> deviceConfigs = new ArrayList<>();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                deviceConfigs.add(WifiDeviceConfig.fromWifiConfiguration(config));
            }
        }
        return new ParceledListSlice<>(deviceConfigs);
    }

    public List<WifiScanInfo> getScanInfoList(String packageName) {
        List<ScanResult> scanResults = getScanResults(packageName);
        if (scanResults == null) {
            return new ArrayList();
        }
        List<WifiScanInfo> scanInfoList = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            scanInfoList.add(WifiScanInfo.fromScanResult(scanResult));
        }
        return scanInfoList;
    }

    public boolean enableHotspot(boolean enable, HotspotConfig hotspotConfig) {
        WifiConfiguration apConfig = null;
        if (hotspotConfig != null) {
            apConfig = hotspotConfig.toWifiConfig();
            int maxConn = hotspotConfig.getMaxConn();
            if (maxConn >= 8) {
                maxConn = 8;
            }
            if (maxConn != Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8)) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", maxConn);
            }
        }
        if (enable) {
            return startSoftAp(apConfig);
        }
        return stopSoftAp();
    }

    public boolean setHotspotConfig(HotspotConfig hotspotConfig, String packageName) {
        WifiConfiguration apConfig = null;
        if (hotspotConfig != null) {
            apConfig = hotspotConfig.toWifiConfig();
            int maxConn = hotspotConfig.getMaxConn();
            if (maxConn >= 8) {
                maxConn = 8;
            }
            if (maxConn != Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8)) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", maxConn);
            }
        }
        return setWifiApConfiguration(apConfig, packageName);
    }

    public HotspotConfig getHotspotConfig() {
        HotspotConfig hotspotConfig = HotspotConfig.fromWifiConfiguration(getWifiApConfiguration());
        hotspotConfig.setMaxConn(Settings.Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8));
        return hotspotConfig;
    }

    public WifiLinkedInfo getLinkedInfo(String packageName) {
        return WifiLinkedInfo.fromWifiInfo(getConnectionInfo(packageName));
    }

    /* access modifiers changed from: protected */
    public void handleAirplaneNotSensitiveWifi() {
    }
}
