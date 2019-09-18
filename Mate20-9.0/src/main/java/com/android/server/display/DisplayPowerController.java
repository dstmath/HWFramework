package com.android.server.display;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.HwLog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import android.view.Display;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.app.IBatteryStats;
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
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.util.List;

final class DisplayPowerController implements AutomaticBrightnessController.Callbacks, ManualBrightnessController.ManualBrightnessCallbacks, IHwDisplayPowerControllerEx.Callbacks {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_RAMP_RATE_FAST = 200;
    private static final int BRIGHTNESS_RAMP_RATE_SLOW = 40;
    private static final int COLOR_FADE_OFF_ANIMATION_DURATION_MILLIS = 150;
    private static final int COLOR_FADE_ON_ANIMATION_DURATION_MILLIS = 250;
    /* access modifiers changed from: private */
    public static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean DEBUG_Controller = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static boolean DEBUG_FPLOG = false;
    private static final boolean DEBUG_PRETEND_PROXIMITY_SENSOR_ABSENT = false;
    private static final int GET_RUNNING_TASKS_FROM_AMS_WARNING_DURATION_MILLIS = 500;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 1000;
    private static final int MSG_CONFIGURE_BRIGHTNESS = 5;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_OFF_UNBLOCKED = 4;
    private static final int MSG_SCREEN_ON_FOR_KEYGUARD_DISMISS_DONE = 8;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_SET_TEMPORARY_AUTO_BRIGHTNESS_ADJUSTMENT = 7;
    private static final int MSG_SET_TEMPORARY_BRIGHTNESS = 6;
    private static final int MSG_SET_TP_KEEP = 9;
    private static final int MSG_UPDATE_POWER_STATE = 1;
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
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    private FingerprintDataInterface fpDataCollector;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private boolean mAnimationEnabled;
    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
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
    private boolean mAppliedBrightnessBoost;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private boolean mAppliedScreenBrightnessOverride;
    private boolean mAppliedTemporaryAutoBrightnessAdjustment;
    private boolean mAppliedTemporaryBrightness;
    private float mAutoBrightnessAdjustment;
    private boolean mAutoBrightnessAdjustmentChanged = false;
    private boolean mAutoBrightnessEnabled = false;
    private Light mAutoCustomBackLight;
    private AutomaticBrightnessController mAutomaticBrightnessController;
    /* access modifiers changed from: private */
    public Light mBackLight;
    /* access modifiers changed from: private */
    public final IBatteryStats mBatteryStats;
    private final DisplayBlanker mBlanker;
    private boolean mBrightnessBucketsInDozeConfig;
    /* access modifiers changed from: private */
    public BrightnessConfiguration mBrightnessConfiguration;
    private BrightnessMappingStrategy mBrightnessMapper;
    private boolean mBrightnessModeChangeNoClearOffsetEnable = false;
    private boolean mBrightnessModeChanged = false;
    private final int mBrightnessRampRateFast;
    private final int mBrightnessRampRateSlow;
    private final BrightnessTracker mBrightnessTracker;
    /* access modifiers changed from: private */
    public final DisplayManagerInternal.DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener = new Runnable() {
        public void run() {
            DisplayPowerController.this.sendUpdatePowerState();
        }
    };
    private final boolean mColorFadeEnabled;
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mCoverModeAnimationFast = false;
    private int mCurrentScreenBrightnessSetting;
    private int mCurrentScreenBrightnessSettingForDB;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    private boolean mCurrentUserIdChange = false;
    private boolean mDisplayBlanksAfterDozeConfig;
    private HwServiceFactory.IDisplayEffectMonitor mDisplayEffectMonitor;
    private HwServiceFactory.IDisplayEngineInterface mDisplayEngineInterface = null;
    private boolean mDisplayReadyLocked;
    private boolean mDozing;
    private int mFeedBack = 0;
    private int mGlobalAlpmState = -1;
    /* access modifiers changed from: private */
    public final DisplayControllerHandler mHandler;
    /* access modifiers changed from: private */
    public IHwDisplayPowerControllerEx mHwDisplayPowerEx = null;
    /* access modifiers changed from: private */
    public HwServiceFactory.IHwSmartBackLightController mHwSmartBackLightController;
    /* access modifiers changed from: private */
    public boolean mImmeBright;
    private int mInitialAutoBrightness;
    private boolean mIsCoverModeClosed = true;
    private int mIsScreenOn = 0;
    private boolean mKeyguardIsLocked = false;
    /* access modifiers changed from: private */
    public boolean mLABCEnabled;
    private Sensor mLABCSensor;
    /* access modifiers changed from: private */
    public boolean mLABCSensorEnabled;
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
                    int unused = DisplayPowerController.this.mPendingBacklight = Backlight;
                    DisplayPowerController.this.sendUpdatePowerState();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mLastBacklight = 102;
    private int mLastBrightnessForAutoBrightnessDB = 0;
    private int mLastBrightnessTarget;
    private boolean mLastStatus = false;
    private int mLastUserSetScreenBrightness;
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
    private float mPendingAutoBrightnessAdjustment;
    /* access modifiers changed from: private */
    public int mPendingBacklight = -1;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPendingRequestChangedLocked;
    private DisplayManagerInternal.DisplayPowerRequest mPendingRequestLocked;
    private int mPendingScreenBrightnessSetting;
    private boolean mPendingScreenOff;
    /* access modifiers changed from: private */
    public ScreenOffUnblocker mPendingScreenOffUnblocker;
    /* access modifiers changed from: private */
    public ScreenOnForKeyguardDismissUnblocker mPendingScreenOnForKeyguardDismissUnblocker;
    /* access modifiers changed from: private */
    public ScreenOnUnblocker mPendingScreenOnUnblocker;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    /* access modifiers changed from: private */
    public boolean mPowerPolicyChangeFromDimming;
    /* access modifiers changed from: private */
    public DisplayManagerInternal.DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private boolean mPoweroffModeChangeAutoEnable = false;
    private int mProximity = -1;
    /* access modifiers changed from: private */
    public boolean mProximityPositive = false;
    private Sensor mProximitySensor;
    /* access modifiers changed from: private */
    public boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DisplayPowerController.this.mProximitySensorEnabled) {
                long time = SystemClock.uptimeMillis();
                boolean z = false;
                float distance = event.values[0];
                boolean positive = distance >= 0.0f && distance < DisplayPowerController.this.mProximityThreshold;
                if (DisplayPowerController.this.mHwDisplayPowerEx != null) {
                    DisplayPowerController.this.mHwDisplayPowerEx.setProxPositive(positive);
                    DisplayPowerController displayPowerController = DisplayPowerController.this;
                    if (positive || DisplayPowerController.this.mHwDisplayPowerEx.getTpKeep()) {
                        z = true;
                    }
                    displayPowerController.handleProximitySensorEvent(time, z);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public float mProximityThreshold;
    private final RampAnimator.Listener mRampAnimatorListener = new RampAnimator.Listener() {
        public void onAnimationEnd() {
            if (DisplayPowerController.this.mUsingHwSmartBackLightController && DisplayPowerController.this.mSmartBackLightEnabled) {
                HwServiceFactory.IHwSmartBackLightController access$300 = DisplayPowerController.this.mHwSmartBackLightController;
                HwServiceFactory.IHwSmartBackLightController unused = DisplayPowerController.this.mHwSmartBackLightController;
                access$300.updateBrightnessState(1);
            }
            if (DisplayPowerController.this.mPowerPolicyChangeFromDimming) {
                boolean unused2 = DisplayPowerController.this.mPowerPolicyChangeFromDimming = false;
                if (DisplayPowerController.DEBUG && DisplayPowerController.this.mPowerRequest != null) {
                    Slog.d(DisplayPowerController.TAG, "update mPowerPolicyChangeFromDimming mPowerRequest.policy=" + DisplayPowerController.this.mPowerRequest.policy);
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
    /* access modifiers changed from: private */
    public RampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOffBlockStartRealTime;
    private boolean mScreenOnBecauseOfPhoneProximity;
    private long mScreenOnBlockStartRealTime;
    /* access modifiers changed from: private */
    public long mScreenOnForKeyguardDismissBlockStartRealTime;
    private final SensorManager mSensorManager;
    private int mSetAutoBackLight = -1;
    private int mSetBrightnessNoLimitAnimationTime = 500;
    private final SettingsObserver mSettingsObserver;
    private int mSkipRampState = 0;
    private final boolean mSkipScreenOnBrightnessRamp;
    /* access modifiers changed from: private */
    public boolean mSmartBackLightEnabled;
    private boolean mSmartBackLightSupported;
    /* access modifiers changed from: private */
    public float mTemporaryAutoBrightnessAdjustment;
    /* access modifiers changed from: private */
    public int mTemporaryScreenBrightness;
    private boolean mUnfinishedBusiness;
    private boolean mUseSensorHubLABC = false;
    private boolean mUseSoftwareAutoBrightnessConfig;
    /* access modifiers changed from: private */
    public boolean mUsingHwSmartBackLightController = false;
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
                    BrightnessConfiguration unused = DisplayPowerController.this.mBrightnessConfiguration = (BrightnessConfiguration) msg.obj;
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 6:
                    int unused2 = DisplayPowerController.this.mTemporaryScreenBrightness = msg.arg1;
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 7:
                    float unused3 = DisplayPowerController.this.mTemporaryAutoBrightnessAdjustment = Float.intBitsToFloat(msg.arg1);
                    DisplayPowerController.this.updatePowerState();
                    return;
                case 8:
                    if (DisplayPowerController.this.mPendingScreenOnForKeyguardDismissUnblocker == msg.obj) {
                        boolean unused4 = DisplayPowerController.this.mImmeBright = true;
                        DisplayPowerController.this.unblockScreenOnForKeyguardDismiss();
                        DisplayPowerController.this.updatePowerState();
                        return;
                    }
                    return;
                case 9:
                    if (DisplayPowerController.this.mHwDisplayPowerEx != null) {
                        DisplayPowerController.this.mHwDisplayPowerEx.handleTpKeep();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private final class ScreenOffUnblocker implements WindowManagerPolicy.ScreenOffListener {
        private ScreenOffUnblocker() {
        }

        public void onScreenOff() {
            DisplayPowerController.this.mHandler.sendMessage(DisplayPowerController.this.mHandler.obtainMessage(4, this));
        }
    }

    private final class ScreenOnForKeyguardDismissUnblocker implements WindowManagerPolicy.KeyguardDismissDoneListener {
        private ScreenOnForKeyguardDismissUnblocker() {
        }

        public void onKeyguardDismissDone() {
            long delay = SystemClock.elapsedRealtime() - DisplayPowerController.this.mScreenOnForKeyguardDismissBlockStartRealTime;
            if (delay > 1000) {
                Slog.i(DisplayPowerController.TAG, "fingerunlock--onKeyguardDismissDone delay " + delay);
            }
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(8, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    private final class ScreenOnUnblocker implements WindowManagerPolicy.ScreenOnListener {
        private ScreenOnUnblocker() {
        }

        public void onScreenOn() {
            DisplayPowerController.this.mHandler.sendMessage(DisplayPowerController.this.mHandler.obtainMessage(3, this));
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            DisplayPowerController.this.handleSettingsChange(false);
        }
    }

    static {
        boolean z = true;
        if (!DEBUG) {
            z = false;
        }
        DEBUG_FPLOG = z;
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
            this.mDisplayEngineInterface.setScene("SCENE_AOD", "ACTION_MODE_OFF");
        } else if (this.mGlobalAlpmState == 0) {
            this.mDisplayEngineInterface.setScene("SCENE_AOD", "ACTION_MODE_ON");
        }
    }

    public void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness) {
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

    public DisplayPowerController(Context context, DisplayManagerInternal.DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
        Resources resources;
        DisplayPowerController displayPowerController;
        int initialLightSensorRate;
        Context context2 = context;
        this.mHandler = new DisplayControllerHandler(handler.getLooper());
        this.mBrightnessTracker = new BrightnessTracker(context2, null);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mCallbacks = callbacks;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mLights = (LightsManager) LocalServices.getService(LightsManager.class);
        SensorManager sensorManager2 = sensorManager;
        this.mSensorManager = sensorManager2;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mBlanker = blanker;
        this.mContext = context2;
        this.mBackLight = this.mLights.getLight(0);
        this.mHwDisplayPowerEx = HwServiceExFactory.getHwDisplayPowerControllerEx(this.mContext, this, this.mSensorManager);
        Resources resources2 = context.getResources();
        int screenBrightnessSettingMinimum = clampAbsoluteBrightness(resources2.getInteger(17694862));
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources2.getInteger(17694856));
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources2.getInteger(17694855));
        this.mScreenBrightnessRangeMinimum = Math.min(screenBrightnessSettingMinimum, this.mScreenBrightnessDimConfig);
        this.mScreenBrightnessRangeMaximum = clampAbsoluteBrightness(resources2.getInteger(17694861));
        this.mScreenBrightnessDefault = clampAbsoluteBrightness(resources2.getInteger(17694860));
        this.mScreenBrightnessForVrRangeMinimum = clampAbsoluteBrightness(resources2.getInteger(17694859));
        this.mScreenBrightnessForVrRangeMaximum = clampAbsoluteBrightness(resources2.getInteger(17694858));
        this.mScreenBrightnessForVrDefault = clampAbsoluteBrightness(resources2.getInteger(17694857));
        this.mUseSoftwareAutoBrightnessConfig = resources2.getBoolean(17956895);
        this.mAllowAutoBrightnessWhileDozingConfig = resources2.getBoolean(17956872);
        this.mBrightnessRampRateFast = resources2.getInteger(17694745);
        this.mBrightnessRampRateSlow = resources2.getInteger(17694746);
        this.mSkipScreenOnBrightnessRamp = resources2.getBoolean(17957024);
        if (this.mUseSoftwareAutoBrightnessConfig) {
            float dozeScaleFactor = resources2.getFraction(18022403, 1, 1);
            int[] brightLevels = resources2.getIntArray(17236004);
            int[] darkLevels = resources2.getIntArray(17236005);
            int[] luxHysteresisLevels = resources2.getIntArray(17236006);
            HysteresisLevels hysteresisLevels = new HysteresisLevels(brightLevels, darkLevels, luxHysteresisLevels);
            long brighteningLightDebounce = (long) resources2.getInteger(17694732);
            long darkeningLightDebounce = (long) resources2.getInteger(17694733);
            boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources2.getBoolean(17956891);
            int lightSensorWarmUpTimeConfig = resources2.getInteger(17694798);
            int lightSensorRate = resources2.getInteger(17694735);
            int initialLightSensorRate2 = resources2.getInteger(17694734);
            long darkeningLightDebounce2 = darkeningLightDebounce;
            if (initialLightSensorRate2 == -1) {
                initialLightSensorRate = lightSensorRate;
            } else {
                if (initialLightSensorRate2 > lightSensorRate) {
                    Slog.w(TAG, "Expected config_autoBrightnessInitialLightSensorRate (" + initialLightSensorRate2 + ") to be less than or equal to config_autoBrightnessLightSensorRate (" + lightSensorRate + ").");
                }
                initialLightSensorRate = initialLightSensorRate2;
            }
            this.mBrightnessMapper = BrightnessMappingStrategy.create(resources2);
            if (this.mBrightnessMapper != null) {
                HwServiceFactory.IHwAutomaticBrightnessController iadm = HwServiceFactory.getHuaweiAutomaticBrightnessController();
                if (iadm != null) {
                    int[] iArr = luxHysteresisLevels;
                    int[] iArr2 = darkLevels;
                    int[] iArr3 = brightLevels;
                    int i = screenBrightnessSettingMinimum;
                    resources = resources2;
                    displayPowerController = this;
                    displayPowerController.mAutomaticBrightnessController = iadm.getInstance(this, handler.getLooper(), sensorManager2, this.mBrightnessMapper, lightSensorWarmUpTimeConfig, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounce, darkeningLightDebounce2, autoBrightnessResetAmbientLuxAfterWarmUp, hysteresisLevels, this.mContext);
                } else {
                    int lightSensorRate2 = lightSensorRate;
                    long brighteningLightDebounce2 = brighteningLightDebounce;
                    int[] iArr4 = luxHysteresisLevels;
                    int[] iArr5 = darkLevels;
                    int[] iArr6 = brightLevels;
                    int i2 = screenBrightnessSettingMinimum;
                    resources = resources2;
                    displayPowerController = this;
                    AutomaticBrightnessController automaticBrightnessController = new AutomaticBrightnessController(displayPowerController, handler.getLooper(), sensorManager, displayPowerController.mBrightnessMapper, lightSensorWarmUpTimeConfig, displayPowerController.mScreenBrightnessRangeMinimum, displayPowerController.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate2, initialLightSensorRate, brighteningLightDebounce2, darkeningLightDebounce2, autoBrightnessResetAmbientLuxAfterWarmUp, hysteresisLevels);
                    displayPowerController.mAutomaticBrightnessController = automaticBrightnessController;
                }
            } else {
                long j = brighteningLightDebounce;
                int[] iArr7 = luxHysteresisLevels;
                int[] iArr8 = darkLevels;
                int[] iArr9 = brightLevels;
                int i3 = screenBrightnessSettingMinimum;
                resources = resources2;
                displayPowerController = this;
                displayPowerController.mUseSoftwareAutoBrightnessConfig = false;
            }
            displayPowerController.fpDataCollector = FingerprintDataInterface.getInstance();
        } else {
            resources = resources2;
            displayPowerController = this;
        }
        displayPowerController.mColorFadeEnabled = !ActivityManager.isLowRamDeviceStatic();
        Resources resources3 = resources;
        displayPowerController.mColorFadeFadesConfig = resources3.getBoolean(17956888);
        displayPowerController.mDisplayBlanksAfterDozeConfig = resources3.getBoolean(17956932);
        displayPowerController.mBrightnessBucketsInDozeConfig = resources3.getBoolean(17956933);
        displayPowerController.mProximitySensor = displayPowerController.mSensorManager.getDefaultSensor(8);
        if (displayPowerController.mProximitySensor != null) {
            displayPowerController.mProximityThreshold = Math.min(displayPowerController.mProximitySensor.getMaximumRange(), 5.0f);
        }
        displayPowerController.mDisplayEngineInterface = HwServiceFactory.getDisplayEngineInterface();
        if (displayPowerController.mDisplayEngineInterface != null) {
            displayPowerController.mDisplayEngineInterface.initialize();
            displayPowerController.mUsingSRE = displayPowerController.mDisplayEngineInterface.getSupported("FEATURE_SRE");
            Slog.i(TAG, "DisplayEngineInterface getSupported SRE:" + displayPowerController.mUsingSRE);
        }
        displayPowerController.mCurrentScreenBrightnessSetting = getScreenBrightnessSetting();
        displayPowerController.mScreenBrightnessForVr = getScreenBrightnessForVrSetting();
        displayPowerController.mAutoBrightnessAdjustment = getAutoBrightnessAdjustmentSetting();
        displayPowerController.mTemporaryScreenBrightness = -1;
        displayPowerController.mPendingScreenBrightnessSetting = -1;
        displayPowerController.mTemporaryAutoBrightnessAdjustment = Float.NaN;
        displayPowerController.mPendingAutoBrightnessAdjustment = Float.NaN;
        int smartBackLightConfig = SystemProperties.getInt("ro.config.hw_smart_backlight", 1);
        if (displayPowerController.mUsingSRE || smartBackLightConfig == 1) {
            if (displayPowerController.mUsingSRE) {
                Slog.i(TAG, "Use SRE instead of SBL");
            } else {
                displayPowerController.mSmartBackLightSupported = true;
                if (DEBUG) {
                    Slog.i(TAG, "get ro.config.hw_smart_backlight = 1");
                }
            }
            int smartBackLightSetting = Settings.System.getInt(displayPowerController.mContext.getContentResolver(), "smart_backlight_enable", -1);
            if (smartBackLightSetting == -1) {
                if (DEBUG) {
                    Slog.i(TAG, "get Settings.System.SMART_BACKLIGHT failed, set default value to 1");
                }
                Settings.System.putInt(displayPowerController.mContext.getContentResolver(), "smart_backlight_enable", 1);
            } else if (DEBUG) {
                Slog.i(TAG, "get Settings.System.SMART_BACKLIGHT = " + smartBackLightSetting);
            }
        } else if (DEBUG) {
            Slog.i(TAG, "get ro.config.hw_smart_backlight = " + smartBackLightConfig + ", mUsingSRE = false, don't support sbl or sre");
        }
        HwServiceFactory.IHwNormalizedManualBrightnessController iadm2 = HwServiceFactory.getHuaweiManualBrightnessController();
        if (iadm2 != null) {
            displayPowerController.mManualBrightnessController = iadm2.getInstance(displayPowerController, displayPowerController.mContext, displayPowerController.mSensorManager);
            if (DEBUG) {
                Slog.i(TAG, "HBM ManualBrightnessController initialized");
            }
        } else {
            displayPowerController.mManualBrightnessController = new ManualBrightnessController(displayPowerController);
        }
        if (displayPowerController.mUseSensorHubLABC) {
            displayPowerController.mLABCSensor = displayPowerController.mSensorManager.getDefaultSensor(65543);
            if (displayPowerController.mLABCSensor == null) {
                Slog.e(TAG, "[LABC] Get LABC Sensor failed !! ");
            }
        } else if (displayPowerController.mSmartBackLightSupported) {
            displayPowerController.mHwSmartBackLightController = HwServiceFactory.getHwSmartBackLightController();
            if (displayPowerController.mHwSmartBackLightController != null) {
                displayPowerController.mUsingHwSmartBackLightController = displayPowerController.mHwSmartBackLightController.checkIfUsingHwSBL();
                displayPowerController.mHwSmartBackLightController.StartHwSmartBackLightController(displayPowerController.mContext, displayPowerController.mLights, displayPowerController.mSensorManager);
            }
        }
        displayPowerController.mDisplayEffectMonitor = HwServiceFactory.getDisplayEffectMonitor(displayPowerController.mContext);
        if (displayPowerController.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        HwServiceFactory.loadHwBrightnessProcessors(displayPowerController.mAutomaticBrightnessController, displayPowerController.mManualBrightnessController);
        if (displayPowerController.mHwDisplayPowerEx != null) {
            displayPowerController.mHwDisplayPowerEx.initTpKeepParamters();
        }
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

    public boolean requestPowerState(DisplayManagerInternal.DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        if (DEBUG && DEBUG_Controller) {
            Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity);
        }
        synchronized (this.mLock) {
            boolean changed = false;
            if (waitForNegativeProximity) {
                try {
                    if (!this.mPendingWaitForNegativeProximityLocked) {
                        this.mPendingWaitForNegativeProximityLocked = true;
                        changed = true;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (this.mPendingRequestLocked == null) {
                this.mPendingRequestLocked = new DisplayManagerInternal.DisplayPowerRequest(request);
                changed = true;
            } else if (!this.mPendingRequestLocked.equals(request)) {
                this.mPendingRequestLocked.copyFrom(request);
                changed = true;
                if (this.mHwDisplayPowerEx != null) {
                    this.mHwDisplayPowerEx.setTPDozeMode(request.useProximitySensor);
                }
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
        return this.mAutomaticBrightnessController.getDefaultConfig();
    }

    /* access modifiers changed from: private */
    public void sendUpdatePowerState() {
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
        this.mPowerState = new DisplayPowerState(this.mContext, this.mBlanker, this.mColorFadeEnabled ? new ColorFade(0) : null);
        this.mAutoCustomBackLight = this.mLights.getLight(LightsManager.LIGHT_ID_AUTOCUSTOMBACKLIGHT);
        this.mManualCustomBackLight = this.mLights.getLight(LightsManager.LIGHT_ID_MANUALCUSTOMBACKLIGHT);
        if (this.mColorFadeEnabled) {
            this.mColorFadeOnAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{0.0f, 1.0f});
            this.mColorFadeOnAnimator.setDuration(250);
            this.mColorFadeOnAnimator.addListener(this.mAnimatorListener);
            this.mColorFadeOffAnimator = ObjectAnimator.ofFloat(this.mPowerState, DisplayPowerState.COLOR_FADE_LEVEL, new float[]{1.0f, 0.0f});
            this.mColorFadeOffAnimator.setDuration(150);
            this.mColorFadeOffAnimator.addListener(this.mAnimatorListener);
        }
        HwServiceFactory.IHwRampAnimator iadm = HwServiceFactory.getHwNormalizedRampAnimator();
        if (iadm != null) {
            this.mScreenBrightnessRampAnimator = iadm.getInstance(this.mPowerState, DisplayPowerState.SCREEN_BRIGHTNESS);
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

    private int getBrightness(boolean autoBrightnessAdjustmentChanged) {
        if (autoBrightnessAdjustmentChanged) {
            return this.mPendingBacklight;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01ab, code lost:
        if (r7.mWaitingForNegativeProximity == false) goto L_0x01bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x01af, code lost:
        if (r7.mScreenOffBecauseOfProximity == false) goto L_0x01bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01b3, code lost:
        if (r7.mProximity != 1) goto L_0x01bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x01b5, code lost:
        if (r3 == 1) goto L_0x01bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01b7, code lost:
        setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01bd, code lost:
        if (r7.mWaitingForNegativeProximity != false) goto L_0x01d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x01c1, code lost:
        if (r7.mScreenOffBecauseOfProximity == false) goto L_0x01d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x01c5, code lost:
        if (r7.mProximity == -1) goto L_0x01d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01c7, code lost:
        if (r3 != 1) goto L_0x01d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x01cd, code lost:
        if (r7.mPowerRequest.useProximitySensor == false) goto L_0x01d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x01cf, code lost:
        setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x01d7, code lost:
        if (r7.mPowerRequest.useProximitySensor != false) goto L_0x01dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x01d9, code lost:
        setProximitySensorEnabled(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x01dc, code lost:
        r7.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x01e0, code lost:
        if (r7.mScreenOffBecauseOfProximity != false) goto L_0x01eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x01e4, code lost:
        if (r7.mProximity != 1) goto L_0x01eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x01e6, code lost:
        r7.mScreenOffBecauseOfProximity = true;
        sendOnProximityPositiveWithWakelock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x01ed, code lost:
        if (r7.mScreenOffBecauseOfProximity == false) goto L_0x0209;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x01f1, code lost:
        if (r7.mProximity == 1) goto L_0x0209;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x01f3, code lost:
        r7.mScreenOffBecauseOfProximity = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x01f9, code lost:
        if (r7.mPowerRequest.useProximitySensor == false) goto L_0x0203;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x01ff, code lost:
        if (r7.mPowerRequest.useProximitySensorbyPhone == false) goto L_0x0203;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0201, code lost:
        r7.mScreenOnBecauseOfPhoneProximity = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0203, code lost:
        sendOnProximityNegativeWithWakelock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x0207, code lost:
        r7.mWaitingForNegativeProximity = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x020b, code lost:
        if (r7.mScreenOffBecauseOfProximity == false) goto L_0x0214;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0211, code lost:
        if (r7.mPowerRequest.useProximitySensorbyPhone != false) goto L_0x0214;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0213, code lost:
        r3 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x0214, code lost:
        sre_init(r3);
        hbm_init(r3);
        sendScreenStateToDE(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x021d, code lost:
        if (r2 == false) goto L_0x0238;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x0221, code lost:
        if (r7.mLastWaitBrightnessMode == false) goto L_0x0231;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0227, code lost:
        if (r7.mPowerRequest.brightnessWaitMode != false) goto L_0x0231;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x022d, code lost:
        if (r7.mPowerRequest.brightnessWaitRet != false) goto L_0x0231;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x022f, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0231, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0232, code lost:
        if (r5 != false) goto L_0x0236;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0234, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0236, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0237, code lost:
        r2 = r2 & r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0238, code lost:
        r14 = r7.mPowerState.getScreenState();
        animateScreenStateChange(r3, r2);
        r15 = r7.mPowerState.getScreenState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x0248, code lost:
        if (r15 != 1) goto L_0x024f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x024a, code lost:
        r1 = 0;
        r7.mWakeupFromSleep = true;
        r7.mProximityPositive = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0250, code lost:
        if (r15 != 5) goto L_0x0254;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x0252, code lost:
        r1 = r7.mScreenBrightnessForVr;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0254, code lost:
        if (r1 >= 0) goto L_0x0265;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x025a, code lost:
        if (r7.mPowerRequest.screenBrightnessOverride <= 0) goto L_0x0265;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x025c, code lost:
        r1 = r7.mPowerRequest.screenBrightnessOverride;
        r7.mTemporaryScreenBrightness = -1;
        r7.mAppliedScreenBrightnessOverride = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x0265, code lost:
        r7.mAppliedScreenBrightnessOverride = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0269, code lost:
        if (r7.mAllowAutoBrightnessWhileDozingConfig == false) goto L_0x0273;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x026f, code lost:
        if (android.view.Display.isDozeState(r15) == false) goto L_0x0273;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x0271, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x0273, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x0274, code lost:
        r16 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x027a, code lost:
        if (r7.mPowerRequest.useAutoBrightness == false) goto L_0x0288;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x027c, code lost:
        if (r15 == 2) goto L_0x0280;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x027e, code lost:
        if (r16 == false) goto L_0x0288;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x0280, code lost:
        if (r1 >= 0) goto L_0x0288;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0284, code lost:
        if (r7.mAutomaticBrightnessController == null) goto L_0x0288;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x0286, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x0288, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x0289, code lost:
        r5 = r2;
        r25 = updateUserSetScreenBrightness();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x028e, code lost:
        if (r25 != false) goto L_0x0292;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x0290, code lost:
        if (r5 == false) goto L_0x0294;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x0292, code lost:
        r7.mTemporaryScreenBrightness = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x0296, code lost:
        if (r7.mTemporaryScreenBrightness <= 0) goto L_0x029d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0298, code lost:
        r1 = r7.mTemporaryScreenBrightness;
        r7.mAppliedTemporaryBrightness = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x029d, code lost:
        r7.mAppliedTemporaryBrightness = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x029f, code lost:
        r26 = updateAutoBrightnessAdjustment();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x02a3, code lost:
        if (r26 == false) goto L_0x02a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x02a5, code lost:
        r7.mTemporaryAutoBrightnessAdjustment = Float.NaN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x02af, code lost:
        if (java.lang.Float.isNaN(r7.mTemporaryAutoBrightnessAdjustment) != false) goto L_0x02b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x02b1, code lost:
        r2 = r7.mTemporaryAutoBrightnessAdjustment;
        r7.mAppliedTemporaryAutoBrightnessAdjustment = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x02b5, code lost:
        r27 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x02b8, code lost:
        r2 = r7.mAutoBrightnessAdjustment;
        r7.mAppliedTemporaryAutoBrightnessAdjustment = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x02c1, code lost:
        if (r7.mPowerRequest.boostScreenBrightness == false) goto L_0x02ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x02c3, code lost:
        if (r1 == 0) goto L_0x02ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x02c5, code lost:
        r1 = 255;
        r7.mAppliedBrightnessBoost = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x02ca, code lost:
        r7.mAppliedBrightnessBoost = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x02cc, code lost:
        r28 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x02ce, code lost:
        if (r28 >= 0) goto L_0x02d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x02d0, code lost:
        if (r26 != false) goto L_0x02d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x02d2, code lost:
        if (r25 == false) goto L_0x02d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x02d4, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x02d6, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x02d7, code lost:
        r3 = r1;
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x02db, code lost:
        if (r7.mAutomaticBrightnessController == null) goto L_0x030c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x02dd, code lost:
        r7.mAutomaticBrightnessController.updatePowerPolicy(r7.mPowerRequest.policy);
        r1 = r7.mAutomaticBrightnessController.hasUserDataPoints();
        r7.mAutomaticBrightnessController.configure(r5, r7.mBrightnessConfiguration, ((float) r7.mLastUserSetScreenBrightness) / 255.0f, r25, r27, r26, r7.mPowerRequest.policy);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x030c, code lost:
        r12 = r1;
        r7.mAutomaticBrightnessController.updateCurrentUserId(r7.mPowerRequest.userId);
        r7.mManualBrightnessController.updateCurrentUserId(r7.mPowerRequest.userId);
        r6 = android.provider.Settings.System.getFloatForUser(r7.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, r7.mPowerRequest.userId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x0332, code lost:
        if (r7.mUseSensorHubLABC != false) goto L_0x0410;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0336, code lost:
        if (DEBUG == false) goto L_0x0365;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x033a, code lost:
        if (r7.mAutoBrightnessEnabled == r5) goto L_0x0365;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x033c, code lost:
        android.util.Slog.d(TAG, "mode change : autoBrightnessEnabled=" + r5 + ",adjustment=" + r6 + ",state=" + r15);
        r7.mAutoBrightnessEnabled = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x0369, code lost:
        if (r7.mPowerRequest.screenAutoBrightness <= 0) goto L_0x0374;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x036b, code lost:
        r7.mAutomaticBrightnessController.updateIntervenedAutoBrightness(r7.mPowerRequest.screenAutoBrightness);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0376, code lost:
        if (r7.mBrightnessModeChanged == false) goto L_0x037a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:225:0x0378, code lost:
        r7.mBrightnessModeChanged = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x037a, code lost:
        r1 = r7.mScreenBrightnessRampAnimator;
        r2 = r7.mAutomaticBrightnessController.getUpdateAutoBrightnessCount();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x0386, code lost:
        if (r7.mPowerRequest.screenAutoBrightness <= 0) goto L_0x038a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x0388, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x038a, code lost:
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x038b, code lost:
        r1.updateBrightnessRampPara(r5, r2, r4, r7.mPowerRequest.policy);
        r7.mfastAnimtionFlag = r7.mAutomaticBrightnessController.getPowerStatus();
        r7.mScreenBrightnessRampAnimator.updateFastAnimationFlag(r7.mfastAnimtionFlag);
        r7.mCoverModeAnimationFast = r7.mAutomaticBrightnessController.getCoverModeFastResponseFlag();
        r7.mScreenBrightnessRampAnimator.updateCoverModeFastAnimationFlag(r7.mCoverModeAnimationFast);
        r7.mScreenBrightnessRampAnimator.updateCameraModeChangeAnimationEnable(r7.mAutomaticBrightnessController.getCameraModeChangeAnimationEnable());
        r7.mScreenBrightnessRampAnimator.updateGameModeChangeAnimationEnable(r7.mAutomaticBrightnessController.getAnimationGameChangeEnable());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x03cc, code lost:
        if (r7.mAutomaticBrightnessController.getReadingModeBrightnessLineEnable() == false) goto L_0x03d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x03ce, code lost:
        r7.mScreenBrightnessRampAnimator.updateReadingModeChangeAnimationEnable(r7.mAutomaticBrightnessController.getReadingModeChangeAnimationEnable());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x03d9, code lost:
        r7.mScreenBrightnessRampAnimator.setBrightnessAnimationTime(r7.mAnimationEnabled, r7.mMillisecond);
        r7.mScreenBrightnessRampAnimator.updateScreenLockedAnimationEnable(r7.mKeyguardIsLocked);
        r7.mOutdoorAnimationFlag = r7.mAutomaticBrightnessController.getOutdoorAnimationFlag();
        r7.mScreenBrightnessRampAnimator.updateOutdoorAnimationFlag(r7.mOutdoorAnimationFlag);
        updatemManualModeAnimationEnable();
        updateManualPowerSavingAnimationEnable();
        updateManualThermalModeAnimationEnable();
        updateBrightnessModeAnimationEnable();
        updateDarkAdaptDimmingEnable();
        updateFastDarkenDimmingEnable();
        r7.mBackLight.updateBrightnessAdjustMode(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0412, code lost:
        if (r7.mLABCSensorEnabled == false) goto L_0x0449;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x0414, code lost:
        if (r5 == false) goto L_0x0440;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x0416, code lost:
        if (r26 == false) goto L_0x0440;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x041a, code lost:
        if (DEBUG == false) goto L_0x0434;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x041c, code lost:
        android.util.Slog.d(TAG, "[LABC]  A = " + r7.mSetAutoBackLight);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x0434, code lost:
        r7.mAutoCustomBackLight.sendCustomBackLight(r7.mSetAutoBackLight);
        r7.mAutoBrightnessAdjustmentChanged = true;
        r7.mLastStatus = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x043f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x0440, code lost:
        if (r5 != false) goto L_0x0449;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x0444, code lost:
        if (r7.mLastStatus != true) goto L_0x0449;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x0446, code lost:
        r7.mLastStatus = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:0x0449, code lost:
        r29 = r3;
        r30 = r5;
        r8 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x0469, code lost:
        if (waitScreenBrightness(r15, r7.mPowerRequest.brightnessWaitMode, r7.mLastWaitBrightnessMode, r7.mPowerRequest.brightnessWaitRet, r7.mPowerRequest.skipWaitKeyguardDismiss) == false) goto L_0x0472;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:0x046b, code lost:
        r28 = 0;
        r7.mWindowManagerPolicy.setInterceptInputForWaitBrightness(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x0474, code lost:
        if (r7.mGlobalAlpmState != 0) goto L_0x0478;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x0476, code lost:
        r28 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x047c, code lost:
        if (r7.mPowerRequest.boostScreenBrightness == false) goto L_0x0482;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:255:0x047e, code lost:
        if (r28 == 0) goto L_0x0482;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x0480, code lost:
        r28 = 255;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x0482, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x0483, code lost:
        if (r28 >= 0) goto L_0x051e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x0485, code lost:
        r2 = r27;
        r4 = r30;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x048a, code lost:
        if (r4 == false) goto L_0x04e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x048e, code lost:
        if (r7.mUseSensorHubLABC != false) goto L_0x0497;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x0490, code lost:
        r5 = r7.mAutomaticBrightnessController.getAutomaticScreenBrightness();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x0497, code lost:
        r5 = getBrightness(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:265:0x049c, code lost:
        r2 = r7.mAutomaticBrightnessController.getAutomaticScreenBrightnessAdjustmentNew(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x04a2, code lost:
        if (r5 >= 0) goto L_0x04e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x04b4, code lost:
        if ((android.os.SystemClock.uptimeMillis() - r7.mAutomaticBrightnessController.getLightSensorEnableTime()) <= 195) goto L_0x04e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x04b6, code lost:
        if (r5 == -2) goto L_0x04c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:270:0x04b8, code lost:
        r0 = android.provider.Settings.System.getInt(r7.mContext.getContentResolver(), "screen_brightness", 100);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x04c8, code lost:
        r0 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:0x04cb, code lost:
        if (DEBUG == false) goto L_0x04e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:274:0x04cd, code lost:
        android.util.Slog.d(TAG, "failed to get auto brightness so set brightness based on SCREEN_BRIGHTNESS:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:0x04e4, code lost:
        r0 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x04e6, code lost:
        r0 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:277:0x04e8, code lost:
        if (r0 < 0) goto L_0x050b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x04ea, code lost:
        r0 = clampScreenBrightness(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:279:0x04f0, code lost:
        if (r7.mUseSensorHubLABC != false) goto L_0x04fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x04f4, code lost:
        if (r7.mAppliedAutoBrightness == false) goto L_0x0503;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x04f6, code lost:
        if (r26 != false) goto L_0x0503;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x04f8, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:285:0x04fc, code lost:
        if (r7.mAppliedAutoBrightness == false) goto L_0x0503;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x0500, code lost:
        if (r7.mAutoBrightnessAdjustmentChanged != false) goto L_0x0503;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:288:0x0502, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:0x0503, code lost:
        putScreenBrightnessSetting(r0);
        r7.mAppliedAutoBrightness = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x0508, code lost:
        r28 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x050c, code lost:
        if (r0 != -2) goto L_0x0510;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x050e, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x0510, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:295:0x0511, code lost:
        r7.mAppliedAutoBrightness = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:297:0x0516, code lost:
        if (r27 == r2) goto L_0x051b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x0518, code lost:
        putAutoBrightnessAdjustmentSetting(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:299:0x051b, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x051e, code lost:
        r4 = r30;
        r0 = false;
        r7.mAppliedAutoBrightness = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:301:0x0523, code lost:
        r7.mAutoBrightnessAdjustmentChanged = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:302:0x0525, code lost:
        if (r28 >= 0) goto L_0x0530;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x052b, code lost:
        if (android.view.Display.isDozeState(r15) == false) goto L_0x0530;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:305:0x052d, code lost:
        r0 = r7.mScreenBrightnessDozeConfig;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x0530, code lost:
        r0 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x0532, code lost:
        if (r0 >= 0) goto L_0x0547;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:309:0x0538, code lost:
        if (r7.mPowerRequest.useAutoBrightness != false) goto L_0x0547;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:310:0x053a, code lost:
        r0 = clampScreenBrightness(getScreenBrightnessSetting());
        r7.mAutomaticBrightnessController.updateIntervenedAutoBrightness(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:312:0x0548, code lost:
        if (r15 != 2) goto L_0x0579;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:314:0x054e, code lost:
        if (r7.mPowerRequest.policy != 0) goto L_0x0579;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x0552, code lost:
        if (r0 <= r7.mScreenBrightnessRangeMinimum) goto L_0x0562;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x0554, code lost:
        r0 = java.lang.Math.max(java.lang.Math.min(r0 - 10, r7.mScreenBrightnessDimConfig), r7.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x0562, code lost:
        android.util.Slog.i(TAG, "set brightness to DIM brightness:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x057e, code lost:
        if (r7.mPowerRequest.policy != 2) goto L_0x05b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:322:0x0582, code lost:
        if (r0 <= r7.mScreenBrightnessRangeMinimum) goto L_0x0592;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x0584, code lost:
        r0 = java.lang.Math.max(java.lang.Math.min(r0 - 10, r7.mScreenBrightnessDimConfig), r7.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:325:0x0596, code lost:
        if (android.util.HwPCUtils.isPcCastModeInServer() != false) goto L_0x059f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x059d, code lost:
        if (android.util.HwPCUtils.getPhoneDisplayID() == -1) goto L_0x05a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x05a3, code lost:
        if (android.util.HwPCUtils.enabledInPad() != false) goto L_0x05a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x05a5, code lost:
        r0 = 0;
        blackScreenOnPcMode();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x05ab, code lost:
        if (r7.mAppliedDimming != false) goto L_0x05ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x05ad, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x05ae, code lost:
        r7.mAppliedDimming = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x05b3, code lost:
        if (r7.mAppliedDimming == false) goto L_0x05b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x05b5, code lost:
        r1 = false;
        r7.mAppliedDimming = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x05bf, code lost:
        if (r7.mPowerRequest.lowPowerMode == false) goto L_0x05de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:0x05c3, code lost:
        if (r0 <= r7.mScreenBrightnessRangeMinimum) goto L_0x05d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:342:0x05c5, code lost:
        r0 = java.lang.Math.max((int) (((float) r0) * java.lang.Math.min(r7.mPowerRequest.screenLowPowerBrightnessFactor, 1.0f)), r7.mScreenBrightnessRangeMinimum);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x05d8, code lost:
        if (r7.mAppliedLowPower != false) goto L_0x05db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:345:0x05da, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:346:0x05db, code lost:
        r7.mAppliedLowPower = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:348:0x05e0, code lost:
        if (r7.mAppliedLowPower == false) goto L_0x05e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:349:0x05e2, code lost:
        r1 = false;
        r7.mAppliedLowPower = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:350:0x05e6, code lost:
        r2 = r1;
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:351:0x05ea, code lost:
        if (r7.mPendingScreenOff != false) goto L_0x06e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:353:0x05ee, code lost:
        if (r7.mSkipScreenOnBrightnessRamp == false) goto L_0x061c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:355:0x05f1, code lost:
        if (r15 != 2) goto L_0x0619;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:0x05f5, code lost:
        if (r7.mSkipRampState != 0) goto L_0x0600;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:0x05f9, code lost:
        if (r7.mDozing == false) goto L_0x0600;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:0x05fb, code lost:
        r7.mInitialAutoBrightness = r1;
        r7.mSkipRampState = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:362:0x0602, code lost:
        if (r7.mSkipRampState != 1) goto L_0x0610;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x0606, code lost:
        if (r7.mUseSoftwareAutoBrightnessConfig == false) goto L_0x0610;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:366:0x060a, code lost:
        if (r1 == r7.mInitialAutoBrightness) goto L_0x0610;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:367:0x060c, code lost:
        r7.mSkipRampState = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:369:0x0613, code lost:
        if (r7.mSkipRampState != 2) goto L_0x061c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:370:0x0615, code lost:
        r7.mSkipRampState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:371:0x0619, code lost:
        r7.mSkipRampState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:373:0x061d, code lost:
        if (r15 == 5) goto L_0x0624;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:374:0x061f, code lost:
        if (r14 != 5) goto L_0x0622;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:375:0x0622, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:376:0x0624, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:378:0x0626, code lost:
        if (r15 != 2) goto L_0x062e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:0x062a, code lost:
        if (r7.mSkipRampState == 0) goto L_0x062e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:381:0x062c, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:382:0x062e, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x0634, code lost:
        if (android.view.Display.isDozeState(r15) == false) goto L_0x063c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:386:0x0638, code lost:
        if (r7.mBrightnessBucketsInDozeConfig == false) goto L_0x063c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:387:0x063a, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:0x063c, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:390:0x063f, code lost:
        if (r7.mColorFadeEnabled == false) goto L_0x064d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:392:0x0649, code lost:
        if (r7.mPowerState.getColorFadeLevel() != 1.0f) goto L_0x064d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:393:0x064b, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:394:0x064d, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:396:0x0650, code lost:
        if (r7.mAppliedTemporaryBrightness != false) goto L_0x0659;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:398:0x0654, code lost:
        if (r7.mAppliedTemporaryAutoBrightnessAdjustment == false) goto L_0x0657;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:399:0x0657, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:400:0x0659, code lost:
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:401:0x065a, code lost:
        if (r5 != false) goto L_0x06c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:402:0x065c, code lost:
        if (r6 != false) goto L_0x06c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:403:0x065e, code lost:
        if (r0 != false) goto L_0x06c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:404:0x0660, code lost:
        if (r3 == false) goto L_0x06c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:405:0x0662, code lost:
        if (r9 != false) goto L_0x06c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:406:0x0664, code lost:
        r31 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:407:0x0668, code lost:
        if (r7.mBrightnessBucketsInDozeConfig == false) goto L_0x066b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:409:0x066c, code lost:
        if (r15 != 2) goto L_0x06a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:411:0x0670, code lost:
        if (r7.mImmeBright != false) goto L_0x0676;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:413:0x0674, code lost:
        if (r7.mWakeupFromSleep == false) goto L_0x06a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:414:0x0676, code lost:
        r7.mImmeBright = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:415:0x067f, code lost:
        if (r7.mAutomaticBrightnessController.getRebootFirstBrightnessAnimationEnable() == false) goto L_0x069d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:417:0x0683, code lost:
        if (r7.mRebootWakeupFromSleep == false) goto L_0x069d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:418:0x0685, code lost:
        if (r1 <= 0) goto L_0x0698;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:419:0x0688, code lost:
        if (r2 == false) goto L_0x068d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:420:0x068a, code lost:
        r0 = r7.mBrightnessRampRateSlow;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:421:0x068d, code lost:
        r0 = r7.mBrightnessRampRateFast;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:422:0x068f, code lost:
        animateScreenBrightness(r1, r0);
        r7.mRebootWakeupFromSleep = false;
        r7.mWakeupFromSleep = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:423:0x0698, code lost:
        animateScreenBrightness(r1, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:425:0x069e, code lost:
        if (r1 <= 0) goto L_0x06a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:426:0x06a0, code lost:
        r7.mWakeupFromSleep = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:427:0x06a2, code lost:
        animateScreenBrightness(r1, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:429:0x06ad, code lost:
        if (r7.mAutomaticBrightnessController.getSetbrightnessImmediateEnableForCaliTest() == false) goto L_0x06b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:430:0x06af, code lost:
        animateScreenBrightness(r1, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:431:0x06b5, code lost:
        if (r2 == false) goto L_0x06ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:432:0x06b7, code lost:
        r0 = r7.mBrightnessRampRateSlow;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:433:0x06ba, code lost:
        r0 = r7.mBrightnessRampRateFast;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:434:0x06bc, code lost:
        animateScreenBrightness(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:435:0x06c0, code lost:
        r31 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:436:0x06c2, code lost:
        if (r9 == false) goto L_0x06d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:438:0x06c8, code lost:
        if (r7.mPowerRequest.useAutoBrightness != false) goto L_0x06d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:439:0x06ca, code lost:
        if (r2 == false) goto L_0x06cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:440:0x06cc, code lost:
        r0 = r7.mBrightnessRampRateSlow;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:441:0x06cf, code lost:
        r0 = r7.mBrightnessRampRateFast;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:442:0x06d1, code lost:
        animateScreenBrightness(r1, r0);
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:443:0x06d6, code lost:
        r0 = false;
        animateScreenBrightness(r1, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:444:0x06da, code lost:
        if (r1 <= 0) goto L_0x06de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:445:0x06dc, code lost:
        r7.mWakeupFromSleep = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:446:0x06de, code lost:
        if (r9 != false) goto L_0x06e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:447:0x06e0, code lost:
        r32 = r2;
        notifyBrightnessChanged(r1, r29, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:448:0x06e8, code lost:
        r32 = r2;
        r2 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:449:0x06ec, code lost:
        r7.mLastWaitBrightnessMode = r7.mPowerRequest.brightnessWaitMode;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:450:0x06f4, code lost:
        if (r7.mPendingScreenOnUnblocker != null) goto L_0x0716;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:452:0x06f8, code lost:
        if (r7.mColorFadeEnabled == false) goto L_0x070a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:454:0x0700, code lost:
        if (r7.mColorFadeOnAnimator.isStarted() != false) goto L_0x0716;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:456:0x0708, code lost:
        if (r7.mColorFadeOffAnimator.isStarted() != false) goto L_0x0716;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:458:0x0712, code lost:
        if (r7.mPowerState.waitUntilClean(r7.mCleanListener) == false) goto L_0x0716;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:459:0x0714, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:460:0x0716, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:461:0x0717, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:462:0x071e, code lost:
        if (r7.mWindowManagerPolicy.getInterceptInputForWaitBrightness() == false) goto L_0x0732;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:464:0x0724, code lost:
        if (r7.mPowerRequest.brightnessWaitMode != false) goto L_0x0732;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:466:0x0728, code lost:
        if (r7.mPendingScreenOnForKeyguardDismissUnblocker != null) goto L_0x0732;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:467:0x072a, code lost:
        if (r3 == false) goto L_0x0732;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:468:0x072c, code lost:
        r7.mWindowManagerPolicy.setInterceptInputForWaitBrightness(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:469:0x0732, code lost:
        if (r3 == false) goto L_0x073e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:471:0x073a, code lost:
        if (r7.mScreenBrightnessRampAnimator.isAnimating() != false) goto L_0x073e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:472:0x073c, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:473:0x073e, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:474:0x073f, code lost:
        r5 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:475:0x0740, code lost:
        if (r3 == false) goto L_0x0752;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:477:0x0743, code lost:
        if (r15 == 1) goto L_0x0752;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:479:0x0747, code lost:
        if (r7.mReportedScreenStateToPolicy != 1) goto L_0x0752;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:480:0x0749, code lost:
        setReportedScreenState(2);
        r7.mWindowManagerPolicy.screenTurnedOn();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:481:0x0752, code lost:
        if (r5 != false) goto L_0x076b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:483:0x0756, code lost:
        if (r7.mUnfinishedBusiness != false) goto L_0x076b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:485:0x075a, code lost:
        if (DEBUG == false) goto L_0x0763;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:486:0x075c, code lost:
        android.util.Slog.d(TAG, "Unfinished business...");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:487:0x0763, code lost:
        r7.mCallbacks.acquireSuspendBlocker();
        r7.mUnfinishedBusiness = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:488:0x076b, code lost:
        if (r3 == false) goto L_0x078c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:489:0x076d, code lost:
        if (r11 == false) goto L_0x078c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:490:0x076f, code lost:
        r6 = r7.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:491:0x0771, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:494:0x0774, code lost:
        if (r7.mPendingRequestChangedLocked != false) goto L_0x0784;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:495:0x0776, code lost:
        r7.mDisplayReadyLocked = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:496:0x077b, code lost:
        if (DEBUG == false) goto L_0x0784;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:497:0x077d, code lost:
        android.util.Slog.d(TAG, "Display ready!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:498:0x0784, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:499:0x0785, code lost:
        sendOnStateChangedWithWakelock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:504:0x078c, code lost:
        if (r5 == false) goto L_0x07b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:506:0x0790, code lost:
        if (r7.mUnfinishedBusiness == false) goto L_0x07b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:508:0x0794, code lost:
        if (DEBUG == false) goto L_0x079d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:509:0x0796, code lost:
        android.util.Slog.d(TAG, "Finished business...");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:511:0x079f, code lost:
        if (r7.fpDataCollector == null) goto L_0x07a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:512:0x07a1, code lost:
        if (r1 <= 0) goto L_0x07a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:513:0x07a3, code lost:
        r7.fpDataCollector.reportScreenTurnedOn();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:514:0x07a8, code lost:
        r0 = false;
        r7.mUnfinishedBusiness = false;
        r7.mCallbacks.releaseSuspendBlocker();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:515:0x07b1, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:517:0x07b3, code lost:
        if (r15 == 2) goto L_0x07b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:518:0x07b5, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:519:0x07b7, code lost:
        r7.mDozing = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:520:0x07b9, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x015c, code lost:
        if (r10 == false) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x015e, code lost:
        initialize();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0161, code lost:
        r1 = -1;
        r2 = false;
        r3 = r7.mPowerRequest.policy;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0168, code lost:
        if (r3 == 4) goto L_0x0187;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x016a, code lost:
        switch(r3) {
            case 0: goto L_0x0184;
            case 1: goto L_0x016f;
            default: goto L_0x016d;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x016d, code lost:
        r3 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0173, code lost:
        if (r7.mPowerRequest.dozeScreenState == 0) goto L_0x017a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0175, code lost:
        r3 = r7.mPowerRequest.dozeScreenState;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x017a, code lost:
        r3 = 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x017d, code lost:
        if (r7.mAllowAutoBrightnessWhileDozingConfig != false) goto L_0x0189;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x017f, code lost:
        r1 = r7.mPowerRequest.dozeScreenBrightness;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0184, code lost:
        r3 = 1;
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0187, code lost:
        r3 = 5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x018e, code lost:
        if (r7.mProximitySensor == null) goto L_0x0207;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0194, code lost:
        if (r7.mPowerRequest.useProximitySensor == false) goto L_0x01a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0196, code lost:
        if (r3 == 1) goto L_0x01a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0198, code lost:
        setProximitySensorEnabled(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x019d, code lost:
        if (r7.mScreenOffBecauseOfProximity != false) goto L_0x01de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01a1, code lost:
        if (r7.mProximity != 1) goto L_0x01de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01a3, code lost:
        r7.mScreenOffBecauseOfProximity = true;
        sendOnProximityPositiveWithWakelock();
     */
    public void updatePowerState() {
        boolean mustInitialize = false;
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
                        if (this.mUseSensorHubLABC) {
                            this.mLastStatus = true;
                        }
                    } else if (this.mPendingRequestChangedLocked) {
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
                        if (this.mBrightnessModeChanged && !this.mCurrentUserIdChange && this.mPowerRequest.useAutoBrightness && !isCoverModeChanged && !this.mIsCoverModeClosed && !this.mBrightnessModeChangeNoClearOffsetEnable && !this.mModeToAutoNoClearOffsetEnable) {
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
                        if (this.mUseSensorHubLABC) {
                            boolean z = this.mLastStatus;
                        }
                    }
                    boolean mustInitialize2 = mustInitialize;
                    try {
                        this.mScreenOnBecauseOfPhoneProximity = false;
                        boolean mustNotify = !this.mDisplayReadyLocked;
                    } catch (Throwable th) {
                        th = th;
                        boolean z2 = mustInitialize2;
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
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
        } else if (this.mLABCSensor != null) {
            this.mLABCEnabled = true;
            setLABCEnabled(wantScreenOn(state));
        }
    }

    private void hbm_init(int state) {
        if (SystemProperties.getInt("ro.config.hw_high_bright_mode", 1) == 1 && getManualModeEnable()) {
            boolean isManulMode = true ^ this.mPowerRequest.useAutoBrightness;
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
            this.mPendingScreenOnForKeyguardDismissUnblocker = new ScreenOnForKeyguardDismissUnblocker();
            this.mScreenOnForKeyguardDismissBlockStartRealTime = SystemClock.elapsedRealtime();
            Slog.i(TAG, "Blocking screen on until keyguard dismiss done.");
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "Blocking screen on until keyguard dismiss done");
                iZrHung.addInfo(arg);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unblockScreenOnForKeyguardDismiss() {
        if (this.mPendingScreenOnForKeyguardDismissUnblocker != null) {
            this.mPendingScreenOnForKeyguardDismissUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOnForKeyguardDismissBlockStartRealTime;
            Slog.i(TAG, "fingerunlock--Unblocked screen on for keyguard dismiss after " + delay + " ms");
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "Unblocked screen on for keyguard dismiss after " + delay + " ms");
                iZrHung.addInfo(arg);
            }
        }
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
            if (Jlog.isPerfTest()) {
                Jlog.i(2205, "JL_PWRSCRON_DPC_BLOCKSCREENON");
            }
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Blocking screen on until initial contents have been drawn.");
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "Blocking screen on until initial contents have been drawn");
                iZrHung.addInfo(arg);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unblockScreenOn() {
        if (this.mPendingScreenOnUnblocker != null) {
            this.mPendingScreenOnUnblocker = null;
            long delay = SystemClock.elapsedRealtime() - this.mScreenOnBlockStartRealTime;
            if (Jlog.isPerfTest()) {
                Jlog.i(2206, "JL_PWRSCRON_DPC_UNBLOCKSCREENON");
            }
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Unblocked screen on after " + delay + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_TRACE_NAME, 0);
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "Unblocked screen on after " + delay + " ms");
                iZrHung.addInfo(arg);
            }
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
    public void unblockScreenOff() {
        if (this.mPendingScreenOffUnblocker != null) {
            this.mPendingScreenOffUnblocker = null;
            Slog.i(TAG, "UL_PowerUnblocked screen off after " + (SystemClock.elapsedRealtime() - this.mScreenOffBlockStartRealTime) + " ms");
            Trace.asyncTraceEnd(131072, SCREEN_OFF_BLOCKED_TRACE_NAME, 0);
        }
    }

    private boolean setScreenState(int state) {
        return setScreenState(state, false);
    }

    private boolean setScreenState(int state, boolean reportOnly) {
        boolean z = true;
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
            this.mWindowManagerPolicy.screenTurnedOff();
            setBrightnessAnimationTime(false, 500);
            setBrightnessNoLimit(-1, 500);
            this.mBrightnessModeChangeNoClearOffsetEnable = this.mPoweroffModeChangeAutoEnable;
            if (this.mPoweroffModeChangeAutoEnable) {
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
            } else {
                unblockScreenOn();
            }
            this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
        }
        if (this.mPendingScreenOnUnblocker != null) {
            z = false;
        }
        return z;
    }

    private void setReportedScreenState(int state) {
        Trace.traceCounter(131072, "ReportedScreenStateToPolicy", state);
        this.mReportedScreenStateToPolicy = state;
    }

    private boolean waitScreenBrightness(int displayState, boolean curReqWaitBright, boolean lastReqWaitBright, boolean enableBright, boolean skipWaitKeyguardDismiss) {
        if (DEBUG && DEBUG_Controller) {
            Slog.i(TAG, "waitScreenBrightness displayState = " + displayState + " curReqWaitBright = " + curReqWaitBright + " lastReqWaitBright = " + lastReqWaitBright + " enableBright = " + enableBright + " skipWaitKeyguardDismiss = " + skipWaitKeyguardDismiss);
        }
        boolean z = true;
        if (displayState == 2) {
            if (curReqWaitBright) {
                return true;
            }
            if (lastReqWaitBright && enableBright && !skipWaitKeyguardDismiss) {
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

    private int clampScreenBrightnessForVr(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessForVrRangeMinimum, this.mScreenBrightnessForVrRangeMaximum);
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void animateScreenBrightness(int target, int rate) {
        int brightnessTargetReal = target;
        if (!(this.mPowerRequest == null || this.mContext == null || this.mPowerRequest.useAutoBrightness || this.mLastBrightnessForAutoBrightnessDB == 0 || target != 0)) {
            int brightnessDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", this.mScreenBrightnessDefault, this.mCurrentUserId);
            if (brightnessDB != 0) {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
                if (DEBUG) {
                    Slog.i(TAG, "LabcCoverMode manualMode set screen_auto_brightness db=0 when poweroff,OrigbrightnessDB=" + brightnessDB);
                }
            }
        }
        this.mLastBrightnessForAutoBrightnessDB = target;
        if (!this.mPowerRequest.useAutoBrightness && brightnessTargetReal > 0) {
            this.mManualBrightnessController.updateManualBrightness(brightnessTargetReal);
            brightnessTargetReal = this.mManualBrightnessController.getManualBrightness();
            if (this.mManualBrightnessController.needFastestRateForManualBrightness()) {
                rate = 0;
            }
            this.mAutomaticBrightnessController.setBackSensorCoverModeBrightness(brightnessTargetReal);
        }
        if (DEBUG) {
            Slog.d(TAG, "Animating brightness: target=" + target + ", rate=" + rate + ",brightnessTargetReal=" + brightnessTargetReal + ",AutoBrightness=" + this.mPowerRequest.useAutoBrightness);
        }
        if (target >= 0 && brightnessTargetReal >= 0) {
            if (target == 0 && rate != 0) {
                rate = 0;
                Slog.e(TAG, "Animating brightness rate is invalid when screen off, set rate to 0");
            }
            if (this.mScreenBrightnessRampAnimator.animateTo(brightnessTargetReal, rate)) {
                Trace.traceCounter(131072, "TargetScreenBrightness", target);
                try {
                    if (this.mUsingHwSmartBackLightController && this.mSmartBackLightEnabled && rate > 0) {
                        if (this.mScreenBrightnessRampAnimator.isAnimating()) {
                            HwServiceFactory.IHwSmartBackLightController iHwSmartBackLightController = this.mHwSmartBackLightController;
                            HwServiceFactory.IHwSmartBackLightController iHwSmartBackLightController2 = this.mHwSmartBackLightController;
                            iHwSmartBackLightController.updateBrightnessState(0);
                        } else if (DEBUG) {
                            Slog.i(TAG, "brightness changed but not animating");
                        }
                    }
                    HwLog.dubaie("DUBAI_TAG_BRIGHTNESS", "brightness=" + brightnessTargetReal);
                    this.mBatteryStats.noteScreenBrightness(target);
                    sendTargetBrightnessToMonitor(target);
                } catch (RemoteException e) {
                }
            } else if (!this.mPowerRequest.useAutoBrightness && this.mLastBrightnessTarget != target) {
                Trace.traceCounter(131072, "TargetScreenBrightness", target);
                try {
                    this.mBatteryStats.noteScreenBrightness(target);
                } catch (RemoteException e2) {
                }
            }
            this.mLastBrightnessTarget = target;
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        int i = 2;
        if (this.mColorFadeEnabled && (this.mColorFadeOnAnimator.isStarted() || this.mColorFadeOffAnimator.isStarted())) {
            if (target == 2) {
                this.mPendingScreenOff = false;
            } else {
                return;
            }
        }
        if (this.mDisplayBlanksAfterDozeConfig && Display.isDozeState(this.mPowerState.getScreenState()) && !Display.isDozeState(target)) {
            this.mPowerState.prepareColorFade(this.mContext, this.mColorFadeFadesConfig ? 2 : 0);
            if (this.mColorFadeOffAnimator != null) {
                this.mColorFadeOffAnimator.end();
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
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
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
            if (!this.mColorFadeEnabled) {
                this.mPowerState.setColorFadeLevel(0.0f);
            }
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                setScreenState(1);
                this.mPendingScreenOff = false;
                this.mPowerState.dismissColorFadeResources();
            } else {
                if (performScreenOffTransition && !checkPhoneWindowIsTop()) {
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

    /* access modifiers changed from: private */
    public void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mProximitySensorEnabled && (this.mPendingProximity != 0 || positive)) {
            if (this.mPendingProximity != 1 || !positive) {
                HwServiceFactory.reportProximitySensorEventToIAware(positive);
                Slog.d(TAG, "UL_Power handleProximitySensorEvent positive:" + positive);
                this.mHandler.removeMessages(2);
                if (positive) {
                    this.mPendingProximity = 1;
                    setPendingProximityDebounceTime(0 + time);
                } else {
                    this.mPendingProximity = 0;
                    setPendingProximityDebounceTime(0 + time);
                }
                if (this.mHwDisplayPowerEx != null) {
                    this.mHwDisplayPowerEx.sendProximityBroadcast(positive);
                }
                debounceProximitySensor();
            }
        }
    }

    /* access modifiers changed from: private */
    public void debounceProximitySensor() {
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

    /* access modifiers changed from: private */
    public void handleSettingsChange(boolean userSwitch) {
        this.mPendingScreenBrightnessSetting = getScreenBrightnessSetting();
        if (userSwitch) {
            this.mCurrentScreenBrightnessSetting = this.mPendingScreenBrightnessSetting;
            if (this.mAutomaticBrightnessController != null) {
                this.mAutomaticBrightnessController.resetShortTermModel();
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
        if (this.mAutoBrightnessAdjustment == this.mPendingAutoBrightnessAdjustment) {
            this.mPendingAutoBrightnessAdjustment = Float.NaN;
            return false;
        }
        this.mAutoBrightnessAdjustment = this.mPendingAutoBrightnessAdjustment;
        this.mPendingAutoBrightnessAdjustment = Float.NaN;
        return true;
    }

    private boolean updateUserSetScreenBrightness() {
        if (this.mPendingScreenBrightnessSetting < 0) {
            return false;
        }
        if (this.mCurrentScreenBrightnessSetting == this.mPendingScreenBrightnessSetting) {
            this.mPendingScreenBrightnessSetting = -1;
            return false;
        }
        this.mCurrentScreenBrightnessSetting = this.mPendingScreenBrightnessSetting;
        this.mLastUserSetScreenBrightness = this.mPendingScreenBrightnessSetting;
        this.mPendingScreenBrightnessSetting = -1;
        return true;
    }

    private void notifyBrightnessChanged(int brightness, boolean userInitiated, boolean hadUserDataPoint) {
        float f;
        float brightnessInNits = convertToNits(brightness);
        if (this.mPowerRequest.useAutoBrightness && brightnessInNits >= 0.0f && this.mAutomaticBrightnessController != null) {
            if (this.mPowerRequest.lowPowerMode) {
                f = this.mPowerRequest.screenLowPowerBrightnessFactor;
            } else {
                f = 1.0f;
            }
            float powerFactor = f;
            this.mBrightnessTracker.notifyBrightnessChanged(brightnessInNits, userInitiated, powerFactor, hadUserDataPoint, this.mAutomaticBrightnessController.isDefaultConfig());
        }
    }

    private float convertToNits(int backlight) {
        if (this.mBrightnessMapper != null) {
            return this.mBrightnessMapper.convertToNits(backlight);
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
            public void run() {
                DisplayPowerController.this.dumpLocal(pw);
            }
        }, 1000);
    }

    /* access modifiers changed from: private */
    public void dumpLocal(PrintWriter pw) {
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
        pw.println("  mPendingScreenOffUnblocker=" + this.mPendingScreenOffUnblocker);
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
        if (this.mBrightnessTracker != null) {
            pw.println();
            this.mBrightnessTracker.dump(pw);
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

    private static String skipRampStateToString(int state) {
        switch (state) {
            case 0:
                return "RAMP_STATE_SKIP_NONE";
            case 1:
                return "RAMP_STATE_SKIP_INITIAL";
            case 2:
                return "RAMP_STATE_SKIP_AUTOBRIGHT";
            default:
                return Integer.toString(state);
        }
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, 255);
    }

    private static float clampAutoBrightnessAdjustment(float value) {
        return MathUtils.constrain(value, -1.0f, 1.0f);
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

    public void updateProximityState(boolean proximityState) {
        if (DEBUG) {
            Slog.d(TAG, "updateProximityState:" + proximityState);
        }
        this.mProximityPositive = proximityState;
        this.mScreenBrightnessRampAnimator.updateProximityState(proximityState);
    }

    public void handleProximitySensorEventEx(long time, boolean positive) {
        handleProximitySensorEvent(time, positive);
    }

    public void handlerSendTpKeepMsgEx() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9));
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
            int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mPowerRequest.userId);
            boolean autoPowerSavingUseManualAnimationTimeEnable = this.mAutomaticBrightnessController.getAutoPowerSavingUseManualAnimationTimeEnable();
            if (brightnessMode != 1 || !autoPowerSavingUseManualAnimationTimeEnable) {
                boolean manualPowerSavingAnimationEnable = this.mManualBrightnessController.getManualPowerSavingAnimationEnable();
                this.mScreenBrightnessRampAnimator.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable);
                if (manualPowerSavingAnimationEnable) {
                    this.mManualBrightnessController.setManualPowerSavingAnimationEnable(false);
                    return;
                }
                return;
            }
            boolean manualPowerSavingAnimationEnable2 = this.mAutomaticBrightnessController.getAutoPowerSavingAnimationEnable();
            this.mScreenBrightnessRampAnimator.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable2);
            if (manualPowerSavingAnimationEnable2) {
                this.mAutomaticBrightnessController.setAutoPowerSavingAnimationEnable(false);
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

    public void updateBrightnessModeAnimationEnable() {
        if (this.mManualBrightnessController != null && this.mScreenBrightnessRampAnimator != null && this.mManualBrightnessController.getBrightnessSetByAppEnable()) {
            this.mScreenBrightnessRampAnimator.updateBrightnessModeAnimationEnable(this.mManualBrightnessController.getBrightnessSetByAppAnimationEnable(), this.mSetBrightnessNoLimitAnimationTime);
        }
    }

    private void updateDarkAdaptDimmingEnable() {
        if (this.mAutomaticBrightnessController != null && this.mScreenBrightnessRampAnimator != null) {
            boolean darkAdaptDimmingEnable = this.mAutomaticBrightnessController.getDarkAdaptDimmingEnable();
            this.mScreenBrightnessRampAnimator.updateDarkAdaptAnimationDimmingEnable(darkAdaptDimmingEnable);
            if (darkAdaptDimmingEnable) {
                this.mAutomaticBrightnessController.clearDarkAdaptDimmingEnable();
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
            Slog.d(TAG, "set mPoweroffModeChangeAutoEnable=" + this.mPoweroffModeChangeAutoEnable + ",mNoClearOffsetEnable=" + this.mBrightnessModeChangeNoClearOffsetEnable);
        }
    }

    public void setBrightnessNoLimit(int brightness, int time) {
        this.mSetBrightnessNoLimitAnimationTime = time;
        this.mAutomaticBrightnessController.setBrightnessNoLimit(brightness, time);
        this.mManualBrightnessController.setBrightnessNoLimit(brightness, time);
    }

    public boolean hwBrightnessSetData(String name, Bundle data, int[] result) {
        boolean ret = this.mBackLight.hwBrightnessSetData(name, data, result);
        if (!ret) {
            return HwServiceFactory.hwBrightnessSetData(name, data, result);
        }
        return ret;
    }

    public boolean hwBrightnessGetData(String name, Bundle data, int[] result) {
        boolean ret = this.mBackLight.hwBrightnessGetData(name, data, result);
        if (!ret) {
            return HwServiceFactory.hwBrightnessGetData(name, data, result);
        }
        return ret;
    }

    public void updateBrightnessModeChangeManualState(boolean enable) {
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.updateBrightnessModeChangeManualState(enable);
        }
    }

    public void updateFastDarkenDimmingEnable() {
        if (this.mAutomaticBrightnessController != null && this.mScreenBrightnessRampAnimator != null) {
            this.mScreenBrightnessRampAnimator.updateFastDarkenDimmingEnable(this.mAutomaticBrightnessController.getFastDarkenDimmingEnable());
        }
    }
}
