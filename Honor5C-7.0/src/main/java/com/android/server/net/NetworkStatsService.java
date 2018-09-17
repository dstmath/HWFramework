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
import com.android.internal.net.VpnInfo;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.ArrayUtils;
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
    private static IHwNetworkStatsService mHwNetworkStatsService;
    protected String mActiveIface;
    private final ArrayMap<String, NetworkIdentitySet> mActiveIfaces;
    private SparseIntArray mActiveUidCounterSet;
    protected final ArrayMap<String, NetworkIdentitySet> mActiveUidIfaces;
    private final AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver;
    private final File mBaseDir;
    private IConnectivityManager mConnManager;
    protected final Context mContext;
    private NetworkStatsRecorder mDevRecorder;
    private long mGlobalAlertBytes;
    private Handler mHandler;
    private Callback mHandlerCallback;
    private String[] mMobileIfaces;
    private final INetworkManagementService mNetworkManager;
    private final DropBoxNonMonotonicObserver mNonMonotonicObserver;
    private long mPersistThreshold;
    private PendingIntent mPollIntent;
    private BroadcastReceiver mPollReceiver;
    private BroadcastReceiver mRemovedReceiver;
    protected final NetworkStatsSettings mSettings;
    private BroadcastReceiver mShutdownReceiver;
    private final Object mStatsLock;
    private final NetworkStatsObservers mStatsObservers;
    protected final File mSystemDir;
    private boolean mSystemReady;
    private final TelephonyManager mTeleManager;
    private BroadcastReceiver mTetherReceiver;
    private final TrustedTime mTime;
    private long mTimeRefreshRealtime;
    private NetworkStats mUidOperations;
    private NetworkStatsRecorder mUidRecorder;
    private NetworkStatsRecorder mUidTagRecorder;
    private BroadcastReceiver mUserReceiver;
    private final WakeLock mWakeLock;
    private NetworkStatsRecorder mXtRecorder;
    private NetworkStatsCollection mXtStatsCached;

    /* renamed from: com.android.server.net.NetworkStatsService.7 */
    class AnonymousClass7 extends Stub {
        private String mCallingPackage;
        private NetworkStatsCollection mUidComplete;
        private NetworkStatsCollection mUidTagComplete;
        final /* synthetic */ String val$callingPackage;

        AnonymousClass7(String val$callingPackage) {
            this.val$callingPackage = val$callingPackage;
            this.mCallingPackage = this.val$callingPackage;
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

        public int[] getRelevantUids() {
            return getUidComplete().getRelevantUids(NetworkStatsService.this.checkAccessLevel(this.mCallingPackage));
        }

        public NetworkStats getDeviceSummaryForNetwork(NetworkTemplate template, long start, long end) {
            if (NetworkStatsService.this.checkAccessLevel(this.mCallingPackage) < NetworkStatsService.MSG_UPDATE_IFACES) {
                throw new SecurityException("Calling package " + this.mCallingPackage + " cannot access device summary network stats");
            }
            NetworkStats result = new NetworkStats(end - start, NetworkStatsService.MSG_PERFORM_POLL);
            long ident = Binder.clearCallingIdentity();
            try {
                result.combineAllValues(NetworkStatsService.this.internalGetSummaryForNetwork(template, start, end, NetworkStatsService.MSG_REGISTER_GLOBAL_ALERT));
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
            int accessLevel = NetworkStatsService.this.checkAccessLevel(this.mCallingPackage);
            NetworkStats stats = getUidComplete().getSummary(template, start, end, accessLevel);
            if (includeTags) {
                stats.combineAllValues(getUidTagComplete().getSummary(template, start, end, accessLevel));
            }
            return stats;
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

        public void close() {
            this.mUidComplete = null;
            this.mUidTagComplete = null;
        }
    }

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
            if (Global.getInt(this.mResolver, name, def ? NetworkStatsService.MSG_PERFORM_POLL : 0) != 0) {
                return true;
            }
            return NetworkStatsService.LOGV;
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
        private DropBoxNonMonotonicObserver() {
        }

        public void foundNonMonotonic(NetworkStats left, int leftIndex, NetworkStats right, int rightIndex, String cookie) {
            Log.w(NetworkStatsService.TAG, "found non-monotonic values; saving to dropbox");
            StringBuilder builder = new StringBuilder();
            builder.append("found non-monotonic ").append(cookie).append(" values at left[").append(leftIndex).append("] - right[").append(rightIndex).append("]\n");
            builder.append("left=").append(left).append('\n');
            builder.append("right=").append(right).append('\n');
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
                case NetworkStatsService.MSG_PERFORM_POLL /*1*/:
                    this.mService.performPoll(msg.arg1);
                    return true;
                case NetworkStatsService.MSG_UPDATE_IFACES /*2*/:
                    this.mService.updateIfaces();
                    return true;
                case NetworkStatsService.MSG_REGISTER_GLOBAL_ALERT /*3*/:
                    this.mService.registerGlobalAlert();
                    return true;
                default:
                    return NetworkStatsService.LOGV;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.net.NetworkStatsService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.net.NetworkStatsService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkStatsService.<clinit>():void");
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
        WakeLock wakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(MSG_PERFORM_POLL, TAG);
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
        this.mStatsLock = new Object();
        this.mActiveIfaces = new ArrayMap();
        this.mActiveUidIfaces = new ArrayMap();
        this.mMobileIfaces = new String[0];
        this.mNonMonotonicObserver = new DropBoxNonMonotonicObserver();
        this.mActiveUidCounterSet = new SparseIntArray();
        this.mUidOperations = new NetworkStats(0, 10);
        this.mPersistThreshold = 2097152;
        this.mTetherReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkStatsService.this.performPoll(NetworkStatsService.MSG_PERFORM_POLL);
            }
        };
        this.mPollReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkStatsService.this.performPoll(NetworkStatsService.MSG_REGISTER_GLOBAL_ALERT);
                NetworkStatsService.this.registerGlobalAlert();
            }
        };
        this.mRemovedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1) {
                    synchronized (NetworkStatsService.this.mStatsLock) {
                        NetworkStatsService.this.mWakeLock.acquire();
                        try {
                            NetworkStatsService networkStatsService = NetworkStatsService.this;
                            int[] iArr = new int[NetworkStatsService.MSG_PERFORM_POLL];
                            iArr[0] = uid;
                            networkStatsService.removeUidsLocked(iArr);
                            NetworkStatsService.this.mWakeLock.release();
                        } catch (Throwable th) {
                            NetworkStatsService.this.mWakeLock.release();
                        }
                    }
                }
            }
        };
        this.mUserReceiver = new BroadcastReceiver() {
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
        this.mShutdownReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkStatsService.this.mStatsLock) {
                    NetworkStatsService.this.shutdownLocked();
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            public void limitReached(String limitName, String iface) {
                NetworkStatsService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkStatsService.TAG);
                if (NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                    NetworkStatsService.this.mHandler.obtainMessage(NetworkStatsService.MSG_PERFORM_POLL, NetworkStatsService.MSG_PERFORM_POLL, 0).sendToTarget();
                    NetworkStatsService.this.mHandler.obtainMessage(NetworkStatsService.MSG_REGISTER_GLOBAL_ALERT).sendToTarget();
                }
            }
        };
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
            this.mDevRecorder = buildRecorder(PREFIX_DEV, this.mSettings.getDevConfig(), LOGV);
            this.mXtRecorder = buildRecorder(PREFIX_XT, this.mSettings.getXtConfig(), LOGV);
            this.mUidRecorder = buildRecorder(PREFIX_UID, this.mSettings.getUidConfig(), LOGV);
            this.mUidTagRecorder = buildRecorder(PREFIX_UID_TAG, this.mSettings.getUidTagConfig(), true);
            hwInitProcRecorder();
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
        this.mSystemReady = LOGV;
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
        this.mAlarmManager.setInexactRepeating(MSG_REGISTER_GLOBAL_ALERT, SystemClock.elapsedRealtime(), this.mSettings.getPollInterval(), this.mPollIntent);
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
        return createSession(null, LOGV);
    }

    public INetworkStatsSession openSessionForUsageStats(String callingPackage) {
        return createSession(callingPackage, true);
    }

    private INetworkStatsSession createSession(String callingPackage, boolean pollOnCreate) {
        assertBandwidthControlEnabled();
        if (pollOnCreate) {
            long ident = Binder.clearCallingIdentity();
            try {
                performPoll(MSG_REGISTER_GLOBAL_ALERT);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return new AnonymousClass7(callingPackage);
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
        return internalGetSummaryForNetwork(template, start, end, MSG_REGISTER_GLOBAL_ALERT).getTotalBytes();
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
            for (int i = 0; i < networkLayer.size(); i += MSG_PERFORM_POLL) {
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
                int i = uid;
                int i2 = tag;
                this.mUidOperations.combineValues(this.mActiveIface, i, set, i2, 0, 0, 0, 0, (long) operationCount);
                i = uid;
                this.mUidOperations.combineValues(this.mActiveIface, i, set, 0, 0, 0, 0, 0, (long) operationCount);
            }
        }
    }

    public void setUidForeground(int uid, boolean uidForeground) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_NETWORK_ACCOUNTING", TAG);
        synchronized (this.mStatsLock) {
            int set = uidForeground ? MSG_PERFORM_POLL : 0;
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
            performPoll(MSG_REGISTER_GLOBAL_ALERT);
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_PERFORM_POLL, Integer.valueOf(MSG_REGISTER_GLOBAL_ALERT)));
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
        String str = null;
        if (this.mSystemReady) {
            performPollLocked(MSG_PERFORM_POLL);
            try {
                NetworkState[] states = this.mConnManager.getAllNetworkState();
                LinkProperties activeLink = this.mConnManager.getActiveLinkProperties();
                if (activeLink != null) {
                    str = activeLink.getInterfaceName();
                }
                this.mActiveIface = str;
                this.mActiveIfaces.clear();
                this.mActiveUidIfaces.clear();
                ArraySet<String> mobileIfaces = new ArraySet();
                int length = states.length;
                for (int i = 0; i < length; i += MSG_PERFORM_POLL) {
                    NetworkState state = states[i];
                    if (state.networkInfo.isConnected()) {
                        boolean isMobile = ConnectivityManager.isNetworkTypeMobile(state.networkInfo.getType());
                        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                        String baseIface = state.linkProperties.getInterfaceName();
                        if (baseIface != null) {
                            findOrCreateNetworkIdentitySet(this.mActiveIfaces, baseIface).add(ident);
                            findOrCreateNetworkIdentitySet(this.mActiveUidIfaces, baseIface).add(ident);
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
                }
                this.mMobileIfaces = (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
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
        NetworkStats xtSnapshot = this.mNetworkManager.getNetworkStatsSummaryXt();
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
            boolean persistNetwork = (flags & MSG_PERFORM_POLL) != 0 ? true : LOGV;
            boolean persistUid = (flags & MSG_UPDATE_IFACES) != 0 ? true : LOGV;
            boolean persistForce = (flags & FLAG_PERSIST_FORCE) != 0 ? true : LOGV;
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
                } else {
                    if (persistNetwork) {
                        this.mDevRecorder.maybePersistLocked(currentTime);
                        this.mXtRecorder.maybePersistLocked(currentTime);
                    }
                    if (persistUid) {
                        this.mUidRecorder.maybePersistLocked(currentTime);
                        this.mUidTagRecorder.maybePersistLocked(currentTime);
                        hwMaybePersistLocked(currentTime);
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
        performPollLocked(MSG_REGISTER_GLOBAL_ALERT);
        if (this.mUidRecorder != null) {
            this.mUidRecorder.removeUidsLocked(uids);
        }
        if (this.mUidTagRecorder != null) {
            this.mUidTagRecorder.removeUidsLocked(uids);
        }
        int length = uids.length;
        for (int i = 0; i < length; i += MSG_PERFORM_POLL) {
            NetworkManagementSocketTagger.resetKernelUidStats(uids[i]);
        }
    }

    private void removeUserLocked(int userId) {
        int[] uids = new int[0];
        for (ApplicationInfo app : this.mContext.getPackageManager().getInstalledApplications(8704)) {
            uids = ArrayUtils.appendInt(uids, UserHandle.getUid(userId, app.uid));
        }
        removeUidsLocked(uids);
    }

    protected void dump(FileDescriptor fd, PrintWriter rawWriter, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        long duration = UnixCalendar.DAY_IN_MILLIS;
        HashSet<String> argSet = new HashSet();
        int length = args.length;
        for (int i = 0; i < length; i += MSG_PERFORM_POLL) {
            String arg = args[i];
            argSet.add(arg);
            if (arg.startsWith("--duration=")) {
                try {
                    duration = Long.parseLong(arg.substring(11));
                } catch (NumberFormatException e) {
                }
            }
        }
        boolean contains = !argSet.contains("--poll") ? argSet.contains("poll") : true;
        boolean checkin = argSet.contains("--checkin");
        boolean contains2 = !argSet.contains("--full") ? argSet.contains("full") : true;
        boolean contains3 = !argSet.contains("--uid") ? argSet.contains("detail") : true;
        boolean contains4 = !argSet.contains("--tag") ? argSet.contains("detail") : true;
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(rawWriter, "  ");
        synchronized (this.mStatsLock) {
            if (contains) {
                performPollLocked(259);
                indentingPrintWriter.println("Forced poll");
                return;
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
                if (contains3) {
                    indentingPrintWriter.println(PREFIX_UID);
                    this.mUidRecorder.dumpCheckin(rawWriter, start, end);
                }
                if (contains4) {
                    indentingPrintWriter.println("tag");
                    this.mUidTagRecorder.dumpCheckin(rawWriter, start, end);
                }
                return;
            } else {
                int i2;
                indentingPrintWriter.println("Active interfaces:");
                indentingPrintWriter.increaseIndent();
                for (i2 = 0; i2 < this.mActiveIfaces.size(); i2 += MSG_PERFORM_POLL) {
                    indentingPrintWriter.printPair("iface", this.mActiveIfaces.keyAt(i2));
                    indentingPrintWriter.printPair("ident", this.mActiveIfaces.valueAt(i2));
                    indentingPrintWriter.println();
                }
                indentingPrintWriter.decreaseIndent();
                indentingPrintWriter.println("Active UID interfaces:");
                indentingPrintWriter.increaseIndent();
                for (i2 = 0; i2 < this.mActiveUidIfaces.size(); i2 += MSG_PERFORM_POLL) {
                    indentingPrintWriter.printPair("iface", this.mActiveUidIfaces.keyAt(i2));
                    indentingPrintWriter.printPair("ident", this.mActiveUidIfaces.valueAt(i2));
                    indentingPrintWriter.println();
                }
                indentingPrintWriter.decreaseIndent();
                indentingPrintWriter.println("Dev stats:");
                indentingPrintWriter.increaseIndent();
                this.mDevRecorder.dumpLocked(indentingPrintWriter, contains2);
                indentingPrintWriter.decreaseIndent();
                indentingPrintWriter.println("Xt stats:");
                indentingPrintWriter.increaseIndent();
                this.mXtRecorder.dumpLocked(indentingPrintWriter, contains2);
                indentingPrintWriter.decreaseIndent();
                if (contains3) {
                    indentingPrintWriter.println("UID stats:");
                    indentingPrintWriter.increaseIndent();
                    this.mUidRecorder.dumpLocked(indentingPrintWriter, contains2);
                    indentingPrintWriter.decreaseIndent();
                }
                if (contains4) {
                    indentingPrintWriter.println("UID tag stats:");
                    indentingPrintWriter.increaseIndent();
                    this.mUidTagRecorder.dumpLocked(indentingPrintWriter, contains2);
                    indentingPrintWriter.decreaseIndent();
                }
                return;
            }
        }
    }

    private NetworkStats getNetworkStatsUidDetail() throws RemoteException {
        NetworkStats uidSnapshot = this.mNetworkManager.getNetworkStatsUidDetail(-1);
        uidSnapshot.combineAllValues(getNetworkStatsTethering());
        uidSnapshot.combineAllValues(this.mUidOperations);
        return uidSnapshot;
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
            isBandwidthControlEnabled = LOGV;
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
