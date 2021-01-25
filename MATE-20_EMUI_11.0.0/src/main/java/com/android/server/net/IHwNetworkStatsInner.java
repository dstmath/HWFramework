package com.android.server.net;

import android.util.ArrayMap;
import com.android.server.net.NetworkStatsService;

public interface IHwNetworkStatsInner {
    NetworkStatsRecorder buildProcRecorder(String str, boolean z);

    ArrayMap<String, NetworkIdentitySet> getActiveUidIfaces();

    NetworkStatsService.NetworkStatsSettings getNetworkStatsSettings();
}
