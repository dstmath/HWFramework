package com.android.server.wm;

import android.os.IBinder;
import java.util.HashMap;
import java.util.Map;

public class HwStartWindowRecord {
    private static final Object INSTANCE_LOCK = new Object();
    private static HwStartWindowRecord sInstance;
    private final Map<Integer, Boolean> mStartFromMainAction = new HashMap();
    private final Map<Integer, IBinder> mStartWindowApps = new HashMap();

    public static HwStartWindowRecord getInstance() {
        HwStartWindowRecord hwStartWindowRecord;
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new HwStartWindowRecord();
            }
            hwStartWindowRecord = sInstance;
        }
        return hwStartWindowRecord;
    }

    private HwStartWindowRecord() {
    }

    public void updateStartWindowApp(Integer uid, IBinder token) {
        synchronized (this.mStartWindowApps) {
            this.mStartWindowApps.put(uid, token);
        }
    }

    public boolean checkStartWindowApp(Integer uid) {
        boolean z;
        synchronized (this.mStartWindowApps) {
            z = this.mStartWindowApps.get(uid) != null;
        }
        return z;
    }

    public IBinder getTransferFromStartWindowApp(Integer uid) {
        IBinder iBinder;
        synchronized (this.mStartWindowApps) {
            iBinder = this.mStartWindowApps.get(uid);
        }
        return iBinder;
    }

    public void resetStartWindowApp(Integer uid) {
        synchronized (this.mStartWindowApps) {
            this.mStartWindowApps.put(uid, null);
        }
    }

    public boolean isStartWindowApp(Integer uid) {
        boolean containsKey;
        synchronized (this.mStartWindowApps) {
            containsKey = this.mStartWindowApps.containsKey(uid);
        }
        return containsKey;
    }

    public void removeStartWindowApp(Integer uid) {
        synchronized (this.mStartWindowApps) {
            this.mStartWindowApps.remove(uid);
        }
        synchronized (this.mStartFromMainAction) {
            this.mStartFromMainAction.remove(uid);
        }
    }

    public void clearStartWindowApp() {
        synchronized (this.mStartWindowApps) {
            this.mStartWindowApps.clear();
        }
        synchronized (this.mStartFromMainAction) {
            this.mStartFromMainAction.clear();
        }
    }

    public void setStartFromMainAction(Integer appUid, boolean isStartFromMainAction) {
        if (appUid.intValue() >= 10000) {
            synchronized (this.mStartFromMainAction) {
                this.mStartFromMainAction.put(appUid, Boolean.valueOf(isStartFromMainAction));
            }
        }
    }

    public boolean getStartFromMainAction(Integer appUid) {
        synchronized (this.mStartFromMainAction) {
            if (!this.mStartFromMainAction.containsKey(appUid)) {
                return false;
            }
            return this.mStartFromMainAction.get(appUid).booleanValue();
        }
    }
}
