package com.android.server.rms.iaware.cpu;

import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.SparseArray;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.UserHandleEx;
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

public class CpuHighFgControl {
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
    private static final Object SLOCK = new Object();
    private static final String SYSTEMUI_NAME = "com.android.systemui";
    private static final String TAG = "CpuHighFgControl";
    private static final String UID_PTAH = "uid_";
    private static CpuHighFgControl sInstance;
    private CpuFeature mCpuFeatureInstance;
    private int mCpuFgLoadLimit = 60;
    private int mCpuFgLoadThreshold = CPU_FG_LOAD_THRESHOLD_DEFAULT;
    private int mCpuLoadLowThreshold = 60;
    private int mCpuTaLoadThreshold = CPU_TA_LOAD_THRESHOLD_DEFAULT;
    private volatile boolean mIsStart = false;
    private CpuLoadDetectorHandler mLoadDetectorHandler;
    private Thread mLoadDetectorThread;
    private int mLoadStatus = 3;
    private int mPollingInterval = 1000;
    private ProcLoadComparator mProcLoadComparator;
    private TaLoadDetectorThread mTaLoadDetectorThread;

    private CpuHighFgControl() {
        initXml();
        this.mProcLoadComparator = new ProcLoadComparator();
        this.mLoadDetectorHandler = new CpuLoadDetectorHandler();
    }

