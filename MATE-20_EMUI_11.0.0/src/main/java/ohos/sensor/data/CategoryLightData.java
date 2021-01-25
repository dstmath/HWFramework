package ohos.sensor.data;

import ohos.sensor.bean.CategoryLight;

public class CategoryLightData extends SensorData<CategoryLight> {
    private static final int[] SENSOR_DATA_DIMS = {3, 6, 3, 1, 3, 3};

    public CategoryLightData(CategoryLight categoryLight, int i, long j, int i2, float[] fArr) {
        super(categoryLight, i, j, i2, fArr);
    }
}
