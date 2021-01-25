package com.huawei.trustzone;

import android.util.Log;

public class Signature {
    private static final String TAG = "Signature";
    private static final int TA_CLIENT_STATUS_INITING = -1;
    private static final int TA_CLIENT_STATUS_INIT_FAIL = -3;
    private static final int TA_CLIENT_STATUS_OK = 0;
    private static final int TA_CLIENT_STATUS_UNINIT = -2;
    private static Signature mInstance = null;
    private int mTaClientStatus = -2;

    private Signature() {
        initTaClient();
    }

    public static synchronized Signature getInstance() {
        Signature signature;
        synchronized (Signature.class) {
            if (mInstance == null) {
                try {
                    mInstance = new Signature();
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "LoadLibrary occurs error " + e.toString());
                    return null;
                }
            }
            signature = mInstance;
        }
        return signature;
    }

    private void initTaClient() {
        this.mTaClientStatus = -1;
        int ret = SignatureNative.teecInit();
        if (ret == 0) {
            this.mTaClientStatus = 0;
            Log.i(TAG, "tee client init OK");
            return;
        }
        this.mTaClientStatus = -3;
        Log.e(TAG, "tee client init fail, ret = " + ret);
    }

    public int signMessage(byte[] challenge, int challenge_leng, byte[] buf) {
        if (this.mTaClientStatus == 0) {
            return SignatureNative.sign(challenge, challenge_leng, buf);
        }
        Log.e(TAG, "teec client init ERROR!");
        return -1;
    }

    public int getDeviceKeyStatus() {
        if (this.mTaClientStatus == 0) {
            return SignatureNative.checkDeviceKey();
        }
        Log.e(TAG, "teec client init ERROR!");
        return -1;
    }
}
