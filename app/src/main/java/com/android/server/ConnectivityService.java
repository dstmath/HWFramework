package com.android.server;

import android.app.BroadcastOptions;
import android.app.Notification;
import android.app.Notification.Builder;
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
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.LinkProperties.CompareResult;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkMisc;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.net.metrics.DefaultNetworkEvent;
import android.net.metrics.NetworkEvent;
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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.security.KeyStore;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.LocalLog.ReadOnlyLocalLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.ProcessList;
import com.android.server.audio.AudioService;
import com.android.server.connectivity.DataConnectionStats;
import com.android.server.connectivity.KeepaliveTracker;
import com.android.server.connectivity.Nat464Xlat;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkDiagnostics;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.PacManager;
import com.android.server.connectivity.PermissionMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.connectivity.Vpn;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.net.LockdownVpnTracker;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.power.AbsPowerManagerService;
import com.android.server.wm.WindowManagerService.H;
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
    private static final String ACTION_PKT_CNT_SAMPLE_INTERVAL_ELAPSED = "android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED";
    private static final String ATTR_MCC = "mcc";
    private static final String ATTR_MNC = "mnc";
    private static final int CODE_REMOVE_LEGACYROUTE_TO_HOST = 1015;
    private static final boolean DBG = true;
    private static final String DEFAULT_TCP_BUFFER_SIZES = "4096,87380,110208,4096,16384,110208";
    private static final String DEFAULT_TCP_RWND_KEY = "net.tcp.default_init_rwnd";
    private static final String DESCRIPTOR = "android.net.wifi.INetworkManager";
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;
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
    private static final int EVENT_SYSTEM_READY = 25;
    private static final int EVENT_TIMEOUT_NETWORK_REQUEST = 20;
    private static final int EVENT_UNREGISTER_NETWORK_FACTORY = 23;
    private static final int INET_CONDITION_LOG_MAX_SIZE = 15;
    private static final boolean LOGD_BLOCKED_NETWORKINFO = true;
    private static final boolean LOGD_RULES = false;
    private static final int MAX_NETWORK_REQUESTS_PER_UID = 100;
    private static final int MAX_NETWORK_REQUEST_LOGS = 20;
    private static final int MAX_NET_ID = 65535;
    private static final int MAX_VALIDATION_LOGS = 10;
    private static final int MIN_NET_ID = 100;
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    private static final String NOTIFICATION_ID = "CaptivePortal.Notification";
    private static final int PROMPT_UNVALIDATED_DELAY_MS = 8000;
    private static final String PROVISIONING_URL_PATH = "/data/misc/radio/provisioning_urls.xml";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 60000;
    private static final boolean SAMPLE_DBG = false;
    private static final String TAG = "ConnectivityService";
    private static final String TAG_PROVISIONING_URL = "provisioningUrl";
    private static final String TAG_PROVISIONING_URLS = "provisioningUrls";
    private static final boolean VDBG = true;
    private static final SparseArray<String> sMagicDecoderRing = null;
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
    private boolean mLockdownEnabled;
    private LockdownVpnTracker mLockdownTracker;
    @GuardedBy("mRulesLock")
    private ArraySet<String> mMeteredIfaces;
    NetworkConfig[] mNetConfigs;
    @GuardedBy("mNetworkForNetId")
    private final SparseBooleanArray mNetIdInUse;
    private WakeLock mNetTransitionWakeLock;
    private String mNetTransitionWakeLockCausedBy;
    private int mNetTransitionWakeLockSerialNumber;
    private int mNetTransitionWakeLockTimeout;
    private INetworkManagementService mNetd;
    private final HashMap<Messenger, NetworkAgentInfo> mNetworkAgentInfos;
    private final HashMap<Messenger, NetworkFactoryInfo> mNetworkFactoryInfos;
    @GuardedBy("mNetworkForNetId")
    private final SparseArray<NetworkAgentInfo> mNetworkForNetId;
    private final SparseArray<NetworkAgentInfo> mNetworkForRequestId;
    private int mNetworkPreference;
    private final LocalLog mNetworkRequestInfoLogs;
    private final HashMap<NetworkRequest, NetworkRequestInfo> mNetworkRequests;
    int mNetworksDefined;
    private int mNextNetId;
    private int mNextNetworkRequestId;
    private int mNumDnsEntries;
    private NetworkInfo mP2pNetworkInfo;
    private PacManager mPacManager;
    private final WakeLock mPendingIntentWakeLock;
    private final PermissionMonitor mPermissionMonitor;
    private INetworkPolicyListener mPolicyListener;
    private INetworkPolicyManager mPolicyManager;
    List mProtectedNetworks;
    private final File mProvisioningUrlFile;
    private Object mProxyLock;
    private final int mReleasePendingIntentDelayMs;
    @GuardedBy("mRulesLock")
    private boolean mRestrictBackground;
    private Object mRulesLock;
    private final SettingsObserver mSettingsObserver;
    private INetworkStatsService mStatsService;
    private boolean mSystemReady;
    TelephonyManager mTelephonyManager;
    private boolean mTestMode;
    private Tethering mTethering;
    private final NetworkStateTrackerHandler mTrackerHandler;
    @GuardedBy("mRulesLock")
    private SparseIntArray mUidRules;
    @GuardedBy("mUidToNetworkRequestCount")
    private final SparseIntArray mUidToNetworkRequestCount;
    private BroadcastReceiver mUserIntentReceiver;
    private UserManager mUserManager;
    private final ArrayDeque<ValidationLog> mValidationLogs;
    @GuardedBy("mVpns")
    private final SparseArray<Vpn> mVpns;

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = ConnectivityService.VDBG;
            switch (msg.what) {
                case ConnectivityService.EVENT_CLEAR_NET_TRANSITION_WAKELOCK /*8*/:
                case ConnectivityService.EVENT_EXPIRE_NET_TRANSITION_WAKELOCK /*24*/:
                    synchronized (ConnectivityService.this) {
                        if (msg.arg1 == ConnectivityService.this.mNetTransitionWakeLockSerialNumber && ConnectivityService.this.mNetTransitionWakeLock.isHeld()) {
                            ConnectivityService.this.mNetTransitionWakeLock.release();
                            String causedBy = ConnectivityService.this.mNetTransitionWakeLockCausedBy;
                            if (msg.what == ConnectivityService.EVENT_EXPIRE_NET_TRANSITION_WAKELOCK) {
                                ConnectivityService.log("Failed to find a new network - expiring NetTransition Wakelock");
                                return;
                            }
                            StringBuilder append = new StringBuilder().append("NetTransition Wakelock (");
                            if (causedBy == null) {
                                causedBy = "unknown";
                            }
                            ConnectivityService.log(append.append(causedBy).append(" cleared because we found a replacement network").toString());
                            return;
                        }
                    }
                case ConnectivityService.EVENT_APPLY_GLOBAL_HTTP_PROXY /*9*/:
                    ConnectivityService.this.handleDeprecatedGlobalHttpProxy();
                case ConnectivityService.EVENT_PROXY_HAS_CHANGED /*16*/:
                    ConnectivityService.this.handleApplyDefaultProxy((ProxyInfo) msg.obj);
                case ConnectivityService.EVENT_REGISTER_NETWORK_FACTORY /*17*/:
                    ConnectivityService.this.handleRegisterNetworkFactory((NetworkFactoryInfo) msg.obj);
                case ConnectivityService.EVENT_REGISTER_NETWORK_AGENT /*18*/:
                    ConnectivityService.this.handleRegisterNetworkAgent((NetworkAgentInfo) msg.obj);
                case ConnectivityService.EVENT_REGISTER_NETWORK_REQUEST /*19*/:
                case ConnectivityService.EVENT_REGISTER_NETWORK_LISTENER /*21*/:
                    ConnectivityService.this.handleRegisterNetworkRequest((NetworkRequestInfo) msg.obj);
                case ConnectivityService.EVENT_RELEASE_NETWORK_REQUEST /*22*/:
                    ConnectivityService.this.handleReleaseNetworkRequest((NetworkRequest) msg.obj, msg.arg1);
                case ConnectivityService.EVENT_UNREGISTER_NETWORK_FACTORY /*23*/:
                    ConnectivityService.this.handleUnregisterNetworkFactory((Messenger) msg.obj);
                case ConnectivityService.EVENT_SYSTEM_READY /*25*/:
                    for (NetworkAgentInfo nai : ConnectivityService.this.mNetworkAgentInfos.values()) {
                        nai.networkMonitor.systemReady = ConnectivityService.VDBG;
                    }
                case ConnectivityService.EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT /*26*/:
                case ConnectivityService.EVENT_REGISTER_NETWORK_LISTENER_WITH_INTENT /*31*/:
                    ConnectivityService.this.handleRegisterNetworkRequestWithIntent(msg);
                case ConnectivityService.EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT /*27*/:
                    ConnectivityService.this.handleReleaseNetworkRequestWithIntent((PendingIntent) msg.obj, msg.arg1);
                case ConnectivityService.EVENT_SET_ACCEPT_UNVALIDATED /*28*/:
                    ConnectivityService connectivityService = ConnectivityService.this;
                    Network network = (Network) msg.obj;
                    boolean z2 = msg.arg1 != 0 ? ConnectivityService.VDBG : ConnectivityService.SAMPLE_DBG;
                    if (msg.arg2 == 0) {
                        z = ConnectivityService.SAMPLE_DBG;
                    }
                    connectivityService.handleSetAcceptUnvalidated(network, z2, z);
                case ConnectivityService.EVENT_CONFIGURE_MOBILE_DATA_ALWAYS_ON /*30*/:
                    ConnectivityService.this.handleMobileDataAlwaysOn();
                case 528395:
                    ConnectivityService.this.mKeepaliveTracker.handleStartKeepalive(msg);
                case 528396:
                    ConnectivityService.this.mKeepaliveTracker.handleStopKeepalive(ConnectivityService.this.getNetworkAgentInfoForNetwork((Network) msg.obj), msg.arg1, msg.arg2);
                default:
            }
        }
    }

    private class LegacyTypeTracker {
        private static final boolean DBG = true;
        private static final boolean VDBG = false;
        private ArrayList<NetworkAgentInfo>[] mTypeLists;

        public LegacyTypeTracker() {
            this.mTypeLists = new ArrayList[46];
        }

        public void addSupportedType(int type) {
            if (this.mTypeLists[type] != null) {
                throw new IllegalStateException("legacy list for type " + type + "already initialized");
            }
            this.mTypeLists[type] = new ArrayList();
        }

        public boolean isTypeSupported(int type) {
            return (!ConnectivityManager.isNetworkTypeValid(type) || this.mTypeLists[type] == null) ? ConnectivityService.SAMPLE_DBG : DBG;
        }

        public NetworkAgentInfo getNetworkForType(int type) {
            if (!isTypeSupported(type) || this.mTypeLists[type].isEmpty()) {
                return null;
            }
            return (NetworkAgentInfo) this.mTypeLists[type].get(ConnectivityService.DISABLED);
        }

        private void maybeLogBroadcast(NetworkAgentInfo nai, DetailedState state, int type, boolean isDefaultNetwork) {
            ConnectivityService.log("Sending " + state + " broadcast for type " + type + " " + nai.name() + " isDefaultNetwork=" + isDefaultNetwork);
        }

        public void add(int type, NetworkAgentInfo nai) {
            if (isTypeSupported(type)) {
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                if (!list.contains(nai)) {
                    list.add(nai);
                    boolean isDefaultNetwork = ConnectivityService.this.isDefaultNetwork(nai);
                    if (list.size() == ConnectivityService.ENABLED || isDefaultNetwork) {
                        maybeLogBroadcast(nai, DetailedState.CONNECTED, type, isDefaultNetwork);
                        ConnectivityService.this.sendLegacyNetworkBroadcast(nai, DetailedState.CONNECTED, type);
                    }
                }
            }
        }

        public void remove(int type, NetworkAgentInfo nai, boolean wasDefault) {
            ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
            if (list != null && !list.isEmpty()) {
                boolean wasFirstNetwork = ((NetworkAgentInfo) list.get(ConnectivityService.DISABLED)).equals(nai);
                if (list.remove(nai)) {
                    DetailedState state = DetailedState.DISCONNECTED;
                    if (wasFirstNetwork || wasDefault) {
                        maybeLogBroadcast(nai, state, type, wasDefault);
                        ConnectivityService.this.sendLegacyNetworkBroadcast(nai, state, type);
                    }
                    if (!list.isEmpty() && wasFirstNetwork) {
                        ConnectivityService.log("Other network available for type " + type + ", sending connected broadcast");
                        NetworkAgentInfo replacement = (NetworkAgentInfo) list.get(ConnectivityService.DISABLED);
                        maybeLogBroadcast(replacement, state, type, ConnectivityService.this.isDefaultNetwork(replacement));
                        ConnectivityService.this.sendLegacyNetworkBroadcast(replacement, state, type);
                    }
                }
            }
        }

        public void remove(NetworkAgentInfo nai, boolean wasDefault) {
            for (int type = ConnectivityService.DISABLED; type < this.mTypeLists.length; type += ConnectivityService.ENABLED) {
                remove(type, nai, wasDefault);
            }
        }

        public void update(NetworkAgentInfo nai) {
            boolean isDefault = ConnectivityService.this.isDefaultNetwork(nai);
            DetailedState state = nai.networkInfo.getDetailedState();
            for (int type = ConnectivityService.DISABLED; type < this.mTypeLists.length; type += ConnectivityService.ENABLED) {
                boolean isFirst;
                ArrayList<NetworkAgentInfo> list = this.mTypeLists[type];
                boolean contains = list != null ? list.contains(nai) : ConnectivityService.SAMPLE_DBG;
                if (list == null || list.size() <= 0) {
                    isFirst = ConnectivityService.SAMPLE_DBG;
                } else {
                    boolean z;
                    if (nai == list.get(ConnectivityService.DISABLED)) {
                        z = DBG;
                    } else {
                        z = ConnectivityService.DISABLED;
                    }
                    isFirst = z;
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
            for (type = ConnectivityService.DISABLED; type < this.mTypeLists.length; type += ConnectivityService.ENABLED) {
                if (this.mTypeLists[type] != null) {
                    pw.print(" " + type);
                }
            }
            pw.println();
            pw.println("Current state:");
            pw.increaseIndent();
            type = ConnectivityService.DISABLED;
            while (type < this.mTypeLists.length) {
                if (!(this.mTypeLists[type] == null || this.mTypeLists[type].size() == 0)) {
                    for (NetworkAgentInfo nai : this.mTypeLists[type]) {
                        pw.println(type + " " + naiToString(nai));
                    }
                }
                type += ConnectivityService.ENABLED;
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println();
        }
    }

    private static class NetworkFactoryInfo {
        public final AsyncChannel asyncChannel;
        public final Messenger messenger;
        public final String name;

        public NetworkFactoryInfo(String name, Messenger messenger, AsyncChannel asyncChannel) {
            this.name = name;
            this.messenger = messenger;
            this.asyncChannel = asyncChannel;
        }
    }

    private class NetworkRequestInfo implements DeathRecipient {
        private static final /* synthetic */ int[] -com-android-server-ConnectivityService$NetworkRequestTypeSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$server$ConnectivityService$NetworkRequestType;
        private final IBinder mBinder;
        final PendingIntent mPendingIntent;
        boolean mPendingIntentSent;
        final int mPid;
        private final NetworkRequestType mType;
        final int mUid;
        final Messenger messenger;
        final NetworkRequest request;

        private static /* synthetic */ int[] -getcom-android-server-ConnectivityService$NetworkRequestTypeSwitchesValues() {
            if (-com-android-server-ConnectivityService$NetworkRequestTypeSwitchesValues != null) {
                return -com-android-server-ConnectivityService$NetworkRequestTypeSwitchesValues;
            }
            int[] iArr = new int[NetworkRequestType.values().length];
            try {
                iArr[NetworkRequestType.LISTEN.ordinal()] = ConnectivityService.ENABLED;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[NetworkRequestType.REQUEST.ordinal()] = ConnectivityService.EVENT_CHANGE_MOBILE_DATA_ENABLED;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[NetworkRequestType.TRACK_DEFAULT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-ConnectivityService$NetworkRequestTypeSwitchesValues = iArr;
            return iArr;
        }

        NetworkRequestInfo(NetworkRequest r, PendingIntent pi, NetworkRequestType type) {
            this.request = r;
            this.mPendingIntent = pi;
            this.messenger = null;
            this.mBinder = null;
            this.mPid = ConnectivityService.getCallingPid();
            this.mUid = ConnectivityService.getCallingUid();
            this.mType = type;
            enforceRequestCountLimit();
        }

        NetworkRequestInfo(Messenger m, NetworkRequest r, IBinder binder, NetworkRequestType type) {
            this.messenger = m;
            this.request = r;
            this.mBinder = binder;
            this.mPid = ConnectivityService.getCallingPid();
            this.mUid = ConnectivityService.getCallingUid();
            this.mType = type;
            this.mPendingIntent = null;
            enforceRequestCountLimit();
            try {
                this.mBinder.linkToDeath(this, ConnectivityService.DISABLED);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        private void enforceRequestCountLimit() {
            synchronized (ConnectivityService.this.mUidToNetworkRequestCount) {
                int networkRequests = ConnectivityService.this.mUidToNetworkRequestCount.get(this.mUid, ConnectivityService.DISABLED) + ConnectivityService.ENABLED;
                if (networkRequests >= ConnectivityService.MIN_NET_ID) {
                    throw new IllegalArgumentException("Too many NetworkRequests filed");
                }
                ConnectivityService.this.mUidToNetworkRequestCount.put(this.mUid, networkRequests);
            }
        }

        private String typeString() {
            switch (-getcom-android-server-ConnectivityService$NetworkRequestTypeSwitchesValues()[this.mType.ordinal()]) {
                case ConnectivityService.ENABLED /*1*/:
                    return "Listen";
                case ConnectivityService.EVENT_CHANGE_MOBILE_DATA_ENABLED /*2*/:
                    return "Request";
                case H.REPORT_LOSING_FOCUS /*3*/:
                    return "Track default";
                default:
                    return "unknown type";
            }
        }

        void unlinkDeathRecipient() {
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, ConnectivityService.DISABLED);
            }
        }

        public void binderDied() {
            ConnectivityService.log("ConnectivityService NetworkRequestInfo binderDied(" + this.request + ", " + this.mBinder + ")");
            ConnectivityService.this.releaseNetworkRequest(this.request);
        }

        public boolean isRequest() {
            if (this.mType == NetworkRequestType.TRACK_DEFAULT || this.mType == NetworkRequestType.REQUEST) {
                return ConnectivityService.VDBG;
            }
            return ConnectivityService.SAMPLE_DBG;
        }

        public String toString() {
            return typeString() + " from uid/pid:" + this.mUid + "/" + this.mPid + " for " + this.request + (this.mPendingIntent == null ? "" : " to trigger " + this.mPendingIntent);
        }
    }

    private enum NetworkRequestType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.ConnectivityService.NetworkRequestType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.ConnectivityService.NetworkRequestType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.NetworkRequestType.<clinit>():void");
        }
    }

    private class NetworkStateTrackerHandler extends Handler {
        final /* synthetic */ ConnectivityService this$0;

        public NetworkStateTrackerHandler(ConnectivityService this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
        }

        private boolean maybeHandleAsyncChannelMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    this.this$0.handleAsyncChannelHalfConnect(msg);
                    break;
                case 69635:
                    NetworkAgentInfo nai = (NetworkAgentInfo) this.this$0.mNetworkAgentInfos.get(msg.replyTo);
                    if (nai != null) {
                        nai.asyncChannel.disconnect();
                        break;
                    }
                    break;
                case 69636:
                    this.this$0.handleAsyncChannelDisconnected(msg);
                    break;
                default:
                    return ConnectivityService.SAMPLE_DBG;
            }
            return ConnectivityService.VDBG;
        }

        private void maybeHandleNetworkAgentMessage(Message msg) {
            NetworkAgentInfo nai = (NetworkAgentInfo) this.this$0.mNetworkAgentInfos.get(msg.replyTo);
            if (nai == null) {
                Object[] objArr = new Object[ConnectivityService.ENABLED];
                objArr[ConnectivityService.DISABLED] = (String) ConnectivityService.sMagicDecoderRing.get(msg.what, Integer.toString(msg.what));
                ConnectivityService.log(String.format("%s from unknown NetworkAgent", objArr));
                return;
            }
            switch (msg.what) {
                case 528385:
                    this.this$0.updateNetworkInfo(nai, msg.obj);
                    break;
                case 528386:
                    NetworkCapabilities networkCapabilities = msg.obj;
                    if (networkCapabilities.hasCapability(ConnectivityService.EVENT_REGISTER_NETWORK_FACTORY) || networkCapabilities.hasCapability(ConnectivityService.EVENT_PROXY_HAS_CHANGED)) {
                        Slog.wtf(ConnectivityService.TAG, "BUG: " + nai + " has CS-managed capability.");
                    }
                    if (nai.everConnected && !nai.networkCapabilities.equalImmutableCapabilities(networkCapabilities)) {
                        Slog.wtf(ConnectivityService.TAG, "BUG: " + nai + " changed immutable capabilities: " + nai.networkCapabilities + " -> " + networkCapabilities);
                    }
                    this.this$0.updateCapabilities(nai, networkCapabilities);
                    break;
                case 528387:
                    ConnectivityService.log("Update of LinkProperties for " + nai.name() + "; created=" + nai.created + "; everConnected=" + nai.everConnected);
                    LinkProperties oldLp = nai.linkProperties;
                    synchronized (nai) {
                        nai.linkProperties = (LinkProperties) msg.obj;
                        break;
                    }
                    if (nai.everConnected) {
                        this.this$0.updateLinkProperties(nai, oldLp);
                        break;
                    }
                    break;
                case 528388:
                    Integer score = msg.obj;
                    if (score != null) {
                        this.this$0.updateNetworkScore(nai, score.intValue());
                        break;
                    }
                    break;
                case 528389:
                    try {
                        this.this$0.mNetd.addVpnUidRanges(nai.network.netId, (UidRange[]) msg.obj);
                        break;
                    } catch (Exception e) {
                        ConnectivityService.loge("Exception in addVpnUidRanges: " + e);
                        break;
                    }
                case 528390:
                    try {
                        this.this$0.mNetd.removeVpnUidRanges(nai.network.netId, (UidRange[]) msg.obj);
                        break;
                    } catch (Exception e2) {
                        ConnectivityService.loge("Exception in removeVpnUidRanges: " + e2);
                        break;
                    }
                case 528392:
                    if (nai.everConnected && !nai.networkMisc.explicitlySelected) {
                        ConnectivityService.loge("ERROR: already-connected network explicitly selected.");
                    }
                    nai.networkMisc.explicitlySelected = ConnectivityService.VDBG;
                    nai.networkMisc.acceptUnvalidated = ((Boolean) msg.obj).booleanValue();
                    break;
                case 528397:
                    this.this$0.mKeepaliveTracker.handleEventPacketKeepalive(nai, msg);
                    break;
                case AbstractConnectivityService.EVENT_SET_EXPLICITLY_UNSELECTED /*528585*/:
                    this.this$0.setExplicitlyUnselected((NetworkAgentInfo) this.this$0.mNetworkAgentInfos.get(msg.replyTo));
                    break;
                case AbstractConnectivityService.EVENT_UPDATE_NETWORK_CONCURRENTLY /*528586*/:
                    this.this$0.updateNetworkConcurrently((NetworkAgentInfo) this.this$0.mNetworkAgentInfos.get(msg.replyTo), (NetworkInfo) msg.obj);
                    break;
                case AbstractConnectivityService.EVENT_TRIGGER_ROAMING_NETWORK_MONITOR /*528587*/:
                    this.this$0.triggerRoamingNetworkMonitor((NetworkAgentInfo) this.this$0.mNetworkAgentInfos.get(msg.replyTo));
                    break;
            }
        }

        private boolean maybeHandleNetworkMonitorMessage(Message msg) {
            NetworkAgentInfo nai;
            switch (msg.what) {
                case 528485:
                    nai = (NetworkAgentInfo) this.this$0.mNetworkAgentInfos.get(msg.replyTo);
                    if (nai != null) {
                        this.this$0.rematchNetworkAndRequests(nai, ReapUnvalidatedNetworks.DONT_REAP);
                        break;
                    }
                    ConnectivityService.loge("EVENT_REMATCH_NETWORK_AND_REQUESTS from unknown NetworkAgent");
                    break;
                case NetworkMonitor.EVENT_NETWORK_TESTED /*532482*/:
                    synchronized (this.this$0.mNetworkForNetId) {
                        nai = (NetworkAgentInfo) this.this$0.mNetworkForNetId.get(msg.arg2);
                        break;
                    }
                    if (nai != null) {
                        String str;
                        boolean valid = msg.arg1 == 0 ? ConnectivityService.VDBG : ConnectivityService.SAMPLE_DBG;
                        StringBuilder append = new StringBuilder().append(nai.name()).append(" validation ").append(valid ? "passed" : "failed");
                        if (msg.obj == null) {
                            str = "";
                        } else {
                            str = " with redirect to " + ((String) msg.obj);
                        }
                        ConnectivityService.log(append.append(str).toString());
                        if (valid != nai.lastValidated) {
                            int oldScore = nai.getCurrentScore();
                            nai.lastValidated = valid;
                            nai.everValidated |= valid;
                            this.this$0.updateCapabilities(nai, nai.networkCapabilities);
                            if (oldScore != nai.getCurrentScore()) {
                                this.this$0.sendUpdatedScoreToFactories(nai);
                            }
                        }
                        this.this$0.updateInetCondition(nai);
                        if (!this.this$0.reportPortalNetwork(nai, msg.arg1)) {
                            Bundle redirectUrlBundle = new Bundle();
                            redirectUrlBundle.putString(NetworkAgent.REDIRECT_URL_KEY, (String) msg.obj);
                            nai.asyncChannel.sendMessage(528391, valid ? ConnectivityService.ENABLED : ConnectivityService.EVENT_CHANGE_MOBILE_DATA_ENABLED, ConnectivityService.DISABLED, redirectUrlBundle);
                            break;
                        }
                    }
                    break;
                case NetworkMonitor.EVENT_NETWORK_LINGER_COMPLETE /*532485*/:
                    nai = (NetworkAgentInfo) msg.obj;
                    if (this.this$0.isLiveNetworkAgent(nai, msg.what)) {
                        this.this$0.handleLingerComplete(nai);
                        break;
                    }
                    break;
                case NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION /*532490*/:
                    int netId = msg.arg2;
                    boolean visible = msg.arg1 != 0 ? ConnectivityService.VDBG : ConnectivityService.SAMPLE_DBG;
                    synchronized (this.this$0.mNetworkForNetId) {
                        nai = (NetworkAgentInfo) this.this$0.mNetworkForNetId.get(netId);
                        break;
                    }
                    if (!(nai == null || visible == nai.lastCaptivePortalDetected)) {
                        nai.lastCaptivePortalDetected = visible;
                        nai.everCaptivePortalDetected |= visible;
                        this.this$0.updateCapabilities(nai, nai.networkCapabilities);
                    }
                    if (visible) {
                        if (nai != null) {
                            this.this$0.setProvNotificationVisibleIntent(ConnectivityService.VDBG, netId, NotificationType.SIGN_IN, nai.networkInfo.getType(), nai.networkInfo.getExtraInfo(), (PendingIntent) msg.obj, nai.networkMisc.explicitlySelected);
                            break;
                        }
                        ConnectivityService.loge("EVENT_PROVISIONING_NOTIFICATION from unknown NetworkMonitor");
                        break;
                    }
                    this.this$0.setProvNotificationVisibleIntent(ConnectivityService.SAMPLE_DBG, netId, null, ConnectivityService.DISABLED, null, null, ConnectivityService.SAMPLE_DBG);
                    break;
                default:
                    return ConnectivityService.SAMPLE_DBG;
            }
            return ConnectivityService.VDBG;
        }

        public void handleMessage(Message msg) {
            if (!maybeHandleAsyncChannelMessage(msg) && !maybeHandleNetworkMonitorMessage(msg)) {
                maybeHandleNetworkAgentMessage(msg);
            }
        }
    }

    private enum NotificationType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.ConnectivityService.NotificationType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.ConnectivityService.NotificationType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.NotificationType.<clinit>():void");
        }
    }

    private enum ReapUnvalidatedNetworks {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.ConnectivityService.ReapUnvalidatedNetworks.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.ConnectivityService.ReapUnvalidatedNetworks.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.ReapUnvalidatedNetworks.<clinit>():void");
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap;

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mUriEventMap = new HashMap();
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, ConnectivityService.SAMPLE_DBG, this);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.ConnectivityService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.ConnectivityService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.ConnectivityService.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addValidationLogs(ReadOnlyLocalLog log, Network network, String networkExtraInfo) {
        synchronized (this.mValidationLogs) {
            while (true) {
                if (this.mValidationLogs.size() >= MAX_VALIDATION_LOGS) {
                    this.mValidationLogs.removeLast();
                } else {
                    this.mValidationLogs.addFirst(new ValidationLog(network, networkExtraInfo, log));
                }
            }
        }
    }

    protected HandlerThread createHandlerThread() {
        return new HandlerThread("ConnectivityServiceThread");
    }

    public ConnectivityService(Context context, INetworkManagementService netManager, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        int i;
        boolean equals;
        this.mVpns = new SparseArray();
        this.mRulesLock = new Object();
        this.mUidRules = new SparseIntArray();
        this.mMeteredIfaces = new ArraySet();
        this.mDefaultInetConditionPublished = DISABLED;
        this.mNetTransitionWakeLockCausedBy = "";
        this.mDefaultProxy = null;
        this.mProxyLock = new Object();
        this.mDefaultProxyDisabled = SAMPLE_DBG;
        this.mGlobalProxy = null;
        this.mPacManager = null;
        this.mNextNetId = MIN_NET_ID;
        this.mNextNetworkRequestId = ENABLED;
        this.mP2pNetworkInfo = new NetworkInfo(13, DISABLED, "WIFI_P2P", "");
        this.mNetworkRequestInfoLogs = new LocalLog(MAX_NETWORK_REQUEST_LOGS);
        this.mValidationLogs = new ArrayDeque(MAX_VALIDATION_LOGS);
        this.mLegacyTypeTracker = new LegacyTypeTracker();
        this.mDataActivityObserver = new BaseNetworkObserver() {
            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
                ConnectivityService.this.sendDataActivityBroadcast(Integer.parseInt(label), active, tsNanos);
            }
        };
        this.mPolicyListener = new Stub() {
            public void onUidRulesChanged(int uid, int uidRules) {
                synchronized (ConnectivityService.this.mRulesLock) {
                    if (ConnectivityService.this.mUidRules.get(uid, ConnectivityService.DISABLED) == uidRules) {
                        return;
                    }
                    if (uidRules == 0) {
                        ConnectivityService.this.mUidRules.delete(uid);
                    } else {
                        ConnectivityService.this.mUidRules.put(uid, uidRules);
                    }
                }
            }

            public void onMeteredIfacesChanged(String[] meteredIfaces) {
                synchronized (ConnectivityService.this.mRulesLock) {
                    ConnectivityService.this.mMeteredIfaces.clear();
                    int length = meteredIfaces.length;
                    for (int i = ConnectivityService.DISABLED; i < length; i += ConnectivityService.ENABLED) {
                        ConnectivityService.this.mMeteredIfaces.add(meteredIfaces[i]);
                    }
                }
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                synchronized (ConnectivityService.this.mRulesLock) {
                    ConnectivityService.this.mRestrictBackground = restrictBackground;
                }
                if (restrictBackground) {
                    ConnectivityService.log("onRestrictBackgroundChanged(true): disabling tethering");
                    ConnectivityService.this.mTethering.untetherAll();
                }
            }

            public void onRestrictBackgroundWhitelistChanged(int uid, boolean whitelisted) {
            }

            public void onRestrictBackgroundBlacklistChanged(int uid, boolean blacklisted) {
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
        this.mNetworkFactoryInfos = new HashMap();
        this.mNetworkRequests = new HashMap();
        this.mUidToNetworkRequestCount = new SparseIntArray();
        this.mNetworkForRequestId = new SparseArray();
        this.mNetworkForNetId = new SparseArray();
        this.mNetIdInUse = new SparseBooleanArray();
        this.mNetworkAgentInfos = new HashMap();
        this.mBlockedAppUids = new HashSet();
        log("ConnectivityService starting up");
        this.mDefaultRequest = createInternetRequestForTransport(-1);
        NetworkRequestInfo defaultNRI = new NetworkRequestInfo(null, this.mDefaultRequest, new Binder(), NetworkRequestType.REQUEST);
        this.mNetworkRequests.put(this.mDefaultRequest, defaultNRI);
        this.mNetworkRequestInfoLogs.log("REGISTER " + defaultNRI);
        this.mDefaultMobileDataRequest = createInternetRequestForTransport(DISABLED);
        this.mHandlerThread = createHandlerThread();
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mTrackerHandler = new NetworkStateTrackerHandler(this, this.mHandlerThread.getLooper());
        if (TextUtils.isEmpty(SystemProperties.get("net.hostname"))) {
            String id = Secure.getString(context.getContentResolver(), "android_id");
            if (id != null && id.length() > 0) {
                String name = SystemProperties.get("ro.config.marketing_name");
                if (TextUtils.isEmpty(name)) {
                    name = Build.MODEL.replace(" ", "_");
                    if (name != null && name.length() > EVENT_REGISTER_NETWORK_AGENT) {
                        name = name.substring(DISABLED, EVENT_REGISTER_NETWORK_AGENT);
                    }
                    name = name + "-" + id;
                    if (name != null && name.length() > EVENT_SYSTEM_READY) {
                        name = name.substring(DISABLED, EVENT_SYSTEM_READY);
                    }
                } else {
                    name = name.replace(" ", "_");
                    if (name.length() > EVENT_SYSTEM_READY) {
                        name = name.substring(DISABLED, EVENT_SYSTEM_READY);
                    }
                }
                SystemProperties.set("net.hostname", name);
            }
        }
        this.mReleasePendingIntentDelayMs = Secure.getInt(context.getContentResolver(), "connectivity_release_pending_intent_delay_ms", 5000);
        this.mContext = (Context) checkNotNull(context, "missing Context");
        this.mNetd = (INetworkManagementService) checkNotNull(netManager, "missing INetworkManagementService");
        this.mStatsService = (INetworkStatsService) checkNotNull(statsService, "missing INetworkStatsService");
        this.mPolicyManager = (INetworkPolicyManager) checkNotNull(policyManager, "missing INetworkPolicyManager");
        this.mKeyStore = KeyStore.getInstance();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        try {
            this.mPolicyManager.setConnectivityListener(this.mPolicyListener);
            this.mRestrictBackground = this.mPolicyManager.getRestrictBackground();
        } catch (RemoteException e) {
            loge("unable to register INetworkPolicyListener" + e);
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mNetTransitionWakeLock = powerManager.newWakeLock(ENABLED, TAG);
        this.mNetTransitionWakeLockTimeout = this.mContext.getResources().getInteger(17694735);
        this.mPendingIntentWakeLock = powerManager.newWakeLock(ENABLED, TAG);
        this.mNetConfigs = new NetworkConfig[46];
        boolean wifiOnly = SystemProperties.getBoolean("ro.radio.noril", SAMPLE_DBG);
        log("wifiOnly=" + wifiOnly);
        String[] naStrings = context.getResources().getStringArray(17235983);
        int length = naStrings.length;
        for (i = DISABLED; i < length; i += ENABLED) {
            String naString = naStrings[i];
            try {
                NetworkConfig n = new NetworkConfig(naString);
                log("naString=" + naString + " config=" + n);
                if (n.type > 45) {
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
                        this.mNetworksDefined += ENABLED;
                    }
                }
            } catch (Exception e2) {
            }
        }
        if (this.mNetConfigs[EVENT_REGISTER_NETWORK_FACTORY] == null) {
            this.mLegacyTypeTracker.addSupportedType(EVENT_REGISTER_NETWORK_FACTORY);
            this.mNetworksDefined += ENABLED;
        }
        log("mNetworksDefined=" + this.mNetworksDefined);
        this.mProtectedNetworks = new ArrayList();
        int[] protectedNetworks = context.getResources().getIntArray(17235984);
        length = protectedNetworks.length;
        for (i = DISABLED; i < length; i += ENABLED) {
            int p = protectedNetworks[i];
            if (this.mNetConfigs[p] == null || this.mProtectedNetworks.contains(Integer.valueOf(p))) {
                loge("Ignoring protectedNetwork " + p);
            } else {
                this.mProtectedNetworks.add(Integer.valueOf(p));
            }
        }
        if (SystemProperties.get("cm.test.mode").equals("true")) {
            equals = SystemProperties.get("ro.build.type").equals("eng");
        } else {
            equals = SAMPLE_DBG;
        }
        this.mTestMode = equals;
        this.mTethering = new Tethering(this.mContext, this.mNetd, statsService);
        this.mPermissionMonitor = new PermissionMonitor(this.mContext, this.mNetd);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_STARTED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.ALL, intentFilter, null, null);
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
        this.mPacManager = new PacManager(this.mContext, this.mHandler, EVENT_PROXY_HAS_CHANGED);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mKeepaliveTracker = new KeepaliveTracker(this.mHandler);
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

    private NetworkRequest createInternetRequestForTransport(int transportType) {
        NetworkCapabilities netCap = new NetworkCapabilities();
        netCap.addCapability(12);
        netCap.addCapability(13);
        if (transportType > -1) {
            netCap.addTransportType(transportType);
        }
        return new NetworkRequest(netCap, -1, nextNetworkRequestId());
    }

    private void handleMobileDataAlwaysOn() {
        boolean isEnabled = VDBG;
        boolean enable = Global.getInt(this.mContext.getContentResolver(), "mobile_data_always_on", DISABLED) == ENABLED ? VDBG : SAMPLE_DBG;
        if (this.mNetworkRequests.get(this.mDefaultMobileDataRequest) == null) {
            isEnabled = SAMPLE_DBG;
        }
        if (enable != isEnabled) {
            if (enable) {
                handleRegisterNetworkRequest(new NetworkRequestInfo(null, this.mDefaultMobileDataRequest, new Binder(), NetworkRequestType.REQUEST));
            } else {
                handleReleaseNetworkRequest(this.mDefaultMobileDataRequest, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
            }
        }
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver.observe(Global.getUriFor("http_proxy"), EVENT_APPLY_GLOBAL_HTTP_PROXY);
        this.mSettingsObserver.observe(Global.getUriFor("mobile_data_always_on"), EVENT_CONFIGURE_MOBILE_DATA_ALWAYS_ON);
    }

    private synchronized int nextNetworkRequestId() {
        int i;
        i = this.mNextNetworkRequestId;
        this.mNextNetworkRequestId = i + ENABLED;
        return i;
    }

    protected int reserveNetId() {
        synchronized (this.mNetworkForNetId) {
            int i = MIN_NET_ID;
            while (i <= MAX_NET_ID) {
                int netId = this.mNextNetId;
                int i2 = this.mNextNetId + ENABLED;
                this.mNextNetId = i2;
                if (i2 > MAX_NET_ID) {
                    this.mNextNetId = MIN_NET_ID;
                }
                if (this.mNetIdInUse.get(netId)) {
                    i += ENABLED;
                } else {
                    this.mNetIdInUse.put(netId, VDBG);
                    return netId;
                }
            }
            throw new IllegalStateException("No free netIds");
        }
    }

    private NetworkState getFilteredNetworkState(int networkType, int uid, boolean ignoreBlocked) {
        boolean z = VDBG;
        if (!this.mLegacyTypeTracker.isTypeSupported(networkType)) {
            return NetworkState.EMPTY;
        }
        NetworkState state;
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            state = nai.getNetworkState();
            state.networkInfo.setType(networkType);
        } else {
            NetworkInfo info = new NetworkInfo(networkType, DISABLED, ConnectivityManager.getNetworkTypeName(networkType), "");
            info.setDetailedState(DetailedState.DISCONNECTED, null, null);
            if (networkType == 13 || networkType == ENABLED) {
                z = SAMPLE_DBG;
            } else if (networkType == 0) {
                z = SAMPLE_DBG;
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
                nai = getNetworkAgentInfoForNetwork(networks[DISABLED]);
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
        boolean z = SAMPLE_DBG;
        if (ignoreBlocked || isSystem(uid)) {
            return SAMPLE_DBG;
        }
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(UserHandle.getUserId(uid));
            if (vpn == null || !vpn.isBlockingUid(uid)) {
                boolean networkMetered;
                int uidRules;
                String iface = lp == null ? "" : lp.getInterfaceName();
                synchronized (this.mRulesLock) {
                    networkMetered = this.mMeteredIfaces.contains(iface);
                    uidRules = this.mUidRules.get(uid, DISABLED);
                }
                boolean allowed = VDBG;
                if (networkMetered) {
                    allowed = (uidRules & 4) != 0 ? SAMPLE_DBG : (this.mRestrictBackground && (uidRules & ENABLED) == 0) ? (uidRules & EVENT_CHANGE_MOBILE_DATA_ENABLED) != 0 ? VDBG : SAMPLE_DBG : VDBG;
                }
                if (allowed) {
                    allowed = (uidRules & 64) == 0 ? VDBG : SAMPLE_DBG;
                }
                if (!allowed) {
                    z = VDBG;
                }
                return z;
            }
            return VDBG;
        }
    }

    private void maybeLogBlockedNetworkInfo(NetworkInfo ni, int uid) {
        if (ni != null) {
            boolean removed = SAMPLE_DBG;
            boolean added = SAMPLE_DBG;
            synchronized (this.mBlockedAppUids) {
                if (ni.getDetailedState() == DetailedState.BLOCKED && this.mBlockedAppUids.add(Integer.valueOf(uid))) {
                    added = VDBG;
                } else if (ni.isConnected() && this.mBlockedAppUids.remove(Integer.valueOf(uid))) {
                    removed = VDBG;
                }
            }
            if (added) {
                log("Returning blocked NetworkInfo to uid=" + uid);
            } else if (removed) {
                log("Returning unblocked NetworkInfo to uid=" + uid);
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
        filterNetworkStateForUid(state, uid, SAMPLE_DBG);
        maybeLogBlockedNetworkInfo(state.networkInfo, uid);
        return state.networkInfo;
    }

    public Network getActiveNetwork() {
        enforceAccessPermission();
        return getActiveNetworkForUidInternal(Binder.getCallingUid(), SAMPLE_DBG);
    }

    public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) {
        enforceConnectivityInternalPermission();
        return getActiveNetworkForUidInternal(uid, ignoreBlocked);
    }

    private Network getActiveNetworkForUidInternal(int uid, boolean ignoreBlocked) {
        NetworkAgentInfo nai;
        Network network = null;
        int user = UserHandle.getUserId(uid);
        int vpnNetId = DISABLED;
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
                filterNetworkStateForUid(state, uid, SAMPLE_DBG);
                return state.networkInfo;
            }
        }
        return getFilteredNetworkState(networkType, uid, SAMPLE_DBG).networkInfo;
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
        for (int networkType = DISABLED; networkType <= 45; networkType += ENABLED) {
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
        NetworkState state = getFilteredNetworkState(networkType, uid, SAMPLE_DBG);
        if (isNetworkWithLinkPropertiesBlocked(state.linkProperties, uid, SAMPLE_DBG)) {
            return null;
        }
        return state.network;
    }

    public Network[] getAllNetworks() {
        Network[] result;
        enforceAccessPermission();
        synchronized (this.mNetworkForNetId) {
            result = new Network[this.mNetworkForNetId.size()];
            for (int i = DISABLED; i < this.mNetworkForNetId.size(); i += ENABLED) {
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
                        int length = networks.length;
                        for (int i = DISABLED; i < length; i += ENABLED) {
                            Network network = networks[i];
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
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
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
        Network[] allNetworks = getAllNetworks();
        int length = allNetworks.length;
        for (int i = DISABLED; i < length; i += ENABLED) {
            NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(allNetworks[i]);
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
        return info != null ? info.isMetered() : SAMPLE_DBG;
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
                    return SAMPLE_DBG;
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
                    return SAMPLE_DBG;
                }
            }
            log("requestRouteToHostAddress on invalid network: " + networkType);
            return SAMPLE_DBG;
        } catch (UnknownHostException e) {
            log("requestRouteToHostAddress got " + e.toString());
            return SAMPLE_DBG;
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
            return VDBG;
        } catch (Exception e) {
            loge("Exception trying to add a route: " + e);
            return SAMPLE_DBG;
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
                bestRoute.writeToParcel(_data, DISABLED);
                _data.writeInt(uid);
                b.transact(CODE_REMOVE_LEGACYROUTE_TO_HOST, _data, _reply, DISABLED);
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
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", TAG);
        }
    }

    private void enforceInternetPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERNET", TAG);
    }

    protected void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
    }

    protected void enforceChangePermission() {
        ConnectivityManager.enforceChangePermission(this.mContext);
    }

    private void enforceTetherAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
    }

    private void enforceKeepalivePermission() {
        this.mContext.enforceCallingOrSelfPermission(KeepaliveTracker.PERMISSION, TAG);
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
            intent.putExtra("isFailover", VDBG);
            info.setFailover(SAMPLE_DBG);
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
            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, "android.permission.RECEIVE_DATA_ACTIVITY_CHANGE", null, null, DISABLED, null, null);
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
            Bundle bundle = null;
            long ident = Binder.clearCallingIdentity();
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (ni.getType() == 3) {
                    intent.setAction("android.net.conn.CONNECTIVITY_CHANGE_SUPL");
                    intent.addFlags(1073741824);
                } else {
                    BroadcastOptions opts = BroadcastOptions.makeBasic();
                    opts.setMaxManifestReceiverApiLevel(EVENT_UNREGISTER_NETWORK_FACTORY);
                    bundle = opts.toBundle();
                }
                try {
                    BatteryStatsService.getService().noteConnectivityChanged(intent.getIntExtra("networkType", -1), ni != null ? ni.getState().toString() : "?");
                } catch (RemoteException e) {
                }
            }
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL, bundle);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void systemReady() {
        loadGlobalProxy();
        synchronized (this) {
            this.mSystemReady = VDBG;
            if (this.mInitialBroadcast != null) {
                this.mContext.sendStickyBroadcastAsUser(this.mInitialBroadcast, UserHandle.ALL);
                this.mInitialBroadcast = null;
            }
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_APPLY_GLOBAL_HTTP_PROXY));
        updateLockdownVpn();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_CONFIGURE_MOBILE_DATA_ALWAYS_ON));
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_SYSTEM_READY));
        this.mPermissionMonitor.startMonitoring();
    }

    private void setupDataActivityTracking(NetworkAgentInfo networkAgent) {
        int timeout;
        String iface = networkAgent.linkProperties.getInterfaceName();
        int type = -1;
        if (networkAgent.networkCapabilities.hasTransport(DISABLED)) {
            timeout = Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_mobile", MAX_VALIDATION_LOGS);
            type = DISABLED;
        } else if (networkAgent.networkCapabilities.hasTransport(ENABLED)) {
            timeout = Global.getInt(this.mContext.getContentResolver(), "data_activity_timeout_wifi", INET_CONDITION_LOG_MAX_SIZE);
            type = ENABLED;
        } else {
            timeout = DISABLED;
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
        if (caps.hasTransport(DISABLED) || caps.hasTransport(ENABLED)) {
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

    protected int getDefaultTcpRwnd() {
        return SystemProperties.getInt(DEFAULT_TCP_RWND_KEY, DISABLED);
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
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_min", values[DISABLED]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_def", values[ENABLED]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_rmem_max", values[EVENT_CHANGE_MOBILE_DATA_ENABLED]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_min", values[3]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_def", values[4]);
                    FileUtils.stringToFile("/sys/kernel/ipv4/tcp_wmem_max", values[5]);
                    this.mCurrentTcpBufferSizes = tcpBufferSizes;
                } catch (IOException e) {
                    loge("Can't set TCP buffer sizes:" + e);
                }
                Integer rwndValue = Integer.valueOf(Global.getInt(this.mContext.getContentResolver(), "tcp_default_init_rwnd", getDefaultTcpRwnd()));
                String sysctlKey = "sys.sysctl.tcp_def_init_rwnd";
                if (rwndValue.intValue() != 0) {
                    SystemProperties.set("sys.sysctl.tcp_def_init_rwnd", rwndValue.toString());
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
        String restoreDefaultNetworkDelayStr = SystemProperties.get(NETWORK_RESTORE_DELAY_PROP_NAME);
        if (!(restoreDefaultNetworkDelayStr == null || restoreDefaultNetworkDelayStr.length() == 0)) {
            try {
                return Integer.parseInt(restoreDefaultNetworkDelayStr);
            } catch (NumberFormatException e) {
            }
        }
        int ret = RESTORE_DEFAULT_NETWORK_DELAY;
        if (networkType <= 45 && this.mNetConfigs[networkType] != null) {
            ret = this.mNetConfigs[networkType].restoreTime;
        }
        return ret;
    }

    private boolean argsContain(String[] args, String target) {
        int length = args.length;
        for (int i = DISABLED; i < length; i += ENABLED) {
            if (args[i].equals(target)) {
                return VDBG;
            }
        }
        return SAMPLE_DBG;
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
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ConnectivityService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        if (argsContain(args, "--diag")) {
            dumpNetworkDiagnostics(pw);
            return;
        }
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
            pw.println("Requests:");
            pw.increaseIndent();
            int i = DISABLED;
            while (true) {
                if (i >= nai.networkRequests.size()) {
                    break;
                }
                pw.println(((NetworkRequest) nai.networkRequests.valueAt(i)).toString());
                i += ENABLED;
            }
            pw.decreaseIndent();
            pw.println("Lingered:");
            pw.increaseIndent();
            for (NetworkRequest nr : nai.networkLingered) {
                pw.println(nr.toString());
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
        pw.println();
        pw.println("Metered Interfaces:");
        pw.increaseIndent();
        for (String println : this.mMeteredIfaces) {
            pw.println(println);
        }
        pw.decreaseIndent();
        pw.println();
        pw.print("Restrict background: ");
        pw.println(this.mRestrictBackground);
        pw.println();
        pw.println("Status for known UIDs:");
        pw.increaseIndent();
        int size = this.mUidRules.size();
        for (i = DISABLED; i < size; i += ENABLED) {
            int uid = this.mUidRules.keyAt(i);
            pw.print("UID=");
            pw.print(uid);
            int uidRules = this.mUidRules.get(uid, DISABLED);
            pw.print(" rules=");
            pw.print(NetworkPolicyManager.uidRulesToString(uidRules));
            pw.println();
        }
        pw.println();
        pw.decreaseIndent();
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
        if (this.mInetLog != null) {
            if (this.mInetLog.size() > 0) {
                pw.println();
                pw.println("Inet condition reports:");
                pw.increaseIndent();
                i = DISABLED;
                while (true) {
                    if (i >= this.mInetLog.size()) {
                        break;
                    }
                    pw.println(this.mInetLog.get(i));
                    i += ENABLED;
                }
                pw.decreaseIndent();
            }
        }
        if (!argsContain(args, "--short")) {
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
        }
    }

    private boolean isLiveNetworkAgent(NetworkAgentInfo nai, int what) {
        if (nai.network == null) {
            return SAMPLE_DBG;
        }
        NetworkAgentInfo officialNai = getNetworkAgentInfoForNetwork(nai.network);
        if (officialNai != null && officialNai.equals(nai)) {
            return VDBG;
        }
        if (officialNai == null) {
            loge(((String) sMagicDecoderRing.get(what, Integer.toString(what))) + " - isLiveNetworkAgent found mismatched netId: " + officialNai + " - " + nai);
        } else {
            loge(((String) sMagicDecoderRing.get(what, Integer.toString(what))) + " - isLiveNetworkAgent found mismatched netId: " + officialNai + " - " + nai);
        }
        return SAMPLE_DBG;
    }

    private boolean isRequest(NetworkRequest request) {
        return ((NetworkRequestInfo) this.mNetworkRequests.get(request)).isRequest();
    }

    private void linger(NetworkAgentInfo nai) {
        nai.lingering = VDBG;
        NetworkEvent.logEvent(nai.network.netId, 5);
        nai.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_LINGER);
        notifyNetworkCallbacks(nai, 524291);
    }

    private void unlinger(NetworkAgentInfo nai) {
        nai.networkLingered.clear();
        if (nai.lingering) {
            nai.lingering = SAMPLE_DBG;
            NetworkEvent.logEvent(nai.network.netId, 6);
            log("Canceling linger of " + nai.name());
            nai.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_CONNECTED);
        }
    }

    private void handleAsyncChannelHalfConnect(Message msg) {
        AsyncChannel ac = msg.obj;
        NetworkAgentInfo nai;
        if (this.mNetworkFactoryInfos.containsKey(msg.replyTo)) {
            if (msg.arg1 == 0) {
                log("NetworkFactory connected");
                for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                    if (nri.isRequest()) {
                        int currentScore;
                        nai = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                        if (nai != null) {
                            currentScore = nai.getCurrentScore();
                        } else {
                            currentScore = DISABLED;
                        }
                        ac.sendMessage(536576, currentScore, DISABLED, nri.request);
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
            log(nai.name() + " got DISCONNECTED, was satisfying " + nai.networkRequests.size());
            if (nai.networkInfo.isConnected()) {
                nai.networkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
            }
            boolean wasDefault = isDefaultNetwork(nai);
            if (wasDefault) {
                this.mDefaultInetConditionPublished = DISABLED;
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
            for (int i = DISABLED; i < nai.networkRequests.size(); i += ENABLED) {
                NetworkRequest request = (NetworkRequest) nai.networkRequests.valueAt(i);
                NetworkAgentInfo currentNetwork = (NetworkAgentInfo) this.mNetworkForRequestId.get(request.requestId);
                if (currentNetwork != null && currentNetwork.network.netId == nai.network.netId) {
                    this.mNetworkForRequestId.remove(request.requestId);
                    sendUpdatedScoreToFactories(request, DISABLED);
                }
            }
            if (nai.networkRequests.get(this.mDefaultRequest.requestId) != null) {
                removeDataActivityTracking(nai);
                notifyLockdownVpn(nai);
                requestNetworkTransitionWakelock(nai.name());
            }
            this.mLegacyTypeTracker.remove(nai, wasDefault);
            rematchAllNetworksAndRequests(null, DISABLED);
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

    private void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        this.mNetworkRequests.put(nri.request, nri);
        this.mNetworkRequestInfoLogs.log("REGISTER " + nri);
        if (!nri.isRequest()) {
            for (NetworkAgentInfo network : this.mNetworkAgentInfos.values()) {
                if (nri.request.networkCapabilities.hasSignalStrength() && network.satisfiesImmutableCapabilitiesOf(nri.request)) {
                    updateSignalStrengthThresholds(network, "REGISTER", nri.request);
                }
            }
        }
        rematchAllNetworksAndRequests(null, DISABLED);
        if (!nri.isRequest() || this.mNetworkForRequestId.get(nri.request.requestId) != null) {
            return;
        }
        if (!SystemProperties.getBoolean("ro.config.hw_vowifi", SAMPLE_DBG)) {
            sendUpdatedScoreToFactories(nri.request, DISABLED);
        } else if (ifNeedToStartLteMmsTimer(nri.request)) {
            loge("need to start LteMmsTimer ,hold handleRegisterNetworkRequest ");
        } else {
            sendUpdatedScoreToFactories(nri.request, DISABLED);
        }
    }

    private void handleReleaseNetworkRequestWithIntent(PendingIntent pendingIntent, int callingUid) {
        NetworkRequestInfo nri = findExistingNetworkRequestInfo(pendingIntent);
        if (nri != null) {
            handleReleaseNetworkRequest(nri.request, callingUid);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean unneeded(NetworkAgentInfo nai) {
        if (!nai.everConnected || nai.isVPN() || nai.lingering || ignoreRemovedByWifiPro(nai)) {
            return SAMPLE_DBG;
        }
        boolean isDisabledMobileNetwork = (nai.networkInfo.getType() != 0 || this.mTelephonyManager.getDataEnabled()) ? SAMPLE_DBG : HwTelephonyManager.getDefault().isVSimEnabled() ? SAMPLE_DBG : VDBG;
        for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
            NetworkAgentInfo existing = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
            if (existing == null) {
                log("can not find existing request info on mNetworkForRequestId, request is " + nri.request);
            }
            boolean checkNetworkSupportBip = !nai.satisfies(nri.request) ? checkNetworkSupportBip(nai, nri.request) : VDBG;
            if (this.mDefaultRequest.requestId == nri.request.requestId && isDisabledMobileNetwork) {
                log("mobile net can't satisfy default network request when mobile data disabled");
                checkNetworkSupportBip = SAMPLE_DBG;
            }
            if (nri.isRequest() && r4) {
                if (nai.networkRequests.get(nri.request.requestId) != null || (existing != null && existing.getCurrentScore() < nai.getCurrentScoreAsValidated())) {
                    return SAMPLE_DBG;
                }
            }
        }
        return VDBG;
    }

    private void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequestInfo nri = (NetworkRequestInfo) this.mNetworkRequests.get(request);
        if (nri != null) {
            if (1000 == callingUid || nri.mUid == callingUid) {
                log("releasing NetworkRequest " + request);
                nri.unlinkDeathRecipient();
                this.mNetworkRequests.remove(request);
                synchronized (this.mUidToNetworkRequestCount) {
                    int requests = this.mUidToNetworkRequestCount.get(nri.mUid, DISABLED);
                    if (requests < ENABLED) {
                        Slog.wtf(TAG, "BUG: too small request count " + requests + " for UID " + nri.mUid);
                    } else if (requests == ENABLED) {
                        this.mUidToNetworkRequestCount.removeAt(this.mUidToNetworkRequestCount.indexOfKey(nri.mUid));
                    } else {
                        this.mUidToNetworkRequestCount.put(nri.mUid, requests - 1);
                    }
                }
                this.mNetworkRequestInfoLogs.log("RELEASE " + nri);
                NetworkAgentInfo nai;
                if (nri.isRequest()) {
                    int wasKept = DISABLED;
                    for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
                        if (nai2.networkRequests.get(nri.request.requestId) != null) {
                            nai2.networkRequests.remove(nri.request.requestId);
                            log(" Removing from current network " + nai2.name() + ", leaving " + nai2.networkRequests.size() + " requests.");
                            if (unneeded(nai2)) {
                                log("no live requests for " + nai2.name() + "; disconnecting");
                                teardownUnneededNetwork(nai2);
                            } else {
                                wasKept |= ENABLED;
                            }
                        }
                    }
                    nai2 = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                    if (nai2 != null) {
                        this.mNetworkForRequestId.remove(nri.request.requestId);
                    }
                    if (!(nri.request.legacyType == -1 || nai2 == null)) {
                        boolean doRemove = VDBG;
                        if (wasKept != 0) {
                            for (int i = DISABLED; i < nai2.networkRequests.size(); i += ENABLED) {
                                NetworkRequest otherRequest = (NetworkRequest) nai2.networkRequests.valueAt(i);
                                if (otherRequest.legacyType == nri.request.legacyType && isRequest(otherRequest)) {
                                    log(" still have other legacy request - leaving");
                                    doRemove = SAMPLE_DBG;
                                }
                            }
                        }
                        if (doRemove) {
                            this.mLegacyTypeTracker.remove(nri.request.legacyType, nai2, SAMPLE_DBG);
                        }
                    }
                    for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
                        nfi.asyncChannel.sendMessage(536577, nri.request);
                    }
                } else {
                    for (NetworkAgentInfo nai22 : this.mNetworkAgentInfos.values()) {
                        nai22.networkRequests.remove(nri.request.requestId);
                        if (nri.request.networkCapabilities.hasSignalStrength() && nai22.satisfiesImmutableCapabilitiesOf(nri.request)) {
                            updateSignalStrengthThresholds(nai22, "RELEASE", nri.request);
                        }
                    }
                }
                callCallbackForRequest(nri, null, 524296);
            } else {
                log("Attempt to release unowned NetworkRequest " + request);
            }
        }
    }

    public void setAcceptUnvalidated(Network network, boolean accept, boolean always) {
        int i;
        int i2 = ENABLED;
        enforceConnectivityInternalPermission();
        InternalHandler internalHandler = this.mHandler;
        InternalHandler internalHandler2 = this.mHandler;
        if (accept) {
            i = ENABLED;
        } else {
            i = DISABLED;
        }
        if (!always) {
            i2 = DISABLED;
        }
        internalHandler.sendMessage(internalHandler2.obtainMessage(EVENT_SET_ACCEPT_UNVALIDATED, i, i2, network));
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
                nai.asyncChannel.sendMessage(528393, accept ? ENABLED : DISABLED);
            }
            if (!accept) {
                nai.asyncChannel.sendMessage(528399);
                teardownUnneededNetwork(nai);
            }
        }
    }

    private void scheduleUnvalidatedPrompt(NetworkAgentInfo nai) {
        log("scheduleUnvalidatedPrompt " + nai.network);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_PROMPT_UNVALIDATED, nai.network), 8000);
    }

    private void handlePromptUnvalidated(Network network) {
        log("handlePromptUnvalidated " + network);
        NetworkAgentInfo nai = getNetworkAgentInfoForNetwork(network);
        if (nai != null && !nai.everValidated && !nai.everCaptivePortalDetected && nai.networkMisc.explicitlySelected && !nai.networkMisc.acceptUnvalidated) {
            Intent intent = new Intent("android.net.conn.PROMPT_UNVALIDATED");
            intent.setData(Uri.fromParts("netId", Integer.toString(network.netId), null));
            intent.addFlags(268435456);
            intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiNoInternetDialog");
            boolean z = VDBG;
            setProvNotificationVisibleIntent(z, nai.network.netId, NotificationType.NO_INTERNET, nai.networkInfo.getType(), nai.networkInfo.getExtraInfo(), PendingIntent.getActivityAsUser(this.mContext, DISABLED, intent, 268435456, null, UserHandle.CURRENT), VDBG);
        }
    }

    public int tether(String iface) {
        Log.d(TAG, "tether: ENTER");
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (!isTetheringSupported()) {
            return 3;
        }
        int status = this.mTethering.tether(iface);
        if (status == 0) {
            Log.d(TAG, "tether() return with ConnectivityManager.TETHER_ERROR_NO_ERROR");
            try {
                this.mPolicyManager.onTetheringChanged(iface, VDBG);
            } catch (RemoteException e) {
            }
        } else {
            Log.w(TAG, "tether() return with status " + status);
        }
        return status;
    }

    public int untether(String iface) {
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (!isTetheringSupported()) {
            return 3;
        }
        int status = this.mTethering.untether(iface);
        if (status == 0) {
            try {
                this.mPolicyManager.onTetheringChanged(iface, SAMPLE_DBG);
            } catch (RemoteException e) {
            }
        }
        return status;
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
        return new String[DISABLED];
    }

    public String[] getTetherableWifiRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableWifiRegexs();
        }
        return new String[DISABLED];
    }

    public String[] getTetherableBluetoothRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return this.mTethering.getTetherableBluetoothRegexs();
        }
        return new String[DISABLED];
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
        boolean z = VDBG;
        enforceTetherAccessPermission();
        boolean tetherEnabledInSettings = Global.getInt(this.mContext.getContentResolver(), "tether_supported", SystemProperties.get("ro.tether.denied").equals("true") ? DISABLED : ENABLED) != 0 ? this.mUserManager.hasUserRestriction("no_config_tethering") ? SAMPLE_DBG : VDBG : SAMPLE_DBG;
        if (!tetherEnabledInSettings || !this.mUserManager.isAdminUser()) {
            return SAMPLE_DBG;
        }
        if (this.mTethering.getTetherableUsbRegexs().length == 0 && this.mTethering.getTetherableWifiRegexs().length == 0) {
            if (this.mTethering.getTetherableBluetoothRegexs().length == 0) {
                return SAMPLE_DBG;
            }
        }
        if (this.mTethering.getUpstreamIfaceTypes().length == 0) {
            z = SAMPLE_DBG;
        }
        return z;
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
                int serialNum = this.mNetTransitionWakeLockSerialNumber + ENABLED;
                this.mNetTransitionWakeLockSerialNumber = serialNum;
                try {
                    this.mNetTransitionWakeLock.acquire();
                    this.mNetTransitionWakeLockCausedBy = forWhom;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_EXPIRE_NET_TRANSITION_WAKELOCK, serialNum, DISABLED), (long) this.mNetTransitionWakeLockTimeout);
                } catch (Throwable th2) {
                    th = th2;
                    int i = serialNum;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        NetworkAgentInfo nai = this.mLegacyTypeTracker.getNetworkForType(networkType);
        if (nai != null) {
            reportNetworkConnectivity(nai.network, percentage > 50 ? VDBG : SAMPLE_DBG);
        }
    }

    /* JADX WARNING: inconsistent code. */
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
            synchronized (nai) {
                if (!nai.everConnected) {
                } else if (isNetworkWithLinkPropertiesBlocked(nai.linkProperties, uid, SAMPLE_DBG)) {
                } else {
                    nai.networkMonitor.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, uid);
                }
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
        a = canonicalizeProxyInfo(a);
        b = canonicalizeProxyInfo(b);
        if (Objects.equals(a, b)) {
            return a != null ? Objects.equals(a.getHost(), b.getHost()) : VDBG;
        } else {
            return SAMPLE_DBG;
        }
    }

    public void setGlobalProxy(ProxyInfo proxyProperties) {
        enforceConnectivityInternalPermission();
        synchronized (this.mProxyLock) {
            if (proxyProperties == this.mGlobalProxy) {
                return;
            }
            if (proxyProperties != null) {
                if (proxyProperties.equals(this.mGlobalProxy)) {
                    return;
                }
            }
            if (this.mGlobalProxy == null || !this.mGlobalProxy.equals(proxyProperties)) {
                String host = "";
                int port = DISABLED;
                String exclList = "";
                String pacFileUrl = "";
                if (proxyProperties == null || (TextUtils.isEmpty(proxyProperties.getHost()) && Uri.EMPTY.equals(proxyProperties.getPacFileUrl()))) {
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
                    Binder.restoreCallingIdentity(token);
                    if (this.mGlobalProxy == null) {
                        proxyProperties = this.mDefaultProxy;
                    }
                    sendProxyBroadcast(proxyProperties);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
    }

    private void loadGlobalProxy() {
        ContentResolver res = this.mContext.getContentResolver();
        String host = Global.getString(res, "global_http_proxy_host");
        int port = Global.getInt(res, "global_http_proxy_port", DISABLED);
        String exclList = Global.getString(res, "global_http_proxy_exclusion_list");
        String pacFileUrl = Global.getString(res, "global_proxy_pac_url");
        if (!(TextUtils.isEmpty(host) && TextUtils.isEmpty(pacFileUrl))) {
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

    private void handleApplyDefaultProxy(ProxyInfo proxy) {
        if (proxy != null && TextUtils.isEmpty(proxy.getHost()) && Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            proxy = null;
        }
        synchronized (this.mProxyLock) {
            if (this.mDefaultProxy != null && this.mDefaultProxy.equals(proxy)) {
            } else if (this.mDefaultProxy == proxy) {
            } else {
                if (proxy != null) {
                    if (!proxy.isValid()) {
                        log("Invalid proxy properties, ignoring: " + proxy.toString());
                        return;
                    }
                }
                if (!(this.mGlobalProxy == null || proxy == null || Uri.EMPTY.equals(proxy.getPacFileUrl()))) {
                    if (proxy.getPacFileUrl().equals(this.mGlobalProxy.getPacFileUrl())) {
                        this.mGlobalProxy = proxy;
                        sendProxyBroadcast(this.mGlobalProxy);
                        return;
                    }
                }
                this.mDefaultProxy = proxy;
                if (this.mGlobalProxy != null) {
                    return;
                }
                if (!this.mDefaultProxyDisabled) {
                    sendProxyBroadcast(proxy);
                }
            }
        }
    }

    private void updateProxy(LinkProperties newLp, LinkProperties oldLp, NetworkAgentInfo nai) {
        ProxyInfo oldProxyInfo = null;
        ProxyInfo httpProxy = newLp == null ? null : newLp.getHttpProxy();
        if (oldLp != null) {
            oldProxyInfo = oldLp.getHttpProxy();
        }
        if (!proxyInfoEqual(httpProxy, oldProxyInfo)) {
            sendProxyBroadcast(getDefaultProxy());
        }
    }

    private void handleDeprecatedGlobalHttpProxy() {
        String proxy = Global.getString(this.mContext.getContentResolver(), "http_proxy");
        if (!TextUtils.isEmpty(proxy)) {
            String[] data = proxy.split(":");
            if (data.length != 0) {
                String proxyHost = data[DISABLED];
                int proxyPort = 8080;
                if (data.length > ENABLED) {
                    try {
                        proxyPort = Integer.parseInt(data[ENABLED]);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                setGlobalProxy(new ProxyInfo(data[DISABLED], proxyPort, ""));
            }
        }
    }

    private void sendProxyBroadcast(ProxyInfo proxy) {
        if (proxy == null) {
            proxy = new ProxyInfo("", DISABLED, "");
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
            return SAMPLE_DBG;
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
        ParcelFileDescriptor establish;
        throwIfLockdownEnabled();
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            establish = ((Vpn) this.mVpns.get(user)).establish(config);
        }
        return establish;
    }

    public void startLegacyVpn(VpnProfile profile) {
        throwIfLockdownEnabled();
        LinkProperties egress = getActiveLinkProperties();
        if (egress == null) {
            throw new IllegalStateException("Missing active network connection");
        }
        int user = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this.mVpns) {
            Vpn mVpn = (Vpn) this.mVpns.get(user);
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
            return new VpnInfo[DISABLED];
        }
        VpnInfo[] vpnInfoArr;
        synchronized (this.mVpns) {
            List<VpnInfo> infoList = new ArrayList();
            for (int i = DISABLED; i < this.mVpns.size(); i += ENABLED) {
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
            LinkProperties linkProperties = getLinkProperties(underlyingNetworks[DISABLED]);
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
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            Slog.w(TAG, "Lockdown VPN only available to AID_SYSTEM");
            return SAMPLE_DBG;
        }
        this.mLockdownEnabled = LockdownVpnTracker.isEnabled();
        if (this.mLockdownEnabled) {
            String profileName = new String(this.mKeyStore.get("LOCKDOWN_VPN"));
            VpnProfile profile = VpnProfile.decode(profileName, this.mKeyStore.get("VPN_" + profileName));
            if (profile == null) {
                Slog.e(TAG, "Lockdown VPN configured invalid profile " + profileName);
                setLockdownTracker(null);
                return VDBG;
            }
            int user = UserHandle.getUserId(Binder.getCallingUid());
            synchronized (this.mVpns) {
                Vpn vpn = (Vpn) this.mVpns.get(user);
                if (vpn == null) {
                    Slog.w(TAG, "VPN for user " + user + " not ready yet. Skipping lockdown");
                    return SAMPLE_DBG;
                }
                setLockdownTracker(new LockdownVpnTracker(this.mContext, this.mNetd, this, vpn, profile));
            }
        } else {
            setLockdownTracker(null);
        }
        return VDBG;
    }

    private void setLockdownTracker(LockdownVpnTracker tracker) {
        LockdownVpnTracker existing = this.mLockdownTracker;
        this.mLockdownTracker = null;
        if (existing != null) {
            existing.shutdown();
        }
        if (tracker != null) {
            try {
                this.mNetd.setFirewallEnabled(VDBG);
                this.mNetd.setFirewallInterfaceRule("lo", VDBG);
                this.mLockdownTracker = tracker;
                this.mLockdownTracker.init();
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mNetd.setFirewallEnabled(SAMPLE_DBG);
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
                return SAMPLE_DBG;
            }
            boolean startAlwaysOnVpn = vpn.startAlwaysOnVpn();
            return startAlwaysOnVpn;
        }
    }

    public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown) {
        enforceConnectivityInternalPermission();
        enforceCrossUserPermission(userId);
        if (LockdownVpnTracker.isEnabled()) {
            return SAMPLE_DBG;
        }
        synchronized (this.mVpns) {
            Vpn vpn = (Vpn) this.mVpns.get(userId);
            if (vpn == null) {
                Slog.w(TAG, "User " + userId + " has no Vpn configuration");
                return SAMPLE_DBG;
            } else if (!vpn.setAlwaysOnPackage(packageName, lockdown)) {
                return SAMPLE_DBG;
            } else if (startAlwaysOnVpn(userId)) {
                vpn.saveAlwaysOnPackage();
                return VDBG;
            } else {
                vpn.setAlwaysOnPackage(null, SAMPLE_DBG);
                return SAMPLE_DBG;
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

    private void setProvNotificationVisible(boolean visible, int networkType, String action) {
        boolean z = visible;
        setProvNotificationVisibleIntent(z, DumpState.DUMP_INSTALLS + (networkType + ENABLED), NotificationType.SIGN_IN, networkType, null, PendingIntent.getBroadcast(this.mContext, DISABLED, new Intent(action), DISABLED), SAMPLE_DBG);
    }

    private void setProvNotificationVisibleIntent(boolean visible, int id, NotificationType notifyType, int networkType, String extraInfo, PendingIntent intent, boolean highPriority) {
        log("setProvNotificationVisibleIntent " + notifyType + " visible=" + visible + " networkType=" + ConnectivityManager.getNetworkTypeName(networkType) + " extraInfo=" + extraInfo + " highPriority=" + highPriority);
        Resources r = Resources.getSystem();
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (visible) {
            Bitmap bmp = null;
            Notification notification = new Notification();
            if (notifyType != NotificationType.NO_INTERNET || networkType != ENABLED) {
                if (notifyType == NotificationType.SIGN_IN) {
                    CharSequence title;
                    CharSequence details;
                    int icon;
                    int i;
                    Object[] objArr;
                    switch (networkType) {
                        case DISABLED /*0*/:
                        case H.ADD_STARTING /*5*/:
                            objArr = new Object[ENABLED];
                            objArr[DISABLED] = Integer.valueOf(DISABLED);
                            title = r.getString(17040326, objArr);
                            details = this.mTelephonyManager.getNetworkOperatorName();
                            icon = 17303214;
                            bmp = BitmapFactory.decodeResource(r, 33751560);
                            break;
                        case ENABLED /*1*/:
                            objArr = new Object[ENABLED];
                            objArr[DISABLED] = Integer.valueOf(DISABLED);
                            title = r.getString(17040325, objArr);
                            objArr = new Object[ENABLED];
                            objArr[DISABLED] = extraInfo;
                            details = r.getString(17040327, objArr);
                            icon = 17303218;
                            if (startBrowserForWifiPortal(notification, extraInfo)) {
                                return;
                            }
                            break;
                        default:
                            objArr = new Object[ENABLED];
                            objArr[DISABLED] = Integer.valueOf(DISABLED);
                            title = r.getString(17040326, objArr);
                            objArr = new Object[ENABLED];
                            objArr[DISABLED] = extraInfo;
                            details = r.getString(17040327, objArr);
                            icon = 17303214;
                            bmp = BitmapFactory.decodeResource(r, 33751560);
                            break;
                    }
                    Builder localOnly = new Builder(this.mContext).setWhen(0).setSmallIcon(icon).setLargeIcon(bmp).setAutoCancel(VDBG).setTicker(title).setColor(this.mContext.getColor(17170519)).setContentTitle(title).setContentText(details).setContentIntent(intent).setLocalOnly(VDBG);
                    if (highPriority) {
                        i = ENABLED;
                    } else {
                        i = DISABLED;
                    }
                    localOnly = localOnly.setPriority(i);
                    if (highPriority) {
                        i = -1;
                    } else {
                        i = DISABLED;
                    }
                    try {
                        notificationManager.notifyAsUser(NOTIFICATION_ID, id, localOnly.setDefaults(i).setOnlyAlertOnce(VDBG).build(), UserHandle.ALL);
                    } catch (NullPointerException npe) {
                        loge("setNotificationVisible: visible notificationManager npe=" + npe);
                        npe.printStackTrace();
                    }
                } else {
                    Slog.wtf(TAG, "Unknown notification type " + notifyType + "on network type " + ConnectivityManager.getNetworkTypeName(networkType));
                    return;
                }
            }
            return;
        }
        try {
            notificationManager.cancelAsUser(NOTIFICATION_ID, id, UserHandle.ALL);
        } catch (NullPointerException npe2) {
            loge("setNotificationVisible: cancel notificationManager npe=" + npe2);
            npe2.printStackTrace();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                        break;
                    } else if (element.equals(TAG_PROVISIONING_URL)) {
                        String mcc = parser.getAttributeValue(null, ATTR_MCC);
                        if (mcc != null) {
                            try {
                                if (Integer.parseInt(mcc) == config.mcc) {
                                    String mnc = parser.getAttributeValue(null, ATTR_MNC);
                                    if (mnc != null && Integer.parseInt(mnc) == config.mnc) {
                                        parser.next();
                                        if (parser.getEventType() == 4) {
                                            break;
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
                if (fileReader2 != null) {
                    try {
                        fileReader2.close();
                    } catch (IOException e4) {
                    }
                }
                return null;
            } catch (FileNotFoundException e5) {
                fileReader = fileReader2;
            } catch (XmlPullParserException e6) {
                e = e6;
                fileReader = fileReader2;
            } catch (IOException e7) {
                e2 = e7;
                fileReader = fileReader2;
            } catch (Throwable th2) {
                th = th2;
                fileReader = fileReader2;
            }
        } catch (FileNotFoundException e8) {
            try {
                loge("Carrier Provisioning Urls file not found");
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e9) {
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e10) {
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e11) {
            e = e11;
            loge("Xml parser exception reading Carrier Provisioning Urls file: " + e);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e12) {
                }
            }
            return null;
        } catch (IOException e13) {
            e2 = e13;
            loge("I/O exception reading Carrier Provisioning Urls file: " + e2);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e14) {
                }
            }
            return null;
        }
    }

    public String getMobileProvisioningUrl() {
        enforceConnectivityInternalPermission();
        String url = getProvisioningUrlBaseFromFile();
        if (TextUtils.isEmpty(url)) {
            url = this.mContext.getResources().getString(17039434);
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
            setProvNotificationVisible(visible, networkType, action);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setAirplaneMode(boolean enable) {
        enforceConnectivityInternalPermission();
        long ident = Binder.clearCallingIdentity();
        try {
            Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", enable ? ENABLED : DISABLED);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, enable);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

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
            boolean alwaysOnLockdown = Secure.getIntForUser(cr, "always_on_vpn_lockdown", DISABLED, userId) != 0 ? VDBG : SAMPLE_DBG;
            if (alwaysOnPackage != null) {
                userVpn.setAlwaysOnPackage(alwaysOnPackage, alwaysOnLockdown);
            }
            if (this.mUserManager.getUserInfo(userId).isPrimary() && LockdownVpnTracker.isEnabled()) {
                updateLockdownVpn();
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
            for (int i = DISABLED; i < vpnsSize; i += ENABLED) {
                ((Vpn) this.mVpns.valueAt(i)).onUserAdded(userId);
            }
        }
    }

    private void onUserRemoved(int userId) {
        synchronized (this.mVpns) {
            int vpnsSize = this.mVpns.size();
            for (int i = DISABLED; i < vpnsSize; i += ENABLED) {
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
        nai.asyncChannel.sendMessage(528398, DISABLED, DISABLED, thresholds);
    }

    public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutMs, IBinder binder, int legacyType) {
        NetworkRequestType type;
        if (networkCapabilities == null) {
            type = NetworkRequestType.TRACK_DEFAULT;
        } else {
            type = NetworkRequestType.REQUEST;
        }
        if (type == NetworkRequestType.TRACK_DEFAULT) {
            networkCapabilities = new NetworkCapabilities(this.mDefaultRequest.networkCapabilities);
            enforceAccessPermission();
        } else {
            NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
            enforceNetworkRequestPermissions(networkCapabilities2);
            enforceMeteredApnPolicy(networkCapabilities2);
            networkCapabilities = networkCapabilities2;
        }
        ensureRequestableCapabilities(networkCapabilities);
        if (timeoutMs < 0 || timeoutMs > 6000000) {
            throw new IllegalArgumentException("Bad timeout specified");
        } else if ("*".equals(networkCapabilities.getNetworkSpecifier())) {
            throw new IllegalArgumentException("Invalid network specifier - must not be '*'");
        } else {
            NetworkRequest networkRequest;
            if (SystemProperties.getBoolean("ro.config.hw_vowifi", SAMPLE_DBG)) {
                networkRequest = new NetworkRequest(changeWifiMmsNetworkCapabilities(networkCapabilities), legacyType, nextNetworkRequestId());
            } else {
                networkRequest = new NetworkRequest(networkCapabilities, legacyType, nextNetworkRequestId());
            }
            NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder, type);
            log("requestNetwork for " + nri);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REGISTER_NETWORK_REQUEST, nri));
            if (timeoutMs > 0) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MAX_NETWORK_REQUEST_LOGS, nri), (long) timeoutMs);
            }
            return networkRequest;
        }
    }

    private void enforceNetworkRequestPermissions(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasCapability(13)) {
            enforceChangePermission();
        } else {
            enforceConnectivityInternalPermission();
        }
    }

    public boolean requestBandwidthUpdate(Network network) {
        enforceAccessPermission();
        if (network == null) {
            return SAMPLE_DBG;
        }
        synchronized (this.mNetworkForNetId) {
            NetworkAgentInfo nai = (NetworkAgentInfo) this.mNetworkForNetId.get(network.netId);
        }
        if (nai == null) {
            return SAMPLE_DBG;
        }
        nai.asyncChannel.sendMessage(528394);
        return VDBG;
    }

    private boolean isSystem(int uid) {
        return uid < AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT ? VDBG : SAMPLE_DBG;
    }

    private void enforceMeteredApnPolicy(NetworkCapabilities networkCapabilities) {
        int uid = Binder.getCallingUid();
        if (!(isSystem(uid) || networkCapabilities.hasCapability(11))) {
            synchronized (this.mRulesLock) {
                int uidRules = this.mUidRules.get(uid, 32);
            }
            if (this.mRestrictBackground && (uidRules & ENABLED) == 0 && (uidRules & EVENT_CHANGE_MOBILE_DATA_ENABLED) == 0) {
                networkCapabilities.addCapability(11);
            }
        }
    }

    public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        enforceNetworkRequestPermissions(networkCapabilities2);
        enforceMeteredApnPolicy(networkCapabilities2);
        ensureRequestableCapabilities(networkCapabilities2);
        NetworkRequest networkRequest = new NetworkRequest(networkCapabilities2, -1, nextNetworkRequestId());
        NetworkRequestInfo nri = new NetworkRequestInfo(networkRequest, operation, NetworkRequestType.REQUEST);
        log("pendingRequest for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REGISTER_NETWORK_REQUEST_WITH_INTENT, nri));
        return networkRequest;
    }

    private void releasePendingNetworkRequestWithDelay(PendingIntent operation) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), DISABLED, operation), (long) this.mReleasePendingIntentDelayMs);
    }

    public void releasePendingNetworkRequest(PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST_WITH_INTENT, getCallingUid(), DISABLED, operation));
    }

    private boolean hasWifiNetworkListenPermission(NetworkCapabilities nc) {
        if (nc == null) {
            return SAMPLE_DBG;
        }
        int[] transportTypes = nc.getTransportTypes();
        if (transportTypes.length != ENABLED || transportTypes[DISABLED] != ENABLED) {
            return SAMPLE_DBG;
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
            return VDBG;
        } catch (SecurityException e) {
            return SAMPLE_DBG;
        }
    }

    public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) {
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        NetworkRequest networkRequest = new NetworkRequest(new NetworkCapabilities(networkCapabilities), -1, nextNetworkRequestId());
        NetworkRequestInfo nri = new NetworkRequestInfo(messenger, networkRequest, binder, NetworkRequestType.LISTEN);
        log("listenForNetwork for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REGISTER_NETWORK_LISTENER, nri));
        return networkRequest;
    }

    public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) {
        checkNotNull(operation, "PendingIntent cannot be null.");
        if (!hasWifiNetworkListenPermission(networkCapabilities)) {
            enforceAccessPermission();
        }
        NetworkRequestInfo nri = new NetworkRequestInfo(new NetworkRequest(new NetworkCapabilities(networkCapabilities), -1, nextNetworkRequestId()), operation, NetworkRequestType.LISTEN);
        log("pendingListenForNetwork for " + nri);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REGISTER_NETWORK_LISTENER, nri));
    }

    public void releaseNetworkRequest(NetworkRequest networkRequest) {
        if (SystemProperties.getBoolean("ro.config.hw_vowifi", SAMPLE_DBG)) {
            wifiMmsRelease(networkRequest);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_RELEASE_NETWORK_REQUEST, getCallingUid(), DISABLED, networkRequest));
    }

    public void registerNetworkFactory(Messenger messenger, String name) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REGISTER_NETWORK_FACTORY, new NetworkFactoryInfo(name, messenger, new AsyncChannel())));
    }

    private void handleRegisterNetworkFactory(NetworkFactoryInfo nfi) {
        log("Got NetworkFactory Messenger for " + nfi.name);
        this.mNetworkFactoryInfos.put(nfi.messenger, nfi);
        nfi.asyncChannel.connect(this.mContext, this.mTrackerHandler, nfi.messenger);
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        enforceConnectivityInternalPermission();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_UNREGISTER_NETWORK_FACTORY, messenger));
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
        return (NetworkAgentInfo) this.mNetworkForRequestId.get(this.mDefaultRequest.requestId);
    }

    private boolean isDefaultNetwork(NetworkAgentInfo nai) {
        return nai == getDefaultNetwork() ? VDBG : SAMPLE_DBG;
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
                    if (TextUtils.equals(network.networkCapabilities.getNetworkSpecifier(), na.networkCapabilities.getNetworkSpecifier()) && isLpIdentical) {
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REGISTER_NETWORK_AGENT, nai));
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
            rematchNetworkAndRequests(identicalNai, ReapUnvalidatedNetworks.REAP);
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
        holdWifiNetworkMessenger(na);
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
        boolean wasRunningClat = nai.clatd != null ? nai.clatd.isStarted() : SAMPLE_DBG;
        boolean shouldRunClat = Nat464Xlat.requiresClat(nai);
        if (!wasRunningClat && shouldRunClat) {
            nai.clatd = new Nat464Xlat(this.mContext, this.mNetd, this.mTrackerHandler, nai);
            nai.clatd.start();
        } else if (wasRunningClat && !shouldRunClat) {
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
        CompareResult<RouteInfo> routeDiff = new CompareResult();
        if (oldLp != null) {
            routeDiff = oldLp.compareAllRoutes(newLp);
        } else if (newLp != null) {
            routeDiff.added = newLp.getAllRoutes();
        }
        for (RouteInfo route : routeDiff.added) {
            if (!route.hasGateway()) {
                log("Adding Route [" + route + "] to network " + netId);
                try {
                    this.mNetd.addRoute(netId, route);
                } catch (Exception e) {
                    if (route.getDestination().getAddress() instanceof Inet4Address) {
                        loge("Exception in addRoute for non-gateway: " + e);
                    } else {
                        loge("Exception in addRoute for non-gateway: " + e);
                    }
                }
            }
        }
        for (RouteInfo route2 : routeDiff.added) {
            if (route2.hasGateway()) {
                log("Adding Route [" + route2 + "] to network " + netId);
                try {
                    this.mNetd.addRoute(netId, route2);
                } catch (Exception e2) {
                    if (route2.getGateway() instanceof Inet4Address) {
                        loge("Exception in addRoute for gateway: " + e2);
                    } else {
                        loge("Exception in addRoute for gateway: " + e2);
                    }
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
        if (routeDiff.added.isEmpty() && routeDiff.removed.isEmpty()) {
            return SAMPLE_DBG;
        }
        return VDBG;
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
        int last = DISABLED;
        for (InetAddress dns : dnses) {
            last += ENABLED;
            SystemProperties.set("net.dns" + last, dns.getHostAddress());
        }
        for (int i = last + ENABLED; i <= this.mNumDnsEntries; i += ENABLED) {
            SystemProperties.set("net.dns" + i, "");
        }
        this.mNumDnsEntries = last;
    }

    private void updateCapabilities(NetworkAgentInfo nai, NetworkCapabilities networkCapabilities) {
        NetworkCapabilities networkCapabilities2 = new NetworkCapabilities(networkCapabilities);
        if (nai.lastValidated) {
            networkCapabilities2.addCapability(EVENT_PROXY_HAS_CHANGED);
        } else {
            networkCapabilities2.removeCapability(EVENT_PROXY_HAS_CHANGED);
        }
        if (nai.lastCaptivePortalDetected) {
            networkCapabilities2.addCapability(EVENT_REGISTER_NETWORK_FACTORY);
        } else {
            networkCapabilities2.removeCapability(EVENT_REGISTER_NETWORK_FACTORY);
        }
        if (!Objects.equals(nai.networkCapabilities, networkCapabilities2)) {
            int oldScore = nai.getCurrentScore();
            if (nai.networkCapabilities.hasCapability(13) != networkCapabilities2.hasCapability(13)) {
                try {
                    this.mNetd.setNetworkPermission(nai.network.netId, networkCapabilities2.hasCapability(13) ? null : NetworkManagementService.PERMISSION_SYSTEM);
                } catch (RemoteException e) {
                    loge("Exception in setNetworkPermission: " + e);
                }
            }
            synchronized (nai) {
                nai.networkCapabilities = networkCapabilities2;
            }
            rematchAllNetworksAndRequests(nai, oldScore);
            notifyNetworkCallbacks(nai, 524294);
        }
    }

    private void sendUpdatedScoreToFactories(NetworkAgentInfo nai) {
        for (int i = DISABLED; i < nai.networkRequests.size(); i += ENABLED) {
            NetworkRequest nr = (NetworkRequest) nai.networkRequests.valueAt(i);
            if (isRequest(nr)) {
                sendUpdatedScoreToFactories(nr, nai.getCurrentScore());
            }
        }
    }

    protected void sendUpdatedScoreToFactories(NetworkRequest networkRequest, int score) {
        log("sending new Min Network Score(" + score + "): " + networkRequest.toString());
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            nfi.asyncChannel.sendMessage(536576, score, DISABLED, networkRequest);
        }
    }

    private void sendUpdatedScoreToFactoriesWhenWifiDisconnected(NetworkRequest networkRequest, int score) {
        for (NetworkFactoryInfo nfi : this.mNetworkFactoryInfos.values()) {
            if (!"Telephony".equals(nfi.name)) {
                nfi.asyncChannel.sendMessage(536576, score, DISABLED, networkRequest);
            }
        }
    }

    private void sendPendingIntentForRequest(NetworkRequestInfo nri, NetworkAgentInfo networkAgent, int notificationType) {
        if (notificationType == 524290 && !nri.mPendingIntentSent) {
            Intent intent = new Intent();
            intent.putExtra("android.net.extra.NETWORK", networkAgent.network);
            intent.putExtra("android.net.extra.NETWORK_REQUEST", nri.request);
            nri.mPendingIntentSent = VDBG;
            sendIntent(nri.mPendingIntent, intent);
        }
    }

    private void sendIntent(PendingIntent pendingIntent, Intent intent) {
        this.mPendingIntentWakeLock.acquire();
        try {
            log("Sending " + pendingIntent);
            pendingIntent.send(this.mContext, DISABLED, intent, this, null);
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

    private void callCallbackForRequest(NetworkRequestInfo nri, NetworkAgentInfo networkAgent, int notificationType) {
        if (nri.messenger != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(NetworkRequest.class.getSimpleName(), new NetworkRequest(nri.request));
            Message msg = Message.obtain();
            if (!(notificationType == 524293 || notificationType == 524296)) {
                bundle.putParcelable(Network.class.getSimpleName(), networkAgent.network);
            }
            switch (notificationType) {
                case 524291:
                    msg.arg1 = 30000;
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
        for (int i = DISABLED; i < nai.networkRequests.size(); i += ENABLED) {
            NetworkRequest nr = (NetworkRequest) nai.networkRequests.valueAt(i);
            if (isRequest(nr)) {
                loge("Dead network still had at least " + nr);
                break;
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
        teardownUnneededNetwork(oldNetwork);
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

    private void rematchNetworkAndRequests(NetworkAgentInfo newNetwork, ReapUnvalidatedNetworks reapUnvalidatedNetworks) {
        if (newNetwork.everConnected) {
            boolean isDisabledMobileNetwork;
            ArrayList<NetworkAgentInfo> affectedNetworks;
            ArrayList<NetworkRequestInfo> addedRequests;
            NetworkAgentInfo currentNetwork;
            boolean satisfies;
            IBatteryStats bs;
            int type;
            String baseIface;
            String stackedIface;
            int i;
            NetworkRequest nr;
            int i2;
            boolean keep = newNetwork.isVPN();
            boolean isNewDefault = SAMPLE_DBG;
            NetworkAgentInfo oldDefaultNetwork = null;
            log("rematching " + newNetwork.name());
            if (newNetwork.networkInfo.getType() == 0) {
                if (!this.mTelephonyManager.getDataEnabled()) {
                    isDisabledMobileNetwork = HwTelephonyManager.getDefault().isVSimEnabled() ? SAMPLE_DBG : VDBG;
                    affectedNetworks = new ArrayList();
                    addedRequests = new ArrayList();
                    log(" network has: " + newNetwork.networkCapabilities);
                    for (NetworkRequestInfo nri : this.mNetworkRequests.values()) {
                        currentNetwork = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri.request.requestId);
                        satisfies = newNetwork.satisfies(nri.request);
                        if (this.mDefaultRequest.requestId == nri.request.requestId && isDisabledMobileNetwork) {
                            log("mobile net can't satisfy default network request when mobile data disabled");
                            satisfies = SAMPLE_DBG;
                        }
                        if (newNetwork == currentNetwork || !satisfies) {
                            if (!satisfies) {
                                if (!checkNetworkSupportBip(newNetwork, nri.request)) {
                                    if (newNetwork.networkRequests.get(nri.request.requestId) != null) {
                                        log("Network " + newNetwork.name() + " stopped satisfying" + " request " + nri.request.requestId);
                                        newNetwork.networkRequests.remove(nri.request.requestId);
                                        if (currentNetwork == newNetwork) {
                                            this.mNetworkForRequestId.remove(nri.request.requestId);
                                            sendUpdatedScoreToFactories(nri.request, DISABLED);
                                        } else if (nri.isRequest()) {
                                            Slog.wtf(TAG, "BUG: Removing request " + nri.request.requestId + " from " + newNetwork.name() + " without updating mNetworkForRequestId or factories!");
                                        }
                                        callCallbackForRequest(nri, newNetwork, 524292);
                                    }
                                }
                            }
                            if (nri.isRequest()) {
                                if (newNetwork.addRequest(nri.request)) {
                                    addedRequests.add(nri);
                                }
                            } else {
                                log("currentScore = " + (currentNetwork == null ? currentNetwork.getCurrentScore() : DISABLED) + ", newScore = " + newNetwork.getCurrentScore());
                                if (currentNetwork != null || currentNetwork.getCurrentScore() < newNetwork.getCurrentScore()) {
                                    log("rematch for " + newNetwork.name());
                                    if (currentNetwork == null) {
                                        log("   accepting network in place of " + currentNetwork.name());
                                        currentNetwork.networkRequests.remove(nri.request.requestId);
                                        currentNetwork.networkLingered.add(nri.request);
                                        affectedNetworks.add(currentNetwork);
                                    } else {
                                        log("   accepting network in place of null");
                                    }
                                    unlinger(newNetwork);
                                    this.mNetworkForRequestId.put(nri.request.requestId, newNetwork);
                                    if (!newNetwork.addRequest(nri.request)) {
                                        Slog.wtf(TAG, "BUG: " + newNetwork.name() + " already has " + nri.request);
                                    }
                                    addedRequests.add(nri);
                                    keep = VDBG;
                                    sendUpdatedScoreToFactories(nri.request, newNetwork.getCurrentScore());
                                    if (this.mDefaultRequest.requestId == nri.request.requestId) {
                                        isNewDefault = VDBG;
                                        oldDefaultNetwork = currentNetwork;
                                    }
                                }
                            }
                        } else {
                            log("Network " + newNetwork.name() + " was already satisfying" + " request " + nri.request.requestId + ". No change.");
                            keep = VDBG;
                        }
                    }
                    for (NetworkAgentInfo nai : affectedNetworks) {
                        if (nai.lingering) {
                            if (unneeded(nai)) {
                                unlinger(nai);
                            } else {
                                linger(nai);
                            }
                        }
                    }
                    if (NetworkFactory.isDualCellDataEnable()) {
                        log("isDualCellDataEnable is true so keep is true");
                        keep = VDBG;
                    }
                    if (isNewDefault) {
                        makeDefault(newNetwork);
                        logDefaultNetworkEvent(newNetwork, oldDefaultNetwork);
                        synchronized (this) {
                            if (this.mNetTransitionWakeLock.isHeld()) {
                                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_CLEAR_NET_TRANSITION_WAKELOCK, this.mNetTransitionWakeLockSerialNumber, DISABLED), 1000);
                            }
                        }
                    }
                    for (NetworkRequestInfo notifyNetworkCallback : addedRequests) {
                        notifyNetworkCallback(newNetwork, notifyNetworkCallback);
                    }
                    if (isNewDefault) {
                        if (oldDefaultNetwork != null) {
                            this.mLegacyTypeTracker.remove(oldDefaultNetwork.networkInfo.getType(), oldDefaultNetwork, VDBG);
                        }
                        this.mDefaultInetConditionPublished = newNetwork.lastValidated ? MIN_NET_ID : DISABLED;
                        this.mLegacyTypeTracker.add(newNetwork.networkInfo.getType(), newNetwork);
                        notifyLockdownVpn(newNetwork);
                    }
                    if (keep) {
                        try {
                            bs = BatteryStatsService.getService();
                            type = newNetwork.networkInfo.getType();
                            baseIface = newNetwork.linkProperties.getInterfaceName();
                            bs.noteNetworkInterfaceType(baseIface, type);
                            for (LinkProperties stacked : newNetwork.linkProperties.getStackedLinks()) {
                                stackedIface = stacked.getInterfaceName();
                                bs.noteNetworkInterfaceType(stackedIface, type);
                                NetworkStatsFactory.noteStackedIface(stackedIface, baseIface);
                            }
                        } catch (RemoteException e) {
                        }
                        i = DISABLED;
                        while (true) {
                            if (i < newNetwork.networkRequests.size()) {
                                break;
                            }
                            nr = (NetworkRequest) newNetwork.networkRequests.valueAt(i);
                            i2 = nr.legacyType;
                            if (r0 != -1 && isRequest(nr)) {
                                this.mLegacyTypeTracker.add(nr.legacyType, newNetwork);
                            }
                            i += ENABLED;
                        }
                        if (newNetwork.isVPN()) {
                            this.mLegacyTypeTracker.add(EVENT_REGISTER_NETWORK_FACTORY, newNetwork);
                        }
                    }
                    if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                        for (NetworkAgentInfo nai2 : this.mNetworkAgentInfos.values()) {
                            if (unneeded(nai2)) {
                                log("Reaping " + nai2.name());
                                teardownUnneededNetwork(nai2);
                            }
                        }
                    }
                }
            }
            isDisabledMobileNetwork = SAMPLE_DBG;
            affectedNetworks = new ArrayList();
            addedRequests = new ArrayList();
            log(" network has: " + newNetwork.networkCapabilities);
            for (NetworkRequestInfo nri2 : this.mNetworkRequests.values()) {
                currentNetwork = (NetworkAgentInfo) this.mNetworkForRequestId.get(nri2.request.requestId);
                satisfies = newNetwork.satisfies(nri2.request);
                log("mobile net can't satisfy default network request when mobile data disabled");
                satisfies = SAMPLE_DBG;
                if (newNetwork == currentNetwork) {
                }
                if (satisfies) {
                    if (checkNetworkSupportBip(newNetwork, nri2.request)) {
                        if (newNetwork.networkRequests.get(nri2.request.requestId) != null) {
                            log("Network " + newNetwork.name() + " stopped satisfying" + " request " + nri2.request.requestId);
                            newNetwork.networkRequests.remove(nri2.request.requestId);
                            if (currentNetwork == newNetwork) {
                                this.mNetworkForRequestId.remove(nri2.request.requestId);
                                sendUpdatedScoreToFactories(nri2.request, DISABLED);
                            } else if (nri2.isRequest()) {
                                Slog.wtf(TAG, "BUG: Removing request " + nri2.request.requestId + " from " + newNetwork.name() + " without updating mNetworkForRequestId or factories!");
                            }
                            callCallbackForRequest(nri2, newNetwork, 524292);
                        }
                    }
                }
                if (nri2.isRequest()) {
                    if (currentNetwork == null) {
                    }
                    log("currentScore = " + (currentNetwork == null ? currentNetwork.getCurrentScore() : DISABLED) + ", newScore = " + newNetwork.getCurrentScore());
                    if (currentNetwork != null) {
                    }
                    log("rematch for " + newNetwork.name());
                    if (currentNetwork == null) {
                        log("   accepting network in place of null");
                    } else {
                        log("   accepting network in place of " + currentNetwork.name());
                        currentNetwork.networkRequests.remove(nri2.request.requestId);
                        currentNetwork.networkLingered.add(nri2.request);
                        affectedNetworks.add(currentNetwork);
                    }
                    unlinger(newNetwork);
                    this.mNetworkForRequestId.put(nri2.request.requestId, newNetwork);
                    if (newNetwork.addRequest(nri2.request)) {
                        Slog.wtf(TAG, "BUG: " + newNetwork.name() + " already has " + nri2.request);
                    }
                    addedRequests.add(nri2);
                    keep = VDBG;
                    sendUpdatedScoreToFactories(nri2.request, newNetwork.getCurrentScore());
                    if (this.mDefaultRequest.requestId == nri2.request.requestId) {
                        isNewDefault = VDBG;
                        oldDefaultNetwork = currentNetwork;
                    }
                } else {
                    if (newNetwork.addRequest(nri2.request)) {
                        addedRequests.add(nri2);
                    }
                }
            }
            for (NetworkAgentInfo nai22 : affectedNetworks) {
                if (nai22.lingering) {
                    if (unneeded(nai22)) {
                        unlinger(nai22);
                    } else {
                        linger(nai22);
                    }
                }
            }
            if (NetworkFactory.isDualCellDataEnable()) {
                log("isDualCellDataEnable is true so keep is true");
                keep = VDBG;
            }
            if (isNewDefault) {
                makeDefault(newNetwork);
                logDefaultNetworkEvent(newNetwork, oldDefaultNetwork);
                synchronized (this) {
                    if (this.mNetTransitionWakeLock.isHeld()) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_CLEAR_NET_TRANSITION_WAKELOCK, this.mNetTransitionWakeLockSerialNumber, DISABLED), 1000);
                    }
                }
            }
            while (nri$iterator.hasNext()) {
                notifyNetworkCallback(newNetwork, notifyNetworkCallback);
            }
            if (isNewDefault) {
                if (oldDefaultNetwork != null) {
                    this.mLegacyTypeTracker.remove(oldDefaultNetwork.networkInfo.getType(), oldDefaultNetwork, VDBG);
                }
                if (newNetwork.lastValidated) {
                }
                this.mDefaultInetConditionPublished = newNetwork.lastValidated ? MIN_NET_ID : DISABLED;
                this.mLegacyTypeTracker.add(newNetwork.networkInfo.getType(), newNetwork);
                notifyLockdownVpn(newNetwork);
            }
            if (keep) {
                bs = BatteryStatsService.getService();
                type = newNetwork.networkInfo.getType();
                baseIface = newNetwork.linkProperties.getInterfaceName();
                bs.noteNetworkInterfaceType(baseIface, type);
                while (stacked$iterator.hasNext()) {
                    stackedIface = stacked.getInterfaceName();
                    bs.noteNetworkInterfaceType(stackedIface, type);
                    NetworkStatsFactory.noteStackedIface(stackedIface, baseIface);
                }
                i = DISABLED;
                while (true) {
                    if (i < newNetwork.networkRequests.size()) {
                        break;
                        if (newNetwork.isVPN()) {
                            this.mLegacyTypeTracker.add(EVENT_REGISTER_NETWORK_FACTORY, newNetwork);
                        }
                    } else {
                        nr = (NetworkRequest) newNetwork.networkRequests.valueAt(i);
                        i2 = nr.legacyType;
                        this.mLegacyTypeTracker.add(nr.legacyType, newNetwork);
                        i += ENABLED;
                    }
                }
            }
            if (reapUnvalidatedNetworks == ReapUnvalidatedNetworks.REAP) {
                for (NetworkAgentInfo nai222 : this.mNetworkAgentInfos.values()) {
                    if (unneeded(nai222)) {
                        log("Reaping " + nai222.name());
                        teardownUnneededNetwork(nai222);
                    }
                }
            }
        }
    }

    private void rematchAllNetworksAndRequests(NetworkAgentInfo changed, int oldScore) {
        if (changed == null || oldScore >= changed.getCurrentScore()) {
            NetworkAgentInfo[] nais = (NetworkAgentInfo[]) this.mNetworkAgentInfos.values().toArray(new NetworkAgentInfo[this.mNetworkAgentInfos.size()]);
            Arrays.sort(nais);
            int length = nais.length;
            for (int i = DISABLED; i < length; i += ENABLED) {
                ReapUnvalidatedNetworks reapUnvalidatedNetworks;
                NetworkAgentInfo nai = nais[i];
                if (nai != nais[nais.length - 1]) {
                    reapUnvalidatedNetworks = ReapUnvalidatedNetworks.DONT_REAP;
                } else {
                    reapUnvalidatedNetworks = ReapUnvalidatedNetworks.REAP;
                }
                rematchNetworkAndRequests(nai, reapUnvalidatedNetworks);
            }
            return;
        }
        rematchNetworkAndRequests(changed, ReapUnvalidatedNetworks.REAP);
    }

    private void updateInetCondition(NetworkAgentInfo nai) {
        if (nai.everValidated && isDefaultNetwork(nai)) {
            int newInetCondition = nai.lastValidated ? MIN_NET_ID : DISABLED;
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
        State state = newInfo.getState();
        int oldScore = networkAgent.getCurrentScore();
        synchronized (networkAgent) {
            NetworkInfo oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        notifyLockdownVpn(networkAgent);
        if (oldInfo == null || oldInfo.getState() != state) {
            log(networkAgent.name() + " EVENT_NETWORK_INFO_CHANGED, going from " + (oldInfo == null ? "null" : oldInfo.getState()) + " to " + state);
            enableDefaultTypeApnWhenWifiConnectionStateChanged(state, newInfo.getType());
            enableDefaultTypeApnWhenBlueToothTetheringStateChanged(networkAgent, newInfo);
            if (!networkAgent.created && (state == State.CONNECTED || (state == State.CONNECTING && networkAgent.isVPN()))) {
                try {
                    if (networkAgent.isVPN()) {
                        boolean z;
                        INetworkManagementService iNetworkManagementService = this.mNetd;
                        int i = networkAgent.network.netId;
                        boolean z2 = networkAgent.linkProperties.getDnsServers().isEmpty() ? SAMPLE_DBG : VDBG;
                        if (networkAgent.networkMisc == null) {
                            z = VDBG;
                        } else if (networkAgent.networkMisc.allowBypass) {
                            z = SAMPLE_DBG;
                        } else {
                            z = VDBG;
                        }
                        iNetworkManagementService.createVirtualNetwork(i, z2, z);
                    } else {
                        String str;
                        INetworkManagementService iNetworkManagementService2 = this.mNetd;
                        int i2 = networkAgent.network.netId;
                        if (networkAgent.networkCapabilities.hasCapability(13)) {
                            str = null;
                        } else {
                            str = NetworkManagementService.PERMISSION_SYSTEM;
                        }
                        iNetworkManagementService2.createPhysicalNetwork(i2, str);
                    }
                    networkAgent.created = VDBG;
                } catch (Exception e) {
                    loge("Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
                    return;
                }
            }
            if (!networkAgent.everConnected && state == State.CONNECTED) {
                networkAgent.everConnected = VDBG;
                updateLinkProperties(networkAgent, null);
                notifyIfacesChangedForNetworkStats();
                networkAgent.networkMonitor.sendMessage(NetworkMonitor.CMD_NETWORK_CONNECTED);
                scheduleUnvalidatedPrompt(networkAgent);
                if (networkAgent.isVPN()) {
                    setVpnSettingValue(VDBG);
                    synchronized (this.mProxyLock) {
                        if (!this.mDefaultProxyDisabled) {
                            this.mDefaultProxyDisabled = VDBG;
                            if (this.mGlobalProxy == null && this.mDefaultProxy != null) {
                                sendProxyBroadcast(null);
                            }
                        }
                    }
                }
                updateSignalStrengthThresholds(networkAgent, "CONNECT", null);
                rematchNetworkAndRequests(networkAgent, ReapUnvalidatedNetworks.REAP);
                notifyNetworkCallbacks(networkAgent, 524289);
            } else if (state == State.DISCONNECTED) {
                networkAgent.asyncChannel.disconnect();
                if (networkAgent.isVPN()) {
                    setVpnSettingValue(SAMPLE_DBG);
                    synchronized (this.mProxyLock) {
                        if (this.mDefaultProxyDisabled) {
                            this.mDefaultProxyDisabled = SAMPLE_DBG;
                            if (this.mGlobalProxy == null && this.mDefaultProxy != null) {
                                sendProxyBroadcast(this.mDefaultProxy);
                            }
                        }
                    }
                }
            } else {
                int i3;
                if (oldInfo == null || oldInfo.getState() != State.SUSPENDED) {
                    if (state == State.SUSPENDED) {
                    }
                }
                if (networkAgent.getCurrentScore() != oldScore) {
                    rematchAllNetworksAndRequests(networkAgent, oldScore);
                }
                if (state == State.SUSPENDED) {
                    i3 = 524299;
                } else {
                    i3 = 524300;
                }
                notifyNetworkCallbacks(networkAgent, i3);
                this.mLegacyTypeTracker.update(networkAgent);
            }
            hintUserSwitchToMobileWhileWifiDisconnected(state, newInfo.getType());
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
            score = DISABLED;
        }
        int oldScore = nai.getCurrentScore();
        nai.setCurrentScore(score);
        rematchAllNetworksAndRequests(nai, oldScore);
        sendUpdatedScoreToFactories(nai);
    }

    protected void notifyNetworkCallback(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        if (nri.mPendingIntent == null) {
            callCallbackForRequest(nri, nai, 524290);
        } else {
            sendPendingIntentForRequest(nri, nai, 524290);
        }
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
            intent.putExtra("isFailover", VDBG);
            nai.networkInfo.setFailover(SAMPLE_DBG);
        }
        if (info.getReason() != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra("extraInfo", info.getExtraInfo());
        }
        NetworkAgentInfo networkAgentInfo = null;
        if (nai.networkRequests.get(this.mDefaultRequest.requestId) != null) {
            networkAgentInfo = getDefaultNetwork();
            if (networkAgentInfo != null) {
                intent.putExtra("otherNetwork", networkAgentInfo.networkInfo);
            } else {
                intent.putExtra("noConnectivity", VDBG);
            }
        }
        intent.putExtra("inetCondition", this.mDefaultInetConditionPublished);
        sendStickyBroadcast(intent);
        if (networkAgentInfo != null) {
            sendConnectedBroadcast(networkAgentInfo.networkInfo);
        }
    }

    protected void notifyNetworkCallbacks(NetworkAgentInfo networkAgent, int notifyType) {
        log("notifyType " + notifyTypeToName(notifyType) + " for " + networkAgent.name());
        for (int i = DISABLED; i < networkAgent.networkRequests.size(); i += ENABLED) {
            NetworkRequestInfo nri = (NetworkRequestInfo) this.mNetworkRequests.get((NetworkRequest) networkAgent.networkRequests.valueAt(i));
            if (nri.mPendingIntent == null) {
                callCallbackForRequest(nri, networkAgent, notifyType);
            } else {
                sendPendingIntentForRequest(nri, networkAgent, notifyType);
            }
        }
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
        return NetworkMonitor.getCaptivePortalServerUrl(this.mContext);
    }

    public void startNattKeepalive(Network network, int intervalSeconds, Messenger messenger, IBinder binder, String srcAddr, int srcPort, String dstAddr) {
        enforceKeepalivePermission();
        this.mKeepaliveTracker.startNattKeepalive(getNetworkAgentInfoForNetwork(network), intervalSeconds, messenger, binder, srcAddr, srcPort, dstAddr, 4500);
    }

    public void stopKeepalive(Network network, int slot) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(528396, slot, DISABLED, network));
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int userId = UserHandle.getCallingUserId();
            setAirplaneMode(SAMPLE_DBG);
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                String[] tetheredIfaces = getTetheredIfaces();
                int length = tetheredIfaces.length;
                for (int i = DISABLED; i < length; i += ENABLED) {
                    untether(tetheredIfaces[i]);
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_config_vpn")) {
                synchronized (this.mVpns) {
                    String alwaysOnPackage = getAlwaysOnVpnPackage(userId);
                    if (alwaysOnPackage != null) {
                        setAlwaysOnVpnPackage(userId, null, SAMPLE_DBG);
                        setVpnPackageAuthorization(alwaysOnPackage, userId, SAMPLE_DBG);
                    }
                }
                VpnConfig vpnConfig = getVpnConfig(userId);
                if (vpnConfig != null) {
                    if (vpnConfig.legacy) {
                        prepareVpn("[Legacy VPN]", "[Legacy VPN]", userId);
                    } else {
                        setVpnPackageAuthorization(vpnConfig.user, userId, SAMPLE_DBG);
                        prepareVpn(null, "[Legacy VPN]", userId);
                    }
                }
            }
        }
    }

    public NetworkMonitor createNetworkMonitor(Context context, Handler handler, NetworkAgentInfo nai, NetworkRequest defaultRequest) {
        return new NetworkMonitor(context, handler, nai, defaultRequest);
    }

    private static void logDefaultNetworkEvent(NetworkAgentInfo newNai, NetworkAgentInfo prevNai) {
        int newNetid = DISABLED;
        int prevNetid = DISABLED;
        int[] transports = new int[DISABLED];
        boolean z = SAMPLE_DBG;
        boolean hadIPv6 = SAMPLE_DBG;
        if (newNai != null) {
            newNetid = newNai.network.netId;
            transports = newNai.networkCapabilities.getTransportTypes();
        }
        if (prevNai != null) {
            prevNetid = prevNai.network.netId;
            LinkProperties lp = prevNai.linkProperties;
            z = lp.hasIPv4Address() ? lp.hasIPv4DefaultRoute() : SAMPLE_DBG;
            hadIPv6 = lp.hasGlobalIPv6Address() ? lp.hasIPv6DefaultRoute() : SAMPLE_DBG;
        }
        DefaultNetworkEvent.logEvent(newNetid, transports, prevNetid, z, hadIPv6);
    }
}
