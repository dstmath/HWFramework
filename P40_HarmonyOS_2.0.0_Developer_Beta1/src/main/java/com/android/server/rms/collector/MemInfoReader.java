package com.android.server.rms.collector;

import android.os.StrictMode;

public final class MemInfoReader extends DefaultMemInfoReader {
    private static final int MEMINFO_BUFFERS = 2;
    private static final int MEMINFO_CACHED = 3;
    private static final int MEMINFO_COUNT = 8;
    private static final int MEMINFO_FREE = 1;
    private static final int MEMINFO_SLAB = 6;
    private static final int MEMINFO_SWAP_FREE = 5;
    private static final int MEMINFO_SWAP_TOTAL = 4;
    private static final int MEMINFO_TOTAL = 0;
    private static final int MEMINFO_UNRECLAIM = 7;
    private final long[] mInfos = new long[8];

    public int readMemInfo() {
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            return ResourceCollector.getMemInfo(this.mInfos);
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

    public long getUnreclaimSizeKb() {
        return this.mInfos[7];
    }
}
