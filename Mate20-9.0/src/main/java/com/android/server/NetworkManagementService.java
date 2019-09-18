package com.android.server;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetd;
import android.net.INetworkManagementEventObserver;
import android.net.ITetheringStatsProvider;
import android.net.InterfaceConfiguration;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkStats;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.util.NetdService;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkActivityListener;
import android.os.INetworkManagementService;
import android.os.PersistableBundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.HexDump;
import com.android.internal.util.Preconditions;
import com.android.server.BatteryService;
import com.android.server.HwServiceFactory;
import com.android.server.NativeDaemonConnector;
import com.android.server.Watchdog;
import com.android.server.os.HwBootFail;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.voiceinteraction.DatabaseHelper;
import com.google.android.collect.Maps;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

public class NetworkManagementService extends INetworkManagementService.Stub implements Watchdog.Monitor {
    static final int DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO = 1;
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    public static final String LIMIT_GLOBAL_ALERT = "globalAlert";
    private static final int MAX_UID_RANGES_PER_COMMAND = 10;
    static final String NETD_SERVICE_NAME = "netd";
    private static final String NETD_TAG = "NetdConnector";
    public static final String PERMISSION_NETWORK = "NETWORK";
    public static final String PERMISSION_SYSTEM = "SYSTEM";
    static final String SOFT_AP_COMMAND = "softap";
    static final String SOFT_AP_COMMAND_SUCCESS = "Ok";
    private static final String TAG = "NetworkManagement";
    /* access modifiers changed from: private */
    public static HwServiceFactory.IHwNetworkManagermentService mNetworkMS;
    @GuardedBy("mQuotaLock")
    private HashMap<String, Long> mActiveAlerts;
    private HashMap<String, IdleTimerParams> mActiveIdleTimers;
    @GuardedBy("mQuotaLock")
    private HashMap<String, Long> mActiveQuotas;
    private volatile boolean mBandwidthControlEnabled;
    private IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public CountDownLatch mConnectedSignal;
    private final NativeDaemonConnector mConnector;
    private final Context mContext;
    private final Handler mDaemonHandler;
    /* access modifiers changed from: private */
    @GuardedBy("mQuotaLock")
    public volatile boolean mDataSaverMode;
    /* access modifiers changed from: private */
    public final Handler mFgHandler;
    @GuardedBy("mRulesLock")
    final SparseBooleanArray mFirewallChainStates;
    private volatile boolean mFirewallEnabled;
    private final Object mIdleTimerLock;
    private int mLastPowerStateFromRadio;
    private int mLastPowerStateFromWifi;
    private int mLinkedStaCount;
    private boolean mMobileActivityFromRadio;
    protected INetd mNetdService;
    private boolean mNetworkActive;
    private final RemoteCallbackList<INetworkActivityListener> mNetworkActivityListeners;
    private final RemoteCallbackList<INetworkManagementEventObserver> mObservers;
    private final Object mQuotaLock;
    /* access modifiers changed from: private */
    public final Object mRulesLock;
    private final SystemServices mServices;
    private final NetworkStatsFactory mStatsFactory;
    private volatile boolean mStrictEnabled;
    @GuardedBy("mTetheringStatsProviders")
    private final HashMap<ITetheringStatsProvider, String> mTetheringStatsProviders;
    private final Thread mThread;
    /* access modifiers changed from: private */
    @GuardedBy("mRulesLock")
    public SparseBooleanArray mUidAllowOnMetered;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidCleartextPolicy;
    @GuardedBy("mRulesLock")
    private SparseIntArray mUidFirewallDozableRules;
    @GuardedBy("mRulesLock")
    private SparseIntArray mUidFirewallPowerSaveRules;
    @GuardedBy("mRulesLock")
    private SparseIntArray mUidFirewallRules;
    @GuardedBy("mRulesLock")
    private SparseIntArray mUidFirewallStandbyRules;
    /* access modifiers changed from: private */
    @GuardedBy("mRulesLock")
    public SparseBooleanArray mUidRejectOnMetered;
    private HwNativeDaemonConnector mhwNativeDaemonConnector;

    private static class IdleTimerParams {
        public int networkCount = 1;
        public final int timeout;
        public final int type;

        IdleTimerParams(int timeout2, int type2) {
            this.timeout = timeout2;
            this.type = type2;
        }
    }

    @VisibleForTesting
    class Injector {
        Injector() {
        }

        /* access modifiers changed from: package-private */
        public void setDataSaverMode(boolean dataSaverMode) {
            boolean unused = NetworkManagementService.this.mDataSaverMode = dataSaverMode;
        }

        /* access modifiers changed from: package-private */
        public void setFirewallChainState(int chain, boolean state) {
            NetworkManagementService.this.setFirewallChainState(chain, state);
        }

        /* access modifiers changed from: package-private */
        public void setFirewallRule(int chain, int uid, int rule) {
            synchronized (NetworkManagementService.this.mRulesLock) {
                NetworkManagementService.this.getUidFirewallRulesLR(chain).put(uid, rule);
            }
        }

