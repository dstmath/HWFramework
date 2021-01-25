package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.app.IBatteryStats;
import com.android.server.wifi.WifiLockManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class WifiLockManager {
    private static final int IGNORE_SCREEN_STATE_MASK = 1;
    private static final int IGNORE_WIFI_STATE_MASK = 2;
    private static final int LOW_LATENCY_NOT_SUPPORTED = 0;
    private static final int LOW_LATENCY_SUPPORTED = 1;
    private static final int LOW_LATENCY_SUPPORT_UNDEFINED = -1;
    private static final String TAG = "WifiLockManager";
    private final ActivityManager mActivityManager;
    private final IBatteryStats mBatteryStats;
    private final ClientModeImpl mClientModeImpl;
    private final Clock mClock;
    private final Context mContext;
    private int mCurrentOpMode;
    private long mCurrentSessionStartTimeMs;
    private boolean mForceHiPerfMode = false;
    private boolean mForceLowLatencyMode = false;
    private final FrameworkFacade mFrameworkFacade;
    private int mFullHighPerfLocksAcquired;
    private int mFullHighPerfLocksReleased;
    private int mFullLowLatencyLocksAcquired;
    private int mFullLowLatencyLocksReleased;
    private final Handler mHandler;
    private int mLatencyModeSupport = -1;
    private final SparseArray<UidRec> mLowLatencyUidWatchList = new SparseArray<>();
    private boolean mScreenOn = false;
    private boolean mVerboseLoggingEnabled = false;
    private boolean mWifiConnected = false;
    private final List<WifiLock> mWifiLocks = new ArrayList();
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    WifiLockManager(Context context, IBatteryStats batteryStats, ClientModeImpl clientModeImpl, FrameworkFacade frameworkFacade, Handler handler, WifiNative wifiNative, Clock clock, WifiMetrics wifiMetrics) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mClientModeImpl = clientModeImpl;
        this.mFrameworkFacade = frameworkFacade;
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mCurrentOpMode = 0;
        this.mWifiNative = wifiNative;
        this.mHandler = handler;
        this.mClock = clock;
        this.mWifiMetrics = wifiMetrics;
        registerUidImportanceTransitions();
    }

    private boolean canActivateHighPerfLock(int ignoreMask) {
        if ((ignoreMask & 2) != 0) {
            return true;
        }
        return 1 != 0 && this.mWifiConnected;
    }

    private boolean canActivateHighPerfLock() {
        return canActivateHighPerfLock(0);
    }

    private boolean canActivateLowLatencyLock(int ignoreMask, UidRec uidRec) {
        boolean check = true;
        boolean check2 = false;
        if ((ignoreMask & 2) == 0) {
            check = 1 != 0 && this.mWifiConnected;
        }
        if ((ignoreMask & 1) == 0) {
            check = check && this.mScreenOn;
        }
        if (uidRec == null) {
            return check;
        }
        if (check && uidRec.mIsFg) {
            check2 = true;
        }
        return check2;
    }

    private boolean canActivateLowLatencyLock(int ignoreMask) {
        return canActivateLowLatencyLock(ignoreMask, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canActivateLowLatencyLock() {
        return canActivateLowLatencyLock(0, null);
    }

    private void registerUidImportanceTransitions() {
        this.mActivityManager.addOnUidImportanceListener(new ActivityManager.OnUidImportanceListener() {
            /* class com.android.server.wifi.WifiLockManager.AnonymousClass1 */

            public void onUidImportance(int uid, int importance) {
                WifiLockManager.this.mHandler.post(new Runnable(uid, importance) {
                    /* class com.android.server.wifi.$$Lambda$WifiLockManager$1$CkMHEvkkoNUZsaBQmCb9B0F8lI */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        WifiLockManager.AnonymousClass1.this.lambda$onUidImportance$0$WifiLockManager$1(this.f$1, this.f$2);
                    }
                });
            }

            public /* synthetic */ void lambda$onUidImportance$0$WifiLockManager$1(int uid, int importance) {
                UidRec uidRec = (UidRec) WifiLockManager.this.mLowLatencyUidWatchList.get(uid);
                if (uidRec != null) {
                    boolean newModeIsFg = importance == 100;
                    if (uidRec.mIsFg != newModeIsFg) {
                        uidRec.mIsFg = newModeIsFg;
                        WifiLockManager.this.updateOpMode();
                        if (WifiLockManager.this.canActivateLowLatencyLock()) {
                            WifiLockManager.this.setBlameLowLatencyUid(uid, uidRec.mIsFg);
                        }
                    }
                }
            }
        }, 125);
    }

    public boolean acquireWifiLock(int lockMode, String tag, IBinder binder, WorkSource ws) {
        if (isValidLockMode(lockMode)) {
            return addLock(new WifiLock(lockMode, tag, binder, new WorkSource(ws)));
        }
        Slog.e(TAG, "invalid lockMode =" + lockMode);
        return false;
    }

    public boolean releaseWifiLock(IBinder binder) {
        return releaseLock(binder);
    }

    public synchronized int getStrongestLockMode() {
        if (!this.mWifiConnected) {
            return 0;
        }
        if (this.mForceHiPerfMode) {
            return 3;
        }
        if (this.mForceLowLatencyMode) {
            return 4;
        }
        if (this.mScreenOn && countFgLowLatencyUids() > 0) {
            return 4;
        }
        if (this.mFullHighPerfLocksAcquired > this.mFullHighPerfLocksReleased) {
            return 3;
        }
        return 0;
    }

    public synchronized WorkSource createMergedWorkSource() {
        WorkSource mergedWS;
        mergedWS = new WorkSource();
        for (WifiLock lock : this.mWifiLocks) {
            mergedWS.add(lock.getWorkSource());
        }
        return mergedWS;
    }

    public synchronized void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        WifiLock wl = findLockByBinder(binder);
        if (wl == null) {
            Slog.e(TAG, "invalid argument, wifi lock not active");
            return;
        }
        WorkSource newWorkSource = new WorkSource(ws);
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "updateWifiLockWakeSource: " + wl + ", newWorkSource=" + newWorkSource);
        }
        int i = wl.mMode;
        if (i != 3) {
            if (i == 4) {
                addWsToLlWatchList(newWorkSource);
                removeWsFromLlWatchList(wl.mWorkSource);
                updateOpMode();
            }
        } else if (canActivateHighPerfLock()) {
            setBlameHiPerfWs(newWorkSource, true);
            setBlameHiPerfWs(wl.mWorkSource, false);
        }
        wl.mWorkSource = newWorkSource;
    }

    public boolean forceHiPerfMode(boolean isEnabled) {
        this.mForceHiPerfMode = isEnabled;
        this.mForceLowLatencyMode = false;
        if (updateOpMode()) {
            return true;
        }
        Slog.e(TAG, "Failed to force hi-perf mode, returning to normal mode");
        this.mForceHiPerfMode = false;
        return false;
    }

    public boolean forceLowLatencyMode(boolean isEnabled) {
        this.mForceLowLatencyMode = isEnabled;
        this.mForceHiPerfMode = false;
        if (updateOpMode()) {
            return true;
        }
        Slog.e(TAG, "Failed to force low-latency mode, returning to normal mode");
        this.mForceLowLatencyMode = false;
        return false;
    }

    public void handleScreenStateChanged(boolean screenOn) {
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "handleScreenStateChanged: screenOn = " + screenOn);
        }
        this.mScreenOn = screenOn;
        if (canActivateLowLatencyLock(1)) {
            updateOpMode();
            setBlameLowLatencyWatchList(screenOn);
        }
    }

    public void updateWifiClientConnected(boolean isConnected) {
        if (this.mWifiConnected != isConnected) {
            this.mWifiConnected = isConnected;
            if (canActivateLowLatencyLock(2)) {
                setBlameLowLatencyWatchList(this.mWifiConnected);
            }
            if (canActivateHighPerfLock(2)) {
                setBlameHiPerfLocks(this.mWifiConnected);
            }
            updateOpMode();
        }
    }

    private synchronized void setBlameHiPerfLocks(boolean shouldBlame) {
        for (WifiLock lock : this.mWifiLocks) {
            if (lock.mMode == 3) {
                setBlameHiPerfWs(lock.getWorkSource(), shouldBlame);
            }
        }
    }

    private static boolean isValidLockMode(int lockMode) {
        if (lockMode == 1 || lockMode == 2 || lockMode == 3 || lockMode == 4) {
            return true;
        }
        return false;
    }

    private void addUidToLlWatchList(int uid) {
        UidRec uidRec = this.mLowLatencyUidWatchList.get(uid);
        if (uidRec != null) {
            uidRec.mLockCount++;
            return;
        }
        UidRec uidRec2 = new UidRec(uid);
        uidRec2.mLockCount = 1;
        this.mLowLatencyUidWatchList.put(uid, uidRec2);
        if (this.mFrameworkFacade.isAppForeground(uid)) {
            uidRec2.mIsFg = true;
        }
        if (canActivateLowLatencyLock(0, uidRec2)) {
            setBlameLowLatencyUid(uid, true);
        }
    }

    private void removeUidFromLlWatchList(int uid) {
        UidRec uidRec = this.mLowLatencyUidWatchList.get(uid);
        if (uidRec == null) {
            Slog.e(TAG, "Failed to find uid in low-latency watch list");
            return;
        }
        if (uidRec.mLockCount > 0) {
            uidRec.mLockCount--;
        } else {
            Slog.e(TAG, "Error, uid record conatains no locks");
        }
        if (uidRec.mLockCount == 0) {
            this.mLowLatencyUidWatchList.remove(uid);
            if (canActivateLowLatencyLock(0, uidRec)) {
                setBlameLowLatencyUid(uid, false);
            }
        }
    }

    private void addWsToLlWatchList(WorkSource ws) {
        int wsSize = ws.size();
        for (int i = 0; i < wsSize; i++) {
            addUidToLlWatchList(ws.get(i));
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                addUidToLlWatchList(workChains.get(i2).getAttributionUid());
            }
        }
    }

    private void removeWsFromLlWatchList(WorkSource ws) {
        int wsSize = ws.size();
        for (int i = 0; i < wsSize; i++) {
            removeUidFromLlWatchList(ws.get(i));
        }
        List<WorkSource.WorkChain> workChains = ws.getWorkChains();
        if (workChains != null) {
            for (int i2 = 0; i2 < workChains.size(); i2++) {
                removeUidFromLlWatchList(workChains.get(i2).getAttributionUid());
            }
        }
    }

    private synchronized boolean addLock(WifiLock lock) {
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "addLock: " + lock);
        }
        if (findLockByBinder(lock.getBinder()) != null) {
            if (this.mVerboseLoggingEnabled) {
                Slog.d(TAG, "attempted to add a lock when already holding one");
            }
            return false;
        }
        this.mWifiLocks.add(lock);
        int i = lock.mMode;
        if (i == 3) {
            this.mFullHighPerfLocksAcquired++;
            if (canActivateHighPerfLock()) {
                setBlameHiPerfWs(lock.mWorkSource, true);
            }
        } else if (i == 4) {
            addWsToLlWatchList(lock.getWorkSource());
            this.mFullLowLatencyLocksAcquired++;
        }
        updateOpMode();
        return true;
    }

    private synchronized WifiLock removeLock(IBinder binder) {
        WifiLock lock;
        lock = findLockByBinder(binder);
        if (lock != null) {
            this.mWifiLocks.remove(lock);
            lock.unlinkDeathRecipient();
        }
        return lock;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean releaseLock(IBinder binder) {
        WifiLock wifiLock = removeLock(binder);
        if (wifiLock == null) {
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "releaseLock: " + wifiLock);
        }
        int i = wifiLock.mMode;
        if (i == 3) {
            this.mFullHighPerfLocksReleased++;
            this.mWifiMetrics.addWifiLockAcqSession(3, this.mClock.getElapsedSinceBootMillis() - wifiLock.getAcqTimestamp());
            if (canActivateHighPerfLock()) {
                setBlameHiPerfWs(wifiLock.mWorkSource, false);
            }
        } else if (i == 4) {
            removeWsFromLlWatchList(wifiLock.getWorkSource());
            this.mFullLowLatencyLocksReleased++;
            this.mWifiMetrics.addWifiLockAcqSession(4, this.mClock.getElapsedSinceBootMillis() - wifiLock.getAcqTimestamp());
        }
        updateOpMode();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean updateOpMode() {
        int newLockMode = getStrongestLockMode();
        if (newLockMode == this.mCurrentOpMode) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "Current opMode: " + this.mCurrentOpMode + " New LockMode: " + newLockMode);
        }
        int i = this.mCurrentOpMode;
        if (i == 3) {
            this.mWifiMetrics.addWifiLockActiveSession(3, this.mClock.getElapsedSinceBootMillis() - this.mCurrentSessionStartTimeMs);
        } else if (i == 4) {
            if (!setLowLatencyMode(false)) {
                Slog.e(TAG, "Failed to reset the OpMode from low-latency to Normal");
                return false;
            }
            this.mWifiMetrics.addWifiLockActiveSession(4, this.mClock.getElapsedSinceBootMillis() - this.mCurrentSessionStartTimeMs);
        }
        this.mCurrentOpMode = 0;
        if (newLockMode != 0) {
            if (newLockMode == 3) {
                this.mCurrentSessionStartTimeMs = this.mClock.getElapsedSinceBootMillis();
            } else if (newLockMode != 4) {
                Slog.e(TAG, "Invalid new opMode: " + newLockMode);
                return false;
            } else if (!setLowLatencyMode(true)) {
                Slog.e(TAG, "Failed to set the OpMode to low-latency");
                return false;
            } else {
                this.mCurrentSessionStartTimeMs = this.mClock.getElapsedSinceBootMillis();
            }
        }
        this.mCurrentOpMode = newLockMode;
        return true;
    }

    private int getLowLatencyModeSupport() {
        if (this.mLatencyModeSupport == -1) {
            String ifaceName = this.mWifiNative.getClientInterfaceName();
            if (ifaceName == null) {
                return -1;
            }
            long supportedFeatures = this.mWifiNative.getSupportedFeatureSet(ifaceName);
            if (supportedFeatures != 0) {
                if ((1073741824 & supportedFeatures) != 0) {
                    this.mLatencyModeSupport = 1;
                } else {
                    this.mLatencyModeSupport = 0;
                }
            }
        }
        return this.mLatencyModeSupport;
    }

    private boolean setLowLatencyMode(boolean enabled) {
        int lowLatencySupport = getLowLatencyModeSupport();
        if (lowLatencySupport == -1) {
            return false;
        }
        if (lowLatencySupport != 1 || this.mClientModeImpl.setLowLatencyMode(enabled)) {
            return true;
        }
        Slog.e(TAG, "Failed to set low latency mode");
        return false;
    }

    private synchronized WifiLock findLockByBinder(IBinder binder) {
        for (WifiLock lock : this.mWifiLocks) {
            if (lock.getBinder() == binder) {
                return lock;
            }
        }
        return null;
    }

    private int countFgLowLatencyUids() {
        int uidCount = 0;
        int listSize = this.mLowLatencyUidWatchList.size();
        for (int idx = 0; idx < listSize; idx++) {
            if (this.mLowLatencyUidWatchList.valueAt(idx).mIsFg) {
                uidCount++;
            }
        }
        return uidCount;
    }

    private void setBlameHiPerfWs(WorkSource ws, boolean shouldBlame) {
        long ident = Binder.clearCallingIdentity();
        if (shouldBlame) {
            try {
                this.mBatteryStats.noteFullWifiLockAcquiredFromSource(ws);
                StatsLog.write(37, ws, 1, 3);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            this.mBatteryStats.noteFullWifiLockReleasedFromSource(ws);
            StatsLog.write(37, ws, 0, 3);
        }
        Binder.restoreCallingIdentity(ident);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBlameLowLatencyUid(int uid, boolean shouldBlame) {
        long ident = Binder.clearCallingIdentity();
        if (shouldBlame) {
            try {
                this.mBatteryStats.noteFullWifiLockAcquired(uid);
                StatsLog.write_non_chained(37, uid, null, 1, 4);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            this.mBatteryStats.noteFullWifiLockReleased(uid);
            StatsLog.write_non_chained(37, uid, null, 0, 4);
        }
        Binder.restoreCallingIdentity(ident);
    }

    private void setBlameLowLatencyWatchList(boolean shouldBlame) {
        for (int idx = 0; idx < this.mLowLatencyUidWatchList.size(); idx++) {
            UidRec uidRec = this.mLowLatencyUidWatchList.valueAt(idx);
            if (uidRec.mIsFg) {
                setBlameLowLatencyUid(uidRec.mUid, shouldBlame);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("Locks acquired: " + this.mFullHighPerfLocksAcquired + " full high perf, " + this.mFullLowLatencyLocksAcquired + " full low latency");
        pw.println("Locks released: " + this.mFullHighPerfLocksReleased + " full high perf, " + this.mFullLowLatencyLocksReleased + " full low latency");
        pw.println();
        pw.println("Locks held:");
        for (WifiLock lock : this.mWifiLocks) {
            pw.print("    ");
            pw.println(lock);
        }
    }

    /* access modifiers changed from: protected */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    /* access modifiers changed from: private */
    public class WifiLock implements IBinder.DeathRecipient {
        long mAcqTimestamp;
        IBinder mBinder;
        int mMode;
        String mTag;
        int mUid = Binder.getCallingUid();
        WorkSource mWorkSource;

        WifiLock(int lockMode, String tag, IBinder binder, WorkSource ws) {
            this.mTag = tag;
            this.mBinder = binder;
            this.mMode = lockMode;
            this.mWorkSource = ws;
            this.mAcqTimestamp = WifiLockManager.this.mClock.getElapsedSinceBootMillis();
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        /* access modifiers changed from: protected */
        public WorkSource getWorkSource() {
            return this.mWorkSource;
        }

        /* access modifiers changed from: protected */
        public int getUid() {
            return this.mUid;
        }

        /* access modifiers changed from: protected */
        public IBinder getBinder() {
            return this.mBinder;
        }

        /* access modifiers changed from: protected */
        public long getAcqTimestamp() {
            return this.mAcqTimestamp;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            WifiLockManager.this.releaseLock(this.mBinder);
        }

        public void unlinkDeathRecipient() {
            this.mBinder.unlinkToDeath(this, 0);
        }

        @Override // java.lang.Object
        public String toString() {
            return "WifiLock{" + this.mTag + " type=" + this.mMode + " uid=" + this.mUid + " workSource=" + this.mWorkSource + "}";
        }
    }

    /* access modifiers changed from: private */
    public class UidRec {
        boolean mIsFg;
        int mLockCount;
        final int mUid;

        UidRec(int uid) {
            this.mUid = uid;
        }
    }
}
