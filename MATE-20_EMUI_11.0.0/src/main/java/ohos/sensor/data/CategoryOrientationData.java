package ohos.sensor.data;

import ohos.sensor.bean.CategoryOrientation;

public class CategoryOrientationData extends SensorData<CategoryOrientation> {
    private static final int[] SENSOR_DATA_DIMS = {16, 3, 1, 3, 5, 4, 5};

    public CategoryOrientationData(CategoryOrientation categoryOrientation, int i, long j, int i2, float[] fArr) {
        super(categoryOrientation, i, j, i2, fArr);
    }
}
