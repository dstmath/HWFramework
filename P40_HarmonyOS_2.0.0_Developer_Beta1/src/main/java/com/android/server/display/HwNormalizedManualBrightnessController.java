package com.android.server.display;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.HwLog;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.HwBrightnessBatteryDetection;
import com.android.server.display.HwBrightnessPgSceneDetection;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.display.HwLightSensorListener;
import com.android.server.display.ManualBrightnessController;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwNormalizedManualBrightnessController extends ManualBrightnessController implements HwLightSensorListener.LightSensorCallbacks, HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks, HwBrightnessBatteryDetection.Callbacks {
    private static final int AMBIENT_LIGHT_MONITOR_SAMPLING_INTERVAL_MS = 2000;
    private static final int BACK_SENSOR_REPORT_TIMEOUT = 300;
    private static final float DEFAULT_BRIGHTNESS = 100.0f;
    private static final int DEFAULT_BRIGHTNESS_NIT = 20;
    private static final int DEFAULT_INIT_BRIGHTNESS = -1;
    private static final float DEFAULT_INIT_BRIGHTNESS_FLOAT = -1.0f;
    private static final int DEFAULT_INIT_LUX = -1;
    private static final float DEFAULT_POWER_SAVING_RATIO = 1.0f;
    private static final String FRONT_CAMERA = "1";
    private static final int HDR_ENTER = 33;
    private static final int HDR_EXIT = 32;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final int MSG_FRONT_CAMERA_UPDATE_BRIGHTNESS = 2;
    private static final int MSG_FRONT_CAMERA_UPDATE_DIMMING_ENABLE = 3;
    private static final int MSG_SENSOR_TIMEOUT = 1;
    private static final int OUTDOOR = 2;
    private static final int PERCENTAGE_RATIO = 100;
    private static final float ROUND_UP_VALUE = 0.5f;
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final String TAG = "HwNormalizedManualBrightnessController";
    private int mAlgoSmoothLightValue;
    private int mAutoBrightnessLevel = -1;
    private int mBackSensorCoverBrightness = -1;
    private int mBackSensorCoverLux = -1;
    private int mBrightnessHighLevelSetByApp = -1;
    private boolean mBrightnessHighLevelSetByAppAnimationEnable = false;
    private boolean mBrightnessHighLevelSetByAppEnable = false;
    private CameraManager.AvailabilityCallback mCameraAvailableCallback = new CameraManager.AvailabilityCallback() {
        /* class com.android.server.display.HwNormalizedManualBrightnessController.AnonymousClass1 */

        @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
        public void onCameraAvailable(String cameraId) {
            if ("1".equals(cameraId)) {
                HwNormalizedManualBrightnessController.this.mCurCameraId = null;
                HwNormalizedManualBrightnessController.this.updateFrontCameraMaxBrightness();
            }
        }

        @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
        public void onCameraUnavailable(String cameraId) {
            if ("1".equals(cameraId)) {
                HwNormalizedManualBrightnessController.this.mCurCameraId = cameraId;
                HwNormalizedManualBrightnessController.this.updateFrontCameraMaxBrightness();
            }
        }
    };
    private CameraManager mCameraManager;
    private final Context mContext;
    private String mCurCameraId = null;
    private int mCurrentUserId = 0;
    private final HwBrightnessXmlLoader.Data mData = HwBrightnessXmlLoader.getData();
    private float mDefaultBrightness = DEFAULT_BRIGHTNESS;
    private int mDefaultBrightnessNit = 20;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private int mFrontCameraMaxBrightness = 255;
    private Handler mHandler;
    private HwBrightnessBatteryDetection mHwBrightnessBatteryDetection;
    private HwBrightnessMapping mHwBrightnessMapping;
    private HwBrightnessPgSceneDetection mHwBrightnessPgSceneDetection;
    private final HwNormalizedManualBrightnessBatteryHandler mHwNormalizedManualBrightnessBatteryHandler;
    private boolean mIsAmbientLuxTimeOut;
    private boolean mIsAmbientLuxValid;
    private final boolean mIsBackSensorEnable;
    private boolean mIsFrontCameraAppKeepBrightnessEnable;
    private boolean mIsFrontCameraDimmingEnable = false;
    private boolean mIsHdrEnable;
    private boolean mIsLightSensorEnable;
    private boolean mIsManualModeAnimationEnable = false;
    private boolean mIsManualModeEnable;
    private boolean mIsManualPowerSavingAnimationEnable = false;
    private boolean mIsManualPowerSavingBrighnessLineDisableForDemo = false;
    private boolean mIsManualPowerSavingEnable = false;
    private boolean mIsThermalModeAnimationEnable = false;
    private boolean mIsThermalModeEnable = false;
    private boolean mIsUsePowerSavingModeCurveEnable = false;
    private long mLastAmbientLightToMonitorTime;
    private float mLastAmbientLuxForFrontCamera = 0.0f;
    private HwLightSensorListener mLightSensorListener = null;
    private int mLowBatteryMaxBrightness = 255;
    private int mManualAmbientLux;
    private float mManualAmbientLuxForCamera = DEFAULT_INIT_BRIGHTNESS_FLOAT;
    private int mManualBrightness = -1;
    private int mManualBrightnessLog = -1;
    private int mManualBrightnessOut = -1;
    private final HandlerThread mManualBrightnessProcessThread;
    private int mMaxBrightnessSetByThermal = 255;
    private HwNormalizedManualBrightnessThresholdDetector mOutdoorDetector = null;
    private int mOutdoorScene;
    private float mPowerRatio = 1.0f;
    private List<float[]> mPowerSavingBrighnessLinePointsList = null;

    static {
        boolean z = false;
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        HWDEBUG = z;
    }

    public HwNormalizedManualBrightnessController(ManualBrightnessController.ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
        super(callbacks);
        this.mLightSensorListener = new HwLightSensorListener(context, this, sensorManager, this.mData.lightSensorRateMills);
        this.mOutdoorDetector = new HwNormalizedManualBrightnessThresholdDetector(this.mData);
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        parseManualModePowerSavingCurve(SystemProperties.get("ro.config.blight_power_curve", ""));
        this.mContext = context;
        this.mHwBrightnessPgSceneDetection = new HwBrightnessPgSceneDetection(this, this.mData.pgSceneDetectionDarkenDelayTime, this.mData.pgSceneDetectionBrightenDelayTime, this.mContext);
        this.mHwBrightnessMapping = new HwBrightnessMapping(this.mData.brightnessMappingPoints);
        this.mIsBackSensorEnable = this.mLightSensorListener.isBackSensorEnable();
        if (this.mData.backSensorCoverModeEnable || this.mData.frontCameraMaxBrightnessEnable) {
            this.mHandler = new HwNormalizedManualBrightnessHandler();
        }
        if (this.mData.manualPowerSavingBrighnessLineDisableForDemo) {
            this.mIsManualPowerSavingBrighnessLineDisableForDemo = isDemoVersion();
        }
        this.mManualBrightnessProcessThread = new HandlerThread(TAG);
        this.mManualBrightnessProcessThread.start();
        this.mHwNormalizedManualBrightnessBatteryHandler = new HwNormalizedManualBrightnessBatteryHandler(this.mManualBrightnessProcessThread.getLooper());
        if (this.mData.batteryModeEnable || this.mData.powerSavingModeBatteryLowLevelEnable) {
            this.mHwNormalizedManualBrightnessBatteryHandler.post(new Runnable() {
                /* class com.android.server.display.$$Lambda$HwNormalizedManualBrightnessController$GVu8D1MFGQP9YDO7lwf4bjCc3nA */

                @Override // java.lang.Runnable
                public final void run() {
                    HwNormalizedManualBrightnessController.this.lambda$new$0$HwNormalizedManualBrightnessController();
                }
            });
        }
    }

    public /* synthetic */ void lambda$new$0$HwNormalizedManualBrightnessController() {
        this.mHwBrightnessBatteryDetection = new HwBrightnessBatteryDetection(this, this.mContext);
    }

    private void parseManualModePowerSavingCurve(String powerSavingCure) {
        String[] powerSavingPoints;
        if (powerSavingCure == null || powerSavingCure.length() <= 0) {
            Slog.i(TAG, "powerSavingCure == null");
            return;
        }
        List<float[]> list = this.mPowerSavingBrighnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mPowerSavingBrighnessLinePointsList = new ArrayList();
        }
        for (String str : powerSavingCure.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            try {
                String[] point = str.split(",");
                this.mPowerSavingBrighnessLinePointsList.add(new float[]{Float.parseFloat(point[0]), Float.parseFloat(point[1])});
            } catch (NumberFormatException e) {
                this.mPowerSavingBrighnessLinePointsList.clear();
                Slog.w(TAG, "parse ManualPowerSaving curve error");
                return;
            }
        }
        List<float[]> list2 = this.mPowerSavingBrighnessLinePointsList;
        if (list2 != null) {
            int listSize = list2.size();
            for (int listIndex = 0; listIndex < listSize; listIndex++) {
                float[] curPoint = this.mPowerSavingBrighnessLinePointsList.get(listIndex);
                if (HWFLOW) {
                    Slog.i(TAG, "ManualPowerSavingPointsList brightnessNit=" + curPoint[0] + ",discount=" + curPoint[1]);
                }
            }
        }
    }

    private static boolean wantScreenOn(int state) {
        if (state == 2 || state == 3) {
            return true;
        }
        return false;
    }

    public void updatePowerState(int state, boolean isPowerOnEnable) {
        if (this.mIsManualModeEnable != isPowerOnEnable) {
            if (HWFLOW) {
                Slog.i(TAG, "HBM SensorEnable change " + this.mIsManualModeEnable + " -> " + isPowerOnEnable);
            }
            this.mIsManualModeEnable = isPowerOnEnable;
        }
        boolean z = this.mIsManualModeEnable;
        if (z) {
            setLightSensorEnabled(wantScreenOn(state));
        } else {
            setLightSensorEnabled(z);
        }
        if (this.mData.frontCameraMaxBrightnessEnable && this.mCameraManager == null) {
            this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
            this.mCameraManager.registerAvailabilityCallback(this.mCameraAvailableCallback, (Handler) null);
            Slog.i(TAG, "registerAvailabilityCallback for manual frontCameraMaxBrightness");
        }
    }

    private void setLightSensorEnabled(boolean isLightSensorEnable) {
        if (isLightSensorEnable) {
            if (!this.mIsLightSensorEnable) {
                this.mIsLightSensorEnable = true;
                this.mLightSensorListener.enableSensor();
                this.mIsAmbientLuxValid = false;
                this.mIsAmbientLuxTimeOut = false;
                this.mBackSensorCoverLux = -1;
                this.mBackSensorCoverBrightness = -1;
                Handler handler = this.mHandler;
                if (handler != null) {
                    handler.sendEmptyMessageDelayed(1, 300);
                }
                if (HWFLOW) {
                    Slog.i(TAG, "ManualMode sensor enable");
                }
            }
            boolean isPgRecognitionListenerRegisted = this.mHwBrightnessPgSceneDetection.getPgRecognitionListenerRegisted();
            if (this.mData.manualPowerSavingBrighnessLineEnable && !isPgRecognitionListenerRegisted) {
                this.mHwBrightnessPgSceneDetection.registerPgRecognitionListener(this.mContext);
                if (HWFLOW) {
                    Slog.i(TAG, "PowerSaving Manul in registerPgBLightSceneChangedListener,=" + this.mHwBrightnessPgSceneDetection.getPgRecognitionListenerRegisted());
                }
            }
        } else if (this.mIsLightSensorEnable) {
            this.mIsLightSensorEnable = false;
            this.mLightSensorListener.disableSensor();
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
            if (this.mData.frontCameraMaxBrightnessEnable && this.mIsFrontCameraDimmingEnable) {
                this.mIsFrontCameraDimmingEnable = false;
            }
            Handler handler2 = this.mHandler;
            if (handler2 != null) {
                handler2.removeMessages(1);
            }
            if (HWFLOW) {
                Slog.i(TAG, "ManualMode sensor disable");
            }
        }
        this.mLastAmbientLightToMonitorTime = 0;
    }

    public void updateManualBrightness(int brightness) {
        this.mManualBrightness = brightness;
        this.mManualBrightnessOut = brightness;
    }

    private static boolean isDemoVersion() {
        String vendor2 = SystemProperties.get("ro.hw.vendor", "");
        String country = SystemProperties.get("ro.hw.country", "");
        Slog.i(TAG, "vendor:" + vendor2 + ",country:" + country);
        return "demo".equalsIgnoreCase(vendor2) || "demo".equalsIgnoreCase(country);
    }

    @Override // com.android.server.display.HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks
    public void updateStateRecognition(boolean isPowerSavingCure, int appType) {
        if (!this.mData.manualPowerSavingBrighnessLineEnable || !this.mIsManualModeEnable || this.mIsManualPowerSavingBrighnessLineDisableForDemo) {
            this.mIsManualPowerSavingAnimationEnable = false;
            this.mIsManualPowerSavingEnable = false;
            return;
        }
        this.mIsManualPowerSavingEnable = isPowerSavingCure;
        this.mIsManualPowerSavingAnimationEnable = getPowerSavingModeBrightnessChangeEnable(this.mManualBrightness, isPowerSavingCure);
        float powerRatio = covertBrightnessToPowerRatio(this.mManualBrightness);
        int tempBrightness = (int) (((float) this.mManualBrightness) * powerRatio);
        if (this.mData.brightnessLevelToNitMappingEnable) {
            tempBrightness = convertNitToBrightnessLevelFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, ((float) convertBrightnessLevelToNit(this.mManualBrightness)) * powerRatio, this.mManualBrightness);
        }
        if (isPowerSavingCure) {
            HwLog.dubaie("DUBAI_TAG_BACKLIGHT_DISCOUNT", "ratio=" + ((int) (DEFAULT_BRIGHTNESS * powerRatio)));
        }
        int pgModeBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(tempBrightness);
        if (pgModeBrightness != this.mManualBrightness) {
            powerRatio = this.mHwBrightnessPgSceneDetection.getPgPowerModeRatio();
            tempBrightness = pgModeBrightness;
        }
        if (this.mManualBrightnessLog != tempBrightness) {
            int brightnessNit = convertBrightnessLevelToNit(this.mManualBrightness);
            if (HWFLOW) {
                Slog.i(TAG, "PowerSaving,ManualMode mManualBrightness=" + this.mManualBrightness + ",brightnessNit=" + brightnessNit + ",powerRatio=" + powerRatio + ",maxNit=" + this.mData.screenBrightnessMaxNit + ",MinNit=" + this.mData.screenBrightnessMinNit + ",isPowerSavingCure=" + isPowerSavingCure + ",appType=" + appType);
            }
            this.mManualBrightnessLog = tempBrightness;
            this.mCallbacks.updateManualBrightnessForLux();
        }
    }

    private boolean getPowerSavingModeBrightnessChangeEnable(int brightness, boolean isUsePowerSavingModeCurveEnable) {
        boolean powerSavingModeBrightnessChangeEnable = false;
        if (this.mIsUsePowerSavingModeCurveEnable != isUsePowerSavingModeCurveEnable) {
            float powerRatio = covertBrightnessLevelToPowerRatio(brightness);
            int tempBrightness = (int) (((float) brightness) * powerRatio);
            if (this.mData.brightnessLevelToNitMappingEnable) {
                tempBrightness = convertNitToBrightnessLevelFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, ((float) convertBrightnessLevelToNit(brightness)) * powerRatio, brightness);
            }
            if (brightness != tempBrightness && !HwServiceFactory.isCoverClosed()) {
                powerSavingModeBrightnessChangeEnable = true;
                if (HWFLOW) {
                    Slog.i(TAG, "PowerSaving Enable=true,Pgbrightness=" + tempBrightness + ",brightness=" + brightness);
                }
            }
        }
        this.mIsUsePowerSavingModeCurveEnable = isUsePowerSavingModeCurveEnable;
        return powerSavingModeBrightnessChangeEnable;
    }

    private float covertBrightnessLevelToPowerRatio(int brightness) {
        if ((!this.mData.manualMode || this.mManualBrightness < this.mData.manualBrightnessMaxLimit) && this.mData.manualPowerSavingBrighnessLineEnable) {
            return getPowerSavingRatio(convertBrightnessLevelToNit(this.mManualBrightness));
        }
        return 1.0f;
    }

    public int getManualBrightness() {
        int i = this.mBrightnessHighLevelSetByApp;
        if (i > 0) {
            return i;
        }
        if (this.mData.backSensorCoverModeEnable) {
            if (this.mBackSensorCoverBrightness > 0) {
                updateBrightenssDbFromCoverModeBrightness();
                return this.mBackSensorCoverBrightness;
            } else if (!this.mIsAmbientLuxValid && !this.mIsAmbientLuxTimeOut && HwServiceFactory.isCoverClosed()) {
                this.mAutoBrightnessLevel = -1;
                return -1;
            }
        }
        float powerSavingRatio = covertBrightnessToPowerRatio(this.mManualBrightness);
        if (Math.abs(this.mPowerRatio - powerSavingRatio) > SMALL_VALUE) {
            int brightnessNit = convertBrightnessLevelToNit(this.mManualBrightness);
            this.mPowerRatio = powerSavingRatio;
            if (HWFLOW) {
                Slog.i(TAG, "PowerSaving powerSavingRatio=" + powerSavingRatio + ",mManualBrightness=" + this.mManualBrightness + ",brightnessNit=" + brightnessNit);
            }
        }
        int manualBrightnessTmp = (int) (((float) this.mManualBrightness) * powerSavingRatio);
        if (this.mData.brightnessLevelToNitMappingEnable) {
            manualBrightnessTmp = convertNitToBrightnessLevelFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, ((float) convertBrightnessLevelToNit(this.mManualBrightness)) * powerSavingRatio, this.mManualBrightness);
        }
        this.mManualBrightnessOut = getValidBrightness(manualBrightnessTmp);
        updateManualBrightnessFromLux(this.mManualBrightnessOut);
        int pgModeBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(this.mManualBrightnessOut);
        if (this.mData.pgModeBrightnessMappingEnable && this.mManualBrightnessOut > this.mData.manualBrightnessMaxLimit) {
            pgModeBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(pgModeBrightness);
        }
        if (pgModeBrightness != this.mManualBrightnessOut) {
            this.mPowerRatio = this.mHwBrightnessPgSceneDetection.getPgPowerModeRatio();
            this.mManualBrightnessOut = pgModeBrightness;
            if (HWFLOW) {
                Slog.i(TAG, "PG_POWER_SAVE_MODE mOut=" + this.mManualBrightnessOut + ",mPowerRatio=" + this.mPowerRatio);
            }
        }
        this.mManualBrightnessOut = updateManualBrightnessFromLimit(this.mManualBrightnessOut);
        return this.mManualBrightnessOut;
    }

    private void updateBrightenssDbFromCoverModeBrightness() {
        if (this.mAutoBrightnessLevel != this.mBackSensorCoverBrightness && Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId) == 1) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", this.mBackSensorCoverBrightness, this.mCurrentUserId);
            if (HWFLOW) {
                Slog.i(TAG, "coverLevel=" + this.mAutoBrightnessLevel + ",mBack=" + this.mBackSensorCoverBrightness);
            }
        }
        this.mAutoBrightnessLevel = this.mBackSensorCoverBrightness;
    }

    private void updateManualBrightnessFromLux(int munalBrightness) {
        if (!this.mData.manualMode || this.mIsHdrEnable) {
            this.mManualBrightnessOut = munalBrightness;
            if (HWFLOW) {
                Slog.i(TAG, "mManualBrightnessOut=" + this.mManualBrightnessOut + ",mData.manualMode=" + this.mData.manualMode);
            }
        } else if (this.mManualBrightnessOut >= this.mData.manualBrightnessMaxLimit) {
            float defaultBrightness = getDefaultBrightnessLevelNew(this.mData.defaultBrightnessLinePoints, (float) this.mManualAmbientLux);
            if (this.mOutdoorScene == 2) {
                int manualBrightnessTmpMin = munalBrightness < this.mData.manualBrightnessMaxLimit ? munalBrightness : this.mData.manualBrightnessMaxLimit;
                this.mManualBrightnessOut = manualBrightnessTmpMin > ((int) defaultBrightness) ? manualBrightnessTmpMin : (int) defaultBrightness;
                if (HWFLOW) {
                    Slog.i(TAG, "mManualBrightnessOut=" + this.mManualBrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                    return;
                }
                return;
            }
            this.mManualBrightnessOut = munalBrightness < this.mData.manualBrightnessMaxLimit ? munalBrightness : this.mData.manualBrightnessMaxLimit;
            if (HWFLOW) {
                Slog.i(TAG, "mManualBrightnessOut1=" + this.mManualBrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
            }
        }
    }

    private int updateManualBrightnessFromLimit(int brightness) {
        int manualbrightnessOut = brightness;
        if (manualbrightnessOut > this.mMaxBrightnessSetByThermal) {
            if (HWFLOW) {
                Slog.i(TAG, "ThermalMode Org=" + manualbrightnessOut + ",mThermal=" + this.mMaxBrightnessSetByThermal);
            }
            manualbrightnessOut = this.mMaxBrightnessSetByThermal;
        }
        if (manualbrightnessOut > this.mLowBatteryMaxBrightness && this.mData.batteryModeEnable) {
            if (HWFLOW) {
                Slog.i(TAG, "mOut=" + manualbrightnessOut + ",mLowBatteryMax=" + this.mLowBatteryMaxBrightness);
            }
            manualbrightnessOut = this.mLowBatteryMaxBrightness;
        }
        if (!this.mData.luxMinMaxBrightnessEnable || manualbrightnessOut <= this.mFrontCameraMaxBrightness) {
            return manualbrightnessOut;
        }
        if (HWFLOW) {
            Slog.i(TAG, "mOut=" + manualbrightnessOut + ",mFrontCameraMaxBrightness=" + this.mFrontCameraMaxBrightness);
        }
        return this.mFrontCameraMaxBrightness;
    }

    private int getValidBrightness(int brightness) {
        int brightnessOut = brightness;
        if (brightnessOut < 4) {
            Slog.w(TAG, "warning mManualBrightness < min,brightnessOut=" + brightnessOut);
            brightnessOut = 4;
        }
        if (brightnessOut <= 255) {
            return brightnessOut;
        }
        Slog.w(TAG, "warning mManualBrightness > max,brightnessOut=" + brightnessOut);
        return 255;
    }

    public int getMaxBrightnessForSeekbar(int autoModeSeekBarMaxBrightness) {
        if (this.mIsHdrEnable) {
            return 255;
        }
        if (this.mData.isAutoModeSeekBarMaxBrightnessBasedLux && !this.mIsLightSensorEnable && autoModeSeekBarMaxBrightness > 0) {
            return autoModeSeekBarMaxBrightness;
        }
        if (this.mData.manualMode) {
            return this.mData.manualBrightnessMaxLimit;
        }
        return 255;
    }

    @Override // com.android.server.display.HwLightSensorListener.LightSensorCallbacks
    public void processSensorData(long timeInMs, int lux, int cct) {
        this.mIsAmbientLuxValid = true;
        boolean isNeedUpdateManualBrightness = false;
        if (this.mData.backSensorCoverModeEnable && needUpdateBrightWhileCoverClosed(lux)) {
            isNeedUpdateManualBrightness = true;
        }
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForHbm();
        if (this.mOutdoorDetector.getLuxChangedFlagForHbm()) {
            isNeedUpdateManualBrightness = true;
            this.mIsManualModeAnimationEnable = true;
        }
        this.mManualAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForHbm();
        if (this.mData.frontCameraMaxBrightnessEnable) {
            this.mManualAmbientLuxForCamera = (float) ((int) this.mOutdoorDetector.getAmbientLuxForFrontCamera());
            if ((this.mLastAmbientLuxForFrontCamera >= this.mData.frontCameraLuxThreshold && this.mManualAmbientLuxForCamera < this.mData.frontCameraLuxThreshold) || (this.mLastAmbientLuxForFrontCamera < this.mData.frontCameraLuxThreshold && this.mManualAmbientLuxForCamera >= this.mData.frontCameraLuxThreshold)) {
                Slog.i(TAG, "updateFrontCameraMaxBrightness mLastAmbientLuxForFrontCamera=" + this.mLastAmbientLuxForFrontCamera + ",mManualAmbientLuxForCamera=" + this.mManualAmbientLuxForCamera);
                updateFrontCameraMaxBrightness();
                this.mLastAmbientLuxForFrontCamera = this.mManualAmbientLuxForCamera;
            }
        }
        if (isNeedUpdateManualBrightness) {
            this.mCallbacks.updateManualBrightnessForLux();
            this.mOutdoorDetector.setLuxChangedFlagForHbm();
            if (HWFLOW) {
                Slog.i(TAG, "mManualAmbientLux=" + this.mManualAmbientLux + ",mIsManualModeAnimationEnable=" + this.mIsManualModeAnimationEnable);
            }
        } else {
            this.mIsManualModeAnimationEnable = false;
        }
        sendAmbientLightToMonitor(timeInMs, (float) lux);
        sendDefaultBrightnessToMonitor();
    }

    private final class HwNormalizedManualBrightnessHandler extends Handler {
        private HwNormalizedManualBrightnessHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwNormalizedManualBrightnessController.this.updateBrightnessIfNoAmbientLuxReported();
            } else if (i == 2) {
                HwNormalizedManualBrightnessController.this.updateBrightness(2);
            } else if (i == 3) {
                HwNormalizedManualBrightnessController.this.setFrontCameraBrightnessDimmingEnable(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBrightnessIfNoAmbientLuxReported() {
        if (!this.mIsAmbientLuxValid) {
            this.mIsAmbientLuxTimeOut = true;
            if (HWFLOW) {
                Slog.i(TAG, "BackSensorCoverMode sensor doesn't report lux in 300ms");
            }
            this.mCallbacks.updateManualBrightnessForLux();
            updateAutoBrightnessDbOnCoverClosed();
        }
    }

    private void updateAutoBrightnessDbOnCoverClosed() {
        int i;
        if (HwServiceFactory.isCoverClosed()) {
            int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
            int autoBrightnessDb = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
            if (brightnessMode == 1 && (i = this.mManualBrightnessOut) > 0 && autoBrightnessDb != i) {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", this.mManualBrightnessOut, this.mCurrentUserId);
                if (HWFLOW) {
                    Slog.i(TAG, "LabcCoverMode mBackSensorCoverBrightness=" + this.mBackSensorCoverBrightness + ",mManualBrightnessOut=" + this.mManualBrightnessOut);
                }
            }
        }
    }

    private boolean isManualMode() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId) == 0;
    }

    private boolean needUpdateBrightWhileCoverClosed(int mixedSensorValue) {
        if (!this.mIsBackSensorEnable) {
            return false;
        }
        if (!HwServiceFactory.isCoverClosed()) {
            if (this.mBackSensorCoverBrightness > 0) {
                boolean isManualMode = isManualMode();
                if (HWFLOW) {
                    Slog.i(TAG, "BackSensorCoverMode cover open, isManualMode=" + isManualMode);
                }
                this.mBackSensorCoverLux = -1;
                this.mBackSensorCoverBrightness = -1;
                if (isManualMode) {
                    return true;
                }
            }
            return false;
        }
        int backSensorValue = this.mLightSensorListener.getBackSensorValue();
        int i = this.mBackSensorCoverLux;
        if (i <= backSensorValue) {
            i = backSensorValue;
        }
        this.mBackSensorCoverLux = i;
        int i2 = this.mBackSensorCoverLux;
        if (i2 <= mixedSensorValue) {
            i2 = mixedSensorValue;
        }
        this.mBackSensorCoverLux = i2;
        if (this.mData.backSensorCoverModeMinLuxInRing > 0 && isPhoneInRing()) {
            this.mBackSensorCoverLux = this.mBackSensorCoverLux > this.mData.backSensorCoverModeMinLuxInRing ? this.mBackSensorCoverLux : this.mData.backSensorCoverModeMinLuxInRing;
        }
        int backSensorCoverBrightness = (int) getDefaultBrightnessLevelNew(this.mData.backSensorCoverModeBrighnessLinePoints, (float) this.mBackSensorCoverLux);
        boolean isManualMode2 = isManualMode();
        if (isManualMode2 && backSensorCoverBrightness < this.mManualBrightness) {
            backSensorCoverBrightness = -1;
        }
        int backSensorCoverBrightness2 = updateBackSensorCoverBrightness(backSensorCoverBrightness);
        if (backSensorCoverBrightness2 == this.mBackSensorCoverBrightness) {
            return false;
        }
        if (HWFLOW) {
            Slog.i(TAG, "BackSensorCoverMode mixed=" + mixedSensorValue + ", back=" + backSensorValue + ", lux=" + this.mBackSensorCoverLux + ", bright=" + backSensorCoverBrightness2 + ", isManualMode=" + isManualMode2);
        }
        this.mBackSensorCoverBrightness = backSensorCoverBrightness2;
        return true;
    }

    private int updateBackSensorCoverBrightness(int backSensorCoverBrightness) {
        int autoBrightnessDb;
        Context context = this.mContext;
        if (context == null || backSensorCoverBrightness >= (autoBrightnessDb = Settings.System.getIntForUser(context.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId))) {
            return backSensorCoverBrightness;
        }
        if (HWFLOW && this.mBackSensorCoverBrightness != autoBrightnessDb) {
            Slog.i(TAG, "BackSensorCoverMode backSensorCoverBrightnessOut=" + backSensorCoverBrightness + "-->autoBrightnessDb=" + autoBrightnessDb);
        }
        return autoBrightnessDb;
    }

    private boolean isPhoneInRing() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int phoneCount = TelephonyManager.getDefault().getPhoneCount();
            for (int phoneCountIndex = 0; phoneCountIndex < phoneCount; phoneCountIndex++) {
                if (TelephonyManager.getDefault().getCallState(phoneCountIndex) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState(SubscriptionManager.getDefaultSubscriptionId()) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void updateCurrentUserId(int userId) {
        if (userId != this.mCurrentUserId) {
            if (HWFLOW) {
                Slog.i(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
            }
            this.mCurrentUserId = userId;
        }
    }

    public boolean needFastestRateForManualBrightness() {
        if (!this.mData.backSensorCoverModeEnable || this.mBackSensorCoverBrightness <= 0 || !HwServiceFactory.isCoverClosed()) {
            return false;
        }
        return true;
    }

    private float covertBrightnessToPowerRatio(int brightness) {
        if ((this.mData.manualMode && this.mManualBrightness >= this.mData.manualBrightnessMaxLimit) || !this.mData.manualPowerSavingBrighnessLineEnable || HwServiceFactory.isCoverClosed()) {
            return 1.0f;
        }
        int brightnessNit = convertBrightnessLevelToNit(this.mManualBrightness);
        if (this.mIsManualPowerSavingEnable) {
            return getPowerSavingRatio(brightnessNit);
        }
        return 1.0f;
    }

    private int convertBrightnessLevelToNit(int brightness) {
        int brightnessTmp = brightness;
        if (brightnessTmp == 0) {
            return brightnessTmp;
        }
        if (brightnessTmp < 4) {
            brightnessTmp = 4;
        }
        if (brightnessTmp > 255) {
            brightnessTmp = 255;
        }
        return convertBrightnessLevelToNitInternal(brightnessTmp);
    }

    private float getPowerSavingRatio(int brightnessNit) {
        List<float[]> list = this.mPowerSavingBrighnessLinePointsList;
        if (list == null || list.size() == 0 || brightnessNit < 0) {
            Slog.e(TAG, "PowerSavingBrighnessLinePointsList warning,set PowerSavingRatio,Nit=" + brightnessNit);
            return 1.0f;
        }
        int linePointsListLength = this.mPowerSavingBrighnessLinePointsList.size();
        if (((float) brightnessNit) < this.mPowerSavingBrighnessLinePointsList.get(0)[0]) {
            return 1.0f;
        }
        float[] prePoint = null;
        float tmpPowerSavingRatio = 1.0f;
        int listIndex = 0;
        while (true) {
            if (listIndex > linePointsListLength - 1) {
                break;
            }
            float[] curPoint = this.mPowerSavingBrighnessLinePointsList.get(listIndex);
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (((float) brightnessNit) >= curPoint[0]) {
                prePoint = curPoint;
                tmpPowerSavingRatio = prePoint[1];
                listIndex++;
            } else if (curPoint[0] <= prePoint[0]) {
                tmpPowerSavingRatio = 1.0f;
                Slog.w(TAG, "nexPoint[0] <= prePoint[0] warning,set default tmpPowerSavingRatio");
            } else {
                tmpPowerSavingRatio = (((curPoint[1] - prePoint[1]) / (curPoint[0] - prePoint[0])) * (((float) brightnessNit) - prePoint[0])) + prePoint[1];
            }
        }
        if (tmpPowerSavingRatio <= 1.0f && tmpPowerSavingRatio >= 0.0f) {
            return tmpPowerSavingRatio;
        }
        Slog.w(TAG, "tmpPowerSavingRatio warning,set default value, tmpPowerSavingRatio= " + this.mPowerRatio);
        return 1.0f;
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
                params.put("brightnessMode", "MANUAL");
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendDefaultBrightnessToMonitor() {
        int lightValue;
        if (this.mDisplayEffectMonitor != null && this.mAlgoSmoothLightValue != (lightValue = (int) this.mOutdoorDetector.getFilterLuxFromManualMode())) {
            this.mAlgoSmoothLightValue = lightValue;
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "algoDefaultBrightness");
            params.put("lightValue", Integer.valueOf(lightValue));
            params.put("brightness", 0);
            params.put("brightnessMode", "MANUAL");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private float getDefaultBrightnessLevelNew(List<PointF> linePointsList, float lux) {
        float brightnessLevel = this.mDefaultBrightness;
        PointF prePoint = null;
        for (PointF curPoint : linePointsList) {
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (lux >= curPoint.x) {
                prePoint = curPoint;
                brightnessLevel = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                return this.mDefaultBrightness;
            } else {
                return (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (lux - prePoint.x)) + prePoint.y;
            }
        }
        return brightnessLevel;
    }

    public boolean getManualModeAnimationEnable() {
        return this.mIsManualModeAnimationEnable;
    }

    public boolean getManualModeEnable() {
        return this.mData.manualMode;
    }

    public boolean getManualPowerSavingAnimationEnable() {
        return this.mIsManualPowerSavingAnimationEnable;
    }

    public void setManualPowerSavingAnimationEnable(boolean isManualPowerSavingAnimationEnable) {
        this.mIsManualPowerSavingAnimationEnable = isManualPowerSavingAnimationEnable;
    }

    public boolean getManualThermalModeEnable() {
        return this.mIsManualModeEnable && this.mIsThermalModeEnable;
    }

    public boolean getManualThermalModeAnimationEnable() {
        return this.mIsThermalModeAnimationEnable;
    }

    public void setManualThermalModeAnimationEnable(boolean isThermalModeAnimationEnable) {
        this.mIsThermalModeAnimationEnable = isThermalModeAnimationEnable;
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
        if (this.mIsManualModeEnable) {
            Slog.i(TAG, "ThermalMode set Manual MaxBrightness=" + brightness + ",mappingBrightness=" + mappingBrightness);
            this.mIsThermalModeAnimationEnable = true;
            this.mIsThermalModeEnable = true;
            this.mCallbacks.updateManualBrightnessForLux();
            return;
        }
        this.mIsThermalModeAnimationEnable = false;
        this.mIsThermalModeEnable = false;
    }

    public boolean getBrightnessSetByAppEnable() {
        return this.mBrightnessHighLevelSetByAppEnable;
    }

    public boolean getBrightnessSetByAppAnimationEnable() {
        return this.mBrightnessHighLevelSetByAppAnimationEnable;
    }

    public void setBrightnessNoLimit(int brightness, int time) {
        Slog.i(TAG, "setBrightnessNoLimit set brightness=" + brightness + ",time=" + time);
        this.mBrightnessHighLevelSetByApp = brightness > 0 ? brightness : -1;
        if (brightness <= 0 || brightness > 255) {
            this.mBrightnessHighLevelSetByAppAnimationEnable = false;
            this.mBrightnessHighLevelSetByAppEnable = false;
        } else {
            this.mBrightnessHighLevelSetByAppAnimationEnable = true;
            this.mBrightnessHighLevelSetByAppEnable = true;
        }
        if (this.mIsManualModeEnable) {
            this.mCallbacks.updateManualBrightnessForLux();
        }
    }

    private int convertBrightnessLevelToNitInternal(int brightness) {
        float brightnessNitTmp;
        if (this.mData.brightnessLevelToNitMappingEnable) {
            brightnessNitTmp = convertBrightnessLevelToNitFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, (float) brightness);
        } else {
            brightnessNitTmp = ((((float) (brightness - 4)) * (this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit)) / 251.0f) + this.mData.screenBrightnessMinNit;
        }
        if (brightnessNitTmp < this.mData.screenBrightnessMinNit) {
            brightnessNitTmp = this.mData.screenBrightnessMinNit;
        }
        if (brightnessNitTmp > this.mData.screenBrightnessMaxNit) {
            brightnessNitTmp = this.mData.screenBrightnessMaxNit;
        }
        return (int) (0.5f + brightnessNitTmp);
    }

    private float convertBrightnessLevelToNitFromRealLinePoints(List<PointF> linePoints, float brightness) {
        int i = this.mDefaultBrightnessNit;
        float brightnessNitTmp = (float) i;
        if (linePoints == null) {
            return (float) i;
        }
        PointF prePoint = null;
        Iterator<PointF> it = linePoints.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF pointItem = it.next();
            if (prePoint == null) {
                prePoint = pointItem;
            }
            if (brightness >= pointItem.x) {
                prePoint = pointItem;
                brightnessNitTmp = prePoint.y;
            } else if (pointItem.x <= prePoint.x) {
                brightnessNitTmp = (float) this.mDefaultBrightnessNit;
            } else {
                brightnessNitTmp = (((pointItem.y - prePoint.y) / (pointItem.x - prePoint.x)) * (brightness - prePoint.x)) + prePoint.y;
            }
        }
        if (HWDEBUG) {
            Slog.d(TAG, "LevelToNit,brightness=" + brightness + ",TobrightnessNitTmp=" + brightnessNitTmp + ",mDefaultBrightnessNit=" + this.mDefaultBrightnessNit);
        }
        return brightnessNitTmp;
    }

    private int convertNitToBrightnessLevelFromRealLinePoints(List<PointF> linePoints, float brightnessNit, int defaultBrightness) {
        float brightnessLevel = (float) defaultBrightness;
        if (linePoints == null) {
            return defaultBrightness;
        }
        PointF prePoint = null;
        Iterator<PointF> it = linePoints.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF pointItem = it.next();
            if (prePoint == null) {
                prePoint = pointItem;
            }
            if (brightnessNit >= pointItem.y) {
                prePoint = pointItem;
                brightnessLevel = prePoint.x;
            } else if (pointItem.y <= prePoint.y) {
                brightnessLevel = (float) defaultBrightness;
            } else {
                brightnessLevel = (((pointItem.x - prePoint.x) / (pointItem.y - prePoint.y)) * (brightnessNit - prePoint.y)) + prePoint.x;
            }
        }
        if (HWDEBUG) {
            Slog.d(TAG, "NitToBrightnessLevel,brightnessNit=" + brightnessNit + ",TobrightnessLevel=" + brightnessLevel + ",defaultBrightness=" + defaultBrightness);
        }
        return (int) (0.5f + brightnessLevel);
    }

    @Override // com.android.server.display.HwBrightnessBatteryDetection.Callbacks
    public void updateBrightnessFromBattery(int lowBatteryMaxBrightness) {
        this.mLowBatteryMaxBrightness = lowBatteryMaxBrightness;
        if (HWFLOW) {
            Slog.i(TAG, "mLowBatteryMaxBrightness = " + this.mLowBatteryMaxBrightness);
        }
        if (this.mIsManualModeEnable) {
            this.mCallbacks.updateManualBrightnessForLux();
        }
    }

    @Override // com.android.server.display.HwBrightnessBatteryDetection.Callbacks
    public void updateBrightnessRatioFromBattery(int brightnessRatio) {
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

    public int getAmbientLux() {
        HwNormalizedManualBrightnessThresholdDetector hwNormalizedManualBrightnessThresholdDetector = this.mOutdoorDetector;
        if (hwNormalizedManualBrightnessThresholdDetector == null) {
            return 0;
        }
        return hwNormalizedManualBrightnessThresholdDetector.getCurrentFilteredAmbientLux();
    }

    public int getBrightnessLevel(int lux) {
        return 0;
    }

    private final class HwNormalizedManualBrightnessBatteryHandler extends Handler {
        HwNormalizedManualBrightnessBatteryHandler(Looper looper) {
            super(looper, null, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFrontCameraMaxBrightness() {
        int brightness = 255;
        if (!this.mData.frontCameraMaxBrightnessEnable) {
            this.mFrontCameraMaxBrightness = 255;
            return;
        }
        if (this.mManualAmbientLuxForCamera < this.mData.frontCameraLuxThreshold && "1".equals(this.mCurCameraId) && !this.mIsFrontCameraAppKeepBrightnessEnable) {
            brightness = this.mData.frontCameraMaxBrightness;
        }
        if (brightness != this.mFrontCameraMaxBrightness) {
            updateFrontCameraBrightnessDimmingEnable();
            this.mFrontCameraMaxBrightness = brightness;
            if (this.mIsLightSensorEnable) {
                Handler handler = this.mHandler;
                if (handler != null) {
                    handler.removeMessages(2);
                    this.mHandler.sendEmptyMessageDelayed(2, (long) this.mData.frontCameraUpdateBrightnessDelayTime);
                }
                if (HWFLOW) {
                    Slog.i(TAG, "updateFrontCameraMaxBrightness, manual brightness=" + brightness + ",lux=" + this.mManualAmbientLuxForCamera + ",mKeepBrightnessEnable=" + this.mIsFrontCameraAppKeepBrightnessEnable);
                }
            }
        }
    }

    private void updateFrontCameraBrightnessDimmingEnable() {
        Handler handler;
        this.mIsFrontCameraDimmingEnable = this.mManualBrightness > this.mData.frontCameraMaxBrightness;
        if (this.mIsFrontCameraDimmingEnable && (handler = this.mHandler) != null) {
            handler.removeMessages(3);
            this.mHandler.sendEmptyMessageDelayed(3, (long) this.mData.frontCameraUpdateDimmingEnableTime);
        }
        if (HWFLOW) {
            Slog.i(TAG, "mIsFrontCameraDimmingEnable=" + this.mIsFrontCameraDimmingEnable + ",mManualBrightness=" + this.mManualBrightness);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFrontCameraBrightnessDimmingEnable(boolean isDimmingEnable) {
        if (HWFLOW) {
            Slog.i(TAG, "setFrontCameraBrightnessDimmingEnable,dimmingEnable=" + isDimmingEnable);
        }
        this.mIsFrontCameraDimmingEnable = isDimmingEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBrightness(int msg) {
        if (this.mCallbacks == null) {
            Slog.w(TAG, "mCallbacks==null,no updateBrightness");
            return;
        }
        if (HWFLOW) {
            Slog.i(TAG, "updateBrightness for callback,msg=" + msg);
        }
        this.mCallbacks.updateManualBrightnessForLux();
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

    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
        updateHdrStatus(curveLevel);
    }

    private void updateHdrStatus(int curveLevel) {
        if (this.mData.hdrModeEnable) {
            if (curveLevel == 33 || curveLevel == 32) {
                this.mIsHdrEnable = curveLevel == 33;
                if (this.mIsLightSensorEnable) {
                    this.mCallbacks.updateManualBrightnessForLux();
                }
                if (HWFLOW) {
                    Slog.i(TAG, "updateHdrStatus set curveLevel=" + curveLevel + ",mIsHdrEnable=" + this.mIsHdrEnable);
                }
            }
        }
    }
}
