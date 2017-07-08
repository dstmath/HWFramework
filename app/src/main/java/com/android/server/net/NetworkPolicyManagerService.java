package com.android.server.net;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.IUidObserver;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager.Stub;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.NetworkIdentity;
import android.net.NetworkInfo;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkState;
import android.net.NetworkTemplate;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IDeviceIdleController;
import android.os.INetworkManagementService;
import android.os.IPowerManager;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DebugUtils;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TrustedTime;
import android.util.Xml;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.NetPluginDelegate;
import com.android.server.NetworkManagementService;
import com.android.server.SystemConfig;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.google.android.collect.Lists;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class NetworkPolicyManagerService extends Stub {
    private static final String ACTION_ALLOW_BACKGROUND = "com.android.server.net.action.ALLOW_BACKGROUND";
    private static final String ACTION_SNOOZE_WARNING = "com.android.server.net.action.SNOOZE_WARNING";
    private static final String ATTR_APP_ID = "appId";
    private static final String ATTR_CYCLE_DAY = "cycleDay";
    private static final String ATTR_CYCLE_TIMEZONE = "cycleTimezone";
    private static final String ATTR_INFERRED = "inferred";
    private static final String ATTR_LAST_LIMIT_SNOOZE = "lastLimitSnooze";
    private static final String ATTR_LAST_SNOOZE = "lastSnooze";
    private static final String ATTR_LAST_WARNING_SNOOZE = "lastWarningSnooze";
    private static final String ATTR_LIMIT_BYTES = "limitBytes";
    private static final String ATTR_METERED = "metered";
    private static final String ATTR_NETWORK_ID = "networkId";
    private static final String ATTR_NETWORK_TEMPLATE = "networkTemplate";
    private static final String ATTR_POLICY = "policy";
    private static final String ATTR_RESTRICT_BACKGROUND = "restrictBackground";
    private static final String ATTR_SUBSCRIBER_ID = "subscriberId";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_WARNING_BYTES = "warningBytes";
    private static final boolean GOOGLE_WARNING_DISABLED = true;
    private static final boolean LOGD = true;
    private static final boolean LOGV = false;
    private static final int MSG_ADVISE_PERSIST_THRESHOLD = 7;
    private static final int MSG_LIMIT_REACHED = 5;
    private static final int MSG_METERED_IFACES_CHANGED = 2;
    private static final int MSG_REMOVE_INTERFACE_QUOTA = 11;
    private static final int MSG_RESTRICT_BACKGROUND_BLACKLIST_CHANGED = 12;
    private static final int MSG_RESTRICT_BACKGROUND_CHANGED = 6;
    private static final int MSG_RESTRICT_BACKGROUND_WHITELIST_CHANGED = 9;
    private static final int MSG_RULES_CHANGED = 1;
    private static final int MSG_SCREEN_ON_CHANGED = 8;
    private static final int MSG_UPDATE_INTERFACE_QUOTA = 10;
    static final String TAG = "NetworkPolicy";
    private static final String TAG_APP_POLICY = "app-policy";
    private static final String TAG_NETWORK_POLICY = "network-policy";
    private static final String TAG_POLICY_LIST = "policy-list";
    private static final String TAG_RESTRICT_BACKGROUND = "restrict-background";
    private static final String TAG_REVOKED_RESTRICT_BACKGROUND = "revoked-restrict-background";
    private static final String TAG_UID_POLICY = "uid-policy";
    private static final String TAG_WHITELIST = "whitelist";
    private static final long TIME_CACHE_MAX_AGE = 86400000;
    public static final int TYPE_LIMIT = 2;
    public static final int TYPE_LIMIT_SNOOZED = 3;
    public static final int TYPE_WARNING = 1;
    private static final int VERSION_ADDED_INFERRED = 7;
    private static final int VERSION_ADDED_METERED = 4;
    private static final int VERSION_ADDED_NETWORK_ID = 9;
    private static final int VERSION_ADDED_RESTRICT_BACKGROUND = 3;
    private static final int VERSION_ADDED_SNOOZE = 2;
    private static final int VERSION_ADDED_TIMEZONE = 6;
    private static final int VERSION_INIT = 1;
    private static final int VERSION_LATEST = 10;
    private static final int VERSION_SPLIT_SNOOZE = 5;
    private static final int VERSION_SWITCH_APP_ID = 8;
    private static final int VERSION_SWITCH_UID = 10;
    private final ArraySet<String> mActiveNotifs;
    private final IActivityManager mActivityManager;
    private final INetworkManagementEventObserver mAlertObserver;
    private final BroadcastReceiver mAllowReceiver;
    private final AppOpsManager mAppOps;
    private IConnectivityManager mConnManager;
    private BroadcastReceiver mConnReceiver;
    private INetworkPolicyListener mConnectivityListener;
    private final Context mContext;
    private final SparseBooleanArray mDefaultRestrictBackgroundWhitelistUids;
    private IDeviceIdleController mDeviceIdleController;
    volatile boolean mDeviceIdleMode;
    final SparseBooleanArray mFirewallChainStates;
    final Handler mHandler;
    private Callback mHandlerCallback;
    private final IPackageManager mIPm;
    private final RemoteCallbackList<INetworkPolicyListener> mListeners;
    private ArraySet<String> mMeteredIfaces;
    private final INetworkManagementService mNetworkManager;
    final ArrayMap<NetworkTemplate, NetworkPolicy> mNetworkPolicy;
    final ArrayMap<NetworkPolicy, String[]> mNetworkRules;
    private final INetworkStatsService mNetworkStats;
    private INotificationManager mNotifManager;
    private final ArraySet<NetworkTemplate> mOverLimitNotified;
    private final BroadcastReceiver mPackageReceiver;
    private final AtomicFile mPolicyFile;
    private final IPowerManager mPowerManager;
    private PowerManagerInternal mPowerManagerInternal;
    private final SparseBooleanArray mPowerSaveTempWhitelistAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final BroadcastReceiver mPowerSaveWhitelistReceiver;
    volatile boolean mRestrictBackground;
    private final SparseBooleanArray mRestrictBackgroundWhitelistRevokedUids;
    private final SparseBooleanArray mRestrictBackgroundWhitelistUids;
    volatile boolean mRestrictPower;
    final Object mRulesLock;
    volatile boolean mScreenOn;
    private final BroadcastReceiver mScreenReceiver;
    private final BroadcastReceiver mSnoozeWarningReceiver;
    private final BroadcastReceiver mStatsReceiver;
    private final boolean mSuppressDefaultPolicy;
    volatile boolean mSystemReady;
    private final Runnable mTempPowerSaveChangedCallback;
    private final TrustedTime mTime;
    private long mTimeRefreshRealtime;
    final SparseIntArray mUidFirewallDozableRules;
    final SparseIntArray mUidFirewallPowerSaveRules;
    final SparseIntArray mUidFirewallStandbyRules;
    private final IUidObserver mUidObserver;
    final SparseIntArray mUidPolicy;
    private final BroadcastReceiver mUidRemovedReceiver;
    final SparseIntArray mUidRules;
    final SparseIntArray mUidState;
    private UsageStatsManagerInternal mUsageStats;
    private final UserManager mUserManager;
    private final BroadcastReceiver mUserReceiver;
    private final BroadcastReceiver mWifiConfigReceiver;
    private final BroadcastReceiver mWifiStateReceiver;

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            try {
                int uid = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackageUidAsUser(packageName, DumpState.DUMP_PREFERRED_XML, userId);
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updateRuleForAppIdleLocked(uid);
                    NetworkPolicyManagerService.this.updateRulesForPowerRestrictionsLocked(uid);
                }
            } catch (NameNotFoundException e) {
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                NetworkPolicyManagerService.this.updateRulesForAppIdleParoleLocked();
            }
        }
    }

    private class NetworkPolicyManagerInternalImpl extends NetworkPolicyManagerInternal {
        private NetworkPolicyManagerInternalImpl() {
        }

        public void resetUserState(int userId) {
            synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                boolean changed = NetworkPolicyManagerService.this.removeUserStateLocked(userId, NetworkPolicyManagerService.LOGV);
                if (NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsLocked(userId)) {
                    changed = NetworkPolicyManagerService.LOGD;
                }
                if (changed) {
                    NetworkPolicyManagerService.this.writePolicyLocked();
                }
            }
        }
    }

    public void removeUidPolicy(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.net.NetworkPolicyManagerService.removeUidPolicy(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkPolicyManagerService.removeUidPolicy(int, int):void");
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
        this(context, activityManager, powerManager, networkStats, networkManagement, NtpTrustedTime.getInstance(context), getSystemDir(), LOGV);
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement, TrustedTime time, File systemDir, boolean suppressDefaultPolicy) {
        this.mRulesLock = new Object();
        this.mNetworkPolicy = new ArrayMap();
        this.mNetworkRules = new ArrayMap();
        this.mUidPolicy = new SparseIntArray();
        this.mUidRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAppIds = new SparseBooleanArray();
        this.mPowerSaveTempWhitelistAppIds = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mDefaultRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistRevokedUids = new SparseBooleanArray();
        this.mMeteredIfaces = new ArraySet();
        this.mOverLimitNotified = new ArraySet();
        this.mActiveNotifs = new ArraySet();
        this.mUidState = new SparseIntArray();
        this.mListeners = new RemoteCallbackList();
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updateUidStateLocked(uid, procState);
                }
            }

            public void onUidGone(int uid) throws RemoteException {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.removeUidStateLocked(uid);
                }
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
            }
        };
        this.mPowerSaveWhitelistReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveWhitelistLocked();
                    NetworkPolicyManagerService.this.updateRulesForGlobalChangeLocked(NetworkPolicyManagerService.LOGV);
                }
            }
        };
        this.mTempPowerSaveChangedCallback = new Runnable() {
            public void run() {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveTempWhitelistLocked();
                    NetworkPolicyManagerService.this.updateRulesForTempWhitelistChangeLocked();
                    NetworkPolicyManagerService.this.purgePowerSaveTempWhitelistLocked();
                }
            }
        };
        this.mScreenReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.mHandler.obtainMessage(NetworkPolicyManagerService.VERSION_SWITCH_APP_ID).sendToTarget();
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1 && "android.intent.action.PACKAGE_ADDED".equals(action)) {
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidLocked(uid);
                    }
                }
            }
        };
        this.mUidRemovedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1) {
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicyManagerService.this.mUidPolicy.delete(uid);
                        NetworkPolicyManagerService.this.removeRestrictBackgroundWhitelistedUidLocked(uid, NetworkPolicyManagerService.LOGD, NetworkPolicyManagerService.LOGD);
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidLocked(uid);
                        NetworkPolicyManagerService.this.writePolicyLocked();
                    }
                }
            }
        };
        this.mUserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId != -1) {
                    if (!action.equals("android.intent.action.USER_REMOVED")) {
                        if (action.equals("android.intent.action.USER_ADDED")) {
                        }
                    }
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicyManagerService.this.removeUserStateLocked(userId, NetworkPolicyManagerService.LOGD);
                        if (action == "android.intent.action.USER_ADDED") {
                            NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsLocked(userId);
                        }
                        NetworkPolicyManagerService.this.updateRulesForGlobalChangeLocked(NetworkPolicyManagerService.LOGD);
                    }
                }
            }
        };
        this.mStatsReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updateNetworkEnabledLocked();
                    NetworkPolicyManagerService.this.updateNotificationsLocked();
                }
            }
        };
        this.mAllowReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.setRestrictBackground(NetworkPolicyManagerService.LOGV);
            }
        };
        this.mSnoozeWarningReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.performSnooze((NetworkTemplate) intent.getParcelableExtra("android.net.NETWORK_TEMPLATE"), NetworkPolicyManagerService.VERSION_INIT);
            }
        };
        this.mWifiConfigReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("changeReason", 0) == NetworkPolicyManagerService.VERSION_INIT) {
                    WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (config.SSID != null) {
                        NetworkTemplate template = NetworkTemplate.buildTemplateWifi(config.SSID);
                        synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                            if (NetworkPolicyManagerService.this.mNetworkPolicy.containsKey(template)) {
                                NetworkPolicyManagerService.this.mNetworkPolicy.remove(template);
                                NetworkPolicyManagerService.this.writePolicyLocked();
                            }
                        }
                    }
                }
            }
        };
        this.mWifiStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null && netInfo.isConnected()) {
                    WifiInfo info = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                    boolean meteredHint = info.getMeteredHint();
                    NetworkTemplate template = NetworkTemplate.buildTemplateWifi(info.getSSID());
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicy policy = (NetworkPolicy) NetworkPolicyManagerService.this.mNetworkPolicy.get(template);
                        if (policy == null && meteredHint) {
                            NetworkPolicyManagerService.this.addNetworkPolicyLocked(NetworkPolicyManagerService.newWifiPolicy(template, meteredHint));
                        } else if (policy != null) {
                            if (policy.inferred) {
                                policy.metered = meteredHint;
                                NetworkPolicyManagerService.this.updateNetworkRulesLocked();
                            }
                        }
                    }
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            public void limitReached(String limitName, String iface) {
                NetworkPolicyManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkPolicyManagerService.TAG);
                if (!NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(NetworkPolicyManagerService.VERSION_SPLIT_SNOOZE, iface).sendToTarget();
                }
            }
        };
        this.mConnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.ensureActiveMobilePolicyLocked();
                    NetworkPolicyManagerService.this.normalizePoliciesLocked();
                    NetworkPolicyManagerService.this.updateNetworkEnabledLocked();
                    NetworkPolicyManagerService.this.updateNetworkRulesLocked();
                    NetworkPolicyManagerService.this.updateNotificationsLocked();
                }
            }
        };
        this.mHandlerCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                int uid;
                int length;
                int i;
                Intent intent;
                int i2;
                switch (msg.what) {
                    case NetworkPolicyManagerService.VERSION_INIT /*1*/:
                        uid = msg.arg1;
                        int uidRules = msg.arg2;
                        NetworkPolicyManagerService.this.dispatchUidRulesChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, uidRules);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i += NetworkPolicyManagerService.VERSION_INIT) {
                            NetworkPolicyManagerService.this.dispatchUidRulesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, uidRules);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_ADDED_SNOOZE /*2*/:
                        String[] meteredIfaces = msg.obj;
                        NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged(NetworkPolicyManagerService.this.mConnectivityListener, meteredIfaces);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i += NetworkPolicyManagerService.VERSION_INIT) {
                            NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), meteredIfaces);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_SPLIT_SNOOZE /*5*/:
                        String iface = msg.obj;
                        NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                        synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                            if (NetworkPolicyManagerService.this.mMeteredIfaces.contains(iface)) {
                                try {
                                    NetworkPolicyManagerService.this.mNetworkStats.forceUpdate();
                                } catch (RemoteException e) {
                                }
                                NetworkPolicyManagerService.this.updateNetworkEnabledLocked();
                                NetworkPolicyManagerService.this.updateNotificationsLocked();
                                break;
                            }
                            break;
                        }
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_ADDED_TIMEZONE /*6*/:
                        boolean restrictBackground = msg.arg1 != 0 ? NetworkPolicyManagerService.LOGD : NetworkPolicyManagerService.LOGV;
                        NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged(NetworkPolicyManagerService.this.mConnectivityListener, restrictBackground);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i += NetworkPolicyManagerService.VERSION_INIT) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), restrictBackground);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                        intent.setFlags(1073741824);
                        NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_ADDED_INFERRED /*7*/:
                        try {
                            NetworkPolicyManagerService.this.mNetworkStats.advisePersistThreshold(((Long) msg.obj).longValue() / 1000);
                        } catch (RemoteException e2) {
                        }
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_SWITCH_APP_ID /*8*/:
                        NetworkPolicyManagerService.this.updateScreenOn();
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_ADDED_NETWORK_ID /*9*/:
                        uid = msg.arg1;
                        i2 = msg.arg2;
                        boolean changed = r0 == NetworkPolicyManagerService.VERSION_INIT ? NetworkPolicyManagerService.LOGD : NetworkPolicyManagerService.LOGV;
                        Boolean whitelisted = msg.obj;
                        if (whitelisted != null) {
                            boolean whitelistedBool = whitelisted.booleanValue();
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundWhitelistChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, whitelistedBool);
                            length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                            for (i = 0; i < length; i += NetworkPolicyManagerService.VERSION_INIT) {
                                NetworkPolicyManagerService.this.dispatchRestrictBackgroundWhitelistChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, whitelistedBool);
                            }
                            NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        }
                        String[] packages = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackagesForUid(uid);
                        if (changed && packages != null) {
                            int userId = UserHandle.getUserId(uid);
                            int length2 = packages.length;
                            for (i2 = 0; i2 < length2; i2 += NetworkPolicyManagerService.VERSION_INIT) {
                                String packageName = packages[i2];
                                intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                                intent.setPackage(packageName);
                                intent.setFlags(1073741824);
                                NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
                            }
                        }
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.VERSION_SWITCH_UID /*10*/:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        NetworkPolicyManagerService.this.setInterfaceQuota((String) msg.obj, (((long) msg.arg1) << 32) | (((long) msg.arg2) & 4294967295L));
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.MSG_REMOVE_INTERFACE_QUOTA /*11*/:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        return NetworkPolicyManagerService.LOGD;
                    case NetworkPolicyManagerService.MSG_RESTRICT_BACKGROUND_BLACKLIST_CHANGED /*12*/:
                        uid = msg.arg1;
                        i2 = msg.arg2;
                        boolean blacklisted = r0 == NetworkPolicyManagerService.VERSION_INIT ? NetworkPolicyManagerService.LOGD : NetworkPolicyManagerService.LOGV;
                        NetworkPolicyManagerService.this.dispatchRestrictBackgroundBlacklistChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, blacklisted);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i += NetworkPolicyManagerService.VERSION_INIT) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundBlacklistChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, blacklisted);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return NetworkPolicyManagerService.LOGD;
                    default:
                        return NetworkPolicyManagerService.LOGV;
                }
            }
        };
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing context");
        this.mActivityManager = (IActivityManager) Preconditions.checkNotNull(activityManager, "missing activityManager");
        this.mPowerManager = (IPowerManager) Preconditions.checkNotNull(powerManager, "missing powerManager");
        this.mNetworkStats = (INetworkStatsService) Preconditions.checkNotNull(networkStats, "missing networkStats");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManagement, "missing networkManagement");
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mTime = (TrustedTime) Preconditions.checkNotNull(time, "missing TrustedTime");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mIPm = AppGlobals.getPackageManager();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this.mHandlerCallback);
        this.mSuppressDefaultPolicy = suppressDefaultPolicy;
        this.mPolicyFile = new AtomicFile(new File(systemDir, "netpolicy.xml"));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        LocalServices.addService(NetworkPolicyManagerInternal.class, new NetworkPolicyManagerInternalImpl());
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void bindNotificationManager(INotificationManager notifManager) {
        this.mNotifManager = (INotificationManager) Preconditions.checkNotNull(notifManager, "missing INotificationManager");
    }

    void updatePowerSaveWhitelistLocked() {
        int i = 0;
        try {
            int i2;
            int[] whitelist = this.mDeviceIdleController.getAppIdWhitelistExceptIdle();
            this.mPowerSaveWhitelistExceptIdleAppIds.clear();
            if (whitelist != null) {
                int length = whitelist.length;
                for (i2 = 0; i2 < length; i2 += VERSION_INIT) {
                    this.mPowerSaveWhitelistExceptIdleAppIds.put(whitelist[i2], LOGD);
                }
            }
            whitelist = this.mDeviceIdleController.getAppIdWhitelist();
            this.mPowerSaveWhitelistAppIds.clear();
            if (whitelist != null) {
                i2 = whitelist.length;
                while (i < i2) {
                    this.mPowerSaveWhitelistAppIds.put(whitelist[i], LOGD);
                    i += VERSION_INIT;
                }
            }
        } catch (RemoteException e) {
        }
    }

    boolean addDefaultRestrictBackgroundWhitelistUidsLocked() {
        List<UserInfo> users = this.mUserManager.getUsers();
        int numberUsers = users.size();
        boolean changed = LOGV;
        for (int i = 0; i < numberUsers; i += VERSION_INIT) {
            if (addDefaultRestrictBackgroundWhitelistUidsLocked(((UserInfo) users.get(i)).id)) {
                changed = LOGD;
            }
        }
        return changed;
    }

    private boolean addDefaultRestrictBackgroundWhitelistUidsLocked(int userId) {
        SystemConfig sysConfig = SystemConfig.getInstance();
        PackageManager pm = this.mContext.getPackageManager();
        ArraySet<String> allowDataUsage = sysConfig.getAllowInDataUsageSave();
        boolean changed = LOGV;
        for (int i = 0; i < allowDataUsage.size(); i += VERSION_INIT) {
            String pkg = (String) allowDataUsage.valueAt(i);
            Slog.d(TAG, "checking restricted background whitelisting for package " + pkg + " and user " + userId);
            try {
                ApplicationInfo app = pm.getApplicationInfoAsUser(pkg, DumpState.DUMP_DEXOPT, userId);
                if (app.isPrivilegedApp()) {
                    int uid = UserHandle.getUid(userId, app.uid);
                    this.mDefaultRestrictBackgroundWhitelistUids.append(uid, LOGD);
                    Slog.d(TAG, "Adding uid " + uid + " (user " + userId + ") to default restricted " + "background whitelist. Revoked status: " + this.mRestrictBackgroundWhitelistRevokedUids.get(uid));
                    if (!this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                        Slog.i(TAG, "adding default package " + pkg + " (uid " + uid + " for user " + userId + ") to restrict background whitelist");
                        this.mRestrictBackgroundWhitelistUids.append(uid, LOGD);
                        changed = LOGD;
                    }
                } else {
                    Slog.wtf(TAG, "pm.getApplicationInfoAsUser() returned non-privileged app: " + pkg);
                }
            } catch (NameNotFoundException e) {
                Slog.wtf(TAG, "No ApplicationInfo for package " + pkg);
            }
        }
        return changed;
    }

    void updatePowerSaveTempWhitelistLocked() {
        try {
            int N = this.mPowerSaveTempWhitelistAppIds.size();
            for (int i = 0; i < N; i += VERSION_INIT) {
                this.mPowerSaveTempWhitelistAppIds.setValueAt(i, LOGV);
            }
            int[] whitelist = this.mDeviceIdleController.getAppIdTempWhitelist();
            if (whitelist != null) {
                int length = whitelist.length;
                for (int i2 = 0; i2 < length; i2 += VERSION_INIT) {
                    this.mPowerSaveTempWhitelistAppIds.put(whitelist[i2], LOGD);
                }
            }
        } catch (RemoteException e) {
        }
    }

    void purgePowerSaveTempWhitelistLocked() {
        for (int i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
            if (!this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                this.mPowerSaveTempWhitelistAppIds.removeAt(i);
            }
        }
    }

    public void systemReady() {
        if (isBandwidthControlEnabled()) {
            this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
            synchronized (this.mRulesLock) {
                updatePowerSaveWhitelistLocked();
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
                    public void onLowPowerModeChanged(boolean enabled) {
                        Slog.d(NetworkPolicyManagerService.TAG, "onLowPowerModeChanged(" + enabled + ")");
                        synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                            if (NetworkPolicyManagerService.this.mRestrictPower != enabled) {
                                NetworkPolicyManagerService.this.mRestrictPower = enabled;
                                NetworkPolicyManagerService.this.updateRulesForRestrictPowerLocked();
                                NetworkPolicyManagerService.this.updateRulesForGlobalChangeLocked(NetworkPolicyManagerService.LOGD);
                            }
                        }
                    }
                });
                this.mRestrictPower = this.mPowerManagerInternal.getLowPowerModeEnabled();
                this.mSystemReady = LOGD;
                readPolicyLocked();
                if (addDefaultRestrictBackgroundWhitelistUidsLocked()) {
                    writePolicyLocked();
                }
                updateRulesForGlobalChangeLocked(LOGV);
                updateNotificationsLocked();
            }
            updateScreenOn();
            try {
                this.mActivityManager.registerUidObserver(this.mUidObserver, VERSION_ADDED_RESTRICT_BACKGROUND);
                this.mNetworkManager.registerObserver(this.mAlertObserver);
            } catch (RemoteException e) {
            }
            IntentFilter screenFilter = new IntentFilter();
            screenFilter.addAction("android.intent.action.SCREEN_ON");
            screenFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mScreenReceiver, screenFilter);
            IntentFilter whitelistFilter = new IntentFilter("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
            this.mContext.registerReceiver(this.mPowerSaveWhitelistReceiver, whitelistFilter, null, this.mHandler);
            ((LocalService) LocalServices.getService(LocalService.class)).setNetworkPolicyTempWhitelistCallback(this.mTempPowerSaveChangedCallback);
            IntentFilter connFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            String str = "android.permission.CONNECTIVITY_INTERNAL";
            this.mContext.registerReceiver(this.mConnReceiver, connFilter, r19, this.mHandler);
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            this.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mHandler);
            this.mContext.registerReceiver(this.mUidRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction("android.intent.action.USER_ADDED");
            userFilter.addAction("android.intent.action.USER_REMOVED");
            this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
            IntentFilter statsFilter = new IntentFilter(NetworkStatsService.ACTION_NETWORK_STATS_UPDATED);
            str = "android.permission.READ_NETWORK_USAGE_HISTORY";
            this.mContext.registerReceiver(this.mStatsReceiver, statsFilter, r19, this.mHandler);
            IntentFilter allowFilter = new IntentFilter(ACTION_ALLOW_BACKGROUND);
            str = "android.permission.MANAGE_NETWORK_POLICY";
            this.mContext.registerReceiver(this.mAllowReceiver, allowFilter, r19, this.mHandler);
            IntentFilter snoozeWarningFilter = new IntentFilter(ACTION_SNOOZE_WARNING);
            str = "android.permission.MANAGE_NETWORK_POLICY";
            this.mContext.registerReceiver(this.mSnoozeWarningReceiver, snoozeWarningFilter, r19, this.mHandler);
            IntentFilter wifiConfigFilter = new IntentFilter("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
            this.mContext.registerReceiver(this.mWifiConfigReceiver, wifiConfigFilter, null, this.mHandler);
            IntentFilter wifiStateFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
            this.mContext.registerReceiver(this.mWifiStateReceiver, wifiStateFilter, null, this.mHandler);
            this.mUsageStats.addAppIdleStateChangeListener(new AppIdleStateChangeListener(null));
            return;
        }
        Slog.w(TAG, "bandwidth controls disabled, unable to enforce policy");
    }

    static NetworkPolicy newWifiPolicy(NetworkTemplate template, boolean metered) {
        return new NetworkPolicy(template, -1, "UTC", -1, -1, -1, -1, metered, LOGD);
    }

    void updateNotificationsLocked() {
        int i;
        ArraySet<String> beforeNotifs = new ArraySet(this.mActiveNotifs);
        this.mActiveNotifs.clear();
        long currentTime = currentTimeMillis();
        for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (isTemplateRelevant(policy.template) && policy.hasCycle()) {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                long totalBytes = getTotalBytes(policy.template, start, currentTime);
                if (!policy.isOverLimit(totalBytes)) {
                    notifyUnderLimitLocked(policy.template);
                    if (policy.isOverWarning(totalBytes) && policy.lastWarningSnooze < start) {
                        enqueueNotification(policy, VERSION_INIT, totalBytes);
                    }
                } else if (policy.lastLimitSnooze >= start) {
                    enqueueNotification(policy, VERSION_ADDED_RESTRICT_BACKGROUND, totalBytes);
                } else {
                    enqueueNotification(policy, VERSION_ADDED_SNOOZE, totalBytes);
                    notifyOverLimitLocked(policy.template);
                }
            }
        }
        for (i = beforeNotifs.size() - 1; i >= 0; i--) {
            String tag = (String) beforeNotifs.valueAt(i);
            if (!this.mActiveNotifs.contains(tag)) {
                cancelNotification(tag);
            }
        }
    }

    private boolean isTemplateRelevant(NetworkTemplate template) {
        if (!template.isMatchRuleMobile()) {
            return LOGD;
        }
        TelephonyManager tele = TelephonyManager.from(this.mContext);
        int[] subIds = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
        int length = subIds.length;
        for (int i = 0; i < length; i += VERSION_INIT) {
            if (template.matches(new NetworkIdentity(0, 0, tele.getSubscriberId(subIds[i]), null, LOGV, LOGD))) {
                return LOGD;
            }
        }
        return LOGV;
    }

    private void notifyOverLimitLocked(NetworkTemplate template) {
        if (!this.mOverLimitNotified.contains(template)) {
            this.mContext.startActivity(buildNetworkOverLimitIntent(template));
            this.mOverLimitNotified.add(template);
        }
    }

    private void notifyUnderLimitLocked(NetworkTemplate template) {
        this.mOverLimitNotified.remove(template);
    }

    private String buildNotificationTag(NetworkPolicy policy, int type) {
        return "NetworkPolicy:" + policy.template.hashCode() + ":" + type;
    }

    private void enqueueNotification(NetworkPolicy policy, int type, long totalBytes) {
        String tag = buildNotificationTag(policy, type);
        Builder builder = new Builder(this.mContext);
        builder.setOnlyAlertOnce(LOGD);
        builder.setWhen(0);
        builder.setColor(this.mContext.getColor(17170519));
        Resources res = this.mContext.getResources();
        CharSequence body;
        CharSequence text;
        switch (type) {
            case VERSION_INIT /*1*/:
                return;
            case VERSION_ADDED_SNOOZE /*2*/:
                body = res.getText(17040573);
                int icon = 17303210;
                switch (policy.template.getMatchRule()) {
                    case VERSION_INIT /*1*/:
                        text = res.getText(17040571);
                        break;
                    case VERSION_ADDED_SNOOZE /*2*/:
                        text = res.getText(17040569);
                        break;
                    case VERSION_ADDED_RESTRICT_BACKGROUND /*3*/:
                        text = res.getText(17040570);
                        break;
                    case VERSION_ADDED_METERED /*4*/:
                        text = res.getText(17040572);
                        icon = 17301624;
                        break;
                    default:
                        text = null;
                        break;
                }
                builder.setOngoing(LOGD);
                builder.setSmallIcon(icon);
                Bitmap bmp2 = BitmapFactory.decodeResource(res, 33751557);
                if (bmp2 != null) {
                    builder.setLargeIcon(bmp2);
                }
                builder.setTicker(text);
                builder.setContentTitle(text);
                builder.setContentText(body);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildNetworkOverLimitIntent(policy.template), 134217728));
                break;
            case VERSION_ADDED_RESTRICT_BACKGROUND /*3*/:
                Object[] objArr = new Object[VERSION_INIT];
                objArr[0] = Formatter.formatFileSize(this.mContext, totalBytes - policy.limitBytes);
                body = res.getString(17040578, objArr);
                switch (policy.template.getMatchRule()) {
                    case VERSION_INIT /*1*/:
                        text = res.getText(17040576);
                        break;
                    case VERSION_ADDED_SNOOZE /*2*/:
                        text = res.getText(17040574);
                        break;
                    case VERSION_ADDED_RESTRICT_BACKGROUND /*3*/:
                        text = res.getText(17040575);
                        break;
                    case VERSION_ADDED_METERED /*4*/:
                        text = res.getText(17040577);
                        break;
                    default:
                        text = null;
                        break;
                }
                builder.setOngoing(LOGD);
                builder.setSmallIcon(17301624);
                Bitmap bmp3 = BitmapFactory.decodeResource(res, 33751559);
                if (bmp3 != null) {
                    builder.setLargeIcon(bmp3);
                }
                builder.setTicker(text);
                builder.setContentTitle(text);
                builder.setContentText(body);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(policy.template), 134217728));
                return;
        }
        try {
            String packageName = this.mContext.getPackageName();
            String str = packageName;
            this.mNotifManager.enqueueNotificationWithTag(packageName, str, tag, 0, builder.getNotification(), new int[VERSION_INIT], -1);
            this.mActiveNotifs.add(tag);
        } catch (RemoteException e) {
        }
    }

    private void cancelNotification(String tag) {
        try {
            this.mNotifManager.cancelNotificationWithTag(this.mContext.getPackageName(), tag, 0, -1);
        } catch (RemoteException e) {
        }
    }

    void updateNetworkEnabledLocked() {
        long currentTime = currentTimeMillis();
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (policy.limitBytes == -1 || !policy.hasCycle()) {
                setNetworkTemplateEnabled(policy.template, LOGD);
            } else {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                boolean overLimitWithoutSnooze = policy.isOverLimit(getTotalBytes(policy.template, start, currentTime)) ? policy.lastLimitSnooze < start ? LOGD : LOGV : LOGV;
                setNetworkTemplateEnabled(policy.template, overLimitWithoutSnooze ? LOGV : LOGD);
            }
        }
    }

    private void setNetworkTemplateEnabled(NetworkTemplate template, boolean enabled) {
    }

    void updateNetworkRulesLocked() {
        try {
            int i;
            int i2;
            NetworkPolicy policy;
            String iface;
            NetworkState[] states = this.mConnManager.getAllNetworkState();
            ArrayList<Pair<String, NetworkIdentity>> connIdents = new ArrayList(states.length);
            ArraySet<String> connIfaces = new ArraySet(states.length);
            int length = states.length;
            for (i = 0; i < length; i += VERSION_INIT) {
                NetworkState state = states[i];
                if (!(state == null || state.networkInfo == null)) {
                    if (state.networkInfo.isConnected()) {
                        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                        String baseIface = state.linkProperties.getInterfaceName();
                        if (baseIface != null) {
                            connIdents.add(Pair.create(baseIface, ident));
                        }
                        for (LinkProperties stackedLink : state.linkProperties.getStackedLinks()) {
                            String stackedIface = stackedLink.getInterfaceName();
                            if (stackedIface != null) {
                                connIdents.add(Pair.create(stackedIface, ident));
                            }
                        }
                    }
                }
            }
            this.mNetworkRules.clear();
            ArrayList<String> ifaceList = Lists.newArrayList();
            for (i2 = this.mNetworkPolicy.size() - 1; i2 >= 0; i2--) {
                policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i2);
                ifaceList.clear();
                for (int j = connIdents.size() - 1; j >= 0; j--) {
                    Pair<String, NetworkIdentity> ident2 = (Pair) connIdents.get(j);
                    if (policy.template.matches((NetworkIdentity) ident2.second)) {
                        ifaceList.add((String) ident2.first);
                    }
                }
                if (ifaceList.size() > 0) {
                    this.mNetworkRules.put(policy, (String[]) ifaceList.toArray(new String[ifaceList.size()]));
                }
            }
            long lowestRule = JobStatus.NO_LATEST_RUNTIME;
            ArraySet<String> arraySet = new ArraySet(states.length);
            long currentTime = currentTimeMillis();
            for (i2 = this.mNetworkRules.size() - 1; i2 >= 0; i2--) {
                long start;
                long totalBytes;
                policy = (NetworkPolicy) this.mNetworkRules.keyAt(i2);
                String[] ifaces = (String[]) this.mNetworkRules.valueAt(i2);
                if (policy.hasCycle()) {
                    start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                    totalBytes = getTotalBytes(policy.template, start, currentTime);
                } else {
                    start = JobStatus.NO_LATEST_RUNTIME;
                    totalBytes = 0;
                }
                Slog.d(TAG, "applying policy " + policy + " to ifaces " + Arrays.toString(ifaces));
                boolean hasWarning = policy.warningBytes != -1 ? LOGD : LOGV;
                boolean hasLimit = policy.limitBytes != -1 ? LOGD : LOGV;
                if (hasLimit || policy.metered) {
                    long quotaBytes;
                    if (!hasLimit) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else if (policy.lastLimitSnooze >= start) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else {
                        quotaBytes = Math.max(1, policy.limitBytes - totalBytes);
                    }
                    if (ifaces.length > VERSION_INIT) {
                        Slog.w(TAG, "shared quota unsupported; generating rule for each iface");
                    }
                    length = ifaces.length;
                    for (i = 0; i < length; i += VERSION_INIT) {
                        iface = ifaces[i];
                        this.mHandler.obtainMessage(VERSION_SWITCH_UID, (int) (quotaBytes >> 32), (int) (-1 & quotaBytes), iface).sendToTarget();
                        arraySet.add(iface);
                    }
                }
                if (hasWarning && policy.warningBytes < lowestRule) {
                    lowestRule = policy.warningBytes;
                }
                if (hasLimit && policy.limitBytes < lowestRule) {
                    lowestRule = policy.limitBytes;
                }
            }
            for (i2 = connIfaces.size() - 1; i2 >= 0; i2--) {
                iface = (String) connIfaces.valueAt(i2);
                this.mHandler.obtainMessage(VERSION_SWITCH_UID, Integer.MAX_VALUE, -1, iface).sendToTarget();
                arraySet.add(iface);
            }
            this.mHandler.obtainMessage(VERSION_ADDED_INFERRED, Long.valueOf(lowestRule)).sendToTarget();
            for (i2 = this.mMeteredIfaces.size() - 1; i2 >= 0; i2--) {
                iface = (String) this.mMeteredIfaces.valueAt(i2);
                if (!arraySet.contains(iface)) {
                    this.mHandler.obtainMessage(MSG_REMOVE_INTERFACE_QUOTA, iface).sendToTarget();
                }
            }
            this.mMeteredIfaces = arraySet;
            this.mHandler.obtainMessage(VERSION_ADDED_SNOOZE, (String[]) this.mMeteredIfaces.toArray(new String[this.mMeteredIfaces.size()])).sendToTarget();
        } catch (RemoteException e) {
        }
    }

    private void ensureActiveMobilePolicyLocked() {
        if (!this.mSuppressDefaultPolicy) {
            TelephonyManager tele = TelephonyManager.from(this.mContext);
            int[] subIds = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
            int length = subIds.length;
            for (int i = 0; i < length; i += VERSION_INIT) {
                ensureActiveMobilePolicyLocked(tele.getSubscriberId(subIds[i]));
            }
        }
    }

    private void ensureActiveMobilePolicyLocked(String subscriberId) {
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, null, LOGV, LOGD);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkTemplate template = (NetworkTemplate) this.mNetworkPolicy.keyAt(i);
            if (template.matches(probeIdent)) {
                Slog.d(TAG, "Found template " + template + " which matches subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId));
                return;
            }
        }
        Slog.i(TAG, "No policy for subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId) + "; generating default policy");
        Time time = new Time();
        time.setToNow();
        addNetworkPolicyLocked(new NetworkPolicy(NetworkTemplate.buildTemplateMobileAll(subscriberId), time.monthDay, time.timezone, -1, -1, -1, -1, LOGD, LOGD));
    }

    protected void readPolicyLocked() {
        this.mNetworkPolicy.clear();
        this.mUidPolicy.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            int version = VERSION_INIT;
            boolean insideWhitelist = LOGV;
            while (true) {
                int type = in.next();
                if (type != VERSION_INIT) {
                    String tag = in.getName();
                    if (type == VERSION_ADDED_SNOOZE) {
                        if (TAG_POLICY_LIST.equals(tag)) {
                            boolean oldValue = this.mRestrictBackground;
                            version = XmlUtils.readIntAttribute(in, ATTR_VERSION);
                            if (version >= VERSION_ADDED_RESTRICT_BACKGROUND) {
                                this.mRestrictBackground = XmlUtils.readBooleanAttribute(in, ATTR_RESTRICT_BACKGROUND);
                            } else {
                                this.mRestrictBackground = LOGV;
                            }
                            if (this.mRestrictBackground != oldValue) {
                                this.mHandler.obtainMessage(VERSION_ADDED_TIMEZONE, this.mRestrictBackground ? VERSION_INIT : 0, 0).sendToTarget();
                            }
                        } else if (TAG_NETWORK_POLICY.equals(tag)) {
                            String attributeValue;
                            String cycleTimezone;
                            long lastLimitSnooze;
                            boolean metered;
                            long lastWarningSnooze;
                            boolean readBooleanAttribute;
                            int networkTemplate = XmlUtils.readIntAttribute(in, ATTR_NETWORK_TEMPLATE);
                            String subscriberId = in.getAttributeValue(null, ATTR_SUBSCRIBER_ID);
                            if (version >= VERSION_ADDED_NETWORK_ID) {
                                attributeValue = in.getAttributeValue(null, ATTR_NETWORK_ID);
                            } else {
                                attributeValue = null;
                            }
                            int cycleDay = XmlUtils.readIntAttribute(in, ATTR_CYCLE_DAY);
                            if (version >= VERSION_ADDED_TIMEZONE) {
                                cycleTimezone = in.getAttributeValue(null, ATTR_CYCLE_TIMEZONE);
                            } else {
                                cycleTimezone = "UTC";
                            }
                            long warningBytes = XmlUtils.readLongAttribute(in, ATTR_WARNING_BYTES);
                            long limitBytes = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES);
                            if (version >= VERSION_SPLIT_SNOOZE) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_LIMIT_SNOOZE);
                            } else if (version >= VERSION_ADDED_SNOOZE) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_SNOOZE);
                            } else {
                                lastLimitSnooze = -1;
                            }
                            if (version < VERSION_ADDED_METERED) {
                                switch (networkTemplate) {
                                    case VERSION_INIT /*1*/:
                                    case VERSION_ADDED_SNOOZE /*2*/:
                                    case VERSION_ADDED_RESTRICT_BACKGROUND /*3*/:
                                        metered = LOGD;
                                        break;
                                    default:
                                        metered = LOGV;
                                        break;
                                }
                            }
                            metered = XmlUtils.readBooleanAttribute(in, ATTR_METERED);
                            if (version >= VERSION_SPLIT_SNOOZE) {
                                lastWarningSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_WARNING_SNOOZE);
                            } else {
                                lastWarningSnooze = -1;
                            }
                            if (version >= VERSION_ADDED_INFERRED) {
                                readBooleanAttribute = XmlUtils.readBooleanAttribute(in, ATTR_INFERRED);
                            } else {
                                readBooleanAttribute = LOGV;
                            }
                            NetworkTemplate template = new NetworkTemplate(networkTemplate, subscriberId, attributeValue);
                            if (template.isPersistable()) {
                                this.mNetworkPolicy.put(template, new NetworkPolicy(template, cycleDay, cycleTimezone, warningBytes, limitBytes, lastWarningSnooze, lastLimitSnooze, metered, readBooleanAttribute));
                            }
                        } else if (TAG_UID_POLICY.equals(tag)) {
                            uid = XmlUtils.readIntAttribute(in, ATTR_UID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedLocked(uid, policy, LOGV);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_APP_POLICY.equals(tag)) {
                            int appId = XmlUtils.readIntAttribute(in, ATTR_APP_ID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            uid = UserHandle.getUid(0, appId);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedLocked(uid, policy, LOGV);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_WHITELIST.equals(tag)) {
                            insideWhitelist = LOGD;
                        } else if (TAG_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistUids.put(XmlUtils.readIntAttribute(in, ATTR_UID), LOGD);
                        } else if (TAG_REVOKED_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistRevokedUids.put(XmlUtils.readIntAttribute(in, ATTR_UID), LOGD);
                        }
                    } else if (type == VERSION_ADDED_RESTRICT_BACKGROUND && TAG_WHITELIST.equals(tag)) {
                        insideWhitelist = LOGV;
                    }
                } else {
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            upgradeLegacyBackgroundData();
        } catch (Throwable e2) {
            Log.wtf(TAG, "problem reading network policy", e2);
        } catch (Throwable e3) {
            Log.wtf(TAG, "problem reading network policy", e3);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void upgradeLegacyBackgroundData() {
        boolean z = LOGD;
        if (Secure.getInt(this.mContext.getContentResolver(), "background_data", VERSION_INIT) == VERSION_INIT) {
            z = LOGV;
        }
        this.mRestrictBackground = z;
        if (this.mRestrictBackground) {
            this.mContext.sendBroadcastAsUser(new Intent("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED"), UserHandle.ALL);
        }
    }

    void writePolicyLocked() {
        FileOutputStream fileOutputStream = null;
        try {
            int i;
            int uid;
            fileOutputStream = this.mPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(LOGD));
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, VERSION_SWITCH_UID);
            XmlUtils.writeBooleanAttribute(out, ATTR_RESTRICT_BACKGROUND, this.mRestrictBackground);
            for (i = 0; i < this.mNetworkPolicy.size(); i += VERSION_INIT) {
                NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
                NetworkTemplate template = policy.template;
                if (template.isPersistable()) {
                    out.startTag(null, TAG_NETWORK_POLICY);
                    XmlUtils.writeIntAttribute(out, ATTR_NETWORK_TEMPLATE, template.getMatchRule());
                    String subscriberId = template.getSubscriberId();
                    if (subscriberId != null) {
                        out.attribute(null, ATTR_SUBSCRIBER_ID, subscriberId);
                    }
                    String networkId = template.getNetworkId();
                    if (networkId != null) {
                        out.attribute(null, ATTR_NETWORK_ID, networkId);
                    }
                    XmlUtils.writeIntAttribute(out, ATTR_CYCLE_DAY, policy.cycleDay);
                    out.attribute(null, ATTR_CYCLE_TIMEZONE, policy.cycleTimezone);
                    XmlUtils.writeLongAttribute(out, ATTR_WARNING_BYTES, policy.warningBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LIMIT_BYTES, policy.limitBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_WARNING_SNOOZE, policy.lastWarningSnooze);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_LIMIT_SNOOZE, policy.lastLimitSnooze);
                    XmlUtils.writeBooleanAttribute(out, ATTR_METERED, policy.metered);
                    XmlUtils.writeBooleanAttribute(out, ATTR_INFERRED, policy.inferred);
                    out.endTag(null, TAG_NETWORK_POLICY);
                }
            }
            for (i = 0; i < this.mUidPolicy.size(); i += VERSION_INIT) {
                uid = this.mUidPolicy.keyAt(i);
                int policy2 = this.mUidPolicy.valueAt(i);
                if (policy2 != 0) {
                    out.startTag(null, TAG_UID_POLICY);
                    XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                    XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy2);
                    out.endTag(null, TAG_UID_POLICY);
                }
            }
            out.endTag(null, TAG_POLICY_LIST);
            out.startTag(null, TAG_WHITELIST);
            int size = this.mRestrictBackgroundWhitelistUids.size();
            for (i = 0; i < size; i += VERSION_INIT) {
                uid = this.mRestrictBackgroundWhitelistUids.keyAt(i);
                out.startTag(null, TAG_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                out.endTag(null, TAG_RESTRICT_BACKGROUND);
            }
            size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            for (i = 0; i < size; i += VERSION_INIT) {
                uid = this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i);
                out.startTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                out.endTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
            }
            out.endTag(null, TAG_WHITELIST);
            out.endDocument();
            this.mPolicyFile.finishWrite(fileOutputStream);
        } catch (IOException e) {
            if (fileOutputStream != null) {
                this.mPolicyFile.failWrite(fileOutputStream);
            }
        }
    }

    public void setUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mRulesLock) {
                long token = Binder.clearCallingIdentity();
                try {
                    int oldPolicy = this.mUidPolicy.get(uid, 0);
                    if (oldPolicy != policy) {
                        setUidPolicyUncheckedLocked(uid, oldPolicy, policy, LOGD);
                    }
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    public void addUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mRulesLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                policy |= oldPolicy;
                if (oldPolicy != policy) {
                    setUidPolicyUncheckedLocked(uid, oldPolicy, policy, LOGD);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    private void setUidPolicyUncheckedLocked(int uid, int oldPolicy, int policy, boolean persist) {
        int i = 0;
        setUidPolicyUncheckedLocked(uid, policy, persist);
        boolean isBlacklisted = policy == VERSION_INIT ? LOGD : LOGV;
        Handler handler = this.mHandler;
        if (isBlacklisted) {
            i = VERSION_INIT;
        }
        handler.obtainMessage(MSG_RESTRICT_BACKGROUND_BLACKLIST_CHANGED, uid, i).sendToTarget();
        boolean wasBlacklisted = oldPolicy == VERSION_INIT ? LOGD : LOGV;
        if ((oldPolicy == 0 && isBlacklisted) || (wasBlacklisted && policy == 0)) {
            this.mHandler.obtainMessage(VERSION_ADDED_NETWORK_ID, uid, VERSION_INIT, null).sendToTarget();
        }
    }

    private void setUidPolicyUncheckedLocked(int uid, int policy, boolean persist) {
        this.mUidPolicy.put(uid, policy);
        updateRulesForDataUsageRestrictionsLocked(uid);
        if (persist) {
            writePolicyLocked();
        }
    }

    public int getUidPolicy(int uid) {
        int i;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            i = this.mUidPolicy.get(uid, 0);
        }
        return i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int[] getUidsWithPolicy(int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        int[] uids = new int[0];
        synchronized (this.mRulesLock) {
            int i = 0;
            while (true) {
                if (i < this.mUidPolicy.size()) {
                    int uid = this.mUidPolicy.keyAt(i);
                    if (this.mUidPolicy.valueAt(i) == policy) {
                        uids = ArrayUtils.appendInt(uids, uid);
                    }
                    i += VERSION_INIT;
                }
            }
        }
        return uids;
    }

    boolean removeUserStateLocked(int userId, boolean writePolicy) {
        int i;
        int i2;
        int i3 = 0;
        boolean changed = LOGV;
        int[] wlUids = new int[0];
        for (i = 0; i < this.mRestrictBackgroundWhitelistUids.size(); i += VERSION_INIT) {
            int uid = this.mRestrictBackgroundWhitelistUids.keyAt(i);
            if (UserHandle.getUserId(uid) == userId) {
                wlUids = ArrayUtils.appendInt(wlUids, uid);
            }
        }
        if (wlUids.length > 0) {
            int length = wlUids.length;
            for (i2 = 0; i2 < length; i2 += VERSION_INIT) {
                removeRestrictBackgroundWhitelistedUidLocked(wlUids[i2], LOGV, LOGV);
            }
            changed = LOGD;
        }
        for (i = this.mRestrictBackgroundWhitelistRevokedUids.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i)) == userId) {
                this.mRestrictBackgroundWhitelistRevokedUids.removeAt(i);
                changed = LOGD;
            }
        }
        int[] uids = new int[0];
        for (i = 0; i < this.mUidPolicy.size(); i += VERSION_INIT) {
            uid = this.mUidPolicy.keyAt(i);
            if (UserHandle.getUserId(uid) == userId) {
                uids = ArrayUtils.appendInt(uids, uid);
            }
        }
        if (uids.length > 0) {
            i2 = uids.length;
            while (i3 < i2) {
                this.mUidPolicy.delete(uids[i3]);
                i3 += VERSION_INIT;
            }
            changed = LOGD;
        }
        updateRulesForGlobalChangeLocked(LOGD);
        if (writePolicy && changed) {
            writePolicyLocked();
        }
        return changed;
    }

    public void setConnectivityListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mConnectivityListener != null) {
            throw new IllegalStateException("Connectivity listener already registered");
        }
        this.mConnectivityListener = listener;
    }

    public void registerListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.register(listener);
    }

    public void unregisterListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.unregister(listener);
    }

    public void setNetworkPolicies(NetworkPolicy[] policies) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            maybeRefreshTrustedTime();
            synchronized (this.mRulesLock) {
                normalizePoliciesLocked(policies);
                updateNetworkEnabledLocked();
                updateNetworkRulesLocked();
                updateNotificationsLocked();
                writePolicyLocked();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void addNetworkPolicyLocked(NetworkPolicy policy) {
        setNetworkPolicies((NetworkPolicy[]) ArrayUtils.appendElement(NetworkPolicy.class, getNetworkPolicies(this.mContext.getOpPackageName()), policy));
    }

    public NetworkPolicy[] getNetworkPolicies(String callingPackage) {
        NetworkPolicy[] policies;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", TAG);
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", TAG);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                return new NetworkPolicy[0];
            }
        }
        synchronized (this.mRulesLock) {
            int size = this.mNetworkPolicy.size();
            policies = new NetworkPolicy[size];
            for (int i = 0; i < size; i += VERSION_INIT) {
                policies[i] = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            }
        }
        return policies;
    }

    private void normalizePoliciesLocked() {
        normalizePoliciesLocked(getNetworkPolicies(this.mContext.getOpPackageName()));
    }

    private void normalizePoliciesLocked(NetworkPolicy[] policies) {
        String[] merged = TelephonyManager.from(this.mContext).getMergedSubscriberIds();
        this.mNetworkPolicy.clear();
        int length = policies.length;
        for (int i = 0; i < length; i += VERSION_INIT) {
            NetworkPolicy policy = policies[i];
            policy.template = NetworkTemplate.normalize(policy.template, merged);
            NetworkPolicy existing = (NetworkPolicy) this.mNetworkPolicy.get(policy.template);
            if (existing == null || existing.compareTo(policy) > 0) {
                if (existing != null) {
                    Slog.d(TAG, "Normalization replaced " + existing + " with " + policy);
                }
                this.mNetworkPolicy.put(policy.template, policy);
            }
        }
    }

    public void snoozeLimit(NetworkTemplate template) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            performSnooze(template, VERSION_ADDED_SNOOZE);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void performSnooze(NetworkTemplate template, int type) {
        maybeRefreshTrustedTime();
        long currentTime = currentTimeMillis();
        synchronized (this.mRulesLock) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.get(template);
            if (policy == null) {
                throw new IllegalArgumentException("unable to find policy for " + template);
            }
            switch (type) {
                case VERSION_INIT /*1*/:
                    policy.lastWarningSnooze = currentTime;
                    break;
                case VERSION_ADDED_SNOOZE /*2*/:
                    policy.lastLimitSnooze = currentTime;
                    break;
                default:
                    throw new IllegalArgumentException("unexpected type");
            }
        }
    }

    public void onTetheringChanged(String iface, boolean tethering) {
        Log.d(TAG, "onTetherStateChanged(" + iface + ", " + tethering + ")");
        synchronized (this.mRulesLock) {
            if (this.mRestrictBackground && tethering) {
                Log.d(TAG, "Tethering on (" + iface + "); disable Data Saver");
                setRestrictBackground(LOGV);
            }
        }
    }

    public void setRestrictBackground(boolean restrictBackground) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            maybeRefreshTrustedTime();
            synchronized (this.mRulesLock) {
                if (restrictBackground == this.mRestrictBackground) {
                    Slog.w(TAG, "setRestrictBackground: already " + restrictBackground);
                    return;
                }
                int i;
                setRestrictBackgroundLocked(restrictBackground);
                Binder.restoreCallingIdentity(token);
                Handler handler = this.mHandler;
                if (restrictBackground) {
                    i = VERSION_INIT;
                } else {
                    i = 0;
                }
                handler.obtainMessage(VERSION_ADDED_TIMEZONE, i, 0).sendToTarget();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setRestrictBackgroundLocked(boolean restrictBackground) {
        Slog.d(TAG, "setRestrictBackgroundLocked(): " + restrictBackground);
        boolean oldRestrictBackground = this.mRestrictBackground;
        this.mRestrictBackground = restrictBackground;
        updateRulesForRestrictBackgroundLocked();
        try {
            if (!this.mNetworkManager.setDataSaverModeEnabled(this.mRestrictBackground)) {
                Slog.e(TAG, "Could not change Data Saver Mode on NMS to " + this.mRestrictBackground);
                this.mRestrictBackground = oldRestrictBackground;
                return;
            }
        } catch (RemoteException e) {
        }
        updateNotificationsLocked();
        writePolicyLocked();
    }

    public void addRestrictBackgroundWhitelistedUid(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            boolean oldStatus = this.mRestrictBackgroundWhitelistUids.get(uid);
            if (oldStatus) {
                Slog.d(TAG, "uid " + uid + " is already whitelisted");
                return;
            }
            boolean needFirewallRules = isUidValidForWhitelistRules(uid);
            Slog.i(TAG, "adding uid " + uid + " to restrict background whitelist");
            this.mRestrictBackgroundWhitelistUids.append(uid, LOGD);
            if (this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                Slog.d(TAG, "Removing uid " + uid + " from revoked restrict background whitelist");
                this.mRestrictBackgroundWhitelistRevokedUids.delete(uid);
            }
            if (needFirewallRules) {
                updateRulesForDataUsageRestrictionsLocked(uid);
            }
            writePolicyLocked();
            int changed = (this.mRestrictBackground && !oldStatus && needFirewallRules) ? VERSION_INIT : 0;
            this.mHandler.obtainMessage(VERSION_ADDED_NETWORK_ID, uid, changed, Boolean.TRUE).sendToTarget();
        }
    }

    public void removeRestrictBackgroundWhitelistedUid(int uid) {
        int i = VERSION_INIT;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            boolean changed = removeRestrictBackgroundWhitelistedUidLocked(uid, LOGV, LOGD);
        }
        Handler handler = this.mHandler;
        if (!changed) {
            i = 0;
        }
        handler.obtainMessage(VERSION_ADDED_NETWORK_ID, uid, i, Boolean.FALSE).sendToTarget();
    }

    private boolean removeRestrictBackgroundWhitelistedUidLocked(int uid, boolean uidDeleted, boolean updateNow) {
        boolean oldStatus = this.mRestrictBackgroundWhitelistUids.get(uid);
        if (oldStatus || uidDeleted) {
            boolean isUidValidForWhitelistRules = !uidDeleted ? isUidValidForWhitelistRules(uid) : LOGD;
            if (oldStatus) {
                Slog.i(TAG, "removing uid " + uid + " from restrict background whitelist");
                this.mRestrictBackgroundWhitelistUids.delete(uid);
            }
            if (this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && !this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                Slog.d(TAG, "Adding uid " + uid + " to revoked restrict background whitelist");
                this.mRestrictBackgroundWhitelistRevokedUids.append(uid, LOGD);
            }
            if (isUidValidForWhitelistRules) {
                updateRulesForDataUsageRestrictionsLocked(uid, uidDeleted);
            }
            if (updateNow) {
                writePolicyLocked();
            }
            if (!this.mRestrictBackground) {
                isUidValidForWhitelistRules = LOGV;
            }
            return isUidValidForWhitelistRules;
        }
        Slog.d(TAG, "uid " + uid + " was not whitelisted before");
        return LOGV;
    }

    public int[] getRestrictBackgroundWhitelistedUids() {
        int[] whitelist;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            int size = this.mRestrictBackgroundWhitelistUids.size();
            whitelist = new int[size];
            for (int i = 0; i < size; i += VERSION_INIT) {
                whitelist[i] = this.mRestrictBackgroundWhitelistUids.keyAt(i);
            }
        }
        return whitelist;
    }

    public int getRestrictBackgroundByCaller() {
        int i = VERSION_ADDED_RESTRICT_BACKGROUND;
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        int uid = Binder.getCallingUid();
        synchronized (this.mRulesLock) {
            long token = Binder.clearCallingIdentity();
            try {
                int policy = getUidPolicy(uid);
                Binder.restoreCallingIdentity(token);
                if (policy == VERSION_INIT) {
                    return VERSION_ADDED_RESTRICT_BACKGROUND;
                } else if (this.mRestrictBackground) {
                    if (this.mRestrictBackgroundWhitelistUids.get(uid)) {
                        i = VERSION_ADDED_SNOOZE;
                    }
                    return i;
                } else {
                    return VERSION_INIT;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public boolean getRestrictBackground() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            z = this.mRestrictBackground;
        }
        return z;
    }

    public void setDeviceIdleMode(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            if (this.mDeviceIdleMode != enabled) {
                this.mDeviceIdleMode = enabled;
                if (this.mSystemReady) {
                    updateRulesForGlobalChangeLocked(LOGV);
                }
                if (enabled) {
                    EventLogTags.writeDeviceIdleOnPhase("net");
                } else {
                    EventLogTags.writeDeviceIdleOffPhase("net");
                }
            }
        }
    }

    private NetworkPolicy findPolicyForNetworkLocked(NetworkIdentity ident) {
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (policy.template.matches(ident)) {
                return policy;
            }
        }
        return null;
    }

    public NetworkQuotaInfo getNetworkQuotaInfo(NetworkState state) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            NetworkQuotaInfo networkQuotaInfoUnchecked = getNetworkQuotaInfoUnchecked(state);
            return networkQuotaInfoUnchecked;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private NetworkQuotaInfo getNetworkQuotaInfoUnchecked(NetworkState state) {
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        synchronized (this.mRulesLock) {
            NetworkPolicy policy = findPolicyForNetworkLocked(ident);
        }
        if (policy == null || !policy.hasCycle()) {
            return null;
        }
        long softLimitBytes;
        long hardLimitBytes;
        long currentTime = currentTimeMillis();
        long end = currentTime;
        long totalBytes = getTotalBytes(policy.template, NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy), currentTime);
        if (policy.warningBytes != -1) {
            softLimitBytes = policy.warningBytes;
        } else {
            softLimitBytes = -1;
        }
        if (policy.limitBytes != -1) {
            hardLimitBytes = policy.limitBytes;
        } else {
            hardLimitBytes = -1;
        }
        return new NetworkQuotaInfo(totalBytes, softLimitBytes, hardLimitBytes);
    }

    public boolean isNetworkMetered(NetworkState state) {
        if (state.networkInfo == null) {
            return LOGV;
        }
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        if (ident.getRoaming()) {
            return LOGD;
        }
        synchronized (this.mRulesLock) {
            NetworkPolicy policy = findPolicyForNetworkLocked(ident);
        }
        if (policy != null) {
            return policy.metered;
        }
        int type = state.networkInfo.getType();
        return (ConnectivityManager.isNetworkTypeMobile(type) || type == VERSION_ADDED_TIMEZONE) ? LOGD : LOGV;
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter fout = new IndentingPrintWriter(writer, "  ");
        ArraySet<String> argSet = new ArraySet(args.length);
        int length = args.length;
        for (int i = 0; i < length; i += VERSION_INIT) {
            argSet.add(args[i]);
        }
        synchronized (this.mRulesLock) {
            int i2;
            if (argSet.contains("--unsnooze")) {
                for (i2 = this.mNetworkPolicy.size() - 1; i2 >= 0; i2--) {
                    ((NetworkPolicy) this.mNetworkPolicy.valueAt(i2)).clearSnooze();
                }
                normalizePoliciesLocked();
                updateNetworkEnabledLocked();
                updateNetworkRulesLocked();
                updateNotificationsLocked();
                writePolicyLocked();
                fout.println("Cleared snooze timestamps");
                return;
            }
            fout.print("System ready: ");
            fout.println(this.mSystemReady);
            fout.print("Restrict background: ");
            fout.println(this.mRestrictBackground);
            fout.print("Restrict power: ");
            fout.println(this.mRestrictPower);
            fout.print("Device idle: ");
            fout.println(this.mDeviceIdleMode);
            fout.println("Network policies:");
            fout.increaseIndent();
            for (i2 = 0; i2 < this.mNetworkPolicy.size(); i2 += VERSION_INIT) {
                fout.println(((NetworkPolicy) this.mNetworkPolicy.valueAt(i2)).toString());
            }
            fout.decreaseIndent();
            fout.print("Metered ifaces: ");
            fout.println(String.valueOf(this.mMeteredIfaces));
            fout.println("Policy for UIDs:");
            fout.increaseIndent();
            int size = this.mUidPolicy.size();
            for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                int uid = this.mUidPolicy.keyAt(i2);
                int policy = this.mUidPolicy.valueAt(i2);
                fout.print("UID=");
                fout.print(uid);
                fout.print(" policy=");
                fout.print(DebugUtils.flagsToString(NetworkPolicyManager.class, "POLICY_", policy));
                fout.println();
            }
            fout.decreaseIndent();
            size = this.mPowerSaveWhitelistExceptIdleAppIds.size();
            if (size > 0) {
                fout.println("Power save whitelist (except idle) app ids:");
                fout.increaseIndent();
                for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                    fout.print("UID=");
                    fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i2));
                    fout.print(": ");
                    fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.valueAt(i2));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mPowerSaveWhitelistAppIds.size();
            if (size > 0) {
                fout.println("Power save whitelist app ids:");
                fout.increaseIndent();
                for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                    fout.print("UID=");
                    fout.print(this.mPowerSaveWhitelistAppIds.keyAt(i2));
                    fout.print(": ");
                    fout.print(this.mPowerSaveWhitelistAppIds.valueAt(i2));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mRestrictBackgroundWhitelistUids.size();
            if (size > 0) {
                fout.println("Restrict background whitelist uids:");
                fout.increaseIndent();
                for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                    fout.print("UID=");
                    fout.print(this.mRestrictBackgroundWhitelistUids.keyAt(i2));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mDefaultRestrictBackgroundWhitelistUids.size();
            if (size > 0) {
                fout.println("Default restrict background whitelist uids:");
                fout.increaseIndent();
                for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                    fout.print("UID=");
                    fout.print(this.mDefaultRestrictBackgroundWhitelistUids.keyAt(i2));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            if (size > 0) {
                fout.println("Default restrict background whitelist uids revoked by users:");
                fout.increaseIndent();
                for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                    fout.print("UID=");
                    fout.print(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i2));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            SparseBooleanArray knownUids = new SparseBooleanArray();
            collectKeys(this.mUidState, knownUids);
            collectKeys(this.mUidRules, knownUids);
            fout.println("Status for all known UIDs:");
            fout.increaseIndent();
            size = knownUids.size();
            for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                uid = knownUids.keyAt(i2);
                fout.print("UID=");
                fout.print(uid);
                int state = this.mUidState.get(uid, 16);
                fout.print(" state=");
                fout.print(state);
                if (state <= VERSION_ADDED_SNOOZE) {
                    fout.print(" (fg)");
                } else {
                    fout.print(state <= VERSION_ADDED_METERED ? " (fg svc)" : " (bg)");
                }
                int uidRules = this.mUidRules.get(uid, 0);
                fout.print(" rules=");
                fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                fout.println();
            }
            fout.decreaseIndent();
            fout.println("Status for just UIDs with rules:");
            fout.increaseIndent();
            size = this.mUidRules.size();
            for (i2 = 0; i2 < size; i2 += VERSION_INIT) {
                uid = this.mUidRules.keyAt(i2);
                fout.print("UID=");
                fout.print(uid);
                uidRules = this.mUidRules.get(uid, 0);
                fout.print(" rules=");
                fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                fout.println();
            }
            fout.decreaseIndent();
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        new NetworkPolicyManagerShellCommand(this.mContext, this).exec(this, in, out, err, args, resultReceiver);
    }

    public boolean isUidForeground(int uid) {
        boolean isUidForegroundLocked;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            isUidForegroundLocked = isUidForegroundLocked(uid);
        }
        return isUidForegroundLocked;
    }

    private boolean isUidForegroundLocked(int uid) {
        return isUidStateForegroundLocked(this.mUidState.get(uid, 16));
    }

    private boolean isUidForegroundOnRestrictBackgroundLocked(int uid) {
        return isProcStateAllowedWhileOnRestrictBackgroundLocked(this.mUidState.get(uid, 16));
    }

    private boolean isUidForegroundOnRestrictPowerLocked(int uid) {
        return isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 16));
    }

    private boolean isUidStateForegroundLocked(int state) {
        return (!this.mScreenOn || state > VERSION_ADDED_SNOOZE) ? LOGV : LOGD;
    }

    private void updateUidStateLocked(int uid, int uidState) {
        int oldUidState = this.mUidState.get(uid, 16);
        if (oldUidState != uidState) {
            this.mUidState.put(uid, uidState);
            updateRestrictBackgroundRulesOnUidStatusChangedLocked(uid, oldUidState, uidState);
            if (isProcStateAllowedWhileIdleOrPowerSaveMode(oldUidState) != isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                if (isUidIdle(uid)) {
                    updateRuleForAppIdleLocked(uid);
                }
                if (this.mDeviceIdleMode) {
                    updateRuleForDeviceIdleLocked(uid);
                }
                if (this.mRestrictPower) {
                    updateRuleForRestrictPowerLocked(uid);
                }
                updateRulesForPowerRestrictionsLocked(uid);
            }
            updateNetworkStats(uid, isUidStateForegroundLocked(uidState));
        }
    }

    private void removeUidStateLocked(int uid) {
        int index = this.mUidState.indexOfKey(uid);
        if (index >= 0) {
            int oldUidState = this.mUidState.valueAt(index);
            this.mUidState.removeAt(index);
            if (oldUidState != 16) {
                updateRestrictBackgroundRulesOnUidStatusChangedLocked(uid, oldUidState, 16);
                if (this.mDeviceIdleMode) {
                    updateRuleForDeviceIdleLocked(uid);
                }
                if (this.mRestrictPower) {
                    updateRuleForRestrictPowerLocked(uid);
                }
                updateRulesForPowerRestrictionsLocked(uid);
                updateNetworkStats(uid, LOGV);
            }
        }
    }

    private void updateNetworkStats(int uid, boolean uidForeground) {
        try {
            this.mNetworkStats.setUidForeground(uid, uidForeground);
        } catch (RemoteException e) {
        }
    }

    private void updateRestrictBackgroundRulesOnUidStatusChangedLocked(int uid, int oldUidState, int newUidState) {
        if (isProcStateAllowedWhileOnRestrictBackgroundLocked(oldUidState) != isProcStateAllowedWhileOnRestrictBackgroundLocked(newUidState)) {
            updateRulesForDataUsageRestrictionsLocked(uid);
        }
    }

    private void updateScreenOn() {
        synchronized (this.mRulesLock) {
            try {
                this.mScreenOn = this.mPowerManager.isInteractive();
            } catch (RemoteException e) {
            }
            updateRulesForScreenLocked();
        }
    }

    private void updateRulesForScreenLocked() {
        int size = this.mUidState.size();
        for (int i = 0; i < size; i += VERSION_INIT) {
            if (this.mUidState.valueAt(i) <= VERSION_ADDED_METERED) {
                updateRestrictionRulesForUidLocked(this.mUidState.keyAt(i));
            }
        }
    }

    static boolean isProcStateAllowedWhileIdleOrPowerSaveMode(int procState) {
        return procState <= VERSION_ADDED_METERED ? LOGD : LOGV;
    }

    static boolean isProcStateAllowedWhileOnRestrictBackgroundLocked(int procState) {
        return procState <= VERSION_ADDED_METERED ? LOGD : LOGV;
    }

    void updateRulesForRestrictPowerLocked() {
        updateRulesForWhitelistedPowerSaveLocked(this.mRestrictPower, (int) VERSION_ADDED_RESTRICT_BACKGROUND, this.mUidFirewallPowerSaveRules);
    }

    void updateRuleForRestrictPowerLocked(int uid) {
        updateRulesForWhitelistedPowerSaveLocked(uid, this.mRestrictPower, (int) VERSION_ADDED_RESTRICT_BACKGROUND);
    }

    void updateRulesForDeviceIdleLocked() {
        updateRulesForWhitelistedPowerSaveLocked(this.mDeviceIdleMode, (int) VERSION_INIT, this.mUidFirewallDozableRules);
    }

    void updateRuleForDeviceIdleLocked(int uid) {
        updateRulesForWhitelistedPowerSaveLocked(uid, this.mDeviceIdleMode, (int) VERSION_INIT);
    }

    private void updateRulesForWhitelistedPowerSaveLocked(boolean enabled, int chain, SparseIntArray rules) {
        if (enabled) {
            int i;
            SparseIntArray uidRules = rules;
            rules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                UserInfo user = (UserInfo) users.get(ui);
                for (i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
                    if (this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                        rules.put(UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(i)), VERSION_INIT);
                    }
                }
                for (i = this.mPowerSaveWhitelistAppIds.size() - 1; i >= 0; i--) {
                    rules.put(UserHandle.getUid(user.id, this.mPowerSaveWhitelistAppIds.keyAt(i)), VERSION_INIT);
                }
            }
            for (i = this.mUidState.size() - 1; i >= 0; i--) {
                if (isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.valueAt(i))) {
                    rules.put(this.mUidState.keyAt(i), VERSION_INIT);
                }
            }
            setUidFirewallRules(chain, rules);
        }
        enableFirewallChainLocked(chain, enabled);
    }

    private void updateRulesForNonMeteredNetworksLocked() {
    }

    private boolean isWhitelistedBatterySaverLocked(int uid) {
        int appId = UserHandle.getAppId(uid);
        return !this.mPowerSaveTempWhitelistAppIds.get(appId) ? this.mPowerSaveWhitelistAppIds.get(appId) : LOGD;
    }

    private void updateRulesForWhitelistedPowerSaveLocked(int uid, boolean enabled, int chain) {
        if (!enabled) {
            return;
        }
        if (isWhitelistedBatterySaverLocked(uid) || isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid))) {
            setUidFirewallRule(chain, uid, VERSION_INIT);
        } else {
            setUidFirewallRule(chain, uid, 0);
        }
    }

    void updateRulesForAppIdleLocked() {
        SparseIntArray uidRules = this.mUidFirewallStandbyRules;
        uidRules.clear();
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int ui = users.size() - 1; ui >= 0; ui--) {
            int[] idleUids = this.mUsageStats.getIdleUidsForUser(((UserInfo) users.get(ui)).id);
            int length = idleUids.length;
            for (int i = 0; i < length; i += VERSION_INIT) {
                int uid = idleUids[i];
                if (!this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid), LOGV) && hasInternetPermissions(uid)) {
                    uidRules.put(uid, VERSION_ADDED_SNOOZE);
                }
            }
        }
        setUidFirewallRules(VERSION_ADDED_SNOOZE, uidRules);
    }

    void updateRuleForAppIdleLocked(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            if (this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid)) || !isUidIdle(uid) || isUidForegroundOnRestrictPowerLocked(uid)) {
                setUidFirewallRule(VERSION_ADDED_SNOOZE, uid, 0);
            } else {
                setUidFirewallRule(VERSION_ADDED_SNOOZE, uid, VERSION_ADDED_SNOOZE);
            }
        }
    }

    void updateRulesForAppIdleParoleLocked() {
        enableFirewallChainLocked(VERSION_ADDED_SNOOZE, this.mUsageStats.isAppIdleParoleOn() ? LOGV : LOGD);
    }

    private void updateRulesForGlobalChangeLocked(boolean restrictedNetworksChanged) {
        long start = System.currentTimeMillis();
        updateRulesForDeviceIdleLocked();
        updateRulesForAppIdleLocked();
        updateRulesForRestrictPowerLocked();
        updateRulesForRestrictBackgroundLocked();
        setRestrictBackgroundLocked(this.mRestrictBackground);
        if (restrictedNetworksChanged) {
            normalizePoliciesLocked();
            updateNetworkRulesLocked();
        }
        Slog.d(TAG, "updateRulesForGlobalChangeLocked(" + restrictedNetworksChanged + ") took " + (System.currentTimeMillis() - start) + "ms");
    }

    private void updateRulesForRestrictBackgroundLocked() {
        PackageManager pm = this.mContext.getPackageManager();
        List<UserInfo> users = this.mUserManager.getUsers();
        List<ApplicationInfo> apps = pm.getInstalledApplications(795136);
        int usersSize = users.size();
        int appsSize = apps.size();
        for (int i = 0; i < usersSize; i += VERSION_INIT) {
            UserInfo user = (UserInfo) users.get(i);
            for (int j = 0; j < appsSize; j += VERSION_INIT) {
                int uid = UserHandle.getUid(user.id, ((ApplicationInfo) apps.get(j)).uid);
                updateRulesForDataUsageRestrictionsLocked(uid);
                updateRulesForPowerRestrictionsLocked(uid);
            }
        }
    }

    private void updateRulesForTempWhitelistChangeLocked() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i += VERSION_INIT) {
            UserInfo user = (UserInfo) users.get(i);
            for (int j = this.mPowerSaveTempWhitelistAppIds.size() - 1; j >= 0; j--) {
                int uid = UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(j));
                updateRuleForAppIdleLocked(uid);
                updateRuleForDeviceIdleLocked(uid);
                updateRuleForRestrictPowerLocked(uid);
                updateRulesForPowerRestrictionsLocked(uid);
            }
        }
    }

    private boolean isUidValidForBlacklistRules(int uid) {
        if (uid == 1013 || uid == 1019 || (UserHandle.isApp(uid) && hasInternetPermissions(uid))) {
            return LOGD;
        }
        return LOGV;
    }

    private boolean isUidValidForWhitelistRules(int uid) {
        return UserHandle.isApp(uid) ? hasInternetPermissions(uid) : LOGV;
    }

    private boolean isUidIdle(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        int userId = UserHandle.getUserId(uid);
        if (!ArrayUtils.isEmpty(packages)) {
            int length = packages.length;
            for (int i = 0; i < length; i += VERSION_INIT) {
                if (!this.mUsageStats.isAppIdle(packages[i], uid, userId)) {
                    return LOGV;
                }
            }
        }
        return LOGD;
    }

    private boolean hasInternetPermissions(int uid) {
        try {
            if (this.mIPm.checkUidPermission("android.permission.INTERNET", uid) != 0) {
                return LOGV;
            }
        } catch (RemoteException e) {
        }
        return LOGD;
    }

    private void updateRestrictionRulesForUidLocked(int uid) {
        updateRuleForDeviceIdleLocked(uid);
        updateRuleForAppIdleLocked(uid);
        updateRuleForRestrictPowerLocked(uid);
        updateRulesForPowerRestrictionsLocked(uid);
        updateRulesForDataUsageRestrictionsLocked(uid);
    }

    private void updateRulesForDataUsageRestrictionsLocked(int uid) {
        updateRulesForDataUsageRestrictionsLocked(uid, LOGV);
    }

    private void updateRulesForDataUsageRestrictionsLocked(int uid, boolean uidDeleted) {
        if (uidDeleted || isUidValidForWhitelistRules(uid)) {
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictBackgroundLocked(uid);
            boolean isBlacklisted = (uidPolicy & VERSION_INIT) != 0 ? LOGD : LOGV;
            boolean isWhitelisted = this.mRestrictBackgroundWhitelistUids.get(uid);
            int oldRule = oldUidRules & 15;
            int newRule = 0;
            if (isForeground) {
                if (isBlacklisted || (this.mRestrictBackground && !isWhitelisted)) {
                    newRule = VERSION_ADDED_SNOOZE;
                } else if (isWhitelisted) {
                    newRule = VERSION_INIT;
                }
            } else if (isBlacklisted) {
                newRule = VERSION_ADDED_METERED;
            } else if (this.mRestrictBackground && isWhitelisted) {
                newRule = VERSION_INIT;
            }
            int newUidRules = newRule | (oldUidRules & 240);
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                if ((newRule & VERSION_ADDED_SNOOZE) != 0) {
                    setMeteredNetworkWhitelist(uid, LOGD);
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, LOGV);
                    }
                } else if ((oldRule & VERSION_ADDED_SNOOZE) != 0) {
                    if (!isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, LOGV);
                    }
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, LOGD);
                    }
                } else if ((newRule & VERSION_ADDED_METERED) != 0 || (oldRule & VERSION_ADDED_METERED) != 0) {
                    setMeteredNetworkBlacklist(uid, isBlacklisted);
                    if ((oldRule & VERSION_ADDED_METERED) != 0 && isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, isWhitelisted);
                    }
                } else if ((newRule & VERSION_INIT) == 0 && (oldRule & VERSION_INIT) == 0) {
                    Log.wtf(TAG, "Unexpected change of metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", blacklisted=" + isBlacklisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                } else {
                    setMeteredNetworkWhitelist(uid, isWhitelisted);
                }
                this.mHandler.obtainMessage(VERSION_INIT, uid, newUidRules).sendToTarget();
            }
        }
    }

    private void updateRulesForPowerRestrictionsLocked(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            boolean restrictMode = (isUidIdle(uid) || this.mRestrictPower) ? LOGD : this.mDeviceIdleMode;
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictPowerLocked(uid);
            boolean isWhitelisted = isWhitelistedBatterySaverLocked(uid);
            int oldRule = oldUidRules & 240;
            int newRule = 0;
            if (isForeground) {
                if (restrictMode) {
                    newRule = 32;
                }
            } else if (restrictMode) {
                newRule = isWhitelisted ? 32 : 64;
            }
            int newUidRules = (oldUidRules & 15) | newRule;
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                if (newRule != 0 && (newRule & 32) == 0 && (newRule & 64) == 0) {
                    Log.wtf(TAG, "Unexpected change of non-metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                }
                this.mHandler.obtainMessage(VERSION_INIT, uid, newUidRules).sendToTarget();
            }
        }
    }

    private void dispatchUidRulesChanged(INetworkPolicyListener listener, int uid, int uidRules) {
        if (listener != null) {
            try {
                listener.onUidRulesChanged(uid, uidRules);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchMeteredIfacesChanged(INetworkPolicyListener listener, String[] meteredIfaces) {
        if (listener != null) {
            try {
                listener.onMeteredIfacesChanged(meteredIfaces);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundChanged(INetworkPolicyListener listener, boolean restrictBackground) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundChanged(restrictBackground);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundWhitelistChanged(INetworkPolicyListener listener, int uid, boolean whitelisted) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundWhitelistChanged(uid, whitelisted);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundBlacklistChanged(INetworkPolicyListener listener, int uid, boolean blacklisted) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundBlacklistChanged(uid, blacklisted);
            } catch (RemoteException e) {
            }
        }
    }

    private void setInterfaceQuota(String iface, long quotaBytes) {
        try {
            this.mNetworkManager.setInterfaceQuota(iface, quotaBytes);
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                NetPluginDelegate.setQuota(iface, quotaBytes);
            }
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void removeInterfaceQuota(String iface) {
        try {
            this.mNetworkManager.removeInterfaceQuota(iface);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem removing interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkBlacklist(int uid, boolean enable) {
        try {
            this.mNetworkManager.setUidMeteredNetworkBlacklist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting blacklist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkWhitelist(int uid, boolean enable) {
        try {
            this.mNetworkManager.setUidMeteredNetworkWhitelist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting whitelist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRules(int chain, SparseIntArray uidRules) {
        try {
            int size = uidRules.size();
            int[] uids = new int[size];
            int[] rules = new int[size];
            for (int index = size - 1; index >= 0; index--) {
                uids[index] = uidRules.keyAt(index);
                rules[index] = uidRules.valueAt(index);
            }
            this.mNetworkManager.setFirewallUidRules(chain, uids, rules);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRule(int chain, int uid, int rule) {
        if (chain == VERSION_INIT) {
            this.mUidFirewallDozableRules.put(uid, rule);
        } else if (chain == VERSION_ADDED_SNOOZE) {
            this.mUidFirewallStandbyRules.put(uid, rule);
        } else if (chain == VERSION_ADDED_RESTRICT_BACKGROUND) {
            this.mUidFirewallPowerSaveRules.put(uid, rule);
        }
        try {
            this.mNetworkManager.setFirewallUidRule(chain, uid, rule);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void enableFirewallChainLocked(int chain, boolean enable) {
        if (this.mFirewallChainStates.indexOfKey(chain) < 0 || this.mFirewallChainStates.get(chain) != enable) {
            this.mFirewallChainStates.put(chain, enable);
            try {
                this.mNetworkManager.setFirewallChainEnabled(chain, enable);
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem enable firewall chain", e);
            } catch (RemoteException e2) {
            }
        }
    }

    private long getTotalBytes(NetworkTemplate template, long start, long end) {
        try {
            return this.mNetworkStats.getNetworkTotalBytes(template, start, end);
        } catch (RuntimeException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
            return 0;
        } catch (RemoteException e2) {
            return 0;
        }
    }

    private boolean isBandwidthControlEnabled() {
        boolean isBandwidthControlEnabled;
        long token = Binder.clearCallingIdentity();
        try {
            isBandwidthControlEnabled = this.mNetworkManager.isBandwidthControlEnabled();
            return isBandwidthControlEnabled;
        } catch (RemoteException e) {
            isBandwidthControlEnabled = LOGV;
            return isBandwidthControlEnabled;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void maybeRefreshTrustedTime() {
        if (getTimeRefreshElapsedRealtime() > TIME_CACHE_MAX_AGE) {
            this.mTimeRefreshRealtime = SystemClock.elapsedRealtime();
            new Thread() {
                public void run() {
                    NetworkPolicyManagerService.this.mTime.forceRefresh();
                }
            }.start();
        }
    }

    private long currentTimeMillis() {
        return this.mTime.hasCache() ? this.mTime.currentTimeMillis() : System.currentTimeMillis();
    }

    private static Intent buildAllowBackgroundDataIntent() {
        return new Intent(ACTION_ALLOW_BACKGROUND);
    }

    private static Intent buildNetworkOverLimitIntent(NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.net.NetworkOverLimitActivity"));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildViewDataUsageIntent(NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    public void addIdleHandler(IdleHandler handler) {
        this.mHandler.getLooper().getQueue().addIdleHandler(handler);
    }

    private static void collectKeys(SparseIntArray source, SparseBooleanArray target) {
        int size = source.size();
        for (int i = 0; i < size; i += VERSION_INIT) {
            target.put(source.keyAt(i), LOGD);
        }
    }

    public void factoryReset(String subscriber) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            int i;
            NetworkPolicy[] policies = getNetworkPolicies(this.mContext.getOpPackageName());
            NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(subscriber);
            int length = policies.length;
            for (i = 0; i < length; i += VERSION_INIT) {
                NetworkPolicy policy = policies[i];
                if (policy.template.equals(template)) {
                    policy.limitBytes = -1;
                    policy.inferred = LOGV;
                    policy.clearSnooze();
                }
            }
            setNetworkPolicies(policies);
            setRestrictBackground(LOGV);
            if (!this.mUserManager.hasUserRestriction("no_control_apps")) {
                int[] uidsWithPolicy = getUidsWithPolicy(VERSION_INIT);
                int length2 = uidsWithPolicy.length;
                for (i = 0; i < length2; i += VERSION_INIT) {
                    setUidPolicy(uidsWithPolicy[i], 0);
                }
            }
        }
    }

    public long getTimeRefreshElapsedRealtime() {
        if (this.mTimeRefreshRealtime != -1) {
            return SystemClock.elapsedRealtime() - this.mTimeRefreshRealtime;
        }
        return JobStatus.NO_LATEST_RUNTIME;
    }
}
