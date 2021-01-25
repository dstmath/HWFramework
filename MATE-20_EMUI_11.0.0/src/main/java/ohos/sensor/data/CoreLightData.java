package ohos.sensor.data;

import ohos.sensor.bean.CoreLight;

public class CoreLightData extends CoreSensorData<CoreLight> {
    private static final int[] SENSOR_LIGHT_DIMS = {3, 6, 3, 1, 3, 3};
    private CoreLight sensor;

    public CoreLightData(CoreLight coreLight, long j, float[] fArr, int i, boolean z) {
        if (coreLight != null) {
            this.sensor = coreLight;
            this.timestamp = j;
            int parserType = parserType(coreLight.getSensorId());
            int[] iArr = SENSOR_LIGHT_DIMS;
            if (parserType < iArr.length) {
                this.values = new float[iArr[parserType]];
                this.sensorDataDim = iArr[parserType];
                System.arraycopy(fArr, 0, this.values, 0, this.values.length);
            } else {
                this.values = new float[16];
                this.sensorDataDim = 16;
                System.arraycopy(fArr, 0, this.values, 0, this.values.length);
            }
            if (z) {
                this.accuracy = i;
            }
        }
    }

    @Override // ohos.sensor.data.CoreSensorData
    public CoreLight getSensor() {
        CoreLight coreLight = this.sensor;
        if (coreLight != null) {
            return coreLight;
        }
        return new CoreLight();
    }
}
