package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.ArrayList;
import java.util.List;

public class HwBrightnessPowerSavingCurve {
    private static final float DEFAULT_POWERSAVING_RATIO = 1.0f;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final String TAG = "HwBrightnessPowerSavingCurve";
    private boolean mIsPowerSavingEnable = false;
    private final int mMaxBrightnessIndoor;
    private List<PointF> mPowerSavingBrighnessLinePointsList = null;
    private final float mScreenBrightnessMaxNit;
    private final float mScreenBrightnessMinNit;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public HwBrightnessPowerSavingCurve(int maxBrightnessIndoor, float screenBrightnessMinNit, float screenBrightnessMaxNit) {
        this.mMaxBrightnessIndoor = maxBrightnessIndoor;
        this.mScreenBrightnessMinNit = screenBrightnessMinNit;
        this.mScreenBrightnessMaxNit = screenBrightnessMaxNit;
        parsePowerSavingCure(SystemProperties.get("ro.config.blight_power_curve", ""));
    }

    private void parsePowerSavingCure(String powerSavingCure) {
        String[] powerSavingPoints;
        if (powerSavingCure == null || powerSavingCure.length() <= 0) {
            Slog.i(TAG, "powerSavingCure == null");
            return;
        }
        List<PointF> list = this.mPowerSavingBrighnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mPowerSavingBrighnessLinePointsList = new ArrayList();
        }
        for (String str : powerSavingCure.split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
            try {
                String[] point = str.split(",");
                this.mPowerSavingBrighnessLinePointsList.add(new PointF(Float.parseFloat(point[0]), Float.parseFloat(point[1])));
            } catch (NumberFormatException e) {
                this.mPowerSavingBrighnessLinePointsList.clear();
                Slog.w(TAG, "parse PowerSaving curve error");
                return;
            }
        }
        List<PointF> list2 = this.mPowerSavingBrighnessLinePointsList;
        if (list2 != null) {
            int listSize = list2.size();
            for (int i = 0; i < listSize; i++) {
                PointF pointTmp = this.mPowerSavingBrighnessLinePointsList.get(i);
                if (HWFLOW) {
                    Slog.i(TAG, "PowerSavingPointsList x = " + pointTmp.x + ", y = " + pointTmp.y);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getPowerSavingBrightness(int brightness) {
        float powerRatio = covertBrightnessToPowerRatio(brightness);
        int tempBrightness = (int) (((float) brightness) * powerRatio);
        if (HWFLOW && tempBrightness != brightness) {
            Slog.i(TAG, "NewCurveModePowerSaving tempBrightness=" + tempBrightness + ", brightness = " + brightness + ", ratio = " + powerRatio);
        }
        return tempBrightness;
    }

    /* access modifiers changed from: package-private */
    public void setPowerSavingEnable(boolean isPowerSavingEnable) {
        if (isPowerSavingEnable != this.mIsPowerSavingEnable) {
            this.mIsPowerSavingEnable = isPowerSavingEnable;
            if (HWFLOW) {
                Slog.i(TAG, "PowerSaving mIsPowerSavingEnable=" + this.mIsPowerSavingEnable);
            }
        }
    }

    private float covertBrightnessToPowerRatio(int brightness) {
        if (brightness >= this.mMaxBrightnessIndoor) {
            return 1.0f;
        }
        int brightnessNit = covertBrightnessLevelToNit(brightness);
        if (this.mIsPowerSavingEnable) {
            return getPowerSavingRatio(brightnessNit);
        }
        return 1.0f;
    }

    private int covertBrightnessLevelToNit(int brightness) {
        if (brightness == 0) {
            return brightness;
        }
        int brightnessTemp = brightness;
        if (brightness < 4) {
            brightnessTemp = 4;
        }
        if (brightness > 255) {
            brightnessTemp = 255;
        }
        float f = this.mScreenBrightnessMaxNit;
        float f2 = this.mScreenBrightnessMinNit;
        return (int) (((((float) (brightnessTemp - 4)) * (f - f2)) / 251.0f) + f2);
    }

    private float getPowerSavingRatio(int brightnessNit) {
        List<PointF> list = this.mPowerSavingBrighnessLinePointsList;
        if (list == null || list.size() == 0 || brightnessNit < 0) {
            Slog.e(TAG, "PowerSavingBrighnessLinePointsList warning,set PowerSavingRatio,brightnessNit=" + brightnessNit);
            return 1.0f;
        }
        int linePointsListLength = this.mPowerSavingBrighnessLinePointsList.size();
        if (((float) brightnessNit) < this.mPowerSavingBrighnessLinePointsList.get(0).x) {
            return 1.0f;
        }
        PointF prePoint = null;
        float tmpPowerSavingRatio = 1.0f;
        int i = 0;
        while (true) {
            if (i >= linePointsListLength) {
                break;
            }
            PointF curPoint = this.mPowerSavingBrighnessLinePointsList.get(i);
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (((float) brightnessNit) >= curPoint.x) {
                prePoint = curPoint;
                tmpPowerSavingRatio = prePoint.y;
                i++;
            } else if (curPoint.x <= prePoint.x) {
                tmpPowerSavingRatio = 1.0f;
                Slog.w(TAG, "nexPoint[0] <= prePoint[0] warning,set default tmpPowerSavingRatio");
            } else {
                tmpPowerSavingRatio = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (((float) brightnessNit) - prePoint.x)) + prePoint.y;
            }
        }
        if (tmpPowerSavingRatio <= 1.0f && tmpPowerSavingRatio >= 0.0f) {
            return tmpPowerSavingRatio;
        }
        Slog.w(TAG, "tmpPowerSavingRatio warning,set default value");
        return 1.0f;
    }
}
