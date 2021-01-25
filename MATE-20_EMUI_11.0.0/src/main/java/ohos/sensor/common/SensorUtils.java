package ohos.sensor.common;

public class SensorUtils {
    public static int parseSensorCategory(int i) {
        return (i & -16777216) >> 24;
    }

    public static int parseSensorIndex(int i) {
        return (i & 65280) >> 8;
    }

    public static int parseSensorType(int i) {
        return (i & 16711680) >> 16;
    }

    private SensorUtils() {
    }
}