        /* access modifiers changed from: package-private */
        public void setUidOnMeteredNetworkList(boolean blacklist, int uid, boolean enable) {
            synchronized (NetworkManagementService.this.mRulesLock) {
                if (blacklist) {
                    try {
                        NetworkManagementService.this.mUidRejectOnMetered.put(uid, enable);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    NetworkManagementService.this.mUidAllowOnMetered.put(uid, enable);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            synchronized (NetworkManagementService.this.mRulesLock) {
                setDataSaverMode(false);
                for (int chain : new int[]{1, 2, 3}) {
                    setFirewallChainState(chain, false);
                    NetworkManagementService.this.getUidFirewallRulesLR(chain).clear();
                }
                NetworkManagementService.this.mUidAllowOnMetered.clear();
                NetworkManagementService.this.mUidRejectOnMetered.clear();
            }
        }
    }

    @VisibleForTesting
    class LocalService extends NetworkManagementInternal {
        LocalService() {
        }

        public boolean isNetworkRestrictedForUid(int uid) {
            return NetworkManagementService.this.isNetworkRestrictedInternal(uid);
        }
    }

    private class NetdCallbackReceiver implements INativeDaemonConnectorCallbacks {
        private NetdCallbackReceiver() {
        }

        public void onDaemonConnected() {
            Slog.i(NetworkManagementService.TAG, "onDaemonConnected()");
            if (NetworkManagementService.this.mConnectedSignal != null) {
                NetworkManagementService.this.mConnectedSignal.countDown();
                CountDownLatch unused = NetworkManagementService.this.mConnectedSignal = null;
                return;
            }
            NetworkManagementService.this.mFgHandler.post(new Runnable() {
                public void run() {
                    NetworkManagementService.this.connectNativeNetdService();
                    NetworkManagementService.this.prepareNativeDaemon();
                }
            });
        }

        public boolean onCheckHoldWakeLock(int code) {
            return code == 613;
        }

        /* JADX WARNING: Removed duplicated region for block: B:79:0x0180  */
        public boolean onEvent(int code, String raw, String[] cooked) {
            boolean valid;
            int i = code;
            String str = raw;
            String[] strArr = cooked;
            String errorMessage = String.format("Invalid event from daemon (%s)", new Object[]{str});
            if (i != 651) {
                if (i == 810) {
                    if (NetworkManagementService.mNetworkMS != null) {
                        NetworkManagementService.mNetworkMS.sendApkDownloadUrlBroadcast(strArr, str);
                    }
                    return true;
                } else if (i != 901) {
                    int i2 = 4;
                    switch (i) {
                        case 600:
                            if (strArr.length < 4 || !strArr[1].equals("Iface")) {
                                throw new IllegalStateException(errorMessage);
                            } else if (strArr[2].equals("added")) {
                                NetworkManagementService.this.notifyInterfaceAdded(strArr[3]);
                                return true;
                            } else if (strArr[2].equals("removed")) {
                                NetworkManagementService.this.notifyInterfaceRemoved(strArr[3]);
                                return true;
                            } else if (strArr[2].equals("changed") && strArr.length == 5) {
                                NetworkManagementService.this.notifyInterfaceStatusChanged(strArr[3], strArr[4].equals("up"));
                                return true;
                            } else if (!strArr[2].equals("linkstate") || strArr.length != 5) {
                                throw new IllegalStateException(errorMessage);
                            } else {
                                NetworkManagementService.this.notifyInterfaceLinkStateChanged(strArr[3], strArr[4].equals("up"));
                                return true;
                            }
                        case NetdResponseCode.BandwidthControl /*601*/:
                            if (strArr.length < 5 || !strArr[1].equals("limit")) {
                                throw new IllegalStateException(errorMessage);
                            } else if (strArr[2].equals("alert")) {
                                NetworkManagementService.this.notifyLimitReached(strArr[3], strArr[4]);
                                return true;
                            } else {
                                throw new IllegalStateException(errorMessage);
                            }
                        default:
                            switch (i) {
                                case NetdResponseCode.InterfaceClassActivity /*613*/:
                                    if (strArr.length < 4 || !strArr[1].equals("IfaceClass")) {
                                        throw new IllegalStateException(errorMessage);
                                    }
                                    long timestampNanos = 0;
                                    int processUid = -1;
                                    if (strArr.length >= 5) {
                                        try {
                                            timestampNanos = Long.parseLong(strArr[4]);
                                            if (strArr.length == 6) {
                                                processUid = Integer.parseInt(strArr[5]);
                                            }
                                        } catch (NumberFormatException e) {
                                        }
                                    } else {
                                        timestampNanos = SystemClock.elapsedRealtimeNanos();
                                    }
                                    NetworkManagementService.this.notifyInterfaceClassActivity(Integer.parseInt(strArr[3]), strArr[2].equals("active") ? 3 : 1, timestampNanos, processUid, false);
                                    return true;
                                case NetdResponseCode.InterfaceAddressChange /*614*/:
                                    if (strArr.length < 7 || !strArr[1].equals("Address")) {
                                        throw new IllegalStateException(errorMessage);
                                    }
                                    String iface = strArr[4];
                                    try {
                                        LinkAddress address = new LinkAddress(strArr[3], Integer.parseInt(strArr[5]), Integer.parseInt(strArr[6]));
                                        if (strArr[2].equals("updated")) {
                                            NetworkManagementService.this.notifyAddressUpdated(iface, address);
                                        } else {
                                            NetworkManagementService.this.notifyAddressRemoved(iface, address);
                                        }
                                        return true;
                                    } catch (NumberFormatException e2) {
                                        throw new IllegalStateException(errorMessage, e2);
                                    } catch (IllegalArgumentException e3) {
                                        throw new IllegalStateException(errorMessage, e3);
                                    }
                                case NetdResponseCode.InterfaceDnsServerInfo /*615*/:
                                    if (strArr.length == 6 && strArr[1].equals("DnsInfo") && strArr[2].equals("servers")) {
                                        try {
                                            NetworkManagementService.this.notifyInterfaceDnsServerInfo(strArr[3], Long.parseLong(strArr[4]), strArr[5].split(","));
                                        } catch (NumberFormatException e4) {
                                            throw new IllegalStateException(errorMessage);
                                        }
                                    }
                                    return true;
                                case NetdResponseCode.RouteChange /*616*/:
                                    if (!strArr[1].equals("Route") || strArr.length < 6) {
                                        throw new IllegalStateException(errorMessage);
                                    }
                                    boolean valid2 = true;
                                    String dev = null;
                                    String via = null;
                                    while (true) {
                                        int i3 = i2;
                                        if (i3 + 1 < strArr.length && valid2) {
                                            if (strArr[i3].equals("dev")) {
                                                if (dev == null) {
                                                    dev = strArr[i3 + 1];
                                                    i2 = i3 + 2;
                                                } else {
                                                    valid = false;
                                                }
                                            } else if (!strArr[i3].equals("via")) {
                                                valid = false;
                                            } else if (via == null) {
                                                via = strArr[i3 + 1];
                                                i2 = i3 + 2;
                                            } else {
                                                valid = false;
                                            }
                                            valid2 = valid;
                                            i2 = i3 + 2;
                                        } else if (valid2) {
                                            InetAddress gateway = null;
                                            if (via != null) {
                                                try {
                                                    gateway = InetAddress.parseNumericAddress(via);
                                                } catch (IllegalArgumentException e5) {
                                                }
                                            }
                                            NetworkManagementService.this.notifyRouteChange(strArr[2], new RouteInfo(new IpPrefix(strArr[3]), gateway, dev));
                                            return true;
                                        }
                                    }
                                    if (valid2) {
                                    }
                                    throw new IllegalStateException(errorMessage);
                                case NetdResponseCode.StrictCleartext /*617*/:
                                    try {
                                        ActivityManager.getService().notifyCleartextNetwork(Integer.parseInt(strArr[1]), HexDump.hexStringToByteArray(strArr[2]));
                                        break;
                                    } catch (RemoteException e6) {
                                        break;
                                    }
                                default:
                                    switch (i) {
                                        case NetdResponseCode.DataSpeedSlowDetected /*660*/:
                                            if (NetworkManagementService.mNetworkMS != null) {
                                                NetworkManagementService.mNetworkMS.sendDataSpeedSlowMessage(strArr, str);
                                            }
                                            return true;
                                        case NetdResponseCode.WebStatInfoReport /*661*/:
                                            if (NetworkManagementService.mNetworkMS != null) {
                                                NetworkManagementService.mNetworkMS.sendWebStatMessage(strArr, str);
                                            }
                                            return true;
                                        case NetdResponseCode.DSCPChangeInfoReport /*662*/:
                                            if (NetworkManagementService.mNetworkMS != null) {
                                                NetworkManagementService.mNetworkMS.sendDSCPChangeMessage(strArr, str);
                                            }
                                            return true;
                                        default:
                                            switch (i) {
                                                case NetdResponseCode.NbVodReport /*670*/:
                                                    Slog.e(NetworkManagementService.TAG, "NetdCallbackReceiver.onEvent NbVodReport");
                                                    if (strArr.length < 12 || !strArr[1].equals("vod")) {
                                                        throw new IllegalStateException(errorMessage);
                                                    }
                                                    try {
                                                        if (NetworkManagementService.mNetworkMS != null) {
                                                            NetworkManagementService.mNetworkMS.reportVodParams(Integer.parseInt(strArr[2]), Integer.parseInt(strArr[3]), Integer.parseInt(strArr[4]), Integer.parseInt(strArr[5]), Integer.parseInt(strArr[6]), Integer.parseInt(strArr[7]), Integer.parseInt(strArr[8]), Integer.parseInt(strArr[9]), Integer.parseInt(strArr[10]), Integer.parseInt(strArr[11]));
                                                        }
                                                        return true;
                                                    } catch (NumberFormatException e7) {
                                                        throw new IllegalStateException(errorMessage, e7);
                                                    }
                                                case NetdResponseCode.NbKsiReport /*671*/:
                                                    Slog.e(NetworkManagementService.TAG, "NetdCallbackReceiver.onEvent NbKsiReport");
                                                    if (strArr.length < 6 || !strArr[1].equals("ksi")) {
                                                        throw new IllegalStateException(errorMessage);
                                                    }
                                                    try {
                                                        if (NetworkManagementService.mNetworkMS != null) {
                                                            NetworkManagementService.mNetworkMS.reportKsiParams(Integer.parseInt(strArr[2]), Integer.parseInt(strArr[3]), Integer.parseInt(strArr[4]), Integer.parseInt(strArr[5]));
                                                        }
                                                        return true;
                                                    } catch (NumberFormatException e8) {
                                                        throw new IllegalStateException(errorMessage, e8);
                                                    }
                                            }
                                    }
                            }
                            return false;
                    }
                }
            }
            if (NetworkManagementService.mNetworkMS != null) {
                NetworkManagementService.mNetworkMS.handleApLinkedStaListChange(str, strArr);
            }
            return true;
        }
    }

    static class NetdResponseCode {
        public static final int ApLinkedStaListChangeHISI = 651;
        public static final int ApLinkedStaListChangeQCOM = 901;
        public static final int ApkDownloadUrlDetected = 810;
        public static final int BandwidthControl = 601;
        public static final int ClatdStatusResult = 223;
        public static final int DSCPChangeInfoReport = 662;
        public static final int DataSpeedSlowDetected = 660;
        public static final int DnsProxyQueryResult = 222;
        public static final int InterfaceAddressChange = 614;
        public static final int InterfaceChange = 600;
        public static final int InterfaceClassActivity = 613;
        public static final int InterfaceDnsServerInfo = 615;
        public static final int InterfaceGetCfgResult = 213;
        public static final int InterfaceListResult = 110;
        public static final int InterfaceRxCounterResult = 216;
        public static final int InterfaceTxCounterResult = 217;
        public static final int IpFwdStatusResult = 211;
        public static final int NbKsiReport = 671;
        public static final int NbVodReport = 670;
        public static final int QuotaCounterResult = 220;
        public static final int RouteChange = 616;
        public static final int SoftapStatusResult = 214;
        public static final int StrictCleartext = 617;
        public static final int TetherDnsFwdTgtListResult = 112;
        public static final int TetherInterfaceListResult = 111;
        public static final int TetherStatusResult = 210;
        public static final int TetheringStatsListResult = 114;
        public static final int TetheringStatsResult = 221;
        public static final int TtyListResult = 113;
        public static final int WebStatInfoReport = 661;

        NetdResponseCode() {
        }
    }

    private class NetdTetheringStatsProvider extends ITetheringStatsProvider.Stub {
        private NetdTetheringStatsProvider() {
        }

        public NetworkStats getTetherStats(int how) {
            if (how != 1) {
                return new NetworkStats(SystemClock.elapsedRealtime(), 0);
            }
            try {
                PersistableBundle bundle = NetworkManagementService.this.mNetdService.tetherGetStats();
                NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), bundle.size());
                NetworkStats.Entry entry = new NetworkStats.Entry();
                for (String iface : bundle.keySet()) {
                    long[] statsArray = bundle.getLongArray(iface);
                    try {
                        entry.iface = iface;
                        entry.uid = -5;
                        entry.set = 0;
                        entry.tag = 0;
                        entry.rxBytes = statsArray[0];
                        entry.rxPackets = statsArray[1];
                        entry.txBytes = statsArray[2];
                        entry.txPackets = statsArray[3];
                        stats.combineValues(entry);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalStateException("invalid tethering stats for " + iface, e);
                    }
                }
                return stats;
            } catch (RemoteException | ServiceSpecificException e2) {
                throw new IllegalStateException("problem parsing tethering stats: ", e2);
            }
        }

        public void setInterfaceQuota(String iface, long quotaBytes) {
        }
    }

    @FunctionalInterface
    private interface NetworkManagementEventCallback {
        void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) throws RemoteException;
    }

    static class SystemServices {
        SystemServices() {
        }

        public IBinder getService(String name) {
            return ServiceManager.getService(name);
        }

        public void registerLocalService(NetworkManagementInternal nmi) {
            LocalServices.addService(NetworkManagementInternal.class, nmi);
        }

        public INetd getNetd() {
            return NetdService.get();
        }
    }

