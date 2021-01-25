package com.android.server.am;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SynchronousResultReceiver;
import android.os.SystemClock;
import android.os.ThreadLocalWorkSource;
import android.telephony.ModemActivityInfo;
import android.telephony.TelephonyManager;
import android.util.IntArray;
import android.util.Slog;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.stats.StatsCompanionService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import libcore.util.EmptyArray;

/* access modifiers changed from: package-private */
public class BatteryExternalStatsWorker implements BatteryStatsImpl.ExternalStatsSync {
    private static final boolean DEBUG = false;
    private static final long EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS = 2000;
    private static final long MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS = 750;
    private static final String TAG = "BatteryExternalStatsWorker";
    @GuardedBy({"this"})
    private Future<?> mBatteryLevelSync;
    private final Context mContext;
    @GuardedBy({"this"})
    private Future<?> mCurrentFuture = null;
    @GuardedBy({"this"})
    private String mCurrentReason = null;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor($$Lambda$BatteryExternalStatsWorker$ML8sXrbYk0MflPvsY2cfCYlcU0w.INSTANCE);
    @GuardedBy({"this"})
    private long mLastCollectionTimeStamp;
    @GuardedBy({"mWorkerLock"})
    private WifiActivityEnergyInfo mLastInfo = new WifiActivityEnergyInfo(0, 0, 0, new long[]{0}, 0, 0, 0, 0);
    @GuardedBy({"this"})
    private boolean mOnBattery;
    @GuardedBy({"this"})
    private boolean mOnBatteryScreenOff;
    private final BatteryStatsImpl mStats;
    private final Runnable mSyncTask = new Runnable() {
        /* class com.android.server.am.BatteryExternalStatsWorker.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            int updateFlags;
            String reason;
            int[] uidsToRemove;
            boolean onBattery;
            boolean onBatteryScreenOff;
            boolean useLatestStates;
            synchronized (BatteryExternalStatsWorker.this) {
                updateFlags = BatteryExternalStatsWorker.this.mUpdateFlags;
                reason = BatteryExternalStatsWorker.this.mCurrentReason;
                uidsToRemove = BatteryExternalStatsWorker.this.mUidsToRemove.size() > 0 ? BatteryExternalStatsWorker.this.mUidsToRemove.toArray() : EmptyArray.INT;
                onBattery = BatteryExternalStatsWorker.this.mOnBattery;
                onBatteryScreenOff = BatteryExternalStatsWorker.this.mOnBatteryScreenOff;
                useLatestStates = BatteryExternalStatsWorker.this.mUseLatestStates;
                BatteryExternalStatsWorker.this.mUpdateFlags = 0;
                BatteryExternalStatsWorker.this.mCurrentReason = null;
                BatteryExternalStatsWorker.this.mUidsToRemove.clear();
                BatteryExternalStatsWorker.this.mCurrentFuture = null;
                BatteryExternalStatsWorker.this.mUseLatestStates = true;
                if ((updateFlags & 31) != 0) {
                    BatteryExternalStatsWorker.this.cancelSyncDueToBatteryLevelChangeLocked();
                }
                if ((updateFlags & 1) != 0) {
                    BatteryExternalStatsWorker.this.cancelCpuSyncDueToWakelockChange();
                }
            }
            try {
                synchronized (BatteryExternalStatsWorker.this.mWorkerLock) {
                    BatteryExternalStatsWorker.this.updateExternalStatsLocked(reason, updateFlags, onBattery, onBatteryScreenOff, useLatestStates);
                }
                if ((updateFlags & 1) != 0) {
                    BatteryExternalStatsWorker.this.mStats.copyFromAllUidsCpuTimes();
                }
                synchronized (BatteryExternalStatsWorker.this.mStats) {
                    for (int uid : uidsToRemove) {
                        StatsLog.write(43, -1, uid, 0);
                        BatteryExternalStatsWorker.this.mStats.removeIsolatedUidLocked(uid);
                    }
                    BatteryExternalStatsWorker.this.mStats.clearPendingRemovedUids();
                }
            } catch (Exception e) {
                Slog.wtf(BatteryExternalStatsWorker.TAG, "Error updating external stats: ", e);
            }
            synchronized (BatteryExternalStatsWorker.this) {
                BatteryExternalStatsWorker.this.mLastCollectionTimeStamp = SystemClock.elapsedRealtime();
            }
        }
    };
    @GuardedBy({"mWorkerLock"})
    private TelephonyManager mTelephony = null;
    @GuardedBy({"this"})
    private final IntArray mUidsToRemove = new IntArray();
    @GuardedBy({"this"})
    private int mUpdateFlags = 0;
    @GuardedBy({"this"})
    private boolean mUseLatestStates = true;
    @GuardedBy({"this"})
    private Future<?> mWakelockChangesUpdate;
    @GuardedBy({"mWorkerLock"})
    private IWifiManager mWifiManager = null;
    private final Object mWorkerLock = new Object();
    private final Runnable mWriteTask = new Runnable() {
        /* class com.android.server.am.BatteryExternalStatsWorker.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (BatteryExternalStatsWorker.this.mStats) {
                BatteryExternalStatsWorker.this.mStats.writeAsyncLocked();
            }
        }
    };

    static /* synthetic */ Thread lambda$new$1(Runnable r) {
        Thread t = new Thread(new Runnable(r) {
            /* class com.android.server.am.$$Lambda$BatteryExternalStatsWorker$ddVY5lmqswnSjXppAxPTOHbuzzQ */
            private final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BatteryExternalStatsWorker.lambda$new$0(this.f$0);
            }
        }, "batterystats-worker");
        t.setPriority(5);
        return t;
    }

