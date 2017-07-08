package com.android.server;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetd;
import android.net.INetworkManagementEventObserver;
import android.net.InterfaceConfiguration;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkStats;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.Handler;
import android.os.INetworkActivityListener;
import android.os.INetworkManagementService.Stub;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.HexDump;
import com.android.internal.util.Preconditions;
import com.android.server.HwServiceFactory.IHwNetworkManagermentService;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.ProcessList;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.power.AbsPowerManagerService;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import com.google.android.collect.Maps;
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
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

public class NetworkManagementService extends Stub implements Monitor {
    static final int DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO = 1;
    private static final boolean DBG = false;
    public static final int DNS_RESOLVER_DEFAULT_MAX_SAMPLES = 64;
    public static final int DNS_RESOLVER_DEFAULT_MIN_SAMPLES = 8;
    public static final int DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS = 1800;
    public static final int DNS_RESOLVER_DEFAULT_SUCCESS_THRESHOLD_PERCENT = 25;
    public static final String LIMIT_GLOBAL_ALERT = "globalAlert";
    private static final int MAX_UID_RANGES_PER_COMMAND = 10;
    private static final String NETD_SERVICE_NAME = "netd";
    private static final String NETD_TAG = "NetdConnector";
    public static final String PERMISSION_NETWORK = "NETWORK";
    public static final String PERMISSION_SYSTEM = "SYSTEM";
    static final String SOFT_AP_COMMAND = "softap";
    static final String SOFT_AP_COMMAND_SUCCESS = "Ok";
    private static final String TAG = "NetworkManagement";
    private static IHwNetworkManagermentService mNetworkMS;
    @GuardedBy("mQuotaLock")
    private HashMap<String, Long> mActiveAlerts;
    private HashMap<String, IdleTimerParams> mActiveIdleTimers;
    @GuardedBy("mQuotaLock")
    private HashMap<String, Long> mActiveQuotas;
    private volatile boolean mBandwidthControlEnabled;
    private IBatteryStats mBatteryStats;
    private CountDownLatch mConnectedSignal;
    private final NativeDaemonConnector mConnector;
    private final Context mContext;
    private final Handler mDaemonHandler;
    @GuardedBy("mQuotaLock")
    private boolean mDataSaverMode;
    private final Handler mFgHandler;
    @GuardedBy("mQuotaLock")
    final SparseBooleanArray mFirewallChainStates;
    private volatile boolean mFirewallEnabled;
    private Object mIdleTimerLock;
    private int mLastPowerStateFromRadio;
    private int mLastPowerStateFromWifi;
    private int mLinkedStaCount;
    private boolean mMobileActivityFromRadio;
    private INetd mNetdService;
    private boolean mNetworkActive;
    private final RemoteCallbackList<INetworkActivityListener> mNetworkActivityListeners;
    private final RemoteCallbackList<INetworkManagementEventObserver> mObservers;
    private Object mQuotaLock;
    private final NetworkStatsFactory mStatsFactory;
    private volatile boolean mStrictEnabled;
    private final Thread mThread;
    @GuardedBy("mQuotaLock")
    private SparseBooleanArray mUidAllowOnMetered;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidCleartextPolicy;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallDozableRules;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallPowerSaveRules;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallRules;
    @GuardedBy("mQuotaLock")
    private SparseIntArray mUidFirewallStandbyRules;
    @GuardedBy("mQuotaLock")
    private SparseBooleanArray mUidRejectOnMetered;
    private HwNativeDaemonConnector mhwNativeDaemonConnector;

