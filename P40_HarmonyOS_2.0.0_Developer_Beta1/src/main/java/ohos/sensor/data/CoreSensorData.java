package ohos.sensor.data;

import ohos.sensor.bean.SensorBean;

public class CoreSensorData<S extends SensorBean> {
    static final int DEFAULT_SENSOR_DATA_DIMS = 16;
    public int accuracy;
    public S sensor;
    public int sensorDataDim;
    public long timestamp;
    public float[] values;

    /* access modifiers changed from: package-private */
    public int parserType(int i) {
        return (16711680 & i) >> 16;
    }

    public S getSensor() {
        return this.sensor;
    }

    public void setSensor(S s) {
        this.sensor = s;
    }

    public int getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(int i) {
        this.accuracy = i;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long j) {
        this.timestamp = j;
    }

    public int getSensorDataDim() {
        return this.sensorDataDim;
    }

    public void setSensorDataDim(int i) {
        this.sensorDataDim = i;
    }

    public float[] getValues() {
        return (float[]) this.values.clone();
    }
}
