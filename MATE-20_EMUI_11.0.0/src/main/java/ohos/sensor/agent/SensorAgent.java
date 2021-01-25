package ohos.sensor.agent;

import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.sensor.bean.SensorBase;
import ohos.sensor.data.SensorData;
import ohos.sensor.listener.ISensorDataCallback;
import ohos.sensor.manager.SensorCore;

public abstract class SensorAgent<S extends SensorBase, D extends SensorData<S>, L extends ISensorDataCallback<D, S>> {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "SensorAgent");
    public static final int SENSOR_CATEGORY_BODY = 5;
    public static final int SENSOR_CATEGORY_DEVICEMOTION = 0;
    public static final int SENSOR_CATEGORY_ENVIRONMENT = 1;
    public static final int SENSOR_CATEGORY_LIGHT = 3;
    public static final int SENSOR_CATEGORY_ORIENTATION = 2;
    public static final int SENSOR_CATEGORY_OTHER = 4;
    public static final int SENSOR_SAMPLING_RATE_FASTEST = 0;
    public static final int SENSOR_SAMPLING_RATE_GAME = 1;
    public static final int SENSOR_SAMPLING_RATE_NORMAL = 3;
    public static final int SENSOR_SAMPLING_RATE_UI = 2;
    public static final String STRING_SENSOR_CATEGORY_BODY = "ohos.sensor.category.body";
    public static final String STRING_SENSOR_CATEGORY_DEVICEMOTION = "ohos.sensor.category.devicemotion";
    public static final String STRING_SENSOR_CATEGORY_ENVIRONMENT = "ohos.sensor.category.environment";
    public static final String STRING_SENSOR_CATEGORY_LIGHT = "ohos.sensor.category.light";
    public static final String STRING_SENSOR_CATEGORY_ORIENTATION = "ohos.sensor.category.orientation";
    public static final String STRING_SENSOR_CATEGORY_OTHER = "ohos.sensor.category.other";

    public abstract List<S> getAllSensors();

    public abstract List<S> getAllSensors(int i);

    public abstract S getSingleSensor(int i);

    public abstract boolean releaseSensorDataCallback(L l);

    public abstract boolean releaseSensorDataCallback(L l, S s);

    public abstract boolean setSensorDataCallback(L l, S s, long j, long j2);

    /* access modifiers changed from: protected */
    public boolean subscribeParamsCheck(L l, S s, long j, long j2) {
        if (l == null) {
            HiLog.error(LABEL, "setSensorDataCallback callback cannot be null", new Object[0]);
            return false;
        } else if (s == null) {
            HiLog.error(LABEL, "setSensorDataCallback sensor cannot be null", new Object[0]);
            return false;
        } else if (j < 0) {
            HiLog.error(LABEL, "setSensorDataCallback sampling interval cannot be less than zero", new Object[0]);
            return false;
        } else if (j2 >= 0) {
            return true;
        } else {
            HiLog.error(LABEL, "setSensorDataCallback report delay cannot be less than zero", new Object[0]);
            return false;
        }
    }

    public long getSensorMinSampleInterval(int i) {
        HiTraceId begin = HiTrace.begin("getSensorMinSamplePeriod", 1);
        long sensorMinSampleInterval = SensorCore.getInstance().getSensorMinSampleInterval(i);
        HiTrace.end(begin);
        return sensorMinSampleInterval;
    }

    public int runCommand(int i, int i2, int i3) {
        HiTraceId begin = HiTrace.begin("runCommand", 1);
        int runCommand = SensorCore.getInstance().runCommand(i, i2, i3);
        HiTrace.end(begin);
        return runCommand;
    }
}
