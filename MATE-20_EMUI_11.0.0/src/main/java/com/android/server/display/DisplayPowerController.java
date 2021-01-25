package com.android.server.display;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.AmbientBrightnessDayStats;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.HwLog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.Display;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.server.FingerprintDataInterface;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.NsdService;
import com.android.server.am.BatteryStatsService;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.IHwDisplayPowerControllerEx;
import com.android.server.display.ManualBrightnessController;
import com.android.server.display.RampAnimator;
import com.android.server.display.whitebalance.DisplayWhiteBalanceController;
import com.android.server.display.whitebalance.DisplayWhiteBalanceFactory;
import com.android.server.display.whitebalance.DisplayWhiteBalanceSettings;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.policy.WindowManagerPolicy;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import java.io.PrintWriter;
import java.util.List;

/* access modifiers changed from: package-private */
public final class DisplayPowerController implements AutomaticBrightnessController.Callbacks, ManualBrightnessController.ManualBrightnessCallbacks, DisplayWhiteBalanceController.Callbacks, IHwDisplayPowerControllerEx.Callbacks {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int COLOR_FADE_OFF_ANIMATION_DURATION_MILLIS = 150;
    private static final int COLOR_FADE_ON_ANIMATION_DURATION_MILLIS = 250;
    private static final int COVER_MODE_DEFAULT_BRIGHTNESS = 33;
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean DEBUG_Controller = false;
    private static final boolean DEBUG_PRETEND_PROXIMITY_SENSOR_ABSENT = false;
    private static final int GET_RUNNING_TASKS_FROM_AMS_WARNING_DURATION_MILLIS = 500;
    private static final int MSG_CONFIGURE_BRIGHTNESS = 5;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_OFF_UNBLOCKED = 4;
    private static final int MSG_SCREEN_ON_EX_UNBLOCKED = 103;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_SET_TEMPORARY_AUTO_BRIGHTNESS_ADJUSTMENT = 7;
    private static final int MSG_SET_TEMPORARY_BRIGHTNESS = 6;
    private static final int MSG_START_DAWN_ANIMATION = 105;
    private static final int MSG_TP_KEEP_STATE_CHANGED = 101;
    private static final int MSG_UPDATE_DISPLAY_MODE = 104;
    private static final int MSG_UPDATE_POWER_STATE = 1;
    private static final int MSG_UPDATE_SCREEN_STATE = 102;
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
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", (String) null));
    private FingerprintDataInterface fpDataCollector;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private boolean mAnimationEnabled;
    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass1 */

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            DisplayPowerController.this.sendUpdatePowerState();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
        }
    };
    private boolean mAppliedAutoBrightness;
    private boolean mAppliedBrightnessBoost;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private boolean mAppliedScreenBrightnessOverride;
    private boolean mAppliedTemporaryAutoBrightnessAdjustment;
    private boolean mAppliedTemporaryBrightness;
    private float mAutoBrightnessAdjustment;
    private boolean mAutoBrightnessEnabled = false;
    private AutomaticBrightnessController mAutomaticBrightnessController;
    private Light mBackLight;
    private final IBatteryStats mBatteryStats;
    private final DisplayBlanker mBlanker;
    private boolean mBrightnessBucketsInDozeConfig;
    private BrightnessConfiguration mBrightnessConfiguration;
    private BrightnessMappingStrategy mBrightnessMapper;
    private boolean mBrightnessModeChangeNoClearOffsetEnable = false;
    private boolean mBrightnessModeChanged = false;
    private final int mBrightnessRampRateFast;
    private final int mBrightnessRampRateSlow;
    private BrightnessReason mBrightnessReason = new BrightnessReason();
    private BrightnessReason mBrightnessReasonTemp = new BrightnessReason();
    private final BrightnessTracker mBrightnessTracker;
    private final DisplayManagerInternal.DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private final boolean mColorFadeEnabled;
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    private final Context mContext;
    private boolean mCoverModeAnimationFast = false;
    private int mCurrentScreenBrightnessSetting;
    private int mCurrentScreenBrightnessSettingForDB;
    private int mCurrentUserId = 0;
    private boolean mCurrentUserIdChange = false;
    private boolean mDisplayBlanksAfterDozeConfig;
    private HwServiceFactory.IDisplayEffectMonitor mDisplayEffectMonitor;
    private HwServiceFactory.IDisplayEngineInterface mDisplayEngineInterface = null;
    private boolean mDisplayReadyLocked;
    private final DisplayWhiteBalanceController mDisplayWhiteBalanceController;
    private final DisplayWhiteBalanceSettings mDisplayWhiteBalanceSettings;
    private boolean mDozing;
    private int mGlobalAlpmState = -1;
    private final DisplayControllerHandler mHandler;
    private IHwDisplayPowerControllerEx mHwDisplayPowerEx = null;
    private HwServiceFactory.IHwSmartBackLightController mHwSmartBackLightController;
    private int mInitialAutoBrightness;
    private boolean mIsCoverModeClosed = true;
    private boolean mIsDawnAnimationPrepared;
    private int mIsScreenOn = 0;
    private boolean mKeyguardIsLocked = false;
    private int mLastBrightnessForAutoBrightnessDB = 0;
    private int mLastBrightnessTarget;
    private int mLastUserSetScreenBrightness;
    private boolean mLastWaitBrightnessMode;
    private boolean mLightSensorOnEnable = false;
    private final LightsManager mLights;
    private final Object mLock = new Object();
    private ManualBrightnessController mManualBrightnessController = null;
    private int mMillisecond;
    private boolean mModeToAutoNoClearOffsetEnable = false;
    private final Runnable mOnProximityNegativeRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass6 */

        @Override // java.lang.Runnable
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityNegative();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityPositiveRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass5 */

        @Override // java.lang.Runnable
        public void run() {
            DisplayPowerController.this.mCallbacks.onProximityPositive();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnStateChangedRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            DisplayPowerController.this.mCallbacks.onStateChanged();
            DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private boolean mOutdoorAnimationFlag = false;
    private float mPendingAutoBrightnessAdjustment;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPendingRequestChangedLocked;
    private DisplayManagerInternal.DisplayPowerRequest mPendingRequestLocked;
    private int mPendingScreenBrightnessSetting;
    private boolean mPendingScreenOff;
    private ScreenOffUnblocker mPendingScreenOffUnblocker;
    private ScreenOnExUnblocker mPendingScreenOnExUnblocker;
    private ScreenOnUnblocker mPendingScreenOnUnblocker;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    private PowerManagerInternal mPowerManagerInternal = ((PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class));
    private boolean mPowerPolicyChangeFromDimming;
    private DisplayManagerInternal.DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private boolean mPoweroffModeChangeAutoEnable = false;
    private int mProximity = -1;
    private boolean mProximityPositive = false;
    private Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass8 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mProximitySensorEnabled) {
                long time = SystemClock.uptimeMillis();
                boolean z = false;
                float distance = event.values[0];
                boolean positive = distance >= 0.0f && distance < DisplayPowerController.this.mProximityThreshold;
                DisplayPowerController.this.mProximitySensorPositive = positive;
                boolean tpKeeped = DisplayPowerController.this.mHwDisplayPowerEx != null && DisplayPowerController.this.mHwDisplayPowerEx.getTpKeep();
                DisplayPowerController displayPowerController = DisplayPowerController.this;
                if (positive || tpKeeped) {
                    z = true;
                }
                displayPowerController.handleProximitySensorEvent(time, z);
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private boolean mProximitySensorPositive;
    private float mProximityThreshold;
    private final RampAnimator.Listener mRampAnimatorListener = new RampAnimator.Listener() {
        /* class com.android.server.display.DisplayPowerController.AnonymousClass2 */

        @Override // com.android.server.display.RampAnimator.Listener
        public void onAnimationEnd() {
            if (DisplayPowerController.this.mUsingHwSmartBackLightController && DisplayPowerController.this.mSmartBackLightEnabled) {
                HwServiceFactory.IHwSmartBackLightController iHwSmartBackLightController = DisplayPowerController.this.mHwSmartBackLightController;
                HwServiceFactory.IHwSmartBackLightController unused = DisplayPowerController.this.mHwSmartBackLightController;
                iHwSmartBackLightController.updateBrightnessState(1);
            }
            if (DisplayPowerController.this.mPowerPolicyChangeFromDimming) {
                DisplayPowerController.this.mPowerPolicyChangeFromDimming = false;
                if (DisplayPowerController.DEBUG && DisplayPowerController.this.mPowerRequest != null) {
                    Slog.i(DisplayPowerController.TAG, "update mPowerPolicyChangeFromDimming mPowerRequest.policy=" + DisplayPowerController.this.mPowerRequest.policy);
                }
                if (!(DisplayPowerController.this.mPowerRequest == null || DisplayPowerController.this.mContext == null || DisplayPowerController.this.mPowerRequest.policy != 0)) {
                    Settings.System.putIntForUser(DisplayPowerController.this.mContext.getContentResolver(), "screen_auto_brightness", 0, DisplayPowerController.this.mCurrentUserId);
                    Slog.i(DisplayPowerController.TAG, "update mPowerPolicyChangeFromDimming set screen_auto_brightness db=0 when poweroff from dim");
                }
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
    private boolean mSREInitialized = false;
    private final int mScreenBrightnessDefault;
    private final int mScreenBrightnessDimConfig;
    private final int mScreenBrightnessDozeConfig;
    private int mScreenBrightnessForVr;
    private final int mScreenBrightnessForVrDefault;
    private final int mScreenBrightnessForVrRangeMaximum;
    private final int mScreenBrightnessForVrRangeMinimum;
    private RampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOffBlockStartRealTime;
    private boolean mScreenOnBecauseOfPhoneProximity;
    private long mScreenOnBlockStartRealTime;
    private long mScreenOnExBlockStartRealTime;
    private HwFoldScreenManagerInternal.ScreenOnUnblockerCallback mScreenOnUnblockerCallback = null;
    private final SensorManager mSensorManager;
    private int mSetBrightnessNoLimitAnimationTime = 500;
    private final SettingsObserver mSettingsObserver;
    private boolean mShouldWaitScreenOnExBlocker;
    private int mSkipRampState = 0;
    private final boolean mSkipScreenOnBrightnessRamp;
    private boolean mSmartBackLightEnabled;
    private boolean mSmartBackLightSupported;
    private float mTemporaryAutoBrightnessAdjustment;
    private int mTemporaryScreenBrightness;
    private boolean mUnfinishedBusiness;
    private boolean mUseSoftwareAutoBrightnessConfig;
    private boolean mUsingHwSmartBackLightController = false;
    private boolean mUsingSRE = false;
    private boolean mWaitingForNegativeProximity;
    private boolean mWakeupFromSleep = true;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private boolean mfastAnimtionFlag = false;

    static {
        boolean z = false;
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        DEBUG_Controller = z;
    }

    public void setAodAlpmState(int globalState) {
        Slog.i(TAG, "mGlobalAlpmState = " + globalState);
        this.mGlobalAlpmState = globalState;
        int i = this.mGlobalAlpmState;
        if (i == 1) {
            sendUpdatePowerState();
            this.mDisplayEngineInterface.setScene("SCENE_AOD", "ACTION_MODE_OFF");
        } else if (i == 0) {
            this.mDisplayEngineInterface.setScene("SCENE_AOD", "ACTION_MODE_ON");
        }
    }

    public void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.setBacklightBrightness(backlightBrightness);
        }
    }

    public void setCameraModeBrightnessLineEnable(boolean cameraModeBrightnessLineEnable) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.setCameraModeBrightnessLineEnable(cameraModeBrightnessLineEnable);
        }
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.updateAutoBrightnessAdjustFactor(adjustFactor);
        }
    }

    /* access modifiers changed from: package-private */
    public int getMaxBrightnessForSeekbar() {
        int maxBrightness = 255;
        if (this.mManualBrightnessController == null) {
            return 255;
        }
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            maxBrightness = automaticBrightnessController.getAutoModeSeekBarMaxBrightness();
        }
        return this.mManualBrightnessController.getMaxBrightnessForSeekbar(maxBrightness);
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
        if (DEBUG) {
            Slog.i(TAG, "setAnimationTime animationEnabled=" + animationEnabled + ",millisecond=" + millisecond);
        }
        this.mAnimationEnabled = animationEnabled;
        this.mMillisecond = millisecond;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
        if (this.mKeyguardIsLocked != isLocked) {
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController != null) {
                automaticBrightnessController.setKeyguardLockedStatus(isLocked);
            }
            RampAnimator<DisplayPowerState> rampAnimator = this.mScreenBrightnessRampAnimator;
            if (rampAnimator != null) {
                rampAnimator.updateScreenLockedAnimationEnable(isLocked);
            }
            this.mKeyguardIsLocked = isLocked;
        }
    }

    public boolean getRebootAutoModeEnable() {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController == null) {
            return false;
        }
        return automaticBrightnessController.getRebootAutoModeEnable();
    }

    public DisplayPowerController(Context context, DisplayManagerInternal.DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
        Resources resources;
        String str;
        DisplayPowerController displayPowerController;
        String str2;
        int initialLightSensorRate;
        this.mHandler = new DisplayControllerHandler(handler.getLooper());
        this.mBrightnessTracker = new BrightnessTracker(context, null);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mCallbacks = callbacks;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mLights = (LightsManager) LocalServices.getService(LightsManager.class);
        this.mSensorManager = sensorManager;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mBlanker = blanker;
        this.mContext = context;
        this.mBackLight = this.mLights.getLight(0);
        Resources resources2 = context.getResources();
        int screenBrightnessSettingMinimum = clampAbsoluteBrightness(resources2.getInteger(17694889));
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources2.getInteger(17694883));
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources2.getInteger(17694882));
        this.mScreenBrightnessRangeMinimum = Math.min(screenBrightnessSettingMinimum, this.mScreenBrightnessDimConfig);
        this.mScreenBrightnessRangeMaximum = clampAbsoluteBrightness(resources2.getInteger(17694888));
        this.mScreenBrightnessDefault = clampAbsoluteBrightness(resources2.getInteger(17694887));
        this.mScreenBrightnessForVrRangeMinimum = clampAbsoluteBrightness(resources2.getInteger(17694886));
        this.mScreenBrightnessForVrRangeMaximum = clampAbsoluteBrightness(resources2.getInteger(17694885));
        this.mScreenBrightnessForVrDefault = clampAbsoluteBrightness(resources2.getInteger(17694884));
        this.mUseSoftwareAutoBrightnessConfig = resources2.getBoolean(17891367);
        this.mAllowAutoBrightnessWhileDozingConfig = resources2.getBoolean(17891342);
        this.mBrightnessRampRateFast = resources2.getInteger(17694752);
        this.mBrightnessRampRateSlow = resources2.getInteger(17694753);
        this.mSkipScreenOnBrightnessRamp = resources2.getBoolean(17891521);
        if (this.mUseSoftwareAutoBrightnessConfig) {
            float dozeScaleFactor = resources2.getFraction(18022406, 1, 1);
            HysteresisLevels ambientBrightnessThresholds = new HysteresisLevels(resources2.getIntArray(17235980), resources2.getIntArray(17235981), resources2.getIntArray(17235982));
            HysteresisLevels screenBrightnessThresholds = new HysteresisLevels(resources2.getIntArray(17236054), resources2.getIntArray(17236057), resources2.getIntArray(17236058));
            long brighteningLightDebounce = (long) resources2.getInteger(17694737);
            long darkeningLightDebounce = (long) resources2.getInteger(17694738);
            boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources2.getBoolean(17891362);
            int lightSensorWarmUpTimeConfig = resources2.getInteger(17694822);
            int lightSensorRate = resources2.getInteger(17694740);
            int initialLightSensorRate2 = resources2.getInteger(17694739);
            if (initialLightSensorRate2 == -1) {
                initialLightSensorRate = lightSensorRate;
            } else {
                if (initialLightSensorRate2 > lightSensorRate) {
                    Slog.w(TAG, "Expected config_autoBrightnessInitialLightSensorRate (" + initialLightSensorRate2 + ") to be less than or equal to config_autoBrightnessLightSensorRate (" + lightSensorRate + ").");
                }
                initialLightSensorRate = initialLightSensorRate2;
            }
            int shortTermModelTimeout = resources2.getInteger(17694741);
            Sensor lightSensor = findDisplayLightSensor(resources2.getString(17039839));
            this.mBrightnessMapper = BrightnessMappingStrategy.create(resources2);
            if (this.mBrightnessMapper != null) {
                HwServiceFactory.IHwAutomaticBrightnessController iadm = HwServiceFactory.getHuaweiAutomaticBrightnessController();
                if (iadm != null) {
                    PackageManager packageManager = context.getPackageManager();
                    Context context2 = this.mContext;
                    str = TAG;
                    displayPowerController = this;
                    resources = resources2;
                    displayPowerController.mAutomaticBrightnessController = iadm.getInstance(displayPowerController, handler.getLooper(), sensorManager, lightSensor, this.mBrightnessMapper, lightSensorWarmUpTimeConfig, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientBrightnessThresholds, screenBrightnessThresholds, (long) shortTermModelTimeout, packageManager, context2);
                } else {
                    str = TAG;
                    resources = resources2;
                    displayPowerController = this;
                    displayPowerController.mAutomaticBrightnessController = new AutomaticBrightnessController(this, handler.getLooper(), sensorManager, lightSensor, displayPowerController.mBrightnessMapper, lightSensorWarmUpTimeConfig, displayPowerController.mScreenBrightnessRangeMinimum, displayPowerController.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientBrightnessThresholds, screenBrightnessThresholds, (long) shortTermModelTimeout, context.getPackageManager());
                }
            } else {
                str = TAG;
                resources = resources2;
                displayPowerController = this;
                displayPowerController.mUseSoftwareAutoBrightnessConfig = false;
            }
            displayPowerController.fpDataCollector = FingerprintDataInterface.getInstance();
        } else {
            str = TAG;
            resources = resources2;
            displayPowerController = this;
        }
        displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
        displayPowerController.mColorFadeFadesConfig = resources.getBoolean(17891359);
        displayPowerController.mDisplayBlanksAfterDozeConfig = resources.getBoolean(17891410);
        displayPowerController.mBrightnessBucketsInDozeConfig = resources.getBoolean(17891411);
        displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
        Sensor sensor = displayPowerController.mProximitySensor;
        if (sensor != null) {
            displayPowerController.mProximityThreshold = Math.min(sensor.getMaximumRange(), (float) TYPICAL_PROXIMITY_THRESHOLD);
        }
        displayPowerController.mDisplayEngineInterface = HwServiceFactory.getDisplayEngineInterface();
        HwServiceFactory.IDisplayEngineInterface iDisplayEngineInterface = displayPowerController.mDisplayEngineInterface;
        if (iDisplayEngineInterface != null) {
            iDisplayEngineInterface.initialize();
            displayPowerController.mUsingSRE = displayPowerController.mDisplayEngineInterface.getSupported("FEATURE_SRE");
            str2 = str;
            Slog.i(str2, "DisplayEngineInterface getSupported SRE:" + displayPowerController.mUsingSRE);
        } else {
            str2 = str;
        }
        displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
        displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
        displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
        displayPowerController.mTemporaryScreenBrightness = -1;
        displayPowerController.mPendingScreenBrightnessSetting = -1;
        displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
        displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
        DisplayWhiteBalanceSettings displayWhiteBalanceSettings = null;
        DisplayWhiteBalanceController displayWhiteBalanceController = null;
        try {
            displayWhiteBalanceSettings = new DisplayWhiteBalanceSettings(displayPowerController.mContext, displayPowerController.mHandler);
            displayWhiteBalanceController = DisplayWhiteBalanceFactory.create(displayPowerController.mHandler, displayPowerController.mSensorManager, resources);
            displayWhiteBalanceSettings.setCallbacks(displayPowerController);
            displayWhiteBalanceController.setCallbacks(displayPowerController);
        } catch (Exception e) {
            Slog.e(str2, "failed to set up display white-balance: " + e);
        }
        displayPowerController.mDisplayWhiteBalanceSettings = displayWhiteBalanceSettings;
        displayPowerController.mDisplayWhiteBalanceController = displayWhiteBalanceController;
        int smartBackLightConfig = SystemProperties.getInt("ro.config.hw_smart_backlight", 1);
        if (displayPowerController.mUsingSRE || smartBackLightConfig == 1) {
            if (displayPowerController.mUsingSRE) {
                Slog.i(str2, "Use SRE instead of SBL");
            } else {
                displayPowerController.mSmartBackLightSupported = true;
                if (DEBUG) {
                    Slog.i(str2, "get ro.config.hw_smart_backlight = 1");
                }
            }
            int smartBackLightSetting = Settings.System.getInt(displayPowerController.mContext.getContentResolver(), "smart_backlight_enable", -1);
            if (smartBackLightSetting == -1) {
                if (DEBUG) {
                    Slog.i(str2, "get Settings.System.SMART_BACKLIGHT failed, set default value to 1");
                }
                Settings.System.putInt(displayPowerController.mContext.getContentResolver(), "smart_backlight_enable", 1);
            } else if (DEBUG) {
                Slog.i(str2, "get Settings.System.SMART_BACKLIGHT = " + smartBackLightSetting);
            }
        } else if (DEBUG) {
            Slog.i(str2, "get ro.config.hw_smart_backlight = " + smartBackLightConfig + ", mUsingSRE = false, don't support sbl or sre");
        }
        if (displayPowerController.mSmartBackLightSupported) {
            displayPowerController.mHwSmartBackLightController = HwServiceFactory.getHwSmartBackLightController();
            HwServiceFactory.IHwSmartBackLightController iHwSmartBackLightController = displayPowerController.mHwSmartBackLightController;
            if (iHwSmartBackLightController != null) {
                displayPowerController.mUsingHwSmartBackLightController = iHwSmartBackLightController.checkIfUsingHwSBL();
                displayPowerController.mHwSmartBackLightController.StartHwSmartBackLightController(displayPowerController.mContext, displayPowerController.mLights, displayPowerController.mSensorManager);
            }
        }
        HwServiceFactory.IHwNormalizedManualBrightnessController iadm2 = HwServiceFactory.getHuaweiManualBrightnessController();
        if (iadm2 != null) {
            displayPowerController.mManualBrightnessController = iadm2.getInstance(displayPowerController, displayPowerController.mContext, displayPowerController.mSensorManager);
            if (DEBUG) {
                Slog.i(str2, "HBM ManualBrightnessController initialized");
            }
        } else {
            displayPowerController.mManualBrightnessController = new ManualBrightnessController(displayPowerController);
        }
        displayPowerController.mDisplayEffectMonitor = HwServiceFactory.getDisplayEffectMonitor(displayPowerController.mContext);
        if (displayPowerController.mDisplayEffectMonitor == null) {
            Slog.e(str2, "getDisplayEffectMonitor failed!");
        }
        displayPowerController.mHwDisplayPowerEx = HwServiceExFactory.getHwDisplayPowerControllerEx(displayPowerController.mContext, displayPowerController);
        IHwDisplayPowerControllerEx iHwDisplayPowerControllerEx = displayPowerController.mHwDisplayPowerEx;
        if (iHwDisplayPowerControllerEx != null) {
            iHwDisplayPowerControllerEx.initTpKeepParamters();
        }
        displayPowerController.mShouldWaitScreenOnExBlocker = displayPowerController.mWindowManagerPolicy.shouldWaitScreenOnExBlocker();
    }

    private Sensor findDisplayLightSensor(String sensorType) {
        if (!TextUtils.isEmpty(sensorType)) {
            List<Sensor> sensors = this.mSensorManager.getSensorList(-1);
            for (int i = 0; i < sensors.size(); i++) {
                Sensor sensor = sensors.get(i);
                if (sensorType.equals(sensor.getStringType())) {
                    return sensor;
                }
            }
        }
        return this.mSensorManager.getDefaultSensor(5);
    }

    public boolean isProximitySensorAvailable() {
        return this.mProximitySensor != null;
    }

    public ParceledListSlice<BrightnessChangeEvent> getBrightnessEvents(int userId, boolean includePackage) {
        return this.mBrightnessTracker.getEvents(userId, includePackage);
    }

    public void onSwitchUser(int newUserId) {
        handleSettingsChange(true);
        this.mBrightnessTracker.onSwitchUser(newUserId);
    }

    public ParceledListSlice<AmbientBrightnessDayStats> getAmbientBrightnessStats(int userId) {
        return this.mBrightnessTracker.getAmbientBrightnessStats(userId);
    }

    public void persistBrightnessTrackerState() {
        this.mBrightnessTracker.persistBrightnessTrackerState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScreenState() {
        blockScreenOn();
        this.mWindowManagerPolicy.setFoldingScreenOffState(true);
        this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
    }

    private void sendUpdatetScreenStateLocked() {
        if (!this.mPendingUpdatePowerStateLocked) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(102));
        }
    }

    public boolean requestScreenState() {
        synchronized (this.mLock) {
            sendUpdatetScreenStateLocked();
        }
        return true;
    }

    public boolean requestPowerState(DisplayManagerInternal.DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        if (DEBUG && DEBUG_Controller) {
            Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity);
        }
        synchronized (this.mLock) {
            boolean changed = false;
            if (waitForNegativeProximity) {
                if (!this.mPendingWaitForNegativeProximityLocked) {
                    this.mPendingWaitForNegativeProximityLocked = true;
                    changed = true;
                }
            }
            if (this.mPendingRequestLocked == null) {
                this.mPendingRequestLocked = new DisplayManagerInternal.DisplayPowerRequest(request);
                changed = true;
            } else if (!this.mPendingRequestLocked.equals(request)) {
                this.mPendingRequestLocked.copyFrom(request);
                changed = true;
            }
            if (changed) {
                this.mDisplayReadyLocked = false;
            }
            if (changed && !this.mPendingRequestChangedLocked) {
                this.mPendingRequestChangedLocked = true;
                sendUpdatePowerStateLocked();
            }
            z = this.mDisplayReadyLocked;
        }
        return z;
    }

    public BrightnessConfiguration getDefaultBrightnessConfiguration() {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController == null) {
            return null;
        }
        return automaticBrightnessController.getDefaultConfig();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUpdatePowerState() {
        synchronized (this.mLock) {
            sendUpdatePowerStateLocked();
        }
    }

    private void sendUpdatePowerStateLocked() {
        if (!this.mPendingUpdatePowerStateLocked) {
            this.mPendingUpdatePowerStateLocked = true;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }
    }

    private void initialize() {
        this.mPowerState = new DisplayPowerState(this.mBlanker, this.mColorFadeEnabled ? new ColorFade(0) : null);
        if (this.mColorFadeEnabled) {
            this.mHwDisplayPowerEx.initializeDawnAnimator(this.mPowerState, this.mAnimatorListener);
            this.mColorFadeOnAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, 0.0f, 1.0f);
            this.mColorFadeOnAnimator.setDuration(250L);
            this.mColorFadeOnAnimator.addListener(this.mAnimatorListener);
            this.mColorFadeOffAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, 1.0f, 0.0f);
            this.mColorFadeOffAnimator.setDuration(150L);
            this.mColorFadeOffAnimator.addListener(this.mAnimatorListener);
        }
        HwServiceFactory.IHwRampAnimator iadm = HwServiceFactory.getHwNormalizedRampAnimator();
        if (iadm != null) {
            this.mScreenBrightnessRampAnimator = iadm.getInstance(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS, this.mContext);
        } else {
            this.mScreenBrightnessRampAnimator = new RampAnimator<>(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
        }
        this.mScreenBrightnessRampAnimator.setListener(this.mRampAnimatorListener);
        try {
            this.mBatteryStats.noteScreenState(this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenBrightness(this.mPowerState.getScreenBrightness());
        } catch (RemoteException e) {
        }
        float brightness = convertToNits(this.mPowerState.getScreenBrightness());
        if (brightness >= 0.0f) {
            this.mBrightnessTracker.start(brightness);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_brightness"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_brightness_for_vr"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_auto_brightness_adj"), false, this.mSettingsObserver, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:412:0x064b  */
    /* JADX WARNING: Removed duplicated region for block: B:414:0x064f A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x0661  */
    private void updatePowerState() {
        Throwable th;
        int previousPolicy;
        boolean mustNotify;
        int state;
        float autoBrightnessAdjustment;
        int brightnessAdjustmentFlags;
        int brightnessAdjustmentFlags2;
        boolean slowChange;
        int brightnessAdjustmentFlags3;
        boolean z;
        boolean hasBrightnessBuckets;
        boolean brightnessIsTemporary;
        AutomaticBrightnessController automaticBrightnessController;
        boolean z2;
        boolean z3;
        boolean slowChange2;
        boolean slowChange3;
        float newAutoBrightnessAdjustment;
        boolean mustInitialize = false;
        this.mBrightnessReasonTemp.set(null);
        synchronized (this.mLock) {
            try {
                this.mPendingUpdatePowerStateLocked = false;
                if (this.mPendingRequestLocked != null) {
                    if (this.mPowerRequest == null) {
                        this.mPowerRequest = new DisplayManagerInternal.DisplayPowerRequest(this.mPendingRequestLocked);
                        this.mWaitingForNegativeProximity = this.mPendingWaitForNegativeProximityLocked;
                        this.mPendingWaitForNegativeProximityLocked = false;
                        this.mPendingRequestChangedLocked = false;
                        mustInitialize = true;
                        previousPolicy = 3;
                    } else if (this.mPendingRequestChangedLocked) {
                        previousPolicy = this.mPowerRequest.policy;
                        this.mBrightnessModeChanged = this.mPowerRequest.useAutoBrightness != this.mPendingRequestLocked.useAutoBrightness;
                        if (this.mBrightnessModeChanged && this.mPowerRequest.useAutoBrightness) {
                            updateBrightnessModeChangeManualState(this.mPowerRequest.useAutoBrightness);
                        }
                        if (this.mBrightnessModeChanged && this.mPowerRequest.screenBrightnessOverride > 0 && this.mPendingRequestLocked.screenBrightnessOverride < 0) {
                            this.mBrightnessModeChanged = false;
                            if (DEBUG) {
                                Slog.i(TAG, "mBrightnessModeChanged without db,brightness=" + this.mPowerRequest.screenBrightnessOverride + ",mPendingBrightness=" + this.mPendingRequestLocked.screenBrightnessOverride);
                            }
                        }
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
                        boolean modeChangeKeepBrightnessOffset = this.mAutomaticBrightnessController.getGameDisableAutoBrightnessModeKeepOffsetEnable();
                        if (this.mBrightnessModeChanged && !this.mCurrentUserIdChange && this.mPowerRequest.useAutoBrightness && !isCoverModeChanged && !this.mIsCoverModeClosed && !this.mBrightnessModeChangeNoClearOffsetEnable && !this.mModeToAutoNoClearOffsetEnable && !modeChangeKeepBrightnessOffset) {
                            updateAutoBrightnessAdjustFactor(0.0f);
                            if (DEBUG) {
                                Slog.i(TAG, "AdjustPositionBrightness set 0");
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
                        if (this.mBrightnessModeChanged && modeChangeKeepBrightnessOffset) {
                            this.mAutomaticBrightnessController.setGameDisableAutoBrightnessModeKeepOffsetEnable(false);
                            Slog.i(TAG, "setGameDisableAutoBrightnessModeKeepOffsetEnable=false");
                        }
                        this.mCurrentUserIdChange = false;
                        this.mWaitingForNegativeProximity |= this.mPendingWaitForNegativeProximityLocked;
                        this.mPendingWaitForNegativeProximityLocked = false;
                        this.mPendingRequestChangedLocked = false;
                        this.mDisplayReadyLocked = false;
                        writeAutoBrightnessDbEnable(this.mPowerRequest.useAutoBrightness);
                    } else {
                        previousPolicy = this.mPowerRequest.policy;
                    }
                    try {
                        mustNotify = !this.mDisplayReadyLocked;
                        this.mScreenOnBecauseOfPhoneProximity = false;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } else {
                    return;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (mustInitialize) {
            initialize();
        }
        int brightness = -1;
        boolean performScreenOffTransition = false;
        int i = this.mPowerRequest.policy;
        if (i == 0) {
            state = 1;
            performScreenOffTransition = true;
        } else if (i == 1) {
            if (this.mPowerRequest.dozeScreenState != 0) {
                state = this.mPowerRequest.dozeScreenState;
            } else {
                state = 3;
            }
            if (!this.mAllowAutoBrightnessWhileDozingConfig) {
                brightness = this.mPowerRequest.dozeScreenBrightness;
                this.mBrightnessReasonTemp.setReason(2);
            }
        } else if (i != 4) {
            state = 2;
        } else {
            state = 5;
        }
        if (this.mProximitySensor != null) {
            IHwDisplayPowerControllerEx iHwDisplayPowerControllerEx = this.mHwDisplayPowerEx;
            if (iHwDisplayPowerControllerEx != null) {
                iHwDisplayPowerControllerEx.setTPDozeMode(this.mPowerRequest.useProximitySensor);
            }
            if (this.mPowerRequest.useProximitySensor && state != 1) {
                setProximitySensorEnabled(true);
                if (!this.mScreenOffBecauseOfProximity && this.mProximity == 1) {
                    this.mScreenOffBecauseOfProximity = true;
                    sendOnProximityPositiveWithWakelock();
                }
            } else if (this.mWaitingForNegativeProximity && this.mScreenOffBecauseOfProximity && this.mProximity == 1 && state != 1) {
                setProximitySensorEnabled(true);
            } else if (this.mWaitingForNegativeProximity || !this.mScreenOffBecauseOfProximity || this.mProximity == -1 || state != 1 || !this.mPowerRequest.useProximitySensor) {
                if (!this.mPowerRequest.useProximitySensor) {
                    setProximitySensorEnabled(false);
                }
                this.mWaitingForNegativeProximity = false;
            } else {
                setProximitySensorEnabled(true);
            }
            if (!this.mScreenOffBecauseOfProximity && this.mProximity == 1) {
                this.mScreenOffBecauseOfProximity = true;
                sendOnProximityPositiveWithWakelock();
            }
            if (this.mScreenOffBecauseOfProximity && this.mProximity != 1) {
                this.mScreenOffBecauseOfProximity = false;
                if (this.mPowerRequest.useProximitySensor && this.mPowerRequest.useProximitySensorbyPhone) {
                    this.mScreenOnBecauseOfPhoneProximity = true;
                }
                sendOnProximityNegativeWithWakelock();
            }
        } else {
            this.mWaitingForNegativeProximity = false;
        }
        if (this.mScreenOffBecauseOfProximity && !this.mPowerRequest.useProximitySensorbyPhone) {
            state = 1;
        }
        sre_init(state);
        hbm_init(state);
        sendScreenStateToDE(state);
        if (performScreenOffTransition) {
            performScreenOffTransition &= !(this.mLastWaitBrightnessMode && !this.mPowerRequest.brightnessWaitMode && !this.mPowerRequest.brightnessWaitRet);
        }
        int oldState = this.mPowerState.getScreenState();
        animateScreenStateChange(state, performScreenOffTransition);
        int state2 = this.mPowerState.getScreenState();
        if (state2 == 1) {
            brightness = 0;
            this.mBrightnessReasonTemp.setReason(5);
            this.mWakeupFromSleep = true;
            this.mProximityPositive = false;
        }
        if (state2 == 5) {
            brightness = this.mScreenBrightnessForVr;
            this.mBrightnessReasonTemp.setReason(6);
        }
        if (brightness >= 0 || this.mPowerRequest.screenBrightnessOverride <= 0) {
            this.mAppliedScreenBrightnessOverride = false;
        } else {
            brightness = this.mPowerRequest.screenBrightnessOverride;
            this.mBrightnessReasonTemp.setReason(7);
            this.mTemporaryScreenBrightness = -1;
            this.mAppliedScreenBrightnessOverride = true;
        }
        boolean autoBrightnessEnabled = this.mPowerRequest.useAutoBrightness && (state2 == 2 || (this.mAllowAutoBrightnessWhileDozingConfig && Display.isDozeState(state2))) && brightness < 0 && this.mAutomaticBrightnessController != null;
        boolean userSetBrightnessChanged = updateUserSetScreenBrightness();
        if (userSetBrightnessChanged || autoBrightnessEnabled) {
            this.mTemporaryScreenBrightness = -1;
        }
        if (this.mTemporaryScreenBrightness > 0) {
            brightness = this.mTemporaryScreenBrightness;
            this.mAppliedTemporaryBrightness = true;
            this.mBrightnessReasonTemp.setReason(8);
        } else {
            this.mAppliedTemporaryBrightness = false;
        }
        boolean autoBrightnessAdjustmentChanged = updateAutoBrightnessAdjustment();
        if (autoBrightnessAdjustmentChanged) {
            this.mTemporaryAutoBrightnessAdjustment = Float.NaN;
        }
        if (!Float.isNaN(this.mTemporaryAutoBrightnessAdjustment)) {
            autoBrightnessAdjustment = this.mTemporaryAutoBrightnessAdjustment;
            brightnessAdjustmentFlags = 1;
            this.mAppliedTemporaryAutoBrightnessAdjustment = true;
        } else {
            autoBrightnessAdjustment = this.mAutoBrightnessAdjustment;
            brightnessAdjustmentFlags = 2;
            this.mAppliedTemporaryAutoBrightnessAdjustment = false;
        }
        if (!this.mPowerRequest.boostScreenBrightness || brightness == 0) {
            this.mAppliedBrightnessBoost = false;
        } else {
            brightness = 255;
            this.mBrightnessReasonTemp.setReason(9);
            this.mAppliedBrightnessBoost = true;
        }
        boolean userInitiatedChange = brightness < 0 && (autoBrightnessAdjustmentChanged || userSetBrightnessChanged);
        boolean hadUserBrightnessPoint = false;
        AutomaticBrightnessController automaticBrightnessController2 = this.mAutomaticBrightnessController;
        if (automaticBrightnessController2 != null) {
            automaticBrightnessController2.updatePowerPolicy(this.mPowerRequest.policy);
            hadUserBrightnessPoint = this.mAutomaticBrightnessController.hasUserDataPoints();
            brightnessAdjustmentFlags2 = brightnessAdjustmentFlags;
            this.mAutomaticBrightnessController.configure(autoBrightnessEnabled, this.mBrightnessConfiguration, ((float) this.mLastUserSetScreenBrightness) / 255.0f, userSetBrightnessChanged, autoBrightnessAdjustment, autoBrightnessAdjustmentChanged, this.mPowerRequest.policy);
            this.mAutomaticBrightnessController.updateCurrentUserId(this.mPowerRequest.userId);
        } else {
            brightnessAdjustmentFlags2 = brightnessAdjustmentFlags;
        }
        this.mManualBrightnessController.updateCurrentUserId(this.mPowerRequest.userId);
        if (this.mAutoBrightnessEnabled != autoBrightnessEnabled) {
            if (DEBUG) {
                Slog.i(TAG, "mode change : autoBrightnessEnabled=" + autoBrightnessEnabled + ",state=" + state2);
            }
            this.mAutoBrightnessEnabled = autoBrightnessEnabled;
        }
        updatemAnimationState(autoBrightnessEnabled);
        updatemManualModeAnimationEnable();
        updateManualPowerSavingAnimationEnable();
        updateManualThermalModeAnimationEnable();
        updateBrightnessModeAnimationEnable();
        updateDarkAdaptDimmingEnable();
        updateFastDarkenDimmingEnable();
        updateNightUpPowerOnWithDimmingEnable();
        this.mBackLight.updateBrightnessAdjustMode(autoBrightnessEnabled);
        if (waitScreenBrightness(state2, this.mPowerRequest.brightnessWaitMode)) {
            brightness = 0;
        }
        if (this.mGlobalAlpmState == 0) {
            brightness = 0;
        }
        if (brightness < 0) {
            float newAutoBrightnessAdjustment2 = autoBrightnessAdjustment;
            if (autoBrightnessEnabled) {
                brightness = this.mAutomaticBrightnessController.getAutomaticScreenBrightness();
                float newAutoBrightnessAdjustment3 = this.mAutomaticBrightnessController.getAutomaticScreenBrightnessAdjustmentNew(brightness);
                if (brightness >= 0 || SystemClock.uptimeMillis() - this.mAutomaticBrightnessController.getLightSensorEnableTime() <= 195) {
                    slowChange2 = false;
                    newAutoBrightnessAdjustment = newAutoBrightnessAdjustment3;
                } else {
                    if (brightness != -2) {
                        slowChange2 = false;
                        newAutoBrightnessAdjustment = newAutoBrightnessAdjustment3;
                        brightness = Settings.System.getInt(this.mContext.getContentResolver(), "screen_brightness", 100);
                    } else {
                        slowChange2 = false;
                        newAutoBrightnessAdjustment = newAutoBrightnessAdjustment3;
                    }
                    if (DEBUG) {
                        Slog.i(TAG, "failed to get auto brightness, get SCREEN_BRIGHTNESS:" + brightness);
                    }
                }
                newAutoBrightnessAdjustment2 = newAutoBrightnessAdjustment;
            } else {
                slowChange2 = false;
            }
            if (brightness >= 0) {
                int brightness2 = clampScreenBrightness(brightness);
                if (this.mAppliedAutoBrightness && !autoBrightnessAdjustmentChanged) {
                    slowChange2 = true;
                }
                putScreenBrightnessSetting(brightness2);
                this.mAppliedAutoBrightness = true;
                this.mBrightnessReasonTemp.setReason(4);
                brightness = brightness2;
                slowChange3 = slowChange2;
            } else {
                this.mAppliedAutoBrightness = brightness == -2;
                slowChange3 = slowChange2;
            }
            if (autoBrightnessAdjustment != newAutoBrightnessAdjustment2) {
                putAutoBrightnessAdjustmentSetting(newAutoBrightnessAdjustment2);
            } else {
                brightnessAdjustmentFlags2 = 0;
            }
            slowChange = slowChange3;
            brightnessAdjustmentFlags3 = brightnessAdjustmentFlags2;
        } else {
            slowChange = false;
            this.mAppliedAutoBrightness = false;
            brightnessAdjustmentFlags3 = 0;
        }
        if (brightness < 0 && Display.isDozeState(state2)) {
            brightness = this.mScreenBrightnessDozeConfig;
            this.mBrightnessReasonTemp.setReason(3);
        }
        if (brightness < 0 && !this.mPowerRequest.useAutoBrightness) {
            brightness = clampScreenBrightness(getScreenBrightnessSetting());
            this.mBrightnessReasonTemp.setReason(1);
            AutomaticBrightnessController automaticBrightnessController3 = this.mAutomaticBrightnessController;
            if (automaticBrightnessController3 != null) {
                automaticBrightnessController3.updateIntervenedAutoBrightness(brightness);
            }
        }
        if (state2 == 2 && this.mPowerRequest.policy == 0) {
            if (brightness > this.mScreenBrightnessRangeMinimum) {
                brightness = Math.max(Math.min(brightness - 10, this.mScreenBrightnessDimConfig), this.mScreenBrightnessRangeMinimum);
            }
            Slog.i(TAG, "set brightness to DIM brightness:" + brightness);
        }
        if (this.mPowerRequest.policy == 2) {
            if (brightness > this.mScreenBrightnessRangeMinimum) {
                int brightness3 = Math.max(Math.min(brightness - 10, this.mScreenBrightnessDimConfig), this.mScreenBrightnessRangeMinimum);
                this.mBrightnessReasonTemp.addModifier(1);
                brightness = brightness3;
            }
            blackScreenOnPcMode();
            if (!this.mAppliedDimming) {
                slowChange = false;
            }
            this.mAppliedDimming = true;
        } else if (this.mAppliedDimming) {
            slowChange = false;
            this.mAppliedDimming = false;
        }
        if (this.mPowerRequest.lowPowerMode) {
            if (brightness > this.mScreenBrightnessRangeMinimum) {
                int brightness4 = Math.max((int) (((float) brightness) * Math.min(this.mPowerRequest.screenLowPowerBrightnessFactor, 1.0f)), this.mScreenBrightnessRangeMinimum);
                this.mBrightnessReasonTemp.addModifier(2);
                brightness = brightness4;
            }
            if (!this.mAppliedLowPower) {
                slowChange = false;
            }
            this.mAppliedLowPower = true;
        } else if (this.mAppliedLowPower) {
            slowChange = false;
            this.mAppliedLowPower = false;
        }
        if (!this.mPendingScreenOff) {
            if (this.mSkipScreenOnBrightnessRamp) {
                if (state2 != 2) {
                    this.mSkipRampState = 0;
                } else if (this.mSkipRampState == 0 && this.mDozing) {
                    this.mInitialAutoBrightness = brightness;
                    this.mSkipRampState = 1;
                } else if (this.mSkipRampState == 1 && this.mUseSoftwareAutoBrightnessConfig && brightness != this.mInitialAutoBrightness) {
                    this.mSkipRampState = 2;
                } else if (this.mSkipRampState == 2) {
                    this.mSkipRampState = 0;
                }
            }
            if (HwFrameworkFactory.getVRSystemServiceManager().isVRDeviceConnected()) {
                brightness = 0;
            }
            boolean wasOrWillBeInVr = state2 == 5 || oldState == 5;
            boolean initialRampSkip = state2 == 2 && this.mSkipRampState != 0;
            if (Display.isDozeState(state2)) {
                if (this.mBrightnessBucketsInDozeConfig) {
                    hasBrightnessBuckets = true;
                    boolean isDisplayContentVisible = !this.mColorFadeEnabled && this.mPowerState.getColorFadeLevel() == 1.0f;
                    brightnessIsTemporary = !this.mAppliedTemporaryBrightness || this.mAppliedTemporaryAutoBrightnessAdjustment;
                    if (initialRampSkip && !hasBrightnessBuckets && !wasOrWillBeInVr && isDisplayContentVisible && !brightnessIsTemporary) {
                        if (!this.mBrightnessBucketsInDozeConfig) {
                            if (state2 != 2 || !this.mWakeupFromSleep) {
                                AutomaticBrightnessController automaticBrightnessController4 = this.mAutomaticBrightnessController;
                                if (automaticBrightnessController4 == null || !automaticBrightnessController4.getSetbrightnessImmediateEnableForCaliTest()) {
                                    animateScreenBrightness(brightness, slowChange ? this.mBrightnessRampRateSlow : this.mBrightnessRampRateFast);
                                    if (brightnessIsTemporary) {
                                        if (userInitiatedChange && ((automaticBrightnessController = this.mAutomaticBrightnessController) == null || !automaticBrightnessController.hasValidAmbientLux())) {
                                            userInitiatedChange = false;
                                        }
                                        notifyBrightnessChanged(brightness, userInitiatedChange, hadUserBrightnessPoint);
                                    }
                                } else {
                                    animateScreenBrightness(brightness, 0);
                                    if (brightnessIsTemporary) {
                                    }
                                }
                            } else {
                                AutomaticBrightnessController automaticBrightnessController5 = this.mAutomaticBrightnessController;
                                if (automaticBrightnessController5 == null) {
                                    z3 = false;
                                } else if (!automaticBrightnessController5.getRebootFirstBrightnessAnimationEnable() || !this.mRebootWakeupFromSleep) {
                                    z3 = false;
                                } else {
                                    if (brightness > 0) {
                                        animateScreenBrightness(brightness, slowChange ? this.mBrightnessRampRateSlow : this.mBrightnessRampRateFast);
                                        this.mRebootWakeupFromSleep = false;
                                        this.mWakeupFromSleep = false;
                                    } else {
                                        animateScreenBrightness(brightness, 0);
                                    }
                                    if (brightnessIsTemporary) {
                                    }
                                }
                                if (brightness > 0) {
                                    this.mWakeupFromSleep = z3;
                                }
                                int i2 = z3 ? 1 : 0;
                                int i3 = z3 ? 1 : 0;
                                int i4 = z3 ? 1 : 0;
                                animateScreenBrightness(brightness, i2);
                                if (brightnessIsTemporary) {
                                }
                            }
                        }
                    }
                    if (brightnessIsTemporary || this.mPowerRequest.useAutoBrightness) {
                        z2 = false;
                        animateScreenBrightness(brightness, 0);
                    } else {
                        animateScreenBrightness(brightness, slowChange ? this.mBrightnessRampRateSlow : this.mBrightnessRampRateFast);
                        z2 = false;
                    }
                    if (brightness > 0) {
                        this.mWakeupFromSleep = z2;
                    }
                    if (brightnessIsTemporary) {
                    }
                }
            }
            hasBrightnessBuckets = false;
            if (!this.mColorFadeEnabled) {
            }
            if (!this.mAppliedTemporaryBrightness) {
            }
            if (initialRampSkip) {
            }
            if (brightnessIsTemporary) {
            }
            z2 = false;
            animateScreenBrightness(brightness, 0);
            if (brightness > 0) {
            }
            if (brightnessIsTemporary) {
            }
        }
        this.mLastWaitBrightnessMode = this.mPowerRequest.brightnessWaitMode;
        if (!this.mBrightnessReasonTemp.equals(this.mBrightnessReason) || brightnessAdjustmentFlags3 != 0) {
            Slog.v(TAG, "Brightness [" + brightness + "] reason changing to: '" + this.mBrightnessReasonTemp.toString(brightnessAdjustmentFlags3) + "', previous reason: '" + this.mBrightnessReason + "'.");
            this.mBrightnessReason.set(this.mBrightnessReasonTemp);
        }
        if (this.mDisplayWhiteBalanceController != null) {
            if (state2 != 2 || !this.mDisplayWhiteBalanceSettings.isEnabled()) {
                this.mDisplayWhiteBalanceController.setEnabled(false);
            } else {
                this.mDisplayWhiteBalanceController.setEnabled(true);
                this.mDisplayWhiteBalanceController.updateDisplayColorTemperature();
            }
        }
        boolean ready = this.mPendingScreenOnUnblocker == null && this.mPendingScreenOnExUnblocker == null && (!this.mColorFadeEnabled || (!this.mHwDisplayPowerEx.checkDawnAnimationStarted() && !this.mColorFadeOnAnimator.isStarted() && !this.mColorFadeOffAnimator.isStarted())) && this.mPowerState.waitUntilClean(this.mCleanListener);
        boolean finished = ready && !this.mScreenBrightnessRampAnimator.isAnimating();
        if (ready && state2 != 1 && this.mReportedScreenStateToPolicy == 1) {
            setReportedScreenState(2);
            this.mWindowManagerPolicy.screenTurnedOn();
        }
        if (!finished && !this.mUnfinishedBusiness) {
            if (DEBUG) {
                Slog.i(TAG, "Unfinished business...");
            }
            this.mCallbacks.acquireSuspendBlocker();
            this.mUnfinishedBusiness = true;
        }
        if (ready && mustNotify) {
            synchronized (this.mLock) {
                if (!this.mPendingRequestChangedLocked) {
                    this.mDisplayReadyLocked = true;
                    if (DEBUG) {
                        Slog.i(TAG, "Display ready!");
                    }
                }
            }
            sendOnStateChangedWithWakelock();
        }
        if (!finished || !this.mUnfinishedBusiness) {
            z = false;
        } else {
            if (DEBUG) {
                Slog.i(TAG, "Finished business...");
            }
            FingerprintDataInterface fingerprintDataInterface = this.fpDataCollector;
            if (fingerprintDataInterface != null && brightness > 0) {
                fingerprintDataInterface.reportScreenTurnedOn();
            }
            z = false;
            this.mUnfinishedBusiness = false;
            this.mCallbacks.releaseSuspendBlocker();
        }
        if (state2 != 2) {
            z = true;
        }
        this.mDozing = z;
        if (previousPolicy != this.mPowerRequest.policy) {
            logDisplayPolicyChanged(this.mPowerRequest.policy);
        }
    }

    private void blackScreenOnPcMode() {
        if ((HwPCUtils.isPcCastModeInServer() || HwPCUtils.getPhoneDisplayID() != -1) && !HwPCUtils.enabledInPad() && !HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) {
            HwPCUtils.log(TAG, "black Screen in PC mode");
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (pcMgr != null) {
                    pcMgr.setScreenPower(false);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "blackScreenOnPcMode RemoteException.");
            }
        }
    }

    private void sre_init(int state) {
        boolean lightSensorOnEnable;
        if (this.mSmartBackLightSupported && this.mSmartBackLightEnabled != this.mPowerRequest.useSmartBacklight) {
            if (DEBUG) {
                Slog.i(TAG, "mPowerRequest.useSmartBacklight change " + this.mSmartBackLightEnabled + " -> " + this.mPowerRequest.useSmartBacklight);
            }
            this.mSmartBackLightEnabled = this.mPowerRequest.useSmartBacklight;
        }
        if (this.mUsingHwSmartBackLightController) {
            this.mHwSmartBackLightController.updatePowerState(state, this.mSmartBackLightEnabled);
        }
        if (!(this.mDisplayEngineInterface == null || this.mLightSensorOnEnable == (lightSensorOnEnable = wantScreenOn(state)))) {
            Slog.i(TAG, "LightSensorEnable change " + this.mLightSensorOnEnable + " -> " + lightSensorOnEnable);
            this.mDisplayEngineInterface.updateLightSensorState(lightSensorOnEnable);
            this.mLightSensorOnEnable = lightSensorOnEnable;
        }
        if (this.mUsingSRE && this.mDisplayEngineInterface != null) {
            if (!this.mSREInitialized || this.mSREEnabled != this.mPowerRequest.useSmartBacklight) {
                this.mSREInitialized = true;
                this.mSREEnabled = this.mPowerRequest.useSmartBacklight;
                Slog.i(TAG, "mPowerRequest.useSmartBacklight : " + this.mSREEnabled);
                if (this.mSREEnabled) {
                    this.mDisplayEngineInterface.setScene("SCENE_SRE", "ACTION_MODE_ON");
                } else {
                    this.mDisplayEngineInterface.setScene("SCENE_SRE", "ACTION_MODE_OFF");
                }
            }
        }
    }

    private void hbm_init(int state) {
        if (SystemProperties.getInt("ro.config.hw_high_bright_mode", 1) == 1 && getManualModeEnable()) {
            boolean isManulMode = true ^ this.mPowerRequest.useAutoBrightness;
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController != null) {
                automaticBrightnessController.setManualModeEnableForPg(isManulMode);
            }
            this.mManualBrightnessController.updatePowerState(state, isManulMode);
        }
    }

    private void sendScreenStateToDE(int state) {
        int currentState;
        if (this.mDisplayEngineInterface != null && this.mIsScreenOn != (currentState = getScreenOnState(state))) {
            Slog.i(TAG, "ScreenState change " + this.mIsScreenOn + " -> " + currentState);
            if (currentState == 1) {
                this.mDisplayEngineInterface.setScene("SCENE_REAL_POWERMODE", "ACTION_MODE_ON");
                HwServiceFactory.setPowerState(0);
            } else {
                this.mDisplayEngineInterface.setScene("SCENE_REAL_POWERMODE", "ACTION_MODE_OFF");
                HwServiceFactory.setPowerState(1);
            }
            this.mIsScreenOn = currentState;
        }
    }

    private void updateCoverModeStatus(boolean isClosed) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.setCoverModeStatus(isClosed);
        }
    }

    @Override // com.android.server.display.AutomaticBrightnessController.Callbacks
    public void updateBrightness() {
        sendUpdatePowerState();
    }

    @Override // com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks
    public void updateManualBrightnessForLux() {
        sendUpdatePowerState();
    }

    public void setBrightnessConfiguration(BrightnessConfiguration c) {
        this.mHandler.obtainMessage(5, c).sendToTarget();
    }

    public void setTemporaryBrightness(int brightness) {
        this.mHandler.obtainMessage(6, brightness, 0).sendToTarget();
    }

    public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
        this.mHandler.obtainMessage(7, Float.floatToIntBits(adjustment), 0).sendToTarget();
    }

    private void blockScreenOn() {
        if (this.mPendingScreenOnUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOnUnblocker = new ScreenOnUnblocker();
            this.mScreenOnBlockStartRealTime = SystemClock.elapsedRealtime();
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Blocking screen on until initial contents have been drawn.");
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "Blocking screen on until initial contents have been drawn");
                iZrHung.addInfo(arg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unblockScreenOn() {
        if (this.mPendingScreenOnUnblocker != null) {
            this.mPendingScreenOnUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOnBlockStartRealTime;
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Unblocked screen on after " + delay + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "Unblocked screen on after " + delay + " ms");
                iZrHung.addInfo(arg);
            }
            ZrHungData arg2 = this.mScreenOnUnblockerCallback;
            if (arg2 != null) {
                arg2.onScreenOnUnblocker();
                this.mScreenOnUnblockerCallback = null;
            }
        }
    }

    private void blockScreenOnEx() {
        if (this.mShouldWaitScreenOnExBlocker && this.mPendingScreenOnExUnblocker == null) {
            this.mPendingScreenOnExUnblocker = new ScreenOnExUnblocker();
            this.mScreenOnExBlockStartRealTime = SystemClock.elapsedRealtime();
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Blocking screen on until additional contents has been ready.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unblockScreenOnEx() {
        if (this.mShouldWaitScreenOnExBlocker && this.mPendingScreenOnExUnblocker != null) {
            this.mPendingScreenOnExUnblocker = null;
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Unblocked screen on ex after " + (SystemClock.elapsedRealtime() - this.mScreenOnExBlockStartRealTime) + " ms");
        }
    }

    private void blockScreenOff() {
        if (this.mPendingScreenOffUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
            this.mPendingScreenOffUnblocker = new ScreenOffUnblocker();
            this.mScreenOffBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "UL_Power Blocking screen off");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unblockScreenOff() {
        if (this.mPendingScreenOffUnblocker != null) {
            this.mPendingScreenOffUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOffBlockStartRealTime;
            Slog.i(TAG, "UL_PowerUnblocked screen off after " + delay + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
        }
    }

    private boolean setScreenState(int state) {
        return setScreenState(state, false);
    }

    private boolean setScreenState(int state, boolean reportOnly) {
        boolean isOff = state == 1;
        if (this.mPowerState.getScreenState() != state) {
            if (isOff && !this.mScreenOffBecauseOfProximity && !this.mScreenOnBecauseOfPhoneProximity) {
                if (this.mReportedScreenStateToPolicy == 2) {
                    setReportedScreenState(3);
                    blockScreenOff();
                    this.mWindowManagerPolicy.screenTurningOff(this.mPendingScreenOffUnblocker);
                    unblockScreenOff();
                } else if (this.mPendingScreenOffUnblocker != null) {
                    return false;
                }
            }
            if (!reportOnly) {
                Trace.traceCounter(131072, "ScreenState", state);
                this.mPowerState.setScreenState(state);
                try {
                    this.mBatteryStats.noteScreenState(state);
                } catch (RemoteException e) {
                }
            }
        }
        boolean isDoze = mSupportAod && (state == 3 || state == 4);
        if (isOff && this.mReportedScreenStateToPolicy != 0 && !this.mScreenOffBecauseOfProximity && !this.mScreenOnBecauseOfPhoneProximity) {
            setReportedScreenState(0);
            unblockScreenOn();
            unblockScreenOnEx();
            this.mWindowManagerPolicy.screenTurnedOff();
            setBrightnessAnimationTime(false, 500);
            setBrightnessNoLimit(-1, 500);
            boolean z = this.mPoweroffModeChangeAutoEnable;
            this.mBrightnessModeChangeNoClearOffsetEnable = z;
            if (z) {
                this.mPoweroffModeChangeAutoEnable = false;
                Slog.i(TAG, "poweroff set mPoweroffModeChangeAutoEnable=" + this.mPoweroffModeChangeAutoEnable);
            }
        } else if (!isOff && !isDoze && this.mReportedScreenStateToPolicy == 3) {
            unblockScreenOff();
            this.mWindowManagerPolicy.screenTurnedOff();
            setReportedScreenState(0);
        }
        if (!isOff && !isDoze && this.mReportedScreenStateToPolicy == 0) {
            setReportedScreenState(1);
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                blockScreenOn();
                blockScreenOnEx();
            } else {
                unblockScreenOn();
                unblockScreenOnEx();
            }
            this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
            if (this.mShouldWaitScreenOnExBlocker) {
                this.mWindowManagerPolicy.screenTurningOnEx(this.mPendingScreenOnExUnblocker);
            }
        }
        return this.mPendingScreenOnUnblocker == null && this.mPendingScreenOnExUnblocker == null;
    }

    private void setReportedScreenState(int state) {
        Trace.traceCounter(131072, "ReportedScreenStateToPolicy", state);
        this.mReportedScreenStateToPolicy = state;
    }

    private boolean waitScreenBrightness(int displayState, boolean curReqWaitBright) {
        if (DEBUG && DEBUG_Controller) {
            Slog.i(TAG, "waitScreenBrightness displayState = " + displayState + " curReqWaitBright = " + curReqWaitBright);
        }
        if (displayState != 2 || !curReqWaitBright) {
            return false;
        }
        return true;
    }

    private int clampScreenBrightnessForVr(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessForVrRangeMinimum, this.mScreenBrightnessForVrRangeMaximum);
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void animateScreenBrightness(int target, int rate) {
        int brightnessDB;
        int brightnessTargetReal = target;
        DisplayManagerInternal.DisplayPowerRequest displayPowerRequest = this.mPowerRequest;
        if (!(displayPowerRequest == null || this.mContext == null || displayPowerRequest.useAutoBrightness || this.mLastBrightnessForAutoBrightnessDB == 0 || target != 0 || (brightnessDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", this.mScreenBrightnessDefault, this.mCurrentUserId)) == 0)) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
            if (DEBUG) {
                Slog.i(TAG, "LabcCoverMode manualMode set screen_auto_brightness db=0 when poweroff,OrigbrightnessDB=" + brightnessDB);
            }
        }
        this.mLastBrightnessForAutoBrightnessDB = target;
        DisplayManagerInternal.DisplayPowerRequest displayPowerRequest2 = this.mPowerRequest;
        if (displayPowerRequest2 != null && !displayPowerRequest2.useAutoBrightness && brightnessTargetReal > 0) {
            this.mManualBrightnessController.updateManualBrightness(brightnessTargetReal);
            brightnessTargetReal = this.mManualBrightnessController.getManualBrightness();
            if (this.mManualBrightnessController.needFastestRateForManualBrightness()) {
                rate = 0;
            }
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController != null) {
                automaticBrightnessController.setBackSensorCoverModeBrightness(brightnessTargetReal);
            }
        }
        if (this.mPowerRequest != null) {
            Slog.i(TAG, "Animating brightness: target=" + target + ", rate=" + rate + ",brightnessTargetReal=" + brightnessTargetReal + ",AutoBrightness=" + this.mPowerRequest.useAutoBrightness);
        }
        if (target >= 0 && brightnessTargetReal >= 0) {
            if (target == 0 && rate != 0) {
                rate = 0;
                Slog.e(TAG, "Animating brightness rate is invalid when screen off, set rate to 0");
            }
            if (this.mScreenBrightnessRampAnimator.animateTo(brightnessTargetReal, rate)) {
                if (this.mUsingHwSmartBackLightController && this.mSmartBackLightEnabled && rate > 0) {
                    if (this.mScreenBrightnessRampAnimator.isAnimating()) {
                        this.mHwSmartBackLightController.updateBrightnessState(0);
                    } else if (DEBUG) {
                        Slog.i(TAG, "brightness changed but not animating");
                    }
                }
                Trace.traceCounter(131072, "TargetScreenBrightness", target);
                try {
                    HwLog.dubaie("DUBAI_TAG_BRIGHTNESS", "brightness=" + brightnessTargetReal);
                    this.mBatteryStats.noteScreenBrightness(target);
                    sendTargetBrightnessToMonitor(target);
                } catch (RemoteException e) {
                }
            } else {
                DisplayManagerInternal.DisplayPowerRequest displayPowerRequest3 = this.mPowerRequest;
                if (!(displayPowerRequest3 == null || displayPowerRequest3.useAutoBrightness || this.mLastBrightnessTarget == target)) {
                    Trace.traceCounter(131072, "TargetScreenBrightness", target);
                    try {
                        this.mBatteryStats.noteScreenBrightness(target);
                    } catch (RemoteException e2) {
                    }
                }
            }
            this.mLastBrightnessTarget = target;
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        synchronized (this.mLock) {
            if (this.mScreenOffBecauseOfProximity) {
                this.mPowerState.mScreenChangedReason = 100;
            } else {
                this.mPowerState.mScreenChangedReason = this.mPendingRequestLocked.mScreenChangeReason;
            }
            Slog.i(TAG, "mPowerState.mScreenChangedReason: " + this.mPowerState.mScreenChangedReason + " mPendingRequestLocked.mScreenChangeReason " + this.mPendingRequestLocked.mScreenChangeReason);
        }
        int i = 2;
        if (this.mColorFadeEnabled && (this.mHwDisplayPowerEx.checkDawnAnimationStarted() || this.mColorFadeOnAnimator.isStarted() || this.mColorFadeOffAnimator.isStarted())) {
            if (target == 2) {
                this.mPendingScreenOff = false;
            } else {
                return;
            }
        }
        if (this.mDisplayBlanksAfterDozeConfig && Display.isDozeState(this.mPowerState.getScreenState()) && !Display.isDozeState(target)) {
            this.mPowerState.prepareColorFade(this.mContext, this.mColorFadeFadesConfig ? 2 : 0);
            ObjectAnimator objectAnimator = this.mColorFadeOffAnimator;
            if (objectAnimator != null) {
                objectAnimator.end();
            }
            setScreenState(1, target != 1);
        }
        if (this.mPendingScreenOff && target != 1) {
            setScreenState(1);
            this.mPendingScreenOff = false;
            this.mPowerState.dismissColorFadeResources();
        }
        if (target == 2) {
            if (setScreenState(2)) {
                Slog.i(TAG, "Want screen on.");
                if (!HwFoldScreenState.isInwardFoldDevice() || !this.mColorFadeEnabled || !this.mPowerRequest.isBrightOrDim()) {
                    this.mPowerState.setColorFadeLevel(1.0f);
                    this.mPowerState.dismissColorFade();
                    return;
                }
                Slog.i(TAG, "check whether is a inward fold device = " + HwFoldScreenState.isInwardFoldDevice());
                this.mHwDisplayPowerEx.inwardFoldDeviceDawnAnimation(this.mPowerManagerInternal.getLastWakeup(), this.mPowerState, 3);
            }
        } else if (target == 5) {
            if ((!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() != 2) && setScreenState(5)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target == 3) {
            if ((!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() != 2) && setScreenState(3)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target == 4) {
            if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 4) {
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
        } else if (target != 6) {
            this.mPendingScreenOff = true;
            if (!this.mColorFadeEnabled || HwActivityTaskManager.isPCMultiCastMode()) {
                this.mPowerState.setColorFadeLevel(0.0f);
            }
            this.mIsDawnAnimationPrepared = false;
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                setScreenState(1);
                this.mPendingScreenOff = false;
                this.mPowerState.dismissColorFadeResources();
                if (HwFoldScreenState.isInwardFoldDevice()) {
                    Slog.i(TAG, "clear old colorfade surface,and prepare dawn animation resource");
                    this.mPowerState.clearColorFadeSurface();
                    this.mPowerState.prepareDawnAnimation(this.mContext, 3);
                    this.mIsDawnAnimationPrepared = true;
                    return;
                }
                return;
            }
            if (performScreenOffTransition && !checkPhoneWindowIsTop()) {
                DisplayPowerState displayPowerState = this.mPowerState;
                Context context = this.mContext;
                if (!this.mColorFadeFadesConfig) {
                    i = 1;
                }
                if (displayPowerState.prepareColorFade(context, i) && this.mPowerState.getScreenState() != 1) {
                    this.mColorFadeOffAnimator.start();
                    return;
                }
            }
            this.mColorFadeOffAnimator.end();
        } else if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 6) {
            if (this.mPowerState.getScreenState() != 6) {
                if (setScreenState(2)) {
                    setScreenState(6);
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
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHandler);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProximitySensorEvent(long time, boolean positive) {
        if (!this.mProximitySensorEnabled) {
            return;
        }
        if (this.mPendingProximity == 0 && !positive) {
            return;
        }
        if (this.mPendingProximity != 1 || !positive) {
            HwServiceFactory.reportProximitySensorEventToIAware(positive);
            Slog.i(TAG, "UL_Power handleProximitySensorEvent positive:" + positive);
            this.mHandler.removeMessages(2);
            if (positive) {
                this.mPendingProximity = 1;
                setPendingProximityDebounceTime(0 + time);
            } else {
                this.mPendingProximity = 0;
                setPendingProximityDebounceTime(0 + time);
            }
            IHwDisplayPowerControllerEx iHwDisplayPowerControllerEx = this.mHwDisplayPowerEx;
            if (iHwDisplayPowerControllerEx != null) {
                iHwDisplayPowerControllerEx.sendProximityBroadcast(positive);
            }
            debounceProximitySensor();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void debounceProximitySensor() {
        if (this.mProximitySensorEnabled && this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                updatePowerState();
                clearPendingProximityDebounceTime();
                return;
            }
            this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(2), this.mPendingProximityDebounceTime);
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

    private void sendOnStateChangedWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnStateChangedRunnable);
    }

    private void logDisplayPolicyChanged(int newPolicy) {
        LogMaker log = new LogMaker(1696);
        log.setType(6);
        log.setSubtype(newPolicy);
        MetricsLogger.action(log);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSettingsChange(boolean userSwitch) {
        this.mPendingScreenBrightnessSetting = getScreenBrightnessSetting();
        if (userSwitch) {
            this.mCurrentScreenBrightnessSetting = this.mPendingScreenBrightnessSetting;
            AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
            if (automaticBrightnessController != null) {
                automaticBrightnessController.resetShortTermModel();
            }
        }
        this.mPendingAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
        this.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
        if (!this.mPowerRequest.useAutoBrightness) {
            sendUpdatePowerState();
        }
    }

    private float getAutoBrightnessAdjustmentSetting() {
        float adj = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, this.mCurrentUserId);
        if (Float.isNaN(adj)) {
            return 0.0f;
        }
        return clampAutoBrightnessAdjustment(adj);
    }

    private int getScreenBrightnessSetting() {
        return clampAbsoluteBrightness(Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mScreenBrightnessDefault, this.mCurrentUserId));
    }

    private int getScreenBrightnessForVrSetting() {
        return clampScreenBrightnessForVr(Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_for_vr", this.mScreenBrightnessForVrDefault, this.mCurrentUserId));
    }

    private void putScreenBrightnessSetting(int brightness) {
        this.mCurrentScreenBrightnessSetting = brightness;
        if (this.mCurrentScreenBrightnessSettingForDB != brightness) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", brightness, this.mCurrentUserId);
        }
        this.mCurrentScreenBrightnessSettingForDB = brightness;
    }

    private void putAutoBrightnessAdjustmentSetting(float adjustment) {
        this.mAutoBrightnessAdjustment = adjustment;
        Settings.System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", adjustment, this.mCurrentUserId);
    }

    private boolean updateAutoBrightnessAdjustment() {
        if (Float.isNaN(this.mPendingAutoBrightnessAdjustment)) {
            return false;
        }
        float f = this.mAutoBrightnessAdjustment;
        float f2 = this.mPendingAutoBrightnessAdjustment;
        if (f == f2) {
            this.mPendingAutoBrightnessAdjustment = Float.NaN;
            return false;
        }
        this.mAutoBrightnessAdjustment = f2;
        this.mPendingAutoBrightnessAdjustment = Float.NaN;
        return true;
    }

    private boolean updateUserSetScreenBrightness() {
        int i = this.mPendingScreenBrightnessSetting;
        if (i < 0) {
            return false;
        }
        if (this.mCurrentScreenBrightnessSetting == i) {
            this.mPendingScreenBrightnessSetting = -1;
            this.mTemporaryScreenBrightness = -1;
            return false;
        }
        this.mCurrentScreenBrightnessSetting = i;
        this.mLastUserSetScreenBrightness = i;
        this.mPendingScreenBrightnessSetting = -1;
        this.mTemporaryScreenBrightness = -1;
        return true;
    }

    private void notifyBrightnessChanged(int brightness, boolean userInitiated, boolean hadUserDataPoint) {
        float powerFactor;
        float brightnessInNits = convertToNits(brightness);
        if (this.mPowerRequest.useAutoBrightness && brightnessInNits >= 0.0f && this.mAutomaticBrightnessController != null) {
            if (this.mPowerRequest.lowPowerMode) {
                powerFactor = this.mPowerRequest.screenLowPowerBrightnessFactor;
            } else {
                powerFactor = 1.0f;
            }
            this.mBrightnessTracker.notifyBrightnessChanged(brightnessInNits, userInitiated, powerFactor, hadUserDataPoint, this.mAutomaticBrightnessController.isDefaultConfig());
        }
    }

    private float convertToNits(int backlight) {
        BrightnessMappingStrategy brightnessMappingStrategy = this.mBrightnessMapper;
        if (brightnessMappingStrategy != null) {
            return brightnessMappingStrategy.convertToNits(backlight);
        }
        return -1.0f;
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
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mScreenBrightnessDefault=" + this.mScreenBrightnessDefault);
        pw.println("  mScreenBrightnessForVrRangeMinimum=" + this.mScreenBrightnessForVrRangeMinimum);
        pw.println("  mScreenBrightnessForVrRangeMaximum=" + this.mScreenBrightnessForVrRangeMaximum);
        pw.println("  mScreenBrightnessForVrDefault=" + this.mScreenBrightnessForVrDefault);
        pw.println("  mUseSoftwareAutoBrightnessConfig=" + this.mUseSoftwareAutoBrightnessConfig);
        pw.println("  mAllowAutoBrightnessWhileDozingConfig=" + this.mAllowAutoBrightnessWhileDozingConfig);
        pw.println("  mBrightnessRampRateFast=" + this.mBrightnessRampRateFast);
        pw.println("  mBrightnessRampRateSlow=" + this.mBrightnessRampRateSlow);
        pw.println("  mSkipScreenOnBrightnessRamp=" + this.mSkipScreenOnBrightnessRamp);
        pw.println("  mColorFadeFadesConfig=" + this.mColorFadeFadesConfig);
        pw.println("  mColorFadeEnabled=" + this.mColorFadeEnabled);
        pw.println("  mDisplayBlanksAfterDozeConfig=" + this.mDisplayBlanksAfterDozeConfig);
        pw.println("  mBrightnessBucketsInDozeConfig=" + this.mBrightnessBucketsInDozeConfig);
        this.mHandler.runWithScissors(new Runnable() {
            /* class com.android.server.display.DisplayPowerController.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                DisplayPowerController.this.dumpLocal(pw);
            }
        }, 1000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpLocal(PrintWriter pw) {
        pw.println();
        pw.println("Display Power Controller Thread State:");
        pw.println("  mPowerRequest=" + this.mPowerRequest);
        pw.println("  mUnfinishedBusiness=" + this.mUnfinishedBusiness);
        pw.println("  mWaitingForNegativeProximity=" + this.mWaitingForNegativeProximity);
        pw.println("  mProximitySensor=" + this.mProximitySensor);
        pw.println("  mProximitySensorEnabled=" + this.mProximitySensorEnabled);
        pw.println("  mProximityThreshold=" + this.mProximityThreshold);
        pw.println("  mProximity=" + proximityToString(this.mProximity));
        pw.println("  mPendingProximity=" + proximityToString(this.mPendingProximity));
        pw.println("  mPendingProximityDebounceTime=" + TimeUtils.formatUptime(this.mPendingProximityDebounceTime));
        pw.println("  mScreenOffBecauseOfProximity=" + this.mScreenOffBecauseOfProximity);
        pw.println("  mLastUserSetScreenBrightness=" + this.mLastUserSetScreenBrightness);
        pw.println("  mCurrentScreenBrightnessSetting=" + this.mCurrentScreenBrightnessSetting);
        pw.println("  mPendingScreenBrightnessSetting=" + this.mPendingScreenBrightnessSetting);
        pw.println("  mTemporaryScreenBrightness=" + this.mTemporaryScreenBrightness);
        pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
        pw.println("  mBrightnessReason=" + this.mBrightnessReason);
        pw.println("  mTemporaryAutoBrightnessAdjustment=" + this.mTemporaryAutoBrightnessAdjustment);
        pw.println("  mPendingAutoBrightnessAdjustment=" + this.mPendingAutoBrightnessAdjustment);
        pw.println("  mScreenBrightnessForVr=" + this.mScreenBrightnessForVr);
        pw.println("  mAppliedAutoBrightness=" + this.mAppliedAutoBrightness);
        pw.println("  mAppliedDimming=" + this.mAppliedDimming);
        pw.println("  mAppliedLowPower=" + this.mAppliedLowPower);
        pw.println("  mAppliedScreenBrightnessOverride=" + this.mAppliedScreenBrightnessOverride);
        pw.println("  mAppliedTemporaryBrightness=" + this.mAppliedTemporaryBrightness);
        pw.println("  mDozing=" + this.mDozing);
        pw.println("  mSkipRampState=" + skipRampStateToString(this.mSkipRampState));
        pw.println("  mInitialAutoBrightness=" + this.mInitialAutoBrightness);
        pw.println("  mScreenOnBlockStartRealTime=" + this.mScreenOnBlockStartRealTime);
        pw.println("  mScreenOffBlockStartRealTime=" + this.mScreenOffBlockStartRealTime);
        pw.println("  mPendingScreenOnUnblocker=" + this.mPendingScreenOnUnblocker);
        pw.println("  mPendingScreenOnExUnblocker=" + this.mPendingScreenOnExUnblocker);
        pw.println("  mPendingScreenOffUnblocker=" + this.mPendingScreenOffUnblocker);
        pw.println("  mPendingScreenOff=" + this.mPendingScreenOff);
        pw.println("  mReportedToPolicy=" + reportedToPolicyToString(this.mReportedScreenStateToPolicy));
        if (this.mScreenBrightnessRampAnimator != null) {
            pw.println("  mScreenBrightnessRampAnimator.isAnimating()=" + this.mScreenBrightnessRampAnimator.isAnimating());
        }
        if (this.mColorFadeOnAnimator != null) {
            pw.println("  mColorFadeOnAnimator.isStarted()=" + this.mColorFadeOnAnimator.isStarted());
        }
        if (this.mColorFadeOffAnimator != null) {
            pw.println("  mColorFadeOffAnimator.isStarted()=" + this.mColorFadeOffAnimator.isStarted());
        }
        DisplayPowerState displayPowerState = this.mPowerState;
        if (displayPowerState != null) {
            displayPowerState.dump(pw);
        }
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.dump(pw);
        }
        if (this.mBrightnessTracker != null) {
            pw.println();
            this.mBrightnessTracker.dump(pw);
        }
        pw.println();
        DisplayWhiteBalanceController displayWhiteBalanceController = this.mDisplayWhiteBalanceController;
        if (displayWhiteBalanceController != null) {
            displayWhiteBalanceController.dump(pw);
            this.mDisplayWhiteBalanceSettings.dump(pw);
        }
    }

    private static String proximityToString(int state) {
        if (state == -1) {
            return "Unknown";
        }
        if (state == 0) {
            return "Negative";
        }
        if (state != 1) {
            return Integer.toString(state);
        }
        return "Positive";
    }

    private static String reportedToPolicyToString(int state) {
        if (state == 0) {
            return "REPORTED_TO_POLICY_SCREEN_OFF";
        }
        if (state == 1) {
            return "REPORTED_TO_POLICY_SCREEN_TURNING_ON";
        }
        if (state != 2) {
            return Integer.toString(state);
        }
        return "REPORTED_TO_POLICY_SCREEN_ON";
    }

    private static boolean wantScreenOn(int state) {
        if (state == 2 || state == 3) {
            return true;
        }
        return false;
    }

    private static int getScreenOnState(int state) {
        if (state != 1) {
            return state != 2 ? 2 : 1;
        }
        return 0;
    }

    private static String skipRampStateToString(int state) {
        if (state == 0) {
            return "RAMP_STATE_SKIP_NONE";
        }
        if (state == 1) {
            return "RAMP_STATE_SKIP_INITIAL";
        }
        if (state != 2) {
            return Integer.toString(state);
        }
        return "RAMP_STATE_SKIP_AUTOBRIGHT";
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, 255);
    }

    private static float clampAutoBrightnessAdjustment(float value) {
        return MathUtils.constrain(value, -1.0f, 1.0f);
    }

    /* access modifiers changed from: private */
    public final class DisplayControllerHandler extends Handler {
        public DisplayControllerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            switch (i) {
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
                    DisplayPowerController.this.mBrightnessConfiguration = (BrightnessConfiguration) msg.obj;
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 6:
                    DisplayPowerController.this.mTemporaryScreenBrightness = msg.arg1;
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 7:
                    DisplayPowerController.this.mTemporaryAutoBrightnessAdjustment = Float.intBitsToFloat(msg.arg1);
                    DisplayPowerController.this.updatePowerState();
                    return;
                default:
                    switch (i) {
                        case 101:
                            DisplayPowerController displayPowerController = DisplayPowerController.this;
                            boolean z = true;
                            if (msg.arg1 != 1) {
                                z = false;
                            }
                            displayPowerController.handlerTpKeepStateChanged(z);
                            return;
                        case 102:
                            DisplayPowerController.this.updateScreenState();
                            return;
                        case 103:
                            if (DisplayPowerController.this.mPendingScreenOnExUnblocker == msg.obj) {
                                DisplayPowerController.this.unblockScreenOnEx();
                                DisplayPowerController.this.updatePowerState();
                                return;
                            }
                            return;
                        case 104:
                            DisplayPowerController.this.updateDisplayMode();
                            return;
                        case 105:
                            DisplayPowerController.this.messageStartDawnAnimation();
                            return;
                        default:
                            return;
                    }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            DisplayPowerController.this.handleSettingsChange(false);
        }
    }

    /* access modifiers changed from: private */
    public final class ScreenOnUnblocker implements WindowManagerPolicy.ScreenOnListener {
        private ScreenOnUnblocker() {
        }

        @Override // com.android.server.policy.WindowManagerPolicy.ScreenOnListener
        public void onScreenOn() {
            DisplayPowerController.this.mHandler.sendMessage(DisplayPowerController.this.mHandler.obtainMessage(3, this));
        }
    }

    /* access modifiers changed from: private */
    public final class ScreenOnExUnblocker implements WindowManagerPolicy.ScreenOnExListener {
        private ScreenOnExUnblocker() {
        }

        @Override // com.android.server.policy.WindowManagerPolicy.ScreenOnExListener
        public void onScreenOnEx() {
            DisplayPowerController.this.mHandler.sendMessage(DisplayPowerController.this.mHandler.obtainMessage(103, this));
        }
    }

    private boolean checkPhoneWindowIsTop() {
        long startTime = SystemClock.elapsedRealtime();
        List<ActivityManager.RunningTaskInfo> tasksInfo = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        long getRunningTasksDuration = SystemClock.elapsedRealtime() - startTime;
        if (getRunningTasksDuration > 500) {
            Slog.i(TAG, "Check Phone Window is top, get the Running Tasks duration: " + getRunningTasksDuration);
        }
        if (tasksInfo != null && tasksInfo.size() > 0) {
            ComponentName cn = tasksInfo.get(0).topActivity;
            if ("com.android.incallui".equals(cn.getPackageName()) && "com.android.incallui.InCallActivity".equals(cn.getClassName())) {
                if (DEBUG) {
                    Slog.i(TAG, "checkPhoneWindowIsTop: incallui window is top");
                }
                return true;
            }
        }
        return false;
    }

    private void writeAutoBrightnessDbEnable(boolean autoEnable) {
        if (this.mPowerRequest.policy == 2 || !autoEnable) {
            this.mBackLight.writeAutoBrightnessDbEnable(false);
        } else if (!this.mPowerPolicyChangeFromDimming) {
            this.mBackLight.writeAutoBrightnessDbEnable(true);
        }
    }

    @Override // com.android.server.display.IHwDisplayPowerControllerEx.Callbacks
    public void onTpKeepStateChanged(boolean tpKeeped) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(101, tpKeeped ? 1 : 0, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerTpKeepStateChanged(boolean tpKeeped) {
        if (DEBUG) {
            Slog.i(TAG, "TpKeepChanged tpKeeped:" + tpKeeped + ", sensorPositive:" + this.mProximitySensorPositive);
        }
        handleProximitySensorEvent(SystemClock.uptimeMillis(), this.mProximitySensorPositive || tpKeeped);
    }

    private void updatemAnimationState(boolean autoBrightnessEnabled) {
        DisplayManagerInternal.DisplayPowerRequest displayPowerRequest;
        if (this.mAutomaticBrightnessController != null && this.mScreenBrightnessRampAnimator != null && (displayPowerRequest = this.mPowerRequest) != null) {
            if (displayPowerRequest.screenAutoBrightness > 0) {
                this.mAutomaticBrightnessController.updateIntervenedAutoBrightness(this.mPowerRequest.screenAutoBrightness);
            }
            boolean z = false;
            if (this.mBrightnessModeChanged) {
                this.mBrightnessModeChanged = false;
            }
            RampAnimator<DisplayPowerState> rampAnimator = this.mScreenBrightnessRampAnimator;
            int updateAutoBrightnessCount = this.mAutomaticBrightnessController.getUpdateAutoBrightnessCount();
            if (this.mPowerRequest.screenAutoBrightness > 0) {
                z = true;
            }
            rampAnimator.updateBrightnessRampPara(autoBrightnessEnabled, updateAutoBrightnessCount, z, this.mPowerRequest.policy);
            this.mfastAnimtionFlag = this.mAutomaticBrightnessController.getPowerStatus();
            this.mScreenBrightnessRampAnimator.updateFastAnimationFlag(this.mfastAnimtionFlag);
            this.mCoverModeAnimationFast = this.mAutomaticBrightnessController.getCoverModeFastResponseFlag();
            this.mScreenBrightnessRampAnimator.updateCoverModeFastAnimationFlag(this.mCoverModeAnimationFast);
            this.mScreenBrightnessRampAnimator.updateCameraModeChangeAnimationEnable(this.mAutomaticBrightnessController.getCameraModeChangeAnimationEnable());
            this.mScreenBrightnessRampAnimator.updateGameModeChangeAnimationEnable(this.mAutomaticBrightnessController.getAnimationGameChangeEnable());
            if (this.mAutomaticBrightnessController.getReadingModeBrightnessLineEnable()) {
                this.mScreenBrightnessRampAnimator.updateReadingModeChangeAnimationEnable(this.mAutomaticBrightnessController.getReadingModeChangeAnimationEnable());
            }
            this.mScreenBrightnessRampAnimator.setBrightnessAnimationTime(this.mAnimationEnabled, this.mMillisecond);
            this.mOutdoorAnimationFlag = this.mAutomaticBrightnessController.getOutdoorAnimationFlag();
            this.mScreenBrightnessRampAnimator.updateOutdoorAnimationFlag(this.mOutdoorAnimationFlag);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisplayMode() {
        Slog.i(TAG, "updateDisplayMode");
        if (HwFoldScreenState.isInwardFoldDevice() && this.mHwDisplayPowerEx.checkDawnAnimationOnIsStarted()) {
            this.mHwDisplayPowerEx.updateColorFadeDawnAnimationAnimator("dawnAnimationOn", false);
        }
    }

    private void sendUpdatetDisplayMode() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(104));
    }

    public void updateDisplayMode(int mode) {
        Slog.i(TAG, "updateDisplayMode:" + mode);
        if (mode == 2) {
            sendUpdatetDisplayMode();
        }
    }

    @Override // com.android.server.display.AutomaticBrightnessController.Callbacks
    public void updateProximityState(boolean proximityState) {
        if (DEBUG) {
            Slog.i(TAG, "updateProximityState:" + proximityState);
        }
        this.mProximityPositive = proximityState;
        this.mScreenBrightnessRampAnimator.updateProximityState(proximityState);
    }

    public boolean getManualModeEnable() {
        ManualBrightnessController manualBrightnessController = this.mManualBrightnessController;
        if (manualBrightnessController != null) {
            return manualBrightnessController.getManualModeEnable();
        }
        Slog.e(TAG, "mManualBrightnessController=null");
        return false;
    }

    public void updatemManualModeAnimationEnable() {
        RampAnimator<DisplayPowerState> rampAnimator;
        if (getManualModeEnable() && (rampAnimator = this.mScreenBrightnessRampAnimator) != null) {
            rampAnimator.updatemManualModeAnimationEnable(this.mManualBrightnessController.getManualModeAnimationEnable());
        }
    }

    public void updateManualPowerSavingAnimationEnable() {
        boolean frontCameraDimmingEnable;
        if (this.mManualBrightnessController != null && this.mScreenBrightnessRampAnimator != null && this.mAutomaticBrightnessController != null) {
            int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
            boolean autoPowerSavingUseManualAnimationTimeEnable = this.mAutomaticBrightnessController.getAutoPowerSavingUseManualAnimationTimeEnable();
            if (brightnessMode != 1 || !autoPowerSavingUseManualAnimationTimeEnable) {
                boolean manualPowerSavingAnimationEnable = this.mManualBrightnessController.getManualPowerSavingAnimationEnable();
                this.mScreenBrightnessRampAnimator.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable);
                if (manualPowerSavingAnimationEnable) {
                    this.mManualBrightnessController.setManualPowerSavingAnimationEnable(false);
                }
            } else {
                boolean manualPowerSavingAnimationEnable2 = this.mAutomaticBrightnessController.getAutoPowerSavingAnimationEnable();
                this.mScreenBrightnessRampAnimator.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable2);
                if (manualPowerSavingAnimationEnable2) {
                    this.mAutomaticBrightnessController.setAutoPowerSavingAnimationEnable(false);
                }
            }
            if (brightnessMode == 1) {
                frontCameraDimmingEnable = this.mAutomaticBrightnessController.getFrontCameraDimmingEnable();
            } else {
                frontCameraDimmingEnable = this.mManualBrightnessController.getFrontCameraDimmingEnable();
            }
            this.mScreenBrightnessRampAnimator.updateFrontCameraDimmingEnable(frontCameraDimmingEnable);
        }
    }

    public void updateManualThermalModeAnimationEnable() {
        ManualBrightnessController manualBrightnessController = this.mManualBrightnessController;
        if (manualBrightnessController != null && this.mScreenBrightnessRampAnimator != null && manualBrightnessController.getManualThermalModeEnable()) {
            boolean manualThermalModeAnimationEnable = this.mManualBrightnessController.getManualThermalModeAnimationEnable();
            this.mScreenBrightnessRampAnimator.updateManualThermalModeAnimationEnable(manualThermalModeAnimationEnable);
            if (manualThermalModeAnimationEnable) {
                this.mManualBrightnessController.setManualThermalModeAnimationEnable(false);
            }
        }
    }

    public void updateBrightnessModeAnimationEnable() {
        ManualBrightnessController manualBrightnessController = this.mManualBrightnessController;
        if (manualBrightnessController != null && this.mScreenBrightnessRampAnimator != null && manualBrightnessController.getBrightnessSetByAppEnable()) {
            this.mScreenBrightnessRampAnimator.updateBrightnessModeAnimationEnable(this.mManualBrightnessController.getBrightnessSetByAppAnimationEnable(), this.mSetBrightnessNoLimitAnimationTime);
        }
    }

    private void updateDarkAdaptDimmingEnable() {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null && this.mScreenBrightnessRampAnimator != null) {
            boolean darkAdaptDimmingEnable = automaticBrightnessController.getDarkAdaptDimmingEnable();
            this.mScreenBrightnessRampAnimator.updateDarkAdaptAnimationDimmingEnable(darkAdaptDimmingEnable);
            if (darkAdaptDimmingEnable) {
                this.mAutomaticBrightnessController.clearDarkAdaptDimmingEnable();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ScreenOffUnblocker implements WindowManagerPolicy.ScreenOffListener {
        private ScreenOffUnblocker() {
        }

        @Override // com.android.server.policy.WindowManagerPolicy.ScreenOffListener
        public void onScreenOff() {
            DisplayPowerController.this.mHandler.sendMessage(DisplayPowerController.this.mHandler.obtainMessage(4, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void setAutoBrightnessLoggingEnabled(boolean enabled) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.setLoggingEnabled(enabled);
        }
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController == null) {
            return 33;
        }
        return automaticBrightnessController.getCoverModeBrightnessFromLastScreenBrightness();
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.setMaxBrightnessFromThermal(brightness);
        }
        this.mManualBrightnessController.setMaxBrightnessFromThermal(brightness);
    }

    public void setModeToAutoNoClearOffsetEnable(boolean enable) {
        this.mModeToAutoNoClearOffsetEnable = enable;
        if (DEBUG) {
            Slog.i(TAG, "set mModeToAutoNoClearOffsetEnable=" + this.mModeToAutoNoClearOffsetEnable);
        }
    }

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController == null) {
            return brightness;
        }
        return automaticBrightnessController.setScreenBrightnessMappingToIndoorMax(brightness);
    }

    private void sendTargetBrightnessToMonitor(int brightness) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put("paramType", "algoDiscountBrightness");
            params.put("brightness", Integer.valueOf(brightness));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    public void setPoweroffModeChangeAutoEnable(boolean enable) {
        this.mPoweroffModeChangeAutoEnable = enable;
        this.mBrightnessModeChangeNoClearOffsetEnable = this.mPoweroffModeChangeAutoEnable;
        if (DEBUG) {
            Slog.i(TAG, "set mPoweroffModeChangeAutoEnable=" + this.mPoweroffModeChangeAutoEnable + ",mNoClearOffsetEnable=" + this.mBrightnessModeChangeNoClearOffsetEnable);
        }
    }

    public void setBrightnessNoLimit(int brightness, int time) {
        this.mSetBrightnessNoLimitAnimationTime = time;
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.setBrightnessNoLimit(brightness, time);
        }
        this.mManualBrightnessController.setBrightnessNoLimit(brightness, time);
    }

    public void updateBrightnessModeChangeManualState(boolean enable) {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null) {
            automaticBrightnessController.updateBrightnessModeChangeManualState(enable);
        }
    }

    public void updateFastDarkenDimmingEnable() {
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null && this.mScreenBrightnessRampAnimator != null) {
            this.mScreenBrightnessRampAnimator.updateFastDarkenDimmingEnable(automaticBrightnessController.getFastDarkenDimmingEnable());
            this.mScreenBrightnessRampAnimator.updateKeyguardUnlockedFastDarkenDimmingEnable(this.mAutomaticBrightnessController.getKeyguardUnlockedFastDarkenDimmingEnable());
        }
    }

    private void updateNightUpPowerOnWithDimmingEnable() {
        RampAnimator<DisplayPowerState> rampAnimator;
        AutomaticBrightnessController automaticBrightnessController = this.mAutomaticBrightnessController;
        if (automaticBrightnessController != null && (rampAnimator = this.mScreenBrightnessRampAnimator) != null) {
            rampAnimator.updateNightUpPowerOnWithDimmingEnable(automaticBrightnessController.getNightUpPowerOnWithDimmingEnable());
        }
    }

    @Override // com.android.server.display.whitebalance.DisplayWhiteBalanceController.Callbacks
    public void updateWhiteBalance() {
        sendUpdatePowerState();
    }

    /* access modifiers changed from: package-private */
    public void setDisplayWhiteBalanceLoggingEnabled(boolean enabled) {
        DisplayWhiteBalanceController displayWhiteBalanceController = this.mDisplayWhiteBalanceController;
        if (displayWhiteBalanceController != null) {
            displayWhiteBalanceController.setLoggingEnabled(enabled);
            this.mDisplayWhiteBalanceSettings.setLoggingEnabled(enabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAmbientColorTemperatureOverride(float cct) {
        DisplayWhiteBalanceController displayWhiteBalanceController = this.mDisplayWhiteBalanceController;
        if (displayWhiteBalanceController != null) {
            displayWhiteBalanceController.setAmbientColorTemperatureOverride(cct);
            sendUpdatePowerState();
        }
    }

    /* access modifiers changed from: private */
    public final class BrightnessReason {
        static final int ADJUSTMENT_AUTO = 2;
        static final int ADJUSTMENT_AUTO_TEMP = 1;
        static final int MODIFIER_DIMMED = 1;
        static final int MODIFIER_LOW_POWER = 2;
        static final int MODIFIER_MASK = 3;
        static final int REASON_AUTOMATIC = 4;
        static final int REASON_BOOST = 9;
        static final int REASON_DOZE = 2;
        static final int REASON_DOZE_DEFAULT = 3;
        static final int REASON_MANUAL = 1;
        static final int REASON_MAX = 9;
        static final int REASON_OVERRIDE = 7;
        static final int REASON_SCREEN_OFF = 5;
        static final int REASON_TEMPORARY = 8;
        static final int REASON_UNKNOWN = 0;
        static final int REASON_VR = 6;
        public int modifier;
        public int reason;

        private BrightnessReason() {
        }

        public void set(BrightnessReason other) {
            int i = 0;
            setReason(other == null ? 0 : other.reason);
            if (other != null) {
                i = other.modifier;
            }
            setModifier(i);
        }

        public void setReason(int reason2) {
            if (reason2 < 0 || reason2 > 9) {
                Slog.w(DisplayPowerController.TAG, "brightness reason out of bounds: " + reason2);
                return;
            }
            this.reason = reason2;
        }

        public void setModifier(int modifier2) {
            if ((modifier2 & -4) != 0) {
                Slog.w(DisplayPowerController.TAG, "brightness modifier out of bounds: 0x" + Integer.toHexString(modifier2));
                return;
            }
            this.modifier = modifier2;
        }

        public void addModifier(int modifier2) {
            setModifier(this.modifier | modifier2);
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof BrightnessReason)) {
                return false;
            }
            BrightnessReason other = (BrightnessReason) obj;
            if (other.reason == this.reason && other.modifier == this.modifier) {
                return true;
            }
            return false;
        }

        public String toString() {
            return toString(0);
        }

        public String toString(int adjustments) {
            StringBuilder sb = new StringBuilder();
            sb.append(reasonToString(this.reason));
            sb.append(" [");
            if ((adjustments & 1) != 0) {
                sb.append(" temp_adj");
            }
            if ((adjustments & 2) != 0) {
                sb.append(" auto_adj");
            }
            if ((this.modifier & 2) != 0) {
                sb.append(" low_pwr");
            }
            if ((this.modifier & 1) != 0) {
                sb.append(" dim");
            }
            int strlen = sb.length();
            if (sb.charAt(strlen - 1) == '[') {
                sb.setLength(strlen - 2);
            } else {
                sb.append(" ]");
            }
            return sb.toString();
        }

        private String reasonToString(int reason2) {
            switch (reason2) {
                case 1:
                    return "manual";
                case 2:
                    return "doze";
                case 3:
                    return "doze_default";
                case 4:
                    return "automatic";
                case 5:
                    return "screen_off";
                case 6:
                    return "vr";
                case 7:
                    return "override";
                case 8:
                    return "temporary";
                case 9:
                    return "boost";
                default:
                    return Integer.toString(reason2);
            }
        }
    }

    public boolean setHwBrightnessData(String name, Bundle data, int[] result) {
        IHwDisplayPowerControllerEx iHwDisplayPowerControllerEx = this.mHwDisplayPowerEx;
        if (iHwDisplayPowerControllerEx != null) {
            return iHwDisplayPowerControllerEx.setHwBrightnessData(name, data, result);
        }
        return false;
    }

    public boolean getHwBrightnessData(String name, Bundle data, int[] result) {
        IHwDisplayPowerControllerEx iHwDisplayPowerControllerEx = this.mHwDisplayPowerEx;
        if (iHwDisplayPowerControllerEx != null) {
            return iHwDisplayPowerControllerEx.getHwBrightnessData(name, data, result);
        }
        return false;
    }

    @Override // com.android.server.display.IHwDisplayPowerControllerEx.Callbacks
    public AutomaticBrightnessController getAutomaticBrightnessController() {
        return this.mAutomaticBrightnessController;
    }

    @Override // com.android.server.display.IHwDisplayPowerControllerEx.Callbacks
    public ManualBrightnessController getManualBrightnessController() {
        return this.mManualBrightnessController;
    }

    public void setBiometricDetectState(int state) {
        DisplayPowerState displayPowerState = this.mPowerState;
        if (displayPowerState != null) {
            displayPowerState.setBiometricDetectState(state);
        }
    }

    public void startDawnAnimation() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(105));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void messageStartDawnAnimation() {
        PowerManager.WakeData mLastWakeUp = this.mPowerManagerInternal.getLastWakeup();
        if (this.mHwDisplayPowerEx != null) {
            if (!this.mIsDawnAnimationPrepared) {
                Slog.i(TAG, "StartDawnAnimation: clear old colorfade surface, and prepare dawn animation resource");
                this.mPowerState.dismissColorFadeResources();
                this.mPowerState.clearColorFadeSurface();
                this.mPowerState.prepareDawnAnimation(this.mContext, 3);
                this.mIsDawnAnimationPrepared = true;
            }
            this.mHwDisplayPowerEx.startInwardFoldDeviceDawnAnimation(mLastWakeUp, this.mPowerState, 3);
        }
    }

    public boolean registerScreenOnUnBlockerCallback(HwFoldScreenManagerInternal.ScreenOnUnblockerCallback callback) {
        if (this.mPendingScreenOnUnblocker == null) {
            Slog.i(TAG, "mPendingScreenOnUnblocker is null");
            return false;
        }
        if (this.mScreenOnUnblockerCallback == null) {
            Slog.i(TAG, "mScreenOnUnblockerCallback is null");
            this.mScreenOnUnblockerCallback = callback;
        }
        Slog.i(TAG, "registerScreenOnUnBlockerCallback success");
        return true;
    }
}
