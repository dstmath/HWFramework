package com.android.server.wifi.ABS;

import android.content.Context;
import com.android.server.wifi.ClientModeImpl;

public class HwAbsDetectorService {
    private static HwAbsDetectorService sHwAbsDetectorService = null;
    private HwAbsStateMachine mHwAbsStateMachine = null;

    public HwAbsDetectorService(Context context, ClientModeImpl wifiStateMachine) {
        HwAbsUtils.logE(false, "init HwABSScenarioDetectorService", new Object[0]);
        this.mHwAbsStateMachine = HwAbsStateMachine.createHwAbsStateMachine(context, wifiStateMachine);
        this.mHwAbsStateMachine.onStart();
    }

    public static HwAbsDetectorService createHwAbsDetectorService(Context context, ClientModeImpl wifiStateMachine) {
        if (sHwAbsDetectorService == null) {
            sHwAbsDetectorService = new HwAbsDetectorService(context, wifiStateMachine);
        }
        return sHwAbsDetectorService;
    }

    public static HwAbsDetectorService getInstance() {
        return sHwAbsDetectorService;
    }

    public boolean isAbsSwitching() {
        HwAbsUtils.logE(false, "HwAbsDetectorService isAbsSwitching", new Object[0]);
        return this.mHwAbsStateMachine.isAbsSwitching();
    }

    public void notifySelEngineEnableWifi() {
        this.mHwAbsStateMachine.notifySelEngineEnableWifi();
    }

    public void notifySelEngineResetCompelete() {
        this.mHwAbsStateMachine.notifySelEngineResetCompelete();
    }

    public void pauseAbsHandover() {
        this.mHwAbsStateMachine.pauseAbsHandover();
    }

    public void restartAbsHandover() {
        this.mHwAbsStateMachine.restartAbsHandover();
    }
}
