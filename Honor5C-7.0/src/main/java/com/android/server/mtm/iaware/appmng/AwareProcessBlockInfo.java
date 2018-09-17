package com.android.server.mtm.iaware.appmng;

import java.util.ArrayList;
import java.util.List;

public class AwareProcessBlockInfo implements Comparable<AwareProcessBlockInfo> {
    public boolean mAlarmChk;
    public int mAppType;
    public int mClassRate;
    public boolean mCleanAlarm;
    public int mImportance;
    public int mMinAdj;
    public List<AwareProcessInfo> mProcessList;
    public boolean mResCleanAllow;
    public int mSubClassRate;
    public String mSubTypeStr;
    public int mUid;
    public long mUpdateTime;

    public AwareProcessBlockInfo(int uid, boolean resCleanAllow, int classRate) {
        this.mResCleanAllow = false;
        this.mCleanAlarm = false;
        this.mAppType = -1;
        this.mAlarmChk = false;
        this.mProcessList = new ArrayList();
        this.mResCleanAllow = resCleanAllow;
        this.mClassRate = classRate;
        this.mSubTypeStr = "base";
        this.mUid = uid;
    }

    public void setMemGroup(int group) {
        if (this.mProcessList != null) {
            for (AwareProcessInfo procInfo : this.mProcessList) {
                if (procInfo != null) {
                    procInfo.mMemGroup = group;
                }
            }
        }
    }

    public AwareProcessBlockInfo(int uid) {
        this.mResCleanAllow = false;
        this.mCleanAlarm = false;
        this.mAppType = -1;
        this.mAlarmChk = false;
        this.mUid = uid;
    }

    public void add(AwareProcessInfo item) {
        if (item != null) {
            if (this.mProcessList == null) {
                this.mProcessList = new ArrayList();
            }
            if (!this.mProcessList.contains(item)) {
                this.mProcessList.add(item);
            }
        }
    }

    public int getPid() {
        int pid = 0;
        if (this.mProcessList == null || this.mProcessList.isEmpty()) {
            return 0;
        }
        AwareProcessInfo info = (AwareProcessInfo) this.mProcessList.get(0);
        if (info != null) {
            pid = info.mPid;
        }
        return pid;
    }

    public int getAdj() {
        int adj = 0;
        if (this.mProcessList == null || this.mProcessList.isEmpty()) {
            return 0;
        }
        AwareProcessInfo info = (AwareProcessInfo) this.mProcessList.get(0);
        if (!(info == null || info.mProcInfo == null)) {
            adj = info.mProcInfo.mCurAdj;
        }
        return adj;
    }

    public int getRate() {
        int classRate = 0;
        if (this.mProcessList == null || this.mProcessList.isEmpty()) {
            return 0;
        }
        AwareProcessInfo info = (AwareProcessInfo) this.mProcessList.get(0);
        if (info != null) {
            classRate = info.mClassRate;
        }
        return classRate;
    }

    public int getSubRate() {
        int subRate = 0;
        if (this.mProcessList == null || this.mProcessList.isEmpty()) {
            return 0;
        }
        AwareProcessInfo info = (AwareProcessInfo) this.mProcessList.get(0);
        if (!(info == null || info.mProcInfo == null)) {
            subRate = info.mSubClassRate;
        }
        return subRate;
    }

    public boolean isSystemApp() {
        boolean z = false;
        if (this.mProcessList == null || this.mProcessList.isEmpty()) {
            return false;
        }
        AwareProcessInfo info = (AwareProcessInfo) this.mProcessList.get(0);
        if (!(info == null || info.mProcInfo == null || info.mProcInfo.mType != 2)) {
            z = true;
        }
        return z;
    }

    public void remove(AwareProcessInfo item) {
        if (this.mProcessList != null && item != null) {
            this.mProcessList.remove(item);
        }
    }

    public List<AwareProcessInfo> getProcessList() {
        return this.mProcessList;
    }

    public boolean isResCleanAllow() {
        return this.mResCleanAllow;
    }

    public String getClassRateStr() {
        return AwareAppMngSort.getClassRateStr(this.mClassRate);
    }

    public boolean contains(int pid) {
        if (this.mProcessList == null || this.mProcessList.isEmpty()) {
            return false;
        }
        for (AwareProcessInfo info : this.mProcessList) {
            if (info != null && info.mPid == pid) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public int compareTo(AwareProcessBlockInfo another) {
        if (another == null) {
            return 0;
        }
        if (this.mClassRate != another.mClassRate) {
            return this.mClassRate - another.mClassRate;
        }
        if (this.mSubClassRate != another.mSubClassRate) {
            return this.mSubClassRate - another.mSubClassRate;
        }
        if (this.mMinAdj != another.mMinAdj) {
            return this.mMinAdj - another.mMinAdj;
        }
        if (this.mImportance != another.mImportance) {
            return this.mImportance - another.mImportance;
        }
        return 0;
    }
}
