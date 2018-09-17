package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.display.HwCCTXmlLoader.Data;

public class HwAmbientCCTFilterAlgo {
    private static final int AMBIENT_CCT_HORIZON = 20000;
    private static final int AMBIENT_MAX_CCT = 10000;
    private static final boolean DEBUG;
    private static final int EXTRA_DELAY_TIME = 100;
    private static final long POWER_ON_ASCENDING_CCT_DEBOUNCE = 500;
    private static final long POWER_ON_DESCENDING_CCT_DEBOUNCE = 1000;
    private static final String TAG = "HwAmbientCCTFilterAlgo";
    protected float mAmbientCCT;
    private float mAmbientCCTNewMax;
    private float mAmbientCCTNewMin;
    private HwRingBuffer mAmbientCCTRingBuffer;
    private HwRingBuffer mAmbientCCTRingBufferFilter;
    private float mAscendDeltaCCTMax;
    public boolean mAutoCCTIntervened = false;
    private final Data mData;
    private float mDescendDeltaCCTMax;
    private boolean mFirstAmbientCCT = true;
    private boolean mIsCoverModeFastResponseFlag = false;
    private float mLastCloseScreenCCT = 0.0f;
    private float mLastObservedCCT;
    private final int mLightSensorRate;
    private final Object mLock = new Object();
    private boolean mNeedToUpdateCCT;
    public long mNextTransitionTime = -1;
    protected long mNormAscendDebounceTime;
    protected long mNormDescendDebounceTime;
    private boolean mPowerStatus = false;
    private long mPrintLogTime = 0;
    private boolean mReportValueWhenSensorOnChange = true;
    private float mlastFilterCCT;

    public interface Callbacks {
        void updateCCT();
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwAmbientCCTFilterAlgo(int lightSensorRate) {
        this.mLightSensorRate = lightSensorRate;
        this.mNeedToUpdateCCT = false;
        this.mAmbientCCTRingBuffer = new HwRingBuffer(50);
        this.mAmbientCCTRingBufferFilter = new HwRingBuffer(50);
        this.mData = HwCCTXmlLoader.getData();
    }

    public void isFirstAmbientCCT(boolean isFirst) {
        this.mFirstAmbientCCT = isFirst;
    }

