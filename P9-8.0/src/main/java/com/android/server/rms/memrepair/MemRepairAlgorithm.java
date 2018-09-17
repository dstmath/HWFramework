package com.android.server.rms.memrepair;

import android.rms.iaware.AwareLog;
import java.util.Arrays;

public class MemRepairAlgorithm {
    public static final int DVALUE_RISE_ALL = 256;
    public static final int DVALUE_RISE_EXCEED_HALF = 64;
    public static final int DVALUE_RISE_EXCEED_ONETHIRD = 32;
    public static final int DVALUE_RISE_EXCEED_TWOTHIRD = 128;
    public static final int DVALUE_RISE_FIRST = 4;
    public static final int DVALUE_RISE_LAST = 8;
    public static final int DVALUE_RISE_MIDDLE = 16;
    public static final int DVALUE_STATE_NEGATIVE = 2;
    public static final int DVALUE_STATE_NONE = 0;
    public static final int DVALUE_STATE_POSITIVE = 1;
    public static final int ESTIMATE_CONTINUE = 3;
    public static final int ESTIMATE_DECREASE = 2;
    public static final int ESTIMATE_FAIL = 5;
    public static final int ESTIMATE_INCREASE = 1;
    public static final int ESTIMATE_OK = 6;
    public static final int ESTIMATE_PARAMS = 4;
    private static final int MAX_DVALUE_FLOAT_PERCENT = 30;
    private static final int MIN_DVALUE_FLOAT_PERCENT = 1;
    private static final int MIN_DVALUE_ZERO_SUM = 10;
    private static final String TAG = "AwareMem_MRAlgo";

    private static class BaseHolder {
        int mSrcValueCount;
        long[] mSrcValues;

        /* synthetic */ BaseHolder(BaseHolder -this0) {
            this();
        }

        private BaseHolder() {
        }

        public void updateSrcValue(long[] srcValues, int srcValueCount) {
            this.mSrcValues = srcValues;
            this.mSrcValueCount = srcValueCount;
        }
    }

    public static class CallbackData {
        public int mDValueState;
        private int mDValueZeroSum;
        public long[] mDValues;

        public void update(int dValueState, long[] dValues, int dValueCount) {
            this.mDValueState = dValueState;
            this.mDValues = Arrays.copyOf(dValues, dValueCount);
        }

        public boolean isIncreased() {
            if ((this.mDValueState & 256) != 0) {
                return true;
            }
            return estimated((this.mDValueState & 4) != 0 ? 1 : 0, (this.mDValueState & 16) != 0 ? 1 : 0, (this.mDValueState & 8) != 0 ? 1 : 0);
        }

        private boolean estimated(int riseFirst, int riseMiddle, int riseLast) {
            int sum = (riseFirst + riseLast) + riseMiddle;
            if ((this.mDValueState & 128) != 0 && sum > 0) {
                return true;
            }
            if ((this.mDValueState & 64) != 0) {
                if (sum > 1) {
                    return true;
                }
                if (sum > 0 && this.mDValueZeroSum > 20) {
                    return true;
                }
            }
            if ((this.mDValueState & 32) != 0) {
                if (sum > 2) {
                    return true;
                }
                if (sum <= 1 || (2 != riseFirst + riseLast && 2 != riseMiddle + riseLast)) {
                    return sum > 1 && this.mDValueZeroSum > 30;
                } else {
                    return true;
                }
            }
        }
    }

    public interface MRCallback {
        int estimateLinear(Object obj, CallbackData callbackData);
    }

    public static class MemRepairHolder extends BaseHolder {
        int mFloatPercent;
        int mMaxZoneCount;
        int mMinIncDValue;
        int mMinZoneCount;

        private MemRepairHolder() {
            super();
        }

        public MemRepairHolder(int minIncDValue, int minZoneCount, int maxZoneCount) {
            super();
            this.mMinIncDValue = minIncDValue;
            this.mMinZoneCount = minZoneCount;
            this.mMaxZoneCount = maxZoneCount;
        }

        public void updateCollectCount(int minZoneCount, int maxZoneCount) {
            this.mMinZoneCount = minZoneCount;
            this.mMaxZoneCount = maxZoneCount;
        }

        public void updateFloatPercent(int floatPercent) {
            this.mFloatPercent = floatPercent;
        }

