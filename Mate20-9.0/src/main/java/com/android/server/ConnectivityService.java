package com.android.server;

import android.app.BroadcastOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.IIpConnectivityMetrics;
import android.net.INetdEventCallback;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.MatchAllNetworkSpecifier;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.NetworkWatchlistManager;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.util.MultinetworkPolicyTracker;
import android.net.wifi.HiSiWifiComm;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
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
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.WakeupMessage;
import com.android.internal.util.XmlUtils;
import com.android.server.AbstractConnectivityService;
import com.android.server.am.BatteryStatsService;
import com.android.server.audio.AudioService;
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
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.NetworkNotificationManager;
import com.android.server.connectivity.PacManager;
import com.android.server.connectivity.PermissionMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.Vpn;
import com.android.server.connectivity.tethering.TetheringDependencies;
import com.android.server.net.BaseNetdEventCallback;
import com.android.server.net.BaseNetworkObserver;
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConnectivityService extends AbstractConnectivityService implements IHwConnectivityServiceInner, PendingIntent.OnFinished {
    private static final String ACTION_PKT_CNT_SAMPLE_INTERVAL_ELAPSED = "android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED";
    private static final String ATTR_MCC = "mcc";
    private static final String ATTR_MNC = "mnc";
    private static final int CODE_REMOVE_LEGACYROUTE_TO_HOST = 1015;
    private static final boolean DBG = true;
    private static final int DEFAULT_LINGER_DELAY_MS = 30000;
    private static final String DEFAULT_TCP_BUFFER_SIZES = "4096,87380,110208,4096,16384,110208";
    private static final String DEFAULT_TCP_RWND_KEY = "net.tcp.default_init_rwnd";
    private static final String DESCRIPTOR = "android.net.wifi.INetworkManager";
    public static final String DIAG_ARG = "--diag";
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;
    private static final boolean ENABLE_WIFI_LTE_CE = SystemProperties.getBoolean("ro.config.enable_wl_coexist", false);
    private static final int EVENT_APPLY_GLOBAL_HTTP_PROXY = 9;
    private static final int EVENT_CHANGE_MOBILE_DATA_ENABLED = 2;
    private static final int EVENT_CLEAR_NET_TRANSITION_WAKELOCK = 8;
    private static final int EVENT_CONFIGURE_MOBILE_DATA_ALWAYS_ON = 30;
    private static final int EVENT_EXPIRE_NET_TRANSITION_WAKELOCK = 24;
    private static final int EVENT_PRIVATE_DNS_SETTINGS_CHANGED = 37;
    private static final int EVENT_PRIVATE_DNS_VALIDATION_UPDATE = 38;
    private static final int EVENT_PROMPT_UNVALIDATED = 29;
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
    private static final int EVENT_SET_ACCEPT_UNVALIDATED = 28;
    private static final int EVENT_SET_AVOID_UNVALIDATED = 35;
    private static final int EVENT_SYSTEM_READY = 25;
    private static final int EVENT_TIMEOUT_NETWORK_REQUEST = 20;
    private static final int EVENT_UNREGISTER_NETWORK_FACTORY = 23;
    private static final boolean HW_DEBUGGABLE = Build.IS_DEBUGGABLE;
    protected static final boolean IS_SUPPORT_LINGER_DELAY;
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final boolean LOGD_BLOCKED_NETWORKINFO = true;
    private static final boolean LOGD_RULES = false;
    private static final int MAX_NETWORK_INFO_LOGS = 40;
    private static final int MAX_NETWORK_REQUESTS_PER_UID = 100;
    private static final int MAX_NETWORK_REQUEST_LOGS = 20;
    private static final int MAX_NET_ID = 64511;
    private static final int MAX_VALIDATION_LOGS = 10;
    private static final int MAX_WAKELOCK_LOGS = 20;
    private static final int MIN_NET_ID = 100;
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    private static final int PROMPT_UNVALIDATED_DELAY_MS = 8000;
    private static final String PROVISIONING_URL_PATH = "/data/misc/radio/provisioning_urls.xml";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 60000;
    public static final String SHORT_ARG = "--short";
    /* access modifiers changed from: private */
    public static final String TAG = ConnectivityService.class.getSimpleName();
    private static final String TAG_PROVISIONING_URL = "provisioningUrl";
    private static final String TAG_PROVISIONING_URLS = "provisioningUrls";
    public static final String TETHERING_ARG = "tethering";
    private static final boolean VDBG = true;
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(new Class[]{AsyncChannel.class, ConnectivityService.class, NetworkAgent.class, NetworkAgentInfo.class});
    private static ConnectivityService sServiceInstance;
    private RouteInfo mBestLegacyRoute;
    @GuardedBy("mBlockedAppUids")
    private final HashSet<Integer> mBlockedAppUids;
    /* access modifiers changed from: private */
    public final Context mContext;
    private String mCurrentTcpBufferSizes;
    protected HwCustConnectivityService mCust;
    private INetworkManagementEventObserver mDataActivityObserver;
    private DataConnectionStats mDataConnectionStats;
    private int mDefaultInetConditionPublished;
    private final NetworkRequest mDefaultMobileDataRequest;
    private volatile ProxyInfo mDefaultProxy;
    private boolean mDefaultProxyDisabled;
    private final NetworkRequest mDefaultRequest;
    private final DnsManager mDnsManager;
    private ProxyInfo mGlobalProxy;
    /* access modifiers changed from: private */
    public final InternalHandler mHandler;
    @VisibleForTesting
    protected final HandlerThread mHandlerThread;
    IHwConnectivityServiceEx mHwCSEx;
    private Intent mInitialBroadcast;
    private IIpConnectivityMetrics mIpConnectivityMetrics;
    /* access modifiers changed from: private */
    public KeepaliveTracker mKeepaliveTracker;
    private KeyStore mKeyStore;
    private long mLastWakeLockAcquireTimestamp;
    private int mLegacyRouteNetId;
    private int mLegacyRouteUid;
    private LegacyTypeTracker mLegacyTypeTracker;
    @VisibleForTesting
    protected int mLingerDelayMs;
    private LingerMonitor mLingerMonitor;
    @GuardedBy("mVpns")
    private boolean mLockdownEnabled;
    @GuardedBy("mVpns")
    private LockdownVpnTracker mLockdownTracker;
    private long mMaxWakelockDurationMs;
    private final IpConnectivityLog mMetricsLog;
    @VisibleForTesting
    final MultinetworkPolicyTracker mMultinetworkPolicyTracker;
    @VisibleForTesting
    final MultipathPolicyTracker mMultipathPolicyTracker;
    NetworkConfig[] mNetConfigs;
    @GuardedBy("mNetworkForNetId")
    private final SparseBooleanArray mNetIdInUse;
    private PowerManager.WakeLock mNetTransitionWakeLock;
    private int mNetTransitionWakeLockTimeout;
    protected INetworkManagementService mNetd;
    @VisibleForTesting
    protected final INetdEventCallback mNetdEventCallback;
    /* access modifiers changed from: private */
    public final HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos;
    protected final HashMap<Messenger, NetworkFactoryInfo> mNetworkFactoryInfos;
    @GuardedBy("mNetworkForNetId")
    private final SparseArray<NetworkAgentInfo> mNetworkForNetId;
    @GuardedBy("mNetworkForRequestId")
    protected final SparseArray<NetworkAgentInfo> mNetworkForRequestId;
    private final LocalLog mNetworkInfoBlockingLogs;
    private int mNetworkPreference;
    protected final LocalLog mNetworkRequestInfoLogs;
    protected final HashMap<NetworkRequest, NetworkRequestInfo> mNetworkRequests;
    int mNetworksDefined;
    private int mNextNetId;
    private int mNextNetworkRequestId;
    /* access modifiers changed from: private */
    public NetworkNotificationManager mNotifier;
    /* access modifiers changed from: private */
    public NetworkInfo mP2pNetworkInfo;
    private PacManager mPacManager;
    private final PowerManager.WakeLock mPendingIntentWakeLock;
    private final PermissionMonitor mPermissionMonitor;
    private final INetworkPolicyListener mPolicyListener;
    private INetworkPolicyManager mPolicyManager;
    private NetworkPolicyManagerInternal mPolicyManagerInternal;
    private final PriorityDump.PriorityDumper mPriorityDumper;
    List mProtectedNetworks;
    private final File mProvisioningUrlFile;
    private Object mProxyLock;
    private final int mReleasePendingIntentDelayMs;
    private final SettingsObserver mSettingsObserver;
    private INetworkStatsService mStatsService;
    private MockableSystemProperties mSystemProperties;
    private boolean mSystemReady;
    TelephonyManager mTelephonyManager;
    private boolean mTestMode;
    /* access modifiers changed from: private */
    public Tethering mTethering;
    private int mTotalWakelockAcquisitions;
    private long mTotalWakelockDurationMs;
    private int mTotalWakelockReleases;
    private final NetworkStateTrackerHandler mTrackerHandler;
    /* access modifiers changed from: private */
    @GuardedBy("mUidToNetworkRequestCount")
    public final SparseIntArray mUidToNetworkRequestCount;
    private BroadcastReceiver mUserIntentReceiver;
    private UserManager mUserManager;
    private BroadcastReceiver mUserPresentReceiver;
    private final ArrayDeque<ValidationLog> mValidationLogs;
    @GuardedBy("mVpns")
    @VisibleForTesting
    protected final SparseArray<Vpn> mVpns;
    private final LocalLog mWakelockLogs;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            switch (i) {
                case 8:
                    break;
                case 9:
                    ConnectivityService.this.handleDeprecatedGlobalHttpProxy();
                    return;
                default:
                    switch (i) {
                        case 16:
                            ConnectivityService.this.handleApplyDefaultProxy((ProxyInfo) msg.obj);
                            return;
                        case 17:
                            ConnectivityService.this.handleRegisterNetworkFactory((NetworkFactoryInfo) msg.obj);
                            return;
                        case 18:
                            ConnectivityService.this.handleRegisterNetworkAgent((NetworkAgentInfo) msg.obj);
                            return;
                        case 19:
                        case 21:
                            ConnectivityService.this.handleRegisterNetworkRequest((NetworkRequestInfo) msg.obj);
                            return;
                        case 20:
                            ConnectivityService.this.handleTimedOutNetworkRequest((NetworkRequestInfo) msg.obj);
                            return;
                        case 22:
                            ConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.arg1);
                            return;
                        case 23:
                            ConnectivityService.this.handleUnregisterNetworkFactory((Messenger) msg.obj);
                            return;
                        case 24:
                            break;
                        case 25:
                            for (NetworkAgentInfo nai : ConnectivityService.this.mNetworkAgentInfos.values()) {
                                nai.networkMonitor.systemReady = true;
                            }
                            ConnectivityService.this.mMultipathPolicyTracker.start();
                            return;
                        case 26:
                        case 31:
                            ConnectivityService.this.handleRegisterNetworkRequestWithIntent(msg);
                            return;
                        case ConnectivityService.EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT /*27*/:
                            ConnectivityService.this.handleReleaseNetworkRequestWithIntent((PendingIntent) msg.obj, msg.arg1);
                            return;
                        case 28:
                            ConnectivityService.this.handleSetAcceptUnvalidated((Network) msg.obj, ConnectivityService.toBool(msg.arg1), ConnectivityService.toBool(msg.arg2));
                            return;
                        case 29:
                            ConnectivityService.this.handlePromptUnvalidated((Network) msg.obj);
                            return;
                        case 30:
                            ConnectivityService.this.handleMobileDataAlwaysOn();
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
            }
            ConnectivityService.this.handleReleaseNetworkTransitionWakelock(msg.what);
        }
    }

    private class LegacyTypeTracker {
        private static final boolean DBG = true;
        private static final boolean VDBG = false;
        private final ArrayList<NetworkAgentInfo>[] mTypeLists = new ArrayList[48];

        public LegacyTypeTracker() {
        }

        public void addSupportedType(int type) {
            if (this.mTypeLists[type] == null) {
                this.mTypeLists[type] = new ArrayList<>();
                return;
            }
            throw new IllegalStateException("legacy list for type " + type + "already initialized");
        }

        public boolean isTypeSupported(int type) {
            return ConnectivityManager.isNetworkTypeValid(type) && this.mTypeLists[type] != null;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
            return null;
         */
        public NetworkAgentInfo getNetworkForType(int type) {
            synchronized (this.mTypeLists) {
                if (isTypeSupported(type) && !this.mTypeLists[type].isEmpty()) {
                    NetworkAgentInfo networkAgentInfo = this.mTypeLists[type].get(0);
                    return networkAgentInfo;
                }
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
                    boolean isDefaultNetwork = ConnectivityService.this.isDefaultNetwork(nai);
                    if (list.size() == 1 || isDefaultNetwork) {
                        maybeLogBroadcast(nai, NetworkInfo.DetailedState.CONNECTED, type, isDefaultNetwork);
                        ConnectivityService.this.sendLegacyNetworkBroadcast(nai, NetworkInfo.DetailedState.CONNECTED, type);
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
            r3 = android.net.NetworkInfo.DetailedState.DISCONNECTED;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
            if (r2 != false) goto L_0x002a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
            if (r9 == false) goto L_0x0032;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
            maybeLogBroadcast(r8, r3, r7, r9);
            com.android.server.ConnectivityService.access$200(r6.this$0, r8, r3, r7);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
            if (r0.isEmpty() != false) goto L_0x0067;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0038, code lost:
            if (r2 == false) goto L_0x0067;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
            com.android.server.ConnectivityService.access$000("Other network available for type " + r7 + ", sending connected broadcast");
            r1 = r0.get(0);
            maybeLogBroadcast(r1, r3, r7, com.android.server.ConnectivityService.access$100(r6.this$0, r1));
            com.android.server.ConnectivityService.access$200(r6.this$0, r1, r3, r7);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0067, code lost:
            return;
         */
        public void remove(int type, NetworkAgentInfo nai, boolean wasDefault) {
            ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
            if (list != null && !list.isEmpty()) {
                boolean wasFirstNetwork = list.get(0).equals(nai);
                synchronized (this.mTypeLists) {
                    if (!list.remove(nai)) {
                    }
                }
            }
        }

        public void remove(NetworkAgentInfo nai, boolean wasDefault) {
            for (int type = 0; type < this.mTypeLists.length; type++) {
                remove(type, nai, wasDefault);
            }
        }

        public void update(NetworkAgentInfo nai) {
            boolean isDefault = ConnectivityService.this.isDefaultNetwork(nai);
            NetworkInfo.DetailedState state = nai.networkInfo.getDetailedState();
            for (int type = 0; type < this.mTypeLists.length; type++) {
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                boolean isFirst = true;
                boolean contains = list != null && list.contains(nai);
                if (!contains || nai != list.get(0)) {
                    isFirst = false;
                }
                if (isFirst || (contains && isDefault)) {
                    maybeLogBroadcast(nai, state, type, isDefault);
                    ConnectivityService.this.sendLegacyNetworkBroadcast(nai, state, type);
                }
            }
        }

        private String naiToString(NetworkAgentInfo nai) {
            String state;
            String name = nai != null ? nai.name() : "null";
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
            for (ArrayList<NetworkAgentInfo> arrayList : this.mTypeLists) {
                if (arrayList != null) {
                    pw.print(" " + type);
                }
            }
            pw.println();
            pw.println("Current state:");
            pw.increaseIndent();
            synchronized (this.mTypeLists) {
                for (int type = 0; type < this.mTypeLists.length; type++) {
                    if (this.mTypeLists[type] != null) {
                        if (!this.mTypeLists[type].isEmpty()) {
                            Iterator<NetworkAgentInfo> it = this.mTypeLists[type].iterator();
                            while (it.hasNext()) {
                                pw.println(type + " " + naiToString(it.next()));
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

    protected static class NetworkFactoryInfo {
        public final AsyncChannel asyncChannel;
        public final Messenger messenger;
        public final String name;

        public NetworkFactoryInfo(String name2, Messenger messenger2, AsyncChannel asyncChannel2) {
            this.name = name2;
            this.messenger = messenger2;
            this.asyncChannel = asyncChannel2;
        }
    }

    protected class NetworkRequestInfo implements IBinder.DeathRecipient {
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
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
            }
        }

        public void binderDied() {
            ConnectivityService.log("ConnectivityService NetworkRequestInfo binderDied(" + this.request + ", " + this.mBinder + ")");
            ConnectivityService.this.releaseNetworkRequest(this.request);
        }

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
                str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            } else {
                str = " to trigger " + this.mPendingIntent;
            }
            sb.append(str);
            return sb.toString();
        }
    }

    private class NetworkStateTrackerHandler extends Handler {
        public NetworkStateTrackerHandler(Looper looper) {
            super(looper);
        }

        private boolean maybeHandleAsyncChannelMessage(Message msg) {
            int i = msg.what;
            if (i != 69632) {
                switch (i) {
                    case 69635:
                        NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                        if (nai != null) {
                            nai.asyncChannel.disconnect();
                            break;
                        }
                        break;
                    case 69636:
                        ConnectivityService.this.handleAsyncChannelDisconnected(msg);
                        break;
                    default:
                        return false;
                }
            } else {
                ConnectivityService.this.handleAsyncChannelHalfConnect(msg);
            }
            return true;
        }

        private void maybeHandleNetworkAgentMessage(Message msg) {
            NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
            if (nai == null) {
                ConnectivityService.log(String.format("%s from unknown NetworkAgent", new Object[]{ConnectivityService.eventName(msg.what)}));
                return;
            }
            int i = msg.what;
            if (i == 528392) {
                if (nai.everConnected && !nai.networkMisc.explicitlySelected) {
                    ConnectivityService.loge("ERROR: already-connected network explicitly selected.");
                }
                nai.networkMisc.explicitlySelected = true;
                nai.networkMisc.acceptUnvalidated = ((Boolean) msg.obj).booleanValue();
            } else if (i != 528397) {
                switch (i) {
                    case 528385:
                        ConnectivityService.this.updateNetworkInfo(nai, (NetworkInfo) msg.obj);
                        break;
                    case 528386:
                        NetworkCapabilities networkCapabilities = (NetworkCapabilities) msg.obj;
                        if (networkCapabilities.hasCapability(17) || networkCapabilities.hasCapability(16) || networkCapabilities.hasCapability(19)) {
                            String access$1400 = ConnectivityService.TAG;
                            Slog.wtf(access$1400, "BUG: " + nai + " has CS-managed capability.");
                        }
                        ConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, networkCapabilities);
                        break;
                    case 528387:
                        ConnectivityService.this.handleUpdateLinkProperties(nai, (LinkProperties) msg.obj);
                        break;
                    case 528388:
                        Integer score = (Integer) msg.obj;
                        if (score != null) {
                            ConnectivityService.this.updateNetworkScore(nai, score.intValue());
                            break;
                        }
                        break;
                    default:
                        ConnectivityService.this.mHwCSEx.maybeHandleNetworkAgentMessageEx(msg, nai);
                        break;
                }
            } else {
                ConnectivityService.this.mKeepaliveTracker.handleEventPacketKeepalive(nai, msg);
            }
        }

        private boolean maybeHandleNetworkMonitorMessage(Message msg) {
            String logMsg;
            int i = msg.what;
            int i2 = 2;
            if (i == 532482) {
                NetworkAgentInfo nai = ConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg2);
                if (nai != null) {
                    boolean valid = msg.arg1 == 0 || msg.arg1 == 2;
                    boolean wasValidated = nai.lastValidated;
                    boolean wasDefault = ConnectivityService.this.isDefaultNetwork(nai);
                    String redirectUrl = msg.obj instanceof String ? (String) msg.obj : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    if (!TextUtils.isEmpty(redirectUrl)) {
                        logMsg = " with redirect to " + redirectUrl;
                    } else {
                        logMsg = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(nai.name());
                    sb.append(" validation ");
                    sb.append(valid ? "passed" : "failed");
                    sb.append(logMsg);
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
                    }
                    ConnectivityService.this.updateInetCondition(nai);
                    if (!ConnectivityService.this.reportPortalNetwork(nai, msg.arg1)) {
                        Bundle redirectUrlBundle = new Bundle();
                        redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, redirectUrl);
                        AsyncChannel asyncChannel = nai.asyncChannel;
                        if (valid) {
                            i2 = 1;
                        }
                        asyncChannel.sendMessage(528391, i2, 0, redirectUrlBundle);
                        if (wasValidated && !nai.lastValidated) {
                            ConnectivityService.this.handleNetworkUnvalidated(nai);
                        }
                    }
                }
            } else if (i == 532490) {
                int netId = msg.arg2;
                boolean visible = ConnectivityService.toBool(msg.arg1);
                NetworkAgentInfo nai2 = ConnectivityService.this.getNetworkAgentInfoForNetId(netId);
                if (!(nai2 == null || visible == nai2.lastCaptivePortalDetected)) {
                    int oldScore2 = nai2.getCurrentScore();
                    nai2.lastCaptivePortalDetected = visible;
                    nai2.everCaptivePortalDetected |= visible;
                    if (!nai2.lastCaptivePortalDetected || 2 != getCaptivePortalMode()) {
                        ConnectivityService.this.updateCapabilities(oldScore2, nai2, nai2.networkCapabilities);
                    } else {
                        ConnectivityService.log("Avoiding captive portal network: " + nai2.name());
                        nai2.asyncChannel.sendMessage(528399);
                        ConnectivityService.this.teardownUnneededNetwork(nai2);
                    }
                }
                if (!visible) {
                    ConnectivityService.this.mNotifier.clearNotification(netId);
                } else if (nai2 == null) {
                    ConnectivityService.loge("EVENT_PROVISIONING_NOTIFICATION from unknown NetworkMonitor");
                } else if (!nai2.networkMisc.provisioningNotificationDisabled) {
                    ConnectivityService.this.mNotifier.showNotification(netId, NetworkNotificationManager.NotificationType.SIGN_IN, nai2, null, (PendingIntent) msg.obj, nai2.networkMisc.explicitlySelected);
                }
            } else if (i != 532494) {
                return false;
            } else {
                NetworkAgentInfo nai3 = ConnectivityService.this.getNetworkAgentInfoForNetId(msg.arg2);
                if (nai3 != null) {
                    ConnectivityService.this.updatePrivateDns(nai3, (DnsManager.PrivateDnsConfig) msg.obj);
                }
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
                if (nai != null && ConnectivityService.this.isLiveNetworkAgent(nai, msg.what)) {
                    ConnectivityService.this.handleLingerComplete(nai);
                }
            } else if (i != 528485) {
                return false;
            } else {
                NetworkAgentInfo nai2 = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                if (nai2 == null) {
                    ConnectivityService.loge("EVENT_REMATCH_NETWORK_AND_REQUESTS from unknown NetworkAgent");
                } else {
                    ConnectivityService.this.rematchNetworkAndRequests(nai2, ReapUnvalidatedNetworks.DONT_REAP, SystemClock.elapsedRealtime());
                }
            }
            return true;
        }

        public void handleMessage(Message msg) {
            if (!maybeHandleAsyncChannelMessage(msg) && !maybeHandleNetworkMonitorMessage(msg) && !maybeHandleNetworkAgentInfoMessage(msg)) {
                maybeHandleNetworkAgentMessage(msg);
            }
        }
    }

    private enum ReapUnvalidatedNetworks {
        REAP,
        DONT_REAP
    }

    private static class SettingsObserver extends ContentObserver {
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

        public void onChange(boolean selfChange) {
            Slog.wtf(ConnectivityService.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            ConnectivityService.loge("No matching event to send for URI=" + uri);
        }
    }

    private class ShellCmd extends ShellCommand {
        private ShellCmd() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0024 A[Catch:{ Exception -> 0x006b }] */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0029 A[Catch:{ Exception -> 0x006b }] */
        public int onCommand(String cmd) {
            boolean z;
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                if (cmd.hashCode() == 144736062) {
                    if (cmd.equals("airplane-mode")) {
                        z = false;
                        if (!z) {
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
                    }
                }
                z = true;
                if (!z) {
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

    private enum UnneededFor {
        LINGER,
        TEARDOWN
    }

    private static class ValidationLog {
        final LocalLog.ReadOnlyLocalLog mLog;
        final String mName;
        final Network mNetwork;

        ValidationLog(Network network, String name, LocalLog.ReadOnlyLocalLog log) {
            this.mNetwork = network;
            this.mName = name;
            this.mLog = log;
        }
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

    private void addValidationLogs(LocalLog.ReadOnlyLocalLog log, Network network, String name) {
        synchronized (this.mValidationLogs) {
            while (this.mValidationLogs.size() >= 10) {
                this.mValidationLogs.removeLast();
            }
            this.mValidationLogs.addFirst(new ValidationLog(network, name, log));
        }
    }

    public ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this(context, netManager, statsService, policyManager, new IpConnectivityLog());
    }

    @VisibleForTesting
    protected ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager, IpConnectivityLog logger) {
        boolean wifiOnly;
        Context context2 = context;
        this.mVpns = new SparseArray<>();
        this.mDefaultInetConditionPublished = 0;
        this.mDefaultProxy = null;
        this.mProxyLock = new Object();
        this.mDefaultProxyDisabled = false;
        this.mGlobalProxy = null;
        this.mPacManager = null;
        this.mNextNetId = 100;
        this.mNextNetworkRequestId = 1;
        this.mP2pNetworkInfo = new NetworkInfo(13, 0, "WIFI_P2P", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        this.mNetworkRequestInfoLogs = new LocalLog(20);
        this.mNetworkInfoBlockingLogs = new LocalLog(40);
        this.mWakelockLogs = new LocalLog(20);
        this.mTotalWakelockAcquisitions = 0;
        this.mTotalWakelockReleases = 0;
        this.mTotalWakelockDurationMs = 0;
        this.mMaxWakelockDurationMs = 0;
        this.mLastWakeLockAcquireTimestamp = 0;
        this.mValidationLogs = new ArrayDeque<>(10);
        this.mLegacyTypeTracker = new LegacyTypeTracker();
        this.mPriorityDumper = new PriorityDump.PriorityDumper() {
            public void dumpHigh(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                ConnectivityService.this.doDump(fd, pw, new String[]{ConnectivityService.DIAG_ARG}, asProto);
                ConnectivityService.this.doDump(fd, pw, new String[]{ConnectivityService.SHORT_ARG}, asProto);
            }

            public void dumpNormal(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                ConnectivityService.this.doDump(fd, pw, args, asProto);
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
                ConnectivityService.this.doDump(fd, pw, args, asProto);
            }
        };
        this.mCust = (HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[0]);
        this.mDataActivityObserver = new BaseNetworkObserver() {
            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
                ConnectivityService.this.sendDataActivityBroadcast(Integer.parseInt(label), active, tsNanos);
            }
        };
        this.mNetdEventCallback = new BaseNetdEventCallback() {
            public void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) {
                try {
                    ConnectivityService.this.mHandler.sendMessage(ConnectivityService.this.mHandler.obtainMessage(38, new DnsManager.PrivateDnsValidationUpdate(netId, InetAddress.parseNumericAddress(ipAddress), hostname, validated)));
                } catch (IllegalArgumentException e) {
                    ConnectivityService.loge("Error parsing ip address in validation event");
                }
            }
        };
        this.mPolicyListener = new NetworkPolicyManager.Listener() {
            public void onUidRulesChanged(int uid, int uidRules) {
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                if (restrictBackground) {
                    ConnectivityService.log("onRestrictBackgroundChanged(true): disabling tethering");
                    ConnectivityService.this.mTethering.untetherAll();
                }
            }
        };
        this.mProvisioningUrlFile = new File(PROVISIONING_URL_PATH);
        this.mUserIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
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
                    }
                }
            }
        };
        this.mUserPresentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ConnectivityService.this.updateLockdownVpn();
                ConnectivityService.this.mContext.unregisterReceiver(this);
            }
        };
        this.mNetworkFactoryInfos = new HashMap<>();
        this.mNetworkRequests = new HashMap<>();
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
        this.mNetworkRequests.put(this.mDefaultRequest, new NetworkRequestInfo(null, this.mDefaultRequest, new Binder()));
        this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI);
        this.mDefaultMobileDataRequest = createDefaultInternetRequestForTransport(0, NetworkRequest.Type.BACKGROUND_REQUEST);
        this.mHandlerThread = new HandlerThread("ConnectivityServiceThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mTrackerHandler = new NetworkStateTrackerHandler(this.mHandlerThread.getLooper());
        this.mHwCSEx.setupUniqueDeviceName();
        this.mReleasePendingIntentDelayMs = Settings.Secure.getInt(context.getContentResolver(), "connectivity_release_pending_intent_delay_ms", 5000);
        this.mLingerDelayMs = this.mSystemProperties.getInt(LINGER_DELAY_PROPERTY, DEFAULT_LINGER_DELAY_MS);
        this.mContext = (Context) Preconditions.checkNotNull(context2, "missing Context");
        this.mNetd = (INetworkManagementService) Preconditions.checkNotNull(netManager, "missing INetworkManagementService");
        this.mStatsService = (INetworkStatsService) Preconditions.checkNotNull(statsService, "missing INetworkStatsService");
        this.mPolicyManager = (INetworkPolicyManager) Preconditions.checkNotNull(policyManager, "missing INetworkPolicyManager");
        this.mPolicyManagerInternal = (NetworkPolicyManagerInternal) Preconditions.checkNotNull((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class), "missing NetworkPolicyManagerInternal");
        this.mKeyStore = KeyStore.getInstance();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        try {
            this.mPolicyManager.registerListener(this.mPolicyListener);
        } catch (RemoteException e) {
            loge("unable to register INetworkPolicyListener" + e);
        }
        PowerManager powerManager = (PowerManager) context2.getSystemService("power");
        this.mNetTransitionWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetTransitionWakeLockTimeout = this.mContext.getResources().getInteger(17694830);
        this.mPendingIntentWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetConfigs = new NetworkConfig[48];
        log("wifiOnly=" + wifiOnly);
        String[] naStrings = context.getResources().getStringArray(17236063);
        int length = naStrings.length;
        for (int i = 0; i < length; i++) {
            try {
                NetworkConfig n = new NetworkConfig(naStrings[i]);
                log("naString=" + naString + " config=" + n);
                if (n.type > 47) {
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
        for (int p : context.getResources().getIntArray(17236028)) {
            if (this.mNetConfigs[p] == null || this.mProtectedNetworks.contains(Integer.valueOf(p))) {
                loge("Ignoring protectedNetwork " + p);
            } else {
                this.mProtectedNetworks.add(Integer.valueOf(p));
            }
        }
        this.mTestMode = this.mSystemProperties.get("cm.test.mode").equals("true") && this.mSystemProperties.get("ro.build.type").equals("eng");
        this.mTethering = makeTethering();
        this.mPermissionMonitor = new PermissionMonitor(this.mContext, this.mNetd);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_STARTED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        this.mContext.registerReceiverAsUser(this.mUserPresentReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_PRESENT"), null, null);
        try {
            this.mNetd.registerObserver(this.mTethering);
            this.mNetd.registerObserver(this.mDataActivityObserver);
        } catch (RemoteException e3) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error registering observer :");
            sb.append(HW_DEBUGGABLE ? e3 : "*");
            loge(sb.toString());
        }
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerSettingsCallbacks();
        this.mDataConnectionStats = new DataConnectionStats(this.mContext);
        this.mDataConnectionStats.startMonitoring();
        this.mPacManager = new PacManager(this.mContext, this.mHandler, 16);
        this.mUserManager = (UserManager) context2.getSystemService("user");
        this.mKeepaliveTracker = new KeepaliveTracker(this.mHandler);
        this.mNotifier = new NetworkNotificationManager(this.mContext, this.mTelephonyManager, (NotificationManager) this.mContext.getSystemService(NotificationManager.class));
        LingerMonitor lingerMonitor = new LingerMonitor(this.mContext, this.mNotifier, Settings.Global.getInt(this.mContext.getContentResolver(), "network_switch_notification_daily_limit", 3), Settings.Global.getLong(this.mContext.getContentResolver(), "network_switch_notification_rate_limit_millis", 60000));
        this.mLingerMonitor = lingerMonitor;
        this.mMultinetworkPolicyTracker = createMultinetworkPolicyTracker(this.mContext, this.mHandler, new Runnable() {
            public final void run() {
                ConnectivityService.this.rematchForAvoidBadWifiUpdate();
            }
        });
        this.mMultinetworkPolicyTracker.start();
        this.mMultipathPolicyTracker = new MultipathPolicyTracker(this.mContext, this.mHandler);
        this.mDnsManager = new DnsManager(this.mContext, this.mNetd, this.mSystemProperties);
        registerPrivateDnsSettingsCallbacks();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PKT_CNT_SAMPLE_INTERVAL_ELAPSED);
        if (HiSiWifiComm.hisiWifiEnabled()) {
            filter.addAction("android.net.wifi.p2p.WIFI_P2P_NETWORK_CHANGED_ACTION");
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (HiSiWifiComm.hisiWifiEnabled() && "android.net.wifi.p2p.WIFI_P2P_NETWORK_CHANGED_ACTION".equals(action)) {
                    NetworkInfo unused = ConnectivityService.this.mP2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                }
            }
        }, new IntentFilter(filter));
    }

    private Tethering makeTethering() {
        Tethering tethering = new Tethering(this.mContext, this.mNetd, this.mStatsService, this.mPolicyManager, IoThread.get().getLooper(), new MockableSystemProperties(), new TetheringDependencies() {
            public boolean isTetheringSupported() {
                return ConnectivityService.this.isTetheringSupported();
            }
        });
        return tethering;
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
    public void updateMobileDataAlwaysOn() {
        this.mHandler.sendEmptyMessage(30);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePrivateDnsSettings() {
        this.mHandler.sendEmptyMessage(37);
    }

    /* access modifiers changed from: private */
    public void handleMobileDataAlwaysOn() {
        boolean isEnabled = true;
        boolean enable = toBool(Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data_always_on", 0)) || this.mCust.mMobileDataAlwaysOnCust;
        if (this.mNetworkRequests.get(this.mDefaultMobileDataRequest) == null) {
            isEnabled = false;
        }
        if (enable != isEnabled) {
            if (enable) {
                handleRegisterNetworkRequest(new NetworkRequestInfo(null, this.mDefaultMobileDataRequest, new Binder()));
            } else {
                handleReleaseNetworkRequest(this.mDefaultMobileDataRequest, 1000);
            }
        }
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Settings.Global.getUriFor("http_proxy"), 9);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("mobile_data_always_on"), 30);
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
            int i = 100;
            while (i <= MAX_NET_ID) {
                try {
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
                    i++;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            throw new IllegalStateException("No free netIds");
        }
    }

    private NetworkState getFilteredNetworkState(int networkType, int uid, boolean ignoreBlocked) {
        NetworkState state;
        if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
            return NetworkState.EMPTY;
        }
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            state = nai.getNetworkState();
            state.networkInfo.setType(networkType);
        } else {
            boolean z = false;
            NetworkInfo info = new NetworkInfo(networkType, 0, ConnectivityManager.getNetworkTypeName(networkType), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            info.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
            if (!(networkType == 13 || networkType == 1 || networkType == 0)) {
                z = true;
            }
            info.setIsAvailable(z);
            NetworkCapabilities capabilities = new NetworkCapabilities();
            capabilities.setCapability(18, true ^ info.isRoaming());
            NetworkState networkState = new NetworkState(info, new LinkProperties(), capabilities, null, null, null);
            state = networkState;
        }
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state;
    }

    /* access modifiers changed from: private */
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0023, code lost:
        return null;
     */
    private Network[] getVpnUnderlyingNetworks(int uid) {
        synchronized (this.mVpns) {
            if (!this.mLockdownEnabled) {
                Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
                if (vpn != null && vpn.appliesToUid(uid)) {
                    Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
                    return underlyingNetworks;
                }
            }
        }
    }

    private NetworkState getUnfilteredActiveNetworkState(int uid) {
        NetworkAgentInfo nai = null;
        NetworkAgentInfo nai2 = this.mNetworkForRequestId.get(this.mDefaultRequest.requestId);
        if (nai2 instanceof NetworkAgentInfo) {
            nai = nai2;
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0026, code lost:
        if (r4 != null) goto L_0x002b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0028, code lost:
        r0 = com.android.server.backup.BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002b, code lost:
        r0 = r4.getInterfaceName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0035, code lost:
        return r3.mPolicyManagerInternal.isUidNetworkingBlocked(r5, r0);
     */
    private boolean isNetworkWithLinkPropertiesBlocked(LinkProperties lp, int uid, boolean ignoreBlocked) {
        if (ignoreBlocked || isSystem(uid)) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn != null && vpn.isBlockingUid(uid)) {
                return true;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        if (r1 == false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0033, code lost:
        r0 = "BLOCKED";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0036, code lost:
        r0 = "UNBLOCKED";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0038, code lost:
        log(java.lang.String.format("Returning %s NetworkInfo to uid=%d", new java.lang.Object[]{r0, java.lang.Integer.valueOf(r8)}));
        r2 = r6.mNetworkInfoBlockingLogs;
        r2.log(r0 + " " + r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0067, code lost:
        return;
     */
    private void maybeLogBlockedNetworkInfo(NetworkInfo ni, int uid) {
        boolean blocked;
        if (ni != null) {
            synchronized (this.mBlockedAppUids) {
                if (ni.getDetailedState() == NetworkInfo.DetailedState.BLOCKED && this.mBlockedAppUids.add(Integer.valueOf(uid))) {
                    blocked = true;
                } else if (ni.isConnected() && this.mBlockedAppUids.remove(Integer.valueOf(uid))) {
                    blocked = false;
                }
            }
        }
    }

    private void filterNetworkStateForUid(NetworkState state, int uid, boolean ignoreBlocked) {
        if (state != null && state.networkInfo != null && state.linkProperties != null) {
            if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, ignoreBlocked)) {
                state.networkInfo.setDetailedState(NetworkInfo.DetailedState.BLOCKED, null, null);
                log("Network is BLOCKED, uid = " + uid);
            }
            if (this.mLockdownTracker != null) {
                this.mLockdownTracker.augmentNetworkInfo(state.networkInfo);
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
        int user = UserHandle.getUserId(uid);
        int vpnNetId = 0;
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(user);
            if (vpn != null && vpn.appliesToUid(uid)) {
                vpnNetId = vpn.getNetId();
            }
        }
        if (vpnNetId != 0) {
            NetworkAgentInfo nai = getNetworkAgentInfoForNetId(vpnNetId);
            if (nai != null && createDefaultNetworkCapabilitiesForUid(uid).satisfiedByNetworkCapabilities(nai.networkCapabilities)) {
                return nai.network;
            }
        }
        NetworkAgentInfo nai2 = getDefaultNetwork();
        if (nai2 != null && isNetworkWithLinkPropertiesBlocked(nai2.linkProperties, uid, ignoreBlocked)) {
            log("ActiveNetwork is blocked, uid = " + uid + ", ignoreBlocked = " + ignoreBlocked);
            nai2 = null;
        }
        return nai2 != null ? nai2.network : null;
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
        if (!HiSiWifiComm.hisiWifiEnabled() || 13 != networkType) {
            int uid = Binder.getCallingUid();
            if (getVpnUnderlyingNetworks(uid) != null) {
                NetworkState state = getUnfilteredActiveNetworkState(uid);
                if (state.networkInfo != null && state.networkInfo.getType() == networkType) {
                    filterNetworkStateForUid(state, uid, false);
                    return state.networkInfo;
                }
            }
            return getFilteredNetworkState(networkType, uid, false).networkInfo;
        }
        log("getNetworkInfo mP2pNetworkInfo:" + this.mP2pNetworkInfo);
        return new NetworkInfo(this.mP2pNetworkInfo);
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
        for (int networkType = 0; networkType <= 47; networkType++) {
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
        NetworkState state = getFilteredNetworkState(networkType, uid, false);
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
        enforceAccessPermission();
        HashMap<Network, NetworkCapabilities> result = new HashMap<>();
        NetworkAgentInfo nai = getDefaultNetwork();
        NetworkCapabilities nc = getNetworkCapabilitiesInternal(nai);
        if (nc != null) {
            result.put(nai.network, nc);
        }
        synchronized (this.mVpns) {
            if (!this.mLockdownEnabled) {
                Vpn vpn = this.mVpns.get(userId);
                if (vpn != null) {
                    Network[] networks = vpn.getUnderlyingNetworks();
                    if (networks != null) {
                        for (Network network : networks) {
                            NetworkCapabilities nc2 = getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
                            if (nc2 != null) {
                                result.put(network, nc2);
                            }
                        }
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
        if (nai != null) {
            synchronized (nai) {
                if (nai.networkCapabilities != null) {
                    NetworkCapabilities networkCapabilitiesRestrictedForCallerPermissions = networkCapabilitiesRestrictedForCallerPermissions(nai.networkCapabilities, Binder.getCallingPid(), Binder.getCallingUid());
                    return networkCapabilitiesRestrictedForCallerPermissions;
                }
            }
        }
        return null;
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
        NetworkCapabilities caps = getUnfilteredActiveNetworkState(Binder.getCallingUid()).networkCapabilities;
        if (caps != null) {
            return true ^ caps.hasCapability(11);
        }
        return true;
    }

    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) {
        NetworkInfo.DetailedState netState;
        LinkProperties lp;
        int netId;
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
                    Binder.restoreCallingIdentity(token);
                    return ok;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
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
        if (!(this.mBestLegacyRoute == null || this.mBestLegacyRoute.getDestination() == null || bestRoute == null || bestRoute.getDestination() == null || this.mBestLegacyRoute.getDestination().getAddress() == null || !this.mBestLegacyRoute.getDestination().getAddress().equals(bestRoute.getDestination().getAddress()))) {
            this.mHwCSEx.removeLegacyRouteToHost(this.mLegacyRouteNetId, this.mBestLegacyRoute, this.mLegacyRouteUid);
            log("removing " + this.mBestLegacyRoute + " for interface " + this.mBestLegacyRoute.getInterface() + " mLegacyRouteNetId " + this.mLegacyRouteNetId + " mLegacyRouteUid " + this.mLegacyRouteUid);
        }
        if (bestRoute != null) {
            log("Adding " + bestRoute + " for interface " + bestRoute.getInterface() + " netId " + netId + " uid " + uid);
        }
        try {
            this.mNetd.addLegacyRouteForNetId(netId, bestRoute, uid);
            this.mBestLegacyRoute = bestRoute;
            this.mLegacyRouteNetId = netId;
            this.mLegacyRouteUid = uid;
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
        this.mIpConnectivityMetrics = IIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService("connmetrics"));
        if (this.mIpConnectivityMetrics == null) {
            Slog.wtf(TAG, "Missing IIpConnectivityMetrics");
        }
        try {
            this.mIpConnectivityMetrics.addNetdEventCallback(0, this.mNetdEventCallback);
        } catch (Exception e) {
            loge("Error registering netd callback: " + e);
        }
    }

    private void enforceCrossUserPermission(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
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

    private void enforceSettingsPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_SETTINGS", "ConnectivityService");
    }

    private boolean checkSettingsPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.NETWORK_SETTINGS") == 0;
    }

    private boolean checkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0;
    }

    private void enforceTetherAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
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
    public void sendDataActivityBroadcast(int deviceType, boolean active, long tsNanos) {
        long ident;
        Intent intent = new Intent("android.net.conn.DATA_ACTIVITY_CHANGE");
        intent.putExtra("deviceType", deviceType);
        intent.putExtra("isActive", active);
        intent.putExtra("tsNanos", tsNanos);
        long ident2 = Binder.clearCallingIdentity();
        try {
            long ident3 = ident2;
            try {
                this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_DATA_ACTIVITY_CHANGE", null, null, 0, null, null);
                Binder.restoreCallingIdentity(ident3);
            } catch (Throwable th) {
                th = th;
                ident = ident3;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            ident = ident2;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void sendStickyBroadcast(Intent intent) {
        synchronized (this) {
            if (!this.mSystemReady) {
                this.mInitialBroadcast = new Intent(intent);
            }
            intent.addFlags(67108864);
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
                    opts.setMaxManifestReceiverApiLevel(23);
                    options = opts.toBundle();
                }
                try {
                    BatteryStatsService.getService().noteConnectivityChanged(intent.getIntExtra("networkType", -1), ni != null ? ni.getState().toString() : "?");
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
        loadGlobalProxy();
        registerNetdEventCallback();
        synchronized (this) {
            this.mSystemReady = true;
            if (this.mInitialBroadcast != null) {
                this.mContext.sendStickyBroadcastAsUser(this.mInitialBroadcast, UserHandle.ALL);
                this.mInitialBroadcast = null;
            }
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9));
        updateLockdownVpn();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(30));
        this.mHandler.sendMessage(this.mHandler.obtainMessage(25));
        this.mPermissionMonitor.startMonitoring();
    }

    private void setupDataActivityTracking(NetworkAgentInfo networkAgent) {
        String iface = networkAgent.linkProperties.getInterfaceName();
        int type = -1;
        int timeout = 0;
        if (networkAgent.networkCapabilities.hasTransport(0)) {
            timeout = Settings.Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_mobile", 10);
            type = 0;
        } else if (networkAgent.networkCapabilities.hasTransport(1)) {
            timeout = Settings.Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_wifi", 15);
            type = 1;
        }
        int timeout2 = timeout;
        if (timeout2 > 0 && iface != null && type != -1) {
            try {
                this.mNetd.addIdleTimer(iface, timeout2, type);
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
                this.mNetd.removeIdleTimer(iface);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception in removeDataActivityTracking ");
                sb.append(HW_DEBUGGABLE ? e : "*");
                loge(sb.toString());
            }
        }
    }

    private void updateMtu(LinkProperties newLp, LinkProperties oldLp) {
        String iface = newLp.getInterfaceName();
        int mtu = newLp.getMtu();
        if (oldLp != null || mtu != 0) {
            if (oldLp != null && newLp.isIdenticalMtu(oldLp)) {
                log("identical MTU - not setting");
            } else if (!LinkProperties.isValidMtu(mtu, newLp.hasGlobalIPv6Address())) {
                if (mtu != 0) {
                    loge("Unexpected mtu value: " + mtu + ", " + iface);
                }
            } else if (TextUtils.isEmpty(iface)) {
                loge("Setting MTU size with null iface.");
            } else {
                try {
                    log("Setting MTU size: " + iface + ", " + mtu);
                    this.mNetd.setMtu(iface, mtu);
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

    private void updateTcpBufferSizes(NetworkAgentInfo nai) {
        if (isDefaultNetwork(nai)) {
            String tcpBufferSizes = nai.linkProperties.getTcpBufferSizes();
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
                    Slog.d(str, "Setting tx/rx TCP buffers to " + tcpBufferSizes);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_min", values[0]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_def", values[1]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_max", values[2]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_min", values[3]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_def", values[4]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_max", values[5]);
                    this.mCurrentTcpBufferSizes = tcpBufferSizes;
                } catch (IOException e) {
                    loge("Can't set TCP buffer sizes:" + e);
                }
                Integer rwndValue = Integer.valueOf(Settings.Global.getInt(this.mContext.getContentResolver(), "tcp_default_init_rwnd", this.mSystemProperties.getInt(DEFAULT_TCP_RWND_KEY, 0)));
                if (rwndValue.intValue() != 0) {
                    this.mSystemProperties.set("sys.sysctl.tcp_def_init_rwnd", rwndValue.toString());
                }
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
        int ret = RESTORE_DEFAULT_NETWORK_DELAY;
        if (networkType <= 47 && this.mNetConfigs[networkType] != null) {
            ret = this.mNetConfigs[networkType].restoreTime;
        }
        return ret;
    }

    private void dumpNetworkDiagnostics(IndentingPrintWriter pw) {
        List<NetworkDiagnostics> netDiags = new ArrayList<>();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
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
    public void doDump(FileDescriptor fd, PrintWriter writer, String[] args, boolean asProto) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (!DumpUtils.checkDumpPermission(this.mContext, TAG, pw) || asProto) {
            return;
        }
        if (ArrayUtils.contains(args, DIAG_ARG)) {
            dumpNetworkDiagnostics(pw);
        } else if (ArrayUtils.contains(args, TETHERING_ARG)) {
            this.mTethering.dump(fd, pw, args);
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
            for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
                pw.println(nai.toString());
                pw.increaseIndent();
                int i = 0;
                pw.println(String.format("Requests: REQUEST:%d LISTEN:%d BACKGROUND_REQUEST:%d total:%d", new Object[]{Integer.valueOf(nai.numForegroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests() - nai.numRequestNetworkRequests()), Integer.valueOf(nai.numBackgroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests())}));
                pw.increaseIndent();
                while (true) {
                    int i2 = i;
                    if (i2 >= nai.numNetworkRequests()) {
                        break;
                    }
                    pw.println(nai.requestAt(i2).toString());
                    i = i2 + 1;
                }
                pw.decreaseIndent();
                pw.println("Lingered:");
                pw.increaseIndent();
                nai.dumpLingerTimers(pw);
                pw.decreaseIndent();
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Network Requests:");
            pw.increaseIndent();
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                pw.println(nri.toString());
            }
            pw.println();
            pw.decreaseIndent();
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
                synchronized (this.mValidationLogs) {
                    pw.println("mValidationLogs (most recent first):");
                    Iterator<ValidationLog> it2 = this.mValidationLogs.iterator();
                    while (it2.hasNext()) {
                        ValidationLog p = it2.next();
                        pw.println(p.mNetwork + " - " + p.mName);
                        pw.increaseIndent();
                        p.mLog.dump(fd, pw, args);
                        pw.decreaseIndent();
                    }
                }
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
                pw.decreaseIndent();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isLiveNetworkAgent(NetworkAgentInfo nai, int what) {
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

    private boolean networkRequiresValidation(NetworkAgentInfo nai) {
        return NetworkMonitor.isValidationRequired(this.mDefaultRequest.networkCapabilities, nai.networkCapabilities);
    }

    /* access modifiers changed from: private */
    public void handlePrivateDnsSettingsChanged() {
        DnsManager.PrivateDnsConfig cfg = this.mDnsManager.getPrivateDnsConfig();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            handlePerNetworkPrivateDnsConfig(nai, cfg);
            if (networkRequiresValidation(nai)) {
                handleUpdateLinkProperties(nai, new LinkProperties(nai.linkProperties));
            }
        }
    }

    private void handlePerNetworkPrivateDnsConfig(NetworkAgentInfo nai, DnsManager.PrivateDnsConfig cfg) {
        if (networkRequiresValidation(nai)) {
            nai.networkMonitor.notifyPrivateDnsSettingsChanged(cfg);
            updatePrivateDns(nai, cfg);
        }
    }

    /* access modifiers changed from: private */
    public void updatePrivateDns(NetworkAgentInfo nai, DnsManager.PrivateDnsConfig newCfg) {
        this.mDnsManager.updatePrivateDns(nai.network, newCfg);
        updateDnses(nai.linkProperties, null, nai.network.netId);
    }

    /* access modifiers changed from: private */
    public void handlePrivateDnsValidationUpdate(DnsManager.PrivateDnsValidationUpdate update) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetId(update.netId);
        if (nai != null) {
            this.mDnsManager.updatePrivateDnsValidation(update);
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
    public void handleAsyncChannelHalfConnect(Message msg) {
        AsyncChannel ac = (AsyncChannel) msg.obj;
        if (this.mNetworkFactoryInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                log("NetworkFactory connected");
                for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                    if (!nri.request.isListen()) {
                        NetworkAgentInfo nai = getNetworkForRequest(nri.request.requestId);
                        ac.sendMessage(536576, nai != null ? nai.getCurrentScore() : 0, 0, nri.request);
                    }
                }
                return;
            }
            loge("Error connecting NetworkFactory");
            this.mNetworkFactoryInfos.remove(msg.obj);
        } else if (!this.mNetworkAgentInfos.containsKey(msg.replyTo)) {
        } else {
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
    public void handleAsyncChannelDisconnected(Message msg) {
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
        nai.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_DISCONNECTED);
        this.mNetworkAgentInfos.remove(nai.messenger);
        nai.maybeStopClat();
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.remove(nai.network.netId);
            sendNetworkStickyBroadcastAsUser("remove", nai);
        }
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest request = nai.requestAt(i);
            NetworkAgentInfo currentNetwork = getNetworkForRequest(request.requestId);
            if (currentNetwork != null && currentNetwork.network.netId == nai.network.netId) {
                clearNetworkForRequest(request.requestId);
                sendUpdatedScoreToFactories(request, 0);
            }
        }
        nai.clearLingerState();
        if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
            removeDataActivityTracking(nai);
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
            try {
                this.mNetd.removeNetwork(nai.network.netId);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception removing network: ");
                sb.append(HW_DEBUGGABLE ? e : "*");
                loge(sb.toString());
            }
            this.mDnsManager.removeNetwork(nai.network);
        }
        synchronized (this.mNetworkForNetId) {
            this.mNetIdInUse.delete(nai.network.netId);
        }
        if (SystemProperties.getBoolean("ro.config.usb_rj45", false) && nai.isEthernet()) {
            sendInetConditionBroadcast(nai.networkInfo);
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
    public void handleRegisterNetworkRequestWithIntent(Message msg) {
        NetworkRequestInfo nri = (NetworkRequestInfo) msg.obj;
        NetworkRequestInfo existingRequest = findExistingNetworkRequestInfo(nri.mPendingIntent);
        if (existingRequest != null) {
            log("Replacing " + existingRequest.request + " with " + nri.request + " because their intents matched.");
            handleReleaseNetworkRequest(existingRequest.request, getCallingUid());
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
            sendUpdatedScoreToFactories(nri.request, 0);
        }
    }

    /* access modifiers changed from: private */
    public void handleReleaseNetworkRequestWithIntent(PendingIntent pendingIntent, int callingUid) {
        NetworkRequestInfo nri = findExistingNetworkRequestInfo(pendingIntent);
        if (nri != null) {
            handleReleaseNetworkRequest(nri.request, callingUid);
        }
    }

    private boolean unneeded(NetworkAgentInfo nai, UnneededFor reason) {
        int numRequests;
        switch (reason) {
            case TEARDOWN:
                numRequests = nai.numRequestNetworkRequests();
                break;
            case LINGER:
                numRequests = nai.numForegroundNetworkRequests();
                break;
            default:
                Slog.wtf(TAG, "Invalid reason. Cannot happen.");
                return true;
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
        log(String.format("UID %d attempted to %s for unowned request %s", new Object[]{Integer.valueOf(callingUid), requestedOperation, nri}));
        return null;
    }

    /* access modifiers changed from: private */
    public void handleTimedOutNetworkRequest(NetworkRequestInfo nri) {
        if (this.mNetworkRequests.get(nri.request) != null && getNetworkForRequest(nri.request.requestId) == null) {
            log("releasing " + nri.request + " (timeout)");
            handleRemoveNetworkRequest(nri);
            callCallbackForRequest(nri, null, 524293, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequestInfo nri = getNriForAppRequest(request, callingUid, "release NetworkRequest");
        if (nri != null) {
            log("releasing " + nri.request + " (release request)");
            handleRemoveNetworkRequest(nri);
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
                Slog.wtf(TAG, "BUG: too small request count " + requests + " for UID " + nri.mUid);
            } else if (requests == 1) {
                this.mUidToNetworkRequestCount.removeAt(this.mUidToNetworkRequestCount.indexOfKey(nri.mUid));
            } else {
                this.mUidToNetworkRequestCount.put(nri.mUid, requests - 1);
            }
        }
        this.mNetworkRequestInfoLogs.log("RELEASE " + nri);
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
            for (NetworkAgentInfo otherNai : this.mNetworkAgentInfos.values()) {
                if (otherNai.isSatisfyingRequest(nri.request.requestId) && otherNai != nai) {
                    String str = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Request ");
                    sb.append(nri.request);
                    sb.append(" satisfied by ");
                    sb.append(otherNai.name());
                    sb.append(", but mNetworkAgentInfos says ");
                    sb.append(nai != null ? nai.name() : "null");
                    Slog.wtf(str, sb.toString());
                }
            }
            if (!(nri.request.legacyType == -1 || nai == null)) {
                boolean i = true;
                if (wasKept) {
                    boolean doRemove = true;
                    for (int i2 = 0; i2 < nai.numNetworkRequests(); i2++) {
                        NetworkRequest otherRequest = nai.requestAt(i2);
                        if (otherRequest.legacyType == nri.request.legacyType && otherRequest.isRequest()) {
                            log(" still have other legacy request - leaving");
                            doRemove = false;
                        }
                    }
                    i = doRemove;
                }
                if (i) {
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
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(28, encodeBool(accept), encodeBool(always), network));
    }

    public void setAvoidUnvalidated(Network network) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(35, network));
    }

    /* access modifiers changed from: private */
    public void handleSetAcceptUnvalidated(Network network, boolean accept, boolean always) {
        log("handleSetAcceptUnvalidated network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated) {
            if (accept != nai.networkMisc.acceptUnvalidated) {
                int oldScore = nai.getCurrentScore();
                nai.networkMisc.acceptUnvalidated = accept;
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
    public void handleSetAvoidUnvalidated(Network network) {
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
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(29, nai.network), 8000);
    }

    public void startCaptivePortalApp(Network network) {
        enforceConnectivityInternalPermission();
        this.mHandler.post(new Runnable(network) {
            private final /* synthetic */ Network f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ConnectivityService.lambda$startCaptivePortalApp$1(ConnectivityService.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$startCaptivePortalApp$1(ConnectivityService connectivityService, Network network) {
        NetworkAgentInfo nai = connectivityService.getNetworkAgentInfoForNetwork(network);
        if (nai != null && nai.networkCapabilities.hasCapability(17)) {
            nai.networkMonitor.sendMessage(NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP);
        }
    }

    public boolean avoidBadWifi() {
        return this.mMultinetworkPolicyTracker.getAvoidBadWifi();
    }

    /* access modifiers changed from: private */
    public void rematchForAvoidBadWifiUpdate() {
        rematchAllNetworksAndRequests(null, 0);
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.networkCapabilities.hasTransport(1)) {
                sendUpdatedScoreToFactories(nai);
            }
        }
    }

    private void dumpAvoidBadWifiSettings(IndentingPrintWriter pw) {
        String description;
        if (!this.mMultinetworkPolicyTracker.configRestrictsAvoidBadWifi()) {
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
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.avoidUnvalidated) {
                pw.println(nai.name());
            }
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    private void showValidationNotification(NetworkAgentInfo nai, NetworkNotificationManager.NotificationType type) {
        String action;
        switch (type) {
            case NO_INTERNET:
                action = "android.net.conn.PROMPT_UNVALIDATED";
                break;
            case LOST_INTERNET:
                action = "android.net.conn.PROMPT_LOST_VALIDATION";
                break;
            default:
                String str = TAG;
                Slog.wtf(str, "Unknown notification type " + type);
                return;
        }
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            log("factory mode is not display dialog");
            return;
        }
        Intent intent = new Intent(action);
        intent.setData(Uri.fromParts("netId", Integer.toString(nai.network.netId), null));
        intent.addFlags(268435456);
        intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiNoInternetDialog");
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    /* access modifiers changed from: private */
    public void handlePromptUnvalidated(Network network) {
        log("handlePromptUnvalidated " + network);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (!(nai == null || nai.networkMisc == null)) {
            log("explicitlySelected:" + nai.networkMisc.explicitlySelected + " , acceptUnvalidated:" + nai.networkMisc.acceptUnvalidated);
        }
        if (nai != null && !nai.everValidated && !nai.everCaptivePortalDetected && nai.networkMisc.explicitlySelected && !nai.networkMisc.acceptUnvalidated) {
            if (1 <= nai.networkMisc.wifiApType) {
                log("ai device do not show validation notification for network " + network);
            } else if (1 == nai.networkMisc.connectToCellularAndWLAN) {
                log("duplex path selected: " + nai.networkMisc.connectToCellularAndWLAN);
            } else {
                log("show validation notification for network " + network);
                showValidationNotification(nai, NetworkNotificationManager.NotificationType.NO_INTERNET);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNetworkUnvalidated(NetworkAgentInfo nai) {
        NetworkCapabilities nc = nai.networkCapabilities;
        log("handleNetworkUnvalidated " + nai.name() + " cap=" + nc);
        if (nc.hasTransport(1) && this.mMultinetworkPolicyTracker.shouldNotifyWifiUnvalidated()) {
            showValidationNotification(nai, NetworkNotificationManager.NotificationType.LOST_INTERNET);
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

    public int tether(String iface, String callerPkg) {
        Log.d(TAG, "tether: ENTER");
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
    public boolean isTetheringSupported() {
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
        } else {
            this.mTethering.startTethering(type, receiver, showProvisioningUi);
        }
    }

    public void stopTethering(int type, String callerPkg) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext, callerPkg);
        this.mTethering.stopTethering(type);
    }

    private void ensureNetworkTransitionWakelock(String forWhom) {
        synchronized (this) {
            if (!this.mNetTransitionWakeLock.isHeld()) {
                this.mNetTransitionWakeLock.acquire();
                this.mLastWakeLockAcquireTimestamp = SystemClock.elapsedRealtime();
                this.mTotalWakelockAcquisitions++;
                this.mWakelockLogs.log("ACQUIRE for " + forWhom);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(24), (long) this.mNetTransitionWakeLockTimeout);
            }
        }
    }

    private void scheduleReleaseNetworkTransitionWakelock() {
        synchronized (this) {
            if (this.mNetTransitionWakeLock.isHeld()) {
                this.mHandler.removeMessages(24);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8), 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleReleaseNetworkTransitionWakelock(int eventId) {
        String event = eventName(eventId);
        synchronized (this) {
            if (!this.mNetTransitionWakeLock.isHeld()) {
                this.mWakelockLogs.log(String.format("RELEASE: already released (%s)", new Object[]{event}));
                Slog.w(TAG, "expected Net Transition WakeLock to be held");
                return;
            }
            this.mNetTransitionWakeLock.release();
            long lockDuration = SystemClock.elapsedRealtime() - this.mLastWakeLockAcquireTimestamp;
            this.mTotalWakelockDurationMs += lockDuration;
            this.mMaxWakelockDurationMs = Math.max(this.mMaxWakelockDurationMs, lockDuration);
            this.mTotalWakelockReleases++;
            this.mWakelockLogs.log(String.format("RELEASE (%s)", new Object[]{event}));
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(36, Binder.getCallingUid(), encodeBool(hasConnectivity), network));
    }

    /* access modifiers changed from: private */
    public void handleReportNetworkConnectivity(Network network, int uid, boolean hasConnectivity) {
        NetworkAgentInfo nai;
        if (network == null) {
            nai = getDefaultNetwork();
        } else {
            nai = getNetworkAgentInfoForNetwork(network);
        }
        if (nai != null && nai.networkInfo.getState() != NetworkInfo.State.DISCONNECTING && nai.networkInfo.getState() != NetworkInfo.State.DISCONNECTED && hasConnectivity != nai.lastValidated) {
            int netid = nai.network.netId;
            log("reportNetworkConnectivity(" + netid + ", " + hasConnectivity + ") by " + uid);
            if (nai.everConnected != 0 && !isNetworkWithLinkPropertiesBlocked(getLinkProperties(nai), uid, false)) {
                nai.networkMonitor.forceReevaluation(uid);
            }
        }
    }

    private ProxyInfo getDefaultProxy() {
        ProxyInfo ret;
        synchronized (this.mProxyLock) {
            ret = this.mGlobalProxy;
            if (ret == null && !this.mDefaultProxyDisabled) {
                ret = this.mDefaultProxy;
            }
        }
        return ret;
    }

    public ProxyInfo getProxyForNetwork(Network network) {
        if (network == null) {
            return getDefaultProxy();
        }
        ProxyInfo globalProxy = getGlobalProxy();
        if (globalProxy != null) {
            return globalProxy;
        }
        if (!NetworkUtils.queryUserAccess(Binder.getCallingUid(), network.netId)) {
            return null;
        }
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai == null) {
            return null;
        }
        synchronized (nai) {
            ProxyInfo proxyInfo = nai.linkProperties.getHttpProxy();
            if (proxyInfo == null) {
                return null;
            }
            ProxyInfo proxyInfo2 = new ProxyInfo(proxyInfo);
            return proxyInfo2;
        }
    }

    private ProxyInfo canonicalizeProxyInfo(ProxyInfo proxy) {
        if (proxy == null || !TextUtils.isEmpty(proxy.getHost())) {
            return proxy;
        }
        if (proxy.getPacFileUrl() == null || Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            return null;
        }
        return proxy;
    }

    private boolean proxyInfoEqual(ProxyInfo a, ProxyInfo b) {
        ProxyInfo a2 = canonicalizeProxyInfo(a);
        ProxyInfo b2 = canonicalizeProxyInfo(b);
        return Objects.equals(a2, b2) && (a2 == null || Objects.equals(a2.getHost(), b2.getHost()));
    }

    /* JADX INFO: finally extract failed */
    public void setGlobalProxy(ProxyInfo proxyProperties) {
        enforceConnectivityInternalPermission();
        synchronized (this.mProxyLock) {
            if (proxyProperties != this.mGlobalProxy) {
                if (proxyProperties != null && proxyProperties.equals(this.mGlobalProxy)) {
                    return;
                }
                if (this.mGlobalProxy == null || !this.mGlobalProxy.equals(proxyProperties)) {
                    String host = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    int port = 0;
                    String exclList = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    String pacFileUrl = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    if (proxyProperties == null || (TextUtils.isEmpty(proxyProperties.getHost()) && Uri.EMPTY.equals(proxyProperties.getPacFileUrl()))) {
                        this.mGlobalProxy = null;
                    } else if (!proxyProperties.isValid()) {
                        log("Invalid proxy properties, ignoring: " + proxyProperties.toString());
                        return;
                    } else {
                        this.mGlobalProxy = new ProxyInfo(proxyProperties);
                        host = this.mGlobalProxy.getHost();
                        port = this.mGlobalProxy.getPort();
                        exclList = this.mGlobalProxy.getExclusionListAsString();
                        if (!Uri.EMPTY.equals(proxyProperties.getPacFileUrl())) {
                            pacFileUrl = proxyProperties.getPacFileUrl().toString();
                        }
                    }
                    ContentResolver res = this.mContext.getContentResolver();
                    long token = Binder.clearCallingIdentity();
                    try {
                        Settings.Global.putString(res, "global_http_proxy_host", host);
                        Settings.Global.putInt(res, "global_http_proxy_port", port);
                        Settings.Global.putString(res, "global_http_proxy_exclusion_list", exclList);
                        Settings.Global.putString(res, "global_proxy_pac_url", pacFileUrl);
                        Binder.restoreCallingIdentity(token);
                        if (this.mGlobalProxy == null) {
                            proxyProperties = this.mDefaultProxy;
                        }
                        sendProxyBroadcast(proxyProperties);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
            }
        }
    }

    private void loadGlobalProxy() {
        ProxyInfo proxyProperties;
        ContentResolver res = this.mContext.getContentResolver();
        String host = Settings.Global.getString(res, "global_http_proxy_host");
        int port = Settings.Global.getInt(res, "global_http_proxy_port", 0);
        String exclList = Settings.Global.getString(res, "global_http_proxy_exclusion_list");
        String pacFileUrl = Settings.Global.getString(res, "global_proxy_pac_url");
        if (!TextUtils.isEmpty(host) || !TextUtils.isEmpty(pacFileUrl)) {
            if (!TextUtils.isEmpty(pacFileUrl)) {
                proxyProperties = new ProxyInfo(pacFileUrl);
            } else {
                proxyProperties = new ProxyInfo(host, port, exclList);
            }
            if (!proxyProperties.isValid()) {
                log("Invalid proxy properties, ignoring: " + proxyProperties.toString());
                return;
            }
            synchronized (this.mProxyLock) {
                this.mGlobalProxy = proxyProperties;
            }
        }
    }

    public ProxyInfo getGlobalProxy() {
        ProxyInfo proxyInfo;
        synchronized (this.mProxyLock) {
            proxyInfo = this.mGlobalProxy;
        }
        return proxyInfo;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x008d, code lost:
        return;
     */
    public void handleApplyDefaultProxy(ProxyInfo proxy) {
        if (proxy != null && TextUtils.isEmpty(proxy.getHost()) && Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            proxy = null;
        }
        synchronized (this.mProxyLock) {
            if (this.mDefaultProxy != null && this.mDefaultProxy.equals(proxy)) {
                return;
            }
            if (this.mDefaultProxy != proxy) {
                if (proxy != null && !proxy.isValid()) {
                    log("Invalid proxy properties, ignoring: " + proxy.toString());
                } else if (this.mGlobalProxy == null || proxy == null || Uri.EMPTY.equals(proxy.getPacFileUrl()) || !proxy.getPacFileUrl().equals(this.mGlobalProxy.getPacFileUrl())) {
                    this.mDefaultProxy = proxy;
                    if (this.mGlobalProxy == null) {
                        if (!this.mDefaultProxyDisabled) {
                            sendProxyBroadcast(proxy);
                        }
                    }
                } else {
                    this.mGlobalProxy = proxy;
                    sendProxyBroadcast(this.mGlobalProxy);
                }
            }
        }
    }

    private void updateProxy(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        ProxyInfo oldProxyInfo = null;
        ProxyInfo newProxyInfo = newLp == null ? null : newLp.getHttpProxy();
        if (oldLp != null) {
            oldProxyInfo = oldLp.getHttpProxy();
        }
        if (!proxyInfoEqual(newProxyInfo, oldProxyInfo)) {
            sendProxyBroadcast(getDefaultProxy());
        }
    }

    /* access modifiers changed from: private */
    public void handleDeprecatedGlobalHttpProxy() {
        String proxy = Settings.Global.getString(this.mContext.getContentResolver(), "http_proxy");
        if (!TextUtils.isEmpty(proxy)) {
            String[] data = proxy.split(":");
            if (data.length != 0) {
                String str = data[0];
                int proxyPort = 8080;
                if (data.length > 1) {
                    try {
                        proxyPort = Integer.parseInt(data[1]);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                setGlobalProxy(new ProxyInfo(data[0], proxyPort, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
            }
        }
    }

    private void sendProxyBroadcast(ProxyInfo proxy) {
        if (proxy == null) {
            proxy = new ProxyInfo(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }
        if (!this.mPacManager.setCurrentProxyScriptUrl(proxy)) {
            log("sending Proxy Broadcast for " + proxy);
            Intent intent = new Intent("android.intent.action.PROXY_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("android.intent.extra.PROXY_INFO", proxy);
            long ident = Binder.clearCallingIdentity();
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Slog.d(TAG, s);
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
            boolean prepare = vpn.prepare(oldPackage, newPackage);
            return prepare;
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
        Vpn mVpn;
        boolean isInsecureVpn = true;
        if (!HwServiceFactory.getHwConnectivityManager().isInsecureVpnDisabled() || !(profile.type == 0 || profile.type == 1)) {
            isInsecureVpn = false;
        }
        if (isInsecureVpn) {
            UiThread.getHandler().post(new Runnable() {
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
                mVpn = this.mVpns.get(user);
            }
            if (mVpn != null) {
                mVpn.startLegacyVpn(profile, this.mKeyStore, egress);
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

    public VpnInfo[] getAllVpnInfo() {
        enforceConnectivityInternalPermission();
        synchronized (this.mVpns) {
            if (this.mLockdownEnabled) {
                VpnInfo[] vpnInfoArr = new VpnInfo[0];
                return vpnInfoArr;
            }
            List<VpnInfo> infoList = new ArrayList<>();
            for (int i = 0; i < this.mVpns.size(); i++) {
                VpnInfo info = createVpnInfo(this.mVpns.valueAt(i));
                if (info != null) {
                    infoList.add(info);
                }
            }
            VpnInfo[] vpnInfoArr2 = (VpnInfo[]) infoList.toArray(new VpnInfo[infoList.size()]);
            return vpnInfoArr2;
        }
    }

    private VpnInfo createVpnInfo(Vpn vpn) {
        VpnInfo info = vpn.getVpnInfo();
        VpnInfo vpnInfo = null;
        if (info == null) {
            return null;
        }
        Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
        if (underlyingNetworks == null) {
            NetworkAgentInfo defaultNetwork = getDefaultNetwork();
            if (!(defaultNetwork == null || defaultNetwork.linkProperties == null)) {
                info.primaryUnderlyingIface = getDefaultNetwork().linkProperties.getInterfaceName();
            }
        } else if (underlyingNetworks.length > 0) {
            LinkProperties linkProperties = getLinkProperties(underlyingNetworks[0]);
            if (linkProperties != null) {
                info.primaryUnderlyingIface = linkProperties.getInterfaceName();
            }
        }
        if (info.primaryUnderlyingIface != null) {
            vpnInfo = info;
        }
        return vpnInfo;
    }

    public VpnConfig getVpnConfig(int userId) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                return null;
            }
            VpnConfig vpnConfig = vpn.getVpnConfig();
            return vpnConfig;
        }
    }

    private void updateAllVpnsCapabilities() {
        synchronized (this.mVpns) {
            for (int i = 0; i < this.mVpns.size(); i++) {
                this.mVpns.valueAt(i).updateCapabilities();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b5, code lost:
        return true;
     */
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
                LockdownVpnTracker lockdownVpnTracker = new LockdownVpnTracker(this.mContext, this.mNetd, this, vpn, profile);
                setLockdownTracker(lockdownVpnTracker);
            } else {
                setLockdownTracker(null);
            }
        }
    }

    @GuardedBy("mVpns")
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

    @GuardedBy("mVpns")
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
            boolean startAlwaysOnVpn = vpn.startAlwaysOnVpn();
            return startAlwaysOnVpn;
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
            boolean isAlwaysOnPackageSupported = vpn.isAlwaysOnPackageSupported(packageName);
            return isAlwaysOnPackageSupported;
        }
    }

    public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown) {
        enforceConnectivityInternalPermission();
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
            } else if (!vpn.setAlwaysOnPackage(packageName, lockdown)) {
                return false;
            } else {
                if (startAlwaysOnVpn(userId)) {
                    return true;
                }
                vpn.setAlwaysOnPackage(null, false);
                return false;
            }
        }
    }

    public String getAlwaysOnVpnPackage(int userId) {
        enforceConnectivityInternalPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = this.mVpns.get(userId);
            if (vpn == null) {
                String str = TAG;
                Slog.w(str, "User " + userId + " has no Vpn configuration");
                return null;
            }
            String alwaysOnPackage = vpn.getAlwaysOnPackage();
            return alwaysOnPackage;
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a3, code lost:
        if (r0 == null) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c9, code lost:
        if (r0 == null) goto L_0x00cc;
     */
    private String getProvisioningUrlBaseFromFile() {
        FileReader fileReader = null;
        Configuration config = this.mContext.getResources().getConfiguration();
        try {
            fileReader = new FileReader(this.mProvisioningUrlFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileReader);
            XmlUtils.beginDocument(parser, TAG_PROVISIONING_URLS);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                    }
                    return null;
                } else if (element.equals(TAG_PROVISIONING_URL)) {
                    String mcc = parser.getAttributeValue(null, ATTR_MCC);
                    if (mcc != null) {
                        try {
                            if (Integer.parseInt(mcc) == config.mcc) {
                                String mnc = parser.getAttributeValue(null, ATTR_MNC);
                                if (mnc != null && Integer.parseInt(mnc) == config.mnc) {
                                    parser.next();
                                    if (parser.getEventType() == 4) {
                                        String text = parser.getText();
                                        try {
                                            fileReader.close();
                                        } catch (IOException e2) {
                                        }
                                        return text;
                                    }
                                }
                            } else {
                                continue;
                            }
                        } catch (NumberFormatException e3) {
                            loge("NumberFormatException in getProvisioningUrlBaseFromFile: " + e3);
                        }
                    } else {
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException e4) {
            loge("Carrier Provisioning Urls file not found");
        } catch (XmlPullParserException e5) {
            loge("Xml parser exception reading Carrier Provisioning Urls file: " + e5);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e6) {
                }
            }
            return null;
        } catch (IOException e7) {
            loge("I/O exception reading Carrier Provisioning Urls file: " + e7);
        } catch (Throwable th) {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    public String getMobileProvisioningUrl() {
        enforceConnectivityInternalPermission();
        String url = getProvisioningUrlBaseFromFile();
        if (TextUtils.isEmpty(url)) {
            url = this.mContext.getResources().getString(17040535);
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
        return String.format(url, new Object[]{this.mTelephonyManager.getSimSerialNumber(), this.mTelephonyManager.getDeviceId(), phoneNumber});
    }

    public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) {
        enforceConnectivityInternalPermission();
        if (ConnectivityManager.isNetworkTypeValid(networkType)) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mNotifier.setProvNotificationVisible(visible, 64512 + networkType + 1, action);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setAirplaneMode(boolean enable) {
        enforceConnectivityInternalPermission();
        long ident = Binder.clearCallingIdentity();
        try {
            Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", encodeBool(enable));
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, enable);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003f, code lost:
        return;
     */
    public void onUserStart(int userId) {
        synchronized (this.mVpns) {
            if (this.mVpns.get(userId) != null) {
                loge("Starting user already has a VPN");
                return;
            }
            this.mVpns.put(userId, new Vpn(this.mHandler.getLooper(), this.mContext, this.mNetd, userId));
            if (this.mUserManager.getUserInfo(userId).isPrimary() && LockdownVpnTracker.isEnabled()) {
                updateLockdownVpn();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserStop(int userId) {
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
    public void onUserAdded(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                this.mVpns.valueAt(i).onUserAdded(userId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserRemoved(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                this.mVpns.valueAt(i).onUserRemoved(userId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserUnlocked(int userId) {
        synchronized (this.mVpns) {
            if (!this.mUserManager.getUserInfo(userId).isPrimary() || !LockdownVpnTracker.isEnabled()) {
                startAlwaysOnVpn(userId);
            } else {
                updateLockdownVpn();
            }
        }
    }

    /* access modifiers changed from: private */
    public void ensureNetworkRequestHasType(NetworkRequest request) {
        if (request.type == NetworkRequest.Type.NONE) {
            throw new IllegalArgumentException("All NetworkRequests in ConnectivityService must have a type");
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
        log(String.format("updateSignalStrengthThresholds: %s, sending %s to %s", new Object[]{detail, Arrays.toString(thresholdsArray.toArray()), nai.name()}));
        nai.asyncChannel.sendMessage(528398, 0, 0, thresholds);
    }

    private void ensureValidNetworkSpecifier(NetworkCapabilities nc) {
        if (nc != null) {
            NetworkSpecifier ns = nc.getNetworkSpecifier();
            if (ns != null) {
                MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(ns);
                ns.assertValidFromUid(Binder.getCallingUid());
            }
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(19, nri));
            if (timeoutMs > 0) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(20, nri), (long) timeoutMs);
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(26, nri));
        return networkRequest;
    }

    private void releasePendingNetworkRequestWithDelay(PendingIntent operation) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation), (long) this.mReleasePendingIntentDelayMs);
    }

    public void releasePendingNetworkRequest(PendingIntent operation) {
        Preconditions.checkNotNull(operation, "PendingIntent cannot be null.");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation));
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(21, nri));
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(21, nri));
    }

    public void releaseNetworkRequest(NetworkRequest networkRequest) {
        ensureNetworkRequestHasType(networkRequest);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(22, getCallingUid(), 0, networkRequest));
    }

    public void registerNetworkFactory(Messenger messenger, String name) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17, new NetworkFactoryInfo(name, messenger, new AsyncChannel())));
    }

    /* access modifiers changed from: private */
    public void handleRegisterNetworkFactory(NetworkFactoryInfo nfi) {
        log("Got NetworkFactory Messenger for " + nfi.name);
        this.mNetworkFactoryInfos.put(nfi.messenger, nfi);
        nfi.asyncChannel.connect(this.mContext, this.mTrackerHandler, nfi.messenger);
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23, messenger));
    }

    /* access modifiers changed from: private */
    public void handleUnregisterNetworkFactory(Messenger messenger) {
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
        NetworkAgentInfo networkForRequest = getNetworkForRequest(this.mDefaultRequest.requestId);
        if (networkForRequest instanceof NetworkAgentInfo) {
            return networkForRequest;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean isDefaultNetwork(NetworkAgentInfo nai) {
        return nai == getDefaultNetwork();
    }

    private boolean isDefaultRequest(NetworkRequestInfo nri) {
        return nri.request.requestId == this.mDefaultRequest.requestId;
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc) {
        enforceConnectivityInternalPermission();
        LinkProperties lp = new LinkProperties(linkProperties);
        lp.ensureDirectlyConnectedRoutes();
        NetworkCapabilities nc = new NetworkCapabilities(networkCapabilities);
        AsyncChannel asyncChannel = new AsyncChannel();
        Network network = new Network(reserveNetId());
        NetworkInfo networkInfo2 = new NetworkInfo(networkInfo);
        Context context = this.mContext;
        NetworkStateTrackerHandler networkStateTrackerHandler = this.mTrackerHandler;
        NetworkStateTrackerHandler networkStateTrackerHandler2 = networkStateTrackerHandler;
        LinkProperties linkProperties2 = lp;
        NetworkAgentInfo nai = new NetworkAgentInfo(messenger, asyncChannel, network, networkInfo2, lp, nc, currentScore, context, networkStateTrackerHandler2, new NetworkMisc(networkMisc), this.mDefaultRequest, this);
        nai.networkCapabilities = mixInCapabilities(nai, nc);
        synchronized (this) {
            nai.networkMonitor.systemReady = this.mSystemReady;
        }
        String extraInfo = networkInfo.getExtraInfo();
        addValidationLogs(nai.networkMonitor.getValidationLogs(), nai.network, TextUtils.isEmpty(extraInfo) ? nai.networkCapabilities.getSSID() : extraInfo);
        log("registerNetworkAgent " + nai);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, nai));
        return nai.network.netId;
    }

    /* access modifiers changed from: private */
    public void handleRegisterNetworkAgent(NetworkAgentInfo nai) {
        log("Got NetworkAgent Messenger");
        NetworkAgentInfo identicalNai = this.mHwCSEx.getIdenticalActiveNetworkAgentInfo(nai);
        if (identicalNai != null) {
            synchronized (identicalNai) {
                identicalNai.networkCapabilities.combineCapabilitiesWithNoSpecifiers(nai.networkCapabilities);
            }
            nai.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_DISCONNECTED);
            rematchNetworkAndRequests(identicalNai, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
            return;
        }
        this.mNetworkAgentInfos.put(nai.messenger, nai);
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.put(nai.network.netId, nai);
            sendNetworkStickyBroadcastAsUser("remove", nai);
        }
        nai.asyncChannel.connect(this.mContext, this.mTrackerHandler, nai.messenger);
        NetworkInfo networkInfo = nai.networkInfo;
        nai.networkInfo = null;
        updateNetworkInfo(nai, networkInfo);
        updateUids(nai, null, nai.networkCapabilities);
        handleLteMobileDataStateChange(networkInfo);
    }

    private void updateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        LinkProperties newLp = new LinkProperties(networkAgent.linkProperties);
        int netId = networkAgent.network.netId;
        if (networkAgent.clatd != null) {
            networkAgent.clatd.fixupLinkProperties(oldLp, newLp);
        }
        updateInterfaces(newLp, oldLp, netId, networkAgent.networkCapabilities);
        updateMtu(newLp, oldLp);
        updateTcpBufferSizes(networkAgent);
        updateRoutes(newLp, oldLp, netId);
        updateDnses(newLp, oldLp, netId);
        this.mDnsManager.updatePrivateDnsStatus(netId, newLp);
        networkAgent.updateClat(this.mNetd);
        if (isDefaultNetwork(networkAgent)) {
            handleApplyDefaultProxy(newLp.getHttpProxy());
        } else {
            updateProxy(newLp, oldLp, networkAgent);
        }
        if (!Objects.equals(newLp, oldLp)) {
            synchronized (networkAgent) {
                networkAgent.linkProperties = newLp;
            }
            notifyIfacesChangedForNetworkStats();
            notifyNetworkCallbacks(networkAgent, 524295);
        }
        updataNetworkAgentInfoForHicure(networkAgent);
        this.mKeepaliveTracker.handleCheckKeepalivesStillValid(networkAgent);
    }

    private void wakeupModifyInterface(String iface, NetworkCapabilities caps, boolean add) {
        if (caps.hasTransport(1)) {
            int mark = this.mContext.getResources().getInteger(17694831);
            int mask = this.mContext.getResources().getInteger(17694832);
            if (mark != 0 && mask != 0) {
                String prefix = "iface:" + iface;
                if (add) {
                    try {
                        this.mNetd.getNetdService().wakeupAddInterface(iface, prefix, mark, mask);
                    } catch (Exception e) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Exception modifying wakeup packet monitoring: ");
                        sb.append(HW_DEBUGGABLE ? e : "*");
                        loge(sb.toString());
                    }
                } else {
                    this.mNetd.getNetdService().wakeupDelInterface(iface, prefix, mark, mask);
                }
            }
        }
    }

    private void updateInterfaces(LinkProperties newLp, LinkProperties oldLp, int netId, NetworkCapabilities caps) {
        List list;
        List list2 = null;
        if (oldLp != null) {
            list = oldLp.getAllInterfaceNames();
        } else {
            list = null;
        }
        if (newLp != null) {
            list2 = newLp.getAllInterfaceNames();
        }
        LinkProperties.CompareResult<String> interfaceDiff = new LinkProperties.CompareResult<>(list, list2);
        for (String iface : interfaceDiff.added) {
            try {
                log("Adding iface " + iface + " to network " + netId);
                this.mNetd.addInterfaceToNetwork(iface, netId);
                wakeupModifyInterface(iface, caps, true);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception adding interface: ");
                sb.append(HW_DEBUGGABLE ? e : "*");
                loge(sb.toString());
            }
        }
        for (String iface2 : interfaceDiff.removed) {
            try {
                log("Removing iface " + iface2 + " from network " + netId);
                wakeupModifyInterface(iface2, caps, false);
                this.mNetd.removeInterfaceFromNetwork(iface2, netId);
            } catch (Exception e2) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Exception removing interface: ");
                sb2.append(HW_DEBUGGABLE ? e2 : "*");
                loge(sb2.toString());
            }
        }
    }

    private boolean updateRoutes(LinkProperties newLp, LinkProperties oldLp, int netId) {
        List list;
        RouteInfo route;
        List list2 = null;
        if (oldLp != null) {
            list = oldLp.getAllRoutes();
        } else {
            list = null;
        }
        if (newLp != null) {
            list2 = newLp.getAllRoutes();
        }
        LinkProperties.CompareResult<RouteInfo> routeDiff = new LinkProperties.CompareResult<>(list, list2);
        for (RouteInfo route2 : routeDiff.added) {
            if (!route2.hasGateway()) {
                if (HW_DEBUGGABLE) {
                    log("Adding Route [" + route2 + "] to network " + netId);
                }
                try {
                    this.mNetd.addRoute(netId, route2);
                } catch (Exception e) {
                    boolean z = route2.getDestination().getAddress() instanceof Inet4Address;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Exception in addRoute for non-gateway: ");
                    sb.append(HW_DEBUGGABLE ? e : "*");
                    loge(sb.toString());
                }
            }
        }
        for (RouteInfo route3 : routeDiff.added) {
            if (route3.hasGateway()) {
                try {
                    this.mNetd.addRoute(netId, route3);
                } catch (Exception e2) {
                    boolean z2 = route3.getGateway() instanceof Inet4Address;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Exception in addRoute for gateway: ");
                    sb2.append(HW_DEBUGGABLE ? e2 : "*");
                    loge(sb2.toString());
                }
            }
        }
        Iterator it = routeDiff.removed.iterator();
        while (it.hasNext()) {
            log("Removing Route [" + route + "] from network " + netId);
            try {
                this.mNetd.removeRoute(netId, route);
            } catch (Exception e3) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Exception in removeRoute: ");
                sb3.append(HW_DEBUGGABLE ? e3 : "*");
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

    private String getNetworkPermission(NetworkCapabilities nc) {
        if (!nc.hasCapability(13)) {
            return "SYSTEM";
        }
        if (!nc.hasCapability(19)) {
            return "NETWORK";
        }
        return null;
    }

    private NetworkCapabilities mixInCapabilities(NetworkAgentInfo nai, NetworkCapabilities nc) {
        if (nai.everConnected && !nai.isVPN() && !nai.networkCapabilities.satisfiedByImmutableNetworkCapabilities(nc)) {
            String diff = nai.networkCapabilities.describeImmutableDifferences(nc);
            if (!TextUtils.isEmpty(diff)) {
                String str = TAG;
                Slog.wtf(str, "BUG: " + nai + " lost immutable capabilities:" + diff);
            }
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
        return newNc;
    }

    /* access modifiers changed from: private */
    public void updateCapabilities(int oldScore, NetworkAgentInfo nai, NetworkCapabilities nc) {
        NetworkCapabilities prevNc;
        NetworkCapabilities newNc = mixInCapabilities(nai, nc);
        if (!Objects.equals(nai.networkCapabilities, newNc)) {
            String oldPermission = getNetworkPermission(nai.networkCapabilities);
            String newPermission = getNetworkPermission(newNc);
            if (!Objects.equals(oldPermission, newPermission) && nai.created && !nai.isVPN()) {
                try {
                    this.mNetd.setNetworkPermission(nai.network.netId, newPermission);
                } catch (RemoteException | IllegalStateException e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Exception in setNetworkPermission: ");
                    sb.append(HW_DEBUGGABLE ? e : "*");
                    loge(sb.toString());
                }
            }
            synchronized (nai) {
                prevNc = nai.networkCapabilities;
                nai.networkCapabilities = newNc;
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
                boolean meteredChanged = prevNc.hasCapability(11) != newNc.hasCapability(11);
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
                this.mNetd.addVpnUidRanges(nai.network.netId, addedRangesArray);
            }
            if (!prevRanges.isEmpty()) {
                UidRange[] removedRangesArray = new UidRange[prevRanges.size()];
                prevRanges.toArray(removedRangesArray);
                this.mNetd.removeVpnUidRanges(nai.network.netId, removedRangesArray);
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception in updateUids: ");
            sb.append(HW_DEBUGGABLE ? e : "*");
            loge(sb.toString());
        }
    }

    public void handleUpdateLinkProperties(NetworkAgentInfo nai, LinkProperties newLp) {
        if (getNetworkAgentInfoForNetId(nai.network.netId) == nai) {
            newLp.ensureDirectlyConnectedRoutes();
            log("Update of LinkProperties for " + nai.name() + "; created=" + nai.created + "; everConnected=" + nai.everConnected);
            LinkProperties oldLp = nai.linkProperties;
            synchronized (nai) {
                nai.linkProperties = newLp;
            }
            if (nai.everConnected) {
                updateLinkProperties(nai, oldLp);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendUpdatedScoreToFactories(NetworkAgentInfo nai) {
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest nr = nai.requestAt(i);
            if (!nr.isListen()) {
                sendUpdatedScoreToFactories(nr, nai.getCurrentScore());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendUpdatedScoreToFactories(NetworkRequest networkRequest, int score) {
        log("sending new Min Network Score(" + score + "): " + networkRequest.toString());
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            nfi.asyncChannel.sendMessage(536576, score, 0, networkRequest);
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
                    putParcelable(bundle, new NetworkCapabilities(networkAgent.networkCapabilities));
                    putParcelable(bundle, new LinkProperties(networkAgent.linkProperties));
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
    public void teardownUnneededNetwork(NetworkAgentInfo nai) {
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
    public void handleLingerComplete(NetworkAgentInfo oldNetwork) {
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
        log("Switching to new default network: " + newNetwork);
        setupDataActivityTracking(newNetwork);
        try {
            this.mNetd.setDefaultNetId(newNetwork.network.netId);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Exception setting default network :");
            sb.append(HW_DEBUGGABLE ? e : "*");
            loge(sb.toString());
        }
        notifyLockdownVpn(newNetwork);
        handleApplyDefaultProxy(newNetwork.linkProperties.getHttpProxy());
        updateTcpBufferSizes(newNetwork);
        this.mDnsManager.setDefaultDnsSystemProperties(newNetwork.linkProperties.getDnsServers());
        notifyIfacesChangedForNetworkStats();
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

    /* access modifiers changed from: private */
    public void rematchNetworkAndRequests(NetworkAgentInfo newNetwork, ReapUnvalidatedNetworks reapUnvalidatedNetworks, long now) {
        int i;
        Iterator<NetworkAgentInfo> it;
        NetworkCapabilities nc;
        boolean wasBackgroundNetwork;
        boolean isDisabledMobileNetwork;
        Iterator<NetworkRequestInfo> it2;
        ArrayList<NetworkRequestInfo> addedRequests;
        ArrayList<NetworkAgentInfo> affectedNetworks;
        NetworkAgentInfo oldDefaultNetwork;
        NetworkAgentInfo oldDefaultNetwork2;
        NetworkRequestInfo nri;
        NetworkAgentInfo currentNetwork;
        NetworkAgentInfo oldDefaultNetwork3;
        NetworkAgentInfo networkAgentInfo = newNetwork;
        long j = now;
        if (networkAgentInfo.everConnected) {
            boolean keep = newNetwork.isVPN();
            boolean wasBackgroundNetwork2 = newNetwork.isBackgroundNetwork();
            int score = newNetwork.getCurrentScore();
            log("rematching " + newNetwork.name());
            boolean isDisabledMobileNetwork2 = networkAgentInfo.networkInfo.getType() == 0 && !this.mTelephonyManager.getDataEnabled() && !HwFrameworkFactory.getHwInnerTelephonyManager().isVSimEnabled();
            ArrayList<NetworkAgentInfo> affectedNetworks2 = new ArrayList<>();
            ArrayList<NetworkRequestInfo> addedRequests2 = new ArrayList<>();
            NetworkCapabilities nc2 = networkAgentInfo.networkCapabilities;
            log(" network has: " + nc2);
            Iterator<NetworkRequestInfo> it3 = this.mNetworkRequests.values().iterator();
            boolean isNewDefault = false;
            NetworkAgentInfo oldDefaultNetwork4 = null;
            while (it3.hasNext()) {
                NetworkRequestInfo nri2 = it3.next();
                if (!nri2.request.isListen()) {
                    NetworkAgentInfo currentNetwork2 = getNetworkForRequest(nri2.request.requestId);
                    boolean satisfies = networkAgentInfo.satisfies(nri2.request);
                    boolean keep2 = keep;
                    NetworkCapabilities nc3 = nc2;
                    if (this.mDefaultRequest.requestId == nri2.request.requestId && isDisabledMobileNetwork2) {
                        log("mobile net can't satisfy default network request when mobile data disabled");
                        satisfies = false;
                    }
                    if (networkAgentInfo != currentNetwork2 || !satisfies) {
                        if (satisfies) {
                            oldDefaultNetwork3 = oldDefaultNetwork4;
                        } else if (checkNetworkSupportBip(networkAgentInfo, nri2.request)) {
                            oldDefaultNetwork3 = oldDefaultNetwork4;
                        } else {
                            if (networkAgentInfo.isSatisfyingRequest(nri2.request.requestId)) {
                                log("Network " + newNetwork.name() + " stopped satisfying request " + nri2.request.requestId);
                                networkAgentInfo.removeRequest(nri2.request.requestId);
                                if (currentNetwork2 == networkAgentInfo) {
                                    clearNetworkForRequest(nri2.request.requestId);
                                    sendUpdatedScoreToFactories(nri2.request, 0);
                                    oldDefaultNetwork = oldDefaultNetwork4;
                                } else {
                                    String str = TAG;
                                    StringBuilder sb = new StringBuilder();
                                    oldDefaultNetwork = oldDefaultNetwork4;
                                    sb.append("BUG: Removing request ");
                                    sb.append(nri2.request.requestId);
                                    sb.append(" from ");
                                    sb.append(newNetwork.name());
                                    sb.append(" without updating mNetworkForRequestId or factories!");
                                    Slog.wtf(str, sb.toString());
                                }
                                callCallbackForRequest(nri2, networkAgentInfo, 524292, 0);
                            } else {
                                oldDefaultNetwork = oldDefaultNetwork4;
                            }
                            addedRequests = addedRequests2;
                            wasBackgroundNetwork = wasBackgroundNetwork2;
                            it2 = it3;
                            isDisabledMobileNetwork = isDisabledMobileNetwork2;
                            keep = keep2;
                            nc = nc3;
                            oldDefaultNetwork4 = oldDefaultNetwork;
                            affectedNetworks = affectedNetworks2;
                            affectedNetworks2 = affectedNetworks;
                            addedRequests2 = addedRequests;
                            it3 = it2;
                            isDisabledMobileNetwork2 = isDisabledMobileNetwork;
                            wasBackgroundNetwork2 = wasBackgroundNetwork;
                            nc2 = nc;
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("currentScore = ");
                        sb2.append(currentNetwork2 != null ? currentNetwork2.getCurrentScore() : 0);
                        sb2.append(", newScore = ");
                        sb2.append(score);
                        log(sb2.toString());
                        if (currentNetwork2 == null || currentNetwork2.getCurrentScore() < score) {
                            log("rematch for " + newNetwork.name());
                            if (currentNetwork2 != null) {
                                log("   accepting network in place of " + currentNetwork2.name());
                                currentNetwork2.removeRequest(nri2.request.requestId);
                                it2 = it3;
                                nri = nri2;
                                boolean z = satisfies;
                                isDisabledMobileNetwork = isDisabledMobileNetwork2;
                                oldDefaultNetwork2 = oldDefaultNetwork;
                                wasBackgroundNetwork = wasBackgroundNetwork2;
                                nc = nc3;
                                affectedNetworks = affectedNetworks2;
                                addedRequests = addedRequests2;
                                currentNetwork2.lingerRequest(nri2.request, j, (long) this.mLingerDelayMs);
                                currentNetwork = currentNetwork2;
                                affectedNetworks.add(currentNetwork);
                            } else {
                                currentNetwork = currentNetwork2;
                                wasBackgroundNetwork = wasBackgroundNetwork2;
                                it2 = it3;
                                boolean z2 = satisfies;
                                isDisabledMobileNetwork = isDisabledMobileNetwork2;
                                nc = nc3;
                                oldDefaultNetwork2 = oldDefaultNetwork;
                                nri = nri2;
                                affectedNetworks = affectedNetworks2;
                                addedRequests = addedRequests2;
                                log("   accepting network in place of null");
                            }
                            networkAgentInfo.unlingerRequest(nri.request);
                            setNetworkForRequest(nri.request.requestId, networkAgentInfo);
                            if (!networkAgentInfo.addRequest(nri.request)) {
                                Slog.wtf(TAG, "BUG: " + newNetwork.name() + " already has " + nri.request);
                            }
                            addedRequests.add(nri);
                            sendUpdatedScoreToFactories(nri.request, score);
                            if (isDefaultRequest(nri)) {
                                NetworkAgentInfo oldDefaultNetwork5 = currentNetwork;
                                if (currentNetwork != null) {
                                    this.mLingerMonitor.noteLingerDefaultNetwork(currentNetwork, networkAgentInfo);
                                }
                                keep = true;
                                isNewDefault = true;
                                oldDefaultNetwork4 = oldDefaultNetwork5;
                            } else {
                                keep = true;
                                oldDefaultNetwork4 = oldDefaultNetwork2;
                            }
                            affectedNetworks2 = affectedNetworks;
                            addedRequests2 = addedRequests;
                            it3 = it2;
                            isDisabledMobileNetwork2 = isDisabledMobileNetwork;
                            wasBackgroundNetwork2 = wasBackgroundNetwork;
                            nc2 = nc;
                        }
                        addedRequests = addedRequests2;
                        wasBackgroundNetwork = wasBackgroundNetwork2;
                        it2 = it3;
                        isDisabledMobileNetwork = isDisabledMobileNetwork2;
                        keep = keep2;
                        nc = nc3;
                        oldDefaultNetwork4 = oldDefaultNetwork;
                        affectedNetworks = affectedNetworks2;
                        affectedNetworks2 = affectedNetworks;
                        addedRequests2 = addedRequests;
                        it3 = it2;
                        isDisabledMobileNetwork2 = isDisabledMobileNetwork;
                        wasBackgroundNetwork2 = wasBackgroundNetwork;
                        nc2 = nc;
                    } else {
                        log("Network " + newNetwork.name() + " was already satisfying request " + nri2.request.requestId + ". No change.");
                        keep = true;
                        nc2 = nc3;
                    }
                }
            }
            boolean keep3 = keep;
            NetworkCapabilities nc4 = nc2;
            ArrayList<NetworkRequestInfo> addedRequests3 = addedRequests2;
            boolean wasBackgroundNetwork3 = wasBackgroundNetwork2;
            boolean z3 = isDisabledMobileNetwork2;
            NetworkAgentInfo oldDefaultNetwork6 = oldDefaultNetwork4;
            ArrayList<NetworkAgentInfo> affectedNetworks3 = affectedNetworks2;
            if (NetworkFactory.isDualCellDataEnable()) {
                log("isDualCellDataEnable is true so keep is true");
                keep3 = true;
            }
            if (isNewDefault) {
                if ((ENABLE_WIFI_LTE_CE && IS_SUPPORT_LINGER_DELAY) || this.mCust.mMobileDataAlwaysOnCust) {
                    updateDefaultNetworkRouting(oldDefaultNetwork6, networkAgentInfo);
                }
                makeDefault(newNetwork);
                metricsLogger().defaultNetworkMetrics().logDefaultNetworkEvent(j, networkAgentInfo, oldDefaultNetwork6);
                scheduleReleaseNetworkTransitionWakelock();
            }
            NetworkCapabilities nc5 = nc4;
            if (!networkAgentInfo.networkCapabilities.equalRequestableCapabilities(nc5)) {
                Slog.wtf(TAG, String.format("BUG: %s changed requestable capabilities during rematch: %s -> %s", new Object[]{newNetwork.name(), nc5, networkAgentInfo.networkCapabilities}));
            }
            if (newNetwork.getCurrentScore() != score) {
                Slog.wtf(TAG, String.format("BUG: %s changed score during rematch: %d -> %d", new Object[]{newNetwork.name(), Integer.valueOf(score), Integer.valueOf(newNetwork.getCurrentScore())}));
            }
            if (wasBackgroundNetwork3 != newNetwork.isBackgroundNetwork()) {
                updateCapabilities(score, networkAgentInfo, networkAgentInfo.networkCapabilities);
                i = 0;
            } else {
                i = 0;
                processListenRequests(networkAgentInfo, false);
            }
            Iterator<NetworkRequestInfo> it4 = addedRequests3.iterator();
            while (it4.hasNext()) {
                notifyNetworkAvailable(networkAgentInfo, it4.next());
            }
            Iterator<NetworkAgentInfo> it5 = affectedNetworks3.iterator();
            while (it5.hasNext()) {
                NetworkAgentInfo nai = it5.next();
                if (IS_SUPPORT_LINGER_DELAY || networkAgentInfo.networkInfo.getType() != 1 || nai.networkInfo.getType() != 0 || !ENABLE_WIFI_LTE_CE) {
                    updateLingerState(nai, j);
                } else {
                    nai.clearLingerState();
                    nai.unlinger();
                    log("unlinger mobile network");
                }
            }
            updateLingerState(networkAgentInfo, j);
            if (isNewDefault) {
                if (oldDefaultNetwork6 != null) {
                    this.mLegacyTypeTracker.remove(oldDefaultNetwork6.networkInfo.getType(), oldDefaultNetwork6, true);
                }
                this.mDefaultInetConditionPublished = networkAgentInfo.lastValidated ? 100 : i;
                this.mLegacyTypeTracker.add(networkAgentInfo.networkInfo.getType(), networkAgentInfo);
                notifyLockdownVpn(newNetwork);
            }
            if (keep3) {
                try {
                    IBatteryStats bs = BatteryStatsService.getService();
                    int type = networkAgentInfo.networkInfo.getType();
                    bs.noteNetworkInterfaceType(networkAgentInfo.linkProperties.getInterfaceName(), type);
                    for (LinkProperties stacked : networkAgentInfo.linkProperties.getStackedLinks()) {
                        bs.noteNetworkInterfaceType(stacked.getInterfaceName(), type);
                    }
                } catch (RemoteException e) {
                }
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= newNetwork.numNetworkRequests()) {
                        break;
                    }
                    NetworkRequest nr = networkAgentInfo.requestAt(i3);
                    if (nr.legacyType != -1 && nr.isRequest()) {
                        this.mLegacyTypeTracker.add(nr.legacyType, networkAgentInfo);
                    }
                    i2 = i3 + 1;
                }
                if (newNetwork.isVPN()) {
                    this.mLegacyTypeTracker.add(17, networkAgentInfo);
                }
            }
            if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                Iterator<NetworkAgentInfo> it6 = this.mNetworkAgentInfos.values().iterator();
                while (it6.hasNext()) {
                    NetworkAgentInfo nai2 = it6.next();
                    if (unneeded(nai2, UnneededFor.TEARDOWN)) {
                        if (nai2.getLingerExpiry() > 0) {
                            updateLingerState(nai2, j);
                        } else {
                            log("Reaping " + nai2.name());
                            if (ENABLE_WIFI_LTE_CE) {
                                boolean isWifiConnected = false;
                                ConnectivityManager cm = ConnectivityManager.from(this.mContext);
                                if (cm != null) {
                                    it = it6;
                                    NetworkInfo networkInfo = cm.getNetworkInfo(1);
                                    if (networkInfo != null) {
                                        isWifiConnected = networkInfo.isConnected();
                                    }
                                } else {
                                    it = it6;
                                }
                                log("isWifiConnected==" + isWifiConnected);
                                if (!isWifiConnected || nai2.networkInfo.getType() != 0) {
                                    teardownUnneededNetwork(nai2);
                                }
                            } else {
                                it = it6;
                                teardownUnneededNetwork(nai2);
                            }
                            it6 = it;
                        }
                    }
                    it = it6;
                    it6 = it;
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
    public void updateInetCondition(NetworkAgentInfo nai) {
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
    public void updateNetworkInfo(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        int i;
        boolean z;
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
            try {
                if (networkAgent.isVPN()) {
                    INetworkManagementService iNetworkManagementService = this.mNetd;
                    int i2 = networkAgent.network.netId;
                    boolean z2 = !networkAgent.linkProperties.getDnsServers().isEmpty();
                    if (networkAgent.networkMisc != null) {
                        if (networkAgent.networkMisc.allowBypass) {
                            z = false;
                            iNetworkManagementService.createVirtualNetwork(i2, z2, z);
                        }
                    }
                    z = true;
                    iNetworkManagementService.createVirtualNetwork(i2, z2, z);
                } else {
                    this.mNetd.createPhysicalNetwork(networkAgent.network.netId, getNetworkPermission(networkAgent.networkCapabilities));
                }
                networkAgent.created = true;
            } catch (Exception e) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Error creating network ");
                sb2.append(networkAgent.network.netId);
                sb2.append(": ");
                sb2.append(HW_DEBUGGABLE ? e.getMessage() : "*");
                loge(sb2.toString());
                return;
            }
        }
        if (!networkAgent.everConnected && state == NetworkInfo.State.CONNECTED) {
            networkAgent.everConnected = true;
            handlePerNetworkPrivateDnsConfig(networkAgent, this.mDnsManager.getPrivateDnsConfig());
            updateLinkProperties(networkAgent, null);
            notifyIfacesChangedForNetworkStats();
            networkAgent.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_CONNECTED);
            scheduleUnvalidatedPrompt(networkAgent);
            if (networkAgent.isVPN()) {
                setVpnSettingValue(true);
                synchronized (this.mProxyLock) {
                    if (!this.mDefaultProxyDisabled) {
                        this.mDefaultProxyDisabled = true;
                        if (this.mGlobalProxy == null && this.mDefaultProxy != null) {
                            sendProxyBroadcast(null);
                        }
                    }
                }
            }
            updateSignalStrengthThresholds(networkAgent, "CONNECT", null);
            rematchNetworkAndRequests(networkAgent, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
            notifyNetworkCallbacks(networkAgent, 524289);
        } else if (state == NetworkInfo.State.DISCONNECTED) {
            networkAgent.asyncChannel.disconnect();
            if (networkAgent.isVPN()) {
                setVpnSettingValue(false);
                synchronized (this.mProxyLock) {
                    if (this.mDefaultProxyDisabled) {
                        this.mDefaultProxyDisabled = false;
                        if (this.mGlobalProxy == null && this.mDefaultProxy != null) {
                            sendProxyBroadcast(this.mDefaultProxy);
                        }
                    }
                }
                updateUids(networkAgent, networkAgent.networkCapabilities, null);
            }
            disconnectAndDestroyNetwork(networkAgent);
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
        updataNetworkAgentInfoForHicure(networkAgent);
    }

    /* access modifiers changed from: private */
    public void updateNetworkScore(NetworkAgentInfo nai, int score) {
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
        } else {
            callCallbackForRequest(nri, nai, 524290, 0);
        }
    }

    /* access modifiers changed from: private */
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
            if (nri.mPendingIntent == null) {
                callCallbackForRequest(nri, networkAgent, notifyType, arg1);
            } else {
                sendPendingIntentForRequest(nri, networkAgent, notifyType);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType) {
        notifyNetworkCallbacks(networkAgent, notifyType, 0);
    }

    private Network[] getDefaultNetworks() {
        ArrayList<Network> defaultNetworks = new ArrayList<>();
        NetworkAgentInfo defaultNetwork = getDefaultNetwork();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.everConnected && (nai == defaultNetwork || nai.isVPN())) {
                defaultNetworks.add(nai.network);
            }
        }
        return (Network[]) defaultNetworks.toArray(new Network[0]);
    }

    /* access modifiers changed from: private */
    public void notifyIfacesChangedForNetworkStats() {
        try {
            this.mStatsService.forceUpdateIfaces(getDefaultNetworks());
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
                public final void run() {
                    ConnectivityService.this.notifyIfacesChangedForNetworkStats();
                }
            });
        }
        return success;
    }

    public String getCaptivePortalServerUrl() {
        enforceConnectivityInternalPermission();
        return NetworkMonitor.getCaptivePortalServerHttpUrl(this.mContext);
    }

    public void startNattKeepalive(Network network, int intervalSeconds, Messenger messenger, IBinder binder, String srcAddr, int srcPort, String dstAddr) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), intervalSeconds, messenger, binder, srcAddr, srcPort, dstAddr, 4500);
    }

    public void stopKeepalive(Network network, int slot) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(528396, slot, 0, network));
    }

    /* JADX INFO: finally extract failed */
    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int userId = UserHandle.getCallingUserId();
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
                        setAlwaysOnVpnPackage(userId, null, false);
                        setVpnPackageAuthorization(alwaysOnPackage, userId, false);
                    }
                    if (this.mLockdownEnabled && userId == 0) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            this.mKeyStore.delete("LOCKDOWN_VPN");
                            this.mLockdownEnabled = false;
                            setLockdownTracker(null);
                            Binder.restoreCallingIdentity(ident);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
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
            Settings.Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", null);
            Settings.Global.putString(this.mContext.getContentResolver(), "private_dns_mode", null);
            Settings.Global.putString(this.mContext.getContentResolver(), "private_dns_specifier", null);
        }
    }

    public byte[] getNetworkWatchlistConfigHash() {
        NetworkWatchlistManager nwm = (NetworkWatchlistManager) this.mContext.getSystemService(NetworkWatchlistManager.class);
        if (nwm != null) {
            return nwm.getWatchlistConfigHash();
        }
        loge("Unable to get NetworkWatchlistManager");
        return null;
    }

    @VisibleForTesting
    public NetworkMonitor createNetworkMonitor(Context context, Handler handler, NetworkAgentInfo nai, NetworkRequest defaultRequest) {
        return new NetworkMonitor(context, handler, nai, defaultRequest);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public MultinetworkPolicyTracker createMultinetworkPolicyTracker(Context c, Handler h, Runnable r) {
        return new MultinetworkPolicyTracker(c, h, r);
    }

    @VisibleForTesting
    public WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int cmd, Object obj) {
        WakeupMessage wakeupMessage = new WakeupMessage(c, h, s, cmd, 0, 0, obj);
        return wakeupMessage;
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
        return b;
    }

    /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
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
    public void updataNetworkAgentInfoForHicure(NetworkAgentInfo nai) {
    }

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

    /* access modifiers changed from: protected */
    public void updateLinkPropertiesEx(NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        updateLinkProperties(networkAgent, oldLp);
    }

    public ConnectivityService getService() {
        return this;
    }

    public HashMap<Messenger, NetworkAgentInfo> getNetworkAgentInfos() {
        return this.mNetworkAgentInfos;
    }

    public String getDescriptor() {
        return DESCRIPTOR;
    }

    public int getCodeRemoveLegacyrouteToHost() {
        return CODE_REMOVE_LEGACYROUTE_TO_HOST;
    }

    public NetworkAgentInfo getHwNetworkForType(int type) {
        return this.mLegacyTypeTracker.getNetworkForType(type);
    }

    public INetworkManagementService getNetd() {
        return this.mNetd;
    }

    public void hwUpdateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        updateLinkProperties(networkAgent, oldLp);
    }
}
