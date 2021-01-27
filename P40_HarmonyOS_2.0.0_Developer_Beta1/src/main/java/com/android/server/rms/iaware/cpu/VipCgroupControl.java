package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.CommonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VipCgroupControl {
    private static final Object SLOCK = new Object();
    private static final String TAG = "VipCgroupControl";
    private static VipCgroupControl sInstance;
    private CpuFeature mCpuFeatureInstance;
    private boolean mEnable;

    private VipCgroupControl() {
    }

    public static VipCgroupControl getInstance() {
        VipCgroupControl vipCgroupControl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new VipCgroupControl();
            }
            vipCgroupControl = sInstance;
        }
        return vipCgroupControl;
    }

    public void enable(CpuFeature cpuFeature) {
        this.mCpuFeatureInstance = cpuFeature;
        this.mEnable = true;
    }

    public void disable() {
        this.mEnable = false;
    }

    public void notifyForkChange(int tid, int tgid) {
        if (this.mEnable && !isVip(tid, tgid)) {
            moveThreadToTa(tid);
        }
    }

    private void moveThreadToTa(int tid) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(CpuFeature.MSG_SET_THREAD_TO_TA);
        buffer.putInt(tid);
        CpuFeature cpuFeature = this.mCpuFeatureInstance;
        if (cpuFeature != null) {
            cpuFeature.sendPacket(buffer);
        }
    }

    private boolean isVip(int tid, int tgid) {
        String vip = getFileString("/proc/" + tgid + "/task/" + tid + "/static_vip");
        if (vip != null && !"1".equals(vip)) {
            return false;
        }
        return true;
    }

    private String getFileString(String path) {
        File file = new File(path);
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            while (true) {
                int temp = inputStream.read();
                if (temp == -1) {
                    break;
                }
                char ch = (char) temp;
                if (!(ch == '\n' || ch == '\r')) {
                    sb.append(ch);
                }
            }
            CommonUtils.closeStream(inputStream, TAG, "checkIsTargetGroup close catch IOException");
            if (sb.length() != 0) {
                return sb.toString();
            }
            AwareLog.e(TAG, "checkIsTargetGroup read fail");
            return null;
        } catch (IOException e) {
            AwareLog.d(TAG, "checkIsTargetGroup read catch IOException msg = " + e.getMessage());
            CommonUtils.closeStream(null, TAG, "checkIsTargetGroup close catch IOException");
            return null;
        } catch (Throwable th) {
            CommonUtils.closeStream(null, TAG, "checkIsTargetGroup close catch IOException");
            throw th;
        }
    }
}
