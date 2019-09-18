package com.android.server.net;

import android.net.NetworkStats;
import android.net.NetworkTemplate;

public abstract class NetworkStatsManagerInternal {
    public abstract void advisePersistThreshold(long j);

    public abstract void forceUpdate();

    public abstract long getNetworkTotalBytes(NetworkTemplate networkTemplate, long j, long j2);

    public abstract NetworkStats getNetworkUidBytes(NetworkTemplate networkTemplate, long j, long j2);

    public abstract void setUidForeground(int i, boolean z);
}
