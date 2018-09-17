package com.android.server;

import android.content.Context;
import android.os.BatteryProperties;

public class HwCustBatteryService {
    public boolean mutePowerConnectedTone() {
        return false;
    }

    public boolean isBadBatteryWarning() {
        return false;
    }

    public void sendBadBatteryWarningNotification(Context context, BatteryProperties oldBatteryProps, BatteryProperties newBatteryProps) {
    }
}
