package com.android.internal.os;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryStats.Uid;
import android.util.SparseArray;
import java.util.List;

public class SensorPowerCalculator extends PowerCalculator {
    private final double mGpsPowerOn;
    private final List<Sensor> mSensors;

    public SensorPowerCalculator(PowerProfile profile, SensorManager sensorManager) {
        this.mSensors = sensorManager.getSensorList(-1);
        this.mGpsPowerOn = profile.getAveragePower(PowerProfile.POWER_GPS_ON);
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        SparseArray<? extends Uid.Sensor> sensorStats = u.getSensorStats();
        int NSE = sensorStats.size();
        for (int ise = 0; ise < NSE; ise++) {
            Uid.Sensor sensor = (Uid.Sensor) sensorStats.valueAt(ise);
            int sensorHandle = sensorStats.keyAt(ise);
            long sensorTime = sensor.getSensorTime().getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
            switch (sensorHandle) {
                case -10000:
                    app.gpsTimeMs = sensorTime;
                    app.gpsPowerMah = (((double) app.gpsTimeMs) * this.mGpsPowerOn) / 3600000.0d;
                    break;
                default:
                    int sensorsCount = this.mSensors.size();
                    for (int i = 0; i < sensorsCount; i++) {
                        Sensor s = (Sensor) this.mSensors.get(i);
                        if (s.getHandle() == sensorHandle) {
                            app.sensorPowerMah += (double) ((((float) sensorTime) * s.getPower()) / 3600000.0f);
                            break;
                        }
                    }
                    break;
            }
        }
    }
}
