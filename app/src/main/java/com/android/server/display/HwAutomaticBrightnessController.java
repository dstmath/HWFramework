package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.HwCustomSpline;
import android.util.Log;
import android.util.Spline;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.input.HwCircleAnimation;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;
import huawei.cust.HwCustUtils;

public class HwAutomaticBrightnessController extends AutomaticBrightnessController {
    private static final boolean DEBUG = false;
    private static final long NORMAL_DARKENING_LIGHT_DEBOUNCE = 8000;
    private static final long POWER_ON_BRIGHTENING_LIGHT_DEBOUNCE = 1000;
    private static final long POWER_ON_DARKENING_LIGHT_DEBOUNCE = 1000;
    private static final long POWER_ON_LUX_ABANDON_COUNT_MAX = 3;
    private static final long POWER_ON_LUX_COUNT_MAX = 8;
    private static String TAG;
    private static final float[] backLightLevel;
    private static final float[] envLight;
    private static final float[][] envToLightSample;
    private static HwCustAutomaticBrightnessController mCustAutomaticBrightnessControllerImpl;
    private static boolean mPowerStatus;
    private static HwCustomSpline mScreenAutoBrightnessHWSpline;
    private int mPowerOnLuxAbandonCount;
    private int mPowerOnLuxCount;

    static {
        TAG = "HwAutomaticBrightnessController";
        mPowerStatus = DEBUG;
        mCustAutomaticBrightnessControllerImpl = (HwCustAutomaticBrightnessController) HwCustUtils.createObj(HwCustAutomaticBrightnessController.class, new Object[0]);
        envToLightSample = new float[][]{new float[]{4.0f, 6.0f, 7.0f, 8.0f, 9.0f, 11.0f, 15.0f, 20.0f, 28.0f, 38.0f, 55.0f, 78.0f, 108.0f, 128.0f}, new float[]{4.0f, 6.0f, 7.0f, 8.0f, 10.0f, 12.0f, 17.0f, 25.0f, 34.0f, 50.0f, 77.0f, 110.0f, 150.0f, 170.0f}, new float[]{4.0f, 7.0f, 8.0f, 9.0f, 11.0f, 14.0f, 23.0f, 33.0f, 48.0f, 65.0f, 100.0f, 145.0f, 190.0f, 210.0f}, new float[]{4.0f, 7.0f, 10.0f, 13.0f, 18.0f, 23.0f, 34.0f, 46.0f, 65.0f, 85.0f, 130.0f, 180.0f, 230.0f, 254.0f}, new float[]{4.0f, 11.0f, 14.0f, 20.0f, 27.0f, 31.0f, 47.0f, 63.0f, 82.0f, 112.0f, 160.0f, 210.0f, 254.0f, 254.0f}, new float[]{4.0f, 14.0f, 22.0f, 28.0f, 37.0f, 45.0f, 60.0f, 78.0f, 100.0f, 130.0f, 185.0f, 254.0f, 254.0f, 254.0f}, new float[]{4.0f, 20.0f, 35.0f, 43.0f, 52.0f, 60.0f, 80.0f, 95.0f, 113.0f, 140.0f, 192.0f, 254.0f, 254.0f, 254.0f}, new float[]{4.0f, 30.0f, 52.0f, 63.0f, 74.0f, 82.0f, 99.0f, 111.0f, 128.0f, 152.0f, 198.0f, 254.0f, 254.0f, 254.0f}, new float[]{4.0f, 45.0f, 65.0f, 79.0f, 93.0f, 101.0f, 115.0f, 127.0f, 143.0f, 164.0f, 205.0f, 254.0f, 254.0f, 254.0f}, new float[]{32.0f, 65.0f, 85.0f, 101.0f, 112.0f, 120.0f, 131.0f, 142.0f, 158.0f, 180.0f, 215.0f, 254.0f, 254.0f, 254.0f}, new float[]{64.0f, 100.0f, 120.0f, 130.0f, 140.0f, 150.0f, 165.0f, 178.0f, 188.0f, 205.0f, 230.0f, 254.0f, 254.0f, 254.0f}};
        envLight = new float[]{0.0f, 10.0f, 20.0f, 30.0f, 50.0f, 75.0f, 200.0f, 400.0f, 800.0f, 1500.0f, 3000.0f, 5000.0f, 8000.0f, 10000.0f};
        backLightLevel = new float[]{0.0f, 26.0f, 52.0f, 78.0f, 104.0f, 128.0f, 154.0f, 180.0f, 203.0f, 228.0f, 255.0f};
    }

