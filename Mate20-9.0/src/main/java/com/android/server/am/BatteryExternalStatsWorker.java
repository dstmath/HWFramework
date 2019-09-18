package com.android.server.am;

import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiActivityEnergyInfo;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SynchronousResultReceiver;
import android.os.SystemClock;
import android.telephony.ModemActivityInfo;
import android.telephony.TelephonyManager;
import android.util.IntArray;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.stats.StatsCompanionService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import libcore.util.EmptyArray;

class BatteryExternalStatsWorker implements BatteryStatsImpl.ExternalStatsSync {
    private static final boolean DEBUG = false;
    private static final long EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS = 2000;
    private static final long MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS = 750;
    private static final String TAG = "BatteryExternalStatsWorker";
    @GuardedBy("this")
    private Future<?> mBatteryLevelSync;
    private final Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public Future<?> mCurrentFuture = null;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public String mCurrentReason = null;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor($$Lambda$BatteryExternalStatsWorker$y4b5S_CLdUbDV0ejaQDagLXGZRg.INSTANCE);
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long mLastCollectionTimeStamp;
    @GuardedBy("mWorkerLock")
    private WifiActivityEnergyInfo mLastInfo;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public boolean mOnBattery;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public boolean mOnBatteryScreenOff;
    /* access modifiers changed from: private */
    public final BatteryStatsImpl mStats;
    private final Runnable mSyncTask;
    @GuardedBy("mWorkerLock")
    private TelephonyManager mTelephony = null;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public final IntArray mUidsToRemove = new IntArray();
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public int mUpdateFlags = 0;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public boolean mUseLatestStates = true;
    @GuardedBy("this")
    private Future<?> mWakelockChangesUpdate;
    @GuardedBy("mWorkerLock")
    private IWifiManager mWifiManager = null;
    /* access modifiers changed from: private */
    public final Object mWorkerLock = new Object();
    private final Runnable mWriteTask;

    static /* synthetic */ Thread lambda$new$0(Runnable r) {
        Thread t = new Thread(r, "batterystats-worker");
        t.setPriority(5);
        return t;
    }

