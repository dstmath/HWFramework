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
    private static final int MIN_DVALUE_ZERO_SUM = 10;
    private static final String TAG = "AwareMem_MRAlgo";

    public interface EstimateCallback {
        int estimateLinear(Object obj, CallbackData callbackData);
    }

    /* access modifiers changed from: private */
    public static class BaseHolder {
        int mSrcValueCount;
        long[] mSrcValues;

        private BaseHolder() {
        }

        public void updateSrcValue(long[] srcValues, int srcValueCount) {
            this.mSrcValues = srcValues;
            this.mSrcValueCount = srcValueCount;
        }
    }

    public static class MemRepairHolder extends BaseHolder {
        int mFloatPercent;
        int mMaxZoneCount;
        int mMinIncDvalue;
        int mMinZoneCount;

        @Override // com.android.server.rms.memrepair.MemRepairAlgorithm.BaseHolder
        public /* bridge */ /* synthetic */ void updateSrcValue(long[] jArr, int i) {
            super.updateSrcValue(jArr, i);
        }

        private MemRepairHolder() {
            super();
        }

        public MemRepairHolder(int minIncDvalue, int minZoneCount, int maxZoneCount) {
            super();
            this.mMinIncDvalue = minIncDvalue;
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
            }
            int i = this.mMinZoneCount;
            if (i < 1 || i >= this.mMaxZoneCount) {
                AwareLog.e(MemRepairAlgorithm.TAG, "invalid min/max zone count");
                return false;
            }
            int i2 = this.mFloatPercent;
            if (i2 >= 1 && i2 <= 30) {
                return true;
            }
            AwareLog.e(MemRepairAlgorithm.TAG, "invalid percent");
            return false;
        }
    }

    public static class CallbackData {
        int mDvalueState;
        private int mDvalueZeroSum;
        long[] mDvalues;

        public void update(int dvalueState, long[] dvalues, int dvalueCount) {
            this.mDvalueState = dvalueState;
            this.mDvalues = Arrays.copyOf(dvalues, dvalueCount);
        }

        public boolean isIncreased() {
            int i = this.mDvalueState;
            if ((i & MemRepairAlgorithm.DVALUE_RISE_ALL) != 0) {
                return true;
            }
            int riseMiddle = 0;
            int riseFirst = (i & 4) != 0 ? 1 : 0;
            int riseLast = (this.mDvalueState & 8) != 0 ? 1 : 0;
            if ((this.mDvalueState & 16) != 0) {
                riseMiddle = 1;
            }
            return estimated(riseFirst, riseMiddle, riseLast);
        }

        private boolean estimated(int riseFirst, int riseMiddle, int riseLast) {
            int sum = riseFirst + riseLast + riseMiddle;
            if ((this.mDvalueState & MemRepairAlgorithm.DVALUE_RISE_EXCEED_TWOTHIRD) != 0 && sum > 0) {
                return true;
            }
            if ((this.mDvalueState & 64) != 0) {
                if (sum > 1) {
                    return true;
                }
                if (sum > 0 && this.mDvalueZeroSum > 20) {
                    return true;
                }
            }
            if ((this.mDvalueState & 32) == 0) {
                return false;
            }
            if (sum > 2) {
                return true;
            }
            if (sum > 1 && (riseFirst + riseLast == 2 || riseMiddle + riseLast == 2)) {
                return true;
            }
            if (sum <= 1 || this.mDvalueZeroSum <= 30) {
                return false;
            }
            return true;
        }
    }

    public static int translateMemRepair(MemRepairHolder srcData, EstimateCallback callback, Object cbUser) {
        if (srcData != null) {
            if (callback != null) {
                if (!srcData.isValid()) {
                    return 4;
                }
                CallbackData cbData = new CallbackData();
                int result = 5;
                int i = 1;
                long[] dvalues = new long[(srcData.mSrcValueCount - 1)];
                long[] zvalues = new long[srcData.mSrcValueCount];
                int mergesCount = (srcData.mMaxZoneCount - srcData.mMinZoneCount) + 1;
                int curZoneCount = srcData.mMaxZoneCount;
                int prevMergeSize = 0;
                int mergeIdx = 0;
                while (true) {
                    if (mergeIdx >= mergesCount) {
                        break;
                    }
                    int mergeSize = srcData.mSrcValueCount / curZoneCount;
                    curZoneCount--;
                    if (prevMergeSize != mergeSize) {
                        prevMergeSize = mergeSize;
                        if (mergeSize >= i) {
                            if (srcData.mSrcValueCount / mergeSize >= srcData.mMinZoneCount) {
                                int dvalueCount = getAndUpdateDvalues(zvalues, getAndUpdateZvalues(srcData, mergeSize, zvalues), dvalues);
                                if (estimateSumResult(dvalues, dvalueCount) != 2) {
                                    cbData.update(estimateDvalue(cbData, srcData, dvalues, dvalueCount), dvalues, dvalueCount);
                                    result = callback.estimateLinear(cbUser, cbData);
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
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    mergeIdx++;
                    i = 1;
                }
                return result;
            }
        }
        AwareLog.e(TAG, "invalid parameter");
        return 4;
    }

    private static int getAndUpdateZvalues(MemRepairHolder srcData, int mergeSize, long[] zvalues) {
        Arrays.fill(zvalues, 0L);
        int zvalueCount = 0;
        int sum = 0;
        for (int j = 0; j < srcData.mSrcValueCount; j++) {
            sum = (int) (((long) sum) + srcData.mSrcValues[j]);
            if ((j + 1) % mergeSize == 0) {
                zvalues[zvalueCount] = (long) (sum / mergeSize);
                sum = 0;
                zvalueCount++;
            }
        }
        if (sum > 0 && zvalueCount > 0) {
            zvalues[zvalueCount - 1] = ((zvalues[zvalueCount - 1] * ((long) mergeSize)) + ((long) sum)) / ((long) (mergeSize + (srcData.mSrcValueCount % mergeSize)));
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "zvalueCount=" + zvalueCount + ",zvalues=" + Arrays.toString(zvalues));
        }
        return zvalueCount;
    }

    private static int getAndUpdateDvalues(long[] zvalues, int zvalueCount, long[] dvalues) {
        Arrays.fill(dvalues, 0L);
        int dvalueCount = 0;
        long prevValue = zvalues[0];
        int j = 1;
        while (j < zvalueCount) {
            dvalues[dvalueCount] = zvalues[j] - prevValue;
            prevValue = zvalues[j];
            j++;
            dvalueCount++;
        }
        return dvalueCount;
    }

    private static int estimateSumResult(long[] dvalues, int dvalueCount) {
        int sum = 0;
        for (int i = 0; i < dvalueCount; i++) {
            sum = (int) (((long) sum) + dvalues[i]);
        }
        if (sum > 0) {
            return 6;
        }
        AwareLog.d(TAG, "dvalues sum=" + sum + ", decreased");
        return 2;
    }

    private static int estimateDvalue(CallbackData cbData, MemRepairHolder srcData, long[] dvalues, int dvalueCount) {
        int state = checkNegativeState(srcData.mFloatPercent, dvalues, dvalueCount);
        if ((state & 2) != 0) {
            AwareLog.d(TAG, "DVALUE_HAVE_NEGATIVE");
            return state;
        } else if (srcData.mMinIncDvalue <= 0) {
            return state | 1;
        } else {
            int[] diffs = new int[dvalueCount];
            cbData.mDvalueZeroSum = getAndUpdateDzValue(dvalues, dvalueCount, diffs, srcData.mMinIncDvalue);
            return estimateDvalueIncreased(diffs, dvalueCount, state);
        }
    }

    private static int estimateDvalueIncreased(int[] diffs, int dvalueCount, int inputState) {
        int state = inputState;
        int firstRiseIdx = getFirstRiseIndex(diffs, dvalueCount);
        if (firstRiseIdx <= -1 || firstRiseIdx != dvalueCount - 1) {
            if (firstRiseIdx > -1) {
                state |= 4;
                AwareLog.d(TAG, "DVALUE_RISE_FIRST_ONLY index=" + firstRiseIdx);
            }
            int lastRiseIdx = getLastRiseIndex(diffs, dvalueCount);
            if (lastRiseIdx > -1) {
                state |= 8;
                AwareLog.d(TAG, "DVALUE_RISE_LAST_ONLY index=" + lastRiseIdx);
            }
            int middleRiseCount = getMiddleRiseCount(diffs, dvalueCount, firstRiseIdx, lastRiseIdx);
            if (middleRiseCount > 0) {
                state |= 16;
                AwareLog.d(TAG, "DVALUE_RISE_MIDDLE count=" + middleRiseCount);
            }
            int lastRiseCount = 0;
            int firstRiseCount = firstRiseIdx > -1 ? firstRiseIdx + 1 : 0;
            if (lastRiseIdx > 0) {
                lastRiseCount = dvalueCount - lastRiseIdx;
            }
            int riseCount = firstRiseCount + middleRiseCount + lastRiseCount;
            if (riseCount * 3 > dvalueCount * 2) {
                state |= DVALUE_RISE_EXCEED_TWOTHIRD;
            } else if (riseCount * 2 > dvalueCount) {
                state |= 64;
            } else if (riseCount * 3 > dvalueCount) {
                state |= 32;
            }
            return state | 1;
        }
        int state2 = state | DVALUE_RISE_ALL;
        AwareLog.d(TAG, "DVALUE_RISE_ALL");
        return state2;
    }

    private static int checkNegativeState(int floatPercent, long[] dvalues, int dvalueCount) {
        int negaCount = 0;
        int sumPositive = 0;
        int sumNegative = 0;
        int i = 0;
        while (true) {
            int i2 = 0;
            if (i >= dvalueCount) {
                break;
            }
            if (dvalues[i] < 0) {
                i2 = 1;
            }
            negaCount += i2;
            if (dvalues[i] > 0) {
                sumPositive = (int) (((long) sumPositive) + dvalues[i]);
            } else {
                sumNegative = (int) (((long) sumNegative) + dvalues[i]);
            }
            i++;
        }
        int percentage = 100;
        if (sumPositive > 0) {
            percentage = ((-sumNegative) * 100) / sumPositive;
        }
        if (percentage > floatPercent || negaCount * 3 > dvalueCount) {
            return 2;
        }
        return 0;
    }

    private static int getAndUpdateDzValue(long[] dvalues, int dvalueCount, int[] dzValues, int minIncDvalue) {
        int zeroSum = 0;
        Arrays.fill(dzValues, 0);
        for (int i = 0; i < dvalueCount; i++) {
            dzValues[i] = dvalues[i] < 0 ? -1 : (int) (dvalues[i] / ((long) minIncDvalue));
            zeroSum += dzValues[i] > 0 ? dzValues[i] : 0;
        }
        return zeroSum;
    }

    private static int getFirstRiseIndex(int[] diffs, int dvalueCount) {
        int firstRiseIdx = 0;
        if (diffs[0] <= 0) {
            firstRiseIdx = -1;
        }
        int index = 1;
        while (index < dvalueCount && diffs[index] > 0 && firstRiseIdx == index - 1) {
            firstRiseIdx = index;
            index++;
        }
        return firstRiseIdx;
    }

    private static int getLastRiseIndex(int[] diffs, int dvalueCount) {
        int lastRiseIdx = diffs[dvalueCount + -1] > 0 ? dvalueCount - 1 : -1;
        int index = dvalueCount - 2;
        while (index >= 0 && diffs[index] > 0 && lastRiseIdx == index + 1) {
            lastRiseIdx = index;
            index--;
        }
        return lastRiseIdx;
    }

    private static int getMiddleRiseCount(int[] diffs, int dvalueCount, int firstRiseIdx, int lastRiseIdx) {
        int middleRiseCount = 0;
        int firstIndex = (firstRiseIdx < 0 || firstRiseIdx >= dvalueCount) ? -1 : firstRiseIdx;
        int lastIndex = (lastRiseIdx < 0 || lastRiseIdx >= dvalueCount) ? dvalueCount - 1 : lastRiseIdx;
        for (int index = firstIndex + 1; index < lastIndex; index++) {
            if (diffs[index] > 0) {
                middleRiseCount++;
            }
        }
        return middleRiseCount;
    }
}
