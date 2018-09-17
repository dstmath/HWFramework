package com.android.server.net;

import android.app.AlarmManager;
import android.content.Context;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.INetworkManagementService;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Slog;
import android.util.TrustedTime;
import com.android.internal.net.NetworkStatsFactory;
import com.android.server.net.NetworkStatsService.NetworkStatsSettings;
import java.io.File;
import java.io.IOException;

public class HwNetworkStatsService extends NetworkStatsService {
    private static final boolean DEBUG;
    public static final long MAX_TRAFFIC_PER_PROCESS = 5242880;
    private static final String PREFIX_UID_AND_PROC = "uid_and_proc";
    private static final String PREFIX_UID_PROC = "uid_proc";
    private static final String TAG = "HwNetworkStatsService";
    private static final String TAG_MONITOR = "HwNetworkStatsService_IMONITOR";
    public static final int TRAFFIC_EVENT_ID = 907400017;
    public static final long UPLOAD_INTERVAL = 1800000;
    private static long mLastUploadTime = 0;
    private NetworkTemplate mMobileTemplate = NetworkTemplate.buildTemplateMobileWildcard();
    private ArrayMap<String, ProcInfo> mProcInfos = new ArrayMap();
    private final NetworkStatsFactory mStatsFactory = new NetworkStatsFactory();
    private NetworkStatsRecorder mUidAndProcRecorder;
    private NetworkStatsRecorder mUidProcRecorder;
    private NetworkStatsCollection mUidProcStatsCached;
    private ArrayMap<String, UploadCycle> mUploadCycles = new ArrayMap();

    private static final class ProcInfo {
        String mPkgName;
        String mVersionName;

        public ProcInfo() {
            this.mPkgName = "";
            this.mVersionName = "";
        }

        public ProcInfo(String pkgName, String versionName) {
            this.mPkgName = pkgName;
            this.mVersionName = versionName;
        }
    }

    private static final class UploadCycle {
        long mLastUploadTraffic;
        String mProc;
        int mUploadCount;

        public UploadCycle() {
            this("", 0);
        }

