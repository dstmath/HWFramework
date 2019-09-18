package com.android.server.location;

import android.os.WorkSource;
import android.util.ArraySet;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GpsFreezeProc {
    private static String TAG = "GpsFreezeProc";
    public static final int WHITE_LIST_TYPE_GPS = 1;
    public static final int WHITE_LIST_TYPE_GPS_TO_NETWORK = 5;
    public static final int WHITE_LIST_TYPE_QUICKGPS_DISABLE = 4;
    public static final int WHITE_LIST_TYPE_QUICKGPS_WHITE = 3;
    public static final int WHITE_LIST_TYPE_WIFISCAN = 2;
    private static GpsFreezeProc mGpsFreezeProc;
    private final HashMap<Integer, ArraySet<String>> mAllPkgWhiteList = new HashMap<>();
    private ArrayList<GpsFreezeListener> mFreezeListenerList = new ArrayList<>();
    private HashMap<String, Integer> mFreezeProcesses = new HashMap<>();

    private GpsFreezeProc() {
    }

    public static GpsFreezeProc getInstance() {
        if (mGpsFreezeProc == null) {
            mGpsFreezeProc = new GpsFreezeProc();
        }
        return mGpsFreezeProc;
    }

    public void addFreezeProcess(String pkg, int uid) {
        Log.d(TAG, "addFreezeProcess enter");
        synchronized (this.mFreezeProcesses) {
            this.mFreezeProcesses.put(pkg, Integer.valueOf(uid));
        }
        String str = TAG;
        Log.d(str, "addFreezeProcess pkg:" + pkg);
        Iterator<GpsFreezeListener> it = this.mFreezeListenerList.iterator();
        while (it.hasNext()) {
            it.next().onFreezeProChange(pkg);
        }
    }

    public void removeFreezeProcess(String pkg, int uid) {
        Log.d(TAG, "removeFreezeProcess enter");
        synchronized (this.mFreezeProcesses) {
            if (uid == 0) {
                try {
                    if (BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(pkg)) {
                        this.mFreezeProcesses.clear();
                    }
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            this.mFreezeProcesses.remove(pkg);
        }
        String str = TAG;
        Log.d(str, "removeFreezeProcess pkg:" + pkg);
        Iterator<GpsFreezeListener> it = this.mFreezeListenerList.iterator();
        while (it.hasNext()) {
            it.next().onFreezeProChange(pkg);
        }
    }

    public boolean isFreeze(String pkgName) {
        boolean containsKey;
        synchronized (this.mFreezeProcesses) {
            containsKey = this.mFreezeProcesses.containsKey(pkgName);
        }
        return containsKey;
    }

    public boolean isInPackageWhiteListByType(int type, String pkgName) {
        synchronized (this.mAllPkgWhiteList) {
            if (!this.mAllPkgWhiteList.containsKey(Integer.valueOf(type))) {
                return false;
            }
            boolean contains = this.mAllPkgWhiteList.get(Integer.valueOf(type)).contains(pkgName);
            return contains;
        }
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        ArraySet<String> mPkgWhiteList;
        if (pkgList == null) {
            Log.e(TAG, "refreshPackageWhitelist pkglist is null");
            return;
        }
        synchronized (this.mAllPkgWhiteList) {
            if (this.mAllPkgWhiteList.containsKey(Integer.valueOf(type))) {
                mPkgWhiteList = this.mAllPkgWhiteList.get(Integer.valueOf(type));
            } else {
                mPkgWhiteList = new ArraySet<>();
            }
            mPkgWhiteList.clear();
            mPkgWhiteList.addAll(pkgList);
            this.mAllPkgWhiteList.put(Integer.valueOf(type), mPkgWhiteList);
        }
        String str = TAG;
        Log.d(str, "refreshPackageWhitelist pkgs-count:" + pkgList.size() + " , type = " + type);
        Iterator<String> it = pkgList.iterator();
        while (it.hasNext()) {
            String str2 = TAG;
            Log.d(str2, "pkgs:" + it.next());
        }
        Iterator<GpsFreezeListener> it2 = this.mFreezeListenerList.iterator();
        while (it2.hasNext()) {
            it2.next().onWhiteListChange(type, pkgList);
        }
    }

    public ArraySet<String> getPackageWhiteList(int type) {
        ArraySet<String> pkgList;
        synchronized (this.mAllPkgWhiteList) {
            if (this.mAllPkgWhiteList.containsKey(Integer.valueOf(type))) {
                pkgList = this.mAllPkgWhiteList.get(Integer.valueOf(type));
            } else {
                pkgList = new ArraySet<>();
            }
        }
        return pkgList;
    }

    public void registerFreezeListener(GpsFreezeListener freezeListener) {
        this.mFreezeListenerList.add(freezeListener);
    }

    public boolean shouldFreeze(WorkSource workSource) {
        boolean shouldFreeze = true;
        for (int i = 0; i < workSource.size(); i++) {
            if (!getInstance().isFreeze(workSource.getName(i))) {
                shouldFreeze = false;
            }
        }
        if (shouldFreeze) {
            Log.i(TAG, "should freeze gps");
        }
        return shouldFreeze;
    }

    public void dump(PrintWriter pw) {
        pw.println("Location Freeze Proc:");
        synchronized (this.mFreezeProcesses) {
            Iterator<String> it = this.mFreezeProcesses.keySet().iterator();
            while (it.hasNext()) {
                pw.println("   " + it.next());
            }
        }
        synchronized (this.mAllPkgWhiteList) {
            for (Map.Entry<Integer, ArraySet<String>> entry : this.mAllPkgWhiteList.entrySet()) {
                ArraySet value = entry.getValue();
                pw.println(" type =" + entry.getKey());
                Iterator it2 = value.iterator();
                while (it2.hasNext()) {
                    pw.println("   " + ((String) it2.next()));
                }
            }
        }
    }
}
