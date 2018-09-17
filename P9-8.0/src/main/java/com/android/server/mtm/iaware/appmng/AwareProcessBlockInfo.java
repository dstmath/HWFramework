package com.android.server.mtm.iaware.appmng;

import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AwareProcessBlockInfo implements Comparable<AwareProcessBlockInfo> {
    public boolean mAlarmChk;
    public int mAppType;
    public int mClassRate;
    public boolean mCleanAlarm;
    public CleanType mCleanType;
    public Map<String, Integer> mDetailedReason;
    public int mImportance;
    public boolean mIsNativeForceStop;
    public int mMinAdj;
    public String mPackageName;
    public List<AwareProcessInfo> mProcessList;
    public String mReason;
    public boolean mResCleanAllow;
    public int mSubClassRate;
    public String mSubTypeStr;
    public int mUid;
    public long mUpdateTime;
    public int mWeight;

    public AwareProcessBlockInfo(int uid, boolean resCleanAllow, int classRate) {
        this.mResCleanAllow = true;
        this.mCleanAlarm = false;
        this.mIsNativeForceStop = true;
        this.mWeight = -1;
        this.mAppType = -1;
        this.mAlarmChk = false;
        this.mProcessList = new ArrayList();
        this.mResCleanAllow = resCleanAllow;
        this.mClassRate = classRate;
        this.mSubTypeStr = MemoryConstant.MEM_REPAIR_CONSTANT_BASE;
        this.mUid = uid;
        this.mCleanType = CleanType.NONE;
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
        this.mResCleanAllow = true;
        this.mCleanAlarm = false;
        this.mIsNativeForceStop = true;
        this.mWeight = -1;
        this.mAppType = -1;
        this.mAlarmChk = false;
        this.mUid = uid;
    }

    public AwareProcessBlockInfo(String reason, int uid, AwareProcessInfo processInfo, int cleanType, Map<String, Integer> detailedReason, int weight) {
        this(reason, uid, processInfo, cleanType, detailedReason);
        this.mWeight = weight;
    }

    public AwareProcessBlockInfo(String reason, int uid, AwareProcessInfo processInfo, int cleanType, Map<String, Integer> detailedReason) {
        this.mResCleanAllow = true;
        this.mCleanAlarm = false;
        this.mIsNativeForceStop = true;
        this.mWeight = -1;
        this.mAppType = -1;
        this.mAlarmChk = false;
        this.mUid = uid;
        this.mProcessList = new ArrayList();
        this.mProcessList.add(processInfo);
        CleanType[] values = CleanType.values();
        if (values.length <= cleanType || cleanType < 0) {
            this.mCleanType = CleanType.NONE;
        } else {
            this.mCleanType = values[cleanType];
        }
        if (!(processInfo == null || processInfo.mProcInfo == null)) {
            if (processInfo.mProcInfo.mPackageName.size() > 0) {
                this.mPackageName = (String) processInfo.mProcInfo.mPackageName.get(0);
            }
            this.mMinAdj = processInfo.mProcInfo.mCurAdj;
        }
        this.mReason = reason;
        this.mDetailedReason = detailedReason;
        if (processInfo != null) {
            this.mImportance = processInfo.mImportance;
            processInfo.mCleanType = this.mCleanType;
            processInfo.mReason = reason;
            processInfo.mDetailedReason = detailedReason;
        }
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

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this.mProcessList != null) {
            sb.append("pid=");
            for (AwareProcessInfo info : this.mProcessList) {
                sb.append(info.mPid).append(" ");
            }
        }
        sb.append(",uid=").append(this.mUid).append(",pkg=").append(this.mPackageName).append(", cleanType=").append(this.mCleanType.description()).append(", weight=").append(this.mWeight).append(", reason=").append(this.mReason);
        return sb.toString();
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
