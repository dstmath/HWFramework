package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuFeatureAmsCommunicator {
    private static final int APP_START_DURATION = 1500;
    private static final int DAFAULT_DELAY_OFF_TIME = 200;
    private static final int DEFAULT_BYTE_SIZE = 4;
    private static final int INIT_DURATION = -1;
    private static final int INVALID_VALUE = -1;
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuFeatureAmsCommunicator";
    private static final int UNIPERF_BOOST_OFF = 4;
    private static final int WINDOW_SWITCH_DURATION = 500;
    private static CpuFeatureAmsCommunicator sInstance;
    private int mDelayOffTime = 200;
    private CpuFeatureAmsCommunicatorHandler mHandler;
    private String mLaunchingPkg;
    private AtomicBoolean mOnDemandBoostEnable = new AtomicBoolean(false);
    private Map<String, Integer> mSpecialAppMap = new ArrayMap();
    private ArrayList<Integer> mUniperfCmdIds = new ArrayList<>();

    private CpuFeatureAmsCommunicator() {
    }

    public static CpuFeatureAmsCommunicator getInstance() {
        CpuFeatureAmsCommunicator cpuFeatureAmsCommunicator;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuFeatureAmsCommunicator();
            }
            cpuFeatureAmsCommunicator = sInstance;
        }
        return cpuFeatureAmsCommunicator;
    }

    public void start(CpuFeature feature) {
        initHandler();
    }

    private void initHandler() {
        if (this.mHandler == null) {
            this.mHandler = new CpuFeatureAmsCommunicatorHandler();
        }
    }

    public void setOnDemandBoostEnable(boolean enable) {
        this.mOnDemandBoostEnable.set(enable);
    }

    /* access modifiers changed from: private */
    public class CpuFeatureAmsCommunicatorHandler extends Handler {
        private CpuFeatureAmsCommunicatorHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CpuFeature.MSG_UNIPERF_BOOST_ON /* 118 */:
                    CpuFeatureAmsCommunicator.this.dealUniperfOnEvent(msg);
                    return;
                case CpuFeature.MSG_UNIPERF_BOOST_OFF /* 119 */:
                    CpuFeatureAmsCommunicator.this.dealUniperfOffEvent(msg);
                    return;
                case CpuFeature.MSG_UNIPERF_BOOST_OFF_SEND /* 120 */:
                    CpuFeatureAmsCommunicator.this.sendUniperfOffEvent();
                    return;
                default:
                    AwareLog.w(CpuFeatureAmsCommunicator.TAG, "handleMessage default msg what = " + msg.what);
                    return;
            }
        }
    }

    public void setTopAppToBoost(int type, String pkgNameExtra) {
        String pkgName = pkgNameExtra;
        boolean isUniperfOn = false;
        if (pkgNameExtra != null) {
            String[] strings = pkgNameExtra.split(":");
            pkgName = strings[0];
            if (strings.length >= 2 && "on".equals(strings[1])) {
                isUniperfOn = true;
            }
        }
        if (type == 1) {
            if (isUniperfOn) {
                doUniperfOnEvent(pkgName, 4099);
            }
            startSpecialApp(pkgName);
        } else if (type != 2) {
            if (type == 3) {
                CpuAppStartOnFire.getInstance().setOnFire();
            } else if (type != 4) {
                AwareLog.e(TAG, "set app boost but type is unknown");
            } else {
                doUniperfOffEvent(pkgName);
            }
        } else if (isUniperfOn) {
            doUniperfOnEvent(pkgName, 4098);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUniperfOffEvent() {
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
        CpuFeatureAmsCommunicatorHandler cpuFeatureAmsCommunicatorHandler;
        if (this.mOnDemandBoostEnable.get() && (cpuFeatureAmsCommunicatorHandler = this.mHandler) != null) {
            Message msg = cpuFeatureAmsCommunicatorHandler.obtainMessage(CpuFeature.MSG_UNIPERF_BOOST_ON);
            msg.obj = pkgName;
            msg.arg1 = uniperfCmdId;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dealUniperfOnEvent(Message msg) {
        String pkgName = null;
        if (msg.obj instanceof String) {
            pkgName = (String) msg.obj;
        }
        int uniperfCmdId = msg.arg1;
        if (pkgName != null) {
            CpuFeatureAmsCommunicatorHandler cpuFeatureAmsCommunicatorHandler = this.mHandler;
            if (cpuFeatureAmsCommunicatorHandler != null) {
                cpuFeatureAmsCommunicatorHandler.removeMessages(CpuFeature.MSG_UNIPERF_BOOST_OFF_SEND);
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
        CpuFeatureAmsCommunicatorHandler cpuFeatureAmsCommunicatorHandler;
        if (this.mOnDemandBoostEnable.get() && (cpuFeatureAmsCommunicatorHandler = this.mHandler) != null) {
            Message msg = cpuFeatureAmsCommunicatorHandler.obtainMessage(CpuFeature.MSG_UNIPERF_BOOST_OFF);
            msg.obj = pkgName;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dealUniperfOffEvent(Message msg) {
        String str;
        String pkgName = null;
        if (msg.obj instanceof String) {
            pkgName = (String) msg.obj;
        }
        if (pkgName != null && (str = this.mLaunchingPkg) != null && str.length() > 0) {
            if (!pkgName.startsWith(this.mLaunchingPkg)) {
                this.mLaunchingPkg = "";
                return;
            }
            this.mLaunchingPkg = "";
            CpuFeatureAmsCommunicatorHandler cpuFeatureAmsCommunicatorHandler = this.mHandler;
            if (cpuFeatureAmsCommunicatorHandler != null) {
                cpuFeatureAmsCommunicatorHandler.removeMessages(CpuFeature.MSG_UNIPERF_BOOST_OFF_SEND);
                this.mHandler.sendEmptyMessageDelayed(CpuFeature.MSG_UNIPERF_BOOST_OFF_SEND, (long) this.mDelayOffTime);
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
