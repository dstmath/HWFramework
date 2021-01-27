package ohos.sensor.data;

import ohos.sensor.bean.CoreBody;

public class CoreBodyData extends CoreSensorData<CoreBody> {
    private static final int[] SENSOR_BODY_DIMS = {2, 1};
    private CoreBody sensor;

    public CoreBodyData(CoreBody coreBody, long j, float[] fArr, int i, boolean z) {
        if (coreBody != null) {
            this.sensor = coreBody;
            this.timestamp = j;
            int parserType = parserType(coreBody.getSensorId());
            int[] iArr = SENSOR_BODY_DIMS;
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
    public CoreBody getSensor() {
        CoreBody coreBody = this.sensor;
        if (coreBody != null) {
            return coreBody;
        }
        return new CoreBody();
    }
}
