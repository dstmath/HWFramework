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
    private static final int DEFAULT_SENSOR_RATE = 30000;
    private static final int MESSAGE_SWING = 1;
    private static final float NANON_TO_SECOND_UNITS = 1.0E-9f;
    public static final int SENSOR_CHANGE_DIRECTION_TO_BOTTOM = 4;
    public static final int SENSOR_CHANGE_DIRECTION_TO_LEFT = 1;
    public static final int SENSOR_CHANGE_DIRECTION_TO_RIGHT = 2;
    public static final int SENSOR_CHANGE_DIRECTION_TO_TOP = 3;
    private static final float SENSOR_CORRENT_THRESHOLD = 0.1f;
    private static final int SWING_DELAY_TIME = 600;
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
    private int mDelayUs = DEFAULT_SENSOR_RATE;
    private int mDirection = 0;
    private Handler mHandler = new Handler() {
        /* class com.huawei.android.hardware.HwSensorManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (HwSensorManager.this.mSensorEventDetector != null) {
                    HwSensorManager.this.mSensorEventDetector.onSwing();
                }
                HwSensorManager.this.resetSensorData();
            }
        }
    };
    SensorEventDetector mSensorEventDetector;
    private SensorManager mSensorManager;
    private int mSwingDelay = SWING_DELAY_TIME;
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
        boolean isSwingEventRegistered = false;
        boolean z = true;
        if (isValid(sensorType) && (sensorType & 16) != 0) {
            isRegistered = registerSystemSensor(rateUs, 1);
            isSwingEventRegistered = true;
        }
        if (isValid(sensorType) && sensorType < 16) {
            resetSensorData();
            this.mTimeStamp = 0;
            boolean registered = registerSystemSensor(rateUs, 4);
            if (!isSwingEventRegistered) {
                z = registered;
            } else if (!isRegistered || !registered) {
                z = false;
            }
            isRegistered = z;
        }
        this.mDelayUs = getDefaultDelay(rateUs);
        this.mType |= sensorType;
        return isRegistered;
    }

    /* JADX INFO: Multiple debug info for r0v2 float[]: [D('gyroX' float), D('values' float[])] */
    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent event) {
        Handler handler;
        SensorEventDetector sensorEventDetector;
        SensorEventDetector sensorEventDetector2;
        SensorEventDetector sensorEventDetector3;
        int type = event.sensor.getType();
        boolean directionChanged = false;
        if (type == 1) {
            float[] values = event.values;
            boolean isSwing = false;
            if (Math.abs(values[0]) > this.mSwingThreshold || Math.abs(values[1]) > this.mSwingThreshold || Math.abs(values[2]) > this.mSwingThreshold) {
                isSwing = true;
            }
            if (isSwing && this.mSensorEventDetector != null && (handler = this.mHandler) != null) {
                handler.removeMessages(1);
                this.mHandler.sendEmptyMessageDelayed(1, (long) this.mSwingDelay);
            }
        } else if (type == 4) {
            float gyroX = event.values[0];
            float gyroY = event.values[1];
            float gyroZ = event.values[2];
            if (this.mTimeStamp != 0) {
                float dt = (float) (event.timestamp - this.mTimeStamp);
                this.mAngleDeltaY = gyroY * dt * NANON_TO_SECOND_UNITS;
                this.mAngleDeltaX = gyroX * dt * NANON_TO_SECOND_UNITS;
            }
            this.mTimeStamp = event.timestamp;
            if (Math.abs(gyroY) >= 0.1f || Math.abs(gyroX) >= 0.1f || Math.abs(gyroZ) >= 0.1f || (sensorEventDetector3 = this.mSensorEventDetector) == null) {
                this.mAngleY += this.mAngleDeltaY;
                this.mAngleX += this.mAngleDeltaX;
                this.mAngleY = getAngleLagerThanPi(this.mAngleY);
                this.mAngleX = getAngleLagerThanPi(this.mAngleX);
                float angleY = reviseAngleY(this.mAngleY, this.mAngleX);
                if (!((this.mType & 8) == 0 || (sensorEventDetector2 = this.mSensorEventDetector) == null)) {
                    sensorEventDetector2.onTiltToMove(this.mAngleDeltaY, this.mAngleDeltaX);
                }
                int i = this.mType;
                if ((i & 4) != 0) {
                    gyroX = 0.0f;
                } else if ((2 & i) != 0) {
                    gyroY = 0.0f;
                }
                Handler handler2 = this.mHandler;
                if (handler2 != null && !handler2.hasMessages(1) && getDirectionChanged(gyroY, gyroX, angleY, this.mAngleX)) {
                    directionChanged = true;
                }
                if (directionChanged && (sensorEventDetector = this.mSensorEventDetector) != null) {
                    sensorEventDetector.onDirectionChanged(this.mDirection);
                    resetSensorData();
                    return;
                }
                return;
            }
            sensorEventDetector3.onCorrect();
            resetSensorData();
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getSensorDelay() {
        return this.mDelayUs;
    }

    public void setRotationAngle(float angle) {
    }

    public void setAngularSpeedThreshold(float speed) {
        this.mAngularSpeedThreshold = speed;
    }

    public void setSwingThreshold(float speed) {
        this.mSwingThreshold = speed;
    }

    public void setSensorListener(SensorEventDetector detector) {
        this.mSensorEventDetector = detector;
    }

    public void setSwingDelay(int ms) {
        this.mSwingDelay = ms;
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
        if (!isValid(sensorType)) {
            Log.w(TAG, "unRegisterListener type invalid : " + sensorType);
            return;
        }
        this.mType &= ~sensorType;
        resetSensorData();
        this.mTimeStamp = 0;
        this.mHandler.removeCallbacksAndMessages(null);
        if ((sensorType & 16) != 0) {
            this.mSensorManager.unregisterListener(this, this.mSensorManager.getDefaultSensor(1));
        } else if (sensorType < 16) {
            this.mSensorManager.unregisterListener(this, this.mSensorManager.getDefaultSensor(4));
        }
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
        boolean isRegisted = this.mSensorManager.registerListener(this, sensor, delayUs);
        if (!isRegisted) {
            Log.w(TAG, "registerListener fail : " + type);
        }
        return isRegisted;
    }

    private int getDefaultDelay(int rate) {
        if (rate == 0) {
            return 0;
        }
        if (rate == 1) {
            return 20000;
        }
        if (rate == 2) {
            return 66667;
        }
        if (rate != 3) {
            return rate;
        }
        return 200000;
    }

    private boolean getDirectionChanged(float wy, float wx, float angley, float anglex) {
        int direction = 0;
        if (Math.abs(wy) > Math.abs(wx)) {
            float f = this.mAngularSpeedThreshold;
            if (wy > f) {
                direction = 2;
            } else if (wy < (-f)) {
                direction = 1;
            }
        } else {
            float f2 = this.mAngularSpeedThreshold;
            if (wx > f2) {
                direction = 4;
            } else if (wx < (-f2)) {
                direction = 3;
            }
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

    private float getAngleLagerThanPi(float angle) {
        if (((double) angle) <= 3.141592653589793d && ((double) angle) > -3.141592653589793d) {
            return angle;
        }
        return 0.0f;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetSensorData() {
        this.mAngleDeltaY = 0.0f;
        this.mAngleDeltaX = 0.0f;
        this.mAngleRevisedY = 0.0f;
        this.mAngleX = 0.0f;
        this.mAngleY = 0.0f;
        this.mDirection = 0;
    }
}
