package com.android.server;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.HiViewEx;
import java.util.Locale;

public class HwSignature {
    private static final int AUTH_DS_LENGTH = 256;
    private static final int HEX_MASK = 255;
    private static final int HEX_MULTIPLY = 2;
    private static final int HIVIEW_ID = 992770101;
    public static final int KEY_INDEX_GENERIC = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
    public static final int KEY_STATE_ERROR = -2;
    public static final int KEY_STATE_OK = 0;
    public static final int KEY_STATE_UNSUPPORT = -1;
    private static final Object LOCK = new Object();
    private static final String PNAME_ID = "signtool";
    private static final String PVERSION_ID = "11.0.1";
    private static final String TAG = "HwSignature";
    private static final int TA_CLIENT_STATUS_INITING = -1;
    private static final int TA_CLIENT_STATUS_INIT_FAIL = -3;
    private static final int TA_CLIENT_STATUS_OK = 0;
    private static final int TA_CLIENT_STATUS_UNINIT = -2;
    private static final String UNKNOWN_PACKAGE = "unknown_package";
    private static HwSignature instance = null;
    private static boolean sLibReady;
    private int mClientStatus = -2;

    private native int signCheckKey();

    private native int signClose();

    private native int signGetCert(int i, int i2, byte[] bArr);

    private native int signGetCertType(int i);

    private native int signGetPublickey(int i, byte[] bArr);

    private native int signHwmember(int i, byte[] bArr, int i2, byte[] bArr2);

    private native int signInit();

    static {
        sLibReady = false;
        try {
            System.loadLibrary("hwsign");
            sLibReady = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load libarary hwsign failed");
            sLibReady = false;
        }
    }

    private HwSignature() {
        Log.d(TAG, "Create HwSignature");
    }

    public static synchronized HwSignature getInstance() {
        HwSignature hwSignature;
        synchronized (HwSignature.class) {
            if (instance == null) {
                instance = new HwSignature();
            }
            hwSignature = instance;
        }
        return hwSignature;
    }

