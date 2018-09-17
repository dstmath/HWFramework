package com.android.server.net;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.DataUsageRequest;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkStatsSession;
import android.net.INetworkStatsSession.Stub;
import android.net.LinkProperties;
import android.net.NetworkIdentity;
import android.net.NetworkState;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStats.NonMonotonicObserver;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.MathUtils;
import android.util.NtpTrustedTime;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.TrustedTime;
import android.util.proto.ProtoOutputStream;
import com.android.internal.net.VpnInfo;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FileRotator;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwNetworkStatsService;
import com.android.server.NetPluginDelegate;
import com.android.server.NetworkManagementService;
import com.android.server.NetworkManagementSocketTagger;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsManager;
import com.android.server.usage.UnixCalendar;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class NetworkStatsService extends AbsNetworkStatsService {
    public static final String ACTION_NETWORK_STATS_POLL = "com.android.server.action.NETWORK_STATS_POLL";
    public static final String ACTION_NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
    private static final int FLAG_PERSIST_ALL = 3;
    private static final int FLAG_PERSIST_FORCE = 256;
    private static final int FLAG_PERSIST_NETWORK = 1;
    private static final int FLAG_PERSIST_UID = 2;
    private static final boolean LOGV = false;
    private static final int MSG_PERFORM_POLL = 1;
    private static final int MSG_REGISTER_GLOBAL_ALERT = 3;
    private static final int MSG_UPDATE_IFACES = 2;
    private static final String PREFIX_DEV = "dev";
    private static final String PREFIX_UID = "uid";
    private static final String PREFIX_UID_TAG = "uid_tag";
    private static final String PREFIX_XT = "xt";
    private static final String TAG = "NetworkStats";
    private static final String TAG_NETSTATS_ERROR = "netstats_error";
    public static final String VT_INTERFACE = "vt_data0";
    private static IHwNetworkStatsService mHwNetworkStatsService = null;
    protected String mActiveIface;
    private final ArrayMap<String, NetworkIdentitySet> mActiveIfaces = new ArrayMap();
    private SparseIntArray mActiveUidCounterSet = new SparseIntArray();
    protected final ArrayMap<String, NetworkIdentitySet> mActiveUidIfaces = new ArrayMap();
    private final AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver = new BaseNetworkObserver() {
        public void limitReached(String limitName, String iface) {
            NetworkStatsService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkStatsService.TAG);
            if (NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                NetworkStatsService.this.mHandler.obtainMessage(1, 1, 0).sendToTarget();
                NetworkStatsService.this.mHandler.obtainMessage(3).sendToTarget();
            }
        }
    };
    private final File mBaseDir;
    private IConnectivityManager mConnManager;
    protected final Context mContext;
    private NetworkStatsRecorder mDevRecorder;
    private long mGlobalAlertBytes;
    private Handler mHandler;
    private Callback mHandlerCallback;
    private String[] mMobileIfaces = new String[0];
    private final INetworkManagementService mNetworkManager;
    private final DropBoxNonMonotonicObserver mNonMonotonicObserver = new DropBoxNonMonotonicObserver(this, null);
    private long mPersistThreshold = 2097152;
    private PendingIntent mPollIntent;
    private BroadcastReceiver mPollReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkStatsService.this.performPoll(3);
            NetworkStatsService.this.registerGlobalAlert();
        }
    };
    private BroadcastReceiver mRemovedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("android.intent.extra.UID", -1) != -1) {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.mWakeLock.acquire();
                    try {
                        NetworkStatsService.this.removeUidsLocked(uid);
                        NetworkStatsService.this.mWakeLock.release();
                    } catch (Throwable th) {
                        NetworkStatsService.this.mWakeLock.release();
                    }
                }
            }
        }
    };
    protected final NetworkStatsSettings mSettings;
    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            synchronized (NetworkStatsService.this.mStatsLock) {
                NetworkStatsService.this.shutdownLocked();
            }
        }
    };
    private final Object mStatsLock = new Object();
    private final NetworkStatsObservers mStatsObservers;
    protected final File mSystemDir;
    private boolean mSystemReady;
    private final TelephonyManager mTeleManager;
    private BroadcastReceiver mTetherReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkStatsService.this.performPoll(1);
        }
    };
    private final TrustedTime mTime;
    private long mTimeRefreshRealtime;
    private NetworkStats mUidOperations = new NetworkStats(0, 10);
    private NetworkStatsRecorder mUidRecorder;
    private NetworkStatsRecorder mUidTagRecorder;
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (userId != -1) {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.mWakeLock.acquire();
                    try {
                        NetworkStatsService.this.removeUserLocked(userId);
                        NetworkStatsService.this.mWakeLock.release();
                    } catch (Throwable th) {
                        NetworkStatsService.this.mWakeLock.release();
                    }
                }
            }
        }
    };
    private final WakeLock mWakeLock;
    private NetworkStatsRecorder mXtRecorder;
    private NetworkStatsCollection mXtStatsCached;

    public interface NetworkStatsSettings {

        public static class Config {
            public final long bucketDuration;
            public final long deleteAgeMillis;
            public final long rotateAgeMillis;

            public Config(long bucketDuration, long rotateAgeMillis, long deleteAgeMillis) {
                this.bucketDuration = bucketDuration;
                this.rotateAgeMillis = rotateAgeMillis;
                this.deleteAgeMillis = deleteAgeMillis;
            }
        }

        Config getDevConfig();

        long getDevPersistBytes(long j);

        long getGlobalAlertBytes(long j);

        long getPollInterval();

        boolean getSampleEnabled();

        long getTimeCacheMaxAge();

        Config getUidConfig();

        long getUidPersistBytes(long j);

        Config getUidTagConfig();

        long getUidTagPersistBytes(long j);

        Config getXtConfig();

        long getXtPersistBytes(long j);
    }

    private static class DefaultNetworkStatsSettings implements NetworkStatsSettings {
        private final ContentResolver mResolver;

        public DefaultNetworkStatsSettings(Context context) {
            this.mResolver = (ContentResolver) Preconditions.checkNotNull(context.getContentResolver());
        }

        private long getGlobalLong(String name, long def) {
            return Global.getLong(this.mResolver, name, def);
        }

        private boolean getGlobalBoolean(String name, boolean def) {
            if (Global.getInt(this.mResolver, name, def ? 1 : 0) != 0) {
                return true;
            }
            return false;
        }

        public long getPollInterval() {
            return getGlobalLong("netstats_poll_interval", HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME);
        }

        public long getTimeCacheMaxAge() {
            return getGlobalLong("netstats_time_cache_max_age", UnixCalendar.DAY_IN_MILLIS);
        }

        public long getGlobalAlertBytes(long def) {
            return getGlobalLong("netstats_global_alert_bytes", def);
        }

        public boolean getSampleEnabled() {
            return getGlobalBoolean("netstats_sample_enabled", true);
        }

        public Config getDevConfig() {
            return new Config(getGlobalLong("netstats_dev_bucket_duration", 3600000), getGlobalLong("netstats_dev_rotate_age", 1296000000), getGlobalLong("netstats_dev_delete_age", 7776000000L));
        }

        public Config getXtConfig() {
            return getDevConfig();
        }

        public Config getUidConfig() {
            return new Config(getGlobalLong("netstats_uid_bucket_duration", 7200000), getGlobalLong("netstats_uid_rotate_age", 1296000000), getGlobalLong("netstats_uid_delete_age", 7776000000L));
        }

        public Config getUidTagConfig() {
            return new Config(getGlobalLong("netstats_uid_tag_bucket_duration", 7200000), getGlobalLong("netstats_uid_tag_rotate_age", 432000000), getGlobalLong("netstats_uid_tag_delete_age", 1296000000));
        }

        public long getDevPersistBytes(long def) {
            return getGlobalLong("netstats_dev_persist_bytes", def);
        }

        public long getXtPersistBytes(long def) {
            return getDevPersistBytes(def);
        }

        public long getUidPersistBytes(long def) {
            return getGlobalLong("netstats_uid_persist_bytes", def);
        }

        public long getUidTagPersistBytes(long def) {
            return getGlobalLong("netstats_uid_tag_persist_bytes", def);
        }
    }

    private class DropBoxNonMonotonicObserver implements NonMonotonicObserver<String> {
        /* synthetic */ DropBoxNonMonotonicObserver(NetworkStatsService this$0, DropBoxNonMonotonicObserver -this1) {
            this();
        }

        private DropBoxNonMonotonicObserver() {
        }

        public void foundNonMonotonic(NetworkStats left, int leftIndex, NetworkStats right, int rightIndex, String cookie) {
            Log.w(NetworkStatsService.TAG, "found non-monotonic values; saving to dropbox");
            StringBuilder builder = new StringBuilder();
            builder.append("found non-monotonic ").append(cookie).append(" values at left[").append(leftIndex).append("] - right[").append(rightIndex).append("]\n");
            builder.append("left=").append(left).append(10);
            builder.append("right=").append(right).append(10);
            ((DropBoxManager) NetworkStatsService.this.mContext.getSystemService("dropbox")).addText(NetworkStatsService.TAG_NETSTATS_ERROR, builder.toString());
        }
    }

    static class HandlerCallback implements Callback {
        private final NetworkStatsService mService;

        HandlerCallback(NetworkStatsService service) {
            this.mService = service;
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.mService.performPoll(msg.arg1);
                    return true;
                case 2:
                    this.mService.updateIfaces();
                    return true;
                case 3:
                    this.mService.registerGlobalAlert();
                    return true;
                default:
                    return false;
            }
        }
    }

    private static File getDefaultSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    private static File getDefaultBaseDir() {
        File baseDir = new File(getDefaultSystemDir(), "netstats");
        baseDir.mkdirs();
        return baseDir;
    }

    public static NetworkStatsService create(Context context, INetworkManagementService networkManager) {
        NetworkStatsService service;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        WakeLock wakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
        mHwNetworkStatsService = HwServiceFactory.getHwNetworkStatsService();
        if (mHwNetworkStatsService != null) {
            service = mHwNetworkStatsService.getInstance(context, networkManager, alarmManager, wakeLock, NtpTrustedTime.getInstance(context), TelephonyManager.getDefault(), new DefaultNetworkStatsSettings(context), new NetworkStatsObservers(), getDefaultSystemDir(), getDefaultBaseDir());
        } else {
            service = new NetworkStatsService(context, networkManager, alarmManager, wakeLock, NtpTrustedTime.getInstance(context), TelephonyManager.getDefault(), new DefaultNetworkStatsSettings(context), new NetworkStatsObservers(), getDefaultSystemDir(), getDefaultBaseDir());
        }
        HandlerThread handlerThread = new HandlerThread(TAG);
        Callback callback = new HandlerCallback(service);
        handlerThread.start();
        service.setHandler(new Handler(handlerThread.getLooper(), callback), callback);
        return service;
    }

    NetworkStatsService(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, WakeLock wakeLock, TrustedTime time, TelephonyManager teleManager, NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManager, "missing INetworkManagementService");
        this.mAlarmManager = (AlarmManager) Preconditions.checkNotNull(alarmManager, "missing AlarmManager");
        this.mTime = (TrustedTime) Preconditions.checkNotNull(time, "missing TrustedTime");
        this.mTimeRefreshRealtime = -1;
        this.mSettings = (NetworkStatsSettings) Preconditions.checkNotNull(settings, "missing NetworkStatsSettings");
        this.mTeleManager = (TelephonyManager) Preconditions.checkNotNull(teleManager, "missing TelephonyManager");
        this.mWakeLock = (WakeLock) Preconditions.checkNotNull(wakeLock, "missing WakeLock");
        this.mStatsObservers = (NetworkStatsObservers) Preconditions.checkNotNull(statsObservers, "missing NetworkStatsObservers");
        this.mSystemDir = (File) Preconditions.checkNotNull(systemDir, "missing systemDir");
        this.mBaseDir = (File) Preconditions.checkNotNull(baseDir, "missing baseDir");
    }

    void setHandler(Handler handler, Callback callback) {
        this.mHandler = handler;
        this.mHandlerCallback = callback;
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void systemReady() {
        this.mSystemReady = true;
        if (isBandwidthControlEnabled()) {
            this.mDevRecorder = buildRecorder(PREFIX_DEV, this.mSettings.getDevConfig(), false);
            this.mXtRecorder = buildRecorder(PREFIX_XT, this.mSettings.getXtConfig(), false);
            this.mUidRecorder = buildRecorder(PREFIX_UID, this.mSettings.getUidConfig(), false);
            this.mUidTagRecorder = buildRecorder(PREFIX_UID_TAG, this.mSettings.getUidTagConfig(), true);
            hwInitProcRecorder();
            hwInitUidAndProcRecorder();
            updatePersistThresholds();
            synchronized (this.mStatsLock) {
                maybeUpgradeLegacyStatsLocked();
                this.mXtStatsCached = this.mXtRecorder.getOrLoadCompleteLocked();
                hwInitProcStatsCollection();
                bootstrapStatsLocked();
            }
            this.mContext.registerReceiver(this.mTetherReceiver, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mPollReceiver, new IntentFilter(ACTION_NETWORK_STATS_POLL), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
            this.mContext.registerReceiver(this.mRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_REMOVED"), null, this.mHandler);
            this.mContext.registerReceiver(this.mShutdownReceiver, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
            try {
                this.mNetworkManager.registerObserver(this.mAlertObserver);
            } catch (RemoteException e) {
            }
            registerPollAlarmLocked();
            registerGlobalAlert();
            return;
        }
        Slog.w(TAG, "bandwidth controls disabled, unable to track stats");
    }

    protected NetworkStatsRecorder buildRecorder(String prefix, Config config, boolean includeTags) {
        return new NetworkStatsRecorder(new FileRotator(this.mBaseDir, prefix, config.rotateAgeMillis, config.deleteAgeMillis), this.mNonMonotonicObserver, (DropBoxManager) this.mContext.getSystemService("dropbox"), prefix, config.bucketDuration, includeTags);
    }

    private void shutdownLocked() {
        long currentTime;
        this.mContext.unregisterReceiver(this.mTetherReceiver);
        this.mContext.unregisterReceiver(this.mPollReceiver);
        this.mContext.unregisterReceiver(this.mRemovedReceiver);
        this.mContext.unregisterReceiver(this.mUserReceiver);
        this.mContext.unregisterReceiver(this.mShutdownReceiver);
        if (this.mTime.hasCache()) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        this.mDevRecorder.forcePersistLocked(currentTime);
        this.mXtRecorder.forcePersistLocked(currentTime);
        this.mUidRecorder.forcePersistLocked(currentTime);
        this.mUidTagRecorder.forcePersistLocked(currentTime);
        this.mDevRecorder = null;
        this.mXtRecorder = null;
        this.mUidRecorder = null;
        this.mUidTagRecorder = null;
        this.mXtStatsCached = null;
        hwShutdownLocked(currentTime);
        hwShutdownUidAndProcLocked(currentTime);
        this.mSystemReady = false;
    }

    private void maybeUpgradeLegacyStatsLocked() {
        try {
            File file = new File(this.mSystemDir, "netstats.bin");
            if (file.exists()) {
                this.mDevRecorder.importLegacyNetworkLocked(file);
                file.delete();
            }
            file = new File(this.mSystemDir, "netstats_xt.bin");
            if (file.exists()) {
                file.delete();
            }
            file = new File(this.mSystemDir, "netstats_uid.bin");
            if (file.exists()) {
                this.mUidRecorder.importLegacyUidLocked(file);
                this.mUidTagRecorder.importLegacyUidLocked(file);
                file.delete();
            }
            hwImportLegacyNetworkLocked();
        } catch (IOException e) {
            Log.wtf(TAG, "problem during legacy upgrade", e);
        } catch (OutOfMemoryError e2) {
            Log.wtf(TAG, "problem during legacy upgrade", e2);
        }
    }

    private void registerPollAlarmLocked() {
        if (this.mPollIntent != null) {
            this.mAlarmManager.cancel(this.mPollIntent);
        }
        this.mPollIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_NETWORK_STATS_POLL), 0);
        this.mAlarmManager.setInexactRepeating(3, SystemClock.elapsedRealtime(), this.mSettings.getPollInterval(), this.mPollIntent);
    }

    private void registerGlobalAlert() {
        try {
            this.mNetworkManager.setGlobalAlert(this.mGlobalAlertBytes);
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem registering for global alert: " + e);
        } catch (RemoteException e2) {
        }
    }

    public INetworkStatsSession openSession() {
        return createSession(null, false);
    }

    public INetworkStatsSession openSessionForUsageStats(String callingPackage) {
        return createSession(callingPackage, true);
    }

    private INetworkStatsSession createSession(final String callingPackage, boolean pollOnCreate) {
        assertBandwidthControlEnabled();
        if (pollOnCreate) {
            long ident = Binder.clearCallingIdentity();
            try {
                performPoll(3);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return new Stub() {
            private String mCallingPackage = callingPackage;
            private NetworkStatsCollection mUidAndProcComplete;
            private NetworkStatsCollection mUidComplete;
            private NetworkStatsCollection mUidTagComplete;

            private NetworkStatsCollection getUidComplete() {
                NetworkStatsCollection networkStatsCollection;
                synchronized (NetworkStatsService.this.mStatsLock) {
                    if (this.mUidComplete == null) {
                        this.mUidComplete = NetworkStatsService.this.mUidRecorder.getOrLoadCompleteLocked();
                    }
                    networkStatsCollection = this.mUidComplete;
                }
                return networkStatsCollection;
            }

            private NetworkStatsCollection getUidTagComplete() {
                NetworkStatsCollection networkStatsCollection;
                synchronized (NetworkStatsService.this.mStatsLock) {
                    if (this.mUidTagComplete == null) {
                        this.mUidTagComplete = NetworkStatsService.this.mUidTagRecorder.getOrLoadCompleteLocked();
                    }
                    networkStatsCollection = this.mUidTagComplete;
                }
                return networkStatsCollection;
            }

            private NetworkStatsCollection getUidAndProcComplete() {
                NetworkStatsCollection networkStatsCollection;
                synchronized (NetworkStatsService.this.mStatsLock) {
                    if (this.mUidAndProcComplete == null && NetworkStatsService.this.getUidAndProcNetworkStatsRecorder() != null) {
                        this.mUidAndProcComplete = NetworkStatsService.this.getUidAndProcNetworkStatsRecorder().getOrLoadCompleteLocked();
                    }
                    networkStatsCollection = this.mUidAndProcComplete;
                }
                return networkStatsCollection;
            }

            public int[] getRelevantUids() {
                return getUidComplete().getRelevantUids(NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
            }

            public NetworkStats getDeviceSummaryForNetwork(NetworkTemplate template, long start, long end) {
                if (NetworkStatsService.this.checkAccessLevel(this.mCallingPackage) < 2) {
                    throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access device summary network stats");
                }
                NetworkStats result = new NetworkStats(end - start, 1);
                long ident = Binder.clearCallingIdentity();
                try {
                    result.combineAllValues(NetworkStatsService.this.internalGetSummaryForNetwork(template, start, end, 3));
                    return result;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }

            public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) {
                return NetworkStatsService.this.internalGetSummaryForNetwork(template, start, end, NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
            }

            public NetworkStatsHistory getHistoryForNetwork(NetworkTemplate template, int fields) {
                return NetworkStatsService.this.internalGetHistoryForNetwork(template, fields, NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
            }

            public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end, boolean includeTags) {
                try {
                    int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                    NetworkStats stats = getUidComplete().getSummary(template, start, end, accessLevel);
                    if (includeTags) {
                        stats.combineAllValues(getUidTagComplete().getSummary(template, start, end, accessLevel));
                    }
                    return stats;
                } catch (NullPointerException e) {
                    Slog.wtf(NetworkStatsService.TAG, "NullPointerException in getSummaryForAllUid", e);
                    throw e;
                }
            }

            public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid, int set, int tag, int fields) {
                int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                if (tag == 0) {
                    return getUidComplete().getHistory(template, uid, set, tag, fields, accessLevel);
                }
                return getUidTagComplete().getHistory(template, uid, set, tag, fields, accessLevel);
            }

            public NetworkStatsHistory getHistoryIntervalForUid(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end) {
                int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                if (tag == 0) {
                    return getUidComplete().getHistory(template, uid, set, tag, fields, start, end, accessLevel);
                }
                if (uid == Binder.getCallingUid()) {
                    return getUidTagComplete().getHistory(template, uid, set, tag, fields, start, end, accessLevel);
                }
                throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access tag information from a different uid");
            }

            public NetworkStats getSummaryForAllPid(NetworkTemplate template, long start, long end) {
                NetworkStats stats = getUidAndProcComplete().getSummary(template, start, end, NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
                Slog.i(NetworkStatsService.TAG, "getSummaryForAllPid stats size= " + stats.size());
                return stats;
            }

            public void close() {
                this.mUidComplete = null;
                this.mUidTagComplete = null;
                this.mUidAndProcComplete = null;
            }
        };
    }

    private int checkAccessLevel(String callingPackage) {
        return NetworkStatsAccess.checkAccessLevel(this.mContext, Binder.getCallingUid(), callingPackage);
    }

    private NetworkStats internalGetSummaryForNetwork(NetworkTemplate template, long start, long end, int accessLevel) {
        return this.mXtStatsCached.getSummary(template, start, end, accessLevel);
    }

    private NetworkStatsHistory internalGetHistoryForNetwork(NetworkTemplate template, int fields, int accessLevel) {
        if (this.mXtStatsCached == null) {
            return null;
        }
        return this.mXtStatsCached.getHistory(template, -1, -1, 0, fields, accessLevel);
    }

    public long getNetworkTotalBytes(NetworkTemplate template, long start, long end) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        return internalGetSummaryForNetwork(template, start, end, 3).getTotalBytes();
    }

    public NetworkStats getDataLayerSnapshotForUid(int uid) throws RemoteException {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        }
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            NetworkStats networkLayer = this.mNetworkManager.getNetworkStatsUidDetail(uid);
            networkLayer.spliceOperationsFrom(this.mUidOperations);
            NetworkStats dataLayer = new NetworkStats(networkLayer.getElapsedRealtime(), networkLayer.size());
            Entry entry = null;
            for (int i = 0; i < networkLayer.size(); i++) {
                entry = networkLayer.getValues(i, entry);
                entry.iface = NetworkStats.IFACE_ALL;
                dataLayer.combineValues(entry);
            }
            return dataLayer;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public String[] getMobileIfaces() {
        return this.mMobileIfaces;
    }

    public void incrementOperationCount(int uid, int tag, int operationCount) {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
        }
        if (operationCount < 0) {
            throw new IllegalArgumentException("operation count can only be incremented");
        } else if (tag == 0) {
            throw new IllegalArgumentException("operation count must have specific tag");
        } else {
            synchronized (this.mStatsLock) {
                int set = this.mActiveUidCounterSet.get(uid, 0);
                this.mUidOperations.combineValues(this.mActiveIface, uid, set, tag, 0, 0, 0, 0, (long) operationCount);
                this.mUidOperations.combineValues(this.mActiveIface, uid, set, 0, 0, 0, 0, 0, (long) operationCount);
            }
        }
    }

    public void setUidForeground(int uid, boolean uidForeground) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
        synchronized (this.mStatsLock) {
            int set = uidForeground ? 1 : 0;
            if (this.mActiveUidCounterSet.get(uid, 0) != set) {
                this.mActiveUidCounterSet.put(uid, set);
                NetworkManagementSocketTagger.setKernelCounterSet(uid, set);
            }
        }
    }

    public void forceUpdateIfaces() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            updateIfaces();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void forceUpdate() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            performPoll(3);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void advisePersistThreshold(long thresholdBytes) {
        long currentTime;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
        assertBandwidthControlEnabled();
        this.mPersistThreshold = MathUtils.constrain(thresholdBytes, 131072, 2097152);
        if (this.mTime.hasCache()) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        synchronized (this.mStatsLock) {
            if (this.mSystemReady) {
                updatePersistThresholds();
                this.mDevRecorder.maybePersistLocked(currentTime);
                this.mXtRecorder.maybePersistLocked(currentTime);
                this.mUidRecorder.maybePersistLocked(currentTime);
                this.mUidTagRecorder.maybePersistLocked(currentTime);
                hwMaybePersistLocked(currentTime);
                hwMaybeUidAndProcPersistLocked(currentTime);
                registerGlobalAlert();
                return;
            }
        }
    }

    public DataUsageRequest registerUsageCallback(String callingPackage, DataUsageRequest request, Messenger messenger, IBinder binder) {
        Preconditions.checkNotNull(callingPackage, "calling package is null");
        Preconditions.checkNotNull(request, "DataUsageRequest is null");
        Preconditions.checkNotNull(request.template, "NetworkTemplate is null");
        Preconditions.checkNotNull(messenger, "messenger is null");
        Preconditions.checkNotNull(binder, "binder is null");
        int callingUid = Binder.getCallingUid();
        int accessLevel = checkAccessLevel(callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            DataUsageRequest normalizedRequest = this.mStatsObservers.register(request, messenger, binder, callingUid, accessLevel);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, Integer.valueOf(3)));
            return normalizedRequest;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void unregisterUsageRequest(DataUsageRequest request) {
        Preconditions.checkNotNull(request, "DataUsageRequest is null");
        int callingUid = Binder.getCallingUid();
        long token = Binder.clearCallingIdentity();
        try {
            this.mStatsObservers.unregister(request, callingUid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void updatePersistThresholds() {
        this.mDevRecorder.setPersistThreshold(this.mSettings.getDevPersistBytes(this.mPersistThreshold));
        this.mXtRecorder.setPersistThreshold(this.mSettings.getXtPersistBytes(this.mPersistThreshold));
        this.mUidRecorder.setPersistThreshold(this.mSettings.getUidPersistBytes(this.mPersistThreshold));
        this.mUidTagRecorder.setPersistThreshold(this.mSettings.getUidTagPersistBytes(this.mPersistThreshold));
        hwUpdateProcPersistThresholds(this.mPersistThreshold);
        hwUpdateProcPersistThresholds(this.mPersistThreshold);
        this.mGlobalAlertBytes = this.mSettings.getGlobalAlertBytes(this.mPersistThreshold);
    }

    private void updateIfaces() {
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                updateIfacesLocked();
                this.mWakeLock.release();
            } catch (Throwable th) {
                this.mWakeLock.release();
            }
        }
    }

    private void updateIfacesLocked() {
        if (this.mSystemReady) {
            performPollLocked(1);
            try {
                NetworkState[] states = this.mConnManager.getAllNetworkState();
                LinkProperties activeLink = this.mConnManager.getActiveLinkProperties();
                this.mActiveIface = activeLink != null ? activeLink.getInterfaceName() : null;
                this.mActiveIfaces.clear();
                this.mActiveUidIfaces.clear();
                ArraySet<String> mobileIfaces = new ArraySet();
                int i = 0;
                int length = states.length;
                while (true) {
                    int i2 = i;
                    if (i2 < length) {
                        NetworkState state = states[i2];
                        if (state.networkInfo.isConnected()) {
                            boolean isMobile = ConnectivityManager.isNetworkTypeMobile(state.networkInfo.getType());
                            NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                            String baseIface = state.linkProperties.getInterfaceName();
                            if (baseIface != null) {
                                findOrCreateNetworkIdentitySet(this.mActiveIfaces, baseIface).add(ident);
                                findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, baseIface).add(ident);
                                if (state.networkCapabilities.hasCapability(4) && (ident.getMetered() ^ 1) != 0) {
                                    NetworkIdentity vtIdent = new NetworkIdentity(ident.getType(), ident.getSubType(), ident.getSubscriberId(), ident.getNetworkId(), ident.getRoaming(), true);
                                    findOrCreateNetworkIdentitySet(this.mActiveIfaces, VT_INTERFACE).add(vtIdent);
                                    findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, VT_INTERFACE).add(vtIdent);
                                }
                                if (isMobile) {
                                    mobileIfaces.add(baseIface);
                                }
                            }
                            for (LinkProperties stackedLink : state.linkProperties.getStackedLinks()) {
                                String stackedIface = stackedLink.getInterfaceName();
                                if (stackedIface != null) {
                                    findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, stackedIface).add(ident);
                                    if (isMobile) {
                                        mobileIfaces.add(stackedIface);
                                    }
                                }
                            }
                        }
                        i = i2 + 1;
                    } else {
                        this.mMobileIfaces = (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
                        return;
                    }
                }
            } catch (RemoteException e) {
            }
        }
    }

    private static <K> NetworkIdentitySet findOrCreateNetworkIdentitySet(ArrayMap<K, NetworkIdentitySet> map, K key) {
        NetworkIdentitySet ident = (NetworkIdentitySet) map.get(key);
        if (ident != null) {
            return ident;
        }
        ident = new NetworkIdentitySet();
        map.put(key, ident);
        return ident;
    }

    private void recordSnapshotLocked(long currentTime) throws RemoteException {
        NetworkStats uidSnapshot = getNetworkStatsUidDetail();
        NetworkStats xtSnapshot = getNetworkStatsXtAndVt();
        NetworkStats devSnapshot = this.mNetworkManager.getNetworkStatsSummaryDev();
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            NetPluginDelegate.getTetherStats(uidSnapshot, xtSnapshot, devSnapshot);
        }
        this.mDevRecorder.recordSnapshotLocked(devSnapshot, this.mActiveIfaces, null, currentTime);
        this.mXtRecorder.recordSnapshotLocked(xtSnapshot, this.mActiveIfaces, null, currentTime);
        VpnInfo[] vpnArray = this.mConnManager.getAllVpnInfo();
        this.mUidRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        this.mUidTagRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        hwRecordSnapshotLocked(currentTime);
        hwRecordUidAndProcSnapshotLocked(currentTime);
        this.mStatsObservers.updateStats(xtSnapshot, uidSnapshot, new ArrayMap(this.mActiveIfaces), new ArrayMap(this.mActiveUidIfaces), vpnArray, currentTime);
    }

    private void bootstrapStatsLocked() {
        long currentTime;
        if (this.mTime.hasCache()) {
            currentTime = this.mTime.currentTimeMillis();
        } else {
            currentTime = System.currentTimeMillis();
        }
        try {
            recordSnapshotLocked(currentTime);
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
        } catch (RemoteException e2) {
        }
    }

    private void performPoll(int flags) {
        if (getTimeRefreshElapsedRealtime() > this.mSettings.getTimeCacheMaxAge()) {
            this.mTimeRefreshRealtime = SystemClock.elapsedRealtime();
            new Thread() {
                public void run() {
                    NetworkStatsService.this.mTime.forceRefresh();
                }
            }.start();
        }
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                performPollLocked(flags);
                this.mWakeLock.release();
            } catch (Throwable th) {
                this.mWakeLock.release();
            }
        }
    }

    private void performPollLocked(int flags) {
        if (this.mSystemReady) {
            long currentTime;
            long startRealtime = SystemClock.elapsedRealtime();
            boolean persistNetwork = (flags & 1) != 0;
            boolean persistUid = (flags & 2) != 0;
            boolean persistForce = (flags & 256) != 0;
            if (this.mTime.hasCache()) {
                currentTime = this.mTime.currentTimeMillis();
            } else {
                currentTime = System.currentTimeMillis();
            }
            try {
                recordSnapshotLocked(currentTime);
                if (persistForce) {
                    this.mDevRecorder.forcePersistLocked(currentTime);
                    this.mXtRecorder.forcePersistLocked(currentTime);
                    this.mUidRecorder.forcePersistLocked(currentTime);
                    this.mUidTagRecorder.forcePersistLocked(currentTime);
                    hwForcePersistLocked(currentTime);
                    hwForceUidAndProcPersistLocked(currentTime);
                } else {
                    if (persistNetwork) {
                        this.mDevRecorder.maybePersistLocked(currentTime);
                        this.mXtRecorder.maybePersistLocked(currentTime);
                    }
                    if (persistUid) {
                        this.mUidRecorder.maybePersistLocked(currentTime);
                        this.mUidTagRecorder.maybePersistLocked(currentTime);
                        hwMaybePersistLocked(currentTime);
                        hwMaybeUidAndProcPersistLocked(currentTime);
                    }
                }
                if (this.mSettings.getSampleEnabled()) {
                    performSampleLocked();
                }
                Intent updatedIntent = new Intent(ACTION_NETWORK_STATS_UPDATED);
                updatedIntent.setFlags(1073741824);
                this.mContext.sendBroadcastAsUser(updatedIntent, UserHandle.ALL, "android.permission.READ_NETWORK_USAGE_HISTORY");
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem reading network stats", e);
            } catch (RemoteException e2) {
            }
        }
    }

    private void performSampleLocked() {
        long trustedTime = this.mTime.hasCache() ? this.mTime.currentTimeMillis() : -1;
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileWildcard();
        Entry devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        Entry xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        Entry uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsMobileSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, trustedTime);
        template = NetworkTemplate.buildTemplateWifiWildcard();
        devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsWifiSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, trustedTime);
    }

    private void removeUidsLocked(int... uids) {
        performPollLocked(3);
        if (this.mUidRecorder != null) {
            this.mUidRecorder.removeUidsLocked(uids);
        }
        if (this.mUidTagRecorder != null) {
            this.mUidTagRecorder.removeUidsLocked(uids);
        }
        for (int uid : uids) {
            NetworkManagementSocketTagger.resetKernelUidStats(uid);
        }
    }

    private void removeUserLocked(int userId) {
        int[] uids = new int[0];
        for (ApplicationInfo app : this.mContext.getPackageManager().getInstalledApplications(4194816)) {
            uids = ArrayUtils.appendInt(uids, UserHandle.getUid(userId, app.uid));
        }
        removeUidsLocked(uids);
    }

    /* JADX WARNING: Missing block: B:51:0x012f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:67:0x020f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter rawWriter, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, rawWriter)) {
            long duration = UnixCalendar.DAY_IN_MILLIS;
            HashSet<String> argSet = new HashSet();
            for (String arg : args) {
                argSet.add(arg);
                if (arg.startsWith("--duration=")) {
                    try {
                        duration = Long.parseLong(arg.substring(11));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            boolean poll = !argSet.contains("--poll") ? argSet.contains("poll") : true;
            boolean checkin = argSet.contains("--checkin");
            boolean fullHistory = !argSet.contains("--full") ? argSet.contains("full") : true;
            boolean includeUid = !argSet.contains("--uid") ? argSet.contains("detail") : true;
            boolean includeTag = !argSet.contains("--tag") ? argSet.contains("detail") : true;
            IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(rawWriter, "  ");
            synchronized (this.mStatsLock) {
                if (args.length > 0 && "--proto".equals(args[0])) {
                    dumpProto(fd);
                } else if (poll) {
                    performPollLocked(LightsManager.LIGHT_ID_MANUALCUSTOMBACKLIGHT);
                    indentingPrintWriter.println("Forced poll");
                } else if (checkin) {
                    long end = System.currentTimeMillis();
                    long start = end - duration;
                    indentingPrintWriter.print("v1,");
                    indentingPrintWriter.print(start / 1000);
                    indentingPrintWriter.print(',');
                    indentingPrintWriter.print(end / 1000);
                    indentingPrintWriter.println();
                    indentingPrintWriter.println(PREFIX_XT);
                    this.mXtRecorder.dumpCheckin(rawWriter, start, end);
                    if (includeUid) {
                        indentingPrintWriter.println(PREFIX_UID);
                        this.mUidRecorder.dumpCheckin(rawWriter, start, end);
                    }
                    if (includeTag) {
                        indentingPrintWriter.println("tag");
                        this.mUidTagRecorder.dumpCheckin(rawWriter, start, end);
                    }
                } else {
                    int i;
                    indentingPrintWriter.println("Active interfaces:");
                    indentingPrintWriter.increaseIndent();
                    for (i = 0; i < this.mActiveIfaces.size(); i++) {
                        indentingPrintWriter.printPair("iface", this.mActiveIfaces.keyAt(i));
                        indentingPrintWriter.printPair("ident", this.mActiveIfaces.valueAt(i));
                        indentingPrintWriter.println();
                    }
                    indentingPrintWriter.decreaseIndent();
                    indentingPrintWriter.println("Active UID interfaces:");
                    indentingPrintWriter.increaseIndent();
                    for (i = 0; i < this.mActiveUidIfaces.size(); i++) {
                        indentingPrintWriter.printPair("iface", this.mActiveUidIfaces.keyAt(i));
                        indentingPrintWriter.printPair("ident", this.mActiveUidIfaces.valueAt(i));
                        indentingPrintWriter.println();
                    }
                    indentingPrintWriter.decreaseIndent();
                    indentingPrintWriter.println("Dev stats:");
                    indentingPrintWriter.increaseIndent();
                    this.mDevRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                    indentingPrintWriter.decreaseIndent();
                    indentingPrintWriter.println("Xt stats:");
                    indentingPrintWriter.increaseIndent();
                    this.mXtRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                    indentingPrintWriter.decreaseIndent();
                    if (includeUid) {
                        indentingPrintWriter.println("UID stats:");
                        indentingPrintWriter.increaseIndent();
                        this.mUidRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                        indentingPrintWriter.decreaseIndent();
                    }
                    if (includeTag) {
                        indentingPrintWriter.println("UID tag stats:");
                        indentingPrintWriter.increaseIndent();
                        this.mUidTagRecorder.dumpLocked(indentingPrintWriter, fullHistory);
                        indentingPrintWriter.decreaseIndent();
                    }
                }
            }
        }
    }

    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        dumpInterfaces(proto, 2272037699585L, this.mActiveIfaces);
        dumpInterfaces(proto, 2272037699586L, this.mActiveUidIfaces);
        this.mDevRecorder.writeToProtoLocked(proto, 1172526071811L);
        this.mXtRecorder.writeToProtoLocked(proto, 1172526071812L);
        this.mUidRecorder.writeToProtoLocked(proto, 1172526071813L);
        this.mUidTagRecorder.writeToProtoLocked(proto, 1172526071814L);
        proto.flush();
    }

    private static void dumpInterfaces(ProtoOutputStream proto, long tag, ArrayMap<String, NetworkIdentitySet> ifaces) {
        for (int i = 0; i < ifaces.size(); i++) {
            long start = proto.start(tag);
            proto.write(1159641169921L, (String) ifaces.keyAt(i));
            ((NetworkIdentitySet) ifaces.valueAt(i)).writeToProto(proto, 1172526071810L);
            proto.end(start);
        }
    }

    private NetworkStats getNetworkStatsUidDetail() throws RemoteException {
        NetworkStats uidSnapshot = this.mNetworkManager.getNetworkStatsUidDetail(-1);
        uidSnapshot.combineAllValues(getNetworkStatsTethering());
        uidSnapshot.combineAllValues(this.mUidOperations);
        return uidSnapshot;
    }

    private NetworkStats getNetworkStatsXtAndVt() throws RemoteException {
        NetworkStats xtSnapshot = this.mNetworkManager.getNetworkStatsSummaryXt();
        long usage = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage();
        NetworkStats vtSnapshot = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        Entry entry = new Entry();
        entry.iface = VT_INTERFACE;
        entry.uid = -1;
        entry.set = -1;
        entry.tag = 0;
        entry.rxBytes = usage / 2;
        entry.rxPackets = 0;
        entry.txBytes = usage - entry.rxBytes;
        entry.txPackets = 0;
        vtSnapshot.combineValues(entry);
        xtSnapshot.combineAllValues(vtSnapshot);
        return xtSnapshot;
    }

    private NetworkStats getNetworkStatsTethering() throws RemoteException {
        try {
            return this.mNetworkManager.getNetworkStatsTethering();
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem reading network stats", e);
            return new NetworkStats(0, 10);
        }
    }

    private void assertBandwidthControlEnabled() {
        if (!isBandwidthControlEnabled()) {
            throw new IllegalStateException("Bandwidth module disabled");
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

    public long getTimeRefreshElapsedRealtime() {
        if (this.mTimeRefreshRealtime != -1) {
            return SystemClock.elapsedRealtime() - this.mTimeRefreshRealtime;
        }
        return JobStatus.NO_LATEST_RUNTIME;
    }
}
