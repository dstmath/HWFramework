package com.android.server.net;

import android.net.INetworkStatsService.Stub;
import java.io.IOException;

public abstract class AbsNetworkStatsService extends Stub {
    public void hwInitProcRecorder() {
    }

    public void hwInitProcStatsCollection() {
    }

    public void hwUpdateProcPersistThresholds(long persistThreshold) {
    }

    public void hwRecordSnapshotLocked(long currentTime) {
    }

    public void hwShutdownLocked(long currentTime) {
    }

    public boolean hwImportLegacyNetworkLocked() throws IOException {
        return false;
    }

    public void hwMaybePersistLocked(long currentTime) {
    }

    public void hwForcePersistLocked(long currentTime) {
    }

    public void hwInitUidAndProcRecorder() {
    }

    public void hwUpdateUidAndProcPersistThresholds(long persistThreshold) {
    }

    public void hwRecordUidAndProcSnapshotLocked(long currentTime) {
    }

    public void hwMaybeUidAndProcPersistLocked(long currentTime) {
    }

    public void hwForceUidAndProcPersistLocked(long currentTime) {
    }

    public void hwShutdownUidAndProcLocked(long currentTime) {
    }

    public NetworkStatsRecorder getUidAndProcNetworkStatsRecorder() {
        return null;
    }
}
