package com.android.server.am;

import java.util.ArrayList;
import java.util.List;

public interface IHwBroadcastQueueEx {
    ArrayList<Integer> getProxyBroadcastPidsLock();

    long proxyBroadcastByPidLock(List<Integer> list, boolean z, boolean z2, long j);
}
