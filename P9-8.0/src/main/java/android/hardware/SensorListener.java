package android.hardware;

@Deprecated
public interface SensorListener {
    void onAccuracyChanged(int i, int i2);

    void onSensorChanged(int i, float[] fArr);
}
