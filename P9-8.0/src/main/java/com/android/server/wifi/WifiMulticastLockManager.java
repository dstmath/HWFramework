package com.android.server.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class WifiMulticastLockManager {
    private static final String TAG = "WifiMulticastLockManager";
    private final IBatteryStats mBatteryStats;
    private final FilterController mFilterController;
    private int mMulticastDisabled = 0;
    private int mMulticastEnabled = 0;
    private final List<Multicaster> mMulticasters = new ArrayList();
    private boolean mVerboseLoggingEnabled = false;

    public interface FilterController {
        void startFilteringMulticastPackets();

        void stopFilteringMulticastPackets();
    }

    private class Multicaster implements DeathRecipient {
        IBinder mBinder;
        String mTag;
        int mUid = Binder.getCallingUid();

        Multicaster(String tag, IBinder binder) {
            this.mTag = tag;
            this.mBinder = binder;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        public void binderDied() {
            Slog.e(WifiMulticastLockManager.TAG, "Multicaster binderDied");
            synchronized (WifiMulticastLockManager.this.mMulticasters) {
                int i = WifiMulticastLockManager.this.mMulticasters.indexOf(this);
                if (i != -1) {
                    WifiMulticastLockManager.this.removeMulticasterLocked(i, this.mUid);
                }
            }
        }

        void unlinkDeathRecipient() {
            this.mBinder.unlinkToDeath(this, 0);
        }

        public int getUid() {
            return this.mUid;
        }

        public String toString() {
            return "Multicaster{" + this.mTag + " uid=" + this.mUid + "}";
        }
    }

    public WifiMulticastLockManager(FilterController filterController, IBatteryStats batteryStats) {
        this.mBatteryStats = batteryStats;
        this.mFilterController = filterController;
    }

    protected void dump(PrintWriter pw) {
        pw.println("mMulticastEnabled " + this.mMulticastEnabled);
        pw.println("mMulticastDisabled " + this.mMulticastDisabled);
        pw.println("Multicast Locks held:");
        for (Multicaster l : this.mMulticasters) {
            pw.print("    ");
            pw.println(l);
        }
    }

    protected void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    public void initializeFiltering() {
        synchronized (this.mMulticasters) {
            if (this.mMulticasters.size() != 0) {
                return;
            }
            this.mFilterController.startFilteringMulticastPackets();
        }
    }

    public void acquireLock(IBinder binder, String tag) {
        synchronized (this.mMulticasters) {
            this.mMulticastEnabled++;
            this.mMulticasters.add(new Multicaster(tag, binder));
            this.mFilterController.stopFilteringMulticastPackets();
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteWifiMulticastEnabled(uid);
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in noteWifiMulticastEnabled: " + e.getMessage());
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void releaseLock() {
        int uid = Binder.getCallingUid();
        synchronized (this.mMulticasters) {
            this.mMulticastDisabled++;
            for (int i = this.mMulticasters.size() - 1; i >= 0; i--) {
                Multicaster m = (Multicaster) this.mMulticasters.get(i);
                if (m != null && m.getUid() == uid) {
                    removeMulticasterLocked(i, uid);
                }
            }
        }
    }

    private void removeMulticasterLocked(int i, int uid) {
        Multicaster removed = (Multicaster) this.mMulticasters.remove(i);
        if (removed != null) {
            removed.unlinkDeathRecipient();
        }
        if (this.mMulticasters.size() == 0) {
            this.mFilterController.startFilteringMulticastPackets();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteWifiMulticastDisabled(uid);
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in noteWifiMulticastDisabled: " + e.getMessage());
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isMulticastEnabled() {
        boolean z = false;
        synchronized (this.mMulticasters) {
            if (this.mMulticasters.size() > 0) {
                z = true;
            }
        }
        return z;
    }
}
