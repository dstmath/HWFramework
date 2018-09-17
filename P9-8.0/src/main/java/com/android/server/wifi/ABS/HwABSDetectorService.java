package com.android.server.wifi.ABS;

import android.content.Context;
import com.android.server.wifi.WifiStateMachine;

public class HwABSDetectorService {
    private static HwABSDetectorService mHwABSDetectorService = null;
    private HwABSStateMachine mHwABSStateMachine = null;

    public HwABSDetectorService(Context context, WifiStateMachine wifiStateMachine) {
        HwABSUtils.logE("init HwABSScenarioDetectorService");
        this.mHwABSStateMachine = HwABSStateMachine.createHwABSStateMachine(context, wifiStateMachine);
        this.mHwABSStateMachine.onStart();
    }

    public static HwABSDetectorService createHwABSDetectorService(Context context, WifiStateMachine wifiStateMachine) {
        if (mHwABSDetectorService == null) {
            mHwABSDetectorService = new HwABSDetectorService(context, wifiStateMachine);
        }
        return mHwABSDetectorService;
    }

    public static HwABSDetectorService getInstance() {
        return mHwABSDetectorService;
    }

    public boolean isABSSwitching() {
        HwABSUtils.logE("HwABSDetectorService isABSSwitching");
        return this.mHwABSStateMachine.isABSSwitching();
    }

    public void notifySelEngineEnableWiFi() {
        this.mHwABSStateMachine.notifySelEngineEnableWiFi();
    }

    public void notifySelEngineResetCompelete() {
        this.mHwABSStateMachine.notifySelEngineResetCompelete();
    }
}
