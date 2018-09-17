package com.android.server.rms.iaware.cpu;

import android.os.Message;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CPUFeature.CPUFeatureHandler;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CPUVipThread {
    private static final String TAG = "CPUVipThread";
    private static CPUVipThread sInstance;
    private CPUFeature mCPUFeatureInstance;
    private int mCurPid = -1;
    private List<Integer> mCurThreads;
    private AtomicBoolean mVipEnable = new AtomicBoolean(false);
    private CPUFeatureHandler mVipHandler;

    private CPUVipThread() {
    }

    public static synchronized CPUVipThread getInstance() {
        CPUVipThread cPUVipThread;
        synchronized (CPUVipThread.class) {
            if (sInstance == null) {
                sInstance = new CPUVipThread();
            }
            cPUVipThread = sInstance;
        }
        return cPUVipThread;
    }

    public void sendPacket(int pid, List<Integer> threads, int msg) {
        if (this.mCPUFeatureInstance != null && threads != null && pid > 0) {
            int num = threads.size();
            ByteBuffer buffer = ByteBuffer.allocate((num * 4) + 12);
            buffer.putInt(msg);
            buffer.putInt(num);
            buffer.putInt(pid);
            for (int i = 0; i < num; i++) {
                buffer.putInt(((Integer) threads.get(i)).intValue());
            }
            if (this.mCPUFeatureInstance.sendPacket(buffer) != 1) {
                AwareLog.e(TAG, "send failed");
            }
        }
    }

    /* JADX WARNING: Missing block: B:22:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setAppVipThread(int pid, List<Integer> threads, boolean isSet) {
        if (isSet) {
            this.mCurThreads = threads;
            this.mCurPid = pid;
        }
        if (!this.mVipEnable.get()) {
            return;
        }
        if (threads == null || pid <= 0) {
            AwareLog.e(TAG, "thread is null or pid <= 0 :" + pid);
        } else if (this.mVipHandler != null) {
            Message msg = this.mVipHandler.obtainMessage();
            msg.what = isSet ? CPUFeature.MSG_SET_VIP_THREAD : CPUFeature.MSG_RESET_VIP_THREAD;
            msg.arg1 = pid;
            msg.obj = threads;
            this.mVipHandler.sendMessage(msg);
        }
    }

    public synchronized void setHandler(CPUFeatureHandler mHandler) {
        this.mVipHandler = mHandler;
    }

    public void start(CPUFeature feature) {
        this.mVipEnable.set(true);
        this.mCPUFeatureInstance = feature;
        if (this.mCurPid > 0 && this.mCurThreads != null) {
            sendPacket(this.mCurPid, this.mCurThreads, CPUFeature.MSG_SET_VIP_THREAD);
        }
    }

    public void stop() {
        this.mVipEnable.set(false);
        if (this.mCurPid > 0 && this.mCurThreads != null) {
            sendPacket(this.mCurPid, this.mCurThreads, CPUFeature.MSG_RESET_VIP_THREAD);
        }
    }
}
