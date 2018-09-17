package com.huawei.android.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import huawei.com.android.internal.widget.HwFragmentContainer;

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
    private static boolean VERSION_LOG;
    private float mAngleDeltaX;
    private float mAngleDeltaY;
    private float mAngleRevisedY;
    private float mAngleX;
    private float mAngleY;
    private float mAngularSpeedThreshold;
    private float mChangedAngleThreshold;
    private int mDelayUs;
    private int mDirection;
    private Handler mHandler;
    SensorEventDetector mSensorEventDetector;
    private SensorManager mSensorManager;
    private int mSwingDelay;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hardware.HwSensorManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hardware.HwSensorManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hardware.HwSensorManager.<clinit>():void");
    }

    public void unRegisterListener(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hardware.HwSensorManager.unRegisterListener(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hardware.HwSensorManager.unRegisterListener(int):void");
    }

    public HwSensorManager(Context context) {
        this.mDelayUs = 30000;
        this.mDirection = 0;
        this.mSwingDelay = 600;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HwSensorManager.TYPE_DIRECTION_CHANGED_WITH_ROTATION /*1*/:
                        if (HwSensorManager.this.mSensorEventDetector != null) {
                            HwSensorManager.this.mSensorEventDetector.onSwing();
                        }
                        HwSensorManager.this.resetSensorData();
                    default:
                }
            }
        };
        if (VERSION_LOG) {
            VERSION_LOG = DEBUG;
            Log.d(TAG, "HwSensorManager version: 1.0.0");
        }
        if (context == null) {
            Log.w(TAG, "SensorDataManager() context is null");
            return;
        }
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        if (this.mSensorManager == null) {
            Log.w(TAG, "Cannot get system service : sensor");
            return;
        }
        this.mChangedAngleThreshold = 0.7853982f;
        this.mAngularSpeedThreshold = 3.1415927f;
        this.mSwingThreshold = 14.0f;
    }

    public boolean registerSensorListener(int sensorType) {
        return registerSensorListener(sensorType, this.mDelayUs);
    }

    public boolean registerSensorListener(int sensorType, int rateUs) {
        boolean z = DEBUG;
        boolean swingEventRegistered = DEBUG;
        if (isValid(sensorType) && (sensorType & TYPE_SWING) != 0) {
            z = registerSystemSensor(rateUs, TYPE_DIRECTION_CHANGED_WITH_ROTATION);
            swingEventRegistered = true;
        }
        if (isValid(sensorType) && sensorType < TYPE_SWING) {
            resetSensorData();
            this.mTimeStamp = 0;
            z = (!swingEventRegistered || z) ? registerSystemSensor(rateUs, TYPE_DIRECTION_HORIZONTAL_WITH_ROTATION) : DEBUG;
        }
        this.mDelayUs = getDefaultDelay(rateUs);
        this.mType |= sensorType;
        return z;
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case TYPE_DIRECTION_CHANGED_WITH_ROTATION /*1*/:
                float[] values = event.values;
                boolean isSwing = DEBUG;
                if (Math.abs(values[0]) <= this.mSwingThreshold && Math.abs(values[TYPE_DIRECTION_CHANGED_WITH_ROTATION]) <= this.mSwingThreshold) {
                    if (Math.abs(values[TYPE_DIRECTION_VERTICAL_WITH_ROTATION]) > this.mSwingThreshold) {
                    }
                    if (isSwing && this.mSensorEventDetector != null && this.mHandler != null) {
                        this.mHandler.removeMessages(TYPE_DIRECTION_CHANGED_WITH_ROTATION);
                        this.mHandler.sendEmptyMessageDelayed(TYPE_DIRECTION_CHANGED_WITH_ROTATION, (long) this.mSwingDelay);
                        return;
                    }
                    return;
                }
                isSwing = true;
                if (isSwing) {
                }
            case TYPE_DIRECTION_HORIZONTAL_WITH_ROTATION /*4*/:
                float gyro_wx = event.values[0];
                float gyro_wy = event.values[TYPE_DIRECTION_CHANGED_WITH_ROTATION];
                float gyro_wz = event.values[TYPE_DIRECTION_VERTICAL_WITH_ROTATION];
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
                    if (!((this.mType & TYPE_TILT_TO_MOVE) == 0 || this.mSensorEventDetector == null)) {
                        this.mSensorEventDetector.onTiltToMove(this.mAngleDeltaY, this.mAngleDeltaX);
                    }
                    if ((this.mType & TYPE_DIRECTION_HORIZONTAL_WITH_ROTATION) != 0) {
                        gyro_wx = 0.0f;
                    } else if ((this.mType & TYPE_DIRECTION_VERTICAL_WITH_ROTATION) != 0) {
                        gyro_wy = 0.0f;
                    }
                    if (this.mHandler == null || this.mHandler.hasMessages(TYPE_DIRECTION_CHANGED_WITH_ROTATION)) {
                        directionChanged = DEBUG;
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
            default:
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getSensorDelay() {
        return this.mDelayUs;
    }

    public void setRotationAngle(float angle) {
        this.mChangedAngleThreshold = angle;
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

    private boolean isValid(int sensorType) {
        return (sensorType <= 0 || sensorType >= TYPE_MAX) ? DEBUG : true;
    }

    private boolean registerSystemSensor(int delayUs, int type) {
        Sensor sensor = this.mSensorManager.getDefaultSensor(type);
        if (sensor == null) {
            Log.w(TAG, "unspported system sensor type : " + type);
            return DEBUG;
        }
        boolean rt = this.mSensorManager.registerListener(this, sensor, delayUs);
        if (!rt) {
            Log.w(TAG, "registerListener fail : " + type);
        }
        return rt;
    }

    private int getDefaultDelay(int rate) {
        switch (rate) {
            case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                return 0;
            case TYPE_DIRECTION_CHANGED_WITH_ROTATION /*1*/:
                return 20000;
            case TYPE_DIRECTION_VERTICAL_WITH_ROTATION /*2*/:
                return 66667;
            case SENSOR_CHANGE_DIRECTION_TO_TOP /*3*/:
                return 200000;
            default:
                return rate;
        }
    }

    private boolean getDirectionChanged(float wy, float wx, float angley, float anglex) {
        int direction = 0;
        if (Math.abs(wy) > Math.abs(wx)) {
            if (wy > this.mAngularSpeedThreshold) {
                direction = TYPE_DIRECTION_VERTICAL_WITH_ROTATION;
            } else if (wy < (-this.mAngularSpeedThreshold)) {
                direction = TYPE_DIRECTION_CHANGED_WITH_ROTATION;
            }
        } else if (wx > this.mAngularSpeedThreshold) {
            direction = TYPE_DIRECTION_HORIZONTAL_WITH_ROTATION;
        } else if (wx < (-this.mAngularSpeedThreshold)) {
            direction = SENSOR_CHANGE_DIRECTION_TO_TOP;
        }
        if (direction == 0 || direction == this.mDirection) {
            return DEBUG;
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
