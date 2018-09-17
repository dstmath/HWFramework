package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.ArrayMap;
import android.util.HwNormalizedSpline;
import android.util.Log;
import android.util.Slog;
import android.util.Spline;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import com.android.server.display.HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.lights.LightsManager;
import java.util.Calendar;
import java.util.List;

public class HwNormalizedAutomaticBrightnessController extends AutomaticBrightnessController implements HwBrightnessPgSceneDetectionCallbacks {
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_FOR_SENSOR_NOT_READY_WHEN_WAKEUP = -1;
    private static final boolean DEBUG;
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int MINDEFAULTBRIGHTNESS = 4;
    private static final int MSG_CoverMode_DEBOUNCED = 4;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 3;
    private static final int MSG_REPORT_PROXIMITY_STATE = 2;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final long POWER_ON_LUX_ABANDON_COUNT_MAX = 3;
    private static final int POWER_ON_LUX_COUNT_MAX = 8;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    public static final String SCREEN_BRIGHTNESS_MODE_LAST = "screen_brightness_mode_last";
    private static String TAG = "HwNormalizedAutomaticBrightnessController";
    private static final int TIME_DELAYED_USING_PROXIMITY_STATE = 500;
    private static final int TIME_PRINT_SENSOR_VALUE_INTERVAL = 4000;
    private static final int TIME_SENSOR_REPORT_NONE_VALUE = 400;
    private static int mDeviceActualBrightnessLevel;
    private static int mDeviceActualBrightnessNit;
    private static int mDeviceStandardBrightnessNit;
    private static HwNormalizedSpline mHwNormalizedScreenAutoBrightnessSpline;
    private static final Object mLock = new Object();
    private boolean mAllowLabcUseProximity;
    private float mAmbientLuxOffset;
    private float mAutoBrightnessOut;
    private final HandlerThread mAutoBrightnessProcessThread;
    private boolean mAutoPowerSavingAnimationEnable;
    private int mBrightnessOutForLog;
    private boolean mCameraModeChangeAnimationEnable;
    private int mCameraModeCountNum;
    private boolean mCameraModeEnable;
    private final Context mContext;
    private int mCoverModeFastResponseTimeDelay;
    private boolean mCoverModeFlag;
    private boolean mCoverStateFast;
    private boolean mCurrentUserChanging;
    private int mCurrentUserId;
    private final Data mData;
    private float mDefaultBrightness;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private HwRingBuffer mHwAmbientLightRingBuffer;
    private HwRingBuffer mHwAmbientLightRingBufferTrace;
    private HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private HwBrightnessPgSceneDetection mHwBrightnessPgSceneDetection;
    private HwEyeProtectionControllerImpl mHwEyeProtectionController;
    private int mHwLastReportedSensorValue;
    private long mHwLastReportedSensorValueTime;
    private int mHwLastSensorValue;
    private final HwNormalizedAutomaticBrightnessHandler mHwNormalizedAutomaticBrightnessHandler;
    private long mHwPrintLogTime;
    private int mHwRateMillis;
    private boolean mHwReportValueWhenSensorOnChange;
    private boolean mIntervenedAutoBrightnessEnable;
    private boolean mIsclosed;
    private long mLastAmbientLightToMonitorTime;
    private int mLastDefaultBrightness;
    private boolean mPolicyChangeFromDim;
    private int mPowerOnLuxAbandonCount;
    private int mPowerOnLuxCount;
    private int mPowerPolicy;
    private boolean mPowerStatus;
    private int mProximity;
    private boolean mProximityPositive;
    private long mProximityReportTime;
    private final Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener;
    private ScreenStateReceiver mScreenStateReceiver;
    private boolean mWakeupCoverBrightnessEnable;

