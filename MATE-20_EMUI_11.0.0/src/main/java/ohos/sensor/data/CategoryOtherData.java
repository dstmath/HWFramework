package ohos.sensor.data;

import ohos.sensor.bean.CategoryOther;

public class CategoryOtherData extends SensorData<CategoryOther> {
    private static final int[] SENSOR_DATA_DIMS = {1, 1, 1, 1};

    public CategoryOtherData(CategoryOther categoryOther, int i, long j, int i2, float[] fArr) {
        super(categoryOther, i, j, i2, fArr);
    }
}
