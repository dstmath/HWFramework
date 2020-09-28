package com.huawei.dmsdp.devicevirtualization;

import com.huawei.android.hwpartdevicevirtualization.BuildConfig;

public class VirtualSensor {
    public static final String STRING_TYPE_HEART_RATE = "huawei.dmsdp.sensor.heart_rate";
    public static final String STRING_TYPE_PPG = "huawei.dmsdp.sensor.ppg";
    public static final int TYPE_HEART_RATE = 1;
    public static final int TYPE_HEART_RATE_PPG = 2;
    private String mDeviceId;
    private int mSensorId;
    private int mSensorType;
    private String mSensorTypeString;

    VirtualSensor(String deviceId, int sensorId, int sensorType) {
        this.mDeviceId = deviceId;
        this.mSensorId = sensorId;
        this.mSensorType = sensorType;
        if (sensorType == 1) {
            this.mSensorTypeString = "huawei.dmsdp.sensor.heart_rate";
        } else if (sensorType != 2) {
            this.mSensorTypeString = BuildConfig.FLAVOR;
        } else {
            this.mSensorTypeString = "huawei.dmsdp.sensor.ppg";
        }
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public int getSensorId() {
        return this.mSensorId;
    }

    public int getSensorType() {
        return this.mSensorType;
    }

    public String getSensorTypeString() {
        return this.mSensorTypeString;
    }
}