    /* renamed from: com.android.server.NetworkManagementService.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$type;

        AnonymousClass1(int val$type) {
            this.val$type = val$type;
        }

        public void run() {
            NetworkManagementService.this.notifyInterfaceClassActivity(this.val$type, 3, SystemClock.elapsedRealtimeNanos(), -1, NetworkManagementService.DBG);
        }
    }

    /* renamed from: com.android.server.NetworkManagementService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ IdleTimerParams val$params;

        AnonymousClass2(IdleTimerParams val$params) {
            this.val$params = val$params;
        }

        public void run() {
            NetworkManagementService.this.notifyInterfaceClassActivity(this.val$params.type, NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO, SystemClock.elapsedRealtimeNanos(), -1, NetworkManagementService.DBG);
        }
    }

    private static class IdleTimerParams {
        public int networkCount;
        public final int timeout;
        public final int type;

        IdleTimerParams(int timeout, int type) {
            this.timeout = timeout;
            this.type = type;
            this.networkCount = NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
        }
    }

    private class NetdCallbackReceiver implements INativeDaemonConnectorCallbacks {
        private NetdCallbackReceiver() {
        }

        public void onDaemonConnected() {
            Slog.i(NetworkManagementService.TAG, "onDaemonConnected()");
            if (NetworkManagementService.this.mConnectedSignal != null) {
                NetworkManagementService.this.mConnectedSignal.countDown();
                NetworkManagementService.this.mConnectedSignal = null;
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
            return code == NetdResponseCode.InterfaceClassActivity ? true : NetworkManagementService.DBG;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            Object[] objArr = new Object[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = raw;
            String errorMessage = String.format("Invalid event from daemon (%s)", objArr);
            switch (code) {
                case NetdResponseCode.InterfaceChange /*600*/:
                    if (cooked.length < 4 || !cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO].equals("Iface")) {
                        throw new IllegalStateException(errorMessage);
                    } else if (cooked[2].equals("added")) {
                        NetworkManagementService.this.notifyInterfaceAdded(cooked[3]);
                        return true;
                    } else if (cooked[2].equals("removed")) {
                        NetworkManagementService.this.notifyInterfaceRemoved(cooked[3]);
                        return true;
                    } else if (cooked[2].equals("changed") && cooked.length == 5) {
                        NetworkManagementService.this.notifyInterfaceStatusChanged(cooked[3], cooked[4].equals("up"));
                        return true;
                    } else if (cooked[2].equals("linkstate") && cooked.length == 5) {
                        NetworkManagementService.this.notifyInterfaceLinkStateChanged(cooked[3], cooked[4].equals("up"));
                        return true;
                    } else {
                        throw new IllegalStateException(errorMessage);
                    }
                case NetdResponseCode.BandwidthControl /*601*/:
                    if (cooked.length < 5 || !cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO].equals("limit")) {
                        throw new IllegalStateException(errorMessage);
                    } else if (cooked[2].equals("alert")) {
                        NetworkManagementService.this.notifyLimitReached(cooked[3], cooked[4]);
                        return true;
                    } else {
                        throw new IllegalStateException(errorMessage);
                    }
                case NetdResponseCode.InterfaceClassActivity /*613*/:
                    if (cooked.length < 4 || !cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO].equals("IfaceClass")) {
                        throw new IllegalStateException(errorMessage);
                    }
                    int i;
                    long timestampNanos = 0;
                    int processUid = -1;
                    if (cooked.length >= 5) {
                        try {
                            timestampNanos = Long.parseLong(cooked[4]);
                            if (cooked.length == 6) {
                                processUid = Integer.parseInt(cooked[5]);
                            }
                        } catch (NumberFormatException e) {
                        }
                    } else {
                        timestampNanos = SystemClock.elapsedRealtimeNanos();
                    }
                    boolean isActive = cooked[2].equals("active");
                    NetworkManagementService networkManagementService = NetworkManagementService.this;
                    int parseInt = Integer.parseInt(cooked[3]);
                    if (isActive) {
                        i = 3;
                    } else {
                        i = NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
                    }
                    networkManagementService.notifyInterfaceClassActivity(parseInt, i, timestampNanos, processUid, NetworkManagementService.DBG);
                    return true;
                case NetdResponseCode.InterfaceAddressChange /*614*/:
                    if (cooked.length < 7 || !cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO].equals("Address")) {
                        throw new IllegalStateException(errorMessage);
                    }
                    String iface = cooked[4];
                    try {
                        LinkAddress address = new LinkAddress(cooked[3], Integer.parseInt(cooked[5]), Integer.parseInt(cooked[6]));
                        if (cooked[2].equals("updated")) {
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
                    if (cooked.length == 6 && cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO].equals("DnsInfo") && cooked[2].equals("servers")) {
                        try {
                            long lifetime = Long.parseLong(cooked[4]);
                            NetworkManagementService.this.notifyInterfaceDnsServerInfo(cooked[3], lifetime, cooked[5].split(","));
                        } catch (NumberFormatException e4) {
                            throw new IllegalStateException(errorMessage);
                        }
                    }
                    return true;
                case NetdResponseCode.RouteChange /*616*/:
                    if (!cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO].equals("Route") || cooked.length < 6) {
                        throw new IllegalStateException(errorMessage);
                    }
                    String via = null;
                    String dev = null;
                    boolean valid = true;
                    for (int i2 = 4; i2 + NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO < cooked.length && valid; i2 += 2) {
                        if (cooked[i2].equals("dev")) {
                            if (dev == null) {
                                dev = cooked[i2 + NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
                            } else {
                                valid = NetworkManagementService.DBG;
                            }
                        } else if (!cooked[i2].equals("via")) {
                            valid = NetworkManagementService.DBG;
                        } else if (via == null) {
                            via = cooked[i2 + NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
                        } else {
                            valid = NetworkManagementService.DBG;
                        }
                    }
                    if (valid) {
                        InetAddress gateway = null;
                        if (via != null) {
                            try {
                                gateway = InetAddress.parseNumericAddress(via);
                            } catch (IllegalArgumentException e5) {
                            }
                        }
                        NetworkManagementService.this.notifyRouteChange(cooked[2], new RouteInfo(new IpPrefix(cooked[3]), gateway, dev));
                        return true;
                    }
                    throw new IllegalStateException(errorMessage);
                case NetdResponseCode.StrictCleartext /*617*/:
                    try {
                        ActivityManagerNative.getDefault().notifyCleartextNetwork(Integer.parseInt(cooked[NetworkManagementService.DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO]), HexDump.hexStringToByteArray(cooked[2]));
                        break;
                    } catch (RemoteException e6) {
                        break;
                    }
                case NetdResponseCode.ApLinkedStaListChangeHISI /*651*/:
                case NetdResponseCode.ApLinkedStaListChangeQCOM /*901*/:
                    if (NetworkManagementService.mNetworkMS != null) {
                        NetworkManagementService.mNetworkMS.handleApLinkedStaListChange(raw, cooked);
                    }
                    return true;
                case NetdResponseCode.DataSpeedSlowDetected /*660*/:
                    if (NetworkManagementService.mNetworkMS != null) {
                        NetworkManagementService.mNetworkMS.sendDataSpeedSlowMessage(cooked, raw);
                    }
                    return true;
                case NetdResponseCode.ApkDownloadUrlDetected /*810*/:
                    if (NetworkManagementService.mNetworkMS != null) {
                        NetworkManagementService.mNetworkMS.sendApkDownloadUrlBroadcast(cooked, raw);
                    }
                    return true;
            }
            return NetworkManagementService.DBG;
        }
    }

    class NetdResponseCode {
        public static final int ApLinkedStaListChangeHISI = 651;
        public static final int ApLinkedStaListChangeQCOM = 901;
        public static final int ApkDownloadUrlDetected = 810;
        public static final int BandwidthControl = 601;
        public static final int ClatdStatusResult = 223;
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

        NetdResponseCode() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.NetworkManagementService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.NetworkManagementService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetworkManagementService.<clinit>():void");
    }

    protected NetworkManagementService(Context context, String socket) {
        this.mConnectedSignal = new CountDownLatch(DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO);
        this.mObservers = new RemoteCallbackList();
        this.mStatsFactory = new NetworkStatsFactory();
        this.mQuotaLock = new Object();
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
        this.mMobileActivityFromRadio = DBG;
        this.mLastPowerStateFromRadio = DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
        this.mLastPowerStateFromWifi = DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
        this.mNetworkActivityListeners = new RemoteCallbackList();
        this.mContext = context;
        this.mFgHandler = new Handler(FgThread.get().getLooper());
        this.mhwNativeDaemonConnector = HwServiceFactory.getHwNativeDaemonConnector();
        this.mhwNativeDaemonConnector.setContext(this.mContext);
        this.mConnector = new NativeDaemonConnector(new NetdCallbackReceiver(), socket, MAX_UID_RANGES_PER_COMMAND, NETD_TAG, HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER, null, FgThread.get().getLooper());
        this.mConnector.setDebug(true);
        this.mThread = new Thread(this.mConnector, NETD_TAG);
        this.mDaemonHandler = new Handler(FgThread.get().getLooper());
        Watchdog.getInstance().addMonitor(this);
    }

    static NetworkManagementService create(Context context, String socket) throws InterruptedException {
        NetworkManagementService service;
        mNetworkMS = HwServiceFactory.getHwNetworkManagermentService();
        if (mNetworkMS != null) {
            service = mNetworkMS.getInstance(context, socket);
            mNetworkMS.setNativeDaemonConnector(service, service.mConnector);
        } else {
            service = new NetworkManagementService(context, socket);
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
        service.connectNativeNetdService();
        return service;
    }

    public static NetworkManagementService create(Context context) throws InterruptedException {
        return create(context, NETD_SERVICE_NAME);
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
            this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
            iBatteryStats = this.mBatteryStats;
            return iBatteryStats;
        }
    }

    public void registerObserver(INetworkManagementEventObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Slog.d(TAG, "registerObserver: pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", observer " + observer);
        this.mObservers.register(observer, Integer.valueOf(Binder.getCallingPid()));
    }

    public void unregisterObserver(INetworkManagementEventObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Slog.d(TAG, "unregisterObserver: pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", observer " + observer);
        this.mObservers.unregister(observer);
    }

    private void notifyInterfaceStatusChanged(String iface, boolean up) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceStatusChanged(iface, up);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyInterfaceLinkStateChanged(String iface, boolean up) {
        int length = this.mObservers.beginBroadcast();
        Slog.d(TAG, "notifyInterfaceLinkStateChanged " + length);
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceLinkStateChanged(iface, up);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyInterfaceAdded(String iface) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceAdded(iface);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyInterfaceRemoved(String iface) {
        this.mActiveAlerts.remove(iface);
        this.mActiveQuotas.remove(iface);
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceRemoved(iface);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyLimitReached(String limitName, String iface) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).limitReached(limitName, iface);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyInterfaceClassActivity(int type, int powerState, long tsNanos, int uid, boolean fromRadio) {
        boolean isMobile = ConnectivityManager.isNetworkTypeMobile(type);
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
                getBatteryStats().noteWifiRadioPowerState(powerState, tsNanos);
            } catch (RemoteException e2) {
            }
        }
        boolean isActive = powerState != 2 ? powerState == 3 ? true : DBG : true;
        if (!(isMobile && !fromRadio && this.mMobileActivityFromRadio)) {
            int length = this.mObservers.beginBroadcast();
            for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                try {
                    Slog.d(TAG, "notifyInterfaceClassActivity: client is " + this.mObservers.getBroadcastItem(i));
                    ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceClassDataActivityChanged(Integer.toString(type), isActive, tsNanos);
                } catch (RemoteException e3) {
                } catch (Throwable th) {
                    this.mObservers.finishBroadcast();
                }
            }
            this.mObservers.finishBroadcast();
        }
        boolean report = DBG;
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

    private void syncFirewallChainLocked(int chain, SparseIntArray uidFirewallRules, String name) {
        int size = uidFirewallRules.size();
        if (size > 0) {
            SparseIntArray rules = uidFirewallRules.clone();
            uidFirewallRules.clear();
            if (DBG) {
                Slog.d(TAG, "Pushing " + size + " active firewall " + name + "UID rules");
            }
            for (int i = 0; i < rules.size(); i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                setFirewallUidRuleLocked(chain, rules.keyAt(i), rules.valueAt(i));
            }
        }
    }

    private void connectNativeNetdService() {
        boolean nativeServiceAvailable = DBG;
        try {
            this.mNetdService = INetd.Stub.asInterface(ServiceManager.getService(NETD_SERVICE_NAME));
            nativeServiceAvailable = this.mNetdService.isAlive();
        } catch (RemoteException e) {
        }
        if (!nativeServiceAvailable) {
            Slog.wtf(TAG, "Can't connect to NativeNetdService netd");
        }
    }

    private void prepareNativeDaemon() {
        this.mBandwidthControlEnabled = DBG;
        if (new File("/proc/net/xt_qtaguid/ctrl").exists()) {
            Slog.d(TAG, "enabling bandwidth control");
            try {
                NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
                objArr[0] = "enable";
                r0.execute("bandwidth", objArr);
                this.mBandwidthControlEnabled = true;
            } catch (NativeDaemonConnectorException e) {
                Log.wtf(TAG, "problem enabling bandwidth controls", e);
            }
        } else {
            Slog.i(TAG, "not enabling bandwidth control");
        }
        SystemProperties.set("net.qtaguid_enabled", this.mBandwidthControlEnabled ? "1" : "0");
        if (this.mBandwidthControlEnabled) {
            try {
                getBatteryStats().noteNetworkStatsEnabled();
            } catch (RemoteException e2) {
            }
        }
        try {
            nativeDaemonConnector = this.mConnector;
            objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = "enable";
            r0.execute("strict", objArr);
            this.mStrictEnabled = true;
        } catch (NativeDaemonConnectorException e3) {
            Log.wtf(TAG, "Failed strict enable", e3);
        }
        synchronized (this.mQuotaLock) {
            int i;
            setDataSaverModeEnabled(this.mDataSaverMode);
            int size = this.mActiveQuotas.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active quota rules");
                }
                HashMap<String, Long> activeQuotas = this.mActiveQuotas;
                this.mActiveQuotas = Maps.newHashMap();
                for (Entry<String, Long> entry : activeQuotas.entrySet()) {
                    setInterfaceQuota((String) entry.getKey(), ((Long) entry.getValue()).longValue());
                }
            }
            size = this.mActiveAlerts.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active alert rules");
                }
                HashMap<String, Long> activeAlerts = this.mActiveAlerts;
                this.mActiveAlerts = Maps.newHashMap();
                for (Entry<String, Long> entry2 : activeAlerts.entrySet()) {
                    setInterfaceAlert((String) entry2.getKey(), ((Long) entry2.getValue()).longValue());
                }
            }
            size = this.mUidRejectOnMetered.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " UIDs to metered whitelist rules");
                }
                SparseBooleanArray uidRejectOnQuota = this.mUidRejectOnMetered;
                this.mUidRejectOnMetered = new SparseBooleanArray();
                for (i = 0; i < uidRejectOnQuota.size(); i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                    setUidMeteredNetworkBlacklist(uidRejectOnQuota.keyAt(i), uidRejectOnQuota.valueAt(i));
                }
            }
            size = this.mUidAllowOnMetered.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " UIDs to metered blacklist rules");
                }
                SparseBooleanArray uidAcceptOnQuota = this.mUidAllowOnMetered;
                this.mUidAllowOnMetered = new SparseBooleanArray();
                for (i = 0; i < uidAcceptOnQuota.size(); i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                    setUidMeteredNetworkWhitelist(uidAcceptOnQuota.keyAt(i), uidAcceptOnQuota.valueAt(i));
                }
            }
            size = this.mUidCleartextPolicy.size();
            if (size > 0) {
                if (DBG) {
                    Slog.d(TAG, "Pushing " + size + " active UID cleartext policies");
                }
                SparseIntArray local = this.mUidCleartextPolicy;
                this.mUidCleartextPolicy = new SparseIntArray();
                for (i = 0; i < local.size(); i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                    setUidCleartextNetworkPolicy(local.keyAt(i), local.valueAt(i));
                }
            }
            setFirewallEnabled(!this.mFirewallEnabled ? LockdownVpnTracker.isEnabled() : true);
            syncFirewallChainLocked(0, this.mUidFirewallRules, "");
            syncFirewallChainLocked(2, this.mUidFirewallStandbyRules, "standby ");
            syncFirewallChainLocked(DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO, this.mUidFirewallDozableRules, "dozable ");
            syncFirewallChainLocked(3, this.mUidFirewallPowerSaveRules, "powersave ");
            if (this.mFirewallChainStates.get(2)) {
                setFirewallChainEnabled(2, true);
            }
            if (this.mFirewallChainStates.get(DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO)) {
                setFirewallChainEnabled(DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO, true);
            }
            if (this.mFirewallChainStates.get(3)) {
                setFirewallChainEnabled(3, true);
            }
        }
    }

    private void notifyAddressUpdated(String iface, LinkAddress address) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).addressUpdated(iface, address);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyAddressRemoved(String iface, LinkAddress address) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).addressRemoved(iface, address);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyInterfaceDnsServerInfo(String iface, long lifetime, String[] addresses) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).interfaceDnsServerInfo(iface, lifetime, addresses);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    private void notifyRouteChange(String action, RouteInfo route) {
        int length = this.mObservers.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                if (action.equals("updated")) {
                    ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).routeUpdated(route);
                } else {
                    ((INetworkManagementEventObserver) this.mObservers.getBroadcastItem(i)).routeRemoved(route);
                }
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mObservers.finishBroadcast();
            }
        }
        this.mObservers.finishBroadcast();
    }

    public String[] listInterfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = "list";
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("interface", objArr), NetdResponseCode.InterfaceListResult);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addUpstreamV6Interface(String iface) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "NetworkManagementService");
        Slog.d(TAG, "addUpstreamInterface(" + iface + ")");
        try {
            Command cmd = new Command("tether", "interface", "add_upstream");
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
            Command cmd = new Command("tether", "interface", "remove_upstream");
            cmd.appendArg(iface);
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw new RemoteException("Cannot remove upstream interface");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InterfaceConfiguration getInterfaceConfig(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonEvent event = this.mConnector.execute("interface", "getcfg", iface);
            event.checkCode(NetdResponseCode.InterfaceGetCfgResult);
            StringTokenizer st = new StringTokenizer(event.getMessage());
            InterfaceConfiguration cfg = new InterfaceConfiguration();
            cfg.setHardwareAddress(st.nextToken(" "));
            InetAddress addr = null;
            int prefixLength = 0;
            try {
                addr = NetworkUtils.numericToInetAddress(st.nextToken());
            } catch (IllegalArgumentException iae) {
                Slog.e(TAG, "Failed to parse ipaddr", iae);
            }
            try {
                prefixLength = Integer.parseInt(st.nextToken());
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
        Command cmd = new Command("interface", "setcfg", iface, linkAddr.getAddress().getHostAddress(), Integer.valueOf(linkAddr.getPrefixLength()));
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
            String str = "interface";
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6privacyextensions";
            objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = iface;
            objArr[2] = enable ? "enable" : "disable";
            nativeDaemonConnector.execute(str, objArr);
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

    public void disableIpv6(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("interface", "ipv6", iface, "disable");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setInterfaceIpv6NdOffload(String iface, boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "interface";
            Object[] objArr = new Object[3];
            objArr[0] = "ipv6ndoffload";
            objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = iface;
            objArr[2] = enable ? "enable" : "disable";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addRoute(int netId, RouteInfo route) {
        modifyRoute("add", "" + netId, route);
    }

    public void removeRoute(int netId, RouteInfo route) {
        modifyRoute("remove", "" + netId, route);
    }

    private void modifyRoute(String action, String netId, RouteInfo route) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Command cmd = new Command("network", "route", action, netId);
        cmd.appendArg(route.getInterface());
        cmd.appendArg(route.getDestination().toString());
        switch (route.getType()) {
            case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                if (route.hasGateway()) {
                    cmd.appendArg(route.getGateway().getHostAddress());
                    break;
                }
                break;
            case H.FINISHED_STARTING /*7*/:
                cmd.appendArg("unreachable");
                break;
            case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                cmd.appendArg("throw");
                break;
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> readRouteList(String filename) {
        Throwable th;
        FileInputStream fileInputStream = null;
        ArrayList<String> list = new ArrayList();
        try {
            FileInputStream fstream = new FileInputStream(filename);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
                while (true) {
                    String s = br.readLine();
                    if (s != null && s.length() != 0) {
                        list.add(s);
                    } else if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e) {
                        }
                    }
                }
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException e2) {
                fileInputStream = fstream;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e3) {
                    }
                }
                return list;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = fstream;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (IOException e5) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return list;
        } catch (Throwable th3) {
            th = th3;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        return list;
    }

    public void setMtu(String iface, int mtu) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonEvent event = this.mConnector.execute("interface", "setmtu", iface, Integer.valueOf(mtu));
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
            Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = "status";
            NativeDaemonEvent event = this.mConnector.execute("ipfwd", objArr);
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
            String str = "ipfwd";
            Object[] objArr = new Object[2];
            objArr[0] = enable ? "enable" : "disable";
            objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = "tethering";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void startTethering(String[] dhcpRange) {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
        objArr[0] = "start";
        Command cmd = new Command("tether", objArr);
        int length = dhcpRange.length;
        while (i < length) {
            cmd.appendArg(dhcpRange[i]);
            i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
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
            Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = "stop";
            this.mConnector.execute("tether", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isTetheringStarted() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = "status";
            NativeDaemonEvent event = this.mConnector.execute("tether", objArr);
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
            List<RouteInfo> routes = new ArrayList();
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
        }
    }

    public String[] listTetheredInterfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("tether", "interface", "list"), NetdResponseCode.TetherInterfaceListResult);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setDnsForwarders(Network network, String[] dns) {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        int netId = network != null ? network.netId : 0;
        Command cmd = new Command("tether", "dns", "set", Integer.valueOf(netId));
        int length = dns.length;
        while (i < length) {
            cmd.appendArg(NetworkUtils.numericToInetAddress(dns[i]).getHostAddress());
            i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
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
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("tether", "dns", "list"), HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private List<InterfaceAddress> excludeLinkLocal(List<InterfaceAddress> addresses) {
        ArrayList<InterfaceAddress> filtered = new ArrayList(addresses.size());
        for (InterfaceAddress ia : addresses) {
            if (!ia.getAddress().isLinkLocalAddress()) {
                filtered.add(ia);
            }
        }
        return filtered;
    }

    private void modifyInterfaceForward(boolean add, String fromIface, String toIface) {
        String str = "ipfwd";
        Object[] objArr = new Object[3];
        objArr[0] = add ? "add" : "remove";
        objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = fromIface;
        objArr[2] = toIface;
        try {
            this.mConnector.execute(new Command(str, objArr));
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
        modifyInterfaceForward(DBG, fromIface, toIface);
    }

    private void modifyNat(String action, String internalInterface, String externalInterface) throws SocketException {
        Command cmd = new Command("nat", action, internalInterface, externalInterface);
        NetworkInterface internalNetworkInterface = NetworkInterface.getByName(internalInterface);
        if (internalNetworkInterface == null) {
            cmd.appendArg("0");
        } else {
            List<InterfaceAddress> interfaceAddresses = excludeLinkLocal(internalNetworkInterface.getInterfaceAddresses());
            cmd.appendArg(Integer.valueOf(interfaceAddresses.size()));
            for (InterfaceAddress ia : interfaceAddresses) {
                cmd.appendArg(NetworkUtils.getNetworkPart(ia.getAddress(), ia.getNetworkPrefixLength()).getHostAddress() + "/" + ia.getNetworkPrefixLength());
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
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("list_ttys", new Object[0]), HdmiCecKeycode.CEC_KEYCODE_F1_BLUE);
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

    private void executeOrLogWithMessage(String command, Object[] args, int expectedResponseCode, String expectedResponseMessage, String logMsg) throws NativeDaemonConnectorException {
        NativeDaemonEvent event = this.mConnector.execute(command, args);
        if (event.getCode() != expectedResponseCode || !event.getMessage().equals(expectedResponseMessage)) {
            Log.e(TAG, logMsg + ": event = " + event);
        }
    }

    public void startAccessPoint(WifiConfiguration wifiConfig, String wlanIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        String logMsg = "startAccessPoint Error setting up softap";
        try {
            if (mNetworkMS != null) {
                mNetworkMS.startAccessPointWithChannel(wifiConfig, wlanIface);
                return;
            }
            executeOrLogWithMessage(SOFT_AP_COMMAND, wifiConfig == null ? new Object[]{"set", wlanIface} : new Object[]{"set", wlanIface, wifiConfig.SSID, "broadcast", Integer.toString(wifiConfig.apChannel), getSecurityType(wifiConfig), new SensitiveArg(wifiConfig.preSharedKey)}, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, logMsg);
            Object[] args = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            args[0] = "startap";
            executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, "startAccessPoint Error starting softap");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private static String getSecurityType(WifiConfiguration wifiConfig) {
        switch (wifiConfig.getAuthType()) {
            case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                return "wpa-psk";
            case H.DO_TRAVERSAL /*4*/:
                return "wpa2-psk";
            default:
                return "open";
        }
    }

    public void wifiFirmwareReload(String wlanIface, String mode) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            executeOrLogWithMessage(SOFT_AP_COMMAND, new Object[]{"fwreload", wlanIface, mode}, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, "wifiFirmwareReload Error reloading " + wlanIface + " fw in " + mode + " mode");
            this.mConnector.waitForCallbacks();
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void stopAccessPoint(String wlanIface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] args = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
        args[0] = "stopap";
        try {
            executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, "stopAccessPoint Error stopping softap");
            wifiFirmwareReload(wlanIface, "STA");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setAccessPoint(WifiConfiguration wifiConfig, String wlanIface) {
        Object[] args;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        String logMsg = "startAccessPoint Error setting up softap";
        if (wifiConfig == null) {
            try {
                args = new Object[]{"set", wlanIface};
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        args = new Object[]{"set", wlanIface, wifiConfig.SSID, "broadcast", "6", getSecurityType(wifiConfig), new SensitiveArg(wifiConfig.preSharedKey)};
        executeOrLogWithMessage(SOFT_AP_COMMAND, args, NetdResponseCode.SoftapStatusResult, SOFT_AP_COMMAND_SUCCESS, logMsg);
    }

    public void addIdleTimer(String iface, int timeout, int type) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Slog.d(TAG, "Adding idletimer");
        }
        synchronized (this.mIdleTimerLock) {
            IdleTimerParams params = (IdleTimerParams) this.mActiveIdleTimers.get(iface);
            if (params != null) {
                params.networkCount += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
                return;
            }
            try {
                this.mConnector.execute("idletimer", "add", iface, Integer.toString(timeout), Integer.toString(type));
                this.mActiveIdleTimers.put(iface, new IdleTimerParams(timeout, type));
                if (ConnectivityManager.isNetworkTypeMobile(type)) {
                    this.mNetworkActive = DBG;
                }
                this.mDaemonHandler.post(new AnonymousClass1(type));
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    public void removeIdleTimer(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (DBG) {
            Slog.d(TAG, "Removing idletimer");
        }
        synchronized (this.mIdleTimerLock) {
            IdleTimerParams params = (IdleTimerParams) this.mActiveIdleTimers.get(iface);
            if (params != null) {
                int i = params.networkCount - 1;
                params.networkCount = i;
                if (i <= 0) {
                    try {
                        this.mConnector.execute("idletimer", "remove", iface, Integer.toString(params.timeout), Integer.toString(params.type));
                        this.mActiveIdleTimers.remove(iface);
                        this.mDaemonHandler.post(new AnonymousClass2(params));
                        return;
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
                if (this.mActiveQuotas.containsKey(iface)) {
                    throw new IllegalStateException("iface " + iface + " already has quota");
                }
                try {
                    this.mConnector.execute("bandwidth", "setiquota", iface, Long.valueOf(quotaBytes));
                    this.mActiveQuotas.put(iface, Long.valueOf(quotaBytes));
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
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
                        return;
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public void setInterfaceAlert(String iface, long alertBytes) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (!this.mBandwidthControlEnabled) {
            return;
        }
        if (this.mActiveQuotas.containsKey(iface)) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveAlerts.containsKey(iface)) {
                    throw new IllegalStateException("iface " + iface + " already has alert");
                }
                try {
                    this.mConnector.execute("bandwidth", "setinterfacealert", iface, Long.valueOf(alertBytes));
                    this.mActiveAlerts.put(iface, Long.valueOf(alertBytes));
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            return;
        }
        throw new IllegalStateException("setting alert requires existing quota on iface");
    }

    public void removeInterfaceAlert(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            synchronized (this.mQuotaLock) {
                if (this.mActiveAlerts.containsKey(iface)) {
                    try {
                        this.mConnector.execute("bandwidth", "removeinterfacealert", iface);
                        this.mActiveAlerts.remove(iface);
                        return;
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

    private void setUidOnMeteredNetworkList(SparseBooleanArray quotaList, int uid, boolean blacklist, boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mBandwidthControlEnabled) {
            String chain = blacklist ? "naughtyapps" : "niceapps";
            String suffix = enable ? "add" : "remove";
            synchronized (this.mQuotaLock) {
                if (quotaList.get(uid, DBG) == enable) {
                    return;
                }
                try {
                    this.mConnector.execute("bandwidth", suffix + chain, Integer.valueOf(uid));
                    if (enable) {
                        quotaList.put(uid, true);
                    } else {
                        quotaList.delete(uid);
                    }
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
        }
    }

    public void setUidMeteredNetworkBlacklist(int uid, boolean enable) {
        setUidOnMeteredNetworkList(this.mUidRejectOnMetered, uid, true, enable);
    }

    public void setUidMeteredNetworkWhitelist(int uid, boolean enable) {
        setUidOnMeteredNetworkList(this.mUidAllowOnMetered, uid, DBG, enable);
    }

    public boolean setDataSaverModeEnabled(boolean enable) {
        if (DBG) {
            Log.d(TAG, "setDataSaverMode: " + enable);
        }
        synchronized (this.mQuotaLock) {
            if (this.mDataSaverMode == enable) {
                Log.w(TAG, "setDataSaverMode(): already " + this.mDataSaverMode);
                return true;
            }
            try {
                boolean changed = this.mNetdService.bandwidthEnableDataSaver(enable);
                if (changed) {
                    this.mDataSaverMode = enable;
                } else {
                    Log.w(TAG, "setDataSaverMode(" + enable + "): netd command silently failed");
                }
                return changed;
            } catch (RemoteException e) {
                Log.w(TAG, "setDataSaverMode(" + enable + "): netd command failed", e);
                return DBG;
            }
        }
    }

    public void setAllowOnlyVpnForUids(boolean add, UidRange[] uidRanges) throws ServiceSpecificException {
        try {
            this.mNetdService.networkRejectNonSecureVpn(add, uidRanges);
        } catch (ServiceSpecificException e) {
            Log.w(TAG, "setAllowOnlyVpnForUids(" + add + ", " + Arrays.toString(uidRanges) + ")" + ": netd command failed", e);
            throw e;
        } catch (RemoteException e2) {
            Log.w(TAG, "setAllowOnlyVpnForUids(" + add + ", " + Arrays.toString(uidRanges) + ")" + ": netd command failed", e2);
            throw e2.rethrowAsRuntimeException();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUidCleartextNetworkPolicy(int uid, int policy) {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        }
        synchronized (this.mQuotaLock) {
            if (this.mUidCleartextPolicy.get(uid, 0) == policy) {
            } else if (this.mStrictEnabled) {
                String policyString;
                switch (policy) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                        policyString = "accept";
                        break;
                    case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                        policyString = "log";
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        policyString = "reject";
                        this.mConnector.execute("strict", "set_uid_cleartext_policy", Integer.valueOf(uid), policyString);
                        this.mUidCleartextPolicy.put(uid, policy);
                    default:
                        throw new IllegalArgumentException("Unknown policy " + policy);
                }
                try {
                    this.mConnector.execute("strict", "set_uid_cleartext_policy", Integer.valueOf(uid), policyString);
                    this.mUidCleartextPolicy.put(uid, policy);
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            } else {
                this.mUidCleartextPolicy.put(uid, policy);
            }
        }
    }

    public boolean isBandwidthControlEnabled() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return this.mBandwidthControlEnabled;
    }

    public NetworkStats getNetworkStatsUidDetail(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.readNetworkStatsDetail(uid, null, -1, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NetworkStats getNetworkStatsTethering() {
        NativeDaemonEvent event;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO);
        try {
            Object[] objArr = new Object[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            objArr[0] = "gettetherstats";
            NativeDaemonEvent[] events = this.mConnector.executeForList("bandwidth", objArr);
            int length = events.length;
            for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                event = events[i];
                if (event.getCode() == HdmiCecKeycode.CEC_KEYCODE_F2_RED) {
                    StringTokenizer tok = new StringTokenizer(event.getMessage());
                    String ifaceIn = tok.nextToken();
                    String ifaceOut = tok.nextToken();
                    NetworkStats.Entry entry = new NetworkStats.Entry();
                    entry.iface = ifaceOut;
                    entry.uid = -5;
                    entry.set = 0;
                    entry.tag = 0;
                    entry.rxBytes = Long.parseLong(tok.nextToken());
                    entry.rxPackets = Long.parseLong(tok.nextToken());
                    entry.txBytes = Long.parseLong(tok.nextToken());
                    entry.txPackets = Long.parseLong(tok.nextToken());
                    stats.combineValues(entry);
                }
            }
            return stats;
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("problem parsing tethering stats: " + event);
        } catch (NumberFormatException e2) {
            throw new IllegalStateException("problem parsing tethering stats: " + event);
        } catch (NativeDaemonConnectorException e3) {
            throw e3.rethrowAsParcelableException();
        }
    }

    public void setDnsConfigurationForNetwork(int netId, String[] servers, String domains) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        ContentResolver resolver = this.mContext.getContentResolver();
        int sampleValidity = Global.getInt(resolver, "dns_resolver_sample_validity_seconds", DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS);
        if (sampleValidity < 0 || sampleValidity > 65535) {
            Slog.w(TAG, "Invalid sampleValidity=" + sampleValidity + ", using default=" + DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS);
            sampleValidity = DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS;
        }
        int successThreshold = Global.getInt(resolver, "dns_resolver_success_threshold_percent", DNS_RESOLVER_DEFAULT_SUCCESS_THRESHOLD_PERCENT);
        if (successThreshold < 0 || successThreshold > 100) {
            Slog.w(TAG, "Invalid successThreshold=" + successThreshold + ", using default=" + DNS_RESOLVER_DEFAULT_SUCCESS_THRESHOLD_PERCENT);
            successThreshold = DNS_RESOLVER_DEFAULT_SUCCESS_THRESHOLD_PERCENT;
        }
        int minSamples = Global.getInt(resolver, "dns_resolver_min_samples", DNS_RESOLVER_DEFAULT_MIN_SAMPLES);
        int maxSamples = Global.getInt(resolver, "dns_resolver_max_samples", DNS_RESOLVER_DEFAULT_MAX_SAMPLES);
        if (minSamples >= 0 && minSamples <= maxSamples) {
            if (maxSamples > DNS_RESOLVER_DEFAULT_MAX_SAMPLES) {
            }
            this.mNetdService.setResolverConfiguration(netId, servers, domains != null ? new String[0] : domains.split(" "), new int[]{sampleValidity, successThreshold, minSamples, maxSamples});
        }
        Slog.w(TAG, "Invalid sample count (min, max)=(" + minSamples + ", " + maxSamples + "), using default=(" + DNS_RESOLVER_DEFAULT_MIN_SAMPLES + ", " + DNS_RESOLVER_DEFAULT_MAX_SAMPLES + ")");
        minSamples = DNS_RESOLVER_DEFAULT_MIN_SAMPLES;
        maxSamples = DNS_RESOLVER_DEFAULT_MAX_SAMPLES;
        if (domains != null) {
        }
        try {
            this.mNetdService.setResolverConfiguration(netId, servers, domains != null ? new String[0] : domains.split(" "), new int[]{sampleValidity, successThreshold, minSamples, maxSamples});
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDnsServersForNetwork(int netId, String[] servers, String domains) {
        Command cmd;
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (servers.length > 0) {
            String str = "resolver";
            Object[] objArr = new Object[3];
            objArr[0] = "setnetdns";
            objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = Integer.valueOf(netId);
            if (domains == null) {
                domains = "";
            }
            objArr[2] = domains;
            cmd = new Command(str, objArr);
            int length = servers.length;
            while (i < length) {
                InetAddress a = NetworkUtils.numericToInetAddress(servers[i]);
                if (!a.isAnyLocalAddress()) {
                    cmd.appendArg(a.getHostAddress());
                }
                i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
            }
        } else {
            cmd = new Command("resolver", "clearnetdns", Integer.valueOf(netId));
        }
        try {
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addVpnUidRanges(int netId, UidRange[] ranges) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = SoundModelContract.KEY_USERS;
        argv[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = "add";
        argv[2] = Integer.valueOf(netId);
        int argc = 3;
        for (int i = 0; i < ranges.length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            int argc2 = argc + DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
            argv[argc] = ranges[i].toString();
            if (i == ranges.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void removeVpnUidRanges(int netId, UidRange[] ranges) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = SoundModelContract.KEY_USERS;
        argv[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = "remove";
        argv[2] = Integer.valueOf(netId);
        int argc = 3;
        for (int i = 0; i < ranges.length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            int argc2 = argc + DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
            argv[argc] = ranges[i].toString();
            if (i == ranges.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void setFirewallEnabled(boolean enabled) {
        enforceSystemUid();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "firewall";
            Object[] objArr = new Object[2];
            objArr[0] = "enable";
            objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = enabled ? "whitelist" : "blacklist";
            nativeDaemonConnector.execute(str, objArr);
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
        String rule = allow ? "allow" : "deny";
        try {
            this.mConnector.execute("firewall", "set_interface_rule", iface, rule);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setFirewallEgressSourceRule(String addr, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        String rule = allow ? "allow" : "deny";
        try {
            this.mConnector.execute("firewall", "set_egress_source_rule", addr, rule);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setFirewallEgressDestRule(String addr, int port, boolean allow) {
        enforceSystemUid();
        Preconditions.checkState(this.mFirewallEnabled);
        String rule = allow ? "allow" : "deny";
        try {
            this.mConnector.execute("firewall", "set_egress_dest_rule", addr, Integer.valueOf(port), rule);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private void closeSocketsForFirewallChainLocked(int chain, String chainName) {
        UidRange[] ranges;
        int[] exemptUids;
        SparseIntArray rules = getUidFirewallRules(chain);
        int numUids = 0;
        int i;
        if (getFirewallType(chain) == 0) {
            ranges = new UidRange[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO];
            ranges[0] = new UidRange(AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
            exemptUids = new int[rules.size()];
            for (i = 0; i < exemptUids.length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                if (rules.valueAt(i) == DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                    exemptUids[numUids] = rules.keyAt(i);
                    numUids += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
                }
            }
            if (numUids != exemptUids.length) {
                exemptUids = Arrays.copyOf(exemptUids, numUids);
            }
        } else {
            ranges = new UidRange[rules.size()];
            for (i = 0; i < ranges.length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                if (rules.valueAt(i) == 2) {
                    int uid = rules.keyAt(i);
                    ranges[numUids] = new UidRange(uid, uid);
                    numUids += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
                }
            }
            if (numUids != ranges.length) {
                ranges = (UidRange[]) Arrays.copyOf(ranges, numUids);
            }
            exemptUids = new int[0];
        }
        try {
            this.mNetdService.socketDestroy(ranges, exemptUids);
        } catch (Exception e) {
            Slog.e(TAG, "Error closing sockets after enabling chain " + chainName + ": " + e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFirewallChainEnabled(int chain, boolean enable) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            if (this.mFirewallChainStates.get(chain) == enable) {
                return;
            }
            String chainName;
            this.mFirewallChainStates.put(chain, enable);
            String operation = enable ? "enable_chain" : "disable_chain";
            switch (chain) {
                case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                    chainName = "dozable";
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    chainName = "standby";
                    this.mConnector.execute("firewall", operation, chainName);
                    if (enable) {
                        if (DBG) {
                            Slog.d(TAG, "Closing sockets after enabling chain " + chainName);
                        }
                        closeSocketsForFirewallChainLocked(chain, chainName);
                        break;
                    }
                case H.REPORT_LOSING_FOCUS /*3*/:
                    chainName = "powersave";
                    this.mConnector.execute("firewall", operation, chainName);
                    if (enable) {
                        if (DBG) {
                            Slog.d(TAG, "Closing sockets after enabling chain " + chainName);
                        }
                        closeSocketsForFirewallChainLocked(chain, chainName);
                        break;
                    }
                default:
                    throw new IllegalArgumentException("Bad child chain: " + chain);
            }
            try {
                this.mConnector.execute("firewall", operation, chainName);
                if (enable) {
                    if (DBG) {
                        Slog.d(TAG, "Closing sockets after enabling chain " + chainName);
                    }
                    closeSocketsForFirewallChainLocked(chain, chainName);
                }
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    private int getFirewallType(int chain) {
        int i = 0;
        switch (chain) {
            case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                return 0;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
            case H.REPORT_LOSING_FOCUS /*3*/:
                return 0;
            default:
                if (!isFirewallEnabled()) {
                    i = DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
                }
                return i;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFirewallUidRules(int chain, int[] uids, int[] rules) {
        enforceSystemUid();
        synchronized (this.mQuotaLock) {
            int index;
            SparseIntArray uidFirewallRules = getUidFirewallRules(chain);
            SparseIntArray newRules = new SparseIntArray();
            for (index = uids.length - 1; index >= 0; index--) {
                int uid = uids[index];
                int rule = rules[index];
                updateFirewallUidRuleLocked(chain, uid, rule);
                newRules.put(uid, rule);
            }
            SparseIntArray rulesToRemove = new SparseIntArray();
            for (index = uidFirewallRules.size() - 1; index >= 0; index--) {
                uid = uidFirewallRules.keyAt(index);
                if (newRules.indexOfKey(uid) < 0) {
                    rulesToRemove.put(uid, 0);
                }
            }
            for (index = rulesToRemove.size() - 1; index >= 0; index--) {
                updateFirewallUidRuleLocked(chain, rulesToRemove.keyAt(index), 0);
            }
            switch (chain) {
                case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                    this.mNetdService.firewallReplaceUidChain("fw_dozable", true, uids);
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    this.mNetdService.firewallReplaceUidChain("fw_standby", DBG, uids);
                    break;
                case H.REPORT_LOSING_FOCUS /*3*/:
                    this.mNetdService.firewallReplaceUidChain("fw_powersave", true, uids);
                    break;
                default:
                    try {
                        Slog.d(TAG, "setFirewallUidRules() called on invalid chain: " + chain);
                        break;
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Error flushing firewall chain " + chain, e);
                        break;
                    }
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

    private boolean updateFirewallUidRuleLocked(int chain, int uid, int rule) {
        boolean z = DBG;
        SparseIntArray uidFirewallRules = getUidFirewallRules(chain);
        int oldUidFirewallRule = uidFirewallRules.get(uid, 0);
        if (DBG) {
            Slog.d(TAG, "oldRule = " + oldUidFirewallRule + ", newRule=" + rule + " for uid=" + uid + " on chain " + chain);
        }
        if (oldUidFirewallRule == rule) {
            if (DBG) {
                Slog.d(TAG, "!!!!! Skipping change");
            }
            return DBG;
        }
        String ruleName = getFirewallRuleName(chain, rule);
        String oldRuleName = getFirewallRuleName(chain, oldUidFirewallRule);
        if (rule == 0) {
            uidFirewallRules.delete(uid);
        } else {
            uidFirewallRules.put(uid, rule);
        }
        if (!ruleName.equals(oldRuleName)) {
            z = true;
        }
        return z;
    }

    private String getFirewallRuleName(int chain, int rule) {
        if (getFirewallType(chain) == 0) {
            if (rule == DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
                return "allow";
            }
            return "deny";
        } else if (rule == 2) {
            return "deny";
        } else {
            return "allow";
        }
    }

    private SparseIntArray getUidFirewallRules(int chain) {
        switch (chain) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return this.mUidFirewallRules;
            case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                return this.mUidFirewallDozableRules;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return this.mUidFirewallStandbyRules;
            case H.REPORT_LOSING_FOCUS /*3*/:
                return this.mUidFirewallPowerSaveRules;
            default:
                throw new IllegalArgumentException("Unknown chain:" + chain);
        }
    }

    public String getFirewallChainName(int chain) {
        switch (chain) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "none";
            case DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO /*1*/:
                return "dozable";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "standby";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "powersave";
            default:
                throw new IllegalArgumentException("Unknown chain:" + chain);
        }
    }

    private static void enforceSystemUid() {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
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
        boolean isEmpty;
        synchronized (this.mNetworkActivityListeners) {
            isEmpty = !this.mNetworkActive ? this.mActiveIdleTimers.isEmpty() : true;
        }
        return isEmpty;
    }

    private void reportNetworkActive() {
        int length = this.mNetworkActivityListeners.beginBroadcast();
        for (int i = 0; i < length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            try {
                ((INetworkActivityListener) this.mNetworkActivityListeners.getBroadcastItem(i)).onNetworkActive();
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mNetworkActivityListeners.finishBroadcast();
            }
        }
        this.mNetworkActivityListeners.finishBroadcast();
    }

    public void monitor() {
        if (this.mConnector != null) {
            this.mConnector.monitor();
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
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
            dumpUidRuleOnQuotaLocked(pw, "blacklist", this.mUidRejectOnMetered);
            dumpUidRuleOnQuotaLocked(pw, "whitelist", this.mUidAllowOnMetered);
        }
        synchronized (this.mUidFirewallRules) {
            dumpUidFirewallRule(pw, "", this.mUidFirewallRules);
        }
        pw.print("UID firewall standby chain enabled: ");
        pw.println(this.mFirewallChainStates.get(2));
        synchronized (this.mUidFirewallStandbyRules) {
            dumpUidFirewallRule(pw, "standby", this.mUidFirewallStandbyRules);
        }
        pw.print("UID firewall dozable chain enabled: ");
        pw.println(this.mFirewallChainStates.get(DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO));
        synchronized (this.mUidFirewallDozableRules) {
            dumpUidFirewallRule(pw, "dozable", this.mUidFirewallDozableRules);
        }
        pw.println("UID firewall powersave chain enabled: " + this.mFirewallChainStates.get(3));
        synchronized (this.mUidFirewallPowerSaveRules) {
            dumpUidFirewallRule(pw, "powersave", this.mUidFirewallPowerSaveRules);
        }
        synchronized (this.mIdleTimerLock) {
            pw.println("Idle timers:");
            for (Entry<String, IdleTimerParams> ent : this.mActiveIdleTimers.entrySet()) {
                pw.print("  ");
                pw.print((String) ent.getKey());
                pw.println(":");
                IdleTimerParams params = (IdleTimerParams) ent.getValue();
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
            return;
        }
        try {
            pw.println(this.mNetdService.isAlive() ? "alive" : "dead");
        } catch (RemoteException e) {
            pw.println("unreachable");
        }
    }

    private void dumpUidRuleOnQuotaLocked(PrintWriter pw, String name, SparseBooleanArray list) {
        pw.print("UID bandwith control ");
        pw.print(name);
        pw.print(" rule: [");
        int size = list.size();
        for (int i = 0; i < size; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
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
        for (int i = 0; i < size; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
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
                return;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        this.mConnector.execute("network", "create", Integer.valueOf(netId));
    }

    public void createVirtualNetwork(int netId, boolean hasDNS, boolean secure) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "network";
            Object[] objArr = new Object[5];
            objArr[0] = "create";
            objArr[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = Integer.valueOf(netId);
            objArr[2] = "vpn";
            objArr[3] = hasDNS ? "1" : "0";
            objArr[4] = secure ? "1" : "0";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void removeNetwork(int netId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", "destroy", Integer.valueOf(netId));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addInterfaceToNetwork(String iface, int netId) {
        modifyInterfaceInNetwork("add", "" + netId, iface);
    }

    public void removeInterfaceFromNetwork(String iface, int netId) {
        modifyInterfaceInNetwork("remove", "" + netId, iface);
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
        Command cmd = new Command("network", "route", "legacy", Integer.valueOf(uid), "add", Integer.valueOf(netId));
        LinkAddress la = routeInfo.getDestinationLinkAddress();
        cmd.appendArg(routeInfo.getInterface());
        cmd.appendArg(la.getAddress().getHostAddress() + "/" + la.getPrefixLength());
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
            this.mConnector.execute("network", "default", "set", Integer.valueOf(netId));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void clearDefaultNetId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            this.mConnector.execute("network", "default", "clear");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setNetworkPermission(int netId, String permission) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (permission != null) {
            try {
                this.mConnector.execute("network", "permission", "network", "set", permission, Integer.valueOf(netId));
                return;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        this.mConnector.execute("network", "permission", "network", "clear", Integer.valueOf(netId));
    }

    public void setPermission(String permission, int[] uids) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[14];
        argv[0] = "permission";
        argv[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = "user";
        argv[2] = "set";
        argv[3] = permission;
        int argc = 4;
        for (int i = 0; i < uids.length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            int argc2 = argc + DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
            argv[argc] = Integer.valueOf(uids[i]);
            if (i == uids.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 4;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
        }
    }

    public void clearPermission(int[] uids) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Object[] argv = new Object[13];
        argv[0] = "permission";
        argv[DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO] = "user";
        argv[2] = "clear";
        int argc = 3;
        for (int i = 0; i < uids.length; i += DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO) {
            int argc2 = argc + DAEMON_MSG_MOBILE_CONN_REAL_TIME_INFO;
            argv[argc] = Integer.valueOf(uids[i]);
            if (i == uids.length - 1 || argc2 == argv.length) {
                try {
                    this.mConnector.execute("network", Arrays.copyOf(argv, argc2));
                    argc = 3;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                }
            }
            argc = argc2;
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
}
