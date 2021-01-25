package com.huawei.displayengine;

import android.content.Context;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: BrigntnessTrainingProcessor */
public class BrightnessTrainingProcessor {
    private static final String TAG = "DE J BrightnessTrainingProcessor";
    private BrightnessTrainingAlgoImpl mAlgo;
    private final Context mContext;
    private Object mLockAlgo = new Object();

    public BrightnessTrainingProcessor(Context context) {
        DeLog.i(TAG, "BrightnessTrainingProcessor enter");
        this.mContext = context;
    }

    private BrightnessTrainingAlgoImpl getBrightnessTrainingAlgo() {
        if (this.mContext == null) {
            DeLog.e(TAG, "mContext is null, returned!");
            return null;
        }
        synchronized (this.mLockAlgo) {
            if (this.mAlgo == null) {
                this.mAlgo = new BrightnessTrainingAlgoImpl(this.mContext);
            }
        }
        return this.mAlgo;
    }

    public int brightnessTrainingProcess(String command, Map<String, Object> map) {
        DeLog.i(TAG, "brightnessTrainingProcess ");
        BrightnessTrainingAlgoImpl algo = getBrightnessTrainingAlgo();
        if (algo == null) {
            DeLog.w(TAG, "getBrightnessTrainingAlgo failed! ");
            return -1;
        }
        int ret = algo.processTraining();
        if (ret != 0) {
            DeLog.i(TAG, "algo.processTraining failed! ");
        }
        return ret;
    }

    public int brightnessTrainingAbort() {
        DeLog.i(TAG, "brightnessTrainingAbort ");
        BrightnessTrainingAlgoImpl algo = getBrightnessTrainingAlgo();
        if (algo == null) {
            DeLog.w(TAG, "getBrightnessTrainingAlgo failed! ");
            return -1;
        }
        int ret = algo.abortTraining();
        if (ret != 0) {
            DeLog.w(TAG, "algo.brightnessTrainingAbort failed! ");
        }
        return ret;
    }
}
