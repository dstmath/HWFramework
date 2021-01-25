package ohos.sensor.bean;

public final class CategoryEnvironment extends SensorBase {
    public static final int SENSOR_TYPE_AMBIENT_TEMPERATURE = 0;
    public static final int SENSOR_TYPE_BAROMETER = 4;
    public static final int SENSOR_TYPE_HUMIDITY = 3;
    public static final int SENSOR_TYPE_MAGNETIC_FIELD = 1;
    public static final int SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED = 2;
    public static final int SENSOR_TYPE_SAR = 5;
    public static final String STRING_SENSOR_TYPE_AMBIENT_TEMPERATURE = "ohos.sensor.type.ambient_temperature";
    public static final String STRING_SENSOR_TYPE_BAROMETER = "ohos.sensor.type.barometer";
    public static final String STRING_SENSOR_TYPE_HUMIDITY = "ohos.sensor.type.humidity";
    public static final String STRING_SENSOR_TYPE_MAGNETIC_FIELD = "ohos.sensor.type.magnetic_field";
    public static final String STRING_SENSOR_TYPE_MAGNETIC_UNCALIBRATED = "ohos.sensor.type.magnetic_uncalibrated";
    public static final String STRING_SENSOR_TYPE_SAR = "ohos.sensor.type.sar";

    public CategoryEnvironment(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        super(i, str, str2, i2, f, f2, i3, i4, j, j2);
    }
}
