package tmsdkobf;

import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.MMatchSysResult;
import tmsdk.common.module.intelli_sms.SmsCheckInput;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdk.common.utils.s;

public class mm {
    public static final int[][] Ai;
    private static mm Ak = null;
    private mn Aj = null;
    private int mRefCount = 0;

    static {
        r0 = new int[3][];
        r0[0] = new int[]{0, 0};
        r0[1] = new int[]{1, 1};
        r0[2] = new int[]{2, 2};
        Ai = r0;
    }

    private mm() {
        f.h("QQPimSecure", "BumbleBeeImpl 00");
    }

    public static mm eV() {
        if (Ak == null) {
            Class cls = mm.class;
            synchronized (mm.class) {
                if (Ak == null) {
                    Ak = new mm();
                }
            }
        }
        return Ak;
    }

    public MMatchSysResult a(SmsEntity smsEntity, Boolean bool) {
        if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
            smsEntity.protocolType = 0;
        }
        SmsCheckResult b = b(smsEntity, bool);
        if (b == null) {
            return new MMatchSysResult(1, 1, 0, 0, 1, null);
        }
        MMatchSysResult mMatchSysResult = new MMatchSysResult(b);
        mMatchSysResult.contentType = aU(mMatchSysResult.contentType);
        return mMatchSysResult;
    }

    public int aU(int -l_2_I) {
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount > 0) {
                mn mnVar = this.Aj;
                int i = 0;
                try {
                    i = mnVar.nativeCalcMap(-l_2_I);
                } catch (Throwable th) {
                }
                if (i == 3) {
                    -l_2_I = 3;
                } else if (i == 4) {
                    -l_2_I = 4;
                }
                return -l_2_I;
            }
            f.d("QQPimSecure", "BumbleBeeImpl filterResult mRefCount==0");
            return -l_2_I;
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0045, code:
            if (r7.a(r0, r8) == 0) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:36:0x00a5, code:
            if (r7.a(r0, r8) != 0) goto L_0x00a7;
     */
    /* JADX WARNING: Missing block: B:37:0x00a7, code:
            r8 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsCheckResult b(SmsEntity smsEntity, Boolean bool) {
        s.bW(512);
        kt.aE(1320031);
        if (bool.booleanValue()) {
            kt.aE(1320004);
        }
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount > 0) {
                mn mnVar = this.Aj;
                if (smsEntity == null) {
                    return null;
                }
                SmsCheckResult a;
                SmsCheckInput smsCheckInput = new SmsCheckInput(smsEntity.phonenum, smsEntity.body, Ai[smsEntity.protocolType][0], 0, 0, 0);
                if (bool.booleanValue() && i.iE()) {
                    a = mnVar.a(smsCheckInput);
                    if (a != null) {
                        f.d("BUMBLEBEE", "Bumble cloud scan success! ");
                        a.sIsCloudResult = true;
                    } else {
                        a = new SmsCheckResult();
                    }
                } else {
                    a = new SmsCheckResult();
                }
                if (bool.booleanValue()) {
                    kr.dz();
                }
                f.d("BUMBLEBEE", "SmsCheckResult = " + (a == null ? "null" : a.toString()));
                return a;
            }
            f.d("QQPimSecure", "BumbleBeeImpl checkSms mRefCount==0");
            return null;
        }
    }

    public void eT() {
        f.h("QQPimSecure", "BumbleBeeImpl 01");
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount <= 0) {
                this.mRefCount = 1;
                this.Aj = new mn();
                f.h("QQPimSecure", "BumbleBeeImpl 02");
                return;
            }
            this.mRefCount++;
        }
    }

    public void eU() {
        f.h("QQPimSecure", "BumbleBeeImpl 03");
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount > 0) {
                this.mRefCount--;
                if (this.mRefCount <= 0) {
                    this.Aj.eX();
                    this.Aj = null;
                    Ak = null;
                }
                f.h("QQPimSecure", "BumbleBeeImpl 04");
                return;
            }
        }
    }

    public SmsCheckResult t(String str, String str2) {
        Class cls = mm.class;
        synchronized (mm.class) {
            if (this.mRefCount > 0) {
                mn mnVar = this.Aj;
                if (str == null || str2 == null) {
                    return null;
                }
                SmsCheckResult u = mnVar.u(str, str2);
                f.d("QQPimSecure", "SmsCheckResult = " + (u == null ? "null" : u.toString()));
                return u;
            }
            f.d("QQPimSecure", "BumbleBeeImpl isPaySms mRefCount==0");
            return null;
        }
    }
}