        public boolean isValid() {
            if (this.mSrcValues == null || this.mSrcValueCount < 1 || this.mSrcValueCount > this.mSrcValues.length) {
                AwareLog.e(MemRepairAlgorithm.TAG, "invalid member");
                return false;
            } else if (this.mMinZoneCount < 1 || this.mMinZoneCount >= this.mMaxZoneCount) {
                AwareLog.e(MemRepairAlgorithm.TAG, "invalid min/max zone count");
                return false;
            } else if (this.mFloatPercent >= 1 && this.mFloatPercent <= 30) {
                return true;
            } else {
                AwareLog.e(MemRepairAlgorithm.TAG, "invalid percent");
                return false;
            }
        }
    }

    public static int translateMemRepair(MemRepairHolder srcData, MRCallback callback, Object cbUser) {
        if (srcData == null || callback == null) {
            AwareLog.e(TAG, "invalid parameter");
            return 4;
        } else if (!srcData.isValid()) {
            return 4;
        } else {
            CallbackData cbData = new CallbackData();
            int result = 5;
            long[] dValues = new long[(srcData.mSrcValueCount - 1)];
            long[] zValues = new long[srcData.mSrcValueCount];
            int mergesCount = (srcData.mMaxZoneCount - srcData.mMinZoneCount) + 1;
            int curZoneCount = srcData.mMaxZoneCount;
            int prevMergeSize = 0;
            for (int mergeIdx = 0; mergeIdx < mergesCount; mergeIdx++) {
                int mergeSize = srcData.mSrcValueCount / curZoneCount;
                curZoneCount--;
                if (prevMergeSize != mergeSize) {
                    prevMergeSize = mergeSize;
                    if (mergeSize < 1 || srcData.mSrcValueCount / mergeSize < srcData.mMinZoneCount) {
                        break;
                    }
                    int dValueCount = getAndUpdateDValues(zValues, getAndUpdateZValues(srcData, mergeSize, zValues), dValues);
                    if (estimateSumResult(dValues, dValueCount) != 2) {
                        cbData.update(estimateDValue(cbData, srcData, dValues, dValueCount), dValues, dValueCount);
                        result = callback.estimateLinear(cbUser, cbData);
                        if (result != 1) {
                            if (result != 3) {
                                break;
                            }
                        } else {
                            return 1;
                        }
                    }
                    return 2;
                }
            }
            return result;
        }
    }

    private static int getAndUpdateZValues(MemRepairHolder srcData, int mergeSize, long[] zValues) {
        Arrays.fill(zValues, 0);
        int zValueCount = 0;
        int sum = 0;
        for (int j = 0; j < srcData.mSrcValueCount; j++) {
            sum = (int) (((long) sum) + srcData.mSrcValues[j]);
            if ((j + 1) % mergeSize == 0) {
                int zValueCount2 = zValueCount + 1;
                zValues[zValueCount] = (long) (sum / mergeSize);
                sum = 0;
                zValueCount = zValueCount2;
            }
        }
        if (sum > 0 && zValueCount > 0) {
            zValues[zValueCount - 1] = ((zValues[zValueCount - 1] * ((long) mergeSize)) + ((long) sum)) / ((long) (mergeSize + (srcData.mSrcValueCount % mergeSize)));
        }
        AwareLog.d(TAG, "zValueCount=" + zValueCount + ",zValues=" + Arrays.toString(zValues));
        return zValueCount;
    }

    private static int getAndUpdateDValues(long[] zValues, int zValueCount, long[] dValues) {
        Arrays.fill(dValues, 0);
        long prevValue = zValues[0];
        int j = 1;
        int dValueCount = 0;
        while (j < zValueCount) {
            int dValueCount2 = dValueCount + 1;
            dValues[dValueCount] = zValues[j] - prevValue;
            prevValue = zValues[j];
            j++;
            dValueCount = dValueCount2;
        }
        return dValueCount;
    }

    private static int estimateSumResult(long[] dValues, int dValueCount) {
        int sum = 0;
        for (int i = 0; i < dValueCount; i++) {
            sum = (int) (((long) sum) + dValues[i]);
        }
        if (sum > 0) {
            return 6;
        }
        AwareLog.d(TAG, "dValues sum=" + sum + ", decreased");
        return 2;
    }

