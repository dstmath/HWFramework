package com.android.server;

import android.app.BroadcastOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.LinkProperties.CompareResult;
import android.net.MatchAllNetworkSpecifier;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkMisc;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Type;
import android.net.NetworkSpecifier;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.net.metrics.DefaultNetworkEvent;
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
import android.os.IBinder.DeathRecipient;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.security.KeyStore;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.LocalLog.ReadOnlyLocalLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.WakeupMessage;
import com.android.internal.util.XmlUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.audio.AudioService;
import com.android.server.connectivity.DataConnectionStats;
import com.android.server.connectivity.KeepaliveTracker;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.connectivity.MockableSystemProperties;
import com.android.server.connectivity.Nat464Xlat;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkDiagnostics;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.NetworkNotificationManager;
import com.android.server.connectivity.NetworkNotificationManager.NotificationType;
import com.android.server.connectivity.PacManager;
import com.android.server.connectivity.PermissionMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.Vpn;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.policy.PhoneWindowManager;
import com.google.android.collect.Lists;
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
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConnectivityService extends AbstractConnectivityService implements OnFinished {
    private static final /* synthetic */ int[] -com-android-server-ConnectivityService$UnneededForSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues = null;
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
    private static final int EVENT_SET_ACCEPT_UNVALIDATED = 28;
    private static final int EVENT_SET_AVOID_UNVALIDATED = 35;
    private static final int EVENT_SYSTEM_READY = 25;
    private static final int EVENT_TIMEOUT_NETWORK_REQUEST = 20;
    private static final int EVENT_UNREGISTER_NETWORK_FACTORY = 23;
    private static final int INET_CONDITION_LOG_MAX_SIZE = 15;
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final boolean LOGD_BLOCKED_NETWORKINFO = true;
    private static final boolean LOGD_RULES = false;
    private static final int MAX_NETWORK_INFO_LOGS = 100;
    private static final int MAX_NETWORK_REQUESTS_PER_UID = 100;
    private static final int MAX_NETWORK_REQUEST_LOGS = 20;
    private static final int MAX_NET_ID = 65535;
    private static final int MAX_VALIDATION_LOGS = 10;
    private static final int MIN_NET_ID = 100;
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    private static final int PROMPT_UNVALIDATED_DELAY_MS = 8000;
    private static final String PROVISIONING_URL_PATH = "/data/misc/radio/provisioning_urls.xml";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 60000;
    private static final boolean SAMPLE_DBG = false;
    public static final String SHORT_ARG = "--short";
    private static final String TAG = ConnectivityService.class.getSimpleName();
    private static final String TAG_PROVISIONING_URL = "provisioningUrl";
    private static final String TAG_PROVISIONING_URLS = "provisioningUrls";
    private static final boolean VDBG = true;
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(new Class[]{AsyncChannel.class, ConnectivityService.class, NetworkAgent.class, NetworkAgentInfo.class});
    private static ConnectivityService sServiceInstance;
    private RouteInfo mBestLegacyRoute;
    @GuardedBy("mBlockedAppUids")
    private final HashSet<Integer> mBlockedAppUids;
    private final Context mContext;
    private String mCurrentTcpBufferSizes;
    private INetworkManagementEventObserver mDataActivityObserver;
    private DataConnectionStats mDataConnectionStats;
    private int mDefaultInetConditionPublished;
    private final NetworkRequest mDefaultMobileDataRequest;
    private volatile ProxyInfo mDefaultProxy;
    private boolean mDefaultProxyDisabled;
    private final NetworkRequest mDefaultRequest;
    private ProxyInfo mGlobalProxy;
    private final InternalHandler mHandler;
    protected final HandlerThread mHandlerThread;
    private ArrayList mInetLog;
    private Intent mInitialBroadcast;
    private KeepaliveTracker mKeepaliveTracker;
    private KeyStore mKeyStore;
    private int mLegacyRouteNetId;
    private int mLegacyRouteUid;
    private LegacyTypeTracker mLegacyTypeTracker;
    protected int mLingerDelayMs;
    private LingerMonitor mLingerMonitor;
    private boolean mLockdownEnabled;
    private LockdownVpnTracker mLockdownTracker;
    private final IpConnectivityLog mMetricsLog;
    final MultinetworkPolicyTracker mMultinetworkPolicyTracker;
    NetworkConfig[] mNetConfigs;
    @GuardedBy("mNetworkForNetId")
    private final SparseBooleanArray mNetIdInUse;
    private WakeLock mNetTransitionWakeLock;
    private String mNetTransitionWakeLockCausedBy;
    private int mNetTransitionWakeLockSerialNumber;
    private int mNetTransitionWakeLockTimeout;
    private INetworkManagementService mNetd;
    private final HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos;
    protected final HashMap<Messenger, NetworkFactoryInfo> mNetworkFactoryInfos;
    @GuardedBy("mNetworkForNetId")
    private final SparseArray<NetworkAgentInfo> mNetworkForNetId;
    protected final SparseArray<NetworkAgentInfo> mNetworkForRequestId;
    private final LocalLog mNetworkInfoBlockingLogs;
    private int mNetworkPreference;
    protected final LocalLog mNetworkRequestInfoLogs;
    protected final HashMap<NetworkRequest, NetworkRequestInfo> mNetworkRequests;
    int mNetworksDefined;
    private int mNextNetId;
    private int mNextNetworkRequestId;
    private NetworkNotificationManager mNotifier;
    private int mNumDnsEntries;
    private NetworkInfo mP2pNetworkInfo;
    private PacManager mPacManager;
    private final WakeLock mPendingIntentWakeLock;
    private final PermissionMonitor mPermissionMonitor;
    private final INetworkPolicyListener mPolicyListener;
    private INetworkPolicyManager mPolicyManager;
    private NetworkPolicyManagerInternal mPolicyManagerInternal;
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
    private Tethering mTethering;
    private final NetworkStateTrackerHandler mTrackerHandler;
    @GuardedBy("mUidToNetworkRequestCount")
    private final SparseIntArray mUidToNetworkRequestCount;
    private BroadcastReceiver mUserIntentReceiver;
    private UserManager mUserManager;
    private BroadcastReceiver mUserPresentReceiver;
    private final ArrayDeque<ValidationLog> mValidationLogs;
    @GuardedBy("mVpns")
    private final SparseArray<Vpn> mVpns;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:12:0x0036, code:
            if (r13.what != 24) goto L_0x0044;
     */
        /* JADX WARNING: Missing block: B:13:0x0038, code:
            com.android.server.ConnectivityService.-wrap19("Failed to find a new network - expiring NetTransition Wakelock");
     */
        /* JADX WARNING: Missing block: B:18:0x0044, code:
            r6 = new java.lang.StringBuilder().append("NetTransition Wakelock (");
     */
        /* JADX WARNING: Missing block: B:19:0x0050, code:
            if (r0 != null) goto L_0x0055;
     */
        /* JADX WARNING: Missing block: B:20:0x0052, code:
            r0 = com.android.server.UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
     */
        /* JADX WARNING: Missing block: B:21:0x0055, code:
            com.android.server.ConnectivityService.-wrap19(r6.append(r0).append(" cleared because we found a replacement network").toString());
     */
        /* JADX WARNING: Missing block: B:50:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:51:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:67:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 8:
                case 24:
                    synchronized (ConnectivityService.this) {
                        if (msg.arg1 == ConnectivityService.this.mNetTransitionWakeLockSerialNumber && ConnectivityService.this.mNetTransitionWakeLock.isHeld()) {
                            ConnectivityService.this.mNetTransitionWakeLock.release();
                            String causedBy = ConnectivityService.this.mNetTransitionWakeLockCausedBy;
                            break;
                        }
                    }
                    break;
                case 9:
                    ConnectivityService.this.handleDeprecatedGlobalHttpProxy();
                    return;
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
                    ConnectivityService.this.handleTimedOutNetworkRequest(msg.obj);
                    return;
                case 22:
                    ConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.arg1);
                    return;
                case 23:
                    ConnectivityService.this.handleUnregisterNetworkFactory((Messenger) msg.obj);
                    return;
                case 25:
                    for (NetworkAgentInfo nai : ConnectivityService.this.mNetworkAgentInfos.values()) {
                        nai.networkMonitor.systemReady = true;
                    }
                    return;
                case 26:
                case 31:
                    ConnectivityService.this.handleRegisterNetworkRequestWithIntent(msg);
                    return;
                case ConnectivityService.EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT /*27*/:
                    ConnectivityService.this.handleReleaseNetworkRequestWithIntent((PendingIntent) msg.obj, msg.arg1);
                    return;
                case 28:
                    ConnectivityService connectivityService = ConnectivityService.this;
                    Network network = (Network) msg.obj;
                    boolean z2 = msg.arg1 != 0;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    connectivityService.handleSetAcceptUnvalidated(network, z2, z);
                    return;
                case 30:
                    ConnectivityService.this.handleMobileDataAlwaysOn();
                    return;
                case 35:
                    ConnectivityService.this.handleSetAvoidUnvalidated((Network) msg.obj);
                    return;
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

    private class LegacyTypeTracker {
        private static final boolean DBG = true;
        private static final boolean VDBG = false;
        private final ArrayList<NetworkAgentInfo>[] mTypeLists = new ArrayList[48];

        public void addSupportedType(int type) {
            if (this.mTypeLists[type] != null) {
                throw new IllegalStateException("legacy list for type " + type + "already initialized");
            }
            this.mTypeLists[type] = new ArrayList();
        }

        public boolean isTypeSupported(int type) {
            return ConnectivityManager.isNetworkTypeValid(type) && this.mTypeLists[type] != null;
        }

        /* JADX WARNING: Missing block: B:12:0x0024, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public NetworkAgentInfo getNetworkForType(int type) {
            synchronized (this.mTypeLists) {
                if (!isTypeSupported(type) || (this.mTypeLists[type].isEmpty() ^ 1) == 0) {
                } else {
                    NetworkAgentInfo networkAgentInfo = (NetworkAgentInfo) this.mTypeLists[type].get(0);
                    return networkAgentInfo;
                }
            }
        }

        private void maybeLogBroadcast(NetworkAgentInfo nai, DetailedState state, int type, boolean isDefaultNetwork) {
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
                        maybeLogBroadcast(nai, DetailedState.CONNECTED, type, isDefaultNetwork);
                        ConnectivityService.this.sendLegacyNetworkBroadcast(nai, DetailedState.CONNECTED, type);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:13:0x0024, code:
            r2 = android.net.NetworkInfo.DetailedState.DISCONNECTED;
     */
        /* JADX WARNING: Missing block: B:14:0x0026, code:
            if (r3 != false) goto L_0x002a;
     */
        /* JADX WARNING: Missing block: B:15:0x0028, code:
            if (r10 == false) goto L_0x0032;
     */
        /* JADX WARNING: Missing block: B:16:0x002a, code:
            maybeLogBroadcast(r9, r2, r8, r10);
            com.android.server.ConnectivityService.-wrap28(r7.this$0, r9, r2, r8);
     */
        /* JADX WARNING: Missing block: B:18:0x0036, code:
            if (r0.isEmpty() != false) goto L_0x006c;
     */
        /* JADX WARNING: Missing block: B:19:0x0038, code:
            if (r3 == false) goto L_0x006c;
     */
        /* JADX WARNING: Missing block: B:20:0x003a, code:
            com.android.server.ConnectivityService.-wrap19("Other network available for type " + r8 + ", sending connected broadcast");
            r1 = (com.android.server.connectivity.NetworkAgentInfo) r0.get(0);
            maybeLogBroadcast(r1, r2, r8, com.android.server.ConnectivityService.-wrap0(r7.this$0, r1));
            com.android.server.ConnectivityService.-wrap28(r7.this$0, r1, r2, r8);
     */
        /* JADX WARNING: Missing block: B:21:0x006c, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void remove(int type, NetworkAgentInfo nai, boolean wasDefault) {
            ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
            if (list != null && !list.isEmpty()) {
                boolean wasFirstNetwork = ((NetworkAgentInfo) list.get(0)).equals(nai);
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
            DetailedState state = nai.networkInfo.getDetailedState();
            for (int type = 0; type < this.mTypeLists.length; type++) {
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                boolean contains = list != null ? list.contains(nai) : false;
                boolean isFirst = contains && nai == list.get(0);
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
                state = nai.networkInfo.getState() + "/" + nai.networkInfo.getDetailedState();
            } else {
                state = "???/???";
            }
            return name + " " + state;
        }

        public void dump(IndentingPrintWriter pw) {
            int type;
            pw.println("mLegacyTypeTracker:");
            pw.increaseIndent();
            pw.print("Supported types:");
            for (type = 0; type < this.mTypeLists.length; type++) {
                if (this.mTypeLists[type] != null) {
                    pw.print(" " + type);
                }
            }
            pw.println();
            pw.println("Current state:");
            pw.increaseIndent();
            synchronized (this.mTypeLists) {
                type = 0;
                while (type < this.mTypeLists.length) {
                    if (!(this.mTypeLists[type] == null || this.mTypeLists[type].isEmpty())) {
                        for (NetworkAgentInfo nai : this.mTypeLists[type]) {
                            pw.println(type + " " + naiToString(nai));
                        }
                    }
                    type++;
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

        public NetworkFactoryInfo(String name, Messenger messenger, AsyncChannel asyncChannel) {
            this.name = name;
            this.messenger = messenger;
            this.asyncChannel = asyncChannel;
        }
    }

    protected class NetworkRequestInfo implements DeathRecipient {
        NetworkRequest clientRequest = null;
        private final IBinder mBinder;
        final PendingIntent mPendingIntent;
        boolean mPendingIntentSent;
        final int mPid;
        DomainPreferType mPreferType = null;
        final int mUid;
        final Messenger messenger;
        final NetworkRequest request;

        NetworkRequestInfo(NetworkRequest r, PendingIntent pi) {
            this.request = r;
            ConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mPendingIntent = pi;
            this.messenger = null;
            this.mBinder = null;
            this.mPid = ConnectivityService.getCallingPid();
            this.mUid = ConnectivityService.getCallingUid();
            enforceRequestCountLimit();
        }

        NetworkRequestInfo(Messenger m, NetworkRequest r, IBinder binder) {
            this.messenger = m;
            this.request = r;
            ConnectivityService.this.ensureNetworkRequestHasType(this.request);
            this.mBinder = binder;
            this.mPid = ConnectivityService.getCallingPid();
            this.mUid = ConnectivityService.getCallingUid();
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
                if (networkRequests >= 100) {
                    throw new IllegalArgumentException("Too many NetworkRequests filed");
                }
                ConnectivityService.this.mUidToNetworkRequestCount.put(this.mUid, networkRequests);
            }
        }

        void unlinkDeathRecipient() {
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
            }
        }

        public void binderDied() {
            ConnectivityService.log("ConnectivityService NetworkRequestInfo binderDied(" + this.request + ", " + this.mBinder + ")");
            ConnectivityService.this.releaseNetworkRequest(this.request);
        }

        public String toString() {
            return "uid/pid:" + this.mUid + "/" + this.mPid + " " + this.request + (this.mPendingIntent == null ? "" : " to trigger " + this.mPendingIntent);
        }
    }

    private class NetworkStateTrackerHandler extends Handler {
        public NetworkStateTrackerHandler(Looper looper) {
            super(looper);
        }

        private boolean maybeHandleAsyncChannelMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    ConnectivityService.this.handleAsyncChannelHalfConnect(msg);
                    break;
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
            return true;
        }

        private void maybeHandleNetworkAgentMessage(Message msg) {
            NetworkAgentInfo nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
            if (nai == null) {
                ConnectivityService.log(String.format("%s from unknown NetworkAgent", new Object[]{(String) ConnectivityService.sMagicDecoderRing.get(msg.what, Integer.toString(msg.what))}));
                return;
            }
            switch (msg.what) {
                case 528385:
                    ConnectivityService.this.updateNetworkInfo(nai, msg.obj);
                    break;
                case 528386:
                    NetworkCapabilities networkCapabilities = msg.obj;
                    if (networkCapabilities.hasCapability(17) || networkCapabilities.hasCapability(16) || networkCapabilities.hasCapability(18)) {
                        Slog.wtf(ConnectivityService.TAG, "BUG: " + nai + " has CS-managed capability.");
                    }
                    ConnectivityService.this.updateCapabilities(nai.getCurrentScore(), nai, networkCapabilities);
                    break;
                case 528387:
                    ConnectivityService.log("Update of LinkProperties for " + nai.name() + "; created=" + nai.created + "; everConnected=" + nai.everConnected);
                    LinkProperties oldLp = nai.linkProperties;
                    synchronized (nai) {
                        nai.linkProperties = (LinkProperties) msg.obj;
                    }
                    if (nai.everConnected) {
                        ConnectivityService.this.updateLinkProperties(nai, oldLp);
                        break;
                    }
                    break;
                case 528388:
                    Integer score = msg.obj;
                    if (score != null) {
                        ConnectivityService.this.updateNetworkScore(nai, score.intValue());
                        break;
                    }
                    break;
                case 528389:
                    try {
                        ConnectivityService.this.mNetd.addVpnUidRanges(nai.network.netId, (UidRange[]) msg.obj);
                        break;
                    } catch (Exception e) {
                        ConnectivityService.loge("Exception in addVpnUidRanges: " + e);
                        break;
                    }
                case 528390:
                    try {
                        ConnectivityService.this.mNetd.removeVpnUidRanges(nai.network.netId, (UidRange[]) msg.obj);
                        break;
                    } catch (Exception e2) {
                        ConnectivityService.loge("Exception in removeVpnUidRanges: " + e2);
                        break;
                    }
                case 528392:
                    if (nai.everConnected && (nai.networkMisc.explicitlySelected ^ 1) != 0) {
                        ConnectivityService.loge("ERROR: already-connected network explicitly selected.");
                    }
                    nai.networkMisc.explicitlySelected = true;
                    nai.networkMisc.acceptUnvalidated = ((Boolean) msg.obj).booleanValue();
                    break;
                case 528397:
                    ConnectivityService.this.mKeepaliveTracker.handleEventPacketKeepalive(nai, msg);
                    break;
                case AbstractConnectivityService.EVENT_SET_EXPLICITLY_UNSELECTED /*528585*/:
                    ConnectivityService.this.setExplicitlyUnselected((NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo));
                    break;
                case AbstractConnectivityService.EVENT_UPDATE_NETWORK_CONCURRENTLY /*528586*/:
                    ConnectivityService.this.updateNetworkConcurrently((NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo), (NetworkInfo) msg.obj);
                    break;
                case AbstractConnectivityService.EVENT_TRIGGER_ROAMING_NETWORK_MONITOR /*528587*/:
                    ConnectivityService.this.triggerRoamingNetworkMonitor((NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo));
                    break;
            }
        }

        private boolean maybeHandleNetworkMonitorMessage(Message msg) {
            NetworkAgentInfo nai;
            int oldScore;
            switch (msg.what) {
                case NetworkMonitor.EVENT_NETWORK_TESTED /*532482*/:
                    synchronized (ConnectivityService.this.mNetworkForNetId) {
                        nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkForNetId.get(msg.arg2);
                    }
                    if (nai != null) {
                        String str;
                        boolean valid = msg.arg1 != 0 ? msg.arg1 == 2 : true;
                        boolean wasValidated = nai.lastValidated;
                        StringBuilder append = new StringBuilder().append(nai.name()).append(" validation ").append(valid ? "passed" : "failed");
                        if (msg.obj == null) {
                            str = "";
                        } else {
                            str = " with redirect to " + ((String) msg.obj);
                        }
                        ConnectivityService.log(append.append(str).toString());
                        if (valid != nai.lastValidated) {
                            oldScore = nai.getCurrentScore();
                            nai.lastValidated = valid;
                            nai.everValidated |= valid;
                            ConnectivityService.this.updateCapabilities(oldScore, nai, nai.networkCapabilities);
                            if (oldScore != nai.getCurrentScore()) {
                                ConnectivityService.this.sendUpdatedScoreToFactories(nai);
                            }
                        }
                        ConnectivityService.this.updateInetCondition(nai);
                        if (!ConnectivityService.this.reportPortalNetwork(nai, msg.arg1)) {
                            int i;
                            Bundle redirectUrlBundle = new Bundle();
                            redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, (String) msg.obj);
                            AsyncChannel asyncChannel = nai.asyncChannel;
                            if (valid) {
                                i = 1;
                            } else {
                                i = 2;
                            }
                            asyncChannel.sendMessage(528391, i, 0, redirectUrlBundle);
                            if (wasValidated && (nai.lastValidated ^ 1) != 0) {
                                ConnectivityService.this.handleNetworkUnvalidated(nai);
                                break;
                            }
                        }
                    }
                    break;
                case NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION /*532490*/:
                    int netId = msg.arg2;
                    boolean visible = msg.arg1 != 0;
                    synchronized (ConnectivityService.this.mNetworkForNetId) {
                        nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkForNetId.get(netId);
                    }
                    if (!(nai == null || visible == nai.lastCaptivePortalDetected)) {
                        oldScore = nai.getCurrentScore();
                        nai.lastCaptivePortalDetected = visible;
                        nai.everCaptivePortalDetected |= visible;
                        if (nai.lastCaptivePortalDetected && 2 == getCaptivePortalMode()) {
                            ConnectivityService.log("Avoiding captive portal network: " + nai.name());
                            nai.asyncChannel.sendMessage(528399);
                            ConnectivityService.this.teardownUnneededNetwork(nai);
                            break;
                        }
                        ConnectivityService.this.updateCapabilities(oldScore, nai, nai.networkCapabilities);
                    }
                    if (visible) {
                        if (nai != null) {
                            if (!nai.networkMisc.provisioningNotificationDisabled) {
                                ConnectivityService.this.mNotifier.showNotification(netId, NotificationType.SIGN_IN, nai, null, (PendingIntent) msg.obj, nai.networkMisc.explicitlySelected);
                                break;
                            }
                        }
                        ConnectivityService.loge("EVENT_PROVISIONING_NOTIFICATION from unknown NetworkMonitor");
                        break;
                    }
                    ConnectivityService.this.mNotifier.clearNotification(netId);
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }

        private int getCaptivePortalMode() {
            return Global.getInt(ConnectivityService.this.mContext.getContentResolver(), "captive_portal_mode", 1);
        }

        private boolean maybeHandleNetworkAgentInfoMessage(Message msg) {
            NetworkAgentInfo nai;
            switch (msg.what) {
                case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE /*1001*/:
                    nai = msg.obj;
                    if (nai != null && ConnectivityService.this.isLiveNetworkAgent(nai, msg.what)) {
                        ConnectivityService.this.handleLingerComplete(nai);
                        break;
                    }
                case 528485:
                    nai = (NetworkAgentInfo) ConnectivityService.this.mNetworkAgentInfos.get(msg.replyTo);
                    if (nai != null) {
                        ConnectivityService.this.rematchNetworkAndRequests(nai, ReapUnvalidatedNetworks.DONT_REAP, SystemClock.elapsedRealtime());
                        break;
                    }
                    ConnectivityService.loge("EVENT_REMATCH_NETWORK_AND_REQUESTS from unknown NetworkAgent");
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void handleMessage(Message msg) {
            if (!maybeHandleAsyncChannelMessage(msg) && (maybeHandleNetworkMonitorMessage(msg) ^ 1) != 0 && (maybeHandleNetworkAgentInfoMessage(msg) ^ 1) != 0) {
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
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        public void onChange(boolean selfChange) {
            Slog.wtf(ConnectivityService.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = (Integer) this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            } else {
                ConnectivityService.loge("No matching event to send for URI=" + uri);
            }
        }
    }

    private enum UnneededFor {
        LINGER,
        TEARDOWN
    }

    private static class ValidationLog {
        final ReadOnlyLocalLog mLog;
        final Network mNetwork;
        final String mNetworkExtraInfo;

        ValidationLog(Network network, String networkExtraInfo, ReadOnlyLocalLog log) {
            this.mNetwork = network;
            this.mNetworkExtraInfo = networkExtraInfo;
            this.mLog = log;
        }
    }

    private static /* synthetic */ int[] -getcom-android-server-ConnectivityService$UnneededForSwitchesValues() {
        if (-com-android-server-ConnectivityService$UnneededForSwitchesValues != null) {
            return -com-android-server-ConnectivityService$UnneededForSwitchesValues;
        }
        int[] iArr = new int[UnneededFor.values().length];
        try {
            iArr[UnneededFor.LINGER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[UnneededFor.TEARDOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-android-server-ConnectivityService$UnneededForSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues() {
        if (-com-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues != null) {
            return -com-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues;
        }
        int[] iArr = new int[NotificationType.values().length];
        try {
            iArr[NotificationType.LOST_INTERNET.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[NotificationType.NETWORK_SWITCH.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[NotificationType.NO_INTERNET.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[NotificationType.SIGN_IN.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues = iArr;
        return iArr;
    }

    private void addValidationLogs(ReadOnlyLocalLog log, Network network, String networkExtraInfo) {
        synchronized (this.mValidationLogs) {
            while (this.mValidationLogs.size() >= 10) {
                this.mValidationLogs.removeLast();
            }
            this.mValidationLogs.addFirst(new ValidationLog(network, networkExtraInfo, log));
        }
    }

    public ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this(context, netManager, statsService, policyManager, new IpConnectivityLog());
    }

    protected ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager, IpConnectivityLog logger) {
        boolean equals;
        this.mVpns = new SparseArray();
        this.mDefaultInetConditionPublished = 0;
        this.mNetTransitionWakeLockCausedBy = "";
        this.mDefaultProxy = null;
        this.mProxyLock = new Object();
        this.mDefaultProxyDisabled = false;
        this.mGlobalProxy = null;
        this.mPacManager = null;
        this.mNextNetId = 100;
        this.mNextNetworkRequestId = 1;
        this.mP2pNetworkInfo = new NetworkInfo(13, 0, "WIFI_P2P", "");
        this.mNetworkRequestInfoLogs = new LocalLog(20);
        this.mNetworkInfoBlockingLogs = new LocalLog(100);
        this.mValidationLogs = new ArrayDeque(10);
        this.mLegacyTypeTracker = new LegacyTypeTracker();
        this.mDataActivityObserver = new BaseNetworkObserver() {
            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
                ConnectivityService.this.sendDataActivityBroadcast(Integer.parseInt(label), active, tsNanos);
            }
        };
        this.mPolicyListener = new Stub() {
            public void onUidRulesChanged(int uid, int uidRules) {
            }

            public void onMeteredIfacesChanged(String[] meteredIfaces) {
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                if (restrictBackground) {
                    ConnectivityService.log("onRestrictBackgroundChanged(true): disabling tethering");
                    ConnectivityService.this.mTethering.untetherAll();
                }
            }

            public void onUidPoliciesChanged(int uid, int uidPolicies) {
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
        this.mNetworkFactoryInfos = new HashMap();
        this.mNetworkRequests = new HashMap();
        this.mUidToNetworkRequestCount = new SparseIntArray();
        this.mNetworkForRequestId = new SparseArray();
        this.mNetworkForNetId = new SparseArray();
        this.mNetIdInUse = new SparseBooleanArray();
        this.mNetworkAgentInfos = new HashMap();
        this.mBlockedAppUids = new HashSet();
        log("ConnectivityService starting up");
        this.mSystemProperties = getSystemProperties();
        this.mMetricsLog = logger;
        this.mDefaultRequest = createInternetRequestForTransport(-1, Type.REQUEST);
        NetworkRequestInfo defaultNRI = new NetworkRequestInfo(null, this.mDefaultRequest, new Binder());
        this.mNetworkRequests.put(this.mDefaultRequest, defaultNRI);
        this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI);
        this.mDefaultMobileDataRequest = createInternetRequestForTransport(0, Type.BACKGROUND_REQUEST);
        this.mHandlerThread = new HandlerThread("ConnectivityServiceThread");
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mTrackerHandler = new NetworkStateTrackerHandler(this.mHandlerThread.getLooper());
        String hostname = SystemProperties.get("net.hostname");
        if (TextUtils.isEmpty(hostname) || hostname.length() < 8) {
            String id = Secure.getString(context.getContentResolver(), "android_id");
            if (id != null && id.length() > 0) {
                if (TextUtils.isEmpty(hostname)) {
                    hostname = SystemProperties.get("ro.config.marketing_name", "");
                    if (TextUtils.isEmpty(hostname)) {
                        hostname = Build.MODEL.replace(" ", "_");
                        if (hostname != null && hostname.length() > 18) {
                            hostname = hostname.substring(0, 18);
                        }
                    } else {
                        hostname = hostname.replace(" ", "_");
                    }
                }
                hostname = hostname + "-" + id;
                if (hostname != null && hostname.length() > 25) {
                    hostname = hostname.substring(0, 25);
                }
                SystemProperties.set("net.hostname", hostname);
            }
        }
        this.mReleasePendingIntentDelayMs = Secure.getInt(context.getContentResolver(), "connectivity_release_pending_intent_delay_ms", 5000);
        this.mLingerDelayMs = this.mSystemProperties.getInt(LINGER_DELAY_PROPERTY, DEFAULT_LINGER_DELAY_MS);
        this.mContext = (Context) checkNotNull(context, "missing Context");
        this.mNetd = (INetworkManagementService) checkNotNull(netManager, "missing INetworkManagementService");
        this.mStatsService = (INetworkStatsService) checkNotNull(statsService, "missing INetworkStatsService");
        this.mPolicyManager = (INetworkPolicyManager) checkNotNull(policyManager, "missing INetworkPolicyManager");
        this.mPolicyManagerInternal = (NetworkPolicyManagerInternal) checkNotNull((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class), "missing NetworkPolicyManagerInternal");
        this.mKeyStore = KeyStore.getInstance();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        try {
            this.mPolicyManager.registerListener(this.mPolicyListener);
        } catch (RemoteException e) {
            loge("unable to register INetworkPolicyListener" + e);
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mNetTransitionWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetTransitionWakeLockTimeout = this.mContext.getResources().getInteger(17694821);
        this.mPendingIntentWakeLock = powerManager.newWakeLock(1, TAG);
        this.mNetConfigs = new NetworkConfig[48];
        boolean wifiOnly = this.mSystemProperties.getBoolean("ro.radio.noril", false);
        log("wifiOnly=" + wifiOnly);
        for (String naString : context.getResources().getStringArray(17236057)) {
            try {
                NetworkConfig n = new NetworkConfig(naString);
                log("naString=" + naString + " config=" + n);
                if (n.type > 47) {
                    loge("Error in networkAttributes - ignoring attempt to define type " + n.type);
                } else {
                    if (wifiOnly) {
                        if (ConnectivityManager.isNetworkTypeMobile(n.type)) {
                            log("networkAttributes - ignoring mobile as this dev is wifiOnly " + n.type);
                        }
                    }
                    if (this.mNetConfigs[n.type] != null) {
                        loge("Error in networkAttributes - ignoring attempt to redefine type " + n.type);
                    } else {
                        this.mLegacyTypeTracker.addSupportedType(n.type);
                        this.mNetConfigs[n.type] = n;
                        this.mNetworksDefined++;
                    }
                }
            } catch (Exception e2) {
            }
        }
        if (this.mNetConfigs[17] == null) {
            this.mLegacyTypeTracker.addSupportedType(17);
            this.mNetworksDefined++;
        }
        log("mNetworksDefined=" + this.mNetworksDefined);
        this.mProtectedNetworks = new ArrayList();
        for (int p : context.getResources().getIntArray(17236025)) {
            if (this.mNetConfigs[p] == null || this.mProtectedNetworks.contains(Integer.valueOf(p))) {
                loge("Ignoring protectedNetwork " + p);
            } else {
                this.mProtectedNetworks.add(Integer.valueOf(p));
            }
        }
        if (this.mSystemProperties.get("cm.test.mode").equals("true")) {
            equals = this.mSystemProperties.get("ro.build.type").equals("eng");
        } else {
            equals = false;
        }
        this.mTestMode = equals;
        this.mTethering = new Tethering(this.mContext, this.mNetd, statsService, this.mPolicyManager, IoThread.get().getLooper(), new MockableSystemProperties());
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
            loge("Error registering observer :" + e3);
        }
        this.mInetLog = new ArrayList();
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerSettingsCallbacks();
        this.mDataConnectionStats = new DataConnectionStats(this.mContext);
        this.mDataConnectionStats.startMonitoring();
        this.mPacManager = new PacManager(this.mContext, this.mHandler, 16);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mKeepaliveTracker = new KeepaliveTracker(this.mHandler);
        this.mNotifier = new NetworkNotificationManager(this.mContext, this.mTelephonyManager, (NotificationManager) this.mContext.getSystemService(NotificationManager.class));
        this.mLingerMonitor = new LingerMonitor(this.mContext, this.mNotifier, Global.getInt(this.mContext.getContentResolver(), "network_switch_notification_daily_limit", 3), Global.getLong(this.mContext.getContentResolver(), "network_switch_notification_rate_limit_millis", LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS));
        this.mMultinetworkPolicyTracker = createMultinetworkPolicyTracker(this.mContext, this.mHandler, new -$Lambda$UMiM-Tf_wXGw9kQrVQXa-uniNcc(this));
        this.mMultinetworkPolicyTracker.start();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PKT_CNT_SAMPLE_INTERVAL_ELAPSED);
        if (HiSiWifiComm.hisiWifiEnabled()) {
            filter.addAction("android.net.wifi.p2p.WIFI_P2P_NETWORK_CHANGED_ACTION");
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (HiSiWifiComm.hisiWifiEnabled() && "android.net.wifi.p2p.WIFI_P2P_NETWORK_CHANGED_ACTION".equals(action)) {
                    ConnectivityService.this.mP2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                }
            }
        }, new IntentFilter(filter));
    }

    private NetworkRequest createInternetRequestForTransport(int transportType, Type type) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        if (transportType > -1) {
            netCap.addTransportType(transportType);
        }
        return new NetworkRequest(netCap, -1, nextNetworkRequestId(), type);
    }

    void updateMobileDataAlwaysOn() {
        this.mHandler.sendEmptyMessage(30);
    }

    private void handleMobileDataAlwaysOn() {
        boolean enable = Global.getInt(this.mContext.getContentResolver(), "mobile_data_always_on", 0) == 1;
        if (enable != (this.mNetworkRequests.get(this.mDefaultMobileDataRequest) != null)) {
            if (enable) {
                handleRegisterNetworkRequest(new NetworkRequestInfo(null, this.mDefaultMobileDataRequest, new Binder()));
            } else {
                handleReleaseNetworkRequest(this.mDefaultMobileDataRequest, 1000);
            }
        }
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Global.getUriFor("http_proxy"), 9);
        this.mSettingsObserver.observe(Global.getUriFor("mobile_data_always_on"), 30);
    }

    private synchronized int nextNetworkRequestId() {
        int i;
        i = this.mNextNetworkRequestId;
        this.mNextNetworkRequestId = i + 1;
        return i;
    }

    protected int reserveNetId() {
        synchronized (this.mNetworkForNetId) {
            int i = 100;
            while (i <= 65535) {
                int netId = this.mNextNetId;
                int i2 = this.mNextNetId + 1;
                this.mNextNetId = i2;
                if (i2 > 65535) {
                    this.mNextNetId = 100;
                }
                if (this.mNetIdInUse.get(netId)) {
                    i++;
                } else {
                    this.mNetIdInUse.put(netId, true);
                    return netId;
                }
            }
            throw new IllegalStateException("No free netIds");
        }
    }

    private NetworkState getFilteredNetworkState(int networkType, int uid, boolean ignoreBlocked) {
        boolean z = true;
        if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
            return NetworkState.EMPTY;
        }
        NetworkState state;
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            state = nai.getNetworkState();
            state.networkInfo.setType(networkType);
        } else {
            NetworkInfo info = new NetworkInfo(networkType, 0, ConnectivityManager.getNetworkTypeName(networkType), "");
            info.setDetailedState(DetailedState.DISCONNECTED, null, null);
            if (networkType == 13 || networkType == 1) {
                z = false;
            } else if (networkType == 0) {
                z = false;
            }
            info.setIsAvailable(z);
            state = new NetworkState(info, new LinkProperties(), new NetworkCapabilities(), null, null, null);
        }
        filterNetworkStateForUid(state, uid, ignoreBlocked);
        return state;
    }

    private NetworkAgentInfo getNetworkAgentInfoForNetwork(Network network) {
        if (network == null) {
            return null;
        }
        NetworkAgentInfo networkAgentInfo;
        synchronized (this.mNetworkForNetId) {
            networkAgentInfo = (NetworkAgentInfo) this.mNetworkForNetId.get(network.netId);
        }
        return networkAgentInfo;
    }

    private Network[] getVpnUnderlyingNetworks(int uid) {
        if (!this.mLockdownEnabled) {
            int user = UserHandle.getUserId(uid);
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(user);
                if (vpn == null || !vpn.appliesToUid(uid)) {
                } else {
                    Network[] underlyingNetworks = vpn.getUnderlyingNetworks();
                    return underlyingNetworks;
                }
            }
        }
        return null;
    }

    private NetworkState getUnfilteredActiveNetworkState(int uid) {
        NetworkAgentInfo nai = null;
        NetworkAgentInfo o = this.mNetworkForRequestId.get(this.mDefaultRequest.requestId);
        if (o instanceof NetworkAgentInfo) {
            nai = o;
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

    /* JADX WARNING: Missing block: B:17:0x0026, code:
            if (r6 != null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:18:0x0028, code:
            r0 = "";
     */
    /* JADX WARNING: Missing block: B:20:0x0031, code:
            return r5.mPolicyManagerInternal.isUidNetworkingBlocked(r7, r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0035, code:
            r0 = r6.getInterfaceName();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isNetworkWithLinkPropertiesBlocked(LinkProperties lp, int uid, boolean ignoreBlocked) {
        if (ignoreBlocked || isSystem(uid)) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn == null || !vpn.isBlockingUid(uid)) {
            } else {
                return true;
            }
        }
    }

    private void maybeLogBlockedNetworkInfo(NetworkInfo ni, int uid) {
        if (ni != null) {
            boolean removed = false;
            boolean added = false;
            synchronized (this.mBlockedAppUids) {
                if (ni.getDetailedState() == DetailedState.BLOCKED && this.mBlockedAppUids.add(Integer.valueOf(uid))) {
                    added = true;
                } else if (ni.isConnected() && this.mBlockedAppUids.remove(Integer.valueOf(uid))) {
                    removed = true;
                }
            }
            if (added) {
                log("Returning blocked NetworkInfo to uid=" + uid);
                this.mNetworkInfoBlockingLogs.log("BLOCKED " + uid);
            } else if (removed) {
                log("Returning unblocked NetworkInfo to uid=" + uid);
                this.mNetworkInfoBlockingLogs.log("UNBLOCKED " + uid);
            }
        }
    }

    private void filterNetworkStateForUid(NetworkState state, int uid, boolean ignoreBlocked) {
        if (state != null && state.networkInfo != null && state.linkProperties != null) {
            if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, ignoreBlocked)) {
                state.networkInfo.setDetailedState(DetailedState.BLOCKED, null, null);
            }
            if (this.mLockdownTracker != null) {
                this.mLockdownTracker.augmentNetworkInfo(state.networkInfo);
            }
            long token = Binder.clearCallingIdentity();
            try {
                state.networkInfo.setMetered(this.mPolicyManager.isNetworkMetered(state));
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public NetworkInfo getActiveNetworkInfo() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        NetworkState state = getUnfilteredActiveNetworkState(uid);
        filterNetworkStateForUid(state, uid, false);
        maybeLogBlockedNetworkInfo(state.networkInfo, uid);
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
        Network network = null;
        int user = UserHandle.getUserId(uid);
        int vpnNetId = 0;
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(user);
            if (vpn != null && vpn.appliesToUid(uid)) {
                vpnNetId = vpn.getNetId();
            }
        }
        if (vpnNetId != 0) {
            synchronized (this.mNetworkForNetId) {
                nai = (NetworkAgentInfo) this.mNetworkForNetId.get(vpnNetId);
            }
            if (nai != null) {
                return nai.network;
            }
        }
        nai = getDefaultNetwork();
        if (nai != null && isNetworkWithLinkPropertiesBlocked(nai.linkProperties, uid, ignoreBlocked)) {
            nai = null;
        }
        if (nai != null) {
            network = nai.network;
        }
        return network;
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
        enforceAccessPermission();
        if (HiSiWifiComm.hisiWifiEnabled() && 13 == networkType) {
            log("getNetworkInfo mP2pNetworkInfo:" + this.mP2pNetworkInfo);
            return new NetworkInfo(this.mP2pNetworkInfo);
        }
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
        if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, false)) {
            return null;
        }
        return state.network;
    }

    public Network[] getAllNetworks() {
        Network[] result;
        enforceAccessPermission();
        synchronized (this.mNetworkForNetId) {
            result = new Network[this.mNetworkForNetId.size()];
            for (int i = 0; i < this.mNetworkForNetId.size(); i++) {
                result[i] = ((NetworkAgentInfo) this.mNetworkForNetId.valueAt(i)).network;
            }
        }
        return result;
    }

    public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) {
        enforceAccessPermission();
        HashMap<Network, NetworkCapabilities> result = new HashMap();
        NetworkAgentInfo nai = getDefaultNetwork();
        NetworkCapabilities nc = getNetworkCapabilitiesInternal(nai);
        if (nc != null) {
            result.put(nai.network, nc);
        }
        if (!this.mLockdownEnabled) {
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(userId);
                if (vpn != null) {
                    Network[] networks = vpn.getUnderlyingNetworks();
                    if (networks != null) {
                        for (Network network : networks) {
                            nc = getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
                            if (nc != null) {
                                result.put(network, nc);
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
        enforceAccessPermission();
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai == null) {
            return null;
        }
        LinkProperties linkProperties;
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    public LinkProperties getLinkProperties(Network network) {
        enforceAccessPermission();
        return getLinkProperties(getNetworkAgentInfoForNetwork(network));
    }

    private LinkProperties getLinkProperties(NetworkAgentInfo nai) {
        if (nai == null) {
            return null;
        }
        LinkProperties linkProperties;
        synchronized (nai) {
            linkProperties = new LinkProperties(nai.linkProperties);
        }
        return linkProperties;
    }

    private NetworkCapabilities getNetworkCapabilitiesInternal(NetworkAgentInfo nai) {
        if (nai != null) {
            synchronized (nai) {
                if (nai.networkCapabilities != null) {
                    NetworkCapabilities networkCapabilities = new NetworkCapabilities(nai.networkCapabilities);
                    return networkCapabilities;
                }
            }
        }
        return null;
    }

    public NetworkCapabilities getNetworkCapabilities(Network network) {
        enforceAccessPermission();
        return getNetworkCapabilitiesInternal(getNetworkAgentInfoForNetwork(network));
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

    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        long token = Binder.clearCallingIdentity();
        try {
            NetworkState state = getUnfilteredActiveNetworkState(uid);
            if (state.networkInfo != null) {
                try {
                    NetworkQuotaInfo networkQuotaInfo = this.mPolicyManager.getNetworkQuotaInfo(state);
                    return networkQuotaInfo;
                } catch (RemoteException e) {
                }
            }
            Binder.restoreCallingIdentity(token);
            return null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean isActiveNetworkMetered() {
        enforceAccessPermission();
        NetworkInfo info = getActiveNetworkInfo();
        return info != null ? info.isMetered() : false;
    }

    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) {
        enforceChangePermission();
        if (this.mProtectedNetworks.contains(Integer.valueOf(networkType))) {
            enforceConnectivityInternalPermission();
        }
        try {
            InetAddress addr = InetAddress.getByAddress(hostAddress);
            if (ConnectivityManager.isNetworkTypeValid(networkType)) {
                NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
                if (nai == null) {
                    if (this.mLegacyTypeTracker.isTypeSupported(networkType)) {
                        log("requestRouteToHostAddress on down network: " + networkType);
                    } else {
                        log("requestRouteToHostAddress on unsupported network: " + networkType);
                    }
                    return false;
                }
                DetailedState netState;
                synchronized (nai) {
                    netState = nai.networkInfo.getDetailedState();
                }
                if (netState == DetailedState.CONNECTED || netState == DetailedState.CAPTIVE_PORTAL_CHECK) {
                    int uid = Binder.getCallingUid();
                    long token = Binder.clearCallingIdentity();
                    try {
                        LinkProperties lp;
                        int netId;
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
                    log("requestRouteToHostAddress on down network (" + networkType + ") - dropped" + " netState=" + netState);
                    return false;
                }
            }
            log("requestRouteToHostAddress on invalid network: " + networkType);
            return false;
        } catch (UnknownHostException e) {
            log("requestRouteToHostAddress got " + e.toString());
            return false;
        }
    }

    private boolean addLegacyRouteToHost(LinkProperties lp, InetAddress addr, int netId, int uid) {
        RouteInfo bestRoute = RouteInfo.selectBestRoute(lp.getAllRoutes(), addr);
        if (bestRoute == null) {
            bestRoute = RouteInfo.makeHostRoute(addr, lp.getInterfaceName());
        } else {
            String iface = bestRoute.getInterface();
            if (bestRoute.getGateway().equals(addr)) {
                bestRoute = RouteInfo.makeHostRoute(addr, iface);
            } else {
                bestRoute = RouteInfo.makeHostRoute(addr, bestRoute.getGateway(), iface);
            }
        }
        if (!(this.mBestLegacyRoute == null || this.mBestLegacyRoute.getDestination() == null || bestRoute == null || bestRoute.getDestination() == null || this.mBestLegacyRoute.getDestination().getAddress() == null || !this.mBestLegacyRoute.getDestination().getAddress().equals(bestRoute.getDestination().getAddress()))) {
            removeLegacyRouteToHost(this.mLegacyRouteNetId, this.mBestLegacyRoute, this.mLegacyRouteUid);
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
            loge("Exception trying to add a route: " + e);
            return false;
        }
    }

    private void removeLegacyRouteToHost(int netId, RouteInfo bestRoute, int uid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        log("removeLegacyRouteToHost");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(netId);
                bestRoute.writeToParcel(_data, 0);
                _data.writeInt(uid);
                b.transact(CODE_REMOVE_LEGACYROUTE_TO_HOST, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } catch (Exception e) {
                loge("Exception trying to remove a route: " + e);
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    private void enforceCrossUserPermission(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
    }

    private void enforceInternetPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERNET", "ConnectivityService");
    }

    protected void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", "ConnectivityService");
    }

    protected void enforceChangePermission() {
        ConnectivityManager.enforceChangePermission(this.mContext);
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
        if (this.mLockdownTracker != null) {
            NetworkInfo info2 = new NetworkInfo(info);
            this.mLockdownTracker.augmentNetworkInfo(info2);
            info = info2;
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

    protected void sendStickyBroadcast(Intent intent) {
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
            }
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL, options);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void systemReady() {
        loadGlobalProxy();
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
        int timeout;
        String iface = networkAgent.linkProperties.getInterfaceName();
        int type = -1;
        if (networkAgent.networkCapabilities.hasTransport(0)) {
            timeout = Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_mobile", 10);
            type = 0;
        } else if (networkAgent.networkCapabilities.hasTransport(1)) {
            timeout = Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_wifi", 15);
            type = 1;
        } else {
            timeout = 0;
        }
        if (timeout > 0 && iface != null && type != -1) {
            try {
                this.mNetd.addIdleTimer(iface, timeout, type);
            } catch (Exception e) {
                loge("Exception in setupDataActivityTracking " + e);
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
                loge("Exception in removeDataActivityTracking " + e);
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
                    Slog.e(TAG, "exception in setMtu()" + e);
                }
            }
        }
    }

    protected MockableSystemProperties getSystemProperties() {
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
                    Slog.d(TAG, "Setting tx/rx TCP buffers to " + tcpBufferSizes);
                    String prefix = "/sys/kernel/ipv4/tcp_";
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
                Integer rwndValue = Integer.valueOf(Global.getInt(this.mContext.getContentResolver(), "tcp_default_init_rwnd", this.mSystemProperties.getInt(DEFAULT_TCP_RWND_KEY, 0)));
                String sysctlKey = "sys.sysctl.tcp_def_init_rwnd";
                if (rwndValue.intValue() != 0) {
                    this.mSystemProperties.set("sys.sysctl.tcp_def_init_rwnd", rwndValue.toString());
                }
            }
        }
    }

    private void flushVmDnsCache() {
        Intent intent = new Intent("android.intent.action.CLEAR_DNS_CACHE");
        intent.addFlags(536870912);
        intent.addFlags(67108864);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
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

    private boolean argsContain(String[] args, String target) {
        for (String arg : args) {
            if (arg.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private void dumpNetworkDiagnostics(IndentingPrintWriter pw) {
        List<NetworkDiagnostics> netDiags = new ArrayList();
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            netDiags.add(new NetworkDiagnostics(nai.network, new LinkProperties(nai.linkProperties), 5000));
        }
        for (NetworkDiagnostics netDiag : netDiags) {
            pw.println();
            netDiag.waitForMeasurements();
            netDiag.dump(pw);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            if (argsContain(args, DIAG_ARG)) {
                dumpNetworkDiagnostics(pw);
                return;
            }
            int i;
            pw.print("NetworkFactories for:");
            for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
                pw.print(" " + nfi.name);
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
                pw.println(String.format("Requests: REQUEST:%d LISTEN:%d BACKGROUND_REQUEST:%d total:%d", new Object[]{Integer.valueOf(nai.numForegroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests() - nai.numRequestNetworkRequests()), Integer.valueOf(nai.numBackgroundNetworkRequests()), Integer.valueOf(nai.numNetworkRequests())}));
                pw.increaseIndent();
                for (i = 0; i < nai.numNetworkRequests(); i++) {
                    pw.println(nai.requestAt(i).toString());
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
            synchronized (this) {
                pw.print("mNetTransitionWakeLock: currently " + (this.mNetTransitionWakeLock.isHeld() ? "" : "not ") + "held");
                if (TextUtils.isEmpty(this.mNetTransitionWakeLockCausedBy)) {
                    pw.println(", last requested never");
                } else {
                    pw.println(", last requested for " + this.mNetTransitionWakeLockCausedBy);
                }
            }
            pw.println();
            this.mTethering.dump(fd, pw, args);
            pw.println();
            this.mKeepaliveTracker.dump(pw);
            pw.println();
            dumpAvoidBadWifiSettings(pw);
            pw.println();
            if (this.mInetLog != null && this.mInetLog.size() > 0) {
                pw.println();
                pw.println("Inet condition reports:");
                pw.increaseIndent();
                for (i = 0; i < this.mInetLog.size(); i++) {
                    pw.println(this.mInetLog.get(i));
                }
                pw.decreaseIndent();
            }
            if (!argsContain(args, SHORT_ARG)) {
                pw.println();
                synchronized (this.mValidationLogs) {
                    pw.println("mValidationLogs (most recent first):");
                    for (ValidationLog p : this.mValidationLogs) {
                        pw.println(p.mNetwork + " - " + p.mNetworkExtraInfo);
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
            }
        }
    }

    private boolean isLiveNetworkAgent(NetworkAgentInfo nai, int what) {
        if (nai.network == null) {
            return false;
        }
        NetworkAgentInfo officialNai = getNetworkAgentInfoForNetwork(nai.network);
        if (officialNai != null && officialNai.equals(nai)) {
            return true;
        }
        loge(((String) sMagicDecoderRing.get(what, Integer.toString(what))) + " - isLiveNetworkAgent found mismatched netId: " + officialNai + " - " + nai);
        return false;
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

    private void handleAsyncChannelHalfConnect(Message msg) {
        AsyncChannel ac = msg.obj;
        NetworkAgentInfo nai;
        if (this.mNetworkFactoryInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                log("NetworkFactory connected");
                for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                    if (!nri.request.isListen()) {
                        int currentScore;
                        nai = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                        if (nai != null) {
                            currentScore = nai.getCurrentScore();
                        } else {
                            currentScore = 0;
                        }
                        ac.sendMessage(536576, currentScore, 0, nri.request);
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
                ((NetworkAgentInfo) this.mNetworkAgentInfos.get(msg.replyTo)).asyncChannel.sendMessage(69633);
                return;
            }
            loge("Error connecting NetworkAgent");
            nai = (NetworkAgentInfo) this.mNetworkAgentInfos.remove(msg.replyTo);
            if (nai != null) {
                boolean wasDefault = isDefaultNetwork(nai);
                synchronized (this.mNetworkForNetId) {
                    this.mNetworkForNetId.remove(nai.network.netId);
                    this.mNetIdInUse.delete(nai.network.netId);
                }
                this.mLegacyTypeTracker.remove(nai, wasDefault);
            }
        }
    }

    private void handleAsyncChannelDisconnected(Message msg) {
        NetworkAgentInfo nai = (NetworkAgentInfo) this.mNetworkAgentInfos.get(msg.replyTo);
        if (nai != null) {
            log(nai.name() + " got DISCONNECTED, was satisfying " + nai.numNetworkRequests());
            if (nai.networkInfo.isConnected()) {
                nai.networkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
            }
            boolean wasDefault = isDefaultNetwork(nai);
            if (wasDefault) {
                this.mDefaultInetConditionPublished = 0;
                logDefaultNetworkEvent(null, nai);
            }
            notifyIfacesChangedForNetworkStats();
            notifyNetworkCallbacks(nai, 524292);
            this.mKeepaliveTracker.handleStopAllKeepalives(nai, -20);
            nai.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_DISCONNECTED);
            this.mNetworkAgentInfos.remove(msg.replyTo);
            updateClat(null, nai.linkProperties, nai);
            synchronized (this.mNetworkForNetId) {
                this.mNetworkForNetId.remove(nai.network.netId);
            }
            for (int i = 0; i < nai.numNetworkRequests(); i++) {
                NetworkRequest request = nai.requestAt(i);
                NetworkAgentInfo currentNetwork = (NetworkAgentInfo) this.mNetworkForRequestId.get(request.requestId);
                if (currentNetwork != null && currentNetwork.network.netId == nai.network.netId) {
                    this.mNetworkForRequestId.remove(request.requestId);
                    sendUpdatedScoreToFactories(request, 0);
                }
            }
            nai.clearLingerState();
            if (nai.isSatisfyingRequest(this.mDefaultRequest.requestId)) {
                removeDataActivityTracking(nai);
                notifyLockdownVpn(nai);
                requestNetworkTransitionWakelock(nai.name());
            }
            this.mLegacyTypeTracker.remove(nai, wasDefault);
            rematchAllNetworksAndRequests(null, 0);
            this.mLingerMonitor.noteDisconnect(nai);
            if (nai.created) {
                try {
                    this.mNetd.removeNetwork(nai.network.netId);
                } catch (Exception e) {
                    loge("Exception removing network: " + e);
                }
            }
            synchronized (this.mNetworkForNetId) {
                this.mNetIdInUse.delete(nai.network.netId);
            }
            if (SystemProperties.getBoolean("ro.config.usb_rj45", false) && nai.isEthernet()) {
                sendInetConditionBroadcast(nai.networkInfo);
                return;
            }
            return;
        }
        NetworkFactoryInfo nfi = (NetworkFactoryInfo) this.mNetworkFactoryInfos.remove(msg.replyTo);
        if (nfi != null) {
            log("unregisterNetworkFactory for " + nfi.name);
        }
    }

    private NetworkRequestInfo findExistingNetworkRequestInfo(PendingIntent pendingIntent) {
        Intent intent = pendingIntent.getIntent();
        for (Entry<NetworkRequest, NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            PendingIntent existingPendingIntent = ((NetworkRequestInfo) entry.getValue()).mPendingIntent;
            if (existingPendingIntent != null && existingPendingIntent.getIntent().filterEquals(intent)) {
                return (NetworkRequestInfo) entry.getValue();
            }
        }
        return null;
    }

    private void handleRegisterNetworkRequestWithIntent(Message msg) {
        NetworkRequestInfo nri = msg.obj;
        NetworkRequestInfo existingRequest = findExistingNetworkRequestInfo(nri.mPendingIntent);
        if (existingRequest != null) {
            log("Replacing " + existingRequest.request + " with " + nri.request + " because their intents matched.");
            handleReleaseNetworkRequest(existingRequest.request, getCallingUid());
        }
        handleRegisterNetworkRequest(nri);
    }

    protected void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        this.mNetworkRequests.put(nri.request, nri);
        this.mNetworkRequestInfoLogs.log("REGISTER " + nri);
        if (nri.request.isListen()) {
            for (NetworkAgentInfo network : this.mNetworkAgentInfos.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && network.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    updateSignalStrengthThresholds(network, "REGISTER", nri.request);
                }
            }
        }
        rematchAllNetworksAndRequests(null, 0);
        if (nri.request.isRequest() && this.mNetworkForRequestId.get(nri.request.requestId) == null) {
            sendUpdatedScoreToFactories(nri.request, 0);
        }
    }

    private void handleReleaseNetworkRequestWithIntent(PendingIntent pendingIntent, int callingUid) {
        NetworkRequestInfo nri = findExistingNetworkRequestInfo(pendingIntent);
        if (nri != null) {
            handleReleaseNetworkRequest(nri.request, callingUid);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x002e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean unneeded(NetworkAgentInfo nai, UnneededFor reason) {
        int numRequests;
        switch (-getcom-android-server-ConnectivityService$UnneededForSwitchesValues()[reason.ordinal()]) {
            case 1:
                numRequests = nai.numForegroundNetworkRequests();
                break;
            case 2:
                numRequests = nai.numRequestNetworkRequests();
                break;
            default:
                Slog.wtf(TAG, "Invalid reason. Cannot happen.");
                return true;
        }
        if (!nai.everConnected || nai.isVPN() || nai.isLingering() || numRequests > 0 || ignoreRemovedByWifiPro(nai)) {
            return false;
        }
        int isDisabledMobileNetwork;
        if (nai.networkInfo.getType() != 0 || (this.mTelephonyManager.getDataEnabled() ^ 1) == 0) {
            isDisabledMobileNetwork = 0;
        } else {
            isDisabledMobileNetwork = HwTelephonyManager.getDefault().isVSimEnabled() ^ 1;
        }
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            if (reason != UnneededFor.LINGER || !nri.request.isBackgroundRequest()) {
                NetworkAgentInfo existing = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                if (existing == null) {
                    log("can not find existing request info on mNetworkForRequestId, request is " + nri.request);
                }
                boolean satisfies = !nai.satisfies(nri.request) ? checkNetworkSupportBip(nai, nri.request) : true;
                if (this.mDefaultRequest.requestId == nri.request.requestId && isDisabledMobileNetwork != 0) {
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
        NetworkRequestInfo nri = (NetworkRequestInfo) this.mNetworkRequests.get(request);
        if (nri == null || 1000 == callingUid || nri.mUid == callingUid) {
            return nri;
        }
        log(String.format("UID %d attempted to %s for unowned request %s", new Object[]{Integer.valueOf(callingUid), requestedOperation, nri}));
        return null;
    }

    private void handleTimedOutNetworkRequest(NetworkRequestInfo nri) {
        if (this.mNetworkRequests.get(nri.request) != null && this.mNetworkForRequestId.get(nri.request.requestId) == null) {
            handleRemoveNetworkRequest(nri, 524293);
        }
    }

    protected void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequestInfo nri = getNriForAppRequest(request, callingUid, "release NetworkRequest");
        if (nri != null) {
            handleRemoveNetworkRequest(nri, 524296);
        }
    }

    protected void handleRemoveNetworkRequest(NetworkRequestInfo nri, int whichCallback) {
        log("releasing " + nri.request + " (" + ConnectivityManager.getCallbackName(whichCallback) + ")");
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
        NetworkAgentInfo nai;
        if (nri.request.isRequest()) {
            boolean wasKept = false;
            nai = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
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
                this.mNetworkForRequestId.remove(nri.request.requestId);
                if (!wasBackgroundNetwork && nai.isBackgroundNetwork()) {
                    updateCapabilities(nai.getCurrentScore(), nai, nai.networkCapabilities);
                }
            }
            for (NetworkAgentInfo otherNai : this.mNetworkAgentInfos.values()) {
                if (otherNai.isSatisfyingRequest(nri.request.requestId) && otherNai != nai) {
                    Slog.wtf(TAG, "Request " + nri.request + " satisfied by " + otherNai.name() + ", but mNetworkAgentInfos says " + (nai != null ? nai.name() : "null"));
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
        } else {
            for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
                nai2.removeRequest(nri.request.requestId);
                if (nri.request.networkCapabilities.hasSignalStrength() && nai2.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    updateSignalStrengthThresholds(nai2, "RELEASE", nri.request);
                }
            }
        }
        callCallbackForRequest(nri, null, whichCallback, 0);
    }

    public void setAcceptUnvalidated(Network network, boolean accept, boolean always) {
        int i;
        int i2 = 1;
        enforceConnectivityInternalPermission();
        InternalHandler internalHandler = this.mHandler;
        InternalHandler internalHandler2 = this.mHandler;
        if (accept) {
            i = 1;
        } else {
            i = 0;
        }
        if (!always) {
            i2 = 0;
        }
        internalHandler.sendMessage(internalHandler2.obtainMessage(28, i, i2, network));
    }

    public void setAvoidUnvalidated(Network network) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(35, network));
    }

    private void handleSetAcceptUnvalidated(Network network, boolean accept, boolean always) {
        log("handleSetAcceptUnvalidated network=" + network + " accept=" + accept + " always=" + always);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated) {
            if (!nai.networkMisc.explicitlySelected) {
                Slog.wtf(TAG, "BUG: setAcceptUnvalidated non non-explicitly selected network");
            }
            if (accept != nai.networkMisc.acceptUnvalidated) {
                int oldScore = nai.getCurrentScore();
                nai.networkMisc.acceptUnvalidated = accept;
                rematchAllNetworksAndRequests(nai, oldScore);
                sendUpdatedScoreToFactories(nai);
            }
            if (always) {
                nai.asyncChannel.sendMessage(528393, accept ? 1 : 0);
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleSetAvoidUnvalidated(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (!(nai == null || nai.lastValidated || nai.avoidUnvalidated)) {
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
        this.mHandler.post(new com.android.server.-$Lambda$UMiM-Tf_wXGw9kQrVQXa-uniNcc.AnonymousClass1(this, network));
    }

    /* synthetic */ void lambda$-com_android_server_ConnectivityService_125386(Network network) {
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && nai.networkCapabilities.hasCapability(17)) {
            nai.networkMonitor.sendMessage(NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP);
        }
    }

    public boolean avoidBadWifi() {
        return this.mMultinetworkPolicyTracker.getAvoidBadWifi();
    }

    private void rematchForAvoidBadWifiUpdate() {
        rematchAllNetworksAndRequests(null, 0);
        for (NetworkAgentInfo nai : this.mNetworkAgentInfos.values()) {
            if (nai.networkCapabilities.hasTransport(1)) {
                sendUpdatedScoreToFactories(nai);
            }
        }
    }

    private void dumpAvoidBadWifiSettings(IndentingPrintWriter pw) {
        boolean configRestrict = this.mMultinetworkPolicyTracker.configRestrictsAvoidBadWifi();
        if (configRestrict) {
            String description;
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
            return;
        }
        pw.println("Bad Wi-Fi avoidance: unrestricted");
    }

    private void showValidationNotification(NetworkAgentInfo nai, NotificationType type) {
        String action;
        switch (-getcom-android-server-connectivity-NetworkNotificationManager$NotificationTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                action = "android.net.conn.PROMPT_LOST_VALIDATION";
                break;
            case 2:
                action = "android.net.conn.PROMPT_UNVALIDATED";
                break;
            default:
                Slog.wtf(TAG, "Unknown notification type " + type);
                return;
        }
        Intent intent = new Intent(action);
        intent.setData(Uri.fromParts("netId", Integer.toString(nai.network.netId), null));
        intent.addFlags(268435456);
        intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiNoInternetDialog");
        this.mNotifier.showNotification(nai.network.netId, type, nai, null, PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, UserHandle.CURRENT), true);
    }

    private void handlePromptUnvalidated(Network network) {
        log("handlePromptUnvalidated " + network);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated && !nai.everCaptivePortalDetected && (nai.networkMisc.explicitlySelected ^ 1) == 0 && !nai.networkMisc.acceptUnvalidated) {
            showValidationNotification(nai, NotificationType.NO_INTERNET);
        }
    }

    private void handleNetworkUnvalidated(NetworkAgentInfo nai) {
        NetworkCapabilities nc = nai.networkCapabilities;
        log("handleNetworkUnvalidated " + nai.name() + " cap=" + nc);
        if (nc.hasTransport(1) && this.mMultinetworkPolicyTracker.shouldNotifyWifiUnvalidated()) {
            showValidationNotification(nai, NotificationType.LOST_INTERNET);
        }
    }

    public int getMultipathPreference(Network network) {
        enforceAccessPermission();
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai == null || (nai.networkInfo.isMetered() ^ 1) == 0) {
            return this.mMultinetworkPolicyTracker.getMeteredMultipathPreference();
        }
        return 7;
    }

    public int tether(String iface) {
        Log.d(TAG, "tether: ENTER");
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (!isTetheringSupported()) {
            return 3;
        }
        int status = this.mTethering.tether(iface);
        Log.w(TAG, "tether() return with status " + status);
        return status;
    }

    public int untether(String iface) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
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

    public int setUsbTethering(boolean enable) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
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

    public boolean isTetheringSupported() {
        int tetherEnabledInSettings;
        enforceTetherAccessPermission();
        if (Global.getInt(this.mContext.getContentResolver(), "tether_supported", this.mSystemProperties.get("ro.tether.denied").equals("true") ? 0 : 1) != 0) {
            tetherEnabledInSettings = this.mUserManager.hasUserRestriction("no_config_tethering") ^ 1;
        } else {
            tetherEnabledInSettings = 0;
        }
        boolean adminUser = false;
        long token = Binder.clearCallingIdentity();
        try {
            adminUser = this.mUserManager.isAdminUser();
            if (tetherEnabledInSettings == 0 || !adminUser) {
                return false;
            }
            return this.mTethering.hasTetherableConfiguration();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (isTetheringSupported()) {
            this.mTethering.startTethering(type, receiver, showProvisioningUi);
        } else {
            receiver.send(3, null);
        }
    }

    public void stopTethering(int type) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        this.mTethering.stopTethering(type);
    }

    private void requestNetworkTransitionWakelock(String forWhom) {
        Throwable th;
        synchronized (this) {
            try {
                if (this.mNetTransitionWakeLock.isHeld()) {
                    return;
                }
                int serialNum = this.mNetTransitionWakeLockSerialNumber + 1;
                this.mNetTransitionWakeLockSerialNumber = serialNum;
                try {
                    this.mNetTransitionWakeLock.acquire();
                    this.mNetTransitionWakeLockCausedBy = forWhom;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(24, serialNum, 0), (long) this.mNetTransitionWakeLockTimeout);
                } catch (Throwable th2) {
                    th = th2;
                    int i = serialNum;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            reportNetworkConnectivity(nai.network, percentage > 50);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0018, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
        NetworkAgentInfo nai;
        enforceAccessPermission();
        enforceInternetPermission();
        if (network == null) {
            nai = getDefaultNetwork();
        } else {
            nai = getNetworkAgentInfoForNetwork(network);
        }
        if (nai != null && nai.networkInfo.getState() != State.DISCONNECTING && nai.networkInfo.getState() != State.DISCONNECTED && hasConnectivity != nai.lastValidated) {
            int uid = Binder.getCallingUid();
            log("reportNetworkConnectivity(" + nai.network.netId + ", " + hasConnectivity + ") by " + uid);
            if (nai.everConnected && !isNetworkWithLinkPropertiesBlocked(getLinkProperties(nai), uid, false)) {
                nai.networkMonitor.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, uid);
            }
        }
    }

    private ProxyInfo getDefaultProxy() {
        ProxyInfo ret;
        synchronized (this.mProxyLock) {
            ret = this.mGlobalProxy;
            if (ret == null && (this.mDefaultProxyDisabled ^ 1) != 0) {
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
        a = canonicalizeProxyInfo(a);
        b = canonicalizeProxyInfo(b);
        if (Objects.equals(a, b)) {
            return a != null ? Objects.equals(a.getHost(), b.getHost()) : true;
        } else {
            return false;
        }
    }

    public void setGlobalProxy(ProxyInfo proxyProperties) {
        enforceConnectivityInternalPermission();
        synchronized (this.mProxyLock) {
            if (proxyProperties == this.mGlobalProxy) {
            } else if (proxyProperties != null && proxyProperties.equals(this.mGlobalProxy)) {
            } else if (this.mGlobalProxy == null || !this.mGlobalProxy.equals(proxyProperties)) {
                String host = "";
                int port = 0;
                String exclList = "";
                String pacFileUrl = "";
                if (proxyProperties == null || (TextUtils.isEmpty(proxyProperties.getHost()) && (Uri.EMPTY.equals(proxyProperties.getPacFileUrl()) ^ 1) == 0)) {
                    this.mGlobalProxy = null;
                } else if (proxyProperties.isValid()) {
                    this.mGlobalProxy = new ProxyInfo(proxyProperties);
                    host = this.mGlobalProxy.getHost();
                    port = this.mGlobalProxy.getPort();
                    exclList = this.mGlobalProxy.getExclusionListAsString();
                    if (!Uri.EMPTY.equals(proxyProperties.getPacFileUrl())) {
                        pacFileUrl = proxyProperties.getPacFileUrl().toString();
                    }
                } else {
                    log("Invalid proxy properties, ignoring: " + proxyProperties.toString());
                    return;
                }
                ContentResolver res = this.mContext.getContentResolver();
                long token = Binder.clearCallingIdentity();
                try {
                    Global.putString(res, "global_http_proxy_host", host);
                    Global.putInt(res, "global_http_proxy_port", port);
                    Global.putString(res, "global_http_proxy_exclusion_list", exclList);
                    Global.putString(res, "global_proxy_pac_url", pacFileUrl);
                    if (this.mGlobalProxy == null) {
                        proxyProperties = this.mDefaultProxy;
                    }
                    sendProxyBroadcast(proxyProperties);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
    }

    private void loadGlobalProxy() {
        ContentResolver res = this.mContext.getContentResolver();
        String host = Global.getString(res, "global_http_proxy_host");
        int port = Global.getInt(res, "global_http_proxy_port", 0);
        String exclList = Global.getString(res, "global_http_proxy_exclusion_list");
        String pacFileUrl = Global.getString(res, "global_proxy_pac_url");
        if (!(TextUtils.isEmpty(host) && (TextUtils.isEmpty(pacFileUrl) ^ 1) == 0)) {
            ProxyInfo proxyProperties;
            if (TextUtils.isEmpty(pacFileUrl)) {
                proxyProperties = new ProxyInfo(host, port, exclList);
            } else {
                proxyProperties = new ProxyInfo(pacFileUrl);
            }
            if (proxyProperties.isValid()) {
                synchronized (this.mProxyLock) {
                    this.mGlobalProxy = proxyProperties;
                }
            } else {
                log("Invalid proxy properties, ignoring: " + proxyProperties.toString());
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

    /* JADX WARNING: Missing block: B:48:0x0094, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleApplyDefaultProxy(ProxyInfo proxy) {
        if (proxy != null && TextUtils.isEmpty(proxy.getHost()) && Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            proxy = null;
        }
        synchronized (this.mProxyLock) {
            if (this.mDefaultProxy != null && this.mDefaultProxy.equals(proxy)) {
            } else if (this.mDefaultProxy == proxy) {
            } else {
                if (proxy != null) {
                    if ((proxy.isValid() ^ 1) != 0) {
                        log("Invalid proxy properties, ignoring: " + proxy.toString());
                        return;
                    }
                }
                if (this.mGlobalProxy == null || proxy == null || (Uri.EMPTY.equals(proxy.getPacFileUrl()) ^ 1) == 0 || !proxy.getPacFileUrl().equals(this.mGlobalProxy.getPacFileUrl())) {
                    this.mDefaultProxy = proxy;
                    if (this.mGlobalProxy != null) {
                    } else if (!this.mDefaultProxyDisabled) {
                        sendProxyBroadcast(proxy);
                    }
                } else {
                    this.mGlobalProxy = proxy;
                    sendProxyBroadcast(this.mGlobalProxy);
                }
            }
        }
    }

    private void updateProxy(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        if (!proxyInfoEqual(newLp == null ? null : newLp.getHttpProxy(), oldLp == null ? null : oldLp.getHttpProxy())) {
            sendProxyBroadcast(getDefaultProxy());
        }
    }

    private void handleDeprecatedGlobalHttpProxy() {
        String proxy = Global.getString(this.mContext.getContentResolver(), "http_proxy");
        if (!TextUtils.isEmpty(proxy)) {
            String[] data = proxy.split(":");
            if (data.length != 0) {
                String proxyHost = data[0];
                int proxyPort = 8080;
                if (data.length > 1) {
                    try {
                        proxyPort = Integer.parseInt(data[1]);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                setGlobalProxy(new ProxyInfo(data[0], proxyPort, ""));
            }
        }
    }

    private void sendProxyBroadcast(ProxyInfo proxy) {
        if (proxy == null) {
            proxy = new ProxyInfo("", 0, "");
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

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Slog.e(TAG, s, t);
    }

    private static <T> T checkNotNull(T value, String message) {
        if (value != null) {
            return value;
        }
        throw new NullPointerException(message);
    }

    public boolean prepareVpn(String oldPackage, String newPackage, int userId) {
        enforceCrossUserPermission(userId);
        throwIfLockdownEnabled();
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn != null) {
                boolean prepare = vpn.prepare(oldPackage, newPackage);
                return prepare;
            }
            return false;
        }
    }

    public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn != null) {
                vpn.setPackageAuthorization(packageName, authorized);
            }
        }
    }

    public ParcelFileDescriptor establishVpn(VpnConfig config) {
        if (HwServiceFactory.getHwConnectivityManager().isVpnDisabled()) {
            return null;
        }
        ParcelFileDescriptor establish;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            establish = ((Vpn) this.mVpns.get(user)).establish(config);
        }
        return establish;
    }

    public void startLegacyVpn(VpnProfile profile) {
        boolean isInsecureVpn = HwServiceFactory.getHwConnectivityManager().isInsecureVpnDisabled() ? profile.type == 0 || profile.type == 1 : false;
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
        throwIfLockdownEnabled();
        LinkProperties egress = getActiveLinkProperties();
        if (egress == null) {
            throw new IllegalStateException("Missing active network connection");
        }
        Vpn mVpn;
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            mVpn = (Vpn) this.mVpns.get(user);
        }
        if (mVpn != null) {
            mVpn.startLegacyVpn(profile, this.mKeyStore, egress);
        }
    }

    public LegacyVpnInfo getLegacyVpnInfo(int userId) {
        LegacyVpnInfo legacyVpnInfo;
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            legacyVpnInfo = ((Vpn) this.mVpns.get(userId)).getLegacyVpnInfo();
        }
        return legacyVpnInfo;
    }

    public VpnInfo[] getAllVpnInfo() {
        enforceConnectivityInternalPermission();
        if (this.mLockdownEnabled) {
            return new VpnInfo[0];
        }
        VpnInfo[] vpnInfoArr;
        synchronized (this.mVpns) {
            List<VpnInfo> infoList = new ArrayList();
            for (int i = 0; i < this.mVpns.size(); i++) {
                VpnInfo info = createVpnInfo((Vpn) this.mVpns.valueAt(i));
                if (info != null) {
                    infoList.add(info);
                }
            }
            vpnInfoArr = (VpnInfo[]) infoList.toArray(new VpnInfo[infoList.size()]);
        }
        return vpnInfoArr;
    }

    private VpnInfo createVpnInfo(Vpn vpn) {
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
        } else if (underlyingNetworks.length > 0) {
            LinkProperties linkProperties = getLinkProperties(underlyingNetworks[0]);
            if (linkProperties != null) {
                info.primaryUnderlyingIface = linkProperties.getInterfaceName();
            }
        }
        if (info.primaryUnderlyingIface == null) {
            info = null;
        }
        return info;
    }

    public VpnConfig getVpnConfig(int userId) {
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn != null) {
                VpnConfig vpnConfig = vpn.getVpnConfig();
                return vpnConfig;
            }
            return null;
        }
    }

    public boolean updateLockdownVpn() {
        if (Binder.getCallingUid() != 1000) {
            Slog.w(TAG, "Lockdown VPN only available to AID_SYSTEM");
            return false;
        }
        this.mLockdownEnabled = LockdownVpnTracker.isEnabled();
        if (this.mLockdownEnabled) {
            byte[] profileTag = this.mKeyStore.get("LOCKDOWN_VPN");
            if (profileTag == null) {
                Slog.e(TAG, "Lockdown VPN configured but cannot be read from keystore");
                return false;
            }
            String profileName = new String(profileTag);
            VpnProfile profile = VpnProfile.decode(profileName, this.mKeyStore.get("VPN_" + profileName));
            if (profile == null) {
                Slog.e(TAG, "Lockdown VPN configured invalid profile " + profileName);
                setLockdownTracker(null);
                return true;
            }
            int user = UserHandle.getUserId(Binder.getCallingUid());
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(user);
                if (vpn == null) {
                    Slog.w(TAG, "VPN for user " + user + " not ready yet. Skipping lockdown");
                    return false;
                }
                setLockdownTracker(new LockdownVpnTracker(this.mContext, this.mNetd, this, vpn, profile));
            }
        } else {
            setLockdownTracker(null);
        }
        return true;
    }

    private void setLockdownTracker(LockdownVpnTracker tracker) {
        LockdownVpnTracker existing = this.mLockdownTracker;
        this.mLockdownTracker = null;
        if (existing != null) {
            existing.shutdown();
        }
        if (tracker != null) {
            try {
                this.mNetd.setFirewallEnabled(true);
                this.mNetd.setFirewallInterfaceRule("lo", true);
                this.mLockdownTracker = tracker;
                this.mLockdownTracker.init();
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mNetd.setFirewallEnabled(false);
    }

    private void throwIfLockdownEnabled() {
        if (this.mLockdownEnabled) {
            throw new IllegalStateException("Unavailable in lockdown mode");
        }
    }

    private boolean startAlwaysOnVpn(int userId) {
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.wtf(TAG, "User " + userId + " has no Vpn configuration");
                return false;
            }
            boolean startAlwaysOnVpn = vpn.startAlwaysOnVpn();
            return startAlwaysOnVpn;
        }
    }

    public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown) {
        enforceConnectivityInternalPermission();
        enforceCrossUserPermission(userId);
        if (LockdownVpnTracker.isEnabled()) {
            return false;
        }
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.w(TAG, "User " + userId + " has no Vpn configuration");
                return false;
            } else if (!vpn.setAlwaysOnPackage(packageName, lockdown)) {
                return false;
            } else if (startAlwaysOnVpn(userId)) {
                vpn.saveAlwaysOnPackage();
                return true;
            } else {
                vpn.setAlwaysOnPackage(null, false);
                return false;
            }
        }
    }

    public String getAlwaysOnVpnPackage(int userId) {
        enforceConnectivityInternalPermission();
        enforceCrossUserPermission(userId);
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.w(TAG, "User " + userId + " has no Vpn configuration");
                return null;
            }
            String alwaysOnPackage = vpn.getAlwaysOnPackage();
            return alwaysOnPackage;
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        return -1;
    }

    private String getProvisioningUrlBaseFromFile() {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        FileReader fileReader = null;
        Configuration config = this.mContext.getResources().getConfiguration();
        try {
            FileReader fileReader2 = new FileReader(this.mProvisioningUrlFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileReader2);
                XmlUtils.beginDocument(parser, TAG_PROVISIONING_URLS);
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        if (fileReader2 != null) {
                            try {
                                fileReader2.close();
                            } catch (IOException e3) {
                            }
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
                                            if (fileReader2 != null) {
                                                try {
                                                    fileReader2.close();
                                                } catch (IOException e4) {
                                                }
                                            }
                                            return text;
                                        }
                                    }
                                } else {
                                    continue;
                                }
                            } catch (NumberFormatException e5) {
                                loge("NumberFormatException in getProvisioningUrlBaseFromFile: " + e5);
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } catch (FileNotFoundException e6) {
                fileReader = fileReader2;
            } catch (XmlPullParserException e7) {
                e = e7;
                fileReader = fileReader2;
            } catch (IOException e8) {
                e2 = e8;
                fileReader = fileReader2;
            } catch (Throwable th2) {
                th = th2;
                fileReader = fileReader2;
            }
        } catch (FileNotFoundException e9) {
            try {
                loge("Carrier Provisioning Urls file not found");
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e10) {
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e11) {
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e12) {
            e = e12;
            loge("Xml parser exception reading Carrier Provisioning Urls file: " + e);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e13) {
                }
            }
            return null;
        } catch (IOException e14) {
            e2 = e14;
            loge("I/O exception reading Carrier Provisioning Urls file: " + e2);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e15) {
                }
            }
            return null;
        }
    }

    public String getMobileProvisioningUrl() {
        enforceConnectivityInternalPermission();
        String url = getProvisioningUrlBaseFromFile();
        if (TextUtils.isEmpty(url)) {
            url = this.mContext.getResources().getString(17040452);
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
        long ident = Binder.clearCallingIdentity();
        try {
            this.mNotifier.setProvNotificationVisible(visible, 65536 + (networkType + 1), action);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setAirplaneMode(boolean enable) {
        enforceConnectivityInternalPermission();
        long ident = Binder.clearCallingIdentity();
        try {
            Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", enable ? 1 : 0);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, enable);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0051, code:
            if (r8.mUserManager.getUserInfo(r9).isPrimary() == false) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:18:0x0057, code:
            if (com.android.server.net.LockdownVpnTracker.isEnabled() == false) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:19:0x0059, code:
            updateLockdownVpn();
     */
    /* JADX WARNING: Missing block: B:20:0x005c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onUserStart(int userId) {
        synchronized (this.mVpns) {
            if (((Vpn) this.mVpns.get(userId)) != null) {
                loge("Starting user already has a VPN");
                return;
            }
            Vpn userVpn = new Vpn(this.mHandler.getLooper(), this.mContext, this.mNetd, userId);
            this.mVpns.put(userId, userVpn);
            ContentResolver cr = this.mContext.getContentResolver();
            String alwaysOnPackage = Secure.getStringForUser(cr, "always_on_vpn_app", userId);
            boolean alwaysOnLockdown = Secure.getIntForUser(cr, "always_on_vpn_lockdown", 0, userId) != 0;
            if (alwaysOnPackage != null) {
                userVpn.setAlwaysOnPackage(alwaysOnPackage, alwaysOnLockdown);
            }
        }
    }

    private void onUserStop(int userId) {
        synchronized (this.mVpns) {
            Vpn userVpn = (Vpn) this.mVpns.get(userId);
            if (userVpn == null) {
                loge("Stopped user has no VPN");
                return;
            }
            userVpn.onUserStopped();
            this.mVpns.delete(userId);
        }
    }

    private void onUserAdded(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                ((Vpn) this.mVpns.valueAt(i)).onUserAdded(userId);
            }
        }
    }

    private void onUserRemoved(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = 0; i < vpnsSize; i++) {
                ((Vpn) this.mVpns.valueAt(i)).onUserRemoved(userId);
            }
        }
    }

    private void onUserUnlocked(int userId) {
        if (this.mUserManager.getUserInfo(userId).isPrimary() && LockdownVpnTracker.isEnabled()) {
            updateLockdownVpn();
        } else {
            startAlwaysOnVpn(userId);
        }
    }

    private void ensureNetworkRequestHasType(NetworkRequest request) {
        if (request.type == Type.NONE) {
            throw new IllegalArgumentException("All NetworkRequests in ConnectivityService must have a type");
        }
    }

    private void ensureRequestableCapabilities(NetworkCapabilities networkCapabilities) {
        String badCapability = networkCapabilities.describeFirstNonRequestableCapability();
        if (badCapability != null) {
            throw new IllegalArgumentException("Cannot request network with " + badCapability);
        }
    }

    private ArrayList<Integer> getSignalStrengthThresholds(NetworkAgentInfo nai) {
        SortedSet<Integer> thresholds = new TreeSet();
        synchronized (nai) {
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && nai.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    thresholds.add(Integer.valueOf(nri.request.networkCapabilities.getSignalStrength()));
                }
            }
        }
        return new ArrayList(thresholds);
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

    public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutMs, IBinder binder, int legacyType) {
        Type type;
        if (networkCapabilities == null) {
            type = Type.TRACK_DEFAULT;
        } else {
            type = Type.REQUEST;
        }
        if (type == Type.TRACK_DEFAULT) {
            networkCapabilities = new NetworkCapabilities(this.mDefaultRequest.networkCapabilities);
            enforceAccessPermission();
        } else {
            NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
            enforceNetworkRequestPermissions(networkCapabilities2);
            enforceMeteredApnPolicy(networkCapabilities2);
            networkCapabilities = networkCapabilities2;
        }
        ensureRequestableCapabilities(networkCapabilities);
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("Bad timeout specified");
        }
        MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(networkCapabilities.getNetworkSpecifier());
        NetworkRequest networkRequest = new NetworkRequest(networkCapabilities, legacyType, nextNetworkRequestId(), type);
        NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
        log("requestNetwork for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(19, nri));
        if (timeoutMs > 0) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(20, nri), (long) timeoutMs);
        }
        return networkRequest;
    }

    private void enforceNetworkRequestPermissions(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasCapability(13)) {
            enforceChangePermission();
        } else {
            enforceConnectivityRestrictedNetworksPermission();
        }
    }

    public boolean requestBandwidthUpdate(Network network) {
        enforceAccessPermission();
        if (network == null) {
            return false;
        }
        NetworkAgentInfo nai;
        synchronized (this.mNetworkForNetId) {
            nai = (NetworkAgentInfo) this.mNetworkForNetId.get(network.netId);
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
        if (!(isSystem(uid) || networkCapabilities.hasCapability(11) || !this.mPolicyManagerInternal.isUidRestrictedOnMeteredNetworks(uid))) {
            networkCapabilities.addCapability(11);
        }
    }

    public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        enforceNetworkRequestPermissions(networkCapabilities2);
        enforceMeteredApnPolicy(networkCapabilities2);
        ensureRequestableCapabilities(networkCapabilities2);
        MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(networkCapabilities2.getNetworkSpecifier());
        NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, -1, nextNetworkRequestId(), Type.REQUEST);
        NetworkRequestInfo nri = new NetworkRequestInfo(networkRequest, operation);
        log("pendingRequest for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(26, nri));
        return networkRequest;
    }

    private void releasePendingNetworkRequestWithDelay(PendingIntent operation) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), 0, operation), (long) this.mReleasePendingIntentDelayMs);
    }

    public void releasePendingNetworkRequest(PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
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
        if (!ConnectivityManager.checkChangePermission(this.mContext)) {
            nc.addCapability(18);
        }
        MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(networkCapabilities.getNetworkSpecifier());
        NetworkRequest networkRequest = new NetworkRequest(nc, -1, nextNetworkRequestId(), Type.LISTEN);
        NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder);
        log("listenForNetwork for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(21, nri));
        return networkRequest;
    }

    public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        MatchAllNetworkSpecifier.checkNotMatchAllNetworkSpecifier(networkCapabilities.getNetworkSpecifier());
        NetworkRequestInfo nri = new NetworkRequestInfo(new NetworkRequest(new NetworkCapabilities(networkCapabilities), -1, nextNetworkRequestId(), Type.LISTEN), operation);
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

    private void handleRegisterNetworkFactory(NetworkFactoryInfo nfi) {
        log("Got NetworkFactory Messenger for " + nfi.name);
        this.mNetworkFactoryInfos.put(nfi.messenger, nfi);
        nfi.asyncChannel.connect(this.mContext, this.mTrackerHandler, nfi.messenger);
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23, messenger));
    }

    private void handleUnregisterNetworkFactory(Messenger messenger) {
        NetworkFactoryInfo nfi = (NetworkFactoryInfo) this.mNetworkFactoryInfos.remove(messenger);
        if (nfi == null) {
            loge("Failed to find Messenger in unregisterNetworkFactory");
        } else {
            log("unregisterNetworkFactory for " + nfi.name);
        }
    }

    private NetworkAgentInfo getDefaultNetwork() {
        Object obj = this.mNetworkForRequestId.get(this.mDefaultRequest.requestId);
        if (obj instanceof NetworkAgentInfo) {
            return (NetworkAgentInfo) obj;
        }
        return null;
    }

    private boolean isDefaultNetwork(NetworkAgentInfo nai) {
        return nai == getDefaultNetwork();
    }

    private boolean isDefaultRequest(NetworkRequestInfo nri) {
        return nri.request.requestId == this.mDefaultRequest.requestId;
    }

    private NetworkAgentInfo getIdenticalActiveNetworkAgentInfo(NetworkAgentInfo na) {
        if (na == null || na.networkInfo.getState() != State.CONNECTED) {
            return null;
        }
        NetworkAgentInfo bestNetwork = null;
        for (NetworkAgentInfo network : this.mNetworkAgentInfos.values()) {
            log("checking existed " + network.name());
            if (network == this.mLegacyTypeTracker.getNetworkForType(network.networkInfo.getType())) {
                LinkProperties curNetworkLp = network.linkProperties;
                LinkProperties newNetworkLp = na.linkProperties;
                if (network.networkInfo.getState() == State.CONNECTED && curNetworkLp != null && !TextUtils.isEmpty(curNetworkLp.getInterfaceName())) {
                    boolean isLpIdentical = curNetworkLp.keyEquals(newNetworkLp);
                    log("LinkProperties Identical are " + isLpIdentical);
                    NetworkSpecifier ns = network.networkCapabilities.getNetworkSpecifier();
                    NetworkSpecifier ns2 = na.networkCapabilities.getNetworkSpecifier();
                    if (ns != null && ns.satisfiedBy(ns2) && isLpIdentical) {
                        log("apparently satisfied");
                        bestNetwork = network;
                        break;
                    }
                }
                log("some key parameter is null, ignore");
            } else {
                log("not recorded, ignore");
            }
        }
        return bestNetwork;
    }

    public int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int currentScore, NetworkMisc networkMisc) {
        enforceConnectivityInternalPermission();
        NetworkAgentInfo nai = new NetworkAgentInfo(messenger, new AsyncChannel(), new Network(reserveNetId()), new NetworkInfo(networkInfo), new LinkProperties(linkProperties), new NetworkCapabilities(networkCapabilities), currentScore, this.mContext, this.mTrackerHandler, new NetworkMisc(networkMisc), this.mDefaultRequest, this);
        synchronized (this) {
            nai.networkMonitor.systemReady = this.mSystemReady;
        }
        addValidationLogs(nai.networkMonitor.getValidationLogs(), nai.network, networkInfo.getExtraInfo());
        log("registerNetworkAgent " + nai);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, nai));
        return nai.network.netId;
    }

    private void handleRegisterNetworkAgent(NetworkAgentInfo na) {
        log("Got NetworkAgent Messenger");
        NetworkAgentInfo identicalNai = getIdenticalActiveNetworkAgentInfo(na);
        if (identicalNai != null) {
            synchronized (identicalNai) {
                identicalNai.networkCapabilities.combineCapabilitiesWithNoSpecifiers(na.networkCapabilities);
            }
            na.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_DISCONNECTED);
            rematchNetworkAndRequests(identicalNai, ReapUnvalidatedNetworks.REAP, SystemClock.elapsedRealtime());
            return;
        }
        this.mNetworkAgentInfos.put(na.messenger, na);
        synchronized (this.mNetworkForNetId) {
            this.mNetworkForNetId.put(na.network.netId, na);
        }
        na.asyncChannel.connect(this.mContext, this.mTrackerHandler, na.messenger);
        NetworkInfo networkInfo = na.networkInfo;
        na.networkInfo = null;
        updateNetworkInfo(na, networkInfo);
        handleLteMobileDataStateChange(networkInfo);
    }

    private void updateLinkProperties(NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        LinkProperties newLp = networkAgent.linkProperties;
        int netId = networkAgent.network.netId;
        if (networkAgent.clatd != null) {
            networkAgent.clatd.fixupLinkProperties(oldLp);
        }
        updateInterfaces(newLp, oldLp, netId);
        updateMtu(newLp, oldLp);
        updateTcpBufferSizes(networkAgent);
        updateRoutes(newLp, oldLp, netId);
        updateDnses(newLp, oldLp, netId);
        updateClat(newLp, oldLp, networkAgent);
        if (isDefaultNetwork(networkAgent)) {
            handleApplyDefaultProxy(newLp.getHttpProxy());
        } else {
            updateProxy(newLp, oldLp, networkAgent);
        }
        if (!Objects.equals(newLp, oldLp)) {
            notifyIfacesChangedForNetworkStats();
            notifyNetworkCallbacks(networkAgent, 524295);
        }
        this.mKeepaliveTracker.handleCheckKeepalivesStillValid(networkAgent);
    }

    private void updateClat(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        boolean wasRunningClat = nai.clatd != null ? nai.clatd.isStarted() : false;
        boolean shouldRunClat = Nat464Xlat.requiresClat(nai);
        if (!wasRunningClat && shouldRunClat) {
            nai.clatd = new Nat464Xlat(this.mContext, this.mNetd, this.mTrackerHandler, nai);
            nai.clatd.start();
        } else if (wasRunningClat && (shouldRunClat ^ 1) != 0) {
            nai.clatd.stop();
        }
    }

    private void updateInterfaces(LinkProperties newLp, LinkProperties oldLp, int netId) {
        CompareResult<String> interfaceDiff = new CompareResult();
        if (oldLp != null) {
            interfaceDiff = oldLp.compareAllInterfaceNames(newLp);
        } else if (newLp != null) {
            interfaceDiff.added = newLp.getAllInterfaceNames();
        }
        for (String iface : interfaceDiff.added) {
            try {
                log("Adding iface " + iface + " to network " + netId);
                this.mNetd.addInterfaceToNetwork(iface, netId);
            } catch (Exception e) {
                loge("Exception adding interface: " + e);
            }
        }
        for (String iface2 : interfaceDiff.removed) {
            try {
                log("Removing iface " + iface2 + " from network " + netId);
                this.mNetd.removeInterfaceFromNetwork(iface2, netId);
            } catch (Exception e2) {
                loge("Exception removing interface: " + e2);
            }
        }
    }

    private boolean updateRoutes(LinkProperties newLp, LinkProperties oldLp, int netId) {
        boolean z;
        CompareResult<RouteInfo> routeDiff = new CompareResult();
        if (oldLp != null) {
            routeDiff = oldLp.compareAllRoutes(newLp);
        }
        if (newLp != null) {
            routeDiff.added = newLp.getAllRoutes();
        }
        for (RouteInfo route : routeDiff.added) {
            if (!route.hasGateway()) {
                log("Adding Route [" + route + "] to network " + netId);
                try {
                    this.mNetd.addRoute(netId, route);
                } catch (Exception e) {
                    z = route.getDestination().getAddress() instanceof Inet4Address;
                    loge("Exception in addRoute for non-gateway: " + e);
                }
            }
        }
        for (RouteInfo route2 : routeDiff.added) {
            if (route2.hasGateway()) {
                log("Adding Route [" + route2 + "] to network " + netId);
                try {
                    this.mNetd.addRoute(netId, route2);
                } catch (Exception e2) {
                    z = route2.getGateway() instanceof Inet4Address;
                    loge("Exception in addRoute for gateway: " + e2);
                }
            }
        }
        for (RouteInfo route22 : routeDiff.removed) {
            log("Removing Route [" + route22 + "] from network " + netId);
            try {
                this.mNetd.removeRoute(netId, route22);
            } catch (Exception e22) {
                loge("Exception in removeRoute: " + e22);
            }
        }
        return routeDiff.added.isEmpty() ? routeDiff.removed.isEmpty() ^ 1 : true;
    }

    private void updateDnses(LinkProperties newLp, LinkProperties oldLp, int netId) {
        if (oldLp == null || !newLp.isIdenticalDnses(oldLp)) {
            Collection<InetAddress> dnses = newLp.getDnsServers();
            log("Setting DNS servers for network " + netId + " to " + dnses);
            try {
                this.mNetd.setDnsConfigurationForNetwork(netId, NetworkUtils.makeStrings(dnses), newLp.getDomains());
            } catch (Exception e) {
                loge("Exception in setDnsConfigurationForNetwork: " + e);
            }
            NetworkAgentInfo defaultNai = getDefaultNetwork();
            if (defaultNai != null && defaultNai.network.netId == netId) {
                setDefaultDnsSystemProperties(dnses);
            }
            flushVmDnsCache();
        }
    }

    private void setDefaultDnsSystemProperties(Collection<InetAddress> dnses) {
        int last = 0;
        for (InetAddress dns : dnses) {
            last++;
            setNetDnsProperty(last, dns.getHostAddress());
        }
        for (int i = last + 1; i <= this.mNumDnsEntries; i++) {
            setNetDnsProperty(i, "");
        }
        this.mNumDnsEntries = last;
    }

    private void setNetDnsProperty(int which, String value) {
        try {
            this.mSystemProperties.set("net.dns" + which, value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting unsupported net.dns property: ", e);
        }
    }

    private String getNetworkPermission(NetworkCapabilities nc) {
        if (!nc.hasCapability(13)) {
            return NetworkManagementService.PERMISSION_SYSTEM;
        }
        if (nc.hasCapability(18)) {
            return null;
        }
        return NetworkManagementService.PERMISSION_NETWORK;
    }

    private void updateCapabilities(int oldScore, NetworkAgentInfo nai, NetworkCapabilities networkCapabilities) {
        if (nai.everConnected && (nai.networkCapabilities.equalImmutableCapabilities(networkCapabilities) ^ 1) != 0) {
            Slog.wtf(TAG, "BUG: " + nai + " changed immutable capabilities: " + nai.networkCapabilities + " -> " + networkCapabilities);
        }
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        if (nai.lastValidated) {
            networkCapabilities2.addCapability(16);
        } else {
            networkCapabilities2.removeCapability(16);
        }
        if (nai.lastCaptivePortalDetected) {
            networkCapabilities2.addCapability(17);
        } else {
            networkCapabilities2.removeCapability(17);
        }
        if (nai.isBackgroundNetwork()) {
            networkCapabilities2.removeCapability(18);
        } else {
            networkCapabilities2.addCapability(18);
        }
        if (!Objects.equals(nai.networkCapabilities, networkCapabilities2)) {
            String oldPermission = getNetworkPermission(nai.networkCapabilities);
            String newPermission = getNetworkPermission(networkCapabilities2);
            if (!(Objects.equals(oldPermission, newPermission) || !nai.created || (nai.isVPN() ^ 1) == 0)) {
                try {
                    this.mNetd.setNetworkPermission(nai.network.netId, newPermission);
                } catch (RemoteException e) {
                    loge("Exception in setNetworkPermission: " + e);
                }
            }
            NetworkCapabilities prevNc = nai.networkCapabilities;
            synchronized (nai) {
                nai.networkCapabilities = networkCapabilities2;
            }
            if (nai.getCurrentScore() == oldScore && networkCapabilities2.equalRequestableCapabilities(prevNc)) {
                processListenRequests(nai, true);
            } else {
                rematchAllNetworksAndRequests(nai, oldScore);
                notifyNetworkCallbacks(nai, 524294);
            }
        }
    }

    private void sendUpdatedScoreToFactories(NetworkAgentInfo nai) {
        for (int i = 0; i < nai.numNetworkRequests(); i++) {
            NetworkRequest nr = nai.requestAt(i);
            if (!nr.isListen()) {
                sendUpdatedScoreToFactories(nr, nai.getCurrentScore());
            }
        }
    }

    protected void sendUpdatedScoreToFactories(NetworkRequest networkRequest, int score) {
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
        if (notificationType == 524290 && (nri.mPendingIntentSent ^ 1) != 0) {
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
        } catch (CanceledException e) {
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
            bundle.putParcelable(NetworkRequest.class.getSimpleName(), new NetworkRequest(nri.clientRequest != null ? nri.clientRequest : nri.request));
            Message msg = Message.obtain();
            if (!(notificationType == 524293 || notificationType == 524296)) {
                bundle.putParcelable(Network.class.getSimpleName(), networkAgent.network);
            }
            switch (notificationType) {
                case 524291:
                    msg.arg1 = arg1;
                    break;
                case 524294:
                    bundle.putParcelable(NetworkCapabilities.class.getSimpleName(), new NetworkCapabilities(networkAgent.networkCapabilities));
                    break;
                case 524295:
                    bundle.putParcelable(LinkProperties.class.getSimpleName(), new LinkProperties(networkAgent.linkProperties));
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

    private void teardownUnneededNetwork(NetworkAgentInfo nai) {
        if (nai.numRequestNetworkRequests() != 0) {
            for (int i = 0; i < nai.numNetworkRequests(); i++) {
                NetworkRequest nr = nai.requestAt(i);
                if (!nr.isListen()) {
                    loge("Dead network still had at least " + nr);
                    break;
                }
            }
        }
        nai.asyncChannel.disconnect();
    }

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

    protected void makeDefault(NetworkAgentInfo newNetwork) {
        log("Switching to new default network: " + newNetwork);
        setupDataActivityTracking(newNetwork);
        try {
            this.mNetd.setDefaultNetId(newNetwork.network.netId);
        } catch (Exception e) {
            loge("Exception setting default network :" + e);
        }
        notifyLockdownVpn(newNetwork);
        handleApplyDefaultProxy(newNetwork.linkProperties.getHttpProxy());
        updateTcpBufferSizes(newNetwork);
        setDefaultDnsSystemProperties(newNetwork.linkProperties.getDnsServers());
    }

    private void processListenRequests(NetworkAgentInfo nai, boolean capabilitiesChanged) {
        NetworkRequest nr;
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            nr = nri.request;
            if (nr.isListen() && nai.isSatisfyingRequest(nr.requestId) && (nai.satisfies(nr) ^ 1) != 0) {
                nai.removeRequest(nri.request.requestId);
                callCallbackForRequest(nri, nai, 524292, 0);
            }
        }
        if (capabilitiesChanged) {
            notifyNetworkCallbacks(nai, 524294);
        }
        for (NetworkRequestInfo nri2 : this.mNetworkRequests.values()) {
            nr = nri2.request;
            if (nr.isListen() && nai.satisfies(nr) && (nai.isSatisfyingRequest(nr.requestId) ^ 1) != 0) {
                nai.addRequest(nr);
                notifyNetworkAvailable(nai, nri2);
            }
        }
    }

    private void rematchNetworkAndRequests(NetworkAgentInfo newNetwork, ReapUnvalidatedNetworks reapUnvalidatedNetworks, long now) {
        if (newNetwork.everConnected) {
            boolean keep = newNetwork.isVPN();
            boolean isNewDefault = false;
            NetworkAgentInfo oldDefaultNetwork = null;
            boolean wasBackgroundNetwork = newNetwork.isBackgroundNetwork();
            int score = newNetwork.getCurrentScore();
            log("rematching " + newNetwork.name());
            int isDisabledMobileNetwork;
            if (newNetwork.networkInfo.getType() != 0 || (this.mTelephonyManager.getDataEnabled() ^ 1) == 0) {
                isDisabledMobileNetwork = 0;
            } else {
                isDisabledMobileNetwork = HwTelephonyManager.getDefault().isVSimEnabled() ^ 1;
            }
            ArrayList<NetworkAgentInfo> affectedNetworks = new ArrayList();
            ArrayList<NetworkRequestInfo> addedRequests = new ArrayList();
            NetworkCapabilities nc = newNetwork.networkCapabilities;
            log(" network has: " + nc);
            for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                if (!nri.request.isListen()) {
                    NetworkAgentInfo currentNetwork = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                    boolean satisfies = newNetwork.satisfies(nri.request);
                    if (this.mDefaultRequest.requestId == nri.request.requestId && isDisabledMobileNetwork != 0) {
                        log("mobile net can't satisfy default network request when mobile data disabled");
                        satisfies = false;
                    }
                    if (newNetwork == currentNetwork && satisfies) {
                        log("Network " + newNetwork.name() + " was already satisfying" + " request " + nri.request.requestId + ". No change.");
                        keep = true;
                    } else {
                        if (!satisfies) {
                            if (!checkNetworkSupportBip(newNetwork, nri.request)) {
                                if (newNetwork.isSatisfyingRequest(nri.request.requestId)) {
                                    log("Network " + newNetwork.name() + " stopped satisfying" + " request " + nri.request.requestId);
                                    newNetwork.removeRequest(nri.request.requestId);
                                    if (currentNetwork == newNetwork) {
                                        this.mNetworkForRequestId.remove(nri.request.requestId);
                                        sendUpdatedScoreToFactories(nri.request, 0);
                                    } else {
                                        Slog.wtf(TAG, "BUG: Removing request " + nri.request.requestId + " from " + newNetwork.name() + " without updating mNetworkForRequestId or factories!");
                                    }
                                    callCallbackForRequest(nri, newNetwork, 524292, 0);
                                }
                            }
                        }
                        log("currentScore = " + (currentNetwork != null ? currentNetwork.getCurrentScore() : 0) + ", newScore = " + score);
                        if (currentNetwork == null || currentNetwork.getCurrentScore() < score) {
                            log("rematch for " + newNetwork.name());
                            if (currentNetwork != null) {
                                log("   accepting network in place of " + currentNetwork.name());
                                currentNetwork.removeRequest(nri.request.requestId);
                                currentNetwork.lingerRequest(nri.request, now, (long) this.mLingerDelayMs);
                                affectedNetworks.add(currentNetwork);
                            } else {
                                log("   accepting network in place of null");
                            }
                            newNetwork.unlingerRequest(nri.request);
                            this.mNetworkForRequestId.put(nri.request.requestId, newNetwork);
                            if (!newNetwork.addRequest(nri.request)) {
                                Slog.wtf(TAG, "BUG: " + newNetwork.name() + " already has " + nri.request);
                            }
                            addedRequests.add(nri);
                            keep = true;
                            sendUpdatedScoreToFactories(nri.request, score);
                            if (isDefaultRequest(nri)) {
                                isNewDefault = true;
                                oldDefaultNetwork = currentNetwork;
                                if (currentNetwork != null) {
                                    this.mLingerMonitor.noteLingerDefaultNetwork(currentNetwork, newNetwork);
                                }
                            }
                        }
                    }
                }
            }
            if (NetworkFactory.isDualCellDataEnable()) {
                log("isDualCellDataEnable is true so keep is true");
                keep = true;
            }
            if (isNewDefault) {
                makeDefault(newNetwork);
                logDefaultNetworkEvent(newNetwork, oldDefaultNetwork);
                synchronized (this) {
                    if (this.mNetTransitionWakeLock.isHeld()) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8, this.mNetTransitionWakeLockSerialNumber, 0), 1000);
                    }
                }
            }
            if (!newNetwork.networkCapabilities.equalRequestableCapabilities(nc)) {
                Slog.wtf(TAG, String.format("BUG: %s changed requestable capabilities during rematch: %s -> %s", new Object[]{nc, newNetwork.networkCapabilities}));
            }
            if (newNetwork.getCurrentScore() != score) {
                Slog.wtf(TAG, String.format("BUG: %s changed score during rematch: %d -> %d", new Object[]{Integer.valueOf(score), Integer.valueOf(newNetwork.getCurrentScore())}));
            }
            if (wasBackgroundNetwork != newNetwork.isBackgroundNetwork()) {
                updateCapabilities(score, newNetwork, newNetwork.networkCapabilities);
            } else {
                processListenRequests(newNetwork, false);
            }
            for (NetworkRequestInfo nri2 : addedRequests) {
                notifyNetworkAvailable(newNetwork, nri2);
            }
            for (NetworkAgentInfo nai : affectedNetworks) {
                if (ENABLE_WIFI_LTE_CE && newNetwork.networkInfo.getType() == 1 && nai.networkInfo.getType() == 0) {
                    nai.unlinger();
                    log("unlinger mobile network");
                } else {
                    updateLingerState(nai, now);
                }
            }
            updateLingerState(newNetwork, now);
            if (isNewDefault) {
                if (oldDefaultNetwork != null) {
                    this.mLegacyTypeTracker.remove(oldDefaultNetwork.networkInfo.getType(), oldDefaultNetwork, true);
                }
                this.mDefaultInetConditionPublished = newNetwork.lastValidated ? 100 : 0;
                this.mLegacyTypeTracker.add(newNetwork.networkInfo.getType(), newNetwork);
                notifyLockdownVpn(newNetwork);
            }
            if (keep) {
                try {
                    IBatteryStats bs = BatteryStatsService.getService();
                    int type = newNetwork.networkInfo.getType();
                    String baseIface = newNetwork.linkProperties.getInterfaceName();
                    bs.noteNetworkInterfaceType(baseIface, type);
                    for (LinkProperties stacked : newNetwork.linkProperties.getStackedLinks()) {
                        String stackedIface = stacked.getInterfaceName();
                        bs.noteNetworkInterfaceType(stackedIface, type);
                        NetworkStatsFactory.noteStackedIface(stackedIface, baseIface);
                    }
                } catch (RemoteException e) {
                }
                for (int i = 0; i < newNetwork.numNetworkRequests(); i++) {
                    NetworkRequest nr = newNetwork.requestAt(i);
                    if (nr.legacyType != -1 && nr.isRequest()) {
                        this.mLegacyTypeTracker.add(nr.legacyType, newNetwork);
                    }
                }
                if (newNetwork.isVPN()) {
                    this.mLegacyTypeTracker.add(17, newNetwork);
                }
            }
            if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
                    if (unneeded(nai2, UnneededFor.TEARDOWN)) {
                        if (nai2.getLingerExpiry() > 0) {
                            updateLingerState(nai2, now);
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
                                log("isWifiConnected==" + isWifiConnected);
                                Object obj = (isWifiConnected && nai2.networkInfo.getType() == 0) ? 1 : null;
                                if (obj == null) {
                                    teardownUnneededNetwork(nai2);
                                }
                            } else {
                                teardownUnneededNetwork(nai2);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void rematchAllNetworksAndRequests(NetworkAgentInfo changed, int oldScore) {
        long now = SystemClock.elapsedRealtime();
        if (changed == null || oldScore >= changed.getCurrentScore()) {
            NetworkAgentInfo[] nais = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[this.mNetworkAgentInfos.size()]);
            Arrays.sort(nais);
            for (NetworkAgentInfo nai : nais) {
                ReapUnvalidatedNetworks reapUnvalidatedNetworks;
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
        if (this.mLockdownTracker == null) {
            return;
        }
        if (nai == null || !nai.isVPN()) {
            this.mLockdownTracker.onNetworkInfoChanged();
        } else {
            this.mLockdownTracker.onVpnStateChanged(nai.networkInfo);
        }
    }

    private void updateNetworkInfo(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        State state = newInfo.getState();
        int oldScore = networkAgent.getCurrentScore();
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        notifyLockdownVpn(networkAgent);
        if (oldInfo == null || oldInfo.getState() != state) {
            log(networkAgent.name() + " EVENT_NETWORK_INFO_CHANGED, going from " + (oldInfo == null ? "null" : oldInfo.getState()) + " to " + state);
            if (!ENABLE_WIFI_LTE_CE) {
                enableDefaultTypeApnWhenWifiConnectionStateChanged(state, newInfo.getType());
            }
            enableDefaultTypeApnWhenBlueToothTetheringStateChanged(networkAgent, newInfo);
            if (!networkAgent.created && (state == State.CONNECTED || (state == State.CONNECTING && networkAgent.isVPN()))) {
                networkAgent.networkCapabilities.addCapability(18);
                try {
                    if (networkAgent.isVPN()) {
                        boolean z;
                        INetworkManagementService iNetworkManagementService = this.mNetd;
                        int i = networkAgent.network.netId;
                        boolean isEmpty = networkAgent.linkProperties.getDnsServers().isEmpty() ^ 1;
                        if (networkAgent.networkMisc != null) {
                            z = networkAgent.networkMisc.allowBypass ^ 1;
                        } else {
                            z = true;
                        }
                        iNetworkManagementService.createVirtualNetwork(i, isEmpty, z);
                    } else {
                        this.mNetd.createPhysicalNetwork(networkAgent.network.netId, getNetworkPermission(networkAgent.networkCapabilities));
                    }
                    networkAgent.created = true;
                } catch (Exception e) {
                    loge("Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
                    return;
                }
            }
            if (!networkAgent.everConnected && state == State.CONNECTED) {
                networkAgent.everConnected = true;
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
            } else if (state == State.DISCONNECTED) {
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
                }
            } else if ((oldInfo != null && oldInfo.getState() == State.SUSPENDED) || state == State.SUSPENDED) {
                int i2;
                if (networkAgent.getCurrentScore() != oldScore) {
                    rematchAllNetworksAndRequests(networkAgent, oldScore);
                }
                if (state == State.SUSPENDED) {
                    i2 = 524299;
                } else {
                    i2 = 524300;
                }
                notifyNetworkCallbacks(networkAgent, i2);
                this.mLegacyTypeTracker.update(networkAgent);
            }
            if (!ENABLE_WIFI_LTE_CE) {
                hintUserSwitchToMobileWhileWifiDisconnected(state, newInfo.getType());
            }
            return;
        }
        if (oldInfo.isRoaming() != newInfo.isRoaming()) {
            log("roaming status changed, notifying NetworkStatsService");
            notifyIfacesChangedForNetworkStats();
        } else {
            log("ignoring duplicate network state non-change");
        }
    }

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

    protected void notifyNetworkAvailable(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        this.mHandler.removeMessages(20, nri);
        if (nri.mPendingIntent != null) {
            sendPendingIntentForRequest(nri, nai, 524290);
            return;
        }
        callCallbackForRequest(nri, nai, 524290, 0);
        if (nai.networkInfo.getState() == State.SUSPENDED) {
            callCallbackForRequest(nri, nai, 524299, 0);
        }
        callCallbackForRequest(nri, nai, 524294, 0);
        callCallbackForRequest(nri, nai, 524295, 0);
    }

    private void sendLegacyNetworkBroadcast(NetworkAgentInfo nai, DetailedState state, int type) {
        NetworkInfo info = new NetworkInfo(nai.networkInfo);
        info.setType(type);
        if (state != DetailedState.DISCONNECTED) {
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

    protected void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType, int arg1) {
        log("notifyType " + notifyTypeToName(notifyType) + " for " + networkAgent.name());
        for (int i = 0; i < networkAgent.numNetworkRequests(); i++) {
            NetworkRequestInfo nri = (NetworkRequestInfo) this.mNetworkRequests.get(networkAgent.requestAt(i));
            if (nri.mPendingIntent == null) {
                callCallbackForRequest(nri, networkAgent, notifyType, arg1);
            } else {
                sendPendingIntentForRequest(nri, networkAgent, notifyType);
            }
        }
    }

    protected void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType) {
        notifyNetworkCallbacks(networkAgent, notifyType, 0);
    }

    private String notifyTypeToName(int notifyType) {
        switch (notifyType) {
            case 524289:
                return "PRECHECK";
            case 524290:
                return "AVAILABLE";
            case 524291:
                return "LOSING";
            case 524292:
                return "LOST";
            case 524293:
                return "UNAVAILABLE";
            case 524294:
                return "CAP_CHANGED";
            case 524295:
                return "IP_CHANGED";
            case 524296:
                return "RELEASED";
            default:
                return "UNKNOWN";
        }
    }

    private void notifyIfacesChangedForNetworkStats() {
        try {
            this.mStatsService.forceUpdateIfaces();
        } catch (Exception e) {
        }
    }

    public boolean addVpnAddress(String address, int prefixLength) {
        boolean addAddress;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            addAddress = ((Vpn) this.mVpns.get(user)).addAddress(address, prefixLength);
        }
        return addAddress;
    }

    public boolean removeVpnAddress(String address, int prefixLength) {
        boolean removeAddress;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            removeAddress = ((Vpn) this.mVpns.get(user)).removeAddress(address, prefixLength);
        }
        return removeAddress;
    }

    public boolean setUnderlyingNetworksForVpn(Network[] networks) {
        boolean success;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            success = ((Vpn) this.mVpns.get(user)).setUnderlyingNetworks(networks);
        }
        if (success) {
            notifyIfacesChangedForNetworkStats();
        }
        return success;
    }

    public String getCaptivePortalServerUrl() {
        return NetworkMonitor.getCaptivePortalServerHttpUrl(this.mContext);
    }

    public void startNattKeepalive(Network network, int intervalSeconds, Messenger messenger, IBinder binder, String srcAddr, int srcPort, String dstAddr) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), intervalSeconds, messenger, binder, srcAddr, srcPort, dstAddr, 4500);
    }

    public void stopKeepalive(Network network, int slot) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(528396, slot, 0, network));
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int userId = UserHandle.getCallingUserId();
            setAirplaneMode(false);
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                for (String tether : getTetheredIfaces()) {
                    untether(tether);
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_config_vpn")) {
                synchronized (this.mVpns) {
                    String alwaysOnPackage = getAlwaysOnVpnPackage(userId);
                    if (alwaysOnPackage != null) {
                        setAlwaysOnVpnPackage(userId, null, false);
                        setVpnPackageAuthorization(alwaysOnPackage, userId, false);
                    }
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
            Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", null);
        }
    }

    public NetworkMonitor createNetworkMonitor(Context context, Handler handler, NetworkAgentInfo nai, NetworkRequest defaultRequest) {
        return new NetworkMonitor(context, handler, nai, defaultRequest);
    }

    MultinetworkPolicyTracker createMultinetworkPolicyTracker(Context c, Handler h, Runnable r) {
        return new MultinetworkPolicyTracker(c, h, r);
    }

    public WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int cmd, Object obj) {
        return new WakeupMessage(c, h, s, cmd, 0, 0, obj);
    }

    private void logDefaultNetworkEvent(NetworkAgentInfo newNai, NetworkAgentInfo prevNai) {
        int newNetid = 0;
        int prevNetid = 0;
        int[] transports = new int[0];
        boolean hadIPv4 = false;
        boolean hadIPv6 = false;
        if (newNai != null) {
            newNetid = newNai.network.netId;
            transports = newNai.networkCapabilities.getTransportTypes();
        }
        if (prevNai != null) {
            prevNetid = prevNai.network.netId;
            LinkProperties lp = prevNai.linkProperties;
            hadIPv4 = lp.hasIPv4Address() ? lp.hasIPv4DefaultRoute() : false;
            hadIPv6 = lp.hasGlobalIPv6Address() ? lp.hasIPv6DefaultRoute() : false;
        }
        this.mMetricsLog.log(new DefaultNetworkEvent(newNetid, transports, prevNetid, hadIPv4, hadIPv6));
    }

    private void logNetworkEvent(NetworkAgentInfo nai, int evtype) {
        this.mMetricsLog.log(new NetworkEvent(nai.network.netId, evtype));
    }

    public SparseArray<Vpn> getmVpns() {
        SparseArray<Vpn> sparseArray;
        synchronized (this.mVpns) {
            sparseArray = this.mVpns;
        }
        return sparseArray;
    }
}
