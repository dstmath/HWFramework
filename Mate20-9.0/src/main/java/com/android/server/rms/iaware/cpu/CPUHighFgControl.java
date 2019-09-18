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
    private int mCpuFgLoadThreshold = 80;
    private int mCpuLoadLowThreshold = 60;
    private int mCpuTaLoadThreshold = CPU_TA_LOAD_THRESHOLD_DEFAULT;
    /* access modifiers changed from: private */
    public volatile boolean mIsStart = false;
    private CpuLoadDetectorHandler mLoadDetectorHandler;
    private Thread mLoadDetectorThread;
    /* access modifiers changed from: private */
    public int mLoadStatus = 3;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private int mPollingInterval = 1000;
    private ProcLoadComparator mProcLoadComparator;
    private TALoadDetectorThread mTaLoadDetectorThread;

    private class CpuLoadDetectorHandler extends Handler {
        private CpuLoadDetectorHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what != 1) {
                AwareLog.e(CPUHighFgControl.TAG, "CpuLoadDetectorHandler msg = " + what + " not support!");
                return;
            }
            CPUHighFgControl.this.notifyLoadChange(4);
        }
    }

    private static class ProcLoadComparator implements Comparator<ProcLoadPair>, Serializable {
        private static final long serialVersionUID = 1;

        private ProcLoadComparator() {
        }

        public int compare(ProcLoadPair lhs, ProcLoadPair rhs) {
            return Integer.compare(rhs.load, lhs.load);
        }
    }

    private static class ProcLoadPair {
        public int load;
        public int pid;

        public ProcLoadPair(int pid2, int load2) {
            this.pid = pid2;
            this.load = load2;
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

        private ProcessStatInfo() {
        }

        public long getWallTime() {
            return this.userTime + this.nice + this.system + this.idle + this.iowait + this.irq + this.softIrq;
        }

        public long getRunningTime() {
            return this.userTime + this.nice + this.system;
        }
    }

    private class TALoadDetectorThread implements Runnable {
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
                        int unused = CPUHighFgControl.this.mLoadStatus = 3;
                    } else if (CPUHighFgControl.this.mLoadStatus == 2) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for high load!");
                        CPUHighFgControl.this.setHighLoadPid();
                        int unused2 = CPUHighFgControl.this.mLoadStatus = 3;
                        CPUHighFgControl.this.rotateStatus(2);
                        CPUHighFgControl.this.sendMessageForLoadDetector();
                    } else if (CPUHighFgControl.this.mLoadStatus == 1) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for low load!");
                        CPUHighFgControl.this.setLowLoad();
                        int unused3 = CPUHighFgControl.this.mLoadStatus = 3;
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
        this.mLoadDetectorHandler = new CpuLoadDetectorHandler();
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
            Integer value = cpuCtlFGValueMap.get(ITEM_CPU_HIGHLOAD_POLLING_INTERVAL);
            if (isValidValue(value, 100, CPU_POLLING_INTERVAL_MAX_VALUE)) {
                this.mPollingInterval = value.intValue();
            }
            Integer value2 = cpuCtlFGValueMap.get(ITEM_CPU_TA_LOAD_THRESHOLD);
            if (isValidValue(value2, 20, 100)) {
                this.mCpuTaLoadThreshold = value2.intValue();
            }
            Integer value3 = cpuCtlFGValueMap.get(ITEM_CPU_FG_LOAD_THRESHOLD);
            if (isValidValue(value3, 20, 100)) {
                this.mCpuFgLoadThreshold = value3.intValue();
            }
            Integer value4 = cpuCtlFGValueMap.get(ITEM_CPU_FG_LOAD_LIMIT);
            if (isValidValue(value4, 20, 100)) {
                this.mCpuFgLoadLimit = value4.intValue();
            }
            Integer value5 = cpuCtlFGValueMap.get(ITEM_CPU_LOAD_LOW_THRESHOLD);
            if (isValidValue(value5, 20, 100)) {
                this.mCpuLoadLowThreshold = value5.intValue();
            }
        }
    }

    public void start(CPUFeature feature) {
        this.mCPUFeatureInstance = feature;
        if (this.mTaLoadDetectorThread == null) {
            this.mTaLoadDetectorThread = new TALoadDetectorThread();
        }
        if (this.mLoadDetectorThread == null || !this.mLoadDetectorThread.isAlive()) {
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
        int tempPid = getFileContent(CPUSET_TA_PROC_PATH);
        if (pid != tempPid) {
            return 0;
        }
        long nextProcessLoad = getProcessLoad(pid);
        long nextCpuLoad = getCpuLoad();
        if (nextCpuLoad == -1) {
        } else if (nextProcessLoad == -1) {
            int i = tempPid;
        } else {
            long totalDeltaCpuTime = nextCpuLoad - totalCpuTimeLocal;
            long totalDeltaProcessTime = nextProcessLoad - processCpuTimeLocal;
            if (totalDeltaCpuTime == 0) {
                return 0;
            }
            int i2 = tempPid;
            return (int) ((100 * totalDeltaProcessTime) / totalDeltaCpuTime);
        }
        return 0;
    }

    private int computeFgProcLoad(List<ProcLoadPair> fgProcLoadArray) {
        if (fgProcLoadArray == null) {
            return 0;
        }
        long totalCpuTime = getCpuLoad();
        if (-1 == totalCpuTime) {
            return 0;
        }
        SparseArray sparseArray = new SparseArray();
        getFgCpuloadPidArray(sparseArray);
        try {
            Thread.sleep((long) this.mPollingInterval);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "computeFgProcLoad InterruptedException");
        }
        SparseArray<Long> nextPidLoadList = new SparseArray<>();
        getFgCpuloadPidArray(nextPidLoadList);
        long nextTotalCpuTime = getCpuLoad();
        if (-1 == nextTotalCpuTime) {
            return 0;
        }
        long totalCpuRuntime = nextTotalCpuTime - totalCpuTime;
        if (totalCpuRuntime == 0) {
            return 0;
        }
        return computeProcsLoad(sparseArray, nextPidLoadList, totalCpuRuntime, fgProcLoadArray);
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
                int procLoad = (int) ((100 * (currArray.get(procPid).longValue() - prevArray.get(procPid).longValue())) / totalTime);
                if (procLoad == 0) {
                    AwareLog.d(TAG, "pid " + procPid + " cpuload is 0 ");
                } else {
                    fgProcLoadArray.add(new ProcLoadPair(procPid, procLoad));
                    totalLoad += procLoad;
                }
            }
        }
        if (fgProcLoadArray.isEmpty() == 0) {
            Collections.sort(fgProcLoadArray, this.mProcLoadComparator);
        }
        return totalLoad;
    }

    private void getFgProcsloadPidArray(String filePath, SparseArray<Long> pidList) {
        List<String> procsList = getProcsList(filePath);
        if (procsList != null) {
            int size = procsList.size();
            for (int i = 0; i < size; i++) {
                int pid = parseInt(procsList.get(i));
                if (pid >= 0 && isFgProc(pid) && isLimitProc(pid)) {
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
        if (!file.exists() || !file.canRead()) {
            AwareLog.e(TAG, "getProcsList file not exists or canot read!");
            return null;
        }
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
        List<String> procsList = new ArrayList<>();
        for (String str : procs) {
            String strProc = str.trim();
            if (strProc.length() != 0) {
                procsList.add(strProc);
            }
        }
        return procsList;
    }

    private void getLimitFgProcsLoadArray(String filePath, SparseArray<Long> pidList) {
        List<String> procsList = getProcsList(filePath);
        if (procsList != null) {
            int size = procsList.size();
            for (int i = 0; i < size; i++) {
                int pid = parseInt(procsList.get(i));
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

    /* access modifiers changed from: private */
    public void startLoadDetector() {
        ProcessStatInfo prevCpuStatInfo = getCpuStatInfo();
        if (prevCpuStatInfo == null) {
            AwareLog.e(TAG, "startLoadDetector prevStatInfo is null!");
            return;
        }
        SparseArray<Long> prevLimFgArray = new SparseArray<>();
        getLimitFgCpuloadPidArray(prevLimFgArray);
        try {
            Thread.sleep((long) (3 * this.mPollingInterval));
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "startLoadDetector InterruptedException");
        }
        SparseArray sparseArray = new SparseArray();
        getLimitFgCpuloadPidArray(sparseArray);
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
        loadDetectorPolicy(cpuLoad, deltaTotalCpuRuntime, prevLimFgArray, sparseArray);
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
            int totalLimitFgLoad = computeProcsLoad(prevArray, currArray, cpuTotalTime, new ArrayList<>());
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

    /* access modifiers changed from: private */
    public void rotateStatus(int loadStatus) {
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
                ProcLoadPair pair = procLoadArray.get(i);
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
        if (filePath == null) {
            return -1;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            return -1;
        }
        int pid = 0;
        FileInputStream input = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufReader = null;
        try {
            input = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(input, "UTF-8");
            bufReader = new BufferedReader(inputStreamReader);
            while (true) {
                String readLine = bufReader.readLine();
                String content = readLine;
                if (readLine == null) {
                    break;
                }
                pid = Integer.parseInt(content.trim());
                if (isFgProc(pid)) {
                    break;
                }
                pid = 0;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
        } catch (FileNotFoundException e2) {
            AwareLog.e(TAG, "exception file not found, file path: " + filePath);
        } catch (UnsupportedEncodingException e3) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
        } catch (IOException e4) {
            AwareLog.e(TAG, "IOException");
        } catch (Throwable th) {
            FileContent.closeBufferedReader(null);
            FileContent.closeInputStreamReader(null);
            FileContent.closeFileInputStream(null);
            throw th;
        }
        FileContent.closeBufferedReader(bufReader);
        FileContent.closeInputStreamReader(inputStreamReader);
        FileContent.closeFileInputStream(input);
        return pid;
    }

    /* access modifiers changed from: private */
    public void setHighLoadPid() {
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
        List<ProcLoadPair> procLoadArray = new ArrayList<>();
        int totalProcLoad = computeFgProcLoad(procLoadArray);
        int procLoadArraySize = procLoadArray.size();
        for (int i = 0; i < procLoadArraySize; i++) {
            AwareLog.d(TAG, "fg the i= " + i + " load is " + procLoadArray.get(i).load + " pid is " + procLoadArray.get(i).pid);
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

    /* access modifiers changed from: private */
    public void setLowLoad() {
        File file = new File(CPUCTL_LIMIT_PROC_PATH);
        if (!file.exists() || !file.canRead()) {
            AwareLog.e(TAG, "setLowLoad file not exists or canot read!");
            return;
        }
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
        if (!file.exists() || !file.canRead()) {
            AwareLog.e(TAG, "isLimitProc file not exists or canot read!" + path.toString());
            return false;
        }
        String procname = null;
        try {
            procname = FileUtils.readTextFile(file, 0, null);
        } catch (IOException e) {
            AwareLog.e(TAG, "isLimitProc IOException + " + e.getMessage());
        }
        if (procname == null) {
            return false;
        }
        if (!"com.android.systemui".contains(procname.trim())) {
            return true;
        }
        AwareLog.d(TAG, "procname:" + procname + "is limit");
        return false;
    }

    private boolean isFgProc(int pid) {
        return isFgThirdPartProc(PTAH_PROC_INFO + pid + PTAH_CGROUP);
    }

    private boolean isThirdPartyUid(String line) {
        if (line == null || !line.contains(CGROUP_CPUACCT)) {
            return false;
        }
        boolean flag = false;
        int indexOf = line.indexOf(UID_PTAH);
        int index = indexOf;
        if (indexOf != -1) {
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
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            Object obj = "";
            boolean flag = false;
            while (true) {
                String readLine = br.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                }
                if (line.contains(CGROUP_CPUSET)) {
                    if (!line.contains("background")) {
                        flag = true;
                    } else {
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        return false;
                    }
                }
                if (flag) {
                    if (isThirdPartyUid(line)) {
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        return true;
                    }
                }
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
        } catch (FileNotFoundException e2) {
            AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
        } catch (UnsupportedEncodingException e3) {
            AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
        } catch (IOException e4) {
            AwareLog.e(TAG, "IOException " + e4.getMessage());
        } catch (Throwable th) {
            FileContent.closeBufferedReader(br);
            FileContent.closeInputStreamReader(isr);
            FileContent.closeFileInputStream(fis);
            throw th;
        }
        FileContent.closeBufferedReader(br);
        FileContent.closeInputStreamReader(isr);
        FileContent.closeFileInputStream(fis);
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
        List<String> listTemp = new ArrayList<>();
        for (String str : conts) {
            if (!"".equals(str)) {
                listTemp.add(str);
            }
        }
        return listTemp;
    }

    private ProcessStatInfo getCpuStatInfo(String content) {
        ProcessStatInfo processStatInfo;
        List<String> listTemp = getProcStatInfo(content);
        if (listTemp == null) {
            processStatInfo = null;
        } else if (listTemp.size() < 10) {
            List<String> list = listTemp;
            processStatInfo = null;
        } else {
            String strUserTime = listTemp.get(1);
            String strNice = listTemp.get(2);
            String strSystem = listTemp.get(3);
            String strIdle = listTemp.get(4);
            String strIoWait = listTemp.get(5);
            long userTime = parseLong(strUserTime);
            long nice = parseLong(strNice);
            long system = parseLong(strSystem);
            String str = strUserTime;
            long idle = parseLong(strIdle);
            String str2 = strNice;
            String str3 = strSystem;
            long iowait = parseLong(strIoWait);
            String str4 = strIdle;
            String str5 = strIoWait;
            long irq = parseLong(listTemp.get(6));
            long irq2 = parseLong(listTemp.get(7));
            if (userTime == -1 || nice == -1 || system == -1 || idle == -1 || iowait == -1 || irq == -1) {
                long j = idle;
                long j2 = irq;
            } else if (irq2 == -1) {
                List<String> list2 = listTemp;
                long j3 = idle;
                long j4 = irq;
            } else {
                List<String> list3 = listTemp;
                ProcessStatInfo info = new ProcessStatInfo();
                info.idle = idle;
                info.iowait = iowait;
                long j5 = idle;
                info.irq = irq;
                info.nice = nice;
                info.softIrq = irq2;
                info.system = system;
                info.userTime = userTime;
                return info;
            }
            AwareLog.e(TAG, "getWallTime inalid value!");
            return null;
        }
        return processStatInfo;
    }

    private long parseLong(String str) {
        long value = -1;
        if (str == null || str.length() == 0) {
            return -1;
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
            return -1;
        }
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
        }
        return value;
    }

    private long getProcessLoad(int pid) {
        String content = getContentWithOneLine(PTAH_PROC_INFO + pid + PATH_STAT, String.valueOf(pid));
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
        String line = null;
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            AwareLog.e(TAG, "getContentWithOneLine file not exists or canot read!");
            return null;
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(filePath);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            do {
                String readLine = br.readLine();
                line = readLine;
                if (readLine == null || keyword == null) {
                    break;
                }
            } while (!line.contains(keyword));
        } catch (FileNotFoundException e) {
            AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
        } catch (UnsupportedEncodingException e2) {
            AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
        } catch (IOException e3) {
            AwareLog.e(TAG, "IOException " + e3.getMessage());
        } catch (Throwable th) {
            FileContent.closeBufferedReader(null);
            FileContent.closeInputStreamReader(null);
            FileContent.closeFileInputStream(null);
            throw th;
        }
        FileContent.closeBufferedReader(br);
        FileContent.closeInputStreamReader(isr);
        FileContent.closeFileInputStream(fis);
        return line;
    }

    /* access modifiers changed from: private */
    public void sendMessageForLoadDetector() {
        this.mLoadDetectorHandler.removeMessages(1);
        this.mLoadDetectorHandler.sendEmptyMessageDelayed(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
    }
}
