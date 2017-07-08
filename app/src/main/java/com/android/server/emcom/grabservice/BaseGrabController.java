package com.android.server.emcom.grabservice;

import android.view.accessibility.AccessibilityEvent;
import com.android.server.emcom.grabservice.AutoGrabService.AccessibilityEventCallback;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.com.android.server.policy.HwGlobalActionsData;

public class BaseGrabController implements AccessibilityEventCallback {
    public static final int CMD_CANCLE = 4;
    public static final int CMD_GRAB = 6;
    private final String packageName;

    public BaseGrabController(String pkgName) {
        this.packageName = pkgName;
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() != null && event.getPackageName().toString().equals(this.packageName)) {
            switch (event.getEventType()) {
                case HwSecDiagnoseConstant.BIT_SYSMOUNT /*32*/:
                case HwGlobalActionsData.FLAG_SILENTMODE_TRANSITING /*2048*/:
                    handleWinStateChangeEvent(event);
                    break;
                case HwSecDiagnoseConstant.BIT_ADBD /*64*/:
                    handleNotifyEvent(event);
                    break;
            }
        }
    }

    protected void handleWinStateChangeEvent(AccessibilityEvent event) {
    }

    protected void handleNotifyEvent(AccessibilityEvent event) {
    }

    public void executeCommand(int cmdId, int notifyId) {
    }

    public void handleTimeout(int appId, int state) {
    }

    public void parseGrabParams(String params) {
    }
}
