package com.huawei.android.hardware;

import android.content.Context;
import android.hardware.SystemSensorManager;
import android.os.Looper;

public class SystemSensorManagerEx {
    private SystemSensorManager mSystemSensorManager = null;

    public SystemSensorManagerEx(Context context, Looper mainLooper) {
        this.mSystemSensorManager = new SystemSensorManager(context, mainLooper);
    }

    public boolean supportSensorFeature(int sensorFeature) {
        SystemSensorManager systemSensorManager = this.mSystemSensorManager;
        if (systemSensorManager != null) {
            return systemSensorManager.supportSensorFeature(sensorFeature);
        }
        return false;
    }
}
