package com.android.server.stats;

import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.ProcessMemoryState;
import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.UidTraffic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.UserInfo;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.icu.util.TimeZone;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkStats;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.BatteryStatsInternal;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CoolingDevice;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IStatsCompanionService;
import android.os.IStatsManager;
import android.os.IStoraged;
import android.os.IThermalEventListener;
import android.os.IThermalService;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.StatsDimensionsValue;
import android.os.StatsLogEventWrapper;
import android.os.SynchronousResultReceiver;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Temperature;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.telephony.ModemActivityInfo;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.StatsLog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.procstats.IProcessStats;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BinderCallsStats;
import com.android.internal.os.KernelCpuSpeedReader;
import com.android.internal.os.KernelCpuThreadReader;
import com.android.internal.os.KernelCpuThreadReaderDiff;
import com.android.internal.os.KernelCpuThreadReaderSettingsObserver;
import com.android.internal.os.KernelCpuUidTimeReader;
import com.android.internal.os.KernelWakelockReader;
import com.android.internal.os.KernelWakelockStats;
import com.android.internal.os.LooperStats;
import com.android.internal.os.PowerProfile;
import com.android.internal.os.ProcessCpuTracker;
import com.android.internal.os.StoragedUidIoStatsReader;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.BinderCallsStatsService;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.am.MemoryStatUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.role.RoleManagerInternal;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.storage.DiskStatsFileLogger;
import com.android.server.storage.DiskStatsLoggingService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StatsCompanionService extends IStatsCompanionService.Stub {
    private static final int APPLICATION_INFO_FIELD_ID = 1;
    public static final int CODE_ACTIVE_CONFIGS_BROADCAST = 1;
    public static final int CODE_DATA_BROADCAST = 1;
    public static final int CODE_SUBSCRIBER_BROADCAST = 1;
    public static final String CONFIG_DIR = "/data/misc/stats-service";
    private static final int CPU_TIME_PER_THREAD_FREQ_MAX_NUM_FREQUENCIES = 8;
    public static final int DEATH_THRESHOLD = 10;
    static final boolean DEBUG = false;
    private static final long EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS = 2000;
    public static final String EXTRA_LAST_REPORT_TIME = "android.app.extra.LAST_REPORT_TIME";
    private static final int INSTALLER_FIELD_ID = 5;
    private static final int MAX_BATTERY_STATS_HELPER_FREQUENCY_MS = 1000;
    private static final String[] MEMORY_INTERESTING_NATIVE_PROCESSES = {"/system/bin/statsd", "/system/bin/surfaceflinger", "/system/bin/apexd", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/healthd", "/system/bin/incidentd", "/system/bin/installd", "/system/bin/lmkd", "/system/bin/logd", "media.codec", "media.extractor", "media.metrics", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/performanced", "/system/bin/tombstoned", "/system/bin/traced", "/system/bin/traced_probes", "webview_zygote", "zygote", "zygote64"};
    private static final long MILLIS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);
    private static final int PACKAGE_NAME_FIELD_ID = 4;
    public static final String RESULT_RECEIVER_CONTROLLER_KEY = "controller_activity";
    static final String TAG = "StatsCompanionService";
    private static final int UID_FIELD_ID = 1;
    private static final int VERSION_FIELD_ID = 2;
    private static final int VERSION_STRING_FIELD_ID = 3;
    @GuardedBy({"sStatsdLock"})
    private static IStatsManager sStatsd;
    private static final Object sStatsdLock = new Object();
    private static IThermalService sThermalService;
    private final AlarmManager mAlarmManager;
    private final AlarmManager.OnAlarmListener mAnomalyAlarmListener = new AnomalyAlarmListener();
    private final BroadcastReceiver mAppUpdateReceiver;
    private File mBaseDir = new File(SystemServiceManager.ensureSystemDir(), "stats_companion");
    private BatteryStatsHelper mBatteryStatsHelper = null;
    private long mBatteryStatsHelperTimestampMs = -1000;
    private final Context mContext;
    private KernelCpuUidTimeReader.KernelCpuUidActiveTimeReader mCpuUidActiveTimeReader = new KernelCpuUidTimeReader.KernelCpuUidActiveTimeReader(false);
    private KernelCpuUidTimeReader.KernelCpuUidClusterTimeReader mCpuUidClusterTimeReader = new KernelCpuUidTimeReader.KernelCpuUidClusterTimeReader(false);
    private KernelCpuUidTimeReader.KernelCpuUidFreqTimeReader mCpuUidFreqTimeReader = new KernelCpuUidTimeReader.KernelCpuUidFreqTimeReader(false);
    private KernelCpuUidTimeReader.KernelCpuUidUserSysTimeReader mCpuUidUserSysTimeReader = new KernelCpuUidTimeReader.KernelCpuUidUserSysTimeReader(false);
    @GuardedBy({"sStatsdLock"})
    private final HashSet<Long> mDeathTimeMillis = new HashSet<>();
    private long mDebugElapsedClockPreviousValue = 0;
    private long mDebugElapsedClockPullCount = 0;
    private long mDebugFailingElapsedClockPreviousValue = 0;
    private long mDebugFailingElapsedClockPullCount = 0;
    @GuardedBy({"sStatsdLock"})
    private final HashMap<Long, String> mDeletedFiles = new HashMap<>();
    private final CompanionHandler mHandler;
    private KernelCpuSpeedReader[] mKernelCpuSpeedReaders;
    private final KernelCpuThreadReaderDiff mKernelCpuThreadReader;
    private final KernelWakelockReader mKernelWakelockReader = new KernelWakelockReader();
    private final INetworkStatsService mNetworkStatsService;
    private final AlarmManager.OnAlarmListener mPeriodicAlarmListener = new PeriodicAlarmListener();
    @GuardedBy({"this"})
    ProcessCpuTracker mProcessCpuTracker = null;
    private IProcessStats mProcessStats = IProcessStats.Stub.asInterface(ServiceManager.getService("procstats"));
    private final AlarmManager.OnAlarmListener mPullingAlarmListener = new PullingAlarmListener();
    private final ShutdownEventReceiver mShutdownEventReceiver;
    private StoragedUidIoStatsReader mStoragedUidIoStatsReader = new StoragedUidIoStatsReader();
    private TelephonyManager mTelephony = null;
    private final KernelWakelockStats mTmpWakelockStats = new KernelWakelockStats();
    private final BroadcastReceiver mUserUpdateReceiver;
    private IWifiManager mWifiManager = null;

    static final class CompanionHandler extends Handler {
        CompanionHandler(Looper looper) {
            super(looper);
        }
    }

    public StatsCompanionService(Context context) {
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mNetworkStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        this.mBaseDir.mkdirs();
        this.mAppUpdateReceiver = new AppUpdateReceiver();
        this.mUserUpdateReceiver = new BroadcastReceiver() {
            /* class com.android.server.stats.StatsCompanionService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                synchronized (StatsCompanionService.sStatsdLock) {
                    IStatsManager unused = StatsCompanionService.sStatsd = StatsCompanionService.fetchStatsdService();
                    if (StatsCompanionService.sStatsd == null) {
                        Slog.w(StatsCompanionService.TAG, "Could not access statsd for UserUpdateReceiver");
                        return;
                    }
                    try {
                        StatsCompanionService.this.informAllUidsLocked(context);
                    } catch (RemoteException e) {
                        Slog.e(StatsCompanionService.TAG, "Failed to inform statsd latest update of all apps", e);
                        StatsCompanionService.this.forgetEverythingLocked();
                    }
                }
            }
        };
        this.mShutdownEventReceiver = new ShutdownEventReceiver();
        PowerProfile powerProfile = new PowerProfile(context);
        int numClusters = powerProfile.getNumCpuClusters();
        this.mKernelCpuSpeedReaders = new KernelCpuSpeedReader[numClusters];
        int firstCpuOfCluster = 0;
        for (int i = 0; i < numClusters; i++) {
            this.mKernelCpuSpeedReaders[i] = new KernelCpuSpeedReader(firstCpuOfCluster, powerProfile.getNumSpeedStepsInCpuCluster(i));
            firstCpuOfCluster += powerProfile.getNumCoresInCpuCluster(i);
        }
        IBinder b = ServiceManager.getService("thermalservice");
        if (b != null) {
            sThermalService = IThermalService.Stub.asInterface(b);
            try {
                sThermalService.registerThermalEventListener(new ThermalEventListener());
                Slog.i(TAG, "register thermal listener successfully");
            } catch (RemoteException e) {
                Slog.e(TAG, "register thermal listener error");
            }
        } else {
            Slog.e(TAG, "cannot find thermalservice, no throttling push notifications");
        }
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityStatsCallback());
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new CompanionHandler(handlerThread.getLooper());
        this.mKernelCpuThreadReader = KernelCpuThreadReaderSettingsObserver.getSettingsModifiedReader(this.mContext);
    }

    public void sendDataBroadcast(IBinder intentSenderBinder, long lastReportTimeNs) {
        enforceCallingPermission();
        IntentSender intentSender = new IntentSender(intentSenderBinder);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LAST_REPORT_TIME, lastReportTimeNs);
        try {
            intentSender.sendIntent(this.mContext, 1, intent, null, null);
        } catch (IntentSender.SendIntentException e) {
            Slog.w(TAG, "Unable to send using IntentSender");
        }
    }

    public void sendActiveConfigsChangedBroadcast(IBinder intentSenderBinder, long[] configIds) {
        enforceCallingPermission();
        IntentSender intentSender = new IntentSender(intentSenderBinder);
        Intent intent = new Intent();
        intent.putExtra("android.app.extra.STATS_ACTIVE_CONFIG_KEYS", configIds);
        try {
            intentSender.sendIntent(this.mContext, 1, intent, null, null);
        } catch (IntentSender.SendIntentException e) {
            Slog.w(TAG, "Unable to send active configs changed broadcast using IntentSender");
        }
    }

    public void sendSubscriberBroadcast(IBinder intentSenderBinder, long configUid, long configKey, long subscriptionId, long subscriptionRuleId, String[] cookies, StatsDimensionsValue dimensionsValue) {
        enforceCallingPermission();
        IntentSender intentSender = new IntentSender(intentSenderBinder);
        Intent intent = new Intent().putExtra("android.app.extra.STATS_CONFIG_UID", configUid).putExtra("android.app.extra.STATS_CONFIG_KEY", configKey).putExtra("android.app.extra.STATS_SUBSCRIPTION_ID", subscriptionId).putExtra("android.app.extra.STATS_SUBSCRIPTION_RULE_ID", subscriptionRuleId).putExtra("android.app.extra.STATS_DIMENSIONS_VALUE", (Parcelable) dimensionsValue);
        ArrayList<String> cookieList = new ArrayList<>(cookies.length);
        int i = 0;
        for (int length = cookies.length; i < length; length = length) {
            cookieList.add(cookies[i]);
            i++;
        }
        intent.putStringArrayListExtra("android.app.extra.STATS_BROADCAST_SUBSCRIBER_COOKIES", cookieList);
        try {
            try {
                intentSender.sendIntent(this.mContext, 1, intent, null, null);
            } catch (IntentSender.SendIntentException e) {
            }
        } catch (IntentSender.SendIntentException e2) {
            Slog.w(TAG, "Unable to send using IntentSender from uid " + configUid + "; presumably it had been cancelled.");
        }
    }

    private static final int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i).intValue();
        }
        return ret;
    }

    private static final long[] toLongArray(List<Long> list) {
        long[] ret = new long[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i).longValue();
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"sStatsdLock"})
    private final void informAllUidsLocked(Context context) throws RemoteException {
        PackageManager pm = context.getPackageManager();
        List<UserInfo> users = ((UserManager) context.getSystemService("user")).getUsers(true);
        try {
            ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
            sStatsd.informAllUidData(fds[0]);
            try {
                fds[0].close();
            } catch (IOException e) {
                Slog.e(TAG, "Failed to close the read side of the pipe.", e);
            }
            BackgroundThread.getHandler().post(new Runnable(fds[1], users, pm) {
                /* class com.android.server.stats.$$Lambda$StatsCompanionService$lgt3DadUXkgOLnAdC3Gl42vPKY */
                private final /* synthetic */ ParcelFileDescriptor f$0;
                private final /* synthetic */ List f$1;
                private final /* synthetic */ PackageManager f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StatsCompanionService.lambda$informAllUidsLocked$0(this.f$0, this.f$1, this.f$2);
                }
            });
        } catch (IOException e2) {
            Slog.e(TAG, "Failed to create a pipe to send uid map data.", e2);
        }
    }

    static /* synthetic */ void lambda$informAllUidsLocked$0(ParcelFileDescriptor writeFd, List users, PackageManager pm) {
        String str;
        String installer;
        PackageManager packageManager = pm;
        String str2 = "";
        FileOutputStream fout = new ParcelFileDescriptor.AutoCloseOutputStream(writeFd);
        try {
            ProtoOutputStream output = new ProtoOutputStream(fout);
            int numRecords = 0;
            Iterator it = users.iterator();
            while (it.hasNext()) {
                List<PackageInfo> pi = packageManager.getInstalledPackagesAsUser(4202496, ((UserInfo) it.next()).id);
                int j = 0;
                int numRecords2 = numRecords;
                while (j < pi.size()) {
                    if (pi.get(j).applicationInfo != null) {
                        try {
                            installer = packageManager.getInstallerPackageName(pi.get(j).packageName);
                        } catch (IllegalArgumentException e) {
                            installer = str2;
                        }
                        long applicationInfoToken = output.start(2246267895809L);
                        output.write(1120986464257L, pi.get(j).applicationInfo.uid);
                        str = str2;
                        output.write(1112396529666L, pi.get(j).getLongVersionCode());
                        output.write(1138166333443L, pi.get(j).versionName);
                        output.write(1138166333444L, pi.get(j).packageName);
                        output.write(1138166333445L, installer == null ? str : installer);
                        numRecords2++;
                        output.end(applicationInfoToken);
                    } else {
                        str = str2;
                    }
                    j++;
                    packageManager = pm;
                    str2 = str;
                }
                packageManager = pm;
                numRecords = numRecords2;
            }
            output.flush();
        } finally {
            IoUtils.closeQuietly(fout);
        }
    }

    private static final class AppUpdateReceiver extends BroadcastReceiver {
        private AppUpdateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String installer;
            if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED") || !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                synchronized (StatsCompanionService.sStatsdLock) {
                    if (StatsCompanionService.sStatsd == null) {
                        Slog.w(StatsCompanionService.TAG, "Could not access statsd to inform it of an app update");
                        return;
                    }
                    try {
                        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                            int uid = intent.getExtras().getInt("android.intent.extra.UID");
                            if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                                context.getPackageManager();
                                StatsCompanionService.sStatsd.informOnePackageRemoved(intent.getData().getSchemeSpecificPart(), uid);
                            }
                        } else {
                            PackageManager pm = context.getPackageManager();
                            int uid2 = intent.getExtras().getInt("android.intent.extra.UID");
                            String app = intent.getData().getSchemeSpecificPart();
                            PackageInfo pi = pm.getPackageInfo(app, DumpState.DUMP_CHANGES);
                            try {
                                installer = pm.getInstallerPackageName(app);
                            } catch (IllegalArgumentException e) {
                                installer = "";
                            }
                            StatsCompanionService.sStatsd.informOnePackage(app, uid2, pi.getLongVersionCode(), pi.versionName, installer == null ? "" : installer);
                        }
                    } catch (Exception e2) {
                        Slog.w(StatsCompanionService.TAG, "Failed to inform statsd of an app update", e2);
                    }
                }
            }
        }
    }

    public static final class AnomalyAlarmListener implements AlarmManager.OnAlarmListener {
        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            Slog.i(StatsCompanionService.TAG, "StatsCompanionService believes an anomaly has occurred at time " + System.currentTimeMillis() + "ms.");
            synchronized (StatsCompanionService.sStatsdLock) {
                if (StatsCompanionService.sStatsd == null) {
                    Slog.w(StatsCompanionService.TAG, "Could not access statsd to inform it of anomaly alarm firing");
                    return;
                }
                try {
                    StatsCompanionService.sStatsd.informAnomalyAlarmFired();
                } catch (RemoteException e) {
                    Slog.w(StatsCompanionService.TAG, "Failed to inform statsd of anomaly alarm firing", e);
                }
            }
        }
    }

    public static final class PullingAlarmListener implements AlarmManager.OnAlarmListener {
        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            synchronized (StatsCompanionService.sStatsdLock) {
                if (StatsCompanionService.sStatsd == null) {
                    Slog.w(StatsCompanionService.TAG, "Could not access statsd to inform it of pulling alarm firing.");
                    return;
                }
                try {
                    StatsCompanionService.sStatsd.informPollAlarmFired();
                } catch (RemoteException e) {
                    Slog.w(StatsCompanionService.TAG, "Failed to inform statsd of pulling alarm firing.", e);
                }
            }
        }
    }

    public static final class PeriodicAlarmListener implements AlarmManager.OnAlarmListener {
        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            synchronized (StatsCompanionService.sStatsdLock) {
                if (StatsCompanionService.sStatsd == null) {
                    Slog.w(StatsCompanionService.TAG, "Could not access statsd to inform it of periodic alarm firing.");
                    return;
                }
                try {
                    StatsCompanionService.sStatsd.informAlarmForSubscriberTriggeringFired();
                } catch (RemoteException e) {
                    Slog.w(StatsCompanionService.TAG, "Failed to inform statsd of periodic alarm firing.", e);
                }
            }
        }
    }

    public static final class ShutdownEventReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.REBOOT") || (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN") && (intent.getFlags() & 268435456) != 0)) {
                Slog.i(StatsCompanionService.TAG, "StatsCompanionService noticed a shutdown.");
                synchronized (StatsCompanionService.sStatsdLock) {
                    if (StatsCompanionService.sStatsd == null) {
                        Slog.w(StatsCompanionService.TAG, "Could not access statsd to inform it of a shutdown event.");
                        return;
                    }
                    try {
                        StatsCompanionService.sStatsd.informDeviceShutdown();
                    } catch (Exception e) {
                        Slog.w(StatsCompanionService.TAG, "Failed to inform statsd of a shutdown event.", e);
                    }
                }
            }
        }
    }

    public void setAnomalyAlarm(long timestampMs) {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setExact(3, timestampMs, "StatsCompanionService.anomaly", this.mAnomalyAlarmListener, this.mHandler);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void cancelAnomalyAlarm() {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.cancel(this.mAnomalyAlarmListener);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void setAlarmForSubscriberTriggering(long timestampMs) {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setExact(3, timestampMs, "StatsCompanionService.periodic", this.mPeriodicAlarmListener, this.mHandler);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void cancelAlarmForSubscriberTriggering() {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.cancel(this.mPeriodicAlarmListener);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void setPullingAlarm(long nextPullTimeMs) {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setExact(3, nextPullTimeMs, "StatsCompanionService.pull", this.mPullingAlarmListener, this.mHandler);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void cancelPullingAlarm() {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.cancel(this.mPullingAlarmListener);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    private void addNetworkStats(int tag, List<StatsLogEventWrapper> ret, NetworkStats stats, boolean withFGBG) {
        int size = stats.size();
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        long wallClockNanos = SystemClock.currentTimeMicro() * 1000;
        NetworkStats.Entry entry = new NetworkStats.Entry();
        for (int j = 0; j < size; j++) {
            stats.getValues(j, entry);
            StatsLogEventWrapper e = new StatsLogEventWrapper(tag, elapsedNanos, wallClockNanos);
            e.writeInt(entry.uid);
            if (withFGBG) {
                e.writeInt(entry.set);
            }
            e.writeLong(entry.rxBytes);
            e.writeLong(entry.rxPackets);
            e.writeLong(entry.txBytes);
            e.writeLong(entry.txPackets);
            ret.add(e);
        }
    }

    private NetworkStats rollupNetworkStatsByFGBG(NetworkStats stats) {
        NetworkStats ret = new NetworkStats(stats.getElapsedRealtime(), 1);
        NetworkStats.Entry entry = new NetworkStats.Entry();
        entry.iface = NetworkStats.IFACE_ALL;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        int size = stats.size();
        NetworkStats.Entry recycle = new NetworkStats.Entry();
        for (int i = 0; i < size; i++) {
            stats.getValues(i, recycle);
            if (recycle.tag == 0) {
                entry.set = recycle.set;
                entry.uid = recycle.uid;
                entry.rxBytes = recycle.rxBytes;
                entry.rxPackets = recycle.rxPackets;
                entry.txBytes = recycle.txBytes;
                entry.txPackets = recycle.txPackets;
                ret.combineValues(entry);
            }
        }
        return ret;
    }

    private static <T extends Parcelable> T awaitControllerInfo(SynchronousResultReceiver receiver) {
        if (receiver == null) {
            return null;
        }
        try {
            SynchronousResultReceiver.Result result = receiver.awaitResult((long) EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS);
            if (result.bundle != null) {
                result.bundle.setDefusable(true);
                T data = (T) result.bundle.getParcelable(RESULT_RECEIVER_CONTROLLER_KEY);
                if (data != null) {
                    return data;
                }
            }
            Slog.e(TAG, "no controller energy info supplied for " + receiver.getName());
        } catch (TimeoutException e) {
            Slog.w(TAG, "timeout reading " + receiver.getName() + " stats");
        }
        return null;
    }

    private void pullKernelWakelock(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        for (Map.Entry<String, KernelWakelockStats.Entry> ent : this.mKernelWakelockReader.readKernelWakelockStats(this.mTmpWakelockStats).entrySet()) {
            KernelWakelockStats.Entry kws = ent.getValue();
            StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e.writeString(ent.getKey());
            e.writeInt(kws.mCount);
            e.writeInt(kws.mVersion);
            e.writeLong(kws.mTotalTime);
            pulledData.add(e);
        }
    }

    private void pullWifiBytesTransfer(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getWifiIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
            } else if (this.mNetworkStatsService == null) {
                Slog.e(TAG, "NetworkStats Service is not available!");
                Binder.restoreCallingIdentity(token);
            } else {
                addNetworkStats(tagId, pulledData, this.mNetworkStatsService.getDetailedUidStats(ifaces).groupedByUid(), false);
                Binder.restoreCallingIdentity(token);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Pulling netstats for wifi bytes has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullWifiBytesTransferByFgBg(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getWifiIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
            } else if (this.mNetworkStatsService == null) {
                Slog.e(TAG, "NetworkStats Service is not available!");
                Binder.restoreCallingIdentity(token);
            } else {
                addNetworkStats(tagId, pulledData, rollupNetworkStatsByFGBG(this.mNetworkStatsService.getDetailedUidStats(ifaces)), true);
                Binder.restoreCallingIdentity(token);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Pulling netstats for wifi bytes w/ fg/bg has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullMobileBytesTransfer(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getMobileIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
            } else if (this.mNetworkStatsService == null) {
                Slog.e(TAG, "NetworkStats Service is not available!");
                Binder.restoreCallingIdentity(token);
            } else {
                addNetworkStats(tagId, pulledData, this.mNetworkStatsService.getDetailedUidStats(ifaces).groupedByUid(), false);
                Binder.restoreCallingIdentity(token);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Pulling netstats for mobile bytes has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullBluetoothBytesTransfer(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        BluetoothActivityEnergyInfo info = fetchBluetoothData();
        if (info.getUidTraffic() != null) {
            UidTraffic[] uidTraffic = info.getUidTraffic();
            for (UidTraffic traffic : uidTraffic) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(traffic.getUid());
                e.writeLong(traffic.getRxBytes());
                e.writeLong(traffic.getTxBytes());
                pulledData.add(e);
            }
        }
    }

    private void pullMobileBytesTransferByFgBg(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getMobileIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
            } else if (this.mNetworkStatsService == null) {
                Slog.e(TAG, "NetworkStats Service is not available!");
                Binder.restoreCallingIdentity(token);
            } else {
                addNetworkStats(tagId, pulledData, rollupNetworkStatsByFGBG(this.mNetworkStatsService.getDetailedUidStats(ifaces)), true);
                Binder.restoreCallingIdentity(token);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Pulling netstats for mobile bytes w/ fg/bg has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullCpuTimePerFreq(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        int cluster = 0;
        while (true) {
            KernelCpuSpeedReader[] kernelCpuSpeedReaderArr = this.mKernelCpuSpeedReaders;
            if (cluster < kernelCpuSpeedReaderArr.length) {
                long[] clusterTimeMs = kernelCpuSpeedReaderArr[cluster].readAbsolute();
                if (clusterTimeMs != null) {
                    for (int speed = clusterTimeMs.length - 1; speed >= 0; speed--) {
                        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                        e.writeInt(cluster);
                        e.writeInt(speed);
                        e.writeLong(clusterTimeMs[speed]);
                        pulledData.add(e);
                    }
                }
                cluster++;
            } else {
                return;
            }
        }
    }

    private void pullKernelUidCpuTime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        this.mCpuUidUserSysTimeReader.readAbsolute(new KernelCpuUidTimeReader.Callback(tagId, elapsedNanos, wallClockNanos, pulledData) {
            /* class com.android.server.stats.$$Lambda$StatsCompanionService$UBm3QCI0bvsnm37DPdPZCp_VPm0 */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ List f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
            }

            public final void onUidCpuTime(int i, Object obj) {
                StatsCompanionService.lambda$pullKernelUidCpuTime$1(this.f$0, this.f$1, this.f$2, this.f$3, i, (long[]) obj);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuTime$1(int tagId, long elapsedNanos, long wallClockNanos, List pulledData, int uid, long[] timesUs) {
        long userTimeUs = timesUs[0];
        long systemTimeUs = timesUs[1];
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeInt(uid);
        e.writeLong(userTimeUs);
        e.writeLong(systemTimeUs);
        pulledData.add(e);
    }

    private void pullKernelUidCpuFreqTime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        this.mCpuUidFreqTimeReader.readAbsolute(new KernelCpuUidTimeReader.Callback(tagId, elapsedNanos, wallClockNanos, pulledData) {
            /* class com.android.server.stats.$$Lambda$StatsCompanionService$CEX8Lyeg27KwlBp4VWGjp9hZExA */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ List f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
            }

            public final void onUidCpuTime(int i, Object obj) {
                StatsCompanionService.lambda$pullKernelUidCpuFreqTime$2(this.f$0, this.f$1, this.f$2, this.f$3, i, (long[]) obj);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuFreqTime$2(int tagId, long elapsedNanos, long wallClockNanos, List pulledData, int uid, long[] cpuFreqTimeMs) {
        for (int freqIndex = 0; freqIndex < cpuFreqTimeMs.length; freqIndex++) {
            if (cpuFreqTimeMs[freqIndex] != 0) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(uid);
                e.writeInt(freqIndex);
                e.writeLong(cpuFreqTimeMs[freqIndex]);
                pulledData.add(e);
            }
        }
    }

    private void pullKernelUidCpuClusterTime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        this.mCpuUidClusterTimeReader.readAbsolute(new KernelCpuUidTimeReader.Callback(tagId, elapsedNanos, wallClockNanos, pulledData) {
            /* class com.android.server.stats.$$Lambda$StatsCompanionService$8XwH_9_4XyR23VE4UEw0TLmPhXk */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ List f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
            }

            public final void onUidCpuTime(int i, Object obj) {
                StatsCompanionService.lambda$pullKernelUidCpuClusterTime$3(this.f$0, this.f$1, this.f$2, this.f$3, i, (long[]) obj);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuClusterTime$3(int tagId, long elapsedNanos, long wallClockNanos, List pulledData, int uid, long[] cpuClusterTimesMs) {
        for (int i = 0; i < cpuClusterTimesMs.length; i++) {
            StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e.writeInt(uid);
            e.writeInt(i);
            e.writeLong(cpuClusterTimesMs[i]);
            pulledData.add(e);
        }
    }

    private void pullKernelUidCpuActiveTime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        this.mCpuUidActiveTimeReader.readAbsolute(new KernelCpuUidTimeReader.Callback(tagId, elapsedNanos, wallClockNanos, pulledData) {
            /* class com.android.server.stats.$$Lambda$StatsCompanionService$xPcEMoQkRUC4lkJfxYah3VZObc */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ List f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
            }

            public final void onUidCpuTime(int i, Object obj) {
                StatsCompanionService.lambda$pullKernelUidCpuActiveTime$4(this.f$0, this.f$1, this.f$2, this.f$3, i, (Long) obj);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuActiveTime$4(int tagId, long elapsedNanos, long wallClockNanos, List pulledData, int uid, Long cpuActiveTimesMs) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeInt(uid);
        e.writeLong(cpuActiveTimesMs.longValue());
        pulledData.add(e);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008d, code lost:
        r0 = th;
     */
    private void pullWifiActivityInfo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable th;
        RemoteException e;
        long token = Binder.clearCallingIdentity();
        synchronized (this) {
            if (this.mWifiManager == null) {
                this.mWifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
            }
        }
        if (this.mWifiManager != null) {
            try {
                SynchronousResultReceiver wifiReceiver = new SynchronousResultReceiver("wifi");
                this.mWifiManager.requestActivityInfo(wifiReceiver);
                WifiActivityEnergyInfo wifiInfo = awaitControllerInfo(wifiReceiver);
                StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e2.writeLong(wifiInfo.getTimeStamp());
                e2.writeInt(wifiInfo.getStackState());
                e2.writeLong(wifiInfo.getControllerTxTimeMillis());
                e2.writeLong(wifiInfo.getControllerRxTimeMillis());
                e2.writeLong(wifiInfo.getControllerIdleTimeMillis());
                e2.writeLong(wifiInfo.getControllerEnergyUsed());
                try {
                    pulledData.add(e2);
                } catch (RemoteException e3) {
                    e = e3;
                }
            } catch (RemoteException e4) {
                e = e4;
                try {
                    Slog.e(TAG, "Pulling wifiManager for wifi controller activity energy info has error", e);
                    Binder.restoreCallingIdentity(token);
                    return;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
            return;
        }
        return;
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00a0, code lost:
        r0 = th;
     */
    private void pullModemActivityInfo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Binder.clearCallingIdentity();
        synchronized (this) {
            if (this.mTelephony == null) {
                this.mTelephony = TelephonyManager.from(this.mContext);
            }
        }
        if (this.mTelephony != null) {
            SynchronousResultReceiver modemReceiver = new SynchronousResultReceiver("telephony");
            this.mTelephony.requestModemActivityInfo(modemReceiver);
            ModemActivityInfo modemInfo = awaitControllerInfo(modemReceiver);
            StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e.writeLong(modemInfo.getTimestamp());
            e.writeLong((long) modemInfo.getSleepTimeMillis());
            e.writeLong((long) modemInfo.getIdleTimeMillis());
            e.writeLong((long) modemInfo.getTxTimeMillis()[0]);
            e.writeLong((long) modemInfo.getTxTimeMillis()[1]);
            e.writeLong((long) modemInfo.getTxTimeMillis()[2]);
            e.writeLong((long) modemInfo.getTxTimeMillis()[3]);
            e.writeLong((long) modemInfo.getTxTimeMillis()[4]);
            e.writeLong((long) modemInfo.getRxTimeMillis());
            e.writeLong((long) modemInfo.getEnergyUsed());
            pulledData.add(e);
            return;
        }
        return;
        while (true) {
        }
    }

    private void pullBluetoothActivityInfo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        BluetoothActivityEnergyInfo info = fetchBluetoothData();
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeLong(info.getTimeStamp());
        e.writeInt(info.getBluetoothStackState());
        e.writeLong(info.getControllerTxTimeMillis());
        e.writeLong(info.getControllerRxTimeMillis());
        e.writeLong(info.getControllerIdleTimeMillis());
        e.writeLong(info.getControllerEnergyUsed());
        pulledData.add(e);
    }

    private synchronized BluetoothActivityEnergyInfo fetchBluetoothData() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            SynchronousResultReceiver bluetoothReceiver = new SynchronousResultReceiver("bluetooth");
            adapter.requestControllerActivityEnergyInfo(bluetoothReceiver);
            return awaitControllerInfo(bluetoothReceiver);
        }
        Slog.e(TAG, "Failed to get bluetooth adapter!");
        return null;
    }

    private void pullSystemElapsedRealtime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeLong(SystemClock.elapsedRealtime());
        pulledData.add(e);
    }

    private void pullSystemUpTime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeLong(SystemClock.uptimeMillis());
        pulledData.add(e);
    }

    private void pullProcessMemoryState(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        for (ProcessMemoryState processMemoryState : ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getMemoryStateForProcesses()) {
            MemoryStatUtil.MemoryStat memoryStat = MemoryStatUtil.readMemoryStatFromFilesystem(processMemoryState.uid, processMemoryState.pid);
            if (memoryStat != null) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(processMemoryState.uid);
                e.writeString(processMemoryState.processName);
                e.writeInt(processMemoryState.oomScore);
                e.writeLong(memoryStat.pgfault);
                e.writeLong(memoryStat.pgmajfault);
                e.writeLong(memoryStat.rssInBytes);
                e.writeLong(memoryStat.cacheInBytes);
                e.writeLong(memoryStat.swapInBytes);
                e.writeLong(0);
                e.writeLong(memoryStat.startTimeNanos);
                e.writeInt(anonAndSwapInKilobytes(memoryStat));
                pulledData.add(e);
            }
        }
    }

    private void pullNativeProcessMemoryState(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        List<String> processNames = Arrays.asList(MEMORY_INTERESTING_NATIVE_PROCESSES);
        int[] pids = Process.getPidsForCommands(MEMORY_INTERESTING_NATIVE_PROCESSES);
        for (int pid : pids) {
            MemoryStatUtil.MemoryStat memoryStat = MemoryStatUtil.readMemoryStatFromProcfs(pid);
            if (memoryStat != null) {
                int uid = Process.getUidForPid(pid);
                String processName = MemoryStatUtil.readCmdlineFromProcfs(pid);
                if (processNames.contains(processName)) {
                    StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                    e.writeInt(uid);
                    e.writeString(processName);
                    e.writeLong(memoryStat.pgfault);
                    e.writeLong(memoryStat.pgmajfault);
                    e.writeLong(memoryStat.rssInBytes);
                    e.writeLong(0);
                    e.writeLong(memoryStat.startTimeNanos);
                    e.writeLong(memoryStat.swapInBytes);
                    e.writeInt(anonAndSwapInKilobytes(memoryStat));
                    pulledData.add(e);
                }
            }
        }
    }

    private static int anonAndSwapInKilobytes(MemoryStatUtil.MemoryStat memoryStat) {
        return (int) ((memoryStat.anonRssInBytes + memoryStat.swapInBytes) / 1024);
    }

    private void pullProcessMemoryHighWaterMark(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        for (ProcessMemoryState managedProcess : ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getMemoryStateForProcesses()) {
            long rssHighWaterMarkInBytes = MemoryStatUtil.readRssHighWaterMarkFromProcfs(managedProcess.pid);
            if (rssHighWaterMarkInBytes != 0) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(managedProcess.uid);
                e.writeString(managedProcess.processName);
                e.writeLong(rssHighWaterMarkInBytes);
                pulledData.add(e);
            }
        }
        int[] pids = Process.getPidsForCommands(MEMORY_INTERESTING_NATIVE_PROCESSES);
        for (int pid : pids) {
            int uid = Process.getUidForPid(pid);
            String processName = MemoryStatUtil.readCmdlineFromProcfs(pid);
            long rssHighWaterMarkInBytes2 = MemoryStatUtil.readRssHighWaterMarkFromProcfs(pid);
            StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e2.writeInt(uid);
            e2.writeString(processName);
            e2.writeLong(rssHighWaterMarkInBytes2);
            pulledData.add(e2);
        }
        SystemProperties.set("sys.rss_hwm_reset.on", "1");
    }

    private void pullSystemIonHeapSize(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        long systemIonHeapSizeInBytes = MemoryStatUtil.readSystemIonHeapSizeFromDebugfs();
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeLong(systemIonHeapSizeInBytes);
        pulledData.add(e);
    }

    private void pullProcessSystemIonHeapSize(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        for (MemoryStatUtil.IonAllocations allocations : MemoryStatUtil.readProcessSystemIonHeapSizesFromDebugfs()) {
            StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e.writeInt(Process.getUidForPid(allocations.pid));
            e.writeString(MemoryStatUtil.readCmdlineFromProcfs(allocations.pid));
            e.writeInt((int) (allocations.totalSizeInBytes / 1024));
            e.writeInt(allocations.count);
            e.writeInt((int) (allocations.maxSizeInBytes / 1024));
            pulledData.add(e);
        }
    }

    private void pullBinderCallsStats(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        BinderCallsStatsService.Internal binderStats = (BinderCallsStatsService.Internal) LocalServices.getService(BinderCallsStatsService.Internal.class);
        if (binderStats != null) {
            List<BinderCallsStats.ExportedCallStat> callStats = binderStats.getExportedCallStats();
            binderStats.reset();
            for (BinderCallsStats.ExportedCallStat callStat : callStats) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(callStat.workSourceUid);
                e.writeString(callStat.className);
                e.writeString(callStat.methodName);
                e.writeLong(callStat.callCount);
                e.writeLong(callStat.exceptionCount);
                e.writeLong(callStat.latencyMicros);
                e.writeLong(callStat.maxLatencyMicros);
                e.writeLong(callStat.cpuTimeMicros);
                e.writeLong(callStat.maxCpuTimeMicros);
                e.writeLong(callStat.maxReplySizeBytes);
                e.writeLong(callStat.maxRequestSizeBytes);
                e.writeLong(callStat.recordedCallCount);
                e.writeInt(callStat.screenInteractive ? 1 : 0);
                e.writeInt(callStat.callingUid);
                pulledData.add(e);
            }
            return;
        }
        throw new IllegalStateException("binderStats is null");
    }

    private void pullBinderCallsStatsExceptions(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        BinderCallsStatsService.Internal binderStats = (BinderCallsStatsService.Internal) LocalServices.getService(BinderCallsStatsService.Internal.class);
        if (binderStats != null) {
            for (Map.Entry<String, Integer> entry : binderStats.getExportedExceptionStats().entrySet()) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeString(entry.getKey());
                e.writeInt(entry.getValue().intValue());
                pulledData.add(e);
            }
            return;
        }
        throw new IllegalStateException("binderStats is null");
    }

    private void pullLooperStats(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        LooperStats looperStats = (LooperStats) LocalServices.getService(LooperStats.class);
        if (looperStats != null) {
            List<LooperStats.ExportedEntry> entries = looperStats.getEntries();
            looperStats.reset();
            for (LooperStats.ExportedEntry entry : entries) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(entry.workSourceUid);
                e.writeString(entry.handlerClassName);
                e.writeString(entry.threadName);
                e.writeString(entry.messageName);
                e.writeLong(entry.messageCount);
                e.writeLong(entry.exceptionCount);
                e.writeLong(entry.recordedMessageCount);
                e.writeLong(entry.totalLatencyMicros);
                e.writeLong(entry.cpuUsageMicros);
                e.writeBoolean(entry.isInteractive);
                e.writeLong(entry.maxCpuUsageMicros);
                e.writeLong(entry.maxLatencyMicros);
                e.writeLong(entry.recordedDelayMessageCount);
                e.writeLong(entry.delayMillis);
                e.writeLong(entry.maxDelayMillis);
                pulledData.add(e);
            }
            return;
        }
        throw new IllegalStateException("looperStats null");
    }

    private void pullDiskStats(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        byte[] junk = new byte[512];
        for (int i = 0; i < junk.length; i++) {
            junk[i] = (byte) i;
        }
        File tmp = new File(Environment.getDataDirectory(), "system/statsdperftest.tmp");
        FileOutputStream fos = null;
        IOException error = null;
        long before = SystemClock.elapsedRealtime();
        try {
            fos = new FileOutputStream(tmp);
            fos.write(junk);
            try {
                fos.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            error = e2;
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        long latency = SystemClock.elapsedRealtime() - before;
        if (tmp.exists()) {
            tmp.delete();
        }
        if (error != null) {
            Slog.e(TAG, "Error performing diskstats latency test");
            latency = -1;
        }
        boolean fileBased = StorageManager.isFileEncryptedNativeOnly();
        int writeSpeed = -1;
        try {
            IBinder binder = ServiceManager.getService("storaged");
            if (binder == null) {
                Slog.e(TAG, "storaged not found");
            }
            writeSpeed = IStoraged.Stub.asInterface(binder).getRecentPerf();
        } catch (RemoteException e4) {
            Slog.e(TAG, "storaged not found");
        }
        StatsLogEventWrapper e5 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e5.writeLong(latency);
        e5.writeBoolean(fileBased);
        e5.writeInt(writeSpeed);
        pulledData.add(e5);
    }

    private void pullDirectoryUsage(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        StatFs statFsData = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        StatFs statFsSystem = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        StatFs statFsCache = new StatFs(Environment.getDownloadCacheDirectory().getAbsolutePath());
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeInt(1);
        e.writeLong(statFsData.getAvailableBytes());
        e.writeLong(statFsData.getTotalBytes());
        pulledData.add(e);
        StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e2.writeInt(2);
        e2.writeLong(statFsCache.getAvailableBytes());
        e2.writeLong(statFsCache.getTotalBytes());
        pulledData.add(e2);
        StatsLogEventWrapper e3 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e3.writeInt(3);
        e3.writeLong(statFsSystem.getAvailableBytes());
        e3.writeLong(statFsSystem.getTotalBytes());
        pulledData.add(e3);
    }

    private void pullAppSize(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Exception e;
        try {
            JSONObject json = new JSONObject(IoUtils.readFileAsString(DiskStatsLoggingService.DUMPSYS_CACHE_PATH));
            long cache_time = json.optLong(DiskStatsFileLogger.LAST_QUERY_TIMESTAMP_KEY, -1);
            JSONArray pkg_names = json.getJSONArray(DiskStatsFileLogger.PACKAGE_NAMES_KEY);
            JSONArray app_sizes = json.getJSONArray(DiskStatsFileLogger.APP_SIZES_KEY);
            JSONArray app_data_sizes = json.getJSONArray(DiskStatsFileLogger.APP_DATA_KEY);
            JSONArray app_cache_sizes = json.getJSONArray(DiskStatsFileLogger.APP_CACHES_KEY);
            int length = pkg_names.length();
            if (app_sizes.length() == length && app_data_sizes.length() == length) {
                if (app_cache_sizes.length() == length) {
                    for (int i = 0; i < length; i++) {
                        StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                        e2.writeString(pkg_names.getString(i));
                        e2.writeLong(app_sizes.optLong(i, -1));
                        e2.writeLong(app_data_sizes.optLong(i, -1));
                        e2.writeLong(app_cache_sizes.optLong(i, -1));
                        e2.writeLong(cache_time);
                        try {
                            pulledData.add(e2);
                        } catch (IOException | JSONException e3) {
                            e = e3;
                            Slog.e(TAG, "exception reading diskstats cache file", e);
                        }
                    }
                    return;
                }
            }
            Slog.e(TAG, "formatting error in diskstats cache file!");
        } catch (IOException | JSONException e4) {
            e = e4;
            Slog.e(TAG, "exception reading diskstats cache file", e);
        }
    }

    private void pullCategorySize(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        try {
            JSONObject json = new JSONObject(IoUtils.readFileAsString(DiskStatsLoggingService.DUMPSYS_CACHE_PATH));
            long cacheTime = json.optLong(DiskStatsFileLogger.LAST_QUERY_TIMESTAMP_KEY, -1);
            StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e.writeInt(1);
            e.writeLong(json.optLong(DiskStatsFileLogger.APP_SIZE_AGG_KEY, -1));
            e.writeLong(cacheTime);
            pulledData.add(e);
            StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e2.writeInt(2);
            e2.writeLong(json.optLong(DiskStatsFileLogger.APP_DATA_SIZE_AGG_KEY, -1));
            e2.writeLong(cacheTime);
            pulledData.add(e2);
            StatsLogEventWrapper e3 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e3.writeInt(3);
            e3.writeLong(json.optLong(DiskStatsFileLogger.APP_CACHE_AGG_KEY, -1));
            e3.writeLong(cacheTime);
            pulledData.add(e3);
            StatsLogEventWrapper e4 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e4.writeInt(4);
            e4.writeLong(json.optLong(DiskStatsFileLogger.PHOTOS_KEY, -1));
            e4.writeLong(cacheTime);
            pulledData.add(e4);
            StatsLogEventWrapper e5 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e5.writeInt(5);
            e5.writeLong(json.optLong(DiskStatsFileLogger.VIDEOS_KEY, -1));
            e5.writeLong(cacheTime);
            pulledData.add(e5);
            StatsLogEventWrapper e6 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e6.writeInt(6);
            e6.writeLong(json.optLong(DiskStatsFileLogger.AUDIO_KEY, -1));
            e6.writeLong(cacheTime);
            pulledData.add(e6);
            StatsLogEventWrapper e7 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e7.writeInt(7);
            e7.writeLong(json.optLong(DiskStatsFileLogger.DOWNLOADS_KEY, -1));
            e7.writeLong(cacheTime);
            pulledData.add(e7);
            StatsLogEventWrapper e8 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e8.writeInt(8);
            e8.writeLong(json.optLong(DiskStatsFileLogger.SYSTEM_KEY, -1));
            e8.writeLong(cacheTime);
            pulledData.add(e8);
            StatsLogEventWrapper e9 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e9.writeInt(9);
            e9.writeLong(json.optLong(DiskStatsFileLogger.MISC_KEY, -1));
            e9.writeLong(cacheTime);
            pulledData.add(e9);
        } catch (IOException | JSONException e10) {
            Slog.e(TAG, "exception reading diskstats cache file", e10);
        }
    }

    private void pullNumBiometricsEnrolled(int modality, int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        UserManager userManager;
        int numEnrolled;
        PackageManager pm = this.mContext.getPackageManager();
        FingerprintManager fingerprintManager = null;
        FaceManager faceManager = null;
        if (pm.hasSystemFeature("android.hardware.fingerprint")) {
            fingerprintManager = (FingerprintManager) this.mContext.getSystemService(FingerprintManager.class);
        }
        if (pm.hasSystemFeature("android.hardware.biometrics.face")) {
            faceManager = (FaceManager) this.mContext.getSystemService(FaceManager.class);
        }
        if (!(modality == 1 && fingerprintManager == null)) {
            if (!((modality == 4 && faceManager == null) || (userManager = (UserManager) this.mContext.getSystemService(UserManager.class)) == null)) {
                long token = Binder.clearCallingIdentity();
                for (UserInfo user : userManager.getUsers()) {
                    int userId = user.getUserHandle().getIdentifier();
                    if (modality == 1) {
                        numEnrolled = fingerprintManager.getEnrolledFingerprints(userId).size();
                    } else if (modality == 4) {
                        numEnrolled = faceManager.getEnrolledFaces(userId).size();
                    } else {
                        return;
                    }
                    StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                    e.writeInt(userId);
                    e.writeInt(numEnrolled);
                    pulledData.add(e);
                }
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private long readProcStatsHighWaterMark(int section) {
        try {
            File[] files = this.mBaseDir.listFiles(new FilenameFilter(section) {
                /* class com.android.server.stats.$$Lambda$StatsCompanionService$BNidAMcUS8SzzhAyVRAjvZQ7Gc */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.io.FilenameFilter
                public final boolean accept(File file, String str) {
                    int i = this.f$0;
                    return str.toLowerCase();
                }
            });
            if (files != null) {
                if (files.length != 0) {
                    if (files.length > 1) {
                        Log.e(TAG, "Only 1 file expected for high water mark. Found " + files.length);
                    }
                    return Long.valueOf(files[0].getName().split("_")[1]).longValue();
                }
            }
            return 0;
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to get procstats high watermark file.", e);
            return 0;
        } catch (NumberFormatException e2) {
            Log.e(TAG, "Failed to parse file name.", e2);
            return 0;
        }
    }

    private void pullProcessStats(int section, int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable th;
        IOException e;
        RemoteException e2;
        SecurityException e3;
        synchronized (this) {
            try {
                long lastHighWaterMark = readProcStatsHighWaterMark(section);
                List<ParcelFileDescriptor> statsFiles = new ArrayList<>();
                long highWaterMark = this.mProcessStats.getCommittedStats(lastHighWaterMark, section, true, statsFiles);
                if (statsFiles.size() != 1) {
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } else {
                    int[] len = new int[1];
                    byte[] stats = readFully(new ParcelFileDescriptor.AutoCloseInputStream(statsFiles.get(0)), len);
                    StatsLogEventWrapper e4 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                    e4.writeStorage(Arrays.copyOf(stats, len[0]));
                    try {
                        pulledData.add(e4);
                        new File(this.mBaseDir.getAbsolutePath() + SliceClientPermissions.SliceAuthority.DELIMITER + section + "_" + lastHighWaterMark).delete();
                        new File(this.mBaseDir.getAbsolutePath() + SliceClientPermissions.SliceAuthority.DELIMITER + section + "_" + highWaterMark).createNewFile();
                    } catch (IOException e5) {
                        e = e5;
                    } catch (RemoteException e6) {
                        e2 = e6;
                        Log.e(TAG, "Getting procstats failed: ", e2);
                    } catch (SecurityException e7) {
                        e3 = e7;
                        Log.e(TAG, "Getting procstats failed: ", e3);
                    }
                }
            } catch (IOException e8) {
                e = e8;
                Log.e(TAG, "Getting procstats failed: ", e);
            } catch (RemoteException e9) {
                e2 = e9;
                Log.e(TAG, "Getting procstats failed: ", e2);
            } catch (SecurityException e10) {
                e3 = e10;
                Log.e(TAG, "Getting procstats failed: ", e3);
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    static byte[] readFully(InputStream stream, int[] outLen) throws IOException {
        int pos = 0;
        int initialAvail = stream.available();
        byte[] data = new byte[(initialAvail > 0 ? initialAvail + 1 : DumpState.DUMP_KEYSETS)];
        while (true) {
            int amt = stream.read(data, pos, data.length - pos);
            if (amt < 0) {
                outLen[0] = pos;
                return data;
            }
            pos += amt;
            if (pos >= data.length) {
                byte[] newData = new byte[(pos + DumpState.DUMP_KEYSETS)];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
        }
    }

    private void pullPowerProfile(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        PowerProfile powerProfile = new PowerProfile(this.mContext);
        Preconditions.checkNotNull(powerProfile);
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        ProtoOutputStream proto = new ProtoOutputStream();
        powerProfile.writeToProto(proto);
        proto.flush();
        e.writeStorage(proto.getBytes());
        pulledData.add(e);
    }

    private void pullBuildInformation(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeString(Build.FINGERPRINT);
        e.writeString(Build.BRAND);
        e.writeString(Build.PRODUCT);
        e.writeString(Build.DEVICE);
        e.writeString(Build.VERSION.RELEASE);
        e.writeString(Build.ID);
        e.writeString(Build.VERSION.INCREMENTAL);
        e.writeString(Build.TYPE);
        e.writeString(Build.TAGS);
        pulledData.add(e);
    }

    /* JADX INFO: finally extract failed */
    private BatteryStatsHelper getBatteryStatsHelper() {
        if (this.mBatteryStatsHelper == null) {
            long callingToken = Binder.clearCallingIdentity();
            try {
                this.mBatteryStatsHelper = new BatteryStatsHelper(this.mContext, false);
                Binder.restoreCallingIdentity(callingToken);
                this.mBatteryStatsHelper.create((Bundle) null);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingToken);
                throw th;
            }
        }
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - this.mBatteryStatsHelperTimestampMs >= 1000) {
            this.mBatteryStatsHelper.refreshStats(0, -1);
            this.mBatteryStatsHelper.clearStats();
            this.mBatteryStatsHelperTimestampMs = currentTime;
        }
        return this.mBatteryStatsHelper;
    }

    private long milliAmpHrsToNanoAmpSecs(double mAh) {
        return (long) ((3.6E9d * mAh) + 0.5d);
    }

    private void pullDeviceCalculatedPowerUse(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        BatteryStatsHelper bsHelper = getBatteryStatsHelper();
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeLong(milliAmpHrsToNanoAmpSecs(bsHelper.getComputedPower()));
        pulledData.add(e);
    }

    private void pullDeviceCalculatedPowerBlameUid(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        List<BatterySipper> sippers = getBatteryStatsHelper().getUsageList();
        if (sippers != null) {
            for (BatterySipper bs : sippers) {
                BatterySipper.DrainType drainType = bs.drainType;
                BatterySipper.DrainType drainType2 = bs.drainType;
                if (drainType == BatterySipper.DrainType.APP) {
                    StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                    e.writeInt(bs.uidObj.getUid());
                    e.writeLong(milliAmpHrsToNanoAmpSecs(bs.totalPowerMah));
                    pulledData.add(e);
                }
            }
        }
    }

    private void pullDeviceCalculatedPowerBlameOther(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        List<BatterySipper> sippers = getBatteryStatsHelper().getUsageList();
        if (sippers != null) {
            for (BatterySipper bs : sippers) {
                BatterySipper.DrainType drainType = bs.drainType;
                BatterySipper.DrainType drainType2 = bs.drainType;
                if (drainType != BatterySipper.DrainType.APP) {
                    BatterySipper.DrainType drainType3 = bs.drainType;
                    BatterySipper.DrainType drainType4 = bs.drainType;
                    if (drainType3 != BatterySipper.DrainType.USER) {
                        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                        e.writeInt(bs.drainType.ordinal());
                        e.writeLong(milliAmpHrsToNanoAmpSecs(bs.totalPowerMah));
                        pulledData.add(e);
                    }
                }
            }
        }
    }

    private void pullDiskIo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        this.mStoragedUidIoStatsReader.readAbsolute(new StoragedUidIoStatsReader.Callback(tagId, elapsedNanos, wallClockNanos, pulledData) {
            /* class com.android.server.stats.$$Lambda$StatsCompanionService$C35JUjeqVrZ2ptbyqiMciF6UQM */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ List f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
            }

            public final void onUidStorageStats(int i, long j, long j2, long j3, long j4, long j5, long j6, long j7, long j8, long j9, long j10) {
                StatsCompanionService.lambda$pullDiskIo$6(this.f$0, this.f$1, this.f$2, this.f$3, i, j, j2, j3, j4, j5, j6, j7, j8, j9, j10);
            }
        });
    }

    static /* synthetic */ void lambda$pullDiskIo$6(int tagId, long elapsedNanos, long wallClockNanos, List pulledData, int uid, long fgCharsRead, long fgCharsWrite, long fgBytesRead, long fgBytesWrite, long bgCharsRead, long bgCharsWrite, long bgBytesRead, long bgBytesWrite, long fgFsync, long bgFsync) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeInt(uid);
        e.writeLong(fgCharsRead);
        e.writeLong(fgCharsWrite);
        e.writeLong(fgBytesRead);
        e.writeLong(fgBytesWrite);
        e.writeLong(bgCharsRead);
        e.writeLong(bgCharsWrite);
        e.writeLong(bgBytesRead);
        e.writeLong(bgBytesWrite);
        e.writeLong(fgFsync);
        e.writeLong(bgFsync);
        pulledData.add(e);
    }

    private void pullProcessCpuTime(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        synchronized (this) {
            if (this.mProcessCpuTracker == null) {
                this.mProcessCpuTracker = new ProcessCpuTracker(false);
                this.mProcessCpuTracker.init();
            }
            this.mProcessCpuTracker.update();
            for (int i = 0; i < this.mProcessCpuTracker.countStats(); i++) {
                ProcessCpuTracker.Stats st = this.mProcessCpuTracker.getStats(i);
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(st.uid);
                e.writeString(st.name);
                e.writeLong(st.base_utime);
                e.writeLong(st.base_stime);
                pulledData.add(e);
            }
        }
    }

    private void pullCpuTimePerThreadFreq(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        KernelCpuThreadReaderDiff kernelCpuThreadReaderDiff = this.mKernelCpuThreadReader;
        if (kernelCpuThreadReaderDiff != null) {
            ArrayList<KernelCpuThreadReader.ProcessCpuUsage> processCpuUsages = kernelCpuThreadReaderDiff.getProcessCpuUsageDiffed();
            if (processCpuUsages != null) {
                int[] cpuFrequencies = this.mKernelCpuThreadReader.getCpuFrequenciesKhz();
                if (cpuFrequencies.length <= 8) {
                    for (int i = 0; i < processCpuUsages.size(); i++) {
                        KernelCpuThreadReader.ProcessCpuUsage processCpuUsage = processCpuUsages.get(i);
                        ArrayList<KernelCpuThreadReader.ThreadCpuUsage> threadCpuUsages = processCpuUsage.threadCpuUsages;
                        for (int j = 0; j < threadCpuUsages.size(); j++) {
                            KernelCpuThreadReader.ThreadCpuUsage threadCpuUsage = threadCpuUsages.get(j);
                            if (threadCpuUsage.usageTimesMillis.length == cpuFrequencies.length) {
                                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                                e.writeInt(processCpuUsage.uid);
                                e.writeInt(processCpuUsage.processId);
                                e.writeInt(threadCpuUsage.threadId);
                                e.writeString(processCpuUsage.processName);
                                e.writeString(threadCpuUsage.threadName);
                                for (int k = 0; k < 8; k++) {
                                    if (k < cpuFrequencies.length) {
                                        e.writeInt(cpuFrequencies[k]);
                                        e.writeInt(threadCpuUsage.usageTimesMillis[k]);
                                    } else {
                                        e.writeInt(0);
                                        e.writeInt(0);
                                    }
                                }
                                pulledData.add(e);
                            } else {
                                String message = "Unexpected number of usage times, expected " + cpuFrequencies.length + " but got " + threadCpuUsage.usageTimesMillis.length;
                                Slog.w(TAG, message);
                                throw new IllegalStateException(message);
                            }
                        }
                    }
                    return;
                }
                String message2 = "Expected maximum 8 frequencies, but got " + cpuFrequencies.length;
                Slog.w(TAG, message2);
                throw new IllegalStateException(message2);
            }
            throw new IllegalStateException("processCpuUsages is null");
        }
        throw new IllegalStateException("mKernelCpuThreadReader is null");
    }

    private void pullTemperature(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable th;
        long callingToken = Binder.clearCallingIdentity();
        try {
            for (Temperature temp : sThermalService.getCurrentTemperatures()) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(temp.getType());
                e.writeString(temp.getName());
                e.writeInt((int) (temp.getValue() * 10.0f));
                e.writeInt(temp.getStatus());
                try {
                    pulledData.add(e);
                } catch (RemoteException e2) {
                }
            }
        } catch (RemoteException e3) {
            try {
                Slog.e(TAG, "Disconnected from thermal service. Cannot pull temperatures.");
                Binder.restoreCallingIdentity(callingToken);
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(callingToken);
            throw th;
        }
        Binder.restoreCallingIdentity(callingToken);
    }

    private void pullCoolingDevices(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable th;
        long callingToken = Binder.clearCallingIdentity();
        try {
            for (CoolingDevice device : sThermalService.getCurrentCoolingDevices()) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                e.writeInt(device.getType());
                e.writeString(device.getName());
                e.writeInt((int) device.getValue());
                try {
                    pulledData.add(e);
                } catch (RemoteException e2) {
                }
            }
        } catch (RemoteException e3) {
            try {
                Slog.e(TAG, "Disconnected from thermal service. Cannot pull temperatures.");
                Binder.restoreCallingIdentity(callingToken);
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(callingToken);
            throw th;
        }
        Binder.restoreCallingIdentity(callingToken);
    }

    private void pullDebugElapsedClock(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        long elapsedMillis = SystemClock.elapsedRealtime();
        long j = this.mDebugElapsedClockPreviousValue;
        long clockDiffMillis = 0;
        if (j != 0) {
            clockDiffMillis = elapsedMillis - j;
        }
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e.writeLong(this.mDebugElapsedClockPullCount);
        e.writeLong(elapsedMillis);
        e.writeLong(elapsedMillis);
        e.writeLong(clockDiffMillis);
        e.writeInt(1);
        pulledData.add(e);
        if (this.mDebugElapsedClockPullCount % 2 == 1) {
            StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
            e2.writeLong(this.mDebugElapsedClockPullCount);
            e2.writeLong(elapsedMillis);
            e2.writeLong(elapsedMillis);
            e2.writeLong(clockDiffMillis);
            e2.writeInt(2);
            pulledData.add(e2);
        }
        this.mDebugElapsedClockPullCount++;
        this.mDebugElapsedClockPreviousValue = elapsedMillis;
    }

    private void pullDebugFailingElapsedClock(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        long elapsedMillis = SystemClock.elapsedRealtime();
        long j = this.mDebugFailingElapsedClockPullCount;
        this.mDebugFailingElapsedClockPullCount = 1 + j;
        long j2 = 0;
        if (j % 5 != 0) {
            e.writeLong(this.mDebugFailingElapsedClockPullCount);
            e.writeLong(elapsedMillis);
            e.writeLong(elapsedMillis);
            long j3 = this.mDebugFailingElapsedClockPreviousValue;
            if (j3 != 0) {
                j2 = elapsedMillis - j3;
            }
            e.writeLong(j2);
            this.mDebugFailingElapsedClockPreviousValue = elapsedMillis;
            pulledData.add(e);
            return;
        }
        this.mDebugFailingElapsedClockPreviousValue = elapsedMillis;
        throw new RuntimeException("Failing debug elapsed clock");
    }

    private void pullDangerousPermissionState(long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable t;
        PackageManager pm;
        PackageManager pm2;
        long token = Binder.clearCallingIdentity();
        try {
            PackageManager pm3 = this.mContext.getPackageManager();
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            int numUsers = users.size();
            int userNum = 0;
            while (userNum < numUsers) {
                UserHandle user = users.get(userNum).getUserHandle();
                List<PackageInfo> pkgs = pm3.getInstalledPackagesAsUser(4096, user.getIdentifier());
                int numPkgs = pkgs.size();
                int pkgNum = 0;
                while (pkgNum < numPkgs) {
                    PackageInfo pkg = pkgs.get(pkgNum);
                    if (pkg.requestedPermissions == null) {
                        pm = pm3;
                    } else {
                        int numPerms = pkg.requestedPermissions.length;
                        int permNum = 0;
                        while (permNum < numPerms) {
                            String permName = pkg.requestedPermissions[permNum];
                            try {
                                PermissionInfo permissionInfo = pm3.getPermissionInfo(permName, 0);
                                int permissionFlags = pm3.getPermissionFlags(permName, pkg.packageName, user);
                                pm2 = pm3;
                                if (permissionInfo.getProtection() == 1) {
                                    StatsLogEventWrapper e = new StatsLogEventWrapper(10050, elapsedNanos, wallClockNanos);
                                    e.writeString(permName);
                                    e.writeInt(pkg.applicationInfo.uid);
                                    e.writeString(pkg.packageName);
                                    e.writeBoolean((pkg.requestedPermissionsFlags[permNum] & 2) != 0);
                                    e.writeInt(permissionFlags);
                                    try {
                                        pulledData.add(e);
                                    } catch (Throwable th) {
                                        t = th;
                                    }
                                }
                            } catch (PackageManager.NameNotFoundException e2) {
                                pm2 = pm3;
                            }
                            permNum++;
                            pm3 = pm2;
                        }
                        pm = pm3;
                    }
                    pkgNum++;
                    pm3 = pm;
                }
                userNum++;
                pm3 = pm3;
            }
        } catch (Throwable th2) {
            t = th2;
            try {
                Log.e(TAG, "Could not read permissions", t);
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th3) {
                Binder.restoreCallingIdentity(token);
                throw th3;
            }
        }
        Binder.restoreCallingIdentity(token);
    }

    private void pullAppOps(long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable t;
        long token = Binder.clearCallingIdentity();
        try {
            AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
            CompletableFuture<AppOpsManager.HistoricalOps> ops = new CompletableFuture<>();
            AppOpsManager.HistoricalOpsRequest histOpsRequest = new AppOpsManager.HistoricalOpsRequest.Builder(Instant.now().minus(1L, (TemporalUnit) ChronoUnit.HOURS).toEpochMilli(), (long) JobStatus.NO_LATEST_RUNTIME).build();
            Executor mainExecutor = this.mContext.getMainExecutor();
            Objects.requireNonNull(ops);
            appOps.getHistoricalOps(histOpsRequest, mainExecutor, new Consumer(ops) {
                /* class com.android.server.stats.$$Lambda$wPejPqIRC0ueiw9uak8ULakT1R8 */
                private final /* synthetic */ CompletableFuture f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.complete((AppOpsManager.HistoricalOps) obj);
                }
            });
            AppOpsManager.HistoricalOps histOps = ops.get(EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            StatsLogEventWrapper e = new StatsLogEventWrapper(10060, elapsedNanos, wallClockNanos);
            int i = 0;
            int uidIdx = 0;
            while (uidIdx < histOps.getUidCount()) {
                AppOpsManager.HistoricalUidOps uidOps = histOps.getUidOpsAt(uidIdx);
                int uid = uidOps.getUid();
                int pkgIdx = i;
                while (pkgIdx < uidOps.getPackageCount()) {
                    AppOpsManager.HistoricalPackageOps packageOps = uidOps.getPackageOpsAt(pkgIdx);
                    int opIdx = i;
                    while (opIdx < packageOps.getOpCount()) {
                        AppOpsManager.HistoricalOp op = packageOps.getOpAt(opIdx);
                        e.writeInt(uid);
                        e.writeString(packageOps.getPackageName());
                        e.writeInt(op.getOpCode());
                        e.writeLong(op.getForegroundAccessCount(13));
                        e.writeLong(op.getBackgroundAccessCount(13));
                        e.writeLong(op.getForegroundRejectCount(13));
                        e.writeLong(op.getBackgroundRejectCount(13));
                        e.writeLong(op.getForegroundAccessDuration(13));
                        e.writeLong(op.getBackgroundAccessDuration(13));
                        try {
                            pulledData.add(e);
                            opIdx++;
                            appOps = appOps;
                        } catch (Throwable th) {
                            t = th;
                            try {
                                Log.e(TAG, "Could not read appops", t);
                                Binder.restoreCallingIdentity(token);
                            } catch (Throwable th2) {
                                Binder.restoreCallingIdentity(token);
                                throw th2;
                            }
                        }
                    }
                    pkgIdx++;
                    i = 0;
                }
                uidIdx++;
                i = 0;
            }
        } catch (Throwable th3) {
            t = th3;
            Log.e(TAG, "Could not read appops", t);
            Binder.restoreCallingIdentity(token);
        }
        Binder.restoreCallingIdentity(token);
    }

    private void pullRoleHolders(long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable th;
        PackageInfo pkg;
        long callingToken = Binder.clearCallingIdentity();
        try {
            PackageManager pm = this.mContext.getPackageManager();
            RoleManagerInternal rmi = (RoleManagerInternal) LocalServices.getService(RoleManagerInternal.class);
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            int numUsers = users.size();
            for (int userNum = 0; userNum < numUsers; userNum++) {
                int userId = users.get(userNum).getUserHandle().getIdentifier();
                ArrayMap<String, ArraySet<String>> roles = rmi.getRolesAndHolders(userId);
                int numRoles = roles.size();
                for (int roleNum = 0; roleNum < numRoles; roleNum++) {
                    String roleName = roles.keyAt(roleNum);
                    ArraySet<String> holders = roles.valueAt(roleNum);
                    int numHolders = holders.size();
                    int holderNum = 0;
                    while (holderNum < numHolders) {
                        String holderName = holders.valueAt(holderNum);
                        try {
                            pkg = pm.getPackageInfoAsUser(holderName, 0, userId);
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.w(TAG, "Role holder " + holderName + " not found");
                            Binder.restoreCallingIdentity(callingToken);
                            return;
                        }
                        try {
                            StatsLogEventWrapper e2 = new StatsLogEventWrapper(10049, elapsedNanos, wallClockNanos);
                            e2.writeInt(pkg.applicationInfo.uid);
                            e2.writeString(holderName);
                            e2.writeString(roleName);
                            pulledData.add(e2);
                            holderNum++;
                            pm = pm;
                            rmi = rmi;
                        } catch (Throwable th2) {
                            th = th2;
                            Binder.restoreCallingIdentity(callingToken);
                            throw th;
                        }
                    }
                }
            }
            Binder.restoreCallingIdentity(callingToken);
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(callingToken);
            throw th;
        }
    }

    private void pullTimeZoneDataInfo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        String tzDbVersion = "Unknown";
        try {
            tzDbVersion = TimeZone.getTZDataVersion();
        } catch (Exception e) {
            Log.e(TAG, "Getting tzdb version failed: ", e);
        }
        StatsLogEventWrapper e2 = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
        e2.writeString(tzDbVersion);
        pulledData.add(e2);
    }

    private void pullExternalStorageInfo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        int externalStorageType;
        StorageManager storageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        if (storageManager != null) {
            for (VolumeInfo vol : storageManager.getVolumes()) {
                String envState = VolumeInfo.getEnvironmentForState(vol.getState());
                DiskInfo diskInfo = vol.getDisk();
                if (diskInfo != null) {
                    if (envState.equals("mounted")) {
                        int volumeType = 3;
                        if (vol.getType() == 0) {
                            volumeType = 1;
                        } else if (vol.getType() == 1) {
                            volumeType = 2;
                        }
                        if (diskInfo.isSd()) {
                            externalStorageType = 1;
                        } else if (diskInfo.isUsb()) {
                            externalStorageType = 2;
                        } else {
                            externalStorageType = 3;
                        }
                        StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                        e.writeInt(externalStorageType);
                        e.writeInt(volumeType);
                        e.writeLong(diskInfo.size);
                        pulledData.add(e);
                    }
                }
            }
        }
    }

    private void pullAppsOnExternalStorageInfo(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        PackageManager pm = this.mContext.getPackageManager();
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        for (ApplicationInfo appInfo : pm.getInstalledApplications(0)) {
            if (appInfo.storageUuid != null) {
                VolumeInfo volumeInfo = storage.findVolumeByUuid(appInfo.storageUuid.toString());
                if (volumeInfo != null) {
                    DiskInfo diskInfo = volumeInfo.getDisk();
                    if (diskInfo != null) {
                        int externalStorageType = -1;
                        if (diskInfo.isSd()) {
                            externalStorageType = 1;
                        } else if (diskInfo.isUsb()) {
                            externalStorageType = 2;
                        } else if (appInfo.isExternal()) {
                            externalStorageType = 3;
                        }
                        if (externalStorageType != -1) {
                            StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                            e.writeInt(externalStorageType);
                            e.writeString(appInfo.packageName);
                            pulledData.add(e);
                        }
                    }
                }
            }
        }
    }

    private void pullFaceSettings(int tagId, long elapsedNanos, long wallClockNanos, List<StatsLogEventWrapper> pulledData) {
        Throwable th;
        long callingToken = Binder.clearCallingIdentity();
        try {
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            int numUsers = users.size();
            for (int userNum = 0; userNum < numUsers; userNum++) {
                int userId = users.get(userNum).getUserHandle().getIdentifier();
                StatsLogEventWrapper e = new StatsLogEventWrapper(tagId, elapsedNanos, wallClockNanos);
                boolean z = false;
                e.writeBoolean(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_unlock_keyguard_enabled", 1, userId) != 0);
                e.writeBoolean(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_unlock_dismisses_keyguard", 0, userId) != 0);
                e.writeBoolean(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_unlock_attention_required", 1, userId) != 0);
                e.writeBoolean(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_unlock_app_enabled", 1, userId) != 0);
                e.writeBoolean(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_unlock_always_require_confirmation", 0, userId) != 0);
                if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "face_unlock_diversity_required", 1, userId) != 0) {
                    z = true;
                }
                e.writeBoolean(z);
                try {
                    pulledData.add(e);
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(callingToken);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(callingToken);
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(callingToken);
            throw th;
        }
    }

    public StatsLogEventWrapper[] pullData(int tagId) {
        enforceCallingPermission();
        List<StatsLogEventWrapper> ret = new ArrayList<>();
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        long wallClockNanos = SystemClock.currentTimeMicro() * 1000;
        switch (tagId) {
            case 10000:
                pullWifiBytesTransfer(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10001:
                pullWifiBytesTransferByFgBg(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10002:
                pullMobileBytesTransfer(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10003:
                pullMobileBytesTransferByFgBg(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10004:
                pullKernelWakelock(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10005:
            case 10018:
            case 10019:
            case 10020:
            case 10030:
            case 10038:
            case 10043:
            case 10045:
            case 10051:
            case 10054:
            case 10055:
            default:
                Slog.w(TAG, "No such tagId data as " + tagId);
                return null;
            case 10006:
                pullBluetoothBytesTransfer(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10007:
                pullBluetoothActivityInfo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10008:
                pullCpuTimePerFreq(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10009:
                pullKernelUidCpuTime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10010:
                pullKernelUidCpuFreqTime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10011:
                pullWifiActivityInfo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10012:
                pullModemActivityInfo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10013:
                pullProcessMemoryState(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10014:
                pullSystemElapsedRealtime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10015:
                pullSystemUpTime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10016:
                pullKernelUidCpuActiveTime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10017:
                pullKernelUidCpuClusterTime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10021:
                pullTemperature(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10022:
                pullBinderCallsStats(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10023:
                pullBinderCallsStatsExceptions(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10024:
                pullLooperStats(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10025:
                pullDiskStats(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10026:
                pullDirectoryUsage(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10027:
                pullAppSize(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10028:
                pullCategorySize(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10029:
                pullProcessStats(15, tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10031:
                pullNumBiometricsEnrolled(1, tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10032:
                pullDiskIo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10033:
                pullPowerProfile(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10034:
                pullProcessStats(2, tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10035:
                pullProcessCpuTime(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10036:
                pullNativeProcessMemoryState(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10037:
                pullCpuTimePerThreadFreq(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10039:
                pullDeviceCalculatedPowerUse(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10040:
                pullDeviceCalculatedPowerBlameUid(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10041:
                pullDeviceCalculatedPowerBlameOther(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10042:
                pullProcessMemoryHighWaterMark(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10044:
                pullBuildInformation(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10046:
                pullDebugElapsedClock(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10047:
                pullDebugFailingElapsedClock(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10048:
                pullNumBiometricsEnrolled(4, tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10049:
                pullRoleHolders(elapsedNanos, wallClockNanos, ret);
                break;
            case 10050:
                pullDangerousPermissionState(elapsedNanos, wallClockNanos, ret);
                break;
            case 10052:
                pullTimeZoneDataInfo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10053:
                pullExternalStorageInfo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10056:
                pullSystemIonHeapSize(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10057:
                pullAppsOnExternalStorageInfo(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10058:
                pullFaceSettings(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10059:
                pullCoolingDevices(tagId, elapsedNanos, wallClockNanos, ret);
                break;
            case 10060:
                pullAppOps(elapsedNanos, wallClockNanos, ret);
                break;
            case 10061:
                pullProcessSystemIonHeapSize(tagId, elapsedNanos, wallClockNanos, ret);
                break;
        }
        return (StatsLogEventWrapper[]) ret.toArray(new StatsLogEventWrapper[ret.size()]);
    }

    public void statsdReady() {
        enforceCallingPermission();
        sayHiToStatsd();
        this.mContext.sendBroadcastAsUser(new Intent("android.app.action.STATSD_STARTED").addFlags(DumpState.DUMP_SERVICE_PERMISSIONS), UserHandle.SYSTEM, "android.permission.DUMP");
    }

    public void triggerUidSnapshot() {
        enforceCallingPermission();
        synchronized (sStatsdLock) {
            long token = Binder.clearCallingIdentity();
            try {
                informAllUidsLocked(this.mContext);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to trigger uid snapshot.", e);
            } finally {
                restoreCallingIdentity(token);
            }
        }
    }

    private void enforceCallingPermission() {
        if (Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforceCallingPermission("android.permission.STATSCOMPANION", null);
        }
    }

    /* access modifiers changed from: private */
    public static IStatsManager fetchStatsdService() {
        return IStatsManager.Stub.asInterface(ServiceManager.getService("stats"));
    }

    public static final class Lifecycle extends SystemService {
        private StatsCompanionService mStatsCompanionService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.stats.StatsCompanionService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r1v2, types: [com.android.server.stats.StatsCompanionService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mStatsCompanionService = new StatsCompanionService(getContext());
            try {
                publishBinderService("statscompanion", this.mStatsCompanionService);
            } catch (Exception e) {
                Slog.e(StatsCompanionService.TAG, "Failed to publishBinderService", e);
            }
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            super.onBootPhase(phase);
            if (phase == 600) {
                this.mStatsCompanionService.systemReady();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void systemReady() {
        sayHiToStatsd();
    }

    /* JADX INFO: finally extract failed */
    private void sayHiToStatsd() {
        synchronized (sStatsdLock) {
            if (sStatsd != null) {
                Slog.e(TAG, "Trying to fetch statsd, but it was already fetched", new IllegalStateException("sStatsd is not null when being fetched"));
                return;
            }
            sStatsd = fetchStatsdService();
            if (sStatsd == null) {
                Slog.i(TAG, "Could not yet find statsd to tell it that StatsCompanion is alive.");
                return;
            }
            try {
                sStatsd.statsCompanionReady();
                try {
                    sStatsd.asBinder().linkToDeath(new StatsdDeathRecipient(), 0);
                } catch (RemoteException e) {
                    Slog.e(TAG, "linkToDeath(StatsdDeathRecipient) failed", e);
                    forgetEverythingLocked();
                }
                IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_REPLACED");
                filter.addAction("android.intent.action.PACKAGE_ADDED");
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addDataScheme("package");
                this.mContext.registerReceiverAsUser(this.mAppUpdateReceiver, UserHandle.ALL, filter, null, null);
                IntentFilter filter2 = new IntentFilter("android.intent.action.USER_INITIALIZE");
                filter2.addAction("android.intent.action.USER_REMOVED");
                this.mContext.registerReceiverAsUser(this.mUserUpdateReceiver, UserHandle.ALL, filter2, null, null);
                IntentFilter filter3 = new IntentFilter("android.intent.action.REBOOT");
                filter3.addAction("android.intent.action.ACTION_SHUTDOWN");
                this.mContext.registerReceiverAsUser(this.mShutdownEventReceiver, UserHandle.ALL, filter3, null, null);
                long token = Binder.clearCallingIdentity();
                try {
                    informAllUidsLocked(this.mContext);
                    restoreCallingIdentity(token);
                    Slog.i(TAG, "Told statsd that StatsCompanionService is alive.");
                } catch (Throwable th) {
                    restoreCallingIdentity(token);
                    throw th;
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to inform statsd that statscompanion is ready", e2);
                forgetEverythingLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public class StatsdDeathRecipient implements IBinder.DeathRecipient {
        private StatsdDeathRecipient() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Slog.i(StatsCompanionService.TAG, "Statsd is dead - erase all my knowledge.");
            synchronized (StatsCompanionService.sStatsdLock) {
                long now = SystemClock.elapsedRealtime();
                Iterator it = StatsCompanionService.this.mDeathTimeMillis.iterator();
                while (it.hasNext()) {
                    Long timeMillis = (Long) it.next();
                    if (now - timeMillis.longValue() > StatsCompanionService.MILLIS_IN_A_DAY) {
                        StatsCompanionService.this.mDeathTimeMillis.remove(timeMillis);
                    }
                }
                for (Long timeMillis2 : StatsCompanionService.this.mDeletedFiles.keySet()) {
                    if (now - timeMillis2.longValue() > StatsCompanionService.MILLIS_IN_A_DAY * 7) {
                        StatsCompanionService.this.mDeletedFiles.remove(timeMillis2);
                    }
                }
                StatsCompanionService.this.mDeathTimeMillis.add(Long.valueOf(now));
                if (StatsCompanionService.this.mDeathTimeMillis.size() >= 10) {
                    StatsCompanionService.this.mDeathTimeMillis.clear();
                    File[] configs = FileUtils.listFilesOrEmpty(new File(StatsCompanionService.CONFIG_DIR));
                    if (configs.length > 0) {
                        String fileName = configs[0].getName();
                        if (configs[0].delete()) {
                            StatsCompanionService.this.mDeletedFiles.put(Long.valueOf(now), fileName);
                        }
                    }
                }
                StatsCompanionService.this.forgetEverythingLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"StatsCompanionService.sStatsdLock"})
    private void forgetEverythingLocked() {
        sStatsd = null;
        this.mContext.unregisterReceiver(this.mAppUpdateReceiver);
        this.mContext.unregisterReceiver(this.mUserUpdateReceiver);
        this.mContext.unregisterReceiver(this.mShutdownEventReceiver);
        cancelAnomalyAlarm();
        cancelPullingAlarm();
        BinderCallsStatsService.Internal binderStats = (BinderCallsStatsService.Internal) LocalServices.getService(BinderCallsStatsService.Internal.class);
        if (binderStats != null) {
            binderStats.reset();
        }
        LooperStats looperStats = (LooperStats) LocalServices.getService(LooperStats.class);
        if (looperStats != null) {
            looperStats.reset();
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            synchronized (sStatsdLock) {
                writer.println("Number of configuration files deleted: " + this.mDeletedFiles.size());
                if (this.mDeletedFiles.size() > 0) {
                    writer.println("  timestamp, deleted file name");
                }
                long lastBootMillis = SystemClock.currentThreadTimeMillis() - SystemClock.elapsedRealtime();
                for (Long elapsedMillis : this.mDeletedFiles.keySet()) {
                    writer.println("  " + (elapsedMillis.longValue() + lastBootMillis) + ", " + this.mDeletedFiles.get(elapsedMillis));
                }
            }
        }
    }

    private static final class ThermalEventListener extends IThermalEventListener.Stub {
        private ThermalEventListener() {
        }

        public void notifyThrottling(Temperature temp) {
            StatsLog.write(189, temp.getType(), temp.getName(), (int) (temp.getValue() * 10.0f), temp.getStatus());
        }
    }

    private static final class ConnectivityStatsCallback extends ConnectivityManager.NetworkCallback {
        private ConnectivityStatsCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            StatsLog.write(98, network.netId, 1);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            StatsLog.write(98, network.netId, 2);
        }
    }
}
