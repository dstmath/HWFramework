package com.huawei.android.net;

import android.net.NetworkStats;

public class NetworkStatsEx {
    public static final int SET_DEFAULT = 0;
    public static final int SET_FOREGROUND = 1;
    private NetworkStats mNetworkStats;

    public NetworkStatsEx(NetworkStats networkStats) {
        this.mNetworkStats = networkStats;
    }

    public static boolean isSetStatic(int set) {
        return set == 9;
    }

    public Entry getTotalIncludingTags(Entry recycle) {
        NetworkStats networkStats = this.mNetworkStats;
        if (networkStats == null) {
            return null;
        }
        return new Entry(networkStats.getTotalIncludingTags(recycle == null ? null : recycle.entry));
    }

    public int size() {
        NetworkStats networkStats = this.mNetworkStats;
        if (networkStats == null) {
            return 0;
        }
        return networkStats.size();
    }

    public Entry getValues(int i, Entry recycle) {
        NetworkStats networkStats = this.mNetworkStats;
        if (networkStats == null) {
            return null;
        }
        return new Entry(networkStats.getValues(i, recycle == null ? null : recycle.entry));
    }

    public static class Entry {
        private NetworkStats.Entry entry;

        private Entry(NetworkStats.Entry entry2) {
            this.entry = entry2;
        }

        public long getRxBytes() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return 0;
            }
            return entry2.rxBytes;
        }

        public long getTxBytes() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return 0;
            }
            return entry2.txBytes;
        }

        public int getUid() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return 0;
            }
            return entry2.uid;
        }

        public int getSet() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return 0;
            }
            return entry2.set;
        }

        public int getActualUid() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return -1;
            }
            return entry2.actUid;
        }

        public String getProc() {
            NetworkStats.Entry entry2 = this.entry;
            return entry2 == null ? "" : entry2.proc;
        }

        public long getMpRxBytes() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return 0;
            }
            return entry2.rxBytes_mp;
        }

        public long getMpTxBytes() {
            NetworkStats.Entry entry2 = this.entry;
            if (entry2 == null) {
                return 0;
            }
            return entry2.txBytes_mp;
        }
    }
}
