package com.android.server.mtm.iaware.appmng;

import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AwareProcessBlockInfo implements Comparable<AwareProcessBlockInfo> {
    public boolean procAlarmChk;
    public int procAppType;
    public int procClassRate;
    public boolean procCleanAlarm;
    public ProcessCleaner.CleanType procCleanType;
    public Map<String, Integer> procDetailedReason;
    public int procImportance;
    public boolean procIsNativeForceStop;
    public int procMinAdj;
    public String procPackageName;
    public List<AwareProcessInfo> procProcessList;
    public String procReason;
    public boolean procResCleanAllow;
    public int procSubClassRate;
    public String procSubTypeStr;
    public int procUid;
    public long procUpdateTime;
    public int procWeight;

    public AwareProcessBlockInfo(int uid, boolean resCleanAllow, int classRate) {
        this.procResCleanAllow = true;
        this.procCleanAlarm = false;
        this.procIsNativeForceStop = true;
        this.procWeight = -1;
        this.procAppType = -1;
        this.procAlarmChk = false;
        this.procProcessList = new ArrayList();
        this.procResCleanAllow = resCleanAllow;
        this.procClassRate = classRate;
        this.procSubTypeStr = MemoryConstant.MEM_REPAIR_CONSTANT_BASE;
        this.procUid = uid;
        this.procCleanType = ProcessCleaner.CleanType.NONE;
    }

    public AwareProcessBlockInfo(int uid) {
        this.procResCleanAllow = true;
        this.procCleanAlarm = false;
        this.procIsNativeForceStop = true;
        this.procWeight = -1;
        this.procAppType = -1;
        this.procAlarmChk = false;
        this.procUid = uid;
    }

    public AwareProcessBlockInfo(String reason, int uid, AwareProcessInfo processInfo, int cleanType, Map<String, Integer> detailedReason) {
        this.procResCleanAllow = true;
        this.procCleanAlarm = false;
        this.procIsNativeForceStop = true;
        this.procWeight = -1;
        this.procAppType = -1;
        this.procAlarmChk = false;
        this.procUid = uid;
        this.procProcessList = new ArrayList();
        this.procProcessList.add(processInfo);
        ProcessCleaner.CleanType[] values = ProcessCleaner.CleanType.values();
        if (values.length <= cleanType || cleanType < 0) {
            this.procCleanType = ProcessCleaner.CleanType.NONE;
        } else {
            this.procCleanType = values[cleanType];
        }
        if (!(processInfo == null || processInfo.procProcInfo == null)) {
            if (processInfo.procProcInfo.mPackageName.size() > 0) {
                this.procPackageName = (String) processInfo.procProcInfo.mPackageName.get(0);
            }
            this.procMinAdj = processInfo.procProcInfo.mCurAdj;
        }
        this.procReason = reason;
        this.procDetailedReason = detailedReason;
        if (processInfo != null) {
            this.procImportance = processInfo.procImportance;
            processInfo.procCleanType = this.procCleanType;
            processInfo.procReason = reason;
            processInfo.procDetailedReason = detailedReason;
        }
    }

    public void setMemGroup(int group) {
        List<AwareProcessInfo> list = this.procProcessList;
        if (list != null) {
            for (AwareProcessInfo procInfo : list) {
                if (procInfo != null) {
                    procInfo.procMemGroup = group;
                }
            }
        }
    }

    public void add(AwareProcessInfo item) {
        if (item != null) {
            if (this.procProcessList == null) {
                this.procProcessList = new ArrayList();
            }
            if (!this.procProcessList.contains(item)) {
                this.procProcessList.add(item);
            }
        }
    }

    public int getPid() {
        AwareProcessInfo info;
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty() || (info = this.procProcessList.get(0)) == null) {
            return 0;
        }
        return info.procPid;
    }

    public int getAdj() {
        AwareProcessInfo info;
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty() || (info = this.procProcessList.get(0)) == null || info.procProcInfo == null) {
            return 0;
        }
        return info.procProcInfo.mCurAdj;
    }

    public int getRate() {
        AwareProcessInfo info;
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty() || (info = this.procProcessList.get(0)) == null) {
            return 0;
        }
        return info.procClassRate;
    }

    public int getSubRate() {
        AwareProcessInfo info;
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty() || (info = this.procProcessList.get(0)) == null || info.procProcInfo == null) {
            return 0;
        }
        return info.procSubClassRate;
    }

    public boolean isSystemApp() {
        AwareProcessInfo info;
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty() || (info = this.procProcessList.get(0)) == null || info.procProcInfo == null || info.procProcInfo.mType != 2) {
            return false;
        }
        return true;
    }

    public void remove(AwareProcessInfo item) {
        List<AwareProcessInfo> list = this.procProcessList;
        if (list != null && item != null) {
            list.remove(item);
        }
    }

    public List<AwareProcessInfo> getProcessList() {
        return this.procProcessList;
    }

    public boolean isResCleanAllow() {
        return this.procResCleanAllow;
    }

    public String getClassRateStr() {
        return AwareAppMngSort.getClassRateStr(this.procClassRate);
    }

    public boolean contains(int pid) {
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (AwareProcessInfo info : this.procProcessList) {
            if (info != null && info.procPid == pid) {
                return true;
            }
        }
        return false;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this.procProcessList != null) {
            sb.append("pid=");
            for (AwareProcessInfo info : this.procProcessList) {
                sb.append(info.procPid);
                sb.append(" ");
            }
        }
        sb.append(",uid=");
        sb.append(this.procUid);
        sb.append(",pkg=");
        sb.append(this.procPackageName);
        sb.append(", cleanType=");
        sb.append(this.procCleanType.description());
        sb.append(", weight=");
        sb.append(this.procWeight);
        sb.append(", reason=");
        sb.append(this.procReason);
        return sb.toString();
    }

    public int compareTo(AwareProcessBlockInfo another) {
        if (another == null) {
            return 0;
        }
        int i = this.procClassRate;
        int i2 = another.procClassRate;
        if (i != i2) {
            return i - i2;
        }
        int i3 = this.procSubClassRate;
        int i4 = another.procSubClassRate;
        if (i3 != i4) {
            return i3 - i4;
        }
        int i5 = this.procMinAdj;
        int i6 = another.procMinAdj;
        if (i5 != i6) {
            return i5 - i6;
        }
        int i7 = this.procImportance;
        int i8 = another.procImportance;
        if (i7 != i8) {
            return i7 - i8;
        }
        return 0;
    }

    private String getProcList() {
        String result = "";
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (AwareProcessInfo proc : this.procProcessList) {
            result = proc.procProcInfo != null ? result + proc.procProcInfo.mProcessName + "&adj:" + proc.procProcInfo.mCurAdj + "&adj-type:" + proc.procProcInfo.mAdjType : result + "invalid process";
        }
        return result;
    }

    private int getUid() {
        AwareProcessInfo proc;
        List<AwareProcessInfo> list = this.procProcessList;
        if (list == null || list.isEmpty() || (proc = this.procProcessList.get(0)) == null || proc.procProcInfo == null) {
            return -1;
        }
        return proc.procProcInfo.mUid;
    }

    public String toStringWithProcName() {
        StringBuffer stringBuffer = new StringBuffer();
        if (this.procProcessList != null) {
            stringBuffer.append("pid=");
            for (AwareProcessInfo info : this.procProcessList) {
                stringBuffer.append(info.procPid);
                stringBuffer.append(" ");
            }
        }
        stringBuffer.append(",uid=");
        stringBuffer.append(getUid());
        stringBuffer.append(",pkg=");
        stringBuffer.append(this.procPackageName);
        stringBuffer.append(",procName=");
        stringBuffer.append(getProcList());
        stringBuffer.append(", cleanType=");
        stringBuffer.append(this.procCleanType.description());
        stringBuffer.append(", reason=");
        stringBuffer.append(this.procReason);
        return stringBuffer.toString();
    }
}
