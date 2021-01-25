package com.huawei.dmsdp.devicevirtualization;

public class VirtualSensorData {
    private int mAccuracy;
    private VirtualSensor mSensor;
    private long mTimestamp;
    private final float[] mValues;

    VirtualSensorData(int valueSize) {
        this.mValues = new float[valueSize];
    }

    VirtualSensorData(VirtualSensor sensor, int accuracy, long timestamp, float[] values) {
        this.mValues = values;
        this.mSensor = sensor;
        this.mAccuracy = accuracy;
        this.mTimestamp = timestamp;
    }

    public float[] getValues() {
        return (float[]) this.mValues.clone();
    }

    public VirtualSensor getSensor() {
        return this.mSensor;
    }

    public int getAccuracy() {
        return this.mAccuracy;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }
}
