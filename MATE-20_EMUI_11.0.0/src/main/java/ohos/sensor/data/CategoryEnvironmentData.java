package ohos.sensor.data;

import ohos.sensor.bean.CategoryEnvironment;

public class CategoryEnvironmentData extends SensorData<CategoryEnvironment> {
    private static final int[] SENSOR_DATA_DIMS = {1, 3, 6, 1, 1, 1};

    public CategoryEnvironmentData(CategoryEnvironment categoryEnvironment, int i, long j, int i2, float[] fArr) {
        super(categoryEnvironment, i, j, i2, fArr);
    }
}
