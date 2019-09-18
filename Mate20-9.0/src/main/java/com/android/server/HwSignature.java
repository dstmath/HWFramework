package com.android.server;

import android.util.Log;

public class HwSignature {
    private static final int AUTH_DS_LENGTH = 256;
    public static final int KEY_INDEX_GENERIC = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
    public static final int KEY_STATE_ERROR = -2;
    public static final int KEY_STATE_OK = 0;
    public static final int KEY_STATE_UNSUPPORT = -1;
    private static final String TAG = "HwSignature";
    private static final int TA_CLIENT_STATUS_INITING = -1;
    private static final int TA_CLIENT_STATUS_INIT_FAIL = -3;
    private static final int TA_CLIENT_STATUS_OK = 0;
    private static final int TA_CLIENT_STATUS_UNINIT = -2;
    private static HwSignature mInstance = null;
    private static final Object mLock = new Object();
    private static boolean sLibReady;
    private int mClientStatus = -2;

    private native int sign_close();

    private native int sign_get_cert(int i, int i2, byte[] bArr);

    private native int sign_get_cert_type(int i);

    private native int sign_get_publickey(int i, byte[] bArr);

    private native int sign_init();

    public native int sign_check_key();

    public native int sign_hwmember(int i, byte[] bArr, int i2, byte[] bArr2, int i3, byte[] bArr3, int i4, byte[] bArr4, int i5, byte[] bArr5);

    static {
        sLibReady = false;
        try {
            System.loadLibrary("hwsign");
            sLibReady = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load libarary hwsign failed >>>>>" + e);
            sLibReady = false;
        }
    }

    private HwSignature() {
        Log.d(TAG, "create HwSignature");
    }

    public static synchronized HwSignature getInstance() {
        HwSignature hwSignature;
        synchronized (HwSignature.class) {
            if (mInstance == null) {
                mInstance = new HwSignature();
            }
            hwSignature = mInstance;
        }
        return hwSignature;
    }

    private void initClient() {
        int ret = -1;
        Log.d(TAG, "HwSignature 64bits so, initClient");
        this.mClientStatus = -1;
        if (sLibReady) {
            try {
                ret = sign_init();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign sign_init failed >>>>>" + e);
            }
        }
        if (ret == 0) {
            this.mClientStatus = 0;
            Log.i(TAG, "tee client init OK");
            return;
        }
        this.mClientStatus = -3;
        Log.e(TAG, "tee client init fail, ret = " + ret);
    }

    public byte[] signMessage(int keyIndex, byte[] packageName, int package_len, byte[] deviceId, int devid_len, byte[] signatureType, int sign_type_len, byte[] challenge, int challenge_len) {
        int ret = -1;
        synchronized (mLock) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "teec client re init... in sign message");
                initClient();
            }
            if (this.mClientStatus == 0) {
                byte[] buf = new byte[256];
                try {
                    ret = sign_hwmember(keyIndex, packageName, package_len, deviceId, devid_len, signatureType, sign_type_len, challenge, challenge_len, buf);
                } catch (UnsatisfiedLinkError e) {
                    UnsatisfiedLinkError unsatisfiedLinkError = e;
                    Log.e(TAG, "call libarary hwsign sign_hwmember failed >>>>>" + e);
                }
                if (ret == 0) {
                    return buf;
                }
                byte[] bArr = new byte[0];
                return bArr;
            }
            Log.e(TAG, "teec client init ERROR!");
            byte[] bArr2 = new byte[0];
            return bArr2;
        }
    }

    public int checkKeyStatus(int keyIndex) {
        int ret = -1;
        synchronized (mLock) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "teec client re init... in check status");
                initClient();
            }
            if (1 == keyIndex && this.mClientStatus == 0) {
                try {
                    ret = sign_check_key();
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "call libarary hwsign sign_hwmember failed >>>>>" + e);
                }
                if (ret == 0) {
                    return 0;
                }
                return -2;
            }
            Log.e(TAG, "teec client init ERROR!");
            return -1;
        }
    }

    public int getPublicKey(int keyIndex, byte[] buf) {
        int ret = 0;
        synchronized (mLock) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "teec client re init...");
                initClient();
            }
            if (this.mClientStatus == 0) {
                try {
                    ret = sign_get_publickey(keyIndex, buf);
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "call libarary hwsign get_publickey failed >>>>>" + e);
                }
            } else {
                Log.e(TAG, "teec client init ERROR!");
            }
        }
        return ret;
    }

    public int getDeviceCertType(int keyIndex) {
        int ret = -1;
        synchronized (mLock) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "teec client re init...");
                initClient();
            }
            if (this.mClientStatus == 0) {
                try {
                    ret = sign_get_cert_type(keyIndex);
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "call libarary hwsign get_cert_type failed >>>>>" + e);
                }
            } else {
                Log.e(TAG, "teec client init ERROR!");
            }
        }
        return ret;
    }

    public int getDeviceCert(int keyIndex, int certType, byte[] certBuf) {
        int ret = -1;
        synchronized (mLock) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "teec client re init...");
                initClient();
            }
            if (this.mClientStatus == 0) {
                try {
                    ret = sign_get_cert(keyIndex, certType, certBuf);
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "call libarary hwsign get_cert failed >>>>>" + e);
                }
            } else {
                Log.e(TAG, "teec client init ERROR!");
            }
        }
        return ret;
    }
}
