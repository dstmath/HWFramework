package com.huawei.android.net;

import android.net.NetworkStats;

public class NetworkStatsEx {
    public static final int SET_DEFAULT = 0;
    public static final int SET_FOREGROUND = 1;
    private NetworkStats mNetworkStats;

    public static class Entry {
        private android.net.NetworkStats.Entry entry;

        /* synthetic */ Entry(android.net.NetworkStats.Entry entry, Entry -this1) {
            this(entry);
        }

        private Entry(android.net.NetworkStats.Entry entry) {
            this.entry = entry;
        }

        public long getRxBytes() {
            return this.entry == null ? 0 : this.entry.rxBytes;
        }

        public long getTxBytes() {
            return this.entry == null ? 0 : this.entry.txBytes;
        }

        public int getUid() {
            return this.entry == null ? 0 : this.entry.uid;
        }

        public int getSet() {
            return this.entry == null ? 0 : this.entry.set;
        }
    }

    public NetworkStatsEx(NetworkStats networkStats) {
        this.mNetworkStats = networkStats;
    }

    public Entry getTotalIncludingTags(Entry recycle) {
        if (this.mNetworkStats == null) {
            return null;
        }
        return new Entry(this.mNetworkStats.getTotalIncludingTags(recycle == null ? null : recycle.entry), null);
    }

    public int size() {
        return this.mNetworkStats == null ? 0 : this.mNetworkStats.size();
    }

    public Entry getValues(int i, Entry recycle) {
        if (this.mNetworkStats == null) {
            return null;
        }
        return new Entry(this.mNetworkStats.getValues(i, recycle == null ? null : recycle.entry), null);
    }
}
