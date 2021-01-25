package com.android.server.rms.iaware.memory.utils;

import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.Pair;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.bigdata.BigDataSupervisor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public final class BigDataStore extends BigDataSupervisor {
    private static final Object LOCK = new Object();
    private static final int MAX_BIGDATA_ITEM = 20;
    private static final String MEMORY_BG_PROCS = "memory_bg_procs";
    private static final String MEMORY_COLD_WARM_LAUNCH = "memory_cold_warm_launch";
    private static final String MEMORY_MMONITOR = "memory_mmonitor";
    private static final String TAG = "BigDataStore";
    private static BigDataStore sInstance;
    public long aboveThresholdTime = 0;
    public long belowThresholdTime = 0;
    public long belowThresholdTimeBegin = 0;
    public long coldLaunch = 0;
    public long lmkOccurCount = 0;
    public long lmkOccurCountStash = 0;
    public long lowMemoryManageCount = 0;
    private ArrayList<Pair<Long, Long>> mBgProcessSavedList = new ArrayList<>();
    private ArrayList<Pair<Long, Long>> mColdWarmLaunchSavedList = new ArrayList<>();
    private long mTimeStamp = System.currentTimeMillis();
    public long meminfoAllocCount = 0;
    public long meminfoAllocCountStash = 0;
    public long slowPathAllocCount = 0;
    public long slowPathAllocCountStash = 0;
    private long totalActivity = 0;
    private long totalColdLaunch = 0;
    private long totalService = 0;
    public long totalTimeBegin = 0;
    public long totalTimeEnd = 0;
    private long totalWarmLaunch = 0;
    public long warmLaunch = 0;

    private BigDataStore() {
    }

    public static BigDataStore getInstance() {
        BigDataStore bigDataStore;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new BigDataStore();
            }
            bigDataStore = sInstance;
        }
        return bigDataStore;
    }

    public long getLmkOccurCount() {
        this.lmkOccurCount = MemoryReader.getLmkOccurCount();
        return this.lmkOccurCount;
    }

    public void getMeminfoAllocCount() {
        try {
            for (String str : MemoryReader.getMeminfoAllocCount()) {
                try {
                    String[] temp = str.split(":");
                    if (temp.length == 2) {
                        if ("Total page alloc count".equals(temp[0])) {
                            this.meminfoAllocCount = Long.parseLong(temp[1]);
                        } else if ("Total slow path page alloc count".equals(temp[0])) {
                            this.slowPathAllocCount = Long.parseLong(temp[1]);
                        }
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "NumberFormatException...");
                }
            }
        } catch (NumberFormatException e2) {
            AwareLog.e(TAG, "NumberFormatException...");
        }
    }

    public String getColdWarmLaunchData(boolean clear) {
        ArrayList<Pair<Long, Long>> arrayList = this.mColdWarmLaunchSavedList;
        if (arrayList == null) {
            return null;
        }
        arrayList.add(Pair.create(Long.valueOf(this.coldLaunch), Long.valueOf(this.warmLaunch)));
        this.coldLaunch = 0;
        this.warmLaunch = 0;
        StringBuilder sb = new StringBuilder();
        int size = 20;
        if (this.mColdWarmLaunchSavedList.size() <= 20) {
            size = this.mColdWarmLaunchSavedList.size();
        }
        for (int i = 0; i < size; i++) {
            this.totalColdLaunch += ((Long) this.mColdWarmLaunchSavedList.get(i).first).longValue();
            this.totalWarmLaunch += ((Long) this.mColdWarmLaunchSavedList.get(i).second).longValue();
        }
        sb.append("\"coldLaunch\":\"");
        long j = this.totalColdLaunch;
        if (j < 0) {
            j = 0;
        }
        sb.append(j);
        sb.append("\",\"warmLaunch\":\"");
        long j2 = this.totalWarmLaunch;
        if (j2 < 0) {
            j2 = 0;
        }
        sb.append(j2);
        sb.append("\"");
        if (clear) {
            this.mColdWarmLaunchSavedList = null;
            this.totalColdLaunch = 0;
            this.totalWarmLaunch = 0;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "getColdWarmLaunchData: " + sb.toString());
        }
        return sb.toString();
    }

    public String getBgAppData(boolean clear) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "getBgAppData. clear: " + clear);
        }
        getBgAppDataInternal();
        StringBuilder sb = new StringBuilder();
        int size = 20;
        if (this.mBgProcessSavedList.size() <= 20) {
            size = this.mBgProcessSavedList.size();
        }
        for (int i = 0; i < size; i++) {
            this.totalActivity += ((Long) this.mBgProcessSavedList.get(i).first).longValue();
            this.totalService += ((Long) this.mBgProcessSavedList.get(i).second).longValue();
        }
        sb.append("\"activity\":\"");
        sb.append(this.totalActivity);
        sb.append("\",\"services\":\"");
        sb.append(this.totalService);
        sb.append("\"");
        if (clear) {
            this.mBgProcessSavedList = null;
            this.totalActivity = 0;
            this.totalService = 0;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "getBgAppData: " + sb.toString());
        }
        return sb.toString();
    }

    private void getBgAppDataInternal() {
        List<AwareProcessInfo> procs;
        if (this.mBgProcessSavedList == null) {
            this.mBgProcessSavedList = new ArrayList<>();
        }
        AwareAppMngSortPolicy policy = MemoryUtils.getAppMngSortPolicy(0, 3);
        if (policy != null) {
            List<AwareProcessBlockInfo> procGroup = new ArrayList<>();
            List<AwareProcessBlockInfo> allowStops = MemoryUtils.getAppMngProcGroup(policy, 2);
            int i = 1;
            List<AwareProcessBlockInfo> shortageStops = MemoryUtils.getAppMngProcGroup(policy, 1);
            if (allowStops != null) {
                procGroup.addAll(allowStops);
            }
            if (shortageStops != null) {
                procGroup.addAll(shortageStops);
            }
            if (!procGroup.isEmpty()) {
                Set<Integer> pidSet = new ArraySet<>();
                long activityCount = 0;
                long serviceCount = 0;
                for (AwareProcessBlockInfo block : procGroup) {
                    if (block == null || (procs = block.getProcessList()) == null || procs.size() < i) {
                        i = 1;
                    } else {
                        for (AwareProcessInfo info : procs) {
                            if (isValidAwareProcessInfo(info)) {
                                if (!pidSet.contains(Integer.valueOf(info.procPid))) {
                                    if (info.procHasShownUi) {
                                        activityCount++;
                                    } else {
                                        serviceCount++;
                                    }
                                    pidSet.add(Integer.valueOf(info.procPid));
                                }
                            }
                        }
                        i = 1;
                    }
                }
                this.mBgProcessSavedList.add(Pair.create(Long.valueOf(activityCount), Long.valueOf(serviceCount)));
            }
        }
    }

    private boolean isValidAwareProcessInfo(AwareProcessInfo info) {
        return (info == null || info.procProcInfo == null || info.procProcInfo.mPackageName == null || info.procPid <= 0) ? false : true;
    }

    public String makeMemBigData(boolean clearData, long timeNow) {
        StringBuilder builder = new StringBuilder();
        String result = MemoryReader.getMmonitorData();
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "result=" + result);
        }
        if (result != null) {
            builder.append("{");
            builder.append(createHeadMsg(MEMORY_MMONITOR, timeNow));
            builder.append("\"data\":");
            builder.append(result);
            builder.append("}");
            builder.append(MemoryConstant.LINE_SEPARATOR);
        }
        String result2 = getColdWarmLaunchData(clearData);
        if (result2 != null) {
            builder.append("{");
            builder.append(createHeadMsg(MEMORY_COLD_WARM_LAUNCH, timeNow));
            builder.append(result2);
            builder.append("}");
            builder.append(MemoryConstant.LINE_SEPARATOR);
        }
        builder.append("{");
        builder.append(createHeadMsg(MEMORY_BG_PROCS, timeNow));
        builder.append(getBgAppData(clearData));
        builder.append("}");
        this.mTimeStamp = timeNow;
        return builder.toString();
    }

    private String createHeadMsg(String featureName, long endTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"feature\":\"");
        sb.append(featureName);
        sb.append("\",");
        sb.append("\"start\":");
        long j = this.mTimeStamp;
        if (j <= 0) {
            j = endTime;
        }
        sb.append(j);
        sb.append(",");
        sb.append("\"end\":");
        sb.append(endTime);
        sb.append(",");
        return sb.toString();
    }

    public String creatJsonStr() {
        JSONObject memoryAllocateJson = new JSONObject();
        JSONObject memoryControlAndLMKJson = new JSONObject();
        JSONObject availMemoryTimeJson = new JSONObject();
        try {
            memoryAllocateJson.put("memoryAllocCount", String.valueOf(this.meminfoAllocCount - this.meminfoAllocCountStash));
            memoryAllocateJson.put("slowPathAllocCount", String.valueOf(this.slowPathAllocCount - this.slowPathAllocCountStash));
            memoryControlAndLMKJson.put("LMKCount", String.valueOf(this.lmkOccurCount - this.lmkOccurCountStash));
            memoryControlAndLMKJson.put("memoryControlCount", String.valueOf(this.lowMemoryManageCount));
            availMemoryTimeJson.put("belowThresholdTime", String.valueOf(this.belowThresholdTime));
            this.aboveThresholdTime = (this.totalTimeEnd - this.totalTimeBegin) - this.belowThresholdTime;
            availMemoryTimeJson.put("aboveThresholdTime", String.valueOf(this.aboveThresholdTime));
        } catch (JSONException e) {
            AwareLog.e(TAG, "JSONException...");
        }
        return "[iAwareMemoryRTStatis_Start]" + MemoryConstant.LINE_SEPARATOR + "{" + MemoryConstant.LINE_SEPARATOR + "\"memoryAllocateCount\":" + memoryAllocateJson.toString() + "," + MemoryConstant.LINE_SEPARATOR + "\"memoryControlAndLMKCount\":" + memoryControlAndLMKJson.toString() + "," + MemoryConstant.LINE_SEPARATOR + "\"availMemoryTimeCount\":" + availMemoryTimeJson.toString() + "," + MemoryConstant.LINE_SEPARATOR + "}" + MemoryConstant.LINE_SEPARATOR + "[iAwareMemoryRTStatis_End]";
    }

    public void clearCache() {
        AwareLog.d(TAG, "enter clearCache...");
        this.lowMemoryManageCount = 0;
        this.belowThresholdTime = 0;
        this.meminfoAllocCountStash = this.meminfoAllocCount;
        this.slowPathAllocCountStash = this.slowPathAllocCount;
        this.lmkOccurCountStash = this.lmkOccurCount;
        this.aboveThresholdTime = 0;
        this.totalTimeBegin = SystemClock.elapsedRealtime();
        this.totalTimeEnd = this.totalTimeBegin;
    }
}
