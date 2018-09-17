package com.android.server.emcom.networkevaluation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.emcom.EmcomThread;
import java.util.LinkedList;

public class NetworkEvaluationEntry {
    private static final int BOTTLENECK_INDEX_IN_RESULT = 1;
    public static final int METRIC_COUNT = 8;
    public static final int METRIC_INTERVAL = 1000;
    public static final int MSG_LTE_EVALUATION_COMPLETE = 1001;
    public static final int MSG_LTE_EVALUATION_FAIL = 1002;
    public static final int MSG_WIFI_EVALUATION_COMPLETE = 2001;
    public static final int MSG_WIFI_EVALUATION_FAIL = 2002;
    public static final int NETWORK_BOTTLENECK_NONE = 1;
    public static final int NETWORK_BOTTLENECK_SIGNAL_STRENGTH = 2;
    public static final int NETWORK_BOTTLENECK_STABILITY = 4;
    public static final int NETWORK_BOTTLENECK_TRAFFIC = 3;
    public static final int NETWORK_BOTTLENECK_UNKNOWN = 0;
    public static final int NETWORK_QUALITY_BAD = 3;
    public static final int NETWORK_QUALITY_DEFAULT = 0;
    public static final int NETWORK_QUALITY_GOOD = 1;
    public static final int NETWORK_QUALITY_NORMAL = 2;
    public static final int NETWORK_TYPE_LTE = 0;
    public static final int NETWORK_TYPE_UNKNOWN = -1;
    public static final int NETWORK_TYPE_WIFI = 1;
    private static final int QUALITY_INDEX_IN_RESULT = 0;
    public static final int RESULT_DIMENSION = 2;
    private static final String TAG = "NetworkEvaluationEntry";
    private static volatile NetworkEvaluationEntry sNetworkEvaluationEntry;
    private LinkedList<INetworkEvaluationCallback> mCallbackList = new LinkedList();
    private Context mContext;
    private int mDataCardIndex = 0;
    private Handler mHandler;
    private LteEvaluationEntry mLteEvaluation;
    private int mNetworkType = -1;
    private int mPreviousBottleneck;
    private int mPreviousQuality;
    private volatile boolean mRunning;
    private WifiEvaluationEntry mWifiEvaluation;

