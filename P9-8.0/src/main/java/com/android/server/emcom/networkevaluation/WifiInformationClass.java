package com.android.server.emcom.networkevaluation;

import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import com.android.server.wifipro.WifiProCHRManager;
import java.util.Arrays;

class WifiInformationClass {
    private static final /* synthetic */ int[] -com-android-server-emcom-networkevaluation-TrafficStatusSwitchesValues = null;
    private static final int BN = 1;
    private static final int BOTTLENECK_INDEX_IN_RESULT = 1;
    private static final int BR = 2;
    private static final int BS = 4;
    private static final int BT = 3;
    private static final int INDEX_LEGAL_LOWER_BOUND = 222;
    private static final int INDEX_LOW_RSSI_THRESHOLD = 200;
    private static final int INDEX_SPECIAL_CASE = 100;
    private static final int INDEX_SPECIAL_CASE_RSSI_THRESHOLD = -60;
    private static final int[][][] INDEX_TO_BOTTLENECK_TABLE;
    private static final int[][][] INDEX_TO_QUALITY_TABLE;
    private static final int[] INTERVAL_ENDS = new int[]{-80, -45, -20, 0};
    private static final int INVALID_VALUE = -1;
    private static final int[] MDEV_RSSI_THRESHOLDS = new int[]{15};
    private static final int[] MDEV_RTT_THRESHOLDS = new int[]{40, 60};
    private static final int MIN_RSSI_VALUE = -127;
    private static final int NETWORK_BOTTLENECK_NONE = 1;
    private static final int NETWORK_BOTTLENECK_RSSI = 2;
    private static final int NETWORK_BOTTLENECK_STABILITY = 4;
    private static final int NETWORK_BOTTLENECK_TRAFFIC = 3;
    private static final int NETWORK_BOTTLENECK_UNKNOWN = 0;
    private static final int NETWORK_QUALITY_BAD = 3;
    private static final int NETWORK_QUALITY_DEFAULT = 0;
    private static final int NETWORK_QUALITY_GOOD = 1;
    private static final int NETWORK_QUALITY_NORMAL = 2;
    private static final int QB = 3;
    private static final int QG = 1;
    private static final int QN = 2;
    private static final int QUALITY_INDEX_IN_RESULT = 0;
    private static final int[][][] REFERENCE_POINT_SETS;
    private static final int RESULT_DIMENSION = 2;
    private static final int[] RSSI_AVG_INTERVAL_ENDS = new int[]{-100, -70, INDEX_SPECIAL_CASE_RSSI_THRESHOLD, -40, -20, 0};
    private static final int[] RSSI_MDEV_INTERVAL_ENDS = new int[]{5, 10, 15, 20, 25};
    private static final float SMALLNUMBER = 0.001f;
    private static final int SUB_INDEX_RANK_0 = 0;
    private static final int SUB_INDEX_RANK_1 = 1;
    private static final int SUB_INDEX_RANK_2 = 2;
    private static final String TAG = "WifiInformationClass";
    private int[] mRssiArray;
    private int[] mRttArray;
    private SnrMetrics mSnrMetrics = new SnrMetrics(this.mRssiArray);
    private StabilityMetrics mStabilityMetrics = new StabilityMetrics(0, 0);
    private TrafficMetrics mTrafficMetrics = new TrafficMetrics(TrafficStatus.NONE);

