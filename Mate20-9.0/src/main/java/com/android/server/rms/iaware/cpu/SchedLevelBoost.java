package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.server.mtm.utils.InnerUtils;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedLevelBoost {
    private static final String TAG = "SchedLevelBoost";
    private static SchedLevelBoost sInstance;
    private static Object sObject = new Object();
    private int mEasCmdId;
    private SparseArray<AppInfo> mFgAppMap = new SparseArray<>();
    private int mFreqCmdId;
    public AtomicBoolean mIsLcpuLimited = new AtomicBoolean(false);
    private AtomicBoolean mIsScreenOn = new AtomicBoolean(true);
    private Set<String> mWhiteList = new ArraySet();

    static class AppInfo {
        boolean isWhiteList;
        int pid;
        int uid;

        public AppInfo(int pid2, int uid2, boolean isWhiteList2) {
            this.pid = pid2;
            this.uid = uid2;
            this.isWhiteList = isWhiteList2;
        }
    }

    private SchedLevelBoost() {
    }

    public static SchedLevelBoost getInstance() {
        SchedLevelBoost schedLevelBoost;
        synchronized (sObject) {
            if (sInstance == null) {
                sInstance = new SchedLevelBoost();
            }
            schedLevelBoost = sInstance;
        }
        return schedLevelBoost;
    }

    public void setParams(String strEasCmdId, String strFreqCmdId) {
        this.mEasCmdId = parseInt(strEasCmdId);
        this.mFreqCmdId = parseInt(strFreqCmdId);
    }

    private int parseInt(String strValue) {
        int value = -1;
        if (strValue == null) {
            AwareLog.e(TAG, "parseInt strValue is null!");
            return -1;
        }
        try {
            value = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseInt catch NumberFormatException, strVlue = " + strValue);
        }
        return value;
    }

    public void addItem(String pkgName) {
        if (pkgName == null) {
            AwareLog.w(TAG, "addItem pkgName is null!");
        } else {
            this.mWhiteList.add(pkgName);
        }
    }

    private boolean isWhiteList(String pkgName) {
        if (pkgName != null) {
            return this.mWhiteList.contains(pkgName);
        }
        AwareLog.w(TAG, "isWhiteList pkgName is null!");
        return false;
    }

    private boolean empty() {
        return this.mWhiteList.isEmpty();
    }

    public void onFgActivitiesChanged(int pid, int uid, boolean fg) {
        if (empty()) {
            AwareLog.w(TAG, "onFgActivitiesChanged config not set!");
            return;
        }
        if (fg) {
            addFgProc(pid, uid);
        } else {
            removeFgProc(pid, uid);
        }
    }

    public void onProcessDied(int pid, int uid) {
        if (empty()) {
            AwareLog.w(TAG, "onProcessDied config not set!");
        } else {
            removeFgProc(pid, uid);
        }
    }

    private void addFgProc(int pid, int uid) {
        synchronized (this.mFgAppMap) {
            boolean isWhiteList = isWhiteList(InnerUtils.getAwarePkgName(pid));
            if (isWhiteList && this.mIsScreenOn.get()) {
                enterSchedLevelBoost();
            }
            this.mFgAppMap.put(pid, new AppInfo(pid, uid, isWhiteList));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0033, code lost:
        return;
     */
    private void removeFgProc(int pid, int uid) {
        synchronized (this.mFgAppMap) {
            AppInfo info = this.mFgAppMap.get(pid);
            if (info != null && pid == info.pid) {
                if (uid == info.uid) {
                    this.mFgAppMap.remove(pid);
                    if (info.isWhiteList && this.mIsScreenOn.get() && !isLastWhiteList()) {
                        exitSchedLevelBoost();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        return;
     */
    public void onScreenStateChanged(boolean isScreenOn) {
        synchronized (this.mFgAppMap) {
            this.mIsScreenOn.set(isScreenOn);
            if (empty()) {
                AwareLog.w(TAG, "onScreenStateChanged config not set!");
            } else if (isLastWhiteList()) {
                if (isScreenOn) {
                    enterSchedLevelBoost();
                } else {
                    exitSchedLevelBoost();
                }
            }
        }
    }

    private boolean isLastWhiteList() {
        synchronized (this.mFgAppMap) {
            int size = this.mFgAppMap.size();
            for (int i = 0; i < size; i++) {
                AppInfo info = this.mFgAppMap.valueAt(i);
                if (info != null) {
                    if (info.isWhiteList) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void enterSchedLevelBoost() {
        AwareLog.d(TAG, "enterSchedLevelBoost!");
        this.mIsLcpuLimited.set(true);
        doSchedLevelBoost(true);
    }

    private void exitSchedLevelBoost() {
        AwareLog.d(TAG, "exitSchedLevelBoost!");
        this.mIsLcpuLimited.set(false);
        doSchedLevelBoost(false);
    }

    private void doSchedLevelBoost(boolean enter) {
        int i = -1;
        if (this.mFreqCmdId > 0) {
            UniPerf instance = UniPerf.getInstance();
            int i2 = this.mFreqCmdId;
            int[] iArr = new int[1];
            iArr[0] = enter ? 0 : -1;
            instance.uniPerfEvent(i2, "", iArr);
        }
        if (this.mEasCmdId > 0) {
            UniPerf instance2 = UniPerf.getInstance();
            int i3 = this.mEasCmdId;
            int[] iArr2 = new int[1];
            if (enter) {
                i = 0;
            }
            iArr2[0] = i;
            instance2.uniPerfEvent(i3, "", iArr2);
        }
    }
}
