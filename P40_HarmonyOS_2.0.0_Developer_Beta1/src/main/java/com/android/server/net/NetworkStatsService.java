package com.android.server.net;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.IUidObserver;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.DataUsageRequest;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkIdentity;
import android.net.NetworkInfo;
import android.net.NetworkStack;
import android.net.NetworkState;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.BestClock;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionPlan;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.net.VpnInfo;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FileRotator;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.NetworkManagementService;
import com.android.server.NetworkManagementSocketTagger;
import com.android.server.ServiceThread;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsManager;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.AppStandbyController;
import com.android.server.utils.PriorityDump;
import com.huawei.android.net.IHwNetworkStats;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class NetworkStatsService extends INetworkStatsService.Stub implements IHwNetworkStatsInner {
    @VisibleForTesting
    public static final String ACTION_NETWORK_STATS_POLL = "com.android.server.action.NETWORK_STATS_POLL";
    public static final String ACTION_NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
    private static final boolean DEBUG;
    private static final int DOMESTIC_BETA_VERSION = 3;
    private static final int DUMP_STATS_SESSION_COUNT = 20;
    private static final int FLAG_PERSIST_ALL = 3;
    private static final int FLAG_PERSIST_FORCE = 256;
    private static final int FLAG_PERSIST_NETWORK = 1;
    private static final int FLAG_PERSIST_UID = 2;
    static final boolean LOGD = Log.isLoggable(TAG, 3);
    static final boolean LOGV = Log.isLoggable(TAG, 2);
    private static final int MSG_PERFORM_POLL = 1;
    private static final int MSG_PERFORM_POLL_REGISTER_ALERT = 2;
    private static final int MY_PID = Process.myPid();
    private static final int PERFORM_POLL_DELAY_MS = 1000;
    private static final int PERFORM_POLL_DELAY_MS_MINIMUM = 100;
    private static final long POLL_RATE_LIMIT_MS = 15000;
    private static final String PREFIX_DEV = "dev";
    private static final String PREFIX_UID = "uid";
    private static final String PREFIX_UID_TAG = "uid_tag";
    private static final String PREFIX_XT = "xt";
    static final String TAG = "NetworkStats";
    private static final String TAG_NETSTATS_ERROR = "netstats_error";
    private static int TYPE_RX_BYTES = 0;
    private static int TYPE_RX_PACKETS = 0;
    private static int TYPE_TCP_RX_PACKETS = 0;
    private static int TYPE_TCP_TX_PACKETS = 0;
    private static int TYPE_TX_BYTES = 0;
    private static int TYPE_TX_PACKETS = 0;
    private static final int UID_MSG_ACTIVE = 102;
    private static final int UID_MSG_GONE = 101;
    private static final int UID_MSG_IDLE = 103;
    private static final int UID_MSG_STATE_CHANGED = 100;
    private static final int USER_TYPE = SystemProperties.getInt("ro.logsystem.usertype", 1);
    public static final String VT_INTERFACE = "vt_data0";
    private static boolean sMpTrafficEnabled = false;
    @GuardedBy({"mStatsLock"})
    protected String mActiveIface;
    @GuardedBy({"mStatsLock"})
    private final ArrayMap<String, NetworkIdentitySet> mActiveIfaces = new ArrayMap<>();
    private SparseIntArray mActiveUidCounterSet = new SparseIntArray();
    @GuardedBy({"mStatsLock"})
    protected final ArrayMap<String, NetworkIdentitySet> mActiveUidIfaces = new ArrayMap<>();
    final IActivityManager mActivityManager = ActivityManager.getService();
    private final AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver = new BaseNetworkObserver() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass9 */

        public void limitReached(String limitName, String iface) {
            NetworkStatsService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkStatsService.TAG);
            if (NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName) && !NetworkStatsService.this.mHandler.hasMessages(2)) {
                NetworkStatsService.this.mHandler.sendEmptyMessageDelayed(2, (long) NetworkStatsService.this.mHwInnerService.getPerformPollDelay());
            }
        }
    };
    private final File mBaseDir;
    private final Clock mClock;
    protected final Context mContext;
    @GuardedBy({"mStatsLock"})
    private Network[] mDefaultNetworks = new Network[0];
    @GuardedBy({"mStatsLock"})
    private NetworkStatsRecorder mDevRecorder;
    private long mGlobalAlertBytes;
    private Handler mHandler;
    private Handler.Callback mHandlerCallback;
    HwInnerNetworkStatsService mHwInnerService = new HwInnerNetworkStatsService(this);
    IHwNetworkStatsServiceEx mHwNetworkStatsServiceEx = null;
    private long mLastStatsSessionPoll;
    @GuardedBy({"mStatsLock"})
    private volatile String[] mMobileIfaces = new String[0];
    private final INetworkManagementService mNetworkManager;
    private final DropBoxNonMonotonicObserver mNonMonotonicObserver = new DropBoxNonMonotonicObserver();
    @GuardedBy({"mOpenSessionCallsPerUid"})
    private final SparseIntArray mOpenSessionCallsPerUid = new SparseIntArray();
    final PackageManagerService mPMS = ((PackageManagerService) ServiceManager.getService("package"));
    private long mPersistThreshold = 2097152;
    private PendingIntent mPollIntent;
    private BroadcastReceiver mPollReceiver = new BroadcastReceiver() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Slog.i(NetworkStatsService.TAG, "Alarm manager triggered");
            NetworkStatsService.this.performPoll(3);
            NetworkStatsService.this.registerGlobalAlert();
        }
    };
    private BroadcastReceiver mRemovedReceiver = new BroadcastReceiver() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
            if (uid != -1) {
                Slog.i(NetworkStatsService.TAG, "ACTION_UID_REMOVED onReceive");
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.mWakeLock.acquire();
                    try {
                        NetworkStatsService.this.removeUidsLocked(uid);
                    } finally {
                        NetworkStatsService.this.mWakeLock.release();
                    }
                }
            }
        }
    };
    protected final NetworkStatsSettings mSettings;
    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass8 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (NetworkStatsService.this.mStatsLock) {
                NetworkStatsService.this.shutdownLocked();
            }
        }
    };
    private final Object mStatsLock = new Object();
    private final NetworkStatsObservers mStatsObservers;
    protected final File mSystemDir;
    private volatile boolean mSystemReady;
    private final TelephonyManager mTeleManager;
    private BroadcastReceiver mTetherReceiver = new BroadcastReceiver() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Slog.i(NetworkStatsService.TAG, "ACTION_TETHER_STATE_CHANGED onReceive");
            NetworkStatsService.this.performPoll(1);
        }
    };
    private long mTimeRefreshRealtime;
    public final Handler mUidEventHandler;
    private final Handler.Callback mUidEventHandlerCallback = new Handler.Callback() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass1 */

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    NetworkStatsService.this.handleUidChanged(msg.arg1, msg.arg2, ((Long) msg.obj).longValue());
                    return true;
                case 101:
                    NetworkStatsService.this.handleUidGone(msg.arg1);
                    return true;
                case 102:
                    return true;
                case 103:
                    return true;
                default:
                    return false;
            }
        }
    };
    private final ServiceThread mUidEventThread;
    private IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass2 */

        public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
            if (NetworkStatsService.this.mUidEventHandler != null) {
                NetworkStatsService.this.mUidEventHandler.obtainMessage(100, uid, procState, Long.valueOf(procStateSeq)).sendToTarget();
            }
        }

        public void onUidGone(int uid, boolean disabled) throws RemoteException {
            if (NetworkStatsService.this.mUidEventHandler != null) {
                NetworkStatsService.this.mUidEventHandler.obtainMessage(101, uid, 0).sendToTarget();
            }
        }

        public void onUidActive(int uid) throws RemoteException {
            if (NetworkStatsService.this.mUidEventHandler != null) {
                NetworkStatsService.this.mUidEventHandler.obtainMessage(102, uid, 0).sendToTarget();
            }
        }

        public void onUidIdle(int uid, boolean disabled) throws RemoteException {
            if (NetworkStatsService.this.mUidEventHandler != null) {
                NetworkStatsService.this.mUidEventHandler.obtainMessage(103, uid, 0).sendToTarget();
            }
        }

        public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {
        }
    };
    private NetworkStats mUidOperations = new NetworkStats(0, 10);
    @GuardedBy({"mStatsLock"})
    private NetworkStatsRecorder mUidRecorder;
    final Object mUidRulesFirstLock = new Object();
    @GuardedBy({"mStatsLock"})
    private NetworkStatsRecorder mUidTagRecorder;
    private int mUpdateStatsCount = 0;
    private final boolean mUseBpfTrafficStats;
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        /* class com.android.server.net.NetworkStatsService.AnonymousClass7 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (userId != -1) {
                Slog.i(NetworkStatsService.TAG, "ACTION_USER_REMOVED onReceive");
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.mWakeLock.acquire();
                    try {
                        NetworkStatsService.this.removeUserLocked(userId);
                    } finally {
                        NetworkStatsService.this.mWakeLock.release();
                    }
                }
            }
        }
    };
    @GuardedBy({"mStatsLock"})
    private VpnInfo[] mVpnInfos = new VpnInfo[0];
    private final PowerManager.WakeLock mWakeLock;
    @GuardedBy({"mStatsLock"})
    private NetworkStatsRecorder mXtRecorder;
    @GuardedBy({"mStatsLock"})
    private NetworkStatsCollection mXtStatsCached;

    private static native long nativeGetIfaceStat(String str, int i, boolean z);

    private static native long nativeGetTotalStat(int i, boolean z);

    private static native long nativeGetUidStat(int i, int i2, boolean z);

    private static native int nativeTagSocket(FileDescriptor fileDescriptor, int i, int i2);

    static {
        boolean z = true;
        if (USER_TYPE != 3) {
            z = false;
        }
        DEBUG = z;
    }

    public interface NetworkStatsSettings {
        boolean getAugmentEnabled();

        Config getDevConfig();

        long getDevPersistBytes(long j);

        long getGlobalAlertBytes(long j);

        long getPollInterval();

        boolean getSampleEnabled();

        Config getUidConfig();

        long getUidPersistBytes(long j);

        Config getUidTagConfig();

        long getUidTagPersistBytes(long j);

        Config getXtConfig();

        long getXtPersistBytes(long j);

        public static class Config {
            public final long bucketDuration;
            public final long deleteAgeMillis;
            public final long rotateAgeMillis;

            public Config(long bucketDuration2, long rotateAgeMillis2, long deleteAgeMillis2) {
                this.bucketDuration = bucketDuration2;
                this.rotateAgeMillis = rotateAgeMillis2;
                this.deleteAgeMillis = deleteAgeMillis2;
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

    private static Clock getDefaultClock() {
        return new BestClock(ZoneOffset.UTC, new Clock[]{SystemClock.currentNetworkTimeClock(), Clock.systemUTC()});
    }

    /* access modifiers changed from: private */
    public static final class NetworkStatsHandler extends Handler {
        NetworkStatsHandler(Looper looper, Handler.Callback callback) {
            super(looper, callback);
        }
    }

    public static NetworkStatsService create(Context context, INetworkManagementService networkManager) {
        NetworkStatsService service = new NetworkStatsService(context, networkManager, (AlarmManager) context.getSystemService("alarm"), ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG), getDefaultClock(), TelephonyManager.getDefault(), new DefaultNetworkStatsSettings(context), new NetworkStatsObservers(), getDefaultSystemDir(), getDefaultBaseDir());
        service.registerLocalService();
        HandlerThread handlerThread = new HandlerThread(TAG);
        Handler.Callback callback = new HandlerCallback(service);
        handlerThread.start();
        service.setHandler(new NetworkStatsHandler(handlerThread.getLooper(), callback), callback);
        return service;
    }

    @VisibleForTesting
    NetworkStatsService(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, PowerManager.WakeLock wakeLock, Clock clock, TelephonyManager teleManager, NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
        this.mHwNetworkStatsServiceEx = HwServiceExFactory.getHwNetworkStatsServiceEx(this, context);
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManager, "missing INetworkManagementService");
        this.mAlarmManager = (AlarmManager) Preconditions.checkNotNull(alarmManager, "missing AlarmManager");
        this.mTimeRefreshRealtime = -1;
        this.mClock = (Clock) Preconditions.checkNotNull(clock, "missing Clock");
        this.mSettings = (NetworkStatsSettings) Preconditions.checkNotNull(settings, "missing NetworkStatsSettings");
        this.mTeleManager = (TelephonyManager) Preconditions.checkNotNull(teleManager, "missing TelephonyManager");
        this.mWakeLock = (PowerManager.WakeLock) Preconditions.checkNotNull(wakeLock, "missing WakeLock");
        this.mStatsObservers = (NetworkStatsObservers) Preconditions.checkNotNull(statsObservers, "missing NetworkStatsObservers");
        this.mSystemDir = (File) Preconditions.checkNotNull(systemDir, "missing systemDir");
        this.mBaseDir = (File) Preconditions.checkNotNull(baseDir, "missing baseDir");
        this.mUseBpfTrafficStats = new File("/sys/fs/bpf/map_netd_app_uid_stats_map").exists();
        this.mUidEventThread = new ServiceThread("NetworkStats.uid", -2, false);
        this.mUidEventThread.start();
        this.mUidEventHandler = new Handler(this.mUidEventThread.getLooper(), this.mUidEventHandlerCallback);
        try {
            this.mActivityManager.registerUidObserver(this.mUidObserver, 3, -1, (String) null);
        } catch (RemoteException e) {
            Slog.w(TAG, "ignored;both services live in system_server");
        }
        try {
            List<ActivityManager.RunningAppProcessInfo> procInfos = this.mActivityManager.getRunningAppProcesses();
            for (int i = 0; i < procInfos.size(); i++) {
                NetworkStatsRecorder.initeSilent(procInfos.get(i).uid);
            }
        } catch (RemoteException e2) {
            Slog.w(TAG, "getRunningAppProcesses RemoteException");
        }
    }

    private void registerLocalService() {
        LocalServices.addService(NetworkStatsManagerInternal.class, new NetworkStatsManagerInternalImpl());
    }

    private boolean isUidStateForegroundUL(int state) {
        return state <= 2 || state == 5;
    }

    /* access modifiers changed from: package-private */
    public void handleUidChanged(int uid, int procState, long procStateSeq) {
        try {
            synchronized (this.mUidRulesFirstLock) {
                String[] temp = this.mPMS.getPackagesForUid(uid);
                int len = 0;
                if (temp != null) {
                    len = temp.length;
                }
                for (int i = 0; i < len; i++) {
                    NetworkStatsRecorder.operatorAppwatch(uid, isUidStateForegroundUL(procState));
                }
            }
        } catch (Exception e) {
            Slog.w(TAG, "handleUidChanged Exception");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUidGone(int uid) {
        try {
            synchronized (this.mUidRulesFirstLock) {
                String[] temp = this.mPMS.getPackagesForUid(uid);
                int len = 0;
                if (temp != null) {
                    len = temp.length;
                }
                for (int i = 0; i < len; i++) {
                    NetworkStatsRecorder.operatorAppwatch(uid);
                }
            }
        } catch (Exception e) {
            Slog.w(TAG, "handleUidGone Exception");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setHandler(Handler handler, Handler.Callback callback) {
        this.mHandler = handler;
        this.mHandlerCallback = callback;
    }

    public void systemReady() {
        this.mSystemReady = true;
        if (!isBandwidthControlEnabled()) {
            Slog.w(TAG, "bandwidth controls disabled, unable to track stats");
            return;
        }
        synchronized (this.mStatsLock) {
            this.mDevRecorder = buildRecorder(PREFIX_DEV, this.mSettings.getDevConfig(), false);
            this.mXtRecorder = buildRecorder(PREFIX_XT, this.mSettings.getXtConfig(), false);
            this.mUidRecorder = buildRecorder("uid", this.mSettings.getUidConfig(), false);
            this.mUidTagRecorder = buildRecorder(PREFIX_UID_TAG, this.mSettings.getUidTagConfig(), true);
            this.mHwInnerService.hwInitUidAndProcRecorder();
            updatePersistThresholdsLocked();
            maybeUpgradeLegacyStatsLocked();
            this.mXtStatsCached = this.mXtRecorder.getOrLoadCompleteLocked();
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
    }

    /* access modifiers changed from: protected */
    public NetworkStatsRecorder buildRecorder(String prefix, NetworkStatsSettings.Config config, boolean includeTags) {
        return new NetworkStatsRecorder(new FileRotator(this.mBaseDir, prefix, config.rotateAgeMillis, config.deleteAgeMillis), this.mNonMonotonicObserver, (DropBoxManager) this.mContext.getSystemService("dropbox"), prefix, config.bucketDuration, includeTags);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mStatsLock"})
    private void shutdownLocked() {
        this.mContext.unregisterReceiver(this.mTetherReceiver);
        this.mContext.unregisterReceiver(this.mPollReceiver);
        this.mContext.unregisterReceiver(this.mRemovedReceiver);
        this.mContext.unregisterReceiver(this.mUserReceiver);
        this.mContext.unregisterReceiver(this.mShutdownReceiver);
        long currentTime = this.mClock.millis();
        this.mDevRecorder.forcePersistLocked(currentTime);
        this.mXtRecorder.forcePersistLocked(currentTime);
        this.mUidRecorder.forcePersistLocked(currentTime);
        this.mUidTagRecorder.forcePersistLocked(currentTime);
        this.mHwInnerService.hwShutdownUidAndProcLocked(currentTime);
        this.mSystemReady = false;
    }

    @GuardedBy({"mStatsLock"})
    private void maybeUpgradeLegacyStatsLocked() {
        try {
            File file = new File(this.mSystemDir, "netstats.bin");
            if (file.exists()) {
                this.mDevRecorder.importLegacyNetworkLocked(file);
                file.delete();
            }
            File file2 = new File(this.mSystemDir, "netstats_xt.bin");
            if (file2.exists()) {
                file2.delete();
            }
            File file3 = new File(this.mSystemDir, "netstats_uid.bin");
            if (file3.exists()) {
                this.mUidRecorder.importLegacyUidLocked(file3);
                this.mUidTagRecorder.importLegacyUidLocked(file3);
                file3.delete();
            }
        } catch (IOException e) {
            Log.wtf(TAG, "problem during legacy upgrade", e);
        } catch (OutOfMemoryError e2) {
            Log.wtf(TAG, "problem during legacy upgrade", e2);
        }
    }

    private void registerPollAlarmLocked() {
        PendingIntent pendingIntent = this.mPollIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
        }
        this.mPollIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_NETWORK_STATS_POLL), 0);
        this.mAlarmManager.setInexactRepeating(3, SystemClock.elapsedRealtime(), this.mSettings.getPollInterval(), this.mPollIntent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerGlobalAlert() {
        try {
            this.mNetworkManager.setGlobalAlert(this.mGlobalAlertBytes);
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem registering for global alert: " + e);
        } catch (RemoteException e2) {
        }
    }

    public INetworkStatsSession openSession() {
        int callingPid = Binder.getCallingPid();
        Slog.i(TAG, "openSession, pid = " + callingPid);
        return openSessionInternal(4, null, callingPid);
    }

    public INetworkStatsSession openSessionForUsageStats(int flags, String callingPackage) {
        int callingPid = Binder.getCallingPid();
        Slog.i(TAG, "openSessionForUsageStats, pid = " + callingPid + ", pkg = " + callingPackage);
        return openSessionInternal(flags, callingPackage, callingPid);
    }

    private boolean isRateLimitedForPoll(int callingUid) {
        long lastCallTime;
        if (callingUid == 1000) {
            return false;
        }
        long now = SystemClock.elapsedRealtime();
        synchronized (this.mOpenSessionCallsPerUid) {
            this.mOpenSessionCallsPerUid.put(callingUid, this.mOpenSessionCallsPerUid.get(callingUid, 0) + 1);
            lastCallTime = this.mLastStatsSessionPoll;
            this.mLastStatsSessionPoll = now;
        }
        if (now - lastCallTime < POLL_RATE_LIMIT_MS) {
            return true;
        }
        return false;
    }

    private INetworkStatsSession openSessionInternal(int flags, final String callingPackage, int callingPid) {
        final int usedFlags;
        assertBandwidthControlEnabled();
        final int callingUid = Binder.getCallingUid();
        if (isRateLimitedForPoll(callingUid)) {
            usedFlags = flags & -2;
        } else {
            usedFlags = flags;
        }
        if ((usedFlags & 3) != 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                Slog.i(TAG, "openSessionInternal, pid = " + callingPid + " pkg = " + callingPackage);
                if (USER_TYPE == 3 && callingPid == MY_PID) {
                    this.mUpdateStatsCount++;
                    if (this.mUpdateStatsCount >= 10) {
                        Slog.w(TAG, new Exception("Update network status too often"));
                        this.mUpdateStatsCount = 0;
                    }
                    performPoll(3);
                } else {
                    this.mUpdateStatsCount = 0;
                    try {
                        performPoll(3);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            } catch (Exception e) {
                Slog.w(TAG, "print log error");
            }
        }
        return new INetworkStatsSession.Stub() {
            /* class com.android.server.net.NetworkStatsService.AnonymousClass3 */
            private final int mAccessLevel;
            private final String mCallingPackage;
            private final int mCallingUid = callingUid;
            private NetworkStatsCollection mUidAndProcComplete;
            private NetworkStatsCollection mUidComplete;
            private NetworkStatsCollection mUidTagComplete;

            {
                String str = callingPackage;
                this.mCallingPackage = str;
                this.mAccessLevel = NetworkStatsService.this.checkAccessLevel(str);
            }

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
                    if (this.mUidAndProcComplete == null && NetworkStatsService.this.mHwInnerService.getUidAndProcNetworkStatsRecorder() != null) {
                        this.mUidAndProcComplete = NetworkStatsService.this.mHwInnerService.getUidAndProcNetworkStatsRecorder().getOrLoadCompleteLocked();
                    }
                    networkStatsCollection = this.mUidAndProcComplete;
                }
                return networkStatsCollection;
            }

            public int[] getRelevantUids() {
                return getUidComplete().getRelevantUids(this.mAccessLevel);
            }

            public NetworkStats getDeviceSummaryForNetwork(NetworkTemplate template, long start, long end) {
                return NetworkStatsService.this.internalGetSummaryForNetwork(template, usedFlags, start, end, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) {
                return NetworkStatsService.this.internalGetSummaryForNetwork(template, usedFlags, start, end, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStatsHistory getHistoryForNetwork(NetworkTemplate template, int fields) {
                return NetworkStatsService.this.internalGetHistoryForNetwork(template, usedFlags, fields, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end, boolean includeTags) {
                NetworkStats stats = new NetworkStats(0, 0);
                try {
                    Slog.i(NetworkStatsService.TAG, "getSummaryForAllUid, uid = " + this.mCallingUid);
                    NetworkStats stats2 = getUidComplete().getSummary(template, start, end, this.mAccessLevel, this.mCallingUid);
                    if (includeTags) {
                        stats2.combineAllValues(getUidTagComplete().getSummary(template, start, end, this.mAccessLevel, this.mCallingUid));
                    }
                    return stats2;
                } catch (NullPointerException e) {
                    if (!NetworkStatsService.this.mSystemReady) {
                        Slog.wtf(NetworkStatsService.TAG, "NullPointerException when the system is shutting down", e);
                        return stats;
                    }
                    Slog.wtf(NetworkStatsService.TAG, "NullPointerException in getSummaryForAllUid", e);
                    throw e;
                }
            }

            public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid, int set, int tag, int fields) {
                Slog.i(NetworkStatsService.TAG, "getHistoryForUid, uid = " + this.mCallingUid);
                if (tag == 0) {
                    return getUidComplete().getHistory(template, null, uid, set, tag, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, this.mAccessLevel, this.mCallingUid);
                }
                return getUidTagComplete().getHistory(template, null, uid, set, tag, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, this.mAccessLevel, this.mCallingUid);
            }

            public NetworkStatsHistory getHistoryIntervalForUid(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end) {
                Slog.i(NetworkStatsService.TAG, "getHistoryIntervalForUid, uid = " + this.mCallingUid);
                if (tag == 0) {
                    return getUidComplete().getHistory(template, null, uid, set, tag, fields, start, end, this.mAccessLevel, this.mCallingUid);
                }
                if (uid == Binder.getCallingUid()) {
                    return getUidTagComplete().getHistory(template, null, uid, set, tag, fields, start, end, this.mAccessLevel, this.mCallingUid);
                }
                throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access tag information from a different uid");
            }

            public NetworkStats getSummaryForAllPid(NetworkTemplate template, long start, long end) {
                int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
                NetworkStatsCollection pidStatsCollection = getUidAndProcComplete();
                if (pidStatsCollection == null) {
                    return new NetworkStats(0, 0);
                }
                NetworkStats stats = pidStatsCollection.getSummary(template, start, end, accessLevel, this.mCallingUid);
                Slog.i(NetworkStatsService.TAG, "getSummaryForAllPid stats size = " + stats.size() + " uid = " + this.mCallingUid);
                return stats;
            }

            public void close() {
                this.mUidComplete = null;
                this.mUidTagComplete = null;
                this.mUidAndProcComplete = null;
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkAccessLevel(String callingPackage) {
        return NetworkStatsAccess.checkAccessLevel(this.mContext, Binder.getCallingUid(), callingPackage);
    }

    /* JADX INFO: finally extract failed */
    private SubscriptionPlan resolveSubscriptionPlan(NetworkTemplate template, int flags) {
        SubscriptionPlan plan = null;
        if ((flags & 4) != 0 && this.mSettings.getAugmentEnabled()) {
            if (LOGD) {
                Slog.d(TAG, "Resolving plan for " + template);
            }
            long token = Binder.clearCallingIdentity();
            try {
                plan = ((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class)).getSubscriptionPlan(template);
                Binder.restoreCallingIdentity(token);
                if (LOGD) {
                    Slog.d(TAG, "Resolved to plan " + plan);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
        return plan;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkStats internalGetSummaryForNetwork(NetworkTemplate template, int flags, long start, long end, int accessLevel, int callingUid) {
        NetworkStatsHistory history = internalGetHistoryForNetwork(template, flags, -1, accessLevel, callingUid);
        Slog.i(TAG, "Summary for network, uid = " + callingUid);
        NetworkStatsHistory.Entry entry = history.getValues(start, end, System.currentTimeMillis(), (NetworkStatsHistory.Entry) null);
        NetworkStats stats = new NetworkStats(end - start, 1);
        stats.addValues(new NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, -1, 0, -1, -1, -1, entry.rxBytes, entry.rxPackets, entry.txBytes, entry.txPackets, entry.operations, entry.rxBytes_mp, entry.rxPackets_mp, entry.txBytes_mp, entry.txPackets_mp));
        return stats;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkStatsHistory internalGetHistoryForNetwork(NetworkTemplate template, int flags, int fields, int accessLevel, int callingUid) {
        Object obj;
        SubscriptionPlan augmentPlan = resolveSubscriptionPlan(template, flags);
        Object obj2 = this.mStatsLock;
        synchronized (obj2) {
            try {
                if (this.mXtStatsCached != null) {
                    Slog.i(TAG, "History for network, uid = " + callingUid);
                    obj = obj2;
                    NetworkStatsHistory history = this.mXtStatsCached.getHistory(template, augmentPlan, -1, -1, 0, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, accessLevel, callingUid);
                    return history;
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getNetworkTotalBytes(NetworkTemplate template, long start, long end) {
        assertSystemReady();
        assertBandwidthControlEnabled();
        return internalGetSummaryForNetwork(template, 4, start, end, 3, Binder.getCallingUid()).getTotalBytes();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkStats getNetworkUidBytes(NetworkTemplate template, long start, long end) {
        NetworkStatsCollection uidComplete;
        assertSystemReady();
        assertBandwidthControlEnabled();
        synchronized (this.mStatsLock) {
            uidComplete = this.mUidRecorder.getOrLoadCompleteLocked();
        }
        return uidComplete.getSummary(template, start, end, 3, 1000);
    }

    /* JADX INFO: finally extract failed */
    public NetworkStats getDataLayerSnapshotForUid(int uid) throws RemoteException {
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        }
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            NetworkStats networkLayer = this.mNetworkManager.getNetworkStatsUidDetail(uid, NetworkStats.INTERFACES_ALL);
            Binder.restoreCallingIdentity(token);
            networkLayer.spliceOperationsFrom(this.mUidOperations);
            NetworkStats dataLayer = new NetworkStats(networkLayer.getElapsedRealtime(), networkLayer.size());
            NetworkStats.Entry entry = null;
            for (int i = 0; i < networkLayer.size(); i++) {
                entry = networkLayer.getValues(i, entry);
                entry.iface = NetworkStats.IFACE_ALL;
                dataLayer.combineValues(entry);
            }
            return dataLayer;
        } catch (Throwable networkLayer2) {
            Binder.restoreCallingIdentity(token);
            throw networkLayer2;
        }
    }

    public NetworkStats getDetailedUidStats(String[] requiredIfaces) {
        try {
            return getNetworkStatsUidDetail(NetworkStatsFactory.augmentWithStackedInterfaces(requiredIfaces));
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error compiling UID stats", e);
            return new NetworkStats(0, 0);
        }
    }

    public String[] getMobileIfaces() {
        return this.mMobileIfaces;
    }

    public void incrementOperationCount(int uid, int tag, int operationCount) {
        Object obj;
        if (Binder.getCallingUid() != uid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", TAG);
        }
        if (operationCount < 0) {
            throw new IllegalArgumentException("operation count can only be incremented");
        } else if (tag != 0) {
            Object obj2 = this.mStatsLock;
            synchronized (obj2) {
                try {
                    int set = this.mActiveUidCounterSet.get(uid, 0);
                    obj = obj2;
                    try {
                        this.mUidOperations.combineValues(this.mActiveIface, uid, set, tag, 0, 0, 0, 0, (long) operationCount);
                        this.mUidOperations.combineValues(this.mActiveIface, uid, set, 0, 0, 0, 0, 0, (long) operationCount);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    obj = obj2;
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("operation count must have specific tag");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setUidForeground(int uid, boolean uidForeground) {
        int status = 0;
        synchronized (this.mStatsLock) {
            int set = uidForeground ? 1 : 0;
            if (this.mActiveUidCounterSet.get(uid, 0) != set) {
                this.mActiveUidCounterSet.put(uid, set);
                status = NetworkManagementSocketTagger.setKernelCounterSet(uid, set);
            }
        }
        this.mHwInnerService.reportNetdStatus(status);
    }

    public void forceUpdateIfaces(Network[] defaultNetworks, VpnInfo[] vpnArray, NetworkState[] networkStates, String activeIface) {
        NetworkStack.checkNetworkStackPermission(this.mContext);
        assertBandwidthControlEnabled();
        Slog.i(TAG, "forceUpdateIfaces, pid = " + Binder.getCallingPid());
        long token = Binder.clearCallingIdentity();
        try {
            updateIfaces(defaultNetworks, vpnArray, networkStates, activeIface);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void forceUpdate() {
        Slog.i(TAG, "forceUpdate, pid = " + Binder.getCallingPid());
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_NETWORK_USAGE_HISTORY", TAG);
        assertBandwidthControlEnabled();
        long token = Binder.clearCallingIdentity();
        try {
            performPoll(3);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void advisePersistThreshold(long thresholdBytes) {
        assertBandwidthControlEnabled();
        this.mPersistThreshold = MathUtils.constrain(thresholdBytes, 131072, 2097152);
        if (LOGV) {
            Slog.v(TAG, "advisePersistThreshold() given " + thresholdBytes + ", clamped to " + this.mPersistThreshold);
        }
        long currentTime = this.mClock.millis();
        synchronized (this.mStatsLock) {
            if (this.mSystemReady) {
                updatePersistThresholdsLocked();
                this.mDevRecorder.maybePersistLocked(currentTime);
                this.mXtRecorder.maybePersistLocked(currentTime);
                this.mUidRecorder.maybePersistLocked(currentTime);
                this.mUidTagRecorder.maybePersistLocked(currentTime);
                this.mHwInnerService.hwMaybeUidAndProcPersistLocked(currentTime);
                registerGlobalAlert();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public DataUsageRequest registerUsageCallback(String callingPackage, DataUsageRequest request, Messenger messenger, IBinder binder) {
        Preconditions.checkNotNull(callingPackage, "calling package is null");
        Preconditions.checkNotNull(request, "DataUsageRequest is null");
        Preconditions.checkNotNull(request.template, "NetworkTemplate is null");
        Preconditions.checkNotNull(messenger, "messenger is null");
        Preconditions.checkNotNull(binder, "binder is null");
        Slog.i(TAG, "registerUsageCallback, pid = " + Binder.getCallingPid());
        int callingUid = Binder.getCallingUid();
        int accessLevel = checkAccessLevel(callingPackage);
        long token = Binder.clearCallingIdentity();
        try {
            DataUsageRequest normalizedRequest = this.mStatsObservers.register(request, messenger, binder, callingUid, accessLevel);
            Binder.restoreCallingIdentity(token);
            if (!this.mHandler.hasMessages(1)) {
                Handler handler = this.mHandler;
                handler.sendMessageDelayed(handler.obtainMessage(1), 1000);
            }
            return normalizedRequest;
        } catch (Throwable normalizedRequest2) {
            Binder.restoreCallingIdentity(token);
            throw normalizedRequest2;
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

    public long getUidStats(int uid, int type) {
        return nativeGetUidStat(uid, type, checkBpfStatsEnable());
    }

    public long getIfaceStats(String iface, int type) {
        long nativeIfaceStats = nativeGetIfaceStat(iface, type, checkBpfStatsEnable());
        if (nativeIfaceStats == -1) {
            return nativeIfaceStats;
        }
        return getTetherStats(iface, type) + nativeIfaceStats;
    }

    public long getTotalStats(int type) {
        long nativeTotalStats = nativeGetTotalStat(type, checkBpfStatsEnable());
        if (nativeTotalStats == -1) {
            return nativeTotalStats;
        }
        return getTetherStats(NetworkStats.IFACE_ALL, type) + nativeTotalStats;
    }

    private long getTetherStats(String iface, int type) {
        HashSet<String> limitIfaces;
        long token = Binder.clearCallingIdentity();
        try {
            NetworkStats tetherSnapshot = getNetworkStatsTethering(0);
            Binder.restoreCallingIdentity(token);
            if (iface == NetworkStats.IFACE_ALL) {
                limitIfaces = null;
            } else {
                limitIfaces = new HashSet<>();
                limitIfaces.add(iface);
            }
            NetworkStats.Entry entry = tetherSnapshot.getTotal((NetworkStats.Entry) null, limitIfaces);
            if (LOGD) {
                Slog.d(TAG, "TetherStats: iface=" + iface + " type=" + type + " entry=" + entry);
            }
            if (type == 0) {
                return entry.rxBytes;
            }
            if (type == 1) {
                return entry.rxPackets;
            }
            if (type == 2) {
                return entry.txBytes;
            }
            if (type != 3) {
                return 0;
            }
            return entry.txPackets;
        } catch (RemoteException e) {
            Slog.w(TAG, "Error get TetherStats: " + e);
            Binder.restoreCallingIdentity(token);
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean checkBpfStatsEnable() {
        return this.mUseBpfTrafficStats;
    }

    public int tagSocket(ParcelFileDescriptor pfd, int tag, int uid) {
        int ret = -1;
        if (this.mContext.checkCallingPermission("android.permission.UPDATE_DEVICE_STATS") == 0) {
            ret = nativeTagSocket(pfd.getFileDescriptor(), tag, uid);
        }
        try {
            pfd.close();
            return ret;
        } catch (IOException e) {
            Slog.w(TAG, "Closed duplicate socket fd failed");
            return -1;
        }
    }

    @GuardedBy({"mStatsLock"})
    private void updatePersistThresholdsLocked() {
        this.mDevRecorder.setPersistThreshold(this.mSettings.getDevPersistBytes(this.mPersistThreshold));
        this.mXtRecorder.setPersistThreshold(this.mSettings.getXtPersistBytes(this.mPersistThreshold));
        this.mUidRecorder.setPersistThreshold(this.mSettings.getUidPersistBytes(this.mPersistThreshold));
        this.mUidTagRecorder.setPersistThreshold(this.mSettings.getUidTagPersistBytes(this.mPersistThreshold));
        this.mHwInnerService.hwUpdateUidAndProcPersistThresholds(this.mPersistThreshold);
        this.mGlobalAlertBytes = this.mSettings.getGlobalAlertBytes(this.mPersistThreshold);
    }

    public static boolean isMpTrafficEnabled() {
        return sMpTrafficEnabled;
    }

    private void updateIfaces(Network[] defaultNetworks, VpnInfo[] vpnArray, NetworkState[] networkStates, String activeIface) {
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                this.mVpnInfos = vpnArray;
                this.mActiveIface = activeIface;
                updateIfacesLocked(defaultNetworks, networkStates);
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    @GuardedBy({"mStatsLock"})
    private void updateIfacesLocked(Network[] defaultNetworks, NetworkState[] states) {
        if (this.mSystemReady) {
            if (LOGV) {
                Slog.v(TAG, "updateIfacesLocked()");
            }
            performPollLocked(1);
            this.mActiveIfaces.clear();
            this.mActiveUidIfaces.clear();
            if (defaultNetworks != null) {
                this.mDefaultNetworks = defaultNetworks;
            }
            ArraySet<String> mobileIfaces = new ArraySet<>();
            for (NetworkState state : states) {
                if (state.networkInfo.isConnected() || state.networkInfo.getState() == NetworkInfo.State.SUSPENDED) {
                    boolean isMobile = ConnectivityManager.isNetworkTypeMobile(state.networkInfo.getType());
                    NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state, ArrayUtils.contains(this.mDefaultNetworks, state.network));
                    String baseIface = state.linkProperties.getInterfaceName();
                    if (baseIface != null) {
                        findOrCreateNetworkIdentitySet(this.mActiveIfaces, baseIface).add(ident);
                        findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, baseIface).add(ident);
                        if (state.networkCapabilities.hasCapability(4) && !ident.getMetered()) {
                            NetworkIdentity vtIdent = new NetworkIdentity(ident.getType(), ident.getSubType(), ident.getSubscriberId(), ident.getNetworkId(), ident.getRoaming(), true, true);
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
                            if (this.mUseBpfTrafficStats) {
                                findOrCreateNetworkIdentitySet(this.mActiveIfaces, stackedIface).add(ident);
                            }
                            findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, stackedIface).add(ident);
                            if (isMobile) {
                                mobileIfaces.add(stackedIface);
                            }
                            NetworkStatsFactory.noteStackedIface(stackedIface, baseIface);
                        }
                    }
                }
            }
            this.mMobileIfaces = (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
        }
    }

    private static <K> NetworkIdentitySet findOrCreateNetworkIdentitySet(ArrayMap<K, NetworkIdentitySet> map, K key) {
        NetworkIdentitySet ident = map.get(key);
        if (ident != null) {
            return ident;
        }
        NetworkIdentitySet ident2 = new NetworkIdentitySet();
        map.put(key, ident2);
        return ident2;
    }

    @GuardedBy({"mStatsLock"})
    private void recordSnapshotLocked(long currentTime) throws RemoteException {
        Trace.traceBegin(2097152, "snapshotUid");
        NetworkStats uidSnapshot = getNetworkStatsUidDetail(NetworkStats.INTERFACES_ALL);
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "snapshotXt");
        NetworkStats xtSnapshot = getNetworkStatsXt();
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "snapshotDev");
        NetworkStats devSnapshot = this.mNetworkManager.getNetworkStatsSummaryDev();
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "snapshotTether");
        NetworkStats tetherSnapshot = getNetworkStatsTethering(0);
        Trace.traceEnd(2097152);
        xtSnapshot.combineAllValues(tetherSnapshot);
        devSnapshot.combineAllValues(tetherSnapshot);
        Trace.traceBegin(2097152, "recordDev");
        this.mDevRecorder.recordSnapshotLocked(devSnapshot, this.mActiveIfaces, null, currentTime);
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "recordXt");
        this.mXtRecorder.recordSnapshotLocked(xtSnapshot, this.mActiveIfaces, null, currentTime);
        Trace.traceEnd(2097152);
        VpnInfo[] vpnArray = this.mVpnInfos;
        Trace.traceBegin(2097152, "recordUid");
        this.mUidRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "recordUidTag");
        this.mUidTagRecorder.recordSnapshotLocked(uidSnapshot, this.mActiveUidIfaces, vpnArray, currentTime);
        this.mHwInnerService.hwRecordUidAndProcSnapshotLocked(currentTime);
        Trace.traceEnd(2097152);
        this.mStatsObservers.updateStats(xtSnapshot, uidSnapshot, new ArrayMap<>(this.mActiveIfaces), new ArrayMap<>(this.mActiveUidIfaces), vpnArray, currentTime);
    }

    @GuardedBy({"mStatsLock"})
    private void bootstrapStatsLocked() {
        try {
            recordSnapshotLocked(this.mClock.millis());
        } catch (IllegalStateException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
        } catch (RemoteException e2) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performPoll(int flags) {
        synchronized (this.mStatsLock) {
            this.mWakeLock.acquire();
            try {
                performPollLocked(flags);
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    @GuardedBy({"mStatsLock"})
    private void performPollLocked(int flags) {
        if (this.mSystemReady) {
            if (LOGV) {
                Slog.v(TAG, "performPollLocked(flags=0x" + Integer.toHexString(flags) + ")");
            }
            Trace.traceBegin(2097152, "performPollLocked");
            Slog.i(TAG, "performPollLocked, pid = " + Binder.getCallingPid());
            boolean persistForce = false;
            boolean persistNetwork = (flags & 1) != 0;
            boolean persistUid = (flags & 2) != 0;
            if ((flags & 256) != 0) {
                persistForce = true;
            }
            long currentTime = this.mClock.millis();
            try {
                recordSnapshotLocked(currentTime);
                Trace.traceBegin(2097152, "[persisting]");
                if (persistForce) {
                    this.mDevRecorder.forcePersistLocked(currentTime);
                    this.mXtRecorder.forcePersistLocked(currentTime);
                    this.mUidRecorder.forcePersistLocked(currentTime);
                    this.mUidTagRecorder.forcePersistLocked(currentTime);
                    this.mHwInnerService.hwForceUidAndProcPersistLocked(currentTime);
                } else {
                    if (persistNetwork) {
                        this.mDevRecorder.maybePersistLocked(currentTime);
                        this.mXtRecorder.maybePersistLocked(currentTime);
                    }
                    if (persistUid) {
                        this.mUidRecorder.maybePersistLocked(currentTime);
                        this.mUidTagRecorder.maybePersistLocked(currentTime);
                        this.mHwInnerService.hwMaybeUidAndProcPersistLocked(currentTime);
                    }
                }
                Trace.traceEnd(2097152);
                if (this.mSettings.getSampleEnabled()) {
                    performSampleLocked();
                }
                Intent updatedIntent = new Intent(ACTION_NETWORK_STATS_UPDATED);
                updatedIntent.setFlags(1073741824);
                this.mContext.sendBroadcastAsUser(updatedIntent, UserHandle.ALL, "android.permission.READ_NETWORK_USAGE_HISTORY");
                Trace.traceEnd(2097152);
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem reading network stats", e);
            } catch (RemoteException e2) {
            }
        }
    }

    @GuardedBy({"mStatsLock"})
    private void performSampleLocked() {
        long currentTime = this.mClock.millis();
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileWildcard();
        NetworkStats.Entry devTotal = this.mDevRecorder.getTotalSinceBootLocked(template);
        NetworkStats.Entry xtTotal = this.mXtRecorder.getTotalSinceBootLocked(template);
        NetworkStats.Entry uidTotal = this.mUidRecorder.getTotalSinceBootLocked(template);
        EventLogTags.writeNetstatsMobileSample(devTotal.rxBytes, devTotal.rxPackets, devTotal.txBytes, devTotal.txPackets, xtTotal.rxBytes, xtTotal.rxPackets, xtTotal.txBytes, xtTotal.txPackets, uidTotal.rxBytes, uidTotal.rxPackets, uidTotal.txBytes, uidTotal.txPackets, currentTime);
        NetworkTemplate template2 = NetworkTemplate.buildTemplateWifiWildcard();
        NetworkStats.Entry devTotal2 = this.mDevRecorder.getTotalSinceBootLocked(template2);
        NetworkStats.Entry xtTotal2 = this.mXtRecorder.getTotalSinceBootLocked(template2);
        NetworkStats.Entry uidTotal2 = this.mUidRecorder.getTotalSinceBootLocked(template2);
        EventLogTags.writeNetstatsWifiSample(devTotal2.rxBytes, devTotal2.rxPackets, devTotal2.txBytes, devTotal2.txPackets, xtTotal2.rxBytes, xtTotal2.rxPackets, xtTotal2.txBytes, xtTotal2.txPackets, uidTotal2.rxBytes, uidTotal2.rxPackets, uidTotal2.txBytes, uidTotal2.txPackets, currentTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mStatsLock"})
    private void removeUidsLocked(int... uids) {
        if (LOGV) {
            Slog.v(TAG, "removeUidsLocked() for UIDs " + Arrays.toString(uids));
        }
        performPollLocked(3);
        NetworkStatsRecorder networkStatsRecorder = this.mUidRecorder;
        if (networkStatsRecorder != null) {
            networkStatsRecorder.removeUidsLocked(uids);
        }
        NetworkStatsRecorder networkStatsRecorder2 = this.mUidTagRecorder;
        if (networkStatsRecorder2 != null) {
            networkStatsRecorder2.removeUidsLocked(uids);
        }
        this.mHwInnerService.removeUids(uids);
        for (int uid : uids) {
            NetworkManagementSocketTagger.resetKernelUidStats(uid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mStatsLock"})
    private void removeUserLocked(int userId) {
        if (LOGV) {
            Slog.v(TAG, "removeUserLocked() for userId=" + userId);
        }
        int[] uids = new int[0];
        for (ApplicationInfo app : this.mContext.getPackageManager().getInstalledApplications(4194816)) {
            uids = ArrayUtils.appendInt(uids, UserHandle.getUid(userId, app.uid));
        }
        removeUidsLocked(uids);
    }

    /* access modifiers changed from: private */
    public class NetworkStatsManagerInternalImpl extends NetworkStatsManagerInternal {
        private NetworkStatsManagerInternalImpl() {
        }

        @Override // com.android.server.net.NetworkStatsManagerInternal
        public long getNetworkTotalBytes(NetworkTemplate template, long start, long end) {
            Trace.traceBegin(2097152, "getNetworkTotalBytes");
            try {
                return NetworkStatsService.this.getNetworkTotalBytes(template, start, end);
            } finally {
                Trace.traceEnd(2097152);
            }
        }

        @Override // com.android.server.net.NetworkStatsManagerInternal
        public NetworkStats getNetworkUidBytes(NetworkTemplate template, long start, long end) {
            Trace.traceBegin(2097152, "getNetworkUidBytes");
            try {
                return NetworkStatsService.this.getNetworkUidBytes(template, start, end);
            } finally {
                Trace.traceEnd(2097152);
            }
        }

        @Override // com.android.server.net.NetworkStatsManagerInternal
        public void setUidForeground(int uid, boolean uidForeground) {
            NetworkStatsService.this.setUidForeground(uid, uidForeground);
        }

        @Override // com.android.server.net.NetworkStatsManagerInternal
        public void advisePersistThreshold(long thresholdBytes) {
            NetworkStatsService.this.advisePersistThreshold(thresholdBytes);
        }

        @Override // com.android.server.net.NetworkStatsManagerInternal
        public void forceUpdate() {
            NetworkStatsService.this.forceUpdate();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0268, code lost:
        r0 = th;
     */
    public void dump(FileDescriptor fd, PrintWriter rawWriter, String[] args) {
        Object obj;
        Throwable th;
        SparseIntArray calls;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, rawWriter)) {
            Slog.i(TAG, "dump, pid = " + Binder.getCallingPid());
            HashSet<String> argSet = new HashSet<>();
            long duration = 86400000;
            for (String arg : args) {
                argSet.add(arg);
                if (arg.startsWith("--duration=")) {
                    try {
                        duration = Long.parseLong(arg.substring(11));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            boolean includeTag = true;
            boolean poll = argSet.contains("--poll") || argSet.contains("poll");
            boolean checkin = argSet.contains("--checkin");
            boolean fullHistory = argSet.contains("--full") || argSet.contains("full");
            boolean includeUid = argSet.contains("--uid") || argSet.contains("detail");
            if (!argSet.contains("--tag") && !argSet.contains("detail")) {
                includeTag = false;
            }
            IndentingPrintWriter pw = new IndentingPrintWriter(rawWriter, "  ");
            Object obj2 = this.mStatsLock;
            synchronized (obj2) {
                try {
                    if (args.length > 0) {
                        try {
                            if (PriorityDump.PROTO_ARG.equals(args[0])) {
                                dumpProtoLocked(fd);
                                return;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            obj = obj2;
                            throw th;
                        }
                    }
                    if (poll) {
                        performPollLocked(LightsManager.LIGHT_ID_MANUALCUSTOMBACKLIGHT);
                        pw.println("Forced poll");
                        return;
                    } else if (checkin) {
                        try {
                            long end = System.currentTimeMillis();
                            long start = end - duration;
                            pw.print("v1,");
                            pw.print(start / 1000);
                            pw.print(',');
                            pw.print(end / 1000);
                            pw.println();
                            pw.println(PREFIX_XT);
                            obj = obj2;
                            try {
                                this.mXtRecorder.dumpCheckin(rawWriter, start, end);
                                if (includeUid) {
                                    pw.println("uid");
                                    this.mUidRecorder.dumpCheckin(rawWriter, start, end);
                                }
                                if (includeTag) {
                                    pw.println("tag");
                                    this.mUidTagRecorder.dumpCheckin(rawWriter, start, end);
                                }
                                return;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            obj = obj2;
                            throw th;
                        }
                    } else {
                        obj = obj2;
                        try {
                            pw.println("Active interfaces:");
                            pw.increaseIndent();
                            for (int i = 0; i < this.mActiveIfaces.size(); i++) {
                                pw.printPair("iface", this.mActiveIfaces.keyAt(i));
                                pw.printPair("ident", this.mActiveIfaces.valueAt(i));
                                pw.println();
                            }
                            pw.decreaseIndent();
                            pw.println("Active UID interfaces:");
                            pw.increaseIndent();
                            for (int i2 = 0; i2 < this.mActiveUidIfaces.size(); i2++) {
                                pw.printPair("iface", this.mActiveUidIfaces.keyAt(i2));
                                pw.printPair("ident", this.mActiveUidIfaces.valueAt(i2));
                                pw.println();
                            }
                            pw.decreaseIndent();
                            synchronized (this.mOpenSessionCallsPerUid) {
                                calls = this.mOpenSessionCallsPerUid.clone();
                            }
                            int N = calls.size();
                            long[] values = new long[N];
                            int j = 0;
                            while (j < N) {
                                try {
                                    values[j] = (((long) calls.valueAt(j)) << 32) | ((long) calls.keyAt(j));
                                    j++;
                                    argSet = argSet;
                                } catch (Throwable th5) {
                                    th = th5;
                                    throw th;
                                }
                            }
                            Arrays.sort(values);
                            pw.println("Top openSession callers (uid=count):");
                            pw.increaseIndent();
                            int end2 = Math.max(0, N - 20);
                            for (int j2 = N - 1; j2 >= end2; j2--) {
                                pw.print((int) (values[j2] & -1));
                                pw.print("=");
                                pw.println((int) (values[j2] >> 32));
                            }
                            pw.decreaseIndent();
                            pw.println();
                            pw.println("Dev stats:");
                            pw.increaseIndent();
                            this.mDevRecorder.dumpLocked(pw, fullHistory);
                            pw.decreaseIndent();
                            pw.println("Xt stats:");
                            pw.increaseIndent();
                            this.mXtRecorder.dumpLocked(pw, fullHistory);
                            pw.decreaseIndent();
                            if (includeUid) {
                                pw.println("UID stats:");
                                pw.increaseIndent();
                                this.mUidRecorder.dumpLocked(pw, fullHistory);
                                pw.decreaseIndent();
                            }
                            if (includeTag) {
                                pw.println("UID tag stats:");
                                pw.increaseIndent();
                                this.mUidTagRecorder.dumpLocked(pw, fullHistory);
                                pw.decreaseIndent();
                            }
                            return;
                        } catch (Throwable th6) {
                            th = th6;
                            throw th;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    obj = obj2;
                    throw th;
                }
            }
        } else {
            return;
        }
        while (true) {
        }
    }

    @GuardedBy({"mStatsLock"})
    private void dumpProtoLocked(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        dumpInterfaces(proto, 2246267895809L, this.mActiveIfaces);
        dumpInterfaces(proto, 2246267895810L, this.mActiveUidIfaces);
        this.mDevRecorder.writeToProtoLocked(proto, 1146756268035L);
        this.mXtRecorder.writeToProtoLocked(proto, 1146756268036L);
        this.mUidRecorder.writeToProtoLocked(proto, 1146756268037L);
        this.mUidTagRecorder.writeToProtoLocked(proto, 1146756268038L);
        proto.flush();
    }

    private static void dumpInterfaces(ProtoOutputStream proto, long tag, ArrayMap<String, NetworkIdentitySet> ifaces) {
        for (int i = 0; i < ifaces.size(); i++) {
            long start = proto.start(tag);
            proto.write(1138166333441L, ifaces.keyAt(i));
            ifaces.valueAt(i).writeToProto(proto, 1146756268034L);
            proto.end(start);
        }
    }

    private NetworkStats getNetworkStatsUidDetail(String[] ifaces) throws RemoteException {
        if (DEBUG) {
            Slog.i("netddebug", "getNetworkStatsUidDetail, pid = " + Binder.getCallingPid());
        }
        NetworkStats uidSnapshot = this.mNetworkManager.getNetworkStatsUidDetail(-1, ifaces);
        NetworkStats tetherSnapshot = getNetworkStatsTethering(1);
        tetherSnapshot.filter(-1, ifaces, -1);
        NetworkStatsFactory.apply464xlatAdjustments(uidSnapshot, tetherSnapshot, this.mUseBpfTrafficStats);
        uidSnapshot.combineAllValues(tetherSnapshot);
        NetworkStats vtStats = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage(1);
        if (vtStats != null) {
            vtStats.filter(-1, ifaces, -1);
            NetworkStatsFactory.apply464xlatAdjustments(uidSnapshot, vtStats, this.mUseBpfTrafficStats);
            uidSnapshot.combineAllValues(vtStats);
        }
        uidSnapshot.combineAllValues(this.mUidOperations);
        return uidSnapshot;
    }

    private NetworkStats getNetworkStatsXt() throws RemoteException {
        NetworkStats xtSnapshot = this.mNetworkManager.getNetworkStatsSummaryXt();
        NetworkStats vtSnapshot = ((TelephonyManager) this.mContext.getSystemService("phone")).getVtDataUsage(0);
        if (vtSnapshot != null) {
            xtSnapshot.combineAllValues(vtSnapshot);
        }
        return xtSnapshot;
    }

    private NetworkStats getNetworkStatsTethering(int how) throws RemoteException {
        try {
            return this.mNetworkManager.getNetworkStatsTethering(how);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem reading network stats", e);
            return new NetworkStats(0, 10);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class HandlerCallback implements Handler.Callback {
        private final NetworkStatsService mService;

        HandlerCallback(NetworkStatsService service) {
            this.mService = service;
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                this.mService.performPoll(3);
                return true;
            } else if (i != 2) {
                return false;
            } else {
                this.mService.performPoll(1);
                this.mService.registerGlobalAlert();
                return true;
            }
        }
    }

    private void assertSystemReady() {
        if (!this.mSystemReady) {
            throw new IllegalStateException("System not ready");
        }
    }

    private void assertBandwidthControlEnabled() {
        if (!isBandwidthControlEnabled()) {
            throw new IllegalStateException("Bandwidth module disabled");
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

    /* access modifiers changed from: private */
    public class DropBoxNonMonotonicObserver implements NetworkStats.NonMonotonicObserver<String> {
        private DropBoxNonMonotonicObserver() {
        }

        public void foundNonMonotonic(NetworkStats left, int leftIndex, NetworkStats right, int rightIndex, String cookie) {
            Log.w(NetworkStatsService.TAG, "Found non-monotonic values; saving to dropbox");
            StringBuilder builder = new StringBuilder();
            builder.append("found non-monotonic " + cookie + " values at left[" + leftIndex + "] - right[" + rightIndex + "]\n");
            builder.append("left=");
            builder.append(left);
            builder.append('\n');
            builder.append("right=");
            builder.append(right);
            builder.append('\n');
            ((DropBoxManager) NetworkStatsService.this.mContext.getSystemService(DropBoxManager.class)).addText(NetworkStatsService.TAG_NETSTATS_ERROR, builder.toString());
        }

        public void foundNonMonotonic(NetworkStats stats, int statsIndex, String cookie) {
            Log.w(NetworkStatsService.TAG, "Found non-monotonic values; saving to dropbox");
            StringBuilder builder = new StringBuilder();
            builder.append("Found non-monotonic " + cookie + " values at [" + statsIndex + "]\n");
            builder.append("stats=");
            builder.append(stats);
            builder.append('\n');
            ((DropBoxManager) NetworkStatsService.this.mContext.getSystemService(DropBoxManager.class)).addText(NetworkStatsService.TAG_NETSTATS_ERROR, builder.toString());
        }

        public void foundNonMonotonic(NetworkStats left, int[] leftIndex, NetworkStats right, int[] rightIndex, String cookie) {
            if (!(leftIndex == null || rightIndex == null || leftIndex.length != rightIndex.length)) {
                Log.w(NetworkStatsService.TAG, "Found non-monotonic values; saving to dropbox");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < leftIndex.length; i++) {
                    builder.append("found non-monotonic ");
                    builder.append(cookie);
                    builder.append(" values at left[");
                    builder.append(leftIndex[i]);
                    builder.append("] - right[");
                    builder.append(rightIndex[i]);
                    builder.append("]");
                    builder.append(System.lineSeparator());
                }
                builder.append("left=");
                builder.append(left);
                builder.append(System.lineSeparator());
                builder.append("right=");
                builder.append(right);
                builder.append(System.lineSeparator());
                ((DropBoxManager) NetworkStatsService.this.mContext.getSystemService(DropBoxManager.class)).addText(NetworkStatsService.TAG_NETSTATS_ERROR, builder.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public static class DefaultNetworkStatsSettings implements NetworkStatsSettings {
        private final ContentResolver mResolver;

        public DefaultNetworkStatsSettings(Context context) {
            this.mResolver = (ContentResolver) Preconditions.checkNotNull(context.getContentResolver());
        }

        private long getGlobalLong(String name, long def) {
            return Settings.Global.getLong(this.mResolver, name, def);
        }

        private boolean getGlobalBoolean(String name, boolean def) {
            return Settings.Global.getInt(this.mResolver, name, def ? 1 : 0) != 0;
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public long getPollInterval() {
            return getGlobalLong("netstats_poll_interval", 1800000);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public long getGlobalAlertBytes(long def) {
            return getGlobalLong("netstats_global_alert_bytes", def);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public boolean getSampleEnabled() {
            return getGlobalBoolean("netstats_sample_enabled", true);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public boolean getAugmentEnabled() {
            return getGlobalBoolean("netstats_augment_enabled", true);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public NetworkStatsSettings.Config getDevConfig() {
            return new NetworkStatsSettings.Config(getGlobalLong("netstats_dev_bucket_duration", AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT), getGlobalLong("netstats_dev_rotate_age", 1296000000), getGlobalLong("netstats_dev_delete_age", 7776000000L));
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public NetworkStatsSettings.Config getXtConfig() {
            return getDevConfig();
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public NetworkStatsSettings.Config getUidConfig() {
            return new NetworkStatsSettings.Config(getGlobalLong("netstats_uid_bucket_duration", AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT), getGlobalLong("netstats_uid_rotate_age", 1296000000), getGlobalLong("netstats_uid_delete_age", 7776000000L));
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public NetworkStatsSettings.Config getUidTagConfig() {
            return new NetworkStatsSettings.Config(getGlobalLong("netstats_uid_tag_bucket_duration", AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT), getGlobalLong("netstats_uid_tag_rotate_age", 432000000), getGlobalLong("netstats_uid_tag_delete_age", 1296000000));
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public long getDevPersistBytes(long def) {
            return getGlobalLong("netstats_dev_persist_bytes", def);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public long getXtPersistBytes(long def) {
            return getDevPersistBytes(def);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public long getUidPersistBytes(long def) {
            return getGlobalLong("netstats_uid_persist_bytes", def);
        }

        @Override // com.android.server.net.NetworkStatsService.NetworkStatsSettings
        public long getUidTagPersistBytes(long def) {
            return getGlobalLong("netstats_uid_tag_persist_bytes", def);
        }
    }

    public long getTimeRefreshElapsedRealtime() {
        if (this.mTimeRefreshRealtime != -1) {
            return SystemClock.elapsedRealtime() - this.mTimeRefreshRealtime;
        }
        return JobStatus.NO_LATEST_RUNTIME;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.net.NetworkStatsService$HwInnerNetworkStatsService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    @Override // com.android.server.net.IHwNetworkStatsInner
    public NetworkStatsSettings getNetworkStatsSettings() {
        return this.mSettings;
    }

    @Override // com.android.server.net.IHwNetworkStatsInner
    public ArrayMap<String, NetworkIdentitySet> getActiveUidIfaces() {
        return this.mActiveUidIfaces;
    }

    @Override // com.android.server.net.IHwNetworkStatsInner
    public NetworkStatsRecorder buildProcRecorder(String tag, boolean isIncludeTags) {
        return buildRecorder(tag, this.mSettings.getUidConfig(), isIncludeTags);
    }

    public class HwInnerNetworkStatsService extends IHwNetworkStats.Stub {
        NetworkStatsService mNetworkStatsService;

        HwInnerNetworkStatsService(NetworkStatsService networkStatsService) {
            this.mNetworkStatsService = networkStatsService;
        }

        public boolean setAlertPeriodType(int period) {
            Slog.i(NetworkStatsService.TAG, "Current alert type is called with period = " + period);
            return NetworkStatsService.this.mHwNetworkStatsServiceEx.setAlertPeriodType(period);
        }

        public boolean setMpTrafficEnabled(boolean enable) {
            int callingUid = Binder.getCallingUid();
            if (callingUid != 1000) {
                Slog.e(NetworkStatsService.TAG, "setMpTrafficEnabled fail, callingUid=" + callingUid);
                return false;
            }
            Slog.i(NetworkStatsService.TAG, "setMpTrafficEnabled to " + enable);
            boolean unused = NetworkStatsService.sMpTrafficEnabled = enable;
            return true;
        }

        public int getPerformPollDelay() {
            return NetworkStatsService.this.mHwNetworkStatsServiceEx.getPerformPollDelay();
        }

        public void reportNetdStatus(int status) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.reportNetdStatus(status);
        }

        public void hwInitUidAndProcRecorder() {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.hwInitUidAndProcRecorder();
        }

        public void hwUpdateUidAndProcPersistThresholds(long persistThreshold) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.hwUpdateUidAndProcPersistThresholds(persistThreshold);
        }

        public void hwRecordUidAndProcSnapshotLocked(long currentTime) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.hwRecordUidAndProcSnapshotLocked(currentTime);
        }

        public void hwMaybeUidAndProcPersistLocked(long currentTime) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.hwMaybeUidAndProcPersistLocked(currentTime);
        }

        public void hwForceUidAndProcPersistLocked(long currentTime) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.hwForceUidAndProcPersistLocked(currentTime);
        }

        public void hwShutdownUidAndProcLocked(long currentTime) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.hwShutdownUidAndProcLocked(currentTime);
        }

        public NetworkStatsRecorder getUidAndProcNetworkStatsRecorder() {
            return NetworkStatsService.this.mHwNetworkStatsServiceEx.getUidAndProcNetworkStatsRecorder();
        }

        public void removeUids(int[] uids) {
            NetworkStatsService.this.mHwNetworkStatsServiceEx.removeUids(uids);
        }
    }
}
