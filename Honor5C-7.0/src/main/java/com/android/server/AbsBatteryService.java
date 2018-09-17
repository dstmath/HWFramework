package com.android.server;

import android.content.Context;
import android.os.BatteryProperties;

public abstract class AbsBatteryService extends SystemService {
    public AbsBatteryService(Context context) {
        super(context);
    }

    protected void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
    }

    protected void updateLight() {
    }

    protected void newUpdateLightsLocked() {
    }

    protected void playRing() {
    }

    protected void stopRing() {
    }

    protected void printBatteryLog(BatteryProperties oldProps, BatteryProperties newProps, int oldPlugType, boolean updatesStopped) {
    }
}
