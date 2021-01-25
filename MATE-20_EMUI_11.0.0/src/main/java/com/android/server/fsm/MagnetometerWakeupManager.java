package com.android.server.fsm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Slog;
import com.android.server.LocalServices;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;

public final class MagnetometerWakeupManager {
    private static final int EXT_HALL_DATA_LENGTH = 2;
    private static final int HALL_FOLDED_THRESHOLD = 0;
    private static final int HALL_THRESHOLD = 1;
    private static final int LOW_TEMP_VALUE_INDEX = 2;
    private static final int LOW_TEMP_VALUE_LENGTH = 3;
    private static final int MAGN_HALL_TYPE = 2;
    private static final int SENSOR_RATE = 100000;
    private static final int SENSOR_TYPE_HALL = 65557;
    private static final String TAG = "Fsm_MagnetometerWakeupManager";
    private static MagnetometerWakeupManager sInstance = null;
    private Context mContext;
    private HwFoldScreenManagerInternal mFoldScreenManagerService;
    private int mHallData = 1;
    private SensorEventListener mMagnetometerListener = new SensorEventListener() {
        /* class com.android.server.fsm.MagnetometerWakeupManager.AnonymousClass1 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != MagnetometerWakeupManager.SENSOR_TYPE_HALL) {
                Slog.e("Fsm_MagnetometerWakeupManager", "Type is not Hall Sensor");
            } else if (event.values.length < 2) {
                Slog.e("Fsm_MagnetometerWakeupManager", "event value lenght is less than 2");
            } else {
                int temDate = (int) event.values[1];
                if (MagnetometerWakeupManager.this.mHallData == temDate) {
                    Slog.e("Fsm_MagnetometerWakeupManager", "mHall data don't change");
                    return;
                }
                MagnetometerWakeupManager.this.mHallData = temDate;
                if (MagnetometerWakeupManager.this.mPowerManager == null) {
                    MagnetometerWakeupManager magnetometerWakeupManager = MagnetometerWakeupManager.this;
                    magnetometerWakeupManager.mPowerManager = (PowerManager) magnetometerWakeupManager.mContext.getSystemService("power");
                }
                if (!MagnetometerWakeupManager.this.mPowerManager.isScreenOn()) {
                    int type = (int) event.values[0];
                    if (type == 2 && MagnetometerWakeupManager.this.mHallData == 1) {
                        if (MagnetometerWakeupManager.this.mFoldScreenManagerService == null) {
                            MagnetometerWakeupManager.this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                        }
                        MagnetometerWakeupManager.this.mFoldScreenManagerService.prepareWakeup(4);
                    }
                    if (type == 2 && MagnetometerWakeupManager.this.mHallData == 0) {
                        Slog.d("Fsm_MagnetometerWakeupManager", "handleDrawWindow to folded status");
                        if (MagnetometerWakeupManager.this.mFoldScreenManagerService == null) {
                            MagnetometerWakeupManager.this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                        }
                        MagnetometerWakeupManager.this.mFoldScreenManagerService.handleDrawWindow();
                    }
                    if (event.values.length >= 3) {
                        int lowTempWarningValue = (int) event.values[2];
                        Slog.i("Fsm_MagnetometerWakeupManager", "hall changed, lowTempWarningValue : " + lowTempWarningValue);
                        if (MagnetometerWakeupManager.this.mFoldScreenManagerService == null) {
                            MagnetometerWakeupManager.this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                        }
                        MagnetometerWakeupManager.this.mFoldScreenManagerService.notifyLowTempWarning(lowTempWarningValue);
                    }
                }
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private PowerManager mPowerManager;

    private MagnetometerWakeupManager(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    public static synchronized MagnetometerWakeupManager getInstance(Context context) {
        MagnetometerWakeupManager magnetometerWakeupManager;
        synchronized (MagnetometerWakeupManager.class) {
            if (sInstance == null) {
                sInstance = new MagnetometerWakeupManager(context);
            }
            magnetometerWakeupManager = sInstance;
        }
        return magnetometerWakeupManager;
    }

    /* access modifiers changed from: protected */
    public int getHallData() {
        return this.mHallData;
    }

    /* access modifiers changed from: protected */
    public void initSensorListener() {
        SensorManager mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (mSensorManager == null) {
            Slog.e("Fsm_MagnetometerWakeupManager", "connect SENSOR_SERVICE fail");
            return;
        }
        boolean isRegisted = mSensorManager.registerListener(this.mMagnetometerListener, mSensorManager.getDefaultSensor(SENSOR_TYPE_HALL), 100000);
        Slog.d("Fsm_MagnetometerWakeupManager", "register Hall Sensor result:" + isRegisted);
    }
}
