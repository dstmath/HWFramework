package ohos.sensor.manager;

import java.util.List;
import ohos.sensor.bean.SensorBean;
import ohos.sensor.listener.CoreSensorDataCallback;

public class SensorCore {
    private static volatile SensorCore instance;

    private SensorCore() {
    }

    public static SensorCore getInstance() {
        if (instance == null) {
            synchronized (SensorCore.class) {
                if (instance == null) {
                    instance = new SensorCore();
                }
            }
        }
        return instance;
    }

    public List<SensorBean> getAllSensors(int i) {
        return SubscribeManager.getInstance().getAllSensorsWithCategory(i);
    }

    public List<SensorBean> getAllSensors(int i, int i2) {
        return SubscribeManager.getInstance().getAllSensorsWithType(i, i2);
    }

    public SensorBean getSingleSensor(int i, int i2) {
        return SubscribeManager.getInstance().getSingleSensorWithType(i, i2);
    }

    public boolean setSensorDataCallback(CoreSensorDataCallback coreSensorDataCallback, SensorBean sensorBean, long j, long j2) {
        return SubscribeManager.getInstance().setSensorDataCallback(coreSensorDataCallback, sensorBean, j, j2);
    }

    public boolean releaseSensorDataCallback(CoreSensorDataCallback coreSensorDataCallback, SensorBean sensorBean) {
        return SubscribeManager.getInstance().releaseSensorDataCallback(coreSensorDataCallback, sensorBean);
    }

    public long getSensorMinSampleInterval(int i) {
        return SubscribeManager.getInstance().getSensorMinSampleInterval(i);
    }

    public int runCommand(int i, int i2, int i3) {
        return SubscribeManager.getInstance().runSensorCommand(i, i2, i3);
    }
}
