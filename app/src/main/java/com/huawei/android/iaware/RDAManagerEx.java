package com.huawei.android.iaware;

import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
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

    public void updateFakeForegroundList(List<String> processList) {
        AwareLog.d(TAG, "HwSysResManager call updateFakeForegroundList.");
        HwSysResManager.getInstance().updateFakeForegroundList(processList);
    }

    public boolean isFakeForegroundProcess(String process) {
        AwareLog.d(TAG, "HwSysResManager call isFakeForegroundProcess.");
        return HwSysResManager.getInstance().isFakeForegroundProcess(process);
    }
}
