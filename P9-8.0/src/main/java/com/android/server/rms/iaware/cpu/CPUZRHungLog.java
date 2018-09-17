package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.util.SparseArray;
import android.util.ZRHung;
import android.util.ZRHung.HungConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CPUZRHungLog {
    private static final int CPU_HIGHLOAD_POLLING_INTERVAL_DEFAULT = 1000;
    private static final int CPU_LOAD_APP_ARRAY_SIZE_DEFAULT = 10;
    private static final String CPU_STAT_PATH = "/proc/stat";
    private static final long INVALID_VALUE = -1;
    private static final int LOAD_STATUS_HIGH = 2;
    private static final int LOAD_STATUS_LOW = 1;
    private static final String PATH_CMDLINE = "/cmdline";
    private static final String PATH_STAT = "/stat";
    private static final String PROC_NAME_MATCHER = "^[\\d]+$";
    private static final String PTAH_CGROUP = "/cgroup";
    private static final String PTAH_PROC_INFO = "/proc/";
    private static final String TAG = "CPUZRHungLog";
    private int mCfgEnable = -1;
    private int mCfgSize = 10;
    private int mCurrStatus = 1;
    private int mPrevStatus = 1;
    private ProcLoadComparator mProcLoadComparator = new ProcLoadComparator();
    private boolean mUploadFlag = true;

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
            return this.userTime + this.nice;
        }
    }

    private void setUploadFlag(boolean value) {
        this.mUploadFlag = value;
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
                fgProcLoadArray.add(new ProcLoadPair(procPid, procLoad));
                totalLoad += procLoad;
            }
        }
        if (!fgProcLoadArray.isEmpty()) {
            Collections.sort(fgProcLoadArray, this.mProcLoadComparator);
        }
        return totalLoad;
    }

    private List<String> getAllProcsList() {
        List<String> procsList = new ArrayList();
        File procDir = new File(PTAH_PROC_INFO);
        if (!procDir.isDirectory()) {
            return procsList;
        }
        File[] dirList = procDir.listFiles();
        if (dirList == null || dirList.length == 0) {
            return procsList;
        }
        for (File dir : dirList) {
            String dirName = dir.getName();
            if (dir.isDirectory() && dirName.matches(PROC_NAME_MATCHER)) {
                procsList.add(dirName);
            }
        }
        return procsList;
    }

    private void getLimitProcsLoadArray(SparseArray<Long> pidList) {
        List<String> procsList = getAllProcsList();
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

    private void getLimitCpuloadPidArray(SparseArray<Long> pidList) {
        getLimitProcsLoadArray(pidList);
    }

    private boolean isTimeToUpload() {
        return this.mPrevStatus == 1 && this.mCurrStatus == 2 && this.mUploadFlag;
    }

    private boolean isEnabledToUpload() {
        boolean z = true;
        if (this.mCfgEnable != -1) {
            if (this.mCfgEnable != 1) {
                z = false;
            }
            return z;
        }
        try {
            HungConfig cfg = ZRHung.getHungConfig((short) 7);
            if (cfg.status != 0) {
                AwareLog.e(TAG, "isEnabledToUpload ZRHung.getHungConfig failed!");
                return false;
            }
            String[] values = cfg.value.split(",");
            this.mCfgEnable = parseInt(values[0].trim());
            this.mCfgSize = parseInt(values[3].trim());
            this.mCfgSize = this.mCfgSize > 0 ? this.mCfgSize : 10;
            if (this.mCfgEnable != 1) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            AwareLog.e(TAG, "isEnabledToUpload ZRHung.getHungConfig exception!");
            return false;
        }
    }

    private void assembleMessage(StringBuilder command, StringBuilder message, List<ProcLoadPair> pairArray) {
        int pairArraySize = pairArray.size();
        int i = 0;
        while (i < this.mCfgSize && i < pairArraySize) {
            ProcLoadPair pair = (ProcLoadPair) pairArray.get(i);
            message.append(getProcessName(pair.pid)).append(",");
            message.append(pair.pid).append(",");
            message.append(pair.load).append("\n");
            if (i == 0) {
                command.append("p=").append(pair.pid);
            }
            i++;
        }
    }

    public void loadPolicy() {
        AwareLog.d(TAG, "loadZRHungDetectorPolicy");
        if (isTimeToUpload() && isEnabledToUpload()) {
            ProcessStatInfo prevCpuStatInfo = getCpuStatInfo();
            if (prevCpuStatInfo == null) {
                AwareLog.e(TAG, "loadZRHungDetectorPolicy prevCpuStatInfo is null!");
                return;
            }
            SparseArray<Long> prevLimArray = new SparseArray();
            getLimitCpuloadPidArray(prevLimArray);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                AwareLog.e(TAG, "loadZRHungDetectorPolicy InterruptedException");
            }
            SparseArray<Long> currLimArray = new SparseArray();
            getLimitCpuloadPidArray(currLimArray);
            ProcessStatInfo currCpuStatInfo = getCpuStatInfo();
            if (currCpuStatInfo == null) {
                AwareLog.e(TAG, "loadZRHungDetectorPolicy currCpuStatInfo is null!");
                return;
            }
            long deltaTotalCpuRuntime = currCpuStatInfo.getWallTime() - prevCpuStatInfo.getWallTime();
            List<ProcLoadPair> totalProcLoadArray = new ArrayList();
            int totalLimitLoad = computeProcsLoad(prevLimArray, currLimArray, deltaTotalCpuRuntime, totalProcLoadArray);
            StringBuilder command = new StringBuilder();
            StringBuilder message = new StringBuilder();
            assembleMessage(command, message, totalProcLoadArray);
            try {
                if (!ZRHung.sendHungEvent((short) 7, command.toString(), message.toString())) {
                    AwareLog.e(TAG, "loadZRHungDetectorPolicy ZRHung.sendHungEvent failed!");
                }
            } catch (Exception e2) {
                AwareLog.e(TAG, "loadZRHungDetectorPolicy ZRHung.sendHungEvent exception!");
            }
            setUploadFlag(false);
        }
    }

    public void rotateStatus(int loadStatus) {
        this.mPrevStatus = this.mCurrStatus;
        this.mCurrStatus = loadStatus;
        if (loadStatus == 1) {
            setUploadFlag(true);
        }
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

    private String getProcessName(int pid) {
        StringBuilder targetPath = new StringBuilder();
        targetPath.append(PTAH_PROC_INFO);
        targetPath.append(pid);
        targetPath.append(PATH_CMDLINE);
        String content = getContentWithOneLine(targetPath.toString(), null);
        if (content == null) {
            AwareLog.e(TAG, "getProcessName null content!");
            content = "";
        }
        return content.trim();
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
}
