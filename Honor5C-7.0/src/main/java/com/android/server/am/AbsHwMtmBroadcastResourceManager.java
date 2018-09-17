package com.android.server.am;

public interface AbsHwMtmBroadcastResourceManager {
    void iawareEndCountBroadcastSpeed(BroadcastRecord broadcastRecord);

    boolean iawareProcessBroadcast(int i, boolean z, BroadcastRecord broadcastRecord, Object obj);

    void iawareStartCountBroadcastSpeed(boolean z, BroadcastRecord broadcastRecord);
}