        public UploadCycle(String proc, long lastUploadTraffic) {
            this.mProc = proc;
            this.mLastUploadTraffic = lastUploadTraffic;
            this.mUploadCount = 0;
        }
    }

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        DEBUG = isLoggable;
    }

    public HwNetworkStatsService(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, WakeLock wakeLock, TrustedTime time, TelephonyManager teleManager, NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
        super(context, networkManager, alarmManager, wakeLock, time, teleManager, settings, statsObservers, systemDir, baseDir);
    }

    public void hwInitProcRecorder() {
        this.mUidProcRecorder = buildRecorder(PREFIX_UID_PROC, this.mSettings.getUidConfig(), false);
    }

    public void hwInitProcStatsCollection() {
        this.mUidProcStatsCached = this.mUidProcRecorder.getOrLoadCompleteLocked();
    }

    public void hwUpdateProcPersistThresholds(long persistThreshold) {
        this.mUidProcRecorder.setPersistThreshold(this.mSettings.getUidPersistBytes(persistThreshold));
    }

    public void hwRecordSnapshotLocked(long currentTime) {
        NetworkStats res = getNetworkStatsProcDetail(this.mActiveIface);
        if (res != null) {
            if (DEBUG) {
                Slog.d(TAG, "mActiveIface: " + this.mActiveIface);
            }
            this.mUidProcRecorder.recordSnapshotLocked(res, this.mActiveUidIfaces, null, currentTime);
            if (isMobile(this.mActiveIface)) {
                shouldUploadTraffic(res);
            }
        }
    }

    private boolean isMobile(String iface) {
        int i = 0;
        boolean isMobile = false;
        if (iface == null) {
            return false;
        }
        String[] allMobiles = getMobileIfaces();
        int length = allMobiles.length;
        while (i < length) {
            String curIface = allMobiles[i];
            if (DEBUG) {
                Slog.d(TAG, "mobile iface:" + curIface);
            }
            if (iface.equals(curIface)) {
                isMobile = true;
                break;
            }
            i++;
        }
        return isMobile;
    }

    private NetworkStats getNetworkStatsProcDetail(String iface) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        return this.mStatsFactory.readNetworkStatsProcDetail(iface);
    }

    private void shouldUploadTraffic(NetworkStats stats) {
        if (stats != null) {
            Entry entry = null;
            for (int i = 0; i < stats.size(); i++) {
                entry = stats.getValues(i, entry);
                if (entry.uid < 10000) {
                    UploadCycle uploadCycle;
                    String proc = entry.proc;
                    ProcInfo procInfo = (ProcInfo) this.mProcInfos.get(proc);
                    if (this.mUploadCycles.containsKey(proc)) {
                        uploadCycle = (UploadCycle) this.mUploadCycles.get(proc);
                    } else {
                        uploadCycle = new UploadCycle();
                        uploadCycle.mProc = proc;
                        this.mUploadCycles.put(proc, uploadCycle);
                    }
                    long curTime = SystemClock.elapsedRealtime();
                    NetworkStatsHistory history = this.mUidProcStatsCached.getHistory(this.mMobileTemplate, entry.uid, entry.set, entry.tag, -1, proc, Long.MIN_VALUE, Long.MAX_VALUE, 3, Binder.getCallingUid());
                    long deltaTime = curTime - mLastUploadTime;
                    long deltaTotal = history.getTotalBytes() - uploadCycle.mLastUploadTraffic;
                    if (DEBUG) {
                        Slog.d(TAG, "maybe report, proc:" + uploadCycle.mProc + ", interval:" + (curTime - mLastUploadTime) + ", mLastUploadTraffic:" + uploadCycle.mLastUploadTraffic + ", curTotal:" + history.getTotalBytes() + ", uploadCount:" + uploadCycle.mUploadCount);
                    }
                    if (deltaTime > UPLOAD_INTERVAL && deltaTotal > MAX_TRAFFIC_PER_PROCESS) {
                        upload(entry, history, procInfo, uploadCycle);
                    }
                }
            }
        }
    }

    private boolean upload(Entry entry, NetworkStatsHistory history, ProcInfo procInfo, UploadCycle uploadCycle) {
        EventStream eStream = IMonitor.openEventStream(TRAFFIC_EVENT_ID);
        if (eStream != null) {
            eStream.setParam((short) 1, procInfo != null ? procInfo.mVersionName : "unknown");
            eStream.setParam((short) 2, entry.uid);
            eStream.setParam((short) 3, history.getTotalBytes());
            eStream.setParam((short) 4, history.getTotalTxBytes());
            eStream.setParam((short) 5, history.getTotalRxBytes());
            StringBuilder extraInfo = new StringBuilder();
            StringBuilder append = extraInfo.append("PackageName: ").append(procInfo != null ? procInfo.mPkgName : entry.proc).append("\n").append("ProcessName: ").append(entry.proc).append("\n").append("UploadCount: ");
            int i = uploadCycle.mUploadCount + 1;
            uploadCycle.mUploadCount = i;
            append.append(i);
            eStream.setParam((short) 6, extraInfo.toString()).setTime(System.currentTimeMillis());
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
            Slog.i(TAG, "upload traffic for proc:" + entry.proc + ", total:" + history.getTotalBytes() + ", rx:" + history.getTotalRxBytes() + ", tx:" + history.getTotalTxBytes() + ", count:" + uploadCycle.mUploadCount);
            mLastUploadTime = SystemClock.elapsedRealtime();
            uploadCycle.mLastUploadTraffic = history.getTotalBytes();
            return true;
        }
        Slog.w(TAG_MONITOR, "Open EventStream failed for event:907400017");
        return false;
    }

    public void hwShutdownLocked(long currentTime) {
        this.mUidProcRecorder.forcePersistLocked(currentTime);
        this.mUidProcRecorder = null;
        this.mUidProcStatsCached = null;
    }

    public boolean hwImportLegacyNetworkLocked() throws IOException {
        File file = new File(this.mSystemDir, "netstats_uid_proc.bin");
        if (!file.exists()) {
            return false;
        }
        this.mUidProcRecorder.importLegacyNetworkLocked(file);
        return file.delete();
    }

    public void hwMaybePersistLocked(long currentTime) {
        this.mUidProcRecorder.maybePersistLocked(currentTime);
    }

    public void hwForcePersistLocked(long currentTime) {
        this.mUidProcRecorder.forcePersistLocked(currentTime);
    }

    private NetworkStats getNetworkStatsUidAndProcDetail() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        try {
            return this.mStatsFactory.javaReadNetworkStatsUidAndProcDetail(-1, null, -1);
        } catch (IOException e) {
            Slog.e(TAG, "problem reading uid-and_proc from kernel", e);
            throw new IllegalStateException(e);
        }
    }

    public void hwInitUidAndProcRecorder() {
        Slog.d(TAG, "hwInitUidAndProcRecorder");
        this.mUidAndProcRecorder = buildRecorder(PREFIX_UID_AND_PROC, this.mSettings.getUidConfig(), false);
    }

    public void hwUpdateUidAndProcPersistThresholds(long persistThreshold) {
        Slog.d(TAG, "hwUpdateUidAndProcPersistThresholds persistThreshold = " + persistThreshold);
        this.mUidAndProcRecorder.setPersistThreshold(this.mSettings.getUidPersistBytes(persistThreshold));
    }

    public void hwRecordUidAndProcSnapshotLocked(long currentTime) {
        Slog.d(TAG, "hwRecordUidAndProcSnapshotLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.recordSnapshotLocked(getNetworkStatsUidAndProcDetail(), this.mActiveUidIfaces, null, currentTime);
    }

    public void hwMaybeUidAndProcPersistLocked(long currentTime) {
        Slog.d(TAG, "hwMaybeUidAndProcPersistLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.maybePersistLocked(currentTime);
    }

    public void hwForceUidAndProcPersistLocked(long currentTime) {
        Slog.d(TAG, "hwForceUidAndProcPersistLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.forcePersistLocked(currentTime);
    }

    public void hwShutdownUidAndProcLocked(long currentTime) {
        Slog.d(TAG, "hwShutdownUidAndProcLocked currentTime = " + currentTime);
        this.mUidAndProcRecorder.forcePersistLocked(currentTime);
        this.mUidAndProcRecorder = null;
    }

    public NetworkStatsRecorder getUidAndProcNetworkStatsRecorder() {
        return this.mUidAndProcRecorder;
    }
}
