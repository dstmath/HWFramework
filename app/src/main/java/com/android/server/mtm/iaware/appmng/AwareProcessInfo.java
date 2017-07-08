package com.android.server.mtm.iaware.appmng;

import com.android.server.mtm.taskstatus.ProcessInfo;

public class AwareProcessInfo {
    public int mClassRate;
    public boolean mHasShownUi;
    public int mImportance;
    public int mMemGroup;
    public int mPid;
    public ProcessInfo mProcInfo;
    public boolean mRestartFlag;
    public int mSubClassRate;
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
        this.mRestartFlag = false;
        this.mPid = pid;
        this.mMemGroup = memGroup;
        this.mImportance = importance;
        this.mClassRate = classRate;
        this.mSubClassRate = 0;
        this.mProcInfo = procInfo;
    }

    public String getStatisticsInfo() {
        StringBuilder build = new StringBuilder();
        build.append("proc info pid:").append(this.mPid).append(",classRate:").append(this.mClassRate).append(",").append(getClassRateStr()).append(",importance:").append(this.mImportance);
        if (this.mProcInfo != null) {
            if (this.mProcInfo.mPackageName != null) {
                build.append(",packageName:");
                for (String pkg : this.mProcInfo.mPackageName) {
                    build.append(" ").append(pkg);
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
}
