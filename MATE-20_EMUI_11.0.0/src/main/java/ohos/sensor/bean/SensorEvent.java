package ohos.sensor.bean;

public class SensorEvent {
    private int accuracy;
    private float[] data;
    private int flags;
    private int[] reserved;
    private int sensorId;
    private long timestamp;
    private int version;

    public SensorEvent(float[] fArr, int[] iArr) {
        this.data = (float[]) fArr.clone();
        this.reserved = (int[]) iArr.clone();
    }

    public int getSensorId() {
        return this.sensorId;
    }

    public void setSensorId(int i) {
        this.sensorId = i;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long j) {
        this.timestamp = j;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int i) {
        this.flags = i;
    }

    public int getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(int i) {
        this.accuracy = i;
    }

    public float[] getData() {
        return (float[]) this.data.clone();
    }

    public void setData(float[] fArr) {
        this.data = (float[]) fArr.clone();
    }

    public int[] getReserved() {
        return (int[]) this.reserved.clone();
    }

    public void setReserved(int[] iArr) {
        this.reserved = (int[]) iArr.clone();
    }
}