    private static /* synthetic */ int[] -getcom-android-server-emcom-networkevaluation-TrafficStatusSwitchesValues() {
        if (-com-android-server-emcom-networkevaluation-TrafficStatusSwitchesValues != null) {
            return -com-android-server-emcom-networkevaluation-TrafficStatusSwitchesValues;
        }
        int[] iArr = new int[TrafficStatus.values().length];
        try {
            iArr[TrafficStatus.HEAVY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TrafficStatus.LIGHT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[TrafficStatus.NONE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-server-emcom-networkevaluation-TrafficStatusSwitchesValues = iArr;
        return iArr;
    }

    static {
        r0 = new int[3][][];
        r0[0] = new int[][]{new int[]{90, 100}, new int[]{130, WifiProCHRManager.WIFI_PORTAL_SAMPLES_COLLECTE}, new int[]{HwActivityManagerService.SERVICE_ADJ, 860}};
        r0[1] = new int[][]{new int[]{60, 60}, new int[]{110, 110}, new int[]{HwActivityManagerService.SERVICE_ADJ, 550}};
        r0[2] = new int[][]{new int[]{60, 60}, new int[]{110, 110}, new int[]{HwActivityManagerService.SERVICE_ADJ, 550}};
        REFERENCE_POINT_SETS = r0;
        r0 = new int[2][][];
        r0[0] = new int[][]{new int[]{1, 2, 3}, new int[]{2, 2, 3}, new int[]{3, 3, 3}};
        r0[1] = new int[][]{new int[]{1, 2, 3}, new int[]{2, 2, 3}, new int[]{3, 3, 3}};
        INDEX_TO_QUALITY_TABLE = r0;
        r0 = new int[2][][];
        r0[0] = new int[][]{new int[]{1, 4, 4}, new int[]{3, 3, 4}, new int[]{3, 3, 3}};
        r0[1] = new int[][]{new int[]{1, 4, 4}, new int[]{3, 3, 4}, new int[]{3, 3, 3}};
        INDEX_TO_BOTTLENECK_TABLE = r0;
    }

    WifiInformationClass(int[] rssiArray, int[] rttArray) {
        this.mRssiArray = rssiArray;
        this.mRttArray = rttArray;
    }

    private boolean checkMetrics() {
        if (this.mRssiArray == null || this.mRttArray == null) {
            return false;
        }
        for (int i : this.mRssiArray) {
            if (i >= 0 || i < MIN_RSSI_VALUE) {
                return false;
            }
        }
        for (int i2 : this.mRttArray) {
            if (i2 <= 0) {
                return false;
            }
        }
        return true;
    }

    boolean extractVitalMetrics() {
        if (checkMetrics()) {
            int[][] referencePoints = getReferencePoints();
            if (referencePoints == null) {
                return false;
            }
            this.mTrafficMetrics.setStatus(deduceTrafficStatus((int) calculateAverageValue(this.mRttArray, this.mRttArray.length), (int) calculateMdevValue(this.mRttArray, this.mRttArray.length), referencePoints));
            int mdevRTT = (int) calculateMdevValue(this.mRttArray, this.mRttArray.length);
            this.mStabilityMetrics.setValues(mdevRTT, (int) calculateMdevValue(this.mRssiArray, this.mRssiArray.length));
            Log.d(TAG, "extractVitalMetrics() complete!\n");
            return true;
        }
        Log.d(TAG, "illegal information!\n");
        return false;
    }

    private int[][] getReferencePoints() {
        int avgRssi = this.mSnrMetrics.avgRssi;
        int i = 0;
        while (i < INTERVAL_ENDS.length - 1) {
            if (avgRssi > INTERVAL_ENDS[i] && avgRssi <= INTERVAL_ENDS[i + 1]) {
                return REFERENCE_POINT_SETS[i];
            }
            i++;
        }
        return null;
    }

    private TrafficStatus deduceTrafficStatus(int avgRtt, int mdevRtt, int[][] referencePoints) {
        if (referencePoints == null) {
            Log.e(TAG, "null referencePoints in deduceTrafficStatus");
            return TrafficStatus.HEAVY;
        }
        int i;
        int number = referencePoints.length;
        for (i = 0; i < number; i++) {
            if (referencePoints[i] == null) {
                Log.e(TAG, "null reference in referencePoints[" + i + "]");
            }
        }
        int[] currentPoint = new int[]{avgRtt, mdevRtt};
        float[] distances = new float[number];
        float[] sortedDistances = new float[number];
        for (i = 0; i < number; i++) {
            distances[i] = calculateEuclideanDistance(currentPoint, referencePoints[i]);
            sortedDistances[i] = distances[i];
        }
        Arrays.sort(sortedDistances);
        int bestMatch = -1;
        i = 0;
        while (i < number) {
            if (sortedDistances[0] - distances[i] > -0.001f || sortedDistances[0] - distances[i] < SMALLNUMBER) {
                bestMatch = i;
            }
            i++;
        }
        switch (bestMatch) {
            case 0:
                return TrafficStatus.NONE;
            case 1:
                return TrafficStatus.LIGHT;
            case 2:
                return TrafficStatus.HEAVY;
            default:
                return TrafficStatus.HEAVY;
        }
    }

    int[] calComprehensiveQuality() {
        int quality;
        int bottleneck;
        int indexRssi = calRssiIndex();
        int indexTraffic = calTrafficIndex();
        int indexStability = calStabilityIndex();
        int index = ((indexRssi * 100) + (indexTraffic * 10)) + indexStability;
        if (index < 0 || index > INDEX_LEGAL_LOWER_BOUND) {
            quality = 0;
            bottleneck = 0;
        } else if (index >= 200) {
            quality = 3;
            bottleneck = 2;
        } else {
            quality = INDEX_TO_QUALITY_TABLE[indexRssi][indexTraffic][indexStability];
            bottleneck = INDEX_TO_BOTTLENECK_TABLE[indexRssi][indexTraffic][indexStability];
            if (index == 100) {
                if (this.mSnrMetrics.avgRssi <= INDEX_SPECIAL_CASE_RSSI_THRESHOLD) {
                    quality = 2;
                    bottleneck = 2;
                } else {
                    quality = 1;
                    bottleneck = 1;
                }
            }
        }
        return new int[]{quality, bottleneck};
    }

    private int calRssiIndex() {
        int mdevRssi = (int) calculateMdevValue(this.mRssiArray, this.mRssiArray.length);
        int avgRssi = (int) calculateAverageValue(this.mRssiArray, this.mRssiArray.length);
        if (avgRssi > RSSI_AVG_INTERVAL_ENDS[0] && avgRssi <= RSSI_AVG_INTERVAL_ENDS[1]) {
            return 2;
        }
        if (avgRssi <= RSSI_AVG_INTERVAL_ENDS[1] || avgRssi > RSSI_AVG_INTERVAL_ENDS[2]) {
            if (avgRssi <= RSSI_AVG_INTERVAL_ENDS[2] || avgRssi > RSSI_AVG_INTERVAL_ENDS[3]) {
                if (avgRssi <= RSSI_AVG_INTERVAL_ENDS[3] || avgRssi > RSSI_AVG_INTERVAL_ENDS[4]) {
                    if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[3]) {
                        return 0;
                    }
                    if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[3] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[4]) {
                        return 2;
                    }
                    return 1;
                } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[1]) {
                    return 0;
                } else {
                    if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[1] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[3]) {
                        return 2;
                    }
                    return 1;
                }
            } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0]) {
                return 0;
            } else {
                if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[2]) {
                    return 2;
                }
                return 1;
            }
        } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0]) {
            return 1;
        } else {
            if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[1]) {
                return 2;
            }
            return 2;
        }
    }

    private int calTrafficIndex() {
        switch (-getcom-android-server-emcom-networkevaluation-TrafficStatusSwitchesValues()[this.mTrafficMetrics.inAirTrafficStatus.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 0;
            default:
                return 0;
        }
    }

    private int calStabilityIndex() {
        int mdevRssi = this.mStabilityMetrics.mdevRssi;
        int mdevRtt = this.mStabilityMetrics.mdevRtt;
        if (mdevRtt < MDEV_RTT_THRESHOLDS[0] && mdevRssi < MDEV_RSSI_THRESHOLDS[0]) {
            return 0;
        }
        if (mdevRtt < MDEV_RTT_THRESHOLDS[0] || mdevRtt >= MDEV_RTT_THRESHOLDS[1] || mdevRssi >= MDEV_RSSI_THRESHOLDS[0]) {
            return 2;
        }
        return 1;
    }

    static float calculateAverageValue(int[] values, int length) {
        if (values == null || length <= 0) {
            Log.e(TAG, "try to calculateAverageValue with null array");
            return -1.0f;
        } else if (length > values.length) {
            Log.e(TAG, "specified length exceeds value array");
            return -1.0f;
        } else {
            float sum = 0.0f;
            for (int i = 0; i < length; i++) {
                sum += (float) i;
            }
            return sum / ((float) length);
        }
    }

    private static float calculateMdevValue(int[] values, int length) {
        if (values == null || length <= 0) {
            Log.e(TAG, "try to calculateMdevValue with null array");
            return -1.0f;
        } else if (length > values.length) {
            Log.e(TAG, "specified length exceeds value array");
            return -1.0f;
        } else {
            float avg = calculateAverageValue(values, length);
            float sum = 0.0f;
            for (int i = 0; i < length; i++) {
                sum += (((float) i) - avg) * (((float) i) - avg);
            }
            return (float) Math.sqrt((double) (sum / ((float) length)));
        }
    }

    private static float calculateEuclideanDistance(int[] point1, int[] point2) {
        if (point1 == null || point2 == null) {
            Log.e(TAG, "try to calculateEuclideanDistance with null array");
            return -1.0f;
        }
        int dimension = point1.length;
        if (dimension != point2.length) {
            Log.i(TAG, "try to calculateEuclideanDistance with arrays of different size");
            return -1.0f;
        }
        int sum = 0;
        for (int i = 0; i < dimension; i++) {
            sum += (point1[i] - point2[i]) * (point1[i] - point2[i]);
        }
        return (float) Math.sqrt((double) sum);
    }

    private static int calRssiIndex(int[] rssiArray, int length) {
        if (rssiArray == null || length <= 0) {
            Log.e(TAG, "try to calRssiIndex with null array");
            return -1;
        }
        int result;
        int mdevRssi = (int) calculateMdevValue(rssiArray, length);
        int avgRssi = (int) calculateAverageValue(rssiArray, length);
        if (avgRssi > RSSI_AVG_INTERVAL_ENDS[0] && avgRssi <= RSSI_AVG_INTERVAL_ENDS[1]) {
            result = 2;
        } else if (avgRssi <= RSSI_AVG_INTERVAL_ENDS[1] || avgRssi > RSSI_AVG_INTERVAL_ENDS[2]) {
            if (avgRssi <= RSSI_AVG_INTERVAL_ENDS[2] || avgRssi > RSSI_AVG_INTERVAL_ENDS[3]) {
                if (avgRssi <= RSSI_AVG_INTERVAL_ENDS[3] || avgRssi > RSSI_AVG_INTERVAL_ENDS[4]) {
                    if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[3]) {
                        result = 0;
                    } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[3] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[4]) {
                        result = 2;
                    } else {
                        result = 1;
                    }
                } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[1]) {
                    result = 0;
                } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[1] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[3]) {
                    result = 2;
                } else {
                    result = 1;
                }
            } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0]) {
                result = 0;
            } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[2]) {
                result = 2;
            } else {
                result = 1;
            }
        } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0]) {
            result = 1;
        } else if (mdevRssi <= RSSI_MDEV_INTERVAL_ENDS[0] || mdevRssi > RSSI_MDEV_INTERVAL_ENDS[1]) {
            result = 2;
        } else {
            result = 2;
        }
        return result;
    }

    private static int calStabilityIndex(int[] rssiArray, int length) {
        if (rssiArray == null || length <= 0) {
            Log.e(TAG, "try to calStabilityIndex with null array");
            return -1;
        } else if (((int) calculateMdevValue(rssiArray, length)) < MDEV_RSSI_THRESHOLDS[0]) {
            return 0;
        } else {
            return 2;
        }
    }

    static int[] calComprehensiveQuality(int[] rssiArray, int length) {
        if (rssiArray == null || length <= 0) {
            Log.e(TAG, "try to calComprehensiveQuality with null array");
            return new int[0];
        }
        int quality;
        int bottleneck;
        int indexRssi = calRssiIndex(rssiArray, length);
        int indexStability = calStabilityIndex(rssiArray, length);
        int avgRssi = (int) calculateAverageValue(rssiArray, length);
        int index = ((indexRssi * 100) + 0) + indexStability;
        if (index < 0 || index > INDEX_LEGAL_LOWER_BOUND) {
            quality = 0;
            bottleneck = 0;
        } else if (index >= 200) {
            quality = 3;
            bottleneck = 2;
        } else {
            quality = INDEX_TO_QUALITY_TABLE[indexRssi][0][indexStability];
            bottleneck = INDEX_TO_BOTTLENECK_TABLE[indexRssi][0][indexStability];
            if (index == 100) {
                if (avgRssi <= INDEX_SPECIAL_CASE_RSSI_THRESHOLD) {
                    quality = 2;
                    bottleneck = 2;
                } else {
                    quality = 1;
                    bottleneck = 1;
                }
            }
        }
        return new int[]{quality, bottleneck};
    }
}
