package com.android.server;

import android.content.Context;
import android.hardware.health.V1_0.HealthInfo;

public abstract class AbsBatteryService extends SystemService {
    public AbsBatteryService(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
    }

    /* access modifiers changed from: protected */
    public void updateLight() {
    }

    /* access modifiers changed from: protected */
    public void newUpdateLightsLocked() {
    }

    /* access modifiers changed from: protected */
    public void playRing() {
    }

    /* access modifiers changed from: protected */
    public void stopRing() {
    }

    /* access modifiers changed from: protected */
    public void printBatteryLog(HealthInfo oldInfo, android.hardware.health.V2_0.HealthInfo newInfo, int oldPlugType, boolean updatesStopped) {
    }

    /* access modifiers changed from: protected */
    public int alterWirelessTxSwitchInternal(int status) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getWirelessTxSwitchInternal() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean supportWirelessTxChargeInternal() {
        return false;
    }
}
