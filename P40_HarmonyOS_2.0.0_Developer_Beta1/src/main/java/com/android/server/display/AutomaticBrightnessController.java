package com.android.server.display;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.TaskStackListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.EventLog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import com.android.internal.os.BackgroundThread;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.util.List;

public class AutomaticBrightnessController {
    private static final int AMBIENT_LIGHT_LONG_HORIZON_MILLIS = 10000;
    private static final long AMBIENT_LIGHT_PREDICTION_TIME_MILLIS = 100;
    private static final int AMBIENT_LIGHT_SHORT_HORIZON_MILLIS = 2000;
    private static final long BRIGHTENING_LIGHT_DEBOUNCE = 2000;
    private static final int BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS = 10000;
    private static final int COVER_MODE_DEFAULT_BRIGHTNESS = 60;
    private static final long DARKENING_LIGHT_DEBOUNCE = 8000;
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean DEBUG_CONTROLLER = false;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    private static final int DEFAULT_WAIT_SENSEOR_AUTO_BRIGHTNESS = -1;
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE = 2;
    private static final int MSG_INVALIDATE_SHORT_TERM_MODEL = 3;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    protected static final int MSG_UPDATE_BRIGHTNESS = 6;
    private static final int MSG_UPDATE_FOREGROUND_APP = 4;
    private static final int MSG_UPDATE_FOREGROUND_APP_SYNC = 5;
    private static final String TAG = "AutomaticBrightnessController";
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = true;
    private float SHORT_TERM_MODEL_THRESHOLD_RATIO;
    private IActivityTaskManager mActivityTaskManager;
    private float mAmbientBrighteningThreshold;
    private final HysteresisLevels mAmbientBrightnessThresholds;
    private float mAmbientDarkeningThreshold;
    private final int mAmbientLightHorizon;
    protected AmbientLightRingBuffer mAmbientLightRingBuffer;
    protected float mAmbientLux;
    protected boolean mAmbientLuxValid;
    private final long mBrighteningLightDebounceConfig;
    private int mBrightnessAdjustmentSampleOldBrightness;
    private float mBrightnessAdjustmentSampleOldLux;
    private boolean mBrightnessAdjustmentSamplePending;
    protected boolean mBrightnessEnlarge;
    private final BrightnessMappingStrategy mBrightnessMapper;
    protected int mBrightnessNoLimitSetByApp;
    protected final Callbacks mCallbacks;
    protected long mCryogenicActiveScreenOffIntervalInMillis;
    protected long mCryogenicLagTimeInMillis;
    protected int mCurrentLightSensorRate;
    private final long mDarkeningLightDebounceConfig;
    private int mDisplayPolicy;
    private final float mDozeScaleFactor;
    protected boolean mFirstAutoBrightness;
    protected boolean mFirstBrightnessAfterProximityNegative;
    private int mForegroundAppCategory;
    private String mForegroundAppPackageName;
    protected AutomaticBrightnessHandler mHandler;
    protected final int mInitialLightSensorRate;
    protected final boolean mIsCoverModeAbandonSensorEnable;
    protected boolean mIsFingerprintOffUnlockEnable = false;
    protected boolean mIsSetBrightnessImmediateEnable;
    protected boolean mIsWaitFristAutoBrightness = false;
    private float mLastObservedLux;
    private long mLastObservedLuxTime;
    protected final Sensor mLightSensor;
    protected long mLightSensorEnableElapsedTimeNanos;
    protected long mLightSensorEnableTime;
    protected boolean mLightSensorEnabled;
    protected final SensorEventListener mLightSensorListener;
    protected int mLightSensorWarmUpTimeConfig;
    private boolean mLoggingEnabled;
    protected int mMaxBrightnessSetByCryogenic;
    protected boolean mMaxBrightnessSetByCryogenicBypass;
    protected boolean mMaxBrightnessSetByCryogenicBypassDelayed;
    protected int mMaxBrightnessSetByThermal;
    private final int mNormalLightSensorRate;
    private PackageManager mPackageManager;
    private int mPendingForegroundAppCategory;
    private String mPendingForegroundAppPackageName;
    protected long mPowerOffTimestamp;
    protected long mPowerOnTimestamp;
    protected int mPowerState = 0;
    protected int mRecentLightSamples;
    protected final boolean mResetAmbientLuxAfterWarmUpConfig;
    protected int mScreenAutoBrightness;
    protected Spline mScreenAutoBrightnessSpline;
    private float mScreenBrighteningThreshold;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private int mScreenBrightnessRangeSetByAppMax;
    private int mScreenBrightnessRangeSetByAppMin;
    private final HysteresisLevels mScreenBrightnessThresholds;
    private float mScreenDarkeningThreshold;
    protected final SensorManager mSensorManager;
    private float mShortTermModelAnchor;
    private long mShortTermModelTimeout;
    private boolean mShortTermModelValid;
    private TaskStackListenerImpl mTaskStackListener;
    protected int mUpdateAutoBrightnessCount;
    protected boolean mWakeupForFirstAutoBrightness;
    protected boolean mWakeupFromSleep;
    private final int mWeightingIntercept;

    public interface Callbacks {
        void updateBrightness();

        void updateProximityState(boolean z);
    }

