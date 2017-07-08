package tmsdkobf;

import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.SmsCheckInput;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;

/* compiled from: Unknown */
public class nl {
    public static final int[][] Cv = null;
    private static nl Cx;
    private nm Cw;
    private int mRefCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.nl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.nl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.nl.<clinit>():void");
    }

    private nl() {
        this.mRefCount = 0;
        this.Cw = null;
        d.g("QQPimSecure", "BumbleBeeImpl 00");
    }

    public static nl fn() {
        if (Cx == null) {
            synchronized (nl.class) {
                if (Cx == null) {
                    Cx = new nl();
                }
            }
        }
        return Cx;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsCheckResult b(SmsEntity smsEntity, Boolean bool) {
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                nm nmVar = this.Cw;
                if (smsEntity == null) {
                    return null;
                }
                SmsCheckResult a;
                SmsCheckInput smsCheckInput = new SmsCheckInput(smsEntity.phonenum, smsEntity.body, Cv[smsEntity.protocolType][0], 0, 0, 0);
                if (bool.booleanValue() && f.iu()) {
                    a = nmVar.a(smsCheckInput);
                    if (a != null) {
                        d.e("BUMBLEBEE", "Bumble cloud scan success! ");
                        a.sIsCloudResult = true;
                    } else {
                        a = new SmsCheckResult();
                    }
                } else {
                    a = new SmsCheckResult();
                }
                SmsCheckResult smsCheckResult = a;
                d.e("BUMBLEBEE", "SmsCheckResult = " + (smsCheckResult == null ? "null" : smsCheckResult.toString()));
                return smsCheckResult;
            }
            d.e("QQPimSecure", "BumbleBeeImpl checkSms mRefCount==0");
            return null;
        }
    }

    public int bL(int i) {
        int i2 = 0;
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                nm nmVar = this.Cw;
                try {
                    i2 = nmVar.nativeCalcMap(i);
                } catch (Throwable th) {
                }
                if (i2 == 3) {
                    i = 3;
                } else if (i2 == 4) {
                    i = 4;
                }
                return i;
            }
            d.e("QQPimSecure", "BumbleBeeImpl filterResult mRefCount==0");
            return i;
        }
    }

    public void fl() {
        d.g("QQPimSecure", "BumbleBeeImpl 01");
        synchronized (nl.class) {
            if (this.mRefCount <= 0) {
                this.mRefCount = 1;
                this.Cw = new nm();
                d.g("QQPimSecure", "BumbleBeeImpl 02");
                return;
            }
            this.mRefCount++;
        }
    }

    public void fm() {
        d.g("QQPimSecure", "BumbleBeeImpl 03");
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                this.mRefCount--;
                if (this.mRefCount <= 0) {
                    this.Cw.fp();
                    this.Cw = null;
                    Cx = null;
                }
                d.g("QQPimSecure", "BumbleBeeImpl 04");
                return;
            }
        }
    }

    public SmsCheckResult t(String str, String str2) {
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                nm nmVar = this.Cw;
                if (str == null || str2 == null) {
                    return null;
                }
                SmsCheckResult u = nmVar.u(str, str2);
                d.e("QQPimSecure", "SmsCheckResult = " + (u == null ? "null" : u.toString()));
                return u;
            }
            d.e("QQPimSecure", "BumbleBeeImpl isPaySms mRefCount==0");
            return null;
        }
    }
}
