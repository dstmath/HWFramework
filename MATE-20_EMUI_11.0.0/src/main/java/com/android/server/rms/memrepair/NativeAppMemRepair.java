package com.android.server.rms.memrepair;

import android.os.Process;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.rms.collector.ResourceCollector;
import com.huawei.android.os.ProcessEx;
import com.huawei.android.os.ProcessExt;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NativeAppMemRepair {
    private static final Object LOCK = new Object();
    private static final int[] PROCESS_FULL_STATS_FORMAT = {32, 4640, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224, 32, 32, 32, 32, 32, 32, 32, 8224};
    private static final int PROC_OUT_LONG = 8192;
    private static final int PROC_OUT_STRING = 4096;
    private static final int PROC_PARENS = 512;
    private static final String PROC_PATH = "/proc";
    private static final int PROC_SPACE_TERM = 32;
    private static final int PROC_STATS_STRING_LENGTH = 6;
    private static final String STAT_PATH = "stat";
    private static final String TAG = "NativeAppMemRepair";
    private static NativeAppMemRepair sInstance;
    private final Map<String, Long> mConfigNativeThreshold = new ArrayMap();
    private AtomicBoolean mEnable = new AtomicBoolean(false);
    private final Map<String, NativeProcessInfo> mNativeProcesses = new ArrayMap();

    private NativeAppMemRepair() {
    }

    public static NativeAppMemRepair getInstance() {
        NativeAppMemRepair nativeAppMemRepair;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new NativeAppMemRepair();
            }
            nativeAppMemRepair = sInstance;
        }
        return nativeAppMemRepair;
    }

    public void enable() {
        if (!this.mEnable.get()) {
            this.mEnable.set(true);
        }
    }

    public void disable() {
        if (this.mEnable.get()) {
            this.mEnable.set(false);
        }
    }

    public void updateConfigNativeThreshold(String name, long threshold) {
        if (TextUtils.isEmpty(name) || threshold <= 0) {
            AwareLog.w(TAG, "The process threshold is invalid");
            return;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "config proc, name=" + name + ", threshold=" + threshold);
        }
        synchronized (this.mConfigNativeThreshold) {
            this.mConfigNativeThreshold.put(name, Long.valueOf(threshold));
        }
    }

    public void doMemRepair(AtomicBoolean interrupted) {
        AwareLog.i(TAG, "doMemRepair now");
        if (!this.mEnable.get()) {
            AwareLog.w(TAG, "Native app MemRepair is disable");
            return;
        }
        synchronized (this.mConfigNativeThreshold) {
            if (this.mConfigNativeThreshold.isEmpty()) {
                AwareLog.w(TAG, "Not config native app mem threshold");
            } else if (interrupted.get()) {
                AwareLog.i(TAG, "this task need interrupt now");
            } else {
                List<NativeProcessInfo> needToKilled = getAllNativeProcessForMemRepair();
                if (needToKilled.isEmpty()) {
                    AwareLog.i(TAG, "No native processes need to repair");
                }
                if (interrupted.get()) {
                    AwareLog.i(TAG, "this task need interrupt now");
                } else {
                    doKillAction(needToKilled, interrupted);
                }
            }
        }
    }

    private void doKillAction(List<NativeProcessInfo> needToKilled, AtomicBoolean interrupted) {
        AwareLog.i(TAG, "do kill action now ");
        for (NativeProcessInfo proc : needToKilled) {
            if (interrupted.get()) {
                AwareLog.i(TAG, "this task need interrupt now");
                return;
            }
            AwareLog.i(TAG, "kill the procs " + proc.toString());
            Process.killProcess(proc.mPid);
            SysMemMngBigData.getInstance().fillSysMemBigData(null, proc, 1002);
        }
    }

    private boolean isAllNativeProcessValid() {
        Iterator<String> it = this.mConfigNativeThreshold.keySet().iterator();
        while (it.hasNext() && (proc = this.mNativeProcesses.get(it.next())) != null && proc.isValidNativeProcessByNode()) {
        }
        return false;
    }

    private List<NativeProcessInfo> getAllNativeProcessForMemRepair() {
        List<NativeProcessInfo> result = new ArrayList<>();
        if (this.mNativeProcesses.isEmpty()) {
            updateAllNativeProcessByNode();
            getAllNativeProcessOverThreshold(result);
        } else if (isAllNativeProcessValid()) {
            getAllNativeProcessOverThreshold(result);
        } else {
            updateAllNativeProcessByNode();
            getAllNativeProcessOverThreshold(result);
        }
        return result;
    }

    private void getAllNativeProcessOverThreshold(List<NativeProcessInfo> result) {
        if (this.mNativeProcesses.isEmpty()) {
            AwareLog.w(TAG, "Native processes cache is empty");
            return;
        }
        for (NativeProcessInfo processInfo : this.mNativeProcesses.values()) {
            if (processInfo.isOverThreshold()) {
                AwareLog.i(TAG, "proc over threshold, proc=" + processInfo.toString());
                result.add(processInfo);
            }
        }
    }

    private void updateAllNativeProcessByNode() {
        AwareLog.d(TAG, "get all native proc from inode");
        int[] pids = ProcessExt.getPids(PROC_PATH, (int[]) null);
        int procNum = pids == null ? 0 : pids.length;
        for (int i = 0; i < procNum; i++) {
            int pid = pids[i];
            if (pid >= 0) {
                String[] procStatsString = new String[6];
                if (ProcessEx.readProcFile(new File(new File(PROC_PATH, Integer.toString(pid)), STAT_PATH).toString(), PROCESS_FULL_STATS_FORMAT, procStatsString, new long[6], (float[]) null)) {
                    updateProcessInfoToCached(pid, procStatsString[0]);
                }
            } else {
                return;
            }
        }
    }

    private void updateProcessInfoToCached(int pid, String baseName) {
        Long threshold;
        if (pid >= 0 && !TextUtils.isEmpty(baseName) && (threshold = this.mConfigNativeThreshold.get(baseName)) != null) {
            this.mNativeProcesses.put(baseName, new NativeProcessInfo(pid, baseName, threshold.longValue()));
        }
    }

    public class NativeProcessInfo {
        private long mCurrentPss;
        private int mPid;
        private String mProcessName;
        private long mPssThreshold;

        private NativeProcessInfo(int pid, String name, long threshold) {
            this.mCurrentPss = 0;
            this.mPid = pid;
            this.mProcessName = name;
            this.mPssThreshold = threshold;
        }

        public String getProcessName() {
            return this.mProcessName;
        }

        public int getPid() {
            return this.mPid;
        }

        public long getPssThreshold() {
            return this.mPssThreshold;
        }

        public long getCurrentPss() {
            return this.mCurrentPss;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isOverThreshold() {
            if (this.mCurrentPss == 0) {
                updateCurrentPss();
            }
            return this.mCurrentPss - this.mPssThreshold > 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isValidNativeProcessByNode() {
            if (this.mPid < 0 || TextUtils.isEmpty(this.mProcessName)) {
                return false;
            }
            File procDir = new File(NativeAppMemRepair.PROC_PATH, Integer.toString(this.mPid));
            if (!procDir.exists()) {
                return false;
            }
            String[] procStatsString = new String[6];
            if (ProcessEx.readProcFile(new File(procDir, NativeAppMemRepair.STAT_PATH).toString(), NativeAppMemRepair.PROCESS_FULL_STATS_FORMAT, procStatsString, new long[6], (float[]) null)) {
                if (this.mProcessName.equals(procStatsString[0])) {
                    return true;
                }
            }
            return false;
        }

        private void updateCurrentPss() {
            this.mCurrentPss = ResourceCollector.getPss(this.mPid, (long[]) null, (long[]) null);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(NativeAppMemRepair.TAG, "mCurrentPss is " + this.mCurrentPss + " mPid = " + this.mPid);
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("pid:");
            sb.append(this.mPid);
            sb.append(",processName:");
            sb.append(this.mProcessName);
            sb.append(",threshold:");
            sb.append(this.mPssThreshold);
            sb.append(",current threshold:");
            sb.append(this.mCurrentPss);
            return sb.toString();
        }
    }
}
