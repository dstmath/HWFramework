package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.SystemClock;
import android.util.HwNewPolicySpline;
import android.util.Log;
import android.util.Slog;
import android.util.Spline;
import com.android.server.LocalServices;
import com.android.server.display.AutomaticBrightnessController.AmbientLightRingBuffer;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.input.HwCircleAnimation;
import com.android.server.input.HwCirclePrompt;
import com.android.server.lights.LightsManager;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;

public class HwAutomaticBrightnessNewPolicyController extends AutomaticBrightnessController {
    private static float BrightenDebounceTimePara = 0.0f;
    private static float BrightenDebounceTimeParaBig = 0.0f;
    private static float BrightenDeltaLuxMax = 0.0f;
    private static float BrightenDeltaLuxMin = 0.0f;
    private static float BrightenDeltaLuxPara = 0.0f;
    private static final boolean DEBUG;
    private static float DarkenDebounceTimePara = 0.0f;
    private static float DarkenDebounceTimeParaBig = 0.0f;
    private static float DarkenDeltaLuxMax = 0.0f;
    private static float DarkenDeltaLuxMin = 0.0f;
    private static float DarkenDeltaLuxPara = 0.0f;
    private static final long NORMAL_DARKENING_LIGHT_DEBOUNCE = 8000;
    private static final long POWER_ON_BRIGHTENING_LIGHT_DEBOUNCE = 1000;
    private static final long POWER_ON_DARKENING_LIGHT_DEBOUNCE = 1000;
    private static final long POWER_ON_LUX_ABANDON_COUNT_MAX = 3;
    private static final long POWER_ON_LUX_COUNT_MAX = 8;
    private static String TAG = null;
    private static final int mLargemStabilityTimeConstant = 10;
    private static int mLastAdjustment = 0;
    private static float mLuxBufferAvg = 0.0f;
    private static float mLuxBufferAvgMax = 0.0f;
    private static float mLuxBufferAvgMin = 0.0f;
    private static boolean mPowerStatus = false;
    private static HwNewPolicySpline mScreenAutoBrightnessHWNewPolicySpline = null;
    private static final int mSmallStabilityTimeConstant = 20;
    private static float mStability;
    private AmbientLightRingBuffer mAmbientLightRingBufferFilter;
    public boolean mAutoBrightnessIntervened;
    private Context mContext;
    public boolean mIsFirstValidAutoBrightness;
    private int mPowerOnLuxAbandonCount;
    private int mPowerOnLuxCount;

    static {
        TAG = "HwAutomaticBrightnessNewPolicyController";
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : DEBUG : true;
        DEBUG = isLoggable;
        mPowerStatus = DEBUG;
        mStability = 0.0f;
        mLuxBufferAvg = 0.0f;
        mLuxBufferAvgMax = 0.0f;
        mLuxBufferAvgMin = 0.0f;
        DarkenDeltaLuxMax = 0.0f;
        DarkenDeltaLuxMin = 0.0f;
        BrightenDeltaLuxMax = 0.0f;
        BrightenDeltaLuxMin = 0.0f;
        DarkenDeltaLuxPara = HwCircleAnimation.SMALL_ALPHA;
        BrightenDeltaLuxPara = 0.0f;
        DarkenDebounceTimePara = HwCircleAnimation.SMALL_ALPHA;
        BrightenDebounceTimePara = 0.0f;
        DarkenDebounceTimeParaBig = 0.0f;
        BrightenDebounceTimeParaBig = HwCirclePrompt.BG_ALPHA;
        mLastAdjustment = 0;
    }

