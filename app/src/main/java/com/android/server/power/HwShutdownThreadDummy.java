package com.android.server.power;

import android.content.Context;

public class HwShutdownThreadDummy implements IHwShutdownThread {
    public boolean isDoShutdownAnimation() {
        return false;
    }

    public void waitShutdownAnimation() {
    }

    public boolean needRebootDialog(String rebootReason, Context context) {
        return false;
    }

    public boolean needRebootProgressDialog(boolean reboot, Context context) {
        return false;
    }

    public void resetValues() {
    }

    public void waitShutdownAnimationComplete(Context context, long shutDownBegin) {
    }
}
