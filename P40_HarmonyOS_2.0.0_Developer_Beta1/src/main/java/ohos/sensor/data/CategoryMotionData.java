package ohos.sensor.data;

import ohos.sensor.bean.CategoryMotion;

public class CategoryMotionData extends SensorData<CategoryMotion> {
    private static final int[] SENSOR_DATA_DIMS = {3, 6, 3, 3, 3, 6, 1, 1};

    public CategoryMotionData(CategoryMotion categoryMotion, int i, long j, int i2, float[] fArr) {
        super(categoryMotion, i, j, i2, fArr);
    }
}
