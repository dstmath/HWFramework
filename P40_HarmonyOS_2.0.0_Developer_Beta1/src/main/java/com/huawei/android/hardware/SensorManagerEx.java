package com.huawei.android.hardware;

import android.hardware.SensorManager;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SensorManagerEx {
    private static final int OP_FAIL = -1;
    private static final String TAG = "SensorManagerEx";

    public static int hwSetSensorConfig(SensorManager sensorManager, String config) {
        if (sensorManager != null) {
            return sensorManager.hwSetSensorConfig(config);
        }
        Log.e(TAG, "hwSetSensorConfig: sensorManager is null!");
        return -1;
    }
}
