package android.hardware;

public final class TriggerEvent {
    public Sensor sensor;
    public long timestamp;
    public final float[] values;

    TriggerEvent(int size) {
        this.values = new float[size];
    }
}
