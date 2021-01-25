package ohos.sensor.data;

import ohos.sensor.bean.CoreMotion;

public class CoreMotionData extends CoreSensorData<CoreMotion> {
    private static final int[] SENSOR_MOTION_DIMS = {3, 6, 3, 3, 3, 6, 1, 1, 1, 1};
    private CoreMotion sensor;

    public CoreMotionData(CoreMotion coreMotion, long j, float[] fArr, int i, boolean z) {
        if (coreMotion != null) {
            this.sensor = coreMotion;
            this.timestamp = j;
            int parserType = parserType(coreMotion.getSensorId());
            int[] iArr = SENSOR_MOTION_DIMS;
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
    public CoreMotion getSensor() {
        CoreMotion coreMotion = this.sensor;
        if (coreMotion != null) {
            return coreMotion;
        }
        return new CoreMotion();
    }
}