    public static boolean needNewPolicy() {
        return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).isHighPrecision();
    }

    private static HwNewPolicySpline createAutoBrightnessHwNewPolicySpline(Context context) {
        try {
            mScreenAutoBrightnessHWNewPolicySpline = HwNewPolicySpline.createHwNewPolicySpline(context);
            return mScreenAutoBrightnessHWNewPolicySpline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    public HwAutomaticBrightnessNewPolicyController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, Context context) {
        super(callbacks, looper, sensorManager, createAutoBrightnessHwNewPolicySpline(context), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, LifeCycleStateMachine.TIME_OUT_TIME, CustomGestureDetector.TOUCH_TOLERANCE);
        this.mPowerOnLuxCount = 0;
        this.mPowerOnLuxAbandonCount = 0;
        this.mAutoBrightnessIntervened = DEBUG;
        this.mIsFirstValidAutoBrightness = DEBUG;
        this.mAmbientLightRingBufferFilter = new AmbientLightRingBuffer(50, LifeCycleStateMachine.TIME_OUT_TIME);
    }

    public HwAutomaticBrightnessNewPolicyController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
        this(callbacks, looper, sensorManager, createAutoBrightnessHwNewPolicySpline(context), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, 0, 0, 0, DEBUG, context);
    }

    public void configure(boolean enable, float adjustment, boolean dozing) {
        configure(enable, adjustment, dozing, DEBUG);
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
        if (needUpdateLevel(adjustment)) {
            mScreenAutoBrightnessHWNewPolicySpline.updateLevel(255.0f * adjustment);
        }
        super.configure(enable, adjustment, dozing, userInitiatedChange, DEBUG);
    }

    public int getAutomaticScreenBrightness() {
        if (!this.mWakeupFromSleep || SystemClock.uptimeMillis() - this.mLightSensorEnableTime >= 200) {
            return super.getAutomaticScreenBrightness();
        }
        if (DEBUG) {
            Slog.d(TAG, "mWakeupFromSleep= " + this.mWakeupFromSleep + ",currentTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableTime=" + this.mLightSensorEnableTime);
        }
        return -1;
    }

    protected void updateBrightnessIfNoAmbientLuxReported() {
        if (this.mWakeupFromSleep) {
            this.mWakeupFromSleep = DEBUG;
            this.mCallbacks.updateBrightness();
            if (DEBUG) {
                Slog.d(TAG, "sensor doesn't report lux in 200ms");
            }
        }
    }

    protected boolean calcNeedToBrighten(float ambient) {
        return ambient - (getBrighteningLuxThreshold() / (getBrighteningLightHystersis() + HwCircleAnimation.SMALL_ALPHA)) > Math.min(40.0f, ambient / 4.0f) + 10.0f ? true : DEBUG;
    }

    public void setPowerStatus(boolean powerStatus) {
        mPowerStatus = powerStatus;
        this.mWakeupFromSleep = powerStatus;
        if (!mPowerStatus) {
            this.mPowerOnLuxAbandonCount = 0;
            this.mPowerOnLuxCount = 0;
        }
    }

    protected long getNextAmbientLightBrighteningTime(long earliedtime) {
        if (!mPowerStatus) {
            return BRIGHTENING_LIGHT_DEBOUNCE + earliedtime;
        }
        if (DEBUG) {
            Slog.d(TAG, "earliestValidTime + POWER_ON_BRIGHTENING_LIGHT_DEBOUNCE");
        }
        return POWER_ON_DARKENING_LIGHT_DEBOUNCE + earliedtime;
    }

    protected long getNextAmbientLightDarkeningTime(long earliedtime) {
        if (!mPowerStatus) {
            return this.DARKENING_LIGHT_DEBOUNCE + earliedtime;
        }
        if (DEBUG) {
            Slog.d(TAG, "earliestValidTime + POWER_ON_DARKENING_LIGHT_DEBOUNCE");
        }
        return POWER_ON_DARKENING_LIGHT_DEBOUNCE + earliedtime;
    }

    protected boolean interceptHandleLightSensorEvent(long time, float lux) {
        if (mPowerStatus) {
            this.mPowerOnLuxAbandonCount++;
            this.mPowerOnLuxCount++;
            if (((long) this.mPowerOnLuxCount) > POWER_ON_LUX_COUNT_MAX) {
                mPowerStatus = DEBUG;
            }
            if (this.mLightSensorEnableElapsedTimeNanos - time > 350000000) {
                if (DEBUG) {
                    Slog.d(TAG, "abandon handleLightSensorEvent:" + lux);
                }
                return true;
            }
        }
        return DEBUG;
    }

    private boolean needUpdateLevel(float adj) {
        int tmp = (int) (255.0f * adj);
        if (mLastAdjustment == tmp) {
            return DEBUG;
        }
        if (DEBUG) {
            Slog.i(TAG, "mLastAdjustment = " + mLastAdjustment + ",tmp=" + tmp);
        }
        mLastAdjustment = tmp;
        return true;
    }

    protected boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        return DEBUG;
    }

    public void saveOffsetAlgorithmParas() {
    }

    protected void clearFilterAlgoParas() {
        this.mAutoBrightnessIntervened = DEBUG;
        this.mIsFirstValidAutoBrightness = DEBUG;
        this.mAmbientLightRingBufferFilter.clear();
    }

    protected void updateBuffer(long time, float ambientLux, int horizon) {
        this.mAmbientLightRingBufferFilter.push(time, ambientLux);
        this.mAmbientLightRingBufferFilter.prune(time - ((long) horizon));
    }

    protected void updatepara(AmbientLightRingBuffer buffer) {
        calculateStability(buffer);
        if (mStability > 100.0f) {
            mStability = 100.0f;
            DarkenDebounceTimeParaBig = HwCircleAnimation.SMALL_ALPHA;
            BRIGHTENING_LIGHT_HYSTERESIS = mStability / 100.0f;
        } else if (mStability < 5.0f) {
            mStability = 5.0f;
            DarkenDebounceTimeParaBig = 0.1f;
            BRIGHTENING_LIGHT_HYSTERESIS = mStability / 100.0f;
        } else {
            DarkenDebounceTimeParaBig = HwCircleAnimation.SMALL_ALPHA;
            BRIGHTENING_LIGHT_HYSTERESIS = mStability / 100.0f;
        }
        BRIGHTENING_LIGHT_DEBOUNCE = (long) (((double) (BrightenDebounceTimeParaBig * 800.0f)) * ((((double) (BrightenDebounceTimePara * mStability)) / 100.0d) + 1.0d));
        this.DARKENING_LIGHT_DEBOUNCE = (long) (((((double) (DarkenDebounceTimePara * mStability)) / 100.0d) + 1.0d) * 4000.0d);
        setDarkenThreshold();
        setBrightenThreshold();
    }

    private void setDarkenThreshold() {
        if (this.mAmbientLux >= 1200.0f) {
            DarkenDeltaLuxMax = Math.max(this.mAmbientLux - 1200.0f, 600.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        } else if (this.mAmbientLux >= 600.0f) {
            DarkenDeltaLuxMax = Math.max(this.mAmbientLux - 600.0f, 580.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        } else if (this.mAmbientLux >= 20.0f) {
            DarkenDeltaLuxMax = Math.max(this.mAmbientLux - 20.0f, 20.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        } else {
            DarkenDeltaLuxMax = Math.max(this.mAmbientLux, HwCircleAnimation.SMALL_ALPHA);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        }
        DarkenDeltaLuxMax *= ((DarkenDeltaLuxPara * (mStability - 5.0f)) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
    }

    public void setBrightenThreshold() {
        if (this.mAmbientLux >= 1000.0f) {
            BrightenDeltaLuxMax = 989.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 500.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * WifiProCommonUtils.RECOVERY_PERCENTAGE) + 489.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 100.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * WifiProCommonUtils.RECOVERY_PERCENTAGE) + 489.0f;
            BrightenDeltaLuxMin = (this.mAmbientLux * 1.375f) + 51.5f;
        } else if (this.mAmbientLux >= 10.0f) {
            BrightenDeltaLuxMax = Math.min((this.mAmbientLux * 20.0f) - 181.0f, (this.mAmbientLux * 4.0f) + 139.0f);
            BrightenDeltaLuxMin = Math.min((this.mAmbientLux * 5.0f) - 31.0f, (this.mAmbientLux * 1.5f) + 39.0f);
        } else if (this.mAmbientLux >= 2.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * WifiProCommonUtils.RECOVERY_PERCENTAGE) + 14.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else {
            BrightenDeltaLuxMax = 15.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        }
        BrightenDeltaLuxMax *= ((BrightenDeltaLuxPara * (mStability - 5.0f)) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
    }

    protected float calculateAmbientLux(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        } else if (N < 5) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMin = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMax = this.mAmbientLightRingBuffer.getLux(N - 1);
            for (int i = N - 2; i >= (N - 1) - 4; i--) {
                if (luxMin > this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMin = this.mAmbientLightRingBuffer.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMax = this.mAmbientLightRingBuffer.getLux(i);
                }
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / CustomGestureDetector.TOUCH_TOLERANCE;
        }
    }

    protected long nextAmbientLightBrighteningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean BrightenChange;
            float BrightenDeltaLux = this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux;
            if (BrightenDeltaLux > BrightenDeltaLuxMax) {
                BrightenChange = true;
            } else if (BrightenDeltaLux <= BrightenDeltaLuxMin || mStability >= 50.0f) {
                BrightenChange = DEBUG;
            } else {
                BrightenChange = true;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    protected long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DarkenChange;
            float DeltaLux = this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i);
            if (DeltaLux >= DarkenDeltaLuxMax && mStability < 15.0f) {
                DarkenChange = true;
            } else if (DeltaLux < DarkenDeltaLuxMin || mStability >= 5.0f) {
                DarkenChange = DEBUG;
            } else {
                DarkenChange = true;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    protected boolean decideToBrighten(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLux >= BrightenDeltaLuxMax) {
            needToBrighten = true;
        } else if (ambientLux - this.mAmbientLux < BrightenDeltaLuxMin || mStability >= 50.0f) {
            needToBrighten = DEBUG;
        } else {
            needToBrighten = true;
        }
        if (!needToBrighten || this.mAutoBrightnessIntervened) {
            return DEBUG;
        }
        return true;
    }

    protected boolean decideToDarken(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLux - ambientLux >= DarkenDeltaLuxMax) {
            needToDarken = true;
        } else if (this.mAmbientLux - ambientLux < DarkenDeltaLuxMin || mStability >= 5.0f) {
            needToDarken = DEBUG;
        } else {
            needToDarken = true;
        }
        if (!needToDarken || this.mAutoBrightnessIntervened) {
            return DEBUG;
        }
        return true;
    }

    public void calculateStability(AmbientLightRingBuffer buffer) {
        int N = buffer.size();
        if (N != 0) {
            int index1;
            int index2;
            float tmp;
            float Stability1;
            float Stability2;
            float currentLux = buffer.getLux(N - 1);
            calculateAvg(buffer);
            float luxT1 = currentLux;
            float luxT2 = currentLux;
            int T1 = 0;
            int T2 = 0;
            int index = 0;
            float luxT1Min = currentLux;
            float luxT2Min = currentLux;
            int indexMin = 0;
            float luxT1Max = currentLux;
            float luxT2Max = currentLux;
            int indexMax = 0;
            int j = 0;
            while (j < N - 1) {
                Object obj;
                int T1Max;
                int T2Max;
                int T1Min;
                int T2Min;
                float lux1 = buffer.getLux((N - 1) - j);
                float lux2 = buffer.getLux(((N - 1) - j) - 1);
                if (mLuxBufferAvg > lux1 || mLuxBufferAvg < lux2) {
                    if (mLuxBufferAvg >= lux1 && mLuxBufferAvg <= lux2) {
                    }
                    if (mLuxBufferAvgMin > lux1 || mLuxBufferAvgMin < lux2) {
                        if (mLuxBufferAvgMin >= lux1 && mLuxBufferAvgMin <= lux2) {
                        }
                        if (mLuxBufferAvgMax > lux1 || mLuxBufferAvgMax < lux2) {
                            if (mLuxBufferAvgMax >= lux1 && mLuxBufferAvgMax <= lux2) {
                            }
                            if (!(index == 0 || (indexMin == 0 && indexMax == 0))) {
                                if (index <= indexMin || index < indexMax) {
                                    if (index >= indexMin && index <= indexMax) {
                                        break;
                                    }
                                }
                                break;
                            }
                            j++;
                        }
                        obj = (mLuxBufferAvgMax == lux1 || mLuxBufferAvgMax != lux2) ? null : 1;
                        if (obj == null) {
                            luxT1Max = lux1;
                            luxT2Max = lux2;
                            T1Max = (N - 1) - j;
                            T2Max = ((N - 1) - j) - 1;
                            indexMax = j;
                        }
                        if (index <= indexMin) {
                        }
                        break;
                    }
                    obj = (mLuxBufferAvgMin == lux1 || mLuxBufferAvgMin != lux2) ? null : 1;
                    if (obj == null) {
                        luxT1Min = lux1;
                        luxT2Min = lux2;
                        T1Min = (N - 1) - j;
                        T2Min = ((N - 1) - j) - 1;
                        indexMin = j;
                    }
                    if (mLuxBufferAvgMax == lux1) {
                    }
                    if (obj == null) {
                        luxT1Max = lux1;
                        luxT2Max = lux2;
                        T1Max = (N - 1) - j;
                        T2Max = ((N - 1) - j) - 1;
                        indexMax = j;
                    }
                    if (index <= indexMin) {
                    }
                    break;
                }
                obj = (mLuxBufferAvg == lux1 && mLuxBufferAvg == lux2) ? 1 : null;
                if (obj == null) {
                    luxT1 = lux1;
                    luxT2 = lux2;
                    T1 = (N - 1) - j;
                    T2 = ((N - 1) - j) - 1;
                    index = j;
                }
                if (mLuxBufferAvgMin == lux1) {
                }
                if (obj == null) {
                    luxT1Min = lux1;
                    luxT2Min = lux2;
                    T1Min = (N - 1) - j;
                    T2Min = ((N - 1) - j) - 1;
                    indexMin = j;
                }
                if (mLuxBufferAvgMax == lux1) {
                }
                if (obj == null) {
                    luxT1Max = lux1;
                    luxT2Max = lux2;
                    T1Max = (N - 1) - j;
                    T2Max = ((N - 1) - j) - 1;
                    indexMax = j;
                }
                if (index <= indexMin) {
                }
                break;
            }
            if (indexMax <= indexMin) {
                index1 = indexMax;
                index2 = indexMin;
            } else {
                index1 = indexMin;
                index2 = indexMax;
            }
            int k1 = (N - 1) - index1;
            while (k1 <= N - 1 && k1 != N - 1) {
                float luxk1 = buffer.getLux(k1);
                float luxk2 = buffer.getLux(k1 + 1);
                if (indexMax > indexMin) {
                    if (luxk1 <= luxk2) {
                        break;
                    }
                    T1 = k1 + 1;
                } else if (luxk1 >= luxk2) {
                    break;
                } else {
                    T1 = k1 + 1;
                }
                k1++;
            }
            int k3 = (N - 1) - index2;
            while (k3 >= 0 && k3 != 0) {
                float luxk3 = buffer.getLux(k3);
                float luxk4 = buffer.getLux(k3 - 1);
                if (indexMax > indexMin) {
                    if (luxk3 >= luxk4) {
                        break;
                    }
                    T2 = k3 - 1;
                } else if (luxk3 <= luxk4) {
                    break;
                } else {
                    T2 = k3 - 1;
                }
                k3--;
            }
            int t1 = (N - 1) - T1;
            int t2 = T2;
            float s1 = calculateStabilityFactor(buffer, T1, N - 1);
            float avg1 = calcluateAvg(buffer, T1, N - 1);
            float s2 = calculateStabilityFactor(buffer, 0, T2);
            float deltaAvg = Math.abs(avg1 - calcluateAvg(buffer, 0, T2));
            float k = 0.0f;
            if (T1 != T2) {
                k = Math.abs((buffer.getLux(T1) - buffer.getLux(T2)) / ((float) (T1 - T2)));
            }
            if (k < 10.0f / (5.0f + k)) {
                tmp = k;
            } else {
                tmp = 10.0f / (5.0f + k);
            }
            if (tmp > 20.0f / (10.0f + deltaAvg)) {
                tmp = 20.0f / (10.0f + deltaAvg);
            }
            if (t1 > mSmallStabilityTimeConstant) {
                Stability1 = s1;
            } else {
                float a1 = (float) Math.exp((double) (t1 - 20));
                float b1 = (float) (20 - t1);
                float s3 = tmp;
                Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
            }
            if (t2 > mLargemStabilityTimeConstant) {
                Stability2 = s2;
            } else {
                float a2 = (float) Math.exp((double) (t2 - 10));
                float b2 = (float) (10 - t2);
                float s4 = tmp;
                Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
            }
            if (t1 > mSmallStabilityTimeConstant) {
                mStability = Stability1;
            } else {
                float a = (float) Math.exp((double) (t1 - 20));
                float b = (float) (20 - t1);
                mStability = ((a * Stability1) + (b * Stability2)) / (a + b);
            }
        }
    }

    private void calculateAvg(AmbientLightRingBuffer buffer) {
        int N = buffer.size();
        if (N != 0) {
            float currentLux = buffer.getLux(N - 1);
            float luxBufferSum = 0.0f;
            float luxBufferMin = currentLux;
            float luxBufferMax = currentLux;
            for (int i = N - 1; i >= 0; i--) {
                float lux = buffer.getLux(i);
                if (lux > luxBufferMax) {
                    luxBufferMax = lux;
                }
                if (lux < luxBufferMin) {
                    luxBufferMin = lux;
                }
                luxBufferSum += lux;
            }
            mLuxBufferAvg = luxBufferSum / ((float) N);
            mLuxBufferAvgMax = (mLuxBufferAvg + luxBufferMax) / 2.0f;
            mLuxBufferAvgMin = (mLuxBufferAvg + luxBufferMin) / 2.0f;
        }
    }

    private float calcluateAvg(AmbientLightRingBuffer buffer, int start, int end) {
        float sum = 0.0f;
        for (int i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        if (end < start) {
            return 0.0f;
        }
        return sum / ((float) ((end - start) + 1));
    }

    private float calculateStabilityFactor(AmbientLightRingBuffer buffer, int start, int end) {
        int size = (end - start) + 1;
        float sum = 0.0f;
        float sigma = 0.0f;
        if (size <= 1) {
            return 0.0f;
        }
        int i;
        for (i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        float avg = sum / ((float) size);
        for (i = start; i <= end; i++) {
            sigma += (buffer.getLux(i) - avg) * (buffer.getLux(i) - avg);
        }
        float ss = sigma / ((float) (size - 1));
        if (avg == 0.0f) {
            return 0.0f;
        }
        return ss / avg;
    }

    protected float getLuxStability() {
        return mStability;
    }
}
