package com.android.server.display;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.HwBrightnessPgSceneDetection;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.display.HwLightSensorController;
import com.android.server.display.ManualBrightnessController;
import com.android.server.gesture.GestureNavConst;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwNormalizedManualBrightnessController extends ManualBrightnessController implements HwLightSensorController.LightSensorCallbacks, HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks {
    private static final int BACK_SENSOR_REPORT_TIMEOUT = 300;
    private static final int DEFAULT = 0;
    private static final float DEFAULT_POWERSAVING_RATIO = 1.0f;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int INDOOR = 1;
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int MINDEFAULTBRIGHTNESS = 4;
    private static final int MSG_SENSOR_TIMEOUT = 1;
    private static final int OUTDOOR = 2;
    private static String TAG = "HwNormalizedManualBrightnessController";
    private int mAlgoSmoothLightValue;
    private boolean mAmbientLuxTimeOut;
    private boolean mAmbientLuxValid;
    private int mAutoBrightnessLevel = -1;
    private int mBackSensorCoverBrightness = -1;
    private int mBackSensorCoverLux = -1;
    private int mBrightnessNoLimitSetByApp = -1;
    private boolean mBrightnessNoLimitSetByAppAnimationEnable = false;
    private boolean mBrightnessNoLimitSetByAppEnable = false;
    private final Context mContext;
    private int mCurrentUserId = 0;
    private final HwBrightnessXmlLoader.Data mData = HwBrightnessXmlLoader.getData();
    private float mDefaultBrightness = 100.0f;
    private int mDefaultBrightnessNit = 20;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private Handler mHandler;
    private HwBrightnessMapping mHwBrightnessMapping;
    private HwBrightnessPgSceneDetection mHwBrightnessPgSceneDetection;
    private final boolean mIsBackSensorEnable;
    private long mLastAmbientLightToMonitorTime;
    private HwLightSensorController mLightSensorController = null;
    private boolean mLightSensorEnable;
    private int mManualAmbientLux;
    private boolean mManualModeAnimationEnable = false;
    private boolean mManualModeEnable;
    private boolean mManualPowerSavingAnimationEnable = false;
    private boolean mManualPowerSavingBrighnessLineDisableForDemo = false;
    private boolean mManualPowerSavingEnable = false;
    private int mManualbrightness = -1;
    private int mManualbrightnessLog = -1;
    private int mManualbrightnessOut = -1;
    private int mMaxBrightnessSetByThermal = 255;
    private HwNormalizedManualBrightnessThresholdDetector mOutdoorDetector = null;
    private int mOutdoorScene;
    private float mPowerRatio = 1.0f;
    List<float[]> mPowerSavingBrighnessLinePointsList = null;
    private boolean mThermalModeAnimationEnable = false;
    private boolean mThermalModeEnable = false;
    private boolean mUsePowerSavingModeCurveEnable = false;

    private final class HwNormalizedManualBrightnessHandler extends Handler {
        private HwNormalizedManualBrightnessHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwNormalizedManualBrightnessController.this.updateBrightnessIfNoAmbientLuxReported();
            }
        }
    }

    static {
        boolean z = true;
        if (!Log.HWLog && (!Log.HWModuleLog || !Log.isLoggable(TAG, 3))) {
            z = false;
        }
        HWDEBUG = z;
    }

    public HwNormalizedManualBrightnessController(ManualBrightnessController.ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
        super(callbacks);
        HwLightSensorController hwLightSensorController = new HwLightSensorController(context, this, sensorManager, this.mData.lightSensorRateMills, TAG);
        this.mLightSensorController = hwLightSensorController;
        this.mOutdoorDetector = new HwNormalizedManualBrightnessThresholdDetector(this.mData);
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        parseManualModePowerSavingCure(SystemProperties.get("ro.config.blight_power_curve", ""));
        this.mContext = context;
        this.mHwBrightnessPgSceneDetection = new HwBrightnessPgSceneDetection(this, this.mData.pgSceneDetectionDarkenDelayTime, this.mData.pgSceneDetectionBrightenDelayTime, this.mContext);
        this.mHwBrightnessMapping = new HwBrightnessMapping(this.mData.brightnessMappingPoints);
        this.mIsBackSensorEnable = this.mLightSensorController.isBackSensorEnable();
        if (this.mData.backSensorCoverModeEnable) {
            this.mHandler = new HwNormalizedManualBrightnessHandler();
        }
        if (this.mData.manualPowerSavingBrighnessLineDisableForDemo) {
            this.mManualPowerSavingBrighnessLineDisableForDemo = isDemoVersion();
        }
    }

    private void parseManualModePowerSavingCure(String powerSavingCure) {
        if (powerSavingCure == null || powerSavingCure.length() <= 0) {
            Slog.i(TAG, "powerSavingCure == null");
            return;
        }
        if (this.mPowerSavingBrighnessLinePointsList != null) {
            this.mPowerSavingBrighnessLinePointsList.clear();
        } else {
            this.mPowerSavingBrighnessLinePointsList = new ArrayList();
        }
        String[] powerSavingPoints = powerSavingCure.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int i = 0;
        while (i < powerSavingPoints.length) {
            try {
                String[] point = powerSavingPoints[i].split(",");
                this.mPowerSavingBrighnessLinePointsList.add(new float[]{Float.parseFloat(point[0]), Float.parseFloat(point[1])});
                i++;
            } catch (NumberFormatException e) {
                this.mPowerSavingBrighnessLinePointsList.clear();
                Slog.w(TAG, "parse ManualPowerSaving curve error");
                return;
            }
        }
        if (this.mPowerSavingBrighnessLinePointsList != null) {
            int listSize = this.mPowerSavingBrighnessLinePointsList.size();
            for (int i2 = 0; i2 < listSize; i2++) {
                float[] temp = this.mPowerSavingBrighnessLinePointsList.get(i2);
                if (HWFLOW) {
                    Slog.d(TAG, "ManualPowerSavingPointsList x = " + temp[0] + ", y = " + temp[1]);
                }
            }
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

    public void updatePowerState(int state, boolean enable) {
        if (this.mManualModeEnable != enable) {
            if (HWFLOW) {
                String str = TAG;
                Slog.i(str, "HBM SensorEnable change " + this.mManualModeEnable + " -> " + enable);
            }
            this.mManualModeEnable = enable;
        }
        if (this.mManualModeEnable) {
            setLightSensorEnabled(wantScreenOn(state));
        } else {
            setLightSensorEnabled(this.mManualModeEnable);
        }
    }

    private void setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnable) {
                this.mLightSensorEnable = true;
                this.mLightSensorController.enableSensor();
                this.mAmbientLuxValid = false;
                this.mAmbientLuxTimeOut = false;
                this.mBackSensorCoverLux = -1;
                this.mBackSensorCoverBrightness = -1;
                if (this.mHandler != null) {
                    this.mHandler.sendEmptyMessageDelayed(1, 300);
                }
                if (HWFLOW) {
                    Slog.i(TAG, "ManualMode sensor enable");
                }
            }
            boolean pGBLListenerRegisted = this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted();
            if (this.mData.manualPowerSavingBrighnessLineEnable && !pGBLListenerRegisted) {
                this.mHwBrightnessPgSceneDetection.registerPgBLightSceneListener(this.mContext);
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "PowerSaving Manul in registerPgBLightSceneChangedListener,=" + this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted());
                }
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            this.mLightSensorController.disableSensor();
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
            }
            if (HWFLOW) {
                Slog.i(TAG, "ManualMode sensor disable");
            }
        }
        this.mLastAmbientLightToMonitorTime = 0;
    }

    public void updateManualBrightness(int brightness) {
        this.mManualbrightness = brightness;
        this.mManualbrightnessOut = brightness;
    }

    private static boolean isDemoVersion() {
        String vendor2 = SystemProperties.get("ro.hw.vendor", "");
        String country = SystemProperties.get("ro.hw.country", "");
        String str = TAG;
        Slog.i(str, "vendor:" + vendor2 + ",country:" + country);
        return "demo".equalsIgnoreCase(vendor2) || "demo".equalsIgnoreCase(country);
    }

    public void updateStateRecognition(boolean usePwrBLightCurve, int appType) {
        if (!this.mData.manualPowerSavingBrighnessLineEnable || !this.mManualModeEnable || this.mManualPowerSavingBrighnessLineDisableForDemo) {
            this.mManualPowerSavingAnimationEnable = false;
            this.mManualPowerSavingEnable = false;
            return;
        }
        this.mManualPowerSavingEnable = usePwrBLightCurve;
        this.mManualPowerSavingAnimationEnable = getPowerSavingModeBrightnessChangeEnable(this.mManualbrightness, usePwrBLightCurve);
        float powerRatio = covertBrightnessToPowerRatio(this.mManualbrightness);
        int tembrightness = (int) (((float) this.mManualbrightness) * powerRatio);
        if (this.mData.brightnessLevelToNitMappingEnable) {
            tembrightness = convertNitToBrightnessLevelFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, ((float) convertBrightnessLevelToNit(this.mManualbrightness)) * powerRatio, this.mManualbrightness);
        }
        int pgModeBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(tembrightness);
        if (pgModeBrightness != this.mManualbrightness) {
            powerRatio = this.mHwBrightnessPgSceneDetection.getPgPowerModeRatio();
            tembrightness = pgModeBrightness;
        }
        if (this.mManualbrightnessLog != tembrightness) {
            int brightnessNit = convertBrightnessLevelToNit(this.mManualbrightness);
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "PowerSaving,ManualMode mManualbrightness=" + this.mManualbrightness + ",brightnessNit=" + brightnessNit + ",powerRatio=" + powerRatio + ",maxNit=" + this.mData.screenBrightnessMaxNit + ",MinNit=" + this.mData.screenBrightnessMinNit + ",usePwrBLightCurve=" + usePwrBLightCurve + ",appType=" + appType);
            }
            this.mManualbrightnessLog = tembrightness;
            this.mCallbacks.updateManualBrightnessForLux();
        }
    }

    private boolean getPowerSavingModeBrightnessChangeEnable(int brightness, boolean usePowerSavingModeCurveEnable) {
        boolean powerSavingModeBrightnessChangeEnable = false;
        if (this.mUsePowerSavingModeCurveEnable != usePowerSavingModeCurveEnable) {
            float powerRatio = covertBrightnessLevelToPowerRatio(brightness);
            int tembrightness = (int) (((float) brightness) * powerRatio);
            if (this.mData.brightnessLevelToNitMappingEnable) {
                tembrightness = convertNitToBrightnessLevelFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, ((float) convertBrightnessLevelToNit(brightness)) * powerRatio, brightness);
            }
            if (brightness != tembrightness) {
                powerSavingModeBrightnessChangeEnable = true;
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "PowerSaving Enable=" + true + ",Pgbrightness=" + tembrightness + ",brightness=" + brightness);
                }
            }
        }
        this.mUsePowerSavingModeCurveEnable = usePowerSavingModeCurveEnable;
        return powerSavingModeBrightnessChangeEnable;
    }

    private float covertBrightnessLevelToPowerRatio(int brightness) {
        if ((!this.mData.manualMode || this.mManualbrightness < this.mData.manualBrightnessMaxLimit) && this.mData.manualPowerSavingBrighnessLineEnable) {
            return getPowerSavingRatio(convertBrightnessLevelToNit(this.mManualbrightness));
        }
        return 1.0f;
    }

    public int getManualBrightness() {
        if (this.mBrightnessNoLimitSetByApp > 0) {
            return this.mBrightnessNoLimitSetByApp;
        }
        if (this.mData.backSensorCoverModeEnable) {
            if (this.mBackSensorCoverBrightness > 0) {
                if (this.mAutoBrightnessLevel != this.mBackSensorCoverBrightness && Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId) == 1) {
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", this.mBackSensorCoverBrightness, this.mCurrentUserId);
                    if (HWFLOW) {
                        String str = TAG;
                        Slog.d(str, "LabcCoverMode mAutoBrightnessLevel=" + this.mAutoBrightnessLevel + ",mBackSensorCoverBrightness=" + this.mBackSensorCoverBrightness);
                    }
                }
                this.mAutoBrightnessLevel = this.mBackSensorCoverBrightness;
                return this.mBackSensorCoverBrightness;
            } else if (!this.mAmbientLuxValid && !this.mAmbientLuxTimeOut && HwServiceFactory.isCoverClosed()) {
                this.mAutoBrightnessLevel = -1;
                return -1;
            }
        }
        float powerSavingRatio = covertBrightnessToPowerRatio(this.mManualbrightness);
        if (HWFLOW && Math.abs(this.mPowerRatio - powerSavingRatio) > 1.0E-7f) {
            int brightnessNit = convertBrightnessLevelToNit(this.mManualbrightness);
            this.mPowerRatio = powerSavingRatio;
            String str2 = TAG;
            Slog.d(str2, "PowerSaving powerSavingRatio=" + powerSavingRatio + ",mManualbrightness=" + this.mManualbrightness + ",brightnessNit=" + brightnessNit);
        }
        int mManualbrightnessTmp = (int) (((float) this.mManualbrightness) * powerSavingRatio);
        if (this.mData.brightnessLevelToNitMappingEnable) {
            mManualbrightnessTmp = convertNitToBrightnessLevelFromRealLinePoints(this.mData.brightnessLevelToNitLinePoints, ((float) convertBrightnessLevelToNit(this.mManualbrightness)) * powerSavingRatio, this.mManualbrightness);
        }
        if (mManualbrightnessTmp < 4) {
            mManualbrightnessTmp = 4;
            String str3 = TAG;
            Slog.w(str3, "warning mManualbrightness < min,Manualbrightness=" + 4);
        }
        if (mManualbrightnessTmp > 255) {
            mManualbrightnessTmp = 255;
            String str4 = TAG;
            Slog.w(str4, "warning mManualbrightness > max,Manualbrightness=" + 255);
        }
        this.mManualbrightnessOut = mManualbrightnessTmp;
        if (!this.mData.manualMode) {
            this.mManualbrightnessOut = mManualbrightnessTmp;
            if (HWFLOW) {
                String str5 = TAG;
                Slog.i(str5, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",mData.manualMode=" + this.mData.manualMode);
            }
        } else if (this.mManualbrightnessOut >= this.mData.manualBrightnessMaxLimit) {
            float defaultBrightness = getDefaultBrightnessLevelNew(this.mData.defaultBrighnessLinePoints, (float) this.mManualAmbientLux);
            if (this.mOutdoorScene == 2) {
                int mManualbrightnessTmpMin = mManualbrightnessTmp < this.mData.manualBrightnessMaxLimit ? mManualbrightnessTmp : this.mData.manualBrightnessMaxLimit;
                this.mManualbrightnessOut = mManualbrightnessTmpMin > ((int) defaultBrightness) ? mManualbrightnessTmpMin : (int) defaultBrightness;
                if (HWFLOW) {
                    String str6 = TAG;
                    Slog.i(str6, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            } else {
                this.mManualbrightnessOut = mManualbrightnessTmp < this.mData.manualBrightnessMaxLimit ? mManualbrightnessTmp : this.mData.manualBrightnessMaxLimit;
                if (HWFLOW) {
                    String str7 = TAG;
                    Slog.i(str7, "mManualbrightnessOut1=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            }
        }
        int pgModeBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(this.mManualbrightnessOut);
        if (this.mData.pgModeBrightnessMappingEnable && this.mManualbrightnessOut > this.mData.manualBrightnessMaxLimit) {
            pgModeBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(pgModeBrightness);
        }
        if (pgModeBrightness != this.mManualbrightnessOut) {
            this.mPowerRatio = this.mHwBrightnessPgSceneDetection.getPgPowerModeRatio();
            this.mManualbrightnessOut = pgModeBrightness;
            if (HWFLOW) {
                String str8 = TAG;
                Slog.d(str8, "PG_POWER_SAVE_MODE mManualbrightnessOut=" + this.mManualbrightnessOut + ",mManualbrightness=" + this.mManualbrightness + ",mPowerRatio=" + this.mPowerRatio);
            }
        }
        if (this.mManualbrightnessOut > this.mMaxBrightnessSetByThermal) {
            if (HWFLOW) {
                String str9 = TAG;
                Slog.d(str9, "ThermalMode OrgManualbrightnessOut=" + this.mManualbrightnessOut + ",mMaxBrightnessSetByThermal=" + this.mMaxBrightnessSetByThermal);
            }
            this.mManualbrightnessOut = this.mMaxBrightnessSetByThermal;
        }
        return this.mManualbrightnessOut;
    }

    public int getMaxBrightnessForSeekbar() {
        if (this.mData.manualMode) {
            return this.mData.manualBrightnessMaxLimit;
        }
        return 255;
    }

    public void processSensorData(long timeInMs, int lux, int cct) {
        this.mAmbientLuxValid = true;
        boolean needUpdateManualBrightness = false;
        if (this.mData.backSensorCoverModeEnable && needUpdateBrightWhileCoverClosed(lux)) {
            needUpdateManualBrightness = true;
        }
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForHBM();
        if (this.mOutdoorDetector.getLuxChangedFlagForHBM()) {
            needUpdateManualBrightness = true;
            this.mManualModeAnimationEnable = true;
        }
        this.mManualAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForHBM();
        if (needUpdateManualBrightness) {
            this.mCallbacks.updateManualBrightnessForLux();
            this.mOutdoorDetector.setLuxChangedFlagForHBM();
            if (HWFLOW) {
                String str = TAG;
                Slog.i(str, "mManualAmbientLux =" + this.mManualAmbientLux + ", mManualModeAnimationEnable=" + this.mManualModeAnimationEnable);
            }
        } else {
            this.mManualModeAnimationEnable = false;
        }
        sendAmbientLightToMonitor(timeInMs, (float) lux);
        sendDefaultBrightnessToMonitor();
    }

    /* access modifiers changed from: private */
    public void updateBrightnessIfNoAmbientLuxReported() {
        if (!this.mAmbientLuxValid) {
            this.mAmbientLuxTimeOut = true;
            if (HWFLOW) {
                Slog.d(TAG, "BackSensorCoverMode sensor doesn't report lux in 300ms");
            }
            this.mCallbacks.updateManualBrightnessForLux();
        }
    }

    private boolean isManualMode() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId) == 0;
    }

    private boolean needUpdateBrightWhileCoverClosed(int mixedSensorValue) {
        if (!this.mIsBackSensorEnable) {
            return false;
        }
        if (!HwServiceFactory.isCoverClosed()) {
            if (this.mBackSensorCoverBrightness > 0) {
                boolean isManualMode = isManualMode();
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "BackSensorCoverMode cover open, isManualMode=" + isManualMode);
                }
                this.mBackSensorCoverLux = -1;
                this.mBackSensorCoverBrightness = -1;
                if (isManualMode) {
                    return true;
                }
            }
            return false;
        }
        int backSensorValue = this.mLightSensorController.getBackSensorValue();
        this.mBackSensorCoverLux = this.mBackSensorCoverLux > backSensorValue ? this.mBackSensorCoverLux : backSensorValue;
        this.mBackSensorCoverLux = this.mBackSensorCoverLux > mixedSensorValue ? this.mBackSensorCoverLux : mixedSensorValue;
        if (this.mData.backSensorCoverModeMinLuxInRing > 0 && isPhoneInRing()) {
            this.mBackSensorCoverLux = this.mBackSensorCoverLux > this.mData.backSensorCoverModeMinLuxInRing ? this.mBackSensorCoverLux : this.mData.backSensorCoverModeMinLuxInRing;
        }
        int backSensorCoverBrightness = (int) getDefaultBrightnessLevelNew(this.mData.backSensorCoverModeBrighnessLinePoints, (float) this.mBackSensorCoverLux);
        boolean isManualMode2 = isManualMode();
        if (isManualMode2 && backSensorCoverBrightness < this.mManualbrightness) {
            backSensorCoverBrightness = -1;
        }
        if (backSensorCoverBrightness == this.mBackSensorCoverBrightness) {
            return false;
        }
        if (HWFLOW) {
            String str2 = TAG;
            Slog.d(str2, "BackSensorCoverMode mixed=" + mixedSensorValue + ", back=" + backSensorValue + ", lux=" + this.mBackSensorCoverLux + ", bright=" + backSensorCoverBrightness + ", isManualMode=" + isManualMode2);
        }
        this.mBackSensorCoverBrightness = backSensorCoverBrightness;
        return true;
    }

    private boolean isPhoneInRing() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int phoneCount = TelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                if (TelephonyManager.getDefault().getCallState(i) != 0) {
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
                String str = TAG;
                Slog.d(str, "user change from  " + this.mCurrentUserId + " into " + userId);
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
        if ((this.mData.manualMode && this.mManualbrightness >= this.mData.manualBrightnessMaxLimit) || !this.mData.manualPowerSavingBrighnessLineEnable) {
            return 1.0f;
        }
        int brightnessNit = convertBrightnessLevelToNit(this.mManualbrightness);
        float powerRatio = 1.0f;
        if (this.mManualPowerSavingEnable) {
            powerRatio = getPowerSavingRatio(brightnessNit);
        }
        return powerRatio;
    }

    private int convertBrightnessLevelToNit(int brightness) {
        if (brightness == 0) {
            return brightness;
        }
        if (brightness < 4) {
            brightness = 4;
        }
        if (brightness > 255) {
            brightness = 255;
        }
        return convertBrightnessLevelToNitInternal(brightness);
    }

    private float getPowerSavingRatio(int brightnssnit) {
        if (this.mPowerSavingBrighnessLinePointsList == null || this.mPowerSavingBrighnessLinePointsList.size() == 0 || brightnssnit < 0) {
            Slog.e(TAG, "PowerSavingBrighnessLinePointsList warning,set PowerSavingRatio,brightnssnit=" + brightnssnit);
            return 1.0f;
        }
        int linePointsListLength = this.mPowerSavingBrighnessLinePointsList.size();
        float[] temp = this.mPowerSavingBrighnessLinePointsList.get(0);
        if (((float) brightnssnit) < temp[0]) {
            return 1.0f;
        }
        float tmpPowerSavingRatio = 1.0f;
        float[] temp1 = null;
        float[] temp12 = temp;
        int i = 0;
        while (true) {
            if (i > linePointsListLength - 1) {
                break;
            }
            float[] temp2 = this.mPowerSavingBrighnessLinePointsList.get(i);
            if (temp1 == null) {
                temp1 = temp2;
            }
            if (((float) brightnssnit) < temp2[0]) {
                float[] temp22 = temp2;
                if (temp22[0] <= temp1[0]) {
                    tmpPowerSavingRatio = 1.0f;
                    Slog.w(TAG, "temp2[0] <= temp1[0] warning,set default tmpPowerSavingRatio");
                } else {
                    tmpPowerSavingRatio = (((temp22[1] - temp1[1]) / (temp22[0] - temp1[0])) * (((float) brightnssnit) - temp1[0])) + temp1[1];
                }
            } else {
                temp1 = temp2;
                tmpPowerSavingRatio = temp1[1];
                i++;
            }
        }
        if (tmpPowerSavingRatio > 1.0f || tmpPowerSavingRatio < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Slog.w(TAG, "tmpPowerSavingRatio warning,set default value, tmpPowerSavingRatio= " + this.mPowerRatio);
            tmpPowerSavingRatio = 1.0f;
        }
        return tmpPowerSavingRatio;
    }

    private void sendAmbientLightToMonitor(long time, float lux) {
        if (this.mDisplayEffectMonitor != null) {
            if (this.mLastAmbientLightToMonitorTime == 0 || time <= this.mLastAmbientLightToMonitorTime) {
                this.mLastAmbientLightToMonitorTime = time;
                return;
            }
            this.mLastAmbientLightToMonitorTime = time;
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "ambientLightCollection");
            params.put("lightValue", Integer.valueOf((int) lux));
            params.put("durationInMs", Integer.valueOf((int) (time - this.mLastAmbientLightToMonitorTime)));
            params.put("brightnessMode", "MANUAL");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendDefaultBrightnessToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            int lightValue = (int) this.mOutdoorDetector.getFilterLuxFromManualMode();
            if (this.mAlgoSmoothLightValue != lightValue) {
                this.mAlgoSmoothLightValue = lightValue;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "algoDefaultBrightness");
                params.put("lightValue", Integer.valueOf(lightValue));
                params.put("brightness", 0);
                params.put("brightnessMode", "MANUAL");
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    public float getDefaultBrightnessLevelNew(List<PointF> linePointsList, float lux) {
        float brightnessLevel = this.mDefaultBrightness;
        PointF temp1 = null;
        for (PointF temp : linePointsList) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                PointF temp2 = temp;
                if (temp2.x > temp1.x) {
                    return (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                float brightnessLevel2 = this.mDefaultBrightness;
                if (!HWFLOW) {
                    return brightnessLevel2;
                }
                String str = TAG;
                Slog.i(str, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel2;
            }
            temp1 = temp;
            brightnessLevel = temp1.y;
        }
        return brightnessLevel;
    }

    public boolean getManualModeAnimationEnable() {
        return this.mManualModeAnimationEnable;
    }

    public boolean getManualModeEnable() {
        return this.mData.manualMode;
    }

    public boolean getManualPowerSavingAnimationEnable() {
        return this.mManualPowerSavingAnimationEnable;
    }

    public void setManualPowerSavingAnimationEnable(boolean manualPowerSavingAnimationEnable) {
        this.mManualPowerSavingAnimationEnable = manualPowerSavingAnimationEnable;
    }

    public boolean getManualThermalModeEnable() {
        return this.mManualModeEnable && this.mThermalModeEnable;
    }

    public boolean getManualThermalModeAnimationEnable() {
        return this.mThermalModeAnimationEnable;
    }

    public void setManualThermalModeAnimationEnable(boolean thermalModeAnimationEnable) {
        this.mThermalModeAnimationEnable = thermalModeAnimationEnable;
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        int mappingBrightness = brightness;
        if (brightness > 0) {
            if (this.mData.thermalModeBrightnessMappingEnable) {
                mappingBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(brightness);
            }
            this.mMaxBrightnessSetByThermal = mappingBrightness;
        } else {
            this.mMaxBrightnessSetByThermal = 255;
        }
        if (this.mManualModeEnable) {
            String str = TAG;
            Slog.i(str, "ThermalMode set Manual MaxBrightness=" + brightness + ",mappingBrightness=" + mappingBrightness);
            this.mThermalModeAnimationEnable = true;
            this.mThermalModeEnable = true;
            this.mCallbacks.updateManualBrightnessForLux();
            return;
        }
        this.mThermalModeAnimationEnable = false;
        this.mThermalModeEnable = false;
    }

    public boolean getBrightnessSetByAppEnable() {
        return this.mBrightnessNoLimitSetByAppEnable;
    }

    public boolean getBrightnessSetByAppAnimationEnable() {
        return this.mBrightnessNoLimitSetByAppAnimationEnable;
    }

    public void setBrightnessNoLimit(int brightness, int time) {
        String str = TAG;
        Slog.i(str, "setBrightnessNoLimit set brightness=" + brightness + ",time=" + time);
        if (brightness > 0) {
            this.mBrightnessNoLimitSetByApp = brightness;
        } else {
            this.mBrightnessNoLimitSetByApp = -1;
        }
        if (brightness <= 0 || brightness > 255) {
            this.mBrightnessNoLimitSetByAppAnimationEnable = false;
            this.mBrightnessNoLimitSetByAppEnable = false;
        } else {
            this.mBrightnessNoLimitSetByAppAnimationEnable = true;
            this.mBrightnessNoLimitSetByAppEnable = true;
        }
        if (this.mManualModeEnable) {
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
        float brightnessNitTmp = (float) this.mDefaultBrightnessNit;
        if (linePoints == null) {
            return (float) this.mDefaultBrightnessNit;
        }
        PointF temp1 = null;
        Iterator<PointF> it = linePoints.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF temp = it.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (brightness < temp.x) {
                PointF temp2 = temp;
                if (temp2.x <= temp1.x) {
                    brightnessNitTmp = (float) this.mDefaultBrightnessNit;
                    if (HWFLOW) {
                        String str = TAG;
                        Slog.d(str, "LevelToNit,brightnessNitTmpdefault=" + this.mDefaultBrightnessNit + ",for_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    brightnessNitTmp = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (brightness - temp1.x)) + temp1.y;
                }
            } else {
                temp1 = temp;
                brightnessNitTmp = temp1.y;
            }
        }
        if (HWDEBUG) {
            String str2 = TAG;
            Slog.d(str2, "LevelToNit,brightness=" + brightness + ",TobrightnessNitTmp=" + brightnessNitTmp + ",mDefaultBrightnessNit=" + this.mDefaultBrightnessNit);
        }
        return brightnessNitTmp;
    }

    private int convertNitToBrightnessLevelFromRealLinePoints(List<PointF> linePoints, float brightnessNit, int defaultBrightness) {
        float brightnessLevel = (float) defaultBrightness;
        if (linePoints == null) {
            return defaultBrightness;
        }
        PointF temp1 = null;
        Iterator<PointF> it = linePoints.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF temp = it.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (brightnessNit < temp.y) {
                PointF temp2 = temp;
                if (temp2.y <= temp1.y) {
                    brightnessLevel = (float) defaultBrightness;
                    if (HWFLOW) {
                        String str = TAG;
                        Slog.i(str, "NitToBrightnessLevel,brightnessLeveldefault=" + defaultBrightness + ",for_temp1.y <= temp2.y,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    brightnessLevel = (((temp2.x - temp1.x) / (temp2.y - temp1.y)) * (brightnessNit - temp1.y)) + temp1.x;
                }
            } else {
                temp1 = temp;
                brightnessLevel = temp1.x;
            }
        }
        if (HWDEBUG) {
            String str2 = TAG;
            Slog.d(str2, "NitToBrightnessLevel,brightnessNit=" + brightnessNit + ",TobrightnessLevel=" + brightnessLevel + ",defaultBrightness=" + defaultBrightness);
        }
        return (int) (0.5f + brightnessLevel);
    }
}
