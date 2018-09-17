package tmsdk.common.module.intelli_sms;

import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;
import tmsdkobf.ma;
import tmsdkobf.ml;

public class NativeBumblebee {
    private static boolean isLoadNativeOK;
    private volatile boolean Ac = false;
    private ml Af;

    static {
        isLoadNativeOK = false;
        isLoadNativeOK = ma.f(TMSDKContext.getApplicaionContext(), "bumblebee-1.0.4-mfr");
    }

    public static boolean isLoadNative() {
        if (!isLoadNativeOK) {
            isLoadNativeOK = ma.f(TMSDKContext.getApplicaionContext(), "bumblebee-1.0.4-mfr");
        }
        f.d("QQPimSecure", "Bumble isLoadNativeOK? " + isLoadNativeOK);
        return isLoadNativeOK;
    }

    public native int nativeCalcMap(int i);

    public synchronized int nativeCalcMap_c(int i) {
        if (!this.Ac) {
            return 0;
        }
        return nativeCalcMap(i);
    }

    public native int nativeCheckSms(SmsCheckInput smsCheckInput, SmsCheckResult smsCheckResult);

    public synchronized int nativeCheckSms_c(SmsCheckInput smsCheckInput, SmsCheckResult smsCheckResult) {
        if (!this.Ac) {
            return -1;
        }
        if (this.Af != null) {
            this.Af.b(smsCheckInput.sender, smsCheckInput.sms, smsCheckInput.uiSmsType);
        }
        return nativeCheckSms(smsCheckInput, smsCheckResult);
    }

    public native void nativeFinishSmsChecker();

    public synchronized void nativeFinishSmsChecker_c() {
        nativeFinishSmsChecker();
        this.Ac = false;
        if (this.Af != null) {
            this.Af.eU();
        }
    }

    public native String nativeGetSmsInfo(String str, String str2);

    public synchronized String nativeGetSmsInfo_c(String str, String str2) {
        if (!this.Ac) {
            return null;
        }
        return nativeGetSmsInfo(str, str2);
    }

    public native int nativeInitSmsChecker(int i, String str);

    public synchronized int nativeInitSmsChecker_c(int i, String str) {
        int nativeInitSmsChecker;
        nativeInitSmsChecker = nativeInitSmsChecker(i, str);
        if (nativeInitSmsChecker == 0) {
            this.Ac = true;
            this.Af = ml.eS();
            this.Af.eT();
        }
        return nativeInitSmsChecker;
    }

    public native int nativeIsPrivateSms(String str, String str2);

    public synchronized int nativeIsPrivateSms_c(String str, String str2) {
        if (!this.Ac) {
            return -1;
        }
        return nativeIsPrivateSms(str, str2);
    }
}
