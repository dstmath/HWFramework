package com.android.server.sensor;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Slog;

public class MagnBracketObserver {
    static final String ACTION_MAGN_BRACKET_ATTACH = "com.huawei.magnbracket.action.ATTACH";
    static final boolean DBG = true;
    static final int SENSOR_TYPE_MAGN_BRACKET = 10009;
    static final int STATE_ATTACH = 1;
    static final int STATE_DETACH = 0;
    static final String TAG = "MagnBracketObserver";
    private Context mContext;
    private final SensorEventListener mSensorEventListener;

    public static MagnBracketObserver getInstance(Context context) {
        return new MagnBracketObserver(context);
    }

    private MagnBracketObserver(Context context) {
        this.mContext = null;
        this.mSensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                int status = (int) event.values[MagnBracketObserver.STATE_DETACH];
                Slog.d(MagnBracketObserver.TAG, "onSensorChanged: status = " + status);
                if (ActivityManagerNative.isSystemReady()) {
                    if (status == MagnBracketObserver.STATE_ATTACH) {
                        Slog.d(MagnBracketObserver.TAG, "MAGN BRACKET sensor attached, notify app");
                        Intent intent = new Intent(MagnBracketObserver.ACTION_MAGN_BRACKET_ATTACH);
                        intent.addFlags(536870912);
                        MagnBracketObserver.this.mContext.sendBroadcast(intent, "huawei.permission.MAGN_BRACKET_SENSORS");
                    }
                    return;
                }
                Slog.w(MagnBracketObserver.TAG, "system not ready");
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mContext = context;
        init();
    }

    private void init() {
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        Sensor sensor = sensorManager.getDefaultSensor(SENSOR_TYPE_MAGN_BRACKET, DBG);
        Slog.d(TAG, "sensor = " + sensor);
        if (sensor != null) {
            sensorManager.registerListener(this.mSensorEventListener, sensor, 3);
        } else {
            Slog.e(TAG, "Phone don't support MAGN BRACKET sensor!");
        }
    }
}
