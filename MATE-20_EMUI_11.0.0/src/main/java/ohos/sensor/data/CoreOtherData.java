package ohos.sensor.data;

import ohos.sensor.bean.CoreOther;

public class CoreOtherData extends CoreSensorData<CoreOther> {
    private static final int[] SENSOR_OTHER_DIMS = {1, 1, 1, 1};
    private CoreOther sensor;

    public CoreOtherData(CoreOther coreOther, long j, float[] fArr, int i, boolean z) {
        if (coreOther != null) {
            this.sensor = coreOther;
            this.timestamp = j;
            int parserType = parserType(coreOther.getSensorId());
            int[] iArr = SENSOR_OTHER_DIMS;
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
    public CoreOther getSensor() {
        CoreOther coreOther = this.sensor;
        if (coreOther != null) {
            return coreOther;
        }
        return new CoreOther();
    }
}
