package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CPUFeatureAMSCommunicator {
    private static final int APP_START_DURATION = 1500;
    private static final int INIT_DURATION = -1;
    private static final int MAX_TOP_APP_DURATION = 10000;
    private static final String TAG = "CPUFeatureAMSCommunicator";
    private static final int WINDOW_SWITCH_DURATION = 500;
    private static CPUFeatureAMSCommunicator sInstance;
    private CPUFeature mCPUFeatureInstance;
    private CPUFeatureAMSCommunicatorHandler mHandler;
    private int mLastSetTopDuration = -1;
    private long mLastSetTopTimeStamp = SystemClock.uptimeMillis();
    private Map<String, Integer> mSpecialAppMap = new ArrayMap();
    private AtomicBoolean mTopAppBoostEnable = new AtomicBoolean(false);

    private class CPUFeatureAMSCommunicatorHandler extends Handler {
        /* synthetic */ CPUFeatureAMSCommunicatorHandler(CPUFeatureAMSCommunicator this$0, CPUFeatureAMSCommunicatorHandler -this1) {
            this();
        }

        private CPUFeatureAMSCommunicatorHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CPUFeature.MSG_SET_TOP_APP_CPUSET /*116*/:
                    CPUFeatureAMSCommunicator.this.setTopAppCpuSet(msg.arg1);
                    return;
                case CPUFeature.MSG_RESET_TOP_APP_CPUSET /*117*/:
                    CPUFeatureAMSCommunicator.this.resetTopAppCpuSet();
                    return;
                default:
                    AwareLog.w(CPUFeatureAMSCommunicator.TAG, "handleMessage default msg what = " + msg.what);
                    return;
            }
        }
    }

    private CPUFeatureAMSCommunicator() {
    }

    public static synchronized CPUFeatureAMSCommunicator getInstance() {
        CPUFeatureAMSCommunicator cPUFeatureAMSCommunicator;
        synchronized (CPUFeatureAMSCommunicator.class) {
            if (sInstance == null) {
                sInstance = new CPUFeatureAMSCommunicator();
            }
            cPUFeatureAMSCommunicator = sInstance;
        }
        return cPUFeatureAMSCommunicator;
    }

    public void start(CPUFeature feature) {
        initHandler();
        this.mCPUFeatureInstance = feature;
        this.mTopAppBoostEnable.set(true);
    }

    private void initHandler() {
        if (this.mHandler == null) {
            this.mHandler = new CPUFeatureAMSCommunicatorHandler(this, null);
        }
    }

    public void stop() {
        this.mTopAppBoostEnable.set(false);
        removeAllMsg();
        resetTopAppCpuSet();
    }

    /* JADX WARNING: Missing block: B:10:0x0026, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x0038, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setTopAppToBoost(int type, String pkgName) {
        int duration;
        switch (type) {
            case 1:
                duration = 1500;
                startSpecialApp(pkgName);
                break;
            case 2:
                duration = 500;
                break;
            case 3:
                CPUAppStartOnFire.getInstance().setOnFire();
                return;
            default:
                AwareLog.e(TAG, "set app boost but type is unknown");
                return;
        }
        if (!(pkgName == null || pkgName.contains("com.antutu") || !isValidTopDuration(duration) || (isTopAppBoostEnable() ^ 1) != 0 || this.mHandler == null)) {
            Message msg = this.mHandler.obtainMessage(CPUFeature.MSG_SET_TOP_APP_CPUSET);
            msg.arg1 = duration;
            this.mHandler.sendMessage(msg);
        }
    }

    private void setTopAppCpuSet(int duration) {
        int pastTimeFromLast = (int) (SystemClock.uptimeMillis() - this.mLastSetTopTimeStamp);
        if (this.mLastSetTopDuration == -1 || pastTimeFromLast > this.mLastSetTopDuration || pastTimeFromLast + duration > this.mLastSetTopDuration) {
            this.mHandler.removeMessages(CPUFeature.MSG_RESET_TOP_APP_CPUSET);
            sendPacketByMsgCode(CPUFeature.MSG_SET_TOP_APP_CPUSET);
            this.mHandler.sendEmptyMessageDelayed(CPUFeature.MSG_RESET_TOP_APP_CPUSET, (long) duration);
            updateLastTop(duration);
        }
    }

    private void updateLastTop(int duration) {
        this.mLastSetTopTimeStamp = SystemClock.uptimeMillis();
        this.mLastSetTopDuration = duration;
    }

    private boolean isValidTopDuration(int duration) {
        return duration > 0 && duration <= 10000;
    }

    private void resetTopAppCpuSet() {
        sendPacketByMsgCode(CPUFeature.MSG_RESET_TOP_APP_CPUSET);
    }

    private void removeAllMsg() {
        this.mHandler.removeMessages(CPUFeature.MSG_SET_TOP_APP_CPUSET);
        this.mHandler.removeMessages(CPUFeature.MSG_RESET_TOP_APP_CPUSET);
    }

    private void sendPacketByMsgCode(int msg) {
        if (this.mCPUFeatureInstance != null) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(msg);
            int resCode = this.mCPUFeatureInstance.sendPacket(buffer);
            if (resCode != 1) {
                AwareLog.e(TAG, "sendPacketByMsgCode sendPacket failed, msg:" + msg + ",send error code:" + resCode);
            }
        }
    }

    private boolean isTopAppBoostEnable() {
        return this.mTopAppBoostEnable.get() ? CPUPowerMode.isPerformanceMode() : false;
    }

    public void updateSpecilaAppMap(String pkgName, int cmdId) {
        if (pkgName != null) {
            this.mSpecialAppMap.put(pkgName, Integer.valueOf(cmdId));
        }
    }

    private void startSpecialApp(String pkgName) {
        if (isInSpecialAppMap(pkgName)) {
            doSpecialAppStart(pkgName);
        }
    }

    private boolean isInSpecialAppMap(String pkgName) {
        if (pkgName != null) {
            return this.mSpecialAppMap.containsKey(pkgName);
        }
        AwareLog.d(TAG, "pkgName is null!");
        return false;
    }

    private void doSpecialAppStart(String pkgName) {
        int cmdId = ((Integer) this.mSpecialAppMap.get(pkgName)).intValue();
        if (cmdId > 0) {
            UniPerf.getInstance().uniPerfEvent(cmdId, "", new int[0]);
        } else {
            AwareLog.d(TAG, "invalid cmdid " + cmdId);
        }
    }
}
