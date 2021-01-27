package com.android.server.net;

import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IUidObserver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkIdentity;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.NetworkState;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.StringNetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BestClock;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IDeviceIdleController;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DataUnit;
import android.util.IntArray;
import android.util.Log;
import android.util.Pair;
import android.util.Range;
import android.util.RecurrenceRule;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.StatLogger;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.NetworkManagementService;
import com.android.server.ServiceThread;
import com.android.server.SystemConfig;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class NetworkPolicyManagerService extends INetworkPolicyManager.Stub {
    private static final String ACTION_ALLOW_BACKGROUND = "com.android.server.net.action.ALLOW_BACKGROUND";
    private static final String ACTION_SNOOZE_RAPID = "com.android.server.net.action.SNOOZE_RAPID";
    private static final String ACTION_SNOOZE_WARNING = "com.android.server.net.action.SNOOZE_WARNING";
    private static final String ATTR_APP_ID = "appId";
    @Deprecated
    private static final String ATTR_CYCLE_DAY = "cycleDay";
    private static final String ATTR_CYCLE_END = "cycleEnd";
    private static final String ATTR_CYCLE_PERIOD = "cyclePeriod";
    private static final String ATTR_CYCLE_START = "cycleStart";
    @Deprecated
    private static final String ATTR_CYCLE_TIMEZONE = "cycleTimezone";
    private static final String ATTR_INFERRED = "inferred";
    private static final String ATTR_LAST_LIMIT_SNOOZE = "lastLimitSnooze";
    private static final String ATTR_LAST_SNOOZE = "lastSnooze";
    private static final String ATTR_LAST_WARNING_SNOOZE = "lastWarningSnooze";
    private static final String ATTR_LIMIT_BEHAVIOR = "limitBehavior";
    private static final String ATTR_LIMIT_BYTES = "limitBytes";
    private static final String ATTR_METERED = "metered";
    private static final String ATTR_NETWORK_ID = "networkId";
    private static final String ATTR_NETWORK_TEMPLATE = "networkTemplate";
    private static final String ATTR_OWNER_PACKAGE = "ownerPackage";
    private static final String ATTR_POLICY = "policy";
    private static final String ATTR_RESTRICT_BACKGROUND = "restrictBackground";
    private static final String ATTR_SUBSCRIBER_ID = "subscriberId";
    private static final String ATTR_SUB_ID = "subId";
    private static final String ATTR_SUMMARY = "summary";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_USAGE_BYTES = "usageBytes";
    private static final String ATTR_USAGE_TIME = "usageTime";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_WARNING_BYTES = "warningBytes";
    private static final int CHAIN_TOGGLE_DISABLE = 2;
    private static final int CHAIN_TOGGLE_ENABLE = 1;
    private static final int CHAIN_TOGGLE_NONE = 0;
    private static final int DOMESTIC_BETA_VERSION = 3;
    private static final boolean GOOGLE_WARNING_DISABLED = true;
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    private static final boolean LOGD;
    private static final boolean LOGV = NetworkPolicyLogger.LOGV;
    private static final int MSG_ADVISE_PERSIST_THRESHOLD = 7;
    private static final int MSG_LIMIT_REACHED = 5;
    private static final int MSG_METERED_IFACES_CHANGED = 2;
    private static final int MSG_METERED_RESTRICTED_PACKAGES_CHANGED = 17;
    private static final int MSG_POLICIES_CHANGED = 13;
    private static final int MSG_REMOVE_INTERFACE_QUOTA = 11;
    private static final int MSG_RESET_FIREWALL_RULES_BY_UID = 15;
    private static final int MSG_RESTRICT_BACKGROUND_CHANGED = 6;
    private static final int MSG_RULES_CHANGED = 1;
    private static final int MSG_SET_NETWORK_TEMPLATE_ENABLED = 18;
    private static final int MSG_SUBSCRIPTION_OVERRIDE = 16;
    private static final int MSG_UPDATE_INTERFACE_QUOTA = 10;
    public static final int OPPORTUNISTIC_QUOTA_UNKNOWN = -1;
    private static final String PROP_SUB_PLAN_OWNER = "persist.sys.sub_plan_owner";
    private static final float QUOTA_FRAC_JOBS_DEFAULT = 0.5f;
    private static final float QUOTA_FRAC_MULTIPATH_DEFAULT = 0.5f;
    private static final float QUOTA_LIMITED_DEFAULT = 0.1f;
    private static final long QUOTA_UNLIMITED_DEFAULT = DataUnit.MEBIBYTES.toBytes(20);
    static final String TAG = "NetworkPolicy";
    private static final String TAG_APP_POLICY = "app-policy";
    private static final String TAG_NETWORK_POLICY = "network-policy";
    private static final String TAG_POLICY_LIST = "policy-list";
    private static final String TAG_RESTRICT_BACKGROUND = "restrict-background";
    private static final String TAG_REVOKED_RESTRICT_BACKGROUND = "revoked-restrict-background";
    private static final String TAG_SUBSCRIPTION_PLAN = "subscription-plan";
    private static final String TAG_UID_POLICY = "uid-policy";
    private static final String TAG_WHITELIST = "whitelist";
    @VisibleForTesting
    public static final int TYPE_LIMIT = 35;
    @VisibleForTesting
    public static final int TYPE_LIMIT_SNOOZED = 36;
    @VisibleForTesting
    public static final int TYPE_RAPID = 45;
    private static final int TYPE_RESTRICT_BACKGROUND = 1;
    private static final int TYPE_RESTRICT_POWER = 2;
    @VisibleForTesting
    public static final int TYPE_WARNING = 34;
    private static final int UID_MSG_GONE = 101;
    private static final int UID_MSG_STATE_CHANGED = 100;
    private static final int VERSION_ADDED_CYCLE = 11;
    private static final int VERSION_ADDED_INFERRED = 7;
    private static final int VERSION_ADDED_METERED = 4;
    private static final int VERSION_ADDED_NETWORK_ID = 9;
    private static final int VERSION_ADDED_RESTRICT_BACKGROUND = 3;
    private static final int VERSION_ADDED_SNOOZE = 2;
    private static final int VERSION_ADDED_TIMEZONE = 6;
    private static final int VERSION_INIT = 1;
    private static final int VERSION_LATEST = 11;
    private static final int VERSION_SPLIT_SNOOZE = 5;
    private static final int VERSION_SWITCH_APP_ID = 8;
    private static final int VERSION_SWITCH_UID = 10;
    private static final long WAIT_FOR_ADMIN_DATA_TIMEOUT_MS = 10000;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private final ArraySet<NotificationId> mActiveNotifs;
    private final IActivityManager mActivityManager;
    private ActivityManagerInternal mActivityManagerInternal;
    private final CountDownLatch mAdminDataAvailableLatch;
    private final INetworkManagementEventObserver mAlertObserver;
    private final BroadcastReceiver mAllowReceiver;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseBooleanArray mAppIdleTempWhitelistAppIds;
    private final AppOpsManager mAppOps;
    private final CarrierConfigManager mCarrierConfigManager;
    private BroadcastReceiver mCarrierConfigReceiver;
    private final Clock mClock;
    private IConnectivityManager mConnManager;
    private BroadcastReceiver mConnReceiver;
    private final Context mContext;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseBooleanArray mDefaultRestrictBackgroundWhitelistUids;
    private IDeviceIdleController mDeviceIdleController;
    @GuardedBy({"mUidRulesFirstLock"})
    volatile boolean mDeviceIdleMode;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseBooleanArray mFirewallChainStates;
    final Handler mHandler;
    private final Handler.Callback mHandlerCallback;
    private IHwBehaviorCollectManager mHwBehaviorManager;
    private final IPackageManager mIPm;
    private final RemoteCallbackList<INetworkPolicyListener> mListeners;
    private boolean mLoadedRestrictBackground;
    private final NetworkPolicyLogger mLogger;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private String[] mMergedSubscriberIds;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private ArraySet<String> mMeteredIfaces;
    private ArraySet<String> mMeteredIfacesReplica;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseArray<Set<Integer>> mMeteredRestrictedUids;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private final SparseIntArray mNetIdToSubId;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final INetworkManagementService mNetworkManager;
    private volatile boolean mNetworkManagerReady;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private final SparseBooleanArray mNetworkMetered;
    final Object mNetworkPoliciesReplicaSecondLock;
    final Object mNetworkPoliciesSecondLock;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    final ArrayMap<NetworkTemplate, NetworkPolicy> mNetworkPolicy;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private final SparseBooleanArray mNetworkRoaming;
    private NetworkStatsManagerInternal mNetworkStats;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private final ArraySet<NetworkTemplate> mOverLimitNotified;
    private final BroadcastReceiver mPackageReceiver;
    @GuardedBy({"mUidRulesFirstLock", "mNetworkPoliciesSecondLock"})
    private final AtomicFile mPolicyFile;
    private PowerManagerInternal mPowerManagerInternal;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseBooleanArray mPowerSaveTempWhitelistAppIds;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseBooleanArray mPowerSaveWhitelistAppIds;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final BroadcastReceiver mPowerSaveWhitelistReceiver;
    @GuardedBy({"mUidRulesFirstLock"})
    volatile boolean mRestrictBackground;
    private boolean mRestrictBackgroundBeforeBsm;
    @GuardedBy({"mUidRulesFirstLock"})
    volatile boolean mRestrictBackgroundChangedInBsm;
    @GuardedBy({"mUidRulesFirstLock"})
    private PowerSaveState mRestrictBackgroundPowerState;
    @GuardedBy({"mUidRulesFirstLock"})
    private final SparseBooleanArray mRestrictBackgroundWhitelistRevokedUids;
    @GuardedBy({"mUidRulesFirstLock"})
    volatile boolean mRestrictPower;
    private final BroadcastReceiver mSnoozeReceiver;
    public final StatLogger mStatLogger;
    private final BroadcastReceiver mStatsReceiver;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private final SparseArray<String> mSubIdToSubscriberId;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    final SparseLongArray mSubscriptionOpportunisticQuota;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    final SparseArray<SubscriptionPlan[]> mSubscriptionPlans;
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    final SparseArray<String> mSubscriptionPlansOwner;
    private final boolean mSuppressDefaultPolicy;
    @GuardedBy({"mUidRulesFirstLock", "mNetworkPoliciesSecondLock"})
    volatile boolean mSystemReady;
    private long mTimeRefreshRealtime;
    @VisibleForTesting
    final Handler mUidEventHandler;
    private final Handler.Callback mUidEventHandlerCallback;
    private final ServiceThread mUidEventThread;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseIntArray mUidFirewallDozableRules;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseIntArray mUidFirewallPowerSaveRules;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseIntArray mUidFirewallStandbyRules;
    private final IUidObserver mUidObserver;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseIntArray mUidPolicy;
    private final BroadcastReceiver mUidRemovedReceiver;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseIntArray mUidRules;
    final Object mUidRulesFirstLock;
    @GuardedBy({"mUidRulesFirstLock"})
    final SparseIntArray mUidState;
    private UsageStatsManagerInternal mUsageStats;
    private final UserManager mUserManager;
    private final BroadcastReceiver mUserReceiver;
    private final BroadcastReceiver mWifiReceiver;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ChainToggleType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RestrictType {
    }

    interface Stats {
        public static final int COUNT = 2;
        public static final int IS_UID_NETWORKING_BLOCKED = 1;
        public static final int UPDATE_NETWORK_ENABLED = 0;
    }

    static {
        boolean z = false;
        if (NetworkPolicyLogger.LOGD || SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        }
        LOGD = z;
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkManagementService networkManagement) {
        this(context, activityManager, networkManagement, AppGlobals.getPackageManager(), getDefaultClock(), getDefaultSystemDir(), false);
    }

    private static File getDefaultSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    private static Clock getDefaultClock() {
        return new BestClock(ZoneOffset.UTC, new Clock[]{SystemClock.currentNetworkTimeClock(), Clock.systemUTC()});
    }

    private static boolean isNeedShowWarning() {
        return IS_DOCOMO && IS_TABLET;
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkManagementService networkManagement, IPackageManager pm, Clock clock, File systemDir, boolean suppressDefaultPolicy) {
        this.mUidRulesFirstLock = new Object();
        this.mNetworkPoliciesSecondLock = new Object();
        this.mNetworkPoliciesReplicaSecondLock = new Object();
        this.mAdminDataAvailableLatch = new CountDownLatch(1);
        this.mNetworkPolicy = new ArrayMap<>();
        this.mSubscriptionPlans = new SparseArray<>();
        this.mSubscriptionPlansOwner = new SparseArray<>();
        this.mSubscriptionOpportunisticQuota = new SparseLongArray();
        this.mUidPolicy = new SparseIntArray();
        this.mUidRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAppIds = new SparseBooleanArray();
        this.mPowerSaveTempWhitelistAppIds = new SparseBooleanArray();
        this.mAppIdleTempWhitelistAppIds = new SparseBooleanArray();
        this.mDefaultRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistRevokedUids = new SparseBooleanArray();
        this.mMeteredIfaces = new ArraySet<>();
        this.mMeteredIfacesReplica = new ArraySet<>();
        this.mOverLimitNotified = new ArraySet<>();
        this.mActiveNotifs = new ArraySet<>();
        this.mUidState = new SparseIntArray();
        this.mNetworkMetered = new SparseBooleanArray();
        this.mNetworkRoaming = new SparseBooleanArray();
        this.mNetIdToSubId = new SparseIntArray();
        this.mSubIdToSubscriberId = new SparseArray<>();
        this.mMergedSubscriberIds = EmptyArray.STRING;
        this.mMeteredRestrictedUids = new SparseArray<>();
        this.mListeners = new RemoteCallbackList<>();
        this.mLogger = new NetworkPolicyLogger();
        this.mStatLogger = new StatLogger(new String[]{"updateNetworkEnabledNL()", "isUidNetworkingBlocked()"});
        this.mUidObserver = new IUidObserver.Stub() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass4 */

            public void onUidStateChanged(int uid, int procState, long procStateSeq) {
                NetworkPolicyManagerService.this.mUidEventHandler.obtainMessage(100, uid, procState, Long.valueOf(procStateSeq)).sendToTarget();
            }

            public void onUidGone(int uid, boolean disabled) {
                NetworkPolicyManagerService.this.mUidEventHandler.obtainMessage(101, uid, 0).sendToTarget();
            }

            public void onUidActive(int uid) {
            }

            public void onUidIdle(int uid, boolean disabled) {
            }

            public void onUidCachedChanged(int uid, boolean cached) {
            }
        };
        this.mPowerSaveWhitelistReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveWhitelistUL();
                    NetworkPolicyManagerService.this.updateRulesForRestrictPowerUL();
                    NetworkPolicyManagerService.this.updateRulesForAppIdleUL();
                }
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1 && "android.intent.action.PACKAGE_ADDED".equals(action)) {
                    if (NetworkPolicyManagerService.LOGV) {
                        Slog.v(NetworkPolicyManagerService.TAG, "ACTION_PACKAGE_ADDED for uid=" + uid);
                    }
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidUL(uid);
                    }
                }
            }
        };
        this.mUidRemovedReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass7 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1) {
                    if (NetworkPolicyManagerService.LOGV) {
                        Slog.v(NetworkPolicyManagerService.TAG, "ACTION_UID_REMOVED for uid=" + uid);
                    }
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        NetworkPolicyManagerService.this.onUidDeletedUL(uid);
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            NetworkPolicyManagerService.this.writePolicyAL();
                        }
                    }
                }
            }
        };
        this.mUserReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass8 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                char c = 65535;
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId != -1) {
                    int hashCode = action.hashCode();
                    if (hashCode != -2061058799) {
                        if (hashCode == 1121780209 && action.equals("android.intent.action.USER_ADDED")) {
                            c = 1;
                        }
                    } else if (action.equals("android.intent.action.USER_REMOVED")) {
                        c = 0;
                    }
                    if (c == 0 || c == 1) {
                        synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                            NetworkPolicyManagerService.this.removeUserStateUL(userId, true);
                            NetworkPolicyManagerService.this.mMeteredRestrictedUids.remove(userId);
                            if (action == "android.intent.action.USER_ADDED") {
                                NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId);
                            }
                            if (NetworkPolicyManagerService.LOGD) {
                                Slog.d(NetworkPolicyManagerService.TAG, "mUserReceiver onReceive intent: " + intent.getAction());
                            }
                            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                NetworkPolicyManagerService.this.updateRulesForGlobalChangeAL(true);
                            }
                        }
                    }
                }
            }
        };
        this.mStatsReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass9 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                    NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                    NetworkPolicyManagerService.this.updateNotificationsNL();
                }
            }
        };
        this.mAllowReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass10 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.setRestrictBackground(false);
            }
        };
        this.mSnoozeReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass11 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (NetworkPolicyManagerService.LOGD) {
                    Slog.d(NetworkPolicyManagerService.TAG, "mSnoozeReceiver onReceive intent: " + intent.getAction());
                }
                NetworkTemplate template = intent.getParcelableExtra("android.net.NETWORK_TEMPLATE");
                if (NetworkPolicyManagerService.ACTION_SNOOZE_WARNING.equals(intent.getAction())) {
                    NetworkPolicyManagerService.this.performSnooze(template, 34);
                } else if (NetworkPolicyManagerService.ACTION_SNOOZE_RAPID.equals(intent.getAction())) {
                    NetworkPolicyManagerService.this.performSnooze(template, 45);
                }
            }
        };
        this.mWifiReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass12 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.upgradeWifiMeteredOverrideAL();
                    }
                }
                NetworkPolicyManagerService.this.mContext.unregisterReceiver(this);
            }
        };
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass13 */

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                if (network != null && networkCapabilities != null) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        boolean newRoaming = true;
                        boolean newMetered = !networkCapabilities.hasCapability(11);
                        boolean meteredChanged = NetworkPolicyManagerService.updateCapabilityChange(NetworkPolicyManagerService.this.mNetworkMetered, newMetered, network);
                        if (networkCapabilities.hasCapability(18)) {
                            newRoaming = false;
                        }
                        boolean roamingChanged = NetworkPolicyManagerService.updateCapabilityChange(NetworkPolicyManagerService.this.mNetworkRoaming, newRoaming, network);
                        if (meteredChanged || roamingChanged) {
                            if (NetworkPolicyManagerService.LOGD) {
                                Slog.d(NetworkPolicyManagerService.TAG, "onCapabilitiesChanged, meteredChanged: " + meteredChanged + ", roamingChanged: " + roamingChanged);
                            }
                            NetworkPolicyManagerService.this.mLogger.meterednessChanged(network.netId, newMetered);
                            NetworkPolicyManagerService.this.updateNetworkRulesNL();
                            NetworkPolicyManagerService.this.syncMeteredIfacesToReplicaNL();
                        }
                    }
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass14 */

            public void limitReached(String limitName, String iface) {
                NetworkPolicyManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkPolicyManagerService.TAG);
                if (!NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(5, iface).sendToTarget();
                }
            }
        };
        this.mConnReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass15 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (NetworkPolicyManagerService.LOGD) {
                    Slog.d(NetworkPolicyManagerService.TAG, "mConnReceiver onReceive intent: " + intent.getAction());
                }
                NetworkPolicyManagerService.this.updateNetworksInternal();
            }
        };
        this.mCarrierConfigReceiver = new BroadcastReceiver() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass16 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("subscription")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    NetworkPolicyManagerService.this.updateSubscriptions();
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            String subscriberId = (String) NetworkPolicyManagerService.this.mSubIdToSubscriberId.get(subId, null);
                            if (subscriberId != null) {
                                NetworkPolicyManagerService.this.ensureActiveMobilePolicyAL(subId, subscriberId);
                                NetworkPolicyManagerService.this.maybeUpdateMobilePolicyCycleAL(subId, subscriberId);
                            } else {
                                Slog.wtf(NetworkPolicyManagerService.TAG, "Missing subscriberId for subId " + subId);
                            }
                            if (NetworkPolicyManagerService.LOGD) {
                                Slog.d(NetworkPolicyManagerService.TAG, "mCarrierConfigReceiver onReceive intent: " + intent.getAction());
                            }
                            NetworkPolicyManagerService.this.handleNetworkPoliciesUpdateAL(true);
                        }
                    }
                }
            }
        };
        this.mHandlerCallback = new Handler.Callback() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass17 */

            /* JADX INFO: Multiple debug info for r0v1 int: [D('uid' int), D('meteredIfaces' java.lang.String[])] */
            /* JADX INFO: Multiple debug info for r0v16 int: [D('userId' int), D('overrideMask' int)] */
            /* JADX INFO: Multiple debug info for r0v17 int: [D('template' android.net.NetworkTemplate), D('userId' int)] */
            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                boolean enabled = false;
                switch (msg.what) {
                    case 1:
                        int uid = msg.arg1;
                        int uidRules = msg.arg2;
                        int length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (int i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchUidRulesChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, uidRules);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 2:
                        String[] meteredIfaces = (String[]) msg.obj;
                        int length2 = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (int i2 = 0; i2 < length2; i2++) {
                            NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i2), meteredIfaces);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 3:
                    case 4:
                    case 8:
                    case 9:
                    case 12:
                    case 14:
                    default:
                        return false;
                    case 5:
                        String iface = (String) msg.obj;
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            if (NetworkPolicyManagerService.this.mMeteredIfaces.contains(iface)) {
                                NetworkPolicyManagerService.this.mNetworkStats.forceUpdate();
                                NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                                NetworkPolicyManagerService.this.updateNotificationsNL();
                            }
                        }
                        return true;
                    case 6:
                        if (msg.arg1 != 0) {
                            enabled = true;
                        }
                        int length3 = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (int i3 = 0; i3 < length3; i3++) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i3), enabled);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        Intent intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                        intent.setFlags(1073741824);
                        NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                        return true;
                    case 7:
                        NetworkPolicyManagerService.this.mNetworkStats.advisePersistThreshold(((Long) msg.obj).longValue() / 1000);
                        return true;
                    case 10:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        NetworkPolicyManagerService.this.setInterfaceQuota((String) msg.obj, (((long) msg.arg1) << 32) | (((long) msg.arg2) & 4294967295L));
                        return true;
                    case 11:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        return true;
                    case 13:
                        int uid2 = msg.arg1;
                        int policy = msg.arg2;
                        Boolean notifyApp = (Boolean) msg.obj;
                        int length4 = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (int i4 = 0; i4 < length4; i4++) {
                            NetworkPolicyManagerService.this.dispatchUidPoliciesChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i4), uid2, policy);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        if (notifyApp.booleanValue()) {
                            NetworkPolicyManagerService.this.broadcastRestrictBackgroundChanged(uid2, notifyApp);
                        }
                        return true;
                    case 15:
                        NetworkPolicyManagerService.this.resetUidFirewallRules(msg.arg1);
                        return true;
                    case 16:
                        int overrideMask = msg.arg1;
                        int overrideValue = msg.arg2;
                        int subId = ((Integer) msg.obj).intValue();
                        int length5 = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (int i5 = 0; i5 < length5; i5++) {
                            NetworkPolicyManagerService.this.dispatchSubscriptionOverride(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i5), subId, overrideMask, overrideValue);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 17:
                        NetworkPolicyManagerService.this.setMeteredRestrictedPackagesInternal((Set) msg.obj, msg.arg1);
                        return true;
                    case 18:
                        NetworkTemplate template = (NetworkTemplate) msg.obj;
                        if (msg.arg1 != 0) {
                            enabled = true;
                        }
                        NetworkPolicyManagerService.this.setNetworkTemplateEnabledInner(template, enabled);
                        return true;
                }
            }
        };
        this.mUidEventHandlerCallback = new Handler.Callback() {
            /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass18 */

            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                int i = msg.what;
                if (i == 100) {
                    NetworkPolicyManagerService.this.handleUidChanged(msg.arg1, msg.arg2, ((Long) msg.obj).longValue());
                    return true;
                } else if (i != 101) {
                    return false;
                } else {
                    NetworkPolicyManagerService.this.handleUidGone(msg.arg1);
                    return true;
                }
            }
        };
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing context");
        this.mActivityManager = (IActivityManager) Preconditions.checkNotNull(activityManager, "missing activityManager");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManagement, "missing networkManagement");
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mClock = (Clock) Preconditions.checkNotNull(clock, "missing Clock");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService(CarrierConfigManager.class);
        this.mIPm = pm;
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this.mHandlerCallback);
        this.mUidEventThread = new ServiceThread("NetworkPolicy.uid", -2, false);
        this.mUidEventThread.start();
        this.mUidEventHandler = new Handler(this.mUidEventThread.getLooper(), this.mUidEventHandlerCallback);
        this.mSuppressDefaultPolicy = suppressDefaultPolicy;
        this.mPolicyFile = new AtomicFile(new File(systemDir, "netpolicy.xml"), "net-policy");
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        LocalServices.addService(NetworkPolicyManagerInternal.class, new NetworkPolicyManagerInternalImpl());
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updatePowerSaveWhitelistUL() {
        try {
            int[] whitelist = this.mDeviceIdleController.getAppIdWhitelistExceptIdle();
            this.mPowerSaveWhitelistExceptIdleAppIds.clear();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveWhitelistExceptIdleAppIds.put(uid, true);
                }
            }
            int[] whitelist2 = this.mDeviceIdleController.getAppIdWhitelist();
            this.mPowerSaveWhitelistAppIds.clear();
            if (whitelist2 != null) {
                for (int uid2 : whitelist2) {
                    this.mPowerSaveWhitelistAppIds.put(uid2, true);
                }
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public boolean addDefaultRestrictBackgroundWhitelistUidsUL() {
        List<UserInfo> users = this.mUserManager.getUsers();
        int numberUsers = users.size();
        boolean changed = false;
        for (int i = 0; i < numberUsers; i++) {
            changed = addDefaultRestrictBackgroundWhitelistUidsUL(users.get(i).id) || changed;
        }
        return changed;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock"})
    private boolean addDefaultRestrictBackgroundWhitelistUidsUL(int userId) {
        SystemConfig sysConfig = SystemConfig.getInstance();
        PackageManager pm = this.mContext.getPackageManager();
        ArraySet<String> allowDataUsage = sysConfig.getAllowInDataUsageSave();
        boolean changed = false;
        for (int i = 0; i < allowDataUsage.size(); i++) {
            String pkg = allowDataUsage.valueAt(i);
            if (LOGD) {
                Slog.d(TAG, "checking restricted background whitelisting for package " + pkg + " and user " + userId);
            }
            try {
                ApplicationInfo app = pm.getApplicationInfoAsUser(pkg, DumpState.DUMP_DEXOPT, userId);
                if (!app.isPrivilegedApp()) {
                    Slog.e(TAG, "addDefaultRestrictBackgroundWhitelistUidsUL(): skipping non-privileged app  " + pkg);
                } else {
                    int uid = UserHandle.getUid(userId, app.uid);
                    this.mDefaultRestrictBackgroundWhitelistUids.append(uid, true);
                    if (LOGD) {
                        Slog.d(TAG, "Adding uid " + uid + " (user " + userId + ") to default restricted background whitelist. Revoked status: " + this.mRestrictBackgroundWhitelistRevokedUids.get(uid));
                    }
                    if (!this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                        if (LOGD) {
                            Slog.d(TAG, "adding default package " + pkg + " (uid " + uid + " for user " + userId + ") to restrict background whitelist");
                        }
                        setUidPolicyUncheckedUL(uid, 4, false);
                        changed = true;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                if (LOGD) {
                    Slog.d(TAG, "No ApplicationInfo for package " + pkg);
                }
            }
        }
        return changed;
    }

    /* access modifiers changed from: private */
    /* renamed from: initService */
    public void lambda$networkScoreAndNetworkManagementServiceReady$0$NetworkPolicyManagerService(CountDownLatch initCompleteSignal) {
        Trace.traceBegin(2097152, "systemReady");
        int oldPriority = Process.getThreadPriority(Process.myTid());
        try {
            Process.setThreadPriority(-2);
            if (!isBandwidthControlEnabled()) {
                Slog.w(TAG, "bandwidth controls disabled, unable to enforce policy");
                return;
            }
            this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
            this.mNetworkStats = (NetworkStatsManagerInternal) LocalServices.getService(NetworkStatsManagerInternal.class);
            synchronized (this.mUidRulesFirstLock) {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    updatePowerSaveWhitelistUL();
                    this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                    this.mPowerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                        /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass1 */

                        public int getServiceType() {
                            return 6;
                        }

                        public void onLowPowerModeChanged(PowerSaveState result) {
                            boolean enabled = result.batterySaverEnabled;
                            if (NetworkPolicyManagerService.LOGD) {
                                Slog.d(NetworkPolicyManagerService.TAG, "onLowPowerModeChanged(" + enabled + ")");
                            }
                            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                                if (NetworkPolicyManagerService.this.mRestrictPower != enabled) {
                                    NetworkPolicyManagerService.this.mRestrictPower = enabled;
                                    NetworkPolicyManagerService.this.updateRulesForRestrictPowerUL();
                                }
                            }
                        }
                    });
                    this.mRestrictPower = this.mPowerManagerInternal.getLowPowerState(6).batterySaverEnabled;
                    this.mSystemReady = true;
                    waitForAdminData();
                    readPolicyAL();
                    this.mRestrictBackgroundBeforeBsm = this.mLoadedRestrictBackground;
                    this.mRestrictBackgroundPowerState = this.mPowerManagerInternal.getLowPowerState(10);
                    if (this.mRestrictBackgroundPowerState.batterySaverEnabled && !this.mLoadedRestrictBackground) {
                        this.mLoadedRestrictBackground = true;
                    }
                    this.mPowerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                        /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass2 */

                        public int getServiceType() {
                            return 10;
                        }

                        public void onLowPowerModeChanged(PowerSaveState result) {
                            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                                NetworkPolicyManagerService.this.updateRestrictBackgroundByLowPowerModeUL(result);
                            }
                        }
                    });
                    if (addDefaultRestrictBackgroundWhitelistUidsUL()) {
                        writePolicyAL();
                    }
                    if (LOGD) {
                        Slog.d(TAG, "initService");
                    }
                    setRestrictBackgroundUL(this.mLoadedRestrictBackground);
                    updateRulesForGlobalChangeAL(false);
                    updateNotificationsNL();
                }
            }
            this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
            try {
                this.mActivityManager.registerUidObserver(this.mUidObserver, 3, 6, PackageManagerService.PLATFORM_PACKAGE_NAME);
                this.mNetworkManager.registerObserver(this.mAlertObserver);
            } catch (RemoteException e) {
            }
            this.mContext.registerReceiver(this.mPowerSaveWhitelistReceiver, new IntentFilter("android.os.action.POWER_SAVE_WHITELIST_CHANGED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mConnReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"), "android.permission.CONNECTIVITY_INTERNAL", this.mHandler);
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addDataScheme("package");
            this.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mHandler);
            this.mContext.registerReceiver(this.mUidRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction("android.intent.action.USER_ADDED");
            userFilter.addAction("android.intent.action.USER_REMOVED");
            this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
            this.mContext.registerReceiver(this.mStatsReceiver, new IntentFilter(NetworkStatsService.ACTION_NETWORK_STATS_UPDATED), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
            this.mContext.registerReceiver(this.mAllowReceiver, new IntentFilter(ACTION_ALLOW_BACKGROUND), "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
            this.mContext.registerReceiver(this.mSnoozeReceiver, new IntentFilter(ACTION_SNOOZE_WARNING), "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
            this.mContext.registerReceiver(this.mSnoozeReceiver, new IntentFilter(ACTION_SNOOZE_RAPID), "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
            this.mContext.registerReceiver(this.mWifiReceiver, new IntentFilter("android.net.wifi.CONFIGURED_NETWORKS_CHANGE"), null, this.mHandler);
            this.mContext.registerReceiver(this.mCarrierConfigReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"), null, this.mHandler);
            ((ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class)).registerNetworkCallback(new NetworkRequest.Builder().build(), this.mNetworkCallback);
            this.mUsageStats.addAppIdleStateChangeListener(new AppIdleStateChangeListener());
            ((SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class)).addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener(this.mHandler.getLooper()) {
                /* class com.android.server.net.NetworkPolicyManagerService.AnonymousClass3 */

                @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
                public void onSubscriptionsChanged() {
                    if (NetworkPolicyManagerService.LOGD) {
                        Slog.d(NetworkPolicyManagerService.TAG, "onSubscriptionsChanged");
                    }
                    NetworkPolicyManagerService.this.updateNetworksInternal();
                }
            });
            initCompleteSignal.countDown();
            Process.setThreadPriority(oldPriority);
            Trace.traceEnd(2097152);
        } finally {
            Process.setThreadPriority(oldPriority);
            Trace.traceEnd(2097152);
        }
    }

    public CountDownLatch networkScoreAndNetworkManagementServiceReady() {
        this.mNetworkManagerReady = true;
        CountDownLatch initCompleteSignal = new CountDownLatch(1);
        this.mHandler.post(new Runnable(initCompleteSignal) {
            /* class com.android.server.net.$$Lambda$NetworkPolicyManagerService$HDTUqowtgLW_V0Kq6psXLWC9ws */
            private final /* synthetic */ CountDownLatch f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                NetworkPolicyManagerService.this.lambda$networkScoreAndNetworkManagementServiceReady$0$NetworkPolicyManagerService(this.f$1);
            }
        });
        return initCompleteSignal;
    }

    public void systemReady(CountDownLatch initCompleteSignal) {
        try {
            if (!initCompleteSignal.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Service NetworkPolicy init timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Service NetworkPolicy init interrupted", e);
        }
    }

    /* access modifiers changed from: private */
    public static boolean updateCapabilityChange(SparseBooleanArray lastValues, boolean newValue, Network network) {
        boolean changed = false;
        if (lastValues.get(network.netId, false) != newValue || lastValues.indexOfKey(network.netId) < 0) {
            changed = true;
        }
        if (changed) {
            lastValues.put(network.netId, newValue);
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    public void updateNotificationsNL() {
        boolean z;
        ArraySet<NotificationId> beforeNotifs;
        int i;
        long totalBytes;
        NetworkPolicyManagerService networkPolicyManagerService = this;
        if (LOGV) {
            Slog.v(TAG, "updateNotificationsNL()");
        }
        Trace.traceBegin(2097152, "updateNotificationsNL");
        ArraySet<NotificationId> beforeNotifs2 = new ArraySet<>(networkPolicyManagerService.mActiveNotifs);
        networkPolicyManagerService.mActiveNotifs.clear();
        long now = networkPolicyManagerService.mClock.millis();
        boolean z2 = true;
        int i2 = networkPolicyManagerService.mNetworkPolicy.size() - 1;
        while (i2 >= 0) {
            NetworkPolicy policy = networkPolicyManagerService.mNetworkPolicy.valueAt(i2);
            if (policy.limitBytes != -1 || isNeedShowWarning()) {
                int subId = networkPolicyManagerService.findRelevantSubIdNL(policy.template);
                if (subId == -1) {
                    i = i2;
                    beforeNotifs = beforeNotifs2;
                    z = z2;
                } else if (!policy.hasCycle()) {
                    i = i2;
                    beforeNotifs = beforeNotifs2;
                    z = z2;
                } else {
                    if (LOGD) {
                        Slog.d(TAG, "Should check whether to notify user about data usage.");
                    }
                    Pair<ZonedDateTime, ZonedDateTime> cycle = (Pair) NetworkPolicyManager.cycleIterator(policy).next();
                    long cycleStart = ((ZonedDateTime) cycle.first).toInstant().toEpochMilli();
                    long cycleEnd = ((ZonedDateTime) cycle.second).toInstant().toEpochMilli();
                    long totalBytes2 = getTotalBytes(policy.template, cycleStart, cycleEnd);
                    PersistableBundle config = networkPolicyManagerService.mCarrierConfigManager.getConfigForSubId(subId);
                    if (CarrierConfigManager.isConfigForIdentifiedCarrier(config)) {
                        boolean notifyWarning = getBooleanDefeatingNullable(config, "data_warning_notification_bool", z2);
                        boolean notifyLimit = getBooleanDefeatingNullable(config, "data_limit_notification_bool", z2);
                        boolean notifyRapid = getBooleanDefeatingNullable(config, "data_rapid_notification_bool", z2);
                        boolean snoozedRecently = false;
                        if (!notifyWarning) {
                            totalBytes = totalBytes2;
                        } else if (!policy.isOverWarning(totalBytes2) || policy.isOverLimit(totalBytes2)) {
                            totalBytes = totalBytes2;
                        } else if (!(policy.lastWarningSnooze >= cycleStart ? z2 : false)) {
                            totalBytes = totalBytes2;
                            enqueueNotification(policy, 34, totalBytes2, null);
                        } else {
                            totalBytes = totalBytes2;
                        }
                        if (notifyLimit) {
                            if (!policy.isOverLimit(totalBytes)) {
                                networkPolicyManagerService.notifyUnderLimitNL(policy.template);
                            } else if (policy.lastLimitSnooze >= cycleStart ? z2 : false) {
                                enqueueNotification(policy, 36, totalBytes, null);
                            } else {
                                enqueueNotification(policy, 35, totalBytes, null);
                                networkPolicyManagerService.notifyOverLimitNL(policy.template);
                            }
                        }
                        if (!notifyRapid || policy.limitBytes == -1) {
                            i = i2;
                            beforeNotifs = beforeNotifs2;
                            z = z2;
                        } else {
                            long recentDuration = TimeUnit.DAYS.toMillis(4);
                            long recentStart = now - recentDuration;
                            long recentBytes = getTotalBytes(policy.template, recentStart, now);
                            long projectedBytes = (recentBytes * (cycleEnd - cycleStart)) / recentDuration;
                            long alertBytes = (policy.limitBytes * 3) / 2;
                            if (LOGD) {
                                Slog.d(TAG, "Rapid usage considering recent " + recentBytes + " projected " + projectedBytes + " alert " + alertBytes);
                            }
                            if (policy.lastRapidSnooze >= now - 86400000) {
                                snoozedRecently = true;
                            }
                            if (projectedBytes <= alertBytes || snoozedRecently) {
                                i = i2;
                                z = true;
                                beforeNotifs = beforeNotifs2;
                            } else {
                                i = i2;
                                z = true;
                                beforeNotifs = beforeNotifs2;
                                enqueueNotification(policy, 45, 0, findRapidBlame(policy.template, recentStart, now));
                            }
                        }
                    } else if (LOGV) {
                        Slog.v(TAG, "isConfigForIdentifiedCarrier returned false");
                        return;
                    } else {
                        return;
                    }
                }
            } else {
                networkPolicyManagerService.notifyUnderLimitNL(policy.template);
                i = i2;
                beforeNotifs = beforeNotifs2;
                z = z2;
            }
            i2 = i - 1;
            beforeNotifs2 = beforeNotifs;
            z2 = z;
            networkPolicyManagerService = this;
        }
        for (int i3 = beforeNotifs2.size() - 1; i3 >= 0; i3--) {
            NotificationId notificationId = beforeNotifs2.valueAt(i3);
            if (!this.mActiveNotifs.contains(notificationId)) {
                cancelNotification(notificationId);
            }
        }
        Trace.traceEnd(2097152);
    }

    private ApplicationInfo findRapidBlame(NetworkTemplate template, long start, long end) {
        String[] packageNames;
        long totalBytes = 0;
        long maxBytes = 0;
        NetworkStats stats = getNetworkUidBytes(template, start, end);
        NetworkStats.Entry entry = null;
        int maxUid = 0;
        for (int i = 0; i < stats.size(); i++) {
            entry = stats.getValues(i, entry);
            long bytes = entry.rxBytes + entry.txBytes;
            totalBytes += bytes;
            if (bytes > maxBytes) {
                maxBytes = bytes;
                maxUid = entry.uid;
            }
        }
        if (maxBytes <= 0 || maxBytes <= totalBytes / 2 || (packageNames = this.mContext.getPackageManager().getPackagesForUid(maxUid)) == null || packageNames.length != 1) {
            return null;
        }
        try {
            return this.mContext.getPackageManager().getApplicationInfo(packageNames[0], 4989440);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private int findRelevantSubIdNL(NetworkTemplate template) {
        for (int i = 0; i < this.mSubIdToSubscriberId.size(); i++) {
            int subId = this.mSubIdToSubscriberId.keyAt(i);
            if (template.matches(new NetworkIdentity(0, 0, this.mSubIdToSubscriberId.valueAt(i), (String) null, false, true, true))) {
                return subId;
            }
        }
        return -1;
    }

    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private void notifyOverLimitNL(NetworkTemplate template) {
        if (!this.mOverLimitNotified.contains(template)) {
            Context context = this.mContext;
            context.startActivity(buildNetworkOverLimitIntent(context.getResources(), template));
            this.mOverLimitNotified.add(template);
        }
    }

    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private void notifyUnderLimitNL(NetworkTemplate template) {
        this.mOverLimitNotified.remove(template);
    }

    private void enqueueNotification(NetworkPolicy policy, int type, long totalBytes, ApplicationInfo rapidBlame) {
        CharSequence title;
        CharSequence body;
        CharSequence body2;
        NotificationId notificationId = new NotificationId(policy, type);
        Notification.Builder builder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_ALERTS);
        builder.setOnlyAlertOnce(true);
        builder.setWhen(0);
        builder.setColor(this.mContext.getColor(17170460));
        Resources res = this.mContext.getResources();
        if (type != 45) {
            switch (type) {
                case 34:
                    if (isNeedShowWarning()) {
                        title = res.getText(17039928);
                        body = res.getString(17039927, Formatter.formatFileSize(this.mContext, totalBytes, 8));
                        builder.setSmallIcon(17301624);
                        Bitmap bmp1 = BitmapFactory.decodeResource(res, 33751681);
                        if (bmp1 != null) {
                            builder.setLargeIcon(bmp1);
                        }
                        if (isNeedShowWarning()) {
                            builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, buildSnoozeWarningIntent(policy.template), DumpState.DUMP_HWFEATURES));
                        }
                        Intent viewIntent = buildViewDataUsageIntent(res, policy.template);
                        if (isHeadlessSystemUserBuild()) {
                            builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, viewIntent, DumpState.DUMP_HWFEATURES, null, UserHandle.CURRENT));
                        } else {
                            builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, viewIntent, DumpState.DUMP_HWFEATURES));
                        }
                        if (!isNeedShowWarning()) {
                            return;
                        }
                    } else if (LOGV) {
                        Slog.v(TAG, "google warning disabled ,don't show notification");
                        return;
                    } else {
                        return;
                    }
                    break;
                case 35:
                    int matchRule = policy.template.getMatchRule();
                    if (matchRule == 1) {
                        title = res.getText(17039921);
                    } else if (matchRule == 4) {
                        title = res.getText(17039930);
                    } else {
                        return;
                    }
                    body = res.getText(17039918);
                    builder.setOngoing(true);
                    Bitmap bmp2 = BitmapFactory.decodeResource(res, 33751679);
                    if (bmp2 != null) {
                        builder.setLargeIcon(bmp2);
                    }
                    builder.setSmallIcon(17303534);
                    Intent intent = buildNetworkOverLimitIntent(res, policy.template);
                    if (!isHeadlessSystemUserBuild()) {
                        builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, DumpState.DUMP_HWFEATURES));
                        break;
                    } else {
                        builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, DumpState.DUMP_HWFEATURES, null, UserHandle.CURRENT));
                        break;
                    }
                case 36:
                    int matchRule2 = policy.template.getMatchRule();
                    if (matchRule2 == 1) {
                        title = res.getText(17039920);
                    } else if (matchRule2 == 4) {
                        title = res.getText(17039929);
                    } else {
                        return;
                    }
                    CharSequence body3 = res.getString(17039919, Formatter.formatFileSize(this.mContext, totalBytes - policy.limitBytes, 8));
                    builder.setOngoing(true);
                    builder.setSmallIcon(17301624);
                    Bitmap bmp3 = BitmapFactory.decodeResource(res, 33751681);
                    if (bmp3 != null) {
                        builder.setLargeIcon(bmp3);
                    }
                    builder.setChannelId(SystemNotificationChannels.NETWORK_STATUS);
                    Intent intent2 = buildViewDataUsageIntent(res, policy.template);
                    if (isHeadlessSystemUserBuild()) {
                        builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent2, DumpState.DUMP_HWFEATURES, null, UserHandle.CURRENT));
                        body2 = body3;
                    } else {
                        body2 = body3;
                        builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent2, DumpState.DUMP_HWFEATURES));
                    }
                    if (isNeedShowWarning()) {
                        body = body2;
                        break;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } else {
            title = res.getText(17039924);
            if (rapidBlame != null) {
                body = res.getString(17039922, rapidBlame.loadLabel(this.mContext.getPackageManager()));
            } else {
                body = res.getString(17039923);
            }
            builder.setSmallIcon(17301624);
            builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, buildSnoozeRapidIntent(policy.template), DumpState.DUMP_HWFEATURES));
            Intent viewIntent2 = buildViewDataUsageIntent(res, policy.template);
            if (isHeadlessSystemUserBuild()) {
                builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, viewIntent2, DumpState.DUMP_HWFEATURES, null, UserHandle.CURRENT));
            } else {
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, viewIntent2, DumpState.DUMP_HWFEATURES));
            }
        }
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setStyle(new Notification.BigTextStyle().bigText(body));
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).notifyAsUser(notificationId.getTag(), notificationId.getId(), builder.build(), UserHandle.ALL);
        this.mActiveNotifs.add(notificationId);
    }

    private void cancelNotification(NotificationId notificationId) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).cancel(notificationId.getTag(), notificationId.getId());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworksInternal() {
        updateSubscriptions();
        if (LOGD) {
            Slog.d(TAG, "updateNetworksInternal");
        }
        synchronized (this.mUidRulesFirstLock) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                ensureActiveMobilePolicyAL();
                normalizePoliciesNL();
                updateNetworkEnabledNL();
                updateNetworkRulesNL();
                updateNotificationsNL();
                syncMeteredIfacesToReplicaNL();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateNetworks() throws InterruptedException {
        if (LOGD) {
            Slog.d(TAG, "updateNetworks");
        }
        updateNetworksInternal();
        CountDownLatch latch = new CountDownLatch(1);
        this.mHandler.post(new Runnable(latch) {
            /* class com.android.server.net.$$Lambda$NetworkPolicyManagerService$lv2qqWetKVoJzbe7z3LT5idTu54 */
            private final /* synthetic */ CountDownLatch f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private boolean maybeUpdateMobilePolicyCycleAL(int subId, String subscriberId) {
        if (LOGV) {
            Slog.v(TAG, "maybeUpdateMobilePolicyCycleAL()");
        }
        boolean policyUpdated = false;
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, (String) null, false, true, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            if (this.mNetworkPolicy.keyAt(i).matches(probeIdent)) {
                policyUpdated |= updateDefaultMobilePolicyAL(subId, this.mNetworkPolicy.valueAt(i));
            }
        }
        return policyUpdated;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getCycleDayFromCarrierConfig(PersistableBundle config, int fallbackCycleDay) {
        int cycleDay;
        if (config == null || (cycleDay = config.getInt("monthly_data_cycle_day_int")) == -1) {
            return fallbackCycleDay;
        }
        Calendar cal = Calendar.getInstance();
        if (cycleDay >= cal.getMinimum(5) && cycleDay <= cal.getMaximum(5)) {
            return cycleDay;
        }
        Slog.e(TAG, "Invalid date in CarrierConfigManager.KEY_MONTHLY_DATA_CYCLE_DAY_INT: " + cycleDay);
        return fallbackCycleDay;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getWarningBytesFromCarrierConfig(PersistableBundle config, long fallbackWarningBytes) {
        if (config == null) {
            return fallbackWarningBytes;
        }
        long warningBytes = config.getLong("data_warning_threshold_bytes_long");
        if (warningBytes == -2) {
            return -1;
        }
        if (warningBytes == -1) {
            return getPlatformDefaultWarningBytes();
        }
        if (warningBytes >= 0) {
            return warningBytes;
        }
        Slog.e(TAG, "Invalid value in CarrierConfigManager.KEY_DATA_WARNING_THRESHOLD_BYTES_LONG; expected a non-negative value but got: " + warningBytes);
        return fallbackWarningBytes;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getLimitBytesFromCarrierConfig(PersistableBundle config, long fallbackLimitBytes) {
        if (config == null) {
            return fallbackLimitBytes;
        }
        long limitBytes = config.getLong("data_limit_threshold_bytes_long");
        if (limitBytes == -2) {
            return -1;
        }
        if (limitBytes == -1) {
            return getPlatformDefaultLimitBytes();
        }
        if (limitBytes >= 0) {
            return limitBytes;
        }
        Slog.e(TAG, "Invalid value in CarrierConfigManager.KEY_DATA_LIMIT_THRESHOLD_BYTES_LONG; expected a non-negative value but got: " + limitBytes);
        return fallbackLimitBytes;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock", "mNetworkPoliciesSecondLock"})
    public void handleNetworkPoliciesUpdateAL(boolean shouldNormalizePolicies) {
        if (shouldNormalizePolicies) {
            normalizePoliciesNL();
        }
        if (LOGD) {
            Slog.d(TAG, "handleNetworkPoliciesUpdateAL, shouldNormalizePolicies: " + shouldNormalizePolicies);
        }
        updateNetworkEnabledNL();
        updateNetworkRulesNL();
        updateNotificationsNL();
        writePolicyAL();
        syncMeteredIfacesToReplicaNL();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    public void updateNetworkEnabledNL() {
        if (LOGV) {
            Slog.v(TAG, "updateNetworkEnabledNL()");
        }
        Trace.traceBegin(2097152, "updateNetworkEnabledNL");
        long startTime = this.mStatLogger.getTime();
        int i = this.mNetworkPolicy.size() - 1;
        while (true) {
            boolean networkEnabled = false;
            if (i >= 0) {
                NetworkPolicy policy = this.mNetworkPolicy.valueAt(i);
                if (policy.limitBytes == -1 || !policy.hasCycle()) {
                    setNetworkTemplateEnabled(policy.template, true);
                } else {
                    Pair<ZonedDateTime, ZonedDateTime> cycle = (Pair) NetworkPolicyManager.cycleIterator(policy).next();
                    long start = ((ZonedDateTime) cycle.first).toInstant().toEpochMilli();
                    if (!(policy.isOverLimit(getTotalBytes(policy.template, start, ((ZonedDateTime) cycle.second).toInstant().toEpochMilli())) && policy.lastLimitSnooze < start)) {
                        networkEnabled = true;
                    }
                    setNetworkTemplateEnabled(policy.template, networkEnabled);
                }
                i--;
            } else {
                this.mStatLogger.logDurationStat(0, startTime);
                Trace.traceEnd(2097152);
                return;
            }
        }
    }

    private void setNetworkTemplateEnabled(NetworkTemplate template, boolean enabled) {
        this.mHandler.obtainMessage(18, enabled ? 1 : 0, 0, template).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetworkTemplateEnabledInner(NetworkTemplate template, boolean enabled) {
        if (template.getMatchRule() == 1) {
            IntArray matchingSubIds = new IntArray();
            synchronized (this.mNetworkPoliciesSecondLock) {
                for (int i = 0; i < this.mSubIdToSubscriberId.size(); i++) {
                    int subId = this.mSubIdToSubscriberId.keyAt(i);
                    if (template.matches(new NetworkIdentity(0, 0, this.mSubIdToSubscriberId.valueAt(i), (String) null, false, true, true))) {
                        matchingSubIds.add(subId);
                    }
                }
            }
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
            for (int i2 = 0; i2 < matchingSubIds.size(); i2++) {
                tm.setPolicyDataEnabled(enabled, matchingSubIds.get(i2));
            }
        }
    }

    private static void collectIfaces(ArraySet<String> ifaces, NetworkState state) {
        String baseIface = state.linkProperties.getInterfaceName();
        if (baseIface != null) {
            ifaces.add(baseIface);
        }
        for (LinkProperties stackedLink : state.linkProperties.getStackedLinks()) {
            String stackedIface = stackedLink.getInterfaceName();
            if (stackedIface != null) {
                ifaces.add(stackedIface);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateSubscriptions() {
        if (LOGV) {
            Slog.v(TAG, "updateSubscriptions()");
        }
        Trace.traceBegin(2097152, "updateSubscriptions");
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
        int[] subIds = ArrayUtils.defeatNullable(((SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class)).getActiveSubscriptionIdList());
        String[] mergedSubscriberIds = ArrayUtils.defeatNullable(tm.getMergedSubscriberIds());
        SparseArray<String> subIdToSubscriberId = new SparseArray<>(subIds.length);
        for (int subId : subIds) {
            String subscriberId = tm.getSubscriberId(subId);
            if (!TextUtils.isEmpty(subscriberId)) {
                subIdToSubscriberId.put(subId, subscriberId);
            } else {
                Slog.wtf(TAG, "Missing subscriberId for subId " + subId);
            }
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            this.mSubIdToSubscriberId.clear();
            for (int i = 0; i < subIdToSubscriberId.size(); i++) {
                this.mSubIdToSubscriberId.put(subIdToSubscriberId.keyAt(i), subIdToSubscriberId.valueAt(i));
            }
            this.mMergedSubscriberIds = mergedSubscriberIds;
        }
        Trace.traceEnd(2097152);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    public void updateNetworkRulesNL() {
        boolean quotaEnabled;
        long j;
        NetworkState[] states;
        int i;
        int i2;
        long j2;
        long quotaBytes;
        int subId;
        int i3;
        NetworkPolicy policy;
        long quotaBytes2;
        if (LOGV) {
            Slog.v(TAG, "updateNetworkRulesNL()");
        }
        Trace.traceBegin(2097152, "updateNetworkRulesNL");
        try {
            NetworkState[] states2 = defeatNullable(this.mConnManager.getAllNetworkState());
            this.mNetIdToSubId.clear();
            ArrayMap<NetworkState, NetworkIdentity> identified = new ArrayMap<>();
            int length = states2.length;
            int i4 = 0;
            while (true) {
                quotaEnabled = true;
                if (i4 >= length) {
                    break;
                }
                NetworkState state = states2[i4];
                if (state.network != null) {
                    this.mNetIdToSubId.put(state.network.netId, parseSubId(state));
                }
                if (state.networkInfo != null && state.networkInfo.isConnected()) {
                    identified.put(state, NetworkIdentity.buildNetworkIdentity(this.mContext, state, true));
                }
                i4++;
            }
            ArraySet<String> newMeteredIfaces = new ArraySet<>();
            ArraySet<String> matchingIfaces = new ArraySet<>();
            long lowestRule = Long.MAX_VALUE;
            for (int i5 = this.mNetworkPolicy.size() - 1; i5 >= 0; i5 = i3 - 1) {
                NetworkPolicy policy2 = this.mNetworkPolicy.valueAt(i5);
                matchingIfaces.clear();
                for (int j3 = identified.size() - 1; j3 >= 0; j3--) {
                    if (policy2.template.matches(identified.valueAt(j3))) {
                        collectIfaces(matchingIfaces, identified.keyAt(j3));
                    }
                }
                if (LOGD) {
                    Slog.d(TAG, "Applying " + policy2 + " to ifaces " + matchingIfaces);
                }
                boolean hasWarning = policy2.warningBytes != -1;
                boolean hasLimit = policy2.limitBytes != -1;
                if (hasLimit || policy2.metered) {
                    if (!hasLimit || !policy2.hasCycle()) {
                        i3 = i5;
                        policy = policy2;
                        quotaBytes2 = JobStatus.NO_LATEST_RUNTIME;
                    } else {
                        Pair<ZonedDateTime, ZonedDateTime> cycle = (Pair) NetworkPolicyManager.cycleIterator(policy2).next();
                        long start = ((ZonedDateTime) cycle.first).toInstant().toEpochMilli();
                        i3 = i5;
                        policy = policy2;
                        long totalBytes = getTotalBytes(policy2.template, start, ((ZonedDateTime) cycle.second).toInstant().toEpochMilli());
                        if (policy.lastLimitSnooze >= start) {
                            quotaBytes2 = JobStatus.NO_LATEST_RUNTIME;
                        } else {
                            quotaBytes2 = Math.max(1L, policy.limitBytes - totalBytes);
                        }
                    }
                    if (matchingIfaces.size() > 1) {
                        Slog.w(TAG, "shared quota unsupported; generating rule for each iface");
                    }
                    for (int j4 = matchingIfaces.size() - 1; j4 >= 0; j4--) {
                        String iface = matchingIfaces.valueAt(j4);
                        setInterfaceQuotaAsync(iface, quotaBytes2);
                        newMeteredIfaces.add(iface);
                    }
                } else {
                    i3 = i5;
                    policy = policy2;
                }
                if (hasWarning && policy.warningBytes < lowestRule) {
                    lowestRule = policy.warningBytes;
                }
                if (hasLimit && policy.limitBytes < lowestRule) {
                    lowestRule = policy.limitBytes;
                }
            }
            int length2 = states2.length;
            int i6 = 0;
            while (true) {
                j = JobStatus.NO_LATEST_RUNTIME;
                if (i6 >= length2) {
                    break;
                }
                NetworkState state2 = states2[i6];
                if (state2.networkInfo != null && state2.networkInfo.isConnected() && !state2.networkCapabilities.hasCapability(11)) {
                    matchingIfaces.clear();
                    collectIfaces(matchingIfaces, state2);
                    for (int j5 = matchingIfaces.size() - 1; j5 >= 0; j5--) {
                        String iface2 = matchingIfaces.valueAt(j5);
                        if (!newMeteredIfaces.contains(iface2)) {
                            setInterfaceQuotaAsync(iface2, JobStatus.NO_LATEST_RUNTIME);
                            newMeteredIfaces.add(iface2);
                        }
                    }
                }
                i6++;
            }
            for (int i7 = this.mMeteredIfaces.size() - 1; i7 >= 0; i7--) {
                String iface3 = this.mMeteredIfaces.valueAt(i7);
                if (!newMeteredIfaces.contains(iface3)) {
                    removeInterfaceQuotaAsync(iface3);
                }
            }
            this.mMeteredIfaces = newMeteredIfaces;
            ContentResolver cr = this.mContext.getContentResolver();
            if (Settings.Global.getInt(cr, "netpolicy_quota_enabled", 1) == 0) {
                quotaEnabled = false;
            }
            long quotaUnlimited = Settings.Global.getLong(cr, "netpolicy_quota_unlimited", QUOTA_UNLIMITED_DEFAULT);
            float quotaLimited = Settings.Global.getFloat(cr, "netpolicy_quota_limited", QUOTA_LIMITED_DEFAULT);
            this.mSubscriptionOpportunisticQuota.clear();
            int length3 = states2.length;
            int i8 = 0;
            while (i8 < length3) {
                NetworkState state3 = states2[i8];
                if (!quotaEnabled) {
                    states = states2;
                    i2 = length3;
                    i = i8;
                    j2 = j;
                } else if (state3.network == null) {
                    states = states2;
                    i2 = length3;
                    i = i8;
                    j2 = j;
                } else {
                    int subId2 = getSubIdLocked(state3.network);
                    SubscriptionPlan plan = getPrimarySubscriptionPlanLocked(subId2);
                    if (plan == null) {
                        states = states2;
                        i2 = length3;
                        i = i8;
                        j2 = j;
                    } else {
                        long limitBytes = plan.getDataLimitBytes();
                        if (!state3.networkCapabilities.hasCapability(18)) {
                            quotaBytes = 0;
                            states = states2;
                            subId = subId2;
                            i2 = length3;
                            i = i8;
                            j2 = j;
                        } else if (limitBytes == -1) {
                            quotaBytes = -1;
                            states = states2;
                            subId = subId2;
                            i2 = length3;
                            i = i8;
                            j2 = j;
                        } else if (limitBytes == j) {
                            quotaBytes = quotaUnlimited;
                            states = states2;
                            subId = subId2;
                            i2 = length3;
                            i = i8;
                            j2 = j;
                        } else {
                            Range<ZonedDateTime> cycle2 = plan.cycleIterator().next();
                            long start2 = cycle2.getLower().toInstant().toEpochMilli();
                            long end = cycle2.getUpper().toInstant().toEpochMilli();
                            Instant now = this.mClock.instant();
                            long startOfDay = ZonedDateTime.ofInstant(now, cycle2.getLower().getZone()).truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli();
                            NetworkTemplate buildTemplateMobileAll = NetworkTemplate.buildTemplateMobileAll(state3.subscriberId);
                            i2 = length3;
                            i = i8;
                            states = states2;
                            subId = subId2;
                            j2 = JobStatus.NO_LATEST_RUNTIME;
                            quotaBytes = Math.max(0L, (long) (((float) ((limitBytes - getTotalBytes(buildTemplateMobileAll, start2, startOfDay)) / ((((end - now.toEpochMilli()) - 1) / TimeUnit.DAYS.toMillis(1)) + 1))) * quotaLimited));
                        }
                        this.mSubscriptionOpportunisticQuota.put(subId, quotaBytes);
                    }
                }
                i8 = i + 1;
                j = j2;
                length3 = i2;
                states2 = states;
            }
            if (LOGD) {
                Slog.d(TAG, "updateNetworkRulesNL, mMeteredIfaces: " + this.mMeteredIfaces);
            }
            ArraySet<String> arraySet = this.mMeteredIfaces;
            this.mHandler.obtainMessage(2, (String[]) arraySet.toArray(new String[arraySet.size()])).sendToTarget();
            this.mHandler.obtainMessage(7, Long.valueOf(lowestRule)).sendToTarget();
            Trace.traceEnd(2097152);
        } catch (RemoteException e) {
        }
    }

    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private void ensureActiveMobilePolicyAL() {
        if (LOGV) {
            Slog.v(TAG, "ensureActiveMobilePolicyAL()");
        }
        if (!this.mSuppressDefaultPolicy) {
            if (LOGD) {
                Slog.d(TAG, "ensureActiveMobilePolicyAL");
            }
            for (int i = 0; i < this.mSubIdToSubscriberId.size(); i++) {
                ensureActiveMobilePolicyAL(this.mSubIdToSubscriberId.keyAt(i), this.mSubIdToSubscriberId.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private boolean ensureActiveMobilePolicyAL(int subId, String subscriberId) {
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, (String) null, false, true, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkTemplate template = this.mNetworkPolicy.keyAt(i);
            if (template.matches(probeIdent)) {
                if (!LOGD) {
                    return false;
                } else {
                    Slog.d(TAG, "Found template " + template + " which matches subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId));
                    return false;
                }
            }
        }
        Slog.i(TAG, "No policy for subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId) + "; generating default policy");
        addNetworkPolicyAL(buildDefaultMobilePolicy(subId, subscriberId));
        return true;
    }

    private long getPlatformDefaultWarningBytes() {
        int dataWarningConfig = this.mContext.getResources().getInteger(17694855);
        if (((long) dataWarningConfig) == -1) {
            return -1;
        }
        return ((long) dataWarningConfig) * 1048576;
    }

    private long getPlatformDefaultLimitBytes() {
        return -1;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public NetworkPolicy buildDefaultMobilePolicy(int subId, String subscriberId) {
        Throwable th;
        NetworkPolicy policy = new NetworkPolicy(NetworkTemplate.buildTemplateMobileAll(subscriberId), NetworkPolicy.buildRule(ZonedDateTime.now().getDayOfMonth(), ZoneId.systemDefault()), getPlatformDefaultWarningBytes(), getPlatformDefaultLimitBytes(), -1, -1, true, true);
        synchronized (this.mUidRulesFirstLock) {
            try {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    updateDefaultMobilePolicyAL(subId, policy);
                }
                try {
                    return policy;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private boolean updateDefaultMobilePolicyAL(int subId, NetworkPolicy policy) {
        int currentCycleDay;
        if (!policy.inferred) {
            if (LOGD) {
                Slog.d(TAG, "Ignoring user-defined policy " + policy);
            }
            return false;
        }
        NetworkPolicy original = new NetworkPolicy(policy.template, policy.cycleRule, policy.warningBytes, policy.limitBytes, policy.lastWarningSnooze, policy.lastLimitSnooze, policy.metered, policy.inferred);
        SubscriptionPlan[] plans = this.mSubscriptionPlans.get(subId);
        if (!ArrayUtils.isEmpty(plans)) {
            SubscriptionPlan plan = plans[0];
            policy.cycleRule = plan.getCycleRule();
            long planLimitBytes = plan.getDataLimitBytes();
            if (planLimitBytes == -1) {
                policy.warningBytes = getPlatformDefaultWarningBytes();
                policy.limitBytes = getPlatformDefaultLimitBytes();
            } else if (planLimitBytes == JobStatus.NO_LATEST_RUNTIME) {
                policy.warningBytes = -1;
                policy.limitBytes = -1;
            } else {
                policy.warningBytes = (9 * planLimitBytes) / 10;
                int dataLimitBehavior = plan.getDataLimitBehavior();
                if (dataLimitBehavior == 0 || dataLimitBehavior == 1) {
                    policy.limitBytes = planLimitBytes;
                } else {
                    policy.limitBytes = -1;
                }
            }
        } else {
            PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(subId);
            if (policy.cycleRule.isMonthly()) {
                currentCycleDay = policy.cycleRule.start.getDayOfMonth();
            } else {
                currentCycleDay = -1;
            }
            policy.cycleRule = NetworkPolicy.buildRule(getCycleDayFromCarrierConfig(config, currentCycleDay), ZoneId.systemDefault());
            policy.warningBytes = getWarningBytesFromCarrierConfig(config, policy.warningBytes);
            policy.limitBytes = getLimitBytesFromCarrierConfig(config, policy.limitBytes);
        }
        if (policy.equals(original)) {
            return false;
        }
        Slog.d(TAG, "Updated " + original + " to " + policy);
        return true;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mUidRulesFirstLock", "mNetworkPoliciesSecondLock"})
    public void readPolicyAL() {
        FileInputStream fis;
        Throwable th;
        Exception e;
        int version;
        String networkId;
        int type;
        RecurrenceRule cycleRule;
        long lastLimitSnooze;
        boolean metered;
        long lastWarningSnooze;
        boolean inferred;
        String cycleTimezone;
        if (LOGV) {
            Slog.v(TAG, "readPolicyAL()");
        }
        this.mNetworkPolicy.clear();
        this.mSubscriptionPlans.clear();
        this.mSubscriptionPlansOwner.clear();
        this.mUidPolicy.clear();
        try {
            FileInputStream fis2 = this.mPolicyFile.openRead();
            try {
                XmlPullParser in = Xml.newPullParser();
                in.setInput(fis2, StandardCharsets.UTF_8.name());
                SparseBooleanArray whitelistedRestrictBackground = new SparseBooleanArray();
                int version2 = 1;
                boolean z = false;
                boolean insideWhitelist = false;
                while (true) {
                    int type2 = in.next();
                    boolean z2 = true;
                    if (type2 == 1) {
                        break;
                    }
                    String tag = in.getName();
                    if (type2 == 2) {
                        if (TAG_POLICY_LIST.equals(tag)) {
                            boolean z3 = this.mRestrictBackground;
                            version2 = XmlUtils.readIntAttribute(in, ATTR_VERSION);
                            if (version2 < 3 || !XmlUtils.readBooleanAttribute(in, ATTR_RESTRICT_BACKGROUND)) {
                                z2 = z;
                            }
                            this.mLoadedRestrictBackground = z2;
                            fis = fis2;
                        } else if (TAG_NETWORK_POLICY.equals(tag)) {
                            int networkTemplate = XmlUtils.readIntAttribute(in, ATTR_NETWORK_TEMPLATE);
                            String subscriberId = in.getAttributeValue(null, ATTR_SUBSCRIBER_ID);
                            if (version2 >= 9) {
                                fis = fis2;
                                try {
                                    networkId = in.getAttributeValue(null, ATTR_NETWORK_ID);
                                } catch (FileNotFoundException e2) {
                                    upgradeDefaultBackgroundDataUL();
                                    IoUtils.closeQuietly(fis);
                                } catch (Exception e3) {
                                    e = e3;
                                    Log.wtf(TAG, "problem reading network policy", e);
                                    IoUtils.closeQuietly(fis);
                                }
                            } else {
                                fis = fis2;
                                networkId = null;
                            }
                            if (version2 >= 11) {
                                type = type2;
                                cycleRule = new RecurrenceRule(RecurrenceRule.convertZonedDateTime(XmlUtils.readStringAttribute(in, ATTR_CYCLE_START)), RecurrenceRule.convertZonedDateTime(XmlUtils.readStringAttribute(in, ATTR_CYCLE_END)), RecurrenceRule.convertPeriod(XmlUtils.readStringAttribute(in, ATTR_CYCLE_PERIOD)));
                            } else {
                                type = type2;
                                int cycleDay = XmlUtils.readIntAttribute(in, ATTR_CYCLE_DAY);
                                if (version2 >= 6) {
                                    cycleTimezone = in.getAttributeValue(null, ATTR_CYCLE_TIMEZONE);
                                } else {
                                    cycleTimezone = "UTC";
                                }
                                cycleRule = NetworkPolicy.buildRule(cycleDay, ZoneId.of(cycleTimezone));
                            }
                            long warningBytes = XmlUtils.readLongAttribute(in, ATTR_WARNING_BYTES);
                            long limitBytes = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES);
                            if (version2 >= 5) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_LIMIT_SNOOZE);
                            } else if (version2 >= 2) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_SNOOZE);
                            } else {
                                lastLimitSnooze = -1;
                            }
                            if (version2 >= 4) {
                                metered = XmlUtils.readBooleanAttribute(in, ATTR_METERED);
                            } else if (networkTemplate != 1) {
                                metered = false;
                            } else {
                                metered = true;
                            }
                            if (version2 >= 5) {
                                lastWarningSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_WARNING_SNOOZE);
                            } else {
                                lastWarningSnooze = -1;
                            }
                            if (version2 >= 7) {
                                inferred = XmlUtils.readBooleanAttribute(in, ATTR_INFERRED);
                            } else {
                                inferred = false;
                            }
                            version = version2;
                            NetworkTemplate template = new NetworkTemplate(networkTemplate, subscriberId, networkId);
                            if (template.isPersistable()) {
                                this.mNetworkPolicy.put(template, new NetworkPolicy(template, cycleRule, warningBytes, limitBytes, lastWarningSnooze, lastLimitSnooze, metered, inferred));
                            }
                        } else {
                            fis = fis2;
                            version = version2;
                            if (TAG_SUBSCRIPTION_PLAN.equals(tag)) {
                                SubscriptionPlan.Builder builder = new SubscriptionPlan.Builder(RecurrenceRule.convertZonedDateTime(XmlUtils.readStringAttribute(in, ATTR_CYCLE_START)), RecurrenceRule.convertZonedDateTime(XmlUtils.readStringAttribute(in, ATTR_CYCLE_END)), RecurrenceRule.convertPeriod(XmlUtils.readStringAttribute(in, ATTR_CYCLE_PERIOD)));
                                builder.setTitle(XmlUtils.readStringAttribute(in, ATTR_TITLE));
                                builder.setSummary(XmlUtils.readStringAttribute(in, ATTR_SUMMARY));
                                long limitBytes2 = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES, -1);
                                int limitBehavior = XmlUtils.readIntAttribute(in, ATTR_LIMIT_BEHAVIOR, -1);
                                if (!(limitBytes2 == -1 || limitBehavior == -1)) {
                                    builder.setDataLimit(limitBytes2, limitBehavior);
                                }
                                long usageBytes = XmlUtils.readLongAttribute(in, ATTR_USAGE_BYTES, -1);
                                long usageTime = XmlUtils.readLongAttribute(in, ATTR_USAGE_TIME, -1);
                                if (usageBytes != -1) {
                                    if (usageTime != -1) {
                                        builder.setDataUsage(usageBytes, usageTime);
                                    }
                                }
                                int subId = XmlUtils.readIntAttribute(in, ATTR_SUB_ID);
                                this.mSubscriptionPlans.put(subId, (SubscriptionPlan[]) ArrayUtils.appendElement(SubscriptionPlan.class, this.mSubscriptionPlans.get(subId), builder.build()));
                                this.mSubscriptionPlansOwner.put(subId, XmlUtils.readStringAttribute(in, ATTR_OWNER_PACKAGE));
                            } else if (TAG_UID_POLICY.equals(tag)) {
                                int uid = XmlUtils.readIntAttribute(in, "uid");
                                int policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                                if (UserHandle.isApp(uid)) {
                                    setUidPolicyUncheckedUL(uid, policy, false);
                                } else {
                                    Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                                }
                            } else if (TAG_APP_POLICY.equals(tag)) {
                                int appId = XmlUtils.readIntAttribute(in, ATTR_APP_ID);
                                int policy2 = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                                int uid2 = UserHandle.getUid(0, appId);
                                if (UserHandle.isApp(uid2)) {
                                    setUidPolicyUncheckedUL(uid2, policy2, false);
                                } else {
                                    Slog.w(TAG, "unable to apply policy to UID " + uid2 + "; ignoring");
                                }
                            } else if (TAG_WHITELIST.equals(tag)) {
                                insideWhitelist = true;
                                version2 = version;
                            } else if (TAG_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                                whitelistedRestrictBackground.append(XmlUtils.readIntAttribute(in, "uid"), true);
                            } else if (TAG_REVOKED_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                                this.mRestrictBackgroundWhitelistRevokedUids.put(XmlUtils.readIntAttribute(in, "uid"), true);
                            }
                        }
                        fis2 = fis;
                        z = false;
                    } else {
                        fis = fis2;
                        version = version2;
                        if (type2 == 3 && TAG_WHITELIST.equals(tag)) {
                            insideWhitelist = false;
                            version2 = version;
                            fis2 = fis;
                            z = false;
                        }
                    }
                    version2 = version;
                    fis2 = fis;
                    z = false;
                }
                fis = fis2;
                int size = whitelistedRestrictBackground.size();
                for (int i = 0; i < size; i++) {
                    int uid3 = whitelistedRestrictBackground.keyAt(i);
                    int policy3 = this.mUidPolicy.get(uid3, 0);
                    if ((policy3 & 1) != 0) {
                        Slog.w(TAG, "ignoring restrict-background-whitelist for " + uid3 + " because its policy is " + NetworkPolicyManager.uidPoliciesToString(policy3));
                    } else if (UserHandle.isApp(uid3)) {
                        int newPolicy = policy3 | 4;
                        if (LOGV) {
                            Log.v(TAG, "new policy for " + uid3 + ": " + NetworkPolicyManager.uidPoliciesToString(newPolicy));
                        }
                        setUidPolicyUncheckedUL(uid3, newPolicy, false);
                    } else {
                        Slog.w(TAG, "unable to update policy on UID " + uid3);
                    }
                }
            } catch (FileNotFoundException e4) {
                fis = fis2;
                upgradeDefaultBackgroundDataUL();
                IoUtils.closeQuietly(fis);
            } catch (Exception e5) {
                fis = fis2;
                e = e5;
                Log.wtf(TAG, "problem reading network policy", e);
                IoUtils.closeQuietly(fis);
            } catch (Throwable th2) {
                fis = fis2;
                th = th2;
                IoUtils.closeQuietly(fis);
                throw th;
            }
        } catch (FileNotFoundException e6) {
            fis = null;
            upgradeDefaultBackgroundDataUL();
            IoUtils.closeQuietly(fis);
        } catch (Exception e7) {
            fis = null;
            e = e7;
            Log.wtf(TAG, "problem reading network policy", e);
            IoUtils.closeQuietly(fis);
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(fis);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    private void upgradeDefaultBackgroundDataUL() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "default_restrict_background_data", 0) == 1) {
            z = true;
        }
        this.mLoadedRestrictBackground = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNetworkPoliciesSecondLock", "mUidRulesFirstLock"})
    private void upgradeWifiMeteredOverrideAL() {
        int i;
        boolean modified = false;
        WifiManager wm = (WifiManager) this.mContext.getSystemService(WifiManager.class);
        List<WifiConfiguration> configs = wm.getConfiguredNetworks();
        int i2 = 0;
        while (i2 < this.mNetworkPolicy.size()) {
            NetworkPolicy policy = this.mNetworkPolicy.valueAt(i2);
            if (policy.template.getMatchRule() != 4 || policy.inferred) {
                i2++;
            } else {
                this.mNetworkPolicy.removeAt(i2);
                modified = true;
                String networkId = NetworkPolicyManager.resolveNetworkId(policy.template.getNetworkId());
                for (WifiConfiguration config : configs) {
                    if (Objects.equals(NetworkPolicyManager.resolveNetworkId(config), networkId)) {
                        Slog.d(TAG, "Found network " + networkId + "; upgrading metered hint");
                        if (policy.metered) {
                            i = 1;
                        } else {
                            i = 2;
                        }
                        config.meteredOverride = i;
                        wm.updateNetwork(config);
                    }
                }
            }
        }
        if (modified) {
            writePolicyAL();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0262  */
    @GuardedBy({"mUidRulesFirstLock", "mNetworkPoliciesSecondLock"})
    public void writePolicyAL() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        FileOutputStream fos;
        String str6 = TAG_REVOKED_RESTRICT_BACKGROUND;
        String str7 = TAG_WHITELIST;
        if (LOGV) {
            Slog.v(TAG, "writePolicyAL()");
        }
        FileOutputStream fos2 = null;
        try {
            fos2 = this.mPolicyFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos2, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.startTag(null, TAG_POLICY_LIST);
                XmlUtils.writeIntAttribute(out, ATTR_VERSION, 11);
                XmlUtils.writeBooleanAttribute(out, ATTR_RESTRICT_BACKGROUND, this.mRestrictBackground);
                int i = 0;
                while (true) {
                    int size = this.mNetworkPolicy.size();
                    str = ATTR_LIMIT_BYTES;
                    str2 = ATTR_CYCLE_PERIOD;
                    str3 = ATTR_CYCLE_START;
                    if (i >= size) {
                        break;
                    }
                    try {
                        NetworkPolicy policy = this.mNetworkPolicy.valueAt(i);
                        NetworkTemplate template = policy.template;
                        if (!template.isPersistable()) {
                            str5 = str6;
                            str4 = str7;
                            fos = fos2;
                        } else {
                            fos = fos2;
                            try {
                                out.startTag(null, TAG_NETWORK_POLICY);
                                str5 = str6;
                                XmlUtils.writeIntAttribute(out, ATTR_NETWORK_TEMPLATE, template.getMatchRule());
                                String subscriberId = template.getSubscriberId();
                                if (subscriberId != null) {
                                    str4 = str7;
                                    out.attribute(null, ATTR_SUBSCRIBER_ID, subscriberId);
                                } else {
                                    str4 = str7;
                                }
                                String networkId = template.getNetworkId();
                                if (networkId != null) {
                                    out.attribute(null, ATTR_NETWORK_ID, networkId);
                                }
                                XmlUtils.writeStringAttribute(out, str3, RecurrenceRule.convertZonedDateTime(((RecurrenceRule) policy.cycleRule).start));
                                XmlUtils.writeStringAttribute(out, ATTR_CYCLE_END, RecurrenceRule.convertZonedDateTime(policy.cycleRule.end));
                                XmlUtils.writeStringAttribute(out, str2, RecurrenceRule.convertPeriod(policy.cycleRule.period));
                                XmlUtils.writeLongAttribute(out, ATTR_WARNING_BYTES, policy.warningBytes);
                                XmlUtils.writeLongAttribute(out, str, policy.limitBytes);
                                XmlUtils.writeLongAttribute(out, ATTR_LAST_WARNING_SNOOZE, policy.lastWarningSnooze);
                                XmlUtils.writeLongAttribute(out, ATTR_LAST_LIMIT_SNOOZE, policy.lastLimitSnooze);
                                XmlUtils.writeBooleanAttribute(out, ATTR_METERED, policy.metered);
                                XmlUtils.writeBooleanAttribute(out, ATTR_INFERRED, policy.inferred);
                                out.endTag(null, TAG_NETWORK_POLICY);
                            } catch (IOException e) {
                                fos2 = fos;
                                if (fos2 != null) {
                                }
                            }
                        }
                        i++;
                        fos2 = fos;
                        str6 = str5;
                        str7 = str4;
                    } catch (IOException e2) {
                        if (fos2 != null) {
                        }
                    }
                }
                String str8 = str6;
                int i2 = 0;
                while (i2 < this.mSubscriptionPlans.size()) {
                    try {
                        int subId = this.mSubscriptionPlans.keyAt(i2);
                        String ownerPackage = this.mSubscriptionPlansOwner.get(subId);
                        SubscriptionPlan[] plans = this.mSubscriptionPlans.valueAt(i2);
                        if (!ArrayUtils.isEmpty(plans)) {
                            int length = plans.length;
                            int i3 = 0;
                            while (i3 < length) {
                                SubscriptionPlan plan = plans[i3];
                                out.startTag(null, TAG_SUBSCRIPTION_PLAN);
                                XmlUtils.writeIntAttribute(out, ATTR_SUB_ID, subId);
                                XmlUtils.writeStringAttribute(out, ATTR_OWNER_PACKAGE, ownerPackage);
                                RecurrenceRule cycleRule = plan.getCycleRule();
                                XmlUtils.writeStringAttribute(out, str3, RecurrenceRule.convertZonedDateTime(cycleRule.start));
                                XmlUtils.writeStringAttribute(out, ATTR_CYCLE_END, RecurrenceRule.convertZonedDateTime(cycleRule.end));
                                XmlUtils.writeStringAttribute(out, str2, RecurrenceRule.convertPeriod(cycleRule.period));
                                XmlUtils.writeStringAttribute(out, ATTR_TITLE, plan.getTitle());
                                XmlUtils.writeStringAttribute(out, ATTR_SUMMARY, plan.getSummary());
                                XmlUtils.writeLongAttribute(out, str, plan.getDataLimitBytes());
                                XmlUtils.writeIntAttribute(out, ATTR_LIMIT_BEHAVIOR, plan.getDataLimitBehavior());
                                XmlUtils.writeLongAttribute(out, ATTR_USAGE_BYTES, plan.getDataUsageBytes());
                                XmlUtils.writeLongAttribute(out, ATTR_USAGE_TIME, plan.getDataUsageTime());
                                out.endTag(null, TAG_SUBSCRIPTION_PLAN);
                                i3++;
                                str3 = str3;
                                str2 = str2;
                                plans = plans;
                                subId = subId;
                                str = str;
                                length = length;
                                ownerPackage = ownerPackage;
                            }
                        }
                        i2++;
                        str3 = str3;
                        str2 = str2;
                        str = str;
                    } catch (IOException e3) {
                        fos2 = fos2;
                        if (fos2 != null) {
                        }
                    }
                }
                for (int i4 = 0; i4 < this.mUidPolicy.size(); i4++) {
                    int uid = this.mUidPolicy.keyAt(i4);
                    int policy2 = this.mUidPolicy.valueAt(i4);
                    if (policy2 != 0) {
                        out.startTag(null, TAG_UID_POLICY);
                        XmlUtils.writeIntAttribute(out, "uid", uid);
                        XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy2);
                        out.endTag(null, TAG_UID_POLICY);
                    }
                }
                out.endTag(null, TAG_POLICY_LIST);
                out.startTag(null, str7);
                int size2 = this.mRestrictBackgroundWhitelistRevokedUids.size();
                int i5 = 0;
                while (i5 < size2) {
                    int uid2 = this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i5);
                    out.startTag(null, str8);
                    XmlUtils.writeIntAttribute(out, "uid", uid2);
                    out.endTag(null, str8);
                    i5++;
                    str8 = str8;
                }
                out.endTag(null, str7);
                out.endDocument();
                try {
                    this.mPolicyFile.finishWrite(fos2);
                } catch (IOException e4) {
                    fos2 = fos2;
                }
            } catch (IOException e5) {
                if (fos2 != null) {
                    this.mPolicyFile.failWrite(fos2);
                }
            }
        } catch (IOException e6) {
            if (fos2 != null) {
            }
        }
    }

    public void setUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mUidRulesFirstLock) {
                long token = Binder.clearCallingIdentity();
                try {
                    int oldPolicy = this.mUidPolicy.get(uid, 0);
                    if (oldPolicy != policy) {
                        setUidPolicyUncheckedUL(uid, oldPolicy, policy, true);
                        this.mLogger.uidPolicyChanged(uid, oldPolicy, policy);
                    }
                } finally {
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
            synchronized (this.mUidRulesFirstLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                int policy2 = policy | oldPolicy;
                if (oldPolicy != policy2) {
                    setUidPolicyUncheckedUL(uid, oldPolicy, policy2, true);
                    this.mLogger.uidPolicyChanged(uid, oldPolicy, policy2);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    public void removeUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mUidRulesFirstLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                int policy2 = oldPolicy & (~policy);
                if (oldPolicy != policy2) {
                    setUidPolicyUncheckedUL(uid, oldPolicy, policy2, true);
                    this.mLogger.uidPolicyChanged(uid, oldPolicy, policy2);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void setUidPolicyUncheckedUL(int uid, int oldPolicy, int policy, boolean persist) {
        boolean notifyApp = false;
        setUidPolicyUncheckedUL(uid, policy, false);
        if (!isUidValidForWhitelistRules(uid)) {
            notifyApp = false;
        } else {
            boolean wasBlacklisted = oldPolicy == 1;
            boolean isBlacklisted = policy == 1;
            boolean wasWhitelisted = oldPolicy == 4;
            boolean isWhitelisted = policy == 4;
            boolean wasBlocked = wasBlacklisted || (this.mRestrictBackground && !wasWhitelisted);
            boolean isBlocked = isBlacklisted || (this.mRestrictBackground && !isWhitelisted);
            if (wasWhitelisted && ((!isWhitelisted || isBlacklisted) && this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && !this.mRestrictBackgroundWhitelistRevokedUids.get(uid))) {
                if (LOGD) {
                    Slog.d(TAG, "Adding uid " + uid + " to revoked restrict background whitelist");
                }
                this.mRestrictBackgroundWhitelistRevokedUids.append(uid, true);
            }
            if (wasBlocked != isBlocked) {
                notifyApp = true;
            }
        }
        this.mHandler.obtainMessage(13, uid, policy, Boolean.valueOf(notifyApp)).sendToTarget();
        if (persist) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                writePolicyAL();
            }
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void setUidPolicyUncheckedUL(int uid, int policy, boolean persist) {
        if (policy == 0) {
            this.mUidPolicy.delete(uid);
        } else {
            this.mUidPolicy.put(uid, policy);
        }
        updateRulesForDataUsageRestrictionsUL(uid);
        if (persist) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                writePolicyAL();
            }
        }
    }

    public int getUidPolicy(int uid) {
        int i;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            i = this.mUidPolicy.get(uid, 0);
        }
        return i;
    }

    public int[] getUidsWithPolicy(int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        int[] uids = new int[0];
        synchronized (this.mUidRulesFirstLock) {
            for (int i = 0; i < this.mUidPolicy.size(); i++) {
                int uid = this.mUidPolicy.keyAt(i);
                int uidPolicy = this.mUidPolicy.valueAt(i);
                if ((policy == 0 && uidPolicy == 0) || (uidPolicy & policy) != 0) {
                    uids = ArrayUtils.appendInt(uids, uid);
                }
            }
        }
        return uids;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public boolean removeUserStateUL(int userId, boolean writePolicy) {
        this.mLogger.removingUserState(userId);
        boolean changed = false;
        for (int i = this.mRestrictBackgroundWhitelistRevokedUids.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i)) == userId) {
                this.mRestrictBackgroundWhitelistRevokedUids.removeAt(i);
                changed = true;
            }
        }
        int[] uids = new int[0];
        for (int i2 = 0; i2 < this.mUidPolicy.size(); i2++) {
            int uid = this.mUidPolicy.keyAt(i2);
            if (UserHandle.getUserId(uid) == userId) {
                uids = ArrayUtils.appendInt(uids, uid);
            }
        }
        if (uids.length > 0) {
            for (int uid2 : uids) {
                this.mUidPolicy.delete(uid2);
            }
            changed = true;
        }
        if (LOGD) {
            Slog.d(TAG, "removeUserStateUL userId: " + userId + ", writePolicy: " + writePolicy + ", changed : " + changed);
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            updateRulesForGlobalChangeAL(true);
            if (writePolicy && changed) {
                writePolicyAL();
            }
        }
        return changed;
    }

    public void registerListener(INetworkPolicyListener listener) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.NETWORKPOLICYMANAGER_REGISTERLISTENER);
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.register(listener);
    }

    public void unregisterListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.unregister(listener);
    }

    public void setNetworkPolicies(NetworkPolicy[] policies) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (LOGD) {
            Slog.d(TAG, "setNetworkPolicies");
        }
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUidRulesFirstLock) {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    normalizePoliciesNL(policies);
                    handleNetworkPoliciesUpdateAL(false);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void addNetworkPolicyAL(NetworkPolicy policy) {
        if (LOGD) {
            Slog.d(TAG, "addNetworkPolicyAL");
        }
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
        synchronized (this.mNetworkPoliciesSecondLock) {
            int size = this.mNetworkPolicy.size();
            policies = new NetworkPolicy[size];
            for (int i = 0; i < size; i++) {
                policies[i] = this.mNetworkPolicy.valueAt(i);
            }
        }
        return policies;
    }

    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private void normalizePoliciesNL() {
        normalizePoliciesNL(getNetworkPolicies(this.mContext.getOpPackageName()));
    }

    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private void normalizePoliciesNL(NetworkPolicy[] policies) {
        this.mNetworkPolicy.clear();
        for (NetworkPolicy policy : policies) {
            if (policy != null) {
                policy.template = NetworkTemplate.normalize(policy.template, this.mMergedSubscriberIds);
                NetworkPolicy existing = this.mNetworkPolicy.get(policy.template);
                if (existing == null || existing.compareTo(policy) > 0) {
                    if (existing != null) {
                        Slog.d(TAG, "Normalization replaced " + existing + " with " + policy);
                    }
                    this.mNetworkPolicy.put(policy.template, policy);
                }
            }
        }
    }

    public void snoozeLimit(NetworkTemplate template) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (LOGD) {
            Slog.d(TAG, "snoozeLimit");
        }
        long token = Binder.clearCallingIdentity();
        try {
            performSnooze(template, 35);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void performSnooze(NetworkTemplate template, int type) {
        long currentTime = this.mClock.millis();
        synchronized (this.mUidRulesFirstLock) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                NetworkPolicy policy = this.mNetworkPolicy.get(template);
                if (policy != null) {
                    if (type == 34) {
                        policy.lastWarningSnooze = currentTime;
                    } else if (type == 35) {
                        policy.lastLimitSnooze = currentTime;
                    } else if (type == 45) {
                        policy.lastRapidSnooze = currentTime;
                    } else {
                        throw new IllegalArgumentException("unexpected type");
                    }
                    if (LOGD) {
                        Slog.d(TAG, "performSnooze, type: " + type);
                    }
                    handleNetworkPoliciesUpdateAL(true);
                } else {
                    throw new IllegalArgumentException("unable to find policy for " + template);
                }
            }
        }
    }

    public void onTetheringChanged(String iface, boolean tethering) {
        synchronized (this.mUidRulesFirstLock) {
            if (this.mRestrictBackground && tethering) {
                Log.d(TAG, "Tethering on (" + iface + "); disable Data Saver");
                setRestrictBackground(false);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setRestrictBackground(boolean restrictBackground) {
        Trace.traceBegin(2097152, "setRestrictBackground");
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mUidRulesFirstLock) {
                    setRestrictBackgroundUL(restrictBackground);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void setRestrictBackgroundUL(boolean restrictBackground) {
        Trace.traceBegin(2097152, "setRestrictBackgroundUL");
        Log.i(TAG, "mRestrictBackground=" + this.mRestrictBackground + ",restrictBackground=" + restrictBackground);
        try {
            if (restrictBackground == this.mRestrictBackground) {
                Slog.w(TAG, "setRestrictBackgroundUL: already " + restrictBackground);
                return;
            }
            Slog.d(TAG, "setRestrictBackgroundUL(): " + restrictBackground);
            boolean oldRestrictBackground = this.mRestrictBackground;
            this.mRestrictBackground = restrictBackground;
            updateRulesForRestrictBackgroundUL();
            try {
                if (!this.mNetworkManager.setDataSaverModeEnabled(this.mRestrictBackground)) {
                    Slog.e(TAG, "Could not change Data Saver Mode on NMS to " + this.mRestrictBackground);
                    this.mRestrictBackground = oldRestrictBackground;
                    Trace.traceEnd(2097152);
                    return;
                }
            } catch (RemoteException e) {
            }
            sendRestrictBackgroundChangedMsg();
            this.mLogger.restrictBackgroundChanged(oldRestrictBackground, this.mRestrictBackground);
            if (this.mRestrictBackgroundPowerState.globalBatterySaverEnabled) {
                this.mRestrictBackgroundChangedInBsm = true;
            }
            synchronized (this.mNetworkPoliciesSecondLock) {
                updateNotificationsNL();
                writePolicyAL();
            }
            Trace.traceEnd(2097152);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void sendRestrictBackgroundChangedMsg() {
        this.mHandler.removeMessages(6);
        this.mHandler.obtainMessage(6, this.mRestrictBackground ? 1 : 0, 0).sendToTarget();
    }

    /* JADX INFO: finally extract failed */
    public int getRestrictBackgroundByCaller() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        int uid = Binder.getCallingUid();
        synchronized (this.mUidRulesFirstLock) {
            long token = Binder.clearCallingIdentity();
            try {
                int policy = getUidPolicy(uid);
                Binder.restoreCallingIdentity(token);
                int i = 3;
                if (policy == 1) {
                    return 3;
                }
                if (!this.mRestrictBackground) {
                    return 1;
                }
                if ((this.mUidPolicy.get(uid) & 4) != 0) {
                    i = 2;
                }
                return i;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
    }

    public boolean getRestrictBackground() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            z = this.mRestrictBackground;
        }
        return z;
    }

    public void setDeviceIdleMode(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        Trace.traceBegin(2097152, "setDeviceIdleMode");
        try {
            synchronized (this.mUidRulesFirstLock) {
                if (this.mDeviceIdleMode != enabled) {
                    this.mDeviceIdleMode = enabled;
                    this.mLogger.deviceIdleModeEnabled(enabled);
                    if (this.mSystemReady) {
                        updateRulesForRestrictPowerUL();
                    }
                } else {
                    return;
                }
            }
            if (enabled) {
                EventLogTags.writeDeviceIdleOnPhase("net");
            } else {
                EventLogTags.writeDeviceIdleOffPhase("net");
            }
            Trace.traceEnd(2097152);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    public void setWifiMeteredOverride(String networkId, int meteredOverride) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            WifiManager wm = (WifiManager) this.mContext.getSystemService(WifiManager.class);
            for (WifiConfiguration config : wm.getConfiguredNetworks()) {
                if (Objects.equals(NetworkPolicyManager.resolveNetworkId(config), networkId)) {
                    config.meteredOverride = meteredOverride;
                    wm.updateNetwork(config);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Deprecated
    public NetworkQuotaInfo getNetworkQuotaInfo(NetworkState state) {
        Log.w(TAG, "Shame on UID " + Binder.getCallingUid() + " for calling the hidden API getNetworkQuotaInfo(). Shame!");
        return new NetworkQuotaInfo();
    }

    private void enforceSubscriptionPlanAccess(int subId, int callingUid, String callingPackage) {
        this.mAppOps.checkPackage(callingUid, callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo si = ((SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class)).getActiveSubscriptionInfo(subId);
            PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(subId);
            if (si == null || !si.isEmbedded() || !si.canManageSubscription(this.mContext, callingPackage)) {
                if (config != null) {
                    String overridePackage = config.getString("config_plans_package_override_string", null);
                    if (!TextUtils.isEmpty(overridePackage) && Objects.equals(overridePackage, callingPackage)) {
                        return;
                    }
                }
                String defaultPackage = this.mCarrierConfigManager.getDefaultCarrierServicePackageName();
                if (TextUtils.isEmpty(defaultPackage) || !Objects.equals(defaultPackage, callingPackage)) {
                    String testPackage = SystemProperties.get("persist.sys.sub_plan_owner." + subId, (String) null);
                    if (TextUtils.isEmpty(testPackage) || !Objects.equals(testPackage, callingPackage)) {
                        String legacyTestPackage = SystemProperties.get("fw.sub_plan_owner." + subId, (String) null);
                        if (TextUtils.isEmpty(legacyTestPackage) || !Objects.equals(legacyTestPackage, callingPackage)) {
                            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_SUBSCRIPTION_PLANS", TAG);
                        }
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public SubscriptionPlan[] getSubscriptionPlans(int subId, String callingPackage) {
        enforceSubscriptionPlanAccess(subId, Binder.getCallingUid(), callingPackage);
        String fake = SystemProperties.get("fw.fake_plan");
        if (!TextUtils.isEmpty(fake)) {
            List<SubscriptionPlan> plans = new ArrayList<>();
            if ("month_hard".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2007-03-14T00:00:00.000Z")).setTitle("G-Mobile").setDataLimit(5368709120L, 1).setDataUsage(1073741824, ZonedDateTime.now().minusHours(36).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2017-03-14T00:00:00.000Z")).setTitle("G-Mobile Happy").setDataLimit(JobStatus.NO_LATEST_RUNTIME, 1).setDataUsage(5368709120L, ZonedDateTime.now().minusHours(36).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2017-03-14T00:00:00.000Z")).setTitle("G-Mobile, Charged after limit").setDataLimit(5368709120L, 1).setDataUsage(5368709120L, ZonedDateTime.now().minusHours(36).toInstant().toEpochMilli()).build());
            } else if ("month_soft".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2007-03-14T00:00:00.000Z")).setTitle("G-Mobile is the carriers name who this plan belongs to").setSummary("Crazy unlimited bandwidth plan with incredibly long title that should be cut off to prevent UI from looking terrible").setDataLimit(5368709120L, 2).setDataUsage(1073741824, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2017-03-14T00:00:00.000Z")).setTitle("G-Mobile, Throttled after limit").setDataLimit(5368709120L, 2).setDataUsage(5368709120L, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2017-03-14T00:00:00.000Z")).setTitle("G-Mobile, No data connection after limit").setDataLimit(5368709120L, 0).setDataUsage(5368709120L, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
            } else if ("month_over".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2007-03-14T00:00:00.000Z")).setTitle("G-Mobile is the carriers name who this plan belongs to").setDataLimit(5368709120L, 2).setDataUsage(6442450944L, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2017-03-14T00:00:00.000Z")).setTitle("G-Mobile, Throttled after limit").setDataLimit(5368709120L, 2).setDataUsage(5368709120L, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2017-03-14T00:00:00.000Z")).setTitle("G-Mobile, No data connection after limit").setDataLimit(5368709120L, 0).setDataUsage(5368709120L, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
            } else if ("month_none".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createRecurringMonthly(ZonedDateTime.parse("2007-03-14T00:00:00.000Z")).setTitle("G-Mobile").build());
            } else if ("prepaid".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createNonrecurring(ZonedDateTime.now().minusDays(20), ZonedDateTime.now().plusDays(10)).setTitle("G-Mobile").setDataLimit(536870912, 0).setDataUsage(104857600, ZonedDateTime.now().minusHours(3).toInstant().toEpochMilli()).build());
            } else if ("prepaid_crazy".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createNonrecurring(ZonedDateTime.now().minusDays(20), ZonedDateTime.now().plusDays(10)).setTitle("G-Mobile Anytime").setDataLimit(536870912, 0).setDataUsage(104857600, ZonedDateTime.now().minusHours(3).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createNonrecurring(ZonedDateTime.now().minusDays(10), ZonedDateTime.now().plusDays(20)).setTitle("G-Mobile Nickel Nights").setSummary("5¢/GB between 1-5AM").setDataLimit(5368709120L, 2).setDataUsage(15728640, ZonedDateTime.now().minusHours(30).toInstant().toEpochMilli()).build());
                plans.add(SubscriptionPlan.Builder.createNonrecurring(ZonedDateTime.now().minusDays(10), ZonedDateTime.now().plusDays(20)).setTitle("G-Mobile Bonus 3G").setSummary("Unlimited 3G data").setDataLimit(1073741824, 2).setDataUsage(314572800, ZonedDateTime.now().minusHours(1).toInstant().toEpochMilli()).build());
            } else if ("unlimited".equals(fake)) {
                plans.add(SubscriptionPlan.Builder.createNonrecurring(ZonedDateTime.now().minusDays(20), ZonedDateTime.now().plusDays(10)).setTitle("G-Mobile Awesome").setDataLimit(JobStatus.NO_LATEST_RUNTIME, 2).setDataUsage(52428800, ZonedDateTime.now().minusHours(3).toInstant().toEpochMilli()).build());
            }
            return (SubscriptionPlan[]) plans.toArray(new SubscriptionPlan[plans.size()]);
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            String ownerPackage = this.mSubscriptionPlansOwner.get(subId);
            if (!Objects.equals(ownerPackage, callingPackage)) {
                if (UserHandle.getCallingAppId() != 1000) {
                    Log.w(TAG, "Not returning plans because caller " + callingPackage + " doesn't match owner " + ownerPackage);
                    return null;
                }
            }
            return this.mSubscriptionPlans.get(subId);
        }
    }

    public void setSubscriptionPlans(int subId, SubscriptionPlan[] plans, String callingPackage) {
        enforceSubscriptionPlanAccess(subId, Binder.getCallingUid(), callingPackage);
        for (SubscriptionPlan plan : plans) {
            Preconditions.checkNotNull(plan);
        }
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mUidRulesFirstLock) {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    this.mSubscriptionPlans.put(subId, plans);
                    this.mSubscriptionPlansOwner.put(subId, callingPackage);
                    String subscriberId = this.mSubIdToSubscriberId.get(subId, null);
                    if (subscriberId != null) {
                        ensureActiveMobilePolicyAL(subId, subscriberId);
                        maybeUpdateMobilePolicyCycleAL(subId, subscriberId);
                    } else {
                        Slog.wtf(TAG, "Missing subscriberId for subId " + subId);
                    }
                    if (LOGD) {
                        Slog.d(TAG, "setSubscriptionPlans, callingPackage: " + callingPackage);
                    }
                    handleNetworkPoliciesUpdateAL(true);
                }
            }
            Intent intent = new Intent("android.telephony.action.SUBSCRIPTION_PLANS_CHANGED");
            intent.addFlags(1073741824);
            intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
            this.mContext.sendBroadcast(intent, "android.permission.MANAGE_SUBSCRIPTION_PLANS");
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSubscriptionPlansOwner(int subId, String packageName) {
        SystemProperties.set("persist.sys.sub_plan_owner." + subId, packageName);
    }

    public String getSubscriptionPlansOwner(int subId) {
        String str;
        if (UserHandle.getCallingAppId() == 1000) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                str = this.mSubscriptionPlansOwner.get(subId);
            }
            return str;
        }
        throw new SecurityException();
    }

    public void setSubscriptionOverride(int subId, int overrideMask, int overrideValue, long timeoutMillis, String callingPackage) {
        enforceSubscriptionPlanAccess(subId, Binder.getCallingUid(), callingPackage);
        synchronized (this.mNetworkPoliciesSecondLock) {
            SubscriptionPlan plan = getPrimarySubscriptionPlanLocked(subId);
            if (plan == null || plan.getDataLimitBehavior() == -1) {
                throw new IllegalStateException("Must provide valid SubscriptionPlan to enable overriding");
            }
        }
        boolean overrideEnabled = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "netpolicy_override_enabled", 1) == 0) {
            overrideEnabled = false;
        }
        if (overrideEnabled || overrideValue == 0) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(16, overrideMask, overrideValue, Integer.valueOf(subId)));
            if (timeoutMillis > 0) {
                Handler handler2 = this.mHandler;
                handler2.sendMessageDelayed(handler2.obtainMessage(16, overrideMask, 0, Integer.valueOf(subId)), timeoutMillis);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter fout = new IndentingPrintWriter(writer, "  ");
            ArraySet<String> argSet = new ArraySet<>(args.length);
            for (String arg : args) {
                argSet.add(arg);
            }
            synchronized (this.mUidRulesFirstLock) {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    if (argSet.contains("--unsnooze")) {
                        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                            this.mNetworkPolicy.valueAt(i).clearSnooze();
                        }
                        if (LOGD) {
                            Slog.d(TAG, "dump()");
                        }
                        handleNetworkPoliciesUpdateAL(true);
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
                    fout.print("Metered ifaces: ");
                    fout.println(String.valueOf(this.mMeteredIfaces));
                    fout.println();
                    fout.println("Network policies:");
                    fout.increaseIndent();
                    for (int i2 = 0; i2 < this.mNetworkPolicy.size(); i2++) {
                        fout.println(this.mNetworkPolicy.valueAt(i2).toString());
                    }
                    fout.decreaseIndent();
                    fout.println();
                    fout.println("Subscription plans:");
                    fout.increaseIndent();
                    for (int i3 = 0; i3 < this.mSubscriptionPlans.size(); i3++) {
                        fout.println("Subscriber ID " + this.mSubscriptionPlans.keyAt(i3) + ":");
                        fout.increaseIndent();
                        SubscriptionPlan[] plans = this.mSubscriptionPlans.valueAt(i3);
                        if (!ArrayUtils.isEmpty(plans)) {
                            for (SubscriptionPlan plan : plans) {
                                fout.println(plan);
                            }
                        }
                        fout.decreaseIndent();
                    }
                    fout.decreaseIndent();
                    fout.println();
                    fout.println("Active subscriptions:");
                    fout.increaseIndent();
                    for (int i4 = 0; i4 < this.mSubIdToSubscriberId.size(); i4++) {
                        fout.println(this.mSubIdToSubscriberId.keyAt(i4) + "=" + NetworkIdentity.scrubSubscriberId(this.mSubIdToSubscriberId.valueAt(i4)));
                    }
                    fout.decreaseIndent();
                    fout.println();
                    fout.println("Merged subscriptions: " + Arrays.toString(NetworkIdentity.scrubSubscriberId(this.mMergedSubscriberIds)));
                    fout.println();
                    fout.println("Policy for UIDs:");
                    fout.increaseIndent();
                    int size = this.mUidPolicy.size();
                    for (int i5 = 0; i5 < size; i5++) {
                        int uid = this.mUidPolicy.keyAt(i5);
                        int policy = this.mUidPolicy.valueAt(i5);
                        fout.print("UID=");
                        fout.print(uid);
                        fout.print(" policy=");
                        fout.print(NetworkPolicyManager.uidPoliciesToString(policy));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    int size2 = this.mPowerSaveWhitelistExceptIdleAppIds.size();
                    if (size2 > 0) {
                        fout.println("Power save whitelist (except idle) app ids:");
                        fout.increaseIndent();
                        for (int i6 = 0; i6 < size2; i6++) {
                            fout.print("UID=");
                            fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i6));
                            fout.print(": ");
                            fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.valueAt(i6));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    int size3 = this.mPowerSaveWhitelistAppIds.size();
                    if (size3 > 0) {
                        fout.println("Power save whitelist app ids:");
                        fout.increaseIndent();
                        for (int i7 = 0; i7 < size3; i7++) {
                            fout.print("UID=");
                            fout.print(this.mPowerSaveWhitelistAppIds.keyAt(i7));
                            fout.print(": ");
                            fout.print(this.mPowerSaveWhitelistAppIds.valueAt(i7));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    int size4 = this.mAppIdleTempWhitelistAppIds.size();
                    if (size4 > 0) {
                        fout.println("App idle whitelist app ids:");
                        fout.increaseIndent();
                        for (int i8 = 0; i8 < size4; i8++) {
                            fout.print("UID=");
                            fout.print(this.mAppIdleTempWhitelistAppIds.keyAt(i8));
                            fout.print(": ");
                            fout.print(this.mAppIdleTempWhitelistAppIds.valueAt(i8));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    int size5 = this.mDefaultRestrictBackgroundWhitelistUids.size();
                    if (size5 > 0) {
                        fout.println("Default restrict background whitelist uids:");
                        fout.increaseIndent();
                        for (int i9 = 0; i9 < size5; i9++) {
                            fout.print("UID=");
                            fout.print(this.mDefaultRestrictBackgroundWhitelistUids.keyAt(i9));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    int size6 = this.mRestrictBackgroundWhitelistRevokedUids.size();
                    if (size6 > 0) {
                        fout.println("Default restrict background whitelist uids revoked by users:");
                        fout.increaseIndent();
                        for (int i10 = 0; i10 < size6; i10++) {
                            fout.print("UID=");
                            fout.print(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i10));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    SparseBooleanArray knownUids = new SparseBooleanArray();
                    collectKeys(this.mUidState, knownUids);
                    collectKeys(this.mUidRules, knownUids);
                    fout.println("Status for all known UIDs:");
                    fout.increaseIndent();
                    int size7 = knownUids.size();
                    for (int i11 = 0; i11 < size7; i11++) {
                        int uid2 = knownUids.keyAt(i11);
                        fout.print("UID=");
                        fout.print(uid2);
                        int state = this.mUidState.get(uid2, 20);
                        fout.print(" state=");
                        fout.print(state);
                        if (state <= 2) {
                            fout.print(" (fg)");
                        } else {
                            fout.print(state <= 6 ? " (fg svc)" : " (bg)");
                        }
                        int uidRules = this.mUidRules.get(uid2, 0);
                        fout.print(" rules=");
                        fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    fout.println("Status for just UIDs with rules:");
                    fout.increaseIndent();
                    int size8 = this.mUidRules.size();
                    for (int i12 = 0; i12 < size8; i12++) {
                        int uid3 = this.mUidRules.keyAt(i12);
                        fout.print("UID=");
                        fout.print(uid3);
                        int uidRules2 = this.mUidRules.get(uid3, 0);
                        fout.print(" rules=");
                        fout.print(NetworkPolicyManager.uidRulesToString(uidRules2));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    fout.println("Admin restricted uids for metered data:");
                    fout.increaseIndent();
                    int size9 = this.mMeteredRestrictedUids.size();
                    for (int i13 = 0; i13 < size9; i13++) {
                        fout.print("u" + this.mMeteredRestrictedUids.keyAt(i13) + ": ");
                        fout.println(this.mMeteredRestrictedUids.valueAt(i13));
                    }
                    fout.decreaseIndent();
                    fout.println();
                    this.mStatLogger.dump(fout);
                    this.mLogger.dumpLogs(fout);
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.net.NetworkPolicyManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new NetworkPolicyManagerShellCommand(this.mContext, this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isUidForeground(int uid) {
        boolean isUidStateForeground;
        synchronized (this.mUidRulesFirstLock) {
            isUidStateForeground = isUidStateForeground(this.mUidState.get(uid, 20));
        }
        return isUidStateForeground;
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private boolean isUidForegroundOnRestrictBackgroundUL(int uid) {
        return NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(this.mUidState.get(uid, 20));
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private boolean isUidForegroundOnRestrictPowerUL(int uid) {
        return NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 20));
    }

    private boolean isUidStateForeground(int state) {
        return state <= 6;
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private boolean updateUidStateUL(int uid, int uidState) {
        Trace.traceBegin(2097152, "updateUidStateUL");
        try {
            int oldUidState = this.mUidState.get(uid, 20);
            if (oldUidState != uidState) {
                this.mUidState.put(uid, uidState);
                updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, uidState);
                if (NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(oldUidState) != NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                    updateRuleForAppIdleUL(uid);
                    if (this.mDeviceIdleMode) {
                        updateRuleForDeviceIdleUL(uid);
                    }
                    if (this.mRestrictPower) {
                        updateRuleForRestrictPowerUL(uid);
                    }
                    updateRulesForPowerRestrictionsUL(uid);
                }
                return true;
            }
            Trace.traceEnd(2097152);
            return false;
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private boolean removeUidStateUL(int uid) {
        int index = this.mUidState.indexOfKey(uid);
        if (index < 0) {
            return false;
        }
        int oldUidState = this.mUidState.valueAt(index);
        this.mUidState.removeAt(index);
        if (oldUidState == 20) {
            return false;
        }
        updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, 20);
        if (this.mDeviceIdleMode) {
            updateRuleForDeviceIdleUL(uid);
        }
        if (this.mRestrictPower) {
            updateRuleForRestrictPowerUL(uid);
        }
        updateRulesForPowerRestrictionsUL(uid);
        return true;
    }

    private void updateNetworkStats(int uid, boolean uidForeground) {
        if (Trace.isTagEnabled(2097152)) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateNetworkStats: ");
            sb.append(uid);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb.append(uidForeground ? "F" : "B");
            Trace.traceBegin(2097152, sb.toString());
        }
        try {
            this.mNetworkStats.setUidForeground(uid, uidForeground);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void updateRestrictBackgroundRulesOnUidStatusChangedUL(int uid, int oldUidState, int newUidState) {
        if (NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(oldUidState) != NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(newUidState)) {
            updateRulesForDataUsageRestrictionsUL(uid);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRulesForPowerSaveUL() {
        Trace.traceBegin(2097152, "updateRulesForPowerSaveUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mRestrictPower, 3, this.mUidFirewallPowerSaveRules);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRuleForRestrictPowerUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mRestrictPower, 3);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRulesForDeviceIdleUL() {
        Trace.traceBegin(2097152, "updateRulesForDeviceIdleUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mDeviceIdleMode, 1, this.mUidFirewallDozableRules);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRuleForDeviceIdleUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mDeviceIdleMode, 1);
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForWhitelistedPowerSaveUL(boolean enabled, int chain, SparseIntArray rules) {
        if (enabled) {
            rules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                UserInfo user = users.get(ui);
                updateRulesForWhitelistedAppIds(rules, this.mPowerSaveTempWhitelistAppIds, user.id);
                updateRulesForWhitelistedAppIds(rules, this.mPowerSaveWhitelistAppIds, user.id);
                if (chain == 3) {
                    updateRulesForWhitelistedAppIds(rules, this.mPowerSaveWhitelistExceptIdleAppIds, user.id);
                }
            }
            for (int i = this.mUidState.size() - 1; i >= 0; i--) {
                if (NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.valueAt(i))) {
                    rules.put(this.mUidState.keyAt(i), 1);
                }
            }
            setUidFirewallRulesUL(chain, rules, 1);
            return;
        }
        setUidFirewallRulesUL(chain, null, 2);
    }

    private void updateRulesForWhitelistedAppIds(SparseIntArray uidRules, SparseBooleanArray whitelistedAppIds, int userId) {
        for (int i = whitelistedAppIds.size() - 1; i >= 0; i--) {
            if (whitelistedAppIds.valueAt(i)) {
                uidRules.put(UserHandle.getUid(userId, whitelistedAppIds.keyAt(i)), 1);
            }
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private boolean isWhitelistedFromPowerSaveUL(int uid, boolean deviceIdleMode) {
        int appId = UserHandle.getAppId(uid);
        boolean isWhitelisted = false;
        boolean isWhitelisted2 = this.mPowerSaveTempWhitelistAppIds.get(appId) || this.mPowerSaveWhitelistAppIds.get(appId);
        if (deviceIdleMode) {
            return isWhitelisted2;
        }
        if (isWhitelisted2 || this.mPowerSaveWhitelistExceptIdleAppIds.get(appId)) {
            isWhitelisted = true;
        }
        return isWhitelisted;
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForWhitelistedPowerSaveUL(int uid, boolean enabled, int chain) {
        if (enabled) {
            if (isWhitelistedFromPowerSaveUL(uid, chain == 1) || isUidForegroundOnRestrictPowerUL(uid)) {
                setUidFirewallRule(chain, uid, 1);
            } else {
                setUidFirewallRule(chain, uid, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRulesForAppIdleUL() {
        Trace.traceBegin(2097152, "updateRulesForAppIdleUL");
        try {
            SparseIntArray uidRules = this.mUidFirewallStandbyRules;
            uidRules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                int[] idleUids = this.mUsageStats.getIdleUidsForUser(users.get(ui).id);
                for (int uid : idleUids) {
                    if (!this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid), false) && hasInternetPermissions(uid)) {
                        uidRules.put(uid, 2);
                    }
                }
            }
            setUidFirewallRulesUL(2, uidRules, 0);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRuleForAppIdleUL(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            if (Trace.isTagEnabled(2097152)) {
                Trace.traceBegin(2097152, "updateRuleForAppIdleUL: " + uid);
            }
            try {
                if (this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid)) || !isUidIdle(uid) || isUidForegroundOnRestrictPowerUL(uid)) {
                    setUidFirewallRule(2, uid, 0);
                    if (LOGD) {
                        Log.d(TAG, "updateRuleForAppIdleUL " + uid + " to DEFAULT");
                    }
                } else {
                    setUidFirewallRule(2, uid, 2);
                    if (LOGD) {
                        Log.d(TAG, "updateRuleForAppIdleUL DENY " + uid);
                    }
                }
            } finally {
                Trace.traceEnd(2097152);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    public void updateRulesForAppIdleParoleUL() {
        boolean paroled = this.mUsageStats.isAppIdleParoleOn();
        boolean enableChain = !paroled;
        enableFirewallChainUL(2, enableChain);
        int ruleCount = this.mUidFirewallStandbyRules.size();
        for (int i = 0; i < ruleCount; i++) {
            int uid = this.mUidFirewallStandbyRules.keyAt(i);
            int oldRules = this.mUidRules.get(uid);
            if (enableChain) {
                oldRules &= 15;
            } else if ((oldRules & 240) == 0) {
            }
            int newUidRules = updateRulesForPowerRestrictionsUL(uid, oldRules, paroled);
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock", "mNetworkPoliciesSecondLock"})
    private void updateRulesForGlobalChangeAL(boolean restrictedNetworksChanged) {
        if (Trace.isTagEnabled(2097152)) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateRulesForGlobalChangeAL: ");
            sb.append(restrictedNetworksChanged ? "R" : "-");
            Trace.traceBegin(2097152, sb.toString());
        }
        try {
            updateRulesForAppIdleUL();
            updateRulesForRestrictPowerUL();
            updateRulesForRestrictBackgroundUL();
            if (restrictedNetworksChanged) {
                if (LOGD) {
                    Slog.d(TAG, "updateRulesForGlobalChangeAL restrictedNetworksChanged");
                }
                normalizePoliciesNL();
                updateNetworkRulesNL();
                syncMeteredIfacesToReplicaNL();
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForRestrictPowerUL() {
        Trace.traceBegin(2097152, "updateRulesForRestrictPowerUL");
        try {
            updateRulesForDeviceIdleUL();
            updateRulesForPowerSaveUL();
            updateRulesForAllAppsUL(2);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForRestrictBackgroundUL() {
        Trace.traceBegin(2097152, "updateRulesForRestrictBackgroundUL");
        try {
            updateRulesForAllAppsUL(1);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForAllAppsUL(int type) {
        if (Trace.isTagEnabled(2097152)) {
            Trace.traceBegin(2097152, "updateRulesForRestrictPowerUL-" + type);
        }
        try {
            PackageManager pm = this.mContext.getPackageManager();
            Trace.traceBegin(2097152, "list-users");
            try {
                List<UserInfo> users = this.mUserManager.getUsers();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "list-uids");
                try {
                    List<ApplicationInfo> apps = pm.getInstalledApplications(4981248);
                    Trace.traceEnd(2097152);
                    int usersSize = users.size();
                    int appsSize = apps.size();
                    for (int i = 0; i < usersSize; i++) {
                        UserInfo user = users.get(i);
                        for (int j = 0; j < appsSize; j++) {
                            int uid = UserHandle.getUid(user.id, apps.get(j).uid);
                            if (type == 1) {
                                updateRulesForDataUsageRestrictionsUL(uid);
                            } else if (type != 2) {
                                Slog.w(TAG, "Invalid type for updateRulesForAllApps: " + type);
                            } else {
                                updateRulesForPowerRestrictionsUL(uid);
                            }
                        }
                    }
                } finally {
                    Trace.traceEnd(2097152);
                }
            } finally {
                Trace.traceEnd(2097152);
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForTempWhitelistChangeUL(int appId) {
        List<UserInfo> users = this.mUserManager.getUsers();
        int numUsers = users.size();
        for (int i = 0; i < numUsers; i++) {
            int uid = UserHandle.getUid(users.get(i).id, appId);
            updateRuleForAppIdleUL(uid);
            updateRuleForDeviceIdleUL(uid);
            updateRuleForRestrictPowerUL(uid);
            updateRulesForPowerRestrictionsUL(uid);
        }
    }

    private boolean isUidValidForBlacklistRules(int uid) {
        if (uid == 1013 || uid == 1019) {
            return true;
        }
        if (!UserHandle.isApp(uid) || !hasInternetPermissions(uid)) {
            return false;
        }
        return true;
    }

    private boolean isUidValidForWhitelistRules(int uid) {
        return UserHandle.isApp(uid) && hasInternetPermissions(uid);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAppIdleWhitelist(int uid, boolean shouldWhitelist) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            if (this.mAppIdleTempWhitelistAppIds.get(uid) != shouldWhitelist) {
                long token = Binder.clearCallingIdentity();
                try {
                    this.mLogger.appIdleWlChanged(uid, shouldWhitelist);
                    if (shouldWhitelist) {
                        this.mAppIdleTempWhitelistAppIds.put(uid, true);
                    } else {
                        this.mAppIdleTempWhitelistAppIds.delete(uid);
                    }
                    updateRuleForAppIdleUL(uid);
                    updateRulesForPowerRestrictionsUL(uid);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int[] getAppIdleWhitelist() {
        int[] uids;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            int len = this.mAppIdleTempWhitelistAppIds.size();
            uids = new int[len];
            for (int i = 0; i < len; i++) {
                uids[i] = this.mAppIdleTempWhitelistAppIds.keyAt(i);
            }
        }
        return uids;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isUidIdle(int uid) {
        synchronized (this.mUidRulesFirstLock) {
            if (this.mAppIdleTempWhitelistAppIds.get(uid)) {
                return false;
            }
        }
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        int userId = UserHandle.getUserId(uid);
        if (packages == null) {
            return true;
        }
        for (String packageName : packages) {
            if (!this.mUsageStats.isAppIdle(packageName, uid, userId)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasInternetPermissions(int uid) {
        try {
            if (this.mIPm.checkUidPermission("android.permission.INTERNET", uid) != 0) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock"})
    private void onUidDeletedUL(int uid) {
        this.mUidRules.delete(uid);
        this.mUidPolicy.delete(uid);
        this.mUidFirewallStandbyRules.delete(uid);
        this.mUidFirewallDozableRules.delete(uid);
        this.mUidFirewallPowerSaveRules.delete(uid);
        this.mPowerSaveWhitelistExceptIdleAppIds.delete(uid);
        this.mPowerSaveWhitelistAppIds.delete(uid);
        this.mPowerSaveTempWhitelistAppIds.delete(uid);
        this.mAppIdleTempWhitelistAppIds.delete(uid);
        this.mHandler.obtainMessage(15, uid, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRestrictionRulesForUidUL(int uid) {
        updateRuleForDeviceIdleUL(uid);
        updateRuleForAppIdleUL(uid);
        updateRuleForRestrictPowerUL(uid);
        updateRulesForPowerRestrictionsUL(uid);
        updateRulesForDataUsageRestrictionsUL(uid);
    }

    private void updateRulesForDataUsageRestrictionsUL(int uid) {
        if (Trace.isTagEnabled(2097152)) {
            Trace.traceBegin(2097152, "updateRulesForDataUsageRestrictionsUL: " + uid);
        }
        try {
            updateRulesForDataUsageRestrictionsULInner(uid);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void updateRulesForDataUsageRestrictionsULInner(int uid) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        int newUidRules;
        if (isUidValidForWhitelistRules(uid)) {
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictBackgroundUL(uid);
            boolean isRestrictedByAdmin = isRestrictedByAdminUL(uid);
            boolean isBlacklisted = (uidPolicy & 1) != 0;
            boolean isWhitelisted = (uidPolicy & 4) != 0;
            int oldRule = oldUidRules & 15;
            int newRule = 0;
            if (isRestrictedByAdmin) {
                newRule = 4;
            } else if (isForeground) {
                if (isBlacklisted || (this.mRestrictBackground && !isWhitelisted)) {
                    newRule = 2;
                } else if (isWhitelisted) {
                    newRule = 1;
                }
            } else if (isBlacklisted) {
                newRule = 4;
            } else if (this.mRestrictBackground && isWhitelisted) {
                newRule = 1;
            }
            int newUidRules2 = (oldUidRules & 240) | newRule;
            if (LOGV) {
                StringBuilder sb = new StringBuilder();
                sb.append("updateRuleForRestrictBackgroundUL(");
                sb.append(uid);
                sb.append("): isForeground=");
                sb.append(isForeground);
                sb.append(", isBlacklisted=");
                sb.append(isBlacklisted);
                sb.append(", isWhitelisted=");
                sb.append(isWhitelisted);
                sb.append(", isRestrictedByAdmin=");
                sb.append(isRestrictedByAdmin);
                sb.append(", oldRule=");
                str = "): isForeground=";
                sb.append(NetworkPolicyManager.uidRulesToString(oldRule));
                sb.append(", newRule=");
                sb.append(NetworkPolicyManager.uidRulesToString(newRule));
                sb.append(", newUidRules=");
                sb.append(NetworkPolicyManager.uidRulesToString(newUidRules2));
                sb.append(", oldUidRules=");
                sb.append(NetworkPolicyManager.uidRulesToString(oldUidRules));
                String sb2 = sb.toString();
                str2 = TAG;
                Log.v(str2, sb2);
            } else {
                str = "): isForeground=";
                str2 = TAG;
            }
            if (newUidRules2 == 0) {
                str4 = str2;
                str5 = str;
                str3 = ", newUidRules=";
                this.mUidRules.delete(uid);
                newUidRules = newUidRules2;
            } else {
                str4 = str2;
                str5 = str;
                str3 = ", newUidRules=";
                newUidRules = newUidRules2;
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                Log.i(str4, "updateRuleForRestrictBackgroundUL(" + uid + str5 + isForeground + ", isBlacklisted=" + isBlacklisted + ", isWhitelisted=" + isWhitelisted + ", isRestrictedByAdmin=" + isRestrictedByAdmin + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + str3 + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules) + ", mRestrictBackground=" + this.mRestrictBackground);
                if (hasRule(newRule, 2)) {
                    setMeteredNetworkWhitelist(uid, true);
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, false);
                    }
                } else {
                    boolean z = false;
                    if (hasRule(oldRule, 2)) {
                        if (!isWhitelisted) {
                            setMeteredNetworkWhitelist(uid, false);
                        }
                        if (isBlacklisted || isRestrictedByAdmin) {
                            setMeteredNetworkBlacklist(uid, true);
                        }
                    } else if (hasRule(newRule, 4) || hasRule(oldRule, 4)) {
                        if (isBlacklisted || isRestrictedByAdmin) {
                            z = true;
                        }
                        setMeteredNetworkBlacklist(uid, z);
                        if (hasRule(oldRule, 4) && isWhitelisted) {
                            setMeteredNetworkWhitelist(uid, isWhitelisted);
                        }
                    } else if (hasRule(newRule, 1) || hasRule(oldRule, 1)) {
                        setMeteredNetworkWhitelist(uid, isWhitelisted);
                    } else {
                        Log.wtf(str4, "Unexpected change of metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", blacklisted=" + isBlacklisted + ", isRestrictedByAdmin=" + isRestrictedByAdmin + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                    }
                }
                this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
            }
        } else if (LOGD) {
            Slog.d(TAG, "no need to update restrict data rules for uid " + uid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mUidRulesFirstLock"})
    private void updateRulesForPowerRestrictionsUL(int uid) {
        int newUidRules = updateRulesForPowerRestrictionsUL(uid, this.mUidRules.get(uid, 0), false);
        if (newUidRules == 0) {
            this.mUidRules.delete(uid);
        } else {
            this.mUidRules.put(uid, newUidRules);
        }
    }

    private int updateRulesForPowerRestrictionsUL(int uid, int oldUidRules, boolean paroled) {
        if (Trace.isTagEnabled(2097152)) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateRulesForPowerRestrictionsUL: ");
            sb.append(uid);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb.append(oldUidRules);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb.append(paroled ? "P" : "-");
            Trace.traceBegin(2097152, sb.toString());
        }
        try {
            return updateRulesForPowerRestrictionsULInner(uid, oldUidRules, paroled);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private int updateRulesForPowerRestrictionsULInner(int uid, int oldUidRules, boolean paroled) {
        if (!isUidValidForBlacklistRules(uid)) {
            if (LOGD) {
                Slog.d(TAG, "no need to update restrict power rules for uid " + uid);
            }
            return 0;
        }
        boolean isIdle = !paroled && isUidIdle(uid);
        boolean restrictMode = isIdle || this.mRestrictPower || this.mDeviceIdleMode;
        boolean isForeground = isUidForegroundOnRestrictPowerUL(uid);
        boolean isWhitelisted = isWhitelistedFromPowerSaveUL(uid, this.mDeviceIdleMode);
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
        if (LOGV) {
            Log.v(TAG, "updateRulesForPowerRestrictionsUL(" + uid + "), isIdle: " + isIdle + ", mRestrictPower: " + this.mRestrictPower + ", mDeviceIdleMode: " + this.mDeviceIdleMode + ", isForeground=" + isForeground + ", isWhitelisted=" + isWhitelisted + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
        }
        if (newRule != oldRule) {
            StringBuilder sb = new StringBuilder();
            sb.append("upPwResUL:");
            sb.append(uid);
            sb.append(",idle:");
            sb.append(isIdle ? 1 : 0);
            sb.append(",resPw:");
            sb.append(this.mRestrictPower ? 1 : 0);
            sb.append(",dIdle:");
            sb.append(this.mDeviceIdleMode ? 1 : 0);
            sb.append(",fg=");
            sb.append(isForeground ? 1 : 0);
            sb.append(",white=");
            sb.append(isWhitelisted ? 1 : 0);
            sb.append(",old=");
            sb.append(oldRule);
            sb.append(",new=");
            sb.append(newRule);
            sb.append(",newUid=");
            sb.append(newUidRules);
            sb.append(",oldUid=");
            sb.append(oldUidRules);
            sb.append(",resBg=");
            sb.append(this.mRestrictBackground ? 1 : 0);
            Log.i(TAG, sb.toString());
            if (newRule == 0 || hasRule(newRule, 32)) {
                if (LOGV) {
                    Log.v(TAG, "Allowing non-metered access for UID " + uid);
                }
            } else if (!hasRule(newRule, 64)) {
                Log.wtf(TAG, "Unexpected change of non-metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
            } else if (LOGV) {
                Log.v(TAG, "Rejecting non-metered access for UID " + uid);
            }
            this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
        }
        return newUidRules;
    }

    /* access modifiers changed from: private */
    public class AppIdleStateChangeListener extends UsageStatsManagerInternal.AppIdleStateChangeListener {
        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            try {
                int uid = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackageUidAsUser(packageName, 8192, userId);
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.mLogger.appIdleStateChanged(uid, idle);
                    NetworkPolicyManagerService.this.updateRuleForAppIdleUL(uid);
                    NetworkPolicyManagerService.this.updateRulesForPowerRestrictionsUL(uid);
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                NetworkPolicyManagerService.this.mLogger.paroleStateChanged(isParoleOn);
                NetworkPolicyManagerService.this.updateRulesForAppIdleParoleUL();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUidRulesChanged(INetworkPolicyListener listener, int uid, int uidRules) {
        if (listener != null) {
            try {
                listener.onUidRulesChanged(uid, uidRules);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchMeteredIfacesChanged(INetworkPolicyListener listener, String[] meteredIfaces) {
        if (listener != null) {
            try {
                listener.onMeteredIfacesChanged(meteredIfaces);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchRestrictBackgroundChanged(INetworkPolicyListener listener, boolean restrictBackground) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundChanged(restrictBackground);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUidPoliciesChanged(INetworkPolicyListener listener, int uid, int uidPolicies) {
        if (listener != null) {
            try {
                listener.onUidPoliciesChanged(uid, uidPolicies);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchSubscriptionOverride(INetworkPolicyListener listener, int subId, int overrideMask, int overrideValue) {
        if (listener != null) {
            try {
                listener.onSubscriptionOverride(subId, overrideMask, overrideValue);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidChanged(int uid, int procState, long procStateSeq) {
        boolean updated;
        Trace.traceBegin(2097152, "onUidStateChanged");
        try {
            synchronized (this.mUidRulesFirstLock) {
                this.mLogger.uidStateChanged(uid, procState, procStateSeq);
                updated = updateUidStateUL(uid, procState);
                this.mActivityManagerInternal.notifyNetworkPolicyRulesUpdated(uid, procStateSeq);
            }
            if (updated) {
                updateNetworkStats(uid, isUidStateForeground(procState));
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidGone(int uid) {
        boolean updated;
        Trace.traceBegin(2097152, "onUidGone");
        try {
            synchronized (this.mUidRulesFirstLock) {
                updated = removeUidStateUL(uid);
            }
            if (updated) {
                updateNetworkStats(uid, false);
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void broadcastRestrictBackgroundChanged(int uid, Boolean changed) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages != null) {
            int userId = UserHandle.getUserId(uid);
            for (String packageName : packages) {
                Intent intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                intent.setPackage(packageName);
                intent.setFlags(1073741824);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
            }
        }
    }

    private void setInterfaceQuotaAsync(String iface, long quotaBytes) {
        this.mHandler.obtainMessage(10, (int) (quotaBytes >> 32), (int) (-1 & quotaBytes), iface).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setInterfaceQuota(String iface, long quotaBytes) {
        try {
            this.mNetworkManager.setInterfaceQuota(iface, quotaBytes);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void removeInterfaceQuotaAsync(String iface) {
        this.mHandler.obtainMessage(11, iface).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeInterfaceQuota(String iface) {
        try {
            this.mNetworkManager.removeInterfaceQuota(iface);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem removing interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkBlacklist(int uid, boolean enable) {
        if (LOGV) {
            Slog.v(TAG, "setMeteredNetworkBlacklist " + uid + ": " + enable);
        }
        try {
            this.mNetworkManager.setUidMeteredNetworkBlacklist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting blacklist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkWhitelist(int uid, boolean enable) {
        if (LOGV) {
            Slog.v(TAG, "setMeteredNetworkWhitelist " + uid + ": " + enable);
        }
        try {
            this.mNetworkManager.setUidMeteredNetworkWhitelist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting whitelist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void setUidFirewallRulesUL(int chain, SparseIntArray uidRules, int toggle) {
        if (uidRules != null) {
            setUidFirewallRulesUL(chain, uidRules);
        }
        if (toggle != 0) {
            boolean z = true;
            if (toggle != 1) {
                z = false;
            }
            enableFirewallChainUL(chain, z);
        }
    }

    private void setUidFirewallRulesUL(int chain, SparseIntArray uidRules) {
        try {
            int size = uidRules.size();
            int[] uids = new int[size];
            int[] rules = new int[size];
            for (int index = size - 1; index >= 0; index--) {
                uids[index] = uidRules.keyAt(index);
                rules[index] = uidRules.valueAt(index);
            }
            this.mNetworkManager.setFirewallUidRules(chain, uids, rules);
            this.mLogger.firewallRulesChanged(chain, uids, rules);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRule(int chain, int uid, int rule) {
        if (Trace.isTagEnabled(2097152)) {
            Trace.traceBegin(2097152, "setUidFirewallRule: " + chain + SliceClientPermissions.SliceAuthority.DELIMITER + uid + SliceClientPermissions.SliceAuthority.DELIMITER + rule);
        }
        if (chain == 1) {
            try {
                this.mUidFirewallDozableRules.put(uid, rule);
            } catch (Throwable th) {
                Trace.traceEnd(2097152);
                throw th;
            }
        } else if (chain == 2) {
            this.mUidFirewallStandbyRules.put(uid, rule);
        } else if (chain == 3) {
            this.mUidFirewallPowerSaveRules.put(uid, rule);
        }
        try {
            this.mNetworkManager.setFirewallUidRule(chain, uid, rule);
            this.mLogger.uidFirewallRuleChanged(chain, uid, rule);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
        Trace.traceEnd(2097152);
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private void enableFirewallChainUL(int chain, boolean enable) {
        if (this.mFirewallChainStates.indexOfKey(chain) < 0 || this.mFirewallChainStates.get(chain) != enable) {
            this.mFirewallChainStates.put(chain, enable);
            try {
                this.mNetworkManager.setFirewallChainEnabled(chain, enable);
                this.mLogger.firewallChainEnabled(chain, enable);
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem enable firewall chain", e);
            } catch (RemoteException e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetUidFirewallRules(int uid) {
        try {
            this.mNetworkManager.setFirewallUidRule(1, uid, 0);
            this.mNetworkManager.setFirewallUidRule(2, uid, 0);
            this.mNetworkManager.setFirewallUidRule(3, uid, 0);
            this.mNetworkManager.setUidMeteredNetworkWhitelist(uid, false);
            this.mNetworkManager.setUidMeteredNetworkBlacklist(uid, false);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem resetting firewall uid rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    @Deprecated
    private long getTotalBytes(NetworkTemplate template, long start, long end) {
        return getNetworkTotalBytes(template, start, end);
    }

    private long getNetworkTotalBytes(NetworkTemplate template, long start, long end) {
        try {
            return this.mNetworkStats.getNetworkTotalBytes(template, start, end);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failed to read network stats: " + e);
            return 0;
        }
    }

    private NetworkStats getNetworkUidBytes(NetworkTemplate template, long start, long end) {
        try {
            return this.mNetworkStats.getNetworkUidBytes(template, start, end);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failed to read network stats: " + e);
            return new NetworkStats(SystemClock.elapsedRealtime(), 0);
        }
    }

    private boolean isBandwidthControlEnabled() {
        long token = Binder.clearCallingIdentity();
        try {
            return this.mNetworkManager.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static Intent buildAllowBackgroundDataIntent() {
        return new Intent(ACTION_ALLOW_BACKGROUND);
    }

    private static Intent buildSnoozeWarningIntent(NetworkTemplate template) {
        Intent intent = new Intent(ACTION_SNOOZE_WARNING);
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", (Parcelable) template);
        return intent;
    }

    private static Intent buildSnoozeRapidIntent(NetworkTemplate template) {
        Intent intent = new Intent(ACTION_SNOOZE_RAPID);
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", (Parcelable) template);
        return intent;
    }

    private static Intent buildNetworkOverLimitIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039870)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", (Parcelable) template);
        return intent;
    }

    private static Intent buildViewDataUsageIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039804)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", (Parcelable) template);
        return intent;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addIdleHandler(MessageQueue.IdleHandler handler) {
        this.mHandler.getLooper().getQueue().addIdleHandler(handler);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mUidRulesFirstLock"})
    @VisibleForTesting
    public void updateRestrictBackgroundByLowPowerModeUL(PowerSaveState result) {
        boolean shouldInvokeRestrictBackground;
        this.mRestrictBackgroundPowerState = result;
        boolean restrictBackground = result.batterySaverEnabled;
        boolean localRestrictBgChangedInBsm = this.mRestrictBackgroundChangedInBsm;
        boolean z = true;
        if (result.globalBatterySaverEnabled) {
            if (this.mRestrictBackground || !result.batterySaverEnabled) {
                z = false;
            }
            shouldInvokeRestrictBackground = z;
            this.mRestrictBackgroundBeforeBsm = this.mRestrictBackground;
            localRestrictBgChangedInBsm = false;
        } else {
            shouldInvokeRestrictBackground = !this.mRestrictBackgroundChangedInBsm;
            restrictBackground = this.mRestrictBackgroundBeforeBsm;
        }
        if (shouldInvokeRestrictBackground) {
            setRestrictBackgroundUL(restrictBackground);
        }
        this.mRestrictBackgroundChangedInBsm = localRestrictBgChangedInBsm;
    }

    private static void collectKeys(SparseIntArray source, SparseBooleanArray target) {
        int size = source.size();
        for (int i = 0; i < size; i++) {
            target.put(source.keyAt(i), true);
        }
    }

    public void factoryReset(String subscriber) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            NetworkPolicy[] policies = getNetworkPolicies(this.mContext.getOpPackageName());
            NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(subscriber);
            for (NetworkPolicy policy : policies) {
                if (policy.template.equals(template)) {
                    policy.limitBytes = -1;
                    policy.inferred = false;
                    policy.clearSnooze();
                }
            }
            if (LOGD) {
                Slog.d(TAG, "factoryReset");
            }
            setNetworkPolicies(policies);
            setRestrictBackground(false);
            if (!this.mUserManager.hasUserRestriction("no_control_apps")) {
                for (int uid : getUidsWithPolicy(1)) {
                    setUidPolicy(uid, 0);
                }
            }
        }
    }

    public boolean isUidNetworkingBlocked(int uid, boolean isNetworkMetered) {
        int uidRules;
        boolean isBackgroundRestricted;
        long startTime = this.mStatLogger.getTime();
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            uidRules = this.mUidRules.get(uid, 0);
            isBackgroundRestricted = this.mRestrictBackground;
        }
        boolean ret = isUidNetworkingBlockedInternal(uid, uidRules, isNetworkMetered, isBackgroundRestricted, this.mLogger);
        this.mStatLogger.logDurationStat(1, startTime);
        return ret;
    }

    private static boolean isSystem(int uid) {
        return uid < 10000;
    }

    static boolean isUidNetworkingBlockedInternal(int uid, int uidRules, boolean isNetworkMetered, boolean isBackgroundRestricted, NetworkPolicyLogger logger) {
        int reason;
        boolean blocked;
        if (isSystem(uid)) {
            reason = 7;
        } else if (hasRule(uidRules, 64)) {
            reason = 0;
        } else if (!isNetworkMetered) {
            reason = 1;
        } else if (hasRule(uidRules, 4)) {
            reason = 2;
        } else if (hasRule(uidRules, 1)) {
            reason = 3;
        } else if (hasRule(uidRules, 2)) {
            reason = 4;
        } else if (isBackgroundRestricted) {
            reason = 5;
        } else {
            reason = 6;
        }
        switch (reason) {
            case 0:
            case 2:
            case 5:
                blocked = true;
                break;
            case 1:
            case 3:
            case 4:
            case 6:
            case 7:
                blocked = false;
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (logger != null) {
            logger.networkBlocked(uid, reason);
        }
        return blocked;
    }

    private class NetworkPolicyManagerInternalImpl extends NetworkPolicyManagerInternal {
        private NetworkPolicyManagerInternalImpl() {
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public void resetUserState(int userId) {
            if (NetworkPolicyManagerService.LOGD) {
                Slog.d(NetworkPolicyManagerService.TAG, "resetUserState userId: " + userId);
            }
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                boolean changed = false;
                boolean changed2 = NetworkPolicyManagerService.this.removeUserStateUL(userId, false);
                if (NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId) || changed2) {
                    changed = true;
                }
                if (changed) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.writePolicyAL();
                    }
                }
            }
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public boolean isUidRestrictedOnMeteredNetworks(int uid) {
            int uidRules;
            boolean isBackgroundRestricted;
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                uidRules = NetworkPolicyManagerService.this.mUidRules.get(uid, 32);
                isBackgroundRestricted = NetworkPolicyManagerService.this.mRestrictBackground;
            }
            if (!isBackgroundRestricted || NetworkPolicyManagerService.hasRule(uidRules, 1) || NetworkPolicyManagerService.hasRule(uidRules, 2)) {
                return false;
            }
            return true;
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public boolean isUidNetworkingBlocked(int uid, String ifname) {
            int uidRules;
            boolean isBackgroundRestricted;
            boolean isNetworkMetered;
            long startTime = NetworkPolicyManagerService.this.mStatLogger.getTime();
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                uidRules = NetworkPolicyManagerService.this.mUidRules.get(uid, 0);
                isBackgroundRestricted = NetworkPolicyManagerService.this.mRestrictBackground;
            }
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesReplicaSecondLock) {
                isNetworkMetered = NetworkPolicyManagerService.this.mMeteredIfacesReplica.contains(ifname);
            }
            boolean ret = NetworkPolicyManagerService.isUidNetworkingBlockedInternal(uid, uidRules, isNetworkMetered, isBackgroundRestricted, NetworkPolicyManagerService.this.mLogger);
            NetworkPolicyManagerService.this.mStatLogger.logDurationStat(1, startTime);
            return ret;
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public void onTempPowerSaveWhitelistChange(int appId, boolean added) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                NetworkPolicyManagerService.this.mLogger.tempPowerSaveWlChanged(appId, added);
                if (added) {
                    NetworkPolicyManagerService.this.mPowerSaveTempWhitelistAppIds.put(appId, true);
                } else {
                    NetworkPolicyManagerService.this.mPowerSaveTempWhitelistAppIds.delete(appId);
                }
                NetworkPolicyManagerService.this.updateRulesForTempWhitelistChangeUL(appId);
            }
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public SubscriptionPlan getSubscriptionPlan(Network network) {
            SubscriptionPlan primarySubscriptionPlanLocked;
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                primarySubscriptionPlanLocked = NetworkPolicyManagerService.this.getPrimarySubscriptionPlanLocked(NetworkPolicyManagerService.this.getSubIdLocked(network));
            }
            return primarySubscriptionPlanLocked;
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public SubscriptionPlan getSubscriptionPlan(NetworkTemplate template) {
            SubscriptionPlan primarySubscriptionPlanLocked;
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                primarySubscriptionPlanLocked = NetworkPolicyManagerService.this.getPrimarySubscriptionPlanLocked(NetworkPolicyManagerService.this.findRelevantSubIdNL(template));
            }
            return primarySubscriptionPlanLocked;
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public long getSubscriptionOpportunisticQuota(Network network, int quotaType) {
            long quotaBytes;
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                quotaBytes = NetworkPolicyManagerService.this.mSubscriptionOpportunisticQuota.get(NetworkPolicyManagerService.this.getSubIdLocked(network), -1);
            }
            if (quotaBytes == -1) {
                return -1;
            }
            if (quotaType == 1) {
                return (long) (((float) quotaBytes) * Settings.Global.getFloat(NetworkPolicyManagerService.this.mContext.getContentResolver(), "netpolicy_quota_frac_jobs", 0.5f));
            }
            if (quotaType == 2) {
                return (long) (((float) quotaBytes) * Settings.Global.getFloat(NetworkPolicyManagerService.this.mContext.getContentResolver(), "netpolicy_quota_frac_multipath", 0.5f));
            }
            return -1;
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public void onAdminDataAvailable() {
            NetworkPolicyManagerService.this.mAdminDataAvailableLatch.countDown();
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public void setAppIdleWhitelist(int uid, boolean shouldWhitelist) {
            NetworkPolicyManagerService.this.setAppIdleWhitelist(uid, shouldWhitelist);
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public void setMeteredRestrictedPackages(Set<String> packageNames, int userId) {
            NetworkPolicyManagerService.this.setMeteredRestrictedPackagesInternal(packageNames, userId);
        }

        @Override // com.android.server.net.NetworkPolicyManagerInternal
        public void setMeteredRestrictedPackagesAsync(Set<String> packageNames, int userId) {
            NetworkPolicyManagerService.this.mHandler.obtainMessage(17, userId, 0, packageNames).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void syncMeteredIfacesToReplicaNL() {
        if (!this.mMeteredIfaces.equals(this.mMeteredIfacesReplica)) {
            synchronized (this.mNetworkPoliciesReplicaSecondLock) {
                this.mMeteredIfacesReplica.clear();
                this.mMeteredIfacesReplica.addAll((ArraySet<? extends String>) this.mMeteredIfaces);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMeteredRestrictedPackagesInternal(Set<String> packageNames, int userId) {
        synchronized (this.mUidRulesFirstLock) {
            Set<Integer> newRestrictedUids = new ArraySet<>();
            for (String packageName : packageNames) {
                int uid = getUidForPackage(packageName, userId);
                if (uid >= 0) {
                    newRestrictedUids.add(Integer.valueOf(uid));
                }
            }
            this.mMeteredRestrictedUids.put(userId, newRestrictedUids);
            handleRestrictedPackagesChangeUL(this.mMeteredRestrictedUids.get(userId), newRestrictedUids);
            this.mLogger.meteredRestrictedPkgsChanged(newRestrictedUids);
        }
    }

    private int getUidForPackage(String packageName, int userId) {
        try {
            return this.mContext.getPackageManager().getPackageUidAsUser(packageName, 4202496, userId);
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    private int parseSubId(NetworkState state) {
        if (state == null || state.networkCapabilities == null || !state.networkCapabilities.hasTransport(0)) {
            return -1;
        }
        NetworkSpecifier spec = state.networkCapabilities.getNetworkSpecifier();
        if (!(spec instanceof StringNetworkSpecifier)) {
            return -1;
        }
        try {
            return Integer.parseInt(((StringNetworkSpecifier) spec).specifier);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private int getSubIdLocked(Network network) {
        return this.mNetIdToSubId.get(network.netId, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    private SubscriptionPlan getPrimarySubscriptionPlanLocked(int subId) {
        SubscriptionPlan[] plans = this.mSubscriptionPlans.get(subId);
        if (ArrayUtils.isEmpty(plans)) {
            return null;
        }
        int length = plans.length;
        for (int i = 0; i < length; i++) {
            SubscriptionPlan plan = plans[i];
            if (plan.getCycleRule().isRecurring() || plan.cycleIterator().next().contains((Range<ZonedDateTime>) ZonedDateTime.now(this.mClock))) {
                return plan;
            }
        }
        return null;
    }

    private void waitForAdminData() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin")) {
            ConcurrentUtils.waitForCountDownNoInterrupt(this.mAdminDataAvailableLatch, 10000, "Wait for admin data");
        }
    }

    private void handleRestrictedPackagesChangeUL(Set<Integer> oldRestrictedUids, Set<Integer> newRestrictedUids) {
        if (this.mNetworkManagerReady) {
            if (oldRestrictedUids == null) {
                for (Integer num : newRestrictedUids) {
                    updateRulesForDataUsageRestrictionsUL(num.intValue());
                }
                return;
            }
            for (Integer num2 : oldRestrictedUids) {
                int uid = num2.intValue();
                if (!newRestrictedUids.contains(Integer.valueOf(uid))) {
                    updateRulesForDataUsageRestrictionsUL(uid);
                }
            }
            for (Integer num3 : newRestrictedUids) {
                int uid2 = num3.intValue();
                if (!oldRestrictedUids.contains(Integer.valueOf(uid2))) {
                    updateRulesForDataUsageRestrictionsUL(uid2);
                }
            }
        }
    }

    @GuardedBy({"mUidRulesFirstLock"})
    private boolean isRestrictedByAdminUL(int uid) {
        Set<Integer> restrictedUids = this.mMeteredRestrictedUids.get(UserHandle.getUserId(uid));
        return restrictedUids != null && restrictedUids.contains(Integer.valueOf(uid));
    }

    /* access modifiers changed from: private */
    public static boolean hasRule(int uidRules, int rule) {
        return (uidRules & rule) != 0;
    }

    public long getTimeRefreshElapsedRealtime() {
        if (this.mTimeRefreshRealtime != -1) {
            return SystemClock.elapsedRealtime() - this.mTimeRefreshRealtime;
        }
        return JobStatus.NO_LATEST_RUNTIME;
    }

    private static NetworkState[] defeatNullable(NetworkState[] val) {
        return val != null ? val : new NetworkState[0];
    }

    private static boolean getBooleanDefeatingNullable(PersistableBundle bundle, String key, boolean defaultValue) {
        return bundle != null ? bundle.getBoolean(key, defaultValue) : defaultValue;
    }

    private static boolean isHeadlessSystemUserBuild() {
        return RoSystemProperties.MULTIUSER_HEADLESS_SYSTEM_USER;
    }

    /* access modifiers changed from: private */
    public class NotificationId {
        private final int mId;
        private final String mTag;

        NotificationId(NetworkPolicy policy, int type) {
            this.mTag = buildNotificationTag(policy, type);
            this.mId = type;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NotificationId)) {
                return false;
            }
            return Objects.equals(this.mTag, ((NotificationId) o).mTag);
        }

        public int hashCode() {
            return Objects.hash(this.mTag);
        }

        private String buildNotificationTag(NetworkPolicy policy, int type) {
            return "NetworkPolicy:" + policy.template.hashCode() + ":" + type;
        }

        public String getTag() {
            return this.mTag;
        }

        public int getId() {
            return this.mId;
        }
    }

    private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        if (this.mHwBehaviorManager == null) {
            this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
        }
        IHwBehaviorCollectManager iHwBehaviorCollectManager = this.mHwBehaviorManager;
        if (iHwBehaviorCollectManager != null) {
            try {
                iHwBehaviorCollectManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
            } catch (Exception e) {
                Log.e(TAG, "sendBehavior:");
            }
        } else {
            Log.w(TAG, "HwBehaviorCollectManager is null");
        }
    }
}
