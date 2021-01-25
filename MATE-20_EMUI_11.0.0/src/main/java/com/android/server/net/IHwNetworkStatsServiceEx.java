package com.android.server.net;

public interface IHwNetworkStatsServiceEx {
    int getPerformPollDelay();

    NetworkStatsRecorder getUidAndProcNetworkStatsRecorder();

    void hwForceUidAndProcPersistLocked(long j);

    void hwInitUidAndProcRecorder();

    void hwMaybeUidAndProcPersistLocked(long j);

    void hwRecordUidAndProcSnapshotLocked(long j);

    void hwShutdownUidAndProcLocked(long j);

    void hwUpdateUidAndProcPersistThresholds(long j);

    void removeUids(int[] iArr);

    void reportNetdStatus(int i);

    boolean setAlertPeriodType(int i);
}
