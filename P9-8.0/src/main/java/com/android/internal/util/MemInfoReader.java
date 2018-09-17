package com.android.internal.util;

import android.os.Debug;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

public final class MemInfoReader {
    final long[] mInfos = new long[14];

    public void readMemInfo() {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            Debug.getMemInfo(this.mInfos);
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public long getTotalSize() {
        return this.mInfos[0] * 1024;
    }

    public long getFreeSize() {
        return this.mInfos[1] * 1024;
    }

    public long getCachedSize() {
        return getCachedSizeKb() * 1024;
    }

    public long getKernelUsedSize() {
        return getKernelUsedSizeKb() * 1024;
    }

    public long getTotalSizeKb() {
        return this.mInfos[0];
    }

    public long getFreeSizeKb() {
        return this.mInfos[1];
    }

    public long getRealCachedSizeKb() {
        return (this.mInfos[2] + this.mInfos[3]) - this.mInfos[9];
    }

    public long getCachedSizeKb() {
        if (this.mInfos[13] != 0) {
            return this.mInfos[13] - getFreeSizeKb();
        }
        return getRealCachedSizeKb();
    }

    public long getKernelUsedSizeKb() {
        return (((this.mInfos[4] + this.mInfos[5]) + this.mInfos[10]) + this.mInfos[11]) + this.mInfos[12];
    }

    public long getSwapTotalSizeKb() {
        return this.mInfos[6];
    }

    public long getSwapFreeSizeKb() {
        return this.mInfos[7];
    }

    public long getZramTotalSizeKb() {
        return this.mInfos[8];
    }

    public long[] getRawInfo() {
        return this.mInfos;
    }
}