    final class EvaluationEntryHandler extends Handler {
        EvaluationEntryHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int[] result;
            switch (msg.what) {
                case 1001:
                    result = (int[]) msg.obj;
                    Log.d(NetworkEvaluationEntry.TAG, "Cellular quality" + result[0] + "\n bottleneck" + result[1] + "\n");
                    if (result[0] != NetworkEvaluationEntry.this.mPreviousQuality || result[1] != NetworkEvaluationEntry.this.mPreviousBottleneck) {
                        NetworkEvaluationEntry.this.reportEvaluationResult(result[0], result[1]);
                        return;
                    }
                    return;
                case 1002:
                    Log.d(NetworkEvaluationEntry.TAG, "Cellular ranking failed!\n");
                    return;
                case 2001:
                    result = msg.obj;
                    Log.d(NetworkEvaluationEntry.TAG, "WiFi quality" + result[0] + "\n bottleneck" + result[1] + "\n");
                    if (result[0] != NetworkEvaluationEntry.this.mPreviousQuality || result[1] != NetworkEvaluationEntry.this.mPreviousBottleneck) {
                        NetworkEvaluationEntry.this.reportEvaluationResult(result[0], result[1]);
                        return;
                    }
                    return;
                case 2002:
                    Log.d(NetworkEvaluationEntry.TAG, "WiFi ranking failed!\n");
                    return;
                default:
                    Log.d(NetworkEvaluationEntry.TAG, "unknown message in NetworkEvaluationEntry\n");
                    return;
            }
        }
    }

    private NetworkEvaluationEntry(Context context) {
        if (context != null) {
            this.mContext = context;
        }
    }

    public static NetworkEvaluationEntry getInstance(Context context) {
        if (sNetworkEvaluationEntry == null && context == null) {
            Log.e(TAG, "try to create a new instance with null context, or get an non-existing instance");
            return null;
        }
        synchronized (NetworkEvaluationEntry.class) {
            if (sNetworkEvaluationEntry == null) {
                sNetworkEvaluationEntry = new NetworkEvaluationEntry(context);
            }
        }
        return sNetworkEvaluationEntry;
    }

    public void startEvaluation(int networkType, int dataCardIndex) {
        if (this.mContext == null) {
            Log.e(TAG, "failed to register NetworkSwitchReceiver due to null context");
        } else if (this.mNetworkType == networkType && this.mDataCardIndex == dataCardIndex) {
            Log.d(TAG, "try to restart an on-going evaluation with identical parameters, abort");
        } else {
            if (this.mRunning) {
                Log.d(TAG, "restart an evaluation with parameters networkType" + networkType + " dataCardIndex" + dataCardIndex);
                stopEvaluation();
            }
            if (checkStartParameters(this.mContext, networkType, dataCardIndex)) {
                this.mRunning = true;
                this.mNetworkType = networkType;
                this.mDataCardIndex = dataCardIndex;
                this.mHandler = new EvaluationEntryHandler(EmcomThread.getInstanceLooper());
                this.mPreviousQuality = 0;
                this.mPreviousBottleneck = 0;
                Log.d(TAG, "start evaluation");
                switch (this.mNetworkType) {
                    case 0:
                        this.mLteEvaluation = LteEvaluationEntry.getInstance(this.mContext, this.mHandler);
                        if (this.mLteEvaluation != null) {
                            this.mLteEvaluation.startEvaluation(dataCardIndex);
                            break;
                        } else {
                            Log.e(TAG, "null LTE evaluation instance");
                            return;
                        }
                    case 1:
                        this.mWifiEvaluation = WifiEvaluationEntry.getInstance(this.mContext, this.mHandler);
                        if (this.mWifiEvaluation != null) {
                            this.mWifiEvaluation.startEvaluation();
                            break;
                        } else {
                            Log.e(TAG, "null LTE evaluation instance");
                            return;
                        }
                    default:
                        Log.e(TAG, "try to start evaluation with unknown network type!");
                        break;
                }
                return;
            }
            Log.e(TAG, "illegal parameter in startEvaluation");
        }
    }

    public void stopEvaluation() {
        if (this.mRunning) {
            if (this.mWifiEvaluation != null) {
                this.mWifiEvaluation.stopEvaluation();
            }
            if (this.mLteEvaluation != null) {
                this.mLteEvaluation.stopEvaluation();
            }
            this.mPreviousQuality = 0;
            this.mPreviousBottleneck = 0;
            this.mNetworkType = -1;
            this.mDataCardIndex = 0;
            this.mRunning = false;
            return;
        }
        Log.d(TAG, "attempt to stop a stopped evaluation");
    }

    private void reportEvaluationResult(int quality, int bottleneck) {
        if (quality == 0 || bottleneck == 0) {
            Log.e(TAG, "received a default result in reportEvaluationResult");
            return;
        }
        respondCallbackFunctions(quality, bottleneck, this.mNetworkType);
        this.mPreviousQuality = quality;
        this.mPreviousBottleneck = bottleneck;
    }

    public void registerNetworkEvaluationCallback(INetworkEvaluationCallback callback) {
        if (callback == null) {
            Log.e(TAG, "null callback or mCallbackList");
            return;
        }
        this.mCallbackList.add(callback);
        Log.d(TAG, "added a callback function");
        callback.onEvaluationResultChanged(new NetworkEvaluationResult(this.mPreviousQuality, this.mPreviousBottleneck, this.mNetworkType));
        Log.d(TAG, "previous result had been sent to the new callback function");
    }

    public void unRegisterNetworkEvaluationCallback(INetworkEvaluationCallback callback) {
        if (callback == null) {
            Log.e(TAG, "null callback or mCallbackList");
            return;
        }
        this.mCallbackList.remove(callback);
        Log.d(TAG, "removed a callback function");
    }

    private void respondCallbackFunctions(int quality, int bottleneck, int networkType) {
        Log.d(TAG, "notify new result to all callback functions : quality " + quality + " bottleneck " + bottleneck + "network type " + networkType);
        NetworkEvaluationResult result = new NetworkEvaluationResult(quality, bottleneck, networkType);
        int size = this.mCallbackList.size();
        for (int i = 0; i < size; i++) {
            ((INetworkEvaluationCallback) this.mCallbackList.get(i)).onEvaluationResultChanged(result);
        }
    }

    private boolean checkStartParameters(Context context, int networkType, int dataCardIndex) {
        if (context == null) {
            Log.e(TAG, "attempt to start evaluation with null context");
            return false;
        } else if (networkType != 0 && 1 != networkType) {
            Log.e(TAG, "attempt to start evaluation with illegal network type");
            return false;
        } else if (dataCardIndex >= 0) {
            return true;
        } else {
            Log.e(TAG, "attempt to start evaluation with negative index");
            return false;
        }
    }
}
