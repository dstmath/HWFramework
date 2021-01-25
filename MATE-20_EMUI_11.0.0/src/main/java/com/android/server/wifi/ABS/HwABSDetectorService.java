package com.android.server.wifi.ABS;

import android.content.Context;
import com.android.server.wifi.ClientModeImpl;

public class HwABSDetectorService {
    private static HwABSDetectorService mHwABSDetectorService = null;
    private HwABSStateMachine mHwABSStateMachine = null;

    public HwABSDetectorService(Context context, ClientModeImpl wifiStateMachine) {
        HwABSUtils.logE(false, "init HwABSScenarioDetectorService", new Object[0]);
        this.mHwABSStateMachine = HwABSStateMachine.createHwABSStateMachine(context, wifiStateMachine);
        this.mHwABSStateMachine.onStart();
    }

    public static HwABSDetectorService createHwABSDetectorService(Context context, ClientModeImpl wifiStateMachine) {
        if (mHwABSDetectorService == null) {
            mHwABSDetectorService = new HwABSDetectorService(context, wifiStateMachine);
        }
        return mHwABSDetectorService;
    }

    public static HwABSDetectorService getInstance() {
        return mHwABSDetectorService;
    }

    public boolean isABSSwitching() {
        HwABSUtils.logE(false, "HwABSDetectorService isABSSwitching", new Object[0]);
        return this.mHwABSStateMachine.isABSSwitching();
    }

    public void notifySelEngineEnableWiFi() {
        this.mHwABSStateMachine.notifySelEngineEnableWiFi();
    }

    public void notifySelEngineResetCompelete() {
        this.mHwABSStateMachine.notifySelEngineResetCompelete();
    }

    public void puaseABSHandover() {
        this.mHwABSStateMachine.puaseABSHandover();
    }

    public void restartABSHandover() {
        this.mHwABSStateMachine.restartABSHandover();
    }
}
