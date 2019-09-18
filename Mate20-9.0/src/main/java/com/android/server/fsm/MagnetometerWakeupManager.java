package com.android.server.fsm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.LocalServices;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;

public final class MagnetometerWakeupManager extends WakeupManager {
    private static final int HALL_THRESHOLD = 1;
    private static final int MAGN_HALL_TYPE = 2;
    private static final int SENSOR_TYPE_HALL = 65557;
    private static final String TAG = "Fsm_MagnetometerWakeupManager";
    /* access modifiers changed from: private */
    public HwFoldScreenManagerInternal mFoldScreenManagerService;
    SensorEventListener mMagnetometerListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != MagnetometerWakeupManager.SENSOR_TYPE_HALL) {
                Slog.e("Fsm_MagnetometerWakeupManager", "Type is not Hall Sensor");
            } else if (event.values.length < 2) {
                Slog.e("Fsm_MagnetometerWakeupManager", "event value lenght is less than 2");
            } else if (!MagnetometerWakeupManager.this.mPowerManager.isScreenOn()) {
                int type = (int) event.values[0];
                int data = (int) event.values[1];
                if (type == 2 && data == 1) {
                    Bundle extra = new Bundle();
                    extra.putInt("uid", 1000);
                    extra.putString("mOpPackageName", MagnetometerWakeupManager.this.mContext.getOpPackageName());
                    extra.putString("reason", "magnetic.wakeUp");
                    if (MagnetometerWakeupManager.this.mFoldScreenManagerService == null) {
                        HwFoldScreenManagerInternal unused = MagnetometerWakeupManager.this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                    }
                    MagnetometerWakeupManager.this.mFoldScreenManagerService.prepareWakeup(4, extra);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public PowerManager mPowerManager = ((PowerManager) this.mContext.getSystemService("power"));
    private PowerManagerInternal mPowerManagerInternal;
    private SensorManager mSensorManager;

    MagnetometerWakeupManager(Context context) {
        super(context);
    }

    public void initSensorListener() {
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mSensorManager == null) {
            Slog.e("Fsm_MagnetometerWakeupManager", "connect SENSOR_SERVICE fail");
            return;
        }
        boolean ret = this.mSensorManager.registerListener(this.mMagnetometerListener, this.mSensorManager.getDefaultSensor(SENSOR_TYPE_HALL), 1);
        Slog.d("Fsm_MagnetometerWakeupManager", "register Hall Sensor result:" + ret);
    }

    public void wakeup() {
        Slog.d("Fsm_MagnetometerWakeupManager", "Wakeup in MagnetometerWakeupManager");
        if (this.mPowerManagerInternal == null) {
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }
        this.mPowerManagerInternal.powerWakeup(SystemClock.uptimeMillis(), this.mReason, this.mUid, this.mOpPackageName, this.mUid);
    }
}