    static /* synthetic */ void lambda$new$0(Runnable r) {
        ThreadLocalWorkSource.setUid(Process.myUid());
        r.run();
    }

    BatteryExternalStatsWorker(Context context, BatteryStatsImpl stats) {
        this.mContext = context;
        this.mStats = stats;
    }

    public synchronized Future<?> scheduleSync(String reason, int flags) {
        return scheduleSyncLocked(reason, flags);
    }

    public synchronized Future<?> scheduleCpuSyncDueToRemovedUid(int uid) {
        this.mUidsToRemove.add(uid);
        return scheduleSyncLocked("remove-uid", 1);
    }

    public synchronized Future<?> scheduleCpuSyncDueToSettingChange() {
        return scheduleSyncLocked("setting-change", 1);
    }

    public Future<?> scheduleReadProcStateCpuTimes(boolean onBattery, boolean onBatteryScreenOff, long delayMillis) {
        synchronized (this.mStats) {
            if (!this.mStats.trackPerProcStateCpuTimes()) {
                return null;
            }
        }
        synchronized (this) {
            if (this.mExecutorService.isShutdown()) {
                return null;
            }
            return this.mExecutorService.schedule((Runnable) PooledLambda.obtainRunnable($$Lambda$cC4f0pNQX9_D9f8AXLmKk2sArGY.INSTANCE, this.mStats, Boolean.valueOf(onBattery), Boolean.valueOf(onBatteryScreenOff)).recycleOnUse(), delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    public Future<?> scheduleCopyFromAllUidsCpuTimes(boolean onBattery, boolean onBatteryScreenOff) {
        synchronized (this.mStats) {
            if (!this.mStats.trackPerProcStateCpuTimes()) {
                return null;
            }
        }
        synchronized (this) {
            if (this.mExecutorService.isShutdown()) {
                return null;
            }
            return this.mExecutorService.submit((Runnable) PooledLambda.obtainRunnable($$Lambda$7toxTvZDSEytL0rCkoEfGilPDWM.INSTANCE, this.mStats, Boolean.valueOf(onBattery), Boolean.valueOf(onBatteryScreenOff)).recycleOnUse());
        }
    }

    public Future<?> scheduleCpuSyncDueToScreenStateChange(boolean onBattery, boolean onBatteryScreenOff) {
        Future<?> scheduleSyncLocked;
        synchronized (this) {
            if (this.mCurrentFuture == null || (this.mUpdateFlags & 1) == 0) {
                this.mOnBattery = onBattery;
                this.mOnBatteryScreenOff = onBatteryScreenOff;
                this.mUseLatestStates = false;
            }
            scheduleSyncLocked = scheduleSyncLocked("screen-state", 1);
        }
        return scheduleSyncLocked;
    }

    public Future<?> scheduleCpuSyncDueToWakelockChange(long delayMillis) {
        Future<?> future;
        synchronized (this) {
            this.mWakelockChangesUpdate = scheduleDelayedSyncLocked(this.mWakelockChangesUpdate, new Runnable() {
                /* class com.android.server.am.$$Lambda$BatteryExternalStatsWorker$r3x3xYmhrLG8kgeNVPXl5EILHwU */

                @Override // java.lang.Runnable
                public final void run() {
                    BatteryExternalStatsWorker.this.lambda$scheduleCpuSyncDueToWakelockChange$3$BatteryExternalStatsWorker();
                }
            }, delayMillis);
            future = this.mWakelockChangesUpdate;
        }
        return future;
    }

    public /* synthetic */ void lambda$scheduleCpuSyncDueToWakelockChange$3$BatteryExternalStatsWorker() {
        scheduleSync("wakelock-change", 1);
        scheduleRunnable(new Runnable() {
            /* class com.android.server.am.$$Lambda$BatteryExternalStatsWorker$PpNEY15dspg9oLlkg1OsyjrPTqw */

            @Override // java.lang.Runnable
            public final void run() {
                BatteryExternalStatsWorker.this.lambda$scheduleCpuSyncDueToWakelockChange$2$BatteryExternalStatsWorker();
            }
        });
    }

    public /* synthetic */ void lambda$scheduleCpuSyncDueToWakelockChange$2$BatteryExternalStatsWorker() {
        this.mStats.postBatteryNeedsCpuUpdateMsg();
    }

    public void cancelCpuSyncDueToWakelockChange() {
        synchronized (this) {
            if (this.mWakelockChangesUpdate != null) {
                this.mWakelockChangesUpdate.cancel(false);
                this.mWakelockChangesUpdate = null;
            }
        }
    }

    public Future<?> scheduleSyncDueToBatteryLevelChange(long delayMillis) {
        Future<?> future;
        synchronized (this) {
            this.mBatteryLevelSync = scheduleDelayedSyncLocked(this.mBatteryLevelSync, new Runnable() {
                /* class com.android.server.am.$$Lambda$BatteryExternalStatsWorker$xR3yCbbVfCo3oq_xPiH7j5l5uac */

                @Override // java.lang.Runnable
                public final void run() {
                    BatteryExternalStatsWorker.this.lambda$scheduleSyncDueToBatteryLevelChange$4$BatteryExternalStatsWorker();
                }
            }, delayMillis);
            future = this.mBatteryLevelSync;
        }
        return future;
    }

    public /* synthetic */ void lambda$scheduleSyncDueToBatteryLevelChange$4$BatteryExternalStatsWorker() {
        scheduleSync("battery-level", 31);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"this"})
    private void cancelSyncDueToBatteryLevelChangeLocked() {
        Future<?> future = this.mBatteryLevelSync;
        if (future != null) {
            future.cancel(false);
            this.mBatteryLevelSync = null;
        }
    }

    @GuardedBy({"this"})
    private Future<?> scheduleDelayedSyncLocked(Future<?> lastScheduledSync, Runnable syncRunnable, long delayMillis) {
        if (this.mExecutorService.isShutdown()) {
            return CompletableFuture.failedFuture(new IllegalStateException("worker shutdown"));
        }
        if (lastScheduledSync != null) {
            if (delayMillis != 0) {
                return lastScheduledSync;
            }
            lastScheduledSync.cancel(false);
        }
        return this.mExecutorService.schedule(syncRunnable, delayMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized Future<?> scheduleWrite() {
        if (this.mExecutorService.isShutdown()) {
            return CompletableFuture.failedFuture(new IllegalStateException("worker shutdown"));
        }
        scheduleSyncLocked("write", 31);
        return this.mExecutorService.submit(this.mWriteTask);
    }

    public synchronized void scheduleRunnable(Runnable runnable) {
        if (!this.mExecutorService.isShutdown()) {
            this.mExecutorService.submit(runnable);
        }
    }

    public void shutdown() {
        this.mExecutorService.shutdownNow();
    }

    @GuardedBy({"this"})
    private Future<?> scheduleSyncLocked(String reason, int flags) {
        if (this.mExecutorService.isShutdown()) {
            return CompletableFuture.failedFuture(new IllegalStateException("worker shutdown"));
        }
        if (this.mCurrentFuture == null) {
            this.mUpdateFlags = flags;
            this.mCurrentReason = reason;
            this.mCurrentFuture = this.mExecutorService.submit(this.mSyncTask);
        }
        this.mUpdateFlags |= flags;
        return this.mCurrentFuture;
    }

    /* access modifiers changed from: package-private */
    public long getLastCollectionTimeStamp() {
        long j;
        synchronized (this) {
            j = this.mLastCollectionTimeStamp;
        }
        return j;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mWorkerLock"})
    private void updateExternalStatsLocked(String reason, int updateFlags, boolean onBattery, boolean onBatteryScreenOff, boolean useLatestStates) {
        Throwable th;
        boolean onBatteryScreenOff2;
        boolean onBattery2;
        BluetoothAdapter adapter;
        SynchronousResultReceiver wifiReceiver;
        SynchronousResultReceiver wifiReceiver2 = null;
        SynchronousResultReceiver bluetoothReceiver = null;
        SynchronousResultReceiver modemReceiver = null;
        boolean railUpdated = false;
        if ((updateFlags & 2) != 0) {
            if (this.mWifiManager == null) {
                this.mWifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
            }
            IWifiManager iWifiManager = this.mWifiManager;
            if (iWifiManager != null) {
                try {
                    if ((iWifiManager.getSupportedFeatures() & 65536) != 0) {
                        wifiReceiver2 = new SynchronousResultReceiver("wifi");
                        this.mWifiManager.requestActivityInfo(wifiReceiver2);
                    }
                    wifiReceiver = wifiReceiver2;
                } catch (RemoteException e) {
                    wifiReceiver = null;
                }
            } else {
                wifiReceiver = null;
            }
            synchronized (this.mStats) {
                this.mStats.updateRailStatsLocked();
            }
            railUpdated = true;
            wifiReceiver2 = wifiReceiver;
        }
        if (!((updateFlags & 8) == 0 || (adapter = BluetoothAdapter.getDefaultAdapter()) == null)) {
            bluetoothReceiver = new SynchronousResultReceiver("bluetooth");
            adapter.requestControllerActivityEnergyInfo(bluetoothReceiver);
        }
        if ((updateFlags & 4) != 0) {
            if (this.mTelephony == null) {
                this.mTelephony = TelephonyManager.from(this.mContext);
            }
            if (this.mTelephony != null) {
                modemReceiver = new SynchronousResultReceiver("telephony");
                this.mTelephony.requestModemActivityInfo(modemReceiver);
            }
            if (!railUpdated) {
                synchronized (this.mStats) {
                    this.mStats.updateRailStatsLocked();
                }
            }
        }
        WifiActivityEnergyInfo wifiInfo = (WifiActivityEnergyInfo) awaitControllerInfo(wifiReceiver2);
        BluetoothActivityEnergyInfo bluetoothInfo = awaitControllerInfo(bluetoothReceiver);
        ModemActivityInfo modemInfo = awaitControllerInfo(modemReceiver);
        synchronized (this.mStats) {
            try {
                this.mStats.addHistoryEventLocked(SystemClock.elapsedRealtime(), SystemClock.uptimeMillis(), 14, reason, 0);
                if ((updateFlags & 1) != 0) {
                    if (useLatestStates) {
                        onBattery2 = this.mStats.isOnBatteryLocked();
                        try {
                            onBatteryScreenOff2 = this.mStats.isOnBatteryScreenOffLocked();
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        onBattery2 = onBattery;
                        onBatteryScreenOff2 = onBatteryScreenOff;
                    }
                    try {
                        this.mStats.updateCpuTimeLocked(onBattery2, onBatteryScreenOff2);
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                if ((updateFlags & 31) != 0) {
                    this.mStats.updateKernelWakelocksLocked();
                    this.mStats.updateKernelMemoryBandwidthLocked();
                }
                if ((updateFlags & 16) != 0) {
                    this.mStats.updateRpmStatsLocked();
                }
                if (bluetoothInfo != null) {
                    if (bluetoothInfo.isValid()) {
                        this.mStats.updateBluetoothStateLocked(bluetoothInfo);
                    } else {
                        Slog.w(TAG, "bluetooth info is invalid: " + bluetoothInfo);
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
        if (wifiInfo != null) {
            if (wifiInfo.isValid()) {
                this.mStats.updateWifiState(extractDeltaLocked(wifiInfo));
            } else {
                Slog.w(TAG, "wifi info is invalid: " + wifiInfo);
            }
        }
        if (modemInfo == null) {
            return;
        }
        if (modemInfo.isValid()) {
            this.mStats.updateMobileRadioState(modemInfo);
            return;
        }
        Slog.w(TAG, "modem info is invalid: " + modemInfo);
    }

    private static <T extends Parcelable> T awaitControllerInfo(SynchronousResultReceiver receiver) {
        if (receiver == null) {
            return null;
        }
        try {
            SynchronousResultReceiver.Result result = receiver.awaitResult((long) EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS);
            if (result.bundle != null) {
                result.bundle.setDefusable(true);
                T data = (T) result.bundle.getParcelable(StatsCompanionService.RESULT_RECEIVER_CONTROLLER_KEY);
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

    /* JADX INFO: Multiple debug info for r8v7 long: [D('idleTimeMs' long), D('totalActiveTimeMs' long)] */
    @GuardedBy({"mWorkerLock"})
    private WifiActivityEnergyInfo extractDeltaLocked(WifiActivityEnergyInfo latest) {
        WifiActivityEnergyInfo delta;
        long rxTimeMs;
        long scanTimeMs;
        long maxExpectedIdleTimeMs;
        long timePeriodMs = latest.mTimestamp - this.mLastInfo.mTimestamp;
        long lastScanMs = this.mLastInfo.mControllerScanTimeMs;
        long lastIdleMs = this.mLastInfo.mControllerIdleTimeMs;
        long lastTxMs = this.mLastInfo.mControllerTxTimeMs;
        long lastRxMs = this.mLastInfo.mControllerRxTimeMs;
        long lastEnergy = this.mLastInfo.mControllerEnergyUsed;
        WifiActivityEnergyInfo delta2 = this.mLastInfo;
        delta2.mTimestamp = latest.getTimeStamp();
        delta2.mStackState = latest.getStackState();
        long txTimeMs = latest.mControllerTxTimeMs - lastTxMs;
        long rxTimeMs2 = latest.mControllerRxTimeMs - lastRxMs;
        long idleTimeMs = latest.mControllerIdleTimeMs - lastIdleMs;
        long scanTimeMs2 = latest.mControllerScanTimeMs - lastScanMs;
        if (txTimeMs < 0 || rxTimeMs2 < 0 || scanTimeMs2 < 0) {
            delta = delta2;
        } else if (idleTimeMs < 0) {
            delta = delta2;
        } else {
            long totalActiveTimeMs = txTimeMs + rxTimeMs2;
            if (totalActiveTimeMs > timePeriodMs) {
                if (totalActiveTimeMs > timePeriodMs + MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Total Active time ");
                    TimeUtils.formatDuration(totalActiveTimeMs, sb);
                    sb.append(" is longer than sample period ");
                    TimeUtils.formatDuration(timePeriodMs, sb);
                    sb.append(".\n");
                    sb.append("Previous WiFi snapshot: ");
                    sb.append("idle=");
                    TimeUtils.formatDuration(lastIdleMs, sb);
                    sb.append(" rx=");
                    scanTimeMs = scanTimeMs2;
                    TimeUtils.formatDuration(lastRxMs, sb);
                    sb.append(" tx=");
                    TimeUtils.formatDuration(lastTxMs, sb);
                    sb.append(" e=");
                    rxTimeMs = rxTimeMs2;
                    sb.append(lastEnergy);
                    sb.append("\n");
                    sb.append("Current WiFi snapshot: ");
                    sb.append("idle=");
                    TimeUtils.formatDuration(latest.mControllerIdleTimeMs, sb);
                    sb.append(" rx=");
                    TimeUtils.formatDuration(latest.mControllerRxTimeMs, sb);
                    sb.append(" tx=");
                    TimeUtils.formatDuration(latest.mControllerTxTimeMs, sb);
                    sb.append(" e=");
                    sb.append(latest.mControllerEnergyUsed);
                    Slog.wtf(TAG, sb.toString());
                } else {
                    scanTimeMs = scanTimeMs2;
                    rxTimeMs = rxTimeMs2;
                }
                maxExpectedIdleTimeMs = 0;
            } else {
                scanTimeMs = scanTimeMs2;
                rxTimeMs = rxTimeMs2;
                maxExpectedIdleTimeMs = timePeriodMs - totalActiveTimeMs;
            }
            delta = delta2;
            delta.mControllerTxTimeMs = txTimeMs;
            delta.mControllerRxTimeMs = rxTimeMs;
            delta.mControllerScanTimeMs = scanTimeMs;
            delta.mControllerIdleTimeMs = Math.min(maxExpectedIdleTimeMs, Math.max(0L, idleTimeMs));
            delta.mControllerEnergyUsed = Math.max(0L, latest.mControllerEnergyUsed - lastEnergy);
            this.mLastInfo = latest;
            return delta;
        }
        if (latest.mControllerTxTimeMs + latest.mControllerRxTimeMs + latest.mControllerIdleTimeMs <= timePeriodMs + MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS) {
            delta.mControllerEnergyUsed = latest.mControllerEnergyUsed;
            delta.mControllerRxTimeMs = latest.mControllerRxTimeMs;
            delta.mControllerTxTimeMs = latest.mControllerTxTimeMs;
            delta.mControllerIdleTimeMs = latest.mControllerIdleTimeMs;
            delta.mControllerScanTimeMs = latest.mControllerScanTimeMs;
        } else {
            delta.mControllerEnergyUsed = 0;
            delta.mControllerRxTimeMs = 0;
            delta.mControllerTxTimeMs = 0;
            delta.mControllerIdleTimeMs = 0;
            delta.mControllerScanTimeMs = 0;
        }
        Slog.v(TAG, "WiFi energy data was reset, new WiFi energy data is " + delta);
        this.mLastInfo = latest;
        return delta;
    }
}
