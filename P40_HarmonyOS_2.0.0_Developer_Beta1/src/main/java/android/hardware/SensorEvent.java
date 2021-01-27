package android.hardware;

import android.annotation.UnsupportedAppUsage;

public class SensorEvent {
    public int accuracy;
    public Sensor sensor;
    public long timestamp;
    public final float[] values;

    @UnsupportedAppUsage
    SensorEvent(int valueSize) {
        this.values = new float[valueSize];
    }
}
