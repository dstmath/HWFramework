package ohos.sensor.listener;

import ohos.sensor.bean.SensorBase;
import ohos.sensor.data.SensorData;

public interface ISensorDataCallback<D extends SensorData<S>, S extends SensorBase> {
    void onAccuracyDataModified(S s, int i);

    void onCommandCompleted(S s);

    void onSensorDataModified(D d);
}
