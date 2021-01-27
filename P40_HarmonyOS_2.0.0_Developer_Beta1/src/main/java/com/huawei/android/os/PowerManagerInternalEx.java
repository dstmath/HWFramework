package com.huawei.android.os;

import android.os.PowerManager;
import android.os.PowerManagerInternal;
import com.android.server.LocalServices;

public class PowerManagerInternalEx {
    public static final int WAKEFULNESS_ASLEEP = 0;
    public static final int WAKEFULNESS_AWAKE = 1;
    public static final int WAKEFULNESS_DOZING = 3;
    private PowerManagerInternal mPowerManagerInternal = ((PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class));

    private PowerManagerInternalEx() {
    }

    public static PowerManagerInternalEx getInstance() {
        return new PowerManagerInternalEx();
    }

    public int getLastWakeupWakeReason() {
        PowerManager.WakeData lastWakeUp = this.mPowerManagerInternal.getLastWakeup();
        if (lastWakeUp != null) {
            return lastWakeUp.wakeReason;
        }
        return 0;
    }
}
