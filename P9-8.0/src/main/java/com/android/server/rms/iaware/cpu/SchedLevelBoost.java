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
    private int mEnterCmdid;
    private int mExitCmdid;
    private SparseArray<AppInfo> mFgAppMap = new SparseArray();
    private int mFreqCmdid;
    private AtomicBoolean mIsScreenOn = new AtomicBoolean(true);
    private Set<String> mWhiteList = new ArraySet();

    static class AppInfo {
        boolean isWhiteList;
        int pid;
        int uid;

        public AppInfo(int pid, int uid, boolean isWhiteList) {
            this.pid = pid;
            this.uid = uid;
            this.isWhiteList = isWhiteList;
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

    public void setParams(String strEnterCmdid, String strExitCmdid, String strFreqCmdid) {
        this.mEnterCmdid = parseInt(strEnterCmdid);
        this.mExitCmdid = parseInt(strExitCmdid);
        this.mFreqCmdid = parseInt(strFreqCmdid);
    }

    private int parseInt(String strValue) {
        int value = -1;
        if (strValue == null) {
            AwareLog.e(TAG, "parseInt strValue is null!");
            return value;
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
            String pkgName = InnerUtils.getAwarePkgName(pid);
            boolean isLastWhiteList = isLastWhiteList();
            boolean isWhiteList = isWhiteList(pkgName);
            if (isWhiteList && (isLastWhiteList ^ 1) != 0 && this.mIsScreenOn.get()) {
                enterSchedLevelBoost();
            }
            this.mFgAppMap.put(pid, new AppInfo(pid, uid, isWhiteList));
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0012, code:
            return;
     */
    /* JADX WARNING: Missing block: B:20:0x0032, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeFgProc(int pid, int uid) {
        synchronized (this.mFgAppMap) {
            AppInfo info = (AppInfo) this.mFgAppMap.get(pid);
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

    /* JADX WARNING: Missing block: B:14:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                AppInfo info = (AppInfo) this.mFgAppMap.valueAt(i);
                if (info != null && info.isWhiteList) {
                    return true;
                }
            }
            return false;
        }
    }

    private void enterSchedLevelBoost() {
        AwareLog.d(TAG, "enterSchedLevelBoost!");
        doSchedLevelBoost(true);
    }

    private void exitSchedLevelBoost() {
        AwareLog.d(TAG, "exitSchedLevelBoost!");
        doSchedLevelBoost(false);
    }

    private void doSchedLevelBoost(boolean enter) {
        if (this.mFreqCmdid > 0) {
            UniPerf instance = UniPerf.getInstance();
            int i = this.mFreqCmdid;
            String str = "";
            int[] iArr = new int[1];
            iArr[0] = enter ? 0 : -1;
            instance.uniPerfEvent(i, str, iArr);
        }
        if (this.mEnterCmdid > 0 && this.mExitCmdid > 0) {
            UniPerf.getInstance().uniPerfEvent(enter ? this.mEnterCmdid : this.mExitCmdid, "", new int[0]);
        }
    }
}
