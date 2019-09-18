package com.android.server.wm;

import android.os.IBinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwStartWindowRecord {
    private static HwStartWindowRecord sInstance;
    private List<Integer> mOriAppLauncher = new ArrayList();
    private Map<Integer, Boolean> mStartFromMainAction = new HashMap();
    private Map<Integer, IBinder> mStartWindowApps = new HashMap();

    public static synchronized HwStartWindowRecord getInstance() {
        HwStartWindowRecord hwStartWindowRecord;
        synchronized (HwStartWindowRecord.class) {
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
        synchronized (this.mStartWindowApps) {
            if (this.mStartWindowApps.get(uid) != null) {
                return true;
            }
            return false;
        }
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

    public void addOriAppLauncher(Integer appUid) {
        synchronized (this.mOriAppLauncher) {
            if (!this.mOriAppLauncher.contains(appUid)) {
                this.mOriAppLauncher.add(appUid);
            }
        }
    }

    public boolean hasOriAppLauncher() {
        boolean z;
        synchronized (this.mOriAppLauncher) {
            z = !this.mOriAppLauncher.isEmpty();
        }
        return z;
    }

    public void clearOriAppLauncher() {
        synchronized (this.mOriAppLauncher) {
            this.mOriAppLauncher.clear();
        }
    }

    public void removeOriAppLauncher(Integer appUid) {
        synchronized (this.mOriAppLauncher) {
            this.mOriAppLauncher.remove(appUid);
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
            boolean booleanValue = this.mStartFromMainAction.get(appUid).booleanValue();
            return booleanValue;
        }
    }
}
