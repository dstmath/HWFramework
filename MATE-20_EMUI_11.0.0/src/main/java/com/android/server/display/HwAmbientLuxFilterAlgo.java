package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class HwAmbientLuxFilterAlgo {
    private static final int AMBIENT_BUFFER_INIT_NUM = 50;
    private static final int AMBIENT_DEFAULT_LUX = 101;
    private static final int AMBIENT_LIGHT_HORIZON = 20000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int AMBIENT_MIN_LUX = 0;
    private static final int AMBIENT_SCENE_BUFFER_INIT_NUM = 250;
    private static final int AMBIENT_SCENE_HORIZON = 80000;
    private static final int AMBIENT_SCENE_MAX_NUM = 228;
    private static final int DEFAULT_BRIGHTEN_COUNT = -1;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int LOG_INTERVAL_MS = 2000;
    private static final int LUX_BUFFER_FIRST_INDEX = 0;
    private static final int LUX_BUFFER_NUM_FOR_LOG = 6;
    private static final float LUX_DIFF_RATIO = 1.5f;
    private static final float LUX_SMALL_THRESHOLD_STABILITY_TH1 = 0.5f;
    private static final float LUX_SMALL_THRESHOLD_STABILITY_TH2 = 3.0f;
    private static final float LUX_SMALL_THRESHOLD_STABILITY_TH3 = 5.0f;
    private static final float LUX_SMALL_THRESHOLD_TH1 = 10.0f;
    private static final float LUX_SMALL_THRESHOLD_TH2 = 50.0f;
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MIN_BRIGHTNESS = 4;
    private static final float MIN_LUX_THRESHOLD = 1.0f;
    private static final int POST_MAX_MIN_AVG_FILTER_INDEX = 2;
    private static final int POST_MEAN_FILTER_INDEX = 1;
    private static final int PRE_MEAN_FILTER_INDEX = 1;
    private static final int PRE_WEIGHT_MEAN_FILTER_INDEX = 2;
    private static final int SMALL_THRESHOLD_BUFFER_NUM = 15;
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final float STABILITY_MAX_VALUE = 100.0f;
    private static final String TAG = "HwAmbientLuxFilterAlgo";
    private HwRingBuffer mAmbientLightRingBuffer;
    private HwRingBuffer mAmbientLightRingBufferFilter;
    private HwRingBuffer mAmbientLightRingBufferScene;
    private float mAmbientLux;
    private float mAmbientLuxForFrontCamera;
    private float mAmbientLuxNewMax;
    private float mAmbientLuxNewMin;
    private boolean mAutoBrightnessIntervened;
    private float mAutoModeEnableFirstLux;
    private int mBackSensorCoverModeBrightness;
    private long mBrighenDebounceTime = 0;
    private int mBrightPointCnt = -1;
    private float mBrightenDeltaLuxForCurrentBrightness;
    private float mBrightenDeltaLuxMax;
    private float mBrightenDeltaLuxMaxForDcMode = 0.0f;
    private float mBrightenDeltaLuxMaxForGameMode = 0.0f;
    private float mBrightenDeltaLuxMaxForLandScapeMode = 0.0f;
    private float mBrightenDeltaLuxMaxForLandscapeGameMode = 0.0f;
    private float mBrightenDeltaLuxMin;
    private boolean mCoverModeDayEnable;
    private boolean mCoverState;
    private int mCurrentAutoBrightness;
    private boolean mCurrentLuxUpForFrontCameraEnable;
    private boolean mDarkModeEnable = false;
    private boolean mDarkTimeDelayFromBrightnessEnable;
    private long mDarkenDebounceTime = 0;
    private float mDarkenDeltaLuxForBackSensorCoverMode;
    private float mDarkenDeltaLuxForCurrentBrightness;
    private float mDarkenDeltaLuxMax;
    private float mDarkenDeltaLuxMaxForDcMode = 0.0f;
    private float mDarkenDeltaLuxMaxForGameMode = 0.0f;
    private float mDarkenDeltaLuxMaxForLandScapeMode = 0.0f;
    private float mDarkenDeltaLuxMaxForLandscapeGameMode = 0.0f;
    private float mDarkenDeltaLuxMin;
    private final HwBrightnessXmlLoader.Data mData;
    private int mDayModeBeginTime = 5;
    private boolean mDayModeEnable;
    private int mDayModeEndTime = 23;
    private int mDayModeSwitchTime = 30;
    private boolean mDcModeBrightnessEnable;
    private boolean mFirstAmbientLux = true;
    private boolean mFirstSetBrightness = true;
    private boolean mGameModeEnable;
    private boolean mHomeModeEnable;
    private HwAmbientLightTransition mHwAmbientLightTransition;
    private boolean mIsCoverModeFastResponseFlag;
    private boolean mIsLandscapeGameModeState = false;
    private boolean mIsWalkingState = false;
    private boolean mIsclosed;
    private boolean mKeyguardIsLocked;
    private float mKeyguardMinBrightLuxDelta;
    private boolean mKeyguardUnLockedFastResponse;
    private float mKeyguardUnLockedFastResponseDarkenDeltaLux;
    private long mKeyguardUnLockedStartTime = 0;
    private boolean mLandscapeModeEnable;
    private float mLastCloseScreenLux = 0.0f;
    private HwRingBuffer mLastCloseScreenRingBuffer;
    private int mLastCloseTime = -1;
    private float mLastObservedLux;
    private final int mLightSensorRate;
    private final Object mLock = new Object();
    private float mLongTimeFilterLuxTh = 500.0f;
    private int mLongTimeFilterNum = 3;
    private int mLongTimeNoFilterNum = 7;
    private boolean mModeToAutoFastDarkenResponseEnable;
    private boolean mModeToAutoFastDarkenResponseMinLuxEnable;
    private float mModeToAutoFastResponseDarkenDeltaLux;
    private long mModeToAutoFastResponseDarkenStartTime = 0;
    private boolean mModeToAutoFastResponseDarkenStartTimeEnable;
    private long mNeedToDarkenTime = 0;
    private boolean mNeedToUpdateBrightness;
    private long mNormBrighenDebounceTime;
    private long mNormBrighenDebounceTimeForSmallThr;
    private long mNormDarkenDebounceTime;
    private long mNormDarkenDebounceTimeForBackSensorCoverMode = 500;
    private long mNormDarkenDebounceTimeForSmallThr;
    private boolean mOffsetResetEnable;
    private float mOffsetValidAmbientBrightenDeltaLux;
    private float mOffsetValidAmbientDarkenDeltaLux;
    private float mOffsetValidAmbientLux;
    private boolean mPowerStatus;
    private long mPrintLogTime = 0;
    private boolean mProximityPositiveStatus;
    private float mRatioForDarkenBackSensorCoverMode = 0.5f;
    private int mResponseDurationPoints;
    private float mSceneAmbientLuxMax;
    private float mSceneAmbientLuxMin;
    private float mSceneAmbientLuxWeight;
    private float mSecondDarkenModeDarkenDeltaLux = 0.0f;
    private boolean mSecondDarkenModeLongTimeResponseEnable = true;
    private boolean mSecondDarkenModeResponseEnable;
    private float mStability = 0.0f;
    private float mStabilityBrightenConstant = 101.0f;
    private float mStabilityBrightenConstantForSmallThr;
    private float mStabilityDarkenConstant = 101.0f;
    private float mStabilityDarkenConstantForSmallThr;
    private float mStabilityForSmallThr = 0.0f;
    private boolean mTouchProximityState;
    private float mWalkModeMinLux = 0.0f;
    private float mlastFilterLux;

    /* access modifiers changed from: private */
    public enum BrightenResponseTimeState {
        COVER_MODE_FAST_RESEPONSE,
        KEYGUARD_LOCKED,
        OUTDOOR_MODE,
        PROXIMIT_POSITIVE,
        POWER_ON_SLOW,
        POWER_ON_FAST,
        GAME_MODE,
        LANDSCAPE_GAME_MODE,
        LANDSCAPE_MODE,
        MODE_TO_AUTO_FAST_DAKERN,
        DEFAULT_MODE
    }

    public interface Callbacks {
        void updateBrightness();
    }

    static {
        boolean z = false;
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        HWDEBUG = z;
    }

    public HwAmbientLuxFilterAlgo(int lightSensorRate) {
        this.mLightSensorRate = lightSensorRate;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        this.mLastCloseScreenRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferScene = new HwRingBuffer(AMBIENT_SCENE_BUFFER_INIT_NUM);
        this.mData = HwBrightnessXmlLoader.getData();
        this.mHwAmbientLightTransition = new HwAmbientLightTransition();
    }

    public void updateFirstAmbientLuxEnable(boolean isFirstLuxEnable) {
        this.mFirstAmbientLux = isFirstLuxEnable;
    }

    public void handleLightSensorEvent(long time, float lux) {
        synchronized (this.mLock) {
            float currentLux = lux;
            if (!this.mFirstAmbientLux && currentLux > this.mData.darkLightLuxMinThreshold && currentLux < this.mData.darkLightLuxMaxThreshold && this.mData.darkLightLuxMinThreshold < this.mData.darkLightLuxMaxThreshold) {
                currentLux += this.mData.darkLightLuxDelta;
                if (currentLux < 0.0f) {
                    currentLux = 0.0f;
                }
            }
            if (currentLux > getValidMaxAmbientLux()) {
                currentLux = getValidMaxAmbientLux();
            }
            try {
                applyLightSensorMeasurement(time, currentLux);
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                Slog.e(TAG, "ArrayIndexOutOfBoundsException");
            }
        }
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - 20000);
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
    }

    public float getCurrentAmbientLux() {
        return this.mAmbientLux;
    }

    private void setAmbientLux(float lux) {
        this.mAmbientLux = getIntegerOrDecimalAmbientLux(lux);
        if (!this.mData.luxRoundEnable && ((float) Math.round(this.mAmbientLux)) < this.mData.ambientLuxMinRound) {
            this.mAmbientLux = (float) Math.round(lux);
            if (HWFLOW) {
                Slog.i(TAG, "setAmbientLux lux =" + lux + "-->roundlux=" + this.mAmbientLux);
            }
        }
        float f = this.mAmbientLux;
        if (f < LUX_SMALL_THRESHOLD_TH1) {
            this.mStabilityBrightenConstantForSmallThr = 0.5f;
            this.mStabilityDarkenConstantForSmallThr = 0.5f;
        } else if (f < LUX_SMALL_THRESHOLD_TH1 || f >= LUX_SMALL_THRESHOLD_TH2) {
            this.mStabilityBrightenConstantForSmallThr = LUX_SMALL_THRESHOLD_STABILITY_TH3;
            this.mStabilityDarkenConstantForSmallThr = LUX_SMALL_THRESHOLD_STABILITY_TH3;
        } else {
            this.mStabilityBrightenConstantForSmallThr = 3.0f;
            this.mStabilityDarkenConstantForSmallThr = 3.0f;
        }
        float f2 = this.mAmbientLux;
        this.mAmbientLuxNewMax = f2;
        this.mAmbientLuxNewMin = f2;
        this.mSceneAmbientLuxMax = f2;
        this.mSceneAmbientLuxMin = f2;
        updateDebounceTime(this.mAmbientLightRingBuffer, f2);
        this.mResponseDurationPoints = 0;
    }

    private float modifyFirstAmbientLux(float lux) {
        int bufferSize = this.mLastCloseScreenRingBuffer.size();
        if (bufferSize > 0 && this.mData.initNumLastBuffer > 0) {
            float sumLux = 0.0f;
            float interfere = this.mData.initDoubleSensorInterfere;
            int cntValidData = 0;
            for (int i = bufferSize - 1; i >= 0; i--) {
                float tmpLux = this.mLastCloseScreenRingBuffer.getLux(i);
                if (Math.abs(this.mAmbientLightRingBuffer.getTime(0) - this.mLastCloseScreenRingBuffer.getTime(i)) < this.mData.initValidCloseTime && Math.abs(lux - tmpLux) < LUX_DIFF_RATIO * interfere) {
                    sumLux += tmpLux;
                    cntValidData++;
                }
            }
            Slog.i(TAG, "LastScreenBuffer: sumLux=" + sumLux + ", cntValidData=" + cntValidData + ", InambientLux=" + lux);
            if (((float) cntValidData) > SMALL_VALUE && lux < ((float) this.mData.initUpperLuxThreshold) + SMALL_VALUE && sumLux / ((float) cntValidData) < SMALL_VALUE + lux) {
                float ave = sumLux / ((float) cntValidData);
                float lambda = 1.0f / (((float) Math.exp((double) ((-this.mData.initSigmoidFuncSlope) * (interfere - Math.abs(lux - ave))))) + 1.0f);
                Slog.i(TAG, "modifyFirstAmbientLux : lambda=" + lambda + ", ave" + ave + ", ambientLux=" + lux);
                return (lambda * ave) + ((1.0f - lambda) * lux);
            }
        }
        return lux;
    }

    private float calcAmbientLuxInCoverState(float lux) {
        float ambientLux = lux;
        if (!this.mPowerStatus) {
            if (this.mData.lastCloseScreenEnable) {
                ambientLux = this.mLastCloseScreenLux;
            } else if (this.mData.backSensorCoverModeEnable) {
                ambientLux = (float) getLuxFromDefaultBrightnessLevel((float) this.mBackSensorCoverModeBrightness);
                Slog.i(TAG, "BackSensorCoverMode ambientLux=" + ambientLux + ", coverModeBrightness=" + this.mBackSensorCoverModeBrightness);
            } else if (!this.mData.coverModeDayEnable) {
                ambientLux = this.mData.coverModeFirstLux;
            } else if (this.mCoverModeDayEnable) {
                ambientLux = (float) getLuxFromDefaultBrightnessLevel((float) this.mData.coverModeDayBrightness);
            } else {
                ambientLux = (float) getLuxFromDefaultBrightnessLevel((float) getCoverModeBrightnessFromLastScreenBrightness());
                Slog.i(TAG, "LabcCoverMode NewambientLux=" + ambientLux + ",LastScreenBrightness=" + getCoverModeBrightnessFromLastScreenBrightness());
            }
            this.mAmbientLightRingBuffer.putLux(0, ambientLux);
            this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
            if (HWFLOW) {
                Slog.i(TAG, "LabcCoverMode ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
            }
        } else {
            if (!this.mData.coverModelastCloseScreenEnable) {
                ambientLux = this.mLastCloseScreenLux;
                this.mAmbientLightRingBuffer.putLux(0, ambientLux);
                this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
                if (HWFLOW) {
                    Slog.i(TAG, "LabcCoverMode1 use lastCloseScreenLux=" + this.mLastCloseScreenLux);
                }
            }
            if (HWFLOW) {
                Slog.i(TAG, "LabcCoverMode1 ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
            }
        }
        return ambientLux;
    }

    private void updateAmbientLux(long time) {
        float ambientLuxLongTime;
        float filterLux = getPreFilterAmbientLux(time, this.mData.preMethodNum);
        updateBuffer(time, filterLux, AMBIENT_LIGHT_HORIZON);
        updateBufferForScene(time, filterLux, AMBIENT_SCENE_HORIZON);
        this.mlastFilterLux = getFilterLastAmbientLux(time);
        float ambientLux = getPostFilterAmbientLux(time, this.mData.postMethodNum);
        if (this.mData.longTimeFilterEnable) {
            updateLongFilterPara();
            ambientLuxLongTime = getLongTimeFilterLux(time, this.mLongTimeNoFilterNum, this.mLongTimeFilterNum);
        } else {
            ambientLuxLongTime = 0.0f;
        }
        float ambientLux2 = updateAmbientLuxForFirstLux(ambientLux, time);
        updateBrightnessPara(time);
        long nextBrightenTransition = getNextAmbientLightBrighteningTransition(time);
        updateOffsetAmbientLux(ambientLux2, time);
        long nextBrightenTransitionForSmallThr = getNextAmbientLightBrighteningTransitionForSmallThr(time);
        long nextDarkenTransitionForSmallThr = getNextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mDarkenDeltaLuxMin, this.mNormDarkenDebounceTimeForSmallThr);
        boolean needToBrightenForSmallThr = decideToBrightenForSmallThr(ambientLux2);
        boolean needToDarkenForSmallThr = decideToDarkenForSmallThr(ambientLux2);
        boolean needToBrightenForSmallThr2 = needToBrightenForSmallThr && nextBrightenTransitionForSmallThr <= time;
        boolean needToDarkenForSmallThr2 = needToDarkenForSmallThr && nextDarkenTransitionForSmallThr <= time;
        float lastestLux = getNoFilterLastAmbientLux(time);
        boolean needToBrighten = (needToBrightenByLux(ambientLux2, lastestLux, ambientLuxLongTime) && nextBrightenTransition <= time) || needToBrightenForSmallThr2;
        boolean needToDarken = (needToDarkenByLux(ambientLux2, lastestLux) && getNextAmbientLightDarkeningTransition(time) <= time) || needToDarkenForSmallThr2 || needToDarkenForBackSensorCoverMode(time, ambientLux2) || needToDarkenForModeToAutoFastDarkenResponse(time, ambientLux2) || needToDarkenForSecondDarkenMode(time, ambientLux2) || needToDarkenForKeyguardUnlockedFastDarkenResponse(time, ambientLux2);
        printLogForCurrentAmbientLux(time);
        updateModeToAutoFastDarkenResponseState(time);
        updateKeyguardUnLockedFastDarkenResponseState(time);
        boolean needToDarken2 = updateNeedToDarkenOnSecondDarkenMode(time, ambientLux2, needToDarken);
        updateAmbientLuxParametersForFrontCamera(time, ambientLux2);
        if (needToBrighten || needToDarken2) {
            this.mNeedToDarkenTime = time;
            float ambientLux3 = resetAmbientLuxForUpdate(ambientLux2, ambientLuxLongTime, lastestLux, needToBrighten, needToDarken2);
            this.mBrightPointCnt = 0;
            setAmbientLux(ambientLux3);
            resetOffsetAmbientLux(ambientLux3, needToBrighten, needToDarken2, needToBrightenForSmallThr2, needToDarkenForSmallThr2);
            printLogForUpdateAmbientLux(needToBrighten, needToBrightenForSmallThr2, needToDarkenForSmallThr2);
            this.mNeedToUpdateBrightness = true;
        }
    }

    private float updateAmbientLuxForFirstLux(float lux, long time) {
        float ambientLux = lux;
        if (this.mFirstAmbientLux) {
            if (this.mCoverState) {
                this.mCoverState = false;
                ambientLux = calcAmbientLuxInCoverState(ambientLux);
            }
            float ambientLux2 = modifyFirstAmbientLux(ambientLux);
            this.mModeToAutoFastResponseDarkenStartTimeEnable = true;
            ambientLux = resetAmbientLuxForModeToAutoFastDarkenResponse(ambientLux2, time);
            setAmbientLux(ambientLux);
            if (this.mData.offsetValidAmbientLuxEnable) {
                setOffsetValidAmbientLux(ambientLux);
            }
            this.mFirstAmbientLux = false;
            this.mCoverModeDayEnable = false;
            if (this.mData.frontCameraMaxBrightnessEnable) {
                updateAmbientLuxForFrontCamera(ambientLux);
            }
            if (HWFLOW) {
                Slog.i(TAG, "updateAmbientLux: Initializing: mBuffer=" + this.mAmbientLightRingBuffer + ",hoEn=" + this.mHomeModeEnable + ", mLx=" + this.mAmbientLux + ",mCloseLux=" + this.mLastCloseScreenLux + ",mKeyguardLuxDelta=" + this.mKeyguardMinBrightLuxDelta + getLuxThresholdStrings() + ",mAMin=" + this.mAmbientLuxNewMin + ",mAMax=" + this.mAmbientLuxNewMax + ",mFilter=" + this.mAmbientLightRingBufferFilter);
            }
            this.mNeedToUpdateBrightness = true;
        }
        return ambientLux;
    }

    private void updateBrightnessPara(long time) {
        updateNewAmbientLuxFromScene(time, this.mAmbientLightRingBufferScene);
        updateDebounceTime(this.mAmbientLightRingBuffer, this.mAmbientLux);
        int i = this.mBrightPointCnt;
        if (((float) i) > -1.0E-6f) {
            this.mBrightPointCnt = i + 1;
        }
        if (this.mBrightPointCnt > this.mData.outdoorResponseCount) {
            this.mBrightPointCnt = -1;
        }
    }

    private float resetAmbientLuxForModeToAutoFastDarkenResponse(float lux, long time) {
        if (!this.mModeToAutoFastDarkenResponseEnable || !this.mData.resetAmbientLuxEnable) {
            return lux;
        }
        if (((float) this.mCurrentAutoBrightness) > this.mData.resetAmbientLuxStartBrightness && lux < this.mData.resetAmbientLuxTh) {
            this.mModeToAutoFastResponseDarkenStartTime = time;
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxEn,lux=" + lux + ",-->resetAmbientLuxTh=" + this.mData.resetAmbientLuxTh);
            }
            float ambientLux = this.mData.resetAmbientLuxTh;
            this.mAmbientLightRingBuffer.putLux(0, ambientLux);
            this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
            return ambientLux;
        } else if (((float) this.mCurrentAutoBrightness) <= this.mData.resetAmbientLuxStartBrightnessMax || lux >= this.mData.resetAmbientLuxThMax) {
            this.mModeToAutoFastDarkenResponseEnable = false;
            this.mModeToAutoFastDarkenResponseMinLuxEnable = false;
            if (!HWFLOW) {
                return lux;
            }
            Slog.i(TAG, "ResetAmbientLuxDisable,no need ModeToAutoFastDarkenResponseEnable,ambientLux=" + lux + ",brightness=" + this.mCurrentAutoBrightness);
            return lux;
        } else {
            this.mModeToAutoFastResponseDarkenStartTime = time;
            if (!HWFLOW) {
                return lux;
            }
            Slog.i(TAG, "ResetAmbientLuxEn,lux=" + lux + ",brightness=" + this.mCurrentAutoBrightness + ",FastResponseOnly,resetAmbientLuxThMax=" + this.mData.resetAmbientLuxThMax + ",brightnessMax=" + this.mData.resetAmbientLuxStartBrightnessMax);
            return lux;
        }
    }

    private void updateOffsetAmbientLux(float ambientLux, long time) {
        if (this.mData.offsetValidAmbientLuxEnable) {
            long nextBrightenTransitionOffset = getNextAmbientLightBrighteningTransitionForOffset(time);
            long nextDarkenTransitionOffset = getNextAmbientLightDarkeningTransitionExtended(time, this.mOffsetValidAmbientLux, this.mOffsetValidAmbientDarkenDeltaLux, (long) this.mData.offsetDarkenDebounceTime);
            boolean needToBrightenOffset = decideToBrightenForOffset(ambientLux);
            boolean needToDarkenOffset = decideToDarkenForOffset(ambientLux);
            boolean needToDarkenOffset2 = true;
            boolean needToBrightenOffset2 = needToBrightenOffset && nextBrightenTransitionOffset <= time;
            if (!needToDarkenOffset || nextDarkenTransitionOffset > time) {
                needToDarkenOffset2 = false;
            }
            if (this.mData.offsetValidAmbientLuxEnable && (needToBrightenOffset2 || needToDarkenOffset2)) {
                setOffsetValidAmbientLux(ambientLux);
            }
        }
    }

    private void resetOffsetAmbientLux(float ambientLux, boolean needToBrighten, boolean needToDarken, boolean needToBrightenForSmallThr, boolean needToDarkenForSmallThr) {
        if (!this.mData.offsetValidAmbientLuxEnable) {
            return;
        }
        if (needToBrightenForSmallThr || needToDarkenForSmallThr || needToBrighten) {
            if (HWFLOW) {
                Slog.i(TAG, "updateAmbientLux,LastOffLux=" + this.mOffsetValidAmbientLux + ",newOffLux=" + ambientLux + ",needToBrighten=" + needToBrighten + ",BrightenS=" + needToBrightenForSmallThr + ",DarkenS=" + needToDarkenForSmallThr);
            }
            setOffsetValidAmbientLux(ambientLux);
        }
    }

    private boolean needToBrightenByLux(float ambientLux, float lastestLux, float ambientLuxLongTime) {
        boolean isNeedToBrighten = decideToBrighten(ambientLux);
        boolean needToBrightenNew = decideToBrighten(lastestLux);
        boolean needToBrightenLongTime = false;
        if (this.mData.longTimeFilterEnable) {
            needToBrightenLongTime = decideToBrighten(ambientLuxLongTime);
        }
        boolean isNeedToBrighten2 = true;
        if (this.mData.longTimeFilterEnable) {
            if (!isNeedToBrighten || !needToBrightenNew || !needToBrightenLongTime) {
                isNeedToBrighten2 = false;
            }
        } else if (!isNeedToBrighten || !needToBrightenNew) {
            isNeedToBrighten2 = false;
        }
        return isNeedToBrighten2;
    }

    private boolean needToDarkenByLux(float ambientLux, float lastestLux) {
        return decideToDarken(ambientLux) && decideToDarken(lastestLux);
    }

    private float resetAmbientLuxForUpdate(float lux, float ambientLuxLongTime, float lastestLux, boolean needToBrighten, boolean needToDarken) {
        float ambientLux = lux;
        if (this.mData.longTimeFilterEnable && needToBrighten && ambientLuxLongTime < this.mLongTimeFilterLuxTh) {
            Slog.i(TAG, "updateAmbientLux ambientLux=" + ambientLux + ",ambientLuxLongTime=" + ambientLuxLongTime + ",lastestLux=" + lastestLux);
            ambientLux = Math.min(Math.min(ambientLux, ambientLuxLongTime), lastestLux);
        }
        if (this.mModeToAutoFastDarkenResponseEnable && this.mData.resetAmbientLuxEnable && needToBrighten) {
            this.mModeToAutoFastDarkenResponseEnable = false;
            this.mModeToAutoFastResponseDarkenStartTimeEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxDisable,no need ModeToAutoFastDarkenResponseEnable,needToBrighten=" + needToBrighten);
            }
        }
        float ambientLux2 = updateAmbientLuxFromResetAmbientLuxThMin(ambientLux, needToDarken);
        if (needToDarken && Math.round(ambientLux2) == 0 && !this.mKeyguardUnLockedFastResponse && (this.mIsWalkingState || (this.mDayModeEnable && this.mData.dayModeDarkenMinLux > 0.0f))) {
            if (this.mIsWalkingState) {
                ambientLux2 = this.mWalkModeMinLux;
            } else {
                ambientLux2 = this.mData.dayModeDarkenMinLux;
            }
            updateCurrentLuxForBuffer(ambientLux2);
            if (HWFLOW) {
                Slog.i(TAG, "updateAmbientLux darken set 0lux -->ambientLux=" + ambientLux2 + ",wState=" + this.mIsWalkingState);
            }
        }
        return ambientLux2;
    }

    private void printLogForCurrentAmbientLux(long time) {
        String str;
        if (HWFLOW && time - this.mPrintLogTime > 2000) {
            StringBuilder sb = new StringBuilder();
            sb.append("t=");
            sb.append(time);
            sb.append(",lx=");
            sb.append(this.mAmbientLightRingBuffer.toString(6));
            sb.append(",mLx=");
            sb.append(this.mAmbientLux);
            sb.append(",mOffLux=");
            sb.append(this.mOffsetValidAmbientLux);
            sb.append(",s=");
            sb.append(this.mStability);
            sb.append(",ss=");
            sb.append(this.mStabilityForSmallThr);
            sb.append(",Avened=");
            sb.append(this.mAutoBrightnessIntervened);
            sb.append(",mPxs=");
            sb.append(this.mProximityPositiveStatus);
            sb.append(",mTPxs=");
            sb.append(this.mTouchProximityState);
            sb.append(getLuxThresholdStrings());
            if (this.mSecondDarkenModeResponseEnable) {
                str = ",SecondDEnable=" + this.mSecondDarkenModeResponseEnable;
            } else {
                str = "";
            }
            sb.append(str);
            sb.append(",mDt=");
            sb.append(this.mDarkenDebounceTime);
            sb.append(",mBt=");
            sb.append(this.mBrighenDebounceTime);
            sb.append(",mMax=");
            sb.append(this.mAmbientLuxNewMax);
            sb.append(",mMin=");
            sb.append(this.mAmbientLuxNewMin);
            sb.append(",mu=");
            sb.append(this.mSceneAmbientLuxWeight);
            sb.append(",sMax=");
            sb.append(this.mSceneAmbientLuxMax);
            sb.append(",sMin=");
            sb.append(this.mSceneAmbientLuxMin);
            Slog.i(TAG, sb.toString());
            this.mPrintLogTime = time;
        }
    }

    private void printLogForUpdateAmbientLux(boolean needToBrighten, boolean needToBrightenForSmallThr, boolean needToDarkenForSmallThr) {
        String str;
        if (HWFLOW) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateAmbientLux: ");
            if (needToBrighten) {
                str = "Brightened,needBs=" + needToBrightenForSmallThr;
            } else {
                str = "Darkened,needDs=" + needToDarkenForSmallThr;
            }
            sb.append(str);
            sb.append(", mBuffer=");
            sb.append(this.mAmbientLightRingBuffer.toString(6));
            sb.append(",mLux=");
            sb.append(this.mAmbientLux);
            sb.append(",hoEn=");
            sb.append(this.mHomeModeEnable);
            sb.append(getLuxThresholdStrings());
            sb.append(",mAMin=");
            sb.append(this.mAmbientLuxNewMin);
            sb.append(",mAMax=");
            sb.append(this.mAmbientLuxNewMax);
            sb.append(",mPxs=");
            sb.append(this.mProximityPositiveStatus);
            sb.append(",mTPxs=");
            sb.append(this.mTouchProximityState);
            sb.append(",mFilter=");
            sb.append(this.mAmbientLightRingBufferFilter.toString(6));
            Slog.i(TAG, sb.toString());
        }
        if (HWFLOW && this.mIsCoverModeFastResponseFlag) {
            Slog.i(TAG, "CoverModeBResponseTime=" + this.mData.coverModeBrightenResponseTime + ",CoverModeDResponseTime=" + this.mData.coverModeDarkenResponseTime);
        }
        if (HWFLOW && this.mPowerStatus) {
            Slog.i(TAG, "PowerOnBT=" + this.mData.powerOnBrightenDebounceTime + ",PowerOnDT=" + this.mData.powerOnDarkenDebounceTime);
        }
    }

    private String getLuxThresholdStrings() {
        StringBuilder luxThresholdBuilder = new StringBuilder();
        if (getThresholdForGameModeEnable()) {
            float f = this.mAmbientLux;
            float brightenLux = this.mBrightenDeltaLuxMaxForGameMode + f;
            luxThresholdBuilder.append(",mBLuxGM=");
            luxThresholdBuilder.append(brightenLux);
            luxThresholdBuilder.append(",mDluxGM=");
            luxThresholdBuilder.append(f - this.mDarkenDeltaLuxMaxForGameMode);
        } else if (isThresholdForLandscapeGameEnable()) {
            float f2 = this.mAmbientLux;
            float brightenLux2 = this.mBrightenDeltaLuxMaxForLandscapeGameMode + f2;
            luxThresholdBuilder.append(",mBLuxLG=");
            luxThresholdBuilder.append(brightenLux2);
            luxThresholdBuilder.append(",mDluxLG=");
            luxThresholdBuilder.append(f2 - this.mDarkenDeltaLuxMaxForLandscapeGameMode);
        } else if (this.mLandscapeModeEnable) {
            float f3 = this.mAmbientLux;
            float brightenLux3 = this.mBrightenDeltaLuxMaxForLandScapeMode + f3;
            luxThresholdBuilder.append(",mBLuxLS=");
            luxThresholdBuilder.append(brightenLux3);
            luxThresholdBuilder.append(",mDluxLS=");
            luxThresholdBuilder.append(f3 - this.mDarkenDeltaLuxMaxForLandScapeMode);
        } else if (getThresholdForDcModeEnable()) {
            float f4 = this.mAmbientLux;
            float brightenLux4 = this.mBrightenDeltaLuxMaxForDcMode + f4;
            luxThresholdBuilder.append(",mBLuxDC=");
            luxThresholdBuilder.append(brightenLux4);
            luxThresholdBuilder.append(",mDluxDc=");
            luxThresholdBuilder.append(f4 - this.mDarkenDeltaLuxMaxForDcMode);
        } else {
            float darkenLux = this.mAmbientLux;
            float brightenLux5 = this.mBrightenDeltaLuxMax + darkenLux;
            luxThresholdBuilder.append(",mBLux=");
            luxThresholdBuilder.append(brightenLux5);
            luxThresholdBuilder.append(",mDlux=");
            luxThresholdBuilder.append(darkenLux - this.mDarkenDeltaLuxMax);
        }
        return luxThresholdBuilder.toString();
    }

    private void updateNewAmbientLuxFromScene(long time, HwRingBuffer hwBuffer) {
        int bufferSize;
        float f = this.mAmbientLux;
        this.mAmbientLuxNewMax = f;
        this.mAmbientLuxNewMin = f;
        this.mSceneAmbientLuxMax = f;
        this.mSceneAmbientLuxMin = f;
        int i = this.mResponseDurationPoints;
        if (i == Integer.MAX_VALUE) {
            this.mResponseDurationPoints = Integer.MAX_VALUE;
        } else {
            this.mResponseDurationPoints = i + 1;
        }
        if (!getThresholdForGameModeEnable() && !this.mLandscapeModeEnable && !getThresholdForDcModeEnable() && !isThresholdForLandscapeGameEnable() && (bufferSize = hwBuffer.size()) != 0 && bufferSize >= this.mData.sceneGapPoints && this.mResponseDurationPoints - this.mData.sceneMinPoints >= this.mData.sceneGapPoints && this.mData.sceneMaxPoints >= this.mData.sceneMinPoints && this.mData.sceneMaxPoints + this.mData.sceneGapPoints <= AMBIENT_SCENE_MAX_NUM) {
            updateSceneBufferAmbientLuxMaxMinAvg(hwBuffer, this.mResponseDurationPoints < this.mData.sceneMaxPoints + this.mData.sceneGapPoints ? bufferSize - this.mResponseDurationPoints : (bufferSize - this.mData.sceneMaxPoints) - this.mData.sceneGapPoints, bufferSize - this.mData.sceneGapPoints);
            this.mSceneAmbientLuxWeight = ((float) this.mData.sceneGapPoints) / ((float) this.mResponseDurationPoints);
            float f2 = this.mAmbientLux;
            float f3 = this.mSceneAmbientLuxMax;
            if (f2 > f3) {
                float f4 = this.mSceneAmbientLuxWeight;
                this.mAmbientLuxNewMax = (f2 * f4) + ((1.0f - f4) * f3);
            }
            float f5 = this.mAmbientLux;
            float f6 = this.mSceneAmbientLuxMin;
            if (f5 > f6) {
                float f7 = this.mSceneAmbientLuxWeight;
                this.mAmbientLuxNewMin = (f5 * f7) + ((1.0f - f7) * f6);
            }
            correctAmbientLux(time);
        }
    }

    private void updateSceneBufferAmbientLuxMaxMinAvg(HwRingBuffer buffer, int start, int end) {
        int bufferSize = buffer.size();
        if (bufferSize == 0 || end < start || start > bufferSize - 1 || end < 0 || start < 0 || end > bufferSize - 1) {
            Slog.i(TAG, "SceneBufferAmbientLux input error,end=" + end + ",start=" + start + ",bufferSize=" + bufferSize);
            return;
        }
        float luxSum = 0.0f;
        float luxMin = buffer.getLux(start);
        float luxMax = buffer.getLux(start);
        for (int i = start; i <= end; i++) {
            float lux = buffer.getLux(i);
            if (lux > luxMax) {
                luxMax = lux;
            }
            if (lux < luxMin) {
                luxMin = lux;
            }
            luxSum += lux;
        }
        float luxMean = getIntegerOrDecimalAmbientLux(luxSum / ((float) ((end - start) + 1)));
        this.mSceneAmbientLuxMax = (this.mData.sceneAmbientLuxMaxWeight * luxMean) + ((1.0f - this.mData.sceneAmbientLuxMaxWeight) * luxMax);
        this.mSceneAmbientLuxMin = (this.mData.sceneAmbientLuxMinWeight * luxMean) + ((1.0f - this.mData.sceneAmbientLuxMinWeight) * luxMin);
    }

    private void correctAmbientLux(long time) {
        float ambientLuxDarkenDelta = calculateLuxThresholdDeltaNew(this.mData.darkenlinePoints, this.mAmbientLux, false);
        float ambientLuxNewMaxBrightenDelta = calculateLuxThresholdDeltaNew(this.mData.brightenlinePoints, this.mAmbientLuxNewMax, true);
        float ambientLuxNewMinBrightenDelta = calculateLuxThresholdDeltaNew(this.mData.brightenlinePoints, this.mAmbientLuxNewMin, false);
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMax - 1.0E-5f) {
            if (HWFLOW && time - this.mPrintLogTime > 2000) {
                Slog.i(TAG, "Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
            }
            this.mAmbientLuxNewMax = this.mAmbientLux;
        }
        if (this.mAmbientLux > (this.mAmbientLuxNewMax + ambientLuxNewMaxBrightenDelta) - 1.0E-5f) {
            if (HWFLOW && time - this.mPrintLogTime > 2000) {
                Slog.i(TAG, " Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxNewMaxBrightenDelta=" + ambientLuxNewMaxBrightenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
            }
            this.mAmbientLuxNewMax = this.mAmbientLux;
        }
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMin - 1.0E-5f) {
            if (HWFLOW && time - this.mPrintLogTime > 2000) {
                Slog.i(TAG, " Reset mAmbientLuxNewMin:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMin=" + this.mAmbientLuxNewMin);
            }
            this.mAmbientLuxNewMin = this.mAmbientLux;
        }
        if (this.mAmbientLux > (this.mAmbientLuxNewMin + ambientLuxNewMinBrightenDelta) - 1.0E-5f) {
            if (HWFLOW && time - this.mPrintLogTime > 2000) {
                Slog.i(TAG, " Reset mAmbientLuxNewMin:mAmbientLux" + this.mAmbientLux + ", ambientLuxNewMinBrightenDelta=" + ambientLuxNewMinBrightenDelta + ", mAmbientLuxNewMin=" + this.mAmbientLuxNewMin);
            }
            this.mAmbientLuxNewMin = this.mAmbientLux;
        }
    }

    public boolean needToUpdateBrightness() {
        return this.mNeedToUpdateBrightness;
    }

    public boolean updateNeedToUpdateBrightnessFlag() {
        this.mNeedToUpdateBrightness = false;
        return false;
    }

    private BrightenResponseTimeState getCurrentBrightenResponseTimeState() {
        if (this.mIsCoverModeFastResponseFlag) {
            return BrightenResponseTimeState.COVER_MODE_FAST_RESEPONSE;
        }
        if (getKeyguardLockedBrightenEnable()) {
            return BrightenResponseTimeState.KEYGUARD_LOCKED;
        }
        if (getOutdoorModeBrightenEnable()) {
            return BrightenResponseTimeState.OUTDOOR_MODE;
        }
        if (getProximityPositiveBrightenEnable()) {
            return BrightenResponseTimeState.PROXIMIT_POSITIVE;
        }
        if (this.mPowerStatus && getSlowResponsePowerStatus()) {
            return BrightenResponseTimeState.POWER_ON_SLOW;
        }
        if (this.mPowerStatus && !getSlowResponsePowerStatus()) {
            return BrightenResponseTimeState.POWER_ON_FAST;
        }
        if (this.mGameModeEnable) {
            return BrightenResponseTimeState.GAME_MODE;
        }
        if (this.mIsLandscapeGameModeState) {
            return BrightenResponseTimeState.LANDSCAPE_GAME_MODE;
        }
        if (this.mLandscapeModeEnable) {
            return BrightenResponseTimeState.LANDSCAPE_MODE;
        }
        if (this.mModeToAutoFastDarkenResponseEnable) {
            return BrightenResponseTimeState.MODE_TO_AUTO_FAST_DAKERN;
        }
        return BrightenResponseTimeState.DEFAULT_MODE;
    }

    private long getNextAmbientLightBrighteningTime(long earliedTime) {
        switch (getCurrentBrightenResponseTimeState()) {
            case COVER_MODE_FAST_RESEPONSE:
                this.mBrighenDebounceTime = this.mData.coverModeBrightenResponseTime;
                break;
            case KEYGUARD_LOCKED:
                this.mBrighenDebounceTime = (long) this.mData.keyguardResponseBrightenTime;
                break;
            case OUTDOOR_MODE:
                this.mBrighenDebounceTime = (long) this.mData.outdoorResponseBrightenTime;
                break;
            case PROXIMIT_POSITIVE:
                this.mBrighenDebounceTime = (long) this.mData.proximityResponseBrightenTime;
                break;
            case POWER_ON_SLOW:
                this.mBrighenDebounceTime = (long) (this.mData.powerOnBrightenDebounceTime + this.mData.initSlowReponseBrightTime);
                break;
            case POWER_ON_FAST:
                this.mBrighenDebounceTime = (long) this.mData.powerOnBrightenDebounceTime;
                break;
            case GAME_MODE:
                this.mBrighenDebounceTime = this.mData.gameModeBrightenDebounceTime;
                break;
            case LANDSCAPE_GAME_MODE:
                this.mBrighenDebounceTime = this.mData.landscapeGameModeBrightenDebounceTime;
                break;
            case LANDSCAPE_MODE:
                this.mBrighenDebounceTime = (long) this.mData.landscapeModeBrightenDebounceTime;
                break;
            case MODE_TO_AUTO_FAST_DAKERN:
                this.mBrighenDebounceTime = (long) this.mData.resetAmbientLuxBrightenDebounceTime;
                break;
            default:
                this.mBrighenDebounceTime = this.mNormBrighenDebounceTime;
                break;
        }
        return this.mBrighenDebounceTime + earliedTime;
    }

    private long getNextAmbientLightDarkeningTime(long earliedTime) {
        if (this.mIsCoverModeFastResponseFlag) {
            this.mDarkenDebounceTime = this.mData.coverModeDarkenResponseTime;
        } else if (getKeyguardLockedDarkenEnable()) {
            this.mDarkenDebounceTime = (long) this.mData.keyguardResponseDarkenTime;
        } else if (getOutdoorModeDarkenEnable()) {
            this.mDarkenDebounceTime = (long) this.mData.outdoorResponseDarkenTime;
        } else if (this.mPowerStatus) {
            this.mDarkenDebounceTime = (long) this.mData.powerOnDarkenDebounceTime;
        } else if (this.mGameModeEnable) {
            this.mDarkenDebounceTime = this.mData.gameModeDarkenDebounceTime;
        } else if (this.mIsLandscapeGameModeState) {
            this.mDarkenDebounceTime = this.mData.landscapeGameModeDarkenDebounceTime;
        } else if (this.mLandscapeModeEnable) {
            this.mDarkenDebounceTime = (long) this.mData.landscapeModeDarkenDebounceTime;
        } else {
            this.mDarkenDebounceTime = this.mNormDarkenDebounceTime;
        }
        return this.mDarkenDebounceTime + earliedTime;
    }

    private boolean getKeyguardLockedBrightenEnable() {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getKeyguardLocked no lux");
            return false;
        } else if (!this.mKeyguardIsLocked || this.mAmbientLightRingBuffer.getLux(bufferSize - 1) < this.mData.keyguardLuxThreshold || this.mData.keyguardResponseBrightenTime < 0) {
            return false;
        } else {
            return !getProximityPositiveBrightenEnable();
        }
    }

    private boolean getOutdoorModeBrightenEnable() {
        return ((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseBrightenTime > 0;
    }

    private boolean getProximityPositiveBrightenEnable() {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getProximityPositive no lux");
            return false;
        } else if (!this.mData.allowLabcUseProximity || !this.mProximityPositiveStatus || this.mAmbientLightRingBuffer.getLux(bufferSize - 1) >= this.mData.proximityLuxThreshold || this.mData.proximityResponseBrightenTime <= 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean getSlowResponsePowerStatus() {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize <= 0) {
            return true;
        }
        for (int i = 0; i < bufferSize; i++) {
            if (this.mAmbientLightRingBuffer.getLux(i) > ((float) this.mData.initSlowReponseUpperLuxThreshold) + SMALL_VALUE) {
                return false;
            }
        }
        return true;
    }

    private boolean getKeyguardLockedDarkenEnable() {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getKeyguardLocked no lux");
            return false;
        } else if (!this.mKeyguardIsLocked || this.mAmbientLightRingBuffer.getLux(bufferSize - 1) < this.mData.keyguardLuxThreshold || this.mData.keyguardResponseDarkenTime < 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean getOutdoorModeDarkenEnable() {
        return ((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseDarkenTime > 0;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.mPowerStatus = powerStatus;
    }

    public void clear() {
        synchronized (this.mLock) {
            if (HWFLOW) {
                Slog.i(TAG, "clear buffer data and algo flags.");
            }
            this.mLastCloseScreenLux = this.mAmbientLux;
            if (HWFLOW) {
                Slog.i(TAG, "LabcCoverMode clear: mLastCloseScreenLux=" + this.mLastCloseScreenLux);
            }
            if (this.mData.initNumLastBuffer > 0) {
                int bufferSize = this.mAmbientLightRingBuffer.size();
                for (int i = 0; i < bufferSize; i++) {
                    this.mLastCloseScreenRingBuffer.push(this.mAmbientLightRingBuffer.getTime(i), this.mAmbientLightRingBuffer.getLux(i));
                }
                int pruneNum = Math.max(this.mLastCloseScreenRingBuffer.size() - this.mData.initNumLastBuffer, 0);
                if (pruneNum > 0) {
                    this.mLastCloseScreenRingBuffer.prune(1 + this.mLastCloseScreenRingBuffer.getTime(pruneNum - 1));
                }
                if (HWFLOW) {
                    Slog.i(TAG, "mLastCloseScreenRingBuffer=" + this.mLastCloseScreenRingBuffer.toString(this.mLastCloseScreenRingBuffer.size()));
                }
            } else {
                Slog.i(TAG, "mLastCloseScreenRingBuffer is set empty!");
            }
            this.mIsCoverModeFastResponseFlag = false;
            this.mAutoBrightnessIntervened = false;
            this.mProximityPositiveStatus = false;
            this.mAmbientLightRingBuffer.clear();
            this.mAmbientLightRingBufferFilter.clear();
            this.mAmbientLightRingBufferScene.clear();
            this.mBrightPointCnt = -1;
            updateLastCloseTime();
        }
    }

    private void updateLastCloseTime() {
        if (this.mData.dayModeAlgoEnable || this.mData.offsetResetEnable) {
            this.mFirstSetBrightness = false;
            Calendar currentCalendar = Calendar.getInstance();
            int lastCloseDay = currentCalendar.get(6);
            int lastCloseHour = currentCalendar.get(11);
            int lastCloseMinute = currentCalendar.get(12);
            this.mLastCloseTime = (lastCloseDay * 24 * 60) + (lastCloseHour * 60) + lastCloseMinute;
            if (HWFLOW) {
                Slog.i(TAG, "DayMode: lastCloseDay=" + lastCloseDay + ",lastCloseHour=" + lastCloseHour + ",lastCloseMinute=" + lastCloseMinute + ",mLastCloseTime=" + this.mLastCloseTime);
            }
        }
    }

    private void updateBuffer(long time, float ambientLux, int horizon) {
        this.mAmbientLightRingBufferFilter.push(time, ambientLux);
        this.mAmbientLightRingBufferFilter.prune(time - ((long) horizon));
    }

    private void updateBufferForScene(long time, float ambientLux, int horizon) {
        this.mAmbientLightRingBufferScene.push(time, ambientLux);
        this.mAmbientLightRingBufferScene.prune(time - ((long) horizon));
    }

    private void updateDebounceTime(HwRingBuffer buffer, float lux) {
        long j;
        long j2;
        long j3;
        updateStability(buffer);
        float luxRound = getIntegerOrDecimalAmbientLux(lux);
        if (luxRound >= this.mData.brightTimeDelayLuxThreshold || this.mlastFilterLux >= this.mData.brightTimeDelayLuxThreshold || !this.mData.brightTimeDelayEnable) {
            if (this.mDcModeBrightnessEnable) {
                j3 = this.mData.dcModeBrightenDebounceTime;
            } else {
                j3 = (long) (((float) this.mData.brighenDebounceTime) * (((this.mData.brightenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / STABILITY_MAX_VALUE) + 1.0f));
            }
            this.mNormBrighenDebounceTime = j3;
        } else {
            this.mNormBrighenDebounceTime = (long) this.mData.brightTimeDelay;
        }
        if ((luxRound < this.mData.darkTimeDelayLuxThreshold || this.mDarkTimeDelayFromBrightnessEnable) && this.mData.darkTimeDelayEnable) {
            float ambientLuxDarkenDelta = calculateLuxThresholdDeltaNew(this.mData.darkenlinePoints, this.mAmbientLux, false);
            float currentAmbientLux = buffer.getLux(buffer.size() - 1);
            float luxNormalizedFactor = (this.mData.darkTimeDelayBeta2 * (this.mAmbientLux - currentAmbientLux)) + (this.mData.darkTimeDelayBeta1 * ((this.mAmbientLux - currentAmbientLux) - ambientLuxDarkenDelta)) + 1.0f;
            if (luxNormalizedFactor < SMALL_VALUE) {
                j2 = (long) (((float) this.mData.darkTimeDelay) + this.mData.darkTimeDelayBeta0);
            } else {
                j2 = ((long) this.mData.darkTimeDelay) + ((long) ((this.mData.darkTimeDelayBeta0 * ((this.mData.darkTimeDelayBeta2 * 1.0f) + 1.0f)) / luxNormalizedFactor));
            }
            this.mNormDarkenDebounceTime = j2;
        } else {
            if (this.mDcModeBrightnessEnable) {
                j = this.mData.dcModeDarkenDebounceTime;
            } else {
                j = (long) (((float) this.mData.darkenDebounceTime) * (((this.mData.darkenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / STABILITY_MAX_VALUE) + 1.0f));
            }
            this.mNormDarkenDebounceTime = j;
        }
        this.mNormBrighenDebounceTimeForSmallThr = (long) this.mData.brighenDebounceTimeForSmallThr;
        this.mNormDarkenDebounceTimeForSmallThr = (long) this.mData.darkenDebounceTimeForSmallThr;
        setDarkenThresholdNew(this.mAmbientLuxNewMin);
        setBrightenThresholdNew(this.mAmbientLuxNewMax);
    }

    private void updateStability(HwRingBuffer buffer) {
        float stability = (float) this.mData.stabilityConstant;
        float stabilityForSmallThr = calculateStabilityForSmallThr(buffer);
        if (stability > STABILITY_MAX_VALUE) {
            this.mStability = STABILITY_MAX_VALUE;
        } else if (stability < ((float) this.mData.stabilityConstant)) {
            this.mStability = (float) this.mData.stabilityConstant;
        } else {
            this.mStability = stability;
        }
        this.mStabilityForSmallThr = Math.min(stabilityForSmallThr, (float) STABILITY_MAX_VALUE);
    }

    private void setBrightenThresholdNew(float amLux) {
        this.mBrightenDeltaLuxMax = calculateLuxThresholdDeltaNew(this.mData.brightenlinePoints, amLux, true);
        this.mBrightenDeltaLuxMin = this.mBrightenDeltaLuxMax * this.mData.ratioForBrightnenSmallThr;
        this.mBrightenDeltaLuxMax *= ((this.mData.brightenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / STABILITY_MAX_VALUE) + 1.0f;
        if (((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseBrightenRatio > 0.0f) {
            this.mBrightenDeltaLuxMax *= this.mData.outdoorResponseBrightenRatio;
        }
        this.mBrightenDeltaLuxMaxForLandScapeMode = calculateLuxThresholdDeltaNew(this.mData.brightenlinePointsForLandscapeMode, this.mAmbientLux, true);
        if (this.mData.gameModeLuxThresholdEnable) {
            this.mBrightenDeltaLuxMaxForGameMode = calculateLuxThresholdDeltaNew(this.mData.brightenlinePointsForGameMode, this.mAmbientLux, true);
        }
        if (this.mData.isLandscapeGameModeEnable) {
            this.mBrightenDeltaLuxMaxForLandscapeGameMode = calculateLuxThresholdDeltaNew(this.mData.brightenLinePointsForLandscapeGameMode, this.mAmbientLux, true);
        }
        if (this.mData.dcModeLuxThresholdEnable) {
            this.mBrightenDeltaLuxMaxForDcMode = calculateLuxThresholdDeltaNew(this.mData.brightenlinePointsForDcMode, this.mAmbientLux, true);
        }
    }

    private void setDarkenThresholdNew(float amLux) {
        float f;
        this.mDarkenDeltaLuxMax = calculateLuxThresholdDeltaNew(this.mData.darkenlinePoints, amLux, false);
        if (this.mAmbientLux < LUX_SMALL_THRESHOLD_TH1) {
            f = this.mDarkenDeltaLuxMax;
        } else {
            f = this.mDarkenDeltaLuxMax * this.mData.ratioForDarkenSmallThr;
        }
        this.mDarkenDeltaLuxMin = f;
        float f2 = this.mDarkenDeltaLuxMax;
        this.mDarkenDeltaLuxForBackSensorCoverMode = this.mRatioForDarkenBackSensorCoverMode * f2;
        this.mModeToAutoFastResponseDarkenDeltaLux = f2 * this.mData.resetAmbientLuxDarkenRatio;
        this.mDarkenDeltaLuxMax *= ((this.mData.darkenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / STABILITY_MAX_VALUE) + 1.0f;
        if (((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseDarkenRatio > 0.0f) {
            this.mDarkenDeltaLuxMax *= this.mData.outdoorResponseDarkenRatio;
        }
        this.mDarkenDeltaLuxMaxForLandScapeMode = calculateLuxThresholdDeltaNew(this.mData.darkenlinePointsForLandscapeMode, this.mAmbientLux, false);
        if (this.mData.gameModeLuxThresholdEnable) {
            this.mDarkenDeltaLuxMaxForGameMode = calculateLuxThresholdDeltaNew(this.mData.darkenlinePointsForGameMode, this.mAmbientLux, false);
        }
        if (this.mData.isLandscapeGameModeEnable) {
            this.mDarkenDeltaLuxMaxForLandscapeGameMode = calculateLuxThresholdDeltaNew(this.mData.darkenLinePointsForLandscapeGameMode, this.mAmbientLux, false);
        }
        if (this.mData.dcModeLuxThresholdEnable) {
            this.mDarkenDeltaLuxMaxForDcMode = calculateLuxThresholdDeltaNew(this.mData.darkenlinePointsForDcMode, this.mAmbientLux, false);
        }
        if (this.mData.secondDarkenModeEnable) {
            this.mSecondDarkenModeDarkenDeltaLux = this.mDarkenDeltaLuxMax * this.mData.secondDarkenModeDarkenDeltaLuxRatio;
        }
        if (this.mData.keyguardUnlockedFastDarkenEnable) {
            this.mKeyguardUnLockedFastResponseDarkenDeltaLux = this.mDarkenDeltaLuxMax * this.mData.keyguardUnlockedDarkenRatio;
        }
    }

    private float calculateLuxThresholdDeltaNew(List<PointF> linePoints, float amLux, boolean isBrighten) {
        float luxThreshold = 0.0f;
        if (linePoints == null) {
            return 0.0f;
        }
        PointF prePoint = null;
        for (PointF curPoint : linePoints) {
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (amLux >= curPoint.x) {
                prePoint = curPoint;
                luxThreshold = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                Slog.w(TAG, "Brighten_prePoint.x <= nexPoint.x,x" + curPoint.x + ", y = " + curPoint.y);
                return 1.0f;
            } else {
                float luxThreshold2 = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (amLux - prePoint.x)) + prePoint.y;
                if (!isBrighten) {
                    return Math.max(luxThreshold2, 1.0f);
                }
                return luxThreshold2;
            }
        }
        return luxThreshold;
    }

    private float getPreFilterAmbientLux(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return getPreFilterMeanFilterLux(now);
        }
        if (filterMethodNum == 2) {
            return getPreFilterWeightedMeanFilterLux(now);
        }
        return getPreFilterNoFilterLux(now);
    }

    private float getPreFilterNoFilterLux(long now) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize != 0) {
            return this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
        }
        Slog.e(TAG, "getPreFilterNoFilterLux: No ambient light readings available, return 0");
        return 101.0f;
    }

    private float getPreFilterMeanFilterLux(long now) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getPreFilterMeanFilterLux: No ambient light readings available, return 0");
            return 101.0f;
        } else if (this.mData.preMeanFilterNum <= 0 || this.mData.preMeanFilterNoFilterNum < this.mData.preMeanFilterNum) {
            Slog.e(TAG, "getPreFilterMeanFilterLux: ErrorPara, return 0, MeanFilterNum=" + this.mData.preMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum);
            return 101.0f;
        } else if (bufferSize <= this.mData.preMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
        } else {
            float sum = 0.0f;
            for (int i = bufferSize - 1; i >= bufferSize - this.mData.preMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return getIntegerOrDecimalAmbientLux(sum / ((float) this.mData.preMeanFilterNum));
        }
    }

    private void updateLongFilterPara() {
        this.mLongTimeNoFilterNum = this.mData.longTimeNoFilterNum;
        this.mLongTimeFilterNum = this.mData.longTimeFilterNum;
        this.mLongTimeFilterLuxTh = this.mData.longTimeFilterLuxTh;
    }

    private float getLongTimeFilterLux(long now, int noFilterNum, int filterNum) {
        int luxSize = this.mAmbientLightRingBuffer.size();
        if (luxSize == 0) {
            Slog.e(TAG, "getLongTimeFilterLux: No ambient light readings available, return 0");
            return 101.0f;
        } else if (filterNum <= 0) {
            Slog.w(TAG, "getLongTimeFilterLux: filterNum=" + filterNum);
            return this.mAmbientLightRingBuffer.getLux(luxSize - 1);
        } else if (luxSize <= noFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(luxSize - 1);
        } else {
            float sum = 0.0f;
            if (luxSize <= filterNum) {
                for (int i = luxSize - 1; i >= 0; i--) {
                    sum += this.mAmbientLightRingBuffer.getLux(i);
                }
                return getIntegerOrDecimalAmbientLux(sum / ((float) luxSize));
            }
            for (int i2 = luxSize - 1; i2 >= luxSize - filterNum; i2--) {
                sum += this.mAmbientLightRingBuffer.getLux(i2);
            }
            return getIntegerOrDecimalAmbientLux(sum / ((float) filterNum));
        }
    }

    private float getPreFilterWeightedMeanFilterLux(long now) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getPreFilterWeightedMeanFilterLux: No ambient light readings available, return 0");
            return 101.0f;
        } else if (this.mData.preWeightedMeanFilterNum <= 0 || this.mData.preWeightedMeanFilterNoFilterNum < this.mData.preWeightedMeanFilterNum) {
            Slog.e(TAG, "getPreFilterWeightedMeanFilterLux: ErrorPara, return 0, WeightedMeanFilterNum=" + this.mData.preWeightedMeanFilterNum + ",WeightedMeanFilterNoFilterNum=" + this.mData.preWeightedMeanFilterNoFilterNum);
            return 101.0f;
        } else {
            float curPointLux = this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
            if (bufferSize <= this.mData.preWeightedMeanFilterNoFilterNum) {
                return curPointLux;
            }
            float maxLux = 0.0f;
            float sum = 0.0f;
            float totalWeight = 0.0f;
            for (int i = bufferSize - 1; i >= bufferSize - this.mData.preWeightedMeanFilterMaxFuncLuxNum; i--) {
                float curPointLux2 = this.mAmbientLightRingBuffer.getLux(i);
                if (curPointLux2 >= maxLux) {
                    maxLux = curPointLux2;
                }
            }
            for (int i2 = bufferSize - 1; i2 >= bufferSize - this.mData.preWeightedMeanFilterNum; i2--) {
                float weight = (this.mAmbientLightRingBuffer.getLux(i2) != 0.0f || maxLux > this.mData.preWeightedMeanFilterLuxTh) ? 1.0f : this.mData.preWeightedMeanFilterAlpha;
                totalWeight += weight;
                sum += this.mAmbientLightRingBuffer.getLux(i2) * weight;
            }
            return getIntegerOrDecimalAmbientLux(sum / totalWeight);
        }
    }

    private float getNoFilterLastAmbientLux(long now) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize != 0) {
            return this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
        }
        Slog.e(TAG, "OrigAmbient: No ambient light readings available, return 0");
        return 101.0f;
    }

    private float getFilterLastAmbientLux(long now) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
        }
        Slog.e(TAG, "FilterLastAmbient: No ambient light readings available, return 0");
        return 101.0f;
    }

    private float getPostFilterAmbientLux(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return getPostFilterMeanFilterLux(now);
        }
        if (filterMethodNum == 2) {
            return getPostFilterMaxMinAvgFilterLux(now);
        }
        return getPostfilterNoFilterLux(now);
    }

    private float getPostfilterNoFilterLux(long now) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
        }
        Slog.e(TAG, "getPostfilterNoFilterLux: No ambient light readings available, return 0");
        return 101.0f;
    }

    private float getPostFilterMeanFilterLux(long now) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getPreFilterMeanFilterLux: No ambient light readings available, return 0");
            return 101.0f;
        } else if (this.mData.postMeanFilterNum <= 0 || this.mData.postMeanFilterNoFilterNum < this.mData.postMeanFilterNum) {
            Slog.e(TAG, "getPostFilterMeanFilterLux: ErrorPara, return 0, MeanFilterNum=" + this.mData.postMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.postMeanFilterNum);
            return 101.0f;
        } else if (bufferSize <= this.mData.postMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
        } else {
            float sum = 0.0f;
            for (int i = bufferSize - 1; i >= bufferSize - this.mData.postMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBufferFilter.getLux(i);
            }
            return getIntegerOrDecimalAmbientLux(sum / ((float) this.mData.postMeanFilterNum));
        }
    }

    private float getPostFilterMaxMinAvgFilterLux(long now) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "getPostFilterMaxMinAvgFilterLux: No ambient light readings available, return 0");
            return 101.0f;
        } else if (this.mData.postMaxMinAvgFilterNum <= 0 || this.mData.postMaxMinAvgFilterNoFilterNum < this.mData.postMaxMinAvgFilterNum) {
            Slog.e(TAG, "getPostFilterMaxMinAvgFilterLux: ErrorPara, return 0, PostMaxMinAvgFilterNoFilterNum=" + this.mData.postMaxMinAvgFilterNoFilterNum + ",PostMaxMinAvgFilterNum=" + this.mData.postMaxMinAvgFilterNum);
            return 101.0f;
        } else if (bufferSize <= this.mData.postMaxMinAvgFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
        } else {
            float sum = this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
            float luxMin = this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
            float luxMax = this.mAmbientLightRingBufferFilter.getLux(bufferSize - 1);
            for (int i = bufferSize - 2; i >= bufferSize - this.mData.postMaxMinAvgFilterNum; i--) {
                if (luxMin > this.mAmbientLightRingBufferFilter.getLux(i)) {
                    luxMin = this.mAmbientLightRingBufferFilter.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBufferFilter.getLux(i)) {
                    luxMax = this.mAmbientLightRingBufferFilter.getLux(i);
                }
                sum += this.mAmbientLightRingBufferFilter.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / ((float) (this.mData.postMaxMinAvgFilterNum - 2));
        }
    }

    private long getNextAmbientLightBrighteningTransition(long time) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize < 1) {
            return time;
        }
        long earliestValidTime = time;
        for (int i = bufferSize - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) - getCurrentBrightenAmbientLux() > getCurrentBrightenDelta())) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long getNextAmbientLightBrighteningTransitionForSmallThr(long time) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize < 1) {
            return time;
        }
        long earliestValidTime = time;
        for (int i = bufferSize - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMin)) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return this.mNormBrighenDebounceTimeForSmallThr + earliestValidTime;
    }

    private long getNextAmbientLightDarkeningTransition(long time) {
        int bufferSize = this.mAmbientLightRingBufferFilter.size();
        if (bufferSize < 1) {
            return time;
        }
        long earliestValidTime = time;
        for (int i = bufferSize - 1; i >= 0; i--) {
            if (!(getCurrentDarkenAmbientLux() - this.mAmbientLightRingBufferFilter.getLux(i) >= getCurrentDarkenDelta())) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    private long getNextAmbientLightDarkeningTransitionExtended(long time, float lux, float deltaLux, long debounceTime) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBufferFilter.size() - 1;
        while (i >= 0 && lux - this.mAmbientLightRingBufferFilter.getLux(i) >= deltaLux) {
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
            i--;
        }
        return earliestValidTime + debounceTime;
    }

    private boolean decideToDarkenForBackSensorCoverMode(float ambientLux) {
        return (((this.mAmbientLux - ambientLux) > this.mDarkenDeltaLuxForBackSensorCoverMode ? 1 : ((this.mAmbientLux - ambientLux) == this.mDarkenDeltaLuxForBackSensorCoverMode ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus;
    }

    private boolean needToDarkenForBackSensorCoverMode(long time, float lux) {
        if (!this.mData.backSensorCoverModeEnable || !this.mIsCoverModeFastResponseFlag || !decideToDarkenForBackSensorCoverMode(lux) || getNextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mDarkenDeltaLuxForBackSensorCoverMode, this.mNormDarkenDebounceTimeForBackSensorCoverMode) > time) {
            return false;
        }
        if (!HWFLOW) {
            return true;
        }
        Slog.i(TAG, "BackSensorCoverMode needToDarkenForBackSensorCoverMode");
        return true;
    }

    private boolean decideToBrighten(float ambientLux) {
        return (((ambientLux - getCurrentBrightenAmbientLux()) > getCurrentBrightenDelta() ? 1 : ((ambientLux - getCurrentBrightenAmbientLux()) == getCurrentBrightenDelta() ? 0 : -1)) >= 0 && (this.mStability > this.mStabilityBrightenConstant ? 1 : (this.mStability == this.mStabilityBrightenConstant ? 0 : -1)) < 0 && ((ambientLux - getCurrentBrightenAmbientLux()) > this.mKeyguardMinBrightLuxDelta ? 1 : ((ambientLux - getCurrentBrightenAmbientLux()) == this.mKeyguardMinBrightLuxDelta ? 0 : -1)) >= 0 && ((ambientLux - getCurrentBrightenAmbientLux()) > this.mBrightenDeltaLuxForCurrentBrightness ? 1 : ((ambientLux - getCurrentBrightenAmbientLux()) == this.mBrightenDeltaLuxForCurrentBrightness ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened;
    }

    private boolean decideToBrightenForSmallThr(float ambientLux) {
        if (getThresholdForDcModeEnable() || this.mAmbientLux < this.mData.luxThNoResponseForSmallThr) {
            return false;
        }
        if (!(ambientLux - this.mAmbientLux >= this.mBrightenDeltaLuxMin && ambientLux - getCurrentBrightenAmbientLux() >= this.mKeyguardMinBrightLuxDelta && this.mStabilityForSmallThr < this.mStabilityBrightenConstantForSmallThr && ambientLux - this.mAmbientLuxNewMax >= this.mBrightenDeltaLuxForCurrentBrightness) || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus || this.mLandscapeModeEnable) {
            return false;
        }
        return true;
    }

    private boolean decideToDarken(float ambientLux) {
        if (this.mData.secondDarkenModeNoResponseDarkenTime > 0 && this.mSecondDarkenModeResponseEnable) {
            return false;
        }
        if (!(getCurrentDarkenAmbientLux() - ambientLux >= getCurrentDarkenDelta() && this.mStability <= this.mStabilityDarkenConstant && getCurrentDarkenAmbientLux() - ambientLux >= this.mDarkenDeltaLuxForCurrentBrightness) || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return false;
        }
        return true;
    }

    private boolean decideToDarkenForSmallThr(float ambientLux) {
        if (getThresholdForDcModeEnable() || this.mAmbientLux < this.mData.luxThNoResponseForSmallThr) {
            return false;
        }
        if (!(this.mAmbientLux - ambientLux >= this.mDarkenDeltaLuxMin && this.mStabilityForSmallThr <= this.mStabilityDarkenConstantForSmallThr && this.mAmbientLuxNewMin - ambientLux >= this.mDarkenDeltaLuxForCurrentBrightness) || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus || this.mLandscapeModeEnable) {
            return false;
        }
        return true;
    }

    public boolean getProximityPositiveEnable() {
        return this.mData.allowLabcUseProximity && this.mProximityPositiveStatus;
    }

    public float getOffsetValidAmbientLux() {
        return this.mData.offsetValidAmbientLuxEnable ? this.mOffsetValidAmbientLux : this.mAmbientLux;
    }

    private float getValidAmbientLux(float lux) {
        float luxOut = lux;
        if (luxOut < 0.0f) {
            luxOut = 0.0f;
        }
        if (luxOut > getValidMaxAmbientLux()) {
            return getValidMaxAmbientLux();
        }
        return luxOut;
    }

    private float getValidMaxAmbientLux() {
        if (this.mData.maxValidAmbientLux > 40000.0f) {
            return this.mData.maxValidAmbientLux;
        }
        return 40000.0f;
    }

    public void setCurrentAmbientLux(float lux) {
        if (((int) this.mAmbientLux) != ((int) lux)) {
            Slog.i(TAG, "setOffsetLux mAmbientLux=" + this.mAmbientLux + ",lux=" + lux);
            this.mAmbientLux = getValidAmbientLux(lux);
        }
    }

    private void setOffsetValidAmbientLux(float lux) {
        this.mOffsetValidAmbientLux = getIntegerOrDecimalAmbientLux(lux);
        if (!this.mData.luxRoundEnable && ((float) Math.round(this.mOffsetValidAmbientLux)) < this.mData.ambientLuxMinRound) {
            this.mOffsetValidAmbientLux = (float) Math.round(lux);
            if (HWFLOW) {
                Slog.i(TAG, "setOffset lux=" + lux + "-->mOffsetRoundLux=" + this.mOffsetValidAmbientLux);
            }
        }
        this.mOffsetValidAmbientBrightenDeltaLux = calculateLuxThresholdDeltaNew(this.mData.brightenlinePoints, this.mOffsetValidAmbientLux, true);
        this.mOffsetValidAmbientDarkenDeltaLux = calculateLuxThresholdDeltaNew(this.mData.darkenlinePoints, this.mOffsetValidAmbientLux, false);
    }

    private long getNextAmbientLightBrighteningTransitionForOffset(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) - this.mOffsetValidAmbientLux > this.mOffsetValidAmbientBrightenDeltaLux)) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return ((long) this.mData.offsetBrightenDebounceTime) + earliestValidTime;
    }

    private boolean decideToBrightenForOffset(float ambientLux) {
        return (((ambientLux - this.mOffsetValidAmbientLux) > this.mOffsetValidAmbientBrightenDeltaLux ? 1 : ((ambientLux - this.mOffsetValidAmbientLux) == this.mOffsetValidAmbientBrightenDeltaLux ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened;
    }

    private boolean decideToDarkenForOffset(float ambientLux) {
        return (((this.mOffsetValidAmbientLux - ambientLux) > this.mOffsetValidAmbientDarkenDeltaLux ? 1 : ((this.mOffsetValidAmbientLux - ambientLux) == this.mOffsetValidAmbientDarkenDeltaLux ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened;
    }

    private float calculateStabilityForSmallThr(HwRingBuffer buffer) {
        int bufferSize = buffer.size();
        if (bufferSize <= 1) {
            return 0.0f;
        }
        if (bufferSize <= 15) {
            return calculateStabilityFactor(buffer, 0, bufferSize - 1);
        }
        return calculateStabilityFactor(buffer, 0, 14);
    }

    private float calculateStabilityFactor(HwRingBuffer buffer, int start, int end) {
        int size = (end - start) + 1;
        float sum = 0.0f;
        float sigma = 0.0f;
        if (size <= 1) {
            return 0.0f;
        }
        for (int i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        float avg = sum / ((float) size);
        for (int i2 = start; i2 <= end; i2++) {
            sigma += (buffer.getLux(i2) - avg) * (buffer.getLux(i2) - avg);
        }
        float sigmaAvg = sigma / ((float) (size - 1));
        if (avg == 0.0f) {
            return 0.0f;
        }
        return sigmaAvg / avg;
    }

    public boolean reportValueWhenSensorOnChange() {
        return this.mData.reportValueWhenSensorOnChange;
    }

    public void setCoverModeStatus(boolean isclosed) {
        if (!isclosed && this.mIsclosed) {
            this.mCoverState = true;
        }
        this.mIsclosed = isclosed;
    }

    public void setCoverModeFastResponseFlag(boolean isFast) {
        this.mIsCoverModeFastResponseFlag = isFast;
        if (HWFLOW) {
            Slog.i(TAG, "LabcCoverMode mIsCoverModeFastResponseFlag=" + this.mIsCoverModeFastResponseFlag);
        }
    }

    public void setBackSensorCoverModeBrightness(int brightness) {
        if (brightness > 0) {
            this.mBackSensorCoverModeBrightness = brightness;
        }
    }

    public void setProximityState(boolean proximityPositive) {
        if (this.mProximityPositiveStatus != proximityPositive) {
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity mProximityPositiveStatus=" + this.mProximityPositiveStatus + "-->proximityPositive=" + proximityPositive);
            }
            this.mProximityPositiveStatus = proximityPositive;
        }
        if (!this.mProximityPositiveStatus) {
            this.mNeedToUpdateBrightness = true;
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity Proximity sets brightness");
            }
        }
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
        if (this.mData.keyguardUnlockedFastDarkenEnable) {
            if (isLocked) {
                this.mKeyguardUnLockedFastResponse = false;
                this.mKeyguardMinBrightLuxDelta = this.mData.keyguardBrightenLuxDeltaMin;
                if (HWFLOW) {
                    Slog.i(TAG, "KeyguardUnLocked LuxDelta=" + this.mKeyguardMinBrightLuxDelta + ",isLocked=" + isLocked + ",mKeyguardUnLockedFastResponse=" + this.mKeyguardUnLockedFastResponse);
                }
            } else {
                this.mKeyguardUnLockedFastResponse = true;
                this.mKeyguardUnLockedStartTime = SystemClock.uptimeMillis();
                if (HWFLOW) {
                    Slog.i(TAG, "KeyguardUnLocked LuxDelta=" + this.mKeyguardMinBrightLuxDelta + ",isLocked=" + isLocked + ",mKeyguardUnLockedFastResponse=" + this.mKeyguardUnLockedFastResponse + ",mKeyguardUnLockedStartTime=" + this.mKeyguardUnLockedStartTime);
                }
            }
        }
        this.mKeyguardIsLocked = isLocked;
    }

    public boolean getOutdoorAnimationFlag() {
        return ((float) this.mData.outdoorLowerLuxThreshold) < this.mAmbientLux;
    }

    public boolean getDayModeEnable() {
        return this.mDayModeEnable;
    }

    public boolean getOffsetResetEnable() {
        return this.mOffsetResetEnable;
    }

    public void setAutoModeEnableFirstLux(float lux) {
        this.mAutoModeEnableFirstLux = getValidAmbientLux(lux);
        Slog.i(TAG, "setAutoModeEnableFirstLux=" + this.mAutoModeEnableFirstLux);
    }

    public void setHomeModeEnable(boolean enable) {
        if (this.mData.homeModeEnable) {
            this.mHomeModeEnable = enable;
            updateDayModeTime();
            updateDayModeEnable();
            Slog.i(TAG, "updatemHoModeEnable=" + enable + ",mDayModeEnable=" + this.mDayModeEnable);
        }
    }

    private void updateDayModeTime() {
        if (this.mHomeModeEnable) {
            this.mDayModeBeginTime = this.mData.homeModeDayBeginTime;
            this.mDayModeEndTime = this.mData.homeModeDayEndTime;
            this.mDayModeSwitchTime = this.mData.homeModeSwitchTime;
            return;
        }
        this.mDayModeBeginTime = this.mData.dayModeBeginTime;
        this.mDayModeEndTime = this.mData.dayModeEndTime;
        this.mDayModeSwitchTime = this.mData.dayModeSwitchTime;
    }

    private void updateDayModeEnable() {
        int currentHour = Calendar.getInstance().get(11);
        this.mDayModeEnable = false;
        int i = this.mDayModeBeginTime;
        int i2 = this.mDayModeEndTime;
        if (i < i2) {
            if (currentHour >= i && currentHour < i2) {
                this.mDayModeEnable = true;
            }
        } else if (currentHour >= i || currentHour < i2) {
            this.mDayModeEnable = true;
        }
    }

    public void setDayModeEnable() {
        Calendar currentCalendar = Calendar.getInstance();
        int openDay = currentCalendar.get(6);
        int openHour = currentCalendar.get(11);
        int openMinute = currentCalendar.get(12);
        int openTime = (openDay * 24 * 60) + (openHour * 60) + openMinute;
        if (this.mData.dayModeAlgoEnable) {
            updateDayModeTime();
            if (this.mFirstSetBrightness || openTime - this.mLastCloseTime >= this.mDayModeSwitchTime) {
                updateDayModeEnable();
            }
        }
        if (this.mData.offsetResetEnable) {
            updateOffsetResetEnable(openTime);
        }
        if (HWFLOW) {
            Slog.i(TAG, "DayMode:openDay=" + openDay + ",openHour=" + openHour + ",openMinute=" + openMinute + ",openTime=" + openTime + ", mLastCloseTime" + this.mLastCloseTime + ", mFirstSetBrightness" + this.mFirstSetBrightness + ",mDayModeEnable=" + this.mDayModeEnable + ",mOffsetResetEnable=" + this.mOffsetResetEnable + ",offsetResetSwitchTime=" + this.mData.offsetResetSwitchTime);
        }
    }

    private void updateOffsetResetEnable(int openTime) {
        this.mOffsetResetEnable = false;
        if (openTime - this.mLastCloseTime >= this.mData.offsetResetSwitchTime) {
            this.mOffsetResetEnable = true;
            Slog.i(TAG, "offsetResetEnable detime=" + (openTime - this.mLastCloseTime));
        } else if (!this.mDarkModeEnable || openTime - this.mLastCloseTime < this.mData.offsetResetSwitchTimeForDarkMode || this.mData.offsetResetSwitchTimeForDarkMode < 0) {
            float luxBright = this.mAutoModeEnableFirstLux + calculateLuxThresholdDeltaNew(this.mData.brightenlinePoints, this.mAutoModeEnableFirstLux, true);
            float luxDark = this.mAutoModeEnableFirstLux - calculateLuxThresholdDeltaNew(this.mData.darkenlinePoints, this.mAutoModeEnableFirstLux, false);
            if (Math.abs(this.mAutoModeEnableFirstLux - this.mLastCloseScreenLux) > ((float) this.mData.offsetResetShortLuxDelta)) {
                float f = this.mLastCloseScreenLux;
                if ((luxBright > f || luxDark < f) && openTime - this.mLastCloseTime >= this.mData.offsetResetShortSwitchTime) {
                    this.mOffsetResetEnable = true;
                    Slog.i(TAG, "offsetResetEnableShort detime=" + (openTime - this.mLastCloseTime) + ",mFirstLux=" + this.mAutoModeEnableFirstLux + ",mCloseLux=" + this.mLastCloseScreenLux + ",luxBright=" + luxBright + ",luxDark=" + luxDark);
                }
            }
        } else {
            this.mOffsetResetEnable = true;
            Slog.i(TAG, "DarkBrightMode offsetResetEnable detime=" + (openTime - this.mLastCloseTime));
        }
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        if (this.mData.coverModeDayEnable) {
            return getCoverModeBrightnessFromLastScreenLux(this.mAmbientLux);
        }
        return this.mData.coverModeDayBrightness;
    }

    public void setCoverModeDayEnable(boolean coverModeDayEnable) {
        this.mCoverModeDayEnable = coverModeDayEnable;
    }

    public boolean getCoverModeDayEnable() {
        return this.mCoverModeDayEnable;
    }

    private int getCoverModeBrightnessFromLastScreenLux(float amLux) {
        if (this.mData.coverModeBrightnessLinePoints == null || amLux < 0.0f) {
            Slog.e(TAG, "LabcCoverMode error input,set MIN_BRIGHTNESS,amLux=" + amLux);
            return 4;
        }
        float coverModebrightness = 0.0f;
        PointF prePoint = null;
        Iterator<PointF> iter = this.mData.coverModeBrightnessLinePoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PointF curPoint = iter.next();
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (amLux >= curPoint.x) {
                prePoint = curPoint;
                coverModebrightness = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                return 4;
            } else {
                coverModebrightness = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (amLux - prePoint.x)) + prePoint.y;
            }
        }
        return (int) coverModebrightness;
    }

    private int getLuxFromDefaultBrightnessLevel(float brightnessLevel) {
        if (this.mData.defaultBrightnessLinePoints == null || brightnessLevel < 0.0f) {
            Slog.e(TAG, "LabcCoverMode,error input,set MIN_Lux,brightnessLevel=" + brightnessLevel);
            return 101;
        } else if (brightnessLevel == 255.0f) {
            Slog.i(TAG, "LabcCoverMode,brightnessLevel=MAX_Brightness,getMaxLux=" + getValidMaxAmbientLux());
            return (int) getValidMaxAmbientLux();
        } else {
            float lux = 0.0f;
            PointF prePoint = null;
            Iterator<PointF> iter = this.mData.defaultBrightnessLinePoints.iterator();
            while (true) {
                if (!iter.hasNext()) {
                    break;
                }
                PointF curPoint = iter.next();
                if (prePoint == null) {
                    prePoint = curPoint;
                }
                if (brightnessLevel >= curPoint.y) {
                    prePoint = curPoint;
                    lux = prePoint.x;
                } else if (curPoint.y <= prePoint.y) {
                    return 101;
                } else {
                    lux = (((curPoint.x - prePoint.x) / (curPoint.y - prePoint.y)) * (brightnessLevel - prePoint.y)) + prePoint.x;
                }
            }
            return (int) lux;
        }
    }

    public void setGameModeEnable(boolean enable) {
        this.mGameModeEnable = enable;
        if (HWFLOW) {
            Slog.i(TAG, "GameBrightMode set mGameModeEnable=" + this.mGameModeEnable);
        }
    }

    public void setDarkTimeDelayFromBrightnessEnable(boolean enable) {
        if (HWFLOW && enable != this.mDarkTimeDelayFromBrightnessEnable) {
            Slog.i(TAG, "DarkTimeDelayFromBrightnessEnable=" + this.mDarkTimeDelayFromBrightnessEnable + ",enable=" + enable);
        }
        this.mDarkTimeDelayFromBrightnessEnable = enable;
    }

    private boolean getThresholdForGameModeEnable() {
        return this.mGameModeEnable && this.mData.gameModeLuxThresholdEnable;
    }

    private boolean getThresholdForDcModeEnable() {
        return this.mDcModeBrightnessEnable && this.mData.dcModeLuxThresholdEnable;
    }

    private float getCurrentBrightenAmbientLux() {
        if (getThresholdForGameModeEnable() || this.mLandscapeModeEnable || getThresholdForDcModeEnable() || isThresholdForLandscapeGameEnable()) {
            return this.mAmbientLux;
        }
        return this.mAmbientLuxNewMax;
    }

    private float getCurrentDarkenAmbientLux() {
        if (getThresholdForGameModeEnable() || this.mLandscapeModeEnable || getThresholdForDcModeEnable() || isThresholdForLandscapeGameEnable()) {
            return this.mAmbientLux;
        }
        return this.mAmbientLuxNewMin;
    }

    private float getCurrentBrightenDelta() {
        if (getThresholdForGameModeEnable()) {
            return this.mBrightenDeltaLuxMaxForGameMode;
        }
        if (isThresholdForLandscapeGameEnable()) {
            return this.mBrightenDeltaLuxMaxForLandscapeGameMode;
        }
        if (this.mLandscapeModeEnable) {
            return this.mBrightenDeltaLuxMaxForLandScapeMode;
        }
        if (getThresholdForDcModeEnable()) {
            return this.mBrightenDeltaLuxMaxForDcMode;
        }
        return this.mBrightenDeltaLuxMax;
    }

    private float getCurrentDarkenDelta() {
        if (getThresholdForGameModeEnable()) {
            return this.mDarkenDeltaLuxMaxForGameMode;
        }
        if (isThresholdForLandscapeGameEnable()) {
            return this.mDarkenDeltaLuxMaxForLandscapeGameMode;
        }
        if (this.mLandscapeModeEnable) {
            return this.mDarkenDeltaLuxMaxForLandScapeMode;
        }
        if (getThresholdForDcModeEnable()) {
            return this.mDarkenDeltaLuxMaxForDcMode;
        }
        return this.mDarkenDeltaLuxMax;
    }

    public void updateLandscapeMode(boolean enable) {
        if (HWFLOW && enable != this.mLandscapeModeEnable) {
            Slog.d(TAG, "LandScapeBrightMode mLandscapeModeEnable=" + this.mLandscapeModeEnable + "-->enable=" + enable);
        }
        this.mLandscapeModeEnable = enable;
    }

    public void updateTouchProximityState(boolean touchProximityState) {
        if (HWDEBUG && touchProximityState != this.mTouchProximityState) {
            Slog.d(TAG, "LandScapeBrightMode mTouchProximityState=" + this.mTouchProximityState + "-->touchProximityState=" + touchProximityState);
        }
        this.mTouchProximityState = touchProximityState;
    }

    public void updateBrightnessModeChangeManualState(boolean state) {
        this.mModeToAutoFastDarkenResponseEnable = state;
        this.mModeToAutoFastDarkenResponseMinLuxEnable = state;
        if (HWFLOW) {
            Slog.i(TAG, "set mModeToAutoFastDarkenResponseEnable=" + state);
        }
    }

    private boolean needToDarkenForSecondDarkenMode(long time, float lux) {
        if (!this.mData.secondDarkenModeEnable || !this.mSecondDarkenModeResponseEnable || getNextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mSecondDarkenModeDarkenDeltaLux, this.mData.secondDarkenModeDarkenDebounceTime) > time || !decideToDarkenForSecondDarkenMode(lux)) {
            return false;
        }
        if (!HWFLOW) {
            return true;
        }
        Slog.i(TAG, "updateAmbientLux needToDarkenForSecondDarkenMode");
        return true;
    }

    private boolean decideToDarkenForSecondDarkenMode(float ambientLux) {
        return (((this.mAmbientLux - ambientLux) > this.mSecondDarkenModeDarkenDeltaLux ? 1 : ((this.mAmbientLux - ambientLux) == this.mSecondDarkenModeDarkenDeltaLux ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus;
    }

    private boolean needToDarkenForModeToAutoFastDarkenResponse(long time, float lux) {
        if (!this.mModeToAutoFastDarkenResponseEnable || !this.mData.resetAmbientLuxEnable || getNextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mModeToAutoFastResponseDarkenDeltaLux, (long) this.mData.resetAmbientLuxDarkenDebounceTime) > time || !decideToDarkenFoModeToAutoFastDarkenResponse(lux)) {
            return false;
        }
        if (!HWFLOW) {
            return true;
        }
        Slog.i(TAG, "updateAmbientLux needToDarkenForModeToAutoFastDarkenResponse");
        return true;
    }

    private boolean decideToDarkenFoModeToAutoFastDarkenResponse(float ambientLux) {
        return (((this.mAmbientLux - ambientLux) > this.mModeToAutoFastResponseDarkenDeltaLux ? 1 : ((this.mAmbientLux - ambientLux) == this.mModeToAutoFastResponseDarkenDeltaLux ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus;
    }

    public void setCurrentAutoBrightness(int brightness) {
        float darkenLuxTh;
        float brightenLuxTh;
        int i;
        if (brightness <= 0) {
            brightenLuxTh = 0.0f;
            darkenLuxTh = 0.0f;
        } else {
            brightenLuxTh = getValidAmbientLux(calculateLuxThresholdDeltaNew(this.mData.brightenlinePointsForBrightnessLevel, (float) brightness, true));
            darkenLuxTh = getValidAmbientLux(calculateLuxThresholdDeltaNew(this.mData.darkenlinePointsForBrightnessLevel, (float) brightness, false));
        }
        this.mBrightenDeltaLuxForCurrentBrightness = brightenLuxTh;
        this.mDarkenDeltaLuxForCurrentBrightness = darkenLuxTh;
        if (HWFLOW && brightness != (i = this.mCurrentAutoBrightness) && i == 0) {
            Slog.i(TAG, "updateCurrentAutoBrightness,bright=" + this.mCurrentAutoBrightness + ",-->brightness=" + brightness + ",bLux=" + this.mBrightenDeltaLuxForCurrentBrightness + ",dLux=" + this.mDarkenDeltaLuxForCurrentBrightness + ",mBLux=" + this.mBrightenDeltaLuxMax + ",mDLux=" + this.mDarkenDeltaLuxMax);
        }
        this.mCurrentAutoBrightness = brightness;
    }

    public void setDcModeBrightnessEnable(boolean enable) {
        if (enable != this.mDcModeBrightnessEnable) {
            Slog.i(TAG, "DcModeBrightnessEnable=" + this.mDcModeBrightnessEnable + "-->enable=" + enable);
            this.mDcModeBrightnessEnable = enable;
        }
    }

    private boolean updateNeedToDarkenOnSecondDarkenMode(long time, float lux, boolean needToDarken) {
        boolean needToDarkenState = needToDarken;
        long timeDelta = time - this.mNeedToDarkenTime;
        long noResponseDarkenTime = this.mSecondDarkenModeLongTimeResponseEnable ? this.mData.secondDarkenModeNoResponseDarkenTime : this.mData.secondDarkenModeNoResponseDarkenTimeMin;
        if (this.mSecondDarkenModeResponseEnable && this.mData.secondDarkenModeNoResponseDarkenTime > 0 && timeDelta > noResponseDarkenTime) {
            this.mSecondDarkenModeResponseEnable = false;
            if (needToDarkenForSecondDarkenModeOnCurrentLux(time, lux)) {
                needToDarkenState = true;
            }
            if (HWFLOW) {
                Slog.i(TAG, "setSecondDarkenModeResponseEnable=false,timeDelta=" + timeDelta + ",needToDarkenState=" + needToDarkenState + ",noResponseDarkenTime=" + noResponseDarkenTime);
            }
        }
        return needToDarkenState;
    }

    private boolean needToDarkenForSecondDarkenModeOnCurrentLux(long time, float lux) {
        if (!this.mData.secondDarkenModeEnable || getNextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mSecondDarkenModeDarkenDeltaLux, this.mData.secondDarkenModeAfterNoResponseCheckTime) > time || !decideToDarkenForSecondDarkenMode(lux)) {
            return false;
        }
        if (!HWFLOW) {
            return true;
        }
        Slog.i(TAG, "updateAmbientLux needToDarkenForSecondDarkenModeOnCurrentLux");
        return true;
    }

    private void updateModeToAutoFastDarkenResponseState(long time) {
        long timeDelta = time - this.mModeToAutoFastResponseDarkenStartTime;
        if (this.mModeToAutoFastDarkenResponseEnable && this.mData.resetAmbientLuxEnable && this.mModeToAutoFastResponseDarkenStartTimeEnable && timeDelta > ((long) this.mData.resetAmbientLuxFastDarkenValidTime)) {
            this.mModeToAutoFastResponseDarkenStartTimeEnable = false;
            this.mModeToAutoFastDarkenResponseEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxDisable,no need ModeToAutoFastDarkenResponseEnable,mCurrentAutoBrightness=" + this.mCurrentAutoBrightness + ",timeDelta=" + timeDelta + ",validTime=" + this.mData.resetAmbientLuxFastDarkenValidTime);
            }
        }
    }

    private float updateAmbientLuxFromResetAmbientLuxThMin(float lux, boolean needDarken) {
        float ambientLuxValue = lux;
        if (this.mModeToAutoFastDarkenResponseEnable && this.mData.resetAmbientLuxEnable && this.mModeToAutoFastDarkenResponseMinLuxEnable && needDarken && this.mData.resetAmbientLuxThMin > 0.0f && lux < this.mData.resetAmbientLuxThMin) {
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxEn,lux=" + lux + ",-->resetAmbientLuxThMin=" + this.mData.resetAmbientLuxThMin);
            }
            this.mModeToAutoFastDarkenResponseMinLuxEnable = false;
            ambientLuxValue = this.mData.resetAmbientLuxThMin;
            updateCurrentLuxForBuffer(ambientLuxValue);
        }
        this.mSecondDarkenModeResponseEnable = false;
        if (this.mData.secondDarkenModeEnable && needDarken && this.mData.secondDarkenModeMinLuxTh > 0.0f && lux < this.mData.secondDarkenModeMinLuxTh && this.mAmbientLux > this.mData.secondDarkenModeMaxLuxTh) {
            this.mSecondDarkenModeResponseEnable = true;
            ambientLuxValue = this.mData.secondDarkenModeMinLuxTh;
            updateCurrentLuxForBuffer(ambientLuxValue);
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxEn,lux=" + lux + ",-->resetlux=" + this.mData.secondDarkenModeMinLuxTh + ",lastAmbientLux=" + this.mAmbientLux + ",mNeedToDarkenTime=" + this.mNeedToDarkenTime);
            }
        }
        return ambientLuxValue;
    }

    private void updateCurrentLuxForBuffer(float lux) {
        if (this.mAmbientLightRingBuffer.size() >= 1 && this.mAmbientLightRingBufferFilter.size() >= 1) {
            HwRingBuffer hwRingBuffer = this.mAmbientLightRingBuffer;
            hwRingBuffer.putLux(hwRingBuffer.size() - 1, lux);
            this.mAmbientLightRingBufferFilter.putLux(this.mAmbientLightRingBuffer.size() - 1, lux);
        }
    }

    public boolean getFastDarkenDimmingEnable() {
        return this.mModeToAutoFastDarkenResponseEnable;
    }

    public boolean getKeyguardUnlockedFastDarkenDimmingEnable() {
        return this.mKeyguardUnLockedFastResponse && this.mData.keyguardUnlockedFastDarkenEnable;
    }

    private void updateKeyguardUnLockedFastDarkenResponseState(long time) {
        if (this.mData.keyguardUnlockedFastDarkenEnable) {
            if (this.mAmbientLux > this.mData.keyguardUnlockedFastDarkenMaxLux && this.mKeyguardUnLockedFastResponse) {
                this.mKeyguardUnLockedFastResponse = false;
                if (this.mKeyguardMinBrightLuxDelta > 0.0f) {
                    this.mKeyguardMinBrightLuxDelta = 0.0f;
                }
                if (HWFLOW) {
                    Slog.i(TAG, "KeyguardUnLocked reset mKeyguardMinBrightLuxDelta=" + this.mKeyguardMinBrightLuxDelta + ",mKeyguardUnLockedFastResponse=" + this.mKeyguardUnLockedFastResponse + ",mAmbientLux=" + this.mAmbientLux);
                }
            }
            long timeDelta = time - this.mKeyguardUnLockedStartTime;
            if (this.mKeyguardUnLockedFastResponse && timeDelta > this.mData.keyguardUnlockedLuxDeltaValidTime && this.mKeyguardMinBrightLuxDelta > 0.0f) {
                this.mKeyguardMinBrightLuxDelta = 0.0f;
                if (HWFLOW) {
                    Slog.i(TAG, "KeyguardUnLocked reset mKeyguardMinBrightLuxDelta=" + this.mKeyguardMinBrightLuxDelta + ",timeDelta=" + timeDelta);
                }
            }
            if (this.mKeyguardUnLockedFastResponse && timeDelta > this.mData.keyguardUnlockedFastDarkenValidTime) {
                this.mKeyguardUnLockedFastResponse = false;
                if (HWFLOW) {
                    Slog.i(TAG, "KeyguardUnLocked, reset KeyguardUnLockedFastResponse false,timeDelta=" + timeDelta + ",mAmbientLux=" + this.mAmbientLux);
                }
            }
        }
    }

    private boolean needToDarkenForKeyguardUnlockedFastDarkenResponse(long time, float lux) {
        if (!this.mKeyguardUnLockedFastResponse || !this.mData.keyguardUnlockedFastDarkenEnable || getNextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mKeyguardUnLockedFastResponseDarkenDeltaLux, this.mData.keyguardUnlockedDarkenDebounceTime) > time || !decideToDarkenForForKeyguardUnlockedDarkenResponse(lux)) {
            return false;
        }
        if (!HWFLOW) {
            return true;
        }
        Slog.i(TAG, "updateAmbientLux needToDarkenForKeyguardUnlockedFastDarkenResponse");
        return true;
    }

    private boolean decideToDarkenForForKeyguardUnlockedDarkenResponse(float ambientLux) {
        return (((this.mAmbientLux - ambientLux) > this.mKeyguardUnLockedFastResponseDarkenDeltaLux ? 1 : ((this.mAmbientLux - ambientLux) == this.mKeyguardUnLockedFastResponseDarkenDeltaLux ? 0 : -1)) >= 0) && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus;
    }

    public void updateSecondDarkenModeNoResponseLongEnable(boolean enable) {
        if (this.mSecondDarkenModeLongTimeResponseEnable != enable) {
            if (HWFLOW) {
                Slog.i(TAG, "secondDarkenMode mSecondDarkenModeLongTimeResponseEnable=" + enable);
            }
            this.mSecondDarkenModeLongTimeResponseEnable = enable;
        }
    }

    public void updateDarkModeEnable(boolean enable) {
        if (HWFLOW) {
            Slog.i(TAG, "DarkBrightMode mDarkModeEnable=" + this.mDarkModeEnable + "->enable=" + enable);
        }
        this.mDarkModeEnable = enable;
    }

    public int getCurrentFilteredAmbientLux() {
        return (int) this.mlastFilterLux;
    }

    private float getIntegerOrDecimalAmbientLux(float lux) {
        return this.mData.luxRoundEnable ? (float) Math.round(lux) : ((float) Math.round(lux * LUX_SMALL_THRESHOLD_TH1)) / LUX_SMALL_THRESHOLD_TH1;
    }

    private void updateAmbientLuxParametersForFrontCamera(long time, float ambientLux) {
        HwAmbientLightTransition hwAmbientLightTransition;
        if (this.mData.frontCameraMaxBrightnessEnable && (hwAmbientLightTransition = this.mHwAmbientLightTransition) != null) {
            long nextBrightenTransitionForFrontCamera = hwAmbientLightTransition.getNextAmbientLightTransitionTime(this.mAmbientLightRingBufferFilter, time, true);
            long nextDarkenTransitionForFrontCamera = this.mHwAmbientLightTransition.getNextAmbientLightTransitionTime(this.mAmbientLightRingBufferFilter, time, false);
            if ((!this.mCurrentLuxUpForFrontCameraEnable && ambientLux >= this.mData.frontCameraBrightenLuxThreshold && nextBrightenTransitionForFrontCamera <= time) || (this.mCurrentLuxUpForFrontCameraEnable && ambientLux < this.mData.frontCameraDarkenLuxThreshold && nextDarkenTransitionForFrontCamera <= time)) {
                updateAmbientLuxForFrontCamera(ambientLux);
            }
        }
    }

    private void updateAmbientLuxForFrontCamera(float lux) {
        this.mAmbientLuxForFrontCamera = lux;
        this.mCurrentLuxUpForFrontCameraEnable = this.mAmbientLuxForFrontCamera >= this.mData.frontCameraLuxThreshold;
        if (HWFLOW) {
            Slog.i(TAG, "updateAmbientLuxForFrontCamera,luxForFrontCamera=" + lux + ",mCurrentLuxUpEnable=" + this.mCurrentLuxUpForFrontCameraEnable);
        }
    }

    public float getAmbientLuxForFrontCamera() {
        return this.mAmbientLuxForFrontCamera;
    }

    /* access modifiers changed from: package-private */
    public void updateWalkStatus(boolean isWalkingState) {
        if (isWalkingState) {
            this.mWalkModeMinLux = this.mData.walkModeMinLux;
        } else {
            this.mWalkModeMinLux = 0.0f;
        }
        if (isWalkingState != this.mIsWalkingState && HWFLOW) {
            Slog.i(TAG, "wState=" + isWalkingState + ",mWMinLux=" + this.mWalkModeMinLux);
        }
        this.mIsWalkingState = isWalkingState;
    }

    /* access modifiers changed from: package-private */
    public void setLandscapeGameModeState(boolean isLandscapeModeState) {
        if (HWFLOW) {
            Slog.i(TAG, "setLandscapeGameModeState mIsLandscapeGameModeState=" + this.mIsLandscapeGameModeState + "->isLandscapeModeState=" + isLandscapeModeState);
        }
        this.mIsLandscapeGameModeState = isLandscapeModeState;
    }

    private boolean isThresholdForLandscapeGameEnable() {
        return this.mIsLandscapeGameModeState && this.mData.isLandscapeGameModeEnable;
    }
}
