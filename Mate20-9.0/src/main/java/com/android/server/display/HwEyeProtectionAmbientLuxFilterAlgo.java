package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwEyeProtectionXmlLoader;

public class HwEyeProtectionAmbientLuxFilterAlgo {
    private static final float AMBIENT_LUX_INVALID = 0.0f;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int FILTER_TYPE_AVERAGE = 1;
    private static final int FILTER_TYPE_MAX_MIN_AVERAGE = 3;
    private static final int FILTER_TYPE_NONE = 0;
    private static final int FILTER_TYPE_WEIGHT_AVERAGE = 2;
    private static final int INTIAL_RING_BUFFER_SIZE = 50;
    private static final long MAX_RECORD_TIME = 20000;
    private static final String TAG = "HwEyeProtectionAmbientLuxFilterAlgo";
    private HwRingBuffer mAmbientLightRingBuffer = new HwRingBuffer(50);
    private float mAmbientLux = 0.0f;
    private float mBrightenDeltaLuxMax;
    private float mDarkenDeltaLuxMax;
    HwEyeProtectionXmlLoader.Data mData;
    private HwRingBuffer mFilteredAmbientLightRingBuffer = new HwRingBuffer(50);
    private boolean mFirstAmbientLux = true;
    private float mLastFilterLux = 0.0f;
    private final Object mLock = new Object();
    private boolean mNeedToBrighten = false;
    private boolean mNeedToDarken = false;
    private long mNormBrightenDebounceTime;
    private long mNormDarkenDebounceTime;

    public HwEyeProtectionAmbientLuxFilterAlgo(HwEyeProtectionXmlLoader.Data filterParameters) {
        if (filterParameters == null) {
            this.mData = new HwEyeProtectionXmlLoader.Data();
        } else {
            this.mData = filterParameters;
        }
    }

    public float updateAmbientLux(float ambientLux) {
        float retLux;
        synchronized (this.mLock) {
            retLux = 0.0f;
            long currentTime = SystemClock.uptimeMillis();
            if (ambientLux > 40000.0f) {
                ambientLux = 40000.0f;
                try {
                    if (DEBUG) {
                        Slog.e(TAG, "ambientLux exceeds limitation.");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Slog.e(TAG, "updateAmbientLux: " + e);
                } catch (Exception e2) {
                    Slog.e(TAG, "updateAmbientLux: " + e2);
                }
            }
            updateAmbientLuxBuffer(currentTime, ambientLux, 20000);
            retLux = updateAmbientLux(ambientLux, currentTime);
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux:currentTime=" + currentTime + " ambientLux = " + ambientLux + " retLux = " + retLux);
            }
        }
        return retLux;
    }

    private float updateAmbientLux(float lux, long currentTime) {
        long j = currentTime;
        float filterLux = filterAmbientLux(j, this.mData.preMethodNum);
        boolean z = false;
        if (this.mFirstAmbientLux) {
            this.mAmbientLux = (float) Math.round(lux);
            this.mFirstAmbientLux = false;
        }
        float latestLux = getOriginLatestAmbientLux(j);
        updateFilteredAmbientLuxBuffer(j, filterLux, 20000);
        this.mLastFilterLux = getFilterLastAmbientLux(j);
        float ambientLux = filterCurrentAmbientLux(j, this.mData.postMethodNum);
        updateParameters(this.mAmbientLightRingBuffer, this.mAmbientLux);
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(j);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(j);
        boolean needToBrighten = decideToBrighten(ambientLux);
        boolean needToBrightenNew = decideToBrighten(latestLux);
        boolean needToDarken = decideToDarken(ambientLux);
        boolean needToDarkenNew = decideToDarken(latestLux);
        boolean z2 = true;
        if (needToBrighten && needToBrightenNew && nextBrightenTransition <= j) {
            z = true;
        }
        this.mNeedToBrighten = z;
        if (!needToDarken || !needToDarkenNew || nextDarkenTransition > j) {
            z2 = false;
        }
        this.mNeedToDarken = z2;
        if (this.mNeedToBrighten || this.mNeedToDarken) {
            this.mAmbientLux = (float) Math.round(lux);
            StringBuilder sb = new StringBuilder();
            long j2 = nextBrightenTransition;
            sb.append("mNeedToBrighten = ");
            sb.append(this.mNeedToBrighten);
            sb.append("mNeedToDarken = ");
            sb.append(this.mNeedToDarken);
            Slog.d(TAG, sb.toString());
        } else {
            long j3 = nextBrightenTransition;
        }
        return ambientLux;
    }

