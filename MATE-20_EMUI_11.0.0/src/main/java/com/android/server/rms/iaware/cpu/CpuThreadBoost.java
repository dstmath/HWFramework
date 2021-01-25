package com.android.server.rms.iaware.cpu;

import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CpuFeature;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CpuThreadBoost {
    private static final Object SLOCK = new Object();
    private static final String STR_SYSTEM_SERVER_PROC_NAME = "system_server";
    private static final String TAG = "CpuThreadBoost";
    private static CpuThreadBoost sInstance;
    private List<String> mBoostThreadsList = new ArrayList();
    private CpuFeature.CpuFeatureHandler mCpuFeatureHandler;
    private CpuFeature mCpuFeatureInstance;
    private boolean mEnable = false;
    private int mMyPid = 0;

    private CpuThreadBoost() {
    }

    public static CpuThreadBoost getInstance() {
        CpuThreadBoost cpuThreadBoost;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuThreadBoost();
            }
            cpuThreadBoost = sInstance;
        }
        return cpuThreadBoost;
    }

    public void setBoostThreadsList(String[] threadsBoostInfo) {
        if (threadsBoostInfo == null) {
            AwareLog.i(TAG, "threadBootInfo is empty");
            return;
        }
        int len = threadsBoostInfo.length;
        this.mBoostThreadsList.clear();
        int index = 0;
        while (index < len) {
            if (ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[index])) {
                index = obtainBoostProcInfo(threadsBoostInfo, index + 1, len);
            }
            index++;
        }
    }

    public void notifyCommChange(int pid, int tgid) {
        if (this.mEnable) {
            int isBoost = 0;
            if (tgid == this.mMyPid) {
                isBoost = 1;
            }
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putInt(CpuFeature.MSG_BINDER_THREAD_CREATE);
            buffer.putInt(tgid);
            buffer.putInt(pid);
            buffer.putInt(isBoost);
            CpuFeature cpuFeature = this.mCpuFeatureInstance;
            if (cpuFeature != null) {
                cpuFeature.sendPacket(buffer);
            }
        }
    }

    private int obtainBoostProcInfo(String[] threadsBoostInfo, int start, int len) {
        int index = start;
        if (index >= len) {
            return index;
        }
        if (STR_SYSTEM_SERVER_PROC_NAME.equals(threadsBoostInfo[index])) {
            index++;
            while (index < len && !ThreadBoostConfig.GAP_IDENTIFIER.equals(threadsBoostInfo[index])) {
                this.mBoostThreadsList.add(threadsBoostInfo[index]);
                index++;
            }
        }
        return index - 1;
    }

    public void start(CpuFeature feature, CpuFeature.CpuFeatureHandler handler) {
        this.mEnable = true;
        this.mCpuFeatureInstance = feature;
        this.mCpuFeatureHandler = handler;
        this.mMyPid = Process.myPid();
        List<String> tidStrArray = new ArrayList<>();
        getSystemThreads(this.mMyPid, tidStrArray);
        sendPacket(tidStrArray);
    }

    public void stop() {
        this.mEnable = false;
    }

    private void getSystemThreads(int pid, List<String> tidStrArray) {
        String tidStr;
        File[] subFiles = new File("/proc/" + pid + "/task/").listFiles();
        if (subFiles != null) {
            for (File eachTidFile : subFiles) {
                try {
                    String tidPath = eachTidFile.getCanonicalPath();
                    String tidName = CpuCommonUtil.getThreadName(tidPath);
                    if (tidName != null) {
                        int boostThreadsListSize = this.mBoostThreadsList.size();
                        for (int i = 0; i < boostThreadsListSize; i++) {
                            if (tidName.contains(this.mBoostThreadsList.get(i)) && (tidStr = CpuCommonUtil.getTidStr(tidPath)) != null) {
                                tidStrArray.add(tidStr);
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    private void sendPacket(List<String> tidStrArray) {
        int num = tidStrArray.size();
        int[] tids = new int[num];
        for (int i = 0; i < num; i++) {
            try {
                tids[i] = Integer.parseInt(tidStrArray.get(i));
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "parseInt failed!");
                return;
            }
        }
        int len = tids.length;
        ByteBuffer buffer = ByteBuffer.allocate((len + 2) * 4);
        buffer.putInt(CpuFeature.MSG_THREAD_BOOST);
        buffer.putInt(len);
        for (int i2 : tids) {
            buffer.putInt(i2);
        }
        CpuFeature cpuFeature = this.mCpuFeatureInstance;
        if (cpuFeature != null) {
            cpuFeature.sendPacket(buffer);
        }
    }

    private void removeCpusMsg() {
        CpuFeature.CpuFeatureHandler cpuFeatureHandler = this.mCpuFeatureHandler;
        if (cpuFeatureHandler != null) {
            cpuFeatureHandler.removeMessages(CpuFeature.MSG_SET_BOOST_CPUS);
            this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_RESET_BOOST_CPUS);
        }
    }

    public void setBoostCpus() {
        if (this.mEnable && this.mCpuFeatureHandler != null) {
            removeCpusMsg();
            this.mCpuFeatureHandler.sendEmptyMessage(CpuFeature.MSG_SET_BOOST_CPUS);
        }
    }

    public void resetBoostCpus() {
        if (this.mEnable && this.mCpuFeatureHandler != null) {
            removeCpusMsg();
            this.mCpuFeatureHandler.sendEmptyMessage(CpuFeature.MSG_RESET_BOOST_CPUS);
        }
    }
}