    private static int estimateDValue(CallbackData cbData, MemRepairHolder srcData, long[] dValues, int dValueCount) {
        int state = checkNegativeState(srcData.mFloatPercent, dValues, dValueCount);
        if ((state & 2) != 0) {
            AwareLog.d(TAG, "DVALUE_HAVE_NEGATIVE");
            return state;
        } else if (srcData.mMinIncDValue <= 0) {
            return state | 1;
        } else {
            int[] diffs = new int[dValueCount];
            cbData.mDValueZeroSum = getAndUpdateDZValue(dValues, dValueCount, diffs, srcData.mMinIncDValue);
            int firstRiseIdx = getFirstRiseIndex(diffs, dValueCount);
            if (firstRiseIdx <= -1 || firstRiseIdx != dValueCount - 1) {
                if (firstRiseIdx > -1) {
                    state |= 4;
                    AwareLog.d(TAG, "DVALUE_RISE_FIRST_ONLY index=" + firstRiseIdx);
                }
                int lastRiseIdx = getLastRiseIndex(diffs, dValueCount);
                if (lastRiseIdx > -1) {
                    state |= 8;
                    AwareLog.d(TAG, "DVALUE_RISE_LAST_ONLY index=" + lastRiseIdx);
                }
                int middleRiseCount = getMiddleRiseCount(diffs, dValueCount, firstRiseIdx, lastRiseIdx);
                if (middleRiseCount > 0) {
                    state |= 16;
                    AwareLog.d(TAG, "DVALUE_RISE_MIDDLE count=" + middleRiseCount);
                }
                int riseCount = ((firstRiseIdx > -1 ? firstRiseIdx + 1 : 0) + middleRiseCount) + (lastRiseIdx > 0 ? dValueCount - lastRiseIdx : 0);
                if (riseCount * 3 > dValueCount * 2) {
                    state |= 128;
                } else if (riseCount * 2 > dValueCount) {
                    state |= 64;
                } else if (riseCount * 3 > dValueCount) {
                    state |= 32;
                }
                return state | 1;
            }
            state |= 256;
            AwareLog.d(TAG, "DVALUE_RISE_ALL");
            return state;
        }
    }

    private static int checkNegativeState(int floatPercent, long[] dValues, int dValueCount) {
        int negaCount = 0;
        int sumPositive = 0;
        for (int i = 0; i < dValueCount; i++) {
            long j;
            long j2;
            negaCount += dValues[i] < 0 ? 1 : 0;
            if (dValues[i] > 0) {
                j = (long) sumPositive;
                j2 = dValues[i];
            } else {
                j = (long) null;
                j2 = dValues[i];
            }
            sumPositive = (int) (j + j2);
        }
        if ((sumPositive > 0 ? ((-null) * 100) / sumPositive : 100) > floatPercent || negaCount * 3 > dValueCount) {
            return 2;
        }
        return 0;
    }

    private static int getAndUpdateDZValue(long[] dValues, int dValueCount, int[] dzValues, int minIncDValue) {
        int zeroSum = 0;
        Arrays.fill(dzValues, 0);
        for (int i = 0; i < dValueCount; i++) {
            int i2;
            dzValues[i] = dValues[i] < 0 ? -1 : (int) (dValues[i] / ((long) minIncDValue));
            if (dzValues[i] > 0) {
                i2 = dzValues[i];
            } else {
                i2 = 0;
            }
            zeroSum += i2;
        }
        return zeroSum;
    }

    private static int getFirstRiseIndex(int[] diffs, int dValueCount) {
        int firstRiseIdx = diffs[0] > 0 ? 0 : -1;
        int index = 1;
        while (index < dValueCount && diffs[index] > 0 && firstRiseIdx == index - 1) {
            firstRiseIdx = index;
            index++;
        }
        return firstRiseIdx;
    }

    private static int getLastRiseIndex(int[] diffs, int dValueCount) {
        int lastRiseIdx = diffs[dValueCount + -1] > 0 ? dValueCount - 1 : -1;
        int index = dValueCount - 2;
        while (index >= 0 && diffs[index] > 0 && lastRiseIdx == index + 1) {
            lastRiseIdx = index;
            index--;
        }
        return lastRiseIdx;
    }

    private static int getMiddleRiseCount(int[] diffs, int dValueCount, int firstRiseIdx, int lastRiseIdx) {
        int middleRiseCount = 0;
        if (firstRiseIdx < 0 || firstRiseIdx >= dValueCount) {
            firstRiseIdx = -1;
        }
        if (lastRiseIdx < 0 || lastRiseIdx >= dValueCount) {
            lastRiseIdx = dValueCount - 1;
        }
        for (int index = firstRiseIdx + 1; index < lastRiseIdx; index++) {
            if (diffs[index] > 0) {
                middleRiseCount++;
            }
        }
        return middleRiseCount;
    }
}
