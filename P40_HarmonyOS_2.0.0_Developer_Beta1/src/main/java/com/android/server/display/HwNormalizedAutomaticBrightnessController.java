package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.BrightnessConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.HwNormalizedSpline;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.view.Display;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.DarkAdaptDetector;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.HwBrightnessBatteryDetection;
import com.android.server.display.HwBrightnessPgSceneDetection;
import com.android.server.display.HwBrightnessSceneController;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.display.HwProximitySensorDetector;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.huawei.android.os.HwPowerManager;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.display.DefaultHwEyeProtectionController;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

public class HwNormalizedAutomaticBrightnessController extends AutomaticBrightnessController implements HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks, HwBrightnessSceneController.Callbacks, HwProximitySensorDetector.Callbacks, HwBrightnessBatteryDetection.Callbacks {
    private static final int AMBIENT_BUFFER_INIT_NUM = 10;
    private static final int AMBIENT_BUFFER_TRACE_INIT_NUM = 50;
    private static final int AMBIENT_LIGHT_MONITOR_SAMPLING_INTERVAL_MS = 2000;
    private static final int BACK_SENSOR_COVER_MODE_BRIGHTNESS = -3;
    private static final float BRIGHTNESS_ADJUSTMENT_RATIO = 2.0f;
    private static final int BRIGHTNESS_DELAY_TIME = 5;
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_FOR_SENSOR_NOT_READY_WHEN_WAKEUP = -1;
    private static final float BRIGHTNESS_MAX_ADJUSTMENT = 1.0f;
    private static final int BRIGHTNESS_OFF_VALUE = 0;
    private static final int COVER_MODE_DEFAULT_RESPONSE_TIME = 2500;
    private static final int DC_MODE_ON_NUM = 1;
    private static final int DEFAULT_INIT_BRIGHTNESS = -1;
    private static final int DEFAULT_INIT_SCENE_LEVEL = -1;
    private static final int DEFAULT_INIT_SENSOR_RATE = -1;
    private static final int DEFAULT_INIT_SENSOR_VALUE = -1;
    private static final long DEFAULT_INIT_SYSTEM_TIME = -1;
    private static final int DEFAULT_VALUE = 0;
    private static final float DEFAUL_OFFSET_LUX = -1.0f;
    private static final int DEFAUL_SENSOR_RATE = 300;
    private static final int DEFAUL_SENSOR_REGISTER_RATE = 1000;
    private static final int DRAG_NO_VALID_BRIGHTNESS = -3;
    private static final int DUAL_SENSOR_BACK_LUX_INDEX = 5;
    private static final int DUAL_SENSOR_FRONT_LUX_INDEX = 4;
    private static final int DUAL_SENSOR_FUSED_LUX_INDEX = 0;
    private static final int DUAL_SENSOR_MAX_INDEX = 6;
    private static final int DUAL_SENSOR_SENSOR_TIME_INDEX = 3;
    private static final int DUAL_SENSOR_SYSTEM_TIME_INDEX = 2;
    private static final int ENABLE_LIGHT_SENSOR_TIME_OUT = 200;
    private static final int EYE_PROTECTION_MODE_CONFIGURE_NUM = 7;
    private static final String FRONT_CAMERA = "1";
    private static final int GAME_MODE_ENTER = 21;
    private static final int GAME_MODE_QUIT = 20;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String HW_CUSTOMIZATION_SCREEN_BRIGHTNESS_MODE = "hw_customization_screen_brightness_mode";
    private static final int INIT_VALUE = -1;
    private static final String KEY_DC_BRIGHTNESS_DIMMING_SWITCH = "hw_dc_brightness_dimming_switch";
    private static final String KEY_READING_MODE_SWITCH = "hw_reading_mode_display_switch";
    private static final int LOG_POWER_ON_MS = 2000;
    private static final int LUX_FACTOR = 10;
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final long MINUTE_IN_MILLIS = 60000;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final int MODE_DEFAULT = 0;
    private static final int MODE_LUX_MIN_MAX = 2;
    private static final int MODE_TOP_GAME = 1;
    private static final int MSG_CLOSE_PROXIMITY = 5;
    private static final int MSG_COVER_MODE_DEBOUNCED = 2;
    private static final int MSG_FRONT_CAMERA_UPDATE_BRIGHTNESS = 3;
    private static final int MSG_FRONT_CAMERA_UPDATE_DIMMING_ENABLE = 4;
    private static final int MSG_OPEN_PROXIMITY = 6;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final int MSG_UPDATE_BRIGHTNESS_DELAY = 7;
    private static final int NORMALIZED_MAX_DEFAULT_BRIGHTNESS = 10000;
    private static final int NO_MAPPING_BRIGHTNESS = -1;
    private static final int PROXIMITY_EVENT_DISTANCE_INDEX = 0;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final float PROXIMITY_POSITIVE_DISTANCE = 5.0f;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final int READING_MODE_ON_NUM = 1;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "HwNormalizedAutomaticBrightnessController";
    private static final int TIME_PRINT_SENSOR_VALUE_INTERVAL = 4000;
    private static final int TIME_SENSOR_REPORT_NONE_VALUE = 400;
    private static int sDeviceActualBrightnessLevel = 0;
    private static int sDeviceActualBrightnessNit = 0;
    private static int sDeviceStandardBrightnessNit = 0;
    private static HwNormalizedSpline sHwNormalizedScreenAutoBrightnessSpline;
    private static Light sLight;
    private float mAmbientLuxForFrontCamera;
    private float mAmbientLuxLast;
    private float mAmbientLuxLastLast;
    private float mAmbientLuxOffset = DEFAUL_OFFSET_LUX;
    private float mAmbientLuxTmp;
    private float mAutoBrightnessOut;
    private final HandlerThread mAutoBrightnessProcessThread;
    private HwBrightnessBatteryDetection mBatteryStateReceiver;
    private int mBrightnessOutForLog = -1;
    private CameraManager.AvailabilityCallback mCameraAvailableCallback;
    private CameraManager mCameraManager;
    private final Context mContext;
    private int mCoverModeFastResponseTimeDelay = 2500;
    private boolean mCoverStateFast = false;
    private CryogenicPowerProcessor mCryogenicProcessor;
    private String mCurCameraId;
    private int mCurrentDarkMode;
    private int mCurrentDisplayMode;
    private int mCurrentUserId = 0;
    private DarkAdaptDetector mDarkAdaptDetector;
    private DarkAdaptDetector.AdaptState mDarkAdaptState;
    private final HwBrightnessXmlLoader.Data mData;
    private float mDefaultBrightness = DEFAUL_OFFSET_LUX;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private int mDualSensorRawAmbient = -1;
    private int mFrontCameraMaxBrightness;
    private HwRingBuffer mHwAmbientLightRingBuffer = new HwRingBuffer(10);
    private HwRingBuffer mHwAmbientLightRingBufferTrace = new HwRingBuffer(50);
    private HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private HwBrightnessMapping mHwBrightnessMapping;
    private HwBrightnessPgSceneDetection mHwBrightnessPgSceneDetection;
    private HwBrightnessPowerSavingCurve mHwBrightnessPowerSavingCurve;
    private HwBrightnessSceneController mHwBrightnessSceneController;
    private HwDualSensorEventListenerImpl mHwDualSensorEventListenerImpl;
    private DefaultHwEyeProtectionController mHwEyeProtectionController;
    private HwHumanFactorBrightness mHwHumanFactorBrightness;
    private int mHwLastReportedSensorValue = -1;
    private long mHwLastReportedSensorValueTime = DEFAULT_INIT_SYSTEM_TIME;
    private int mHwLastSensorValue = -1;
    private final HwNormalizedAutomaticBrightnessHandler mHwNormalizedAutomaticBrightnessHandler;
    private long mHwPrintLogTime = DEFAULT_INIT_SYSTEM_TIME;
    private HwProximitySensorDetector mHwProximitySensorDetector;
    private int mHwRateMillis = 300;
    private boolean mIsAnimationGameChangeEnable;
    private boolean mIsAutoPowerSavingAnimationEnable;
    private boolean mIsAutoPowerSavingBrighnessLineDisableForDemo;
    private boolean mIsBrightnessLimitedByThermal;
    private boolean mIsCallDeInQrCodeScene;
    private boolean mIsCameraModeChangeAnimationEnable = false;
    private boolean mIsCameraModeEnable = false;
    private boolean mIsClosed = false;
    private boolean mIsCurrentUserChanging = false;
    private boolean mIsDarkAdaptDimmingEnable;
    private boolean mIsDarkenAmbientEnable;
    private volatile boolean mIsDragFinished;
    private boolean mIsFrontCameraAppKeepBrightnessEnable;
    private boolean mIsFrontCameraDimmingEnable;
    private boolean mIsGameDisableAutoBrightnessModeKeepOffsetEnable;
    private boolean mIsGameModeBrightnessOffsetEnable;
    private boolean mIsHwReportValueWhenSensorOnChange = true;
    private boolean mIsIntervenedAutoBrightnessEnable = false;
    private boolean mIsLastDozingSensorEnable;
    private boolean mIsLastGameDisableAutoBrightnessModeEnable;
    private boolean mIsLocked;
    private boolean mIsNightUpTimeEnable;
    private boolean mIsPolicyChangeFromDim = false;
    private boolean mIsPowerOnEnable;
    private boolean mIsPowerOnStatus = false;
    private boolean mIsReadingModeChangeAnimationEnable = false;
    private boolean mIsScreenStatus = false;
    private boolean mIsWakeupCoverBrightnessEnable;
    private long mLastAmbientLightToMonitorTime;
    private float mLastAmbientLuxForFrontCamera;
    private int mLastDefaultBrightness;
    private int mLowBatteryMaxBrightness;
    private int mLuxMaxBrightness;
    private int mLuxMinBrightness;
    private DisplayEngineManager mManager;
    private Runnable mMaxBrightnessFromCryogenicDelayedRunnable;
    private Handler mMaxBrightnessFromCryogenicHandler;
    private int mPowerOnLuxAbandonCount = 0;
    private int mPowerOnLuxCount = 0;
    private int mPowerPolicy = 0;
    private String mPowerStateNameForMonitor;
    private int mProximity = -1;
    private int mResetAmbientLuxDisableBrightnessOffset;
    private int mScreenBrightnesOut;
    private volatile int mScreenBrightnessBeforeAdj;
    private ScreenStateReceiver mScreenStateReceiver;
    private SensorObserver mSensorObserver;
    private int mSensorOption = -1;
    private TouchProximityDetector mTouchProximityDetector;

    static {
        boolean z = true;
        if (!Log.HWLog && (!Log.HWModuleLog || !Log.isLoggable(TAG, 3))) {
            z = false;
        }
        HWDEBUG = z;
        loadDeviceBrightness();
        if (HWFLOW) {
            Slog.i(TAG, "DeviceActualLevel=" + sDeviceActualBrightnessLevel + ",DeviceActualBrightnessNit=" + sDeviceActualBrightnessNit + ",DeviceStandardBrightnessNit=" + sDeviceStandardBrightnessNit);
        }
    }

