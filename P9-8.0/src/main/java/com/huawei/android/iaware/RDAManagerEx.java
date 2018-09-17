package com.huawei.android.iaware;

import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import java.util.List;

public class RDAManagerEx {
    private static final String TAG = "RDAManagerEx";
    private static RDAManagerEx mRDAManagerEx;

    public static synchronized RDAManagerEx getInstance() {
        RDAManagerEx rDAManagerEx;
        synchronized (RDAManagerEx.class) {
            if (mRDAManagerEx == null) {
                mRDAManagerEx = new RDAManagerEx();
            }
            rDAManagerEx = mRDAManagerEx;
        }
        return rDAManagerEx;
    }

    public int getDumpData(int time, List<DumpData> dumpData) {
        AwareLog.d(TAG, "call getDumpData.");
        return HwSysResManager.getInstance() != null ? HwSysResManager.getInstance().getDumpData(time, dumpData) : 0;
    }

    public int getStatisticsData(List<StatisticsData> statisticsData) {
        AwareLog.d(TAG, "call getStatisticsData.");
        return HwSysResManager.getInstance() != null ? HwSysResManager.getInstance().getStatisticsData(statisticsData) : 0;
    }

    public void init(Bundle bundle) {
        if (HwSysResManager.getInstance() != null) {
            AwareLog.d(TAG, "call HwSysResManager init.");
            HwSysResManager.getInstance().init(bundle);
        }
    }

    public void configUpdate() {
        if (HwSysResManager.getInstance() != null) {
            AwareLog.d(TAG, "HwSysResManager call configUpdate.");
            HwSysResManager.getInstance().configUpdate();
        }
    }

    public void enableFeature(int featureId) {
        if (HwSysResManager.getInstance() != null) {
            AwareLog.d(TAG, "HwSysResManager call enableFeature");
            HwSysResManager.getInstance().enableFeature(featureId);
        }
    }

    public void disableFeature(int featureId) {
        if (HwSysResManager.getInstance() != null) {
            AwareLog.d(TAG, "HwSysResManager call disableFeature.");
            HwSysResManager.getInstance().disableFeature(featureId);
        }
    }

    public String fetchBigData(int featureId, boolean clear) {
        AwareLog.d(TAG, "HwSysResManager call saveBigData.");
        return HwSysResManager.getInstance().saveBigData(featureId, clear);
    }

    public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) {
        AwareLog.d(TAG, "HwSysResManager call fetchBigDataByVersion.");
        return HwSysResManager.getInstance().fetchBigDataByVersion(iVer, fId, beta, clear);
    }

    public void updateFakeForegroundList(List<String> processList) {
        AwareLog.d(TAG, "HwSysResManager call updateFakeForegroundList.");
        HwSysResManager.getInstance().updateFakeForegroundList(processList);
    }

    public boolean isFakeForegroundProcess(String process) {
        AwareLog.d(TAG, "HwSysResManager call isFakeForegroundProcess.");
        return HwSysResManager.getInstance().isFakeForegroundProcess(process);
    }

    public int getPid(String procName) {
        AwareLog.d(TAG, "HwSysResManager call getpid.");
        return HwSysResManager.getInstance().getPid(procName);
    }

    public long getPss(int pid) {
        AwareLog.d(TAG, "HwSysResManager call getPss.");
        return HwSysResManager.getInstance().getPss(pid);
    }

    public void reportData(CollectData data) {
        HwSysResManager.getInstance().reportData(data);
    }

    public void reportHabitData(ParceledListSlice habitData) {
        HwSysResManager.getInstance().reportHabitData(habitData);
    }

    public void reportAppType(String pkgName, int appType, boolean status, int attr) {
        HwSysResManager.getInstance().reportAppType(pkgName, appType, status, attr);
    }

    public List<String> getLongTimeRunningApps() {
        return HwSysResManager.getInstance().getLongTimeRunningApps();
    }

    public long getMemAvaliable() {
        AwareLog.d(TAG, "HwSysResManager call getMemAvaliable.");
        return HwSysResManager.getInstance().getMemAvaliable();
    }

    public List<String> getMostFrequentUsedApps(int n, int minCount) {
        AwareLog.d(TAG, "HwSysResManager call getMostFrequentUsedApps.");
        return HwSysResManager.getInstance().getMostFrequentUsedApps(n, minCount);
    }

    public List<MemRepairPkgInfo> getMemRepairProcGroup(int sceneType) {
        AwareLog.d(TAG, "HwSysResManager call getMemRepairProcGroup.");
        return HwSysResManager.getInstance().getMemRepairProcGroup(sceneType);
    }

    public void triggerUpdateWhiteList() {
        HwSysResManager.getInstance().triggerUpdateWhiteList();
    }

    public void custConfigUpdate() {
        AwareLog.d(TAG, "HwSysResManager call custConfigUpdate.");
        HwSysResManager.getInstance().custConfigUpdate();
    }
}
