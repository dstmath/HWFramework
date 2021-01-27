package com.android.server.display;

import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.huawei.displayengine.HwXmlAmPoint;
import java.util.Iterator;
import java.util.List;

public class HwHumanFactorBrightness {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final int MODE_LUX_MIN_MAX = 2;
    private static final int MODE_TOP_GAME = 1;
    private static final String TAG = "HwHumanFactorBrightness";
    private final HwBrightnessXmlLoader.Data mData = HwBrightnessXmlLoader.getData();

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    /* access modifiers changed from: package-private */
    public int calculateHumanFactorMinBrightness(float ambientLux, int mode) {
        List<HwXmlAmPoint> brightnessPoints;
        if (ambientLux < 0.0f) {
            Slog.w(TAG, "ambientLux<0, return default humanFactorMinBrightness");
            return 4;
        }
        float offsetMinBrightness = 4.0f;
        HwXmlAmPoint prePoint = null;
        if (mode == 1) {
            brightnessPoints = this.mData.gameModeAmbientLuxValidBrightnessPoints;
        } else if (mode == 2) {
            brightnessPoints = this.mData.luxMinMaxBrightnessPoints;
        } else {
            brightnessPoints = this.mData.ambientLuxValidBrightnessPoints;
        }
        Iterator<HwXmlAmPoint> iter = brightnessPoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            HwXmlAmPoint curPoint = iter.next();
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (ambientLux >= curPoint.x) {
                prePoint = curPoint;
                offsetMinBrightness = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                offsetMinBrightness = 4.0f;
                Slog.w(TAG, "HumanFactorMinBrightness prePoint.x <= nextPoint.x,x=" + curPoint.x + ",y=" + curPoint.y);
            } else {
                offsetMinBrightness = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (ambientLux - prePoint.x)) + prePoint.y;
            }
        }
        return (int) offsetMinBrightness;
    }

    /* access modifiers changed from: package-private */
    public int calculateHumanFactorMaxBrightness(float ambientLux, int mode) {
        List<HwXmlAmPoint> brightnessPoints;
        if (ambientLux < 0.0f) {
            Slog.w(TAG, "ambientLux<0, return default humanFactorMaxBrightness");
            return 255;
        }
        float offsetMaxBrightness = 255.0f;
        HwXmlAmPoint prePoint = null;
        if (mode == 1) {
            brightnessPoints = this.mData.gameModeAmbientLuxValidBrightnessPoints;
        } else if (mode == 2) {
            brightnessPoints = this.mData.luxMinMaxBrightnessPoints;
        } else {
            brightnessPoints = this.mData.ambientLuxValidBrightnessPoints;
        }
        Iterator<HwXmlAmPoint> iter = brightnessPoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            HwXmlAmPoint curPoint = iter.next();
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (ambientLux >= curPoint.x) {
                prePoint = curPoint;
                offsetMaxBrightness = prePoint.z;
            } else if (curPoint.x <= prePoint.x) {
                offsetMaxBrightness = 255.0f;
                Slog.w(TAG, "HumanFactorMaxBrightness prePoint.x <= nextPoint.x,x=" + curPoint.x + ",z=" + curPoint.z);
            } else {
                offsetMaxBrightness = (((curPoint.z - prePoint.z) / (curPoint.x - prePoint.x)) * (ambientLux - prePoint.x)) + prePoint.z;
            }
        }
        return (int) offsetMaxBrightness;
    }
}
