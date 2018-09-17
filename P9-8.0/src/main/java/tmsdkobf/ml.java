package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.Buffalo;
import tmsdk.common.module.intelli_sms.DecomposeResult;
import tmsdk.common.utils.f;

public class ml {
    private static ml Ah = null;
    private Buffalo Ag = null;
    private int mRefCount = 0;

    private ml() {
        f.h("QQPimSecure", "BuffaloImpl 00");
    }

    public static ml eS() {
        if (Ah == null) {
            Class cls = ml.class;
            synchronized (ml.class) {
                if (Ah == null) {
                    Ah = new ml();
                }
            }
        }
        return Ah;
    }

    public String b(String str, String str2, int i) {
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount > 0) {
                Buffalo buffalo = this.Ag;
                DecomposeResult decomposeResult = new DecomposeResult();
                if (buffalo.nativeCheckSmsHash_c(str, str2, i, decomposeResult) != 0 || decomposeResult.strResult == null) {
                    return null;
                }
                ku.bt(decomposeResult.strResult);
                return decomposeResult.strResult;
            }
            f.d("QQPimSecure", "BumbleBeeImpl checkSms mRefCount==0");
            return null;
        }
    }

    public void eT() {
        f.h("QQPimSecure", "BuffaloImpl 01");
        String b = lu.b(TMSDKContext.getApplicaionContext(), "rule.dat", null);
        if (b != null) {
            Class cls = mm.class;
            synchronized (mm.class) {
                if (this.mRefCount <= 0) {
                    this.Ag = new Buffalo();
                    this.Ag.nativeInitHashChecker_c(b);
                    this.mRefCount = 1;
                    f.h("QQPimSecure", "BuffaloImpl 02");
                    return;
                }
                this.mRefCount++;
            }
        }
    }

    public void eU() {
        f.h("QQPimSecure", "BuffaloImpl 03");
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount > 0) {
                this.mRefCount--;
                if (this.mRefCount <= 0) {
                    if (this.Ag != null) {
                        this.Ag.nativeFinishHashChecker_c();
                    }
                    Ah = null;
                }
                f.h("QQPimSecure", "BuffaloImpl 04");
                return;
            }
        }
    }
}
