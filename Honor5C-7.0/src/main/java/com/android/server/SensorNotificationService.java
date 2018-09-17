package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.UserHandle;
import android.util.Slog;

public class SensorNotificationService extends SystemService implements SensorEventListener {
    private static final boolean DBG = true;
    private static final String TAG = "SensorNotificationService";
    private Context mContext;
    private Sensor mMetaSensor;
    private SensorManager mSensorManager;

    public SensorNotificationService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        LocalServices.addService(SensorNotificationService.class, this);
    }

    public void onBootPhase(int phase) {
        if (phase == NetdResponseCode.InterfaceChange) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.mMetaSensor = this.mSensorManager.getDefaultSensor(32);
            if (this.mMetaSensor == null) {
                Slog.d(TAG, "Cannot obtain dynamic meta sensor, not supported.");
            } else {
                this.mSensorManager.registerListener(this, this.mMetaSensor, 0);
            }
        }
    }

    private void broadcastDynamicSensorChanged() {
        Intent i = new Intent("android.intent.action.DYNAMIC_SENSOR_CHANGED");
        i.setFlags(1073741824);
        this.mContext.sendBroadcastAsUser(i, UserHandle.ALL);
        Slog.d(TAG, "DYNS sent dynamic sensor broadcast");
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == this.mMetaSensor) {
            broadcastDynamicSensorChanged();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