    public HwNormalizedAutomaticBrightnessController(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor lightSensor, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, HysteresisLevels ambientBrightnessThresholds, HysteresisLevels screenBrightnessThresholds, long shortTermModelTimeout, PackageManager packageManager, Context context) {
        super(callbacks, looper, sensorManager, lightSensor, mapper, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, ambientBrightnessThresholds, screenBrightnessThresholds, shortTermModelTimeout, packageManager);
        Spline spline = null;
        this.mScreenStateReceiver = null;
        this.mScreenBrightnessBeforeAdj = -1;
        this.mIsDragFinished = true;
        this.mIsGameModeBrightnessOffsetEnable = false;
        this.mIsAnimationGameChangeEnable = false;
        this.mIsAutoPowerSavingBrighnessLineDisableForDemo = false;
        this.mResetAmbientLuxDisableBrightnessOffset = 0;
        this.mCurrentDisplayMode = 0;
        this.mCurrentDarkMode = 16;
        this.mLowBatteryMaxBrightness = 255;
        this.mLuxMinBrightness = 4;
        this.mLuxMaxBrightness = 255;
        this.mIsAutoPowerSavingAnimationEnable = false;
        this.mIsWakeupCoverBrightnessEnable = false;
        this.mIsPowerOnEnable = false;
        this.mIsLastGameDisableAutoBrightnessModeEnable = false;
        this.mIsGameDisableAutoBrightnessModeKeepOffsetEnable = false;
        this.mIsNightUpTimeEnable = false;
        this.mIsDarkenAmbientEnable = false;
        this.mCurCameraId = null;
        this.mFrontCameraMaxBrightness = 255;
        this.mLastAmbientLuxForFrontCamera = 0.0f;
        this.mAmbientLuxForFrontCamera = 0.0f;
        this.mIsLastDozingSensorEnable = false;
        this.mIsFrontCameraDimmingEnable = false;
        this.mCameraAvailableCallback = new CameraManager.AvailabilityCallback() {
            /* class com.android.server.display.HwNormalizedAutomaticBrightnessController.AnonymousClass1 */

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraAvailable(String cameraId) {
                if ("1".equals(cameraId)) {
                    if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                        Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "onCameraAvailable mCurCameraId=" + HwNormalizedAutomaticBrightnessController.this.mCurCameraId + ",-->null");
                    }
                    HwNormalizedAutomaticBrightnessController.this.mCurCameraId = null;
                    HwNormalizedAutomaticBrightnessController.this.updateFrontCameraMaxBrightness();
                }
            }

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraUnavailable(String cameraId) {
                if ("1".equals(cameraId)) {
                    if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                        Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "onCameraAvailable mCurCameraId=" + HwNormalizedAutomaticBrightnessController.this.mCurCameraId + "->cameraId=" + cameraId);
                    }
                    HwNormalizedAutomaticBrightnessController.this.mCurCameraId = cameraId;
                    HwNormalizedAutomaticBrightnessController.this.updateFrontCameraMaxBrightness();
                }
            }
        };
        this.mHwAmbientLuxFilterAlgo = new HwAmbientLuxFilterAlgo(lightSensorRate);
        Optional<HwNormalizedSpline> hwSpline = createHwNormalizedAutoBrightnessSpline(context);
        this.mScreenAutoBrightnessSpline = hwSpline.isPresent() ? (Spline) hwSpline.get() : spline;
        this.mAutoBrightnessProcessThread = new HandlerThread(TAG);
        this.mAutoBrightnessProcessThread.start();
        this.mHwNormalizedAutomaticBrightnessHandler = new HwNormalizedAutomaticBrightnessHandler(this.mAutoBrightnessProcessThread.getLooper());
        this.mIsHwReportValueWhenSensorOnChange = this.mHwAmbientLuxFilterAlgo.reportValueWhenSensorOnChange();
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_EYE_PROTECTION_PART_FACTORY_IMPL).getHwEyeProtectionController(context, this);
        }
        this.mData = HwBrightnessXmlLoader.getData();
        this.mIsFingerprintOffUnlockEnable = this.mData.fingerprintOffUnlockEnable;
        this.mContext = context;
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        sendXmlConfigToMonitor();
        this.mHwBrightnessPgSceneDetection = new HwBrightnessPgSceneDetection(this, this.mData.pgSceneDetectionDarkenDelayTime, this.mData.pgSceneDetectionBrightenDelayTime, this.mContext);
        this.mHwDualSensorEventListenerImpl = HwDualSensorEventListenerImpl.getInstance(sensorManager, this.mContext);
        this.mSensorOption = this.mHwDualSensorEventListenerImpl.getModuleSensorOption(TAG);
        if (this.mData.darkAdapterEnable) {
            this.mDarkAdaptDetector = new DarkAdaptDetector(this.mData);
        }
        this.mHwBrightnessMapping = new HwBrightnessMapping(this.mData.brightnessMappingPoints);
        if (this.mData.pgReregisterScene) {
            this.mScreenStateReceiver = new ScreenStateReceiver();
        }
        this.mHwBrightnessPowerSavingCurve = new HwBrightnessPowerSavingCurve(this.mData.manualBrightnessMaxLimit, this.mData.screenBrightnessMinNit, this.mData.screenBrightnessMaxNit);
        initOptionalFunction(this.mContext);
        this.mManager = new DisplayEngineManager();
    }

    private void initOptionalFunction(Context content) {
        if (this.mData.cryogenicEnable) {
            this.mMaxBrightnessFromCryogenicHandler = new Handler();
            this.mMaxBrightnessFromCryogenicDelayedRunnable = new Runnable() {
                /* class com.android.server.display.HwNormalizedAutomaticBrightnessController.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    HwNormalizedAutomaticBrightnessController.this.setMaxBrightnessFromCryogenicDelayed();
                }
            };
        }
        if (this.mData.touchProximityEnable) {
            this.mTouchProximityDetector = new TouchProximityDetector(this.mData);
        }
        if (this.mData.autoPowerSavingBrighnessLineDisableForDemo) {
            this.mIsAutoPowerSavingBrighnessLineDisableForDemo = isDemoVersion();
        }
        if (this.mData.batteryModeEnable || this.mData.powerSavingModeBatteryLowLevelEnable) {
            this.mHwNormalizedAutomaticBrightnessHandler.post(new Runnable() {
                /* class com.android.server.display.$$Lambda$HwNormalizedAutomaticBrightnessController$jnNtc4iA0p3fDF_dij6zJ_THjoc */

                @Override // java.lang.Runnable
                public final void run() {
                    HwNormalizedAutomaticBrightnessController.this.lambda$initOptionalFunction$0$HwNormalizedAutomaticBrightnessController();
                }
            });
        }
        this.mHwHumanFactorBrightness = new HwHumanFactorBrightness();
        this.mHwBrightnessSceneController = new HwBrightnessSceneController(this, this.mContext, this.mHwAmbientLuxFilterAlgo, sHwNormalizedScreenAutoBrightnessSpline, this.mHwHumanFactorBrightness);
        this.mHwProximitySensorDetector = new HwProximitySensorDetector(this, this.mSensorManager, this.mHwAmbientLuxFilterAlgo);
        initBrightnessParaForSpline();
    }

    public /* synthetic */ void lambda$initOptionalFunction$0$HwNormalizedAutomaticBrightnessController() {
        this.mBatteryStateReceiver = new HwBrightnessBatteryDetection(this, this.mContext);
    }

    private void initBrightnessParaForSpline() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.initBrightnessParaForSpline();
        }
    }

    private static void loadDeviceBrightness() {
        LightsManager lightsManager = (LightsManager) LocalServices.getService(LightsManager.class);
        if (lightsManager == null) {
            Slog.e(TAG, "loadDeviceBrightness() get LightsManager failed");
            return;
        }
        Light lcdLight = lightsManager.getLight(0);
        if (lcdLight == null) {
            Slog.e(TAG, "loadDeviceBrightness() get Light failed");
            return;
        }
        sDeviceActualBrightnessLevel = lcdLight.getDeviceActualBrightnessLevel();
        sDeviceActualBrightnessNit = lcdLight.getDeviceActualBrightnessNit();
        sDeviceStandardBrightnessNit = lcdLight.getDeviceStandardBrightnessNit();
    }

    private static Optional<HwNormalizedSpline> createHwNormalizedAutoBrightnessSpline(Context context) {
        try {
            sHwNormalizedScreenAutoBrightnessSpline = HwNormalizedSpline.createHwNormalizedSpline(context, sDeviceActualBrightnessLevel, sDeviceActualBrightnessNit, sDeviceStandardBrightnessNit);
            return Optional.of(sHwNormalizedScreenAutoBrightnessSpline);
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return Optional.empty();
        }
    }

    @Override // com.android.server.display.HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks
    public void updateStateRecognition(boolean isPowerSavingCurveEnable, int appType) {
        if (this.mLightSensorEnabled && !this.mIsAutoPowerSavingBrighnessLineDisableForDemo) {
            if (sHwNormalizedScreenAutoBrightnessSpline == null) {
                Slog.w(TAG, "sHwNormalizedScreenAutoBrightnessSpline is null, no orig powerSaving");
                return;
            }
            HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
            if (hwBrightnessSceneController != null && hwBrightnessSceneController.mGameLevel == 21 && sHwNormalizedScreenAutoBrightnessSpline.isPowerSavingBrightnessLineEnable()) {
                Slog.i(TAG, "GameBrightMode no orig powerSaving");
            }
            if (this.mData.autoPowerSavingUseManualAnimationTimeEnable) {
                this.mIsAutoPowerSavingAnimationEnable = sHwNormalizedScreenAutoBrightnessSpline.isPowerSavingModeBrightnessChangeEnable(this.mAmbientLux, isPowerSavingCurveEnable);
            }
            sHwNormalizedScreenAutoBrightnessSpline.setPowerSavingModeEnable(isPowerSavingCurveEnable);
            HwBrightnessPowerSavingCurve hwBrightnessPowerSavingCurve = this.mHwBrightnessPowerSavingCurve;
            if (hwBrightnessPowerSavingCurve != null) {
                hwBrightnessPowerSavingCurve.setPowerSavingEnable(isPowerSavingCurveEnable);
            }
            updateAutoBrightness(true, false);
        }
    }

    private static boolean isDemoVersion() {
        String vendor2 = SystemProperties.get("ro.hw.vendor", "");
        String country = SystemProperties.get("ro.hw.country", "");
        Slog.i(TAG, "vendor:" + vendor2 + ",country:" + country);
        return "demo".equalsIgnoreCase(vendor2) || "demo".equalsIgnoreCase(country);
    }

    public boolean getAnimationGameChangeEnable() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            this.mIsAnimationGameChangeEnable = hwBrightnessSceneController.mIsAnimationGameChangeEnable;
        }
        boolean isAnimationEnable = this.mIsAnimationGameChangeEnable && this.mData.gameModeEnable;
        if (!this.mHwAmbientLuxFilterAlgo.getProximityPositiveEnable()) {
            this.mIsAnimationGameChangeEnable = false;
            HwBrightnessSceneController hwBrightnessSceneController2 = this.mHwBrightnessSceneController;
            if (hwBrightnessSceneController2 != null) {
                hwBrightnessSceneController2.mIsAnimationGameChangeEnable = false;
            }
        }
        if (HWFLOW && isAnimationEnable != this.mIsAnimationGameChangeEnable) {
            Slog.i(TAG, "GameBrightMode set dimming isAnimationEnable=" + this.mIsAnimationGameChangeEnable);
        }
        return isAnimationEnable;
    }

    public boolean getGameModeEnable() {
        return this.mData.gameModeEnable;
    }

    public boolean getAutoPowerSavingUseManualAnimationTimeEnable() {
        return this.mData.autoPowerSavingUseManualAnimationTimeEnable;
    }

    public boolean getAutoPowerSavingAnimationEnable() {
        return this.mIsAutoPowerSavingAnimationEnable;
    }

    public void setAutoPowerSavingAnimationEnable(boolean isAutoPowerSavingAnimationEnable) {
        this.mIsAutoPowerSavingAnimationEnable = isAutoPowerSavingAnimationEnable;
    }

    public void configure(boolean enable, BrightnessConfiguration configuration, float brightness, boolean userChangedBrightness, float adjustment, boolean userChangedAutoBrightnessAdjustment, int displayPolicy) {
        boolean z = false;
        boolean isDozing = displayPolicy == 1;
        if (this.mLightSensorEnabled && (!enable || isDozing)) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mHwAmbientLuxFilterAlgo.clear();
            if (!this.mIsHwReportValueWhenSensorOnChange) {
                clearSensorData();
            }
            this.mLastAmbientLightToMonitorTime = 0;
            DarkAdaptDetector darkAdaptDetector = this.mDarkAdaptDetector;
            if (darkAdaptDetector != null) {
                darkAdaptDetector.setAutoModeOff();
                this.mIsDarkAdaptDimmingEnable = false;
            }
        }
        if (!enable || isDozing) {
            this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
        }
        if (this.mData.foldScreenModeEnable) {
            setFoldDisplayModeEnable(enable && !isDozing);
        }
        if (this.mIsLastDozingSensorEnable != enable && this.mData.fingerprintOffUnlockEnable) {
            this.mIsLastDozingSensorEnable = enable;
            if (enable) {
                this.mIsWaitFristAutoBrightness = true;
            }
        }
        HwNormalizedAutomaticBrightnessController.super.configure(enable, configuration, brightness, userChangedBrightness, adjustment, userChangedAutoBrightnessAdjustment, displayPolicy);
        if (this.mLightSensorEnabled && this.mHwPrintLogTime == DEFAULT_INIT_SYSTEM_TIME) {
            this.mHwPrintLogTime = this.mLightSensorEnableTime;
        }
        if (enable && !isDozing && !this.mHwBrightnessPgSceneDetection.getPgRecognitionListenerRegisted()) {
            this.mHwBrightnessPgSceneDetection.registerPgRecognitionListener(this.mContext);
            if (HWFLOW) {
                Slog.i(TAG, "PowerSaving auto in registerPgBLightSceneChangedListener=" + this.mHwBrightnessPgSceneDetection.getPgRecognitionListenerRegisted());
            }
        }
        updateProximitySensorEnabled(enable, isDozing);
        if (enable && !isDozing) {
            z = true;
        }
        setTouchProximityEnabled(z);
        updateContentObserver();
        initCameraManager();
    }

    private void initCameraManager() {
        if (this.mData.frontCameraMaxBrightnessEnable && this.mCameraManager == null) {
            this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
            this.mCameraManager.registerAvailabilityCallback(this.mCameraAvailableCallback, (Handler) null);
            Slog.i(TAG, "registerAvailabilityCallback for auto frontCameraMaxBrightness");
        }
    }

    private void setTouchProximityEnabled(boolean isTouchProximityEnabled) {
        TouchProximityDetector touchProximityDetector = this.mTouchProximityDetector;
        if (touchProximityDetector == null) {
            return;
        }
        if (isTouchProximityEnabled) {
            touchProximityDetector.enable();
        } else {
            touchProximityDetector.disable();
        }
    }

    private void updateContentObserver() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.updateContentObserver();
        }
    }

    public int getAutomaticScreenBrightness() {
        int i;
        int i2;
        int i3;
        if (this.mWakeupFromSleep && SystemClock.uptimeMillis() - this.mLightSensorEnableTime < 200) {
            if (HWFLOW) {
                Slog.i(TAG, "mWakeupFromSleep= " + this.mWakeupFromSleep + ",currentTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableTime=" + this.mLightSensorEnableTime);
            }
            this.mScreenBrightnesOut = -1;
        } else if (needToSetBrightnessBaseIntervened()) {
            this.mScreenBrightnesOut = HwNormalizedAutomaticBrightnessController.super.getAutomaticScreenBrightness();
        } else if (needToSetBrightnessBaseProximity()) {
            this.mScreenBrightnesOut = -2;
        } else {
            this.mScreenBrightnesOut = HwNormalizedAutomaticBrightnessController.super.getAutomaticScreenBrightness();
        }
        if (this.mScreenBrightnesOut > this.mLowBatteryMaxBrightness && this.mData.batteryModeEnable) {
            if (HWFLOW) {
                Slog.i(TAG, "mScreenBrightnesOut = " + this.mScreenBrightnesOut + ", mLowBatteryMaxBrightness = " + this.mLowBatteryMaxBrightness);
            }
            this.mScreenBrightnesOut = this.mLowBatteryMaxBrightness;
        }
        if (!this.mIsIntervenedAutoBrightnessEnable && this.mData.luxMinMaxBrightnessEnable && (i2 = this.mScreenBrightnesOut) < (i3 = this.mLuxMinBrightness) && i2 >= 4) {
            this.mScreenBrightnesOut = i3;
        }
        if (this.mData.frontCameraMaxBrightnessEnable && (i = this.mScreenBrightnesOut) > this.mFrontCameraMaxBrightness && i > 0 && !this.mIsLocked && !this.mIsPowerOnStatus) {
            if (HWDEBUG) {
                Slog.i(TAG, "mScreenBrightnesOut = " + this.mScreenBrightnesOut + "--> mFrontCameraMaxBrightness= " + this.mFrontCameraMaxBrightness);
            }
            this.mScreenBrightnesOut = this.mFrontCameraMaxBrightness;
        }
        return this.mScreenBrightnesOut;
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            HwNormalizedAutomaticBrightnessController.this.mContext.registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.e(HwNormalizedAutomaticBrightnessController.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "BroadcastReceiver.onReceive() action:" + action);
            if ("android.intent.action.BOOT_COMPLETED".equals(action) && !HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.getPgRecognitionListenerRegisted()) {
                HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.registerPgRecognitionListener(HwNormalizedAutomaticBrightnessController.this.mContext);
                if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                    Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "BOOT_COMPLETED: auto in registerPgBLightSceneChangedListener,=" + HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.getPgRecognitionListenerRegisted());
                }
            }
        }
    }

    private boolean needToSetBrightnessBaseIntervened() {
        return this.mIsIntervenedAutoBrightnessEnable && (this.mData.allowLabcUseProximity || this.mData.proximitySceneModeEnable);
    }

    public int getAutoBrightnessBaseInOutDoorLimit(int brightness) {
        int tmpBrightnessOut;
        if (getHdrModeEnable()) {
            return brightness;
        }
        if ((this.mData.isAutoModeSeekBarMaxBrightnessBasedLux && this.mAmbientLux > this.mData.autoModeSeekBarMaxBrightnessLuxTh) || this.mAmbientLux >= ((float) this.mData.outDoorThreshold) || !this.mData.autoModeInOutDoorLimitEnble) {
            return brightness;
        }
        if (brightness < this.mData.manualBrightnessMaxLimit) {
            tmpBrightnessOut = brightness;
        } else {
            tmpBrightnessOut = this.mData.manualBrightnessMaxLimit;
        }
        return tmpBrightnessOut;
    }

    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
        if (sHwNormalizedScreenAutoBrightnessSpline == null) {
            Slog.i(TAG, "setPersonalizedBrightnessCurveLevel failed! curveLevel=" + curveLevel + ",sHwNormalizedScreenAutoBrightnessSpline=null");
            return;
        }
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.setPersonalizedBrightnessCurveLevel(curveLevel);
        }
    }

    public void updateNewBrightnessCurveTmp() {
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.updateNewBrightnessCurveTmp();
            sendPersonalizedCurveAndParamToMonitor(sHwNormalizedScreenAutoBrightnessSpline.getPersonalizedDefaultCurve(), sHwNormalizedScreenAutoBrightnessSpline.getPersonalizedAlgoParam());
            return;
        }
        Slog.e(TAG, "NewCurveMode updateNewBrightnessCurveTmp fail,mSpline==null");
    }

    public void updateNewBrightnessCurve() {
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.updateNewBrightnessCurve();
        } else {
            Slog.e(TAG, "NewCurveMode updateNewBrightnessCurve fail,sHwNormalizedScreenAutoBrightnessSpline==null");
        }
    }

    public List<PointF> getCurrentDefaultNewCurveLine() {
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline != null) {
            return hwNormalizedSpline.getCurrentDefaultNewCurveLine();
        }
        return null;
    }

    public void updateIntervenedAutoBrightness(int brightness) {
        this.mAutoBrightnessOut = (float) brightness;
        this.mIsIntervenedAutoBrightnessEnable = true;
        if (this.mData.cryogenicEnable) {
            this.mMaxBrightnessSetByCryogenicBypass = true;
        }
        if (sHwNormalizedScreenAutoBrightnessSpline != null) {
            if (!this.mData.manualMode || getHdrModeEnable()) {
                HwNormalizedAutomaticBrightnessController.super.updateIntervenedAutoBrightness((int) this.mAutoBrightnessOut);
                return;
            }
            if (this.mIsDragFinished) {
                this.mScreenBrightnessBeforeAdj = getAutomaticScreenBrightness();
                this.mIsDragFinished = false;
            }
            this.mDefaultBrightness = sHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
            float lux = sHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForBrightness();
            if (HWFLOW) {
                Slog.i(TAG, "HwAutoBrightnessIn=" + brightness + ",defaultBrightness=" + this.mDefaultBrightness + ",lux=" + lux);
            }
            this.mAutoBrightnessOut = getBrightnessFromLimit(this.mAutoBrightnessOut, lux);
            if (this.mData.frontCameraMaxBrightnessEnable) {
                float f = this.mAutoBrightnessOut;
                int i = this.mFrontCameraMaxBrightness;
                if (f >= ((float) i)) {
                    this.mAutoBrightnessOut = (float) i;
                }
            }
            if (HWFLOW) {
                Slog.i(TAG, "HwAutoBrightnessOut=" + this.mAutoBrightnessOut);
            }
            HwNormalizedAutomaticBrightnessController.super.updateIntervenedAutoBrightness((int) this.mAutoBrightnessOut);
        }
    }

    private float getBrightnessFromLimit(float brightness, float lux) {
        if (this.mData.isAutoModeSeekBarMaxBrightnessBasedLux && lux > this.mData.autoModeSeekBarMaxBrightnessLuxTh) {
            if (HWFLOW) {
                Slog.i(TAG, "isAutoModeSeekBarMaxBrightnessBasedLux,brightness=" + brightness);
            }
            return brightness;
        } else if (brightness < ((float) this.mData.manualBrightnessMaxLimit)) {
            return brightness;
        } else {
            if (lux > ((float) this.mData.outDoorThreshold)) {
                int autoBrightnessOutTmp = ((int) brightness) < this.mData.manualBrightnessMaxLimit ? (int) brightness : this.mData.manualBrightnessMaxLimit;
                float f = this.mDefaultBrightness;
                return autoBrightnessOutTmp > ((int) f) ? (float) autoBrightnessOutTmp : (float) ((int) f);
            }
            return brightness < ((float) this.mData.manualBrightnessMaxLimit) ? brightness : (float) this.mData.manualBrightnessMaxLimit;
        }
    }

    /* JADX INFO: Multiple debug info for r5v2 int: [D('luxIndex' int), D('average' int)] */
    private int getSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            long time = SystemClock.uptimeMillis();
            int bufferSize = this.mHwAmbientLightRingBuffer.size();
            if (bufferSize <= 0) {
                return time - this.mHwLastReportedSensorValueTime < 400 ? this.mHwLastSensorValue : this.mHwLastReportedSensorValue;
            }
            int sum = 0;
            for (int luxIndex = bufferSize - 1; luxIndex >= 0; luxIndex--) {
                sum = (int) (((float) sum) + this.mHwAmbientLightRingBuffer.getLux(luxIndex));
            }
            int average = sum / bufferSize;
            if (average >= 0) {
                this.mHwLastSensorValue = average;
            }
            this.mHwAmbientLightRingBuffer.clear();
            if (time - this.mHwPrintLogTime > 4000) {
                int bufferSize2 = this.mHwAmbientLightRingBufferTrace.size();
                if (HWFLOW) {
                    Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(bufferSize2));
                }
                this.mHwAmbientLightRingBufferTrace.clear();
                this.mHwPrintLogTime = time;
            }
            return this.mHwLastSensorValue;
        }
    }

    private void clearSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.clear();
            int bufferSize = this.mHwAmbientLightRingBufferTrace.size();
            if (HWFLOW) {
                Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(bufferSize));
            }
            this.mHwAmbientLightRingBufferTrace.clear();
            this.mHwLastReportedSensorValueTime = DEFAULT_INIT_SYSTEM_TIME;
            this.mHwLastReportedSensorValue = -1;
            this.mHwLastSensorValue = -1;
            this.mHwPrintLogTime = DEFAULT_INIT_SYSTEM_TIME;
        }
    }

    private float getTouchProximityProcessedLux(boolean isFirstLux, float lux) {
        String str;
        float luxOut = lux;
        TouchProximityDetector touchProximityDetector = this.mTouchProximityDetector;
        if (touchProximityDetector == null) {
            return lux;
        }
        if (!isFirstLux) {
            boolean isCurrentLuxValid = touchProximityDetector.isCurrentLuxValid();
            if (bypassTouchProximityResult()) {
                isCurrentLuxValid = true;
            }
            boolean isNeedUseLastLux = true;
            updateTouchProximityState(!isCurrentLuxValid);
            float lastLux = this.mAmbientLuxLastLast;
            float f = this.mAmbientLuxLast;
            if (lastLux <= f) {
                lastLux = f;
            }
            if (isCurrentLuxValid || luxOut >= lastLux) {
                isNeedUseLastLux = false;
            }
            if (HWDEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("TouchProximityDetector isValid=");
                sb.append(isCurrentLuxValid);
                sb.append(", lux=");
                sb.append(luxOut);
                if (isNeedUseLastLux) {
                    str = "->" + lastLux;
                } else {
                    str = "";
                }
                sb.append(str);
                Slog.d(TAG, sb.toString());
            }
            if (isNeedUseLastLux) {
                luxOut = lastLux;
            }
        }
        this.mTouchProximityDetector.startNextLux();
        this.mAmbientLuxLastLast = isFirstLux ? luxOut : this.mAmbientLuxLast;
        this.mAmbientLuxLast = luxOut;
        return luxOut;
    }

    private void reportLightSensorEventToAlgo(long time, float lux) {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        float luxOut = getTouchProximityProcessedLux(!this.mAmbientLuxValid, lux);
        updateFirstAmbientLuxPara(luxOut);
        this.mHwAmbientLuxFilterAlgo.handleLightSensorEvent(time, luxOut);
        this.mAmbientLuxTmp = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        if (!isProximityDynamicEnable()) {
            this.mAmbientLux = this.mAmbientLuxTmp;
        }
        boolean isDarkAdaptStateChanged = handleDarkAdaptDetector(luxOut);
        if (this.mData.frontCameraMaxBrightnessEnable) {
            this.mAmbientLuxForFrontCamera = this.mHwAmbientLuxFilterAlgo.getAmbientLuxForFrontCamera();
            if ((this.mLastAmbientLuxForFrontCamera <= this.mData.frontCameraLuxThreshold && this.mAmbientLuxForFrontCamera > this.mData.frontCameraLuxThreshold) || (this.mLastAmbientLuxForFrontCamera > this.mData.frontCameraLuxThreshold && this.mAmbientLuxForFrontCamera <= this.mData.frontCameraLuxThreshold)) {
                Slog.i(TAG, "updateFrontCameraMaxBrightness mLastAmbientLuxForFrontCamera=" + this.mLastAmbientLuxForFrontCamera + ",mAmbientLuxForFrontCamera=" + this.mAmbientLuxForFrontCamera);
                updateFrontCameraMaxBrightness();
                this.mLastAmbientLuxForFrontCamera = this.mAmbientLuxForFrontCamera;
            }
        }
        if (this.mHwAmbientLuxFilterAlgo.needToUpdateBrightness() || isDarkAdaptStateChanged) {
            if (isUpdateAutoBrightnessDelayedForDarkenLuxChange()) {
                Slog.i(TAG, "DProximity need delayed time to update brightness, mAmbientLuxTmp=" + this.mAmbientLuxTmp + ",mAmbientLux=" + this.mAmbientLux);
                updateProximityEnableStateDelayedMsg(true);
                updateAutoBrightnessForLuxChangeDelayedMsg();
            } else {
                updateAutoBrightnessForLuxChange(this.mAmbientLuxTmp);
            }
        }
        if (!this.mIsHwReportValueWhenSensorOnChange) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(1, (long) this.mHwRateMillis);
        }
        if (!getProximityPositive()) {
            sendAmbientLightToMonitor(time, luxOut);
        } else {
            this.mLastAmbientLightToMonitorTime = 0;
        }
        sendDefaultBrightnessToMonitor();
    }

    private void updateFirstAmbientLuxPara(float lux) {
        if (!this.mAmbientLuxValid) {
            this.mWakeupFromSleep = false;
            this.mIsWaitFristAutoBrightness = false;
            updateWakeupFromSleep(this.mWakeupFromSleep);
            this.mAmbientLuxValid = true;
            this.mHwAmbientLuxFilterAlgo.updateFirstAmbientLuxEnable(true);
            if (sHwNormalizedScreenAutoBrightnessSpline != null) {
                if (this.mData.dayModeAlgoEnable || this.mData.offsetResetEnable) {
                    this.mHwAmbientLuxFilterAlgo.setAutoModeEnableFirstLux(lux);
                    this.mHwAmbientLuxFilterAlgo.setDayModeEnable();
                    if (this.mData.dayModeAlgoEnable) {
                        sHwNormalizedScreenAutoBrightnessSpline.setDayModeEnable(this.mHwAmbientLuxFilterAlgo.getDayModeEnable());
                    }
                    updateOffsetPara();
                    if (HWFLOW) {
                        Slog.i(TAG, "DayMode:dayModeEnable=" + this.mHwAmbientLuxFilterAlgo.getDayModeEnable() + ",offsetEnable=" + this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable());
                    }
                }
                if (HWFLOW) {
                    Slog.i(TAG, "mAmbientLuxValid=" + this.mAmbientLuxValid + ",mWakeupFromSleep=" + this.mWakeupFromSleep + ",mIsWaitFristAutoBrightness=" + this.mIsWaitFristAutoBrightness);
                }
            }
        }
    }

    private void updateOffsetPara() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.updateOffsetPara();
        }
    }

    private void updateDarkTimeDelayFromBrightnessEnable() {
        HwNormalizedSpline hwNormalizedSpline;
        if (this.mData.darkTimeDelayEnable && (hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline) != null) {
            float defaultBrightness = hwNormalizedSpline.getNewDefaultBrightness(this.mAmbientLux);
            if (this.mAmbientLux < this.mData.darkTimeDelayLuxThreshold || defaultBrightness >= this.mData.darkTimeDelayBrightness) {
                this.mHwAmbientLuxFilterAlgo.setDarkTimeDelayFromBrightnessEnable(false);
                return;
            }
            if (HWFLOW) {
                Slog.i(TAG, "DarkTimeDelay mAmbientLux=" + this.mAmbientLux + ",defaultBrightness=" + defaultBrightness + ",darkTimeDelayBrightness=" + this.mData.darkTimeDelayBrightness);
            }
            this.mHwAmbientLuxFilterAlgo.setDarkTimeDelayFromBrightnessEnable(true);
        }
    }

    private void updateSecondDarkenModeNoResponseLongEnable() {
        HwNormalizedSpline hwNormalizedSpline;
        if (this.mData.secondDarkenModeEnable && this.mData.secondDarkenModeNoResponseDarkenTime > 0 && this.mData.secondDarkenModeNoResponseDarkenTimeMin > 0 && this.mData.secondDarkenModeMinLuxTh > 0.0f && (hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline) != null) {
            float normalizedBrightness = hwNormalizedSpline.interpolate(this.mData.secondDarkenModeMinLuxTh) * 10000.0f;
            if (((int) this.mAmbientLux) == ((int) this.mData.secondDarkenModeMinLuxTh)) {
                float brightness = (255.0f * normalizedBrightness) / 10000.0f;
                if (HWFLOW) {
                    Slog.i(TAG, "secondDarkenMode lux=" + this.mData.secondDarkenModeMinLuxTh + ",brightness=" + brightness + ",normalizedBrightness=" + normalizedBrightness + ",noFlickertarget=" + this.mData.darkenNoFlickerTarget);
                }
            }
            this.mHwAmbientLuxFilterAlgo.updateSecondDarkenModeNoResponseLongEnable(normalizedBrightness < this.mData.darkenNoFlickerTarget);
        }
    }

    private void sendAmbientLightToMonitor(long time, float lux) {
        if (this.mDisplayEffectMonitor != null) {
            long j = this.mLastAmbientLightToMonitorTime;
            if (j == 0 || time <= j) {
                this.mLastAmbientLightToMonitorTime = time;
                return;
            }
            int durationInMs = (int) (time - j);
            if (durationInMs >= 2000) {
                this.mLastAmbientLightToMonitorTime = time;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "ambientLightCollection");
                params.put("lightValue", Integer.valueOf((int) lux));
                params.put("durationInMs", Integer.valueOf(durationInMs));
                params.put("brightnessMode", "AUTO");
                int i = this.mDualSensorRawAmbient;
                if (i >= 0) {
                    params.put("rawLightValue", Integer.valueOf(i));
                }
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendDefaultBrightnessToMonitor() {
        HwNormalizedSpline hwNormalizedSpline;
        int defaultBrightness;
        if (this.mDisplayEffectMonitor != null && (hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline) != null && this.mLastDefaultBrightness != (defaultBrightness = (int) hwNormalizedSpline.getCurrentDefaultBrightnessNoOffset())) {
            this.mLastDefaultBrightness = defaultBrightness;
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "algoDefaultBrightness");
            params.put("lightValue", Integer.valueOf((int) sHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForBrightness()));
            params.put("brightness", Integer.valueOf(defaultBrightness));
            params.put("brightnessMode", "AUTO");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendPowerStateToMonitor(int policy) {
        String newStateName;
        if (this.mDisplayEffectMonitor != null) {
            if (policy == 0 || policy == 1) {
                newStateName = "OFF";
            } else if (policy == 2) {
                newStateName = "DIM";
            } else if (policy == 3) {
                newStateName = "ON";
            } else if (policy != 4) {
                newStateName = "OFF";
            } else {
                newStateName = "VR";
            }
            if (this.mPowerStateNameForMonitor != newStateName) {
                this.mPowerStateNameForMonitor = newStateName;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "powerStateUpdate");
                params.put("powerState", newStateName);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendXmlConfigToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "xmlConfig");
            params.put("enable", Boolean.valueOf(this.mData.monitorEnable));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendPersonalizedCurveAndParamToMonitor(List<Short> curve, List<Float> algoParam) {
        if (this.mDisplayEffectMonitor != null && curve != null && !curve.isEmpty() && algoParam != null && !algoParam.isEmpty()) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "personalizedCurveAndParam");
            params.put("personalizedCurve", curve);
            params.put("personalizedParam", algoParam);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void setBrightnessLimitedByThermal(boolean isLimited) {
        sendThermalLimitToMonitor(isLimited);
    }

    private void sendThermalLimitToMonitor(boolean isLimited) {
        if (this.mDisplayEffectMonitor != null && this.mIsBrightnessLimitedByThermal != isLimited) {
            this.mIsBrightnessLimitedByThermal = isLimited;
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "thermalLimit");
            params.put("isLimited", Boolean.valueOf(isLimited));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void handleLightSensorEvent(long time, float lux) {
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline == null || !hwNormalizedSpline.isCalibrationTestEnable()) {
            this.mIsSetBrightnessImmediateEnable = false;
            if (!this.mAmbientLuxValid || this.mIsHwReportValueWhenSensorOnChange) {
                reportLightSensorEventToAlgo(time, lux);
                if (!this.mIsHwReportValueWhenSensorOnChange) {
                    synchronized (this.mHwAmbientLightRingBuffer) {
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
            return;
        }
        this.mIsSetBrightnessImmediateEnable = true;
        updateLightSensorFromDb();
    }

    private void updateLightSensorFromDb() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        this.mAmbientLuxValid = true;
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline != null) {
            float ambientLux = hwNormalizedSpline.getAmbientValueFromDb();
            if (((int) (ambientLux * 10.0f)) != ((int) (this.mAmbientLux * 10.0f))) {
                this.mAmbientLux = ambientLux;
                if (HWFLOW) {
                    Slog.i(TAG, "setAmbientLuxDB=" + this.mAmbientLux);
                }
                updateAutoBrightness(true, false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMaxBrightnessFromCryogenicDelayed() {
        if (HWFLOW) {
            Slog.i(TAG, "mMaxBrightnessSetByCryogenicBypassDelayed=false");
        }
        this.mMaxBrightnessSetByCryogenicBypassDelayed = false;
        if (this.mMaxBrightnessSetByCryogenic < 255 && this.mLightSensorEnabled) {
            Slog.i(TAG, "Cryogenic set mMaxBrightnessSetByCryogenic=" + this.mMaxBrightnessSetByCryogenic);
            this.mCallbacks.updateBrightness();
        }
    }

    public void setPowerStatus(boolean isPowerOn) {
        updateCryogenicPara(isPowerOn);
        if (HWFLOW && this.mIsPowerOnStatus != isPowerOn) {
            Slog.i(TAG, "set power status:mIsPowerOnStatus=" + this.mIsPowerOnStatus + ",isPowerOn=" + isPowerOn);
        }
        if (this.mIsPowerOnStatus != isPowerOn && isPowerOn && this.mData.coverModeDayEnable) {
            updateCoverModeDayBrightness();
        }
        updateVehiclePara(isPowerOn);
        this.mIsPowerOnStatus = isPowerOn;
        this.mIsScreenStatus = isPowerOn;
        this.mWakeupFromSleep = isPowerOn;
        updateWakeupFromSleep(this.mWakeupFromSleep);
        this.mWakeupForFirstAutoBrightness = isPowerOn;
        this.mHwAmbientLuxFilterAlgo.setPowerStatus(isPowerOn);
        if (!this.mIsPowerOnStatus) {
            this.mPowerOnLuxAbandonCount = 0;
            this.mPowerOnLuxCount = 0;
            this.mIsWakeupCoverBrightnessEnable = false;
        }
    }

    private void updateCryogenicPara(boolean isPowerOn) {
        Runnable runnable;
        if (this.mData.cryogenicEnable) {
            if (isPowerOn) {
                this.mPowerOnTimestamp = SystemClock.elapsedRealtime();
                if (this.mPowerOnTimestamp - this.mPowerOffTimestamp > this.mData.cryogenicActiveScreenOffIntervalInMillis) {
                    if (HWFLOW) {
                        Slog.i(TAG, "mPowerOnTimestamp - mPowerOffTimestamp=" + (this.mPowerOnTimestamp - this.mPowerOffTimestamp) + ", apply Cryogenic brightness limit(" + this.mMaxBrightnessSetByCryogenic + ")!");
                    }
                    this.mMaxBrightnessSetByCryogenicBypass = false;
                }
                if (HWFLOW) {
                    Slog.i(TAG, "mMaxBrightnessSetByCryogenicBypass=" + this.mMaxBrightnessSetByCryogenicBypass + " mMaxBrightnessSetByCryogenicBypassDelayed=" + this.mMaxBrightnessSetByCryogenicBypassDelayed);
                }
                if (this.mMaxBrightnessSetByCryogenic == 255) {
                    this.mMaxBrightnessSetByCryogenicBypassDelayed = true;
                    if (HWFLOW) {
                        Slog.d(TAG, "No Cryogenic brightness limit! Then it should be active " + (this.mData.cryogenicLagTimeInMillis / MINUTE_IN_MILLIS) + "min later!");
                    }
                    Handler handler = this.mMaxBrightnessFromCryogenicHandler;
                    if (handler != null && (runnable = this.mMaxBrightnessFromCryogenicDelayedRunnable) != null) {
                        handler.removeCallbacks(runnable);
                        this.mMaxBrightnessFromCryogenicHandler.postDelayed(this.mMaxBrightnessFromCryogenicDelayedRunnable, this.mData.cryogenicLagTimeInMillis);
                        return;
                    }
                    return;
                }
                return;
            }
            this.mPowerOffTimestamp = SystemClock.elapsedRealtime();
            CryogenicPowerProcessor cryogenicPowerProcessor = this.mCryogenicProcessor;
            if (cryogenicPowerProcessor != null) {
                cryogenicPowerProcessor.onScreenOff();
            }
        }
    }

    private void updateVehiclePara(boolean isPowerOn) {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.updateVehiclePara(isPowerOn);
        }
    }

    private void updateCoverModeDayBrightness() {
        int openHour;
        boolean isClosed = HwServiceFactory.isCoverClosed();
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
        if (isClosed && brightnessMode == 1 && (openHour = Calendar.getInstance().get(11)) >= this.mData.converModeDayBeginTime && openHour < this.mData.coverModeDayEndTime) {
            setCoverModeDayEnable(true);
            this.mIsWakeupCoverBrightnessEnable = true;
            Slog.i(TAG, "LabcCoverMode,isClosed=" + isClosed + ",openHour=" + openHour + ",coverModeBrightness=" + this.mData.coverModeDayBrightness);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isInValidLightSensorEvent(long time, float lux) {
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime < ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime) {
            Slog.i(TAG, "sensor not ready yet at time " + time);
            return true;
        } else if (this.mIsCurrentUserChanging) {
            return true;
        } else {
            if (this.mIsPowerOnStatus) {
                this.mPowerOnLuxAbandonCount++;
                this.mPowerOnLuxCount++;
                if (this.mPowerOnLuxCount > getPowerOnFastResponseLuxNum() && currentTime > this.mLightSensorEnableTime + this.mData.powerOnFastResponseTime) {
                    if (HWFLOW) {
                        Slog.i(TAG, "set power status:false,mPowerOnLuxCount=" + this.mPowerOnLuxCount + ",powerOnFastResponseLuxNum=" + getPowerOnFastResponseLuxNum() + ",currentTime=" + currentTime + ",deltaTime= " + (currentTime - this.mLightSensorEnableTime));
                    }
                    this.mIsPowerOnStatus = false;
                    this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
                }
                if (this.mLightSensorEnableElapsedTimeNanos - time > 0) {
                    if (HWFLOW) {
                        Slog.i(TAG, "abandon handleLightSensorEvent:" + lux);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        this.mIsIntervenedAutoBrightnessEnable = false;
        if (sHwNormalizedScreenAutoBrightnessSpline == null) {
            Slog.w(TAG, "updateAutoBrightnessAdjustFactor,sHwNormalizedScreenAutoBrightnessSpline==null");
            return;
        }
        float lux = this.mHwAmbientLuxFilterAlgo.getOffsetValidAmbientLux();
        if (this.mData.offsetValidAmbientLuxEnable) {
            float luxCurrent = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
            boolean isProximityPositiveEnable = this.mHwAmbientLuxFilterAlgo.getProximityPositiveEnable();
            float positionBrightness = adjustFactor * 255.0f;
            float defautBrightness = sHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
            if (isProximityPositiveEnable && ((int) positionBrightness) > ((int) defautBrightness)) {
                lux = luxCurrent;
            }
            this.mHwAmbientLuxFilterAlgo.setCurrentAmbientLux(lux);
        }
        updateAmbientLuxFromOffset(lux);
        if (HWFLOW) {
            Slog.i(TAG, "AdjustPositionBrightness=" + ((int) (adjustFactor * 255.0f)) + ",lux=" + lux);
        }
        float brightnessNewOffset = 255.0f * adjustFactor;
        if (this.mData.luxMinMaxBrightnessEnable && ((int) brightnessNewOffset) < this.mLuxMinBrightness && ((int) brightnessNewOffset) > 0) {
            Slog.i(TAG, "AdjustPositionBrightness,brightnessNewOffset=" + brightnessNewOffset + "-->mLuxMinBrightness=" + this.mLuxMinBrightness);
            brightnessNewOffset = (float) this.mLuxMinBrightness;
        }
        if (this.mData.frontCameraMaxBrightnessEnable && ((int) brightnessNewOffset) > this.mFrontCameraMaxBrightness && ((int) brightnessNewOffset) > 0) {
            Slog.i(TAG, "AdjustPositionBrightness,brightnessNewOffset=" + brightnessNewOffset + "-->mFrontCameraMaxBrightness=" + this.mFrontCameraMaxBrightness);
            brightnessNewOffset = (float) this.mFrontCameraMaxBrightness;
        }
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            this.mIsGameModeBrightnessOffsetEnable = hwBrightnessSceneController.mIsGameModeBrightnessOffsetEnable;
            if (this.mIsGameModeBrightnessOffsetEnable) {
                Slog.i(TAG, "updateLevelGameWithLux mIsGameModeBrightnessOffsetEnable=" + this.mIsGameModeBrightnessOffsetEnable);
            }
        }
        if (this.mIsGameModeBrightnessOffsetEnable) {
            sHwNormalizedScreenAutoBrightnessSpline.updateLevelGameWithLux(brightnessNewOffset, lux);
        } else {
            sHwNormalizedScreenAutoBrightnessSpline.updateLevelWithLux(brightnessNewOffset, lux);
        }
        updateResetAmbientLuxDisableBrightnessOffset(adjustFactor);
    }

    private void updateAmbientLuxFromOffset(float lux) {
        if (((int) (lux * 10.0f)) != ((int) (this.mAmbientLux * 10.0f))) {
            Slog.i(TAG, "updateAmbientLuxFromOffset mAmbientLux=" + this.mAmbientLux + "->offsetlux=" + lux);
            this.mAmbientLux = lux;
        }
    }

    private void updateResetAmbientLuxDisableBrightnessOffset(float adjustFactor) {
        int brightnessOffset = (int) (255.0f * adjustFactor);
        int i = 0;
        if (this.mResetAmbientLuxDisableBrightnessOffset == 0 && brightnessOffset > this.mData.resetAmbientLuxDisableBrightnessOffset) {
            updateBrightnessModeChangeManualState(false);
        }
        if (brightnessOffset > this.mData.resetAmbientLuxDisableBrightnessOffset) {
            i = brightnessOffset;
        }
        this.mResetAmbientLuxDisableBrightnessOffset = i;
    }

    /* access modifiers changed from: protected */
    public boolean setAutoBrightnessAdjustment(float adjustment) {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateAmbientLuxMsg() {
        reportLightSensorEventToAlgo(SystemClock.uptimeMillis(), (float) getSensorData());
    }

    /* access modifiers changed from: private */
    public final class HwNormalizedAutomaticBrightnessHandler extends Handler {
        HwNormalizedAutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.w(HwNormalizedAutomaticBrightnessController.TAG, "HwNormalizedAutomaticBrightnessHandler msg==null");
                return;
            }
            switch (msg.what) {
                case 1:
                    HwNormalizedAutomaticBrightnessController.this.handleUpdateAmbientLuxMsg();
                    return;
                case 2:
                    HwNormalizedAutomaticBrightnessController.this.setCoverModeFastResponseFlag();
                    return;
                case 3:
                    HwNormalizedAutomaticBrightnessController.this.updateAutoBrightness(3);
                    return;
                case 4:
                    HwNormalizedAutomaticBrightnessController.this.setFrontCameraBrightnessDimmingEnable(false);
                    return;
                case 5:
                    HwNormalizedAutomaticBrightnessController.this.handleDisableProximityDelayedMsg();
                    return;
                case 6:
                    HwNormalizedAutomaticBrightnessController.this.handleEnableProximityDelayedMsg();
                    return;
                case 7:
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = HwNormalizedAutomaticBrightnessController.this;
                    hwNormalizedAutomaticBrightnessController.updateAutoBrightnessForLuxChange(hwNormalizedAutomaticBrightnessController.mAmbientLuxTmp);
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessIfNoAmbientLuxReported() {
        if (this.mWakeupFromSleep) {
            this.mWakeupFromSleep = false;
            this.mIsWaitFristAutoBrightness = false;
            updateWakeupFromSleep(this.mWakeupFromSleep);
            this.mCallbacks.updateBrightness();
            this.mFirstAutoBrightness = false;
            this.mUpdateAutoBrightnessCount++;
            if (HWFLOW) {
                Slog.i(TAG, "sensor doesn't report lux in 200ms");
            }
        }
    }

    public void updateCurrentUserId(int userId) {
        if (userId != this.mCurrentUserId) {
            if (HWFLOW) {
                Slog.i(TAG, "updateCurrentUserId change from " + this.mCurrentUserId + " into " + userId);
            }
            this.mCurrentUserId = userId;
            this.mIsCurrentUserChanging = true;
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mAmbientLuxValid = false;
            this.mHwAmbientLuxFilterAlgo.clear();
            HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
            if (hwNormalizedSpline != null) {
                hwNormalizedSpline.updateCurrentUserId(userId);
            }
            HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
            if (hwBrightnessSceneController != null) {
                hwBrightnessSceneController.updateCurrentUserId(userId);
            }
            this.mIsCurrentUserChanging = false;
        }
    }

    private void updateProximitySensorEnabledMsg(boolean isProximityEnable) {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector != null) {
            hwProximitySensorDetector.updateProximitySensorEnabledMsg(isProximityEnable);
        }
    }

    private void setProximitySensorEnabled(boolean isProximitySensorEnabled) {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector != null) {
            hwProximitySensorDetector.setProximitySensorEnabled(isProximitySensorEnabled);
        }
    }

    public void updatePowerStateAndPolicy(int state, int policy) {
        boolean z = false;
        boolean isPowerOnEnable = wantScreenOn(policy) || (state == 2 && policy == 1 && this.mData.fingerprintOffUnlockEnable);
        this.mPowerState = state;
        if (isPowerOnEnable != this.mIsPowerOnEnable) {
            updateNightUpModeTime(isPowerOnEnable);
            setPowerStatus(isPowerOnEnable);
        }
        this.mIsPowerOnEnable = isPowerOnEnable;
        if (this.mPowerPolicy == 2 && policy != 2) {
            z = true;
        }
        this.mIsPolicyChangeFromDim = z;
        this.mPowerPolicy = policy;
        sendPowerStateToMonitor(policy);
    }

    private void updateDarkenAmbientEnable() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.updateDarkenAmbientEnable(this.mAmbientLux);
        }
    }

    private void updateNightUpModeTime(boolean isPowerOnEnable) {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.updateNightUpModeTime(isPowerOnEnable);
        }
    }

    public boolean getNightUpPowerOnWithDimmingEnable() {
        boolean isDimmingEnable = false;
        if (!this.mData.nightUpModeEnable) {
            return false;
        }
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            this.mIsNightUpTimeEnable = hwBrightnessSceneController.mIsNightUpTimeEnable;
            this.mIsDarkenAmbientEnable = this.mHwBrightnessSceneController.mIsDarkenAmbientEnable;
        }
        if (this.mIsNightUpTimeEnable && this.mIsDarkenAmbientEnable) {
            isDimmingEnable = true;
        }
        if (HWDEBUG) {
            Slog.i(TAG, "NightUpBrightMode mIsNightUpTimeEnable=" + this.mIsNightUpTimeEnable + ",mIsDarkenAmbientEnable=" + this.mIsDarkenAmbientEnable);
        }
        return isDimmingEnable;
    }

    private static boolean wantScreenOn(int state) {
        if (state == 2 || state == 3) {
            return true;
        }
        return false;
    }

    private boolean needToSetBrightnessBaseProximity() {
        HwBrightnessSceneController hwBrightnessSceneController;
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector != null) {
            this.mProximity = hwProximitySensorDetector.mProximity;
        }
        boolean isNeedToSet = true;
        if (!this.mBrightnessEnlarge && this.mBrightnessNoLimitSetByApp > 0) {
            this.mBrightnessEnlarge = true;
            Slog.i(TAG, "set mBrightnessEnlarge=" + this.mBrightnessEnlarge + ",mBrightnessNoLimitSetByApp=" + this.mBrightnessNoLimitSetByApp);
        }
        if (!this.mBrightnessEnlarge && this.mWakeupForFirstAutoBrightness && this.mProximity == 1) {
            this.mBrightnessEnlarge = true;
            if (HWFLOW) {
                Slog.i(TAG, "mWakeupForFirstAutoBrightness set mBrightnessEnlarge=" + this.mBrightnessEnlarge);
            }
        }
        if (this.mData.isInwardFoldScreenLuxEnable && (hwBrightnessSceneController = this.mHwBrightnessSceneController) != null && hwBrightnessSceneController.mCurrentDisplayMode == 1) {
            this.mBrightnessEnlarge = true;
            if (HWFLOW) {
                Slog.i(TAG, "DISPLAY_MODE_FULL set mBrightnessEnlarge=" + this.mBrightnessEnlarge);
            }
        }
        if (this.mProximity != 1 || this.mBrightnessEnlarge || this.mUpdateAutoBrightnessCount <= 1 || this.mPowerPolicy == 2 || this.mIsPolicyChangeFromDim) {
            isNeedToSet = false;
        }
        if (HWFLOW && isNeedToSet) {
            Slog.i(TAG, "mProximity=" + this.mProximity + ",mBrightnessEnlarge=" + this.mBrightnessEnlarge + ",mUpdateAutoBrightnessCount=" + this.mUpdateAutoBrightnessCount + ",mPowerPolicy=" + this.mPowerPolicy + ",mIsPolicyChangeFromDim=" + this.mIsPolicyChangeFromDim);
        }
        return isNeedToSet;
    }

    public void setSplineEyeProtectionControlFlag(boolean isEyeFlag) {
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.setEyeProtectionControlFlag(isEyeFlag);
        }
    }

    public boolean getPowerStatus() {
        return this.mIsPowerOnStatus;
    }

    public boolean getScreenStatus() {
        return this.mIsScreenStatus;
    }

    public void setCoverModeStatus(boolean isClosed) {
        if (isClosed) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(2);
        }
        if (!isClosed && this.mIsClosed) {
            this.mCoverStateFast = true;
            this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(2, (long) this.mCoverModeFastResponseTimeDelay);
        }
        this.mIsClosed = isClosed;
        this.mHwAmbientLuxFilterAlgo.setCoverModeStatus(isClosed);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCoverModeFastResponseFlag() {
        this.mCoverStateFast = false;
        this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.setNoOffsetEnable(this.mCoverStateFast);
        }
        if (HWFLOW) {
            Slog.i(TAG, "LabcCoverMode FastResponseFlag =" + this.mCoverStateFast);
        }
    }

    public boolean getCoverModeFastResponseFlag() {
        return this.mCoverStateFast;
    }

    public void setBackSensorCoverModeBrightness(int brightness) {
        if (this.mData.backSensorCoverModeEnable && brightness > 0) {
            this.mScreenAutoBrightness = brightness;
            this.mHwAmbientLuxFilterAlgo.setBackSensorCoverModeBrightness(brightness);
        }
    }

    private int getPowerOnFastResponseLuxNum() {
        return this.mData.powerOnFastResponseLuxNum;
    }

    private boolean getCameraModeBrightnessLineEnable() {
        return this.mData.cameraModeEnable;
    }

    public boolean getReadingModeBrightnessLineEnable() {
        return this.mData.readingModeEnable;
    }

    public void setCameraModeBrightnessLineEnable(boolean isCameraModeEnable) {
        if (sHwNormalizedScreenAutoBrightnessSpline != null && getCameraModeBrightnessLineEnable()) {
            sHwNormalizedScreenAutoBrightnessSpline.setCameraModeEnable(isCameraModeEnable);
            if (this.mIsCameraModeEnable != isCameraModeEnable) {
                if (HWFLOW) {
                    Slog.i(TAG, "CameraModeEnable change isCameraModeEnable=" + isCameraModeEnable);
                }
                this.mIsCameraModeChangeAnimationEnable = true;
                updateAutoBrightness(true, false);
            }
            this.mIsCameraModeEnable = isCameraModeEnable;
        }
    }

    public boolean getCameraModeChangeAnimationEnable() {
        boolean isAnimationEnable = this.mIsCameraModeChangeAnimationEnable;
        this.mIsCameraModeChangeAnimationEnable = false;
        return isAnimationEnable;
    }

    public boolean getReadingModeChangeAnimationEnable() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            this.mIsReadingModeChangeAnimationEnable = hwBrightnessSceneController.mIsReadingModeChangeAnimationEnable;
        }
        boolean isLastModeEnable = this.mIsReadingModeChangeAnimationEnable;
        this.mIsReadingModeChangeAnimationEnable = false;
        HwBrightnessSceneController hwBrightnessSceneController2 = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController2 != null) {
            hwBrightnessSceneController2.mIsReadingModeChangeAnimationEnable = false;
        }
        return isLastModeEnable;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
        this.mIsLocked = isLocked;
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo == null) {
            Slog.e(TAG, "mHwAmbientLuxFilterAlgo=null");
        } else {
            hwAmbientLuxFilterAlgo.setKeyguardLockedStatus(isLocked);
        }
    }

    public boolean getRebootAutoModeEnable() {
        String enterpriseCotaVersion = SystemProperties.get("ro.product.EcotaVersion", "");
        if (enterpriseCotaVersion == null || enterpriseCotaVersion.isEmpty() || Settings.System.getIntForUser(this.mContext.getContentResolver(), HW_CUSTOMIZATION_SCREEN_BRIGHTNESS_MODE, 1, this.mCurrentUserId) != 0) {
            return this.mData.rebootAutoModeEnable;
        }
        Slog.i(TAG, "Brightness mode has been customized to manual mode");
        return false;
    }

    public boolean getOutdoorAnimationFlag() {
        return this.mHwAmbientLuxFilterAlgo.getOutdoorAnimationFlag();
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        if (this.mData.backSensorCoverModeEnable) {
            return -3;
        }
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
        if (this.mIsWakeupCoverBrightnessEnable && !this.mHwAmbientLuxFilterAlgo.getCoverModeDayEnable()) {
            this.mIsWakeupCoverBrightnessEnable = false;
        }
        if (brightnessMode != 1 || this.mIsWakeupCoverBrightnessEnable) {
            return this.mData.coverModeDayBrightness;
        }
        return this.mHwAmbientLuxFilterAlgo.getCoverModeBrightnessFromLastScreenBrightness();
    }

    public void setCoverModeDayEnable(boolean isCoverModeDayEnable) {
        this.mHwAmbientLuxFilterAlgo.setCoverModeDayEnable(isCoverModeDayEnable);
    }

    public int setScreenBrightnessMappingToIndoorMax(int brightness) {
        int brightnessOut;
        boolean isCallDeInQrCodeScene = brightness > -1;
        if (isCallDeInQrCodeScene != this.mIsCallDeInQrCodeScene) {
            this.mIsCallDeInQrCodeScene = isCallDeInQrCodeScene;
            this.mManager.setScene(54, this.mIsCallDeInQrCodeScene ? 16 : 17);
        }
        if (brightness == -1 || !this.mData.manualMode || brightness > 255) {
            HwBrightnessPgSceneDetection.setQrCodeAppBrightnessNoPowerSaving(false);
            return brightness;
        }
        if (brightness < 4) {
            HwBrightnessPgSceneDetection.setQrCodeAppBrightnessNoPowerSaving(false);
            brightnessOut = 4;
            if (HWFLOW && this.mBrightnessOutForLog != 4) {
                this.mBrightnessOutForLog = 4;
                Slog.i(TAG, "mScreenBrightnessOverrideFromWindowManagerMapping brightnessIn=" + brightness + ",brightnessOut=4");
            }
        } else {
            if (this.mData.QRCodeBrightnessminLimit <= 0 || !this.mHwBrightnessPgSceneDetection.isQrCodeAppBoostBrightness(brightness) || isFullSceenVideoPlay()) {
                HwBrightnessPgSceneDetection.setQrCodeAppBrightnessNoPowerSaving(false);
                brightnessOut = getMappingBrightnessFromWindow(brightness);
            } else {
                HwBrightnessPgSceneDetection.setQrCodeAppBrightnessNoPowerSaving(true);
                brightnessOut = this.mData.manualBrightnessMaxLimit;
                if (HWFLOW && this.mBrightnessOutForLog != brightnessOut) {
                    Slog.i(TAG, "QrCodeBrightness=" + brightness + "-->brightnessOut=" + brightnessOut);
                }
            }
            if (!(brightness == brightnessOut || this.mBrightnessOutForLog == brightnessOut)) {
                this.mBrightnessOutForLog = brightnessOut;
                Slog.i(TAG, "mScreenBrightnessOverrideFromWindowManagerMapping brightnessIn=" + brightness + ",brightnessOut=" + brightnessOut + ",getHdrModeEnable()=" + getHdrModeEnable());
            }
        }
        return brightnessOut;
    }

    private boolean isFullSceenVideoPlay() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController == null) {
            return false;
        }
        return hwBrightnessSceneController.mIsVideoPlay;
    }

    private int getMappingBrightnessFromWindow(int brightness) {
        if ((!this.mData.brightnessMappingForWindowBrightnessEnable && (!this.mData.hdrModeWindowMappingEnable || !getHdrModeEnable())) || this.mHwBrightnessMapping == null) {
            return (((brightness - 4) * (this.mData.manualBrightnessMaxLimit - 4)) / 251) + 4;
        }
        if (!this.mData.hdrModeWindowMappingEnable || !getHdrModeEnable()) {
            return this.mHwBrightnessMapping.getMappingBrightnessForWindowBrightness(this.mData.brightnessMappingPointsForWindowBrightness, brightness);
        }
        return this.mHwBrightnessMapping.getMappingBrightnessForWindowBrightness(this.mData.brightnessMappingPointsForHdrMode, brightness);
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        if (HWFLOW) {
            Slog.i(TAG, "ThermalMode set auto MaxBrightness=" + brightness + ",validMin=" + this.mData.minValidThermalBrightness);
        }
        int mappingBrightness = brightness;
        if (brightness <= 0 || ((float) brightness) <= this.mData.minValidThermalBrightness) {
            this.mMaxBrightnessSetByThermal = 255;
        } else {
            if (this.mData.thermalModeBrightnessMappingEnable) {
                mappingBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(brightness);
            }
            this.mMaxBrightnessSetByThermal = mappingBrightness;
        }
        if (this.mLightSensorEnabled) {
            Slog.i(TAG, "ThermalMode set auto MaxBrightness=" + brightness + ",mappingBrightness=" + mappingBrightness);
            this.mCallbacks.updateBrightness();
        }
    }

    public void setMaxBrightnessFromCryogenic(int brightness) {
        if (this.mData.cryogenicEnable) {
            int mappingBrightness = brightness;
            if (brightness <= 0 || ((float) brightness) <= this.mData.minValidThermalBrightness) {
                this.mMaxBrightnessSetByCryogenic = 255;
            } else {
                if (this.mData.cryogenicModeBrightnessMappingEnable) {
                    mappingBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(brightness);
                }
                this.mMaxBrightnessSetByCryogenic = mappingBrightness;
            }
            if (this.mLightSensorEnabled) {
                Slog.i(TAG, "Cryogenic set auto MaxBrightness=" + brightness + ",mMaxBrightnessSetByCryogenic=" + this.mMaxBrightnessSetByCryogenic);
                this.mCallbacks.updateBrightness();
            }
        }
    }

    public boolean getRebootFirstBrightnessAnimationEnable() {
        return this.mData.rebootFirstBrightnessAnimationEnable;
    }

    /* access modifiers changed from: protected */
    public int getAdjustLightValByPgMode(int rawLightVal) {
        HwBrightnessPgSceneDetection hwBrightnessPgSceneDetection = this.mHwBrightnessPgSceneDetection;
        if (hwBrightnessPgSceneDetection != null) {
            int powerSavingBrightness = hwBrightnessPgSceneDetection.getAdjustLightValByPgMode(rawLightVal);
            if (this.mData.pgModeBrightnessMappingEnable && rawLightVal > this.mData.manualBrightnessMaxLimit) {
                powerSavingBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(powerSavingBrightness);
                if (HWFLOW) {
                    Slog.i(TAG, "PG_POWER_SAVE_MODE auto powerSavingBrightness=" + powerSavingBrightness + ",rawLightVal=" + rawLightVal);
                }
            }
            return powerSavingBrightness;
        }
        Slog.w(TAG, "mHwBrightnessPgSceneDetection=null");
        return rawLightVal;
    }

    public int getPowerSavingBrightness(int brightness) {
        HwNormalizedSpline hwNormalizedSpline;
        HwNormalizedSpline hwNormalizedSpline2 = sHwNormalizedScreenAutoBrightnessSpline;
        if ((hwNormalizedSpline2 == null || hwNormalizedSpline2.getPersonalizedBrightnessCurveEnable()) && this.mHwBrightnessPowerSavingCurve != null && (hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline) != null && hwNormalizedSpline.isPowerSavingBrightnessLineEnable()) {
            return this.mHwBrightnessPowerSavingCurve.getPowerSavingBrightness(brightness);
        }
        return brightness;
    }

    public void setBrightnessNoLimit(int brightness, int time) {
        this.mBrightnessNoLimitSetByApp = (brightness < 4 || brightness > 255) ? -1 : brightness;
        if (this.mLightSensorEnabled) {
            Slog.i(TAG, "setBrightnessNoLimit set auto Brightness=" + brightness + ",time=" + time);
            this.mCallbacks.updateBrightness();
        }
    }

    /* access modifiers changed from: protected */
    public boolean setLightSensorEnabled(boolean isLightSensorEnabled) {
        if (isLightSensorEnabled) {
            if (!this.mLightSensorEnabled) {
                if (HWFLOW) {
                    Slog.i(TAG, "Enable LightSensor start ...");
                }
                this.mLightSensorEnabled = true;
                this.mFirstAutoBrightness = true;
                this.mUpdateAutoBrightnessCount = 0;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
                if (hwProximitySensorDetector != null) {
                    hwProximitySensorDetector.updateLightSensorEnableTime(this.mLightSensorEnableTime);
                }
                this.mLightSensorEnableElapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
                this.mCurrentLightSensorRate = this.mInitialLightSensorRate;
                registerSensor(this.mInitialLightSensorRate);
                if (this.mWakeupFromSleep) {
                    this.mHandler.sendEmptyMessageAtTime(6, this.mLightSensorEnableTime + 200);
                }
                if (HWFLOW) {
                    Slog.i(TAG, "Enable LightSensor at time:mLightSensorEnableTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableElapsedTimeNanos=" + this.mLightSensorEnableElapsedTimeNanos);
                }
                this.mScreenBrightnessBeforeAdj = -1;
                this.mIsDragFinished = true;
                updateLightSensorEnabled(this.mLightSensorEnabled);
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mFirstAutoBrightness = false;
            Slog.i(TAG, "Disable LightSensor starting...");
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            this.mCurrentLightSensorRate = -1;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(6);
            if (this.mData.frontCameraMaxBrightnessEnable && this.mIsFrontCameraDimmingEnable) {
                this.mIsFrontCameraDimmingEnable = false;
            }
            unregisterSensor();
            this.mAmbientLuxValid = !this.mResetAmbientLuxAfterWarmUpConfig;
            if (HWFLOW) {
                Slog.i(TAG, "Disable LightSensor at time:" + SystemClock.uptimeMillis());
            }
            updateLightSensorEnabled(this.mLightSensorEnabled);
        }
        return false;
    }

    private void updateLightSensorEnabled(boolean isLightSensorEnabled) {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.updateLightSensorEnabled(isLightSensorEnabled);
        }
    }

    private void registerSensor(int sensorRate) {
        if (this.mHwDualSensorEventListenerImpl == null) {
            Slog.w(TAG, "HwDualSensorEventListenerImpl is null,registerSensor failed");
            return;
        }
        int i = this.mSensorOption;
        if (i == -1) {
            this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, sensorRate * 1000, (Handler) this.mHandler);
        } else if (i == 0) {
            this.mSensorObserver = new SensorObserver();
            this.mHwDualSensorEventListenerImpl.attachFrontSensorData(this.mSensorObserver);
        } else if (i == 1) {
            this.mSensorObserver = new SensorObserver();
            this.mHwDualSensorEventListenerImpl.attachBackSensorData(this.mSensorObserver);
        } else if (i == 2) {
            this.mSensorObserver = new SensorObserver();
            this.mHwDualSensorEventListenerImpl.attachFusedSensorData(this.mSensorObserver);
        } else {
            Slog.w(TAG, "mSensorOption is not valid, no register sensor, mSensorOption=" + this.mSensorOption);
        }
    }

    private void unregisterSensor() {
        HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl = this.mHwDualSensorEventListenerImpl;
        if (hwDualSensorEventListenerImpl == null) {
            Slog.w(TAG, "HwDualSensorEventListenerImpl is null,unregisterSensor failed");
            return;
        }
        int i = this.mSensorOption;
        if (i == -1) {
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
        } else if (i == 0) {
            SensorObserver sensorObserver = this.mSensorObserver;
            if (sensorObserver != null) {
                hwDualSensorEventListenerImpl.detachFrontSensorData(sensorObserver);
            }
        } else if (i == 1) {
            SensorObserver sensorObserver2 = this.mSensorObserver;
            if (sensorObserver2 != null) {
                hwDualSensorEventListenerImpl.detachBackSensorData(sensorObserver2);
            }
        } else if (i == 2) {
            SensorObserver sensorObserver3 = this.mSensorObserver;
            if (sensorObserver3 != null) {
                hwDualSensorEventListenerImpl.detachFusedSensorData(sensorObserver3);
            }
        } else {
            Slog.w(TAG, "mSensorOption is not valid, no unregister sensor, mSensorOption=" + this.mSensorOption);
        }
    }

    private boolean handleDarkAdaptDetector(float lux) {
        HwNormalizedSpline.DarkAdaptState splineDarkAdaptState;
        boolean z = false;
        if (this.mDarkAdaptDetector == null || this.mCoverStateFast || this.mHwAmbientLuxFilterAlgo.getProximityPositiveEnable() || sHwNormalizedScreenAutoBrightnessSpline == null) {
            return false;
        }
        this.mDarkAdaptDetector.updateLux(lux, this.mAmbientLux);
        DarkAdaptDetector.AdaptState newState = this.mDarkAdaptDetector.getState();
        DarkAdaptDetector.AdaptState adaptState = this.mDarkAdaptState;
        if (adaptState == newState) {
            return false;
        }
        if (adaptState == DarkAdaptDetector.AdaptState.UNADAPTED && newState == DarkAdaptDetector.AdaptState.ADAPTING) {
            z = true;
        }
        this.mIsDarkAdaptDimmingEnable = z;
        this.mDarkAdaptState = newState;
        int i = AnonymousClass3.$SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[newState.ordinal()];
        if (i == 1) {
            splineDarkAdaptState = HwNormalizedSpline.DarkAdaptState.UNADAPTED;
        } else if (i == 2) {
            splineDarkAdaptState = HwNormalizedSpline.DarkAdaptState.ADAPTING;
        } else if (i != 3) {
            splineDarkAdaptState = null;
        } else {
            splineDarkAdaptState = HwNormalizedSpline.DarkAdaptState.ADAPTED;
        }
        sHwNormalizedScreenAutoBrightnessSpline.setDarkAdaptState(splineDarkAdaptState);
        return true;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.display.HwNormalizedAutomaticBrightnessController$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState = new int[DarkAdaptDetector.AdaptState.values().length];

        static {
            try {
                $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[DarkAdaptDetector.AdaptState.UNADAPTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[DarkAdaptDetector.AdaptState.ADAPTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$display$DarkAdaptDetector$AdaptState[DarkAdaptDetector.AdaptState.ADAPTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private void unlockDarkAdaptLine() {
        if (this.mDarkAdaptDetector != null && sHwNormalizedScreenAutoBrightnessSpline != null && this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable()) {
            sHwNormalizedScreenAutoBrightnessSpline.unlockDarkAdaptLine();
        }
    }

    public boolean getDarkAdaptDimmingEnable() {
        return this.mDarkAdaptDetector != null && this.mIsDarkAdaptDimmingEnable && this.mLightSensorEnabled;
    }

    public void clearDarkAdaptDimmingEnable() {
        this.mIsDarkAdaptDimmingEnable = false;
    }

    /* access modifiers changed from: private */
    public class SensorObserver implements Observer {
        SensorObserver() {
        }

        @Override // java.util.Observer
        public void update(Observable observable, Object arg) {
            if (arg instanceof long[]) {
                long[] data = (long[]) arg;
                long systemTimeStamp = data[2];
                long sensorTimeStamp = data[3];
                int lux = getFoldScreenLux((int) data[0], data, systemTimeStamp);
                if ((!HwServiceFactory.shouldFilteInvalidSensorVal((float) lux) || !HwNormalizedAutomaticBrightnessController.this.mIsCoverModeAbandonSensorEnable) && !HwNormalizedAutomaticBrightnessController.this.isInValidLightSensorEvent(sensorTimeStamp, (float) lux)) {
                    HwNormalizedAutomaticBrightnessController.this.handleLightSensorEvent(systemTimeStamp, (float) lux);
                    int i = HwNormalizedAutomaticBrightnessController.this.mSensorOption;
                    HwDualSensorEventListenerImpl unused = HwNormalizedAutomaticBrightnessController.this.mHwDualSensorEventListenerImpl;
                    if (i == 2) {
                        HwNormalizedAutomaticBrightnessController.this.mDualSensorRawAmbient = (int) data[4];
                    }
                }
            }
        }

        private int getFoldScreenLux(int lux, long[] data, long systemTimeStamp) {
            int luxOut = lux;
            int fusedLux = lux;
            int frontLux = lux;
            int backLux = lux;
            if (HwNormalizedAutomaticBrightnessController.this.mData.foldScreenModeEnable && data != null && data.length >= 6) {
                fusedLux = (int) data[0];
                frontLux = (int) data[4];
                backLux = (int) data[5];
                if (HwNormalizedAutomaticBrightnessController.this.mHwBrightnessSceneController != null) {
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = HwNormalizedAutomaticBrightnessController.this;
                    hwNormalizedAutomaticBrightnessController.mCurrentDisplayMode = hwNormalizedAutomaticBrightnessController.mHwBrightnessSceneController.mCurrentDisplayMode;
                } else {
                    HwNormalizedAutomaticBrightnessController.this.mCurrentDisplayMode = 0;
                }
                if (HwNormalizedAutomaticBrightnessController.this.mData.isInwardFoldScreenLuxEnable) {
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController2 = HwNormalizedAutomaticBrightnessController.this;
                    luxOut = hwNormalizedAutomaticBrightnessController2.getLuxFromMultiSensor(hwNormalizedAutomaticBrightnessController2.mCurrentDisplayMode, lux, frontLux, backLux, fusedLux);
                } else {
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController3 = HwNormalizedAutomaticBrightnessController.this;
                    luxOut = hwNormalizedAutomaticBrightnessController3.getLuxFromDualSensor(hwNormalizedAutomaticBrightnessController3.mCurrentDisplayMode, lux, frontLux, backLux, fusedLux);
                }
            }
            if (systemTimeStamp - HwNormalizedAutomaticBrightnessController.this.mLightSensorEnableTime < 2000) {
                if (HwNormalizedAutomaticBrightnessController.this.mData.foldScreenModeEnable) {
                    if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                        Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "ambient lux=" + luxOut + ",systemTimeStamp=" + systemTimeStamp + ",frontLux=" + frontLux + ",backLux=" + backLux + ",fusedLux=" + fusedLux + ",disMode=" + HwNormalizedAutomaticBrightnessController.this.mCurrentDisplayMode + ",isIn=" + HwNormalizedAutomaticBrightnessController.this.mData.isInwardFoldScreenLuxEnable);
                    }
                } else if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                    Slog.i(HwNormalizedAutomaticBrightnessController.TAG, "ambient lux=" + luxOut + ",systemTimeStamp=" + systemTimeStamp);
                }
            }
            return luxOut;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getLuxFromMultiSensor(int displayMode, int lux, int frontLux, int backLux, int fusedLux) {
        if (displayMode != 1) {
            return displayMode != 2 ? lux : backLux;
        }
        return fusedLux;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getLuxFromDualSensor(int displayMode, int lux, int frontLux, int backLux, int fusedLux) {
        if (displayMode != 1) {
            if (displayMode == 2) {
                return frontLux;
            }
            if (displayMode == 3) {
                return backLux;
            }
            if (displayMode != 4) {
                return lux;
            }
        }
        return fusedLux;
    }

    public void getUserDragInfo(Bundle data) {
        if (sHwNormalizedScreenAutoBrightnessSpline != null) {
            int targetBrightness = getAutomaticScreenBrightness();
            int i = -3;
            if (this.mMaxBrightnessSetByCryogenic < 255) {
                targetBrightness = targetBrightness >= this.mMaxBrightnessSetByCryogenic ? -3 : targetBrightness;
                Slog.i(TAG, "mMaxBrightnessSetByCryogenic=" + this.mMaxBrightnessSetByCryogenic);
            }
            if (this.mMaxBrightnessSetByThermal < 255) {
                if (targetBrightness < this.mMaxBrightnessSetByThermal) {
                    i = targetBrightness;
                }
                targetBrightness = i;
                Slog.i(TAG, "mMaxBrightnessSetByThermal=" + this.mMaxBrightnessSetByThermal);
            }
            Slog.i(TAG, "getUserDragInfo startBL=" + this.mScreenBrightnessBeforeAdj + ", targetBrightness=" + targetBrightness);
            boolean isDeltaValid = sHwNormalizedScreenAutoBrightnessSpline.isDeltaValid();
            sHwNormalizedScreenAutoBrightnessSpline.resetUserDragLimitFlag();
            data.putBoolean("DeltaValid", isDeltaValid && !this.mIsDragFinished);
            data.putInt("StartBrightness", this.mScreenBrightnessBeforeAdj);
            data.putInt("EndBrightness", targetBrightness);
            data.putInt("FilteredAmbientLight", (int) (this.mAmbientLux + 0.5f));
            data.putBoolean("ProximityPositive", getProximityPositive());
            this.mIsDragFinished = true;
        }
    }

    public void setVideoPlayStatus(boolean isVideoPlay) {
        Slog.i(TAG, "setVideoPlayStatus, isVideoPlay=" + isVideoPlay);
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.setVideoPlayStatus(isVideoPlay);
        }
    }

    public void registerCryogenicProcessor(CryogenicPowerProcessor processor) {
        this.mCryogenicProcessor = processor;
    }

    public float getAutomaticScreenBrightnessAdjustmentNew(int brightness) {
        return MathUtils.constrain(((((float) brightness) * 2.0f) / ((float) this.mData.manualBrightnessMaxLimit)) - 1.0f, (float) DEFAUL_OFFSET_LUX, 1.0f);
    }

    private void updateTouchProximityState(boolean isProximityPositive) {
        if (this.mHwAmbientLuxFilterAlgo != null) {
            if (HWDEBUG) {
                Slog.i(TAG, "LandScapeBrightMode isProximityPositive=" + isProximityPositive + ",bypassTouchProximityResult=" + bypassTouchProximityResult());
            }
            this.mHwAmbientLuxFilterAlgo.updateTouchProximityState(isProximityPositive);
        }
    }

    private boolean bypassTouchProximityResult() {
        if (this.mHwBrightnessSceneController == null) {
            return false;
        }
        if (this.mData.isInwardFoldScreenLuxEnable && this.mHwBrightnessSceneController.mCurrentDisplayMode != 1) {
            return true;
        }
        if (!this.mData.landscapeModeUseTouchProximity || this.mHwBrightnessSceneController.mIsLandscapeModeState) {
            return false;
        }
        return true;
    }

    public void updateBrightnessModeChangeManualState(boolean isBrightnessModeChangeManual) {
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo != null) {
            hwAmbientLuxFilterAlgo.updateBrightnessModeChangeManualState(isBrightnessModeChangeManual);
        }
    }

    @Override // com.android.server.display.HwBrightnessSceneController.Callbacks
    public void setProximitySceneMode(boolean isProximitySceneMode) {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector != null) {
            hwProximitySensorDetector.setProximitySceneMode(isProximitySceneMode);
        }
    }

    private boolean getProximitySceneModeOpened() {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector == null) {
            return false;
        }
        return hwProximitySensorDetector.mIsProximitySceneModeOpened;
    }

    public boolean getFastDarkenDimmingEnable() {
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo == null || brightnessMode != 1 || !hwAmbientLuxFilterAlgo.getFastDarkenDimmingEnable()) {
            return false;
        }
        return true;
    }

    public boolean getKeyguardUnlockedFastDarkenDimmingEnable() {
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo == null || brightnessMode != 1 || !hwAmbientLuxFilterAlgo.getKeyguardUnlockedFastDarkenDimmingEnable()) {
            return false;
        }
        return true;
    }

    private void setFoldDisplayModeEnable(boolean isFoldDisplayModeEnable) {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            hwBrightnessSceneController.setFoldDisplayModeEnable(isFoldDisplayModeEnable);
        }
    }

    @Override // com.android.server.display.HwBrightnessBatteryDetection.Callbacks
    public void updateBrightnessFromBattery(int lowBatteryMaxBrightness) {
        this.mLowBatteryMaxBrightness = lowBatteryMaxBrightness;
        if (HWFLOW) {
            Slog.i(TAG, "mLowBatteryMaxBrightness = " + this.mLowBatteryMaxBrightness);
        }
        if (this.mLightSensorEnabled) {
            this.mCallbacks.updateBrightness();
        }
    }

    @Override // com.android.server.display.HwBrightnessBatteryDetection.Callbacks
    public void updateBrightnessRatioFromBattery(int brightnessRatio) {
        if (HWFLOW) {
            Slog.i(TAG, "updateBrightnessRatioFromBattery, ratio=" + brightnessRatio);
        }
        HwBrightnessPgSceneDetection hwBrightnessPgSceneDetection = this.mHwBrightnessPgSceneDetection;
        if (hwBrightnessPgSceneDetection == null) {
            Slog.w(TAG, "mHwBrightnessPgSceneDetection == null,no update PowerRatio");
        } else {
            hwBrightnessPgSceneDetection.updateBrightnessRatioFromBattery(brightnessRatio);
        }
    }

    private void updateLuxMinMaxBrightness(float lux) {
        this.mLuxMinBrightness = this.mHwHumanFactorBrightness.calculateHumanFactorMinBrightness(lux, 2);
        this.mLuxMaxBrightness = this.mHwHumanFactorBrightness.calculateHumanFactorMaxBrightness(lux, 2);
        if (HWFLOW) {
            Slog.i(TAG, "updateLuxMinMaxBrightness, lux = " + lux + ",mLuxMinBrightness= " + this.mLuxMinBrightness + ",mLuxMaxBrightness=" + this.mLuxMaxBrightness);
        }
    }

    public int getCurrentBrightnessNit() {
        Bundle data = new Bundle();
        int[] result = new int[1];
        Light light = getLight();
        if (light == null) {
            return 0;
        }
        int currentBrightnessNit = 0;
        if (light.getHwBrightnessData("CurrentBrightness", data, result)) {
            int currentBrightness = data.getInt("Brightness");
            currentBrightnessNit = this.mHwBrightnessMapping.convertBrightnessLevelToNit(currentBrightness);
            if (HWDEBUG) {
                Slog.i(TAG, "currentBrightness=" + currentBrightness + ",currentBrightnessNit=" + currentBrightnessNit);
            }
        }
        return currentBrightnessNit;
    }

    public int getTheramlMaxBrightnessNit() {
        HwBrightnessMapping hwBrightnessMapping = this.mHwBrightnessMapping;
        if (hwBrightnessMapping != null) {
            return hwBrightnessMapping.convertBrightnessLevelToNit(this.mMaxBrightnessSetByThermal);
        }
        return 0;
    }

    public int getMinBrightnessNit() {
        return (int) this.mData.screenBrightnessMinNit;
    }

    public int getMaxBrightnessNit() {
        return (int) this.mData.screenBrightnessMaxNit;
    }

    public void setMaxBrightnessNitFromThermal(int brightnessNit) {
        float brightnessNitTmp = (float) brightnessNit;
        if (brightnessNitTmp < this.mData.screenBrightnessMinNit && brightnessNitTmp > 0.0f) {
            Slog.w(TAG, "ThermalMode brightnessNit=" + brightnessNit + " < minNit=" + this.mData.screenBrightnessMinNit);
            brightnessNitTmp = this.mData.screenBrightnessMinNit;
        }
        if (brightnessNitTmp > this.mData.screenBrightnessMaxNit) {
            Slog.w(TAG, "ThermalMode brightnessNit=" + brightnessNit + " > maxNit=" + this.mData.screenBrightnessMaxNit);
            float brightnessNitTmp2 = this.mData.screenBrightnessMaxNit;
        }
        int maxBrightnessLevel = this.mHwBrightnessMapping.convertBrightnessNitToLevel(brightnessNit);
        if (HWFLOW) {
            Slog.i(TAG, "ThermalMode setMaxBrightnessNitFromThermal brightnessNit=" + brightnessNit + "-->maxBrightnessLevel=" + maxBrightnessLevel);
        }
        setMaxBrightnessFromThermal(maxBrightnessLevel);
    }

    private static Light getLight() {
        if (sLight == null) {
            sLight = ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0);
        }
        return sLight;
    }

    public int getAmbientLux() {
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo == null) {
            return 0;
        }
        return hwAmbientLuxFilterAlgo.getCurrentFilteredAmbientLux();
    }

    public int getBrightnessLevel(int lux) {
        HwNormalizedSpline hwNormalizedSpline = sHwNormalizedScreenAutoBrightnessSpline;
        if (hwNormalizedSpline == null) {
            return 0;
        }
        return (int) hwNormalizedSpline.getDefaultBrightness((float) lux);
    }

    public boolean getGameDisableAutoBrightnessModeKeepOffsetEnable() {
        if (!this.mData.gameDisableAutoBrightnessModeEnable) {
            return false;
        }
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            this.mIsGameDisableAutoBrightnessModeKeepOffsetEnable = hwBrightnessSceneController.mIsGameDisableAutoBrightnessModeKeepOffsetEnable;
        }
        return this.mIsGameDisableAutoBrightnessModeKeepOffsetEnable;
    }

    public void setGameDisableAutoBrightnessModeKeepOffsetEnable(boolean isKeepOffsetEnable) {
        this.mIsGameDisableAutoBrightnessModeKeepOffsetEnable = isKeepOffsetEnable;
        this.mHwBrightnessSceneController.mIsGameDisableAutoBrightnessModeKeepOffsetEnable = isKeepOffsetEnable;
    }

    public boolean getGameDisableAutoBrightnessModeStatus() {
        HwBrightnessSceneController hwBrightnessSceneController;
        if (!this.mData.gameDisableAutoBrightnessModeEnable || (hwBrightnessSceneController = this.mHwBrightnessSceneController) == null) {
            return false;
        }
        hwBrightnessSceneController.updateGameDisableAutoBrightnessModeEnable();
        return getGameDisableAutoBrightnessModeEnable();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFrontCameraMaxBrightness() {
        int brightness = 255;
        if (!this.mData.frontCameraMaxBrightnessEnable) {
            this.mFrontCameraMaxBrightness = 255;
            return;
        }
        if (this.mAmbientLuxForFrontCamera < this.mData.frontCameraLuxThreshold && "1".equals(this.mCurCameraId) && !this.mIsFrontCameraAppKeepBrightnessEnable) {
            brightness = this.mData.frontCameraMaxBrightness;
        }
        if (brightness != this.mFrontCameraMaxBrightness) {
            updateFrontCameraBrightnessDimmingEnable();
            this.mFrontCameraMaxBrightness = brightness;
            if (this.mLightSensorEnabled) {
                this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
                if (!this.mIsLocked && !this.mIsPowerOnStatus) {
                    this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(3, (long) this.mData.frontCameraUpdateBrightnessDelayTime);
                }
                if (HWFLOW) {
                    Slog.i(TAG, "updateFrontCameraMaxBrightness, atuo brightness=" + brightness + ",lux=" + this.mAmbientLuxForFrontCamera + ",mKeepBrightnessEnable=" + this.mIsFrontCameraAppKeepBrightnessEnable + ",mIsLocked=" + this.mIsLocked + ",mIsPowerOnStatus=" + this.mIsPowerOnStatus);
                }
            }
        }
    }

    private void updateFrontCameraBrightnessDimmingEnable() {
        this.mIsFrontCameraDimmingEnable = this.mScreenAutoBrightness > this.mData.frontCameraMaxBrightness;
        if (this.mIsFrontCameraDimmingEnable) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(4);
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(4, (long) this.mData.frontCameraUpdateDimmingEnableTime);
        }
        if (HWFLOW) {
            Slog.i(TAG, "mIsFrontCameraDimmingEnable=" + this.mIsFrontCameraDimmingEnable + ",mScreenAutoBrightness=" + this.mScreenAutoBrightness);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFrontCameraBrightnessDimmingEnable(boolean isDimmingEnable) {
        if (HWFLOW) {
            Slog.i(TAG, "setFrontCameraBrightnessDimmingEnable,isDimmingEnable=" + isDimmingEnable);
        }
        this.mIsFrontCameraDimmingEnable = isDimmingEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAutoBrightness(int msg) {
        if (this.mCallbacks == null) {
            Slog.w(TAG, "mCallbacks==null,no updateBrightness");
            return;
        }
        if (HWFLOW) {
            Slog.i(TAG, "updateAutoBrightness for callback,msg=" + msg);
        }
        this.mCallbacks.updateBrightness();
    }

    public boolean getFrontCameraDimmingEnable() {
        return this.mData.frontCameraMaxBrightnessEnable && this.mIsFrontCameraDimmingEnable;
    }

    public void setFrontCameraAppEnableState(boolean isFrontCameraAppKeepBrightnessEnable) {
        if (isFrontCameraAppKeepBrightnessEnable != this.mIsFrontCameraAppKeepBrightnessEnable) {
            if (HWFLOW) {
                Slog.i(TAG, "setFrontCameraAppEnableState=" + isFrontCameraAppKeepBrightnessEnable);
            }
            this.mIsFrontCameraAppKeepBrightnessEnable = isFrontCameraAppKeepBrightnessEnable;
        }
    }

    public void resetCurrentBrightness() {
        resetCurrentBrightnessDelay();
    }

    public void resetCurrentBrightnessFromOff() {
        resetCurrentBrightnessFromOffInternal();
    }

    public void setCurrentBrightnessOff() {
        Light light = getLight();
        if (light == null) {
            Slog.w(TAG, "light == null, setCurrentBrightnessOff failed");
            return;
        }
        int currentBrightness = light.getCurrentBrightness();
        if (currentBrightness > 0) {
            if (HWFLOW) {
                Slog.i(TAG, "setCurrentBrightnessOff,currentBrightness=" + currentBrightness);
            }
            setCurrentBrightnessBypassLights(0, 0, 0, 0);
        } else if (HWFLOW) {
            Slog.i(TAG, "setCurrentBrightnessOff no update,currentBrightness=" + currentBrightness);
        }
    }

    private void resetCurrentBrightnessFromOffInternal() {
        Light light = getLight();
        if (light == null) {
            Slog.w(TAG, "light == null, resetCurrentBrightnessFromOffInternal failed");
            return;
        }
        int currentBrightness = light.getCurrentBrightness();
        if (currentBrightness > 0) {
            if (HWFLOW) {
                Slog.i(TAG, "resetCurrentBrightnessFromOffInternal,currentBrightness=" + currentBrightness);
            }
            setCurrentBrightnessBypassLights(-1, -1, -1, -1);
        } else if (HWFLOW) {
            Slog.i(TAG, "resetCurrentBrightnessFromOffInternal no update,currentBrightness=" + currentBrightness);
        }
    }

    private void resetCurrentBrightnessDelay() {
        Light light = getLight();
        if (light == null) {
            Slog.w(TAG, "light == null, resetCurrentBrightnessDelay failed");
            return;
        }
        int currentBrightness = light.getCurrentBrightness();
        if (currentBrightness > 0) {
            if (HWFLOW) {
                Slog.i(TAG, "resetCurrentBrightness,currentBrightness=" + currentBrightness);
            }
            setCurrentBrightnessBypassLights(0, 0, 0, 0);
            try {
                Thread.sleep(5);
                Slog.i(TAG, "resetCurrentBrightness off sleep, time=5");
            } catch (InterruptedException e) {
                Slog.w(TAG, "resetCurrentBrightnessDelay InterruptedException");
            }
            setCurrentBrightnessBypassLights(-1, -1, -1, -1);
        } else if (HWFLOW) {
            Slog.i(TAG, "resetCurrentBrightness no update,currentBrightness=" + currentBrightness);
        }
    }

    private void setCurrentBrightnessBypassLights(int process, int scene, int level, int animation) {
        Bundle bundle = new Bundle();
        bundle.putInt("ManufactureProcess", process);
        bundle.putInt("Scene", scene);
        bundle.putInt("Level", level);
        bundle.putInt("AnimationTime", animation);
        int ret = HwPowerManager.setHwBrightnessData("ManufactureBrightness", bundle);
        if (ret == 0) {
            Slog.i(TAG, "setCurrentBrightnessBypassLights sucess, level = " + level + ",ret = " + ret);
            return;
        }
        Slog.w(TAG, "setCurrentBrightnessBypassLights failed, level = " + level + ",ret = " + ret);
    }

    public int getAutoModeSeekBarMaxBrightness() {
        if (!this.mData.isAutoModeSeekBarMaxBrightnessBasedLux || this.mAmbientLux <= this.mData.autoModeSeekBarMaxBrightnessLuxTh) {
            return this.mData.manualBrightnessMaxLimit;
        }
        return this.mData.autoModeSeekBarMaxBrightnessValue;
    }

    @Override // com.android.server.display.HwBrightnessSceneController.Callbacks
    public void updateCurrentAutoBrightness() {
        updateAutoBrightness(true, false);
    }

    @Override // com.android.server.display.HwProximitySensorDetector.Callbacks
    public void updateProximityState(boolean isProximityState) {
        HwProximitySensorDetector hwProximitySensorDetector;
        if (this.mCallbacks != null) {
            this.mCallbacks.updateProximityState(isProximityState);
            Slog.i(TAG, "updateProximityState, isProximityState=" + isProximityState);
        }
        if (isProximityDynamicEnable() && !isProximityState && this.mLightSensorEnabled && (hwProximitySensorDetector = this.mHwProximitySensorDetector) != null && hwProximitySensorDetector.isProximitySensorEnabled()) {
            Slog.i(TAG, "DProximity updateProximityState, isProximityState=" + isProximityState);
            updateProximityEnableStateDelayedMsg(isProximityState);
        }
    }

    @Override // com.android.server.display.HwProximitySensorDetector.Callbacks
    public void updateFirstBrightnessAfterProximityNegative(boolean isFirstBrightnessAfterProximityNegative) {
        this.mFirstBrightnessAfterProximityNegative = isFirstBrightnessAfterProximityNegative;
        Slog.i(TAG, "updateFirstBrightnessAfterProximityNegative,enable=" + isFirstBrightnessAfterProximityNegative);
    }

    private void updateWakeupFromSleep(boolean isWakeupFromSleep) {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector != null) {
            hwProximitySensorDetector.updateWakeupFromSleep(isWakeupFromSleep);
        }
    }

    private boolean getHdrModeEnable() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            return hwBrightnessSceneController.getHdrModeEnable();
        }
        return false;
    }

    private boolean getGameDisableAutoBrightnessModeEnable() {
        HwBrightnessSceneController hwBrightnessSceneController = this.mHwBrightnessSceneController;
        if (hwBrightnessSceneController != null) {
            return hwBrightnessSceneController.getGameDisableAutoBrightnessModeEnable();
        }
        return false;
    }

    private boolean getProximityPositive() {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector == null) {
            return false;
        }
        return hwProximitySensorDetector.getProximityPositive();
    }

    private void updateProximitySensorEnabled(boolean isProximityEnable, boolean isDozing) {
        boolean z = true;
        if (this.mData.proximitySceneModeEnable) {
            if (!getProximitySceneModeOpened() || !isProximityEnable || isDozing) {
                z = false;
            }
            updateProximitySensorEnabledMsg(z);
        } else if (isProximityDynamicEnable()) {
            if (!this.mData.allowLabcUseProximity || !this.mData.isDynamicEnableProximity || !isProximityEnable || isDozing) {
                z = false;
            }
            updateDynamicProximitySensorEnabledMsg(z);
        } else {
            if (!this.mData.allowLabcUseProximity || this.mData.isDynamicEnableProximity || !isProximityEnable || isDozing) {
                z = false;
            }
            updateProximitySensorEnabledMsg(z);
        }
    }

    private boolean isProximityDynamicEnable() {
        return this.mData.allowLabcUseProximity && this.mData.isDynamicEnableProximity;
    }

    private void updateDynamicProximitySensorEnabledMsg(boolean isProximityEnable) {
        if (!isProximityEnable) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(6);
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
            if (HWDEBUG) {
                Slog.i(TAG, "updateDynamicProximitySensorEnabledMsg, clear delayedMsg, DisableProximity");
            }
            updateProximitySensorEnabledMsg(false);
        }
    }

    private boolean isUpdateAutoBrightnessDelayedForDarkenLuxChange() {
        if (this.mHwProximitySensorDetector != null && isProximityDynamicEnable() && !this.mHwProximitySensorDetector.isProximitySensorEnabled() && this.mAmbientLux > this.mAmbientLuxTmp && !this.mWakeupForFirstAutoBrightness && this.mUpdateAutoBrightnessCount > 0) {
            return true;
        }
        return false;
    }

    private void updateProximityEnableStateDelayedMsg(boolean isProximityEnable) {
        if (isProximityEnable) {
            updateProximityStateEnableDelayedMsg();
        } else {
            updateProximityStateDisableDelayedMsg();
        }
    }

    private void updateProximityStateEnableDelayedMsg() {
        if (HWFLOW) {
            Slog.i(TAG, "DProximity updateProximityStateEnableDelayedMsg, set MSG_OPEN_PROXIMITY delayTimeMs=" + this.mData.enableProximityDelayTime);
        }
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(6);
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
        this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(6, (long) this.mData.enableProximityDelayTime);
    }

    private void updateProximityStateDisableDelayedMsg() {
        if (getProximityPositiveEnable()) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
            Slog.i(TAG, "DProximity updateProximityStateDisableDelayedMsg, positive --> clear MSG_CLOSE_PROXIMITY.");
            return;
        }
        Slog.i(TAG, "DProximity updateProximityStateDisableDelayedMsg, set MSG_CLOSE_PROXIMITY delayTimeMs=" + this.mData.disableProximityDelayTime);
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
        this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(5, (long) this.mData.disableProximityDelayTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDisableProximityDelayedMsg() {
        if (getProximityPositiveEnable()) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
            if (HWFLOW) {
                Slog.i(TAG, "DProximity handleDisableProximityDelayedMsg, positive --> clear MSG_CLOSE_PROXIMITY.");
                return;
            }
            return;
        }
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(6);
        if (HWFLOW) {
            Slog.i(TAG, "DProximity handleDisableProximityDelayedMsg, DisableProximity");
        }
        updateProximitySensorEnabledMsg(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEnableProximityDelayedMsg() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
        if (HWFLOW) {
            Slog.i(TAG, "DProximity handleEnableProximityDelayedMsg, EnableProximity");
        }
        updateProximitySensorEnabledMsg(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAutoBrightnessForLuxChange(float ambientLux) {
        if (isProximityDynamicEnable()) {
            if (HWFLOW) {
                Slog.i(TAG, "DProximity updateAutoBrightnessForLuxChange,mAmbientLux=" + this.mAmbientLux + ",mAmbientLuxTmp=" + this.mAmbientLuxTmp + ",ambientLux=" + ambientLux + ",mWakeupForFirstAutoBrightness=" + this.mWakeupForFirstAutoBrightness);
            }
            HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
            if (hwProximitySensorDetector != null) {
                this.mProximity = hwProximitySensorDetector.mProximity;
            }
            if (this.mProximity == 1 && !this.mWakeupForFirstAutoBrightness && ambientLux < this.mAmbientLux) {
                this.mHwAmbientLuxFilterAlgo.updateNeedToUpdateBrightnessFlag();
                Slog.i(TAG, "DProximity PROXIMITY_POSITIVE --> no need updateAutoBrightness");
                return;
            }
        }
        this.mAmbientLux = ambientLux;
        updateDarkenAmbientEnable();
        if (this.mData.luxMinMaxBrightnessEnable) {
            updateLuxMinMaxBrightness(ambientLux);
        }
        if (HWFLOW) {
            Slog.i(TAG, "need to update brightness: mAmbientLux=" + this.mAmbientLux + ",mProximity=" + this.mProximity + ",mWakeupForFirstAutoBrightness=" + this.mWakeupForFirstAutoBrightness);
        }
        this.mHwAmbientLuxFilterAlgo.updateNeedToUpdateBrightnessFlag();
        updateDarkTimeDelayFromBrightnessEnable();
        updateSecondDarkenModeNoResponseLongEnable();
        updateAutoBrightness(true, false);
    }

    private void updateAutoBrightnessForLuxChangeDelayedMsg() {
        if (HWFLOW) {
            Slog.i(TAG, "DProximity updateAutoBrightnessForLuxChangeDelayedMsg, updateBrightness delayTimeMs=" + this.mData.updateDarkAmbientLuxDelayTime);
        }
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(7);
        this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(7, (long) this.mData.updateDarkAmbientLuxDelayTime);
    }

    private boolean getProximityPositiveEnable() {
        HwProximitySensorDetector hwProximitySensorDetector = this.mHwProximitySensorDetector;
        if (hwProximitySensorDetector == null) {
            return false;
        }
        this.mProximity = hwProximitySensorDetector.mProximity;
        if (this.mProximity == 1) {
            return true;
        }
        return false;
    }

    public long getLightSensorEnableTime() {
        if (!Display.isDozeState(this.mPowerState) || !this.mData.fingerprintOffUnlockEnable) {
            return this.mLightSensorEnableTime;
        }
        if (HWFLOW) {
            Slog.i(TAG, "fingerprintOffUnlockEnable=" + this.mData.fingerprintOffUnlockEnable);
        }
        return SystemClock.uptimeMillis();
    }
}
