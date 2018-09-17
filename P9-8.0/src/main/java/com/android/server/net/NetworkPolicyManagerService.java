package com.android.server.net;

import android.annotation.IntDef;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.IUidObserver;
import android.app.Notification.BigTextStyle;
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
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.PersistableBundle;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
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
import android.provider.Settings.Secure;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TrustedTime;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocalServices;
import com.android.server.NetPluginDelegate;
import com.android.server.NetworkManagementService;
import com.android.server.ServiceThread;
import com.android.server.SystemConfig;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.display.DisplayTransformManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.google.android.collect.Lists;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
    private static final int CHAIN_TOGGLE_DISABLE = 2;
    private static final int CHAIN_TOGGLE_ENABLE = 1;
    private static final int CHAIN_TOGGLE_NONE = 0;
    private static final boolean GOOGLE_WARNING_DISABLED = true;
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final boolean LOGD = false;
    private static final boolean LOGV = false;
    public static final int MAX_PROC_STATE_SEQ_HISTORY = (ActivityManager.isLowRamDeviceStatic() ? 50 : DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE);
    private static final int MSG_ADVISE_PERSIST_THRESHOLD = 7;
    private static final int MSG_LIMIT_REACHED = 5;
    private static final int MSG_METERED_IFACES_CHANGED = 2;
    private static final int MSG_POLICIES_CHANGED = 13;
    private static final int MSG_REMOVE_INTERFACE_QUOTA = 11;
    private static final int MSG_RESET_FIREWALL_RULES_BY_UID = 15;
    private static final int MSG_RESTRICT_BACKGROUND_CHANGED = 6;
    private static final int MSG_RULES_CHANGED = 1;
    private static final int MSG_SET_FIREWALL_RULES = 14;
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
    public static final int TYPE_LIMIT = 35;
    public static final int TYPE_LIMIT_SNOOZED = 36;
    private static final int TYPE_RESTRICT_BACKGROUND = 1;
    private static final int TYPE_RESTRICT_POWER = 2;
    public static final int TYPE_WARNING = 34;
    private static final int UID_MSG_GONE = 101;
    private static final int UID_MSG_STATE_CHANGED = 100;
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
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final ArraySet<NotificationId> mActiveNotifs;
    private final IActivityManager mActivityManager;
    private ActivityManagerInternal mActivityManagerInternal;
    private final INetworkManagementEventObserver mAlertObserver;
    private final BroadcastReceiver mAllowReceiver;
    private final AppOpsManager mAppOps;
    private final CarrierConfigManager mCarrierConfigManager;
    private BroadcastReceiver mCarrierConfigReceiver;
    private IConnectivityManager mConnManager;
    private BroadcastReceiver mConnReceiver;
    private final Context mContext;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mDefaultRestrictBackgroundWhitelistUids;
    private IDeviceIdleController mDeviceIdleController;
    @GuardedBy("mUidRulesFirstLock")
    volatile boolean mDeviceIdleMode;
    @GuardedBy("mUidRulesFirstLock")
    final SparseBooleanArray mFirewallChainStates;
    final Handler mHandler;
    private final Callback mHandlerCallback;
    private final IPackageManager mIPm;
    private final RemoteCallbackList<INetworkPolicyListener> mListeners;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private ArraySet<String> mMeteredIfaces;
    private final INetworkManagementService mNetworkManager;
    final Object mNetworkPoliciesSecondLock;
    final ArrayMap<NetworkTemplate, NetworkPolicy> mNetworkPolicy;
    final ArrayMap<NetworkPolicy, String[]> mNetworkRules;
    private final INetworkStatsService mNetworkStats;
    private INotificationManager mNotifManager;
    public ProcStateSeqHistory mObservedHistory;
    @GuardedBy("mNetworkPoliciesSecondLock")
    private final ArraySet<NetworkTemplate> mOverLimitNotified;
    private final BroadcastReceiver mPackageReceiver;
    @GuardedBy("allLocks")
    private final AtomicFile mPolicyFile;
    private PowerManagerInternal mPowerManagerInternal;
    @GuardedBy("mUidRulesFirstLock")
    private final SparseBooleanArray mPowerSaveTempWhitelistAppIds;
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
    private final BroadcastReceiver mSnoozeWarningReceiver;
    private final BroadcastReceiver mStatsReceiver;
    private final boolean mSuppressDefaultPolicy;
    @GuardedBy("allLocks")
    volatile boolean mSystemReady;
    private final Runnable mTempPowerSaveChangedCallback;
    private final TrustedTime mTime;
    private long mTimeRefreshRealtime;
    public final Handler mUidEventHandler;
    private final Callback mUidEventHandlerCallback;
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
    private final BroadcastReceiver mWifiConfigReceiver;
    private final BroadcastReceiver mWifiStateReceiver;

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        /* synthetic */ AppIdleStateChangeListener(NetworkPolicyManagerService this$0, AppIdleStateChangeListener -this1) {
            this();
        }

        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            try {
                int uid = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackageUidAsUser(packageName, 8192, userId);
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updateRuleForAppIdleUL(uid);
                    NetworkPolicyManagerService.this.updateRulesForPowerRestrictionsUL(uid);
                }
            } catch (NameNotFoundException e) {
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                NetworkPolicyManagerService.this.updateRulesForAppIdleParoleUL();
            }
        }
    }

    @IntDef(flag = false, value = {0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChainToggleType {
    }

    private class NetworkPolicyManagerInternalImpl extends NetworkPolicyManagerInternal {
        /* synthetic */ NetworkPolicyManagerInternalImpl(NetworkPolicyManagerService this$0, NetworkPolicyManagerInternalImpl -this1) {
            this();
        }

        private NetworkPolicyManagerInternalImpl() {
        }

        public void resetUserState(int userId) {
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                boolean changed = NetworkPolicyManagerService.this.removeUserStateUL(userId, false);
                if (NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId)) {
                    changed = true;
                }
                if (changed) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.writePolicyAL();
                    }
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
            if (!isBackgroundRestricted || (NetworkPolicyManagerService.hasRule(uidRules, 1) ^ 1) == 0) {
                return false;
            }
            return NetworkPolicyManagerService.hasRule(uidRules, 2) ^ 1;
        }

        public boolean isUidNetworkingBlocked(int uid, String ifname) {
            int uidRules;
            boolean isBackgroundRestricted;
            boolean isNetworkMetered;
            synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                uidRules = NetworkPolicyManagerService.this.mUidRules.get(uid, 0);
                isBackgroundRestricted = NetworkPolicyManagerService.this.mRestrictBackground;
                synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                    isNetworkMetered = NetworkPolicyManagerService.this.mMeteredIfaces.contains(ifname);
                }
            }
            if (NetworkPolicyManagerService.hasRule(uidRules, 64)) {
                return true;
            }
            if (!isNetworkMetered) {
                return false;
            }
            if (NetworkPolicyManagerService.hasRule(uidRules, 4)) {
                return true;
            }
            return (NetworkPolicyManagerService.hasRule(uidRules, 1) || NetworkPolicyManagerService.hasRule(uidRules, 2) || !isBackgroundRestricted) ? false : true;
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

    public static final class ProcStateSeqHistory {
        private static final int INVALID_UID = -1;
        private int mHistoryNext;
        private final int mMaxCapacity;
        private final long[] mProcStateSeqs;
        private final int[] mUids = new int[this.mMaxCapacity];

        public ProcStateSeqHistory(int maxCapacity) {
            this.mMaxCapacity = maxCapacity;
            Arrays.fill(this.mUids, -1);
            this.mProcStateSeqs = new long[this.mMaxCapacity];
        }

        @GuardedBy("mUidRulesFirstLock")
        public void addProcStateSeqUL(int uid, long procStateSeq) {
            this.mUids[this.mHistoryNext] = uid;
            this.mProcStateSeqs[this.mHistoryNext] = procStateSeq;
            this.mHistoryNext = increaseNext(this.mHistoryNext, 1);
        }

        @GuardedBy("mUidRulesFirstLock")
        public void dumpUL(IndentingPrintWriter fout) {
            if (this.mUids[0] == -1) {
                fout.println("NONE");
                return;
            }
            int index = this.mHistoryNext;
            while (true) {
                index = increaseNext(index, -1);
                if (this.mUids[index] != -1) {
                    fout.println(getString(this.mUids[index], this.mProcStateSeqs[index]));
                    if (index == this.mHistoryNext) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        public static String getString(int uid, long procStateSeq) {
            return "UID=" + uid + " procStateSeq=" + procStateSeq;
        }

        private int increaseNext(int next, int increment) {
            next += increment;
            if (next >= this.mMaxCapacity) {
                return 0;
            }
            if (next < 0) {
                return this.mMaxCapacity - 1;
            }
            return next;
        }
    }

    @IntDef(flag = false, value = {1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RestrictType {
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
        this(context, activityManager, networkStats, networkManagement, AppGlobals.getPackageManager(), NtpTrustedTime.getInstance(context), getSystemDir(), false);
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    private static boolean isNeedShowWarning() {
        return IS_DOCOMO ? IS_TABLET : false;
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, INetworkStatsService networkStats, INetworkManagementService networkManagement, IPackageManager pm, TrustedTime time, File systemDir, boolean suppressDefaultPolicy) {
        this.mUidRulesFirstLock = new Object();
        this.mNetworkPoliciesSecondLock = new Object();
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
        this.mDefaultRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistRevokedUids = new SparseBooleanArray();
        this.mMeteredIfaces = new ArraySet();
        this.mOverLimitNotified = new ArraySet();
        this.mActiveNotifs = new ArraySet();
        this.mUidState = new SparseIntArray();
        this.mListeners = new RemoteCallbackList();
        this.mObservedHistory = new ProcStateSeqHistory(MAX_PROC_STATE_SEQ_HISTORY);
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
                NetworkPolicyManagerService.this.mUidEventHandler.obtainMessage(100, uid, procState, Long.valueOf(procStateSeq)).sendToTarget();
            }

            public void onUidGone(int uid, boolean disabled) throws RemoteException {
                NetworkPolicyManagerService.this.mUidEventHandler.obtainMessage(101, uid, 0).sendToTarget();
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid, boolean disabled) throws RemoteException {
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
        this.mTempPowerSaveChangedCallback = new Runnable() {
            public void run() {
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveTempWhitelistUL();
                    NetworkPolicyManagerService.this.updateRulesForTempWhitelistChangeUL();
                    NetworkPolicyManagerService.this.purgePowerSaveTempWhitelistUL();
                }
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1 && "android.intent.action.PACKAGE_ADDED".equals(action)) {
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
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId != -1) {
                    if (action.equals("android.intent.action.USER_REMOVED") || action.equals("android.intent.action.USER_ADDED")) {
                        synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                            NetworkPolicyManagerService.this.removeUserStateUL(userId, true);
                            if (action == "android.intent.action.USER_ADDED") {
                                NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsUL(userId);
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
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
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
        this.mSnoozeWarningReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.performSnooze((NetworkTemplate) intent.getParcelableExtra("android.net.NETWORK_TEMPLATE"), 34);
            }
        };
        this.mWifiConfigReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("changeReason", 0) == 1) {
                    WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (config.SSID != null) {
                        NetworkTemplate template = NetworkTemplate.buildTemplateWifi(config.SSID);
                        synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                            synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                                if (NetworkPolicyManagerService.this.mNetworkPolicy.containsKey(template)) {
                                    NetworkPolicyManagerService.this.mNetworkPolicy.remove(template);
                                    NetworkPolicyManagerService.this.writePolicyAL();
                                }
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
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            NetworkPolicy policy = (NetworkPolicy) NetworkPolicyManagerService.this.mNetworkPolicy.get(template);
                            if (policy == null && meteredHint) {
                                NetworkPolicyManagerService.this.addNetworkPolicyAL(NetworkPolicyManagerService.newWifiPolicy(template, meteredHint));
                            } else if (policy != null) {
                                if (policy.inferred) {
                                    policy.metered = meteredHint;
                                    NetworkPolicyManagerService.this.updateNetworkRulesNL();
                                }
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
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(5, iface).sendToTarget();
                }
            }
        };
        this.mConnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                    synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                        NetworkPolicyManagerService.this.ensureActiveMobilePolicyAL();
                        NetworkPolicyManagerService.this.normalizePoliciesNL();
                        NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                        NetworkPolicyManagerService.this.updateNetworkRulesNL();
                        NetworkPolicyManagerService.this.updateNotificationsNL();
                    }
                }
            }
        };
        this.mCarrierConfigReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("subscription")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    String subscriberId = TelephonyManager.from(NetworkPolicyManagerService.this.mContext).getSubscriberId(subId);
                    NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                    synchronized (NetworkPolicyManagerService.this.mUidRulesFirstLock) {
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            if (NetworkPolicyManagerService.this.ensureActiveMobilePolicyAL(subId, subscriberId)) {
                            } else if (NetworkPolicyManagerService.this.maybeUpdateMobilePolicyCycleNL(subId)) {
                                NetworkPolicyManagerService.this.handleNetworkPoliciesUpdateAL(true);
                            }
                        }
                    }
                }
            }
        };
        this.mHandlerCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                int uid;
                int length;
                int i;
                switch (msg.what) {
                    case 1:
                        uid = msg.arg1;
                        int uidRules = msg.arg2;
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchUidRulesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, uidRules);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 2:
                        String[] meteredIfaces = msg.obj;
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), meteredIfaces);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 5:
                        String iface = msg.obj;
                        NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                        synchronized (NetworkPolicyManagerService.this.mNetworkPoliciesSecondLock) {
                            if (NetworkPolicyManagerService.this.mMeteredIfaces.contains(iface)) {
                                try {
                                    NetworkPolicyManagerService.this.mNetworkStats.forceUpdate();
                                } catch (RemoteException e) {
                                }
                                NetworkPolicyManagerService.this.updateNetworkEnabledNL();
                                NetworkPolicyManagerService.this.updateNotificationsNL();
                            }
                        }
                        return true;
                    case 6:
                        boolean restrictBackground = msg.arg1 != 0;
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), restrictBackground);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        Intent intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                        intent.setFlags(1073741824);
                        NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                        return true;
                    case 7:
                        try {
                            NetworkPolicyManagerService.this.mNetworkStats.advisePersistThreshold(((Long) msg.obj).longValue() / 1000);
                        } catch (RemoteException e2) {
                        }
                        return true;
                    case 10:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        NetworkPolicyManagerService.this.setInterfaceQuota((String) msg.obj, (((long) msg.arg1) << 32) | (((long) msg.arg2) & 4294967295L));
                        return true;
                    case 11:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        return true;
                    case 13:
                        uid = msg.arg1;
                        int policy = msg.arg2;
                        Boolean notifyApp = msg.obj;
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchUidPoliciesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, policy);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        if (notifyApp.booleanValue()) {
                            NetworkPolicyManagerService.this.broadcastRestrictBackgroundChanged(uid, notifyApp);
                        }
                        return true;
                    case 14:
                        int chain = msg.arg1;
                        int toggle = msg.arg2;
                        SparseIntArray uidRules2 = msg.obj;
                        if (uidRules2 != null) {
                            NetworkPolicyManagerService.this.setUidFirewallRules(chain, uidRules2);
                        }
                        if (toggle != 0) {
                            NetworkPolicyManagerService.this.enableFirewallChainUL(chain, toggle == 1);
                        }
                        return true;
                    case 15:
                        NetworkPolicyManagerService.this.resetUidFirewallRules(msg.arg1);
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.mUidEventHandlerCallback = new Callback() {
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
        this.mNetworkStats = (INetworkStatsService) Preconditions.checkNotNull(networkStats, "missing networkStats");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManagement, "missing networkManagement");
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mTime = (TrustedTime) Preconditions.checkNotNull(time, "missing TrustedTime");
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
        this.mPolicyFile = new AtomicFile(new File(systemDir, "netpolicy.xml"));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        LocalServices.addService(NetworkPolicyManagerInternal.class, new NetworkPolicyManagerInternalImpl(this, null));
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void bindNotificationManager(INotificationManager notifManager) {
        this.mNotifManager = (INotificationManager) Preconditions.checkNotNull(notifManager, "missing INotificationManager");
    }

    void updatePowerSaveWhitelistUL() {
        int i = 0;
        try {
            int length;
            int[] whitelist = this.mDeviceIdleController.getAppIdWhitelistExceptIdle();
            this.mPowerSaveWhitelistExceptIdleAppIds.clear();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveWhitelistExceptIdleAppIds.put(uid, true);
                }
            }
            whitelist = this.mDeviceIdleController.getAppIdWhitelist();
            this.mPowerSaveWhitelistAppIds.clear();
            if (whitelist != null) {
                length = whitelist.length;
                while (i < length) {
                    this.mPowerSaveWhitelistAppIds.put(whitelist[i], true);
                    i++;
                }
            }
        } catch (RemoteException e) {
        }
    }

    boolean addDefaultRestrictBackgroundWhitelistUidsUL() {
        List<UserInfo> users = this.mUserManager.getUsers();
        int numberUsers = users.size();
        boolean changed = false;
        for (int i = 0; i < numberUsers; i++) {
            if (addDefaultRestrictBackgroundWhitelistUidsUL(((UserInfo) users.get(i)).id)) {
                changed = true;
            }
        }
        return changed;
    }

    private boolean addDefaultRestrictBackgroundWhitelistUidsUL(int userId) {
        SystemConfig sysConfig = SystemConfig.getInstance();
        PackageManager pm = this.mContext.getPackageManager();
        ArraySet<String> allowDataUsage = sysConfig.getAllowInDataUsageSave();
        boolean changed = false;
        for (int i = 0; i < allowDataUsage.size(); i++) {
            String pkg = (String) allowDataUsage.valueAt(i);
            try {
                ApplicationInfo app = pm.getApplicationInfoAsUser(pkg, DumpState.DUMP_DEXOPT, userId);
                if (app.isPrivilegedApp()) {
                    int uid = UserHandle.getUid(userId, app.uid);
                    this.mDefaultRestrictBackgroundWhitelistUids.append(uid, true);
                    if (!this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                        setUidPolicyUncheckedUL(uid, 4, false);
                        changed = true;
                    }
                } else {
                    Slog.e(TAG, "addDefaultRestrictBackgroundWhitelistUidsUL(): skipping non-privileged app  " + pkg);
                }
            } catch (NameNotFoundException e) {
            }
        }
        return changed;
    }

    void updatePowerSaveTempWhitelistUL() {
        try {
            int N = this.mPowerSaveTempWhitelistAppIds.size();
            for (int i = 0; i < N; i++) {
                this.mPowerSaveTempWhitelistAppIds.setValueAt(i, false);
            }
            int[] whitelist = this.mDeviceIdleController.getAppIdTempWhitelist();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveTempWhitelistAppIds.put(uid, true);
                }
            }
        } catch (RemoteException e) {
        }
    }

    void purgePowerSaveTempWhitelistUL() {
        for (int i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
            if (!this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                this.mPowerSaveTempWhitelistAppIds.removeAt(i);
            }
        }
    }

    private void initService(CountDownLatch initCompleteSignal) {
        Trace.traceBegin(2097152, "systemReady");
        int oldPriority = Process.getThreadPriority(Process.myTid());
        try {
            Process.setThreadPriority(-2);
            if (isBandwidthControlEnabled()) {
                this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
                synchronized (this.mUidRulesFirstLock) {
                    synchronized (this.mNetworkPoliciesSecondLock) {
                        updatePowerSaveWhitelistUL();
                        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
                            public int getServiceType() {
                                return 6;
                            }

                            public void onLowPowerModeChanged(PowerSaveState result) {
                                boolean enabled = result.batterySaverEnabled;
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
                        readPolicyAL();
                        this.mRestrictBackgroundBeforeBsm = this.mRestrictBackground;
                        this.mRestrictBackgroundPowerState = this.mPowerManagerInternal.getLowPowerState(10);
                        boolean localRestrictBackground = this.mRestrictBackgroundPowerState.batterySaverEnabled;
                        if (localRestrictBackground && localRestrictBackground != this.mRestrictBackground) {
                            this.mRestrictBackground = localRestrictBackground;
                            this.mHandler.obtainMessage(6, this.mRestrictBackground ? 1 : 0, 0).sendToTarget();
                        }
                        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
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
                        setRestrictBackgroundUL(this.mRestrictBackground);
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
                ((LocalService) LocalServices.getService(LocalService.class)).setNetworkPolicyTempWhitelistCallback(this.mTempPowerSaveChangedCallback);
                this.mContext.registerReceiver(this.mConnReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"), "android.permission.CONNECTIVITY_INTERNAL", this.mHandler);
                IntentFilter packageFilter = new IntentFilter();
                packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
                packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
                this.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mHandler);
                this.mContext.registerReceiver(this.mUidRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
                IntentFilter userFilter = new IntentFilter();
                userFilter.addAction("android.intent.action.USER_ADDED");
                userFilter.addAction("android.intent.action.USER_REMOVED");
                this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
                this.mContext.registerReceiver(this.mStatsReceiver, new IntentFilter(NetworkStatsService.ACTION_NETWORK_STATS_UPDATED), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
                this.mContext.registerReceiver(this.mAllowReceiver, new IntentFilter(ACTION_ALLOW_BACKGROUND), "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
                this.mContext.registerReceiver(this.mSnoozeWarningReceiver, new IntentFilter(ACTION_SNOOZE_WARNING), "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
                this.mContext.registerReceiver(this.mWifiConfigReceiver, new IntentFilter("android.net.wifi.CONFIGURED_NETWORKS_CHANGE"), null, this.mHandler);
                this.mContext.registerReceiver(this.mWifiStateReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"), null, this.mHandler);
                this.mContext.registerReceiver(this.mCarrierConfigReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"), null, this.mHandler);
                this.mUsageStats.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
                initCompleteSignal.countDown();
                Process.setThreadPriority(oldPriority);
                Trace.traceEnd(2097152);
                return;
            }
            Slog.w(TAG, "bandwidth controls disabled, unable to enforce policy");
        } finally {
            Process.setThreadPriority(oldPriority);
            Trace.traceEnd(2097152);
        }
    }

    public CountDownLatch networkScoreAndNetworkManagementServiceReady() {
        CountDownLatch initCompleteSignal = new CountDownLatch(1);
        this.mHandler.post(new -$Lambda$hlRLCZCUKiWKuPbzPq01UpErk2Y(this, initCompleteSignal));
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

    static NetworkPolicy newWifiPolicy(NetworkTemplate template, boolean metered) {
        return new NetworkPolicy(template, -1, "UTC", -1, -1, -1, -1, metered, true);
    }

    void updateNotificationsNL() {
        int i;
        ArraySet<NotificationId> beforeNotifs = new ArraySet(this.mActiveNotifs);
        this.mActiveNotifs.clear();
        long currentTime = currentTimeMillis();
        for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (isTemplateRelevant(policy.template) && policy.hasCycle()) {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                long totalBytes = getTotalBytes(policy.template, start, currentTime);
                if (!policy.isOverLimit(totalBytes)) {
                    notifyUnderLimitNL(policy.template);
                    if (policy.isOverWarning(totalBytes) && policy.lastWarningSnooze < start) {
                        enqueueNotification(policy, 34, totalBytes);
                    }
                } else if (policy.lastLimitSnooze >= start) {
                    enqueueNotification(policy, 36, totalBytes);
                } else {
                    enqueueNotification(policy, 35, totalBytes);
                    notifyOverLimitNL(policy.template);
                }
            }
        }
        for (i = beforeNotifs.size() - 1; i >= 0; i--) {
            NotificationId notificationId = (NotificationId) beforeNotifs.valueAt(i);
            if (!this.mActiveNotifs.contains(notificationId)) {
                cancelNotification(notificationId);
            }
        }
    }

    private boolean isTemplateRelevant(NetworkTemplate template) {
        if (!template.isMatchRuleMobile()) {
            return true;
        }
        TelephonyManager tele = TelephonyManager.from(this.mContext);
        for (int subId : SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()) {
            if (template.matches(new NetworkIdentity(0, 0, tele.getSubscriberId(subId), null, false, true))) {
                return true;
            }
        }
        return false;
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

    private void enqueueNotification(NetworkPolicy policy, int type, long totalBytes) {
        NotificationId notificationId = new NotificationId(policy, type);
        Builder builder = new Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
        builder.setOnlyAlertOnce(true);
        builder.setWhen(0);
        builder.setColor(this.mContext.getColor(17170769));
        Resources res = this.mContext.getResources();
        CharSequence body = null;
        CharSequence title;
        switch (type) {
            case 34:
                if ((isNeedShowWarning() ^ 1) == 0) {
                    title = res.getText(17039846);
                    body = res.getString(17039845);
                    builder.setSmallIcon(17301624);
                    Bitmap bmp1 = BitmapFactory.decodeResource(res, 33751681);
                    if (bmp1 != null) {
                        builder.setLargeIcon(bmp1);
                    }
                    builder.setTicker(title);
                    builder.setContentTitle(title);
                    builder.setContentText(body);
                    builder.setDefaults(-1);
                    builder.setChannelId(SystemNotificationChannels.NETWORK_ALERTS);
                    if (isNeedShowWarning()) {
                        builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, buildSnoozeWarningIntent(policy.template), 134217728));
                    }
                    builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(res, policy.template), 134217728));
                    if (!isNeedShowWarning()) {
                        return;
                    }
                }
                return;
                break;
            case 35:
                body = res.getText(17039839);
                int icon = 17303319;
                switch (policy.template.getMatchRule()) {
                    case 1:
                        title = res.getText(17039842);
                        break;
                    case 2:
                        title = res.getText(17039836);
                        break;
                    case 3:
                        title = res.getText(17039838);
                        break;
                    case 4:
                        title = res.getText(17039848);
                        icon = 17301624;
                        break;
                    default:
                        title = null;
                        break;
                }
                builder.setOngoing(true);
                builder.setSmallIcon(icon);
                Bitmap bmp2 = BitmapFactory.decodeResource(res, 33751679);
                if (bmp2 != null) {
                    builder.setLargeIcon(bmp2);
                }
                builder.setTicker(title);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildNetworkOverLimitIntent(res, policy.template), 134217728));
                break;
            case 36:
                long overBytes = totalBytes - policy.limitBytes;
                body = res.getString(17039840, new Object[]{Formatter.formatFileSize(this.mContext, overBytes)});
                switch (policy.template.getMatchRule()) {
                    case 1:
                        title = res.getText(17039841);
                        break;
                    case 2:
                        title = res.getText(17039835);
                        break;
                    case 3:
                        title = res.getText(17039837);
                        break;
                    case 4:
                        title = res.getText(17039847);
                        break;
                    default:
                        title = null;
                        break;
                }
                builder.setOngoing(true);
                builder.setSmallIcon(17301624);
                Bitmap bmp3 = BitmapFactory.decodeResource(res, 33751681);
                if (bmp3 != null) {
                    builder.setLargeIcon(bmp3);
                }
                builder.setTicker(title);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(res, policy.template), 134217728));
                if (!isNeedShowWarning()) {
                    return;
                }
                break;
        }
        try {
            String packageName = this.mContext.getPackageName();
            if (!TextUtils.isEmpty(body)) {
                builder.setStyle(new BigTextStyle().bigText(body));
            }
            this.mNotifManager.enqueueNotificationWithTag(packageName, packageName, notificationId.getTag(), notificationId.getId(), builder.build(), -1);
            this.mActiveNotifs.add(notificationId);
        } catch (RemoteException e) {
        }
    }

    private void cancelNotification(NotificationId notificationId) {
        try {
            this.mNotifManager.cancelNotificationWithTag(this.mContext.getPackageName(), notificationId.getTag(), notificationId.getId(), -1);
        } catch (RemoteException e) {
        }
    }

    private boolean maybeUpdateMobilePolicyCycleNL(int subId) {
        PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(subId);
        if (config == null) {
            return false;
        }
        boolean policyUpdated = false;
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, TelephonyManager.from(this.mContext).getSubscriberId(subId), null, false, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            if (((NetworkTemplate) this.mNetworkPolicy.keyAt(i)).matches(probeIdent)) {
                NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
                if (policy.inferred) {
                    int cycleDay = getCycleDayFromCarrierConfig(config, policy.cycleDay);
                    long warningBytes = getWarningBytesFromCarrierConfig(config, policy.warningBytes);
                    long limitBytes = getLimitBytesFromCarrierConfig(config, policy.limitBytes);
                    if (policy.cycleDay != cycleDay || policy.warningBytes != warningBytes || policy.limitBytes != limitBytes) {
                        policyUpdated = true;
                        policy.cycleDay = cycleDay;
                        policy.warningBytes = warningBytes;
                        policy.limitBytes = limitBytes;
                    }
                }
            }
        }
        return policyUpdated;
    }

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

    void handleNetworkPoliciesUpdateAL(boolean shouldNormalizePolicies) {
        if (shouldNormalizePolicies) {
            normalizePoliciesNL();
        }
        updateNetworkEnabledNL();
        updateNetworkRulesNL();
        updateNotificationsNL();
        writePolicyAL();
    }

    void updateNetworkEnabledNL() {
        long currentTime = currentTimeMillis();
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (policy.limitBytes == -1 || (policy.hasCycle() ^ 1) != 0) {
                setNetworkTemplateEnabled(policy.template, true);
            } else {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                boolean overLimitWithoutSnooze = policy.isOverLimit(getTotalBytes(policy.template, start, currentTime)) ? policy.lastLimitSnooze < start : false;
                setNetworkTemplateEnabled(policy.template, overLimitWithoutSnooze ^ 1);
            }
        }
    }

    private void setNetworkTemplateEnabled(NetworkTemplate template, boolean enabled) {
        if (template.getMatchRule() == 1) {
            SubscriptionManager sm = SubscriptionManager.from(this.mContext);
            TelephonyManager tm = TelephonyManager.from(this.mContext);
            for (int subId : sm.getActiveSubscriptionIdList()) {
                if (template.matches(new NetworkIdentity(0, 0, tm.getSubscriberId(subId), null, false, true))) {
                    tm.setPolicyDataEnabled(enabled, subId);
                }
            }
        }
    }

    void updateNetworkRulesNL() {
        try {
            int i;
            NetworkPolicy policy;
            String iface;
            NetworkState[] states = this.mConnManager.getAllNetworkState();
            ArrayList<Pair<String, NetworkIdentity>> connIdents = new ArrayList(states.length);
            ArraySet<String> connIfaces = new ArraySet(states.length);
            for (NetworkState state : states) {
                if (!(state == null || state.networkInfo == null || !state.networkInfo.isConnected())) {
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
            this.mNetworkRules.clear();
            ArrayList<String> ifaceList = Lists.newArrayList();
            for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
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
            for (i = this.mNetworkRules.size() - 1; i >= 0; i--) {
                long start;
                long totalBytes;
                policy = (NetworkPolicy) this.mNetworkRules.keyAt(i);
                String[] ifaces = (String[]) this.mNetworkRules.valueAt(i);
                if (policy.hasCycle()) {
                    start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                    totalBytes = getTotalBytes(policy.template, start, currentTime);
                } else {
                    start = JobStatus.NO_LATEST_RUNTIME;
                    totalBytes = 0;
                }
                boolean hasWarning = policy.warningBytes != -1;
                boolean hasLimit = policy.limitBytes != -1;
                if (hasLimit || policy.metered) {
                    long quotaBytes;
                    if (!hasLimit) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else if (policy.lastLimitSnooze >= start) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else {
                        quotaBytes = Math.max(1, policy.limitBytes - totalBytes);
                    }
                    if (ifaces.length > 1) {
                        Slog.w(TAG, "shared quota unsupported; generating rule for each iface");
                    }
                    for (String iface2 : ifaces) {
                        this.mHandler.obtainMessage(10, (int) (quotaBytes >> 32), (int) (-1 & quotaBytes), iface2).sendToTarget();
                        arraySet.add(iface2);
                    }
                }
                if (hasWarning && policy.warningBytes < lowestRule) {
                    lowestRule = policy.warningBytes;
                }
                if (hasLimit && policy.limitBytes < lowestRule) {
                    lowestRule = policy.limitBytes;
                }
            }
            for (i = connIfaces.size() - 1; i >= 0; i--) {
                iface2 = (String) connIfaces.valueAt(i);
                this.mHandler.obtainMessage(10, HwBootFail.STAGE_BOOT_SUCCESS, -1, iface2).sendToTarget();
                arraySet.add(iface2);
            }
            this.mHandler.obtainMessage(7, Long.valueOf(lowestRule)).sendToTarget();
            for (i = this.mMeteredIfaces.size() - 1; i >= 0; i--) {
                iface2 = (String) this.mMeteredIfaces.valueAt(i);
                if (!arraySet.contains(iface2)) {
                    this.mHandler.obtainMessage(11, iface2).sendToTarget();
                }
            }
            this.mMeteredIfaces = arraySet;
            this.mHandler.obtainMessage(2, (String[]) this.mMeteredIfaces.toArray(new String[this.mMeteredIfaces.size()])).sendToTarget();
        } catch (RemoteException e) {
        }
    }

    private void ensureActiveMobilePolicyAL() {
        if (!this.mSuppressDefaultPolicy) {
            TelephonyManager tele = TelephonyManager.from(this.mContext);
            for (int subId : SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()) {
                ensureActiveMobilePolicyAL(subId, tele.getSubscriberId(subId));
            }
        }
    }

    private boolean ensureActiveMobilePolicyAL(int subId, String subscriberId) {
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, null, false, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            if (((NetworkTemplate) this.mNetworkPolicy.keyAt(i)).matches(probeIdent)) {
                return false;
            }
        }
        Slog.i(TAG, "No policy for subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId) + "; generating default policy");
        addNetworkPolicyAL(buildDefaultMobilePolicy(subId, subscriberId));
        return true;
    }

    private long getPlatformDefaultWarningBytes() {
        int dataWarningConfig = this.mContext.getResources().getInteger(17694820);
        if (((long) dataWarningConfig) == -1) {
            return -1;
        }
        return ((long) dataWarningConfig) * 1048576;
    }

    private long getPlatformDefaultLimitBytes() {
        return -1;
    }

    public NetworkPolicy buildDefaultMobilePolicy(int subId, String subscriberId) {
        PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(subId);
        Time time = new Time();
        time.setToNow();
        String cycleTimezone = time.timezone;
        int cycleDay = getCycleDayFromCarrierConfig(config, time.monthDay);
        long warningBytes = getWarningBytesFromCarrierConfig(config, -1);
        if (isNeedShowWarning()) {
            warningBytes = getWarningBytesFromCarrierConfig(config, getPlatformDefaultWarningBytes());
        }
        return new NetworkPolicy(NetworkTemplate.buildTemplateMobileAll(subscriberId), cycleDay, cycleTimezone, warningBytes, getLimitBytesFromCarrierConfig(config, getPlatformDefaultLimitBytes()), -1, -1, true, true);
    }

    protected void readPolicyAL() {
        this.mNetworkPolicy.clear();
        this.mUidPolicy.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            SparseBooleanArray whitelistedRestrictBackground = new SparseBooleanArray();
            int version = 1;
            boolean insideWhitelist = false;
            while (true) {
                int type = in.next();
                int uid;
                int policy;
                if (type != 1) {
                    String tag = in.getName();
                    if (type == 2) {
                        if (TAG_POLICY_LIST.equals(tag)) {
                            boolean oldValue = this.mRestrictBackground;
                            version = XmlUtils.readIntAttribute(in, ATTR_VERSION);
                            if (version >= 3) {
                                this.mRestrictBackground = XmlUtils.readBooleanAttribute(in, ATTR_RESTRICT_BACKGROUND);
                            } else {
                                this.mRestrictBackground = false;
                            }
                            if (this.mRestrictBackground != oldValue) {
                                this.mHandler.obtainMessage(6, this.mRestrictBackground ? 1 : 0, 0).sendToTarget();
                            }
                        } else if (TAG_NETWORK_POLICY.equals(tag)) {
                            String networkId;
                            String cycleTimezone;
                            long lastLimitSnooze;
                            boolean metered;
                            long lastWarningSnooze;
                            boolean inferred;
                            int networkTemplate = XmlUtils.readIntAttribute(in, ATTR_NETWORK_TEMPLATE);
                            String subscriberId = in.getAttributeValue(null, ATTR_SUBSCRIBER_ID);
                            if (version >= 9) {
                                networkId = in.getAttributeValue(null, ATTR_NETWORK_ID);
                            } else {
                                networkId = null;
                            }
                            int cycleDay = XmlUtils.readIntAttribute(in, ATTR_CYCLE_DAY);
                            if (version >= 6) {
                                cycleTimezone = in.getAttributeValue(null, ATTR_CYCLE_TIMEZONE);
                            } else {
                                cycleTimezone = "UTC";
                            }
                            long warningBytes = XmlUtils.readLongAttribute(in, ATTR_WARNING_BYTES);
                            long limitBytes = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES);
                            if (version >= 5) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_LIMIT_SNOOZE);
                            } else if (version >= 2) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_SNOOZE);
                            } else {
                                lastLimitSnooze = -1;
                            }
                            if (version < 4) {
                                switch (networkTemplate) {
                                    case 1:
                                    case 2:
                                    case 3:
                                        metered = true;
                                        break;
                                    default:
                                        metered = false;
                                        break;
                                }
                            }
                            metered = XmlUtils.readBooleanAttribute(in, ATTR_METERED);
                            if (version >= 5) {
                                lastWarningSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_WARNING_SNOOZE);
                            } else {
                                lastWarningSnooze = -1;
                            }
                            if (version >= 7) {
                                inferred = XmlUtils.readBooleanAttribute(in, ATTR_INFERRED);
                            } else {
                                inferred = false;
                            }
                            NetworkTemplate template = new NetworkTemplate(networkTemplate, subscriberId, networkId);
                            if (template.isPersistable()) {
                                this.mNetworkPolicy.put(template, new NetworkPolicy(template, cycleDay, cycleTimezone, warningBytes, limitBytes, lastWarningSnooze, lastLimitSnooze, metered, inferred));
                            }
                        } else if (TAG_UID_POLICY.equals(tag)) {
                            uid = XmlUtils.readIntAttribute(in, ATTR_UID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedUL(uid, policy, false);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_APP_POLICY.equals(tag)) {
                            int appId = XmlUtils.readIntAttribute(in, ATTR_APP_ID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            uid = UserHandle.getUid(0, appId);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedUL(uid, policy, false);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_WHITELIST.equals(tag)) {
                            insideWhitelist = true;
                        } else if (TAG_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            whitelistedRestrictBackground.append(XmlUtils.readIntAttribute(in, ATTR_UID), true);
                        } else if (TAG_REVOKED_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistRevokedUids.put(XmlUtils.readIntAttribute(in, ATTR_UID), true);
                        }
                    } else if (type == 3 && TAG_WHITELIST.equals(tag)) {
                        insideWhitelist = false;
                    }
                } else {
                    int size = whitelistedRestrictBackground.size();
                    for (int i = 0; i < size; i++) {
                        uid = whitelistedRestrictBackground.keyAt(i);
                        policy = this.mUidPolicy.get(uid, 0);
                        if ((policy & 1) != 0) {
                            Slog.w(TAG, "ignoring restrict-background-whitelist for " + uid + " because its policy is " + NetworkPolicyManager.uidPoliciesToString(policy));
                        } else if (UserHandle.isApp(uid)) {
                            setUidPolicyUncheckedUL(uid, policy | 4, false);
                        } else {
                            Slog.w(TAG, "unable to update policy on UID " + uid);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            upgradeLegacyBackgroundDataUL();
        } catch (Throwable e2) {
            Log.wtf(TAG, "problem reading network policy", e2);
        } catch (Throwable e3) {
            Log.wtf(TAG, "problem reading network policy", e3);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void upgradeLegacyBackgroundDataUL() {
        boolean z = true;
        if (Secure.getInt(this.mContext.getContentResolver(), "background_data", 1) == 1) {
            z = false;
        }
        this.mRestrictBackground = z;
        if (this.mRestrictBackground) {
            this.mContext.sendBroadcastAsUser(new Intent("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED"), UserHandle.ALL);
        }
    }

    void writePolicyAL() {
        FileOutputStream fos = null;
        try {
            int i;
            int uid;
            fos = this.mPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, 10);
            XmlUtils.writeBooleanAttribute(out, ATTR_RESTRICT_BACKGROUND, this.mRestrictBackground);
            for (i = 0; i < this.mNetworkPolicy.size(); i++) {
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
            for (i = 0; i < this.mUidPolicy.size(); i++) {
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
            int size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            for (i = 0; i < size; i++) {
                uid = this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i);
                out.startTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                out.endTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
            }
            out.endTag(null, TAG_WHITELIST);
            out.endDocument();
            this.mPolicyFile.finishWrite(fos);
        } catch (IOException e) {
            if (fos != null) {
                this.mPolicyFile.failWrite(fos);
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
            synchronized (this.mUidRulesFirstLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                policy |= oldPolicy;
                if (oldPolicy != policy) {
                    setUidPolicyUncheckedUL(uid, oldPolicy, policy, true);
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
                policy = oldPolicy & (~policy);
                if (oldPolicy != policy) {
                    setUidPolicyUncheckedUL(uid, oldPolicy, policy, true);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    private void setUidPolicyUncheckedUL(int uid, int oldPolicy, int policy, boolean persist) {
        boolean notifyApp;
        setUidPolicyUncheckedUL(uid, policy, persist);
        if (isUidValidForWhitelistRules(uid)) {
            boolean wasBlacklisted = oldPolicy == 1;
            boolean isBlacklisted = policy == 1;
            boolean wasWhitelisted = oldPolicy == 4;
            boolean isWhitelisted = policy == 4;
            int wasBlocked = !wasBlacklisted ? this.mRestrictBackground ? wasWhitelisted ^ 1 : 0 : 1;
            int isBlocked = !isBlacklisted ? this.mRestrictBackground ? isWhitelisted ^ 1 : 0 : 1;
            if (wasWhitelisted && ((!isWhitelisted || isBlacklisted) && this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && (this.mRestrictBackgroundWhitelistRevokedUids.get(uid) ^ 1) != 0)) {
                this.mRestrictBackgroundWhitelistRevokedUids.append(uid, true);
            }
            notifyApp = wasBlocked != isBlocked;
        } else {
            notifyApp = false;
        }
        this.mHandler.obtainMessage(13, uid, policy, Boolean.valueOf(notifyApp)).sendToTarget();
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

    boolean removeUserStateUL(int userId, boolean writePolicy) {
        int i;
        int i2 = 0;
        boolean changed = false;
        for (i = this.mRestrictBackgroundWhitelistRevokedUids.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i)) == userId) {
                this.mRestrictBackgroundWhitelistRevokedUids.removeAt(i);
                changed = true;
            }
        }
        int[] uids = new int[0];
        for (i = 0; i < this.mUidPolicy.size(); i++) {
            int uid = this.mUidPolicy.keyAt(i);
            if (UserHandle.getUserId(uid) == userId) {
                uids = ArrayUtils.appendInt(uids, uid);
            }
        }
        if (uids.length > 0) {
            int length = uids.length;
            while (i2 < length) {
                this.mUidPolicy.delete(uids[i2]);
                i2++;
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

    void addNetworkPolicyAL(NetworkPolicy policy) {
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
                policies[i] = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            }
        }
        return policies;
    }

    private void normalizePoliciesNL() {
        normalizePoliciesNL(getNetworkPolicies(this.mContext.getOpPackageName()));
    }

    private void normalizePoliciesNL(NetworkPolicy[] policies) {
        String[] merged = TelephonyManager.from(this.mContext).getMergedSubscriberIds();
        this.mNetworkPolicy.clear();
        for (NetworkPolicy policy : policies) {
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
            performSnooze(template, 35);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void performSnooze(NetworkTemplate template, int type) {
        maybeRefreshTrustedTime();
        long currentTime = currentTimeMillis();
        synchronized (this.mUidRulesFirstLock) {
            synchronized (this.mNetworkPoliciesSecondLock) {
                NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.get(template);
                if (policy == null) {
                    throw new IllegalArgumentException("unable to find policy for " + template);
                }
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
                handleNetworkPoliciesUpdateAL(true);
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

    /* JADX WARNING: Missing block: B:13:0x0041, code:
            android.os.Trace.traceEnd(2097152);
     */
    /* JADX WARNING: Missing block: B:14:0x0044, code:
            return;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            android.os.Binder.restoreCallingIdentity(r0);
            r3 = r8.mHandler;
     */
    /* JADX WARNING: Missing block: B:21:0x004e, code:
            if (r9 == false) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:22:0x0050, code:
            r2 = 1;
     */
    /* JADX WARNING: Missing block: B:23:0x0051, code:
            r3.obtainMessage(6, r2, 0).sendToTarget();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRestrictBackground(boolean restrictBackground) {
        int i = 0;
        Trace.traceBegin(2097152, "setRestrictBackground");
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
            long token = Binder.clearCallingIdentity();
            try {
                maybeRefreshTrustedTime();
                synchronized (this.mUidRulesFirstLock) {
                    if (restrictBackground == this.mRestrictBackground) {
                        Slog.w(TAG, "setRestrictBackground: already " + restrictBackground);
                    } else {
                        setRestrictBackgroundUL(restrictBackground);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void setRestrictBackgroundUL(boolean restrictBackground) {
        Slog.d(TAG, "setRestrictBackgroundUL(): " + restrictBackground);
        boolean oldRestrictBackground = this.mRestrictBackground;
        this.mRestrictBackground = restrictBackground;
        updateRulesForRestrictBackgroundUL();
        try {
            if (!this.mNetworkManager.setDataSaverModeEnabled(this.mRestrictBackground)) {
                Slog.e(TAG, "Could not change Data Saver Mode on NMS to " + this.mRestrictBackground);
                this.mRestrictBackground = oldRestrictBackground;
                return;
            }
        } catch (RemoteException e) {
        }
        if (this.mRestrictBackgroundPowerState.globalBatterySaverEnabled) {
            this.mRestrictBackgroundChangedInBsm = true;
        }
        synchronized (this.mNetworkPoliciesSecondLock) {
            updateNotificationsNL();
            writePolicyAL();
        }
    }

    /* JADX WARNING: Missing block: B:29:0x003d, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getRestrictBackgroundByCaller() {
        int i = 3;
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        int uid = Binder.getCallingUid();
        synchronized (this.mUidRulesFirstLock) {
            long token = Binder.clearCallingIdentity();
            try {
                int policy = getUidPolicy(uid);
                Binder.restoreCallingIdentity(token);
                if (policy == 1) {
                    return 3;
                } else if (!this.mRestrictBackground) {
                    return 1;
                } else if ((this.mUidPolicy.get(uid) & 4) != 0) {
                    i = 2;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
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

    /* JADX WARNING: Missing block: B:16:0x002a, code:
            if (r7 == false) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:17:0x002c, code:
            com.android.server.EventLogTags.writeDeviceIdleOnPhase("net");
     */
    /* JADX WARNING: Missing block: B:18:0x0032, code:
            android.os.Trace.traceEnd(2097152);
     */
    /* JADX WARNING: Missing block: B:19:0x0035, code:
            return;
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            com.android.server.EventLogTags.writeDeviceIdleOffPhase("net");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDeviceIdleMode(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        Trace.traceBegin(2097152, "setDeviceIdleMode");
        try {
            synchronized (this.mUidRulesFirstLock) {
                if (this.mDeviceIdleMode != enabled) {
                    this.mDeviceIdleMode = enabled;
                    if (this.mSystemReady) {
                        updateRulesForRestrictPowerUL();
                    }
                }
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private NetworkPolicy findPolicyForNetworkNL(NetworkIdentity ident) {
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
        NetworkPolicy policy;
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        synchronized (this.mNetworkPoliciesSecondLock) {
            policy = findPolicyForNetworkNL(ident);
        }
        if (policy == null || (policy.hasCycle() ^ 1) != 0) {
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
            return false;
        }
        NetworkPolicy policy;
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        synchronized (this.mNetworkPoliciesSecondLock) {
            policy = findPolicyForNetworkNL(ident);
        }
        if (policy != null) {
            return policy.metered;
        }
        int type = state.networkInfo.getType();
        if ((ConnectivityManager.isNetworkTypeMobile(type) && ident.getMetered()) || type == 6) {
            return true;
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter fout = new IndentingPrintWriter(writer, "  ");
            ArraySet<String> argSet = new ArraySet(args.length);
            for (String arg : args) {
                argSet.add(arg);
            }
            synchronized (this.mUidRulesFirstLock) {
                synchronized (this.mNetworkPoliciesSecondLock) {
                    int i;
                    if (argSet.contains("--unsnooze")) {
                        for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                            ((NetworkPolicy) this.mNetworkPolicy.valueAt(i)).clearSnooze();
                        }
                        handleNetworkPoliciesUpdateAL(true);
                        fout.println("Cleared snooze timestamps");
                        return;
                    }
                    int uid;
                    int uidRules;
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
                    for (i = 0; i < this.mNetworkPolicy.size(); i++) {
                        fout.println(((NetworkPolicy) this.mNetworkPolicy.valueAt(i)).toString());
                    }
                    fout.decreaseIndent();
                    fout.print("Metered ifaces: ");
                    fout.println(String.valueOf(this.mMeteredIfaces));
                    fout.println("Policy for UIDs:");
                    fout.increaseIndent();
                    int size = this.mUidPolicy.size();
                    for (i = 0; i < size; i++) {
                        uid = this.mUidPolicy.keyAt(i);
                        int policy = this.mUidPolicy.valueAt(i);
                        fout.print("UID=");
                        fout.print(uid);
                        fout.print(" policy=");
                        fout.print(NetworkPolicyManager.uidPoliciesToString(policy));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    size = this.mPowerSaveWhitelistExceptIdleAppIds.size();
                    if (size > 0) {
                        fout.println("Power save whitelist (except idle) app ids:");
                        fout.increaseIndent();
                        for (i = 0; i < size; i++) {
                            fout.print("UID=");
                            fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i));
                            fout.print(": ");
                            fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.valueAt(i));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    size = this.mPowerSaveWhitelistAppIds.size();
                    if (size > 0) {
                        fout.println("Power save whitelist app ids:");
                        fout.increaseIndent();
                        for (i = 0; i < size; i++) {
                            fout.print("UID=");
                            fout.print(this.mPowerSaveWhitelistAppIds.keyAt(i));
                            fout.print(": ");
                            fout.print(this.mPowerSaveWhitelistAppIds.valueAt(i));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    size = this.mDefaultRestrictBackgroundWhitelistUids.size();
                    if (size > 0) {
                        fout.println("Default restrict background whitelist uids:");
                        fout.increaseIndent();
                        for (i = 0; i < size; i++) {
                            fout.print("UID=");
                            fout.print(this.mDefaultRestrictBackgroundWhitelistUids.keyAt(i));
                            fout.println();
                        }
                        fout.decreaseIndent();
                    }
                    size = this.mRestrictBackgroundWhitelistRevokedUids.size();
                    if (size > 0) {
                        fout.println("Default restrict background whitelist uids revoked by users:");
                        fout.increaseIndent();
                        for (i = 0; i < size; i++) {
                            fout.print("UID=");
                            fout.print(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i));
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
                    for (i = 0; i < size; i++) {
                        uid = knownUids.keyAt(i);
                        fout.print("UID=");
                        fout.print(uid);
                        int state = this.mUidState.get(uid, 17);
                        fout.print(" state=");
                        fout.print(state);
                        if (state <= 2) {
                            fout.print(" (fg)");
                        } else {
                            fout.print(state <= 4 ? " (fg svc)" : " (bg)");
                        }
                        uidRules = this.mUidRules.get(uid, 0);
                        fout.print(" rules=");
                        fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    fout.println("Status for just UIDs with rules:");
                    fout.increaseIndent();
                    size = this.mUidRules.size();
                    for (i = 0; i < size; i++) {
                        uid = this.mUidRules.keyAt(i);
                        fout.print("UID=");
                        fout.print(uid);
                        uidRules = this.mUidRules.get(uid, 0);
                        fout.print(" rules=");
                        fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                        fout.println();
                    }
                    fout.decreaseIndent();
                    fout.println("Observed uid state changes:");
                    fout.increaseIndent();
                    this.mObservedHistory.dumpUL(fout);
                    fout.decreaseIndent();
                }
            }
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new NetworkPolicyManagerShellCommand(this.mContext, this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    public boolean isUidForeground(int uid) {
        boolean isUidForegroundUL;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mUidRulesFirstLock) {
            isUidForegroundUL = isUidForegroundUL(uid);
        }
        return isUidForegroundUL;
    }

    private boolean isUidForegroundUL(int uid) {
        return isUidStateForegroundUL(this.mUidState.get(uid, 17));
    }

    private boolean isUidForegroundOnRestrictBackgroundUL(int uid) {
        return NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(this.mUidState.get(uid, 17));
    }

    private boolean isUidForegroundOnRestrictPowerUL(int uid) {
        return NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 17));
    }

    private boolean isUidStateForegroundUL(int state) {
        return state <= 2;
    }

    private void updateUidStateUL(int uid, int uidState) {
        Trace.traceBegin(2097152, "updateUidStateUL");
        try {
            int oldUidState = this.mUidState.get(uid, 17);
            if (oldUidState != uidState) {
                this.mUidState.put(uid, uidState);
                updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, uidState);
                if (NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(oldUidState) != NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                    if (isUidIdle(uid)) {
                        updateRuleForAppIdleUL(uid);
                    }
                    if (this.mDeviceIdleMode) {
                        updateRuleForDeviceIdleUL(uid);
                    }
                    if (this.mRestrictPower) {
                        updateRuleForRestrictPowerUL(uid);
                    }
                    updateRulesForPowerRestrictionsUL(uid);
                }
                updateNetworkStats(uid, isUidStateForegroundUL(uidState));
            }
            Trace.traceEnd(2097152);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
        }
    }

    private void removeUidStateUL(int uid) {
        int index = this.mUidState.indexOfKey(uid);
        if (index >= 0) {
            int oldUidState = this.mUidState.valueAt(index);
            this.mUidState.removeAt(index);
            if (oldUidState != 17) {
                updateRestrictBackgroundRulesOnUidStatusChangedUL(uid, oldUidState, 17);
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
            Trace.traceBegin(2097152, "updateNetworkStats: " + uid + "/" + (uidForeground ? "F" : "B"));
        }
        try {
            this.mNetworkStats.setUidForeground(uid, uidForeground);
        } catch (RemoteException e) {
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void updateRestrictBackgroundRulesOnUidStatusChangedUL(int uid, int oldUidState, int newUidState) {
        if (NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(oldUidState) != NetworkPolicyManager.isProcStateAllowedWhileOnRestrictBackground(newUidState)) {
            updateRulesForDataUsageRestrictionsUL(uid);
        }
    }

    void updateRulesForPowerSaveUL() {
        Trace.traceBegin(2097152, "updateRulesForPowerSaveUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mRestrictPower, 3, this.mUidFirewallPowerSaveRules);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    void updateRuleForRestrictPowerUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mRestrictPower, 3);
    }

    void updateRulesForDeviceIdleUL() {
        Trace.traceBegin(2097152, "updateRulesForDeviceIdleUL");
        try {
            updateRulesForWhitelistedPowerSaveUL(this.mDeviceIdleMode, 1, this.mUidFirewallDozableRules);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    void updateRuleForDeviceIdleUL(int uid) {
        updateRulesForWhitelistedPowerSaveUL(uid, this.mDeviceIdleMode, 1);
    }

    private void updateRulesForWhitelistedPowerSaveUL(boolean enabled, int chain, SparseIntArray rules) {
        if (enabled) {
            SparseIntArray uidRules = rules;
            rules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                UserInfo user = (UserInfo) users.get(ui);
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
            setUidFirewallRulesAsync(chain, rules, 1);
            return;
        }
        setUidFirewallRulesAsync(chain, null, 2);
    }

    private void updateRulesForWhitelistedAppIds(SparseIntArray uidRules, SparseBooleanArray whitelistedAppIds, int userId) {
        for (int i = whitelistedAppIds.size() - 1; i >= 0; i--) {
            if (whitelistedAppIds.valueAt(i)) {
                uidRules.put(UserHandle.getUid(userId, whitelistedAppIds.keyAt(i)), 1);
            }
        }
    }

    private boolean isWhitelistedBatterySaverUL(int uid, boolean deviceIdleMode) {
        boolean isWhitelisted;
        int appId = UserHandle.getAppId(uid);
        if (this.mPowerSaveTempWhitelistAppIds.get(appId)) {
            isWhitelisted = true;
        } else {
            isWhitelisted = this.mPowerSaveWhitelistAppIds.get(appId);
        }
        if (deviceIdleMode) {
            return isWhitelisted;
        }
        return !isWhitelisted ? this.mPowerSaveWhitelistExceptIdleAppIds.get(appId) : true;
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

    void updateRulesForAppIdleUL() {
        Trace.traceBegin(2097152, "updateRulesForAppIdleUL");
        try {
            SparseIntArray uidRules = this.mUidFirewallStandbyRules;
            uidRules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                for (int uid : this.mUsageStats.getIdleUidsForUser(((UserInfo) users.get(ui)).id)) {
                    if (!this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid), false) && hasInternetPermissions(uid)) {
                        uidRules.put(uid, 2);
                    }
                }
            }
            setUidFirewallRulesAsync(2, uidRules, 0);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    void updateRuleForAppIdleUL(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            if (Trace.isTagEnabled(2097152)) {
                Trace.traceBegin(2097152, "updateRuleForAppIdleUL: " + uid);
            }
            try {
                if (this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid)) || !isUidIdle(uid) || (isUidForegroundOnRestrictPowerUL(uid) ^ 1) == 0) {
                    setUidFirewallRule(2, uid, 0);
                } else {
                    setUidFirewallRule(2, uid, 2);
                }
                Trace.traceEnd(2097152);
            } catch (Throwable th) {
                Trace.traceEnd(2097152);
            }
        }
    }

    void updateRulesForAppIdleParoleUL() {
        boolean paroled = this.mUsageStats.isAppIdleParoleOn();
        boolean enableChain = paroled ^ 1;
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

    private void updateRulesForGlobalChangeAL(boolean restrictedNetworksChanged) {
        if (Trace.isTagEnabled(2097152)) {
            Trace.traceBegin(2097152, "updateRulesForGlobalChangeAL: " + (restrictedNetworksChanged ? "R" : "-"));
        }
        try {
            updateRulesForAppIdleUL();
            updateRulesForRestrictPowerUL();
            updateRulesForRestrictBackgroundUL();
            if (restrictedNetworksChanged) {
                normalizePoliciesNL();
                updateNetworkRulesNL();
            }
            Trace.traceEnd(2097152);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
        }
    }

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

    private void updateRulesForRestrictBackgroundUL() {
        Trace.traceBegin(2097152, "updateRulesForRestrictBackgroundUL");
        try {
            updateRulesForAllAppsUL(1);
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private void updateRulesForAllAppsUL(int type) {
        long j;
        if (Trace.isTagEnabled(2097152)) {
            j = 2097152;
            Trace.traceBegin(2097152, "updateRulesForRestrictPowerUL-" + type);
        }
        long th;
        try {
            PackageManager pm = this.mContext.getPackageManager();
            j = 2097152;
            Trace.traceBegin(2097152, "list-users");
            th = this.mUserManager;
            List users = th.getUsers();
            Trace.traceEnd(th);
            j = 2097152;
            Trace.traceBegin(2097152, "list-uids");
            th = 4981248;
            List installedApplications = pm.getInstalledApplications(4981248);
            Trace.traceEnd(th);
            int usersSize = users.size();
            int appsSize = installedApplications.size();
            for (int i = 0; i < usersSize; i++) {
                UserInfo user = (UserInfo) users.get(i);
                for (int j2 = 0; j2 < appsSize; j2++) {
                    int uid = UserHandle.getUid(user.id, ((ApplicationInfo) installedApplications.get(j2)).uid);
                    switch (type) {
                        case 1:
                            updateRulesForDataUsageRestrictionsUL(uid);
                            break;
                        case 2:
                            updateRulesForPowerRestrictionsUL(uid);
                            break;
                        default:
                            Slog.w(TAG, "Invalid type for updateRulesForAllApps: " + type);
                            break;
                    }
                }
            }
            Trace.traceEnd(2097152);
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(j);
        } finally {
            j = 2097152;
            Trace.traceEnd(2097152);
        }
    }

    private void updateRulesForTempWhitelistChangeUL() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = (UserInfo) users.get(i);
            for (int j = this.mPowerSaveTempWhitelistAppIds.size() - 1; j >= 0; j--) {
                int uid = UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(j));
                updateRuleForAppIdleUL(uid);
                updateRuleForDeviceIdleUL(uid);
                updateRuleForRestrictPowerUL(uid);
                updateRulesForPowerRestrictionsUL(uid);
            }
        }
    }

    private boolean isUidValidForBlacklistRules(int uid) {
        if (uid == 1013 || uid == 1019 || (UserHandle.isApp(uid) && hasInternetPermissions(uid))) {
            return true;
        }
        return false;
    }

    private boolean isUidValidForWhitelistRules(int uid) {
        return UserHandle.isApp(uid) ? hasInternetPermissions(uid) : false;
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

    private void onUidDeletedUL(int uid) {
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
        if (isUidValidForWhitelistRules(uid)) {
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictBackgroundUL(uid);
            boolean isBlacklisted = (uidPolicy & 1) != 0;
            boolean isWhitelisted = (uidPolicy & 4) != 0;
            int oldRule = oldUidRules & 15;
            int newRule = 0;
            if (isForeground) {
                if (isBlacklisted || (this.mRestrictBackground && (isWhitelisted ^ 1) != 0)) {
                    newRule = 2;
                } else if (isWhitelisted) {
                    newRule = 1;
                }
            } else if (isBlacklisted) {
                newRule = 4;
            } else if (this.mRestrictBackground && isWhitelisted) {
                newRule = 1;
            }
            int newUidRules = newRule | (oldUidRules & 240);
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                if ((newRule & 2) != 0) {
                    setMeteredNetworkWhitelist(uid, true);
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, false);
                    }
                } else if ((oldRule & 2) != 0) {
                    if (!isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, false);
                    }
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, true);
                    }
                } else if ((newRule & 4) != 0 || (oldRule & 4) != 0) {
                    setMeteredNetworkBlacklist(uid, isBlacklisted);
                    if ((oldRule & 4) != 0 && isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, isWhitelisted);
                    }
                } else if ((newRule & 1) == 0 && (oldRule & 1) == 0) {
                    Log.wtf(TAG, "Unexpected change of metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", blacklisted=" + isBlacklisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                } else {
                    setMeteredNetworkWhitelist(uid, isWhitelisted);
                }
                this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
            }
        }
    }

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
            Trace.traceBegin(2097152, "updateRulesForPowerRestrictionsUL: " + uid + "/" + oldUidRules + "/" + (paroled ? "P" : "-"));
        }
        try {
            int updateRulesForPowerRestrictionsULInner = updateRulesForPowerRestrictionsULInner(uid, oldUidRules, paroled);
            return updateRulesForPowerRestrictionsULInner;
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    private int updateRulesForPowerRestrictionsULInner(int uid, int oldUidRules, boolean paroled) {
        if (!isUidValidForBlacklistRules(uid)) {
            return 0;
        }
        boolean restrictMode = ((!paroled ? isUidIdle(uid) : false) || this.mRestrictPower) ? true : this.mDeviceIdleMode;
        boolean isForeground = isUidForegroundOnRestrictPowerUL(uid);
        boolean isWhitelisted = isWhitelistedBatterySaverUL(uid, this.mDeviceIdleMode);
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
        if (newRule != oldRule) {
            if (newRule != 0 && (newRule & 32) == 0 && (newRule & 64) == 0) {
                Log.wtf(TAG, "Unexpected change of non-metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
            }
            this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
        }
        return newUidRules;
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

    private void dispatchUidPoliciesChanged(INetworkPolicyListener listener, int uid, int uidPolicies) {
        if (listener != null) {
            try {
                listener.onUidPoliciesChanged(uid, uidPolicies);
            } catch (RemoteException e) {
            }
        }
    }

    void handleUidChanged(int uid, int procState, long procStateSeq) {
        Trace.traceBegin(2097152, "onUidStateChanged");
        try {
            synchronized (this.mUidRulesFirstLock) {
                this.mObservedHistory.addProcStateSeqUL(uid, procStateSeq);
                updateUidStateUL(uid, procState);
                this.mActivityManagerInternal.notifyNetworkPolicyRulesUpdated(uid, procStateSeq);
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

    void handleUidGone(int uid) {
        Trace.traceBegin(2097152, "onUidGone");
        try {
            synchronized (this.mUidRulesFirstLock) {
                removeUidStateUL(uid);
            }
        } finally {
            Trace.traceEnd(2097152);
        }
    }

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

    private void setUidFirewallRulesAsync(int chain, SparseIntArray uidRules, int toggle) {
        this.mHandler.obtainMessage(14, chain, toggle, uidRules).sendToTarget();
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
        if (Trace.isTagEnabled(2097152)) {
            Trace.traceBegin(2097152, "setUidFirewallRule: " + chain + "/" + uid + "/" + rule);
        }
        if (chain == 1) {
            try {
                this.mUidFirewallDozableRules.put(uid, rule);
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem setting firewall uid rules", e);
            } catch (RemoteException e2) {
            } catch (Throwable th) {
                Trace.traceEnd(2097152);
            }
        } else if (chain == 2) {
            this.mUidFirewallStandbyRules.put(uid, rule);
        } else if (chain == 3) {
            this.mUidFirewallPowerSaveRules.put(uid, rule);
        }
        this.mNetworkManager.setFirewallUidRule(chain, uid, rule);
        Trace.traceEnd(2097152);
    }

    private void enableFirewallChainUL(int chain, boolean enable) {
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
        long token = Binder.clearCallingIdentity();
        boolean isBandwidthControlEnabled;
        try {
            isBandwidthControlEnabled = this.mNetworkManager.isBandwidthControlEnabled();
            return isBandwidthControlEnabled;
        } catch (RemoteException e) {
            isBandwidthControlEnabled = false;
            return isBandwidthControlEnabled;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void maybeRefreshTrustedTime() {
        if (getTimeRefreshElapsedRealtime() > 86400000) {
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

    private static Intent buildSnoozeWarningIntent(NetworkTemplate template) {
        Intent intent = new Intent(ACTION_SNOOZE_WARNING);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildNetworkOverLimitIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039804)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildViewDataUsageIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039763)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    public void addIdleHandler(IdleHandler handler) {
        this.mHandler.getLooper().getQueue().addIdleHandler(handler);
    }

    public void updateRestrictBackgroundByLowPowerModeUL(PowerSaveState result) {
        boolean shouldInvokeRestrictBackground;
        this.mRestrictBackgroundPowerState = result;
        boolean restrictBackground = result.batterySaverEnabled;
        boolean localRestrictBgChangedInBsm = this.mRestrictBackgroundChangedInBsm;
        if (result.globalBatterySaverEnabled) {
            shouldInvokeRestrictBackground = !this.mRestrictBackground ? result.batterySaverEnabled : false;
            this.mRestrictBackgroundBeforeBsm = this.mRestrictBackground;
            localRestrictBgChangedInBsm = false;
        } else {
            shouldInvokeRestrictBackground = this.mRestrictBackgroundChangedInBsm ^ 1;
            restrictBackground = this.mRestrictBackgroundBeforeBsm;
        }
        if (shouldInvokeRestrictBackground) {
            setRestrictBackground(restrictBackground);
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

    private static boolean hasRule(int uidRules, int rule) {
        return (uidRules & rule) != 0;
    }

    private static void logUidStatus(int uid, String descr) {
        Slog.d(TAG, String.format("uid %d is %s", new Object[]{Integer.valueOf(uid), descr}));
    }

    public long getTimeRefreshElapsedRealtime() {
        if (this.mTimeRefreshRealtime != -1) {
            return SystemClock.elapsedRealtime() - this.mTimeRefreshRealtime;
        }
        return JobStatus.NO_LATEST_RUNTIME;
    }
}
