package ohos.sensor.bean;

public final class CategoryOrientation extends SensorBase {
    public static final int SENSOR_TYPE_6DOF = 0;
    public static final int SENSOR_TYPE_DEVICE_ORIENTATION = 2;
    public static final int SENSOR_TYPE_GAME_ROTATION_VECTOR = 5;
    public static final int SENSOR_TYPE_GEOMAGNETIC_ROTATION_VECTOR = 6;
    public static final int SENSOR_TYPE_ORIENTATION = 3;
    public static final int SENSOR_TYPE_ROTATION_VECTOR = 4;
    public static final int SENSOR_TYPE_SCREEN_ROTATION = 1;
    public static final String STRING_SENSOR_TYPE_6DOF = "ohos.sensor.type.6dof";
    public static final String STRING_SENSOR_TYPE_DEVICE_ORIENTATION = "ohos.sensor.type.device_orientation";
    public static final String STRING_SENSOR_TYPE_GAME_ROTATION_VECTOR = "ohos.sensor.type.game_rotation_vector";
    public static final String STRING_SENSOR_TYPE_GEOMAGNETIC_ROTATION_VECTOR = "ohos.sensor.type.geomagnetic_rotation_vector";
    public static final String STRING_SENSOR_TYPE_ORIENTATION = "ohos.sensor.type.orientation";
    public static final String STRING_SENSOR_TYPE_ROTATION_VECTOR = "ohos.sensor.type.rotation_vector";
    public static final String STRING_SENSOR_TYPE_SCREEN_ROTATION = "ohos.sensor.type.screen_rotation";

    public CategoryOrientation(int i, String str, String str2, int i2, float f, float f2, int i3, int i4, long j, long j2) {
        super(i, str, str2, i2, f, f2, i3, i4, j, j2);
    }
}
