package com.android.server.policy;

import android.app.AlarmManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.IMonitor;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import com.huawei.android.os.HwPowerManager;
import com.huawei.screenrecorder.activities.SurfaceControlEx;

public class TurnOnWakeScreenManager {
    private static final int BRIGHTNESS_OFF_VALUE = 0;
    private static final int BRIGHTNESS_RESUME_VALUE = -1;
    private static final double DEFAULT_AXIS_Z = -9.0d;
    private static final int DELAY_TIME_TO_TURN_OFF_GSENSOR = 10000;
    private static final String E922001500_CONTENT = "CONTENT";
    private static final String E922001500_COUNT = "COUNT";
    private static final String E922001500_DESCRIPTION = "DESCRIPTION";
    private static final String E922001500_DEVICE_NAME = "DEVICE_NAME";
    private static final String E922001500_ERROR_LEVEL = "ERROR_LEVEL";
    private static final String E922001500_IC_NAME = "IC_NAME";
    private static final boolean ESD_PROTECT_ENABLE = SystemProperties.getBoolean("hw_mc.lcd.esd_protect_enable", false);
    private static final boolean ESD_REMOVE_ALARM = SystemProperties.getBoolean("hw_mc.lcd.esd_remove_alarm", false);
    private static final int EVENT_AXIS_INDEX = 2;
    private static final boolean IS_ESD_DELAY_ENABLE = SystemProperties.getBoolean("ro.product.esd_delay_enable", false);
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "TurnOnWakeScreenManager";
    private static final int TP_IRON_DETECT_CODE = 922001500;
    private static final int TURN_OFF_ALL_PROXIMITY_SENSOR_MSG = 1002;
    private static final int TURN_OFF_G_SENSOR_MSG = 1000;
    private static final int TURN_OFF_SCREE_DEALY = 200;
    private static final int TURN_ON_PROXIMITY_SENSOR_MSG = 1001;
    private static final double TURN_THRESHOLD_DOWN = (ESD_PROTECT_ENABLE ? -8.5d : -9.5d);
    private static final double TURN_THRESHOLD_UP;
    private static final int TYPE_COUNT = 1;
    private static final String TYPE_DESCRIPTION = "TP is at iron plate mode";
    private static final String TYPE_DEVICE_NAME = "LCD";
    private static final int TYPE_ERROR_LEVEL = 6;
    private static final String TYPE_IC_NAME = "FT8719";
    private static TurnOnWakeScreenManager sInstance;
    private final long TIMER_VALUE = 10800000;
    private Sensor mAccSensor = null;
    private final SensorEventListener mAccSensorListener = new SensorEventListener() {
        /* class com.android.server.policy.TurnOnWakeScreenManager.AnonymousClass1 */
        double lastAxisZ = TurnOnWakeScreenManager.DEFAULT_AXIS_Z;

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                double eventAxisZ = (double) event.values[2];
                if (eventAxisZ > TurnOnWakeScreenManager.TURN_THRESHOLD_UP && this.lastAxisZ > TurnOnWakeScreenManager.TURN_THRESHOLD_UP) {
                    TurnOnWakeScreenManager.this.processDeviceUp();
                }
                if (eventAxisZ < TurnOnWakeScreenManager.TURN_THRESHOLD_DOWN && this.lastAxisZ < TurnOnWakeScreenManager.TURN_THRESHOLD_DOWN && !TurnOnWakeScreenManager.this.mIsAcquireLock) {
                    TurnOnWakeScreenManager.this.removeDisableGsensorDelayMsg();
                    TurnOnWakeScreenManager.this.processDeviceDown();
                }
                this.lastAxisZ = eventAxisZ;
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private AlarmManager mAlarmManager = null;
    private Context mContext;
    private IBinder mDisplayToken = null;
    private boolean mIsAccSensorEnabled = false;
    private boolean mIsAcquireLock = false;
    private volatile boolean mIsScreenOn = true;
    private volatile boolean mIsSensorWorking = false;
    private boolean mIsTurningOffSensor = false;
    private PowerManager mPowerManager = null;
    private PowerManager.WakeLock mProximityWakeLock = null;
    private SensorHandler mSensorHandler = null;
    private SensorManager mSensorManager = null;

    static {
        double d = -8.5d;
        if (ESD_PROTECT_ENABLE) {
            d = -7.0d;
        }
        TURN_THRESHOLD_UP = d;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processDeviceUp() {
        if (ESD_PROTECT_ENABLE) {
            Log.i(TAG, "onSensorChanged-AccSensor. screen is up, will turn on screen.");
            turnScreenOn();
            if (ESD_REMOVE_ALARM) {
                this.mIsSensorWorking = false;
                turnOffAllSensor();
                return;
            }
            startAlarm();
            return;
        }
        this.mSensorHandler.sendEmptyMessage(1002);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processDeviceDown() {
        if (ESD_PROTECT_ENABLE) {
            Log.i(TAG, "onSensorChanged-AccSensor. screen is down, will turn off screen.");
            turnScreenOff();
            if (!ESD_REMOVE_ALARM) {
                startAlarm();
                return;
            }
            return;
        }
        this.mSensorHandler.sendEmptyMessage(1001);
    }

    private void startAlarm() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
            this.mAlarmManager.setExact(0, System.currentTimeMillis() + 10800000, null, new AlarmManager.OnAlarmListener() {
                /* class com.android.server.policy.TurnOnWakeScreenManager.AnonymousClass2 */

                @Override // android.app.AlarmManager.OnAlarmListener
                public void onAlarm() {
                    TurnOnWakeScreenManager.this.turnOffAllSensor();
                    TurnOnWakeScreenManager.this.turnScreenOn();
                    TurnOnWakeScreenManager.this.mAlarmManager = null;
                }
            }, this.mSensorHandler);
        }
    }

    private TurnOnWakeScreenManager(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mAccSensor = this.mSensorManager.getDefaultSensor(1);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mProximityWakeLock = this.mPowerManager.newWakeLock(32, TAG);
        if (ESD_PROTECT_ENABLE) {
            this.mDisplayToken = SurfaceControlEx.getInternalDisplayToken();
        }
        this.mSensorHandler = new SensorHandler(BackgroundThread.getHandler().getLooper());
    }

    private void setCurrentBrightnessOff() {
        Log.i(TAG, "setDisplayStatus setCurrentBrightnessOff");
        Bundle bundle = new Bundle();
        bundle.putInt("ManufactureProcess", 0);
        bundle.putInt("Scene", 0);
        bundle.putInt(HwHiNetworkParmStatistics.LEVEL, 0);
        bundle.putInt("AnimationTime", 0);
        int ret = HwPowerManager.setHwBrightnessData("ManufactureBrightness", bundle);
        Log.i(TAG, "set current brightness off. ret = : " + ret);
        if (ret != 0) {
            Log.w(TAG, "set current brightness off failed, ret = " + ret);
        }
    }

    private void resumeCurrentBrightness() {
        Log.i(TAG, "setDisplayStatus setCurrentBrightnessResume");
        Bundle bundle = new Bundle();
        bundle.putInt("ManufactureProcess", -1);
        bundle.putInt("Scene", -1);
        bundle.putInt(HwHiNetworkParmStatistics.LEVEL, -1);
        bundle.putInt("AnimationTime", -1);
        int ret = HwPowerManager.setHwBrightnessData("ManufactureBrightness", bundle);
        Log.i(TAG, "resume current brightness. ret = : " + ret);
        if (ret != 0) {
            Log.w(TAG, "resume current brightness failed, ret = " + ret);
        }
    }

    private void turnScreenOff() {
        if (this.mDisplayToken == null) {
            this.mDisplayToken = SurfaceControlEx.getInternalDisplayToken();
        }
        Log.i(TAG, "Turn off screen, current displayToken : " + this.mDisplayToken);
        if (this.mIsScreenOn) {
            boolean z = ESD_REMOVE_ALARM;
            if (!z || (z && this.mIsSensorWorking)) {
                Log.i(TAG, "Turn off screen, current screen : " + this.mIsScreenOn);
                setCurrentBrightnessOff();
                SurfaceControlEx.setDisplayPowerMode(this.mDisplayToken, 0);
                this.mIsScreenOn = false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void turnScreenOn() {
        if (this.mDisplayToken == null) {
            this.mDisplayToken = SurfaceControlEx.getInternalDisplayToken();
        }
        Log.i(TAG, "Turn on screen, current displayToken : " + this.mDisplayToken);
        if (!this.mIsScreenOn) {
            Log.i(TAG, "Turn on screen, current screen : " + this.mIsScreenOn);
            SurfaceControlEx.setDisplayPowerMode(this.mDisplayToken, 2);
            resumeCurrentBrightness();
            this.mIsScreenOn = true;
        }
    }

    public static synchronized TurnOnWakeScreenManager getInstance(Context context) {
        TurnOnWakeScreenManager turnOnWakeScreenManager;
        synchronized (TurnOnWakeScreenManager.class) {
            if (sInstance == null) {
                sInstance = new TurnOnWakeScreenManager(context);
            }
            turnOnWakeScreenManager = sInstance;
        }
        return turnOnWakeScreenManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void turnOnProximitySensor() {
        PowerManager.WakeLock wakeLock = this.mProximityWakeLock;
        if (wakeLock == null) {
            return;
        }
        if (!wakeLock.isHeld()) {
            Log.i(TAG, "Acquiring proximity wake lock");
            this.mProximityWakeLock.acquire();
            this.mIsAcquireLock = true;
            reportMonitorData(null);
            return;
        }
        Log.i(TAG, "Proximity wake lock already acquired");
    }

    private void turnOffProximitySensor() {
        PowerManager.WakeLock wakeLock = this.mProximityWakeLock;
        if (wakeLock == null) {
            return;
        }
        if (wakeLock.isHeld()) {
            Log.i(TAG, "Releasing proximity wake lock");
            this.mProximityWakeLock.release();
            this.mIsAcquireLock = false;
            reportMonitorData(null);
            return;
        }
        Log.i(TAG, "Proximity wake lock already released");
    }

    public void turnOffAllSensor() {
        if (ESD_PROTECT_ENABLE) {
            turnOffSensor();
        } else {
            this.mSensorHandler.sendEmptyMessage(1002);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void turnOffSensor() {
        if (this.mIsAcquireLock) {
            turnOffProximitySensor();
        }
        if (IS_ESD_DELAY_ENABLE) {
            sendDelayMsgToDisableGsensor();
        } else {
            setAccSensorEnabled(false);
        }
    }

    public void setAccSensorEnabled(boolean isEnable) {
        Log.i(TAG, "setAccSensorEnabled enable:" + isEnable);
        this.mIsSensorWorking = isEnable;
        if (isEnable) {
            if (!this.mIsAccSensorEnabled) {
                this.mIsAccSensorEnabled = true;
                this.mSensorManager.registerListener(this.mAccSensorListener, this.mAccSensor, 3, 2);
            }
        } else if (this.mIsAccSensorEnabled) {
            this.mIsAccSensorEnabled = false;
            this.mSensorManager.unregisterListener(this.mAccSensorListener);
            this.mDisplayToken = null;
        }
    }

    public boolean isTurnOnSensorSupport() {
        Log.i(TAG, "isTurnOnSensorSupport mIsAccSensorEnabled:" + this.mIsAccSensorEnabled + ",mIsAcquireLock:" + this.mIsAcquireLock);
        return this.mIsAccSensorEnabled || this.mIsAcquireLock;
    }

    public void turnOffScreen() {
        new Handler().postDelayed(new Runnable() {
            /* class com.android.server.policy.TurnOnWakeScreenManager.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                if (TurnOnWakeScreenManager.this.mPowerManager.isScreenOn()) {
                    Log.i(TurnOnWakeScreenManager.TAG, "turnOffScreen goToSleep");
                    TurnOnWakeScreenManager.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
                    TurnOnWakeScreenManager.this.reportMonitorData("reason=goToSleep");
                }
            }
        }, 200);
    }

    public void reportMonitorData(String flag) {
        IMonitor.EventStream eventStream = IMonitor.openEventStream((int) TP_IRON_DETECT_CODE);
        eventStream.setParam(E922001500_ERROR_LEVEL, 6);
        eventStream.setParam(E922001500_IC_NAME, TYPE_IC_NAME);
        eventStream.setParam(E922001500_COUNT, 1);
        eventStream.setParam(E922001500_DEVICE_NAME, TYPE_DEVICE_NAME);
        eventStream.setParam(E922001500_DESCRIPTION, TYPE_DESCRIPTION);
        String content = getContent();
        if (flag != null) {
            content = content + "," + flag;
        }
        eventStream.setParam(E922001500_CONTENT, content);
        IMonitor.sendEvent(eventStream);
        IMonitor.closeEventStream(eventStream);
        Log.i(TAG, "reportMonitorData content:" + content);
    }

    private String getContent() {
        return "keycode=703,wakelockstatus=" + this.mIsAcquireLock;
    }

    /* access modifiers changed from: private */
    public class SensorHandler extends Handler {
        SensorHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    Log.i(TurnOnWakeScreenManager.TAG, "SensorHandler handleMessage try to trun off g-Sensor");
                    TurnOnWakeScreenManager.this.setAccSensorEnabled(false);
                    TurnOnWakeScreenManager.this.mIsTurningOffSensor = false;
                    return;
                case 1001:
                    TurnOnWakeScreenManager.this.turnOnProximitySensor();
                    return;
                case 1002:
                    TurnOnWakeScreenManager.this.turnOffSensor();
                    return;
                default:
                    return;
            }
        }
    }

    private void sendDelayMsgToDisableGsensor() {
        if (!this.mIsTurningOffSensor) {
            this.mSensorHandler.sendEmptyMessageDelayed(1000, HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
            this.mIsTurningOffSensor = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeDisableGsensorDelayMsg() {
        if (IS_ESD_DELAY_ENABLE && this.mIsTurningOffSensor) {
            this.mSensorHandler.removeMessages(1000);
            this.mIsTurningOffSensor = false;
        }
    }
}
