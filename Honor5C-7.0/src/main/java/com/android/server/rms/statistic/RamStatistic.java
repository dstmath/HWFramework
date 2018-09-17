package com.android.server.rms.statistic;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.utils.Utils;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wm.HwWindowManagerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RamStatistic extends HwResStatisticImpl {
    private static final String TAG = "RamStatistic";
    private static RamStatistic mRamStatistic;

    public static synchronized RamStatistic getInstance() {
        RamStatistic ramStatistic;
        synchronized (RamStatistic.class) {
            if (mRamStatistic == null) {
                mRamStatistic = new RamStatistic();
                if (Utils.DEBUG) {
                    Log.d(TAG, "[res count] create RamStatistic object");
                }
            }
            ramStatistic = mRamStatistic;
        }
        return ramStatistic;
    }

    public boolean init(HwConfigReader config) {
        this.mConfig = config;
        this.mResRecordMap = new HashMap();
        HwResStatisticImpl.buildResRecordMap(this.mConfig, 20, this.mResRecordMap);
        HwResStatisticImpl.buildResRecordMap(this.mConfig, 100, this.mResRecordMap);
        HwResStatisticImpl.buildResRecordMap(this.mConfig, WifiProCommonDefs.TYEP_HAS_INTERNET, this.mResRecordMap);
        this.mLastDelayTotalTimes = HwResStatisticImpl.getAspectCount(25);
        this.mLastFrameLostTotalTimes = HwResStatisticImpl.getAspectCount(26);
        this.mLastAnrTotalTimes = HwResStatisticImpl.getAspectCount(24);
        return true;
    }

    public Bundle sample(int groupID) {
        return super.sample(groupID);
    }

    public boolean statistic(Bundle data) {
        if (Utils.DEBUG) {
            Log.d(TAG, "[res count] ram statistic ====");
        }
        ArrayList<Pair<String, Integer>> aspects = composeAspect();
        statisticMemData(20, data, aspects);
        statisticMemData(100, data, aspects);
        statisticMemData(WifiProCommonDefs.TYEP_HAS_INTERNET, data, aspects);
        return true;
    }

    public Map<String, HwResRecord> obtainResRecordMap() {
        return this.mResRecordMap;
    }

    public boolean resetResRecordMap(Map<String, HwResRecord> resRecordMap) {
        return super.resetResRecordMap(resRecordMap);
    }

    public boolean acquire(int groupID) {
        return super.acquire(groupID);
    }

    private void statisticMemData(int groupID, Bundle data, ArrayList<Pair<String, Integer>> aspects) {
        if (data != null && this.mConfig != null) {
            String groupName = this.mConfig.getGroupName(groupID);
            int subTypeNum = this.mConfig.getSubTypeNum(groupID);
            for (int subType = 0; subType < subTypeNum; subType++) {
                String subTypeName = this.mConfig.getSubTypeName(groupID, subType);
                int value = parseValue(data, groupID, subTypeName);
                if (value >= 0) {
                    int level = HwResStatisticImpl.getLevel(this.mConfig, groupID, subType, value);
                    HwResRecord resRecord = (HwResRecord) this.mResRecordMap.get(HwResStatisticImpl.getKeyCode(groupName, subTypeName, level));
                    if (resRecord != null) {
                        resRecord.updateAspectData(aspects);
                    }
                    if (Utils.DEBUG) {
                        Log.d(TAG, "[res count] ram statistic groupName:" + groupName + " subTypeName:" + subTypeName + " value:" + value + " level:" + level + " resRecord:" + resRecord);
                    }
                }
            }
        }
    }

    private int parseValue(Bundle data, int groupID, String name) {
        int value = -1;
        if (data == null || name == null) {
            return -1;
        }
        switch (groupID) {
            case HwGnssLogHandlerMsgID.UPDATEBINDERRORTIME /*20*/:
            case WifiProCommonDefs.TYEP_HAS_INTERNET /*101*/:
                value = (int) (data.getLong(name, -1) >> 10);
                break;
            case HwWindowManagerService.ROG_FREEZE_TIMEOUT /*100*/:
                value = data.getInt(name, -1);
                break;
        }
        return value;
    }

    private ArrayList<Pair<String, Integer>> composeAspect() {
        int delayTotalTimes = HwResStatisticImpl.getAspectCount(25);
        int frameLostTotalTimes = HwResStatisticImpl.getAspectCount(26);
        int anrTotalTimes = HwResStatisticImpl.getAspectCount(24);
        int delayTimes = delayTotalTimes - this.mLastDelayTotalTimes;
        int frameLostTimes = frameLostTotalTimes - this.mLastFrameLostTotalTimes;
        int anrTimes = anrTotalTimes - this.mLastAnrTotalTimes;
        this.mLastDelayTotalTimes = delayTotalTimes;
        this.mLastFrameLostTotalTimes = frameLostTotalTimes;
        this.mLastAnrTotalTimes = anrTotalTimes;
        ArrayList<Pair<String, Integer>> data = new ArrayList();
        data.add(Pair.create("Level_times", Integer.valueOf(1)));
        data.add(Pair.create("Delay_times", Integer.valueOf(delayTimes)));
        data.add(Pair.create("FrameLost_times", Integer.valueOf(frameLostTimes)));
        data.add(Pair.create("Anr_times", Integer.valueOf(anrTimes)));
        return data;
    }
}
