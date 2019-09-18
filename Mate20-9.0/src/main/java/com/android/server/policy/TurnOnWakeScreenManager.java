package com.android.server.policy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.EsdDetection;
import android.util.IMonitor;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class TurnOnWakeScreenManager {
    private static final int DELAY_TIME_TO_TURN_OFF_GSENSOR = 10000;
    private static final boolean ESD_DELAY_ENABLE = SystemProperties.getBoolean("ro.product.esd_delay_enable", false);
    private static final String TAG = "TurnOnWakeScreenManager";
    private static final int TP_IRON_DETECT_CODE = 922001500;
    private static final int TURN_OFF_G_SENSOR_MSG = 1000;
    private static final double TURN_THRESHOLD_DOWN = -9.5d;
    private static final double TURN_THRESHOLD_UP = -8.5d;
    private static final int TYPE_COUNT = 1;
    private static final String TYPE_DESCRIPTION = "TP is at iron plate mode";
    private static final String TYPE_DEVICE_NAME = "LCD";
    private static final int TYPE_ERROR_LEVEL = 6;
    private static final String TYPE_IC_NAME = "FT8719";
    private static TurnOnWakeScreenManager mTurnOnwakeupmanager;
    /* access modifiers changed from: private */
    public boolean mAcquireLock = false;
    /* access modifiers changed from: private */
    public Context mContext;
    private Sensor mGSensor = null;
    private boolean mGSensorEnabled = false;
    private final SensorEventListener mGSensorListener = new SensorEventListener() {
        double lastZAxis = -9.0d;

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != 1) {
                return;
            }
            if (!EsdDetection.isEsdEnabled()) {
                double zAxis = (double) event.values[2];
                if (zAxis > TurnOnWakeScreenManager.TURN_THRESHOLD_UP && this.lastZAxis > TurnOnWakeScreenManager.TURN_THRESHOLD_UP) {
                    TurnOnWakeScreenManager.this.turnOffAllSensor();
                }
                if (zAxis < TurnOnWakeScreenManager.TURN_THRESHOLD_DOWN && this.lastZAxis < TurnOnWakeScreenManager.TURN_THRESHOLD_DOWN && !TurnOnWakeScreenManager.this.mAcquireLock) {
                    TurnOnWakeScreenManager.this.removeDisableGsensorDelayMsg();
                    TurnOnWakeScreenManager.this.turnOnProximitySensor();
                }
                this.lastZAxis = zAxis;
                return;
            }
            boolean esdStatus = EsdDetection.getInstance(TurnOnWakeScreenManager.this.mContext).esdDetection(event);
            int esdCurrentStatus = EsdDetection.getInstance(TurnOnWakeScreenManager.this.mContext).getEsdCurrentStatus();
            if (!esdStatus) {
                TurnOnWakeScreenManager.this.turnOffAllSensor();
            }
            if (esdStatus && esdCurrentStatus == 1) {
                TurnOnWakeScreenManager.this.removeDisableGsensorDelayMsg();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsTurningOffSensor = false;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager = null;
    private PowerManager.WakeLock mProximityWakeLock = null;
    private SensorHandler mSensorHandler = null;
    private SensorManager mSensorManager = null;

    private class SensorHandler extends Handler {
        SensorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1000) {
                Log.i(TurnOnWakeScreenManager.TAG, "SensorHandler handleMessage try to trun off g-Sensor");
                TurnOnWakeScreenManager.this.setGSensorEnabled(false);
                boolean unused = TurnOnWakeScreenManager.this.mIsTurningOffSensor = false;
            }
        }
    }

    public static synchronized TurnOnWakeScreenManager getInstance(Context context) {
        TurnOnWakeScreenManager turnOnWakeScreenManager;
        synchronized (TurnOnWakeScreenManager.class) {
            if (mTurnOnwakeupmanager == null) {
                mTurnOnwakeupmanager = new TurnOnWakeScreenManager(context);
            }
            turnOnWakeScreenManager = mTurnOnwakeupmanager;
        }
        return turnOnWakeScreenManager;
    }

    private TurnOnWakeScreenManager(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mGSensor = this.mSensorManager.getDefaultSensor(1);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mProximityWakeLock = this.mPowerManager.newWakeLock(32, TAG);
        this.mSensorHandler = new SensorHandler(BackgroundThread.getHandler().getLooper());
    }

    /* access modifiers changed from: private */
    public void turnOnProximitySensor() {
        if (this.mProximityWakeLock == null) {
            return;
        }
        if (!this.mProximityWakeLock.isHeld()) {
            Log.i(TAG, "Acquiring proximity wake lock");
            this.mProximityWakeLock.acquire();
            this.mAcquireLock = true;
            reportMonitorData(null);
            return;
        }
        Log.i(TAG, "Proximity wake lock already acquired");
    }

    private void turnOffProximitySensor() {
        if (this.mProximityWakeLock == null) {
            return;
        }
        if (this.mProximityWakeLock.isHeld()) {
            Log.i(TAG, "Releasing proximity wake lock");
            try {
                this.mProximityWakeLock.release();
            } catch (RuntimeException e) {
                Log.e(TAG, "Releasing proximity wake lock RuntimeException");
            }
            this.mAcquireLock = false;
            reportMonitorData(null);
            return;
        }
        Log.i(TAG, "Proximity wake lock already released");
    }

    public void turnOffAllSensor() {
        if (this.mAcquireLock) {
            turnOffProximitySensor();
        }
        if (EsdDetection.isEsdEnabled()) {
            EsdDetection.getInstance(this.mContext).unInitStatus();
        }
        if (ESD_DELAY_ENABLE) {
            sendDelayMsgToDisableGsensor();
        } else {
            setGSensorEnabled(false);
        }
    }

    public void setGSensorEnabled(boolean enable) {
        Log.i(TAG, "setGSensorEnabled  enable:" + enable);
        if (enable) {
            if (!this.mGSensorEnabled) {
                this.mGSensorEnabled = true;
                if (EsdDetection.isEsdEnabled()) {
                    this.mSensorManager.registerListener(this.mGSensorListener, this.mGSensor, 0, 0);
                } else {
                    this.mSensorManager.registerListener(this.mGSensorListener, this.mGSensor, 3, 2);
                }
            }
        } else if (this.mGSensorEnabled) {
            this.mGSensorEnabled = false;
            this.mSensorManager.unregisterListener(this.mGSensorListener);
        }
    }

    public boolean isTurnOnSensorSupport() {
        Log.i(TAG, "isTurnOnSensorSupport  mGSensorEnabled:" + this.mGSensorEnabled + ",mAcquireLock:" + this.mAcquireLock);
        return this.mGSensorEnabled || this.mAcquireLock;
    }

    public void turnOffScreen() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (TurnOnWakeScreenManager.this.mPowerManager.isScreenOn()) {
                    Log.i(TurnOnWakeScreenManager.TAG, "trunOffScreen  goToSleep");
                    TurnOnWakeScreenManager.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
                    TurnOnWakeScreenManager.this.reportMonitorData("reason=goToSleep");
                }
            }
        }, 200);
    }

    public void reportMonitorData(String flag) {
        IMonitor.EventStream eStream = IMonitor.openEventStream(TP_IRON_DETECT_CODE);
        eStream.setParam(0, 6);
        eStream.setParam(1, TYPE_IC_NAME);
        eStream.setParam(4, 1);
        eStream.setParam(6, TYPE_DEVICE_NAME);
        eStream.setParam(3, TYPE_DESCRIPTION);
        String content = getContent();
        if (flag != null) {
            content = content + "," + flag;
        }
        eStream.setParam(5, content);
        IMonitor.sendEvent(eStream);
        IMonitor.closeEventStream(eStream);
        Log.i(TAG, "reportMonitorData content:" + content);
    }

    private String getContent() {
        return "keycode=703,wakelockstatus=" + this.mAcquireLock;
    }

    private void sendDelayMsgToDisableGsensor() {
        if (!this.mIsTurningOffSensor) {
            this.mSensorHandler.sendEmptyMessageDelayed(1000, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            this.mIsTurningOffSensor = true;
        }
    }

    /* access modifiers changed from: private */
    public void removeDisableGsensorDelayMsg() {
        if (ESD_DELAY_ENABLE && this.mIsTurningOffSensor) {
            this.mSensorHandler.removeMessages(1000);
            this.mIsTurningOffSensor = false;
        }
    }
}
