package com.android.server.rms.iaware.cpu;

import android.os.Bundle;
import android.os.Message;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerServiceEx;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.ProcessExt;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuVipThread {
    private static final int BYTE_BUFFER_SIZE = 4;
    private static final int INVALID_VALUE = -1;
    public static final int SCHED_VIP_PRIO_KEY_THREAD = 8;
    private static final int SCHED_VIP_PRIO_TOP_APP = 10;
    private static final Object SLOCK = new Object();
    private static final String STR_IAWAREPERF_PROC_NAME = "iawareperf@1.0-";
    private static final String STR_PERFGENIUS_PROC_NAME = "perfgenius@2.0-";
    private static final String STR_SERVICE_MANAGER_PROC_NAME = "servicemanager";
    private static final String TAG = "CpuVipThread";
    private static CpuVipThread sInstance;
    private CpuFeature mCpuFeatureInstance;
    private int mCurPid = -1;
    private List<Integer> mCurThreads;
    private List<String> mSetVipThreadConfig = new ArrayList();
    private List<Integer> mSetVipThreadList = new ArrayList();
    private AtomicBoolean mVipEnable = new AtomicBoolean(false);
    private CpuFeature.CpuFeatureHandler mVipHandler;
    private AtomicBoolean mVipSchedEnable = new AtomicBoolean(false);

    private CpuVipThread() {
    }

    public static CpuVipThread getInstance() {
        CpuVipThread cpuVipThread;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuVipThread();
            }
            cpuVipThread = sInstance;
        }
        return cpuVipThread;
    }

    public void sendPacket(int pid, List<Integer> threads, int msg) {
        if (!(this.mCpuFeatureInstance == null || threads == null || pid <= 0)) {
            if (msg != 144 || !CpuMultiDisplay.getInstance().isInFocusPidList(pid)) {
                int num = threads.size();
                ByteBuffer buffer = ByteBuffer.allocate((num + 3) * 4);
                buffer.putInt(msg);
                buffer.putInt(num);
                buffer.putInt(pid);
                for (int i = 0; i < num; i++) {
                    buffer.putInt(threads.get(i).intValue());
                }
                if (this.mCpuFeatureInstance.sendPacket(buffer) != 1) {
                    AwareLog.e(TAG, "send failed");
                }
            }
        }
    }

    public void notifyProcessGroupChange(int pid, int renderThreadTid, int grp) {
        CpuFeature.CpuFeatureHandler cpuFeatureHandler;
        if (this.mVipEnable.get() && grp == 3 && (cpuFeatureHandler = this.mVipHandler) != null) {
            Message msg = cpuFeatureHandler.obtainMessage(CpuFeature.MSG_SET_THREAD_TO_VIP);
            Bundle bundle = new Bundle();
            bundle.putInt(CpuFeature.KEY_BUNDLE_PID, pid);
            bundle.putInt(CpuFeature.KEY_BUNDLE_RENDER_TID, renderThreadTid);
            msg.setData(bundle);
            this.mVipHandler.sendMessage(msg);
        }
    }

    public void setAppVipThread(int pid, List<Integer> threads, boolean isSet, boolean isSetGroup) {
        synchronized (SLOCK) {
            if (isSet) {
                this.mCurThreads = threads;
                this.mCurPid = pid;
            }
            if (this.mVipEnable.get()) {
                if (threads != null) {
                    if (pid > 0) {
                        if (this.mVipHandler != null) {
                            Message msg = this.mVipHandler.obtainMessage();
                            msg.what = isSet ? CpuFeature.MSG_SET_VIP_THREAD : CpuFeature.MSG_RESET_VIP_THREAD;
                            msg.arg1 = pid;
                            msg.arg2 = isSetGroup ? 1 : -1;
                            msg.obj = threads;
                            this.mVipHandler.sendMessage(msg);
                        }
                        return;
                    }
                }
                AwareLog.e(TAG, "thread is null or pid <= 0 :" + pid);
            }
        }
    }

    public void setHandler(CpuFeature.CpuFeatureHandler handler) {
        synchronized (SLOCK) {
            this.mVipHandler = handler;
        }
    }

    public void start(CpuFeature feature) {
        List<Integer> list;
        this.mVipEnable.set(true);
        this.mCpuFeatureInstance = feature;
        int i = this.mCurPid;
        if (i > 0 && (list = this.mCurThreads) != null) {
            sendPacket(i, list, CpuFeature.MSG_SET_VIP_THREAD);
        }
    }

    public void stop() {
        List<Integer> list;
        this.mVipEnable.set(false);
        int i = this.mCurPid;
        if (i > 0 && (list = this.mCurThreads) != null) {
            sendPacket(i, list, CpuFeature.MSG_RESET_VIP_THREAD);
        }
    }

    public void setDisplayToVip(boolean isSet, int pid) {
        int msgId = isSet ? CpuFeature.MSG_SET_VIP_THREAD : CpuFeature.MSG_RESET_VIP_THREAD;
        int renderTid = HwActivityManagerServiceEx.getRenderTid(pid);
        if (renderTid < 0) {
            HwActivityManager.setProcessRecForPid(pid);
            renderTid = HwActivityManagerServiceEx.getRenderTid(pid);
        }
        ArrayList<Integer> tids = new ArrayList<>();
        tids.add(Integer.valueOf(pid));
        tids.add(Integer.valueOf(renderTid));
        sendPacket(pid, tids, msgId);
    }

    /* access modifiers changed from: protected */
    public List<String> getVipPidList() {
        if (this.mSetVipThreadConfig.isEmpty()) {
            this.mSetVipThreadConfig.add(STR_IAWAREPERF_PROC_NAME);
            this.mSetVipThreadConfig.add(STR_PERFGENIUS_PROC_NAME);
            this.mSetVipThreadConfig.add(STR_SERVICE_MANAGER_PROC_NAME);
        }
        return this.mSetVipThreadConfig;
    }

    /* access modifiers changed from: protected */
    public void setVipPidList(String name, int pid) {
        if (name != null) {
            this.mSetVipThreadList.add(Integer.valueOf(pid));
        }
    }

    /* access modifiers changed from: protected */
    public void enableThreadVipSched() {
        setVipSched(8);
        this.mVipSchedEnable.set(true);
    }

    /* access modifiers changed from: protected */
    public void disableThreadVipSched() {
        this.mVipSchedEnable.set(false);
        setVipSched(0);
        this.mSetVipThreadConfig.clear();
        this.mSetVipThreadList.clear();
    }

    /* access modifiers changed from: protected */
    public void setThreadVipPolicy(int msgId, int pid, List<Integer> threads) {
        if (this.mVipSchedEnable.get()) {
            if (msgId == 143 || msgId == 144) {
                setThreadVipPriority(threads, msgId == 143 ? 10 : 0);
            }
        }
    }

    public void setThreadVipAndQos(int pid, String hostingType) {
        if (this.mVipSchedEnable.get() && pid > 0 && BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG.equals(hostingType)) {
            ResourceCollector.setThreadVipPriority(pid, 10);
            ProcessExt.setThreadQosPolicy(pid, 10);
        }
    }

    private void setVipSched(int priority) {
        int subPid;
        if (!this.mSetVipThreadList.isEmpty()) {
            for (Integer pid : this.mSetVipThreadList) {
                if (pid != null && (subPid = pid.intValue()) > 0) {
                    List<Integer> tidArray = new ArrayList<>();
                    CpuCommonUtil.getThreads(subPid, tidArray);
                    setThreadVipPriority(tidArray, priority);
                }
            }
        }
    }

    private void setThreadVipPriority(List<Integer> tidArray, int priority) {
        if (!tidArray.isEmpty()) {
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "setThreadVipPriority, tidArray = " + tidArray + " priority = " + priority);
            }
            for (Integer tid : tidArray) {
                int tmpTid = tid.intValue();
                if (tmpTid > 0) {
                    ResourceCollector.setThreadVipPriority(tmpTid, priority);
                }
            }
        }
    }
}
