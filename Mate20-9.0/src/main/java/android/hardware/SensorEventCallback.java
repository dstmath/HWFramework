package android.hardware;

public abstract class SensorEventCallback implements SensorEventListener2 {
    public void onSensorChanged(SensorEvent event) {
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onFlushCompleted(Sensor sensor) {
    }

    public void onSensorAdditionalInfo(SensorAdditionalInfo info) {
    }
}
