package ohos.sensor.data;

import java.math.BigDecimal;
import ohos.sensor.bean.CategoryEnvironment;

public class CategoryEnvironmentData extends SensorData<CategoryEnvironment> {
    private static final int SCALE_BIGDECIMAL = 16;
    public static final float SEA_PRESSURE = 1013.25f;
    private static final int[] SENSOR_DATA_DIMS = {1, 3, 6, 1, 1, 1};
    private static final float ZERO_PRESSURE_ALTITUDE = 44330.0f;

    public CategoryEnvironmentData(CategoryEnvironment categoryEnvironment, int i, long j, int i2, float[] fArr) {
        super(categoryEnvironment, i, j, i2, fArr);
    }

    public static float getDeviceAltitude(float f, float f2) {
        return (1.0f - BigDecimal.valueOf(Math.pow((double) BigDecimal.valueOf((double) f2).divide(BigDecimal.valueOf((double) f), 16, 4).floatValue(), (double) BigDecimal.valueOf(1.0d).divide(BigDecimal.valueOf(5.255d), 16, 4).floatValue())).floatValue()) * ZERO_PRESSURE_ALTITUDE;
    }

    public static float getDeviceAltitude(float f) {
        return getDeviceAltitude(1013.25f, f);
    }
}
