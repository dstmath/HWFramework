package com.android.server.display;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.display.DisplayEffectMonitor.MonitorModule;
import com.android.server.display.HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks;
import com.android.server.lights.LightsManager;
import java.util.ArrayList;
import java.util.List;

public class HwNormalizedManualBrightnessController extends ManualBrightnessController implements LightSensorCallbacks, HwBrightnessPgSceneDetectionCallbacks {
    private static boolean DEBUG = false;
    private static final int DEFAULT = 0;
    private static final float DEFAULT_POWERSAVING_RATIO = 1.0f;
    private static final int INDOOR = 1;
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int MINDEFAULTBRIGHTNESS = 4;
    private static final int OUTDOOR = 2;
    private static String TAG = "HwNormalizedManualBrightnessController";
    private int mAlgoSmoothLightValue;
    private final Context mContext;
    private final Data mData = HwBrightnessXmlLoader.getData(getDeviceActualBrightnessLevel());
    private float mDefaultBrightness = 100.0f;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private HwBrightnessPgSceneDetection mHwBrightnessPgSceneDetection;
    private long mLastAmbientLightToMonitorTime;
    private HwLightSensorController mLightSensorController = null;
    private boolean mLightSensorEnable;
    private int mManualAmbientLux;
    private boolean mManualModeAnimationEnable = false;
    private boolean mManualModeEnable;
    private boolean mManualPowerSavingAnimationEnable = false;
    private boolean mManualPowerSavingEnable = false;
    private int mManualbrightness = -1;
    private int mManualbrightnessLog = -1;
    private int mManualbrightnessOut = -1;
    private int mMaxBrightnessSetByThermal = 255;
    private boolean mNeedUpdateManualBrightness;
    private HwNormalizedManualBrightnessThresholdDetector mOutdoorDetector = null;
    private int mOutdoorScene;
    private float mPowerRatio = 1.0f;
    List<float[]> mPowerSavingBrighnessLinePointsList = null;
    private boolean mThermalModeAnimationEnable = false;
    private boolean mThermalModeEnable = false;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    private int getDeviceActualBrightnessLevel() {
        return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessLevel();
    }