    public void handleLightSensorEvent(long time, float cct) {
        synchronized (this.mLock) {
            if (cct > 10000.0f) {
                if (DEBUG) {
                    Slog.i(TAG, "cct >= max, cct=" + cct);
                }
                cct = 10000.0f;
            }
            try {
                applyLightSensorMeasurement(time, cct);
                updateAmbientCCT(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                Slog.e(TAG, "Array index out of bounds");
            }
        }
        return;
    }

    private void applyLightSensorMeasurement(long time, float cct) {
        this.mAmbientCCTRingBuffer.prune(time - 20000);
        this.mAmbientCCTRingBuffer.push(time, cct);
        this.mLastObservedCCT = cct;
    }

    public float getCurrentAmbientCCT() {
        return this.mAmbientCCT;
    }

    private void setAmbientCCT(float cct) {
        this.mAmbientCCT = (float) Math.round(cct);
        updatepara(this.mAmbientCCTRingBuffer, this.mAmbientCCT);
    }

    public void updateAmbientCCT() {
        synchronized (this.mLock) {
            long time = SystemClock.uptimeMillis();
            try {
                this.mAmbientCCTRingBuffer.push(time, this.mLastObservedCCT);
                this.mAmbientCCTRingBuffer.prune(time - 20000);
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientCCT:time=" + time + ",mLastObservedCCT=" + this.mLastObservedCCT);
                }
                updateAmbientCCT(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                Slog.e(TAG, "Array index out of bounds");
            }
        }
        return;
    }

    private void updateAmbientCCT(long time) {
        float filterCCT = prefilterAmbientCCT(time, this.mData.preMethodNum);
        float lastestCCT = getOrigLastAmbientCCT(time);
        updateBuffer(time, filterCCT, AMBIENT_CCT_HORIZON);
        this.mlastFilterCCT = getFilterLastAmbientCCT(time);
        float ambientCCT = postfilterAmbientCCT(time, this.mData.postMethodNum);
        if (this.mFirstAmbientCCT) {
            setAmbientCCT(ambientCCT);
            this.mFirstAmbientCCT = false;
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientCCT: Initializing: mAmbientCCTRingBuffer=" + this.mAmbientCCTRingBuffer + ", mAmbientCCT=" + this.mAmbientCCT + ",mLastCloseScreenCCT=" + this.mLastCloseScreenCCT + ",mAmbientCCTRingBufferFilter=" + this.mAmbientCCTRingBufferFilter);
            }
            this.mNeedToUpdateCCT = true;
        }
        this.mAmbientCCTNewMax = this.mAmbientCCT;
        this.mAmbientCCTNewMin = this.mAmbientCCT;
        updatepara(this.mAmbientCCTRingBuffer, this.mAmbientCCT);
        long nextAscendTransition = nextAmbientCCTAscendingTransition(time);
        long nextDescendTransition = nextAmbientCCTDescendingTransition(time);
        boolean needToAscend = decideToAscend(ambientCCT);
        boolean needToAscendNew = decideToAscend(lastestCCT);
        boolean needToDescend = decideToDescend(ambientCCT);
        boolean needToDescendNew = decideToDescend(lastestCCT);
        needToAscend = needToAscend && needToAscendNew && nextAscendTransition <= time;
        needToDescend = needToDescend && needToDescendNew && nextDescendTransition <= time;
        if (DEBUG && time - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "t=" + time + ",cct=" + this.mAmbientCCTRingBuffer.toString(6) + ",mCCT=" + this.mAmbientCCT + ",AuIntervened=" + this.mAutoCCTIntervened + ",mlastFilterCCT=" + this.mlastFilterCCT + ",ambientCCT=" + ambientCCT + ",nextAscendTransition = " + nextAscendTransition + ",nextDescendTransition=" + nextDescendTransition + ",mAmbientCCTNewMax=" + this.mAmbientCCTNewMax + ",mAmbientCCTNewMin=" + this.mAmbientCCTNewMin + ",mAscendDeltaCCTMax=" + this.mAscendDeltaCCTMax + ",mDescendDeltaCCTMax=" + this.mDescendDeltaCCTMax);
            this.mPrintLogTime = time;
        }
        if ((needToAscend | needToDescend) != 0) {
            setAmbientCCT(ambientCCT);
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientCCT: " + (needToAscend ? "Ascend" : "Descend") + ", mAmbientCCTRingBuffer=" + this.mAmbientCCTRingBuffer.toString(6) + ", mAmbientCCT=" + this.mAmbientCCT);
                Slog.d(TAG, "PreMethodNum=" + this.mData.preMethodNum + ",PreMeanFilterNum=" + this.mData.preMeanFilterNum + ",mData.preMeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum + ",PostMethodNum=" + this.mData.postMethodNum);
            }
            if (DEBUG && this.mIsCoverModeFastResponseFlag) {
                Slog.i(TAG, "CoverModeBResponseTime=" + this.mData.coverModeAscendResponseTime + ",CoverModeDResponseTime=" + this.mData.coverModeDescendResponseTime);
            }
            if (DEBUG && this.mPowerStatus) {
                Slog.i(TAG, "PowerOnAT=500,PowerOnDT=1000");
            }
            this.mNeedToUpdateCCT = true;
            nextAscendTransition = nextAmbientCCTAscendingTransition(time);
            nextDescendTransition = nextAmbientCCTDescendingTransition(time);
        }
        if (nextDescendTransition >= nextAscendTransition) {
            nextDescendTransition = nextAscendTransition;
        }
        this.mNextTransitionTime = nextDescendTransition;
        this.mNextTransitionTime = this.mNextTransitionTime > time ? (this.mNextTransitionTime + ((long) this.mLightSensorRate)) + 100 : (((long) this.mLightSensorRate) + time) + 100;
        if (DEBUG && time - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "updateAmbientCCT: Scheduling ambient cct update for " + this.mNextTransitionTime + TimeUtils.formatUptime(this.mNextTransitionTime));
        }
    }

    public boolean needToUpdateCCT() {
        return this.mNeedToUpdateCCT;
    }

    public boolean cctUpdated() {
        this.mNeedToUpdateCCT = false;
        return false;
    }

    public boolean needToSendUpdateAmbientCCTMsg() {
        return this.mNextTransitionTime > 0;
    }

    public long getSendUpdateAmbientCCTMsgTime() {
        return this.mNextTransitionTime;
    }

    private long getNextAmbientCCTAscendingTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mData.coverModeAscendResponseTime + earliedtime;
        }
        if (this.mPowerStatus) {
            return 500 + earliedtime;
        }
        return this.mNormAscendDebounceTime + earliedtime;
    }

    private long getNextAmbientCCTDescendingTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mData.coverModeDescendResponseTime + earliedtime;
        }
        if (this.mPowerStatus) {
            return 1000 + earliedtime;
        }
        return this.mNormDescendDebounceTime + earliedtime;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.mPowerStatus = powerStatus;
    }

    public void clear() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "clear buffer data and algo flags");
            }
            this.mLastCloseScreenCCT = this.mAmbientCCT;
            if (DEBUG) {
                Slog.d(TAG, "LabcCoverMode clear: mLastCloseScreenCCT=" + this.mLastCloseScreenCCT);
            }
            this.mIsCoverModeFastResponseFlag = false;
            this.mAutoCCTIntervened = false;
            this.mAmbientCCTRingBuffer.clear();
            this.mAmbientCCTRingBufferFilter.clear();
        }
    }

    private void updateBuffer(long time, float ambientCCT, int horizon) {
        this.mAmbientCCTRingBufferFilter.push(time, ambientCCT);
        this.mAmbientCCTRingBufferFilter.prune(time - ((long) horizon));
    }

    private void updatepara(HwRingBuffer buffer, float cct) {
        this.mNormAscendDebounceTime = (long) this.mData.ascendDebounceTime;
        this.mNormDescendDebounceTime = (long) this.mData.descendDebounceTime;
        setDescendThresholdNew(cct);
        setAscendThresholdNew(cct);
    }

    private void setAscendThresholdNew(float amCCT) {
        this.mAscendDeltaCCTMax = calculateAscendThresholdDelta(amCCT);
    }

    private void setDescendThresholdNew(float amCCT) {
        this.mDescendDeltaCCTMax = calculateDescendThresholdDelta(amCCT);
    }

    private float calculateAscendThresholdDelta(float amCCT) {
        float ascendThreshold = 0.0f;
        PointF temp1 = null;
        for (PointF temp : this.mData.CCTAscendlinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amCCT < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amCCT - temp1.x)) + temp1.y;
                }
                if (!DEBUG) {
                    return 1.0f;
                }
                Slog.i(TAG, "Ascend_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return 1.0f;
            }
            temp1 = temp;
            ascendThreshold = temp.y;
        }
        return ascendThreshold;
    }

    private float calculateDescendThresholdDelta(float amCCT) {
        float descendThreshold = 0.0f;
        PointF temp1 = null;
        for (PointF temp : this.mData.CCTDescendlinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amCCT < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    float descendThresholdTmp = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amCCT - temp1.x)) + temp1.y;
                    return descendThresholdTmp > 1.0f ? descendThresholdTmp : 1.0f;
                } else if (!DEBUG) {
                    return 1.0f;
                } else {
                    Slog.i(TAG, "Descend_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    return 1.0f;
                }
            }
            temp1 = temp;
            descendThreshold = temp.y;
        }
        return descendThreshold;
    }

    private float prefilterAmbientCCT(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return prefilterMeanFilter(now);
        }
        if (filterMethodNum == 2) {
            return prefilterWeightedMeanFilter(now);
        }
        return prefilterNoFilter(now);
    }

    private float prefilterNoFilter(long now) {
        int N = this.mAmbientCCTRingBuffer.size();
        if (N != 0) {
            return this.mAmbientCCTRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "prefilterNoFilter: No ambient cct readings available, return 0");
        return 0.0f;
    }

    private float prefilterMeanFilter(long now) {
        int N = this.mAmbientCCTRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient cct readings available, return 0");
            return 0.0f;
        } else if (this.mData.preMeanFilterNum <= 0 || this.mData.preMeanFilterNoFilterNum < this.mData.preMeanFilterNum) {
            Slog.e(TAG, "prefilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.preMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum);
            return 0.0f;
        } else if (N <= this.mData.preMeanFilterNoFilterNum) {
            return this.mAmbientCCTRingBuffer.getLux(N - 1);
        } else {
            float sum = 0.0f;
            for (int i = N - 1; i >= N - this.mData.preMeanFilterNum; i--) {
                sum += this.mAmbientCCTRingBuffer.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.preMeanFilterNum));
        }
    }

    private float prefilterWeightedMeanFilter(long now) {
        int N = this.mAmbientCCTRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: No ambient cct readings available, return 0");
            return 0.0f;
        } else if (this.mData.preWeightedMeanFilterNum <= 0 || this.mData.preWeightedMeanFilterNoFilterNum < this.mData.preWeightedMeanFilterNum) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: ErrorPara, return 0, WeightedMeanFilterNum=" + this.mData.preWeightedMeanFilterNum + ",WeightedMeanFilterNoFilterNum=" + this.mData.preWeightedMeanFilterNoFilterNum);
            return 0.0f;
        } else {
            float tempCCT = this.mAmbientCCTRingBuffer.getLux(N - 1);
            if (N <= this.mData.preWeightedMeanFilterNoFilterNum) {
                return tempCCT;
            }
            int i;
            float maxCCT = 0.0f;
            float sum = 0.0f;
            float totalWeight = 0.0f;
            for (i = N - 1; i >= N - this.mData.preWeightedMeanFilterMaxFuncCCTNum; i--) {
                tempCCT = this.mAmbientCCTRingBuffer.getLux(i);
                if (tempCCT >= maxCCT) {
                    maxCCT = tempCCT;
                }
            }
            for (i = N - 1; i >= N - this.mData.preWeightedMeanFilterNum; i--) {
                float weight;
                if (this.mAmbientCCTRingBuffer.getLux(i) != 0.0f || maxCCT > this.mData.preWeightedMeanFilterCCTTh) {
                    weight = 1.0f;
                } else {
                    weight = this.mData.preWeightedMeanFilterAlpha * 1.0f;
                }
                totalWeight += weight;
                sum += this.mAmbientCCTRingBuffer.getLux(i) * weight;
            }
            return (float) Math.round(sum / totalWeight);
        }
    }

    private float getOrigLastAmbientCCT(long now) {
        int N = this.mAmbientCCTRingBuffer.size();
        if (N != 0) {
            return this.mAmbientCCTRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "OrigAmbient: No ambient cct readings available, return 0");
        return 0.0f;
    }

    private float getFilterLastAmbientCCT(long now) {
        int N = this.mAmbientCCTRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientCCTRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "FilterLastAmbient: No ambient cct readings available, return 0");
        return 0.0f;
    }

    private float postfilterAmbientCCT(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return postfilterMeanFilter(now);
        }
        if (filterMethodNum == 2) {
            return postfilterMaxMinAvgFilter(now);
        }
        return postfilterNoFilter(now);
    }

    private float postfilterNoFilter(long now) {
        int N = this.mAmbientCCTRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientCCTRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "postfilterNoFilter: No ambient cct readings available, return 0");
        return 0.0f;
    }

    private float postfilterMeanFilter(long now) {
        int N = this.mAmbientCCTRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient cct readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMeanFilterNum <= 0 || this.mData.postMeanFilterNoFilterNum < this.mData.postMeanFilterNum) {
            Slog.e(TAG, "postfilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.postMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.postMeanFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMeanFilterNoFilterNum) {
            return this.mAmbientCCTRingBufferFilter.getLux(N - 1);
        } else {
            float sum = 0.0f;
            for (int i = N - 1; i >= N - this.mData.postMeanFilterNum; i--) {
                sum += this.mAmbientCCTRingBufferFilter.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.postMeanFilterNum));
        }
    }

    private float postfilterMaxMinAvgFilter(long now) {
        int N = this.mAmbientCCTRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: No ambient cct readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMaxMinAvgFilterNum <= 0 || this.mData.postMaxMinAvgFilterNoFilterNum < this.mData.postMaxMinAvgFilterNum) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: ErrorPara, return 0, PostMaxMinAvgFilterNoFilterNum=" + this.mData.postMaxMinAvgFilterNoFilterNum + ",PostMaxMinAvgFilterNum=" + this.mData.postMaxMinAvgFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMaxMinAvgFilterNoFilterNum) {
            return this.mAmbientCCTRingBufferFilter.getLux(N - 1);
        } else {
            float sum = this.mAmbientCCTRingBufferFilter.getLux(N - 1);
            float cctMin = this.mAmbientCCTRingBufferFilter.getLux(N - 1);
            float cctMax = this.mAmbientCCTRingBufferFilter.getLux(N - 1);
            for (int i = N - 2; i >= N - this.mData.postMaxMinAvgFilterNum; i--) {
                if (cctMin > this.mAmbientCCTRingBufferFilter.getLux(i)) {
                    cctMin = this.mAmbientCCTRingBufferFilter.getLux(i);
                }
                if (cctMax < this.mAmbientCCTRingBufferFilter.getLux(i)) {
                    cctMax = this.mAmbientCCTRingBufferFilter.getLux(i);
                }
                sum += this.mAmbientCCTRingBufferFilter.getLux(i);
            }
            return ((sum - cctMin) - cctMax) / 3.0f;
        }
    }

    private long nextAmbientCCTAscendingTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientCCTRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean AscendChange;
            if (this.mAmbientCCTRingBufferFilter.getLux(i) - this.mAmbientCCTNewMax > this.mAscendDeltaCCTMax) {
                AscendChange = true;
            } else {
                AscendChange = false;
            }
            if (!AscendChange) {
                break;
            }
            earliestValidTime = this.mAmbientCCTRingBufferFilter.getTime(i);
        }
        return getNextAmbientCCTAscendingTime(earliestValidTime);
    }

    private long nextAmbientCCTDescendingTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientCCTRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DescendChange;
            if (this.mAmbientCCTNewMin - this.mAmbientCCTRingBufferFilter.getLux(i) >= this.mDescendDeltaCCTMax) {
                DescendChange = true;
            } else {
                DescendChange = false;
            }
            if (!DescendChange) {
                break;
            }
            earliestValidTime = this.mAmbientCCTRingBufferFilter.getTime(i);
        }
        return getNextAmbientCCTDescendingTime(earliestValidTime);
    }

    private boolean decideToAscend(float ambientCCT) {
        boolean needToAscend;
        if (ambientCCT - this.mAmbientCCTNewMax >= this.mAscendDeltaCCTMax) {
            needToAscend = true;
        } else {
            needToAscend = false;
        }
        return needToAscend ? this.mAutoCCTIntervened ^ 1 : false;
    }

    private boolean decideToDescend(float ambientCCT) {
        boolean needToDescend;
        if (this.mAmbientCCTNewMin - ambientCCT >= this.mDescendDeltaCCTMax) {
            needToDescend = true;
        } else {
            needToDescend = false;
        }
        return needToDescend ? this.mAutoCCTIntervened ^ 1 : false;
    }

    public boolean reportValueWhenSensorOnChange() {
        return this.mReportValueWhenSensorOnChange;
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
}
