package ohos.sensor.data;

import ohos.sensor.bean.SensorBase;

public class SensorData<S extends SensorBase> {
    protected static final int DEFAULT_SENSOR_DATA_DIMS = 16;
    public int accuracy;
    public S sensor;
    public int sensorDataDim;
    public long timestamp;
    public float[] values;

    public SensorData(S s, int i, long j, int i2, float[] fArr) {
        this.sensor = s;
        this.accuracy = i;
        this.timestamp = j;
        this.sensorDataDim = i2;
        this.values = (float[]) fArr.clone();
    }

    public S getSensor() {
        return this.sensor;
    }

    public int getAccuracy() {
        return this.accuracy;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getSensorDataDim() {
        return this.sensorDataDim;
    }

    public float[] getValues() {
        float[] fArr = this.values;
        if (fArr == null) {
            return new float[0];
        }
        return (float[]) fArr.clone();
    }
}