    private static HwCustomSpline createAutoBrightnessHwCustomSpline() {
        try {
            mScreenAutoBrightnessHWSpline = HwCustomSpline.createHwCustomSpline(envLight, backLightLevel, envToLightSample);
            return mScreenAutoBrightnessHWSpline;
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    public HwAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, Context context) {
        super(callbacks, looper, sensorManager, createAutoBrightnessHwCustomSpline(), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, LifeCycleStateMachine.TIME_OUT_TIME, CustomGestureDetector.TOUCH_TOLERANCE);
        this.mPowerOnLuxCount = 0;
        this.mPowerOnLuxAbandonCount = 0;
    }

    public HwAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
        this(callbacks, looper, sensorManager, createAutoBrightnessHwCustomSpline(), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, 0, 0, 0, DEBUG, context);
    }

    public void configure(boolean enable, float adjustment, boolean dozing) {
        configure(enable, adjustment, dozing, DEBUG);
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
        mScreenAutoBrightnessHWSpline.updateLevel(((Math.max(Math.min(adjustment, HwCircleAnimation.SMALL_ALPHA), -1.0f) + HwCircleAnimation.SMALL_ALPHA) / 2.0f) * 255.0f);
        super.configure(enable, adjustment, dozing, userInitiatedChange, DEBUG);
    }

    protected boolean calcNeedToBrighten(float ambient) {
        return ambient - (getBrighteningLuxThreshold() / (getBrighteningLightHystersis() + HwCircleAnimation.SMALL_ALPHA)) > Math.min(40.0f, ambient / 4.0f) + 10.0f ? true : DEBUG;
    }

    protected boolean calcNeedToDarken(float ambient) {
        boolean z = true;
        if (mCustAutomaticBrightnessControllerImpl == null || !mCustAutomaticBrightnessControllerImpl.avoidScreenFlash() || ambient > ((float) mCustAutomaticBrightnessControllerImpl.getLowLuxThreshhold())) {
            return true;
        }
        if ((getDarkeningLuxThreshold() / (HwCircleAnimation.SMALL_ALPHA - getDarkeningLightHystersis())) - ambient <= Math.min(7.0f, ambient / 2.0f)) {
            z = DEBUG;
        }
        return z;
    }

    public void setPowerStatus(boolean powerStatus) {
        mPowerStatus = powerStatus;
        if (!mPowerStatus) {
            this.mPowerOnLuxAbandonCount = 0;
            this.mPowerOnLuxCount = 0;
        }
    }

    protected long getNextAmbientLightBrighteningTime(long earliedtime) {
        if (mPowerStatus) {
            return POWER_ON_DARKENING_LIGHT_DEBOUNCE + earliedtime;
        }
        return BRIGHTENING_LIGHT_DEBOUNCE + earliedtime;
    }

    protected long getNextAmbientLightDarkeningTime(long earliedtime) {
        if (mPowerStatus) {
            return POWER_ON_DARKENING_LIGHT_DEBOUNCE + earliedtime;
        }
        return NORMAL_DARKENING_LIGHT_DEBOUNCE + earliedtime;
    }

    protected boolean interceptHandleLightSensorEvent(long time, float lux) {
        if (mPowerStatus) {
            this.mPowerOnLuxAbandonCount++;
            this.mPowerOnLuxCount++;
            if (((long) this.mPowerOnLuxCount) > POWER_ON_LUX_COUNT_MAX) {
                mPowerStatus = DEBUG;
            }
            if (((long) this.mPowerOnLuxAbandonCount) < POWER_ON_LUX_ABANDON_COUNT_MAX) {
                return true;
            }
        }
        return DEBUG;
    }
}
