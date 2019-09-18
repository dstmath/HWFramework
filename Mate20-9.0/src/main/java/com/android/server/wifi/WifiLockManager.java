package com.android.server.wifi;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class WifiLockManager {
    private static final String TAG = "WifiLockManager";
    private final IBatteryStats mBatteryStats;
    private final Context mContext;
    private int mFullHighPerfLocksAcquired;
    private int mFullHighPerfLocksReleased;
    private int mFullLocksAcquired;
    private int mFullLocksReleased;
    private int mScanLocksAcquired;
    private int mScanLocksReleased;
    private boolean mVerboseLoggingEnabled = false;
    private final List<WifiLock> mWifiLocks = new ArrayList();

    private class WifiLock implements IBinder.DeathRecipient {
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

        public void binderDied() {
            boolean unused = WifiLockManager.this.releaseLock(this.mBinder);
        }

        public void unlinkDeathRecipient() {
            this.mBinder.unlinkToDeath(this, 0);
        }

        public String toString() {
            return "WifiLock{" + this.mTag + " type=" + this.mMode + " uid=" + this.mUid + " workSource=" + this.mWorkSource + "}";
        }
    }

    WifiLockManager(Context context, IBatteryStats batteryStats) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
    }

    public boolean acquireWifiLock(int lockMode, String tag, IBinder binder, WorkSource ws) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        if (isValidLockMode(lockMode)) {
            if (ws == null || ws.isEmpty()) {
                ws = new WorkSource(Binder.getCallingUid());
            } else {
                this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
            }
            WifiLock wifiLock = new WifiLock(lockMode, tag, binder, ws);
            return addLock(wifiLock);
        }
        throw new IllegalArgumentException("lockMode =" + lockMode);
    }

    public boolean releaseWifiLock(IBinder binder) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        return releaseLock(binder);
    }

    public synchronized int getStrongestLockMode() {
        if (this.mWifiLocks.isEmpty()) {
            return 0;
        }
        if (this.mFullHighPerfLocksAcquired > this.mFullHighPerfLocksReleased) {
            return 3;
        }
        if (this.mFullLocksAcquired > this.mFullLocksReleased) {
            return 1;
        }
        return 2;
    }

    public synchronized WorkSource createMergedWorkSource() {
        WorkSource mergedWS;
        mergedWS = new WorkSource();
        for (WifiLock lock : this.mWifiLocks) {
            mergedWS.add(lock.getWorkSource());
        }
        return mergedWS;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c A[Catch:{ all -> 0x0060 }] */
    public synchronized void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        WorkSource newWorkSource;
        long ident;
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
        WifiLock wl = findLockByBinder(binder);
        if (wl != null) {
            if (ws != null) {
                if (!ws.isEmpty()) {
                    newWorkSource = new WorkSource(ws);
                    if (this.mVerboseLoggingEnabled) {
                        Slog.d(TAG, "updateWifiLockWakeSource: " + wl + ", newWorkSource=" + newWorkSource);
                    }
                    ident = Binder.clearCallingIdentity();
                    this.mBatteryStats.noteFullWifiLockAcquiredFromSource(newWorkSource);
                    this.mBatteryStats.noteFullWifiLockReleasedFromSource(wl.mWorkSource);
                    wl.mWorkSource = newWorkSource;
                    Binder.restoreCallingIdentity(ident);
                }
            }
            newWorkSource = new WorkSource(Binder.getCallingUid());
            if (this.mVerboseLoggingEnabled) {
            }
            ident = Binder.clearCallingIdentity();
            try {
                this.mBatteryStats.noteFullWifiLockAcquiredFromSource(newWorkSource);
                this.mBatteryStats.noteFullWifiLockReleasedFromSource(wl.mWorkSource);
                wl.mWorkSource = newWorkSource;
                Binder.restoreCallingIdentity(ident);
            } catch (RemoteException e) {
                try {
                    Slog.e(TAG, "RemoteException in noteFullWifiLockReleasedFromSource or noteFullWifiLockAcquiredFromSource: " + e.getMessage());
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        } else {
            throw new IllegalArgumentException("Wifi lock not active");
        }
        return;
    }

    public boolean clearWifiLocks() {
        return clearWifiLocksLocked();
    }

    private synchronized boolean clearWifiLocksLocked() {
        List<WifiLock> copyList = new ArrayList<>();
        copyList.addAll(this.mWifiLocks);
        for (WifiLock l : copyList) {
            if ("WiFiDirectFT".equals(l.mTag)) {
                Slog.d(TAG, "don't release module: " + "WiFiDirectFT");
            } else if (!releaseWifiLock(l.mBinder)) {
                Slog.d(TAG, "releaseWifiLock failed , don't send CMD_LACKS_CHANGED");
            }
        }
        if (this.mWifiLocks.size() != 0) {
            Slog.d(TAG, "mWifiLocks.size() != 0, don't send CMD_LOCKS_CHANGED");
            return false;
        }
        Slog.e(TAG, "CMD_LOCKS_CHANGED is waived!!!");
        return true;
    }

    private static boolean isValidLockMode(int lockMode) {
        if (lockMode == 1 || lockMode == 2 || lockMode == 3) {
            return true;
        }
        return false;
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
        boolean lockAdded = false;
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteFullWifiLockAcquiredFromSource(lock.mWorkSource);
            switch (lock.mMode) {
                case 1:
                    this.mFullLocksAcquired++;
                    break;
                case 2:
                    this.mScanLocksAcquired++;
                    break;
                case 3:
                    this.mFullHighPerfLocksAcquired++;
                    break;
            }
            lockAdded = true;
            Binder.restoreCallingIdentity(ident);
        } catch (RemoteException e) {
            try {
                Slog.e(TAG, "RemoteException in addLock : " + e.getMessage());
                return lockAdded;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
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
    public synchronized boolean releaseLock(IBinder binder) {
        WifiLock wifiLock = removeLock(binder);
        if (wifiLock == null) {
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "releaseLock: " + wifiLock);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteFullWifiLockReleasedFromSource(wifiLock.mWorkSource);
            switch (wifiLock.mMode) {
                case 1:
                    this.mFullLocksReleased++;
                    break;
                case 2:
                    this.mScanLocksReleased++;
                    break;
                case 3:
                    this.mFullHighPerfLocksReleased++;
                    break;
            }
            Binder.restoreCallingIdentity(ident);
        } catch (RemoteException e) {
            try {
                Slog.e(TAG, "RemoteException occurs: " + e.getMessage());
                return true;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private synchronized WifiLock findLockByBinder(IBinder binder) {
        for (WifiLock lock : this.mWifiLocks) {
            if (lock.getBinder() == binder) {
                return lock;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("Locks acquired: " + this.mFullLocksAcquired + " full, " + this.mFullHighPerfLocksAcquired + " full high perf, " + this.mScanLocksAcquired + " scan");
        pw.println("Locks released: " + this.mFullLocksReleased + " full, " + this.mFullHighPerfLocksReleased + " full high perf, " + this.mScanLocksReleased + " scan");
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
}