    private void initClient() {
        int ret = -1;
        Log.d(TAG, "HwSignature 64bits so, initClient");
        this.mClientStatus = -1;
        if (sLibReady) {
            try {
                ret = signInit();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Call libarary hwsign sign init failed");
            }
        }
        if (ret == 0) {
            this.mClientStatus = 0;
            Log.i(TAG, "Tee client init OK");
            return;
        }
        this.mClientStatus = -3;
        Log.e(TAG, "Tee client init fail, ret = " + ret);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c8 A[Catch:{ all -> 0x00df, all -> 0x00e6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00cd A[Catch:{ all -> 0x00df, all -> 0x00e6 }, DONT_GENERATE] */
    public byte[] signMessage(int keyIndex, byte[] packageName, int packageLen, byte[] deviceId, int devidLen, byte[] signatureType, int signTypeLen, byte[] challenge, int challengeLen) {
        byte[] inputBuf;
        if (packageName == null || packageLen <= 0 || deviceId == null || devidLen <= 0 || signatureType == null || signTypeLen <= 0 || challenge == null || challengeLen <= 0) {
            Log.e(TAG, "The parameter is invalidpackageLen " + packageLen + "devidLen " + devidLen + "signTypeLen " + signTypeLen + "challengeLen " + challengeLen);
            return new byte[0];
        }
        int ret = -1;
        synchronized (LOCK) {
            try {
                if (this.mClientStatus != 0) {
                    try {
                        Log.i(TAG, "teec client re init in sign message");
                        initClient();
                    } catch (Throwable th) {
                        th = th;
                    }
                }
                if (this.mClientStatus == 0) {
                    byte[] buf = new byte[256];
                    int inputLen = packageLen + devidLen + signTypeLen + challengeLen + 1;
                    try {
                        inputBuf = new byte[inputLen];
                        try {
                            ret = signHwmember(keyIndex, inputBuf, inputLen, buf);
                        } catch (UnsatisfiedLinkError e) {
                            Log.e(TAG, "Call libarary hwsign sign hwmember failed");
                            if (ret != 0) {
                            }
                        }
                    } catch (UnsatisfiedLinkError e2) {
                        Log.e(TAG, "Call libarary hwsign sign hwmember failed");
                        if (ret != 0) {
                        }
                    }
                    try {
                        System.arraycopy(packageName, 0, inputBuf, 0, packageLen);
                        int inputLenTmp = 0 + packageLen;
                        System.arraycopy(challenge, 0, inputBuf, inputLenTmp, challengeLen);
                        int inputLenTmp2 = inputLenTmp + challengeLen;
                        System.arraycopy(deviceId, 0, inputBuf, inputLenTmp2, devidLen);
                        System.arraycopy(signatureType, 0, inputBuf, inputLenTmp2 + devidLen, signTypeLen);
                        reportInformation(getCallPackageName(), "signHwmember");
                    } catch (UnsatisfiedLinkError e3) {
                        Log.e(TAG, "Call libarary hwsign sign hwmember failed");
                        if (ret != 0) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                    if (ret != 0) {
                        return buf;
                    }
                    return new byte[0];
                }
                Log.e(TAG, "Teec client init error!");
                return new byte[0];
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public int checkKeyStatus(int keyIndex) {
        int ret = -1;
        synchronized (LOCK) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "Teec client re init in check status");
                initClient();
            }
            if (keyIndex == 1 && this.mClientStatus == 0) {
                try {
                    reportInformation(getCallPackageName(), "signCheckKey");
                    ret = signCheckKey();
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "Call libarary hwsign sign hwmember failed");
                }
                if (ret == 0) {
                    return 0;
                }
                return -2;
            }
            Log.e(TAG, "Teec client init error!");
            return -1;
        }
    }

    public int getPublicKey(int keyIndex, byte[] buf) {
        int ret = -1;
        if (buf == null || buf.length <= 0) {
            Log.e(TAG, "The parameter is invalid");
            return -1;
        }
        synchronized (LOCK) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "Teec client re init.");
                initClient();
            }
            if (this.mClientStatus == 0) {
                try {
                    reportInformation(getCallPackageName(), "signGetPublickey");
                    ret = signGetPublickey(keyIndex, buf);
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "Call libarary hwsign get_publickey failed");
                }
            } else {
                Log.e(TAG, "Teec client init error!");
            }
        }
        return ret;
    }

    private String getCallPackageName() {
        Application application = ActivityThreadEx.currentApplication();
        if (application == null) {
            Log.d(TAG, "Get current application failed");
            return UNKNOWN_PACKAGE;
        }
        Context context = application.getApplicationContext();
        if (context == null) {
            Log.d(TAG, "Get context failed");
            return UNKNOWN_PACKAGE;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.d(TAG, "getPackageManager failed");
            return UNKNOWN_PACKAGE;
        }
        String[] pkgList = packageManager.getPackagesForUid(Binder.getCallingUid());
        if (pkgList != null && pkgList.length != 0) {
            return pkgList[0];
        }
        Log.d(TAG, "Package list is null");
        return UNKNOWN_PACKAGE;
    }

    private void reportInformation(String packageName, String funcName) {
        Log.d(TAG, "packageName:" + packageName + "funcName:" + funcName);
        HiViewEx.report(HiViewEx.byJson((int) HIVIEW_ID, String.format(Locale.ROOT, "{PNAMEID:%s,PVERSIONID:%s,PACKAGENAME:%s,FUNCNAME:%s}", PNAME_ID, PVERSION_ID, packageName, funcName)));
    }

    public int getDeviceCertType(int keyIndex) {
        int ret = -1;
        synchronized (LOCK) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "Teec client re init.");
                initClient();
            }
            if (this.mClientStatus == 0) {
                try {
                    ret = signGetCertType(keyIndex);
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "Call libarary hwsign get_cert_type failed");
                }
            } else {
                Log.e(TAG, "Teec client init error!");
            }
        }
        return ret;
    }

    public int getDeviceCert(int keyIndex, int certType, byte[] certBuf) {
        int ret = -1;
        synchronized (LOCK) {
            if (this.mClientStatus != 0) {
                Log.i(TAG, "Teec client re init.");
                initClient();
            }
            if (this.mClientStatus == 0) {
                try {
                    ret = signGetCert(keyIndex, certType, certBuf);
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "Call libarary hwsign get cert failed");
                }
            } else {
                Log.e(TAG, "Teec client init ERROR!");
            }
        }
        return ret;
    }
}
