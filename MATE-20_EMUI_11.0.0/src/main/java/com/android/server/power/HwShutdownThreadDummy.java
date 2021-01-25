package com.android.server.power;

import android.content.Context;

public class HwShutdownThreadDummy implements IHwShutdownThread {
    @Override // com.android.server.power.IHwShutdownThread
    public boolean isShutDownAnimationAvailable() {
        return false;
    }

    @Override // com.android.server.power.IHwShutdownThread
    public boolean isDoShutdownAnimation() {
        return false;
    }

    @Override // com.android.server.power.IHwShutdownThread
    public void waitShutdownAnimation() {
    }

    @Override // com.android.server.power.IHwShutdownThread
    public boolean needRebootDialog(String rebootReason, Context context) {
        return false;
    }

    @Override // com.android.server.power.IHwShutdownThread
    public boolean needRebootProgressDialog(boolean reboot, Context context) {
        return false;
    }

    @Override // com.android.server.power.IHwShutdownThread
    public void resetValues() {
    }

    @Override // com.android.server.power.IHwShutdownThread
    public void waitShutdownAnimationComplete(Context context, long shutDownBegin) {
    }

    @Override // com.android.server.power.IHwShutdownThread
    public void onEarlyShutdownBegin(boolean isReboot, boolean isRebootSafeMode, String reason) {
    }
}