    protected NetworkManagementService(Context context, String socket, SystemServices services) {
        this.mConnectedSignal = new CountDownLatch(1);
        this.mObservers = new RemoteCallbackList<>();
        this.mStatsFactory = new NetworkStatsFactory();
        this.mTetheringStatsProviders = Maps.newHashMap();
        this.mQuotaLock = new Object();
        this.mRulesLock = new Object();
        this.mActiveQuotas = Maps.newHashMap();
        this.mActiveAlerts = Maps.newHashMap();
        this.mUidRejectOnMetered = new SparseBooleanArray();
        this.mUidAllowOnMetered = new SparseBooleanArray();
        this.mUidCleartextPolicy = new SparseIntArray();
        this.mUidFirewallRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mIdleTimerLock = new Object();
        this.mActiveIdleTimers = Maps.newHashMap();
        this.mLinkedStaCount = 0;
        this.mMobileActivityFromRadio = false;
        this.mLastPowerStateFromRadio = 1;
        this.mLastPowerStateFromWifi = 1;
        this.mNetworkActivityListeners = new RemoteCallbackList<>();
        this.mContext = context;
        this.mServices = services;
        this.mFgHandler = new Handler(FgThread.get().getLooper());
        this.mhwNativeDaemonConnector = HwServiceFactory.getHwNativeDaemonConnector();
        this.mhwNativeDaemonConnector.setContext(this.mContext);
        NativeDaemonConnector nativeDaemonConnector = new NativeDaemonConnector(new NetdCallbackReceiver(), socket, 10, NETD_TAG, 160, null, FgThread.get().getLooper());
        this.mConnector = nativeDaemonConnector;
        this.mConnector.setDebug(true);
        this.mThread = new Thread(this.mConnector, NETD_TAG);
        this.mDaemonHandler = new Handler(FgThread.get().getLooper());
        Watchdog.getInstance().addMonitor(this);
        this.mServices.registerLocalService(new LocalService());
        synchronized (this.mTetheringStatsProviders) {
            this.mTetheringStatsProviders.put(new NetdTetheringStatsProvider(), NETD_SERVICE_NAME);
        }
    }

    @VisibleForTesting
    NetworkManagementService() {
        this.mConnectedSignal = new CountDownLatch(1);
        this.mObservers = new RemoteCallbackList<>();
        this.mStatsFactory = new NetworkStatsFactory();
        this.mTetheringStatsProviders = Maps.newHashMap();
        this.mQuotaLock = new Object();
        this.mRulesLock = new Object();
        this.mActiveQuotas = Maps.newHashMap();
        this.mActiveAlerts = Maps.newHashMap();
        this.mUidRejectOnMetered = new SparseBooleanArray();
        this.mUidAllowOnMetered = new SparseBooleanArray();
        this.mUidCleartextPolicy = new SparseIntArray();
        this.mUidFirewallRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mIdleTimerLock = new Object();
        this.mActiveIdleTimers = Maps.newHashMap();
        this.mLinkedStaCount = 0;
        this.mMobileActivityFromRadio = false;
        this.mLastPowerStateFromRadio = 1;
        this.mLastPowerStateFromWifi = 1;
        this.mNetworkActivityListeners = new RemoteCallbackList<>();
        this.mConnector = null;
        this.mContext = null;
        this.mDaemonHandler = null;
        this.mFgHandler = null;
        this.mThread = null;
        this.mServices = null;
    }

    static NetworkManagementService create(Context context, String socket, SystemServices services) throws InterruptedException {
        NetworkManagementService service;
        mNetworkMS = HwServiceFactory.getHwNetworkManagermentService();
        if (mNetworkMS != null) {
            service = mNetworkMS.getInstance(context, socket, services);
            mNetworkMS.setNativeDaemonConnector(service, service.mConnector);
        } else {
            service = new NetworkManagementService(context, socket, services);
        }
        CountDownLatch connectedSignal = service.mConnectedSignal;
        if (DBG) {
            Slog.d(TAG, "Creating NetworkManagementService");
        }
        service.mThread.start();
        if (DBG) {
            Slog.d(TAG, "Awaiting socket connection");
        }
        connectedSignal.await();
        if (DBG) {
            Slog.d(TAG, "Connected");
        }
        if (DBG) {
            Slog.d(TAG, "Connecting native netd service");
        }
        service.connectNativeNetdService();
        if (DBG) {
            Slog.d(TAG, "Connected");
        }
        return service;
    }

    public static NetworkManagementService create(Context context) throws InterruptedException {
        return create(context, NETD_SERVICE_NAME, new SystemServices());
    }

    public void systemReady() {
        if (DBG) {
            long start = System.currentTimeMillis();
            prepareNativeDaemon();
            Slog.d(TAG, "Prepared in " + (System.currentTimeMillis() - start) + "ms");
            return;
        }
        prepareNativeDaemon();
    }

    private IBatteryStats getBatteryStats() {
        synchronized (this) {
            if (this.mBatteryStats != null) {
                IBatteryStats iBatteryStats = this.mBatteryStats;
                return iBatteryStats;
            }
            this.mBatteryStats = IBatteryStats.Stub.asInterface(this.mServices.getService("batterystats"));
            IBatteryStats iBatteryStats2 = this.mBatteryStats;
            return iBatteryStats2;
        }
    }

