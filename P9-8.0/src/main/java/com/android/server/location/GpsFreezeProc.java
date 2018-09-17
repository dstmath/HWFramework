package com.android.server.location;

import android.os.WorkSource;
import android.util.ArraySet;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class GpsFreezeProc {
    private static String TAG = "GpsFreezeProc";
    public static final int WHITE_LIST_TYPE_GPS = 1;
    public static final int WHITE_LIST_TYPE_GPS_TO_NETWORK = 5;
    public static final int WHITE_LIST_TYPE_QUICKGPS_DISABLE = 4;
    public static final int WHITE_LIST_TYPE_QUICKGPS_WHITE = 3;
    public static final int WHITE_LIST_TYPE_WIFISCAN = 2;
    private static GpsFreezeProc mGpsFreezeProc;
    private final HashMap<Integer, ArraySet<String>> mAllPkgWhiteList = new HashMap();
    private ArrayList<GpsFreezeListener> mFreezeListenerList = new ArrayList();
    private HashMap<String, Integer> mFreezeProcesses = new HashMap();

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
        Log.d(TAG, "addFreezeProcess pkg:" + pkg);
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onFreezeProChange(pkg);
        }
    }

    public void removeFreezeProcess(String pkg, int uid) {
        Log.d(TAG, "removeFreezeProcess enter");
        synchronized (this.mFreezeProcesses) {
            if (uid == 0) {
                if ("".equals(pkg)) {
                    this.mFreezeProcesses.clear();
                }
            }
            this.mFreezeProcesses.remove(pkg);
        }
        Log.d(TAG, "removeFreezeProcess pkg:" + pkg);
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onFreezeProChange(pkg);
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
            if (this.mAllPkgWhiteList.containsKey(Integer.valueOf(type))) {
                boolean contains = ((ArraySet) this.mAllPkgWhiteList.get(Integer.valueOf(type))).contains(pkgName);
                return contains;
            }
            return false;
        }
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        if (pkgList == null) {
            Log.e(TAG, "refreshPackageWhitelist pkglist is null");
            return;
        }
        synchronized (this.mAllPkgWhiteList) {
            ArraySet<String> mPkgWhiteList;
            if (this.mAllPkgWhiteList.containsKey(Integer.valueOf(type))) {
                mPkgWhiteList = (ArraySet) this.mAllPkgWhiteList.get(Integer.valueOf(type));
            } else {
                mPkgWhiteList = new ArraySet();
            }
            mPkgWhiteList.clear();
            mPkgWhiteList.addAll(pkgList);
            this.mAllPkgWhiteList.put(Integer.valueOf(type), mPkgWhiteList);
        }
        Log.d(TAG, "refreshPackageWhitelist pkgs-count:" + pkgList.size() + " , type = " + type);
        for (String pkg : pkgList) {
            Log.d(TAG, "pkgs:" + pkg);
        }
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onWhiteListChange(type, pkgList);
        }
    }

    public ArraySet<String> getPackageWhiteList(int type) {
        ArraySet<String> pkgList;
        synchronized (this.mAllPkgWhiteList) {
            if (this.mAllPkgWhiteList.containsKey(Integer.valueOf(type))) {
                pkgList = (ArraySet) this.mAllPkgWhiteList.get(Integer.valueOf(type));
            } else {
                pkgList = new ArraySet();
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
            for (String pkg : this.mFreezeProcesses.keySet()) {
                pw.println("   " + pkg);
            }
        }
        synchronized (this.mAllPkgWhiteList) {
            for (Entry<Integer, ArraySet<String>> entry : this.mAllPkgWhiteList.entrySet()) {
                ArraySet<String> pkglist = (ArraySet) entry.getValue();
                pw.println(" type =" + ((Integer) entry.getKey()));
                for (String pkg2 : pkglist) {
                    pw.println("   " + pkg2);
                }
            }
        }
    }
}