    private final class HwNormalizedAutomaticBrightnessHandler extends Handler {
        public HwNormalizedAutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwNormalizedAutomaticBrightnessController.this.handleUpdateAmbientLuxMsg();
                    return;
                case 2:
                    HwNormalizedAutomaticBrightnessController.this.handleProximitySensorEvent();
                    return;
                case 3:
                    HwNormalizedAutomaticBrightnessController.this.debounceProximitySensor();
                    return;
                case 4:
                    HwNormalizedAutomaticBrightnessController.this.setCoverModeFastResponseFlag();
                    return;
                default:
                    return;
            }
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            HwNormalizedAutomaticBrightnessController.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.e(HwNormalizedAutomaticBrightnessController.TAG, "Invalid input parameter!");
                return;
            }
            Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "BroadcastReceiver.onReceive() action:" + intent.getAction());
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && !HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted()) {
                HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.registerPgBLightSceneListener(HwNormalizedAutomaticBrightnessController.this.mContext);
                if (HwNormalizedAutomaticBrightnessController.DEBUG) {
                    Slog.d(HwNormalizedAutomaticBrightnessController.TAG, "BOOT_COMPLETED: auto in registerPgBLightSceneChangedListener,=" + HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted());
                }
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
        mDeviceActualBrightnessLevel = 0;
        mDeviceActualBrightnessNit = 0;
        mDeviceStandardBrightnessNit = 0;
        mDeviceActualBrightnessLevel = getDeviceActualBrightnessLevel();
        mDeviceActualBrightnessNit = getDeviceActualBrightnessNit();
        mDeviceStandardBrightnessNit = getDeviceStandardBrightnessNit();
        if (DEBUG) {
            Slog.i(TAG, "DeviceActualLevel=" + mDeviceActualBrightnessLevel + ",DeviceActualBrightnessNit=" + mDeviceActualBrightnessNit + ",DeviceStandardBrightnessNit=" + mDeviceStandardBrightnessNit);
        }
    }

    private static int getDeviceActualBrightnessLevel() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessLevel();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getDeviceActualBrightnessNit() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessNit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getDeviceStandardBrightnessNit() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceStandardBrightnessNit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static HwNormalizedSpline createHwNormalizedAutoBrightnessSpline(Context context) {
        try {
            mHwNormalizedScreenAutoBrightnessSpline = HwNormalizedSpline.createHwNormalizedSpline(context, mDeviceActualBrightnessLevel, mDeviceActualBrightnessNit, mDeviceStandardBrightnessNit);
            return mHwNormalizedScreenAutoBrightnessSpline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    public void updateStateRecognition(boolean usePwrBLightCurve, int appType) {
        if (this.mLightSensorEnabled) {
            if (this.mData.autoPowerSavingUseManualAnimationTimeEnable) {
                if (mHwNormalizedScreenAutoBrightnessSpline.getPowerSavingModeBrightnessChangeEnable(this.mAmbientLux, usePwrBLightCurve)) {
                    this.mAutoPowerSavingAnimationEnable = true;
                } else {
                    this.mAutoPowerSavingAnimationEnable = false;
                }
            }
            mHwNormalizedScreenAutoBrightnessSpline.setPowerSavingModeEnable(usePwrBLightCurve);
            updateAutoBrightness(true);
        }
    }

    public boolean getAutoPowerSavingUseManualAnimationTimeEnable() {
        return this.mData.autoPowerSavingUseManualAnimationTimeEnable;
    }

    public boolean getAutoPowerSavingAnimationEnable() {
        return this.mAutoPowerSavingAnimationEnable;
    }

    public void setAutoPowerSavingAnimationEnable(boolean enable) {
        this.mAutoPowerSavingAnimationEnable = enable;
    }

    public HwNormalizedAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, int ambientLightHorizon, float autoBrightnessAdjustmentMaxGamma, HysteresisLevels dynamicHysteresis, Context context) {
        super(callbacks, looper, sensorManager, createHwNormalizedAutoBrightnessSpline(context), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma, dynamicHysteresis);
        this.mPowerStatus = false;
        this.mPowerOnLuxCount = 0;
        this.mPowerOnLuxAbandonCount = 0;
        this.mCurrentUserId = 0;
        this.mCurrentUserChanging = false;
        this.mHwRateMillis = 300;
        this.mHwPrintLogTime = -1;
        this.mHwLastSensorValue = -1;
        this.mHwLastReportedSensorValue = -1;
        this.mHwLastReportedSensorValueTime = -1;
        this.mHwReportValueWhenSensorOnChange = true;
        this.mHwAmbientLightRingBuffer = new HwRingBuffer(10);
        this.mHwAmbientLightRingBufferTrace = new HwRingBuffer(50);
        this.mPowerPolicy = 0;
        this.mPolicyChangeFromDim = false;
        this.mIntervenedAutoBrightnessEnable = false;
        this.mProximity = -1;
        this.mCoverModeFastResponseTimeDelay = 2500;
        this.mCoverStateFast = false;
        this.mIsclosed = false;
        this.mCameraModeEnable = false;
        this.mCameraModeChangeAnimationEnable = false;
        this.mCameraModeCountNum = 0;
        this.mDefaultBrightness = -1.0f;
        this.mBrightnessOutForLog = -1;
        this.mAmbientLuxOffset = -1.0f;
        this.mScreenStateReceiver = null;
        this.mAutoPowerSavingAnimationEnable = false;
        this.mWakeupCoverBrightnessEnable = false;
        this.mProximitySensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                boolean z = false;
                if (HwNormalizedAutomaticBrightnessController.this.mProximitySensorEnabled) {
                    float distance = event.values[0];
                    HwNormalizedAutomaticBrightnessController.this.mProximityReportTime = SystemClock.uptimeMillis();
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = HwNormalizedAutomaticBrightnessController.this;
                    if (distance >= 0.0f && distance < 5.0f) {
                        z = true;
                    }
                    hwNormalizedAutomaticBrightnessController.mProximityPositive = z;
                    if (HwNormalizedAutomaticBrightnessController.DEBUG) {
                        Slog.d(HwNormalizedAutomaticBrightnessController.TAG, "mProximitySensorListener: time = " + HwNormalizedAutomaticBrightnessController.this.mProximityReportTime + "; distance = " + distance);
                    }
                    if (!HwNormalizedAutomaticBrightnessController.this.mWakeupFromSleep && HwNormalizedAutomaticBrightnessController.this.mProximityReportTime - HwNormalizedAutomaticBrightnessController.this.mLightSensorEnableTime > 500) {
                        HwNormalizedAutomaticBrightnessController.this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessage(2);
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mHwAmbientLuxFilterAlgo = new HwAmbientLuxFilterAlgo(lightSensorRate, mDeviceActualBrightnessLevel);
        this.mAutoBrightnessProcessThread = new HandlerThread(TAG);
        this.mAutoBrightnessProcessThread.start();
        this.mHwNormalizedAutomaticBrightnessHandler = new HwNormalizedAutomaticBrightnessHandler(this.mAutoBrightnessProcessThread.getLooper());
        this.mHwReportValueWhenSensorOnChange = this.mHwAmbientLuxFilterAlgo.reportValueWhenSensorOnChange();
        this.mProximitySensor = sensorManager.getDefaultSensor(8);
        this.mAllowLabcUseProximity = this.mHwAmbientLuxFilterAlgo.needToUseProximity();
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = new HwEyeProtectionControllerImpl(context, this);
        }
        this.mData = HwBrightnessXmlLoader.getData(getDeviceActualBrightnessLevel());
        this.mContext = context;
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        this.mHwBrightnessPgSceneDetection = new HwBrightnessPgSceneDetection(this, this.mData.pgSceneDetectionDarkenDelayTime, this.mData.pgSceneDetectionBrightenDelayTime, this.mContext);
        if (this.mData.pgReregisterScene) {
            this.mScreenStateReceiver = new ScreenStateReceiver();
        }
    }

    public HwNormalizedAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
        this(callbacks, looper, sensorManager, createHwNormalizedAutoBrightnessSpline(context), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, 0, 0, 0, 0, false, 10000, 3.0f, null, context);
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = new HwEyeProtectionControllerImpl(context, this);
        }
    }

    public void configure(boolean enable, float adjustment, boolean dozing) {
        configure(enable, adjustment, dozing, false);
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
        boolean z = false;
        if (this.mLightSensorEnabled && (enable ^ 1) != 0) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mHwAmbientLuxFilterAlgo.clear();
            if (!this.mHwReportValueWhenSensorOnChange) {
                clearSensorData();
            }
            this.mLastAmbientLightToMonitorTime = 0;
        }
        if (!enable) {
            this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
        }
        super.configure(enable, adjustment, dozing, userInitiatedChange);
        if (this.mLightSensorEnabled && -1 == this.mHwPrintLogTime) {
            this.mHwPrintLogTime = this.mLightSensorEnableTime;
        }
        if (!(!enable || (dozing ^ 1) == 0 || this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted())) {
            this.mHwBrightnessPgSceneDetection.registerPgBLightSceneListener(this.mContext);
            if (DEBUG) {
                Slog.d(TAG, "PowerSaving auto in registerPgBLightSceneChangedListener,=" + this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted());
            }
        }
        if (this.mAllowLabcUseProximity && enable) {
            z = dozing ^ 1;
        }
        setProximitySensorEnabled(z);
    }

    public int getAutomaticScreenBrightness() {
        if (this.mWakeupFromSleep && SystemClock.uptimeMillis() - this.mLightSensorEnableTime < 200) {
            if (DEBUG) {
                Slog.d(TAG, "mWakeupFromSleep= " + this.mWakeupFromSleep + ",currentTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableTime=" + this.mLightSensorEnableTime);
            }
            return -1;
        } else if (needToSetBrightnessBaseIntervened()) {
            return super.getAutomaticScreenBrightness();
        } else {
            if (needToSetBrightnessBaseProximity()) {
                return -2;
            }
            return super.getAutomaticScreenBrightness();
        }
    }

    private boolean needToSetBrightnessBaseIntervened() {
        return this.mIntervenedAutoBrightnessEnable ? this.mAllowLabcUseProximity : false;
    }

    public void updateIntervenedAutoBrightness(int brightness) {
        this.mAutoBrightnessOut = (float) brightness;
        this.mIntervenedAutoBrightnessEnable = true;
        if (this.mData.manualMode) {
            this.mDefaultBrightness = mHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
            float lux = mHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForBrightness();
            if (DEBUG) {
                Slog.i(TAG, "HwAutoBrightnessIn=" + brightness + ",defaultBrightness=" + this.mDefaultBrightness + ",lux=" + lux);
            }
            if (this.mAutoBrightnessOut >= ((float) this.mData.manualBrightnessMaxLimit)) {
                if (lux > ((float) this.mData.outDoorThreshold)) {
                    int autoBrightnessOutTmp = ((int) this.mAutoBrightnessOut) < this.mData.manualBrightnessMaxLimit ? (int) this.mAutoBrightnessOut : this.mData.manualBrightnessMaxLimit;
                    if (autoBrightnessOutTmp <= ((int) this.mDefaultBrightness)) {
                        autoBrightnessOutTmp = (int) this.mDefaultBrightness;
                    }
                    this.mAutoBrightnessOut = (float) autoBrightnessOutTmp;
                } else {
                    this.mAutoBrightnessOut = this.mAutoBrightnessOut < ((float) this.mData.manualBrightnessMaxLimit) ? this.mAutoBrightnessOut : (float) this.mData.manualBrightnessMaxLimit;
                }
            }
            if (DEBUG) {
                Slog.i(TAG, "HwAutoBrightnessOut=" + this.mAutoBrightnessOut);
            }
            super.updateIntervenedAutoBrightness((int) this.mAutoBrightnessOut);
            return;
        }
        super.updateIntervenedAutoBrightness((int) this.mAutoBrightnessOut);
    }

    private int getSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            long time = SystemClock.uptimeMillis();
            int N = this.mHwAmbientLightRingBuffer.size();
            int i;
            if (N > 0) {
                int sum = 0;
                for (int i2 = N - 1; i2 >= 0; i2--) {
                    sum = (int) (((float) sum) + this.mHwAmbientLightRingBuffer.getLux(i2));
                }
                int average = sum / N;
                if (average >= 0) {
                    this.mHwLastSensorValue = average;
                }
                this.mHwAmbientLightRingBuffer.clear();
                if (time - this.mHwPrintLogTime > 4000) {
                    Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(this.mHwAmbientLightRingBufferTrace.size()));
                    this.mHwAmbientLightRingBufferTrace.clear();
                    this.mHwPrintLogTime = time;
                }
                i = this.mHwLastSensorValue;
                return i;
            } else if (time - this.mHwLastReportedSensorValueTime < 400) {
                i = this.mHwLastSensorValue;
                return i;
            } else {
                i = this.mHwLastReportedSensorValue;
                return i;
            }
        }
    }

    private void clearSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.clear();
            Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(this.mHwAmbientLightRingBufferTrace.size()));
            this.mHwAmbientLightRingBufferTrace.clear();
            this.mHwLastReportedSensorValueTime = -1;
            this.mHwLastReportedSensorValue = -1;
            this.mHwLastSensorValue = -1;
            this.mHwPrintLogTime = -1;
        }
    }

    private void reportLightSensorEventToAlgo(long time, float lux) {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        if (!this.mAmbientLuxValid) {
            this.mWakeupFromSleep = false;
            this.mAmbientLuxValid = true;
            this.mHwAmbientLuxFilterAlgo.isFirstAmbientLux(true);
            if (this.mData.dayModeAlgoEnable || this.mData.offsetResetEnable) {
                this.mHwAmbientLuxFilterAlgo.setDayModeEnable();
                if (this.mData.dayModeAlgoEnable) {
                    mHwNormalizedScreenAutoBrightnessSpline.setDayModeEnable(this.mHwAmbientLuxFilterAlgo.getDayModeEnable());
                }
                if (this.mData.offsetResetEnable) {
                    this.mAmbientLuxOffset = mHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForOffset();
                    if (this.mAmbientLuxOffset != -1.0f) {
                        mHwNormalizedScreenAutoBrightnessSpline.reSetOffsetFromHumanFactor(this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable(), (int) calculateOffsetMinBrightness(this.mAmbientLuxOffset), (int) calculateOffsetMaxBrightness(this.mAmbientLuxOffset));
                    }
                }
                if (DEBUG) {
                    Slog.d(TAG, "DayMode:dayModeEnable=" + this.mHwAmbientLuxFilterAlgo.getDayModeEnable() + ",offsetEnable=" + this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable());
                }
            }
            if (DEBUG) {
                Slog.d(TAG, "mAmbientLuxValid=" + this.mAmbientLuxValid + ",mWakeupFromSleep= " + this.mWakeupFromSleep);
            }
        }
        this.mHwAmbientLuxFilterAlgo.handleLightSensorEvent(time, lux);
        this.mAmbientLux = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        if (this.mHwAmbientLuxFilterAlgo.needToUpdateBrightness()) {
            if (DEBUG) {
                Slog.d(TAG, "need to update brightness: mAmbientLux=" + this.mAmbientLux);
            }
            this.mHwAmbientLuxFilterAlgo.brightnessUpdated();
            updateAutoBrightness(true);
        }
        if (!this.mHwReportValueWhenSensorOnChange) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(1, (long) this.mHwRateMillis);
        }
        if (this.mProximityPositive) {
            this.mLastAmbientLightToMonitorTime = 0;
        } else {
            sendAmbientLightToMonitor(time, lux);
        }
        sendDefaultBrightnessToMonitor();
        sendPowerOnBrightnessChangedToMonitor();
    }

    private float calculateOffsetMinBrightness(float amLux) {
        if (amLux < 0.0f) {
            Slog.w(TAG, "amlux<0, return offsetMIN");
            return 4.0f;
        }
        float offsetMinBrightness = 4.0f;
        HwXmlAmPoint temp1 = null;
        for (HwXmlAmPoint temp : this.mData.ambientLuxValidBrightnessPoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                HwXmlAmPoint temp2 = temp;
                if (temp.x <= temp1.x) {
                    offsetMinBrightness = 4.0f;
                    if (DEBUG) {
                        Slog.i(TAG, "OffsetMinBrightness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    offsetMinBrightness = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
                return offsetMinBrightness;
            }
            temp1 = temp;
            offsetMinBrightness = temp.y;
        }
        return offsetMinBrightness;
    }

    private float calculateOffsetMaxBrightness(float amLux) {
        if (amLux < 0.0f) {
            Slog.w(TAG, "amlux<0, return offsetMAX");
            return 255.0f;
        }
        float offsetMaxBrightness = 255.0f;
        HwXmlAmPoint temp1 = null;
        for (HwXmlAmPoint temp : this.mData.ambientLuxValidBrightnessPoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                HwXmlAmPoint temp2 = temp;
                if (temp.x <= temp1.x) {
                    offsetMaxBrightness = 255.0f;
                    if (DEBUG) {
                        Slog.i(TAG, "OffsetMaxBrightness_temp1.x <= temp2.x,x" + temp.x + ", z = " + temp.z);
                    }
                } else {
                    offsetMaxBrightness = (((temp.z - temp1.z) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.z;
                }
                return offsetMaxBrightness;
            }
            temp1 = temp;
            offsetMaxBrightness = temp.z;
        }
        return offsetMaxBrightness;
    }

    private void sendAmbientLightToMonitor(long time, float lux) {
        if (this.mDisplayEffectMonitor != null) {
            if (this.mLastAmbientLightToMonitorTime == 0 || time <= this.mLastAmbientLightToMonitorTime) {
                this.mLastAmbientLightToMonitorTime = time;
                return;
            }
            int durationInMs = (int) (time - this.mLastAmbientLightToMonitorTime);
            this.mLastAmbientLightToMonitorTime = time;
            ArrayMap<String, Object> params = new ArrayMap();
            params.put(MonitorModule.PARAM_TYPE, "ambientLightCollection");
            params.put("lightValue", Integer.valueOf((int) lux));
            params.put("durationInMs", Integer.valueOf(durationInMs));
            params.put("brightnessMode", "AUTO");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendDefaultBrightnessToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            int defaultBrightness = (int) mHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
            if (this.mLastDefaultBrightness != defaultBrightness) {
                this.mLastDefaultBrightness = defaultBrightness;
                int lightValue = (int) mHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForBrightness();
                ArrayMap<String, Object> params = new ArrayMap();
                params.put(MonitorModule.PARAM_TYPE, "algoDefaultBrightness");
                params.put("lightValue", Integer.valueOf(lightValue));
                params.put("brightness", Integer.valueOf(defaultBrightness));
                params.put("brightnessMode", "AUTO");
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendPowerOnBrightnessChangedToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            if (this.mCoverStateFast) {
                this.mCoverModeFlag = true;
            }
            if (this.mHwAmbientLuxFilterAlgo.getAmbientLuxSavedNum() == 10) {
                if (this.mCoverModeFlag) {
                    this.mCoverModeFlag = false;
                } else if (this.mUpdateAutoBrightnessCount >= 2) {
                    List<Integer> lightValueList = this.mHwAmbientLuxFilterAlgo.getAmbientLuxSavedData(10);
                    if (lightValueList != null && lightValueList.size() == 10) {
                        ArrayMap<String, Object> params = new ArrayMap();
                        params.put(MonitorModule.PARAM_TYPE, "powerOnBrightnessChanged");
                        params.put("lightValueList", lightValueList);
                        this.mDisplayEffectMonitor.sendMonitorParam(params);
                    }
                }
            }
        }
    }

    protected void handleLightSensorEvent(long time, float lux) {
        if (mHwNormalizedScreenAutoBrightnessSpline.getCalibrationTestEable()) {
            this.mSetbrightnessImmediateEnable = true;
            getLightSensorFromDB();
            return;
        }
        this.mSetbrightnessImmediateEnable = false;
        if (!this.mAmbientLuxValid || this.mHwReportValueWhenSensorOnChange) {
            reportLightSensorEventToAlgo(time, lux);
            if (!this.mHwReportValueWhenSensorOnChange) {
                synchronized (mLock) {
                    this.mHwLastReportedSensorValue = (int) lux;
                    this.mHwLastReportedSensorValueTime = time;
                }
                return;
            }
            return;
        }
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.push(time, lux);
            this.mHwAmbientLightRingBufferTrace.push(time, lux);
            this.mHwLastReportedSensorValue = (int) lux;
            this.mHwLastReportedSensorValueTime = time;
        }
    }

    protected void getLightSensorFromDB() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        this.mAmbientLuxValid = true;
        float ambientLux = mHwNormalizedScreenAutoBrightnessSpline.getAmbientValueFromDB();
        if (((int) (ambientLux * 10.0f)) != ((int) (this.mAmbientLux * 10.0f))) {
            this.mAmbientLux = ambientLux;
            if (DEBUG) {
                Slog.d(TAG, "setAmbientLuxDB=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
    }

    public void setPowerStatus(boolean powerStatus) {
        if (DEBUG && this.mPowerStatus != powerStatus) {
            Slog.d(TAG, "set power status:mPowerStatus=" + this.mPowerStatus + ",powerStatus=" + powerStatus);
        }
        if (this.mPowerStatus != powerStatus && powerStatus && this.mData.coverModeDayEnable) {
            updateCoverModeDayBrightness();
        }
        this.mPowerStatus = powerStatus;
        this.mWakeupFromSleep = powerStatus;
        this.mHwAmbientLuxFilterAlgo.setPowerStatus(powerStatus);
        if (!this.mPowerStatus) {
            this.mPowerOnLuxAbandonCount = 0;
            this.mPowerOnLuxCount = 0;
            this.mWakeupCoverBrightnessEnable = false;
        }
        if (this.mHwEyeProtectionController != null) {
            this.mHwEyeProtectionController.onScreenStateChanged(powerStatus);
        }
    }

    private void updateCoverModeDayBrightness() {
        boolean isClosed = HwServiceFactory.isCoverClosed();
        int brightnessMode = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
        if (isClosed && brightnessMode == 1) {
            int openHour = Calendar.getInstance().get(11);
            if (openHour >= this.mData.converModeDayBeginTime && openHour < this.mData.coverModeDayEndTime) {
                setCoverModeDayEnable(true);
                this.mWakeupCoverBrightnessEnable = true;
                Slog.i(TAG, "LabcCoverMode,isClosed=" + isClosed + ",openHour=" + openHour + ",coverModeBrightness=" + this.mData.coverModeDayBrightness);
            }
        }
    }

    protected boolean interceptHandleLightSensorEvent(long time, float lux) {
        if (SystemClock.uptimeMillis() < ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime) {
            Slog.i(TAG, "sensor not ready yet at time " + time);
            return true;
        } else if (this.mCurrentUserChanging) {
            return true;
        } else {
            if (this.mPowerStatus) {
                this.mPowerOnLuxAbandonCount++;
                this.mPowerOnLuxCount++;
                if (this.mPowerOnLuxCount > getpowerOnFastResponseLuxNum()) {
                    if (DEBUG) {
                        Slog.d(TAG, "set power status:false,powerOnFastResponseLuxNum=" + getpowerOnFastResponseLuxNum());
                    }
                    this.mPowerStatus = false;
                    this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
                }
                if (this.mLightSensorEnableElapsedTimeNanos - time > 0) {
                    if (DEBUG) {
                        Slog.d(TAG, "abandon handleLightSensorEvent:" + lux);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        this.mIntervenedAutoBrightnessEnable = false;
        float lux = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        if (DEBUG) {
            Slog.i(TAG, "AdjustPositionBrightness=" + ((int) (adjustFactor * 255.0f)) + ",lux=" + lux);
        }
        mHwNormalizedScreenAutoBrightnessSpline.updateLevelWithLux(adjustFactor * 255.0f, lux);
    }

    protected boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        return false;
    }

    public void saveOffsetAlgorithmParas() {
    }

    private void handleUpdateAmbientLuxMsg() {
        reportLightSensorEventToAlgo(SystemClock.uptimeMillis(), (float) getSensorData());
    }

    protected void updateBrightnessIfNoAmbientLuxReported() {
        if (this.mWakeupFromSleep) {
            this.mWakeupFromSleep = false;
            this.mCallbacks.updateBrightness();
            this.mFirstAutoBrightness = false;
            this.mUpdateAutoBrightnessCount++;
            if (DEBUG) {
                Slog.d(TAG, "sensor doesn't report lux in 200ms");
            }
        }
    }

    public void updateCurrentUserId(int userId) {
        if (userId != this.mCurrentUserId) {
            if (DEBUG) {
                Slog.d(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
            }
            this.mCurrentUserId = userId;
            this.mCurrentUserChanging = true;
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mAmbientLuxValid = false;
            this.mHwAmbientLuxFilterAlgo.clear();
            mHwNormalizedScreenAutoBrightnessSpline.updateCurrentUserId(userId);
            this.mCurrentUserChanging = false;
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mProximitySensorEnabled) {
                this.mProximitySensorEnabled = true;
                getSensorManager().registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHwNormalizedAutomaticBrightnessHandler);
                if (DEBUG) {
                    Slog.d(TAG, "open proximity sensor");
                }
            }
        } else if (this.mProximitySensorEnabled) {
            this.mProximitySensorEnabled = false;
            this.mProximity = -1;
            getSensorManager().unregisterListener(this.mProximitySensorListener);
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
            this.mCallbacks.updateProximityState(false);
            if (DEBUG) {
                Slog.d(TAG, "close proximity sensor");
            }
        }
    }

    private void processProximityState() {
        int proximity = this.mHwAmbientLuxFilterAlgo.getProximityState();
        if (this.mProximity != proximity) {
            if (DEBUG) {
                Slog.d(TAG, "mProximity=" + this.mProximity + ",proximity=" + proximity);
            }
            if (this.mProximity == 1 && proximity == 0) {
                this.mFirstBrightnessAfterProximityNegative = true;
            }
            this.mProximity = proximity;
            if (this.mProximity != -1) {
                if (this.mProximity == 1) {
                    this.mCallbacks.updateProximityState(true);
                } else if (this.mProximity == 0) {
                    this.mCallbacks.updateProximityState(false);
                }
            }
        }
    }

    private void handleProximitySensorEvent() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
        this.mHwAmbientLuxFilterAlgo.handleProximitySensorEvent(this.mProximityReportTime, this.mProximityPositive);
        processProximityState();
        if (this.mHwAmbientLuxFilterAlgo.needToSendProximityDebounceMsg()) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageAtTime(3, this.mHwAmbientLuxFilterAlgo.getPendingProximityDebounceTime());
        }
    }

    private void debounceProximitySensor() {
        if (DEBUG) {
            Slog.d(TAG, "process MSG_PROXIMITY_SENSOR_DEBOUNCED");
        }
        this.mHwAmbientLuxFilterAlgo.debounceProximitySensor();
        processProximityState();
    }

    public void updatePowerPolicy(int policy) {
        if (this.mPowerPolicy != 2 || policy == 2) {
            this.mPolicyChangeFromDim = false;
        } else {
            this.mPolicyChangeFromDim = true;
        }
        this.mPowerPolicy = policy;
    }

    private boolean needToSetBrightnessBaseProximity() {
        boolean needToSet = (this.mProximity != 1 || (this.mBrightnessEnlarge ^ 1) == 0 || this.mUpdateAutoBrightnessCount <= 1 || this.mPowerPolicy == 2) ? false : this.mPolicyChangeFromDim ^ 1;
        if (DEBUG && needToSet) {
            Slog.d(TAG, "mProximity= " + this.mProximity + ",mBrightnessEnlarge=" + this.mBrightnessEnlarge + ",mUpdateAutoBrightnessCount=" + this.mUpdateAutoBrightnessCount + ",mPowerPolicy=" + this.mPowerPolicy + ",mPolicyChangeFromDim=" + this.mPolicyChangeFromDim);
        }
        return needToSet;
    }

    public void setSplineEyeProtectionControlFlag(boolean flag) {
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            mHwNormalizedScreenAutoBrightnessSpline.setEyeProtectionControlFlag(flag);
        }
    }

    public boolean getPowerStatus() {
        return this.mPowerStatus;
    }

    public void setCoverModeStatus(boolean isclosed) {
        if (isclosed) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(4);
        }
        if (!isclosed && this.mIsclosed) {
            this.mCoverStateFast = true;
            this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(4, (long) this.mCoverModeFastResponseTimeDelay);
        }
        this.mIsclosed = isclosed;
        this.mHwAmbientLuxFilterAlgo.setCoverModeStatus(isclosed);
    }

    public void setCoverModeFastResponseFlag() {
        this.mCoverStateFast = false;
        this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
        mHwNormalizedScreenAutoBrightnessSpline.setNoOffsetEnable(this.mCoverStateFast);
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode FastResponseFlag =" + this.mCoverStateFast);
        }
    }

    public boolean getCoverModeFastResponseFlag() {
        return this.mCoverStateFast;
    }

    public int getpowerOnFastResponseLuxNum() {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            return 8;
        }
        return this.mHwAmbientLuxFilterAlgo.getpowerOnFastResponseLuxNum();
    }

    public void updateAutoDBWhenSameBrightness(int brightness) {
        int brightnessAutoDB = System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
        int brightnessManualDB = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", 0, this.mCurrentUserId);
        if (brightnessAutoDB != brightness && brightnessManualDB == brightness) {
            System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", brightness, this.mCurrentUserId);
            if (DEBUG) {
                Slog.d(TAG, "OrigAutoDB=" + brightnessAutoDB + ",ManualDB=" + brightnessManualDB + ",brightness=" + brightness + ",mScreenAutoBrightness=" + this.mScreenAutoBrightness);
            }
        }
    }

    public boolean getCameraModeBrightnessLineEnable() {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            return false;
        }
        return this.mHwAmbientLuxFilterAlgo.getCameraModeBrightnessLineEnable();
    }

    public void setCameraModeBrightnessLineEnable(boolean cameraModeEnable) {
        if (mHwNormalizedScreenAutoBrightnessSpline != null && getCameraModeBrightnessLineEnable()) {
            mHwNormalizedScreenAutoBrightnessSpline.setCameraModeEnable(cameraModeEnable);
            if ((!cameraModeEnable && this.mCameraModeEnable) || (cameraModeEnable && (this.mCameraModeEnable ^ 1) != 0)) {
                this.mCameraModeChangeAnimationEnable = true;
                this.mCameraModeCountNum = 0;
                updateAutoBrightness(true);
            }
            this.mCameraModeCountNum++;
            if (this.mCameraModeCountNum == 3) {
                this.mCameraModeCountNum = 1;
                this.mCameraModeChangeAnimationEnable = false;
            }
            this.mCameraModeEnable = cameraModeEnable;
        }
    }

    public boolean getCameraModeChangeAnimationEnable() {
        return this.mCameraModeChangeAnimationEnable;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            Slog.e(TAG, "mHwAmbientLuxFilterAlgo=null");
        } else {
            this.mHwAmbientLuxFilterAlgo.setKeyguardLockedStatus(isLocked);
        }
    }

    public boolean getRebootAutoModeEnable() {
        return this.mData.rebootAutoModeEnable;
    }

    public boolean getOutdoorAnimationFlag() {
        return this.mHwAmbientLuxFilterAlgo.getOutdoorAnimationFlag();
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        int brightnessMode = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
        if (this.mWakeupCoverBrightnessEnable && (this.mHwAmbientLuxFilterAlgo.getCoverModeDayEnable() ^ 1) != 0) {
            this.mWakeupCoverBrightnessEnable = false;
        }
        if (brightnessMode != 1 || (this.mWakeupCoverBrightnessEnable ^ 1) == 0) {
            return this.mData.coverModeDayBrightness;
        }
        return this.mHwAmbientLuxFilterAlgo.getCoverModeBrightnessFromLastScreenBrightness();
    }

    public void setCoverModeDayEnable(boolean coverModeDayEnable) {
        this.mHwAmbientLuxFilterAlgo.setCoverModeDayEnable(coverModeDayEnable);
    }

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        if (brightness == -1 || (this.mData.manualMode ^ 1) != 0 || brightness > 255) {
            return brightness;
        }
        int brightnessOut;
        if (brightness < 4) {
            brightnessOut = 4;
            if (DEBUG && this.mBrightnessOutForLog != 4) {
                this.mBrightnessOutForLog = 4;
                Slog.d(TAG, "mScreenBrightnessOverrideFromWindowManagerMapping brightnessIn=" + brightness + ",brightnessOut=" + 4);
            }
        } else {
            brightnessOut = (((brightness - 4) * (this.mData.manualBrightnessMaxLimit - 4)) / 251) + 4;
            if (!(!DEBUG || brightness == brightnessOut || this.mBrightnessOutForLog == brightnessOut)) {
                this.mBrightnessOutForLog = brightnessOut;
                Slog.d(TAG, "mScreenBrightnessOverrideFromWindowManagerMapping brightnessIn=" + brightness + ",brightnessOut=" + brightnessOut);
            }
        }
        return brightnessOut;
    }

    public void setManualModeEnableForPg(boolean manualModeEnableForPg) {
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        if (brightness > 0) {
            this.mMaxBrightnessSetByThermal = brightness;
        } else {
            this.mMaxBrightnessSetByThermal = 255;
        }
        if (this.mLightSensorEnabled) {
            Slog.i(TAG, "ThermalMode set auto MaxBrightness=" + brightness);
            this.mCallbacks.updateBrightness();
        }
    }

    public boolean getRebootFirstBrightnessAnimationEnable() {
        return this.mData.rebootFirstBrightnessAnimationEnable;
    }

    public int getAdjustLightValByPgMode(int rawLightVal) {
        if (this.mHwBrightnessPgSceneDetection != null) {
            return this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(rawLightVal);
        }
        Slog.w(TAG, "mHwBrightnessPgSceneDetection=null");
        return rawLightVal;
    }
}
