package com.android.server.rms.collector;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

public final class MemInfoReader {
    private static final int MEMINFO_BUFFERS = 2;
    private static final int MEMINFO_CACHED = 3;
    private static final int MEMINFO_COUNT = 8;
    private static final int MEMINFO_FREE = 1;
    private static final int MEMINFO_SLAB = 6;
    private static final int MEMINFO_SUNRECLAIM = 7;
    private static final int MEMINFO_SWAP_FREE = 5;
    private static final int MEMINFO_SWAP_TOTAL = 4;
    private static final int MEMINFO_TOTAL = 0;
    private final long[] mInfos;

    public MemInfoReader() {
        this.mInfos = new long[MEMINFO_COUNT];
    }

    public int readMemInfo() {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            int memInfo = ResourceCollector.getMemInfo(this.mInfos);
            return memInfo;
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public long[] getMemInfo() {
        return this.mInfos;
    }

    public long getTotalSizeKb() {
        return this.mInfos[0];
    }

    public long getFreeSizeKb() {
        return this.mInfos[MEMINFO_FREE];
    }

    public long getCachedSizeKb() {
        return this.mInfos[MEMINFO_CACHED];
    }

    public long getBuffersSizeKb() {
        return this.mInfos[MEMINFO_BUFFERS];
    }

    public long getSlabSizeKb() {
        return this.mInfos[MEMINFO_SLAB];
    }

    public long getSwapTotalSizeKb() {
        return this.mInfos[MEMINFO_SWAP_TOTAL];
    }

    public long getSwapFreeSizeKb() {
        return this.mInfos[MEMINFO_SWAP_FREE];
    }

    public long getSUnreclaimSizeKb() {
        return this.mInfos[MEMINFO_SUNRECLAIM];
    }
}
