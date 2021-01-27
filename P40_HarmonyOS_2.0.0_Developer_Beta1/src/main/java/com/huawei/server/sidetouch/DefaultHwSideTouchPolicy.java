package com.huawei.server.sidetouch;

import android.os.Bundle;
import android.view.KeyEvent;

public class DefaultHwSideTouchPolicy {
    private static DefaultHwSideTouchPolicy sInstance = null;

    public static synchronized DefaultHwSideTouchPolicy getInstance() {
        DefaultHwSideTouchPolicy defaultHwSideTouchPolicy;
        synchronized (DefaultHwSideTouchPolicy.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwSideTouchPolicy();
            }
            defaultHwSideTouchPolicy = sInstance;
        }
        return defaultHwSideTouchPolicy;
    }

    public void systemReady() {
    }

    public boolean interceptVolumeKey(KeyEvent event, boolean isInjected, boolean isScreenOn, int keyCode, boolean isDown) {
        return false;
    }

    public boolean shouldSendToSystemMediaSession(KeyEvent event, boolean isInjected, boolean isScreenOn, boolean keyguardActive) {
        return false;
    }

    public boolean isSideTouchEvent(KeyEvent event, boolean isInjected) {
        return false;
    }

    public void notifyVolumePanelStatus(boolean isVolumePanelVisible) {
    }

    public void screenTurnedOn() {
    }

    public void screenTurnedOff(boolean isProximityPositive) {
    }

    public boolean isTalkbackEnable() {
        return false;
    }

    public boolean isMusicOnly(boolean isScreenOn) {
        return true;
    }

    public void onRotationChanged(int rotation) {
    }

    public boolean checkVolumeTriggerStatusAndReset() {
        return false;
    }

    public int[] runSideTouchCommandByType(int type, Bundle bundle) {
        return null;
    }
}
