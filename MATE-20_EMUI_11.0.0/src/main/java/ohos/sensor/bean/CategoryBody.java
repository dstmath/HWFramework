package ohos.sensor.bean;

public class CategoryBody extends SensorBase {
    public static final int SENSOR_TYPE_HEART_RATE = 0;
    public static final int SENSOR_TYPE_WEAR_DETECTION = 1;
    public static final String STRING_SENSOR_TYPE_HEART_RATE = "ohos.sensor.type.heart_rate";

    public CategoryBody(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        super(i, str, str2, i2, f, f2, i3, i4, j, j2);
    }
}
