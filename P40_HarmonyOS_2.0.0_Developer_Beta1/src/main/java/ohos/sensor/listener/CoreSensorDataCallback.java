package ohos.sensor.listener;

import ohos.sensor.bean.SensorBean;
import ohos.sensor.data.CoreSensorData;

public interface CoreSensorDataCallback<D extends CoreSensorData<S>, S extends SensorBean> {
    void onAccuracyDataModified(S s, int i);

    void onCommandCompleted(S s);

    void onSensorDataModified(D d);
}
