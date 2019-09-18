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

        private BaseHolder() {
        }

        public void updateSrcValue(long[] srcValues, int srcValueCount) {
            this.mSrcValues = srcValues;
            this.mSrcValueCount = srcValueCount;
        }
    }

    public static class CallbackData {
        public int mDValueState;
        /* access modifiers changed from: private */
        public int mDValueZeroSum;
        public long[] mDValues;

        public void update(int dValueState, long[] dValues, int dValueCount) {
            this.mDValueState = dValueState;
            this.mDValues = Arrays.copyOf(dValues, dValueCount);
        }

        public boolean isIncreased() {
            int riseMiddle = 1;
            if ((this.mDValueState & 256) != 0) {
                return true;
            }
            int riseFirst = (this.mDValueState & 4) != 0 ? 1 : 0;
            int riseLast = (this.mDValueState & 8) != 0 ? 1 : 0;
            if ((this.mDValueState & 16) == 0) {
                riseMiddle = 0;
            }
            return estimated(riseFirst, riseMiddle, riseLast);
        }

        private boolean estimated(int riseFirst, int riseMiddle, int riseLast) {
            int sum = riseFirst + riseLast + riseMiddle;
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
                if (sum > 1 && (2 == riseFirst + riseLast || 2 == riseMiddle + riseLast)) {
                    return true;
                }
                if (sum > 1 && this.mDValueZeroSum > 30) {
                    return true;
                }
            }
            return false;
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

        public /* bridge */ /* synthetic */ void updateSrcValue(long[] jArr, int i) {
            super.updateSrcValue(jArr, i);
        }

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
        MemRepairHolder memRepairHolder = srcData;
        MRCallback mRCallback = callback;
        if (memRepairHolder == null || mRCallback == null) {
            Object obj = cbUser;
            AwareLog.e(TAG, "invalid parameter");
            return 4;
        } else if (!srcData.isValid()) {
            return 4;
        } else {
            CallbackData cbData = new CallbackData();
            int result = 5;
            int i = 1;
            long[] dValues = new long[(memRepairHolder.mSrcValueCount - 1)];
            long[] zValues = new long[memRepairHolder.mSrcValueCount];
            int mergesCount = (memRepairHolder.mMaxZoneCount - memRepairHolder.mMinZoneCount) + 1;
            int curZoneCount = memRepairHolder.mMaxZoneCount;
            int prevMergeSize = 0;
            int mergeIdx = 0;
            while (true) {
                if (mergeIdx >= mergesCount) {
                    break;
                }
                int mergeSize = memRepairHolder.mSrcValueCount / curZoneCount;
                curZoneCount--;
                if (prevMergeSize != mergeSize) {
                    prevMergeSize = mergeSize;
                    if (mergeSize < i || memRepairHolder.mSrcValueCount / mergeSize < memRepairHolder.mMinZoneCount) {
                        break;
                    }
                    int dValueCount = getAndUpdateDValues(zValues, getAndUpdateZValues(memRepairHolder, mergeSize, zValues), dValues);
                    if (estimateSumResult(dValues, dValueCount) != 2) {
                        cbData.update(estimateDValue(cbData, memRepairHolder, dValues, dValueCount), dValues, dValueCount);
                        result = mRCallback.estimateLinear(cbUser, cbData);
                        if (result != i) {
                            if (result != 3) {
                                break;
                            }
                        } else {
                            return i;
                        }
                    } else {
                        return 2;
                    }
                } else {
                    Object obj2 = cbUser;
                }
                mergeIdx++;
                i = 1;
            }
            Object obj3 = cbUser;
            return result;
        }
    }

    private static int getAndUpdateZValues(MemRepairHolder srcData, int mergeSize, long[] zValues) {
        Arrays.fill(zValues, 0);
        int sum = 0;
        int sum2 = 0;
        for (int j = 0; j < srcData.mSrcValueCount; j++) {
            sum2 = (int) (((long) sum2) + srcData.mSrcValues[j]);
            if ((j + 1) % mergeSize == 0) {
                zValues[sum] = (long) (sum2 / mergeSize);
                sum2 = 0;
                sum++;
            }
        }
        if (sum2 > 0 && sum > 0) {
            zValues[sum - 1] = ((zValues[sum - 1] * ((long) mergeSize)) + ((long) sum2)) / ((long) (mergeSize + (srcData.mSrcValueCount % mergeSize)));
        }
        AwareLog.d(TAG, "zValueCount=" + sum + ",zValues=" + Arrays.toString(zValues));
        return sum;
    }

    private static int getAndUpdateDValues(long[] zValues, int zValueCount, long[] dValues) {
        Arrays.fill(dValues, 0);
        int dValueCount = 0;
        long prevValue = zValues[0];
        int j = 1;
        while (j < zValueCount) {
            dValues[dValueCount] = zValues[j] - prevValue;
            prevValue = zValues[j];
            j++;
            dValueCount++;
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
            int unused = cbData.mDValueZeroSum = getAndUpdateDZValue(dValues, dValueCount, diffs, srcData.mMinIncDValue);
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
                int lastRiseCount = 0;
                int firstRiseCount = firstRiseIdx > -1 ? firstRiseIdx + 1 : 0;
                if (lastRiseIdx > 0) {
                    lastRiseCount = dValueCount - lastRiseIdx;
                }
                int riseCount = firstRiseCount + middleRiseCount + lastRiseCount;
                if (riseCount * 3 > dValueCount * 2) {
                    state |= 128;
                } else if (riseCount * 2 > dValueCount) {
                    state |= 64;
                } else if (riseCount * 3 > dValueCount) {
                    state |= 32;
                }
                return state | 1;
            }
            int state2 = state | 256;
            AwareLog.d(TAG, "DVALUE_RISE_ALL");
            return state2;
        }
    }

    private static int checkNegativeState(int floatPercent, long[] dValues, int dValueCount) {
        int sumNegative = 0;
        int sumPositive = 0;
        int negaCount = 0;
        for (int i = 0; i < dValueCount; i++) {
            negaCount += dValues[i] < 0 ? 1 : 0;
            if (dValues[i] > 0) {
                sumPositive = (int) (((long) sumPositive) + dValues[i]);
            } else {
                sumNegative = (int) (((long) sumNegative) + dValues[i]);
            }
        }
        int percentage = 100;
        if (sumPositive > 0) {
            percentage = ((-sumNegative) * 100) / sumPositive;
        }
        if (percentage > floatPercent || negaCount * 3 > dValueCount) {
            return 2;
        }
        return 0;
    }

    private static int getAndUpdateDZValue(long[] dValues, int dValueCount, int[] dzValues, int minIncDValue) {
        Arrays.fill(dzValues, 0);
        int zeroSum = 0;
        for (int i = 0; i < dValueCount; i++) {
            dzValues[i] = dValues[i] < 0 ? -1 : (int) (dValues[i] / ((long) minIncDValue));
            zeroSum += dzValues[i] > 0 ? dzValues[i] : 0;
        }
        return zeroSum;
    }

    private static int getFirstRiseIndex(int[] diffs, int dValueCount) {
        int firstRiseIdx = 0;
        if (diffs[0] <= 0) {
            firstRiseIdx = -1;
        }
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
        int firstRiseIdx2 = (firstRiseIdx < 0 || firstRiseIdx >= dValueCount) ? -1 : firstRiseIdx;
        int lastRiseIdx2 = (lastRiseIdx < 0 || lastRiseIdx >= dValueCount) ? dValueCount - 1 : lastRiseIdx;
        for (int index = firstRiseIdx2 + 1; index < lastRiseIdx2; index++) {
            if (diffs[index] > 0) {
                middleRiseCount++;
            }
        }
        return middleRiseCount;
    }
}