    BatteryExternalStatsWorker(Context context, BatteryStatsImpl stats) {
        WifiActivityEnergyInfo wifiActivityEnergyInfo = new WifiActivityEnergyInfo(0, 0, 0, new long[]{0}, 0, 0, 0, 0);
        this.mLastInfo = wifiActivityEnergyInfo;
        this.mSyncTask = new Runnable() {
            public void run() {
                int updateFlags;
                String reason;
                int[] uidsToRemove;
                boolean onBattery;
                boolean onBatteryScreenOff;
                boolean useLatestStates;
                int i;
                synchronized (BatteryExternalStatsWorker.this) {
                    updateFlags = BatteryExternalStatsWorker.this.mUpdateFlags;
                    reason = BatteryExternalStatsWorker.this.mCurrentReason;
                    uidsToRemove = BatteryExternalStatsWorker.this.mUidsToRemove.size() > 0 ? BatteryExternalStatsWorker.this.mUidsToRemove.toArray() : EmptyArray.INT;
                    onBattery = BatteryExternalStatsWorker.this.mOnBattery;
                    onBatteryScreenOff = BatteryExternalStatsWorker.this.mOnBatteryScreenOff;
                    useLatestStates = BatteryExternalStatsWorker.this.mUseLatestStates;
                    int unused = BatteryExternalStatsWorker.this.mUpdateFlags = 0;
                    String unused2 = BatteryExternalStatsWorker.this.mCurrentReason = null;
                    BatteryExternalStatsWorker.this.mUidsToRemove.clear();
                    Future unused3 = BatteryExternalStatsWorker.this.mCurrentFuture = null;
                    boolean unused4 = BatteryExternalStatsWorker.this.mUseLatestStates = true;
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
                            BatteryExternalStatsWorker.this.mStats.removeIsolatedUidLocked(uid);
                        }
                        BatteryExternalStatsWorker.this.mStats.clearPendingRemovedUids();
                    }
                } catch (Exception e) {
                    Slog.wtf(BatteryExternalStatsWorker.TAG, "Error updating external stats: ", e);
                }
                synchronized (BatteryExternalStatsWorker.this) {
                    long unused5 = BatteryExternalStatsWorker.this.mLastCollectionTimeStamp = SystemClock.elapsedRealtime();
                }
            }
        };
        this.mWriteTask = new Runnable() {
            public void run() {
                synchronized (BatteryExternalStatsWorker.this.mStats) {
                    BatteryExternalStatsWorker.this.mStats.writeAsyncLocked();
                }
            }
        };
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        if (r5.mExecutorService.isShutdown() != false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        r0 = r5.mExecutorService.schedule(com.android.internal.util.function.pooled.PooledLambda.obtainRunnable(com.android.server.am.$$Lambda$BatteryExternalStatsWorker$cC4f0pNQX9_D9f8AXLmKk2sArGY.INSTANCE, r5.mStats, java.lang.Boolean.valueOf(r6), java.lang.Boolean.valueOf(r7)).recycleOnUse(), r8, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0034, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0035, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0036, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        monitor-enter(r5);
     */
    public Future<?> scheduleReadProcStateCpuTimes(boolean onBattery, boolean onBatteryScreenOff, long delayMillis) {
        synchronized (this.mStats) {
            if (!this.mStats.trackPerProcStateCpuTimes()) {
                return null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        if (r5.mExecutorService.isShutdown() != false) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        r0 = r5.mExecutorService.submit(com.android.internal.util.function.pooled.PooledLambda.obtainRunnable(com.android.server.am.$$Lambda$BatteryExternalStatsWorker$7toxTvZDSEytL0rCkoEfGilPDWM.INSTANCE, r5.mStats, java.lang.Boolean.valueOf(r6), java.lang.Boolean.valueOf(r7)).recycleOnUse());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0034, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        monitor-enter(r5);
     */
    public Future<?> scheduleCopyFromAllUidsCpuTimes(boolean onBattery, boolean onBatteryScreenOff) {
        synchronized (this.mStats) {
            if (!this.mStats.trackPerProcStateCpuTimes()) {
                return null;
            }
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
                public final void run() {
                    BatteryExternalStatsWorker.lambda$scheduleCpuSyncDueToWakelockChange$2(BatteryExternalStatsWorker.this);
                }
            }, delayMillis);
            future = this.mWakelockChangesUpdate;
        }
        return future;
    }

    public static /* synthetic */ void lambda$scheduleCpuSyncDueToWakelockChange$2(BatteryExternalStatsWorker batteryExternalStatsWorker) {
        batteryExternalStatsWorker.scheduleSync("wakelock-change", 1);
        batteryExternalStatsWorker.scheduleRunnable(new Runnable() {
            public final void run() {
                BatteryExternalStatsWorker.this.mStats.postBatteryNeedsCpuUpdateMsg();
            }
        });
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
                public final void run() {
                    BatteryExternalStatsWorker.this.scheduleSync("battery-level", 31);
                }
            }, delayMillis);
            future = this.mBatteryLevelSync;
        }
        return future;
    }

    /* access modifiers changed from: private */
    @GuardedBy("this")
    public void cancelSyncDueToBatteryLevelChangeLocked() {
        if (this.mBatteryLevelSync != null) {
            this.mBatteryLevelSync.cancel(false);
            this.mBatteryLevelSync = null;
        }
    }

    @GuardedBy("this")
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

    @GuardedBy("this")
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
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00f2, code lost:
        if (r5 == null) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00f8, code lost:
        if (r5.isValid() == false) goto L_0x0104;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00fa, code lost:
        r1.mStats.updateWifiState(extractDeltaLocked(r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0104, code lost:
        android.util.Slog.w(TAG, "wifi info is invalid: " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x011b, code lost:
        if (r7 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0121, code lost:
        if (r7.isValid() == false) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0123, code lost:
        r1.mStats.updateMobileRadioState(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0129, code lost:
        android.util.Slog.w(TAG, "modem info is invalid: " + r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        return;
     */
    @GuardedBy("mWorkerLock")
    public void updateExternalStatsLocked(String reason, int updateFlags, boolean onBattery, boolean onBatteryScreenOff, boolean useLatestStates) {
        boolean onBatteryScreenOff2;
        boolean onBattery2;
        SynchronousResultReceiver wifiReceiver = null;
        SynchronousResultReceiver bluetoothReceiver = null;
        SynchronousResultReceiver modemReceiver = null;
        if ((updateFlags & 2) != 0) {
            if (this.mWifiManager == null) {
                this.mWifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
            }
            if (this.mWifiManager != null) {
                try {
                    wifiReceiver = new SynchronousResultReceiver("wifi");
                    this.mWifiManager.requestActivityInfo(wifiReceiver);
                } catch (RemoteException e) {
                }
            }
        }
        if ((updateFlags & 8) != 0) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                bluetoothReceiver = new SynchronousResultReceiver("bluetooth");
                adapter.requestControllerActivityEnergyInfo(bluetoothReceiver);
            }
        }
        if ((updateFlags & 4) != 0) {
            if (this.mTelephony == null) {
                this.mTelephony = TelephonyManager.from(this.mContext);
            }
            if (this.mTelephony != null) {
                modemReceiver = new SynchronousResultReceiver("telephony");
                this.mTelephony.requestModemActivityInfo(modemReceiver);
            }
        }
        WifiActivityEnergyInfo wifiInfo = awaitControllerInfo(wifiReceiver);
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
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } else {
                        onBattery2 = onBattery;
                        onBatteryScreenOff2 = onBatteryScreenOff;
                    }
                    try {
                        this.mStats.updateCpuTimeLocked(onBattery2, onBatteryScreenOff2);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } else {
                    boolean z = onBatteryScreenOff;
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
            } catch (Throwable th3) {
                th = th3;
                boolean z2 = onBattery;
                throw th;
            }
        }
    }

    private static <T extends Parcelable> T awaitControllerInfo(SynchronousResultReceiver receiver) {
        if (receiver == null) {
            return null;
        }
        try {
            SynchronousResultReceiver.Result result = receiver.awaitResult(EXTERNAL_STATS_SYNC_TIMEOUT_MILLIS);
            if (result.bundle != null) {
                result.bundle.setDefusable(true);
                T data = result.bundle.getParcelable(StatsCompanionService.RESULT_RECEIVER_CONTROLLER_KEY);
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

    @GuardedBy("mWorkerLock")
    private WifiActivityEnergyInfo extractDeltaLocked(WifiActivityEnergyInfo latest) {
        WifiActivityEnergyInfo delta;
        long scanTimeMs;
        long maxExpectedIdleTimeMs;
        long lastEnergy;
        WifiActivityEnergyInfo wifiActivityEnergyInfo = latest;
        long timePeriodMs = wifiActivityEnergyInfo.mTimestamp - this.mLastInfo.mTimestamp;
        long lastScanMs = this.mLastInfo.mControllerScanTimeMs;
        long lastIdleMs = this.mLastInfo.mControllerIdleTimeMs;
        long lastTxMs = this.mLastInfo.mControllerTxTimeMs;
        long lastRxMs = this.mLastInfo.mControllerRxTimeMs;
        long lastEnergy2 = this.mLastInfo.mControllerEnergyUsed;
        WifiActivityEnergyInfo delta2 = this.mLastInfo;
        long lastEnergy3 = lastEnergy2;
        delta2.mTimestamp = latest.getTimeStamp();
        delta2.mStackState = latest.getStackState();
        long txTimeMs = wifiActivityEnergyInfo.mControllerTxTimeMs - lastTxMs;
        WifiActivityEnergyInfo delta3 = delta2;
        long lastEnergy4 = lastEnergy3;
        long rxTimeMs = wifiActivityEnergyInfo.mControllerRxTimeMs - lastRxMs;
        long lastTxMs2 = lastTxMs;
        long idleTimeMs = wifiActivityEnergyInfo.mControllerIdleTimeMs - lastIdleMs;
        long scanTimeMs2 = wifiActivityEnergyInfo.mControllerScanTimeMs - lastScanMs;
        long j = lastScanMs;
        if (txTimeMs < 0 || rxTimeMs < 0) {
            long j2 = lastIdleMs;
            long j3 = scanTimeMs2;
            long j4 = lastRxMs;
            delta = delta3;
            long lastIdleMs2 = lastEnergy4;
            long j5 = lastTxMs2;
            long timePeriodMs2 = idleTimeMs;
        } else if (scanTimeMs2 < 0) {
            long j6 = timePeriodMs;
            long j7 = lastIdleMs;
            long j8 = scanTimeMs2;
            long j9 = lastRxMs;
            delta = delta3;
            long lastIdleMs3 = lastEnergy4;
            long j10 = lastTxMs2;
            long timePeriodMs3 = idleTimeMs;
        } else {
            long totalActiveTimeMs = txTimeMs + rxTimeMs;
            if (totalActiveTimeMs > timePeriodMs) {
                maxExpectedIdleTimeMs = 0;
                if (totalActiveTimeMs > timePeriodMs + MAX_WIFI_STATS_SAMPLE_ERROR_MILLIS) {
                    StringBuilder sb = new StringBuilder();
                    scanTimeMs = scanTimeMs2;
                    sb.append("Total Active time ");
                    TimeUtils.formatDuration(totalActiveTimeMs, sb);
                    sb.append(" is longer than sample period ");
                    TimeUtils.formatDuration(timePeriodMs, sb);
                    sb.append(".\n");
                    sb.append("Previous WiFi snapshot: ");
                    sb.append("idle=");
                    TimeUtils.formatDuration(lastIdleMs, sb);
                    sb.append(" rx=");
                    TimeUtils.formatDuration(lastRxMs, sb);
                    sb.append(" tx=");
                    long lastTxMs3 = lastTxMs2;
                    TimeUtils.formatDuration(lastTxMs3, sb);
                    long j11 = lastIdleMs;
                    sb.append(" e=");
                    lastEnergy = lastEnergy4;
                    sb.append(lastEnergy);
                    long j12 = lastTxMs3;
                    sb.append("\n");
                    sb.append("Current WiFi snapshot: ");
                    sb.append("idle=");
                    TimeUtils.formatDuration(wifiActivityEnergyInfo.mControllerIdleTimeMs, sb);
                    sb.append(" rx=");
                    TimeUtils.formatDuration(wifiActivityEnergyInfo.mControllerRxTimeMs, sb);
                    sb.append(" tx=");
                    TimeUtils.formatDuration(wifiActivityEnergyInfo.mControllerTxTimeMs, sb);
                    sb.append(" e=");
                    sb.append(wifiActivityEnergyInfo.mControllerEnergyUsed);
                    Slog.wtf(TAG, sb.toString());
                } else {
                    scanTimeMs = scanTimeMs2;
                    lastEnergy = lastEnergy4;
                    long j13 = lastTxMs2;
                }
            } else {
                scanTimeMs = scanTimeMs2;
                lastEnergy = lastEnergy4;
                long j14 = lastTxMs2;
                maxExpectedIdleTimeMs = timePeriodMs - totalActiveTimeMs;
            }
            delta = delta3;
            delta.mControllerTxTimeMs = txTimeMs;
            delta.mControllerRxTimeMs = rxTimeMs;
            long j15 = timePeriodMs;
            long scanTimeMs3 = scanTimeMs;
            delta.mControllerScanTimeMs = scanTimeMs3;
            long j16 = scanTimeMs3;
            long j17 = totalActiveTimeMs;
            long j18 = lastRxMs;
            delta.mControllerIdleTimeMs = Math.min(maxExpectedIdleTimeMs, Math.max(0, idleTimeMs));
            delta.mControllerEnergyUsed = Math.max(0, wifiActivityEnergyInfo.mControllerEnergyUsed - lastEnergy);
            this.mLastInfo = wifiActivityEnergyInfo;
            return delta;
        }
        delta.mControllerEnergyUsed = wifiActivityEnergyInfo.mControllerEnergyUsed;
        delta.mControllerRxTimeMs = wifiActivityEnergyInfo.mControllerRxTimeMs;
        delta.mControllerTxTimeMs = wifiActivityEnergyInfo.mControllerTxTimeMs;
        delta.mControllerIdleTimeMs = wifiActivityEnergyInfo.mControllerIdleTimeMs;
        delta.mControllerScanTimeMs = wifiActivityEnergyInfo.mControllerScanTimeMs;
        Slog.v(TAG, "WiFi energy data was reset, new WiFi energy data is " + delta);
        this.mLastInfo = wifiActivityEnergyInfo;
        return delta;
    }
}
