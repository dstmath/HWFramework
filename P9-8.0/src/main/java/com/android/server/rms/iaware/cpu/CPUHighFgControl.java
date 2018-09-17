package com.android.server.rms.iaware.cpu;

import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.SparseArray;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CPUHighFgControl {
    private static final String CGROUP_CPUACCT = ":cpuacct";
    private static final String CGROUP_CPUSET = ":cpuset";
    private static final String CGROUP_CPUSET_BG = "background";
    private static final String CPUCTL_LIMIT_PROC_PATH = "/dev/cpuctl/limit/cgroup.procs";
    private static final String CPUCTL_PROC_PATH = "/dev/cpuctl/cgroup.procs";
    private static final String CPUSET_FG_PROC_PATH = "/dev/cpuset/foreground/cgroup.procs";
    private static final String CPUSET_TA_PROC_PATH = "/dev/cpuset/top-app/cgroup.procs";
    private static final int CPU_FG_LOAD_LIMIT_DEFAULT = 60;
    private static final int CPU_FG_LOAD_THRESHOLD_DEFAULT = 80;
    private static final int CPU_HIGHLOAD_POLLING_INTERVAL_DEFAULT = 1000;
    private static final int CPU_LOAD_LOW_THRESHOLD_DEFAULT = 60;
    private static final int CPU_LOAD_THRESHOLD_MAX = 100;
    private static final int CPU_LOAD_THRESHOLD_MIN = 20;
    private static final int CPU_POLLING_INTERVAL_MAX_VALUE = 5000;
    private static final int CPU_POLLING_INTERVAL_MIN_VALUE = 100;
    private static final String CPU_STAT_PATH = "/proc/stat";
    private static final int CPU_TA_LOAD_REGULAR_THRESHOLD_DEFAULT = 75;
    private static final int CPU_TA_LOAD_THRESHOLD_DEFAULT = 90;
    private static final long INVALID_VALUE = -1;
    private static final String ITEM_CPU_FG_LOAD_LIMIT = "cpu_fg_load_limit";
    private static final String ITEM_CPU_FG_LOAD_THRESHOLD = "cpu_fg_load_threshold";
    private static final String ITEM_CPU_HIGHLOAD_POLLING_INTERVAL = "cpu_highload_polling_interval";
    private static final String ITEM_CPU_LOAD_LOW_THRESHOLD = "cpu_load_low_threshold";
    private static final String ITEM_CPU_TA_LOAD_THRESHOLD = "cpu_ta_load_threshold";
    private static final int LOAD_DETCTOR_DELAY = 10000;
    private static final int LOAD_STATUS_DETECTOR = 4;
    private static final int LOAD_STATUS_HIGH = 2;
    private static final int LOAD_STATUS_IDLE = 3;
    private static final int LOAD_STATUS_LOW = 1;
    private static final int MSG_LOAD_DETCTOR = 1;
    private static final String PATH_COMM = "/comm";
    private static final String PATH_STAT = "/stat";
    private static final String PTAH_CGROUP = "/cgroup";
    private static final String PTAH_PROC_INFO = "/proc/";
    private static final String SYSTEMUI_NAME = "com.android.systemui";
    private static final String TAG = "CPUHighFgControl";
    private static final String UID_PTAH = "uid_";
    private static CPUHighFgControl sInstance;
    private CPUFeature mCPUFeatureInstance;
    private CPUZRHungLog mCPUZRHung;
    private int mCpuFgLoadLimit = 60;
    private int mCpuFgLoadThreshold = CPU_FG_LOAD_THRESHOLD_DEFAULT;
    private int mCpuLoadLowThreshold = 60;
    private int mCpuTaLoadThreshold = CPU_TA_LOAD_THRESHOLD_DEFAULT;
    private volatile boolean mIsStart = false;
    private CpuLoadDetectorHandler mLoadDetectorHandler;
    private Thread mLoadDetectorThread;
    private int mLoadStatus = 3;
    private Object mLock = new Object();
    private int mPollingInterval = 1000;
    private ProcLoadComparator mProcLoadComparator;
    private TALoadDetectorThread mTaLoadDetectorThread;

    private class CpuLoadDetectorHandler extends Handler {
        /* synthetic */ CpuLoadDetectorHandler(CPUHighFgControl this$0, CpuLoadDetectorHandler -this1) {
            this();
        }

        private CpuLoadDetectorHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 1:
                    CPUHighFgControl.this.notifyLoadChange(4);
                    return;
                default:
                    AwareLog.e(CPUHighFgControl.TAG, "CpuLoadDetectorHandler msg = " + what + " not support!");
                    return;
            }
        }
    }

    private static class ProcLoadComparator implements Comparator<ProcLoadPair>, Serializable {
        private static final long serialVersionUID = 1;

        /* synthetic */ ProcLoadComparator(ProcLoadComparator -this0) {
            this();
        }

        private ProcLoadComparator() {
        }

        public int compare(ProcLoadPair lhs, ProcLoadPair rhs) {
            return Integer.compare(rhs.load, lhs.load);
        }
    }

    private static class ProcLoadPair {
        public int load;
        public int pid;

        public ProcLoadPair(int pid, int load) {
            this.pid = pid;
            this.load = load;
        }
    }

    private static class ProcessStatInfo {
        public long idle;
        public long iowait;
        public long irq;
        public long nice;
        public long softIrq;
        public long system;
        public long userTime;

        /* synthetic */ ProcessStatInfo(ProcessStatInfo -this0) {
            this();
        }

        private ProcessStatInfo() {
        }

        public long getWallTime() {
            return (((((this.userTime + this.nice) + this.system) + this.idle) + this.iowait) + this.irq) + this.softIrq;
        }

        public long getRunningTime() {
            return (this.userTime + this.nice) + this.system;
        }
    }

    private class TALoadDetectorThread implements Runnable {
        /* synthetic */ TALoadDetectorThread(CPUHighFgControl this$0, TALoadDetectorThread -this1) {
            this();
        }

        private TALoadDetectorThread() {
        }

        public void run() {
            Thread.currentThread().setPriority(10);
            while (CPUHighFgControl.this.mIsStart) {
                synchronized (CPUHighFgControl.this.mLock) {
                    while (CPUHighFgControl.this.mLoadStatus == 3) {
                        try {
                            CPUHighFgControl.this.mLock.wait();
                        } catch (InterruptedException e) {
                            AwareLog.e(CPUHighFgControl.TAG, "TALoadDetectorThread InterruptedException return");
                        }
                    }
                    if (CPUHighFgControl.this.mLoadStatus == 4) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for load detector!");
                        CPUHighFgControl.this.startLoadDetector();
                        CPUHighFgControl.this.mLoadStatus = 3;
                    } else if (CPUHighFgControl.this.mLoadStatus == 2) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for high load!");
                        CPUHighFgControl.this.setHighLoadPid();
                        CPUHighFgControl.this.mLoadStatus = 3;
                        CPUHighFgControl.this.rotateStatus(2);
                        CPUHighFgControl.this.sendMessageForLoadDetector();
                    } else if (CPUHighFgControl.this.mLoadStatus == 1) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for low load!");
                        CPUHighFgControl.this.setLowLoad();
                        CPUHighFgControl.this.mLoadStatus = 3;
                        CPUHighFgControl.this.rotateStatus(1);
                        CPUHighFgControl.this.sendMessageForLoadDetector();
                    }
                }
            }
        }
    }

    private CPUHighFgControl() {
        initXml();
        this.mCPUZRHung = new CPUZRHungLog();
        this.mProcLoadComparator = new ProcLoadComparator();
        this.mLoadDetectorHandler = new CpuLoadDetectorHandler(this, null);
    }

    public static synchronized CPUHighFgControl getInstance() {
        CPUHighFgControl cPUHighFgControl;
        synchronized (CPUHighFgControl.class) {
            if (sInstance == null) {
                sInstance = new CPUHighFgControl();
            }
            cPUHighFgControl = sInstance;
        }
        return cPUHighFgControl;
    }

    private boolean isValidValue(Integer value, int min, int max) {
        if (value == null || value.intValue() <= min || value.intValue() >= max) {
            return false;
        }
        return true;
    }

    private void initXml() {
        Map<String, Integer> cpuCtlFGValueMap = new CpuCtlLimitConfig().getCpuCtlFGValueMap();
        if (!cpuCtlFGValueMap.isEmpty()) {
            Integer value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_HIGHLOAD_POLLING_INTERVAL);
            if (isValidValue(value, 100, CPU_POLLING_INTERVAL_MAX_VALUE)) {
                this.mPollingInterval = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_TA_LOAD_THRESHOLD);
            if (isValidValue(value, 20, 100)) {
                this.mCpuTaLoadThreshold = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_FG_LOAD_THRESHOLD);
            if (isValidValue(value, 20, 100)) {
                this.mCpuFgLoadThreshold = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_FG_LOAD_LIMIT);
            if (isValidValue(value, 20, 100)) {
                this.mCpuFgLoadLimit = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_LOAD_LOW_THRESHOLD);
            if (isValidValue(value, 20, 100)) {
                this.mCpuLoadLowThreshold = value.intValue();
            }
        }
    }

    public void start(CPUFeature feature) {
        this.mCPUFeatureInstance = feature;
        if (this.mTaLoadDetectorThread == null) {
            this.mTaLoadDetectorThread = new TALoadDetectorThread(this, null);
        }
        if (this.mLoadDetectorThread == null || (this.mLoadDetectorThread.isAlive() ^ 1) != 0) {
            this.mLoadDetectorThread = new Thread(this.mTaLoadDetectorThread, "taLoadDetectorThread");
        }
        this.mIsStart = true;
        this.mLoadDetectorThread.start();
    }

    public void stop() {
        this.mLoadDetectorHandler.removeMessages(1);
        if (this.mLoadDetectorThread != null && this.mLoadDetectorThread.isAlive()) {
            this.mIsStart = false;
            this.mLoadDetectorThread.interrupt();
        }
        this.mLoadDetectorThread = null;
    }

    public void notifyLoadChange(int cpuLoadStatus) {
        AwareLog.d(TAG, "notifyLoadChange cpuLoadStatus " + cpuLoadStatus);
        synchronized (this.mLock) {
            this.mLoadStatus = cpuLoadStatus;
            this.mLock.notifyAll();
        }
    }

    private int taLoadCompute() {
        long totalCpuTimeLocal = getCpuLoad();
        int pid = getFileContent(CPUSET_TA_PROC_PATH);
        if (pid == -1) {
            return 0;
        }
        long processCpuTimeLocal = getProcessLoad(pid);
        if (totalCpuTimeLocal == -1 || processCpuTimeLocal == -1) {
            return 0;
        }
        try {
            Thread.sleep((long) this.mPollingInterval);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "taLoadCompute InterruptedException");
        }
        if (pid != getFileContent(CPUSET_TA_PROC_PATH)) {
            return 0;
        }
        long nextProcessLoad = getProcessLoad(pid);
        long nextCpuLoad = getCpuLoad();
        if (nextCpuLoad == -1 || nextProcessLoad == -1) {
            return 0;
        }
        long totalDeltaCpuTime = nextCpuLoad - totalCpuTimeLocal;
        long totalDeltaProcessTime = nextProcessLoad - processCpuTimeLocal;
        if (totalDeltaCpuTime == 0) {
            return 0;
        }
        return (int) ((100 * totalDeltaProcessTime) / totalDeltaCpuTime);
    }

    private int computeFgProcLoad(List<ProcLoadPair> fgProcLoadArray) {
        if (fgProcLoadArray == null) {
            return 0;
        }
        long totalCpuTime = getCpuLoad();
        if (-1 == totalCpuTime) {
            return 0;
        }
        SparseArray<Long> pidLoadList = new SparseArray();
        getFgCpuloadPidArray(pidLoadList);
        try {
            Thread.sleep((long) this.mPollingInterval);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "computeFgProcLoad InterruptedException");
        }
        SparseArray<Long> nextPidLoadList = new SparseArray();
        getFgCpuloadPidArray(nextPidLoadList);
        long nextTotalCpuTime = getCpuLoad();
        if (-1 == nextTotalCpuTime) {
            return 0;
        }
        long totalCpuRuntime = nextTotalCpuTime - totalCpuTime;
        if (totalCpuRuntime == 0) {
            return 0;
        }
        return computeProcsLoad(pidLoadList, nextPidLoadList, totalCpuRuntime, fgProcLoadArray);
    }

    private int computeProcsLoad(SparseArray<Long> prevArray, SparseArray<Long> currArray, long totalTime, List<ProcLoadPair> fgProcLoadArray) {
        if (prevArray == null || currArray == null || totalTime == 0 || fgProcLoadArray == null) {
            AwareLog.e(TAG, "computeProcsLoad invalid params");
            return 0;
        }
        int totalLoad = 0;
        int currArraySize = currArray.size();
        for (int i = 0; i < currArraySize; i++) {
            int procPid = currArray.keyAt(i);
            if (prevArray.indexOfKey(procPid) >= 0) {
                int procLoad = (int) ((100 * (((Long) currArray.get(procPid)).longValue() - ((Long) prevArray.get(procPid)).longValue())) / totalTime);
                if (procLoad == 0) {
                    AwareLog.d(TAG, "pid " + procPid + " cpuload is 0 ");
                } else {
                    fgProcLoadArray.add(new ProcLoadPair(procPid, procLoad));
                    totalLoad += procLoad;
                }
            }
        }
        if (!fgProcLoadArray.isEmpty()) {
            Collections.sort(fgProcLoadArray, this.mProcLoadComparator);
        }
        return totalLoad;
    }

    private void getFgProcsloadPidArray(String filePath, SparseArray<Long> pidList) {
        List<String> procsList = getProcsList(filePath);
        if (procsList != null) {
            int size = procsList.size();
            for (int i = 0; i < size; i++) {
                int pid = parseInt((String) procsList.get(i));
                if (pid >= 0 && (isFgProc(pid) ^ 1) == 0 && (isLimitProc(pid) ^ 1) == 0) {
                    long tempLoad = getProcessLoad(pid);
                    if (-1 != tempLoad) {
                        pidList.put(pid, Long.valueOf(tempLoad));
                    }
                }
            }
        }
    }

    private List<String> getProcsList(String filePath) {
        File file = new File(filePath);
        if (file.exists() && (file.canRead() ^ 1) == 0) {
            String groupProcs = null;
            try {
                groupProcs = FileUtils.readTextFile(file, 0, null);
            } catch (IOException e) {
                AwareLog.e(TAG, "IOException + " + e.getMessage());
            }
            if (groupProcs == null || groupProcs.length() == 0) {
                return null;
            }
            String[] procs = groupProcs.split("\n");
            List<String> procsList = new ArrayList();
            for (String str : procs) {
                String strProc = str.trim();
                if (strProc.length() != 0) {
                    procsList.add(strProc);
                }
            }
            return procsList;
        }
        AwareLog.e(TAG, "getProcsList file not exists or canot read!");
        return null;
    }

    private void getLimitFgProcsLoadArray(String filePath, SparseArray<Long> pidList) {
        List<String> procsList = getProcsList(filePath);
        if (procsList != null) {
            int size = procsList.size();
            for (int i = 0; i < size; i++) {
                int pid = parseInt((String) procsList.get(i));
                if (pid >= 0) {
                    long tempLoad = getProcessLoad(pid);
                    if (-1 != tempLoad) {
                        pidList.put(pid, Long.valueOf(tempLoad));
                    }
                }
            }
        }
    }

    private void getFgCpuloadPidArray(SparseArray<Long> pidList) {
        getFgProcsloadPidArray(CPUSET_FG_PROC_PATH, pidList);
    }

    private void getLimitFgCpuloadPidArray(SparseArray<Long> pidList) {
        getLimitFgProcsLoadArray(CPUCTL_LIMIT_PROC_PATH, pidList);
    }

    private int computeLoad(long deltaTotaltime, long deltaRuntime) {
        if (deltaTotaltime != 0) {
            return (int) ((100 * deltaRuntime) / deltaTotaltime);
        }
        AwareLog.e(TAG, "computeLoad deltaTotaltime is zero!");
        return -1;
    }

    private void startLoadDetector() {
        ProcessStatInfo prevCpuStatInfo = getCpuStatInfo();
        if (prevCpuStatInfo == null) {
            AwareLog.e(TAG, "startLoadDetector prevStatInfo is null!");
            return;
        }
        SparseArray<Long> prevLimFgArray = new SparseArray();
        getLimitFgCpuloadPidArray(prevLimFgArray);
        try {
            Thread.sleep((long) (this.mPollingInterval * 3));
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "startLoadDetector InterruptedException");
        }
        SparseArray<Long> currLimFgArray = new SparseArray();
        getLimitFgCpuloadPidArray(currLimFgArray);
        ProcessStatInfo currCpuStatInfo = getCpuStatInfo();
        if (currCpuStatInfo == null) {
            AwareLog.e(TAG, "startLoadDetector currStatInfo is null!");
            return;
        }
        long deltaTotalCpuRuntime = currCpuStatInfo.getWallTime() - prevCpuStatInfo.getWallTime();
        int cpuLoad = computeLoad(deltaTotalCpuRuntime, currCpuStatInfo.getRunningTime() - prevCpuStatInfo.getRunningTime());
        if (cpuLoad == -1) {
            AwareLog.e(TAG, "startLoadDetector computeLoad maybe error!");
            return;
        }
        loadDetectorPolicy(cpuLoad, deltaTotalCpuRuntime, prevLimFgArray, currLimFgArray);
        if (cpuLoad >= this.mCpuTaLoadThreshold) {
            loadZRHungPolicy();
        }
    }

    private boolean isLimitGroupHasPid() {
        List<String> limitPidList = getProcsList(CPUCTL_LIMIT_PROC_PATH);
        if (limitPidList == null || limitPidList.size() <= 0) {
            return false;
        }
        return true;
    }

    private void loadDetectorPolicy(int cpuLoad, long cpuTotalTime, SparseArray<Long> prevArray, SparseArray<Long> currArray) {
        AwareLog.d(TAG, "loadDetectorPolicy cpu_total_Load = " + cpuLoad);
        if (cpuLoad >= this.mCpuTaLoadThreshold) {
            int totalLimitFgLoad = computeProcsLoad(prevArray, currArray, cpuTotalTime, new ArrayList());
            AwareLog.d(TAG, "loadDetectorPolicy limit totalLoad = " + totalLimitFgLoad);
            if (totalLimitFgLoad >= this.mCpuTaLoadThreshold) {
                AwareLog.d(TAG, "loadDetectorPolicy limit + ta load high, and a new high load process may be add in the system, so continue detector!");
            } else {
                AwareLog.d(TAG, "limit low, need to find another high load pid to control");
                setHighLoadPid();
            }
        } else if (cpuLoad <= this.mCpuLoadLowThreshold) {
            AwareLog.d(TAG, "loadDetectorPolicy setLowLoad");
            setLowLoad();
        }
        if (isLimitGroupHasPid()) {
            AwareLog.d(TAG, "loadDetectorPolicy limit group has pid, so continue detector!");
            sendMessageForLoadDetector();
        }
    }

    private void loadZRHungPolicy() {
        this.mCPUZRHung.loadPolicy();
    }

    private void rotateStatus(int loadStatus) {
        this.mCPUZRHung.rotateStatus(loadStatus);
    }

    private void chgProcessGroup(int pid, String groupPath) {
        if (pid > 0) {
            setGroup(pid, groupPath);
        }
    }

    private void chgProcessGroup(List<ProcLoadPair> procLoadArray, String groupPath) {
        if (procLoadArray != null) {
            int currLoad = 0;
            int size = procLoadArray.size();
            for (int i = 0; i < size; i++) {
                ProcLoadPair pair = (ProcLoadPair) procLoadArray.get(i);
                if (pair != null) {
                    if (currLoad >= this.mCpuFgLoadLimit) {
                        break;
                    }
                    AwareLog.d(TAG, "chgProcessGroup list pid = " + pair.pid + " load = " + pair.load);
                    currLoad += pair.load;
                    chgProcessGroup(pair.pid, groupPath);
                }
            }
        }
    }

    private int getFileContent(String filePath) {
        NumberFormatException e;
        Throwable th;
        if (filePath == null) {
            return -1;
        }
        File file = new File(filePath);
        if (!file.exists() || (file.canRead() ^ 1) != 0) {
            return -1;
        }
        int pid = 0;
        FileInputStream input = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufReader = null;
        try {
            InputStreamReader inputStreamReader2;
            FileInputStream input2 = new FileInputStream(filePath);
            try {
                inputStreamReader2 = new InputStreamReader(input2, "UTF-8");
            } catch (NumberFormatException e2) {
                e = e2;
                input = input2;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (FileNotFoundException e3) {
                input = input2;
                AwareLog.e(TAG, "exception file not found, file path: " + filePath);
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (UnsupportedEncodingException e4) {
                input = input2;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (IOException e5) {
                input = input2;
                try {
                    AwareLog.e(TAG, "IOException");
                    FileContent.closeBufferedReader(bufReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(input);
                    return pid;
                } catch (Throwable th2) {
                    th = th2;
                    FileContent.closeBufferedReader(bufReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(input);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                input = input2;
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                throw th;
            }
            try {
                BufferedReader bufReader2 = new BufferedReader(inputStreamReader2);
                while (true) {
                    try {
                        String content = bufReader2.readLine();
                        if (content == null) {
                            break;
                        }
                        pid = Integer.parseInt(content.trim());
                        if (isFgProc(pid)) {
                            break;
                        }
                        pid = 0;
                    } catch (NumberFormatException e6) {
                        e = e6;
                        bufReader = bufReader2;
                        inputStreamReader = inputStreamReader2;
                        input = input2;
                        AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                        FileContent.closeBufferedReader(bufReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(input);
                        return pid;
                    } catch (FileNotFoundException e7) {
                        bufReader = bufReader2;
                        inputStreamReader = inputStreamReader2;
                        input = input2;
                        AwareLog.e(TAG, "exception file not found, file path: " + filePath);
                        FileContent.closeBufferedReader(bufReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(input);
                        return pid;
                    } catch (UnsupportedEncodingException e8) {
                        bufReader = bufReader2;
                        inputStreamReader = inputStreamReader2;
                        input = input2;
                        AwareLog.e(TAG, "UnsupportedEncodingException ");
                        FileContent.closeBufferedReader(bufReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(input);
                        return pid;
                    } catch (IOException e9) {
                        bufReader = bufReader2;
                        inputStreamReader = inputStreamReader2;
                        input = input2;
                        AwareLog.e(TAG, "IOException");
                        FileContent.closeBufferedReader(bufReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(input);
                        return pid;
                    } catch (Throwable th4) {
                        th = th4;
                        bufReader = bufReader2;
                        inputStreamReader = inputStreamReader2;
                        input = input2;
                        FileContent.closeBufferedReader(bufReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(input);
                        throw th;
                    }
                }
                FileContent.closeBufferedReader(bufReader2);
                FileContent.closeInputStreamReader(inputStreamReader2);
                FileContent.closeFileInputStream(input2);
                input = input2;
            } catch (NumberFormatException e10) {
                e = e10;
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (FileNotFoundException e11) {
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "exception file not found, file path: " + filePath);
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (UnsupportedEncodingException e12) {
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "UnsupportedEncodingException ");
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (IOException e13) {
                inputStreamReader = inputStreamReader2;
                input = input2;
                AwareLog.e(TAG, "IOException");
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                return pid;
            } catch (Throwable th5) {
                th = th5;
                inputStreamReader = inputStreamReader2;
                input = input2;
                FileContent.closeBufferedReader(bufReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(input);
                throw th;
            }
        } catch (NumberFormatException e14) {
            e = e14;
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
            FileContent.closeBufferedReader(bufReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(input);
            return pid;
        } catch (FileNotFoundException e15) {
            AwareLog.e(TAG, "exception file not found, file path: " + filePath);
            FileContent.closeBufferedReader(bufReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(input);
            return pid;
        } catch (UnsupportedEncodingException e16) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
            FileContent.closeBufferedReader(bufReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(input);
            return pid;
        } catch (IOException e17) {
            AwareLog.e(TAG, "IOException");
            FileContent.closeBufferedReader(bufReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(input);
            return pid;
        }
        return pid;
    }

    private void setHighLoadPid() {
        int taLoad = taLoadCompute();
        AwareLog.d(TAG, "setHighLoadPid taLoadCompute = " + taLoad);
        setHightLoad(taLoad);
    }

    private void setHightLoad(int taLoad) {
        AwareLog.d(TAG, "setHighLoad taLoad is " + taLoad);
        if (taLoad >= this.mCpuTaLoadThreshold) {
            chgProcessGroup(getFileContent(CPUSET_TA_PROC_PATH), CPUCTL_LIMIT_PROC_PATH);
            return;
        }
        List procLoadArray = new ArrayList();
        int totalProcLoad = computeFgProcLoad(procLoadArray);
        int procLoadArraySize = procLoadArray.size();
        for (int i = 0; i < procLoadArraySize; i++) {
            AwareLog.d(TAG, "fg the i= " + i + " load is " + ((ProcLoadPair) procLoadArray.get(i)).load + " pid is " + ((ProcLoadPair) procLoadArray.get(i)).pid);
        }
        AwareLog.d(TAG, "setHighLoadPid computeFgProcLoad = " + totalProcLoad);
        if (totalProcLoad >= this.mCpuFgLoadThreshold || taLoad + totalProcLoad >= this.mCpuTaLoadThreshold) {
            chgProcessGroup(procLoadArray, CPUCTL_LIMIT_PROC_PATH);
            return;
        }
        if (taLoad >= CPU_TA_LOAD_REGULAR_THRESHOLD_DEFAULT) {
            chgProcessGroup(getFileContent(CPUSET_TA_PROC_PATH), CPUCTL_LIMIT_PROC_PATH);
        }
    }

    private void setLowLoad() {
        File file = new File(CPUCTL_LIMIT_PROC_PATH);
        if (file.exists() && (file.canRead() ^ 1) == 0) {
            String groupProcs = null;
            try {
                groupProcs = FileUtils.readTextFile(file, 0, null);
            } catch (IOException e) {
                AwareLog.e(TAG, "IOException + " + e.getMessage());
            }
            if (groupProcs == null || groupProcs.length() == 0) {
                AwareLog.d(TAG, "setLowLoad readTextFile null procs!");
                return;
            }
            for (String str : groupProcs.split("\n")) {
                String strProc = str.trim();
                if (strProc.length() != 0) {
                    int pid = parseInt(strProc);
                    if (pid != -1) {
                        setGroup(pid, CPUCTL_PROC_PATH);
                    }
                }
            }
            return;
        }
        AwareLog.e(TAG, "setLowLoad file not exists or canot read!");
    }

    private void setGroup(int pid, String file) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        if (CPUCTL_LIMIT_PROC_PATH.equals(file)) {
            buffer.putInt(CPUFeature.MSG_SET_LIMIT_CGROUP);
        } else if (CPUCTL_PROC_PATH.equals(file)) {
            buffer.putInt(CPUFeature.MSG_SET_FG_CGROUP);
        } else {
            return;
        }
        buffer.putInt(pid);
        AwareLog.i(TAG, "setGroup pid = " + pid + " processgroup = " + file);
        if (this.mCPUFeatureInstance != null) {
            this.mCPUFeatureInstance.sendPacket(buffer);
        }
    }

    private boolean isLimitProc(int pid) {
        StringBuilder path = new StringBuilder();
        path.append(PTAH_PROC_INFO);
        path.append(pid);
        path.append(PATH_COMM);
        File file = new File(path.toString());
        if (file.exists() && (file.canRead() ^ 1) == 0) {
            String procname = null;
            try {
                procname = FileUtils.readTextFile(file, 0, null);
            } catch (IOException e) {
                AwareLog.e(TAG, "isLimitProc IOException + " + e.getMessage());
            }
            if (procname == null) {
                return false;
            }
            procname = procname.trim();
            if (!SYSTEMUI_NAME.contains(procname)) {
                return true;
            }
            AwareLog.d(TAG, "procname:" + procname + "is limit");
            return false;
        }
        AwareLog.e(TAG, "isLimitProc file not exists or canot read!" + path.toString());
        return false;
    }

    private boolean isFgProc(int pid) {
        StringBuilder targetPath = new StringBuilder();
        targetPath.append(PTAH_PROC_INFO);
        targetPath.append(pid);
        targetPath.append(PTAH_CGROUP);
        return isFgThirdPartProc(targetPath.toString());
    }

    private boolean isThirdPartyUid(String line) {
        if (line == null || (line.contains(CGROUP_CPUACCT) ^ 1) != 0) {
            return false;
        }
        boolean flag = false;
        int index = line.indexOf(UID_PTAH);
        if (index != -1) {
            int index_slash = line.indexOf(47, index);
            String strUid = null;
            if (index_slash != -1) {
                strUid = line.substring(UID_PTAH.length() + index, index_slash);
            }
            if (UserHandle.getAppId(parseInt(strUid)) > 10000) {
                flag = true;
            }
        }
        return flag;
    }

    private boolean isFgThirdPartProc(String path) {
        NumberFormatException e;
        FileNotFoundException e2;
        UnsupportedEncodingException e3;
        IOException e4;
        Throwable th;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            InputStreamReader isr2;
            FileInputStream fis2 = new FileInputStream(path);
            try {
                isr2 = new InputStreamReader(fis2, "UTF-8");
            } catch (NumberFormatException e5) {
                e = e5;
                fis = fis2;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (FileNotFoundException e6) {
                e2 = e6;
                fis = fis2;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (UnsupportedEncodingException e7) {
                e3 = e7;
                fis = fis2;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (IOException e8) {
                e4 = e8;
                fis = fis2;
                try {
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                throw th;
            }
            try {
                BufferedReader br2 = new BufferedReader(isr2);
                try {
                    String line = "";
                    boolean flag = false;
                    while (true) {
                        line = br2.readLine();
                        if (line == null) {
                            FileContent.closeBufferedReader(br2);
                            FileContent.closeInputStreamReader(isr2);
                            FileContent.closeFileInputStream(fis2);
                            fis = fis2;
                            break;
                        }
                        if (line.contains(CGROUP_CPUSET)) {
                            if (line.contains("background")) {
                                FileContent.closeBufferedReader(br2);
                                FileContent.closeInputStreamReader(isr2);
                                FileContent.closeFileInputStream(fis2);
                                return false;
                            }
                            flag = true;
                        }
                        if (flag && isThirdPartyUid(line)) {
                            FileContent.closeBufferedReader(br2);
                            FileContent.closeInputStreamReader(isr2);
                            FileContent.closeFileInputStream(fis2);
                            return true;
                        }
                    }
                } catch (NumberFormatException e9) {
                    e = e9;
                    br = br2;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return false;
                } catch (FileNotFoundException e10) {
                    e2 = e10;
                    br = br2;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return false;
                } catch (UnsupportedEncodingException e11) {
                    e3 = e11;
                    br = br2;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return false;
                } catch (IOException e12) {
                    e4 = e12;
                    br = br2;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    br = br2;
                    isr = isr2;
                    fis = fis2;
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    throw th;
                }
            } catch (NumberFormatException e13) {
                e = e13;
                isr = isr2;
                fis = fis2;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (FileNotFoundException e14) {
                e2 = e14;
                isr = isr2;
                fis = fis2;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (UnsupportedEncodingException e15) {
                e3 = e15;
                isr = isr2;
                fis = fis2;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (IOException e16) {
                e4 = e16;
                isr = isr2;
                fis = fis2;
                AwareLog.e(TAG, "IOException " + e4.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return false;
            } catch (Throwable th5) {
                th = th5;
                isr = isr2;
                fis = fis2;
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                throw th;
            }
        } catch (NumberFormatException e17) {
            e = e17;
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
            FileContent.closeBufferedReader(br);
            FileContent.closeInputStreamReader(isr);
            FileContent.closeFileInputStream(fis);
            return false;
        } catch (FileNotFoundException e18) {
            e2 = e18;
            AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
            FileContent.closeBufferedReader(br);
            FileContent.closeInputStreamReader(isr);
            FileContent.closeFileInputStream(fis);
            return false;
        } catch (UnsupportedEncodingException e19) {
            e3 = e19;
            AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
            FileContent.closeBufferedReader(br);
            FileContent.closeInputStreamReader(isr);
            FileContent.closeFileInputStream(fis);
            return false;
        } catch (IOException e20) {
            e4 = e20;
            AwareLog.e(TAG, "IOException " + e4.getMessage());
            FileContent.closeBufferedReader(br);
            FileContent.closeInputStreamReader(isr);
            FileContent.closeFileInputStream(fis);
            return false;
        }
        return false;
    }

    private long getCpuLoad() {
        String strContent = getContentWithOneLine(CPU_STAT_PATH, "cpu ");
        if (strContent == null) {
            AwareLog.e(TAG, "getCpuLoad null content!");
            return -1;
        }
        ProcessStatInfo info = getCpuStatInfo(strContent);
        if (info == null) {
            return -1;
        }
        return info.getWallTime();
    }

    private ProcessStatInfo getCpuStatInfo() {
        String strContent = getContentWithOneLine(CPU_STAT_PATH, "cpu ");
        if (strContent != null) {
            return getCpuStatInfo(strContent);
        }
        AwareLog.e(TAG, "getProcessLoad null content!");
        return null;
    }

    private List<String> getProcStatInfo(String content) {
        if (content == null || content.length() == 0) {
            AwareLog.e(TAG, "getWallTime null content!");
            return null;
        }
        String[] conts = content.split(" ");
        List<String> listTemp = new ArrayList();
        for (String str : conts) {
            if (!"".equals(str)) {
                listTemp.add(str);
            }
        }
        return listTemp;
    }

    private ProcessStatInfo getCpuStatInfo(String content) {
        List<String> listTemp = getProcStatInfo(content);
        if (listTemp == null || listTemp.size() < 10) {
            return null;
        }
        String strNice = (String) listTemp.get(2);
        String strSystem = (String) listTemp.get(3);
        String strIdle = (String) listTemp.get(4);
        String strIoWait = (String) listTemp.get(5);
        String strIrq = (String) listTemp.get(6);
        String strSoftIrq = (String) listTemp.get(7);
        long userTime = parseLong((String) listTemp.get(1));
        long nice = parseLong(strNice);
        long system = parseLong(strSystem);
        long idle = parseLong(strIdle);
        long iowait = parseLong(strIoWait);
        long irq = parseLong(strIrq);
        long softIrq = parseLong(strSoftIrq);
        if (userTime == -1 || nice == -1 || system == -1 || idle == -1 || iowait == -1 || irq == -1 || softIrq == -1) {
            AwareLog.e(TAG, "getWallTime inalid value!");
            return null;
        }
        ProcessStatInfo info = new ProcessStatInfo(null);
        info.idle = idle;
        info.iowait = iowait;
        info.irq = irq;
        info.nice = nice;
        info.softIrq = softIrq;
        info.system = system;
        info.userTime = userTime;
        return info;
    }

    private long parseLong(String str) {
        long value = -1;
        if (str == null || str.length() == 0) {
            return value;
        }
        try {
            value = Long.parseLong(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseLong NumberFormatException e = " + e.getMessage());
        }
        return value;
    }

    private int parseInt(String str) {
        int value = -1;
        if (str == null || str.length() == 0) {
            return value;
        }
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
        }
        return value;
    }

    private long getProcessLoad(int pid) {
        StringBuilder targetPath = new StringBuilder();
        targetPath.append(PTAH_PROC_INFO);
        targetPath.append(pid);
        targetPath.append(PATH_STAT);
        String content = getContentWithOneLine(targetPath.toString(), String.valueOf(pid));
        if (content != null) {
            return getPorcseeTime(content);
        }
        AwareLog.e(TAG, "getProcessLoad null content!");
        return -1;
    }

    private long getPorcseeTime(String content) {
        String[] conts = content.split(" ");
        if (conts.length < 15) {
            AwareLog.e(TAG, "getIdleTime content inalid = " + content);
            return -1;
        }
        String strUTime = conts[13];
        String strSTime = conts[14];
        long utime = parseLong(strUTime);
        long stime = parseLong(strSTime);
        if (utime != -1 && stime != -1) {
            return utime + stime;
        }
        AwareLog.e(TAG, "getPorcseeTime inalid value!");
        return -1;
    }

    private String getContentWithOneLine(String filePath, String keyword) {
        FileNotFoundException e;
        UnsupportedEncodingException e2;
        IOException e3;
        Throwable th;
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (file.exists() && (file.canRead() ^ 1) == 0) {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            String line = null;
            try {
                FileInputStream fis2 = new FileInputStream(filePath);
                try {
                    InputStreamReader isr2 = new InputStreamReader(fis2, "UTF-8");
                    try {
                        BufferedReader br2 = new BufferedReader(isr2);
                        do {
                            try {
                                line = br2.readLine();
                                if (line == null || keyword == null) {
                                    FileContent.closeBufferedReader(br2);
                                    FileContent.closeInputStreamReader(isr2);
                                    FileContent.closeFileInputStream(fis2);
                                    fis = fis2;
                                }
                            } catch (FileNotFoundException e4) {
                                e = e4;
                                br = br2;
                                isr = isr2;
                                fis = fis2;
                                AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                                FileContent.closeBufferedReader(br);
                                FileContent.closeInputStreamReader(isr);
                                FileContent.closeFileInputStream(fis);
                                return line;
                            } catch (UnsupportedEncodingException e5) {
                                e2 = e5;
                                br = br2;
                                isr = isr2;
                                fis = fis2;
                                AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                                FileContent.closeBufferedReader(br);
                                FileContent.closeInputStreamReader(isr);
                                FileContent.closeFileInputStream(fis);
                                return line;
                            } catch (IOException e6) {
                                e3 = e6;
                                br = br2;
                                isr = isr2;
                                fis = fis2;
                                try {
                                    AwareLog.e(TAG, "IOException " + e3.getMessage());
                                    FileContent.closeBufferedReader(br);
                                    FileContent.closeInputStreamReader(isr);
                                    FileContent.closeFileInputStream(fis);
                                    return line;
                                } catch (Throwable th2) {
                                    th = th2;
                                    FileContent.closeBufferedReader(br);
                                    FileContent.closeInputStreamReader(isr);
                                    FileContent.closeFileInputStream(fis);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                br = br2;
                                isr = isr2;
                                fis = fis2;
                                FileContent.closeBufferedReader(br);
                                FileContent.closeInputStreamReader(isr);
                                FileContent.closeFileInputStream(fis);
                                throw th;
                            }
                        } while (!line.contains(keyword));
                        FileContent.closeBufferedReader(br2);
                        FileContent.closeInputStreamReader(isr2);
                        FileContent.closeFileInputStream(fis2);
                        fis = fis2;
                    } catch (FileNotFoundException e7) {
                        e = e7;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        return line;
                    } catch (UnsupportedEncodingException e8) {
                        e2 = e8;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        return line;
                    } catch (IOException e9) {
                        e3 = e9;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "IOException " + e3.getMessage());
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        return line;
                    } catch (Throwable th4) {
                        th = th4;
                        isr = isr2;
                        fis = fis2;
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        throw th;
                    }
                } catch (FileNotFoundException e10) {
                    e = e10;
                    fis = fis2;
                    AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return line;
                } catch (UnsupportedEncodingException e11) {
                    e2 = e11;
                    fis = fis2;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return line;
                } catch (IOException e12) {
                    e3 = e12;
                    fis = fis2;
                    AwareLog.e(TAG, "IOException " + e3.getMessage());
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    return line;
                } catch (Throwable th5) {
                    th = th5;
                    fis = fis2;
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    throw th;
                }
            } catch (FileNotFoundException e13) {
                e = e13;
                AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return line;
            } catch (UnsupportedEncodingException e14) {
                e2 = e14;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return line;
            } catch (IOException e15) {
                e3 = e15;
                AwareLog.e(TAG, "IOException " + e3.getMessage());
                FileContent.closeBufferedReader(br);
                FileContent.closeInputStreamReader(isr);
                FileContent.closeFileInputStream(fis);
                return line;
            }
            return line;
        }
        AwareLog.e(TAG, "getContentWithOneLine file not exists or canot read!");
        return null;
    }

    private void sendMessageForLoadDetector() {
        this.mLoadDetectorHandler.removeMessages(1);
        this.mLoadDetectorHandler.sendEmptyMessageDelayed(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
    }
}
