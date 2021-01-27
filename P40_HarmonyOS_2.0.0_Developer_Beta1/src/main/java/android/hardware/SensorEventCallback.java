package android.hardware;

public abstract class SensorEventCallback implements SensorEventListener2 {
    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent event) {
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override // android.hardware.SensorEventListener2
    public void onFlushCompleted(Sensor sensor) {
    }

    public void onSensorAdditionalInfo(SensorAdditionalInfo info) {
    }
}
