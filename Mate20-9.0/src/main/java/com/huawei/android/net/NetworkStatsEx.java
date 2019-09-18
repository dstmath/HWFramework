package com.huawei.android.net;

import android.net.NetworkStats;

public class NetworkStatsEx {
    public static final int SET_DEFAULT = 0;
    public static final int SET_FOREGROUND = 1;
    private NetworkStats mNetworkStats;

    public static class Entry {
        /* access modifiers changed from: private */
        public NetworkStats.Entry entry;

        private Entry(NetworkStats.Entry entry2) {
            this.entry = entry2;
        }

        public long getRxBytes() {
            if (this.entry == null) {
                return 0;
            }
            return this.entry.rxBytes;
        }

        public long getTxBytes() {
            if (this.entry == null) {
                return 0;
            }
            return this.entry.txBytes;
        }

        public int getUid() {
            if (this.entry == null) {
                return 0;
            }
            return this.entry.uid;
        }

        public int getSet() {
            if (this.entry == null) {
                return 0;
            }
            return this.entry.set;
        }

        public int getActualUid() {
            if (this.entry == null) {
                return -1;
            }
            return this.entry.actUid;
        }

        public String getProc() {
            return this.entry == null ? "" : this.entry.proc;
        }

        public long getMpRxBytes() {
            if (this.entry == null) {
                return 0;
            }
            return this.entry.rxBytes_mp;
        }

        public long getMpTxBytes() {
            if (this.entry == null) {
                return 0;
            }
            return this.entry.txBytes_mp;
        }
    }

    public NetworkStatsEx(NetworkStats networkStats) {
        this.mNetworkStats = networkStats;
    }

    public Entry getTotalIncludingTags(Entry recycle) {
        NetworkStats.Entry entry;
        if (this.mNetworkStats == null) {
            return null;
        }
        NetworkStats networkStats = this.mNetworkStats;
        if (recycle == null) {
            entry = null;
        } else {
            entry = recycle.entry;
        }
        return new Entry(networkStats.getTotalIncludingTags(entry));
    }

    public static boolean isSetStatic(int set) {
        if (9 == set) {
            return true;
        }
        return false;
    }

    public int size() {
        if (this.mNetworkStats == null) {
            return 0;
        }
        return this.mNetworkStats.size();
    }

    public Entry getValues(int i, Entry recycle) {
        if (this.mNetworkStats == null) {
            return null;
        }
        return new Entry(this.mNetworkStats.getValues(i, recycle == null ? null : recycle.entry));
    }
}
