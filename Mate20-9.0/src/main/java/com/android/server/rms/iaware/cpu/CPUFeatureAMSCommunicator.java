package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.pfw.autostartup.comm.XmlConst;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CPUFeatureAMSCommunicator {
    private static final int APP_START_DURATION = 1500;
    private static final int INIT_DURATION = -1;
    private static final int MAX_TOP_APP_DURATION = 10000;
    private static final String TAG = "CPUFeatureAMSCommunicator";
    private static final int UNIPERF_BOOST_OFF = 4;
    private static final int WINDOW_SWITCH_DURATION = 500;
    private static CPUFeatureAMSCommunicator sInstance;
    private CPUFeature mCPUFeatureInstance;
    private int mDelayOffTime = 200;
    private CPUFeatureAMSCommunicatorHandler mHandler;
    private int mLastSetTopDuration = -1;
    private long mLastSetTopTimeStamp = SystemClock.uptimeMillis();
    private String mLaunchingPkg;
    private AtomicBoolean mOnDemandBoostEnable = new AtomicBoolean(false);
    private Map<String, Integer> mSpecialAppMap = new ArrayMap();
    private AtomicBoolean mTopAppBoostEnable = new AtomicBoolean(false);
    private ArrayList<Integer> mUniperfCmdIds = new ArrayList<>();

    private class CPUFeatureAMSCommunicatorHandler extends Handler {
        private CPUFeatureAMSCommunicatorHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 116:
                    CPUFeatureAMSCommunicator.this.setTopAppCpuSet(msg.arg1);
                    return;
                case CPUFeature.MSG_RESET_TOP_APP_CPUSET /*117*/:
                    CPUFeatureAMSCommunicator.this.resetTopAppCpuSet();
                    return;
                case CPUFeature.MSG_UNIPERF_BOOST_ON /*118*/:
                    CPUFeatureAMSCommunicator.this.dealUniperfOnEvent(msg);
                    return;
                case CPUFeature.MSG_UNIPERF_BOOST_OFF /*119*/:
                    CPUFeatureAMSCommunicator.this.dealUniperfOffEvent(msg);
                    return;
                case 120:
                    CPUFeatureAMSCommunicator.this.sendUniperfOffEvent();
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
    }

    private void initHandler() {
        if (this.mHandler == null) {
            this.mHandler = new CPUFeatureAMSCommunicatorHandler();
        }
    }

    public void stop() {
        removeAllMsg();
        resetTopAppCpuSet();
    }

    public void setTopAppBoostEnable(boolean enable) {
        this.mTopAppBoostEnable.set(enable);
    }

    public void setOnDemandBoostEnable(boolean enable) {
        this.mOnDemandBoostEnable.set(enable);
    }

    public void setTopAppToBoost(int type, String pkgNameExtra) {
        int duration = 0;
        String pkgName = pkgNameExtra;
        boolean isUniperfOn = false;
        if (pkgNameExtra != null) {
            String[] strings = pkgNameExtra.split(":");
            pkgName = strings[0];
            if (strings.length >= 2 && XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_.equals(strings[1])) {
                isUniperfOn = true;
            }
        }
        switch (type) {
            case 1:
                duration = 1500;
                if (isUniperfOn) {
                    doUniperfOnEvent(pkgName, 4099);
                }
                startSpecialApp(pkgName);
                break;
            case 2:
                duration = 500;
                if (isUniperfOn) {
                    doUniperfOnEvent(pkgName, 4098);
                    break;
                }
                break;
            case 3:
                CPUAppStartOnFire.getInstance().setOnFire();
                return;
            case 4:
                doUniperfOffEvent(pkgName);
                break;
            default:
                AwareLog.e(TAG, "set app boost but type is unknown");
                return;
        }
        if (pkgName != null && isValidTopDuration(duration) && isTopAppBoostEnable() && this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage(116);
            msg.arg1 = duration;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void setTopAppCpuSet(int duration) {
        int pastTimeFromLast = (int) (SystemClock.uptimeMillis() - this.mLastSetTopTimeStamp);
        if (this.mLastSetTopDuration == -1 || pastTimeFromLast > this.mLastSetTopDuration || pastTimeFromLast + duration > this.mLastSetTopDuration) {
            this.mHandler.removeMessages(CPUFeature.MSG_RESET_TOP_APP_CPUSET);
            sendPacketByMsgCode(116);
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

    /* access modifiers changed from: private */
    public void resetTopAppCpuSet() {
        sendPacketByMsgCode(CPUFeature.MSG_RESET_TOP_APP_CPUSET);
    }

    private void removeAllMsg() {
        this.mHandler.removeMessages(116);
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
        return this.mTopAppBoostEnable.get() && CPUPowerMode.isPerformanceMode();
    }

    /* access modifiers changed from: private */
    public void sendUniperfOffEvent() {
        int size = this.mUniperfCmdIds.size();
        for (int i = 0; i < size; i++) {
            int cmdId = getCmdId(this.mUniperfCmdIds.get(i));
            if (cmdId > 0) {
                UniPerf.getInstance().uniPerfEvent(cmdId, "", new int[]{-1});
            }
        }
        this.mUniperfCmdIds.clear();
    }

    private int getCmdId(Integer cmdId) {
        if (cmdId != null) {
            return cmdId.intValue();
        }
        return -1;
    }

    private void doUniperfOnEvent(String pkgName, int uniperfCmdId) {
        if (this.mOnDemandBoostEnable.get() && this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage(CPUFeature.MSG_UNIPERF_BOOST_ON);
            msg.obj = pkgName;
            msg.arg1 = uniperfCmdId;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void dealUniperfOnEvent(Message msg) {
        String pkgName = (String) msg.obj;
        int uniperfCmdId = msg.arg1;
        if (pkgName != null) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(120);
            }
            this.mUniperfCmdIds.add(Integer.valueOf(uniperfCmdId));
            this.mLaunchingPkg = pkgName;
            if (uniperfCmdId == 4099) {
                this.mDelayOffTime = OnDemandBoost.getInstance().getColdStartOffDelay();
            } else {
                this.mDelayOffTime = OnDemandBoost.getInstance().getWinSwitchOffDelay();
            }
        }
    }

    private void doUniperfOffEvent(String pkgName) {
        if (this.mOnDemandBoostEnable.get() && this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage(CPUFeature.MSG_UNIPERF_BOOST_OFF);
            msg.obj = pkgName;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void dealUniperfOffEvent(Message msg) {
        String pkgName = (String) msg.obj;
        if (pkgName != null && this.mLaunchingPkg != null && this.mLaunchingPkg.length() > 0) {
            if (!pkgName.startsWith(this.mLaunchingPkg)) {
                this.mLaunchingPkg = "";
                return;
            }
            this.mLaunchingPkg = "";
            if (this.mHandler != null) {
                this.mHandler.removeMessages(120);
                this.mHandler.sendEmptyMessageDelayed(120, (long) this.mDelayOffTime);
            }
        }
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
        int cmdId = getCmdId(this.mSpecialAppMap.get(pkgName));
        if (cmdId > 0) {
            UniPerf.getInstance().uniPerfEvent(cmdId, "", new int[0]);
            return;
        }
        AwareLog.d(TAG, "invalid cmdid " + cmdId);
    }
}
