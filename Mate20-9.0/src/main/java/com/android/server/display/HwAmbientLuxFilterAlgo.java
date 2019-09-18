package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.gesture.GestureNavConst;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class HwAmbientLuxFilterAlgo {
    private static final int AMBIENT_LIGHT_HORIZON = 20000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int AMBIENT_MIN_LUX = 0;
    private static final int AMBIENT_SCENE_HORIZON = 80000;
    private static final int EXTRA_DELAY_TIME = 100;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int LOG_INTERVAL_MS = 2000;
    private static final int MIN_BRIGHTNESS = 4;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final String TAG = "HwAmbientLuxFilterAlgo";
    private static final long mNormDarkenDebounceTimeForBackSensorCoverMode = 500;
    private static final float mRatioForDarkenBackSensorCoverMode = 0.5f;
    private HwRingBuffer mAmbientLightRingBuffer;
    private HwRingBuffer mAmbientLightRingBufferFilter;
    private HwRingBuffer mAmbientLightRingBufferScene;
    protected float mAmbientLux;
    private float mAmbientLuxNewMax;
    private float mAmbientLuxNewMin;
    public boolean mAutoBrightnessIntervened = false;
    private float mAutoModeEnableFirstLux;
    private int mBackSensorCoverModeBrightness;
    private int mBrightPointCnt = -1;
    private float mBrightenDeltaLuxForCurrentBrightness;
    private float mBrightenDeltaLuxMax;
    private float mBrightenDeltaLuxMaxForDcMode = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mBrightenDeltaLuxMaxForGameMode = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mBrightenDeltaLuxMaxForLandScapeMode = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mBrightenDeltaLuxMin;
    private boolean mCoverModeDayEnable = false;
    private boolean mCoverState = false;
    private int mCurrentAutoBrightness;
    private boolean mDarkTimeDelayFromBrightnessEnable = false;
    private float mDarkenDeltaLuxForBackSensorCoverMode;
    private float mDarkenDeltaLuxForCurrentBrightness;
    private float mDarkenDeltaLuxMax;
    private float mDarkenDeltaLuxMaxForDcMode = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mDarkenDeltaLuxMaxForGameMode = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mDarkenDeltaLuxMaxForLandScapeMode = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mDarkenDeltaLuxMin;
    private final HwBrightnessXmlLoader.Data mData;
    private boolean mDayModeEnable = false;
    private boolean mDcModeBrightnessEnable = false;
    private boolean mFirstAmbientLux = true;
    private boolean mFirstSetBrightness = true;
    private boolean mGameModeEnable = false;
    private boolean mIsCoverModeFastResponseFlag = false;
    private boolean mIsclosed = false;
    private boolean mKeyguardIsLocked;
    private boolean mLandScapeModeEnable = false;
    private float mLastCloseScreenLux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private HwRingBuffer mLastCloseScreenRingBuffer;
    private int mLastCloseTime = -1;
    private float mLastObservedLux;
    private final int mLightSensorRate;
    private final Object mLock = new Object();
    private float mLuxBufferAvg = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mLuxBufferAvgMax = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mLuxBufferAvgMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private boolean mModeToAutoFastDarkenResponseEanble = false;
    private boolean mModeToAutoFastDarkenResponseMinLuxEanble = false;
    private float mModeToAutoFastResponseDarkenDeltaLux;
    private long mModeToAutoFastResponseDarkenStartTime = 0;
    private boolean mModeToAutoFastResponseDarkenStartTimeEnable = false;
    private boolean mNeedToSendProximityDebounceMsg = false;
    private boolean mNeedToUpdateBrightness;
    public long mNextTransitionTime = -1;
    protected long mNormBrighenDebounceTime;
    protected long mNormBrighenDebounceTimeForSmallThr;
    protected long mNormDarkenDebounceTime;
    protected long mNormDarkenDebounceTimeForSmallThr;
    private boolean mOffsetResetEnable = false;
    private float mOffsetValidAmbientBrightenDeltaLux;
    private float mOffsetValidAmbientDarkenDeltaLux;
    private float mOffsetValidAmbientLux;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPowerStatus = false;
    private long mPrintLogTime = 0;
    private int mProximity = -1;
    private int mProximityNegativeDebounceTime = 3000;
    private int mProximityPositiveDebounceTime = 150;
    private boolean mProximityPositiveStatus;
    private int mResponseDurationPoints;
    private float mSceneAmbientLuxMax;
    private float mSceneAmbientLuxMin;
    private float mSceneAmbientLuxWeight;
    private float mSecondDarkenModeDarkenDeltaLux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private boolean mSecondDarkenModeResponseEnable = false;
    private float mStability = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mStabilityBrightenConstant = 101.0f;
    private float mStabilityBrightenConstantForSmallThr;
    private float mStabilityDarkenConstant = 101.0f;
    private float mStabilityDarkenConstantForSmallThr;
    private float mStabilityForSmallThr = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private boolean mTouchProximityState = false;
    private float mlastFilterLux;

    public interface Callbacks {
        void updateBrightness();
    }

    static {
        boolean z = true;
        if (!Log.HWLog && (!Log.HWModuleLog || !Log.isLoggable(TAG, 3))) {
            z = false;
        }
        HWDEBUG = z;
    }

    public HwAmbientLuxFilterAlgo(int lightSensorRate) {
        this.mLightSensorRate = lightSensorRate;
        this.mNeedToUpdateBrightness = false;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        this.mAmbientLightRingBufferScene = new HwRingBuffer(GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_4);
        this.mLastCloseScreenRingBuffer = new HwRingBuffer(50);
        this.mData = HwBrightnessXmlLoader.getData();
    }

    public void isFirstAmbientLux(boolean isFirst) {
        this.mFirstAmbientLux = isFirst;
    }

    public void handleLightSensorEvent(long time, float lux) {
        synchronized (this.mLock) {
            if (!this.mFirstAmbientLux && lux > this.mData.darkLightLuxMinThreshold && lux < this.mData.darkLightLuxMaxThreshold && this.mData.darkLightLuxMinThreshold < this.mData.darkLightLuxMaxThreshold) {
                lux += this.mData.darkLightLuxDelta;
                if (lux < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    lux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                }
            }
            if (lux > 40000.0f) {
                lux = 40000.0f;
            }
            try {
                applyLightSensorMeasurement(time, lux);
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
    }

    public float getCurrentAmbientLux() {
        return this.mAmbientLux;
    }

    private void setAmbientLux(float lux) {
        this.mAmbientLux = (float) Math.round(lux);
        if (this.mAmbientLux < 10.0f) {
            this.mStabilityBrightenConstantForSmallThr = 0.5f;
            this.mStabilityDarkenConstantForSmallThr = 0.5f;
        } else if (this.mAmbientLux < 10.0f || this.mAmbientLux >= 50.0f) {
            this.mStabilityBrightenConstantForSmallThr = 5.0f;
            this.mStabilityDarkenConstantForSmallThr = 5.0f;
        } else {
            this.mStabilityBrightenConstantForSmallThr = 3.0f;
            this.mStabilityDarkenConstantForSmallThr = 3.0f;
        }
        this.mAmbientLuxNewMax = this.mAmbientLux;
        this.mAmbientLuxNewMin = this.mAmbientLux;
        this.mSceneAmbientLuxMax = this.mAmbientLux;
        this.mSceneAmbientLuxMin = this.mAmbientLux;
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        this.mResponseDurationPoints = 0;
    }

    public void updateAmbientLux() {
        synchronized (this.mLock) {
            long time = SystemClock.uptimeMillis();
            try {
                this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
                this.mAmbientLightRingBuffer.prune(time - HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
                if (HWFLOW) {
                    Slog.d(TAG, "updateAmbientLux:time=" + time + ",mLastObservedLux=" + this.mLastObservedLux);
                }
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private float modifyFirstAmbientLux(float ambientLux) {
        int N = this.mLastCloseScreenRingBuffer.size();
        if (N > 0 && this.mData.initNumLastBuffer > 0) {
            float sumLux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            float interfere = this.mData.initDoubleSensorInterfere;
            int cntValidData = 0;
            for (int i = N - 1; i >= 0; i--) {
                float tmpLux = this.mLastCloseScreenRingBuffer.getLux(i);
                if (Math.abs(this.mAmbientLightRingBuffer.getTime(0) - this.mLastCloseScreenRingBuffer.getTime(i)) < this.mData.initValidCloseTime && Math.abs(ambientLux - tmpLux) < 1.5f * interfere) {
                    sumLux += tmpLux;
                    cntValidData++;
                }
            }
            Slog.i(TAG, "LastScreenBuffer: sumLux=" + sumLux + ", cntValidData=" + cntValidData + ", InambientLux=" + ambientLux);
            if (((float) cntValidData) > 1.0E-7f && ambientLux < ((float) this.mData.initUpperLuxThreshold) + 1.0E-6f && sumLux / ((float) cntValidData) < 1.0E-6f + ambientLux) {
                float ave = sumLux / ((float) cntValidData);
                float lambda = 1.0f / (((float) Math.exp((double) ((-this.mData.initSigmoidFuncSlope) * (interfere - Math.abs(ambientLux - ave))))) + 1.0f);
                Slog.i(TAG, "modifyFirstAmbientLux : lambda=" + lambda + ", ave" + ave + ", ambientLux=" + ambientLux);
                return (lambda * ave) + ((1.0f - lambda) * ambientLux);
            }
        }
        return ambientLux;
    }

    private float calcAmbientLuxInCoverState(float ambientLux) {
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
        long j;
        String str;
        String str2;
        long j2 = time;
        float filterLux = prefilterAmbientLux(j2, this.mData.preMethodNum);
        float lastestLux = getOrigLastAmbientLux(time);
        updateBuffer(j2, filterLux, AMBIENT_LIGHT_HORIZON);
        updateBufferForScene(j2, filterLux, AMBIENT_SCENE_HORIZON);
        this.mlastFilterLux = getFilterLastAmbientLux(time);
        float ambientLux = postfilterAmbientLux(j2, this.mData.postMethodNum);
        if (this.mFirstAmbientLux) {
            if (this.mCoverState) {
                this.mCoverState = false;
                ambientLux = calcAmbientLuxInCoverState(ambientLux);
            }
            ambientLux = modifyFirstAmbientLux(ambientLux);
            this.mModeToAutoFastResponseDarkenStartTimeEnable = true;
            if (this.mModeToAutoFastDarkenResponseEanble && this.mData.resetAmbientLuxEnable) {
                if (((float) this.mCurrentAutoBrightness) > this.mData.resetAmbientLuxStartBrightness && ambientLux < this.mData.resetAmbientLuxTh) {
                    this.mModeToAutoFastResponseDarkenStartTime = j2;
                    if (HWFLOW) {
                        Slog.i(TAG, "ResetAmbientLuxEn,lux=" + ambientLux + ",-->resetAmbientLuxTh=" + this.mData.resetAmbientLuxTh);
                    }
                    ambientLux = this.mData.resetAmbientLuxTh;
                    this.mAmbientLightRingBuffer.putLux(0, ambientLux);
                    this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
                } else if (((float) this.mCurrentAutoBrightness) <= this.mData.resetAmbientLuxStartBrightnessMax || ambientLux >= this.mData.resetAmbientLuxThMax) {
                    this.mModeToAutoFastDarkenResponseEanble = false;
                    this.mModeToAutoFastDarkenResponseMinLuxEanble = false;
                    if (HWFLOW) {
                        Slog.i(TAG, "ResetAmbientLuxDisable,no need ModeToAutoFastDarkenResponseEanble,ambientLux=" + ambientLux + ",brightness=" + this.mCurrentAutoBrightness);
                    }
                } else {
                    this.mModeToAutoFastResponseDarkenStartTime = j2;
                    if (HWFLOW) {
                        Slog.i(TAG, "ResetAmbientLuxEn,lux=" + ambientLux + ",brightness=" + this.mCurrentAutoBrightness + ",FastResponseOnly,resetAmbientLuxThMax=" + this.mData.resetAmbientLuxThMax + ",brightnessMax=" + this.mData.resetAmbientLuxStartBrightnessMax);
                    }
                }
            }
            setAmbientLux(ambientLux);
            if (this.mData.offsetValidAmbientLuxEnable) {
                setOffsetValidAmbientLux(ambientLux);
            }
            this.mFirstAmbientLux = false;
            this.mCoverModeDayEnable = false;
            if (HWFLOW) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux + ",mLastCloseScreenLux=" + this.mLastCloseScreenLux + getLuxThresholdStrings() + ",mAMin=" + this.mAmbientLuxNewMin + ",mAMax=" + this.mAmbientLuxNewMax + ",mAmbientLightRingBufferFilter=" + this.mAmbientLightRingBufferFilter);
            }
            this.mNeedToUpdateBrightness = true;
        }
        float ambientLux2 = ambientLux;
        updateNewAmbientLuxFromScene(j2, this.mAmbientLightRingBufferScene);
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        boolean needToBrighten = decideToBrighten(ambientLux2);
        boolean needToBrightenNew = decideToBrighten(lastestLux);
        boolean needToDarken = decideToDarken(ambientLux2);
        boolean needToDarkenNew = decideToDarken(lastestLux);
        if (this.mData.offsetValidAmbientLuxEnable) {
            long nextBrightenTransitionOffset = nextAmbientLightBrighteningTransitionForOffset(time);
            long nextDarkenTransitionOffset = nextAmbientLightDarkeningTransitionExtended(j2, this.mOffsetValidAmbientLux, this.mOffsetValidAmbientDarkenDeltaLux, (long) this.mData.offsetDarkenDebounceTime);
            boolean needToBrightenOffset = decideToBrightenForOffset(ambientLux2);
            boolean needToDarkenOffset = decideToDarkenForOffset(ambientLux2);
            boolean needToBrightenOffset2 = needToBrightenOffset && nextBrightenTransitionOffset <= j2;
            boolean needToDarkenOffset2 = needToDarkenOffset && nextDarkenTransitionOffset <= j2;
            if (this.mData.offsetValidAmbientLuxEnable && (needToBrightenOffset2 || needToDarkenOffset2)) {
                setOffsetValidAmbientLux(ambientLux2);
            }
        }
        if (((float) this.mBrightPointCnt) > -1.0E-6f) {
            this.mBrightPointCnt++;
        }
        if (this.mBrightPointCnt > this.mData.outdoorResponseCount) {
            this.mBrightPointCnt = -1;
        }
        long nextBrightenTransitionForSmallThr = nextAmbientLightBrighteningTransitionForSmallThr(time);
        long nextDarkenTransitionForSmallThr = nextAmbientLightDarkeningTransitionExtended(j2, this.mAmbientLux, this.mDarkenDeltaLuxMin, this.mNormDarkenDebounceTimeForSmallThr);
        boolean needToBrightenForSmallThr = decideToBrightenForSmallThr(ambientLux2);
        boolean needToDarkenForSmallThr = decideToDarkenForSmallThr(ambientLux2);
        boolean needToBrightenForSmallThr2 = needToBrightenForSmallThr && nextBrightenTransitionForSmallThr <= j2;
        boolean needToDarkenForSmallThr2 = needToDarkenForSmallThr && nextDarkenTransitionForSmallThr <= j2;
        boolean needToBrighten2 = (needToBrighten && needToBrightenNew && nextBrightenTransition <= j2) || needToBrightenForSmallThr2;
        boolean needToDarken2 = (needToDarken && needToDarkenNew && nextDarkenTransition <= j2) || needToDarkenForSmallThr2 || needToDarkenForBackSensorCoverMode(j2, ambientLux2) || needToDarkenForModeToAutoFastDarkenResponse(j2, ambientLux2) || needToDarkenForSecondDarkenMode(j2, ambientLux2);
        float brightenLux = this.mAmbientLux + this.mBrightenDeltaLuxMax;
        float f = this.mAmbientLux - this.mDarkenDeltaLuxMax;
        long j3 = nextDarkenTransitionForSmallThr;
        if (!HWFLOW || j2 - this.mPrintLogTime <= 2000) {
            float f2 = filterLux;
            float f3 = lastestLux;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("t=");
            sb.append(j2);
            sb.append(",lx=");
            float f4 = brightenLux;
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
                str2 = ",SecondDEnable=" + this.mSecondDarkenModeResponseEnable;
            } else {
                str2 = "";
            }
            sb.append(str2);
            sb.append(",mDt=");
            float f5 = filterLux;
            float f6 = lastestLux;
            sb.append(this.mNormDarkenDebounceTime);
            sb.append(",mBt=");
            sb.append(this.mNormBrighenDebounceTime);
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
            Slog.d(TAG, sb.toString());
            this.mPrintLogTime = j2;
        }
        updateModeToAutoFastDarkenResponseState(time);
        if (needToBrighten2 || needToDarken2) {
            if (this.mModeToAutoFastDarkenResponseEanble && this.mData.resetAmbientLuxEnable && needToBrighten2) {
                this.mModeToAutoFastDarkenResponseEanble = false;
                this.mModeToAutoFastResponseDarkenStartTimeEnable = false;
                if (HWFLOW) {
                    Slog.i(TAG, "ResetAmbientLuxDisable,no need ModeToAutoFastDarkenResponseEanble,needToBrighten=" + needToBrighten2);
                }
            }
            float ambientLux3 = updateAmbientLuxFromResetAmbientLuxThMin(ambientLux2, needToDarken2);
            if (needToDarken2 && ambientLux3 == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && this.mDayModeEnable && this.mData.dayModeDarkenMinLux > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                ambientLux3 = this.mData.dayModeDarkenMinLux;
                updateCurrentLuxForBuffer(ambientLux3);
                if (HWFLOW) {
                    Slog.i(TAG, "updateAmbientLux darken set 0lux -->ambientLux=" + this.mData.dayModeDarkenMinLux);
                }
            }
            float ambientLux4 = ambientLux3;
            this.mBrightPointCnt = 0;
            setAmbientLux(ambientLux4);
            if (this.mData.offsetValidAmbientLuxEnable && (needToBrightenForSmallThr2 || needToDarkenForSmallThr2 || (needToBrighten2 && this.mLandScapeModeEnable))) {
                if (HWFLOW) {
                    Slog.i(TAG, "updateAmbientLux,LastOffLux=" + this.mOffsetValidAmbientLux + ",newOffLux=" + ambientLux4 + ",needToBrighten=" + needToBrighten2 + ",BrightenS=" + needToBrightenForSmallThr2 + ",DarkenS=" + needToDarkenForSmallThr2);
                }
                setOffsetValidAmbientLux(ambientLux4);
            }
            if (HWFLOW) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("updateAmbientLux: ");
                if (needToBrighten2) {
                    str = "Brightened,needBs=" + needToBrightenForSmallThr2;
                } else {
                    str = "Darkened,needDs=" + needToDarkenForSmallThr2;
                }
                sb2.append(str);
                sb2.append(", mAmbientLightRingBuffer=");
                sb2.append(this.mAmbientLightRingBuffer.toString(6));
                sb2.append(",mAmbientLux=");
                sb2.append(this.mAmbientLux);
                sb2.append(getLuxThresholdStrings());
                sb2.append(",mAMin=");
                sb2.append(this.mAmbientLuxNewMin);
                sb2.append(",mAMax=");
                sb2.append(this.mAmbientLuxNewMax);
                sb2.append(",mPxs=");
                sb2.append(this.mProximityPositiveStatus);
                sb2.append(",mTPxs=");
                sb2.append(this.mTouchProximityState);
                sb2.append(", mAmbientLightRingBufferF=");
                sb2.append(this.mAmbientLightRingBufferFilter.toString(6));
                Slog.d(TAG, sb2.toString());
            }
            if (HWFLOW && this.mIsCoverModeFastResponseFlag) {
                Slog.i(TAG, "CoverModeBResponseTime=" + this.mData.coverModeBrightenResponseTime + ",CoverModeDResponseTime=" + this.mData.coverModeDarkenResponseTime);
            }
            if (HWFLOW && this.mPowerStatus) {
                Slog.i(TAG, "PowerOnBT=" + this.mData.powerOnBrightenDebounceTime + ",PowerOnDT=" + this.mData.powerOnDarkenDebounceTime);
            }
            this.mNeedToUpdateBrightness = true;
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        this.mNextTransitionTime = nextDarkenTransition < nextBrightenTransition ? nextDarkenTransition : nextBrightenTransition;
        if (this.mNextTransitionTime > j2) {
            boolean z = needToBrightenForSmallThr2;
            boolean z2 = needToDarkenForSmallThr2;
            j = this.mNextTransitionTime + ((long) this.mLightSensorRate);
        } else {
            boolean z3 = needToDarkenForSmallThr2;
            j = ((long) this.mLightSensorRate) + j2;
        }
        this.mNextTransitionTime = j + 100;
        if (HWFLOW && j2 - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + this.mNextTransitionTime + TimeUtils.formatUptime(this.mNextTransitionTime));
        }
    }

    private String getLuxThresholdStrings() {
        StringBuilder luxThresholdBuilder = new StringBuilder();
        if (getThresholdForGameModeEnable()) {
            float brightenLux = this.mAmbientLux + this.mBrightenDeltaLuxMaxForGameMode;
            luxThresholdBuilder.append(",mBLuxGM=");
            luxThresholdBuilder.append(brightenLux);
            luxThresholdBuilder.append(",mDluxGM=");
            luxThresholdBuilder.append(this.mAmbientLux - this.mDarkenDeltaLuxMaxForGameMode);
        } else if (this.mLandScapeModeEnable) {
            float brightenLux2 = this.mAmbientLux + this.mBrightenDeltaLuxMaxForLandScapeMode;
            luxThresholdBuilder.append(",mBLuxLS=");
            luxThresholdBuilder.append(brightenLux2);
            luxThresholdBuilder.append(",mDluxLS=");
            luxThresholdBuilder.append(this.mAmbientLux - this.mDarkenDeltaLuxMaxForLandScapeMode);
        } else if (getThresholdForDcModeEnable()) {
            float brightenLux3 = this.mAmbientLux + this.mBrightenDeltaLuxMaxForDcMode;
            luxThresholdBuilder.append(",mBLuxDC=");
            luxThresholdBuilder.append(brightenLux3);
            luxThresholdBuilder.append(",mDluxDc=");
            luxThresholdBuilder.append(this.mAmbientLux - this.mDarkenDeltaLuxMaxForDcMode);
        } else {
            float brightenLux4 = this.mAmbientLux + this.mBrightenDeltaLuxMax;
            luxThresholdBuilder.append(",mBLux=");
            luxThresholdBuilder.append(brightenLux4);
            luxThresholdBuilder.append(",mDlux=");
            luxThresholdBuilder.append(this.mAmbientLux - this.mDarkenDeltaLuxMax);
        }
        return luxThresholdBuilder.toString();
    }

    private void updateNewAmbientLuxFromScene(long time, HwRingBuffer hwBuffer) {
        int N = hwBuffer.size();
        this.mAmbientLuxNewMax = this.mAmbientLux;
        this.mAmbientLuxNewMin = this.mAmbientLux;
        this.mSceneAmbientLuxMax = this.mAmbientLux;
        this.mSceneAmbientLuxMin = this.mAmbientLux;
        if (this.mResponseDurationPoints == Integer.MAX_VALUE) {
            this.mResponseDurationPoints = Integer.MAX_VALUE;
        } else {
            this.mResponseDurationPoints++;
        }
        if (!getThresholdForGameModeEnable() && !this.mLandScapeModeEnable && !getThresholdForDcModeEnable() && N != 0 && N >= this.mData.sceneGapPoints && this.mResponseDurationPoints - this.mData.sceneMinPoints >= this.mData.sceneGapPoints && this.mData.sceneMaxPoints >= this.mData.sceneMinPoints && this.mData.sceneMaxPoints + this.mData.sceneGapPoints <= 228) {
            updateSceneBufferAmbientLuxMaxMinAvg(hwBuffer, this.mResponseDurationPoints < this.mData.sceneMaxPoints + this.mData.sceneGapPoints ? N - this.mResponseDurationPoints : (N - this.mData.sceneMaxPoints) - this.mData.sceneGapPoints, N - this.mData.sceneGapPoints);
            this.mSceneAmbientLuxWeight = ((float) this.mData.sceneGapPoints) / ((float) this.mResponseDurationPoints);
            if (this.mAmbientLux > this.mSceneAmbientLuxMax) {
                this.mAmbientLuxNewMax = (this.mSceneAmbientLuxWeight * this.mAmbientLux) + ((1.0f - this.mSceneAmbientLuxWeight) * this.mSceneAmbientLuxMax);
            }
            if (this.mAmbientLux > this.mSceneAmbientLuxMin) {
                this.mAmbientLuxNewMin = (this.mSceneAmbientLuxWeight * this.mAmbientLux) + ((1.0f - this.mSceneAmbientLuxWeight) * this.mSceneAmbientLuxMin);
            }
            correctAmbientLux(time);
        }
    }

    private void updateSceneBufferAmbientLuxMaxMinAvg(HwRingBuffer buffer, int start, int end) {
        if (buffer.size() == 0 || end < start || start > N - 1 || end < 0 || start < 0 || end > N - 1) {
            Slog.i(TAG, "SceneBufferAmbientLux input error,end=" + end + ",start=" + start + ",N=" + N);
            return;
        }
        float luxMin = buffer.getLux(start);
        float luxMax = buffer.getLux(start);
        float luxMin2 = luxMin;
        float luxSum = 0.0f;
        for (int i = start; i <= end; i++) {
            float lux = buffer.getLux(i);
            if (lux > luxMax) {
                luxMax = lux;
            }
            if (lux < luxMin2) {
                luxMin2 = lux;
            }
            luxSum += lux;
        }
        float luxMean = (float) Math.round(luxSum / ((float) ((end - start) + 1)));
        this.mSceneAmbientLuxMax = (this.mData.sceneAmbientLuxMaxWeight * luxMean) + ((1.0f - this.mData.sceneAmbientLuxMaxWeight) * luxMax);
        this.mSceneAmbientLuxMin = (this.mData.sceneAmbientLuxMinWeight * luxMean) + ((1.0f - this.mData.sceneAmbientLuxMinWeight) * luxMin2);
    }

    private void correctAmbientLux(long time) {
        float ambientLuxDarkenDelta = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePoints, this.mAmbientLux);
        float ambientLuxNewMaxBrightenDelta = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePoints, this.mAmbientLuxNewMax);
        float ambientLuxNewMinBrightenDelta = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePoints, this.mAmbientLuxNewMin);
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMax - 1.0E-5f) {
            if (HWFLOW && time - this.mPrintLogTime > 2000) {
                Slog.i(TAG, " Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
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

    public boolean brightnessUpdated() {
        this.mNeedToUpdateBrightness = false;
        return false;
    }

    public boolean needToSendUpdateAmbientLuxMsg() {
        return this.mNextTransitionTime > 0;
    }

    public long getSendUpdateAmbientLuxMsgTime() {
        return this.mNextTransitionTime;
    }

    private long getNextAmbientLightBrighteningTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mData.coverModeBrightenResponseTime + earliedtime;
        }
        if (getKeyguardLockedBrightenEnable()) {
            return ((long) this.mData.keyguardResponseBrightenTime) + earliedtime;
        }
        if (getOutdoorModeBrightenEnable()) {
            return ((long) this.mData.outdoorResponseBrightenTime) + earliedtime;
        }
        if (getProximityPositiveBrightenEnable()) {
            return ((long) this.mData.proximityResponseBrightenTime) + earliedtime;
        }
        if (this.mPowerStatus) {
            if (getSlowResponsePowerStatus()) {
                return ((long) this.mData.powerOnBrightenDebounceTime) + earliedtime + ((long) this.mData.initSlowReponseBrightTime);
            }
            return ((long) this.mData.powerOnBrightenDebounceTime) + earliedtime;
        } else if (this.mGameModeEnable) {
            return this.mData.gameModeBrightenDebounceTime + earliedtime;
        } else {
            if (this.mLandScapeModeEnable) {
                return ((long) this.mData.landScapeModeBrightenDebounceTime) + earliedtime;
            }
            if (this.mModeToAutoFastDarkenResponseEanble) {
                return ((long) this.mData.resetAmbientLuxBrightenDebounceTime) + earliedtime;
            }
            return this.mNormBrighenDebounceTime + earliedtime;
        }
    }

    private long getNextAmbientLightDarkeningTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mData.coverModeDarkenResponseTime + earliedtime;
        }
        if (getKeyguardLockedDarkenEnable()) {
            return ((long) this.mData.keyguardResponseDarkenTime) + earliedtime;
        }
        if (getOutdoorModeDarkenEnable()) {
            return ((long) this.mData.outdoorResponseDarkenTime) + earliedtime;
        }
        if (this.mPowerStatus) {
            return ((long) this.mData.powerOnDarkenDebounceTime) + earliedtime;
        }
        if (this.mGameModeEnable) {
            return this.mData.gameModeDarkenDebounceTime + earliedtime;
        }
        if (this.mLandScapeModeEnable) {
            return ((long) this.mData.landScapeModeDarkenDebounceTime) + earliedtime;
        }
        return this.mNormDarkenDebounceTime + earliedtime;
    }

    private boolean getKeyguardLockedBrightenEnable() {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "getKeyguardLocked no lux");
            return false;
        } else if (!this.mKeyguardIsLocked || this.mAmbientLightRingBuffer.getLux(N - 1) < this.mData.keyguardLuxThreshold || this.mData.keyguardResponseBrightenTime < 0 || getProximityPositiveBrightenEnable()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean getOutdoorModeBrightenEnable() {
        if (((float) this.mBrightPointCnt) <= -1.0E-6f || this.mAmbientLux <= ((float) this.mData.outdoorLowerLuxThreshold) || this.mData.outdoorResponseBrightenTime <= 0) {
            return false;
        }
        return true;
    }

    private boolean getProximityPositiveBrightenEnable() {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "getProximityPositive no lux");
            return false;
        } else if (!this.mData.allowLabcUseProximity || !this.mProximityPositiveStatus || this.mAmbientLightRingBuffer.getLux(N - 1) >= this.mData.proximityLuxThreshold || this.mData.proximityResponseBrightenTime <= 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean getSlowResponsePowerStatus() {
        int N = this.mAmbientLightRingBuffer.size();
        if (N > 0) {
            for (int i = 0; i < N; i++) {
                if (this.mAmbientLightRingBuffer.getLux(i) > ((float) this.mData.initSlowReponseUpperLuxThreshold) + 1.0E-6f) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean getKeyguardLockedDarkenEnable() {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "getKeyguardLocked no lux");
            return false;
        } else if (!this.mKeyguardIsLocked || this.mAmbientLightRingBuffer.getLux(N - 1) < this.mData.keyguardLuxThreshold || this.mData.keyguardResponseDarkenTime < 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean getOutdoorModeDarkenEnable() {
        if (((float) this.mBrightPointCnt) <= -1.0E-6f || this.mAmbientLux <= ((float) this.mData.outdoorLowerLuxThreshold) || this.mData.outdoorResponseDarkenTime <= 0) {
            return false;
        }
        return true;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.mPowerStatus = powerStatus;
    }

    public void clear() {
        synchronized (this.mLock) {
            if (HWFLOW) {
                Slog.d(TAG, "clear buffer data and algo flags");
            }
            this.mLastCloseScreenLux = this.mAmbientLux;
            if (HWFLOW) {
                Slog.d(TAG, "LabcCoverMode clear: mLastCloseScreenLux=" + this.mLastCloseScreenLux);
            }
            if (this.mData.initNumLastBuffer > 0) {
                int N = this.mAmbientLightRingBuffer.size();
                for (int i = 0; i < N; i++) {
                    this.mLastCloseScreenRingBuffer.push(this.mAmbientLightRingBuffer.getTime(i), this.mAmbientLightRingBuffer.getLux(i));
                }
                int pruneNTmp = this.mLastCloseScreenRingBuffer.size() - this.mData.initNumLastBuffer;
                if ((pruneNTmp > 0 ? pruneNTmp : 0) > 0) {
                    this.mLastCloseScreenRingBuffer.prune(1 + this.mLastCloseScreenRingBuffer.getTime(pruneN - 1));
                }
                if (HWFLOW) {
                    Slog.d(TAG, "mLastCloseScreenRingBuffer=" + this.mLastCloseScreenRingBuffer.toString(this.mLastCloseScreenRingBuffer.size()));
                }
            } else {
                Slog.i(TAG, "mLastCloseScreenRingBuffer is set empty!");
            }
            this.mIsCoverModeFastResponseFlag = false;
            this.mAutoBrightnessIntervened = false;
            this.mProximityPositiveStatus = false;
            this.mProximity = -1;
            this.mPendingProximity = -1;
            this.mAmbientLightRingBuffer.clear();
            this.mAmbientLightRingBufferFilter.clear();
            this.mAmbientLightRingBufferScene.clear();
            this.mBrightPointCnt = -1;
            if (this.mData.dayModeAlgoEnable || this.mData.offsetResetEnable) {
                this.mFirstSetBrightness = false;
                Calendar c = Calendar.getInstance();
                int lastCloseDay = c.get(6);
                int lastCloseHour = c.get(11);
                this.mLastCloseTime = (lastCloseDay * 24 * 60) + (lastCloseHour * 60) + c.get(12);
                if (HWFLOW) {
                    Slog.d(TAG, "DayMode: lastCloseDay=" + lastCloseDay + ",lastCloseHour=" + lastCloseHour + ",lastCloseMinute=" + lastCloseMinute + ",mLastCloseTime=" + this.mLastCloseTime);
                }
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

    private void updatepara(HwRingBuffer buffer, float lux) {
        float stability = calculateStability(buffer);
        float stabilityForSmallThr = calculateStabilityForSmallThr(buffer);
        if (stability > 100.0f) {
            this.mStability = 100.0f;
        } else if (stability < ((float) this.mData.stabilityConstant)) {
            this.mStability = (float) this.mData.stabilityConstant;
        } else {
            this.mStability = stability;
        }
        if (stabilityForSmallThr > 100.0f) {
            this.mStabilityForSmallThr = 100.0f;
        } else {
            this.mStabilityForSmallThr = stabilityForSmallThr;
        }
        float mLux = (float) Math.round(lux);
        if (mLux < this.mData.brightTimeDelayLuxThreshold && this.mlastFilterLux < this.mData.brightTimeDelayLuxThreshold && this.mData.brightTimeDelayEnable) {
            this.mNormBrighenDebounceTime = (long) this.mData.brightTimeDelay;
        } else if (this.mDcModeBrightnessEnable) {
            this.mNormBrighenDebounceTime = this.mData.dcModeBrightenDebounceTime;
        } else {
            this.mNormBrighenDebounceTime = (long) (((float) this.mData.brighenDebounceTime) * (((this.mData.brightenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + 1.0f));
        }
        if ((mLux < this.mData.darkTimeDelayLuxThreshold || this.mDarkTimeDelayFromBrightnessEnable) && this.mData.darkTimeDelayEnable) {
            float ambientLuxDarkenDelta = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePoints, this.mAmbientLux);
            float currentAmbientLux = buffer.getLux(buffer.size() - 1);
            float luxNormalizedFactor = (this.mData.darkTimeDelayBeta2 * (this.mAmbientLux - currentAmbientLux)) + (this.mData.darkTimeDelayBeta1 * ((this.mAmbientLux - currentAmbientLux) - ambientLuxDarkenDelta)) + 1.0f;
            if (luxNormalizedFactor < 0.001f) {
                this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkTimeDelay) + this.mData.darkTimeDelayBeta0);
            } else {
                this.mNormDarkenDebounceTime = ((long) this.mData.darkTimeDelay) + ((long) ((this.mData.darkTimeDelayBeta0 * (1.0f + (this.mData.darkTimeDelayBeta2 * 1.0f))) / luxNormalizedFactor));
            }
        } else if (this.mDcModeBrightnessEnable) {
            this.mNormDarkenDebounceTime = this.mData.dcModeDarkenDebounceTime;
        } else {
            this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkenDebounceTime) * (1.0f + ((this.mData.darkenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f)));
        }
        this.mNormBrighenDebounceTimeForSmallThr = (long) this.mData.brighenDebounceTimeForSmallThr;
        this.mNormDarkenDebounceTimeForSmallThr = (long) this.mData.darkenDebounceTimeForSmallThr;
        setDarkenThresholdNew(this.mAmbientLuxNewMin);
        setBrightenThresholdNew(this.mAmbientLuxNewMax);
    }

    private void setBrightenThresholdNew(float amLux) {
        this.mBrightenDeltaLuxMax = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePoints, amLux);
        this.mBrightenDeltaLuxMin = this.mBrightenDeltaLuxMax * this.mData.ratioForBrightnenSmallThr;
        this.mBrightenDeltaLuxMax *= 1.0f + ((this.mData.brightenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f);
        if (((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseBrightenRatio > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            this.mBrightenDeltaLuxMax *= this.mData.outdoorResponseBrightenRatio;
        }
        this.mBrightenDeltaLuxMaxForLandScapeMode = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePointsForLandScapeMode, this.mAmbientLux);
        if (this.mData.gameModeLuxThresholdEnable) {
            this.mBrightenDeltaLuxMaxForGameMode = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePointsForGameMode, this.mAmbientLux);
        }
        if (this.mData.dcModeLuxThresholdEnable) {
            this.mBrightenDeltaLuxMaxForDcMode = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePointsForDcMode, this.mAmbientLux);
        }
    }

    private void setDarkenThresholdNew(float amLux) {
        this.mDarkenDeltaLuxMax = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePoints, amLux);
        if (this.mAmbientLux < 10.0f) {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax;
        } else {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax * this.mData.ratioForDarkenSmallThr;
        }
        this.mDarkenDeltaLuxForBackSensorCoverMode = this.mDarkenDeltaLuxMax * 0.5f;
        this.mModeToAutoFastResponseDarkenDeltaLux = this.mDarkenDeltaLuxMax * this.mData.resetAmbientLuxDarkenRatio;
        this.mDarkenDeltaLuxMax *= 1.0f + ((this.mData.darkenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f);
        if (((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseDarkenRatio > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            this.mDarkenDeltaLuxMax *= this.mData.outdoorResponseDarkenRatio;
        }
        this.mDarkenDeltaLuxMaxForLandScapeMode = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePointsForLandScapeMode, this.mAmbientLux);
        if (this.mData.gameModeLuxThresholdEnable) {
            this.mDarkenDeltaLuxMaxForGameMode = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePointsForGameMode, this.mAmbientLux);
        }
        if (this.mData.dcModeLuxThresholdEnable) {
            this.mDarkenDeltaLuxMaxForDcMode = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePointsForDcMode, this.mAmbientLux);
        }
        if (this.mData.secondDarkenModeEanble) {
            this.mSecondDarkenModeDarkenDeltaLux = this.mDarkenDeltaLuxMax * this.mData.secondDarkenModeDarkenDeltaLuxRatio;
        }
    }

    private float calculateBrightenThresholdDeltaNew(List<PointF> linePoints, float amLux) {
        float brightenThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (linePoints == null) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        PointF temp1 = null;
        Iterator iter = linePoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PointF temp = iter.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp2.x <= temp1.x) {
                    brightenThreshold = 1.0f;
                    if (HWFLOW) {
                        Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    brightenThreshold = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
            } else {
                temp1 = temp;
                brightenThreshold = temp1.y;
            }
        }
        return brightenThreshold;
    }

    private float calculateDarkenThresholdDeltaNew(List<PointF> linePoints, float amLux) {
        float darkenThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (linePoints == null) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        PointF temp1 = null;
        Iterator iter = linePoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PointF temp = iter.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp2.x <= temp1.x) {
                    darkenThreshold = 1.0f;
                    if (HWFLOW) {
                        Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    float darkenThresholdTmp = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                    float f = 1.0f;
                    if (darkenThresholdTmp > 1.0f) {
                        f = darkenThresholdTmp;
                    }
                    darkenThreshold = f;
                }
            } else {
                temp1 = temp;
                darkenThreshold = temp1.y;
            }
        }
        return darkenThreshold;
    }

    private float prefilterAmbientLux(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return prefilterMeanFilter(now);
        }
        if (filterMethodNum == 2) {
            return prefilterWeightedMeanFilter(now);
        }
        return prefilterNoFilter(now);
    }

    private float prefilterNoFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N != 0) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "prefilterNoFilter: No ambient light readings available, return 0");
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    private float prefilterMeanFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (this.mData.preMeanFilterNum <= 0 || this.mData.preMeanFilterNoFilterNum < this.mData.preMeanFilterNum) {
            Slog.e(TAG, "prefilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.preMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum);
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (N <= this.mData.preMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            for (int i = N - 1; i >= N - this.mData.preMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.preMeanFilterNum));
        }
    }

    private float prefilterWeightedMeanFilter(long now) {
        float weight;
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: No ambient light readings available, return 0");
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (this.mData.preWeightedMeanFilterNum <= 0 || this.mData.preWeightedMeanFilterNoFilterNum < this.mData.preWeightedMeanFilterNum) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: ErrorPara, return 0, WeightedMeanFilterNum=" + this.mData.preWeightedMeanFilterNum + ",WeightedMeanFilterNoFilterNum=" + this.mData.preWeightedMeanFilterNoFilterNum);
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else {
            float tempLux = this.mAmbientLightRingBuffer.getLux(N - 1);
            if (N <= this.mData.preWeightedMeanFilterNoFilterNum) {
                return tempLux;
            }
            float maxLux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            float sum = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            float totalWeight = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            for (int i = N - 1; i >= N - this.mData.preWeightedMeanFilterMaxFuncLuxNum; i--) {
                float tempLux2 = this.mAmbientLightRingBuffer.getLux(i);
                if (tempLux2 >= maxLux) {
                    maxLux = tempLux2;
                }
            }
            for (int i2 = N - 1; i2 >= N - this.mData.preWeightedMeanFilterNum; i2--) {
                if (this.mAmbientLightRingBuffer.getLux(i2) != GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || maxLux > this.mData.preWeightedMeanFilterLuxTh) {
                    weight = 1.0f * 1.0f;
                } else {
                    weight = this.mData.preWeightedMeanFilterAlpha * 1.0f;
                }
                totalWeight += weight;
                sum += this.mAmbientLightRingBuffer.getLux(i2) * weight;
            }
            return (float) Math.round(sum / totalWeight);
        }
    }

    private float getOrigLastAmbientLux(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N != 0) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "OrigAmbient: No ambient light readings available, return 0");
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    private float getFilterLastAmbientLux(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "FilterLastAmbient: No ambient light readings available, return 0");
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    private float postfilterAmbientLux(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return postfilterMeanFilter(now);
        }
        if (filterMethodNum == 2) {
            return postfilterMaxMinAvgFilter(now);
        }
        return postfilterNoFilter(now);
    }

    private float postfilterNoFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "postfilterNoFilter: No ambient light readings available, return 0");
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    private float postfilterMeanFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (this.mData.postMeanFilterNum <= 0 || this.mData.postMeanFilterNoFilterNum < this.mData.postMeanFilterNum) {
            Slog.e(TAG, "postfilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.postMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.postMeanFilterNum);
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (N <= this.mData.postMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        } else {
            float sum = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            for (int i = N - 1; i >= N - this.mData.postMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBufferFilter.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.postMeanFilterNum));
        }
    }

    private float postfilterMaxMinAvgFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: No ambient light readings available, return 0");
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (this.mData.postMaxMinAvgFilterNum <= 0 || this.mData.postMaxMinAvgFilterNoFilterNum < this.mData.postMaxMinAvgFilterNum) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: ErrorPara, return 0, PostMaxMinAvgFilterNoFilterNum=" + this.mData.postMaxMinAvgFilterNoFilterNum + ",PostMaxMinAvgFilterNum=" + this.mData.postMaxMinAvgFilterNum);
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (N <= this.mData.postMaxMinAvgFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        } else {
            float sum = this.mAmbientLightRingBufferFilter.getLux(N - 1);
            float luxMin = this.mAmbientLightRingBufferFilter.getLux(N - 1);
            float luxMax = this.mAmbientLightRingBufferFilter.getLux(N - 1);
            for (int i = N - 2; i >= N - this.mData.postMaxMinAvgFilterNum; i--) {
                if (luxMin > this.mAmbientLightRingBufferFilter.getLux(i)) {
                    luxMin = this.mAmbientLightRingBufferFilter.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBufferFilter.getLux(i)) {
                    luxMax = this.mAmbientLightRingBufferFilter.getLux(i);
                }
                sum += this.mAmbientLightRingBufferFilter.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / 3.0f;
        }
    }

    private long nextAmbientLightBrighteningTransition(long time) {
        boolean BrightenChange;
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            if (this.mAmbientLightRingBufferFilter.getLux(i) - getCurrentBrightenAmbientLux() > getCurrentBrightenDelta()) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long nextAmbientLightBrighteningTransitionForSmallThr(long time) {
        boolean BrightenChange;
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMin) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return this.mNormBrighenDebounceTimeForSmallThr + earliestValidTime;
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        boolean DarkenChange;
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            if (getCurrentDarkenAmbientLux() - this.mAmbientLightRingBufferFilter.getLux(i) >= getCurrentDarkenDelta()) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    private long nextAmbientLightDarkeningTransitionExtended(long time, float lux, float deltaLux, long debounceTime) {
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBufferFilter.size() - 1;
        while (i >= 0 && lux - this.mAmbientLightRingBufferFilter.getLux(i) >= deltaLux) {
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
            i--;
        }
        return earliestValidTime + debounceTime;
    }

    private boolean decideToDarkenForBackSensorCoverMode(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLux - ambientLux >= this.mDarkenDeltaLuxForBackSensorCoverMode) {
            needToDarken = true;
        } else {
            needToDarken = false;
        }
        return needToDarken && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus;
    }

    private boolean needToDarkenForBackSensorCoverMode(long time, float lux) {
        if (this.mData.backSensorCoverModeEnable && this.mIsCoverModeFastResponseFlag && decideToDarkenForBackSensorCoverMode(lux)) {
            if (nextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mDarkenDeltaLuxForBackSensorCoverMode, 500) <= time) {
                if (HWFLOW) {
                    Slog.d(TAG, "BackSensorCoverMode needToDarkenForBackSensorCoverMode");
                }
                return true;
            }
        }
        return false;
    }

    private boolean decideToBrighten(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - getCurrentBrightenAmbientLux() < getCurrentBrightenDelta() || this.mStability >= this.mStabilityBrightenConstant || ambientLux - getCurrentBrightenAmbientLux() < this.mBrightenDeltaLuxForCurrentBrightness) {
            needToBrighten = false;
        } else {
            needToBrighten = true;
        }
        return needToBrighten && !this.mAutoBrightnessIntervened;
    }

    private boolean decideToBrightenForSmallThr(float ambientLux) {
        boolean needToBrighten;
        boolean z = false;
        if (getThresholdForDcModeEnable()) {
            return false;
        }
        if (ambientLux - this.mAmbientLux < this.mBrightenDeltaLuxMin || this.mStabilityForSmallThr >= this.mStabilityBrightenConstantForSmallThr || ambientLux - this.mAmbientLuxNewMax < this.mBrightenDeltaLuxForCurrentBrightness) {
            needToBrighten = false;
        } else {
            needToBrighten = true;
        }
        if (needToBrighten && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus && !this.mLandScapeModeEnable) {
            z = true;
        }
        return z;
    }

    private boolean decideToDarken(float ambientLux) {
        boolean needToDarken;
        if (getCurrentDarkenAmbientLux() - ambientLux < getCurrentDarkenDelta() || this.mStability > this.mStabilityDarkenConstant || getCurrentDarkenAmbientLux() - ambientLux < this.mDarkenDeltaLuxForCurrentBrightness) {
            needToDarken = false;
        } else {
            needToDarken = true;
        }
        return needToDarken && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus;
    }

    private boolean decideToDarkenForSmallThr(float ambientLux) {
        boolean needToDarken;
        boolean z = false;
        if (getThresholdForDcModeEnable()) {
            return false;
        }
        if (this.mAmbientLux - ambientLux < this.mDarkenDeltaLuxMin || this.mStabilityForSmallThr > this.mStabilityDarkenConstantForSmallThr || this.mAmbientLuxNewMin - ambientLux < this.mDarkenDeltaLuxForCurrentBrightness) {
            needToDarken = false;
        } else {
            needToDarken = true;
        }
        if (needToDarken && !this.mAutoBrightnessIntervened && !this.mProximityPositiveStatus && !this.mLandScapeModeEnable) {
            z = true;
        }
        return z;
    }

    public boolean getProximityPositiveEnable() {
        return this.mData.allowLabcUseProximity && this.mProximityPositiveStatus;
    }

    public float getOffsetValidAmbientLux() {
        if (this.mData.offsetValidAmbientLuxEnable) {
            return this.mOffsetValidAmbientLux;
        }
        return this.mAmbientLux;
    }

    public float getValidAmbientLux(float lux) {
        float luxtmp = lux;
        if (luxtmp < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            luxtmp = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (luxtmp > 40000.0f) {
            return 40000.0f;
        }
        return luxtmp;
    }

    public void setCurrentAmbientLux(float lux) {
        if (((int) this.mAmbientLux) != ((int) lux)) {
            Slog.i(TAG, "setOffsetLux mAmbientLux=" + this.mAmbientLux + ",lux=" + lux);
            this.mAmbientLux = getValidAmbientLux(lux);
        }
    }

    private void setOffsetValidAmbientLux(float lux) {
        this.mOffsetValidAmbientLux = (float) Math.round(lux);
        this.mOffsetValidAmbientBrightenDeltaLux = calculateBrightenThresholdDeltaNew(this.mData.brightenlinePoints, this.mOffsetValidAmbientLux);
        this.mOffsetValidAmbientDarkenDeltaLux = calculateDarkenThresholdDeltaNew(this.mData.darkenlinePoints, this.mOffsetValidAmbientLux);
    }

    private long nextAmbientLightBrighteningTransitionForOffset(long time) {
        boolean BrightenChange;
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mOffsetValidAmbientLux > this.mOffsetValidAmbientBrightenDeltaLux) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return ((long) this.mData.offsetBrightenDebounceTime) + earliestValidTime;
    }

    private boolean decideToBrightenForOffset(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mOffsetValidAmbientLux >= this.mOffsetValidAmbientBrightenDeltaLux) {
            needToBrighten = true;
        } else {
            needToBrighten = false;
        }
        return needToBrighten && !this.mAutoBrightnessIntervened;
    }

    private boolean decideToDarkenForOffset(float ambientLux) {
        boolean needToDarken;
        if (this.mOffsetValidAmbientLux - ambientLux >= this.mOffsetValidAmbientDarkenDeltaLux) {
            needToDarken = true;
        } else {
            needToDarken = false;
        }
        return needToDarken && !this.mAutoBrightnessIntervened;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0114, code lost:
        r22 = r23;
     */
    private float calculateStability(HwRingBuffer buffer) {
        float luxT1Max;
        int indexMax;
        int index;
        int T1;
        int index2;
        int index1;
        float tmp;
        float a1;
        float Stability2;
        float Stability;
        float luxT1;
        HwRingBuffer hwRingBuffer = buffer;
        int N = buffer.size();
        if (N <= 1) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        float currentLux = hwRingBuffer.getLux(N - 1);
        calculateAvg(buffer);
        int indexMax2 = 0;
        float luxT2Max = currentLux;
        float luxT2Max2 = currentLux;
        int indexMin = 0;
        float f = currentLux;
        float luxT2Min = currentLux;
        int index3 = 0;
        int index4 = 0;
        int T12 = 0;
        float luxT2 = currentLux;
        float luxT2Max3 = currentLux;
        int j = 0;
        while (true) {
            if (j >= N - 1) {
                float f2 = luxT2Max3;
                luxT1Max = luxT2Max;
                indexMax = indexMax2;
                break;
            }
            float lux1 = hwRingBuffer.getLux((N - 1) - j);
            float currentLux2 = currentLux;
            float lux2 = hwRingBuffer.getLux(((N - 1) - j) - 1);
            float luxT12 = luxT2Max3;
            if (((this.mLuxBufferAvg > lux1 || this.mLuxBufferAvg < lux2) && (this.mLuxBufferAvg < lux1 || this.mLuxBufferAvg > lux2)) || (Math.abs(this.mLuxBufferAvg - lux1) < 1.0E-7f && Math.abs(this.mLuxBufferAvg - lux2) < 1.0E-7f)) {
                luxT1 = luxT12;
            } else {
                luxT1 = lux1;
                luxT2 = lux2;
                index3 = j;
                index4 = ((N - 1) - j) - 1;
                T12 = (N - 1) - j;
            }
            float luxT13 = luxT1;
            if (((this.mLuxBufferAvgMin <= lux1 && this.mLuxBufferAvgMin >= lux2) || (this.mLuxBufferAvgMin >= lux1 && this.mLuxBufferAvgMin <= lux2)) && (Math.abs(this.mLuxBufferAvgMin - lux1) >= 1.0E-7f || Math.abs(this.mLuxBufferAvgMin - lux2) >= 1.0E-7f)) {
                indexMin = j;
                float f3 = lux2;
                float luxT2Min2 = lux1;
            }
            if (((this.mLuxBufferAvgMax > lux1 || this.mLuxBufferAvgMax < lux2) && (this.mLuxBufferAvgMax < lux1 || this.mLuxBufferAvgMax > lux2)) || (Math.abs(this.mLuxBufferAvgMax - lux1) < 1.0E-7f && Math.abs(this.mLuxBufferAvgMax - lux2) < 1.0E-7f)) {
                luxT1Max = luxT2Max;
                indexMax = indexMax2;
            } else {
                indexMax = j;
                float luxT2Max4 = lux1;
                luxT1Max = lux2;
            }
            if (index3 == 0 || ((indexMin == 0 && indexMax == 0) || ((index3 > indexMin || index3 < indexMax) && (index3 < indexMin || index3 > indexMax)))) {
                j++;
                indexMax2 = indexMax;
                currentLux = currentLux2;
                luxT2Max = luxT1Max;
                luxT2Max3 = luxT13;
            }
        }
        if (indexMax <= indexMin) {
            index1 = indexMax;
            index2 = indexMin;
        } else {
            index1 = indexMin;
            index2 = indexMax;
        }
        int k1 = (N - 1) - index1;
        while (true) {
            int index12 = index1;
            if (k1 > N - 1) {
                float luxT2Max5 = luxT1Max;
                break;
            } else if (k1 == N - 1) {
                float f4 = luxT1Max;
                break;
            } else {
                float luxk1 = hwRingBuffer.getLux(k1);
                float luxT2Max6 = luxT1Max;
                float luxT2Max7 = hwRingBuffer.getLux(k1 + 1);
                if (indexMax > indexMin) {
                    if (luxk1 <= luxT2Max7) {
                        break;
                    }
                    T1 = k1 + 1;
                } else if (luxk1 >= luxT2Max7) {
                    break;
                } else {
                    T1 = k1 + 1;
                }
                k1++;
                index1 = index12;
                luxT1Max = luxT2Max6;
            }
        }
        int k3 = (N - 1) - index2;
        while (k3 >= 0 && k3 != 0) {
            float luxk3 = hwRingBuffer.getLux(k3);
            float luxk4 = hwRingBuffer.getLux(k3 - 1);
            if (indexMax > indexMin) {
                if (luxk3 >= luxk4) {
                    break;
                }
                index = k3 - 1;
            } else if (luxk3 <= luxk4) {
                break;
            } else {
                index = k3 - 1;
            }
            k3--;
        }
        int t1 = (N - 1) - T1;
        int t2 = index;
        float s1 = calculateStabilityFactor(hwRingBuffer, T1, N - 1);
        int i = index2;
        float avg1 = calcluateAvg(hwRingBuffer, T1, N - 1);
        int i2 = N;
        float s2 = calculateStabilityFactor(hwRingBuffer, 0, index);
        float avg2 = calcluateAvg(hwRingBuffer, 0, index);
        float f5 = luxT2;
        float deltaAvg = Math.abs(avg1 - avg2);
        float k = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (T1 != index) {
            k = Math.abs((hwRingBuffer.getLux(T1) - hwRingBuffer.getLux(index)) / ((float) (T1 - index)));
        }
        if (k < 10.0f / (k + 5.0f)) {
            tmp = k;
        } else {
            tmp = 10.0f / (k + 5.0f);
        }
        if (tmp > 20.0f / (deltaAvg + 10.0f)) {
            tmp = 20.0f / (deltaAvg + 10.0f);
        }
        float f6 = avg2;
        if (t1 > this.mData.stabilityTime1) {
            a1 = s1;
            float f7 = deltaAvg;
            int i3 = T1;
        } else {
            float f8 = deltaAvg;
            int i4 = T1;
            float a12 = (float) Math.exp((double) (t1 - this.mData.stabilityTime1));
            float b1 = (float) (this.mData.stabilityTime1 - t1);
            a1 = ((a12 * s1) + (b1 * tmp)) / (a12 + b1);
        }
        if (t2 > this.mData.stabilityTime2) {
            Stability2 = s2;
            float f9 = s1;
        } else {
            float f10 = s1;
            float a2 = (float) Math.exp((double) (t2 - this.mData.stabilityTime2));
            float b2 = (float) (this.mData.stabilityTime2 - t2);
            Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
        }
        if (t1 > this.mData.stabilityTime1) {
            Stability = a1;
            float f11 = avg1;
            int i5 = t2;
        } else {
            float f12 = avg1;
            int i6 = t2;
            float a = (float) Math.exp((double) (t1 - this.mData.stabilityTime1));
            float b = (float) (this.mData.stabilityTime1 - t1);
            Stability = ((a * a1) + (b * Stability2)) / (a + b);
        }
        return Stability;
    }

    private void calculateAvg(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N != 0) {
            float currentLux = buffer.getLux(N - 1);
            float luxBufferSum = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
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
            this.mLuxBufferAvg = luxBufferSum / ((float) N);
            this.mLuxBufferAvgMax = (this.mLuxBufferAvg + luxBufferMax) / 2.0f;
            this.mLuxBufferAvgMin = (this.mLuxBufferAvg + luxBufferMin) / 2.0f;
        }
    }

    private float calculateStabilityForSmallThr(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= 1) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (N <= 15) {
            return calculateStabilityFactor(buffer, 0, N - 1);
        }
        return calculateStabilityFactor(buffer, 0, 14);
    }

    private float calcluateAvg(HwRingBuffer buffer, int start, int end) {
        float sum = 0.0f;
        for (int i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        if (end < start) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        return sum / ((float) ((end - start) + 1));
    }

    private float calculateStabilityFactor(HwRingBuffer buffer, int start, int end) {
        int size = (end - start) + 1;
        float sum = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float sigma = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (size <= 1) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        for (int i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        float avg = sum / ((float) size);
        for (int i2 = start; i2 <= end; i2++) {
            sigma += (buffer.getLux(i2) - avg) * (buffer.getLux(i2) - avg);
        }
        float ss = sigma / ((float) (size - 1));
        if (avg == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        return ss / avg;
    }

    public boolean reportValueWhenSensorOnChange() {
        return this.mData.reportValueWhenSensorOnChange;
    }

    public int getProximityState() {
        return this.mProximity;
    }

    public boolean needToUseProximity() {
        return this.mData.allowLabcUseProximity;
    }

    public boolean needToSendProximityDebounceMsg() {
        return this.mNeedToSendProximityDebounceMsg;
    }

    public long getPendingProximityDebounceTime() {
        return this.mPendingProximityDebounceTime;
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

    public boolean getLastCloseScreenEnable() {
        return !this.mData.lastCloseScreenEnable;
    }

    public void clearProximity() {
        this.mProximity = -1;
        this.mPendingProximity = -1;
        setProximityState(false);
    }

    private void setProximityState(boolean proximityPositive) {
        this.mProximityPositiveStatus = proximityPositive;
        if (!this.mProximityPositiveStatus) {
            this.mNeedToUpdateBrightness = true;
            if (HWFLOW) {
                Slog.i(TAG, "Proximity sets brightness");
            }
        }
    }

    private void clearPendingProximityDebounceTime() {
        if (this.mPendingProximityDebounceTime >= 0) {
            this.mPendingProximityDebounceTime = -1;
        }
    }

    public void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mPendingProximity == 0 && !positive) {
            return;
        }
        if (this.mPendingProximity != 1 || !positive) {
            if (positive) {
                this.mPendingProximity = 1;
                this.mPendingProximityDebounceTime = ((long) this.mProximityPositiveDebounceTime) + time;
            } else {
                this.mPendingProximity = 0;
                this.mPendingProximityDebounceTime = ((long) this.mProximityNegativeDebounceTime) + time;
            }
            debounceProximitySensor();
        }
    }

    public void debounceProximitySensor() {
        this.mNeedToSendProximityDebounceMsg = false;
        if (this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                if (this.mProximity == 1) {
                    setProximityState(true);
                } else if (this.mProximity == 0) {
                    setProximityState(false);
                }
                if (HWFLOW) {
                    Slog.d(TAG, "debounceProximitySensor:mProximity=" + this.mProximity);
                }
                clearPendingProximityDebounceTime();
                return;
            }
            this.mNeedToSendProximityDebounceMsg = true;
        }
    }

    public int getpowerOnFastResponseLuxNum() {
        return this.mData.powerOnFastResponseLuxNum;
    }

    public boolean getCameraModeBrightnessLineEnable() {
        return this.mData.cameraModeEnable;
    }

    public boolean getReadingModeBrightnessLineEnable() {
        return this.mData.readingModeEnable;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
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

    public void setDayModeEnable() {
        Calendar c = Calendar.getInstance();
        int openDay = c.get(6);
        int openHour = c.get(11);
        int openMinute = c.get(12);
        int openTime = (openDay * 24 * 60) + (openHour * 60) + openMinute;
        if (this.mData.dayModeAlgoEnable && (this.mFirstSetBrightness || openTime - this.mLastCloseTime >= this.mData.dayModeSwitchTime)) {
            this.mDayModeEnable = false;
            if (this.mData.dayModeBeginTime < this.mData.dayModeEndTime) {
                if (openHour >= this.mData.dayModeBeginTime && openHour < this.mData.dayModeEndTime) {
                    this.mDayModeEnable = true;
                }
            } else if (openHour >= this.mData.dayModeBeginTime || openHour < this.mData.dayModeEndTime) {
                this.mDayModeEnable = true;
            }
        }
        if (this.mData.offsetResetEnable) {
            this.mOffsetResetEnable = false;
            if (openTime - this.mLastCloseTime >= this.mData.offsetResetSwitchTime) {
                this.mOffsetResetEnable = true;
                Slog.i(TAG, "offsetResetEnable detime=" + (openTime - this.mLastCloseTime));
            } else {
                float luxBright = this.mAutoModeEnableFirstLux + calculateBrightenThresholdDeltaNew(this.mData.brightenlinePoints, this.mAutoModeEnableFirstLux);
                float luxDark = this.mAutoModeEnableFirstLux - calculateDarkenThresholdDeltaNew(this.mData.darkenlinePoints, this.mAutoModeEnableFirstLux);
                if (Math.abs(this.mAutoModeEnableFirstLux - this.mLastCloseScreenLux) > ((float) this.mData.offsetResetShortLuxDelta) && ((luxBright > this.mLastCloseScreenLux || luxDark < this.mLastCloseScreenLux) && openTime - this.mLastCloseTime >= this.mData.offsetResetShortSwitchTime)) {
                    this.mOffsetResetEnable = true;
                    Slog.i(TAG, "offsetResetEnableShort detime=" + (openTime - this.mLastCloseTime) + ",mFirstLux=" + this.mAutoModeEnableFirstLux + ",mCloseLux=" + this.mLastCloseScreenLux + ",luxBright=" + luxBright + ",luxDark=" + luxDark);
                }
            }
        }
        if (HWFLOW) {
            Slog.d(TAG, "DayMode:openDay=" + openDay + ",openHour=" + openHour + ",openMinute=" + openMinute + ",openTime=" + openTime + ", mLastCloseTime" + this.mLastCloseTime + ", mFirstSetBrightness" + this.mFirstSetBrightness + ",mDayModeEnable=" + this.mDayModeEnable + ",mOffsetResetEnable=" + this.mOffsetResetEnable + ",offsetResetSwitchTime=" + this.mData.offsetResetSwitchTime);
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
        if (this.mData.coverModeBrighnessLinePoints == null || amLux < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Slog.e(TAG, "LabcCoverMode error input,set MIN_BRIGHTNESS,amLux=" + amLux);
            return 4;
        }
        float coverModebrightness = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        PointF temp1 = null;
        Iterator iter = this.mData.coverModeBrighnessLinePoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PointF temp = iter.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp2.x <= temp1.x) {
                    coverModebrightness = 4.0f;
                    if (HWFLOW) {
                        Slog.d(TAG, "LabcCoverMode,set MIN_BRIGHTNESS,Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    coverModebrightness = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
            } else {
                temp1 = temp;
                coverModebrightness = temp1.y;
            }
        }
        return (int) coverModebrightness;
    }

    private int getLuxFromDefaultBrightnessLevel(float brightnessLevel) {
        if (this.mData.defaultBrighnessLinePoints == null || brightnessLevel < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Slog.e(TAG, "LabcCoverMode,error input,set MIN_Lux,brightnessLevel=" + brightnessLevel);
            return 0;
        } else if (brightnessLevel == 255.0f) {
            Slog.i(TAG, "LabcCoverMode,brightnessLevel=MAX_Brightness,getMaxLux=40000");
            return AMBIENT_MAX_LUX;
        } else {
            float lux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            PointF temp1 = null;
            Iterator iter = this.mData.defaultBrighnessLinePoints.iterator();
            while (true) {
                if (!iter.hasNext()) {
                    break;
                }
                PointF temp = iter.next();
                if (temp1 == null) {
                    temp1 = temp;
                }
                if (brightnessLevel < temp.y) {
                    PointF temp2 = temp;
                    if (temp2.y <= temp1.y) {
                        lux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                        if (HWFLOW) {
                            Slog.d(TAG, "LabcCoverMode,set MIN_Lux,Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        }
                    } else {
                        lux = (((temp2.x - temp1.x) / (temp2.y - temp1.y)) * (brightnessLevel - temp1.y)) + temp1.x;
                    }
                } else {
                    temp1 = temp;
                    lux = temp1.x;
                }
            }
            return (int) lux;
        }
    }

    public void setGameModeEnable(boolean enable) {
        this.mGameModeEnable = enable;
        if (HWFLOW) {
            Slog.d(TAG, "GameBrightMode set mGameModeEnable=" + this.mGameModeEnable);
        }
    }

    public void setDarkTimeDelayFromBrightnessEnable(boolean enable) {
        if (HWFLOW && enable != this.mDarkTimeDelayFromBrightnessEnable) {
            Slog.d(TAG, "DarkTimeDelayFromBrightnessEnable=" + this.mDarkTimeDelayFromBrightnessEnable + ",enable=" + enable);
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
        if (getThresholdForGameModeEnable() || this.mLandScapeModeEnable || getThresholdForDcModeEnable()) {
            return this.mAmbientLux;
        }
        return this.mAmbientLuxNewMax;
    }

    private float getCurrentDarkenAmbientLux() {
        if (getThresholdForGameModeEnable() || this.mLandScapeModeEnable || getThresholdForDcModeEnable()) {
            return this.mAmbientLux;
        }
        return this.mAmbientLuxNewMin;
    }

    public float getCurrentBrightenDelta() {
        if (getThresholdForGameModeEnable()) {
            return this.mBrightenDeltaLuxMaxForGameMode;
        }
        if (this.mLandScapeModeEnable) {
            return this.mBrightenDeltaLuxMaxForLandScapeMode;
        }
        if (getThresholdForDcModeEnable()) {
            return this.mBrightenDeltaLuxMaxForDcMode;
        }
        return this.mBrightenDeltaLuxMax;
    }

    public float getCurrentDarkenDelta() {
        if (getThresholdForGameModeEnable()) {
            return this.mDarkenDeltaLuxMaxForGameMode;
        }
        if (this.mLandScapeModeEnable) {
            return this.mDarkenDeltaLuxMaxForLandScapeMode;
        }
        if (getThresholdForDcModeEnable()) {
            return this.mDarkenDeltaLuxMaxForDcMode;
        }
        return this.mDarkenDeltaLuxMax;
    }

    public void updateLandScapeMode(boolean enable) {
        if (HWFLOW && enable != this.mLandScapeModeEnable) {
            Slog.d(TAG, "LandScapeBrightMode mLandScapeModeEnable=" + this.mLandScapeModeEnable + "-->enable=" + enable);
        }
        this.mLandScapeModeEnable = enable;
    }

    public void updateTouchProximityState(boolean touchProximityState) {
        if (HWDEBUG && touchProximityState != this.mTouchProximityState) {
            Slog.d(TAG, "LandScapeBrightMode mTouchProximityState=" + this.mTouchProximityState + "-->touchProximityState=" + touchProximityState);
        }
        this.mTouchProximityState = touchProximityState;
    }

    public void updateBrightnessModeChangeManualState(boolean state) {
        this.mModeToAutoFastDarkenResponseEanble = state;
        this.mModeToAutoFastDarkenResponseMinLuxEanble = state;
        if (HWFLOW) {
            Slog.i(TAG, "set mModeToAutoFastDarkenResponseEanble=" + state);
        }
    }

    private boolean needToDarkenForSecondDarkenMode(long time, float lux) {
        if (this.mData.secondDarkenModeEanble && this.mSecondDarkenModeResponseEnable) {
            if (nextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mSecondDarkenModeDarkenDeltaLux, this.mData.secondDarkenModeDarkenDebounceTime) <= time && decideToDarkenForSecondDarkenMode(lux)) {
                if (HWFLOW) {
                    Slog.i(TAG, "updateAmbientLux needToDarkenForSecondDarkenMode");
                }
                return true;
            }
        }
        return false;
    }

    private boolean decideToDarkenForSecondDarkenMode(float ambientLux) {
        if (!(this.mAmbientLux - ambientLux >= this.mSecondDarkenModeDarkenDeltaLux) || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return false;
        }
        return true;
    }

    private boolean needToDarkenForModeToAutoFastDarkenResponse(long time, float lux) {
        if (this.mModeToAutoFastDarkenResponseEanble && this.mData.resetAmbientLuxEnable) {
            if (nextAmbientLightDarkeningTransitionExtended(time, this.mAmbientLux, this.mModeToAutoFastResponseDarkenDeltaLux, (long) this.mData.resetAmbientLuxDarkenDebounceTime) <= time && decideToDarkenFoModeToAutoFastDarkenResponse(lux)) {
                if (HWFLOW) {
                    Slog.i(TAG, "updateAmbientLux needToDarkenForModeToAutoFastDarkenResponse");
                }
                return true;
            }
        }
        return false;
    }

    private boolean decideToDarkenFoModeToAutoFastDarkenResponse(float ambientLux) {
        if (!(this.mAmbientLux - ambientLux >= this.mModeToAutoFastResponseDarkenDeltaLux) || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return false;
        }
        return true;
    }

    public void setCurrentAutoBrightness(int brightness) {
        float darkenLuxTh;
        float brightenLuxTh;
        if (brightness <= 0) {
            brightenLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            darkenLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else {
            brightenLuxTh = getValidAmbientLux(calculateBrightenThresholdDeltaNew(this.mData.brightenlinePointsForBrightnessLevel, (float) brightness));
            darkenLuxTh = getValidAmbientLux(calculateDarkenThresholdDeltaNew(this.mData.darkenlinePointsForBrightnessLevel, (float) brightness));
        }
        this.mBrightenDeltaLuxForCurrentBrightness = brightenLuxTh;
        this.mDarkenDeltaLuxForCurrentBrightness = darkenLuxTh;
        if (HWFLOW && brightness != this.mCurrentAutoBrightness && this.mCurrentAutoBrightness == 0) {
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

    private void updateModeToAutoFastDarkenResponseState(long time) {
        long timeDelta = time - this.mModeToAutoFastResponseDarkenStartTime;
        if (this.mModeToAutoFastDarkenResponseEanble && this.mData.resetAmbientLuxEnable && this.mModeToAutoFastResponseDarkenStartTimeEnable && timeDelta > ((long) this.mData.resetAmbientLuxFastDarkenValidTime)) {
            this.mModeToAutoFastResponseDarkenStartTimeEnable = false;
            this.mModeToAutoFastDarkenResponseEanble = false;
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxDisable,no need ModeToAutoFastDarkenResponseEanble,mCurrentAutoBrightness=" + this.mCurrentAutoBrightness + ",timeDelta=" + timeDelta + ",validTime=" + this.mData.resetAmbientLuxFastDarkenValidTime);
            }
        }
    }

    private float updateAmbientLuxFromResetAmbientLuxThMin(float lux, boolean needDarken) {
        float ambientLuxValue = lux;
        this.mSecondDarkenModeResponseEnable = false;
        if (this.mModeToAutoFastDarkenResponseEanble && this.mData.resetAmbientLuxEnable && this.mModeToAutoFastDarkenResponseMinLuxEanble && needDarken && this.mData.resetAmbientLuxThMin > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && lux < this.mData.resetAmbientLuxThMin) {
            if (HWFLOW) {
                Slog.i(TAG, "ResetAmbientLuxEn,lux=" + lux + ",-->resetAmbientLuxThMin=" + this.mData.resetAmbientLuxThMin);
            }
            this.mModeToAutoFastDarkenResponseMinLuxEanble = false;
            float ambientLuxValue2 = this.mData.resetAmbientLuxThMin;
            updateCurrentLuxForBuffer(ambientLuxValue2);
            return ambientLuxValue2;
        } else if (!this.mData.secondDarkenModeEanble || !needDarken || this.mData.secondDarkenModeMinLuxTh <= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || lux >= this.mData.secondDarkenModeMinLuxTh || this.mAmbientLux <= this.mData.secondDarkenModeMaxLuxTh) {
            return ambientLuxValue;
        } else {
            this.mSecondDarkenModeResponseEnable = true;
            float ambientLuxValue3 = this.mData.secondDarkenModeMinLuxTh;
            updateCurrentLuxForBuffer(ambientLuxValue3);
            if (!HWFLOW) {
                return ambientLuxValue3;
            }
            Slog.i(TAG, "ResetAmbientLuxEn,lux=" + lux + ",-->resetlux=" + this.mData.secondDarkenModeMinLuxTh + ",lastAmbientLux=" + this.mAmbientLux);
            return ambientLuxValue3;
        }
    }

    private void updateCurrentLuxForBuffer(float lux) {
        if (this.mAmbientLightRingBuffer.size() >= 1 && this.mAmbientLightRingBufferFilter.size() >= 1) {
            this.mAmbientLightRingBuffer.putLux(this.mAmbientLightRingBuffer.size() - 1, lux);
            this.mAmbientLightRingBufferFilter.putLux(this.mAmbientLightRingBuffer.size() - 1, lux);
        }
    }

    public boolean getFastDarkenDimmingEnable() {
        return this.mModeToAutoFastDarkenResponseEanble;
    }
}
