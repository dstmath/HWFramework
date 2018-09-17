package com.android.server.emcom.networkevaluation;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.server.emcom.ParaManagerConstants;

class LteRankingProcess {
    private static final int BOTTLENECK_INDEX_IN_RESULT = 1;
    private static final int INVALID_VALUE = -1;
    private static final int[] LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL = new int[]{-130, -115, -95, 0};
    private static final int MSG_LTE_RANKING_COMPLETE = 1001;
    private static final int MSG_LTE_RANKING_FAIL = 1002;
    private static final int NETWORK_BOTTLENECK_NONE = 1;
    private static final int NETWORK_BOTTLENECK_SIGNAL_STRENGTH = 2;
    private static final int NETWORK_BOTTLENECK_UNKNOWN = 0;
    private static final int NETWORK_QUALITY_BAD = 3;
    private static final int NETWORK_QUALITY_DEFAULT = 0;
    private static final int NETWORK_QUALITY_GOOD = 1;
    private static final int NETWORK_QUALITY_NORMAL = 2;
    private static final int QUALITY_INDEX_IN_RESULT = 0;
    private static final int RESULT_DIMENSION = 2;
    private static final String TAG = "LteRankingProcess";
    private static final int[] WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL = new int[]{-120, -90, -75, 0};
    private Handler mHandler;
    private int[] mSignalStrengthArray;

    LteRankingProcess(int[] signalStrengthArray, Handler handler) {
        if (signalStrengthArray == null || handler == null) {
            Log.e(TAG, "try to create a LteRankingProcess with null parameter");
            return;
        }
        this.mSignalStrengthArray = signalStrengthArray;
        this.mHandler = handler;
    }

    public void startRanking() {
        if (this.mSignalStrengthArray == null) {
            Log.e(TAG, "try to startRanking() with null signal strength array");
        } else if (this.mHandler == null) {
            Log.d(TAG, "try to startRanking() with null signal strength array");
        } else {
            int avgSignalStrength = (int) calculateAverageValue(this.mSignalStrengthArray, this.mSignalStrengthArray.length);
            int quality = 0;
            int bottleneck = 0;
            if (avgSignalStrength > LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[0] && avgSignalStrength <= LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[1]) {
                quality = 3;
                Log.d(TAG, "avgSignalStrength is " + avgSignalStrength + ", output quality is bad");
            } else if (avgSignalStrength > LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[1] && avgSignalStrength <= LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[2]) {
                quality = 2;
                Log.d(TAG, "avgSignalStrength is " + avgSignalStrength + ", output quality is normal");
            } else if (avgSignalStrength > LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[2] && avgSignalStrength <= LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[3]) {
                quality = 1;
                Log.d(TAG, "avgSignalStrength is " + avgSignalStrength + ", output quality is good");
            }
            switch (quality) {
                case 1:
                    bottleneck = 1;
                    break;
                case 2:
                    bottleneck = 2;
                    break;
                case 3:
                    bottleneck = 2;
                    break;
                default:
                    Log.d(TAG, "illegal quality");
                    break;
            }
            int[] result = new int[]{quality, bottleneck};
            Message msg = Message.obtain();
            if (quality == 0) {
                msg.what = 1002;
                msg.obj = result;
                this.mHandler.sendMessage(msg);
            } else {
                msg.what = 1001;
                msg.obj = result;
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private static float calculateAverageValue(int[] values, int length) {
        if (values == null || length <= 0) {
            return -1.0f;
        }
        if (length > values.length) {
            Log.e(TAG, "specified length exceeds value array");
            return -1.0f;
        }
        float sum = 0.0f;
        for (int i = 0; i < length; i++) {
            sum += (float) values[i];
        }
        return sum / ((float) length);
    }

    static void staticStartRanking(int[] signalStrengthArray, int length, Handler handler, int cellularType) {
        if (signalStrengthArray == null || length <= 0) {
            Log.e(TAG, "null signalStrengthArray in staticStartRanking");
        } else if (handler == null) {
            Log.e(TAG, "null handler in staticStartRanking");
        } else if (1000 == cellularType || 2000 == cellularType) {
            Log.i(TAG, "cellular type is " + cellularType + ", not qualified for evaluation");
        } else {
            int quality;
            int avgSignalStrength = (int) calculateAverageValue(signalStrengthArray, length);
            switch (cellularType) {
                case 3000:
                    if (avgSignalStrength <= WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL[0] || avgSignalStrength > WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL[1]) {
                        if (avgSignalStrength <= WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL[1] || avgSignalStrength > WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL[2]) {
                            if (avgSignalStrength > WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL[2] && avgSignalStrength <= WCDMA_QUALITY_SIGNAL_STRENGTH_INTERVAL[3]) {
                                quality = 1;
                                Log.d(TAG, "3G avgSignalStrength is " + avgSignalStrength + ", output quality is good");
                                break;
                            }
                            quality = 0;
                            Log.d(TAG, "3G avgSignalStrength is " + avgSignalStrength + ", beyond evaluation boundary");
                            break;
                        }
                        quality = 2;
                        Log.d(TAG, "3G avgSignalStrength is " + avgSignalStrength + ", output quality is normal");
                        break;
                    }
                    quality = 3;
                    Log.d(TAG, "3G avgSignalStrength is " + avgSignalStrength + ", output quality is bad");
                    break;
                    break;
                case ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE /*4000*/:
                    if (avgSignalStrength <= LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[0] || avgSignalStrength > LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[1]) {
                        if (avgSignalStrength <= LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[1] || avgSignalStrength > LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[2]) {
                            if (avgSignalStrength > LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[2] && avgSignalStrength <= LTE_QUALITY_SIGNAL_STRENGTH_INTERVAL[3]) {
                                quality = 1;
                                Log.d(TAG, "LTE avgSignalStrength is " + avgSignalStrength + ", output quality is good");
                                break;
                            }
                            quality = 0;
                            Log.d(TAG, "LTE avgSignalStrength is " + avgSignalStrength + ", beyond evaluation boundary");
                            break;
                        }
                        quality = 2;
                        Log.d(TAG, "LTE avgSignalStrength is " + avgSignalStrength + ", output quality is normal");
                        break;
                    }
                    quality = 3;
                    Log.d(TAG, "LTE avgSignalStrength is " + avgSignalStrength + ", output quality is bad");
                    break;
                    break;
                default:
                    Log.e(TAG, "illegal cellular type");
                    return;
            }
            int bottleneck = 0;
            switch (quality) {
                case 1:
                    bottleneck = 1;
                    break;
                case 2:
                    bottleneck = 2;
                    break;
                case 3:
                    bottleneck = 2;
                    break;
                default:
                    Log.d(TAG, "illegal quality");
                    break;
            }
            int[] result = new int[]{quality, bottleneck};
            Message msg = Message.obtain();
            if (quality == 0) {
                msg.what = 1002;
                msg.obj = result;
                handler.sendMessage(msg);
            } else {
                msg.what = 1001;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        }
    }
}
