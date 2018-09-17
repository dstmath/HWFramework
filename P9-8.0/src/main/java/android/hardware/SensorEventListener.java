package android.hardware;

public interface SensorEventListener {
    void onAccuracyChanged(Sensor sensor, int i);

    void onSensorChanged(SensorEvent sensorEvent);
}
