package com.android.server.net;

import android.content.Context;
import android.net.NetworkStats;
import android.util.IMonitor;
import android.util.Slog;
import com.android.internal.net.VpnInfo;
import java.io.IOException;

public final class HwNetworkStatsServiceEx implements IHwNetworkStatsServiceEx {
    private static final int NET_D_TIME_OUT_ERROR = -11;
    private static final int NET_D_TIME_OUT_EVENT_ID = 907021006;
    private static final int[] PERIOD_TIMES = {1000, 500, 300, 200, 100};
    private static final String PREFIX_UID_AND_PROC = "uid_and_proc";
    private static final String TAG = "HwNetworkStatsServiceEx";
    private static final String TAG_MONITOR = "HwNetworkStatsServiceEx_IMONITOR";
    private final Context mContext;
    IHwNetworkStatsInner mINetworkStatsInner = null;
    private int mPerformPollDelay = PERIOD_TIMES[0];
    private final NetworkStatsFactoryEx mStatsFactoryEx;
    private NetworkStatsRecorder mUidAndProcRecorder;

    public HwNetworkStatsServiceEx(IHwNetworkStatsInner hwNetworkStatsInner, Context context) {
        this.mINetworkStatsInner = hwNetworkStatsInner;
        this.mContext = context;
        this.mStatsFactoryEx = NetworkStatsFactoryEx.create();
    }

    public void reportNetdStatus(int status) {
        if (status == -11) {
            IMonitor.EventStream eventStream = IMonitor.openEventStream((int) NET_D_TIME_OUT_EVENT_ID);
            if (eventStream != null) {
                IMonitor.sendEvent(eventStream);
                IMonitor.closeEventStream(eventStream);
                return;
            }
            Slog.w(TAG_MONITOR, "Open EventStream failed for event:907021006");
        }
    }

    private NetworkStats getNetworkStatsUidAndProcDetail() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactoryEx.readPidNetworkStatsDetail(-1, (String[]) null, -1);
        } catch (IOException e) {
            Slog.e(TAG, "problem reading uid-and_proc from kernel", e);
            throw new IllegalStateException(e);
        }
    }

    public void hwInitUidAndProcRecorder() {
        Slog.i(TAG, "hwInitUidAndProcRecorder");
        this.mUidAndProcRecorder = this.mINetworkStatsInner.buildProcRecorder(PREFIX_UID_AND_PROC, false);
    }

    public void hwUpdateUidAndProcPersistThresholds(long persistThreshold) {
        Slog.i(TAG, "hwUpdateUidAndProcPersistThresholds persistThreshold = " + persistThreshold);
        this.mUidAndProcRecorder.setPersistThreshold(this.mINetworkStatsInner.getNetworkStatsSettings().getUidPersistBytes(persistThreshold));
    }

    public void hwRecordUidAndProcSnapshotLocked(long currentTime) {
        Slog.i(TAG, "hwRecordUidAndProcSnapshotLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.recordSnapshotLocked(getNetworkStatsUidAndProcDetail(), this.mINetworkStatsInner.getActiveUidIfaces(), (VpnInfo[]) null, currentTime);
    }

    public void hwMaybeUidAndProcPersistLocked(long currentTime) {
        Slog.i(TAG, "hwMaybeUidAndProcPersistLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.maybePersistLocked(currentTime);
    }

    public void hwForceUidAndProcPersistLocked(long currentTime) {
        Slog.i(TAG, "hwForceUidAndProcPersistLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.forcePersistLocked(currentTime);
    }

    public void hwShutdownUidAndProcLocked(long currentTime) {
        Slog.i(TAG, "hwShutdownUidAndProcLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.forcePersistLocked(currentTime);
        this.mUidAndProcRecorder = null;
    }

    public NetworkStatsRecorder getUidAndProcNetworkStatsRecorder() {
        Slog.i(TAG, "getUidAndProcNetworkStatsRecorder");
        return this.mUidAndProcRecorder;
    }

    public boolean setAlertPeriodType(int period) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        return changeCurrentDelay(period);
    }

    public int getPerformPollDelay() {
        return this.mPerformPollDelay;
    }

    public void removeUids(int[] uids) {
        this.mStatsFactoryEx.removeUids(uids);
    }

    private boolean changeCurrentDelay(int period) {
        if (period >= 0) {
            int[] iArr = PERIOD_TIMES;
            if (period < iArr.length) {
                this.mPerformPollDelay = iArr[period];
                return true;
            }
        }
        Slog.i(TAG, "Illegal value, so ignore it and keep the value before.");
        return false;
    }
}
