package com.huawei.trustzone;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.HiViewEx;
import com.huawei.coauth.pool.CoAuthResService;
import java.util.Locale;

public class Signature {
    private static final int HIVIEW_ID = 992770101;
    private static final String PNAME_ID = "signtool";
    private static final String PVERSION_ID = "11.0.1";
    private static final int STATE_FAIL = -1;
    private static final String TAG = "Signature";
    private static final int TA_CLIENT_STATUS_INIT_FAIL = -3;
    private static final int TA_CLIENT_STATUS_OK = 0;
    private static final int TA_CLIENT_STATUS_UNINIT = -2;
    private static final String UNKNOWN_PACKAGE = "unknown_package";
    private static Signature sInstance = null;
    private int mTaClientStatus = -2;

    private Signature() {
        initTaClient();
    }

    public static synchronized Signature getInstance() {
        Signature signature;
        synchronized (Signature.class) {
            if (sInstance == null) {
                try {
                    sInstance = new Signature();
                } catch (UnsatisfiedLinkError e) {
                    Log.e(TAG, "LoadLibrary occurs error");
                    return null;
                }
            }
            signature = sInstance;
        }
        return signature;
    }

    private void initTaClient() {
        this.mTaClientStatus = -1;
        reportInformation(getCallPackageName(), "teecInit");
        int ret = SignatureNative.teecInit();
        if (ret == 0) {
            this.mTaClientStatus = 0;
            Log.i(TAG, "Tee client init OK");
            return;
        }
        this.mTaClientStatus = -3;
        Log.e(TAG, "Tee client init fail, ret = " + ret);
    }

    public int signMessage(byte[] challenge, int challengeLen, byte[] buf) {
        if (challenge == null || challengeLen <= 0 || buf == null || buf.length <= 0) {
            Log.e(TAG, "Sign message parameter is invalid");
            return -1;
        } else if (this.mTaClientStatus == 0) {
            reportInformation(getCallPackageName(), CoAuthResService.KEY_SIGN);
            return SignatureNative.sign(challenge, challengeLen, buf);
        } else {
            Log.e(TAG, "Teec client init error");
            return -1;
        }
    }

    public int getDeviceKeyStatus() {
        if (this.mTaClientStatus == 0) {
            reportInformation(getCallPackageName(), "checkDeviceKey");
            return SignatureNative.checkDeviceKey();
        }
        Log.e(TAG, "Teec client init ERROR!");
        return -1;
    }

    private static String getCallPackageName() {
        Application application = ActivityThreadEx.currentApplication();
        if (application == null) {
            Log.e(TAG, "Get current application failed");
            return UNKNOWN_PACKAGE;
        }
        Context context = application.getApplicationContext();
        if (context != null) {
            return context.getPackageName();
        }
        Log.e(TAG, "Get context failed");
        return UNKNOWN_PACKAGE;
    }

    private static void reportInformation(String packageName, String funcName) {
        Log.d(TAG, "PackageName:" + packageName + "funcName:" + funcName);
        HiViewEx.report(HiViewEx.byJson((int) HIVIEW_ID, String.format(Locale.ROOT, "{PNAMEID:%s,PVERSIONID:%s,PACKAGENAME:%s,FUNCNAME:%s}", PNAME_ID, PVERSION_ID, packageName, funcName)));
    }
}