    public HwNormalizedManualBrightnessController(ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
        super(callbacks);
        this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mData.lightSensorRateMills);
        this.mOutdoorDetector = new HwNormalizedManualBrightnessThresholdDetector(this.mData);
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        parseManualModePowerSavingCure(SystemProperties.get("ro.config.blight_power_curve", ""));
        this.mContext = context;
        this.mHwBrightnessPgSceneDetection = new HwBrightnessPgSceneDetection(this, this.mData.pgSceneDetectionDarkenDelayTime, this.mData.pgSceneDetectionBrightenDelayTime, this.mContext);
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
                float x = Float.parseFloat(point[0]);
                float y = Float.parseFloat(point[1]);
                this.mPowerSavingBrighnessLinePointsList.add(new float[]{x, y});
                i++;
            } catch (NumberFormatException e) {
                this.mPowerSavingBrighnessLinePointsList.clear();
                Slog.w(TAG, "parse ManualPowerSaving curve error");
                return;
            }
        }
        if (this.mPowerSavingBrighnessLinePointsList != null) {
            int listSize = this.mPowerSavingBrighnessLinePointsList.size();
            for (i = 0; i < listSize; i++) {
                float[] temp = (float[]) this.mPowerSavingBrighnessLinePointsList.get(i);
                if (DEBUG) {
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
            if (DEBUG) {
                Slog.i(TAG, "HBM SensorEnable change " + this.mManualModeEnable + " -> " + enable);
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
                if (DEBUG) {
                    Slog.i(TAG, "HBM ManualMode sensor enable");
                }
            }
            boolean pGBLListenerRegisted = this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted();
            if (this.mData.manualPowerSavingBrighnessLineEnable && (pGBLListenerRegisted ^ 1) != 0) {
                this.mHwBrightnessPgSceneDetection.registerPgBLightSceneListener(this.mContext);
                if (DEBUG) {
                    Slog.d(TAG, "PowerSaving Manul in registerPgBLightSceneChangedListener,=" + this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted());
                }
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            this.mLightSensorController.disableSensor();
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
            if (DEBUG) {
                Slog.i(TAG, "HBM ManualMode sensor disenable");
            }
        }
        this.mLastAmbientLightToMonitorTime = 0;
    }

    public void updateManualBrightness(int brightness) {
        this.mManualbrightness = brightness;
        this.mManualbrightnessOut = brightness;
    }

    public void updateStateRecognition(boolean usePwrBLightCurve, int appType) {
        if (this.mData.manualPowerSavingBrighnessLineEnable && this.mManualModeEnable) {
            this.mManualPowerSavingEnable = usePwrBLightCurve;
            this.mManualPowerSavingAnimationEnable = true;
            float powerRatio = covertBrightnessToPowerRatio(this.mManualbrightness);
            int tembrightness = (int) (((float) this.mManualbrightness) * powerRatio);
            int pgModeBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(tembrightness);
            if (pgModeBrightness != this.mManualbrightness) {
                powerRatio = this.mHwBrightnessPgSceneDetection.getPgPowerModeRatio();
                tembrightness = pgModeBrightness;
            }
            if (this.mManualbrightnessLog != tembrightness) {
                int brightnessNit = covertBrightnessLevelToNit(this.mManualbrightness);
                if (DEBUG) {
                    Slog.d(TAG, "PowerSaving,ManualMode mManualbrightness=" + this.mManualbrightness + ",brightnessNit=" + brightnessNit + ",powerRatio=" + powerRatio + ",maxNit=" + this.mData.screenBrightnessMaxNit + ",MinNit=" + this.mData.screenBrightnessMinNit + ",usePwrBLightCurve=" + usePwrBLightCurve + ",appType=" + appType);
                }
                this.mManualbrightnessLog = tembrightness;
                this.mCallbacks.updateManualBrightnessForLux();
                return;
            }
            return;
        }
        this.mManualPowerSavingAnimationEnable = false;
        this.mManualPowerSavingEnable = false;
    }

    public int getManualBrightness() {
        float powerSavingRatio = covertBrightnessToPowerRatio(this.mManualbrightness);
        if (DEBUG && Math.abs(this.mPowerRatio - powerSavingRatio) > 1.0E-7f) {
            int brightnessNit = covertBrightnessLevelToNit(this.mManualbrightness);
            this.mPowerRatio = powerSavingRatio;
            Slog.d(TAG, "PowerSaving powerSavingRatio=" + powerSavingRatio + ",mManualbrightness=" + this.mManualbrightness + ",brightnessNit=" + brightnessNit);
        }
        int mManualbrightnessTmp = (int) (((float) this.mManualbrightness) * powerSavingRatio);
        int pgModeBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(mManualbrightnessTmp);
        if (pgModeBrightness != this.mManualbrightness) {
            this.mPowerRatio = this.mHwBrightnessPgSceneDetection.getPgPowerModeRatio();
            mManualbrightnessTmp = pgModeBrightness;
        }
        if (mManualbrightnessTmp < 4) {
            mManualbrightnessTmp = 4;
            Slog.w(TAG, "warning mManualbrightness < min,Manualbrightness=" + 4);
        }
        if (mManualbrightnessTmp > 255) {
            mManualbrightnessTmp = 255;
            Slog.w(TAG, "warning mManualbrightness > max,Manualbrightness=" + 255);
        }
        this.mManualbrightnessOut = mManualbrightnessTmp;
        if (!this.mData.manualMode) {
            this.mManualbrightnessOut = mManualbrightnessTmp;
            if (DEBUG) {
                Slog.i(TAG, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",mData.manualMode=" + this.mData.manualMode);
            }
        } else if (this.mManualbrightnessOut >= this.mData.manualBrightnessMaxLimit) {
            float defaultBrightness = getDefaultBrightnessLevelNew(this.mData.defaultBrighnessLinePoints, (float) this.mManualAmbientLux);
            if (this.mOutdoorScene == 2) {
                int mManualbrightnessTmpMin = mManualbrightnessTmp < this.mData.manualBrightnessMaxLimit ? mManualbrightnessTmp : this.mData.manualBrightnessMaxLimit;
                if (mManualbrightnessTmpMin <= ((int) defaultBrightness)) {
                    mManualbrightnessTmpMin = (int) defaultBrightness;
                }
                this.mManualbrightnessOut = mManualbrightnessTmpMin;
                if (DEBUG) {
                    Slog.i(TAG, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            } else {
                if (mManualbrightnessTmp >= this.mData.manualBrightnessMaxLimit) {
                    mManualbrightnessTmp = this.mData.manualBrightnessMaxLimit;
                }
                this.mManualbrightnessOut = mManualbrightnessTmp;
                if (DEBUG) {
                    Slog.i(TAG, "mManualbrightnessOut1=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            }
        }
        if (this.mManualbrightnessOut > this.mMaxBrightnessSetByThermal) {
            if (DEBUG) {
                Slog.d(TAG, "ThermalMode OrgManualbrightnessOut=" + this.mManualbrightnessOut + ",mMaxBrightnessSetByThermal=" + this.mMaxBrightnessSetByThermal);
            }
            this.mManualbrightnessOut = this.mMaxBrightnessSetByThermal;
        }
        return this.mManualbrightnessOut;
    }

    public int getMaxBrightnessForSeekbar() {
        if (!this.mData.manualMode) {
            this.mData.manualBrightnessMaxLimit = 255;
        }
        return this.mData.manualBrightnessMaxLimit;
    }

    public void processSensorData(long timeInMs, int lux, int cct) {
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForHBM();
        this.mNeedUpdateManualBrightness = this.mOutdoorDetector.getLuxChangedFlagForHBM();
        this.mManualAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForHBM();
        if (this.mNeedUpdateManualBrightness) {
            this.mManualModeAnimationEnable = true;
            this.mCallbacks.updateManualBrightnessForLux();
            this.mOutdoorDetector.setLuxChangedFlagForHBM();
            if (DEBUG) {
                Slog.i(TAG, "mManualAmbientLux =" + this.mManualAmbientLux + ",mNeedUpdateManualBrightness1=" + this.mNeedUpdateManualBrightness);
            }
        } else {
            this.mManualModeAnimationEnable = false;
        }
        sendAmbientLightToMonitor(timeInMs, (float) lux);
        sendDefaultBrightnessToMonitor();
    }

    private float covertBrightnessToPowerRatio(int brightness) {
        if ((this.mData.manualMode && this.mManualbrightness >= this.mData.manualBrightnessMaxLimit) || (this.mData.manualPowerSavingBrighnessLineEnable ^ 1) != 0) {
            return 1.0f;
        }
        int brightnessNit = covertBrightnessLevelToNit(this.mManualbrightness);
        float powerRatio = 1.0f;
        if (this.mManualPowerSavingEnable) {
            powerRatio = getPowerSavingRatio(brightnessNit);
        }
        return powerRatio;
    }

    private int covertBrightnessLevelToNit(int brightness) {
        if (brightness == 0) {
            return brightness;
        }
        if (brightness < 4) {
            brightness = 4;
        }
        if (brightness > 255) {
            brightness = 255;
        }
        return (int) (((((float) (brightness - 4)) * (this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit)) / 251.0f) + this.mData.screenBrightnessMinNit);
    }

    private float getPowerSavingRatio(int brightnssnit) {
        if (this.mPowerSavingBrighnessLinePointsList == null || this.mPowerSavingBrighnessLinePointsList.size() == 0 || brightnssnit < 0) {
            Slog.e(TAG, "PowerSavingBrighnessLinePointsList warning,set PowerSavingRatio,brightnssnit=" + brightnssnit);
            return 1.0f;
        }
        int linePointsListLength = this.mPowerSavingBrighnessLinePointsList.size();
        if (((float) brightnssnit) < ((float[]) this.mPowerSavingBrighnessLinePointsList.get(0))[0]) {
            return 1.0f;
        }
        float[] temp1 = null;
        float tmpPowerSavingRatio = 1.0f;
        for (int i = 0; i <= linePointsListLength - 1; i++) {
            float[] temp = (float[]) this.mPowerSavingBrighnessLinePointsList.get(i);
            if (temp1 == null) {
                temp1 = temp;
            }
            if (((float) brightnssnit) < temp[0]) {
                float[] temp2 = temp;
                if (temp[0] <= temp1[0]) {
                    tmpPowerSavingRatio = 1.0f;
                    Slog.w(TAG, "temp2[0] <= temp1[0] warning,set default tmpPowerSavingRatio");
                } else {
                    tmpPowerSavingRatio = (((temp[1] - temp1[1]) / (temp[0] - temp1[0])) * (((float) brightnssnit) - temp1[0])) + temp1[1];
                }
                if (tmpPowerSavingRatio > 1.0f || tmpPowerSavingRatio < 0.0f) {
                    Slog.w(TAG, "tmpPowerSavingRatio warning,set default value, tmpPowerSavingRatio= " + this.mPowerRatio);
                    tmpPowerSavingRatio = 1.0f;
                }
                return tmpPowerSavingRatio;
            }
            temp1 = temp;
            tmpPowerSavingRatio = temp[1];
        }
        Slog.w(TAG, "tmpPowerSavingRatio warning,set default value, tmpPowerSavingRatio= " + this.mPowerRatio);
        tmpPowerSavingRatio = 1.0f;
        return tmpPowerSavingRatio;
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
            params.put("brightnessMode", "MANUAL");
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendDefaultBrightnessToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            int lightValue = (int) this.mOutdoorDetector.getFilterLuxFromManualMode();
            if (this.mAlgoSmoothLightValue != lightValue) {
                this.mAlgoSmoothLightValue = lightValue;
                ArrayMap<String, Object> params = new ArrayMap();
                params.put(MonitorModule.PARAM_TYPE, "algoDefaultBrightness");
                params.put("lightValue", Integer.valueOf(lightValue));
                params.put("brightness", Integer.valueOf(0));
                params.put("brightnessMode", "MANUAL");
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    public float getDefaultBrightnessLevelNew(List<PointF> linePointsList, float lux) {
        List<PointF> linePointsListIn = linePointsList;
        float brightnessLevel = this.mDefaultBrightness;
        PointF temp1 = null;
        for (PointF temp : linePointsList) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                brightnessLevel = this.mDefaultBrightness;
                if (!DEBUG) {
                    return brightnessLevel;
                }
                Slog.i(TAG, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel;
            }
            temp1 = temp;
            brightnessLevel = temp.y;
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
        return this.mManualModeEnable ? this.mThermalModeEnable : false;
    }

    public boolean getManualThermalModeAnimationEnable() {
        return this.mThermalModeAnimationEnable;
    }

    public void setManualThermalModeAnimationEnable(boolean thermalModeAnimationEnable) {
        this.mThermalModeAnimationEnable = thermalModeAnimationEnable;
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        if (brightness > 0) {
            this.mMaxBrightnessSetByThermal = brightness;
        } else {
            this.mMaxBrightnessSetByThermal = 255;
        }
        if (this.mManualModeEnable) {
            Slog.i(TAG, "ThermalMode set Manual MaxBrightness=" + brightness);
            this.mThermalModeAnimationEnable = true;
            this.mThermalModeEnable = true;
            this.mCallbacks.updateManualBrightnessForLux();
            return;
        }
        this.mThermalModeAnimationEnable = false;
        this.mThermalModeEnable = false;
    }
}
