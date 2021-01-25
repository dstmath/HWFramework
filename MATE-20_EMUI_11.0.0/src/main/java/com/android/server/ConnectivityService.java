package com.android.server;

import android.app.BroadcastOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.net.CaptivePortal;
import android.net.ConnectionInfo;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.IDnsResolver;
import android.net.IIpConnectivityMetrics;
import android.net.INetd;
import android.net.INetdEventCallback;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkMonitor;
import android.net.INetworkMonitorCallbacks;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.ISocketKeepaliveCallback;
import android.net.ITetheringEventCallback;
import android.net.InetAddresses;
import android.net.IpMemoryStore;
import android.net.IpPrefix;
import android.net.LinkProperties;
import android.net.MatchAllNetworkSpecifier;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkMonitorManager;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.NetworkStackClient;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.NetworkWatchlistManager;
import android.net.PrivateDnsConfigParcel;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.netlink.InetDiagMessage;
import android.net.shared.NetworkMonitorUtils;
import android.net.shared.PrivateDnsConfig;
import android.net.util.MultinetworkPolicyTracker;
import android.net.util.NetdService;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.security.KeyStore;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.WakeupMessage;
import com.android.internal.util.XmlUtils;
import com.android.server.AbstractConnectivityService;
import com.android.server.BatteryService;
import com.android.server.ConnectivityService;
import com.android.server.am.BatteryStatsService;
import com.android.server.connectivity.AutodestructReference;
import com.android.server.connectivity.DataConnectionStats;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.IHwConnectivityServiceInner;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.connectivity.KeepaliveTracker;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.connectivity.MockableSystemProperties;
import com.android.server.connectivity.MultipathPolicyTracker;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkDiagnostics;
import com.android.server.connectivity.NetworkNotificationManager;
import com.android.server.connectivity.PermissionMonitor;
import com.android.server.connectivity.ProxyTracker;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.Vpn;
import com.android.server.connectivity.tethering.TetheringDependencies;
import com.android.server.net.BaseNetdEventCallback;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.net.HwNetworkPolicyManagerInternal;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.utils.PriorityDump;
import com.google.android.collect.Lists;
import com.huawei.server.connectivity.IHwConnectivityServiceEx;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConnectivityService extends AbstractConnectivityService implements IHwConnectivityServiceInner, PendingIntent.OnFinished {
    private static final String ACTION_PKT_CNT_SAMPLE_INTERVAL_ELAPSED = "android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED";
    private static final String ATTR_MCC = "mcc";
    private static final String ATTR_MNC = "mnc";
    private static final boolean DBG = true;
    private static final boolean DDBG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_CAPTIVE_PORTAL_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final int DEFAULT_LINGER_DELAY_MS = 30000;
    @VisibleForTesting
    protected static final String DEFAULT_TCP_BUFFER_SIZES = "4096,87380,110208,4096,16384,110208";
    private static final String DEFAULT_TCP_RWND_KEY = "net.tcp.default_init_rwnd";
    private static final String DIAG_ARG = "--diag";
    private static final boolean ENABLE_WIFI_LTE_CE = SystemProperties.getBoolean("ro.config.enable_wl_coexist", false);
    private static final int EVENT_APPLY_GLOBAL_HTTP_PROXY = 9;
    private static final int EVENT_CLEAR_NET_TRANSITION_WAKELOCK = 8;
    private static final int EVENT_CONFIGURE_ALWAYS_ON_NETWORKS = 30;
    private static final int EVENT_DATA_SAVER_CHANGED = 40;
    private static final int EVENT_EXPIRE_NET_TRANSITION_WAKELOCK = 24;
    public static final int EVENT_NETWORK_TESTED = 41;
    public static final int EVENT_PRIVATE_DNS_CONFIG_RESOLVED = 42;
    private static final int EVENT_PRIVATE_DNS_SETTINGS_CHANGED = 37;
    private static final int EVENT_PRIVATE_DNS_VALIDATION_UPDATE = 38;
    private static final int EVENT_PROMPT_UNVALIDATED = 29;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 43;
    private static final int EVENT_PROXY_HAS_CHANGED = 16;
    private static final int EVENT_REGISTER_NETWORK_AGENT = 18;
    private static final int EVENT_REGISTER_NETWORK_FACTORY = 17;
    private static final int EVENT_REGISTER_NETWORK_LISTENER = 21;
    private static final int EVENT_REGISTER_NETWORK_LISTENER_WITH_INTENT = 31;
    private static final int EVENT_REGISTER_NETWORK_REQUEST = 19;
    private static final int EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT = 26;
    private static final int EVENT_RELEASE_NETWORK_REQUEST = 22;
    private static final int EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT = 27;
    private static final int EVENT_REVALIDATE_NETWORK = 36;
    private static final int EVENT_SET_ACCEPT_PARTIAL_CONNECTIVITY = 45;
    private static final int EVENT_SET_ACCEPT_UNVALIDATED = 28;
    private static final int EVENT_SET_AVOID_UNVALIDATED = 35;
    private static final int EVENT_SWITCH_NETWORK_STATE_CACHE_ON = 46;
    private static final int EVENT_SYSTEM_READY = 25;
    private static final int EVENT_TIMEOUT_NETWORK_REQUEST = 20;
    public static final int EVENT_TIMEOUT_NOTIFICATION = 44;
    private static final int EVENT_UID_RULES_CHANGED = 39;
    private static final int EVENT_UNREGISTER_NETWORK_FACTORY = 23;
    private static final boolean HW_DEBUGGABLE = Build.IS_DEBUGGABLE;
    private static final String IFACE_ETH0 = "eth0";
    private static final String IFACE_ETH1 = "eth1";
    protected static final boolean IS_SUPPORT_LINGER_DELAY;
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final boolean LOGD_BLOCKED_NETWORKINFO = true;
    private static final int MAX_NETWORK_INFO_LOGS = 40;
    private static final int MAX_NETWORK_REQUESTS_PER_UID = 100;
    private static final int MAX_NETWORK_REQUEST_LOGS = 20;
    private static final int MAX_NET_ID = 64511;
    private static final int MAX_WAKELOCK_LOGS = 20;
    private static final int MIN_NET_ID = 100;
    private static final String MOBILE_DATA_ALWAYS_ON_FOR_MPLINK = "mobile_data_always_on_for_mplink";
    private static final String NETWORK_ARG = "networks";
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    private static final int NETWORK_STATE_BLOCK = 1;
    private static final int NETWORK_STATE_CACHE_LIFE_TIME = 2000;
    private static final int NETWORK_STATE_INVALID = -1;
    private static final int PROMPT_UNVALIDATED_DELAY_MS = 8000;
    public static final int PROVISIONING_NOTIFICATION_HIDE = 0;
    public static final int PROVISIONING_NOTIFICATION_SHOW = 1;
    private static final String PROVISIONING_URL_PATH = "/data/misc/radio/provisioning_urls.xml";
    private static final String REQUEST_ARG = "requests";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 60000;
    public static final String SHORT_ARG = "--short";
    private static final String TAG = ConnectivityService.class.getSimpleName();
    private static final String TAG_PROVISIONING_URL = "provisioningUrl";
    private static final String TAG_PROVISIONING_URLS = "provisioningUrls";
    private static final String TETHERING_ARG = "tethering";
    private static final int TIMEOUT_NOTIFICATION_DELAY_MS = 20000;
    private static final boolean VDBG = true;
    private static boolean isEthernetPriority = "ethernet".equals(SystemProperties.get("persist.network.firstpriority", "ethernet"));
    private static boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(new Class[]{AsyncChannel.class, ConnectivityService.class, NetworkAgent.class, NetworkAgentInfo.class});
    @GuardedBy({"mBandwidthRequests"})
    private final SparseArray<Integer> mBandwidthRequests;
    @GuardedBy({"mBlockedAppUids"})
    private final HashSet<Integer> mBlockedAppUids;
    private final Context mContext;
    private String mCurrentTcpBufferSizes;
    protected HwCustConnectivityService mCust;
    private INetworkManagementEventObserver mDataActivityObserver;
    private int mDefaultInetConditionPublished;
    private final NetworkRequest mDefaultMobileDataRequest;
    private final NetworkRequest mDefaultRequest;
    private final NetworkRequest mDefaultWifiRequest;
    private final DnsManager mDnsManager;
    @VisibleForTesting
    protected IDnsResolver mDnsResolver;
    private final InternalHandler mHandler;
    @VisibleForTesting
    protected final HandlerThread mHandlerThread;
    IHwConnectivityServiceEx mHwCSEx;
    private HwNetworkPolicyManagerInternal mHwPolicyManagerInternal;
    private Intent mInitialBroadcast;
    private BroadcastReceiver mIntentReceiver;
    private KeepaliveTracker mKeepaliveTracker;
    private KeyStore mKeyStore;
    private long mLastWakeLockAcquireTimestamp;
    private final LegacyTypeTracker mLegacyTypeTracker;
    @VisibleForTesting
    protected int mLingerDelayMs;
    private LingerMonitor mLingerMonitor;
    @GuardedBy({"mVpns"})
    private boolean mLockdownEnabled;
    @GuardedBy({"mVpns"})
    private LockdownVpnTracker mLockdownTracker;
    private long mMaxWakelockDurationMs;
    private final IpConnectivityLog mMetricsLog;
    @VisibleForTesting
    final MultinetworkPolicyTracker mMultinetworkPolicyTracker;
    @VisibleForTesting
    final MultipathPolicyTracker mMultipathPolicyTracker;
    private INetworkManagementService mNMS;
    private NetworkConfig[] mNetConfigs;
    @GuardedBy({"mNetworkForNetId"})
    private final SparseBooleanArray mNetIdInUse;
    private PowerManager.WakeLock mNetTransitionWakeLock;
    private int mNetTransitionWakeLockTimeout;
    @VisibleForTesting
    protected INetd mNetd;
    @VisibleForTesting
    protected final INetdEventCallback mNetdEventCallback;
    private final HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos;
    private final HashMap<Messenger, NetworkFactoryInfo> mNetworkFactoryInfos;
    @GuardedBy({"mNetworkForNetId"})
    private final SparseArray<NetworkAgentInfo> mNetworkForNetId;
    @GuardedBy({"mNetworkForRequestId"})
    private final SparseArray<NetworkAgentInfo> mNetworkForRequestId;
    private final LocalLog mNetworkInfoBlockingLogs;
    protected final LocalLog mNetworkRequestInfoLogs;
    private final ConcurrentHashMap<NetworkRequest, NetworkRequestInfo> mNetworkRequests;
    private int mNetworksDefined;
    private int mNextNetId;
    private int mNextNetworkRequestId;
    private NetworkNotificationManager mNotifier;
    private NetworkInfo mP2pNetworkInfo;
    private final PowerManager.WakeLock mPendingIntentWakeLock;
    @VisibleForTesting
    protected final PermissionMonitor mPermissionMonitor;
    private final INetworkPolicyListener mPolicyListener;
    private INetworkPolicyManager mPolicyManager;
    private NetworkPolicyManagerInternal mPolicyManagerInternal;
    private final PriorityDump.PriorityDumper mPriorityDumper;
    private List mProtectedNetworks;
    private final File mProvisioningUrlFile;
    @VisibleForTesting
    protected final ProxyTracker mProxyTracker;
    private final int mReleasePendingIntentDelayMs;
    private boolean mRestrictBackground;
    private final SettingsObserver mSettingsObserver;
    private INetworkStatsService mStatsService;
    private MockableSystemProperties mSystemProperties;
    private boolean mSystemReady;
    @GuardedBy({"mTNSLock"})
    private TestNetworkService mTNS;
    private final Object mTNSLock;
    private TelephonyManager mTelephonyManager;
    private Tethering mTethering;
    private int mTotalWakelockAcquisitions;
    private long mTotalWakelockDurationMs;
    private int mTotalWakelockReleases;
    private final NetworkStateTrackerHandler mTrackerHandler;
    private SparseIntArray mUidRules;
    @GuardedBy({"mUidToNetworkRequestCount"})
    private final SparseIntArray mUidToNetworkRequestCount;
    private UserManager mUserManager;
    private BroadcastReceiver mUserPresentReceiver;
    @GuardedBy({"mVpns"})
    @VisibleForTesting
    protected final SparseArray<Vpn> mVpns;
    private final LocalLog mWakelockLogs;

    /* access modifiers changed from: private */
    public enum ReapUnvalidatedNetworks {
        REAP,
        DONT_REAP
    }

    /* access modifiers changed from: private */
    public enum UnneededFor {
        LINGER,
        TEARDOWN
    }

    static {
        boolean z = false;
        if (-1 != SystemProperties.getInt(LINGER_DELAY_PROPERTY, -1)) {
            z = true;
        }
        IS_SUPPORT_LINGER_DELAY = z;
    }

    /* access modifiers changed from: private */
    public static String eventName(int what) {
        return sMagicDecoderRing.get(what, Integer.toString(what));
    }

    private static IDnsResolver getDnsResolver() {
        return IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class LegacyTypeTracker {
        private static final boolean DBG = true;
        private static final boolean VDBG = false;
        private final ConnectivityService mService;
        private final ArrayList<NetworkAgentInfo>[] mTypeLists = new ArrayList[55];

        LegacyTypeTracker(ConnectivityService service) {
            this.mService = service;
        }

        public void addSupportedType(int type) {
            ArrayList<NetworkAgentInfo>[] arrayListArr = this.mTypeLists;
            if (arrayListArr[type] == null) {
                arrayListArr[type] = new ArrayList<>();
                return;
            }
            throw new IllegalStateException("legacy list for type " + type + "already initialized");
        }

        public boolean isTypeSupported(int type) {
            return ConnectivityManager.isNetworkTypeValid(type) && this.mTypeLists[type] != null;
        }

        public NetworkAgentInfo getNetworkForType(int type) {
            synchronized (this.mTypeLists) {
                if (!isTypeSupported(type) || this.mTypeLists[type].isEmpty()) {
                    return null;
                }
                return this.mTypeLists[type].get(0);
            }
        }

        private void maybeLogBroadcast(NetworkAgentInfo nai, NetworkInfo.DetailedState state, int type, boolean isDefaultNetwork) {
            ConnectivityService.log("Sending " + state + " broadcast for type " + type + " " + nai.name() + " isDefaultNetwork=" + isDefaultNetwork);
        }

        public void add(int type, NetworkAgentInfo nai) {
            if (isTypeSupported(type)) {
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                if (!list.contains(nai)) {
                    synchronized (this.mTypeLists) {
                        list.add(nai);
                    }
                    boolean isDefaultNetwork = this.mService.isDefaultNetwork(nai);
                    if (list.size() == 1 || isDefaultNetwork) {
                        maybeLogBroadcast(nai, NetworkInfo.DetailedState.CONNECTED, type, isDefaultNetwork);
                        this.mService.sendLegacyNetworkBroadcast(nai, NetworkInfo.DetailedState.CONNECTED, type);
                    }
                }
            }
        }

        public void remove(int type, NetworkAgentInfo nai, boolean wasDefault) {
            ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
            if (list != null && !list.isEmpty()) {
                boolean wasFirstNetwork = list.get(0).equals(nai);
                synchronized (this.mTypeLists) {
                    if (!list.remove(nai)) {
                        return;
                    }
                }
                if (wasFirstNetwork || wasDefault) {
                    maybeLogBroadcast(nai, NetworkInfo.DetailedState.DISCONNECTED, type, wasDefault);
                    this.mService.sendLegacyNetworkBroadcast(nai, NetworkInfo.DetailedState.DISCONNECTED, type);
                }
                if (!list.isEmpty() && wasFirstNetwork) {
                    ConnectivityService.log("Other network available for type " + type + ", sending connected broadcast");
                    NetworkAgentInfo replacement = list.get(0);
                    maybeLogBroadcast(replacement, NetworkInfo.DetailedState.CONNECTED, type, this.mService.isDefaultNetwork(replacement));
                    this.mService.sendLegacyNetworkBroadcast(replacement, NetworkInfo.DetailedState.CONNECTED, type);
                }
            }
        }

        public void remove(NetworkAgentInfo nai, boolean wasDefault) {
            for (int type = 0; type < this.mTypeLists.length; type++) {
                remove(type, nai, wasDefault);
            }
        }

        public void update(NetworkAgentInfo nai) {
            boolean isDefault = this.mService.isDefaultNetwork(nai);
            NetworkInfo.DetailedState state = nai.networkInfo.getDetailedState();
            int type = 0;
            while (true) {
                ArrayList<NetworkAgentInfo>[] arrayListArr = this.mTypeLists;
                if (type < arrayListArr.length) {
                    ArrayList<NetworkAgentInfo> list = arrayListArr[type];
                    boolean isFirst = true;
                    boolean contains = list != null && list.contains(nai);
                    if (!contains || nai != list.get(0)) {
                        isFirst = false;
                    }
                    if (isFirst || (contains && isDefault)) {
                        maybeLogBroadcast(nai, state, type, isDefault);
                        this.mService.sendLegacyNetworkBroadcast(nai, state, type);
                    }
                    type++;
                } else {
                    return;
                }
            }
        }

        private String naiToString(NetworkAgentInfo nai) {
            String state;
            String name = nai.name();
            if (nai.networkInfo != null) {
                state = nai.networkInfo.getState() + SliceClientPermissions.SliceAuthority.DELIMITER + nai.networkInfo.getDetailedState();
            } else {
                state = "???/???";
            }
            return name + " " + state;
        }

        public void dump(IndentingPrintWriter pw) {
            pw.println("mLegacyTypeTracker:");
            pw.increaseIndent();
            pw.print("Supported types:");
            int type = 0;
            while (true) {
                ArrayList<NetworkAgentInfo>[] arrayListArr = this.mTypeLists;
                if (type >= arrayListArr.length) {
                    break;
                }
                if (arrayListArr[type] != null) {
                    pw.print(" " + type);
                }
                type++;
            }
            pw.println();
            pw.println("Current state:");
            pw.increaseIndent();
            synchronized (this.mTypeLists) {
                for (int type2 = 0; type2 < this.mTypeLists.length; type2++) {
                    if (this.mTypeLists[type2] != null) {
                        if (!this.mTypeLists[type2].isEmpty()) {
                            Iterator<NetworkAgentInfo> it = this.mTypeLists[type2].iterator();
                            while (it.hasNext()) {
                                pw.println(type2 + " " + naiToString(it.next()));
                            }
                        }
                    }
                }
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println();
        }
    }

    public ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this(context, netManager, statsService, policyManager, getDnsResolver(), new IpConnectivityLog(), NetdService.getInstance());
    }

    @VisibleForTesting
    protected ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager, IDnsResolver dnsresolver, IpConnectivityLog logger, INetd netd) {
        NetworkRequestInfo defaultNRI;
        this.mVpns = new SparseArray<>();
        this.mUidRules = new SparseIntArray();
        int i = 0;
        this.mDefaultInetConditionPublished = 0;
        this.mTNSLock = new Object();
        this.mNextNetId = 100;
        this.mNextNetworkRequestId = 1;
        this.mP2pNetworkInfo = new NetworkInfo(13, 0, "WIFI_P2P", "");
        this.mNetworkRequestInfoLogs = new LocalLog(20);
        this.mNetworkInfoBlockingLogs = new LocalLog(40);
        this.mWakelockLogs = new LocalLog(20);
        this.mTotalWakelockAcquisitions = 0;
        this.mTotalWakelockReleases = 0;
        this.mTotalWakelockDurationMs = 0;
        this.mMaxWakelockDurationMs = 0;
        this.mLastWakeLockAcquireTimestamp = 0;
        this.mBandwidthRequests = new SparseArray<>(10);
        this.mLegacyTypeTracker = new LegacyTypeTracker(this);
        this.mPriorityDumper = new PriorityDump.PriorityDumper() {
            /* class com.android.server.ConnectivityService.AnonymousClass1 */

            @Override // com.android.server.utils.PriorityDump.PriorityDumper
            public void dumpHigh(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                ConnectivityService.this.doDump(fd, pw, new String[]{ConnectivityService.DIAG_ARG}, asProto);
                ConnectivityService.this.doDump(fd, pw, new String[]{ConnectivityService.SHORT_ARG}, asProto);
            }

            @Override // com.android.server.utils.PriorityDump.PriorityDumper
            public void dumpNormal(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                ConnectivityService.this.doDump(fd, pw, args, asProto);
            }

            @Override // com.android.server.utils.PriorityDump.PriorityDumper
            public void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                ConnectivityService.this.doDump(fd, pw, args, asProto);
            }
        };
        this.mCust = (HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[0]);
        this.mDataActivityObserver = new BaseNetworkObserver() {
            /* class com.android.server.ConnectivityService.AnonymousClass3 */

            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
                ConnectivityService.this.sendDataActivityBroadcast(Integer.parseInt(label), active, tsNanos);
            }
        };
        this.mNetdEventCallback = new BaseNetdEventCallback() {
            /* class com.android.server.ConnectivityService.AnonymousClass4 */

            public void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) {
                try {
                    ConnectivityService.this.mHandler.sendMessage(ConnectivityService.this.mHandler.obtainMessage(38, new DnsManager.PrivateDnsValidationUpdate(netId, InetAddress.parseNumericAddress(ipAddress), hostname, validated)));
                } catch (IllegalArgumentException e) {
                    ConnectivityService.loge("Error parsing ip address in validation event");
                }
            }

            public void onDnsEvent(int netId, int eventType, int returnCode, String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
                NetworkAgentInfo nai = ConnectivityService.this.getNetworkAgentInfoForNetId(netId);
                if (nai != null && nai.satisfies(ConnectivityService.this.mDefaultRequest)) {
                    nai.networkMonitor().notifyDnsResponse(returnCode);
                }
            }

            public /* synthetic */ void lambda$onNat64PrefixEvent$0$ConnectivityService$4(int netId, boolean added, String prefixString, int prefixLength) {
                ConnectivityService.this.handleNat64PrefixEvent(netId, added, prefixString, prefixLength);
            }

            public void onNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) {
                ConnectivityService.this.mHandler.post(new Runnable(netId, added, prefixString, prefixLength) {
                    /* class com.android.server.$$Lambda$ConnectivityService$4$kjr9gauOtOpxwsI0DG7Gt6Wd1hI */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ boolean f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ int f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ConnectivityService.AnonymousClass4.this.lambda$onNat64PrefixEvent$0$ConnectivityService$4(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }
        };
        this.mPolicyListener = new NetworkPolicyManager.Listener() {
            /* class com.android.server.ConnectivityService.AnonymousClass5 */

            public void onUidRulesChanged(int uid, int uidRules) {
                ConnectivityService.this.mHandler.sendMessage(ConnectivityService.this.mHandler.obtainMessage(39, uid, uidRules));
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                ConnectivityService.log("onRestrictBackgroundChanged(restrictBackground=" + restrictBackground + ")");
                ConnectivityService.this.mHandler.sendMessage(ConnectivityService.this.mHandler.obtainMessage(40, restrictBackground ? 1 : 0, 0));
                if (restrictBackground) {
                    ConnectivityService.log("onRestrictBackgroundChanged(true): disabling tethering");
                    ConnectivityService.this.mTethering.untetherAll();
                }
            }
        };
        this.mProvisioningUrlFile = new File(PROVISIONING_URL_PATH);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.server.ConnectivityService.AnonymousClass7 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                ConnectivityService.this.ensureRunningOnConnectivityServiceThread();
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                Uri packageData = intent.getData();
                String packageName = packageData != null ? packageData.getSchemeSpecificPart() : null;
                if (userId != -10000) {
                    if ("android.intent.action.USER_STARTED".equals(action)) {
                        ConnectivityService.this.onUserStart(userId);
                    } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                        ConnectivityService.this.onUserStop(userId);
                    } else if ("android.intent.action.USER_ADDED".equals(action)) {
                        ConnectivityService.this.onUserAdded(userId);
                    } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                        ConnectivityService.this.onUserRemoved(userId);
                    } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                        ConnectivityService.this.onUserUnlocked(userId);
                    } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                        ConnectivityService.this.onPackageAdded(packageName, uid);
                    } else if ("android.intent.action.PACKAGE_REPLACED".equals(action)) {
                        ConnectivityService.this.onPackageReplaced(packageName, uid);
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        ConnectivityService.this.onPackageRemoved(packageName, uid, intent.getBooleanExtra("android.intent.extra.REPLACING", false));
                    }
                }
            }
        };
        this.mUserPresentReceiver = new BroadcastReceiver() {
            /* class com.android.server.ConnectivityService.AnonymousClass8 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                ConnectivityService.this.updateLockdownVpn();
                ConnectivityService.this.mContext.unregisterReceiver(this);
            }
        };
        this.mNetworkFactoryInfos = new HashMap<>();
        this.mNetworkRequests = new ConcurrentHashMap<>();
        this.mUidToNetworkRequestCount = new SparseIntArray();
        this.mNetworkForRequestId = new SparseArray<>();
        this.mNetworkForNetId = new SparseArray<>();
        this.mNetIdInUse = new SparseBooleanArray();
        this.mNetworkAgentInfos = new HashMap<>();
        this.mBlockedAppUids = new HashSet<>();
        this.mHwCSEx = null;
        log("ConnectivityService starting up");
        this.mHwCSEx = HwServiceExFactory.getHwConnectivityServiceEx(this, context);
        this.mSystemProperties = getSystemProperties();
        this.mMetricsLog = logger;
        this.mDefaultRequest = createDefaultInternetRequestForTransport(-1, NetworkRequest.Type.REQUEST);
        NetworkRequestInfo defaultNRI2 = new NetworkRequestInfo(null, this.mDefaultRequest, new Binder());
        this.mNetworkRequests.put(this.mDefaultRequest, defaultNRI2);
        this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI2);
        this.mDefaultMobileDataRequest = createDefaultInternetRequestForTransport(0, NetworkRequest.Type.BACKGROUND_REQUEST);
        this.mDefaultWifiRequest = createDefaultInternetRequestForTransport(1, NetworkRequest.Type.BACKGROUND_REQUEST);
        this.mHandlerThread = new HandlerThread("ConnectivityServiceThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mTrackerHandler = new NetworkStateTrackerHandler(this.mHandlerThread.getLooper());
        this.mHwCSEx.setupUniqueDeviceName();
        this.mReleasePendingIntentDelayMs = Settings.Secure.getInt(context.getContentResolver(), "connectivity_release_pending_intent_delay_ms", 5000);
        this.mLingerDelayMs = this.mSystemProperties.getInt(LINGER_DELAY_PROPERTY, DEFAULT_LINGER_DELAY_MS);
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mNMS = (INetworkManagementService) Preconditions.checkNotNull(netManager, "missing INetworkManagementService");
        this.mStatsService = (INetworkStatsService) Preconditions.checkNotNull(statsService, "missing INetworkStatsService");
        this.mPolicyManager = (INetworkPolicyManager) Preconditions.checkNotNull(policyManager, "missing INetworkPolicyManager");
        this.mPolicyManagerInternal = (NetworkPolicyManagerInternal) Preconditions.checkNotNull((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class), "missing NetworkPolicyManagerInternal");
        this.mHwPolicyManagerInternal = (HwNetworkPolicyManagerInternal) LocalServices.getService(HwNetworkPolicyManagerInternal.class);
        this.mDnsResolver = (IDnsResolver) Preconditions.checkNotNull(dnsresolver, "missing IDnsResolver");
        this.mProxyTracker = makeProxyTracker();
        this.mNetd = netd;
        this.mKeyStore = KeyStore.getInstance();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        try {
            this.mPolicyManager.registerListener(this.mPolicyListener);
        } catch (RemoteException e) {
            loge("unable to register INetworkPolicyListener" + e);
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mNetTransitionWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetTransitionWakeLockTimeout = this.mContext.getResources().getInteger(17694856);
        this.mPendingIntentWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetConfigs = new NetworkConfig[55];
        boolean wifiOnly = this.mSystemProperties.getBoolean("ro.radio.noril", false);
        log("wifiOnly=" + wifiOnly);
        String[] naStrings = context.getResources().getStringArray(17236091);
        int length = naStrings.length;
        while (i < length) {
            String naString = naStrings[i];
            try {
                NetworkConfig n = new NetworkConfig(naString);
                StringBuilder sb = new StringBuilder();
                defaultNRI = defaultNRI2;
                try {
                    sb.append("naString=");
                    sb.append(naString);
                    sb.append(" config=");
                    sb.append(n);
                    log(sb.toString());
                    if (n.type > 54) {
                        loge("Error in networkAttributes - ignoring attempt to define type " + n.type);
                    } else if (wifiOnly && ConnectivityManager.isNetworkTypeMobile(n.type)) {
                        log("networkAttributes - ignoring mobile as this dev is wifiOnly " + n.type);
                    } else if (this.mNetConfigs[n.type] != null) {
                        loge("Error in networkAttributes - ignoring attempt to redefine type " + n.type);
                    } else {
                        this.mLegacyTypeTracker.addSupportedType(n.type);
                        this.mNetConfigs[n.type] = n;
                        this.mNetworksDefined++;
                    }
                } catch (Exception e2) {
                }
            } catch (Exception e3) {
                defaultNRI = defaultNRI2;
            }
            i++;
            defaultNRI2 = defaultNRI;
        }
        if (this.mNetConfigs[17] == null) {
            this.mLegacyTypeTracker.addSupportedType(17);
            this.mNetworksDefined++;
        }
        if (this.mNetConfigs[9] == null && hasService("ethernet")) {
            this.mLegacyTypeTracker.addSupportedType(9);
            this.mNetworksDefined++;
        }
        log("mNetworksDefined=" + this.mNetworksDefined);
        this.mProtectedNetworks = new ArrayList();
        int[] protectedNetworks = context.getResources().getIntArray(17236049);
        for (int p : protectedNetworks) {
            if (this.mNetConfigs[p] == null || this.mProtectedNetworks.contains(Integer.valueOf(p))) {
                loge("Ignoring protectedNetwork " + p);
            } else {
                this.mProtectedNetworks.add(Integer.valueOf(p));
            }
        }
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mTethering = makeTethering();
        this.mPermissionMonitor = new PermissionMonitor(this.mContext, this.mNetd);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_STARTED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, this.mHandler);
        this.mContext.registerReceiverAsUser(this.mUserPresentReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_PRESENT"), null, null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter2, null, this.mHandler);
        try {
            this.mNMS.registerObserver(this.mTethering);
            this.mNMS.registerObserver(this.mDataActivityObserver);
        } catch (RemoteException e4) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Error registering observer :");
            sb2.append(HW_DEBUGGABLE ? e4 : "*");
            loge(sb2.toString());
        }
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerSettingsCallbacks();
        new DataConnectionStats(this.mContext).startMonitoring();
        this.mKeepaliveTracker = new KeepaliveTracker(this.mContext, this.mHandler);
        Context context2 = this.mContext;
        this.mNotifier = new NetworkNotificationManager(context2, this.mTelephonyManager, (NotificationManager) context2.getSystemService(NotificationManager.class));
        this.mLingerMonitor = new LingerMonitor(this.mContext, this.mNotifier, Settings.Global.getInt(this.mContext.getContentResolver(), "network_switch_notification_daily_limit", 3), Settings.Global.getLong(this.mContext.getContentResolver(), "network_switch_notification_rate_limit_millis", 60000));
        this.mMultinetworkPolicyTracker = createMultinetworkPolicyTracker(this.mContext, this.mHandler, new Runnable() {
            /* class com.android.server.$$Lambda$ConnectivityService$SFqiR4Pfksb1C7csMC3uNxCllR8 */

            @Override // java.lang.Runnable
            public final void run() {
                ConnectivityService.this.lambda$new$0$ConnectivityService();
            }
        });
        this.mMultinetworkPolicyTracker.start();
        this.mMultipathPolicyTracker = new MultipathPolicyTracker(this.mContext, this.mHandler);
        this.mDnsManager = new DnsManager(this.mContext, this.mDnsResolver, this.mSystemProperties);
        registerPrivateDnsSettingsCallbacks();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Tethering makeTethering() {
        return new Tethering(this.mContext, this.mNMS, this.mStatsService, this.mPolicyManager, IoThread.get().getLooper(), new MockableSystemProperties(), new TetheringDependencies() {
            /* class com.android.server.ConnectivityService.AnonymousClass2 */

            @Override // com.android.server.connectivity.tethering.TetheringDependencies
            public boolean isTetheringSupported() {
                return ConnectivityService.this.isTetheringSupported();
            }

            @Override // com.android.server.connectivity.tethering.TetheringDependencies
            public NetworkRequest getDefaultNetworkRequest() {
                return ConnectivityService.this.mDefaultRequest;
            }
        });
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ProxyTracker makeProxyTracker() {
        return new ProxyTracker(this.mContext, this.mHandler, 16);
    }

    private static NetworkCapabilities createDefaultNetworkCapabilitiesForUid(int uid) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        netCap.removeCapability(15);
        netCap.setSingleUid(uid);
        return netCap;
    }

    private NetworkRequest createDefaultInternetRequestForTransport(int transportType, NetworkRequest.Type type) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        if (transportType > -1) {
            netCap.addTransportType(transportType);
        }
        return new NetworkRequest(netCap, -1, nextNetworkRequestId(), type);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateAlwaysOnNetworks() {
        this.mHandler.sendEmptyMessage(30);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePrivateDnsSettings() {
        this.mHandler.sendEmptyMessage(37);
    }

    private void handleAlwaysOnNetworkRequest(NetworkRequest networkRequest, String settingName, boolean defaultValue) {
        boolean enable = toBool(Settings.Global.getInt(this.mContext.getContentResolver(), settingName, encodeBool(defaultValue)));
        boolean mpEnable = toBool(Settings.Global.getInt(this.mContext.getContentResolver(), MOBILE_DATA_ALWAYS_ON_FOR_MPLINK, 0));
        log("handleAlwaysOnNetworkRequest enable = " + enable + ";mpEnable = " + mpEnable);
        boolean isEnabled = true;
        if ("mobile_data_always_on".equals(settingName)) {
            enable = enable || this.mCust.mMobileDataAlwaysOnCust || mpEnable;
        }
        if (this.mNetworkRequests.get(networkRequest) == null) {
            isEnabled = false;
        }
        if (enable != isEnabled) {
            if (enable) {
                handleRegisterNetworkRequest(new NetworkRequestInfo(null, networkRequest, new Binder()));
            } else {
                handleReleaseNetworkRequest(networkRequest, 1000, false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigureAlwaysOnNetworks() {
        handleAlwaysOnNetworkRequest(this.mDefaultMobileDataRequest, "mobile_data_always_on", false);
        handleAlwaysOnNetworkRequest(this.mDefaultWifiRequest, "wifi_always_requested", false);
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Settings.Global.getUriFor("http_proxy"), 9);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("mobile_data_always_on"), 30);
        this.mSettingsObserver.observe(Settings.Global.getUriFor(MOBILE_DATA_ALWAYS_ON_FOR_MPLINK), 30);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("wifi_always_requested"), 30);
    }

    private void registerPrivateDnsSettingsCallbacks() {
        for (Uri uri : DnsManager.getPrivateDnsSettingsUris()) {
            this.mSettingsObserver.observe(uri, 37);
        }
    }

    private synchronized int nextNetworkRequestId() {
        int i;
        i = this.mNextNetworkRequestId;
        this.mNextNetworkRequestId = i + 1;
        return i;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int reserveNetId() {
        synchronized (this.mNetworkForNetId) {
            for (int i = 100; i <= MAX_NET_ID; i++) {
                int netId = this.mNextNetId;
                int i2 = this.mNextNetId + 1;
                this.mNextNetId = i2;
                if (i2 > MAX_NET_ID) {
                    this.mNextNetId = 100;
                }
                if (!this.mNetIdInUse.get(netId)) {
                    this.mNetIdInUse.put(netId, true);
                    return netId;
                }
            }
            throw new IllegalStateException("No free netIds");
        }
    }

    private NetworkState getFilteredNetworkState(int networkType, int uid) {
        NetworkState state;
        if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
            return NetworkState.EMPTY;
        }
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            state = nai.getNetworkState();
            state.networkInfo.setType(networkType);
        } else {
            NetworkInfo info = new NetworkInfo(networkType, 0, ConnectivityManager.getNetworkTypeName(networkType), "");
            info.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
            info.setIsAvailable((networkType == 13 || networkType == 1 || networkType == 0) ? false : true);
            NetworkCapabilities capabilities = new NetworkCapabilities();
            capabilities.setCapability(18, true ^ info.isRoaming());
            state = new NetworkState(info, new LinkProperties(), capabilities, (Network) null, (String) null, (String) null);
        }
        filterNetworkStateForUid(state, uid, false);
        return state;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public NetworkAgentInfo getNetworkAgentInfoForNetwork(Network network) {
        if (network == null) {
            return null;
        }
        return getNetworkAgentInfoForNetId(network.netId);
    }

    /* access modifiers changed from: protected */
    public NetworkAgentInfo getNetworkAgentInfoForNetId(int netId) {
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForNetId) {
            networkAgentInfo = this.mNetworkForNetId.get(netId);
        }
        return networkAgentInfo;
    }

    private Network[] getVpnUnderlyingNetworks(int uid) {
        Vpn vpn;
        synchronized (this.mVpns) {
            if (this.mLockdownEnabled || (vpn = this.mVpns.get(UserHandle.getUserId(uid))) == null || !vpn.appliesToUid(uid)) {
                return null;
            }
            return vpn.getUnderlyingNetworks();
        }
    }

    private NetworkState getUnfilteredActiveNetworkState(int uid) {
        NetworkAgentInfo nai = null;
        Object o = this.mNetworkForRequestId.get(this.mDefaultRequest.requestId);
        if (o instanceof NetworkAgentInfo) {
            nai = (NetworkAgentInfo) o;
        }
        Network[] networks = getVpnUnderlyingNetworks(uid);
        if (networks != null) {
            if (networks.length > 0) {
                nai = getNetworkAgentInfoForNetwork(networks[0]);
            } else {
                nai = null;
            }
        }
        if (nai != null) {
            return nai.getNetworkState();
        }
        return NetworkState.EMPTY;
    }

    private boolean isNetworkWithLinkPropertiesBlocked(LinkProperties lp, int uid, boolean ignoreBlocked) {
        boolean z = false;
        if (ignoreBlocked) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn != null && vpn.getLockdown() && vpn.isBlockingUid(uid)) {
                return true;
            }
        }
        String iface = lp == null ? "" : lp.getInterfaceName();
        boolean isBlock = false;
        boolean isNeedGetFromPolicy = true;
        int cacheBlock = this.mHwCSEx.getCacheNetworkState(uid, iface);
        int pid = -1;
        if (cacheBlock > -1) {
            if (cacheBlock == 1) {
                z = true;
            }
            isBlock = z;
            isNeedGetFromPolicy = false;
        }
        if (isNeedGetFromPolicy) {
            isBlock = this.mPolicyManagerInternal.isUidNetworkingBlocked(uid, iface);
            this.mHwCSEx.setCacheNetworkState(uid, iface, isBlock);
        }
        if (!isBlock && this.mHwPolicyManagerInternal != null) {
            if (uid == Binder.getCallingUid()) {
                pid = Binder.getCallingPid();
            }
            isBlock = this.mHwPolicyManagerInternal.isRestrictedByHaware(uid, pid);
            if (isBlock) {
                log("HAware B uid " + uid + " pid " + pid);
            }
        }
        return isBlock;
    }

    private void maybeLogBlockedNetworkInfo(NetworkInfo ni, int uid) {
        boolean blocked;
        if (ni != null) {
            synchronized (this.mBlockedAppUids) {
                if (ni.getDetailedState() == NetworkInfo.DetailedState.BLOCKED && this.mBlockedAppUids.add(Integer.valueOf(uid))) {
                    blocked = true;
                } else if (ni.isConnected() && this.mBlockedAppUids.remove(Integer.valueOf(uid))) {
                    blocked = false;
                } else {
                    return;
                }
            }
            String action = blocked ? "B" : "UB";
            log(String.format("%s net to uid=%d", action, Integer.valueOf(uid)));
            LocalLog localLog = this.mNetworkInfoBlockingLogs;
            localLog.log(action + " " + uid);
        }
    }

    private void maybeLogBlockedStatusChanged(NetworkRequestInfo nri, Network net, boolean blocked) {
        if (nri != null && net != null) {
            String action = blocked ? "B" : "UB";
            log(String.format("%s for %d(%d) on netId %d", Boolean.valueOf(blocked), Integer.valueOf(nri.mUid), Integer.valueOf(nri.request.requestId), Integer.valueOf(net.netId)));
            LocalLog localLog = this.mNetworkInfoBlockingLogs;
            localLog.log(action + " " + nri.mUid);
        }
    }

    private void filterNetworkStateForUid(NetworkState state, int uid, boolean ignoreBlocked) {
        if (state != null && state.networkInfo != null && state.linkProperties != null) {
            if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, ignoreBlocked)) {
                state.networkInfo.setDetailedState(NetworkInfo.DetailedState.BLOCKED, null, null);
                log("B uid " + uid);
            }
            synchronized (this.mVpns) {
                if (this.mLockdownTracker != null) {
                    this.mLockdownTracker.augmentNetworkInfo(state.networkInfo);
                }
            }
        }
    }

    public NetworkInfo getActiveNetworkInfo() {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_GETACTIVENETWORKINFO);
        }
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        NetworkState state = getUnfilteredActiveNetworkState(uid);
        filterNetworkStateForUid(state, uid, false);
        maybeLogBlockedNetworkInfo(state.networkInfo, uid);
        if (isAppBindedNetwork()) {
            return getActiveNetworkForMpLink(state.networkInfo, Binder.getCallingUid());
        }
        return state.networkInfo;
    }

    public Network getActiveNetwork() {
        enforceAccessPermission();
        return getActiveNetworkForUidInternal(Binder.getCallingUid(), false);
    }

    public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) {
        enforceConnectivityInternalPermission();
        return getActiveNetworkForUidInternal(uid, ignoreBlocked);
    }

    private Network getActiveNetworkForUidInternal(int uid, boolean ignoreBlocked) {
        NetworkAgentInfo nai;
        int user = UserHandle.getUserId(uid);
        int vpnNetId = 0;
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(user);
            if (vpn != null && vpn.appliesToUid(uid)) {
                vpnNetId = vpn.getNetId();
            }
        }
        if (vpnNetId != 0 && (nai = getNetworkAgentInfoForNetId(vpnNetId)) != null && createDefaultNetworkCapabilitiesForUid(uid).satisfiedByNetworkCapabilities(nai.networkCapabilities)) {
            return nai.network;
        }
        NetworkAgentInfo nai2 = getDefaultNetwork();
        if (nai2 != null && isNetworkWithLinkPropertiesBlocked(nai2.linkProperties, uid, ignoreBlocked)) {
            log("B uid " + uid);
            nai2 = null;
        }
        if (nai2 != null) {
            return nai2.network;
        }
        return null;
    }

    public NetworkInfo getActiveNetworkInfoUnfiltered() {
        enforceAccessPermission();
        return getUnfilteredActiveNetworkState(Binder.getCallingUid()).networkInfo;
    }

    public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) {
        enforceConnectivityInternalPermission();
        NetworkState state = getUnfilteredActiveNetworkState(uid);
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state.networkInfo;
    }

    public NetworkInfo getNetworkInfo(int networkType) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_GETNETWORKINFO);
        }
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (getVpnUnderlyingNetworks(uid) != null) {
            NetworkState state = getUnfilteredActiveNetworkState(uid);
            if (state.networkInfo != null && state.networkInfo.getType() == networkType) {
                filterNetworkStateForUid(state, uid, false);
                return state.networkInfo;
            }
        }
        return getFilteredNetworkState(networkType, uid).networkInfo;
    }

    public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) {
        enforceAccessPermission();
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai == null) {
            return null;
        }
        NetworkState state = nai.getNetworkState();
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state.networkInfo;
    }

    public NetworkInfo[] getAllNetworkInfo() {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_GETAllNETWORKINFO);
        }
        enforceAccessPermission();
        ArrayList<NetworkInfo> result = Lists.newArrayList();
        for (int networkType = 0; networkType <= 54; networkType++) {
            NetworkInfo info = getNetworkInfo(networkType);
            if (info != null) {
                result.add(info);
            }
        }
        return (NetworkInfo[]) result.toArray(new NetworkInfo[result.size()]);
    }

    public Network getNetworkForType(int networkType) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        NetworkState state = getFilteredNetworkState(networkType, uid);
        if (!isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, false)) {
            return state.network;
        }
        return null;
    }

    public Network[] getAllNetworks() {
        Network[] result;
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_GETALLNETWORKS);
        }
        enforceAccessPermission();
        synchronized (this.mNetworkForNetId) {
            result = new Network[this.mNetworkForNetId.size()];
            for (int i = 0; i < this.mNetworkForNetId.size(); i++) {
                result[i] = this.mNetworkForNetId.valueAt(i).network;
            }
        }
        return result;
    }

    public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) {
        Vpn vpn;
        Network[] networks;
        enforceAccessPermission();
        HashMap<Network, NetworkCapabilities> result = new HashMap<>();
        NetworkAgentInfo nai = getDefaultNetwork();
        NetworkCapabilities nc = getNetworkCapabilitiesInternal(nai);
        if (nc != null) {
            result.put(nai.network, nc);
        }
        synchronized (this.mVpns) {
            if (!(this.mLockdownEnabled || (vpn = this.mVpns.get(userId)) == null || (networks = vpn.getUnderlyingNetworks()) == null)) {
                for (Network network : networks) {
                    NetworkCapabilities nc2 = getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
                    if (nc2 != null) {
                        result.put(network, nc2);
                    }
                }
            }
        }
        return (NetworkCapabilities[]) result.values().toArray(new NetworkCapabilities[result.size()]);
    }

    public boolean isNetworkSupported(int networkType) {
        enforceAccessPermission();
        return this.mLegacyTypeTracker.isTypeSupported(networkType);
    }

    public LinkProperties getActiveLinkProperties() {
        enforceAccessPermission();
        return getUnfilteredActiveNetworkState(Binder.getCallingUid()).linkProperties;
    }

    public LinkProperties getLinkPropertiesForType(int networkType) {
        LinkProperties linkProperties;
        enforceAccessPermission();
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    public LinkProperties getLinkProperties(Network network) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_GETLINKPROPERTIES);
        }
        enforceAccessPermission();
        return getLinkProperties(getNetworkAgentInfoForNetwork(network));
    }

    private LinkProperties getLinkProperties(NetworkAgentInfo nai) {
        LinkProperties linkProperties;
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    private NetworkCapabilities getNetworkCapabilitiesInternal(NetworkAgentInfo nai) {
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            if (nai.networkCapabilities == null) {
                return null;
            }
            return networkCapabilitiesRestrictedForCallerPermissions(nai.networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
        }
    }

    public NetworkCapabilities getNetworkCapabilities(Network network) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_GETNETWORKCAPABILITIES);
        }
        enforceAccessPermission();
        return getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
    }

    private NetworkCapabilities networkCapabilitiesRestrictedForCallerPermissions(NetworkCapabilities nc, int callerPid, int callerUid) {
        NetworkCapabilities newNc = new NetworkCapabilities(nc);
        if (!checkSettingsPermission(callerPid, callerUid)) {
            newNc.setUids(null);
            newNc.setSSID(null);
        }
        if (newNc.getNetworkSpecifier() != null) {
            newNc.setNetworkSpecifier(newNc.getNetworkSpecifier().redact());
        }
        return newNc;
    }

    private void restrictRequestUidsForCaller(NetworkCapabilities nc) {
        if (!checkSettingsPermission()) {
            nc.setSingleUid(Binder.getCallingUid());
        }
    }

    private void restrictBackgroundRequestForCaller(NetworkCapabilities nc) {
        if (!this.mPermissionMonitor.hasUseBackgroundNetworksPermission(Binder.getCallingUid())) {
            nc.addCapability(19);
        }
    }

    public NetworkState[] getAllNetworkState() {
        enforceConnectivityInternalPermission();
        ArrayList<NetworkState> result = Lists.newArrayList();
        for (Network network : getAllNetworks()) {
            NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
            if (nai != null) {
                result.add(nai.getNetworkState());
            }
        }
        return (NetworkState[]) result.toArray(new NetworkState[result.size()]);
    }

    @Deprecated
    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        String str = TAG;
        Log.w(str, "Shame on UID " + Binder.getCallingUid() + " for calling the hidden API getNetworkQuotaInfo(). Shame!");
        return new NetworkQuotaInfo();
    }

    public boolean isActiveNetworkMetered() {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_ISACTIVENETWORKMETERED);
        }
        enforceAccessPermission();
        NetworkCapabilities caps = getNetworkCapabilities(getActiveNetwork());
        if (caps != null) {
            return true ^ caps.hasCapability(11);
        }
        return true;
    }

    private boolean disallowedBecauseSystemCaller() {
        if (!isSystem(Binder.getCallingUid()) || SystemProperties.getInt("ro.product.first_api_level", 0) <= EVENT_SET_ACCEPT_UNVALIDATED) {
            return false;
        }
        log("This method exists only for app backwards compatibility and must not be called by system services.");
        return true;
    }

    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) {
        NetworkInfo.DetailedState netState;
        LinkProperties lp;
        int netId;
        if (disallowedBecauseSystemCaller()) {
            return false;
        }
        enforceChangePermission();
        if (this.mProtectedNetworks.contains(Integer.valueOf(networkType))) {
            enforceConnectivityInternalPermission();
        }
        try {
            InetAddress addr = InetAddress.getByAddress(hostAddress);
            if (!ConnectivityManager.isNetworkTypeValid(networkType)) {
                log("requestRouteToHostAddress on invalid network: " + networkType);
                return false;
            }
            NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
            if (nai == null) {
                if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
                    log("requestRouteToHostAddress on unsupported network: " + networkType);
                } else {
                    log("requestRouteToHostAddress on down network: " + networkType);
                }
                return false;
            }
            synchronized (nai) {
                netState = nai.networkInfo.getDetailedState();
            }
            if (netState == NetworkInfo.DetailedState.CONNECTED || netState == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                int uid = Binder.getCallingUid();
                long token = Binder.clearCallingIdentity();
                try {
                    synchronized (nai) {
                        lp = nai.linkProperties;
                        netId = nai.network.netId;
                    }
                    boolean ok = addLegacyRouteToHost(lp, addr, netId, uid);
                    log("requestRouteToHostAddress ok=" + ok);
                    return ok;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                log("requestRouteToHostAddress on down network (" + networkType + ") - dropped netState=" + netState);
                return false;
            }
        } catch (UnknownHostException e) {
            log("requestRouteToHostAddress got " + e.toString());
            return false;
        }
    }

    private boolean addLegacyRouteToHost(LinkProperties lp, InetAddress addr, int netId, int uid) {
        RouteInfo bestRoute;
        RouteInfo bestRoute2 = RouteInfo.selectBestRoute(lp.getAllRoutes(), addr);
        if (bestRoute2 == null) {
            bestRoute = RouteInfo.makeHostRoute(addr, lp.getInterfaceName());
        } else {
            String iface = bestRoute2.getInterface();
            if (bestRoute2.getGateway().equals(addr)) {
                bestRoute = RouteInfo.makeHostRoute(addr, iface);
            } else {
                bestRoute = RouteInfo.makeHostRoute(addr, bestRoute2.getGateway(), iface);
            }
        }
        try {
            this.mNMS.addLegacyRouteForNetId(netId, bestRoute, uid);
            return true;
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception trying to add a route: ");
            sb.append(HW_DEBUGGABLE ? e : "*");
            loge(sb.toString());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void registerNetdEventCallback() {
        IIpConnectivityMetrics ipConnectivityMetrics = IIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService("connmetrics"));
        if (ipConnectivityMetrics == null) {
            Slog.wtf(TAG, "Missing IIpConnectivityMetrics");
            return;
        }
        try {
            ipConnectivityMetrics.addNetdEventCallback(0, this.mNetdEventCallback);
        } catch (Exception e) {
            loge("Error registering netd callback: " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidRulesChanged(int uid, int newRules) {
        if (this.mUidRules.get(uid, 0) != newRules) {
            maybeNotifyNetworkBlockedForNewUidRules(uid, newRules);
            if (newRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newRules);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleRestrictBackgroundChanged(boolean restrictBackground) {
        if (this.mRestrictBackground != restrictBackground) {
            for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                boolean curMetered = nai.networkCapabilities.isMetered();
                maybeNotifyNetworkBlocked(nai, curMetered, curMetered, this.mRestrictBackground, restrictBackground);
            }
            this.mRestrictBackground = restrictBackground;
        }
    }

    private boolean isUidNetworkingWithVpnBlocked(int uid, int uidRules, boolean isNetworkMetered, boolean isBackgroundRestricted) {
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn != null && vpn.getLockdown() && vpn.isBlockingUid(uid)) {
                return true;
            }
            NetworkPolicyManagerInternal networkPolicyManagerInternal = this.mPolicyManagerInternal;
            return NetworkPolicyManagerInternal.isUidNetworkingBlocked(uid, uidRules, isNetworkMetered, isBackgroundRestricted);
        }
    }

    private void enforceCrossUserPermission(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
    }

    private boolean checkAnyPermissionOf(String... permissions) {
        for (String permission : permissions) {
            if (this.mContext.checkCallingOrSelfPermission(permission) == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAnyPermissionOf(int pid, int uid, String... permissions) {
        for (String permission : permissions) {
            if (this.mContext.checkPermission(permission, pid, uid) == 0) {
                return true;
            }
        }
        return false;
    }

    private void enforceAnyPermissionOf(String... permissions) {
        if (!checkAnyPermissionOf(permissions)) {
            throw new SecurityException("Requires one of the following permissions: " + String.join(", ", permissions) + ".");
        }
    }

    private void enforceInternetPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERNET", "ConnectivityService");
    }

    /* access modifiers changed from: protected */
    public void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    /* access modifiers changed from: protected */
    public void enforceChangePermission() {
        ConnectivityManager.enforceChangePermission(this.mContext);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceSettingsPermission() {
        enforceAnyPermissionOf("android.permission.NETWORK_SETTINGS", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkSettingsPermission() {
        return checkAnyPermissionOf("android.permission.NETWORK_SETTINGS", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0 || this.mContext.checkPermission("android.permission.MAINLINE_NETWORK_STACK", pid, uid) == 0;
    }

    private void enforceTetherAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    private void enforceConnectivityInternalPermission() {
        enforceAnyPermissionOf("android.permission.CONNECTIVITY_INTERNAL", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private void enforceControlAlwaysOnVpnPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_ALWAYS_ON_VPN", "ConnectivityService");
    }

    private void enforceNetworkStackSettingsOrSetup() {
        enforceAnyPermissionOf("android.permission.NETWORK_SETTINGS", "android.permission.NETWORK_SETUP_WIZARD", "android.permission.NETWORK_STACK", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkNetworkStackPermission() {
        return checkAnyPermissionOf("android.permission.NETWORK_STACK", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private boolean checkNetworkSignalStrengthWakeupPermission(int pid, int uid) {
        return checkAnyPermissionOf(pid, uid, "android.permission.NETWORK_SIGNAL_STRENGTH_WAKEUP", "android.permission.MAINLINE_NETWORK_STACK");
    }

    private void enforceConnectivityRestrictedNetworksPermission() {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_USE_RESTRICTED_NETWORKS", "ConnectivityService");
        } catch (SecurityException e) {
            enforceConnectivityInternalPermission();
        }
    }

    private void enforceKeepalivePermission() {
        this.mContext.enforceCallingOrSelfPermission(KeepaliveTracker.PERMISSION, "ConnectivityService");
    }

    public void sendConnectedBroadcast(NetworkInfo info) {
        enforceConnectivityInternalPermission();
        sendGeneralBroadcast(info, "android.net.conn.CONNECTIVITY_CHANGE");
    }

    private void sendInetConditionBroadcast(NetworkInfo info) {
        sendGeneralBroadcast(info, "android.net.conn.INET_CONDITION_ACTION");
    }

    private Intent makeGeneralIntent(NetworkInfo info, String bcastType) {
        synchronized (this.mVpns) {
            if (this.mLockdownTracker != null) {
                info = new NetworkInfo(info);
                this.mLockdownTracker.augmentNetworkInfo(info);
            }
        }
        Intent intent = new Intent(bcastType);
        intent.putExtra("networkInfo", new NetworkInfo(info));
        intent.putExtra("networkType", info.getType());
        if (info.isFailover()) {
            intent.putExtra("isFailover", true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra("extraInfo", info.getExtraInfo());
        }
        intent.putExtra("inetCondition", this.mDefaultInetConditionPublished);
        return intent;
    }

    private void sendGeneralBroadcast(NetworkInfo info, String bcastType) {
        sendStickyBroadcast(makeGeneralIntent(info, bcastType));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDataActivityBroadcast(int deviceType, boolean active, long tsNanos) {
        Intent intent = new Intent("android.net.conn.DATA_ACTIVITY_CHANGE");
        intent.putExtra("deviceType", deviceType);
        intent.putExtra("isActive", active);
        intent.putExtra("tsNanos", tsNanos);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_DATA_ACTIVITY_CHANGE", null, null, 0, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendStickyBroadcast(Intent intent) {
        synchronized (this) {
            if (!this.mSystemReady && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                this.mInitialBroadcast = new Intent(intent);
            }
            intent.addFlags(DumpState.DUMP_HANDLE);
            log("sendStickyBroadcast: action=" + intent.getAction());
            Bundle options = null;
            long ident = Binder.clearCallingIdentity();
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (ni.getType() == 3) {
                    intent.setAction("android.net.conn.CONNECTIVITY_CHANGE_SUPL");
                    intent.addFlags(1073741824);
                } else {
                    BroadcastOptions opts = BroadcastOptions.makeBasic();
                    opts.setMaxManifestReceiverApiLevel((int) EVENT_UNREGISTER_NETWORK_FACTORY);
                    options = opts.toBundle();
                }
                try {
                    BatteryStatsService.getService().noteConnectivityChanged(intent.getIntExtra("networkType", -1), ni.getState().toString());
                } catch (RemoteException e) {
                }
                intent.addFlags(DumpState.DUMP_COMPILER_STATS);
            }
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL, options);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mProxyTracker.loadGlobalProxy();
        registerNetdEventCallback();
        this.mTethering.systemReady();
        synchronized (this) {
            this.mSystemReady = true;
            if (this.mInitialBroadcast != null) {
                this.mContext.sendStickyBroadcastAsUser(this.mInitialBroadcast, UserHandle.ALL);
                this.mInitialBroadcast = null;
            }
        }
        updateLockdownVpn();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(30));
        InternalHandler internalHandler2 = this.mHandler;
        internalHandler2.sendMessage(internalHandler2.obtainMessage(EVENT_SYSTEM_READY));
        this.mPermissionMonitor.startMonitoring();
    }

    private void setupDataActivityTracking(NetworkAgentInfo networkAgent) {
        int timeout;
        String iface = networkAgent.linkProperties.getInterfaceName();
        int type = -1;
        if (networkAgent.networkCapabilities.hasTransport(0)) {
            timeout = Settings.Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_mobile", 10);
            type = 0;
        } else if (networkAgent.networkCapabilities.hasTransport(1)) {
            timeout = Settings.Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_wifi", 15);
            type = 1;
        } else {
            timeout = 0;
        }
        if (timeout > 0 && iface != null && type != -1) {
            try {
                this.mNMS.addIdleTimer(iface, timeout, type);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception in setupDataActivityTracking ");
                sb.append(HW_DEBUGGABLE ? e : "*");
                loge(sb.toString());
            }
        }
    }

    private void removeDataActivityTracking(NetworkAgentInfo networkAgent) {
        String iface = networkAgent.linkProperties.getInterfaceName();
        NetworkCapabilities caps = networkAgent.networkCapabilities;
        if (iface == null) {
            return;
        }
        if (caps.hasTransport(0) || caps.hasTransport(1)) {
            try {
                this.mNMS.removeIdleTimer(iface);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception in removeDataActivityTracking ");
                sb.append(HW_DEBUGGABLE ? e : "*");
                loge(sb.toString());
            }
        }
    }

    private void updateDataActivityTracking(NetworkAgentInfo newNetwork, NetworkAgentInfo oldNetwork) {
        if (newNetwork != null) {
            setupDataActivityTracking(newNetwork);
        }
        if (oldNetwork != null) {
            removeDataActivityTracking(oldNetwork);
        }
    }

    private void updateMtu(LinkProperties newLp, LinkProperties oldLp) {
        String iface = newLp.getInterfaceName();
        int mtu = newLp.getMtu();
        if (oldLp != null || mtu != 0) {
            if (oldLp != null && newLp.isIdenticalMtu(oldLp)) {
                log("identical MTU - not setting");
            } else if (!LinkProperties.isValidMtu(mtu, newLp.hasGlobalIpv6Address())) {
                if (mtu != 0) {
                    loge("Unexpected mtu value: " + mtu + ", " + iface);
                }
            } else if (TextUtils.isEmpty(iface)) {
                loge("Setting MTU size with null iface.");
            } else {
                try {
                    log("Setting MTU size: " + iface + ", " + mtu);
                    this.mNMS.setMtu(iface, mtu);
                } catch (Exception e) {
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("exception in setMtu() ");
                    sb.append(HW_DEBUGGABLE ? e : "*");
                    Slog.e(str, sb.toString());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public MockableSystemProperties getSystemProperties() {
        return new MockableSystemProperties();
    }

    private void updateTcpBufferSizes(String tcpBufferSizes) {
        String[] values = null;
        if (tcpBufferSizes != null) {
            values = tcpBufferSizes.split(",");
        }
        if (values == null || values.length != 6) {
            log("Invalid tcpBufferSizes string: " + tcpBufferSizes + ", using defaults");
            tcpBufferSizes = DEFAULT_TCP_BUFFER_SIZES;
            values = tcpBufferSizes.split(",");
        }
        if (!tcpBufferSizes.equals(this.mCurrentTcpBufferSizes)) {
            try {
                String str = TAG;
                Slog.i(str, "Setting tx/rx TCP buffers to " + tcpBufferSizes);
                this.mNetd.setTcpRWmemorySize(String.join(" ", values[0], values[1], values[2]), String.join(" ", values[3], values[4], values[5]));
                this.mCurrentTcpBufferSizes = tcpBufferSizes;
            } catch (RemoteException | ServiceSpecificException e) {
                loge("Can't set TCP buffer sizes:" + e);
            }
            Integer rwndValue = Integer.valueOf(Settings.Global.getInt(this.mContext.getContentResolver(), "tcp_default_init_rwnd", this.mSystemProperties.getInt(DEFAULT_TCP_RWND_KEY, 0)));
            if (rwndValue.intValue() != 0) {
                this.mSystemProperties.set("sys.sysctl.tcp_def_init_rwnd", rwndValue.toString());
            }
        }
    }

    public int getRestoreDefaultNetworkDelay(int networkType) {
        String restoreDefaultNetworkDelayStr = this.mSystemProperties.get(NETWORK_RESTORE_DELAY_PROP_NAME);
        if (!(restoreDefaultNetworkDelayStr == null || restoreDefaultNetworkDelayStr.length() == 0)) {
            try {
                return Integer.parseInt(restoreDefaultNetworkDelayStr);
            } catch (NumberFormatException e) {
            }
        }
        if (networkType > 54) {
            return RESTORE_DEFAULT_NETWORK_DELAY;
        }
        NetworkConfig[] networkConfigArr = this.mNetConfigs;
        if (networkConfigArr[networkType] != null) {
            return networkConfigArr[networkType].restoreTime;
        }
        return RESTORE_DEFAULT_NETWORK_DELAY;
    }

    private void dumpNetworkDiagnostics(IndentingPrintWriter pw) {
        List<NetworkDiagnostics> netDiags = new ArrayList<>();
        NetworkAgentInfo[] networksSortedById = networksSortedById();
        for (NetworkAgentInfo nai : networksSortedById) {
            netDiags.add(new NetworkDiagnostics(nai.network, new LinkProperties(nai.linkProperties), 5000));
        }
        for (NetworkDiagnostics netDiag : netDiags) {
            pw.println();
            netDiag.waitForMeasurements();
            netDiag.dump(pw);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        PriorityDump.dump(this.mPriorityDumper, fd, writer, args);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doDump(FileDescriptor fd, PrintWriter writer, String[] args, boolean asProto) {
        int i;
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (!DumpUtils.checkDumpPermission(this.mContext, TAG, pw) || asProto) {
            return;
        }
        if (ArrayUtils.contains(args, DIAG_ARG)) {
            dumpNetworkDiagnostics(pw);
        } else if (ArrayUtils.contains(args, TETHERING_ARG)) {
            this.mTethering.dump(fd, pw, args);
        } else if (ArrayUtils.contains(args, NETWORK_ARG)) {
            dumpNetworks(pw);
        } else if (ArrayUtils.contains(args, REQUEST_ARG)) {
            dumpNetworkRequests(pw);
        } else {
            pw.print("NetworkFactories for:");
            Iterator<NetworkFactoryInfo> it = this.mNetworkFactoryInfos.values().iterator();
            while (it.hasNext()) {
                pw.print(" " + it.next().name);
            }
            pw.println();
            pw.println();
            NetworkAgentInfo defaultNai = getDefaultNetwork();
            pw.print("Active default network: ");
            if (defaultNai == null) {
                pw.println("none");
            } else {
                pw.println(defaultNai.network.netId);
            }
            pw.println();
            pw.println("Current Networks:");
            pw.increaseIndent();
            dumpNetworks(pw);
            pw.decreaseIndent();
            pw.println();
            pw.print("Restrict background: ");
            pw.println(this.mRestrictBackground);
            pw.println();
            pw.println("Status for known UIDs:");
            pw.increaseIndent();
            int size = this.mUidRules.size();
            int i2 = 0;
            while (true) {
                if (i2 >= size) {
                    break;
                }
                try {
                    int uid = this.mUidRules.keyAt(i2);
                    int uidRules = this.mUidRules.get(uid, 0);
                    pw.println("UID=" + uid + " rules=" + NetworkPolicyManager.uidRulesToString(uidRules));
                } catch (ArrayIndexOutOfBoundsException e) {
                    pw.println("  ArrayIndexOutOfBoundsException");
                } catch (ConcurrentModificationException e2) {
                    pw.println("  ConcurrentModificationException");
                }
                i2++;
            }
            pw.println();
            pw.decreaseIndent();
            pw.println("Network Requests:");
            pw.increaseIndent();
            dumpNetworkRequests(pw);
            pw.decreaseIndent();
            pw.println();
            this.mLegacyTypeTracker.dump(pw);
            pw.println();
            this.mTethering.dump(fd, pw, args);
            pw.println();
            this.mKeepaliveTracker.dump(pw);
            pw.println();
            dumpAvoidBadWifiSettings(pw);
            pw.println();
            this.mMultipathPolicyTracker.dump(pw);
            if (!ArrayUtils.contains(args, SHORT_ARG)) {
                pw.println();
                pw.println("mNetworkRequestInfoLogs (most recent first):");
                pw.increaseIndent();
                this.mNetworkRequestInfoLogs.reverseDump(fd, pw, args);
                pw.decreaseIndent();
                pw.println();
                pw.println("mNetworkInfoBlockingLogs (most recent first):");
                pw.increaseIndent();
                this.mNetworkInfoBlockingLogs.reverseDump(fd, pw, args);
                pw.decreaseIndent();
                pw.println();
                pw.println("NetTransition WakeLock activity (most recent first):");
                pw.increaseIndent();
                pw.println("total acquisitions: " + this.mTotalWakelockAcquisitions);
                pw.println("total releases: " + this.mTotalWakelockReleases);
                pw.println("cumulative duration: " + (this.mTotalWakelockDurationMs / 1000) + "s");
                pw.println("longest duration: " + (this.mMaxWakelockDurationMs / 1000) + "s");
                if (this.mTotalWakelockAcquisitions > this.mTotalWakelockReleases) {
                    pw.println("currently holding WakeLock for: " + ((SystemClock.elapsedRealtime() - this.mLastWakeLockAcquireTimestamp) / 1000) + "s");
                }
                this.mWakelockLogs.reverseDump(fd, pw, args);
                pw.println();
                pw.println("bandwidth update requests (by uid):");
                pw.increaseIndent();
                synchronized (this.mBandwidthRequests) {
                    for (i = 0; i < this.mBandwidthRequests.size(); i++) {
                        pw.println("[" + this.mBandwidthRequests.keyAt(i) + "]: " + this.mBandwidthRequests.valueAt(i));
                    }
                }
                pw.decreaseIndent();
                pw.decreaseIndent();
            }
            pw.println();
            pw.println("NetworkStackClient logs:");
            pw.increaseIndent();
            NetworkStackClient.getInstance().dump(pw);
            pw.decreaseIndent();
            pw.println();
            pw.println("Permission Monitor:");
            pw.increaseIndent();
            this.mPermissionMonitor.dump(pw);
            pw.decreaseIndent();
        }
    }

    private void dumpNetworks(IndentingPrintWriter pw) {
        NetworkAgentInfo[] networksSortedById = networksSortedById();
        for (NetworkAgentInfo nai : networksSortedById) {
            pw.println(nai.toString());
            pw.increaseIndent();
            pw.println(String.format("Requests: REQUEST:%d LISTEN:%d BACKGROUND_REQUEST:%d total:%d", Integer.valueOf(nai.numForegroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests() - nai.numRequestNetworkRequests()), Integer.valueOf(nai.numBackgroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests())));
            pw.increaseIndent();
            for (int i = 0; i < nai.numNetworkRequests(); i++) {
                pw.println(nai.requestAt(i).toString());
            }
            pw.decreaseIndent();
            pw.println("Lingered:");
            pw.increaseIndent();
            nai.dumpLingerTimers(pw);
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
    }

    private void dumpNetworkRequests(IndentingPrintWriter pw) {
        for (NetworkRequestInfo nri : requestsSortedById()) {
            pw.println(nri.toString());
        }
    }

    private NetworkAgentInfo[] networksSortedById() {
        NetworkAgentInfo[] networks = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[0]);
        Arrays.sort(networks, Comparator.comparingInt($$Lambda$ConnectivityService$_NU7EIcPVSuF_gWH_NWN_gBL4w.INSTANCE));
        return networks;
    }

    private NetworkRequestInfo[] requestsSortedById() {
        NetworkRequestInfo[] requests = (NetworkRequestInfo[]) this.mNetworkRequests.values().toArray(new NetworkRequestInfo[0]);
        Arrays.sort(requests, Comparator.comparingInt($$Lambda$ConnectivityService$iOdlQdHoQM14teTSEPRHRRL3k.INSTANCE));
        return requests;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLiveNetworkAgent(NetworkAgentInfo nai, int what) {
        if (nai.network == null) {
            return false;
        }
        NetworkAgentInfo officialNai = getNetworkAgentInfoForNetwork(nai.network);
        if (officialNai != null && officialNai.equals(nai)) {
            return true;
        }
        loge(eventName(what) + " - isLiveNetworkAgent found mismatched netId: " + officialNai + " - " + nai);
        return false;
    }

    /* access modifiers changed from: private */
    public class NetworkStateTrackerHandler extends Handler {
        public NetworkStateTrackerHandler(Looper looper) {
            super(looper);
        }

        private boolean maybeHandleAsyncChannelMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    ConnectivityService.this.handleAsyncChannelHalfConnect(msg);
                    return true;
                case 69633:
                case 69634:
                default:
                    return false;
                case 69635:
                    NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                    if (nai == null) {
                        return true;
                    }
                    nai.asyncChannel.disconnect();
                    return true;
                case 69636:
                    ConnectivityService.this.handleAsyncChannelDisconnected(msg);
                    return true;
            }
        }

        private void maybeHandleNetworkAgentMessage(Message msg) {
            NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
            boolean z = false;
            if (nai == null) {
                ConnectivityService.log(String.format("%s from unknown NetworkAgent", ConnectivityService.eventName(msg.what)));
                return;
            }
            int i = msg.what;
            if (i == 528392) {
                if (nai.everConnected) {
                    ConnectivityService.loge("ERROR: cannot call explicitlySelected on already-connected network");
                }
                nai.networkMisc.explicitlySelected = msg.arg1 == 1;
                nai.networkMisc.acceptUnvalidated = msg.arg1 == 1 && msg.arg2 == 1;
                NetworkMisc networkMisc = nai.networkMisc;
                if (msg.arg2 == 1) {
                    z = true;
                }
                networkMisc.acceptPartialConnectivity = z;
            } else if (i != 528397) {
                switch (i) {
                    case 528385:
                        ConnectivityService.this.updateNetworkInfo(nai, (NetworkInfo) msg.obj);
                        return;
                    case 528386:
                        NetworkCapabilities networkCapabilities = (NetworkCapabilities) msg.obj;
                        if (networkCapabilities.hasConnectivityManagedCapability()) {
                            Slog.wtf(ConnectivityService.TAG, "BUG: " + nai + " has CS-managed capability.");
                        }
                        ConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, networkCapabilities);
                        return;
                    case 528387:
                        ConnectivityService.this.handleUpdateLinkProperties(nai, (LinkProperties) msg.obj);
                        return;
                    case 528388:
                        ConnectivityService.this.updateNetworkScore(nai, msg.arg1);
                        return;
                    default:
                        ConnectivityService.this.mHwCSEx.maybeHandleNetworkAgentMessageEx(msg, nai, this);
                        return;
                }
            } else {
                ConnectivityService.this.mKeepaliveTracker.handleEventSocketKeepalive(nai, msg);
            }
        }

        private boolean maybeHandleNetworkMonitorMessage(Message msg) {
            String logMsg;
            int i = 2;
            switch (msg.what) {
                case 41:
                    NetworkAgentInfo nai = ConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg2);
                    if (nai != null) {
                        boolean wasPartial = nai.partialConnectivity;
                        nai.partialConnectivity = (msg.arg1 & 2) != 0;
                        boolean partialConnectivityChanged = wasPartial != nai.partialConnectivity;
                        boolean valid = (msg.arg1 & 1) != 0;
                        boolean wasValidated = nai.lastValidated;
                        boolean wasDefault = ConnectivityService.this.isDefaultNetwork(nai);
                        if (nai.captivePortalValidationPending && valid) {
                            nai.captivePortalValidationPending = false;
                        }
                        String redirectUrl = msg.obj instanceof String ? (String) msg.obj : "";
                        if (ConnectivityService.HW_DEBUGGABLE) {
                            if (!TextUtils.isEmpty(redirectUrl)) {
                                logMsg = "redirect to " + redirectUrl;
                            } else {
                                logMsg = "no redirect";
                            }
                            ConnectivityService.log(logMsg);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(nai.name());
                        sb.append(" validation ");
                        sb.append(valid ? "passed" : "failed");
                        ConnectivityService.log(sb.toString());
                        if (valid != nai.lastValidated) {
                            if (wasDefault) {
                                ConnectivityService.this.metricsLogger().defaultNetworkMetrics().logDefaultNetworkValidity(SystemClock.elapsedRealtime(), valid);
                            }
                            int oldScore = nai.getCurrentScore();
                            nai.lastValidated = valid;
                            nai.everValidated |= valid;
                            if (valid && nai.networkMisc != null && nai.networkMisc.acceptUnvalidated) {
                                ConnectivityService.log(nai.name() + " change acceptUnvalidated to false cause validation pass.");
                                nai.networkMisc.acceptUnvalidated = false;
                                if (nai.asyncChannel != null) {
                                    nai.asyncChannel.sendMessage(528393, ConnectivityService.encodeBool(false));
                                }
                            }
                            ConnectivityService.this.updateCapabilities(oldScore, nai, nai.networkCapabilities);
                            if (oldScore != nai.getCurrentScore()) {
                                ConnectivityService.this.sendUpdatedScoreToFactories(nai);
                            }
                            if (valid) {
                                ConnectivityService.this.handleFreshlyValidatedNetwork(nai);
                                ConnectivityService.this.mNotifier.clearNotification(nai.network.netId, NetworkNotificationManager.NotificationType.NO_INTERNET);
                                ConnectivityService.this.mNotifier.clearNotification(nai.network.netId, NetworkNotificationManager.NotificationType.LOST_INTERNET);
                                ConnectivityService.this.mNotifier.clearNotification(nai.network.netId, NetworkNotificationManager.NotificationType.PARTIAL_CONNECTIVITY);
                            }
                        } else if (partialConnectivityChanged) {
                            ConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, nai.networkCapabilities);
                        }
                        ConnectivityService.this.updateInetCondition(nai);
                        if (!ConnectivityService.this.reportPortalNetwork(nai, msg.arg1)) {
                            Bundle redirectUrlBundle = new Bundle();
                            redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                            AsyncChannel asyncChannel = nai.asyncChannel;
                            if (valid) {
                                i = 1;
                            }
                            asyncChannel.sendMessage(528391, i, 0, redirectUrlBundle);
                            if (!wasPartial && nai.partialConnectivity) {
                                ConnectivityService.this.mHandler.removeMessages(29, nai.network);
                                ConnectivityService.this.handlePromptUnvalidated(nai.network);
                            }
                            if (wasValidated && !nai.lastValidated) {
                                ConnectivityService.this.handleNetworkUnvalidated(nai);
                                break;
                            }
                        }
                    }
                    break;
                case 42:
                    NetworkAgentInfo nai2 = ConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg2);
                    if (nai2 != null) {
                        ConnectivityService.this.updatePrivateDns(nai2, (PrivateDnsConfig) msg.obj);
                        break;
                    }
                    break;
                case 43:
                    int netId = msg.arg2;
                    boolean visible = ConnectivityService.toBool(msg.arg1);
                    NetworkAgentInfo nai3 = ConnectivityService.this.getNetworkAgentInfoForNetId(netId);
                    if (!(nai3 == null || visible == nai3.lastCaptivePortalDetected)) {
                        int oldScore2 = nai3.getCurrentScore();
                        nai3.lastCaptivePortalDetected = visible;
                        nai3.everCaptivePortalDetected |= visible;
                        if (nai3.lastCaptivePortalDetected && 2 == getCaptivePortalMode()) {
                            ConnectivityService.log("Avoiding captive portal network: " + nai3.name());
                            nai3.asyncChannel.sendMessage(528399);
                            ConnectivityService.this.teardownUnneededNetwork(nai3);
                            break;
                        } else {
                            ConnectivityService.this.updateCapabilities(oldScore2, nai3, nai3.networkCapabilities);
                        }
                    }
                    if (visible) {
                        if (nai3 != null) {
                            if (!nai3.networkMisc.provisioningNotificationDisabled) {
                                ConnectivityService.this.mNotifier.showNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN, nai3, null, (PendingIntent) msg.obj, nai3.networkMisc.explicitlySelected);
                                break;
                            }
                        } else {
                            ConnectivityService.loge("EVENT_PROVISIONING_NOTIFICATION from unknown NetworkMonitor");
                            break;
                        }
                    } else {
                        ConnectivityService.this.mNotifier.clearNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN);
                        ConnectivityService.this.mNotifier.clearNotification(netId, NetworkNotificationManager.NotificationType.NETWORK_SWITCH);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        private int getCaptivePortalMode() {
            return Settings.Global.getInt(ConnectivityService.this.mContext.getContentResolver(), "captive_portal_mode", 1);
        }

        private boolean maybeHandleNetworkAgentInfoMessage(Message msg) {
            int i = msg.what;
            if (i == 1001) {
                NetworkAgentInfo nai = (NetworkAgentInfo) msg.obj;
                if (nai == null || !ConnectivityService.this.isLiveNetworkAgent(nai, msg.what)) {
                    return true;
                }
                ConnectivityService.this.handleLingerComplete(nai);
                return true;
            } else if (i != 528485) {
                return false;
            } else {
                NetworkAgentInfo nai2 = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                if (nai2 == null) {
                    ConnectivityService.loge("EVENT_REMATCH_NETWORK_AND_REQUESTS from unknown NetworkAgent");
                    return true;
                }
                ConnectivityService.this.rematchNetworkAndRequests(nai2, ReapUnvalidatedNetworks.DONT_REAP, SystemClock.elapsedRealtime());
                return true;
            }
        }

        private boolean maybeHandleNetworkFactoryMessage(Message msg) {
            if (msg.what != 536580) {
                return false;
            }
            ConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.sendingUid, true);
            return true;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (!maybeHandleAsyncChannelMessage(msg) && !maybeHandleNetworkMonitorMessage(msg) && !maybeHandleNetworkAgentInfoMessage(msg) && !maybeHandleNetworkFactoryMessage(msg)) {
                maybeHandleNetworkAgentMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    public class NetworkMonitorCallbacks extends INetworkMonitorCallbacks.Stub {
        private final AutodestructReference<NetworkAgentInfo> mNai;
        private final int mNetId;

        private NetworkMonitorCallbacks(NetworkAgentInfo nai) {
            this.mNetId = nai.network.netId;
            this.mNai = new AutodestructReference<>(nai);
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void onNetworkMonitorCreated(INetworkMonitor networkMonitor) {
            ConnectivityService.this.mHandler.sendMessage(ConnectivityService.this.mHandler.obtainMessage(18, new Pair(this.mNai.getAndDestroy(), networkMonitor)));
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void notifyNetworkTested(int testResult, String redirectUrl) {
            ConnectivityService.this.mTrackerHandler.sendMessage(ConnectivityService.this.mTrackerHandler.obtainMessage(41, testResult, this.mNetId, redirectUrl));
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void notifyPrivateDnsConfigResolved(PrivateDnsConfigParcel config) {
            ConnectivityService.this.mTrackerHandler.sendMessage(ConnectivityService.this.mTrackerHandler.obtainMessage(42, 0, this.mNetId, PrivateDnsConfig.fromParcel(config)));
        }

        /* JADX INFO: finally extract failed */
        @Override // android.net.INetworkMonitorCallbacks
        public void showProvisioningNotification(String action, String packageName) {
            Intent intent = new Intent(action);
            intent.setPackage(packageName);
            long token = Binder.clearCallingIdentity();
            try {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(ConnectivityService.this.mContext, 0, intent, 0);
                Binder.restoreCallingIdentity(token);
                ConnectivityService.this.mTrackerHandler.sendMessage(ConnectivityService.this.mTrackerHandler.obtainMessage(43, 1, this.mNetId, pendingIntent));
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void hideProvisioningNotification() {
            ConnectivityService.this.mTrackerHandler.sendMessage(ConnectivityService.this.mTrackerHandler.obtainMessage(43, 0, this.mNetId));
        }

        @Override // android.net.INetworkMonitorCallbacks
        public int getInterfaceVersion() {
            return 3;
        }
    }

    private boolean networkRequiresPrivateDnsValidation(NetworkAgentInfo nai) {
        return NetworkMonitorUtils.isPrivateDnsValidationRequired(nai.networkCapabilities);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFreshlyValidatedNetwork(NetworkAgentInfo nai) {
        if (nai != null) {
            PrivateDnsConfig cfg = this.mDnsManager.getPrivateDnsConfig();
            if (cfg.useTls && TextUtils.isEmpty(cfg.hostname)) {
                updateDnses(nai.linkProperties, null, nai.network.netId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrivateDnsSettingsChanged() {
        PrivateDnsConfig cfg = this.mDnsManager.getPrivateDnsConfig();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            handlePerNetworkPrivateDnsConfig(nai, cfg);
            if (networkRequiresPrivateDnsValidation(nai)) {
                handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
            }
        }
    }

    private void handlePerNetworkPrivateDnsConfig(NetworkAgentInfo nai, PrivateDnsConfig cfg) {
        if (networkRequiresPrivateDnsValidation(nai)) {
            nai.networkMonitor().notifyPrivateDnsChanged(cfg.toParcel());
            updatePrivateDns(nai, cfg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePrivateDns(NetworkAgentInfo nai, PrivateDnsConfig newCfg) {
        this.mDnsManager.updatePrivateDns(nai.network, newCfg);
        updateDnses(nai.linkProperties, null, nai.network.netId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrivateDnsValidationUpdate(DnsManager.PrivateDnsValidationUpdate update) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(update.netId);
        if (nai != null) {
            this.mDnsManager.updatePrivateDnsValidation(update);
            handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
        }
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0017: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v0 java.lang.String) */
    /* access modifiers changed from: public */
    private void handleNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) {
        NetworkAgentInfo nai = this.mNetworkForNetId.get(netId);
        if (nai != null) {
            Object[] objArr = new Object[4];
            objArr[0] = added ? "added" : "removed";
            objArr[1] = Integer.valueOf(netId);
            objArr[2] = prefixString;
            objArr[3] = Integer.valueOf(prefixLength);
            log(String.format("NAT64 prefix %s on netId %d: %s/%d", objArr));
            IpPrefix prefix = null;
            if (added) {
                try {
                    prefix = new IpPrefix(InetAddresses.parseNumericAddress(prefixString), prefixLength);
                } catch (IllegalArgumentException e) {
                    loge("Invalid NAT64 prefix " + prefixString + SliceClientPermissions.SliceAuthority.DELIMITER + prefixLength);
                    return;
                }
            }
            nai.clatd.setNat64Prefix(prefix);
            handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
        }
    }

    private void updateLingerState(NetworkAgentInfo nai, long now) {
        nai.updateLingerTimer();
        if (nai.isLingering() && nai.numForegroundNetworkRequests() > 0) {
            log("Unlingering " + nai.name());
            nai.unlinger();
            logNetworkEvent(nai, 6);
        } else if (unneeded(nai, UnneededFor.LINGER) && nai.getLingerExpiry() > 0) {
            int lingerTime = (int) (nai.getLingerExpiry() - now);
            log("Lingering " + nai.name() + " for " + lingerTime + "ms");
            nai.linger();
            logNetworkEvent(nai, 5);
            notifyNetworkCallbacks(nai, 524291, lingerTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAsyncChannelHalfConnect(Message msg) {
        int serial;
        int score;
        AsyncChannel ac = (AsyncChannel) msg.obj;
        if (this.mNetworkFactoryInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                log("NetworkFactory connected");
                this.mNetworkFactoryInfos.get(msg.replyTo).asyncChannel.sendMessage(69633);
                for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                    if (!nri.request.isListen()) {
                        NetworkAgentInfo nai = getNetworkForRequest(nri.request.requestId);
                        if (nai != null) {
                            score = nai.getCurrentScore();
                            serial = nai.factorySerialNumber;
                        } else {
                            score = 0;
                            serial = -1;
                        }
                        ac.sendMessage(536576, score, serial, nri.request);
                    }
                }
                return;
            }
            loge("Error connecting NetworkFactory");
            this.mNetworkFactoryInfos.remove(msg.obj);
        } else if (this.mNetworkAgentInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                log("NetworkAgent connected");
                this.mNetworkAgentInfos.get(msg.replyTo).asyncChannel.sendMessage(69633);
                return;
            }
            loge("Error connecting NetworkAgent");
            NetworkAgentInfo nai2 = this.mNetworkAgentInfos.remove(msg.replyTo);
            if (nai2 != null) {
                boolean wasDefault = isDefaultNetwork(nai2);
                synchronized (this.mNetworkForNetId) {
                    this.mNetworkForNetId.remove(nai2.network.netId);
                    this.mNetIdInUse.delete(nai2.network.netId);
                    sendNetworkStickyBroadcastAsUser("remove", nai2);
                }
                this.mLegacyTypeTracker.remove(nai2, wasDefault);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAsyncChannelDisconnected(Message msg) {
        NetworkAgentInfo nai = this.mNetworkAgentInfos.get(msg.replyTo);
        if (nai != null) {
            disconnectAndDestroyNetwork(nai);
            return;
        }
        NetworkFactoryInfo nfi = this.mNetworkFactoryInfos.remove(msg.replyTo);
        if (nfi != null) {
            log("unregisterNetworkFactory for " + nfi.name);
        }
    }

    private void disconnectAndDestroyNetwork(NetworkAgentInfo nai) {
        log(nai.name() + " got DISCONNECTED, was satisfying " + nai.numNetworkRequests());
        this.mNotifier.clearNotification(nai.network.netId);
        if (nai.networkInfo.isConnected()) {
            nai.networkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
        }
        boolean wasDefault = isDefaultNetwork(nai);
        if (wasDefault) {
            this.mDefaultInetConditionPublished = 0;
            metricsLogger().defaultNetworkMetrics().logDefaultNetworkEvent(SystemClock.elapsedRealtime(), null, nai);
        }
        notifyIfacesChangedForNetworkStats();
        notifyNetworkCallbacks(nai, 524292);
        this.mKeepaliveTracker.handleStopAllKeepalives(nai, -20);
        for (String iface : nai.linkProperties.getAllInterfaceNames()) {
            wakeupModifyInterface(iface, nai.networkCapabilities, false);
        }
        nai.networkMonitor().notifyNetworkDisconnected();
        this.mNetworkAgentInfos.remove(nai.messenger);
        nai.clatd.update();
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.remove(nai.network.netId);
            sendNetworkStickyBroadcastAsUser("remove", nai);
        }
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest request = nai.requestAt(i);
            NetworkAgentInfo currentNetwork = getNetworkForRequest(request.requestId);
            if (currentNetwork != null && currentNetwork.network.netId == nai.network.netId) {
                clearNetworkForRequest(request.requestId);
                sendUpdatedScoreToFactories(request, null);
            }
        }
        nai.clearLingerState();
        if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
            updateDataActivityTracking(null, nai);
            notifyLockdownVpn(nai);
            ensureNetworkTransitionWakelock(nai.name());
        }
        this.mLegacyTypeTracker.remove(nai, wasDefault);
        if (!nai.networkCapabilities.hasTransport(4)) {
            updateAllVpnsCapabilities();
        }
        rematchAllNetworksAndRequests(null, 0);
        this.mLingerMonitor.noteDisconnect(nai);
        if (nai.created) {
            destroyNativeNetwork(nai);
            this.mDnsManager.removeNetwork(nai.network);
        }
        synchronized (this.mNetworkForNetId) {
            this.mNetIdInUse.delete(nai.network.netId);
        }
        if (SystemProperties.getBoolean("ro.config.usb_rj45", false) && nai.isEthernet()) {
            sendInetConditionBroadcast(nai.networkInfo);
        }
    }

    private boolean createNativeNetwork(NetworkAgentInfo networkAgent) {
        boolean z;
        try {
            if (networkAgent.isVPN()) {
                INetd iNetd = this.mNetd;
                int i = networkAgent.network.netId;
                if (networkAgent.networkMisc != null) {
                    if (networkAgent.networkMisc.allowBypass) {
                        z = false;
                        iNetd.networkCreateVpn(i, z);
                    }
                }
                z = true;
                iNetd.networkCreateVpn(i, z);
            } else {
                this.mNetd.networkCreatePhysical(networkAgent.network.netId, getNetworkPermission(networkAgent.networkCapabilities));
            }
            this.mDnsResolver.createNetworkCache(networkAgent.network.netId);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            loge("Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
            return false;
        }
    }

    private void destroyNativeNetwork(NetworkAgentInfo networkAgent) {
        try {
            this.mNetd.networkDestroy(networkAgent.network.netId);
            this.mDnsResolver.destroyNetworkCache(networkAgent.network.netId);
        } catch (RemoteException | ServiceSpecificException e) {
            loge("Exception destroying network: " + e);
        }
    }

    private NetworkRequestInfo findExistingNetworkRequestInfo(PendingIntent pendingIntent) {
        Intent intent = pendingIntent.getIntent();
        for (Map.Entry<NetworkRequest, NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            PendingIntent existingPendingIntent = entry.getValue().mPendingIntent;
            if (existingPendingIntent != null && existingPendingIntent.getIntent().filterEquals(intent)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRegisterNetworkRequestWithIntent(Message msg) {
        NetworkRequestInfo nri = (NetworkRequestInfo) msg.obj;
        NetworkRequestInfo existingRequest = findExistingNetworkRequestInfo(nri.mPendingIntent);
        if (existingRequest != null) {
            log("Replacing " + existingRequest.request + " with " + nri.request + " because their intents matched.");
            handleReleaseNetworkRequest(existingRequest.request, getCallingUid(), false);
        }
        handleRegisterNetworkRequest(nri);
    }

    /* access modifiers changed from: protected */
    public void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        this.mNetworkRequests.put(nri.request, nri);
        LocalLog localLog = this.mNetworkRequestInfoLogs;
        localLog.log("REGISTER " + nri);
        if (nri.request.isListen()) {
            for (NetworkAgentInfo network : this.mNetworkAgentInfos.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && network.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    updateSignalStrengthThresholds(network, "REGISTER", nri.request);
                }
            }
        }
        rematchAllNetworksAndRequests(null, 0);
        if (nri.request.isRequest() && getNetworkForRequest(nri.request.requestId) == null) {
            sendUpdatedScoreToFactories(nri.request, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReleaseNetworkRequestWithIntent(PendingIntent pendingIntent, int callingUid) {
        NetworkRequestInfo nri = findExistingNetworkRequestInfo(pendingIntent);
        if (nri != null) {
            handleReleaseNetworkRequest(nri.request, callingUid, false);
        }
    }

    private boolean unneeded(NetworkAgentInfo nai, UnneededFor reason) {
        int numRequests;
        int i = AnonymousClass9.$SwitchMap$com$android$server$ConnectivityService$UnneededFor[reason.ordinal()];
        if (i == 1) {
            numRequests = nai.numRequestNetworkRequests();
        } else if (i != 2) {
            Slog.wtf(TAG, "Invalid reason. Cannot happen.");
            return true;
        } else {
            numRequests = nai.numForegroundNetworkRequests();
        }
        if (!nai.everConnected || nai.isVPN() || nai.isLingering() || numRequests > 0 || ignoreRemovedByWifiPro(nai)) {
            return false;
        }
        boolean isDisabledMobileNetwork = nai.networkInfo.getType() == 0 && !this.mTelephonyManager.getDataEnabled() && !HwFrameworkFactory.getHwInnerTelephonyManager().isVSimEnabled();
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            if (reason != UnneededFor.LINGER || !nri.request.isBackgroundRequest()) {
                NetworkAgentInfo existing = getNetworkForRequest(nri.request.requestId);
                boolean satisfies = nai.satisfies(nri.request) || checkNetworkSupportBip(nai, nri.request);
                if (this.mDefaultRequest.requestId == nri.request.requestId && isDisabledMobileNetwork) {
                    log("mobile net can't satisfy default network request when mobile data disabled");
                    satisfies = false;
                }
                if (nri.request.isRequest() && satisfies) {
                    if (nai.isSatisfyingRequest(nri.request.requestId) || (existing != null && existing.getCurrentScore() < nai.getCurrentScoreAsValidated())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private NetworkRequestInfo getNriForAppRequest(NetworkRequest request, int callingUid, String requestedOperation) {
        NetworkRequestInfo nri = this.mNetworkRequests.get(request);
        if (nri == null || 1000 == callingUid || nri.mUid == callingUid) {
            return nri;
        }
        log(String.format("UID %d attempted to %s for unowned request %s", Integer.valueOf(callingUid), requestedOperation, nri));
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTimedOutNetworkRequest(NetworkRequestInfo nri) {
        if (this.mNetworkRequests.get(nri.request) != null && getNetworkForRequest(nri.request.requestId) == null) {
            log("releasing " + nri.request + " (timeout)");
            handleRemoveNetworkRequest(nri);
            callCallbackForRequest(nri, null, 524293, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void handleReleaseNetworkRequest(NetworkRequest request, int callingUid, boolean callOnUnavailable) {
        NetworkRequestInfo nri;
        if ((!HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported() || !this.mHwCSEx.releaseNetworkSliceByApp(request, callingUid)) && (nri = getNriForAppRequest(request, callingUid, "release NetworkRequest")) != null) {
            log("releasing " + nri.request + " (release request)");
            handleRemoveNetworkRequest(nri);
            if (callOnUnavailable) {
                callCallbackForRequest(nri, null, 524293, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleRemoveNetworkRequest(NetworkRequestInfo nri, int whichCallback) {
        handleRemoveNetworkRequest(nri);
    }

    private void handleRemoveNetworkRequest(NetworkRequestInfo nri) {
        nri.unlinkDeathRecipient();
        this.mNetworkRequests.remove(nri.request);
        synchronized (this.mUidToNetworkRequestCount) {
            int requests = this.mUidToNetworkRequestCount.get(nri.mUid, 0);
            if (requests < 1) {
                String str = TAG;
                Slog.wtf(str, "BUG: too small request count " + requests + " for UID " + nri.mUid);
            } else if (requests == 1) {
                this.mUidToNetworkRequestCount.removeAt(this.mUidToNetworkRequestCount.indexOfKey(nri.mUid));
            } else {
                this.mUidToNetworkRequestCount.put(nri.mUid, requests - 1);
            }
        }
        LocalLog localLog = this.mNetworkRequestInfoLogs;
        localLog.log("RELEASE " + nri);
        if (nri.request.isRequest()) {
            boolean wasKept = false;
            NetworkAgentInfo nai = getNetworkForRequest(nri.request.requestId);
            if (nai != null) {
                boolean wasBackgroundNetwork = nai.isBackgroundNetwork();
                nai.removeRequest(nri.request.requestId);
                log(" Removing from current network " + nai.name() + ", leaving " + nai.numNetworkRequests() + " requests.");
                updateLingerState(nai, SystemClock.elapsedRealtime());
                if (unneeded(nai, UnneededFor.TEARDOWN)) {
                    log("no live requests for " + nai.name() + "; disconnecting");
                    teardownUnneededNetwork(nai);
                } else {
                    wasKept = true;
                }
                clearNetworkForRequest(nri.request.requestId);
                if (!wasBackgroundNetwork && nai.isBackgroundNetwork()) {
                    updateCapabilities(nai.getCurrentScore(), nai, nai.networkCapabilities);
                }
            }
            if (!(nri.request.legacyType == -1 || nai == null)) {
                boolean doRemove = true;
                if (wasKept) {
                    for (int i = 0; i < nai.numNetworkRequests(); i++) {
                        NetworkRequest otherRequest = nai.requestAt(i);
                        if (otherRequest.legacyType == nri.request.legacyType && otherRequest.isRequest()) {
                            log(" still have other legacy request - leaving");
                            doRemove = false;
                        }
                    }
                }
                if (doRemove) {
                    this.mLegacyTypeTracker.remove(nri.request.legacyType, nai, false);
                }
            }
            for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
                nfi.asyncChannel.sendMessage(536577, nri.request);
            }
            return;
        }
        for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
            nai2.removeRequest(nri.request.requestId);
            if (nri.request.networkCapabilities.hasSignalStrength() && nai2.satisfiesImmutableCapabilitiesOf(nri.request)) {
                updateSignalStrengthThresholds(nai2, "RELEASE", nri.request);
            }
        }
    }

    public void setAcceptUnvalidated(Network network, boolean accept, boolean always) {
        enforceNetworkStackSettingsOrSetup();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_SET_ACCEPT_UNVALIDATED, encodeBool(accept), encodeBool(always), network));
    }

    public void setAcceptPartialConnectivity(Network network, boolean accept, boolean always) {
        enforceNetworkStackSettingsOrSetup();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(45, encodeBool(accept), encodeBool(always), network));
    }

    public void setAvoidUnvalidated(Network network) {
        enforceNetworkStackSettingsOrSetup();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(35, network));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetAcceptUnvalidated(Network network, boolean accept, boolean always) {
        log("handleSetAcceptUnvalidated network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated) {
            if (accept != nai.networkMisc.acceptUnvalidated) {
                int oldScore = nai.getCurrentScore();
                nai.networkMisc.acceptUnvalidated = accept;
                nai.networkMisc.acceptPartialConnectivity = accept;
                rematchAllNetworksAndRequests(nai, oldScore);
                sendUpdatedScoreToFactories(nai);
            }
            if (always) {
                nai.asyncChannel.sendMessage(528393, encodeBool(accept));
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetAcceptPartialConnectivity(Network network, boolean accept, boolean always) {
        log("handleSetAcceptPartialConnectivity network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.lastValidated) {
            if (accept != nai.networkMisc.acceptPartialConnectivity) {
                nai.networkMisc.acceptPartialConnectivity = accept;
            }
            if (always) {
                nai.asyncChannel.sendMessage(528393, encodeBool(accept));
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
                return;
            }
            nai.networkMonitor().setAcceptPartialConnectivity();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetAvoidUnvalidated(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.lastValidated && !nai.avoidUnvalidated) {
            int oldScore = nai.getCurrentScore();
            nai.avoidUnvalidated = true;
            rematchAllNetworksAndRequests(nai, oldScore);
            sendUpdatedScoreToFactories(nai);
        }
    }

    private void scheduleUnvalidatedPrompt(NetworkAgentInfo nai) {
        log("scheduleUnvalidatedPrompt " + nai.network);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessageDelayed(internalHandler.obtainMessage(29, nai.network), 8000);
    }

    public void startCaptivePortalApp(Network network) {
        enforceConnectivityInternalPermission();
        this.mHandler.post(new Runnable(network) {
            /* class com.android.server.$$Lambda$ConnectivityService$OIhIcUZjeJci4rP6veezE8o67U */
            private final /* synthetic */ Network f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ConnectivityService.this.lambda$startCaptivePortalApp$3$ConnectivityService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$startCaptivePortalApp$3$ConnectivityService(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && nai.networkCapabilities.hasCapability(17)) {
            nai.networkMonitor().launchCaptivePortalApp();
        }
    }

    public void startCaptivePortalAppInternal(Network network, Bundle appExtras) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MAINLINE_NETWORK_STACK", "ConnectivityService");
        Intent appIntent = new Intent("android.net.conn.CAPTIVE_PORTAL");
        appIntent.putExtras(appExtras);
        appIntent.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new CaptivePortalImpl(network).asBinder()));
        appIntent.setFlags(272629760);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null) {
            nai.captivePortalValidationPending = true;
        }
        Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(appIntent) {
            /* class com.android.server.$$Lambda$ConnectivityService$vGRhfNpFTw0hellWUlmBolfzRy8 */
            private final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void runOrThrow() {
                ConnectivityService.this.lambda$startCaptivePortalAppInternal$4$ConnectivityService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$startCaptivePortalAppInternal$4$ConnectivityService(Intent appIntent) throws Exception {
        this.mContext.startActivityAsUser(appIntent, UserHandle.CURRENT);
    }

    class CaptivePortalImpl extends ICaptivePortal.Stub {
        private final Network mNetwork;

        CaptivePortalImpl(Network network) {
            this.mNetwork = network;
        }

        public void appResponse(int response) {
            NetworkMonitorManager nm;
            if (response == 2) {
                ConnectivityService.this.enforceSettingsPermission();
            }
            NetworkAgentInfo nai = ConnectivityService.this.getNetworkAgentInfoForNetwork(this.mNetwork);
            if (nai != null && (nm = nai.networkMonitor()) != null) {
                nm.notifyCaptivePortalAppFinished(response);
            }
        }

        public void logEvent(int eventId, String packageName) {
            ConnectivityService.this.enforceSettingsPermission();
            new MetricsLogger().action(eventId, packageName);
        }
    }

    public boolean avoidBadWifi() {
        return this.mMultinetworkPolicyTracker.getAvoidBadWifi();
    }

    public boolean shouldAvoidBadWifi() {
        if (checkNetworkStackPermission()) {
            return avoidBadWifi();
        }
        throw new SecurityException("avoidBadWifi requires NETWORK_STACK permission");
    }

    /* access modifiers changed from: private */
    /* renamed from: rematchForAvoidBadWifiUpdate */
    public void lambda$new$0$ConnectivityService() {
        rematchAllNetworksAndRequests(null, 0);
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.networkCapabilities.hasTransport(1)) {
                sendUpdatedScoreToFactories(nai);
            }
        }
    }

    private void dumpAvoidBadWifiSettings(IndentingPrintWriter pw) {
        String description;
        boolean configRestrict = this.mMultinetworkPolicyTracker.configRestrictsAvoidBadWifi();
        if (!configRestrict) {
            pw.println("Bad Wi-Fi avoidance: unrestricted");
            return;
        }
        pw.println("Bad Wi-Fi avoidance: " + avoidBadWifi());
        pw.increaseIndent();
        pw.println("Config restrict:   " + configRestrict);
        String value = this.mMultinetworkPolicyTracker.getAvoidBadWifiSetting();
        if ("0".equals(value)) {
            description = "get stuck";
        } else if (value == null) {
            description = "prompt";
        } else if ("1".equals(value)) {
            description = "avoid";
        } else {
            description = value + " (?)";
        }
        pw.println("User setting:      " + description);
        pw.println("Network overrides:");
        pw.increaseIndent();
        NetworkAgentInfo[] networksSortedById = networksSortedById();
        for (NetworkAgentInfo nai : networksSortedById) {
            if (nai.avoidUnvalidated) {
                pw.println(nai.name());
            }
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.ConnectivityService$9  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass9 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$ConnectivityService$UnneededFor = new int[UnneededFor.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType = new int[NetworkNotificationManager.NotificationType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.LOGGED_IN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.NO_INTERNET.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.LOST_INTERNET.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[NetworkNotificationManager.NotificationType.PARTIAL_CONNECTIVITY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$server$ConnectivityService$UnneededFor[UnneededFor.TEARDOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$server$ConnectivityService$UnneededFor[UnneededFor.LINGER.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private void showNetworkNotification(NetworkAgentInfo nai, NetworkNotificationManager.NotificationType type) {
        String action;
        int i = AnonymousClass9.$SwitchMap$com$android$server$connectivity$NetworkNotificationManager$NotificationType[type.ordinal()];
        if (i == 1) {
            action = "android.settings.WIFI_SETTINGS";
            this.mHandler.removeMessages(44);
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessageDelayed(internalHandler.obtainMessage(44, nai.network.netId, 0), 20000);
        } else if (i == 2) {
            action = "android.net.conn.PROMPT_UNVALIDATED";
        } else if (i == 3) {
            action = "android.net.conn.PROMPT_LOST_VALIDATION";
        } else if (i != 4) {
            String str = TAG;
            Slog.wtf(str, "Unknown notification type " + type);
            return;
        } else {
            action = "android.net.conn.PROMPT_PARTIAL_CONNECTIVITY";
            boolean z = nai.networkMisc.explicitlySelected;
        }
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            log("factory mode is not display dialog");
            return;
        }
        Intent intent = new Intent(action);
        if (type != NetworkNotificationManager.NotificationType.LOGGED_IN) {
            intent.setData(Uri.fromParts("netId", Integer.toString(nai.network.netId), null));
            intent.addFlags(268435456);
            intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiNoInternetDialog");
        }
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    private boolean shouldPromptUnvalidated(NetworkAgentInfo nai) {
        if (nai.everValidated || nai.everCaptivePortalDetected) {
            return false;
        }
        if (nai.networkCapabilities != null && !nai.networkCapabilities.hasTransport(1)) {
            return false;
        }
        if (nai.partialConnectivity && !nai.networkMisc.acceptPartialConnectivity) {
            return true;
        }
        if (!nai.networkMisc.explicitlySelected || nai.networkMisc.acceptUnvalidated) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePromptUnvalidated(Network network) {
        log("handlePromptUnvalidated " + network);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (!(nai == null || nai.networkMisc == null)) {
            log("explicitlySelected:" + nai.networkMisc.explicitlySelected + " , acceptUnvalidated:" + nai.networkMisc.acceptUnvalidated);
        }
        if (nai != null && shouldPromptUnvalidated(nai)) {
            nai.asyncChannel.sendMessage(528399);
            if (1 <= nai.networkMisc.wifiApType) {
                log("ai device do not show validation notification for network " + network);
            } else if (nai.networkMisc.connectToCellularAndWLAN == 1) {
                log("duplex path selected: " + nai.networkMisc.connectToCellularAndWLAN);
            } else {
                log("show validation notification for network " + network);
                if (nai.partialConnectivity) {
                    showNetworkNotification(nai, NetworkNotificationManager.NotificationType.PARTIAL_CONNECTIVITY);
                } else if (!HwDeviceManager.disallowOp(70, nai.networkCapabilities.getSSID())) {
                    showNetworkNotification(nai, NetworkNotificationManager.NotificationType.NO_INTERNET);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkUnvalidated(NetworkAgentInfo nai) {
        NetworkCapabilities nc = nai.networkCapabilities;
        log("handleNetworkUnvalidated " + nai.name() + " cap=" + nc);
        if (nc.hasTransport(1) && this.mMultinetworkPolicyTracker.shouldNotifyWifiUnvalidated()) {
            showNetworkNotification(nai, NetworkNotificationManager.NotificationType.LOST_INTERNET);
        }
    }

    public int getMultipathPreference(Network network) {
        enforceAccessPermission();
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && nai.networkCapabilities.hasCapability(11)) {
            return 7;
        }
        Integer networkPreference = this.mMultipathPolicyTracker.getMultipathPreference(network);
        if (networkPreference != null) {
            return networkPreference.intValue();
        }
        return this.mMultinetworkPolicyTracker.getMeteredMultipathPreference();
    }

    public NetworkRequest getDefaultRequest() {
        return this.mDefaultRequest;
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 8) {
                if (i == 9) {
                    ConnectivityService.this.mProxyTracker.loadDeprecatedGlobalHttpProxy();
                    return;
                } else if (i == 44) {
                    ConnectivityService.this.mNotifier.clearNotification(msg.arg1, NetworkNotificationManager.NotificationType.LOGGED_IN);
                    return;
                } else if (i != 45) {
                    switch (i) {
                        case 16:
                            ConnectivityService.this.handleApplyDefaultProxy((ProxyInfo) msg.obj);
                            return;
                        case 17:
                            ConnectivityService.this.handleRegisterNetworkFactory((NetworkFactoryInfo) msg.obj);
                            return;
                        case 18:
                            Pair<NetworkAgentInfo, INetworkMonitor> arg = (Pair) msg.obj;
                            ConnectivityService.this.handleRegisterNetworkAgent((NetworkAgentInfo) arg.first, (INetworkMonitor) arg.second);
                            return;
                        case 19:
                        case 21:
                            ConnectivityService.this.handleRegisterNetworkRequest((NetworkRequestInfo) msg.obj);
                            return;
                        case 20:
                            ConnectivityService.this.handleTimedOutNetworkRequest((NetworkRequestInfo) msg.obj);
                            return;
                        case 22:
                            ConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.arg1, false);
                            return;
                        case ConnectivityService.EVENT_UNREGISTER_NETWORK_FACTORY /* 23 */:
                            ConnectivityService.this.handleUnregisterNetworkFactory((Messenger) msg.obj);
                            return;
                        case ConnectivityService.EVENT_EXPIRE_NET_TRANSITION_WAKELOCK /* 24 */:
                            break;
                        case ConnectivityService.EVENT_SYSTEM_READY /* 25 */:
                            ConnectivityService.this.mMultipathPolicyTracker.start();
                            return;
                        case 26:
                        case 31:
                            ConnectivityService.this.handleRegisterNetworkRequestWithIntent(msg);
                            return;
                        case ConnectivityService.EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT /* 27 */:
                            ConnectivityService.this.handleReleaseNetworkRequestWithIntent((PendingIntent) msg.obj, msg.arg1);
                            return;
                        case ConnectivityService.EVENT_SET_ACCEPT_UNVALIDATED /* 28 */:
                            ConnectivityService.this.handleSetAcceptUnvalidated((Network) msg.obj, ConnectivityService.toBool(msg.arg1), ConnectivityService.toBool(msg.arg2));
                            return;
                        case 29:
                            ConnectivityService.this.handlePromptUnvalidated((Network) msg.obj);
                            return;
                        case 30:
                            ConnectivityService.this.handleConfigureAlwaysOnNetworks();
                            return;
                        default:
                            switch (i) {
                                case 35:
                                    ConnectivityService.this.handleSetAvoidUnvalidated((Network) msg.obj);
                                    return;
                                case 36:
                                    ConnectivityService.this.handleReportNetworkConnectivity((Network) msg.obj, msg.arg1, ConnectivityService.toBool(msg.arg2));
                                    return;
                                case 37:
                                    ConnectivityService.this.handlePrivateDnsSettingsChanged();
                                    return;
                                case 38:
                                    ConnectivityService.this.handlePrivateDnsValidationUpdate((DnsManager.PrivateDnsValidationUpdate) msg.obj);
                                    return;
                                case 39:
                                    ConnectivityService.this.handleUidRulesChanged(msg.arg1, msg.arg2);
                                    return;
                                case 40:
                                    ConnectivityService.this.handleRestrictBackgroundChanged(ConnectivityService.toBool(msg.arg1));
                                    return;
                                default:
                                    switch (i) {
                                        case 528395:
                                            ConnectivityService.this.mKeepaliveTracker.handleStartKeepalive(msg);
                                            return;
                                        case 528396:
                                            ConnectivityService.this.mKeepaliveTracker.handleStopKeepalive(ConnectivityService.this.getNetworkAgentInfoForNetwork((Network) msg.obj), msg.arg1, msg.arg2);
                                            return;
                                        default:
                                            return;
                                    }
                            }
                    }
                } else {
                    ConnectivityService.this.handleSetAcceptPartialConnectivity((Network) msg.obj, ConnectivityService.toBool(msg.arg1), ConnectivityService.toBool(msg.arg2));
                    return;
                }
            }
            ConnectivityService.this.handleReleaseNetworkTransitionWakelock(msg.what);
        }
    }

    public int tether(String iface, String callerPkg) {
        Log.i(TAG, "tether: ENTER");
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (!isTetheringSupported()) {
            return 3;
        }
        int status = this.mTethering.tether(iface);
        String str = TAG;
        Log.w(str, "tether() return with status " + status);
        return status;
    }

    public int untether(String iface, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (isTetheringSupported()) {
            return this.mTethering.untether(iface);
        }
        return 3;
    }

    public int getLastTetherError(String iface) {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getLastTetherError(iface);
        }
        return 3;
    }

    public String[] getTetherableUsbRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableUsbRegexs();
        }
        return new String[0];
    }

    public String[] getTetherableWifiRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableWifiRegexs();
        }
        return new String[0];
    }

    public String[] getTetherableBluetoothRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableBluetoothRegexs();
        }
        return new String[0];
    }

    public int setUsbTethering(boolean enable, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (isTetheringSupported()) {
            return this.mTethering.setUsbTethering(enable);
        }
        return 3;
    }

    public String[] getTetherableIfaces() {
        enforceTetherAccessPermission();
        return this.mTethering.getTetherableIfaces();
    }

    public String[] getTetheredIfaces() {
        enforceTetherAccessPermission();
        return this.mTethering.getTetheredIfaces();
    }

    public String[] getTetheringErroredIfaces() {
        enforceTetherAccessPermission();
        return this.mTethering.getErroredIfaces();
    }

    public String[] getTetheredDhcpRanges() {
        enforceConnectivityInternalPermission();
        return this.mTethering.getTetheredDhcpRanges();
    }

    public boolean isTetheringSupported(String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        return isTetheringSupported();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTetheringSupported() {
        boolean tetherEnabledInSettings = toBool(Settings.Global.getInt(this.mContext.getContentResolver(), "tether_supported", encodeBool(this.mSystemProperties.get("ro.tether.denied").equals("true") ^ true))) && !this.mUserManager.hasUserRestriction("no_config_tethering");
        long token = Binder.clearCallingIdentity();
        try {
            boolean adminUser = this.mUserManager.isAdminUser();
            if (!tetherEnabledInSettings || !adminUser || !this.mTethering.hasTetherableConfiguration()) {
                return false;
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        if (!isTetheringSupported()) {
            receiver.send(3, null);
            return;
        }
        HwCustConnectivityService hwCustConnectivityService = this.mCust;
        if (hwCustConnectivityService == null || !hwCustConnectivityService.isNeedCustTethering(this.mContext)) {
            this.mTethering.startTethering(type, receiver, showProvisioningUi);
        }
    }

    public void stopTethering(int type, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.stopTethering(type);
    }

    public void getLatestTetheringEntitlementResult(int type, ResultReceiver receiver, boolean showEntitlementUi, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.getLatestTetheringEntitlementResult(type, receiver, showEntitlementUi);
    }

    public void registerTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.registerTetheringEventCallback(callback);
    }

    public void unregisterTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.unregisterTetheringEventCallback(callback);
    }

    private void ensureNetworkTransitionWakelock(String forWhom) {
        synchronized (this) {
            if (!this.mNetTransitionWakeLock.isHeld()) {
                this.mNetTransitionWakeLock.acquire();
                this.mLastWakeLockAcquireTimestamp = SystemClock.elapsedRealtime();
                this.mTotalWakelockAcquisitions++;
                this.mWakelockLogs.log("ACQUIRE for " + forWhom);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK), (long) this.mNetTransitionWakeLockTimeout);
            }
        }
    }

    private void scheduleReleaseNetworkTransitionWakelock() {
        synchronized (this) {
            if (this.mNetTransitionWakeLock.isHeld()) {
                this.mHandler.removeMessages(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8), 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReleaseNetworkTransitionWakelock(int eventId) {
        String event = eventName(eventId);
        synchronized (this) {
            if (!this.mNetTransitionWakeLock.isHeld()) {
                this.mWakelockLogs.log(String.format("RELEASE: already released (%s)", event));
                Slog.w(TAG, "expected Net Transition WakeLock to be held");
                return;
            }
            this.mNetTransitionWakeLock.release();
            long lockDuration = SystemClock.elapsedRealtime() - this.mLastWakeLockAcquireTimestamp;
            this.mTotalWakelockDurationMs += lockDuration;
            this.mMaxWakelockDurationMs = Math.max(this.mMaxWakelockDurationMs, lockDuration);
            this.mTotalWakelockReleases++;
            this.mWakelockLogs.log(String.format("RELEASE (%s)", event));
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            reportNetworkConnectivity(nai.network, percentage > 50);
        }
    }

    public void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
        enforceAccessPermission();
        enforceInternetPermission();
        int uid = Binder.getCallingUid();
        int connectivityInfo = encodeBool(hasConnectivity);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(36, uid, connectivityInfo, network));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportNetworkConnectivity(Network network, int uid, boolean hasConnectivity) {
        NetworkAgentInfo nai;
        if (network == null) {
            nai = getDefaultNetwork();
        } else {
            nai = getNetworkAgentInfoForNetwork(network);
        }
        if (nai != null && nai.networkInfo.getState() != NetworkInfo.State.DISCONNECTING && nai.networkInfo.getState() != NetworkInfo.State.DISCONNECTED && hasConnectivity != nai.lastValidated) {
            int netid = nai.network.netId;
            log("reportNetworkConnectivity(" + netid + ", " + hasConnectivity + ") by " + uid);
            if (nai.everConnected && !isNetworkWithLinkPropertiesBlocked(getLinkProperties(nai), uid, false)) {
                nai.networkMonitor().forceReevaluation(uid);
            }
        }
    }

    public ProxyInfo getProxyForNetwork(Network network) {
        ProxyInfo globalProxy = this.mProxyTracker.getGlobalProxy();
        if (globalProxy != null) {
            return globalProxy;
        }
        if (network == null) {
            Network activeNetwork = getActiveNetworkForUidInternal(Binder.getCallingUid(), true);
            if (activeNetwork == null) {
                return null;
            }
            return getLinkPropertiesProxyInfo(activeNetwork);
        } else if (queryUserAccess(Binder.getCallingUid(), network.netId)) {
            return getLinkPropertiesProxyInfo(network);
        } else {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean queryUserAccess(int uid, int netId) {
        return NetworkUtils.queryUserAccess(uid, netId);
    }

    private ProxyInfo getLinkPropertiesProxyInfo(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        ProxyInfo proxyInfo = null;
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            ProxyInfo linkHttpProxy = nai.linkProperties.getHttpProxy();
            if (linkHttpProxy != null) {
                proxyInfo = new ProxyInfo(linkHttpProxy);
            }
        }
        return proxyInfo;
    }

    public void setGlobalProxy(ProxyInfo proxyProperties) {
        enforceConnectivityInternalPermission();
        this.mProxyTracker.setGlobalProxy(proxyProperties);
    }

    public ProxyInfo getGlobalProxy() {
        return this.mProxyTracker.getGlobalProxy();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleApplyDefaultProxy(ProxyInfo proxy) {
        if (proxy != null && TextUtils.isEmpty(proxy.getHost()) && Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            proxy = null;
        }
        this.mProxyTracker.setDefaultProxy(proxy);
    }

    private void updateProxy(LinkProperties newLp, LinkProperties oldLp) {
        ProxyInfo oldProxyInfo = null;
        ProxyInfo newProxyInfo = newLp == null ? null : newLp.getHttpProxy();
        if (oldLp != null) {
            oldProxyInfo = oldLp.getHttpProxy();
        }
        if (!ProxyTracker.proxyInfoEqual(newProxyInfo, oldProxyInfo)) {
            this.mProxyTracker.sendProxyBroadcast();
        }
    }

    /* access modifiers changed from: private */
    public static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap<>();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Slog.wtf(ConnectivityService.TAG, "Should never be reached.");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            ConnectivityService.loge("No matching event to send for URI=" + uri);
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Slog.i(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Slog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Slog.e(TAG, s, t);
    }

    public boolean prepareVpn(String oldPackage, String newPackage, int userId) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_PREPAREVPN);
        }
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                return false;
            }
            return vpn.prepare(oldPackage, newPackage);
        }
    }

    public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn != null) {
                vpn.setPackageAuthorization(packageName, authorized);
            }
        }
    }

    public ParcelFileDescriptor establishVpn(VpnConfig config) {
        ParcelFileDescriptor establish;
        if (HwServiceFactory.getHwConnectivityManager().isVpnDisabled()) {
            return null;
        }
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            establish = this.mVpns.get(user).establish(config);
        }
        return establish;
    }

    public void startLegacyVpn(VpnProfile profile) {
        boolean isInsecureVpn = true;
        if (!HwServiceFactory.getHwConnectivityManager().isInsecureVpnDisabled() || !(profile.type == 0 || profile.type == 1)) {
            isInsecureVpn = false;
        }
        if (isInsecureVpn) {
            UiThread.getHandler().post(new Runnable() {
                /* class com.android.server.ConnectivityService.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast toast = Toast.makeText(ConnectivityService.this.mContext, ConnectivityService.this.mContext.getResources().getString(33685912), 1);
                    toast.getWindowParams().type = 2006;
                    toast.show();
                }
            });
            return;
        }
        LinkProperties egress = getActiveLinkProperties();
        if (egress != null) {
            int user = UserHandle.getUserId(Binder.getCallingUid());
            synchronized (this.mVpns) {
                throwIfLockdownEnabled();
                Vpn mVpn = this.mVpns.get(user);
                if (mVpn != null) {
                    mVpn.startLegacyVpn(profile, this.mKeyStore, egress);
                }
            }
            return;
        }
        throw new IllegalStateException("Missing active network connection");
    }

    public LegacyVpnInfo getLegacyVpnInfo(int userId) {
        LegacyVpnInfo legacyVpnInfo;
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            legacyVpnInfo = this.mVpns.get(userId).getLegacyVpnInfo();
        }
        return legacyVpnInfo;
    }

    private VpnInfo[] getAllVpnInfo() {
        ensureRunningOnConnectivityServiceThread();
        synchronized (this.mVpns) {
            if (this.mLockdownEnabled) {
                return new VpnInfo[0];
            }
            List<VpnInfo> infoList = new ArrayList<>();
            for (int i = 0; i < this.mVpns.size(); i++) {
                VpnInfo info = createVpnInfo(this.mVpns.valueAt(i));
                if (info != null) {
                    infoList.add(info);
                }
            }
            return (VpnInfo[]) infoList.toArray(new VpnInfo[infoList.size()]);
        }
    }

    private VpnInfo createVpnInfo(Vpn vpn) {
        LinkProperties linkProperties;
        VpnInfo info = vpn.getVpnInfo();
        if (info == null) {
            return null;
        }
        Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
        if (underlyingNetworks == null) {
            NetworkAgentInfo defaultNetwork = getDefaultNetwork();
            if (!(defaultNetwork == null || defaultNetwork.linkProperties == null)) {
                info.primaryUnderlyingIface = getDefaultNetwork().linkProperties.getInterfaceName();
            }
        } else if (underlyingNetworks.length > 0 && (linkProperties = getLinkProperties(underlyingNetworks[0])) != null) {
            info.primaryUnderlyingIface = linkProperties.getInterfaceName();
        }
        if (info.primaryUnderlyingIface == null) {
            return null;
        }
        return info;
    }

    public VpnConfig getVpnConfig(int userId) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                return null;
            }
            return vpn.getVpnConfig();
        }
    }

    private void updateAllVpnsCapabilities() {
        Network defaultNetwork = getNetwork(getDefaultNetwork());
        synchronized (this.mVpns) {
            for (int i = 0; i < this.mVpns.size(); i++) {
                Vpn vpn = this.mVpns.valueAt(i);
                updateVpnCapabilities(vpn, vpn.updateCapabilities(defaultNetwork));
            }
        }
    }

    private void updateVpnCapabilities(Vpn vpn, NetworkCapabilities nc) {
        ensureRunningOnConnectivityServiceThread();
        NetworkAgentInfo vpnNai = getNetworkAgentInfoForNetId(vpn.getNetId());
        if (vpnNai != null && nc != null) {
            updateCapabilities(vpnNai.getCurrentScore(), vpnNai, nc);
        }
    }

    public boolean updateLockdownVpn() {
        if (Binder.getCallingUid() != 1000) {
            Slog.w(TAG, "Lockdown VPN only available to AID_SYSTEM");
            return false;
        }
        synchronized (this.mVpns) {
            this.mLockdownEnabled = LockdownVpnTracker.isEnabled();
            if (this.mLockdownEnabled) {
                byte[] profileTag = this.mKeyStore.get("LOCKDOWN_VPN");
                if (profileTag == null) {
                    Slog.e(TAG, "Lockdown VPN configured but cannot be read from keystore");
                    return false;
                }
                String profileName = new String(profileTag);
                KeyStore keyStore = this.mKeyStore;
                VpnProfile profile = VpnProfile.decode(profileName, keyStore.get("VPN_" + profileName));
                if (profile == null) {
                    String str = TAG;
                    Slog.e(str, "Lockdown VPN configured invalid profile " + profileName);
                    setLockdownTracker(null);
                    return true;
                }
                int user = UserHandle.getUserId(Binder.getCallingUid());
                Vpn vpn = this.mVpns.get(user);
                if (vpn == null) {
                    String str2 = TAG;
                    Slog.w(str2, "VPN for user " + user + " not ready yet. Skipping lockdown");
                    return false;
                }
                setLockdownTracker(new LockdownVpnTracker(this.mContext, this.mNMS, this, vpn, profile));
            } else {
                setLockdownTracker(null);
            }
            return true;
        }
    }

    @GuardedBy({"mVpns"})
    private void setLockdownTracker(LockdownVpnTracker tracker) {
        LockdownVpnTracker existing = this.mLockdownTracker;
        this.mLockdownTracker = null;
        if (existing != null) {
            existing.shutdown();
        }
        if (tracker != null) {
            this.mLockdownTracker = tracker;
            this.mLockdownTracker.init();
        }
    }

    @GuardedBy({"mVpns"})
    private void throwIfLockdownEnabled() {
        if (this.mLockdownEnabled) {
            throw new IllegalStateException("Unavailable in lockdown mode");
        }
    }

    private boolean startAlwaysOnVpn(int userId) {
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.wtf(str, "User " + userId + " has no Vpn configuration");
                return false;
            }
            return vpn.startAlwaysOnVpn();
        }
    }

    public boolean isAlwaysOnVpnPackageSupported(int userId, String packageName) {
        enforceSettingsPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return false;
            }
            return vpn.isAlwaysOnPackageSupported(packageName);
        }
    }

    public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown, List<String> lockdownWhitelist) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            if (LockdownVpnTracker.isEnabled()) {
                return false;
            }
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return false;
            } else if (!vpn.setAlwaysOnPackage(packageName, lockdown, lockdownWhitelist)) {
                return false;
            } else {
                if (startAlwaysOnVpn(userId)) {
                    return true;
                }
                vpn.setAlwaysOnPackage(null, false, null);
                return false;
            }
        }
    }

    public String getAlwaysOnVpnPackage(int userId) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return null;
            }
            return vpn.getAlwaysOnPackage();
        }
    }

    public boolean isVpnLockdownEnabled(int userId) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return false;
            }
            return vpn.getLockdown();
        }
    }

    public List<String> getVpnLockdownWhitelist(int userId) {
        enforceControlAlwaysOnVpnPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return null;
            }
            return vpn.getLockdownWhitelist();
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        return -1;
    }

    private String getProvisioningUrlBaseFromFile() {
        String mcc;
        String mnc;
        FileReader fileReader = null;
        Configuration config = this.mContext.getResources().getConfiguration();
        try {
            FileReader fileReader2 = new FileReader(this.mProvisioningUrlFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileReader2);
            XmlUtils.beginDocument(parser, TAG_PROVISIONING_URLS);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    try {
                        fileReader2.close();
                    } catch (IOException e) {
                    }
                    return null;
                } else if (element.equals(TAG_PROVISIONING_URL) && (mcc = parser.getAttributeValue(null, ATTR_MCC)) != null) {
                    try {
                        if (Integer.parseInt(mcc) == config.mcc && (mnc = parser.getAttributeValue(null, ATTR_MNC)) != null && Integer.parseInt(mnc) == config.mnc) {
                            parser.next();
                            if (parser.getEventType() == 4) {
                                String text = parser.getText();
                                try {
                                    fileReader2.close();
                                } catch (IOException e2) {
                                }
                                return text;
                            }
                        }
                    } catch (NumberFormatException e3) {
                        loge("NumberFormatException in getProvisioningUrlBaseFromFile: " + e3);
                    }
                }
            }
        } catch (FileNotFoundException e4) {
            loge("Carrier Provisioning Urls file not found");
            if (0 != 0) {
                fileReader.close();
            }
        } catch (XmlPullParserException e5) {
            loge("Xml parser exception reading Carrier Provisioning Urls file: " + e5);
            if (0 != 0) {
                fileReader.close();
            }
        } catch (IOException e6) {
            loge("I/O exception reading Carrier Provisioning Urls file: " + e6);
            if (0 != 0) {
                try {
                    fileReader.close();
                } catch (IOException e7) {
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileReader.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
        return null;
    }

    public String getMobileProvisioningUrl() {
        enforceConnectivityInternalPermission();
        String url = getProvisioningUrlBaseFromFile();
        if (TextUtils.isEmpty(url)) {
            url = this.mContext.getResources().getString(17040622);
            log("getMobileProvisioningUrl: mobile_provisioining_url from resource =" + url);
        } else {
            log("getMobileProvisioningUrl: mobile_provisioning_url from File =" + url);
        }
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        String phoneNumber = this.mTelephonyManager.getLine1Number();
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = "0000000000";
        }
        return String.format(url, this.mTelephonyManager.getSimSerialNumber(), this.mTelephonyManager.getDeviceId(), phoneNumber);
    }

    public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) {
        enforceConnectivityInternalPermission();
        if (ConnectivityManager.isNetworkTypeValid(networkType)) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mNotifier.setProvNotificationVisible(visible, networkType + 1 + 64512, action);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setAirplaneMode(boolean enable) {
        enforceNetworkStackSettingsOrSetup();
        long ident = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", encodeBool(enable));
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", enable);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserStart(int userId) {
        synchronized (this.mVpns) {
            if (this.mVpns.get(userId) != null) {
                loge("Starting user already has a VPN");
                return;
            }
            this.mVpns.put(userId, new Vpn(this.mHandler.getLooper(), this.mContext, this.mNMS, userId));
            if (this.mUserManager.getUserInfo(userId).isPrimary() && LockdownVpnTracker.isEnabled()) {
                updateLockdownVpn();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserStop(int userId) {
        synchronized (this.mVpns) {
            Vpn userVpn = this.mVpns.get(userId);
            if (userVpn == null) {
                loge("Stopped user has no VPN");
                return;
            }
            userVpn.onUserStopped();
            this.mVpns.delete(userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserAdded(int userId) {
        this.mPermissionMonitor.onUserAdded(userId);
        Network defaultNetwork = getNetwork(getDefaultNetwork());
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                Vpn vpn = this.mVpns.valueAt(i);
                vpn.onUserAdded(userId);
                updateVpnCapabilities(vpn, vpn.updateCapabilities(defaultNetwork));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserRemoved(int userId) {
        this.mPermissionMonitor.onUserRemoved(userId);
        Network defaultNetwork = getNetwork(getDefaultNetwork());
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                Vpn vpn = this.mVpns.valueAt(i);
                vpn.onUserRemoved(userId);
                updateVpnCapabilities(vpn, vpn.updateCapabilities(defaultNetwork));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPackageAdded(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName) || uid < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid package in onPackageAdded: " + packageName + " | " + uid);
            return;
        }
        this.mPermissionMonitor.onPackageAdded(packageName, uid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPackageReplaced(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName) || uid < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid package in onPackageReplaced: " + packageName + " | " + uid);
            return;
        }
        int userId = UserHandle.getUserId(uid);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn != null) {
                if (TextUtils.equals(vpn.getAlwaysOnPackage(), packageName)) {
                    String str2 = TAG;
                    Slog.i(str2, "Restarting always-on VPN package " + packageName + " for user " + userId);
                    vpn.startAlwaysOnVpn();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPackageRemoved(String packageName, int uid, boolean isReplacing) {
        if (TextUtils.isEmpty(packageName) || uid < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid package in onPackageRemoved: " + packageName + " | " + uid);
            return;
        }
        this.mPermissionMonitor.onPackageRemoved(uid);
        int userId = UserHandle.getUserId(uid);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn != null) {
                if (TextUtils.equals(vpn.getAlwaysOnPackage(), packageName) && !isReplacing) {
                    String str2 = TAG;
                    Slog.i(str2, "Removing always-on VPN package " + packageName + " for user " + userId);
                    vpn.setAlwaysOnPackage(null, false, null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserUnlocked(int userId) {
        synchronized (this.mVpns) {
            if (!this.mUserManager.getUserInfo(userId).isPrimary() || !LockdownVpnTracker.isEnabled()) {
                startAlwaysOnVpn(userId);
            } else {
                updateLockdownVpn();
            }
        }
    }

    /* access modifiers changed from: protected */
    public static class NetworkFactoryInfo {
        public final AsyncChannel asyncChannel;
        public final int factorySerialNumber;
        public final Messenger messenger;
        public final String name;

        NetworkFactoryInfo(String name2, Messenger messenger2, AsyncChannel asyncChannel2, int factorySerialNumber2) {
            this.name = name2;
            this.messenger = messenger2;
            this.asyncChannel = asyncChannel2;
            this.factorySerialNumber = factorySerialNumber2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ensureNetworkRequestHasType(NetworkRequest request) {
        if (request.type == NetworkRequest.Type.NONE) {
            throw new IllegalArgumentException("All NetworkRequests in ConnectivityService must have a type");
        }
    }

    /* access modifiers changed from: protected */
    public class NetworkRequestInfo implements IBinder.DeathRecipient {
        NetworkRequest clientRequest = null;
        private final IBinder mBinder;
        final PendingIntent mPendingIntent;
        boolean mPendingIntentSent;
        final int mPid;
        AbstractConnectivityService.DomainPreferType mPreferType = null;
        final int mUid;
        final Messenger messenger;
        final NetworkRequest request;

        NetworkRequestInfo(NetworkRequest r, PendingIntent pi) {
            this.request = r;
            ConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mPendingIntent = pi;
            this.messenger = null;
            this.mBinder = null;
            this.mPid = Binder.getCallingPid();
            this.mUid = Binder.getCallingUid();
            enforceRequestCountLimit();
        }

        NetworkRequestInfo(Messenger m, NetworkRequest r, IBinder binder) {
            this.messenger = m;
            this.request = r;
            ConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mBinder = binder;
            this.mPid = Binder.getCallingPid();
            this.mUid = Binder.getCallingUid();
            this.mPendingIntent = null;
            enforceRequestCountLimit();
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        private void enforceRequestCountLimit() {
            synchronized (ConnectivityService.this.mUidToNetworkRequestCount) {
                int networkRequests = ConnectivityService.this.mUidToNetworkRequestCount.get(this.mUid, 0) + 1;
                if (networkRequests < 100) {
                    ConnectivityService.this.mUidToNetworkRequestCount.put(this.mUid, networkRequests);
                } else {
                    throw new ServiceSpecificException(1);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void unlinkDeathRecipient() {
            IBinder iBinder = this.mBinder;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            ConnectivityService.log("ConnectivityService NetworkRequestInfo binderDied(" + this.request + ", " + this.mBinder + ")");
            ConnectivityService.this.releaseNetworkRequest(this.request);
        }

        @Override // java.lang.Object
        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("uid/pid:");
            sb.append(this.mUid);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb.append(this.mPid);
            sb.append(" ");
            sb.append(this.request);
            if (this.mPendingIntent == null) {
                str = "";
            } else {
                str = " to trigger " + this.mPendingIntent;
            }
            sb.append(str);
            return sb.toString();
        }
    }

    private void ensureRequestableCapabilities(NetworkCapabilities networkCapabilities) {
        String badCapability = networkCapabilities.describeFirstNonRequestableCapability();
        if (badCapability != null) {
            throw new IllegalArgumentException("Cannot request network with " + badCapability);
        }
    }

    private void ensureSufficientPermissionsForRequest(NetworkCapabilities nc, int callerPid, int callerUid) {
        if (nc.getSSID() != null && !checkSettingsPermission(callerPid, callerUid)) {
            throw new SecurityException("Insufficient permissions to request a specific SSID");
        } else if (nc.hasSignalStrength() && !checkNetworkSignalStrengthWakeupPermission(callerPid, callerUid)) {
            throw new SecurityException("Insufficient permissions to request a specific signal strength");
        }
    }

    private ArrayList<Integer> getSignalStrengthThresholds(NetworkAgentInfo nai) {
        SortedSet<Integer> thresholds = new TreeSet<>();
        synchronized (nai) {
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && nai.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    thresholds.add(Integer.valueOf(nri.request.networkCapabilities.getSignalStrength()));
                }
            }
        }
        return new ArrayList<>(thresholds);
    }

    private void updateSignalStrengthThresholds(NetworkAgentInfo nai, String reason, NetworkRequest request) {
        String detail;
        ArrayList<Integer> thresholdsArray = getSignalStrengthThresholds(nai);
        Bundle thresholds = new Bundle();
        thresholds.putIntegerArrayList("thresholds", thresholdsArray);
        if (request == null || !request.networkCapabilities.hasSignalStrength()) {
            detail = reason;
        } else {
            detail = reason + " " + request.networkCapabilities.getSignalStrength();
        }
        log(String.format("updateSignalStrengthThresholds: %s, sending %s to %s", detail, Arrays.toString(thresholdsArray.toArray()), nai.name()));
        nai.asyncChannel.sendMessage(528398, 0, 0, thresholds);
    }

    private void ensureValidNetworkSpecifier(NetworkCapabilities nc) {
        NetworkSpecifier ns;
        if (nc != null && (ns = nc.getNetworkSpecifier()) != null) {
            MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(ns);
            ns.assertValidFromUid(Binder.getCallingUid());
        }
    }

    public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutMs, IBinder binder, int legacyType) {
        NetworkRequest.Type type;
        NetworkCapabilities networkCapabilities2;
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONNECTIVITY_REQUESTNETWORK);
        }
        if (networkCapabilities == null) {
            type = NetworkRequest.Type.TRACK_DEFAULT;
        } else {
            type = NetworkRequest.Type.REQUEST;
        }
        if (type == NetworkRequest.Type.TRACK_DEFAULT) {
            networkCapabilities2 = createDefaultNetworkCapabilitiesForUid(Binder.getCallingUid());
            enforceAccessPermission();
        } else {
            networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
            enforceNetworkRequestPermissions(networkCapabilities2);
            enforceMeteredApnPolicy(networkCapabilities2);
        }
        ensureRequestableCapabilities(networkCapabilities2);
        ensureSufficientPermissionsForRequest(networkCapabilities2, Binder.getCallingPid(), Binder.getCallingUid());
        restrictRequestUidsForCaller(networkCapabilities2);
        if (timeoutMs >= 0) {
            ensureValidNetworkSpecifier(networkCapabilities2);
            NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, legacyType, nextNetworkRequestId(), type);
            NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
            log("requestNetwork for " + nri);
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessage(internalHandler.obtainMessage(19, nri));
            if (timeoutMs > 0) {
                InternalHandler internalHandler2 = this.mHandler;
                internalHandler2.sendMessageDelayed(internalHandler2.obtainMessage(20, nri), (long) timeoutMs);
            }
            return networkRequest;
        }
        throw new IllegalArgumentException("Bad timeout specified");
    }

    private void enforceNetworkRequestPermissions(NetworkCapabilities networkCapabilities) {
        if (!networkCapabilities.hasCapability(13)) {
            enforceConnectivityRestrictedNetworksPermission();
        } else {
            enforceChangePermission();
        }
    }

    public boolean requestBandwidthUpdate(Network network) {
        NetworkAgentInfo nai;
        enforceAccessPermission();
        if (network == null) {
            return false;
        }
        synchronized (this.mNetworkForNetId) {
            nai = this.mNetworkForNetId.get(network.netId);
        }
        if (nai == null) {
            return false;
        }
        nai.asyncChannel.sendMessage(528394);
        synchronized (this.mBandwidthRequests) {
            int uid = Binder.getCallingUid();
            Integer uidReqs = this.mBandwidthRequests.get(uid);
            if (uidReqs == null) {
                uidReqs = new Integer(0);
            }
            this.mBandwidthRequests.put(uid, Integer.valueOf(uidReqs.intValue() + 1));
        }
        return true;
    }

    private boolean isSystem(int uid) {
        return uid < 10000;
    }

    private void enforceMeteredApnPolicy(NetworkCapabilities networkCapabilities) {
        int uid = Binder.getCallingUid();
        if (!isSystem(uid) && !networkCapabilities.hasCapability(11) && this.mPolicyManagerInternal.isUidRestrictedOnMeteredNetworks(uid)) {
            networkCapabilities.addCapability(11);
        }
    }

    public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        enforceNetworkRequestPermissions(networkCapabilities2);
        enforceMeteredApnPolicy(networkCapabilities2);
        ensureRequestableCapabilities(networkCapabilities2);
        ensureSufficientPermissionsForRequest(networkCapabilities2, Binder.getCallingPid(), Binder.getCallingUid());
        ensureValidNetworkSpecifier(networkCapabilities2);
        restrictRequestUidsForCaller(networkCapabilities2);
        NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, -1, nextNetworkRequestId(), NetworkRequest.Type.REQUEST);
        NetworkRequestInfo nri = new NetworkRequestInfo(networkRequest, operation);
        log("pendingRequest for " + nri);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(26, nri));
        return networkRequest;
    }

    private void releasePendingNetworkRequestWithDelay(PendingIntent operation) {
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessageDelayed(internalHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation), (long) this.mReleasePendingIntentDelayMs);
    }

    public void releasePendingNetworkRequest(PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation));
    }

    private boolean hasWifiNetworkListenPermission(NetworkCapabilities nc) {
        if (nc == null) {
            return false;
        }
        int[] transportTypes = nc.getTransportTypes();
        if (transportTypes.length != 1 || transportTypes[0] != 1) {
            return false;
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "ConnectivityService");
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) {
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        ensureSufficientPermissionsForRequest(networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
        restrictRequestUidsForCaller(nc);
        restrictBackgroundRequestForCaller(nc);
        ensureValidNetworkSpecifier(nc);
        NetworkRequest networkRequest = new NetworkRequest(nc, -1, nextNetworkRequestId(), NetworkRequest.Type.LISTEN);
        NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
        log("listenForNetwork for " + nri);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(21, nri));
        return networkRequest;
    }

    public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        ensureValidNetworkSpecifier(networkCapabilities);
        ensureSufficientPermissionsForRequest(networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        restrictRequestUidsForCaller(nc);
        NetworkRequestInfo nri = new NetworkRequestInfo(new NetworkRequest(nc, -1, nextNetworkRequestId(), NetworkRequest.Type.LISTEN), operation);
        log("pendingListenForNetwork for " + nri);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(21, nri));
    }

    public void releaseNetworkRequest(NetworkRequest networkRequest) {
        ensureNetworkRequestHasType(networkRequest);
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(22, getCallingUid(), 0, networkRequest));
    }

    public int registerNetworkFactory(Messenger messenger, String name) {
        enforceConnectivityInternalPermission();
        NetworkFactoryInfo nfi = new NetworkFactoryInfo(name, messenger, new AsyncChannel(), NetworkFactory.SerialNumber.nextSerialNumber());
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(17, nfi));
        return nfi.factorySerialNumber;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRegisterNetworkFactory(NetworkFactoryInfo nfi) {
        log("Got NetworkFactory Messenger for " + nfi.name);
        this.mNetworkFactoryInfos.put(nfi.messenger, nfi);
        nfi.asyncChannel.connect(this.mContext, this.mTrackerHandler, nfi.messenger);
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        enforceConnectivityInternalPermission();
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(EVENT_UNREGISTER_NETWORK_FACTORY, messenger));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUnregisterNetworkFactory(Messenger messenger) {
        NetworkFactoryInfo nfi = this.mNetworkFactoryInfos.remove(messenger);
        if (nfi == null) {
            loge("Failed to find Messenger in unregisterNetworkFactory");
            return;
        }
        log("unregisterNetworkFactory for " + nfi.name);
    }

    private NetworkAgentInfo getNetworkForRequest(int requestId) {
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForRequestId) {
            networkAgentInfo = this.mNetworkForRequestId.get(requestId);
        }
        return networkAgentInfo;
    }

    private void clearNetworkForRequest(int requestId) {
        synchronized (this.mNetworkForRequestId) {
            this.mNetworkForRequestId.remove(requestId);
        }
    }

    private void setNetworkForRequest(int requestId, NetworkAgentInfo nai) {
        synchronized (this.mNetworkForRequestId) {
            this.mNetworkForRequestId.put(requestId, nai);
        }
    }

    private NetworkAgentInfo getDefaultNetwork() {
        Object obj = getNetworkForRequest(this.mDefaultRequest.requestId);
        if (obj instanceof NetworkAgentInfo) {
            return (NetworkAgentInfo) obj;
        }
        return null;
    }

    private Network getNetwork(NetworkAgentInfo nai) {
        if (nai != null) {
            return nai.network;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ensureRunningOnConnectivityServiceThread() {
        if (this.mHandler.getLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Not running on ConnectivityService thread: " + Thread.currentThread().getName());
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isDefaultNetwork(NetworkAgentInfo nai) {
        return nai == getDefaultNetwork();
    }

    private boolean isDefaultRequest(NetworkRequestInfo nri) {
        return nri.request.requestId == this.mDefaultRequest.requestId;
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc) {
        return registerNetworkAgent(messenger, networkInfo, linkProperties, networkCapabilities, currentScore, networkMisc, -1);
    }

    /* JADX INFO: finally extract failed */
    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc, int factorySerialNumber) {
        enforceConnectivityInternalPermission();
        LinkProperties lp = new LinkProperties(linkProperties);
        lp.ensureDirectlyConnectedRoutes();
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        NetworkAgentInfo nai = new NetworkAgentInfo(messenger, new AsyncChannel(), new Network(reserveNetId()), new NetworkInfo(networkInfo), lp, nc, currentScore, this.mContext, this.mTrackerHandler, new NetworkMisc(networkMisc), this, this.mNetd, this.mDnsResolver, this.mNMS, factorySerialNumber);
        nai.setNetworkCapabilities(mixInCapabilities(nai, nc));
        String extraInfo = networkInfo.getExtraInfo();
        String name = TextUtils.isEmpty(extraInfo) ? nai.networkCapabilities.getSSID() : extraInfo;
        if (HW_DEBUGGABLE) {
            log("registerNetworkAgent " + nai);
        }
        long token = Binder.clearCallingIdentity();
        try {
            getNetworkStack().makeNetworkMonitor(nai.network, name, new NetworkMonitorCallbacks(nai));
            Binder.restoreCallingIdentity(token);
            return nai.network.netId;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public NetworkStackClient getNetworkStack() {
        return NetworkStackClient.getInstance();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRegisterNetworkAgent(NetworkAgentInfo nai, INetworkMonitor networkMonitor) {
        nai.onNetworkMonitorCreated(networkMonitor);
        log("Got NetworkAgent Messenger");
        NetworkAgentInfo identicalNai = this.mHwCSEx.getIdenticalActiveNetworkAgentInfo(nai);
        if (identicalNai != null) {
            synchronized (identicalNai) {
                identicalNai.networkCapabilities.combineCapabilitiesWithNoSpecifiers(nai.networkCapabilities);
            }
            try {
                networkMonitor.start();
                networkMonitor.notifyNetworkDisconnected();
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
            rematchNetworkAndRequests(identicalNai, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
            return;
        }
        this.mNetworkAgentInfos.put(nai.messenger, nai);
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.put(nai.network.netId, nai);
            sendNetworkStickyBroadcastAsUser("remove", nai);
        }
        try {
            networkMonitor.start();
        } catch (RemoteException e2) {
            e2.rethrowAsRuntimeException();
        }
        nai.asyncChannel.connect(this.mContext, this.mTrackerHandler, nai.messenger);
        NetworkInfo networkInfo = nai.networkInfo;
        nai.networkInfo = null;
        updateNetworkInfo(nai, networkInfo);
        updateUids(nai, null, nai.networkCapabilities);
        handleLteMobileDataStateChange(networkInfo);
        if (isTv && nai.linkProperties != null) {
            if ((IFACE_ETH0.equals(nai.linkProperties.getInterfaceName()) || IFACE_ETH1.equals(nai.linkProperties.getInterfaceName())) && isEthernetPriority) {
                for (NetworkAgentInfo n : this.mNetworkAgentInfos.values()) {
                    if (n != null && !n.isEthernet()) {
                        teardownUnneededNetwork(n);
                    }
                }
            }
        }
    }

    private void updateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties newLp, LinkProperties oldLp) {
        int netId = networkAgent.network.netId;
        networkAgent.clatd.fixupLinkProperties(oldLp, newLp);
        updateInterfaces(newLp, oldLp, netId, networkAgent.networkCapabilities);
        updateVpnFiltering(newLp, oldLp, networkAgent);
        updateMtu(newLp, oldLp);
        if (isDefaultNetwork(networkAgent)) {
            updateTcpBufferSizes(newLp.getTcpBufferSizes());
        }
        updateRoutes(newLp, oldLp, netId);
        updateDnses(newLp, oldLp, netId);
        this.mDnsManager.updatePrivateDnsStatus(netId, newLp);
        if (isDefaultNetwork(networkAgent)) {
            handleApplyDefaultProxy(newLp.getHttpProxy());
        } else {
            updateProxy(newLp, oldLp);
        }
        if (!Objects.equals(newLp, oldLp)) {
            synchronized (networkAgent) {
                networkAgent.linkProperties = newLp;
            }
            networkAgent.clatd.update();
            notifyIfacesChangedForNetworkStats();
            networkAgent.networkMonitor().notifyLinkPropertiesChanged(newLp);
            if (networkAgent.everConnected) {
                notifyNetworkCallbacks(networkAgent, 524295);
            }
        }
        this.mKeepaliveTracker.handleCheckKeepalivesStillValid(networkAgent);
    }

    private void wakeupModifyInterface(String iface, NetworkCapabilities caps, boolean add) {
        if (caps.hasTransport(1)) {
            int mark = this.mContext.getResources().getInteger(17694857);
            int mask = this.mContext.getResources().getInteger(17694858);
            if (mark != 0 && mask != 0) {
                String prefix = "iface:" + iface;
                if (add) {
                    try {
                        this.mNetd.wakeupAddInterface(iface, prefix, mark, mask);
                    } catch (Exception e) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Exception modifying wakeup packet monitoring: ");
                        sb.append(HW_DEBUGGABLE ? e : "*");
                        loge(sb.toString());
                    }
                } else {
                    this.mNetd.wakeupDelInterface(iface, prefix, mark, mask);
                }
            }
        }
    }

    private void updateInterfaces(LinkProperties newLp, LinkProperties oldLp, int netId, NetworkCapabilities caps) {
        Serializable serializable;
        List list = null;
        List allInterfaceNames = oldLp != null ? oldLp.getAllInterfaceNames() : null;
        if (newLp != null) {
            list = newLp.getAllInterfaceNames();
        }
        LinkProperties.CompareResult<String> interfaceDiff = new LinkProperties.CompareResult<>(allInterfaceNames, list);
        Iterator it = interfaceDiff.added.iterator();
        while (true) {
            serializable = "*";
            if (!it.hasNext()) {
                break;
            }
            String iface = (String) it.next();
            try {
                log("Adding iface " + iface + " to network " + netId);
                this.mNMS.addInterfaceToNetwork(iface, netId);
                wakeupModifyInterface(iface, caps, true);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception adding interface: ");
                if (HW_DEBUGGABLE) {
                    serializable = e;
                }
                sb.append(serializable);
                loge(sb.toString());
            }
        }
        for (String iface2 : interfaceDiff.removed) {
            try {
                log("Removing iface " + iface2 + " from network " + netId);
                wakeupModifyInterface(iface2, caps, false);
                this.mNMS.removeInterfaceFromNetwork(iface2, netId);
            } catch (Exception e2) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Exception removing interface: ");
                sb2.append(HW_DEBUGGABLE ? e2 : serializable);
                loge(sb2.toString());
            }
        }
    }

    private boolean updateRoutes(LinkProperties newLp, LinkProperties oldLp, int netId) {
        Serializable serializable;
        List list = null;
        List allRoutes = oldLp != null ? oldLp.getAllRoutes() : null;
        if (newLp != null) {
            list = newLp.getAllRoutes();
        }
        LinkProperties.CompareResult<RouteInfo> routeDiff = new LinkProperties.CompareResult<>(allRoutes, list);
        if (HW_DEBUGGABLE) {
            log(" to network " + netId);
        }
        Iterator it = routeDiff.added.iterator();
        while (true) {
            serializable = "*";
            if (!it.hasNext()) {
                break;
            }
            RouteInfo route = (RouteInfo) it.next();
            if (!route.hasGateway()) {
                try {
                    this.mNMS.addRoute(netId, route);
                } catch (Exception e) {
                    boolean z = route.getDestination().getAddress() instanceof Inet4Address;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Exception in addRoute for non-gateway: ");
                    if (HW_DEBUGGABLE) {
                        serializable = e;
                    }
                    sb.append(serializable);
                    loge(sb.toString());
                }
            }
        }
        for (RouteInfo route2 : routeDiff.added) {
            if (route2.hasGateway()) {
                try {
                    this.mNMS.addRoute(netId, route2);
                } catch (Exception e2) {
                    boolean z2 = route2.getGateway() instanceof Inet4Address;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Exception in addRoute for gateway: ");
                    sb2.append(HW_DEBUGGABLE ? e2 : serializable);
                    loge(sb2.toString());
                }
            }
        }
        for (RouteInfo route3 : routeDiff.removed) {
            if (HW_DEBUGGABLE) {
                log("Removing Route [" + route3 + "] from network " + netId);
            }
            try {
                this.mNMS.removeRoute(netId, route3);
            } catch (Exception e3) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Exception in removeRoute: ");
                sb3.append(HW_DEBUGGABLE ? e3 : serializable);
                loge(sb3.toString());
            }
        }
        return !routeDiff.added.isEmpty() || !routeDiff.removed.isEmpty();
    }

    private void updateDnses(LinkProperties newLp, LinkProperties oldLp, int netId) {
        if (oldLp == null || !newLp.isIdenticalDnses(oldLp)) {
            NetworkAgentInfo defaultNai = getDefaultNetwork();
            boolean isDefaultNetwork = defaultNai != null && defaultNai.network.netId == netId;
            Collection<InetAddress> dnses = newLp.getDnsServers();
            log("Setting DNS servers for network " + netId + " to " + dnses);
            try {
                this.mDnsManager.setDnsConfigurationForNetwork(netId, newLp, isDefaultNetwork);
            } catch (Exception e) {
                loge("Exception in setDnsConfigurationForNetwork: " + e);
            }
        }
    }

    private void updateVpnFiltering(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        String newIface = null;
        String oldIface = oldLp != null ? oldLp.getInterfaceName() : null;
        if (newLp != null) {
            newIface = newLp.getInterfaceName();
        }
        boolean wasFiltering = requiresVpnIsolation(nai, nai.networkCapabilities, oldLp);
        boolean needsFiltering = requiresVpnIsolation(nai, nai.networkCapabilities, newLp);
        if (!wasFiltering && !needsFiltering) {
            return;
        }
        if (!Objects.equals(oldIface, newIface) || wasFiltering != needsFiltering) {
            Set<UidRange> ranges = nai.networkCapabilities.getUids();
            int vpnAppUid = nai.networkCapabilities.getEstablishingVpnAppUid();
            if (wasFiltering) {
                this.mPermissionMonitor.onVpnUidRangesRemoved(oldIface, ranges, vpnAppUid);
            }
            if (needsFiltering) {
                this.mPermissionMonitor.onVpnUidRangesAdded(newIface, ranges, vpnAppUid);
            }
        }
    }

    private int getNetworkPermission(NetworkCapabilities nc) {
        if (!nc.hasCapability(13)) {
            return 2;
        }
        if (!nc.hasCapability(19)) {
            return 1;
        }
        return 0;
    }

    private NetworkCapabilities mixInCapabilities(NetworkAgentInfo nai, NetworkCapabilities nc) {
        if (nai.everConnected && !nai.isVPN() && !nai.networkCapabilities.satisfiedByImmutableNetworkCapabilities(nc) && !TextUtils.isEmpty(nai.networkCapabilities.describeImmutableDifferences(nc))) {
            String str = TAG;
            Slog.wtf(str, "BUG: " + nai + " lost immutable capabilities");
        }
        NetworkCapabilities newNc = new NetworkCapabilities(nc);
        if (nai.lastValidated) {
            newNc.addCapability(16);
        } else {
            newNc.removeCapability(16);
        }
        if (nai.lastCaptivePortalDetected) {
            newNc.addCapability(17);
        } else {
            newNc.removeCapability(17);
        }
        if (nai.isBackgroundNetwork()) {
            newNc.removeCapability(19);
        } else {
            newNc.addCapability(19);
        }
        if (nai.isSuspended()) {
            newNc.removeCapability(21);
        } else {
            newNc.addCapability(21);
        }
        if (nai.partialConnectivity) {
            newNc.addCapability(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK);
        } else {
            newNc.removeCapability(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK);
        }
        return newNc;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCapabilities(int oldScore, NetworkAgentInfo nai, NetworkCapabilities nc) {
        NetworkCapabilities prevNc;
        NetworkCapabilities newNc = mixInCapabilities(nai, nc);
        if (!Objects.equals(nai.networkCapabilities, newNc)) {
            int oldPermission = getNetworkPermission(nai.networkCapabilities);
            int newPermission = getNetworkPermission(newNc);
            if (oldPermission != newPermission && nai.created && !nai.isVPN()) {
                try {
                    this.mNMS.setNetworkPermission(nai.network.netId, newPermission);
                } catch (RemoteException | IllegalStateException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Exception in setNetworkPermission: ");
                    sb.append(HW_DEBUGGABLE ? e : "*");
                    loge(sb.toString());
                }
            }
            synchronized (nai) {
                prevNc = nai.networkCapabilities;
                nai.setNetworkCapabilities(newNc);
            }
            updateUids(nai, prevNc, newNc);
            boolean roamingChanged = true;
            if (nai.getCurrentScore() != oldScore || !newNc.equalRequestableCapabilities(prevNc)) {
                rematchAllNetworksAndRequests(nai, oldScore);
                notifyNetworkCallbacks(nai, 524294);
            } else {
                processListenRequests(nai, true);
            }
            if (prevNc != null) {
                boolean oldMetered = prevNc.isMetered();
                boolean newMetered = newNc.isMetered();
                boolean meteredChanged = oldMetered != newMetered;
                if (meteredChanged) {
                    boolean z = this.mRestrictBackground;
                    maybeNotifyNetworkBlocked(nai, oldMetered, newMetered, z, z);
                }
                if (prevNc.hasCapability(18) == newNc.hasCapability(18)) {
                    roamingChanged = false;
                }
                if (meteredChanged || roamingChanged) {
                    notifyIfacesChangedForNetworkStats();
                }
            }
            if (!newNc.hasTransport(4)) {
                updateAllVpnsCapabilities();
            }
        }
    }

    private boolean requiresVpnIsolation(NetworkAgentInfo nai, NetworkCapabilities nc, LinkProperties lp) {
        if (nc == null || lp == null || !nai.isVPN() || nai.networkMisc.allowBypass || nc.getEstablishingVpnAppUid() == 1000 || lp.getInterfaceName() == null) {
            return false;
        }
        if (lp.hasIPv4DefaultRoute() || lp.hasIPv6DefaultRoute()) {
            return true;
        }
        return false;
    }

    private void updateUids(NetworkAgentInfo nai, NetworkCapabilities prevNc, NetworkCapabilities newNc) {
        Set<UidRange> newRanges = null;
        Set<UidRange> prevRanges = prevNc == null ? null : prevNc.getUids();
        if (newNc != null) {
            newRanges = newNc.getUids();
        }
        if (prevRanges == null) {
            prevRanges = new ArraySet<>();
        }
        if (newRanges == null) {
            newRanges = new ArraySet<>();
        }
        Set<UidRange> prevRangesCopy = new ArraySet<>(prevRanges);
        prevRanges.removeAll(newRanges);
        newRanges.removeAll(prevRangesCopy);
        try {
            if (!newRanges.isEmpty()) {
                UidRange[] addedRangesArray = new UidRange[newRanges.size()];
                newRanges.toArray(addedRangesArray);
                this.mNMS.addVpnUidRanges(nai.network.netId, addedRangesArray);
            }
            if (!prevRanges.isEmpty()) {
                UidRange[] removedRangesArray = new UidRange[prevRanges.size()];
                prevRanges.toArray(removedRangesArray);
                this.mNMS.removeVpnUidRanges(nai.network.netId, removedRangesArray);
            }
            boolean wasFiltering = requiresVpnIsolation(nai, prevNc, nai.linkProperties);
            boolean shouldFilter = requiresVpnIsolation(nai, newNc, nai.linkProperties);
            String iface = nai.linkProperties.getInterfaceName();
            if (wasFiltering && !prevRanges.isEmpty()) {
                this.mPermissionMonitor.onVpnUidRangesRemoved(iface, prevRanges, prevNc.getEstablishingVpnAppUid());
            }
            if (shouldFilter && !newRanges.isEmpty()) {
                this.mPermissionMonitor.onVpnUidRangesAdded(iface, newRanges, newNc.getEstablishingVpnAppUid());
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in updateUids: ");
            sb.append(HW_DEBUGGABLE ? e : "*");
            loge(sb.toString());
        }
    }

    public void handleUpdateLinkProperties(NetworkAgentInfo nai, LinkProperties newLp) {
        ensureRunningOnConnectivityServiceThread();
        if (getNetworkAgentInfoForNetId(nai.network.netId) == nai) {
            newLp.ensureDirectlyConnectedRoutes();
            log("Update of LinkProperties for " + nai.name() + "; created=" + nai.created + "; everConnected=" + nai.everConnected);
            updateLinkProperties(nai, newLp, new LinkProperties(nai.linkProperties));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUpdatedScoreToFactories(NetworkAgentInfo nai) {
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest nr = nai.requestAt(i);
            if (!nr.isListen()) {
                sendUpdatedScoreToFactories(nr, nai);
            }
        }
    }

    private void sendUpdatedScoreToFactories(NetworkRequest networkRequest, NetworkAgentInfo nai) {
        int score = 0;
        int serial = 0;
        if (nai != null) {
            score = nai.getCurrentScore();
            serial = nai.factorySerialNumber;
        }
        if (DDBG) {
            log("sending new Min Network Score(" + score + "): " + networkRequest.toString());
        }
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            nfi.asyncChannel.sendMessage(536576, score, serial, networkRequest);
        }
    }

    private void sendUpdatedScoreToFactoriesWhenWifiDisconnected(NetworkRequest networkRequest, int score) {
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            if (!"Telephony".equals(nfi.name)) {
                nfi.asyncChannel.sendMessage(536576, score, 0, networkRequest);
            }
        }
    }

    private void sendPendingIntentForRequest(NetworkRequestInfo nri, NetworkAgentInfo networkAgent, int notificationType) {
        if (notificationType == 524290 && !nri.mPendingIntentSent) {
            Intent intent = new Intent();
            intent.putExtra("android.net.extra.NETWORK", networkAgent.network);
            intent.putExtra("android.net.extra.NETWORK_REQUEST", nri.clientRequest != null ? nri.clientRequest : nri.request);
            nri.mPendingIntentSent = true;
            sendIntent(nri.mPendingIntent, intent);
        }
    }

    private void sendIntent(PendingIntent pendingIntent, Intent intent) {
        this.mPendingIntentWakeLock.acquire();
        try {
            log("Sending " + pendingIntent);
            pendingIntent.send(this.mContext, 0, intent, this, null);
        } catch (PendingIntent.CanceledException e) {
            log(pendingIntent + " was not sent, it had been canceled.");
            this.mPendingIntentWakeLock.release();
            releasePendingNetworkRequest(pendingIntent);
        }
    }

    @Override // android.app.PendingIntent.OnFinished
    public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
        log("Finished sending " + pendingIntent);
        this.mPendingIntentWakeLock.release();
        releasePendingNetworkRequestWithDelay(pendingIntent);
    }

    private void callCallbackForRequest(NetworkRequestInfo nri, NetworkAgentInfo networkAgent, int notificationType, int arg1) {
        if (nri.messenger != null) {
            Bundle bundle = new Bundle();
            putParcelable(bundle, new NetworkRequest(nri.clientRequest != null ? nri.clientRequest : nri.request));
            Message msg = Message.obtain();
            if (notificationType != 524293) {
                putParcelable(bundle, networkAgent.network);
            }
            switch (notificationType) {
                case 524290:
                    putParcelable(bundle, networkCapabilitiesRestrictedForCallerPermissions(networkAgent.networkCapabilities, nri.mPid, nri.mUid));
                    putParcelable(bundle, new LinkProperties(networkAgent.linkProperties));
                    msg.arg1 = arg1;
                    break;
                case 524291:
                    msg.arg1 = arg1;
                    break;
                case 524294:
                    putParcelable(bundle, networkCapabilitiesRestrictedForCallerPermissions(networkAgent.networkCapabilities, nri.mPid, nri.mUid));
                    break;
                case 524295:
                    putParcelable(bundle, new LinkProperties(networkAgent.linkProperties));
                    break;
                case 524299:
                    maybeLogBlockedStatusChanged(nri, networkAgent.network, arg1 != 0);
                    msg.arg1 = arg1;
                    break;
            }
            msg.what = notificationType;
            msg.setData(bundle);
            try {
                nri.messenger.send(msg);
            } catch (RemoteException e) {
                loge("RemoteException caught trying to send a callback msg for " + nri.request);
            }
        }
    }

    private static <T extends Parcelable> void putParcelable(Bundle bundle, T t) {
        bundle.putParcelable(t.getClass().getSimpleName(), t);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void teardownUnneededNetwork(NetworkAgentInfo nai) {
        if (nai.numRequestNetworkRequests() != 0) {
            int i = 0;
            while (true) {
                if (i >= nai.numNetworkRequests()) {
                    break;
                }
                NetworkRequest nr = nai.requestAt(i);
                if (!nr.isListen()) {
                    loge("Dead network still had at least " + nr);
                    break;
                }
                i++;
            }
        }
        nai.asyncChannel.disconnect();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLingerComplete(NetworkAgentInfo oldNetwork) {
        if (oldNetwork == null) {
            loge("Unknown NetworkAgentInfo in handleLingerComplete");
            return;
        }
        log("handleLingerComplete for " + oldNetwork.name());
        oldNetwork.clearLingerState();
        if (unneeded(oldNetwork, UnneededFor.TEARDOWN)) {
            teardownUnneededNetwork(oldNetwork);
        } else {
            updateCapabilities(oldNetwork.getCurrentScore(), oldNetwork, oldNetwork.networkCapabilities);
        }
    }

    /* access modifiers changed from: protected */
    public void makeDefault(NetworkAgentInfo newNetwork) {
        if (HW_DEBUGGABLE) {
            log("Switching to new default network: " + newNetwork);
        }
        try {
            this.mNMS.setDefaultNetId(newNetwork.network.netId);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception setting default network :");
            sb.append(HW_DEBUGGABLE ? e : "*");
            loge(sb.toString());
        }
        notifyLockdownVpn(newNetwork);
        handleApplyDefaultProxy(newNetwork.linkProperties.getHttpProxy());
        updateTcpBufferSizes(newNetwork.linkProperties.getTcpBufferSizes());
        this.mDnsManager.setDefaultDnsSystemProperties(newNetwork.linkProperties.getDnsServers());
        notifyIfacesChangedForNetworkStats();
        updateAllVpnsCapabilities();
        notifyMpLinkDefaultNetworkChange();
    }

    private void processListenRequests(NetworkAgentInfo nai, boolean capabilitiesChanged) {
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            NetworkRequest nr = nri.request;
            if (nr.isListen() && nai.isSatisfyingRequest(nr.requestId) && !nai.satisfies(nr)) {
                nai.removeRequest(nri.request.requestId);
                callCallbackForRequest(nri, nai, 524292, 0);
            }
        }
        if (capabilitiesChanged) {
            notifyNetworkCallbacks(nai, 524294);
        }
        for (NetworkRequestInfo nri2 : this.mNetworkRequests.values()) {
            NetworkRequest nr2 = nri2.request;
            if (nr2.isListen() && nai.satisfies(nr2) && !nai.isSatisfyingRequest(nr2.requestId)) {
                nai.addRequest(nr2);
                notifyNetworkAvailable(nai, nri2);
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:180:0x00c9 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:182:0x00c9 */
    /* JADX INFO: Multiple debug info for r15v16 'currentNetwork'  com.android.server.connectivity.NetworkAgentInfo: [D('currentNetwork' com.android.server.connectivity.NetworkAgentInfo), D('satisfies' boolean)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rematchNetworkAndRequests(NetworkAgentInfo newNetwork, ReapUnvalidatedNetworks reapUnvalidatedNetworks, long now) {
        boolean isUserDataEnabled;
        boolean z;
        int i;
        Iterator<NetworkAgentInfo> it;
        boolean satisfies;
        int score;
        boolean wasBackgroundNetwork;
        NetworkCapabilities nc;
        boolean keep;
        NetworkAgentInfo currentNetwork;
        NetworkAgentInfo oldDefaultNetwork;
        NetworkRequestInfo nri;
        if (newNetwork.everConnected) {
            boolean keep2 = newNetwork.isVPN();
            boolean wasBackgroundNetwork2 = newNetwork.isBackgroundNetwork();
            int score2 = newNetwork.getCurrentScore();
            log("rematching " + newNetwork.name());
            ArrayList<NetworkAgentInfo> affectedNetworks = new ArrayList<>();
            ArrayList<NetworkRequestInfo> addedRequests = new ArrayList<>();
            boolean isProvisioning = Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0;
            boolean isUserDataEnabled2 = Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 1) == 1;
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                isUserDataEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data0", 1) == 1;
            } else {
                isUserDataEnabled = isUserDataEnabled2;
            }
            boolean isDisabledMobileNetwork = newNetwork.networkInfo.getType() == 0 && !isUserDataEnabled && !isProvisioning && !HwFrameworkFactory.getHwInnerTelephonyManager().isVSimEnabled();
            NetworkCapabilities nc2 = newNetwork.networkCapabilities;
            if (HW_DEBUGGABLE) {
                log(" network has: " + nc2);
            }
            boolean isNewDefault = false;
            NetworkAgentInfo oldDefaultNetwork2 = null;
            for (NetworkRequestInfo nri2 : this.mNetworkRequests.values()) {
                if (!nri2.request.isListen()) {
                    NetworkAgentInfo currentNetwork2 = getNetworkForRequest(nri2.request.requestId);
                    boolean satisfies2 = newNetwork.satisfies(nri2.request);
                    if (this.mDefaultRequest.requestId != nri2.request.requestId || !isDisabledMobileNetwork) {
                        satisfies = satisfies2;
                    } else {
                        log("mobile net can't satisfy default network request when mobile data disabled");
                        satisfies = false;
                    }
                    if (newNetwork != currentNetwork2 || !satisfies) {
                        if (satisfies) {
                            keep = keep2;
                        } else if (checkNetworkSupportBip(newNetwork, nri2.request)) {
                            keep = keep2;
                        } else {
                            if (newNetwork.isSatisfyingRequest(nri2.request.requestId)) {
                                log("Network " + newNetwork.name() + " stopped satisfying request " + nri2.request.requestId);
                                newNetwork.removeRequest(nri2.request.requestId);
                                if (currentNetwork2 == newNetwork) {
                                    clearNetworkForRequest(nri2.request.requestId);
                                    sendUpdatedScoreToFactories(nri2.request, null);
                                    keep = keep2;
                                } else {
                                    String str = TAG;
                                    StringBuilder sb = new StringBuilder();
                                    keep = keep2;
                                    sb.append("BUG: Removing request ");
                                    sb.append(nri2.request.requestId);
                                    sb.append(" from ");
                                    sb.append(newNetwork.name());
                                    sb.append(" without updating mNetworkForRequestId or factories!");
                                    Slog.wtf(str, sb.toString());
                                }
                                callCallbackForRequest(nri2, newNetwork, 524292, 0);
                            } else {
                                keep = keep2;
                            }
                            wasBackgroundNetwork = wasBackgroundNetwork2;
                            score = score2;
                            keep2 = keep;
                            nc = nc2;
                            isNewDefault = isNewDefault;
                            nc2 = nc;
                            wasBackgroundNetwork2 = wasBackgroundNetwork;
                            score2 = score;
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("CS = ");
                        sb2.append(currentNetwork2 != null ? currentNetwork2.getCurrentScore() : 0);
                        sb2.append(", NS = ");
                        sb2.append(score2);
                        log(sb2.toString());
                        if (currentNetwork2 == null || currentNetwork2.getCurrentScore() < score2) {
                            log("rematch for " + newNetwork.name());
                            if (currentNetwork2 != null) {
                                if (HW_DEBUGGABLE) {
                                    log("accepting network in place of " + currentNetwork2.name());
                                }
                                currentNetwork2.removeRequest(nri2.request.requestId);
                                currentNetwork = currentNetwork2;
                                nri = nri2;
                                wasBackgroundNetwork = wasBackgroundNetwork2;
                                oldDefaultNetwork = oldDefaultNetwork2;
                                score = score2;
                                nc = nc2;
                                currentNetwork2.lingerRequest(nri2.request, now, (long) this.mLingerDelayMs);
                                affectedNetworks.add(currentNetwork);
                            } else {
                                nri = nri2;
                                wasBackgroundNetwork = wasBackgroundNetwork2;
                                score = score2;
                                currentNetwork = currentNetwork2;
                                oldDefaultNetwork = oldDefaultNetwork2;
                                nc = nc2;
                                if (HW_DEBUGGABLE) {
                                    log("accepting network in place of null");
                                }
                            }
                            newNetwork.unlingerRequest(nri.request);
                            setNetworkForRequest(nri.request.requestId, newNetwork);
                            if (!newNetwork.addRequest(nri.request)) {
                                Slog.wtf(TAG, "BUG: " + newNetwork.name() + " already has " + nri.request);
                            }
                            addedRequests.add(nri);
                            sendUpdatedScoreToFactories(nri.request, newNetwork);
                            if (isDefaultRequest(nri)) {
                                oldDefaultNetwork2 = currentNetwork;
                                if (currentNetwork != null) {
                                    this.mLingerMonitor.noteLingerDefaultNetwork(currentNetwork, newNetwork);
                                }
                                keep2 = true;
                                isNewDefault = true;
                            } else {
                                keep2 = true;
                                oldDefaultNetwork2 = oldDefaultNetwork;
                                isNewDefault = isNewDefault;
                            }
                            nc2 = nc;
                            wasBackgroundNetwork2 = wasBackgroundNetwork;
                            score2 = score;
                        }
                        wasBackgroundNetwork = wasBackgroundNetwork2;
                        score = score2;
                        keep2 = keep;
                        nc = nc2;
                        isNewDefault = isNewDefault;
                        nc2 = nc;
                        wasBackgroundNetwork2 = wasBackgroundNetwork;
                        score2 = score;
                    } else {
                        log(newNetwork.name() + " NC " + nri2.request.requestId);
                        keep2 = true;
                    }
                }
            }
            boolean keep3 = keep2;
            if (NetworkFactory.isDualCellDataEnable()) {
                log("isDualCellDataEnable is true so keep is true");
                keep3 = true;
            }
            if (isNewDefault) {
                if ((ENABLE_WIFI_LTE_CE && IS_SUPPORT_LINGER_DELAY) || this.mCust.mMobileDataAlwaysOnCust) {
                    updateDataActivityTracking(newNetwork, oldDefaultNetwork2);
                    updateDefaultNetworkRouting(oldDefaultNetwork2, newNetwork);
                }
                makeDefault(newNetwork);
                metricsLogger().defaultNetworkMetrics().logDefaultNetworkEvent(now, newNetwork, oldDefaultNetwork2);
                scheduleReleaseNetworkTransitionWakelock();
            }
            if (!newNetwork.networkCapabilities.equalRequestableCapabilities(nc2)) {
                z = true;
                Slog.wtf(TAG, String.format("BUG: %s changed requestable capabilities during rematch: %s -> %s", newNetwork.name(), nc2, newNetwork.networkCapabilities));
            } else {
                z = true;
            }
            if (newNetwork.getCurrentScore() != score2) {
                String str2 = TAG;
                Object[] objArr = new Object[3];
                objArr[0] = newNetwork.name();
                Integer valueOf = Integer.valueOf(score2);
                char c = z ? 1 : 0;
                char c2 = z ? 1 : 0;
                char c3 = z ? 1 : 0;
                objArr[c] = valueOf;
                objArr[2] = Integer.valueOf(newNetwork.getCurrentScore());
                Slog.wtf(str2, String.format("BUG: %s changed score during rematch: %d -> %d", objArr));
            }
            if (wasBackgroundNetwork2 != newNetwork.isBackgroundNetwork()) {
                updateCapabilities(score2, newNetwork, newNetwork.networkCapabilities);
                i = 0;
            } else {
                i = 0;
                processListenRequests(newNetwork, false);
            }
            Iterator<NetworkRequestInfo> it2 = addedRequests.iterator();
            while (it2.hasNext()) {
                notifyNetworkAvailable(newNetwork, it2.next());
            }
            Iterator<NetworkAgentInfo> it3 = affectedNetworks.iterator();
            while (it3.hasNext()) {
                NetworkAgentInfo nai = it3.next();
                if (IS_SUPPORT_LINGER_DELAY || newNetwork.networkInfo.getType() != z || nai.networkInfo.getType() != 0 || !ENABLE_WIFI_LTE_CE) {
                    updateLingerState(nai, now);
                } else {
                    nai.clearLingerState();
                    nai.unlinger();
                    log("unlinger mobile network");
                }
            }
            updateLingerState(newNetwork, now);
            if (isNewDefault) {
                if (oldDefaultNetwork2 != null) {
                    this.mLegacyTypeTracker.remove(oldDefaultNetwork2.networkInfo.getType(), oldDefaultNetwork2, z);
                }
                this.mDefaultInetConditionPublished = newNetwork.lastValidated ? 100 : i;
                this.mLegacyTypeTracker.add(newNetwork.networkInfo.getType(), newNetwork);
                notifyLockdownVpn(newNetwork);
            }
            if (keep3) {
                try {
                    IBatteryStats bs = BatteryStatsService.getService();
                    int type = newNetwork.networkInfo.getType();
                    bs.noteNetworkInterfaceType(newNetwork.linkProperties.getInterfaceName(), type);
                    for (LinkProperties stacked : newNetwork.linkProperties.getStackedLinks()) {
                        bs.noteNetworkInterfaceType(stacked.getInterfaceName(), type);
                    }
                } catch (RemoteException e) {
                }
                for (int i2 = 0; i2 < newNetwork.numNetworkRequests(); i2++) {
                    NetworkRequest nr = newNetwork.requestAt(i2);
                    if (nr.legacyType != -1 && nr.isRequest()) {
                        this.mLegacyTypeTracker.add(nr.legacyType, newNetwork);
                    }
                }
                if (newNetwork.isVPN()) {
                    this.mLegacyTypeTracker.add(17, newNetwork);
                }
            }
            if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                Iterator<NetworkAgentInfo> it4 = this.mNetworkAgentInfos.values().iterator();
                while (it4.hasNext()) {
                    NetworkAgentInfo nai2 = it4.next();
                    if (!unneeded(nai2, UnneededFor.TEARDOWN)) {
                        it = it4;
                    } else if (nai2.getLingerExpiry() > 0) {
                        updateLingerState(nai2, now);
                        it = it4;
                    } else {
                        log("Reaping " + nai2.name());
                        if (ENABLE_WIFI_LTE_CE) {
                            boolean isWifiConnected = false;
                            ConnectivityManager cm = ConnectivityManager.from(this.mContext);
                            if (cm != null) {
                                NetworkInfo networkInfo = cm.getNetworkInfo(1);
                                if (networkInfo != null) {
                                    isWifiConnected = networkInfo.isConnected();
                                }
                            }
                            StringBuilder sb3 = new StringBuilder();
                            it = it4;
                            sb3.append("isWifiConnected==");
                            sb3.append(isWifiConnected);
                            log(sb3.toString());
                            if (!isWifiConnected || nai2.networkInfo.getType() != 0) {
                                teardownUnneededNetwork(nai2);
                            }
                        } else {
                            it = it4;
                            teardownUnneededNetwork(nai2);
                        }
                    }
                    it4 = it;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void rematchAllNetworksAndRequests(NetworkAgentInfo changed, int oldScore) {
        ReapUnvalidatedNetworks reapUnvalidatedNetworks;
        long now = SystemClock.elapsedRealtime();
        if (changed == null || oldScore >= changed.getCurrentScore()) {
            NetworkAgentInfo[] nais = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[this.mNetworkAgentInfos.size()]);
            Arrays.sort(nais);
            for (NetworkAgentInfo nai : nais) {
                if (nai != nais[nais.length - 1]) {
                    reapUnvalidatedNetworks = ReapUnvalidatedNetworks.DONT_REAP;
                } else {
                    reapUnvalidatedNetworks = ReapUnvalidatedNetworks.REAP;
                }
                rematchNetworkAndRequests(nai, reapUnvalidatedNetworks, now);
            }
            return;
        }
        rematchNetworkAndRequests(changed, ReapUnvalidatedNetworks.REAP, now);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateInetCondition(NetworkAgentInfo nai) {
        if (nai.everValidated && isDefaultNetwork(nai)) {
            int newInetCondition = nai.lastValidated ? 100 : 0;
            if (newInetCondition != this.mDefaultInetConditionPublished) {
                this.mDefaultInetConditionPublished = newInetCondition;
                sendInetConditionBroadcast(nai.networkInfo);
            }
        }
    }

    private void notifyLockdownVpn(NetworkAgentInfo nai) {
        synchronized (this.mVpns) {
            if (this.mLockdownTracker != null) {
                if (nai == null || !nai.isVPN()) {
                    this.mLockdownTracker.onNetworkInfoChanged();
                } else {
                    this.mLockdownTracker.onVpnStateChanged(nai.networkInfo);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkInfo(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        int i;
        NetworkInfo.State state = newInfo.getState();
        int oldScore = networkAgent.getCurrentScore();
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        notifyLockdownVpn(networkAgent);
        StringBuilder sb = new StringBuilder();
        sb.append(networkAgent.name());
        sb.append(" EVENT_NETWORK_INFO_CHANGED, going from ");
        sb.append(oldInfo == null ? "null" : oldInfo.getState());
        sb.append(" to ");
        sb.append(state);
        log(sb.toString());
        if (!ENABLE_WIFI_LTE_CE) {
            enableDefaultTypeApnWhenWifiConnectionStateChanged(state, newInfo.getType());
        }
        enableDefaultTypeApnWhenBlueToothTetheringStateChanged(networkAgent, newInfo);
        if (!networkAgent.created && (state == NetworkInfo.State.CONNECTED || (state == NetworkInfo.State.CONNECTING && networkAgent.isVPN()))) {
            networkAgent.networkCapabilities.addCapability(19);
            if (createNativeNetwork(networkAgent)) {
                networkAgent.created = true;
            } else {
                return;
            }
        }
        if (!networkAgent.everConnected && state == NetworkInfo.State.CONNECTED) {
            networkAgent.everConnected = true;
            if (networkAgent.linkProperties == null) {
                String str = TAG;
                Slog.wtf(str, networkAgent.name() + " connected with null LinkProperties");
            }
            synchronized (networkAgent) {
                networkAgent.setNetworkCapabilities(networkAgent.networkCapabilities);
            }
            handlePerNetworkPrivateDnsConfig(networkAgent, this.mDnsManager.getPrivateDnsConfig());
            updateLinkProperties(networkAgent, new LinkProperties(networkAgent.linkProperties), null);
            if (networkAgent.networkMisc.acceptPartialConnectivity) {
                networkAgent.networkMonitor().setAcceptPartialConnectivity();
            }
            networkAgent.networkMonitor().notifyNetworkConnected(networkAgent.linkProperties, networkAgent.networkCapabilities);
            scheduleUnvalidatedPrompt(networkAgent);
            updateSignalStrengthThresholds(networkAgent, "CONNECT", null);
            if (networkAgent.isVPN()) {
                setVpnSettingValue(true);
                updateAllVpnsCapabilities();
            }
            rematchNetworkAndRequests(networkAgent, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
            notifyNetworkCallbacks(networkAgent, 524289);
        } else if (state == NetworkInfo.State.DISCONNECTED) {
            networkAgent.asyncChannel.disconnect();
            if (networkAgent.isVPN()) {
                setVpnSettingValue(false);
                updateUids(networkAgent, networkAgent.networkCapabilities, null);
            }
            disconnectAndDestroyNetwork(networkAgent);
            if (networkAgent.isVPN()) {
                this.mProxyTracker.sendProxyBroadcast();
            }
        } else if ((oldInfo != null && oldInfo.getState() == NetworkInfo.State.SUSPENDED) || state == NetworkInfo.State.SUSPENDED) {
            if (networkAgent.getCurrentScore() != oldScore) {
                rematchAllNetworksAndRequests(networkAgent, oldScore);
            }
            updateCapabilities(networkAgent.getCurrentScore(), networkAgent, networkAgent.networkCapabilities);
            if (state == NetworkInfo.State.SUSPENDED) {
                i = 524297;
            } else {
                i = 524298;
            }
            notifyNetworkCallbacks(networkAgent, i);
            this.mLegacyTypeTracker.update(networkAgent);
        }
        if (!ENABLE_WIFI_LTE_CE) {
            hintUserSwitchToMobileWhileWifiDisconnected(state, newInfo.getType());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkScore(NetworkAgentInfo nai, int score) {
        log("updateNetworkScore for " + nai.name() + " to " + score);
        if (score < 0) {
            loge("updateNetworkScore for " + nai.name() + " got a negative score (" + score + ").  Bumping score to min of 0");
            score = 0;
        }
        int oldScore = nai.getCurrentScore();
        nai.setCurrentScore(score);
        rematchAllNetworksAndRequests(nai, oldScore);
        sendUpdatedScoreToFactories(nai);
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkAvailable(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        this.mHandler.removeMessages(20, nri);
        if (nri.mPendingIntent != null) {
            sendPendingIntentForRequest(nri, nai, 524290);
            return;
        }
        callCallbackForRequest(nri, nai, 524290, isUidNetworkingWithVpnBlocked(nri.mUid, this.mUidRules.get(nri.mUid), nai.networkCapabilities.isMetered(), this.mRestrictBackground) ? 1 : 0);
    }

    private void maybeNotifyNetworkBlocked(NetworkAgentInfo nai, boolean oldMetered, boolean newMetered, boolean oldRestrictBackground, boolean newRestrictBackground) {
        boolean oldBlocked;
        boolean newBlocked;
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequestInfo nri = this.mNetworkRequests.get(nai.requestAt(i));
            int uidRules = this.mUidRules.get(nri.mUid);
            synchronized (this.mVpns) {
                oldBlocked = isUidNetworkingWithVpnBlocked(nri.mUid, uidRules, oldMetered, oldRestrictBackground);
                newBlocked = isUidNetworkingWithVpnBlocked(nri.mUid, uidRules, newMetered, newRestrictBackground);
            }
            if (oldBlocked != newBlocked) {
                callCallbackForRequest(nri, nai, 524299, encodeBool(newBlocked));
            }
        }
    }

    private void maybeNotifyNetworkBlockedForNewUidRules(int uid, int newRules) {
        boolean oldBlocked;
        boolean newBlocked;
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            boolean metered = nai.networkCapabilities.isMetered();
            synchronized (this.mVpns) {
                oldBlocked = isUidNetworkingWithVpnBlocked(uid, this.mUidRules.get(uid), metered, this.mRestrictBackground);
                newBlocked = isUidNetworkingWithVpnBlocked(uid, newRules, metered, this.mRestrictBackground);
            }
            if (oldBlocked != newBlocked) {
                int arg = encodeBool(newBlocked);
                for (int i = 0; i < nai.numNetworkRequests(); i++) {
                    NetworkRequestInfo nri = this.mNetworkRequests.get(nai.requestAt(i));
                    if (nri != null && nri.mUid == uid) {
                        callCallbackForRequest(nri, nai, 524299, arg);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void sendLegacyNetworkBroadcast(NetworkAgentInfo nai, NetworkInfo.DetailedState state, int type) {
        NetworkInfo info = new NetworkInfo(nai.networkInfo);
        info.setType(type);
        if (state != NetworkInfo.DetailedState.DISCONNECTED) {
            info.setDetailedState(state, null, info.getExtraInfo());
            sendConnectedBroadcast(info);
            return;
        }
        info.setDetailedState(state, info.getReason(), info.getExtraInfo());
        Intent intent = new Intent("android.net.conn.CONNECTIVITY_CHANGE");
        intent.putExtra("networkInfo", info);
        intent.putExtra("networkType", info.getType());
        if (info.isFailover()) {
            intent.putExtra("isFailover", true);
            nai.networkInfo.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra("extraInfo", info.getExtraInfo());
        }
        NetworkAgentInfo newDefaultAgent = null;
        if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
            newDefaultAgent = getDefaultNetwork();
            if (newDefaultAgent != null) {
                intent.putExtra("otherNetwork", newDefaultAgent.networkInfo);
            } else {
                intent.putExtra("noConnectivity", true);
            }
        }
        intent.putExtra("inetCondition", this.mDefaultInetConditionPublished);
        sendStickyBroadcast(intent);
        if (newDefaultAgent != null) {
            sendConnectedBroadcast(newDefaultAgent.networkInfo);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType, int arg1) {
        String notification = ConnectivityManager.getCallbackName(notifyType);
        log("notifyType " + notification + " for " + networkAgent.name());
        for (int i = 0; i < networkAgent.numNetworkRequests(); i++) {
            NetworkRequestInfo nri = this.mNetworkRequests.get(networkAgent.requestAt(i));
            if (nri != null) {
                if (nri.mPendingIntent == null) {
                    callCallbackForRequest(nri, networkAgent, notifyType, arg1);
                } else {
                    sendPendingIntentForRequest(nri, networkAgent, notifyType);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType) {
        notifyNetworkCallbacks(networkAgent, notifyType, 0);
    }

    private Network[] getDefaultNetworks() {
        ensureRunningOnConnectivityServiceThread();
        ArrayList<Network> defaultNetworks = new ArrayList<>();
        NetworkAgentInfo defaultNetwork = getDefaultNetwork();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.everConnected && (nai == defaultNetwork || nai.isVPN())) {
                defaultNetworks.add(nai.network);
            }
        }
        return (Network[]) defaultNetworks.toArray(new Network[0]);
    }

    private void notifyIfacesChangedForNetworkStats() {
        ensureRunningOnConnectivityServiceThread();
        String activeIface = null;
        LinkProperties activeLinkProperties = getActiveLinkProperties();
        if (activeLinkProperties != null) {
            activeIface = activeLinkProperties.getInterfaceName();
        }
        try {
            this.mStatsService.forceUpdateIfaces(getDefaultNetworks(), getAllVpnInfo(), getAllNetworkState(), activeIface);
        } catch (Exception e) {
        }
    }

    public boolean addVpnAddress(String address, int prefixLength) {
        boolean addAddress;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            addAddress = this.mVpns.get(user).addAddress(address, prefixLength);
        }
        return addAddress;
    }

    public boolean removeVpnAddress(String address, int prefixLength) {
        boolean removeAddress;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            removeAddress = this.mVpns.get(user).removeAddress(address, prefixLength);
        }
        return removeAddress;
    }

    public boolean setUnderlyingNetworksForVpn(Network[] networks) {
        boolean success;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            throwIfLockdownEnabled();
            success = this.mVpns.get(user).setUnderlyingNetworks(networks);
        }
        if (success) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.$$Lambda$ConnectivityService$tyyIxrN1UBdbonRFAT6eEH4wVic */

                @Override // java.lang.Runnable
                public final void run() {
                    ConnectivityService.this.lambda$setUnderlyingNetworksForVpn$5$ConnectivityService();
                }
            });
        }
        return success;
    }

    public /* synthetic */ void lambda$setUnderlyingNetworksForVpn$5$ConnectivityService() {
        updateAllVpnsCapabilities();
        notifyIfacesChangedForNetworkStats();
    }

    public String getCaptivePortalServerUrl() {
        enforceConnectivityInternalPermission();
        String settingUrl = this.mContext.getResources().getString(17039878);
        if (!TextUtils.isEmpty(settingUrl)) {
            return settingUrl;
        }
        String settingUrl2 = Settings.Global.getString(this.mContext.getContentResolver(), "captive_portal_http_url");
        if (!TextUtils.isEmpty(settingUrl2)) {
            return settingUrl2;
        }
        return DEFAULT_CAPTIVE_PORTAL_HTTP_URL;
    }

    public void startNattKeepalive(Network network, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, int srcPort, String dstAddr) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), (FileDescriptor) null, intervalSeconds, cb, srcAddr, srcPort, dstAddr, 4500);
    }

    public void startNattKeepaliveWithFd(Network network, FileDescriptor fd, int resourceId, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, String dstAddr) {
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), fd, resourceId, intervalSeconds, cb, srcAddr, dstAddr, 4500);
    }

    public void startTcpKeepalive(Network network, FileDescriptor fd, int intervalSeconds, ISocketKeepaliveCallback cb) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startTcpKeepalive(getNetworkAgentInfoForNetwork(network), fd, intervalSeconds, cb);
    }

    public void stopKeepalive(Network network, int slot) {
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(528396, slot, 0, network));
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int userId = UserHandle.getCallingUserId();
            Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable() {
                /* class com.android.server.$$Lambda$ConnectivityService$LEHWBvz4Sr8QDKRwIiJBgJlcRE */

                public final void runOrThrow() {
                    ConnectivityService.this.lambda$factoryReset$6$ConnectivityService();
                }
            });
            setAirplaneMode(false);
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                String pkgName = this.mContext.getOpPackageName();
                for (String tether : getTetheredIfaces()) {
                    untether(tether, pkgName);
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_config_vpn")) {
                synchronized (this.mVpns) {
                    String alwaysOnPackage = getAlwaysOnVpnPackage(userId);
                    if (alwaysOnPackage != null) {
                        setAlwaysOnVpnPackage(userId, null, false, null);
                        setVpnPackageAuthorization(alwaysOnPackage, userId, false);
                    }
                    if (this.mLockdownEnabled && userId == 0) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            this.mKeyStore.delete("LOCKDOWN_VPN");
                            this.mLockdownEnabled = false;
                            setLockdownTracker(null);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    VpnConfig vpnConfig = getVpnConfig(userId);
                    if (vpnConfig != null) {
                        if (vpnConfig.legacy) {
                            prepareVpn("[Legacy VPN]", "[Legacy VPN]", userId);
                        } else {
                            setVpnPackageAuthorization(vpnConfig.user, userId, false);
                            prepareVpn(null, "[Legacy VPN]", userId);
                        }
                    }
                }
            }
            if (!this.mUserManager.hasUserRestriction("disallow_config_private_dns")) {
                Settings.Global.putString(this.mContext.getContentResolver(), "private_dns_mode", "opportunistic");
            }
            Settings.Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", null);
            Settings.Global.putString(this.mContext.getContentResolver(), "private_dns_mode", null);
            Settings.Global.putString(this.mContext.getContentResolver(), "private_dns_specifier", null);
        }
    }

    public /* synthetic */ void lambda$factoryReset$6$ConnectivityService() throws Exception {
        IpMemoryStore.getMemoryStore(this.mContext).factoryReset();
    }

    public byte[] getNetworkWatchlistConfigHash() {
        NetworkWatchlistManager nwm = (NetworkWatchlistManager) this.mContext.getSystemService(NetworkWatchlistManager.class);
        if (nwm != null) {
            return nwm.getWatchlistConfigHash();
        }
        loge("Unable to get NetworkWatchlistManager");
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public MultinetworkPolicyTracker createMultinetworkPolicyTracker(Context c, Handler h, Runnable r) {
        return new MultinetworkPolicyTracker(c, h, r);
    }

    @VisibleForTesting
    public WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int cmd, Object obj) {
        return new WakeupMessage(c, h, s, cmd, 0, 0, obj);
    }

    @VisibleForTesting
    public boolean hasService(String name) {
        return ServiceManager.checkService(name) != null;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public IpConnectivityMetrics.Logger metricsLogger() {
        return (IpConnectivityMetrics.Logger) Preconditions.checkNotNull((IpConnectivityMetrics.Logger) LocalServices.getService(IpConnectivityMetrics.Logger.class), "no IpConnectivityMetrics service");
    }

    private void logNetworkEvent(NetworkAgentInfo nai, int evtype) {
        this.mMetricsLog.log(nai.network.netId, nai.networkCapabilities.getTransportTypes(), new NetworkEvent(evtype));
    }

    /* access modifiers changed from: private */
    public static boolean toBool(int encodedBoolean) {
        return encodedBoolean != 0;
    }

    /* access modifiers changed from: private */
    public static int encodeBool(boolean b) {
        return b ? 1 : 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.ConnectivityService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
    }

    private class ShellCmd extends ShellCommand {
        private ShellCmd() {
        }

        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                if ((cmd.hashCode() == 144736062 && cmd.equals("airplane-mode")) ? false : true) {
                    return handleDefaultCommands(cmd);
                }
                String action = getNextArg();
                if ("enable".equals(action)) {
                    ConnectivityService.this.setAirplaneMode(true);
                    return 0;
                } else if ("disable".equals(action)) {
                    ConnectivityService.this.setAirplaneMode(false);
                    return 0;
                } else if (action == null) {
                    pw.println(Settings.Global.getInt(ConnectivityService.this.mContext.getContentResolver(), "airplane_mode_on") == 0 ? "disabled" : "enabled");
                    return 0;
                } else {
                    onHelp();
                    return -1;
                }
            } catch (Exception e) {
                pw.println(e);
                return -1;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Connectivity service commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  airplane-mode [enable|disable]");
            pw.println("    Turn airplane mode on or off.");
            pw.println("  airplane-mode");
            pw.println("    Get airplane mode.");
        }
    }

    @GuardedBy({"mVpns"})
    private Vpn getVpnIfOwner() {
        VpnInfo info;
        int uid = Binder.getCallingUid();
        Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
        if (vpn == null || (info = vpn.getVpnInfo()) == null || info.ownerUid != uid) {
            return null;
        }
        return vpn;
    }

    private Vpn enforceActiveVpnOrNetworkStackPermission() {
        if (checkNetworkStackPermission()) {
            return null;
        }
        synchronized (this.mVpns) {
            Vpn vpn = getVpnIfOwner();
            if (vpn != null) {
                return vpn;
            }
            throw new SecurityException("App must either be an active VPN or have the NETWORK_STACK permission");
        }
    }

    public int getConnectionOwnerUid(ConnectionInfo connectionInfo) {
        Vpn vpn = enforceActiveVpnOrNetworkStackPermission();
        if (connectionInfo.protocol == OsConstants.IPPROTO_TCP || connectionInfo.protocol == OsConstants.IPPROTO_UDP) {
            try {
                int uid = InetDiagMessage.getConnectionOwnerUid(connectionInfo.protocol, connectionInfo.local, connectionInfo.remote);
                if (vpn == null || vpn.appliesToUid(uid)) {
                    return uid;
                }
                return -1;
            } catch (ErrnoException e) {
                return -1;
            }
        } else {
            throw new IllegalArgumentException("Unsupported protocol " + connectionInfo.protocol);
        }
    }

    public boolean isCallerCurrentAlwaysOnVpnApp() {
        boolean z;
        synchronized (this.mVpns) {
            Vpn vpn = getVpnIfOwner();
            z = vpn != null && vpn.getAlwaysOn();
        }
        return z;
    }

    public boolean isCallerCurrentAlwaysOnVpnLockdownApp() {
        boolean z;
        synchronized (this.mVpns) {
            Vpn vpn = getVpnIfOwner();
            z = vpn != null && vpn.getLockdown();
        }
        return z;
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [com.android.server.TestNetworkService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder startOrGetTestNetworkService() {
        ?? r1;
        synchronized (this.mTNSLock) {
            TestNetworkService.enforceTestNetworkPermissions(this.mContext);
            if (this.mTNS == null) {
                this.mTNS = new TestNetworkService(this.mContext, this.mNMS);
            }
            r1 = this.mTNS;
        }
        return r1;
    }

    /* access modifiers changed from: protected */
    public void notifyMpLinkDefaultNetworkChange() {
    }

    /* access modifiers changed from: protected */
    public boolean isAppBindedNetwork() {
        return false;
    }

    /* access modifiers changed from: protected */
    public NetworkInfo getActiveNetworkForMpLink(NetworkInfo info, int uid) {
        return info;
    }

    /* access modifiers changed from: protected */
    public DnsManager getDnsManager() {
        return this.mDnsManager;
    }

    public SparseArray<Vpn> getmVpns() {
        SparseArray<Vpn> sparseArray;
        synchronized (this.mVpns) {
            sparseArray = this.mVpns;
        }
        return sparseArray;
    }

    @Override // com.android.server.connectivity.IHwConnectivityServiceInner
    public ConnectivityService getService() {
        return this;
    }

    @Override // com.android.server.connectivity.IHwConnectivityServiceInner
    public HashMap<Messenger, NetworkAgentInfo> getNetworkAgentInfos() {
        return this.mNetworkAgentInfos;
    }

    @Override // com.android.server.connectivity.IHwConnectivityServiceInner
    public NetworkAgentInfo getHwNetworkForType(int type) {
        return this.mLegacyTypeTracker.getNetworkForType(type);
    }

    @Override // com.android.server.connectivity.IHwConnectivityServiceInner
    public void hwUpdateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        updateLinkProperties(networkAgent, new LinkProperties(networkAgent.linkProperties), oldLp);
    }

    @Override // com.android.server.connectivity.IHwConnectivityServiceInner
    public INetworkManagementService getmNMSHw() {
        return this.mNMS;
    }

    /* access modifiers changed from: protected */
    public LocalLog obtainNetworkRequestInfoLogsHw() {
        return this.mNetworkRequestInfoLogs;
    }

    /* access modifiers changed from: protected */
    public NetworkAgentInfo getNetworkAgentInfoForNetIdHw(int netId) {
        return getNetworkAgentInfoForNetId(netId);
    }

    /* access modifiers changed from: protected */
    public void enforceAccessPermissionHw() {
        enforceAccessPermission();
    }

    /* access modifiers changed from: protected */
    public void enforceChangePermissionHw() {
        enforceChangePermission();
    }

    /* access modifiers changed from: protected */
    public void sendStickyBroadcastHw(Intent intent) {
        sendStickyBroadcast(intent);
    }

    /* access modifiers changed from: protected */
    public ConcurrentHashMap<NetworkRequest, NetworkRequestInfo> obtainNetworkRequestsMapHw() {
        return this.mNetworkRequests;
    }

    /* access modifiers changed from: protected */
    public HashMap<Messenger, NetworkFactoryInfo> obtainNetworkFactoryInfosMapHw() {
        return this.mNetworkFactoryInfos;
    }

    /* access modifiers changed from: protected */
    public SparseArray<NetworkAgentInfo> obtainNetworkForRequestIdArrayHw() {
        return this.mNetworkForRequestId;
    }

    /* access modifiers changed from: protected */
    public void sendUpdatedScoreToFactoriesHw(NetworkRequest networkRequest, NetworkAgentInfo nai) {
        sendUpdatedScoreToFactories(networkRequest, nai);
    }

    /* access modifiers changed from: protected */
    public void rematchAllNetworksAndRequestsHw(NetworkAgentInfo changed, int oldScore) {
        rematchAllNetworksAndRequests(changed, oldScore);
    }
}
