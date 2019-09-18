package com.android.server.stats;

import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProcessMemoryState;
import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.UidTraffic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.NetworkStats;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.BatteryStatsInternal;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.IStatsCompanionService;
import android.os.IStatsManager;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.StatsDimensionsValue;
import android.os.StatsLogEventWrapper;
import android.os.SynchronousResultReceiver;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.ModemActivityInfo;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.NetworkStatsFactory;
import com.android.internal.os.KernelCpuSpeedReader;
import com.android.internal.os.KernelUidCpuActiveTimeReader;
import com.android.internal.os.KernelUidCpuClusterTimeReader;
import com.android.internal.os.KernelUidCpuFreqTimeReader;
import com.android.internal.os.KernelUidCpuTimeReader;
import com.android.internal.os.KernelWakelockReader;
import com.android.internal.os.KernelWakelockStats;
import com.android.internal.os.PowerProfile;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.DumpState;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StatsCompanionService extends IStatsCompanionService.Stub {
    public static final int CODE_DATA_BROADCAST = 1;
    public static final int CODE_SUBSCRIBER_BROADCAST = 1;
    public static final String CONFIG_DIR = "/data/misc/stats-service";
    public static final int DEATH_THRESHOLD = 10;
    static final boolean DEBUG = false;
    private static final long EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS = 2000;
    public static final String EXTRA_LAST_REPORT_TIME = "android.app.extra.LAST_REPORT_TIME";
    /* access modifiers changed from: private */
    public static final long MILLIS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);
    public static final String RESULT_RECEIVER_CONTROLLER_KEY = "controller_activity";
    static final String TAG = "StatsCompanionService";
    /* access modifiers changed from: private */
    @GuardedBy("sStatsdLock")
    public static IStatsManager sStatsd;
    /* access modifiers changed from: private */
    public static final Object sStatsdLock = new Object();
    private final AlarmManager mAlarmManager;
    private final PendingIntent mAnomalyAlarmIntent;
    private final BroadcastReceiver mAppUpdateReceiver;
    private final Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("sStatsdLock")
    public final HashSet<Long> mDeathTimeMillis = new HashSet<>();
    /* access modifiers changed from: private */
    @GuardedBy("sStatsdLock")
    public final HashMap<Long, String> mDeletedFiles = new HashMap<>();
    private KernelCpuSpeedReader[] mKernelCpuSpeedReaders;
    private KernelUidCpuActiveTimeReader mKernelUidCpuActiveTimeReader = new KernelUidCpuActiveTimeReader();
    private KernelUidCpuClusterTimeReader mKernelUidCpuClusterTimeReader = new KernelUidCpuClusterTimeReader();
    private KernelUidCpuFreqTimeReader mKernelUidCpuFreqTimeReader = new KernelUidCpuFreqTimeReader();
    private KernelUidCpuTimeReader mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
    private final KernelWakelockReader mKernelWakelockReader = new KernelWakelockReader();
    private final PendingIntent mPeriodicAlarmIntent;
    private final PendingIntent mPullingAlarmIntent;
    private final ShutdownEventReceiver mShutdownEventReceiver;
    private final StatFs mStatFsData = new StatFs(Environment.getDataDirectory().getAbsolutePath());
    private final StatFs mStatFsSystem = new StatFs(Environment.getRootDirectory().getAbsolutePath());
    private final StatFs mStatFsTemp = new StatFs(Environment.getDownloadCacheDirectory().getAbsolutePath());
    private TelephonyManager mTelephony = null;
    private final KernelWakelockStats mTmpWakelockStats = new KernelWakelockStats();
    private final BroadcastReceiver mUserUpdateReceiver;
    private IWifiManager mWifiManager = null;

    public static final class AnomalyAlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
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

    private static final class AppUpdateReceiver extends BroadcastReceiver {
        private AppUpdateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
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
                                PackageManager packageManager = context.getPackageManager();
                                StatsCompanionService.sStatsd.informOnePackageRemoved(intent.getData().getSchemeSpecificPart(), uid);
                            }
                        } else {
                            PackageManager pm = context.getPackageManager();
                            int uid2 = intent.getExtras().getInt("android.intent.extra.UID");
                            String app = intent.getData().getSchemeSpecificPart();
                            StatsCompanionService.sStatsd.informOnePackage(app, uid2, pm.getPackageInfo(app, DumpState.DUMP_CHANGES).getLongVersionCode());
                        }
                    } catch (Exception e) {
                        Slog.w(StatsCompanionService.TAG, "Failed to inform statsd of an app update", e);
                    }
                }
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private StatsCompanionService mStatsCompanionService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r1v2, types: [com.android.server.stats.StatsCompanionService, android.os.IBinder] */
        public void onStart() {
            this.mStatsCompanionService = new StatsCompanionService(getContext());
            try {
                publishBinderService("statscompanion", this.mStatsCompanionService);
            } catch (Exception e) {
                Slog.e(StatsCompanionService.TAG, "Failed to publishBinderService", e);
            }
        }

        public void onBootPhase(int phase) {
            super.onBootPhase(phase);
            if (phase == 600) {
                this.mStatsCompanionService.systemReady();
            }
        }
    }

    public static final class PeriodicAlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
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

    public static final class PullingAlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
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

    public static final class ShutdownEventReceiver extends BroadcastReceiver {
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

    private class StatsdDeathRecipient implements IBinder.DeathRecipient {
        private StatsdDeathRecipient() {
        }

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

    public StatsCompanionService(Context context) {
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mAnomalyAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.mContext, AnomalyAlarmReceiver.class), 0);
        this.mPullingAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.mContext, PullingAlarmReceiver.class), 0);
        this.mPeriodicAlarmIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.mContext, PeriodicAlarmReceiver.class), 0);
        this.mAppUpdateReceiver = new AppUpdateReceiver();
        this.mUserUpdateReceiver = new BroadcastReceiver() {
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
        this.mKernelUidCpuFreqTimeReader.setThrottleInterval(0);
        long[] readFreqs = this.mKernelUidCpuFreqTimeReader.readFreqs(powerProfile);
        this.mKernelUidCpuClusterTimeReader.setThrottleInterval(0);
        this.mKernelUidCpuActiveTimeReader.setThrottleInterval(0);
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

    public void sendSubscriberBroadcast(IBinder intentSenderBinder, long configUid, long configKey, long subscriptionId, long subscriptionRuleId, String[] cookies, StatsDimensionsValue dimensionsValue) {
        long j = configUid;
        String[] strArr = cookies;
        enforceCallingPermission();
        IntentSender intentSender = new IntentSender(intentSenderBinder);
        Intent intent = new Intent().putExtra("android.app.extra.STATS_CONFIG_UID", j).putExtra("android.app.extra.STATS_CONFIG_KEY", configKey).putExtra("android.app.extra.STATS_SUBSCRIPTION_ID", subscriptionId).putExtra("android.app.extra.STATS_SUBSCRIPTION_RULE_ID", subscriptionRuleId).putExtra("android.app.extra.STATS_DIMENSIONS_VALUE", dimensionsValue);
        ArrayList<String> cookieList = new ArrayList<>(strArr.length);
        int i = 0;
        for (int length = strArr.length; i < length; length = length) {
            cookieList.add(strArr[i]);
            i++;
        }
        intent.putStringArrayListExtra("android.app.extra.STATS_BROADCAST_SUBSCRIBER_COOKIES", cookieList);
        try {
            ArrayList<String> arrayList = cookieList;
            Intent intent2 = intent;
            try {
                intentSender.sendIntent(this.mContext, 1, intent, null, null);
            } catch (IntentSender.SendIntentException e) {
            }
        } catch (IntentSender.SendIntentException e2) {
            ArrayList<String> arrayList2 = cookieList;
            Intent intent3 = intent;
            Slog.w(TAG, "Unable to send using IntentSender from uid " + j + "; presumably it had been cancelled.");
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
    @GuardedBy("sStatsdLock")
    public final void informAllUidsLocked(Context context) throws RemoteException {
        PackageManager pm = context.getPackageManager();
        List<UserInfo> users = ((UserManager) context.getSystemService("user")).getUsers(true);
        List<Integer> uids = new ArrayList<>();
        List<Long> versions = new ArrayList<>();
        List<String> apps = new ArrayList<>();
        for (UserInfo profile : users) {
            List<PackageInfo> pi = pm.getInstalledPackagesAsUser(4202496, profile.id);
            for (int j = 0; j < pi.size(); j++) {
                if (pi.get(j).applicationInfo != null) {
                    uids.add(Integer.valueOf(pi.get(j).applicationInfo.uid));
                    versions.add(Long.valueOf(pi.get(j).getLongVersionCode()));
                    apps.add(pi.get(j).packageName);
                }
            }
        }
        sStatsd.informAllUidData(toIntArray(uids), toLongArray(versions), (String[]) apps.toArray(new String[apps.size()]));
    }

    public void setAnomalyAlarm(long timestampMs) {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setExact(3, timestampMs, this.mAnomalyAlarmIntent);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void cancelAnomalyAlarm() {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.cancel(this.mAnomalyAlarmIntent);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void setAlarmForSubscriberTriggering(long timestampMs) {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setExact(3, timestampMs, this.mPeriodicAlarmIntent);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void cancelAlarmForSubscriberTriggering() {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.cancel(this.mPeriodicAlarmIntent);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void setPullingAlarm(long nextPullTimeMs) {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.setExact(3, nextPullTimeMs, this.mPullingAlarmIntent);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    public void cancelPullingAlarm() {
        enforceCallingPermission();
        long callingToken = Binder.clearCallingIdentity();
        try {
            this.mAlarmManager.cancel(this.mPullingAlarmIntent);
        } finally {
            Binder.restoreCallingIdentity(callingToken);
        }
    }

    private void addNetworkStats(int tag, List<StatsLogEventWrapper> ret, NetworkStats stats, boolean withFGBG) {
        int size = stats.size();
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        NetworkStats.Entry entry = new NetworkStats.Entry();
        for (int j = 0; j < size; j++) {
            stats.getValues(j, entry);
            StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tag, withFGBG ? 6 : 5);
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
            SynchronousResultReceiver.Result result = receiver.awaitResult(EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS);
            if (result.bundle != null) {
                result.bundle.setDefusable(true);
                T data = result.bundle.getParcelable(RESULT_RECEIVER_CONTROLLER_KEY);
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

    private void pullKernelWakelock(int tagId, List<StatsLogEventWrapper> pulledData) {
        KernelWakelockStats wakelockStats = this.mKernelWakelockReader.readKernelWakelockStats(this.mTmpWakelockStats);
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        for (Map.Entry<String, KernelWakelockStats.Entry> ent : wakelockStats.entrySet()) {
            KernelWakelockStats.Entry kws = ent.getValue();
            StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 4);
            e.writeString(ent.getKey());
            e.writeInt(kws.mCount);
            e.writeInt(kws.mVersion);
            e.writeLong(kws.mTotalTime);
            pulledData.add(e);
        }
    }

    private void pullWifiBytesTransfer(int tagId, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getWifiIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
                return;
            }
            addNetworkStats(tagId, pulledData, new NetworkStatsFactory().readNetworkStatsDetail(-1, ifaces, 0, null).groupedByUid(), false);
            Binder.restoreCallingIdentity(token);
        } catch (IOException e) {
            Slog.e(TAG, "Pulling netstats for wifi bytes has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullWifiBytesTransferByFgBg(int tagId, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getWifiIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
                return;
            }
            addNetworkStats(tagId, pulledData, rollupNetworkStatsByFGBG(new NetworkStatsFactory().readNetworkStatsDetail(-1, ifaces, 0, null)), true);
            Binder.restoreCallingIdentity(token);
        } catch (IOException e) {
            Slog.e(TAG, "Pulling netstats for wifi bytes w/ fg/bg has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullMobileBytesTransfer(int tagId, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getMobileIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
                return;
            }
            addNetworkStats(tagId, pulledData, new NetworkStatsFactory().readNetworkStatsDetail(-1, ifaces, 0, null).groupedByUid(), false);
            Binder.restoreCallingIdentity(token);
        } catch (IOException e) {
            Slog.e(TAG, "Pulling netstats for mobile bytes has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullBluetoothBytesTransfer(int tagId, List<StatsLogEventWrapper> pulledData) {
        BluetoothActivityEnergyInfo info = pullBluetoothData();
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        if (info.getUidTraffic() != null) {
            for (UidTraffic traffic : info.getUidTraffic()) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 3);
                e.writeInt(traffic.getUid());
                e.writeLong(traffic.getRxBytes());
                e.writeLong(traffic.getTxBytes());
                pulledData.add(e);
            }
        }
    }

    private void pullMobileBytesTransferByFgBg(int tagId, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        try {
            String[] ifaces = ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).getMobileIfaces();
            if (ifaces.length == 0) {
                Binder.restoreCallingIdentity(token);
                return;
            }
            addNetworkStats(tagId, pulledData, rollupNetworkStatsByFGBG(new NetworkStatsFactory().readNetworkStatsDetail(-1, ifaces, 0, null)), true);
            Binder.restoreCallingIdentity(token);
        } catch (IOException e) {
            Slog.e(TAG, "Pulling netstats for mobile bytes w/ fg/bg has error", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void pullCpuTimePerFreq(int tagId, List<StatsLogEventWrapper> pulledData) {
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        for (int cluster = 0; cluster < this.mKernelCpuSpeedReaders.length; cluster++) {
            long[] clusterTimeMs = this.mKernelCpuSpeedReaders[cluster].readAbsolute();
            if (clusterTimeMs != null) {
                for (int speed = clusterTimeMs.length - 1; speed >= 0; speed--) {
                    StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 3);
                    e.writeInt(cluster);
                    e.writeInt(speed);
                    e.writeLong(clusterTimeMs[speed]);
                    pulledData.add(e);
                }
            }
        }
    }

    private void pullKernelUidCpuTime(int tagId, List<StatsLogEventWrapper> pulledData) {
        this.mKernelUidCpuTimeReader.readAbsolute(new KernelUidCpuTimeReader.Callback(SystemClock.elapsedRealtimeNanos(), tagId, pulledData) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ List f$2;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r4;
            }

            public final void onUidCpuTime(int i, long j, long j2) {
                StatsCompanionService.lambda$pullKernelUidCpuTime$0(this.f$0, this.f$1, this.f$2, i, j, j2);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuTime$0(long elapsedNanos, int tagId, List pulledData, int uid, long userTimeUs, long systemTimeUs) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 3);
        e.writeInt(uid);
        e.writeLong(userTimeUs);
        e.writeLong(systemTimeUs);
        pulledData.add(e);
    }

    private void pullKernelUidCpuFreqTime(int tagId, List<StatsLogEventWrapper> pulledData) {
        this.mKernelUidCpuFreqTimeReader.readAbsolute(new KernelUidCpuFreqTimeReader.Callback(SystemClock.elapsedRealtimeNanos(), tagId, pulledData) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ List f$2;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r4;
            }

            public final void onUidCpuFreqTime(int i, long[] jArr) {
                StatsCompanionService.lambda$pullKernelUidCpuFreqTime$1(this.f$0, this.f$1, this.f$2, i, jArr);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuFreqTime$1(long elapsedNanos, int tagId, List pulledData, int uid, long[] cpuFreqTimeMs) {
        for (int freqIndex = 0; freqIndex < cpuFreqTimeMs.length; freqIndex++) {
            if (cpuFreqTimeMs[freqIndex] != 0) {
                StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 3);
                e.writeInt(uid);
                e.writeInt(freqIndex);
                e.writeLong(cpuFreqTimeMs[freqIndex]);
                pulledData.add(e);
            }
        }
    }

    private void pullKernelUidCpuClusterTime(int tagId, List<StatsLogEventWrapper> pulledData) {
        this.mKernelUidCpuClusterTimeReader.readAbsolute(new KernelUidCpuClusterTimeReader.Callback(SystemClock.elapsedRealtimeNanos(), tagId, pulledData) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ List f$2;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r4;
            }

            public final void onUidCpuPolicyTime(int i, long[] jArr) {
                StatsCompanionService.lambda$pullKernelUidCpuClusterTime$2(this.f$0, this.f$1, this.f$2, i, jArr);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuClusterTime$2(long elapsedNanos, int tagId, List pulledData, int uid, long[] cpuClusterTimesMs) {
        for (int i = 0; i < cpuClusterTimesMs.length; i++) {
            StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 3);
            e.writeInt(uid);
            e.writeInt(i);
            e.writeLong(cpuClusterTimesMs[i]);
            pulledData.add(e);
        }
    }

    private void pullKernelUidCpuActiveTime(int tagId, List<StatsLogEventWrapper> pulledData) {
        this.mKernelUidCpuActiveTimeReader.readAbsolute(new KernelUidCpuActiveTimeReader.Callback(SystemClock.elapsedRealtimeNanos(), tagId, pulledData) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ List f$2;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r4;
            }

            public final void onUidCpuActiveTime(int i, long j) {
                StatsCompanionService.lambda$pullKernelUidCpuActiveTime$3(this.f$0, this.f$1, this.f$2, i, j);
            }
        });
    }

    static /* synthetic */ void lambda$pullKernelUidCpuActiveTime$3(long elapsedNanos, int tagId, List pulledData, int uid, long cpuActiveTimesMs) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 2);
        e.writeInt(uid);
        e.writeLong(cpuActiveTimesMs);
        pulledData.add(e);
    }

    private void pullWifiActivityInfo(int tagId, List<StatsLogEventWrapper> pulledData) {
        long token = Binder.clearCallingIdentity();
        if (this.mWifiManager == null) {
            this.mWifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
        }
        if (this.mWifiManager != null) {
            try {
                SynchronousResultReceiver wifiReceiver = new SynchronousResultReceiver("wifi");
                this.mWifiManager.requestActivityInfo(wifiReceiver);
                WifiActivityEnergyInfo wifiInfo = awaitControllerInfo(wifiReceiver);
                StatsLogEventWrapper e = new StatsLogEventWrapper(SystemClock.elapsedRealtimeNanos(), tagId, 6);
                e.writeLong(wifiInfo.getTimeStamp());
                e.writeInt(wifiInfo.getStackState());
                e.writeLong(wifiInfo.getControllerTxTimeMillis());
                e.writeLong(wifiInfo.getControllerRxTimeMillis());
                e.writeLong(wifiInfo.getControllerIdleTimeMillis());
                e.writeLong(wifiInfo.getControllerEnergyUsed());
                pulledData.add(e);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Pulling wifiManager for wifi controller activity energy info has error", e2);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    private void pullModemActivityInfo(int tagId, List<StatsLogEventWrapper> pulledData) {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        if (this.mTelephony == null) {
            this.mTelephony = TelephonyManager.from(this.mContext);
        }
        if (this.mTelephony != null) {
            SynchronousResultReceiver modemReceiver = new SynchronousResultReceiver("telephony");
            this.mTelephony.requestModemActivityInfo(modemReceiver);
            ModemActivityInfo modemInfo = awaitControllerInfo(modemReceiver);
            StatsLogEventWrapper e = new StatsLogEventWrapper(SystemClock.elapsedRealtimeNanos(), tagId, 10);
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
        }
    }

    private void pullBluetoothActivityInfo(int tagId, List<StatsLogEventWrapper> pulledData) {
        BluetoothActivityEnergyInfo info = pullBluetoothData();
        StatsLogEventWrapper e = new StatsLogEventWrapper(SystemClock.elapsedRealtimeNanos(), tagId, 6);
        e.writeLong(info.getTimeStamp());
        e.writeInt(info.getBluetoothStackState());
        e.writeLong(info.getControllerTxTimeMillis());
        e.writeLong(info.getControllerRxTimeMillis());
        e.writeLong(info.getControllerIdleTimeMillis());
        e.writeLong(info.getControllerEnergyUsed());
        pulledData.add(e);
    }

    private synchronized BluetoothActivityEnergyInfo pullBluetoothData() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            SynchronousResultReceiver bluetoothReceiver = new SynchronousResultReceiver("bluetooth");
            adapter.requestControllerActivityEnergyInfo(bluetoothReceiver);
            return awaitControllerInfo(bluetoothReceiver);
        }
        Slog.e(TAG, "Failed to get bluetooth adapter!");
        return null;
    }

    private void pullSystemElapsedRealtime(int tagId, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(SystemClock.elapsedRealtimeNanos(), tagId, 1);
        e.writeLong(SystemClock.elapsedRealtime());
        pulledData.add(e);
    }

    private void pullDiskSpace(int tagId, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(SystemClock.elapsedRealtimeNanos(), tagId, 3);
        e.writeLong(this.mStatFsData.getAvailableBytes());
        e.writeLong(this.mStatFsSystem.getAvailableBytes());
        e.writeLong(this.mStatFsTemp.getAvailableBytes());
        pulledData.add(e);
    }

    private void pullSystemUpTime(int tagId, List<StatsLogEventWrapper> pulledData) {
        StatsLogEventWrapper e = new StatsLogEventWrapper(SystemClock.elapsedRealtimeNanos(), tagId, 1);
        e.writeLong(SystemClock.uptimeMillis());
        pulledData.add(e);
    }

    private void pullProcessMemoryState(int tagId, List<StatsLogEventWrapper> pulledData) {
        List<ProcessMemoryState> processMemoryStates = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getMemoryStateForProcesses();
        long elapsedNanos = SystemClock.elapsedRealtimeNanos();
        for (ProcessMemoryState processMemoryState : processMemoryStates) {
            StatsLogEventWrapper e = new StatsLogEventWrapper(elapsedNanos, tagId, 8);
            e.writeInt(processMemoryState.uid);
            e.writeString(processMemoryState.processName);
            e.writeInt(processMemoryState.oomScore);
            e.writeLong(processMemoryState.pgfault);
            e.writeLong(processMemoryState.pgmajfault);
            e.writeLong(processMemoryState.rssInBytes);
            e.writeLong(processMemoryState.cacheInBytes);
            e.writeLong(processMemoryState.swapInBytes);
            pulledData.add(e);
        }
    }

    public StatsLogEventWrapper[] pullData(int tagId) {
        enforceCallingPermission();
        List<StatsLogEventWrapper> ret = new ArrayList<>();
        switch (tagId) {
            case 10000:
                pullWifiBytesTransfer(tagId, ret);
                break;
            case 10001:
                pullWifiBytesTransferByFgBg(tagId, ret);
                break;
            case 10002:
                pullMobileBytesTransfer(tagId, ret);
                break;
            case 10003:
                pullMobileBytesTransferByFgBg(tagId, ret);
                break;
            case 10004:
                pullKernelWakelock(tagId, ret);
                break;
            case 10006:
                pullBluetoothBytesTransfer(tagId, ret);
                break;
            case 10007:
                pullBluetoothActivityInfo(tagId, ret);
                break;
            case 10008:
                pullCpuTimePerFreq(tagId, ret);
                break;
            case 10009:
                pullKernelUidCpuTime(tagId, ret);
                break;
            case 10010:
                pullKernelUidCpuFreqTime(tagId, ret);
                break;
            case 10011:
                pullWifiActivityInfo(tagId, ret);
                break;
            case 10012:
                pullModemActivityInfo(tagId, ret);
                break;
            case 10013:
                pullProcessMemoryState(tagId, ret);
                break;
            case 10014:
                pullSystemElapsedRealtime(tagId, ret);
                break;
            case 10015:
                pullSystemUpTime(tagId, ret);
                break;
            case 10016:
                pullKernelUidCpuActiveTime(tagId, ret);
                break;
            case 10017:
                pullKernelUidCpuClusterTime(tagId, ret);
                break;
            case 10018:
                pullDiskSpace(tagId, ret);
                break;
            default:
                Slog.w(TAG, "No such tagId data as " + tagId);
                return null;
        }
        return (StatsLogEventWrapper[]) ret.toArray(new StatsLogEventWrapper[ret.size()]);
    }

    public void statsdReady() {
        enforceCallingPermission();
        sayHiToStatsd();
        this.mContext.sendBroadcastAsUser(new Intent("android.app.action.STATSD_STARTED").addFlags(DumpState.DUMP_SERVICE_PERMISSIONS), UserHandle.SYSTEM, "android.permission.DUMP");
    }

    /* JADX INFO: finally extract failed */
    public void triggerUidSnapshot() {
        enforceCallingPermission();
        synchronized (sStatsdLock) {
            long token = Binder.clearCallingIdentity();
            try {
                informAllUidsLocked(this.mContext);
                restoreCallingIdentity(token);
            } catch (RemoteException e) {
                try {
                    Slog.e(TAG, "Failed to trigger uid snapshot.", e);
                    restoreCallingIdentity(token);
                } catch (Throwable th) {
                    restoreCallingIdentity(token);
                    throw th;
                }
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

    /* access modifiers changed from: private */
    public void systemReady() {
        sayHiToStatsd();
    }

    private void sayHiToStatsd() {
        long token;
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
                token = Binder.clearCallingIdentity();
                informAllUidsLocked(this.mContext);
                restoreCallingIdentity(token);
                Slog.i(TAG, "Told statsd that StatsCompanionService is alive.");
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to inform statsd that statscompanion is ready", e2);
                forgetEverythingLocked();
            } catch (Throwable th) {
                restoreCallingIdentity(token);
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void forgetEverythingLocked() {
        sStatsd = null;
        this.mContext.unregisterReceiver(this.mAppUpdateReceiver);
        this.mContext.unregisterReceiver(this.mUserUpdateReceiver);
        this.mContext.unregisterReceiver(this.mShutdownEventReceiver);
        cancelAnomalyAlarm();
        cancelPullingAlarm();
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
}
