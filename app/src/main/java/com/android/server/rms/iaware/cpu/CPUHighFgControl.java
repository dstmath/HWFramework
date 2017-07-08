package com.android.server.rms.iaware.cpu;

import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.SparseArray;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
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
    private static final String CGROUP_CPU = ":cpu:";
    private static final String CGROUP_CPUACCT = ":cpuacct";
    private static final String CPUCTL_LIMIT_BG_PROC_PATH = "/dev/cpuctl/bg_non_interactive/limit_bg/cgroup.procs";
    private static final String CPUCTL_LIMIT_FG_PROC_PATH = "/dev/cpuctl/limit_fg/cgroup.procs";
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
    private static final int CPU_TA_LOAD_THRESHOLD_DEFAULT = 90;
    private static final long INVALID_VALUE = -1;
    private static final String ITEM_CPU_FG_LOAD_LIMIT = "cpu_fg_load_limit";
    private static final String ITEM_CPU_FG_LOAD_THRESHOLD = "cpu_fg_load_threshold";
    private static final String ITEM_CPU_HIGHLOAD_POLLING_INTERVAL = "cpu_highload_polling_interval";
    private static final String ITEM_CPU_LOAD_LOW_THRESHOLD = "cpu_load_low_threshold";
    private static final String ITEM_CPU_TA_LOAD_THRESHOLD = "cpu_ta_load_threshold";
    private static final int LOAD_DETCTOR_DELAY = 30000;
    private static final int LOAD_STATUS_DETECTOR = 4;
    private static final int LOAD_STATUS_HIGH = 2;
    private static final int LOAD_STATUS_IDLE = 3;
    private static final int LOAD_STATUS_LOW = 1;
    private static final int MSG_LOAD_DETCTOR = 1;
    private static final String PATH_STAT = "/stat";
    private static final String PTAH_CGROUP = "/cgroup";
    private static final String PTAH_PROC_INFO = "/proc/";
    private static final String TAG = "CPUHighFgControl";
    private static final String UID_PTAH = "uid_";
    private CPUFeature mCPUFeatureInstance;
    private CPUNetLink mCPUNetLink;
    private int mCpuFgLoadLimit;
    private int mCpuFgLoadThreshold;
    private int mCpuLoadLowThreshold;
    private int mCpuTaLoadThreshold;
    private volatile boolean mIsStart;
    private CpuLoadDetectorHandler mLoadDetectorHandler;
    private Thread mLoadDetectorThread;
    private int mLoadStatus;
    private Object mLock;
    private int mPollingInterval;
    private ProcLoadComparator mProcLoadComparator;
    private TALoadDetectorThread mTaLoadDetectorThread;

    private class CpuLoadDetectorHandler extends Handler {
        private CpuLoadDetectorHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case CPUHighFgControl.MSG_LOAD_DETCTOR /*1*/:
                    CPUHighFgControl.this.notifyLoadChange(CPUHighFgControl.LOAD_STATUS_DETECTOR);
                default:
                    AwareLog.e(CPUHighFgControl.TAG, "CpuLoadDetectorHandler msg = " + what + " not support!");
            }
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

        private ProcessStatInfo() {
        }

        public long getWallTime() {
            return (((((this.userTime + this.nice) + this.system) + this.idle) + this.iowait) + this.irq) + this.softIrq;
        }

        public long getRunningTime() {
            return this.userTime + this.nice;
        }
    }

    private class TALoadDetectorThread implements Runnable {
        private TALoadDetectorThread() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Thread.currentThread().setPriority(10);
            loop0:
            while (CPUHighFgControl.this.mIsStart) {
                synchronized (CPUHighFgControl.this.mLock) {
                    while (true) {
                        if (CPUHighFgControl.this.mLoadStatus != CPUHighFgControl.LOAD_STATUS_IDLE) {
                            break;
                        }
                        try {
                            CPUHighFgControl.this.mLock.wait();
                        } catch (InterruptedException e) {
                            AwareLog.e(CPUHighFgControl.TAG, "TALoadDetectorThread InterruptedException return");
                        }
                    }
                    if (CPUHighFgControl.this.mLoadStatus == CPUHighFgControl.LOAD_STATUS_DETECTOR) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for load detector!");
                        CPUHighFgControl.this.startLoadDetector();
                        CPUHighFgControl.this.mLoadStatus = CPUHighFgControl.LOAD_STATUS_IDLE;
                    } else if (CPUHighFgControl.this.mLoadStatus == CPUHighFgControl.LOAD_STATUS_HIGH) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for high load!");
                        CPUHighFgControl.this.setHighLoadPid();
                        CPUHighFgControl.this.mLoadStatus = CPUHighFgControl.LOAD_STATUS_IDLE;
                        CPUHighFgControl.this.sendMessageForLoadDetector();
                    } else if (CPUHighFgControl.this.mLoadStatus == CPUHighFgControl.MSG_LOAD_DETCTOR) {
                        AwareLog.d(CPUHighFgControl.TAG, "DetectorThread run for low load!");
                        CPUHighFgControl.this.setLowLoad();
                        CPUHighFgControl.this.mLoadStatus = CPUHighFgControl.LOAD_STATUS_IDLE;
                        CPUHighFgControl.this.sendMessageForLoadDetector();
                    }
                }
            }
        }
    }

    public CPUHighFgControl(CPUFeature feature) {
        this.mPollingInterval = CPU_HIGHLOAD_POLLING_INTERVAL_DEFAULT;
        this.mCpuTaLoadThreshold = CPU_TA_LOAD_THRESHOLD_DEFAULT;
        this.mCpuFgLoadThreshold = CPU_FG_LOAD_THRESHOLD_DEFAULT;
        this.mCpuFgLoadLimit = CPU_LOAD_LOW_THRESHOLD_DEFAULT;
        this.mCpuLoadLowThreshold = CPU_LOAD_LOW_THRESHOLD_DEFAULT;
        this.mLoadStatus = LOAD_STATUS_IDLE;
        this.mLock = new Object();
        this.mIsStart = false;
        initXml();
        this.mCPUFeatureInstance = feature;
        this.mCPUNetLink = new CPUNetLink(this);
        this.mProcLoadComparator = new ProcLoadComparator();
        this.mLoadDetectorHandler = new CpuLoadDetectorHandler();
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
            if (isValidValue(value, CPU_POLLING_INTERVAL_MIN_VALUE, CPU_POLLING_INTERVAL_MAX_VALUE)) {
                this.mPollingInterval = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_TA_LOAD_THRESHOLD);
            if (isValidValue(value, CPU_LOAD_THRESHOLD_MIN, CPU_POLLING_INTERVAL_MIN_VALUE)) {
                this.mCpuTaLoadThreshold = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_FG_LOAD_THRESHOLD);
            if (isValidValue(value, CPU_LOAD_THRESHOLD_MIN, CPU_POLLING_INTERVAL_MIN_VALUE)) {
                this.mCpuFgLoadThreshold = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_FG_LOAD_LIMIT);
            if (isValidValue(value, CPU_LOAD_THRESHOLD_MIN, CPU_POLLING_INTERVAL_MIN_VALUE)) {
                this.mCpuFgLoadLimit = value.intValue();
            }
            value = (Integer) cpuCtlFGValueMap.get(ITEM_CPU_LOAD_LOW_THRESHOLD);
            if (isValidValue(value, CPU_LOAD_THRESHOLD_MIN, CPU_POLLING_INTERVAL_MIN_VALUE)) {
                this.mCpuLoadLowThreshold = value.intValue();
            }
        }
    }

    public void start() {
        this.mCPUNetLink.start();
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
        this.mCPUNetLink.stop();
        this.mLoadDetectorHandler.removeMessages(MSG_LOAD_DETCTOR);
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
        int pid = FileContent.getFileContent(CPUSET_TA_PROC_PATH);
        if (pid == -1) {
            return 0;
        }
        long processCpuTimeLocal = getProcessLoad(pid);
        if (totalCpuTimeLocal == INVALID_VALUE || processCpuTimeLocal == INVALID_VALUE) {
            return 0;
        }
        try {
            Thread.sleep((long) this.mPollingInterval);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "taLoadCompute InterruptedException");
        }
        if (pid != FileContent.getFileContent(CPUSET_TA_PROC_PATH)) {
            return 0;
        }
        long nextProcessLoad = getProcessLoad(pid);
        long nextCpuLoad = getCpuLoad();
        if (nextCpuLoad == INVALID_VALUE || nextProcessLoad == INVALID_VALUE) {
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
        SparseArray<Long> pidLoadList = new SparseArray();
        getFgCpuloadPidArray(pidLoadList);
        try {
            Thread.sleep((long) this.mPollingInterval);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "computeFgProcLoad InterruptedException");
        }
        SparseArray<Long> nextPidLoadList = new SparseArray();
        getFgCpuloadPidArray(nextPidLoadList);
        long totalCpuRuntime = getCpuLoad() - totalCpuTime;
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
        for (int i = 0; i < currArray.size(); i += MSG_LOAD_DETCTOR) {
            int procPid = currArray.keyAt(i);
            if (prevArray.indexOfKey(procPid) >= 0) {
                int procLoad = (int) ((100 * (((Long) currArray.get(procPid)).longValue() - ((Long) prevArray.get(procPid)).longValue())) / totalTime);
                fgProcLoadArray.add(new ProcLoadPair(procPid, procLoad));
                totalLoad += procLoad;
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
            for (int i = 0; i < size; i += MSG_LOAD_DETCTOR) {
                int pid = parseInt((String) procsList.get(i));
                if (pid >= 0 && isFgProc(pid)) {
                    long tempLoad = getProcessLoad(pid);
                    if (INVALID_VALUE != tempLoad) {
                        pidList.put(pid, Long.valueOf(tempLoad));
                    }
                }
            }
        }
    }

    private List<String> getProcsList(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.canRead()) {
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
            int length = procs.length;
            for (int i = 0; i < length; i += MSG_LOAD_DETCTOR) {
                String strProc = procs[i].trim();
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
            for (int i = 0; i < size; i += MSG_LOAD_DETCTOR) {
                int pid = parseInt((String) procsList.get(i));
                if (pid >= 0) {
                    long tempLoad = getProcessLoad(pid);
                    if (INVALID_VALUE != tempLoad) {
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
        getLimitFgProcsLoadArray(CPUCTL_LIMIT_FG_PROC_PATH, pidList);
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
            Thread.sleep((long) (this.mPollingInterval * LOAD_STATUS_IDLE));
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
        } else {
            loadDetectorPolicy(cpuLoad, deltaTotalCpuRuntime, prevLimFgArray, currLimFgArray);
        }
    }

    private boolean isLimitGroupHasPid() {
        List<String> limitBgPidList = getProcsList(CPUCTL_LIMIT_BG_PROC_PATH);
        List<String> limitFgpidList = getProcsList(CPUCTL_LIMIT_FG_PROC_PATH);
        if ((limitBgPidList == null || limitBgPidList.size() <= 0) && (limitFgpidList == null || limitFgpidList.size() <= 0)) {
            return false;
        }
        return true;
    }

    private void loadDetectorPolicy(int cpuLoad, long cpuTotalTime, SparseArray<Long> prevArray, SparseArray<Long> currArray) {
        AwareLog.d(TAG, "loadDetectorPolicy cpu_total_Load = " + cpuLoad);
        if (cpuLoad >= this.mCpuTaLoadThreshold) {
            int totalLimitFgLoad = computeProcsLoad(prevArray, currArray, cpuTotalTime, new ArrayList());
            AwareLog.d(TAG, "loadDetectorPolicy limit_fg totalLoad = " + totalLimitFgLoad);
            if (totalLimitFgLoad >= this.mCpuTaLoadThreshold) {
                AwareLog.d(TAG, "loadDetectorPolicy limit_fg + ta load high, and a new high load process may be add in the system, so continue detector!");
            } else {
                AwareLog.d(TAG, "limit fg low, need to find another high load pid to control");
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

    private void chgProcessGroup(int pid, String groupPath) {
        if (pid > 0) {
            setGroup(pid, groupPath);
        }
    }

    private void chgProcessGroup(List<ProcLoadPair> procLoadArray, String groupPath) {
        if (procLoadArray != null) {
            int currLoad = 0;
            int size = procLoadArray.size();
            for (int i = 0; i < size; i += MSG_LOAD_DETCTOR) {
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

    private void setHighLoadPid() {
        int taLoad = taLoadCompute();
        AwareLog.d(TAG, "setHighLoadPid taLoadCompute = " + taLoad);
        setHightLoad(taLoad);
    }

    private void setHightLoad(int taLoad) {
        AwareLog.d(TAG, "setHighLoad taLoad is " + taLoad);
        if (taLoad >= this.mCpuTaLoadThreshold) {
            chgProcessGroup(FileContent.getFileContent(CPUSET_TA_PROC_PATH), CPUCTL_LIMIT_FG_PROC_PATH);
            return;
        }
        List procLoadArray = new ArrayList();
        int totalProcLoad = computeFgProcLoad(procLoadArray);
        for (int i = 0; i < procLoadArray.size(); i += MSG_LOAD_DETCTOR) {
            AwareLog.d(TAG, "fg the i= " + i + " load is " + ((ProcLoadPair) procLoadArray.get(i)).load + " pid is " + ((ProcLoadPair) procLoadArray.get(i)).pid);
        }
        AwareLog.d(TAG, "setHighLoadPid computeFgProcLoad = " + totalProcLoad);
        if (totalProcLoad >= this.mCpuFgLoadThreshold || taLoad + totalProcLoad >= this.mCpuTaLoadThreshold) {
            chgProcessGroup(procLoadArray, CPUCTL_LIMIT_FG_PROC_PATH);
        }
    }

    private void setLowLoad() {
        File file = new File(CPUCTL_LIMIT_FG_PROC_PATH);
        if (file.exists() && file.canRead()) {
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
            String[] procs = groupProcs.split("\n");
            int length = procs.length;
            for (int i = 0; i < length; i += MSG_LOAD_DETCTOR) {
                String strProc = procs[i].trim();
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
        if (CPUCTL_LIMIT_FG_PROC_PATH.equals(file)) {
            buffer.putInt(CPUFeature.MSG_SET_LIMIT_FG_CGROUP);
        } else if (CPUCTL_LIMIT_BG_PROC_PATH.equals(file)) {
            buffer.putInt(CPUFeature.MSG_SET_LIMIT_BG_CGROUP);
        } else if (CPUCTL_PROC_PATH.equals(file)) {
            buffer.putInt(CPUFeature.MSG_SET_FG_CGROUP);
        } else {
            return;
        }
        buffer.putInt(pid);
        AwareLog.d(TAG, "setGroup pid = " + pid + " processgroup = " + file);
        if (this.mCPUFeatureInstance != null) {
            this.mCPUFeatureInstance.sendPacket(buffer);
        }
    }

    private boolean isFgProc(int pid) {
        StringBuilder targetPath = new StringBuilder();
        targetPath.append(PTAH_PROC_INFO);
        targetPath.append(pid);
        targetPath.append(PTAH_CGROUP);
        return isFgThirdPartProc(targetPath.toString());
    }

    private boolean isThirdPartyUid(String line) {
        if (line == null || !line.contains(CGROUP_CPUACCT)) {
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
            if (parseInt(strUid) > LifeCycleStateMachine.TIME_OUT_TIME) {
                flag = true;
            }
        }
        return flag;
    }

    private boolean isFgThirdPartProc(String path) {
        InputStreamReader isr;
        NumberFormatException e;
        FileNotFoundException e2;
        UnsupportedEncodingException e3;
        IOException e4;
        Throwable th;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            try {
                isr = new InputStreamReader(fis, "UTF-8");
            } catch (NumberFormatException e5) {
                e = e5;
                fileInputStream = fis;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (FileNotFoundException e6) {
                e2 = e6;
                fileInputStream = fis;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (UnsupportedEncodingException e7) {
                e3 = e7;
                fileInputStream = fis;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (IOException e8) {
                e4 = e8;
                fileInputStream = fis;
                try {
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                throw th;
            }
            try {
                BufferedReader br = new BufferedReader(isr);
                try {
                    String line = AppHibernateCst.INVALID_PKG;
                    boolean flag = false;
                    while (true) {
                        line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.contains(CGROUP_CPU)) {
                            if (line.charAt(line.length() - 1) == '/') {
                                flag = true;
                            } else {
                                FileContent.closeBufferedReader(br);
                                FileContent.closeInputStreamReader(isr);
                                FileContent.closeFileInputStream(fis);
                                return false;
                            }
                        }
                        if (flag && isThirdPartyUid(line)) {
                            FileContent.closeBufferedReader(br);
                            FileContent.closeInputStreamReader(isr);
                            FileContent.closeFileInputStream(fis);
                            return true;
                        }
                    }
                    FileContent.closeBufferedReader(br);
                    FileContent.closeInputStreamReader(isr);
                    FileContent.closeFileInputStream(fis);
                    fileInputStream = fis;
                } catch (NumberFormatException e9) {
                    e = e9;
                    bufferedReader = br;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return false;
                } catch (FileNotFoundException e10) {
                    e2 = e10;
                    bufferedReader = br;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return false;
                } catch (UnsupportedEncodingException e11) {
                    e3 = e11;
                    bufferedReader = br;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return false;
                } catch (IOException e12) {
                    e4 = e12;
                    bufferedReader = br;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    bufferedReader = br;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (NumberFormatException e13) {
                e = e13;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (FileNotFoundException e14) {
                e2 = e14;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (UnsupportedEncodingException e15) {
                e3 = e15;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (IOException e16) {
                e4 = e16;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "IOException " + e4.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return false;
            } catch (Throwable th5) {
                th = th5;
                inputStreamReader = isr;
                fileInputStream = fis;
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                throw th;
            }
        } catch (NumberFormatException e17) {
            e = e17;
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
            FileContent.closeBufferedReader(bufferedReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(fileInputStream);
            return false;
        } catch (FileNotFoundException e18) {
            e2 = e18;
            AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
            FileContent.closeBufferedReader(bufferedReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(fileInputStream);
            return false;
        } catch (UnsupportedEncodingException e19) {
            e3 = e19;
            AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
            FileContent.closeBufferedReader(bufferedReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(fileInputStream);
            return false;
        } catch (IOException e20) {
            e4 = e20;
            AwareLog.e(TAG, "IOException " + e4.getMessage());
            FileContent.closeBufferedReader(bufferedReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(fileInputStream);
            return false;
        }
        return false;
    }

    private long getCpuLoad() {
        String strContent = getContentWithOneLine(CPU_STAT_PATH, "cpu ");
        if (strContent == null) {
            AwareLog.e(TAG, "getCpuLoad null content!");
            return INVALID_VALUE;
        }
        ProcessStatInfo info = getCpuStatInfo(strContent);
        if (info == null) {
            return INVALID_VALUE;
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
        int length = conts.length;
        for (int i = 0; i < length; i += MSG_LOAD_DETCTOR) {
            String str = conts[i];
            if (!AppHibernateCst.INVALID_PKG.equals(str)) {
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
        String strNice = (String) listTemp.get(LOAD_STATUS_HIGH);
        String strSystem = (String) listTemp.get(LOAD_STATUS_IDLE);
        String strIdle = (String) listTemp.get(LOAD_STATUS_DETECTOR);
        String strIoWait = (String) listTemp.get(5);
        String strIrq = (String) listTemp.get(6);
        String strSoftIrq = (String) listTemp.get(7);
        long userTime = parseLong((String) listTemp.get(MSG_LOAD_DETCTOR));
        long nice = parseLong(strNice);
        long system = parseLong(strSystem);
        long idle = parseLong(strIdle);
        long iowait = parseLong(strIoWait);
        long irq = parseLong(strIrq);
        long softIrq = parseLong(strSoftIrq);
        if (userTime == INVALID_VALUE || nice == INVALID_VALUE || system == INVALID_VALUE || idle == INVALID_VALUE || iowait == INVALID_VALUE || irq == INVALID_VALUE || softIrq == INVALID_VALUE) {
            AwareLog.e(TAG, "getWallTime inalid value!");
            return null;
        }
        ProcessStatInfo info = new ProcessStatInfo();
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
        long value = INVALID_VALUE;
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
        return INVALID_VALUE;
    }

    private long getPorcseeTime(String content) {
        String[] conts = content.split(" ");
        if (conts.length < 15) {
            AwareLog.e(TAG, "getIdleTime content inalid = " + content);
            return INVALID_VALUE;
        }
        String strUTime = conts[13];
        String strSTime = conts[14];
        long utime = parseLong(strUTime);
        long stime = parseLong(strSTime);
        if (utime != INVALID_VALUE && stime != INVALID_VALUE) {
            return utime + stime;
        }
        AwareLog.e(TAG, "getPorcseeTime inalid value!");
        return INVALID_VALUE;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getContentWithOneLine(String filePath, String keyword) {
        FileNotFoundException e;
        UnsupportedEncodingException e2;
        IOException e3;
        Throwable th;
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            String str = null;
            try {
                FileInputStream fis = new FileInputStream(filePath);
                try {
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    try {
                        BufferedReader br = new BufferedReader(isr);
                        do {
                            try {
                                str = br.readLine();
                                if (str == null || keyword == null) {
                                    FileContent.closeBufferedReader(br);
                                    FileContent.closeInputStreamReader(isr);
                                    FileContent.closeFileInputStream(fis);
                                    fileInputStream = fis;
                                }
                            } catch (FileNotFoundException e4) {
                                e = e4;
                                bufferedReader = br;
                                inputStreamReader = isr;
                                fileInputStream = fis;
                            } catch (UnsupportedEncodingException e5) {
                                e2 = e5;
                                bufferedReader = br;
                                inputStreamReader = isr;
                                fileInputStream = fis;
                            } catch (IOException e6) {
                                e3 = e6;
                                bufferedReader = br;
                                inputStreamReader = isr;
                                fileInputStream = fis;
                            } catch (Throwable th2) {
                                th = th2;
                                bufferedReader = br;
                                inputStreamReader = isr;
                                fileInputStream = fis;
                            }
                        } while (!str.contains(keyword));
                        FileContent.closeBufferedReader(br);
                        FileContent.closeInputStreamReader(isr);
                        FileContent.closeFileInputStream(fis);
                        fileInputStream = fis;
                    } catch (FileNotFoundException e7) {
                        e = e7;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                        FileContent.closeBufferedReader(bufferedReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(fileInputStream);
                        return str;
                    } catch (UnsupportedEncodingException e8) {
                        e2 = e8;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                        FileContent.closeBufferedReader(bufferedReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(fileInputStream);
                        return str;
                    } catch (IOException e9) {
                        e3 = e9;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        try {
                            AwareLog.e(TAG, "IOException " + e3.getMessage());
                            FileContent.closeBufferedReader(bufferedReader);
                            FileContent.closeInputStreamReader(inputStreamReader);
                            FileContent.closeFileInputStream(fileInputStream);
                            return str;
                        } catch (Throwable th3) {
                            th = th3;
                            FileContent.closeBufferedReader(bufferedReader);
                            FileContent.closeInputStreamReader(inputStreamReader);
                            FileContent.closeFileInputStream(fileInputStream);
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        FileContent.closeBufferedReader(bufferedReader);
                        FileContent.closeInputStreamReader(inputStreamReader);
                        FileContent.closeFileInputStream(fileInputStream);
                        throw th;
                    }
                } catch (FileNotFoundException e10) {
                    e = e10;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return str;
                } catch (UnsupportedEncodingException e11) {
                    e2 = e11;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return str;
                } catch (IOException e12) {
                    e3 = e12;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "IOException " + e3.getMessage());
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    return str;
                } catch (Throwable th5) {
                    th = th5;
                    fileInputStream = fis;
                    FileContent.closeBufferedReader(bufferedReader);
                    FileContent.closeInputStreamReader(inputStreamReader);
                    FileContent.closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (FileNotFoundException e13) {
                e = e13;
                AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return str;
            } catch (UnsupportedEncodingException e14) {
                e2 = e14;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return str;
            } catch (IOException e15) {
                e3 = e15;
                AwareLog.e(TAG, "IOException " + e3.getMessage());
                FileContent.closeBufferedReader(bufferedReader);
                FileContent.closeInputStreamReader(inputStreamReader);
                FileContent.closeFileInputStream(fileInputStream);
                return str;
            }
            return str;
        }
        AwareLog.e(TAG, "getContentWithOneLine file not exists or canot read!");
        return null;
    }

    private void sendMessageForLoadDetector() {
        this.mLoadDetectorHandler.removeMessages(MSG_LOAD_DETCTOR);
        this.mLoadDetectorHandler.sendEmptyMessageDelayed(MSG_LOAD_DETCTOR, 30000);
    }
}