    private void updateAmbientLuxBuffer(long currentTime, float ambientLux, long maxRecordTime) {
        this.mAmbientLightRingBuffer.push(currentTime, ambientLux);
        this.mAmbientLightRingBuffer.prune(currentTime - maxRecordTime);
    }

    private void updateFilteredAmbientLuxBuffer(long currentTime, float ambientLux, long elapsedRecordTime) {
        this.mFilteredAmbientLightRingBuffer.push(currentTime, ambientLux);
        this.mFilteredAmbientLightRingBuffer.prune(currentTime - elapsedRecordTime);
    }

    private float filterAmbientLux(long currentTime, int filterType) {
        switch (filterType) {
            case 0:
                return prefilterNoneFilter(currentTime);
            case 1:
                return prefilterAverageFilter(currentTime);
            case 2:
                return prefilterWeightedAverageFilter(currentTime);
            default:
                return prefilterNoneFilter(currentTime);
        }
    }

    private float prefilterNoneFilter(long currentTime) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize != 0) {
            return this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
        }
        Slog.e(TAG, "prefilterNoFilter: No ambient light readings available, return 0");
        return 0.0f;
    }

    private float prefilterAverageFilter(long currentTime) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.preMeanFilterNum <= 0 || this.mData.preMeanFilterNoFilterNum < this.mData.preMeanFilterNum) {
            Slog.e(TAG, "prefilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.preMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum);
            return 0.0f;
        } else if (bufferSize <= this.mData.preMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
        } else {
            float sum = 0.0f;
            for (int i = bufferSize - 1; i >= bufferSize - this.mData.preMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.preMeanFilterNum));
        }
    }

    private float prefilterWeightedAverageFilter(long currentTime) {
        float weight;
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize == 0) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.preWeightedMeanFilterNum <= 0 || this.mData.preWeightedMeanFilterNoFilterNum < this.mData.preWeightedMeanFilterNum) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: ErrorPara, return 0, WeightedMeanFilterNum=" + this.mData.preWeightedMeanFilterNum + ",WeightedMeanFilterNoFilterNum=" + this.mData.preWeightedMeanFilterNoFilterNum);
            return 0.0f;
        } else {
            float tempLux = this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
            if (bufferSize <= this.mData.preWeightedMeanFilterNoFilterNum) {
                return tempLux;
            }
            float maxLux = 0.0f;
            float sum = 0.0f;
            float totalWeight = 0.0f;
            for (int i = bufferSize - 1; i >= bufferSize - this.mData.preWeightedMeanFilterMaxFuncLuxNum; i--) {
                float tempLux2 = this.mAmbientLightRingBuffer.getLux(i);
                if (tempLux2 >= maxLux) {
                    maxLux = tempLux2;
                }
            }
            for (int i2 = bufferSize - 1; i2 >= bufferSize - this.mData.preWeightedMeanFilterNum; i2--) {
                if (this.mAmbientLightRingBuffer.getLux(i2) != 0.0f || maxLux > this.mData.preWeightedMeanFilterLuxTh) {
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

    private float filterCurrentAmbientLux(long currentTime, int filterType) {
        if (filterType == 3) {
            return obtainCurrentAmbientLuxFromMaxMinAverageFilter(currentTime);
        }
        switch (filterType) {
            case 0:
                return obtainCurrentAmbientLuxFromNoneFilter(currentTime);
            case 1:
                return obtainCurrentAmbientLuxFromAverageFilter(currentTime);
            default:
                return obtainCurrentAmbientLuxFromNoneFilter(currentTime);
        }
    }

    private float obtainCurrentAmbientLuxFromNoneFilter(long currentTime) {
        int N = this.mFilteredAmbientLightRingBuffer.size();
        if (N != 0) {
            return this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "postfilterNoFilter: No ambient light readings available, return 0");
        return 0.0f;
    }

    private float obtainCurrentAmbientLuxFromAverageFilter(long currentTime) {
        int N = this.mFilteredAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMeanFilterNum <= 0 || this.mData.postMeanFilterNoFilterNum < this.mData.postMeanFilterNum) {
            Slog.e(TAG, "postfilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.postMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.postMeanFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMeanFilterNoFilterNum) {
            return this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = 0.0f;
            for (int i = N - 1; i >= N - this.mData.postMeanFilterNum; i--) {
                sum += this.mFilteredAmbientLightRingBuffer.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.postMeanFilterNum));
        }
    }

    private float obtainCurrentAmbientLuxFromMaxMinAverageFilter(long currentTime) {
        int N = this.mFilteredAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMaxMinAvgFilterNum <= 0 || this.mData.postMaxMinAvgFilterNoFilterNum < this.mData.postMaxMinAvgFilterNum) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: ErrorPara, return 0, PostMaxMinAvgFilterNoFilterNum=" + this.mData.postMaxMinAvgFilterNoFilterNum + ",PostMaxMinAvgFilterNum=" + this.mData.postMaxMinAvgFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMaxMinAvgFilterNoFilterNum) {
            return this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
            float luxMin = this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
            float luxMax = this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
            for (int i = N - 2; i >= N - this.mData.postMaxMinAvgFilterNum; i--) {
                if (luxMin > this.mFilteredAmbientLightRingBuffer.getLux(i)) {
                    luxMin = this.mFilteredAmbientLightRingBuffer.getLux(i);
                }
                if (luxMax < this.mFilteredAmbientLightRingBuffer.getLux(i)) {
                    luxMax = this.mFilteredAmbientLightRingBuffer.getLux(i);
                }
                sum += this.mFilteredAmbientLightRingBuffer.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / 3.0f;
        }
    }

    private void updateParameters(HwRingBuffer buffer, float lux) {
        float luxInteger = (float) Math.round(lux);
        if (luxInteger >= this.mData.brightTimeDelayLuxThreshold || this.mLastFilterLux >= this.mData.brightTimeDelayLuxThreshold || !this.mData.brightTimeDelayEnable) {
            this.mNormBrightenDebounceTime = (long) this.mData.brightenDebounceTime;
        } else {
            this.mNormBrightenDebounceTime = (long) this.mData.brightTimeDelay;
        }
        if (luxInteger >= this.mData.darkTimeDelayLuxThreshold || !this.mData.darkTimeDelayEnable) {
            this.mNormDarkenDebounceTime = (long) this.mData.darkenDebounceTime;
        } else {
            float ambientLuxDarkenDelta = calculateDarkenThresholdDelta(this.mAmbientLux);
            float currentAmbientLux = buffer.getLux(buffer.size() - 1);
            float luxNormalizedFactor = (this.mData.darkTimeDelayBeta2 * (this.mAmbientLux - currentAmbientLux)) + (this.mData.darkTimeDelayBeta1 * ((this.mAmbientLux - currentAmbientLux) - ambientLuxDarkenDelta)) + 1.0f;
            if (luxNormalizedFactor < 0.001f) {
                this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkTimeDelay) + this.mData.darkTimeDelayBeta0);
            } else {
                this.mNormDarkenDebounceTime = ((long) this.mData.darkTimeDelay) + ((long) ((this.mData.darkTimeDelayBeta0 * (1.0f + (this.mData.darkTimeDelayBeta2 * 1.0f))) / luxNormalizedFactor));
            }
        }
        setDarkenThresholdNew(this.mAmbientLux);
        setBrightenThresholdNew(this.mAmbientLux);
    }

    private float getOriginLatestAmbientLux(long currentTime) {
        int bufferSize = this.mAmbientLightRingBuffer.size();
        if (bufferSize != 0) {
            return this.mAmbientLightRingBuffer.getLux(bufferSize - 1);
        }
        Slog.e(TAG, "getOriginLatestAmbientLux: No ambient light readings available, return 0");
        return 0.0f;
    }

    private void setBrightenThresholdNew(float amLux) {
        this.mBrightenDeltaLuxMax = calculateBrightenThresholdDelta(amLux);
    }

    private void setDarkenThresholdNew(float amLux) {
        this.mDarkenDeltaLuxMax = calculateDarkenThresholdDelta(amLux);
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
                if (temp2.x > temp1.x) {
                    return (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
                if (!DEBUG) {
                    return 1.0f;
                }
                Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return 1.0f;
            }
            temp1 = temp;
            brightenThreshold = temp1.y;
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
                if (temp2.x > temp1.x) {
                    float darkenThresholdTmp = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                    float darkenThreshold2 = 1.0f;
                    if (darkenThresholdTmp > 1.0f) {
                        darkenThreshold2 = darkenThresholdTmp;
                    }
                    return darkenThreshold2;
                } else if (!DEBUG) {
                    return 1.0f;
                } else {
                    Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    return 1.0f;
                }
            } else {
                temp1 = temp;
                darkenThreshold = temp1.y;
            }
        }
        return darkenThreshold;
    }

    private long nextAmbientLightBrighteningTransition(long time) {
        boolean BrightenChange;
        long earliestValidTime = time;
        for (int i = this.mFilteredAmbientLightRingBuffer.size() - 1; i >= 0; i--) {
            if (this.mFilteredAmbientLightRingBuffer.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mFilteredAmbientLightRingBuffer.getTime(i);
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        boolean DarkenChange;
        long earliestValidTime = time;
        for (int i = this.mFilteredAmbientLightRingBuffer.size() - 1; i >= 0; i--) {
            if (this.mAmbientLux - this.mFilteredAmbientLightRingBuffer.getLux(i) >= this.mDarkenDeltaLuxMax) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mFilteredAmbientLightRingBuffer.getTime(i);
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    private long getNextAmbientLightBrighteningTime(long earliedtime) {
        return this.mNormBrightenDebounceTime + earliedtime;
    }

    private long getNextAmbientLightDarkeningTime(long earliedtime) {
        return this.mNormDarkenDebounceTime + earliedtime;
    }

    private boolean decideToBrighten(float ambientLux) {
        if (ambientLux - this.mAmbientLux >= this.mBrightenDeltaLuxMax) {
            return true;
        }
        return false;
    }

    private boolean decideToDarken(float ambientLux) {
        if (this.mAmbientLux - ambientLux >= this.mDarkenDeltaLuxMax) {
            return true;
        }
        return false;
    }

    public boolean getNeedToBrighten() {
        return this.mNeedToBrighten;
    }

    public boolean getNeedToDarken() {
        return this.mNeedToDarken;
    }

    private float getFilterLastAmbientLux(long now) {
        int N = this.mFilteredAmbientLightRingBuffer.size();
        if (N != 0) {
            return this.mFilteredAmbientLightRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "FilterLastAmbient: No ambient light readings available, return 0");
        return 0.0f;
    }

    public void clear() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "clear buffer data");
            }
            this.mAmbientLightRingBuffer.clear();
            this.mFilteredAmbientLightRingBuffer.clear();
            this.mFirstAmbientLux = true;
        }
    }
}
