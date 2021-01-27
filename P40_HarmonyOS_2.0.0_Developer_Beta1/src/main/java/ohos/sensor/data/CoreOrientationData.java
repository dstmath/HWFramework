package ohos.sensor.data;

import ohos.sensor.bean.CoreOrientation;

public class CoreOrientationData extends CoreSensorData<CoreOrientation> {
    private static final int[] SENSOR_ORIENTATION_DIMS = {16, 3, 1, 3, 5, 4, 5};
    private CoreOrientation sensor;

    public CoreOrientationData(CoreOrientation coreOrientation, long j, float[] fArr, int i, boolean z) {
        if (coreOrientation != null) {
            this.sensor = coreOrientation;
            this.timestamp = j;
            int parserType = parserType(coreOrientation.getSensorId());
            int[] iArr = SENSOR_ORIENTATION_DIMS;
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
    public CoreOrientation getSensor() {
        CoreOrientation coreOrientation = this.sensor;
        if (coreOrientation != null) {
            return coreOrientation;
        }
        return new CoreOrientation();
    }
}
