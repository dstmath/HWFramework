package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant.CleanReason;
import android.text.TextUtils;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort.ClassRate;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwareProcessInfo {
    public int mClassRate;
    public CleanType mCleanType;
    public Map<String, Integer> mDetailedReason;
    public boolean mHasShownUi;
    public int mImportance;
    public int mMemGroup;
    public int mPid;
    public ProcessInfo mProcInfo;
    public String mReason;
    public boolean mRestartFlag = false;
    public int mSubClassRate;
    public int mTaskId = -1;
    public XmlConfig mXmlConfig;

    public static class XmlConfig {
        public int mCfgDefaultGroup;
        public boolean mFrequentlyUsed;
        public boolean mResCleanAllow;
        public boolean mRestartFlag;

        public XmlConfig(int groupId, boolean frequentUsed, boolean allowCleanRes, boolean restartFlag) {
            this.mCfgDefaultGroup = groupId;
            this.mFrequentlyUsed = frequentUsed;
            this.mResCleanAllow = allowCleanRes;
            this.mRestartFlag = restartFlag;
        }
    }

    public boolean getRestartFlag() {
        if (this.mRestartFlag) {
            return true;
        }
        if (this.mXmlConfig == null) {
            return false;
        }
        return this.mXmlConfig.mRestartFlag;
    }

    public AwareProcessInfo(int pid, int memGroup, int importance, int classRate, ProcessInfo procInfo) {
        this.mPid = pid;
        this.mMemGroup = memGroup;
        this.mImportance = importance;
        this.mClassRate = classRate;
        this.mSubClassRate = 0;
        this.mProcInfo = procInfo;
    }

    public AwareProcessInfo(int pid, ProcessInfo procInfo) {
        this.mPid = pid;
        this.mProcInfo = procInfo;
    }

    public String getProcessStatus() {
        if (this.mDetailedReason == null) {
            return null;
        }
        Status[] values = Status.values();
        Integer statType = (Integer) this.mDetailedReason.get(AppMngTag.STATUS.getDesc());
        if (statType == null || values.length <= statType.intValue() || statType.intValue() < 0) {
            return null;
        }
        return values[statType.intValue()].description();
    }

    public boolean isForegroundApp() {
        if (this.mDetailedReason == null) {
            return false;
        }
        Integer statType = (Integer) this.mDetailedReason.get(AppMngTag.STATUS.getDesc());
        if (statType == null) {
            return false;
        }
        return Status.TOP_ACTIVITY.ordinal() == statType.intValue() || Status.FOREGROUND_APP.ordinal() == statType.intValue();
    }

    public boolean isVisibleApp(int visibleAppAdj) {
        if (this.mDetailedReason == null || this.mProcInfo == null || this.mProcInfo.mCurAdj != visibleAppAdj) {
            return false;
        }
        Integer statType = (Integer) this.mDetailedReason.get(AppMngTag.STATUS.getDesc());
        if (statType != null && Status.VISIBLE_APP.ordinal() == statType.intValue()) {
            return true;
        }
        return false;
    }

    public boolean isAwareProtected() {
        if (TextUtils.isEmpty(this.mReason)) {
            return false;
        }
        return CleanReason.LIST.getCode().equals(this.mReason);
    }

    public String getStatisticsInfo() {
        StringBuilder build = new StringBuilder();
        build.append("proc info pid:").append(this.mPid).append(",classRate:").append(this.mClassRate).append(",").append(getClassRateStr()).append(",importance:").append(this.mImportance);
        if (this.mProcInfo != null) {
            if (this.mProcInfo.mPackageName != null) {
                build.append(",packageName:");
                int size = this.mProcInfo.mPackageName.size();
                for (int i = 0; i < size; i++) {
                    build.append(" ").append((String) this.mProcInfo.mPackageName.get(i));
                }
            }
            build.append(",procName:").append(this.mProcInfo.mProcessName);
            build.append(",adj:").append(this.mProcInfo.mCurAdj);
        }
        return build.toString();
    }

    public String getClassRateStr() {
        return AwareAppMngSort.getClassRateStr(this.mClassRate);
    }

    public static List<AwareProcessInfo> getAwareProcInfosList() {
        List<AwareProcessInfo> awareProcList = new ArrayList();
        ProcessInfoCollector processInfoCollector = ProcessInfoCollector.getInstance();
        if (processInfoCollector == null) {
            return awareProcList;
        }
        List<ProcessInfo> procList = processInfoCollector.getProcessInfoList();
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = (ProcessInfo) procList.get(i);
            awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo));
        }
        return awareProcList;
    }

    /* JADX WARNING: Missing block: B:3:0x000a, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromPackage(String packageName, int userId) {
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList();
        if (packageName == null || userId < 0 || packageName.equals("")) {
            return awareProcList;
        }
        ProcessInfoCollector processInfoCollector = ProcessInfoCollector.getInstance();
        if (processInfoCollector == null) {
            return awareProcList;
        }
        ArrayList<ProcessInfo> procList = processInfoCollector.getProcessInfosFromPackage(packageName, userId);
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = (ProcessInfo) procList.get(i);
            awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo));
        }
        return awareProcList;
    }

    /* JADX WARNING: Missing block: B:3:0x000a, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromPackageList(List<String> packlist, List<Integer> uidList) {
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList();
        if (packlist == null || uidList == null || packlist.size() != uidList.size()) {
            return awareProcList;
        }
        ProcessInfoCollector processInfoCollector = ProcessInfoCollector.getInstance();
        if (processInfoCollector == null) {
            return awareProcList;
        }
        int i;
        HashMap<String, Integer> packMap = new HashMap();
        int listSize = packlist.size();
        for (i = 0; i < listSize; i++) {
            packMap.put((String) packlist.get(i), (Integer) uidList.get(i));
        }
        ArrayList<ProcessInfo> procList = processInfoCollector.getProcessInfosFromPackageMap(packMap);
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (i = 0; i < size; i++) {
            ProcessInfo procInfo = (ProcessInfo) procList.get(i);
            awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo));
        }
        return awareProcList;
    }

    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromTask(int taskId, int userId) {
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList();
        if (taskId < 0 || userId < 0) {
            return awareProcList;
        }
        ProcessInfoCollector processInfoCollector = ProcessInfoCollector.getInstance();
        if (processInfoCollector == null) {
            return awareProcList;
        }
        ArrayList<ProcessInfo> procList = processInfoCollector.getProcessInfosFromTask(taskId, userId);
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = (ProcessInfo) procList.get(i);
            AwareProcessInfo awareProcInfo = new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo);
            awareProcInfo.mTaskId = taskId;
            awareProcList.add(awareProcInfo);
        }
        return awareProcList;
    }

    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromTaskList(List<Integer> taskIdList, List<Integer> userIdList) {
        if (taskIdList == null || userIdList == null || taskIdList.size() != userIdList.size()) {
            return null;
        }
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList();
        int listSize = taskIdList.size();
        for (int i = 0; i < listSize; i++) {
            awareProcList.addAll(getAwareProcInfosFromTask(((Integer) taskIdList.get(i)).intValue(), ((Integer) userIdList.get(i)).intValue()));
        }
        return awareProcList;
    }
}
