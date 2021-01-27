package ohos.sensor.bean;

public class SensorBean {
    public static final int SENSOR_GROUP_MASK = -16777216;
    public static final int SENSOR_GROUP_MASK_SHIFT = 24;
    public static final int SENSOR_INDEX_MASK = 65280;
    public static final int SENSOR_INDEX_MASK_SHIFT = 8;
    public static final int SENSOR_TYPE_MASK = 16711680;
    public static final int SENSOR_TYPE_MASK_SHIFT = 16;
    private int cacheMaxCount;
    private int flags;
    private long maxInterval;
    private long minInterval;
    private String name;
    private float resolution;
    private int sensorId;
    private float upperRange;
    private String vendor;
    private int version;

    public SensorBean() {
        this(0, null, null, 0, 0.0f, 0.0f, 0, 0, 0, 0);
    }

    public SensorBean(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        this.sensorId = i;
        this.name = str;
        this.vendor = str2;
        this.version = i2;
        this.upperRange = f;
        this.resolution = f2;
        this.flags = i3;
        this.cacheMaxCount = i4;
        this.minInterval = j;
        this.maxInterval = j2;
    }

    public String getName() {
        return this.name;
    }

    public String getVendor() {
        return this.vendor;
    }

    public int getSensorId() {
        return this.sensorId;
    }

    public int getVersion() {
        return this.version;
    }

    public float getUpperRange() {
        return this.upperRange;
    }

    public float getResolution() {
        return this.resolution;
    }

    public int getCacheMaxCount() {
        return this.cacheMaxCount;
    }

    public long getMinInterval() {
        return this.minInterval;
    }

    public long getMaxInterval() {
        return this.maxInterval;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setSensorId(int i) {
        this.sensorId = i;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setVendor(String str) {
        this.vendor = str;
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public void setUpperRange(float f) {
        this.upperRange = f;
    }

    public void setResolution(float f) {
        this.resolution = f;
    }

    public void setFlags(int i) {
        this.flags = i;
    }

    public void setCacheMaxCount(int i) {
        this.cacheMaxCount = i;
    }

    public void setMinInterval(long j) {
        this.minInterval = j;
    }

    public void setMaxInterval(long j) {
        this.maxInterval = j;
    }
}