    public void registerObserver(INetworkManagementEventObserver observer) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NETWORKMANAGEMENT_REGISTEROBSERVER);
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Slog.d(TAG, "registerObserver: pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", observer " + observer);
        this.mObservers.register(observer, Integer.valueOf(Binder.getCallingPid()));
    }

    public void unregisterObserver(INetworkManagementEventObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Slog.d(TAG, "unregisterObserver: pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", observer " + observer);
        this.mObservers.unregister(observer);
    }

    private void invokeForAllObservers(NetworkManagementEventCallback eventCallback) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                eventCallback.sendCallback(this.mObservers.getBroadcastItem(i));
            } catch (RemoteException | RuntimeException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
                throw th;
            }
        }
        this.mObservers.finishBroadcast();
    }

    /* access modifiers changed from: private */
    public void notifyInterfaceStatusChanged(String iface, boolean up) {
        invokeForAllObservers(new NetworkManagementEventCallback(iface, up) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.interfaceStatusChanged(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyInterfaceLinkStateChanged(String iface, boolean up) {
        invokeForAllObservers(new NetworkManagementEventCallback(iface, up) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.interfaceLinkStateChanged(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyInterfaceAdded(String iface) {
        invokeForAllObservers(new NetworkManagementEventCallback(iface) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.interfaceAdded(this.f$0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyInterfaceRemoved(String iface) {
        this.mActiveAlerts.remove(iface);
        this.mActiveQuotas.remove(iface);
        invokeForAllObservers(new NetworkManagementEventCallback(iface) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.interfaceRemoved(this.f$0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyLimitReached(String limitName, String iface) {
        invokeForAllObservers(new NetworkManagementEventCallback(limitName, iface) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.limitReached(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyInterfaceClassActivity(int type, int powerState, long tsNanos, int uid, boolean fromRadio) {
        boolean isMobile = ConnectivityManager.isNetworkTypeMobile(type);
        boolean isActive = true;
        if (isMobile) {
            if (fromRadio) {
                this.mMobileActivityFromRadio = true;
            } else if (this.mMobileActivityFromRadio) {
                powerState = this.mLastPowerStateFromRadio;
            }
            if (this.mLastPowerStateFromRadio != powerState) {
                this.mLastPowerStateFromRadio = powerState;
                try {
                    getBatteryStats().noteMobileRadioPowerState(powerState, tsNanos, uid);
                } catch (RemoteException e) {
                }
            }
        }
        if (ConnectivityManager.isNetworkTypeWifi(type) && this.mLastPowerStateFromWifi != powerState) {
            this.mLastPowerStateFromWifi = powerState;
            try {
                getBatteryStats().noteWifiRadioPowerState(powerState, tsNanos, uid);
            } catch (RemoteException e2) {
            }
        }
        if (!(powerState == 2 || powerState == 3)) {
            isActive = false;
        }
        if (!isMobile || fromRadio || !this.mMobileActivityFromRadio) {
            invokeForAllObservers(new NetworkManagementEventCallback(type, isActive, tsNanos) {
                private final /* synthetic */ int f$0;
                private final /* synthetic */ boolean f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                    iNetworkManagementEventObserver.interfaceClassDataActivityChanged(Integer.toString(this.f$0), this.f$1, this.f$2);
                }
            });
        }
        boolean report = false;
        synchronized (this.mIdleTimerLock) {
            if (this.mActiveIdleTimers.isEmpty()) {
                isActive = true;
            }
            if (this.mNetworkActive != isActive) {
                this.mNetworkActive = isActive;
                report = isActive;
            }
        }
        if (report) {
            reportNetworkActive();
        }
    }

    public void registerTetheringStatsProvider(ITetheringStatsProvider provider, String name) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
        Preconditions.checkNotNull(provider);
        synchronized (this.mTetheringStatsProviders) {
            this.mTetheringStatsProviders.put(provider, name);
        }
    }

    public void unregisterTetheringStatsProvider(ITetheringStatsProvider provider) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
        synchronized (this.mTetheringStatsProviders) {
            this.mTetheringStatsProviders.remove(provider);
        }
    }

    public void tetherLimitReached(ITetheringStatsProvider provider) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
        synchronized (this.mTetheringStatsProviders) {
            if (this.mTetheringStatsProviders.containsKey(provider)) {
                notifyLimitReached(LIMIT_GLOBAL_ALERT, null);
            }
        }
    }

    private void syncFirewallChainLocked(int chain, String name) {
        SparseIntArray rules;
        synchronized (this.mRulesLock) {
            SparseIntArray uidFirewallRules = getUidFirewallRulesLR(chain);
            rules = uidFirewallRules.clone();
            uidFirewallRules.clear();
        }
        if (rules.size() > 0) {
            if (DBG) {
                Slog.d(TAG, "Pushing " + rules.size() + " active firewall " + name + "UID rules");
            }
            for (int i = 0; i < rules.size(); i++) {
                setFirewallUidRuleLocked(chain, rules.keyAt(i), rules.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public void connectNativeNetdService() {
        this.mNetdService = this.mServices.getNetd();
    }

    /* access modifiers changed from: private */
    public void prepareNativeDaemon() {
        this.mBandwidthControlEnabled = false;
        boolean hasKernelSupport = new File("/proc/net/xt_qtaguid/ctrl").exists();
        synchronized (this.mQuotaLock) {
            if (hasKernelSupport) {
                try {
                    Slog.d(TAG, "enabling bandwidth control");
                    this.mConnector.execute("bandwidth", "enable");
                    this.mBandwidthControlEnabled = true;
                } catch (NativeDaemonConnectorException e) {
                    Log.wtf(TAG, "problem enabling bandwidth controls", e);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                Slog.i(TAG, "not enabling bandwidth control");
            }
            SystemProperties.set("net.qtaguid_enabled", this.mBandwidthControlEnabled ? "1" : "0");
            try {
                this.mConnector.execute("strict", "enable");
                this.mStrictEnabled = true;
            } catch (NativeDaemonConnectorException e2) {
                Log.wtf(TAG, "Failed strict enable", e2);
            }
            setDataSaverModeEnabled(this.mDataSaverMode);
            if (this.mActiveQuotas.size() > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active quota rules");
                }
                HashMap<String, Long> activeQuotas = this.mActiveQuotas;
                this.mActiveQuotas = Maps.newHashMap();
                for (Map.Entry<String, Long> entry : activeQuotas.entrySet()) {
                    setInterfaceQuota(entry.getKey(), entry.getValue().longValue());
                }
            }
            if (this.mActiveAlerts.size() > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active alert rules");
                }
                HashMap<String, Long> activeAlerts = this.mActiveAlerts;
                this.mActiveAlerts = Maps.newHashMap();
                for (Map.Entry<String, Long> entry2 : activeAlerts.entrySet()) {
                    setInterfaceAlert(entry2.getKey(), entry2.getValue().longValue());
                }
            }
            SparseBooleanArray uidRejectOnQuota = null;
            SparseBooleanArray uidAcceptOnQuota = null;
            synchronized (this.mRulesLock) {
                if (this.mUidRejectOnMetered.size() > 0) {
                    if (DBG) {
                        Slog.d(TAG, "Pushing " + size + " UIDs to metered blacklist rules");
                    }
                    uidRejectOnQuota = this.mUidRejectOnMetered;
                    this.mUidRejectOnMetered = new SparseBooleanArray();
                }
                if (this.mUidAllowOnMetered.size() > 0) {
                    if (DBG) {
                        Slog.d(TAG, "Pushing " + size + " UIDs to metered whitelist rules");
                    }
                    uidAcceptOnQuota = this.mUidAllowOnMetered;
                    this.mUidAllowOnMetered = new SparseBooleanArray();
                }
            }
            if (uidRejectOnQuota != null) {
                for (int i = 0; i < uidRejectOnQuota.size(); i++) {
                    setUidMeteredNetworkBlacklist(uidRejectOnQuota.keyAt(i), uidRejectOnQuota.valueAt(i));
                }
            }
            if (uidAcceptOnQuota != null) {
                for (int i2 = 0; i2 < uidAcceptOnQuota.size(); i2++) {
                    setUidMeteredNetworkWhitelist(uidAcceptOnQuota.keyAt(i2), uidAcceptOnQuota.valueAt(i2));
                }
            }
            if (this.mUidCleartextPolicy.size() > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active UID cleartext policies");
                }
                SparseIntArray local = this.mUidCleartextPolicy;
                this.mUidCleartextPolicy = new SparseIntArray();
                for (int i3 = 0; i3 < local.size(); i3++) {
                    setUidCleartextNetworkPolicy(local.keyAt(i3), local.valueAt(i3));
                }
            }
            try {
                setFirewallEnabled(this.mFirewallEnabled);
            } catch (IllegalStateException e3) {
                Log.wtf(TAG, "Failed firewall enable", e3);
            }
            syncFirewallChainLocked(0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            syncFirewallChainLocked(2, "standby ");
            syncFirewallChainLocked(1, "dozable ");
            syncFirewallChainLocked(3, "powersave ");
            for (int chain : new int[]{2, 1, 3}) {
                if (getFirewallChainState(chain)) {
                    setFirewallChainEnabled(chain, true);
                }
            }
        }
        if (this.mBandwidthControlEnabled) {
            try {
                getBatteryStats().noteNetworkStatsEnabled();
            } catch (RemoteException e4) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyAddressUpdated(String iface, LinkAddress address) {
        invokeForAllObservers(new NetworkManagementEventCallback(iface, address) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ LinkAddress f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.addressUpdated(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyAddressRemoved(String iface, LinkAddress address) {
        invokeForAllObservers(new NetworkManagementEventCallback(iface, address) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ LinkAddress f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.addressRemoved(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyInterfaceDnsServerInfo(String iface, long lifetime, String[] addresses) {
        invokeForAllObservers(new NetworkManagementEventCallback(iface, lifetime, addresses) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ String[] f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
            }

            public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                iNetworkManagementEventObserver.interfaceDnsServerInfo(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyRouteChange(String action, RouteInfo route) {
        if (action.equals("updated")) {
            invokeForAllObservers(new NetworkManagementEventCallback(route) {
                private final /* synthetic */ RouteInfo f$0;

                {
                    this.f$0 = r1;
                }

                public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                    iNetworkManagementEventObserver.routeUpdated(this.f$0);
                }
            });
        } else {
            invokeForAllObservers(new NetworkManagementEventCallback(route) {
                private final /* synthetic */ RouteInfo f$0;

                {
                    this.f$0 = r1;
                }

                public final void sendCallback(INetworkManagementEventObserver iNetworkManagementEventObserver) {
                    iNetworkManagementEventObserver.routeRemoved(this.f$0);
                }
            });
        }
    }

    public INetd getNetdService() throws RemoteException {
        CountDownLatch connectedSignal = this.mConnectedSignal;
        if (connectedSignal != null) {
            try {
                connectedSignal.await();
            } catch (InterruptedException e) {
            }
        }
        return this.mNetdService;
    }

    public String[] listInterfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("interface", "list"), 110);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addUpstreamV6Interface(String iface) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "NetworkManagementService");
        Slog.d(TAG, "addUpstreamInterface(" + iface + ")");
        try {
            NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("tether", "interface", "add_upstream");
            cmd.appendArg(iface);
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw new RemoteException("Cannot add upstream interface");
        }
    }

    public void removeUpstreamV6Interface(String iface) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "NetworkManagementService");
        Slog.d(TAG, "removeUpstreamInterface(" + iface + ")");
        try {
            NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("tether", "interface", "remove_upstream");
            cmd.appendArg(iface);
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw new RemoteException("Cannot remove upstream interface");
        }
    }

    public InterfaceConfiguration getInterfaceConfig(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            int prefixLength = 0;
            NativeDaemonEvent event = this.mConnector.execute("interface", "getcfg", iface);
            event.checkCode(NetdResponseCode.InterfaceGetCfgResult);
            StringTokenizer st = new StringTokenizer(event.getMessage());
            try {
                InterfaceConfiguration cfg = new InterfaceConfiguration();
                cfg.setHardwareAddress(st.nextToken(" "));
                InetAddress addr = null;
                try {
                    addr = NetworkUtils.numericToInetAddress(st.nextToken());
                } catch (IllegalArgumentException iae) {
                    Slog.e(TAG, "Failed to parse ipaddr", iae);
                }
                try {
                    prefixLength = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    Slog.e(TAG, "Failed to parse prefixLength", nfe);
                }
                cfg.setLinkAddress(new LinkAddress(addr, prefixLength));
                while (st.hasMoreTokens()) {
                    cfg.setFlag(st.nextToken());
                }
                return cfg;
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Invalid response from daemon: " + event);
            }
        } catch (NativeDaemonConnectorException e2) {
            throw e2.rethrowAsParcelableException();
        }
    }

    public void setInterfaceConfig(String iface, InterfaceConfiguration cfg) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        LinkAddress linkAddr = cfg.getLinkAddress();
        if (linkAddr == null || linkAddr.getAddress() == null) {
            throw new IllegalStateException("Null LinkAddress given");
        }
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("interface", "setcfg", iface, linkAddr.getAddress().getHostAddress(), Integer.valueOf(linkAddr.getPrefixLength()));
        for (String flag : cfg.getFlags()) {
            cmd.appendArg(flag);
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setInterfaceDown(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        InterfaceConfiguration ifcg = getInterfaceConfig(iface);
        ifcg.setInterfaceDown();
        setInterfaceConfig(iface, ifcg);
    }

    public void setInterfaceUp(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        InterfaceConfiguration ifcg = getInterfaceConfig(iface);
        ifcg.setInterfaceUp();
        setInterfaceConfig(iface, ifcg);
    }

    public void setInterfaceIpv6PrivacyExtensions(String iface, boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6privacyextensions";
            objArr[1] = iface;
            objArr[2] = enable ? "enable" : "disable";
            nativeDaemonConnector.execute("interface", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearInterfaceAddresses(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("interface", "clearaddrs", iface);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void enableIpv6(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("interface", "ipv6", iface, "enable");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setIPv6AddrGenMode(String iface, int mode) throws ServiceSpecificException {
        try {
            this.mNetdService.setIPv6AddrGenMode(iface, mode);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void disableIpv6(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("interface", "ipv6", iface, "disable");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addRoute(int netId, RouteInfo route) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.NETWORKMANAGEMENT_ADDROUTE);
        }
        modifyRoute("add", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + netId, route);
    }

    public void removeRoute(int netId, RouteInfo route) {
        modifyRoute("remove", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + netId, route);
    }

    private void modifyRoute(String action, String netId, RouteInfo route) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("network", "route", action, netId);
        cmd.appendArg(route.getInterface());
        cmd.appendArg(route.getDestination().toString());
        int type = route.getType();
        if (type != 1) {
            if (type == 7) {
                cmd.appendArg("unreachable");
            } else if (type == 9) {
                cmd.appendArg("throw");
            }
        } else if (route.hasGateway()) {
            cmd.appendArg(route.getGateway().getHostAddress());
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003d, code lost:
        if (r0 == null) goto L_0x0040;
     */
    private ArrayList<String> readRouteList(String filename) {
        FileInputStream fstream = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            fstream = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
            while (true) {
                String readLine = br.readLine();
                String s = readLine;
                if (!(readLine == null || s.length() == 0)) {
                    list.add(s);
                }
                try {
                    fstream.close();
                    break;
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
        } catch (Throwable th) {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return list;
    }

    public void setMtu(String iface, int mtu) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonEvent execute = this.mConnector.execute("interface", "setmtu", iface, Integer.valueOf(mtu));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void shutdown() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", TAG);
        Slog.i(TAG, "Shutting down");
    }

    public boolean getIpForwardingEnabled() throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonEvent event = this.mConnector.execute("ipfwd", "status");
            event.checkCode(NetdResponseCode.IpFwdStatusResult);
            return event.getMessage().endsWith("enabled");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setIpForwardingEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            Object[] objArr = new Object[2];
            objArr[0] = enable ? "enable" : "disable";
            objArr[1] = ConnectivityService.TETHERING_ARG;
            nativeDaemonConnector.execute("ipfwd", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void startTethering(String[] dhcpRange) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("tether", "start");
        for (String d : dhcpRange) {
            cmd.appendArg(d);
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void stopTethering() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("tether", "stop");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isTetheringStarted() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonEvent event = this.mConnector.execute("tether", "status");
            event.checkCode(NetdResponseCode.TetherStatusResult);
            return event.getMessage().endsWith("started");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void tetherInterface(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("tether", "interface", "add", iface);
            List<RouteInfo> routes = new ArrayList<>();
            routes.add(new RouteInfo(getInterfaceConfig(iface).getLinkAddress(), null, iface));
            addInterfaceToLocalNetwork(iface, routes);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void untetherInterface(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("tether", "interface", "remove", iface);
            removeInterfaceFromLocalNetwork(iface);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (Throwable th) {
            removeInterfaceFromLocalNetwork(iface);
            throw th;
        }
    }

    public String[] listTetheredInterfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("tether", "interface", "list"), 111);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setDnsForwarders(Network network, String[] dns) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("tether", "dns", "set", Integer.valueOf(network != null ? network.netId : 0));
        for (String s : dns) {
            cmd.appendArg(NetworkUtils.numericToInetAddress(s).getHostAddress());
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public String[] getDnsForwarders() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("tether", "dns", "list"), 112);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private List<InterfaceAddress> excludeLinkLocal(List<InterfaceAddress> addresses) {
        ArrayList<InterfaceAddress> filtered = new ArrayList<>(addresses.size());
        for (InterfaceAddress ia : addresses) {
            if (!ia.getAddress().isLinkLocalAddress()) {
                filtered.add(ia);
            }
        }
        return filtered;
    }

    private void modifyInterfaceForward(boolean add, String fromIface, String toIface) {
        Object[] objArr = new Object[3];
        objArr[0] = add ? "add" : "remove";
        objArr[1] = fromIface;
        objArr[2] = toIface;
        try {
            this.mConnector.execute(new NativeDaemonConnector.Command("ipfwd", objArr));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void startInterfaceForwarding(String fromIface, String toIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        modifyInterfaceForward(true, fromIface, toIface);
    }

    public void stopInterfaceForwarding(String fromIface, String toIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        modifyInterfaceForward(false, fromIface, toIface);
    }

    private void modifyNat(String action, String internalInterface, String externalInterface) throws SocketException {
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("nat", action, internalInterface, externalInterface);
        NetworkInterface internalNetworkInterface = NetworkInterface.getByName(internalInterface);
        if (internalNetworkInterface == null) {
            cmd.appendArg("0");
        } else {
            List<InterfaceAddress> interfaceAddresses = excludeLinkLocal(internalNetworkInterface.getInterfaceAddresses());
            cmd.appendArg(Integer.valueOf(interfaceAddresses.size()));
            for (InterfaceAddress ia : interfaceAddresses) {
                InetAddress addr = NetworkUtils.getNetworkPart(ia.getAddress(), ia.getNetworkPrefixLength());
                cmd.appendArg(addr.getHostAddress() + SliceClientPermissions.SliceAuthority.DELIMITER + ia.getNetworkPrefixLength());
            }
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void enableNat(String internalInterface, String externalInterface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            modifyNat("enable", internalInterface, externalInterface);
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                NetPluginDelegate.natStarted(internalInterface, externalInterface);
            }
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
    }

    public void disableNat(String internalInterface, String externalInterface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            modifyNat("disable", internalInterface, externalInterface);
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                NetPluginDelegate.natStopped(internalInterface, externalInterface);
            }
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
    }

    public String[] listTtys() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("list_ttys", new Object[0]), 113);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void attachPppd(String tty, String localAddr, String remoteAddr, String dns1Addr, String dns2Addr) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("pppd", "attach", tty, NetworkUtils.numericToInetAddress(localAddr).getHostAddress(), NetworkUtils.numericToInetAddress(remoteAddr).getHostAddress(), NetworkUtils.numericToInetAddress(dns1Addr).getHostAddress(), NetworkUtils.numericToInetAddress(dns2Addr).getHostAddress());
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void detachPppd(String tty) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("pppd", "detach", tty);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addIdleTimer(String iface, int timeout, final int type) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Slog.d(TAG, "Adding idletimer");
        }
        synchronized (this.mIdleTimerLock) {
            IdleTimerParams params = this.mActiveIdleTimers.get(iface);
            if (params != null) {
                params.networkCount++;
                return;
            }
            try {
                this.mConnector.execute("idletimer", "add", iface, Integer.toString(timeout), Integer.toString(type));
                this.mActiveIdleTimers.put(iface, new IdleTimerParams(timeout, type));
                if (ConnectivityManager.isNetworkTypeMobile(type)) {
                    this.mNetworkActive = false;
                }
                this.mDaemonHandler.post(new Runnable() {
                    public void run() {
                        NetworkManagementService.this.notifyInterfaceClassActivity(type, 3, SystemClock.elapsedRealtimeNanos(), -1, false);
                    }
                });
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0067, code lost:
        return;
     */
    public void removeIdleTimer(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Slog.d(TAG, "Removing idletimer");
        }
        synchronized (this.mIdleTimerLock) {
            final IdleTimerParams params = this.mActiveIdleTimers.get(iface);
            if (params != null) {
                int i = params.networkCount - 1;
                params.networkCount = i;
                if (i <= 0) {
                    try {
                        this.mConnector.execute("idletimer", "remove", iface, Integer.toString(params.timeout), Integer.toString(params.type));
                        this.mActiveIdleTimers.remove(iface);
                        this.mDaemonHandler.post(new Runnable() {
                            public void run() {
                                NetworkManagementService.this.notifyInterfaceClassActivity(params.type, 1, SystemClock.elapsedRealtimeNanos(), -1, false);
                            }
                        });
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public NetworkStats getNetworkStatsSummaryDev() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsSummaryDev();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsSummaryXt() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsSummaryXt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsDetail() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsDetail(-1, null, -1, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setInterfaceQuota(String iface, long quotaBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                if (!this.mActiveQuotas.containsKey(iface)) {
                    try {
                        this.mConnector.execute("bandwidth", "setiquota", iface, Long.valueOf(quotaBytes));
                        this.mActiveQuotas.put(iface, Long.valueOf(quotaBytes));
                        synchronized (this.mTetheringStatsProviders) {
                            for (ITetheringStatsProvider provider : this.mTetheringStatsProviders.keySet()) {
                                try {
                                    provider.setInterfaceQuota(iface, quotaBytes);
                                } catch (RemoteException e) {
                                    Log.e(TAG, "Problem setting tethering data limit on provider " + this.mTetheringStatsProviders.get(provider) + ": " + e);
                                }
                            }
                        }
                    } catch (NativeDaemonConnectorException e2) {
                        throw e2.rethrowAsParcelableException();
                    }
                } else {
                    throw new IllegalStateException("iface " + iface + " already has quota");
                }
            }
        }
    }

    public void removeInterfaceQuota(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveQuotas.containsKey(iface)) {
                    this.mActiveQuotas.remove(iface);
                    this.mActiveAlerts.remove(iface);
                    try {
                        this.mConnector.execute("bandwidth", "removeiquota", iface);
                        synchronized (this.mTetheringStatsProviders) {
                            for (ITetheringStatsProvider provider : this.mTetheringStatsProviders.keySet()) {
                                try {
                                    provider.setInterfaceQuota(iface, -1);
                                } catch (RemoteException e) {
                                    Log.e(TAG, "Problem removing tethering data limit on provider " + this.mTetheringStatsProviders.get(provider) + ": " + e);
                                }
                            }
                        }
                    } catch (NativeDaemonConnectorException e2) {
                        throw e2.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public void setInterfaceAlert(String iface, long alertBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            if (this.mActiveQuotas.containsKey(iface)) {
                synchronized (this.mQuotaLock) {
                    if (!this.mActiveAlerts.containsKey(iface)) {
                        try {
                            this.mConnector.execute("bandwidth", "setinterfacealert", iface, Long.valueOf(alertBytes));
                            this.mActiveAlerts.put(iface, Long.valueOf(alertBytes));
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    } else {
                        throw new IllegalStateException("iface " + iface + " already has alert");
                    }
                }
                return;
            }
            throw new IllegalStateException("setting alert requires existing quota on iface");
        }
    }

    public void removeInterfaceAlert(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveAlerts.containsKey(iface)) {
                    try {
                        this.mConnector.execute("bandwidth", "removeinterfacealert", iface);
                        this.mActiveAlerts.remove(iface);
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public void setGlobalAlert(long alertBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            try {
                this.mConnector.execute("bandwidth", "setglobalalert", Long.valueOf(alertBytes));
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    private void setUidOnMeteredNetworkList(int uid, boolean blacklist, boolean enable) {
        SparseBooleanArray quotaList;
        boolean oldEnable;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            String chain = blacklist ? "naughtyapps" : "niceapps";
            String suffix = enable ? "add" : "remove";
            synchronized (this.mQuotaLock) {
                synchronized (this.mRulesLock) {
                    if (blacklist) {
                        try {
                            quotaList = this.mUidRejectOnMetered;
                        } catch (Throwable th) {
                            while (true) {
                                throw th;
                            }
                        }
                    } else {
                        quotaList = this.mUidAllowOnMetered;
                    }
                    oldEnable = quotaList.get(uid, false);
                }
                if (oldEnable != enable) {
                    Trace.traceBegin(2097152, "inetd bandwidth");
                    try {
                        NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                        nativeDaemonConnector.execute("bandwidth", suffix + chain, Integer.valueOf(uid));
                        synchronized (this.mRulesLock) {
                            if (enable) {
                                quotaList.put(uid, true);
                            } else {
                                quotaList.delete(uid);
                            }
                        }
                        Trace.traceEnd(2097152);
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    } catch (Throwable th2) {
                        while (true) {
                        }
                        throw th2;
                    }
                }
            }
        }
    }

    public void setUidMeteredNetworkBlacklist(int uid, boolean enable) {
        setUidOnMeteredNetworkList(uid, true, enable);
    }

    public void setUidMeteredNetworkWhitelist(int uid, boolean enable) {
        setUidOnMeteredNetworkList(uid, false, enable);
    }

    public boolean setDataSaverModeEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_SETTINGS", TAG);
        if (DBG) {
            Log.d(TAG, "setDataSaverMode: " + enable);
        }
        synchronized (this.mQuotaLock) {
            if (this.mDataSaverMode == enable) {
                Log.w(TAG, "setDataSaverMode(): already " + this.mDataSaverMode);
                return true;
            }
            Trace.traceBegin(2097152, "bandwidthEnableDataSaver");
            try {
                boolean changed = this.mNetdService.bandwidthEnableDataSaver(enable);
                if (changed) {
                    this.mDataSaverMode = enable;
                } else {
                    Log.w(TAG, "setDataSaverMode(" + enable + "): netd command silently failed");
                }
                Trace.traceEnd(2097152);
                return changed;
            } catch (RemoteException e) {
                try {
                    Log.w(TAG, "setDataSaverMode(" + enable + "): netd command failed", e);
                    return false;
                } finally {
                    Trace.traceEnd(2097152);
                }
            }
        }
    }

    public void setAllowOnlyVpnForUids(boolean add, UidRange[] uidRanges) throws ServiceSpecificException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
        try {
            this.mNetdService.networkRejectNonSecureVpn(add, uidRanges);
        } catch (ServiceSpecificException e) {
            Log.w(TAG, "setAllowOnlyVpnForUids(" + add + ", " + Arrays.toString(uidRanges) + "): netd command failed", e);
            throw e;
        } catch (RemoteException e2) {
            Log.w(TAG, "setAllowOnlyVpnForUids(" + add + ", " + Arrays.toString(uidRanges) + "): netd command failed", e2);
            throw e2.rethrowAsRuntimeException();
        }
    }

    public int getNetdPid() {
        try {
            return this.mNetdService.getNetdPid();
        } catch (ServiceSpecificException e) {
            Log.w(TAG, "getNetdPid, SSE:", e);
            return -1;
        } catch (RemoteException e2) {
            Log.w(TAG, "getNetdPid RE", e2);
            return -1;
        }
    }

    private void applyUidCleartextNetworkPolicy(int uid, int policy) {
        String policyString;
        switch (policy) {
            case 0:
                policyString = "accept";
                break;
            case 1:
                policyString = "log";
                break;
            case 2:
                policyString = "reject";
                break;
            default:
                throw new IllegalArgumentException("Unknown policy " + policy);
        }
        try {
            this.mConnector.execute("strict", "set_uid_cleartext_policy", Integer.valueOf(uid), policyString);
            this.mUidCleartextPolicy.put(uid, policy);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setUidCleartextNetworkPolicy(int uid, int policy) {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        }
        synchronized (this.mQuotaLock) {
            int oldPolicy = this.mUidCleartextPolicy.get(uid, 0);
            if (oldPolicy != policy) {
                if (!this.mStrictEnabled) {
                    this.mUidCleartextPolicy.put(uid, policy);
                    return;
                }
                if (!(oldPolicy == 0 || policy == 0)) {
                    applyUidCleartextNetworkPolicy(uid, 0);
                }
                applyUidCleartextNetworkPolicy(uid, policy);
            }
        }
    }

    public boolean isBandwidthControlEnabled() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return this.mBandwidthControlEnabled;
    }

    public NetworkStats getNetworkStatsUidDetail(int uid, String[] ifaces) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsDetail(uid, ifaces, -1, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsTethering(int how) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        synchronized (this.mTetheringStatsProviders) {
            for (ITetheringStatsProvider provider : this.mTetheringStatsProviders.keySet()) {
                try {
                    stats.combineAllValues(provider.getTetherStats(how));
                } catch (RemoteException e) {
                    Log.e(TAG, "Problem reading tethering stats from " + this.mTetheringStatsProviders.get(provider) + ": " + e);
                }
            }
        }
        return stats;
    }

    public void setDnsConfigurationForNetwork(int netId, String[] servers, String[] domains, int[] params, String tlsHostname, String[] tlsServers) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mNetdService.setResolverConfiguration(netId, servers, domains, params, tlsHostname, tlsServers, new String[0]);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void addVpnUidRanges(int netId, UidRange[] ranges) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = DatabaseHelper.SoundModelContract.KEY_USERS;
        argv[1] = "add";
        argv[2] = Integer.valueOf(netId);
        int argc = 3;
        for (int i = 0; i < ranges.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = ranges[i].toString();
            if (i == ranges.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            } else {
                argc = argc2;
            }
        }
    }

    public void removeVpnUidRanges(int netId, UidRange[] ranges) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = DatabaseHelper.SoundModelContract.KEY_USERS;
        argv[1] = "remove";
        argv[2] = Integer.valueOf(netId);
        int argc = 3;
        for (int i = 0; i < ranges.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = ranges[i].toString();
            if (i == ranges.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            } else {
                argc = argc2;
            }
        }
    }

    public void setFirewallEnabled(boolean enabled) {
        enforceSystemUid();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            Object[] objArr = new Object[2];
            objArr[0] = "enable";
            objArr[1] = enabled ? "whitelist" : "blacklist";
            nativeDaemonConnector.execute("firewall", objArr);
            this.mFirewallEnabled = enabled;
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isFirewallEnabled() {
        enforceSystemUid();
        return this.mFirewallEnabled;
    }

    public void setFirewallInterfaceRule(String iface, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        try {
            this.mConnector.execute("firewall", "set_interface_rule", iface, allow ? "allow" : "deny");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: type inference failed for: r1v5, types: [java.lang.Object[]] */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0070, code lost:
        r0 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0072, code lost:
        if (r5 == r0.length) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0074, code lost:
        r0 = java.util.Arrays.copyOf(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007b, code lost:
        r3 = r0;
        r1 = new int[0];
        r0 = r5;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private void closeSocketsForFirewallChainLocked(int chain, String chainName) {
        UidRange[] ranges;
        int[] exemptUids;
        int[] exemptUids2;
        int numUids = 0;
        if (getFirewallType(chain) == 0) {
            ranges = new UidRange[]{new UidRange(10000, HwBootFail.STAGE_BOOT_SUCCESS)};
            synchronized (this.mRulesLock) {
                SparseIntArray rules = getUidFirewallRulesLR(chain);
                exemptUids2 = new int[rules.size()];
                for (int i = 0; i < exemptUids2.length; i++) {
                    if (rules.valueAt(i) == 1) {
                        exemptUids2[numUids] = rules.keyAt(i);
                        numUids++;
                    }
                }
            }
            exemptUids = exemptUids2;
            if (numUids != exemptUids.length) {
                exemptUids = Arrays.copyOf(exemptUids, numUids);
            }
        } else {
            synchronized (this.mRulesLock) {
                try {
                    SparseIntArray rules2 = getUidFirewallRulesLR(chain);
                    UidRange[] ranges2 = new UidRange[rules2.size()];
                    int numUids2 = 0;
                    int i2 = 0;
                    while (i2 < ranges2.length) {
                        try {
                            if (rules2.valueAt(i2) == 2) {
                                int uid = rules2.keyAt(i2);
                                ranges2[numUids2] = new UidRange(uid, uid);
                                numUids2++;
                            }
                            i2++;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
        try {
            this.mNetdService.socketDestroy(ranges, exemptUids);
        } catch (RemoteException | ServiceSpecificException | IllegalArgumentException e) {
            Slog.e(TAG, "Error closing sockets after enabling chain " + chainName + ": " + e);
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockSplitter
        jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x002b in list []
        	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
        	at jadx.core.dex.instructions.SwitchNode.initBlocks(SwitchNode.java:64)
        	at jadx.core.dex.visitors.blocksmaker.BlockSplitter.lambda$initBlocksInTargetNodes$0(BlockSplitter.java:71)
        	at java.util.ArrayList.forEach(ArrayList.java:1257)
        	at jadx.core.dex.visitors.blocksmaker.BlockSplitter.initBlocksInTargetNodes(BlockSplitter.java:68)
        	at jadx.core.dex.visitors.blocksmaker.BlockSplitter.visit(BlockSplitter.java:53)
        */
    public void setFirewallChainEnabled(int r8, boolean r9) {
        /*
            r7 = this;
            enforceSystemUid()
            java.lang.Object r0 = r7.mQuotaLock
            monitor-enter(r0)
            java.lang.Object r1 = r7.mRulesLock     // Catch:{ all -> 0x007f }
            monitor-enter(r1)     // Catch:{ all -> 0x007f }
            boolean r2 = r7.getFirewallChainState(r8)     // Catch:{ all -> 0x007c }
            if (r2 != r9) goto L_0x0012     // Catch:{ all -> 0x007c }
            monitor-exit(r1)     // Catch:{ all -> 0x007c }
            monitor-exit(r0)     // Catch:{ all -> 0x007f }
            return
        L_0x0012:
            r7.setFirewallChainState(r8, r9)     // Catch:{ all -> 0x007c }
            monitor-exit(r1)     // Catch:{ all -> 0x007c }
            if (r9 == 0) goto L_0x001b
            java.lang.String r1 = "enable_chain"
            goto L_0x001d
        L_0x001b:
            java.lang.String r1 = "disable_chain"
        L_0x001d:
            switch(r8) {
                case 1: goto L_0x002b;
                case 2: goto L_0x0027;
                case 3: goto L_0x0023;
                default: goto L_0x0020;
            }     // Catch:{ all -> 0x007f }
        L_0x0020:
            java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x007f }
            goto L_0x0067     // Catch:{ all -> 0x007f }
        L_0x0023:
            java.lang.String r2 = "powersave"     // Catch:{ all -> 0x007f }
            goto L_0x002e     // Catch:{ all -> 0x007f }
        L_0x0027:
            java.lang.String r2 = "standby"     // Catch:{ all -> 0x007f }
            goto L_0x002e     // Catch:{ all -> 0x007f }
        L_0x002b:
            java.lang.String r2 = "dozable"     // Catch:{ all -> 0x007f }
        L_0x002e:
            com.android.server.NativeDaemonConnector r3 = r7.mConnector     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            java.lang.String r4 = "firewall"     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            r5 = 2     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            r6 = 0     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            r5[r6] = r1     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            r6 = 1     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            r5[r6] = r2     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            r3.execute(r4, r5)     // Catch:{ NativeDaemonConnectorException -> 0x0061 }
            if (r9 == 0) goto L_0x005f
            boolean r3 = DBG     // Catch:{ all -> 0x007f }
            if (r3 == 0) goto L_0x005c     // Catch:{ all -> 0x007f }
            java.lang.String r3 = "NetworkManagement"     // Catch:{ all -> 0x007f }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x007f }
            r4.<init>()     // Catch:{ all -> 0x007f }
            java.lang.String r5 = "Closing sockets after enabling chain "     // Catch:{ all -> 0x007f }
            r4.append(r5)     // Catch:{ all -> 0x007f }
            r4.append(r2)     // Catch:{ all -> 0x007f }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x007f }
            android.util.Slog.d(r3, r4)     // Catch:{ all -> 0x007f }
        L_0x005c:
            r7.closeSocketsForFirewallChainLocked(r8, r2)     // Catch:{ all -> 0x007f }
        L_0x005f:
            monitor-exit(r0)     // Catch:{ all -> 0x007f }
            return     // Catch:{ all -> 0x007f }
            r3 = move-exception     // Catch:{ all -> 0x007f }
            java.lang.IllegalArgumentException r4 = r3.rethrowAsParcelableException()     // Catch:{ all -> 0x007f }
            throw r4     // Catch:{ all -> 0x007f }
        L_0x0067:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x007f }
            r3.<init>()     // Catch:{ all -> 0x007f }
            java.lang.String r4 = "Bad child chain: "     // Catch:{ all -> 0x007f }
            r3.append(r4)     // Catch:{ all -> 0x007f }
            r3.append(r8)     // Catch:{ all -> 0x007f }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x007f }
            r2.<init>(r3)     // Catch:{ all -> 0x007f }
            throw r2     // Catch:{ all -> 0x007f }
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x007c }
            throw r2     // Catch:{ all -> 0x007f }
            r1 = move-exception     // Catch:{ all -> 0x007f }
            monitor-exit(r0)     // Catch:{ all -> 0x007f }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetworkManagementService.setFirewallChainEnabled(int, boolean):void");
    }

    private int getFirewallType(int chain) {
        switch (chain) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 0;
            default:
                return true ^ isFirewallEnabled() ? 1 : 0;
        }
    }

    public void setFirewallUidRules(int chain, int[] uids, int[] rules) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            synchronized (this.mRulesLock) {
                SparseIntArray uidFirewallRules = getUidFirewallRulesLR(chain);
                SparseIntArray newRules = new SparseIntArray();
                for (int index = uids.length - 1; index >= 0; index--) {
                    int uid = uids[index];
                    int rule = rules[index];
                    updateFirewallUidRuleLocked(chain, uid, rule);
                    newRules.put(uid, rule);
                }
                SparseIntArray rulesToRemove = new SparseIntArray();
                for (int index2 = uidFirewallRules.size() - 1; index2 >= 0; index2--) {
                    int uid2 = uidFirewallRules.keyAt(index2);
                    if (newRules.indexOfKey(uid2) < 0) {
                        rulesToRemove.put(uid2, 0);
                    }
                }
                for (int index3 = rulesToRemove.size() - 1; index3 >= 0; index3--) {
                    updateFirewallUidRuleLocked(chain, rulesToRemove.keyAt(index3), 0);
                }
            }
            switch (chain) {
                case 1:
                    this.mNetdService.firewallReplaceUidChain("fw_dozable", true, uids);
                    break;
                case 2:
                    this.mNetdService.firewallReplaceUidChain("fw_standby", false, uids);
                    break;
                case 3:
                    try {
                        this.mNetdService.firewallReplaceUidChain("fw_powersave", true, uids);
                        break;
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Error flushing firewall chain " + chain, e);
                        break;
                    }
                default:
                    Slog.d(TAG, "setFirewallUidRules() called on invalid chain: " + chain);
                    break;
            }
        }
    }

    public void setFirewallUidRule(int chain, int uid, int rule) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            setFirewallUidRuleLocked(chain, uid, rule);
        }
    }

    private void setFirewallUidRuleLocked(int chain, int uid, int rule) {
        if (updateFirewallUidRuleLocked(chain, uid, rule)) {
            try {
                this.mConnector.execute("firewall", "set_uid_rule", getFirewallChainName(chain), Integer.valueOf(uid), getFirewallRuleName(chain, rule));
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x004d, code lost:
        return false;
     */
    private boolean updateFirewallUidRuleLocked(int chain, int uid, int rule) {
        synchronized (this.mRulesLock) {
            SparseIntArray uidFirewallRules = getUidFirewallRulesLR(chain);
            int oldUidFirewallRule = uidFirewallRules.get(uid, 0);
            if (DBG) {
                Slog.d(TAG, "oldRule = " + oldUidFirewallRule + ", newRule=" + rule + " for uid=" + uid + " on chain " + chain);
            }
            if (oldUidFirewallRule != rule) {
                String ruleName = getFirewallRuleName(chain, rule);
                String oldRuleName = getFirewallRuleName(chain, oldUidFirewallRule);
                if (rule == 0) {
                    uidFirewallRules.delete(uid);
                } else {
                    uidFirewallRules.put(uid, rule);
                }
                boolean z = !ruleName.equals(oldRuleName);
                return z;
            } else if (DBG) {
                Slog.d(TAG, "!!!!! Skipping change");
            }
        }
    }

    private String getFirewallRuleName(int chain, int rule) {
        if (getFirewallType(chain) == 0) {
            if (rule == 1) {
                return "allow";
            }
            return "deny";
        } else if (rule == 2) {
            return "deny";
        } else {
            return "allow";
        }
    }

    /* access modifiers changed from: private */
    public SparseIntArray getUidFirewallRulesLR(int chain) {
        switch (chain) {
            case 0:
                return this.mUidFirewallRules;
            case 1:
                return this.mUidFirewallDozableRules;
            case 2:
                return this.mUidFirewallStandbyRules;
            case 3:
                return this.mUidFirewallPowerSaveRules;
            default:
                throw new IllegalArgumentException("Unknown chain:" + chain);
        }
    }

    public String getFirewallChainName(int chain) {
        switch (chain) {
            case 0:
                return "none";
            case 1:
                return "dozable";
            case 2:
                return "standby";
            case 3:
                return "powersave";
            default:
                throw new IllegalArgumentException("Unknown chain:" + chain);
        }
    }

    private static void enforceSystemUid() {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only available to AID_SYSTEM");
        }
    }

    public void startClatd(String interfaceName) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("clatd", "start", interfaceName);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void stopClatd(String interfaceName) throws IllegalStateException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("clatd", "stop", interfaceName);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isClatdStarted(String interfaceName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonEvent event = this.mConnector.execute("clatd", "status", interfaceName);
            event.checkCode(NetdResponseCode.ClatdStatusResult);
            return event.getMessage().endsWith("started");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void registerNetworkActivityListener(INetworkActivityListener listener) {
        this.mNetworkActivityListeners.register(listener);
    }

    public void unregisterNetworkActivityListener(INetworkActivityListener listener) {
        this.mNetworkActivityListeners.unregister(listener);
    }

    public boolean isNetworkActive() {
        boolean z;
        synchronized (this.mNetworkActivityListeners) {
            if (!this.mNetworkActive) {
                if (!this.mActiveIdleTimers.isEmpty()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    private void reportNetworkActive() {
        int length = this.mNetworkActivityListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mNetworkActivityListeners.getBroadcastItem(i).onNetworkActive();
            } catch (RemoteException | RuntimeException e) {
            } catch (Throwable th) {
                this.mNetworkActivityListeners.finishBroadcast();
                throw th;
            }
        }
        this.mNetworkActivityListeners.finishBroadcast();
    }

    public void monitor() {
        if (this.mConnector != null) {
            this.mConnector.monitor();
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("NetworkManagementService NativeDaemonConnector Log:");
            this.mConnector.dump(fd, pw, args);
            pw.println();
            pw.print("Bandwidth control enabled: ");
            pw.println(this.mBandwidthControlEnabled);
            pw.print("mMobileActivityFromRadio=");
            pw.print(this.mMobileActivityFromRadio);
            pw.print(" mLastPowerStateFromRadio=");
            pw.println(this.mLastPowerStateFromRadio);
            pw.print("mNetworkActive=");
            pw.println(this.mNetworkActive);
            synchronized (this.mQuotaLock) {
                pw.print("Active quota ifaces: ");
                pw.println(this.mActiveQuotas.toString());
                pw.print("Active alert ifaces: ");
                pw.println(this.mActiveAlerts.toString());
                pw.print("Data saver mode: ");
                pw.println(this.mDataSaverMode);
                synchronized (this.mRulesLock) {
                    dumpUidRuleOnQuotaLocked(pw, "blacklist", this.mUidRejectOnMetered);
                    dumpUidRuleOnQuotaLocked(pw, "whitelist", this.mUidAllowOnMetered);
                }
            }
            synchronized (this.mRulesLock) {
                dumpUidFirewallRule(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, this.mUidFirewallRules);
                pw.print("UID firewall standby chain enabled: ");
                pw.println(getFirewallChainState(2));
                dumpUidFirewallRule(pw, "standby", this.mUidFirewallStandbyRules);
                pw.print("UID firewall dozable chain enabled: ");
                pw.println(getFirewallChainState(1));
                dumpUidFirewallRule(pw, "dozable", this.mUidFirewallDozableRules);
                pw.println("UID firewall powersave chain enabled: " + getFirewallChainState(3));
                dumpUidFirewallRule(pw, "powersave", this.mUidFirewallPowerSaveRules);
            }
            synchronized (this.mIdleTimerLock) {
                pw.println("Idle timers:");
                for (Map.Entry<String, IdleTimerParams> ent : this.mActiveIdleTimers.entrySet()) {
                    pw.print("  ");
                    pw.print(ent.getKey());
                    pw.println(":");
                    IdleTimerParams params = ent.getValue();
                    pw.print("    timeout=");
                    pw.print(params.timeout);
                    pw.print(" type=");
                    pw.print(params.type);
                    pw.print(" networkCount=");
                    pw.println(params.networkCount);
                }
            }
            pw.print("Firewall enabled: ");
            pw.println(this.mFirewallEnabled);
            pw.print("Netd service status: ");
            if (this.mNetdService == null) {
                pw.println("disconnected");
            } else {
                try {
                    pw.println(this.mNetdService.isAlive() ? "alive" : "dead");
                } catch (RemoteException e) {
                    pw.println("unreachable");
                }
            }
        }
    }

    private void dumpUidRuleOnQuotaLocked(PrintWriter pw, String name, SparseBooleanArray list) {
        pw.print("UID bandwith control ");
        pw.print(name);
        pw.print(" rule: [");
        int size = list.size();
        for (int i = 0; i < size; i++) {
            pw.print(list.keyAt(i));
            if (i < size - 1) {
                pw.print(",");
            }
        }
        pw.println("]");
    }

    private void dumpUidFirewallRule(PrintWriter pw, String name, SparseIntArray rules) {
        pw.print("UID firewall ");
        pw.print(name);
        pw.print(" rule: [");
        int size = rules.size();
        for (int i = 0; i < size; i++) {
            pw.print(rules.keyAt(i));
            pw.print(":");
            pw.print(rules.valueAt(i));
            if (i < size - 1) {
                pw.print(",");
            }
        }
        pw.println("]");
    }

    public void createPhysicalNetwork(int netId, String permission) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (permission != null) {
            try {
                this.mConnector.execute("network", "create", Integer.valueOf(netId), permission);
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        } else {
            this.mConnector.execute("network", "create", Integer.valueOf(netId));
        }
    }

    public void createVirtualNetwork(int netId, boolean hasDNS, boolean secure) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            Object[] objArr = new Object[5];
            objArr[0] = "create";
            objArr[1] = Integer.valueOf(netId);
            objArr[2] = "vpn";
            objArr[3] = hasDNS ? "1" : "0";
            objArr[4] = secure ? "1" : "0";
            nativeDaemonConnector.execute("network", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void removeNetwork(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
        try {
            this.mNetdService.networkDestroy(netId);
        } catch (ServiceSpecificException e) {
            Log.w(TAG, "removeNetwork(" + netId + "): ", e);
            throw e;
        } catch (RemoteException e2) {
            Log.w(TAG, "removeNetwork(" + netId + "): ", e2);
            throw e2.rethrowAsRuntimeException();
        }
    }

    public void addInterfaceToNetwork(String iface, int netId) {
        modifyInterfaceInNetwork("add", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + netId, iface);
    }

    public void removeInterfaceFromNetwork(String iface, int netId) {
        modifyInterfaceInNetwork("remove", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + netId, iface);
    }

    private void modifyInterfaceInNetwork(String action, String netId, String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", "interface", action, netId, iface);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addLegacyRouteForNetId(int netId, RouteInfo routeInfo, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NativeDaemonConnector.Command cmd = new NativeDaemonConnector.Command("network", "route", "legacy", Integer.valueOf(uid), "add", Integer.valueOf(netId));
        LinkAddress la = routeInfo.getDestinationLinkAddress();
        cmd.appendArg(routeInfo.getInterface());
        cmd.appendArg(la.getAddress().getHostAddress() + SliceClientPermissions.SliceAuthority.DELIMITER + la.getPrefixLength());
        if (routeInfo.hasGateway()) {
            cmd.appendArg(routeInfo.getGateway().getHostAddress());
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setDefaultNetId(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, "set", Integer.valueOf(netId));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearDefaultNetId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, "clear");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setNetworkPermission(int netId, String permission) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (permission != null) {
            try {
                this.mConnector.execute("network", "permission", "network", "set", permission, Integer.valueOf(netId));
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        } else {
            this.mConnector.execute("network", "permission", "network", "clear", Integer.valueOf(netId));
        }
    }

    public void setPermission(String permission, int[] uids) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[14];
        argv[0] = "permission";
        argv[1] = "user";
        argv[2] = "set";
        argv[3] = permission;
        int argc = 4;
        for (int i = 0; i < uids.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = Integer.valueOf(uids[i]);
            if (i == uids.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 4;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            } else {
                argc = argc2;
            }
        }
    }

    public void clearPermission(int[] uids) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = "permission";
        argv[1] = "user";
        argv[2] = "clear";
        int argc = 3;
        for (int i = 0; i < uids.length; i++) {
            int argc2 = argc + 1;
            argv[argc] = Integer.valueOf(uids[i]);
            if (i == uids.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            } else {
                argc = argc2;
            }
        }
    }

    public void allowProtect(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", "protect", "allow", Integer.valueOf(uid));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void denyProtect(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", "protect", "deny", Integer.valueOf(uid));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addInterfaceToLocalNetwork(String iface, List<RouteInfo> routes) {
        modifyInterfaceInNetwork("add", "local", iface);
        for (RouteInfo route : routes) {
            if (!route.isDefaultRoute()) {
                modifyRoute("add", "local", route);
            }
        }
    }

    public void removeInterfaceFromLocalNetwork(String iface) {
        modifyInterfaceInNetwork("remove", "local", iface);
    }

    public int removeRoutesFromLocalNetwork(List<RouteInfo> routes) {
        int failures = 0;
        for (RouteInfo route : routes) {
            try {
                modifyRoute("remove", "local", route);
            } catch (IllegalStateException e) {
                failures++;
            }
        }
        return failures;
    }

    public boolean isNetworkRestricted(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return isNetworkRestrictedInternal(uid);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0033, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0092, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00bb, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00e8, code lost:
        return true;
     */
    public boolean isNetworkRestrictedInternal(int uid) {
        synchronized (this.mRulesLock) {
            if (!getFirewallChainState(2) || this.mUidFirewallStandbyRules.get(uid) != 2) {
                if (!getFirewallChainState(1) || this.mUidFirewallDozableRules.get(uid) == 1) {
                    if (!getFirewallChainState(3) || this.mUidFirewallPowerSaveRules.get(uid) == 1) {
                        if (this.mUidRejectOnMetered.get(uid)) {
                            if (DBG) {
                                Slog.d(TAG, "Uid " + uid + " restricted because of no metered data in the background");
                            }
                        } else if (!this.mDataSaverMode || this.mUidAllowOnMetered.get(uid)) {
                            return false;
                        } else {
                            if (DBG) {
                                Slog.d(TAG, "Uid " + uid + " restricted because of data saver mode");
                            }
                        }
                    } else if (DBG) {
                        Slog.d(TAG, "Uid " + uid + " restricted because of power saver mode");
                    }
                } else if (DBG) {
                    Slog.d(TAG, "Uid " + uid + " restricted because of device idle mode");
                }
            } else if (DBG) {
                Slog.d(TAG, "Uid " + uid + " restricted because of app standby mode");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setFirewallChainState(int chain, boolean state) {
        synchronized (this.mRulesLock) {
            this.mFirewallChainStates.put(chain, state);
        }
    }

    private boolean getFirewallChainState(int chain) {
        boolean z;
        synchronized (this.mRulesLock) {
            z = this.mFirewallChainStates.get(chain);
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Injector getInjector() {
        return new Injector();
    }

    public String getFirewallRuleNameHw(int chain, int rule) {
        return getFirewallRuleName(chain, rule);
    }
}
