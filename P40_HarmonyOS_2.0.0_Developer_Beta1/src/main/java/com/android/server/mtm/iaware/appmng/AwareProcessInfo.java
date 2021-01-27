package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Intent;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.policy.BrFilterPolicy;
import com.android.server.mtm.iaware.appmng.rule.BroadcastMngRule;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AwareProcessInfo {
    public static final int STABLE_PROVIDER_IN = 2;
    public static final int STABLE_PROVIDER_INOUT = 3;
    public static final int STABLE_PROVIDER_OUT = 1;
    private boolean isFas = false;
    private boolean isFreeze = false;
    private boolean isRare = false;
    private boolean isStandBy = false;
    private final ArrayMap<String, BrFilterPolicy> mBrPolicys = new ArrayMap<>();
    private int mState = 10;
    public int procClassRate;
    public ProcessCleaner.CleanType procCleanType;
    public Map<String, Integer> procDetailedReason;
    public boolean procHasShownUi;
    public int procImportance;
    public int procListPolicy = -1;
    public int procMemGroup;
    public int procPid;
    public ProcessInfo procProcInfo;
    public String procReason;
    public boolean procRestartFlag = false;
    public int procStableValue = 0;
    public int procSubClassRate;
    public int procSwapIndex;
    public int procTaskId = -1;
    public XmlConfig procXmlConfig;

    public static class XmlConfig {
        public int procCfgDefaultGroup;
        public boolean procFrequentlyUsed;
        public boolean procResCleanAllow;
        public boolean procRestartFlag;

        public XmlConfig(int groupId, boolean frequentUsed, boolean allowCleanRes, boolean restartFlag) {
            this.procCfgDefaultGroup = groupId;
            this.procFrequentlyUsed = frequentUsed;
            this.procResCleanAllow = allowCleanRes;
            this.procRestartFlag = restartFlag;
        }
    }

    public AwareProcessInfo(int pid, int memGroup, int importance, int classRate, ProcessInfo procInfo) {
        this.procPid = pid;
        this.procMemGroup = memGroup;
        this.procImportance = importance;
        this.procClassRate = classRate;
        this.procSubClassRate = 0;
        this.procProcInfo = procInfo;
        this.procSwapIndex = 1;
    }

    public AwareProcessInfo(int pid, ProcessInfo procInfo) {
        this.procPid = pid;
        this.procProcInfo = procInfo;
    }

    public boolean getRestartFlag() {
        if (this.procRestartFlag) {
            return true;
        }
        XmlConfig xmlConfig = this.procXmlConfig;
        if (xmlConfig == null) {
            return false;
        }
        return xmlConfig.procRestartFlag;
    }

    public String getProcessStatus() {
        if (this.procDetailedReason == null) {
            return null;
        }
        AppStatusUtils.Status[] values = AppStatusUtils.Status.values();
        Integer statType = this.procDetailedReason.get(RuleParserUtil.AppMngTag.STATUS.getDesc());
        if (statType == null || values.length <= statType.intValue() || statType.intValue() < 0) {
            return null;
        }
        return values[statType.intValue()].description();
    }

    public boolean isForegroundApp() {
        Integer statType;
        Map<String, Integer> map = this.procDetailedReason;
        if (map == null || (statType = map.get(RuleParserUtil.AppMngTag.STATUS.getDesc())) == null) {
            return false;
        }
        if (AppStatusUtils.Status.TOP_ACTIVITY.ordinal() == statType.intValue() || AppStatusUtils.Status.FOREGROUND_APP.ordinal() == statType.intValue()) {
            return true;
        }
        return false;
    }

    public boolean isVisibleApp(int visibleAppAdj) {
        ProcessInfo processInfo;
        Integer statType;
        if (this.procDetailedReason == null || (processInfo = this.procProcInfo) == null || processInfo.mCurAdj != visibleAppAdj || (statType = this.procDetailedReason.get(RuleParserUtil.AppMngTag.STATUS.getDesc())) == null || AppStatusUtils.Status.VISIBLE_APP.ordinal() != statType.intValue()) {
            return false;
        }
        return true;
    }

    public boolean isAwareProtected() {
        if (TextUtils.isEmpty(this.procReason)) {
            return false;
        }
        return AppMngConstant.CleanReason.LIST.getCode().equals(this.procReason);
    }

    public String getStatisticsInfo() {
        StringBuilder build = new StringBuilder();
        build.append("proc info pid:");
        build.append(this.procPid);
        build.append(",classRate:");
        build.append(this.procClassRate);
        build.append(",");
        build.append(getClassRateStr());
        build.append(",importance:");
        build.append(this.procImportance);
        ProcessInfo processInfo = this.procProcInfo;
        if (processInfo != null) {
            if (processInfo.mPackageName != null) {
                build.append(",packageName:");
                int size = this.procProcInfo.mPackageName.size();
                for (int i = 0; i < size; i++) {
                    build.append(" ");
                    build.append((String) this.procProcInfo.mPackageName.get(i));
                }
            }
            build.append(",procName:");
            build.append(this.procProcInfo.mProcessName);
            build.append(",adj:");
            build.append(this.procProcInfo.mCurAdj);
        }
        return build.toString();
    }

    public String getClassRateStr() {
        return AwareAppMngSort.getClassRateStr(this.procClassRate);
    }

    public static List<AwareProcessInfo> getAwareProcInfosList() {
        List<AwareProcessInfo> awareProcList = new ArrayList<>();
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
            ProcessInfo procInfo = procList.get(i);
            awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, AwareAppMngSort.ClassRate.NORMAL.ordinal(), procInfo));
        }
        return awareProcList;
    }

    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromPackage(String packageName, int userId) {
        ProcessInfoCollector processInfoCollector;
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList<>();
        if (packageName == null || userId < 0 || packageName.equals("") || (processInfoCollector = ProcessInfoCollector.getInstance()) == null) {
            return awareProcList;
        }
        ArrayList<ProcessInfo> procList = processInfoCollector.getProcessInfosFromPackage(packageName, userId);
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = procList.get(i);
            awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, AwareAppMngSort.ClassRate.NORMAL.ordinal(), procInfo));
        }
        return awareProcList;
    }

    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromPackageList(List<String> packlist, List<Integer> uidList) {
        ProcessInfoCollector processInfoCollector;
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList<>();
        if (packlist == null || uidList == null || packlist.size() != uidList.size() || (processInfoCollector = ProcessInfoCollector.getInstance()) == null) {
            return awareProcList;
        }
        ArrayMap<String, Integer> packMap = new ArrayMap<>();
        int listSize = packlist.size();
        for (int i = 0; i < listSize; i++) {
            packMap.put(packlist.get(i), uidList.get(i));
        }
        ArrayList<ProcessInfo> procList = processInfoCollector.getProcessInfosFromPackageMap(packMap);
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (int i2 = 0; i2 < size; i2++) {
            ProcessInfo procInfo = procList.get(i2);
            awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, AwareAppMngSort.ClassRate.NORMAL.ordinal(), procInfo));
        }
        return awareProcList;
    }

    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromTask(int taskId, int userId) {
        ProcessInfoCollector processInfoCollector;
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList<>();
        if (taskId < 0 || userId < 0 || (processInfoCollector = ProcessInfoCollector.getInstance()) == null) {
            return awareProcList;
        }
        ArrayList<ProcessInfo> procList = processInfoCollector.getProcessInfosFromTask(taskId, userId);
        if (procList.isEmpty()) {
            return awareProcList;
        }
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = procList.get(i);
            AwareProcessInfo awareProcInfo = new AwareProcessInfo(procInfo.mPid, 0, 0, AwareAppMngSort.ClassRate.NORMAL.ordinal(), procInfo);
            awareProcInfo.procTaskId = taskId;
            awareProcList.add(awareProcInfo);
        }
        return awareProcList;
    }

    public static ArrayList<AwareProcessInfo> getAwareProcInfosFromTaskList(List<Integer> taskIdList, List<Integer> userIdList) {
        if (taskIdList == null || userIdList == null || taskIdList.size() != userIdList.size()) {
            return null;
        }
        ArrayList<AwareProcessInfo> awareProcList = new ArrayList<>();
        int listSize = taskIdList.size();
        for (int i = 0; i < listSize; i++) {
            awareProcList.addAll(getAwareProcInfosFromTask(taskIdList.get(i).intValue(), userIdList.get(i).intValue()));
        }
        return awareProcList;
    }

    public int getState() {
        int i = this.mState;
        if (i == 1) {
            return i;
        }
        ProcessInfo processInfo = this.procProcInfo;
        if (processInfo == null || (!processInfo.mForegroundActivities && !this.procProcInfo.mForegroundServices && !this.procProcInfo.mForceToForeground)) {
            return this.mState;
        }
        return 9;
    }

    public void setState(int state) {
        switch (state) {
            case 1:
                this.isFreeze = true;
                break;
            case 2:
                this.isFreeze = false;
                break;
            case 3:
                this.isStandBy = true;
                break;
            case 4:
                this.isStandBy = false;
                break;
            case 5:
                this.isRare = true;
                break;
            case 6:
                this.isRare = false;
                break;
            case 7:
                this.isFas = true;
                break;
            case 8:
                this.isFas = false;
                break;
        }
        resetState();
    }

    private void resetState() {
        if (this.isFreeze) {
            this.mState = 1;
        } else if (this.isFas) {
            this.mState = 7;
        } else if (this.isRare) {
            this.mState = 5;
        } else if (this.isStandBy) {
            this.mState = 3;
        } else {
            this.mState = 10;
        }
    }

    public void updateProcessInfo(ProcessInfo procInfo) {
        this.procProcInfo = procInfo;
    }

    public void updateBrPolicy() {
        ArrayMap<String, RuleNode> rules;
        BrFilterPolicy brPolicy;
        BroadcastMngRule brRule = DecisionMaker.getInstance().getBroadcastMngRule(AppMngConstant.BroadcastSource.BROADCAST_FILTER);
        if (!(brRule == null || (rules = brRule.getBrRules()) == null)) {
            synchronized (this.mBrPolicys) {
                for (Map.Entry<String, RuleNode> ent : rules.entrySet()) {
                    String action = ent.getKey();
                    RuleNode node = ent.getValue();
                    BrFilterPolicy brPolicy2 = this.mBrPolicys.get(action);
                    String id = (String) this.procProcInfo.mPackageName.get(0);
                    Intent intent = new Intent(action);
                    if (brPolicy2 == null) {
                        BrFilterPolicy brPolicy3 = new BrFilterPolicy(id, 0, getState());
                        this.mBrPolicys.put(action, brPolicy3);
                        brPolicy = brPolicy3;
                    } else {
                        brPolicy = brPolicy2;
                    }
                    brPolicy.setPolicy(brRule.updateApply(AppMngConstant.BroadcastSource.BROADCAST_FILTER, id, intent, this, node));
                    brPolicy.setState(getState());
                }
            }
        }
    }

    public BrFilterPolicy getBrPolicy(Intent intent) {
        synchronized (this.mBrPolicys) {
            if (intent == null) {
                return null;
            }
            return this.mBrPolicys.get(intent.getAction());
        }
    }

    public ArrayMap<String, BrFilterPolicy> getBrFilterPolicyMap() {
        ArrayMap<String, BrFilterPolicy> arrayMap;
        synchronized (this.mBrPolicys) {
            arrayMap = new ArrayMap<>(this.mBrPolicys);
        }
        return arrayMap;
    }
}
