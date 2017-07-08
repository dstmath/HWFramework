package com.android.server.rms.statistic;

import android.os.Bundle;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.util.Log;
import com.android.server.location.HwGnssLogErrorCode;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.resource.HwSysInnerResImpl;
import com.android.server.rms.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HwResStatisticImpl implements HwResStatistic {
    private static final String TAG = "HwResStatisticImpl";
    protected static final String TAG_TIMES_ANR = "Anr_times";
    protected static final String TAG_TIMES_DELAY = "Delay_times";
    protected static final String TAG_TIMES_FRAMELOST = "FrameLost_times";
    protected static final String TAG_TIMES_LEVEL = "Level_times";
    protected HwConfigReader mConfig;
    protected int mLastAnrTotalTimes;
    protected int mLastDelayTotalTimes;
    protected int mLastFrameLostTotalTimes;
    protected HashMap<String, HwResRecord> mResRecordMap;

    public static HwResStatistic getResStatistic(int groupID) {
        switch (groupID) {
            case HwGnssLogHandlerMsgID.UPDATEBINDERRORTIME /*20*/:
                return RamStatistic.getInstance();
            default:
                if (Utils.DEBUG) {
                    Log.d(TAG, "[res count] unknown groupID:" + groupID);
                }
                return null;
        }
    }

    protected static String getKeyCode(String groupName, String subTypeName, int level) {
        if (groupName == null || subTypeName == null) {
            return null;
        }
        return groupName + "_" + subTypeName + "_" + Integer.toString(level);
    }

    protected static void buildResRecordMap(HwConfigReader config, int groupID, HashMap<String, HwResRecord> resRecordMap) {
        if (config != null && resRecordMap != null) {
            String groupName = config.getGroupName(groupID);
            int subTypeNum = config.getSubTypeNum(groupID);
            for (int index = 0; index < subTypeNum; index++) {
                String subTypeName = config.getSubTypeName(groupID, index);
                ArrayList<Integer> levels = config.getSubTypeLevels(groupID, index);
                if (levels != null) {
                    for (Integer intValue : levels) {
                        buildResRecord(groupName, subTypeName, intValue.intValue(), resRecordMap);
                    }
                }
            }
        }
    }

    protected static void buildResRecord(String groupName, String subTypeName, int level, HashMap<String, HwResRecord> resRecordMap) {
        String keyCode = getKeyCode(groupName, subTypeName, level);
        if (resRecordMap != null && ((HwResRecord) resRecordMap.get(keyCode)) == null) {
            resRecordMap.put(keyCode, new HwResRecord(groupName, subTypeName, level));
        }
    }

    protected static int getAspectCount(int resourceType) {
        HwSysResource sysResource = HwSysResImpl.getResource(resourceType);
        if (sysResource == null) {
            return 0;
        }
        Bundle data = sysResource.query();
        if (data == null) {
            return 0;
        }
        switch (resourceType) {
            case HwGnssLogHandlerMsgID.INJECT_EXTRA_PARAM /*24*/:
                return data.getInt("ANRCount");
            case HwGnssLogErrorCode.GPS_DAILY_CNT_REPORT_FAILD /*25*/:
                return data.getInt("DelayCount");
            case HwGnssLogErrorCode.GPS_NTP_WRONG /*26*/:
                return data.getInt("FramelostCount");
            default:
                return 0;
        }
    }

    protected static int getLevel(HwConfigReader config, int groupID, int subType, int value) {
        int level = 0;
        for (Integer intValue : config.getSubTypeLevels(groupID, subType)) {
            int v = intValue.intValue();
            level = v;
            if (value <= v) {
                break;
            }
        }
        return level;
    }

    public boolean init(HwConfigReader config) {
        return true;
    }

    public Bundle sample(int groupID) {
        HwSysResource sysResource = HwSysInnerResImpl.getResource(groupID);
        if (sysResource != null) {
            return sysResource.query();
        }
        return null;
    }

    public boolean statistic(Bundle data) {
        return true;
    }

    public Map<String, HwResRecord> obtainResRecordMap() {
        return null;
    }

    public boolean resetResRecordMap(Map<String, HwResRecord> resRecordMap) {
        if (resRecordMap != null) {
            for (Entry entry : resRecordMap.entrySet()) {
                HwResRecord rr = (HwResRecord) entry.getValue();
                if (rr != null) {
                    rr.resetAspectData();
                }
            }
        }
        return true;
    }

    public boolean acquire(int groupID) {
        HwSysResource sysResource = HwSysInnerResImpl.getResource(groupID);
        if (sysResource == null) {
            return false;
        }
        sysResource.acquire(null, null, null);
        return true;
    }
}
