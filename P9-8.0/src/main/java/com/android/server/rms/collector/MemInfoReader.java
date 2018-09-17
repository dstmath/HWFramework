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
    private final long[] mInfos = new long[8];

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
        return (long[]) this.mInfos.clone();
    }

    public long getTotalSizeKb() {
        return this.mInfos[0];
    }

    public long getFreeSizeKb() {
        return this.mInfos[1];
    }

    public long getCachedSizeKb() {
        return this.mInfos[3];
    }

    public long getBuffersSizeKb() {
        return this.mInfos[2];
    }

    public long getSlabSizeKb() {
        return this.mInfos[6];
    }

    public long getSwapTotalSizeKb() {
        return this.mInfos[4];
    }

    public long getSwapFreeSizeKb() {
        return this.mInfos[5];
    }

    public long getSUnreclaimSizeKb() {
        return this.mInfos[7];
    }
}
