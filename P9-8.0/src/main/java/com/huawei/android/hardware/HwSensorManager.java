package com.huawei.android.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HwSensorManager implements SensorEventListener {
    private static final int ACCELERATE_SPEED_THRESHOLD = 14;
    private static final boolean DEBUG = false;
    private static final int MESSAGE_SWING = 1;
    private static final float NANON_TO_SECOND_UNITs = 1.0E-9f;
    public static final int SENSOR_CHANGE_DIRECTION_TO_BOTTOM = 4;
    public static final int SENSOR_CHANGE_DIRECTION_TO_LEFT = 1;
    public static final int SENSOR_CHANGE_DIRECTION_TO_RIGHT = 2;
    public static final int SENSOR_CHANGE_DIRECTION_TO_TOP = 3;
    private static final float SENSOR_CORRENT_THRESHOLD = 0.1f;
    private static final String TAG = "HwSensorManager";
    public static final int TYPE_DIRECTION_CHANGED_WITH_ROTATION = 1;
    public static final int TYPE_DIRECTION_HORIZONTAL_WITH_ROTATION = 4;
    public static final int TYPE_DIRECTION_VERTICAL_WITH_ROTATION = 2;
    public static final int TYPE_MAX = 32;
    public static final int TYPE_MAX_GYRO = 16;
    public static final int TYPE_SWING = 16;
    public static final int TYPE_TILT_TO_MOVE = 8;
    private static final String VERSION = "1.0.0";
    private float mAngleDeltaX;
    private float mAngleDeltaY;
    private float mAngleRevisedY;
    private float mAngleX;
    private float mAngleY;
    private float mAngularSpeedThreshold;
    private int mDelayUs = 30000;
    private int mDirection = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (HwSensorManager.this.mSensorEventDetector != null) {
                        HwSensorManager.this.mSensorEventDetector.onSwing();
                    }
                    HwSensorManager.this.resetSensorData();
                    return;
                default:
                    return;
            }
        }
    };
    SensorEventDetector mSensorEventDetector;
    private SensorManager mSensorManager;
    private int mSwingDelay = 600;
    private float mSwingThreshold;
    private long mTimeStamp;
    private int mType;

    public interface SensorEventDetector {
        void onCorrect();

        void onDirectionChanged(int i);

        void onSwing();

        void onTiltToMove(float f, float f2);
    }

    static {
        Log.d(TAG, "HwSensorManager version: 1.0.0");
    }

    public HwSensorManager(Context context) {
        if (context == null) {
            Log.w(TAG, "SensorDataManager() context is null");
            return;
        }
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        if (this.mSensorManager == null) {
            Log.w(TAG, "Cannot get system service : sensor");
            return;
        }
        this.mAngularSpeedThreshold = 3.1415927f;
        this.mSwingThreshold = 14.0f;
    }

    public boolean registerSensorListener(int sensorType) {
        return registerSensorListener(sensorType, this.mDelayUs);
    }

    public boolean registerSensorListener(int sensorType, int rateUs) {
        boolean isRegistered = false;
        boolean swingEventRegistered = false;
        if (isValid(sensorType) && (sensorType & 16) != 0) {
            isRegistered = registerSystemSensor(rateUs, 1);
            swingEventRegistered = true;
        }
        if (isValid(sensorType) && sensorType < 16) {
            resetSensorData();
            this.mTimeStamp = 0;
            isRegistered = (!swingEventRegistered || isRegistered) ? registerSystemSensor(rateUs, 4) : false;
        }
        this.mDelayUs = getDefaultDelay(rateUs);
        this.mType |= sensorType;
        return isRegistered;
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case 1:
                float[] values = event.values;
                boolean isSwing = false;
                if (Math.abs(values[0]) > this.mSwingThreshold || Math.abs(values[1]) > this.mSwingThreshold || Math.abs(values[2]) > this.mSwingThreshold) {
                    isSwing = true;
                }
                if (isSwing && this.mSensorEventDetector != null && this.mHandler != null) {
                    this.mHandler.removeMessages(1);
                    this.mHandler.sendEmptyMessageDelayed(1, (long) this.mSwingDelay);
                    return;
                }
                return;
            case 4:
                float gyro_wx = event.values[0];
                float gyro_wy = event.values[1];
                float gyro_wz = event.values[2];
                if (this.mTimeStamp != 0) {
                    float dT = (float) (event.timestamp - this.mTimeStamp);
                    this.mAngleDeltaY = (gyro_wy * dT) * NANON_TO_SECOND_UNITs;
                    this.mAngleDeltaX = (gyro_wx * dT) * NANON_TO_SECOND_UNITs;
                }
                this.mTimeStamp = event.timestamp;
                if (Math.abs(gyro_wy) >= SENSOR_CORRENT_THRESHOLD || Math.abs(gyro_wx) >= SENSOR_CORRENT_THRESHOLD || Math.abs(gyro_wz) >= SENSOR_CORRENT_THRESHOLD || this.mSensorEventDetector == null) {
                    boolean directionChanged;
                    this.mAngleY += this.mAngleDeltaY;
                    this.mAngleX += this.mAngleDeltaX;
                    this.mAngleY = getAngleLagerThanPI(this.mAngleY);
                    this.mAngleX = getAngleLagerThanPI(this.mAngleX);
                    float angle_y = reviseAngleY(this.mAngleY, this.mAngleX);
                    if (!((this.mType & 8) == 0 || this.mSensorEventDetector == null)) {
                        this.mSensorEventDetector.onTiltToMove(this.mAngleDeltaY, this.mAngleDeltaX);
                    }
                    if ((this.mType & 4) != 0) {
                        gyro_wx = 0.0f;
                    } else if ((this.mType & 2) != 0) {
                        gyro_wy = 0.0f;
                    }
                    if (this.mHandler == null || (this.mHandler.hasMessages(1) ^ 1) == 0) {
                        directionChanged = false;
                    } else {
                        directionChanged = getDirectionChanged(gyro_wy, gyro_wx, angle_y, this.mAngleX);
                    }
                    if (directionChanged && this.mSensorEventDetector != null) {
                        this.mSensorEventDetector.onDirectionChanged(this.mDirection);
                        resetSensorData();
                        return;
                    }
                    return;
                }
                this.mSensorEventDetector.onCorrect();
                resetSensorData();
                return;
            default:
                return;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getSensorDelay() {
        return this.mDelayUs;
    }

    public void setRotationAngle(float angle) {
    }

    public void setAngularSpeedThreshold(float w) {
        this.mAngularSpeedThreshold = w;
    }

    public void setSwingThreshold(float speed) {
        this.mSwingThreshold = speed;
    }

    public void setSensorListener(SensorEventDetector detector) {
        this.mSensorEventDetector = detector;
    }

    public void setSwingDelay(int mS) {
        this.mSwingDelay = mS;
    }

    public void setMoveDirection(int direction) {
        this.mDirection = direction;
    }

    public void unRegisterListeners() {
        this.mType = 0;
        resetSensorData();
        this.mTimeStamp = 0;
        this.mHandler.removeCallbacksAndMessages(null);
        this.mSensorManager.unregisterListener(this);
    }

    public void unRegisterListener(int sensorType) {
        if (isValid(sensorType)) {
            this.mType &= ~sensorType;
            resetSensorData();
            this.mTimeStamp = 0;
            this.mHandler.removeCallbacksAndMessages(null);
            if ((sensorType & 16) != 0) {
                this.mSensorManager.unregisterListener(this, this.mSensorManager.getDefaultSensor(1));
            } else if (sensorType < 16) {
                this.mSensorManager.unregisterListener(this, this.mSensorManager.getDefaultSensor(4));
            }
            return;
        }
        Log.w(TAG, "unRegisterListener type invalid : " + sensorType);
    }

    private boolean isValid(int sensorType) {
        return sensorType > 0 && sensorType < 32;
    }

    private boolean registerSystemSensor(int delayUs, int type) {
        Sensor sensor = this.mSensorManager.getDefaultSensor(type);
        if (sensor == null) {
            Log.w(TAG, "unspported system sensor type : " + type);
            return false;
        }
        boolean rt = this.mSensorManager.registerListener(this, sensor, delayUs);
        if (!rt) {
            Log.w(TAG, "registerListener fail : " + type);
        }
        return rt;
    }

    private int getDefaultDelay(int rate) {
        switch (rate) {
            case 0:
                return 0;
            case 1:
                return 20000;
            case 2:
                return 66667;
            case 3:
                return 200000;
            default:
                return rate;
        }
    }

    private boolean getDirectionChanged(float wy, float wx, float angley, float anglex) {
        int direction = 0;
        if (Math.abs(wy) > Math.abs(wx)) {
            if (wy > this.mAngularSpeedThreshold) {
                direction = 2;
            } else if (wy < (-this.mAngularSpeedThreshold)) {
                direction = 1;
            }
        } else if (wx > this.mAngularSpeedThreshold) {
            direction = 4;
        } else if (wx < (-this.mAngularSpeedThreshold)) {
            direction = 3;
        }
        if (direction == 0 || direction == this.mDirection) {
            return false;
        }
        this.mDirection = direction;
        return true;
    }

    private float reviseAngleY(float angley, float anglex) {
        if (Math.abs(Math.abs(anglex) - 1.5707964f) >= 0.17453294f) {
            this.mAngleRevisedY = angley;
        }
        return this.mAngleRevisedY;
    }

    private float getAngleLagerThanPI(float angle) {
        float temp = angle;
        if (((double) angle) > 3.141592653589793d) {
            return 0.0f;
        }
        if (((double) angle) <= -3.141592653589793d) {
            return 0.0f;
        }
        return temp;
    }

    private void resetSensorData() {
        this.mAngleDeltaY = 0.0f;
        this.mAngleDeltaX = 0.0f;
        this.mAngleRevisedY = 0.0f;
        this.mAngleX = 0.0f;
        this.mAngleY = 0.0f;
        this.mDirection = 0;
    }
}
