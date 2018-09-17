package com.android.server.display;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal.DisplayPowerCallbacks;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager.BacklightBrightness;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.pc.IHwPCManager;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.HwLog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.ScreenOffListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import com.android.internal.app.IBatteryStats;
import com.android.server.FingerprintUnlockDataCollector;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IDisplayEngineInterface;
import com.android.server.HwServiceFactory.IHwAutomaticBrightnessController;
import com.android.server.HwServiceFactory.IHwNormalizedManualBrightnessController;
import com.android.server.HwServiceFactory.IHwRampAnimator;
import com.android.server.HwServiceFactory.IHwSmartBackLightController;
import com.android.server.LocalServices;
import com.android.server.am.BatteryStatsService;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks;
import com.android.server.display.RampAnimator.Listener;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import java.io.PrintWriter;
import java.util.List;

final class DisplayPowerController implements Callbacks, ManualBrightnessCallbacks {
    static final /* synthetic */ boolean -assertionsDisabled = (DisplayPowerController.class.desiredAssertionStatus() ^ 1);
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_RAMP_RATE_FAST = 200;
    private static final int BRIGHTNESS_RAMP_RATE_SLOW = 40;
    private static final int COLOR_FADE_OFF_ANIMATION_DURATION_MILLIS = 150;
    private static final int COLOR_FADE_ON_ANIMATION_DURATION_MILLIS = 250;
    private static boolean DEBUG = false;
    private static boolean DEBUG_Controller = false;
    private static boolean DEBUG_FPLOG = false;
    private static final boolean DEBUG_PRETEND_PROXIMITY_SENSOR_ABSENT = false;
    private static final int GET_RUNNING_TASKS_FROM_AMS_WARNING_DURATION_MILLIS = 500;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 1000;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_OFF_UNBLOCKED = 4;
    private static final int MSG_SCREEN_ON_FOR_KEYGUARD_DISMISS_DONE = 5;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_UPDATE_POWER_STATE = 1;
    private static boolean NEED_NEW_BRIGHTNESS_PROCESS = false;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_SENSOR_NEGATIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_SENSOR_POSITIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final int RAMP_STATE_SKIP_AUTOBRIGHT = 2;
    private static final int RAMP_STATE_SKIP_INITIAL = 1;
    private static final int RAMP_STATE_SKIP_NONE = 0;
    private static final int REPORTED_TO_POLICY_SCREEN_OFF = 0;
    private static final int REPORTED_TO_POLICY_SCREEN_ON = 2;
    private static final int REPORTED_TO_POLICY_SCREEN_TURNING_OFF = 3;
    private static final int REPORTED_TO_POLICY_SCREEN_TURNING_ON = 1;
    private static final int SCREEN_DIM_MINIMUM_REDUCTION = 10;
    private static final String SCREEN_OFF_BLOCKED_TRACE_NAME = "Screen off blocked";
    private static final String SCREEN_ON_BLOCKED_TRACE_NAME = "Screen on blocked";
    private static final int SCREEN_STATE_HOLD_ON = 2;
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 1;
    private static final String TAG = "DisplayPowerController";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final boolean USE_COLOR_FADE_ON_ANIMATION = false;
    private static final String USE_SENSORHUB_LABC_PROP = "use_sensorhub_labc";
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    private FingerprintUnlockDataCollector fpDataCollector;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private boolean mAnimationEnabled;
    private final AnimatorListener mAnimatorListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            DisplayPowerController.this.sendUpdatePowerState();
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
        }
    };
    private boolean mAppliedAutoBrightness;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private boolean mAutoBrightnessAdjustmentChanged = false;
    private boolean mAutoBrightnessEnabled = false;
    private Light mAutoCustomBackLight;
    private AutomaticBrightnessController mAutomaticBrightnessController;
    private Light mBackLight;
    private final IBatteryStats mBatteryStats;
    private final DisplayBlanker mBlanker;
    private boolean mBrightnessModeChangeNoClearOffsetEnable = false;
    private boolean mBrightnessModeChanged = false;
    private final int mBrightnessRampRateFast;
    private final int mBrightnessRampRateSlow;
    private final DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener = new Runnable() {
        public void run() {
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    private final Context mContext;
    private boolean mCoverModeAnimationFast = false;
    private int mCurrentUserId = 0;
    private boolean mCurrentUserIdChange = false;
    private IDisplayEngineInterface mDisplayEngineInterface = null;
    private boolean mDisplayReadyLocked;
    private boolean mDozing;
    private int mFeedBack = 0;
    private int mGlobalAlpmState = -1;
    private final DisplayControllerHandler mHandler;
    private IHwSmartBackLightController mHwSmartBackLightController;
    private boolean mImmeBright;
    private int mInitialAutoBrightness;
    private boolean mIsCoverModeClosed = true;
    private int mIsScreenOn = 0;
    private boolean mKeyguardIsLocked = false;
    private boolean mLABCEnabled;
    private Sensor mLABCSensor;
    private boolean mLABCSensorEnabled;
    private final SensorEventListener mLABCSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mLABCSensorEnabled && DisplayPowerController.this.mLABCEnabled) {
                int Backlight = (int) event.values[0];
                int Ambientlight = (int) event.values[1];
                int FeedBack = (int) event.values[2];
                if (DisplayPowerController.DEBUG) {
                    Slog.d(DisplayPowerController.TAG, "[LABC] onSensorChanged----BL =  " + Backlight + ", AL=  " + Ambientlight + ", FeedBack=  " + FeedBack);
                }
                if (Backlight >= 0) {
                    DisplayPowerController.this.mPendingBacklight = Backlight;
                    DisplayPowerController.this.sendUpdatePowerState();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mLastBacklight = 102;
    private boolean mLastStatus = false;
    private boolean mLastWaitBrightnessMode;
    private boolean mLightSensorOnEnable = false;
    private final LightsManager mLights;
    private final Object mLock = new Object();
    private ManualBrightnessController mManualBrightnessController = null;
    private Light mManualCustomBackLight;
    private int mMillisecond;
    private boolean mModeToAutoNoClearOffsetEnable = false;
    private final Runnable mOnProximityNegativeRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityNegative();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityPositiveRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityPositive();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnStateChangedRunnable = new Runnable() {
        public void run() {
            DisplayPowerController.this.mCallbacks.onStateChanged();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private boolean mOutdoorAnimationFlag = false;
    private int mPendingBacklight = -1;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPendingRequestChangedLocked;
    private DisplayPowerRequest mPendingRequestLocked;
    private boolean mPendingScreenOff;
    private ScreenOffUnblocker mPendingScreenOffUnblocker;
    private ScreenOnForKeyguardDismissUnblocker mPendingScreenOnForKeyguardDismissUnblocker;
    private ScreenOnUnblocker mPendingScreenOnUnblocker;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    private boolean mPowerPolicyChangeFromDimming;
    private DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private boolean mPoweroffModeChangeAutoEnable = false;
    private int mProximity = -1;
    private boolean mProximityPositive = false;
    private Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mProximitySensorEnabled) {
                long time = SystemClock.uptimeMillis();
                float distance = event.values[0];
                boolean positive = distance >= 0.0f && distance < DisplayPowerController.this.mProximityThreshold;
                DisplayPowerController.this.handleProximitySensorEvent(time, positive);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private float mProximityThreshold;
    private final Listener mRampAnimatorListener = new Listener() {
        public void onAnimationEnd() {
            if (DisplayPowerController.this.mUsingHwSmartBackLightController && DisplayPowerController.this.mSmartBackLightEnabled) {
                DisplayPowerController.this.mHwSmartBackLightController.updateBrightnessState(1);
            }
            if (DisplayPowerController.this.mPowerPolicyChangeFromDimming) {
                DisplayPowerController.this.mPowerPolicyChangeFromDimming = false;
                DisplayPowerController.this.mBackLight.writeAutoBrightnessDbEnable(true);
            }
            if (DisplayPowerController.this.mProximityPositive) {
                try {
                    DisplayPowerController.this.mBatteryStats.noteScreenBrightness(DisplayPowerController.this.mScreenBrightnessRampAnimator.getCurrentBrightness());
                } catch (RemoteException e) {
                }
            }
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private boolean mRebootWakeupFromSleep = true;
    private int mReportedScreenStateToPolicy;
    private boolean mSREEnabled = false;
    private final int mScreenBrightnessDarkConfig;
    private final int mScreenBrightnessDimConfig;
    private final int mScreenBrightnessDozeConfig;
    private RampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOffBlockStartRealTime;
    private long mScreenOnBlockStartRealTime;
    private long mScreenOnForKeyguardDismissBlockStartRealTime;
    private final SensorManager mSensorManager;
    private int mSetAutoBackLight = -1;
    private int mSkipRampState = 0;
    private final boolean mSkipScreenOnBrightnessRamp;
    private boolean mSmartBackLightEnabled;
    private boolean mSmartBackLightSupported;
    private boolean mUnfinishedBusiness;
    private boolean mUseSensorHubLABC = false;
    private boolean mUseSoftwareAutoBrightnessConfig;
    private boolean mUsingHwSmartBackLightController = false;
    private boolean mUsingSRE = false;
    private boolean mWaitingForNegativeProximity;
    private boolean mWakeupFromSleep = true;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private boolean mfastAnimtionFlag = false;

    private final class DisplayControllerHandler extends Handler {
        public DisplayControllerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 2:
                    DisplayPowerController.this.debounceProximitySensor();
                    return;
                case 3:
                    if (DisplayPowerController.this.mPendingScreenOnUnblocker == msg.obj) {
                        DisplayPowerController.this.unblockScreenOn();
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                case 4:
                    if (DisplayPowerController.this.mPendingScreenOffUnblocker == msg.obj) {
                        DisplayPowerController.this.unblockScreenOff();
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                case 5:
                    if (DisplayPowerController.this.mPendingScreenOnForKeyguardDismissUnblocker == msg.obj) {
                        DisplayPowerController.this.mImmeBright = true;
                        DisplayPowerController.this.unblockScreenOnForKeyguardDismiss();
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private final class ScreenOffUnblocker implements ScreenOffListener {
        /* synthetic */ ScreenOffUnblocker(DisplayPowerController this$0, ScreenOffUnblocker -this1) {
            this();
        }

        private ScreenOffUnblocker() {
        }

        public void onScreenOff() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(4, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    private final class ScreenOnForKeyguardDismissUnblocker implements KeyguardDismissDoneListener {
        /* synthetic */ ScreenOnForKeyguardDismissUnblocker(DisplayPowerController this$0, ScreenOnForKeyguardDismissUnblocker -this1) {
            this();
        }

        private ScreenOnForKeyguardDismissUnblocker() {
        }

        public void onKeyguardDismissDone() {
            long delay = SystemClock.elapsedRealtime() - DisplayPowerController.this.mScreenOnForKeyguardDismissBlockStartRealTime;
            if (delay > 1000) {
                Slog.i(DisplayPowerController.TAG, "fingerunlock--onKeyguardDismissDone delay " + delay);
            }
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(5, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    private final class ScreenOnUnblocker implements ScreenOnListener {
        /* synthetic */ ScreenOnUnblocker(DisplayPowerController this$0, ScreenOnUnblocker -this1) {
            this();
        }

        private ScreenOnUnblocker() {
        }

        public void onScreenOn() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(3, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    static {
        boolean z;
        boolean z2 = true;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        DEBUG = z;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 3);
        } else {
            z = false;
        }
        DEBUG_Controller = z;
        if (!DEBUG) {
            z2 = false;
        }
        DEBUG_FPLOG = z2;
    }

    private void setPowerStatus(boolean powerStatus) {
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.setPowerStatus(powerStatus);
        }
    }

    public void setAodAlpmState(int globalState) {
        Slog.i(TAG, "mGlobalAlpmState = " + globalState);
        this.mGlobalAlpmState = globalState;
        if (this.mGlobalAlpmState == 1) {
            sendUpdatePowerState();
        }
    }

    public void setBacklightBrightness(BacklightBrightness backlightBrightness) {
        this.mAutomaticBrightnessController.setBacklightBrightness(backlightBrightness);
    }

    public void setCameraModeBrightnessLineEnable(boolean cameraModeBrightnessLineEnable) {
        this.mAutomaticBrightnessController.setCameraModeBrightnessLineEnable(cameraModeBrightnessLineEnable);
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        this.mAutomaticBrightnessController.updateAutoBrightnessAdjustFactor(adjustFactor);
    }

    public int getMaxBrightnessForSeekbar() {
        return this.mManualBrightnessController.getMaxBrightnessForSeekbar();
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
        if (DEBUG) {
            Slog.i(TAG, "setAnimationTime animationEnabled=" + animationEnabled + ",millisecond=" + millisecond);
        }
        this.mAnimationEnabled = animationEnabled;
        this.mMillisecond = millisecond;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
        this.mKeyguardIsLocked = isLocked;
        this.mAutomaticBrightnessController.setKeyguardLockedStatus(this.mKeyguardIsLocked);
    }

    public boolean getRebootAutoModeEnable() {
        return this.mAutomaticBrightnessController.getRebootAutoModeEnable();
    }

    public DisplayPowerController(Context context, DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
        this.mHandler = new DisplayControllerHandler(handler.getLooper());
        this.mCallbacks = callbacks;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mLights = (LightsManager) LocalServices.getService(LightsManager.class);
        this.mSensorManager = sensorManager;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mBlanker = blanker;
        this.mContext = context;
        this.mBackLight = this.mLights.getLight(0);
        NEED_NEW_BRIGHTNESS_PROCESS = this.mBackLight.isHighPrecision();
        Resources resources = context.getResources();
        int screenBrightnessSettingMinimum = clampAbsoluteBrightness(resources.getInteger(17694849));
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources.getInteger(17694843));
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources.getInteger(17694842));
        this.mScreenBrightnessDarkConfig = clampAbsoluteBrightness(resources.getInteger(17694841));
        if (this.mScreenBrightnessDarkConfig > this.mScreenBrightnessDimConfig) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessDim (" + this.mScreenBrightnessDimConfig + ").");
        }
        if (this.mScreenBrightnessDarkConfig > screenBrightnessSettingMinimum) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessSettingMinimum (" + screenBrightnessSettingMinimum + ").");
        }
        int screenBrightnessRangeMinimum = Math.min(Math.min(screenBrightnessSettingMinimum, this.mScreenBrightnessDimConfig), this.mScreenBrightnessDarkConfig);
        this.mScreenBrightnessRangeMaximum = 255;
        this.mUseSoftwareAutoBrightnessConfig = resources.getBoolean(17956894);
        this.mAllowAutoBrightnessWhileDozingConfig = resources.getBoolean(17956872);
        this.mUseSensorHubLABC = SystemProperties.getBoolean(USE_SENSORHUB_LABC_PROP, false);
        this.mBrightnessRampRateFast = resources.getInteger(17694747);
        this.mBrightnessRampRateSlow = resources.getInteger(17694748);
        this.mSkipScreenOnBrightnessRamp = resources.getBoolean(17957008);
        int lightSensorRate = resources.getInteger(17694737);
        int initialLightSensorRate = resources.getInteger(17694736);
        if (initialLightSensorRate == -1) {
            initialLightSensorRate = lightSensorRate;
        } else if (initialLightSensorRate > lightSensorRate) {
            Slog.w(TAG, "Expected config_autoBrightnessInitialLightSensorRate (" + initialLightSensorRate + ") to be less than or equal to " + "config_autoBrightnessLightSensorRate (" + lightSensorRate + ").");
        }
        long brighteningLightDebounce = (long) resources.getInteger(17694734);
        long darkeningLightDebounce = (long) resources.getInteger(17694735);
        boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources.getBoolean(17956890);
        int ambientLightHorizon = resources.getInteger(17694733);
        float autoBrightnessAdjustmentMaxGamma = resources.getFraction(18022400, 1, 1);
        HysteresisLevels hysteresisLevels = new HysteresisLevels(resources.getIntArray(17236005), resources.getIntArray(17236006), resources.getIntArray(17236007));
        if (this.mUseSoftwareAutoBrightnessConfig) {
            int[] lux = resources.getIntArray(17235986);
            int[] screenBrightness = resources.getIntArray(17235985);
            int lightSensorWarmUpTimeConfig = resources.getInteger(17694798);
            float dozeScaleFactor = resources.getFraction(18022403, 1, 1);
            if (!this.mUseSensorHubLABC) {
                Spline screenAutoBrightnessSpline = createAutoBrightnessSpline(lux, screenBrightness);
                if (screenAutoBrightnessSpline == null) {
                    Slog.e(TAG, "Error in config.xml.  config_autoBrightnessLcdBacklightValues (size " + screenBrightness.length + ") " + "must be monotic and have exactly one more entry than " + "config_autoBrightnessLevels (size " + lux.length + ") " + "which must be strictly increasing.  " + "Auto-brightness will be disabled.");
                    this.mUseSoftwareAutoBrightnessConfig = false;
                } else {
                    int bottom = clampAbsoluteBrightness(screenBrightness[0]);
                    if (this.mScreenBrightnessDarkConfig > bottom) {
                        Slog.w(TAG, "config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") should be less than or equal to the first value of " + "config_autoBrightnessLcdBacklightValues (" + bottom + ").");
                    }
                    if (bottom < screenBrightnessRangeMinimum) {
                        screenBrightnessRangeMinimum = bottom;
                    }
                    IHwAutomaticBrightnessController iadm = HwServiceFactory.getHuaweiAutomaticBrightnessController();
                    if (iadm != null) {
                        this.mAutomaticBrightnessController = iadm.getInstance(this, handler.getLooper(), sensorManager, screenAutoBrightnessSpline, lightSensorWarmUpTimeConfig, screenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma, hysteresisLevels, this.mContext);
                    } else {
                        this.mAutomaticBrightnessController = new AutomaticBrightnessController(this, handler.getLooper(), sensorManager, screenAutoBrightnessSpline, lightSensorWarmUpTimeConfig, screenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma, hysteresisLevels);
                    }
                }
            }
            this.fpDataCollector = FingerprintUnlockDataCollector.getInstance();
        }
        this.mScreenBrightnessRangeMinimum = screenBrightnessRangeMinimum;
        this.mColorFadeFadesConfig = resources.getBoolean(17956887);
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), 5.0f);
        }
        this.mDisplayEngineInterface = HwServiceFactory.getDisplayEngineInterface();
        if (this.mDisplayEngineInterface != null) {
            this.mDisplayEngineInterface.initialize();
            this.mUsingSRE = this.mDisplayEngineInterface.getSupported("FEATURE_SRE");
            Slog.i(TAG, "DisplayEngineInterface getSupported SRE:" + this.mUsingSRE);
        }
        int smartBackLightConfig = SystemProperties.getInt("ro.config.hw_smart_backlight", 1);
        if (this.mUsingSRE || smartBackLightConfig == 1) {
            if (this.mUsingSRE) {
                Slog.i(TAG, "Use SRE instead of SBL");
            } else {
                this.mSmartBackLightSupported = true;
                if (DEBUG) {
                    Slog.i(TAG, "get ro.config.hw_smart_backlight = 1");
                }
            }
            int smartBackLightSetting = System.getInt(this.mContext.getContentResolver(), "smart_backlight_enable", -1);
            if (smartBackLightSetting == -1) {
                if (DEBUG) {
                    Slog.i(TAG, "get Settings.System.SMART_BACKLIGHT failed, set default value to 1");
                }
                System.putInt(this.mContext.getContentResolver(), "smart_backlight_enable", 1);
            } else if (DEBUG) {
                Slog.i(TAG, "get Settings.System.SMART_BACKLIGHT = " + smartBackLightSetting);
            }
        } else if (DEBUG) {
            Slog.i(TAG, "get ro.config.hw_smart_backlight = " + smartBackLightConfig + ", mUsingSRE = false, don't support sbl or sre");
        }
        IHwNormalizedManualBrightnessController iadm2 = HwServiceFactory.getHuaweiManualBrightnessController();
        if (iadm2 != null) {
            this.mManualBrightnessController = iadm2.getInstance(this, this.mContext, this.mSensorManager);
            if (DEBUG) {
                Slog.i(TAG, "HBM ManualBrightnessController initialized");
            }
        } else {
            this.mManualBrightnessController = new ManualBrightnessController(this);
        }
        if (this.mUseSensorHubLABC) {
            this.mLABCSensor = this.mSensorManager.getDefaultSensor(65543);
            if (this.mLABCSensor == null) {
                Slog.e(TAG, "[LABC] Get LABC Sensor failed !! ");
            }
        } else if (this.mSmartBackLightSupported) {
            this.mHwSmartBackLightController = HwServiceFactory.getHwSmartBackLightController();
            if (this.mHwSmartBackLightController != null) {
                this.mUsingHwSmartBackLightController = this.mHwSmartBackLightController.checkIfUsingHwSBL();
                this.mHwSmartBackLightController.StartHwSmartBackLightController(this.mContext, this.mLights, this.mSensorManager);
            }
        }
    }

    public boolean isProximitySensorAvailable() {
        return this.mProximitySensor != null;
    }

    public boolean requestPowerState(DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        if (DEBUG && DEBUG_Controller) {
            Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity);
        }
        synchronized (this.mLock) {
            boolean changed = false;
            if (waitForNegativeProximity) {
                if ((this.mPendingWaitForNegativeProximityLocked ^ 1) != 0) {
                    this.mPendingWaitForNegativeProximityLocked = true;
                    changed = true;
                }
            }
            if (this.mPendingRequestLocked == null) {
                this.mPendingRequestLocked = new DisplayPowerRequest(request);
                changed = true;
            } else if (!this.mPendingRequestLocked.equals(request)) {
                this.mPendingRequestLocked.copyFrom(request);
                changed = true;
            }
            if (changed) {
                this.mDisplayReadyLocked = false;
            }
            if (changed && (this.mPendingRequestChangedLocked ^ 1) != 0) {
                this.mPendingRequestChangedLocked = true;
                sendUpdatePowerStateLocked();
            }
            z = this.mDisplayReadyLocked;
        }
        return z;
    }

    private void sendUpdatePowerState() {
        synchronized (this.mLock) {
            sendUpdatePowerStateLocked();
        }
    }

    private void sendUpdatePowerStateLocked() {
        if (!this.mPendingUpdatePowerStateLocked) {
            this.mPendingUpdatePowerStateLocked = true;
            Message msg = this.mHandler.obtainMessage(1);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    private void initialize() {
        this.mPowerState = new DisplayPowerState(this.mContext, this.mBlanker, new ColorFade(0));
        this.mAutoCustomBackLight = this.mLights.getLight(LightsManager.LIGHT_ID_AUTOCUSTOMBACKLIGHT);
        this.mManualCustomBackLight = this.mLights.getLight(LightsManager.LIGHT_ID_MANUALCUSTOMBACKLIGHT);
        this.mColorFadeOnAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{0.0f, 1.0f});
        this.mColorFadeOnAnimator.setDuration(250);
        this.mColorFadeOnAnimator.addListener(this.mAnimatorListener);
        this.mColorFadeOffAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{1.0f, 0.0f});
        this.mColorFadeOffAnimator.setDuration(150);
        this.mColorFadeOffAnimator.addListener(this.mAnimatorListener);
        IHwRampAnimator iadm = HwServiceFactory.getHwNormalizedRampAnimator();
        if (iadm != null) {
            this.mScreenBrightnessRampAnimator = iadm.getInstance(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
        } else {
            this.mScreenBrightnessRampAnimator = new RampAnimator(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
        }
        this.mScreenBrightnessRampAnimator.setListener(this.mRampAnimatorListener);
        try {
            this.mBatteryStats.noteScreenState(this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenBrightness(this.mPowerState.getScreenBrightness());
        } catch (RemoteException e) {
        }
    }

    private int getBrightness(boolean autoBrightnessAdjustmentChanged) {
        if (autoBrightnessAdjustmentChanged) {
            return this.mPendingBacklight;
        }
        return 0;
    }

    /* JADX WARNING: Missing block: B:16:0x006c, code:
            if (r21 == false) goto L_0x0071;
     */
    /* JADX WARNING: Missing block: B:17:0x006e, code:
            initialize();
     */
    /* JADX WARNING: Missing block: B:18:0x0071, code:
            r12 = -1;
            r25 = false;
     */
    /* JADX WARNING: Missing block: B:19:0x007a, code:
            switch(r30.mPowerRequest.policy) {
                case 0: goto L_0x0209;
                case 1: goto L_0x020e;
                case 2: goto L_0x007d;
                case 3: goto L_0x007d;
                case 4: goto L_0x022c;
                default: goto L_0x007d;
            };
     */
    /* JADX WARNING: Missing block: B:20:0x007d, code:
            r3 = 2;
     */
    /* JADX WARNING: Missing block: B:22:0x0080, code:
            if (-assertionsDisabled != false) goto L_0x022f;
     */
    /* JADX WARNING: Missing block: B:23:0x0082, code:
            if (r3 != 0) goto L_0x022f;
     */
    /* JADX WARNING: Missing block: B:25:0x0089, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:83:0x0209, code:
            r3 = 1;
            r25 = true;
     */
    /* JADX WARNING: Missing block: B:85:0x0214, code:
            if (r30.mPowerRequest.dozeScreenState == 0) goto L_0x022a;
     */
    /* JADX WARNING: Missing block: B:86:0x0216, code:
            r3 = r30.mPowerRequest.dozeScreenState;
     */
    /* JADX WARNING: Missing block: B:88:0x0220, code:
            if (r30.mAllowAutoBrightnessWhileDozingConfig != false) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:89:0x0222, code:
            r12 = r30.mPowerRequest.dozeScreenBrightness;
     */
    /* JADX WARNING: Missing block: B:90:0x022a, code:
            r3 = 3;
     */
    /* JADX WARNING: Missing block: B:91:0x022c, code:
            r3 = 5;
     */
    /* JADX WARNING: Missing block: B:93:0x0233, code:
            if (r30.mProximitySensor == null) goto L_0x0849;
     */
    /* JADX WARNING: Missing block: B:95:0x023b, code:
            if (r30.mPowerRequest.useProximitySensor == false) goto L_0x07f0;
     */
    /* JADX WARNING: Missing block: B:97:0x023e, code:
            if (r3 == 1) goto L_0x07f0;
     */
    /* JADX WARNING: Missing block: B:98:0x0240, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:99:0x024a, code:
            if (r30.mScreenOffBecauseOfProximity != false) goto L_0x025b;
     */
    /* JADX WARNING: Missing block: B:101:0x0251, code:
            if (r30.mProximity != 1) goto L_0x025b;
     */
    /* JADX WARNING: Missing block: B:102:0x0253, code:
            r30.mScreenOffBecauseOfProximity = true;
            sendOnProximityPositiveWithWakelock();
     */
    /* JADX WARNING: Missing block: B:104:0x025f, code:
            if (r30.mScreenOffBecauseOfProximity != false) goto L_0x0270;
     */
    /* JADX WARNING: Missing block: B:106:0x0266, code:
            if (r30.mProximity != 1) goto L_0x0270;
     */
    /* JADX WARNING: Missing block: B:107:0x0268, code:
            r30.mScreenOffBecauseOfProximity = true;
            sendOnProximityPositiveWithWakelock();
     */
    /* JADX WARNING: Missing block: B:109:0x0274, code:
            if (r30.mScreenOffBecauseOfProximity == false) goto L_0x0285;
     */
    /* JADX WARNING: Missing block: B:111:0x027b, code:
            if (r30.mProximity == 1) goto L_0x0285;
     */
    /* JADX WARNING: Missing block: B:112:0x027d, code:
            r30.mScreenOffBecauseOfProximity = false;
            sendOnProximityNegativeWithWakelock();
     */
    /* JADX WARNING: Missing block: B:114:0x0289, code:
            if (r30.mScreenOffBecauseOfProximity == false) goto L_0x0294;
     */
    /* JADX WARNING: Missing block: B:116:0x0291, code:
            if (r30.mPowerRequest.useProximitySensorbyPhone != false) goto L_0x0294;
     */
    /* JADX WARNING: Missing block: B:117:0x0293, code:
            r3 = 1;
     */
    /* JADX WARNING: Missing block: B:118:0x0294, code:
            sre_init(r3);
            hbm_init(r3);
            sendScreenStateToDE(r3);
     */
    /* JADX WARNING: Missing block: B:119:0x02a3, code:
            if (r25 == false) goto L_0x02c1;
     */
    /* JADX WARNING: Missing block: B:121:0x02a9, code:
            if (r30.mLastWaitBrightnessMode == false) goto L_0x0850;
     */
    /* JADX WARNING: Missing block: B:123:0x02b3, code:
            if ((r30.mPowerRequest.brightnessWaitMode ^ 1) == 0) goto L_0x0850;
     */
    /* JADX WARNING: Missing block: B:124:0x02b5, code:
            r23 = r30.mPowerRequest.brightnessWaitRet ^ 1;
     */
    /* JADX WARNING: Missing block: B:125:0x02bd, code:
            r25 = r25 & (r23 ^ 1);
     */
    /* JADX WARNING: Missing block: B:126:0x02c1, code:
            r24 = r30.mPowerState.getScreenState();
            animateScreenStateChange(r3, r25);
            r3 = r30.mPowerState.getScreenState();
     */
    /* JADX WARNING: Missing block: B:127:0x02d9, code:
            if (r3 != 1) goto L_0x02e6;
     */
    /* JADX WARNING: Missing block: B:128:0x02db, code:
            r12 = 0;
            r30.mWakeupFromSleep = true;
            r30.mProximityPositive = false;
     */
    /* JADX WARNING: Missing block: B:129:0x02e6, code:
            r10 = false;
            r30.mAutomaticBrightnessController.updateCurrentUserId(r30.mPowerRequest.userId);
            r8 = android.provider.Settings.System.getFloatForUser(r30.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, r30.mPowerRequest.userId);
     */
    /* JADX WARNING: Missing block: B:130:0x030c, code:
            if (NEED_NEW_BRIGHTNESS_PROCESS != false) goto L_0x0314;
     */
    /* JADX WARNING: Missing block: B:131:0x030e, code:
            r8 = r30.mPowerRequest.screenAutoBrightnessAdjustment;
     */
    /* JADX WARNING: Missing block: B:133:0x0318, code:
            if (r30.mUseSensorHubLABC != false) goto L_0x0873;
     */
    /* JADX WARNING: Missing block: B:135:0x031e, code:
            if (r30.mAllowAutoBrightnessWhileDozingConfig == false) goto L_0x0857;
     */
    /* JADX WARNING: Missing block: B:137:0x0321, code:
            if (r3 == 3) goto L_0x0326;
     */
    /* JADX WARNING: Missing block: B:139:0x0324, code:
            if (r3 != 4) goto L_0x0854;
     */
    /* JADX WARNING: Missing block: B:140:0x0326, code:
            r11 = true;
     */
    /* JADX WARNING: Missing block: B:141:0x0327, code:
            if (r9 == false) goto L_0x085a;
     */
    /* JADX WARNING: Missing block: B:142:0x0329, code:
            r28 = r30.mPowerRequest.brightnessSetByUser;
     */
    /* JADX WARNING: Missing block: B:144:0x0337, code:
            if (r30.mPowerRequest.useAutoBrightness == false) goto L_0x0861;
     */
    /* JADX WARNING: Missing block: B:146:0x033a, code:
            if (r3 == 2) goto L_0x033e;
     */
    /* JADX WARNING: Missing block: B:147:0x033c, code:
            if (r11 == false) goto L_0x0861;
     */
    /* JADX WARNING: Missing block: B:148:0x033e, code:
            if (r12 >= 0) goto L_0x085e;
     */
    /* JADX WARNING: Missing block: B:149:0x0340, code:
            r10 = true;
     */
    /* JADX WARNING: Missing block: B:150:0x0341, code:
            r4 = r30.mAutomaticBrightnessController;
     */
    /* JADX WARNING: Missing block: B:151:0x0346, code:
            if (r3 == 2) goto L_0x0864;
     */
    /* JADX WARNING: Missing block: B:152:0x0348, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:153:0x0349, code:
            r4.configure(r10, r8, r2, false);
            r30.mAutomaticBrightnessController.updatePowerPolicy(r30.mPowerRequest.policy);
     */
    /* JADX WARNING: Missing block: B:154:0x035c, code:
            if (DEBUG == false) goto L_0x0398;
     */
    /* JADX WARNING: Missing block: B:156:0x0362, code:
            if (r30.mAutoBrightnessEnabled == r10) goto L_0x0398;
     */
    /* JADX WARNING: Missing block: B:157:0x0364, code:
            android.util.Slog.d(TAG, "mode change : autoBrightnessEnabled=" + r10 + ",adjustment=" + r8 + ",state=" + r3);
            r30.mAutoBrightnessEnabled = r10;
     */
    /* JADX WARNING: Missing block: B:159:0x039a, code:
            if (NEED_NEW_BRIGHTNESS_PROCESS == false) goto L_0x046d;
     */
    /* JADX WARNING: Missing block: B:161:0x03a2, code:
            if (r30.mPowerRequest.screenAutoBrightness <= 0) goto L_0x0867;
     */
    /* JADX WARNING: Missing block: B:162:0x03a4, code:
            r30.mAutomaticBrightnessController.updateIntervenedAutoBrightness(r30.mPowerRequest.screenAutoBrightness);
            r30.mScreenBrightnessRampAnimator.mAutoBrightnessIntervened = true;
     */
    /* JADX WARNING: Missing block: B:163:0x03b8, code:
            r30.mScreenBrightnessRampAnimator.mIsAutoBrightnessMode = r10;
     */
    /* JADX WARNING: Missing block: B:164:0x03c2, code:
            if (r30.mBrightnessModeChanged == false) goto L_0x03cf;
     */
    /* JADX WARNING: Missing block: B:165:0x03c4, code:
            r30.mScreenBrightnessRampAnimator.mIsFirstValidAutoBrightness = r10;
            r30.mBrightnessModeChanged = false;
     */
    /* JADX WARNING: Missing block: B:166:0x03cf, code:
            r4 = r30.mScreenBrightnessRampAnimator;
            r5 = r30.mAutomaticBrightnessController.getUpdateAutoBrightnessCount();
     */
    /* JADX WARNING: Missing block: B:167:0x03e1, code:
            if (r30.mPowerRequest.screenAutoBrightness <= 0) goto L_0x0870;
     */
    /* JADX WARNING: Missing block: B:168:0x03e3, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:169:0x03e4, code:
            r4.updateBrightnessRampPara(r10, r5, r2, r30.mPowerRequest.policy);
            r30.mfastAnimtionFlag = r30.mAutomaticBrightnessController.getPowerStatus();
            r30.mScreenBrightnessRampAnimator.updateFastAnimationFlag(r30.mfastAnimtionFlag);
            r30.mCoverModeAnimationFast = r30.mAutomaticBrightnessController.getCoverModeFastResponseFlag();
            r30.mScreenBrightnessRampAnimator.updateCoverModeFastAnimationFlag(r30.mCoverModeAnimationFast);
            r30.mScreenBrightnessRampAnimator.updateCameraModeChangeAnimationEnable(r30.mAutomaticBrightnessController.getCameraModeChangeAnimationEnable());
            r30.mScreenBrightnessRampAnimator.setBrightnessAnimationTime(r30.mAnimationEnabled, r30.mMillisecond);
            r30.mScreenBrightnessRampAnimator.updateScreenLockedAnimationEnable(r30.mKeyguardIsLocked);
            r30.mOutdoorAnimationFlag = r30.mAutomaticBrightnessController.getOutdoorAnimationFlag();
            r30.mScreenBrightnessRampAnimator.updateOutdoorAnimationFlag(r30.mOutdoorAnimationFlag);
            updatemManualModeAnimationEnable();
            updateManualPowerSavingAnimationEnable();
            updateManualThermalModeAnimationEnable();
            r30.mBackLight.updateBrightnessAdjustMode(r10);
     */
    /* JADX WARNING: Missing block: B:171:0x0489, code:
            if (waitScreenBrightness(r3, r30.mPowerRequest.brightnessWaitMode, r30.mLastWaitBrightnessMode, r30.mPowerRequest.brightnessWaitRet, r30.mPowerRequest.skipWaitKeyguardDismiss) == false) goto L_0x0494;
     */
    /* JADX WARNING: Missing block: B:172:0x048b, code:
            r12 = 0;
            r30.mWindowManagerPolicy.setInterceptInputForWaitBrightness(true);
     */
    /* JADX WARNING: Missing block: B:174:0x0498, code:
            if (r30.mGlobalAlpmState != 0) goto L_0x049b;
     */
    /* JADX WARNING: Missing block: B:175:0x049a, code:
            r12 = 0;
     */
    /* JADX WARNING: Missing block: B:177:0x04a1, code:
            if (r30.mPowerRequest.boostScreenBrightness == false) goto L_0x04a7;
     */
    /* JADX WARNING: Missing block: B:178:0x04a3, code:
            if (r12 == 0) goto L_0x04a7;
     */
    /* JADX WARNING: Missing block: B:179:0x04a5, code:
            r12 = 255;
     */
    /* JADX WARNING: Missing block: B:180:0x04a7, code:
            r27 = false;
     */
    /* JADX WARNING: Missing block: B:181:0x04a9, code:
            if (r12 >= 0) goto L_0x0976;
     */
    /* JADX WARNING: Missing block: B:182:0x04ab, code:
            if (r10 == false) goto L_0x0502;
     */
    /* JADX WARNING: Missing block: B:184:0x04b1, code:
            if (r30.mUseSensorHubLABC != false) goto L_0x0950;
     */
    /* JADX WARNING: Missing block: B:185:0x04b3, code:
            r12 = r30.mAutomaticBrightnessController.getAutomaticScreenBrightness();
     */
    /* JADX WARNING: Missing block: B:186:0x04bb, code:
            if (r12 >= 0) goto L_0x0502;
     */
    /* JADX WARNING: Missing block: B:188:0x04ce, code:
            if ((android.os.SystemClock.uptimeMillis() - r30.mAutomaticBrightnessController.getLightSensorEnableTime()) <= 195) goto L_0x0502;
     */
    /* JADX WARNING: Missing block: B:190:0x04d1, code:
            if (r12 == -2) goto L_0x04e4;
     */
    /* JADX WARNING: Missing block: B:191:0x04d3, code:
            r12 = android.provider.Settings.System.getInt(r30.mContext.getContentResolver(), "screen_brightness", 100);
     */
    /* JADX WARNING: Missing block: B:193:0x04e6, code:
            if (DEBUG == false) goto L_0x0502;
     */
    /* JADX WARNING: Missing block: B:194:0x04e8, code:
            android.util.Slog.d(TAG, "failed to get auto brightness so set brightness based on SCREEN_BRIGHTNESS:" + r12);
     */
    /* JADX WARNING: Missing block: B:195:0x0502, code:
            if (r12 < 0) goto L_0x096a;
     */
    /* JADX WARNING: Missing block: B:196:0x0504, code:
            r12 = clampScreenBrightness(r12);
     */
    /* JADX WARNING: Missing block: B:197:0x050e, code:
            if (r30.mUseSensorHubLABC != false) goto L_0x0958;
     */
    /* JADX WARNING: Missing block: B:199:0x0514, code:
            if (r30.mAppliedAutoBrightness == false) goto L_0x051c;
     */
    /* JADX WARNING: Missing block: B:201:0x0518, code:
            if ((r9 ^ 1) == 0) goto L_0x051c;
     */
    /* JADX WARNING: Missing block: B:202:0x051a, code:
            r27 = true;
     */
    /* JADX WARNING: Missing block: B:203:0x051c, code:
            r30.mAppliedAutoBrightness = true;
     */
    /* JADX WARNING: Missing block: B:204:0x0521, code:
            r30.mAutoBrightnessAdjustmentChanged = false;
     */
    /* JADX WARNING: Missing block: B:205:0x0526, code:
            if (r12 >= 0) goto L_0x0532;
     */
    /* JADX WARNING: Missing block: B:207:0x0529, code:
            if (r3 == 3) goto L_0x052e;
     */
    /* JADX WARNING: Missing block: B:209:0x052c, code:
            if (r3 != 4) goto L_0x0532;
     */
    /* JADX WARNING: Missing block: B:210:0x052e, code:
            r12 = r30.mScreenBrightnessDozeConfig;
     */
    /* JADX WARNING: Missing block: B:211:0x0532, code:
            r16 = android.os.SystemProperties.getInt("ro.config.hw_high_bright_mode", 1);
     */
    /* JADX WARNING: Missing block: B:212:0x053a, code:
            if (r12 >= 0) goto L_0x097d;
     */
    /* JADX WARNING: Missing block: B:214:0x0544, code:
            if ((r30.mPowerRequest.useAutoBrightness ^ 1) == 0) goto L_0x097d;
     */
    /* JADX WARNING: Missing block: B:216:0x0549, code:
            if (r16 != 1) goto L_0x097d;
     */
    /* JADX WARNING: Missing block: B:217:0x054b, code:
            r15 = true;
     */
    /* JADX WARNING: Missing block: B:218:0x054c, code:
            if (r15 == false) goto L_0x05a5;
     */
    /* JADX WARNING: Missing block: B:219:0x054e, code:
            r12 = clampScreenBrightness(r30.mPowerRequest.screenBrightness);
     */
    /* JADX WARNING: Missing block: B:220:0x055c, code:
            if (DEBUG == false) goto L_0x0578;
     */
    /* JADX WARNING: Missing block: B:221:0x055e, code:
            android.util.Slog.i(TAG, "HBM brightnessIn =" + r12);
     */
    /* JADX WARNING: Missing block: B:222:0x0578, code:
            r30.mManualBrightnessController.updateManualBrightness(r12);
            r12 = r30.mManualBrightnessController.getManualBrightness();
     */
    /* JADX WARNING: Missing block: B:223:0x0589, code:
            if (DEBUG == false) goto L_0x05a5;
     */
    /* JADX WARNING: Missing block: B:224:0x058b, code:
            android.util.Slog.i(TAG, "HBM brightnessOut =" + r12);
     */
    /* JADX WARNING: Missing block: B:225:0x05a5, code:
            if (r12 >= 0) goto L_0x05c8;
     */
    /* JADX WARNING: Missing block: B:227:0x05af, code:
            if ((r30.mPowerRequest.useAutoBrightness ^ 1) == 0) goto L_0x05c8;
     */
    /* JADX WARNING: Missing block: B:228:0x05b1, code:
            r12 = clampScreenBrightness(r30.mPowerRequest.screenBrightness);
     */
    /* JADX WARNING: Missing block: B:229:0x05bf, code:
            if (NEED_NEW_BRIGHTNESS_PROCESS == false) goto L_0x05c8;
     */
    /* JADX WARNING: Missing block: B:230:0x05c1, code:
            r30.mAutomaticBrightnessController.updateIntervenedAutoBrightness(r12);
     */
    /* JADX WARNING: Missing block: B:232:0x05c9, code:
            if (r3 != 2) goto L_0x0605;
     */
    /* JADX WARNING: Missing block: B:234:0x05d1, code:
            if (r30.mPowerRequest.policy != 0) goto L_0x0605;
     */
    /* JADX WARNING: Missing block: B:236:0x05d7, code:
            if (r12 <= r30.mScreenBrightnessRangeMinimum) goto L_0x05eb;
     */
    /* JADX WARNING: Missing block: B:237:0x05d9, code:
            r12 = java.lang.Math.max(java.lang.Math.min(r12 - 10, r30.mScreenBrightnessDimConfig), r30.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:238:0x05eb, code:
            android.util.Slog.i(TAG, "set brightness to DIM brightness:" + r12);
     */
    /* JADX WARNING: Missing block: B:240:0x060c, code:
            if (r30.mPowerRequest.policy != 2) goto L_0x0980;
     */
    /* JADX WARNING: Missing block: B:242:0x0612, code:
            if (r12 <= r30.mScreenBrightnessRangeMinimum) goto L_0x0626;
     */
    /* JADX WARNING: Missing block: B:243:0x0614, code:
            r12 = java.lang.Math.max(java.lang.Math.min(r12 - 10, r30.mScreenBrightnessDimConfig), r30.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:245:0x062a, code:
            if (android.util.HwPCUtils.isPcCastModeInServer() == false) goto L_0x0638;
     */
    /* JADX WARNING: Missing block: B:247:0x0632, code:
            if ((android.util.HwPCUtils.enabledInPad() ^ 1) == 0) goto L_0x0638;
     */
    /* JADX WARNING: Missing block: B:248:0x0634, code:
            r12 = 0;
            blackScreenOnPcMode();
     */
    /* JADX WARNING: Missing block: B:250:0x063c, code:
            if (r30.mAppliedDimming != false) goto L_0x0640;
     */
    /* JADX WARNING: Missing block: B:251:0x063e, code:
            r27 = false;
     */
    /* JADX WARNING: Missing block: B:252:0x0640, code:
            r30.mAppliedDimming = true;
     */
    /* JADX WARNING: Missing block: B:254:0x064b, code:
            if (r30.mPowerRequest.lowPowerMode == false) goto L_0x098f;
     */
    /* JADX WARNING: Missing block: B:256:0x0651, code:
            if (r12 <= r30.mScreenBrightnessRangeMinimum) goto L_0x066e;
     */
    /* JADX WARNING: Missing block: B:257:0x0653, code:
            r12 = java.lang.Math.max((int) (((float) r12) * java.lang.Math.min(r30.mPowerRequest.screenLowPowerBrightnessFactor, 1.0f)), r30.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Missing block: B:259:0x0672, code:
            if (r30.mAppliedLowPower != false) goto L_0x0676;
     */
    /* JADX WARNING: Missing block: B:260:0x0674, code:
            r27 = false;
     */
    /* JADX WARNING: Missing block: B:261:0x0676, code:
            r30.mAppliedLowPower = true;
     */
    /* JADX WARNING: Missing block: B:263:0x067f, code:
            if (r30.mPendingScreenOff != false) goto L_0x06f1;
     */
    /* JADX WARNING: Missing block: B:265:0x0685, code:
            if (r30.mSkipScreenOnBrightnessRamp == false) goto L_0x069f;
     */
    /* JADX WARNING: Missing block: B:267:0x0688, code:
            if (r3 != 2) goto L_0x09c6;
     */
    /* JADX WARNING: Missing block: B:269:0x068e, code:
            if (r30.mSkipRampState != 0) goto L_0x099e;
     */
    /* JADX WARNING: Missing block: B:271:0x0694, code:
            if (r30.mDozing == false) goto L_0x099e;
     */
    /* JADX WARNING: Missing block: B:272:0x0696, code:
            r30.mInitialAutoBrightness = r12;
            r30.mSkipRampState = 1;
     */
    /* JADX WARNING: Missing block: B:274:0x06a0, code:
            if (r3 == 5) goto L_0x06a7;
     */
    /* JADX WARNING: Missing block: B:276:0x06a5, code:
            if (r24 != 5) goto L_0x09cd;
     */
    /* JADX WARNING: Missing block: B:277:0x06a7, code:
            r29 = true;
     */
    /* JADX WARNING: Missing block: B:279:0x06aa, code:
            if (r3 != 2) goto L_0x09d1;
     */
    /* JADX WARNING: Missing block: B:281:0x06b0, code:
            if (r30.mSkipRampState != 0) goto L_0x09d1;
     */
    /* JADX WARNING: Missing block: B:283:0x06b4, code:
            if ((r29 ^ 1) == 0) goto L_0x09d4;
     */
    /* JADX WARNING: Missing block: B:285:0x06b7, code:
            if (r3 != 2) goto L_0x0a04;
     */
    /* JADX WARNING: Missing block: B:287:0x06bd, code:
            if (r30.mImmeBright != false) goto L_0x06c5;
     */
    /* JADX WARNING: Missing block: B:289:0x06c3, code:
            if (r30.mWakeupFromSleep == false) goto L_0x0a04;
     */
    /* JADX WARNING: Missing block: B:290:0x06c5, code:
            r30.mImmeBright = false;
     */
    /* JADX WARNING: Missing block: B:291:0x06d2, code:
            if (r30.mAutomaticBrightnessController.getRebootFirstBrightnessAnimationEnable() == false) goto L_0x09f5;
     */
    /* JADX WARNING: Missing block: B:293:0x06d8, code:
            if (r30.mRebootWakeupFromSleep == false) goto L_0x09f5;
     */
    /* JADX WARNING: Missing block: B:294:0x06da, code:
            if (r12 <= 0) goto L_0x09ed;
     */
    /* JADX WARNING: Missing block: B:295:0x06dc, code:
            if (r27 == false) goto L_0x09e7;
     */
    /* JADX WARNING: Missing block: B:296:0x06de, code:
            r2 = r30.mBrightnessRampRateSlow;
     */
    /* JADX WARNING: Missing block: B:297:0x06e2, code:
            animateScreenBrightness(r12, r2);
            r30.mRebootWakeupFromSleep = false;
            r30.mWakeupFromSleep = false;
     */
    /* JADX WARNING: Missing block: B:298:0x06f1, code:
            r30.mLastWaitBrightnessMode = r30.mPowerRequest.brightnessWaitMode;
     */
    /* JADX WARNING: Missing block: B:299:0x06ff, code:
            if (r30.mPendingScreenOnUnblocker != null) goto L_0x0a28;
     */
    /* JADX WARNING: Missing block: B:301:0x070b, code:
            if ((r30.mColorFadeOnAnimator.isStarted() ^ 1) == 0) goto L_0x0a28;
     */
    /* JADX WARNING: Missing block: B:303:0x0717, code:
            if ((r30.mColorFadeOffAnimator.isStarted() ^ 1) == 0) goto L_0x0a28;
     */
    /* JADX WARNING: Missing block: B:304:0x0719, code:
            r26 = r30.mPowerState.waitUntilClean(r30.mCleanListener);
     */
    /* JADX WARNING: Missing block: B:306:0x072d, code:
            if (r30.mWindowManagerPolicy.getInterceptInputForWaitBrightness() == false) goto L_0x0747;
     */
    /* JADX WARNING: Missing block: B:308:0x0735, code:
            if (r30.mPowerRequest.brightnessWaitMode != false) goto L_0x0747;
     */
    /* JADX WARNING: Missing block: B:310:0x073b, code:
            if (r30.mPendingScreenOnForKeyguardDismissUnblocker != null) goto L_0x0747;
     */
    /* JADX WARNING: Missing block: B:311:0x073d, code:
            if (r26 == false) goto L_0x0747;
     */
    /* JADX WARNING: Missing block: B:312:0x073f, code:
            r30.mWindowManagerPolicy.setInterceptInputForWaitBrightness(false);
     */
    /* JADX WARNING: Missing block: B:313:0x0747, code:
            if (r26 == false) goto L_0x0a2c;
     */
    /* JADX WARNING: Missing block: B:314:0x0749, code:
            r14 = r30.mScreenBrightnessRampAnimator.isAnimating() ^ 1;
     */
    /* JADX WARNING: Missing block: B:315:0x0753, code:
            if (r26 == false) goto L_0x076b;
     */
    /* JADX WARNING: Missing block: B:317:0x0756, code:
            if (r3 == 1) goto L_0x076b;
     */
    /* JADX WARNING: Missing block: B:319:0x075d, code:
            if (r30.mReportedScreenStateToPolicy != 1) goto L_0x076b;
     */
    /* JADX WARNING: Missing block: B:320:0x075f, code:
            r30.mReportedScreenStateToPolicy = 2;
            r30.mWindowManagerPolicy.screenTurnedOn();
     */
    /* JADX WARNING: Missing block: B:321:0x076b, code:
            if (r14 != 0) goto L_0x078e;
     */
    /* JADX WARNING: Missing block: B:323:0x0773, code:
            if ((r30.mUnfinishedBusiness ^ 1) == 0) goto L_0x078e;
     */
    /* JADX WARNING: Missing block: B:325:0x0777, code:
            if (DEBUG == false) goto L_0x0782;
     */
    /* JADX WARNING: Missing block: B:326:0x0779, code:
            android.util.Slog.d(TAG, "Unfinished business...");
     */
    /* JADX WARNING: Missing block: B:327:0x0782, code:
            r30.mCallbacks.acquireSuspendBlocker();
            r30.mUnfinishedBusiness = true;
     */
    /* JADX WARNING: Missing block: B:328:0x078e, code:
            if (r26 == false) goto L_0x07b3;
     */
    /* JADX WARNING: Missing block: B:329:0x0790, code:
            if (r22 == false) goto L_0x07b3;
     */
    /* JADX WARNING: Missing block: B:330:0x0792, code:
            r4 = r30.mLock;
     */
    /* JADX WARNING: Missing block: B:331:0x0796, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:334:0x079b, code:
            if (r30.mPendingRequestChangedLocked != false) goto L_0x07af;
     */
    /* JADX WARNING: Missing block: B:335:0x079d, code:
            r30.mDisplayReadyLocked = true;
     */
    /* JADX WARNING: Missing block: B:336:0x07a4, code:
            if (DEBUG == false) goto L_0x07af;
     */
    /* JADX WARNING: Missing block: B:337:0x07a6, code:
            android.util.Slog.d(TAG, "Display ready!");
     */
    /* JADX WARNING: Missing block: B:338:0x07af, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:339:0x07b0, code:
            sendOnStateChangedWithWakelock();
     */
    /* JADX WARNING: Missing block: B:340:0x07b3, code:
            if (r14 == 0) goto L_0x07e7;
     */
    /* JADX WARNING: Missing block: B:342:0x07b9, code:
            if (r30.mUnfinishedBusiness == false) goto L_0x07e7;
     */
    /* JADX WARNING: Missing block: B:344:0x07bd, code:
            if (DEBUG == false) goto L_0x07c8;
     */
    /* JADX WARNING: Missing block: B:345:0x07bf, code:
            android.util.Slog.d(TAG, "Finished business...");
     */
    /* JADX WARNING: Missing block: B:347:0x07ca, code:
            if (DEBUG_FPLOG == false) goto L_0x07db;
     */
    /* JADX WARNING: Missing block: B:349:0x07d0, code:
            if (r30.fpDataCollector == null) goto L_0x07db;
     */
    /* JADX WARNING: Missing block: B:350:0x07d2, code:
            if (r12 <= 0) goto L_0x07db;
     */
    /* JADX WARNING: Missing block: B:351:0x07d4, code:
            r30.fpDataCollector.reportScreenTurnedOn();
     */
    /* JADX WARNING: Missing block: B:352:0x07db, code:
            r30.mUnfinishedBusiness = false;
            r30.mCallbacks.releaseSuspendBlocker();
     */
    /* JADX WARNING: Missing block: B:354:0x07e8, code:
            if (r3 == 2) goto L_0x0a32;
     */
    /* JADX WARNING: Missing block: B:355:0x07ea, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:356:0x07eb, code:
            r30.mDozing = r2;
     */
    /* JADX WARNING: Missing block: B:357:0x07ef, code:
            return;
     */
    /* JADX WARNING: Missing block: B:359:0x07f4, code:
            if (r30.mWaitingForNegativeProximity == false) goto L_0x080e;
     */
    /* JADX WARNING: Missing block: B:361:0x07fa, code:
            if (r30.mScreenOffBecauseOfProximity == false) goto L_0x080e;
     */
    /* JADX WARNING: Missing block: B:363:0x0801, code:
            if (r30.mProximity != 1) goto L_0x080e;
     */
    /* JADX WARNING: Missing block: B:365:0x0804, code:
            if (r3 == 1) goto L_0x080e;
     */
    /* JADX WARNING: Missing block: B:366:0x0806, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:368:0x0812, code:
            if (r30.mWaitingForNegativeProximity != false) goto L_0x0834;
     */
    /* JADX WARNING: Missing block: B:370:0x0818, code:
            if (r30.mScreenOffBecauseOfProximity == false) goto L_0x0834;
     */
    /* JADX WARNING: Missing block: B:372:0x081f, code:
            if (r30.mProximity == -1) goto L_0x0834;
     */
    /* JADX WARNING: Missing block: B:374:0x0822, code:
            if (r3 != 1) goto L_0x0834;
     */
    /* JADX WARNING: Missing block: B:376:0x082a, code:
            if (r30.mPowerRequest.useProximitySensor == false) goto L_0x0834;
     */
    /* JADX WARNING: Missing block: B:377:0x082c, code:
            setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Missing block: B:379:0x083a, code:
            if (r30.mPowerRequest.useProximitySensor != false) goto L_0x0842;
     */
    /* JADX WARNING: Missing block: B:380:0x083c, code:
            setProximitySensorEnabled(false);
     */
    /* JADX WARNING: Missing block: B:381:0x0842, code:
            r30.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Missing block: B:382:0x0849, code:
            r30.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Missing block: B:383:0x0850, code:
            r23 = 0;
     */
    /* JADX WARNING: Missing block: B:384:0x0854, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:385:0x0857, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:387:0x085e, code:
            r10 = false;
     */
    /* JADX WARNING: Missing block: B:388:0x0861, code:
            r10 = false;
     */
    /* JADX WARNING: Missing block: B:389:0x0864, code:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:390:0x0867, code:
            r30.mScreenBrightnessRampAnimator.mAutoBrightnessIntervened = false;
     */
    /* JADX WARNING: Missing block: B:391:0x0870, code:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:393:0x0877, code:
            if (r30.mLABCSensorEnabled == false) goto L_0x046d;
     */
    /* JADX WARNING: Missing block: B:395:0x087d, code:
            if (r30.mAllowAutoBrightnessWhileDozingConfig == false) goto L_0x0905;
     */
    /* JADX WARNING: Missing block: B:397:0x0880, code:
            if (r3 == 3) goto L_0x0885;
     */
    /* JADX WARNING: Missing block: B:399:0x0883, code:
            if (r3 != 4) goto L_0x0903;
     */
    /* JADX WARNING: Missing block: B:400:0x0885, code:
            r11 = true;
     */
    /* JADX WARNING: Missing block: B:402:0x088c, code:
            if (r30.mPowerRequest.useAutoBrightness == false) goto L_0x0909;
     */
    /* JADX WARNING: Missing block: B:404:0x088f, code:
            if (r3 == 2) goto L_0x0893;
     */
    /* JADX WARNING: Missing block: B:405:0x0891, code:
            if (r11 == false) goto L_0x0909;
     */
    /* JADX WARNING: Missing block: B:406:0x0893, code:
            if (r12 >= 0) goto L_0x0907;
     */
    /* JADX WARNING: Missing block: B:407:0x0895, code:
            r10 = true;
     */
    /* JADX WARNING: Missing block: B:408:0x0896, code:
            if (r9 == false) goto L_0x090b;
     */
    /* JADX WARNING: Missing block: B:409:0x0898, code:
            r28 = r30.mPowerRequest.brightnessSetByUser;
     */
    /* JADX WARNING: Missing block: B:410:0x08a0, code:
            r4 = r30.mAutomaticBrightnessController;
            r5 = r30.mPowerRequest.screenAutoBrightnessAdjustment;
     */
    /* JADX WARNING: Missing block: B:411:0x08ab, code:
            if (r3 == 2) goto L_0x090e;
     */
    /* JADX WARNING: Missing block: B:412:0x08ad, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:413:0x08ae, code:
            r4.configure(r10, r5, r2, r28);
     */
    /* JADX WARNING: Missing block: B:414:0x08b3, code:
            if (r10 == false) goto L_0x0910;
     */
    /* JADX WARNING: Missing block: B:415:0x08b5, code:
            if (r9 == false) goto L_0x0910;
     */
    /* JADX WARNING: Missing block: B:416:0x08b7, code:
            r30.mSetAutoBackLight = (int) (((r30.mPowerRequest.screenAutoBrightnessAdjustment + 1.0f) / 2.0f) * 255.0f);
     */
    /* JADX WARNING: Missing block: B:417:0x08cd, code:
            if (DEBUG == false) goto L_0x08ed;
     */
    /* JADX WARNING: Missing block: B:418:0x08cf, code:
            android.util.Slog.d(TAG, "[LABC]  A = " + r30.mSetAutoBackLight);
     */
    /* JADX WARNING: Missing block: B:419:0x08ed, code:
            r30.mAutoCustomBackLight.sendCustomBackLight(r30.mSetAutoBackLight);
            r30.mAutoBrightnessAdjustmentChanged = true;
            r30.mLastStatus = true;
     */
    /* JADX WARNING: Missing block: B:420:0x0902, code:
            return;
     */
    /* JADX WARNING: Missing block: B:421:0x0903, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:422:0x0905, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:423:0x0907, code:
            r10 = false;
     */
    /* JADX WARNING: Missing block: B:424:0x0909, code:
            r10 = false;
     */
    /* JADX WARNING: Missing block: B:425:0x090b, code:
            r28 = false;
     */
    /* JADX WARNING: Missing block: B:426:0x090e, code:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:427:0x0910, code:
            if (r10 != false) goto L_0x046d;
     */
    /* JADX WARNING: Missing block: B:429:0x0916, code:
            if (r30.mLastStatus == false) goto L_0x046d;
     */
    /* JADX WARNING: Missing block: B:431:0x091a, code:
            if (DEBUG == false) goto L_0x093c;
     */
    /* JADX WARNING: Missing block: B:432:0x091c, code:
            android.util.Slog.d(TAG, "[LABC]  M = " + r30.mPowerRequest.screenBrightness);
     */
    /* JADX WARNING: Missing block: B:433:0x093c, code:
            r30.mManualCustomBackLight.sendCustomBackLight(r30.mPowerRequest.screenBrightness);
            r30.mLastStatus = false;
     */
    /* JADX WARNING: Missing block: B:434:0x0950, code:
            r12 = getBrightness(r10);
     */
    /* JADX WARNING: Missing block: B:436:0x095c, code:
            if (r30.mAppliedAutoBrightness == false) goto L_0x051c;
     */
    /* JADX WARNING: Missing block: B:438:0x0964, code:
            if ((r30.mAutoBrightnessAdjustmentChanged ^ 1) == 0) goto L_0x051c;
     */
    /* JADX WARNING: Missing block: B:439:0x0966, code:
            r27 = true;
     */
    /* JADX WARNING: Missing block: B:441:0x096b, code:
            if (r12 != -2) goto L_0x0974;
     */
    /* JADX WARNING: Missing block: B:442:0x096d, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:443:0x096e, code:
            r30.mAppliedAutoBrightness = r2;
     */
    /* JADX WARNING: Missing block: B:444:0x0974, code:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:445:0x0976, code:
            r30.mAppliedAutoBrightness = false;
     */
    /* JADX WARNING: Missing block: B:446:0x097d, code:
            r15 = false;
     */
    /* JADX WARNING: Missing block: B:448:0x0984, code:
            if (r30.mAppliedDimming == false) goto L_0x0645;
     */
    /* JADX WARNING: Missing block: B:449:0x0986, code:
            r27 = false;
            r30.mAppliedDimming = false;
     */
    /* JADX WARNING: Missing block: B:451:0x0993, code:
            if (r30.mAppliedLowPower == false) goto L_0x067b;
     */
    /* JADX WARNING: Missing block: B:452:0x0995, code:
            r27 = false;
            r30.mAppliedLowPower = false;
     */
    /* JADX WARNING: Missing block: B:454:0x09a3, code:
            if (r30.mSkipRampState != 1) goto L_0x09b8;
     */
    /* JADX WARNING: Missing block: B:456:0x09a9, code:
            if (r30.mUseSoftwareAutoBrightnessConfig == false) goto L_0x09b8;
     */
    /* JADX WARNING: Missing block: B:458:0x09af, code:
            if (r12 == r30.mInitialAutoBrightness) goto L_0x09b8;
     */
    /* JADX WARNING: Missing block: B:459:0x09b1, code:
            r30.mSkipRampState = 2;
     */
    /* JADX WARNING: Missing block: B:461:0x09bd, code:
            if (r30.mSkipRampState != 2) goto L_0x069f;
     */
    /* JADX WARNING: Missing block: B:462:0x09bf, code:
            r30.mSkipRampState = 0;
     */
    /* JADX WARNING: Missing block: B:463:0x09c6, code:
            r30.mSkipRampState = 0;
     */
    /* JADX WARNING: Missing block: B:464:0x09cd, code:
            r29 = false;
     */
    /* JADX WARNING: Missing block: B:466:0x09d2, code:
            if (r3 == 3) goto L_0x06b2;
     */
    /* JADX WARNING: Missing block: B:467:0x09d4, code:
            animateScreenBrightness(r12, 0);
     */
    /* JADX WARNING: Missing block: B:468:0x09dc, code:
            if (NEED_NEW_BRIGHTNESS_PROCESS == false) goto L_0x06f1;
     */
    /* JADX WARNING: Missing block: B:469:0x09de, code:
            r30.mAutomaticBrightnessController.saveOffsetAlgorithmParas();
     */
    /* JADX WARNING: Missing block: B:470:0x09e7, code:
            r2 = r30.mBrightnessRampRateFast;
     */
    /* JADX WARNING: Missing block: B:471:0x09ed, code:
            animateScreenBrightness(r12, 0);
     */
    /* JADX WARNING: Missing block: B:472:0x09f5, code:
            if (r12 <= 0) goto L_0x09fc;
     */
    /* JADX WARNING: Missing block: B:473:0x09f7, code:
            r30.mWakeupFromSleep = false;
     */
    /* JADX WARNING: Missing block: B:474:0x09fc, code:
            animateScreenBrightness(r12, 0);
     */
    /* JADX WARNING: Missing block: B:476:0x0a0c, code:
            if (r30.mAutomaticBrightnessController.getSetbrightnessImmediateEnableForCaliTest() == false) goto L_0x0a16;
     */
    /* JADX WARNING: Missing block: B:477:0x0a0e, code:
            animateScreenBrightness(r12, 0);
     */
    /* JADX WARNING: Missing block: B:478:0x0a16, code:
            if (r27 == false) goto L_0x0a23;
     */
    /* JADX WARNING: Missing block: B:479:0x0a18, code:
            r2 = r30.mBrightnessRampRateSlow;
     */
    /* JADX WARNING: Missing block: B:480:0x0a1c, code:
            animateScreenBrightness(r12, r2);
     */
    /* JADX WARNING: Missing block: B:481:0x0a23, code:
            r2 = r30.mBrightnessRampRateFast;
     */
    /* JADX WARNING: Missing block: B:482:0x0a28, code:
            r26 = false;
     */
    /* JADX WARNING: Missing block: B:483:0x0a2c, code:
            r14 = 0;
     */
    /* JADX WARNING: Missing block: B:487:0x0a32, code:
            r2 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updatePowerState() {
        boolean mustInitialize = false;
        boolean autoBrightnessAdjustmentChanged = false;
        synchronized (this.mLock) {
            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerController updatePowerState mPendingRequestLocked=" + this.mPendingRequestLocked);
            this.mPendingUpdatePowerStateLocked = false;
            if (this.mPendingRequestLocked == null) {
                return;
            }
            if (this.mPowerRequest == null) {
                this.mPowerRequest = new DisplayPowerRequest(this.mPendingRequestLocked);
                this.mWaitingForNegativeProximity = this.mPendingWaitForNegativeProximityLocked;
                this.mPendingWaitForNegativeProximityLocked = false;
                this.mPendingRequestChangedLocked = false;
                mustInitialize = true;
                if (this.mUseSensorHubLABC) {
                    this.mLastStatus = true;
                    autoBrightnessAdjustmentChanged = true;
                }
            } else if (this.mPendingRequestChangedLocked) {
                autoBrightnessAdjustmentChanged = this.mPowerRequest.screenAutoBrightnessAdjustment != this.mPendingRequestLocked.screenAutoBrightnessAdjustment;
                boolean z = this.mPowerRequest.useAutoBrightness != this.mPendingRequestLocked.useAutoBrightness ? this.mPowerRequest.brightnessSetByUser == this.mPendingRequestLocked.brightnessSetByUser : false;
                this.mBrightnessModeChanged = z;
                if (this.mPowerRequest.policy == 2 && this.mPendingRequestLocked.policy != 2) {
                    this.mPowerPolicyChangeFromDimming = true;
                }
                if (this.mCurrentUserId != this.mPendingRequestLocked.userId) {
                    this.mCurrentUserIdChange = true;
                    this.mCurrentUserId = this.mPendingRequestLocked.userId;
                    this.mBackLight.updateUserId(this.mCurrentUserId);
                }
                this.mPowerRequest.copyFrom(this.mPendingRequestLocked);
                boolean isClosed = HwServiceFactory.isCoverClosed();
                boolean isCoverModeChanged = false;
                if (isClosed != this.mIsCoverModeClosed) {
                    this.mIsCoverModeClosed = isClosed;
                    isCoverModeChanged = true;
                }
                updateCoverModeStatus(isClosed);
                if (!(!this.mBrightnessModeChanged || (this.mCurrentUserIdChange ^ 1) == 0 || !this.mPowerRequest.useAutoBrightness || isCoverModeChanged || (this.mIsCoverModeClosed ^ 1) == 0 || (this.mBrightnessModeChangeNoClearOffsetEnable ^ 1) == 0 || (this.mModeToAutoNoClearOffsetEnable ^ 1) == 0)) {
                    updateAutoBrightnessAdjustFactor(0.0f);
                    if (DEBUG) {
                        Slog.d(TAG, "AdjustPositionBrightness set 0");
                    }
                }
                if (this.mBrightnessModeChangeNoClearOffsetEnable) {
                    this.mBrightnessModeChangeNoClearOffsetEnable = false;
                    Slog.i(TAG, "set mBrightnessModeChangeNoClearOffsetEnable=" + this.mBrightnessModeChangeNoClearOffsetEnable);
                }
                if (this.mBrightnessModeChanged && this.mModeToAutoNoClearOffsetEnable) {
                    this.mModeToAutoNoClearOffsetEnable = false;
                    Slog.i(TAG, "set mModeToAutoNoClearOffsetEnable1=" + this.mModeToAutoNoClearOffsetEnable);
                }
                this.mCurrentUserIdChange = false;
                this.mWaitingForNegativeProximity |= this.mPendingWaitForNegativeProximityLocked;
                this.mPendingWaitForNegativeProximityLocked = false;
                this.mPendingRequestChangedLocked = false;
                this.mDisplayReadyLocked = false;
                writeAutoBrightnessDbEnable(this.mPowerRequest.useAutoBrightness);
                if (this.mUseSensorHubLABC && !this.mLastStatus) {
                    autoBrightnessAdjustmentChanged = true;
                }
            }
            boolean mustNotify = this.mDisplayReadyLocked ^ 1;
        }
    }

    private void blackScreenOnPcMode() {
        HwPCUtils.log(TAG, "brightness set 0 in PC mode");
        try {
            IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
            if (pcMgr != null) {
                pcMgr.setScreenPower(false);
            }
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "blackScreenOnPcMode " + e);
        }
    }

    private void sre_init(int state) {
        if (!this.mUseSensorHubLABC) {
            if (this.mSmartBackLightSupported && this.mSmartBackLightEnabled != this.mPowerRequest.useSmartBacklight) {
                if (DEBUG) {
                    Slog.i(TAG, "mPowerRequest.useSmartBacklight change " + this.mSmartBackLightEnabled + " -> " + this.mPowerRequest.useSmartBacklight);
                }
                this.mSmartBackLightEnabled = this.mPowerRequest.useSmartBacklight;
            }
            if (this.mUsingHwSmartBackLightController) {
                this.mHwSmartBackLightController.updatePowerState(state, this.mSmartBackLightEnabled);
            }
            if (this.mDisplayEngineInterface != null) {
                boolean lightSensorOnEnable = wantScreenOn(state);
                if (this.mLightSensorOnEnable != lightSensorOnEnable) {
                    Slog.i(TAG, "LightSensorEnable change " + this.mLightSensorOnEnable + " -> " + lightSensorOnEnable);
                    this.mDisplayEngineInterface.updateLightSensorState(lightSensorOnEnable);
                    this.mLightSensorOnEnable = lightSensorOnEnable;
                }
            }
            if (this.mUsingSRE && this.mDisplayEngineInterface != null) {
                this.mSREEnabled = this.mPowerRequest.useSmartBacklight;
                Slog.i(TAG, "mPowerRequest.useSmartBacklight : " + this.mSREEnabled);
                if (this.mSREEnabled) {
                    this.mDisplayEngineInterface.setScene("SCENE_SRE", "ACTION_MODE_ON");
                } else {
                    this.mDisplayEngineInterface.setScene("SCENE_SRE", "ACTION_MODE_OFF");
                }
            }
        } else if (this.mLABCSensor != null) {
            this.mLABCEnabled = true;
            setLABCEnabled(wantScreenOn(state));
        }
    }

    private void hbm_init(int state) {
        if (SystemProperties.getInt("ro.config.hw_high_bright_mode", 1) == 1 && getManualModeEnable()) {
            boolean isManulMode = this.mPowerRequest.useAutoBrightness ^ 1;
            this.mAutomaticBrightnessController.setManualModeEnableForPg(isManulMode);
            this.mManualBrightnessController.updatePowerState(state, isManulMode);
        }
    }

    private void sendScreenStateToDE(int state) {
        if (this.mDisplayEngineInterface != null) {
            int currentState = getScreenOnState(state);
            if (this.mIsScreenOn != currentState) {
                Slog.i(TAG, "ScreenState change " + this.mIsScreenOn + " -> " + currentState);
                if (currentState == 1) {
                    this.mDisplayEngineInterface.setScene("SCENE_REAL_POWERMODE", "ACTION_MODE_ON");
                } else {
                    this.mDisplayEngineInterface.setScene("SCENE_REAL_POWERMODE", "ACTION_MODE_OFF");
                }
                this.mIsScreenOn = currentState;
            }
        }
    }

    private void updateCoverModeStatus(boolean isClosed) {
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.setCoverModeStatus(isClosed);
        }
    }

    public void updateBrightness() {
        sendUpdatePowerState();
    }

    public void updateManualBrightnessForLux() {
        sendUpdatePowerState();
    }

    private void blockScreenOnForKeyguardDismiss() {
        if (this.mPendingScreenOnForKeyguardDismissUnblocker == null) {
            this.mPendingScreenOnForKeyguardDismissUnblocker = new ScreenOnForKeyguardDismissUnblocker(this, null);
            this.mScreenOnForKeyguardDismissBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen on until keyguard dismiss done.");
        }
    }

    private void unblockScreenOnForKeyguardDismiss() {
        if (this.mPendingScreenOnForKeyguardDismissUnblocker != null) {
            this.mPendingScreenOnForKeyguardDismissUnblocker = null;
            Slog.i(TAG, "fingerunlock--Unblocked screen on for keyguard dismiss after " + (SystemClock.elapsedRealtime() - this.mScreenOnForKeyguardDismissBlockStartRealTime) + " ms");
        }
    }

    private void blockScreenOn() {
        if (this.mPendingScreenOnUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOnUnblocker = new ScreenOnUnblocker(this, null);
            this.mScreenOnBlockStartRealTime = SystemClock.elapsedRealtime();
            if (Jlog.isPerfTest()) {
                Jlog.i(2205, "JL_PWRSCRON_DPC_BLOCKSCREENON");
            }
            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerController Blocking screen on until initial contents have been drawn.");
        }
    }

    private void unblockScreenOn() {
        if (this.mPendingScreenOnUnblocker != null) {
            this.mPendingScreenOnUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOnBlockStartRealTime;
            if (Jlog.isPerfTest()) {
                Jlog.i(2206, "JL_PWRSCRON_DPC_UNBLOCKSCREENON");
            }
            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerController Unblocked screen on after " + delay + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
        }
    }

    private void blockScreenOff() {
        if (this.mPendingScreenOffUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOffUnblocker = new ScreenOffUnblocker(this, null);
            this.mScreenOffBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen off");
        }
    }

    private void unblockScreenOff() {
        if (this.mPendingScreenOffUnblocker != null) {
            this.mPendingScreenOffUnblocker = null;
            Slog.i(TAG, "Unblocked screen off after " + (SystemClock.elapsedRealtime() - this.mScreenOffBlockStartRealTime) + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
        }
    }

    private boolean setScreenState(int state) {
        boolean z = true;
        boolean isOff = state == 1;
        if (this.mPowerState.getScreenState() != state) {
            if (isOff && (this.mScreenOffBecauseOfProximity ^ 1) != 0) {
                if (this.mReportedScreenStateToPolicy == 2) {
                    this.mReportedScreenStateToPolicy = 3;
                    blockScreenOff();
                    this.mWindowManagerPolicy.screenTurningOff(this.mPendingScreenOffUnblocker);
                    return false;
                } else if (this.mPendingScreenOffUnblocker != null) {
                    return false;
                }
            }
            this.mPowerState.setScreenState(state);
            try {
                this.mBatteryStats.noteScreenState(state);
            } catch (RemoteException e) {
            }
        }
        boolean isDoze = mSupportAod && (state == 3 || state == 4);
        if (isOff && this.mReportedScreenStateToPolicy != 0 && (this.mScreenOffBecauseOfProximity ^ 1) != 0) {
            this.mReportedScreenStateToPolicy = 0;
            unblockScreenOn();
            this.mWindowManagerPolicy.screenTurnedOff();
            setPowerStatus(false);
            setBrightnessAnimationTime(false, 500);
            this.mBrightnessModeChangeNoClearOffsetEnable = this.mPoweroffModeChangeAutoEnable;
            if (this.mPoweroffModeChangeAutoEnable) {
                this.mPoweroffModeChangeAutoEnable = false;
                Slog.i(TAG, "poweroff set mPoweroffModeChangeAutoEnable=" + this.mPoweroffModeChangeAutoEnable);
            }
        } else if (!(isOff || (isDoze ^ 1) == 0 || this.mReportedScreenStateToPolicy != 3)) {
            unblockScreenOff();
            this.mWindowManagerPolicy.screenTurnedOff();
            this.mReportedScreenStateToPolicy = 0;
        }
        if (!(isOff || (isDoze ^ 1) == 0 || this.mReportedScreenStateToPolicy != 0)) {
            this.mReportedScreenStateToPolicy = 1;
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                blockScreenOn();
            } else {
                unblockScreenOn();
            }
            this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
            setPowerStatus(true);
        }
        if (this.mPendingScreenOnUnblocker != null) {
            z = false;
        }
        return z;
    }

    private boolean waitScreenBrightness(int displayState, boolean curReqWaitBright, boolean lastReqWaitBright, boolean enableBright, boolean skipWaitKeyguardDismiss) {
        boolean z = true;
        if (DEBUG && DEBUG_Controller) {
            Slog.i(TAG, "waitScreenBrightness displayState = " + displayState + " curReqWaitBright = " + curReqWaitBright + " lastReqWaitBright = " + lastReqWaitBright + " enableBright = " + enableBright + " skipWaitKeyguardDismiss = " + skipWaitKeyguardDismiss);
        }
        if (displayState == 2) {
            if (curReqWaitBright) {
                return true;
            }
            if (lastReqWaitBright && enableBright && (skipWaitKeyguardDismiss ^ 1) != 0) {
                blockScreenOnForKeyguardDismiss();
                this.mWindowManagerPolicy.waitKeyguardDismissDone(this.mPendingScreenOnForKeyguardDismissUnblocker);
            }
        } else if (this.mPendingScreenOnForKeyguardDismissUnblocker != null) {
            unblockScreenOnForKeyguardDismiss();
            this.mWindowManagerPolicy.cancelWaitKeyguardDismissDone();
        }
        if (this.mPendingScreenOnForKeyguardDismissUnblocker == null) {
            z = false;
        }
        return z;
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void animateScreenBrightness(int target, int rate) {
        if (DEBUG) {
            Slog.d(TAG, "Animating brightness: target=" + target + ", rate=" + rate);
        }
        if (target >= 0) {
            if (target == 0 && rate != 0) {
                rate = 0;
                Slog.e(TAG, "Animating brightness rate is invalid when screen off, set rate to 0");
            }
            if (this.mScreenBrightnessRampAnimator.animateTo(target, rate)) {
                try {
                    if (this.mUsingHwSmartBackLightController && this.mSmartBackLightEnabled && rate > 0) {
                        if (this.mScreenBrightnessRampAnimator.isAnimating()) {
                            this.mHwSmartBackLightController.updateBrightnessState(0);
                        } else if (DEBUG) {
                            Slog.i(TAG, "brightness changed but not animating");
                        }
                    }
                    HwLog.dubaie("DUBAI_TAG_BRIGHTNESS", "brightness=" + target);
                    this.mBatteryStats.noteScreenBrightness(target);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        int i = 2;
        if (this.mColorFadeOnAnimator.isStarted() || this.mColorFadeOffAnimator.isStarted()) {
            if (target == 2) {
                this.mPendingScreenOff = false;
            } else {
                return;
            }
        }
        if (this.mPendingScreenOff && target != 1) {
            setScreenState(1);
            this.mPendingScreenOff = false;
            this.mPowerState.dismissColorFadeResources();
        }
        if (target == 2) {
            if (setScreenState(2)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target == 5) {
            if (!(this.mScreenBrightnessRampAnimator.isAnimating() && this.mPowerState.getScreenState() == 2) && setScreenState(5)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target == 3) {
            if (!(this.mScreenBrightnessRampAnimator.isAnimating() && this.mPowerState.getScreenState() == 2) && setScreenState(3)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target != 4) {
            this.mPendingScreenOff = true;
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                setScreenState(1);
                this.mPendingScreenOff = false;
                this.mPowerState.dismissColorFadeResources();
            } else {
                if (performScreenOffTransition && (checkPhoneWindowIsTop() ^ 1) != 0) {
                    DisplayPowerState displayPowerState = this.mPowerState;
                    Context context = this.mContext;
                    if (!this.mColorFadeFadesConfig) {
                        i = 1;
                    }
                    if (displayPowerState.prepareColorFade(context, i) && this.mPowerState.getScreenState() != 1) {
                        this.mColorFadeOffAnimator.start();
                    }
                }
                this.mColorFadeOffAnimator.end();
            }
        } else if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 4) {
            if (this.mPowerState.getScreenState() != 4) {
                if (setScreenState(3)) {
                    setScreenState(4);
                } else {
                    return;
                }
            }
            this.mPowerState.setColorFadeLevel(1.0f);
            this.mPowerState.dismissColorFade();
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mProximitySensorEnabled) {
                this.mProximitySensorEnabled = true;
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 1, this.mHandler);
            }
        } else if (this.mProximitySensorEnabled) {
            this.mProximitySensorEnabled = false;
            this.mProximity = -1;
            this.mPendingProximity = -1;
            this.mHandler.removeMessages(2);
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            clearPendingProximityDebounceTime();
        }
    }

    private void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mProximitySensorEnabled && (this.mPendingProximity != 0 || (positive ^ 1) == 0)) {
            if (this.mPendingProximity != 1 || !positive) {
                HwServiceFactory.reportProximitySensorEventToIAware(positive);
                Slog.d(TAG, "handleProximitySensorEvent positive:" + positive);
                this.mHandler.removeMessages(2);
                if (positive) {
                    this.mPendingProximity = 1;
                    setPendingProximityDebounceTime(time + 0);
                } else {
                    this.mPendingProximity = 0;
                    setPendingProximityDebounceTime(time + 0);
                }
                debounceProximitySensor();
            }
        }
    }

    private void debounceProximitySensor() {
        if (this.mProximitySensorEnabled && this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                updatePowerState();
                clearPendingProximityDebounceTime();
                return;
            }
            Message msg = this.mHandler.obtainMessage(2);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageAtTime(msg, this.mPendingProximityDebounceTime);
        }
    }

    private void clearPendingProximityDebounceTime() {
        if (this.mPendingProximityDebounceTime >= 0) {
            this.mPendingProximityDebounceTime = -1;
            this.mCallbacks.releaseSuspendBlocker();
        }
    }

    private void setPendingProximityDebounceTime(long debounceTime) {
        if (this.mPendingProximityDebounceTime < 0) {
            this.mCallbacks.acquireSuspendBlocker();
        }
        this.mPendingProximityDebounceTime = debounceTime;
    }

    private void setLABCEnabled(boolean enable) {
        if (enable) {
            if (!this.mLABCSensorEnabled) {
                this.mLABCSensorEnabled = true;
                this.mSensorManager.registerListener(this.mLABCSensorListener, this.mLABCSensor, 500000);
            }
        } else if (this.mLABCSensorEnabled) {
            this.mLABCSensorEnabled = false;
            this.mSensorManager.unregisterListener(this.mLABCSensorListener);
        }
    }

    private void sendOnStateChangedWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnStateChangedRunnable);
    }

    private void sendOnProximityPositiveWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityPositiveRunnable);
    }

    private void sendOnProximityNegativeWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityNegativeRunnable);
    }

    public void dump(final PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println();
            pw.println("Display Power Controller Locked State:");
            pw.println("  mDisplayReadyLocked=" + this.mDisplayReadyLocked);
            pw.println("  mPendingRequestLocked=" + this.mPendingRequestLocked);
            pw.println("  mPendingRequestChangedLocked=" + this.mPendingRequestChangedLocked);
            pw.println("  mPendingWaitForNegativeProximityLocked=" + this.mPendingWaitForNegativeProximityLocked);
            pw.println("  mPendingUpdatePowerStateLocked=" + this.mPendingUpdatePowerStateLocked);
        }
        pw.println();
        pw.println("Display Power Controller Configuration:");
        pw.println("  mScreenBrightnessDozeConfig=" + this.mScreenBrightnessDozeConfig);
        pw.println("  mScreenBrightnessDimConfig=" + this.mScreenBrightnessDimConfig);
        pw.println("  mScreenBrightnessDarkConfig=" + this.mScreenBrightnessDarkConfig);
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mUseSoftwareAutoBrightnessConfig=" + this.mUseSoftwareAutoBrightnessConfig);
        pw.println("  mAllowAutoBrightnessWhileDozingConfig=" + this.mAllowAutoBrightnessWhileDozingConfig);
        pw.println("  mColorFadeFadesConfig=" + this.mColorFadeFadesConfig);
        this.mHandler.runWithScissors(new Runnable() {
            public void run() {
                DisplayPowerController.this.dumpLocal(pw);
            }
        }, 1000);
    }

    private void dumpLocal(PrintWriter pw) {
        pw.println();
        pw.println("Display Power Controller Thread State:");
        pw.println("  mPowerRequest=" + this.mPowerRequest);
        pw.println("  mWaitingForNegativeProximity=" + this.mWaitingForNegativeProximity);
        pw.println("  mProximitySensor=" + this.mProximitySensor);
        pw.println("  mProximitySensorEnabled=" + this.mProximitySensorEnabled);
        pw.println("  mProximityThreshold=" + this.mProximityThreshold);
        pw.println("  mProximity=" + proximityToString(this.mProximity));
        pw.println("  mPendingProximity=" + proximityToString(this.mPendingProximity));
        pw.println("  mPendingProximityDebounceTime=" + TimeUtils.formatUptime(this.mPendingProximityDebounceTime));
        pw.println("  mScreenOffBecauseOfProximity=" + this.mScreenOffBecauseOfProximity);
        pw.println("  mAppliedAutoBrightness=" + this.mAppliedAutoBrightness);
        pw.println("  mAppliedDimming=" + this.mAppliedDimming);
        pw.println("  mAppliedLowPower=" + this.mAppliedLowPower);
        pw.println("  mPendingScreenOnUnblocker=" + this.mPendingScreenOnUnblocker);
        pw.println("  mPendingScreenOff=" + this.mPendingScreenOff);
        pw.println("  mReportedToPolicy=" + reportedToPolicyToString(this.mReportedScreenStateToPolicy));
        pw.println("  mScreenBrightnessRampAnimator.isAnimating()=" + this.mScreenBrightnessRampAnimator.isAnimating());
        if (this.mColorFadeOnAnimator != null) {
            pw.println("  mColorFadeOnAnimator.isStarted()=" + this.mColorFadeOnAnimator.isStarted());
        }
        if (this.mColorFadeOffAnimator != null) {
            pw.println("  mColorFadeOffAnimator.isStarted()=" + this.mColorFadeOffAnimator.isStarted());
        }
        if (this.mPowerState != null) {
            this.mPowerState.dump(pw);
        }
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.dump(pw);
        }
    }

    private static String proximityToString(int state) {
        switch (state) {
            case -1:
                return "Unknown";
            case 0:
                return "Negative";
            case 1:
                return "Positive";
            default:
                return Integer.toString(state);
        }
    }

    private static String reportedToPolicyToString(int state) {
        switch (state) {
            case 0:
                return "REPORTED_TO_POLICY_SCREEN_OFF";
            case 1:
                return "REPORTED_TO_POLICY_SCREEN_TURNING_ON";
            case 2:
                return "REPORTED_TO_POLICY_SCREEN_ON";
            default:
                return Integer.toString(state);
        }
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    private static boolean isScreenOn(int state) {
        return state != 1;
    }

    private static int getScreenOnState(int state) {
        switch (state) {
            case 1:
                return 0;
            case 2:
                return 1;
            default:
                return 2;
        }
    }

    private static Spline createAutoBrightnessSpline(int[] lux, int[] brightness) {
        if (lux == null || lux.length == 0 || brightness == null || brightness.length == 0) {
            Slog.e(TAG, "Could not create auto-brightness spline.");
            return null;
        }
        try {
            int n = brightness.length;
            float[] x = new float[n];
            float[] y = new float[n];
            y[0] = normalizeAbsoluteBrightness(brightness[0]);
            for (int i = 1; i < n; i++) {
                x[i] = (float) lux[i - 1];
                y[i] = normalizeAbsoluteBrightness(brightness[i]);
            }
            Spline spline = Spline.createSpline(x, y);
            if (DEBUG) {
                Slog.d(TAG, "Auto-brightness spline: " + spline);
                for (float v = 1.0f; v < ((float) lux[lux.length - 1]) * 1.25f; v *= 1.25f) {
                    Slog.d(TAG, String.format("  %7.1f: %7.1f", new Object[]{Float.valueOf(v), Float.valueOf(spline.interpolate(v))}));
                }
            }
            return spline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    private static float normalizeAbsoluteBrightness(int value) {
        return ((float) clampAbsoluteBrightness(value)) / 255.0f;
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, 255);
    }

    private boolean checkPhoneWindowIsTop() {
        String incalluiPackageName = "com.android.incallui";
        String incalluiClassName = "com.android.incallui.InCallActivity";
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        long startTime = SystemClock.elapsedRealtime();
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        long getRunningTasksDuration = SystemClock.elapsedRealtime() - startTime;
        if (getRunningTasksDuration > 500) {
            Slog.i(TAG, "Check Phone Window is top, get the Running Tasks duration: " + getRunningTasksDuration);
        }
        if (tasksInfo != null && tasksInfo.size() > 0) {
            ComponentName cn = ((RunningTaskInfo) tasksInfo.get(0)).topActivity;
            if (incalluiPackageName.equals(cn.getPackageName()) && incalluiClassName.equals(cn.getClassName())) {
                if (DEBUG) {
                    Slog.i(TAG, "checkPhoneWindowIsTop: incallui window is top");
                }
                return true;
            }
        }
        return false;
    }

    private void writeAutoBrightnessDbEnable(boolean autoEnable) {
        if (NEED_NEW_BRIGHTNESS_PROCESS) {
            if (this.mPowerRequest.policy == 2 || (autoEnable ^ 1) != 0) {
                this.mBackLight.writeAutoBrightnessDbEnable(false);
            } else if (!this.mPowerPolicyChangeFromDimming) {
                this.mBackLight.writeAutoBrightnessDbEnable(true);
            }
        }
    }

    public void updateProximityState(boolean proximityState) {
        if (DEBUG) {
            Slog.d(TAG, "updateProximityState:" + proximityState);
        }
        this.mProximityPositive = proximityState;
        this.mScreenBrightnessRampAnimator.updateProximityState(proximityState);
    }

    public boolean getManualModeEnable() {
        if (this.mManualBrightnessController != null) {
            return this.mManualBrightnessController.getManualModeEnable();
        }
        Slog.e(TAG, "mManualBrightnessController=null");
        return false;
    }

    public void updatemManualModeAnimationEnable() {
        if (getManualModeEnable() && this.mScreenBrightnessRampAnimator != null) {
            this.mScreenBrightnessRampAnimator.updatemManualModeAnimationEnable(this.mManualBrightnessController.getManualModeAnimationEnable());
        }
    }

    public void updateManualPowerSavingAnimationEnable() {
        if (this.mManualBrightnessController != null && this.mScreenBrightnessRampAnimator != null && this.mAutomaticBrightnessController != null) {
            int brightnessMode = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mPowerRequest.userId);
            boolean autoPowerSavingUseManualAnimationTimeEnable = this.mAutomaticBrightnessController.getAutoPowerSavingUseManualAnimationTimeEnable();
            boolean manualPowerSavingAnimationEnable;
            if (brightnessMode == 1 && autoPowerSavingUseManualAnimationTimeEnable) {
                manualPowerSavingAnimationEnable = this.mAutomaticBrightnessController.getAutoPowerSavingAnimationEnable();
                this.mScreenBrightnessRampAnimator.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable);
                if (manualPowerSavingAnimationEnable) {
                    this.mAutomaticBrightnessController.setAutoPowerSavingAnimationEnable(false);
                    return;
                }
                return;
            }
            manualPowerSavingAnimationEnable = this.mManualBrightnessController.getManualPowerSavingAnimationEnable();
            this.mScreenBrightnessRampAnimator.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable);
            if (manualPowerSavingAnimationEnable) {
                this.mManualBrightnessController.setManualPowerSavingAnimationEnable(false);
            }
        }
    }

    public void updateManualThermalModeAnimationEnable() {
        if (this.mManualBrightnessController != null && this.mScreenBrightnessRampAnimator != null && this.mManualBrightnessController.getManualThermalModeEnable()) {
            boolean manualThermalModeAnimationEnable = this.mManualBrightnessController.getManualThermalModeAnimationEnable();
            this.mScreenBrightnessRampAnimator.updateManualThermalModeAnimationEnable(manualThermalModeAnimationEnable);
            if (manualThermalModeAnimationEnable) {
                this.mManualBrightnessController.setManualThermalModeAnimationEnable(false);
            }
        }
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        return this.mAutomaticBrightnessController.getCoverModeBrightnessFromLastScreenBrightness();
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        this.mAutomaticBrightnessController.setMaxBrightnessFromThermal(brightness);
        this.mManualBrightnessController.setMaxBrightnessFromThermal(brightness);
    }

    public void setModeToAutoNoClearOffsetEnable(boolean enable) {
        this.mModeToAutoNoClearOffsetEnable = enable;
        if (DEBUG) {
            Slog.d(TAG, "set mModeToAutoNoClearOffsetEnable=" + this.mModeToAutoNoClearOffsetEnable);
        }
    }

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        return this.mAutomaticBrightnessController.setScreenBrightnessMappingtoIndoorMax(brightness);
    }

    public void setPoweroffModeChangeAutoEnable(boolean enable) {
        this.mPoweroffModeChangeAutoEnable = enable;
        this.mBrightnessModeChangeNoClearOffsetEnable = this.mPoweroffModeChangeAutoEnable;
        if (DEBUG) {
            Slog.d(TAG, "set mPoweroffModeChangeAutoEnable=" + this.mPoweroffModeChangeAutoEnable + ",mNoClearOffsetEnable=" + this.mBrightnessModeChangeNoClearOffsetEnable);
        }
    }
}
