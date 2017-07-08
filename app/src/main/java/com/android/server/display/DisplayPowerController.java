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
import android.provider.Settings.System;
import android.util.Flog;
import android.util.HwLog;
import android.util.Jlog;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import com.android.internal.app.IBatteryStats;
import com.android.server.FingerprintUnlockDataCollector;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHiACELightController;
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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_RAMP_RATE_FAST = 200;
    private static final int BRIGHTNESS_RAMP_RATE_SLOW = 40;
    private static final int COLOR_FADE_OFF_ANIMATION_DURATION_MILLIS = 150;
    private static final int COLOR_FADE_ON_ANIMATION_DURATION_MILLIS = 250;
    private static boolean DEBUG = false;
    private static boolean DEBUG_Controller = false;
    private static boolean DEBUG_FPLOG = false;
    private static final boolean DEBUG_PRETEND_PROXIMITY_SENSOR_ABSENT = false;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 1000;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_SCREEN_ON_FOR_KEYGUARD_DISMISS_DONE = 5;
    private static final int MSG_SCREEN_ON_UNBLOCKED = 3;
    private static final int MSG_UPDATE_POWER_STATE = 1;
    private static boolean NEED_NEW_BRIGHTNESS_PROCESS = false;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_SENSOR_NEGATIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_SENSOR_POSITIVE_DEBOUNCE_DELAY = 0;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final int REPORTED_TO_POLICY_SCREEN_OFF = 0;
    private static final int REPORTED_TO_POLICY_SCREEN_ON = 2;
    private static final int REPORTED_TO_POLICY_SCREEN_TURNING_ON = 1;
    private static final int SCREEN_DIM_MINIMUM_REDUCTION = 10;
    private static final String SCREEN_ON_BLOCKED_TRACE_NAME = "Screen on blocked";
    private static final String TAG = "DisplayPowerController";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final boolean USE_COLOR_FADE_ON_ANIMATION = false;
    private static final String USE_SENSORHUB_LABC_PROP = "use_sensorhub_labc";
    private static final boolean mSupportAod = false;
    private FingerprintUnlockDataCollector fpDataCollector;
    private final boolean mAllowAutoBrightnessWhileDozingConfig;
    private final AnimatorListener mAnimatorListener;
    private boolean mAppliedAutoBrightness;
    private boolean mAppliedDimming;
    private boolean mAppliedLowPower;
    private boolean mAutoBrightnessAdjustmentChanged;
    private boolean mAutoBrightnessEnabled;
    private Light mAutoCustomBackLight;
    private AutomaticBrightnessController mAutomaticBrightnessController;
    private Light mBackLight;
    private final IBatteryStats mBatteryStats;
    private final DisplayBlanker mBlanker;
    private boolean mBrightnessModeChanged;
    private final int mBrightnessRampRateFast;
    private final DisplayPowerCallbacks mCallbacks;
    private final Runnable mCleanListener;
    private boolean mColorFadeFadesConfig;
    private ObjectAnimator mColorFadeOffAnimator;
    private ObjectAnimator mColorFadeOnAnimator;
    private final Context mContext;
    private boolean mCoverModeAnimationFast;
    private int mCurrentUserId;
    private boolean mCurrentUserIdChange;
    private boolean mDisplayReadyLocked;
    private int mFeedBack;
    private final DisplayControllerHandler mHandler;
    private IHiACELightController mHiACELightController;
    private IHwSmartBackLightController mHwSmartBackLightController;
    private boolean mImmeBright;
    private boolean mIsCoverModeClosed;
    private boolean mLABCEnabled;
    private Sensor mLABCSensor;
    private boolean mLABCSensorEnabled;
    private final SensorEventListener mLABCSensorListener;
    private int mLastBacklight;
    private boolean mLastStatus;
    private boolean mLastWaitBrightnessMode;
    private final LightsManager mLights;
    private final Object mLock;
    private ManualBrightnessController mManualBrightnessController;
    private Light mManualCustomBackLight;
    private final Runnable mOnProximityNegativeRunnable;
    private final Runnable mOnProximityPositiveRunnable;
    private final Runnable mOnStateChangedRunnable;
    private int mPendingBacklight;
    private int mPendingProximity;
    private long mPendingProximityDebounceTime;
    private boolean mPendingRequestChangedLocked;
    private DisplayPowerRequest mPendingRequestLocked;
    private boolean mPendingScreenOff;
    private ScreenOnForKeyguardDismissUnblocker mPendingScreenOnForKeyguardDismissUnblocker;
    private ScreenOnUnblocker mPendingScreenOnUnblocker;
    private boolean mPendingUpdatePowerStateLocked;
    private boolean mPendingWaitForNegativeProximityLocked;
    private boolean mPowerPolicyChangeFromDimming;
    private DisplayPowerRequest mPowerRequest;
    private DisplayPowerState mPowerState;
    private int mProximity;
    private boolean mProximityPositive;
    private Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener;
    private float mProximityThreshold;
    private final Listener mRampAnimatorListener;
    private int mReportedScreenStateToPolicy;
    private boolean mSREEnabled;
    private final int mScreenBrightnessDarkConfig;
    private final int mScreenBrightnessDimConfig;
    private final int mScreenBrightnessDozeConfig;
    private RampAnimator<DisplayPowerState> mScreenBrightnessRampAnimator;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private boolean mScreenOffBecauseOfProximity;
    private long mScreenOnBlockStartRealTime;
    private long mScreenOnForKeyguardDismissBlockStartRealTime;
    private final SensorManager mSensorManager;
    private int mSetAutoBackLight;
    private boolean mSmartBackLightEnabled;
    private boolean mSmartBackLightSupported;
    private boolean mUnfinishedBusiness;
    private boolean mUseSensorHubLABC;
    private boolean mUseSoftwareAutoBrightnessConfig;
    private boolean mUsingBLC;
    private boolean mUsingHiACE;
    private boolean mUsingHwSmartBackLightController;
    private boolean mUsingSRE;
    private boolean mWaitingForNegativeProximity;
    private boolean mWakeupFromSleep;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private boolean mfastAnimtionFlag;

    /* renamed from: com.android.server.display.DisplayPowerController.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ PrintWriter val$pw;

        AnonymousClass9(PrintWriter val$pw) {
            this.val$pw = val$pw;
        }

        public void run() {
            DisplayPowerController.this.dumpLocal(this.val$pw);
        }
    }

    private final class DisplayControllerHandler extends Handler {
        public DisplayControllerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DisplayPowerController.REPORTED_TO_POLICY_SCREEN_TURNING_ON /*1*/:
                    DisplayPowerController.this.updatePowerState();
                case DisplayPowerController.REPORTED_TO_POLICY_SCREEN_ON /*2*/:
                    DisplayPowerController.this.debounceProximitySensor();
                case DisplayPowerController.MSG_SCREEN_ON_UNBLOCKED /*3*/:
                    if (DisplayPowerController.this.mPendingScreenOnUnblocker == msg.obj) {
                        DisplayPowerController.this.unblockScreenOn();
                        DisplayPowerController.this.updatePowerState();
                    }
                case DisplayPowerController.MSG_SCREEN_ON_FOR_KEYGUARD_DISMISS_DONE /*5*/:
                    if (DisplayPowerController.this.mPendingScreenOnForKeyguardDismissUnblocker == msg.obj) {
                        DisplayPowerController.this.mImmeBright = true;
                        DisplayPowerController.this.unblockScreenOnForKeyguardDismiss();
                        DisplayPowerController.this.updatePowerState();
                    }
                default:
            }
        }
    }

    private final class ScreenOnForKeyguardDismissUnblocker implements KeyguardDismissDoneListener {
        private ScreenOnForKeyguardDismissUnblocker() {
        }

        public void onKeyguardDismissDone() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(DisplayPowerController.MSG_SCREEN_ON_FOR_KEYGUARD_DISMISS_DONE, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    private final class ScreenOnUnblocker implements ScreenOnListener {
        private ScreenOnUnblocker() {
        }

        public void onScreenOn() {
            Message msg = DisplayPowerController.this.mHandler.obtainMessage(DisplayPowerController.MSG_SCREEN_ON_UNBLOCKED, this);
            msg.setAsynchronous(true);
            DisplayPowerController.this.mHandler.sendMessage(msg);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.DisplayPowerController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.DisplayPowerController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.DisplayPowerController.<clinit>():void");
    }

    private void setPowerStatus(boolean powerStatus) {
        if (this.mAutomaticBrightnessController != null) {
            this.mAutomaticBrightnessController.setPowerStatus(powerStatus);
        }
    }

    public void setBacklightBrightness(BacklightBrightness backlightBrightness) {
        this.mAutomaticBrightnessController.setBacklightBrightness(backlightBrightness);
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        this.mAutomaticBrightnessController.updateAutoBrightnessAdjustFactor(adjustFactor);
    }

    public int getMaxBrightnessForSeekbar() {
        return this.mManualBrightnessController.getMaxBrightnessForSeekbar();
    }

    public DisplayPowerController(Context context, DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager, DisplayBlanker blanker) {
        this.mBrightnessModeChanged = USE_COLOR_FADE_ON_ANIMATION;
        this.mIsCoverModeClosed = true;
        this.mLock = new Object();
        this.mPendingBacklight = PROXIMITY_UNKNOWN;
        this.mFeedBack = REPORTED_TO_POLICY_SCREEN_OFF;
        this.mLastBacklight = HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION;
        this.mSetAutoBackLight = PROXIMITY_UNKNOWN;
        this.mLastStatus = USE_COLOR_FADE_ON_ANIMATION;
        this.mAutoBrightnessAdjustmentChanged = USE_COLOR_FADE_ON_ANIMATION;
        this.mUseSensorHubLABC = USE_COLOR_FADE_ON_ANIMATION;
        this.mAutoBrightnessEnabled = USE_COLOR_FADE_ON_ANIMATION;
        this.mUsingHwSmartBackLightController = USE_COLOR_FADE_ON_ANIMATION;
        this.mUsingHiACE = USE_COLOR_FADE_ON_ANIMATION;
        this.mUsingBLC = USE_COLOR_FADE_ON_ANIMATION;
        this.mUsingSRE = USE_COLOR_FADE_ON_ANIMATION;
        this.mSREEnabled = USE_COLOR_FADE_ON_ANIMATION;
        this.mHiACELightController = null;
        this.mManualBrightnessController = null;
        this.mProximity = PROXIMITY_UNKNOWN;
        this.mProximityPositive = USE_COLOR_FADE_ON_ANIMATION;
        this.mPendingProximity = PROXIMITY_UNKNOWN;
        this.mPendingProximityDebounceTime = -1;
        this.mWakeupFromSleep = true;
        this.mCurrentUserId = REPORTED_TO_POLICY_SCREEN_OFF;
        this.mCurrentUserIdChange = USE_COLOR_FADE_ON_ANIMATION;
        this.mfastAnimtionFlag = USE_COLOR_FADE_ON_ANIMATION;
        this.mCoverModeAnimationFast = USE_COLOR_FADE_ON_ANIMATION;
        this.mAnimatorListener = new AnimatorListener() {
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
        this.mRampAnimatorListener = new Listener() {
            public void onAnimationEnd() {
                if (DisplayPowerController.this.mUsingHwSmartBackLightController && DisplayPowerController.this.mSmartBackLightEnabled) {
                    DisplayPowerController.this.mHwSmartBackLightController.updateBrightnessState(DisplayPowerController.REPORTED_TO_POLICY_SCREEN_TURNING_ON);
                }
                if (DisplayPowerController.this.mUsingSRE && DisplayPowerController.this.mSREEnabled) {
                    DisplayPowerController.this.mHiACELightController.updateBrightnessState(DisplayPowerController.REPORTED_TO_POLICY_SCREEN_TURNING_ON);
                }
                if (DisplayPowerController.this.mPowerPolicyChangeFromDimming) {
                    DisplayPowerController.this.mPowerPolicyChangeFromDimming = DisplayPowerController.USE_COLOR_FADE_ON_ANIMATION;
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
        this.mCleanListener = new Runnable() {
            public void run() {
                DisplayPowerController.this.sendUpdatePowerState();
            }
        };
        this.mLABCSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (DisplayPowerController.this.mLABCSensorEnabled && DisplayPowerController.this.mLABCEnabled) {
                    int Backlight = (int) event.values[DisplayPowerController.REPORTED_TO_POLICY_SCREEN_OFF];
                    int Ambientlight = (int) event.values[DisplayPowerController.REPORTED_TO_POLICY_SCREEN_TURNING_ON];
                    int FeedBack = (int) event.values[DisplayPowerController.REPORTED_TO_POLICY_SCREEN_ON];
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
        this.mOnStateChangedRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onStateChanged();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mOnProximityPositiveRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onProximityPositive();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mOnProximityNegativeRunnable = new Runnable() {
            public void run() {
                DisplayPowerController.this.mCallbacks.onProximityNegative();
                DisplayPowerController.this.mCallbacks.releaseSuspendBlocker();
            }
        };
        this.mProximitySensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (DisplayPowerController.this.mProximitySensorEnabled) {
                    long time = SystemClock.uptimeMillis();
                    float distance = event.values[DisplayPowerController.REPORTED_TO_POLICY_SCREEN_OFF];
                    boolean positive = (distance < 0.0f || distance >= DisplayPowerController.this.mProximityThreshold) ? DisplayPowerController.USE_COLOR_FADE_ON_ANIMATION : true;
                    DisplayPowerController.this.handleProximitySensorEvent(time, positive);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mHandler = new DisplayControllerHandler(handler.getLooper());
        this.mCallbacks = callbacks;
        this.mBatteryStats = BatteryStatsService.getService();
        this.mLights = (LightsManager) LocalServices.getService(LightsManager.class);
        this.mSensorManager = sensorManager;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mBlanker = blanker;
        this.mContext = context;
        this.mBackLight = this.mLights.getLight(REPORTED_TO_POLICY_SCREEN_OFF);
        NEED_NEW_BRIGHTNESS_PROCESS = this.mBackLight.isHighPrecision();
        Resources resources = context.getResources();
        int screenBrightnessSettingMinimum = clampAbsoluteBrightness(resources.getInteger(17694819));
        this.mScreenBrightnessDozeConfig = clampAbsoluteBrightness(resources.getInteger(17694822));
        this.mScreenBrightnessDimConfig = clampAbsoluteBrightness(resources.getInteger(17694827));
        this.mScreenBrightnessDarkConfig = clampAbsoluteBrightness(resources.getInteger(17694828));
        if (this.mScreenBrightnessDarkConfig > this.mScreenBrightnessDimConfig) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessDim (" + this.mScreenBrightnessDimConfig + ").");
        }
        if (this.mScreenBrightnessDarkConfig > this.mScreenBrightnessDimConfig) {
            Slog.w(TAG, "Expected config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") to be less than or equal to " + "config_screenBrightnessSettingMinimum (" + screenBrightnessSettingMinimum + ").");
        }
        int screenBrightnessRangeMinimum = Math.min(Math.min(screenBrightnessSettingMinimum, this.mScreenBrightnessDimConfig), this.mScreenBrightnessDarkConfig);
        this.mScreenBrightnessRangeMaximum = RampAnimator.DEFAULT_MAX_BRIGHTNESS;
        this.mUseSoftwareAutoBrightnessConfig = resources.getBoolean(17956900);
        this.mAllowAutoBrightnessWhileDozingConfig = resources.getBoolean(17956941);
        this.mUseSensorHubLABC = SystemProperties.getBoolean(USE_SENSORHUB_LABC_PROP, USE_COLOR_FADE_ON_ANIMATION);
        this.mBrightnessRampRateFast = resources.getInteger(17694923);
        int lightSensorRate = resources.getInteger(17694825);
        long brighteningLightDebounce = (long) resources.getInteger(17694823);
        long darkeningLightDebounce = (long) resources.getInteger(17694824);
        boolean autoBrightnessResetAmbientLuxAfterWarmUp = resources.getBoolean(17956942);
        int ambientLightHorizon = resources.getInteger(17694826);
        float autoBrightnessAdjustmentMaxGamma = resources.getFraction(18022401, REPORTED_TO_POLICY_SCREEN_TURNING_ON, REPORTED_TO_POLICY_SCREEN_TURNING_ON);
        if (this.mUseSoftwareAutoBrightnessConfig) {
            int[] lux = resources.getIntArray(17236008);
            int[] screenBrightness = resources.getIntArray(17236009);
            int lightSensorWarmUpTimeConfig = resources.getInteger(17694829);
            float dozeScaleFactor = resources.getFraction(18022402, REPORTED_TO_POLICY_SCREEN_TURNING_ON, REPORTED_TO_POLICY_SCREEN_TURNING_ON);
            if (!this.mUseSensorHubLABC) {
                Spline screenAutoBrightnessSpline = createAutoBrightnessSpline(lux, screenBrightness);
                if (screenAutoBrightnessSpline == null) {
                    Slog.e(TAG, "Error in config.xml.  config_autoBrightnessLcdBacklightValues (size " + screenBrightness.length + ") " + "must be monotic and have exactly one more entry than " + "config_autoBrightnessLevels (size " + lux.length + ") " + "which must be strictly increasing.  " + "Auto-brightness will be disabled.");
                    this.mUseSoftwareAutoBrightnessConfig = USE_COLOR_FADE_ON_ANIMATION;
                } else {
                    int bottom = clampAbsoluteBrightness(screenBrightness[REPORTED_TO_POLICY_SCREEN_OFF]);
                    if (this.mScreenBrightnessDarkConfig > bottom) {
                        Slog.w(TAG, "config_screenBrightnessDark (" + this.mScreenBrightnessDarkConfig + ") should be less than or equal to the first value of " + "config_autoBrightnessLcdBacklightValues (" + bottom + ").");
                    }
                    if (bottom < screenBrightnessRangeMinimum) {
                        screenBrightnessRangeMinimum = bottom;
                    }
                    IHwAutomaticBrightnessController iadm = HwServiceFactory.getHuaweiAutomaticBrightnessController();
                    if (iadm != null) {
                        this.mAutomaticBrightnessController = iadm.getInstance(this, handler.getLooper(), sensorManager, screenAutoBrightnessSpline, lightSensorWarmUpTimeConfig, screenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma, this.mContext);
                    } else {
                        this.mAutomaticBrightnessController = new AutomaticBrightnessController(this, handler.getLooper(), sensorManager, screenAutoBrightnessSpline, lightSensorWarmUpTimeConfig, screenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum, dozeScaleFactor, lightSensorRate, brighteningLightDebounce, darkeningLightDebounce, autoBrightnessResetAmbientLuxAfterWarmUp, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma);
                    }
                }
            }
            this.fpDataCollector = FingerprintUnlockDataCollector.getInstance();
        }
        this.mScreenBrightnessRangeMinimum = screenBrightnessRangeMinimum;
        this.mColorFadeFadesConfig = resources.getBoolean(17956905);
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            this.mProximityThreshold = Math.min(this.mProximitySensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
        }
        this.mHiACELightController = HwServiceFactory.getHiACELightController();
        if (this.mHiACELightController != null) {
            this.mHiACELightController.initialize();
            this.mUsingBLC = this.mHiACELightController.checkIfUsingBLC();
            this.mUsingSRE = this.mHiACELightController.checkIfUsingSRE();
            this.mUsingHiACE = !this.mUsingBLC ? this.mUsingSRE : true;
            if (this.mUsingHiACE && !this.mHiACELightController.startHiACELightController(this.mContext, this.mSensorManager)) {
                this.mUsingBLC = USE_COLOR_FADE_ON_ANIMATION;
                this.mUsingSRE = USE_COLOR_FADE_ON_ANIMATION;
                this.mUsingHiACE = USE_COLOR_FADE_ON_ANIMATION;
            }
        }
        int smartBackLightConfig = SystemProperties.getInt("ro.config.hw_smart_backlight", REPORTED_TO_POLICY_SCREEN_TURNING_ON);
        if (this.mUsingSRE || smartBackLightConfig == REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
            if (this.mUsingSRE) {
                Slog.i(TAG, "Use SRE instead of SBL");
            } else {
                this.mSmartBackLightSupported = true;
                if (DEBUG) {
                    Slog.i(TAG, "get ro.config.hw_smart_backlight = 1");
                }
            }
            int smartBackLightSetting = System.getInt(this.mContext.getContentResolver(), "smart_backlight_enable", PROXIMITY_UNKNOWN);
            if (smartBackLightSetting == PROXIMITY_UNKNOWN) {
                if (DEBUG) {
                    Slog.i(TAG, "get Settings.System.SMART_BACKLIGHT failed, set default value to 1");
                }
                System.putInt(this.mContext.getContentResolver(), "smart_backlight_enable", REPORTED_TO_POLICY_SCREEN_TURNING_ON);
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
            this.mLABCSensor = this.mSensorManager.getDefaultSensor(10007);
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
        return this.mProximitySensor != null ? true : USE_COLOR_FADE_ON_ANIMATION;
    }

    public boolean requestPowerState(DisplayPowerRequest request, boolean waitForNegativeProximity) {
        boolean z;
        if (DEBUG && DEBUG_Controller) {
            Slog.d(TAG, "requestPowerState: " + request + ", waitForNegativeProximity=" + waitForNegativeProximity);
        }
        synchronized (this.mLock) {
            boolean changed = USE_COLOR_FADE_ON_ANIMATION;
            if (waitForNegativeProximity) {
                if (!this.mPendingWaitForNegativeProximityLocked) {
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
                this.mDisplayReadyLocked = USE_COLOR_FADE_ON_ANIMATION;
            }
            if (changed && !this.mPendingRequestChangedLocked) {
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
            Message msg = this.mHandler.obtainMessage(REPORTED_TO_POLICY_SCREEN_TURNING_ON);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    private void initialize() {
        this.mPowerState = new DisplayPowerState(this.mContext, this.mBlanker, new ColorFade(REPORTED_TO_POLICY_SCREEN_OFF));
        this.mAutoCustomBackLight = this.mLights.getLight(9);
        this.mManualCustomBackLight = this.mLights.getLight(SCREEN_DIM_MINIMUM_REDUCTION);
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
            HwLog.bdate("BDAT_TAG_SCREEN_STATE", "state=" + this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenState(this.mPowerState.getScreenState());
            this.mBatteryStats.noteScreenBrightness(this.mPowerState.getScreenBrightness());
        } catch (RemoteException e) {
        }
    }

    private int getBrightness(boolean autoBrightnessAdjustmentChanged) {
        if (autoBrightnessAdjustmentChanged) {
            return this.mPendingBacklight;
        }
        return REPORTED_TO_POLICY_SCREEN_OFF;
    }

    private void updatePowerState() {
        boolean mustInitialize = USE_COLOR_FADE_ON_ANIMATION;
        boolean autoBrightnessAdjustmentChanged = USE_COLOR_FADE_ON_ANIMATION;
        synchronized (this.mLock) {
            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerController updatePowerState mPendingRequestLocked=" + this.mPendingRequestLocked);
            this.mPendingUpdatePowerStateLocked = USE_COLOR_FADE_ON_ANIMATION;
            if (this.mPendingRequestLocked == null) {
                return;
            }
            int state;
            boolean hbmEnable;
            boolean z;
            if (this.mPowerRequest == null) {
                this.mPowerRequest = new DisplayPowerRequest(this.mPendingRequestLocked);
                this.mWaitingForNegativeProximity = this.mPendingWaitForNegativeProximityLocked;
                this.mPendingWaitForNegativeProximityLocked = USE_COLOR_FADE_ON_ANIMATION;
                this.mPendingRequestChangedLocked = USE_COLOR_FADE_ON_ANIMATION;
                mustInitialize = true;
                if (this.mUseSensorHubLABC) {
                    this.mLastStatus = true;
                    autoBrightnessAdjustmentChanged = true;
                }
            } else if (this.mPendingRequestChangedLocked) {
                autoBrightnessAdjustmentChanged = this.mPowerRequest.screenAutoBrightnessAdjustment != this.mPendingRequestLocked.screenAutoBrightnessAdjustment ? true : USE_COLOR_FADE_ON_ANIMATION;
                boolean z2 = this.mPowerRequest.useAutoBrightness != this.mPendingRequestLocked.useAutoBrightness ? this.mPowerRequest.brightnessSetByUser == this.mPendingRequestLocked.brightnessSetByUser ? true : USE_COLOR_FADE_ON_ANIMATION : USE_COLOR_FADE_ON_ANIMATION;
                this.mBrightnessModeChanged = z2;
                if (this.mPowerRequest.policy == REPORTED_TO_POLICY_SCREEN_ON && this.mPendingRequestLocked.policy != REPORTED_TO_POLICY_SCREEN_ON) {
                    this.mPowerPolicyChangeFromDimming = true;
                }
                if (this.mCurrentUserId != this.mPendingRequestLocked.userId) {
                    this.mCurrentUserIdChange = true;
                    this.mCurrentUserId = this.mPendingRequestLocked.userId;
                    this.mBackLight.updateUserId(this.mCurrentUserId);
                }
                this.mPowerRequest.copyFrom(this.mPendingRequestLocked);
                boolean isClosed = HwServiceFactory.isCoverClosed();
                boolean isCoverModeChanged = USE_COLOR_FADE_ON_ANIMATION;
                if (isClosed != this.mIsCoverModeClosed) {
                    this.mIsCoverModeClosed = isClosed;
                    isCoverModeChanged = true;
                }
                updateCoverModeStatus(isClosed);
                if (!(!this.mBrightnessModeChanged || this.mCurrentUserIdChange || !this.mPowerRequest.useAutoBrightness || isCoverModeChanged || this.mIsCoverModeClosed)) {
                    updateAutoBrightnessAdjustFactor(0.0f);
                    if (DEBUG) {
                        Slog.d(TAG, "AdjustPositionBrightness set 0");
                    }
                }
                this.mCurrentUserIdChange = USE_COLOR_FADE_ON_ANIMATION;
                this.mWaitingForNegativeProximity |= this.mPendingWaitForNegativeProximityLocked;
                this.mPendingWaitForNegativeProximityLocked = USE_COLOR_FADE_ON_ANIMATION;
                this.mPendingRequestChangedLocked = USE_COLOR_FADE_ON_ANIMATION;
                this.mDisplayReadyLocked = USE_COLOR_FADE_ON_ANIMATION;
                writeAutoBrightnessDbEnable();
                if (this.mUseSensorHubLABC && !this.mLastStatus) {
                    autoBrightnessAdjustmentChanged = true;
                }
            }
            boolean mustNotify = this.mDisplayReadyLocked ? USE_COLOR_FADE_ON_ANIMATION : true;
            if (mustInitialize) {
                initialize();
            }
            int brightness = PROXIMITY_UNKNOWN;
            boolean performScreenOffTransition = USE_COLOR_FADE_ON_ANIMATION;
            switch (this.mPowerRequest.policy) {
                case REPORTED_TO_POLICY_SCREEN_OFF /*0*/:
                    state = REPORTED_TO_POLICY_SCREEN_TURNING_ON;
                    performScreenOffTransition = true;
                    break;
                case REPORTED_TO_POLICY_SCREEN_TURNING_ON /*1*/:
                    if (this.mPowerRequest.dozeScreenState != 0) {
                        state = this.mPowerRequest.dozeScreenState;
                    } else {
                        state = MSG_SCREEN_ON_UNBLOCKED;
                    }
                    if (!this.mAllowAutoBrightnessWhileDozingConfig) {
                        brightness = this.mPowerRequest.dozeScreenBrightness;
                        break;
                    }
                    break;
                default:
                    state = REPORTED_TO_POLICY_SCREEN_ON;
                    break;
            }
            if (!-assertionsDisabled) {
                Object obj;
                if (state != 0) {
                    obj = REPORTED_TO_POLICY_SCREEN_TURNING_ON;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            if (this.mProximitySensor != null) {
                if (this.mPowerRequest.useProximitySensor && state != REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                    setProximitySensorEnabled(true);
                    if (!this.mScreenOffBecauseOfProximity && this.mProximity == REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                        this.mScreenOffBecauseOfProximity = true;
                        sendOnProximityPositiveWithWakelock();
                    }
                } else if (this.mWaitingForNegativeProximity && this.mScreenOffBecauseOfProximity && this.mProximity == REPORTED_TO_POLICY_SCREEN_TURNING_ON && state != REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                    setProximitySensorEnabled(true);
                } else if (!this.mWaitingForNegativeProximity && this.mScreenOffBecauseOfProximity && this.mProximity != PROXIMITY_UNKNOWN && state == REPORTED_TO_POLICY_SCREEN_TURNING_ON && this.mPowerRequest.useProximitySensor) {
                    setProximitySensorEnabled(true);
                } else {
                    if (!this.mPowerRequest.useProximitySensor) {
                        setProximitySensorEnabled(USE_COLOR_FADE_ON_ANIMATION);
                    }
                    this.mWaitingForNegativeProximity = USE_COLOR_FADE_ON_ANIMATION;
                }
                if (!this.mScreenOffBecauseOfProximity && this.mProximity == REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                    this.mScreenOffBecauseOfProximity = true;
                    sendOnProximityPositiveWithWakelock();
                }
                if (this.mScreenOffBecauseOfProximity && this.mProximity != REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                    this.mScreenOffBecauseOfProximity = USE_COLOR_FADE_ON_ANIMATION;
                    sendOnProximityNegativeWithWakelock();
                }
            } else {
                this.mWaitingForNegativeProximity = USE_COLOR_FADE_ON_ANIMATION;
            }
            if (this.mScreenOffBecauseOfProximity && !this.mPowerRequest.useProximitySensorbyPhone) {
                state = REPORTED_TO_POLICY_SCREEN_TURNING_ON;
            }
            sre_init(state);
            hbm_init(state);
            animateScreenStateChange(state, performScreenOffTransition);
            state = this.mPowerState.getScreenState();
            if (state == REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                brightness = REPORTED_TO_POLICY_SCREEN_OFF;
                this.mWakeupFromSleep = true;
                this.mProximityPositive = USE_COLOR_FADE_ON_ANIMATION;
            }
            boolean autoBrightnessEnabled = USE_COLOR_FADE_ON_ANIMATION;
            this.mAutomaticBrightnessController.updateCurrentUserId(this.mPowerRequest.userId);
            float adjustment = System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, this.mPowerRequest.userId);
            if (!NEED_NEW_BRIGHTNESS_PROCESS) {
                adjustment = this.mPowerRequest.screenAutoBrightnessAdjustment;
            }
            boolean autoBrightnessEnabledInDoze;
            if (!this.mUseSensorHubLABC) {
                autoBrightnessEnabledInDoze = this.mAllowAutoBrightnessWhileDozingConfig ? (state == MSG_SCREEN_ON_UNBLOCKED || state == 4) ? true : USE_COLOR_FADE_ON_ANIMATION : USE_COLOR_FADE_ON_ANIMATION;
                autoBrightnessEnabled = (this.mPowerRequest.useAutoBrightness && (state == REPORTED_TO_POLICY_SCREEN_ON || autoBrightnessEnabledInDoze)) ? brightness < 0 ? true : USE_COLOR_FADE_ON_ANIMATION : USE_COLOR_FADE_ON_ANIMATION;
                this.mAutomaticBrightnessController.configure(autoBrightnessEnabled, adjustment, state != REPORTED_TO_POLICY_SCREEN_ON ? true : USE_COLOR_FADE_ON_ANIMATION, USE_COLOR_FADE_ON_ANIMATION, this.mPowerRequest.useTwilight);
                this.mAutomaticBrightnessController.updatePowerPolicy(this.mPowerRequest.policy);
                if (DEBUG && this.mAutoBrightnessEnabled != autoBrightnessEnabled) {
                    Slog.d(TAG, "mode change : autoBrightnessEnabled=" + autoBrightnessEnabled + ",adjustment=" + adjustment + ",state=" + state);
                    this.mAutoBrightnessEnabled = autoBrightnessEnabled;
                }
                if (NEED_NEW_BRIGHTNESS_PROCESS) {
                    if (this.mPowerRequest.screenAutoBrightness > 0) {
                        this.mAutomaticBrightnessController.updateIntervenedAutoBrightness(this.mPowerRequest.screenAutoBrightness);
                        this.mScreenBrightnessRampAnimator.mAutoBrightnessIntervened = true;
                    } else {
                        this.mScreenBrightnessRampAnimator.mAutoBrightnessIntervened = USE_COLOR_FADE_ON_ANIMATION;
                    }
                    this.mScreenBrightnessRampAnimator.mIsAutoBrightnessMode = autoBrightnessEnabled;
                    if (this.mBrightnessModeChanged) {
                        this.mScreenBrightnessRampAnimator.mIsFirstValidAutoBrightness = autoBrightnessEnabled;
                        this.mBrightnessModeChanged = USE_COLOR_FADE_ON_ANIMATION;
                    }
                    this.mScreenBrightnessRampAnimator.updateBrightnessRampPara(autoBrightnessEnabled, this.mAutomaticBrightnessController.getUpdateAutoBrightnessCount(), this.mPowerRequest.screenAutoBrightness > 0 ? true : USE_COLOR_FADE_ON_ANIMATION, this.mPowerRequest.policy);
                    this.mfastAnimtionFlag = this.mAutomaticBrightnessController.getPowerStatus();
                    this.mScreenBrightnessRampAnimator.updateFastAnimationFlag(this.mfastAnimtionFlag);
                    this.mCoverModeAnimationFast = this.mAutomaticBrightnessController.getCoverModeFastResponseFlag();
                    this.mScreenBrightnessRampAnimator.updateCoverModeFastAnimationFlag(this.mCoverModeAnimationFast);
                    this.mBackLight.updateBrightnessAdjustMode(autoBrightnessEnabled);
                }
            } else if (this.mLABCSensorEnabled) {
                autoBrightnessEnabledInDoze = this.mAllowAutoBrightnessWhileDozingConfig ? (state == MSG_SCREEN_ON_UNBLOCKED || state == 4) ? true : USE_COLOR_FADE_ON_ANIMATION : USE_COLOR_FADE_ON_ANIMATION;
                autoBrightnessEnabled = (this.mPowerRequest.useAutoBrightness && (state == REPORTED_TO_POLICY_SCREEN_ON || autoBrightnessEnabledInDoze)) ? brightness < 0 ? true : USE_COLOR_FADE_ON_ANIMATION : USE_COLOR_FADE_ON_ANIMATION;
                if (autoBrightnessEnabled && autoBrightnessAdjustmentChanged) {
                    this.mSetAutoBackLight = (int) (((this.mPowerRequest.screenAutoBrightnessAdjustment + 1.0f) / 2.0f) * 255.0f);
                    if (DEBUG) {
                        Slog.d(TAG, "[LABC]  A = " + this.mSetAutoBackLight);
                    }
                    this.mAutoCustomBackLight.sendCustomBackLight(this.mSetAutoBackLight);
                    this.mAutoBrightnessAdjustmentChanged = true;
                    this.mLastStatus = true;
                    return;
                } else if (!autoBrightnessEnabled && this.mLastStatus) {
                    if (DEBUG) {
                        Slog.d(TAG, "[LABC]  M = " + this.mPowerRequest.screenBrightness);
                    }
                    this.mManualCustomBackLight.sendCustomBackLight(this.mPowerRequest.screenBrightness);
                    this.mLastStatus = USE_COLOR_FADE_ON_ANIMATION;
                }
            }
            if (waitScreenBrightness(state, this.mPowerRequest.brightnessWaitMode, this.mLastWaitBrightnessMode, this.mPowerRequest.brightnessWaitRet)) {
                brightness = REPORTED_TO_POLICY_SCREEN_OFF;
                this.mWindowManagerPolicy.setInterceptInputForWaitBrightness(true);
            }
            if (this.mPowerRequest.boostScreenBrightness && brightness != 0) {
                brightness = RampAnimator.DEFAULT_MAX_BRIGHTNESS;
            }
            boolean slowChange = USE_COLOR_FADE_ON_ANIMATION;
            if (brightness < 0) {
                if (autoBrightnessEnabled) {
                    if (this.mUseSensorHubLABC) {
                        brightness = getBrightness(autoBrightnessEnabled);
                    } else {
                        brightness = this.mAutomaticBrightnessController.getAutomaticScreenBrightness();
                    }
                    if (brightness < 0 && SystemClock.uptimeMillis() - this.mAutomaticBrightnessController.getLightSensorEnableTime() > 195) {
                        if (brightness != BRIGHTNESS_FOR_PROXIMITY_POSITIVE) {
                            brightness = System.getInt(this.mContext.getContentResolver(), "screen_brightness", 100);
                        }
                        if (DEBUG) {
                            Slog.d(TAG, "failed to get auto brightness so set brightness based on SCREEN_BRIGHTNESS:" + brightness);
                        }
                    }
                }
                if (brightness >= 0) {
                    brightness = clampScreenBrightness(brightness);
                    if (this.mUseSensorHubLABC) {
                        if (this.mAppliedAutoBrightness && !this.mAutoBrightnessAdjustmentChanged) {
                            slowChange = true;
                        }
                    } else if (this.mAppliedAutoBrightness && !autoBrightnessAdjustmentChanged) {
                        slowChange = true;
                    }
                    this.mAppliedAutoBrightness = true;
                } else {
                    this.mAppliedAutoBrightness = brightness == BRIGHTNESS_FOR_PROXIMITY_POSITIVE ? true : USE_COLOR_FADE_ON_ANIMATION;
                }
            } else {
                this.mAppliedAutoBrightness = USE_COLOR_FADE_ON_ANIMATION;
            }
            this.mAutoBrightnessAdjustmentChanged = USE_COLOR_FADE_ON_ANIMATION;
            if (brightness < 0 && (state == MSG_SCREEN_ON_UNBLOCKED || state == 4)) {
                brightness = this.mScreenBrightnessDozeConfig;
            }
            int hbmModeConfig = SystemProperties.getInt("ro.config.hw_high_bright_mode", REPORTED_TO_POLICY_SCREEN_TURNING_ON);
            if (brightness >= 0 || this.mPowerRequest.useAutoBrightness) {
                hbmEnable = USE_COLOR_FADE_ON_ANIMATION;
            } else {
                hbmEnable = hbmModeConfig == REPORTED_TO_POLICY_SCREEN_TURNING_ON ? true : USE_COLOR_FADE_ON_ANIMATION;
            }
            if (hbmEnable) {
                brightness = clampScreenBrightness(this.mPowerRequest.screenBrightness);
                if (DEBUG) {
                    Slog.i(TAG, "HBM brightnessIn =" + brightness);
                }
                this.mManualBrightnessController.updateManualBrightness(brightness);
                brightness = this.mManualBrightnessController.getManualBrightness();
                if (DEBUG) {
                    Slog.i(TAG, "HBM brightnessOut =" + brightness);
                }
            }
            if (brightness < 0 && !this.mPowerRequest.useAutoBrightness) {
                brightness = clampScreenBrightness(this.mPowerRequest.screenBrightness);
                if (NEED_NEW_BRIGHTNESS_PROCESS) {
                    this.mAutomaticBrightnessController.updateIntervenedAutoBrightness(brightness);
                }
            }
            if (state == REPORTED_TO_POLICY_SCREEN_ON && this.mPowerRequest.policy == 0) {
                if (brightness > this.mScreenBrightnessRangeMinimum) {
                    brightness = Math.max(Math.min(brightness - 10, this.mScreenBrightnessDimConfig), this.mScreenBrightnessRangeMinimum);
                }
                Slog.i(TAG, "set brightness to DIM brightness:" + brightness);
            }
            if (this.mPowerRequest.policy == REPORTED_TO_POLICY_SCREEN_ON) {
                if (brightness > this.mScreenBrightnessRangeMinimum) {
                    brightness = Math.max(Math.min(brightness - 10, this.mScreenBrightnessDimConfig), this.mScreenBrightnessRangeMinimum);
                }
                if (!this.mAppliedDimming) {
                    slowChange = USE_COLOR_FADE_ON_ANIMATION;
                }
                this.mAppliedDimming = true;
            } else if (this.mAppliedDimming) {
                slowChange = USE_COLOR_FADE_ON_ANIMATION;
                this.mAppliedDimming = USE_COLOR_FADE_ON_ANIMATION;
            }
            if (this.mPowerRequest.lowPowerMode) {
                if (brightness > this.mScreenBrightnessRangeMinimum) {
                    brightness = Math.max(brightness / REPORTED_TO_POLICY_SCREEN_ON, this.mScreenBrightnessRangeMinimum);
                }
                if (!this.mAppliedLowPower) {
                    slowChange = USE_COLOR_FADE_ON_ANIMATION;
                }
                this.mAppliedLowPower = true;
            } else if (this.mAppliedLowPower) {
                slowChange = USE_COLOR_FADE_ON_ANIMATION;
                this.mAppliedLowPower = USE_COLOR_FADE_ON_ANIMATION;
            }
            if (!this.mPendingScreenOff) {
                if (state != REPORTED_TO_POLICY_SCREEN_ON && state != MSG_SCREEN_ON_UNBLOCKED) {
                    animateScreenBrightness(brightness, REPORTED_TO_POLICY_SCREEN_OFF);
                    if (NEED_NEW_BRIGHTNESS_PROCESS) {
                        this.mAutomaticBrightnessController.saveOffsetAlgorithmParas();
                    }
                } else if (state == REPORTED_TO_POLICY_SCREEN_ON && (this.mImmeBright || this.mWakeupFromSleep)) {
                    this.mImmeBright = USE_COLOR_FADE_ON_ANIMATION;
                    if (brightness > 0) {
                        this.mWakeupFromSleep = USE_COLOR_FADE_ON_ANIMATION;
                    }
                    animateScreenBrightness(brightness, REPORTED_TO_POLICY_SCREEN_OFF);
                } else if (this.mAutomaticBrightnessController.getSetbrightnessImmediateEnableForCaliTest()) {
                    animateScreenBrightness(brightness, REPORTED_TO_POLICY_SCREEN_OFF);
                } else {
                    animateScreenBrightness(brightness, slowChange ? BRIGHTNESS_RAMP_RATE_SLOW : BRIGHTNESS_RAMP_RATE_FAST);
                }
            }
            this.mLastWaitBrightnessMode = this.mPowerRequest.brightnessWaitMode;
            if (this.mPendingScreenOnUnblocker != null || this.mColorFadeOnAnimator.isStarted() || this.mColorFadeOffAnimator.isStarted()) {
                z = USE_COLOR_FADE_ON_ANIMATION;
            } else {
                z = this.mPowerState.waitUntilClean(this.mCleanListener);
            }
            if (this.mWindowManagerPolicy.getInterceptInputForWaitBrightness() && !this.mPowerRequest.brightnessWaitMode && this.mPendingScreenOnForKeyguardDismissUnblocker == null && z) {
                this.mWindowManagerPolicy.setInterceptInputForWaitBrightness(USE_COLOR_FADE_ON_ANIMATION);
            }
            boolean finished = z ? this.mScreenBrightnessRampAnimator.isAnimating() ? USE_COLOR_FADE_ON_ANIMATION : true : USE_COLOR_FADE_ON_ANIMATION;
            if (z && state != REPORTED_TO_POLICY_SCREEN_TURNING_ON && this.mReportedScreenStateToPolicy == REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                this.mReportedScreenStateToPolicy = REPORTED_TO_POLICY_SCREEN_ON;
                this.mWindowManagerPolicy.screenTurnedOn();
            }
            if (!(finished || this.mUnfinishedBusiness)) {
                if (DEBUG) {
                    Slog.d(TAG, "Unfinished business...");
                }
                this.mCallbacks.acquireSuspendBlocker();
                this.mUnfinishedBusiness = true;
            }
            if (z && mustNotify) {
                synchronized (this.mLock) {
                    if (!this.mPendingRequestChangedLocked) {
                        this.mDisplayReadyLocked = true;
                        if (DEBUG) {
                            Slog.d(TAG, "Display ready!");
                        }
                    }
                }
                sendOnStateChangedWithWakelock();
            }
            if (finished && this.mUnfinishedBusiness) {
                if (DEBUG) {
                    Slog.d(TAG, "Finished business...");
                }
                if (DEBUG_FPLOG && this.fpDataCollector != null && brightness > 0) {
                    this.fpDataCollector.reportScreenTurnedOn();
                }
                this.mUnfinishedBusiness = USE_COLOR_FADE_ON_ANIMATION;
                this.mCallbacks.releaseSuspendBlocker();
            }
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
            if (this.mUsingHiACE) {
                this.mSREEnabled = this.mPowerRequest.useSmartBacklight;
                this.mHiACELightController.updatePowerState(state, this.mSREEnabled);
            }
        } else if (this.mLABCSensor != null) {
            this.mLABCEnabled = true;
            setLABCEnabled(wantScreenOn(state));
        }
    }

    private void hbm_init(int state) {
        if (SystemProperties.getInt("ro.config.hw_high_bright_mode", REPORTED_TO_POLICY_SCREEN_TURNING_ON) == REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
            this.mManualBrightnessController.updatePowerState(state, this.mPowerRequest.useAutoBrightness ? USE_COLOR_FADE_ON_ANIMATION : true);
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
        }
    }

    private void unblockScreenOnForKeyguardDismiss() {
        if (this.mPendingScreenOnForKeyguardDismissUnblocker != null) {
            this.mPendingScreenOnForKeyguardDismissUnblocker = null;
            Slog.i(TAG, "Unblocked screen on for keyguard dismiss after " + (SystemClock.elapsedRealtime() - this.mScreenOnForKeyguardDismissBlockStartRealTime) + " ms");
        }
    }

    private void blockScreenOn() {
        if (this.mPendingScreenOnUnblocker == null) {
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_TRACE_NAME, REPORTED_TO_POLICY_SCREEN_OFF);
            this.mPendingScreenOnUnblocker = new ScreenOnUnblocker();
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
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_TRACE_NAME, REPORTED_TO_POLICY_SCREEN_OFF);
        }
    }

    private boolean setScreenState(int state) {
        if (this.mPowerState.getScreenState() != state) {
            if (this.mPowerState.getScreenState() != REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
            }
            this.mPowerState.setScreenState(state);
            try {
                HwLog.bdate("BDAT_TAG_SCREEN_STATE", "state=" + state);
                this.mBatteryStats.noteScreenState(state);
            } catch (RemoteException e) {
            }
        }
        boolean isOff = state == REPORTED_TO_POLICY_SCREEN_TURNING_ON ? true : USE_COLOR_FADE_ON_ANIMATION;
        boolean isDoze = (mSupportAod && (state == MSG_SCREEN_ON_UNBLOCKED || state == 4)) ? true : USE_COLOR_FADE_ON_ANIMATION;
        if (isOff && this.mReportedScreenStateToPolicy != 0 && !this.mScreenOffBecauseOfProximity) {
            this.mReportedScreenStateToPolicy = REPORTED_TO_POLICY_SCREEN_OFF;
            unblockScreenOn();
            this.mWindowManagerPolicy.screenTurnedOff();
            setPowerStatus(USE_COLOR_FADE_ON_ANIMATION);
        } else if (!(isOff || isDoze || this.mReportedScreenStateToPolicy != 0)) {
            this.mReportedScreenStateToPolicy = REPORTED_TO_POLICY_SCREEN_TURNING_ON;
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                blockScreenOn();
            } else {
                unblockScreenOn();
            }
            this.mWindowManagerPolicy.screenTurningOn(this.mPendingScreenOnUnblocker);
            setPowerStatus(true);
        }
        if (this.mPendingScreenOnUnblocker == null) {
            return true;
        }
        return USE_COLOR_FADE_ON_ANIMATION;
    }

    private boolean waitScreenBrightness(int displayState, boolean curReqWaitBright, boolean lastReqWaitBright, boolean dismiss) {
        boolean z = true;
        if (DEBUG && DEBUG_Controller) {
            Slog.i(TAG, "waitScreenBrightness displayState = " + displayState + " curReqWaitBright = " + curReqWaitBright + " lastReqWaitBright = " + lastReqWaitBright + " dismiss = " + dismiss);
        }
        if (displayState == REPORTED_TO_POLICY_SCREEN_ON) {
            if (curReqWaitBright) {
                return true;
            }
            if (lastReqWaitBright && dismiss) {
                blockScreenOnForKeyguardDismiss();
                this.mWindowManagerPolicy.waitKeyguardDismissDone(this.mPendingScreenOnForKeyguardDismissUnblocker);
            }
        } else if (this.mPendingScreenOnForKeyguardDismissUnblocker != null) {
            unblockScreenOnForKeyguardDismiss();
            this.mWindowManagerPolicy.cancelWaitKeyguardDismissDone();
        }
        if (this.mPendingScreenOnForKeyguardDismissUnblocker == null) {
            z = USE_COLOR_FADE_ON_ANIMATION;
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
                rate = REPORTED_TO_POLICY_SCREEN_OFF;
                Slog.e(TAG, "Animating brightness rate is invalid when screen off, set rate to 0");
            }
            if (this.mScreenBrightnessRampAnimator.animateTo(target, rate)) {
                try {
                    if (this.mUsingHwSmartBackLightController && this.mSmartBackLightEnabled && rate > 0) {
                        if (this.mScreenBrightnessRampAnimator.isAnimating()) {
                            this.mHwSmartBackLightController.updateBrightnessState(REPORTED_TO_POLICY_SCREEN_OFF);
                        } else if (DEBUG) {
                            Slog.i(TAG, "brightness changed but not animating");
                        }
                    }
                    if (this.mUsingSRE && this.mSREEnabled && rate > 0 && this.mScreenBrightnessRampAnimator.isAnimating()) {
                        this.mHiACELightController.updateBrightnessState(REPORTED_TO_POLICY_SCREEN_OFF);
                    }
                    HwLog.bdate("BDAT_TAG_BRIGHTNESS", "brightness=" + target);
                    this.mBatteryStats.noteScreenBrightness(target);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void animateScreenStateChange(int target, boolean performScreenOffTransition) {
        int i = REPORTED_TO_POLICY_SCREEN_ON;
        if (this.mColorFadeOnAnimator.isStarted() || this.mColorFadeOffAnimator.isStarted()) {
            if (target == REPORTED_TO_POLICY_SCREEN_ON) {
                this.mPendingScreenOff = USE_COLOR_FADE_ON_ANIMATION;
            } else {
                return;
            }
        }
        if (this.mPendingScreenOff && target != REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
            setScreenState(REPORTED_TO_POLICY_SCREEN_TURNING_ON);
            this.mPendingScreenOff = USE_COLOR_FADE_ON_ANIMATION;
            this.mPowerState.dismissColorFadeResources();
        }
        if (target == REPORTED_TO_POLICY_SCREEN_ON) {
            if (setScreenState(REPORTED_TO_POLICY_SCREEN_ON)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target == MSG_SCREEN_ON_UNBLOCKED) {
            if (!(this.mScreenBrightnessRampAnimator.isAnimating() && this.mPowerState.getScreenState() == REPORTED_TO_POLICY_SCREEN_ON) && setScreenState(MSG_SCREEN_ON_UNBLOCKED)) {
                this.mPowerState.setColorFadeLevel(1.0f);
                this.mPowerState.dismissColorFade();
            }
        } else if (target != 4) {
            this.mPendingScreenOff = true;
            if (this.mPowerState.getColorFadeLevel() == 0.0f) {
                setScreenState(REPORTED_TO_POLICY_SCREEN_TURNING_ON);
                this.mPendingScreenOff = USE_COLOR_FADE_ON_ANIMATION;
                this.mPowerState.dismissColorFadeResources();
            } else {
                if (performScreenOffTransition) {
                    DisplayPowerState displayPowerState = this.mPowerState;
                    Context context = this.mContext;
                    if (!this.mColorFadeFadesConfig) {
                        i = REPORTED_TO_POLICY_SCREEN_TURNING_ON;
                    }
                    if (!(!displayPowerState.prepareColorFade(context, i) || this.mPowerState.getScreenState() == REPORTED_TO_POLICY_SCREEN_TURNING_ON || checkPhoneWindowIsTop())) {
                        this.mColorFadeOffAnimator.start();
                    }
                }
                this.mColorFadeOffAnimator.end();
            }
        } else if (!this.mScreenBrightnessRampAnimator.isAnimating() || this.mPowerState.getScreenState() == 4) {
            if (this.mPowerState.getScreenState() != 4) {
                if (setScreenState(MSG_SCREEN_ON_UNBLOCKED)) {
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
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, REPORTED_TO_POLICY_SCREEN_TURNING_ON, this.mHandler);
            }
        } else if (this.mProximitySensorEnabled) {
            this.mProximitySensorEnabled = USE_COLOR_FADE_ON_ANIMATION;
            this.mProximity = PROXIMITY_UNKNOWN;
            this.mPendingProximity = PROXIMITY_UNKNOWN;
            this.mHandler.removeMessages(REPORTED_TO_POLICY_SCREEN_ON);
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            clearPendingProximityDebounceTime();
        }
    }

    private void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mProximitySensorEnabled && (this.mPendingProximity != 0 || positive)) {
            if (this.mPendingProximity != REPORTED_TO_POLICY_SCREEN_TURNING_ON || !positive) {
                Slog.d(TAG, "handleProximitySensorEvent positive:" + positive);
                this.mHandler.removeMessages(REPORTED_TO_POLICY_SCREEN_ON);
                if (positive) {
                    this.mPendingProximity = REPORTED_TO_POLICY_SCREEN_TURNING_ON;
                    setPendingProximityDebounceTime(time + 0);
                } else {
                    this.mPendingProximity = REPORTED_TO_POLICY_SCREEN_OFF;
                    setPendingProximityDebounceTime(time + 0);
                }
                debounceProximitySensor();
            }
        }
    }

    private void debounceProximitySensor() {
        if (this.mProximitySensorEnabled && this.mPendingProximity != PROXIMITY_UNKNOWN && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                updatePowerState();
                clearPendingProximityDebounceTime();
                return;
            }
            Message msg = this.mHandler.obtainMessage(REPORTED_TO_POLICY_SCREEN_ON);
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
            this.mLABCSensorEnabled = USE_COLOR_FADE_ON_ANIMATION;
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

    public void dump(PrintWriter pw) {
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
        this.mHandler.runWithScissors(new AnonymousClass9(pw), 1000);
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
            case PROXIMITY_UNKNOWN /*-1*/:
                return "Unknown";
            case REPORTED_TO_POLICY_SCREEN_OFF /*0*/:
                return "Negative";
            case REPORTED_TO_POLICY_SCREEN_TURNING_ON /*1*/:
                return "Positive";
            default:
                return Integer.toString(state);
        }
    }

    private static String reportedToPolicyToString(int state) {
        switch (state) {
            case REPORTED_TO_POLICY_SCREEN_OFF /*0*/:
                return "REPORTED_TO_POLICY_SCREEN_OFF";
            case REPORTED_TO_POLICY_SCREEN_TURNING_ON /*1*/:
                return "REPORTED_TO_POLICY_SCREEN_TURNING_ON";
            case REPORTED_TO_POLICY_SCREEN_ON /*2*/:
                return "REPORTED_TO_POLICY_SCREEN_ON";
            default:
                return Integer.toString(state);
        }
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case REPORTED_TO_POLICY_SCREEN_ON /*2*/:
            case MSG_SCREEN_ON_UNBLOCKED /*3*/:
                return true;
            default:
                return USE_COLOR_FADE_ON_ANIMATION;
        }
    }

    private static boolean isScreenOn(int state) {
        return state != REPORTED_TO_POLICY_SCREEN_TURNING_ON ? true : USE_COLOR_FADE_ON_ANIMATION;
    }

    private static Spline createAutoBrightnessSpline(int[] lux, int[] brightness) {
        if (brightness.length == 0) {
            Slog.e(TAG, "brightness length is 0");
            return null;
        }
        try {
            int n = brightness.length;
            float[] x = new float[n];
            float[] y = new float[n];
            y[REPORTED_TO_POLICY_SCREEN_OFF] = normalizeAbsoluteBrightness(brightness[REPORTED_TO_POLICY_SCREEN_OFF]);
            for (int i = REPORTED_TO_POLICY_SCREEN_TURNING_ON; i < n; i += REPORTED_TO_POLICY_SCREEN_TURNING_ON) {
                x[i] = (float) lux[i + PROXIMITY_UNKNOWN];
                y[i] = normalizeAbsoluteBrightness(brightness[i]);
            }
            Spline spline = Spline.createSpline(x, y);
            if (DEBUG) {
                Slog.d(TAG, "Auto-brightness spline: " + spline);
                for (float v = 1.0f; v < ((float) lux[lux.length + PROXIMITY_UNKNOWN]) * 1.25f; v *= 1.25f) {
                    String str = TAG;
                    Object[] objArr = new Object[REPORTED_TO_POLICY_SCREEN_ON];
                    objArr[REPORTED_TO_POLICY_SCREEN_OFF] = Float.valueOf(v);
                    objArr[REPORTED_TO_POLICY_SCREEN_TURNING_ON] = Float.valueOf(spline.interpolate(v));
                    Slog.d(str, String.format("  %7.1f: %7.1f", objArr));
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
        return MathUtils.constrain(value, REPORTED_TO_POLICY_SCREEN_OFF, RampAnimator.DEFAULT_MAX_BRIGHTNESS);
    }

    private boolean checkPhoneWindowIsTop() {
        String incalluiPackageName = "com.android.incallui";
        String incalluiClassName = "com.android.incallui.InCallActivity";
        List<RunningTaskInfo> tasksInfo = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(REPORTED_TO_POLICY_SCREEN_TURNING_ON);
        if (tasksInfo != null && tasksInfo.size() > 0) {
            ComponentName cn = ((RunningTaskInfo) tasksInfo.get(REPORTED_TO_POLICY_SCREEN_OFF)).topActivity;
            Slog.i(TAG, "checkPhoneWindowIsTop:pakcage name:" + cn.getPackageName() + ",ClassName name:" + cn.getClassName());
            return (incalluiPackageName.equals(cn.getPackageName()) && incalluiClassName.equals(cn.getClassName())) ? true : USE_COLOR_FADE_ON_ANIMATION;
        }
    }

    private void writeAutoBrightnessDbEnable() {
        if (NEED_NEW_BRIGHTNESS_PROCESS) {
            if (this.mPowerRequest.policy == REPORTED_TO_POLICY_SCREEN_ON) {
                this.mBackLight.writeAutoBrightnessDbEnable(USE_COLOR_FADE_ON_ANIMATION);
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
}
