package com.android.server.rms.iaware.cpu;

import android.iawareperf.RtgSchedController;
import android.os.Process;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.collector.ResourceCollector;
import com.huawei.android.os.ProcessExt;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuThreadMinUtilBoost {
    private static final String CGROUP_PROCS_NODE = "/dev/cpuctl/cgroup.procs";
    private static final int INVALID_VALUE = -1;
    private static final int MAX_MINUTIL_VALUE = 1024;
    private static final int MAX_PID_VALUE = 950;
    private static final int MIN_MINUTIL_VALUE = 0;
    private static final int MIN_PID_VALUE = 650;
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuThreadMinUtilBoost";
    private static final int TYPE_FOCUS_CHANGE = 3;
    private static final String TYPE_PROCESS = "process";
    private static final String TYPE_THREAD = "thread";
    private static CpuThreadMinUtilBoost sInstance;
    private AtomicBoolean mBoostEnable = new AtomicBoolean(false);
    private int mBoostPid = -1;
    private int mBoostRenderTid = -1;
    private RtgSchedController mMinUtilController;
    private Map<String, Integer> mPidMinUtilConfig = new ArrayMap();
    private Map<String, Integer> mSetThreadMinUtilMap = new ArrayMap();
    private Map<String, Integer> mThreadMinUtilConfig = new ArrayMap();

    private CpuThreadMinUtilBoost() {
    }

    public static CpuThreadMinUtilBoost getInstance() {
        CpuThreadMinUtilBoost cpuThreadMinUtilBoost;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuThreadMinUtilBoost();
            }
            cpuThreadMinUtilBoost = sInstance;
        }
        return cpuThreadMinUtilBoost;
    }

    public void setBoostConifg(Map<String, Integer> config, String type) {
        int tmpMinUtil;
        if (config == null || type == null) {
            AwareLog.w(TAG, "setBoostConifg config is null.");
            return;
        }
        for (Map.Entry<String, Integer> entry : config.entrySet()) {
            String tidName = entry.getKey();
            Integer minUtil = entry.getValue();
            if (tidName != null && minUtil != null && (tmpMinUtil = minUtil.intValue()) >= 0 && tmpMinUtil <= 1024) {
                if (TYPE_PROCESS.equals(type)) {
                    this.mPidMinUtilConfig.put(tidName, minUtil);
                }
                if (TYPE_THREAD.equals(type)) {
                    this.mThreadMinUtilConfig.put(tidName, minUtil);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initBoostConifg() {
        initSystemServerThreads(Process.myPid());
        initPids();
    }

    public void start(CpuFeature feature) {
        if (!this.mBoostEnable.get()) {
            initMinUtilController();
            setThreadMinUtilBoost();
            this.mBoostEnable.set(true);
        }
    }

    public void stop() {
        if (this.mBoostEnable.get()) {
            this.mBoostEnable.set(false);
            resetThreadMinUtilBoost();
            resetMinUtil();
            this.mMinUtilController = null;
        }
    }

    private void setThreadMinUtilBoost() {
        AwareLog.i(TAG, "mSetThreadMinUtilMap : " + this.mSetThreadMinUtilMap);
        for (Map.Entry<String, Integer> entry : this.mSetThreadMinUtilMap.entrySet()) {
            String tidName = entry.getKey();
            if (tidName != null) {
                Integer minUtil = this.mPidMinUtilConfig.get(tidName);
                if (minUtil == null) {
                    minUtil = this.mThreadMinUtilConfig.get(tidName);
                }
                Integer tid = entry.getValue();
                if (!(minUtil == null || tid == null || tid.intValue() <= 0)) {
                    ResourceCollector.setThreadMinUtil(tid.intValue(), minUtil.intValue());
                }
            }
        }
    }

    private void initPids() {
        List<String> vipPidNameList = CpuVipThread.getInstance().getVipPidList();
        if (!((vipPidNameList == null || vipPidNameList.isEmpty()) && this.mPidMinUtilConfig.isEmpty())) {
            List<String> procStrList = CpuCommonUtil.getProcsList(CGROUP_PROCS_NODE);
            if (procStrList == null || procStrList.isEmpty()) {
                AwareLog.e(TAG, "initPids fail.");
                return;
            }
            int minUtilPidSize = this.mPidMinUtilConfig.size();
            int vipSize = vipPidNameList.size();
            for (String pidStr : procStrList) {
                if (minUtilPidSize > 0 || vipSize > 0) {
                    int pid = CpuCommonUtil.parseInt(pidStr);
                    if (pid >= MIN_PID_VALUE && pid <= MAX_PID_VALUE) {
                        String comm = CpuCommonUtil.getThreadName("/proc/" + pidStr);
                        if (comm != null) {
                            if (vipPidNameList.contains(comm)) {
                                vipSize--;
                                CpuVipThread.getInstance().setVipPidList(comm, pid);
                            }
                            if (this.mPidMinUtilConfig.containsKey(comm)) {
                                minUtilPidSize--;
                                this.mSetThreadMinUtilMap.put(comm, Integer.valueOf(pid));
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void initSystemServerThreads(int pid) {
        File[] subFiles = new File("/proc/" + pid + "/task/").listFiles();
        if (subFiles == null) {
            AwareLog.e(TAG, "initSystemServerThreads null");
            return;
        }
        int minUtilThreadSize = this.mThreadMinUtilConfig.size();
        Map<String, Integer> mFifoSchedConfig = CpuFifoSched.getInstance().getFifoConfig();
        int fifoThreadSize = 0;
        if (mFifoSchedConfig != null) {
            fifoThreadSize = mFifoSchedConfig.size();
        }
        for (File eachTidFile : subFiles) {
            if (minUtilThreadSize > 0 || fifoThreadSize > 0) {
                try {
                    String tidPath = eachTidFile.getCanonicalPath();
                    String tidName = CpuCommonUtil.getThreadName(tidPath);
                    if (tidName != null) {
                        if (this.mThreadMinUtilConfig.containsKey(tidName)) {
                            minUtilThreadSize--;
                            int tid = CpuCommonUtil.getThreadId(tidPath);
                            if (tid > 0) {
                                this.mSetThreadMinUtilMap.put(tidName, Integer.valueOf(tid));
                            }
                        }
                        if (mFifoSchedConfig != null && mFifoSchedConfig.containsKey(tidName)) {
                            fifoThreadSize--;
                            CpuFifoSched.getInstance().setFifoSchedThread(tidName, tidPath);
                        }
                    }
                } catch (IOException e) {
                }
            } else {
                return;
            }
        }
    }

    private void resetThreadMinUtilBoost() {
        Integer tid;
        for (Map.Entry<String, Integer> entry : this.mSetThreadMinUtilMap.entrySet()) {
            if (!(entry.getKey() == null || (tid = entry.getValue()) == null || tid.intValue() <= 0)) {
                ResourceCollector.setThreadMinUtil(tid.intValue(), 0);
            }
        }
        this.mSetThreadMinUtilMap.clear();
        this.mPidMinUtilConfig.clear();
        this.mThreadMinUtilConfig.clear();
    }

    private void initMinUtilController() {
        if (this.mMinUtilController == null) {
            this.mMinUtilController = AwareRmsRtgSchedPlugin.getInstance().getRtgSchedController();
        }
    }

    private void resetMinUtil() {
        if (this.mBoostPid != -1) {
            this.mBoostPid = -1;
            this.mBoostRenderTid = -1;
            setThreadBoost(-1, -1);
        }
    }

    /* access modifiers changed from: protected */
    public void focusChangeReport(int pid, int type) {
        if (this.mBoostEnable.get() && type == 3) {
            int appType = CpuCommonUtil.getAppTypeByPid(pid);
            if (appType == 8 || appType == 21) {
                setMinUtil(pid);
            } else {
                resetMinUtil();
            }
        }
    }

    private void setMinUtil(int pid) {
        int renderTid = CpuCommonUtil.getRenderTid(pid);
        if (pid > 0 && renderTid > 0) {
            setThreadBoost(pid, renderTid);
            this.mBoostPid = pid;
            this.mBoostRenderTid = renderTid;
        }
    }

    private void setThreadBoost(int pid, int renderTid) {
        if (this.mMinUtilController == null) {
            AwareLog.w(TAG, "setThreadBoost failed: null controller.");
            return;
        }
        int uid = ProcessExt.getUidForPid(pid);
        StringBuilder builder = new StringBuilder();
        builder.append("boostUid:");
        builder.append(uid);
        builder.append(";utid:");
        builder.append(pid);
        builder.append(";rtid:");
        builder.append(renderTid);
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "setThreadBoost: " + builder.toString());
        }
        this.mMinUtilController.setRtgThread(builder.toString());
    }
}
