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
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.StatLogger;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.NetPluginDelegate;
import com.android.server.NetworkManagementService;
import com.android.server.ServiceThread;
import com.android.server.SystemConfig;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
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
    private static final boolean GOOGLE_WARNING_DISABLED = true;
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", BluetoothManagerService.DEFAULT_PACKAGE_NAME).contains("docomo");
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    /* access modifiers changed from: private */
    public static final boolean LOGD = NetworkPolicyLogger.LOGD;
    /* access modifiers changed from: private */
    public static final boolean LOGV = NetworkPolicyLogger.LOGV;
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
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final ArraySet<NotificationId> mActiveNotifs;
    private final IActivityManager mActivityManager;
    private ActivityManagerInternal mActivityManagerInternal;
    /* access modifiers changed from: private */
    public final CountDownLatch mAdminDataAvailableLatch;
    private final INetworkManagementEventObserver mAlertObserver;
    private final BroadcastReceiver mAllowReceiver;
    private final AppOpsManager mAppOps;
    private final CarrierConfigManager mCarrierConfigManager;
    private BroadcastReceiver mCarrierConfigReceiver;
    private final Clock mClock;
    private IConnectivityManager mConnManager;
    private BroadcastReceiver mConnReceiver;
    /* access modifiers changed from: private */
    public final Context mContext;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mDefaultRestrictBackgroundWhitelistUids;
    private IDeviceIdleController mDeviceIdleController;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mDeviceIdleMode;
    @GuardedBy("mUidRulesFirstLock")
    final SparseBooleanArray mFirewallChainStates;
    final Handler mHandler;
    private final Handler.Callback mHandlerCallback;
    private IHwBehaviorCollectManager mHwBehaviorManager;
    private final IPackageManager mIPm;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<INetworkPolicyListener> mListeners;
    private boolean mLoadedRestrictBackground;
    /* access modifiers changed from: private */
    public final NetworkPolicyLogger mLogger;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private String[] mMergedSubscriberIds;
    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPoliciesSecondLock")
    public ArraySet<String> mMeteredIfaces;
    /* access modifiers changed from: private */
    public ArraySet<String> mMeteredIfacesReplica;
    /* access modifiers changed from: private */
    @GuardedBy("mUidRulesFirstLock")
    public final SparseArray<Set<Integer>> mMeteredRestrictedUids;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final SparseIntArray mNetIdToSubId;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final INetworkManagementService mNetworkManager;
    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPoliciesSecondLock")
    public final SparseBooleanArray mNetworkMetered;
    final Object mNetworkPoliciesReplicaSecondLock;
    final Object mNetworkPoliciesSecondLock;
    @GuardedBy("mNetworkPoliciesSecondLock")
    final ArrayMap<NetworkTemplate, NetworkPolicy> mNetworkPolicy;
    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPoliciesSecondLock")
    public final SparseBooleanArray mNetworkRoaming;
    /* access modifiers changed from: private */
    public NetworkStatsManagerInternal mNetworkStats;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final ArraySet<NetworkTemplate> mOverLimitNotified;
    private final BroadcastReceiver mPackageReceiver;
    @GuardedBy("allLocks")
    private final AtomicFile mPolicyFile;
    private PowerManagerInternal mPowerManagerInternal;
    /* access modifiers changed from: private */
    @GuardedBy("mUidRulesFirstLock")
    public final SparseBooleanArray mPowerSaveTempWhitelistAppIds;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mPowerSaveWhitelistAppIds;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final BroadcastReceiver mPowerSaveWhitelistReceiver;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mRestrictBackground;
    private boolean mRestrictBackgroundBeforeBsm;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mRestrictBackgroundChangedInBsm;
    @GuardedBy("mUidRulesFirstLock")
    private PowerSaveState mRestrictBackgroundPowerState;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mRestrictBackgroundWhitelistRevokedUids;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mRestrictPower;
    private final BroadcastReceiver mSnoozeReceiver;
    public final StatLogger mStatLogger;
    private final BroadcastReceiver mStatsReceiver;
    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPoliciesSecondLock")
    public final SparseArray<String> mSubIdToSubscriberId;
    @GuardedBy("mNetworkPoliciesSecondLock")
    final SparseLongArray mSubscriptionOpportunisticQuota;
    @GuardedBy("mNetworkPoliciesSecondLock")
    final SparseArray<SubscriptionPlan[]> mSubscriptionPlans;
    @GuardedBy("mNetworkPoliciesSecondLock")
    final SparseArray<String> mSubscriptionPlansOwner;
    private final boolean mSuppressDefaultPolicy;
    @GuardedBy("allLocks")
    volatile boolean mSystemReady;
    private long mTimeRefreshRealtime;
    @VisibleForTesting
    public final Handler mUidEventHandler;
    private final Handler.Callback mUidEventHandlerCallback;
    private final ServiceThread mUidEventThread;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidFirewallDozableRules;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidFirewallPowerSaveRules;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidFirewallStandbyRules;
    private final IUidObserver mUidObserver;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidPolicy;
    private final BroadcastReceiver mUidRemovedReceiver;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidRules;
    final Object mUidRulesFirstLock;
    @GuardedBy("mUidRulesFirstLock")
    final SparseIntArray mUidState;
    private UsageStatsManagerInternal mUsageStats;
    private final UserManager mUserManager;
    private final BroadcastReceiver mUserReceiver;
    private final BroadcastReceiver mWifiReceiver;

    private class AppIdleStateChangeListener extends UsageStatsManagerInternal.AppIdleStateChangeListener {
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface ChainToggleType {
    }

    private class NetworkPolicyManagerInternalImpl extends NetworkPolicyManagerInternal {
        private NetworkPolicyManagerInternalImpl() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:11:0x001c  */
        public void resetUserState(int userId) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                boolean changed = false;
                boolean changed2 = NetworkPolicyManagerService.this.removeUserStateUL(userId, false);
                if (!NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId)) {
                    if (!changed2) {
                        if (changed) {
                            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                NetworkPolicyManagerService.this.writePolicyAL();
                            }
                        }
                    }
                }
                changed = true;
                if (changed) {
                }
            }
        }

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

        public boolean isUidNetworkingBlocked(int uid, String ifname) {
            boolean isNetworkMetered;
            long startTime = NetworkPolicyManagerService.this.mStatLogger.getTime();
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesReplicaSecondLock) {
                isNetworkMetered = NetworkPolicyManagerService.this.mMeteredIfacesReplica.contains(ifname);
            }
            boolean ret = NetworkPolicyManagerService.this.isUidNetworkingBlockedInternal(uid, isNetworkMetered);
            NetworkPolicyManagerService.this.mStatLogger.logDurationStat(1, startTime);
            return ret;
        }

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

        public SubscriptionPlan getSubscriptionPlan(Network network) {
            SubscriptionPlan access$4200;
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                access$4200 = NetworkPolicyManagerService.this.getPrimarySubscriptionPlanLocked(NetworkPolicyManagerService.this.getSubIdLocked(network));
            }
            return access$4200;
        }

        public SubscriptionPlan getSubscriptionPlan(NetworkTemplate template) {
            SubscriptionPlan access$4200;
            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                access$4200 = NetworkPolicyManagerService.this.getPrimarySubscriptionPlanLocked(NetworkPolicyManagerService.this.findRelevantSubIdNL(template));
            }
            return access$4200;
        }

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

        public void onAdminDataAvailable() {
            NetworkPolicyManagerService.this.mAdminDataAvailableLatch.countDown();
        }

        public void setMeteredRestrictedPackages(Set<String> packageNames, int userId) {
            NetworkPolicyManagerService.this.setMeteredRestrictedPackagesInternal(packageNames, userId);
        }

        public void setMeteredRestrictedPackagesAsync(Set<String> packageNames, int userId) {
            NetworkPolicyManagerService.this.mHandler.obtainMessage(17, userId, 0, packageNames).sendToTarget();
        }
    }

    private class NotificationId {
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
            return Objects.hash(new Object[]{this.mTag});
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface RestrictType {
    }

    interface Stats {
        public static final int COUNT = 2;
        public static final int IS_UID_NETWORKING_BLOCKED = 1;
        public static final int UPDATE_NETWORK_ENABLED = 0;
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
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveWhitelistUL();
                    NetworkPolicyManagerService.this.updateRulesForRestrictPowerUL();
                    NetworkPolicyManagerService.this.updateRulesForAppIdleUL();
                }
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
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
                    switch (c) {
                        case 0:
                        case 1:
                            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                                NetworkPolicyManagerService.this.removeUserStateUL(userId, true);
                                NetworkPolicyManagerService.this.mMeteredRestrictedUids.remove(userId);
                                if (action == "android.intent.action.USER_ADDED") {
                                    boolean unused = NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId);
                                }
                                synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                    NetworkPolicyManagerService.this.updateRulesForGlobalChangeAL(true);
                                }
                                break;
                            }
                    }
                }
            }
        };
        this.mStatsReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                    NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                    NetworkPolicyManagerService.this.updateNotificationsNL();
                }
            }
        };
        this.mAllowReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.setRestrictBackground(false);
            }
        };
        this.mSnoozeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkTemplate template = intent.getParcelableExtra("android.net.NETWORK_TEMPLATE");
                if (NetworkPolicyManagerService.ACTION_SNOOZE_WARNING.equals(intent.getAction())) {
                    NetworkPolicyManagerService.this.performSnooze(template, 34);
                } else if (NetworkPolicyManagerService.ACTION_SNOOZE_RAPID.equals(intent.getAction())) {
                    NetworkPolicyManagerService.this.performSnooze(template, 45);
                }
            }
        };
        this.mWifiReceiver = new BroadcastReceiver() {
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
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                if (network != null && networkCapabilities != null) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        boolean newMetered = !networkCapabilities.hasCapability(11);
                        boolean meteredChanged = NetworkPolicyManagerService.updateCapabilityChange(NetworkPolicyManagerService.this.mNetworkMetered, newMetered, network);
                        boolean roamingChanged = NetworkPolicyManagerService.updateCapabilityChange(NetworkPolicyManagerService.this.mNetworkRoaming, !networkCapabilities.hasCapability(18), network);
                        if (meteredChanged || roamingChanged) {
                            NetworkPolicyManagerService.this.mLogger.meterednessChanged(network.netId, newMetered);
                            NetworkPolicyManagerService.this.updateNetworkRulesNL();
                            NetworkPolicyManagerService.this.syncMeteredIfacesToReplicaNL();
                        }
                    }
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            public void limitReached(String limitName, String iface) {
                NetworkPolicyManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkPolicyManagerService.TAG);
                if (!NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(5, iface).sendToTarget();
                }
            }
        };
        this.mConnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.updateNetworksInternal();
            }
        };
        this.mCarrierConfigReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("subscription")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    NetworkPolicyManagerService.this.updateSubscriptions();
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            String subscriberId = (String) NetworkPolicyManagerService.this.mSubIdToSubscriberId.get(subId, null);
                            if (subscriberId != null) {
                                boolean unused = NetworkPolicyManagerService.this.ensureActiveMobilePolicyAL(subId, subscriberId);
                                boolean unused2 = NetworkPolicyManagerService.this.maybeUpdateMobilePolicyCycleAL(subId, subscriberId);
                            } else {
                                Slog.wtf(NetworkPolicyManagerService.TAG, "Missing subscriberId for subId " + subId);
                            }
                            NetworkPolicyManagerService.this.handleNetworkPoliciesUpdateAL(true);
                        }
                    }
                }
            }
        };
        this.mHandlerCallback = new Handler.Callback() {
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v42, resolved type: boolean} */
            /* JADX WARNING: type inference failed for: r1v0 */
            /* JADX WARNING: type inference failed for: r1v1, types: [int] */
            /* JADX WARNING: type inference failed for: r1v5, types: [int] */
            /* JADX WARNING: type inference failed for: r1v18, types: [int] */
            /* JADX WARNING: type inference failed for: r1v29, types: [int] */
            /* JADX WARNING: type inference failed for: r1v36, types: [int] */
            /* JADX WARNING: type inference failed for: r1v43 */
            /* JADX WARNING: type inference failed for: r1v44 */
            /* JADX WARNING: type inference failed for: r1v45 */
            /* JADX WARNING: type inference failed for: r1v46 */
            /* JADX WARNING: type inference failed for: r1v47 */
            /* JADX WARNING: type inference failed for: r1v48 */
            /* JADX WARNING: Multi-variable type inference failed */
            public boolean handleMessage(Message msg) {
                ? enabled = 0;
                switch (msg.what) {
                    case 1:
                        int uid = msg.arg1;
                        int uidRules = msg.arg2;
                        int length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        while (enabled < length) {
                            NetworkPolicyManagerService.this.dispatchUidRulesChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(enabled), uid, uidRules);
                            enabled++;
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 2:
                        String[] meteredIfaces = (String[]) msg.obj;
                        int length2 = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        while (enabled < length2) {
                            NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(enabled), meteredIfaces);
                            enabled++;
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
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
                        boolean restrictBackground = msg.arg1 != 0;
                        int length3 = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        while (enabled < length3) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(enabled), restrictBackground);
                            enabled++;
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
                        while (enabled < length4) {
                            NetworkPolicyManagerService.this.dispatchUidPoliciesChanged(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(enabled), uid2, policy);
                            enabled++;
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
                        while (enabled < length5) {
                            NetworkPolicyManagerService.this.dispatchSubscriptionOverride(NetworkPolicyManagerService.this.mListeners.getBroadcastItem(enabled), subId, overrideMask, overrideValue);
                            enabled++;
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 17:
                        NetworkPolicyManagerService.this.setMeteredRestrictedPackagesInternal((Set) msg.obj, msg.arg1);
                        return true;
                    case 18:
                        NetworkTemplate template = (NetworkTemplate) msg.obj;
                        if (msg.arg1 != 0) {
                            enabled = 1;
                        }
                        NetworkPolicyManagerService.this.setNetworkTemplateEnabledInner(template, enabled);
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.mUidEventHandlerCallback = new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        NetworkPolicyManagerService.this.handleUidChanged(msg.arg1, msg.arg2, ((Long) msg.obj).longValue());
                        return true;
                    case 101:
                        NetworkPolicyManagerService.this.handleUidGone(msg.arg1);
                        return true;
                    default:
                        return false;
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
    public boolean addDefaultRestrictBackgroundWhitelistUidsUL(int userId) {
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
    public void initService(CountDownLatch initCompleteSignal) {
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
                    setRestrictBackgroundUL(this.mLoadedRestrictBackground);
                    updateRulesForGlobalChangeAL(false);
                    updateNotificationsNL();
                }
            }
            this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
            try {
                this.mActivityManager.registerUidObserver(this.mUidObserver, 3, -1, null);
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
                public void onSubscriptionsChanged() {
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
        CountDownLatch initCompleteSignal = new CountDownLatch(1);
        this.mHandler.post(new Runnable(initCompleteSignal) {
            private final /* synthetic */ CountDownLatch f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NetworkPolicyManagerService.this.initService(this.f$1);
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
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00e0  */
    public void updateNotificationsNL() {
        boolean z;
        int i;
        long totalBytes;
        Pair<ZonedDateTime, ZonedDateTime> cycle;
        if (LOGV) {
            Slog.v(TAG, "updateNotificationsNL()");
        }
        Trace.traceBegin(2097152, "updateNotificationsNL");
        ArraySet arraySet = new ArraySet(this.mActiveNotifs);
        this.mActiveNotifs.clear();
        long now = this.mClock.millis();
        boolean z2 = true;
        int i2 = this.mNetworkPolicy.size() - 1;
        while (true) {
            int i3 = i2;
            if (i3 >= 0) {
                NetworkPolicy policy = this.mNetworkPolicy.valueAt(i3);
                int subId = findRelevantSubIdNL(policy.template);
                if (subId != -1 && policy.hasCycle()) {
                    Pair<ZonedDateTime, ZonedDateTime> cycle2 = (Pair) NetworkPolicyManager.cycleIterator(policy).next();
                    if (cycle2.first == null) {
                        Pair<ZonedDateTime, ZonedDateTime> pair = cycle2;
                        NetworkPolicy networkPolicy = policy;
                        int i4 = subId;
                        int i5 = i3;
                        break;
                    } else if (cycle2.second == null) {
                        Pair<ZonedDateTime, ZonedDateTime> pair2 = cycle2;
                        NetworkPolicy networkPolicy2 = policy;
                        int i6 = subId;
                        int i7 = i3;
                        break;
                    } else {
                        long cycleStart = ((ZonedDateTime) cycle2.first).toInstant().toEpochMilli();
                        long cycleEnd = ((ZonedDateTime) cycle2.second).toInstant().toEpochMilli();
                        long totalBytes2 = getTotalBytes(policy.template, cycleStart, cycleEnd);
                        PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(subId);
                        boolean notifyWarning = getBooleanDefeatingNullable(config, "data_warning_notification_bool", z2);
                        boolean notifyLimit = getBooleanDefeatingNullable(config, "data_limit_notification_bool", z2);
                        boolean notifyRapid = getBooleanDefeatingNullable(config, "data_rapid_notification_bool", z2);
                        boolean snoozedRecently = false;
                        if (notifyWarning && policy.isOverWarning(totalBytes2) && !policy.isOverLimit(totalBytes2)) {
                            if (!(policy.lastWarningSnooze >= cycleStart ? z2 : false)) {
                                PersistableBundle persistableBundle = config;
                                totalBytes = totalBytes2;
                                enqueueNotification(policy, 34, totalBytes2, null);
                                if (notifyLimit) {
                                    if (policy.isOverLimit(totalBytes)) {
                                        if (policy.lastLimitSnooze >= cycleStart ? z2 : false) {
                                            enqueueNotification(policy, 36, totalBytes, null);
                                        } else {
                                            enqueueNotification(policy, 35, totalBytes, null);
                                            notifyOverLimitNL(policy.template);
                                        }
                                    } else {
                                        notifyUnderLimitNL(policy.template);
                                    }
                                }
                                if (notifyRapid || policy.limitBytes == -1) {
                                    z = z2;
                                    i = i3;
                                } else {
                                    long recentDuration = TimeUnit.DAYS.toMillis(4);
                                    long recentStart = now - recentDuration;
                                    long recentEnd = now;
                                    long recentBytes = getTotalBytes(policy.template, recentStart, recentEnd);
                                    long cycleDuration = cycleEnd - cycleStart;
                                    long j = cycleDuration;
                                    long cycleDuration2 = (recentBytes * cycleDuration) / recentDuration;
                                    int i8 = i3;
                                    long alertBytes = (policy.limitBytes * 3) / 2;
                                    if (LOGD) {
                                        cycle = cycle2;
                                        StringBuilder sb = new StringBuilder();
                                        int i9 = subId;
                                        sb.append("Rapid usage considering recent ");
                                        sb.append(recentBytes);
                                        sb.append(" projected ");
                                        sb.append(cycleDuration2);
                                        sb.append(" alert ");
                                        sb.append(alertBytes);
                                        Slog.d(TAG, sb.toString());
                                    } else {
                                        cycle = cycle2;
                                        int i10 = subId;
                                    }
                                    if (policy.lastRapidSnooze >= now - 86400000) {
                                        snoozedRecently = true;
                                    }
                                    if (cycleDuration2 <= alertBytes || snoozedRecently) {
                                        i = i8;
                                        z = true;
                                    } else {
                                        Pair<ZonedDateTime, ZonedDateTime> pair3 = cycle;
                                        long j2 = alertBytes;
                                        i = i8;
                                        z = true;
                                        enqueueNotification(policy, 45, 0, findRapidBlame(policy.template, recentStart, recentEnd));
                                    }
                                }
                            }
                        }
                        totalBytes = totalBytes2;
                        if (notifyLimit) {
                        }
                        if (notifyRapid) {
                        }
                        z = z2;
                        i = i3;
                    }
                } else {
                    z = z2;
                    i = i3;
                }
                i2 = i - 1;
                z2 = z;
            } else {
                boolean z3 = z2;
                for (int i11 = arraySet.size() - 1; i11 >= 0; i11--) {
                    NotificationId notificationId = (NotificationId) arraySet.valueAt(i11);
                    if (!this.mActiveNotifs.contains(notificationId)) {
                        cancelNotification(notificationId);
                    }
                }
                Trace.traceEnd(2097152);
                return;
            }
        }
    }

    private ApplicationInfo findRapidBlame(NetworkTemplate template, long start, long end) {
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
        if (maxBytes > 0 && maxBytes > totalBytes / 2) {
            String[] packageNames = this.mContext.getPackageManager().getPackagesForUid(maxUid);
            if (packageNames != null && packageNames.length == 1) {
                try {
                    return this.mContext.getPackageManager().getApplicationInfo(packageNames[0], 4989440);
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public int findRelevantSubIdNL(NetworkTemplate template) {
        for (int i = 0; i < this.mSubIdToSubscriberId.size(); i++) {
            int subId = this.mSubIdToSubscriberId.keyAt(i);
            NetworkIdentity probeIdent = new NetworkIdentity(0, 0, this.mSubIdToSubscriberId.valueAt(i), null, false, true, true);
            if (template.matches(probeIdent)) {
                return subId;
            }
        }
        return -1;
    }

    private void notifyOverLimitNL(NetworkTemplate template) {
        if (!this.mOverLimitNotified.contains(template)) {
            this.mContext.startActivity(buildNetworkOverLimitIntent(this.mContext.getResources(), template));
            this.mOverLimitNotified.add(template);
        }
    }

    private void notifyUnderLimitNL(NetworkTemplate template) {
        this.mOverLimitNotified.remove(template);
    }

    private void enqueueNotification(NetworkPolicy policy, int type, long totalBytes, ApplicationInfo rapidBlame) {
        CharSequence title;
        CharSequence body;
        CharSequence body2;
        int i;
        CharSequence title2;
        NetworkPolicy networkPolicy = policy;
        int i2 = type;
        long j = totalBytes;
        ApplicationInfo applicationInfo = rapidBlame;
        NotificationId notificationId = new NotificationId(networkPolicy, i2);
        Notification.Builder builder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_ALERTS);
        builder.setOnlyAlertOnce(true);
        builder.setWhen(0);
        builder.setColor(this.mContext.getColor(17170784));
        Resources res = this.mContext.getResources();
        if (i2 != 45) {
            switch (i2) {
                case 34:
                    if (!isNeedShowWarning()) {
                        if (LOGV) {
                            Slog.v(TAG, "google warning disabled ,don't show notification");
                        }
                        return;
                    }
                    title = res.getText(17039887);
                    body = res.getString(17039886, new Object[]{Formatter.formatFileSize(this.mContext, j)});
                    builder.setSmallIcon(17301624);
                    Bitmap bmp1 = BitmapFactory.decodeResource(res, 33751681);
                    if (bmp1 != null) {
                        builder.setLargeIcon(bmp1);
                    }
                    if (isNeedShowWarning()) {
                        i = 134217728;
                        builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, buildSnoozeWarningIntent(networkPolicy.template), 134217728));
                    } else {
                        i = 134217728;
                    }
                    builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(res, networkPolicy.template), i));
                    if (!isNeedShowWarning()) {
                        return;
                    }
                    break;
                case 35:
                    int matchRule = networkPolicy.template.getMatchRule();
                    if (matchRule == 1) {
                        title2 = res.getText(17039880);
                    } else if (matchRule == 4) {
                        title2 = res.getText(17039889);
                    } else {
                        return;
                    }
                    title = title2;
                    body = res.getText(17039877);
                    builder.setOngoing(true);
                    Bitmap bmp2 = BitmapFactory.decodeResource(res, 33751679);
                    if (bmp2 != null) {
                        builder.setLargeIcon(bmp2);
                    }
                    builder.setSmallIcon(17303472);
                    builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildNetworkOverLimitIntent(res, networkPolicy.template), 134217728));
                    break;
                case 36:
                    int matchRule2 = networkPolicy.template.getMatchRule();
                    if (matchRule2 == 1) {
                        title = res.getText(17039879);
                    } else if (matchRule2 == 4) {
                        title = res.getText(17039888);
                    } else {
                        return;
                    }
                    CharSequence string = res.getString(17039878, new Object[]{Formatter.formatFileSize(this.mContext, j - networkPolicy.limitBytes)});
                    builder.setOngoing(true);
                    builder.setSmallIcon(17301624);
                    Bitmap bmp3 = BitmapFactory.decodeResource(res, 33751681);
                    if (bmp3 != null) {
                        builder.setLargeIcon(bmp3);
                    }
                    builder.setChannelId(SystemNotificationChannels.NETWORK_STATUS);
                    CharSequence body3 = string;
                    builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(res, networkPolicy.template), 134217728));
                    if (isNeedShowWarning()) {
                        body = body3;
                        break;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } else {
            title = res.getText(17039883);
            if (applicationInfo != null) {
                body2 = res.getString(17039881, new Object[]{applicationInfo.loadLabel(this.mContext.getPackageManager())});
            } else {
                body2 = res.getString(17039882);
            }
            body = body2;
            builder.setSmallIcon(17301624);
            builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, buildSnoozeRapidIntent(networkPolicy.template), 134217728));
            builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(res, networkPolicy.template), 134217728));
        }
        CharSequence title3 = title;
        builder.setTicker(title3);
        builder.setContentTitle(title3);
        builder.setContentText(body);
        builder.setStyle(new Notification.BigTextStyle().bigText(body));
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).notifyAsUser(notificationId.getTag(), notificationId.getId(), builder.build(), UserHandle.ALL);
        this.mActiveNotifs.add(notificationId);
    }

    private void cancelNotification(NotificationId notificationId) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).cancel(notificationId.getTag(), notificationId.getId());
    }

    /* access modifiers changed from: private */
    public void updateNetworksInternal() {
        updateSubscriptions();
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

    @VisibleForTesting
    public void updateNetworks() throws InterruptedException {
        updateNetworksInternal();
        CountDownLatch latch = new CountDownLatch(1);
        this.mHandler.post(new Runnable(latch) {
            private final /* synthetic */ CountDownLatch f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    /* access modifiers changed from: private */
    public boolean maybeUpdateMobilePolicyCycleAL(int subId, String subscriberId) {
        if (LOGV) {
            Slog.v(TAG, "maybeUpdateMobilePolicyCycleAL()");
        }
        boolean policyUpdated = false;
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, null, false, true, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            if (this.mNetworkPolicy.keyAt(i).matches(probeIdent)) {
                policyUpdated |= updateDefaultMobilePolicyAL(subId, this.mNetworkPolicy.valueAt(i));
            }
        }
        return policyUpdated;
    }

    @VisibleForTesting
    public int getCycleDayFromCarrierConfig(PersistableBundle config, int fallbackCycleDay) {
        if (config == null) {
            return fallbackCycleDay;
        }
        int cycleDay = config.getInt("monthly_data_cycle_day_int");
        if (cycleDay == -1) {
            return fallbackCycleDay;
        }
        Calendar cal = Calendar.getInstance();
        if (cycleDay >= cal.getMinimum(5) && cycleDay <= cal.getMaximum(5)) {
            return cycleDay;
        }
        Slog.e(TAG, "Invalid date in CarrierConfigManager.KEY_MONTHLY_DATA_CYCLE_DAY_INT: " + cycleDay);
        return fallbackCycleDay;
    }

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
    public void handleNetworkPoliciesUpdateAL(boolean shouldNormalizePolicies) {
        if (shouldNormalizePolicies) {
            normalizePoliciesNL();
        }
        updateNetworkEnabledNL();
        updateNetworkRulesNL();
        updateNotificationsNL();
        writePolicyAL();
        syncMeteredIfacesToReplicaNL();
    }

    /* access modifiers changed from: package-private */
    public void updateNetworkEnabledNL() {
        if (LOGV) {
            Slog.v(TAG, "updateNetworkEnabledNL()");
        }
        Trace.traceBegin(2097152, "updateNetworkEnabledNL");
        long startTime = this.mStatLogger.getTime();
        int i = this.mNetworkPolicy.size() - 1;
        while (true) {
            int i2 = i;
            boolean networkEnabled = false;
            if (i2 >= 0) {
                NetworkPolicy policy = this.mNetworkPolicy.valueAt(i2);
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
                i = i2 - 1;
            } else {
                this.mStatLogger.logDurationStat(0, startTime);
                Trace.traceEnd(2097152);
                return;
            }
        }
    }

    private void setNetworkTemplateEnabled(NetworkTemplate template, boolean enabled) {
        this.mHandler.obtainMessage(18, enabled, 0, template).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        r3 = (android.telephony.TelephonyManager) r1.mContext.getSystemService(android.telephony.TelephonyManager.class);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0056, code lost:
        if (r0 >= r2.size()) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
        r3.setPolicyDataEnabled(r17, r2.get(r0));
        r0 = r0 + 1;
     */
    public void setNetworkTemplateEnabledInner(NetworkTemplate template, boolean enabled) {
        if (template.getMatchRule() == 1) {
            IntArray matchingSubIds = new IntArray();
            synchronized (this.mNetworkPoliciesSecondLock) {
                int i = 0;
                int i2 = 0;
                while (i2 < this.mSubIdToSubscriberId.size()) {
                    try {
                        int subId = this.mSubIdToSubscriberId.keyAt(i2);
                        NetworkIdentity networkIdentity = new NetworkIdentity(0, 0, this.mSubIdToSubscriberId.valueAt(i2), null, false, true, true);
                        try {
                            if (template.matches(networkIdentity)) {
                                matchingSubIds.add(subId);
                            }
                            i2++;
                        } catch (Throwable th) {
                            th = th;
                            boolean z = enabled;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        NetworkTemplate networkTemplate = template;
                        boolean z2 = enabled;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                NetworkTemplate networkTemplate2 = template;
            }
        } else {
            NetworkTemplate networkTemplate3 = template;
        }
        boolean z3 = enabled;
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
    public void updateNetworkRulesNL() {
        boolean z;
        ContentResolver cr;
        NetworkState[] states;
        int i;
        long quotaBytes;
        int subId;
        ArrayMap<NetworkState, NetworkIdentity> identified;
        NetworkPolicy policy;
        long lowestRule;
        ArraySet<String> newMeteredIfaces;
        long quotaBytes2;
        if (LOGV) {
            Slog.v(TAG, "updateNetworkRulesNL()");
        }
        Trace.traceBegin(2097152, "updateNetworkRulesNL");
        try {
            NetworkState[] states2 = defeatNullable(this.mConnManager.getAllNetworkState());
            this.mNetIdToSubId.clear();
            ArrayMap<NetworkState, NetworkIdentity> identified2 = new ArrayMap<>();
            int length = states2.length;
            int i2 = 0;
            while (true) {
                z = true;
                if (i2 >= length) {
                    break;
                }
                NetworkState state = states2[i2];
                if (state.network != null) {
                    this.mNetIdToSubId.put(state.network.netId, parseSubId(state));
                }
                if (state.networkInfo != null && state.networkInfo.isConnected()) {
                    identified2.put(state, NetworkIdentity.buildNetworkIdentity(this.mContext, state, true));
                }
                i2++;
            }
            ArraySet<String> newMeteredIfaces2 = new ArraySet<>();
            ArraySet arraySet = new ArraySet();
            int i3 = this.mNetworkPolicy.size() - 1;
            long lowestRule2 = Long.MAX_VALUE;
            while (true) {
                int i4 = i3;
                if (i4 < 0) {
                    break;
                }
                NetworkPolicy policy2 = this.mNetworkPolicy.valueAt(i4);
                arraySet.clear();
                for (int j = identified2.size() - z; j >= 0; j--) {
                    if (policy2.template.matches(identified2.valueAt(j))) {
                        collectIfaces(arraySet, identified2.keyAt(j));
                    }
                }
                if (LOGD != 0) {
                    Slog.d(TAG, "Applying " + policy2 + " to ifaces " + arraySet);
                }
                boolean hasWarning = policy2.warningBytes != -1 ? z : false;
                boolean hasLimit = policy2.limitBytes != -1 ? z : false;
                if (hasLimit || policy2.metered) {
                    if (!hasLimit || !policy2.hasCycle()) {
                        policy = policy2;
                        identified = identified2;
                        newMeteredIfaces = newMeteredIfaces2;
                        lowestRule = lowestRule2;
                        quotaBytes2 = JobStatus.NO_LATEST_RUNTIME;
                    } else {
                        Pair<ZonedDateTime, ZonedDateTime> cycle = (Pair) NetworkPolicyManager.cycleIterator(policy2).next();
                        long start = ((ZonedDateTime) cycle.first).toInstant().toEpochMilli();
                        newMeteredIfaces = newMeteredIfaces2;
                        policy = policy2;
                        identified = identified2;
                        Pair<ZonedDateTime, ZonedDateTime> pair = cycle;
                        lowestRule = lowestRule2;
                        long totalBytes = getTotalBytes(policy2.template, start, ((ZonedDateTime) cycle.second).toInstant().toEpochMilli());
                        if (policy.lastLimitSnooze >= start) {
                            quotaBytes2 = JobStatus.NO_LATEST_RUNTIME;
                        } else {
                            quotaBytes2 = Math.max(1, policy.limitBytes - totalBytes);
                        }
                    }
                    long quotaBytes3 = quotaBytes2;
                    if (arraySet.size() > 1) {
                        Slog.w(TAG, "shared quota unsupported; generating rule for each iface");
                    }
                    for (int j2 = arraySet.size() - 1; j2 >= 0; j2--) {
                        String iface = (String) arraySet.valueAt(j2);
                        setInterfaceQuotaAsync(iface, quotaBytes3);
                        newMeteredIfaces.add(iface);
                    }
                    newMeteredIfaces2 = newMeteredIfaces;
                } else {
                    policy = policy2;
                    identified = identified2;
                    lowestRule = lowestRule2;
                }
                if (!hasWarning || policy.warningBytes >= lowestRule) {
                    lowestRule2 = lowestRule;
                } else {
                    lowestRule2 = policy.warningBytes;
                }
                if (hasLimit && policy.limitBytes < lowestRule2) {
                    lowestRule2 = policy.limitBytes;
                }
                i3 = i4 - 1;
                identified2 = identified;
                z = true;
            }
            long lowestRule3 = lowestRule2;
            for (NetworkState state2 : states2) {
                if (state2.networkInfo != null && state2.networkInfo.isConnected() && !state2.networkCapabilities.hasCapability(11)) {
                    arraySet.clear();
                    collectIfaces(arraySet, state2);
                    for (int j3 = arraySet.size() - 1; j3 >= 0; j3--) {
                        String iface2 = (String) arraySet.valueAt(j3);
                        if (!newMeteredIfaces2.contains(iface2)) {
                            setInterfaceQuotaAsync(iface2, JobStatus.NO_LATEST_RUNTIME);
                            newMeteredIfaces2.add(iface2);
                        }
                    }
                }
            }
            for (int i5 = this.mMeteredIfaces.size() - 1; i5 >= 0; i5--) {
                String iface3 = this.mMeteredIfaces.valueAt(i5);
                if (!newMeteredIfaces2.contains(iface3)) {
                    removeInterfaceQuotaAsync(iface3);
                }
            }
            this.mMeteredIfaces = newMeteredIfaces2;
            ContentResolver cr2 = this.mContext.getContentResolver();
            boolean z2 = true;
            if (Settings.Global.getInt(cr2, "netpolicy_quota_enabled", 1) == 0) {
                z2 = false;
            }
            boolean quotaEnabled = z2;
            long quotaUnlimited = Settings.Global.getLong(cr2, "netpolicy_quota_unlimited", QUOTA_UNLIMITED_DEFAULT);
            float quotaLimited = Settings.Global.getFloat(cr2, "netpolicy_quota_limited", QUOTA_LIMITED_DEFAULT);
            this.mSubscriptionOpportunisticQuota.clear();
            int length2 = states2.length;
            int i6 = 0;
            while (i6 < length2) {
                NetworkState state3 = states2[i6];
                if (quotaEnabled && state3.network != null) {
                    int subId2 = getSubIdLocked(state3.network);
                    SubscriptionPlan plan = getPrimarySubscriptionPlanLocked(subId2);
                    if (plan != null) {
                        long limitBytes = plan.getDataLimitBytes();
                        if (!state3.networkCapabilities.hasCapability(18)) {
                            quotaBytes = 0;
                        } else if (limitBytes == -1) {
                            quotaBytes = -1;
                        } else {
                            if (limitBytes == JobStatus.NO_LATEST_RUNTIME) {
                                quotaBytes = quotaUnlimited;
                                states = states2;
                                subId = subId2;
                                NetworkState networkState = state3;
                                cr = cr2;
                                SubscriptionPlan subscriptionPlan = plan;
                                i = i6;
                            } else {
                                Range<ZonedDateTime> cycle2 = plan.cycleIterator().next();
                                long start2 = cycle2.getLower().toInstant().toEpochMilli();
                                long end = cycle2.getUpper().toInstant().toEpochMilli();
                                Instant now = this.mClock.instant();
                                states = states2;
                                cr = cr2;
                                Instant now2 = now;
                                Range<ZonedDateTime> range = cycle2;
                                subId = subId2;
                                SubscriptionPlan subscriptionPlan2 = plan;
                                i = i6;
                                NetworkState networkState2 = state3;
                                long totalBytes2 = getTotalBytes(NetworkTemplate.buildTemplateMobileAll(state3.subscriberId), start2, ZonedDateTime.ofInstant(now, cycle2.getLower().getZone()).truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli());
                                long remainingBytes = limitBytes - totalBytes2;
                                long j4 = totalBytes2;
                                Instant instant = now2;
                                long j5 = remainingBytes;
                                quotaBytes = Math.max(0, (long) (((float) (remainingBytes / ((((end - now2.toEpochMilli()) - 1) / TimeUnit.DAYS.toMillis(1)) + 1))) * quotaLimited));
                            }
                            this.mSubscriptionOpportunisticQuota.put(subId, quotaBytes);
                            i6 = i + 1;
                            states2 = states;
                            cr2 = cr;
                        }
                        states = states2;
                        subId = subId2;
                        i = i6;
                        cr = cr2;
                        this.mSubscriptionOpportunisticQuota.put(subId, quotaBytes);
                        i6 = i + 1;
                        states2 = states;
                        cr2 = cr;
                    }
                }
                states = states2;
                i = i6;
                cr = cr2;
                i6 = i + 1;
                states2 = states;
                cr2 = cr;
            }
            ContentResolver contentResolver = cr2;
            this.mHandler.obtainMessage(2, (String[]) this.mMeteredIfaces.toArray(new String[this.mMeteredIfaces.size()])).sendToTarget();
            this.mHandler.obtainMessage(7, Long.valueOf(lowestRule3)).sendToTarget();
            Trace.traceEnd(2097152);
        } catch (RemoteException e) {
        }
    }

    private void ensureActiveMobilePolicyAL() {
        if (LOGV) {
            Slog.v(TAG, "ensureActiveMobilePolicyAL()");
        }
        if (!this.mSuppressDefaultPolicy) {
            for (int i = 0; i < this.mSubIdToSubscriberId.size(); i++) {
                ensureActiveMobilePolicyAL(this.mSubIdToSubscriberId.keyAt(i), this.mSubIdToSubscriberId.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean ensureActiveMobilePolicyAL(int subId, String subscriberId) {
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, null, false, true, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            if (this.mNetworkPolicy.keyAt(i).matches(probeIdent)) {
                if (LOGD) {
                    Slog.d(TAG, "Found template " + template + " which matches subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId));
                }
                return false;
            }
        }
        Slog.i(TAG, "No policy for subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId) + "; generating default policy");
        addNetworkPolicyAL(buildDefaultMobilePolicy(subId, subscriberId));
        return true;
    }

    private long getPlatformDefaultWarningBytes() {
        int dataWarningConfig = this.mContext.getResources().getInteger(17694829);
        if (((long) dataWarningConfig) == -1) {
            return -1;
        }
        return ((long) dataWarningConfig) * 1048576;
    }

    private long getPlatformDefaultLimitBytes() {
        return -1;
    }

    @VisibleForTesting
    public NetworkPolicy buildDefaultMobilePolicy(int subId, String subscriberId) {
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(subscriberId);
        NetworkPolicy policy = new NetworkPolicy(template, NetworkPolicy.buildRule(ZonedDateTime.now().getDayOfMonth(), ZoneId.systemDefault()), getPlatformDefaultWarningBytes(), getPlatformDefaultLimitBytes(), -1, -1, true, true);
        synchronized (this.mUidRulesFirstLock) {
            try {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    updateDefaultMobilePolicyAL(subId, policy);
                }
                try {
                    return policy;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                int i = subId;
                throw th;
            }
        }
    }

    private boolean updateDefaultMobilePolicyAL(int subId, NetworkPolicy policy) {
        int currentCycleDay;
        int i = subId;
        NetworkPolicy networkPolicy = policy;
        if (!networkPolicy.inferred) {
            if (LOGD) {
                Slog.d(TAG, "Ignoring user-defined policy " + networkPolicy);
            }
            return false;
        }
        NetworkTemplate networkTemplate = networkPolicy.template;
        RecurrenceRule recurrenceRule = networkPolicy.cycleRule;
        long j = networkPolicy.warningBytes;
        long j2 = networkPolicy.limitBytes;
        long j3 = networkPolicy.lastWarningSnooze;
        long j4 = networkPolicy.lastLimitSnooze;
        NetworkPolicy networkPolicy2 = new NetworkPolicy(networkTemplate, recurrenceRule, j, j2, j3, j4, networkPolicy.metered, networkPolicy.inferred);
        SubscriptionPlan[] plans = this.mSubscriptionPlans.get(i);
        if (!ArrayUtils.isEmpty(plans)) {
            SubscriptionPlan plan = plans[0];
            networkPolicy.cycleRule = plan.getCycleRule();
            long planLimitBytes = plan.getDataLimitBytes();
            if (planLimitBytes != -1) {
                if (planLimitBytes != JobStatus.NO_LATEST_RUNTIME) {
                    networkPolicy.warningBytes = (9 * planLimitBytes) / 10;
                    switch (plan.getDataLimitBehavior()) {
                        case 0:
                        case 1:
                            networkPolicy.limitBytes = planLimitBytes;
                            break;
                        default:
                            networkPolicy.limitBytes = -1;
                            break;
                    }
                } else {
                    networkPolicy.warningBytes = -1;
                    networkPolicy.limitBytes = -1;
                }
            } else {
                networkPolicy.warningBytes = getPlatformDefaultWarningBytes();
                networkPolicy.limitBytes = getPlatformDefaultLimitBytes();
            }
        } else {
            PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(i);
            if (networkPolicy.cycleRule.isMonthly()) {
                currentCycleDay = networkPolicy.cycleRule.start.getDayOfMonth();
            } else {
                currentCycleDay = -1;
            }
            networkPolicy.cycleRule = NetworkPolicy.buildRule(getCycleDayFromCarrierConfig(config, currentCycleDay), ZoneId.systemDefault());
            networkPolicy.warningBytes = getWarningBytesFromCarrierConfig(config, networkPolicy.warningBytes);
            networkPolicy.limitBytes = getLimitBytesFromCarrierConfig(config, networkPolicy.limitBytes);
        }
        if (networkPolicy.equals(networkPolicy2)) {
            return false;
        }
        Slog.d(TAG, "Updated " + networkPolicy2 + " to " + networkPolicy);
        return true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0112 A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x011a A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0126 A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0130 A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0135 A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x013e A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x014b A[Catch:{ FileNotFoundException -> 0x038c, Exception -> 0x0382, all -> 0x0380 }] */
    public void readPolicyAL() {
        int version;
        boolean z;
        String networkId;
        RecurrenceRule cycleRule;
        long lastLimitSnooze;
        boolean metered;
        long lastWarningSnooze;
        boolean inferred;
        NetworkTemplate template;
        boolean metered2;
        long lastLimitSnooze2;
        String cycleTimezone;
        if (LOGV) {
            Slog.v(TAG, "readPolicyAL()");
        }
        this.mNetworkPolicy.clear();
        this.mSubscriptionPlans.clear();
        this.mSubscriptionPlansOwner.clear();
        this.mUidPolicy.clear();
        String str = null;
        FileInputStream fis = null;
        try {
            fis = this.mPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(fis, StandardCharsets.UTF_8.name());
            SparseBooleanArray whitelistedRestrictBackground = new SparseBooleanArray();
            int version2 = 1;
            boolean insideWhitelist = false;
            while (true) {
                int next = in.next();
                int type = next;
                boolean z2 = true;
                if (next == 1) {
                    break;
                }
                String tag = in.getName();
                if (type != 2) {
                    version = version2;
                    if (type == 3 && TAG_WHITELIST.equals(tag)) {
                        z = false;
                    }
                    version2 = version;
                    str = null;
                } else if (TAG_POLICY_LIST.equals(tag)) {
                    boolean z3 = this.mRestrictBackground;
                    version2 = XmlUtils.readIntAttribute(in, ATTR_VERSION);
                    if (version2 < 3 || !XmlUtils.readBooleanAttribute(in, ATTR_RESTRICT_BACKGROUND)) {
                        z2 = false;
                    }
                    this.mLoadedRestrictBackground = z2;
                    str = null;
                } else {
                    if (TAG_NETWORK_POLICY.equals(tag)) {
                        int networkTemplate = XmlUtils.readIntAttribute(in, ATTR_NETWORK_TEMPLATE);
                        String subscriberId = in.getAttributeValue(str, ATTR_SUBSCRIBER_ID);
                        if (version2 >= 9) {
                            networkId = in.getAttributeValue(str, ATTR_NETWORK_ID);
                        } else {
                            networkId = str;
                        }
                        if (version2 >= 11) {
                            String start = XmlUtils.readStringAttribute(in, ATTR_CYCLE_START);
                            String end = XmlUtils.readStringAttribute(in, ATTR_CYCLE_END);
                            String str2 = start;
                            String str3 = end;
                            cycleRule = new RecurrenceRule(RecurrenceRule.convertZonedDateTime(start), RecurrenceRule.convertZonedDateTime(end), RecurrenceRule.convertPeriod(XmlUtils.readStringAttribute(in, ATTR_CYCLE_PERIOD)));
                        } else {
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
                            lastLimitSnooze2 = XmlUtils.readLongAttribute(in, ATTR_LAST_LIMIT_SNOOZE);
                        } else if (version2 >= 2) {
                            lastLimitSnooze2 = XmlUtils.readLongAttribute(in, ATTR_LAST_SNOOZE);
                        } else {
                            lastLimitSnooze = -1;
                            if (version2 < 4) {
                                metered2 = XmlUtils.readBooleanAttribute(in, ATTR_METERED);
                            } else if (networkTemplate != 1) {
                                metered = false;
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
                                template = new NetworkTemplate(networkTemplate, subscriberId, networkId);
                                if (template.isPersistable()) {
                                    ArrayMap<NetworkTemplate, NetworkPolicy> arrayMap = this.mNetworkPolicy;
                                    NetworkPolicy networkPolicy = new NetworkPolicy(template, cycleRule, warningBytes, limitBytes, lastWarningSnooze, lastLimitSnooze, metered, inferred);
                                    arrayMap.put(template, networkPolicy);
                                }
                                version = version2;
                            } else {
                                metered2 = true;
                            }
                            metered = metered2;
                            if (version2 >= 5) {
                            }
                            if (version2 >= 7) {
                            }
                            template = new NetworkTemplate(networkTemplate, subscriberId, networkId);
                            if (template.isPersistable()) {
                            }
                            version = version2;
                        }
                        lastLimitSnooze = lastLimitSnooze2;
                        if (version2 < 4) {
                        }
                        metered = metered2;
                        if (version2 >= 5) {
                        }
                        if (version2 >= 7) {
                        }
                        template = new NetworkTemplate(networkTemplate, subscriberId, networkId);
                        if (template.isPersistable()) {
                        }
                        version = version2;
                    } else {
                        String str4 = str;
                        if (TAG_SUBSCRIPTION_PLAN.equals(tag)) {
                            String start2 = XmlUtils.readStringAttribute(in, ATTR_CYCLE_START);
                            String end2 = XmlUtils.readStringAttribute(in, ATTR_CYCLE_END);
                            String period = XmlUtils.readStringAttribute(in, ATTR_CYCLE_PERIOD);
                            SubscriptionPlan.Builder builder = new SubscriptionPlan.Builder(RecurrenceRule.convertZonedDateTime(start2), RecurrenceRule.convertZonedDateTime(end2), RecurrenceRule.convertPeriod(period));
                            builder.setTitle(XmlUtils.readStringAttribute(in, ATTR_TITLE));
                            builder.setSummary(XmlUtils.readStringAttribute(in, ATTR_SUMMARY));
                            long limitBytes2 = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES, -1);
                            int limitBehavior = XmlUtils.readIntAttribute(in, ATTR_LIMIT_BEHAVIOR, -1);
                            if (!(limitBytes2 == -1 || limitBehavior == -1)) {
                                builder.setDataLimit(limitBytes2, limitBehavior);
                            }
                            String str5 = end2;
                            version = version2;
                            long usageBytes = XmlUtils.readLongAttribute(in, ATTR_USAGE_BYTES, -1);
                            long usageTime = XmlUtils.readLongAttribute(in, ATTR_USAGE_TIME, -1);
                            long j = limitBytes2;
                            long usageBytes2 = usageBytes;
                            if (usageBytes2 != -1) {
                                int i = limitBehavior;
                                long usageTime2 = usageTime;
                                if (usageTime2 != -1) {
                                    builder.setDataUsage(usageBytes2, usageTime2);
                                }
                            } else {
                                long j2 = usageTime;
                            }
                            int subId = XmlUtils.readIntAttribute(in, ATTR_SUB_ID);
                            String str6 = start2;
                            String str7 = period;
                            SubscriptionPlan.Builder builder2 = builder;
                            this.mSubscriptionPlans.put(subId, (SubscriptionPlan[]) ArrayUtils.appendElement(SubscriptionPlan.class, this.mSubscriptionPlans.get(subId), builder.build()));
                            this.mSubscriptionPlansOwner.put(subId, XmlUtils.readStringAttribute(in, ATTR_OWNER_PACKAGE));
                        } else {
                            version = version2;
                            if (TAG_UID_POLICY.equals(tag)) {
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
                                z = true;
                            } else if (TAG_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                                whitelistedRestrictBackground.append(XmlUtils.readIntAttribute(in, "uid"), true);
                            } else if (TAG_REVOKED_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                                this.mRestrictBackgroundWhitelistRevokedUids.put(XmlUtils.readIntAttribute(in, "uid"), true);
                            }
                        }
                    }
                    version2 = version;
                    str = null;
                }
                insideWhitelist = z;
                version2 = version;
                str = null;
            }
            int i2 = version2;
            int size = whitelistedRestrictBackground.size();
            for (int i3 = 0; i3 < size; i3++) {
                int uid3 = whitelistedRestrictBackground.keyAt(i3);
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
        } catch (FileNotFoundException e) {
            upgradeDefaultBackgroundDataUL();
        } catch (Exception e2) {
            Log.wtf(TAG, "problem reading network policy", e2);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    private void upgradeDefaultBackgroundDataUL() {
        boolean z = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "default_restrict_background_data", 0) != 1) {
            z = false;
        }
        this.mLoadedRestrictBackground = z;
    }

    /* access modifiers changed from: private */
    public void upgradeWifiMeteredOverrideAL() {
        boolean modified = false;
        WifiManager wm = (WifiManager) this.mContext.getSystemService(WifiManager.class);
        List<WifiConfiguration> configs = wm.getConfiguredNetworks();
        int i = 0;
        while (i < this.mNetworkPolicy.size()) {
            NetworkPolicy policy = this.mNetworkPolicy.valueAt(i);
            if (policy.template.getMatchRule() != 4 || policy.inferred) {
                i++;
            } else {
                this.mNetworkPolicy.removeAt(i);
                modified = true;
                String networkId = NetworkPolicyManager.resolveNetworkId(policy.template.getNetworkId());
                for (WifiConfiguration config : configs) {
                    if (Objects.equals(NetworkPolicyManager.resolveNetworkId(config), networkId)) {
                        Slog.d(TAG, "Found network " + networkId + "; upgrading metered hint");
                        config.meteredOverride = policy.metered ? 1 : 2;
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
    public void writePolicyAL() {
        if (LOGV) {
            Slog.v(TAG, "writePolicyAL()");
        }
        try {
            FileOutputStream fos = this.mPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, 11);
            XmlUtils.writeBooleanAttribute(out, ATTR_RESTRICT_BACKGROUND, this.mRestrictBackground);
            for (int i = 0; i < this.mNetworkPolicy.size(); i++) {
                NetworkPolicy policy = this.mNetworkPolicy.valueAt(i);
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
                    XmlUtils.writeStringAttribute(out, ATTR_CYCLE_START, RecurrenceRule.convertZonedDateTime(policy.cycleRule.start));
                    XmlUtils.writeStringAttribute(out, ATTR_CYCLE_END, RecurrenceRule.convertZonedDateTime(policy.cycleRule.end));
                    XmlUtils.writeStringAttribute(out, ATTR_CYCLE_PERIOD, RecurrenceRule.convertPeriod(policy.cycleRule.period));
                    XmlUtils.writeLongAttribute(out, ATTR_WARNING_BYTES, policy.warningBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LIMIT_BYTES, policy.limitBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_WARNING_SNOOZE, policy.lastWarningSnooze);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_LIMIT_SNOOZE, policy.lastLimitSnooze);
                    XmlUtils.writeBooleanAttribute(out, ATTR_METERED, policy.metered);
                    XmlUtils.writeBooleanAttribute(out, ATTR_INFERRED, policy.inferred);
                    out.endTag(null, TAG_NETWORK_POLICY);
                }
            }
            for (int i2 = 0; i2 < this.mSubscriptionPlans.size(); i2++) {
                int subId = this.mSubscriptionPlans.keyAt(i2);
                String ownerPackage = this.mSubscriptionPlansOwner.get(subId);
                SubscriptionPlan[] plans = this.mSubscriptionPlans.valueAt(i2);
                if (!ArrayUtils.isEmpty(plans)) {
                    for (SubscriptionPlan plan : plans) {
                        out.startTag(null, TAG_SUBSCRIPTION_PLAN);
                        XmlUtils.writeIntAttribute(out, ATTR_SUB_ID, subId);
                        XmlUtils.writeStringAttribute(out, ATTR_OWNER_PACKAGE, ownerPackage);
                        RecurrenceRule cycleRule = plan.getCycleRule();
                        XmlUtils.writeStringAttribute(out, ATTR_CYCLE_START, RecurrenceRule.convertZonedDateTime(cycleRule.start));
                        XmlUtils.writeStringAttribute(out, ATTR_CYCLE_END, RecurrenceRule.convertZonedDateTime(cycleRule.end));
                        XmlUtils.writeStringAttribute(out, ATTR_CYCLE_PERIOD, RecurrenceRule.convertPeriod(cycleRule.period));
                        XmlUtils.writeStringAttribute(out, ATTR_TITLE, plan.getTitle());
                        XmlUtils.writeStringAttribute(out, ATTR_SUMMARY, plan.getSummary());
                        XmlUtils.writeLongAttribute(out, ATTR_LIMIT_BYTES, plan.getDataLimitBytes());
                        XmlUtils.writeIntAttribute(out, ATTR_LIMIT_BEHAVIOR, plan.getDataLimitBehavior());
                        XmlUtils.writeLongAttribute(out, ATTR_USAGE_BYTES, plan.getDataUsageBytes());
                        XmlUtils.writeLongAttribute(out, ATTR_USAGE_TIME, plan.getDataUsageTime());
                        out.endTag(null, TAG_SUBSCRIPTION_PLAN);
                    }
                }
            }
            for (int i3 = 0; i3 < this.mUidPolicy.size(); i3++) {
                int uid = this.mUidPolicy.keyAt(i3);
                int policy2 = this.mUidPolicy.valueAt(i3);
                if (policy2 != 0) {
                    out.startTag(null, TAG_UID_POLICY);
                    XmlUtils.writeIntAttribute(out, "uid", uid);
                    XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy2);
                    out.endTag(null, TAG_UID_POLICY);
                }
            }
            out.endTag(null, TAG_POLICY_LIST);
            out.startTag(null, TAG_WHITELIST);
            int size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            for (int i4 = 0; i4 < size; i4++) {
                int uid2 = this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i4);
                out.startTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, "uid", uid2);
                out.endTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
            }
            out.endTag(null, TAG_WHITELIST);
            out.endDocument();
            this.mPolicyFile.finishWrite(fos);
        } catch (IOException e) {
            if (0 != 0) {
                this.mPolicyFile.failWrite(null);
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

    private void normalizePoliciesNL() {
        normalizePoliciesNL(getNetworkPolicies(this.mContext.getOpPackageName()));
    }

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
                    if (type != 45) {
                        switch (type) {
                            case 34:
                                policy.lastWarningSnooze = currentTime;
                                break;
                            case 35:
                                policy.lastLimitSnooze = currentTime;
                                break;
                            default:
                                throw new IllegalArgumentException("unexpected type");
                        }
                    } else {
                        policy.lastRapidSnooze = currentTime;
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

    private void setRestrictBackgroundUL(boolean restrictBackground) {
        Trace.traceBegin(2097152, "setRestrictBackgroundUL");
        Log.i(TAG, "mRestrictBackground=" + this.mRestrictBackground + ",restrictBackground=" + restrictBackground);
        try {
            if (restrictBackground == this.mRestrictBackground) {
                Slog.w(TAG, "setRestrictBackgroundUL: already " + restrictBackground);
                Trace.traceEnd(2097152);
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
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
            throw th;
        }
    }

    private void sendRestrictBackgroundChangedMsg() {
        this.mHandler.removeMessages(6);
        this.mHandler.obtainMessage(6, this.mRestrictBackground ? 1 : 0, 0).sendToTarget();
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0036, code lost:
        return r5;
     */
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        if (r5 == false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        com.android.server.EventLogTags.writeDeviceIdleOnPhase("net");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0036, code lost:
        com.android.server.EventLogTags.writeDeviceIdleOffPhase("net");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        android.os.Trace.traceEnd(2097152);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        return;
     */
    public void setDeviceIdleMode(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        Trace.traceBegin(2097152, "setDeviceIdleMode");
        try {
            synchronized (this.mUidRulesFirstLock) {
                if (this.mDeviceIdleMode == enabled) {
                    Trace.traceEnd(2097152);
                    return;
                }
                this.mDeviceIdleMode = enabled;
                this.mLogger.deviceIdleModeEnabled(enabled);
                if (this.mSystemReady) {
                    updateRulesForRestrictPowerUL();
                }
            }
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
            throw th;
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
                    String testPackage = SystemProperties.get("persist.sys.sub_plan_owner." + subId, null);
                    if (TextUtils.isEmpty(testPackage) || !Objects.equals(testPackage, callingPackage)) {
                        String legacyTestPackage = SystemProperties.get("fw.sub_plan_owner." + subId, null);
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
        int i = subId;
        String str = callingPackage;
        enforceSubscriptionPlanAccess(i, Binder.getCallingUid(), str);
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
            String ownerPackage = this.mSubscriptionPlansOwner.get(i);
            if (!Objects.equals(ownerPackage, str)) {
                if (UserHandle.getCallingAppId() != 1000) {
                    Log.w(TAG, "Not returning plans because caller " + str + " doesn't match owner " + ownerPackage);
                    return null;
                }
            }
            SubscriptionPlan[] subscriptionPlanArr = this.mSubscriptionPlans.get(i);
            return subscriptionPlanArr;
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(16, overrideMask, overrideValue, Integer.valueOf(subId)));
            if (timeoutMillis > 0) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(16, overrideMask, 0, Integer.valueOf(subId)), timeoutMillis);
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
                        int subId = this.mSubscriptionPlans.keyAt(i3);
                        fout.println("Subscriber ID " + subId + ":");
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
                        int subId2 = this.mSubIdToSubscriberId.keyAt(i4);
                        fout.println(subId2 + "=" + NetworkIdentity.scrubSubscriberId(this.mSubIdToSubscriberId.valueAt(i4)));
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
                    int size4 = this.mDefaultRestrictBackgroundWhitelistUids.size();
                    if (size4 > 0) {
                        fout.println("Default restrict background whitelist uids:");
                        fout.increaseIndent();
                        for (int i8 = 0; i8 < size4; i8++) {
                            fout.print("UID=");
                            fout.print(this.mDefaultRestrictBackgroundWhitelistUids.keyAt(i8));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    int size5 = this.mRestrictBackgroundWhitelistRevokedUids.size();
                    if (size5 > 0) {
                        fout.println("Default restrict background whitelist uids revoked by users:");
                        fout.increaseIndent();
                        for (int i9 = 0; i9 < size5; i9++) {
                            fout.print("UID=");
                            fout.print(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i9));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    SparseBooleanArray knownUids = new SparseBooleanArray();
                    collectKeys(this.mUidState, knownUids);
                    collectKeys(this.mUidRules, knownUids);
                    fout.println("Status for all known UIDs:");
                    fout.increaseIndent();
                    int size6 = knownUids.size();
                    for (int i10 = 0; i10 < size6; i10++) {
                        int uid2 = knownUids.keyAt(i10);
                        fout.print("UID=");
                        fout.print(uid2);
                        int state = this.mUidState.get(uid2, 18);
                        fout.print(" state=");
                        fout.print(state);
                        if (state <= 2) {
                            fout.print(" (fg)");
                        } else {
                            fout.print(state <= 4 ? " (fg svc)" : " (bg)");
                        }
                        int uidRules = this.mUidRules.get(uid2, 0);
                        fout.print(" rules=");
                        fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    fout.println("Status for just UIDs with rules:");
                    fout.increaseIndent();
                    int size7 = this.mUidRules.size();
                    for (int i11 = 0; i11 < size7; i11++) {
                        int uid3 = this.mUidRules.keyAt(i11);
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
                    int size8 = this.mMeteredRestrictedUids.size();
                    for (int i12 = 0; i12 < size8; i12++) {
                        fout.print("u" + this.mMeteredRestrictedUids.keyAt(i12) + ": ");
                        fout.println(this.mMeteredRestrictedUids.valueAt(i12));
                    }
                    fout.decreaseIndent();
                    fout.println();
                    this.mStatLogger.dump(fout);
                    this.mLogger.dumpLogs(fout);
                }
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new NetworkPolicyManagerShellCommand(this.mContext, this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    @VisibleForTesting
    public boolean isUidForeground(int uid) {
        boolean isUidStateForeground;
        synchronized (this.mUidRulesFirstLock) {
            isUidStateForeground = isUidStateForeground(this.mUidState.get(uid, 18));
        }
        return isUidStateForeground;
    }

    private boolean isUidForegroundOnRestrictBackgroundUL(int uid) {
        return NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(this.mUidState.get(uid, 18));
    }

    private boolean isUidForegroundOnRestrictPowerUL(int uid) {
        return NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 18));
    }

    private boolean isUidStateForeground(int state) {
        return state <= 4;
    }

    private void updateUidStateUL(int uid, int uidState) {
        Trace.traceBegin(2097152, "updateUidStateUL");
        try {
            int oldUidState = this.mUidState.get(uid, 18);
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
                updateNetworkStats(uid, isUidStateForeground(uidState));
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void removeUidStateUL(int uid) {
        int index = this.mUidState.indexOfKey(uid);
        if (index >= 0) {
            int oldUidState = this.mUidState.valueAt(index);
            this.mUidState.removeAt(index);
            if (oldUidState != 18) {
                updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, 18);
                if (this.mDeviceIdleMode) {
                    updateRuleForDeviceIdleUL(uid);
                }
                if (this.mRestrictPower) {
                    updateRuleForRestrictPowerUL(uid);
                }
                updateRulesForPowerRestrictionsUL(uid);
                updateNetworkStats(uid, false);
            }
        }
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
    public void updateRulesForPowerSaveUL() {
        Trace.traceBegin(2097152, "updateRulesForPowerSaveUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mRestrictPower, 3, this.mUidFirewallPowerSaveRules);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRuleForRestrictPowerUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mRestrictPower, 3);
    }

    /* access modifiers changed from: package-private */
    public void updateRulesForDeviceIdleUL() {
        Trace.traceBegin(2097152, "updateRulesForDeviceIdleUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mDeviceIdleMode, 1, this.mUidFirewallDozableRules);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRuleForDeviceIdleUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mDeviceIdleMode, 1);
    }

    private void updateRulesForWhitelistedPowerSaveUL(boolean enabled, int chain, SparseIntArray rules) {
        if (enabled) {
            SparseIntArray uidRules = rules;
            uidRules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                UserInfo user = users.get(ui);
                updateRulesForWhitelistedAppIds(uidRules, this.mPowerSaveTempWhitelistAppIds, user.id);
                updateRulesForWhitelistedAppIds(uidRules, this.mPowerSaveWhitelistAppIds, user.id);
                if (chain == 3) {
                    updateRulesForWhitelistedAppIds(uidRules, this.mPowerSaveWhitelistExceptIdleAppIds, user.id);
                }
            }
            for (int i = this.mUidState.size() - 1; i >= 0; i--) {
                if (NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.valueAt(i))) {
                    uidRules.put(this.mUidState.keyAt(i), 1);
                }
            }
            setUidFirewallRulesUL(chain, uidRules, 1);
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

    private boolean isWhitelistedBatterySaverUL(int uid, boolean deviceIdleMode) {
        int appId = UserHandle.getAppId(uid);
        boolean isWhitelisted = true;
        boolean isWhitelisted2 = this.mPowerSaveTempWhitelistAppIds.get(appId) || this.mPowerSaveWhitelistAppIds.get(appId);
        if (deviceIdleMode) {
            return isWhitelisted2;
        }
        if (!isWhitelisted2 && !this.mPowerSaveWhitelistExceptIdleAppIds.get(appId)) {
            isWhitelisted = false;
        }
        return isWhitelisted;
    }

    private void updateRulesForWhitelistedPowerSaveUL(int uid, boolean enabled, int chain) {
        if (enabled) {
            if (isWhitelistedBatterySaverUL(uid, chain == 1) || isUidForegroundOnRestrictPowerUL(uid)) {
                setUidFirewallRule(chain, uid, 1);
            } else {
                setUidFirewallRule(chain, uid, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRulesForAppIdleUL() {
        Trace.traceBegin(2097152, "updateRulesForAppIdleUL");
        try {
            SparseIntArray uidRules = this.mUidFirewallStandbyRules;
            uidRules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                for (int uid : this.mUsageStats.getIdleUidsForUser(users.get(ui).id)) {
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
    public void updateRuleForAppIdleUL(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            if (Trace.isTagEnabled(2097152)) {
                Trace.traceBegin(2097152, "updateRuleForAppIdleUL: " + uid);
            }
            try {
                if (this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid)) || !isUidIdle(uid) || isUidForegroundOnRestrictPowerUL(uid)) {
                    setUidFirewallRule(2, uid, 0);
                } else {
                    setUidFirewallRule(2, uid, 2);
                }
            } finally {
                Trace.traceEnd(2097152);
            }
        }
    }

    /* access modifiers changed from: package-private */
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
    public void updateRulesForGlobalChangeAL(boolean restrictedNetworksChanged) {
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
                normalizePoliciesNL();
                updateNetworkRulesNL();
                syncMeteredIfacesToReplicaNL();
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    /* access modifiers changed from: private */
    public void updateRulesForRestrictPowerUL() {
        Trace.traceBegin(2097152, "updateRulesForRestrictPowerUL");
        try {
            updateRulesForDeviceIdleUL();
            updateRulesForPowerSaveUL();
            updateRulesForAllAppsUL(2);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void updateRulesForRestrictBackgroundUL() {
        Trace.traceBegin(2097152, "updateRulesForRestrictBackgroundUL");
        try {
            updateRulesForAllAppsUL(1);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void updateRulesForAllAppsUL(int type) {
        int i = type;
        if (Trace.isTagEnabled(2097152)) {
            Trace.traceBegin(2097152, "updateRulesForRestrictPowerUL-" + i);
        }
        try {
            PackageManager pm = this.mContext.getPackageManager();
            Trace.traceBegin(2097152, "list-users");
            List users = this.mUserManager.getUsers();
            Trace.traceEnd(2097152);
            Trace.traceBegin(2097152, "list-uids");
            List<ApplicationInfo> apps = pm.getInstalledApplications(4981248);
            Trace.traceEnd(2097152);
            int usersSize = users.size();
            int appsSize = apps.size();
            for (int i2 = 0; i2 < usersSize; i2++) {
                UserInfo user = (UserInfo) users.get(i2);
                for (int j = 0; j < appsSize; j++) {
                    int uid = UserHandle.getUid(user.id, apps.get(j).uid);
                    switch (i) {
                        case 1:
                            updateRulesForDataUsageRestrictionsUL(uid);
                            break;
                        case 2:
                            updateRulesForPowerRestrictionsUL(uid);
                            break;
                        default:
                            Slog.w(TAG, "Invalid type for updateRulesForAllApps: " + i);
                            break;
                    }
                }
            }
            Trace.traceEnd(2097152);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void updateRulesForTempWhitelistChangeUL(int appId) {
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
        if (uid == 1013 || uid == 1019 || (UserHandle.isApp(uid) && hasInternetPermissions(uid))) {
            return true;
        }
        return false;
    }

    private boolean isUidValidForWhitelistRules(int uid) {
        return UserHandle.isApp(uid) && hasInternetPermissions(uid);
    }

    private boolean isUidIdle(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        int userId = UserHandle.getUserId(uid);
        if (packages != null) {
            for (String packageName : packages) {
                if (!this.mUsageStats.isAppIdle(packageName, uid, userId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasInternetPermissions(int uid) {
        try {
            if (this.mIPm.checkUidPermission("android.permission.INTERNET", uid) != 0) {
                return false;
            }
        } catch (RemoteException e) {
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void onUidDeletedUL(int uid) {
        this.mUidRules.delete(uid);
        this.mUidPolicy.delete(uid);
        this.mUidFirewallStandbyRules.delete(uid);
        this.mUidFirewallDozableRules.delete(uid);
        this.mUidFirewallPowerSaveRules.delete(uid);
        this.mPowerSaveWhitelistExceptIdleAppIds.delete(uid);
        this.mPowerSaveWhitelistAppIds.delete(uid);
        this.mPowerSaveTempWhitelistAppIds.delete(uid);
        this.mHandler.obtainMessage(15, uid, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void updateRestrictionRulesForUidUL(int uid) {
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
        if (!isUidValidForWhitelistRules(uid)) {
            if (LOGD) {
                Slog.d(TAG, "no need to update restrict data rules for uid " + uid);
            }
            return;
        }
        boolean z = false;
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
        int newUidRules = (oldUidRules & 240) | newRule;
        if (LOGV) {
            Log.v(TAG, "updateRuleForRestrictBackgroundUL(" + uid + "): isForeground=" + isForeground + ", isBlacklisted=" + isBlacklisted + ", isWhitelisted=" + isWhitelisted + ", isRestrictedByAdmin=" + isRestrictedByAdmin + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
        }
        if (newUidRules == 0) {
            this.mUidRules.delete(uid);
        } else {
            this.mUidRules.put(uid, newUidRules);
        }
        if (newRule != oldRule) {
            Log.i(TAG, "updateRuleForRestrictBackgroundUL(" + uid + "): isForeground=" + isForeground + ", isBlacklisted=" + isBlacklisted + ", isWhitelisted=" + isWhitelisted + ", isRestrictedByAdmin=" + isRestrictedByAdmin + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules) + ", mRestrictBackground=" + this.mRestrictBackground);
            if (hasRule(newRule, 2)) {
                setMeteredNetworkWhitelist(uid, true);
                if (isBlacklisted) {
                    setMeteredNetworkBlacklist(uid, false);
                }
            } else if (hasRule(oldRule, 2)) {
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
                Log.wtf(TAG, "Unexpected change of metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", blacklisted=" + isBlacklisted + ", isRestrictedByAdmin=" + isRestrictedByAdmin + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
            }
            this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void updateRulesForPowerRestrictionsUL(int uid) {
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
        int i = uid;
        int i2 = oldUidRules;
        boolean restrictMode = false;
        if (!isUidValidForBlacklistRules(uid)) {
            if (LOGD) {
                Slog.d(TAG, "no need to update restrict power rules for uid " + i);
            }
            return 0;
        }
        boolean isIdle = !paroled && isUidIdle(uid);
        if (isIdle || this.mRestrictPower || this.mDeviceIdleMode) {
            restrictMode = true;
        }
        boolean isForeground = isUidForegroundOnRestrictPowerUL(uid);
        boolean isWhitelisted = isWhitelistedBatterySaverUL(i, this.mDeviceIdleMode);
        int oldRule = i2 & 240;
        int newRule = 0;
        if (isForeground) {
            if (restrictMode) {
                newRule = 32;
            }
        } else if (restrictMode) {
            newRule = isWhitelisted ? 32 : 64;
        }
        int newUidRules = (i2 & 15) | newRule;
        if (LOGV) {
            Log.v(TAG, "updateRulesForPowerRestrictionsUL(" + i + "), isIdle: " + isIdle + ", mRestrictPower: " + this.mRestrictPower + ", mDeviceIdleMode: " + this.mDeviceIdleMode + ", isForeground=" + isForeground + ", isWhitelisted=" + isWhitelisted + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
        }
        if (newRule != oldRule) {
            Log.i(TAG, "updateRulesForPowerRestrictionsUL(" + i + "), isIdle: " + isIdle + ", mRestrictPower: " + this.mRestrictPower + ", mDeviceIdleMode: " + this.mDeviceIdleMode + ", isForeground=" + isForeground + ", isWhitelisted=" + isWhitelisted + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldRule) + ", newRule=" + NetworkPolicyManager.uidRulesToString(newRule) + ", newUidRules=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldUidRules=" + NetworkPolicyManager.uidRulesToString(oldUidRules) + ", mRestrictBackground=" + this.mRestrictBackground);
            if (newRule == 0 || hasRule(newRule, 32)) {
                if (LOGV) {
                    Log.v(TAG, "Allowing non-metered access for UID " + i);
                }
            } else if (!hasRule(newRule, 64)) {
                Log.wtf(TAG, "Unexpected change of non-metered UID state for " + i + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
            } else if (LOGV) {
                Log.v(TAG, "Rejecting non-metered access for UID " + i);
            }
            this.mHandler.obtainMessage(1, i, newUidRules).sendToTarget();
        }
        return newUidRules;
    }

    /* access modifiers changed from: private */
    public void dispatchUidRulesChanged(INetworkPolicyListener listener, int uid, int uidRules) {
        if (listener != null) {
            try {
                listener.onUidRulesChanged(uid, uidRules);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchMeteredIfacesChanged(INetworkPolicyListener listener, String[] meteredIfaces) {
        if (listener != null) {
            try {
                listener.onMeteredIfacesChanged(meteredIfaces);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchRestrictBackgroundChanged(INetworkPolicyListener listener, boolean restrictBackground) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundChanged(restrictBackground);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchUidPoliciesChanged(INetworkPolicyListener listener, int uid, int uidPolicies) {
        if (listener != null) {
            try {
                listener.onUidPoliciesChanged(uid, uidPolicies);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchSubscriptionOverride(INetworkPolicyListener listener, int subId, int overrideMask, int overrideValue) {
        if (listener != null) {
            try {
                listener.onSubscriptionOverride(subId, overrideMask, overrideValue);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidChanged(int uid, int procState, long procStateSeq) {
        Trace.traceBegin(2097152, "onUidStateChanged");
        try {
            synchronized (this.mUidRulesFirstLock) {
                this.mLogger.uidStateChanged(uid, procState, procStateSeq);
                updateUidStateUL(uid, procState);
                this.mActivityManagerInternal.notifyNetworkPolicyRulesUpdated(uid, procStateSeq);
            }
            Trace.traceEnd(2097152);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidGone(int uid) {
        Trace.traceBegin(2097152, "onUidGone");
        try {
            synchronized (this.mUidRulesFirstLock) {
                removeUidStateUL(uid);
            }
            Trace.traceEnd(2097152);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void broadcastRestrictBackgroundChanged(int uid, Boolean changed) {
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
    public void setInterfaceQuota(String iface, long quotaBytes) {
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

    private void removeInterfaceQuotaAsync(String iface) {
        this.mHandler.obtainMessage(11, iface).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void removeInterfaceQuota(String iface) {
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
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem setting firewall uid rules", e);
            } catch (RemoteException e2) {
            } catch (Throwable th) {
                Trace.traceEnd(2097152);
                throw th;
            }
        } else if (chain == 2) {
            this.mUidFirewallStandbyRules.put(uid, rule);
        } else if (chain == 3) {
            this.mUidFirewallPowerSaveRules.put(uid, rule);
        }
        this.mNetworkManager.setFirewallUidRule(chain, uid, rule);
        this.mLogger.uidFirewallRuleChanged(chain, uid, rule);
        Trace.traceEnd(2097152);
    }

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
    public void resetUidFirewallRules(int uid) {
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
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildSnoozeRapidIntent(NetworkTemplate template) {
        Intent intent = new Intent(ACTION_SNOOZE_RAPID);
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildNetworkOverLimitIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039834)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildViewDataUsageIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039779)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    @VisibleForTesting
    public void addIdleHandler(MessageQueue.IdleHandler handler) {
        this.mHandler.getLooper().getQueue().addIdleHandler(handler);
    }

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
        long startTime = this.mStatLogger.getTime();
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        boolean ret = isUidNetworkingBlockedInternal(uid, isNetworkMetered);
        this.mStatLogger.logDurationStat(1, startTime);
        return ret;
    }

    /* access modifiers changed from: private */
    public boolean isUidNetworkingBlockedInternal(int uid, boolean isNetworkMetered) {
        int uidRules;
        boolean isBackgroundRestricted;
        synchronized (this.mUidRulesFirstLock) {
            uidRules = this.mUidRules.get(uid, 0);
            isBackgroundRestricted = this.mRestrictBackground;
        }
        if (hasRule(uidRules, 64)) {
            this.mLogger.networkBlocked(uid, 0);
            return true;
        } else if (!isNetworkMetered) {
            this.mLogger.networkBlocked(uid, 1);
            return false;
        } else if (hasRule(uidRules, 4)) {
            this.mLogger.networkBlocked(uid, 2);
            return true;
        } else if (hasRule(uidRules, 1)) {
            this.mLogger.networkBlocked(uid, 3);
            return false;
        } else if (hasRule(uidRules, 2)) {
            this.mLogger.networkBlocked(uid, 4);
            return false;
        } else if (isBackgroundRestricted) {
            this.mLogger.networkBlocked(uid, 5);
            return true;
        } else {
            this.mLogger.networkBlocked(uid, 6);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void syncMeteredIfacesToReplicaNL() {
        if (!this.mMeteredIfaces.equals(this.mMeteredIfacesReplica)) {
            synchronized (this.mNetworkPoliciesReplicaSecondLock) {
                this.mMeteredIfacesReplica.clear();
                this.mMeteredIfacesReplica.addAll(this.mMeteredIfaces);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setMeteredRestrictedPackagesInternal(Set<String> packageNames, int userId) {
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
        StringNetworkSpecifier networkSpecifier = state.networkCapabilities.getNetworkSpecifier();
        if (!(networkSpecifier instanceof StringNetworkSpecifier)) {
            return -1;
        }
        try {
            return Integer.parseInt(networkSpecifier.specifier);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPoliciesSecondLock")
    public int getSubIdLocked(Network network) {
        return this.mNetIdToSubId.get(network.netId, -1);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPoliciesSecondLock")
    public SubscriptionPlan getPrimarySubscriptionPlanLocked(int subId) {
        SubscriptionPlan[] plans = this.mSubscriptionPlans.get(subId);
        if (!ArrayUtils.isEmpty(plans)) {
            int length = plans.length;
            for (int i = 0; i < length; i++) {
                SubscriptionPlan plan = plans[i];
                if (plan.getCycleRule().isRecurring() || plan.cycleIterator().next().contains(ZonedDateTime.now(this.mClock))) {
                    return plan;
                }
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
        if (oldRestrictedUids == null) {
            for (Integer intValue : newRestrictedUids) {
                updateRulesForDataUsageRestrictionsUL(intValue.intValue());
            }
            return;
        }
        for (Integer intValue2 : oldRestrictedUids) {
            int uid = intValue2.intValue();
            if (!newRestrictedUids.contains(Integer.valueOf(uid))) {
                updateRulesForDataUsageRestrictionsUL(uid);
            }
        }
        for (Integer intValue3 : newRestrictedUids) {
            int uid2 = intValue3.intValue();
            if (!oldRestrictedUids.contains(Integer.valueOf(uid2))) {
                updateRulesForDataUsageRestrictionsUL(uid2);
            }
        }
    }

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

    private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        if (this.mHwBehaviorManager == null) {
            this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
        }
        if (this.mHwBehaviorManager != null) {
            try {
                this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
            } catch (Exception e) {
                Log.e(TAG, "sendBehavior:");
            }
        } else {
            Log.w(TAG, "HwBehaviorCollectManager is null");
        }
    }
}