    public static CpuHighFgControl getInstance() {
        CpuHighFgControl cpuHighFgControl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuHighFgControl();
            }
            cpuHighFgControl = sInstance;
        }
        return cpuHighFgControl;
    }

    private boolean isValidValue(Integer value, int min, int max) {
        if (value == null || value.intValue() <= min || value.intValue() >= max) {
            return false;
        }
        return true;
    }

    private void initXml() {
        Map<String, Integer> cpuCtlFgValueMap = new CpuCtlLimitConfig().getCpuCtlFgValueMap();
        if (!cpuCtlFgValueMap.isEmpty()) {
            Integer value = cpuCtlFgValueMap.get(ITEM_CPU_HIGHLOAD_POLLING_INTERVAL);
            if (isValidValue(value, 100, CPU_POLLING_INTERVAL_MAX_VALUE)) {
                this.mPollingInterval = value.intValue();
            }
            Integer value2 = cpuCtlFgValueMap.get(ITEM_CPU_TA_LOAD_THRESHOLD);
            if (isValidValue(value2, 20, 100)) {
                this.mCpuTaLoadThreshold = value2.intValue();
            }
            Integer value3 = cpuCtlFgValueMap.get(ITEM_CPU_FG_LOAD_THRESHOLD);
            if (isValidValue(value3, 20, 100)) {
                this.mCpuFgLoadThreshold = value3.intValue();
            }
            Integer value4 = cpuCtlFgValueMap.get(ITEM_CPU_FG_LOAD_LIMIT);
            if (isValidValue(value4, 20, 100)) {
                this.mCpuFgLoadLimit = value4.intValue();
            }
            Integer value5 = cpuCtlFgValueMap.get(ITEM_CPU_LOAD_LOW_THRESHOLD);
            if (isValidValue(value5, 20, 100)) {
                this.mCpuLoadLowThreshold = value5.intValue();
            }
        }
    }

    public void start(CpuFeature feature) {
        this.mCpuFeatureInstance = feature;
        if (this.mTaLoadDetectorThread == null) {
            this.mTaLoadDetectorThread = new TaLoadDetectorThread();
        }
        Thread thread = this.mLoadDetectorThread;
        if (thread == null || !thread.isAlive()) {
            this.mLoadDetectorThread = new Thread(this.mTaLoadDetectorThread, "taLoadDetectorThread");
        }
        this.mIsStart = true;
        this.mLoadDetectorThread.start();
    }

    public void stop() {
        this.mLoadDetectorHandler.removeMessages(1);
        Thread thread = this.mLoadDetectorThread;
        if (thread != null && thread.isAlive()) {
            this.mIsStart = false;
            this.mLoadDetectorThread.interrupt();
        }
        this.mLoadDetectorThread = null;
    }

    public void notifyLoadChange(int cpuLoadStatus) {
        AwareLog.d(TAG, "notifyLoadChange cpuLoadStatus " + cpuLoadStatus);
        synchronized (SLOCK) {
            this.mLoadStatus = cpuLoadStatus;
            SLOCK.notifyAll();
        }
    }

    private int computeProcLoad(String filePath, List<ProcLoadPair> procLoadArray) {
        if (procLoadArray == null) {
            return 0;
        }
        long totalCpuTime = getCpuLoad();
        if (totalCpuTime == -1) {
            return 0;
        }
        SparseArray<Long> pidLoadList = new SparseArray<>();
        getProcsloadPidArray(filePath, pidLoadList);
        try {
            Thread.sleep((long) this.mPollingInterval);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "computeProcLoad InterruptedException");
        }
        SparseArray<Long> nextPidLoadList = new SparseArray<>();
        getProcsloadPidArray(filePath, nextPidLoadList);
        long nextTotalCpuTime = getCpuLoad();
        if (nextTotalCpuTime == -1) {
            return 0;
        }
        long totalCpuRuntime = nextTotalCpuTime - totalCpuTime;
        if (totalCpuRuntime == 0) {
            return 0;
        }
        return computeProcsLoad(pidLoadList, nextPidLoadList, totalCpuRuntime, procLoadArray);
    }

    private int computeProcsLoad(SparseArray<Long> prevArray, SparseArray<Long> currArray, long totalTime, List<ProcLoadPair> procLoadArray) {
        if (prevArray == null || currArray == null || totalTime == 0 || procLoadArray == null) {
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
                    procLoadArray.add(new ProcLoadPair(procPid, procLoad));
                    totalLoad += procLoad;
                }
            }
        }
        if (!procLoadArray.isEmpty()) {
            Collections.sort(procLoadArray, this.mProcLoadComparator);
        }
        return totalLoad;
    }

    private void getProcsloadPidArray(String filePath, SparseArray<Long> pidList) {
        List<String> procsList = CpuCommonUtil.getProcsList(filePath);
        if (!(procsList == null || procsList.isEmpty())) {
            int size = procsList.size();
            for (int i = 0; i < size; i++) {
                int pid = parseInt(procsList.get(i));
                if (pid >= 0 && isFgProc(pid) && isLimitProc(pid)) {
                    long tempLoad = getProcessLoad(pid);
                    if (tempLoad != -1) {
                        pidList.put(pid, Long.valueOf(tempLoad));
                    }
                }
            }
        }
    }

    private void getLimitFgProcsLoadArray(String filePath, SparseArray<Long> pidList) {
        List<String> procsList = CpuCommonUtil.getProcsList(filePath);
        if (!(procsList == null || procsList.isEmpty())) {
            int size = procsList.size();
            for (int i = 0; i < size; i++) {
                int pid = parseInt(procsList.get(i));
                if (pid >= 0) {
                    long tempLoad = getProcessLoad(pid);
                    if (tempLoad != -1) {
                        pidList.put(pid, Long.valueOf(tempLoad));
                    }
                }
            }
        }
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
    /* access modifiers changed from: public */
    private void startLoadDetector() {
        ProcessStatInfo prevCpuStatInfo = getCpuStatInfo();
        if (prevCpuStatInfo == null) {
            AwareLog.e(TAG, "startLoadDetector prevStatInfo is null!");
            return;
        }
        SparseArray<Long> prevLimFgArray = new SparseArray<>();
        getLimitFgCpuloadPidArray(prevLimFgArray);
        try {
            Thread.sleep((long) (this.mPollingInterval * 3));
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "startLoadDetector InterruptedException");
        }
        SparseArray<Long> currLimFgArray = new SparseArray<>();
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
        List<String> limitPidList = CpuCommonUtil.getProcsList(CPUCTL_LIMIT_PROC_PATH);
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
                AwareLog.d(TAG, "loadDetectorPolicy limit + ta load high, continue detector");
            } else {
                AwareLog.d(TAG, "loadDetectorPolicy limit low");
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

    /* access modifiers changed from: private */
    public class TaLoadDetectorThread implements Runnable {
        private TaLoadDetectorThread() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Thread.currentThread().setPriority(10);
            while (CpuHighFgControl.this.mIsStart) {
                synchronized (CpuHighFgControl.SLOCK) {
                    while (CpuHighFgControl.this.mLoadStatus == 3) {
                        try {
                            CpuHighFgControl.SLOCK.wait();
                        } catch (InterruptedException e) {
                            AwareLog.e(CpuHighFgControl.TAG, "TaLoadDetectorThread InterruptedException return");
                        }
                    }
                    if (CpuHighFgControl.this.mLoadStatus == 4) {
                        AwareLog.d(CpuHighFgControl.TAG, "DetectorThread run for load detector!");
                        CpuHighFgControl.this.startLoadDetector();
                        CpuHighFgControl.this.mLoadStatus = 3;
                    } else if (CpuHighFgControl.this.mLoadStatus == 2) {
                        AwareLog.d(CpuHighFgControl.TAG, "DetectorThread run for high load!");
                        CpuHighFgControl.this.setHighLoadPid();
                        CpuHighFgControl.this.mLoadStatus = 3;
                        CpuHighFgControl.this.sendMessageForLoadDetector();
                    } else if (CpuHighFgControl.this.mLoadStatus == 1) {
                        AwareLog.d(CpuHighFgControl.TAG, "DetectorThread run for low load!");
                        CpuHighFgControl.this.setLowLoad();
                        CpuHighFgControl.this.mLoadStatus = 3;
                        CpuHighFgControl.this.sendMessageForLoadDetector();
                    } else {
                        AwareLog.d(CpuHighFgControl.TAG, "TaLoadDetectorThread invalid mLoadStatus");
                    }
                }
            }
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
            for (int i = 0; i < size; i++) {
                ProcLoadPair pair = procLoadArray.get(i);
                if (pair != null) {
                    if (currLoad < this.mCpuFgLoadLimit) {
                        AwareLog.d(TAG, "chgProcessGroup list pid = " + pair.pid + " load = " + pair.load);
                        currLoad += pair.load;
                        chgProcessGroup(pair.pid, groupPath);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setHighLoadPid() {
        List<ProcLoadPair> taLoadArray = new ArrayList<>();
        int taLoad = computeProcLoad(CPUSET_TA_PROC_PATH, taLoadArray);
        AwareLog.d(TAG, "taLoad is " + taLoad);
        int taLoadArraySize = taLoadArray.size();
        for (int i = 0; i < taLoadArraySize; i++) {
            ProcLoadPair pair = taLoadArray.get(i);
            AwareLog.d(TAG, "ta the i= " + i + " load is " + pair.load + " pid is " + pair.pid);
            if (pair.load >= this.mCpuFgLoadThreshold) {
                chgProcessGroup(pair.pid, CPUCTL_LIMIT_PROC_PATH);
                return;
            }
        }
        List<ProcLoadPair> fgLoadArray = new ArrayList<>();
        int totalProcLoad = computeProcLoad(CPUSET_FG_PROC_PATH, fgLoadArray);
        AwareLog.d(TAG, "fgLoad is " + totalProcLoad);
        int fgLoadArraySize = fgLoadArray.size();
        for (int i2 = 0; i2 < fgLoadArraySize; i2++) {
            AwareLog.d(TAG, "fg the i= " + i2 + " load is " + fgLoadArray.get(i2).load + " pid is " + fgLoadArray.get(i2).pid);
        }
        if (totalProcLoad >= this.mCpuFgLoadThreshold || taLoad + totalProcLoad >= this.mCpuTaLoadThreshold) {
            chgProcessGroup(fgLoadArray, CPUCTL_LIMIT_PROC_PATH);
            return;
        }
        for (int i3 = 0; i3 < taLoadArraySize; i3++) {
            if (taLoadArray.get(i3).load >= CPU_TA_LOAD_REGULAR_THRESHOLD_DEFAULT) {
                chgProcessGroup(taLoadArray.get(i3).pid, CPUCTL_LIMIT_PROC_PATH);
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLowLoad() {
        int pid;
        File file = new File(CPUCTL_LIMIT_PROC_PATH);
        if (!file.exists() || !file.canRead()) {
            AwareLog.e(TAG, "setLowLoad file not exists or canot read!");
            return;
        }
        String groupProcs = null;
        try {
            groupProcs = FileUtilsEx.readTextFile(file, 0, (String) null);
        } catch (IOException e) {
            AwareLog.e(TAG, "IOException + " + e.getMessage());
        }
        if (groupProcs == null || groupProcs.length() == 0) {
            AwareLog.d(TAG, "setLowLoad readTextFile null procs!");
            return;
        }
        for (String str : groupProcs.split("\n")) {
            String strProc = str.trim();
            if (!(strProc.length() == 0 || (pid = parseInt(strProc)) == -1)) {
                setGroup(pid, CPUCTL_PROC_PATH);
            }
        }
    }

    private void setGroup(int pid, String file) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        if (CPUCTL_LIMIT_PROC_PATH.equals(file)) {
            buffer.putInt(CpuFeature.MSG_SET_LIMIT_CGROUP);
        } else if (CPUCTL_PROC_PATH.equals(file)) {
            buffer.putInt(CpuFeature.MSG_SET_FG_CGROUP);
        } else {
            return;
        }
        buffer.putInt(pid);
        AwareLog.i(TAG, "setGroup pid = " + pid + " processgroup = " + file);
        CpuFeature cpuFeature = this.mCpuFeatureInstance;
        if (cpuFeature != null) {
            cpuFeature.sendPacket(buffer);
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
        String procName = null;
        try {
            procName = FileUtilsEx.readTextFile(file, 0, (String) null);
        } catch (IOException e) {
            AwareLog.e(TAG, "isLimitProc IOException + " + e.getMessage());
        }
        if (procName == null) {
            return false;
        }
        String procName2 = procName.trim();
        if (!"com.android.systemui".contains(procName2)) {
            return true;
        }
        AwareLog.d(TAG, "procName:" + procName2 + "is limit");
        return false;
    }

    private boolean isFgProc(int pid) {
        return isFgThirdPartProc(PTAH_PROC_INFO + pid + PTAH_CGROUP);
    }

    private boolean isThirdPartyUid(String line) {
        int index;
        if (line == null || !line.contains(CGROUP_CPUACCT) || (index = line.indexOf(UID_PTAH)) == -1) {
            return false;
        }
        int indexSlash = line.indexOf(47, index);
        String strUid = null;
        if (indexSlash != -1) {
            strUid = line.substring(UID_PTAH.length() + index, indexSlash);
        }
        if (UserHandleEx.getAppId(parseInt(strUid)) > 10000) {
            return true;
        }
        return false;
    }

    private boolean isFgThirdPartProc(String path) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            boolean flag = false;
            while (true) {
                String line = br.readLine();
                if (line == null) {
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
                    try {
                        if (isThirdPartyUid(line)) {
                            FileContent.closeBufferedReader(br);
                            FileContent.closeInputStreamReader(isr);
                            FileContent.closeFileInputStream(fis);
                            return true;
                        }
                    } catch (UnsupportedEncodingException | NumberFormatException e) {
                        AwareLog.e(TAG, "Invalid NumberFormat or UnsupportedEncoding");
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            AwareLog.e(TAG, "Invalid file");
        } catch (IOException e3) {
            AwareLog.e(TAG, "IOException");
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
        List<String> listTemp = new ArrayList<>();
        if (content == null || content.length() == 0) {
            AwareLog.e(TAG, "getWallTime null content!");
            return listTemp;
        }
        String[] conts = content.split(" ");
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
        long userTime = parseLong(listTemp.get(1));
        long nice = parseLong(listTemp.get(2));
        long system = parseLong(listTemp.get(3));
        long idle = parseLong(listTemp.get(4));
        long iowait = parseLong(listTemp.get(5));
        long irq = parseLong(listTemp.get(6));
        long softIrq = parseLong(listTemp.get(7));
        if (userTime != -1 && nice != -1 && system != -1 && idle != -1 && iowait != -1 && irq != -1 && softIrq != -1) {
            return new ProcessStatInfo(userTime, nice, system, idle, iowait, irq, softIrq);
        }
        AwareLog.e(TAG, "getWallTime inalid value!");
        return null;
    }

    private long parseLong(String str) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseLong NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    private int parseInt(String str) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    private long getProcessLoad(int pid) {
        String content = getContentWithOneLine(PTAH_PROC_INFO + pid + PATH_STAT, String.valueOf(pid));
        if (content != null) {
            return getProcessTime(content);
        }
        AwareLog.e(TAG, "getProcessLoad null content!");
        return -1;
    }

    private long getProcessTime(String content) {
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
        AwareLog.e(TAG, "getProcessTime inalid value!");
        return -1;
    }

    public String getContentWithOneLine(String filePath, String keyword) {
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
        String line = null;
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
            AwareLog.e(TAG, "Invalid file");
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
    public static class ProcLoadPair {
        public int load;
        public int pid;

        public ProcLoadPair(int pid2, int load2) {
            this.pid = pid2;
            this.load = load2;
        }
    }

    /* access modifiers changed from: private */
    public static class ProcLoadComparator implements Comparator<ProcLoadPair>, Serializable {
        private static final long serialVersionUID = 1;

        private ProcLoadComparator() {
        }

        public int compare(ProcLoadPair lhs, ProcLoadPair rhs) {
            return Integer.compare(rhs.load, lhs.load);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageForLoadDetector() {
        this.mLoadDetectorHandler.removeMessages(1);
        this.mLoadDetectorHandler.sendEmptyMessageDelayed(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
    }

    /* access modifiers changed from: private */
    public class CpuLoadDetectorHandler extends Handler {
        private CpuLoadDetectorHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what != 1) {
                AwareLog.e(CpuHighFgControl.TAG, "CpuLoadDetectorHandler msg = " + what + " not support!");
                return;
            }
            CpuHighFgControl.this.notifyLoadChange(4);
        }
    }

    /* access modifiers changed from: private */
    public static class ProcessStatInfo {
        private long mIdle;
        private long mIowait;
        private long mIrq;
        private long mNice;
        private long mSoftIrq;
        private long mSystem;
        private long mUserTime;

        public ProcessStatInfo(long userTime, long nice, long system, long idle, long iowait, long irq, long softIrq) {
            this.mUserTime = userTime;
            this.mNice = nice;
            this.mSystem = system;
            this.mIdle = idle;
            this.mIowait = iowait;
            this.mIrq = irq;
            this.mSoftIrq = softIrq;
        }

        public long getWallTime() {
            return this.mUserTime + this.mNice + this.mSystem + this.mIdle + this.mIowait + this.mIrq + this.mSoftIrq;
        }

        public long getRunningTime() {
            return this.mUserTime + this.mNice + this.mSystem;
        }
    }
}
