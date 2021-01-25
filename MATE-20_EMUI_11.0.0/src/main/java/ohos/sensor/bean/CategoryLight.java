package ohos.sensor.bean;

public final class CategoryLight extends SensorBase {
    public static final int SENSOR_TYPE_AMBIENT_LIGHT = 2;
    public static final int SENSOR_TYPE_COLOR_RGB = 4;
    public static final int SENSOR_TYPE_COLOR_TEMPERATURE = 3;
    public static final int SENSOR_TYPE_COLOR_XYZ = 5;
    public static final int SENSOR_TYPE_PROXIMITY = 0;
    public static final int SENSOR_TYPE_TOF = 1;
    public static final String STRING_SENSOR_TYPE_AMBIENT_LIGHT = "ohos.sensor.type.ambient_light";
    public static final String STRING_SENSOR_TYPE_COLOR_RGB = "ohos.sensor.type.color_rgb";
    public static final String STRING_SENSOR_TYPE_COLOR_TEMPERATURE = "ohos.sensor.type.color_temperature";
    public static final String STRING_SENSOR_TYPE_COLOR_XYZ = "ohos.sensor.type.color_xyz";
    public static final String STRING_SENSOR_TYPE_PROXIMITY = "ohos.sensor.type.proximity";
    public static final String STRING_SENSOR_TYPE_TOF = "ohos.sensor.type.tof";

    public CategoryLight(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        super(i, str, str2, i2, f, f2, i3, i4, j, j2);
    }
}
