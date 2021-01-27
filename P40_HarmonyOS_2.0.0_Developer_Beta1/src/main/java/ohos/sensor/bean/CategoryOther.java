package ohos.sensor.bean;

public final class CategoryOther extends SensorBase {
    public static final int SENSOR_TYPE_GRIP_DETECTOR = 1;
    public static final int SENSOR_TYPE_HALL = 0;
    public static final int SENSOR_TYPE_MAGNET_BRACKET = 2;
    public static final int SENSOR_TYPE_PRESSURE_DETECTOR = 3;
    public static final String STRING_SENSOR_TYPE_GRIP_DETECTOR = "ohos.sensor.type.grip_detector";
    public static final String STRING_SENSOR_TYPE_HALL = "ohos.sensor.type.hall";
    public static final String STRING_SENSOR_TYPE_MAGNET_BRACKET = "ohos.sensor.type.magnet_bracket";
    public static final String STRING_SENSOR_TYPE_PRESSURE_DETECTOR = "ohos.sensor.type.pressure_detector";

    public CategoryOther(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        super(i, str, str2, i2, f, f2, i3, i4, j, j2);
    }
}
