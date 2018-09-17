package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.emcom.SmartcareConstants;
import com.android.server.rms.iaware.cpu.CPUFeature;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HwAmbientLuxFilterAlgo {
    private static final int AMBIENT_LIGHT_HORIZON = 20000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int AMBIENT_MIN_LUX = 0;
    private static final int AMBIENT_SCENE_HORIZON = 80000;
    private static final boolean DEBUG;
    private static final int EXTRA_DELAY_TIME = 100;
    private static final int MIN_BRIGHTNESS = 4;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final String TAG = "HwAmbientLuxFilterAlgo";
    private HwRingBuffer mAmbientLightRingBuffer;
    private HwRingBuffer mAmbientLightRingBufferFilter;
    private HwRingBuffer mAmbientLightRingBufferScene;
    protected float mAmbientLux;
    private float mAmbientLuxNewMax;
    private float mAmbientLuxNewMin;
    public boolean mAutoBrightnessIntervened = false;
    private int mBrightPointCnt = -1;
    private float mBrightenDeltaLuxMax;
    private float mBrightenDeltaLuxMin;
    private boolean mCoverModeDayEnable = false;
    private boolean mCoverState = false;
    private float mDarkenDeltaLuxMax;
    private float mDarkenDeltaLuxMin;
    private final Data mData;
    private boolean mDayModeEnable = false;
    private boolean mFirstAmbientLux = true;
    private boolean mFirstSetBrightness = true;
    private boolean mIsCoverModeFastResponseFlag = false;
    private boolean mIsclosed = false;
    private boolean mKeyguardIsLocked;
    private float mLastCloseScreenLux = 0.0f;
    private HwRingBuffer mLastCloseScreenRingBuffer;
    private int mLastCloseTime = -1;
    private float mLastObservedLux;
    private final int mLightSensorRate;
    private final Object mLock = new Object();
    private float mLuxBufferAvg = 0.0f;
    private float mLuxBufferAvgMax = 0.0f;
    private float mLuxBufferAvgMin = 0.0f;
    private boolean mNeedToSendProximityDebounceMsg = false;
    private boolean mNeedToUpdateBrightness;
    public long mNextTransitionTime = -1;
    protected long mNormBrighenDebounceTime;
    protected long mNormBrighenDebounceTimeForSmallThr;
    protected long mNormDarkenDebounceTime;
    protected long mNormDarkenDebounceTimeForSmallThr;
    private boolean mOffsetResetEnable = false;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPowerStatus = false;
    private long mPrintLogTime = 0;
    private int mProximity = -1;
    private int mProximityNegativeDebounceTime = 3000;
    private int mProximityPositiveDebounceTime = CPUFeature.MSG_SET_VIP_THREAD_PARAMS;
    private boolean mProximityPositiveStatus;
    private int mResponseDurationPoints;
    private float mSceneAmbientLuxMax;
    private float mSceneAmbientLuxMin;
    private float mSceneAmbientLuxWeight;
    private float mStability = 0.0f;
    private float mStabilityBrightenConstant = 101.0f;
    private float mStabilityBrightenConstantForSmallThr;
    private float mStabilityDarkenConstant = 101.0f;
    private float mStabilityDarkenConstantForSmallThr;
    private float mStabilityForSmallThr = 0.0f;
    private float mlastFilterLux;

    public interface Callbacks {
        void updateBrightness();
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwAmbientLuxFilterAlgo(int lightSensorRate, int deviceActualBrightnessLevel) {
        this.mLightSensorRate = lightSensorRate;
        this.mNeedToUpdateBrightness = false;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        this.mAmbientLightRingBufferScene = new HwRingBuffer(250);
        this.mLastCloseScreenRingBuffer = new HwRingBuffer(50);
        this.mData = HwBrightnessXmlLoader.getData(deviceActualBrightnessLevel);
    }

    public void isFirstAmbientLux(boolean isFirst) {
        this.mFirstAmbientLux = isFirst;
    }

    public void handleLightSensorEvent(long time, float lux) {
        synchronized (this.mLock) {
            if (!this.mFirstAmbientLux && lux > this.mData.darkLightLuxMinThreshold && lux < this.mData.darkLightLuxMaxThreshold && this.mData.darkLightLuxMinThreshold < this.mData.darkLightLuxMaxThreshold) {
                lux += this.mData.darkLightLuxDelta;
                if (lux < 0.0f) {
                    lux = 0.0f;
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
        return;
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - 20000);
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
    }

    public float getCurrentAmbientLux() {
        return this.mAmbientLux;
    }

    public int getAmbientLuxSavedNum() {
        return this.mAmbientLightRingBuffer.size();
    }

    public List<Integer> getAmbientLuxSavedData(int size) {
        if (size > this.mAmbientLightRingBuffer.size()) {
            size = this.mAmbientLightRingBuffer.size();
        }
        ArrayList<Integer> data = new ArrayList();
        for (int i = 0; i < size; i++) {
            data.add(Integer.valueOf((int) this.mAmbientLightRingBuffer.getLux(i)));
        }
        return data;
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
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        this.mResponseDurationPoints = 0;
    }

    public void updateAmbientLux() {
        synchronized (this.mLock) {
            long time = SystemClock.uptimeMillis();
            try {
                this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
                this.mAmbientLightRingBuffer.prune(time - 20000);
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientLux:time=" + time + ",mLastObservedLux=" + this.mLastObservedLux);
                }
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return;
    }

    private float modifyFirstAmbientLux(float ambientLux) {
        int N = this.mLastCloseScreenRingBuffer.size();
        if (N > 0 && this.mData.initNumLastBuffer > 0) {
            float sumLux = 0.0f;
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

    private void updateAmbientLux(long time) {
        int needToBrighten;
        int needToDarken;
        float filterLux = prefilterAmbientLux(time, this.mData.preMethodNum);
        float lastestLux = getOrigLastAmbientLux(time);
        updateBuffer(time, filterLux, AMBIENT_LIGHT_HORIZON);
        updateBufferForScene(time, filterLux, AMBIENT_SCENE_HORIZON);
        this.mlastFilterLux = getFilterLastAmbientLux(time);
        float ambientLux = postfilterAmbientLux(time, this.mData.postMethodNum);
        if (this.mFirstAmbientLux) {
            if (this.mCoverState) {
                this.mCoverState = false;
                if (this.mPowerStatus) {
                    if (!this.mData.coverModelastCloseScreenEnable) {
                        ambientLux = this.mLastCloseScreenLux;
                        this.mAmbientLightRingBuffer.putLux(0, ambientLux);
                        this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
                        if (DEBUG) {
                            Slog.i(TAG, "LabcCoverMode1 use lastCloseScreenLux=" + this.mLastCloseScreenLux);
                        }
                    }
                    if (DEBUG) {
                        Slog.i(TAG, "LabcCoverMode1 ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
                    }
                } else {
                    if (this.mData.lastCloseScreenEnable) {
                        ambientLux = this.mLastCloseScreenLux;
                    } else if (!this.mData.coverModeDayEnable) {
                        ambientLux = this.mData.coverModeFirstLux;
                    } else if (this.mCoverModeDayEnable) {
                        ambientLux = (float) getLuxFromDefaultBrightnessLevel((float) this.mData.coverModeDayBrightness);
                        this.mCoverModeDayEnable = false;
                    } else {
                        ambientLux = (float) getLuxFromDefaultBrightnessLevel((float) getCoverModeBrightnessFromLastScreenBrightness());
                        Slog.i(TAG, "LabcCoverMode NewambientLux=" + ambientLux + ",LastScreenBrightness=" + getCoverModeBrightnessFromLastScreenBrightness());
                    }
                    this.mAmbientLightRingBuffer.putLux(0, ambientLux);
                    this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
                    if (DEBUG) {
                        Slog.i(TAG, "LabcCoverMode ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
                    }
                }
            }
            ambientLux = modifyFirstAmbientLux(ambientLux);
            setAmbientLux(ambientLux);
            this.mFirstAmbientLux = false;
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux + ",mLastCloseScreenLux=" + this.mLastCloseScreenLux + ",mAmbientLightRingBufferFilter=" + this.mAmbientLightRingBufferFilter);
            }
            this.mNeedToUpdateBrightness = true;
        }
        updateNewAmbientLuxFromScene(this.mAmbientLightRingBufferScene);
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        boolean needToBrighten2 = decideToBrighten(ambientLux);
        boolean needToBrightenNew = decideToBrighten(lastestLux);
        boolean needToDarken2 = decideToDarken(ambientLux);
        boolean needToDarkenNew = decideToDarken(lastestLux);
        if (((float) this.mBrightPointCnt) > -1.0E-6f) {
            this.mBrightPointCnt++;
        }
        if (this.mBrightPointCnt > this.mData.outdoorResponseCount) {
            this.mBrightPointCnt = -1;
        }
        long nextBrightenTransitionForSmallThr = nextAmbientLightBrighteningTransitionForSmallThr(time);
        long nextDarkenTransitionForSmallThr = nextAmbientLightDarkeningTransitionForSmallThr(time);
        boolean needToBrightenForSmallThr = decideToBrightenForSmallThr(ambientLux);
        boolean needToDarkenForSmallThr = decideToDarkenForSmallThr(ambientLux);
        needToBrightenForSmallThr = needToBrightenForSmallThr && nextBrightenTransitionForSmallThr <= time;
        needToDarkenForSmallThr = needToDarkenForSmallThr && nextDarkenTransitionForSmallThr <= time;
        if (needToBrighten2 && needToBrightenNew && nextBrightenTransition <= time) {
            needToBrighten = 1;
        } else {
            needToBrighten2 = needToBrightenForSmallThr;
        }
        if (needToDarken2 && needToDarkenNew && nextDarkenTransition <= time) {
            needToDarken = 1;
        } else {
            needToDarken2 = needToDarkenForSmallThr;
        }
        float brightenLux = this.mAmbientLux + this.mBrightenDeltaLuxMax;
        float darkenLux = this.mAmbientLux - this.mDarkenDeltaLuxMax;
        if (DEBUG && time - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "t=" + time + ",lx=" + this.mAmbientLightRingBuffer.toString(6) + ",mLx=" + this.mAmbientLux + ",s=" + this.mStability + ",ss=" + this.mStabilityForSmallThr + ",AuIntervened=" + this.mAutoBrightnessIntervened + ",mlastFilterLux=" + this.mlastFilterLux + ",mProximityState=" + this.mProximityPositiveStatus + ",bLux=" + brightenLux + ",dLux=" + darkenLux + ",mDt=" + this.mNormDarkenDebounceTime + ",mBt=" + this.mNormBrighenDebounceTime + ",mMax=" + this.mAmbientLuxNewMax + ",mMin=" + this.mAmbientLuxNewMin + ",mu=" + this.mSceneAmbientLuxWeight + ",sMax=" + this.mSceneAmbientLuxMax + ",sMin=" + this.mSceneAmbientLuxMin);
            this.mPrintLogTime = time;
        }
        if ((needToBrighten | needToDarken) != 0) {
            this.mBrightPointCnt = 0;
            setAmbientLux(ambientLux);
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: " + (needToBrighten != 0 ? "Brightened" : "Darkened") + ", mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer.toString(6) + ",mPxs=" + this.mProximityPositiveStatus + ", mAmbientLux=" + this.mAmbientLux + ",s=" + this.mStability + ",ss=" + this.mStabilityForSmallThr + ",needBs=" + needToBrightenForSmallThr + ",needDs=" + needToDarkenForSmallThr + ", mAmbientLightRingBufferF=" + this.mAmbientLightRingBufferFilter.toString(6));
                Slog.d(TAG, "PreMethodNum=" + this.mData.preMethodNum + ",PreMeanFilterNum=" + this.mData.preMeanFilterNum + ",mData.preMeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum + ",PostMethodNum=" + this.mData.postMethodNum);
            }
            if (DEBUG && this.mIsCoverModeFastResponseFlag) {
                Slog.i(TAG, "CoverModeBResponseTime=" + this.mData.coverModeBrightenResponseTime + ",CoverModeDResponseTime=" + this.mData.coverModeDarkenResponseTime);
            }
            if (DEBUG && this.mPowerStatus) {
                Slog.i(TAG, "PowerOnBT=" + this.mData.powerOnBrightenDebounceTime + ",PowerOnDT=" + this.mData.powerOnDarkenDebounceTime);
            }
            this.mNeedToUpdateBrightness = true;
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        if (nextDarkenTransition >= nextBrightenTransition) {
            nextDarkenTransition = nextBrightenTransition;
        }
        this.mNextTransitionTime = nextDarkenTransition;
        this.mNextTransitionTime = this.mNextTransitionTime > time ? (this.mNextTransitionTime + ((long) this.mLightSensorRate)) + 100 : (((long) this.mLightSensorRate) + time) + 100;
        if (DEBUG && time - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + this.mNextTransitionTime + TimeUtils.formatUptime(this.mNextTransitionTime));
        }
    }

    private void updateNewAmbientLuxFromScene(HwRingBuffer hwBuffer) {
        int N = hwBuffer.size();
        this.mAmbientLuxNewMax = this.mAmbientLux;
        this.mAmbientLuxNewMin = this.mAmbientLux;
        this.mSceneAmbientLuxMax = this.mAmbientLux;
        this.mSceneAmbientLuxMin = this.mAmbientLux;
        if (this.mResponseDurationPoints == SmartcareConstants.INVALID) {
            this.mResponseDurationPoints = SmartcareConstants.INVALID;
        } else {
            this.mResponseDurationPoints++;
        }
        if (N != 0 && N >= this.mData.sceneGapPoints && this.mResponseDurationPoints - this.mData.sceneMinPoints >= this.mData.sceneGapPoints && this.mData.sceneMaxPoints >= this.mData.sceneMinPoints && this.mData.sceneMaxPoints + this.mData.sceneGapPoints <= 228) {
            updateSceneBufferAmbientLuxMaxMinAvg(hwBuffer, this.mResponseDurationPoints < this.mData.sceneMaxPoints + this.mData.sceneGapPoints ? N - this.mResponseDurationPoints : (N - this.mData.sceneMaxPoints) - this.mData.sceneGapPoints, N - this.mData.sceneGapPoints);
            this.mSceneAmbientLuxWeight = ((float) this.mData.sceneGapPoints) / ((float) this.mResponseDurationPoints);
            if (this.mAmbientLux > this.mSceneAmbientLuxMax) {
                this.mAmbientLuxNewMax = (this.mSceneAmbientLuxWeight * this.mAmbientLux) + ((1.0f - this.mSceneAmbientLuxWeight) * this.mSceneAmbientLuxMax);
            }
            if (this.mAmbientLux > this.mSceneAmbientLuxMin) {
                this.mAmbientLuxNewMin = (this.mSceneAmbientLuxWeight * this.mAmbientLux) + ((1.0f - this.mSceneAmbientLuxWeight) * this.mSceneAmbientLuxMin);
            }
            correctAmbientLux();
        }
    }

    private void updateSceneBufferAmbientLuxMaxMinAvg(HwRingBuffer buffer, int start, int end) {
        int N = buffer.size();
        if (N == 0 || end < start || start > N - 1 || end < 0 || start < 0 || end > N - 1) {
            Slog.i(TAG, "SceneBufferAmbientLux input error,end=" + end + ",start=" + start + ",N=" + N);
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
        float luxMean = (float) Math.round(luxSum / ((float) ((end - start) + 1)));
        this.mSceneAmbientLuxMax = (this.mData.sceneAmbientLuxMaxWeight * luxMean) + ((1.0f - this.mData.sceneAmbientLuxMaxWeight) * luxMax);
        this.mSceneAmbientLuxMin = (this.mData.sceneAmbientLuxMinWeight * luxMean) + ((1.0f - this.mData.sceneAmbientLuxMinWeight) * luxMin);
    }

    private void correctAmbientLux() {
        float ambientLuxDarkenDelta = calculateDarkenThresholdDelta(this.mAmbientLux);
        float ambientLuxNewMaxBrightenDelta = calculateBrightenThresholdDelta(this.mAmbientLuxNewMax);
        float ambientLuxNewMinBrightenDelta = calculateBrightenThresholdDelta(this.mAmbientLuxNewMin);
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMax - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
            this.mAmbientLuxNewMax = this.mAmbientLux;
        }
        if (this.mAmbientLux > (this.mAmbientLuxNewMax + ambientLuxNewMaxBrightenDelta) - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxNewMaxBrightenDelta=" + ambientLuxNewMaxBrightenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
            this.mAmbientLuxNewMax = this.mAmbientLux;
        }
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMin - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMin:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMin=" + this.mAmbientLuxNewMin);
            this.mAmbientLuxNewMin = this.mAmbientLux;
        }
        if (this.mAmbientLux > (this.mAmbientLuxNewMin + ambientLuxNewMinBrightenDelta) - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMin:mAmbientLux" + this.mAmbientLux + ", ambientLuxNewMinBrightenDelta=" + ambientLuxNewMinBrightenDelta + ", mAmbientLuxNewMin=" + this.mAmbientLuxNewMin);
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
        if (!this.mPowerStatus) {
            return this.mNormBrighenDebounceTime + earliedtime;
        }
        if (getSlowResponsePowerStatus()) {
            return (((long) this.mData.powerOnBrightenDebounceTime) + earliedtime) + ((long) this.mData.initSlowReponseBrightTime);
        }
        return ((long) this.mData.powerOnBrightenDebounceTime) + earliedtime;
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
        return this.mNormDarkenDebounceTime + earliedtime;
    }

    /* JADX WARNING: Missing block: B:15:0x0036, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getKeyguardLockedBrightenEnable() {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "getKeyguardLocked no lux");
            return false;
        } else if (!this.mKeyguardIsLocked || this.mAmbientLightRingBuffer.getLux(N - 1) <= this.mData.keyguardLuxThreshold || this.mData.keyguardResponseBrightenTime <= 0 || getProximityPositiveBrightenEnable()) {
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
        } else if (!this.mKeyguardIsLocked || this.mAmbientLightRingBuffer.getLux(N - 1) <= this.mData.keyguardLuxThreshold || this.mData.keyguardResponseDarkenTime <= 0) {
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
            if (DEBUG) {
                Slog.d(TAG, "clear buffer data and algo flags");
            }
            this.mLastCloseScreenLux = this.mAmbientLux;
            if (DEBUG) {
                Slog.d(TAG, "LabcCoverMode clear: mLastCloseScreenLux=" + this.mLastCloseScreenLux);
            }
            if (this.mData.initNumLastBuffer > 0) {
                int N = this.mAmbientLightRingBuffer.size();
                for (int i = 0; i < N; i++) {
                    this.mLastCloseScreenRingBuffer.push(this.mAmbientLightRingBuffer.getTime(i), this.mAmbientLightRingBuffer.getLux(i));
                }
                int pruneNTmp = this.mLastCloseScreenRingBuffer.size() - this.mData.initNumLastBuffer;
                int pruneN = pruneNTmp > 0 ? pruneNTmp : 0;
                if (pruneN > 0) {
                    this.mLastCloseScreenRingBuffer.prune(1 + this.mLastCloseScreenRingBuffer.getTime(pruneN - 1));
                }
                if (DEBUG) {
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
                int lastCloseMinute = c.get(12);
                this.mLastCloseTime = (((lastCloseDay * 24) * 60) + (lastCloseHour * 60)) + lastCloseMinute;
                if (DEBUG) {
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
        if (mLux >= this.mData.brightTimeDelayLuxThreshold || this.mlastFilterLux >= this.mData.brightTimeDelayLuxThreshold || !this.mData.brightTimeDelayEnable) {
            this.mNormBrighenDebounceTime = (long) (((float) this.mData.brighenDebounceTime) * (((this.mData.brightenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + 1.0f));
        } else {
            this.mNormBrighenDebounceTime = (long) this.mData.brightTimeDelay;
        }
        if (mLux >= this.mData.darkTimeDelayLuxThreshold || !this.mData.darkTimeDelayEnable) {
            this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkenDebounceTime) * (((this.mData.darkenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + 1.0f));
        } else {
            float ambientLuxDarkenDelta = calculateDarkenThresholdDelta(this.mAmbientLux);
            float currentAmbientLux = buffer.getLux(buffer.size() - 1);
            float luxNormalizedFactor = ((this.mData.darkTimeDelayBeta2 * (this.mAmbientLux - currentAmbientLux)) + (this.mData.darkTimeDelayBeta1 * ((this.mAmbientLux - currentAmbientLux) - ambientLuxDarkenDelta))) + 1.0f;
            if (luxNormalizedFactor < 0.001f) {
                this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkTimeDelay) + this.mData.darkTimeDelayBeta0);
            } else {
                this.mNormDarkenDebounceTime = ((long) this.mData.darkTimeDelay) + ((long) ((this.mData.darkTimeDelayBeta0 * ((this.mData.darkTimeDelayBeta2 * 1.0f) + 1.0f)) / luxNormalizedFactor));
            }
        }
        this.mNormBrighenDebounceTimeForSmallThr = (long) this.mData.brighenDebounceTimeForSmallThr;
        this.mNormDarkenDebounceTimeForSmallThr = (long) this.mData.darkenDebounceTimeForSmallThr;
        setDarkenThresholdNew(this.mAmbientLuxNewMin);
        setBrightenThresholdNew(this.mAmbientLuxNewMax);
    }

    private void setBrightenThresholdNew(float amLux) {
        this.mBrightenDeltaLuxMax = calculateBrightenThresholdDelta(amLux);
        this.mBrightenDeltaLuxMin = this.mBrightenDeltaLuxMax * this.mData.ratioForBrightnenSmallThr;
        this.mBrightenDeltaLuxMax *= ((this.mData.brightenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + 1.0f;
        if (((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseBrightenRatio > 0.0f) {
            this.mBrightenDeltaLuxMax *= this.mData.outdoorResponseBrightenRatio;
        }
    }

    private void setDarkenThresholdNew(float amLux) {
        this.mDarkenDeltaLuxMax = calculateDarkenThresholdDelta(amLux);
        if (this.mAmbientLux < 10.0f) {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax;
        } else {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax * this.mData.ratioForDarkenSmallThr;
        }
        this.mDarkenDeltaLuxMax *= ((this.mData.darkenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + 1.0f;
        if (((float) this.mBrightPointCnt) > -1.0E-6f && this.mAmbientLux > ((float) this.mData.outdoorLowerLuxThreshold) && this.mData.outdoorResponseDarkenRatio > 0.0f) {
            this.mDarkenDeltaLuxMax *= this.mData.outdoorResponseDarkenRatio;
        }
    }

    private float calculateBrightenThresholdDelta(float amLux) {
        float brightenThreshold = 0.0f;
        PointF temp1 = null;
        for (PointF temp : this.mData.brightenlinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
                if (!DEBUG) {
                    return 1.0f;
                }
                Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return 1.0f;
            }
            temp1 = temp;
            brightenThreshold = temp.y;
        }
        return brightenThreshold;
    }

    private float calculateDarkenThresholdDelta(float amLux) {
        float darkenThreshold = 0.0f;
        PointF temp1 = null;
        for (PointF temp : this.mData.darkenlinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    float darkenThresholdTmp = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                    return darkenThresholdTmp > 1.0f ? darkenThresholdTmp : 1.0f;
                } else if (!DEBUG) {
                    return 1.0f;
                } else {
                    Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    return 1.0f;
                }
            }
            temp1 = temp;
            darkenThreshold = temp.y;
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
        return 0.0f;
    }

    private float prefilterMeanFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.preMeanFilterNum <= 0 || this.mData.preMeanFilterNoFilterNum < this.mData.preMeanFilterNum) {
            Slog.e(TAG, "prefilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.preMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum);
            return 0.0f;
        } else if (N <= this.mData.preMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = 0.0f;
            for (int i = N - 1; i >= N - this.mData.preMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.preMeanFilterNum));
        }
    }

    private float prefilterWeightedMeanFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.preWeightedMeanFilterNum <= 0 || this.mData.preWeightedMeanFilterNoFilterNum < this.mData.preWeightedMeanFilterNum) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: ErrorPara, return 0, WeightedMeanFilterNum=" + this.mData.preWeightedMeanFilterNum + ",WeightedMeanFilterNoFilterNum=" + this.mData.preWeightedMeanFilterNoFilterNum);
            return 0.0f;
        } else {
            float tempLux = this.mAmbientLightRingBuffer.getLux(N - 1);
            if (N <= this.mData.preWeightedMeanFilterNoFilterNum) {
                return tempLux;
            }
            int i;
            float maxLux = 0.0f;
            float sum = 0.0f;
            float totalWeight = 0.0f;
            for (i = N - 1; i >= N - this.mData.preWeightedMeanFilterMaxFuncLuxNum; i--) {
                tempLux = this.mAmbientLightRingBuffer.getLux(i);
                if (tempLux >= maxLux) {
                    maxLux = tempLux;
                }
            }
            for (i = N - 1; i >= N - this.mData.preWeightedMeanFilterNum; i--) {
                float weight;
                if (this.mAmbientLightRingBuffer.getLux(i) != 0.0f || maxLux > this.mData.preWeightedMeanFilterLuxTh) {
                    weight = 1.0f;
                } else {
                    weight = this.mData.preWeightedMeanFilterAlpha * 1.0f;
                }
                totalWeight += weight;
                sum += this.mAmbientLightRingBuffer.getLux(i) * weight;
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
        return 0.0f;
    }

    private float getFilterLastAmbientLux(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "FilterLastAmbient: No ambient light readings available, return 0");
        return 0.0f;
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
        return 0.0f;
    }

    private float postfilterMeanFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMeanFilterNum <= 0 || this.mData.postMeanFilterNoFilterNum < this.mData.postMeanFilterNum) {
            Slog.e(TAG, "postfilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.postMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.postMeanFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        } else {
            float sum = 0.0f;
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
            return 0.0f;
        } else if (this.mData.postMaxMinAvgFilterNum <= 0 || this.mData.postMaxMinAvgFilterNoFilterNum < this.mData.postMaxMinAvgFilterNum) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: ErrorPara, return 0, PostMaxMinAvgFilterNoFilterNum=" + this.mData.postMaxMinAvgFilterNoFilterNum + ",PostMaxMinAvgFilterNum=" + this.mData.postMaxMinAvgFilterNum);
            return 0.0f;
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
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLuxNewMax > this.mBrightenDeltaLuxMax) {
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
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean BrightenChange;
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
        return earliestValidTime + this.mNormBrighenDebounceTimeForSmallThr;
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLuxNewMin - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax) {
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

    private long nextAmbientLightDarkeningTransitionForSmallThr(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMin) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + this.mNormDarkenDebounceTimeForSmallThr;
    }

    private boolean decideToBrighten(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLuxNewMax < this.mBrightenDeltaLuxMax || this.mStability >= this.mStabilityBrightenConstant) {
            needToBrighten = false;
        } else {
            needToBrighten = true;
        }
        return needToBrighten ? this.mAutoBrightnessIntervened ^ 1 : false;
    }

    private boolean decideToBrightenForSmallThr(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLux < this.mBrightenDeltaLuxMin || this.mStabilityForSmallThr >= this.mStabilityBrightenConstantForSmallThr) {
            needToBrighten = false;
        } else {
            needToBrighten = true;
        }
        return (!needToBrighten || (this.mAutoBrightnessIntervened ^ 1) == 0) ? false : this.mProximityPositiveStatus ^ 1;
    }

    private boolean decideToDarken(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLuxNewMin - ambientLux < this.mDarkenDeltaLuxMax || this.mStability > this.mStabilityDarkenConstant) {
            needToDarken = false;
        } else {
            needToDarken = true;
        }
        return (!needToDarken || (this.mAutoBrightnessIntervened ^ 1) == 0) ? false : this.mProximityPositiveStatus ^ 1;
    }

    private boolean decideToDarkenForSmallThr(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLux - ambientLux < this.mDarkenDeltaLuxMin || this.mStabilityForSmallThr > this.mStabilityDarkenConstantForSmallThr) {
            needToDarken = false;
        } else {
            needToDarken = true;
        }
        return (!needToDarken || (this.mAutoBrightnessIntervened ^ 1) == 0) ? false : this.mProximityPositiveStatus ^ 1;
    }

    private float calculateStability(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= 1) {
            return 0.0f;
        }
        int index1;
        int index2;
        float tmp;
        float Stability1;
        float Stability2;
        float Stability;
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
        for (int j = 0; j < N - 1; j++) {
            Object obj;
            float lux1 = buffer.getLux((N - 1) - j);
            float lux2 = buffer.getLux(((N - 1) - j) - 1);
            if ((this.mLuxBufferAvg <= lux1 && this.mLuxBufferAvg >= lux2) || (this.mLuxBufferAvg >= lux1 && this.mLuxBufferAvg <= lux2)) {
                obj = (Math.abs(this.mLuxBufferAvg - lux1) >= 1.0E-7f || Math.abs(this.mLuxBufferAvg - lux2) >= 1.0E-7f) ? null : 1;
                if (obj == null) {
                    luxT1 = lux1;
                    luxT2 = lux2;
                    T1 = (N - 1) - j;
                    T2 = ((N - 1) - j) - 1;
                    index = j;
                }
            }
            if ((this.mLuxBufferAvgMin <= lux1 && this.mLuxBufferAvgMin >= lux2) || (this.mLuxBufferAvgMin >= lux1 && this.mLuxBufferAvgMin <= lux2)) {
                obj = (Math.abs(this.mLuxBufferAvgMin - lux1) >= 1.0E-7f || Math.abs(this.mLuxBufferAvgMin - lux2) >= 1.0E-7f) ? null : 1;
                if (obj == null) {
                    luxT1Min = lux1;
                    luxT2Min = lux2;
                    indexMin = j;
                }
            }
            if ((this.mLuxBufferAvgMax <= lux1 && this.mLuxBufferAvgMax >= lux2) || (this.mLuxBufferAvgMax >= lux1 && this.mLuxBufferAvgMax <= lux2)) {
                obj = (Math.abs(this.mLuxBufferAvgMax - lux1) >= 1.0E-7f || Math.abs(this.mLuxBufferAvgMax - lux2) >= 1.0E-7f) ? null : 1;
                if (obj == null) {
                    luxT1Max = lux1;
                    luxT2Max = lux2;
                    indexMax = j;
                }
            }
            if (index != 0 && ((indexMin != 0 || indexMax != 0) && ((index <= indexMin && index >= indexMax) || (index >= indexMin && index <= indexMax)))) {
                break;
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
        while (k1 <= N - 1 && k1 != N - 1) {
            float luxk1 = buffer.getLux(k1);
            float luxk2 = buffer.getLux(k1 + 1);
            if (indexMax > indexMin) {
                if (luxk1 <= luxk2) {
                    break;
                }
            } else if (luxk1 >= luxk2) {
                break;
            }
            T1 = k1 + 1;
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
            } else if (luxk3 <= luxk4) {
                break;
            }
            T2 = k3 - 1;
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
        if (t1 > this.mData.stabilityTime1) {
            Stability1 = s1;
        } else {
            float a1 = (float) Math.exp((double) (t1 - this.mData.stabilityTime1));
            float b1 = (float) (this.mData.stabilityTime1 - t1);
            float s3 = tmp;
            Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
        }
        if (t2 > this.mData.stabilityTime2) {
            Stability2 = s2;
        } else {
            float a2 = (float) Math.exp((double) (t2 - this.mData.stabilityTime2));
            float b2 = (float) (this.mData.stabilityTime2 - t2);
            float s4 = tmp;
            Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
        }
        if (t1 > this.mData.stabilityTime1) {
            Stability = Stability1;
        } else {
            float a = (float) Math.exp((double) (t1 - this.mData.stabilityTime1));
            float b = (float) (this.mData.stabilityTime1 - t1);
            Stability = ((a * Stability1) + (b * Stability2)) / (a + b);
        }
        return Stability;
    }

    private void calculateAvg(HwRingBuffer buffer) {
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
            this.mLuxBufferAvg = luxBufferSum / ((float) N);
            this.mLuxBufferAvgMax = (this.mLuxBufferAvg + luxBufferMax) / 2.0f;
            this.mLuxBufferAvgMin = (this.mLuxBufferAvg + luxBufferMin) / 2.0f;
        }
    }

    private float calculateStabilityForSmallThr(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= 1) {
            return 0.0f;
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
            return 0.0f;
        }
        return sum / ((float) ((end - start) + 1));
    }

    private float calculateStabilityFactor(HwRingBuffer buffer, int start, int end) {
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
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode mIsCoverModeFastResponseFlag=" + this.mIsCoverModeFastResponseFlag);
        }
    }

    public boolean getLastCloseScreenEnable() {
        return this.mData.lastCloseScreenEnable ^ 1;
    }

    private void setProximityState(boolean proximityPositive) {
        this.mProximityPositiveStatus = proximityPositive;
        if (!this.mProximityPositiveStatus) {
            this.mNeedToUpdateBrightness = true;
            if (DEBUG) {
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
        if (this.mPendingProximity == 0 && (positive ^ 1) != 0) {
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
                if (DEBUG) {
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

    public void setDayModeEnable() {
        Calendar c = Calendar.getInstance();
        int openDay = c.get(6);
        int openHour = c.get(11);
        int openMinute = c.get(12);
        int openTime = (((openDay * 24) * 60) + (openHour * 60)) + openMinute;
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
            }
        }
        if (DEBUG) {
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
        if (this.mData.coverModeBrighnessLinePoints == null || amLux < 0.0f) {
            Slog.e(TAG, "LabcCoverMode error input,set MIN_BRIGHTNESS,amLux=" + amLux);
            return 4;
        }
        float coverModebrightness = 0.0f;
        PointF temp1 = null;
        for (PointF temp : this.mData.coverModeBrighnessLinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x <= temp1.x) {
                    coverModebrightness = 4.0f;
                    if (DEBUG) {
                        Slog.d(TAG, "LabcCoverMode,set MIN_BRIGHTNESS,Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    coverModebrightness = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
                return (int) coverModebrightness;
            }
            temp1 = temp;
            coverModebrightness = temp.y;
        }
        return (int) coverModebrightness;
    }

    private int getLuxFromDefaultBrightnessLevel(float brightnessLevel) {
        if (this.mData.defaultBrighnessLinePoints == null || brightnessLevel < 0.0f) {
            Slog.e(TAG, "LabcCoverMode,error input,set MIN_Lux,brightnessLevel=" + brightnessLevel);
            return 0;
        } else if (brightnessLevel == 255.0f) {
            Slog.i(TAG, "LabcCoverMode,brightnessLevel=MAX_Brightness,getMaxLux=40000");
            return AMBIENT_MAX_LUX;
        } else {
            float lux = 0.0f;
            PointF temp1 = null;
            for (PointF temp : this.mData.defaultBrighnessLinePoints) {
                if (temp1 == null) {
                    temp1 = temp;
                }
                if (brightnessLevel < temp.y) {
                    PointF temp2 = temp;
                    if (temp.y <= temp1.y) {
                        lux = 0.0f;
                        if (DEBUG) {
                            Slog.d(TAG, "LabcCoverMode,set MIN_Lux,Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        }
                    } else {
                        lux = (((temp.x - temp1.x) / (temp.y - temp1.y)) * (brightnessLevel - temp1.y)) + temp1.x;
                    }
                    return (int) lux;
                }
                temp1 = temp;
                lux = temp.x;
            }
            return (int) lux;
        }
    }
}