    public void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness) {
        this.mScreenBrightnessRangeSetByAppMin = backlightBrightness.min;
        this.mScreenBrightnessRangeSetByAppMax = backlightBrightness.max;
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
    }

    public AutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor lightSensor, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, HysteresisLevels ambientBrightnessThresholds, HysteresisLevels screenBrightnessThresholds, long shortTermModelTimeout, PackageManager packageManager) {
        this.mIsCoverModeAbandonSensorEnable = SystemProperties.getInt("ro.config.hw_cover_brightness", COVER_MODE_DEFAULT_BRIGHTNESS) == 0;
        this.mIsSetBrightnessImmediateEnable = false;
        this.mWakeupForFirstAutoBrightness = false;
        this.mScreenAutoBrightness = -1;
        this.mDisplayPolicy = 0;
        this.mMaxBrightnessSetByThermal = 255;
        this.mMaxBrightnessSetByCryogenic = 255;
        this.mMaxBrightnessSetByCryogenicBypass = false;
        this.mMaxBrightnessSetByCryogenicBypassDelayed = false;
        this.mPowerOnTimestamp = 0;
        this.mPowerOffTimestamp = 0;
        this.mCryogenicActiveScreenOffIntervalInMillis = 1800000;
        this.mCryogenicLagTimeInMillis = 1800000;
        this.mBrightnessNoLimitSetByApp = -1;
        this.mWakeupFromSleep = true;
        this.mBrightnessEnlarge = false;
        this.mFirstBrightnessAfterProximityNegative = false;
        this.SHORT_TERM_MODEL_THRESHOLD_RATIO = 0.6f;
        this.mLightSensorListener = new SensorEventListener() {
            /* class com.android.server.display.AutomaticBrightnessController.AnonymousClass2 */

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent event) {
                if (AutomaticBrightnessController.this.mLightSensorEnabled) {
                    long time = SystemClock.uptimeMillis();
                    float lux = event.values[0];
                    long timeStamp = event.timestamp;
                    if (AutomaticBrightnessController.DEBUG && time - AutomaticBrightnessController.this.mLightSensorEnableTime < 4000) {
                        Slog.i(AutomaticBrightnessController.TAG, "ambient lux=" + lux + ",timeStamp =" + timeStamp);
                    }
                    if ((!HwServiceFactory.shouldFilteInvalidSensorVal(lux) || !AutomaticBrightnessController.this.mIsCoverModeAbandonSensorEnable) && !AutomaticBrightnessController.this.isInValidLightSensorEvent(timeStamp, lux)) {
                        AutomaticBrightnessController.this.handleLightSensorEvent(time, lux);
                    }
                }
            }

            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mBrightnessMapper = mapper;
        this.mScreenBrightnessRangeMinimum = brightnessMin;
        this.mScreenBrightnessRangeMaximum = brightnessMax;
        this.mScreenBrightnessRangeSetByAppMin = this.mScreenBrightnessRangeMinimum;
        this.mScreenBrightnessRangeSetByAppMax = this.mScreenBrightnessRangeMaximum;
        this.mLightSensorWarmUpTimeConfig = lightSensorWarmUpTime;
        this.mDozeScaleFactor = dozeScaleFactor;
        this.mNormalLightSensorRate = lightSensorRate;
        this.mInitialLightSensorRate = initialLightSensorRate;
        this.mCurrentLightSensorRate = -1;
        this.mBrighteningLightDebounceConfig = brighteningLightDebounceConfig;
        this.mDarkeningLightDebounceConfig = darkeningLightDebounceConfig;
        this.mResetAmbientLuxAfterWarmUpConfig = resetAmbientLuxAfterWarmUpConfig;
        this.mAmbientLightHorizon = 10000;
        this.mWeightingIntercept = 10000;
        this.mAmbientBrightnessThresholds = ambientBrightnessThresholds;
        this.mScreenBrightnessThresholds = screenBrightnessThresholds;
        this.mShortTermModelTimeout = shortTermModelTimeout;
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = -1.0f;
        this.mHandler = new AutomaticBrightnessHandler(looper);
        this.mAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        this.mLightSensor = lightSensor;
        this.mActivityTaskManager = ActivityTaskManager.getService();
        this.mPackageManager = packageManager;
        this.mTaskStackListener = new TaskStackListenerImpl();
        this.mForegroundAppPackageName = null;
        this.mPendingForegroundAppPackageName = null;
        this.mForegroundAppCategory = -1;
        this.mPendingForegroundAppCategory = -1;
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return false;
        }
        this.mBrightnessMapper.setLoggingEnabled(loggingEnabled);
        this.mLoggingEnabled = loggingEnabled;
        return true;
    }

    public int getAutomaticScreenBrightness() {
        if (!this.mAmbientLuxValid && this.mIsWaitFristAutoBrightness && this.mIsFingerprintOffUnlockEnable) {
            return -1;
        }
        int brightness = this.mScreenAutoBrightness;
        if (brightness >= 0) {
            brightness = MathUtils.constrain(brightness, this.mScreenBrightnessRangeSetByAppMin, this.mScreenBrightnessRangeSetByAppMax);
        }
        int i = this.mBrightnessNoLimitSetByApp;
        if (i > 0) {
            return i;
        }
        if (!this.mMaxBrightnessSetByCryogenicBypass && !this.mMaxBrightnessSetByCryogenicBypassDelayed && brightness > this.mMaxBrightnessSetByCryogenic) {
            brightness = this.mMaxBrightnessSetByCryogenic;
        }
        setBrightnessLimitedByThermal(brightness > this.mMaxBrightnessSetByThermal);
        if (brightness > this.mMaxBrightnessSetByThermal) {
            brightness = this.mMaxBrightnessSetByThermal;
        }
        int brightness2 = getAutoBrightnessBaseInOutDoorLimit(brightness);
        if (this.mDisplayPolicy == 1) {
            return (int) (((float) brightness2) * this.mDozeScaleFactor);
        }
        return brightness2;
    }

    public boolean hasValidAmbientLux() {
        return this.mAmbientLuxValid;
    }

    public float getAutomaticScreenBrightnessAdjustment() {
        return this.mBrightnessMapper.getAutoBrightnessAdjustment();
    }

    public void configure(boolean enable, BrightnessConfiguration configuration, float brightness, boolean userChangedBrightness, float adjustment, boolean userChangedAutoBrightnessAdjustment, int displayPolicy) {
        boolean z = true;
        boolean dozing = displayPolicy == 1;
        boolean changed = setBrightnessConfiguration(configuration) | setDisplayPolicy(displayPolicy);
        if (userChangedAutoBrightnessAdjustment) {
            changed |= setAutoBrightnessAdjustment(adjustment);
        }
        if (userChangedBrightness && enable) {
            changed |= setScreenBrightnessByUser(brightness);
        }
        boolean userInitiatedChange = userChangedBrightness || userChangedAutoBrightnessAdjustment;
        if (userInitiatedChange && enable && !dozing) {
            prepareBrightnessAdjustmentSample();
        }
        if ((!enable || dozing) && (!enable || !dozing || this.mPowerState != 2 || !this.mIsFingerprintOffUnlockEnable)) {
            z = false;
        }
        if (setLightSensorEnabled(z) || changed) {
            updateAutoBrightness(false, userInitiatedChange);
        }
    }

    public boolean hasUserDataPoints() {
        return this.mBrightnessMapper.hasUserDataPoints();
    }

    public boolean isDefaultConfig() {
        return this.mBrightnessMapper.isDefaultConfig();
    }

    public BrightnessConfiguration getDefaultConfig() {
        return this.mBrightnessMapper.getDefaultConfig();
    }

    private boolean setDisplayPolicy(int policy) {
        if (this.mDisplayPolicy == policy) {
            return false;
        }
        int oldPolicy = this.mDisplayPolicy;
        this.mDisplayPolicy = policy;
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "Display policy transitioning from " + oldPolicy + " to " + policy);
        }
        if (!isInteractivePolicy(policy) && isInteractivePolicy(oldPolicy)) {
            this.mHandler.sendEmptyMessageDelayed(3, this.mShortTermModelTimeout);
            return true;
        } else if (!isInteractivePolicy(policy) || isInteractivePolicy(oldPolicy)) {
            return true;
        } else {
            this.mHandler.removeMessages(3);
            return true;
        }
    }

    private static boolean isInteractivePolicy(int policy) {
        return policy == 3 || policy == 2 || policy == 4;
    }

    private boolean setScreenBrightnessByUser(float brightness) {
        if (!this.mAmbientLuxValid) {
            return false;
        }
        this.mBrightnessMapper.addUserDataPoint(this.mAmbientLux, brightness);
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = this.mAmbientLux;
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "ShortTermModel: anchor=" + this.mShortTermModelAnchor);
        }
        return true;
    }

    public void resetShortTermModel() {
        this.mBrightnessMapper.clearUserDataPoints();
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = -1.0f;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invalidateShortTermModel() {
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "ShortTermModel: invalidate user data");
        }
        this.mShortTermModelValid = false;
    }

    public boolean setBrightnessConfiguration(BrightnessConfiguration configuration) {
        if (!this.mBrightnessMapper.setBrightnessConfiguration(configuration)) {
            return false;
        }
        resetShortTermModel();
        return true;
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Automatic Brightness Controller Configuration:");
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mScreenBrightnessRangeSetByAppMin=" + this.mScreenBrightnessRangeSetByAppMin);
        pw.println("  mScreenBrightnessRangeSetByAppMax=" + this.mScreenBrightnessRangeSetByAppMax);
        pw.println("  mDozeScaleFactor=" + this.mDozeScaleFactor);
        pw.println("  mInitialLightSensorRate=" + this.mInitialLightSensorRate);
        pw.println("  mNormalLightSensorRate=" + this.mNormalLightSensorRate);
        pw.println("  mLightSensorWarmUpTimeConfig=" + this.mLightSensorWarmUpTimeConfig);
        pw.println("  mBrighteningLightDebounceConfig=" + this.mBrighteningLightDebounceConfig);
        pw.println("  mDarkeningLightDebounceConfig=" + this.mDarkeningLightDebounceConfig);
        pw.println("  mResetAmbientLuxAfterWarmUpConfig=" + this.mResetAmbientLuxAfterWarmUpConfig);
        pw.println("  mAmbientLightHorizon=" + this.mAmbientLightHorizon);
        pw.println("  mWeightingIntercept=" + this.mWeightingIntercept);
        pw.println();
        pw.println("Automatic Brightness Controller State:");
        pw.println("  mLightSensor=" + this.mLightSensor);
        pw.println("  mLightSensorEnabled=" + this.mLightSensorEnabled);
        pw.println("  mLightSensorEnableTime=" + TimeUtils.formatUptime(this.mLightSensorEnableTime));
        pw.println("  mCurrentLightSensorRate=" + this.mCurrentLightSensorRate);
        pw.println("  mAmbientLux=" + this.mAmbientLux);
        pw.println("  mAmbientLuxValid=" + this.mAmbientLuxValid);
        pw.println("  mAmbientBrighteningThreshold=" + this.mAmbientBrighteningThreshold);
        pw.println("  mAmbientDarkeningThreshold=" + this.mAmbientDarkeningThreshold);
        pw.println("  mScreenBrighteningThreshold=" + this.mScreenBrighteningThreshold);
        pw.println("  mScreenDarkeningThreshold=" + this.mScreenDarkeningThreshold);
        pw.println("  mLastObservedLux=" + this.mLastObservedLux);
        pw.println("  mLastObservedLuxTime=" + TimeUtils.formatUptime(this.mLastObservedLuxTime));
        pw.println("  mRecentLightSamples=" + this.mRecentLightSamples);
        pw.println("  mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer);
        pw.println("  mScreenAutoBrightness=" + this.mScreenAutoBrightness);
        pw.println("  mDisplayPolicy=" + DisplayManagerInternal.DisplayPowerRequest.policyToString(this.mDisplayPolicy));
        pw.println("  mShortTermModelTimeout=" + this.mShortTermModelTimeout);
        pw.println("  mShortTermModelAnchor=" + this.mShortTermModelAnchor);
        pw.println("  mShortTermModelValid=" + this.mShortTermModelValid);
        pw.println("  mBrightnessAdjustmentSamplePending=" + this.mBrightnessAdjustmentSamplePending);
        pw.println("  mBrightnessAdjustmentSampleOldLux=" + this.mBrightnessAdjustmentSampleOldLux);
        pw.println("  mBrightnessAdjustmentSampleOldBrightness=" + this.mBrightnessAdjustmentSampleOldBrightness);
        pw.println("  mForegroundAppPackageName=" + this.mForegroundAppPackageName);
        pw.println("  mPendingForegroundAppPackageName=" + this.mPendingForegroundAppPackageName);
        pw.println("  mForegroundAppCategory=" + this.mForegroundAppCategory);
        pw.println("  mPendingForegroundAppCategory=" + this.mPendingForegroundAppCategory);
        pw.println();
        this.mBrightnessMapper.dump(pw);
        pw.println();
        this.mAmbientBrightnessThresholds.dump(pw);
        this.mScreenBrightnessThresholds.dump(pw);
    }

    /* access modifiers changed from: protected */
    public boolean setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                this.mFirstAutoBrightness = true;
                this.mUpdateAutoBrightnessCount = 0;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                this.mLightSensorEnableElapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
                this.mCurrentLightSensorRate = this.mInitialLightSensorRate;
                registerForegroundAppUpdater();
                this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mCurrentLightSensorRate * 1000, this.mHandler);
                if (this.mWakeupFromSleep) {
                    this.mHandler.sendEmptyMessageAtTime(6, this.mLightSensorEnableTime + 200);
                }
                if (DEBUG) {
                    Slog.i(TAG, "Enable LightSensor at time:mLightSensorEnableTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableElapsedTimeNanos=" + this.mLightSensorEnableElapsedTimeNanos);
                }
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mFirstAutoBrightness = false;
            this.mAmbientLuxValid = !this.mResetAmbientLuxAfterWarmUpConfig;
            this.mScreenAutoBrightness = -1;
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            this.mCurrentLightSensorRate = -1;
            this.mHandler.removeMessages(1);
            unregisterForegroundAppUpdater();
            this.mHandler.removeMessages(6);
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            if (DEBUG) {
                Slog.i(TAG, "Disable LightSensor at time:" + SystemClock.uptimeMillis());
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleLightSensorEvent(long time, float lux) {
        Trace.traceCounter(131072, "ALS", (int) lux);
        this.mHandler.removeMessages(1);
        if (this.mAmbientLightRingBuffer.size() == 0) {
            adjustLightSensorRate(this.mNormalLightSensorRate);
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    /* access modifiers changed from: protected */
    public boolean getSetbrightnessImmediateEnableForCaliTest() {
        return this.mIsSetBrightnessImmediateEnable;
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mRecentLightSamples++;
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
        this.mLastObservedLuxTime = time;
    }

    private void adjustLightSensorRate(int lightSensorRate) {
        if (lightSensorRate != this.mCurrentLightSensorRate) {
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "adjustLightSensorRate: previousRate=" + this.mCurrentLightSensorRate + ", currentRate=" + lightSensorRate);
            }
            this.mCurrentLightSensorRate = lightSensorRate;
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, lightSensorRate * 1000, this.mHandler);
        }
    }

    /* access modifiers changed from: protected */
    public boolean setAutoBrightnessAdjustment(float adjustment) {
        return this.mBrightnessMapper.setAutoBrightnessAdjustment(adjustment);
    }

    private void setAmbientLux(float lux) {
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "setAmbientLux(" + lux + ")");
        }
        if (lux < 0.0f) {
            Slog.w(TAG, "Ambient lux was negative, ignoring and setting to 0");
            lux = 0.0f;
        }
        this.mAmbientLux = lux;
        this.mAmbientBrighteningThreshold = this.mAmbientBrightnessThresholds.getBrighteningThreshold(lux);
        this.mAmbientDarkeningThreshold = this.mAmbientBrightnessThresholds.getDarkeningThreshold(lux);
        if (!this.mShortTermModelValid) {
            float f = this.mShortTermModelAnchor;
            if (f != -1.0f) {
                float f2 = this.SHORT_TERM_MODEL_THRESHOLD_RATIO;
                float minAmbientLux = f - (f * f2);
                float maxAmbientLux = f + (f2 * f);
                float f3 = this.mAmbientLux;
                if (minAmbientLux >= f3 || f3 >= maxAmbientLux) {
                    Slog.d(TAG, "ShortTermModel: reset data, ambient lux is " + this.mAmbientLux + "(" + minAmbientLux + ", " + maxAmbientLux + ")");
                    resetShortTermModel();
                    return;
                }
                if (this.mLoggingEnabled) {
                    Slog.d(TAG, "ShortTermModel: re-validate user data, ambient lux is " + minAmbientLux + " < " + this.mAmbientLux + " < " + maxAmbientLux);
                }
                this.mShortTermModelValid = true;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r9v3 long: [D('startTime' long), D('endIndex' int)] */
    private float calculateAmbientLux(long now, long horizon) {
        long j = now;
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "calculateAmbientLux(" + j + ", " + horizon + ")");
        }
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        }
        int endIndex = 0;
        long horizonStartTime = j - horizon;
        int i = 0;
        while (i < N - 1 && this.mAmbientLightRingBuffer.getTime(i + 1) <= horizonStartTime) {
            endIndex++;
            i++;
        }
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "calculateAmbientLux: selected endIndex=" + endIndex + ", point=(" + this.mAmbientLightRingBuffer.getTime(endIndex) + ", " + this.mAmbientLightRingBuffer.getLux(endIndex) + ")");
        }
        float sum = 0.0f;
        float totalWeight = 0.0f;
        long endTime = AMBIENT_LIGHT_PREDICTION_TIME_MILLIS;
        int i2 = N - 1;
        while (true) {
            if (i2 < endIndex) {
                break;
            }
            long eventTime = this.mAmbientLightRingBuffer.getTime(i2);
            if (i2 == endIndex && eventTime < horizonStartTime) {
                eventTime = horizonStartTime;
            }
            long startTime = eventTime - j;
            float weight = calculateWeight(startTime, endTime);
            if (weight < 0.0f) {
                break;
            }
            float lux = this.mAmbientLightRingBuffer.getLux(i2);
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "calculateAmbientLux: [" + startTime + ", " + endTime + "]: lux=" + lux + ", weight=" + weight);
            }
            totalWeight += weight;
            sum += lux * weight;
            endTime = startTime;
            i2--;
            j = now;
            endIndex = endIndex;
            horizonStartTime = horizonStartTime;
        }
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "calculateAmbientLux: totalWeight=" + totalWeight + ", newAmbientLux=" + (sum / totalWeight));
        }
        return sum / totalWeight;
    }

    private float calculateWeight(long startDelta, long endDelta) {
        return weightIntegral(endDelta) - weightIntegral(startDelta);
    }

    private float weightIntegral(long x) {
        return ((float) x) * ((((float) x) * 0.5f * 3.0f) + ((float) this.mWeightingIntercept));
    }

    private long nextAmbientLightBrighteningTransition(long time) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && this.mAmbientLightRingBuffer.getLux(i) > this.mAmbientBrighteningThreshold) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && this.mAmbientLightRingBuffer.getLux(i) < this.mAmbientDarkeningThreshold) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAmbientLux() {
        long time = SystemClock.uptimeMillis();
        this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        updateAmbientLux(time);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a0, code lost:
        if (r9 <= r14) goto L_0x00a2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00fa  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0103  */
    private void updateAmbientLux(long time) {
        if (!this.mAmbientLuxValid) {
            long timeWhenSensorWarmedUp = ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime;
            if (time < timeWhenSensorWarmedUp) {
                if (this.mLoggingEnabled) {
                    Slog.d(TAG, "updateAmbientLux: Sensor not ready yet: time=" + time + ", timeWhenSensorWarmedUp=" + timeWhenSensorWarmedUp);
                }
                this.mHandler.sendEmptyMessageAtTime(1, timeWhenSensorWarmedUp);
                return;
            }
            setAmbientLux(calculateAmbientLux(time, BRIGHTENING_LIGHT_DEBOUNCE));
            this.mAmbientLuxValid = true;
            if (this.mWakeupFromSleep) {
                this.mWakeupFromSleep = false;
                this.mFirstAutoBrightness = true;
            }
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux);
            }
            updateAutoBrightness(true, false);
        }
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        float slowAmbientLux = calculateAmbientLux(time, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        float fastAmbientLux = calculateAmbientLux(time, BRIGHTENING_LIGHT_DEBOUNCE);
        float f = this.mAmbientBrighteningThreshold;
        if (slowAmbientLux < f || fastAmbientLux < f || nextBrightenTransition > time) {
            float f2 = this.mAmbientDarkeningThreshold;
            if (slowAmbientLux <= f2) {
                if (fastAmbientLux <= f2) {
                }
            }
            long nextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
            long nextTransitionTime2 = nextTransitionTime <= time ? nextTransitionTime : ((long) this.mNormalLightSensorRate) + time;
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + nextTransitionTime2 + TimeUtils.formatUptime(nextTransitionTime2));
            }
            this.mHandler.sendEmptyMessageAtTime(1, nextTransitionTime2);
        }
        setAmbientLux(fastAmbientLux);
        if (this.mLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateAmbientLux: ");
            sb.append(fastAmbientLux > this.mAmbientLux ? "Brightened" : "Darkened");
            sb.append(": mBrighteningLuxThreshold=");
            sb.append(this.mAmbientBrighteningThreshold);
            sb.append(", mAmbientLightRingBuffer=");
            sb.append(this.mAmbientLightRingBuffer);
            sb.append(", mAmbientLux=");
            sb.append(this.mAmbientLux);
            Slog.d(TAG, sb.toString());
        }
        updateAutoBrightness(true, false);
        nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        long nextTransitionTime3 = Math.min(nextDarkenTransition, nextBrightenTransition);
        if (nextTransitionTime3 <= time) {
        }
        if (this.mLoggingEnabled) {
        }
        this.mHandler.sendEmptyMessageAtTime(1, nextTransitionTime2);
    }

    /* access modifiers changed from: protected */
    public void updateAutoBrightness(boolean sendUpdate, boolean isManuallySet) {
        if (this.mAmbientLuxValid) {
            int newScreenAutoBrightness = getAutoBrightnessBaseInOutDoorLimit(getAdjustLightValByPgMode(getPowerSavingBrightness(clampScreenBrightness(Math.round(255.0f * this.mScreenAutoBrightnessSpline.interpolate(this.mAmbientLux))))));
            if (this.mScreenAutoBrightness != -1 && !isManuallySet && ((float) newScreenAutoBrightness) > this.mScreenDarkeningThreshold && ((float) newScreenAutoBrightness) < this.mScreenBrighteningThreshold && DEBUG) {
                Slog.i(TAG, "ignoring newScreenAutoBrightness: " + this.mScreenDarkeningThreshold + " < " + newScreenAutoBrightness + " < " + this.mScreenBrighteningThreshold);
            }
            if (this.mScreenAutoBrightness != newScreenAutoBrightness || this.mFirstAutoBrightness || this.mFirstBrightnessAfterProximityNegative) {
                if (DEBUG) {
                    Slog.i(TAG, "updateAutoBrightness: mScreenAutoBrightness=" + this.mScreenAutoBrightness + ", newScreenAutoBrightness=" + newScreenAutoBrightness);
                }
                this.mBrightnessEnlarge = newScreenAutoBrightness > this.mScreenAutoBrightness;
                this.mScreenAutoBrightness = newScreenAutoBrightness;
                this.mScreenBrighteningThreshold = this.mScreenBrightnessThresholds.getBrighteningThreshold((float) newScreenAutoBrightness);
                this.mScreenDarkeningThreshold = this.mScreenBrightnessThresholds.getDarkeningThreshold((float) newScreenAutoBrightness);
                this.mFirstAutoBrightness = false;
                this.mFirstBrightnessAfterProximityNegative = false;
                if (this.mWakeupForFirstAutoBrightness) {
                    this.mWakeupForFirstAutoBrightness = false;
                    Slog.i(TAG, "mWakeupForFirstAutoBrightness = false");
                }
                this.mUpdateAutoBrightnessCount++;
                if (this.mUpdateAutoBrightnessCount == Integer.MAX_VALUE) {
                    this.mUpdateAutoBrightnessCount = 2;
                    Slog.i(TAG, "mUpdateAutoBrightnessCount == Integer.MAX_VALUE,so set it be 2");
                }
                if (sendUpdate) {
                    this.mCallbacks.updateBrightness();
                }
            }
        } else if (DEBUG) {
            Slog.i(TAG, "mAmbientLuxValid= false,sensor is not ready");
        }
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void prepareBrightnessAdjustmentSample() {
        if (!this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = true;
            this.mBrightnessAdjustmentSampleOldLux = this.mAmbientLuxValid ? this.mAmbientLux : -1.0f;
            this.mBrightnessAdjustmentSampleOldBrightness = this.mScreenAutoBrightness;
        } else {
            this.mHandler.removeMessages(2);
        }
        this.mHandler.sendEmptyMessageDelayed(2, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    private void cancelBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            this.mHandler.removeMessages(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void collectBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            if (this.mAmbientLuxValid && this.mScreenAutoBrightness >= 0) {
                if (this.mLoggingEnabled) {
                    Slog.d(TAG, "Auto-brightness adjustment changed by user: lux=" + this.mAmbientLux + ", brightness=" + this.mScreenAutoBrightness + ", ring=" + this.mAmbientLightRingBuffer);
                }
                EventLog.writeEvent((int) EventLogTags.AUTO_BRIGHTNESS_ADJ, Float.valueOf(this.mBrightnessAdjustmentSampleOldLux), Integer.valueOf(this.mBrightnessAdjustmentSampleOldBrightness), Float.valueOf(this.mAmbientLux), Integer.valueOf(this.mScreenAutoBrightness));
            }
        }
    }

    private void registerForegroundAppUpdater() {
        try {
            this.mActivityTaskManager.registerTaskStackListener(this.mTaskStackListener);
            updateForegroundApp();
        } catch (RemoteException e) {
            if (this.mLoggingEnabled) {
                Slog.e(TAG, "Failed to register foreground app updater: " + e);
            }
        }
    }

    private void unregisterForegroundAppUpdater() {
        try {
            this.mActivityTaskManager.unregisterTaskStackListener(this.mTaskStackListener);
        } catch (RemoteException e) {
        }
        this.mForegroundAppPackageName = null;
        this.mForegroundAppCategory = -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForegroundApp() {
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "Attempting to update foreground app");
        }
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.server.display.AutomaticBrightnessController.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    ActivityManager.StackInfo info = AutomaticBrightnessController.this.mActivityTaskManager.getFocusedStackInfo();
                    if (info == null) {
                        return;
                    }
                    if (info.topActivity != null) {
                        String packageName = info.topActivity.getPackageName();
                        if (AutomaticBrightnessController.this.mForegroundAppPackageName == null || !AutomaticBrightnessController.this.mForegroundAppPackageName.equals(packageName)) {
                            AutomaticBrightnessController.this.mPendingForegroundAppPackageName = packageName;
                            AutomaticBrightnessController.this.mPendingForegroundAppCategory = -1;
                            try {
                                ApplicationInfo app = AutomaticBrightnessController.this.mPackageManager.getApplicationInfo(packageName, DumpState.DUMP_CHANGES);
                                AutomaticBrightnessController.this.mPendingForegroundAppCategory = app.category;
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                            AutomaticBrightnessController.this.mHandler.sendEmptyMessage(5);
                        }
                    }
                } catch (RemoteException e2) {
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForegroundAppSync() {
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "Updating foreground app: packageName=" + this.mPendingForegroundAppPackageName + ", category=" + this.mPendingForegroundAppCategory);
        }
        this.mForegroundAppPackageName = this.mPendingForegroundAppPackageName;
        this.mPendingForegroundAppPackageName = null;
        this.mForegroundAppCategory = this.mPendingForegroundAppCategory;
        this.mPendingForegroundAppCategory = -1;
        updateAutoBrightness(true, false);
    }

    /* access modifiers changed from: protected */
    public final class AutomaticBrightnessHandler extends Handler {
        public AutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AutomaticBrightnessController.this.updateAmbientLux();
                    return;
                case 2:
                    AutomaticBrightnessController.this.collectBrightnessAdjustmentSample();
                    return;
                case 3:
                    AutomaticBrightnessController.this.invalidateShortTermModel();
                    return;
                case 4:
                    AutomaticBrightnessController.this.updateForegroundApp();
                    return;
                case 5:
                    AutomaticBrightnessController.this.updateForegroundAppSync();
                    return;
                case 6:
                    AutomaticBrightnessController.this.updateBrightnessIfNoAmbientLuxReported();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class TaskStackListenerImpl extends TaskStackListener {
        TaskStackListenerImpl() {
        }

        public void onTaskStackChanged() {
            AutomaticBrightnessController.this.mHandler.sendEmptyMessage(4);
        }
    }

    /* access modifiers changed from: protected */
    public static final class AmbientLightRingBuffer {
        private static final float BUFFER_SLACK = 1.5f;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux;
        private long[] mRingTime;
        private int mStart;

        public AmbientLightRingBuffer(long lightSensorRate, int ambientLightHorizon) {
            this.mCapacity = (int) Math.ceil((double) ((((float) ambientLightHorizon) * BUFFER_SLACK) / ((float) lightSensorRate)));
            int i = this.mCapacity;
            this.mRingLux = new float[i];
            this.mRingTime = new long[i];
        }

        public float getLux(int index) {
            return this.mRingLux[offsetOf(index)];
        }

        public long getTime(int index) {
            return this.mRingTime[offsetOf(index)];
        }

        public void push(long time, float lux) {
            int next = this.mEnd;
            int i = this.mCount;
            int i2 = this.mCapacity;
            if (i == i2) {
                int newSize = i2 * 2;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int i3 = this.mStart;
                int length = i2 - i3;
                System.arraycopy(this.mRingLux, i3, newRingLux, 0, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
                int i4 = this.mStart;
                if (i4 != 0) {
                    System.arraycopy(this.mRingLux, 0, newRingLux, length, i4);
                    System.arraycopy(this.mRingTime, 0, newRingTime, length, this.mStart);
                }
                this.mRingLux = newRingLux;
                this.mRingTime = newRingTime;
                next = this.mCapacity;
                this.mCapacity = newSize;
                this.mStart = 0;
            }
            this.mRingTime[next] = time;
            this.mRingLux[next] = lux;
            this.mEnd = next + 1;
            if (this.mEnd == this.mCapacity) {
                this.mEnd = 0;
            }
            this.mCount++;
        }

        public void prune(long horizon) {
            if (this.mCount != 0) {
                while (this.mCount > 1) {
                    int next = this.mStart + 1;
                    int i = this.mCapacity;
                    if (next >= i) {
                        next -= i;
                    }
                    if (this.mRingTime[next] > horizon) {
                        break;
                    }
                    this.mStart = next;
                    this.mCount--;
                }
                long[] jArr = this.mRingTime;
                int i2 = this.mStart;
                if (jArr[i2] < horizon) {
                    jArr[i2] = horizon;
                }
            }
        }

        public int size() {
            return this.mCount;
        }

        public void clear() {
            this.mStart = 0;
            this.mEnd = 0;
            this.mCount = 0;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append('[');
            int i = 0;
            while (true) {
                int i2 = this.mCount;
                if (i < i2) {
                    long next = i + 1 < i2 ? getTime(i + 1) : SystemClock.uptimeMillis();
                    if (i != 0) {
                        buf.append(", ");
                    }
                    buf.append(getLux(i));
                    buf.append(" / ");
                    buf.append(next - getTime(i));
                    buf.append("ms");
                    i++;
                } else {
                    buf.append(']');
                    return buf.toString();
                }
            }
        }

        private int offsetOf(int index) {
            if (index >= this.mCount || index < 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            int index2 = index + this.mStart;
            int i = this.mCapacity;
            if (index2 >= i) {
                return index2 - i;
            }
            return index2;
        }
    }

    /* access modifiers changed from: protected */
    public long getNextAmbientLightBrighteningTime(long earliedtime) {
        return BRIGHTENING_LIGHT_DEBOUNCE + earliedtime;
    }

    /* access modifiers changed from: protected */
    public long getNextAmbientLightDarkeningTime(long earliedtime) {
        return DARKENING_LIGHT_DEBOUNCE + earliedtime;
    }

    public void setPowerStatus(boolean powerStatus) {
    }

    /* access modifiers changed from: protected */
    public boolean isInValidLightSensorEvent(long time, float lux) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateIntervenedAutoBrightness(int brightness) {
        this.mScreenAutoBrightness = brightness;
        if (DEBUG) {
            Slog.i(TAG, "update IntervenedAutoBrightness:mScreenAutoBrightness= " + this.mScreenAutoBrightness);
        }
    }

    public long getLightSensorEnableTime() {
        return this.mLightSensorEnableTime;
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessIfNoAmbientLuxReported() {
    }

    public int getUpdateAutoBrightnessCount() {
        return this.mUpdateAutoBrightnessCount;
    }

    public void updateCurrentUserId(int userId) {
    }

    public void updatePowerStateAndPolicy(int state, int policy) {
    }

    public boolean getPowerStatus() {
        return false;
    }

    public void setCoverModeStatus(boolean isclosed) {
    }

    public boolean getCoverModeFastResponseFlag() {
        return false;
    }

    public void setBackSensorCoverModeBrightness(int brightness) {
    }

    public void setCameraModeBrightnessLineEnable(boolean cameraModeBrightnessLineEnable) {
    }

    public boolean getCameraModeChangeAnimationEnable() {
        return false;
    }

    public boolean getReadingModeChangeAnimationEnable() {
        return false;
    }

    public boolean getReadingModeBrightnessLineEnable() {
        return false;
    }

    public boolean getRebootAutoModeEnable() {
        return false;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
    }

    public boolean getOutdoorAnimationFlag() {
        return false;
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        return 0;
    }

    public void setMaxBrightnessFromThermal(int brightness) {
    }

    public void setMaxBrightnessFromCryogenic(int brightness) {
    }

    public int setScreenBrightnessMappingToIndoorMax(int brightness) {
        return brightness;
    }

    public void setManualModeEnableForPg(boolean manualModeEnableForPg) {
    }

    public boolean getRebootFirstBrightnessAnimationEnable() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getAdjustLightValByPgMode(int rawLightVal) {
        return rawLightVal;
    }

    /* access modifiers changed from: protected */
    public int getPowerSavingBrightness(int brightness) {
        return brightness;
    }

    /* access modifiers changed from: protected */
    public void setBrightnessLimitedByThermal(boolean isLimited) {
    }

    public void setBrightnessNoLimit(int brightness, int time) {
    }

    public int getAutoBrightnessBaseInOutDoorLimit(int brightness) {
        return brightness;
    }

    public boolean getDarkAdaptDimmingEnable() {
        return false;
    }

    public void clearDarkAdaptDimmingEnable() {
    }

    public boolean getAutoPowerSavingUseManualAnimationTimeEnable() {
        return false;
    }

    public boolean getAutoPowerSavingAnimationEnable() {
        return false;
    }

    public void setAutoPowerSavingAnimationEnable(boolean enable) {
    }

    public void getUserDragInfo(Bundle data) {
    }

    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
    }

    public void updateNewBrightnessCurveTmp() {
    }

    public void updateNewBrightnessCurve() {
    }

    public List<PointF> getCurrentDefaultNewCurveLine() {
        return null;
    }

    public boolean getAnimationGameChangeEnable() {
        return false;
    }

    public float getAutomaticScreenBrightnessAdjustmentNew(int brightness) {
        return 0.0f;
    }

    public void updateBrightnessModeChangeManualState(boolean enable) {
    }

    public boolean getFastDarkenDimmingEnable() {
        return false;
    }

    public boolean getKeyguardUnlockedFastDarkenDimmingEnable() {
        return false;
    }

    public void setVideoPlayStatus(boolean isVideoPlay) {
    }

    public void setMaxBrightnessNitFromThermal(int brightnessNit) {
    }

    public int getCurrentBrightnessNit() {
        return 0;
    }

    public int getTheramlMaxBrightnessNit() {
        return 0;
    }

    public int getMinBrightnessNit() {
        return 0;
    }

    public int getMaxBrightnessNit() {
        return 0;
    }

    public boolean getAutoBrightnessEnable() {
        return this.mLightSensorEnabled;
    }

    public int getAmbientLux() {
        return 0;
    }

    public int getBrightnessLevel(int lux) {
        return 0;
    }

    public boolean getGameDisableAutoBrightnessModeKeepOffsetEnable() {
        return false;
    }

    public void setGameDisableAutoBrightnessModeKeepOffsetEnable(boolean enable) {
    }

    public boolean getGameDisableAutoBrightnessModeStatus() {
        return false;
    }

    public boolean getNightUpPowerOnWithDimmingEnable() {
        return false;
    }

    public void setFrontCameraAppEnableState(boolean enable) {
    }

    public boolean getFrontCameraDimmingEnable() {
        return false;
    }

    public void resetCurrentBrightness() {
    }

    public void resetCurrentBrightnessFromOff() {
    }

    public void setCurrentBrightnessOff() {
    }

    public int getAutoModeSeekBarMaxBrightness() {
        return 255;
    }
}
