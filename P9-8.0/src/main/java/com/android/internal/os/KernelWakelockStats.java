package com.android.internal.os;

import java.util.HashMap;

public class KernelWakelockStats extends HashMap<String, Entry> {
    int kernelWakelockVersion;

    public static class Entry {
        public int mCount;
        public long mTotalTime;
        public int mVersion;

        Entry(int count, long totalTime, int version) {
            this.mCount = count;
            this.mTotalTime = totalTime;
            this.mVersion = version;
        }
    }
}
