package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.Pair;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BigDataStore {
    private static final int MAX_BIGDATA_ITEM = 20;
    private static final String TAG = "BigDataStore";
    private static final Object mLock = new Object();
    private static BigDataStore sInstance;
    public long aboveThresholdTime = 0;
    public long belowThresholdTime = 0;
    public long belowThresholdTimeBegin = 0;
    private ArrayList<Pair<Long, Long>> bgProcessSavedList = new ArrayList<>();
    public long coldLaunch = 0;
    private ArrayList<Pair<Long, Long>> coldWarmLaunchSavedList = new ArrayList<>();
    public long lmkOccurCount = 0;
    public long lmkOccurCountStash = 0;
    public long lowMemoryManageCount = 0;
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
        synchronized (mLock) {
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
        String[] totalAllocCount = MemoryReader.getMeminfoAllocCount();
        try {
            int length = totalAllocCount.length;
            int i = 0;
            while (i < length) {
                try {
                    String[] temp = totalAllocCount[i].split(":");
                    if (temp.length == 2) {
                        if ("Total page alloc count".equals(temp[0])) {
                            this.meminfoAllocCount = Long.parseLong(temp[1]);
                        } else if ("Total slow path page alloc count".equals(temp[0])) {
                            this.slowPathAllocCount = Long.parseLong(temp[1]);
                        }
                    }
                    i++;
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "NumberFormatException...");
                }
            }
        } catch (NumberFormatException e2) {
            NumberFormatException numberFormatException = e2;
            AwareLog.e(TAG, "NumberFormatException...");
        }
    }

    public String getColdWarmLaunchData(boolean clear) {
        if (this.coldWarmLaunchSavedList == null) {
            return null;
        }
        this.coldWarmLaunchSavedList.add(Pair.create(Long.valueOf(this.coldLaunch), Long.valueOf(this.warmLaunch)));
        this.coldLaunch = 0;
        this.warmLaunch = 0;
        StringBuilder sb = new StringBuilder();
        int i = 20;
        if (this.coldWarmLaunchSavedList.size() <= 20) {
            i = this.coldWarmLaunchSavedList.size();
        }
        int size = i;
        for (int i2 = 0; i2 < size; i2++) {
            this.totalColdLaunch += ((Long) this.coldWarmLaunchSavedList.get(i2).first).longValue();
            this.totalWarmLaunch += ((Long) this.coldWarmLaunchSavedList.get(i2).second).longValue();
        }
        sb.append("\"coldLaunch\":\"");
        sb.append(this.totalColdLaunch >= 0 ? this.totalColdLaunch : 0);
        sb.append("\",\"warmLaunch\":\"");
        sb.append(this.totalWarmLaunch >= 0 ? this.totalWarmLaunch : 0);
        sb.append("\"");
        if (clear) {
            this.coldWarmLaunchSavedList = new ArrayList<>();
            this.totalColdLaunch = 0;
            this.totalWarmLaunch = 0;
        }
        AwareLog.d(TAG, "getColdWarmLaunchData: " + sb.toString());
        return sb.toString();
    }

    public String getBgAppData(boolean clear) {
        AwareLog.d(TAG, "getBgAppData. clear: " + clear);
        getBgAppDataInternal();
        StringBuilder sb = new StringBuilder();
        int i = 20;
        if (this.bgProcessSavedList.size() <= 20) {
            i = this.bgProcessSavedList.size();
        }
        int size = i;
        for (int i2 = 0; i2 < size; i2++) {
            this.totalActivity += ((Long) this.bgProcessSavedList.get(i2).first).longValue();
            this.totalService += ((Long) this.bgProcessSavedList.get(i2).second).longValue();
        }
        sb.append("\"activity\":\"");
        sb.append(this.totalActivity);
        sb.append("\",\"services\":\"");
        sb.append(this.totalService);
        sb.append("\"");
        if (clear) {
            this.bgProcessSavedList = new ArrayList<>();
            this.totalActivity = 0;
            this.totalService = 0;
        }
        AwareLog.d(TAG, "getBgAppData: " + sb.toString());
        return sb.toString();
    }

    private void getBgAppDataInternal() {
        if (this.bgProcessSavedList == null) {
            this.bgProcessSavedList = new ArrayList<>();
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
                    if (block != null) {
                        List<AwareProcessInfo> procs = block.getProcessList();
                        if (procs != null && procs.size() >= i) {
                            for (AwareProcessInfo info : procs) {
                                if (isValidAwareProcessInfo(info)) {
                                    if (!pidSet.contains(Integer.valueOf(info.mPid))) {
                                        if (info.mHasShownUi) {
                                            activityCount++;
                                        } else {
                                            serviceCount++;
                                        }
                                        pidSet.add(Integer.valueOf(info.mPid));
                                    }
                                }
                            }
                        }
                    }
                    i = 1;
                }
                this.bgProcessSavedList.add(Pair.create(Long.valueOf(activityCount), Long.valueOf(serviceCount)));
            }
        }
    }

    private boolean isValidAwareProcessInfo(AwareProcessInfo info) {
        return (info == null || info.mProcInfo == null || info.mProcInfo.mPackageName == null || info.mPid <= 0) ? false : true;
    }
}
