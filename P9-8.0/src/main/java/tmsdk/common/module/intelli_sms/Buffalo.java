package tmsdk.common.module.intelli_sms;

import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;
import tmsdkobf.ma;

public class Buffalo {
    private static boolean isLoadNativeOK;
    private volatile boolean Ac = false;

    static {
        isLoadNativeOK = false;
        isLoadNativeOK = ma.f(TMSDKContext.getApplicaionContext(), "buffalo-1.0.0-mfr");
    }

    public static boolean isLoadNative() {
        if (!isLoadNativeOK) {
            isLoadNativeOK = ma.f(TMSDKContext.getApplicaionContext(), "buffalo-1.0.0-mfr");
        }
        f.d("QQPimSecure", "Buffalo isLoadNativeOK? " + isLoadNativeOK);
        return isLoadNativeOK;
    }

    public native int nativeCheckSmsHash(String str, String str2, int i, DecomposeResult decomposeResult);

    public synchronized int nativeCheckSmsHash_c(String str, String str2, int i, DecomposeResult decomposeResult) {
        if (!this.Ac) {
            return -1;
        }
        return nativeCheckSmsHash(str, str2, i, decomposeResult);
    }

    public native void nativeFinishHashChecker();

    public synchronized void nativeFinishHashChecker_c() {
        nativeFinishHashChecker();
        this.Ac = false;
    }

    public native int nativeInitHashChecker(String str);

    public synchronized boolean nativeInitHashChecker_c(String str) {
        if (nativeInitHashChecker(str) == 0) {
            this.Ac = true;
        }
        return this.Ac;
    }
}
