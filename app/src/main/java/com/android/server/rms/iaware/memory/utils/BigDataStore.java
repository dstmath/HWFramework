package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;

public final class BigDataStore {
    private static final String TAG = "BigDataStore";
    private static BigDataStore sInstance;
    public long aboveThresholdTime;
    public long belowThresholdTime;
    public long belowThresholdTimeBegin;
    public long coldStartCount;
    public long lmkOccurCount;
    public long lmkOccurCountStash;
    public long lowMemoryManageCount;
    public long meminfoAllocCount;
    public long meminfoAllocCountStash;
    public long slowPathAllocCount;
    public long slowPathAllocCountStash;
    public long totalStartCount;
    public long totalTimeBegin;
    public long totalTimeEnd;

    private BigDataStore() {
        this.meminfoAllocCount = 0;
        this.slowPathAllocCount = 0;
        this.lmkOccurCount = 0;
        this.lowMemoryManageCount = 0;
        this.belowThresholdTime = 0;
        this.totalStartCount = 0;
        this.coldStartCount = 0;
        this.lmkOccurCountStash = 0;
        this.meminfoAllocCountStash = 0;
        this.slowPathAllocCountStash = 0;
        this.belowThresholdTimeBegin = 0;
        this.aboveThresholdTime = 0;
        this.totalTimeBegin = 0;
        this.totalTimeEnd = 0;
    }

    public static synchronized BigDataStore getInstance() {
        BigDataStore bigDataStore;
        synchronized (BigDataStore.class) {
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
                String[] temp = str.split(":");
                if (temp.length == 2) {
                    if ("Total page alloc count".equals(temp[0])) {
                        this.meminfoAllocCount = Long.parseLong(temp[1]);
                    } else if ("Total slow path page alloc count".equals(temp[0])) {
                        this.slowPathAllocCount = Long.parseLong(temp[1]);
                    }
                }
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException...");
        }
    }
}
